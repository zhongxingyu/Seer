 package com.wolvencraft.prison.mines.mine;
 
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 import org.bukkit.material.MaterialData;
 
 import java.util.HashMap;
 import java.util.Map;
 
 @SerializableAs("MineBlock")
 public class MineBlock implements ConfigurationSerializable {
 
     private MaterialData block;
     private double chance;
 
     public MineBlock(MaterialData block, double chance) {
         this.block = block;
         this.chance = chance;
     }
 
     public MineBlock(Map<String, Object> me) {
         chance = (Double) me.get("chance");
         block = new MaterialData((Integer) me.get("blockId"), ((Integer) me.get("blockData")).byteValue());
     }
 
    public MaterialData getBlock() {
        return block;
    }

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

     public Map<String, Object> serialize() {
         Map<String, Object> me = new HashMap<String, Object>();
         me.put("chance", chance);
         me.put("blockId", block.getItemTypeId());
         me.put("blockData", block.getData());
         return me;
     }
     
    public MineBlock clone() {
    	return new MineBlock(block, chance);
    }
 }
