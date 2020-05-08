 package SABS;
 import resources.SABS.StartSABSHelper;
 import com.rational.test.ft.*;
 import com.rational.test.ft.object.interfaces.*;
 import com.rational.test.ft.object.interfaces.SAP.*;
 import com.rational.test.ft.object.interfaces.WPF.*;
 import com.rational.test.ft.object.interfaces.dojo.*;
 import com.rational.test.ft.object.interfaces.siebel.*;
 import com.rational.test.ft.object.interfaces.flex.*;
 import com.rational.test.ft.object.interfaces.generichtmlsubdomain.*;
 import com.rational.test.ft.script.*;
 import com.rational.test.ft.value.*;
 import com.rational.test.ft.vp.*;
 import com.ibm.rational.test.ft.object.interfaces.sapwebportal.*;
 import ru.sabstest.*;
 
 public class StartSABS extends StartSABSHelper
 {
 
 	public void testMain(Object[] args) 
 	{
 		try{		
 			String user = (String) args[0];
 			String pwd = (String) args[1];
 			String sign = (String) args[2];
			run(Settings.path + "\\purs_loader.exe",Settings.path + "\\bin");
 			Log.msg(" .");
 
 			Loginwindow().inputKeys(user + "{ENTER}" + pwd + "{ENTER}");
 			Log.msg(" .");
 			logTestResult("Login", true); 
 
 			SignComnfirmbutton().click();
 			SigncomboBox().select(sign);
 			Signokbutton().click();
 			sleep(3);
 			LoadSignwindow().inputKeys("{ENTER}");
 			//SignNextbutton().click();	
 			sleep(3);
 			LoadSignwindow().inputKeys("{ENTER}");
 			//SignDonebutton().click();
 			Log.msg(" .");
 
 			// Window: purs_loader.exe: 044582002    
 			SABSwindow().waitForExistence(15.0, 2.0);
 			SABSwindow().maximize();
 		} catch(Exception e) {
 			e.printStackTrace();
 			Log.msg(e);
 		}
 	}
 }
 
