 package ru.spbau.bioinf.tagfinder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Peak implements Comparable<Peak>{
     private double value;
     private double intensity;
     private int charge;
 
     private int componentId;
 
     private List<Peak> next = new ArrayList<Peak>();
 
    private boolean isProcessed = false;
 
     public Peak(double value, double intensity, int charge) {
         this.value = value;
         this.intensity = intensity;
         this.charge = charge;
     }
 
     public double getValue() {
         return value;
     }
 
     public double getIntensity() {
         return intensity;
     }
 
     public int getCharge() {
         return charge;
     }
 
    public boolean isProcessed() {
        return isProcessed;
     }
 
    public void setProcessed(boolean processed) {
        isProcessed = processed;
     }
 
     public int getComponentId() {
         return componentId;
     }
 
     public void setComponentId(int componentId) {
         if (componentId != this.componentId) {
             this.componentId = componentId;
             for (Peak peak : next) {
                 peak.setComponentId(componentId);
             }
         }
     }
 
     public void addNext(Peak peak) {
         next.add(peak);
         if (peak.getComponentId() != componentId) {
             doUpdateComponentId(peak);
         }
     }
 
     public List<Peak> getNext() {
         return next;
     }
 
     private void doUpdateComponentId(Peak peak) {
         int minComponentId = Math.min(componentId, peak.getComponentId());
         setComponentId(minComponentId);
         peak.setComponentId(minComponentId);
     }
 
     public boolean updateComponentId() {
         for (Peak peak : next) {
             if (peak.getComponentId() != componentId) {
                 doUpdateComponentId(peak);
                 return true;
             }
         }
         return false;
     }
 
     public double diff(Peak prev) {
         return value - prev.getValue();
     }
 
     @Override
     public int compareTo(Peak other) {
         if (other.getValue() < value) {
             return 1;
         } else if (other.getValue() > value) {
             return -1;
         }
         return 0;
     }
 }
