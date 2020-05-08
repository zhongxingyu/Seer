 /*
  *  Copyright the original author or authors.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package therian.operator.convert;
 
 import java.lang.reflect.Type;
 import java.util.Iterator;
 
 import org.apache.commons.lang3.reflect.TypeUtils;
 
 import therian.TherianContext;
 import therian.buildweaver.StandardOperator;
 import therian.operation.Convert;
 import therian.operation.GetElementType;
 
 /**
  * Attempts to convert to {@link Iterable} and call {@link Iterable#iterator()}.
  */
 @SuppressWarnings("rawtypes")
 @StandardOperator
 public class DefaultToIteratorConverter extends Converter<Object, Iterator> {
 
     @Override
     public boolean perform(TherianContext context, Convert<? extends Object, ? super Iterator> convert) {
         final Iterable sourceIterable = context.eval(Convert.to(Iterable.class, convert.getSourcePosition()));
         convert.getTargetPosition().setValue(sourceIterable.iterator());
         return true;
     }
 
     @Override
     public boolean supports(TherianContext context, Convert<?, ? super Iterator> convert) {
         if (!super.supports(context, convert) || convert.getSourcePosition().getValue() instanceof Iterator<?>) {
             return false;
         }
         final Convert<?, Iterable> toIterable = Convert.to(Iterable.class, convert.getSourcePosition());
 
        if (!context.supports(toIterable)) {
             return false;
         }
         final GetElementType<?> getTargetElementType = GetElementType.of(convert.getTargetPosition());
         if (!context.supports(getTargetElementType)) {
             return false;
         }
 
         final Type targetElementType = context.eval(getTargetElementType);
         final GetElementType<?> getSourceElementType = GetElementType.of(convert.getSourcePosition());
         final Type sourceElementType;
         if (context.supports(getSourceElementType)) {
             sourceElementType = context.eval(getSourceElementType);
         } else {
             sourceElementType = convert.getSourcePosition().getType();
         }
         return TypeUtils.isAssignable(sourceElementType, targetElementType);
     }
 
 }
