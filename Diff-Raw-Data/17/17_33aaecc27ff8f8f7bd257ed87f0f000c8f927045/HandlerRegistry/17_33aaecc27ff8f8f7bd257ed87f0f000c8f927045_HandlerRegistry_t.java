 package core;
 import data.ServerChannel;
 import data.ServerSource;
 import handlers.InputHandler;
 import handlers.OutputHandler;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.io.File;
 import java.net.URL;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.lang.reflect.Constructor;
 
 /**
  * @author see http://code.google.com/p/wirc/wiki/AUTHORS
  */
 public class HandlerRegistry
 {
 	private final Map<String, List<OutputHandler>> outputHandlers;
 	private final Map<String, List<InputHandler>> inputHandlers;
 	private final OutputHandler defaultOutputHandler;
 	private final InputHandler defaultInputHandler;
 	private final Facade mgr;
 
 	private static final Pattern splitter = Pattern.compile("\\s");
 
 	public HandlerRegistry(Facade mgr, OutputHandler defaultOutput, InputHandler defaultInput)
 	{
 		this.mgr = mgr;
 		this.defaultOutputHandler = defaultOutput;
 		this.defaultInputHandler = defaultInput;
 
 		this.outputHandlers = new HashMap<String, List<OutputHandler>>();
 		this.inputHandlers = new HashMap<String, List<InputHandler>>();
 	}
 
 	public void addInputHandler(InputHandler added, String... codes)
 	{
 		for (String code : codes)
 		{
 			if (inputHandlers.containsKey(code))
 			{
 				inputHandlers.get(code).add(added);
 			}
 			else
 			{
 				LinkedList<InputHandler> list = new LinkedList<InputHandler>();
 				list.add(added);
 
 				inputHandlers.put(code, list);
 			}
 		}
 	}
 
 	public void addOutputHandler(OutputHandler added, String... codes)
 	{
 		for (String code : codes)
 		{
 			if (outputHandlers.containsKey(code))
 			{
 				outputHandlers.get(code).add(added);
 			}
 			else
 			{
 				LinkedList<OutputHandler> list = new LinkedList<OutputHandler>();
 				list.add(added);
 
 				outputHandlers.put(code, list);
 			}
 		}
 	}
 
 	public boolean removeInputHandler(InputHandler par)
 	{
 		Boolean removed = false;
 		List temp;
 
 		for (String code : inputHandlers.keySet())
 		{
 			temp = inputHandlers.get(code);
 
 			if (temp.contains(par))
 			{
 				temp.remove(code);
 				removed = true;
 			}
 		}
 		
 		return removed;
 	}
 
 	public boolean removeOutputHandler(OutputHandler par)
 	{
 		Boolean removed = false;
 		List temp;
 
 		for (String code : outputHandlers.keySet())
 		{
 			temp = outputHandlers.get(code);
 
 			if (temp.contains(par))
 			{
 				temp.remove(code);
 				removed = true;
 			}
 		}
 
 		return removed;
 	}
 
 	public void postInputMessage(String msg, String server)
 	{
 		int sIndex = msg.indexOf(" ");
 
 		if (!msg.startsWith(":") || sIndex < 0)
 			throw new IllegalArgumentException("Malformed message");
 
 		// contradiction... chat.freenode.comPING :barjavel.freenode.net
 
 		String sender = msg.substring(1, sIndex);
 		String body = msg.substring(sIndex + 1);
 
 		postInputMessage(body, new ServerSource(server, sender));
 	}
 
 	public void postInputMessage(String msg, ServerSource source)
 	{
 		String[] msgParts = splitter.split(msg, 2);
 		String command = msgParts[0];
 
 		if (inputHandlers.containsKey(command))
 		{
 			for (InputHandler mp : inputHandlers.get(command))
 			{
 				try
 				{
 					mp.process(msg, source);
 				}
 				catch (Exception ex)
 				{
 					mgr.printDebugMsg(mp.getClass().getName() + " was passed a malformed message: [" + source + "] " + msg);
 				}
 			}
 		}
 		else
 		{
 			defaultInputHandler.process(msg, source);
 		}
 	}
 
 	public void postOutputMessage(String msg, ServerChannel dest)
 	{
 		String[] msgParts = msg.split("\\s", 2);
 
 		if (msgParts.length != 2) return;
 
		String command = msgParts[0].toUpperCase();

		if (outputHandlers.containsKey(command))
 		{
			for (OutputHandler mp : outputHandlers.get(command))
 			{
 				try
 				{
 					mp.process(msg, dest);
 				}
 				catch (Exception ex)
 				{
					mgr.printDebugMsg(mp.getClass().getName() + " was passed a malformed message: [" + msg + "]");
 				}
 			}
 		}
 		else
 		{
 			defaultOutputHandler.process(msg, dest);
 		}
 	}
 
 	public void registerHandler(Object handler, String[] handles)
 	{
 		if (handler == null || handles == null || handles.length == 0)
 			return;
 
 		if (handler instanceof handlers.OutputHandler)
 		{
 			addOutputHandler((handlers.OutputHandler)handler, handles);
 		}
 		else if (handler instanceof handlers.InputHandler)
 		{
 			addInputHandler((handlers.InputHandler)handler, handles);
 		}
 	}
 
 
 	public void unregisterHandler(Object handler, String[] handles)
 	{
 
 	}
 
 	public void loadHandlers(File path)
 	{
 		try
 		{
 			ClassLoader loader = new RemoteClassLoader();
 
 			String name, urlStr;
 			URL urlFile = path.toURI().toURL();
 
 			ZipInputStream zipIn = new ZipInputStream(urlFile.openStream());
 			ZipEntry entry;
 
 			while ((entry = zipIn.getNextEntry()) != null)
 			{
 				name = entry.getName();
 
 				if (name.matches("handlers/input/.+\\.class"))
 				{
 					System.out.println("Loading input handler: " + name);
 
 					urlStr = mgr.getClass().getResource("/" + name).toString();
 					Class<InputHandler> cls = (Class<InputHandler>)loader.loadClass(urlStr);
 					Constructor<InputHandler> con = (Constructor<InputHandler>)cls.getConstructors()[0];
 
 					InputHandler in = con.newInstance(mgr);	// store me
 
 					registerHandler(in, in.getHooks());
 				}
 				else if (name.matches("handlers/output/.+\\.class"))
 				{
 					System.out.println("Loading output handler: " + name);
 
 					urlStr = mgr.getClass().getResource("/" + name).toString();
 					Class<OutputHandler> cls = (Class<OutputHandler>)loader.loadClass(urlStr);
 					Constructor<OutputHandler> con = (Constructor<OutputHandler>)cls.getConstructors()[0];
 
 					OutputHandler out = con.newInstance(mgr);	// store me
 
 					registerHandler(out, out.getHooks());
 				}
 			}
 
 			zipIn.close();
 		}
 		catch (Exception e)
 		{
 			System.out.println("Could not load handlers: " + e);
 		}
 	}
 }
