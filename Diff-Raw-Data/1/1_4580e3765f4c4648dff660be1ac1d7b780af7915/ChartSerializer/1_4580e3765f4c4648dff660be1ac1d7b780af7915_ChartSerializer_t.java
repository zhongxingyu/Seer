 package org.pentaho.chart.model.util;
 
 import org.pentaho.chart.model.ChartModel;
 import org.pentaho.chart.model.GraphPlot;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 import org.pentaho.platform.plugin.action.chartbeans.ChartDataDefinition;
 
 public class ChartSerializer {
   private static XStream chartWriter = new XStream(new JettisonMappedXmlDriver());
   
   private static XStream chartDefWriter = new XStream(new JettisonMappedXmlDriver());
   
   static{
     chartWriter.alias("ChartModel", ChartModel.class); //$NON-NLS-1$
     
     chartWriter.setMode(XStream.NO_REFERENCES);
     chartWriter.useAttributeFor(GraphPlot.class, "categoryAxisLabel"); //$NON-NLS-1$
     chartWriter.useAttributeFor(ChartModel.class, "theme"); //$NON-NLS-1$
     chartWriter.useAttributeFor(ChartModel.class, "chartEngine"); //$NON-NLS-1$
    chartWriter.useAttributeFor(ChartModel.class, "animate"); //$NON-NLS-1$
     
 
     chartDefWriter.setMode(XStream.NO_REFERENCES);
     chartDefWriter.alias("ChartDataModel", ChartDataDefinition.class); //$NON-NLS-1$
     
   }
   public static String serialize(ChartModel model){
     return chartWriter.toXML(model);
   }
   
   public static ChartModel deSerialize(String input){
     return (ChartModel) chartWriter.fromXML(input);
   }
   
   
   public static String serializeDataDefinition(ChartDataDefinition def){
     return chartDefWriter.toXML(def);
     
   }
   
   public static ChartDataDefinition deSerializeDataDefinition(String input){
     return (ChartDataDefinition) chartDefWriter.fromXML(input);
     
   }
 }
