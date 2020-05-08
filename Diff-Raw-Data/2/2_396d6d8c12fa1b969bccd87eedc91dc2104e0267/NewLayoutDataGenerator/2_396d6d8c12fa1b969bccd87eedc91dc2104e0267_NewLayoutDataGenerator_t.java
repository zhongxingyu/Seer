 package org.eclipse.gmf.graphdef.codegen.templates;
 
 import org.eclipse.gmf.gmfgraph.*;
 import org.eclipse.gmf.graphdef.codegen.*;
 
 public class NewLayoutDataGenerator
 {
   protected static String nl;
   public static synchronized NewLayoutDataGenerator create(String lineSeparator)
   {
     nl = lineSeparator;
     NewLayoutDataGenerator result = new NewLayoutDataGenerator();
     nl = null;
     return result;
   }
 
   protected final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
   protected final String TEXT_1 = "";
   protected final String TEXT_2 = "\t\t\t";
   protected final String TEXT_3 = NL;
 
   public String generate(Object argument)
   {
     StringBuffer stringBuffer = new StringBuffer();
     
 // NOTE: this code expects that constrainted figure is already added to its parent
 
 GraphDefDispatcher.LayoutArgs argsBundle = (GraphDefDispatcher.LayoutArgs) argument;
 final GraphDefDispatcher dispatcher = argsBundle.getDispatcher();
 final LayoutData gmfLayoutData = argsBundle.getData();
 final Figure figureInstance = argsBundle.getFigure();
 
 // merely makes sure layoutData present
 // and parent figure got chance to have layout initialized 
 
if (gmfLayoutData != null && figureInstance.eContainer() instanceof Figure && ((Figure) figureInstance.eContainer()).getLayout() != null) {
     stringBuffer.append(TEXT_1);
     stringBuffer.append(dispatcher.dispatch(gmfLayoutData, argsBundle));
     stringBuffer.append(TEXT_2);
     }
     stringBuffer.append(TEXT_3);
     return stringBuffer.toString();
   }
 }
