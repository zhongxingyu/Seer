 package org.icefaces.ace.model.tree;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Copyright 2010-2011 ICEsoft Technologies Canada Corp.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * <p/>
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * <p/>
  * User: Nils
  * Date: 2012-08-14
  * Time: 2:03 PM
  */
public class NodeStateMap implements Map<Object, NodeState> {
     Map<Object, NodeState> map = new HashMap<Object, NodeState>();
     KeySegmentConverter keyConverter;
 
 
 
     public NodeStateMap() {
     }
 
     public NodeStateMap(KeySegmentConverter keyConverter) {
         this.keyConverter = keyConverter;
     }
 
     public KeySegmentConverter getKeyConverter() {
         return keyConverter;
     }
 
     public void setKeyConverter(KeySegmentConverter keyConverter) {
         this.keyConverter = keyConverter;
     }
 
     public int size() {
         return map.size();
     }
 
     public boolean isEmpty() {
         return map.isEmpty();
     }
 
     public boolean containsKey(Object o) {
         return map.containsKey(o);
     }
 
     public boolean containsValue(Object o) {
         return map.containsValue(o);
     }
 
     public NodeState get(Object o) {
         NodeState state;
 
         state = map.get(o);
         if (state != null) return state;
 
         // If state is null, create a new state for the node.
         state = new NodeState();
         put(o, state);
         return state;
     }
 
     public NodeState put(Object o, NodeState nodeState) {
         if (keyConverter == null) return map.put(o, nodeState);
         else return map.put(keyConverter.getSegment(o), nodeState);
     }
 
     public NodeState remove(Object o) {
         if (keyConverter == null) return map.remove(o);
         else return map.remove(keyConverter.getSegment(o));
     }
 
     public void putAll(Map<? extends Object, ? extends NodeState> inputMap) {
         for (Object key : inputMap.keySet())
             put(key, inputMap.get(key));
     }
 
     public void clear() {
         map.clear();
     }
 
     public Set<Object> keySet() {
         return map.keySet();
     }
 
     public Collection<NodeState> values() {
         return map.values();
     }
 
     public Set<Entry<Object, NodeState>> entrySet() {
         return map.entrySet();
     }
 }
