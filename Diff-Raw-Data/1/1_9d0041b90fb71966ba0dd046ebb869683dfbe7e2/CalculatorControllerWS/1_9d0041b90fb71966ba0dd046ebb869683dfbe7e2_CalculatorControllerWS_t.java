 package au.edu.qut.inn372.greenhat.ws;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.bean.Equipment;
 import au.edu.qut.inn372.greenhat.bean.Panel;
 import au.edu.qut.inn372.greenhat.bean.UserProfile;
 import au.edu.qut.inn372.greenhat.dao.CalculatorDAO;
 import au.edu.qut.inn372.greenhat.dao.EquipmentDAO;
 import au.edu.qut.inn372.greenhat.dao.PanelDAO;
 import au.edu.qut.inn372.greenhat.dao.UserProfileDAO;
 import au.edu.qut.inn372.greenhat.dao.gae.CalculatorDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.EquipmentDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.UserProfileDAOImpl;
 import au.edu.qut.inn372.greenhat.dao.gae.PanelDAOImpl;;
 
 /**
  * 
  * @author Charleston Telles
  * @version 1.0
  */
 @WebService
 public class CalculatorControllerWS {
 	private Calculator calculator = new Calculator();	
 	private CalculatorDAO calculatorDAO = new CalculatorDAOImpl();
 	private EquipmentDAO equipmentDAO = new EquipmentDAOImpl();
 	private UserProfileDAO userProfileDAO = new UserProfileDAOImpl();
 	private PanelDAO panelDAO = new PanelDAOImpl();
 	
 	@WebMethod
 	@WebResult(name = "result") 
 	public String saveCalculation(@WebParam(name = "calculator") Calculator calculator){
 		try {
 			calculatorDAO.save(calculator);
 			return calculator.getKey();
 		} catch (Exception e) {
 			return "error: " + e;
 		}		
 	}
 	
 	@WebMethod
 	@WebResult(name = "result") 
 	public String deleteCalculation(@WebParam(name = "calculator") Calculator calculator){
 		try {
 			calculatorDAO.remove(calculator);
 			return "ok";
 		} catch (Exception e) {
 			return "error: " + e;
 		}		
 	}
 	
 	@WebMethod
 	@WebResult(name = "calculators") 
 	public List<Calculator> getCalculations(@WebParam(name = "userProfile") UserProfile userProfile){
 		try {
 			return calculatorDAO.getAllByUserProfile(userProfile);
 		} catch (Exception e) {
 			return new ArrayList<Calculator>();
 		}		
 	}
 	
 	@WebMethod
 	@WebResult(name = "calculator") 
 	public Calculator getCalculation(@WebParam(name = "name") String name){
 		try {
 			return calculatorDAO.getByName(name);
 		} catch (Exception e) {
 			return new Calculator();
 		}		
 	}
 	
 	@WebMethod
 	@WebResult(name = "calculator") 
 	public Calculator calcEnergyProduction(@WebParam(name = "calculator") Calculator calculator){
 		this.calculator = calculator;
 		//this.calculator.calculateSolarPower();
 		this.calculator.performCalculations();
 		return calculator;
 	}
 	
 	@WebMethod
 	@WebResult(name = "equipments") 
 	public Equipment[] getEquipments(){
 		List<Equipment> arrayList = equipmentDAO.getEquipments();		
 		Equipment[] list = new Equipment[arrayList.size()];
 		arrayList.toArray(list);
 		return list;
 	}
 	
 	@WebMethod
 	@WebResult(name = "userProfile") 
 	public UserProfile saveUserProfile(@WebParam(name = "userProfile") UserProfile userProfile){
 		try {
 			userProfileDAO.save(userProfile);
 			return userProfile;
 		} catch (Exception e) {
 			return new UserProfile();
 		}		
 	}
 	
 	@WebMethod
 	@WebResult(name = "result") 
 	public UserProfile validateCredentials(@WebParam(name = "email") String email, @WebParam(name = "password") String password){
 		try {
 			return userProfileDAO.validateCredential(email, password);
 		} catch (Exception e) {
 			return new UserProfile();
 		}		
 	}
 	
 	@WebMethod
 	@WebResult(name = "panels") 
 	public Panel[] getPanels(){
 		List<Panel> arrayList = panelDAO.getPanels();		
 		Panel[] list = new Panel[arrayList.size()];
 		arrayList.toArray(list);
 		return list;
 	}
 	
 	@WebMethod
 	@WebResult(name = "sunLightHours") 
 	public double getSunLightHours(@WebParam(name = "latitude") double latitude,@WebParam(name = "longitude") double longitude){
 		return calculator.getSunLightHours(latitude, longitude);
 	}
 }
