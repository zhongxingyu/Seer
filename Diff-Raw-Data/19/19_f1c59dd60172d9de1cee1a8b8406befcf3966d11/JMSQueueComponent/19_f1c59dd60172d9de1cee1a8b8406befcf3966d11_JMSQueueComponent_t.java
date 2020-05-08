 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.as.integration.hornetq.jopr;
 
 import org.jboss.as.integration.hornetq.jopr.util.ManagementSupport;
 import org.jboss.deployers.spi.management.ManagementView;
 import org.jboss.managed.api.ComponentType;
 import org.jboss.managed.api.ManagedOperation;
 import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
 import org.jboss.metatype.api.types.SimpleMetaType;
 import org.jboss.metatype.api.values.CollectionValueSupport;
 import org.jboss.metatype.api.values.CompositeValueSupport;
 import org.jboss.metatype.api.values.MetaValue;
 import org.jboss.metatype.api.values.SimpleValueSupport;
 import org.rhq.core.domain.configuration.*;
 import org.rhq.core.domain.measurement.AvailabilityType;
 import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
 import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
 import org.rhq.core.pluginapi.inventory.ResourceComponent;
 import org.rhq.core.pluginapi.measurement.MeasurementFacet;
 import org.rhq.core.pluginapi.operation.OperationFacet;
 import org.rhq.core.pluginapi.operation.OperationResult;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static org.jboss.as.integration.hornetq.jopr.JMSConstants.Queue.COMPONENT_NAME;
 
 /**
  * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
  *         Created: 17-Mar-2010
  */
 public class JMSQueueComponent extends JMSResourceComponent implements ResourceComponent, MeasurementFacet, OperationFacet, ConfigurationFacet
 {
    @Override
    String getMeasurementsOperationName()
    {
       return "getQueueMeasurements";
    }
 
    public AvailabilityType getAvailability()
    {
       try
       {
          ManagementView view = getProfileService();
          ManagedOperation operation = ManagementSupport.getOperation(view, getComponentName(), "isPaused", getComponentType());
          SimpleValueSupport val = (SimpleValueSupport) operation.invoke(new SimpleValueSupport(SimpleMetaType.STRING, resourceContext.getResourceKey()));
          boolean paused = ((Boolean)val.getValue()).booleanValue();
          return paused ? AvailabilityType.DOWN : AvailabilityType.UP;
       }
       catch (Exception e)
       {
          return AvailabilityType.DOWN;
       }
    }
 
    @Override
    String getComponentName()
    {
       return COMPONENT_NAME;
    }
 
    @Override
    ComponentType getComponentType()
    {
       return JMSConstants.Queue.COMPONENT_TYPE;
    }
 
    @Override
    String getConfigurationOperationName()
    {
       return "getQueueConfiguration";
    }
 
    @Override
    protected String getInvokeOperation()
    {
       return "invokeQueueOperation";
    }
 
    @Override
    protected String getInvokeOperationJMSMessage()
    {
       return "invokeQueueOperationMessageType";
    }
 
    @Override
    protected String getInvokeOperationSubscriptionMessage()
    {
       return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
 
    @Override
    String getDeleteOperationName()
    {
       return "deleteQueue";
    }
 
    public void updateResourceConfiguration(ConfigurationUpdateReport configurationUpdateReport)
    {
       configurationUpdateReport.setStatus(ConfigurationUpdateStatus.INPROGRESS);
 
       ManagementView view = null;
       try
       {
          view = getProfileService();
       }
       catch (Exception e)
       {
          configurationUpdateReport.setStatus(ConfigurationUpdateStatus.FAILURE);
          configurationUpdateReport.setErrorMessage(e.getMessage());
          e.printStackTrace();
          return;
       }
       Map<String, PropertySimple> simpleProps = configurationUpdateReport.getConfiguration().getSimpleProperties();
       String name = simpleProps.get("name").getStringValue();
       String jndiName = simpleProps.get("jndiBindings").getStringValue();
       String DLA = simpleProps.get("dla").getStringValue();
       String expiryAddress = simpleProps.get("expiryAddress").getStringValue();
       int maxSize = simpleProps.get("maxSize").getIntegerValue();
       int pageSize = simpleProps.get("pageSize").getIntegerValue();
       int maxDeliveryAttempts = simpleProps.get("maxDeliveryAttempts").getIntegerValue();
       long redeliveryDelay = simpleProps.get("redeliveryDelay").getLongValue();
       boolean lastValueQueue = simpleProps.get("lastValueQueue").getBooleanValue();
       long redistributionDelay = simpleProps.get("redistributionDelay").getLongValue();
       boolean sendToDLAOnNoRoute = simpleProps.get("sendToDLAOnNoRoute").getBooleanValue();
       String addressFullMessagePolicy = simpleProps.get("addressFullMessagePolicy").getStringValue();
 
 
       StringBuffer sendRoles = new StringBuffer();
       StringBuffer consumeRoles = new StringBuffer();
       createRoles(configurationUpdateReport, name, sendRoles, consumeRoles);
       try
       {
          ManagedOperation operation = ManagementSupport.getOperation(view, JMSConstants.Queue.COMPONENT_NAME, "updateQueueConfiguration", JMSConstants.Queue.COMPONENT_TYPE);
          operation.invoke(new SimpleValueSupport(SimpleMetaType.STRING, resourceContext.getResourceKey()),
              new SimpleValueSupport(SimpleMetaType.STRING, jndiName),
              new SimpleValueSupport(SimpleMetaType.STRING, DLA),
              new SimpleValueSupport(SimpleMetaType.STRING, expiryAddress),
              new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, maxSize),
              new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, pageSize),
              new SimpleValueSupport(SimpleMetaType.INTEGER_PRIMITIVE, maxDeliveryAttempts),
              new SimpleValueSupport(SimpleMetaType.LONG_PRIMITIVE, redeliveryDelay),
              new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, lastValueQueue),
              new SimpleValueSupport(SimpleMetaType.LONG_PRIMITIVE, redistributionDelay),
              new SimpleValueSupport(SimpleMetaType.BOOLEAN_PRIMITIVE, sendToDLAOnNoRoute),
              new SimpleValueSupport(SimpleMetaType.STRING, addressFullMessagePolicy),
              new SimpleValueSupport(SimpleMetaType.STRING, sendRoles.toString()),
              new SimpleValueSupport(SimpleMetaType.STRING, consumeRoles.toString()));
       }
       catch (Exception e)
       {
          configurationUpdateReport.setStatus(ConfigurationUpdateStatus.FAILURE);
          configurationUpdateReport.setErrorMessage(e.getMessage());
          e.printStackTrace();
          return;
       }
       configurationUpdateReport.setStatus(ConfigurationUpdateStatus.SUCCESS);
    }
 
    private void createRoles(ConfigurationUpdateReport configurationUpdateReport, String name, StringBuffer sendRoles, StringBuffer consumeRoles)
    {
      PropertyList propertyList = (PropertyList) configurationUpdateReport.getConfiguration().get("roles");
       if(propertyList == null)
       {
          return;
       }
       List<Property> roles = propertyList.getList();
       for (Property role : roles)
       {
          PropertyMap actRole = (PropertyMap) role;
          boolean send;
          boolean consume;
          PropertySimple simple = (PropertySimple) actRole.get("name");
          name = simple.getStringValue();
          simple = (PropertySimple) actRole.get("send");
          send = simple.getBooleanValue();
          simple = (PropertySimple) actRole.get("consume");
          consume = simple.getBooleanValue();
          if(send)
          {
             if(sendRoles.length() > 0)
             {
                sendRoles.append(",");
             }
             sendRoles.append(name);
          }
          if(consume)
          {
             if(consumeRoles.length() > 0)
             {
                consumeRoles.append(",");
             }
             consumeRoles.append(name);
          }
       }
    }
 
    private void populateParams(final Collection<PropertySimple> props, final SimpleValueSupport[] params, final SimpleValueSupport[] signature)
    {
       int pos = 0;
       for (PropertySimple prop : props)
       {
          String[] val = prop.getName().split(":");
          if (val.length == 1)
          {
             params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getStringValue()));
             signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "java.lang.String");
          }
          else
          {
             if (val[0].equals("Boolean"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getBooleanValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "java.lang.Boolean");
             }
             else if (val[0].equals("boolean"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getBooleanValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "boolean");
             }
             else if (val[0].equals("String"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getStringValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "java.lang.String");
             }
             else if (val[0].equals("Long"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getLongValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "java.lang.Long");
             }
             else if (val[0].equals("long"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getLongValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "long");
             }
             else if (val[0].equals("Integer"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getIntegerValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "java.lang.Integer");
             }
             else if (val[0].equals("int"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getIntegerValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "int");
             }
             else if (val[0].equals("Double"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getDoubleValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "java.lang.Double");
             }
             else if (val[0].equals("double"))
             {
                params[pos] = new SimpleValueSupport(SimpleMetaType.STRING, getStringValue(prop.getDoubleValue()));
                signature[pos] = new SimpleValueSupport(SimpleMetaType.STRING, "double");
             }
          }
          pos++;
       }
    }
 
    private String getStringValue(Object o)
    {
       return o == null ? "null" : o.toString();
    }
 
 
    private OperationResult formatResults(Object val, String type) throws Exception
    {
       if (type == null)
       {
          SimpleValueSupport valueSupport = (SimpleValueSupport) val;
          return new OperationResult(valueSupport.getValue().toString());
       }
       else if(type.equalsIgnoreCase("String"))
       {
          SimpleValueSupport valueSupport = (SimpleValueSupport) val;
          return new OperationResult(valueSupport.getValue().toString());
       }
       else if(type.equalsIgnoreCase("JMSMessage"))
       {
          OperationResult operationResult = new OperationResult();
          Configuration c = operationResult.getComplexResults();
          PropertyList property = new PropertyList("result");
          CollectionValueSupport valueSupport = (CollectionValueSupport) val;
          MetaValue[] msgs =  valueSupport.getElements();
          for (MetaValue mv : msgs)
          {
             CompositeValueSupport msg = (CompositeValueSupport) mv;
             org.rhq.core.domain.configuration.PropertyMap p1 = new org.rhq.core.domain.configuration.PropertyMap("element");
             property.add(p1);
             ImmutableCompositeMetaType metaType = (ImmutableCompositeMetaType) msg.getMetaType();
             Set<String> keys = metaType.keySet();
             for (String key : keys)
             {
                SimpleValueSupport sattr = (SimpleValueSupport) msg.get(key);
                p1.put(new PropertySimple(key,sattr.getValue()));
             }
          }
          c.put(property);
          return operationResult;
       }
       else if (val instanceof CompositeValueSupport)
       {
          CompositeValueSupport valueSupport = (CompositeValueSupport) val;
          if (valueSupport.containsKey("cause"))
          {
             CompositeValueSupport cause = (CompositeValueSupport) valueSupport.get("cause");
             SimpleValueSupport message = (SimpleValueSupport) cause.get("message");
             Exception exception = new Exception(message.toString());
             throw exception;
          }
          return new OperationResult("not yet");
       }
       return new OperationResult("not yet");
    }
 }
