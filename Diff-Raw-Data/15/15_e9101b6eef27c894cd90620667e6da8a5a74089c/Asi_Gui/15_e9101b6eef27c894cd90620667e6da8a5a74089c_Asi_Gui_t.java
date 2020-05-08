 package asi.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.InlineHTML;
 import com.google.gwt.user.client.ui.InlineLabel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class Asi_Gui {
 
 	//Buttons
 	public Button calculateButton;
 	public Button costNextButton;
 	public Button panelsNextButton;
 	
 	//Textboxes
 	public TextBox powerConsumption;
 	public TextBox feedInTariff;
 	public TextBox tariffRates;
 	public TextBox hoursOfSun;
 	public TextBox panelPower;
 	public TextBox nPanels;
 	public TextBox panelCost;
 	public TextBox installCost;
 	public TextBox panelWattage;
 	public TextBox panelDegradation;
 	public TextBox elecCost;
 	
 	//Listboxes
 	public ListBox panelAngle;
 	public ListBox panelDirection;
 	
 	//TabPanel
 	
 	public TabPanel tabPanel;
 	
 	//VerticalPanels
 	
 	public VerticalPanel costPanel;
 	public VerticalPanel panelPanel;
 	public VerticalPanel powerPanel;
 	public VerticalPanel resultsPanel;
 	
     //Labels
     
 	public InlineLabel powerConsumptionLabel;
 	public InlineLabel tariffRatesLabel;
 	public InlineLabel feedInTariffLabel;
 	public InlineLabel hoursOfSunLabel;
 	public InlineLabel panelPowerLabel;
 	public InlineLabel nPanelsLabel;
 	public InlineLabel panelCostLabel;
 	public InlineLabel installCostLabel;
 	public InlineLabel panelAngleLabel;
 	public InlineLabel panelDirectionLabel;
 	public InlineLabel panelWattageLabel;
 	public InlineLabel panelDegradationLabel;
 	public InlineLabel elecCostLabel;
	public InlineLabel resultsLabel;
     
     //HTML
 	public InlineHTML space; 
 	public HTML homeText;
     
     //Array
     
 	public String[] directions;
 
 	public Asi_Gui()
 	{
 		
 		 // Create a tab panel
 	    tabPanel = new TabPanel();
 	    tabPanel.setWidth("100%");
 	    
 	    
 	    // Panels for tabPanel
 	    
 	    costPanel = new VerticalPanel();
 	    panelPanel = new VerticalPanel();
 	    powerPanel = new VerticalPanel();
	    resultsPanel = new VerticalPanel();
 	    
 
        
 	    //Labels
 	    
 	    powerConsumptionLabel = new InlineLabel("Enter Power Consumption:");
 	    tariffRatesLabel = new InlineLabel("Enter Tariff Rates:");
 	    feedInTariffLabel = new InlineLabel("Enter Feed-In Tariff Rates:");
 	    hoursOfSunLabel = new InlineLabel("Enter the average hours of sunlight per day");
 	    panelPowerLabel = new InlineLabel("Enter the power rating of each panel");
 	    nPanelsLabel = new InlineLabel("Enter the number of panels");
 	    panelCostLabel = new InlineLabel("Enter the cost of each panel");
 	    installCostLabel = new InlineLabel("Enter the cost of installation");
 	    panelAngleLabel = new InlineLabel("Select angle of solar panels");
 	    panelDirectionLabel = new InlineLabel("Select direction of solar panels");
 	    panelWattageLabel = new InlineLabel("Enter your panel wattage");
 	    panelDegradationLabel = new InlineLabel("Enter your panel degradation(%/year)");
 	    elecCostLabel = new InlineLabel("Enter the cost of your electricity ($ per kw/h)");
	    resultsLabel = new InlineLabel("Results here");
 	    
 	    	    
 	    //TextBoxes
 	    
 	     powerConsumption = new TextBox();
 	     tariffRates = new TextBox();
 	     feedInTariff = new TextBox();
 	     hoursOfSun = new TextBox();
 	 	 panelPower = new TextBox();
 		 nPanels= new TextBox();
 		 panelCost= new TextBox();
 		 installCost = new TextBox();
 		 panelWattage = new TextBox();
 		 panelDegradation = new TextBox();
 		 elecCost = new TextBox();
 		 
 		 //Listboxes
 		 
 		 panelAngle = new ListBox();
 		 panelDirection = new ListBox();
 			 
 		 //Add values to panel Angle
 		 
 		 for (int degrees = 5; degrees <= 180; degrees += 5)
 		 {
 			 panelAngle.addItem(Integer.toString(degrees) + " degrees");
 		 }
 		 
 		 //Add values to panel direction
 		 
 		 
 		 directions = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "SW"};
 		 
 		 for (int i = 0; i < directions.length; i++)
 		 {
 			 panelDirection.addItem(directions[i]);
 		 }
 		 
 	    
 	    //Buttons
 		 
 	    calculateButton = new Button("Calculate");
 	    costNextButton = new Button("Next");
 	    panelsNextButton = new Button("Next");
 	    
 	    //HTML ELEMENTS
 	    
 	    homeText = new HTML("Hi and welcome to the Solar Calulcator" +
 	    		"developer by Agilis Sol Industria to help you choose the right solar panel for you!");
  
 	    space = new InlineHTML("<br/>");
 	    
 	}
 	
 	//Method for adding Textboxes to panel
 	
 	//THIS SEEMS TO BE AT FAULT IN CAUSING ERRORS>
 	public void addToPanel(VerticalPanel vPanel, TextBox textbox, InlineLabel label)
 	{
 		vPanel.add(label);
 		vPanel.add(textbox);
 		vPanel.add(space);
 	
 	}
 	
 	//Overloaded method for adding listboxes to panel
 	
 	public void addToPanel(VerticalPanel vPanel, ListBox listbox, InlineLabel label)
 	{	
 		vPanel.add(label);
 		vPanel.add(listbox);
 		vPanel.add(space);
 	
 	}
 }
