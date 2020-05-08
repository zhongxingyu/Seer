 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app.gui.svgComponents;
 
 import app.utils.Utils;
 import config.MainConfiguration;
 import config.SVGConfiguration;
 import java.awt.Color;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import javax.swing.JLabel;
 import org.apache.batik.dom.svg.SVGOMPoint;
 import org.apache.batik.swing.JSVGCanvas;
 import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
 import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
 import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
 import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
 import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
 import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
 import org.w3c.dom.Element;
 import org.w3c.dom.svg.SVGDocument;
 import org.w3c.dom.svg.SVGLocatable;
 import org.w3c.dom.svg.SVGMatrix;
 
 /**
  *
  * @author vara
  */
 
 /* important "Rendering Process"
  * 
  *  The rendering process can be broken down into five phases. 
  *  Not all of those steps are required - depending on the method used to specify 
  *  the SVG document to display, but basically the steps in the rendering process are:
  *
  *  --Building a DOM tree 
  *	If the loadSVGDocument(String) method is used, the SVG file is parsed 
  *	and an SVG DOM Tree is built.
  * 
  *  --Building a GVT tree 
  *	Once an SVGDocument is created (using the step 1 or if the setSVGDocument(SVGDocument) 
  *	method has been used) - a GVT tree is constructed. 
  *	The GVT tree is the data structure used internally to render an SVG document. 
  *	see the org.apache.batik.gvt package.
  * 
  *  --Executing the SVGLoad event handlers 
  *	If the document is dynamic, the scripts are initialized and the SVGLoad 
  *	event is dispatched before the initial rendering. 
  * 
  *  --Rendering the GVT tree 
  *	Then the GVT tree is rendered. see the org.apache.batik.gvt.renderer package.
  * 
  *  --Running the document 
  *	If the document is dynamic, the update threads are started.
  */
 
 public class Canvas extends JSVGCanvas{
 
     private LabelStatusDocument labStatusDocument= new LabelStatusDocument("Chose Document");
     private JLabel labelViewMousePosyton = new JLabel("");
     private SVGConfiguration svgConfig = new SVGConfiguration();
     
     public Canvas(){	
 	
 	//setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
 	setDocumentState(Canvas.ALWAYS_STATIC);
 	addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
 		@Override
 		public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
 		    String currentStatus = "Document Loading...";
 		    labStatusDocument.setText(currentStatus);
 		    if(MainConfiguration.getMode()){			
 			System.out.println(currentStatus);
 		    }
 		}
 		@Override
 		public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
 		    String currentStatus = "Document Loading Completed";
 		    labStatusDocument.setText(currentStatus);
 		    if(MainConfiguration.getMode()){			
 			System.out.println(currentStatus);
 		    }
 		}
 	    });
 	    
 	addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
 	    @Override
 	    public void gvtBuildStarted(GVTTreeBuilderEvent e) {
 		    
 		    String currentStatus = "Document Build Started...";
 		    labStatusDocument.setText(currentStatus);
 		    if(MainConfiguration.getMode()){			
 			System.out.println(currentStatus);
 		    }
 	    }
 	    @Override
 	    public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
 		    String currentStatus = "Document Build Completed";
 		    labStatusDocument.setText(currentStatus);
 		    if(MainConfiguration.getMode()){			
 			System.out.println(currentStatus);
 		    }
 	    }
 	});
 	addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
 	    @Override
 	    public void gvtRenderingPrepare(GVTTreeRendererEvent e) {

 		    String currentStatus = "Document Rendering Prepare ...";
 		    labStatusDocument.setText(currentStatus);
 		    if(MainConfiguration.getMode()){			
 			System.out.println(currentStatus);
 		    }
 
 	    }
 	    @Override
 	    public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
 
 		    String currentStatus = "Document Rendering Completed";
 		    labStatusDocument.setText(currentStatus);
 		    if(MainConfiguration.getMode()){			
 			System.out.println(currentStatus);
 		    }

 	    }
 	});
 	
 	ZoomAndMoveMouseListener listener=new  ZoomAndMoveMouseListener();
         addMouseListener(listener);
         addMouseMotionListener(listener);
 	
     }
     public boolean isDocumentSet(){ return (getSVGDocument() != null);}
     
     protected void setTextforLabStatusDocument(String str){
 	labStatusDocument.setText(str);
     }
     
     public LabelStatusDocument getLabStatusDocument(){
 	return labStatusDocument;
     }
 
     public JLabel getLabelViewMousePosyton() {
 	return labelViewMousePosyton;
     }
     
     protected class LabelStatusDocument extends JLabel implements MouseMotionListener{
 
 	public LabelStatusDocument(String str){
 	    super(str);
 	    //setForeground(Color.BLUE);
 	    addMouseMotionListener(this);
 	    setVerticalAlignment(0);
 	    setBorder(Utils.createSimpleBorder(1,1,1,1,Color.BLUE));
 	    
 	}
 	public void mouseDragged(MouseEvent e) {
 	    
 	}
 
 	public void mouseMoved(MouseEvent e) {
 	    setToolTipText(getText());
 	    //System.out.println("mouseMoved");
 	}
     }//class LabelStatusDocument
     
     private final class ZoomAndMoveMouseListener extends MouseAdapter
     {
 	@Override
 	public void mouseClicked(MouseEvent evt) {
 	    AffineTransform at;
 
 	    at = getRenderingTransform();
 	    if (at != null) {
 		    Point2D p2d = new Point2D.Double(evt.getX(),evt.getY());
 		    at.preConcatenate(AffineTransform.getTranslateInstance(-p2d.getX(),-p2d.getY()));
 
 		if(evt.getButton()==3){
 
 		    at.preConcatenate(AffineTransform.getScaleInstance(svgConfig.getZoomOutRateX(),
 								       svgConfig.getZoomOutRateY()));
 		    if(MainConfiguration.getMode())
 			System.out.println("zoomout");
 		}
 		else {
 
 		    at.preConcatenate(AffineTransform.getScaleInstance(svgConfig.getZoomInRateX(),
 								       svgConfig.getZoomInRateX()));
 		    if(MainConfiguration.getMode())
 			System.out.println("zoomin");
 		}
 
 		at.preConcatenate(AffineTransform.getTranslateInstance(p2d.getX(),p2d.getY()));
 		setRenderingTransform(at);
 	    }
 	}
 	@Override
 	public void mouseDragged(MouseEvent e) {
 	    //throw new UnsupportedOperationException("Not supported yet.");
 	    
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
	    SVGDocument doc = getSVGDocument();
	    if(doc != null){
 		SVGOMPoint svgp =getLocalPoint(doc.getRootElement(),e.getX() ,e.getY());
 		String str = "source comp. ("+e.getX()+","+e.getY()+") "+
 			     "screen ("+e.getXOnScreen()+","+e.getYOnScreen()+") "+
 			     "root Elemnt ("+svgp.getX()+","+svgp.getY()+")";
 		labelViewMousePosyton.setText(str);
 	    }	    
 	}
     }
     
     static protected SVGOMPoint getLocalPoint(Element element, int x, int y) 
     {	    
 	    SVGMatrix mat = ((SVGLocatable) element).getScreenCTM();
 	    SVGMatrix imat = mat.inverse(); // screen -> elem
 	    SVGOMPoint pt = new SVGOMPoint(x, y);
 	    return (SVGOMPoint) pt.matrixTransform(imat);
     }    
 }
