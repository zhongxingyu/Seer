 package de.dhbw.wwi11sca.skdns.server;
 
 /**
  * 
  * @author SKDNS Marktsimulationen
  * 
  * In der MarketSimulation werden alle Berechnungen fr die Marktsimulation ausgefhrt.
  *
  */
 import de.dhbw.wwi11sca.skdns.shared.Company;
 import de.dhbw.wwi11sca.skdns.shared.Machines;
 import de.dhbw.wwi11sca.skdns.shared.OwnCompany;
 import de.dhbw.wwi11sca.skdns.shared.SimulationVersion;
 
 public class MarketSimulation {
 
 	OwnCompany ownCompany;
 	Company company1;
 	Company company2;
 	Company company3;
 
 	// Sonderfall 1
 	double freePersonal = 0;
 	int necessaryPersonal = 0;
 
 	// Schritt 2
 	int random = 0;
 	int topLineMarket = 0;
 	int topLineMarketArithmetic = 0;
 	final static double increaseRandom[] = { 1.01, 1.01, 1.02, 1.03, 1.04,
 			1.02, 1.02, 1.01, 1.00, 0.99, 0.98, 1.01, 1.05, 1.02, 0.99, 1.03 };
 
 	// Schritt 3
 	final static double marketingRandom[] = { 1.00, 1.00, 1.00, 1.00, 1.05,
 			1.05, 1.05, 1.05, 1.05, 1.05, 1.1, 1.1, 1.1, 1.15, 1.15, 1.2 };
 
 	// Schritt 5
 	int amount = 0;
 	int amortization = 0;
 
 	public SimulationVersion simulate(SimulationVersion version) {
 
 		ownCompany = version.getOwnCompany();
 		company1 = version.getCompany1();
 		company2 = version.getCompany2();
 		company3 = version.getCompany3();
 
 		// Hinzufgen des neu eingestellten Personals
 		ownCompany.setNumberOfStaff(ownCompany.getNumberOfStaff() + version.getPersonal());
 		
 		// Sonderfall 1
 		// berprft, ob gengend Personal fr den Einsatz aller Maschinen
 		// vorhanden ist
 		
 		specialCaseOne(version);
 
 		// Schritt 1
 		// Berechnung des Gesamtumsatz des Marktes
 		// Umsatz aller Unternehmen addieren
 
 		topLineMarket = calculateTopLineMarket(topLineMarket,
 				ownCompany.getTopLine(), company1.getTopLine(),
 				company2.getTopLine(), company3.getTopLine());
 
 		// Schritt 2
 		// Ermittlung des Marktwachstums und erneute Berechnung des Gesamtumsatz
 		// des Markets
 		// Wachstum des Markets bestimmen (in Abhngigkeit des Umsatzes)
 		increaseRandom(1, 16, random);
 		topLineMarket = (int) Math.ceil(increaseRandom[random] * topLineMarket);
 
 		// Schritt 3
 		// Ermittlung des Umsatz aller Unternehmen
 		// Marketingmanahmen und Preis werden bercksichtigt
 		// bei den Unternehmen Bercksichtigung durch Randomwerte
 		ownCompany.setTopLine(calculateTopLineOwnCompany(
 				version.getMarketing(), ownCompany.getTopLine(), ownCompany
 						.getProduct().getPrice(), version.getPrice()));
 
 		company1.setTopLine(calculateTopLineCompanies(company1));
 		company2.setTopLine(calculateTopLineCompanies(company2));
 		company3.setTopLine(calculateTopLineCompanies(company3));
 
 		// Schritt 4
 		// Ermittlung des Marktgesamtumsatzes anhand der in Schritt 3
 		// ermittelten Unternehmensumstze
 		topLineMarketArithmetic = calculateTopLineMarket(
 				topLineMarketArithmetic, ownCompany.getTopLine(),
 				company1.getTopLine(), company2.getTopLine(),
 				company3.getTopLine());
 
 		// Ermittlung der Unternehmensmarktanteile
 		ownCompany.setMarketShare(calculateMarketShare(topLineMarketArithmetic,
 				ownCompany.getTopLine(), ownCompany.getMarketShare()));
 		company1.setMarketShare(calculateMarketShare(topLineMarketArithmetic,
 				company1.getTopLine(), company1.getMarketShare()));
 		company2.setMarketShare(calculateMarketShare(topLineMarketArithmetic,
 				company2.getTopLine(), company2.getMarketShare()));
 		company3.setMarketShare(calculateMarketShare(topLineMarketArithmetic,
 				company3.getTopLine(), company3.getMarketShare()));
 
 		// Schritt 5
 		// Ermittlung der Unternehmensabsatzmengen
 		ownCompany.getProduct().setSalesVolume(calculateSalesVolume(topLineMarket, ownCompany.getMarketShare(),
 				ownCompany.getProduct().getPrice(), ownCompany.getProduct()
 						.getSalesVolume()));
 		company1.getProduct().setSalesVolume(calculateSalesVolume(topLineMarket, company1.getMarketShare(), company1
 				.getProduct().getPrice(), company1.getProduct()
 				.getSalesVolume()));
 		company2.getProduct().setSalesVolume(calculateSalesVolume(topLineMarket, company1.getMarketShare(), company1
 				.getProduct().getPrice(), company2.getProduct()
 				.getSalesVolume()));
 		company3.getProduct().setSalesVolume(calculateSalesVolume(topLineMarket, company1.getMarketShare(), company1
 				.getProduct().getPrice(), company3.getProduct()
 				.getSalesVolume()));
 
 		// berprfung, ob der Markt wchst oder sinkt
 		if (increaseRandom[random] > 1) {
 			// der Markt wchst == 1
 			version.setMarketIncrease(1);
 		} else if (increaseRandom[random] < 1) {
 			// der Markt sinkt == -1
 			version.setMarketIncrease(-1);
 		} else {
 			// der Markt verndert sich nicht == 0
 			version.setMarketIncrease(0);
 		} // Ende if-else
 
 		// Anzahl der neu eingestellten Mitarbeiter wird auf die
 		// Mitarbeiteranzahl des eigenen Unternehmens gerechnet
 		ownCompany.setNumberOfStaff(ownCompany.getNumberOfStaff()
 				+ version.getPersonal());
 		// Die Werte der neu gekauften Maschine werden auf die Werte der
 		// Maschine des eigenen Unternehmens gerechnet
 		ownCompany.getMachines().setCapacity(
 				version.getMachineCapacity()
 						+ ownCompany.getMachines().getCapacity());
 		ownCompany.getMachines()
 				.setStaff(
 						version.getMachineStaff()
 								+ ownCompany.getMachines().getStaff());
 		ownCompany.getMachines().setAccountingValue(
 				version.getMachineValue()
 						+ ownCompany.getMachines().getAccountingValue());
 
 		// Sonderfall 2
 		// Ermittelt, ob die Kapazitten der Maschinen ausgelastet sind
 		specialCaseTwo(version, ownCompany);
 
 		// Ermittlung der Maschinenabschreibungen
 		amortization = getAmortization(amortization, version.getMachineValue(),
 				ownCompany.getMachines());
 
 		// Schritt 6 Ausgaben berechnen
 		// Ermittlung des Gewinns des eigenen Unternehmens
 		amount = (int) Math.ceil(ownCompany.getTopLine()
 				- ownCompany.getFixedCosts() - ownCompany.getSalaryStaff()
 				* ownCompany.getNumberOfStaff() - version.getMarketing()
				- ownCompany.getVariableCosts() - amortization);
 
 		ownCompany.setAmount(amount);
 
 		return version;
 	} // Ende method simulate
 
 
 	public void specialCaseOne(SimulationVersion version) {
 		freePersonal = (ownCompany.getNumberOfStaff()+ version.getMachineStaff())
 				* 0.7
 				- (ownCompany.getMachines().getStaff() + version
 						.getMachineStaff());
 
 		if (freePersonal < 0) {
 			necessaryPersonal = (int) Math
 					.ceil((version.getMachineStaff() + ownCompany.getMachines()
 							.getStaff()) / 0.7 - ownCompany.getNumberOfStaff());
 
 			version.setNecessaryPersonal(necessaryPersonal);
 
 		} else{
 			version.setNecessaryPersonal(0);
 		}
 		
 		ownCompany.setNumberOfStaff(ownCompany.getNumberOfStaff() + version.getMachineStaff());
 
 	} // Ende method specialCaseOne
 
 	public void specialCaseTwo(SimulationVersion version, OwnCompany ownCompany) {
 
 		// Absatzmenge des eigenen Unternehmens berechnen
 		int salesVolume = calculateSalesVolume(ownCompany.getTopLine(),
 				ownCompany.getProduct().getPrice());
 
 		if (ownCompany.getMachines().getCapacity() < salesVolume) {
 			// Die Absatzmenge entspricht der 100% Maschinenauslastung
 			ownCompany.getProduct().setSalesVolume(
 					ownCompany.getMachines().getCapacity());
 			// Der Umsatz sinkt
 			ownCompany.setTopLine((int) Math.ceil(salesVolume
 					* ownCompany.getProduct().getPrice()));
 
 			version.setUnusedMachineCapacity(true);
 
 			// Marktanteil der Konkurrenzunternehmen
 			double marketSharesCompanies = company1.getMarketShare()
 					+ company2.getMarketShare() + company3.getMarketShare();
 			// Umrechnung der berschussabsatzmenge auf die
 			// Konkurrenzunternehmen
 			calculateAdditionalVolume(marketSharesCompanies, company1);
 			calculateAdditionalVolume(marketSharesCompanies, company2);
 			calculateAdditionalVolume(marketSharesCompanies, company3);
 
 			// Berechnen des neuen Gesamtmarktes
 			topLineMarket = calculateTopLineMarket(topLineMarket,
 					ownCompany.getTopLine(), company1.getTopLine(),
 					company2.getTopLine(), company3.getTopLine());
 			// neue Marktanteile berechen
 			// Ermittlung der Unternehmensmarktanteile
 			ownCompany.setMarketShare(calculateMarketShare(
 					topLineMarketArithmetic, ownCompany.getTopLine(),
 					ownCompany.getMarketShare()));
 			company1.setMarketShare(calculateMarketShare(
 					topLineMarketArithmetic, company1.getTopLine(),
 					company1.getMarketShare()));
 			company2.setMarketShare(calculateMarketShare(
 					topLineMarketArithmetic, company2.getTopLine(),
 					company2.getMarketShare()));
 			company3.setMarketShare(calculateMarketShare(
 					topLineMarketArithmetic, company3.getTopLine(),
 					company3.getMarketShare()));
 
 		} else {
 			ownCompany.getProduct().setSalesVolume(salesVolume);
 		} // Ende if-else
 			// Berechnen der Absatzmengen der Konkurrenzunternehmen
 		company1.getProduct().setSalesVolume(
 				calculateSalesVolume(company1.getTopLine(), company1
 						.getProduct().getPrice()));
 		company2.getProduct().setSalesVolume(
 				calculateSalesVolume(company2.getTopLine(), company2
 						.getProduct().getPrice()));
 		company3.getProduct().setSalesVolume(
 				calculateSalesVolume(company3.getTopLine(), company3
 						.getProduct().getPrice()));
 	} // Ende method specialCaseTwo
 
 	private int calculateSalesVolume(int topLine, double price) {
 		double topLineCalc = topLine;
 		int salesVolume = (int) Math.ceil(topLineCalc / price);
 		return salesVolume;
 	}
 
 	private Company calculateAdditionalVolume(double marketSharesCompanies,
 			Company company) {
 		double distributionKey = company.getMarketShare()
 				/ marketSharesCompanies;
 		double salesVolume = (ownCompany.getProduct().getSalesVolume() - ownCompany
 				.getMachines().getCapacity())
 				* ownCompany.getProduct().getPrice()
 				* distributionKey
 				+ company.getProduct().getSalesVolume();
 
 		company.getProduct().setSalesVolume((int) Math.ceil(salesVolume));
 
 		System.out.println("SalesVolume: " + salesVolume);
 
 		// neuer Umsatz des Unternehmens
 		company.setTopLine((int) (salesVolume * company.getProduct().getPrice()));
 		return company;
 
 	} // Ende method calculateAdditionalVolume
 
 	public static int calculateTopLineMarket(int topLineMarket,
 			int tLOwnCompany, int tLCompany1, int tLCompany2, int tLCompany3) {
 
 		topLineMarket = tLOwnCompany + tLCompany1 + tLCompany2 + tLCompany3;
 		return topLineMarket;
 	} // Ende method calculateTopLineMarket
 
 	public static int increaseRandom(int low, int high, int random) {
 		random = (int) Math.round(Math.random() * (high - low) + low);
 		return random;
 	} // Ende method increaseRandom
 
 	public static int calculateTopLineCompanies(Company company) {
 		int topLine = company.getTopLine();
 
 		// Marketing berechnen (anhand Random)
 		int random = (int) Math.ceil(Math.random() * 14 + 1);
 		double marketing = marketingRandom[random];
 
 		// Preisfaktor berechnen (anhand Random)
 		double priceFactor = Math.random() * (1.01 - 0.99) + 0.99;
 
 		// neuer Preis des Unternehmens ermitteln
 		company.getProduct().setPrice(
 				Math.ceil(company.getProduct().getPrice() / priceFactor));
 
 		// Umsatz berechnen
 		topLine = (int) Math.ceil(topLine * marketing * priceFactor);
 		return topLine;
 	} // Ende method calculateTopLineCompanies
 
 	public static int calculateTopLineOwnCompany(int marketingInput,
 			int topLine, double priceOld, double priceNew) {
 
 		double marketingInputCalc = marketingInput;
 		double topLineCalc = topLine;
 		// Werbeumsatzanstieg
 		double marketing = marketingInputCalc / topLineCalc;
 
 		if (marketing > 0.2) {
 			marketing = 1.2;
 		} else {
 			marketing = marketing + 1.0;// Ende if-Statement
 		}
 		// Preisnderung
 		if (priceNew == 0) {
 			priceNew = priceOld;
 		}
 		double priceFactor = priceOld / priceNew;
 		
 		topLine = (int) (Math.ceil(topLine * marketing * priceFactor));
 		return topLine;
 	} // Ende method calculateTopLineOwnCompany
 
 	public static double calculateMarketShare(int topLine, int topLineCompany,
 			double marketShare) {
 		double topLineCompanyCalc = topLineCompany;
 		double topLineCalc = topLine;
 		// TODO warum sind topLineCompanyCalc und topLineCalc 0?
 		marketShare = Math.round((marketShare + (topLineCompanyCalc / topLineCalc) * 100) * 0.5);
 		return marketShare;
 	} // Ende method calculateMarketShare
 
 	public static int calculateSalesVolume(int topLineMarket,
 			double marketShare, double price, int salesVolume) {
 		int realTopLine = (int) (topLineMarket * marketShare / 100);
 
 		salesVolume = (int) Math.ceil(realTopLine / price);
 		return salesVolume;
 	} // Ende method calculateSalesVolume
 
 	private static int getAmortization(int amortization, int machineValue,
 			Machines machine) {
 
 		amortization = (int) Math.ceil(machineValue / 10
 				+ machine.getAccountingValue() / machine.getServiceLife());
 		return amortization;
 	} // Ende method getAmortization
 } // Ende class MarketSimulation
