 package sk.stuba.fiit.perconik.debug;
 
 import java.util.Set;
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.commands.operations.OperationHistoryEvent;
 import org.eclipse.core.filebuffers.IFileBuffer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.jface.text.IMarkSelection;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
 import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import sk.stuba.fiit.perconik.core.utilities.PluginConsole;
 import sk.stuba.fiit.perconik.core.utilities.SmartStringBuilder;
 import sk.stuba.fiit.perconik.debug.plugin.Activator;
 import com.google.common.collect.ImmutableList;
 
 public final class Debug
 {
 	private Debug()
 	{
 		throw new AssertionError();
 	}
 	
 	private static final class ConsoleHolder
 	{
 		static final PluginConsole console = PluginConsole.of(Activator.getDefault());
 		
 		private ConsoleHolder()
 		{
 			throw new AssertionError();
 		}
 	}
 	
 	public static final PluginConsole getDefaultConsole()
 	{
 		return ConsoleHolder.console;
 	}
 	
 	private static final PluginConsole console()
 	{
 		return getDefaultConsole();
 	}
 
 	public static final void put(final String message)
 	{
 		console().put(message);
 	}
 	
 	public static final void put(final String format, final Object ... args)
 	{
 		console().put(format, args);
 	}
 	
 	public static final void print(final String message)
 	{
 		console().print(message);
 	}
 	
 	public static final void print(final String format, final Object ... args)
 	{
 		console().print(format, args);
 	}
 
 	public static final void notice(final String message)
 	{
 		console().notice(message);
 	}
 	
 	public static final void notice(final String format, Object ... args)
 	{
 		console().notice(format, args);
 	}
 	
 	public static final void warning(final String message)
 	{
 		console().warning(message);
 	}
 
 	public static final void warning(final String format, Object ... args)
 	{
 		console().warning(format, args);
 	}
 
 	public static final void error(final String message, final Exception e)
 	{
 		console().error(message, e);
 	}
 	
 	public static final String dumpFileBuffer(final IFileBuffer buffer)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		IPath   location = buffer.getLocation();
 		IStatus status   = buffer.getStatus();
 		
 		long modificationStamp = buffer.getModificationStamp();
 		
 		boolean commitable     = buffer.isCommitable();
 		boolean dirty          = buffer.isDirty();
 		boolean shared         = buffer.isShared();
 		boolean stateValidated = buffer.isStateValidated();
 		
 		boolean synchronizationContextRequested = buffer.isSynchronizationContextRequested();
 		boolean synchronizedWithFileSystem      = buffer.isSynchronized();
 
 		builder.append("location: ").appendln(location);
		builder.appendln("status:").lines(dumpStatus(status));
 		
 		builder.append("modification stamp: ").appendln(modificationStamp < 0 ? "unknown" : modificationStamp);
 		
 		builder.append("commitable: ").appendln(commitable);
 		builder.append("dirty: ").appendln(dirty);
 		builder.append("shared: ").appendln(shared);
 		builder.append("state validated: ").appendln(stateValidated);
 		
 		builder.append("synchronization context requested: ").appendln(synchronizationContextRequested);
 		builder.append("synchronized with file system: ").appendln(synchronizedWithFileSystem);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpLaunch(final ILaunch launch) throws CoreException
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
 		
 		String  mode = launch.getLaunchMode();
 		
 		builder.append("mode: ").appendln(mode);
 		builder.appendln("configuration:").lines(dumpLaunchConfiguration(configuration));
 	
 		return builder.toString();
 	}
 
 	public static final String dumpLaunchConfiguration(final ILaunchConfiguration configuration) throws CoreException
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		ILaunchConfigurationType type = configuration.getType();
 		IFile file = configuration.getFile();
 		
 		String      name     = configuration.getName();
 		String      category = configuration.getCategory();
 		Set<String> modes    = configuration.getModes();
 		
 		String  application = configuration.getAttribute("application", "?");
 		String  product     = configuration.getAttribute("product", "?");
 		boolean useProduct  = configuration.getAttribute("useProduct", false);
 		
 		builder.append("name: ").appendln(name);
 		builder.append("category: ").appendln(category);
 		builder.appendln("type:").lines(dumpLaunchConfigurationType(type));
 		builder.append("modes: ").list(modes.isEmpty() ? ImmutableList.of("none") : modes).appendln();
 		
 		if (file != null)
 		{
 			builder.append("full path: ").appendln(file.getFullPath());
 			builder.append("location: ").appendln(file.getLocation());
 			builder.append("URI: ").appendln(file.getLocationURI());
 		}
 		
 		builder.append("application: ").appendln(application);
 		builder.append("product: ").append(product).append(" (use ").append(useProduct).appendln(")");
 		
 //		Map<Object, Object> attributes = configuration.getAttributes();
 //		
 //		for (Entry<Object, Object> entry: attributes.entrySet())
 //		{
 //			builder.append(entry.getKey()).append(": ").appendln(entry.getValue());
 //		}
 
 		return builder.toString();
 	}
 
 	public static final String dumpLaunchConfigurationType(final ILaunchConfigurationType type)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		String name             = type.getName();
 		String category         = type.getCategory();
 		String identifier       = type.getIdentifier();
 		String pluginIdentifier = type.getPluginIdentifier();
 		String contributorName  = type.getContributorName();
 
 		builder.append("name: ").appendln(name);
 		builder.append("category: ").appendln(category);
 		builder.append("identifier: ").appendln(identifier);
 		builder.append("plugin identifier: ").appendln(pluginIdentifier);
 		builder.append("contributor name: ").appendln(contributorName);
 		
 		return builder.toString();
 	}	
 	
 	public static final String dumpOperationHistoryEvent(final OperationHistoryEvent event)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 		IUndoableOperation operation = event.getOperation();
 		IStatus            status    = event.getStatus();
 
 		int type = event.getEventType();
 		
 		builder.append("type: ").appendln(type);
 		
 		builder.appendln("operation:").lines(dumpUndoableOperation(operation));
		builder.appendln("status:").lines(dumpStatus(status));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpPage(final IWorkbenchPage page)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		String label = page.getLabel();
 
 		builder.append("label: ").appendln(label);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpPart(final IWorkbenchPart part)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		String title   = part.getTitle();
 		String tooltip = part.getTitleToolTip();
 	
 		builder.append("title: ").appendln(title);
 		builder.append("tooltip: ").appendln(tooltip);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpRefactoringDescriptorProxy(final RefactoringDescriptorProxy proxy)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		String project     = proxy.getProject();
 		String description = proxy.getDescription();
 
 		long timestamp = proxy.getTimeStamp();
 	
 		builder.append("project: ").appendln(project);
 		builder.append("description: ").appendln(description);
 		builder.append("timestamp: ").appendln(timestamp);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpRefactoringExecutionEvent(final RefactoringExecutionEvent event)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 		
 		int type = event.getEventType();
 
 		RefactoringDescriptorProxy descriptor = event.getDescriptor();
 
 		builder.append("type: ").appendln(type);
 		builder.appendln("descriptor:").lines(dumpRefactoringDescriptorProxy(descriptor));
 		
 		return builder.toString();
 	}
 
 	public static final String dumpSelection(final ISelection selection)
 	{
 		if (selection instanceof IMarkSelection)
 		{
 			return dumpMarkSelection((IMarkSelection) selection);
 		}
 		else if (selection instanceof IStructuredSelection)
 		{
 			return dumpStructuredSelection((IStructuredSelection) selection);
 		}
 		else if (selection instanceof ITextSelection)
 		{
 			return dumpTextSelection((ITextSelection) selection);
 		}
 		else
 		{
 			SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 			boolean empty = selection.isEmpty();
 			
 			builder.append("empty: ").appendln(empty);
 			
 			return builder.toString();
 		}
 	}
 
 	public static final String dumpMarkSelection(final IMarkSelection selection)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 		boolean empty = selection.isEmpty();
 		
 		int offset = selection.getOffset();
 		int length = selection.getLength();
 
 		builder.append("empty: ").appendln(empty);
 		
 		builder.append("offset: ").appendln(offset);
 		builder.append("length: ").appendln(length);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpStructuredSelection(final IStructuredSelection selection)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 		boolean empty = selection.isEmpty();
 		
 		int size = selection.size();
 
 		builder.append("empty: ").appendln(empty);
 		
 		builder.append("size: ").appendln(size);
 		
 		return builder.toString();
 	}
 
 	public static final String dumpTextSelection(final ITextSelection selection)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 		boolean empty = selection.isEmpty();
 		
 		int start = selection.getStartLine();
 		int end   = selection.getEndLine();
 
 		int offset = selection.getOffset();
 		int length = selection.getLength();
 
 		String text = selection.getText();
 
 		builder.append("empty: ").appendln(empty);
 		
 		builder.append("start line: ").appendln(start);
 		builder.append("end line: ").appendln(end);
 
 		builder.append("offset: ").appendln(offset);
 		builder.append("length: ").appendln(length);
 
 		builder.append("text: \"").append(text).appendln('"');
 
 		return builder.toString();
 	}
 	
 	public static final String dumpThrowable(final Throwable throwable)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 		Class<?> type    = throwable.getClass();
 		String   message = throwable.getMessage();
 
 		builder.append("class: ").appendln(type);
 		builder.append("message: ").appendln(message);
 		
 		return builder.toString();
 	}
 	
 	public static final String dumpUndoableOperation(final IUndoableOperation operation)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 		String label = operation.getLabel();
 
 		boolean execute = operation.canExecute();
 		boolean redo    = operation.canRedo();
 		boolean undo    = operation.canUndo();
 		
 		builder.append("label: ").appendln(label);
 		
 		builder.append("execute: ").appendln(execute);
 		builder.append("redo: ").appendln(redo);
 		builder.append("undo: ").appendln(undo);
 		
 		return builder.toString();		
 	}
 
 	public static final String dumpStatus(final IStatus status)
 	{
 		SmartStringBuilder builder = new SmartStringBuilder().tab();
 
 		int code     = status.getCode();
 		int severity = status.getSeverity();
 		
 		String plugin  = status.getPlugin();
 		String message = status.getMessage();
 		
 		boolean ok    = status.isOK();
 		boolean multi = status.isMultiStatus();
 
 		Throwable throwable = status.getException();
 
 		builder.append("code: ").appendln(code);
 		builder.append("severity: ").appendln(severity);
 		
 		builder.append("plugin: ").appendln(plugin);
 		builder.append("message: ").appendln(message);
 		
 		builder.append("ok: ").appendln(ok);
 		builder.append("multi: ").appendln(multi);
 
 		if (throwable != null)
 		{
 			builder.appendln("exception:").lines(dumpThrowable(throwable));
 		}
 		
 		return builder.toString();
 	}
 }
