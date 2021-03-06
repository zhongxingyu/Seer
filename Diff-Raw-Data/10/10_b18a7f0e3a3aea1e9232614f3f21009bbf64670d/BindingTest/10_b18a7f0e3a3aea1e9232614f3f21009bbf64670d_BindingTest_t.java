 /*
  * Copyright (C) 2007 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.inject;
 
 import junit.framework.TestCase;
 import com.google.inject.spi.BindingVisitor;
 import com.google.inject.spi.LinkedBinding;
 import com.google.inject.spi.InstanceBinding;
 import com.google.inject.spi.ProviderInstanceBinding;
 import com.google.inject.spi.LinkedProviderBinding;
 import com.google.inject.spi.ClassBinding;
 import com.google.inject.spi.ConstantBinding;
 import com.google.inject.spi.ProviderBinding;
 import com.google.inject.spi.ConvertedConstantBinding;
 import com.google.inject.spi.Dependency;
 import com.google.inject.name.Names;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author crazybob@google.com (Bob Lee)
  */
 public class BindingTest extends TestCase {
 
   public void testDependencies() {
     Injector injector = Guice.createInjector();
     ClassBinding<Dependent> binding
         = (ClassBinding<Dependent>) injector.getBinding(Dependent.class);
     Collection<Dependency<?>> dependencies = binding.getDependencies();
     assertEquals(4, dependencies.size());
   }
 
   static class Dependent {
     @Inject A a;
     @Inject Dependent(A a, B b) {}
     @Inject void injectBob(Bob bob) {}
   }
 
   public void testExplicitCyclicDependency() {
     Guice.createInjector(new AbstractModule() {
       protected void configure() {
         bind(A.class);
         bind(B.class);
       }
     }).getInstance(A.class);
   }
 
   static class A { @Inject B b; }
   static class B { @Inject A a; }
 
   public void testProviderBinding() {
     Injector injector = Guice.createInjector();
     Binding<Bob> bobBinding = injector.getBinding(Bob.class);
     assertTrue(bobBinding.getProvider().get() instanceof Bob);
     Binding<Provider<Bob>> bobProviderBinding = bobBinding.getProviderBinding();
     assertTrue(bobProviderBinding.getProvider().get().get() instanceof Bob);
     Binding<Provider<Provider<Bob>>> bobProviderProviderBinding
         = bobProviderBinding.getProviderBinding();
     assertTrue(bobProviderProviderBinding.getProvider().get().get().get()
         instanceof Bob);
   }
 
   static class Bob {}
 
   public void testVisitor() {
     MyVisitor myVisitor = new MyVisitor();
 
     Injector injector = Guice.createInjector(new MyModule());
 
     for (Binding<?> binding : injector.getBindings().values()) {
       binding.accept(myVisitor);
     }
 
     myVisitor.verify();
   }
 
   static class MyModule extends AbstractModule {
 
     protected void configure() {
       // Linked.
       bind(Object.class).to(Runnable.class).in(Scopes.SINGLETON);
 
       // Instance.
       bind(Runnable.class).toInstance(new Runnable() {
         public void run() {}
       });
 
       // Provider instance.
       bind(Foo.class).toProvider(new Provider<Foo>() {
         public Foo get() {
           return new Foo();
         }
       }).in(Scopes.SINGLETON);
 
       // Provider.
       bind(Foo.class)
           .annotatedWith(Names.named("provider"))
           .toProvider(FooProvider.class);
 
       // Class.
       bind(Bar.class).in(Scopes.SINGLETON);
 
       // Constant.
       bindConstant().annotatedWith(Names.named("name")).to("Bob");
     }
   }
 
   static class Foo {}
 
   public static class FooProvider implements Provider<Foo> {
     public Foo get() {
       throw new UnsupportedOperationException();
     }
   }
 
   public static class Bar {}
 
   static class MyVisitor implements BindingVisitor<Object> {
 
     boolean linkedVisited;
 
     public void visit(LinkedBinding<?> linkedBinding) {
       linkedVisited = true;
       assertEquals(Runnable.class,
           linkedBinding.getTarget().getKey().getTypeLiteral().getType());
       assertEquals(Scopes.SINGLETON, linkedBinding.getScope());
     }
 
     boolean sawRunnable;
 
     public void visit(InstanceBinding<?> instanceBinding) {
       if (instanceBinding.getInstance() instanceof Runnable) {
         sawRunnable = true;
       }
       assertEquals(Scopes.NO_SCOPE, instanceBinding.getScope());
     }
 
     boolean providerInstanceVisited;
 
     public void visit(ProviderInstanceBinding<?> providerInstanceBinding) {
       if (providerInstanceBinding.getKey().getRawType() == Foo.class) {
         providerInstanceVisited = true;
         assertTrue(providerInstanceBinding.getProvider().get() instanceof Foo);
         assertEquals(Scopes.SINGLETON, providerInstanceBinding.getScope());
       }
     }
 
     boolean providerVisited;
 
     public void visit(LinkedProviderBinding<?> linkedProviderBinding) {
       providerVisited = true;
       assertEquals(FooProvider.class,
           linkedProviderBinding.getTargetProvider().getKey().getRawType());
     }
 
     boolean classVisitied;
 
     public void visit(ClassBinding<?> classBinding) {
       if (classBinding.getKey().getRawType().equals(Bar.class)) {
         classVisitied = true;
         assertEquals(Scopes.SINGLETON, classBinding.getScope());
       }
     }
 
     boolean constantVisited;
 
     public void visit(ConstantBinding<?> constantBinding) {
       constantVisited = true;
       assertEquals(Key.get(String.class, Names.named("name")),
           constantBinding.getKey());
       assertEquals("Bob", constantBinding.getValue());
     }
 
     public void visit(ProviderBinding<?> binding) {
     }
 
     public void visit(
         ConvertedConstantBinding<? extends Object> convertedConstantBinding) {
     }
 
     void verify() {
       assertTrue(linkedVisited);
       assertTrue(sawRunnable);
       assertTrue(providerInstanceVisited);
       assertTrue(providerVisited);
       assertTrue(classVisitied);
       assertTrue(constantVisited);
     }
   }
 
   public void testBindToUnboundLinkedBinding() {
     try {
       Guice.createInjector(new AbstractModule() {
         protected void configure() {
           bind(Collection.class).to(List.class);
         }
       });
      // Despite the fact that the binding to List.class cannot be resolved,
      // we successfully return a (broken) injector. This is a known bug.
       fail("Dangling linked binding");
     } catch (CreationException expected) {
     }
   }
 }
