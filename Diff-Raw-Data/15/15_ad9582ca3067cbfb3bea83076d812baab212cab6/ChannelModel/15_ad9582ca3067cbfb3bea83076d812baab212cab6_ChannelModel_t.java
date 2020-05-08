 package com.github.dreamrec.ads;
 
 import com.github.dreamrec.HiPassPreFilter;
 
 /**
  * 
  */
 public class ChannelModel {
     private final String PHYSICAL_DIMENSION = "g";
     protected Divider divider;
     protected String name;
     protected HiPassPreFilter hiPassPreFilter;
     protected boolean isEnabled;
 
     public boolean isPositiveOk() {
         return isPositiveOk;
     }
 
     public void setPositiveOk(boolean positiveOk) {
         isPositiveOk = positiveOk;
     }
 
     public boolean isNegativeOk() {
         return isNegativeOk;
     }
 
     public void setNegativeOk(boolean negativeOk) {
         isNegativeOk = negativeOk;
     }
 
     protected boolean  isPositiveOk;   // is positive electrode good connected
     protected boolean isNegativeOk;   // is negative electrode good connected
 
     public HiPassPreFilter getHiPassPreFilter() {
         return hiPassPreFilter;
     }
 
     public int getIntDivider(){
         int intDivider = 0;
         if (isEnabled){
             intDivider = divider.getValue();
         }
         return intDivider;
 
     }
     
     public boolean isEnabled() {
         return isEnabled;
     }
 
     public void setEnabled(boolean enabled) {
         isEnabled = enabled;
     }
 
     public String getPhysicalDimension() {
         return PHYSICAL_DIMENSION;
     }
 
    public void setHiPassFilterFrequency(int channelFrequency, HiPassFrequency hiPassFrequency){
         hiPassPreFilter =  new HiPassPreFilter(channelFrequency, hiPassFrequency);
     }
 
     public HiPassFrequency getHiPassFilterFrequency() {
         return hiPassPreFilter.getCutOffFrequency();
     }
 
     public void setDivider(Divider divider) {
         this.divider = divider;
     }
 
     public Divider getDivider() {
         return divider;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 
     public String getName() {
         return name;
     }
 }
