 package org.hackystat.sensor.xmldata.option;
 
 import java.util.List;
 
 import org.hackystat.sensor.xmldata.XmlDataController;
 
 /**
 * The -multishell option is deprecated and scheduled for removal. The XmlSensor will use whatever
 * setting is present in the SensorShellProperties file. 
 * 
  * The options that is used to notify the data sending Options that a
  * MultiSensorShell instance should be used instead of a single-threaded
  * SensorShell.
 * 
 * 
  * @author aito
  * 
  */
 public class MultiShellOption extends AbstractOption {
   /** This option's name, which is "-multishell". */
   public static final String OPTION_NAME = "-multishell";
 
   /**
    * Static factory method that creates an option with the specified controller
    * and parameters. The name of this option is set to "-multishell".
    * @param controller the specified controller.
    * @param parameters the specified parameters.
    */
   public MultiShellOption(XmlDataController controller, List<String> parameters) {
     super(controller, MultiShellOption.OPTION_NAME, parameters);
   }
 
   /** Processes this option by setting the multi-shell option to true. */
   @Override
   public void process() {
     if (this.isValid()) {
       this.getController().addOptionObject(Options.MULTI_SHELL, Boolean.TRUE);
     }
   }
 
   /**
    * Returns true if the list of parameters contains no parameters.
    * @return true if this option has no parameters, false if not.
    */
   @Override
   public boolean isValid() {
     if (!this.getParameters().isEmpty()) {
       String msg = "The " + OPTION_NAME + " option does not accept parameters.  ";
       this.getController().fireMessage(msg);
       return false;
     }
    this.getController().fireMessage("-multishell option ignored. Using SensorShellProperties.");
     return true;
   }
 }
