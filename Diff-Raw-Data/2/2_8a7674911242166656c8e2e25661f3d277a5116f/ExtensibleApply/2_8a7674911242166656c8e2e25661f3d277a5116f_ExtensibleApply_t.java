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
 
 package see.functions.service;
 
 import com.google.common.base.Preconditions;
 import see.evaluation.Context;
 import see.evaluation.ToFunction;
 import see.functions.ContextCurriedFunction;
 import see.functions.Function;
 import see.functions.VarArgFunction;
 
 import javax.annotation.Nonnull;
 import java.util.List;
 
 /**
  * Function application, which tries to convert input to function via {@link ToFunction}, obtained from context.
  */
 public class ExtensibleApply implements ContextCurriedFunction<VarArgFunction<Object, Object>> {
     @Override
     public VarArgFunction<Object, Object> apply(@Nonnull final Context context) {
         return new VarArgFunction<Object, Object>() {
             @Override
             public Object apply(@Nonnull List<Object> args) {
                 Preconditions.checkArgument(args.size() >= 1, "Apply takes one or more arguments");
 
                 Function<List<Object>, ?> function = convertToFunction(args.get(0));
 
                 return function.apply(args.subList(1, args.size()));
             }
 
             private Function<List<Object>, ?> convertToFunction(Object f) {
                 final ToFunction toFunction = context.getServices().getInstance(ToFunction.class);
 
                 if (!toFunction.isDefinedAt(f)) {
                    throw new IllegalArgumentException("Cannot convert to function " + f);
                 }
 
                 return toFunction.apply(f);
             }
 
             @Override
             public String toString() {
                 return "applyExt";
             }
         };
 
 
     }
 
     @Override
     public String toString() {
         return "applyExt";
     }
 }
