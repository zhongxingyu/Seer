 package controllers;
 
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.JsonNodeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 import org.codehaus.jackson.node.TextNode;
 
 import engine.Day;
 import engine.Employee;
 import engine.EmployeeDirectory;
 import engine.EngineController;
 import engine.Position;
 import engine.RoleDirectory;
 import engine.TemplateDay;
 import engine.UidNotFoundException;
 import engine.Role;
 import engine.Util;
 import engine.uid.OutOfUidsException;
 import play.*;
 import play.libs.Json;
 import play.mvc.*;
 import session.SessionManager;
 
 public class Application extends Controller {
 
 	private static EngineController engineController = null;
 
 	public static Result init() {
 
 		Logger.debug("init(): START");
 
 		Result result = null;
 
 		if (engineController == null) {
 			try {
 				engineController = SessionManager.getInstance().newSession(); //TODO: should be redone
 				result = ok();
 			} catch (Exception e) {
				Logger.error("Exception when creating session. Msg: " + e.getMessage());
 				result = internalServerError(createJsonErrorMessage("Exception when creating session. Msg: " + e.getMessage()));
 			}
 		} else {
 			
 			ArrayNode employees = getEmployeesJsonResponse(engineController);
 			ArrayNode roles = getRolesJsonResponse(engineController);
 			
 			ObjectNode response = Json.newObject();
 			response.put("employees", employees);
 			response.put("roles", roles);
 			
 			result = ok(response);
 		}
 
 		Logger.debug("init(): END. Result = " + result.toString());
 
 		return result; 
 	}
 
 	public static Result addEmployee() {
 
 		// format
 		// {"addemployee" : <employeeInfo>}
 		// employeeInfo: {"name" : <value>, "min_hours_day" : <value>, "max_hours_day" : <value>, 
 		// "min_hours_week" : <value>, "max_hours_week" : <value>, "skills" : [<skill1>, <skill2>, ... <skillN>]}
 
 		Logger.debug("addEmployee(): START");
 
 		Result result = null;
 		JsonNode json = request().body().asJson();
 
 		if (json != null) {
 
 			// -1 => add contact
 			boolean success = false;
 			Employee employee = null;
 			try {
 				employee = manipulateEmployeeFromJsonData(-1, json);
 			} catch (IllegalArgumentException iae) {
 				Logger.error("addEmployee(): IllegalArgumentException. Msg: " + iae.getMessage());
 				result = badRequest(createJsonErrorMessage(iae.getMessage()));
 			} catch (OutOfUidsException ooue) {
 				Logger.error("addEmployee(): OutOfUidsException. Msg: " + ooue.getMessage());
 				result = badRequest(createJsonErrorMessage(ooue.getMessage()));
 			} catch (UidNotFoundException unfe) {
 				Logger.error("addEmployee(): UidNotFoundException. Msg: " + unfe.getMessage());
 				result = badRequest(createJsonErrorMessage(unfe.getMessage()));
 			} finally {
 				success = (employee != null) ? true : false;
 			}
 
 			if (success) {
 				// create the response
 				ObjectNode response = Json.newObject();
 				response.put("uid", employee.getUid());
 				result = ok(response);
 			}
 
 		} else {
 			Logger.info("addEmployee(): Malformed Request: " + request().body().asText());
 			result = badRequest(createJsonErrorMessage("Malformed request!"));
 		}
 
 		Logger.debug("addEmployee(): END. Result = " + result.toString());
 
 		return result;
 	}
 
 	public static Result removeEmployee() {
 
 		// format
 		// {"uid" : <uid>}
 
 		Logger.debug("removeEmployee(): START");
 
 		Result result = null;
 		JsonNode json = request().body().asJson();
 		if (json != null) {
 
 			boolean success = true;
 
 			int uid = json.path("uid").asInt(-1);
 			Logger.debug("removeEmployee(): uid of employee to be removed = " + uid);
 			if (uid == -1) {
 				success = false;
 				Logger.debug("removeEmployee(): Parameter error: uid. Value: " + uid);
 				result = badRequest(createJsonErrorMessage("Parameter error: id. Value: " + uid));
 			}
 
 			if (success) {
 				// Deassociate the employee from all roles.
 				int[] roleUids = engineController.getRoleDirectory().deassociateEmployeeFromAllRoles(uid);
 				for (int roleUid : roleUids) {
 					Logger.debug("removeEmployee(): Employee " + engineController.getEmployeeDirectory().getEmployee(uid).getName() + " was deassociated with role " + engineController.getRoleDirectory().getRole(roleUid).getName());
 				}
 
 				// Remove the employee
 				engineController.getEmployeeDirectory().removeEmployee(uid);
 				result = ok();
 			}
 
 		} else {
 			Logger.debug("removeEmployee(): Invalid Request. Cannot parse json.");
 			result = badRequest(createJsonErrorMessage("Request is invalid"));
 		}
 
 		Logger.debug("removeEmployee(): END. Result = " + result.toString());
 
 		return result;
 	}
 
 	public static Result updateEmployee() {
 
 		// format
 		// {"uid" : <uid>, "name" : <name>, ...}
 
 		Logger.debug("updateEmployee: START");
 
 		Result result = null;
 		JsonNode json = request().body().asJson();
 		if (json != null) {
 
 			int uid = json.path("uid").asInt(-1);
 
 			if (uid != -1) {
 				boolean success = false;
 				Employee employee = null;
 				try {
 					employee = manipulateEmployeeFromJsonData(uid, json);
 				} catch (IllegalArgumentException iae) {
 					Logger.error("addEmployee(): IllegalArgumentException. Msg: " + iae.getMessage());
 					result = badRequest(createJsonErrorMessage(iae.getMessage()));
 				} catch (OutOfUidsException ooue) {
 					Logger.error("addEmployee(): OutOfUidsException. Msg: " + ooue.getMessage());
 					result = badRequest(createJsonErrorMessage(ooue.getMessage()));
 				} catch (UidNotFoundException unfe) {
 					Logger.error("addEmployee(): UidNotFoundException. Msg: " + unfe.getMessage());
 					result = badRequest(createJsonErrorMessage(unfe.getMessage()));
 				} finally {
 					success = (employee != null) ? true : false;
 				}
 
 				if (success) {
 					// create the response
 					ObjectNode response = Json.newObject();
 					response.put("uid", employee.getUid());
 					response.put("name", employee.getName());
 					response.put("minHoursDay", employee.getMinHoursPerDay());
 					response.put("maxHoursDay", employee.getMaxHoursPerDay());
 					response.put("minHoursWeek", employee.getMinHoursPerWeek());
 					response.put("maxHoursWeek", employee.getMaxHoursPerWeek());
 					result = ok(response);
 				}
 
 			} else {
 				Logger.debug("updateEmployee(): Malformed Request. Missing uid.");
 				result = badRequest(createJsonErrorMessage("Malformed Request. Missing uid."));
 			}
 
 		} else {
 			Logger.debug("updateEmployee(): Malformed Request: " + request().body().asText());
 			result = badRequest(createJsonErrorMessage("Malformed Request!"));
 		}
 
 		Logger.debug("updateEmployee: END. Result: " + result.toString());
 
 		return result;
 	}
 
 	public static Result addRole() { 
 
 		// format
 		// {"addskill" : <roleInfo>}
 		// skillInfo: {"name" : <value>, "employees" : [<employee1>, <employee2>, ... <employeeN>]}
 
 		Logger.debug("addRole(): START");
 
 		Result result = null;
 		JsonNode json = request().body().asJson();
 		if (json != null) {
 
 				boolean success = false;
 				Role role = null;
 				try {
 					role = manipulateRoleFromJsonData(-1, json); // -1 => create.
 				} catch (IllegalArgumentException iae) {
 					Logger.error("addRole(): IllegalArgumentException. Msg: " + iae.getMessage());
 					result = badRequest(createJsonErrorMessage(iae.getMessage()));
 				} catch (OutOfUidsException ooue) {
 					Logger.error("addRole(): OutOfUidsException. Msg: " + ooue.getMessage());
 					result = internalServerError(createJsonErrorMessage(ooue.getMessage()));
 				} catch (UidNotFoundException unfe) {
 					Logger.error("addRole(): UidNotFoundException. Msg: " + unfe.getMessage());
 					result = badRequest(createJsonErrorMessage(unfe.getMessage()));
 				} finally {
 					success = (role != null) ? true : false;
 				}
 
 				if (success) {
 					ObjectNode response = Json.newObject();
 					response.put("uid", role.getUid());
 					response.put("name", role.getName());
 					ArrayNode employeesJsonArray = JsonNodeFactory.instance.arrayNode();
 					List<Employee> employees = role.getEmployees();
 					for (Employee employee : employees) {
 						employeesJsonArray.add(employee.getUid());
 					}
 
 					response.put("employeeUids",employeesJsonArray);
 					result = ok(response);
 				}
 
 		} else {
 			result = badRequest(createJsonErrorMessage("Malformed request!"));
 		}
 
 		Logger.debug("addRole(): END. Result = " + result.toString());
 
 		return result;
 	}
 
 	public static Result updateRole() {
 
 		Logger.debug("updateRole(): START");
 
 		Result result = null;
 		JsonNode json = request().body().asJson();
 		if (json != null) {
 
 			int uid = json.path("uid").asInt(-1);
 
 			if (uid != -1) {
 				boolean success = false;
 				Role role = null;
 				try {
 					role = manipulateRoleFromJsonData(uid, json);
 				} catch (IllegalArgumentException iae) {
 					Logger.error("updateRole(): IllegalArgumentException. Msg: " + iae.getMessage());
 					result = badRequest(createJsonErrorMessage(iae.getMessage()));
 				} catch (OutOfUidsException ooue) {
 					Logger.error("updateRole(): OutOfUidsException. Msg: " + ooue.getMessage());
 					result = badRequest(createJsonErrorMessage(ooue.getMessage()));
 				} catch (UidNotFoundException unfe) {
 					Logger.error("updateRole(): UidNotFoundException. Msg: " + unfe.getMessage());
 					result = badRequest(createJsonErrorMessage(unfe.getMessage()));
 				} finally {
 					success = (role != null) ? true : false;
 				}
 
 				if (success) {
 					// create the response
 					ObjectNode response = Json.newObject();
 					response.put("uid", role.getUid());
 					response.put("name", role.getName());
 
 					ArrayNode employeesJsonArray = JsonNodeFactory.instance.arrayNode();
 					List<Employee> employees = role.getEmployees();
 					for (Employee employee : employees) {
 						employeesJsonArray.add(employee.getUid());
 					}
 
 					response.put("employeeUids",employeesJsonArray);
 					result = ok(response);
 				}
 
 			} else {
 				result = badRequest(createJsonErrorMessage("Invalid request. Uid is missing!"));
 			}
 
 		} else {
 			result = badRequest(createJsonErrorMessage("Malformed request!"));
 		}
 
 		Logger.debug("updateRole(): END. Result = " + result.toString());
 
 		return result;
 	}
 
 	public static Result removeRole() {
 
 		// format
 		// {"uid" : <uid>}
 
 		Logger.debug("removeRole(): START");
 
 		Result result = null;
 		JsonNode json = request().body().asJson();
 		if (json != null) {
 
 			boolean success = true;
 
 			int uid = json.path("uid").asInt(-1);
 			Logger.debug("uid = " + uid);
 			if (uid == -1) {
 				success = false;
 				Logger.debug("removeRole(): Parameter error: id. Value: " + uid);
 				result = badRequest(createJsonErrorMessage("Parameter error: id. Value: " + uid));
 			}
 
 			if (success) {
 				engineController.getRoleDirectory().removeRole(uid); 
 				result = ok();
 			}
 
 		} else {
 			Logger.debug("removeRole(): Invalid Request. Cannot parse json.");
 			result = badRequest(createJsonErrorMessage("Request is invalid"));
 		}
 
 		Logger.debug("removeRole(): END. Result = " + result.toString());
 
 		return result;
 	}
 	
 	public static Result addDayTemplate() {
 		Logger.debug("addDayTemplate(): START");
 
 		Result result = null;
 		JsonNode json = request().body().asJson();
 		if (json != null) {
 			
 			TemplateDay template = null;
 			boolean success = false;
 			try {
 				template = manipulateDayTemplateFromJsonData(-1, json);
 			} catch (IllegalArgumentException e) {
 				Logger.error("addDayTemplate(): IllegalArgumentException. Msg: " + e.getMessage());
 				result = badRequest(createJsonErrorMessage(e.getMessage()));
 			} catch (UidNotFoundException e) {
 				Logger.error("addDayTemplate(): UidNotFoundException. Msg: " + e.getMessage());
 				result = badRequest(createJsonErrorMessage(e.getMessage()));
 			} catch (OutOfUidsException e) {
 				Logger.error("addDayTemplate(): OutOfUidsException. Msg: " + e.getMessage());
 				result = badRequest(createJsonErrorMessage(e.getMessage()));
 			} finally {
 				success = (template != null) ? true : false; 
 			}
 
 			if (success) {
 				ObjectNode templateDayJson = Json.newObject();
 				templateDayJson.put("uid", template.getUid());
 				result = ok(templateDayJson);
 			}
 			
 		} else {
 			Logger.debug("addDayTemplate(): Invalid Request. Cannot parse json.");
 			result = badRequest(createJsonErrorMessage("Request is invalid"));
 		}
 
 		Logger.debug("addDayTemplate(): END. Result = " + result.toString());
 
 		return result;
 	}
 
 	private static ArrayNode getEmployeesJsonResponse(EngineController engineController) {
 
 		ArrayNode employeeArray = Json.newObject().arrayNode();
 
 		List<Employee> employeeList = engineController.getEmployeeDirectory().getAllEmployees();
 		for (int i = 0; i < employeeList.size(); i++) {
 
 			// Get the data.
 			int uid = employeeList.get(i).getUid();
 			String name = employeeList.get(i).getName();
 			int minHoursDay = employeeList.get(i).getMinHoursPerDay();
 			int maxHoursDay = employeeList.get(i).getMaxHoursPerDay();
 			int minHoursWeek = employeeList.get(i).getMinHoursPerWeek();
 			int maxHoursWeek = employeeList.get(i).getMaxHoursPerWeek();
 
 			// Create employeeInfo json structure
 			ObjectNode employee = Json.newObject();
 
 			employee.put("uid", uid);
 			employee.put("name", name);
 			employee.put("minHoursDay", minHoursDay);
 			employee.put("maxHoursDay", maxHoursDay);
 			employee.put("minHoursWeek", minHoursWeek);
 			employee.put("maxHoursWeek", maxHoursWeek);
 
 			employeeArray.add(employee);
 		}
 
 		return employeeArray;
 	}
 
 	private static ArrayNode getRolesJsonResponse(EngineController ec) {
 
 		ArrayNode roleArray = Json.newObject().arrayNode();
 
 		List<Role> roleList = engineController.getRoleDirectory().getAllRoles();
 		for (int i = 0; i < roleList.size(); i++) {
 
 			List<Employee> associatedEmployees = roleList.get(i).getEmployees();
 
 			// Create skillInfo json structure
 			ObjectNode role = Json.newObject();
 			ArrayNode associatedEmployeesJson = Json.newObject().arrayNode();
 
 			// fill in the skills array
 			for (int j = 0; j < associatedEmployees.size(); j++) {
 				associatedEmployeesJson.add(associatedEmployees.get(j).getUid());
 			}
 
 			role.put("uid", roleList.get(i).getUid());
 			role.put("name", roleList.get(i).getName());
 			role.put("employeeUids", associatedEmployeesJson);
 
 			roleArray.add(role);
 		}
 		
 		return roleArray;
 	}
 
 	// Simple method to packet an error message as JSON.
 	private static String createJsonErrorMessage(String errorMsg) {
 
 		String errorMessage = "{\"errorMsg\" : \"" + errorMsg + "\"}";
 		return errorMessage;
 	}
 
 	// (uid == -1) => contact will be created
 	private static Employee manipulateEmployeeFromJsonData(int uid, JsonNode jsonData) throws IllegalArgumentException, UidNotFoundException, OutOfUidsException{
 
 		String name = jsonData.path("name").asText();
 		int minHoursDay = jsonData.path("minHoursDay").asInt(-1);
 		int maxHoursDay = jsonData.path("maxHoursDay").asInt(-1);
 		int minHoursWeek = jsonData.path("minHoursWeek").asInt(-1);
 		int maxHoursWeek = jsonData.path("maxHoursWeek").asInt(-1);
 
 		if (name == null || name.equals("")) {
 			throw new IllegalArgumentException("Parameter error: name == " + name);
 		} else if (minHoursDay == -1) {
 			throw new IllegalArgumentException("Parameter error: minHoursDay == " + minHoursDay);
 		} else if (maxHoursDay == -1) {
 			throw new IllegalArgumentException("Parameter error: maxHoursDay == " + maxHoursDay);
 		} else if (minHoursWeek == -1) {
 			throw new IllegalArgumentException("Parameter error: minHoursWeek == " + minHoursWeek);
 		} else if (maxHoursWeek == -1) {
 			throw new IllegalArgumentException("Parameter error: maxHoursWeek == " + maxHoursWeek);
 		}
 
 		Employee employee = null;
 		if (uid == -1) {
 			employee = engineController.getEmployeeDirectory().createNewEmployee(name, minHoursDay, maxHoursDay, minHoursWeek, maxHoursWeek);
 		} else {
 			employee = engineController.getEmployeeDirectory().updateEmployee(uid, name, minHoursDay, maxHoursDay, minHoursWeek, maxHoursWeek);
 		}
 
 		return employee;
 	}
 
 	private static Role manipulateRoleFromJsonData (int uid, JsonNode jsonData) throws IllegalArgumentException, UidNotFoundException, OutOfUidsException {
 
 		String name = jsonData.path("name").asText();
 
 		if (name == null || name.equals("")) {
 			throw new IllegalArgumentException("Parameter error: name == " + name);
 		} 
 		Iterator<JsonNode> iterator = jsonData.path("employeeUids").getElements();
 		List<Employee> associatedEmployees = new ArrayList<Employee>();
 		if (iterator != null) {
 			int employeeUid = 0;
 			Employee employee = null;
 			while (iterator.hasNext()) {
 				employeeUid = Integer.parseInt(iterator.next().getTextValue()); // I don't understand this json implementation... :/ 
 				employee = engineController.getEmployeeDirectory().getEmployee(employeeUid);
 				associatedEmployees.add(employee);
 			}
 		} else {
 			throw new IllegalArgumentException("Parameter error: associatedEmployeesInterator == " + name);
 		}
 
 		Role role = null;
 		if (uid == -1) {
 			role = engineController.getRoleDirectory().createNewRole(name, associatedEmployees);
 		} else {
 			role = engineController.getRoleDirectory().updateRole(uid, name, associatedEmployees);
 		}
 
 		return role;
 	}
 	
 	// TODO: fix this!!
 	private static TemplateDay manipulateDayTemplateFromJsonData (int uid, JsonNode jsonData) throws IllegalArgumentException, UidNotFoundException, OutOfUidsException {
 
 		String name = jsonData.path("name").asText();
 		if (name == null || name.equals("")) {
 			throw new IllegalArgumentException("Parameter error: name == " + name);
 		} 
 		
 		Iterator<JsonNode> iterator = jsonData.path("positions").getElements();
 		List<Position> positions = new ArrayList<Position>();
 		if (iterator != null) {
 			while (iterator.hasNext()) {
 				JsonNode positionJson = iterator.next();
 				int requiredRoleUid = positionJson.path("requiredRoleUid").asInt(-1);
 				int startTime = positionJson.path("startTime").asInt(-1);
 				int endTime = positionJson.path("endTime").asInt(-1);
 				
 				if (requiredRoleUid != -1 && startTime != -1 && endTime != -1) {
 					Date startTimeObj = Util.intHour2DateObject(startTime);
 					Date endTimeObj = Util.intHour2DateObject(endTime);
 					Position position = engineController.getTemplateManager().createPosition(requiredRoleUid, startTimeObj, endTimeObj);
 					positions.add(position);
 				} else {
 					throw new IllegalArgumentException("Parameter error related to Position structure. requiredRoleUid == " + requiredRoleUid + "; startTime == " + startTime + "; endTime == " + endTime);
 				}
 			}
 		} else {
 			throw new IllegalArgumentException("Parameter error: associatedEmployeesInterator == " + name);
 		}
 		
 		TemplateDay templateDay = null;
 		
 		if (uid == -1) {
 			templateDay = engineController.getTemplateManager().createNewDayTemplate(name, positions);
 		} else {
 			templateDay = engineController.getTemplateManager().updateDayTemplate(uid, name, positions);
 		}
 
 		return templateDay;
 	}
 }
