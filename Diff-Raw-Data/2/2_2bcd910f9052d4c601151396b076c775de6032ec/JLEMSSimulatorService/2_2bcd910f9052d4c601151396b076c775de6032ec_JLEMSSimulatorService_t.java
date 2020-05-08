 /*******************************************************************************
  * The MIT License (MIT)
  *
  * Copyright (c) 2011, 2013 OpenWorm.
  * http://openworm.org
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the MIT License
  * which accompanies this distribution, and is available at
  * http://opensource.org/licenses/MIT
  *
  * Contributors:
  *     	OpenWorm - http://openworm.org/people.html
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
  * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
  * USE OR OTHER DEALINGS IN THE SOFTWARE.
  *******************************************************************************/
 package org.geppetto.simulator.jlems;
 
 import java.util.Collection;
 import java.util.StringTokenizer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geppetto.core.common.GeppettoExecutionException;
 import org.geppetto.core.common.GeppettoInitializationException;
 import org.geppetto.core.model.IModel;
 import org.geppetto.core.model.ModelWrapper;
 import org.geppetto.core.model.state.AStateNode;
 import org.geppetto.core.model.state.CompositeStateNode;
 import org.geppetto.core.model.state.SimpleStateNode;
 import org.geppetto.core.model.state.StateTreeRoot;
 import org.geppetto.core.model.values.ValuesFactory;
 import org.geppetto.core.simulation.IRunConfiguration;
 import org.geppetto.core.simulation.ISimulatorCallbackListener;
 import org.geppetto.core.simulator.ASimulator;
 import org.lemsml.jlems.core.api.ALEMSValue;
 import org.lemsml.jlems.core.api.LEMSBuildConfiguration;
 import org.lemsml.jlems.core.api.LEMSBuildException;
 import org.lemsml.jlems.core.api.LEMSBuildOptions;
 import org.lemsml.jlems.core.api.LEMSBuildOptionsEnum;
 import org.lemsml.jlems.core.api.LEMSBuilder;
 import org.lemsml.jlems.core.api.LEMSDocumentReader;
 import org.lemsml.jlems.core.api.LEMSDoubleValue;
 import org.lemsml.jlems.core.api.LEMSExecutionException;
 import org.lemsml.jlems.core.api.LEMSResultsContainer;
 import org.lemsml.jlems.core.api.LEMSSimulator;
 import org.lemsml.jlems.core.api.interfaces.ILEMSBuildConfiguration;
 import org.lemsml.jlems.core.api.interfaces.ILEMSBuildOptions;
 import org.lemsml.jlems.core.api.interfaces.ILEMSBuilder;
 import org.lemsml.jlems.core.api.interfaces.ILEMSDocument;
 import org.lemsml.jlems.core.api.interfaces.ILEMSResultsContainer;
 import org.lemsml.jlems.core.api.interfaces.ILEMSRunConfiguration;
 import org.lemsml.jlems.core.api.interfaces.ILEMSSimulator;
 import org.lemsml.jlems.core.api.interfaces.ILEMSStateInstance;
 import org.lemsml.jlems.core.api.interfaces.IStateIdentifier;
 import org.lemsml.jlems.core.expression.ParseError;
 import org.lemsml.jlems.core.sim.ContentError;
 import org.springframework.stereotype.Service;
 
 /**
  * @author matteocantarelli
  * 
  */
 @Service
 public class JLEMSSimulatorService extends ASimulator
 {
 
 	private static Log logger = LogFactory.getLog(JLEMSSimulatorService.class);
 	private ILEMSSimulator _simulator = null;
 	private StateTreeRoot _stateTree = null;
 	private int _step = 0;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geppetto.core.simulator.ASimulator#initialize(org.geppetto.core.model.IModel, org.geppetto.core.simulation.ISimulatorCallbackListener)
 	 */
 	@Override
	public void initialize(IModel model, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
 	{
 		super.initialize(model, listener);
 		try
 		{
 			ILEMSBuilder builder = new LEMSBuilder();
 			ILEMSDocument lemsDocument = (ILEMSDocument) ((ModelWrapper) model).getModel("lems");
 
 			builder.addDocument(lemsDocument);
 
 			ILEMSBuildOptions options = new LEMSBuildOptions();
 			options.addBuildOption(LEMSBuildOptionsEnum.FLATTEN);
 
 			ILEMSBuildConfiguration config = new LEMSBuildConfiguration();
 			builder.build(config, options); // pre-build to read the run configuration and target from the file
 
 			ILEMSRunConfiguration runConfig = LEMSDocumentReader.getLEMSRunConfiguration(lemsDocument);
 			config = new LEMSBuildConfiguration(LEMSDocumentReader.getTarget(lemsDocument));
 			Collection<ILEMSStateInstance> stateInstances = builder.build(config, options); // real build for our specific target
 
 			_simulator = new LEMSSimulator();
 			for(ILEMSStateInstance instance : stateInstances)
 			{
 				_simulator.initialize(instance, runConfig);
 			}
 
 		}
 		catch(LEMSBuildException e)
 		{
 			throw new GeppettoInitializationException(e);
 		}
 		catch(LEMSExecutionException e)
 		{
 			throw new GeppettoInitializationException(e);
 		}
 		catch(ContentError e)
 		{
 			throw new GeppettoInitializationException(e);
 		}
 		catch(ParseError e)
 		{
 			throw new GeppettoInitializationException(e);
 		}
 		logger.info("jLEMS Simulator initialized");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.geppetto.core.simulator.ISimulator#simulate(org.geppetto.core.simulation.IRunConfiguration)
 	 */
 	@Override
 	public void simulate(IRunConfiguration runConfiguration) throws GeppettoExecutionException
 	{
 		ILEMSResultsContainer results = new LEMSResultsContainer();
 		try
 		{
 			_simulator.advance(results);
 			_step++;
 		}
 		catch(LEMSExecutionException e)
 		{
 			throw new GeppettoExecutionException(e);
 		}
 		getListener().stateTreeUpdated(getGeppettoStateTree(results));
 		//For Debugging purposes
 		// if(_step==50)
 		// {
 		// int x=0;
 		// for(ALEMSValue value:results.getStateValues(new StateIdentifier("hhpop[0]/v")))
 		// {
 		// LEMSDoubleValue dv=(LEMSDoubleValue) value;
 		// logger.error("X:"+x*0.01+" Y:"+dv.getAsDouble());
 		// x++;
 		// }
 		// }
 	}
 
 	/**
 	 * @param results
 	 * @return
 	 */
 	private StateTreeRoot getGeppettoStateTree(ILEMSResultsContainer results)
 	{
 		if(_stateTree == null)
 		{
 			_stateTree = new StateTreeRoot(_model.getId());
 		}
 		if(_stateTree.getChildren().isEmpty())
 		{
 			for(IStateIdentifier state : results.getStates().keySet())
 			{
 				// for every state found in the results add a node in the tree
 				StringTokenizer tokenizer = new StringTokenizer(state.getStatePath(), "/");
 				CompositeStateNode node = _stateTree;
 				while(tokenizer.hasMoreElements())
 				{
 					String current = tokenizer.nextToken();
 					boolean found = false;
 					for(AStateNode child : node.getChildren())
 					{
 						if(child.getName().equals(current))
 						{
 							if(child instanceof CompositeStateNode)
 							{
 								node = (CompositeStateNode) child;
 							}
 							found = true;
 							break;
 						}
 					}
 					if(found)
 					{
 						continue;
 					}
 					else
 					{
 						if(tokenizer.hasMoreElements())
 						{
 							// not a leaf, create a composite state node
 							CompositeStateNode newNode = new CompositeStateNode(current);
 							node.addChild(newNode);
 							node = newNode;
 						}
 						else
 						{
 							// it's a leaf node
 							SimpleStateNode newNode = new SimpleStateNode(current);
 							ALEMSValue lemsValue = results.getStates().get(state).get(results.getStates().get(state).size() - 1);
 							if(lemsValue instanceof LEMSDoubleValue)
 							{
 								newNode.addValue(ValuesFactory.getDoubleValue(((LEMSDoubleValue) lemsValue).getAsDouble()));
 							}
 							node.addChild(newNode);
 						}
 					}
 				}
 			}
 		}
 		else
 		{
 			UpdateLEMSStateTreeVisitor updateStateTreeVisitor = new UpdateLEMSStateTreeVisitor(results);
 			_stateTree.apply(updateStateTreeVisitor);
 		}
 		return _stateTree;
 	}
 
 }
