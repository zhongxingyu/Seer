 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: Transform.java,v 1.2 2003-11-14 19:45:09 shahid.shah Exp $
  */
 
 package com.netspective.commons.text;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Iterator;
 import java.io.Writer;
 import java.io.IOException;
 import java.io.File;
 
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.io.Resource;
 
 public class Transform
 {
     private static final Log log = LogFactory.getLog(Transform.class);
 
     public static class StyleSheetParameter
     {
         private String name;
         private ValueSource value;
 
         public StyleSheetParameter()
         {
         }
 
         public String getName()
         {
             return name;
         }
 
         public void setName(String name)
         {
             this.name = name;
         }
 
         public ValueSource getValue()
         {
             return value;
         }
 
         public void setValue(ValueSource value)
         {
             this.value = value;
         }
     }
 
     public static class SystemProperty
     {
         private String name;
         private ValueSource value;
 
         public SystemProperty()
         {
         }
 
         public String getName()
         {
             return name;
         }
 
         public void setName(String name)
         {
             this.name = name;
         }
 
         public ValueSource getValue()
         {
             return value;
         }
 
         public void setValue(ValueSource value)
         {
             this.value = value;
         }
     }
 
     private Class relativeToClass;
     private boolean styleSheetIsFile;
     private boolean sourceIsFile;
     private ValueSource styleSheet;
     private ValueSource source;
     private List systemProperties = new ArrayList();
     private List params = new ArrayList();
 
     public Transform()
     {
     }
 
     public List getParams()
     {
         return params;
     }
 
     public StyleSheetParameter createParam()
     {
         return new StyleSheetParameter();
     }
 
     public void addParam(StyleSheetParameter param)
     {
         params.add(param);
     }
 
     public SystemProperty createSystemProperty()
     {
         return new SystemProperty();
     }
 
     public void addSystemProperty(SystemProperty param)
     {
         systemProperties.add(param);
     }
 
     public boolean isSourceIsFile()
     {
         return sourceIsFile;
     }
 
     public void setSourceIsFile(boolean sourceIsFile)
     {
         this.sourceIsFile = sourceIsFile;
     }
 
     public boolean isStyleSheetIsFile()
     {
         return styleSheetIsFile;
     }
 
     public void setStyleSheetIsFile(boolean styleSheetIsFile)
     {
         this.styleSheetIsFile = styleSheetIsFile;
     }
 
     public ValueSource getSource()
     {
         return source;
     }
 
     public void setSourceResource(ValueSource source)
     {
         this.source = source;
         setSourceIsFile(false);
     }
 
     public void setSourceFile(ValueSource source)
     {
         setSourceResource(source);
         setSourceIsFile(true);
     }
 
     public ValueSource getStyleSheet()
     {
         return styleSheet;
     }
 
     public void setStyleSheetResource(ValueSource styleSheet)
     {
         this.styleSheet = styleSheet;
         setStyleSheetIsFile(false);
     }
 
     public void setStyleSheetFile(ValueSource styleSheet)
     {
         setStyleSheetResource(styleSheet);
         setStyleSheetIsFile(true);
     }
 
     public Class getRelativeToClass()
     {
         return relativeToClass;
     }
 
     public void setRelativeToClass(Class relativeToClass)
     {
         this.relativeToClass = relativeToClass;
     }
 
     public StreamSource getStreamSource(ValueSource vs, ValueContext vc, boolean isFile)
     {
         String sourceValue = vs.getTextValue(vc);
         if(isFile)
             return new StreamSource(new File(sourceValue));
         else
         {
             Resource resource =
                 relativeToClass == null ?
                     new Resource(this.getClass().getClassLoader(), sourceValue) :
                     new Resource(relativeToClass, sourceValue);
             return new StreamSource(resource.getResourceAsStream());
         }
     }
 
    public void render(Writer writer, ValueContext vc, Map additionalParams) throws IOException
     {
         render(writer, vc, null, additionalParams, true);
     }
 
    public boolean render(Writer writer, ValueContext vc, Source transformSource, Map additionalParams, boolean writeErrors) throws IOException
     {
         if(source == null && transformSource == null)
         {
             if(writeErrors)
                 writer.write("No source attribute provided.");
             log.error("No source attribute provided for " + this);
             return false;
         }
 
         if(styleSheet == null)
         {
             if(writeErrors)
                 writer.write("No style-sheet attribute provided.");
             log.error("No style-sheet attribute provided for " + this);
             return false;
         }
 
         try
         {
             Map savedProps = new HashMap();
             for(int i = 0; i < systemProperties.size(); i++)
             {
                 SystemProperty prop = (SystemProperty) systemProperties.get(i);
                 String currentValue = System.getProperty(prop.getName());
                 if(currentValue != null)
                     savedProps.put(prop.getName(), currentValue);
                 System.setProperty(prop.getName(), prop.getValue().getTextValue(vc));
             }
 
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer(getStreamSource(styleSheet, vc, styleSheetIsFile));
 
             for(Iterator i = savedProps.entrySet().iterator(); i.hasNext(); )
             {
                 Map.Entry entry = (Map.Entry) i.next();
                 System.setProperty((String) entry.getKey(), (String) entry.getValue());
             }
 
             List params = getParams();
             for(int i = 0; i < params.size(); i++)
             {
                 StyleSheetParameter param = (StyleSheetParameter) params.get(i);
                 transformer.setParameter(param.getName(), param.getValue().getTextValue(vc));
             }
 
             if(additionalParams != null)
             {
                 for(Iterator i = additionalParams.entrySet().iterator(); i.hasNext(); )
                 {
                     Map.Entry entry = (Map.Entry) i.next();
                     transformer.setParameter(entry.getKey().toString(), entry.getValue());
                 }
             }
 
             transformer.transform(transformSource != null ? transformSource : getStreamSource(source, vc, sourceIsFile),
                                   new StreamResult(writer));
 
             return true;
         }
         catch(TransformerConfigurationException e)
         {
             if(writeErrors)
                 writer.write("<pre>"+ TextUtils.getStackTrace(e) +"</pre>");
            log.error("XSLT error in " + this.getClass().getName(), e);
            return false;
         }
         catch(TransformerException e)
         {
             if(writeErrors)
                 writer.write("<pre>"+ TextUtils.getStackTrace(e) +"</pre>");
            log.error("XSLT error in " + this.getClass().getName(), e);
            return false;
         }
     }
 }
 
