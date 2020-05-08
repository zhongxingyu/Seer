 package pt.ist.expenditureTrackingSystem.domain;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import myorg.domain.User;
 import myorg.domain.scheduler.WriteCustomTask;
 import net.sourceforge.fenixedu.domain.RemotePerson;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.ist.fenixframework.plugins.remote.domain.RemoteHost;
 import pt.ist.fenixframework.plugins.remote.domain.RemoteSystem;
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
 	    resultSetQuery = statementQuery
 		    .executeQuery("select fenix.USER.USER_U_ID, fenix.PARTY.PARTY_NAME, fenix.PARTY_CONTACT.VALUE, fenix.PARTY.OID from fenix.USER inner join fenix.PARTY on fenix.PARTY.OID = fenix.USER.OID_PERSON left join fenix.PARTY_CONTACT on fenix.PARTY_CONTACT.OID_PARTY = fenix.PARTY.OID and fenix.PARTY_CONTACT.OJB_CONCRETE_CLASS = 'net.sourceforge.fenixedu.domain.contacts.EmailAddress' and fenix.PARTY_CONTACT.TYPE = 'INSTITUTIONAL' group by fenix.USER.USER_U_ID;");
 	    int c = 0;
 	    int u = 0;
 	    while (resultSetQuery.next()) {
 		c++;
 		final String username = resultSetQuery.getString(1);
 		final String mlname = resultSetQuery.getString(2);
 		final String email = resultSetQuery.getString(3);
 		final String remotePersonOid = resultSetQuery.getString(4);
 		final User user = User.findByUsername(username);
 		if (user != null) {
 		    u = createNewUser(u, mlname, remotePersonOid, username);
 		}
 	    }
 	    System.out.println("Processed: " + c + " users.");
 	    System.out.println("Updated: " + u + " users.");
 	} catch (final InterruptedException e) {
 	    throw new Error(e);
 	} finally {
 	    if (resultSetQuery != null) {
 		resultSetQuery.close();
 	    }
 	    if (statementQuery != null) {
 		statementQuery.close();
 	    }
 	}
     }
 
     private static class PersonCreatorTask extends WriteCustomTask {
 
 	final String mlname;
 	final String remotePersonOid;
 	final String username;
 
 	int result = 0;
 
 	PersonCreatorTask(final String mlname, final String remotePersonOid, final String username) {
 	    this.mlname = mlname;
 	    this.remotePersonOid = remotePersonOid;
 	    this.username = username;
 	}
 
 	@Override
 	protected void doService() {
 	    final User user = User.findByUsername(username);
 	    final MultiLanguageString name = MultiLanguageString.importFromString(mlname);
 	    final String localizedName = name.getContent();
 
 	    final Person person = user.getExpenditurePerson();
 	    if (person != null) {
 		if (!localizedName.equals(person.getName())) {
 		    person.setName(name.getContent());
 		    result = 1;
 		}
 		// if (email != null && !email.equals(person.getEmail())) {
 		// person.setEmail(email);
 		// }
 	    }
 	    final module.organization.domain.Person organizationPerson = user.getPerson();
 	    if (organizationPerson != null) {
 		if (!localizedName.equals(organizationPerson.getName())) {
		    organizationPerson.setName(name.getContent());
 		    result = 1;
 		}
 		syncEmail(organizationPerson, remotePersonOid);
 	    }
 	}
     }
 
     private static int createNewUser(final int u, final String mlname, final String remotePersonOid, final String username)
 	    throws InterruptedException {
 	final int[] result = new int[] { u };
 	final Thread thread = new Thread() {
 	    @Override
 	    public void run() {
 		try {
 		    Transaction.begin(true);
 		    Transaction.currentFenixTransaction().setReadOnly();
 		    final PersonCreatorTask personCreatorTask = new PersonCreatorTask(mlname, remotePersonOid, username);
 		    personCreatorTask.doIt();
 		    result[0] += personCreatorTask.result;
 		} finally {
 		    Transaction.forceFinish();
 		}
 	    }
 	};
 	thread.start();
 	thread.join();
 	return result[0];
     }
 
     private static void syncEmail(final module.organization.domain.Person person, final String remotePersonOid) {
 	if (person != null) {
 	    RemotePerson remotePerson = person.getRemotePerson();
 	    final RemoteHost remoteHost = getRemoteHost();
 	    if (remoteHost != null) {
 		if (remotePerson == null) {
 		    remotePerson = new RemotePerson();
 		    remotePerson.setRemoteHost(remoteHost);
 		    remotePerson.setRemoteOid(remotePersonOid);
 		    person.setRemotePerson(remotePerson);
 		}
 		// final String email = remotePerson.getEmailForSendingEmails();
 		// if (email != null && !email.isEmpty()) {
 		// if (!email.equals(exPerson.getEmail())) {
 		// exPerson.setEmail(email);
 		// }
 		// } else {
 		// exPerson.setEmail(null);
 		// }
 	    }
 	}
     }
 
     private static RemoteHost getRemoteHost() {
 	// TODO : This is a hack... it should be selected when the person is
 	// first imported.
 	for (final RemoteHost remoteHost : RemoteSystem.getInstance().getRemoteHostsSet()) {
 	    return remoteHost;
 	}
 	return null;
     }
 
 }
