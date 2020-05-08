 /*
  * Copyright 2009 Andrew Pietsch
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing permissions
  * and limitations under the License.
  */
 
 package com.pietschy.gwt.pectin.demo.client.format;
 
 import com.pietschy.gwt.pectin.client.format.ListDisplayFormat;
 
 import java.util.Collection;
 
 /**
  * Created by IntelliJ IDEA.
  * User: andrew
  * Date: Nov 21, 2009
  * Time: 12:19:13 PM
  * To change this template use File | Settings | File Templates.
  */
 class LuckyNumberLabelFormat implements ListDisplayFormat<Integer>
 {
    public String format(Collection<Integer> values)
    {
       if (values.size() == 0)
       {
          return "You have no lucky numbers.";
       }
       else if (values.size() == 1)
       {
         return "Your lucky number is " + values.iterator().next() + ".";
       }
       else
       {
 
          StringBuilder buf = new StringBuilder("Your lucky numbers are ");
          int last = values.size() - 1;
          int index = 0;
          for (Integer integer : values)
          {
             if (index > 0 && index != last)
             {
                buf.append(", ");
             }
             if (index == last)
             {
                buf.append(" and ");
             }
 
             buf.append(integer);
 
             index++;
          }
 
          buf.append(".");
 
          return buf.toString();
       }
    }
 
 }
