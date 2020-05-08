 package au.edu.qut.inn372.greenhat.bean;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 
 import org.primefaces.model.chart.CartesianChartModel;
 import org.primefaces.model.chart.ChartSeries;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.representation.Form;
 
 import au.edu.qut.inn372.greenhat.controller.Chart;
 /**
  * Bean that represents a Calculator
  * 
  * @author Charleston Telles
  * @version 0.2
  */
 @ManagedBean
 @SessionScoped
 public class Calculator implements Serializable {
 	private static final long serialVersionUID = 8267206547724271616L;
 	
 	@ManagedProperty (value = "#{equipment}")
 	private Equipment equipment;
 	
 	@ManagedProperty (value = "#{customer}")
 	private Customer customer;
 	
 
 	private Calculation [] calculations  =  new Calculation[25];
 	
 	@ManagedProperty (value = "#{chart}")
 	private Chart chart;
 	
 	/**
 	 * Calculator's Name.
 	 * For example sellers can name a calculator using customer name, e.g. Calc_John
 	 * Customer can use names to remember the calculator, e.g. Calc01, MyCalc, etc
 	 */
 	private String name;
 	/**
 	 * Unique identifier used by Google data store
 	 */
 	private String key;
 	/**
 	 * Datetime of the latest calculator's update
 	 */
 	private Date datetime;
 	/**
 	 * Calculator status.
 	 * 0 = incomplete
 	 * 1 = complete
 	 * 2 = template
 	 */
 	private int status = 0; //0=incomplete, 1=complete, 2=template
 	DecimalFormat df = new DecimalFormat("#.###");
 	/**
 	 * Return the Calculator status name
 	 * 0 = incomplete
 	 * 1 = complete
 	 * 2 = template
 	 * @return
 	 */
 	public String getStatusName(){
 		switch (this.status) {
 		case 0:
 			return "Incomplete";
 		case 1:
 			return "Complete";
 		case 2:
 			return "Template";
 		default:
 			return "unknow";
 		}
 	}
 	/**
 	 * Formats the date time to Brisbane TimeZone
 	 * 
 	 * TODO: date format should be based on customer location
 	 * 
 	 * @return
 	 */
 	public String getFormatedDateTime(){
 		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
 		try {
 			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+10:00"));//Brisbane TimeZone
 			return dateFormat.format(this.datetime);			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "unknow";
 		}
 	}
 	
 	/**
 	 * Get the customer
 	 * @return customer value of the customer property
 	 */
 	public Customer getCustomer() {
 		return customer;
 	}
 
 	/**
 	 * Set the customer
 	 * @param customer value for the customer property
 	 */
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 
 	/**
 	 * Set the equipment
 	 * @param equipment value for the equipment property
 	 */
 	public void setEquipment(Equipment equipment) {
 		this.equipment = equipment;
 	}
 	
 	/**
 	 * Get the equipment
 	 * @return equipment value of the equipment property
 	 */
 	public Equipment getEquipment() {
 		return equipment;
 	}
 
 	/**
 	 * Get the calculations for a specified range of years
 	 * @return the calculations
 	 */
 	public Calculation[] getCalculations() {
 		return calculations;
 	}
 	/**
 	 * Sets the calculations 
 	 * @param calculations
 	 */
 	public void setCalculations(Calculation[] calculations) {
 		this.calculations = calculations;
 	}
 
 	/**
 	 * Gets the calculator's name
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 	/**
 	 * Sets the calculator's name
 	 * @param name
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	/**
 	 * Gets the calculator's database key
 	 * @return database key
 	 */
 	public String getKey() {
 		return key;
 	}
 	/**
 	 * Sets the calculator's database key
 	 * @param key
 	 */
 	public void setKey(String key) {
 		this.key = key;
 	}
 	/**
 	 * Gets the latest calculator's datetime
 	 * @return latest datetime
 	 */
 	public Date getDatetime() {
 		return datetime;
 	}
 	/**
 	 * Sets the latest calculator's datetime
 	 * @param datetime
 	 */
 	public void setDatetime(Date datetime) {
 		this.datetime = datetime;
 	}
 	/**
 	 * Gets the calculator's status
 	 * @return status
 	 */
 	public int getStatus() {
 		return status;
 	}
 	/**
 	 * Sets the calculator's status
 	 * @param status
 	 */
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	/**
 	 * Set the calculations for a specified range of years
 	 * @param calculations the calculations to set
 	 */
 	public void performCalculations() {
 		Tariff tariff = customer.getTariff();
 		Location location = customer.getLocation();
 		Roof roof = location.getRoof();
 		Bank[] banks = roof.getBanks();
 		
 		double cumulativeSaving = 0.0;
 		double replacementGeneration = 0.0;
 		
 		calculateBankPowerOutput(banks,0);
 		calculateBankPowerOutput(banks,1);
 
 		calculateDayLightElectricityUsage();
 		
 		
 		for(int i=0; i<25; i++){
 			double panelEfficiency = calculatePanelEfficiency(i);
 			double bank1Efficiency = calculateBankEfficiency(banks, panelEfficiency, 0);
 			double bank2Efficiency = calculateBankEfficiency(banks, panelEfficiency, 1);
 			double bank1DailySolarPower = calculateBankDailySolarPower(location, banks, bank1Efficiency, 0);
 			double bank2DailySolarPower = calculateBankDailySolarPower(location, banks, bank2Efficiency, 1);	
 			double dailySolarPower = calculateDailySolarPower(bank1DailySolarPower, bank2DailySolarPower);
 			double annualSolarPower = Math.round( (dailySolarPower *365)*1000.0)/1000.0;
 			double tariff11Fee = calculateTariff11Fee(tariff, i);
 			replacementGeneration = calculateReplacementGeneration(dailySolarPower);
 			double exportedGeneration = calculateExportedGeneration(replacementGeneration, dailySolarPower);
 			double moneySaved = calculateMoneySaved(replacementGeneration, tariff11Fee);
 			double moneyEarned = calculateMoneyEarned(tariff, exportedGeneration);
 			double dailySaving = calculateDailySaving(moneySaved, moneyEarned);
 			double annualSaving = calculateAnnualSaving(dailySaving);
 			cumulativeSaving = Math.round( (cumulativeSaving + annualSaving) *100.0)/100.0;
 			double returnOnInvestment = calculateReturnOnInvestment(cumulativeSaving);
 			
 			this.calculations[i] = new Calculation(i+2012, panelEfficiency, bank1Efficiency, 
 					bank2Efficiency, bank1DailySolarPower, bank2DailySolarPower,
 					tariff11Fee, dailySolarPower, annualSolarPower, replacementGeneration, exportedGeneration, 
 					moneySaved, moneyEarned, dailySaving, annualSaving, cumulativeSaving, returnOnInvestment);
 		}
 		calculatePaybackPeriod();
 	}
 
 	/**
 	 * Calculate return on investment
 	 * @param cumulativeSaving
 	 * @return return on investment
 	 */
 	public double calculateReturnOnInvestment(double cumulativeSaving) {
 		double returnOnInvestment = Math.round( (cumulativeSaving/equipment.getCost()) *100.0)/100.0;
 		return returnOnInvestment;
 	}
 
 	/**
 	 * Calculate annual saving
 	 * @param dailySaving
 	 * @return annual saving
 	 */
 	public double calculateAnnualSaving(double dailySaving) {
 		double annualSaving = Math.round( (dailySaving * 365) *100.0)/100.0;
 		return annualSaving;
 	}
 
 	/**
 	 * Calculate daily saving
 	 * @param moneySaved
 	 * @param moneyEarned
 	 * @return daily saving
 	 */
 	public double calculateDailySaving(double moneySaved, double moneyEarned) {
 		double dailySaving = Math.round( (moneySaved + moneyEarned) * 100.0)/100.0;
 		return dailySaving;
 	}
 
 	/**
 	 * Calculate money earned from exporting solar generation (excess energy produced)
 	 * @param tariff
 	 * @param exportedGeneration
 	 * @return money earned
 	 */
 	public double calculateMoneyEarned(Tariff tariff, double exportedGeneration) {
 		double moneyEarned = Math.round( (exportedGeneration * tariff.getFeedInFee()) * 100.0)/100.0;
 		return moneyEarned;
 	}
 
 	/**
 	 * Money saved from replacement solar generation (energy saved)
 	 * @param replacementGeneration
 	 * @param tariff11Fee
 	 * @return money saved
 	 */
 	public double calculateMoneySaved(double replacementGeneration, double tariff11Fee) {
 		double moneySaved = Math.round( (replacementGeneration * tariff11Fee) * 100.0)/100.0;
 		return moneySaved;
 	}
 
 	/**
 	 * Calculate exported solar generation (excess energy produced)
 	 * @param replacementGeneration
 	 * @param dailySolarPower
 	 * @return exported solar generation
 	 * @throws NumberFormatException
 	 */
 	public double calculateExportedGeneration(double replacementGeneration, double dailySolarPower)
 			throws NumberFormatException {
 		double exportedGeneration = Math.round( (dailySolarPower - replacementGeneration) *1000.0)/1000.0;
 		return exportedGeneration;
 	}
 
 	/**
 	 * Calculate replacement solar generation (energy saved)
 	 * @param dailySolarPower
 	 * @return replacement solar generation
 	 * @throws NumberFormatException
 	 */
 	public double calculateReplacementGeneration(double dailySolarPower) throws NumberFormatException {
 		double replacementGeneration;
 		if(customer.getElectricityUsage().getDayLightElectricityUsage() > dailySolarPower){
 			replacementGeneration = Math.round (dailySolarPower *1000.0)/1000.0;
 		}
 		else {
 			replacementGeneration = Math.round (customer.getElectricityUsage().getDayLightElectricityUsage() *1000.0)/1000.0;
 		}
 		return replacementGeneration;
 	}
 
 	/**
 	 * Calculate tariff 11 fee for given year
 	 * @param tariff
 	 * @param i
 	 * @return tariff fee
 	 */
 	public double calculateTariff11Fee(Tariff tariff, int i) {
 		double tariff11Fee = Math.round( (tariff.getTariff11Fee() * ( 
 								Math.pow( (1+tariff.getAnnualTariffIncrease()/100.0), ((i+1)-1) ) ) ) * 10000.0)/10000.0 ;
 		return tariff11Fee;
 	}
 
 	/**
 	 * Calculate daily solar power
 	 * @param bank1DailySolarPower
 	 * @param bank2DailySolarPower
 	 * @return total daily solar power generation
 	 * @throws NumberFormatException
 	 */
 	public double calculateDailySolarPower(double bank1DailySolarPower, double bank2DailySolarPower) 
 			throws NumberFormatException {
 		double dailySolarPower = Math.round ( ((bank1DailySolarPower + bank2DailySolarPower) 
 				* equipment.getInverter().getEfficiency()/100.0 )*1000.0)/1000.0;
 		return dailySolarPower;
 	}
 
 	/**
 	 * Calculate a bank's daily solar power
 	 * @param location
 	 * @param banks
 	 * @param bank1Efficiency
 	 * @return daily solar power generated by a bank
 	 * @throws NumberFormatException
 	 */
 	public double calculateBankDailySolarPower(Location location, Bank[] banks, double bank1Efficiency, int i) throws NumberFormatException {
 		double bank1DailySolarPower = Math.round( (banks[i].getPowerOutput() * (bank1Efficiency/100)
 				* location.getSunLightHours())*1000.0)/1000.0;
 		return bank1DailySolarPower;
 	}
 
 	/**
 	 * Calculate a bank's efficiency
 	 * @param banks
 	 * @param panelEfficiency
 	 * @return bank's efficiency
 	 */
 	public double calculateBankEfficiency(Bank[] banks, double panelEfficiency, int i) {
 		double bank1Efficiency = Math.round( panelEfficiency * (1-banks[i].getOrientationEfficiencyLoss()/100)
 				* (1-banks[i].getAngleEfficiencyLoss()/100) *100.0) /100.0;
 		return bank1Efficiency;
 	}
 
 	/**
 	 * Calculate panel efficiency
 	 * @param i
 	 * @return panel efficiency
 	 */
 	public double calculatePanelEfficiency(int i) {
 		double panelEfficiency = 0;
 		try {
 			panelEfficiency = Math.round( (100 - (equipment.getPanels().get(0).getEfficiencyLoss() * (i-1+1))) * 100.0) /100.0;
 		} catch (Exception e){}
 		return panelEfficiency;
 
 	}
 
 	/**
 	 * Calculate the payback period
 	 * @param paybackPeriod the paybackPeriod to set
 	 */
 	public void calculatePaybackPeriod() {
 		int paybackPeriod = 1;
 		for(int i=0; i<25; i++){
 			if(calculations[i].getReturnOnInvestment() < 1){ 
 				paybackPeriod++;
 			}
 		}
 		calculations[0].setPaybackPeriod(paybackPeriod);
 	}
 
 	/**
 	 * Calculate a bank's power output
 	 * @param bank
 	 * @param numOfPanels
 	 */
 	public void calculateBankPowerOutput(Bank [] banks, int i){
 		try{
 			//bank.setPowerOutput((bank.getNumberOfPanels() * equipment.getPanels().get(0).getPowerRating())/ 1000);
			banks[i].setPowerOutput((banks[i].getNumberOfPanels() * equipment.getPanels().get(0).getPowerRating())/ 1000);
 		} catch (Exception e){
 		}
 	}
 	
 	/**
 	 * Calculate a day light electricity usage
 	 */
 	public void calculateDayLightElectricityUsage(){
 		double dayLightElectricityUsage = customer.getElectricityUsage().getDayTimeHourlyUsage() 
 				* customer.getLocation().getSunLightHours();
 		//customer.setDayLightElectricityUsage(dayLightElectricityUsage);
 		customer.getElectricityUsage().setDayLightElectricityUsage(dayLightElectricityUsage);
 	}
 	
 	/**
 	 * Calculate the system cost
 	 */
 	public void calculateSystemCost(){
 		equipment.setCost( (equipment.getPanels().get(0).getCost() * equipment.getTotalPanels()) + equipment.getInverter().getCost());
 	}
 	
 	/**
 	 * Calculate the system size
 	 */
 	public void calculateSystemSize(){
 		equipment.setSize( (equipment.getPanels().get(0).getPowerRating() * equipment.getTotalPanels()) / 1000);
 	}
 	
 	public void calculateBankOrientationEfficiencyLoss(Bank [] banks, String orientation, int index) {
 		if(orientation.trim().equalsIgnoreCase("North"))
 			banks[index].setOrientationEfficiencyLoss(5.0);
 		else if (orientation.trim().equalsIgnoreCase("North East"))
 			banks[index].setOrientationEfficiencyLoss(0);
 		else if (orientation.trim().equalsIgnoreCase("East"))
 			banks[index].setOrientationEfficiencyLoss(5.0);
 		else if (orientation.trim().equalsIgnoreCase("South"))
 			banks[index].setOrientationEfficiencyLoss(15);
 		else if (orientation.trim().equalsIgnoreCase("South East"))
 			banks[index].setOrientationEfficiencyLoss(10);
 		else if (orientation.trim().equalsIgnoreCase("West"))
 			banks[index].setOrientationEfficiencyLoss(15);
 		else if(orientation.trim().equalsIgnoreCase("North West"))
 			banks[index].setOrientationEfficiencyLoss(10);
 		else
 			banks[index].setOrientationEfficiencyLoss(20);
 	}
 	
 	public void calculateBankAngleEfficiencyLoss(Bank [] banks, double angle, int index) {
 		if((angle >= 0 && angle < 22.5) || (angle > 337.5 && angle <= 360))
 			banks[index].setAngleEfficiencyLoss(0.0);
 		else if (angle >= 22.5 && angle < 67.5)
 			banks[index].setAngleEfficiencyLoss(5.0);
 		else if (angle >= 67.5 && angle < 112.5)
 			banks[index].setAngleEfficiencyLoss(10.0);
 		else if (angle >= 112.5 && angle < 157.5)
 			banks[index].setAngleEfficiencyLoss(15.0);
 		else if (angle >= 157.5 && angle < 202.5)
 			banks[index].setAngleEfficiencyLoss(20);
 		else if (angle >= 202.5 && angle < 247.5)
 			banks[index].setAngleEfficiencyLoss(15);
 		else if(angle >= 247.5 && angle < 292.5)
 			banks[index].setAngleEfficiencyLoss(10);
 		else if(angle >= 292.5 && angle < 337.5)
 			banks[index].setAngleEfficiencyLoss(5.0);
 		//else
 			//banks[index].setOrientationEfficiencyLoss(0);
 	}
 	
 	/**
 	 * @return the chart
 	 */
 	public Chart getChart() {
 		return chart;
 	}
 	/**
 	 * @param chart the chart to set
 	 */
 	public void setChart(Chart chart) {
 		this.chart = chart;
 	}
 	
 	
 	public void createChart(){
 		try {
 			CartesianChartModel CostCategoryModel;
 			CartesianChartModel savingsCategoryModel;
 			CostCategoryModel = new CartesianChartModel();
 			savingsCategoryModel = new CartesianChartModel();
 			
 			ChartSeries costsWithOutSolarSeries = new ChartSeries();
 			costsWithOutSolarSeries.setLabel("Cost Without Solar");
 			
 			ChartSeries costsWithSolarSeries = new ChartSeries();
 			costsWithSolarSeries.setLabel("Costs With Solar");
 	        
 			for(Calculation curCalculation : this.calculations) {			
 				double annualCost = curCalculation.getTariff11Fee() * this.getCustomer().getElectricityUsage().getDailyAverageUsage()*365;			
 				costsWithOutSolarSeries.set(curCalculation.getYear() + "", annualCost);			
 				costsWithSolarSeries.set(curCalculation.getYear()+"", annualCost - curCalculation.getAnnualSaving());		
 			}
 	        	
 	        ChartSeries savings = new ChartSeries();
 	        savings.setLabel("Cumulative Savings");
 	        
 	        ChartSeries paybackPeriod = new ChartSeries();
 	        paybackPeriod.setLabel("Initial Investment");
 	        
 			double systemCost = this.getEquipment().getCost();		
 	        
 			for(Calculation curCalculation : this.calculations) {			
 				savings.set(curCalculation.getYear()+"", curCalculation.getCumulativeSaving());	
 				paybackPeriod.set(curCalculation.getYear()+"", systemCost);	
 			}
 
 			CostCategoryModel.addSeries(costsWithOutSolarSeries);
 			CostCategoryModel.addSeries(costsWithSolarSeries);
 	        
 			savingsCategoryModel.addSeries(savings);
 			savingsCategoryModel.addSeries(paybackPeriod);
 	        
 	        if(this.chart == null){
 	        	this.chart = new Chart();
 	        }
 			this.chart.setCostsCategoryModel(CostCategoryModel);
 	        this.chart.setSavingsCategoryModel(savingsCategoryModel);
 	        this.setChart(chart);
 		} catch(Exception e){}
 	}
 	/**
 	 * Gets the sunLightHours based on an specific location
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * 
 	 * @return sunLightHours
 	 */
 	public double getSunLightHours(double latitude, double longitude){
 		return getDayLightHours(latitude, longitude);
 	}
 	
 	
 	/**
 	 * Call a restful webservice to get the total daylighthours based on
 	 * the latitude and longitude
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * 
 	 * @return daylighthours
 	 */
 	private double getDayLightHours (double latitude, double longitude){
 		Client client = new Client();
 		try {
 			
 			
 			WebResource webResource = client.resource("http://www.earthtools.org/sun/"+latitude+"/"+longitude+"/22/4/0/0");
 			client.setConnectTimeout(3000);
 			client.setReadTimeout(3000);
 			
 			com.sun.jersey.api.representation.Form input = new Form();
 
 			// send HTTP POST
 			ClientResponse response = webResource.type("text/xml")
 			        .post(ClientResponse.class, input);
 			
 			
 			String output = response.getEntity(String.class);
 			
 			String sunrise = output.substring(output.indexOf("<sunrise>")+9,output.indexOf("</sunrise>"));
 			String sunset = output.substring(output.indexOf("<sunset>")+8,output.indexOf("</sunset>"));
 			
 			
 			System.out.println("sunrise: " + sunrise);
 			System.out.println("sunset: " + sunset);
 			
 			DateFormat formatter = new SimpleDateFormat("hh:mm:ss");
 			Date date1 = formatter.parse(sunrise);
 			Date date2 = formatter.parse(sunset);
 
 			// Get msec from each, and subtract.
 			long diff = date2.getTime() - date1.getTime();
 			double diffInHours = diff / ((double) 1000 * 60 * 60);
 
 			System.out.println("diff: " + diffInHours / 3);
 			
 			client.destroy();
 			
 			return diffInHours / 2.5;		
 			
 		} catch (Exception e) {
 			client.destroy();
 			System.out.println("error: " + e);
 		}
 		return 1;
 	}
 
 }
