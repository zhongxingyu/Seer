 /**
  * Copyright 2013 Emeka Mosanya, all rights reserved.
  *
  * Unauthorized copying of this file, via any medium is strictly prohibited
  * Proprietary and confidential.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.objectstream.spi.streambuilder.collection;
 
 
 import org.objectstream.spi.StreamBuilder;
 import org.objectstream.value.Evaluator;
 import org.objectstream.value.Value;
 import org.objectstream.value.ValueObserver;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class CollectionStreamBuilder implements StreamBuilder {
 
     private final Set<Value> nodes = new HashSet<>();
     private final Map<Evaluator, Value> nodeMap = new HashMap<>();
     private final Map<Value, Set<ValueObserver>> nodeObservers = new HashMap<>();
 
     private final Map<Value, Set<Value>> nodeParents = new HashMap<>();
     private final Map<Value, Set<Value>> nodeChildren = new HashMap<>();
 
 /*
 Class<?> cl = Class.forName("javax.swing.JLabel");
 Constructor<?> cons = cl.getConstructor(String.class);
 Object o = cons.newInstance("JLabel");
 
  */
 
     @Override
     public <M> void observe(Value<M> value, ValueObserver<M> observer) {
         Set<ValueObserver> observers = nodeObservers.get(value);
         if (observers == null) {
             observers = new HashSet<>();
             nodeObservers.put(value, observers);
         }
         if (!observers.contains(observer)) {
             observers.add(observer);
             observer.notify(value);
         }
     }
 
     @Override
     public Value value(Evaluator calculator) {
         Value value = nodeMap.get(calculator);
         if (value == null) {
             value = new Value(calculator);
             nodes.add(value);
             nodeMap.put(calculator, value);
         }
 
         return value;
     }
 
     @Override
     public void bind(Value parent, Value child) {
         verify(parent);
         verify(child);
 
         Set<Value> children = nodeChildren.get(parent);
         if (children == null) {
             children = new HashSet<>();
             nodeChildren.put(parent, children);
         }
 
         children.add(child);
 
         Set<Value> parents = nodeParents.get(child);
         if (parents == null) {
             parents = new HashSet<>();
             nodeParents.put(child, parents);
         }
 
         parents.add(parent);
     }
 
     @Override
     public void unbind(Value parent, Value child) {
         verify(parent);
         verify(child);
 
         Set<Value> children = nodeChildren.get(parent);
         if (children != null) {
             children.remove(child);
         }
 
         Set<Value> parents = nodeParents.get(child);
         if (parents != null) {
             parents.remove(parent);
         }
     }
 
     @Override
     public void notifyChange(Value value) {
         Set<ValueObserver> observers = nodeObservers.get(value);
         if (observers != null) {
             for (ValueObserver observer : observers) {
                 observer.notify(value);
             }
         }
     }
 
     @Override
     public void invalidate(Value value) {
         verify(value);
         Set<Value> values = new HashSet<>();
         values.add(value);
         invalidate(values);
     }
 
     private void invalidate(Set<Value> values) {
         if (values != null && !values.isEmpty()) {
             for (Value value : values) {
                 value.setDirty();
                 notifyChange(value);
                 invalidate(nodeParents.get(value));
             }
         }
     }
 
     private void verify(Value value) {
         if (!nodes.contains(value)) {
             throw new RuntimeException(String.format("Unknown node:", value));
         }
     }
 }
