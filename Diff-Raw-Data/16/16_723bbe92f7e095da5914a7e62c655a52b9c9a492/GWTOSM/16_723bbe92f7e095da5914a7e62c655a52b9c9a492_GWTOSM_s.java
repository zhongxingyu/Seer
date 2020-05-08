 package org.openstreetmap.gwt.client;
 
 import org.openstreetmap.gwt.shared.FieldVerifier;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.shell.remoteui.MessageTransport.RequestException;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ButtonBase;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.xml.client.DOMException;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Element;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 import com.google.gwt.xml.client.XMLParser;
 //import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
 import org.vaadin.gwtgraphics.client.DrawingArea;
 import org.vaadin.gwtgraphics.client.animation.Animate;
 import org.vaadin.gwtgraphics.client.shape.Circle;
 import org.vaadin.gwtgraphics.client.shape.Path;
 import org.vaadin.gwtgraphics.client.shape.Text;
 import org.vaadin.gwtgraphics.client.shape.Rectangle;
 import org.vaadin.gwtgraphics.client.shape.path.LineTo;
 import org.openstreetmap.gwt.client.GWT2dAdaptor;
 import org.openstreetmap.josm.data.Bounds;
 import org.openstreetmap.josm.data.coor.LatLon;
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.data.osm.Main;
 //import org.openstreetmap.josm.data.osm.Node;
 import org.openstreetmap.josm.data.osm.OsmPrimitive;
 import org.openstreetmap.josm.data.osm.visitor.paint.GWTGraphics2D;
 import org.openstreetmap.josm.data.osm.visitor.paint.IGwtGraphics2D;
 import org.openstreetmap.josm.data.osm.visitor.paint.INavigatableComponent;
 import org.openstreetmap.josm.data.osm.visitor.paint.MapPaintVisitor;
 import org.openstreetmap.josm.data.osm.visitor.paint.NavigatableComponent;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class GWTOSM implements EntryPoint {
 
     // the canvas!
     //    private GWTCanvas canvas;
     private IGwtGraphics2D canvas;
     MapPaintVisitor painter;
   /**
    * The message displayed to the user when the server cannot be reached or
    * returns an error.
    */
   private static final String SERVER_ERROR = "An error occurred while "
       + "attempting to contact the server. Please check your network "
       + "connection and try again.";
 
   /**
    * Create a remote service proxy to talk to the server-side Greeting service.
    */
   private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
   private DataSet data= new DataSet();
 
   /**
    * This is the entry point method.
    */
     
   public void drawmap() {
 
       String on = "Integer.toString(orderNumber)";
       int strlen = on.length();
        int x = (400)/2 - 4*strlen;
        int y = (400)/2 + 4;
        canvas.drawString(on, x, y);
 
        try {     
     	   
     	  // data = new DataSet();
     	   INavigatableComponent nc = new NavigatableComponent(data );
     	   painter=new MapPaintVisitor (nc);
 
 	   boolean virtual=false;
 
 	   
 	   
 	   fetchData();
 		OsmPrimitive primitive = new org.openstreetmap.josm.data.osm.Node(new LatLon(1,2));
 		data.addPrimitive(primitive );
 
 		OsmPrimitive primitive2 = new org.openstreetmap.josm.data.osm.Node(new LatLon(42,22));
 		primitive2.setOsmId(2, 1);
 		data.addPrimitive(primitive2 );
 
 		OsmPrimitive primitive3 = new org.openstreetmap.josm.data.osm.Node(new LatLon(5.1,5.2));
 		primitive3.setOsmId(3, 1);
 		data.addPrimitive(primitive3 );
 
 		OsmPrimitive primitive4 = new org.openstreetmap.josm.data.osm.Node(new LatLon(10,23));
 		primitive4.setOsmId(4, 1);
 		data.addPrimitive(primitive4 );
 
 	 	Bounds bounds= new Bounds(-180,-90, 180,90);
 	 	//x: -180.0 -> 180.0, y: -90.0 -> 90.0
 	 	
 	 	
 	 	painter.setGraphics(canvas);
 	   painter.visitAll(data, virtual, bounds);
        }
        catch (Exception e)
 	   {
 	       GWT.log("exception!"  + e.toString());
 	   }
   }
 
   public void fetchData()
   {
 	  RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET,
       "http://api.openstreetmap.org/api/0.6/map?bbox=19.508028,42.0629942,19.5137787,42.0668174");
   try {
     requestBuilder.sendRequest(null, new RequestCallback() {
       public void onError(Request request, Throwable exception) {
         requestFailed(exception);
       }
      private void requestFailed(Throwable exception) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void onResponseReceived(Request request, Response response) {
         renderXML(response.getText());
       }
 	private void renderXML(String text) {
 		// TODO Auto-generated method stub
 		//data
 		
 		 try {
 			    // parse the XML document into a DOM
 			    Document messageDom = XMLParser.parse(text);
 
 			    NodeList nodes = messageDom.getElementsByTagName("node");
 			    
 			    for (int i =0; i < nodes.getLength(); i++)
 			    {
 			    	Node node=nodes.item(i);
 			    	String lat = ((Element)node).getAttribute("lat");
 			    	String lon = ((Element)node).getAttribute("lon");
 			    	String id = ((Element)node).getAttribute("id");
 			    	String version = ((Element)node).getAttribute("version");
 			    	GWT.log(lat);
 			    	int ilon=Integer.parseInt(lon);
 					int ilat=Integer.parseInt(lat);
 					int iid=Integer.parseInt(id);
 					int iversion=Integer.parseInt(version);
 					OsmPrimitive primitive = new org.openstreetmap.josm.data.osm.Node(new LatLon(ilat,ilon));
 			    	primitive .setOsmId(iid, iversion);
 			    	
 					data.addPrimitive(primitive );
 			    }
 			    
 		 }	
 		 catch (DOMException e) {
 			    Window.alert("Could not parse XML document.");
 			  }
 
 	}
 	
     });
   } catch (com.google.gwt.http.client.RequestException e) {
 	// TODO Auto-generated catch block
 	e.printStackTrace();
 	requestFailed(e);
 }
   }
   private void requestFailed(com.google.gwt.http.client.RequestException e) {
 	// TODO Auto-generated method stub
 	
 }
 
