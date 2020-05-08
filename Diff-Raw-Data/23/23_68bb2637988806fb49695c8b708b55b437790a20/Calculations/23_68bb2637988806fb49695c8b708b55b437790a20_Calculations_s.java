 package project.server;
 
 import project.shared.CalcException;
 
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
 	private double latitude;
 	private String panelNumber;
 	private String inverterNumber;
 	private String energyCompany;
 	private int tilt;
 	private int orientation;
 	private static double DAYSPERYEAR = 365.25;
 	private double [] cumulative;
 	private static double YEARLYEFFICIENCYLOSS = 0.007;
 	private static double IDEALANGLEFACTOR = 0.87; //qld
 	private double idealTilt;
 
 	
 	
 	public Calculations(String panelNumber, int numberPanels, String suburb,
 			String inverterNumber, String energyCompany, double dailyUsage, 
 			int tilt, int orientation) throws CalcException{
 		if(numberPanels < 0){
 			throw new CalcException("Number of panels must be positive");
 		}
 		if(dailyUsage < 0){
 			throw new CalcException("Daily usage must be positive");
 		}
 		if((tilt < 0) || (tilt > 90)){
 			throw new CalcException("Tilt must be between 0 and 90 degrees");
 		}
 		if((orientation <= -360) || (orientation >= 360)){
 			throw new CalcException("Orientation must be between -360 and 360 degrees");
 		}
 
 		this.panelNumber = panelNumber;
 		this.inverterNumber = inverterNumber;
 		this.energyCompany = energyCompany;
 		inverter = new Inverter(inverterNumber);
 		panel = new SolarPanel(panelNumber);
		sunData = new SunData(suburb);
		tariffs = new Tariffs(energyCompany);
		tariff11 = tariffs.getNormalTariff();
		feedInTariff = tariffs.getFeedInTariff();
 		this.numberPanels = numberPanels;
 		systemPower = inverter.getPMax();
		
 		//changes system power if the total power of the solar panels
 		//is less than that of the inverter pMax
 		if(panel.getPMaxNOCT()*numberPanels < systemPower){
 			systemPower = panel.getPMaxNOCT()*numberPanels;
 		}
 			
 		replacementGeneration = dailyUsage;
 		inverterEfficiency = inverter.getMaxEfficiency();
 		solarExposure = sunData.getSolarExposure();
 		latitude = Math.abs(sunData.getLatitude());
 		this.tilt = tilt;
 		idealTilt = this.latitude * IDEALANGLEFACTOR;
 		this.orientation = Math.abs(orientation);
 		
 		//changes orientation angle to a number between 0 and 180
 		if(this.orientation > 180){
 			this.orientation = 360 - this.orientation;
 		}
 		
 		//calculate orientation efficiency loss
 		if((Math.abs(tilt - idealTilt) <= 5) && 
 				this.orientation <= 10){
 			orientationEfficiencyLoss = 0;
 		}else if((Math.abs(tilt - idealTilt) <= 20) && 
 				this.orientation <= 60){
 			orientationEfficiencyLoss = 0.1;
 		}else{
 			orientationEfficiencyLoss = 0.2;
 		}		
 	}
 	
 	//use year to adjust efficiency
 	public double getPowerGenerated(int year){			
 		//daily generation
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
 	
 	public double getBreakEven(double initialCost, double interestRate) throws CalcException{
 		if(initialCost < 0){
 			throw new CalcException("Initial cost must be positive");
 		}
 		if(interestRate < 0){
 			throw new CalcException("Interest rate must be positive");
 		}
 		
 		double[] investment = new double [20];
 		investment[0] = initialCost;
 		for(int i=1; i<investment.length; i++){
 			investment[i] = investment[i-1]*(1+interestRate/100);
 		}
 		
 		double[] savings = new double [20];
 		savings = this.getCumulativeSavings(20);
 		double year = 0;
 		//find what year the investment breaks even
 		for(int i=0; i<savings.length; i++){
 			if(savings[i] < investment[i]){
 				year = i + 1.0;
 			}
 		}
 		if(year < 20){
 			double saved = savings[(int) year - 1];
 			double invested = investment[(int) year - 1];
 			double day = 0;
 			//find what day the investment breaks even 
 			for(int i=0; i<366; i++){
 				saved = saved + this.getDailySavings((int)year+1);
 				invested = invested + (interestRate/100/365)*invested;
 				if(saved < invested){
 					day = i + 1.0;
 				}	
 			}
 			double breakEven = year + day/365;
 			return breakEven;
 		}else{
 			//this number to indicate investment doesnt break even
 			return -5.0;
 		}
 	}
 	
 	//For testing only
 		public Calculations(String panelNumber, int numberPanels, String suburb,
 				String inverterNumber, String energyCompany, double dailyUsage, 
 				int tilt, int orientation, String test) throws CalcException{
 			if(numberPanels < 0){
 				throw new CalcException("Number of panels must be positive");
 			}
 			if(dailyUsage < 0){
 				throw new CalcException("Daily usage must be positive");
 			}
 			if((tilt < 0) || (tilt > 90)){
 				throw new CalcException("Tilt must be between 0 and 90 degrees");
 			}
 			if((orientation <= -360) || (orientation >= 360)){
 				throw new CalcException("Orientation must be between -360 and 360 degrees");
 			}
 
 			this.panelNumber = panelNumber;
 			this.inverterNumber = inverterNumber;
 			this.energyCompany = energyCompany;
 			inverter = new Inverter();
 			panel = new SolarPanel();
 			sunData = new SunData();
 			tariffs = new Tariffs();
 			tariff11 = tariffs.getNormalTariff();
 			feedInTariff = tariffs.getFeedInTariff();
 			this.numberPanels = numberPanels;
 			systemPower = inverter.getPMax();
 			
 			//changes system power if the total power of the solar panels
 			//is less than that of the inverter pMax
 			if(panel.getPMaxNOCT()*numberPanels < systemPower){
 				systemPower = panel.getPMaxNOCT()*numberPanels;
 			}
 				
 			replacementGeneration = dailyUsage;
 			inverterEfficiency = inverter.getMaxEfficiency();
 			solarExposure = sunData.getSolarExposure();
 			latitude = Math.abs(sunData.getLatitude());
 			this.tilt = tilt;
 			idealTilt = this.latitude * IDEALANGLEFACTOR;
 			this.orientation = Math.abs(orientation);
 			//changes orientation angle to a number between 0 and 180
 			if(this.orientation > 180){
 				this.orientation = 360 - this.orientation;
 			}
 			
 			if((Math.abs(tilt - idealTilt) <= 5) && 
 					this.orientation <= 10){
 				orientationEfficiencyLoss = 0;
 			}else if((Math.abs(tilt - idealTilt) <= 20) && 
 					this.orientation <= 60){
 				orientationEfficiencyLoss = 0.1;
 			}else{
 				orientationEfficiencyLoss = 0.2;
 			}		
 		}
 		
 	//the following getters are just for testing
 	public String getPanelNumber(){
 		return panelNumber;
 	}
 	
 	public String getInverterNumber(){
 		return inverterNumber;
 	}
 	
 	public String getEnergyCompany(){
 		return energyCompany;
 	}
 	
 	public double getTariff11(){
 		return tariff11;
 	}
 	
 	public double getFeedInTariff(){
 		return feedInTariff;
 	}
 	
 	public int getNumberPanels(){
 		return numberPanels;
 	}
 	
 	public double getSystemPower(){
 		return systemPower;
 	}
 	
 	public double getReplacementGeneration(){
 		return replacementGeneration;
 	}
 	
 	public double getInverterEfficiency(){
 		return inverterEfficiency;
 	}
 	
 	public double getSolarExposure(){
 		return solarExposure;
 	}
 	
 	public double getLatitude(){
 		return latitude;
 	}
 	
 	public int getTilt(){
 		return tilt;
 	}
 	
 	public int getOrientation(){
 		return orientation;
 	}
 	
 	public double getOrientationEfficiencyLoss(){
 		return orientationEfficiencyLoss;
 	}
 	
 	
 }
