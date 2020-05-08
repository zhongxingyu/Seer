 package cyberwaste.kuzoff.driver;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import com.martiansoftware.jsap.FlaggedOption;
 import com.martiansoftware.jsap.JSAP;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.Parameter;
 import com.martiansoftware.jsap.SimpleJSAP;
 
 import cyberwaste.kuzoff.CommandRunner;
 
 public class SimpleClient {
     
     private static final String DATABASE_FOLDER_OPTION = "database";
     private static final String COMMAND_NAME_OPTION = "command";
     private static final String PARAMETERS_OPTION = "parameters";
 
     public static void main(String[] args) {
         try {
             JSAP jsap = new SimpleJSAP(
                "kuzoff", 
                "", 
                new Parameter[] {
                    new FlaggedOption(DATABASE_FOLDER_OPTION)
                        .setShortFlag('d').setLongFlag("database").setUsageName("database root folder").setRequired(true),
                    new FlaggedOption(COMMAND_NAME_OPTION)
                        .setShortFlag('c').setLongFlag("command").setUsageName("command to execute").setRequired(true),
                    new FlaggedOption(PARAMETERS_OPTION)
                        .setShortFlag('p')
                        .setLongFlag("parameters")
                        .setUsageName("command parameters (name_1=value_1;...name_k=value_k)")
                        .setRequired(false)
                }
            );
             
            JSAPResult result = jsap.parse(args);
            if (result.success()) {
                String databaseFolder = result.getString(DATABASE_FOLDER_OPTION);
                CommandRunner commandRunner = new CommandRunner(databaseFolder);
                
                String commandName = result.getString(COMMAND_NAME_OPTION);
               Map<String, String> parameters = parseParameters(result.getString(PARAMETERS_OPTION, ""));
                commandRunner.runCommand(commandName, parameters);
            } else {
                System.err.println("ERROR: Wrong command syntax.\nUsage:\n" + jsap.getUsage());
                System.exit(1);
            }
         } catch (Exception e) {
             System.err.println("ERROR: " + e.getMessage());
             e.printStackTrace(System.err);
             System.exit(1);
         }
     }
 
     private static Map<String, String> parseParameters(String parametersAsString) throws IOException {
         Properties properties = new Properties();
         properties.load(new StringReader(parametersAsString.replace(';', '\n')));
         
         Map<String, String> result = new HashMap<String, String>();
         for (String propertyName : properties.stringPropertyNames()) {
             result.put(propertyName, properties.getProperty(propertyName));
         }
         
         return result;
     }
 }
