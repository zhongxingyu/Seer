 package org.bh.plugin.branchSpecificRepresentative.calc;
 
 import java.awt.Color;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.bh.calculation.IBranchSpecificCalculator;
 import org.bh.data.DTOAccessException;
 import org.bh.data.DTOBranch;
 import org.bh.data.DTOBusinessData;
 import org.bh.data.DTOCompany;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.StringValue;
 import org.bh.platform.PlatformController;
 
 /**
  * This class gives the possibility to calculate a branch specific
  * representative
  * 
  * 
  * @author Denis Roster, Tim Herzenstiel, Sebastian Schumacher
  * @update Günter Hesse
  * @version 1.0, 21.12.2011
  * 
  */
 public class BranchSpecificCalculator implements IBranchSpecificCalculator {
 
 	private double ratingBSR;
 	private Color evaluationOfRating = null;
 	private DTOScenario scenario = null;
 	private Logger log = Logger.getLogger(BranchSpecificCalculator.class);
 
 	/* Specified by interface/super class. */
 	public DTOCompany calculateBSR(DTOBusinessData businessData,
 			DTOScenario scenario) {
 		DTOBranch oldBranch = scenario.getSelectedBranch();
 
 		DTOBranch currBranch = (DTOBranch) oldBranch.clone();
 
 		// Setze die normierte Branche ins Szenario, für Abweichungsanalyse.
 		scenario.setNormedBranch(currBranch);
 
 		this.scenario = scenario;
 
 		// FCFs ermitteln
 		List<DTOCompany> compList = currBranch.getChildren();
 		Iterator<DTOCompany> CompItr = compList.iterator();
 
 		while (CompItr.hasNext()) {
 			DTOCompany company = CompItr.next();
 			calculateFCFs(company);
 		}
 
 		// Tabelle erstellen
 		double[][] companiesAndPeriodsNotNormed = createTable(currBranch);
 
 		// Durchschnitt der Unternehmen
 		double[] averageCompanyNotNormed = getAverage(
 				companiesAndPeriodsNotNormed, "company");
 
 		// Wurzeln ermitteln
 		double[] cubeRoot = getCubeRoot(averageCompanyNotNormed);
 
 		// Do the Branch has Companies?
 		List<DTOCompany> companyList = currBranch.getChildren();
 		Iterator<DTOCompany> CompanyItr = companyList.iterator();
 
 		while (CompanyItr.hasNext()) {
 			DTOCompany currCompany = CompanyItr.next();
 
 			// Norm all FCF-Period-Values
 			if (!currCompany.isNormed) {
 				getNormedCFValue("", currCompany);
 			}
 
 		}
 
 		// Normierte Tabelle erstellen
 		double[][] companiesAndPeriodsNormed = createTable(currBranch);
 		
 		// Normierte Tabelle für Güteberechnung
 		double[][] companiesAndPeriodsNormedRating = createTable(currBranch);
 		
 		// Stutzung der normierten Tabelle
 		double[][] companyAndPeriodsNormedTrimmed = trimmedAverage(
 				companiesAndPeriodsNormed, 2.5);
 
 		// Spezifizierung der gestutzten normierten Tabelle
 		double[] bsrArray = getSpecificAverage(companyAndPeriodsNormedTrimmed,
 				cubeRoot);
 
 		// Werte dem Ergebnis DTOCompany-Objekt zuweisen
 		DTOCompany dtoBSRaverage = new DTOCompany();
 
 		DTOPeriod[] periods = new DTOPeriod[bsrArray.length];
 
 		// ACHTUNG: Hardcoded Jahreszahl --> keine neueren Unternehmensdaten für
 		// die Jahre NACH 2010!!!
 		int year = 2010;
 
 		for (int i = 0; i < periods.length; i++) {
 			periods[i] = new DTOPeriod();
 			periods[i].put(DTOPeriod.Key.NAME, new DoubleValue(year - i));
 			periods[i].put(DTOPeriod.Key.FCF, new DoubleValue(bsrArray[i]));
 			dtoBSRaverage.addChild(periods[i]);
 		}
 
 		computeRating(companiesAndPeriodsNormedRating, bsrArray);
 
 		this.scenario.setBsrCalculatorWithRating(this);
 
 		return dtoBSRaverage;
 
 	}
 
 	/* Specified by interface/super class. */
 	public void getNormedCFValue(String choice, DTOCompany currCompany) {
 
 		DoubleValue firstFCF = null;
 		DoubleValue normedFCF = null;
 		DoubleValue helpFCF = null;
 		double helpDouble = 0;
 		double firstDouble = 0;
 		boolean firstPeriod = true;
 
 		// update References in company.
 		currCompany.updateReferences(scenario);
 
 		// Do the Company has any Periods?
 		List<DTOPeriod> periodList = currCompany.getChildren();
 		Iterator<DTOPeriod> PeriodItr = periodList.iterator();
 
 		int i = 0;
 		int firstPeriodNumber = periodList.size() - 1;
 		DTOPeriod firstPeriodObject = periodList.get(firstPeriodNumber);
 
 		while (PeriodItr.hasNext()) {
 			DTOPeriod currPeriod = PeriodItr.next();
 
 			// TODO Überlegen, ob wir hier mit einer Periode weniger arbeiten,
 			// weil interne Berechnung fehl schlägt!
 			// Sollte dies nicht gewählt werden, bitte Rücksprache mit Yannick
 
 			if (firstPeriod) {
 
 				firstFCF = (DoubleValue) firstPeriodObject
 						.get(DTOPeriod.Key.FCF);
 				firstDouble = firstFCF.getValue();
 				firstPeriod = false;
 
 			}
 
 			helpFCF = (DoubleValue) currPeriod.get(DTOPeriod.Key.FCF);
 			helpDouble = helpFCF.getValue();
 			if (firstDouble < 0) {
 				normedFCF = new DoubleValue(
 						((helpDouble / firstDouble) * (-1)) + 1);
 				currPeriod.put(DTOPeriod.Key.FCF, normedFCF);
 
 			} else if (firstDouble > 0) {
 				normedFCF = new DoubleValue(((helpDouble / firstDouble) - 1));
 				currPeriod.put(DTOPeriod.Key.FCF, normedFCF);
 
 			}
 
 			i++;
 		}
 
 		firstPeriod = true;
 		currCompany.setNormed(true);
 	}
 
 	private void computeRating(double[][] companiesAndPeriodsNormed,
 			double[] bsrArray) {
 
 		double[] rowSum = new double[(companiesAndPeriodsNormed[0].length)];
 
 		for (int zeile = 0; zeile < companiesAndPeriodsNormed[0].length; zeile++) {
 			for (int spalte = 0; spalte < companiesAndPeriodsNormed.length; spalte++) {
 				rowSum[zeile] = rowSum[zeile]
 						+ (Math.pow(
 								(companiesAndPeriodsNormed[spalte][zeile] - bsrArray[zeile]),
 								2.0));
 			}
 		}
 
 		// mit Wertung multiplizieren
 		double percentage = 0.4;
 		double result = 0;
 		for (int i = 0; i < rowSum.length; i++) {
 			result = result + rowSum[i] * percentage;
 			percentage = percentage - 0.1;
 		}
 
 		this.ratingBSR = result / (companiesAndPeriodsNormed.length);
 		
 		System.out.println("" + this.ratingBSR);
 	}
 
	private DoubleValue getBSRCFforPeriod(int period,
			DTOCompany branchSpecificRepresentative) {
		DTOPeriod currPeriod = null;

		List<DTOPeriod> periodList = branchSpecificRepresentative.getChildren();
		Iterator<DTOPeriod> periodItr = periodList.iterator();

		for (int count = 0; periodItr.hasNext();) {
			currPeriod = periodItr.next();
			if (count == period)
				break;
		}
		return ((DoubleValue) currPeriod.get(DTOPeriod.Key.FCF));
	}

 	public double getRating() {
 		return this.ratingBSR;
 	}
 
 	/**
 	 * This method counts the number of companies and years according to a
 	 * branch
 	 * 
 	 * @param DTOBranch
 	 *            currNormedBranch
 	 * @return double-array with the size of [companies] and [periods]
 	 */
 	private double[][] getNumberOfCompaniesAndPeriods(DTOBranch currNormedBranch) {
 		int companyCounter = 0, periodCounter = 0;
 
 		List<DTOCompany> companyList = currNormedBranch.getChildren();
 		companyCounter = companyList.size();
 
 		for(DTOCompany company: companyList){
 			if(company.getChildrenSize() > periodCounter){
 				periodCounter = company.getChildrenSize();
 			}
 		}
 
 		return new double[companyCounter][periodCounter];
 	}
 
 	/**
 	 * This method calculates the average depending on the choice (column =
 	 * "company" OR row = "year")
 	 * 
 	 * @param double[][] table
 	 * @param String
 	 *            choice
 	 * @return double-array of average values
 	 */
 	private double[] getAverage(double[][] table, String choice) {
 		double[] result = null;
 
 		if (choice == "company") {
 			result = new double[table.length];
 			for (int spalte = 0; spalte < table.length; spalte++) {
 				for (int zeile = 0; zeile < table[0].length; zeile++) {
 					result[spalte] = result[spalte] + table[spalte][zeile];
 				}
 				result[spalte] = result[spalte] / table[0].length;
 
 			}
 
 		} else if (choice == "year") {
 			result = null;
 			result = new double[table[0].length];
 			for (int zeile = 0; zeile < table[0].length; zeile++) {
 				for (int spalte = 0; spalte < table.length; spalte++) {
 					result[zeile] = result[zeile] + table[spalte][zeile];
 				}
 				result[zeile] = result[zeile] / table.length;
 			}
 
 		}
 		return result;
 	}
 
 	/**
 	 * This method calculates and return the cube root of a given array
 	 * 
 	 * @param double[]
 	 * @return new double[] including the cube root results
 	 */
 	private double[] getCubeRoot(double[] averageCompanyNotNormed) {
 		double[] result = new double[averageCompanyNotNormed.length];
 		for (int i = 0; i < averageCompanyNotNormed.length; i++) {
 			result[i] = Math.pow(Math.abs(averageCompanyNotNormed[i]),
 					(1 / 3.0));
 		}
 
 		return result;
 	}
 
 	/**
 	 * This method trimms a double[][] table by cutting off outliers according
 	 * to a given factor
 	 * 
 	 * @param double[][] table
 	 * @param double factor
 	 * @return new double[][] trimmed-table
 	 */
 	private double[][] trimmedAverage(double[][] table, double factor) {
 		double[] avg = getAverage(table, "year");
 
 		for (int zeile = 0; zeile < table[0].length; zeile++) {
 			for (int spalte = 1; spalte < table.length; spalte++) {
 				if (((Math.abs(avg[zeile])) * (-1) * factor) > table[spalte][zeile]) {
 					table[spalte][zeile] = Double.MAX_VALUE;
 				} else if (((Math.abs(avg[zeile])) * factor) < table[spalte][zeile]) {
 					table[spalte][zeile] = Double.MAX_VALUE;
 				}
 			}
 		}
 		return table;
 	}
 
 	/**
 	 * This method calculates the specific average of a table with the weights
 	 * of a given [][]-Array
 	 * 
 	 * @param double[][] table
 	 * @param double[] cubeRoot
 	 * @return result double[] of the branch specific representative values of
 	 *         the years
 	 */
 	private double[] getSpecificAverage(double[][] table, double[] cubeRoot) {
 		double weight = 0;
 		double[] result = new double[table[0].length];
 
 		for (int zeile = 0; zeile < table[0].length; zeile++) {
 			for (int spalte = 0; spalte < table.length; spalte++) {
 				if (table[spalte][zeile] != Double.MAX_VALUE) {
 					result[zeile] = result[zeile] + table[spalte][zeile]
 							* cubeRoot[spalte];
 					weight = weight + cubeRoot[spalte];
 
 				}
 
 			}
 			result[zeile] = result[zeile] / weight;
 			weight = 0;
 		}
 		return result;
 	}
 
 	/**
 	 * This method creates a table out of a DTOBranch object
 	 * 
 	 * @param DTOBranch
 	 *            currBranch
 	 * @return the result table is implemented by using a double[][]
 	 */
 	private double[][] createTable(DTOBranch currBranch) {
 
 		double[][] companiesAndPeriods = getNumberOfCompaniesAndPeriods(currBranch);
 
 		// Do the Branch has Companies?
 		List<DTOCompany> companyList = currBranch.getChildren();
 		Iterator<DTOCompany> CompanyItr = companyList.iterator();
 		int innerCompanyCounter = 0;
 
 		while (CompanyItr.hasNext()) {
 			DTOCompany currCompany = CompanyItr.next();
 
 			// Do the Company has any Periods?
 			List<DTOPeriod> periodList = currCompany.getChildren();
 			Iterator<DTOPeriod> PeriodItr = periodList.iterator();
 			DoubleValue helper = null;
 			int innerPeriodCounter = 0;
 
 			while (PeriodItr.hasNext()) {
 				DTOPeriod currPeriod = PeriodItr.next();
 
 				helper = (DoubleValue) currPeriod.get(DTOPeriod.Key.FCF);
 				companiesAndPeriods[innerCompanyCounter][innerPeriodCounter] = helper
 						.getValue();
 				innerPeriodCounter++;
 
 			}
 
 			innerCompanyCounter++;
 
 		}
 		return companiesAndPeriods;
 	}
 
 	public Color getEvaluationOfRating() {
 
 		List<Double> bsrRatings = PlatformController
 				.getAllBSRRatings();
 
 		double highestRating = 0;
 		double lowestRating = 0; // info: low value is better than high value
 		double currentRating = 0;
 
 		Iterator<Double> bsrRatingIterator = bsrRatings.iterator();
 
 		// iteriere über die Rating, um das höchste/tiefste Rating
 		// zu bekommen und auf dieser Grundlage die berechnete Güte
 		// bewerten zu können
 		while (bsrRatingIterator.hasNext()) {
 			currentRating = bsrRatingIterator.next();
 			if (currentRating > highestRating)
 				highestRating = currentRating;
 			else {
 				if ((lowestRating == 0) || (lowestRating > currentRating))
 					lowestRating = currentRating;
 			}
 		}
 
 		// Wertebereich in drittel teilen und Farbe entsprechend zuweisen
 		// grün - gelb - rot
 		double difference = (highestRating - lowestRating) / 3;
 
 		if (this.ratingBSR <= (lowestRating + difference)) {
 			this.evaluationOfRating = Color.GREEN;
 		} else {
 			if (this.ratingBSR > (highestRating - difference))
 				this.evaluationOfRating = Color.RED;
 			else
 				this.evaluationOfRating = Color.YELLOW;
 		}
 		return this.evaluationOfRating;
 	}
 
 	// Get all FCFs
 
 	/**
 	 * This method calculates the Free-Cash-Flows out of the basic data
 	 * (BusinessData-Object)
 	 * 
 	 * @param DTOCompany
 	 *            currCompany
 	 */
 	private void calculateFCFs(DTOCompany currCompany) {
 		// update References in company.
 		currCompany.updateReferences(scenario);
 
 		// Do the Company has any Periods?
 		List<DTOPeriod> periodList = currCompany.getChildren();
 		Iterator<DTOPeriod> PeriodItr = periodList.iterator();
 
 		int i = 0;
 
 		while (PeriodItr.hasNext()) {
 			DTOPeriod currPeriod = PeriodItr.next();
 
 			// Berechnung der richtigen Unternehmensdaten
 			try {
 				currPeriod.put(DTOPeriod.Key.FCF, currPeriod.getFCF());
 			} catch (DTOAccessException dtoaccess) {
 				if (i > 0) {
 					log.error(
 							"Period not readable "
 									+ currCompany.get(DTOCompany.Key.NAME)
 									+ " year: "
 									+ currPeriod.get(DTOPeriod.Key.NAME),
 							dtoaccess);
 				}
 				//TODO Find better solution here, which is also used in algorithm. We have problems calculating FCF in first period!!!
 				currPeriod.put(DTOPeriod.Key.FCF, new DoubleValue(0.0));
 			}
 			try {
 				currPeriod.put(DTOPeriod.Key.LIABILITIES,
 						currPeriod.getLiabilities());
 			} catch (DTOAccessException dtoaccess) {
 				log.error(
 						"Period not readable: "
 								+ currCompany.get(DTOCompany.Key.NAME)
 								+ " year: "
 								+ currPeriod.get(DTOPeriod.Key.NAME), dtoaccess);
 				currPeriod.put(DTOPeriod.Key.LIABILITIES, new DoubleValue(0.0));
 			}
 
 			i++;
 		}
 	}
 
 }
