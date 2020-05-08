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
 package de.osmui.model.pipelinemodel;
 
 import java.util.List;
 import java.util.Observable;
 
 import de.osmui.model.exceptions.TasksNotCompatibleException;
 import de.osmui.model.exceptions.TasksNotInModelException;
 
 /**
  * This is the abstract base class for all pipeline models
  * 
  * @author Niklas Schnelle
  * 
  * @see AbstractPipelineModelTest
  */
 public abstract class AbstractPipelineModel extends Observable {
 
 	/**
 	 * Gets all Source Tasks in the model, that is the Tasks without any
 	 * inputPipes one normally traverses the model from the SourceTasks to the
 	 * drains using the Task objects
 	 * 
 	 * @return the list of SourceTasks
 	 */
 	public abstract List<AbstractTask> getSourceTasks();
 
 
 	/**
 	 * Adds the given task to the model
 	 * 
 	 * @param task
 	 */
 	public abstract void addTask(AbstractTask task);
 
 	/**
 	 * Adds the the child Task to the model using the parent task as parent,
 	 * that is connecting a compatible output of the parent to a compatible
 	 * input of the child. If there are no compatible pipes a
 	 * TasksNotCompatibleException will be thrown. If the parent is not yet in
 	 * the model TasksNotInModelException will be thrown
 	 * 
 	 * @param parent
 	 * @param child
 	 * @throws TasksNotCompatibleException
 	 * @throws TasksNotInModelException
 	 */
 	public abstract void addTask(AbstractTask parent, AbstractTask child)
 			throws TasksNotCompatibleException, TasksNotInModelException;
 
 	/**
 	 * Removes the given task from the model, if successfull returns true false
 	 * otherwise
 	 * 
 	 * @param task
 	 * @return boolean indicating success
 	 * @throws TasksNotInModelException 
 	 */
 	public abstract boolean removeTask(AbstractTask task) throws TasksNotInModelException;
 
 	/**
 	 * Connects the given tasks using the first parameter as parent task of the
 	 * second parameter. If there is no compatible pipe left a
 	 * TasksNotCompatibleException will be thrown. The first unconnected
 	 * outputPipe of the parent that has an unconnected and compatible port on
 	 * the child will be connected with this port. The pipe with which the
 	 * connection was made is returned
 	 * 
 	 * @param parent
 	 * @param child
 	 * @throws TasksNotCompatibleException
 	 * @throws TasksNotInModelException
 	 */
 	public AbstractPipe connectTasks(AbstractTask parent, AbstractTask child)
 			throws TasksNotCompatibleException, TasksNotInModelException {
 		if (parent == null || child == null) {
 			throw new TasksNotCompatibleException("parent or child is null");
 		} else if (parent.getModel() != this || child.getModel() != this) {
 			throw new TasksNotInModelException(
 					"Either parent or child is not in the model");
 		} else {
 			// Test outputPipes of the parent for compatibility with ports on
 			// the child
 			List<AbstractPipe> pipes = parent.getOutputPipes();
 			List<AbstractPort> ports = child.getInputPorts();
 			boolean outVariable = false;
 			boolean inVariable = false;
 			// First check for unconnected pipes then we can also look for variable ones
 			for (AbstractPipe out : pipes) {				
 				if(!out.isConnected()){
 					for (AbstractPort in : ports) {
 						if(!(in.isConnected()) && out.getType().equals(in.getType())){
 							if(out.connect(in)){
 								return out;
 							} else {
 								throw new TasksNotCompatibleException("connect failed");
 							}
 						}
 					}				
 				}
 			}
 			// If we got nothing yet check variable pipes
 			for (AbstractPipe out : pipes) {
 				outVariable = out instanceof VariablePipe;
 				if(outVariable){
 					for (AbstractPort in : ports) {
 						inVariable = in instanceof VariablePort;
 						// test if types match
 						if(out.getType().equals(in.getType())){
 							if(outVariable){
 								// Lets create a new pipe and reuse out
 								out = ((VariablePipe) out).createPipe();
 							}
 							if(inVariable){
 								// Lets create a new port and reuse in
 								in = ((VariablePort) in).createPort();
 							}
 							if(out.connect(in)){
 								return out;
 							} else {
 								throw new TasksNotCompatibleException("connect failed");
 							}
 						}
 					}				
 				}
 			}
 			// We haven't returned in the loop so no compatible pipe/port was
 			// found throw
 			throw new TasksNotCompatibleException(
 					"The given tasks weren't compatible");
 		}
 	}
 	/**
 	 * Connects the given output pipe with the given input port, if both corresponding tasks are in this
 	 * model
 	 * 
 	 * @param outputAbstractPipe output, AbstractPort input
 	 * @param input
 	 * @return
 	 * @throws TasksNotCompatibleException
 	 * @throws TasksNotInModelException
 	 */
 	public AbstractPipe connectTasks(AbstractPipe output, AbstractPort input) throws TasksNotCompatibleException, TasksNotInModelException {
 		if(output == null || input == null){
 			return null;
 		} else {
 			//The parent of the port is the child of the pipe, easy isn't it?
 			AbstractTask child = input.getParent();
 			AbstractTask parent = output.getSource();
 			if(child.getModel() != this || parent.getModel() != this){
 				throw new TasksNotInModelException("Either parent or child isn't in the model");
 			} else if (output.isConnected() || input.isConnected() || !output.connect(input)){
 				throw new TasksNotCompatibleException("Parent and child weren't compatible on this pipe/port");
 			}
 			
 			return output;
 		}
 		
 	}
 
 	/**
 	 * Disconnects the two tasks if they were connected, does nothing otherwise
 	 * 
 	 * @param parent
 	 * @param child
 	 * @return the pipe that was unconnected, null if nothing was disconnected
 	 */
 	public AbstractPipe disconnectTasks(AbstractTask parent, AbstractTask child)
 			throws TasksNotInModelException {
 		if (parent == null || child == null) {
 			// Can't connect with null return null
 			return null;
 		} else if (parent.getModel() != this || child.getModel() != this) {
 			throw new TasksNotInModelException(
 					"Either parent or child is not in the model");
 		} else {
 			// First we need to find the connection between the two and then
 			// remove it
 			List<AbstractPipe> pipes = parent.getOutputPipes();
 
 			for (AbstractPipe out : pipes) {
 				if (out.getTarget().getParent().equals(child)) {
 					out.disconnect();
 					return out;
 				}
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * Gets whether this model currently represents a executable pipeline that
 	 * is a pipeline that can be executed by osmosis
 	 * 
 	 * @return true if executable false otherwise
 	 */
 	public abstract boolean isExecutable();
 
 }
