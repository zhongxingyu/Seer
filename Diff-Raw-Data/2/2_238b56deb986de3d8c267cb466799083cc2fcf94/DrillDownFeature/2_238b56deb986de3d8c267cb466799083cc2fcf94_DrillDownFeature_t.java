 package org.eclipse.editor.features;
 
 import static com.google.common.base.Predicates.instanceOf;
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.Iterables.find;
 import static java.util.Arrays.asList;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.platform.IDiagramEditor;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IPeService;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
 import org.eclipse.graphiti.ui.features.AbstractDrillDownFeature;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 
 import com.google.common.base.Function;
 
 public class DrillDownFeature extends AbstractDrillDownFeature {
 
 	public DrillDownFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public String getName() {
 		return "Open subdiagram";
 	}
 
 	@Override
 	public String getDescription() {
		return "Open the subdiagram associated with this item";
 	}
 
 	@Override
 	public boolean canExecute(ICustomContext context) {
 		return getBusinessObject(context) != null;
 	}
 
 	private EClass getBusinessObject(ICustomContext context) {
 		List<PictogramElement> pes = asList(context.getPictogramElements());
 		if (pes.size() != 1)
 			return null;
 
 		return (EClass) find(transform(pes, toBusinessObject()), instanceOf(EClass.class), null);
 	}
 
 	private Function<PictogramElement, Object> toBusinessObject() {
 		return new Function<PictogramElement, Object>() {
 			@Override
 			public Object apply(PictogramElement pe) {
 				return getBusinessObjectForPictogramElement(pe);
 			}
 		};
 	}
 
 	@Override
 	protected Collection<Diagram> getDiagrams() {
 		Collection<EObject> contents = getDiagram().eResource().getContents();
 		return transform(filter(contents, instanceOf(Diagram.class)), castToDiagram());
 	}
 
 	@Override
 	public void execute(ICustomContext context) {
 		if (super.canExecute(context)) {
 			super.execute(context);
 		} else {
 			createNewDiagramAndOpenIt(context);
 		}
 	}
 
 	private void createNewDiagramAndOpenIt(ICustomContext context) {
 		try {
 			EClass businessObject = getBusinessObject(context);
 			Diagram newDiagram = createNewDiagram(businessObject.getName() + "-sub");
 			openDiagramEditor(newDiagram, getDiagramEditor().getEditingDomain(), getFeatureProvider().getDiagramTypeProvider().getProviderId(), false);
 			link(newDiagram, businessObject);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Diagram createNewDiagram(String name) throws CoreException {
 		IFeatureProvider featureProvider = getFeatureProvider();
 		Diagram currentDiagram = featureProvider.getDiagramTypeProvider().getDiagram();
 		IPeService peService = Graphiti.getPeService();
 		Diagram newDiagram = peService.createDiagram(currentDiagram.getDiagramTypeId(), name, currentDiagram.isSnapToGrid());
 		currentDiagram.eResource().getContents().add(newDiagram);
 
 		return newDiagram;
 	}
 
 	public IDiagramEditor openDiagramEditor(Diagram diagram, TransactionalEditingDomain domain, String providerId, boolean disposeEditingDomain) {
 		IDiagramEditor ret = null;
 		DiagramEditorInput diagramEditorInput = DiagramEditorInput.createEditorInput(diagram, domain, providerId, disposeEditingDomain);
 		IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		try {
 			IEditorPart editorPart = IDE.openEditor(workbenchPage, diagramEditorInput, DiagramEditor.DIAGRAM_EDITOR_ID);
 			if (editorPart instanceof IDiagramEditor) {
 				ret = (IDiagramEditor) editorPart;
 			}
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}
 
 		return ret;
 	}
 
 	private static Function<Object, Diagram> castToDiagram() {
 		return new Function<Object, Diagram>() {
 			@Override
 			public Diagram apply(Object o) {
 				return (Diagram) o;
 			}
 
 		};
 	}
 }
