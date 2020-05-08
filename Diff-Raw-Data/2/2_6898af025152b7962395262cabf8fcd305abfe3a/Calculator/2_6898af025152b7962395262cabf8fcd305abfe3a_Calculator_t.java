 package au.edu.qut.inn372.greenhat.bean;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.ksoap2.serialization.SoapObject;
 
 public class Calculator extends AndroidAbstractBean implements Serializable {
 	
 	private static final long serialVersionUID = 5951374471213496241L;
 
 	private Equipment equipment;
 	
 	private Customer customer;
 	
 	private Calculation [] calculations;
 	
 	private Calculation calculation;
 	
 	/**
 	 * Datetime of the latest calculator's update
 	 */
 	private Date datetime;
 	
 	/**
 	 * Calculator's Name.
 	 * For example sellers can name a calculator using customer name, e.g. Calc_John
 	 * Customer can use names to remember the calculator, e.g. Calc01, MyCalc, etc
 	 */
 	private String name;
 	
 	/**
 	 * Calculator status.
 	 * 0 = incomplete
 	 * 1 = complete
 	 * 2 = template
 	 */
 	private int status = 0; //0=incomplete, 1=complete, 2=template
 	
 	/**
 	 * Unique identifier used by Google data store
 	 */
 	private String key;
 	
 	/**
 	 * Return the Calculator status name
 	 * 0 = incomplete
 	 * 1 = complete
 	 * 2 = template
 	 * @return
 	 */
 	public String getStatusName(){
 		switch (this.status) {
 		case 0:
 			return "Incomplete";
 		case 1:
 			return "Complete";
 		case 2:
 			return "Template";
 		default:
 			return "unknow";
 		}
 	}
 	
 	
 	
 	/**
 	 * Formats the date time to Brisbane TimeZone
 	 * 
 	 * TODO: date format should be based on customer location
 	 * 
 	 * @return
 	 */
 	public String getFormatedDateTime(){
 		DateFormat dateFormat = new SimpleDateFormat("MM/dd kk:mm");
 		try {
 			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+10:00"));//Brisbane TimeZone
 			return dateFormat.format(this.datetime);			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "unknown";
 		}
 	}
 	
 	
 	
 	
 	public Calculator() {
 		equipment = new Equipment();
 		customer = new Customer();
 		calculations = new Calculation[25];
 	}
 	
 	
 	public Calculator(SoapObject soapObject, int soapOperation){
 		calculations = new Calculation[25];
 		if (soapObject != null)
 			switch (soapOperation) {
 			case AndroidAbstractBean.OPERATION_CALC_ENERGY_PRODUCTION:
 				//Iterate through soap objects
 				soapObject = (SoapObject)soapObject.getProperty(0);
 				int calculationsCount = 0;
 				for(int i=0; i<soapObject.getPropertyCount(); i++) {
 					if (i<25) {
 						//calculations
 						SoapObject curSoapObject = (SoapObject)soapObject.getProperty(i);
 						this.calculations[calculationsCount] = new Calculation(curSoapObject, soapOperation);
 						calculationsCount++;
 					}
 					else {
 						if (i<26) {
 							//customer
 							//this.customer = new Customer(curSoapObject, soapOperation) //Should implement this but can save time by doing this later
 						}
 						else {
 							//equipment
 							//this.equipment = new Equipment(curSoapObject, soapOperation) //Should implement this but can save time by doing this later
 						}
 					}
 				}
 				break;
 			case AndroidAbstractBean.OPERATION_SAVE_CALCULATION:
 				break;
 			case AndroidAbstractBean.OPERATION_GET_CALCULATIONS:
 				//To save time, for now we don't unmarshal the calculations in this operation because loading a calculation to edit it means you only need the inputs - the calculations will be reperformed if the user wants to look at the output
 				//chart field not needed
 				this.customer = new Customer((SoapObject)soapObject.getProperty("customer"), soapOperation);
 				try {
 					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
					dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));//UTC
 					this.datetime = dateFormat.parse(soapObject.getProperty("datetime").toString());
 				} catch (ParseException e) {
 					System.out.println("Error parsing datetime");
 					e.printStackTrace();
 				}
 				this.equipment = new Equipment((SoapObject)soapObject.getProperty("equipment"), soapOperation);
 				this.key = soapObject.getProperty("key").toString();
 				this.name = soapObject.getProperty("name").toString();
 				this.status = new Integer(soapObject.getProperty("status").toString());
 				break;
 			case AndroidAbstractBean.OPERATION_DELETE_CALCULATION:	
 				break;
 			default:
 				break;
 			}		
 		
 	}
 	
 	
 	
 	/**
 	 * Get the customer
 	 * @return customer value of the customer property
 	 */
 	public Customer getCustomer() {
 		return customer;
 	}
 
 	/**
 	 * Set the customer
 	 * @param customer value for the customer property
 	 */
 	public void setCustomer(Customer customer) {
 		this.customer = customer;
 	}
 
 	/**
 	 * Set the equipment
 	 * @param equipment value for the equipment property
 	 */
 	public void setEquipment(Equipment equipment) {
 		this.equipment = equipment;
 	}
 	
 	/**
 	 * Get the equipment
 	 * @return equipment value of the equipment property
 	 */
 	public Equipment getEquipment() {
 		return equipment;
 	}
 
 	/**
 	 * Get the calculation
 	 * @return the calculation
 	 */
 	public Calculation getCalculation() {
 		return calculation;
 	}
 
 	/**
 	 * Set the calculation
 	 * @param calculation the calculation to set
 	 */
 	public void setCalculation(Calculation calculation) {
 		this.calculation = calculation;
 	}
 
 	/**
 	 * Get the calculations for a specified range of years
 	 * @return the calculations
 	 */
 	public Calculation[] getCalculations() {
 		return calculations;
 	}
 	/**
 	 * Sets the calculations 
 	 * @param calculations
 	 */
 	public void setCalculations(Calculation[] calculations) {
 		this.calculations = calculations;
 	}
 	
 
 	public Date getDatetime() {
 		return datetime;
 	}
 
 
 	public void setDatetime(Date datetime) {
 		this.datetime = datetime;
 	}
 
 
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Gets the calculator's status
 	 * @return status
 	 */
 	public int getStatus() {
 		return status;
 	}
 	
 	/**
 	 * Sets the calculator's status
 	 * @param status
 	 */
 	public void setStatus(int status) {
 		this.status = status;
 	}
 	
 	/**
 	 * Gets the calculator's database key
 	 * @return database key
 	 */
 	public String getKey() {
 		return key;
 	}
 	/**
 	 * Sets the calculator's database key
 	 * @param key
 	 */
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 
 	/**
 	 * ATTENTION: ALL VALUES MUST BE CONVERTED TO STRING
 	 */
 	@Override
 	protected void setSoapObject(int soapOperation) {
 		SoapObject calculatorElement;
 		switch (soapOperation) {
 		case AndroidAbstractBean.OPERATION_CALC_ENERGY_PRODUCTION:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_CALC_ENERGY_PRODUCTION_NAME);
 			calculatorElement = new SoapObject("", "calculator");
 			calculatorElement.addSoapObject(customer.getSoapObject(-1));
 			calculatorElement.addSoapObject(equipment.getSoapObject(-1));
 			this.soapObject.addSoapObject(calculatorElement);
 			break;
 		case AndroidAbstractBean.OPERATION_SAVE_CALCULATION:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_SAVE_CALCULATION_NAME);
 			calculatorElement = new SoapObject("", "calculator");
 			calculatorElement.addSoapObject(customer.getSoapObject(-1));
 			calculatorElement.addSoapObject(equipment.getSoapObject(-1));
 			if(name!=null) {
 				calculatorElement.addProperty("name", ""+this.name);
 			}
 			calculatorElement.addProperty("status", ""+this.status);
 			if(key!=null) {
 				calculatorElement.addProperty("key", ""+this.key);
 			}
 			this.soapObject.addSoapObject(calculatorElement);
 			break;
 		case AndroidAbstractBean.OPERATION_GET_EQUIPMENTS:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_GET_EQUIPMENTS_NAME);
 			calculatorElement = new SoapObject("", "calculator");
 			calculatorElement = setDefaultSoapObject(calculatorElement);
 			this.soapObject.addSoapObject(calculatorElement);
 			break;
 		case AndroidAbstractBean.OPERATION_DELETE_CALCULATION:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_DELETE_CALCULATION_NAME);
 			calculatorElement = new SoapObject("", "calculator");
 			calculatorElement = setDefaultSoapObject(calculatorElement);
 			this.soapObject.addSoapObject(calculatorElement);
 			break;
 		default:
 			this.soapObject = new SoapObject("", "calculator");
 			calculatorElement = new SoapObject("", "calculator");
 			calculatorElement = setDefaultSoapObject(calculatorElement);
 			this.soapObject.addSoapObject(calculatorElement);
 			break;
 		}
 	}
 	
 	/**
 	 * Adds all object properties to the given soap object and returns it
 	 * @param currentSoapObject Soap object that fields are added to
 	 * @return Amended soap object with fields
 	 */
 	private SoapObject setDefaultSoapObject(SoapObject currentSoapObject) {
 		currentSoapObject.addSoapObject(equipment.getSoapObject(-1));
 		currentSoapObject.addSoapObject(customer.getSoapObject(-1));
 		currentSoapObject.addProperty("name", ""+this.name);
 		currentSoapObject.addProperty("status", ""+this.status);
 		currentSoapObject.addProperty("key", ""+this.key);
 		//Add datetime
 		//Check the right way of marshalling this
 		//for(Calculation curCalculation : this.getCalculations()) {
 		//	currentSoapObject.addSoapObject(curCalculation.getSoapObject(-1));
 		//}
 		return currentSoapObject;
 	}
 	
 }
