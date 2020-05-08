 package ussr.remote.facade;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.List;
 
 import ussr.builder.helpers.BuilderHelper;
 import ussr.description.geometry.VectorDescription;
 import ussr.model.Module;
 import ussr.physics.PhysicsSimulation;
 import ussr.physics.jme.JMESimulation;
 
 public class SimulationTabControl extends UnicastRemoteObject implements SimulationTabControlInter{
 
 	/**
 	 * JME level simulation.
 	 */
 	private JMESimulation jmeSimulation;
 
 	public SimulationTabControl(JMESimulation jmeSimulation) throws RemoteException {
 		this.jmeSimulation = jmeSimulation;
 	}
 
 	
 	public void setModulePosition(int moduleID,VectorDescription newModulePosition)throws RemoteException{
 		
 		int amountModules = jmeSimulation.getModules().size();
 		for (int index=0; index<amountModules; index++){
 			
 			if (jmeSimulation.getModules().get(index).getID()==moduleID){
 				jmeSimulation.getModules().get(index).setPosition(newModulePosition);
 			}
 			
 		}
 	}
 	
 	public VectorDescription getModulePosition(int moduleID)throws RemoteException{
 		int amountModules = jmeSimulation.getModules().size();
 		for (int index=0; index<amountModules; index++){
 			
 			if (jmeSimulation.getModules().get(index).getID()==moduleID){
 				return  jmeSimulation.getModules().get(index).getPhysics().get(0).getPosition();
 			}
 			
 		}
 		return null;
 	}
 	
 	private int amountModules =0;
 	public void deleteModules(List<Integer> ids)throws RemoteException{
 		//jmeSimulation.getModules().clear();
 		for(int moduleID=0;moduleID<ids.size();moduleID++){
 			 amountModules = jmeSimulation.getModules().size();
 			
 			System.out.println("Size List:"+ jmeSimulation.getModules().size());
 		
 			
 			for (int index=0; index<amountModules; index++){				
 				System.out.println("ID:"+ ids.get(moduleID));
 				int currentModuleID = jmeSimulation.getModules().get(index).getID();
 				Module currentModule =jmeSimulation.getModules().get(index);
 				int moduleToDeleteID = ids.get(moduleID);
 				
 				if (currentModuleID==moduleToDeleteID ){
 					
					BuilderHelper.deleteModule(currentModule,false);
 					
 					PhysicsSimulation simulation= currentModule.getSimulation();
 					List<Module> modules = simulation.getModules();
 					modules.remove(currentModule);
 					JMESimulation jmeSimulation =(JMESimulation)simulation;
 					jmeSimulation.setModules(modules);
 					
 					//PhysicsSimulation physicsSimulation = currentModule.getSimulation();
 					//physicsSimulation.getModules().remove(currentModule);
 					
 					//amountModules = jmeSimulation.getModules().size();
 					break;
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 }
