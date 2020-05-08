 
 public class FFCSTReactor extends FFReactor 
 {	
 	private double cIn;
 	private double flowRate;
 	private double reactorVolume;
 	private double defaultConcentration;
 	
 	public FFCSTReactor()
 	{
 		this(1000.0, 1.0, 0.001, 1.0, 100.0, 1.0);
 	}
 	
 	public FFCSTReactor(double tFinal, double cIn, double kInit, double flowRate, double reactorVolume, double defaultConcentration)
 	{
 		super(tFinal, kInit, 0);
 		this.setcIn(cIn);
 		this.setFlowRate(flowRate);
 		this.setReactorVolume(reactorVolume); 
 		this.setDefaultConcentration(defaultConcentration);
 	}
 	
	public double getCCurrAndDecrement()
 	{
 		setCCurr(getCCurr() * (1 - flowRate / reactorVolume) + cIn * (flowRate / reactorVolume));
 		setCCurr(getCCurr() - (getkInit() * getCCurr()));
		return getCCurr();
 	}
 	
 	public double getPercentageOfConcentrationLeft(){
		return getCCurrAndDecrement() / defaultConcentration;
 	}
 
 	public double getFlowRate() {
 		return flowRate;
 	}
 
 	public void setFlowRate(double flowRate) {
 		this.flowRate = flowRate;
 	}
 
 	public double getcIn() {
 		return cIn;
 	}
 
 	public void setcIn(double cIn) {
 		this.cIn = cIn;
 	}
 
 	public double getReactorVolume() {
 		return reactorVolume;
 	}
 
 	public void setReactorVolume(double reactorVolume) {
 		this.reactorVolume = reactorVolume;
 	}
 
 	public double getDefaultConcentration() {
 		return defaultConcentration;
 	}
 
 	public void setDefaultConcentration(double defaultConcentration) {
 		this.defaultConcentration = defaultConcentration;
 	}
 }
