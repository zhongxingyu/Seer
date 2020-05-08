 /*******************************************************************************
  *
  * Copyright (c) 2004-2010 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors: 
  *
  *    Kohsuke Kawaguchi
  *     
  *******************************************************************************/ 
 
 package org.kohsuke.stapler.export;
 
 import org.kohsuke.stapler.StaplerResponse;
 
 import java.util.Stack;
 import java.io.Writer;
 import java.io.IOException;
 import java.beans.Introspector;
 
 /**
  * Writes XML.
  *
  * @author Kohsuke Kawaguchi
  */
 final class XMLDataWriter implements DataWriter {
 
     private String name;
     private final Stack<String> objectNames = new Stack<String>();
     private final Stack<Boolean> arrayState = new Stack<Boolean>();
     private final Writer out;
     public boolean isArray;
 
     XMLDataWriter(Object bean, Writer out) throws IOException {
         Class c=bean.getClass();
         while (c.isAnonymousClass())
             c = c.getSuperclass();
         name = Introspector.decapitalize(c.getSimpleName());
         this.out = out;
     }
 
     XMLDataWriter(Object bean, StaplerResponse rsp) throws IOException {
         this(bean,rsp.getWriter());
     }
 
     public void name(String name) {
         this.name = name;
     }
 
     public void valuePrimitive(Object v) throws IOException {
         value(v.toString());
     }
 
     public void value(String v) throws IOException {
         String n = adjustName();
         out.write('<'+n+'>');
         out.write(escape(v));
         out.write("</"+n+'>');
     }
 
     private String escape(String v) {
         StringBuffer buf = new StringBuffer(v.length()+64);
         for( int i=0; i<v.length(); i++ ) {
             char ch = v.charAt(i);
             if(ch=='<')
                 buf.append("&lt;");
             else
             if(ch=='>')
                 buf.append("&gt;");
             else
             if(ch=='&')
                 buf.append("&amp;");
             else
                 buf.append(ch);
         }
         return buf.toString();
     }
 
     public void valueNull() {
         // use absence to indicate null.
     }
 
     public void startArray() {
         // use repeated element to display array
         // this means nested arrays are not supported
         isArray = true;
     }
 
     public void endArray() {
         isArray = false;
     }
 
     public void startObject() throws IOException {
         objectNames.push(name);
         out.write('<'+adjustName()+'>');
         arrayState.push(isArray);
         isArray = false;
     }
 
     public void endObject() throws IOException {
         name = objectNames.pop();
         isArray = arrayState.pop();
         out.write("</"+adjustName()+'>');
     }
 
     /**
      * Returns the name to be used as an element name
      * by considering {@link #isArray}
      */
     private String adjustName() {
        String escaped = makeXmlName(name);
        if(isArray) return toSingular(escaped);
        return escaped;
     }
 
     /*package*/ static String toSingular(String name) {
         if(name.endsWith("s"))
             return name.substring(0,name.length()-1);
         return name;
     }

    /*package*/ static String makeXmlName(String name) {
        if (name.length()==0)   name="_";

        if (!XmlChars.isNameStart(name.charAt(0))) {
            if (name.length()>1 && XmlChars.isNameStart(name.charAt(1)))
                name = name.substring(1);
            else
                name = '_'+name;
        }

        int i=1;
        while (i<name.length()) {
            if (XmlChars.isNameChar(name.charAt(i)))
                i++;
            else
                name = name.substring(0,i)+name.substring(i+1);
        }

        return name;
    }
 }
