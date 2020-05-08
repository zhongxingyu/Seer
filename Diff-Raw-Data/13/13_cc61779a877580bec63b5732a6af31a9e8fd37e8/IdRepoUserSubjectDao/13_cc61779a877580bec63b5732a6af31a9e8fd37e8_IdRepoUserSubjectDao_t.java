 package com.sun.identity.admin.dao;
 
 import com.iplanet.sso.SSOException;
 import com.sun.identity.admin.model.IdRepoUserViewSubject;
 import com.sun.identity.admin.model.ViewSubject;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdSearchOpModifier;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdType;
 import java.io.Serializable;
import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class IdRepoUserSubjectDao extends IdRepoSubjectDao implements Serializable {
 
     private String namingAttribute;
 
     protected IdType getIdType() {
         return IdType.USER;
     }
 
     protected ViewSubject newViewSubject(AMIdentity ami) {
         IdRepoUserViewSubject uvs = (IdRepoUserViewSubject) getSubjectType().newViewSubject();
         uvs.setName(ami.getUniversalId());
 
         Map attrs;
         try {
             attrs = ami.getAttributes();
         } catch (IdRepoException idre) {
             attrs = null;
         } catch (SSOException ssoe) {
             attrs = null;
         }
 
         if (attrs != null) {
             Set cnSet = (Set) attrs.get("cn");
             if (cnSet != null && cnSet.size() > 0) {
                 String cn = (String) cnSet.iterator().next();
                 uvs.setCn(cn);
             }
         }
 
         return uvs;
     }
 
     public String getNamingAttribute() {
         return namingAttribute;
     }
 
     public void setNamingAttribute(String namingAttribute) {
         this.namingAttribute = namingAttribute;
     }
 
     @Override
     protected IdSearchResults getIdSearchResults(IdSearchControl idsc, String pattern) {
         if (!pattern.equals("*")) {
             pattern = "*";
         }
 
         return super.getIdSearchResults(idsc, pattern);
     }
 
     @Override
     protected IdSearchControl getIdSearchControl(String pattern) {
         IdSearchControl idsc = super.getIdSearchControl(pattern);
        Map searchMap = new HashMap();
 
        searchMap.put(getNamingAttribute(), Collections.singleton(pattern));
        searchMap.put("cn", Collections.singleton(pattern));
 
         idsc.setSearchModifiers(IdSearchOpModifier.OR, searchMap);
         return idsc;
     }
 
     public void decorate(ViewSubject vs) {
         assert(vs instanceof IdRepoUserViewSubject);
         IdRepoUserViewSubject idus = (IdRepoUserViewSubject)vs;
 
         IdRepoUserViewSubject decoratedIdus = (IdRepoUserViewSubject)getViewSubject(idus.getName());
 
         // TODO
         // any other user decoration?
         idus.setCn(decoratedIdus.getCn());
     }
 }
