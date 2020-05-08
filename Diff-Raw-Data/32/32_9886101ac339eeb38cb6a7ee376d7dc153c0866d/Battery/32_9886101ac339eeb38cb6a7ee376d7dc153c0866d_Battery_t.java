 package au.edu.qut.inn372.greenhat.bean;
 
 import java.io.Serializable;
 
 import org.ksoap2.serialization.SoapObject;
 
 /**
  * Bean that represents an Equipment
  * 
  * @author Charleston Telles
  * @version 1.0
  */
 public class Battery extends AndroidAbstractBean implements Serializable{
 
 	/**
 	 * Unique identifier
 	 */
 	private static final long serialVersionUID = -4373394917607346069L;
 	/**
 	 * Battery Cost
 	 */
 	private double cost;
 	public Battery(SoapObject batterySoap, int soapOperation) {
		if (batterySoap != null)
 			switch (soapOperation) {
 			case AndroidAbstractBean.OPERATION_GET_EQUIPMENTS:
				this.cost = new Double(batterySoap.getProperty("cost").toString());
 				break;
 			default:
 				break;
 			}	
 	}
 	public Battery() {
 	}
 	/**
 	 * Gets battery cost
 	 * @return cost
 	 */
 	public double getCost() {
 		return cost;
 	}
 	/**
 	 * Sets battery cost
 	 * @param cost
 	 */
 	public void setCost(double cost) {
 		this.cost = cost;
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
 			this.soapObject = new SoapObject("", "battery");
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
 		currentSoapObject.addProperty("cost", ""+this.cost);
 		return currentSoapObject;
 	}
 	
 
 }
