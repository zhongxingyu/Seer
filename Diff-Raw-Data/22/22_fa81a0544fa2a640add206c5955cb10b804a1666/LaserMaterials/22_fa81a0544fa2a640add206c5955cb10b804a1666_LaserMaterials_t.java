 package net.minecraft.src.lasers;
 
 import net.minecraft.src.MapColor;
 import net.minecraft.src.Material;
 import net.minecraft.src.MaterialTransparent;
 
 class LaserMaterials {
     public static final Material laser;
 
     static {
        laser = new MaterialTransparent(MapColor.airColor);
     }
 }
