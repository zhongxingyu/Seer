 package com.djdch.bukkit.onehundredgenerator.mc100;
 
 public abstract class GenLayer {
     private long b;
     protected GenLayer a;
     private long c;
     private long d;
 
     public static GenLayer[] a(long paramLong) {
         Object localObject1 = new LayerIsland(1L);
         localObject1 = new GenLayerZoomFuzzy(2000L, (GenLayer) localObject1);
         localObject1 = new GenLayerIsland(1L, (GenLayer) localObject1);
         localObject1 = new GenLayerZoom(2001L, (GenLayer) localObject1);
         localObject1 = new GenLayerIsland(2L, (GenLayer) localObject1);
         localObject1 = new GenLayerIcePlains(2L, (GenLayer) localObject1);
         localObject1 = new GenLayerZoom(2002L, (GenLayer) localObject1);
         localObject1 = new GenLayerIsland(3L, (GenLayer) localObject1);
         localObject1 = new GenLayerZoom(2003L, (GenLayer) localObject1);
         localObject1 = new GenLayerIsland(4L, (GenLayer) localObject1);
         localObject1 = new GenLayerMushroomIsland(5L, (GenLayer) localObject1);
 
         int i = 4;
 
         Object localObject2 = localObject1;
         localObject2 = GenLayerZoom.a(1000L, (GenLayer) localObject2, 0);
         localObject2 = new GenLayerRiverInit(100L, (GenLayer) localObject2);
         localObject2 = GenLayerZoom.a(1000L, (GenLayer) localObject2, i + 2);
         localObject2 = new GenLayerRiver(1L, (GenLayer) localObject2);
         localObject2 = new GenLayerSmooth(1000L, (GenLayer) localObject2);
 
         Object localObject3 = localObject1;
         localObject3 = GenLayerZoom.a(1000L, (GenLayer) localObject3, 0);
         localObject3 = new GenLayerBiome(200L, (GenLayer) localObject3);
         localObject3 = GenLayerZoom.a(1000L, (GenLayer) localObject3, 2);
 
         Object localObject4 = new GenLayerTemperature((GenLayer) localObject3);
         Object localObject5 = new GenLayerDownfall((GenLayer) localObject3);
 
         for (int j = 0; j < i; j++) {
             localObject3 = new GenLayerZoom(1000 + j, (GenLayer) localObject3);
             if (j == 0)
                 localObject3 = new GenLayerIsland(3L, (GenLayer) localObject3);
 
             if (j == 0) {
                 localObject3 = new GenLayerMushroomShore(1000L, (GenLayer) localObject3);
             }
             localObject4 = new GenLayerSmoothZoom(1000 + j, (GenLayer) localObject4);
             localObject4 = new GenLayerTemperatureMix((GenLayer) localObject4, (GenLayer) localObject3, j);
             localObject5 = new GenLayerSmoothZoom(1000 + j, (GenLayer) localObject5);
             localObject5 = new GenLayerDownfallMix((GenLayer) localObject5, (GenLayer) localObject3, j);
         }
 
         localObject3 = new GenLayerSmooth(1000L, (GenLayer) localObject3);
 
         localObject3 = new GenLayerRiverMix(100L, (GenLayer) localObject3, (GenLayer) localObject2);
 
         Object localObject6 = localObject3;
 
         localObject4 = GenLayerSmoothZoom.a(1000L, (GenLayer) localObject4, 2);
         localObject5 = GenLayerSmoothZoom.a(1000L, (GenLayer) localObject5, 2);
 
         GenLayerZoomVoronoi localGenLayerZoomVoronoi = new GenLayerZoomVoronoi(10L, (GenLayer) localObject3);
 
         ((GenLayer) localObject3).b(paramLong);
         ((GenLayer) localObject4).b(paramLong);
         ((GenLayer) localObject5).b(paramLong);
 
         localGenLayerZoomVoronoi.b(paramLong);
 
        return (new GenLayer[] { (GenLayer) localObject3, localGenLayerZoomVoronoi, (GenLayer) localObject4, (GenLayer) localObject5, (GenLayer) localObject6 });
     }
 
     public GenLayer(long paramLong) {
         this.d = paramLong;
         this.d *= (this.d * 6364136223846793005L + 1442695040888963407L);
         this.d += paramLong;
         this.d *= (this.d * 6364136223846793005L + 1442695040888963407L);
         this.d += paramLong;
         this.d *= (this.d * 6364136223846793005L + 1442695040888963407L);
         this.d += paramLong;
     }
 
     public void b(long paramLong) {
         this.b = paramLong;
         if (this.a != null)
             this.a.b(paramLong);
         this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
         this.b += this.d;
         this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
         this.b += this.d;
         this.b *= (this.b * 6364136223846793005L + 1442695040888963407L);
         this.b += this.d;
     }
 
     public void a(long paramLong1, long paramLong2) {
         this.c = this.b;
         this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
         this.c += paramLong1;
         this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
         this.c += paramLong2;
         this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
         this.c += paramLong1;
         this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
         this.c += paramLong2;
     }
 
     protected int a(int paramInt) {
         int i = (int) ((this.c >> 24) % paramInt);
         if (i < 0)
             i += paramInt;
         this.c *= (this.c * 6364136223846793005L + 1442695040888963407L);
         this.c += this.b;
         return i;
     }
 
     public abstract int[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4);
 }
