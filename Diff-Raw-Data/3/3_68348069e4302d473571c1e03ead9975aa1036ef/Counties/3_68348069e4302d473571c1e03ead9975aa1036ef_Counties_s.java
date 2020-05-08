 /*
  * Copyright 2013 Eike Kettner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.eknet.countyj;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eknet.county.CounterKey;
 import org.eknet.county.County;
import scala.actors.threadpool.Arrays;
 import scala.collection.JavaConversions$;
 
 /**
  * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
  * @since 25.03.13 19:11
  */
 public final class Counties {
 
   private Counties() {
   }
 
   public static CounterKey parseKey(String key) {
     return CounterKey.parse(key, CounterKey.defaultSegmentDelimiter(), CounterKey.defaultNameSeparator());
   }
 
   public static JDefaultCounty create() {
     return new JDefaultCounty();
   }
 
   static JCounty next(County c, String... path) {
     List<CounterKey> keys = new ArrayList<>(path.length);
     for (String s : path) {
       keys.add(CounterKey.apply(s));
     }
     return JCounty.from(c).apply(JavaConversions$.MODULE$.asScalaBuffer(keys));
   }
 
   static JCounty next(County c, CounterKey... paths) {
     //noinspection unchecked
     List<CounterKey> keys = Arrays.asList(paths);
     return JCounty.from(c).apply(JavaConversions$.MODULE$.asScalaBuffer(keys));
   }
 }
