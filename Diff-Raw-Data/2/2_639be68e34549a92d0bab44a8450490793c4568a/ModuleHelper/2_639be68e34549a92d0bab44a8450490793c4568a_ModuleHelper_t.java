 package org.akquinet.audit;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InvalidClassException;
 import java.io.Serializable;
 import java.util.List;
 
 import org.akquinet.httpd.ConfigFile;
 import org.akquinet.httpd.ParserException;
 import org.akquinet.httpd.syntax.Directive;
 
 /**
  * This class will help you to find apache modules that are loaded dynamically and modules that have statically 
  * been compiled into the apache.
  * 
  * This class is intended to be subclassed.
  *  
  * @author immanuel
  *
  */
 public class ModuleHelper implements Serializable
 {
 	private static final long serialVersionUID = -6541226395865237818L;
 	private ConfigFile _conf;
 	private ProcessBuilder _httpd;
 	
 	/**
 	 * Using this constructor you won't get access to modules compiled into the apache. getCompiledIntoModulesList() will always return an empty array.
 	 * @param conf the configuration
 	 * @see getCompiledIntoModulesList()
 	 */
 	public ModuleHelper(ConfigFile conf)
 	{
 		_conf = conf;
 		_httpd = null;
 	}
 
 	/**
 	 * Constructor for this class to build an object that can use all capabilities of this class (@see getCompiledIntoModulesList()).
 	 * @param conf the configuration
 	 * @param apacheExecutable the apache executable (often something like /usr/sbin/httpd or /usr/sbin/apache2)
 	 */
 	public ModuleHelper(ConfigFile conf, File apacheExecutable)
 	{
 		_conf = conf;
 		
 		try
 		{
 			_httpd = new ProcessBuilder(apacheExecutable.getCanonicalPath(), "-l");
 		}
 		catch (IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * This method looks for all LoadModule-directives in the configuration and returns a list of them.
 	 * @return List of all LoadModule-directives in the configuration.
 	 */
 	public List<Directive> getLoadModuleList()
 	{
 		return _conf.getDirectiveIgnoreCase("LoadModule");
 	}
 	
 	/**
 	 * This method looks for all LoadFile-directives in the configuration and returns a list of them.
 	 * @return List of all LoadFile-directives in the configuration.
 	 */
 	public List<Directive> getLoadFileList()
 	{
 		return _conf.getDirectiveIgnoreCase("LoadFile");
 	}
 
 	/**
 	 * This method will use the apache executable to determine which modules have been compiled into it.
 	 * @return An array with the names of all modules compiled into your apache executable.
 	 */
 	public String[] getCompiledIntoModulesList()
 	{
 		if(_httpd == null)
 		{
 			return new String[0];
 		}
 		
 		try
 		{
 			Process p = _httpd.start();
 			InputStream stdOut = p.getInputStream();
 			
 			StringBuffer buf = new StringBuffer();
 			int b;
 			Thread.sleep(100);
 			while(stdOut.available() >= 1)
 			{
 				b = stdOut.read();
 				buf.append((char)b);
 			}
 			p.waitFor();
 			
 			String output = buf.toString();
 			String[] modList = output.split("(\r\n|\n)");
 			return modList;
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
 	
 	/**
 	 * Diggs through the configuration looking for a LoadModule directive for the specified module name
 	 * LoadModule moduleName path/to/some/dso
 	 * @param moduleName
 	 * @return The LoadModule directive if existent, null otherwise
 	 */
 	public Directive getLoadModuleDirective(String moduleName)
 	{
 		List<Directive> dirs = getLoadModuleList();
 		
 		for (Directive directive : dirs)
 		{
 			String[] arguments = directive.getValue().trim().split("[ \t]+");
 			if(arguments == null || arguments.length < 2)
 			{
 				continue;
 			}
 			
			if(arguments[0].equals(moduleName))
 			{
 				return directive;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param moduleName
 	 * @return true iff a module with name moduleName is beeing loaded by a LoadModule directive
 	 */
 	public boolean isModuleLoaded(String moduleName)
 	{
 		return getLoadModuleDirective(moduleName) != null;
 	}
 	
 	/**
 	 * 
 	 * @param compiledIntoModuleName Note this name is most times different from the name used by a LoadModule directive
 	 * @return true iff a module with name compiledIntoModuleName has been compiled into the apache binary.
 	 */
 	public boolean isModuleCompiledInto(String compiledIntoModuleName)
 	{
 		String[] modList = getCompiledIntoModulesList();
 		
 		for (String str : modList)
 		{
 			if(str.matches("( |\t)*" + compiledIntoModuleName + "( |\t)*"))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Reparse the config-file. Useful if the config-file has been changed.
 	 * @throws ParserException
 	 * @throws IOException
 	 */
 	public void reparse() throws ParserException, IOException
 	{
 		_conf.reparse();
 	}
 	
 	/**
 	 * serialization write-method
 	 * @param s
 	 * @throws IOException
 	 */
 	private synchronized void writeObject( java.io.ObjectOutputStream s ) throws IOException
 	{
 		s.writeLong(serialVersionUID);
 		s.writeObject(_conf);
 		s.writeBoolean(_httpd != null);
 		if(_httpd != null)
 		{
 			s.writeObject(_httpd.command());
 		}
 	}
 
 	/**
 	 * serialization read-method
 	 * @param s
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	@SuppressWarnings("unchecked")
 	private synchronized void readObject( java.io.ObjectInputStream s ) throws IOException, ClassNotFoundException
 	{
 		if(s.readLong() != serialVersionUID)
 		{
 			throw new InvalidClassException("Trying to deserialize an object but it's serialVersionUID doesn't match the implementation.");
 		}
 		
 		_conf = (ConfigFile) s.readObject();
 		if(s.readBoolean())
 		{
 			_httpd = new ProcessBuilder((List<String>) s.readObject());
 		}
 		
 	}
 }
