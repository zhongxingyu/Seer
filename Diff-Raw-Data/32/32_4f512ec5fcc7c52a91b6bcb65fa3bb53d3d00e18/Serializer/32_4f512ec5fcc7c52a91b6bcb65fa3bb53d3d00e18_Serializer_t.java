 package core;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import modele.PerspectiveModel;
 
 public class Serializer {
 	
 	PerspectiveModel perspec;
 	private static Serializer instance;
 	
 	private Serializer(){}
 	
 	/**
 	 * Retourne l'instance
 	 * @return
 	 */
 	public static Serializer getInstance(){
 		if (instance == null)
 			instance = new Serializer();
 		return instance;
 	}
 	
 	public void serialize(String filename){
 		perspec = PerspectiveModel.getInstance();
 		
 		try
 	    {
 	       FileOutputStream fileOut = new FileOutputStream("serialize/"+filename+".ser");
 	       ObjectOutputStream out = new ObjectOutputStream(fileOut);
 	       out.writeObject(perspec);
 	       out.close();
 	       fileOut.close();
 	    }catch(IOException i){
 	        i.printStackTrace();
 	    }
 		
 	}
 	
 	public void deserialize(String fileName){
 		perspec = null;
 		
         try
         {
           FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream input = new ObjectInputStream(fileIn);
            perspec = (PerspectiveModel)input.readObject();
            PerspectiveModel.getInstance().setProperties(perspec);
            input.close();
            fileIn.close();
        }catch(IOException i){
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c){
            c.printStackTrace();
            return;
        }
 	}
 	
 }
