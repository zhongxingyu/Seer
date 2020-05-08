 package fhbrs.ooka.ateam.components;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.net.*;
 
 public class RTEMenu {
 
 	private enum MenuName {MAIN, COMPONENTS, COMPONENT, ADD, EXIT, SOURCES};
 	private MenuName name = MenuName.MAIN;
 	private ComponentManager manager;
     private ArrayList<IComponent> components;
     private boolean running;
     
 	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 	private String input;
 	private ComponentRepository repo;
 	    
 	public RTEMenu(ComponentManager manager, ComponentRepository repo){
 		this.manager = manager;
 		this.repo = repo;
 	}	
 	
 	public void menu() throws Exception{
 		switch (name){
 			case MAIN: mainMenu();
 			break;
 	
 			case COMPONENTS: componentsMenu();
 			break;
 			
 			case COMPONENT: componentMenu();
 			break;
 			
 			case ADD: addMenu();
 			break;
 			
 			case SOURCES: sourcesMenu();
 			break;
 
 			case EXIT: exitMenu();
 			break;
 		}
 	}
 	
 	private void mainMenu() throws IOException{
 		System.out.println("Please enter a number to choose an option:");
 		System.out.println("1: Show all loaded Components");
 		System.out.println("2: Show all running Components");
 		System.out.println("3: Add a component");
 		System.out.println("4: Show all Sources");
 		System.out.println("0: Exit");
 		input = in.readLine();
 		if(input.equals("1")){
 			running = false;
 			name = MenuName.COMPONENTS;
 		}
 		else if(input.equals("2")){
 			running = true;
 			name = MenuName.COMPONENTS;
 		}
 		else if(input.equals("3")){
 			name = MenuName.ADD;
 		}
 		else if(input.equals("4")){
 			name = MenuName.SOURCES;
 		}
 		else if(input.equals("0") || input.equals("exit")){
 			name = MenuName.EXIT;
 		}
 		else{
 			System.out.println(input);
 			System.out.println("Invalid input!");
 		}
 	}
 	
 	private void componentsMenu() throws IOException{
 		if (running){
 			components = manager.getRunningComponents();
 			System.out.println("Currently running components.");
 		}
 		else{
 			components = manager.getAllComponents();
 			System.out.println("List of all components.");			
 		}
 		for (int i=0; i<components.size(); i++){
 			System.out.println("Component "+(i+1)+": "+components.get(i).getName());
 		}
 		System.out.println("\nFor more information on a component " +
 				"please enter the name or number of it.");
 		System.out.println("Enter 0 or 'back' to return to the main Menu.");
 
 		input = in.readLine();
 		if (input.equals("0") || input.equals("back")){
 			name = MenuName.MAIN;
 		}
 		else if(manager.findByName(input) != null){
 			name = MenuName.COMPONENT;
 		}
 		else{
 			System.out.println("Invalid input!");
 		}
 	}
 	
 	private void componentMenu() throws Exception{
 		String state = "stopped";
 		String toggle = "start";
 		IComponent comp = manager.findByName(input);
 		if (comp.getState()==ComponentState.Running){
 			state="running";
 			toggle = "stop";
 		}
 		System.out.println("Component name: "+comp.getName());
 		System.out.println("Component description: "+comp.getDescription());
 		System.out.println("Component state: "+state);
 		System.out.println("\nEnter 1 or '"+toggle+"'to "+toggle+"the component");
 		System.out.println("Enter 2 or 'remove' to remove the component from the RTE");
 		System.out.println("Enter 0 or 'back' to return to the main Menu.");
 
 		input = in.readLine();
 		if (input.equals("0") || input.equals("back")){
 			name = MenuName.MAIN;
 		}
 		else if(input.equals("1") || input.equals(toggle)){
 			if (comp.getState()==ComponentState.Running)
 				comp.stop();
 			else
 				comp.start();
 		}
 		else if(input.equals("2") || input.equals("remove")){
 			manager.remove(comp);
 			System.out.println("Component "+comp.getName()+" has been removed from the RTE.");
 			name = MenuName.MAIN;
 		}
 		else{
 			System.out.println("Invalid input!");
 		}
 	}
 
 	private void addMenu() throws IOException{
 		System.out.println("Please enter the classname of the component you want to add");
 		System.out.println("Enter 0 or 'back' to return to the main menu.");
 		input = in.readLine();
 		if (input.equals("0") || input.equals("back")){
 			name = MenuName.MAIN;
 		}
 		else{
 			try{
 				IComponent comp = repo.createComponent(input);
 				manager.add(comp);
 				System.out.println("Compnent has been added.");
 				name = MenuName.MAIN;
 			}
 			catch (Exception e){
 				System.out.println("The specified component could not be found.");
 			}
 		}
 	}
 	
 	private void sourcesMenu() throws IOException{
 		System.out.println("List of all known sources");
 		for (int i=0; i<repo.getSources().size(); i++){
 			System.out.println(repo.getSources().get(i));
 		}
 		System.out.println("Enter 1 or 'add' to add a source.");
 		System.out.println("Enter 0 or 'back' to return to the main menu.");
 		input = in.readLine();
 		if (input.equals("0") || input.equals("back")){
 			name = MenuName.MAIN;
 		}
 		else if(input.equals("1") || input.equals("add")){
 			System.out.println("Please enter the URL or filepath to the source.");
 			System.out.println("Enter 0 or 'back' to return to the source menu.");
 			input = in.readLine();
 			if (input.equals("0") || input.equals("back")){
 				return;
 			}
 			try{
 				repo.getSources().add((new URL(input)));
 				System.out.println("Source has been added to the repository.");				
 			}
 			catch(MalformedURLException me){
 				System.out.println("Invalid URL or file path.");
 			}
 		}
 	}
 	
 	private void exitMenu() throws IOException{
 		System.out.println("Are you sure you want to close the RTE? (yes/no)");
 		input = in.readLine();
 		if (input.equals("yes") || input.equals("y")){
 			System.out.println("Goodbey!");
 			System.exit(0);
 		}
 		else if (input.equals("no") || input.equals("n")){
 			name = MenuName.MAIN;
 		}
 		else{
 			System.out.println("Invalid input!");
 		}
 	}
 }
