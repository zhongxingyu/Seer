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
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Sets;
 import see.evaluator.EagerVisitor;
 import see.evaluator.LazyVisitor;
 import see.evaluator.ValueProcessor;
 import see.functions.properties.ChainResolver;
 import see.parser.config.GrammarConfiguration;
 import see.reactive.Dependency;
 import see.reactive.Signal;
 import see.tree.Node;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Signal creation. Expects second argument to be a tree.
 * Creates a
  */
 public class MakeSignal extends ReactiveFunction<Object, Signal<?>> {
     @Override
     public Signal<?> apply(ContextConfig config, final List<Object> input, final Map<String, ?> context) {
         Preconditions.checkArgument(input.size() == 2, "MakeSignal takes two arguments");
 
         final Node<Object> tree = (Node<Object>) input.get(1);
 
         GrammarConfiguration grammarConfig = config.getGrammarConfig();
         List<ValueProcessor> visitorProcessors = config.getValueProcessors();
 
         ChainResolver chainResolver = grammarConfig.getChainResolver();
 
         SignalCapture signalCapture = new SignalCapture();
         EagerVisitor eagerVisitor = new EagerVisitor(context, prepend(signalCapture, visitorProcessors), chainResolver);
         tree.accept(eagerVisitor);
 
         final LazyVisitor lazyVisitor = new LazyVisitor(context, prepend(new SignalExpand(), visitorProcessors), chainResolver);
         return config.getReactiveFactory().bind(signalCapture.dependencies, new Supplier<Object>() {
             @Override
             public Object get() {
                 return tree.accept(lazyVisitor);
             }
         });
     }
 
     private static <T> List<T> prepend(T item, List<T> list) {
         return ImmutableList.<T>builder().add(item).addAll(list).build();
     }
 
     @Override
     public String toString() {
         return "signal";
     }
 

    private static final class SignalCapture implements ValueProcessor {
        private final Collection<Dependency> dependencies = Sets.newHashSet();

         @Override
         public Object apply(@Nullable Object input) {
             if (input instanceof Signal<?>) {
                 Signal<?> signal = (Signal<?>) input;
                dependencies.add(signal);
                 return signal.getNow();
             }
             return input;
         }
     }
 
    private static final class SignalExpand implements ValueProcessor {
         @Override
         public Object apply(@Nullable Object input) {
             if (input instanceof Signal<?>) {
                 Signal<?> signal = (Signal<?>) input;
                 return signal.getNow();
             }
             return input;
         }
     }
 }
