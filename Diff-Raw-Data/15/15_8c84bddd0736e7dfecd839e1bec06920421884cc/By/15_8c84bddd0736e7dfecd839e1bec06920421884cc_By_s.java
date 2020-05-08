 /**
  * Copyright (C) 2008 Ovea <dev@testatoo.org>
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
 
 package org.testatoo.cartridge.html4;
 
 
 import org.testatoo.core.Duration;
 
 import java.util.*;
 
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 
 public abstract class By {
 
     public static By id(final String id) {
         if (id == null)
             throw new IllegalArgumentException("Cannot find component with a null id.");
 
         return new By() {
             @Override
             public String id(HtmlEvaluator evaluator) {
                 return id(evaluator, new Duration(2000, MILLISECONDS), new Duration(500, MILLISECONDS));
             }
 
             @Override
             public String id(HtmlEvaluator evaluator, Duration duration, Duration frequency) {
                 return waitUntilId(evaluator, id, duration, frequency);
             }
 
             @Override
             public List<String> ids(HtmlEvaluator evaluator) {
                 return ids(evaluator, new Duration(2000, MILLISECONDS), new Duration(500, MILLISECONDS));
             }
 
             @Override
             public List<String> ids(HtmlEvaluator evaluator, Duration duration, Duration frequency) {
                 return Arrays.asList(waitUntilIds(evaluator, id, duration, frequency));
             }
 
             @Override
             public String toString() {
                 return "by id=" + id;
             }
         };
     }
 
     public static By jQuery(final String jQueryExpression) {
         if (jQueryExpression == null)
             throw new IllegalArgumentException("Cannot find component when jQueryExpression is null.");
 
         return new By() {
 
             @Override
             public String id(HtmlEvaluator evaluator) {
                 return id(evaluator, new Duration(2000, MILLISECONDS), new Duration(500, MILLISECONDS));
             }
 
             @Override
             public String id(HtmlEvaluator evaluator, Duration duration, Duration frequency) {
                 return waitUntilId(evaluator, "jquery:" + jQueryExpression, duration, frequency);
             }
 
             @Override
             public List<String> ids(HtmlEvaluator evaluator) {
                 return ids(evaluator, new Duration(2000, MILLISECONDS), new Duration(500, MILLISECONDS));
             }
 
             @Override
             public List<String> ids(HtmlEvaluator evaluator, Duration duration, Duration frequency) {
                 return Arrays.asList(waitUntilIds(evaluator, "jquery:" + jQueryExpression, duration, frequency));
             }
 
             @Override
             public String toString() {
                 return "by jQueryExpression=" + jQueryExpression;
             }
         };
     }
 
     public abstract String id(HtmlEvaluator evaluator);
 
     public abstract String id(HtmlEvaluator evaluator, Duration duration, Duration frequency);
 
     public abstract List<String> ids(HtmlEvaluator evaluator);
 
     public abstract List<String> ids(HtmlEvaluator evaluator, Duration duration, Duration frequency);
 
     public abstract String toString();
 
     private static String waitUntilId(HtmlEvaluator evaluator, String expression, Duration duration, Duration frequency) {
         Throwable ex = null;
         try {
             final long step = frequency.unit.toMillis(frequency.duration);
 
            for (long timeout = duration.unit.toMillis(duration.duration); timeout > 0; timeout -= step, Thread.sleep(step)) {
                 try {
                     return evaluator.elementId(expression);
                } catch (Throwable e) {
                     ex = e;
                 }
             }
         } catch (InterruptedException e) {
             ex = e;
         }
 
         if (ex instanceof EvaluatorException) {
             throw (EvaluatorException) ex;
         }
         throw new RuntimeException("Unable to reach the condition in " + duration.duration + " " + duration.unit, ex);
     }
 
     private static String[] waitUntilIds(HtmlEvaluator evaluator, String expression, Duration duration, Duration frequency) {
         Throwable ex = null;
         try {
             final long step = frequency.unit.toMillis(frequency.duration);
 
            for (long timeout = duration.unit.toMillis(duration.duration); timeout > 0; timeout -= step, Thread.sleep(step)) {
                 try {
                     return evaluator.elementsId(expression);
                } catch (Throwable e) {
                     ex = e;
                 }
             }
         } catch (InterruptedException e) {
             ex = e;
         }
 
         if (ex instanceof EvaluatorException) {
             throw (EvaluatorException) ex;
         }
         throw new RuntimeException("Unable to reach the condition in " + duration.duration + " " + duration.unit, ex);
     }
 }
