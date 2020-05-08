 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class is adapted from org.eclipse.compare.internal.merge.DocumentMerger
  * and is used to do two or three-way compares
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.utils;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.ISharedDocumentAdapter;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.SharedDocumentAdapter;
 import org.eclipse.compare.contentmergeviewer.ITokenComparator;
 import org.eclipse.compare.contentmergeviewer.TokenComparator;
 import org.eclipse.compare.rangedifferencer.IRangeComparator;
 import org.eclipse.compare.rangedifferencer.RangeDifference;
 import org.eclipse.compare.rangedifferencer.RangeDifferencer;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPositionCategoryException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.Position;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4ECompareEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileRevisionTypedElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4EFileTypedElement;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 
 public class DiffUtils {
 
 	public static final double TOO_LONG = 10000000.0; // the value of N*M when to start binding the run time
 
 	private static final String DIFF_RANGE_CATEGORY = Activator.PLUGIN_ID + ".DIFF_RANGE_CATEGORY"; //$NON-NLS-1$
 
 	/** Selects between smartTokenDiff and mergingTokenDiff */
 	private static final boolean USE_MERGING_TOKEN_DIFF = false;
 
 	public static final String OPTIMIZED_ALGORITHM_USED = "OPTIMIZED_ALGORITHM_USED"; //$NON-NLS-1$
 
 	/**
 	 * Perform a two level 2- or 3-way diff. The first level is based on line comparison, the second level on token
 	 * comparison.
 	 * 
 	 * @throws CoreException
 	 */
 	public ArrayList<Diff> doDiff(boolean aIsThreeWay, boolean aIgnoreWhitespace, R4ECompareEditorInput input)
 			throws CoreException {
 
 		//THese structures will be used to hold found differences
 		ArrayList<Diff> changeDiffs = new ArrayList<Diff>();
 		ArrayList<Diff> allDiffs;
 
 		//Get documents to compare form input
 		IDocument lDoc = getDocument(input.getLeftElement());
 		IDocument rDoc = getDocument(input.getRightElement());
 		CompareConfiguration config = input.getCompareConfiguration();
 
 		if (lDoc == null || rDoc == null) {
			return changeDiffs; //Nothing to compare
 		}
 
 		IDocument aDoc = null; //No ancestor by default
 		Position aRegion = null; //We will compare whole files only
 		if (aIsThreeWay && null != input.getAncestorElement()) {
 			aDoc = getDocument(input.getAncestorElement());
 		}
 
 		resetPositions(lDoc);
 		resetPositions(rDoc);
 		resetPositions(aDoc);
 
 		boolean ignoreWhiteSpace = aIgnoreWhitespace;
 
 		DocLineComparator sright = new DocLineComparator(rDoc, null, ignoreWhiteSpace);
 		DocLineComparator sleft = new DocLineComparator(lDoc, null, ignoreWhiteSpace);
 		DocLineComparator sancestor = null;
 		if (aDoc != null) {
 			sancestor = new DocLineComparator(aDoc, null, ignoreWhiteSpace);
 		}
 
 		final Object[] result = new Object[1];
 		final DocLineComparator sa = sancestor, sl = sleft, sr = sright;
 		IRunnableWithProgress runnable = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
 				monitor.beginTask("Computing differences...", maxWork(sa, sl, sr)); //$NON-NLS-1$
 				try {
 					result[0] = RangeDifferencer.findRanges(monitor, sa, sl, sr);
 				} catch (OutOfMemoryError ex) {
 					System.gc();
 					throw new InvocationTargetException(ex);
 				}
 				if (monitor.isCanceled()) { // canceled
 					throw new InterruptedException();
 				}
 				monitor.done();
 			}
 		};
 
 		RangeDifference[] e = null;
 		try {
 			config.getContainer().run(true, true, runnable);
 			e = (RangeDifference[]) result[0];
 		} catch (InvocationTargetException ex) {
 			// we create a NOCHANGE range for the whole document
 			Diff diff = new Diff(null, RangeDifference.NOCHANGE, aDoc, aRegion, 0, aDoc != null ? aDoc.getLength() : 0,
 					lDoc, null, 0, lDoc.getLength(), rDoc, null, 0, rDoc.getLength(), aIsThreeWay, config);
 
 			allDiffs = new ArrayList<Diff>();
 			allDiffs.add(diff);
 			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Too many differences found", //$NON-NLS-1$
 					ex.getTargetException()));
 		} catch (InterruptedException ex) {
 			// we create a NOCHANGE range for the whole document
 			Diff diff = new Diff(null, RangeDifference.NOCHANGE, aDoc, aRegion, 0, aDoc != null ? aDoc.getLength() : 0,
 					lDoc, null, 0, lDoc.getLength(), rDoc, null, 0, rDoc.getLength(), aIsThreeWay, config);
 
 			allDiffs = new ArrayList<Diff>();
 			allDiffs.add(diff);
			return changeDiffs;
 		}
 
 		if (isCapped(sa, sl, sr)) {
 			config.setProperty(OPTIMIZED_ALGORITHM_USED, new Boolean(true));
 		} else {
 			config.setProperty(OPTIMIZED_ALGORITHM_USED, new Boolean(false));
 		}
 
 		ArrayList<Diff> newAllDiffs = new ArrayList<Diff>();
 		for (RangeDifference es : e) {
 			int ancestorStart = 0;
 			int ancestorEnd = 0;
 			if (sancestor != null) {
 				ancestorStart = sancestor.getTokenStart(es.ancestorStart());
 				ancestorEnd = getTokenEnd2(sancestor, es.ancestorStart(), es.ancestorLength());
 			}
 
 			int leftStart = sleft.getTokenStart(es.leftStart());
 			int leftEnd = getTokenEnd2(sleft, es.leftStart(), es.leftLength());
 
 			int rightStart = sright.getTokenStart(es.rightStart());
 			int rightEnd = getTokenEnd2(sright, es.rightStart(), es.rightLength());
 
 			Diff diff = new Diff(null, es.kind(), aDoc, aRegion, ancestorStart, ancestorEnd, lDoc, null, leftStart,
 					leftEnd, rDoc, null, rightStart, rightEnd, aIsThreeWay, config);
 
 			newAllDiffs.add(diff); // remember all range diffs for scrolling
 
 			if (isPatchHunk()) {
 				if (useChange(diff, config)) {
 					recordChangeDiff(diff, changeDiffs);
 				}
 			} else {
 				if (ignoreWhiteSpace || useChange(es.kind(), config)) {
 
 					// Extract the string for each contributor.
 					String a = null;
 					if (sancestor != null) {
 						a = extract2(aDoc, sancestor, es.ancestorStart(), es.ancestorLength());
 					}
 					String s = extract2(lDoc, sleft, es.leftStart(), es.leftLength());
 					String d = extract2(rDoc, sright, es.rightStart(), es.rightLength());
 
 					// Indicate whether all contributors are whitespace
 					if (ignoreWhiteSpace && (a == null || a.trim().length() == 0) && s.trim().length() == 0
 							&& d.trim().length() == 0) {
 						diff.fIsWhitespace = true;
 					}
 
 					// If the diff is of interest, record it and generate the token diffs
 					if (useChange(diff, config)) {
 						recordChangeDiff(diff, changeDiffs);
 						if (s.length() > 0 && d.length() > 0) {
 							if (a == null && sancestor != null) {
 								a = extract2(aDoc, sancestor, es.ancestorStart(), es.ancestorLength());
 							}
 							if (USE_MERGING_TOKEN_DIFF) {
 								mergingTokenDiff(diff, aDoc, a, rDoc, d, lDoc, s, aIsThreeWay, config);
 							} else {
 								simpleTokenDiff(diff, aDoc, a, rDoc, d, lDoc, s, aIsThreeWay, config);
 							}
 						}
 					}
 				}
 			}
 		}
 		changeDiffs = newAllDiffs;
 		return changeDiffs;
 	}
 
 	private IDocument getDocument(ITypedElement aElement) throws CoreException {
 
		ISharedDocumentAdapter adapter = null;
		IEditorInput editorInput = null;
 		if (aElement instanceof R4EFileTypedElement) {
 			adapter = (ISharedDocumentAdapter) ((R4EFileTypedElement) aElement).getAdapter(ISharedDocumentAdapter.class);
			if (null != adapter) {
				editorInput = adapter.getDocumentKey(aElement);
			}
 		} else if (aElement instanceof R4EFileRevisionTypedElement) {
 			adapter = (ISharedDocumentAdapter) ((R4EFileRevisionTypedElement) aElement).getAdapter(ISharedDocumentAdapter.class);
			if (null != adapter) {
				editorInput = adapter.getDocumentKey(aElement);
			}
 		} else {
 			return null; //Wrong input type
 		}
		if (null == adapter || null == editorInput) {
			return null; //Cannot find editor input
		}
 		IDocumentProvider provider = SharedDocumentAdapter.getDocumentProvider(editorInput);
		if (null == provider) {
			return null; //Cannot find document provider
		}

 		adapter.connect(provider, editorInput);
 		return provider.getDocument(editorInput);
 	}
 
 	private void resetPositions(IDocument doc) {
 		if (doc == null) {
 			return;
 		}
 		try {
 			doc.removePositionCategory(DIFF_RANGE_CATEGORY);
 		} catch (BadPositionCategoryException e) {
 			// Ignore
 		}
 		doc.addPositionCategory(DIFF_RANGE_CATEGORY);
 	}
 
 	private static int maxWork(IRangeComparator a, IRangeComparator l, IRangeComparator r) {
 		int ln = l.getRangeCount();
 		int rn = r.getRangeCount();
 		if (a != null) {
 			int an = a.getRangeCount();
 			return (2 * Math.max(an, ln)) + (2 * Math.max(an, rn));
 		}
 		return 2 * Math.max(ln, rn);
 	}
 
 	private boolean isCapped(DocLineComparator ancestor, DocLineComparator left, DocLineComparator right) {
 		int aLength = ancestor == null ? 0 : ancestor.getRangeCount();
 		int lLength = left.getRangeCount();
 		int rLength = right.getRangeCount();
 		if ((double) aLength * (double) lLength > TOO_LONG || (double) aLength * (double) rLength > TOO_LONG
 				|| (double) lLength * (double) rLength > TOO_LONG) {
 			return true;
 		}
 		return false;
 	}
 
 	private static int getTokenEnd2(ITokenComparator tc, int start, int length) {
 		return tc.getTokenStart(start + length);
 	}
 
 	/**
 	 * Returns the content of lines in the specified range as a String. This includes the line separators.
 	 * 
 	 * @param doc
 	 *            the document from which to extract the characters
 	 * @param start
 	 *            index of first line
 	 * @param length
 	 *            number of lines
 	 * @return the contents of the specified line range as a String
 	 */
 	private String extract2(IDocument doc, ITokenComparator tc, int start, int length) {
 		int count = tc.getRangeCount();
 		if (length > 0 && count > 0) {
 			int startPos = tc.getTokenStart(start);
 			int endPos;
 
 			if (length == 1) {
 				endPos = startPos + tc.getTokenLength(start);
 			} else {
 				endPos = tc.getTokenStart(start + length);
 			}
 
 			try {
 				return doc.get(startPos, endPos - startPos);
 			} catch (BadLocationException e) {
 				// silently ignored
 			}
 
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	private boolean isPatchHunk() {
 		return false;
 	}
 
 	/*
 	 * Returns true if kind of change should be shown.
 	 */
 	public boolean useChange(Diff diff, CompareConfiguration aConfig) {
 		if (diff.fIsWhitespace) {
 			return false;
 		}
 		int kind = diff.getKind();
 		return useChange(kind, aConfig);
 	}
 
 	private boolean useChange(int kind, CompareConfiguration aConfig) {
 		if (kind == RangeDifference.NOCHANGE) {
 			return false;
 		}
 		if (aConfig.isChangeIgnored(kind)) {
 			return false;
 		}
 		if (kind == RangeDifference.ANCESTOR) {
 			return false;
 		}
 		return true;
 	}
 
 	private void recordChangeDiff(Diff diff, ArrayList<Diff> aChangeDiffs) {
 		aChangeDiffs.add(diff); // here we remember only the real diffs
 	}
 
 	/*
 	 * Performs a "smart" token based 3-way diff on the character range specified by the given baseDiff.
 	 * It is "smart" because it tries to minimize the number of token diffs by merging them.
 	 */
 	private void mergingTokenDiff(Diff baseDiff, IDocument ancestorDoc, String a, IDocument rightDoc, String d,
 			IDocument leftDoc, String s, boolean aIsThreeWay, CompareConfiguration aConfig) {
 		ITokenComparator sa = null;
 		int ancestorStart = 0;
 		if (ancestorDoc != null) {
 			sa = createTokenComparator(a);
 			ancestorStart = baseDiff.fAncestorPos.getOffset();
 		}
 
 		int rightStart = baseDiff.fRightPos.getOffset();
 		ITokenComparator sm = createTokenComparator(d);
 
 		int leftStart = baseDiff.fLeftPos.getOffset();
 		ITokenComparator sy = createTokenComparator(s);
 
 		RangeDifference[] r = RangeDifferencer.findRanges(sa, sy, sm);
 		for (int i = 0; i < r.length; i++) {
 			RangeDifference es = r[i];
 			// determine range of diffs in one line
 			int start = i;
 			int leftLine = -1;
 			int rightLine = -1;
 			try {
 				leftLine = leftDoc.getLineOfOffset(leftStart + sy.getTokenStart(es.leftStart()));
 				rightLine = rightDoc.getLineOfOffset(rightStart + sm.getTokenStart(es.rightStart()));
 			} catch (BadLocationException e) {
 				// silently ignored
 			}
 			i++;
 			for (; i < r.length; i++) {
 				es = r[i];
 				try {
 					if (leftLine != leftDoc.getLineOfOffset(leftStart + sy.getTokenStart(es.leftStart()))) {
 						break;
 					}
 					if (rightLine != rightDoc.getLineOfOffset(rightStart + sm.getTokenStart(es.rightStart()))) {
 						break;
 					}
 				} catch (BadLocationException e) {
 					// silently ignored
 				}
 			}
 			int end = i;
 
 			// find first diff from left
 			RangeDifference first = null;
 			for (int ii = start; ii < end; ii++) {
 				es = r[ii];
 				if (useChange(es.kind(), aConfig)) {
 					first = es;
 					break;
 				}
 			}
 
 			// find first diff from mine
 			RangeDifference last = null;
 			for (int ii = end - 1; ii >= start; ii--) {
 				es = r[ii];
 				if (useChange(es.kind(), aConfig)) {
 					last = es;
 					break;
 				}
 			}
 
 			if (first != null && last != null) {
 
 				int ancestorStart2 = 0;
 				int ancestorEnd2 = 0;
 				if (ancestorDoc != null && null != sa) {
 					ancestorStart2 = ancestorStart + sa.getTokenStart(first.ancestorStart());
 					ancestorEnd2 = ancestorStart + getTokenEnd(sa, last.ancestorStart(), last.ancestorLength());
 				}
 
 				int leftStart2 = leftStart + sy.getTokenStart(first.leftStart());
 				int leftEnd2 = leftStart + getTokenEnd(sy, last.leftStart(), last.leftLength());
 
 				int rightStart2 = rightStart + sm.getTokenStart(first.rightStart());
 				int rightEnd2 = rightStart + getTokenEnd(sm, last.rightStart(), last.rightLength());
 				Diff diff = new Diff(baseDiff, first.kind(), ancestorDoc, null, ancestorStart2, ancestorEnd2, leftDoc,
 						null, leftStart2, leftEnd2, rightDoc, null, rightStart2, rightEnd2, aIsThreeWay, aConfig);
 				diff.fIsToken = true;
 				baseDiff.add(diff);
 			}
 		}
 	}
 
 	/*
 	 * Performs a token based 3-way diff on the character range specified by the given baseDiff.
 	 */
 	private void simpleTokenDiff(final Diff baseDiff, IDocument ancestorDoc, String a, IDocument rightDoc, String d,
 			IDocument leftDoc, String s, boolean aIsThreeWay, CompareConfiguration aConfig) {
 
 		int ancestorStart = 0;
 		ITokenComparator sa = null;
 		if (ancestorDoc != null) {
 			ancestorStart = baseDiff.fAncestorPos.getOffset();
 			sa = createTokenComparator(a);
 		}
 
 		int rightStart = baseDiff.fRightPos.getOffset();
 		ITokenComparator sm = createTokenComparator(d);
 
 		int leftStart = baseDiff.fLeftPos.getOffset();
 		ITokenComparator sy = createTokenComparator(s);
 
 		RangeDifference[] e = RangeDifferencer.findRanges(sa, sy, sm);
 		for (RangeDifference es : e) {
 			int kind = es.kind();
 			if (kind != RangeDifference.NOCHANGE) {
 
 				int ancestorStart2 = ancestorStart;
 				int ancestorEnd2 = ancestorStart;
 				if (ancestorDoc != null && null != sa) {
 					ancestorStart2 += sa.getTokenStart(es.ancestorStart());
 					ancestorEnd2 += getTokenEnd(sa, es.ancestorStart(), es.ancestorLength());
 				}
 
 				int leftStart2 = leftStart + sy.getTokenStart(es.leftStart());
 				int leftEnd2 = leftStart + getTokenEnd(sy, es.leftStart(), es.leftLength());
 
 				int rightStart2 = rightStart + sm.getTokenStart(es.rightStart());
 				int rightEnd2 = rightStart + getTokenEnd(sm, es.rightStart(), es.rightLength());
 
 				Diff diff = new Diff(baseDiff, kind, ancestorDoc, null, ancestorStart2, ancestorEnd2, leftDoc, null,
 						leftStart2, leftEnd2, rightDoc, null, rightStart2, rightEnd2, aIsThreeWay, aConfig);
 
 				// ensure that token diff is smaller than basediff
 				int leftS = baseDiff.fLeftPos.offset;
 				int leftE = baseDiff.fLeftPos.offset + baseDiff.fLeftPos.length;
 				int rightS = baseDiff.fRightPos.offset;
 				int rightE = baseDiff.fRightPos.offset + baseDiff.fRightPos.length;
 				if (leftS != leftStart2 || leftE != leftEnd2 || rightS != rightStart2 || rightE != rightEnd2) {
 					diff.fIsToken = true;
 					// add to base Diff
 					baseDiff.add(diff);
 				}
 			}
 		}
 	}
 
 	private ITokenComparator createTokenComparator(String s) {
 		return new TokenComparator(s);
 	}
 
 	private int getTokenEnd(ITokenComparator tc, int start, int count) {
 		if (count <= 0) {
 			return tc.getTokenStart(start);
 		}
 		int index = start + count - 1;
 		return tc.getTokenStart(index) + tc.getTokenLength(index);
 	}
 }
