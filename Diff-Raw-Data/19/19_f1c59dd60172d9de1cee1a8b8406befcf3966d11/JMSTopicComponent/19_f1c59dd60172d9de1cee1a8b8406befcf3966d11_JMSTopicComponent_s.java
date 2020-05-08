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
 import org.jboss.metatype.api.types.SimpleMetaType;
 import org.jboss.metatype.api.values.SimpleValueSupport;
 import org.rhq.core.domain.configuration.*;
 import org.rhq.core.domain.measurement.AvailabilityType;
 import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
  *         Created: 17-Mar-2010
  */
 public class JMSTopicComponent extends JMSResourceComponent
 {
    public AvailabilityType getAvailability()
    {
       return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
 
    @Override
    String getComponentName()
    {
       return JMSConstants.Topic.COMPONENT_NAME;
    }
 
    @Override
    ComponentType getComponentType()
    {
       return JMSConstants.Topic.COMPONENT_TYPE;
    }
 
    @Override
    String getConfigurationOperationName()
    {
       return "getTopicConfiguration";
    }
 
    @Override
   String getMeasurementsOperationName()
   {
      return "getTopicMeasurements";
   }
 
 
    @Override
    protected String getInvokeOperation()
    {
       return "invokeTopicOperation";
    }
 
    @Override
    protected String getInvokeOperationJMSMessage()
    {
       return "invokeTopicOperationMessageType";
    }
 
    @Override
    protected String getInvokeOperationSubscriptionMessage()
    {
       return "invokeTopicOperationSubscriptionType";
    }
 
    @Override
    String getDeleteOperationName()
    {
       return "deleteTopic";
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
       StringBuffer createNonDurableRoles = new StringBuffer();
       StringBuffer deleteNonDurableRoles = new StringBuffer();
       StringBuffer createDurableRoles = new StringBuffer();
       StringBuffer deleteDurableRoles = new StringBuffer();
       createRoles(configurationUpdateReport, name, sendRoles, consumeRoles, createNonDurableRoles, deleteNonDurableRoles, createDurableRoles, deleteDurableRoles);
       try
       {
          ManagedOperation operation = ManagementSupport.getOperation(view, JMSConstants.Topic.COMPONENT_NAME,
                "updateTopicConfiguration", JMSConstants.Topic.COMPONENT_TYPE);
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
              new SimpleValueSupport(SimpleMetaType.STRING, consumeRoles.toString()),
              new SimpleValueSupport(SimpleMetaType.STRING, createDurableRoles.toString()),
              new SimpleValueSupport(SimpleMetaType.STRING, deleteDurableRoles.toString()),
              new SimpleValueSupport(SimpleMetaType.STRING, createNonDurableRoles.toString()),
              new SimpleValueSupport(SimpleMetaType.STRING, deleteNonDurableRoles.toString()));
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
 
    private void createRoles(ConfigurationUpdateReport configurationUpdateReport, String name, StringBuffer sendRoles, StringBuffer consumeRoles, StringBuffer createNonDurableRoles, StringBuffer deleteNonDurableRoles, StringBuffer createDurableRoles, StringBuffer deleteDurableRoles)
    {
      PropertyList propertyList = (PropertyList) configurationUpdateReport.getConfiguration().get("securityConfig");
       List<Property> roles = propertyList.getList();
       for (Property role : roles)
       {
          PropertyMap actRole = (PropertyMap) role;
          boolean send;
          boolean consume;
          boolean createNonDurableQueue;
          boolean deleteNonDurableQueue;
          boolean createDurableQueue;
          boolean deleteDurableQueue;
          PropertySimple simple = (PropertySimple) actRole.get("name");
          name = simple.getStringValue();
          simple = (PropertySimple) actRole.get("send");
          send = simple.getBooleanValue();
          simple = (PropertySimple) actRole.get("consume");
          consume = simple.getBooleanValue();
          simple = (PropertySimple) actRole.get("createNonDurableQueue");
          createNonDurableQueue = simple.getBooleanValue();
          simple = (PropertySimple) actRole.get("deleteNonDurableQueue");
          deleteNonDurableQueue = simple.getBooleanValue();
          simple = (PropertySimple) actRole.get("createDurableQueue");
          createDurableQueue = simple.getBooleanValue();
          simple = (PropertySimple) actRole.get("deleteDurableQueue");
          deleteDurableQueue = simple.getBooleanValue();
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
          if(createDurableQueue)
          {
             if(createDurableRoles.length() > 0)
             {
                createDurableRoles.append(",");
             }
             createDurableRoles.append(name);
          }
          if(deleteDurableQueue)
          {
             if(deleteDurableRoles.length() > 0)
             {
                deleteDurableRoles.append(",");
             }
             deleteDurableRoles.append(name);
          }
          if(createNonDurableQueue)
          {
             if(createNonDurableRoles.length() > 0)
             {
                createNonDurableRoles.append(",");
             }
             createNonDurableRoles.append(name);
          }
          if(deleteNonDurableQueue)
          {
             if(deleteNonDurableRoles.length() > 0)
             {
                deleteNonDurableRoles.append(",");
             }
             deleteNonDurableRoles.append(name);
          }
       }
    }
 
 }
