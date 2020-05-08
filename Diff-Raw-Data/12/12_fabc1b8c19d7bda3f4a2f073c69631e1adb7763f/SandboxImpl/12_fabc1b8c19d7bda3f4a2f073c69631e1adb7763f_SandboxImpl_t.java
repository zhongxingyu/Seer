 package evaluationserver.server.sandbox;
 
 import evaluationserver.server.util.SystemCommand;
 import java.io.IOException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class SandboxImpl implements Sandbox {
 
 	private static final Logger logger = Logger.getLogger(SandboxImpl.class.getPackage().getName());
 	private final String command;
 	private final String inputDataKey;
 	private final String programKey;
 	private final String solutionDataKey;
 	private final String timeLimitKey;
 	private final String memoryLimitKey;
 	private final String outputLimitKey;
 	private final ExecutionResultFactory resultFactory;
 	
 	public SandboxImpl(String command, ExecutionResultFactory resultFactory, String programKey, String inputDataKey, String solutionDataKey, String timeLimitKey, String memoryLimitKey, String outputLimitKey) {
 		this.command = command;
 		this.programKey = programKey;
 		this.inputDataKey = inputDataKey;
 		this.solutionDataKey = solutionDataKey;
 		this.timeLimitKey = timeLimitKey;
 		this.memoryLimitKey = memoryLimitKey;
 		this.outputLimitKey = outputLimitKey;
 		this.resultFactory = resultFactory;
 	}
 
 	public SandboxImpl(String command, ExecutionResultFactory resultFactory) {
 		this(command, resultFactory, "%program%", "%inputData%", "%solutionData%", "%timeLimit%", "%memoryLimit%", "%outputLimit%");
 	}
 	
 	@Override
 	public ExecutionResult execute(Solution solution) throws ExecutionException {
 		logger.log(Level.FINEST, ("Sandbox start"));
 		final Date start = new Date();
 		final String cmd = this.prepareCommand(solution);
 		logger.log(Level.FINEST, ("Executing command: '" + cmd + "'"));
 		
 		SystemCommand systemCommand = new SystemCommand(Runtime.getRuntime());
 		try {
 			
 			systemCommand.exec(cmd);
 			logger.log(Level.FINEST, ("Sandbox message: '" + systemCommand.getOutput() + "'"));
 			
 			if (systemCommand.getReturnCode() != 0)
 				throw new ExecutionException("Sandbox process return value: " + systemCommand.getReturnCode() + "(" + systemCommand.getOutput() + ")");
 			
 			return resultFactory.create(systemCommand.getOutput(), start, systemCommand.getError());
 			
 		} catch (IOException ex) {
			throw new ExecutionException("Error during sandbox execution (" + ex.getMessage() + ")", ex);
 		} catch (InterruptedException ex) {
			throw new ExecutionException("Error during sandbox execution (" + ex.getMessage() + ")", ex);
 		} catch (IllegalArgumentException ex) {
 			throw new ExecutionException(ex.getMessage());
 		}
 	}
 
 
 	protected String prepareCommand(Solution solution) {
 		return command
 				.replace(programKey, solution.getProgram().getAbsolutePath())
 				.replace(inputDataKey, solution.getInputData().getAbsolutePath())
 				.replace(solutionDataKey, solution.getSolutionData().getAbsolutePath())
 				.replace(timeLimitKey, Integer.toString(solution.getTimeLimit()))
 				.replace(memoryLimitKey, Integer.toString(solution.getMemoryLimit()))
 				.replace(outputLimitKey, Integer.toString(solution.getOutputLimit()));
 	}
 }
