 package org.ow2.fractal.mind.ide.template;
 
 import org.ow2.fractal.mind.ide.emf.mindide.*;
 
 
 public class TemplatePrimitiveADL
  {
   protected static String nl;
   public static synchronized TemplatePrimitiveADL create(String lineSeparator)
   {
     nl = lineSeparator;
     TemplatePrimitiveADL result = new TemplatePrimitiveADL();
     nl = null;
     return result;
   }
 
   public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "primitive ";
  protected final String TEXT_2 = " {" + NL + " ";
   protected final String TEXT_3 = NL + "\t";
   protected final String TEXT_4 = NL + "  \tsource ";
   protected final String TEXT_5 = ".c;" + NL + "}";
 
    public String generate(MindAdl adl, String... contains)
   {
     final StringBuffer stringBuffer = new StringBuffer();
     
  String qualifiedName = adl.getQualifiedName();
  String cfile = adl.getName().substring(0,1).toLowerCase()+adl.getName().substring(1);
 
 
     stringBuffer.append(TEXT_1);
     stringBuffer.append(qualifiedName);
     stringBuffer.append(TEXT_2);
     for(String l : contains) {
     stringBuffer.append(TEXT_3);
     stringBuffer.append(l);
     }
     stringBuffer.append(TEXT_4);
     stringBuffer.append(cfile);
     stringBuffer.append(TEXT_5);
     return stringBuffer.toString();
   }
 }
