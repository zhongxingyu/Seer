 package ClassAdminBackEnd;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Date;
 import java.util.LinkedList;
 
 public class EntityTypeFactory {
 	public EntityType makeEntityType(){
 		try{
 			EntityType eT;
 
 			System.out.println("Enter the name of the entity type");
 
 			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
 			String name = bufferRead.readLine();
 			
 
 			eT = new EntityType(name);
 			eT.setIndex(Global.getGlobal().getActiveProject().getEntityTypes().size());
 			Global.getGlobal().getActiveProject().getEntityTypes().add(eT);
 
 			return eT;
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
	public EntityType makeEntityTypeCSV(String name, Boolean isTextField){
 		
 		EntityType eT = new EntityType(name,null, null, null, null, null, null, isTextField, null, true, 0.0);
 		eT.setIndex(Global.getGlobal().getActiveProject().getEntityTypes().size());
 		Global.getGlobal().getActiveProject().getEntityTypes().add(eT);
 		
 		return eT;
 		
 	}
 }
