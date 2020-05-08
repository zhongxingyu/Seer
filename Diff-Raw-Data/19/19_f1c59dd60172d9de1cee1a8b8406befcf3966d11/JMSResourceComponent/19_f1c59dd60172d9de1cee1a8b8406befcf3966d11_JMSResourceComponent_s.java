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
 import org.jboss.as.integration.hornetq.jopr.util.Operation;
 import org.jboss.deployers.spi.management.ManagementView;
 import org.jboss.managed.api.ComponentType;
 import org.jboss.managed.api.ManagedOperation;
 import org.jboss.metatype.api.types.ArrayMetaType;
 import org.jboss.metatype.api.types.ImmutableCompositeMetaType;
 import org.jboss.metatype.api.types.SimpleMetaType;
 import org.jboss.metatype.api.values.*;
 import org.rhq.core.domain.configuration.Configuration;
 import org.rhq.core.domain.configuration.PropertyList;
 import org.rhq.core.domain.configuration.PropertySimple;
 import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
 import org.rhq.core.domain.configuration.definition.PropertyDefinition;
 import org.rhq.core.domain.measurement.*;
 import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
 import org.rhq.core.pluginapi.inventory.DeleteResourceFacet;
 import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
 import org.rhq.core.pluginapi.inventory.ResourceComponent;
 import org.rhq.core.pluginapi.inventory.ResourceContext;
 import org.rhq.core.pluginapi.measurement.MeasurementFacet;
 import org.rhq.core.pluginapi.operation.OperationFacet;
 import org.rhq.core.pluginapi.operation.OperationResult;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
  *         Created: 17-Mar-2010
  */
 public abstract class JMSResourceComponent implements ResourceComponent, MeasurementFacet, OperationFacet, ConfigurationFacet, JMSComponent, DeleteResourceFacet
 {
    protected ResourceContext resourceContext;
    private JMSComponent jmsComponent;
 
    public AvailabilityType getAvailability()
    {
       return AvailabilityType.UP;
    }
 
    public void deleteResource() throws Exception
    {
       ManagementView view = getProfileService();
       ManagedOperation operation = ManagementSupport.getOperation(view, getComponentName(), getDeleteOperationName(), getComponentType());
       operation.invoke(new SimpleValueSupport(SimpleMetaType.STRING, resourceContext.getResourceKey()));
    }
 
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> measurementScheduleRequests) throws Exception
    {
       ArrayValueSupport support = new ArrayValueSupport(new ArrayMetaType(SimpleMetaType.STRING, false));
       SimpleValueSupport[] valueSupports = new SimpleValueSupport[measurementScheduleRequests.size()];
       Iterator<MeasurementScheduleRequest> it = measurementScheduleRequests.iterator();
       for (int i = 0, valueSupportsLength = valueSupports.length; i < valueSupportsLength; i++)
       {
          valueSupports[i] = new SimpleValueSupport(SimpleMetaType.STRING, it.next().getName());
       }
       support.setValue(valueSupports);
       ManagementView view = getProfileService();
 
       ManagedOperation operation = ManagementSupport.getOperation(view, getComponentName(), getMeasurementsOperationName(), getComponentType());
       ArrayValueSupport vals = (ArrayValueSupport) operation.invoke(new SimpleValueSupport(SimpleMetaType.STRING, resourceContext.getResourceKey()), support);
       it = measurementScheduleRequests.iterator();
       for (int i = 0, valueSupportsLength = valueSupports.length; i < valueSupportsLength; i++)
       {
          MeasurementScheduleRequest request = it.next();
          SimpleValueSupport simpleValueSupport = (SimpleValueSupport) vals.getValue(i);
          simpleValueSupport.getValue();
          if (request.getDataType().equals(DataType.MEASUREMENT))
          {
             report.addData(new MeasurementDataNumeric(request, Double.valueOf(simpleValueSupport.getValue().toString())));
          }
          else if (request.getDataType().equals(DataType.TRAIT))
          {
             report.addData(new MeasurementDataTrait(request, simpleValueSupport.getValue().toString()));
          }
       }
    }
 
    public Configuration loadResourceConfiguration() throws Exception
    {
       Configuration config = new Configuration();
       ManagementView view = getProfileService();
 
       ManagedOperation operation = ManagementSupport.getOperation(view, getComponentName(), getConfigurationOperationName(), getComponentType());
 
       CompositeValueSupport val = (CompositeValueSupport) operation.invoke(new SimpleValueSupport(SimpleMetaType.STRING, resourceContext.getResourceKey()));
 
       ConfigurationDefinition configDef = resourceContext.getResourceType().getResourceConfigurationDefinition();
       List<PropertyDefinition> propertyDefinitionList = configDef.getPropertiesInGroup("HornetQCustomProperties");
       for (PropertyDefinition definition : propertyDefinitionList)
       {
          String name = definition.getName();
          if (val.containsKey(name))
          {
             MetaValue mv = val.get(name);
             if(mv instanceof SimpleValueSupport)
             {
                SimpleValueSupport attr = (SimpleValueSupport) mv;
                PropertySimple simple = new PropertySimple(name, attr.getValue());
                config.put(simple);
             }
             else if(mv instanceof CollectionValueSupport)
             {
               PropertyList property = new PropertyList("securityConfig");
                CollectionValueSupport valueSupport = (CollectionValueSupport) mv;
                MetaValue[] msgs =  valueSupport.getElements();
                for (MetaValue mv2 : msgs)
                {
                   CompositeValueSupport msg = (CompositeValueSupport) mv2;
                  org.rhq.core.domain.configuration.PropertyMap p1 = new org.rhq.core.domain.configuration.PropertyMap("element");
                   property.add(p1);
                   ImmutableCompositeMetaType metaType = (ImmutableCompositeMetaType) msg.getMetaType();
                   Set<String> keys = metaType.keySet();
                   for (String key : keys)
                   {
                      SimpleValueSupport sattr = (SimpleValueSupport) msg.get(key);
                      if(sattr != null)
                         p1.put(new PropertySimple(key,sattr.getValue()));
                   }
                }
                config.put(property);
             }
          }
       }
       return config;
    }
 
    public OperationResult invokeOperation(String s, Configuration configuration) throws InterruptedException, Exception
    {
       Operation oper = Operation.getOperation(s);
       Collection<PropertySimple> props = configuration.getSimpleProperties().values();
       SimpleValueSupport[] params = new SimpleValueSupport[props.size()];
       SimpleValueSupport[] signature = new SimpleValueSupport[props.size()];
       populateParams(props, params, signature);
       ArrayValueSupport param = new ArrayValueSupport(new ArrayMetaType(SimpleMetaType.STRING, false));
       param.setValue(params);
       ArrayValueSupport sig = new ArrayValueSupport(new ArrayMetaType(SimpleMetaType.STRING, false));
       sig.setValue(signature);
       SimpleValueSupport queueName = new SimpleValueSupport(SimpleMetaType.STRING, resourceContext.getResourceKey());
       SimpleValueSupport methodName = new SimpleValueSupport(SimpleMetaType.STRING, oper.getOperationName());
       ManagementView view = getProfileService();
       String methodOperation = getInvokeOperation();
       if("JMSMessage".equalsIgnoreCase(oper.getResultsType()))
       {
          methodOperation = getInvokeOperationJMSMessage();
       }
       else if("SubscriptionInfo".equalsIgnoreCase(oper.getResultsType()))
       {
          methodOperation = getInvokeOperationSubscriptionMessage();
       }
       ManagedOperation operation = ManagementSupport.getOperation(view, getComponentName(), methodOperation, getComponentType());
       Object result = null;
       result = operation.invoke(queueName, methodName, param, sig);
       if (result == null)
       {
          return null;
       }
       return formatResults(result, oper.getResultsType());
    }
 
    public void start(ResourceContext resourceContext) throws InvalidPluginConfigurationException, Exception
    {
       this.resourceContext = resourceContext;
 
       jmsComponent = (JMSComponent) resourceContext.getParentResourceComponent();
    }
 
    public void stop()
    {
       this.resourceContext = null;
    }
    
    protected abstract String getInvokeOperationSubscriptionMessage();
 
    protected abstract String getInvokeOperationJMSMessage();
 
    protected abstract String getInvokeOperation();
 
 
    abstract String getComponentName();
 
    abstract ComponentType getComponentType();
 
    abstract String getConfigurationOperationName();
 
    abstract String getMeasurementsOperationName();
 
    abstract String getDeleteOperationName();
 
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
       else if(type.equalsIgnoreCase("JMSMessage") || type.equalsIgnoreCase("SubscriptionInfo"))
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
                if(sattr != null)
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
 
    public ManagementView getProfileService() throws Exception
    {
       return jmsComponent.getProfileService();
    }
 }
