 
 public abstract class FFReactor {
 	private double tFinal;
 	private double kInit;
 	private double cCurr;
 	private double tCurr;
 	
 	public FFReactor()
 	{
 		this(1000.0, 0.1, 1.0);
 	}
 	
 	public FFReactor(double tFinal, double kInit, double cCurr)
 	{
 		this.settFinal(tFinal);
 		this.setkInit(kInit);
 		this.setCCurr(cCurr);
 		this.settCurr(0);
 	}
 
 	public abstract double getPercentageOfConcentrationLeft();
	public abstract void doReactionStep();
 	
 	public double gettCurr() {
 		return tCurr;
 	}
 
 	public void settCurr(double tCurr) {
 		this.tCurr = tCurr;
 	}
 
 	public double getCCurr() {
 		return cCurr;
 	}
 
 	public void setCCurr(double cCurr) {
 		this.cCurr = cCurr;
 	}
 
 	public double getkInit() {
 		return kInit;
 	}
 
 	public void setkInit(double kInit) {
 		this.kInit = kInit;
 	}
 
 	public double gettFinal() {
 		return tFinal;
 	}
 
 	public void settFinal(double tFinal) {
 		this.tFinal = tFinal;
 	}
 }
