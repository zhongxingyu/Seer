 package com.bencvt.minecraft.buildregion.ui;
 
 import libshapedraw.MinecraftAccess;
 import libshapedraw.primitive.ReadonlyColor;
 import libshapedraw.primitive.ReadonlyVector3;
 import libshapedraw.primitive.Vector3;
 
 import org.lwjgl.opengl.GL11;
 
 import com.bencvt.minecraft.buildregion.region.Axis;
 import com.bencvt.minecraft.buildregion.region.RegionBase;
 import com.bencvt.minecraft.buildregion.region.RegionPlane;
 
 /**
  * A LibShapeDraw Shape representing a plane. This is purely cosmetic; see
  * RegionPlane for the object that actually defines the plane.
  * <p>
  * The plane is rendered as an infinite 1-block-thick wireframe grid. Only a
  * few cells of the grid are rendered, clustered around the origin, which can
  * change. Cells further away from the origin are increasingly transparent.
  * 
  * @author bencvt
  */
 public class RenderPlane extends RenderBase {
     /** How many grid cubes to render. */
     public static final int PLANE_RENDER_RADIUS = 10;
     /** Lazily instantiated lookup table. */
     private static double[][] alphaTable;
 
     private final Axis axis;
     private final Vector3 observerPosition;
 
     // Internal arrays used by renderLines, persisted to the instance so we
     // don't keep pushing temporary objects to the heap every rendering frame.
     private final double[] baseCoords = {0.0, 0.0, 0.0};
     private final double[] curCoords = {0.0, 0.0, 0.0};
 
     public RenderPlane(ReadonlyColor lineColorVisible, ReadonlyColor lineColorHidden, RegionPlane region) {
        super(lineColorVisible, lineColorHidden, true);//XXX
         onUpdateOrigin(getOrigin().set(region.getOriginReadonly()));
         axis = region.getAxis();
         observerPosition = new Vector3(); // only two of these coords are relevant
     }
 
     @Override
     public boolean updateIfPossible(RegionBase region, boolean animate) {
         if (!region.isRegionType(RegionPlane.class)) {
             return false;
         }
         RegionPlane plane = (RegionPlane) region;
         if (axis != plane.getAxis()) {
             return false;
         }
         if (animate && Math.pow(getCoord() - plane.getCoord(), 2.0) > SHIFT_MAX_SQUARED) {
             return false;
         }
         animateShiftOrigin(plane.getOriginReadonly(), animate);
         return true;
     }
 
     private double getCoord() {
         if (axis == Axis.X) {
             return getOriginReadonly().getX();
         } else if (axis == Axis.Y) {
             return getOriginReadonly().getY();
         } else if (axis == Axis.Z) {
             return getOriginReadonly().getZ();
         } else {
             throw new IllegalStateException();
         }
     }
 
     @Override
     public void updateObserverPosition(ReadonlyVector3 observerPosition) {
         // Keep up with the player, moving the shape along the plane.
         this.observerPosition.set(observerPosition);
     }
 
     @Override
     protected void renderLines(MinecraftAccess mc, ReadonlyColor lineColor) {
         double alphaLine = getAlphaBase();
 
         curCoords[0] = baseCoords[0] = observerPosition.getX();
         curCoords[1] = baseCoords[1] = observerPosition.getY();
         curCoords[2] = baseCoords[2] = observerPosition.getZ();
         final int dim0;
         final int dim1;
         if (axis == Axis.X) {
             curCoords[0] = baseCoords[0] = getOriginReadonly().getX();
             dim0 = 1;
             dim1 = 2;
         } else if (axis == Axis.Y) {
             curCoords[1] = baseCoords[1] = getOriginReadonly().getY();
             dim0 = 0;
             dim1 = 2;
         } else if (axis == Axis.Z) {
             curCoords[2] = baseCoords[2] = getOriginReadonly().getZ();
             dim0 = 0;
             dim1 = 1;
         } else {
             throw new IllegalStateException();
         }
         baseCoords[dim0] = (int) baseCoords[dim0];
         baseCoords[dim1] = (int) baseCoords[dim1];
 
         for (int off0 = -PLANE_RENDER_RADIUS; off0 <= PLANE_RENDER_RADIUS; off0++) {
             curCoords[dim0] = baseCoords[dim0] + off0;
             for (int off1 = -PLANE_RENDER_RADIUS; off1 <= PLANE_RENDER_RADIUS; off1++) {
                 curCoords[dim1] = baseCoords[dim1] + off1;
                 final double alphaScale = alphaLine * getAlphaScale(off0, off1);
                 if (alphaScale <= 0.0) {
                     continue;
                 }
                 final double x0 = curCoords[0] + CUBE_MARGIN;
                 final double x1 = curCoords[0] + 1 - CUBE_MARGIN;
                 final double y0 = curCoords[1] + CUBE_MARGIN;
                 final double y1 = curCoords[1] + 1 - CUBE_MARGIN;
                 final double z0 = curCoords[2] + CUBE_MARGIN;
                 final double z1 = curCoords[2] + 1 - CUBE_MARGIN;
                 lineColor.glApply(alphaScale);
                 // Because sides are rendered with a different transparency we
                 // need to check the axis again.
                 if (axis == Axis.X) {
                     // west
                     mc.startDrawing(GL11.GL_LINE_LOOP);
                     mc.addVertex(x0, y0, z0);
                     mc.addVertex(x0, y1, z0);
                     mc.addVertex(x0, y1, z1);
                     mc.addVertex(x0, y0, z1);
                     mc.finishDrawing();
                     // east
                     mc.startDrawing(GL11.GL_LINE_LOOP);
                     mc.addVertex(x1, y0, z0);
                     mc.addVertex(x1, y1, z0);
                     mc.addVertex(x1, y1, z1);
                     mc.addVertex(x1, y0, z1);
                     mc.finishDrawing();
                     // sides
                     lineColor.glApply(alphaScale * ALPHA_SIDE);
                     mc.startDrawing(GL11.GL_LINES);
                     mc.addVertex(x0, y0, z0).addVertex(x1, y0, z0);
                     mc.addVertex(x0, y1, z0).addVertex(x1, y1, z0);
                     mc.addVertex(x0, y1, z1).addVertex(x1, y1, z1);
                     mc.addVertex(x0, y0, z1).addVertex(x1, y0, z1);
                     mc.finishDrawing();
                 } else if (axis == Axis.Y) {
                     // bottom
                     mc.startDrawing(GL11.GL_LINE_LOOP);
                     mc.addVertex(x0, y0, z0);
                     mc.addVertex(x1, y0, z0);
                     mc.addVertex(x1, y0, z1);
                     mc.addVertex(x0, y0, z1);
                     mc.finishDrawing();
                     // top
                     mc.startDrawing(GL11.GL_LINE_LOOP);
                     mc.addVertex(x0, y1, z0);
                     mc.addVertex(x1, y1, z0);
                     mc.addVertex(x1, y1, z1);
                     mc.addVertex(x0, y1, z1);
                     mc.finishDrawing();
                     // sides
                     lineColor.glApply(alphaScale * ALPHA_SIDE);
                     mc.startDrawing(GL11.GL_LINES);
                     mc.addVertex(x0, y0, z0).addVertex(x0, y1, z0);
                     mc.addVertex(x1, y0, z0).addVertex(x1, y1, z0);
                     mc.addVertex(x1, y0, z1).addVertex(x1, y1, z1);
                     mc.addVertex(x0, y0, z1).addVertex(x0, y1, z1);
                     mc.finishDrawing();
                 } else if (axis == Axis.Z) {
                     // north
                     mc.startDrawing(GL11.GL_LINE_LOOP);
                     mc.addVertex(x0, y0, z0);
                     mc.addVertex(x1, y0, z0);
                     mc.addVertex(x1, y1, z0);
                     mc.addVertex(x0, y1, z0);
                     mc.finishDrawing();
                     // south
                     mc.startDrawing(GL11.GL_LINE_LOOP);
                     mc.addVertex(x0, y0, z1);
                     mc.addVertex(x1, y0, z1);
                     mc.addVertex(x1, y1, z1);
                     mc.addVertex(x0, y1, z1);
                     mc.finishDrawing();
                     // sides
                     lineColor.glApply(alphaScale * ALPHA_SIDE);
                     mc.startDrawing(GL11.GL_LINES);
                     mc.addVertex(x0, y0, z0).addVertex(x0, y0, z1);
                     mc.addVertex(x1, y0, z0).addVertex(x1, y0, z1);
                     mc.addVertex(x1, y1, z0).addVertex(x1, y1, z1);
                     mc.addVertex(x0, y1, z0).addVertex(x0, y1, z1);
                     mc.finishDrawing();
                 }
             }
         }
     }
 
     private static double getAlphaScale(int off0, int off1) {
         //return Math.max(0.0, (PLANE_RENDER_RADIUS + 1 - Math.sqrt(off0*off0 + off1*off1)) / (PLANE_RENDER_RADIUS + 1));
         if (alphaTable == null) {
             final int R = PLANE_RENDER_RADIUS + 1;
             alphaTable = new double[R][R];
             for (int i = 0; i < R; i++) {
                 for (int j = 0; j < R; j++) {
                     alphaTable[i][j] = Math.max(0.0, (R - Math.sqrt(i*i + j*j)) / R);
                 }
             }
         }
         return alphaTable[Math.abs(off0)][Math.abs(off1)];
     }
 }
