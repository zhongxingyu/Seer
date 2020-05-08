 package org.akquinet.audit.bsi.httpd.software;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.audit.ui.UserCommunicator;
 import org.akquinet.httpd.ConfigFile;
 
 public class Quest5 implements YesNoQuestion
 {
 	private static final String _id = "Quest5";
 	private ConfigFile _conf;
 	private static final UserCommunicator _uc = UserCommunicator.getDefault();
 	
 	private Quest5a _q5a;
 	private Quest5b _q5b;
 	private ResourceBundle _labels;
 	
 	public Quest5(ConfigFile conf)
 	{
 		_labels = ResourceBundle.getBundle(_id, _uc.getLocale());
 		_conf = conf;
 		_q5a = new Quest5a(_conf);
 		_q5b = new Quest5b(_conf);
 	}
 
 	/**
 	 * Looking for AllowOverride-directives. Checking whether there is no with parameters other than None and at least one
 	 * in global context with parameter None.
 	 */
 	@Override
 	public boolean answer()
 	{
 		_uc.printHeading3(_id);
 		_uc.printHidingParagraph(_labels.getString("S0"), _labels.getString("P0"));
 		_uc.printParagraph( _labels.getString("Q0") );
 		
 		try
 		{
 			_conf.reparse();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		_uc.println( _labels.getString("L1") );
 		_uc.beginIndent();
 		boolean answer5a = _q5a.answer();
 		boolean answer5b = _q5b.answer();
 		boolean ret = answer5a && answer5b;
 		
 		_uc.endIndent();
 		_uc.println(MessageFormat.format( _labels.getString("L2") , _id));
 		_uc.printAnswer(ret, ret ?
 				  _labels.getString("S1_good") 
 				: _labels.getString("S1_bad") );
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
 
 	@Override
 	public int getBlockNumber()
 	{
 		return 2;
 	}
 
 	@Override
 	public int getNumber()
 	{
		return 25;
 	}
 
 	@Override
 	public String[] getRequirements()
 	{
 		return new String[0];
 	}
 }
