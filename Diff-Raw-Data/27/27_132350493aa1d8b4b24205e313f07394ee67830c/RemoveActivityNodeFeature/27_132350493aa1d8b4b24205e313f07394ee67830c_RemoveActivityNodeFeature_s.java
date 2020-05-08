 package org.modelexecution.xmof.diagram.features;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IRemoveFeature;
 import org.eclipse.graphiti.features.context.IRemoveContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityEdge;
 import org.modelexecution.xmof.Syntax.Activities.IntermediateActivities.ActivityNode;
 
 public class RemoveActivityNodeFeature extends DefaultRemoveFeature {
 
 	public RemoveActivityNodeFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 	
 	@Override
 	public void remove(IRemoveContext context) {
 		removeNodes(context);
 		super.remove(context);
 	}
 	
 	private void removeNodes(IRemoveContext context) {
 		EList<EObject> activityNodes = context.getPictogramElement().getLink()
 				.getBusinessObjects();
 		for (EObject eObject : activityNodes) {
 			if (eObject instanceof ActivityNode) {
 				ActivityNode activityNode = (ActivityNode) eObject;
 				removeEdges(activityNode.getIncoming());
 				removeEdges(activityNode.getOutgoing());
 			}
 		}				
 	}
 	
 	protected void removeEdges(EList<ActivityEdge> edges) {
 		for (ActivityEdge edge : new BasicEList<ActivityEdge>(edges)) {
 			remove(getEdgeConnection(edge));
 		}
 	}
 	
 	protected PictogramElement getEdgeConnection(ActivityEdge edge) {
 		return getFeatureProvider().getPictogramElementForBusinessObject(edge);
 	}
 
 	protected void remove(PictogramElement pictogramElement) {
 		IRemoveContext removeContext = new RemoveContext(pictogramElement);
 		IRemoveFeature removeFeature = getFeatureProvider().getRemoveFeature(
 				removeContext);
 		if (removeFeature != null) {
 			removeFeature.remove(removeContext);
 		}
 	}
 	
 	
 }
