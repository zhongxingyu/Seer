 //Copyright (C) 2011-2013 Chris Price (xCP23x)
 //This software uses the GNU GPL v2 license
 //See http://github.com/xCP23x/RestockIt/blob/master/README and http://github.com/xCP23x/RestockIt/blob/master/LICENSE for details
 
 package org.cp23.restockit;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 class RIperm extends RestockIt {
     
     private Boolean isCreated=false, isOpened=false, isDestroyed=false, isBlacklistBypass=false;
     private Block block=null;
     private String line=null;
     private Player player=null;
     
     public RIperm(Block bl, Player pl, String ln){
         block = bl;
         player = pl;
         line = ln;
     }
     
     public RIperm(Block bl, Player pl, Block sgn){
         block = bl;
         player = pl;
         line = ((Sign)sgn.getState()).getLine(2);
     }
     
     public void setCreated(){
         isCreated=true;
         isOpened=false;
         isDestroyed=false;
         isBlacklistBypass=false;
     }
     
     public void setOpened(){
         isCreated=false;
         isOpened=true;
         isDestroyed=false;
         isBlacklistBypass=false;
     }
     
     public void setDestroyed(){
         isCreated=false;
         isOpened=false;
         isDestroyed=true;
         isBlacklistBypass=false;
     }
     
     public void setBlacklistBypass(){
         isCreated=false;
         isOpened=false;
         isDestroyed=false;
         isBlacklistBypass=true;
     }
     
     private String getType(){
         if(isCreated) return ".create";
         if(isOpened) return ".open";
         if(isDestroyed) return ".destroy";
         return null;
     }
     
     public Player getPlayer(){
         return player;
     }
     
     public String getPerm(){
         String pre = "restockit.";
         String type = getType();
         
         if(isBlacklistBypass) return pre + "blacklist.bypass";
         
         return pre + getBlockType() + type;
     }
     
     public String getDeprecatedPerm(){
         String pre = "restockit.";
         String type = getType();
        if(type==null) return null;
         
         if(isInList(block.getType(), listType.SINGLE)) return pre + "chest" + type;
         if(isInList(block.getType(), listType.DOUBLE)) return pre + "chest" + type;
         return getPerm();
     }
     
     public String getBlockType(){
         if(line.equalsIgnoreCase("incinerator")) return "incinerator";
         if(isInList(block.getType(), listType.DISPENSERS)) return "dispenser";
         if(isContainer(block.getType())) return "container";
         return block.getType().toString().toLowerCase();
     }
     
 }
