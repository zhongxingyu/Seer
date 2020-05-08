 package com.bukkit.gemo.FalseBook.IC.ICs.worldedit;
 
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseChip;
 import com.bukkit.gemo.FalseBook.IC.ICs.BaseIC;
 import com.bukkit.gemo.FalseBook.IC.ICs.ICGroup;
 import com.bukkit.gemo.FalseBook.IC.ICs.InputState;
 import com.bukkit.gemo.utils.ICUtils;
 import com.bukkit.gemo.utils.SignUtils;
 import net.minecraft.server.EntityAnimal;
 import net.minecraft.server.EntityArrow;
 import net.minecraft.server.EntityPig;
 import net.minecraft.server.World;
 import org.bukkit.Location;
 import org.bukkit.block.Sign;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.util.Vector;
 
 public class ICEntityCannon extends BaseIC {
     public ICEntityCannon() {
         this.ICName = "ENTITY CANNON";
         this.ICNumber = "ic.entitycannon";
         setICGroup(ICGroup.WORLDEDIT);
         this.chipState = new BaseChip(true, false, false, "Clock", "", "");
         this.chipState.setOutputs("", "", "");
         this.chipState.setLines("", "");
         this.ICDescription = "";
     }
 
     public void checkCreation(SignChangeEvent event) {
         String type = event.getLine(1) + event.getLine(2);
         
         String speed = event.getLine(3);
         try {
             Float speedValue = Float.parseFloat(speed);
             if(speedValue < 1.0f) {
                 speedValue = 1.0f;
             }
             if(speedValue > 32.0f) {
                 speedValue = 32.0f;
             }
             event.setLine(3, speedValue.toString());
         } catch (NumberFormatException e) {
             SignUtils.cancelSignCreation(event, "Type is 2nd line. Speed is 3rd line.");
             return;
         }
         
         try {
             EntityType et = null;
             for(EntityType t : EntityType.values())
             {
                 if(type.compareToIgnoreCase(t.toString()) == 0) {
                     et = t;
                     break;
                 }
             }
             
            if(et == null) {
                 String types = "";
                 for(EntityType t : EntityType.values())
                 {
                     types = types + t.toString() + " ";
                 }
 
                 SignUtils.cancelSignCreation(event, "Valid types are: " + types);
                 return;
             }
         } catch (Exception e) {
             String types = "";
             for(EntityType t : EntityType.values())
             {
                 types = types + t.toString() + " ";
             }
                 
             SignUtils.cancelSignCreation(event, "Valid types are: " + types);
             return;
         }
     }
 
     public void Execute(Sign signBlock, InputState currentInputs, InputState previousInputs) {
         if ((currentInputs.isInputOneHigh()) && (previousInputs.isInputOneLow())) {
             int dir = SignUtils.getDirection(signBlock);
             
             Location location = ICUtils.getLeverPos(signBlock).clone();
             location.setX(location.getX() + 0.5d);
             location.setY(location.getY() + 0.5d);
             location.setZ(location.getZ() + 0.5d);
             
             String speed = signBlock.getLine(3);
             Float speedValue = Float.parseFloat(speed);
             
             Vector velocity = new Vector(0.0f, speedValue, 0.0f);
             if (dir == 1) {
                 velocity = new Vector(0.0f, 0.0f, -speedValue);
             } else if (dir == 2) {
                 velocity = new Vector(-speedValue, 0.0f, 0.0f);
             } else if (dir == 3) {
                 velocity = new Vector(0.0f, 0.0f, speedValue);
             } else if (dir == 4) {
                 velocity = new Vector(speedValue, 0.0f, 0.0f);
             }
 
             CraftWorld world = (CraftWorld)location.getWorld();
             
             String type = signBlock.getLine(1) + signBlock.getLine(2);
             
             EntityType et = null;
             for(EntityType t : EntityType.values())
             {
                 if(type.compareToIgnoreCase(t.toString()) == 0) {
                     et = t;
                     break;
                 }
             }
             
            final Entity ent = (Entity)world.spawnCreature(location, et);
            final Vector vel = velocity;
            ent.setVelocity(vel);
            
            this.core.getServer().getScheduler().scheduleSyncDelayedTask(this.core, new Runnable() {
			    public void run() {
			        ent.setVelocity(vel);
			    }
			}, 2);
         }
     }
 }
