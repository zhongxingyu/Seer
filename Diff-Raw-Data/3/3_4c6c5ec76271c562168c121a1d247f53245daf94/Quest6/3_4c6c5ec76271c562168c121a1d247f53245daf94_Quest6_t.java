 package org.akquinet.audit.bsi.httpd;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.akquinet.audit.FormattedConsole;
 import org.akquinet.audit.YesNoQuestion;
 
 public class Quest6 implements YesNoQuestion
 {
 	private static final String _id = "Quest6";
 	private ProcessBuilder _httpd;
 	private static final FormattedConsole _console = FormattedConsole.getDefault();
 	private static final FormattedConsole.OutputLevel _level = FormattedConsole.OutputLevel.Q1;
 	private InputStream _stdErr;
 	
 	public Quest6(File apacheExecutable)
 	{
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
 	 * Looking for AllowOverride-directives. Checking whether there is no with parameters other than None and at least one
 	 * in global context with parameter None.
 	 */
 	@Override
 	public boolean answer()
 	{
 		//TODO: test!
 		try
 		{
 			Process p = _httpd.start();
 			_stdErr = p.getErrorStream();
 			int exit = p.waitFor();
 			
 			if(exit == 0)
 			{
 				_console.printAnswer(_level, true, "Syntax of main configuration file OK.");
 				return true;
 			}
 			else
 			{
 				_console.printAnswer(_level, false, "Syntax errors in main configuration file:");
 				StringBuffer buf = new StringBuffer();
 				int b = _stdErr.read();
 				while(b != -1)
 				{
 					buf.append(b);
 					b = _stdErr.read();
 				}
 				_console.println(_level, buf.toString());
 				
 				return false;
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
			_console.printAnswer(_level, false, "Problem while answering question. Caught an IOException (see stderr).");
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
			_console.printAnswer(_level, false, "Problem while answering question. Caught an InterruptedException (see stderr).");
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isCritical()
 	{
 		return true;
 	}
 
 	@Override
 	public String getID()
 	{
 		return _id;
 	}
 }
