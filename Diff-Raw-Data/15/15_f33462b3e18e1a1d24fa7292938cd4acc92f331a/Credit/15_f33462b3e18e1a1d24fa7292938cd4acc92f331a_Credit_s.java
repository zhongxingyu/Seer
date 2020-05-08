 package Client.Entities;
 
 	import Client.Application.UnableToTakeCreditException;
 
 public class Credit {
 	
 	private double initialCreditHeight;	
 	private double creditLeft;
 
 	private int contractPeriod;
 	private int leftPeriods;
 	
 	private double anuity;
 	private boolean hasCredit;
 	
 	private double interestPercentage;
 	public final static double INTERESTPERPERIODINPERCENT = 0.5d;
 	public final static double BASEINTERESTINPERCENT = 5d;
 	
 	public Credit(double creditHeight, int contractPeriod) throws UnableToTakeCreditException{
 		//Dynamische Anpassung des Zinsatzes -> (Laufzeit * 0,5) + Basissatz von 5%... heit Kredit ber 10 Jahre hat 10% Zinsen 
 		this.interestPercentage = getPercentageForPeriods(contractPeriod);
 		
 		this.initialCreditHeight = creditHeight;
 		this.leftPeriods = contractPeriod;
 		
 		this.creditLeft = creditHeight;
 		this.contractPeriod = contractPeriod;
 		setAnuity(creditHeight);
		CanTakeCredit(creditHeight, contractPeriod);
 	}
 
 	public static double getPercentageForPeriods(int contractPeriod) {
 		return ((contractPeriod * INTERESTPERPERIODINPERCENT) + BASEINTERESTINPERCENT) / 100;
 	}
 
 	/**
 	 * Setzt die Annuitt (periodische Abzugsrate) gem folgender Logik:
 	 * Jahreswert: K * ((q^n)*(q-1))/((q^n)-1)
 	 * 
 	 * K -> Barwert des Darlehens
 	 * q -> Zinsatz + 1 (100% + Zinssatz) 
 	 * n -> Laufzeit des Darlehens pro Periode
 	 * @param creditHeight -> K
 	 * @param contractPeriod -> n
 	 */
 	private void setAnuity(double creditHeight) {
 		
 		this.anuity = creditHeight * (Math.pow(1+interestPercentage,contractPeriod)*interestPercentage)/(Math.pow(1+interestPercentage,contractPeriod)-1);
 	}
 
 	private void CanTakeCredit(double creditHeight, int contractPeriod) throws UnableToTakeCreditException {
		//TODO: Sind Kreditvorraussetzungen so ok?
 		//comment(lars): ber nen maximalwert msste man sich nochmal gedanken machen.
		//comment(lars): ich wrde sagen man kann auch 2 runden vor schluss nen kredit ber 10 runden aufnehmen, sonst muss man ja in der 2. periode die hlfte zurckzahlen wenn man ne maschine fr 5 jahre finanzieren will.
		if (creditHeight > 9000000)
 			throw new UnableToTakeCreditException(UnableToTakeCreditException.TakeCreditReason.CreditTooHigh);
		//if (contractPeriod > (PeriodInfo.getMaxPeriods()- PeriodInfo.getNumberOfActPeriod()))
			//throw new UnableToTakeCreditException(UnableToTakeCreditException.TakeCreditReason.PeriodLongerThanPlaytime);
		if (contractPeriod > 10)
 			throw new UnableToTakeCreditException(UnableToTakeCreditException.TakeCreditReason.PeriodTooLong);
 		
 	}
 
 	/**
 	 * Senkt den genommenen Kredit um die errechnete Tilgung und gibt zurck ob die Gesamtsumme getilgt wurde
 	 * @return true => Kredit wurde vollstndig zurckbezahlt
 	 */
 	public boolean payInterestAndRepayment(){
 		double interestPayment = getInterestPayment();
 		//Logging
 		PeriodInfo.getActualPeriod().setInterestPayment(interestPayment);
 		
 		double repayment = (anuity - interestPayment);
 		leftPeriods--;
 		//if((int) repayment >=  (int) creditLeft - 1){
 		if (leftPeriods == 0) {
 			creditLeft = 0;
 			return true;
 		}
 		
 		creditLeft -= repayment;
 		return false;
 	}
 	
 	/**
 	 * Gibt den zu zahlenen Betrag (Anuitt) zurck
 	 * @return Anuitt
 	 */
 	public double getInitialCreditHeight(){
 		return initialCreditHeight;
 	}
 	
 	public double getAnuity(){
 		return anuity;
 	}
 	
 	public double getInterestPercentage() {
 		return interestPercentage;
 	}
 	
 	public double getInterestPayment() {
 		return (creditLeft * interestPercentage);
 	}
 
 	public double getCreditLeft() {
 		return creditLeft;
 	}
 	
 	public double getLeftPeriods() {
 		return leftPeriods;
 	}
 	
 
 }
