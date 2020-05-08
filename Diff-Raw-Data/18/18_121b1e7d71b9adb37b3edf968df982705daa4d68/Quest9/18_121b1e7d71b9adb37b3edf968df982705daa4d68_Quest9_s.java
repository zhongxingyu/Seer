 package org.akquinet.audit.bsi.httpd.usersNrights;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.audit.ui.UserCommunicator;
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.syntax.Directive;
 
 public class Quest9 implements YesNoQuestion
 {
	static final String SERVER_ROOT_NOT_SET = "ServerRoot not or ambiguously set!";
 	private static final String _id = "Quest9";
 	private static final UserCommunicator _uc = UserCommunicator.getDefault();
 	private ConfigFile _conf;
 	private String _serverRoot;
 	private String _commandPath;
 	private String _command9a;
 	private String _getUserNGroupCommand;
 	private String _apacheExecutable;
 	private Quest9a _q9a;
 	private Quest9b _q9b;
 	private ResourceBundle _labels;
 	private boolean _highSec;
 
 	public Quest9(ConfigFile conf, String apacheExecutable, boolean highSec)
 	{
 		this(conf, "./", "quest9.sh", "getApacheStartingUser.sh", apacheExecutable, highSec);
 	}
 	
 	public Quest9(ConfigFile conf, String command9aPath, String command9a, String getUserNGroupCommand, String apacheExecutable, boolean highSec)
 	{
 		_conf = conf;
 		_serverRoot = getServerRoot();
 		_commandPath = command9aPath;
 		_command9a = command9a;
 		_getUserNGroupCommand = getUserNGroupCommand;
 		_apacheExecutable = apacheExecutable;
 		_labels = ResourceBundle.getBundle(_id, _uc.getLocale());
 		_highSec = highSec;
 	}
 
 	@Override
 	public boolean answer()
 	{
 		_uc.printHeading3(_id);
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
 		
 		if(_serverRoot == null || _serverRoot == "")
 		{
 			_uc.printAnswer(false, _labels.getString("E1") );
 			return false;
 		}
 
 		_uc.printExample(_serverRoot);
 		
 		boolean ret = false;
 		if(_serverRoot != null)
 		{
 			String user = getApacheStartingUser();
 			_uc.println( MessageFormat.format( _labels.getString("L2") , user));
 			
 			if(_command9a != null && _commandPath  != null)
 			{
 				_q9a = new Quest9a(_serverRoot, user, _commandPath, _command9a, _highSec);
 			}
 			else
 			{
 				_q9a = new Quest9a(_serverRoot, user, _highSec);
 			}
 			_q9b = new Quest9b(_serverRoot);
 			
 			_uc.beginIndent();
 				ret = _q9a.answer();
 				ret &= _q9b.answer();
 			_uc.endIndent();
 			_uc.println( MessageFormat.format(_labels.getString("S1"), _id));
 			_uc.printAnswer(ret, ret ?  _labels.getString("S2")  :  _labels.getString("S3") );
 			
 		}
 		else
 		{
 			_uc.println( MessageFormat.format(_labels.getString("S1"), _id));
 			_uc.printAnswer(false,  _labels.getString("S4") );
 			ret = false;
 		}
 		
 		_uc.beginHidingParagraph( _labels.getString("S5") );
 			_uc.println( _labels.getString("S6") );
 			_uc.printExample("ServerRoot /path/to/some/directory/");
 			_uc.printParagraph( _labels.getString("S7") );
 		_uc.endHidingParagraph();
 		
 		return ret;
 	}
 
 	private String getApacheStartingUser()
 	{
 		try
 		{
 			Process p = (new ProcessBuilder(_commandPath + _getUserNGroupCommand, _apacheExecutable)).start();
 			InputStream is = p.getInputStream();
 			StringBuffer buf = new StringBuffer();
 			int b = is.read();
 			while(b != -1)
 			{
 				buf.append((char)b);
 				b = is.read();
 			}
 			return buf.toString().split("[ \t]+")[0];
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String getServerRoot()
 	{
 		List<Directive> srvRootList = _conf.getDirective("ServerRoot");
 		if(srvRootList.size() != 1)
 		{
 			return null;
 		}
 		
 		String ret = srvRootList.get(0).getValue().trim();
 		if(ret.startsWith("\"") && ret.endsWith("\"") && ret.length() > 1)
 		{
 			ret = ret.substring(1);
 			ret = ret.substring(0, ret.length()-1);
 		}
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
 		return 3;
 	}
 
 	@Override
 	public int getNumber()
 	{
 		return 9;
 	}
 
 	@Override
 	public String[] getRequirements()
 	{
 		return new String[0];
 	}
 
 }
