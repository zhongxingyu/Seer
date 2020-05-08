 package ussr.aGui.tabs.controllers;
 
 import java.util.Vector;
 
 import javax.swing.JRadioButton;
 
 import ussr.aGui.tabs.views.constructionTabs.AssignBehaviorsTab;
 import ussr.builder.SupportedModularRobots;
 import ussr.builder.controllerReassignmentTool.AssignControllerTool;
 import ussr.builder.helpers.BuilderHelper;
 import ussr.physics.jme.JMESimulation;
 
 public class AssignBehaviorsTabController {
 
 	private static Object[] strings;
 	
 	private static  Vector<String> classesOfControllers ;	
 
 
 	private static  Vector<String> tempClassesOfControllers =  new Vector<String> ()  ;
 	
 	//private static javax.swing.JList tempjList1;
 	
 	/**
 	 * The name of the package where all the controllers are stored for interactive assignment
 	 */
 	private static final String packageName = "ussr.builder.controllerReassignmentTool";
 
 
 	/**
 	 * Loads the names of the controllers existing in the package "ussr.builder.controllerReassignmentTool" into List
 	 * @param jList1
 	 */
 	public static void loadExistingControllers(javax.swing.JList jList1){
 		//tempjList1 = jList1;
 		Class[] classes = null;
 		try {
 			classes = BuilderHelper.getClasses(packageName);
 		} catch (ClassNotFoundException e) {
 			throw new Error ("The package named as: "+ packageName + "was not found in the directory of USSR");			
 		}		
 
 		/*Loop through the classes and take only controllers, but not the classes defining the tool*/
 		classesOfControllers = new Vector<String>();
 		for (int i=0; i<classes.length;i++){
 			if (classes[i].toString().contains("AssignControllerTool")||classes[i].toString().contains("ControllerStrategy")){
 				//do nothing	
 			}else{
 				classesOfControllers.add(classes[i].toString().replace("class "+packageName+".", ""));
 			}
 		}			
 		updateList(jList1,classesOfControllers);
 		/*Update the list with newly loaded names of controllers*/
 		
 	}
 	
 	public static Vector<String> getClassesOfControllers() {
 		return classesOfControllers;
 	}
 
 	public static void updateList(javax.swing.JList jList1,final Vector<String> controllers ){
 		jList1.setModel(new javax.swing.AbstractListModel() {
 			Object[] strings =  controllers.toArray();
 			public int getSize() { return strings.length; }
 			public Object getElementAt(int i) { return strings[i]; }
 		});		
 	}
 
 	public static void jList1MouseReleased(javax.swing.JList jList1,JMESimulation jmeSimulation) {
 		AssignBehaviorsTab.getJLabel10005().setVisible(false);
 		jmeSimulation.setPicker(new AssignControllerTool(packageName+"."+jList1.getSelectedValue()));
 	}
 
   public static void jButtonGroupActionPerformed(javax.swing.AbstractButton radionButton,JMESimulation jmeSimulation){
   
 	  if (radionButton.getText().contains("ATRON")){		  
 		  updateList(AssignBehaviorsTab.getJList1(),filterOut("ATRON"));
 	  }else if (radionButton.getText().contains("Odin")){
 		  updateList(AssignBehaviorsTab.getJList1(),filterOut("Odin"));
	  }else if (radionButton.getText().contains("MTran")){
 		  updateList(AssignBehaviorsTab.getJList1(),filterOut("MTRAN"));
 	  }else if (radionButton.getText().contains("CKBotStandard")){
 		  updateList(AssignBehaviorsTab.getJList1(),filterOut("CKBotStandard"));
 	  }
   }
   
   public static Vector<String> filterOut(String modularRobotName){
 	  tempClassesOfControllers.removeAllElements();
 	  for (int index=0; index<classesOfControllers.size();index++){
 		  if (classesOfControllers.get(index).contains(modularRobotName.toString())){
 			  tempClassesOfControllers.add(classesOfControllers.get(index));
 		  }
 	  }
 /*	  if (tempClassesOfControllers.isEmpty()){
 		  tempClassesOfControllers.add(filterValue + "is not supported yet");
 	  }*/
 	return tempClassesOfControllers;
 	  
   }
   
   
 
 }
