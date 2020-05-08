 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app.gui.svgComponents;
 
 import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
 import org.apache.batik.swing.gvt.GVTTreeRendererListener;
 import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
 import org.apache.batik.swing.svg.GVTTreeBuilderListener;
 import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
 import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
 import org.w3c.dom.svg.SVGDocument;
 
 /**
  *
  * @author vara
  */
 public class SVGBridgeListeners extends SVGBridgeComponents implements 
 				SVGDocumentLoaderListener,
 				GVTTreeBuilderListener,
 				GVTTreeRendererListener	{
     
     private String absoluteFilePath="";
     
     public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
	
 	setTextToCurrentStatus("Document Loading Started ...");
     }
 
     public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
 	
 	setTextToCurrentStatus("Document Loading Completed");
     }
 
     public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
 	setTextToCurrentStatus("Document Loading Cancelled !");
     }
 
     public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
 	setTextToCurrentStatus("Document Loading Failed !");
     }
 
     public void documentLoadingCompleted(GVTTreeBuilderEvent e) {
 	setTextToCurrentStatus("Document Loading Completed");
     }
 
     public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
 	setTextToCurrentStatus("Document Build Completed");
     }
 
     public void gvtBuildCancelled(GVTTreeBuilderEvent e) {
 	setTextToCurrentStatus("Document Build Cancelled !");
     }
 
     public void gvtBuildFailed(GVTTreeBuilderEvent e) {
 	setTextToCurrentStatus("Document Build Failed !");
     }
 
     public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
 	setTextToCurrentStatus("Document Rendering Prepare ...");	
     }
 
     public void gvtRenderingStarted(GVTTreeRendererEvent e) {
 	setTextToCurrentStatus("Document Rendering Started ...");
 	setRederingStatus(true);
     }
 
     public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
 	
 	setTextToCurrentStatus(absoluteFilePath);
 	setRederingStatus(false);
     }
 
     public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
 	setTextToCurrentStatus("Documnet Rendering Cancelled !");
     }
 
     public void gvtRenderingFailed(GVTTreeRendererEvent e) {
 	setTextToCurrentStatus("Documnet Rendering Failed !");
     }
 
     public void gvtBuildStarted(GVTTreeBuilderEvent e) {
 	setTextToCurrentStatus("Documnet Build Started");
     }
     public void setAbsoluteFilePath(String path){
 	absoluteFilePath = path;
     }
 }
