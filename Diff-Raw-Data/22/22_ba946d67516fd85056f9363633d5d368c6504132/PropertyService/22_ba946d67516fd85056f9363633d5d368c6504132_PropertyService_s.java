 /**
  * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
  * If a copy of the MPL was not distributed with this file, You can obtain one at 
  * http://mozilla.org/MPL/2.0/.
  * 
  * This Source Code Form is also subject to the terms of the Health-Related Additional
  * Disclaimer of Warranty and Limitation of Liability available at
  * http://www.carewebframework.org/licensing/disclaimer.
  */
 package gov.ihs.cwf.property;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import gov.ihs.cwf.domain.DomainObjectFactory;
 import gov.ihs.cwf.mbroker.BrokerSession;
 
 import org.apache.commons.lang.StringUtils;
 
import org.carewebframework.api.AliasRegistry;
import org.carewebframework.api.AliasRegistry.AliasRegistryForType;
 import org.carewebframework.api.AliasRegistry.AliasType;
 import org.carewebframework.api.property.IPropertyService;
 import org.carewebframework.common.StrUtil;
 
 public class PropertyService implements IPropertyService, IPropertyDAO {
     
    private final AliasRegistryForType propertyAliases = AliasRegistry.getInstance(AliasType.PROPERTY);
    
     private final BrokerSession broker;
     
     public PropertyService(BrokerSession brokerSession) {
         this.broker = brokerSession;
     }
     
     private String getInstanceId(Property property) {
         return StringUtils.isEmpty(property.getInstanceId()) ? "1" : property.getInstanceId();
     }
     
     /**
      * Returns alias for name if it exists, or the original name if not.
      * 
      * @param propertyName Property name.
      * @return Alias if exists, original name if not.
      */
     private String toAlias(String propertyName) {
        return propertyAliases.contains(propertyName) ? propertyAliases.get(propertyName) : propertyName;
     }
     
     /**
      * Returns an instance of the specified property, observing property aliases.
      * 
      * @param propertyName Property name
      * @param instanceName Instance name
      * @param entity Entity list
      * @return Newly created property.
      */
     private Property newProperty(String propertyName, String instanceName, String entity) {
         return new Property(toAlias(propertyName), instanceName, entity);
     }
     
     @Override
     public void fetchValue(Property property, List<String> values) {
         if (property.isDefined()) {
             if ("W".equals(property.getDataType())) {
                 broker.callRPCList("CIAVMRPC GETPARWP", values, property.getName(), property.getEntityList(),
                     getInstanceId(property));
             } else if (property.isMultiValued() && "*".equals(property.getInstanceId())) {
                 broker.callRPCList("CIAVMRPC GETPARLI", values, property.getName(), property.getEntityList(),
                     property.getFormat());
             } else {
                 broker.callRPCList("CIAVMRPC GETPAR", values, property.getName(), property.getEntityList(),
                     getInstanceId(property), property.getFormat());
             }
         }
     }
     
     @Override
     public void saveValue(Property property, String entity) {
         if (property.isDefined()) {
             Object value = property.isEmpty() ? "@" : "W".equals(property.getDataType()) ? property.getValues() : property
                     .getValue();
             String result = broker.callRPC("CIAVMRPC SETPAR", property.getName(), value, entity == null ? "USR" : entity,
                 getInstanceId(property));
             
             if (result.contains(StrUtil.U)) {
                 throw new RuntimeException(result);
             }
         }
     }
     
     @Override
     public List<String> getInstances(String propertyName, String entity) {
         return broker.callRPCList("CIAVUTPR GETINST", null, toAlias(propertyName), entity);
     }
     
     @Override
     public List<String> getMatching(String prefix) {
         return broker.callRPCList("CIAVMRPC GETPARPF", null, prefix);
     }
     
     @Override
     public PropertyDefinition getDefinition(long id) {
         try {
             return DomainObjectFactory.get(PropertyDefinition.class, id);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     @Override
     public PropertyDefinition getDefinition(String propertyName) {
         try {
             return DomainObjectFactory.get(PropertyDefinition.class, toAlias(propertyName), "8989.51");
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     @Override
     public String getValue(String propertyName, String instanceName) {
         List<String> result = getValues(propertyName, instanceName);
         return result == null ? null : StrUtil.fromList(result);
     }
     
     @Override
     public List<String> getValues(String propertyName, String instanceName) {
         try {
             Property property = newProperty(propertyName, instanceName, "USR^SYS");
             List<String> result = new ArrayList<String>();
             fetchValue(property, result);
             return result;
         } catch (Exception e) {
             return null;
         }
     }
     
     @Override
     public void saveValue(String propertyName, String instanceName, boolean asGlobal, String value) {
         Property property = newProperty(propertyName, instanceName, null);
         property.setValue(value);
         saveValue(property, asGlobal ? "SYS" : "USR");
     }
     
     @Override
     public void saveValues(String propertyName, String instanceName, boolean asGlobal, List<String> values) {
         Property property = newProperty(propertyName, instanceName, null);
         property.setValues(values);
         saveValue(property, asGlobal ? "SYS" : "USR");
     }
     
     @Override
     public List<String> getInstances(String propertyName, boolean asGlobal) {
         return getInstances(propertyName, asGlobal ? "SYS" : "USR");
     }
     
     @Override
     public boolean isAvailable() {
         return broker != null && broker.isConnected();
     }
 }
