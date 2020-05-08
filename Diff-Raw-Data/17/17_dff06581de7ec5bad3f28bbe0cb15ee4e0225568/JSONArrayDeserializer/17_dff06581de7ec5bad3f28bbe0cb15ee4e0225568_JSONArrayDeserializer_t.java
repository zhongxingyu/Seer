 /* *****************************************************************************
  * JSONArrayDeserializer.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
  * Use is subject to license terms.                                            *
  * J_LZ_COPYRIGHT_END *********************************************************/
 
 /*
  * Copyright 2001-2007 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openlaszlo.remote.json.soap.encoding;
 
 import org.apache.axis.encoding.DeserializationContext;
 import org.apache.axis.encoding.ser.ArrayDeserializer;
 import org.apache.axis.message.SOAPHandler;
 import org.openlaszlo.sc.ScriptCompiler;
 
 import java.util.ArrayList;
 import org.apache.log4j.Logger;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 public class JSONArrayDeserializer extends ArrayDeserializer
 {
     public static Logger mLogger =
         Logger.getLogger(JSONArrayDeserializer.class);
 
     // array item tag to use (in LFC, see valueToElement() in
     // data/LzDataElement.as)
     String mItemTag = null;
 
     /**
      * onStartChild is called on each child element.
      * @param namespace is the namespace of the child element
      * @param localName is the local name of the child element
      * @param prefix is the prefix used on the name of the child element
      * @param attributes are the attributes of the child element
      * @param context is the deserialization context.
      * @return is a Deserializer to use to deserialize a child (must be
      * a derived class of SOAPHandler) or null if no deserialization should
      * be performed.
      */
     public SOAPHandler onStartChild(String namespace,
                                     String localName,
                                     String prefix,
                                     Attributes attributes,
                                     DeserializationContext context)
         throws SAXException {
 
         if (mItemTag == null) mItemTag = localName;
 
         return super.onStartChild(namespace, localName, prefix, attributes, context);
 
     }
 
 
     /**
      * When valueComplete() is invoked on the array, 
      * first convert the array value into the expected array.
      * Then call super.valueComplete() to inform referents
      * that the array value is ready.
      **/
     public void valueComplete() throws SAXException
     { 
 
         /*
           // How to print a stack trace to the log:
           RuntimeException st = new RuntimeException();
           java.io.StringWriter sw = new java.io.StringWriter();
           java.io.PrintWriter ow = new java.io.PrintWriter(sw); 
           st.printStackTrace(ow);
           ow.flush();
           mLogger.debug(sw.toString());
         */
 
         if (componentsReady()) {
             try {
                 ArrayList list = (ArrayList)value;
                 StringBuffer body = new StringBuffer();
                 int alen=list.size();
                 int count = 0;
                body.append("[");
                 while (count < alen) {
                     body.append((String)(list.get(count)));
                     if (++count < alen) {
                         body.append(",");
                     }
                 }
 
                body.append("]");
                 value = body.toString();
 
                 mLogger.debug("JSONArrayDeserializer valueComplete value.class = "+value.getClass()+" value="+value);
 
             } catch (RuntimeException e) {
                 // We must ignore exceptions from convert for Arrays with null - why?
             }
         }     
         super.valueComplete();
     }
 }
