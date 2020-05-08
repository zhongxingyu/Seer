 package com.hifiremote.jp1;
 
 public class DeviceType
 {
  DeviceType( String aName, int number, int aMap, int aType )
   {
     this.name = aName;
    this.number = number;
     this.map = aMap;
     this.type = aType;

   }
 
   public String toString(){ return name; }
   public String getName(){ return name; }
  public int getNumber(){ return number; }
   public int getMap(){ return map; }
   public int getType(){ return type; }
   public DeviceType setButtonMap( ButtonMap buttonMap )
   {
     this.buttonMap = buttonMap;
     return this;
   }
   public ButtonMap getButtonMap()
   {
     return buttonMap;
   }
 
   public boolean isMapped( Button b )
   {
     return buttonMap.isPresent( b );
   }
 
   private String name;
  private int number;
   private int map;
   private int type;
   private ButtonMap buttonMap = null;
 }
