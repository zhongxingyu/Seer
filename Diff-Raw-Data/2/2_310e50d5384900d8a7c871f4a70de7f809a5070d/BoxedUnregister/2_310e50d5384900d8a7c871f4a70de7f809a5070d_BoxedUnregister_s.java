 package org.vfsutils.shell.commands;
 
 import org.vfsutils.shell.CommandProvider;
 import org.vfsutils.shell.Engine;
 
 /**
  * Allows unregistering only vfs script commands, not the java class or bsh script
  * commands
  * @author kleij - at - users.sourceforge.net
  *
  */
 public class BoxedUnregister extends Unregister {
 
 	/**
 	 * Only unregister vfs script commands
 	 */
 	protected void unregister(String cmd, CommandProvider command, Engine engine) {
 		if (command instanceof Register.Script) {
			if ("vfs".equals(((Register.Script)command).type)) {
 				super.unregister(cmd, command, engine);
 			}
 			else {
 				engine.error("Can not unregister " + cmd);
 			}
 		}
 		else {
 			engine.error("Can not unregister " + cmd);
 		}
 		
 	}
 
 	
 
 }
