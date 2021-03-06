 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.ui.internal.texteditor.quickdiff;
 
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 import org.eclipse.jface.util.Assert;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.AnnotationModelEvent;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.IAnnotationModelListener;
 import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
 import org.eclipse.jface.text.source.ILineDiffInfo;
 import org.eclipse.jface.text.source.ILineDiffer;
 
import org.eclipse.ui.progress.IProgressConstants;
 import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;
 
 import org.eclipse.ui.internal.texteditor.TextEditorPlugin;
 import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.DocLineComparator;
 import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.RangeDifference;
 import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.RangeDifferencer;
 import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.LinkedRangeFactory.LowMemoryException;
 
 /**
  * Standard implementation of <code>ILineDiffer</code> as an incremental diff engine. A 
  * <code>DocumentLineDiffer</code> can be initialized to some start state. Once connected to a 
  * <code>IDocument</code> and a reference document has been set, changes reported via the 
  * <code>IDocumentListener</code> interface will be tracked and the incremental diff updated.
  * 
  * <p>The diff state can be queried using the <code>ILineDiffer</code> interface.</p>
  * 
  * <p>Since diff information is model information attached to a document, this class implements 
  * <code>IAnnotationModel</code> and can be attached to <code>IAnnotationModelExtension</code>s.</p>
  * 
  * <p>This class is not supposed to be subclassed.</p>
  * 
  * @since 3.0
  */
 public class DocumentLineDiffer implements ILineDiffer, IDocumentListener, IAnnotationModel {
 
 	/**
 	 * Artificial line difference information indicating a change with an empty line as original text.
 	 */
 	private static class LineChangeInfo implements ILineDiffInfo {
 
 		private static final String[] ORIGINAL_TEXT= new String[] { "\n" }; //$NON-NLS-1$
 
 		/*
 		 * @see org.eclipse.jface.text.source.ILineDiffInfo#getRemovedLinesBelow()
 		 */
 		public int getRemovedLinesBelow() {
 			return 0;
 		}
 
 		/*
 		 * @see org.eclipse.jface.text.source.ILineDiffInfo#getRemovedLinesAbove()
 		 */
 		public int getRemovedLinesAbove() {
 			return 0;
 		}
 
 		/*
 		 * @see org.eclipse.jface.text.source.ILineDiffInfo#getChangeType()
 		 */
 		public int getChangeType() {
 			return CHANGED;
 		}
 
 		/*
 		 * @see org.eclipse.jface.text.source.ILineDiffInfo#hasChanges()
 		 */
 		public boolean hasChanges() {
 			return true;
 		}
 
 		/*
 		 * @see org.eclipse.jface.text.source.ILineDiffInfo#getOriginalText()
 		 */
 		public String[] getOriginalText() {
 			return ORIGINAL_TEXT;
 		}
 	}
 
 	/** Tells whether this class is in debug mode. */
 	private static boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ui.workbench.texteditor/debug/DocumentLineDiffer"));  //$NON-NLS-1$//$NON-NLS-2$
 
 	/** Suspended state */
 	private static final int SUSPENDED= 0;
 	/** Initializing state */
 	private static final int INITIALIZING= 1;
 	/** Synchronized state */
 	private static final int SYNCHRONIZED= 2;
 
 	/** This differ's state */
 	private int fState= SUSPENDED;
 	/** Artificial line difference information indicating a change with an empty line as original text. */
 	private final ILineDiffInfo fLineChangeInfo= new LineChangeInfo();
 	
 	/** The provider for the reference document. */
 	IQuickDiffReferenceProvider fReferenceProvider;
 	/** The number of clients connected to this model. */
 	private int fOpenConnections;
 	/** The current document being tracked. */
 	private IDocument fLeftDocument;
 	/** The reference document. */
 	private IDocument fRightDocument;
 	/** 
 	 * Flag to indicate whether a change has been made to the line table and any clients should 
 	 * update their presentation.
 	 */
 	private boolean fUpdateNeeded;
 	/** The listeners on this annotation model. */
 	private List fAnnotationModelListeners= new ArrayList();
 	/** The job currently initializing the differ, or <code>null</code> if there is none. */
 	private Job fInitializationJob;
 	/** Stores <code>DocumentEvents</code> while an initialization is going on. */
 	private List fStoredEvents= new ArrayList();
 	/**
 	 * The differences between <code>fLeftDocument</code> and <code>fRightDocument</code>. 
 	 * This is the model we work on.
 	 */
 	private List fDifferences= new ArrayList();
 	/**
 	 * The differences removed in one iteration. Stored to be able to send out differentiated
 	 * annotation events. 
 	 */
 	private List fRemoved= new ArrayList();
 	/**
 	 * The differences added in one iteration. Stored to be able to send out differentiated
 	 * annotation events. 
 	 */
 	private List fAdded= new ArrayList();
 	/**
 	 * The differences changed in one iteration. Stored to be able to send out differentiated
 	 * annotation events. 
 	 */
 	private List fChanged= new ArrayList();
 	/** The first line affected by a document event. */
 	private int fFirstLine;
 	/** The number of lines affected by a document event. */
 	private int fNLines;
 	/** The most recent range difference returned in a getLineInfo call, so it can be recyled. */
 	private RangeDifference fLastDifference;
 	/**
 	 * <code>true</code> if incoming document events should be ignored,
 	 * <code>false</code> if not.
 	 */
 	private boolean fIgnoreDocumentEvents= true;
 
 	/**
 	 * Creates a new differ.
 	 */
 	public DocumentLineDiffer() {
 	}
 
 	/* ILineDiffer implementation */
 
 	/*
 	 * @see org.eclipse.jface.text.source.ILineDiffer#getLineInfo(int)
 	 */
 	public ILineDiffInfo getLineInfo(int line) {
 		
 		if (isSuspended())
 			return fLineChangeInfo;
 		
 		// try cache first / speeds up linear search
 		RangeDifference last= fLastDifference;
 		if (last != null && last.rightStart() <= line && last.rightEnd() > line)
 			return new DiffRegion(last, line - last.rightStart(), fDifferences, fLeftDocument);
 		
 		fLastDifference= getRangeDifferenceForRightLine(line);
 		last= fLastDifference;
 		if (last != null)
 			return new DiffRegion(last, line - last.rightStart(), fDifferences, fLeftDocument);
 		else
 			return null;
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.ILineDiffer#revertLine(int)
 	 */
 	public synchronized void revertLine(int line) throws BadLocationException {
 		if (!isInitialized())
 			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$
 		
 		DiffRegion region= (DiffRegion) getLineInfo(line);
 		if (region == null || fRightDocument == null || fLeftDocument == null)
 			return;
 		
 		RangeDifference diff= region.getDifference();
 		int rOffset= fRightDocument.getLineOffset(line);
 		int rLength= fRightDocument.getLineLength(line);
 		int leftLine= diff.leftStart() + region.getOffset();
 		String replacement;
 		if (leftLine >= diff.leftEnd()) // restoring a deleted line?
 			replacement= new String();
 		else {
 			int lOffset= fLeftDocument.getLineOffset(leftLine);
 			int lLength= fLeftDocument.getLineLength(leftLine);
 			replacement= fLeftDocument.get(lOffset, lLength);
 		}
 		fRightDocument.replace(rOffset, rLength, replacement);
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.ILineDiffer#revertBlock(int)
 	 */
 	public synchronized void revertBlock(int line) throws BadLocationException {
 		if (!isInitialized())
 			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$
 		
 		DiffRegion region= (DiffRegion) getLineInfo(line);
 		if (region == null || fRightDocument == null || fLeftDocument == null)
 			return;
 		
 		RangeDifference diff= region.getDifference();
 		int rOffset= fRightDocument.getLineOffset(diff.rightStart());
 		int rLength= fRightDocument.getLineOffset(diff.rightEnd() - 1) + fRightDocument.getLineLength(diff.rightEnd() - 1) - rOffset;
 		int lOffset= fLeftDocument.getLineOffset(diff.leftStart());
 		int lLength= fLeftDocument.getLineOffset(diff.leftEnd() - 1) + fLeftDocument.getLineLength(diff.leftEnd() - 1) - lOffset;
 		fRightDocument.replace(rOffset, rLength, fLeftDocument.get(lOffset, lLength));
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.ILineDiffer#revertSelection(int, int)
 	 */
 	public synchronized void revertSelection(int line, int nLines) throws BadLocationException {
 		if (!isInitialized())
 			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$
 		
 		int rOffset= -1, rLength= -1, lOffset= -1, lLength= -1;
 		RangeDifference diff= null;
 		Iterator it= fDifferences.iterator();
 		
 		// get start
 		while (it.hasNext()) {
 			diff= (RangeDifference) it.next();
 			if (line < diff.rightEnd()) {
 				rOffset= fRightDocument.getLineOffset(line);
 				int leftLine= Math.min(diff.leftStart() + line - diff.rightStart(), diff.leftEnd() - 1);
 				lOffset= fLeftDocument.getLineOffset(leftLine);
 				break;
 			}
 		}
 		
 		if (rOffset == -1 || lOffset == -1)
 			return;
 		
 		// get end / length
 		int to= line + nLines - 1;
 		while (it.hasNext()) {
 			diff= (RangeDifference) it.next();
 			if (to < diff.rightEnd()) {
 				int rEndOffset= fRightDocument.getLineOffset(to) + fRightDocument.getLineLength(to);
 				rLength= rEndOffset - rOffset;
 				int leftLine= Math.min(diff.leftStart() + to - diff.rightStart(), diff.leftEnd() - 1);
 				int lEndOffset= fLeftDocument.getLineOffset(leftLine) + fLeftDocument.getLineLength(leftLine);
 				lLength= lEndOffset - lOffset;
 				break;
 			}
 		}
 		
 		if (rLength == -1 || lLength == -1)
 			return;
 		
 		fRightDocument.replace(rOffset, rLength, fLeftDocument.get(lOffset, lLength));
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.ILineDiffer#restoreAfterLine(int)
 	 */
 	public synchronized int restoreAfterLine(int line) throws BadLocationException {
 		if (!isInitialized())
 			throw new BadLocationException(QuickDiffMessages.getString("quickdiff.nonsynchronized")); //$NON-NLS-1$
 
 		DiffRegion region= (DiffRegion) getLineInfo(line);
 		if (region == null || fRightDocument == null || fLeftDocument == null)
 			return 0;
 		
 		if (region.getRemovedLinesBelow() < 1)
 			return 0;
 
 		RangeDifference diff= null;
 		for (Iterator it= fDifferences.iterator(); it.hasNext();) {
 			diff= (RangeDifference) it.next();
 			if (line >= diff.rightStart() && line < diff.rightEnd()) {
 				if (diff.kind() == RangeDifference.NOCHANGE && it.hasNext())
 					diff= (RangeDifference) it.next();
 				break;
 			}
 		}
 		
 		if (diff == null)
 			return 0;
 
 		int rOffset= fRightDocument.getLineOffset(diff.rightEnd()); 
 		int rLength= 0;
 		int leftLine= diff.leftStart() + diff.rightLength();
 		int lOffset= fLeftDocument.getLineOffset(leftLine);
 		int lLength= fLeftDocument.getLineOffset(diff.leftEnd() - 1) + fLeftDocument.getLineLength(diff.leftEnd() - 1) - lOffset;
 		fRightDocument.replace(rOffset, rLength, fLeftDocument.get(lOffset, lLength));
 
 		return diff.leftLength() - diff.rightLength();
 	}
 
 	/**
 	 * Returns the receivers initialization state.
 	 * 
 	 * @return <code>true</code> if we are initialized and in sync with the document.
 	 */
 	private boolean isInitialized() {
 		return fState == SYNCHRONIZED;
 	}
 	
 	/**
 	 * Returns the receivers synchronization state.
 	 * 
 	 * @return <code>true</code> if we are initialized and in sync with the document.
 	 */
 	public synchronized boolean isSynchronized() {
 		return fState == SYNCHRONIZED;
 	}
 
 	/**
 	 * Returns <code>true</code> if the differ is suspended.
 	 * 
 	 * @return <code>true</code> if the differ is suspended
 	 */
 	private synchronized boolean isSuspended() {
 		return fState == SUSPENDED;
 	}
 
 	/**
 	 * Sets the reference provider for this instance. If one has been installed before, it is 
 	 * disposed.
 	 * 
 	 * @param provider the new provider
 	 */
 	public void setReferenceProvider(IQuickDiffReferenceProvider provider) {
 		Assert.isNotNull(provider);
 		if (provider != fReferenceProvider) {
 			if (fReferenceProvider != null)
 				fReferenceProvider.dispose();
 			fReferenceProvider= provider;
 			initialize();
 		}
 	}
 
 	/**
 	 * Returns the reference provider currently installed, or <code>null</code> if none is installed.
 	 * 
 	 * @return the current reference provider.
 	 */
 	public IQuickDiffReferenceProvider getReferenceProvider() {
 		return fReferenceProvider;
 	}
 
 	/**
 	 * (Re-)initializes the differ using the current reference and <code>DiffInitializer</code>
 	 */
 	synchronized void initialize() {
 		// make new incoming changes go into the queue of stored events, plus signal we can't restore.
 		fState= INITIALIZING;
 		
 		if (fRightDocument == null)
 			return;
 		
 		// there is no point in receiving updates before the job we get a new copy of the document for diffing
 		fIgnoreDocumentEvents= true;
 		
 		if (fLeftDocument != null) {
 			fLeftDocument.removeDocumentListener(this);
 			fLeftDocument= null;
 		}
 		
 		// if there already is a job:
 		// return if it has not started yet, cancel it if already running
 		final Job oldJob= fInitializationJob; 
 		if (oldJob != null) {
 			// don't chain up jobs if there is one waiting already.
 			if (oldJob.getState() == Job.WAITING)
 				return;
 			else
 				oldJob.cancel();
 		}
 		
 		fInitializationJob= new Job(QuickDiffMessages.getString("quickdiff.initialize")) { //$NON-NLS-1$
 
 			/*
 			 * This is run in a different thread. As the documents might be synchronized, never ever
 			 * access the documents in a synchronized section or expect deadlocks. See
 			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44692
 			 */
 			public IStatus run(IProgressMonitor monitor) {
 				
 				// 1:	wait for any previous job that was canceled to avoid job flooding
 				// It will return relatively quickly as RangeDifferencer supports canceling
 				if (oldJob != null)
 					try {
 						oldJob.join();
 					} catch (InterruptedException e) {
 						// will not happen as noone interrupts our thread
 						Assert.isTrue(false);
 					}
 				
 				
 				// 2:	get the reference document 
 				IQuickDiffReferenceProvider provider= fReferenceProvider;
 				final IDocument left;
 				try {
 					left= provider == null ? null : provider.getReference(monitor);
 				} catch (CoreException e) {
 					synchronized (DocumentLineDiffer.this) {
 						if (isCanceled(monitor))
 							return Status.CANCEL_STATUS;
 						else {
 							clearModel();
 							fireModelChanged();
 							DocumentLineDiffer.this.notifyAll();
 							return e.getStatus();
 						}
 					}
 				} catch (OperationCanceledException e) {
 					return Status.CANCEL_STATUS;
 				}
 
 				// Getting our own copies of the documents for offline diffing.
 				//
 				// We need to make sure that we do get all document modifications after
 				// copying the documents as we want to reinject them later on to become consistent.
 				//
 				// Now this is fun. The reference documents may be PartiallySynchronizedDocuments
 				// which will result in a deadlock if they get changed externally before we get
 				// our exclusive copies.
 				// Here's what we do: we try over and over (without synchronization) to get copies 
 				// without interleaving modification. If there is a document change, we just repeat. 
 				
 				IDocument right= fRightDocument; // fRightDocument, but not subject to change
 				IDocument actual= null; // the copy of the actual (right) document
 				IDocument reference= null; // the copy of the reference (left) document
 				
 				synchronized (DocumentLineDiffer.this) {
 					// 4: take an early exit if the documents are not valid
 					if (left == null || right == null) {
 						if (isCanceled(monitor))
 							return Status.CANCEL_STATUS;
 						else {
 							clearModel();
 							fireModelChanged();
 							DocumentLineDiffer.this.notifyAll();
 							return Status.OK_STATUS;
 						}
 					}
 					
 					// set the reference document
 					fLeftDocument= left;
 					// start listening to document events.
 					fIgnoreDocumentEvents= false;
 				}
 
 				// accessing the reference docuent offline - reference provider need
 				// to be able to deal with this.
 				left.addDocumentListener(DocumentLineDiffer.this);
 				
 				int i= 0;
 				do {
 					if (i++ == 100) // XXX this is an arbitrary emergency exit in case a referenced document goes nuts
 						return new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, IStatus.OK, QuickDiffMessages.getFormattedString("quickdiff.error.getting_document_content", new Object[] {left.getClass(), right.getClass()}), null); //$NON-NLS-1$
 					
 					// clear events
 					synchronized (DocumentLineDiffer.this) {
 						if (isCanceled(monitor))
 							return Status.CANCEL_STATUS;
 
 						fStoredEvents.clear();
 					}
 					
 					// access documents unsynched:
 					// get an exclusive copy of the actual document
 					reference= createCopy(left);
 					actual= createCopy(right);
 					
 					synchronized (DocumentLineDiffer.this) {
 						if (fStoredEvents.size() == 0 && reference != null && actual != null)
 							break;
 					}
 					
 				} while (true);
 				
 				// 6:	Do Da Diffing
 				DocLineComparator ref= new DocLineComparator(reference, null, false);
 				DocLineComparator act= new DocLineComparator(actual, null, false);
 				List diffs;
 				diffs= RangeDifferencer.findRanges(monitor, ref, act);
 				
 				// 7:	Reset the model to the just gotten differences
 				// 		re-inject stored events to get up to date.
 				synchronized (DocumentLineDiffer.this) {
 					if (isCanceled(monitor))
 						return Status.CANCEL_STATUS;
 				
 					// set the new differences so we can operate on them
 					fDifferences= diffs;
 				}
 
 				// reinject events accumulated in the meantime.
 				try {
 					do {
 						DocumentEvent event;
 						synchronized (DocumentLineDiffer.this) {
 							if (isCanceled(monitor))
 								return Status.CANCEL_STATUS;
 						
 							if (fStoredEvents.isEmpty()) {
 								// we are done
 								fInitializationJob= null;
 								fState= SYNCHRONIZED;
 								fLastDifference= null;
 								
 								// inform blocking calls.
 								DocumentLineDiffer.this.notifyAll();
 								
 								break;
 							} else {
 								event= (DocumentEvent) fStoredEvents.remove(0);
 							}
 						}
 						
 						// access documents unsynched:
 						handleAboutToBeChanged(event);
 						handleChanged(event);
 
 					} while (true);
 					
 				} catch (BadLocationException e) {
 					left.removeDocumentListener(DocumentLineDiffer.this);
 					clearModel();
 					initialize();
 					return Status.CANCEL_STATUS;
 				} catch (LowMemoryException e) {
 					handleLowMemory(e);
 					return Status.CANCEL_STATUS;
 				}
 				
 				fireModelChanged();
 				return Status.OK_STATUS;
 			}
 
 			private boolean isCanceled(IProgressMonitor monitor) {
 				return fInitializationJob != this || monitor != null && monitor.isCanceled();
 			}
 
 			private void clearModel() {
 				fLeftDocument= null;
 				fInitializationJob= null;
 				fStoredEvents.clear();
 				fLastDifference= null;
 				fDifferences.clear();
 			}
 			
 			/**
 			 * Creates a copy of <code>document</code> and catches any
 			 * exceptions that may occur if the document is not modified concurrently.
 			 * Do not call this method in a synchronized block as document.get() is called
 			 * and may result in a deadlock otherwise.
 			 * 
 			 * @param document the document to create a copy of
 			 * @return a copy of the document, or <code>null</code> if an exception was thrown
 			 */
 			private IDocument createCopy(IDocument document) {
 				Assert.isNotNull(document);
 				// TODO needs for sure a safer synchronization method
 				// this is a temporary workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=56091
 				try {
 					return new Document(document.get());
 				} catch (NullPointerException e) {
 				} catch (ArrayStoreException e) {
 				} catch (IndexOutOfBoundsException e) {
 				} catch (ConcurrentModificationException e) {
 				}
 				return null;
 			}
 		};
 		
 		fInitializationJob.setSystem(true);
 		fInitializationJob.setPriority(Job.DECORATE);
		fInitializationJob.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
 		fInitializationJob.schedule();
 	}
 
 	/* IDocumentListener implementation */
 
 	/*
 	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
 	 */
 	public synchronized void documentAboutToBeChanged(DocumentEvent event) {
 		if (fIgnoreDocumentEvents)
 			return;
 		
 		// if a initialization is going on, we just store the events in the meantime
 		if (!isInitialized() && fInitializationJob != null) {
 			fStoredEvents.add(event);
 			return;
 		}
 
 		try {
 			handleAboutToBeChanged(event);
 		} catch (BadLocationException e) {
 			reinitOnError(e);
 			return;
 		} catch (NullPointerException e) {
 			reinitOnError(e);
 			return;
 		} catch (ArrayStoreException e) {
 			reinitOnError(e);
 			return;
 		} catch (IndexOutOfBoundsException e) {
 			reinitOnError(e);
 			return;
 		} catch (ConcurrentModificationException e) {
 			reinitOnError(e);
 			return;
 		}
 	}
 
 	
 	/**
 	 * Unsynchronized version of <code>documentAboutToBeChanged</code>, called by <code>documentAboutToBeChanged</code>
 	 * and {@link #initialize()}. 
 	 * 
 	 * @param event the document event to be handled
 	 */
 	void handleAboutToBeChanged(DocumentEvent event) throws BadLocationException {
 		IDocument doc= event.getDocument();
 		if (doc == null)
 			return;
 
 		// store size of replaced region (never synchronized -> not a problem)
 		fFirstLine= doc.getLineOfOffset(event.getOffset()); // store change bounding lines
 		fNLines= doc.getLineOfOffset(event.getOffset() + event.getLength()) - fFirstLine + 1;
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
 	 */
 	public synchronized void documentChanged(DocumentEvent event) {
 		if (fIgnoreDocumentEvents)
 			return;
 		
 		if (!isInitialized())
 			return;
 		
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=44692
 		// don't allow incremental update for changes from the reference document
 		// as this could deadlock
 		if (event.getDocument() == fLeftDocument) {
 			initialize();
 			return;
 		}
 		
 		try {
 			handleChanged(event);
 		} catch (BadLocationException e) {
 			reinitOnError(e);
 			return;
 		} catch (NullPointerException e) {
 			reinitOnError(e);
 			return;
 		} catch (ArrayStoreException e) {
 			reinitOnError(e);
 			return;
 		} catch (IndexOutOfBoundsException e) {
 			reinitOnError(e);
 			return;
 		} catch (ConcurrentModificationException e) {
 			reinitOnError(e);
 			return;
 		} catch (LowMemoryException e) {
 			handleLowMemory(e);
 			return;
 		}
 		
 		// inform listeners about change
 		if (fUpdateNeeded) {
 			AnnotationModelEvent ame= new AnnotationModelEvent(this, false);
 			for (Iterator it= fAdded.iterator(); it.hasNext(); ) {
 				RangeDifference rd= (RangeDifference) it.next();
 				ame.annotationAdded(rd.getDiffRegion(fDifferences, fLeftDocument));
 			}
 			for (Iterator it= fRemoved.iterator(); it.hasNext(); ) {
 				RangeDifference rd= (RangeDifference) it.next();
 				ame.annotationRemoved(rd.getDiffRegion(fDifferences, fLeftDocument));
 			}
 			for (Iterator it= fChanged.iterator(); it.hasNext(); ) {
 				RangeDifference rd= (RangeDifference) it.next();
 				ame.annotationChanged(rd.getDiffRegion(fDifferences, fLeftDocument));
 			}
 			fireModelChanged(ame);
 			fUpdateNeeded= false;
 		}
 	}
 
 	/**
 	 * Re-initializes the differ if an exception is thrown upon accessing the documents. This can 
 	 * happen if the documents get concurrently modified from a background thread.
 	 * 
 	 * @param e the exception thrown, which is logged in debug mode
 	 */
 	private void reinitOnError(Exception e) {
 		if (DEBUG)
 			System.err.println("reinitializing quickdiff:\n" + e.getLocalizedMessage() + "\n" + e.getStackTrace());  //$NON-NLS-1$//$NON-NLS-2$
 		initialize();
 	}
 
 	/**
 	 * Implementation of documentChanged, non synchronized.
 	 * 
 	 * @param event the document event
 	 * @throws LowMemoryException if the differ runs out of memory
 	 */
 	void handleChanged(DocumentEvent event) throws BadLocationException, LowMemoryException {
 		/*
 		 * Now, here we have a great example of object oriented programming.
 		 */
 		
 		// documents: left, right; modified and unchanged are either of both
 		IDocument left= fLeftDocument;
 		IDocument right= fRightDocument;
 		IDocument modified= event.getDocument();
 		if (modified != left && modified != right)
 			Assert.isTrue(false);
 
 		String insertion= event.getText();
 		int added= insertion == null ? 1 : modified.computeNumberOfLines(insertion) + 1;
 		// size: the size of the document change in lines
 		
 		// put an upper bound to the delay we can afford
 		if (added > 50 || fNLines > 50) {
 			initialize();
 			return;
 		}
 
 		int size= Math.max(fNLines, added) + 1;
 		int lineDelta= added - fNLines;
 		int lastLine= fFirstLine + fNLines - 1;
 		
 		int repetitionField;
 		if (modified == left) {
 			int originalLine= getRightLine(lastLine + 1);
 			repetitionField= searchForRepetitionField(size - 1, right, originalLine);
 		} else {
 			int originalLine= getLeftLine(lastLine + 1);
 			repetitionField= searchForRepetitionField(size - 1, left, originalLine);
 		}
 		lastLine += repetitionField;
 		
 
 		// get enclosing range: search for a consistent block of at least the size of our
 		// change before and after the change. 
 		RangeDifference consistentBefore, consistentAfter;
 		if (modified == left) {
 			consistentBefore= findConsistentRangeBeforeLeft(fFirstLine, size);
 			consistentAfter= findConsistentRangeAfterLeft(lastLine, size);
 		} else {
 			consistentBefore= findConsistentRangeBeforeRight(fFirstLine, size);
 			consistentAfter= findConsistentRangeAfterRight(lastLine, size);
 		}
 		
 		// optimize unchanged blocks: if the consistent blocks around the change are larger than
 		// size, we redimension them (especially important when there are only very little changes.
 		int shiftBefore= 0;
 		if (consistentBefore.kind() == RangeDifference.NOCHANGE) {
 			int unchanged;
 			if (modified == left)
 				unchanged= Math.min(fFirstLine, consistentBefore.leftEnd()) - consistentBefore.leftStart();
 			else
 				unchanged=  Math.min(fFirstLine, consistentBefore.rightEnd()) - consistentBefore.rightStart();
 			
 			shiftBefore= Math.max(0, unchanged - size);
 		}
 
 		int shiftAfter= 0;
 		if (consistentAfter.kind() == RangeDifference.NOCHANGE) {
 			int unchanged;
 			if (modified == left)
 				unchanged= consistentAfter.leftEnd() - Math.max(lastLine + 1, consistentAfter.leftStart());
 			else
 				unchanged= consistentAfter.rightEnd() - Math.max(lastLine + 1, consistentAfter.rightStart());
 				
 			shiftAfter= Math.max(0, unchanged - size);
 		}
 
 		// get the document regions that will be rediffed, take into account that on the 
 		// document, the change has already happened.
 		// left (reference) document
 		int leftOffset= left.getLineOffset(consistentBefore.leftStart() + shiftBefore);
 		int leftLine= Math.max(consistentAfter.leftEnd() - 1, 0);
 		if (modified == left)
 			leftLine += lineDelta;
 		IRegion leftLastLine= left.getLineInformation(leftLine - shiftAfter);
 		int leftEndOffset= leftLastLine.getOffset() + leftLastLine.getLength();
 		IRegion leftRegion= new Region(leftOffset, leftEndOffset - leftOffset);
 		DocLineComparator reference= new DocLineComparator(left, leftRegion, false);
 
 		// right (actual) document
 		int rightOffset= right.getLineOffset(consistentBefore.rightStart() + shiftBefore);
 		int rightLine= Math.max(consistentAfter.rightEnd() - 1, 0);
 		if (modified == right)
 			rightLine += lineDelta;
 		IRegion rightLastLine= right.getLineInformation(rightLine - shiftAfter);
 		int rightEndOffset= rightLastLine.getOffset() + rightLastLine.getLength();
 		IRegion rightRegion= new Region(rightOffset, rightEndOffset - rightOffset);
 		DocLineComparator change= new DocLineComparator(right, rightRegion, false);
 
 		// put an upper bound to the delay we can afford
 		if (leftLine - shiftAfter - (consistentBefore.leftStart() + shiftBefore) > 50 || rightLine - shiftAfter - (consistentBefore.rightStart() + shiftBefore) > 50) {
 			initialize();
 			return;
 		}
 
 		// debug
 //			System.out.println("compare window: "+size+"\n\n<" + left.get(leftRegion.getOffset(), leftRegion.getLength()) +  //$NON-NLS-1$//$NON-NLS-2$
 //					">\n\n<" + right.get(rightRegion.getOffset(), rightRegion.getLength()) + ">\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		// compare
 		List diffs= RangeDifferencer.findRanges(reference, change);
 		if (diffs.size() == 0) {
 			diffs.add(new RangeDifference(RangeDifference.CHANGE, 0, 0, 0, 0));
 		}
 
 		
 		// shift the partial diffs to the absolute document positions
 		int leftShift= consistentBefore.leftStart() + shiftBefore;
 		int rightShift= consistentBefore.rightStart() + shiftBefore;
 		for (Iterator it= diffs.iterator(); it.hasNext();) {
 			RangeDifference d= (RangeDifference) it.next();
 			d.shiftLeft(leftShift);
 			d.shiftRight(rightShift);
 		}
 		
 		// undo optimization shifting
 		if (shiftBefore > 0) {
 			RangeDifference first= (RangeDifference) diffs.get(0);
 			if (first.kind() == RangeDifference.NOCHANGE)
 				first.extendStart(-shiftBefore);
 			else
 				diffs.add(0, new RangeDifference(RangeDifference.NOCHANGE, first.rightStart() - shiftBefore, shiftBefore, first.leftStart() - shiftBefore, shiftBefore));
 		}
 		
 		RangeDifference last= (RangeDifference) diffs.get(diffs.size() - 1);
 		if (shiftAfter > 0) {
 			if (last.kind() == RangeDifference.NOCHANGE)
 				last.extendEnd(shiftAfter);
 			else
 				diffs.add(new RangeDifference(RangeDifference.NOCHANGE, last.rightEnd(), shiftAfter , last.leftEnd(), shiftAfter));
 		}
 
 		// replace changed diff range
 		ListIterator it= fDifferences.listIterator();
 		Iterator newIt= diffs.iterator();
 		RangeDifference current;
 		boolean changed= false;
 
 		// replace regions from consistentBefore to consistentAfter with new diffs
 
 		// search for consistentBefore
 		do {
 			Assert.isTrue(it.hasNext());
 			current= (RangeDifference) it.next();
 		} while (current != consistentBefore);
 		Assert.isTrue(current == consistentBefore);
 
 		fChanged.clear();
 		fRemoved.clear();
 		fAdded.clear();
 		
 		// replace until consistentAfter
 		while (current != consistentAfter) {
 			if (newIt.hasNext()) {
 				Object o= newIt.next();
 				if (!current.equals(o)) {
 					fRemoved.add(current);
 					fAdded.add(o);
 					changed= true;
 					it.set(o);
 				}
 			} else {
 				fRemoved.add(current);
 				it.remove();
 				fUpdateNeeded= true;
 			}
 			Assert.isTrue(it.hasNext());
 			current= (RangeDifference) it.next();
 		}
 		
 		// replace consistentAfter
 		Assert.isTrue(current == consistentAfter);
 		if (newIt.hasNext()) {
 			Object o= newIt.next();
 			if (!current.equals(o)) {
 				fRemoved.add(current);
 				fAdded.add(o);
 				changed= true;
 				it.set(o);
 			}
 		} else {
 			fRemoved.add(current);
 			it.remove();
 			fUpdateNeeded= true;
 		}
 
 		// add remaining new diffs
 		while (newIt.hasNext()) {
 			Object next= newIt.next();
 			fAdded.add(next);
 			it.add(next);
 			changed= true;
 		}
 
 		// shift the old remaining diffs
 		boolean init= true;
 		while (it.hasNext()) {
 			current= (RangeDifference) it.next();
 			if (init) {
 				init= false;
 				leftShift= last.leftEnd() - current.leftStart();
 				rightShift= last.rightEnd() - current.rightStart();
 				if (leftShift != 0 || rightShift != 0)
 					changed= true;
 				else
 					break;
 			}
 //			fChanged.add(current); // not needed since positional shifting is not handled by an annotation model
 			current.shiftLeft(leftShift);
 			current.shiftRight(rightShift);
 		}
 
 		fUpdateNeeded= changed;
 		fLastDifference= null;
 			
 	}
 
 	/**
 	 * Finds a consistent range of at least size before <code>line</code> in the left document.
 	 * 
 	 * @param line the line before which the range has to occur
 	 * @param size the minimal size of the range 
 	 * @return the first range found, or the first range in the differ if none can be found
 	 */
 	private RangeDifference findConsistentRangeBeforeLeft(int line, int size) {
 		RangeDifference found= null;
 		
 		for (ListIterator it= fDifferences.listIterator(); it.hasNext();) {
 			RangeDifference difference= (RangeDifference) it.next();
 			if (found == null || difference.kind() == RangeDifference.NOCHANGE 
 					&& (difference.leftEnd() < line && difference.leftLength() >= size
 							|| difference.leftEnd() >= line && line - difference.leftStart() >= size))
 				found= difference;
 			
 			if (difference.leftEnd() >= line)
 				break;
 		}
 		
 		return found;
 	}
 
 	/**
 	 * Finds a consistent range of at least size after <code>line</code> in the left document.
 	 * 
 	 * @param line the line after which the range has to occur
 	 * @param size the minimal size of the range 
 	 * @return the first range found, or the last range in the differ if none can be found
 	 */
 	private RangeDifference findConsistentRangeAfterLeft(int line, int size) {
 		RangeDifference found= null;
 		
 		for (ListIterator it= fDifferences.listIterator(fDifferences.size()); it.hasPrevious();) {
 			RangeDifference difference= (RangeDifference) it.previous();
 			if (found == null || difference.kind() == RangeDifference.NOCHANGE 
 					&& (difference.leftStart() > line && difference.leftLength() >= size
 							|| difference.leftStart() <= line && difference.leftEnd() - line >= size))
 				found= difference;
 			
 			if (difference.leftStart() <= line)
 				break;
 			
 		}
 		
 		return found;
 	}
 
 	/**
 	 * Finds a consistent range of at least size before <code>line</code> in the right document.
 	 * 
 	 * @param line the line before which the range has to occur
 	 * @param size the minimal size of the range 
 	 * @return the first range found, or the first range in the differ if none can be found
 	 */
 	private RangeDifference findConsistentRangeBeforeRight(int line, int size) {
 		RangeDifference found= null;
 		
 		int unchanged= -1; // the number of unchanged lines before line
 		for (ListIterator it= fDifferences.listIterator(); it.hasNext();) {
 			RangeDifference difference= (RangeDifference) it.next();
 			if (found == null)
 				found= difference;
 			else if (difference.kind() == RangeDifference.NOCHANGE) {
 				unchanged= Math.min(line, difference.rightEnd()) - difference.rightStart();
 				if (unchanged >= size)
 					found= difference;
 			}
 			
 			if (difference.rightEnd() >= line)
 				break;
 		}
 		
 		return found;
 	}
 
 	/**
 	 * Finds a consistent range of at least size after <code>line</code> in the right document.
 	 * 
 	 * @param line the line after which the range has to occur
 	 * @param size the minimal size of the range 
 	 * @return the first range found, or the last range in the differ if none can be found
 	 */
 	private RangeDifference findConsistentRangeAfterRight(int line, int size) {
 		RangeDifference found= null;
 		
 		int unchanged= -1; // the number of unchanged lines after line
 		for (ListIterator it= fDifferences.listIterator(fDifferences.size()); it.hasPrevious();) {
 			RangeDifference difference= (RangeDifference) it.previous();
 			if (found == null)
 				found= difference;
 			else if (difference.kind() == RangeDifference.NOCHANGE) {
 				unchanged= difference.rightEnd() - Math.max(line + 1, difference.rightStart()); // + 1 to step over the changed line
 				if (unchanged >= size)
 					found= difference;
 			}
 
 			if (difference.rightStart() <= line)
 				break;
 		}
 		
 		return found;
 	}
 
 	/**
 	 * Returns the size of a repetition field starting a <code>line</code>.
 	 * 
 	 * @param size the maximal length of the repeat window
 	 * @param doc the document to search
 	 * @param line the line to start searching
 	 * @return the size of a found repetition field, or zero
 	 * @throws BadLocationException if <code>doc</code> is modified concurrently
 	 */
 	private int searchForRepetitionField(int size, IDocument doc, int line) throws BadLocationException {
 		/* 
 		 Repetition fields: a line wise repetition of maximal size <code>size</code>
 		 can urge a change to come at its end, as diffing greedily takes the longest
 		 unchanged range possible:
 		 <pre>
 		 before
 		 repeat
 		 repeat
 		 repeat
 		 repeat
 		 repeat
 		 repeat
 		 repeat
 		 repeat
 		 after
 		 </pre>
 		
 		 Inserting another repeat element anywhere in the repetition field will create
 		 an addition at its end.
 		
 		 Size is one less than our window size (as this is already one more than the actual number
 		 of affected lines.
 		 */
 		
 		/*
 		 * Implementation:
 		 * Window of maximum repetition size. Whenever the current matches the first in the window,
 		 * we advance it by one. If there are more free slots in the window, the current line is
 		 * appended.
 		 * We terminate if the current line does not match and there are no more free slots.
 		 * 
 		 * TODO what if we have a prefix to a repetition field? Probably does not matter.
 		 */
 		LinkedList window= new LinkedList();
 		int nLines= doc.getNumberOfLines();
 		int repetition= line - 1;
 		int l= line;
 		
 		while (l >= 0 && l < nLines) {
 			IRegion r= doc.getLineInformation(l);
 			String current= doc.get(r.getOffset(), r.getLength());
 			
 			if (!window.isEmpty() && window.get(0).equals(current)) {
 				// repetition found, shift
 				window.removeFirst();
 				window.addLast(current);
 				repetition= l;
 			} else {
 				// no repetition, add if there is room
 				// otherwise return
 				if (window.size() < size)
 					window.addLast(current);
 				else
 					break; 
 			}
 			
 			l++;
 		}
 		
 		int fieldLength= repetition - line + 1;
 		Assert.isTrue(fieldLength >= 0);
 		return fieldLength;
 	}
 
 	/**
 	 * Gets the corresponding line on the left side for a line on the right.
 	 * 
 	 * @param rightLine the line on the right side
 	 * @return the corresponding left hand line, or <code>-1</code>
 	 */
 	private int getLeftLine(int rightLine) {
 		RangeDifference d= getRangeDifferenceForRightLine(rightLine);
 		if (d == null)
 			return -1;
 		return Math.min(d.leftEnd() - 1, d.leftStart() + rightLine - d.rightStart());
 	}
 
 	/**
 	 * Gets the corresponding line on the right side for a line on the left.
 	 * 
 	 * @param leftLine the line on the left side
 	 * @return the corresponding right hand line, or <code>-1</code>
 	 */
 	private int getRightLine(int leftLine) {
 		RangeDifference d= getRangeDifferenceForLeftLine(leftLine);
 		if (d == null)
 			return -1;
 		return Math.min(d.rightEnd() - 1, d.rightStart() + leftLine - d.leftStart());
 	}
 	
 	/**
 	 * Gets the RangeDifference for a line on the left hand side.
 	 * 
 	 * @param leftLine the line on the left side
 	 * @return the corresponding RangeDifference, or <code>null</code>
 	 */
 	private RangeDifference getRangeDifferenceForLeftLine(int leftLine) {
 		for (Iterator it= fDifferences.iterator(); it.hasNext();) {
 			RangeDifference d= (RangeDifference) it.next();
 			if (leftLine >= d.leftStart() && leftLine < d.leftEnd()) {
 				return d;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the RangeDifference for a line on the right hand side.
 	 * 
 	 * @param rightLine the line on the right side
 	 * @return the corresponding RangeDifference, or <code>null</code>
 	 */
 	private RangeDifference getRangeDifferenceForRightLine(int rightLine) {
 		for (Iterator it= fDifferences.iterator(); it.hasNext();) {
 			RangeDifference d= (RangeDifference) it.next();
 			if (rightLine >= d.rightStart() && rightLine < d.rightEnd()) {
 				return d;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
 	 */
 	public void addAnnotationModelListener(IAnnotationModelListener listener) {
 		fAnnotationModelListeners.add(listener);
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotationModelListener(org.eclipse.jface.text.source.IAnnotationModelListener)
 	 */
 	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
 		fAnnotationModelListeners.remove(listener);
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#connect(org.eclipse.jface.text.IDocument)
 	 */
 	public void connect(IDocument document) {
 		Assert.isTrue(fRightDocument == null || fRightDocument == document);
 		
 		++fOpenConnections;
 		if (fOpenConnections == 1) {
 			fRightDocument= document;
 			fRightDocument.addDocumentListener(this);
 			initialize();
 		}
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#disconnect(org.eclipse.jface.text.IDocument)
 	 */
 	public void disconnect(IDocument document) {
 		Assert.isTrue(fRightDocument == document);
 		
 		--fOpenConnections;
 		
 		if (fOpenConnections == 0)
 			uninstall();
 	}
 	
 	/**
 	 * Uninstalls all components and dereferences any objects.
 	 */
 	private void uninstall() {
 		synchronized (this) {
 			fState= SUSPENDED;
 			fIgnoreDocumentEvents= true;
 			if (fInitializationJob != null)
 				fInitializationJob.cancel();
 			fInitializationJob= null;
 			
 			if (fLeftDocument != null)
 				fLeftDocument.removeDocumentListener(this);
 			fLeftDocument= null;
 			
 			if (fRightDocument != null)
 				fRightDocument.removeDocumentListener(this);
 			fRightDocument= null;
 		}
 
 		if (fReferenceProvider != null) {
 			fReferenceProvider.dispose();
 			fReferenceProvider= null;
 		}
 		
 		fDifferences.clear();
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#addAnnotation(org.eclipse.jface.text.source.Annotation, org.eclipse.jface.text.Position)
 	 */
 	public void addAnnotation(Annotation annotation, Position position) {
 		throw new UnsupportedOperationException();
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#removeAnnotation(org.eclipse.jface.text.source.Annotation)
 	 */
 	public void removeAnnotation(Annotation annotation) {
 		throw new UnsupportedOperationException();
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#getAnnotationIterator()
 	 */
 	public Iterator getAnnotationIterator() {
 		final List copy= new ArrayList(fDifferences);
 		final Iterator iter= copy.iterator();
 		return new Iterator() {
 
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 
 			public boolean hasNext() {
 				return iter.hasNext();
 			}
 
 			public Object next() {
 				RangeDifference diff= (RangeDifference) iter.next();
 				return diff.getDiffRegion(copy, fLeftDocument);
 			}
 			
 		};
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModel#getPosition(org.eclipse.jface.text.source.Annotation)
 	 */
 	public Position getPosition(Annotation annotation) {
 		if (fRightDocument != null && annotation instanceof DiffRegion) {
 			RangeDifference difference= ((DiffRegion)annotation).getDifference();
 			try {
 				int offset= fRightDocument.getLineOffset(difference.rightStart());
 				return new Position(offset, fRightDocument.getLineOffset(difference.rightEnd() - 1) + fRightDocument.getLineLength(difference.rightEnd() - 1) - offset);
 			} catch (BadLocationException e) {
 				// ignore and return null;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Informs all annotation model listeners that this model has been changed.
 	 */
 	protected void fireModelChanged() {
 		fireModelChanged(new AnnotationModelEvent(this));
 	}
 
 	/**
 	 * Informs all annotation model listeners that this model has been changed
 	 * as described in the annotation model event. The event is sent out
 	 * to all listeners implementing <code>IAnnotationModelListenerExtension</code>.
 	 * All other listeners are notified by just calling <code>modelChanged(IAnnotationModel)</code>.
 	 * 
 	 * @param event the event to be sent out to the listeners
 	 */
 	protected void fireModelChanged(AnnotationModelEvent event) {
 		ArrayList v= new ArrayList(fAnnotationModelListeners);
 		Iterator e= v.iterator();
 		while (e.hasNext()) {
 			IAnnotationModelListener l= (IAnnotationModelListener)e.next();
 			if (l instanceof IAnnotationModelListenerExtension)
 				 ((IAnnotationModelListenerExtension)l).modelChanged(event);
 			else
 				l.modelChanged(this);
 		}
 	}
 	
 	/**
 	 * Stops diffing of this differ. All differences are cleared.
 	 */
 	public synchronized void suspend() {
 		if (fInitializationJob != null) {
 			fInitializationJob.cancel();
 			fInitializationJob= null;
 		}
 		if (fRightDocument != null)
 			fRightDocument.removeDocumentListener(this);
 		if (fLeftDocument != null)
 			fLeftDocument.removeDocumentListener(this);
 		fLeftDocument= null;
 		
 		fLastDifference= null;
 		fStoredEvents.clear();
 		fDifferences.clear();
 		
 		fState= SUSPENDED;
 		
 		fireModelChanged();
 	}
 	
 	/**
 	 * Resumes diffing of this differ. Must only be called after suspend.
 	 */
 	public synchronized void resume() {
 		if (fRightDocument != null)
 			fRightDocument.addDocumentListener(this);
 		initialize();
 	}
 
 	/**
 	 * Handle low memory situation during diffing. Called from UI and jobs.
 	 * 
 	 * @param e the low memory exception
 	 */
 	private void handleLowMemory(LowMemoryException e) {
 		if (DEBUG)
 			System.err.println("Disabling QuickDiff:\n" + e);  //$NON-NLS-1$
 		suspend();
 	}
 }
