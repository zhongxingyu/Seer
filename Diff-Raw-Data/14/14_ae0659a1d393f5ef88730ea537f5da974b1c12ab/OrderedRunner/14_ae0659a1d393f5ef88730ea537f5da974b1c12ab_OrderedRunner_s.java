 /*
  * Copyright (C) 2013 Project-Phoenix
  * 
  * This file is part of library.
  * 
  * library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.phoenix.junit;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 
 public class OrderedRunner extends BlockJUnit4ClassRunner {
 
     public OrderedRunner(Class<?> clazz) throws InitializationError {
         super(clazz);
     }
 
     @Override
     protected List<FrameworkMethod> computeTestMethods() {
         List<FrameworkMethod> list = super.computeTestMethods();
         Collections.sort(list, ORDER_COMPARATOR);
         return list;
     }
 
     private static final Comparator<FrameworkMethod> ORDER_COMPARATOR = new Comparator<FrameworkMethod>() {
 
        @Override
         public int compare(FrameworkMethod m1, FrameworkMethod m2) {
             Order o1 = m1.getAnnotation(Order.class);
             Order o2 = m2.getAnnotation(Order.class);
 
             if (o1 == null || o2 == null) {
                 return -1;
             }
 
             return o1.value() - o2.value();
         }
     };
     
     @Retention(RetentionPolicy.RUNTIME)
     public @interface Order {
         public int value();
     }
 }
