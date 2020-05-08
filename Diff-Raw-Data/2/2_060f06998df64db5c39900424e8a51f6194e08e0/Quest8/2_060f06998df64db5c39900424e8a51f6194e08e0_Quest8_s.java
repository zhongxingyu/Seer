 package org.akquinet.audit.bsi.httpd.usersNrights;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.akquinet.audit.Automated;
 import org.akquinet.audit.ShellAnsweredQuestion;
 import org.akquinet.audit.YesNoQuestion;
 import org.akquinet.audit.ui.UserCommunicator;
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.ParserException;
 import org.akquinet.httpd.syntax.Directive;
 import org.akquinet.httpd.syntax.StatementList;
 
 @Automated
 public class Quest8 implements YesNoQuestion
 {
 	private static final long serialVersionUID = -2441169990468550625L;
 	
 	private static final String PERMISSION_PATTERN = "-...--.--.";
 	private static final String PERMISSION_PATTERN_LOW = "-....-..-.";
 	
 	private static final String _id = "Quest8";
 	private transient UserCommunicator _uc = UserCommunicator.getDefault();
 	private String _command;
 	private String _commandPath;
 	private File _configFile;
 	private transient ResourceBundle _labels;
 	private ConfigFile _conf;
 	private boolean _highSec;
 
 	public Quest8(File configFile, ConfigFile conf, boolean highSec)
 	{
 		this(configFile, conf, "./", "QfileSafe.sh", highSec);
 	}
 	
 	public Quest8(File configFile, ConfigFile conf, boolean highSec, UserCommunicator uc)
 	{
		this(configFile, conf, "./", "QfileSafe.sh", highSec, UserCommunicator.getDefault());
 	}
 
 	public Quest8(File configFile, ConfigFile conf, String commandPath, String command, boolean highSec)
 	{
 		this(configFile, conf, commandPath, command, highSec, UserCommunicator.getDefault());
 	}
 	
 	public Quest8(File configFile, ConfigFile conf, String commandPath, String command, boolean highSec, UserCommunicator uc)
 	{
 		_uc = uc;
 		_configFile = configFile;
 		_commandPath = commandPath;
 		_command = command;
 		_conf = conf;
 		_labels = ResourceBundle.getBundle(_id, _uc.getLocale());
 		_highSec = highSec;
 	}
 
 	@Override
 	public boolean answer()
 	{
 		_uc.printHeading3( _labels.getString("name") );
 		_uc.printParagraph( _labels.getString("Q0") );
 		
 		
 		try
 		{
 			Set<File> problems = lookForProblems(PERMISSION_PATTERN);
 			boolean ret = problems.isEmpty();
 			
 			if(!_highSec && !ret)
 			{
 				Set<File> problems_low = lookForProblems(PERMISSION_PATTERN_LOW);
 				ret = problems_low.isEmpty();
 				_uc.printAnswer(false, ret ? _labels.getString("S5") : _labels.getString("S4") );
 			}
 			else
 			{
 				_uc.printAnswer(ret, ret ? _labels.getString("S1") : _labels.getString("S2") );
 			}
 			
 			if(!ret)
 			{
 				StringBuffer buf = new StringBuffer();
 				for (File file : problems)
 				{
 					buf = buf.append(file.getCanonicalPath()).append('\n');
 				}
 				
 				_uc.printExample(buf.toString());
 			}
 			
 			_uc.beginHidingParagraph( _labels.getString("S3") );
 				_uc.printParagraph( _labels.getString("P1") );
 				_uc.printExample("-rw------- 1 root root 23650 2011-04-29 15:38 httpd.conf");
 			_uc.endHidingParagraph();
 			
 			return ret;
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	private ShellAnsweredQuestion checkFile(File file, String permissionPattern) throws IOException
 	{
 		return new ShellAnsweredQuestion(_commandPath + _command, file.getCanonicalPath(), permissionPattern);
 	}
 
 	private Set<File> lookForProblems(String permissionPattern) throws IOException
 	{
 		Set<File> problems = new HashSet<File>();
 		
 		try
 		{
 			for(Directive incDir : _conf.getAllDirectivesIgnoreCase("Include"))
 			{
 				List<File> files = StatementList.filesToInclude(_conf.getHeadExpression().getServerRoot(), incDir.getValue().trim());
 				for(File file : files)
 				{
 					ShellAnsweredQuestion quest = checkFile(file, permissionPattern);
 					if(!quest.answer())
 					{
 						problems.add(file);
 					}
 				}
 			}
 		}
 		catch (ParserException e)
 		{
 			throw new RuntimeException(e);
 		}
 		
 		ShellAnsweredQuestion quest = checkFile(_configFile, permissionPattern);
 		if(!quest.answer())
 		{
 			problems.add(_configFile);
 		}
 		
 		return problems;
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
 		return 8;
 	}
 
 	@Override
 	public String[] getRequirements()
 	{
 		return new String[0];
 	}
 
 	@Override
 	public void initialize() throws Exception
 	{
 		_conf.reparse();
 	}
 	
 	private synchronized void readObject( java.io.ObjectInputStream s ) throws IOException, ClassNotFoundException
 	{
 		s.defaultReadObject();
 		_uc = UserCommunicator.getDefault();
 		_labels = ResourceBundle.getBundle(_id, _uc.getLocale());
 	}
 	
 	@Override
 	public String getName()
 	{
 		return _labels.getString("name");
 	}
 
 	@Override
 	public void setUserCommunicator(UserCommunicator uc)
 	{
 		_uc = uc;
 	}
 }
