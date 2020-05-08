 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.passerelle.actors.hardware;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.management.MBeanServerConnection;
 
 import org.dawb.common.util.ExpressionUtils;
import org.dawb.common.util.SubstituteUtils;
 import org.dawb.passerelle.actors.hardware.motor.MotorBean;
 import org.dawb.passerelle.actors.hardware.motor.MotorContainer;
 import org.dawb.passerelle.actors.hardware.motor.MotorParameter;
 import org.dawb.passerelle.common.actors.AbstractDataMessageTransformer;
 import org.dawb.passerelle.common.message.DataMessageComponent;
 import org.dawb.passerelle.common.message.DataMessageException;
 import org.dawb.passerelle.common.message.IVariable;
 import org.dawb.passerelle.common.message.IVariable.VARIABLE_TYPE;
 import org.dawb.passerelle.common.message.MessageUtils;
 import org.dawb.passerelle.common.message.Variable;
 import org.dawb.tango.extensions.TangoUtils;
 import org.dawb.tango.extensions.factory.TangoConnection;
 import org.dawb.tango.extensions.factory.TangoConnectionFactory;
 import org.dawb.workbench.jmx.RemoteWorkbenchAgent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ptolemy.data.BooleanToken;
 import ptolemy.data.expr.Parameter;
 import ptolemy.data.expr.StringParameter;
 import ptolemy.kernel.CompositeEntity;
 import ptolemy.kernel.util.Attribute;
 import ptolemy.kernel.util.IllegalActionException;
 import ptolemy.kernel.util.Settable;
 
 import com.isencia.passerelle.actor.ProcessingException;
 import com.isencia.passerelle.util.ptolemy.StringChoiceParameter;
 
 import fr.esrf.TangoApi.DeviceAttribute;
 import org.eclipse.swt.SWT;
 
 /**
  * This actor sets or gets the TANGO mock mode. It has four different modes
  * of operation:
  * - Read-only the value of the TANGO mock mode
  * - Set the TANGO mock mode according to an incoming scalar
  * - Force the TANGO mock mode to be true
  * - Force the TANGO mock mode to be false
  * 
  * 
  * @author svensson
  *
  */
 public class TangoMockModeTransformer extends AbstractDataMessageTransformer {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2405810900325470900L;
 	private static final Logger logger = LoggerFactory.getLogger(TangoMockModeTransformer.class);
 
 	protected static final List<String> ACTOR_MODE;
 	static {
 		ACTOR_MODE = new ArrayList<String>(4);
 		ACTOR_MODE.add("TANGO Mock Mode variable read-only");
 		ACTOR_MODE.add("TANGO Mock Mode set by variable");
 		ACTOR_MODE.add("TANGO Mock Mode forced to true");
 		ACTOR_MODE.add("TANGO Mock Mode forced to false");
 	}
 
 	private final Parameter				actorModeParameter;
 	private final StringParameter  		tangoMockModeVariableNameParam;
 	private final Parameter             passInputsParameter;
 
 	private String 						actorMode = ACTOR_MODE.get(0);
 	private String                      tangoMockModeVariableName = "tango_mock_mode";
 	private boolean                     isPassInputs = true;
 	
 
 	public TangoMockModeTransformer(CompositeEntity container, String name) throws Exception {
 		
 		super(container, name);
 		
 		actorModeParameter = new StringChoiceParameter(this, "Actor Mode", ACTOR_MODE, SWT.SINGLE);
 		registerConfigurableParameter(actorModeParameter);
 		actorModeParameter.setExpression(ACTOR_MODE.get(0));
 		
 		tangoMockModeVariableNameParam  = new StringParameter(this, "TANGO Mock Mode Variable Name");
 		registerConfigurableParameter(tangoMockModeVariableNameParam);
 		tangoMockModeVariableNameParam.setExpression(tangoMockModeVariableName);
 
 		passInputsParameter = new Parameter(this,"Pass Inputs On",new BooleanToken(true));
 		registerConfigurableParameter(passInputsParameter);
 
 	}
 
 	
 	public void attributeChanged(Attribute attribute) throws IllegalActionException {
 		
 		if (attribute == actorModeParameter) {
 			actorMode = actorModeParameter.getExpression();
 		} else if (attribute == tangoMockModeVariableNameParam) {
 			tangoMockModeVariableName = tangoMockModeVariableNameParam.getExpression();
 		} else if (attribute == passInputsParameter) {
 			final BooleanToken b = (BooleanToken) passInputsParameter.getToken();
 			isPassInputs = b.booleanValue();
 		}		
 	    super.attributeChanged(attribute);
 	}
 
 	@Override
 	protected DataMessageComponent getTransformedMessage(List<DataMessageComponent> cache) throws ProcessingException {
 		
 		try {
 			String tangoMockModeScalar = null;
 			boolean tangoMockMode = TangoConnectionFactory.isMockMode();
 			logger.info("Actor \""+getName()+"\": initial TANGO mock mode set to "+tangoMockMode);
 			boolean tangoMockModeNew = tangoMockMode;
 			// Try to read upstream variable name tangoMockModeVariableName
 			for (DataMessageComponent dataMessageComponent : cache) {
 				if (dataMessageComponent.getScalar()!=null) {
 					for (String name : dataMessageComponent.getScalar().keySet()) {						
 						if (name.equals(tangoMockModeVariableName)) {
 							tangoMockModeScalar = dataMessageComponent.getScalar().get(name);
 							logger.info("Actor \""+getName()+"\": TANGO mock mode variable "+tangoMockModeVariableName+" set to "+tangoMockModeScalar);
 						}
 					}
 				}
 			}
 			// Action of actor depending on actorModeParameter
 			if (actorModeParameter.getExpression().equals(ACTOR_MODE.get(0))) {
 				// Just read the mock mode, i.e. do nothing here
 			} else if (actorModeParameter.getExpression().equals(ACTOR_MODE.get(1))) {
 				// Set the mock mode according to tangoMockModeVariableName
 				if (tangoMockModeScalar==null) {
 					throw createDataMessageException("Cannot set tango mock mode because "+tangoMockModeVariableName+" is not present in the incoming port!", null);					
 				} else {
 					if (tangoMockModeScalar.equals("true")) {
 						tangoMockModeNew = true;
 						logger.info("Actor \""+getName()+"\": TANGO mock mode set to be true according to "+tangoMockModeVariableName);
 					} else if (tangoMockModeScalar.equals("false")) {	
 						tangoMockModeNew = false;
 						logger.info("Actor \""+getName()+"\": TANGO mock mode set to be false according to "+tangoMockModeVariableName);
 					} else {
 						// Raise exception as the content of tangoMockModeVariableName is neither true nor false
 						throw createDataMessageException("Cannot set tango mock mode because "+tangoMockModeVariableName+" is set to "+tangoMockModeScalar+", it should be set to either true or false!", null);
 					}
 				}
 			} else if (actorModeParameter.getExpression().equals(ACTOR_MODE.get(2))) {
 				// Force mock mode to true
 				tangoMockModeNew = true;
 				logger.info("Actor \""+getName()+"\": TANGO mock mode forced to be true");
 			} else if (actorModeParameter.getExpression().equals(ACTOR_MODE.get(3))) {
 				// Force mock mode to false
 				tangoMockModeNew = false;
 				logger.info("Actor \""+getName()+"\": TANGO mock mode forced to be false");
 			}			
 			// Should the mock mode be changed?
 			if (tangoMockMode != tangoMockModeNew) {
 				logger.info("Actor \""+getName()+"\": TANGO mock mode changed to be "+tangoMockModeNew);
 				TangoConnectionFactory.setMockMode(tangoMockModeNew);
 			}
 			// Set the outgoing variable tangoMockModeVariableName
 			final DataMessageComponent despatch = new DataMessageComponent();
 			despatch.setMeta(MessageUtils.getMeta(cache));
 			if (isPassInputs) {
 				final Map<String,String> upStreamScalar = MessageUtils.getScalar(cache);
 			    despatch.addScalar(upStreamScalar);
 			}
 			despatch.putScalar(tangoMockModeVariableName, ""+tangoMockModeNew);
 
 			return despatch;
 
 		} catch (Exception e) {
 			throw createDataMessageException("Cannot set tango mock mode!", e);
 		}
 	    
 	}
 
 
 	@Override
 	protected String getOperationName() {
 		return "Get/Set TANGO mock mode";
 	}
 
 	@Override
 	protected String getExtendedInfo() {
 		return "Actor to set and get the TANGO mock mode";
 	}
 	
 	public List<IVariable> getInputVariables() {
 		
 		final List<IVariable> inputs = new ArrayList<IVariable>(7);
 		final List<String>    names  = new ArrayList<String>(7);
 		
 		final List<IVariable> vars = super.getInputVariables();				
 		for (IVariable input : vars) {
 			if (!names.contains(input.getVariableName())) {
 				inputs.add(input);
 				names.add(input.getVariableName());
 			}
 		}
 
 		return inputs;
 	}
 
 	/**
 	 */
 	public List<IVariable> getOutputVariables() {
 
 		final List<IVariable> ret;
 		if (isPassInputs) {
 			ret =  getInputVariables();
 		} else {
 			ret = new ArrayList<IVariable>(2);
 		}
 
 		return ret;
 	}
 	
 }
