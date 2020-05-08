 /*OsmUi is a user interface for Osmosis
     Copyright (C) 2011  Verena Käfer, Peter Vollmer, Niklas Schnelle
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or 
     any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
  * 
  */
 package de.osmui.util;
 
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Scanner;
 
 import de.osmui.model.exceptions.TasksNotCompatibleException;
 import de.osmui.model.exceptions.TasksNotInModelException;
 import de.osmui.model.pipelinemodel.AbstractParameter;
 import de.osmui.model.pipelinemodel.AbstractPipelineModel;
 import de.osmui.model.pipelinemodel.AbstractPipe;
 import de.osmui.model.pipelinemodel.AbstractPort;
 import de.osmui.model.pipelinemodel.AbstractTask;
 import de.osmui.model.pipelinemodel.IntParameter;
 import de.osmui.model.pipelinemodel.JGPipelineModel;
 import de.osmui.model.pipelinemodel.VariablePipe;
 import de.osmui.model.pipelinemodel.VariablePort;
 import de.osmui.util.exceptions.ImportException;
 import de.osmui.util.exceptions.TaskNameUnknownException;
 
 /**
  * This class is responsible for importing an Osmosis command line into an
  * AbstractPipelineModel and exporting an Osmosis command line from a given
  * AbstractPipelineModel. It is implemented using the Singelton pattern.
  * 
  * @author Niklas Schnelle
  * 
  * @see CommandLineTraslatorTest
  */
 public class CommandlineTranslator {
 	protected static CommandlineTranslator instance;
 	
 	/** Private constructor for Singelton pattern **/
 	private CommandlineTranslator(){}
 
 	/**
 	 * This method finishes a pipe, that is it connects its still unconnected
 	 * ports and adds its pipes to the stack, or if they are named map
 	 * 
 	 * @param model
 	 * @param currTask
 	 * @param pipeStack
 	 * @param pipeMap
 	 * @throws ImportException
 	 */
 	private void finishTask(AbstractPipelineModel model, AbstractTask currTask,
 			Stack<AbstractPipe> pipeStack, Map<String, AbstractPipe> pipeMap)
 			throws ImportException {
 		System.out.println(currTask.getCommandlineForm());
 		// Wire up input ports not connected through explicit naming using the
 		// stack
 		for (AbstractPort port : currTask.getInputPorts()) {
 			if (!port.isConnected()) {
 				try {
 					model.connectTasks(pipeStack.pop(), port);
 				} catch (TasksNotCompatibleException e) {
 					throw new ImportException(
 							"Tried to connect incompatible tasks at:"
 									+ currTask.getCommandlineForm());
 				} catch(EmptyStackException e){
 					throw new ImportException("No unnamed pipes on the stack to connect");
 				} catch (TasksNotInModelException e) {
 					// Failure of program logic task should be in the model
 					e.printStackTrace();
 				}
 			}
 		}
 		// Push all unnamed pipes onto the stack and add named pipes to the map
 		for (AbstractPipe pipe : currTask.getOutputPipes()) {
 			if (!pipe.isNamed()) {
 				pipeStack.push(pipe);
 			} else {
 				pipeMap.put(pipe.getName(), pipe);
				// If the pipe name starts with "AUTO" we don't need this auto 
				// generated name anymore
				pipe.setName(null);
 			}
 		}
 		System.out.println("Task done");
 
 	}
 
 	/**
 	 * This method creates a new task object from the given token
 	 * 
 	 * @param tm
 	 * @param currToken
 	 * @return
 	 * @throws ImportException
 	 */
 	private AbstractTask createTaskFromToken(TaskManager tm, String currToken)
 			throws ImportException {
 		// Create a new task object
 		String taskName = tm.unshortenTaskname(currToken.substring(2));
 		try {
 			return tm.createTask(taskName);
 		} catch (TaskNameUnknownException e) {
 			throw new ImportException("Taskname " + taskName
 					+ "doesn't match anything");
 		}
 	}
 
 	/**
 	 * This method handles parameters for the current task, especially useful
 	 * here is that for variable pipes their specifying parameter will be dealt
 	 * with. That is new pipes will be created.
 	 * 
 	 * @param currTask
 	 * @param param
 	 * @param paramValue
 	 * @throws ImportException
 	 */
 	private void handleParam(AbstractTask currTask, AbstractParameter param,
 			String paramValue) throws ImportException {
 
 	
 		// Check if the parameter specifies a variable
 		// pipe/port count and create pipes/ports
 		// accordingly
 		if (param instanceof IntParameter) {
 			IntParameter intParam = (IntParameter) param;
 			int wantedCount = Integer.parseInt(paramValue);
 			int defaultCount = Integer.parseInt(intParam.getDefaultValue());
 
 			for (AbstractPort port : currTask.getInputPorts()) {
 
 				if (port instanceof VariablePort
 						&& ((VariablePort)port).getReferencedParam().equals(intParam)) {
 					AbstractPort newPort;
 					for (int i = defaultCount; i < wantedCount; ++i) {
 						newPort = ((VariablePort) port).createPort();
 						currTask.getInputPorts().add(newPort);
 					}
 					// We are done
 					return;
 				}
 			}
 
 			for (AbstractPipe pipe : currTask.getOutputPipes()) {
 
 				if (pipe instanceof VariablePipe
 						&& ((VariablePipe) pipe).getReferencedParam().equals(intParam)) {
 					AbstractPipe newPipe;
 					for (int i = defaultCount; i < wantedCount; ++i) {
 						newPipe = ((VariablePipe) pipe).createPipe();
 						currTask.getOutputPipes().add(newPipe);
 					}
 					// We are done
 					return;
 				}
 			}
 		}
 		
 		try{
 			param.setValue(paramValue);
 		}
 		catch(NumberFormatException e){
 			throw new ImportException("Parameter: "+param.getName()+" expected Number");
 		}
 	}
 
 	/**
 	 * This method handles Tokens that are either a pipe or a parameter
 	 * 
 	 * @param model
 	 *            the model we are importing to
 	 * @param currTask
 	 *            the current task
 	 * @param pipeMap
 	 *            the map where named pipes are stored
 	 * @param currToken
 	 *            the current token
 	 * @throws ImportException
 	 */
 	private void handleParamOrPipe(AbstractPipelineModel model,
 			AbstractTask currTask, Map<String, AbstractPipe> pipeMap,
 			String currToken) throws ImportException {
 		AbstractParameter param;
 		AbstractPipe pipe;
 		String paramName;
 		String paramValue;
 		int pipeNum = 0;
 
 		int eqIndex = currToken.indexOf("=");
 		paramName = (eqIndex >= 0) ? currToken.substring(0, eqIndex) : null;
 		paramValue = (eqIndex >= 0) ? currToken.substring(eqIndex + 1)
 				: currToken;
 		if (paramName == null) {
 			// This is the value of the default parameter
 			param = currTask.getDefaultParameter();
 			handleParam(currTask, param, paramValue);
 		} else if (paramName.startsWith("inPipe.")) {
 			// We need to connect a named pipe with the correct port
 			// 7th position is the one after the .
 			pipeNum = Integer.parseInt(paramName.substring(7));
 			// Check if the pipeNum exists
 			if (pipeNum >= currTask.getInputPorts().size()) {
 				throw new ImportException(
 						"Pipe index of inPipe does not exist: " + currToken);
 			}
 			pipe = pipeMap.remove(paramValue);
 			if (pipe == null) {
 				throw new ImportException("Can't connect to unknown pipe: "
 						+ paramValue);
 			}
 			try {
 				model.connectTasks(pipe, currTask.getInputPorts().get(pipeNum));
 			} catch (TasksNotCompatibleException e) {
 				throw new ImportException(
 						"Tried to connect incompatible tasks at: "
 								+ currTask.getCommandlineForm());
 			} catch (TasksNotInModelException e) {
 				// Failure in program logic task should be added
 				e.printStackTrace();
 			}
 
 		} else if (paramName.startsWith("outPipe.")) {
 			// We got a new named pipe
 			// 6th position is the one after the .
 			pipeNum = Integer.parseInt(paramName.substring(8));
 			// Check if the pipeNum exists
 			if (pipeNum >= currTask.getOutputPipes().size()) {
 				throw new ImportException(
 						"Pipe index of outPipe does not exist: " + currToken);
 			}
 			currTask.getOutputPipes().get(pipeNum).setName(paramValue);
 		} else {
 			// This is a named parameter
 			param = currTask.getParameters().get(paramName);
 			if (param == null) {
 				throw new ImportException("Unknown parameter: " + paramName
 						+ " for Task" + currTask.getName());
 			}
 			handleParam(currTask, param, paramValue);
 		}
 	}
 
 	/**
 	 * Imports an osmosis command line into the given model. This is an example
 	 * line: "--rx full/planet-071128.osm.bz2 " + "--tee 2 outPipe.1=fooPipe " +
 	 * "--bp file=polygons/europe/germany/baden-wuerttemberg.poly \\" +
 	 * "--wx baden-wuerttemberg.osm.bz2 inPipe.0=fooPipe \\" +
 	 * "--bp file=polygons/europe/germany/bayern.poly " + "--wx bayern.osm.bz2""
 	 * 
 	 * @param model
 	 * @param line
 	 * @throws ImportException
 	 */
 	public void importLine(AbstractPipelineModel model, String line)
 
 			throws ImportException {
 		//StringTokenizer st = new StringTokenizer(line, " \n\r\f\t");
 		Scanner st = new Scanner(line);
 		st.useDelimiter("[ \\t\\r\\n\\f\\\\]+");
 
 		// Stack for unnamed pipes
 		Stack<AbstractPipe> pipeStack = new Stack<AbstractPipe>();
 		// HashMap for named pipes so we can connect them later on
 		HashMap<String, AbstractPipe> pipeMap = new HashMap<String, AbstractPipe>();
 
 		AbstractTask currTask = null;
 		TaskManager tm = TaskManager.getInstance();
 		String currToken;
 
 		while (st.hasNext()) {
 			currToken = st.next();
 			// System.out.println(currToken);
 			if (currToken.startsWith("--")) {
 				// Ok we got a new task:
 				// If currTask != null this wasn't the first task and the last
 				// is done
 				if (currTask != null) {
 					finishTask(model, currTask, pipeStack, pipeMap);
 				}
 
 				currTask = createTaskFromToken(tm, currToken);
 				model.addTask(currTask);
 			} else {
 				// Not a task must be parameter or pipe, the currTask must be
 				// non null or else something's wrong
 				if (currTask == null) {
 					throw new ImportException("Parameters before first task");
 				}
 				handleParamOrPipe(model, currTask, pipeMap, currToken);
 			}
 
 		}
 
 		// Finish the last Task
 		if (currTask != null) {
 			finishTask(model, currTask, pipeStack, pipeMap);
 		}
 
 	}
 
 	
 	/**
 	 * This private function is used to deal with a unfinished task:
 	 * - It adds all still unfinished Downstream tasks that aren't yet in unfinished to it
 	 * - It tries whether all dependencies are met, if not pushing them
 	 * - When called again (after being added by a now finished upstream task) it marks this
 	 * task as finished and writes it to the StringBuilder
 	 * - This algorithm ensures that tasks will (if possible) be put right before their downstream neighbor
 	 * @param unfin
 	 * @param fin
 	 * @param sb
 	 * @param task
 	 */
 	private void exportTask(Stack<AbstractTask> unfin, Set<AbstractTask> fin, StringBuilder sb, AbstractTask task){
 		// When we are done we need the downstream tasks on the stack
 		AbstractTask currTask;
 		AbstractPort downPort;		
 		// Push all connected unfinished Downstream tasks and 
 		for(AbstractPipe pipe : task.getOutputPipes()){
 			if(pipe.isConnected()){
 				downPort = pipe.getTarget();
 				currTask = downPort.getParent();
 				if(!fin.contains(currTask) && !unfin.contains(currTask)){
 					unfin.push(currTask);
 				}
 			}
 		}
 		
 		
 		// Push any unfinished dependencies
 		AbstractPipe upPipe;
 		boolean unmetDependecy = false;
 		for(AbstractPort port : task.getInputPorts()){
 			if(port.isConnected()){
 				upPipe = port.getIncoming();
 				currTask = upPipe.getSource();
 				if(!fin.contains(currTask)){
 					unfin.push(currTask);
 					unmetDependecy = true;
 				}
 			}
 			
 		}
 		if(unmetDependecy){
 			return;
 		}
 		// All dependencies are now cleared append task (without pipes)
 		sb.append(task.getCommandlineForm());
 
 		// This task is now finished
 		fin.add(task);
 		
 	}
 	/**
 	 * This method exports a model into a osmosis call e.g.
 	 * 
 	 * @param model
 	 * @return the line e.g. "--foo opt outPipe.0=AUTO1to1 --bar opt=val inPipe.0=AUTO1to1"
 	 */
 	public String exportLine(AbstractPipelineModel model){
 		
 		Stack<AbstractTask> unfinished = new Stack<AbstractTask>();
 		HashSet<AbstractTask> finished = new HashSet<AbstractTask>();
 		StringBuilder builder = new StringBuilder();
 		// Add all source tasks to the unfinished stack
 		for(AbstractTask task : model.getSourceTasks()){
 			unfinished.add(task);
 		}
 		
 		while(!unfinished.isEmpty()){
 			AbstractTask currTask = unfinished.pop();
 			if(!finished.contains(currTask)){
 				// This call tries to finish the task, if it still needs dependencies
 				// it will push those to resolve them first
 				exportTask(unfinished, finished, builder, currTask);
 			}
 		}
 		
 		return builder.toString();
 	}
 	
 	/**
 	 * This returns an Instance of CommandlineTranslator, see Singelton pattern
 	 * 
 	 * @return
 	 */
 	public static CommandlineTranslator getInstance() {
 		if (instance == null) {
 			instance = new CommandlineTranslator();
 		}
 		return instance;
 	}
 
 	// Little test method ;-)
 	public static void main(String[] args) {
 		JGPipelineModel model = new JGPipelineModel();
 		CommandlineTranslator trans = CommandlineTranslator.getInstance();
 		try {
 			trans.importLine(
 					model,
 					"--rx full/planet-071128.osm.bz2 "
 					+ "--tee  4  \n\\\t"
 					+ "--bp file=polygons/europe/germany/baden-wuerttemberg.poly  \\"
 					+ "--wx baden-wuerttemberg.osm.bz2  \\"
 					+ "--bp file=polygons/europe/germany/baden-wuerttemberg.poly  \\"
 					+ "--wx baden-wuerttemberg.osm.bz2  \\"
 					+ "--bp file=polygons/europe/germany/baden-wuerttemberg.poly  \\"
 					+ "--wx baden-wuerttemberg.osm.bz2  \\"
 					+ "--bp file=polygons/europe/germany/baden-wuerttemberg.poly  \\");
 			
 			System.out.println(trans.exportLine(model));
 		} catch (ImportException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
