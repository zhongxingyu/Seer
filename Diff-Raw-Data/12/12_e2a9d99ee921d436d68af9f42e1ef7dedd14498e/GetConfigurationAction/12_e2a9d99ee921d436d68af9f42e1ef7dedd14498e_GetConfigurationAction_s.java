 package net.i2cat.mantychore.actionsets.junos.actions;
 
 import java.util.List;
 
 import net.i2cat.mantychore.commandsets.junos.JunosCommandFactory;
 import net.i2cat.mantychore.commons.Action;
 import net.i2cat.mantychore.commons.Command;
 import net.i2cat.mantychore.commons.CommandException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class GetConfigurationAction extends Action {
 	public static final String	GETCONFIG		= "getConfiguration";
 	Logger						logger			= LoggerFactory.getLogger(GetConfigurationAction.class);
	private List<String>		commandsList	= null;
 	int							index			= 0;
 
 	public GetConfigurationAction() {
 		// super(GETCONFIG);
 		initialize();
 	}
 
 	protected void initialize() {
		commands.add(getCommand(GETCONFIG));
 	}
 
 	protected Command getCommand(String commandID) {
 		try {
 			JunosCommandFactory commandFactory = new JunosCommandFactory();
 			return commandFactory.createCommand(commandID);
 		} catch (CommandException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
