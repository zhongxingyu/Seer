 package com.conga.tools.mokol.plugin.priam;
 
 import java.util.Map;
 import java.util.TreeMap;

 import com.conga.tools.mokol.CommandContext;
 import com.conga.tools.mokol.Shell;
 import com.conga.tools.mokol.ShellException;
 import com.conga.tools.mokol.metadata.CommandDescriptor;
import com.conga.tools.mokol.plugin.base.BasePlugin;
 import com.conga.tools.mokol.spi.annotation.Command;
import com.conga.tools.mokol.spi.annotation.Plugin;
 
 /**
  * 
  * @author Todd Fast
  */

public class PriamPlugin extends BasePlugin {
 
 	/**
 	 *
 	 *
 	 */
 	@Override
 	public String getName() {
		return getClass().getName().substring(0,
				getClass().getName().lastIndexOf("."));
 	}
 
 	/**
 	 *
 	 *
 	 */
 	@Override
 	public String getVersion() {
		return "0.1";
	}

	/**
	 * @return
	 * 
	 * 
	 */
	@Override
	public Map<String, CommandDescriptor> initialize(Shell shell)
			throws ShellException {
		return new TreeMap<String, CommandDescriptor>();
 	}
 
 	/**
 	 *
 	 *
 	 */
 	@Command(alias = "createDomains")
 	public CreateDomainsCommand createDomains(CommandContext context) {
 		return new CreateDomainsCommand();
 	}
 
 	/**
 	 *
 	 *
 	 */
 	@Command(alias = "dropDomains")
 	public DropDomainsCommand dropDomains(CommandContext context) {
 		return new DropDomainsCommand();
 	}
 
 	/**
 	 *
 	 *
 	 */
 	@Command(alias = "clearDomains")
 	public ClearDomainsCommand clearDomains(CommandContext context) {
 		return new ClearDomainsCommand();
 	}
 
 	/**
 	 *
 	 *
 	 */
 	@Command(alias = "createApp")
 	public CreateAppCommand createApp(CommandContext context) {
 		return new CreateAppCommand();
 	}
 
 }
