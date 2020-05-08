 package de.opalproject.vespucci.sliceEditor.features;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 
 import de.opalproject.vespucci.datamodel.DatamodelFactory;
 import de.opalproject.vespucci.datamodel.Ensemble;
 
 public class CreateEmptyEnsembleFeature extends AbstractCreateFeature {
  
     private static final String TITLE = "Empty Ensemble";
  
     public CreateEmptyEnsembleFeature(IFeatureProvider fp) {
         // set name and description of the creation feature
         super(fp, "Empty Ensemble", "Create Empty Ensemble");
     }
     
 	@Override
 	public String getCreateImageId() {
 		return "de.opalproject.vespucci.sliceEditor.emptyEnsemble";
 	} 
  
     public boolean canCreate(ICreateContext context) {
         return context.getTargetContainer() instanceof Diagram;
     }
  
     public Object[] create(ICreateContext context) {
  
         // create Ensemble
     	DatamodelFactory factory = DatamodelFactory.eINSTANCE;
     	// TODO change to type empty ensemble
        Ensemble ens = factory.createConcreteEnsemble();
         // Add model element to resource.
         // We add the model element to the resource of the diagram for
         // as it is not needed in the Ensemble Explorer.
         getDiagram().eResource().getContents().add(ens);
         ens.setName(TITLE);
         ens.setQuery("empty");
  
         // do the add
         addGraphicalRepresentation(context, ens);
  
         // return newly created business object(s)
         return new Object[] { ens };
     }
 }
