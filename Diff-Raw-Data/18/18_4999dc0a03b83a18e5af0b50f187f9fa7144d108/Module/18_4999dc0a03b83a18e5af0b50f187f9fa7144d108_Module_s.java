 /**
  * 
  */
 
 package com.zygon.trade;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A Module is intended to be a user facing element in the system. They hold
  * status information and any controllers that an operator might want to use
  * to interact with the underlying functionality.
  *
  * @author zygon
  */
 public abstract class Module implements OutputProvider, CommandProcessor, Installable, Configurable {
 
     private final String name;
     private final Logger logger;
     private final Schema schema; 
    private final Schema childSchema;
     private final Set<String> commands = new HashSet<>();
      
     private Module parent = null;
     private Configuration configuration; // is it easier to set a config once
                                          // and mutate it - or keep resetting it?
    protected Module(String name, Schema schema, Schema childSchema, Collection<String> supportedCommands) {
         this.name = name;
         this.logger = LoggerFactory.getLogger(this.name);
         
         this.schema = schema;
         this.childSchema = childSchema;
         
         if (supportedCommands != null) {
             for (String cmdName : supportedCommands) {
                 this.commands.add(cmdName);
             }
         }
     }
     
    protected Module(String name, Schema schema, Schema childSchema) {
         this(name, schema, childSchema, null);
     }
     
     protected Module(String name) {
         this(name, null, null);
     }
 
     @Override
     public void configure(Configuration configuration) {
         // TODO: inspect properties and take action probably before setting
         // the perm config
         this.configuration = configuration;
     }
     
     private void createChild(String cls, String[] configuration) {
         Configuration config = new Configuration(); // TODO: feed in args
         
         Class<?> clazz = null;
         
         try {
             clazz = Class.forName(cls);
         } catch (ClassNotFoundException cnfe) {
             logger.error(null, cnfe);
         }
         
         Module instance = null;
         
         try {
             instance = (Module) clazz.getConstructor().newInstance();
         } catch (Exception e) {
             logger.error(null, e);
         }
         
         instance.install();
         
         instance.configure(config);
         instance.setParent(this);
         
         // TBD: how are children persisted? ?
         
         instance.initialize();
     }
     
     /*pkg*/ void doInit() {
         
         Module[] modules = this.getModules();
         
         if (modules != null) {
             for (Module child : this.getModules()) {
                 child.setParent(this);
             }
         }
         
         this.logger.info("Initializing module {}", this.name);
         
         boolean initialized = false;
         
         try {
             this.initialize();
             initialized = true;
         } catch (Exception e) {
             this.logger.error("Exception occurred while initializing module " + this.name, e);
         }
         
         if (initialized) {
             if (modules != null) {
                 for (Module child : this.getModules()) {
                     child.doInit();
                 }
             }
         }
     }
     
     /*pkg*/ void doUninit() {
         
         this.logger.info("Unintializing module {}", this.name);
         
         Module[] modules = this.getModules();
         if (modules != null) {
             for (Module child : this.getModules()) {
                 child.doUninit();
             }
         }
         
         this.uninitialize();
     }
 
     @Override
    public Schema getChildSchema() {
         return this.childSchema;
     }
 
     @Override
     public Configuration getConfiguration() {
         return this.configuration;
     }
 
     protected final Logger getLogger() {
         return this.logger;
     }
     
     private Module getModule(Module module, String id) {
         if (module.name.equals(id)) {
             return module;
         } else {
             Module[] children = module.getModules();
             if (children != null) {
                 Module mod = null;
                 for (Module m : children) {
                     mod = getModule(m, id);
                     if (mod != null) {
                         break;
                     }
                 }
                 
                 return mod;
             }
         }
         
         return null;
     }
     
     protected Module getModule(String id) {
         Module root = this.getRoot();
         return this.getModule(root, id);
     }
     
     public abstract Module[] getModules();
     
     @Override
     public final String getDisplayname() {
         return this.name;
     }
 
     @Override
     public final String getNavigationElementToken() {
         return this.name.toLowerCase().replace(" ", "");
     }
     
     @Override
     public Response getOutput(Request request) {
         String output = "";
         
         if (request.isCommandRequest()) {
             String commandName = request.getCommandName();
             
             if (this.commands.contains(commandName)) {
                 Command command = new Command(commandName, request.getArgs());
                 CommandResult result = this.process(command);
                 if (!result.isSuccessful()) {
                     String outputMessage = "Command failed due to: " + result.getMessage();
                     logger.debug(outputMessage);
                     output = outputMessage;
                 }
             } else {
                 String outputMessage = "No command found for: " +request.getCommandName();
                 logger.debug(outputMessage);
                 output = outputMessage;
             }
             
         } else if (request.isListCommandRequest()) {
             StringBuilder sb = new StringBuilder();
             
             if (this.hasSchema()) {
                 sb.append(Command.EDIT);
                 this.writeProperties(sb, this.schema);
                 sb.append('\n');
             }
             
             if (this.hasChildSchema()) {
                 sb.append(Command.CREATE);
                 this.writeProperties(sb, this.childSchema);
                 sb.append('\n');
             }
             
             output = sb.toString();
         }
         
         return new Response(output);
     }
     
     public Module getParent() {
         return this.parent;
     }
     
     public final Module getRoot() {
         Module node = this;
         
         while (node.parent != null) {
             node = node.parent;
         }
         
         return node;
     }
 
     @Override
     public Schema getSchema() {
         return this.schema;
     }
     
     protected final boolean hasChildSchema() {
         return this.childSchema != null;
     }
     
     protected final boolean hasSchema() {
         return this.schema != null;
     }
     
     public abstract void initialize();
 
     @Override
     public void install() {
         logger.info("Installing " + this.getDisplayname());
         
         // TODO: persist meta information
         
         logger.info("Installation of " + this.getDisplayname() + " complete");
     }
 
     @Override
     public CommandResult process(Command command) {
         return CommandResult.SUCCESS;
         
 //        CommandResult result = null;
 //        
 //        if (command.isEditRequest()) {
 //            if (this.hasSchema()) {
 //                if (command.hasArguments()) {
 //                    
 //                    // TODO: link up the args with the schema
 //                    
 //                } else {
 //                    // do we care?
 //                }
 //                
 //            } else {
 //                logger.error("Unable to process edit request. No schema.");
 //                throw new IllegalArgumentException("Unable to process edit request. No schema.");
 //            }
 //        } else if (command.isCreateRequest()) {
 //            if (this.hasChildSchema()) {
 //                
 //                // TODO
 //            } else {
 //                logger.error("Unable to process create request. No child schema.");
 //                throw new IllegalArgumentException("Unable to process create request. No child schema.");
 //            }
 //        }
 //        
 //        return result;
     }
     
     private void setParent(Module parent) {
         this.parent = parent;
     }
     
     public abstract void uninitialize();
 
     @Override
     public void uninstall() {
         logger.info("Uninstalling " + this.getDisplayname());
         
         // TODO: remove meta information
         
         logger.info("Uninstallation of " + this.getDisplayname() + " complete");
     }
     
     // TBD: an output controller
     private void writeProperties (StringBuilder sb, Schema schema) {
         for (Property prop : schema.getProperties()) {
             if (prop.hasOptions()) {
                 sb.append(" <|");
                 String[] options = prop.getOptions();
                 for (int i = 0; i < options.length; i++) {
                     sb.append(options[i]);
                     if (i < options.length - 1) {
                         sb.append("|");
                     }
                 }
                 sb.append("|>");
             } else if (prop.hasDefault()) {
                 sb.append(" <").append(prop.getName()).append("|").append(prop.getDefaultValue()).append(">");
             } else {
                 sb.append(" <").append(prop.getName()).append(">");
             }
         }
     }
 }
