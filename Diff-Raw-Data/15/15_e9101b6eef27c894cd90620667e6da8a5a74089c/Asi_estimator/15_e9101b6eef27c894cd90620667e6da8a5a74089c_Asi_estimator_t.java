 package asi.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.XMLParser;
 import asi.client.Asi_Gui;
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Asi_estimator implements EntryPoint {
 
 	public Asi_Gui webGui;
 	
 	public void onModuleLoad() 
 	{		
 		webGui = new Asi_Gui();
 		
 		
 	    webGui.addToPanel(webGui.costPanel, webGui.panelCost, webGui.panelCostLabel);
 	    webGui.addToPanel(webGui.costPanel, webGui.installCost, webGui.installCostLabel);  
 	    webGui.costPanel.add(webGui.costNextButton);
 	    //Add to panelPanel
 	    
 	    webGui.addToPanel(webGui.panelPanel, webGui.nPanels, webGui.nPanelsLabel);
 	    webGui.addToPanel(webGui.panelPanel, webGui.panelAngle, webGui.panelAngleLabel);
 	    webGui.addToPanel(webGui.panelPanel, webGui.panelDirection, webGui.panelDirectionLabel);
 	    webGui.addToPanel(webGui.panelPanel, webGui.panelWattage, webGui.panelWattageLabel);
 	    webGui.addToPanel(webGui.panelPanel, webGui.panelDegradation,  webGui.panelDegradationLabel);
 	    webGui.addToPanel(webGui.panelPanel, webGui.hoursOfSun, webGui.hoursOfSunLabel);
 	    webGui.panelPanel.add(webGui.panelsNextButton);
 
 	    
 	    //Add to powerPanel
 	    
 	    webGui.addToPanel(webGui.powerPanel, webGui.powerConsumption, webGui.powerConsumptionLabel);
 	    webGui.addToPanel(webGui.powerPanel, webGui.panelPower, webGui.panelPowerLabel);
 	    webGui.addToPanel(webGui.powerPanel, webGui.tariffRates, webGui.tariffRatesLabel);
 	    webGui.addToPanel(webGui.powerPanel, webGui.feedInTariff, webGui.feedInTariffLabel);
 	    webGui.addToPanel(webGui.powerPanel, webGui.elecCost,webGui. elecCostLabel);
 	    webGui.powerPanel.add(webGui.calculateButton);
 	    
	    //Add to resultsPanel
	    
	    webGui.resultsPanel.add(webGui.resultsLabel);
 	   	    
 		//Add to tabPanel
 	    
 	    webGui.tabPanel.add(webGui.homeText, "Home");
 	    webGui.tabPanel.add(webGui.costPanel, "Cost");
 	    webGui.tabPanel.add(webGui.panelPanel, "Panels");
 	    webGui.tabPanel.add(webGui.powerPanel, "Power");
	    webGui.tabPanel.add(webGui.resultsPanel, "Results");
 		
 		
 		
 	    
 	    RootPanel.get("interface").add(webGui.tabPanel);
 	    
 	}
 }
 	
 	
