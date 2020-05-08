 package com.github.dreamrec.ads;
 
 /**
  *
  */
 public class AdsChannelModel extends ChannelModel {
 
    public String PHYSICAL_DIMENSION = "uV";
     private Gain gain;
     private CommutatorState commutatorState;
     protected boolean isLoffEnable;
     private boolean isRldSenseEnabled;   // DRL
     private int rldSenseEnabledBits;
     private int loffSenseEnabledBits;
     private int loffFlipBits;
 
     public void setLoffFlipBits(int loffFlipBits) {
         this.loffFlipBits = loffFlipBits;
     }
 
     @Override
     public void setDivider(int divider) {
         this.divider = divider;
         if (divider == 0) {
             commutatorState = CommutatorState.INPUT_SHORT;
             isLoffEnable = false;
             isRldSenseEnabled = false;
         }
     }
 
     public boolean isRldSenseEnabled() {
         return isRldSenseEnabled;
     }
 
     public void setRldSenseEnabled(boolean rldSenseEnabled) {
         isRldSenseEnabled = rldSenseEnabled;
     }
 
 
     public Gain getGain() {
         return gain;
     }
 
     public void setGain(Gain gain) {
         this.gain = gain;
     }
 
     public CommutatorState getCommutatorState() {
         return (divider == 0) ? CommutatorState.INPUT_SHORT : commutatorState;
     }
 
     public void setCommutatorState(CommutatorState commutatorState) {
         this.commutatorState = commutatorState;
     }
 
     public boolean isLoffEnable() {
         return isLoffEnable;
     }
 
     public void setLoffEnable(boolean loffEnable) {
         isLoffEnable = loffEnable;
     }
 
     public int enabledBit() {
         return divider == 0 ? 0x80 : 0;
     }
 
     public int getRldSenseEnabledBits() {
         return (isRldSenseEnabled & (divider != 0)) ? rldSenseEnabledBits : 0;
     }
 
     public void setRldSenseEnabledBits(int rldSenseEnabledBits) {
         this.rldSenseEnabledBits = rldSenseEnabledBits;
     }
 
     public int getLoffSenseEnabledBits() {
         if (divider == 0) {
             return 0;
         } else{
             return isLoffEnable ? loffSenseEnabledBits : 0;
         }
     }
 
     public void setLoffSenseEnabledBits(int loffSenseEnabledBits) {
         this.loffSenseEnabledBits = loffSenseEnabledBits;
     }
 }
