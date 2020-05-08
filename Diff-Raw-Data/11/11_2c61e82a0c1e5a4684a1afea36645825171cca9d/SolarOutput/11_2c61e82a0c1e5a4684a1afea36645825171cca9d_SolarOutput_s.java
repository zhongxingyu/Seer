 package powerCalculations;
 
 import java.text.DecimalFormat;
 
 import environmentalSpecifications.*;
 
 
 /**
  * SolarOutput is responsible for all calculations relating to energy output of a system
  *
  */
 public class SolarOutput {	
 	
 	private static int wattsInKilowatt = 1000;
 	private static DecimalFormat moneyDecFormat = new DecimalFormat("#.##");
 	private static int installCost = 1000;
 	
 	/**
 	 * Calculates the hourly output of a system in Wh
 	 * 
 	 * @param system the solar panel system configuration
 	 * @return The hourly output of a system in KWh
 	 */
 	public static double calculateHourlyOutput(SystemConfiguration system) {
 		double output = calculateRawHourlyOutput(system) * system.getInverterEfficiency();
 		return output;
 	}
 	
 	/**
 	 * Calculates the raw hourly output of the system regardless of efficiencies
 	 * @param system the solar panel system confguration
 	 * @return Wh produced in a single hour
 	 */
 	private static double calculateRawHourlyOutput(SystemConfiguration system) {
 		return system.getPanelCount() * system.getPanelOutput();
 	}
 	
 	/**
 	 *  Calculates the gross daily output of a system given its location and set-up
 	 *  
 	 * @param location LocationDetails details about the location of the set-up
 	 * @param system PanelConfiguration the solar panel system configuration
 	 * @return The daily of output of the system in KWh
 	 */
 	public static double calculateGrossDailyOutput(LocationDetails location, SystemConfiguration system) {
 		return calculateHourlyOutput(system) * location.getDaylightHours() / wattsInKilowatt;
 	}
 	
 	/**
 	 *  Calculates the net daily output of a system given its location and set-up, and hours consumed
 	 *  
 	 * @param location LocationDetails details about the location of the set-up
 	 * @param system PanelConfiguration the solar panel system configuration
 	 * @return The daily of output of the system in KWh
 	 */
 	public static double calculateNetDailyOutput(LocationDetails location, SystemConfiguration system) {
 		return calculateGrossDailyOutput(location, system) - (location.getMonthlyConsumption() / 30);
 	}
 	
 	/**
 	 *  Calculates the monthly output of a system given its location and set-up
 	 * 
 	 * @param location LocationDetails details about the location of the set-up
 	 * @param system PanelConfiguration the solar panel system configuration
 	 * @return The daily of output of the system in KWh
 	 */
 	public static double calculateGrossMonthlyOutput(LocationDetails location , SystemConfiguration system) {
 		return calculateGrossDailyOutput(location, system)*30;
 	}
 	
 	/**
 	 * 
 	 * Calculates the expected system cost based on the system rating
 	 * @param system PanelConfiguration the solar panel system configuration
 	 * @return The expected dollar cost of the system
 	 */
 	public static double calculateSystemCost(SystemConfiguration system) {
 		double hourlyRawOutput = calculateRawHourlyOutput(system);
 		double rawCost = hourlyRawOutput + calculateInverterCost(hourlyRawOutput)  + calculateInstallCost();
 	    return Double.valueOf(moneyDecFormat.format(rawCost));
 	}
 
 	/**
 	 * 
 	 * @param systemOutput the Raw output of a system in W
 	 * @return expected cost of an inverter to match output
 	 */
 	public static double calculateInverterCost(double systemOutput) {
 		return (Math.ceil(systemOutput)) + 300;
 		//return 1.06954219*systemOutput + 269.44442;
 		//return -0.00004211*(systemOutput *systemOutput) + 1.06954219*systemOutput + 269.44442;
 	}
 	
 	/**
 	 * Get the cost of installation for a system
 	 * @return current flat install rate of $1000
 	 */
 	public static double calculateInstallCost() {
 		return Double.valueOf(moneyDecFormat.format(installCost));
 	}
 	
 	public static double calculateTotalPanelCost(SystemConfiguration system) {
 		return Double.valueOf(moneyDecFormat.format(calculatePanelCost(system) * system.getPanelCount()));
 	}
 	
 	/**
 	 * calculate the cost of a panel in a system
 	 * Panel cost is currently calculated at $1/watt
 	 * @return the cost of a solar panel given its output
 	 */
 	private static double calculatePanelCost(SystemConfiguration system) {
 		return system.getPanelOutput();
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the dollar value that the system is generating per month
 	 */
 	public static double calculateMonthlyOutputValue(SystemConfiguration system, LocationDetails location) {
 		double outputValue = calculateGrossMonthlyOutput(location, system)*(location.getExportRate());
 		return Double.valueOf(moneyDecFormat.format(outputValue));
 	}
 
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return a string stating how long it will be until the system has paid for itself
 	 */
 	public static String calculateBreakEven(SystemConfiguration system, LocationDetails location) {
 		int totalMonths = (int) (calculateSystemCost(system)/calculateMonthlyOutputValue(system, location));
 		int months = totalMonths % 12;
 		int years = (totalMonths - months) / 12;
 		return(years + " years, " + months + " months");
 	}
 }
