 /**
  * 
  */
 package au.edu.qut.inn372.greenhat.controller;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.primefaces.component.inputtext.InputText;
 import org.primefaces.component.selectonemenu.SelectOneMenu;
 
 import au.edu.qut.inn372.greenhat.bean.Calculation;
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.bean.Customer;
 import au.edu.qut.inn372.greenhat.bean.ElectricityUsage;
 import au.edu.qut.inn372.greenhat.bean.Equipment;
 import au.edu.qut.inn372.greenhat.bean.Inverter;
 import au.edu.qut.inn372.greenhat.bean.Location;
 import au.edu.qut.inn372.greenhat.bean.Panel;
 import au.edu.qut.inn372.greenhat.bean.Roof;
 import au.edu.qut.inn372.greenhat.bean.Tariff;
 import au.edu.qut.inn372.greenhat.bean.UserProfile;
 import au.edu.qut.inn372.greenhat.dao.CalculatorDAO;
 import au.edu.qut.inn372.greenhat.dao.UserProfileDAO;
 import au.edu.qut.inn372.greenhat.dao.gae.CalculatorDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.UserProfileDAOImpl;
 
 /**
  * @author Martin Daniel
  * @version 1.0
  */
 public class CalculatorControllerTest {
 
 	private CalculatorController calculatorController;
 	private Calculator calculator;
 	private CalculatorDAO calculatorDAO;
 	private UserProfileDAO userProfileDAO;
 	private Map<String, String> equipments;
 	List<Equipment> listEquipments;
 	private Equipment selectedEquipment;
 
 	private Map<String, String> locations;
 	List<Location> listLocations;
 
 	private List<Panel> panelList;
 	private Panel selectedPanel;
 	
 	private Map<String, String> inverters;
 	List<Inverter> listInverters;
 
 	private int tabIndex = 0;
 	private String responseMessage;
 
 	private CalculatorDataModel savedCalculators;
 	private Calculator[] selectedCalculators;
 	
 	@Before
 	public void setUp() throws Exception {
 		calculatorController = new CalculatorController();
 		calculator = new Calculator();
 		calculatorDAO = new CalculatorDAOImpl();
 		userProfileDAO = new UserProfileDAOImpl();
 		equipments = new HashMap<String, String>();
 		listEquipments = new ArrayList<Equipment>();
 		selectedEquipment = new Equipment();
 		locations = new HashMap<String, String>();
 		listLocations = new ArrayList<Location>();
 		panelList = new ArrayList<Panel>();
 		selectedPanel = new Panel();
 		inverters = new HashMap<String, String>();
 		listInverters = new ArrayList<Inverter>();
 		savedCalculators = new CalculatorDataModel();
 		selectedCalculators = new Calculator[4];
 		
 		calculatorController.setCalculator(calculator);
 		calculatorController.setEquipments(equipments);
 		calculatorController.setInverters(inverters);
 		calculatorController.setListInverters(listInverters);
 		calculatorController.setLocations(locations);
 		calculatorController.setPanelList(panelList);
 		calculatorController.setResponseMessage("Message Responded");
 		calculatorController.setSavedCalculators(savedCalculators);
 		calculatorController.setSelectedCalculators(selectedCalculators);
 		calculatorController.setSelectedEquipment(selectedEquipment);
 		calculatorController.setSelectedPanel(selectedPanel);
 		calculatorController.setTabIndex(tabIndex);
 		
 		calculator.setCustomer(new Customer());
 		calculator.getCustomer().setUserProfile(new UserProfile());
 		calculator.getCustomer().setTariff(new Tariff());
 		calculator.getCustomer().setLocation(new Location());
 		calculator.getCustomer().getLocation().setRoof(new Roof());
 		calculator.getCustomer().setElectricityUsage(new ElectricityUsage());
 	}
 
 	@Test
 	public void testSetGetCalculator() {
 		assertEquals(calculator, calculatorController.getCalculator());
 		Calculator newCalculator = new Calculator();
 		calculatorController.setCalculator(newCalculator);
 		assertEquals(newCalculator, calculatorController.getCalculator());
 	}
 	
 	@Test
 	public void testSetGetTabIndex(){
 		assertEquals(0, calculatorController.getTabIndex());
 		calculatorController.setTabIndex(2);
 		assertEquals(2, calculatorController.getTabIndex());
 	}
 	
 	@Test
 	public void testSetGetSelectedCalculators(){
 		assertEquals(selectedCalculators.length, calculatorController.getSelectedCalculators().length);
 		Calculator [] newSelectedCalculators = new Calculator[3];
 		calculatorController.setSelectedCalculators(newSelectedCalculators);
 		assertEquals(newSelectedCalculators.length, calculatorController.getSelectedCalculators().length);
 	}
 
 	@Test
 	public void testSetGetResponseMessage(){
 		assertEquals("Message Responded", calculatorController.getResponseMessage());
 		calculatorController.setResponseMessage("Message Unresponded");
 		assertEquals("Message Unresponded", calculatorController.getResponseMessage());
 	}
 	
 	@Test
 	public void testSetDefaultEquipment(){
 		//listEquipments.add(new Equipment());
 		calculatorController.setDefaultEquipmentSelection();
		assertEquals("Kit 2.5KWh - 10 panels(250W)", calculatorController.getCalculator().getEquipment().getKitName());
 	}
 	
 	@Test
 	public void testCalculate(){
 		//assertEquals("output.xhtml", calculatorController.calculate());
 	}
 	
 	@Test
 	public void testSetGetEquipments(){
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("5.6KWh 8 500W Panel","5.6KWh 8 500W Panel");
 		assertEquals(1, map.size());
 	}
 	
 	@Test
 	public void testSetGetLocations(){
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("Brisbane","Brisbane");
 		assertEquals(1, map.size());
 	}
 	
 	@Test
 	public void testSetGetPanels(){
 		assertEquals(5, calculatorController.getPanels().size());
 	}
 	
 	@Test
 	public void testSetGetOrientations(){
 		assertEquals(8, calculatorController.getListOfOrientation().size());
 	}
 	
 	@Test
 	public void testGetUserTypeList(){
 		assertEquals(4, calculatorController.getUserTypeList().size());
 	}
 	
 	@Test
 	public void testListOfPanelBrands(){
 		assertEquals(10, calculatorController.getListOfInverterBrands().size());
 	}
 	
 	@Test
 	public void testListOfInverterBrands(){
 		assertEquals(10, calculatorController.getListOfInverterBrands().size());
 	}
 	
 	@Test
 	public void testHandleEquipmentChange(){
 		SelectOneMenu selectOneMenu = new SelectOneMenu();
 		ValueChangeEvent event = new ValueChangeEvent(selectOneMenu, "", "Kit 2.5KWh - 10 panels(250W)");
 		calculatorController.handleEquipmentChange(event);
 	}
 	
 	@Test
 	public void testHandlePanelChange(){
 		InputText inputText = new InputText();
 		ValueChangeEvent event = new ValueChangeEvent(inputText, "", "BP Solar Panels");
 		calculatorController.handleNumOfPanelsChange(event);
 	}
 	
 	@Test
 	public void testHandleInverterChange(){
 		SelectOneMenu selectOneMenu = new SelectOneMenu();
 		ValueChangeEvent event = new ValueChangeEvent(selectOneMenu, "", "BP Solar Inverters");
 		calculatorController.handleInverterChange(event);
 	}
 	
 	@Test
 	public void testMoveToEquipment(){
 		calculatorController.moveToEquipment();
 		assertEquals(2, calculatorController.getTabIndex());
 	}
 	
 	@Test
 	public void testMoveToCustomer(){
 		calculatorController.moveToCustomer();
 		assertEquals(1, calculatorController.getTabIndex());
 	}
 	
 	@Test
 	public void testMoveToRoof(){
 		calculatorController.moveToRoof();
 		assertEquals(3, calculatorController.getTabIndex());
 	}
 	
 	@Test
 	public void testMoveToLocation(){
 		calculatorController.moveToLocation();
 		assertEquals(0, calculatorController.getTabIndex());
 	}
 	
 	@Test
 	public void testMoveToSummary(){
 		calculatorController.moveToSummary();
 		assertEquals(4, calculatorController.getTabIndex());
 	}
 	
 	@Test
 	public void testMoveToOutput(){
 		calculatorController.moveToOutput();
 		assertEquals(5, calculatorController.getTabIndex());
 	}
 	
 	@Test
 	public void testSetGetSelectedEquipment(){
 		assertEquals(selectedEquipment, calculatorController.getSelectedEquipment());
 		Equipment newSelectedEquipment = new Equipment();
 		calculatorController.setSelectedEquipment(newSelectedEquipment);
 		assertEquals(newSelectedEquipment, calculatorController.getSelectedEquipment());
 	}
 	
 	@Test
 	public void testShowUserProfileScreen(){
 		assertEquals("userprofile.xhtml", calculatorController.showUserProfileScreen());
 	}
 	
 	@Test
 	public void testShowLoginScreen(){
 		assertEquals("tabinput.xhtml", calculatorController.showLoginScreen());
 	}
 	
 	@Test
 	public void testNewCalculation(){
 		Customer persistedCustomer = this.calculator.getCustomer();
 		this.calculator = new Calculator();
 		this.calculator.setChart(new Chart());
 		this.calculator.setCustomer(persistedCustomer);
 		assertEquals(calculator.getCustomer(), persistedCustomer);
 	}
 	
 	@Test
 	public void testBackToHome(){
 		assertEquals("home.xhtml?faces-redirect=true", calculatorController.backToHome());
 	}
 	
 	@Test
 	public void testGetPanelList(){
 		assertEquals(panelList.size(), calculatorController.getPanelList().size());
 	}
 	
 	@Test
 	public void testSetGetSelectedPanel(){
 		assertEquals(selectedPanel, calculatorController.getSelectedPanel());
 		Panel newSelectedPanel = new Panel();
 		calculatorController.setSelectedPanel(newSelectedPanel);
 		assertEquals(newSelectedPanel, calculatorController.getSelectedPanel());
 	}
 	
 	@Test
 	public void testHandleBankOrientationChange(){
 		SelectOneMenu selectOneMenu = new SelectOneMenu();
 		ValueChangeEvent event = new ValueChangeEvent(selectOneMenu, "", "North");
 		calculatorController.handleBankOrientationChange(event);
 	}
 	
 	@Test
 	public void testHandleNumOfPanelsChange(){
 		InputText inputText = new InputText();
 		ValueChangeEvent event = new ValueChangeEvent(inputText, "", "3");
 		calculatorController.handleNumOfPanelsChange(event);
 	}
 	
 	@Test
 	public void testCalculateAngleEfficiency(){
 		InputText inputText = new InputText();
 		ValueChangeEvent event = new ValueChangeEvent(inputText, "", "60");
 		calculatorController.calculateAngleEfficiency(event);
 	}
 	
 	@Test
 	public void testSetGetInverters(){
 		inverters.put("Solarfun Inverters", "Solarfun Inverters");
 		calculatorController.setInverters(inverters);
 		assertEquals("Solarfun Inverters", inverters.get("Solarfun Inverters"));
 	}
 	
 	@Test
 	public void testSetGetListInverters(){
 		listInverters.add(new Inverter());
 		assertEquals(1, listInverters.size());
 	}
 	
 	@Test
 	public void testGetPanels() {
 	    List<SelectItem> list = new ArrayList<SelectItem>();
 	    list.add(new SelectItem(1, "1"));
 	    list.add(new SelectItem(2, "2"));
 	    list.add(new SelectItem(3, "3"));
 	    list.add(new SelectItem(4, "4"));
 	    list.add(new SelectItem(5, "5"));
 	    assertEquals(calculatorController.getPanels().size(), list.size());
 	}
 	
 	@Test
 	public void testCreateChart(){
 		calculator.setCustomer(new Customer());
 		calculator.getCustomer().setElectricityUsage(new ElectricityUsage());
 		calculator.getCustomer().getElectricityUsage().setDailyAverageUsage(2);
 		
 		Calculation calculation = new Calculation();
 		calculation.setAnnualSaving(10);
 		calculation.setAnnualSolarPower(10);
 		calculation.setBank1DailySolarPower(2);
 		calculation.setBank1Efficiency(2);
 		calculation.setBank2DailySolarPower(2);
 		calculation.setBank2Efficiency(2);
 		calculation.setCumulativeSaving(10);
 		calculation.setDailySaving(2);
 		calculation.setDailySolarPower(10);
 		calculation.setExportedGeneration(10);
 		calculation.setMoneyEarned(5);
 		calculation.setMoneySaved(5);
 		calculation.setPanelEfficiency(0.5);
 		calculation.setPaybackPeriod(3);
 		calculation.setReplacementGeneration(3);
 		calculation.setReturnOnInvestment(0.18);
 		calculation.setTariff11Fee(0.5);
 		calculation.setYear(2012);
 		
 		Calculation [] calculations = new Calculation[1];
 		calculations[0] = calculation;
 		calculator.setCalculations(calculations);
 		//assertEquals("chart.xhtml", calculatorController.createChart());
 	}
 	
 	@Test
 	public void testSaveCalculation() throws Exception {
 		//Calculation [] calculations = null;
 		//calculator.setCalculations(calculations);
 		//calculatorController.saveCalculation();
 	}
 	
 	@Test
 	public void testTemplate(){
 		
 	}
 	
 	@Test
 	public void testGetSavedCalculators(){
 		//this.savedCalculators = new CalculatorDataModel(
 				//calculatorDAO.getAllByUserProfile(calculator.getCustomer()
 						//.getUserProfile()));
 		//CalculatorDataModel newSavedCalculators = new CalculatorDataModel();
 		//assertEquals(newSavedCalculators, calculatorController.getSavedCalculators());
 	}
 	
 	@Test
 	public void testDeleteCalculation() throws Exception{
 		//calculatorController.saveCalculation();
 		//selectedCalculators[0] = new Calculator();
 		//selectedCalculators[1] = new Calculator();
 		//assertEquals("home.xhtml", calculatorController.deleteCalculation());
 	}
 	
 	@Test
 	public void testOpenCalculation(){
 		//assertEquals("tabinput.xhtml?faces-redirect=true", calculatorController.openCalculation());
 	}
 	
 	@Test
 	public void testCompareCalculation(){
 		
 	}
 	
 	@Test
 	public void testSaveUserProfile(){
 		//calculatorController.saveUserProfile();
 	}
 	
 	@Test
 	public void testValidateCredentials(){
 		
 	}
 }
