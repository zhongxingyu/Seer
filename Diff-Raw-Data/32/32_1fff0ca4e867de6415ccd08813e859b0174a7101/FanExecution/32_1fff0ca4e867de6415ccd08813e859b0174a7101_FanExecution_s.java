 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.actions;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.Future;
 import net.jot.logger.JOTLogger;
 import net.jot.utils.JOTUtilities;
 import org.netbeans.api.extexecution.ExecutionDescriptor;
 import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
 import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
 import org.netbeans.api.extexecution.ExecutionService;
 import org.netbeans.api.extexecution.ExternalProcessBuilder;
 import org.netbeans.api.extexecution.input.InputProcessor;
 import org.netbeans.api.extexecution.print.LineConvertor;
 import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.Exceptions;
 
 /**
  * Mostly copied from python module
  * Provides shell for running external commands
  * @author thibautc
  */
 public class FanExecution
 {
 	// execution commands
 
 	private String command;
 	private String workingDirectory;
	private Vector<String> commandArgs = new Vector();
 	//private String path;
 	private String displayName;
 	private boolean redirect;
 	private List<LineConvertor> outConvertors = new ArrayList<LineConvertor>();
 	private List<LineConvertor> errConvertors = new ArrayList<LineConvertor>();
 	private InputProcessorFactory outProcessorFactory;
 	private InputProcessorFactory errProcessorFactory;
 	private boolean addStandardConvertors;
 	private FileLocator fileLocator;
 	private boolean lineBased;
 	private Runnable postExecutionHook;
 
 	public FanExecution()
 	{
 	}
 
 	@Override
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder("Executing in [").append(workingDirectory).append("] ").append(command).append(" ");
 		for (String arg : commandArgs)
 		{
 			sb = sb.append(arg).append(" ");
 		}
 		return sb.toString();
 	}
 
 	public ExecutionDescriptor toExecutionDescriptor()
 	{
 		return descriptor;
 	}
 	//internal process control
 	private ExecutionDescriptor descriptor = new ExecutionDescriptor().frontWindow(true).controllable(true).inputVisible(true).showProgress(true).showSuspended(true);
 
 	/**
 	 * Execute the process described by this object
 	 * @return a Future object that provides the status of the running process
 	 */
 	public synchronized Future<Integer> run()
 	{
 		try
 		{
 			ExecutionService service =
 				ExecutionService.newService(
 				buildProcess(),
 				descriptor, displayName);
 			//io = descriptor.getInputOutput();
 			// Start Service
 			JOTLogger.info(this, toString());
 			return service.run();
 			//io = InputOutputManager.getInputOutput(displayName, true, path).getInputOutput();
 		} catch (Exception ex)
 		{
 			Exceptions.printStackTrace(ex);
 			return null;
 		}
 
 	}
 
 	public ExternalProcessBuilder buildProcess() throws IOException
 	{
 		ExternalProcessBuilder processBuilder =
 			new ExternalProcessBuilder(command);
 		if (workingDirectory == null)
 		{
 			workingDirectory = new File(System.getProperty("user.home")).getAbsolutePath();
 		}
 		processBuilder = processBuilder.workingDirectory(FileUtil.normalizeFile(new File(workingDirectory)));
 		for (int i = 0; i != commandArgs.size(); i++)
 		{
 			String arg = commandArgs.get(i);
 			if (arg != null && arg.trim().length() > 0)
 			{
 				if (JOTUtilities.isWindowsOS())
 				{
 					arg = arg.replace("\\", "\\\\");
 				}
 				processBuilder = processBuilder.addArgument(arg);
 			}
 		}
 
 		processBuilder = processBuilder.redirectErrorStream(redirect);
 		return processBuilder;
 	}
 
 	private ExecutionDescriptor buildDescriptor()
 	{
 
 		return descriptor;
 	}
 
 	public synchronized String getCommand()
 	{
 		return command;
 	}
 
 	public synchronized void setCommand(String command)
 	{
 		this.command = command;
 	}
 
 	public synchronized Vector<String> getCommandArgs()
 	{
 		return commandArgs;
 	}
 
 	public synchronized void addCommandArg(String arg)
 	{
 		if (arg != null && arg.length() > 0)
 		{
 			commandArgs.add(arg);
 		}
 	}
 
 	public synchronized String getWorkingDirectory()
 	{
 		return workingDirectory;
 	}
 
 	public synchronized void setWorkingDirectory(String workingDirectory)
 	{
 		this.workingDirectory = workingDirectory;
 	}
 
 	public synchronized String getDisplayName()
 	{
 		return displayName;
 	}
 
 	public synchronized void setDisplayName(String displayName)
 	{
 		this.displayName = displayName;
 	}
 
 	public synchronized void setShowControls(boolean showControls)
 	{
 		descriptor = descriptor.controllable(showControls);
 	}
 
 	public FanExecution addOutConvertor(LineConvertor convertor)
 	{
 		this.outConvertors.add(convertor);
 		descriptor = descriptor.outConvertorFactory(lineConvertorFactory(outConvertors));
 		return this;
 	}
 
 	public FanExecution addErrConvertor(LineConvertor convertor)
 	{
 		this.errConvertors.add(convertor);
 		descriptor = descriptor.errConvertorFactory(lineConvertorFactory(errConvertors));
 		return this;
 	}
 
 	public synchronized void addStandardRecognizers()
 	{
 		this.addStandardConvertors = true;
 		descriptor = descriptor.outConvertorFactory(lineConvertorFactory(outConvertors));
 		descriptor = descriptor.errConvertorFactory(lineConvertorFactory(errConvertors));
 	}
 
 	public void setErrProcessorFactory(InputProcessorFactory errProcessorFactory)
 	{
 		this.errProcessorFactory = errProcessorFactory;
 		descriptor = descriptor.errProcessorFactory(errProcessorFactory);
 	}
 
 	public void setOutProcessorFactory(InputProcessorFactory outProcessorFactory)
 	{
 		this.outProcessorFactory = outProcessorFactory;
 		descriptor = descriptor.outProcessorFactory(outProcessorFactory);
 	}
 
 	public FanExecution lineBased(boolean lineBased)
 	{
 		this.lineBased = lineBased;
 		if (lineBased)
 		{
 			descriptor = descriptor.errLineBased(lineBased).outLineBased(lineBased);
 		}
 
 		return this;
 	}
 
 	private LineConvertorFactory lineConvertorFactory(List<LineConvertor> convertors)
 	{
 		LineConvertor[] convertorArray = convertors.toArray(new LineConvertor[convertors.size()]);
 		if (addStandardConvertors)
 		{
 			return FanLineFactory.withStandardConvertors(fileLocator, convertorArray);
 		}
 		return FanLineFactory.create(fileLocator, convertorArray);
 	}
 
 	public synchronized void setShowInput(boolean showInput)
 	{
 		descriptor = descriptor.inputVisible(showInput);
 	}
 
 	public synchronized void setRedirectError(boolean redirect)
 	{
 		this.redirect = redirect;
 	}
 
 	public synchronized void setShowProgress(boolean showProgress)
 	{
 		descriptor = descriptor.showProgress(showProgress);
 	}
 
 	/**
 	 * Can the process be suppended
 	 * @param showSuspended boolean to set the status
 	 */
 	public synchronized void setShowSuspended(boolean showSuspended)
 	{
 		descriptor = descriptor.showSuspended(showSuspended);
 	}
 
 	/**
 	 * Show the window of the running process
 	 * @param showWindow display the windown or not?
 	 */
 	public synchronized void setShowWindow(boolean showWindow)
 	{
 		descriptor = descriptor.frontWindow(showWindow);
 	}
 	private final FanOutputProcessor outProcessor = new FanOutputProcessor();
 
 	/**
 	 * Attach a Processor to collect the output of the running process
 	 */
 	public void attachOutputProcessor()
 	{
 		descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory()
 		{
 
 			public InputProcessor newInputProcessor()
 			{
 				return outProcessor;
 			}
 
 			public InputProcessor newInputProcessor(InputProcessor defaultProcessor)
 			{
 				return outProcessor;
 			}
 		});
 	}
 
 	public void setPostExecutionHook(Runnable runnable)
 	{
 		postExecutionHook = runnable;
 		descriptor = descriptor.postExecution(runnable);
 	}
 
 	public Runnable getPostExecutionHook()
 	{
 		return postExecutionHook;
 	}
 
 	/**
 	 * Retive the output form the running process
 	 * @return a string reader for the process
 	 */
 	public Reader getOutput()
 	{
 		return new StringReader(outProcessor.getData());
 	}
 
 	/**
 	 * Attach input processor to the running process
 	 */
 	public void attachInputProcessor()
 	{
 		//descriptor = descriptor.
 	}
 
 	/**
 	 * Writes data to the running process
 	 * @return StringWirter
 	 */
 	public Writer getInput()
 	{
 		return null;
 	}
 }
