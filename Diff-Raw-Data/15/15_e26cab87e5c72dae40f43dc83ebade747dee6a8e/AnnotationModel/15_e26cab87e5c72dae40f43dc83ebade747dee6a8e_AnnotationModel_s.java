 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jface.text.source;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.eclipse.jface.text.Assert;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.ISynchronizable;
 import org.eclipse.jface.text.Position;
 
 
 /**
  * Standard implementation of <code>IAnnotationModel</code>. This class can directly
  * be used by clients. Subclasses may adapt this annotation model to other existing 
  * annotation mechanisms.
  */
 public class AnnotationModel implements IAnnotationModel, IAnnotationModelExtension, ISynchronizable {
 	
 	/**
 	 * Internal annotation model listener for forwarding annotation model changes from the attached models to the
 	 * registered listeners of the outer most annotation model.
 	 * 
 	 * @since 3.0
 	 */
 	private class InternalModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {
 
 		/*
 		 * @see org.eclipse.jface.text.source.IAnnotationModelListener#modelChanged(org.eclipse.jface.text.source.IAnnotationModel)
 		 */
 		public void modelChanged(IAnnotationModel model) {
 			AnnotationModel.this.fireModelChanged(new AnnotationModelEvent(model, true));
 		}
 
 		/*
 		 * @see org.eclipse.jface.text.source.IAnnotationModelListenerExtension#modelChanged(org.eclipse.jface.text.source.AnnotationModelEvent)
 		 */
 		public void modelChanged(AnnotationModelEvent event) {
 			AnnotationModel.this.fireModelChanged(event);
 		}
 	}
 
 	/** 
 	 * The list of managed annotations 
 	 * @deprecated since 3.0 use <code>getAnnotationMap</code> instead
 	 */
 	protected Map fAnnotations;
 	/** The list of annotation model listeners */
 	protected ArrayList fAnnotationModelListeners;
 	/** The document connected with this model */
 	protected IDocument fDocument;
 	/** The number of open connections to the same document */
 	private int fOpenConnections= 0;
 	/** The document listener for tracking whether document positions might have been changed. */
 	private IDocumentListener fDocumentListener;
 	/** The flag indicating whether the document positions might have been changed. */
 	private boolean fDocumentChanged= true;
 	/** 
 	 * The model's attachment.
 	 * @since 3.0
 	 */
 	private Map fAttachments= new HashMap();
 	/**
 	 * The annotation model listener on attached sub-models.
 	 * @since 3.0
 	 */
 	private IAnnotationModelListener fModelListener= new InternalModelListener();
 	/**
 	 * The current annotation model event.
 	 * @since 3.0
 	 */
 	private AnnotationModelEvent fModelEvent;
 	/**
 	 * The modification stamp.
 	 * @since 3.0
 	 */
 	private Object fModificationStamp= new Object();
 	
 	/**
 	 * Creates a new annotation model. The annotation is empty, i.e. does not
 	 * manage any annotations and is not connected to any document.
 	 */
 	public AnnotationModel() {
 		fAnnotations= new AnnotationMap(10);
 		fAnnotationModelListeners= new ArrayList(2);
 		
 		fDocumentListener= new IDocumentListener() {
 			
 			public void documentAboutToBeChanged(DocumentEvent event) {
 			}
 
 			public void documentChanged(DocumentEvent event) {
 				fDocumentChanged= true;
 			}
 		};
 	}
 	
 	/**
 	 * Returns the annotation map internally used by this annotation model.
 	 * 
 	 * @return the annotation map internally used by this annotation model
 	 * @since 3.0
 	 */
 	protected IAnnotationMap getAnnotationMap() {
 		return (IAnnotationMap) fAnnotations;
 	}
 	
     /*
      * @see org.eclipse.jface.text.ISynchronizable#getLockObject()
      * @since 3.0
      */
     public Object getLockObject() {
         return getAnnotationMap().getLockObject();
     }
     
     /*
      * @see org.eclipse.jface.text.ISynchronizable#setLockObject(java.lang.Object)
 	 * @since 3.0
      */
     public void setLockObject(Object lockObject) {
         getAnnotationMap().setLockObject(lockObject);
     }
     
     /**
      * Returns the current annotation model event. This is the event that will be sent out
      * when calling <code>fireModelChanged</code>.
      * 
      * @return the current annotation model event
      * @since 3.0
      */
     protected AnnotationModelEvent getAnnotationModelEvent() {
     	if (fModelEvent == null) {
     		fModelEvent= createAnnotationModelEvent();
     		fModelEvent.markWorldChange(false);
     		fModificationStamp= fModelEvent;
     	}
     	return fModelEvent;
     }
 
 	/*
 	 * @see IAnnotationModel#addAnnotation(Annotation, Position)
 	 */
 	public void addAnnotation(Annotation annotation, Position position) {
 		try {
 			addAnnotation(annotation, position, true);
 		} catch (BadLocationException e) {
 			// ignore invalid position
 		}
 	}
 
 	/*
 	 * @see IAnnotationModelExtension#replaceAnnotations(Annotation[], Map)
 	 * @since 3.0
 	 */
 	public void replaceAnnotations(Annotation[] annotationsToRemove, Map annotationsToAdd) {
 		try {
 			replaceAnnotations(annotationsToRemove, annotationsToAdd, true);
 		} catch (BadLocationException x) {
 		}
 	}
 	
 	/**
 	 * Replaces the given annotations in this model and if advised fires a
 	 * model change event.
 	 * 
 	 * @param annotationsToRemove the annotations to be removed
 	 * @param annotationsToAdd the annotations to be added
 	 * @param fireModelChanged <code>true</code> if a model change event
 	 *            should be fired, <code>false</code> otherwise
 	 * @throws BadLocationException in case an annotation should be added at an
 	 *             invalid position
 	 * @since 3.0
 	 */
 	protected void replaceAnnotations(Annotation[] annotationsToRemove, Map annotationsToAdd, boolean fireModelChanged) throws BadLocationException {
 		
 		if (annotationsToRemove != null) {
 			for (int i= 0, length= annotationsToRemove.length; i < length; i++)
 				removeAnnotation(annotationsToRemove[i], false);
 		}
 		
 		if (annotationsToAdd != null) {
 			Iterator iter= annotationsToAdd.entrySet().iterator();
 			while (iter.hasNext()) {
 				Map.Entry mapEntry= (Map.Entry) iter.next();
 				Annotation annotation= (Annotation) mapEntry.getKey();
 				Position position= (Position) mapEntry.getValue();
 				addAnnotation(annotation, position, false);
 			}
 		}
 		
 		if (fireModelChanged)
 			fireModelChanged();
 	}
 	
 	/**
 	 * Adds the given annotation to this model. Associates the 
 	 * annotation with the given position. If requested, all annotation
 	 * model listeners are informed about this model change. If the annotation
 	 * is already managed by this model nothing happens.
 	 *
 	 * @param annotation the annotation to add
 	 * @param position the associate position
 	 * @param fireModelChanged indicates whether to notify all model listeners
 	 * @throws BadLocationException if the position is not a valid document position
 	 */
 	protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {
 		if (!fAnnotations.containsKey(annotation)) {
 			
 			addPosition(fDocument, position);
 			fAnnotations.put(annotation, position);
 			getAnnotationModelEvent().annotationAdded(annotation);
 
 			if (fireModelChanged)
 				fireModelChanged();
 		}
 	}
 
 	/*
 	 * @see IAnnotationModel#addAnnotationModelListener(IAnnotationModelListener)
 	 */
 	public void addAnnotationModelListener(IAnnotationModelListener listener) {
 		if (!fAnnotationModelListeners.contains(listener)) {
 			fAnnotationModelListeners.add(listener);
 			if (listener instanceof IAnnotationModelListenerExtension) {
 				IAnnotationModelListenerExtension extension= (IAnnotationModelListenerExtension) listener;
 				AnnotationModelEvent event= createAnnotationModelEvent();
 				event.markSealed();
 			    extension.modelChanged(event);
 			} else
 			    listener.modelChanged(this);
 		}
 	}
 
 	/**
 	 * Adds the given position to the default position category of the
 	 * given document.
 	 *
 	 * @param document the document to which to add the position
 	 * @param position the position to add
 	 * @throws BadLocationException if the position is not a valid document position
 	 */
 	protected void addPosition(IDocument document, Position position) throws BadLocationException {
		if (document != null) {
			synchronized (document) {
				document.addPosition(position);
			}
		}
 	}
 	
 	/**
 	 * Removes the given position from the default position category of the
 	 * given document.
 	 *
 	 * @param document the document to which to add the position
 	 * @param position the position to add
 	 * 
 	 * @since 3.0
 	 */
 	protected void removePosition(IDocument document, Position position) {
		if (document != null) {
			synchronized (document) {
				document.removePosition(position);
			}
		}
 	}
 
 	/*
 	 * @see IAnnotationModel#connect(IDocument)
 	 */
 	public void connect(IDocument document) {
 		Assert.isTrue(fDocument == null || fDocument == document);
 		
 		if (fDocument == null) {
 			fDocument= document;
 			Iterator e= getAnnotationMap().valuesIterator();
 			while (e.hasNext())
 				try {
 					addPosition(fDocument, (Position) e.next());
 				} catch (BadLocationException x) {
 					// ignore invalid position
 				}
 		}
 		
 		++ fOpenConnections;
 		if (fOpenConnections == 1) {
 			fDocument.addDocumentListener(fDocumentListener);
 			connected();
 		}
 		
 		for (Iterator it= fAttachments.keySet().iterator(); it.hasNext();) {
 			IAnnotationModel model= (IAnnotationModel) fAttachments.get(it.next());
 			model.connect(document);
 		}
 	}
 	
 	/**
 	 * Hook method. Is called as soon as this model becomes connected to a document.
 	 * Subclasses may re-implement.
 	 */
 	protected void connected() {
 	}
 	
 	/**
 	 * Hook method. Is called as soon as this model becomes disconnected from its document.
 	 * Subclasses may re-implement.
 	 */
 	protected void disconnected() {
 	}
 	
 	/*
 	 * @see IAnnotationModel#disconnect(IDocument)
 	 */
 	public void disconnect(IDocument document) {
 		
 		Assert.isTrue(fDocument == document);
 
 		for (Iterator it= fAttachments.keySet().iterator(); it.hasNext();) {
 			IAnnotationModel model= (IAnnotationModel) fAttachments.get(it.next());
 			model.disconnect(document);
 		}
 		
 		-- fOpenConnections;
 		if (fOpenConnections == 0) {
 			
 			disconnected();
 			fDocument.removeDocumentListener(fDocumentListener);
 		
 			if (fDocument != null) {
 				Iterator e= getAnnotationMap().valuesIterator();
 				while (e.hasNext()) {
 					Position p= (Position) e.next();
 					removePosition(fDocument, p);
 				}
 				fDocument= null;
 			}
 		}
 	}
 	
 	/**
 	 * Informs all annotation model listeners that this model has been changed.
 	 */
 	protected void fireModelChanged() {
 		if (fModelEvent != null) {
 			fireModelChanged(fModelEvent);
 			fModelEvent= null;
 		}
 	}
 	
 	/**
 	 * Creates and returns a new annotation model event. Subclasses may override.
 	 * 
 	 * @return a new and empty annotation model event
 	 * @since 3.0
 	 */
 	protected AnnotationModelEvent createAnnotationModelEvent() {
 		return new AnnotationModelEvent(this);
 	}
 	
 	/**
 	 * Informs all annotation model listeners that this model has been changed
 	 * as described in the annotation model event. The event is sent out
 	 * to all listeners implementing <code>IAnnotationModelListenerExtension</code>.
 	 * All other listeners are notified by just calling <code>modelChanged(IAnnotationModel)</code>.
 	 * 
 	 * @param event the event to be sent out to the listeners
 	 * @since 2.0
 	 */
 	protected void fireModelChanged(AnnotationModelEvent event) {
 		
 		event.markSealed();
 		
 		if (event.isEmpty())
 			return;
 			
 		ArrayList v= new ArrayList(fAnnotationModelListeners);
 		Iterator e= v.iterator();
 		while (e.hasNext()) {
 			IAnnotationModelListener l= (IAnnotationModelListener) e.next();
 			if (l instanceof IAnnotationModelListenerExtension)
 				((IAnnotationModelListenerExtension) l).modelChanged(event);
 			else
 				l.modelChanged(this);
 		}
 	}
 	
 	/**
 	 * Removes the given annotations from this model. If requested all
 	 * annotation model listeners will be informed about this change. 
 	 * <code>modelInitiated</code> indicates whether the deletion has 
 	 * been initiated by this model or by one of its clients.
 	 * 
 	 * @param annotations the annotations to be removed
 	 * @param fireModelChanged indicates whether to notify all model listeners
 	 * @param modelInitiated indicates whether this changes has been initiated by this model
 	 */
 	protected void removeAnnotations(List annotations, boolean fireModelChanged, boolean modelInitiated) {
 		if (annotations.size() > 0) {
 			Iterator e= annotations.iterator();
 			while (e.hasNext())
 				removeAnnotation((Annotation) e.next(), false);
 				
 			if (fireModelChanged)
 				fireModelChanged();
 		}
 	}
 	
 	/**
 	 * Removes all annotations from the model whose associated positions have been
 	 * deleted. If requested inform all model listeners about the change.
 	 *
 	 * @param fireModelChanged indicates whether to notify all model listeners
 	 */
 	protected void cleanup(boolean fireModelChanged) {
 		if (fDocumentChanged) {
 			fDocumentChanged= false;
 			
 			ArrayList deleted= new ArrayList();
 			Iterator e= getAnnotationMap().keySetIterator();
 			while (e.hasNext()) {
 				Annotation a= (Annotation) e.next();
 				Position p= (Position) fAnnotations.get(a);
 				if (p == null || p.isDeleted())
 					deleted.add(a);
 			}
 			
 			removeAnnotations(deleted, fireModelChanged, false);
 		}
 	}
 	
 	/*
 	 * @see IAnnotationModel#getAnnotationsIterator()
 	 */
 	public Iterator getAnnotationIterator() {
 		return getAnnotationIterator(true, true);
 	}
 	
 	/**
 	 * Returns all annotations managed by this model. <code>cleanup</code>
 	 * indicates whether all annotations whose associated positions are 
 	 * deleted should previously be removed from the model. <code>recurse</code> indicates
 	 * whether annotations of attached sub-models should also be returned.
 	 * 
 	 * @param cleanup indicates whether annotations with deleted associated positions are removed
 	 * @param recurse whether to return annotations managed by sub-models.
 	 * @return all annotations managed by this model
 	 * @since 3.0
 	 */
 	private Iterator getAnnotationIterator(boolean cleanup, boolean recurse) {
 		
 		if (!recurse)
 			return getAnnotationIterator(cleanup);
 		
 		List iterators= new ArrayList(fAttachments.size() + 1);
 		iterators.add(getAnnotationIterator(cleanup));
 		for (Iterator it= fAttachments.keySet().iterator(); it.hasNext();) {
 			iterators.add(((IAnnotationModel) fAttachments.get(it.next())).getAnnotationIterator());
 		}
 		
 		final Iterator iter= iterators.iterator();
 		
 		// Meta iterator...
 		return new Iterator() {
 
 			/** The current iterator. */
 			private Iterator fCurrent= (Iterator) iter.next(); // there is at least one.
 			/** The current element. */
 			private Object fCurrentElement;
 			
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 
 			public boolean hasNext() {
 				if (fCurrentElement != null)
 					return true;
 				
 				if (fCurrent.hasNext()) {
 					fCurrentElement= fCurrent.next();
 					return true;
 				} else if (iter.hasNext()) {
 					fCurrent= (Iterator) iter.next();
 					return hasNext();
 				} else
 					return false;
 			}
 
 			public Object next() {
 				if (!hasNext())
 					throw new NoSuchElementException();
 				
 				Object element= fCurrentElement;
 				fCurrentElement= null;
 				return element;
 			}
 			
 		};
 	}
 	
 	/**
 	 * Returns all annotations managed by this model. <code>cleanup</code>
 	 * indicates whether all annotations whose associated positions are 
 	 * deleted should previously be removed from the model.
 	 *
 	 * @param cleanup indicates whether annotations with deleted associated positions are removed
 	 * @return all annotations managed by this model
 	 */
 	protected Iterator getAnnotationIterator(boolean cleanup) {
 		if (cleanup)
 			cleanup(true);
 			
 		return getAnnotationMap().keySetIterator();
 	}
 	
 	/*
 	 * @see IAnnotationModel#getPosition(Annotation)
 	 */
 	public Position getPosition(Annotation annotation) {
 		Position position= (Position) fAnnotations.get(annotation);
 		if (position != null)
 			return position;
 		
 		Iterator it= fAttachments.values().iterator();
 		while (position == null && it.hasNext())
 			position= ((IAnnotationModel) it.next()).getPosition(annotation);	
 		return position;
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#removeAllAnnotations()
 	 * @since 3.0
 	 */
 	public void removeAllAnnotations() {
 		removeAllAnnotations(true);
 	}
 
 	/**
 	 * Removes all annotations from the annotation model. If requested
 	 * inform all model change listeners about this change.
 	 *
 	 * @param fireModelChanged indicates whether to notify all model listeners
 	 */
 	protected void removeAllAnnotations(boolean fireModelChanged) {
 		
 		if (fDocument != null) {
 			Iterator e= getAnnotationMap().keySetIterator();
 			while (e.hasNext()) {
 				Annotation a= (Annotation) e.next();
 				Position p= (Position) fAnnotations.get(a);
 				removePosition(fDocument, p);
 //				p.delete();
 				getAnnotationModelEvent().annotationRemoved(a, p);
 			}
 		}
 		
 		fAnnotations.clear();
 		
 		if (fireModelChanged)
 			fireModelChanged();
 	}
 	
 	/*
 	 * @see IAnnotationModel#removeAnnotation(Annotation)
 	 */
 	public void removeAnnotation(Annotation annotation) {
 		removeAnnotation(annotation, true);
 	}
 		
 	/**
 	 * Removes the given annotation from the annotation model. 
 	 * If requested inform all model change listeners about this change.
 	 *
 	 * @param annotation the annotation to be removed
 	 * @param fireModelChanged indicates whether to notify all model listeners
 	 */
 	protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
 		if (fAnnotations.containsKey(annotation)) {
 			
 			Position p= null;
 			if (fDocument != null) {
 				p= (Position) fAnnotations.get(annotation);
 				removePosition(fDocument, p);
 //				p.delete();
 			}
 				
 			fAnnotations.remove(annotation);
 			getAnnotationModelEvent().annotationRemoved(annotation, p);
 			
 			if (fireModelChanged)
 				fireModelChanged();
 		}
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#modifyAnnotationPosition(org.eclipse.jface.text.source.Annotation, org.eclipse.jface.text.Position)
 	 * @since 3.0
 	 */
 	public void modifyAnnotationPosition(Annotation annotation, Position position) {
 		modifyAnnotationPosition(annotation, position, true);
 	}
 	
 	/**
 	 * Modifies the associated position of the given annotation to the given
 	 * position. If the annotation is not yet managed by this annotation model,
 	 * the annotation is added. When the position is <code>null</code>, the
 	 * annotation is removed from the model.
 	 * <p>
 	 * If requested, all annotation model change listeners will be informed
 	 * about the change.
 	 * 
 	 * @param annotation the annotation whose associated position should be
 	 *            modified
 	 * @param position the position to whose values the associated position
 	 *            should be changed
 	 * @param fireModelChanged indicates whether to notify all model listeners
 	 * @since 3.0
 	 */
 	protected void modifyAnnotationPosition(Annotation annotation, Position position, boolean fireModelChanged) {
 		if (position == null) {
 			removeAnnotation(annotation, fireModelChanged);
 		} else {
 			Position p= (Position) fAnnotations.get(annotation);
 			if (p != null) {
 				
 				if (position.getOffset() != p.getOffset() || position.getLength() != p.getLength()) {
 					p.setOffset(position.getOffset());
 					p.setLength(position.getLength());
 				}
 				getAnnotationModelEvent().annotationChanged(annotation);
 				if (fireModelChanged)
 					fireModelChanged();
 				
 			} else {
 				try {
 					addAnnotation(annotation, position, fireModelChanged);
 				} catch (BadLocationException x) {
 					// ignore invalid position
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Modifies the given annotation if the annotation is managed by this
 	 * annotation model.
 	 * <p>
 	 * If requested, all annotation model change listeners will be informed
 	 * about the change.
 	 * 
 	 * @param annotation the annotation to be modified
 	 * @param fireModelChanged indicates whether to notify all model listeners
 	 * @since 3.0
 	 */
 	protected void modifyAnnotation(Annotation annotation, boolean fireModelChanged) {
 		if (fAnnotations.containsKey(annotation)) {
 			getAnnotationModelEvent().annotationChanged(annotation);
 			if (fireModelChanged)
 				fireModelChanged();
 		}
 	}
 	
 	/*
 	 * @see IAnnotationModel#removeAnnotationModelListener(IAnnotationModelListener)
 	 */
 	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
 		fAnnotationModelListeners.remove(listener);
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#attach(java.lang.Object, java.lang.Object)
 	 * @since 3.0
 	 */
 	public void addAnnotationModel(Object key, IAnnotationModel attachment) {
 		Assert.isNotNull(attachment);
 		if (!fAttachments.containsValue(attachment)) {
 			fAttachments.put(key, attachment);
 			for (int i= 0; i < fOpenConnections; i++)
 				attachment.connect(fDocument);
 			attachment.addAnnotationModelListener(fModelListener);
 		}
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#get(java.lang.Object)
 	 * @since 3.0
 	 */
 	public IAnnotationModel getAnnotationModel(Object key) {
 		return (IAnnotationModel) fAttachments.get(key);
 	}
 
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#detach(java.lang.Object)
 	 * @since 3.0
 	 */
 	public IAnnotationModel removeAnnotationModel(Object key) {
 		IAnnotationModel ret= (IAnnotationModel) fAttachments.remove(key);
 		if (ret != null) {
 			for (int i= 0; i < fOpenConnections; i++)
 				ret.disconnect(fDocument);
 			ret.removeAnnotationModelListener(fModelListener);
 		}
 		return ret;
 	}
 	
 	/*
 	 * @see org.eclipse.jface.text.source.IAnnotationModelExtension#getModificationStamp()
 	 * @since 3.0
 	 */
 	public Object getModificationStamp() {
 		return fModificationStamp;
 	}
 }
