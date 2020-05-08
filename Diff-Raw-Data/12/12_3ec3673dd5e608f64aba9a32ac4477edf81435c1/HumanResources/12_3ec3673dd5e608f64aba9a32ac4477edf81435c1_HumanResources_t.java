 package server;
 
 import java.util.ArrayList;
 
 import constant.Constant;
 
 /**
  * Created by: User: Lars Trey Date: 28.09.13 Time: 18:11
  */
 // TODO: ueberpruefung ob Benefits noch gueltig, entfernen aus bookedbenefits,
 // kosten die benefits per round verursachen
 // TODO alle wagelevel rechnungen / 10 000
 public class HumanResources extends DepartmentRoundSensitive {
 
 	// Lohn pro Runde pro Mitarbeiter
 	private TWage wagePerRound;
 
 	// Gesamtkosten Loehne
 	private int wagesSum;
 
 	// Liste der gebuchten Benefits
 	private ArrayList<BenefitBooking> benefitBooking = new ArrayList<BenefitBooking>();
 
 	// Anzahl der bereits gebuchten Arbeiterstunden
 	private int workingHoursPerRound = 0;
 
 	// ---------- Attribute zur Berechnung der Motivaiton
 	// Lohn der Letzten Runde
 	private TWage wageLastRound = null;
 
 	// Summe der Benefitinvestitionen der letzten Runde
 	private long sumBenefitLastRound = 0;
 
 	// Summe der Benefitinvestitionen dieser Runde
 	private long sumBenfit = 0;
 	
 	// Historie der Motivation
 	private ArrayList<TMotivation> historyMotivation = new ArrayList<TMotivation>();
 
 	public HumanResources(Company c) throws Exception {
 		super(c, "Personal", Constant.DepartmentFixcost.HUMAN_RESOURCES);
 
 		setWagePerRound(new TWage(c.getLocation().getInitWage(), GameEngine.getGameEngine().getRound(),c.getLocation().getWageLevel() )); 
 		this.wagesSum = calcWagesSum();
 		// TODO: BENEFIT BOOKING!?
 		// this.benefitBooking = new BenefitBooking();
 
 		// HR bei MarketData registrieren, um den durchschnittslohn zu
 		// uebermitteln
 		MarketData.getMarketData().addHR(this);
 
 	}
 
 	/**
 	 * Bucht ein Benefit ueber den Namen
 	 * 
 	 * @param name
 	 *            Name des Benefits
 	 * @param duration
 	 *            Dauer in Runden
 	 * @throws Exception
 	 *             Name muss ungleich null und nicht leer sein, duraiton > 0 und
 	 *             Das Benefit noch nicht gebucht worden sein
 	 */
 	public void bookBenefit(String name, int duration) throws Exception {
 
 		// Liefert das Benefit aus der Liste mit allen benefits zurueck
 		Benefit benefit = Benefit.getBenefitByName(name);
 
 		// Alle gebuchten Benefits durchsuchen
 		for (BenefitBooking bB : benefitBooking) {
 
 			// Abgelaufene Buchungen und welche, die nur in dieser Runde noch
 			// gueltig sind, sollen ignoriert werden
 			if (bB.getRemainingRounds() <= 1
 					&& bB.getStartInRound() <= GameEngine.getGameEngine()
 							.getRound())
 				continue;
 
 			// Auf Gleichheit ueberpruefne
 			if (bB.getBenefit().equals(benefit)) {
 				// TODO durch Nachricht ersetzen?!
 				throw new IllegalArgumentException("Beneift bereits gebucht");
 			}
 		}
 
 		// Benefit zur Liste der gebuchten hinzufuegen
 		benefitBooking.add(new BenefitBooking(benefit, duration));
 
 	}
 
 	private int calcWagesSum() {
 
 		return wagePerRound.getAmount() * this.getCountEmployees();
 	}
 
 	public void setWagePerRound(TWage wagePerRound) {
 
 		this.wagePerRound = wagePerRound;
 	}
 
 	public TWage getWagesPerHour() {
 
 		return wagePerRound;
 	}
 
 	public int getCountEmployees() {
 		// gibt die mindestanzahl der Arbeiter zurck
 		return ((int) (workingHoursPerRound / Constant.HumanResources.HR_WORK_WEEK) < (Constant.HumanResources.HR_EMPLOYEES)?Constant.HumanResources.HR_EMPLOYEES : (int) (workingHoursPerRound / Constant.HumanResources.HR_WORK_WEEK));
 	}
 
 	public int getWagesSum() {
 
 		return wagesSum;
 	}
 
 	public ArrayList<BenefitBooking> getBenefitBooking() {
 
 		return benefitBooking;
 	}
 
 	public void increaseWorkingHour(int quantity) {
 
 		workingHoursPerRound++;
 	}
 
 	public int getWorkingHours() {
 
 		return (workingHoursPerRound<Constant.HumanResources.HR_WORK_WEEK*Constant.HumanResources.HR_EMPLOYEES)?Constant.HumanResources.HR_WORK_WEEK*Constant.HumanResources.HR_EMPLOYEES:workingHoursPerRound;
 	}
 
 	@Override
 	public void prepareForNewRound(int round) throws Exception {
 
 		workingHoursPerRound = 0;
 
 		// Lohn der aktuellen Runde als Lohn der letzten Runde deklarieren
 		// Ab beginn der ersten Runde
 		if (wagePerRound != null)
 			wageLastRound = wagePerRound;
 
 		long sumBenefits = 0;
 
 		// Benefits vom Konto abbuchen
 		for (BenefitBooking bP : benefitBooking) {
 			// Nur Buchungen beruecksichtigen, die noch eine Restlaufzeit haben
 			// und entweder diese Runde beginnen oder bereits laufen
 			if (bP.getRemainingRounds() > 0 && bP.getStartInRound() <= round) {
 
 				// Betrag fr diese Runde vom Konto abbuchen
 				if (!getCompany().getBankAccount().decreaseBalance(
 						bP.getBenefit().getCostsPerRound())) {
 
 					// Benefitinvestitionen kummulieren
 					sumBenefits += bP.getBenefit().getCostsPerRound();
 				}
 
 			}
 		}
 
 		sumBenefitLastRound = this.sumBenfit;
 		this.sumBenfit = sumBenefits;
 	}
 	
 	/**
 	 * Liefert die History der Motivation zurueck
 	 * @return
 	 */
 	public ArrayList<TMotivation> getHistoryOfMotivation() {		
 		return historyMotivation;
 	}
 	
 	/**
 	 * Fuegt die Motivation der aktuellen Runde der Historie hinzu
 	 * @throws Exception
 	 */
 	public void refreshMotivationHistory() throws Exception {
 		historyMotivation.add( new TMotivation(getMotivation() , GameEngine.getGameEngine().getRound() ));
 	}
 
 	/**
 	 * Berechnet die Motivation in dieser Runde
 	 * 
 	 * @return Motivation in Prozent * 100
 	 * @throws Exception
 	 */
 	public int getMotivation() throws Exception {
 		// TODO: testen mit unterschieden zur letzten Runde!
 		// Gehaltsunterschied zur Vorrunde
 		double diffWageToLastRound = 1.0;
 
 		// Nur ab der zweiten Runde git es einen Unterschied
 		if (GameEngine.getGameEngine().getRound() > 1) {
 			// Unterschied in Prozent berechnen
 			// (Neuer Lohn) / (Alter Lohn)
 			diffWageToLastRound = (wagePerRound.getAmount())
 					/ (double) wageLastRound.getAmount();
 		}
 
 		// Einfluss auf die Motivation durch den Lohn zur Vorrunde
 		// Berechnung je nach Positiv oder Negativ unterscheiden
 		double influenceWageToLastRound = ((diffWageToLastRound < 1.0) ? 1 - Math
 				.pow(((diffWageToLastRound - 1) * (Constant.HumanResources.IMPACT_DIFF_NEG / 10.0)),
 						2)
 				: 1 + Math.sqrt((diffWageToLastRound - 1)
 						* (Constant.HumanResources.IMPACT_DIFF_POS / 10.0)));
 		
 		influenceWageToLastRound = ( influenceWageToLastRound > 1.45 ) ? 1.45 : influenceWageToLastRound;
 		
 		// Gehaltsunterschied zur Gruppe
 		// ( Eigener Lohn) / Durchschnitt
 		double averageWageAmount = MarketData.getMarketData().getAvereageWage()
 				.getAmount();
 		double diffWageToAverage = ((wagePerRound.getAmount() / (wagePerRound
 				.getWageLevel() / 10000.0))) / averageWageAmount;
 
 		// Einfluss auf die Motivaiton durch den Unterschied zum Markt
 		// Berechnung je nach Positiv oder Negative unterschiedlich
 		double influenceWageToAverage = ((diffWageToAverage < 1.0) ? 1 - Math
 				.pow(((diffWageToAverage - 1) * (Constant.HumanResources.IMPACT_DIFF_NEG / 10.0)),
 						2)
 				: 1 + Math.sqrt((diffWageToAverage - 1)
 						* (Constant.HumanResources.IMPACT_DIFF_POS / 10.0)));
 
 		influenceWageToAverage = ( influenceWageToAverage > 1.45 ) ? 1.45 : influenceWageToAverage;
 		
 		// Unterschied wird ab der zweiten Runde berechnet
 		double diffBenefitToLastRound = 1.0;
		
		sumBenfit = getSumBenefits();
 
 		if (GameEngine.getGameEngine().getRound() > 1) {
 			// Wenn weder Benfits in dieser Runde noch in der letzten
 			// existieren, bleibt die das verhaeltnis 100%
 			if (!(sumBenefitLastRound == 0 && sumBenfit == 0)) {
 				// Wenn diese Runde Benefits hinzugekommen sind, in der letzten
 				// Runde keine existierten, dann wird die letzte Rund als 1 cent
 				// betrachtet, um Div0 zu verhindern
 				if (sumBenefitLastRound == 0) {
 					sumBenefitLastRound = 1;
 				}
 				diffBenefitToLastRound = (sumBenfit)
 						/ (double) sumBenefitLastRound;
 			}
 		}
 
 		// Einfluss auf die Motivaiton berechnen
 		double influenceBenefitToLastRound = ((diffBenefitToLastRound < 1.0) ? 1 - Math
 				.pow(((diffBenefitToLastRound - 1) * (Constant.HumanResources.IMPACT_DIFF_NEG / 10.0)),
 						2)
 				: 1 + Math.sqrt((diffBenefitToLastRound - 1)
 						* (Constant.HumanResources.IMPACT_DIFF_POS / 10.0)));
 
 		influenceBenefitToLastRound = ( influenceBenefitToLastRound > 1.45 ) ? 1.45 : influenceBenefitToLastRound;
 		
 		// Unterschied zur Gruppe berechnen
 		double averageBenefit = MarketData.getMarketData().getAverageBenefit();
 		double diffBenefitToAverage = 1.0;
 
 		if (averageBenefit > 0.0) {
 			diffBenefitToAverage = ((sumBenfit / (getCompany().getLocation()
 					.getWageLevel() / 10000.0))) / averageBenefit;
 		}
 
 		// Einfluss auf die Motivation berechnen
 		double influenceBenefitToAverage = ((diffBenefitToAverage < 1.0) ? 1 - Math
 				.pow(((diffBenefitToAverage - 1) * (Constant.HumanResources.IMPACT_DIFF_NEG / 10.0)),
 						2)
 				: 1 + Math.sqrt((diffBenefitToAverage - 1)
 						* (Constant.HumanResources.IMPACT_DIFF_POS / 10.0)));
 		
 		influenceBenefitToAverage = ( influenceBenefitToAverage > 1.45 ) ? 1.45 : influenceBenefitToAverage;
 
 		// Motivation gewichtet berechnen TODO: mal checken
 		double motivation = (influenceWageToLastRound
 				* (Constant.HumanResources.IMPACT_DIFF_INTERNAL / 100.0) + influenceWageToAverage
 				* (Constant.HumanResources.IMPACT_DIFF_MARKET / 100.0))
 				* (Constant.HumanResources.HR_FACTOR_WAGE / 100.0)
 				+ (influenceBenefitToLastRound
 						* (Constant.HumanResources.IMPACT_DIFF_INTERNAL / 100.0) + influenceBenefitToAverage
 						* (Constant.HumanResources.IMPACT_DIFF_MARKET / 100.0))
 				* (Constant.HumanResources.HR_FACTOR_BENEFIT / 100.0);
 
 		motivation = ( motivation <= 0.0) ? 0.0 : motivation;
 		
 		return (int) (motivation * 10000); 
 	}
 
 	/**
 	 * Liefert die Summe der Benefitinvestitionen zurueck. Umgerechnet auf
 	 * Lohnniveau 100
 	 * 
 	 * @return Lohnniveauinvestionen auf Niveau 100
 	 */
 	public long getSumBenefits() {
		sumBenfit = 0;
		for(BenefitBooking b : benefitBooking) {
			if( b.getRemainingRounds() > 0 ) {
				sumBenfit += b.getBenefit().getCostsPerRound();
			}
		}
 		return (long) Math.floor(sumBenfit
 				/ (getCompany().getLocation().getWageLevel() / 10000.0));
 	}
 	@Override
 	public int getFixCosts(){
 		//Standardmig: 40 Leute Stammbelegschaft: (40 Stunden woche)
		return this.getWagesPerHour().getAmount() * this.getWorkingHours();
 		 
 	}
 }
