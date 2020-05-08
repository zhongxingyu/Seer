 package com.blackout.solarpanelcalculator.client;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DecoratorPanel;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.DoubleBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.IntegerBox;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 import com.google.gwt.visualization.client.AbstractDataTable;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.VisualizationUtils;
 import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
 import com.google.gwt.visualization.client.visualizations.ColumnChart;
 import com.google.gwt.visualization.client.visualizations.ColumnChart.Options;
 import com.google.gwt.visualization.client.visualizations.LineChart;
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class CalculationClient implements EntryPoint {
 
 	
 	private double[] monthResults = null;
 	private ColumnChart chart;//for monthly results chart
 	private LineChart lineChart;//for payback time line chart
 	private TreeMap<Double,String> payBackTime = null;
 	Label lblmonthsResultstext = new Label();
 	ListBox citycomboBox = new ListBox();	
 	ListBox roofDirectioncomboBox = new ListBox();		
 	DoubleBox systemCostBox = new DoubleBox();		
 	DoubleBox doubleBoxSize = new DoubleBox();	
 	DoubleBox roofLossBox = new DoubleBox();	              
     DoubleBox inverterBox = new DoubleBox();    
     DoubleBox doubleBoxWiring = new DoubleBox();   
     IntegerBox integerBoxLifeSpan = new IntegerBox();    
     DoubleBox doubleBoxPowerCost = new DoubleBox();  
     DoubleBox doubleBoxTarrif = new DoubleBox();    
     DoubleBox doubleBoxReplacePercent = new DoubleBox();	
 	DoubleBox doubleBoxAgeLoss = new DoubleBox();
 	DoubleBox doubleBoxIrradiance = new DoubleBox();
 	private IntegerBox integerBoxpaybackYear = new IntegerBox();
 	private DoubleBox txtWhatYear = new DoubleBox();
 
 	private DoubleBox txtDailySolarGeneration = new DoubleBox();
 
 
 	private Button btnCalculation = new Button();
 	
 	
 	private DoubleBox txtDailySavings = new DoubleBox();
 	
 	
 	
 	private DoubleBox txtPayBackYear = new DoubleBox();
     private DoubleBox txtPowerEstimate = new DoubleBox();
     private IntegerBox txtHouseholdSize = new IntegerBox();
     private RadioButton rdbtnHeavy = new RadioButton("usage", "Heavy");
     private RadioButton rdbtnMedium = new RadioButton("usage", "Medium");
     private RadioButton rdbtnLight = new RadioButton("usage", "Light");    
     
     /* vv Court's WorthInvestment items vv */
    private DoubleBox txtDailySavings2;
    private DoubleBox txtPayBackYear2;
    private DoubleBox txtExpectedDuration;
    private Button btnWorthInvesting;
    private DoubleBox lblWorthInvesting;
     /* ^^ Court's WorthInvestment items ^^ */
     
 	private CalculationServiceAsync service; 
 	
 	public void onModuleLoad() {
 		 RootPanel.get("tdMainPanel").add(loadAllControlsNew());
 		loadAllUIControls();
 		service= (CalculationServiceAsync) GWT.create(CalculationService.class);
 		ServiceDefTarget serviceDef = (ServiceDefTarget) service;
         serviceDef.setServiceEntryPoint(GWT.getModuleBaseURL()
             + "calculationService");
         
         createColumnChart(monthResults);//load months results charts
         createLineChart(payBackTime);
 	}
 
 	
 	private void loadAllUIControls() {
 	
 		txtWhatYear.setText("0");
 	
 		rdbtnMedium.setValue(true);
 		txtHouseholdSize.setValue(0);
 		
 		btnCalculation.setText("Calculate");
 		
 		txtDailySavings.setText("0");
 		txtPayBackYear.setText("");
 		btnCalculation.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				doCalculation();
 			}
 		});
 		RootPanel.get("tdDailySolarGenerationCalculate").add(btnCalculation);
 		RootPanel.get("tdDailySolarGenerationResult").add(txtDailySolarGeneration);		
 		
 		RootPanel.get("tdDailySavingsResult").add(txtDailySavings);		
 		RootPanel.get("tdPowerEstimateResult").add(txtPowerEstimate);		
 		RootPanel.get("tdHouseholdSize").add(txtHouseholdSize);		
 		RootPanel.get("tdUsageType").add(rdbtnHeavy);
 		RootPanel.get("tdUsageType").add(rdbtnMedium);
 		RootPanel.get("tdUsageType").add(rdbtnLight);
 		RootPanel.get("tdPayBackYearResult").add(txtPayBackYear);
 		//RootPanel.get("idMonthTextResults").add( lblmonthsResultstext);
 	}
 	
 	/* Court's WorthInvestment method */
 	private void loadWorthInvesting() {
 		txtDailySavings2.setText("0");
 		txtPayBackYear2.setText("3");
 		txtExpectedDuration.setText("5");
 		btnWorthInvesting.setText("Worth it?");
 		lblWorthInvesting.setText("");
 		
 		btnWorthInvesting.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				getWorthInvestmentFromServer();
 			}
 		});
 		
 		RootPanel.get("tdDailySaved").add(txtDailySavings2);
 		RootPanel.get("tdFinalPayback").add(txtPayBackYear2);
 		RootPanel.get("tdExpectedDuration").add(txtExpectedDuration);
 		RootPanel.get("tdWorthInvestingCalculate").add(btnWorthInvesting);
 		RootPanel.get("tdWorthInvestingResult").add(lblWorthInvesting);
 	}
 	
 	protected void getWorthInvestmentFromServer() {
 		CalculationServiceAsync service = (CalculationServiceAsync) GWT.create(CalculationService.class);
         ServiceDefTarget serviceDef = (ServiceDefTarget) service;
         serviceDef.setServiceEntryPoint(GWT.getModuleBaseURL()
             + "calculationService");
         WorthInvestingCallback callback = new WorthInvestingCallback(lblWorthInvesting);
         service.doWorthInvestment(txtDailySavings2.getValue(), txtPayBackYear2.getValue(), txtExpectedDuration.getValue(), callback);
 	}
 
 	protected void doCalculation(){		
         
         // calculate the generation for all months
         service.doSolarGenerationForAllMonths(doubleBoxSize.getValue()/1000, roofLossBox.getValue()/100, inverterBox.getValue()/100, doubleBoxWiring.getValue()/100, txtWhatYear.getValue(), doubleBoxAgeLoss.getValue()/100, new AsyncCallback<double[]>() {
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());				
 			}
 
 			public void onSuccess(double[] result) {
 				chart.draw(createMonthGenerationTable(result),createOptions());//draw column chart
 				
 				//to remove the string month results display when charts are properly set up
 				 String months[]={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
 				 StringBuffer buffer = new StringBuffer();
 				 for(int i=0;i<12;i++){
 						buffer.append(months[i]);
 						buffer.append(":");
 						buffer.append(result[i]);
 						buffer.append("kwh\n");					
 			}
 				 lblmonthsResultstext.setText(buffer.toString());
 			}
 		});
 
         // calculate the daily generation,  
         service.doDailySolarGeneration(doubleBoxSize.getValue()/1000, roofLossBox.getValue()/100, inverterBox.getValue()/100, doubleBoxWiring.getValue()/100, txtWhatYear.getValue(), doubleBoxAgeLoss.getValue()/100, doubleBoxIrradiance.getValue(), new AsyncCallback<Double>() {
 			public void onFailure(Throwable caught) {				
 				Window.alert(caught.getMessage());				
 			}
 
 			public void onSuccess(Double result) {
 				txtDailySolarGeneration.setValue(result);
 				getDailySaving(result);//use dailyCalculation result for dailySavingCalculation
 				getPaybackTime(result);
 			}});
         
         //calculate power consumption
         service.doPowerConsumption(txtHouseholdSize.getValue(), getUsageSelection(), new AsyncCallback<Double>() {
 			public void onFailure(Throwable caught) {				
 				Window.alert(caught.getMessage());				
 			}
 
 			public void onSuccess(Double result) {
 				txtPowerEstimate.setValue(result);				
 			}});
         
 	}
 	
 	protected void getPaybackTime(Double result) {
 		service.getPayBackTime(systemCostBox.getValue(), integerBoxLifeSpan.getValue(), doubleBoxReplacePercent.getValue()/100, 
 				doubleBoxTarrif.getValue()/100, doubleBoxPowerCost.getValue()/100, result, doubleBoxAgeLoss.getValue()/100,integerBoxpaybackYear.getValue(), new AsyncCallback<TreeMap<Double,String>>(){
 
 					@Override
 					public void onFailure(Throwable caught) {
 						Window.alert(caught.getMessage());	
 						
 					}
 
 					@Override
 					public void onSuccess(TreeMap<Double, String> result) {
 						lineChart.draw(createPayBackTable(result),createLineChartOptions());
 						
 					}
 			
 		});
 		
 	}
 
 
 	private String getUsageSelection() {
 		if (rdbtnHeavy.getValue())
 			return "Heavy";
 		else if(rdbtnLight.getValue())
 			return "Light";
 		else return "Medium";
 		
 	}
 	
 //	Calculate Daily savings
 	private void getDailySaving( double dailyGeneration){
  		   service.doDailySavings(dailyGeneration, doubleBoxReplacePercent.getValue()/100, doubleBoxTarrif.getValue()/100, doubleBoxPowerCost.getValue()/100, new AsyncCallback<Double>() {
 			public void onFailure(Throwable caught) {				
 				Window.alert(caught.getMessage());				
 			}
 
 			public void onSuccess(Double result) {
 				txtDailySavings.setText(result.toString());
 				getPayBackYear(result);// use savings result to calculate pay back year
 			}});			
 
 	}
 //	Calculate Payback year
 	private void getPayBackYear( double dailySaving){
 		service.doPayBackYear(systemCostBox.getValue(), integerBoxLifeSpan.getValue(),
 				dailySaving,new AsyncCallback<Double>() {
 			public void onFailure(Throwable caught) {				
 				Window.alert(caught.getMessage());				
 			}
 
 			public void onSuccess(Double result) {
 				txtPayBackYear.setText(result.toString());
 			}});
 	}
 	private Widget loadAllControlsNew() {
 		
 //		pop up message
 		String irradianceMsg = "This value is auto updated based on your location you can also enter a value to override it";
 		PopMessage irradianceHelpMsg = new PopMessage(irradianceMsg);		
 		String overallCostMsg = "The overall cost is your solar system and installation cost minus any goverment rebates  ";
 		PopMessage overallCostHelpMsg = new PopMessage(overallCostMsg);
 		
 		
 //		set boxes to their default values
 		 systemCostBox.setText("18000");	
 		 doubleBoxSize.setText("4950");	
 		 roofLossBox.setText("88.5"); 
 	     inverterBox.setText("96") ;
 	     doubleBoxWiring.setText("98");   
 	     integerBoxLifeSpan.setText("25");     
 	     doubleBoxPowerCost.setText("19.41");
 	     doubleBoxTarrif.setText("44");  
 	     doubleBoxReplacePercent.setText("24"); 
 		 doubleBoxAgeLoss.setText("0.7"); 
 		 doubleBoxIrradiance.setText("5.1"); 
 		 integerBoxpaybackYear.setValue(25);
 		
 //		create labels
 		Label lblAssumeSolarIrradiance = new Label("Solar irradiance level in your location is(kWh/m2/day):");
 		lblAssumeSolarIrradiance.addMouseOverHandler(irradianceHelpMsg);
 		lblAssumeSolarIrradiance.addMouseOutHandler(irradianceHelpMsg);
 		Label lblAssumeSolarPanel = new Label("Solar panel aging efficiency loss per year is(%):");
 		Label lblAssumeUseAtHome = new Label("The percentage of solar generation used at home is(%):");
 		Label lblTarrif = new Label("Your feed in tarrif is (cents in kwh):");
 		Label lblPowerCost = new Label("Your electricity cost is (cents per kwh):");
 		Label lblLifeSpan = new Label("The Solar panel life span is(years):");
 		Label lblWiring = new Label("The wiring efficiency is(%):");
 		Label inverterLbl = new Label("The inverter efficiency is(%):");
 		Label roofLossLbl = new Label("The efficiency loss due to angles and directions is(%):");
 		Label lblSystemSize = new Label("Your solar system size (watts):");
 		Label systemCostLbl = new Label("Your overall purchase cost(dollars):");
 		systemCostLbl.addMouseOverHandler(overallCostHelpMsg);
 		systemCostLbl.addMouseOutHandler(overallCostHelpMsg);
 		Label roofFaceLbl = new Label("Select your roof direction:");
 		Label cityLbl = new Label("Select your city:");	 
 		
 //		 combobox selections
 		citycomboBox.addItem("Select City....");
 		citycomboBox.addItem("Sydney (NSW)");
 		citycomboBox.addItem("Melbourne (VIC)");
 		citycomboBox.addItem("Brisbane (QLD)");
 		citycomboBox.addItem("Perth (WA)");
 		citycomboBox.addItem("Adelaide (SA)");
 		citycomboBox.addItem("Gold Coast \u2013 Tweed (QLD/NSW)");
 		citycomboBox.addItem("Newcastle (NSW)");
 		citycomboBox.addItem("Canberra \u2013 Queanbeyan (ACT/NSW)");
 		citycomboBox.addItem("Wollongong (NSW)");
 		citycomboBox.addItem("Sunshine Coast (QLD)");
 		citycomboBox.addItem("Hobart (TAS)");
 		citycomboBox.addItem("Geelong (VIC)");
 		citycomboBox.addItem("Townsville (QLD)");
 		citycomboBox.addItem("Cairns (QLD)");
 		citycomboBox.addItem("Toowoomba (QLD)");
 		citycomboBox.addItem("Darwin (NT)");
 		citycomboBox.addItem("Albury \u2013 Wodonga (NSW/VIC)");
 		citycomboBox.addItem("Launceston (TAS)");
 		citycomboBox.addItem("Ballarat (VIC)");
 		citycomboBox.addItem("Bendigo (VIC)");
 		citycomboBox.addItem("Mandurah (WA)");
 		citycomboBox.addItem("Mackay (QLD)");
 		citycomboBox.addItem("Burnie \u2013 Devonport (TAS)");
 		citycomboBox.addItem("Latrobe Valley (VIC)");
 		citycomboBox.addItem("Rockhampton (QLD)");
 		citycomboBox.addItem("Bunbury (WA)");
 		citycomboBox.addItem("Bundaberg (QLD)");
 		citycomboBox.addItem("Hervey Bay (QLD)");
 		citycomboBox.addItem("Wagga Wagga (NSW)");
 		citycomboBox.addItem("Coffs Habour (NSW)");
 		citycomboBox.addItem("Gladstone (QLD)");
 		citycomboBox.addItem("Mildura (VIC)");
 		citycomboBox.addItem("Shepparton (VIC)");
 	
 //	root combobox selections
 		roofDirectioncomboBox.addItem("Select panel direction...");
 		roofDirectioncomboBox.addItem("Facing directly South");
 		roofDirectioncomboBox.addItem("South South West(22.5 degrees from South)");
 		roofDirectioncomboBox.addItem("South West(45 degrees from South)");
 		roofDirectioncomboBox.addItem("West South West(67.5 degrees from South)");
 		roofDirectioncomboBox.addItem("Facing directly West");
 		roofDirectioncomboBox.addItem("South South East(22.5 degrees from South)");
 		roofDirectioncomboBox.addItem("South East(45 degrees from South)");
 		roofDirectioncomboBox.addItem("East South East(67.5 degrees from South)");
 		roofDirectioncomboBox.addItem("Facing directly East");
 		
 	
 		doubleBoxSize.setWidth("106px");
 	    // Create a table to layout the form options
 	    FlexTable layout = new FlexTable();
 	    layout.setCellSpacing(2);
 	    layout.setWidth("364px");
 
 //	    add widgets to flextable
 	    FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
 	    layout.setWidget(1, 0, cityLbl);
 	    layout.setWidget(1, 1,citycomboBox);
 	    citycomboBox.setWidth("122px");
 	    layout.setWidget(2, 0, roofFaceLbl);
 	    layout.setWidget(2, 1, roofDirectioncomboBox);
 	    roofDirectioncomboBox.setWidth("123px");
 	    layout.setWidget(3, 0, lblSystemSize);
 	    layout.setWidget(3, 1, doubleBoxSize);
 	    systemCostBox.setWidth("106px");
 	    layout.setWidget(4, 0, systemCostLbl);
 	    layout.setWidget(4, 1, systemCostBox);
 	    
 //	   set styles to labels and boxes
 	    roofLossLbl.setStyleName("gwt-Label-assumptions"); 	   
 	    roofLossBox.setStyleName("gwt-DoubleBox-assumptions");   
 	    inverterLbl.setStyleName("gwt-Label-assumptions");	
 	    inverterBox.setStyleName("gwt-DoubleBox-assumptions");		
 		lblWiring.setStyleName("gwt-Label-assumptions");			
 		doubleBoxWiring.setStyleName("gwt-DoubleBox-assumptions");	
 		lblLifeSpan.setStyleName("gwt-Label-assumptions");		
 		integerBoxLifeSpan.setStyleName("gwt-DoubleBox-assumptions");		
 		lblPowerCost.setStyleName("gwt-Label-assumptions");
 		doubleBoxPowerCost.setStyleName("gwt-DoubleBox-assumptions");		
 		lblTarrif.setStyleName("gwt-Label-assumptions");
 		doubleBoxTarrif.setStyleName("gwt-DoubleBox-assumptions");		
 		lblAssumeUseAtHome.setStyleName("gwt-Label-assumptions");
 		doubleBoxReplacePercent.setStyleName("gwt-DoubleBox-assumptions");		
 		lblAssumeSolarPanel.setStyleName("gwt-Label-assumptions");	
 		doubleBoxAgeLoss.setStyleName("gwt-DoubleBox-assumptions");		
 		doubleBoxIrradiance.setStyleName("gwt-DoubleBox-assumptions");		
 		lblAssumeSolarIrradiance.setStyleName("gwt-Label-assumptions");
 	    
 //		all the horizontalPanels host each label and doublebox in disclosure panel
 	    HorizontalPanel horizontalPanel = new HorizontalPanel();
 	    HorizontalPanel horizontalPanel1 = new HorizontalPanel();
 	    HorizontalPanel horizontalPanel2 = new HorizontalPanel();
 	    HorizontalPanel horizontalPanel3 = new HorizontalPanel();	    
 	    HorizontalPanel horizontalPanel4 = new HorizontalPanel();
 	    HorizontalPanel horizontalPanel5 = new HorizontalPanel();
 	    HorizontalPanel horizontalPanel6 = new HorizontalPanel();
 	    HorizontalPanel horizontalPanel7 = new HorizontalPanel();
 	    HorizontalPanel horizontalPanel8 = new HorizontalPanel();	    
 	    horizontalPanel.add(roofLossLbl);
 	    horizontalPanel.add(roofLossBox);
 	    horizontalPanel1.add(inverterLbl);
 	    horizontalPanel1.add(inverterBox);	    
 	    horizontalPanel2.add(lblWiring);
 	    horizontalPanel2.add(doubleBoxWiring);
 	    horizontalPanel3.add(lblLifeSpan);
 	    horizontalPanel3.add(integerBoxLifeSpan);
 	    horizontalPanel4.add(lblPowerCost);
 	    horizontalPanel4.add(doubleBoxPowerCost);
 	    horizontalPanel5.add(lblTarrif);
 	    horizontalPanel5.add(doubleBoxTarrif);
 	    horizontalPanel6.add(lblAssumeUseAtHome);
 	    horizontalPanel6.add(doubleBoxReplacePercent);
 	    horizontalPanel7.add(lblAssumeSolarPanel);
 	    horizontalPanel7.add(doubleBoxAgeLoss);
 	    horizontalPanel8.add(lblAssumeSolarIrradiance);
 	    horizontalPanel8.add(doubleBoxIrradiance);    
 	    VerticalPanel verticalPanel = new VerticalPanel();
 	    verticalPanel.add(horizontalPanel);
 	    verticalPanel.add(horizontalPanel1);
 	    verticalPanel.add(horizontalPanel2);
 	    verticalPanel.add(horizontalPanel3);
 	    verticalPanel.add(horizontalPanel4);
 	    verticalPanel.add(horizontalPanel5);
 	    verticalPanel.add(horizontalPanel6);
 	    verticalPanel.add(horizontalPanel7);
 	    verticalPanel.add(horizontalPanel8);    
 	    
 	    // Add advanced options to form in a disclosure panel
 	    DisclosurePanel advancedDisclosure = new DisclosurePanel(
 	        "Our assumptions");
 	    advancedDisclosure.setContent(verticalPanel);
 	    advancedDisclosure.setOpen(true);
 	    advancedDisclosure.setAnimationEnabled(true);   
 	    layout.setWidget(5, 0, advancedDisclosure);
 	    advancedDisclosure.setWidth("358px");
 	    cellFormatter.setColSpan(5, 0, 2);
 	 // Wrap the content in a DecoratorPanel
 	    DecoratorPanel decPanel = new DecoratorPanel();
 	    decPanel.setWidget(layout);
 	    return decPanel;
 	  }
 	
 	/*use to add pop msg to explain stuff*/
 	class PopMessage implements MouseOverHandler,MouseOutHandler {
 		// Create a basic popup widget
 		 PopupPanel simplePopup ;
 		public PopMessage(String whatMsg){
 			simplePopup = new PopupPanel(true);
 			simplePopup.addStyleName("gwt-PopupPanel");
 		    simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
 		    simplePopup.setWidth("150px");
 		    simplePopup.setWidget(
 		        new HTML(whatMsg));
 	}
 		@Override
 		public void onMouseOver(MouseOverEvent event) {
 			// Reposition the popup relative to the button
 			Widget source = (Widget) event.getSource();
             int left = source.getAbsoluteLeft() + 10;
             int top = source.getAbsoluteTop() +20;
             simplePopup.setPopupPosition(left, top);
             // Show the popup
             simplePopup.show();			
 		}	
 		@Override
 		public void onMouseOut(MouseOutEvent event) {
 			// hide it 
 			simplePopup.hide();			
 		}		
 	}
 	
 	
 	
 	/* use gwt char api to create column chart*/
 	 void createColumnChart(final double monthResults[]){
 	        Runnable onLoadCallback = new Runnable() {
 	          public void run() {
 	            Panel panel = RootPanel.get("idMonthSolarGenerationResults");
 	     
 	            // Create a column chart visualization.	           
 	            chart = new ColumnChart(createMonthGenerationTable(monthResults), createOptions());
 	           //to do chart.addSelectHandler(createSelectHandler(chart));	           
 	            panel.add(chart);
 	            
 	          }
 	        };
 
 	        // Load the visualization api, passing the onLoadCallback to be called
 	        // when loading is done.
 	        VisualizationUtils.loadVisualizationApi(onLoadCallback, ColumnChart.PACKAGE);
 	        }
 	       
 	 /*create line chart for payback time*/
 	 private void createLineChart(final TreeMap<Double,String> paybackResults){
 		 Runnable onLoadCallback = new Runnable() {
 	          public void run() {
 	            Panel panel = RootPanel.get("idPayBackYearLineGraph");
 	     
 	            // Create a column chart visualization.	           
 	            lineChart = new LineChart(createPayBackTable(paybackResults),createLineChartOptions());
 	           //to do chart.addSelectHandler(createSelectHandler(chart));
 	           
 	            panel.add(lineChart);
 	            
 	          }
 	        };
 
 	        // Load the visualization api, passing the onLoadCallback to be called
 	        // when loading is done.
 	        VisualizationUtils.loadVisualizationApi(onLoadCallback, LineChart.PACKAGE);
 	 }
 	 
 	protected com.google.gwt.visualization.client.visualizations.LineChart.Options createLineChartOptions() {
 		 com.google.gwt.visualization.client.visualizations.LineChart.Options options = LineChart.Options.create();
 		    options.setWidth(700);
 		    options.setHeight(400);		    
 		    options.setTitle("Payback years");		    
 		    options.setMin(systemCostBox.getValue()*-1);
 		    
 		return options;
 	}
 
 
 	protected AbstractDataTable createPayBackTable(
 			TreeMap<Double, String> paybackResults) {
 		 DataTable data = DataTable.create();
 		 data.addColumn(ColumnType.STRING,"Time");
 		 data.addColumn(ColumnType.NUMBER, "money");
 		 if(paybackResults==null){
 			 data.addRows(integerBoxpaybackYear.getValue());
 			for(int i = 0; i<integerBoxpaybackYear.getValue();i++){
 				data.setValue(i, 0, Integer.toString(i));
 				data.setValue(i, 1, 0);
 				
 			}
 		 }
 		 else{
 		 data.addRows(paybackResults.size());
 		 
 			 int i = 0;
 			for(Entry<Double, String> entry :paybackResults.entrySet()){
 				
 				data.setValue(i, 0, entry.getValue());
 				data.setValue(i, 1, entry.getKey());
 				i++;
 			}
 
 		 }
 		return data;
 	}
 
 
 	//use to populate month generation table for Column chart	
 	private AbstractDataTable createMonthGenerationTable(double monthResults[]) {
 	    DataTable data = DataTable.create();
 	    data.addColumn(ColumnType.STRING, "Month");
 	    data.addColumn(ColumnType.NUMBER, "kwh");
 	    String months[]={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
 	    
 	    data.addRows(months.length);
 	    
 	    for(int i = 0; i<months.length;i++){
 	    	data.setValue(i, 0, months[i].toString());
 	    	if(monthResults==null)
 	    		data.setValue(i, 1, 0);
 	    	else
 	    	data.setValue(i, 1, monthResults[i]);
 	    }
 	   
 	    return data;
 
 }
 	//column create options
 	private Options createOptions() {
 	    Options options = ColumnChart.Options.create();
 	    options.setWidth(700);
 	    options.setHeight(240);
 	    options.set3D(true);
 	    options.setTitle("kwh Generation per month");
 	    options.setStacked(true);
 	    options.setMin(0);
 	    options.setEnableTooltip(true);
 	    
 	    return options;
 	  }
 }
