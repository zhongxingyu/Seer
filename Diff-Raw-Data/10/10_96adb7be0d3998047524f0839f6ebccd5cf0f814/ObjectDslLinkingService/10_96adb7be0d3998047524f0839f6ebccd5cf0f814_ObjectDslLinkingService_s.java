 package org.neclipse.graphiti.objecteditor.services;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.graphiti.dt.IDiagramType;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.PictogramLink;
 import org.eclipse.graphiti.mm.pictograms.PictogramsFactory;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.neclipse.xtext.objectdsl.objectDsl.NamespaceDeclaration;
 
 /**
  * 
  * @author Neeraj Bhusare
  */
 public class ObjectDslLinkingService {
 
     /**
      * 
      * @param namespaceDeclaration
      */
     public void linkRootModel(final IFile file) {
         // TODO
     }
 
     /**
      * 
      * @param namespaceDeclaration
      */
     public void linkRootModel(final NamespaceDeclaration namespaceDeclaration) {
         // Create a Pictogram link
         final PictogramLink pictogramLink = createPictogramLink();
         pictogramLink.getBusinessObjects().add(namespaceDeclaration);
 
         // Create a Pictogram model and set the link to the PictogramLink
         final PictogramElement pictogramElement = createPictogramElement();
         pictogramElement.setLink(pictogramLink);
     }
 
     /**
      * 
      * @return
      */
     private PictogramLink createPictogramLink() {
         return PictogramsFactory.eINSTANCE.createPictogramLink();
     }
 
     /**
      * 
      * @return
      */
     private PictogramElement createPictogramElement() {
         final IDiagramType[] diagramTypes = GraphitiUi.getExtensionManager().getDiagramTypes();
         if (diagramTypes.length == 0) {
             // TODO: Throw some exception here
         }
 
         final IDiagramType diagramType = diagramTypes[0];
        final Diagram pictogramElement = Graphiti.getPeCreateService().createDiagram(diagramType.getId(), // TODO:
                                                                                                          // Inject the
                                                                                                          // PE create
                                                                                                          // service
                                                                                                          // using
                                                                                                          // Google
                                                                                                          // Guice
                diagramType.getName(), false);// TODO: File a bug for the method with no last param
         return pictogramElement;
     }
 
 }
