 package org.akquinet.audit.bsi.httpd;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import org.akquinet.audit.bsi.httpd.os.Quest1;
 import org.akquinet.audit.bsi.httpd.software.Quest2;
 import org.akquinet.audit.bsi.httpd.software.Quest3;
 import org.akquinet.audit.bsi.httpd.software.Quest4;
 import org.akquinet.audit.bsi.httpd.software.Quest5;
 import org.akquinet.audit.bsi.httpd.software.Quest6;
 import org.akquinet.audit.bsi.httpd.software.Quest7;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest10;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest11;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest12;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest8;
 import org.akquinet.audit.bsi.httpd.usersNrights.Quest9;
 import org.akquinet.audit.ui.UserCommunicator;
 import org.akquinet.httpd.ConfigFile;
 
 public class HttpdAudit
 {
 	enum OperatingSystem
 	{
 		Ubuntu,
 		SUSE,
 		RedHat,
 		UNKNOWN
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		Locale locale = Locale.getDefault();
 		for (String arg : args)
 		{
 			if(arg.equals("en"))
 			{
 				locale = Locale.ENGLISH;
 				break;
 			}
 			else if(arg.equals("de"))
 			{
 				locale = Locale.GERMAN;
 				break;
 			}
 		}
 		
 		try
 		{
 			ResourceBundle labels = ResourceBundle.getBundle("global", locale);
 			
 			if(!"root".equals(System.getenv("USER")))
 			{
 				//System.out.println("This application has to be run as root!");
 				System.out.println( labels.getString("E1_notRunAsRoot") );
 				System.exit(1);
 			}
 			
 			String apacheExec;
 			String apacheConf;
 			String tmp = "";
 			
 			OperatingSystem os = getOperatingSystem();
 			switch(os)
 			{
 			case Ubuntu:
 				apacheExec = "/usr/sbin/apache2";
 				apacheConf = "/etc/apache2/apache2.conf";
 				break;
 			case SUSE:
 				apacheExec = "/usr/sbin/httpd2";
 				apacheConf = "/etc/apache2/httpd.conf";
 				break;
 			case RedHat:
 				apacheExec = "/usr/sbin/httpd";
 				apacheConf = "/etc/httpd/httpd.conf";
 				break;
 			case UNKNOWN:
 			default:
 				apacheExec = "/usr/sbin/httpd";
 				apacheConf = "/etc/httpd/httpd.conf";
 				break;
 			}
 			
 			try
 			{
 				File htmlReport = new File("./htmlReport.htm");
 				System.out.println( MessageFormat.format(labels.getString("L1_willSaveHTML"), htmlReport.getCanonicalPath()) );
 				//System.out.println("I will save a detailed and more readable report of this audit in " + htmlReport.getCanonicalPath() + " .");
 				UserCommunicator.setDefault(new UserCommunicator(htmlReport));
 			}
 			catch (Exception e) //shouldn't happen
 			{
 				e.printStackTrace();
 			}
 			
 			UserCommunicator uc = UserCommunicator.getDefault();
 
 			for (String string : args)
 			{
 				if(string.equals("--fast"))
 				{
 					uc.setIgnore_WaitForUserToContinue(true);
 				}
 			}
 			
 			uc.printHeading1( labels.getString("H1") );
 			
 			uc.printParagraph( labels.getString("P1") );
 
 			uc.printParagraph( labels.getString("P2") );
 			
 			uc.printParagraph( labels.getString("P5") );
 			
 			uc.printParagraph( labels.getString("P3") );
 			uc.printExample("/usr/sbin/apache2    (Debian/Ubuntu)\n" +
 					"/usr/sbin/httpd      (RedHat)\n" +
 					"/usr/sbin/httpd2     (SUSE)");
 			
 			tmp = uc.askStringQuestion( labels.getString("Q1") , apacheExec);
 			apacheExec = "".equals(tmp.trim()) ? apacheExec : tmp;
 			File apacheExecutable = new File(apacheExec);
 
 			while(! apacheExecutable.exists() )
 			{
 				tmp = uc.askStringQuestion( MessageFormat.format(labels.getString("E2"), tmp) );
 				//tmp = uc.askStringQuestion(tmp + " doesn't exist. So what is your apache executable? ");
 				apacheExecutable = new File(tmp.trim());
 			}
 			
 			uc.println("");
 			
 			uc.println( labels.getString("L2") );
 			uc.printExample("/etc/apache2/apache2.conf    (Debian/Ubuntu)\n" +
 					"/etc/httpd/httpd.conf        (RedHat)\n" +
 					"/etc/apache2/httpd.conf      (SUSE)");
 			tmp = uc.askStringQuestion( labels.getString("Q2") , apacheConf);
 			apacheConf = "".equals(tmp.trim()) ? apacheConf : tmp;
 			File configFile = new File(apacheConf);
 			
 			while(! configFile.exists() )
 			{
 				tmp = uc.askStringQuestion( MessageFormat.format(labels.getString("E3"), tmp) );
 				//tmp = uc.askStringQuestion(tmp + " doesn't exist. So what is you apache main configuration file? ");
 				configFile = new File(tmp.trim());
 			}
 			ConfigFile conf = null;
 			try
 			{
 				conf = new ConfigFile(configFile);
 			}
 			catch(RuntimeException e)
 			{
 				uc.printParagraph(labels.getString("E4HttpdAudit"));
 				uc.printExample(e.getMessage());
 				uc.finishCommunication();
 				System.exit(1);
 			}
 			
 			uc.println("");
 			
 			boolean highSec = uc.askYesNoQuestion( labels.getString("Q3") , true);
 			
 			boolean highPriv = uc.askYesNoQuestion( labels.getString("Q4") , true);
 			
 			uc.println("");
 			uc.printParagraph( labels.getString("P4") );
 			
 			uc.waitForUserToContinue();
 			
 			uc.printHeading1( labels.getString("H2") );
 			
 			boolean overallAnswer = true;
 			
 
 			uc.printHeading2( labels.getString("H3") );
 			overallAnswer &= (new Quest1(highSec)).answer();
 			uc.waitForUserToContinue();
 			
 			uc.printHeading2( labels.getString("H4") );
 			boolean goodSyntax = (new Quest6(apacheExecutable)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= goodSyntax;
 			if(!goodSyntax)
 			{
 				uc.printParagraph( labels.getString("E5HttpdAudit") );
 				uc.finishCommunication();
 				System.exit(0);
 			}
 			overallAnswer &= (new Quest2(apacheExecutable)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest3(conf, apacheExecutable)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest4(conf, apacheExecutable)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest5(conf)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest7(conf)).answer();
 			uc.waitForUserToContinue();
 			
 			uc.printHeading2( labels.getString("H5") );
 			overallAnswer &= (new Quest8(configFile, conf, highSec)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest9(conf, apacheExecutable.getName(), highSec)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest10(conf)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest11(conf)).answer();
 			uc.waitForUserToContinue();
 			overallAnswer &= (new Quest12(conf, apacheExecutable.getName())).answer();
 			uc.waitForUserToContinue();
 			
 			if(overallAnswer)
 			{
 				uc.printHeading1( labels.getString("H6") );
 				uc.printParagraph( labels.getString("L3_ok") );
 			}
 			else
 			{
 				uc.printHeading1( labels.getString("H6") );
 				uc.printParagraph( labels.getString("L3_bad") );
 			}
 			
 			uc.finishCommunication();
 		}
 		catch (IOException e)
 		{
 			UserCommunicator.getDefault().finishCommunication();
 			e.printStackTrace();
 		}
 	}
 
 	private static OperatingSystem getOperatingSystem()
 	{
 		try
 		{
 			Process p = (new ProcessBuilder("./distro.sh")).start();
 			int exitVal = p.waitFor();
 			
 			switch(exitVal)
 			{
 			case 10:
 				return OperatingSystem.Ubuntu;
 			case 20:
 				return OperatingSystem.SUSE;
 			case 30:
 				return OperatingSystem.RedHat;
 			case 0:
 			default:
 				return OperatingSystem.UNKNOWN;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return OperatingSystem.UNKNOWN;
 		}
 	}
 
 }
