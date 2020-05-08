 package net.minecraft.src.redstoneExtended.Laser;
 
 import net.minecraft.src.MapColor;
 import net.minecraft.src.Material;
 import net.minecraft.src.MaterialTransparent;
 
 public class Materials {
     public static final Material laser;
 
     static {
        laser = new MaterialTransparent(MapColor.Air_color);
     }
 }
