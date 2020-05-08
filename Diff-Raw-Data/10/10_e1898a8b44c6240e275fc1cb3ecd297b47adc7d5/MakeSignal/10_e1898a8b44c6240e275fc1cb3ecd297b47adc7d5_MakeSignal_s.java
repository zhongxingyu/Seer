 /*
  * Copyright 2011 Vasily Shiyan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package see.functions.reactive;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Supplier;
import com.google.common.collect.ClassToInstanceMap;
 import com.google.common.collect.Sets;
 import see.evaluation.Context;
 import see.evaluation.ToFunction;
 import see.evaluation.ValueProcessor;
 import see.evaluation.evaluators.SimpleContext;
 import see.evaluation.visitors.EagerVisitor;
 import see.evaluation.visitors.LazyVisitor;
 import see.functions.ContextCurriedFunction;
 import see.functions.VarArgFunction;
 import see.properties.ChainResolver;
 import see.reactive.Signal;
 import see.reactive.SignalFactory;
 import see.tree.Node;
 
 import javax.annotation.Nonnull;
 import java.util.Collection;
 import java.util.List;
 
import static com.google.common.collect.ImmutableClassToInstanceMap.builder;
 import static see.evaluation.conversions.FunctionConversions.concat;
 
 /**
  * Signal creation. Expects second argument to be a tree.
  * Creates a signal bound to signals present in tree.
  */
 public class MakeSignal implements ContextCurriedFunction<Object, Signal<?>> {
     @Override
     public VarArgFunction<Object, Signal<?>> apply(@Nonnull final Context context) {
         return new VarArgFunction<Object, Signal<?>>() {
             @Override
             public Signal<?> apply(@Nonnull List<Object> input) {
                 Preconditions.checkArgument(input.size() == 2, "MakeSignal takes two arguments");
 
                 final Node<Object> tree = (Node<Object>) input.get(1);
 
                 Collection<Signal<?>> dependencies = extractDependencies(tree);
 
                 final LazyVisitor lazyVisitor = createVisitor();
                 return context.getServices().getInstance(SignalFactory.class).bind(dependencies, new Supplier<Object>() {
                     @Override
                     public Object get() {
                         return tree.accept(lazyVisitor);
                     }
                 });
             }
 
             @Override
             public String toString() {
                 return "signal";
             }
 
             private Collection<Signal<?>> extractDependencies(Node<Object> tree) {
                 Collection<Signal<?>> dependencies = Sets.newHashSet();
 
                 Context extractionContext = getPatchedContext(new SignalExtractor(dependencies));
                 tree.accept(new EagerVisitor(extractionContext, getProcessor(), getResolver()));
 
                 return dependencies;
             }
 
             private LazyVisitor createVisitor() {
                 return new LazyVisitor(getPatchedContext(new SignalToFunction()), getProcessor(), getResolver());
             }
 
             private Context getPatchedContext(ToFunction signalFunction) {
                 ToFunction old = context.getServices().getInstance(ToFunction.class);
 
                ClassToInstanceMap<Object> altServices = builder()
                        .put(ToFunction.class, concat(signalFunction, old))
                        .build();

                return SimpleContext.create(context.getScope(), altServices);
             }
 
             private ValueProcessor getProcessor() {
                 return context.getServices().getInstance(ValueProcessor.class);
             }
 
             private ChainResolver getResolver() {
                 return context.getServices().getInstance(ChainResolver.class);
             }
         };
     }
 
     @Override
     public String toString() {
         return "signal";
     }
 
     private static class SignalExtractor extends SignalToFunction {
         private final Collection<? super Signal<?>> dependencies;
 
         private SignalExtractor(Collection<? super Signal<?>> dependencies) {
             this.dependencies = dependencies;
         }
 
         @Override
         public boolean isDefinedAt(Object input) {
             if (input instanceof Signal<?>) {
                 dependencies.add((Signal<?>) input);
             }
 
             return super.isDefinedAt(input);
         }
     }
 
     private static class SignalToFunction implements ToFunction {
         @Nonnull
         @Override
         public ContextCurriedFunction<Object, ?> apply(@Nonnull Object input) {
             final Signal<?> signal = (Signal<?>) input;
             return new ContextCurriedFunction<Object, Object>() {
                 @Override
                 public VarArgFunction<Object, Object> apply(@Nonnull Context context) {
                     return new VarArgFunction<Object, Object>() {
                         @Override
                         public Object apply(@Nonnull List<Object> objects) {
                             return signal.now();
                         }
                     };
                 }
             };
         }
 
         @Override
         public boolean isDefinedAt(Object input) {
             return input instanceof Signal;
         }
     }
 }
