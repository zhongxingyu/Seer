 package net.mms_projects.tostream.ui.cli.commands;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import net.mms_projects.tostream.Manager;
 import net.mms_projects.tostream.Service;
 import net.mms_projects.tostream.Settings;
 import net.mms_projects.tostream.managers.ServiceManager;
 import net.mms_projects.tostream.ui.cli.Command;
 import net.mms_projects.tostream.ui.cli.ResourcePasser;
 import net.mms_projects.utils.InlineSwitch;
 
 public class Setup extends Command {
 
 	public Setup() {
 		command = "setup";
 	}
 	
 	@Override
 	public boolean run(String[] args, ResourcePasser resources) {
 		final BufferedReader br = new BufferedReader(new InputStreamReader(
 				System.in));
 		if (args[1].equalsIgnoreCase("stream")) {
			InlineSwitch<Integer, String> switcher = new InlineSwitch<Integer, String>();
 			switcher.addClause(Service.AUTH_METHOD_TOKEN, "token");
 			switcher.addClause(Service.AUTH_METHOD_USERNAME, "username");
 			
 			final ServiceManager manager = new ServiceManager();
 			System.out.println("Setting up stream...");
 			
 			for (String serviceName : manager.getItemNames()) {
 				System.out.print(" * ");
 				System.out.print("[" + manager.getItemIndex(serviceName) + "]\t");
 				System.out.println(serviceName + "\t<" + switcher.runSwitch(manager.getItem(serviceName).authMethod) + ">");
 			}
 			System.out.print("Type the number of the streaming service: ");
 			String serviceKey = null;
 			try {
 				serviceKey = manager.getItemName(Integer.parseInt(br.readLine().trim()));
 				manager.setCurrentItem(serviceKey);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 			System.out.println("Service: " + manager.getCurrentItem().name);
 			
 			System.out.println("This service uses a " + switcher.runSwitch(manager.getCurrentItem().authMethod) + " based authentification method.");
 			
 			switcher.runSwitch(Service.AUTH_METHOD_TOKEN, new Runnable() {
 				@Override
 				public void run() {
 					System.out.print("Enter your streaming token: ");
 					try {
 						manager.getCurrentItem().setToken(br.readLine().trim());
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 			try {
 				resources.settings.set(Settings.STREAM_URL, manager.getCurrentItem().getStreamUrl());
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			System.out.println("The streaming URL has been set to: " + manager.getCurrentItem().getStreamUrl());
 		}
 		return super.run(args, resources);
 	}
 	
 }
