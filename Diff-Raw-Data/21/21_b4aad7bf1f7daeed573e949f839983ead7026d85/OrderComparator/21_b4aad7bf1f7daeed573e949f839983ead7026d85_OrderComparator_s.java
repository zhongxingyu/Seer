 /*
  * Copyright (C) 2011 Klaus Reimer <k@ailis.de>
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package de.ailis.maven.plugin.javascript;
 
 import java.util.Comparator;
 import java.util.List;
 
 import com.google.javascript.jscomp.JSSourceFile;
 
 /**
  * This comparator sorts a list by looking up the values in the ordered list
  * specified in the constructor. Values which are not in the ordered list
  * are always put at the end of the resulting list.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public class OrderComparator implements Comparator<JSSourceFile>
 {
     /** The ordered filenames */
     private final List<String> order;
 
     /**
      * Constructs a new order comparator.
      *
      * @param order
      *            The ordered filenames
      */
     public OrderComparator(final List<String> order)
     {
         this.order = order;
     }
 
     /**
      * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
      */
     @Override
     public int compare(final JSSourceFile o1, final JSSourceFile o2)
     {
        final int i1 = this.order.indexOf(o1.getName());
        final int i2 = this.order.indexOf(o2.getName());
         if (i1 == i2) return 0;
         if (i1 < 0) return 1;
         if (i2 < 0) return -1;
         return i1 - i2;
     }
 }
