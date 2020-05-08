 package com.tihiy.reo;
 
 
 import java.util.logging.Logger;
 
 public class SphereModel implements InterfaceModel {
 
     private static Logger log = Logger.getLogger(SphereModel.class.getName());
     private ElectrodeSystem electrodeSystem;
     private BodyGeometry bodyGeometry;
     private double roBlood = 1.35;
     private double roTissue = 7;
 
 
     public double potentialInPointFromElectrode(ReoPoint point, ReoPoint electrode){
         double potentialValue;
         double toPoint = toPoint(point);
         double toElectrode = toPoint(electrode);
         double cosinus = getCosinus(toPoint, toElectrode, fromPointToElectrode(point, electrode));
         potentialValue = roTissue/(2*Math.PI*toElectrode)*summaPoN(10, toPoint,toElectrode,cosinus);
 //        log.info("ToPoint = "+ toPoint + "\n" + "ToElectrode = " + toElectrode + "\n"+ "Cosinus = " + cosinus +
 //                "\n potentialValue" + potentialValue);
         return potentialValue;
     }
 
     private double toPoint(ReoPoint point){
         double value;
         double x = point.getX();
         double y = point.getY();
         double z = point.getZ();
         double r = bodyGeometry.getrSphere();
         double h = bodyGeometry.getH();
         value = Math.sqrt((r + h - z)*(r + h - z)+ y*y + x*x);
         return value;
     }
 
     private static double fromPointToElectrode(ReoPoint point, ReoPoint electrode){
         double value;
        value = Math.sqrt((electrode.getY() - point.getY())*(electrode.getY() - point.getY())+ point.getZ()*point.getZ());
         return value;
     }
 
     private static double getCosinus(double toPoint, double toElectrode, double toPointElectrode){
         double value;
         value = toPoint*toPoint+toElectrode*toElectrode - toPointElectrode*toPointElectrode;
         value /= (2 * toPoint * toElectrode);
         return value;
     }
 
     private double underSumm(int n, double toPoint, double toElectrode, double cosinus){
         double rSphera = bodyGeometry.getrSphere();
         double value = 1;
         for(int i = 0; i < n; i++){
             value *= rSphera*rSphera/(toElectrode*toPoint);
         }
         value *= rSphera/toPoint;
         value *= ((n*(roBlood - roTissue))/(n*(roBlood+roTissue)+roBlood));
         value *= legandr(n,cosinus);
 //        log.info("underSumm = "+ value);
         return value;
     }
 
     private static double legandr(int n, double cosinus){
         double element = 1;
         double elementPrePre = 1;
         double elementPre = cosinus;
         for(int i = 2; i <= n; i++){
             element = (2*i - 1)* cosinus *elementPre - (i - 1)*elementPrePre;
             element /= i;
             elementPrePre = elementPre;
             elementPre = element;
         }
         if(n==1){
             return cosinus;
         }
         return element;
     }
 
     private double summaPoN(int n, double toPoint, double toElectrode, double cosinus){
         double value = 0;
         for(int i = 0; i < n; i++){
             value += underSumm(i, toPoint, toElectrode, cosinus);
 //            log.info("element Sum = " + value);
         }
 //        log.info("SummaPoN = " + value);
         return value;
     }
 
     @Override
     public double getPotentialInPoint(ReoPoint point) {
         double potentialValue;
         // todo Now used only one of two electrode
         potentialValue = potentialInPointFromElectrode(point, electrodeSystem.getIElectrode(ElectrodeSystem.El.MINUS));
         potentialValue -= potentialInPointFromElectrode(point, electrodeSystem.getIElectrode(ElectrodeSystem.El.PLUS));
         return potentialValue;
     }
 
     @Override
     public void setElectrodeSystem(ElectrodeSystem electrodeSystem) {
         this.electrodeSystem = electrodeSystem;
     }
     @Override
     public void setBodyGeometry(BodyGeometry bodyGeometry) {
         this.bodyGeometry = bodyGeometry;
     }
 
     public void setRoBlood(double roBlood) {
         this.roBlood = roBlood;
     }
 
     public void setRoTissue(double roTissue) {
         this.roTissue = roTissue;
     }
 
     public void setRo(double roTissue, double roBlood){
         this.roBlood = roBlood;
         this.roTissue = roTissue;
     }
 }
