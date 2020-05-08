 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.fiz.aas.services.auxiliaryobjects;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 import de.fiz.ddb.aas.authorization.Privilege;
 
 /**
  *
  * @author bkl
  */
 //@XmlRootElement
 //@XmlAccessorType(XmlAccessType.NONE)
 public class PrivilegeUsersMap implements MultivaluedMap<Privilege, String> {
 
     //private HashMap<Privilege, List<String>> _map = new HashMap<Privilege, List<String>>();
     private EnumMap<Privilege, List<String>> _map = new EnumMap<Privilege, List<String>>(Privilege.class);
 
     // ***************************************************************************
     @Override
     public void putSingle(Privilege key, String value) {
         if ((key == null) || (value == null))
             return;
         if (this._map.get(key) == null) {
             this._map.put(key, Collections.synchronizedList(new ArrayList<String>())).add(value);
         }
         else {
             this._map.get(key).add(value);
         }
     }
 
     @Override
     public void add(Privilege key, String value) {
         this.putSingle(key, value);
     }
 
     @Override
     public String getFirst(Privilege key) {
         List<String> vList = this._map.get(key);
         if (!vList.isEmpty())
             return vList.get(0);
         else
             return null;
     }
 
     @Override
     public int size() {
         return this._map.size();
     }
 
     @Override
     public boolean isEmpty() {
         return this._map.isEmpty();
     }
 
     @Override
     public boolean containsKey(Object key) {
         return this._map.containsKey(key);
     }
 
     @Override
     public boolean containsValue(Object value) {
         return this._map.containsValue(value);
     }
 
     @Override
     public List<String> get(Object key) {
         return this._map.get(key);
     }
 
     @Override
     public List<String> put(Privilege key, List<String> value) {
         return this._map.put(key, value);
     }
 
     @Override
     public List<String> remove(Object key) {
         return this._map.remove(key);
     }
 
     @Override
     public void putAll(Map<? extends Privilege, ? extends List<String>> m) {
         Privilege vPrivilege;
        for (Iterator<Privilege> iter = (Iterator<Privilege>) m.keySet().iterator(); iter.hasNext();) {
             vPrivilege = iter.next();
             if (m.get(vPrivilege) != null) {
                 this._map.put(vPrivilege, m.get(vPrivilege));
             }
         }
     }
 
     @Override
     public void clear() {
         this._map.clear();
     }
 
     @Override
     public Set<Privilege> keySet() {
         return this._map.keySet();
     }
 
     @Override
     public Collection<List<String>> values() {
         return this._map.values();
     }
 
     @Override
     public Set<Entry<Privilege, List<String>>> entrySet() {
         //throw new UnsupportedOperationException("Not supported yet.");
         return this._map.entrySet();
     }
 
     // ***************************************************************************
 }
