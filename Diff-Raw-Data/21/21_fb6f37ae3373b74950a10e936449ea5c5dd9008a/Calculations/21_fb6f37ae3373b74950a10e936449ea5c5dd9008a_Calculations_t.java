 package project.server;
 
 public class Calculations {
 	
 	private Inverter inverter;
 	private SolarPanel panel;
 	private SunData sunData;
 	private Tariffs tariffs;
 	private double systemPower;
 	private int numberPanels;
 	private double dailySavings;
 	private double orientationEfficiencyLoss;
 	private double inverterEfficiency;
 	private double solarExposure;
 	private double powerGenerated;
 	private double tariff11;
 	private double feedInTariff;
 	private double replacementGeneration;
	private int latitude;
	private int tilt;
	private int orientation;
 	private static double DAYSPERYEAR = 365.25;
 	private double [] cumulative;
 	private static double YEARLYEFFICIENCYLOSS = 0.007;
 	private static double IDEALANGLEFACTOR = 0.87; //qld
 	private double idealTilt;
 
 	
 	public Calculations(String panelNumber, int numberPanels, int postcode,
 			String inverterNumber, String energyCompany, double dailyUsage, 
 			int latitude, int tilt, int orientation){
 		//TODO change all constructors later
 		inverter = new Inverter(inverterNumber);
 		panel = new SolarPanel();
 		sunData = new SunData(postcode);
 		tariffs = new Tariffs(energyCompany);
 		tariff11 = tariffs.getNormalTariff();
 		feedInTariff = tariffs.getFeedInTariff();
 		this.numberPanels = numberPanels;
 		//change to a calculated value
 		//orientationEfficiencyLoss = efficiencyLoss/100;
 		//find out if this is how it works
 		systemPower = inverter.getPMax();
 		//change this to user entered data
 		replacementGeneration = dailyUsage;
 		inverterEfficiency = inverter.getMaxEfficiency();
 		solarExposure = sunData.getSolarExposure();
		this.latitude = Math.abs(latitude);
		this.tilt = Math.abs(tilt);
		this.orientation = Math.abs(orientation);
		idealTilt = this.latitude * IDEALANGLEFACTOR;
		
		if((Math.abs(tilt - idealTilt) <= 5) && 
				(Math.abs(orientation) <= 10)){
 			orientationEfficiencyLoss = 0;
		}else if((Math.abs(tilt - idealTilt) <= 20) && 
				(Math.abs(orientation) <= 60)){
 			orientationEfficiencyLoss = 0.1;
 		}else{
 			orientationEfficiencyLoss = 1;
 		}
 		
 		
 		
 	}
 	//use year to adjust efficiency
 	public double getPowerGenerated(int year){
 		
 		if(panel.getPMaxNOCT()*numberPanels < systemPower){
 			systemPower = panel.getPMaxNOCT()*numberPanels;
 		}
 				
 		//daily generation
 		//needs verification and solar panel age efficiency loss
 		powerGenerated = systemPower * (1 - orientationEfficiencyLoss) * 
 				(1 - YEARLYEFFICIENCYLOSS * (year - 1)) * inverterEfficiency *
 				solarExposure;
 		return powerGenerated;
 	}
 	
 	public double getDailySavings(int year){
 		//in dollars
 		double power = this.getPowerGenerated(year);
 		if(replacementGeneration < power){
 			dailySavings = replacementGeneration * tariff11 + 
 				(power - replacementGeneration)
 				* feedInTariff;
 		}else{
 			dailySavings = power * tariff11;
 		}
 		return dailySavings;
 	}
 	//maybe just return a single value??
 	public double[] getCumulativeSavings(int numYears){
 		cumulative = new double[numYears];
 		cumulative[0] = getDailySavings(1)*DAYSPERYEAR;
 		for (int i=1; i<numYears; i++){
 			cumulative[i] = cumulative[i-1] + getDailySavings(i+1)*DAYSPERYEAR;
 		}
 		return cumulative;
 	}
 }
