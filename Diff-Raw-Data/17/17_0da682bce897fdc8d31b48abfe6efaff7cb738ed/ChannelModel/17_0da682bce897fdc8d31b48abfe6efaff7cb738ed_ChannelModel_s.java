 package com.github.dreamrec.ads;
 
 /**
  * 
  */
 public class ChannelModel {

     protected int divider;
     protected String name;
     protected int hiPassBufferSize;
 
    public String getPhysicalDimension() {
        return "g";
    }
     public void setDivider(int divider) {
         this.divider = divider;
     }
 
     public int getDivider() {
         return divider;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
 
     public String getName() {
         return name;
     }
 
     public int getHiPassBufferSize() {
         return hiPassBufferSize;
     }
 
     public void setHiPassBufferSize(int hiPassBufferSize) {
         this.hiPassBufferSize = hiPassBufferSize;
     }
 }
