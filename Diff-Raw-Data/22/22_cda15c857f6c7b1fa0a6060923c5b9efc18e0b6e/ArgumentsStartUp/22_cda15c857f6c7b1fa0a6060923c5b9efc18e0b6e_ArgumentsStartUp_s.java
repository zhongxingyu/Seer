 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app;
 
 import app.utils.MyLogger;
 import config.GUIConfiguration;
 import config.MainConfiguration;
 import java.awt.Dimension;
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 /**
  *
  * @author vara
  */
 public class ArgumentsStartUp {
     
     private boolean isArguments = false;
     private boolean exitProgram = false;
    private String infoParameters = "\n\t-v  \t(version) print version application" +
                     "\n\t-V  \t(verbose mode) result return by function only on console" +
 				    "\n\t-Vg \t(verbose mode) like -v + create window in gui with the same result on console" +
 				    "\n\t-sp \t(Show properties) create window in app with properties chart file" +
 				    "\n\t-c  \t(path to configuration file) not suported yet !!!"+
 				    "\n\t-f  \t(path to chart file) file format documents must by only svg !!!"+
                     "\n\t-ws \t(window size  eq. -ws 800 600 ) " +
                            "\n\t\t\tIt takes two arguments defining the size of the total number of main window." +
                            "\n\t\t\tIf there is to adopt the size of a window the size of screen resolution"+
                     "\n\t-fs  \t(Display root window in full screen mode)"+
 				    "\n\t-h  \t(Show this text)";
     
     /**
      *
      * @param arg
      */
     public ArgumentsStartUp(String [] arg){
         if(arg.length>0){
             try {
                 checkParameters(arg);                
                 MyLogger.log.log(Level.FINE,"Validate arguments status OK !");
             } catch (Exception ex) {
                MyLogger.log.log(Level.WARNING,ex.getMessage());
                 helpInformation(ex.getMessage());
             }
         }else{
             MyLogger.log.log(Level.FINE,"no arguments to start application");
         }
         if(exitProgram()){            
             MyLogger.log.log(Level.FINE,"Set exit program! EXIT");
             System.exit(0);
         }
     }
     
     private void checkParameters(String [] arg) throws Exception{
         //Vektor for positioning arguments
         VectorPositionForArgumentsStartup vecArgumentsOffset =new VectorPositionForArgumentsStartup(arg);
         ValidateParameter valid = new ValidateParameter();
 
         String paramWhithoutMarker = "";
         //number of arguments accepted by the function
         Integer numOfParam;
         
         while(true){
             try {
                 String param = vecArgumentsOffset.getNextParameter();
                 //System.out.println("Counter arg. offset "+vecArgumentsOffset.getCounter());
                 //System.out.print("\n"+param);
                 //'-' parameter marker
                 if(param.charAt(0)=='-'){
                     //check chars without parameter marker
                     if( !(paramWhithoutMarker=param.substring(1)).equals("") ){
 
                         if((numOfParam = valid.getInformationOnParameter(paramWhithoutMarker))!=null){
                             //System.out.println(" [num of org] "+numOfParam.intValue());
                             //If more than one argument
                             if(numOfParam.intValue()!=0){
                                 Parameter arrayForParam = new Parameter(paramWhithoutMarker);
                                 for(int i=1;i<=numOfParam.intValue();i++){
                                     //parameter with value (argument for parameter)
                                     String argument;
                                     if(vecArgumentsOffset.isNextParameter()){
 
                                         argument = vecArgumentsOffset.getNextParameter();                                        ;
                                         //check is next argument isn't next parameter
                                         if(argument.charAt(0)=='-'){
                                             throw new Exception("Missing argument for '-"+paramWhithoutMarker+"'"+
                                                     "Required number of arguments ["+numOfParam.intValue()+"]\tTry with '-h'");
                                         }
                                         //add argument to array
                                         //System.out.println(""+argument);
                                         arrayForParam.addValue(argument);
                                     }else{
                                         throw new Exception("Required number of arguments for '-"+paramWhithoutMarker+
                                                 "' ["+numOfParam.intValue()+"]\tTry with '-h'");
                                     }
                                 }//end for
 
                                 orderCommands(arrayForParam);
 
                             }else{//only one parameter (switch)
                                 orderCommands(new Parameter(paramWhithoutMarker,null));
                             }
 
                         }else{
                             throw new Exception("Unrecognized parameter '"+param+"'\tTry with '-h'");
                         }
 
                     }else{
                         throw new Exception("Bad parameter only '-' not suported\tTry with '-h'");
                     }
                 }else{
                     throw new Exception("Bad parameter , missing '-' in '"+param+"'\tTry with '-h'");
                 }
 
             } catch (IndexOutOfBoundsException ex) {                
                 //System.out.println("Koniec "+ex.getMessage());
                 //ex.printStackTrace();
                 break;//end while
             }
 
         }//while        
     }
     
     /**
      *
      * @param p
      * @throws java.lang.Exception
      */
     public void orderCommands(Parameter p) throws Exception{
 	
         switch(p.getCharParam()){
             case 'V':
 
                 MainConfiguration.setMode(true);
                 break;
             case 'f':
                 String path = p.getValue(0);
                 if(!(new File(path)).exists())
                     throw new Exception("This path to file doesn't exist "+path);
                 MainConfiguration.setPathChartToFile(path);
                 break;
             case 's'+'p':
 
                 MainConfiguration.setShowDocumentProperties(true);
                 break;
             case 'V'+'g':
 
                 MainConfiguration.setModeVerboseGui(true);
                 break;
             case 'h':
 
                System.out.println(Version.getVersion());
                 setExitProgram(true);
                 throw new Exception(getHelpInformation());
             case 'w'+'s':
                                 
                 {//sWidth or sHeight == null This situation will probably never take place. I hope ! ;D
                     String sWidth = p.getValue(0);
                     String sHeight = p.getValue(1);
                     int width;
                     int height;
                     if(sWidth!=null){
                         try{
                             width = Integer.parseInt(sWidth);
                         }catch(NumberFormatException e){
                             throw new Exception(e.getClass().getSimpleName()+": Width "+e.getMessage());
                         }
                     }else
                         throw new Exception("Error "+Parameter.class.getName()+" " +
                                             "method getValue(0) return null");
                     if(sHeight!=null){
                         try{
                             height = Integer.parseInt(sHeight);
                         }catch(NumberFormatException e){
                             throw new Exception(e.getClass().getSimpleName()+": Height "+e.getMessage());
                         }
                     }else
                         throw new Exception("Error "+Parameter.class.getName()+
                                             " method getValue(1) return null");
 
                     GUIConfiguration.setWindowSize(new Dimension(width, height));
                 }                          
                 break;
             case 'v':
                System.out.println(Version.getVersion());
                 setExitProgram(true);
                break;
             case 'f'+'s':
                 GUIConfiguration.setModeScreen(GUIConfiguration.FULL_SCREEN);
                 break;
             default:
                 setExitProgram(true);
                 throw new Exception("Unrecognized parameter '"+p.getParam()+"'\tTry with '-h'");
         }
     }
     
     /**
      *
      * @return
      */
     public String getHelpInformation(){
        return infoParameters;
     }
     
     /**
      *
      * @return
      */
     public boolean isArguments(){
         return isArguments;
     }
     
     /**
      *
      * @param val
      */
     public void setExitProgram(boolean val){
         exitProgram = val;
     }
     
     /**
      *
      * @return
      */
     public boolean exitProgram(){
         return exitProgram;
     }
     
     private void setArgumentsOn(){
         isArguments = true;
     }
 
     /**
      *
      * @param text
      */
     protected void helpInformation(String text){
         System.out.println(""+text);        
     }
 
     /**
      *
      */
     public class ValidateParameter {
 
         private HashMap <String,Integer> map = new HashMap<String,Integer>();
 
         /**
          *
          */
         public ValidateParameter(){
 
             map.put("Vg",0);
             map.put("V",0);
             map.put("sp",0);
             map.put("f",1);
             map.put("c",1);
             map.put("h",0);
             map.put("ws",2);
             map.put("v",0);
             map.put("fs",0);
         }
         /**
          *
          * @param val
          * @return
          */
         public Integer getInformationOnParameter(String val){
             return map.get(val);
         }
     }
 }
