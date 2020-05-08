 package au.edu.qut.inn372.greenhat.bean;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ksoap2.serialization.SoapObject;
 /**
  * Equipment Bean
  * 
  * @author Charleston Telles
  *
  */
 public class Equipment extends AndroidAbstractBean implements Serializable {
 
 	private static final long serialVersionUID = 5455126267096688150L;
 	private double cost;
 	private double size;
 	private String kitName = "";
 	
 	private List<Panel> panels = new ArrayList<Panel>();
 	
 	private Inverter inverter;
 	
 	private Battery battery;
 	
 	public Equipment(SoapObject equipmentSoap, int soapOperation) {
		if (equipmentSoap != null)
 			switch (soapOperation) {
 			case AndroidAbstractBean.OPERATION_GET_EQUIPMENTS:
 				this.cost = new Double(((equipmentSoap.getProperty("cost"))).toString());
 				this.size = new Double(((equipmentSoap.getProperty("size"))).toString());
 				this.kitName = new String(((equipmentSoap.getProperty("kitName"))).toString());
 				this.inverter = new Inverter((SoapObject)equipmentSoap.getProperty("inverter"), soapOperation);
 				this.battery = new Battery((SoapObject)equipmentSoap.getProperty("battery"), soapOperation);
 				SoapObject panels = ((SoapObject)(equipmentSoap.getProperty("panels"))); //this is only the first panel object!
 				//There are 5 properties besides the panel, so total number of properties - 5 = number of panels - this should change later as it is a bit of a hack
 				for (int curPanelIndex = 0; curPanelIndex < equipmentSoap.getPropertyCount() - 5; curPanelIndex++) {
 					this.panels.add(new Panel(panels, soapOperation));
 				}
 				break;
 			default:
 				break;
 			}		
 	}
 	
 	public Equipment() {
 		inverter = new Inverter();
 		battery = new Battery();
 	}
 	
 	//Added by Martin - what if customer wants to enter number of panels, instead of selecting from kit
 	//private int totalPanels;
 	/**
 	 * Get the inverter
 	 * @return the inverter value of the inverter property
 	 */
 	public Inverter getInverter() {
 		return inverter;
 	}
 	
 	/**
 	 * Set the inverter 
 	 * @param inverter new value for the inverter property
 	 */
 	public void setInverter(Inverter inverter) {
 		this.inverter = inverter;
 	}
 	
 	
 	/**
 	 * Gets Battery Bean
 	 * @return Battery
 	 */
 	public Battery getBattery() {
 		return battery;
 	}
 	/**
 	 * Sets Battery Bean
 	 * @param battery
 	 */
 	public void setBattery(Battery battery) {
 		this.battery = battery;
 	}
 	
 	/**
 	 * Get the panels
 	 * @return panels value of the panels property
 	 */
 	public List<Panel> getPanels() {
 		return panels;
 	}
 		
 	/**
 	 * Set the panels
 	 * @param panels new value for the panels property
 	 */
 	public void setPanels(List<Panel> panels) {
 		this.panels = panels;
 	}
 	/**
 	 * Add panel to Panel List
 	 * @param panel
 	 */
 	public void addPanel (Panel panel){
 		this.panels.add(panel);
 	}
 	/**
 	 * Remove panel from Panel List
 	 * @param panel
 	 */
 	public void removePanel (Panel panel){
 		this.panels.remove(panel);
 	}
 		
 	/**
 	 * Gets Kit name
 	 * @return kit name
 	 */
 	public String getKitName() {
 		return kitName;
 	}
 	/**
 	 * Sets kit name
 	 * 
 	 * @param kitName
 	 */
 	public void setKitName(String kitName) {
 		this.kitName = kitName;
 	}
 
 
 	/**
 	 * Get the selected panel
 	 * @return selected panel value of the selected panel property
 	 */
 	public int getTotalPanels() { 
 		if (this.panels != null)
 			return this.panels.size();
 		else
 			return 0;
 		//return totalPanels;
 	} 
 	
 	/**
 	 * @param totalPanels the totalPanels to set
 	 */
 	//public void setTotalPanels(int totalPanels) {
 		//this.totalPanels = totalPanels;
 	//}
 
 	/**
 	 * Get the cost
 	 * @return cost value of the cost property
 	 */
 	public double getCost() {
 		return cost;
 	}
 	
 	/**
 	 * Set the cost
 	 * @param cost value for the cost property
 	 */
 	public void setCost(double cost) {
 		this.cost = cost;
 	}
 	
 	/**
 	 * Get the size of the system
 	 * @return size value of the size property
 	 */
 	public double getSize() {
 		return size;
 	}
 	
 	/**
 	 * Set the size of the system
 	 * @param size value for the size property
 	 */
 	public void setSize(double size) {
 		this.size = size; 
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
 		case AndroidAbstractBean.OPERATION_GET_EQUIPMENTS:
 			this.soapObject = new SoapObject(AndroidAbstractBean.NAMESPACE,AndroidAbstractBean.OPERATION_GET_EQUIPMENTS_NAME);
 			this.soapObject = setDefaultSoapObject(this.soapObject);
 			break;
 		default:
 			this.soapObject = new SoapObject("", "equipment");
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
 		currentSoapObject.addProperty("size", ""+this.size);
 		currentSoapObject.addProperty("kitName", ""+this.kitName);
 		for(Panel curPanel : this.panels) {
 			currentSoapObject.addSoapObject(curPanel.getSoapObject(-1));
 		}
 		currentSoapObject.addSoapObject(this.inverter.getSoapObject(-1));
 		currentSoapObject.addSoapObject(this.battery.getSoapObject(-1));
 		return currentSoapObject;
 	}
 
 }
