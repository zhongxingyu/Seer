 package org.ow2.mindEd.ide.core.template;
 
import org.ow2.mindEd.ide.model.MindAdl;
 
 
 public class TemplateCompositeADL
  {
   protected static String nl;
   public static synchronized TemplateCompositeADL create(String lineSeparator)
   {
     nl = lineSeparator;
     TemplateCompositeADL result = new TemplateCompositeADL();
     nl = null;
     return result;
   }
 
   public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "composite ";
   protected final String TEXT_2 = " {" + NL + "  ";
   protected final String TEXT_3 = NL + "\t";
   protected final String TEXT_4 = NL + "}";
 
    public String generate(MindAdl adl, String... contains)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
  String qualifiedName = adl.getQualifiedName();
  
 
 
     stringBuffer.append(TEXT_1);
     stringBuffer.append(qualifiedName);
     stringBuffer.append(TEXT_2);
     for(String l : contains) {
     stringBuffer.append(TEXT_3);
     stringBuffer.append(l);
     }
     stringBuffer.append(TEXT_4);
     return stringBuffer.toString();
   }
 }
