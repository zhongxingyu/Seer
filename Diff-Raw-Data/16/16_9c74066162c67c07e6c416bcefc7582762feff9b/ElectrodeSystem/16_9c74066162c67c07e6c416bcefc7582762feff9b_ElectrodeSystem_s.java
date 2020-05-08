 package com.tihiy.reo;
 
 class ElectrodeSystem{
     static enum El {PLUS, MINUS}
 
     private double xShift = 0;
     private double yShift = 0;
     private double a;
     private double b;
 
     ElectrodeSystem(double a, double b ,double xShift, double yShift) {
         this.xShift = xShift;
         this.yShift = yShift;
         this.a = a;
         this.b = b;
     }
 
     ElectrodeSystem(double a, double b) {
         this.a = a;
         this.b = b;
     }
 
    public void setPosition(double x, double y){
        this.xShift = x;
        this.yShift = y;
     }
 
     public ReoPoint getElectrode(El el){
         if(el==El.MINUS){
             return new ReoPoint(xShift, yShift - a);
         }else{
             return new ReoPoint(xShift, yShift + a);
         }
     }
 }
