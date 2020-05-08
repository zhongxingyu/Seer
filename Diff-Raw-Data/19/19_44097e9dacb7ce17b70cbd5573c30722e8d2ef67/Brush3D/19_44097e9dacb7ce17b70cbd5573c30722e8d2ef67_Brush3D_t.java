 package tk.amberide.ide.gui.editor.map.tool._3d;
 
 import tk.amberide.engine.data.map.Direction;
 import tk.amberide.engine.data.map.Layer;
 import tk.amberide.engine.data.map.Layer3D;
 import tk.amberide.engine.data.map.Tile;
 import tk.amberide.engine.data.map.Tile3D;
 import static tk.amberide.engine.data.map.Angle.*;
 import tk.amberide.engine.data.map.TileModel;
 import tk.amberide.engine.data.math.Angles;
 import tk.amberide.ide.data.res.Tileset;
 import tk.amberide.engine.gl.camera.EulerCamera;
 import tk.amberide.engine.gl.tess.ITesselator;
 import tk.amberide.engine.gl.tess.ImmediateTesselator;
 import tk.amberide.ide.gui.editor.map.MapContext;
 import tk.amberide.engine.input.AbstractKeyboard;
 import java.awt.Component;
 import org.lwjgl.input.Keyboard;
 import static org.lwjgl.opengl.GL11.*;
 import org.lwjgl.util.vector.Vector2f;
 import tk.amberide.engine.data.map.Angle;
 import tk.amberide.engine.data.map.TileType;
 
 /**
  *
  * @author Tudor
  */
 public class Brush3D extends AbstractTool3D {
 
     protected EulerCamera camera;
     protected Angle angle = _180;
     protected ITesselator tess = new ImmediateTesselator();
 
     public Brush3D(MapContext context, EulerCamera camera) {
         super(context);
         this.camera = camera;
     }
 
     @Override
     public void doScroll(int delta) {
         if (delta >= 1) {
             angle = angle == _45 ? _180 : (angle == _90 ? _45 : angle);
 
         } else if (delta <= -1) {
             angle = angle == _45 ? _90 : (angle == _180 ? _45 : angle);
         }
     }
 
     public boolean apply(int x, int y, int z) {
         boolean modified = false;
         switch (context.drawType) {
             case MapContext.TYPE_TILE:
                 if (context.tileSelection != null) {
                     Tileset.TileSprite[][] sel = context.tileSelection.clone();
                     int w = sel.length;
                     int h = sel[0].length;
                     for (int sx = 0; sx != w; sx++) {
                         for (int sy = 0; sy != h; sy++) {
                             boolean tiled = setAngledTile(x, y, z, sx, sy, h, w, camera.getFacingDirection(), angle, sel);
                             if (!modified) {
                                 modified = tiled;
                             }
                         }
                     }
                 }
                 break;
             case MapContext.EXT_TYPE_MODEL:
                 if (context.EXT_modelSelection != null) {
                     Layer l = context.map.getLayer(context.layer);
                     if (l instanceof Layer3D) {
                         Layer3D l3d = (Layer3D) l;
                        if (isInBounds(x, y)) {
                            TileModel p = l3d.getModel(x, y, z);
                            if (p == null || p.getModel() != context.EXT_modelSelection) {
                                l3d.setModel(x, y, z, new TileModel(context.EXT_modelSelection));
                                modified = true;
                            }
                         }
                     }
                 }
                 break;
         }
 
         return modified;
     }
 
     protected final boolean setAngledTile(int x, int y, int z, int sx, int sy, int h, int w, Direction dir, Angle angle, Tileset.TileSprite[][] sel) {
         // It works, that's all you need to know.
         switch (angle) {
             case _180:
                 switch (dir.toCardinal()) {
                     case NORTH:
                         return setTile(x + sy, y + sx, z, sel[sx][h - sy - 1], dir, angle);
                     case EAST:
                         return setTile(sx + x - w + 1, sy + y, z, sel[w - sx - 1][h - sy - 1], dir, angle);
                     case SOUTH:
                         return setTile(x + sy - h + 1, y + sx - w + 1, z, sel[w - sx - 1][sy], dir, angle);
                     case WEST:
                         return setTile(sx + x, sy + y - h + 1, z, sel[sx][sy], dir, angle);
                 }
             case _90:
                 switch (dir.toCardinal()) {
                     case NORTH:
                         return setTile(x, y + sx, z + sy, sel[sx][h - sy - 1], dir, angle);
                     case EAST:
                         return setTile(sx + x - w + 1, y, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                     case SOUTH:
                         return setTile(x, y + sx - w + 1, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                     case WEST:
                         return setTile(sx + x, y, z + sy, sel[sx][h - sy - 1], dir, angle);
                 }
             case _45:
                 switch (dir.toCardinal()) {
                     case NORTH:
                         return setTile(x + sy, y + sx, z + sy, sel[sx][h - sy - 1], dir, angle);
                     case EAST:
                         return setTile(sx + x - w + 1, sy + y, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                     case SOUTH:
                         return setTile(x - sy, y + sx - w + 1, z + sy, sel[w - sx - 1][h - sy - 1], dir, angle);
                     case WEST:
                         return setTile(sx + x, y - sy, z + sy, sel[sx][h - sy - 1], dir, angle);
                 }
         }
         return false;
     }
 
     protected boolean setTile(int x, int y, int z, Tileset.TileSprite tile, Direction dir, Angle angle) {
         Layer lay = context.map.getLayer(context.layer);
         boolean modified = false;
         if (isInBounds(x, y)) {
             Tile r = lay.getTile(x, y, z);
             if (tile != null) {
                 modified = r == null || !tile.equals(r.getSprite());
                TileType type;
                if (AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                    type = AbstractKeyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? TileType.TILE_CORNER_INVERSED : TileType.TILE_CORNER;
                } else {
                    type = TileType.TILE_NORMAL;
                }
                lay.setTile(x, y, z, new Tile3D(tile, dir, angle, type));
             } else {
                 modified = r != null;
                 lay.setTile(x, y, z, null);
             }
         }
         return modified;
     }
 
     public void draw(int x, int y, int z) {
         if (context.drawType == MapContext.EXT_TYPE_MODEL && context.EXT_modelSelection != null) {
             glPushAttrib(GL_CURRENT_BIT | GL_POLYGON_BIT);
             glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
             tess.drawModel3D(new TileModel(context.EXT_modelSelection), x, z, y);
             glPopAttrib();
         } else if (context.tileSelection != null) {
             glPushMatrix();
             float[] tr = new float[]{x, y, z, 0};
 
             switch (camera.getFacingDirection().toCardinal()) {
                 case SOUTH:
                     tr[0]++;
                     tr[2]++;
                     tr[3] = 180;
                     break;
                 case WEST:
                     tr[2]++;
                     tr[3] = 90;
                     break;
                 case EAST:
                     tr[0]++;
                     tr[3] = 270;
                     break;
             }
             glTranslatef(tr[0], tr[1], tr[2]);
             float y1, y2, y3, y4;
             y1 = y2 = y3 = y4 = 0;
             Vector2f ix;
             Direction dir = camera.getFacingDirection();
             if (AbstractKeyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !dir.cardinal()) {
                 switch (dir) {
                     case NORTH_EAST:
                         y3++;
                         break;
                     case NORTH_WEST:
                         y2++;
                         break;
                     case SOUTH_EAST:
                         y4++;
                         break;
                     case SOUTH_WEST:
                         y1++;
                         break;
                 }
                 ix = Angles.circleIntercept(_180.intValue(), 0, 0, context.tileSelection[0].length);
             } else {
                 ix = Angles.circleIntercept(angle.intValue(), 0, 0, context.tileSelection[0].length);
             }
             glRotatef(tr[3], 0, 1, 0);
 
             glBegin(GL_LINE_LOOP);
             {
                 int w = context.tileSelection.length;
                 glVertex3f(0, 0 + y1, 0);
                 glVertex3f(ix.x, ix.y + y2, 0);
                 glVertex3f(ix.x, ix.y + y3, w);
                 glVertex3f(0, 0 + y4, w);
             }
             glEnd();
             glPopMatrix();
         }
     }
 
     public Component getContextComponent() {
         return null;
     }
 }
