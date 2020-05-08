 package org.akquinet.audit;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 
 public class ShellAnsweredQuestion
 {
 	private ProcessBuilder _command;
	private OutputStream _out;
	private InputStream _in;
 
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
			_out = p.getOutputStream();
			_in = p.getInputStream();
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
 	
	public OutputStream getOutputStream()
 	{
		return _out;
 	}
 	
	public InputStream getInputStream()
 	{
		return _in;
 	}
 }
