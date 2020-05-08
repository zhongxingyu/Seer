 package com.seitenbau.eclipse.plugin.consolelink;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.ui.console.FileLink;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.console.IPatternMatchListenerDelegate;
 import org.eclipse.ui.console.PatternMatchEvent;
 import org.eclipse.ui.console.TextConsole;
 
 public class LinkPatternMatchListenerDelegate implements IPatternMatchListenerDelegate 
 {
 
 	private static final String FILE_PREFIX = "file://";
 
 	TextConsole console;
 	
 	@Override
 	public void connect(TextConsole console) 
 	{
 		this.console = console;
 	}
 
 	@Override
 	public void disconnect() 
 	{
 		this.console = null;
 	}
 
 	@Override
 	public void matchFound(PatternMatchEvent event) 
 	{
 		String pathString = "";
 		if (event.getSource().equals(console)) 
 		{
 			try 
 			{
 				pathString = console.getDocument().get(event.getOffset(), event.getLength());
 				if (pathString.startsWith(FILE_PREFIX)) 
 				{
 					pathString = pathString.substring(FILE_PREFIX.length());
 				}
 			} 
 			catch (BadLocationException exp) 
 			{
 				String msg = String.format("Bad location path %s", pathString);
 				debug(msg , exp);
 			}
 			IPath path = Path.fromOSString(pathString);
 			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
 			if (file != null) 
 			{
				if(file.exists())
 				{
					FileLink fileLink = new FileLink(file, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, -1, -1, -1);
					try 
					{
						console.addHyperlink(fileLink, event.getOffset(), event.getLength());
					} catch (BadLocationException exp) {
						String msg = String.format("Bad location path %s", pathString);
						debug(msg , exp);
					}
 				}
 			} 
 		}
 	}
 	
 	public void debug(String msg, Exception e) 
 	{
 		getLog().log(new Status(Status.INFO, Activator.PLUGIN_ID, Status.OK, msg, e));
 	}
 	
 	protected ILog getLog() 
 	{
 		return Activator.getDefault().getLog();
 	}
 
 }
