 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.model;
 
 import com.fasterxml.jackson.annotation.*;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.util.ArrayList;
 import littlesmarttool2.util.JSON;
 
 /**
  *
  * @author Rasmus
  */
 public class LANCCommand extends Command {
     
     private int commandByte0, commandByte1;
     
     private static LANCCommand[] array;
     
     public static LANCCommand[] getArray() {
         if(null==ModelUtil.lancCommands) 
             ModelUtil.LoadData();
         return ModelUtil.lancCommands;
     }
     
     @JsonCreator
     public LANCCommand(@JsonProperty("name") String name, @JsonProperty("description") String description, @JsonProperty("models") CameraModel[] models, @JsonProperty("commandByte0") byte commandByte0, @JsonProperty("commandByte1") byte commandByte1)
     {
         super(name, description, models);
         this.commandByte0 = commandByte0;
         this.commandByte1 = commandByte1;
     }
     
     public int getCommandByte0()
     {
         return commandByte0;
     }
     
     public int getCommandByte1()
     {
         return commandByte1;
     }
     
     public static void main(String[] args) throws Throwable /*Cough*/ {
         ArrayList<LANCCommand> cmds = new ArrayList<LANCCommand>();
         
         BufferedReader reader = new BufferedReader(new FileReader("LANC.csv"));
         
         CameraModel[] models = new CameraModel[0];
         for (CameraBrand cameraBrand : CameraBrand.getArray()) {
             if(cameraBrand.getBrandName().equals("Sony")){
                 models = cameraBrand.getModels();
                 break;
             }
         }
         
         String line = reader.readLine();
         int i = 0;
         while(line!=null){
             String[] thingies = line.split(";");
             if(thingies.length>2){
                 byte byte0 = (byte)Integer.parseInt(thingies[0], 16);
                 byte byte1 = (byte)Integer.parseInt(thingies[1], 16);
                 String name = thingies[2];
                 i++;
                 cmds.add(new LANCCommand(name, "", models, byte0, byte1));
             }
             
             line = reader.readLine();
         }
         LANCCommand[] commands = new LANCCommand[cmds.size()];
         i = 0;
         for (LANCCommand command : cmds) {
             commands[i++]=command;
         }
         JSON.writeObjectToFile(commands, "LANCCommandList-output.json");
         System.out.println("Found "+i+" non-epmty lines.");
     }
 }
