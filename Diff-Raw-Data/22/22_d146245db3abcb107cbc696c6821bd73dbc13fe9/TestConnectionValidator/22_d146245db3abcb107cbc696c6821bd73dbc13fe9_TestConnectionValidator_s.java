 package hudson.plugins.sctmexecutor.validators;
 
 import hudson.plugins.sctmexecutor.Messages;
 import hudson.util.FormFieldValidator;
 
 import java.io.IOException;
 import java.net.URL;
 import java.rmi.RemoteException;
 
 import javax.servlet.ServletException;
 
 import com.borland.scc.sccsystem.SystemService;
 import com.borland.scc.sccsystem.SystemServiceServiceLocator;
 
 public class TestConnectionValidator extends FormFieldValidator {
   private String serviceURL;
   private String user;
   private String password;
   
   
   public TestConnectionValidator(String serviceURL, String user, String password) {
     super(null);
     this.serviceURL = serviceURL;
     this.user = user;
     this.password = password;
   }
 
 
   @Override
   protected void check() throws IOException, ServletException {
    if (!serviceURL.matches("http://((\\d{1,3}.){3}\\d{1,3})??|(\\p{Alnum}*)(:\\d{0,5})??(/\\p{Alnum}*)??/services")) { //$NON-NLS-1$
       error(Messages.getString("TestConnectionValidator.msg.noValidURL")); //$NON-NLS-1$
       return;
     }
     try {
       SystemService systemService = new SystemServiceServiceLocator().getsccsystem(new URL(serviceURL + "/sccsystem?wsdl")); //$NON-NLS-1$
       try {
         systemService.logonUser(user, password);
         ok(Messages.getString("TestConnectionValidator.msg.connectionSuccessful")); //$NON-NLS-1$
       } catch (RemoteException e) {
         error(Messages.getString("TestConnectionValidator.msg.wrongLoginData")); //$NON-NLS-1$
       }
     } catch (Exception e) {
       error(Messages.getString("TestConnectionValidator.msg.connectionFailed")); //$NON-NLS-1$
     }
   }
 
 }
