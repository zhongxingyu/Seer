 package org.akquinet.audit;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 
 public class ShellAnsweredQuestion
 {
 	private ProcessBuilder _command;
	private OutputStream _stdIn;
	private InputStream _stdOut;
	private InputStream _stdErr;
 
 	public ShellAnsweredQuestion(String command)
 	{
 		_command = new ProcessBuilder(command);
 	}
 
 	public boolean answer()
 	{
 		int retVal = 1;
 		try
 		{
 			Process p = _command.start();
			_stdIn = p.getOutputStream();
			_stdOut = p.getInputStream();
			_stdErr = p.getErrorStream();
 			p.waitFor();
 			retVal = p.exitValue();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		return retVal == 0;
 	}
 	
	public OutputStream getStdIn()
 	{
		return _stdIn;
 	}
 	
	public InputStream getStdOut()
 	{
		return _stdOut;
	}
	
	public InputStream getStdErr()
	{
		return _stdErr;
 	}
 }
