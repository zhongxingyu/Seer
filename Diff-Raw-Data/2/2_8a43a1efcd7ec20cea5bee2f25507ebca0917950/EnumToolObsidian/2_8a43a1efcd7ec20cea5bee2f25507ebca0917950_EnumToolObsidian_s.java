 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode 
 
 package net.minecraft.src;
 
 public enum EnumToolObsidian
 {
    OBSIDIAN("OBSIDIAN", 5, 0, 2000, 10F, 3); 
 
     private final int harvestLevel;
     private final int maxUses;
     private final float efficiencyOnProperMaterial;
     private final int damageVsEntity;
 
     private EnumToolObsidian(String s, int i, int j, int k, float f, int l)
     {
         harvestLevel = j;
         maxUses = k;
         efficiencyOnProperMaterial = f;
         damageVsEntity = l;
     }
 
     public int getMaxUses()
     {
         return maxUses;
     }
 
     public float getEfficiencyOnProperMaterial()
     {
         return efficiencyOnProperMaterial;
     }
 
     public int getDamageVsEntity()
     {
         return damageVsEntity;
     }
 
     public int getHarvestLevel()
     {
         return harvestLevel;
     }
 }
