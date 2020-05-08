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
 
 package see.evaluator;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import see.functions.properties.ChainResolver;
 import see.tree.Node;
 
import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Visitor, which evaluates function arguments eagerly.
  */
 public class EagerVisitor extends AbstractVisitor {
     public EagerVisitor(Map<String, ?> context, List<ValueProcessor> valueProcessors, ChainResolver resolver) {
         super(context, valueProcessors, resolver);
     }
 
     @Override
     protected <Arg> List<Arg> evaluateArgs(List<Node<Arg>> arguments) {
         List<Arg> lazyArgs = Lists.transform(arguments, new Function<Node<Arg>, Arg>() {
             @Override
             public Arg apply(Node<Arg> input) {
                 return input.accept(EagerVisitor.this);
             }
         });
 
        return Collections.unmodifiableList(Lists.newArrayList(lazyArgs)); // Forces evaluation
     }
 }
