 package com.fing.pis.bizativiti.plugin.xpdl;
 
 import java.util.ArrayList;
 
 import org.jsoup.Jsoup;
 import org.wfmc._2009.xpdl2.Activity;
 import org.wfmc._2009.xpdl2.Description;
 import org.wfmc._2009.xpdl2.Lane;
 import org.wfmc._2009.xpdl2.Loop;
 import org.wfmc._2009.xpdl2.NodeGraphicsInfo;
 import org.wfmc._2009.xpdl2.NodeGraphicsInfos;
 import org.wfmc._2009.xpdl2.PackageType;
 import org.wfmc._2009.xpdl2.Pool;
 import org.wfmc._2009.xpdl2.Pools;
 import org.wfmc._2009.xpdl2.StartEvent;
 import org.wfmc._2009.xpdl2.TriggerResultMessage;
 import org.wfmc._2009.xpdl2.TriggerTimer;
 
 import com.fing.pis.bizativiti.common.metamodel.MetamodelTask.LoopType;
 
 /**
  * Clase con métodos auxiliares utilizados por los translators
  */
 public class Util {
 
     private Util() {}
 
     public static String getDescription(Activity activity) {
         for (Object child : activity.getContent()) {
             if (child instanceof Description) {
                 return ((Description) child).getValue();
             }
         }
         //No importa si no hay descripcion!!! creooo        
         //throw new RuntimeException("Description not found in activity");
         return "";
     }
 
     public static String getId(Activity activity) {
         return activity.getId();
     }
 
     public static String getName(Activity activity) {
         return activity.getName();
     }
 
     //------
 
     public static String getTriggerAttrTimer(StartEvent event) {
         TriggerTimer trigger = event.getTriggerTimer();
         String sTriggerAttr = null;
         if (trigger.getTimeDateAttr() != null) {
             sTriggerAttr = trigger.getTimeDateAttr();
         } else if (trigger.getTimeCycleAttr() != null) {
             sTriggerAttr = trigger.getTimeCycleAttr();
         } else {
             throw new RuntimeException("No attributes inside TriggerTimer tag");
         }
         return sTriggerAttr;
     }
 
     public static String getTriggerType(StartEvent event) {
         TriggerTimer trigger = event.getTriggerTimer();
         String sTriggerType = null;
         if (trigger.getTimeDateAttr() != null) {
             sTriggerType = "TimeDate";
         } else if (trigger.getTimeCycleAttr() != null) {
             sTriggerType = "TimeCycle";
         } else {
             throw new RuntimeException("No attributes inside TriggerTimer tag");
         }
         return sTriggerType;
     }
 
     public static String getMessageId(StartEvent event) {
         TriggerResultMessage triggerResult = event.getTriggerResultMessage();
         if (triggerResult != null) {
             if (triggerResult.getMessage() != null) {
                 return triggerResult.getMessage().getId();
             } else {
                 throw new RuntimeException("No attributes inside Message tag");
             }
         } else {
             throw new RuntimeException("No attributes inside TriggerResultMessage tag");
         }
     }
 
     public static double getX(Activity activity) {
         if (activity.getContent() != null) {
             for (Object o : activity.getContent()) {
                 if (o instanceof NodeGraphicsInfos) {
                     NodeGraphicsInfos nodeGraph = (NodeGraphicsInfos) o;
                     ArrayList<NodeGraphicsInfo> node = (ArrayList<NodeGraphicsInfo>) nodeGraph.getNodeGraphicsInfo();
                     return node.get(0).getCoordinates().getXCoordinate();
                 }
             }
             throw new RuntimeException("No content inside activity tag");
         }
         throw new RuntimeException("No NodeGraphics inside activity tag");
     }
 
     public static double getY(Activity activity) {
         if (activity.getContent() != null) {
             for (Object o : activity.getContent()) {
                 if (o instanceof NodeGraphicsInfos) {
                     NodeGraphicsInfos nodeGraph = (NodeGraphicsInfos) o;
                     ArrayList<NodeGraphicsInfo> node = (ArrayList<NodeGraphicsInfo>) nodeGraph.getNodeGraphicsInfo();
                     return node.get(0).getCoordinates().getYCoordinate();
                 }
             }
             throw new RuntimeException("No content inside activity tag");
         }
         throw new RuntimeException("No NodeGraphics inside activity tag");
     }
 
     public static String getLaneId(Activity activity) {
         if (activity.getContent() != null) {
             for (Object o : activity.getContent()) {
                 if (o instanceof NodeGraphicsInfos) {
                     NodeGraphicsInfos nodeGraph = (NodeGraphicsInfos) o;
                     ArrayList<NodeGraphicsInfo> node = (ArrayList<NodeGraphicsInfo>) nodeGraph.getNodeGraphicsInfo();
                     return node.get(0).getLaneId();
                 }
             }
             throw new RuntimeException("No content inside activity tag");
         }
         throw new RuntimeException("No NodeGraphics inside activity tag");
     }
 
     public static double getHeight(Activity activity) {
         if (activity.getContent() != null) {
             for (Object o : activity.getContent()) {
                 if (o instanceof NodeGraphicsInfos) {
                     NodeGraphicsInfos nodeGraph = (NodeGraphicsInfos) o;
                     ArrayList<NodeGraphicsInfo> node = (ArrayList<NodeGraphicsInfo>) nodeGraph.getNodeGraphicsInfo();
                     return node.get(0).getHeight();
                 }
             }
             throw new RuntimeException("No content inside activity tag");
         }
         throw new RuntimeException("No NodeGraphics inside activity tag");
     }
 
     public static double getWidth(Activity activity) {
         if (activity.getContent() != null) {
             for (Object o : activity.getContent()) {
                 if (o instanceof NodeGraphicsInfos) {
                     NodeGraphicsInfos nodeGraph = (NodeGraphicsInfos) o;
                     ArrayList<NodeGraphicsInfo> node = (ArrayList<NodeGraphicsInfo>) nodeGraph.getNodeGraphicsInfo();
                     return node.get(0).getWidth();
                 }
             }
             throw new RuntimeException("No content inside activity tag");
         }
         throw new RuntimeException("No NodeGraphics inside activity tag");
     }
 
     public static LoopType getLoopType(Activity activity) {
         LoopType result = LoopType.None;
         for (Object child : activity.getContent()) {
             if (child instanceof Loop) {
                 Loop l = (Loop) child;
                 if ("None".equals(l.getLoopType())) {
                     result = LoopType.None;
                 } else if ("Multi".equals(l.getLoopType())) {
                     result = LoopType.Multi;
                 } else if ("Standard".equals(l.getLoopType())) {
                     result = LoopType.Standard;
                 }
                 break;
             }
         }
         return result;
     }
 
     public static String getLaneName(Activity activity, PackageType pType) {
 
         String result = "Default_Bizativiti_Lane";
         double y = getY(activity);
         double position = y + getHeight(activity) / 2;
 
         Pools pools = pType.getPools();
         for (Pool p : pools.getPool()) {
             for (Lane lane : p.getLanes().getLane()) {
                 for (NodeGraphicsInfo ngInfo : lane.getNodeGraphicsInfos().getNodeGraphicsInfo()) {
                     double alto = ngInfo.getHeight();
                     double desde = ngInfo.getCoordinates().getYCoordinate();
                     if (position >= desde && position < (alto + desde)) {
                         result = lane.getName();
                         break;
                     }
                 }
             }
         }
 
         return result;
     }
 
     /* Parsea un String html y obtiene solo el texto, se usa para las descriptions sacarle el estilo*/
     public static String getPlainTextFromHTML(String html) {
         String htmlString = Jsoup.parse(html).text();
         return htmlString.replaceAll("\\<.*?>", "");
     }

 }
