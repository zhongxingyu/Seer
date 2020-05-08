 package net.freifunk.autodeploy;
 
 import static com.google.inject.name.Names.named;
 
 import java.io.File;
 import java.util.List;
 import java.util.Set;
 
 import net.freifunk.autodeploy.device.Device;
 import net.freifunk.autodeploy.device.DeviceDeployer;
 import net.freifunk.autodeploy.firmware.Firmware;
 import net.freifunk.autodeploy.firmware.FirmwareConfigurator;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.openqa.selenium.WebDriver;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Ordering;
 import com.google.inject.ConfigurationException;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Key;
 import com.google.inject.TypeLiteral;
 
 /**
  * Main class for auto deployer.
  *
  * @author Andreas Baldeau <andreas@baldeau.net>
  */
 public class Main {
 
     private static final String HELP_OPTION = "h";
     private static final String LIST_MODELS_OPTION = "M";
     private static final String LIST_FIRMWARES_OPTION = "F";
     private static final String DEPLOY_ONLY_OPTION = "d";
     private static final String CONFIGURE_ONLY_OPTION = "c";
     private static final String FIRMWARE_OPTION = "f";
     private static final String FIRMWARE_IMAGE_OPTION = "i";
     private static final String PASSWORD_OPTION = "p";
     private static final String NODENAME_OPTION = "n";
     private static final String MODEL_OPTION = "m";
 
     private final Injector _injector;
 
     public Main() {
         _injector = Guice.createInjector(new AutoDeployModule());
     }
 
     /**
     * Main entry point for the auto deployer.
     */
     public static void main(final String[] args) {
         new Main().run(args);
     }
 
     private void run(final String[] args) {
         try {
             final Options options = new Options();
             options.addOption(new Option(HELP_OPTION, "help", false, "Show this help."));
             options.addOption(new Option(LIST_MODELS_OPTION, "list-models", false, "List supported models."));
             options.addOption(new Option(LIST_FIRMWARES_OPTION, "list-firmwares", false, "List supported firmwares."));
             options.addOption(new Option(DEPLOY_ONLY_OPTION, "deploy-only", false, "Only deploy the firmware to the device."));
             options.addOption(new Option(CONFIGURE_ONLY_OPTION, "configure-only", false, "Only configure the firmware on the device."));
             options.addOption(new Option(FIRMWARE_OPTION, "firmware", true, "The firmware to configure."));
             options.addOption(new Option(FIRMWARE_IMAGE_OPTION, "image", true, "The firmware image."));
             options.addOption(new Option(PASSWORD_OPTION, "password", true, "The new root password for the device."));
             options.addOption(new Option(NODENAME_OPTION, "nodename", true, "The name for the node. Will also be the hostname."));
             options.addOption(new Option(MODEL_OPTION, "model", true, "The model."));
 
             final CommandLineParser parser = new PosixParser();
             CommandLine commandLine;
             try {
                 commandLine = parser.parse(options, args);
             } catch (ParseException e) {
                 commandLine = null; // The compiler doesn't get the exit and thus complains about commandLine not being initialized.
                 System.err.println(e.getMessage());
                 System.exit(255);
             }
 
             if (commandLine.hasOption(HELP_OPTION)) {
                 HelpFormatter helpFormatter = new HelpFormatter();
                 helpFormatter.printHelp("ff-autodeploy", options);
                 System.exit(0);
             }
 
             if (commandLine.hasOption(LIST_FIRMWARES_OPTION)) {
                 listFirmwares();
                 System.exit(0);
             }
 
             if (commandLine.hasOption(LIST_MODELS_OPTION)) {
                 listModels();
                 System.exit(0);
             }
 
             final boolean deployOnly = commandLine.hasOption(DEPLOY_ONLY_OPTION);
             final boolean configureOnly = commandLine.hasOption(CONFIGURE_ONLY_OPTION);
 
             if (deployOnly && configureOnly) {
                 System.err.println("Specifying --deploy-only and --configure-only not allowed.");
                 System.exit(1);
             }
 
             final boolean deploy = deployOnly || !configureOnly;
             final boolean configure = configureOnly || !deployOnly;
 
             final String firmware;
             final File firmwareImage;
             if (deploy) {
             	firmware = getArgValue(commandLine, FIRMWARE_OPTION, "No firmware specified.", 2);
                 final String firmwareFileString = getArgValue(
                     commandLine,
                     FIRMWARE_IMAGE_OPTION,
                     "No firmware image specified.",
                     3
                 );
                 firmwareImage = new File(firmwareFileString);
 
                 if (!firmwareImage.exists()) {
                     System.err.println("File not found: " + firmwareImage.getPath());
                     System.exit(4);
                 }
 
                 if (!firmwareImage.isFile()) {
                     System.err.println("Not a file: " + firmwareImage.getPath());
                     System.exit(5);
                 }
             } else {
             	firmware = null;
                 firmwareImage = null;
             }
 
            final String model;
             final String password;
             final String nodename;
             if (configure) {
                model = getArgValue(commandLine, MODEL_OPTION, "Model not set.", 6) ;
                 password = getArgValue(commandLine, PASSWORD_OPTION, "Root password not set.", 7);
                 nodename = getArgValue(commandLine, NODENAME_OPTION, "Node name not set.", 8);
             } else {
                model = null;
                 password = null;
                 nodename = null;
             }
 
             final DeviceDeployer deployer = getDeployer(model);
             final FirmwareConfigurator configurator = getConfigurator(firmware);
 
             deployer.deploy(firmwareImage);
             configurator.configure(password, nodename);
         } finally {
             // tear down selenium
             _injector.getInstance(WebDriver.class).close();
         }
     }
 
     private static String getArgValue(
         final CommandLine commandLine,
         final String option,
         final String errorMessage,
         final int exitCode
     ) {
         final String value = Strings.nullToEmpty(commandLine.getOptionValue(option)).trim();
 
         if (value.isEmpty()) {
             System.err.println(errorMessage);
             System.exit(exitCode);
         }
 
         return value;
     }
 
     private void listFirmwares() {
         final Set<Firmware> supportedFirmwares = getSupportedFirmwares();
         final List<Firmware> sortedFirmwares = Ordering.<Firmware>natural().sortedCopy(supportedFirmwares);
 
         final StringBuilder output = new StringBuilder();
         output.append("Supported firmwares:");
         for (final Firmware firmware: sortedFirmwares) {
             output.append("\n    ");
             output.append(firmware.getName());
             output.append(" - ");
             output.append(firmware.getDisplayName());
         }
         System.out.println(output.toString());
     }
 
     private Set<Firmware> getSupportedFirmwares() {
         return _injector.getInstance(Key.get(new TypeLiteral<Set<Firmware>>(){}));
     }
 
     private void listModels() {
         final Set<Device> supportedDevices = getSupportedDevices();
         final List<Device> sortedDevices = Ordering.<Device>natural().sortedCopy(supportedDevices);
 
         final StringBuilder output = new StringBuilder();
         output.append("Supported models:");
         for (final Device device: sortedDevices) {
             output.append("\n    ");
             output.append(device.asString());
         }
         System.out.println(output.toString());
     }
 
     private Set<Device> getSupportedDevices() {
         return _injector.getInstance(Key.get(new TypeLiteral<Set<Device>>(){}));
     }
 
     private DeviceDeployer getDeployer(final String model) {
         try {
             return _injector.getInstance(Key.get(DeviceDeployer.class, named(model)));
         } catch (final ConfigurationException e) {
             System.err.println("No device deployer registered for model: " + model);
             System.exit(253);
             return null; // the compiler doesn't get the exit
         }
     }
 
     private FirmwareConfigurator getConfigurator(final String firmware) {
         try {
             return _injector.getInstance(Key.get(FirmwareConfigurator.class, named(firmware)));
         } catch (final ConfigurationException e) {
             System.err.println("No firmware configurator registered for firmware: " + firmware);
             System.exit(254);
             return null; // the compiler doesn't get the exit
         }
     }
 }
