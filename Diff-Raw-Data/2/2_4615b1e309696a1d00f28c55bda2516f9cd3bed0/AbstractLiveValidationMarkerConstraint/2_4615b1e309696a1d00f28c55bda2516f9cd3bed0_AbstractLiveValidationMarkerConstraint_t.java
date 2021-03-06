 /**
  * Copyright (C) 2012 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.studio.validation.constraints;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.bonitasoft.studio.common.emf.tools.ModelHelper;
 import org.bonitasoft.studio.common.log.BonitaStudioLog;
 import org.bonitasoft.studio.model.form.Form;
 import org.bonitasoft.studio.model.form.Widget;
 import org.bonitasoft.studio.model.process.BoundaryEvent;
 import org.bonitasoft.studio.model.process.Connection;
 import org.bonitasoft.studio.model.process.Container;
 import org.bonitasoft.studio.model.process.FlowElement;
 import org.bonitasoft.studio.model.process.MainProcess;
 import org.bonitasoft.studio.model.process.diagram.form.part.FormDiagramEditor;
 import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditor;
 import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditorPlugin;
 import org.bonitasoft.studio.model.process.diagram.part.ProcessDiagramEditorUtil;
 import org.bonitasoft.studio.model.process.diagram.providers.ProcessMarkerNavigationProvider;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.validation.AbstractModelConstraint;
 import org.eclipse.emf.validation.EMFEventType;
 import org.eclipse.emf.validation.IValidationContext;
 import org.eclipse.emf.validation.model.IConstraintStatus;
 import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeCompartmentEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
 import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * @author Romain Bioteau
  *
  */
 public abstract class AbstractLiveValidationMarkerConstraint extends AbstractModelConstraint {
 
 	private static final String CONSTRAINT_ID = "constraintId";
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.emf.validation.AbstractModelConstraint#validate(org.eclipse.emf.validation.IValidationContext)
 	 */
 	@Override
 	public final IStatus validate(IValidationContext ctx) {
 		IStatus status = null ;
 		final EMFEventType eType = ctx.getEventType();
 		final EStructuralFeature featureTriggered = ctx.getFeature();
 		if(featureTriggered != null && featureTriggered.equals(NotationPackage.Literals.VIEW__ELEMENT)){
 			EObject eobject = (EObject) ctx.getFeatureNewValue();
 			if(eobject != null){
 				MainProcess mainProc = ModelHelper.getMainProcess(eobject);
 				if(mainProc!= null && !mainProc.isEnableValidation()){
 					return Status.OK_STATUS;
 				}
 			}
 		}
 		if (eType != EMFEventType.NULL) { //LIVE
 			status = performLiveValidation(ctx);
 			updateValidationMarkersOnDiagram(status, ctx);
 		}else{ //Batch
 			status = performBatchValidation(ctx);
 		}
 		
 		return status;
 	}
 
 	
 	private void updateValidationMarkersOnDiagram(IStatus status,IValidationContext context) {
 		if(PlatformUI.isWorkbenchRunning() &&  PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow() != null &&  PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow().getActivePage() != null){
 			IEditorPart editorPart = PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow().getActivePage()
 					.getActiveEditor();
 
 			if (editorPart instanceof DiagramEditor) {
 				EObject validatedObject  = context.getTarget();
 				if(status instanceof IConstraintStatus){
 					validatedObject = ((IConstraintStatus) status).getTarget();
 				}
 				if(validatedObject == null){
 					return;
 				}
 				View view = null ;
 				if(validatedObject instanceof View){
 					view = (View) validatedObject;
 				}else{
 					view = getViewFor((DiagramEditor) editorPart,validatedObject);
 				}
 				if(view == null ){
 					return ;
 				}
 				String viewId = ViewUtil.getIdStr(view);
 
 				final DiagramEditPart diagramEditPart = ((DiagramEditor) editorPart).getDiagramEditPart();
 				IFile target = diagramEditPart.getDiagramView().eResource() != null ? WorkspaceSynchronizer.getFile(diagramEditPart.getDiagramView().eResource()) : null;
 				if (target != null) {
 					try {
 						for(IMarker marker : target.findMarkers(getMarkerType((DiagramEditor) editorPart), false, IResource.DEPTH_ZERO)){
 							String elementId = (String) marker.getAttribute(org.eclipse.gmf.runtime.common.ui.resources.IMarker.ELEMENT_ID);
 							String constraintId = (String) marker.getAttribute(CONSTRAINT_ID);
 							if(elementId != null && elementId.equals(viewId) && getConstraintId().equals(constraintId)){
 								marker.delete();
 							}
 						}
 					} catch (CoreException e) {
 						BonitaStudioLog.error(e);
 					}
 				}
 
 				// create problem markers on the appropriate resources
 				if(!status.isOK()){
 					createMarkers(target,(IStatus) status, diagramEditPart,(DiagramEditor) editorPart);
 				}
 			}
 		}
 	}
 
 	protected abstract IStatus performLiveValidation(IValidationContext context);
 
 	protected abstract IStatus performBatchValidation(IValidationContext context);
 
 	protected String getMarkerType(DiagramEditor editor){
 		if(editor instanceof ProcessDiagramEditor){
 			 return ProcessMarkerNavigationProvider.MARKER_TYPE;
 		}else if (editor instanceof FormDiagramEditor){
 			 return org.bonitasoft.studio.model.process.diagram.form.providers.ProcessMarkerNavigationProvider.MARKER_TYPE;
 		}
 		return null;
 	}
 
 	protected abstract String getConstraintId();
 
 	private View getViewFor(DiagramEditor editor,EObject validatedObject) {
 		if(editor instanceof ProcessDiagramEditor){
 			if(!(validatedObject instanceof FlowElement 
 					|| validatedObject instanceof BoundaryEvent 
 					|| validatedObject instanceof Container 
 					|| validatedObject instanceof Connection)){
 				validatedObject = ModelHelper.getParentFlowElement(validatedObject);
 			}
 		}else if(editor instanceof FormDiagramEditor){
 			if(!(validatedObject instanceof Widget 
 					|| validatedObject instanceof Form)){
 				EObject result = ModelHelper.getParentWidget(validatedObject);
 				if(result == null){
 					result = ModelHelper.getParentForm(validatedObject);
 				}
 				validatedObject = result;
 			}
 		}
 		for(Object ep : editor.getDiagramGraphicalViewer().getEditPartRegistry().values()){
 			if(!(ep instanceof ITextAwareEditPart) && !(ep instanceof ShapeCompartmentEditPart) && ep instanceof IGraphicalEditPart && ((IGraphicalEditPart)ep).resolveSemanticElement() != null &&  ((IGraphicalEditPart)ep).resolveSemanticElement().equals(validatedObject)){
 				return ((IGraphicalEditPart)ep).getNotationView();
 			}
 		}
 		return null ;
 	}
 
 	private void createMarkers(IFile target, IStatus validationStatus,
 			DiagramEditPart diagramEditPart,DiagramEditor editor) {
 		if (validationStatus.isOK()) {
 			return;
 		}
 		final IStatus rootStatus = validationStatus;
 		List allStatuses = new ArrayList();
 		ProcessDiagramEditorUtil.LazyElement2ViewMap element2ViewMap = new ProcessDiagramEditorUtil.LazyElement2ViewMap(
 				diagramEditPart.getDiagramView(), collectTargetElements(
 						rootStatus, new HashSet<EObject>(), allStatuses));
 		for (Iterator it = allStatuses.iterator(); it.hasNext();) {
 			IConstraintStatus nextStatus = (IConstraintStatus) it.next();
 			EObject targetEObject = nextStatus.getTarget();
 			View view = null ;
 			if(targetEObject instanceof View){
 				view = (View) targetEObject;
 			}else{
 				view = ProcessDiagramEditorUtil.findView(diagramEditPart,
 						targetEObject, element2ViewMap);
 			}
 			addMarker(editor,diagramEditPart.getViewer(), target,ViewUtil.getIdStr(view), EMFCoreUtil.getQualifiedName(
 					nextStatus.getTarget(), true), nextStatus.getMessage(),
 					nextStatus.getSeverity());
 		}
 	}
 
 	private static Set<EObject> collectTargetElements(IStatus status,
 			Set<EObject> targetElementCollector, List allConstraintStatuses) {
 		if (status instanceof IConstraintStatus) {
 			targetElementCollector
 			.add(((IConstraintStatus) status).getTarget());
 			allConstraintStatuses.add(status);
 		}
 		if (status.isMultiStatus()) {
 			IStatus[] children = status.getChildren();
 			for (int i = 0; i < children.length; i++) {
 				collectTargetElements(children[i], targetElementCollector,
 						allConstraintStatuses);
 			}
 		}
 		return targetElementCollector;
 	}
 
 	private void addMarker(DiagramEditor editor,EditPartViewer viewer, IFile target,
 			String elementId, String location, String message,
 			int statusSeverity) {
 		if (target == null) {
 			return;
 		}
 		IMarker marker = null;
 		try {
 			marker = target.createMarker(getMarkerType(editor));
 			marker.setAttribute(IMarker.MESSAGE, message);
 			marker.setAttribute(IMarker.LOCATION, location);
 			marker.setAttribute(
 					org.eclipse.gmf.runtime.common.ui.resources.IMarker.ELEMENT_ID,
 					elementId);
 			marker.setAttribute(CONSTRAINT_ID,getConstraintId());
 			int markerSeverity = IMarker.SEVERITY_INFO;
 			if (statusSeverity == IStatus.WARNING) {
 				markerSeverity = IMarker.SEVERITY_WARNING;
 			} else if (statusSeverity == IStatus.ERROR
 					|| statusSeverity == IStatus.CANCEL) {
 				markerSeverity = IMarker.SEVERITY_ERROR;
 			}
 			marker.setAttribute(IMarker.SEVERITY, markerSeverity);
 		} catch (CoreException e) {
 			ProcessDiagramEditorPlugin.getInstance().logError(
 					"Failed to create validation marker", e); //$NON-NLS-1$
 		}
 	}
 
 }
