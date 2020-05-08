 package org.akquinet.audit.ui;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import org.akquinet.audit.ui.FormattedConsole.OutputLevel;
 
 public class UserCommunicator
 {
 	private static UserCommunicator _default = null;
 	private static final FormattedConsole _console = FormattedConsole.getDefault();
 	
 	private int _indentLevel;
 	private File _htmlReport;
 	private boolean _hide;
 	private ResourceBundle _labels;
 	
 	private HtmlReportLogger _htmlLogger;
 	
 	public UserCommunicator()
 	{
 		this(null);
 	}
 	
 	public UserCommunicator(File htmlReport)
 	{
 		_htmlReport = htmlReport;
 		_indentLevel = 0;
 		_htmlLogger = new HtmlReportLogger(Locale.getDefault());
 		_hide = false;
 		setLocale(Locale.getDefault());
 	}
 	
 	public void reportError(String error)
 	{
 		_console.println(getLevel(), error);
 	}
 	
 	public void reportError(Exception error)
 	{
 		_console.reportException(getLevel(), error);
 	}
 
 	public void printHeading1(String heading)
 	{
 		_hide = false;
 		_indentLevel = 0;
 		_console.printSeperatorLine();
 		_console.printSeperatorLine();
 		_console.println(getLevel(), heading + "\n\n");
 		
 		if(_htmlReport != null)
 		{
 			_htmlLogger.printHeading1(heading);
 		}
 		
 		_indentLevel = 1;
 	}
 	
 	public void printHeading2(String heading)
 	{
 		_hide = false;
 		_indentLevel = 0;
 		_console.printSeperatorLine();
 		_console.println(getLevel(), heading + "\n");
 
 		if(_htmlReport != null)
 		{
 			_htmlLogger.printHeading2(heading);
 		}
 		
 		_indentLevel = 1;
 	}
 	
 	public void printHeading3(String heading)
 	{
 		_hide = false;
 		_console.println(getLevel(), "---===" + heading + "===--- -- -  -");
 
 		if(_htmlReport != null)
 		{
 			_htmlLogger.printHeading3(heading);
 		}
 	}
 	
 	public void printParagraph(String text)
 	{
 		if(!_hide)
 		{
 			text = _console.wrapString(text, getLevel());
 			_console.println(getLevel(), text + "\n");
 		}
 
 		if(_htmlReport != null)
 		{
 			_htmlLogger.printParagraph(text);
 		}
 	}
 
 	public void println(String text)
 	{
 		if(!_hide)
 		{
 			_console.println(getLevel(), text);
 		}
 
 		if(_htmlReport != null)
 		{
 			_htmlLogger.println(text);
 		}
 	}
 	
 	public void printExample(String example)
 	{
 		if(!_hide)
 		{
 			if(getLevel() == FormattedConsole.OutputLevel.Q2)
 			{
 				_console.println(getLevel(), "\t" + example.replaceAll("\n", "\n\t\t"));
 			}
 			else
 			{
 				_console.println(getLevel(), "\t" + example.replaceAll("\n", "\n\t"));
 			}
 		}
 
 		if(_htmlReport != null)
 		{
 			_htmlLogger.printExample(example);
 		}
 	}
 	
 	private OutputLevel getLevel()
 	{
 		OutputLevel level;
 		switch(_indentLevel)
 		{
 		case 0:
 			level = OutputLevel.HEADING;
 			break;
 		case 1:
 			level = OutputLevel.Q1;
 			break;
 		case 2:
 			level = OutputLevel.Q2;
 			break;
 		default:
 			level = OutputLevel.RAW;
 			break;
 		}
 		return level;
 	}
 
 	public void printAnswer(boolean answer, String cause)
 	{
 		if(!_hide)
 		{
 			_console.printAnswer(getLevel(), answer, cause);
 		}
 
 		if(_htmlReport != null)
 		{
 			_htmlLogger.printAnswer(answer, cause);
 		}
 	}
 	
 	public void printHidingParagraph(String shortDescription, String expandedText)
 	{
 		if(_htmlReport != null)
 		{
 			_htmlLogger.beginHidingParagraph(shortDescription);
 			_htmlLogger.printParagraph(expandedText);
 			_htmlLogger.endHidingParagraph();
 		}
 	}
 	
 	public void beginHidingParagraph(String hiddenText)
 	{
 		_hide = true;
 		if(_htmlReport != null)
 		{
 			_htmlLogger.beginHidingParagraph(hiddenText);
 		}
 	}
 	
 	public void endHidingParagraph()
 	{
 		_hide = false;
 		if(_htmlReport != null)
 		{
 			_htmlLogger.endHidingParagraph();
 		}
 	}
 	
 	public void beginIndent()
 	{
 		_indentLevel++;
 		
 		if(_htmlReport != null)
 		{
 			_htmlLogger.beginIndent();
 		}
 	}
 	
 	public void endIndent()
 	{
 		_indentLevel--;
 		
 		if(_htmlReport != null)
 		{
 			_htmlLogger.endIndent();
 		}
 	}
 	
 	public void finishCommunication()
 	{
 		try
 		{
 			_htmlLogger.writeToFile(_htmlReport);
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	public boolean askYesNoQuestion(String question)
 	{
 		boolean answer = askYesNoQuestion(question, false);
 		
 		return answer;
 	}
 	
 	public boolean askYesNoQuestion(String question, Boolean defaultAnswer)
 	{
 		boolean answer = _console.askYesNoQuestion(getLevel(), question, defaultAnswer);
 		
 		_htmlLogger.printParagraph(question + " <i>" + (answer ? _labels.getString("S8_yes") : _labels.getString("S8_no")) + "</i>");
 		
 		return answer;
 	}
 	
 	public String askStringQuestion(String question)
 	{
 		String answer = askStringQuestion(question, null);
 		
 		_htmlLogger.printParagraph(question + " <i>" + answer + "</i>");
 		
 		return answer;
 	}
 
 	public String askStringQuestion(String question, String defaultAnswer)
 	{
 		String answer = _console.askStringQuestion(getLevel(), question, defaultAnswer);
 		
 		_htmlLogger.printParagraph(question + " <i>" + answer + "</i>");
 		
 		return answer;
 	}
 	
 	public void waitForUserToContinue()
 	{
 		_console.waitForUserToContinue();
 	}
 	
 	public static UserCommunicator getDefault()
 	{
 		if(_default == null)
 		{
 			_default = new UserCommunicator();
 		}
 		
 		return _default;
 	}
 	
 	public static void setDefault(UserCommunicator def) throws Exception
 	{
 		if(_default == null)
 		{
 			_default = def;
 		}
 		else
 		{
 			throw new Exception("Default value already set.");
 		}
 	}
 	
 	public void setIgnore_WaitForUserToContinue(boolean b)
 	{
 		_console.setIgnore_WaitForUserToContinue(b);
 	}
 
 	public void markReport()
 	{
 		_htmlLogger.mark();
 	}
 	
 	public void resetReport()
 	{
 		_htmlLogger.reset();
 	}
 	
 	public Locale getLocale()
 	{
 		return _labels.getLocale();
 	}
 	
 	public void setLocale(Locale locale)
 	{
 		_labels = ResourceBundle.getBundle("global", locale);
 		_console.setLocale(locale);
 		_htmlLogger.setLocale(locale);
 	}
 }
