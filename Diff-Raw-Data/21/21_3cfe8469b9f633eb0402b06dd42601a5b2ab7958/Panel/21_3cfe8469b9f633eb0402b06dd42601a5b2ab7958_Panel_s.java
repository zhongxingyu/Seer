 package au.edu.qut.inn372.greenhat.bean;
 
 import java.io.Serializable;
 
 import org.ksoap2.serialization.SoapObject;
 
 /**
  * 
  * Panel Bean
  * 
  * @author Fabian Kohl
  *
  */
 
 public class Panel extends AndroidAbstractBean implements Serializable {
 	
 	private static final long serialVersionUID = -3846338865698898556L;
 	private int id;
 	private double width;
 	private double height;
 	private double efficiency;
 	private double efficiencyLoss = 0.7;  //TODO: Hardcoded Value!
 	
 	private double powerRating;
 	private double size; 
 	private double cost;
 	private String brand;
 	
 	
 	public Panel(SoapObject panelSoap, int soapOperation) {
 		if (panelSoap != null)
 			switch (soapOperation) {
 			case AndroidAbstractBean.OPERATION_GET_EQUIPMENTS: case AndroidAbstractBean.OPERATION_GET_PANELS:
 			default:
 				this.cost = new Double(panelSoap.getProperty("cost").toString());
 				this.efficiency = new Double(panelSoap.getProperty("efficiency").toString());
 				this.efficiencyLoss = new Double(panelSoap.getProperty("efficiencyLoss").toString());
 				this.height = new Double(panelSoap.getProperty("height").toString());
 				this.id = new Integer(panelSoap.getProperty("id").toString());
 				this.powerRating = new Double(panelSoap.getProperty("powerRating").toString());
 				this.size = new Double(panelSoap.getProperty("size").toString());
 				this.width = new Double(panelSoap.getProperty("width").toString());
 				this.brand = new String(panelSoap.getProperty("brand").toString());
 				break;
 			}		
 	}
 
 	public Panel() {
 	}
 
 	/**
 	 * Get the id
 	 * @return id of the panel
 	 */
 	public int getId() {
 		return id;
 	}
 	
 	/**
 	 * Set the id
 	 * @param id new value for the panel id
 	 */
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Get the width
 	 * @return width of the panel
 	 */
 	public double getWidth() {
 		return width;
 	}
 	
 	/**
 	 * Set the width
 	 * @param width new width for the panel
 	 */
 	public void setWidth(double width) {
 		this.width = width;
 	}
 	
 	/**
 	 * Get the height
 	 * @return height of the panel
 	 */
 	public double getHeight() {
 		return height;
 	}
 	
 	/**
 	 * Set the height
 	 * @param height new height for the panel
 	 */
 	public void setHeight(double height) {
 		this.height = height;
 	}
 	
 	/**
 	 * Get the efficiency
 	 * @return efficiency of the panel
 	 */
 	public double getEfficiency() {
 		return efficiency;
 	}
 	
 	/**
 	 * Set the efficiency
 	 * @param efficiency new panel efficiency
 	 */
 	public void setEfficiency(double efficiency) {
 		this.efficiency = efficiency;
 	}
 	
 	/**
 	 * Get the efficiency
 	 * @return efficiencyLoss of the panel
 	 */
 	public double getEfficiencyLoss() {
 		return efficiencyLoss;
 	}
 	
 	/**
 	 * Set the efficiency loss
 	 * @param efficiencyLoss new efficiency loss of the panel
 	 */
 	public void setEfficiencyLoss(double efficiencyLoss) {
 		this.efficiencyLoss = efficiencyLoss;
 	}
 
 	/**
 	 * @return the panelPower
 	 */
 	public double getPowerRating() {
 		return powerRating;
 	}
 
 	/**
 	 * @param panelPower the panelPower to set
 	 */
 	public void setPowerRating(double panelPower) {
 		this.powerRating = panelPower;
 	}
 
 
 
 	/**
 	 * @return the size
 	 */
 	public double getSize() {
 		return size;
 	}
 
 	/**
 	 * @param size the size to set
 	 */
 	public void setSize(double size) {
 		this.size = size;
 	}
 
 	/**
 	 * @return the cost
 	 */
 	public double getCost() {
 		return cost;
 	}
 
 	/**
 	 * @param cost the cost to set
 	 */
 	public void setCost(double cost) {
 		this.cost = cost;
 	}
 
 	/**
 	 * @return the brand
 	 */
 	public String getBrand() {
 		return brand;
 	}
 
 	/**
 	 * @param brand the brand to set
 	 */
 	public void setBrand(String brand) {
 		this.brand = brand;
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
 		case AndroidAbstractBean.OPERATION_GET_PANELS:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_GET_PANELS_NAME);
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		default:
 			this.soapObject = new SoapObject("", "panels");
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
 		currentSoapObject.addProperty("id", ""+this.id);
 		currentSoapObject.addProperty("width", ""+this.width);
 		currentSoapObject.addProperty("height", ""+this.height);
		currentSoapObject.addProperty("efficiency", ""+this.efficiency);
 		currentSoapObject.addProperty("efficiencyLoss", ""+this.efficiencyLoss);
 		currentSoapObject.addProperty("powerRating", ""+this.powerRating);
 		currentSoapObject.addProperty("size", ""+this.size);
 		currentSoapObject.addProperty("cost", ""+this.cost);
 		currentSoapObject.addProperty("brand", ""+this.brand);
 		return currentSoapObject;
 	}
 }
