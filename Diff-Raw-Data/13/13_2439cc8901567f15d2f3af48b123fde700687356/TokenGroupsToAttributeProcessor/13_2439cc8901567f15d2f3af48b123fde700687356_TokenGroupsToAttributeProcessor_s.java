 package net.archigny.cas.persondir.adutils;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.BeanCreationException;
 import org.springframework.beans.factory.InitializingBean;
 import net.archigny.cas.persondir.processors.IAttributesProcessor;
 import net.archigny.utils.ad.api.IActiveDirectoryTokenGroupsRegistry;
 
 public class TokenGroupsToAttributeProcessor implements IAttributesProcessor, InitializingBean {
 
     private final Logger                log = LoggerFactory.getLogger(TokenGroupsToAttributeProcessor.class);
 
     /**
      * tokenGroups registry used to translate SID to ldap groups
      */
     IActiveDirectoryTokenGroupsRegistry tokenRegistry;
 
     /**
      * source attribute containing group tokens in byte[] or String form
      */
     private String                      sourceAttribute;
 
     /**
      * target attributes containing decoded groups
      */
     private String                      targetAtribute;
 
     @Override
     public void afterPropertiesSet() throws Exception {
 
         if (tokenRegistry == null) {
             throw new BeanCreationException("tokenRegistry cannot be null");
         }
         if (sourceAttribute == null) {
             throw new BeanCreationException("dnAttribute cannot be null");
         }
         if (targetAtribute == null) {
             throw new BeanCreationException("targetAttribute cannot be null");
         }
 
     }
 
     @Override
     public void processAttributes(Map<String, List<Object>> attributes) {
 
         List<Object> sourceValues = attributes.get(sourceAttribute);
 
         // Source attribute found ?
         if ((sourceValues == null) || (sourceValues.isEmpty())) {
             return;
         }
 
         if (log.isDebugEnabled()) {
             log.debug(sourceValues.size() + " token values found, processing...");
         }
 
         try {
             List<Object> targetValues = new ArrayList<Object>(sourceValues.size());
 
             for (Object rawValue : sourceValues) {
                 String groupName = tokenRegistry.getDnFromToken((byte[]) rawValue);
                if (groupName == null) {
                    log.info("unable to decode raw tokebGroup class : " + rawValue.getClass().getCanonicalName());
                 } else {
                     targetValues.add(groupName);
                 }
             }
 
             if ((targetValues != null) && (!targetValues.isEmpty())) {
                 attributes.put(targetAtribute, targetValues);
            } else {
                log.info("This DN seems to have no tokenGroups");
             }
 
         } catch (ClassCastException e1) {
             log.error("Unable to cast a source attribute value to byte[]");
         }
 
     }
 
     @Override
     public Set<String> getPossibleUserAttributeNames() {
 
         Set<String> attributeNames = new HashSet<String>();
         attributeNames.add(targetAtribute);
 
         return attributeNames;
     }
 
     // Getters et setters
 
     public IActiveDirectoryTokenGroupsRegistry getTokenRegistry() {
 
         return tokenRegistry;
     }
 
     public void setTokenRegistry(IActiveDirectoryTokenGroupsRegistry tokenRegistry) {
 
         this.tokenRegistry = tokenRegistry;
     }
 
     public String getSourceAttribute() {
 
         return sourceAttribute;
     }
 
     public void setSourceAttribute(String dnAttribute) {
 
         this.sourceAttribute = dnAttribute;
     }
 
     public String getTargetAtribute() {
 
         return targetAtribute;
     }
 
     public void setTargetAtribute(String targetAtribute) {
 
         this.targetAtribute = targetAtribute;
     }
 
 }
