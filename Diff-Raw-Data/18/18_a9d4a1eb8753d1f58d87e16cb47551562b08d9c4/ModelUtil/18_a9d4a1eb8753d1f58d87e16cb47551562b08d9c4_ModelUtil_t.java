 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.model;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.TimeoutException;
 import littlesmarttool2.comm.ProgrammingUpdateListener;
 import littlesmarttool2.comm.SerialController;
 import static littlesmarttool2.model.ConnectionType.*;
 import littlesmarttool2.util.JSON;
 
 /**
  *
  * @author marcher89
  */
 public class ModelUtil {
     
     static CameraBrand[] cameraBrands;
     static WireCommand[] wireCommands;
     static LANCCommand[] lancCommands;
     static IRCommand[] standardIRCommands;
     static List<IRCommand> customIRCommands;
     
     static HashMap<Command,Integer> commandMap;
     static int currentCommand;
     static int howManyTimesShouldITry = 2;
     private static String CUSTOM_IR_DIR = "CustomIRCommands";
     
     public static void LoadData() {
         cameraBrands = JSON.readObjectFromFile("cameraList.json", CameraBrand[].class);
         
         HashMap<String, CameraModel> map = new HashMap<>();
         
         //Mapping identifiers to camera models
         for (CameraBrand cameraBrand : cameraBrands) {
             for (CameraModel cameraModel : cameraBrand.getModels()) {
                 map.put(cameraModel.getIdentifier(), cameraModel);
             }
         }
         
         CameraModel[] models;
         int i;
         
         wireCommands = JSON.readObjectFromFile("WireCommandList.json", WireCommand[].class);
         lancCommands = JSON.readObjectFromFile("LANCCommandList.json", LANCCommand[].class);
         standardIRCommands = JSON.readObjectFromFile("IRCommandList.json", IRCommand[].class);
         
         customIRCommands = JSON.readObjectsFromDir(CUSTOM_IR_DIR, IRCommand.class);
         
         for (IRCommand command : customIRCommands) {
             models = new CameraModel[command.getCameraModels().length];
             i=0;
             for (CameraModel cModel : command.getCameraModels()) {
                 models[i++] = map.get(cModel.getIdentifier());
                 map.get(cModel.getIdentifier()).addIRCommand(command);
             }
             command.setCameraModels(models);
         }
         
         for (IRCommand command : standardIRCommands) {
             models = new CameraModel[command.getCameraModels().length];
             i=0;
             for (CameraModel cModel : command.getCameraModels()) {
                 models[i++] = map.get(cModel.getIdentifier());
                 map.get(cModel.getIdentifier()).addIRCommand(command);
             }
             command.setCameraModels(models);
         }
         
         for (WireCommand command : wireCommands) {
             models = new CameraModel[command.getCameraModels().length];
             i=0;
             for (CameraModel cModel : command.getCameraModels()) {
                 models[i++] = map.get(cModel.getIdentifier());
                 map.get(cModel.getIdentifier()).addWireCommand(command);
             }
             command.setCameraModels(models);
         }
         
         for (LANCCommand command : lancCommands) {
             models = new CameraModel[command.getCameraModels().length];
             i=0;
             for (CameraModel cModel : command.getCameraModels()) {
                 models[i++] = map.get(cModel.getIdentifier());
                 map.get(cModel.getIdentifier()).addLANCCommand(command);
             }
             command.setCameraModels(models);
         }
         System.out.println("Loaded " + cameraBrands.length + " camera brands.");
         System.out.println("Loaded " + (standardIRCommands.length+customIRCommands.size()) + " IR commands of which "+ customIRCommands.size() +" are custom.");
         System.out.println("Loaded " + lancCommands.length + " LANC commands.");
         System.out.println("Loaded " + wireCommands.length + " wire commands.");
     }
     
     /**
      * Saves the IR command to disc and in the internal data model.
      * @param command
      * @return True if it was successful otherwise false.
      */
     public static boolean saveCustomIRCommand(IRCommand command)
     {
        String filename = CUSTOM_IR_DIR+"/"+makeURLString(command.getName())+".json";
         if(JSON.fileExists(filename)){
             int i = 1;
            filename = filename.substring(0, filename.length()-5) + i + ".json";;
             while(JSON.fileExists(filename)){
                 i++;
                filename = filename.substring(0, filename.length()-6) + i + ".json";
             }
         }
        if (!JSON.writeObjectToFile(command, filename))
             return false;
         
         customIRCommands.add(command);
         return true;
     }
     
     public static void SendConfigurationToSnapper(Configuration conf, SerialController comm, ProgrammingUpdateListener listener) throws IOException, TimeoutException {
         commandMap = new HashMap<>();
         currentCommand = 1;
 
         System.out.print("Clearing EEPROM...");
         listener.update("Clearing old configuration");
         String response = comm.send('F', new String[]{"1"}, 5000, howManyTimesShouldITry);
         System.out.println("done");
         if(!response.equals("F;1"))//--------------------------------"F" Clear EEPROM
             throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to clear the EEPROM. Response: "+response);
         System.out.print("Switching connection type...");
         listener.update("Setting output type (" + conf.getOutputType() + ")");
         switch(conf.getOutputType()){
             case IR:
                 if(!comm.send('O', new String[]{"1"},10000, howManyTimesShouldITry).equals("O;1"))//------------------------"O" Output mode: IR
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the output mode to IR.");
                 break;
             case Wire:
                 if(!comm.send('O', new String[]{"2"},10000, howManyTimesShouldITry).equals("O;1"))//------------------------"O" Output mode: Wire
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the output mode to wire.");
                 break;
             case LANC:
                 if(!comm.send('O', new String[]{"3"},10000, howManyTimesShouldITry).equals("O;1"))//------------------------"O" Output mode: LANC
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the output mode to LANC.");
                 break;
         }
         System.out.println("done");
         
         if(!comm.send("N;1",10000, howManyTimesShouldITry).equals("N;1"))//-----------------"N" Turn output off
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to turn output off.");
         
         
         if(conf.isTimelapse())
             uploadTimelapse(conf, comm, listener);
         else
             uploadChannels(conf, comm, listener);
         
         
         listener.update("Saving configuration to the StratoSnapper");
         if(!comm.send("M",10000, howManyTimesShouldITry).equals("M;1"))//-----------------"M" Store to EEPROM
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to store configuration to EEPROM.");
     }
     
     private static void uploadTimelapse(Configuration conf, SerialController comm, ProgrammingUpdateListener listener) throws IOException, TimeoutException {
         listener.update("Setting timelapse configuration");
         System.out.println("Sending timelapse command...");
         int sendId = sendCommandToSnapper(comm, conf.getTimelapseCommand());
         
         if(!comm.send('U', new String[]{
             sendId+"",
             conf.getTimelapseDelay()+""
         },10000, howManyTimesShouldITry).equals("U;1"))//-----------------"N" Turn output off
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set timelapse configuration.");
         
                     
     }
     
     private static void uploadChannels(Configuration conf, SerialController comm, ProgrammingUpdateListener listener) throws IOException, TimeoutException {
         int rangeNumber = 1, triggerNumber = 1;
         String response;
 
         for (Channel channel : conf.getChannels()) {
             int servo = channel.getId();
             Setting setting = channel.getSetting();
 
             //Thresholds / Triggers
             listener.update("Sending triggers for channel " + channel.getId());
             for (Threshold threshold : setting.getThresholds()) {
                 if(threshold.getUpCommand() != Command.getNothingCommand()) {
                     
                     int sendId = sendCommandToSnapper(comm, threshold.getUpCommand());
                     
                     System.out.print("Sending trigger...");
                     response = comm.send('T', new String[]{//------------------------------"T" Trigger point
                         triggerNumber+"",//number
                         servo+"",//servo
                         channel.convertPromilleToValue(threshold.getValuePromille())+"",//trig point
                         "1",//going high
                         "0",//going low
                         "50",//hysteresis
                         sendId+""//command
                     }, 1000, howManyTimesShouldITry);
                     System.out.println("done");
                     if(!response.equals("T;1"))
                         throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to send a trigger point with an up command. Response: "+response);
                     triggerNumber++;
                 }
                 
                 if(threshold.getDownCommand() == Command.getNothingCommand()) 
                     continue;
                 
                 int sendId = sendCommandToSnapper(comm, threshold.getDownCommand());
                 
                 System.out.print("Sending trigger...");
                 response = comm.send('T', new String[]{//------------------------------"T" Trigger point
                     triggerNumber+"",//number
                     servo+"",//servo
                     channel.convertPromilleToValue(threshold.getValuePromille())+"",//trig point
                     "0",//going high
                     "1",//going low
                     "50",//hysteresis
                     sendId+""//command
                 },1000, howManyTimesShouldITry);
                 System.out.println("done");
                 if(!response.equals("T;1"))
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to a trigger point with a down command. Response: "+response);
                 triggerNumber++;
             } //End for (threshold : thresholds
 
             listener.update("Sending ranges for channel " + channel.getId());
             //Blocks / Ranges
             for (Block block : setting.getBlocks()) {
                 if(block.getCommand() == Command.getNothingCommand())
                     continue;
                 int minPromille = block.getLowerThreshold() != null ? block.getLowerThreshold().getValuePromille() : 0;
                 //Max is exclusive, except for the highest block
                 int maxPromille = block.getUpperThreshold() != null ? block.getUpperThreshold().getValuePromille()-1 : 1000;
 
                 int sendId = sendCommandToSnapper(comm, block.getCommand());
                 System.out.print("Sending range...");
                 response = comm.send('R', new String[]{//------------------------------"R" Range trigger
                     rangeNumber+"",//number
                     servo+"",//servo
                     channel.convertPromilleToValue(maxPromille)+"",//max point
                     channel.convertPromilleToValue(minPromille)+"",//min point
                     block.getInterval()+"",//timing range high
                     block.getInterval()+"",//timing range low
                     "1",//expo
                     sendId+""//command
                 },1000, howManyTimesShouldITry);
                 System.out.println("done");
                 if(!response.equals("R;1"))
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to a range trigger. Response: "+response);
                 rangeNumber++;
             } //End for (block : blocks)
         } //End for (channel : channels)
     }
 
     /**
      * Uploads and command to the StratoSnapper and returns the given id of the command.
      */
     private static int sendCommandToSnapper(SerialController comm, Command command) throws IOException, TimeoutException{
         
         if(command.getClass() == WireCommand.class){
             System.out.print("Sending wire pulse length");
             String response = comm.send('P', new String[]{//------------------------------------------------------------"L" LANC Command
                 ((WireCommand)command).getPulseLength()+""//time in ms
             },5000, howManyTimesShouldITry);
             if(!response.equals("P;1"))
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the wire pulse length. Response: "+response);
             System.out.println("done");
             return ((WireCommand)command).getPinConfig();
         }
         
         if(commandMap.containsKey(command)) return commandMap.get(command);
         
         if(command.getClass() == IRCommand.class){
             int commandId = currentCommand++;
             commandId++; //Using space 2-8 for IR commands
             commandMap.put(command, commandId);
             
             IRCommand ir = (IRCommand) command;
             int[] pulsdata = ir.getPulsedata();
             
             System.out.print("Sending IR repetition info...");
             if(!comm.send("G;"+commandId+";"+ir.getRepeats()+";"+ir.getDelayBetweenRepeats(),5000, howManyTimesShouldITry).equals("G;1"))//-----------------"G" IR Repetitions
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the IR repetitions property.");
             System.out.println("done");
             
             for (int puls = 1; puls <= (pulsdata.length/2); puls++) {
                 String[] cmds = new String[]{//----------------------------------------------------------------------"I" IR puls
                     commandId+"",//command
                     puls+"",//puls number
                     pulsdata[puls*2-2]+"",//timing high
                     pulsdata[puls*2-1]+""//timing low
                 };
                 System.out.print("Sending IR command part " + puls + " ...");
                 String response = comm.send('I', cmds,5000, howManyTimesShouldITry);
                 System.out.println("done");
                 if(!response.equals("I;1")) //TODO: what about repeats??
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set an IR pulse.\n"
                             + "Sent: "+Arrays.deepToString(cmds)+"\n"
                 + "Response: "+response);
             }
             if(!comm.send("K;"+ir.getFrequency(),5000, howManyTimesShouldITry).equals("K;1"))//----------------------------------------------"K" IR Frequency
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to set the IR frequency.");
             
             if(!comm.send("M",10000, howManyTimesShouldITry).equals("M;1"))//-----------------"M" Store to EEPROM
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to store configuration to EEPROM");
             return commandId;
         }
         else if(command.getClass() == LANCCommand.class){
             int commandId = currentCommand++;
             commandMap.put(command, commandId);
             
             LANCCommand lanc = (LANCCommand)command;
             System.out.print("Sending LANC command...");
             String response = comm.send('L', new String[]{//------------------------------------------------------------"L" LANC Command
                 commandId+"",//command
                 lanc.getCommandByte0()+"",//byte 0
                 lanc.getCommandByte1()+"" //byte 1
             },5000, howManyTimesShouldITry);
             if(!response.equals("L;1"))
                 throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to send a LANC command. Response: "+response);
             System.out.println("done");
             return commandId;
         }
         return -1;
     }
 
     private static String makeURLString(String string) {
         return string.replaceAll("[^(\\d|\\w|_| )]", "").replace(' ', '_');
     }
 }
