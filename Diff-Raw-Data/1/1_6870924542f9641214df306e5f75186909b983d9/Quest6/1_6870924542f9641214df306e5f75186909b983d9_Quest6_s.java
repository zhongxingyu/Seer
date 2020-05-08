 package org.akquinet.audit.bsi.httpd.software;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ResourceBundle;
 
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.audit.ui.UserCommunicator;
 
 public class Quest6 implements YesNoQuestion
 {
 	private static final String _id = "Quest6";
 	private ProcessBuilder _httpd;
 	private static final UserCommunicator _uc = UserCommunicator.getDefault();
 	private InputStream _stdErr;
 	private ResourceBundle _labels;
 	
 	public Quest6(File apacheExecutable)
 	{
 		_labels = ResourceBundle.getBundle(_id, _uc.getLocale());
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
 		_uc.printHeading3(_id);
 		_uc.printParagraph( _labels.getString("Q0") );
 		
 		try
 		{
 			Process p = _httpd.start();
 			_stdErr = p.getErrorStream();
 			int exit = p.waitFor();
 			
 			if(exit == 0)
 			{
 				_uc.printAnswer(true, _labels.getString("S1") );
 				printExtraInfo();
 				return true;
 			}
 			else
 			{
 				_uc.printAnswer(false, _labels.getString("S2") );
 				StringBuffer buf = new StringBuffer();
 				int b = _stdErr.read();
 				while(b != -1)
 				{
 					buf.append((char)b);
 					b = _stdErr.read();
 				}
 				_uc.printExample(buf.toString());
 				
 				printExtraInfo();
 				return false;
 			}
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 		catch (InterruptedException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	private void printExtraInfo()
 	{
 		_uc.beginHidingParagraph(_labels.getString("S3"));
 			_uc.printParagraph(_labels.getString("S4"));
 		_uc.endHidingParagraph();
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
 
 	@Override
 	public int getBlockNumber()
 	{
 		return 2;
 	}
 
 	@Override
 	public int getNumber()
 	{
 		return 6;
 	}
 
 	@Override
 	public String[] getRequirements()
 	{
 		return new String[0];
 	}
 }
