 package contollers;
 
 import java.util.ArrayList;
 
 import beans.UserBean;
 import beans.UserBeanHelper;
 import ass2.FormManager;
 import ass2.JDBCConnector;
 import ass2.ParameterManager;
 
 public class AccountController extends MasterFormBasedController {
 
 	ArrayList<String> thingsChanged;
 	public AccountController(ParameterManager params) {
 		super(params);
 		thingsChanged = new ArrayList<String>();
 	}
 
 	protected void createForm() {
 
 		formManager.addOptionalForm("password", paramManager.getIndividualParam("password"));
 		formManager.addOptionalForm("email", paramManager.getIndividualParam("email"), FormManager.RESTRICT_EMAIL);
 		formManager.addOptionalForm("nickname", paramManager.getIndividualParam("nickname"));
 		formManager.addOptionalForm("firstName", paramManager.getIndividualParam("firstName"));
 		formManager.addOptionalForm("lastName", paramManager.getIndividualParam("lastName"));
 		formManager.addOptionalForm("yearOfBirth", paramManager.getIndividualParam("yearOfBirth"), FormManager.RESTRICT_NUMERIC_ONLY);
 		formManager.addOptionalForm("address", paramManager.getIndividualParam("address"));
 		formManager.addOptionalForm("ccNumber", paramManager.getIndividualParam("ccNumber"), FormManager.RESTRICT_NUMERIC_ONLY);
 	}
 
 	public void updateAccount(UserBean ub) {
 		if (ub != null) {
 
 			//i guess we should only set things if they actually changed
 
 			String[] stringForms = {"password", "email", "nickname", "firstName", "lastName", "address"};
 			String[] intForms = {"yearOfBirth", "ccNumber"};
 
 			UserBeanHelper ubHelper = new UserBeanHelper(ub);
 
 			for (String form: stringForms) {
 				if (formManager.isValueBlank(form) == false) {
 					if (ubHelper.getStringAttributeFromName(form).equals(paramManager.getIndividualParam(form)) == false) {
 						thingsChanged.add(form);
 						ubHelper.setStringAttributeFromName(form, paramManager.getIndividualParam(form));
 					}
 
 				}
 			}
 
 			for (String form: intForms) {
 				if (formManager.isValueBlank(form) == false) {
 					int num = Integer.parseInt(paramManager.getIndividualParam(form));
 
 					if (ubHelper.getIntAttributeFromName(form) != num) {
 						thingsChanged.add(form);
 						ubHelper.setIntAttributeFromName(form, num);
 					}
 
 				}
 			}
 			if (hasChanged()) {
 				JDBCConnector.updateAccount(ub);
 
 			}
 		} else {
 			System.out.println("User is null, i'm not going to update the account");
 		}
 	}
 	public boolean hasChanged() {
 		return thingsChanged.size() > 0;
 	}
 	public String getChangedDetails() {
 		StringBuilder s = new StringBuilder();
 		for (int i = 0; i < thingsChanged.size(); i++) {
 			s.append(thingsChanged.get(i));
 
 			if (i > 0 && i < thingsChanged.size()) {
 				s.append(", ");
 			}
 		}
 
 		return s.toString();
 	}
 
 }
