 
 package com.zygon.trade;
 
 import com.zygon.command.Command;
 import com.zygon.command.CommandResult;
 import com.zygon.schema.ConfigurationSchema;
 import java.util.Collection;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The purpose of this class is to abstract away the responsibilities of 
  * advertising, creating, deleting children
  *
  * @author zygon
  */
 public abstract class ParentModule extends Module {
 
    private final Class<Module> childClazz;
     private final Logger logger;
     private final ConfigurationSchema childSchema;
     
     public ParentModule(String name, Schema schema, Collection<String> supportedCommands, Class<Module> childClazz) {
         super(name, schema, supportedCommands);
         
         this.childClazz = childClazz;
         this.logger = LoggerFactory.getLogger(name);
         
         Module instance = null;
         
         try {
             instance = (Module) this.childClazz.getConstructor(String.class).newInstance("foo");
         } catch (Exception e) {
             logger.error(null, e);
             throw new RuntimeException();
         }
         
         this.childSchema = instance.getSchema();
     }
 
     public ParentModule(String name, Schema schema, Class<Module> childClazz) {
         this (name, schema, null, childClazz);
     }
 
     public ParentModule(String name, Class<Module> childClazz) {
         this(name, null, null, childClazz);
     }
     
     private void createChild(String[] configuration) {
         Configuration config = null; //new Configuration(); // TODO: feed in args
         
         Module instance = null;
         
         try {
             instance = (Module) this.childClazz.getConstructor().newInstance();
         } catch (Exception e) {
             logger.error(null, e);
         }
         
         instance.install();
         
         instance.configure(config);
         instance.setParent(this);
         
         // TBD: how are children persisted? ?
         
         instance.initialize();
     }
     
     @Override
     public Response getOutput(Request request) {
         String output = "";
         
         if (request.isListCommandRequest()) {
             StringBuilder sb = new StringBuilder();
             
             if (this.hasSchema()) {
                 sb.append(Command.EDIT);
                 sb.append('\n');
                 this.writeProperties(sb, this.getSchema());
                 sb.append('\n');
             }
             
             sb.append(Command.CREATE);
             sb.append('\n');
             this.writeProperties(sb, this.childSchema);
             sb.append('\n');
             
             output = sb.toString();
         }
         
         return new Response(output);
     }
 }
