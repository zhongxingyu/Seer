 package org.akquinet.audit.bsi.httpd;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.akquinet.audit.FormattedConsole;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.syntax.Directive;
 
 public class Quest3 implements YesNoQuestion
 {
 	private static final String _id = "Quest3";
 	private ConfigFile _conf;
 	private ProcessBuilder _httpd;
 	private static final FormattedConsole _console = FormattedConsole.getDefault();
 	private static final FormattedConsole.OutputLevel _level = FormattedConsole.OutputLevel.Q1;
 	
 	public Quest3(ConfigFile conf, File apacheExecutable)
 	{
 		_conf = conf;
 		try
 		{
			_httpd = new ProcessBuilder(apacheExecutable.getCanonicalPath(), "-t");
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * checks whether there are any Include-directives in the config file
 	 */
 	@Override
 	public boolean answer()
 	{
 		//TODO: test
 		List<Directive> loadList = _conf.getDirectiveIgnoreCase("LoadModule");
 		for (Directive directive : loadList)
 		{
 			String[] arguments = directive.getValue().trim().split("( |\t)*");
 			if(arguments == null || arguments.length < 2)
 			{
 				continue;
 			}
 			
 			if(arguments[0].equals("security2_module"))
 			{
 				Directive modSec = directive;
 				_console.printAnswer(_level, true, "ModSecurity is being loaded:");
 				_console.println(_level, modSec.getLinenumber() + ": " + modSec.getName() + " " + modSec.getValue());
 				return true;
 			}
 			
 		}
 		
 		//maybe ModSecurity is compiled into the apache binary, check for that:
 		try
 		{
 			Process p = _httpd.start();
 			InputStream stdErr = p.getErrorStream();
 			p.waitFor();
 			
 			StringBuffer buf = new StringBuffer();
 			int b = stdErr.read();
 			while(b != -1)
 			{
 				buf.append(b);
 				b = stdErr.read();
 			}
 			String output = buf.toString();
 			String[] modList = output.split("(\n|\r\n)");
 			for (String str : modList)
 			{
 				if(str.matches("( |\t)*mod_security.c"))
 				{
 					_console.printAnswer(_level, true, "ModSecurity is compiled into the httpd binary.");
 					return true;
 				}
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		
 		_console.printAnswer(_level, false, "ModSecurity seems not to be loaded.");
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