private void requestFailed(RequestException ex) {
	// TODO Auto-generated method stub
	
}
 
 public void onModuleLoad() {
     final Button sendButton = new Button("Send");
     final TextBox nameField = new TextBox();
     nameField.setText("GWT User");
     final Label errorLabel = new Label();
 
     try
 	{
 	    LatLon pos = new LatLon(1, 2);
 	    OsmPrimitive primitive = new org.openstreetmap.josm.data.osm.Node(pos );
 	    data.addPrimitive(primitive);
 	    
 	  
 	}
     catch (Exception e)
 	{
     	GWT.log(e.getMessage());
 	}
     // We can add style names to widgets
     sendButton.addStyleName("sendButton");
 
     // Add the nameField and sendButton to the RootPanel
     // Use RootPanel.get() to get the entire body element
     RootPanel.get("nameFieldContainer").add(nameField);
     RootPanel.get("sendButtonContainer").add(sendButton);
     RootPanel.get("errorLabelContainer").add(errorLabel);
 
     // Focus the cursor on the name field when the app loads
     nameField.setFocus(true);
     nameField.selectAll();
 
     // Create the popup dialog box
     final DialogBox dialogBox = new DialogBox();
     dialogBox.setText("Remote Procedure Call");
     dialogBox.setAnimationEnabled(true);
     final Button closeButton = new Button("Close");
     // We can set the id of a widget by accessing its Element
     closeButton.getElement().setId("closeButton");
     final Label textToServerLabel = new Label();
     final HTML serverResponseLabel = new HTML();
     VerticalPanel dialogVPanel = new VerticalPanel();
     dialogVPanel.addStyleName("dialogVPanel");
     dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
     dialogVPanel.add(textToServerLabel);
     dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
     dialogVPanel.add(serverResponseLabel);
     dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
 
 
     canvas = new GWTGraphics2D(dialogVPanel);
     
     drawmap();
 
     dialogVPanel.add(closeButton);
     dialogBox.setWidget(dialogVPanel);
 
     // Add a handler to close the DialogBox
     closeButton.addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent event) {
         dialogBox.hide();
         sendButton.setEnabled(true);
         sendButton.setFocus(true);
       }
     });
 
     // Create a handler for the sendButton and nameField
     class MyHandler implements ClickHandler, KeyUpHandler {
       /**
        * Fired when the user clicks on the sendButton.
        */
       public void onClick(ClickEvent event) {
         sendNameToServer();
       }
 
       /**
        * Fired when the user types in the nameField.
        */
       public void onKeyUp(KeyUpEvent event) {
         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
           sendNameToServer();
         }
       }
 
       /**
        * Send the name from the nameField to the server and wait for a response.
        */
       private void sendNameToServer() {
         // First, we validate the input.
         errorLabel.setText("");
         String textToServer = nameField.getText();
         if (!FieldVerifier.isValidName(textToServer)) {
           errorLabel.setText("Please enter at least four characters");
           return;
         }
         
         // Then, we send the input to the server.
         sendButton.setEnabled(false);
         textToServerLabel.setText(textToServer);
         serverResponseLabel.setText("");
         greetingService.greetServer(textToServer, new AsyncCallback<String>() {
           public void onFailure(Throwable caught) {
             // Show the RPC error message to the user
             dialogBox.setText("Remote Procedure Call - Failure");
             serverResponseLabel.addStyleName("serverResponseLabelError");
             serverResponseLabel.setHTML(SERVER_ERROR);
             dialogBox.center();
             closeButton.setFocus(true);
           }
 
           public void onSuccess(String result) {
             dialogBox.setText("Remote Procedure Call");
             serverResponseLabel.removeStyleName("serverResponseLabelError");
             serverResponseLabel.setHTML(result);
             dialogBox.center();
             closeButton.setFocus(true);
           }
         });
       }
     }
 
     // Add a handler to send the name to the server
     MyHandler handler = new MyHandler();
     sendButton.addClickHandler(handler);
     nameField.addKeyUpHandler(handler);
   }
 }
