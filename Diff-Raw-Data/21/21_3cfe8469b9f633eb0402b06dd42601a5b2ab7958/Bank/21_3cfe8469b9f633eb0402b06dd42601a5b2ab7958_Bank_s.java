 package au.edu.qut.inn372.greenhat.bean;
 
 import java.io.Serializable;
 
 import org.ksoap2.serialization.SoapObject;
 
 
 public class Bank extends AndroidAbstractBean implements Serializable {
 	
 	private static final long serialVersionUID = 295341398887435456L;
 	private int bankId;
 	private double angle;
 	private String selectedOrientation;
 	private double efficiency;
 	private int numberOfPanels;
 	private double orientationEfficiencyLoss;
 	private double angleEfficiencyLoss;
 	private double powerOutput;
 	private double orientation;
 	
 	public Bank() {}
 	
 	public Bank(SoapObject soapObject, int soapOperation){
 		if (soapObject != null)
 			switch (soapOperation) {
 			case AndroidAbstractBean.OPERATION_GET_CALCULATIONS:
 			default:
 				this.bankId = new Integer(soapObject.getProperty("bankId").toString());
 				this.angleEfficiencyLoss = new Double(soapObject.getProperty("angleEfficiencyLoss").toString());
 				this.angle = new Double(soapObject.getProperty("angle").toString());
 				this.efficiency = new Double(soapObject.getProperty("efficiency").toString());
 				this.numberOfPanels = new Integer(soapObject.getProperty("numberOfPanels").toString());
 				this.orientationEfficiencyLoss = new Double(soapObject.getProperty("orientationEfficiencyLoss").toString());
 				this.powerOutput = new Double(soapObject.getProperty("powerOutput").toString());
 				this.orientation = new Double(soapObject.getProperty("orientation").toString());
 				break;
 			}
 	}
 	
 	/**
 	 * Get the bankId
 	 * @return bankId value of the bankId property
 	 */
 	public int getBankId(){
 		return bankId;
 	}
 	
 	/**
 	 * Set the bankId
 	 * @param bankId new value for the bankId property
 	 */
 	public void setBankId(int bankId){
 		this.bankId = bankId;
 	}
 	
 	/**
 	 * Get the angle
 	 * @return angle value of the angle property
 	 */
 	public double getAngle(){
 		return angle;
 	}
 	
 	/**
 	 * Set the angle
 	 * @param angle new value for the angle property
 	 */
 	public void setAngle(double angle){
 		this.angle = angle;
 	}
 
 	/**
 	 * Set the orientation list
 	 * @param orientation list new value for the orientation list property
 	 */
 	//public void setListOfOrientation(List<SelectItem> listOfOrientation) {
 	//	this.listOfOrientation = listOfOrientation;
 	//}
 	
 	/**
 	 * Get the selected orientation
 	 * @return selected orientation value of the selected orientation property
 	 */
 	public String getSelectedOrientation() { 
 		return selectedOrientation; 
 	} 
 	
 	/**
 	 * Set the selected orientation
 	 * @param selected orientation new value for the selected orientation property
 	 */
 	public void setSelectedOrientation(String selectedOrientation) { 
 		this.selectedOrientation = selectedOrientation; 
 	}
 
 	/**
 	 * @return the efficiency
 	 */
 	public double getEfficiency() {
 		return efficiency;
 	}
 
 	/**
 	 * @param efficiency the efficiency to set
 	 */
 	public void setEfficiency(double efficiency) {
 		this.efficiency = efficiency;
 	}
 
 	/**
 	 * @return the numberOfPanels
 	 */
 	public int getNumberOfPanels() {
 		return numberOfPanels;
 	}
 
 	/**
 	 * @param numberOfPanels the numberOfPanels to set
 	 */
 	public void setNumberOfPanels(int numberOfPanels) {
 		this.numberOfPanels = numberOfPanels;
 	}
 
 	/**
 	 * @return the orientationEfficiencyLoss
 	 */
 	public double getOrientationEfficiencyLoss() {
 		return orientationEfficiencyLoss;
 	}
 
 	/**
 	 * @param orientationEfficiencyLoss the orientationEfficiencyLoss to set
 	 */
 	public void setOrientationEfficiencyLoss(double orientationEfficiencyLoss) {
 		this.orientationEfficiencyLoss = orientationEfficiencyLoss;
 	}
 
 	/**
 	 * @return the angleEfficiencyLoss
 	 */
 	public double getAngleEfficiencyLoss() {
 		return angleEfficiencyLoss;
 	}
 
 	/**
 	 * @param angleEfficiencyLoss the angleEfficiencyLoss to set
 	 */
 	public void setAngleEfficiencyLoss(double angleEfficiencyLoss) {
 		this.angleEfficiencyLoss = angleEfficiencyLoss;
 	}
 
 	/**
 	 * @return the powerOutput
 	 */
 	public double getPowerOutput() {
 		return powerOutput;
 	}
 
 	/**
 	 * @param powerOutput the powerOutput to set
 	 */
 	public void setPowerOutput(double powerOutput){
 		this.powerOutput = powerOutput;
 	}
 
 	/**
 	 * @return the orientation
 	 */
 	public double getOrientation() {
 		return orientation;
 	}
 
 	/**
 	 * @param orientation the orientation to set
 	 */
 	public void setOrientation(double orientation) {
 		this.orientation = orientation;
 	}
 	
 	/**
 	 * ATTENTION: ALL VALUES MUST BE CONVERTED TO STRING
 	 */
 	@Override
 	protected void setSoapObject(int soapOperation) {
 		switch (soapOperation) {
 		case AndroidAbstractBean.OPERATION_CALC_ENERGY_PRODUCTION:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_CALC_ENERGY_PRODUCTION_NAME);
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		case AndroidAbstractBean.OPERATION_SAVE_CALCULATION:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_SAVE_CALCULATION_NAME);
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		default:
 			this.soapObject = new SoapObject("", "banks");
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		}
 	}
 	
 	
 	/**
 	 * Adds all object properties to the given soap object and returns it
 	 * @param currentSoapObject Soap object that fields are added to
 	 * @return Amended soap object with fields
 	 */
 	private SoapObject setDefaultSoapObject(SoapObject currentSoapObject) {
 		currentSoapObject.addProperty("bankId", ""+this.bankId);
 		currentSoapObject.addProperty("angle", ""+this.angle);
 		currentSoapObject.addProperty("selectedOrientation", ""+this.selectedOrientation);
		currentSoapObject.addProperty("efficiency", this.efficiency);
 		currentSoapObject.addProperty("numberOfPanels", ""+this.numberOfPanels);
 		currentSoapObject.addProperty("orientationEfficiencyLoss", ""+this.orientationEfficiencyLoss);
 		currentSoapObject.addProperty("angleEfficiencyLoss", ""+this.angleEfficiencyLoss);
 		currentSoapObject.addProperty("powerOutput", ""+this.powerOutput);
 		currentSoapObject.addProperty("orientation", ""+this.orientation);
 		return currentSoapObject;
 	}
 }
