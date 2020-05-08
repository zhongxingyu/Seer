 package org.akquinet.audit.bsi.httpd.usersNrights;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.akquinet.audit.FormattedConsole;
 import org.akquinet.audit.ShellAnsweredQuestion;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.httpd.ConfigFile;
 
 public class Quest12b implements YesNoQuestion
 {
 	private static final String _id = "Quest12b";
 	private ConfigFile _conf;
 	private static final FormattedConsole _console = FormattedConsole.getDefault();
 	private static final FormattedConsole.OutputLevel _level = FormattedConsole.OutputLevel.Q2;
 	private static final String _command = "./quest12.sh";
 
 	public Quest12b(ConfigFile conf)
 	{
 		_conf = conf;
 	}
 
 	@Override
 	public boolean answer()
 	{
 		_console.println(FormattedConsole.OutputLevel.HEADING, "----" + _id + "----");
 
 		_console.println(_level, "Looking for directives User and Group in the apache configuration file...");
 		
 		String user = null;
 		String group = null;
		
		user = _conf.getDirective("User").get(0).getValue().trim();
		group = _conf.getDirective("Group").get(0).getValue().trim();
		
		if(user == null || group == null)
 		{
 			_console.printAnswer(_level, false, "User or Group directive not found. No dedicated user specified.");
 			return false;
 		}
 		
 		ShellAnsweredQuestion quest = new ShellAnsweredQuestion(_command, user, group);
 		boolean ret = quest.answer();
 		
 		StringBuffer buf = new StringBuffer();
 		InputStream stdOut = quest.getStdOut();
 		try
 		{
 			int b = stdOut.read();
 			while(b != -1)
 			{
 				buf.append((char) b);
 				b = stdOut.read();
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		_console.printAnswer(_level, ret, buf.toString());
 		
 		return ret;
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
