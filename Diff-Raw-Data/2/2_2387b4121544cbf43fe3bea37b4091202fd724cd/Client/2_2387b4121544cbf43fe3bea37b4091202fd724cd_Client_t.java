 package start;
 
 import java.lang.reflect.Method;
 import java.util.Arrays;
 
 
 
 public class Client {
 
 	public static void main(String[] args) {
 
 		String clientName = null;
 
 		if ( (args == null) || (args.length == 0) ) {
 			clientName = "template";
 		} else {
 			clientName = args[0];
 		}
 
 		Class clientToStart = null;
 
 		if ("gricli".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.gricli.Gricli.class;
 		} else if ("template".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.frontend.view.swing.GrisuTemplateApp.class;
 		} else if ("virtscreen".equalsIgnoreCase(clientName)) {
 			clientToStart = org.bestgrid.virtscreen.view.GrisuVirtScreen.class;
 		} else if ("grid-tray".equalsIgnoreCase(clientName)) {
 			clientToStart = grith.gridsession.view.tray.GridSessionTrayClient.class;
 		} else if ("grid-session".equalsIgnoreCase(clientName)) {
 			clientToStart = grith.gridsession.CliSessionControl.class;
 		} else if ("ssh-setup".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.frontend.view.swing.utils.ssh.SshKeyCopyFrame.class;
 		} else if ("grython".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.Grython.class;
 		} else if ("gee".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.frontend.gee.Gee.class;
 		} else if ("phyml".equalsIgnoreCase(clientName)) {
 			clientToStart = phyml.PhymlMain.class;
 		} else if ("nesi-setup".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.frontend.view.swing.utils.ssh.wizard.NesiSetupWizard.class;
 		} else if ("benchmark-create".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.frontend.benchmark.Create.class;
 		} else if ("benchmark-submit".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.frontend.benchmark.Submit.class;
 		} else if ("benchmark-results".equalsIgnoreCase(clientName)) {
 			clientToStart = grisu.frontend.benchmark.Results.class;
         } else if ("goji".equalsIgnoreCase(clientName)) {
             clientToStart = nz.org.nesi.goji.view.cli.GojiCli.class;
         } else if ("envtest".equalsIgnoreCase(clientName)) {
             clientToStart = nz.org.nesi.envtester.view.swing.TestStarterFrame.class;
        } else if ("app-manage".equalsIgnoreCase(clientName)) {
            clientToStart = nz.org.nesi.appmanage.AppManage.class;
         } else if ("grisu".equalsIgnoreCase(clientName)) {
 
             Class grisuClass = null;
             try {
                 grisuClass = Class.forName("Grisu");
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 System.exit(1);
             }
 
             clientToStart = grisuClass;
 		} else {
 			System.out.println("Client "+clientName+" not available. Exiting...");
 			System.exit(1);
 		}
 
 		String[] args_new = new String[] {};
 		if (args.length > 1) {
 			args_new = Arrays.copyOfRange(args, 1, args.length);
 		}
 
 		try {
 			Method initMethod = clientToStart.getDeclaredMethod("main",
 					String[].class);
 
 			initMethod.invoke(null, (Object) args_new);
 
 		} catch (Exception e) {
 			System.err
 			.println("Can't start client: " + e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 	}
 
 
 }
