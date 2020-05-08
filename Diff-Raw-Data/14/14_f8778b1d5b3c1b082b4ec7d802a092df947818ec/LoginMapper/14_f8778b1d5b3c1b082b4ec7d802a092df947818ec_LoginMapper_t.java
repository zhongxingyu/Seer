 package data;
 
import static main.Constants.LOGIN_CHECK;
 
 import java.sql.SQLException;
 
 import domain.AbstractDomainClass;
 
 public class LoginMapper extends AbstractDataMapper {
 	
 	public Boolean checkLogin(String username, String password) throws SQLException, ClassNotFoundException{
 		start();
		setRetrieveString(LOGIN_CHECK);
 		setRetrieveString(getRetrieveString().replaceAll("::username", "" + username));
 		setRetrieveString(getRetrieveString().replaceAll("::password", password));
 		executeToResultSet(getRetrieveString());
 		if (getResult().next()){
 			return true;
 		}else {
 			return false;
 		}
 	
 	}
 
 	@Override
 	AbstractDomainClass placeAndReturn() throws SQLException,
 			ClassNotFoundException {
 		throw new UnsupportedOperationException("Method is not available to this class");
 	}
 
 	@Override
 	void loadUpdateString(AbstractDomainClass d) throws SQLException,
 			ClassNotFoundException {
 		throw new UnsupportedOperationException("Method is not available to this class");
 		
 	}
 
 	@Override
 	void loadDeleteString(AbstractDomainClass d) throws SQLException,
 			ClassNotFoundException {
 		throw new UnsupportedOperationException("Method is not available to this class");
 		
 	}
 
 	@Override
 	void loadInsertString(AbstractDomainClass d) throws SQLException,
 			ClassNotFoundException {
 		throw new UnsupportedOperationException("Method is not available to this class");
 		
 	}
 
 }
