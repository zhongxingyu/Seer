 package org.fritzing.fritzing.diagram.export;
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.gef.EditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramGraphicalViewer;
 import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
 import org.eclipse.gmf.runtime.notation.View;
 import org.fritzing.fritzing.Element;
 import org.fritzing.fritzing.Part;
 import org.fritzing.fritzing.Sketch;
 import org.fritzing.fritzing.Wire;
 import org.fritzing.fritzing.diagram.edit.parts.SketchEditPart;
 import org.fritzing.fritzing.impl.LEDImpl;
 import org.fritzing.fritzing.impl.ResistorImpl;
 
 public class Fritzing2Eagle {	
 	public static String createEagleScript(IDiagramGraphicalViewer viewer) {
 		String result = "";
 		SketchEditPart sketchEP = (SketchEditPart) viewer.getContents();
 		Sketch sketch = (Sketch) ((View) sketchEP.getModel()).getElement();
 		
 		ArrayList<EagleSCRPart> partList = new ArrayList<EagleSCRPart>();
 		ArrayList<EagleSCRNet> netList = new ArrayList<EagleSCRNet>();
 	
 		// create a new entry in the ArrayList 'partList' for each component:		
 		for (Part p: sketch.getParts()) {
 			String partClass = "";
 			if (p instanceof ResistorImpl)	
 				partClass = "Resistor";
 			if (p instanceof LEDImpl)
 				partClass = "LED";
 			
 			EagleSCRPart part = new EagleSCRPart(
 				p.getName(),			// part name (e.g. 'R1')
 				partClass,			// part type (e.g. 'Resistor')
 				"fritzing",			// library name
 				new CoordPair(		// Fritzing coordinates
 					((float)getLayoutInfo(viewer, p).getLocation().x), 
 					(float)getLayoutInfo(viewer, p).getLocation().y)
 				);
 			partList.add(part);			
 		}
 		
 		// fine-tune component placement - scale and convert Fritzing coordinates into
 		// coordinates appropriate for Eagle components
 		// first reflect component positions around the X-axis to account for Fritzing 
 		// assuming an origin in the top left and Eagle assuming an origin in the bottom 
 		// left
 		for (int i=0; i<partList.size(); i++) {
 			float xPos = partList.get(i).partPos.xVal;
 			float yPos = partList.get(i).partPos.yVal;
 			float yLimit = (float)3.2;
 			
 			partList.get(i).setPosition(new CoordPair(xPos, (float)(yLimit - yPos)));
 			if (partList.get(i).partType.equalsIgnoreCase("Arduino"))  {
 				partList.get(i).setPosition(new CoordPair((float)0, (float)0));
 				partList.get(i).lockPos();
 			}
 		}
 		
 		// create a new entry in the ArrayList 'netList' for each component:
 		int genericNet = 1;
 		for (Wire w: sketch.getWires()) {
 			String netName = w.getName();
 			if (netName == null) {
 				netName = "N$" + genericNet;
 				genericNet++;
 			}
 			EagleSCRNet net = new EagleSCRNet(
 				netName,
 				w.getSource(),
 				w.getTarget()
 				);
			System.out.println("_____" + netName +"____");			
			System.out.println("source:" + w.getSource().getParent().getName() + "." + w.getSource().getName());						
 			System.out.println("target:" + w.getTarget().getParent().getName() + "." + w.getTarget().getName());
 			netList.add(net);
 		}
 		
 		ScriptExporter exporter = new ScriptExporter();
 
 		result = exporter.export(partList, netList);
 		System.out.println(result);
 		
 		return result;
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	private static ShapeEditPart getLayoutInfo(IDiagramGraphicalViewer viewer, Element e) {
 		List<EditPart> editParts = viewer.findEditPartsForElement(
 				EMFCoreUtil.getProxyID(e), ShapeEditPart.class);
 		return (ShapeEditPart) editParts.get(0);
 	}
 	
 
 }
