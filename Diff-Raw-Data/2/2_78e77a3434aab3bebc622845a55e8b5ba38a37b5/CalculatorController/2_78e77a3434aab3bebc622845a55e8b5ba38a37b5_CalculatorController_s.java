 package au.edu.qut.inn372.greenhat.controller;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import org.primefaces.event.SelectEvent;
 import org.primefaces.event.TabChangeEvent;
 
 import au.edu.qut.inn372.greenhat.bean.Bank;
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.bean.Customer;
 import au.edu.qut.inn372.greenhat.bean.Equipment;
 import au.edu.qut.inn372.greenhat.bean.Location;
 import au.edu.qut.inn372.greenhat.bean.Panel;
 import au.edu.qut.inn372.greenhat.bean.UserProfile;
 import au.edu.qut.inn372.greenhat.dao.CalculatorDAO;
 import au.edu.qut.inn372.greenhat.dao.EquipmentDAO;
 import au.edu.qut.inn372.greenhat.dao.LocationDAO;
 import au.edu.qut.inn372.greenhat.dao.PanelDAO;
 import au.edu.qut.inn372.greenhat.dao.UserProfileDAO;
 import au.edu.qut.inn372.greenhat.dao.gae.CalculatorDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.EquipmentDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.LocationDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.PanelDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.UserProfileDAOImpl;
 
 /**
  * Bean that represents a Calcualtor Controller
  * @author Charleston Telles
  * @version 1.0
  */
 @ManagedBean
 @SessionScoped
 public class CalculatorController implements Serializable {
 
 	private static final long serialVersionUID = 8091277788980459284L;
 	private static final int customerUsageIndex = 1;
 	private static final int equipmentTabIndex = 2;
 	private static final int roofTabIndex = 3;
 	private static final int locationTabIndex = 0;
 	private static final int summaryTabIndex = 4;
 	
 	@ManagedProperty (value = "#{calculator}")
 	private Calculator calculator;
 	
 	private CalculatorDAO calculatorDAO = new CalculatorDAOImpl();
 	private UserProfileDAO userProfileDAO = new UserProfileDAOImpl();
 	
 	//private List<Equipment> equipments = new ArrayList<Equipment>();
 	//private EquipmentDataModel equipmentDataModel;
 	private Map<String,String> equipments = new HashMap<String, String>();
 	List<Equipment> listEquipments;
 	private Equipment selectedEquipment; 
 	
 	private Map<String, String> locations = new HashMap<String, String>();
 	List<Location> listLocations;
 	
 	private List<Panel> panelList = new ArrayList<Panel>();
 	private Panel selectedPanel;
 	
 	private int tabIndex = 0;	
 	private String responseMessage = "";
 	
 	private CalculatorDataModel savedCalculators;
 	private Calculator[] selectedCalculators;  
 	
 	public CalculatorController(){
 		EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
 		//equipments = equipmentDAO.getEquipments();
 		//equipmentDataModel = new EquipmentDataModel(equipments);
 		listEquipments = equipmentDAO.getEquipments();
 		for (Equipment equipment : listEquipments) {
 			this.equipments.put(equipment.getKitName(), equipment.getKitName());
 		}
 		
 		LocationDAO locationDAO = new LocationDAOImpl();
 		listLocations = locationDAO.getLocations();
 		for(Location location : listLocations){
 			this.locations.put(location.getCity(), location.getCity());
 		}
 		
 		PanelDAO panelDAO = new PanelDAOImpl();
 		panelList = panelDAO.getPanels();
 	}
 	
 	/**
 	 * Gets a list of saved calculators
 	 * @return
 	 */
 	public CalculatorDataModel getSavedCalculators() {
 		this.savedCalculators = new CalculatorDataModel(calculatorDAO.getAllByUserProfile(calculator.getCustomer().getUserProfile()));
 		return savedCalculators;
 	}
 	/**
 	 * Sets a list of saved calculators 
 	 * @param savedCalculators
 	 */
 	public void setSavedCalculators(CalculatorDataModel savedCalculators) {
 		this.savedCalculators = savedCalculators;
 	}
 
 	/**
 	 * Gets a list of selected calculators from UI
 	 * @return
 	 */
 	public Calculator[] getSelectedCalculators() {
 		return selectedCalculators;
 	}
 
 	/**
 	 * Sets a list of selected calculators to UI
 	 * @param selectedCalculators
 	 */
 	public void setSelectedCalculators(Calculator[] selectedCalculators) {
 		this.selectedCalculators = selectedCalculators;
 	}
 
 
 	/**
 	 * Gets the Response Message
 	 * @return response message
 	 */
 	public String getResponseMessage() {
 		return responseMessage;
 	}
 
 	/**
 	 * Sets the response message
 	 * @param responseMessage
 	 */
 	public void setResponseMessage(String responseMessage) {
 		this.responseMessage = responseMessage;
 	}
 
 
 	/**
 	 * Get the calculator
 	 * @return calculator value of the calculator property
 	 */
 	public Calculator getCalculator() {
 		return calculator;
 	}
 	/**
 	 * Set the calculator
 	 * @param calculator value for the calculator property
 	 */
 	public void setCalculator(Calculator calculator) {
 		this.calculator = calculator;
 		setDefaultEquipmentSelection();
 	}
 	
 	/**
 	 * First equipment kit selected by default
 	 */
 	public  void setDefaultEquipmentSelection(){
 		selectedEquipment = listEquipments.get(0);
 		this.calculator.setEquipment(selectedEquipment);
 	}
 	/**
 	 * Gets Tab Index
 	 * @return tab index
 	 */
 	public int getTabIndex() {
 		return tabIndex;
 	}
 	/**
 	 * Sets tabIndex
 	 * @param tabIndex
 	 */
 	public void setTabIndex(int tabIndex) {
 		this.tabIndex = tabIndex;
 	}
 
 	/**
 	 * Perform calculations and return the page to navigate to
 	 * @return
 	 */
 	public String calculate(){
 		calculator.performCalculations();
 		calculator.setStatus(1);
 		return "output.xhtml";
 	}
 
 	/**
 	 * Set the equipments
 	 * @param equipments
 	 */
 	//public void setEquipment(List<Equipment> equipments){
 		//this.equipments = equipments;
 	//}
 
 	/**
 	 * Get the equipments
 	 * @return equipments
 	 */
 	//public List<Equipment> getEquipments(){
 		//return equipments;
 	//}
 	
 	public Map<String, String> getEquipments(){
 		return equipments;
 	}
 	/**
 	 * @param equipments the equipments to set
 	 */
 	public void setEquipments(Map<String, String> equipments) {
 		this.equipments = equipments;
 	}
 
 	/**
 	 * Get the locations
 	 * @return the locations
 	 */
 	public Map<String, String> getLocations() {
 		return locations;
 	}
 
 	/**
 	 * Set the locations
 	 * @param locations the locations to set
 	 */
 	public void setLocations(Map<String, String> locations) {
 		this.locations = locations;
 	}
 
 	/**
 	 * Save the calculation
 	 */
 	public void saveCalculation(){
 		FacesContext context = FacesContext.getCurrentInstance();
 		try{
 			calculatorDAO.save(calculator);
 			context.addMessage(null, new FacesMessage("Calculation Saved."));
 		} catch (Exception e){
 			context.addMessage(null, new FacesMessage("Error: " + e));
 		}
 	}
 	
 	/**
 	 * Get the list of panels
 	 * @return list of panels
 	 */
 	public List<SelectItem> getPanels() {
 		    List<SelectItem> list = new ArrayList<SelectItem>();
 		    list.add(new SelectItem(1, "1"));
 		    list.add(new SelectItem(2, "2"));
 		    list.add(new SelectItem(3, "3"));
 		    list.add(new SelectItem(4, "4"));
 		    list.add(new SelectItem(5, "5"));
 		    return list;
 	}
 	
 	/**
 	 * Get the orientation list
 	 * @return orientation list value of the orientation list property
 	 */
 	
 	public List<SelectItem> getListOfOrientation() {
 		List<SelectItem> list = new ArrayList<SelectItem>();
 	    list.add(new SelectItem("North", "North"));
 	    list.add(new SelectItem("North East", "North East"));
 	    list.add(new SelectItem("North West", "North West"));
 	    list.add(new SelectItem("South", "South"));
 	    list.add(new SelectItem("South East", "South East"));
 	    list.add(new SelectItem("South West", "South West"));
 	    list.add(new SelectItem("West", "West"));
 	    list.add(new SelectItem("East", "East"));
 	    return list;
 	}
 	
 	/**
 	 * Get the list of panels
 	 * @return list of panels
 	 */
 	public List<SelectItem> getUserTypeList() {
 		    List<SelectItem> list = new ArrayList<SelectItem>();
 		    list.add(new SelectItem(0, "<< Select Type >>"));
 		    list.add(new SelectItem(1, "Customer"));
 		    list.add(new SelectItem(2, "Seller"));
 		    list.add(new SelectItem(3, "Admin"));
 		    return list;
 	}
 	
 	/**
 	 * Returns a list of panel brands
 	 * @return list of panel brands
 	 */
 	public List<SelectItem> getListOfPanelBrands() {
 		List<SelectItem> list = new ArrayList<SelectItem>();
 	    list.add(new SelectItem("BP Solar Panels", "BP Solar Panels"));
 	    list.add(new SelectItem("Sharp Solar Panels", "Sharp Solar Panels"));
 	    list.add(new SelectItem("Sunlinq Portable Solar Panels", "Sunlinq Portable Solar Panels"));
 	    list.add(new SelectItem("SunPower Solar Panels", "SunPower Solar Panels"));
 	    list.add(new SelectItem("SunTech Solar Panels", "SunTech Solar Panels"));
 	    list.add(new SelectItem("Powerfilm Flexible Solar Panels", "Powerfilm Flexible Solar Panels"));
 	    list.add(new SelectItem("Sanyo Solar Panels", "Sanyo Solar Panels"));
 	    list.add(new SelectItem("Global Solar Panels", "Global Solar Panels"));
 	    list.add(new SelectItem("Solarfun", "Solarfun"));
 	    list.add(new SelectItem("REC Solar Panels", "REC Solar Panels"));
 	    return list;
 	}
 	
 	/**
 	 * Loads selected equipment to calculator
 	 */
 	public void handleEquipmentChange(ValueChangeEvent event){
 		try{
 			for (Equipment equipment : listEquipments) { 
 				if (equipment.getKitName().equalsIgnoreCase(event.getNewValue().toString())){
 					this.calculator.setEquipment(equipment);	
 				}
 			}
 			moveToEquipment();
 		} catch (Exception e) {}
 	}
 	
 	/**
 	 * Loads selected location to calculator
 	 */
 	public void handleLocationChange(ValueChangeEvent event){
 		try{
 			for (Location location : listLocations) {
 				if (location.getCity().equalsIgnoreCase(event.getNewValue().toString())){
 					this.calculator.getCustomer().setLocation(location);
 				}
 			}
 			moveToLocation();
 		} catch (Exception e){}
 	}
 	
 	/**
 	 * Loads selected panel to calculator
 	 */
 	public void handlePanelChange(ValueChangeEvent event){
 		try{
 			for (Panel panel : panelList) {
 				if (panel.getBrand().equalsIgnoreCase(event.getNewValue().toString())){
 					for(int index=0; index < this.calculator.getEquipment().getPanels().size(); index++){
 						this.calculator.getEquipment().getPanels().set(index, panel);
 					}
 					this.calculator.getEquipment().setCost(this.calculator.getEquipment().getTotalPanels() * panel.getCost() + this.calculator.getEquipment().getInverter().getCost());
 				}
 			}
 			moveToEquipment();
 		} catch (Exception e){}
 	}
 	
 	/**
 	 * Move to the equipment tab
 	 * @return the equipment tab
 	 */
 	public void moveToEquipment(){
 		int currentIndex = customerUsageIndex;
 		setTabIndex(currentIndex+1);
 		getTabIndex();
 		
 	}
 	
 	public void moveToCustomer(){
 		int currentIndex = locationTabIndex;
 		setTabIndex(currentIndex+1);
 		getTabIndex();
 		
 	}
 	
 	/**
 	 * Move to the roof tab
 	 * @return the roof tab
 	 */
 	public void moveToRoof(){
 		int currentIndex = equipmentTabIndex;
 		setTabIndex(currentIndex+1);
 		getTabIndex();
 		//Set default for number of panels for bank 1
 		this.calculator.getCustomer().getLocation().getRoof().getBanks()[0].setNumberOfPanels(this.calculator.getEquipment().getTotalPanels());
 	}
 	
 	/**
 	 * Move to the location tab
 	 * @return the location tab
 	 */
 	public void moveToLocation(){
 		int currentIndex = locationTabIndex;
 		setTabIndex(currentIndex);
 		getTabIndex();
 	}
 	
 	/**
 	 * Move to the summary tab
 	 * @return the summary tab
 	 */
 	public void moveToSummary(){
 		int currentIndex = roofTabIndex;
 		setTabIndex(currentIndex+1);
 		getTabIndex();
 		Bank [] banks = calculator.getCustomer().getLocation().getRoof().getBanks();
 		
 		for(int i=0; i<banks.length; i++){
 			calculator.calculateBankOrientationEfficiencyLoss(banks, banks[i].getSelectedOrientation(), i);
 			calculator.calculateBankAngleEfficiencyLoss(banks, banks[i].getAngle(), i);
 		}
 	}
 	
 	/**
 	 * Move to the output tab
 	 * @return the output tab
 	 */
 	public void moveToOutput(){
 		int currentIndex = summaryTabIndex;
 		setTabIndex(currentIndex+1);
 		getTabIndex(); 
 	}
 
 	/**
 	 * @return the equipmentDataModel
 	 */
 	//public EquipmentDataModel getEquipmentDataModel() {
 		//return equipmentDataModel;
 	//}
 
 	/**
 	 * @return the selectedEquipment
 	 */
 	public Equipment getSelectedEquipment() {
 		return selectedEquipment;
 	}
 
 	/**
 	 * @param selectedEquipment the selectedEquipment to set
 	 */
 	public void setSelectedEquipment(Equipment selectedEquipment) {
 		this.selectedEquipment = selectedEquipment;
 	}
 	
     public void onRowSelect(SelectEvent event) {  
 		for (Equipment equipment : listEquipments) {
 			if (equipment.getKitName().equalsIgnoreCase(((Equipment)event.getObject()).getKitName())){
 				this.calculator.setEquipment(equipment);
 			}
 		}
 		moveToEquipment();
     }  
     
 	/**
 	 * Save UserProfile
 	 */
 	public void saveUserProfile() {
 		userProfileDAO.save(calculator.getCustomer().getUserProfile());
 		responseMessage = "User profile created!";
 	}
 
 	/**
 	 * Show user profile creation screen
 	 * 
 	 * @return
 	 */
 	public String showUserProfileScreen() {
 		calculator.getCustomer().setUserProfile(new UserProfile());
 		responseMessage = "";
 		return "userprofile.xhtml";
 	}
 
 	public String showLoginScreen() {
 		responseMessage = "";
 		return "login.xhtml";
 	}
 	/**
 	 * Open input screen to a new calculation
 	 * @return
 	 */
 	public String newCalculation(){
 		Customer persistedCustomer = calculator.getCustomer();
 		this.calculator = new Calculator();
 		this.calculator.setCustomer(persistedCustomer);
 		this.calculator.setEquipment(new Equipment());
 		this.calculator.getEquipment().addPanel(new Panel());
 		moveToLocation();
 		return "tabinput.xhtml";
 	}
 	/**
 	 * delete selected calculations
 	 */
 	public String deleteCalculation(){
 		FacesContext context = FacesContext.getCurrentInstance();
 		try{
 			for(Calculator calc : selectedCalculators)
 				calculatorDAO.remove(calc);
 			context.addMessage(null, new FacesMessage("Successful",
 					"Calculation(s) deleted."));
 		} catch (Exception e){
 			context.addMessage(null, new FacesMessage("Error: " + e));
 		}		
 		return "home.xhtml";
 	}
 	/**
 	 * Open a selected Calculation
 	 * @return
 	 */
 	public String openCalculation(){
 		return "tabinput.xhtml";
 	}
 	/**
 	 * Compares two or more calculations
 	 */
 	public void compareCalculation(){
 		FacesContext context = FacesContext.getCurrentInstance();
 		context.addMessage(null, new FacesMessage("Successful",
 				"To be implemented - Iteration #4"));
 	}
 	/**
 	 * Back to Home Page
 	 */
 	public String backToHome(){
 		return "home.xhtml?faces-redirect=true";
 	}
 	/**
 	 * Validate login credentials
 	 * 
 	 * @return
 	 */
 	public String validateCredentials() {
 		UserProfile response = userProfileDAO.validateCredential(calculator
 				.getCustomer().getUserProfile().getEmail(), calculator
 				.getCustomer().getUserProfile().getPassword());
 		if (response.getKey() == null) {
 			responseMessage = "Invalid Email or Password.";
 			return "login.xhtml?faces-redirect=true";
 		} else {
 			if (calculator.getCustomer() == null)
 				calculator.setCustomer(new Customer());
 			calculator.getCustomer().setUserProfile(response);
 			responseMessage = "";
 			return "home.xhtml?faces-redirect=true";
 
 		}
 	}
 
 	/**
 	 * @return the panelList
 	 */
 	public List<Panel> getPanelList() {
 		return panelList;
 	}
 
 	/**
 	 * @param panelList the panelList to set
 	 */
 	public void setPanelList(List<Panel> panelList) {
 		this.panelList = panelList;
 	}
 
 	/**
 	 * @return the selectedPanel
 	 */
 	public Panel getSelectedPanel() {
 		return selectedPanel;
 	}
 
 	/**
 	 * @param selectedPanel the selectedPanel to set
 	 */
 	public void setSelectedPanel(Panel selectedPanel) {
 		this.selectedPanel = selectedPanel;
 	}
 	
 	/**
 	 * Create a chart and navigate to the chart page to display it.
 	 * @return
 	 */
 	public String createChart(){
 		calculator.createChart();
 		return "chart.xhtml";
 	}
 }
