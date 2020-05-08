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
 
 import ptolemy.kernel.CompositeEntity;
 import ptolemy.kernel.util.Settable;
 
 import com.isencia.passerelle.actor.ProcessingException;
 
 import fr.esrf.TangoApi.DeviceAttribute;
 
 /**
  * This actor sets or gets a motor value, if set then value should be set
  * (expands allowed) and if get then motor name should be set only.
  * 
  * @author gerring
  *
  */
 public class TangoMotorTransformer extends AbstractDataMessageTransformer {
 	
 	private static final Logger logger = LoggerFactory.getLogger(TangoMotorTransformer.class);
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -5335673681296652024L;
 
 	private MotorParameter motorsParam;
 
 	public TangoMotorTransformer(CompositeEntity container, String name) throws Exception {
 		
 		super(container, name);
 		
 		motorsParam  = new MotorParameter(this,"Motors");
 		registerConfigurableParameter(motorsParam);
 		
 		memoryManagementParam.setVisibility(Settable.NONE);
 		dataSetNaming.setVisibility(Settable.NONE);
 	}
 
 	protected int getMinimumCacheSize() {
 		return 0;
 	}
 	
 	@Override
 	protected DataMessageComponent getTransformedMessage(List<DataMessageComponent> cache) throws ProcessingException {
 		
 		try {
 			final MotorContainer cont = (MotorContainer)motorsParam.getBeanFromValue(MotorContainer.class);
 	    	final DataMessageComponent comp = MessageUtils.mergeAll(cache);
 		    
 	    	if (cont == null) {
 		    	comp.putScalar("error_messsage", "No motors configured, '"+getName()+"' did nothing.");
 		    	return comp;
 		    }
 		    
 		    final Map<String,String> values = setMotors(cont, comp);
 	    	if (values!=null) comp.addScalar(values);
 		
 	        return comp;
 	        
 		} catch (Exception e) {
 			throw createDataMessageException("Cannot set motor values!", e);
 		}
 	    
 	}
 
 	private Map<String, String> setMotors(final MotorContainer cont, final DataMessageComponent comp) throws DataMessageException {
 	
 		final Map<String,String> ret = new HashMap<String,String>(cont.size());
 		for (MotorBean mb : cont.getExpressions()) {
 			
 			final String motorPath = SubstituteUtils.substitute(mb.getMotorName(), comp.getScalar());
 			final String baseUri   = TangoUtils.getHardwareAddress(motorPath);
 			String attribute       = mb.getAttributeName();
 			if (attribute==null||"".equals(attribute.trim())) attribute="Position";
 			
 			DeviceAttribute value = null;
 			if (!mb.isReadOnly()) {
 				try {
 					final double dbl = ExpressionUtils.evaluateExpression(mb.getExpression(), comp.getScalar());
 					if (Double.isNaN(dbl) || Double.isInfinite(dbl)) throw new Exception();
 					value = new DeviceAttribute(attribute, dbl);
 					
 				} catch (Exception e) {
 					final String exp = SubstituteUtils.substitute(mb.getExpression(), comp.getScalar());
 					value = new DeviceAttribute(attribute, exp);
 				}
 			}
 			
 			try {
 				final TangoConnection connection = TangoConnectionFactory.openConnection(baseUri, attribute);
 				if (value!=null) {
 					connection.setValue(value);	// Uses remote call for MockMode				
 				}
 				try {				
 					value = connection.getValue();
 				} catch (Exception ne) {
 					logger.debug("Error in TangoMotorTransformer "+getName());
 					logger.debug("Error message: "+ne.getMessage());
 					logger.debug(ne.getStackTrace().toString());
 					value = new DeviceAttribute(attribute, 0.0);
 				}
 				try {
 					final String dblString = String.valueOf(value.extractDouble());
 					ret.put(connection.getName(), dblString);
 				} catch (Exception ne) {
 					ret.put(connection.getName(), String.valueOf(value.extractString()));
 				}
 				
 			} catch (Exception e) {
 				throw createDataMessageException("Cannot connect to "+baseUri, e);
 			}
 			
 			
 		}
 		
 		return ret;
 	}
 
 	@Override
 	protected String getOperationName() {
 		return "Get/Set Motor";
 	}
 
 	@Override
 	protected String getExtendedInfo() {
 		return "Actor to set and get motor";
 	}
 	
 	@Override
 	public List<IVariable> getOutputVariables() {
 		
 		try {
 		    final List<IVariable>    ret  = super.getOutputVariables();
 			final MotorContainer cont = (MotorContainer)motorsParam.getBeanFromValue(MotorContainer.class);
 			if (cont==null || cont.isEmpty()) return ret;
 			
 			for (MotorBean mb : cont.getExpressions()) {
 			    ret.add(new Variable(mb.getMotorName().substring(mb.getMotorName().lastIndexOf('/')+1), VARIABLE_TYPE.SCALAR, mb.getExpression(), String.class));
 			}
 			
 			return ret;
 			
 		} catch (Exception e) {
 			logger.error("Cannot read variables", e);
 			return null;
 		}
 
 	}
 	
 }
