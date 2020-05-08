 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.entityreborn.chservercommunication.ch;
 
 import com.laytonsmith.annotations.api;
 import com.laytonsmith.core.CHVersion;
 import com.laytonsmith.core.constructs.CVoid;
 import com.laytonsmith.core.constructs.Construct;
 import com.laytonsmith.core.constructs.Target;
 import com.laytonsmith.core.environments.CommandHelperEnvironment;
 import com.laytonsmith.core.environments.Environment;
 import com.laytonsmith.core.exceptions.ConfigRuntimeException;
 import com.laytonsmith.core.functions.AbstractFunction;
 import com.laytonsmith.core.functions.Exceptions;
 import me.entityreborn.chservercommunication.Exceptions.InvalidChannelException;
 import me.entityreborn.chservercommunication.Exceptions.InvalidNameException;
 import me.entityreborn.chservercommunication.NodePoint;
 import me.entityreborn.chservercommunication.Publisher;
 import me.entityreborn.chservercommunication.Subscriber;
 import me.entityreborn.chservercommunication.Tracking;
 import org.zeromq.ZMQ;
 
 /**
  *
  * @author import
  */
 public class Functions {
     public abstract static class CommFunc extends AbstractFunction {
 
         public Exceptions.ExceptionType[] thrown() {
             return null;
         }
 
         public boolean isRestricted() {
             return true;
         }
 
         public Boolean runAsync() {
            return null;
         }
 
         public CHVersion since() {
             return CHVersion.V3_3_1;
         }
     }
     
     @api(environments = {CommandHelperEnvironment.class})
     public static class listen extends CommFunc {
 
         public Construct exec(Target t, Environment environment, 
                 Construct... args) throws ConfigRuntimeException {
             String name = args[0].val();
             String endpoint = args[1].val();
             int type = ZMQ.PUB;
             
             if (args.length == 3) {
                 String stype = args[2].val().toUpperCase();
 
                 if (!stype.equals("PUB") && !stype.equals("SUB")) {
                     throw new ConfigRuntimeException("You must specify PUB or SUB"
                             + " for comm_listen's third argument!", t);
                 }
 
                 if (stype.equals("SUB")) {
                     type = ZMQ.SUB;
                 }
             }
             
             NodePoint node;
             
             try {
                 node = Tracking.getOrCreate(type, name);
             } catch (InvalidNameException ex) {
                 throw new ConfigRuntimeException("Invalid name " + name + 
                         " given to comm_listen!", t);
             }
             
             node.listen(endpoint);
             
             return new CVoid(t);
         }
 
         public String getName() {
             return "comm_listen";
         }
 
         public Integer[] numArgs() {
             return new Integer[]{2, 3};
         }
 
         public String docs() {
             return "void {name, endpoint[, type]} Listen. Automatically creates the"
                     + " the socket if it doesn't exist already."
                     + " Type can be PUB or SUB, but defaults to PUB.";
         }
     }
     
     @api(environments = {CommandHelperEnvironment.class})
     public static class connect extends CommFunc {
         public Construct exec(Target t, Environment environment, 
                 Construct... args) throws ConfigRuntimeException {
             String name = args[0].val();
             String endpoint = args[1].val();
             int type = ZMQ.SUB;
             
             if (args.length == 3) {
                 String stype = args[2].val().toUpperCase();
 
                 if (!stype.equals("PUB") && !stype.equals("SUB")) {
                     throw new ConfigRuntimeException("You must specify PUB or SUB"
                             + " for comm_disconnect's third argument!", t);
                 }
 
                 if (stype.equals("PUB")) {
                     type = ZMQ.PUB;
                 }
             }
             
             NodePoint node;
             
             try {
                 node = Tracking.getOrCreate(type, name);
             } catch (InvalidNameException ex) {
                 throw new ConfigRuntimeException("Invalid name " + name + 
                         " given to comm_connect!", t);
             }
             
             node.connect(endpoint);
             
             return new CVoid(t);
         }
 
         public String getName() {
             return "comm_connect";
         }
 
         public Integer[] numArgs() {
             return new Integer[]{2, 3};
         }
 
         public String docs() {
             return "void {name, endpoint[, type]} Connect. Automatically creates the"
                     + " the socket if it doesn't exist already. Type can be PUB "
                     + "or SUB, but defaults to SUB.";
         }
     }
     
     @api(environments = {CommandHelperEnvironment.class})
     public static class disconnect extends CommFunc {
         public Construct exec(Target t, Environment environment, 
                 Construct... args) throws ConfigRuntimeException {
             String name = args[0].val();
             String endpoint = args[1].val();
             int type = ZMQ.SUB;
             
             if (args.length == 3) {
                 String stype = args[2].val().toUpperCase();
 
                 if (!stype.equals("PUB") && !stype.equals("SUB")) {
                     throw new ConfigRuntimeException("You must specify PUB or SUB"
                             + " for comm_disconnect's third argument!", t);
                 }
 
                 if (stype.equals("PUB")) {
                     type = ZMQ.PUB;
                 }
             }
             
             NodePoint node;
             
             try {
                 if (type == ZMQ.PUB) {
                     node = Tracking.getPub(name);
                 } else {
                     node = Tracking.getSub(name);
                 }
             } catch (InvalidNameException ex) {
                 throw new ConfigRuntimeException("Invalid name " + name + 
                         " given to comm_disconnect!", t);
             }
             
             if (node == null) {
                 throw new ConfigRuntimeException("Unknown " + name + " "
                         + " given to comm_disconnect!", t);
             }
             
             node.disconnect(endpoint);
             
             return new CVoid(t);
         }
 
         public String getName() {
             return "comm_disconnect";
         }
 
         public Integer[] numArgs() {
             return new Integer[]{2, 3};
         }
 
         public String docs() {
             return "void {name, endpoint[, type]} Disconnect. Type can be PUB or"
                     + " SUB, but defaults to SUB.";
         }
     }
     
     @api(environments = {CommandHelperEnvironment.class})
     public static class close extends CommFunc {
         public Construct exec(Target t, Environment environment, 
                 Construct... args) throws ConfigRuntimeException {
             String name = args[0].val();
             int type = ZMQ.SUB;
             
             if (args.length == 2) {
                 String stype = args[1].val().toUpperCase();
 
                 if (!stype.equals("PUB") && !stype.equals("SUB")) {
                     throw new ConfigRuntimeException("You must specify PUB or SUB"
                             + " for comm_close's second argument!", t);
                 }
 
                 if (stype.equals("PUB")) {
                     type = ZMQ.PUB;
                 }
             }
             
             boolean found;
             
             try {
                 found = Tracking.close(name, type);
             } catch (InvalidNameException ex) {
                 throw new ConfigRuntimeException("Invalid name " + name + 
                         " given to comm_close!", t);
             }
             
             if (!found) {
                 throw new ConfigRuntimeException("Unknown " + name + " "
                         + " given to comm_close!", t);
             }
             
             return new CVoid(t);
         }
 
         public String getName() {
             return "comm_close";
         }
 
         public Integer[] numArgs() {
             return new Integer[]{2};
         }
 
         public String docs() {
             return "void {name, type} Close. Type can be PUB or SUB."
                     + " This will disconnect any and all connections and binds"
                     + " related to this name for this type.";
         }
     }
     
     @api(environments = {CommandHelperEnvironment.class})
     public static class subscribe extends CommFunc {
         public Construct exec(Target t, Environment environment, 
                 Construct... args) throws ConfigRuntimeException {
             String name = args[0].val();
             String channel = args[1].val();
 
             NodePoint node;
             
             try {
                 node = Tracking.getSub(name);
             } catch (InvalidNameException ex) {
                 throw new ConfigRuntimeException("Invalid name " + name + 
                         " given to comm_subscribe!", t);
             }
             
             if (node == null) {
                 throw new ConfigRuntimeException("Unknown SUB " + name + 
                         " given to comm_subscribe!", t);
             }
             try {
                 ((Subscriber)node).subscribe(channel);
             } catch (InvalidChannelException ex) {
                 throw new ConfigRuntimeException("Invalid channel " + channel + 
                         " given to comm_subscribe!", t);
             }
             
             return new CVoid(t);
         }
 
         public String getName() {
             return "comm_subscribe";
         }
 
         public Integer[] numArgs() {
             return new Integer[]{2};
         }
 
         public String docs() {
             return "void {name, channel} Subscribe SUB <name> to <channel>.";
         }
     }
     
     @api(environments = {CommandHelperEnvironment.class})
     public static class unsubscribe extends CommFunc {
         public Construct exec(Target t, Environment environment, 
                 Construct... args) throws ConfigRuntimeException {
             String name = args[0].val();
             String channel = args[1].val();
 
             NodePoint node;
             
             try {
                 node = Tracking.getSub(name);
             } catch (me.entityreborn.chservercommunication.Exceptions.InvalidNameException ex) {
                 throw new ConfigRuntimeException("Invalid name " + name + 
                         " given to comm_unsubscribe!", t);
             }
             
             if (node == null) {
                 throw new ConfigRuntimeException("Unknown SUB " + name + 
                         " given to comm_unsubscribe!", t);
             }
             try {
                 ((Subscriber)node).unsubscribe(channel);
             } catch (InvalidChannelException ex) {
                 throw new ConfigRuntimeException("Invalid channel " + channel + 
                         " given to comm_subscribe!", t);
             }
             
             return new CVoid(t);
         }
 
         public String getName() {
             return "comm_unsubscribe";
         }
 
         public Integer[] numArgs() {
             return new Integer[]{2};
         }
 
         public String docs() {
             return "void {name, channel} Unsubscribe SUB <name> from <channel>.";
         }
     }
     
     @api(environments = {CommandHelperEnvironment.class})
     public static class publish extends CommFunc {
         public Construct exec(Target t, Environment environment, 
                 Construct... args) throws ConfigRuntimeException {
             String name = args[0].val();
             String channel = args[1].val();
             String message = args[2].val();
             String origpub = null;
             
             if (args.length == 4) {
                 origpub = args[3].val();
             }
 
             NodePoint node;
             
             try {
                 node = Tracking.getPub(name);
             
                 if (node == null) {
                     throw new ConfigRuntimeException("Unknown PUB " + name + 
                             " given to comm_publish!", t);
                 }
                 
                 ((Publisher)node).publish(channel, message, origpub);
             } catch (InvalidChannelException ex) {
                 throw new ConfigRuntimeException("Invalid channel " + channel + 
                         " given to comm_publish!", t);
             } catch (InvalidNameException ex) {
                 throw new ConfigRuntimeException("Invalid name " + name + 
                         " given to comm_publish!", t);
             }
             
             return new CVoid(t);
         }
 
         public String getName() {
             return "comm_publish";
         }
 
         public Integer[] numArgs() {
             return new Integer[]{3, 4};
         }
 
         public String docs() {
             return "void {name, channel, message[, originalid]} Publish <message>"
                     + " to <channel> of PUB with name <name>. if originalid is"
                     + " given, that publisher's name will be used instead of"
                     + " this one.";
         }
     }
 }
