 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor.annotation;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.jface.text.source.AnnotationModel;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 
 /**
  * Handles the management of annotation models used by an IntentDocumentProvider.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentAnnotationModelManager {
 
 	/**
 	 * The handled annotationModel.
 	 */
 	private AnnotationModel annotationModel;
 
 	/**
 	 * The currently handled compilation status list, mapped with its corresponding annotation (use for
 	 * updating the annotations).
 	 */
 	private Map<CompilationStatus, Annotation> handledCompilationStatus;
 
 	/**
 	 * IntentAnnotationModelManager constructor.
 	 */
 	public IntentAnnotationModelManager() {
 		this.annotationModel = new AnnotationModel();
 		this.handledCompilationStatus = new HashMap<CompilationStatus, Annotation>();
 	}
 
 	/**
 	 * Adds the given compilation status as an Annotation in the handled annotationModel at the given
 	 * position.
 	 * 
 	 * @param repositoryAdapter
 	 *            the Repository Adapter to use
 	 * @param status
 	 *            the compilation status to add
 	 * @param position
 	 *            the position of this annotation
 	 */
 	public void addAnnotationFromStatus(RepositoryAdapter repositoryAdapter, CompilationStatus status,
 			Position position) {
 		if (!(handledCompilationStatus.containsKey(status))) {
 			// If the status is a Synchronization Status
 			URI uri = null;
 			if (status instanceof SynchronizerCompilationStatus
 					&& ((SynchronizerCompilationStatus)status).getCompiledResourceURI() != null
 					&& ((SynchronizerCompilationStatus)status).getCompiledResourceURI().length() > 0) {
 				// We use the repository Adapter to get the Resource containing
 				// the target of the synchronization error
 				String compiledResourceURI = ((SynchronizerCompilationStatus)status).getCompiledResourceURI();
 				Resource resource = null;
 				try {
 					resource = repositoryAdapter.getResource(compiledResourceURI);
 					if (resource != null) {
 						uri = resource.getURI();
 					}
 					// CHECKSTYLE:OFF
 				} catch (Exception e) {
 					// CHECKSTYLE:ON
 					// Silent catch
 				}
 			}
 
			// We create an annotation from the status and add it to the annotation model
			Annotation annotation = IntentAnnotationFactory
					.createAnnotationFromCompilationStatus(uri, status);
			addAnnotation(annotation, position);
			handledCompilationStatus.put(status, annotation);

 		}
 	}
 
 	/**
 	 * Adds the given annotation to the handled annotationModel at the given position.
 	 * 
 	 * @param annotation
 	 *            the annotation to add
 	 * @param position
 	 *            the position of this annotation
 	 */
 	private synchronized void addAnnotation(Annotation annotation, Position position) {
 		annotationModel.addAnnotation(annotation, position);
 	}
 
 	/**
 	 * Returns the handled annotationModel.
 	 * 
 	 * @return the handled annotationModel
 	 */
 	public IAnnotationModel getAnnotationModel() {
 		return annotationModel;
 	}
 
 	/**
 	 * Removes all the compiler annotations from the handled annotationModel.
 	 */
 	public synchronized void removeAllCompilerAnnotations() {
 		@SuppressWarnings("unchecked")
 		Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
 
 		while (annotationIterator.hasNext()) {
 			Annotation annotation = annotationIterator.next();
 			if (isCompilerAnnotation(annotation.getType())) {
 				annotationModel.removeAnnotation(annotation);
 			}
 		}
 		handledCompilationStatus.clear();
 	}
 
 	/**
 	 * Removes all the compiler annotations associated to the given element that have changed or have been
 	 * deleted.
 	 * 
 	 * @param adapter
 	 *            the RepositoryAdapter to use for determine if the stored status are still valid
 	 * @param element
 	 *            the element to inspect
 	 */
 	public synchronized void removeInvalidCompilerAnnotations(RepositoryAdapter adapter,
 			IntentGenericElement element) {
 		// For each compilationStatus associated to the given element
 		Iterator<CompilationStatus> compilationStatusIterator = handledCompilationStatus.keySet().iterator();
 		while (compilationStatusIterator.hasNext()) {
 			boolean removeCurrentStatus = false;
 			CompilationStatus currentStatus = compilationStatusIterator.next();
 			Object currentStatusTargetID = adapter.getIDFromElement(currentStatus.getTarget());
 			Object elementID = adapter.getIDFromElement(element);
 			removeCurrentStatus = currentStatusTargetID == null;
 			// If the status is concerning the given element
 			// FIXME find a way to determine those targets
 			if (currentStatusTargetID != null && currentStatusTargetID.equals(elementID)) {
 				if (isCompilerAnnotation(this.handledCompilationStatus.get(currentStatus).getType())) {
 					// If the currentElement doesn't contain this status any more
 					if (!element.getCompilationStatus().contains(currentStatus)) {
 						removeCurrentStatus = true;
 					}
 				}
 			}
 			if (removeCurrentStatus) {
 				annotationModel.removeAnnotation(this.handledCompilationStatus.get(currentStatus));
 				compilationStatusIterator.remove();
 			}
 		}
 	}
 
 	/**
 	 * Creates a syntax error annotation at the given offset, of the given length.
 	 * 
 	 * @param message
 	 *            the message associated to this syntax error
 	 * @param offset
 	 *            offset of the syntax error annotation
 	 * @param length
 	 *            length of the syntax error annotation.
 	 */
 	public void createSyntaxErrorAnnotation(String message, int offset, int length) {
 		IntentAnnotation syntaxErrorAnnotation = IntentAnnotationFactory.createSyntaxErrorAnnotation();
 		syntaxErrorAnnotation.setText(message);
 		Position position = new Position(offset, length);
 		addAnnotation(syntaxErrorAnnotation, position);
 	}
 
 	/**
 	 * Removes all the syntax error annotations from the manage annotation model.
 	 */
 	public synchronized void removeSyntaxErrorsAnnotations() {
 		@SuppressWarnings("unchecked")
 		Iterator<Annotation> annotationIterator = (Iterator<Annotation>)annotationModel
 				.getAnnotationIterator();
 		while (annotationIterator.hasNext()) {
 			Annotation next = annotationIterator.next();
 			if (IntentAnnotationFactory.INTENT_ANNOT_SYNTAX_ERROR.equals(next.getType())) {
 				annotationModel.removeAnnotation(next);
 			}
 		}
 	}
 
 	/**
 	 * Return true if the given annotationType indicates a compileAnnotation, false otherwise.
 	 * 
 	 * @param type
 	 *            the type of an annotation
 	 * @return true if the given annotationType indicates a compileAnnotation, false otherwise
 	 */
 	private boolean isCompilerAnnotation(String type) {
 		return IntentAnnotationFactory.INTENT_ANNOT_COMPILER_ERROR.equals(type)
 				|| IntentAnnotationFactory.INTENT_ANNOT_COMPILER_WARNING.equals(type)
 				|| IntentAnnotationFactory.INTENT_ANNOT_GENERAL_INFO.equals(type)
 				|| IntentAnnotationFactory.INTENT_ANNOT_SYNC_WARNING.equals(type);
 	}
 }
