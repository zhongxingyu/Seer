 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentPartitioner;
 import org.eclipse.jface.text.ITypedRegion;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.TypedRegion;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 
 /**
  * Computes the partitions of a document using the Intent parser.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentPartitioner implements IDocumentPartitioner {
 
 	private static final Map<Pattern, Integer> KIND_BY_REGEXPS;
 
 	private static final int SU_START_KIND = 0;
 
 	private static final int SU_END_KIND = 1;
 
 	private static final int MU_KIND = 2;
 
 	/** The legal content types of this partitioner. */
 	protected final String[] fLegalContentTypes;
 
 	/** The partitioner's document. */
 	protected IDocument document;
 
 	private List<IntentRegion> regions = new ArrayList<IntentRegion>();
 	static {
 		KIND_BY_REGEXPS = new LinkedHashMap<Pattern, Integer>();
 		KIND_BY_REGEXPS.put(Pattern.compile("@M((?!M@).)*M@", Pattern.MULTILINE | Pattern.DOTALL), MU_KIND);
 		KIND_BY_REGEXPS.put(Pattern.compile("Document\\s*\\{\\s*", Pattern.MULTILINE | Pattern.DOTALL),
 				SU_START_KIND);
 		KIND_BY_REGEXPS.put(Pattern.compile("Chapter\\s*\\{\\s*", Pattern.MULTILINE | Pattern.DOTALL),
 				SU_START_KIND);
 		KIND_BY_REGEXPS.put(Pattern.compile("Section\\s*\\{\\s*", Pattern.MULTILINE | Pattern.DOTALL),
 				SU_START_KIND);
 		KIND_BY_REGEXPS.put(Pattern.compile("}\\s*", Pattern.MULTILINE | Pattern.DOTALL), SU_END_KIND);
 	}
 
 	/**
 	 * Creates a new Partitioner using the given content types.
 	 * 
 	 * @param legalContentTypes
 	 *            the content types
 	 */
 	public IntentPartitioner(String[] legalContentTypes) {
 		fLegalContentTypes = TextUtilities.copy(legalContentTypes);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#connect(org.eclipse.jface.text.IDocument)
 	 */
 	public void connect(IDocument currentDocument) {
 		document = currentDocument;
 		updateRegions();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#disconnect()
 	 */
 	public void disconnect() {
 		// do nothing
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
 	 */
 	public void documentAboutToBeChanged(DocumentEvent event) {
 		// do nothing
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#getContentType(int)
 	 */
 	public String getContentType(int offset) {
 		ITypedRegion region = getPartition(offset);
 		if (region != null) {
 			return region.getType();
 		}
 		return IntentDocumentProvider.INTENT_DESCRIPTIONUNIT;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#getLegalContentTypes()
 	 */
 	public String[] getLegalContentTypes() {
 		return TextUtilities.copy(fLegalContentTypes);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#documentChanged(org.eclipse.jface.text.DocumentEvent)
 	 */
 	public boolean documentChanged(final DocumentEvent event) {
 		updateRegions();
 		return false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#computePartitioning(int, int)
 	 */
 	public ITypedRegion[] computePartitioning(int offset, int length) {
 		ITypedRegion[] res = regions.toArray(new ITypedRegion[regions.size()]);
 		Arrays.sort(res, new Comparator<ITypedRegion>() {
 
 			public int compare(ITypedRegion o1, ITypedRegion o2) {
 				return Integer.valueOf(o1.getOffset()).compareTo(Integer.valueOf(o2.getOffset()));
 			}
 		});
 		return res;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.IDocumentPartitioner#getPartition(int)
 	 */
 	public ITypedRegion getPartition(int offset) {
 		for (ITypedRegion region : regions) {
 			if (region.getOffset() <= offset && (region.getOffset() + region.getLength()) >= offset) {
 				return region;
 			}
 		}
 		return new TypedRegion(offset, 1, IntentDocumentProvider.INTENT_DESCRIPTIONUNIT);
 	}
 
 	private boolean alreadyIncluded(List<IntentRegion> existingRegions, ITypedRegion region) {
 		for (IntentRegion existingRegion : existingRegions) {
 			if (existingRegion.contains(region)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Regexp-based partitions computation.
 	 */
 	private void updateRegions() {
 		List<IntentRegion> newRegions = new ArrayList<IntentRegion>();
 		String text = document.get();
 
 		// Step 1 : Computing simple partitions: modeling units & structural content
 		for (Entry<Pattern, Integer> regexpEntry : KIND_BY_REGEXPS.entrySet()) {
 			Matcher m = regexpEntry.getKey().matcher(text);
 			while (m.find()) {
 				IntentRegion newRegion = createIntentRegion(m.start(), m.end() - m.start(),
 						regexpEntry.getValue());
 				if (!alreadyIncluded(newRegions, newRegion)) {
 					newRegions.add(newRegion);
 				}
 			}
 		}
 
 		// Step 2 : Sorting partitions & fill the blanks with description units
 		Collections.sort(newRegions);
 		int lastOffset = 0;
 		IntentRegion lastRegion = null;
 		List<IntentRegion> regionsToAdd = new ArrayList<IntentRegion>();
 		for (IntentRegion region : newRegions) {
 			if (region.getOffset() > lastOffset) {
 				// we fill the blanks with description units partitions
 				regionsToAdd.addAll(createDescriptionAndTitle(lastOffset, region.getOffset() - lastOffset,
 						lastRegion));
 			}
 			lastOffset = region.getOffset() + region.getLength();
 			lastRegion = region;
 		}
 
 		// Step 3 : Adding new partitions to the existing
 		for (IntentRegion intentTokenToAdd : regionsToAdd) {
 			newRegions.add(intentTokenToAdd);
 		}
 		Collections.sort(newRegions);
 		regions = newRegions;
 	}
 
 	/**
 	 * Creates the region for the description unit and its title if present.
 	 * 
 	 * @param offset
 	 *            the description unit offset
 	 * @param length
 	 *            the description unit length
 	 * @param previousRegion
 	 *            the previous region contentType
 	 * @return the description unit & the title if present
 	 */
 	private List<IntentRegion> createDescriptionAndTitle(int offset, int length, IntentRegion previousRegion) {
 		List<IntentRegion> unitRegions = new ArrayList<IntentRegion>();
 		int unitOffset = offset;
 		int unitLength = length;
		if (SU_START_KIND == previousRegion.getKind()) {
 			try {
 				String text = document.get(unitOffset, unitLength);
 				String[] lines = text.split("\\n");
 				if (lines.length > 2) {
 					final int titleLength = lines[0].length();
 					unitRegions.add(new IntentRegion(unitOffset, titleLength,
 							IntentDocumentProvider.INTENT_TITLE));
 					unitOffset += titleLength;
 					unitLength -= titleLength;
 				}
 			} catch (BadLocationException e) {
 				IntentUiLogger.logError(e);
 			}
 		}
 		unitRegions.add(new IntentRegion(unitOffset, unitLength,
 				IntentDocumentProvider.INTENT_DESCRIPTIONUNIT));
 		return unitRegions;
 	}
 
 	public IntentRegion createIntentRegion(int offset, int length, int kind) {
 		String type = IntentDocumentProvider.INTENT_DESCRIPTIONUNIT;
 		switch (kind) {
 			case -1:
 				break;
 			case SU_START_KIND:
 				type = IntentDocumentProvider.INTENT_STRUCTURAL_CONTENT;
 				break;
 			case SU_END_KIND:
 				type = IntentDocumentProvider.INTENT_STRUCTURAL_CONTENT;
 				break;
 			case MU_KIND:
 				type = IntentDocumentProvider.INTENT_MODELINGUNIT;
 				break;
 			default:
 				break;
 		}
 		return new IntentRegion(offset, length, type, kind);
 	}
 
 	/**
 	 * A comparable ITypedRegion.
 	 */
 	class IntentRegion extends TypedRegion implements Comparable<ITypedRegion> {
 		int kind = -1;
 
 		public IntentRegion(int offset, int length, String type) {
 			super(offset, length, type);
 		}
 
 		public IntentRegion(int offset, int length, String type, int kind) {
 			this(offset, length, type);
 			this.kind = kind;
 		}
 
 		public int getKind() {
 			return kind;
 		}
 
 		/**
 		 * Returns true if the current region contains the given one.
 		 * 
 		 * @param o
 		 *            the given region
 		 * @return true if the current region contains the given one
 		 */
 		public boolean contains(ITypedRegion o) {
 			return o.getOffset() >= getOffset()
 					&& (o.getOffset() + o.getLength()) <= (getOffset() + getLength());
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see java.lang.Comparable#compareTo(java.lang.Object)
 		 */
 		public int compareTo(ITypedRegion o) {
 			return new Integer(getOffset()).compareTo(o.getOffset());
 		}
 	}
 }
