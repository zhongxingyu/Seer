 package ch.hslu.appe.fs1301.business.shared;
 
 import java.util.List;
 
 import com.google.inject.Inject;
 
 import ch.hslu.appe.fs1301.data.shared.Person;
 import ch.hslu.appe.fs1301.data.shared.iAPPEEntityManager;
 
 public class SessionAPI implements iSessionAPI {
 
 	@Inject
 	private iAPPEEntityManager fEntityManager;
 	private Person fPerson;
 	
 	@Override
 	public boolean isAuthenticated() {
 		return fPerson != null;
 	}
 
 	@Override
 	public String getUserName() {
 		if (isAuthenticated()) {
 			return fPerson.getBenutzername();
 		}
 		return null;
 	}
 
 	@Override
 	public boolean hasRole(int role) {
 		if (isAuthenticated()) {
 			if ((fPerson.getRolle() & role) != 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean authenticate(String login, String password) {
 		StringBuilder query = new StringBuilder();
 		query.append("SELECT p FROM Person p WHERE p.benutzername = '");
 		query.append(login);
 		query.append("' and p.passwort = '");
 		query.append(password);
 		query.append("'");
 		
 		List<Person> resultList = fEntityManager.executeQuery(query.toString(), Person.class);
 		if (resultList.isEmpty()) {
 			return false;
 		} else {
 			fPerson = resultList.get(0);
			if (hasRole(UserRole.Admin | UserRole.Sysuser) && fPerson.getAktiv() > 0) {
 				return true;
 			} else {
 				fPerson = null;
 				return false;
 			}			
 		}
 	}
 
 }
