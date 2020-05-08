 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 import java.util.Random;
 
 import net.minecraft.server.World;
 
 public class BiomeDecorator {
     protected World a;
     protected Random b;
     protected int c;
     protected int d;
     protected BiomeBase e;
     protected WorldGenerator f = new WorldGenClay(4);
     protected WorldGenerator g = new WorldGenSand(7, Block.SAND.id);
     protected WorldGenerator h = new WorldGenSand(6, Block.GRAVEL.id);
     protected WorldGenerator i = new WorldGenMinable(Block.DIRT.id, 32);
     protected WorldGenerator j = new WorldGenMinable(Block.GRAVEL.id, 32);
     protected WorldGenerator k = new WorldGenMinable(Block.COAL_ORE.id, 16);
     protected WorldGenerator l = new WorldGenMinable(Block.IRON_ORE.id, 8);
     protected WorldGenerator m = new WorldGenMinable(Block.GOLD_ORE.id, 8);
     protected WorldGenerator n = new WorldGenMinable(Block.REDSTONE_ORE.id, 7);
     protected WorldGenerator o = new WorldGenMinable(Block.DIAMOND_ORE.id, 7);
     protected WorldGenerator p = new WorldGenMinable(Block.LAPIS_ORE.id, 6);
     protected WorldGenerator q = new WorldGenFlowers(Block.YELLOW_FLOWER.id);
     protected WorldGenerator r = new WorldGenFlowers(Block.RED_ROSE.id);
     protected WorldGenerator s = new WorldGenFlowers(Block.BROWN_MUSHROOM.id);
     protected WorldGenerator t = new WorldGenFlowers(Block.RED_MUSHROOM.id);
     protected WorldGenerator u = new WorldGenHugeMushroom();
     protected WorldGenerator v = new WorldGenReed();
     protected WorldGenerator w = new WorldGenCactus();
     protected WorldGenerator x = new WorldGenWaterLily();
 
     protected int y = 0;
     protected int z = 0;
     protected int A = 2;
     protected int B = 1;
     protected int C = 0;
     protected int D = 0;
     protected int E = 0;
     protected int F = 0;
     protected int G = 1;
     protected int H = 3;
     protected int I = 1;
     protected int J = 0;
     public boolean K = true;
 
     public BiomeDecorator(BiomeBase paramBiomeBase) {
         this.e = paramBiomeBase;
     }
 
     public void a(World paramWorld, Random paramRandom, int paramInt1, int paramInt2) {
         if (this.a != null)
             throw new RuntimeException("Already decorating!!");
         this.a = paramWorld;
         this.b = paramRandom;
         this.c = paramInt1;
         this.d = paramInt2;
 
         a();
 
         this.a = null;
         this.b = null;
     }
 
     protected void a() {
         b();
         int i3;
        for (int i1 = 0; i1 < this.H; i1++) {
             i2 = this.c + this.b.nextInt(16) + 8;
             i3 = this.d + this.b.nextInt(16) + 8;
             this.g.a(this.a, this.b, i2, this.a.f(i2, i3), i3);
         }
 
         for (i1 = 0; i1 < this.I; i1++) {
             i2 = this.c + this.b.nextInt(16) + 8;
             i3 = this.d + this.b.nextInt(16) + 8;
             this.f.a(this.a, this.b, i2, this.a.f(i2, i3), i3);
         }
 
         for (i1 = 0; i1 < this.G; i1++) {
             i2 = this.c + this.b.nextInt(16) + 8;
             i3 = this.d + this.b.nextInt(16) + 8;
             this.g.a(this.a, this.b, i2, this.a.f(i2, i3), i3);
         }
 
         i1 = this.z;
         if (this.b.nextInt(10) == 0)
             i1++;
         int i4;
        for (int i2 = 0; i2 < i1; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.d + this.b.nextInt(16) + 8;
             WorldGenerator localWorldGenerator = this.e.a(this.b);
             localWorldGenerator.a(1.0D, 1.0D, 1.0D);
             localWorldGenerator.a(this.a, this.b, i3, this.a.getHighestBlockYAt(i3, i4), i4);
         }
 
         for (i2 = 0; i2 < this.J; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.d + this.b.nextInt(16) + 8;
             this.u.a(this.a, this.b, i3, this.a.getHighestBlockYAt(i3, i4), i4);
         }
         int i5;
         for (i2 = 0; i2 < this.A; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.b.nextInt(this.a.height);
             i5 = this.d + this.b.nextInt(16) + 8;
             this.q.a(this.a, this.b, i3, i4, i5);
 
             if (this.b.nextInt(4) == 0) {
                 i3 = this.c + this.b.nextInt(16) + 8;
                 i4 = this.b.nextInt(this.a.height);
                 i5 = this.d + this.b.nextInt(16) + 8;
                 this.r.a(this.a, this.b, i3, i4, i5);
             }
 
         }
 
         for (i2 = 0; i2 < this.B; i2++) {
             i3 = 1;
 
             i4 = this.c + this.b.nextInt(16) + 8;
             i5 = this.b.nextInt(this.a.height);
             int i6 = this.d + this.b.nextInt(16) + 8;
             new WorldGenGrass(Block.LONG_GRASS.id, i3).a(this.a, this.b, i4, i5, i6);
         }
 
         for (i2 = 0; i2 < this.C; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.b.nextInt(this.a.height);
             i5 = this.d + this.b.nextInt(16) + 8;
             new WorldGenDeadBush(Block.DEAD_BUSH.id).a(this.a, this.b, i3, i4, i5);
         }
 
         for (i2 = 0; i2 < this.y; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.d + this.b.nextInt(16) + 8;
             i5 = this.b.nextInt(this.a.height);
             while ((i5 > 0) && (this.a.getTypeId(i3, i5 - 1, i4) == 0))
                 i5--;
             this.x.a(this.a, this.b, i3, i5, i4);
         }
 
         for (i2 = 0; i2 < this.D; i2++) {
             if (this.b.nextInt(4) == 0) {
                 i3 = this.c + this.b.nextInt(16) + 8;
                 i4 = this.d + this.b.nextInt(16) + 8;
                 i5 = this.a.getHighestBlockYAt(i3, i4);
                 this.s.a(this.a, this.b, i3, i5, i4);
             }
 
             if (this.b.nextInt(8) == 0) {
                 i3 = this.c + this.b.nextInt(16) + 8;
                 i4 = this.d + this.b.nextInt(16) + 8;
                 i5 = this.b.nextInt(this.a.height);
                 this.t.a(this.a, this.b, i3, i5, i4);
             }
         }
 
         if (this.b.nextInt(4) == 0) {
             i2 = this.c + this.b.nextInt(16) + 8;
             i3 = this.b.nextInt(this.a.height);
             i4 = this.d + this.b.nextInt(16) + 8;
             this.s.a(this.a, this.b, i2, i3, i4);
         }
 
         if (this.b.nextInt(8) == 0) {
             i2 = this.c + this.b.nextInt(16) + 8;
             i3 = this.b.nextInt(this.a.height);
             i4 = this.d + this.b.nextInt(16) + 8;
             this.t.a(this.a, this.b, i2, i3, i4);
         }
 
         for (i2 = 0; i2 < this.E; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.d + this.b.nextInt(16) + 8;
             i5 = this.b.nextInt(this.a.height);
             this.v.a(this.a, this.b, i3, i5, i4);
         }
 
         for (i2 = 0; i2 < 10; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.b.nextInt(this.a.height);
             i5 = this.d + this.b.nextInt(16) + 8;
             this.v.a(this.a, this.b, i3, i4, i5);
         }
 
         if (this.b.nextInt(32) == 0) {
             i2 = this.c + this.b.nextInt(16) + 8;
             i3 = this.b.nextInt(this.a.height);
             i4 = this.d + this.b.nextInt(16) + 8;
             new WorldGenPumpkin().a(this.a, this.b, i2, i3, i4);
         }
 
         for (i2 = 0; i2 < this.F; i2++) {
             i3 = this.c + this.b.nextInt(16) + 8;
             i4 = this.b.nextInt(this.a.height);
             i5 = this.d + this.b.nextInt(16) + 8;
             this.w.a(this.a, this.b, i3, i4, i5);
         }
 
         if (this.K) {
             for (i2 = 0; i2 < 50; i2++) {
                 i3 = this.c + this.b.nextInt(16) + 8;
                 i4 = this.b.nextInt(this.b.nextInt(this.a.height - 8) + 8);
                 i5 = this.d + this.b.nextInt(16) + 8;
                 new WorldGenLiquids(Block.WATER.id).a(this.a, this.b, i3, i4, i5);
             }
 
             for (i2 = 0; i2 < 20; i2++) {
                 i3 = this.c + this.b.nextInt(16) + 8;
                 i4 = this.b.nextInt(this.b.nextInt(this.b.nextInt(this.a.height - 16) + 8) + 8);
                 i5 = this.d + this.b.nextInt(16) + 8;
                 new WorldGenLiquids(Block.LAVA.id).a(this.a, this.b, i3, i4, i5);
             }
         }
     }
 
     protected void a(int paramInt1, WorldGenerator paramWorldGenerator, int paramInt2, int paramInt3) {
         for (int i1 = 0; i1 < paramInt1; i1++) {
             int i2 = this.c + this.b.nextInt(16);
             int i3 = this.b.nextInt(paramInt3 - paramInt2) + paramInt2;
             int i4 = this.d + this.b.nextInt(16);
             paramWorldGenerator.a(this.a, this.b, i2, i3, i4);
         }
     }
 
     protected void b(int paramInt1, WorldGenerator paramWorldGenerator, int paramInt2, int paramInt3) {
         for (int i1 = 0; i1 < paramInt1; i1++) {
             int i2 = this.c + this.b.nextInt(16);
             int i3 = this.b.nextInt(paramInt3) + this.b.nextInt(paramInt3) + (paramInt2 - paramInt3);
             int i4 = this.d + this.b.nextInt(16);
             paramWorldGenerator.a(this.a, this.b, i2, i3, i4);
         }
     }
 
     protected void b() {
         a(20, this.i, 0, this.a.height);
         a(10, this.j, 0, this.a.height);
         a(20, this.k, 0, this.a.height);
         a(20, this.l, 0, this.a.height / 2);
         a(2, this.m, 0, this.a.height / 4);
         a(8, this.n, 0, this.a.height / 8);
         a(1, this.o, 0, this.a.height / 8);
         b(1, this.p, this.a.height / 8, this.a.height / 8);
     }
 }
