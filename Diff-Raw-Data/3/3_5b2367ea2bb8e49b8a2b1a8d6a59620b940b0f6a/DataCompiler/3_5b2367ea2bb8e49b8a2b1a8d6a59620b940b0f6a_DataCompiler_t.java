 /* *****************************************************************************
  * DataCompiler.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import org.openlaszlo.auth.AuthenticationException;
 import org.openlaszlo.iv.flash.api.action.*;
 import java.io.*;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.jdom.Element;
 import org.jdom.Text;
 import org.jdom.Content;
 import org.jdom.JDOMException;
 import org.openlaszlo.xml.internal.XMLUtils;
 
 /** Compiler for local data elements.
  *
  * @author  Henry Minsky
  * @author  Oliver Steele
  */
 class DataCompiler extends ElementCompiler {
 
     /* TODO [hqm 2007-07] This is for top level datasets only. The function in the LFC,
      * lzAddLocalData, creates the dataset
      * *immediately*, it is not queued for instantiation. This happens to
      * allow forward references to datasets in LZX code. It also happens to slow
      * down initialization of an app if it has large static datasets. This could be
      * made better by queuing the data for quantized lzIdle processing, although it
      * would mean delaying the "ondata" of the datasets until they were processed. 
      */
 
     static final String LOCAL_DATA_FNAME = "lzAddLocalData";
 
     DataCompiler(CompilationEnvironment env) {
         super(env);
     }
 
     static boolean isElement(Element element) {
         if (element.getName().equals("dataset")) {
             // return type in ('soap', 'http') or src is url
             String src = element.getAttributeValue("src");
             String type = element.getAttributeValue("type");
             if (type != null && (type.equals("soap") || type.equals("http"))) {
                 return false;
             }
             if (src != null && src.indexOf("http:") == 0) {
                 return false;
             }
             return src == null || !XMLUtils.isURL(src);
         }
         return false;
     }
     
     public void compile(Element element) {
         String dsetname = XMLUtils.requireAttributeValue(element, "name");
         boolean trimwhitespace = "true".equals(element.getAttributeValue("trimwhitespace"));
         String content = NodeModel.getDatasetContent(element, mEnv, trimwhitespace);
        mEnv.compileScript("var "+dsetname+";");
        mEnv.compileScript(dsetname + "= "+LOCAL_DATA_FNAME+"("+ScriptCompiler.quote(dsetname) + ", " +content+
                            "," + trimwhitespace+");\n");
     }
 
 }
