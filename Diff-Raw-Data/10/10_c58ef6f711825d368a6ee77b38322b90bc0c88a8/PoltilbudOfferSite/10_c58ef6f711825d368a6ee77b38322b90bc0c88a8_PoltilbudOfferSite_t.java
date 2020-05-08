 package no.poltilbud.site.client;
 
 import no.poltilbud.site.client.widget.SortableTable;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
 import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.xml.client.DOMException;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 import com.google.gwt.xml.client.Text;
 import com.google.gwt.xml.client.XMLParser;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class PoltilbudOfferSite implements EntryPoint {
 	
 	final Label dueDateField = new Label();
 	final SortableTable flexTable = new SortableTable();
 
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		
 		RootPanel.get("pubDateFieldContainer").add(dueDateField);
 		
 		String url = "../thisMonthOffersXML.xml";
 		RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			requestBuilder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					requestFailed(exception);
 				}
 				public void onResponseReceived(Request request, Response response) {
 				      if (200 == response.getStatusCode()) {
 				    	  parseXml(response.getText());
 				      } else {
 				    	  Window.alert("Could not handle response: "
 				  				+ response.getStatusCode()
 				  				+ " " + response.getText() );
 				      }
 				    }      
 			});
 		} catch (RequestException ex) {
 			requestFailed(ex);
 		}
 		
 		final Button aboutUs = new Button("Informasjon");
 
 		flexTable.setStyleName("sortableTable");
 		flexTable.setBorderWidth(1);
 		flexTable.setCellPadding(4);
 		flexTable.setCellSpacing(1);
 		
 		flexTable.addColumnHeader("Produktnavn", 0);
 		flexTable.addColumnHeader("Produkttype", 1);
 		flexTable.addColumnHeader("Pris forrige måned", 2);
 		flexTable.addColumnHeader("Pris denne måned", 3);
 		flexTable.addColumnHeader("Nedsatt kr", 4);
 		flexTable.addColumnHeader("Nedsatt i prosent", 5);
 		
 		RowFormatter rowFormatter = flexTable.getRowFormatter();
 		rowFormatter.setStyleName(0, "tableHeader");
 
 		CellFormatter cellFormatter = flexTable.getCellFormatter();
 		// Set the styles for the headers
 		for (int colIndex=0; colIndex<6; colIndex++){
 			cellFormatter.setStyleName(0, colIndex, "headerStyle");
 			cellFormatter.setAlignment(0, colIndex, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
 		}
 		
 		RootPanel.get("slot1").add(flexTable);
 		RootPanel.get("aboutUsContainer").add(aboutUs);
 		
 		// Create the popup dialog box
 		final DialogBox dialogBox = new DialogBox();
 		dialogBox.setText("Informasjon om poltilbud.com");
 		dialogBox.setAnimationEnabled(true);
 		final Button closeButton = new Button("Lukk");
 		// We can set the id of a widget by accessing its Element
 		closeButton.getElement().setId("closeButton");
 		VerticalPanel dialogVPanel = new VerticalPanel();
 		dialogVPanel.addStyleName("dialogVPanel");
 		dialogVPanel.add(new HTML("Poltilbud.com er kun et hobbyprosjekt opprettet for å teste ut nye teknologier."));
 		dialogVPanel.add(new HTML("<br> Android applikasjon er under utvikling."));
 		dialogVPanel.add(new HTML("<br> Kildekoden finner du på <a href=\"http://github.com/sconrads/Poltilbud-GWT-app\">Github </a>"));
 		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
 		dialogVPanel.add(closeButton);
 		dialogBox.setWidget(dialogVPanel);
 		
 		// Add a handler to close the DialogBox
 		closeButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				dialogBox.hide();
 				aboutUs.setEnabled(true);
 				aboutUs.setFocus(true);
 			}
 		});
 		
 		// Create a handler for the sendButton and nameField
 		class MyHandler implements ClickHandler, KeyUpHandler {
 			/**
 			 * Fired when the user clicks on the sendButton.
 			 */
 			public void onClick(ClickEvent event) {
 				dialogBox.center();
 				closeButton.setFocus(true);
 			}
 
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 				dialogBox.center();
 				closeButton.setFocus(true);
 			}
 		}
 		
 		MyHandler handler = new MyHandler();
 		aboutUs.addClickHandler(handler);
 		
 	}
 	
 	private void parseXml(String xml){
 		try {
 		    // parse the XML document into a DOM
 		    Document messageDom = XMLParser.parse(xml);
 
 		    //Get the publication date
 		    Text bodyNode = (Text)messageDom.getElementsByTagName("pubDate").item(0).getFirstChild();
 		    String body = bodyNode.getData();
 		    
 		    String year = body.substring(0, 4);
 		    String month = body.substring(4, 6);
 		    String day = body.substring(6, 8);
 		    
 		    dueDateField.setText(day + "." + month + "." + year);
 		   
 		    Node poltilbudNode = messageDom.getElementsByTagName("poltilbud").item(0);
 	
 		    //get the product
 		    NodeList nodeList = poltilbudNode.getChildNodes();
 		    for(int i=0; i<nodeList.getLength(); i++){
 		    	Node childNode = nodeList.item(i);
 		    	  if (childNode.getNodeName().equals("product")) {
 		    		  String name = childNode.getOwnerDocument().getElementsByTagName("name").item(i-1).getFirstChild().getNodeValue();
		    		  String url = childNode.getOwnerDocument().getElementsByTagName("url").item(i-1).getFirstChild().getNodeValue();
		    		  flexTable.setValue(i, 0, new Anchor(name, url).toString());
 		    		  String type = childNode.getOwnerDocument().getElementsByTagName("type").item(i-1).getFirstChild().getNodeValue();
 		    		  flexTable.setValue(i, 1, type);
 		    		  String oldPrice = childNode.getOwnerDocument().getElementsByTagName("oldPrice").item(i-1).getFirstChild().getNodeValue();
 		    		  Double oldPriceDubl = Double.parseDouble(oldPrice);
 		    		  flexTable.setValue(i, 2, oldPriceDubl);
 		    		  String newPrice = childNode.getOwnerDocument().getElementsByTagName("newPrice").item(i-1).getFirstChild().getNodeValue();
 		    		  Double newPriceDubl = Double.parseDouble(newPrice);
 		    		  flexTable.setValue(i, 3, newPriceDubl);	    		
 		    		  String differance = childNode.getOwnerDocument().getElementsByTagName("differance").item(i-1).getFirstChild().getNodeValue();
 		    		  Double differanceDubl = Double.parseDouble(differance);
 		    		  flexTable.setValue(i, 4, differanceDubl);
 		    		  String differancePerc = childNode.getOwnerDocument().getElementsByTagName("differancePerc").item(i-1).getFirstChild().getNodeValue();
 		    		  Double differancePercDubl = Double.parseDouble(differancePerc);
		    		  flexTable.setValue(i, 5, differancePercDubl);		    				    		  
 		    	  }
 
 		    }
 		    
 		    
 		  } catch (DOMException e) {
 		    Window.alert("Could not parse XML document.");
 		  }
 
 	}
 	
 	private void requestFailed(Throwable exception) {
 		Window.alert("Failed to send the message: "
 				+ exception.getMessage());
 	}
 }
