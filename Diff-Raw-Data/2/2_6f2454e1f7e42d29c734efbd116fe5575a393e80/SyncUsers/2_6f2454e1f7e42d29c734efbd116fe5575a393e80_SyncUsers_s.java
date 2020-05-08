 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import myorg.domain.User;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.pstm.Transaction;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class SyncUsers extends SyncUsers_Base {
 
 
     public SyncUsers() {
         super();
     }
 
     @Override
     public void executeTask() {
 	try {
 	    syncData();
 	} catch (final IOException e) {
 	    throw new Error(e);
 	} catch (final SQLException e) {
 	    throw new Error(e);
 	}
     }
 
     @Override
     public String getLocalizedName() {
 	return getClass().getName();
     }
 
     @Service
     public static void syncData() throws IOException, SQLException {
 	final Connection connection = Transaction.getCurrentJdbcConnection();
 
 	Statement statementQuery = null;
 	ResultSet resultSetQuery = null;
 	try {
 	    statementQuery = connection.createStatement();
	    resultSetQuery = statementQuery.executeQuery("select fenix.USER.USER_U_ID, fenix.PARTY.PARTY_NAME, fenix.PARTY_CONTACT.VALUE from fenix.USER inner join fenix.PARTY on fenix.PARTY.ID_INTERNAL = fenix.USER.KEY_PERSON inner join fenix.PARTY_CONTACT on fenix.PARTY_CONTACT.KEY_PARTY = fenix.PARTY.ID_INTERNAL where fenix.PARTY_CONTACT.OJB_CONCRETE_CLASS = 'net.sourceforge.fenixedu.domain.contacts.EmailAddress' and fenix.PARTY_CONTACT.TYPE = 'INSTITUTIONAL' group by fenix.USER.USER_U_ID;");
 	    int c = 0;
 	    int u = 0;
 	    while (resultSetQuery.next()) {
 		c++;
 		final String username = resultSetQuery.getString(1);
 		final String mlname = resultSetQuery.getString(2);
 		final String email = resultSetQuery.getString(3);
 		final User user = User.findByUsername(username);
 		if (user != null) {
 		    final Person person = user.getExpenditurePerson();
 		    if (person != null) {
 			final MultiLanguageString name = MultiLanguageString.importFromString(mlname);
 			final String localizedName = name.getContent();
 			if (!localizedName.equals(person.getName())) {
 			    person.setName(name.getContent());
 			    u++;
 			}
 			if (!email.equals(person.getEmail())) {
 			    person.setEmail(email);
 			}
 		    }
 		}
 	    }
 	    System.out.println("Processed: " + c + " users.");
 	    System.out.println("Updated: " + u + " users.");
 	} finally {
 	    if (resultSetQuery != null) {
 		resultSetQuery.close();
 	    }
 	    if (statementQuery != null) {
 		statementQuery.close();
 	    }
 	}
     }
 
 }
