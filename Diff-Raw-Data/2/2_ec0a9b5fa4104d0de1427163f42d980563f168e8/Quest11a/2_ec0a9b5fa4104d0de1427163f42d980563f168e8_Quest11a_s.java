 package org.akquinet.audit.bsi.httpd;
 
 import org.akquinet.audit.FormattedConsole;
 import org.akquinet.audit.YesNoQuestion;
 
 public class Quest11a implements YesNoQuestion
 {
 	private static final String _id = "Quest11a";
 	private static final FormattedConsole _console = FormattedConsole.getDefault();
 	private static final FormattedConsole.OutputLevel _level = FormattedConsole.OutputLevel.Q2;
 
 	public Quest11a()
 	{
 	}
 
 	/**
 	 * checks whether there are any Include-directives in the config file
 	 */
 	@Override
 	public boolean answer()
 	{
 		_console.println(FormattedConsole.OutputLevel.HEADING, "----" + _id + "----");
 		boolean ret = _console.askYesNoQuestion(_level, "Have you properly set up a chroot environment for the apache httpd server which will block access outside of the servers root directory?");
 		if(ret)
 		{
 			ret = _console.askYesNoQuestion(_level, "Have recently checked it's configuration?");
 			if(!ret)
 			{
 				_console.println(_level, "Please check that configuration. For now I will assume the chroot doesn't work properly.");
 			}
 		}
 		_console.printAnswer(_level, ret, ret ? 
 						"Ok this should block access to files outside of the servers root directory."
 						: "No chroot - it may be possible to access files outside of the servers root directory if not sealed otherwise.");
		return false;
 	}
 
 	@Override
 	public boolean isCritical()
 	{
 		return false;
 	}
 
 	@Override
 	public String getID()
 	{
 		return _id;
 	}
 }
