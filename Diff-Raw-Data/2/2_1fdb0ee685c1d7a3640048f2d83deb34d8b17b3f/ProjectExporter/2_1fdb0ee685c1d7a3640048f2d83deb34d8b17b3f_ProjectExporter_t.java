 /*
     This program is a plugin for LateralGM
 
     Copyright (c) 2012 Serge Humphrey<bobtheblueberry@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.btbb.hoiley;
 
 import static org.lateralgm.main.Util.deRef;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.zip.DeflaterOutputStream;
 
 import org.lateralgm.components.impl.ResNode;
 import org.lateralgm.file.GmStreamEncoder;
 import org.lateralgm.file.ResourceList;
 import org.lateralgm.file.StreamEncoder;
 import org.lateralgm.main.LGM;
 import org.lateralgm.resources.Background;
 import org.lateralgm.resources.Background.PBackground;
 import org.lateralgm.resources.Font;
 import org.lateralgm.resources.Font.PFont;
 import org.lateralgm.resources.GameInformation;
 import org.lateralgm.resources.GameInformation.PGameInformation;
 import org.lateralgm.resources.GmObject;
 import org.lateralgm.resources.GmObject.PGmObject;
 import org.lateralgm.resources.InstantiableResource;
 import org.lateralgm.resources.Path;
 import org.lateralgm.resources.Path.PPath;
 import org.lateralgm.resources.Resource;
 import org.lateralgm.resources.ResourceReference;
 import org.lateralgm.resources.Room;
 import org.lateralgm.resources.Room.PRoom;
 import org.lateralgm.resources.Script;
 import org.lateralgm.resources.Sound;
 import org.lateralgm.resources.Sound.PSound;
 import org.lateralgm.resources.Sound.SoundKind;
 import org.lateralgm.resources.Sprite;
 import org.lateralgm.resources.Sprite.MaskShape;
 import org.lateralgm.resources.Sprite.PSprite;
 import org.lateralgm.resources.Timeline;
 import org.lateralgm.resources.library.LibAction;
 import org.lateralgm.resources.sub.Action;
 import org.lateralgm.resources.sub.Argument;
 import org.lateralgm.resources.sub.BackgroundDef;
 import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
 import org.lateralgm.resources.sub.Event;
 import org.lateralgm.resources.sub.Instance;
 import org.lateralgm.resources.sub.Instance.PInstance;
 import org.lateralgm.resources.sub.MainEvent;
 import org.lateralgm.resources.sub.Moment;
 import org.lateralgm.resources.sub.PathPoint;
 import org.lateralgm.resources.sub.Tile;
 import org.lateralgm.resources.sub.Tile.PTile;
 import org.lateralgm.resources.sub.View;
 import org.lateralgm.resources.sub.View.PView;
 import org.lateralgm.util.PropertyMap;
 
 public class ProjectExporter {
 
     private int action_index = 0;
     private int iccode_index = 0;
     private File actionDir;
     public File projectDir;
 
     public ProjectExporter()
         {
             projectDir = new File("RuneroGame/");
         }
 
     public void clean() {
         deleteDir(projectDir);
     }
 
     private void deleteDir(File dir) {
         for (File f : dir.listFiles()) {
             if (f.isDirectory())
                 deleteDir(f);
             f.delete();
         }
     }
 
     public void export() {
 
         projectDir.mkdir();
         // Export stuff
 
         actionDir = new File(projectDir, "actions");
         actionDir.mkdir();
 
         // Write Sprites
         exportSprites(projectDir);
         // Write Backgrounds
         exportBackgrounds(projectDir);
         // Write Sounds
         exportSounds(projectDir);
         // Write Paths
         exportPaths(projectDir);
         // Write Scripts
         exportScripts(projectDir);
         // Write Fonts
         exportFonts(projectDir);
         // Write Timelines
         exportTimelines(projectDir);
         // Write Objects
         exportGmObjects(projectDir);
         // Write Rooms
         exportRooms(projectDir);
         // Write Game Information
         exportGameInfo(projectDir);
         // Write Global settings
         exportSettings(projectDir);
     }
 
     private void exportSprites(File parentdir) {
         File sprDir = new File(parentdir, "sprites");
         sprDir.mkdir();
         ResourceList<Sprite> sprites = LGM.currentFile.resMap.getList(Sprite.class);
         Iterator<Sprite> sprI = sprites.iterator();
 
         while (sprI.hasNext()) {
             ArrayList<String> subimgs = new ArrayList<String>();
             Sprite s = sprI.next();
             if (s.subImages.isEmpty()) {
                 System.out.println(s + " has no subimgs");
             } else {
                 int count = 0;
                 for (int i = 0; i < s.subImages.size(); i++) {
                     File imf = new File(sprDir, s.getName() + "_sub" + count++ + ".img");
 
                     BufferedImage image = s.subImages.get(i);
                     boolean trans = s.get(PSprite.TRANSPARENT);
                     writeImage(image, imf, trans);
                     System.out.println("Wrote SubImage " + imf);
 
                     subimgs.add(imf.getName());
                 }
             }
             // TRANSPARENT,SHAPE,ALPHA_TOLERANCE,SEPARATE_MASK,SMOOTH_EDGES,PRELOAD,ORIGIN_X,ORIGIN_Y,
             // BB_MODE*,BB_LEFT,BB_RIGHT,BB_TOP,BB_BOTTOM
             // * ignored
             boolean transparent = s.properties.get(PSprite.TRANSPARENT);
             MaskShape shape = s.properties.get(PSprite.SHAPE);
             int alpha = s.properties.get(PSprite.ALPHA_TOLERANCE);
             boolean mask = s.properties.get(PSprite.SEPARATE_MASK);
             boolean smooth = s.properties.get(PSprite.SMOOTH_EDGES);
             boolean preload = s.properties.get(PSprite.PRELOAD);
             int x = s.properties.get(PSprite.ORIGIN_X);
             int y = s.properties.get(PSprite.ORIGIN_Y);
             int left = s.properties.get(PSprite.BB_LEFT);
             int right = s.properties.get(PSprite.BB_RIGHT);
             int top = s.properties.get(PSprite.BB_TOP);
             int bottom = s.properties.get(PSprite.BB_BOTTOM);
 
             File f = new File(sprDir, s.getName() + ".dat");
 
             try {
                 StreamEncoder out = new StreamEncoder(f);
                 writeStr(s.getName(), out);
                 out.write4(s.getId());
                 if (s.subImages != null) {
                     out.write4(s.subImages.getWidth());
                     out.write4(s.subImages.getHeight());
                 } else {
                     out.write4(0);
                     out.write4(0);
                 }
                 // subimages
                 out.write4(subimgs.size());
                 for (String ss : subimgs)
                     writeStr(ss, out);
 
                 writeBool(transparent, out);
                 // PRECISE,RECTANGLE,DISK,DIAMOND,POLYGON
                 int box;
                 if (shape == MaskShape.RECTANGLE)
                     box = 0;
                 else if (shape == MaskShape.PRECISE)
                     box = 1;
                 else if (shape == MaskShape.DISK)
                     box = 2;
                 else if (shape == MaskShape.DIAMOND)
                     box = 3;
                 else if (shape == MaskShape.POLYGON)
                     box = 4;
                 else
                     box = -1;
                 out.write(box);
                 out.write4(alpha);
                 writeBool(mask, out);
                 writeBool(smooth, out);
                 writeBool(preload, out);
                 out.write4(x);
                 out.write4(y);
                 out.write4(left);
                 out.write4(right);
                 out.write4(top);
                 out.write4(bottom);
                 // write mask
                 writeSpriteMask(s, shape, out);
                 out.close();
                 System.out.println("Wrote " + f);
 
             } catch (IOException e) {
                 System.err.println("Error, can't write sprite " + f);
             }
         }
     }
 
     private void writeSpriteMask(Sprite s, MaskShape shape, StreamEncoder out) throws IOException {
         if (shape != MaskShape.PRECISE) {
             if (shape != MaskShape.RECTANGLE)
                 System.out.println("Cannot create mask shape of type " + shape);
             out.write4(0);
             return;
         }
         if (s.subImages == null) {
             out.write4(0);
             return;
         }
         // precise
 
         // determine length of data for each subimage
         int len = s.subImages.getWidth() * s.subImages.getHeight();
         int wlen = (int) Math.ceil(len / 8.0);
         out.write4(wlen);
 
         int threshold = 0x7F; // TODO: Add threshold thing to LGM - use alpha var
         for (BufferedImage img : s.subImages) {
             boolean hasAlpha = img.getColorModel().hasAlpha();
             int pixels[] = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
             int trans = img.getRGB(0, img.getHeight() - 1) & 0x00FFFFFF;
 
             for (int p = 0; p < pixels.length; p += 8) {
                 char b = 0;
                 for (int j = 0; j < 8; j++) {
                     if (p + j >= pixels.length) {
                         b <<= (7 - j);
                         break;
                     }
                     int pixel = pixels[p + j];
                     if (hasAlpha) {
                         if ((pixel >>> 24) > threshold)
                             b++;// solid
                     } else {
                         if ((pixels[p] & 0x00FFFFFF) != trans)
                             b++;// solid
                     }
 
                     if (j < 7)
                         b <<= 1;
                 }
                 out.write(b);
             }
         }
     }
 
     private void exportSounds(File parentdir) {
 
         File sndDir = new File(parentdir, "sounds");
         sndDir.mkdir();
         ResourceList<Sound> sounds = LGM.currentFile.resMap.getList(Sound.class);
         Iterator<Sound> sndI = sounds.iterator();
         while (sndI.hasNext()) {
             Sound s = sndI.next();
             if (s.data == null || s.data.length < 1) {
                 continue;
             }
             PropertyMap<PSound> map = s.properties;
             String name = s.getName() + map.get(PSound.FILE_TYPE);
             File sf = new File(sndDir, name);
             try {
                 BufferedOutputStream w = new BufferedOutputStream(new FileOutputStream(sf));
                 w.write(s.data);
                 w.close();
                 System.out.println("Wrote sound " + sf);
             } catch (FileNotFoundException exc) {
                 System.err.println("Can't open file " + sf);
             } catch (IOException exc) {
                 System.err.println("Error writing sound " + sf.getName());
                 exc.printStackTrace();
             }
 
             // KIND,FILE_TYPE,FILE_NAME,CHORUS,ECHO,FLANGER,GARGLE,REVERB,VOLUME,PAN,PRELOAD
             SoundKind kind = s.properties.get(PSound.KIND);
             String file_type = s.properties.get(PSound.FILE_TYPE);
             String file_name = s.properties.get(PSound.FILE_NAME);
             boolean chorus = s.properties.get(PSound.CHORUS);
             boolean echo = s.properties.get(PSound.ECHO);
             boolean flanger = s.properties.get(PSound.FLANGER);
             boolean gargle = s.properties.get(PSound.GARGLE);
             boolean reverb = s.properties.get(PSound.REVERB);
             double volume = s.properties.get(PSound.VOLUME);
             double pan = s.properties.get(PSound.PAN);
             boolean preload = s.properties.get(PSound.PRELOAD);
 
             File data = new File(sndDir, s.getName() + ".dat");
             try {
                 PrintWriter ps = new PrintWriter(data);
 
                 ps.println(s.getName());
                 ps.println(s.getId());
                 if (kind == SoundKind.NORMAL) {
                     ps.println("NORMAL");
                 } else if (kind == SoundKind.MULTIMEDIA) {
                     ps.println("MULTIMEDIA");
                 } else if (kind == SoundKind.BACKGROUND) {
                     ps.println("BACKGROUND");
                 } else if (kind == SoundKind.SPATIAL) {
                     ps.println("SPATIAL");
                 }
                 ps.println(file_type);
                 ps.println(file_name);
                 ps.println(chorus);
                 ps.println(echo);
                 ps.println(flanger);
                 ps.println(gargle);
                 ps.println(reverb);
                 ps.println(volume);
                 ps.println(pan);
                 ps.println(preload);
                 ps.println(sf.getName());
 
                 ps.close();
             } catch (FileNotFoundException e) {
                 System.err.println("Error writing sound data " + data);
             }
 
         }
     }
 
     private void exportBackgrounds(File parentdir) {
         File bgDir = new File(parentdir, "backgrounds");
         bgDir.mkdir();
         ResourceList<Background> backgrounds = LGM.currentFile.resMap.getList(Background.class);
         Iterator<Background> bgI = backgrounds.iterator();
         while (bgI.hasNext()) {
             Background b = bgI.next();
             if (b.getBackgroundImage() == null) {
                 continue;
             }
             File bf = new File(bgDir, b.getName() + ".img");
 
             writeImage(b.getBackgroundImage(), bf, false);
             System.out.println("Wrote background " + bf);
 
             // TRANSPARENT,SMOOTH_EDGES,PRELOAD,USE_AS_TILESET,TILE_WIDTH,
             // TILE_HEIGHT,H_OFFSET,V_OFFSET,H_SEP, V_SEP
             boolean transparent = b.properties.get(PBackground.TRANSPARENT);
             boolean smooth = b.properties.get(PBackground.SMOOTH_EDGES);
             boolean preload = b.properties.get(PBackground.PRELOAD);
             boolean tileset = b.properties.get(PBackground.USE_AS_TILESET);
             int tile_width = b.properties.get(PBackground.TILE_WIDTH);
             int tile_height = b.properties.get(PBackground.TILE_HEIGHT);
             int h_offset = b.properties.get(PBackground.H_OFFSET);
             int v_offset = b.properties.get(PBackground.V_OFFSET);
             int h_sep = b.properties.get(PBackground.H_SEP);
             int v_sep = b.properties.get(PBackground.V_SEP);
 
             File f = new File(bgDir, b.getName() + ".dat");
             try {
                 PrintWriter s = new PrintWriter(f);
                 s.println(b.getName());
                 s.println(b.getId());
                 s.println(transparent);
                 s.println(smooth);
                 s.println(preload);
                 s.println(tileset);
                 s.println(tile_width);
                 s.println(tile_height);
                 s.println(h_offset);
                 s.println(v_offset);
                 s.println(h_sep);
                 s.println(v_sep);
                 s.println(bf.getName());
 
                 s.close();
             } catch (FileNotFoundException exc) {
                 System.err.println("Error writing background data for file " + f);
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     private void exportPaths(File parentdir) {
         File pathDir = new File(parentdir, "paths");
         pathDir.mkdir();
         ResourceList<Path> paths = LGM.currentFile.resMap.getList(Path.class);
         Iterator<Path> pathI = paths.iterator();
         while (pathI.hasNext()) {
             Path p = pathI.next();
             File f = new File(pathDir, p.getName() + ".dat");
             // SMOOTH,CLOSED,PRECISION,BACKGROUND_ROOM,SNAP_X,SNAP_Y
             boolean smooth = p.properties.get(PPath.SMOOTH);
             boolean closed = p.properties.get(PPath.CLOSED);
             int precision = p.properties.get(PPath.PRECISION);
             String background_room = "";
             ResourceReference<?> ref = p.properties.get(PPath.BACKGROUND_ROOM);
             if (ref.get() instanceof Room) {
                 background_room = ((ResourceReference<Room>) ref).get().getName();
             }
 
             int snapX = p.properties.get(PPath.SNAP_X);
             int snapY = p.properties.get(PPath.SNAP_Y);
             int points = p.points.size();
 
             PrintWriter s;
             try {
                 s = new PrintWriter(f);
 
                 s.println(p.getName());
                 s.println(p.getId());
                 s.print(smooth);
                 s.print(" ");
                 s.print(closed);
                 s.print(" ");
                 s.print(precision);
                 s.print(" ");
                 s.print(background_room);
                 s.print(" ");
                 s.print(snapX);
                 s.print(" ");
                 s.print(snapY);
                 s.print(" ");
                 s.println(points);
 
                 for (PathPoint pp : p.points) {
                     s.println(pp.getX() + "," + pp.getY() + "@" + pp.getSpeed());
                 }
                 s.close();
                 System.out.println("Exported path " + f);
 
             } catch (FileNotFoundException e) {
                 System.err.println("Cannot export path " + f);
             }
 
         }
     }
 
     private void exportScripts(File parentdir) {
         File scrDir = new File(parentdir, "scripts");
         scrDir.mkdir();
         ResourceList<Script> scrs = LGM.currentFile.resMap.getList(Script.class);
         Iterator<Script> scrI = scrs.iterator();
         while (scrI.hasNext()) {
             Script s = scrI.next();
             File f = new File(scrDir, s.getName() + ".gml");
             try {
                 StreamEncoder out = new StreamEncoder(f);
                 writeStr(s.getName(), out);
                 out.write4(s.getId());
                 writeStr(s.getCode(), out);
                 out.close();
                 System.out.println("Wrote Script " + f);
             } catch (IOException e) {
                 System.err.println("Couldn't write script " + f);
                 e.printStackTrace();
             }
 
         }
     }
 
     private void exportFonts(File parentdir) {
         File fntDir = new File(parentdir, "fonts");
         fntDir.mkdir();
         ResourceList<Font> fonts = LGM.currentFile.resMap.getList(Font.class);
         Iterator<Font> fntI = fonts.iterator();
         while (fntI.hasNext()) {
             Font font = fntI.next();
             File ff = new File(fntDir, font.getName() + ".dat");
             try {
                 // resource name,
                 // FONT_NAME,SIZE,BOLD,ITALIC,ANTIALIAS,CHARSET,RANGE_MIN,RANGE_MAX
                 String font_name = font.properties.get(PFont.FONT_NAME);
                 int size = font.properties.get(PFont.SIZE);
                 boolean bold = font.properties.get(PFont.BOLD);
                 boolean italic = font.properties.get(PFont.ITALIC);
                 int antialias = font.properties.get(PFont.ANTIALIAS);
                 int charset = font.properties.get(PFont.CHARSET);
                 int range_min = font.properties.get(PFont.RANGE_MIN);
                 int range_max = font.properties.get(PFont.RANGE_MAX);
 
                 PrintWriter s = new PrintWriter(ff);
 
                 s.println(font.getName());
                 s.println(font.getId());
                 s.println(font_name);
                 s.println(size);
                 s.println(bold);
                 s.println(italic);
                 s.println(antialias);
                 s.println(charset);
                 s.println(range_min);
                 s.println(range_max);
 
                 s.close();
 
                 System.out.println("Wrote font data " + ff);
             } catch (FileNotFoundException exc) {
                 System.err.println("Couldn't open font file for writing: " + ff);
             }
         }
 
     }
 
     private void exportTimelines(File parentdir) {
         File tlDir = new File(parentdir, "timelines");
         tlDir.mkdir();
         ResourceList<Timeline> tls = LGM.currentFile.resMap.getList(Timeline.class);
         Iterator<Timeline> tlI = tls.iterator();
         while (tlI.hasNext()) {
             Timeline t = tlI.next();
             File tlf = new File(tlDir, t.getName() + ".dat");
 
             try {
                 PrintWriter s = new PrintWriter(tlf);
                 s.println(t.getName());
                 s.println(t.getId());
                 for (Moment m : t.moments) {
                     s.println(m.stepNo);
                     for (Action a : m.actions) {
                         int i = writeAction(a);
                         s.println("#" + i);
                     }
                 }
                 s.close();
                 System.out.println("Wrote timeline " + tlf);
             } catch (IOException e) {
                 System.err.println("Can not write timeline data " + tlf);
             }
 
         }
     }
 
     private void exportGmObjects(File parentdir) {
         File objDir = new File(parentdir, "objects");
         objDir.mkdir();
         ResourceList<GmObject> objs = LGM.currentFile.resMap.getList(GmObject.class);
         Iterator<GmObject> objI = objs.iterator();
         while (objI.hasNext()) {
             GmObject obj = objI.next();
             File dat = new File(objDir, obj.getName() + ".dat");
 
             try {
                 // SPRITE,SOLID,VISIBLE,DEPTH,PERSISTENT,PARENT,MASK
                 PrintWriter s = new PrintWriter(dat);
                 s.println(obj.getName());
                 s.println(obj.getId());
                 s.println(getId((ResourceReference<?>) obj.get(PGmObject.SPRITE)));
                 s.println(obj.properties.get(PGmObject.SOLID));
                 s.println(obj.properties.get(PGmObject.VISIBLE));
                 s.println(obj.properties.get(PGmObject.DEPTH));
                 s.println(obj.properties.get(PGmObject.PERSISTENT));
                 s.println(getId((ResourceReference<?>) obj.get(PGmObject.PARENT), -100));
                 s.println(getId((ResourceReference<?>) obj.get(PGmObject.MASK)));
 
                 int numMainEvents = 11;// ver == 800 ? 12 : 11;
                 // Does not support custom trigger events
 
                 for (int j = 0; j < numMainEvents; j++) {
                     MainEvent me = obj.mainEvents.get(j);
                     for (Event ev : me.events) {
                         s.print(ev.mainId + ",");
                         if (j == MainEvent.EV_COLLISION)
                             s.println(getId(ev.other));
                         else
                             s.println(ev.id);
 
                         for (int i = 0; i < ev.actions.size(); i++) {
                             Action a = ev.actions.get(i);
                             int actn = writeAction(a);
                             String str;
                             if (i + 1 < ev.actions.size())
                                 str = ",";
                             else
                                 str = "";
                             s.print("#" + actn + str);
                         }
                         s.print('\n');
                     }
                 }
 
                 s.close();
                 System.out.println("Wrote object " + dat);
             } catch (IOException e) {
                 System.err.println("Can not write object data " + dat);
                 e.printStackTrace();
             }
 
         }
     }
 
     private void exportRooms(File parentdir) {
         File rmDir = new File(parentdir, "rooms");
         rmDir.mkdir();
 
         // Room Order
         File order = new File(rmDir, "rooms.lst");
         try {
             PrintWriter c = new PrintWriter(order);
             Enumeration<?> e = LGM.root.preorderEnumeration();
             while (e.hasMoreElements()) {
                 ResNode node = (ResNode) e.nextElement();
                 if (node.kind == org.lateralgm.resources.Room.class) {
                     org.lateralgm.resources.Room r = (org.lateralgm.resources.Room) deRef((ResourceReference<?>) node
                             .getRes());
                     if (r == null) {
                         // Probably the root Room folder
                         continue;
                     }
                     c.println(r.getName() + "," + r.getId());
                 }
             }
             c.close();
         } catch (FileNotFoundException exc) {
             System.err.println("Can not export room data " + order);
         }
 
         ResourceList<Room> rms = LGM.currentFile.resMap.getList(Room.class);
         Iterator<Room> rmI = rms.iterator();
         while (rmI.hasNext()) {
             Room r = rmI.next();
             File dat = new File(rmDir, r.getName() + ".dat");
             try {
                 // Creation code
                 String code = r.properties.get(PRoom.CREATION_CODE);
                 if (!code.equals("")) {
                     File cc = new File(rmDir, r.getName() + "_ccode.gml");
                     PrintWriter pw = new PrintWriter(cc);
                     pw.print(code);
                     pw.close();
                 }
 
                 PrintWriter w = new PrintWriter(dat);
                 // CAPTION,WIDTH,HEIGHT,SNAP_X,SNAP_Y,ISOMETRIC,SPEED,PERSISTENT,BACKGROUND_COLOR,
                 // DRAW_BACKGROUND_COLOR,CREATION_CODE,REMEMBER_WINDOW_SIZE,EDITOR_WIDTH,EDITOR_HEIGHT,SHOW_GRID,
                 // SHOW_OBJECTS,SHOW_TILES,SHOW_BACKGROUNDS,SHOW_FOREGROUNDS,SHOW_VIEWS,DELETE_UNDERLYING_OBJECTS,
                 // DELETE_UNDERLYING_TILES,CURRENT_TAB,SCROLL_BAR_X,SCROLL_BAR_Y,ENABLE_VIEWS
 
                 w.println(r.getName());
                 w.println(r.getId());
 
                 w.println(r.properties.get(PRoom.CAPTION));
                 w.println(r.properties.get(PRoom.WIDTH));
                 w.println(r.properties.get(PRoom.HEIGHT));
 
                 // USELESS: SNAP_X, SNAP_Y, ISOMETRIC
 
                 w.println(r.properties.get(PRoom.SPEED));
                 w.println(r.properties.get(PRoom.PERSISTENT));
                 Color c = r.properties.get(PRoom.BACKGROUND_COLOR);
                 w.println(c.getRGB());
                 w.println(r.properties.get(PRoom.DRAW_BACKGROUND_COLOR));
                 // Creation code, whether or not there is any
                 w.println(!code.equals(""));
                 w.println(r.backgroundDefs.size());
                 for (BackgroundDef b : r.backgroundDefs) {
                     // VISIBLE,FOREGROUND,BACKGROUND,X,Y,TILE_HORIZ,TILE_VERT,H_SPEED,V_SPEED,STRETCH
                     boolean visible = b.properties.get(PBackgroundDef.VISIBLE);
                     boolean foreground = b.properties.get(PBackgroundDef.FOREGROUND);
                     // bg id
                     int x = b.properties.get(PBackgroundDef.X);
                     int y = b.properties.get(PBackgroundDef.Y);
                     boolean tile_horiz = b.properties.get(PBackgroundDef.TILE_HORIZ);
                     boolean tile_vert = b.properties.get(PBackgroundDef.TILE_VERT);
                     int hspeed = b.properties.get(PBackgroundDef.H_SPEED);
                     int vspeed = b.properties.get(PBackgroundDef.V_SPEED);
                     boolean stretch = b.properties.get(PBackgroundDef.STRETCH);
 
                     print(w, visible, foreground,
                             getId((ResourceReference<?>) b.properties.get(PBackgroundDef.BACKGROUND)), x, y,
                             tile_horiz, tile_vert, hspeed, vspeed, stretch);
                 }
                 w.println(r.properties.get(PRoom.ENABLE_VIEWS));
                 w.println(r.views.size());
                 for (View view : r.views) {
                     // VISIBLE,VIEW_X,VIEW_Y,VIEW_W,VIEW_H,PORT_X,PORT_Y,PORT_W,PORT_H,
                     // BORDER_H,BORDER_V, SPEED_H, SPEED_V, OBJECT
                     boolean visible = view.properties.get(PView.VISIBLE);
                     int view_x = view.properties.get(PView.VIEW_X);
                     int view_y = view.properties.get(PView.VIEW_Y);
                     int view_w = view.properties.get(PView.VIEW_W);
                     int view_h = view.properties.get(PView.VIEW_H);
                     int port_x = view.properties.get(PView.PORT_X);
                     int port_y = view.properties.get(PView.PORT_Y);
                     int port_w = view.properties.get(PView.PORT_W);
                     int port_h = view.properties.get(PView.PORT_H);
                     int border_h = view.properties.get(PView.BORDER_H);
                     int border_v = view.properties.get(PView.BORDER_V);
                     int speed_h = view.properties.get(PView.SPEED_H);
                     int speed_v = view.properties.get(PView.SPEED_V);
                     print(w, visible, view_x, view_y, view_w, view_h, port_x, port_y, port_w, port_h, border_h,
                             border_v, speed_h, speed_v, getId((ResourceReference<?>) view.properties.get(PView.OBJECT)));
                 }
                 w.println(r.instances.size());
 
                 for (Instance in : r.instances) {
                     String in_code = in.getCreationCode();
                     String ccode_file = "null";
                     if (!in_code.equals("")) {
                         int index = iccode_index++;
                         File ccf = new File(rmDir, "c_" + index + ".gml");
                         PrintWriter ccw = new PrintWriter(ccf);
                         ccw.print(in_code);
                         ccw.close();
                         ccode_file = "@" + index;
                     }
                     ResourceReference<GmObject> or = in.properties.get(PInstance.OBJECT);
                     print(w, in.getPosition().x, in.getPosition().y, getId(or), in.properties.get(PInstance.ID),
                             ccode_file);
                 }
                 w.println(r.tiles.size());
 
                 for (Tile tile : r.tiles) {
                     ResourceReference<Background> rb = tile.properties.get(PTile.BACKGROUND);
 
                     print(w, tile.getRoomPosition().x, tile.getRoomPosition().y, tile.getBackgroundPosition().x,
                             tile.getBackgroundPosition().y, tile.getSize().width, tile.getSize().height,
                             tile.getDepth(), getId(rb), tile.properties.get(PTile.ID));
                 }
                 // Rest of the stuff is useless
 
                 w.close();
                 System.out.println("Wrote room " + dat);
             } catch (FileNotFoundException e) {
                 System.err.println("Cannot write room " + dat);
             }
         }
     }
 
     private void exportGameInfo(File parentdir) {
         GameInformation info = LGM.currentFile.gameInfo;
 
         File infoFile = new File(parentdir, "Game Information.rtf");
         File settingsFile = new File(parentdir, "Game Information.dat");
 
         String text = info.get(PGameInformation.TEXT);
         try {
             // Thanks LateralGM XD
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(infoFile));
             int val = text.length();
             out.write(val & 255);
             out.write((val >> 8) & 255);
             out.write((val >> 16) & 255);
             out.write((val >> 24) & 255);
 
             byte[] encoded = text.getBytes(Charset.forName("ISO-8859-1"));
             out.write(encoded);
             out.close();
 
             // BACKGROUND_COLOR,MIMIC_GAME_WINDOW,FORM_CAPTION,LEFT,TOP,WIDTH,HEIGHT,SHOW_BORDER,
             // ALLOW_RESIZE, STAY_ON_TOP,PAUSE_GAME,TEXT
             Color bg_color = info.get(PGameInformation.BACKGROUND_COLOR);
             String caption = info.get(PGameInformation.FORM_CAPTION);
             int left = info.get(PGameInformation.LEFT);
             int top = info.get(PGameInformation.TOP);
             int width = info.get(PGameInformation.WIDTH);
             int height = info.get(PGameInformation.HEIGHT);
             boolean mimic = info.get(PGameInformation.MIMIC_GAME_WINDOW);
             boolean show_border = info.get(PGameInformation.SHOW_BORDER);
             boolean allow_resize = info.get(PGameInformation.ALLOW_RESIZE);
             boolean stay_on_top = info.get(PGameInformation.STAY_ON_TOP);
             boolean pause_game = info.get(PGameInformation.PAUSE_GAME);
 
             PrintWriter s = new PrintWriter(settingsFile);
             s.println(bg_color.getRGB());
             s.println(caption);
             s.println(left);
             s.println(top);
             s.println(width);
             s.println(height);
             s.println(mimic);
             s.println(show_border);
             s.println(allow_resize);
             s.println(stay_on_top);
             s.println(pause_game);
             s.close();
 
             System.out.println("Wrote Game Information.");
 
         } catch (IOException e) {
             System.err.println("Couldn't write Game information");
             e.printStackTrace();
         }
     }
 
     private void exportSettings(File parentdir) {
         // aH WHO CARES!
     }
 
     private int writeAction(Action act) throws IOException {
         final int index = action_index++;
         File f = new File(actionDir, "a_" + index + ".dat");
         LibAction la = act.getLibAction();
 
         StreamEncoder out = new StreamEncoder(f);
         out.write4(la.parent != null ? la.parent.id : la.parentId);
         out.write4(la.id);
 
         List<Argument> args = act.getArguments();
         out.write4(args.size());
         for (Argument arg : args) {
             out.write(arg.kind);
             Class<? extends Resource<?, ?>> kind = Argument.getResourceKind(arg.kind);
 
             if (kind != null && InstantiableResource.class.isAssignableFrom(kind)) {
                 out.write(0);
                 Resource<?, ?> r = deRef((ResourceReference<?>) arg.getRes());
                 if (r != null && r instanceof InstantiableResource<?, ?>)
                     out.write4(((InstantiableResource<?, ?>) r).getId());
                 else
                     out.write4(0);
             } else {
                 if (la.actionKind == Action.ACT_CODE) {
                     out.write(2);
                     // There should only be 1 argument for Code type action
                     writeStr(arg.getVal(), out);
                 } else {
                     out.write(1);
                     writeStr(arg.getVal(), out);
                 }
             }
         }
         ResourceReference<GmObject> at = act.getAppliesTo();
         if (at != null) {
             if (at == GmObject.OBJECT_OTHER)
                 out.write4(-2);
             else if (at == GmObject.OBJECT_SELF)
                 out.write4(-1);
             else
                 out.write4(getId(at, -100));
         } else {
             out.write4(-100);
         }
         writeBool(act.isRelative(), out);
         writeBool(act.isNot(), out);
         out.close();
 
         return index;
     }
 
     private void writeImage(BufferedImage i, File f, boolean useTransp) {
 
         int width = i.getWidth();
         int height = i.getHeight();
 
         int trans = i.getRGB(0, height - 1) & 0x00FFFFFF;
         // gotta do 2 factor
         int texWidth = 2;
         int texHeight = 2;
 
         // find the closest power of 2 for the width and height
         // of the produced texture
         while (texWidth < width) {
             texWidth *= 2;
         }
         while (texHeight < height) {
             texHeight *= 2;
         }

         int pixels[] = i.getRGB(0, 0, width, height, null, 0, width);
         try {
             ByteArrayOutputStream baos = new ByteArrayOutputStream(pixels.length * 4);
             DeflaterOutputStream dos = new DeflaterOutputStream(baos);
 
             // RGBA
             write4(width, dos);
             write4(height, dos);
             write4(texWidth, dos);
             write4(texHeight, dos);
             for (int p = 0; p < pixels.length; p++) {
                 dos.write(pixels[p] >>> 16 & 0xFF);
                 dos.write(pixels[p] >>> 8 & 0xFF);
                 dos.write(pixels[p] & 0xFF);
                 if (useTransp && ((pixels[p] & 0x00FFFFFF) == trans))
                     dos.write(0);
                 else
                     dos.write(pixels[p] >>> 24);
             }
 
             dos.finish();
 
             FileOutputStream out = new FileOutputStream(f);
             out.write(baos.toByteArray());
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Borrowed from {@link GmStreamEncoder#writeId}
      * 
      * @param id
      * @param noneval
      * @throws IOException
      */
     private <R extends Resource<R, ?>> int getId(ResourceReference<R> id, int noneval) {
         R r = deRef(id);
         if (r != null && r instanceof InstantiableResource<?, ?>)
             return ((InstantiableResource<?, ?>) r).getId();
         else
             return noneval;
     }
 
     private <R extends Resource<R, ?>> int getId(ResourceReference<R> id) {
         return getId(id, -1);
     }
 
     private void print(PrintWriter s, Object... args) {
         String line = "";
         for (Object a : args) {
             line = line + a + ",";
         }
         s.println(line.substring(0, line.length() - 1));
     }
 
     private void writeStr(String str, StreamEncoder out) throws IOException {
         byte[] encoded = str.getBytes();
         out.write4(encoded.length);
         out.write(encoded);
     }
 
     private void writeBool(boolean bool, StreamEncoder out) throws IOException {
         out.write(bool ? 1 : 0);
     }
 
     private void write4(int val, OutputStream s) throws IOException {
         s.write(val & 255);
         s.write((val >>> 8) & 255);
         s.write((val >>> 16) & 255);
         s.write((val >>> 24) & 255);
     }
 }
