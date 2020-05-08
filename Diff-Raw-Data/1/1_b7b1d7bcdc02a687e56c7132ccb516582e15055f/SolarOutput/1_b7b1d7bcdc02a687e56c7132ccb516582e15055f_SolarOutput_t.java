 package powerCalculations;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import environmentalSpecifications.*;
 
 
 /**
  * SolarOutput is responsible for all calculations relating to energy output of a system
  * @author Glen-Andrew 
  */
 public class SolarOutput {	
 	
 	private static DecimalFormat moneyDecFormat = new DecimalFormat("#.##");
 	private static int installCost = 1000;
 	private static double daysInMonth = 30.44;
 	private static ArrayList<Double> data = new ArrayList<Double>();
 	
 	/**
 	 * calculates the theoretical maximum output of the system by multiplying panels, panel output
 	 * and inverter efficiency
 	 * @param system
 	 * @return max output
 	 */
 	public static double calculateSystemRating(SystemConfiguration system) {
 		return (system.getPanelCount()*system.getPanelOutput()*system.getInverterEfficiency());
 	}
 	
 	
 	/**
 	 * calculates the total area of the solar panels by dividing the system rating by the panel density
 	 * @param system
 	 * @return area of solar panel
 	 */
 	public static double calculatePanelArea(SystemConfiguration system) {
 		return ((calculateSystemRating(system)/system.getInverterEfficiency())/system.getPanelDensity());
 	}
 	
 	
 	/**
 	 * calculates the amount of solar exposure the system will get in winter by multiplying the area and the insolation
 	 * it is assumed that only half of the daylight hours are usable to the system
 	 * @param system
 	 * @param location
 	 * @return total energy hitting the panels in KWh
 	 */
 	public static double calculateSolarExposureWinter(SystemConfiguration system, LocationDetails location) {
 		double maxExposure = location.getSolarInsolationWinter()*calculatePanelArea(system);
 		double likelyExposure = maxExposure/2;
 		return likelyExposure;
 	}
 	
 	/**
 	 * calculates the amount of solar exposure the system will get in summer by multiplying the area and the insolation
 	 * it is assumed that only half of the daylight hours are usable to the system
 	 * @param system
 	 * @param location
 	 * @return total energy hitting the panels in KWh
 	 */
 	public static double calculateSolarExposureSummer(SystemConfiguration system, LocationDetails location) {
 		double maxExposure = location.getSolarInsolationSummer()*calculatePanelArea(system);
 		double likelyExposure = maxExposure/2;
 		return likelyExposure;
 	}
 	
 	/**
 	 * Calculates the efficiency of the overall system by multiplying the panel efficiency, effects of temperature
 	 * and the yearly denigration
 	 * @param system
 	 * @param location
 	 * @param year
 	 * @return
 	 */
 	public static double calculatePanelEfficiencyWinter (SystemConfiguration system, LocationDetails location, int year) {
 		int tempDifference = location.getRoofTempWinter() - 25;
 		double tempAjustment = tempDifference*system.getTempCoefficient();
 		double denegration = 1;
 		if (year > 25) {
 			denegration = Math.pow((1-system.getPanelDegradation()),25);
 		} else {
 			denegration = Math.pow((1-system.getPanelDegradation()),year);
 		}
 		double ajustedEfficiency = (system.getPanelEfficiency() + tempAjustment) * denegration;
 		return ajustedEfficiency;
 	}
 	
 	public static double calculatePanelEfficiencySummer (SystemConfiguration system, LocationDetails location, int year) {
 		int tempDifference = location.getRoofTempSummer() - 25;
 		double tempAjustment = tempDifference*system.getTempCoefficient();
 		double denegration = 1;
 		if (year > 25) {
 			denegration = Math.pow((1-system.getPanelDegradation()),25);
 		} else {
 			denegration = Math.pow((1-system.getPanelDegradation()),year);
 		}
 		double ajustedEfficiency = (system.getPanelEfficiency() + tempAjustment) * denegration;
 		return ajustedEfficiency;
 	}
 	
 	/**
 	 * Calculates the output by multiplying the efficiency and the solar exposure, checks to see if this
 	 * is more than the maximum the system can produce.
 	 * @param system
 	 * @param location
 	 * @param year
 	 * @return The lowest of the maximum the system can produce in optimal conditions and the maximum the weather and efficiency
 	 * will allow the system to produce.
 	 */
 	public static double calculateMonthlyWinterOutput (SystemConfiguration system, LocationDetails location, int year) {
 		double maxPossible = calculatePanelEfficiencyWinter(system, location, year)*calculateSolarExposureWinter(system, location);
 		double systemMax = calculateSystemRating(system)*(location.getDaylightHoursWinter()/2)*Math.pow((1-system.getPanelDegradation()), year);
 		double systemMaxKillowatts = systemMax/1000;
 		if (maxPossible > systemMaxKillowatts) {
 			return (systemMaxKillowatts*daysInMonth);
 		}
 		else {
 			return (maxPossible*daysInMonth);
 		}
 	}
 	
 	public static double calculateMonthlySummerOutput (SystemConfiguration system, LocationDetails location, int year) {
 		double maxPossible = calculatePanelEfficiencySummer(system, location, year)*calculateSolarExposureSummer(system, location);
 		double systemMax = calculateSystemRating(system)*(location.getDaylightHoursSummer()/2)*Math.pow((1-system.getPanelDegradation()), year);
 		double systemMaxKillowatts = systemMax/1000;
 		if (maxPossible > systemMaxKillowatts) {
 			return (systemMaxKillowatts*daysInMonth);
 		}
 		else {
 			return (maxPossible*daysInMonth);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @param year
 	 * @return returns the average output of all the months in the given year
 	 */
 	public static double calculateAverageMonthlyOutput (SystemConfiguration system, LocationDetails location, int year) {
 		return (calculateMonthlyWinterOutput(system, location, year) + calculateMonthlySummerOutput(system, location, year)) / 2;
 	}
 	
 	/**
 	 * calculates the monthly savings during the cold months of May-Oct
 	 * @param system
 	 * @param location
 	 * @param year
 	 * @return
 	 */
 	public static double calculateMonthlyWinterSavings (SystemConfiguration system, LocationDetails location, int year) {
 		double monthlyOutput = calculateMonthlyWinterOutput(system, location, year);
 		double exportedPower = 0;
 		if (monthlyOutput > location.getMonthlyWinterConsumption()) {
 			exportedPower = monthlyOutput - location.getMonthlyWinterConsumption();
 		}
 		return ((monthlyOutput - exportedPower)*location.getImportRate()) + (exportedPower*location.getExportRate());
 	}
 	
 	/**
 	 * calculates the monthly savings of the warm months of Nov-Apr
 	 * @param system
 	 * @param location
 	 * @param year
 	 * @return
 	 */
 	public static double calculateMonthlySummerSavings (SystemConfiguration system, LocationDetails location, int year) {
 		double monthlyOutput = calculateMonthlySummerOutput(system, location, year);
 		double exportedPower = 0;
 		if (monthlyOutput > location.getMonthlySummerConsumption()) {
 			exportedPower = monthlyOutput - location.getMonthlySummerConsumption();
 		}
 		return ((monthlyOutput - exportedPower)*location.getImportRate()) + (exportedPower*location.getExportRate());
 	}
 	
 	/**
 	 * Calculates the average savings per month over the given year
 	 * @param system
 	 * @param location
 	 * @param year
 	 * @return
 	 */
 	public static double calculateAverageMonthlySavings (SystemConfiguration system, LocationDetails location, int year) {
 		return (calculateMonthlyWinterSavings(system, location, year) + calculateMonthlySummerSavings(system, location, year)) / 2;
 	}
 	
 	/**
 	 * Calculates the time it will take to break even by accumulating the monthly savings until they are
 	 * are equal to or greater than the cost of the system
 	 * @param system
 	 * @param location
 	 * @return the time in years and months until the savings reach the cost of the system
 	 */
 	public static String calculateBreakEvenTime (SystemConfiguration system, LocationDetails location) {
 		double cost = calculateSystemCost(system);
 		double savings = 0;
 		int year = 0;
 		int month = 0;
 		int finalMonth = 1;
 		boolean never = false;
 		while (savings < cost) {
 			while (month < 12 && savings < cost) {
 				savings = savings + calculateAverageMonthlySavings(system, location, year);
 				month = month + 1;
 				finalMonth = month;
 			}
 			month = 0;
 			year = year + 1;
 			// If it has been more than 200 years, stop the calculation and return never
 			if (year > 200) {
 				never = true;
 				savings = cost;
 			}
 		}
 		if (never == true) {
 			return ("Never!");
 		} else {
 			return (year-1 + " years, " + finalMonth + " months");
 		}
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the average monthly savings during the first year of operation
 	 */
 	public static double getInitialMonthlySavings (SystemConfiguration system, LocationDetails location) {
 		return calculateAverageMonthlySavings(system, location, 0);
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the average monthly savings during the cold months in the first year of operation
 	 */
 	public static double getInitialMonthlyWinterSavings (SystemConfiguration system, LocationDetails location) {
 		return calculateMonthlyWinterSavings(system, location, 0);
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the average monthly savings during the warm months in the first year of operation
 	 */
 	public static double getInitialMonthlySummerSavings (SystemConfiguration system, LocationDetails location) {
 		return calculateMonthlySummerSavings(system, location, 0);
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the average monthly output during the first year of operation
 	 */
 	public static double getInitialMonthlyOutput (SystemConfiguration system, LocationDetails location) {
 		return calculateAverageMonthlyOutput(system, location, 0);
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the average monthly output during the cold months in the first year of operation
 	 */
 	public static double getInitialMonthlyWinterOutput (SystemConfiguration system, LocationDetails location) {
 		return calculateMonthlyWinterOutput(system, location, 0);
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the average monthly output during the warm months in the first year of operation
 	 */
 	public static double getInitialMonthlySummerOutput (SystemConfiguration system, LocationDetails location) {
 		return calculateMonthlySummerOutput(system, location, 0);
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the total output of the system in its first year
 	 */
 	public static double getFristYearOutput (SystemConfiguration system, LocationDetails location) {
 		return (calculateAverageMonthlyOutput(system, location, 0)*12);
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the total amount of money saved during the first year
 	 */
 	public static double getFristYearSavings (SystemConfiguration system, LocationDetails location) {
 		return (calculateAverageMonthlySavings(system, location, 0)*12);
 	}
 	
 	/**
 	 * 
 	 * @return savings data
 	 */
 	public static List<Double> getData(SystemConfiguration system, LocationDetails location) {
 		double savings = 0;
 		int year = 0;
 		while (year < 50) {
 			savings = savings + (calculateAverageMonthlySavings(system, location, year)*12);
 			data.add(savings); // Collects the accumulative savings each month in an array to be used for graphing.
 			year = year + 1;
 		}
 		
 		return data;
 	}
 	
 	/**
 	 * 
 	 * @param system
 	 * @param location
 	 * @return the total amount of power the system fed back into the grid in the first year in KWh
 	 */
 	public static double getFristYearExport(SystemConfiguration system, LocationDetails location) {
 		double monthlyOutputSummer = calculateMonthlySummerOutput(system, location, 0);
 		double exportedPowerSummer = 0;
 		if (monthlyOutputSummer > location.getMonthlySummerConsumption()) {
 			exportedPowerSummer = (monthlyOutputSummer - location.getMonthlySummerConsumption())*6;
 		}
 		double monthlyOutputWinter = calculateMonthlyWinterOutput(system, location, 0);
 		double exportedPowerWinter = 0;
 		if (monthlyOutputWinter > location.getMonthlyWinterConsumption()) {
 			exportedPowerWinter = (monthlyOutputWinter - location.getMonthlyWinterConsumption())*6;
 		}
 		return (exportedPowerSummer+exportedPowerWinter);
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 
 	/**
 	 * 
 	 * Calculates the expected system cost based on the system rating
 	 * @param system PanelConfiguration the solar panel system configuration
 	 * @return The expected dollar cost of the system
 	 */
 	public static double calculateSystemCost(SystemConfiguration system) {
 		double hourlyRawOutput = system.getPanelOutput()*system.getPanelCount();
 		double rawCost = hourlyRawOutput + calculateInverterCost(hourlyRawOutput)  + calculateInstallCost();
 	    return Double.valueOf(moneyDecFormat.format(rawCost));
 	}
 
 	/**
 	 * 
 	 * @param systemOutput the Raw output of a system in W
 	 * @return expected cost of an inverter to match output
 	 */
 	public static double calculateInverterCost(double systemOutput) {
 		//Calculation should be somthing like this, don't have time to implement and test right now.
 		//Say, inverters come in increments of 250 watts, at $200 per increment. With $300 base line.
 		//return (Math.ceil(systemOutput / 250))*200 + 300;
 		
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
 
 	
 
 }
