 package com.sun.identity.entitlement;
 
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import javax.security.auth.Subject;
 
 public class AnyUserSubject extends VirtualSubject {
     private static final long serialVersionUID = -403250971215465050L;
 
     public VirtualId getVirtualId() {
         return VirtualId.ANY_USER;
     }
 
     public SubjectDecision evaluate(
         SubjectAttributesManager mgr,
         Subject subject,
         String resourceName,
         Map<String, Set<String>> environment)
         throws EntitlementException {
         // TODO
         return new SubjectDecision(true, Collections.EMPTY_MAP);
     }
 
 
     public Map<String, Set<String>> getSearchIndexAttributes() {
         Map<String, Set<String>> map = new HashMap<String, Set<String>>();
         Set<String> set = new HashSet<String>();
        set.add(SubjectAttributesCollector.ATTR_NAME_ALL_ENTITIES);
         map.put(SubjectAttributesCollector.NAMESPACE_IDENTITY, set);
         return map;
     }
 
     public Set<String> getRequiredAttributeNames() {
         return(Collections.EMPTY_SET);
     }
 
     public boolean isIdentity() {
         return true;
     }
 
     public void setState(String state) {
         // nothing
     }
 
     public String getState() {
         return "";
     }
 }
