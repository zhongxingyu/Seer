 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.server.Block;
 import net.minecraft.server.EntityChicken;
 import net.minecraft.server.EntityCow;
 import net.minecraft.server.EntityCreeper;
 import net.minecraft.server.EntityEnderman;
 import net.minecraft.server.EntityPig;
 import net.minecraft.server.EntitySheep;
 import net.minecraft.server.EntitySkeleton;
 import net.minecraft.server.EntitySlime;
 import net.minecraft.server.EntitySpider;
 import net.minecraft.server.EntitySquid;
 import net.minecraft.server.EntityZombie;
 import net.minecraft.server.EnumCreatureType;
 import net.minecraft.server.World;
 
 public class BiomeBase extends net.minecraft.server.BiomeBase {
     public static final BiomeBase[] a = new BiomeBase[256];
 
     public static final BiomeBase OCEAN = new BiomeOcean(0).b(112).a("Ocean").b(-1.0F, 0.4F);
     public static final BiomeBase PLAINS = new BiomePlains(1).b(9286496).a("Plains").a(0.8F, 0.4F);
    public static final BiomeBase DESERT = new BiomeDesert(2).b(16421912).a("Desert").a(2.0F, 0.0F).b(0.1F, 0.2F);
//    public static final BiomeBase DESERT = new BiomeDesert(2).b(16421912).a("Desert").g().a(2.0F, 0.0F).b(0.1F, 0.2F);
     public static final BiomeBase EXTREME_HILLS = new BiomeBigHills(3).b(6316128).a("Extreme Hills").b(0.2F, 1.8F).a(0.2F, 0.3F);
     public static final BiomeBase FOREST = new BiomeForest(4).b(353825).a("Forest").a(5159473).a(0.7F, 0.8F);
     public static final BiomeBase TAIGA = new BiomeTaiga(5).b(747097).a("Taiga").a(5159473).a(0.3F, 0.8F).b(0.1F, 0.4F);
     public static final BiomeBase SWAMPLAND = new BiomeSwamp(6).b(522674).a("Swampland").a(9154376).b(-0.2F, 0.1F).a(0.8F, 0.9F);
     public static final BiomeBase RIVER = new BiomeRiver(7).b(255).a("River").b(-0.5F, 0.0F);
 //    public static final BiomeBase HELL = new BiomeHell(8).b(16711680).a("Hell").g().a(2.0F, 0.0F);
 //    public static final BiomeBase SKY = new BiomeTheEnd(9).b(8421631).a("Sky").g();
     public static final BiomeBase FROZEN_OCEAN = new BiomeOcean(10).b(9474208).a("FrozenOcean").b(-1.0F, 0.5F).a(0.0F, 0.5F);
     public static final BiomeBase FROZEN_RIVER = new BiomeRiver(11).b(10526975).a("FrozenRiver").b(-0.5F, 0.0F).a(0.0F, 0.5F);
     public static final BiomeBase ICE_PLAINS = new BiomeIcePlains(12).b(16777215).a("Ice Plains").a(0.0F, 0.5F);
     public static final BiomeBase ICE_MOUNTAINS = new BiomeIcePlains(13).b(10526880).a("Ice Mountains").b(0.2F, 1.8F).a(0.0F, 0.5F);
     public static final BiomeBase MUSHROOM_ISLAND = new BiomeMushrooms(14).b(16711935).a("MushroomIsland").a(0.9F, 1.0F).b(0.2F, 1.0F);
     public static final BiomeBase MUSHROOM_SHORE = new BiomeMushrooms(15).b(10486015).a("MushroomIslandShore").a(0.9F, 1.0F).b(-1.0F, 0.1F);
 
 //    public String r;
 //    public int s;
 //    public byte t = (byte)Block.GRASS.id;
 //    public byte u = (byte)Block.DIRT.id;
 //    public int v = 5169201;
 //    public float w = 0.1F;
 //    public float x = 0.3F;
 //    public float y = 0.5F;
 //    public float z = 0.5F;
 //    public int A = 16777215;
     public BiomeDecorator B;
     @SuppressWarnings({ "unchecked", "rawtypes" })
     protected List<BiomeMeta> C = new ArrayList();
     @SuppressWarnings({ "unchecked", "rawtypes" })
     protected List<BiomeMeta> D = new ArrayList();
     @SuppressWarnings({ "unchecked", "rawtypes" })
     protected List<BiomeMeta> E = new ArrayList();
 //    private boolean K;
 //    private boolean L = true;
 //    public final int F;
     protected WorldGenTrees G = new WorldGenTrees(false);
     protected WorldGenBigTree H = new WorldGenBigTree(false);
     protected WorldGenForest I = new WorldGenForest(false);
     protected WorldGenSwampTree J = new WorldGenSwampTree();
 
     protected BiomeBase(int paramInt) {
         super(paramInt);
 
 //      this.F = paramInt;
         a[paramInt] = this;
         this.B = createBiomeDecorator();
 
         // Override values
         this.t = (byte) Block.GRASS.id;
         this.u = (byte) Block.DIRT.id;
         this.v = 5169201;
         this.w = 0.1F;
         this.x = 0.3F;
         this.y = 0.5F;
         this.z = 0.5F;
         this.A = 16777215;
 
         this.D.add(new BiomeMeta(EntitySheep.class, 12, 4, 4));
         this.D.add(new BiomeMeta(EntityPig.class, 10, 4, 4));
         this.D.add(new BiomeMeta(EntityChicken.class, 10, 4, 4));
         this.D.add(new BiomeMeta(EntityCow.class, 8, 4, 4));
 
         this.C.add(new BiomeMeta(EntitySpider.class, 10, 4, 4));
         this.C.add(new BiomeMeta(EntityZombie.class, 10, 4, 4));
         this.C.add(new BiomeMeta(EntitySkeleton.class, 10, 4, 4));
         this.C.add(new BiomeMeta(EntityCreeper.class, 10, 4, 4));
         this.C.add(new BiomeMeta(EntitySlime.class, 10, 4, 4));
         this.C.add(new BiomeMeta(EntityEnderman.class, 1, 1, 4));
 
         this.E.add(new BiomeMeta(EntitySquid.class, 10, 4, 4));
     }
 
     protected BiomeDecorator createBiomeDecorator() {
         return new BiomeDecorator(this);
     }
 
     private BiomeBase a(float paramFloat1, float paramFloat2) {
         if ((paramFloat1 > 0.1F) && (paramFloat1 < 0.2F))
             throw new IllegalArgumentException("Please avoid temperatures in the range 0.1 - 0.2 because of snow");
 
         this.y = paramFloat1;
         this.z = paramFloat2;
         return this;
     }
 
     private BiomeBase b(float paramFloat1, float paramFloat2) {
         this.w = paramFloat1;
         this.x = paramFloat2;
         return this;
     }
 
 //    private BiomeBase g() {
 //        this.L = false;
 //        return this;
 //    }
 
 //    @Override
 //    public WorldGenerator a(Random paramRandom) {
     public WorldGenerator aa(Random paramRandom) {
         if (paramRandom.nextInt(10) == 0) {
             return this.H;
         }
         return this.G;
     }
 
     protected BiomeBase a(String paramString) {
         this.r = paramString;
         return this;
     }
 
     protected BiomeBase a(int paramInt) {
         this.v = paramInt;
         return this;
     }
 
     protected BiomeBase b(int paramInt) {
         this.s = paramInt;
         return this;
     }
 
     @Override
     public List<BiomeMeta> a(EnumCreatureType paramEnumCreatureType) {
         if (paramEnumCreatureType == EnumCreatureType.MONSTER)
             return this.C;
         if (paramEnumCreatureType == EnumCreatureType.CREATURE)
             return this.D;
         if (paramEnumCreatureType == EnumCreatureType.WATER_CREATURE)
             return this.E;
         return null;
     }
 
 //    public boolean b() {
 //        return this.K;
 //    }
 
 //    public boolean c() {
 //        if (this.K)
 //            return false;
 //        return this.L;
 //    }
 
     @Override
     public float d() {
         return 0.1F;
     }
 
 //    public final int e() {
 //        return (int) (this.z * 65536.0F);
 //    }
 
 //    public final int f() {
 //        return (int) (this.y * 65536.0F);
 //    }
 
     @Override
     public void a(World paramWorld, Random paramRandom, int paramInt1, int paramInt2) {
         this.B.a(paramWorld, paramRandom, paramInt1, paramInt2);
     }
 }
