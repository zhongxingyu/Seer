 package org.hazelwire.main;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Scanner;
 
 /**
  * This is just for the first prototype, later on a more proper CLI will be build (and a GUI).
  * @author Tim Strijdhorst
  *
  */
 public class CLI implements TextInterface
 {	
 	boolean readyToStop;
 	
 	public static void main(String[] args)
 	{
 		new CLI().run();
 	}
 	
 	@Override
 	public void print(String message)
 	{
 		System.out.print(message);
 	}
 
 	@Override
 	public void println(String message)
 	{
 		System.out.println(message);
 	}
 	
 	public void printArrayList(ArrayList<String> message)
 	{
 		Iterator iterate = message.iterator();
 		
 		while(iterate.hasNext())
 		{
 			System.out.println(iterate.next());
 		}
 	}
 	
 	public void run()
 	{
 		Scanner scanner = new Scanner(System.in);
 		Generator generator = Generator.getInstance();
 		
 		while(!readyToStop)
 		{			
 			print("Command: ");
 			
 			if(scanner.hasNextLine())
 			{
 				handleCommand(scanner.nextLine());
 			}
 		}
 	}
 	
 	public void handleCommand(String commandLine)
 	{
 		ArrayList<String> params = createCommandList(commandLine);
 		String error = "";
 		
 		if(params.size() > 0)
 		{
 			String command = params.get(0).toLowerCase();
 			
 			try
 			{
 				/**
 				 * Command: list
 				 * Params[1]: what to list
 				 */
 				if(command.equals("list") && params.size() >= 1)
 				{
 					if(params.get(1).equals("selected"))
 					{
 						printArrayList(Generator.getInstance().getSelectedModules());
 					}
 					else if(params.get(1).equals("available"))
 					{
 						printArrayList(Generator.getInstance().getAvailableModules());
 					}
 				}
 				
 				
 				/**
 				 * Command: select
 				 * Params[1]: the id of the to be selected module
 				 */
 				else if(command.equals("select") && params.size() >= 1)
 				{
 					Generator.getInstance().getModuleSelector().selectModule(Integer.valueOf(params.get(1)));
 					println("I tried to select the module with id: "+params.get(1));
 				}
 				
 				
 				/**
 				 * Command: select
 				 * Params[1]: the id of the to be deselected module
 				 */
 				else if(command.equals("deselect") && params.size() >= 1)
 				{
					Generator.getInstance().getModuleSelector().deselectModule(Integer.valueOf(params.get(1)));
 					println("I tried to deselect the module with id: "+params.get(1));
 				}
 				
 				else if(command.equals("generate"))
 				{
 					Generator.getInstance().generateVM();
 					println("I tried to generate the vm, if it worked it's located at: "+Configuration.getInstance().getVMExportPath());
 				}
 				
 				else if(command.equals("exit"))
 				{
 					System.exit(0);
 				}
 			}
 			catch(Exception e)
 			{
 				
 			}
 		}
 	}
 	
 	public ArrayList<String> createCommandList(String commandLine)
 	{
 		Scanner scanner = new Scanner(commandLine);
 		ArrayList<String> params = new ArrayList<String>();
 		
 		if(scanner.hasNext()) params.add(scanner.next()); //the first command is delimited by a whitespace
 		scanner.useDelimiter(", *"); //after that they are delimited by comma
 		
 		//First 'param' is the actual command, the rest are parameters, C style
 		while(scanner.hasNext())
 		{
 			params.add(scanner.next());
 		}
 		
 		if(params.size() >= 2) params.set(1, params.get(1).substring(1)); //remove leading whitespace
 		return params;
 	}
 }
