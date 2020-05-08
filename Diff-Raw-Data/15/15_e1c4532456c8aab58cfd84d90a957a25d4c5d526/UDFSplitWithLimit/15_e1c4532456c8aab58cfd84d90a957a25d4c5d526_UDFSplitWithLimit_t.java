 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hive.ql.udf.generic;
 
 import java.util.ArrayList;
 
 import org.apache.hadoop.hive.ql.exec.Description;
 import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
 import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
 import org.apache.hadoop.hive.ql.metadata.HiveException;
 import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
 import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
 import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
 import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
 import org.apache.hadoop.io.Text;
 import java.util.regex.Pattern;
 import org.apache.hadoop.io.IntWritable;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 /**
  * GenericUDFSplit.
  *
  */
 @Description(name = "split", value = "_FUNC_(str, regex, [limit]) - Splits str around occurances that match "
     + "regex", extended = "Example:\n"
     + "  > SELECT _FUNC_('oneAtwoBthreeC', '[ABC]', 2) FROM src LIMIT 1;\n"
     + "  [\"one\", \"two\" \n"
     + "  Note: setting limit to 0 will cause trailing empty/null entries to be truncated (which is earlier behavior"
     + "    when \"limit\" was not available as third parameter)")
 public class UDFSplitWithLimit extends GenericUDF {
   private ObjectInspectorConverters.Converter[] converters;
 
   @Override
   public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
     if ( arguments.length < 2 || arguments.length > 3) {
       throw new UDFArgumentLengthException(
           "The function SPLIT(s, regexp) takes either 2 (without limit) or 3 (with limit) arguments.");
     }
 
     converters = new ObjectInspectorConverters.Converter[arguments.length];
     for (int i = 0; i < arguments.length; i++) {
       converters[i] = ObjectInspectorConverters.getConverter(arguments[i],
           PrimitiveObjectInspectorFactory.writableStringObjectInspector);
     }
 
     return ObjectInspectorFactory
         .getStandardListObjectInspector(PrimitiveObjectInspectorFactory
             .writableStringObjectInspector);
   }
 
   @Override
   public Object evaluate(DeferredObject[] arguments) throws HiveException {
     try {
       if (arguments[0].get() == null || arguments[1].get() == null) {
         return null;
       }
 
       Text s = (Text) converters[0].convert(arguments[0].get());
       Text regex = (Text) converters[1].convert(arguments[1].get());
       int limit = Integer.MAX_VALUE; 
       if (arguments.length>=3) {
         limit = Integer.parseInt(((Text)(converters[2]).convert(arguments[2].get())).toString());    }
 
       ArrayList<Text> result = new ArrayList<Text>();
 
       for (String str : split(s.toString(),regex.toString(), limit)) {
         result.add(new Text(str));
       }
 
       return result;
     } catch (Exception e) {
       System.err.println(toString(e));
       throw new HiveException(toString(e));
     }
   }
   private static String toString(Exception e) {
     StringWriter sw = new StringWriter();
     e.printStackTrace(new PrintWriter(sw));
     return e.getMessage() + ": " + sw.toString();
   }
 
   /**
   * The String.split() method has a strange behavior for the case of truncating 
   * strings containing null/empty split entries at the end: I have fixed this 
   * behavior, thus requiring this split() method to be included within this UDF 
   * to override /replace the standard String.split()
   */
   public static String[] split(String str, String regex, int limit) {
       /* fastpath if the regex is a
        (1)one-char String and this character is not one of the
           RegEx's meta characters ".$|()[{^?*+\\", or
        (2)two-char String and the first char is the backslash and
           the second is not the ascii digit or ascii letter.
        */
       char ch = 0;
       if (((regex.length() == 1 &&
               ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0)) == -1) ||
               (regex.length() == 2 &&
                       regex.charAt(0) == '\\' &&
                       (((ch = regex.charAt(1))-'0')|('9'-ch)) < 0 &&
                       ((ch-'a')|('z'-ch)) < 0 &&
                       ((ch-'A')|('Z'-ch)) < 0)) &&
               (ch < Character.MIN_HIGH_SURROGATE ||
                       ch > Character.MAX_LOW_SURROGATE))
       {
           int off = 0;
           int next = 0;
           boolean limited = limit > 0;
           ArrayList<String> list = new ArrayList();
           while ((next = str.indexOf(ch, off)) != -1) {
              if (!limited || list.size() < limit) {
                   list.add(str.substring(off, next));
                   off = next + 1;
               } else {    // last one
                  // //assert (list.size() == limit - 1);
                  // list.add(str.substring(off, off+));
                  // off = regex.length();
                   break;
               }
           }
           // If no match was found, return this
           if (off == 0)
               return new String[]{str};
 
           // Add remaining segment
           if (!limited || list.size() < limit)
//              list.add(str.substring(off, regex.length()));
               list.add(str.substring(off));
 
           // Construct result
           int resultSize = list.size();
          // Here is the "strange" truncation behavior that is being modified
           if (limit == 0)
               while (resultSize > 0 && list.get(resultSize - 1).length() == 0)
                   resultSize--;
           String[] result = new String[resultSize];
           return list.subList(0, resultSize).toArray(result);
       }
       return Pattern.compile(regex).split(str, limit);
   }
 
   @Override
   public String getDisplayString(String[] children) {
     return "split(" + children[0] + ", " + children[1] + (children.length>2 ? (","+children[2]) : "")  +   ")";
   }
 
 }
