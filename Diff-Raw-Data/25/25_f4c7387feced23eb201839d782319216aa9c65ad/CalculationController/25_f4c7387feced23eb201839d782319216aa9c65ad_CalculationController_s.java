 package com.qut.spc.controller;
 
 import java.text.DecimalFormat;
 
 import javax.persistence.EntityNotFoundException;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import com.qut.spc.api.SystemCalculationAPI;
 import com.qut.spc.calculations.ElectricityCalculator;
 import com.qut.spc.calculations.ROICalculator;
 import com.qut.spc.calculations.SolarSystem;
 import com.qut.spc.calculations.TotalCostCalculator;
 import com.qut.spc.exceptions.InvalidArgumentException;
 import com.qut.spc.feedInTariff.ElectricityCost;
 import com.qut.spc.feedInTariff.FeedInTariffProvider;
 import com.qut.spc.model.Battery;
 import com.qut.spc.model.Inverter;
 import com.qut.spc.model.Panel;
 
 /**
  * Jersey public access path for retrieving calculation results
  * @author simen
  *
  */
 @Path("/calculate/")
 public class CalculationController {
 	
 	private SystemCalculationAPI calculator;
 	
 	private ROICalculator roiCalculator;
 	
 	public CalculationController(){
 		roiCalculator=new ROICalculator();
 		calculator=new SolarSystem(new ElectricityCalculator(),new TotalCostCalculator(),roiCalculator);
 	}
 	
 	public CalculationController(SystemCalculationAPI calculator,ROICalculator roiCalc) {
 		this.calculator=calculator;
 		this.roiCalculator=roiCalc;
 	}
 	
 	
 	
 	/**
 	 * Based on the given inputs returns an XML representation with results from calculating system cost, electricity production
 	 * and return on investment
 	 * @param panelId
 	 * @param panelCount
 	 * @param batteryId
 	 * @param inverterId
 	 * @param postcode
 	 * @param systemCost
 	 * @param inverterEfficiency
 	 * @param panelEfficiency
 	 * @param panelOutput
 	 * @param energyConsumption
 	 * @return
 	 * @throws InvalidArgumentException
 	 */
 	@GET
 	@Produces("application/xml")
 	@Path("/")
 	public String getCalculations(
 			@QueryParam("panelId")@DefaultValue("-1") int panelId,
 			@QueryParam("panelCount")@DefaultValue("1") int panelCount, 
 			@QueryParam("batteryId")@DefaultValue("-1")int batteryId,
 			@QueryParam("inverterId")@DefaultValue("-1")int inverterId, 
 			@QueryParam("postcode")@DefaultValue("")String postcode,
 			@QueryParam("systemCost")@DefaultValue("-1")double systemCost,
 			@QueryParam("inverterEfficiency")@DefaultValue("-1")double inverterEfficiency,
 			@QueryParam("panelEfficiency")@DefaultValue("-1")double panelEfficiency,
 			@QueryParam("panelOutput")@DefaultValue("-1")double panelOutput,
 			@QueryParam("energyConsumption")@DefaultValue("-1")double energyConsumption) throws InvalidArgumentException{
 		
 		verifyInputs(panelId, batteryId, inverterId, systemCost,
 				inverterEfficiency, panelEfficiency, panelOutput,energyConsumption);
 		
 		setComponents(panelId, batteryId, inverterId, postcode,
 				inverterEfficiency, panelOutput, panelCount,panelEfficiency,systemCost,energyConsumption);
 		
 		
 		DecimalFormat df=new DecimalFormat("#.##");
 		StringBuilder builder=new StringBuilder();
 		
 		builder.append("<calculations>");
 		calculateThings(builder, df);
 		builder.append("</calculations>");
 		
 		
 		return 	builder.toString();
 	}
 
 	
 	private void setComponents(int panelId, int batteryId, int inverterId,
 			String postcode, double inverterEfficiency, double panelOutput, int panelCount,double panelEfficiency, double systemCost, double energyConsumption) {
 		try{
 			//If battery isn't specified, then set a default battery
 			if(batteryId<0){
 				Battery battery=new Battery();
 				calculator.setBattery(battery);
 				calculator.setSystemCost(systemCost);
 			}else{
 				calculator.setBatteryId(batteryId);				
 			}
 			
 			//If panel isn't specified, then set a default panel with provided capacity and efficiency
 			if(panelId<0){
 				Panel panel=new Panel();
 				panel.setCapacity(panelOutput);
 				panel.setEfficiency(panelEfficiency);
 				calculator.setSystemCost(systemCost);
 				calculator.setPanel(panel);
 			}else{
 				calculator.setPanelId(panelId);
 				calculator.setpanelCount(panelCount);
 			}
 			
 			//If inverter isn't specified, then set a default inverter with provided efficiency
 			if(inverterId<0){
 				Inverter inverter=new Inverter();
 				inverter.setEfficiency(inverterEfficiency);
 				calculator.setSystemCost(systemCost);
 				calculator.setInverter(inverter);
 			}else{
 				calculator.setInverterId(inverterId);				
 			}
 			calculator.setLocation(postcode);
 			
 			roiCalculator.setDailyUsage(energyConsumption);
 			roiCalculator.setFeedInTariff(FeedInTariffProvider.getFeedInTariffByPostcode(postcode));
 			roiCalculator.setCostOfElectricity(ElectricityCost.getElectricityCost(postcode));
 			
 		}catch(EntityNotFoundException e){
 			throw new InvalidArgumentException(e.getMessage());
 		}catch(IllegalArgumentException e){
 			throw new InvalidArgumentException(e.getMessage());
 		}
 	}
 
 	private void verifyInputs(int panelId, int batteryId, int inverterId,
 			double systemCost, double inverterEfficiency,
 			double panelEfficiency, double panelOutput, double energyConsumption) {
 		String errorMessage="";
 		if(panelId<0 && (panelEfficiency<0||panelOutput<0||systemCost<0)){
 			errorMessage+="PanelId and panelEfficiency, panelOutput or systemCost is not defined \n";
 		}
 		if(batteryId<0&&systemCost<0){
 			errorMessage+="BatteryId is not defined and neither is systemCost \n";
 		}
 		if(inverterId<0&& (inverterEfficiency<0||systemCost<0)){
 			errorMessage+="InverterId and inverterEfficiency or systemCost is not defined \n";
 		}
 		if(energyConsumption<0)
 			errorMessage+="EnergyConsumption must be non negative";
 		if(!errorMessage.equals("")){
 			throw new InvalidArgumentException(errorMessage);
 		}
 	}
 	
 	private void calculateThings(StringBuilder builder,DecimalFormat df){		
 		double[] weekCalc=calculate(7);
 		double[]monthCalc=calculate(30);
 		double[]yearCalc=calculate(365);
 		double[]twentyFiveYear=calculate(365*25);
 		appendParameter(yearCalc[0],monthCalc[0],weekCalc[0],twentyFiveYear[0], "electricityProduction", builder, df);		
 		appendParameter(yearCalc[1],monthCalc[1],weekCalc[1],twentyFiveYear[1], "totalCost", builder, df);
 		appendParameter(yearCalc[2],monthCalc[2],weekCalc[2],twentyFiveYear[2], "returnOnInvestment", builder, df);
 
 	}
 	
 	private double[] calculate(int timespan){
 		calculator.setTimespan(timespan);
 		
 		double elProduction=calculator.getElectricityProduction();
 		double tc=calculator.getTotalCost();
 		
 		roiCalculator.setElectricityProduction(elProduction,timespan);
 		roiCalculator.setSystemCost(tc);
 		
 		double roi=calculator.getROI();
 		
 		return new double[]{elProduction,tc,roi};
 	}
 	
 	private void appendParameter(double year,double month,double week,double twentyFive, String name, StringBuilder builder,DecimalFormat format){
 		builder.append("<"+name+">");
 		builder.append("<year>"+format.format(year)+"</year>");
 		builder.append("<month>"+format.format(month)+"</month>");
 		builder.append("<week>"+format.format(week)+"</week>");
 		builder.append("<twentyFiveYears>"+format.format(twentyFive)+"</twentyFiveYears>");
 		builder.append("</"+name+">");
 	}
 }
