 package org.eclipse.gmf.internal.bridge.genmodel;
 
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.ModeledViewmap;
 import org.eclipse.gmf.codegen.gmfgen.Viewmap;
 import org.eclipse.gmf.gmfgraph.Canvas;
 import org.eclipse.gmf.gmfgraph.Compartment;
 import org.eclipse.gmf.gmfgraph.Connection;
 import org.eclipse.gmf.gmfgraph.DiagramElement;
 import org.eclipse.gmf.gmfgraph.DiagramLabel;
 import org.eclipse.gmf.gmfgraph.Node;
 
 public class ModeledViewmapProducer extends DefaultViewmapProducer {
 
 	@Override
 	public Viewmap create(Canvas canvasElement) {
 		ModeledViewmap result = GMFGenFactory.eINSTANCE.createModeledViewmap();
 		result.setFigureModel(canvasElement);
 		return result;
 	}
 
 	@Override
 	public Viewmap create(Node node) {
		return createModeledViewmap(node);
 	}
 
 	@Override
 	public Viewmap create(Connection link) {
 		return createModeledViewmap(link);
 	}
 
 	@Override
 	public Viewmap create(Compartment compartment) {
 		return createModeledViewmap(compartment);
 	}
 
 	@Override
 	public Viewmap create(DiagramLabel label) {
 		return createModeledViewmap(label);
 	}
 
 	@Override
 	public String[] dependencies() {
 		return new String[0];
 	}
 
 	private ModeledViewmap createModeledViewmap(DiagramElement diagramElement) {
 		ModeledViewmap result = GMFGenFactory.eINSTANCE.createModeledViewmap();
 		result.setFigureModel(diagramElement);
 		return result;
 	}
 
 }
