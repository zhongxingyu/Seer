 package uk.ac.ebi.biosd.client.ui.test;
 
 import uk.ac.ebi.biosd.client.query.AttributedObject;
 
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.widgets.Window;
 
 public class ObjectViewer extends Window
 {
  public ObjectViewer( AttributedObject obj )
  {
   setTitle( "Value properties: "+obj.getStringValue() );  
   setWidth(750);  
  setHeight(105); 
 //  setLeft(offsetLeft);  
   setCanDragReposition(true);  
   setCanDragResize(true);  
   setShowMinimizeButton(false);
   setIsModal(true);
   setShowModalMask(true);
   centerInPage();
   
   setAlign(VerticalAlignment.CENTER);
 
 //  addItem( new ObjectViewPanel(obj) );
   addItem( new ObjectViewPanelHTML(obj) );
  }  
 }
