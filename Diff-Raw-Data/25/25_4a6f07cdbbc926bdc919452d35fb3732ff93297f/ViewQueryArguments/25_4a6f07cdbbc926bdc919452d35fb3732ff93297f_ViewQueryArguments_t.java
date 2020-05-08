 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.db;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.gravidence.gravifon.util.BasicUtils;
 
 /**
  * Design document view query arguments container.
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public class ViewQueryArguments {
     
     /**
      * @see #getArguments()
      */
     private Map<String, String> arguments;
     
     /**
      * Initializes arguments map instance.
      */
     public ViewQueryArguments() {
         arguments = new HashMap<>();
     }
     
     /**
      * Returns arguments map instance.
      * 
      * @return arguments map instance
      */
     public Map<String, String> getArguments() {
         return arguments;
     }
     
     /**
      * Adds <code>key</code> query argument.
      * 
      * @param value query argument value
      * @return reference to this object
      */
     public ViewQueryArguments addKey(String value) {
         String key = String.format("\"%s\"", value);
         
         arguments.put("key", key);
         
         return this;
     }
     
     /**
      * Adds <code>key</code> query argument.<p>
      * <code>Value</code> is converted to corresponding JSON object.
      * 
      * @param value query argument value
      * @return reference to this object
      */
     public ViewQueryArguments addKey(List<?> value) {
         arguments.put("key", BasicUtils.objectToJsonString(value));
         
         return this;
     }
     
     /**
      * Adds <code>startkey</code> query argument.<p>
      * <code>Value</code> is converted to corresponding JSON object.
      * 
      * @param value query argument value
      * @return reference to this object
      */
     public ViewQueryArguments addStartKey(List<?> value) {
         arguments.put("startkey", BasicUtils.objectToJsonString(value));
         
         return this;
     }
     
     /**
     * Adds <code>endkey</code> query argument.<p>
     * <code>Value</code> is converted to corresponding JSON object.
     * 
     * @param value query argument value
     * @return reference to this object
     */
    public ViewQueryArguments addEndKey(List<?> value) {
        arguments.put("endkey", BasicUtils.objectToJsonString(value));
        
        return this;
    }
    
    /**
      * Adds <code>include_docs</code> query argument.
      * 
      * @param value query argument value
      * @return reference to this object
      */
     public ViewQueryArguments addIncludeDocs(boolean value) {
         arguments.put("include_docs", Boolean.toString(value));
         
         return this;
     }
     
     /**
      * Adds <code>limit</code> query argument.
      * 
      * @param value query argument value
      * @return reference to this object
      */
     public ViewQueryArguments addLimit(long value) {
         arguments.put("limit", Long.toString(value));
         
         return this;
     }
     
 }
