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
 
 package org.objectstream.spi;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.objectstream.context.CallContext;
 import org.objectstream.exceptions.ExceptionUtils;
 import org.objectstream.instrumentation.*;
 import org.objectstream.instrumentation.collections.ObjectStreamCollection;
 import org.objectstream.value.Evaluator;
 import org.objectstream.value.MethodEvaluator;
 import org.objectstream.value.Value;
 import org.objectstream.value.ValueObserver;
 
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
 import java.util.*;
 
 public class DefaultObjectStreamProvider implements ObjectStreamProvider {
     private final Map<Integer, Map<Integer, Set<Bind>>> binds = new HashMap<>();
     private final Map<Value, Integer> objects = new HashMap<>();
     private final Map<Integer, Set<Value>> values = new HashMap<>();
     private final ProxyFactory proxyFactory;
     private final CallContext context;
     private final StreamBuilder streamBuilder;
 
     public DefaultObjectStreamProvider(StreamBuilder streamBuilder, ProxyFactory proxyFactory, CallContext context) {
         this.context = context;
         this.proxyFactory = proxyFactory;
         this.streamBuilder = new StreamBuilderWrapper(streamBuilder);
     }
 
     public ProxyFactory getProxyFactory() {
         return proxyFactory;
     }
 
     public CallContext getContext() {
         return context;
     }
 
     @Override
     public <T> T createProxy(T object) {
         if (object instanceof Collection) {
             if (object instanceof ObjectStreamCollection) {
                 return object;
             } else {
                 return (T) new ObjectStreamCollection((Collection) object, this);
             }
         }
         //eval
         return getProxyFactory().createObjectProxy(object, new ObjectStreamProviderHandler<>(object,this));
     }
 
     @Override
     public void enhance(Object object) {
         getProxyFactory().enhance(object, new FieldEnhancer(this));  //createProxy
     }
 
     @Override
     public int calculateHashCode(Object object) {
         if (object instanceof ObjectStreamProxy) {
             return System.identityHashCode(((ObjectStreamProxy) object).getOriginalObject());
         } else {
             return System.identityHashCode(object);
         }
     }
 
     @Override
     public StreamBuilder getStreamBuilder() {
         return streamBuilder;
     }
 
     @Override
     public Object eval(Object object, Method method, Object[] parameters) {
         String name = method.getName();
 
         //We do not intercept calculateHashCode and equals.
         if ("equals".equals(name)) {
             Object other = parameters[0] instanceof ObjectStreamProxy ? ((ObjectStreamProxy) parameters[0]).getOriginalObject() : parameters[0];
             return object.equals(other) ? Boolean.TRUE : Boolean.FALSE;
         } else if ("hashCode".equals(name)) {
             return object.hashCode();
         } else if ("getOriginalObject".equals(name)) {
             return object;
         }
 
         Object res = null;
 
         if (isValue(method)) {
             //here, the same value must be returned for the same parameters unless invalidated
             //We talking about value in the sense of functional programming (no side effects)
             Value value = streamBuilder.value(new MethodEvaluator(object, method, parameters, this));   //enhance
             getContext().push(value);
 
             Object oldValue = value.getValue();
             res = value.eval(); //this call must be between the push and the pop
 
             getContext().pop();
             if (!getContext().empty()) {
                 streamBuilder.bind(getContext().peek(), value);
             }
 
             if (!res.equals(oldValue)) {
                 streamBuilder.notifyChange(value);
             }
 
         } else {
             //Here are the method call with side effects.
             try {
                 //This is a special case of "findDependentValues"
                 Value readPropertyValue = findReadPropertyValue(object, method, parameters);
                 Object oldPropertyValue = null;
                 Object newPropertyValue = null;
 
                 if (readPropertyValue != null) {
                     oldPropertyValue = readPropertyValue.eval();
                     newPropertyValue = parameters[0];
                     if (newPropertyValue != null && newPropertyValue instanceof Collection) {
                         if (!(newPropertyValue instanceof ObjectStreamCollection)) {
                             newPropertyValue = createProxy(newPropertyValue);
                             parameters[0] = newPropertyValue;
                         }
                         ((ObjectStreamCollection) newPropertyValue).addParent(readPropertyValue);
                     }
                 }
 
                 res = method.invoke(object, parameters);
                                                   //optimisation for properties
                 if (readPropertyValue != null && oldPropertyValue != newPropertyValue) {
                     streamBuilder.invalidate(readPropertyValue);
                     if (oldPropertyValue != null) {
                         //Remove oldPropertyValue dependencies and any children dependencies in case of collection
                         unbind(object, oldPropertyValue);
                         if (oldPropertyValue instanceof Collection) {
                             for (Object child : ((Collection) oldPropertyValue)) {
                                 unbind(object, child);
                             }
                             if (!getContext().empty()) {
                                 ((ObjectStreamCollection) newPropertyValue).removeParent(getContext().peek());
                             }
                         }
                     } else {
                         //If the previous value was null, we do not have any information about dependencies.  Therefore
                         //we need to blast everything.
                         invalidate(object);
                     }
                 }
             } catch (Throwable e) {
                 throw ExceptionUtils.wrap(e);
             }
         }
 
         return res;
     }
 
     private boolean isValue(Method method) {
         return method.getReturnType() != Void.TYPE;
     }
 
     private Value findReadPropertyValue(Object object, Method method, Object[] objects) {
         Value result = null;
         try {
             for (PropertyDescriptor propertyDescriptor :
                     Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors()) {
                 Method write = propertyDescriptor.getWriteMethod();
                 if (write != null && write.equals(method)) {
                     Method read = propertyDescriptor.getReadMethod();
                     if (read != null && read.getParameterTypes().length == 0) {
                         result = streamBuilder.value(new MethodEvaluator(object, read, new Object[]{}, this)); //enhance
                     }
                 }
             }
         } catch (Throwable e) {
             throw ExceptionUtils.wrap(e);
         }
         return result;
     }
 
     /**
      * This implementation of the ObjectStreamProvider require to remember every binds at object level in addition
      * to value level.  This is necessary because we do not have field level dependency management (only method level).
      * If we replace the value of an Object field, we need to be able to find every binds between the parent and the
      * child objects to severe them.
      * <p/>
      * This is not necessary if we implement a class transformer that allows us to intercept field read and write.
      *
      * @param parent
      * @param child
      */
     private void registerBind(Value parent, Value child) {
         Integer parentHash = objects.get(parent);
         Integer childHash = objects.get(child);
         Map<Integer, Set<Bind>> children = binds.get(parentHash);
         if (children == null) {
             children = new HashMap<>();
             binds.put(parentHash, children);
         }
 
         Set<Bind> parentChildrenBinds = children.get(childHash);
         if (parentChildrenBinds == null) {
             parentChildrenBinds = new HashSet<>();
             children.put(childHash, parentChildrenBinds);
         }
 
         parentChildrenBinds.add(new Bind(parent, child));
     }
 
     private void unRegisterBind(Value parent, Value child) {
         Integer parentHash = objects.get(parent);
         Integer childHash = objects.get(child);
         Map<Integer, Set<Bind>> children = binds.get(parentHash);
 
         if (children == null) {
             return;
         }
 
         Set<Bind> parentChildrenBinds = children.get(childHash);
         if (parentChildrenBinds == null) {
             return;
         }
 
         parentChildrenBinds.remove(new Bind(parent, child));
     }
 
     /**
      * This method will unbind a child object from its parent.  It uses the binds registered by the
      * <code>registerBind()</code> method.
      *
      * @param parent
      * @param child
      */
     private void unbind(Object parent, Object child) {
         Integer parentHash = calculateHashCode(parent);
         Integer childHash = calculateHashCode(child);
         Map<Integer, Set<Bind>> children = binds.get(parentHash);
         if (children != null) {
             Set<Bind> parentChildBinds = children.get(childHash);
             if (parentChildBinds != null) {
                 for (Bind bind : parentChildBinds) {
                     streamBuilder.unbind(bind.getParent(), bind.getChild());
                     streamBuilder.invalidate(bind.getParent());
                 }
             }
         }
     }
 
     /**
      * This method will invalidate every value of this object.  This is far of being optimal but it is necessary
      * when a field goes from null to a new value as no dependencies have been created from a null field.
      * This will not be necessary after implementing the object transformer with field read/write interception.
      *
      * @param object
      */
     private void invalidate(Object object) {
         Set<Value> objectValues = values.get(calculateHashCode(object));
         if (objectValues != null) {
             for (Value value : objectValues) {
                 streamBuilder.invalidate(value);
             }
         }
     }
 
     private static class Bind {
         private final Value parent;
         private final Value child;
 
         private Bind(Value parent, Value child) {
             this.parent = parent;
             this.child = child;
         }
 
         private Value getParent() {
             return parent;
         }
 
         private Value getChild() {
             return child;
         }
 
         @Override
         public int hashCode() {
             return new HashCodeBuilder(17, 37).append(parent).append(child).toHashCode();
         }
 
         @Override
         public boolean equals(Object object) {
             if (object == this) return true;
             if (object == null) return false;
             if (this.getClass() != object.getClass()) return false;
             Bind other = (Bind) object;
             return new EqualsBuilder().append(parent, other.parent).append(child, other.child).isEquals();
         }
 
     }
 
     private class StreamBuilderWrapper implements StreamBuilder {
         private final StreamBuilder streamBuilder;
 
         private StreamBuilderWrapper(StreamBuilder streamBuilder) {
             this.streamBuilder = streamBuilder;
         }
 
         @Override
         public Value value(Evaluator evaluator) {
             Value value = streamBuilder.value(evaluator);
             Integer objectHash = calculateHashCode(evaluator.targetObject());
             objects.put(value, objectHash);
             Set<Value> objectValues = values.get(objectHash);
             if (objectValues == null) {
                 objectValues = new HashSet<>();
                 values.put(objectHash, objectValues);
             }
             objectValues.add(value);
             return value;
         }
 
         @Override
         public <M> void observe(Value<M> value, ValueObserver<M> observer) {
             streamBuilder.observe(value, observer);
         }
 
         @Override
         public void bind(Value parent, Value child) {
             streamBuilder.bind(parent, child);
 
             registerBind(parent, child);
         }
 
         @Override
         public void unbind(Value parent, Value child) {
             streamBuilder.unbind(parent, child);
             unRegisterBind(parent, child);
         }
 
         @Override
         public void invalidate(Value readPropertyValue) {
             streamBuilder.invalidate(readPropertyValue);
         }
 
         @Override
         public void notifyChange(Value value) {
             streamBuilder.notifyChange(value);
         }
     }
 }
