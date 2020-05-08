 package org.polyforms.delegation;
 
 import org.polyforms.delegation.builder.DelegationBuilder;
 import org.polyforms.delegation.builder.DelegationBuilderHolder;
 import org.springframework.core.GenericTypeResolver;
 
 @SuppressWarnings("unchecked")
 public abstract class DelegatorRegister<S> extends ParameterAwareRegister<S> {
     private final Class<S> delegatorType;
     private S source;
 
     protected DelegatorRegister() {
         super();
         delegatorType = (Class<S>) GenericTypeResolver.resolveTypeArgument(this.getClass(), DelegatorRegister.class);
     }
 
     protected final <T> T delegate() {
         return getBuilder().<T> delegate();
     }
 
     protected final <T> T delegate(final Object delegator) {
         return this.<T> delegate();
     }
 
     protected final <T> void with(final DelegateeRegister<T> delegateeRegister) {
         delegateeRegister.register(getSource());
     }
 
     private S getSource() {
         if (source == null) {
             source = getBuilder().delegateFrom(delegatorType);
         }
         return source;
     }
 
     private DelegationBuilder getBuilder() {
         return DelegationBuilderHolder.get();
     }
 
     protected abstract class DelegateeRegister<T> extends ParameterAwareRegister<S> {
         protected DelegateeRegister(final String name) {
             this();
             getBuilder().withName(name);
         }
 
         protected DelegateeRegister() {
             super();
             final Class<T> delegateeType = (Class<T>) GenericTypeResolver.resolveTypeArgument(this.getClass(),
                     DelegateeRegister.class);
             getBuilder().delegateTo(delegateeType);
         }
 
         protected final T delegate() {
            return getBuilder().<T> delegate();
         }
 
         protected final T delegate(final Object delegator) {
             return delegate();
         }
 
         private DelegationBuilder getBuilder() {
             return DelegationBuilderHolder.get();
         }
     }
 }
