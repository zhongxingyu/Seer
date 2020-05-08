 package org.polyforms.delegation;
 
 import org.polyforms.delegation.builder.DelegationBuilder;
 import org.polyforms.delegation.builder.DelegationBuilderHolder;
 import org.springframework.core.GenericTypeResolver;
 
 /**
  * The helper class is used by client to register delegation by code.
  * 
  * Clients need extends this class and override
  * {@link org.polyforms.delegation.builder.DelegationRegister#register(Object)} to register the delegation pair which
  * specifies the delegator and delegatee's class and type. Additional configurations like name of bean,
  * {@link org.polyforms.delegation.builder.argumentProvider}s are able to customize the delegation more fine
  * granularity.
  * 
  * <pre>
  * public static class DelegateeDelegationBuilder extends DelegatorRegister&lt;Delegator&gt; {
  *     &#064;Override
  *     public void register(final Delegator delegator) {
  *      ... // register delegations
  *     }
  * }
  * 
  * </pre>
  * 
  * Default delegation is delegate to first argument of delegator method. {@link #with(DelegateeRegister)} is used to
  * register delegation for specified delegatee type.
  * 
  * <h2>Some Scenerios<h2>
  * <h3>Register method in delegator to delegatee method with same name.</h3>
  * <code>this.<Delegatee> delegate(delegator.delegatorMethod(null...));</code>
  * 
  * <h3>Register method in delegator to specified delegatee method.</h3>
  * <code>this.<Delegatee> delegate(delegator.delegatorMethod(null...)).delegateeMethod(argumentProvider...);</code>
  * 
  * <h3>Register void method in delegator to specified delegatee method.</h3>
  * <code>delegator.delegatorMethod(null...); delegate().delegateeMethod(argumentProvider...);</code>
  * 
  * <h3>Register all abstract methods in delegator.</h3> <code>delegate();</code>
  * 
  * @author Kuisong Tong
  * @since 1.0
  * 
  * @param <S> type of delegator
  */
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
