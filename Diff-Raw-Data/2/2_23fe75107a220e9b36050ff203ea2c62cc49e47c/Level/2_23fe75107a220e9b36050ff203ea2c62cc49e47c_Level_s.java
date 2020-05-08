 package org.spacebar.escape.common;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import org.spacebar.escape.common.hash.FNV32;
 
 public class Level {
 
     public static class MetaData {
         final public int width;
 
         final public int height;
 
         final public String title;
 
         final public String author;
 
         public MetaData(int width, int height, String title, String author) {
             this.width = width;
             this.height = height;
             this.title = title;
             this.author = author;
         }
     }
 
     // panel colors
     public final static int PANEL_REGULAR = 0;
 
     public final static int PANEL_BLUE = 1;
 
     public final static int PANEL_GREEN = 2;
 
     public final static int PANEL_RED = 3;
 
     // panels under tiles
     public final static int TF_NONE = 0;
 
     /* panel under tile (ie, pushable block) */
     /*
      * if HASPANEL is set, then TF_RPANELH * 2 + TF_RPANELL says what kind (see
      * panel colors above)
      */
     public final static int TF_HASPANEL = 1;
 
     public final static int TF_RPANELL = 4;
 
     public final static int TF_RPANELH = 8;
 
     /* panel under tile in bizarro world */
     /* same refinement */
     public final static int TF_OPANEL = 2;
 
     public final static int TF_ROPANELL = 16;
 
     public final static int TF_ROPANELH = 32;
 
     public final static int TF_TEMP = 64; // used for swapping during play
 
     // panels
     public final static int T_FLOOR = 0;
 
     public final static int T_RED = 1;
 
     public final static int T_BLUE = 2;
 
     public final static int T_GREY = 3;
 
     public final static int T_GREEN = 4;
 
     public final static int T_EXIT = 5;
 
     public final static int T_HOLE = 6;
 
     public final static int T_GOLD = 7;
 
     public final static int T_LASER = 8;
 
     public final static int T_PANEL = 9;
 
     public final static int T_STOP = 10;
 
     public final static int T_RIGHT = 11;
 
     public final static int T_LEFT = 12;
 
     public final static int T_UP = 13;
 
     public final static int T_DOWN = 14;
 
     public final static int T_ROUGH = 15;
 
     public final static int T_ELECTRIC = 16;
 
     public final static int T_ON = 17;
 
     public final static int T_OFF = 18;
 
     public final static int T_TRANSPORT = 19;
 
     public final static int T_BROKEN = 20;
 
     public final static int T_LR = 21;
 
     public final static int T_UD = 22;
 
     public final static int T_0 = 23;
 
     public final static int T_1 = 24;
 
     public final static int T_NS = 25;
 
     public final static int T_NE = 26;
 
     public final static int T_NW = 27;
 
     public final static int T_SE = 28;
 
     public final static int T_SW = 29;
 
     public final static int T_WE = 30;
 
     public final static int T_BUTTON = 31;
 
     public final static int T_BLIGHT = 32;
 
     public final static int T_RLIGHT = 33;
 
     public final static int T_GLIGHT = 34;
 
     public final static int T_BLACK = 35;
 
     public final static int T_BUP = 36;
 
     public final static int T_BDOWN = 37;
 
     public final static int T_RUP = 38;
 
     public final static int T_RDOWN = 39;
 
     public final static int T_GUP = 40;
 
     public final static int T_GDOWN = 41;
 
     public final static int T_BSPHERE = 42;
 
     public final static int T_RSPHERE = 43;
 
     public final static int T_GSPHERE = 44;
 
     public final static int T_SPHERE = 45;
 
     public final static int T_TRAP2 = 46;
 
     public final static int T_TRAP1 = 47;
 
     public final static int T_BPANEL = 48;
 
     public final static int T_RPANEL = 49;
 
     public final static int T_GPANEL = 50;
 
     public final static int T_STEEL = 51;
 
     public final static int T_BSTEEL = 52;
 
     public final static int T_RSTEEL = 53;
 
     public final static int T_GSTEEL = 54;
 
     /**
      * @return Returns the author.
      */
     public String getAuthor() {
         return author;
     }
 
     /**
      * @return Returns the title.
      */
     public String getTitle() {
         return title;
     }
 
     public int getBotX(int index) {
         return bots[index].getX();
     }
 
     public int getBotY(int index) {
         return bots[index].getY();
     }
 
     public int getBotType(int index) {
         return bots[index].getBotType();
     }
 
     public int getPlayerX() {
         return player.getX();
     }
 
     public int getPlayerY() {
         return player.getY();
     }
 
     public int getHeight() {
         return height;
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getBotCount() {
         return bots.length;
     }
 
     // metadata
     private final String title;
 
     private final String author;
 
     // width, height
     final int width;
 
     final int height;
 
     final private Player player;
 
     // shown
     private final int tiles[];
 
     // "other" (tiles swapped into bizarro world by panels)
     private final int oTiles[];
 
     // destinations for transporters and panels (as index into tiles)
     private final int dests[];
 
     // has a panel (under a pushable block)? etc.
     private final int flags[];
 
     private final Bot bots[];
 
     // dirty
     public final DirtyList dirty;
 
     // cached laser
     private IntTriple laser;
 
     // the meat
     private void warp(Entity ent, int targX, int targY) {
         checkStepOff(ent.getX(), ent.getY());
         ent.setX(targX);
         ent.setY(targY);
 
         switch (tileAt(targX, targY)) {
         case T_PANEL:
             swapO(destAt(targX, targY));
             break;
         default:
             ;
         }
     }
 
     private IntPair where(int idx) {
         int x = idx % width;
         int y = idx / width;
 
         return new IntPair(x, y);
     }
 
     int index(int x, int y) {
         return (y * width) + x;
     }
 
     public int tileAt(int i) {
         return tiles[i];
     }
 
     public int tileAt(int x, int y) {
         return tiles[y * width + x];
     }
 
     public int oTileAt(int x, int y) {
         return oTiles[y * width + x];
     }
 
     private void setTile(int i, int t) {
         tiles[i] = t;
         dirty.setDirty(i);
     }
 
     private void setTile(int x, int y, int t) {
         setTile(y * width + x, t);
     }
 
     private int destAt(int x, int y) {
         return dests[y * width + x];
     }
 
     private int flagAt(int x, int y) {
         return flags[y * width + x];
     }
 
     public boolean isWon() {
         return tileAt(player.getX(), player.getY()) == T_EXIT;
     }
 
     private boolean travel(int x, int y, int d, IntPair result) {
         switch (d) {
         case Entity.DIR_UP:
             if (y == 0) {
                 return false;
             } else {
                 result.x = x;
                 result.y = y - 1;
                 return true;
             }
         case Entity.DIR_DOWN:
             if (y == (height - 1)) {
                 return false;
             } else {
                 result.x = x;
                 result.y = y + 1;
                 return true;
             }
         case Entity.DIR_LEFT:
             if (x == 0) {
                 return false;
             } else {
                 result.x = x - 1;
                 result.y = y;
                 return true;
             }
         case Entity.DIR_RIGHT:
             if (x == (width - 1)) {
                 return false;
             } else {
                 result.x = x + 1;
                 result.y = y;
                 return true;
             }
         default:
             return false; /* ?? */
         }
     }
 
     // Return true if a laser can 'see' the player.
     public boolean isDead() {
         // bots kill, without laser
         if (isBotAt(player.getX(), player.getY())) {
             laser = null;
             return true;
         }
 
         // otherwise, look for lasers from the current dude
         for (int dd = Entity.FIRST_DIR; dd <= Entity.LAST_DIR; dd++) {
             int lx = player.getX(), ly = player.getY();
 
             IntPair r = new IntPair();
             while (travel(lx, ly, dd, r)) {
                 lx = r.x;
                 ly = r.y;
 
                 if (tileAt(lx, ly) == T_LASER) {
                     int tileX = r.x;
                     int tileY = r.y;
                     int d = Entity.dirReverse(dd);
 
                     laser = new IntTriple(tileX, tileY, d);
                     return true;
                 }
                 int tt = tileAt(lx, ly);
                 if (tt != T_FLOOR && tt != T_ELECTRIC && tt != T_ROUGH
                         && tt != T_RDOWN && tt != T_GDOWN && tt != T_BDOWN
                         && tt != T_TRAP2 && tt != T_TRAP1 && tt != T_PANEL
                         && tt != T_BPANEL && tt != T_GPANEL && tt != T_RPANEL
                         && tt != T_BLACK && tt != T_HOLE)
                     break;
                 // all robots also block lasers
                 if (isBotAt(lx, ly))
                     break;
             }
         }
         laser = null;
         return false;
     }
 
     private void swapO(int idx) {
         int tmp = tiles[idx];
         setTile(idx, oTiles[idx]);
         oTiles[idx] = tmp;
 
         /* swap haspanel/opanel and their refinements as well */
         flags[idx] =
 
         /* panel bits */
         ((flags[idx] & TF_HASPANEL) != 0 ? TF_OPANEL : TF_NONE)
                 | ((flags[idx] & TF_OPANEL) != 0 ? TF_HASPANEL : TF_NONE)
                 |
 
                 /* refinement */
                 ((flags[idx] & TF_RPANELL) != 0 ? TF_ROPANELL : TF_NONE)
                 | ((flags[idx] & TF_RPANELH) != 0 ? TF_ROPANELH : TF_NONE)
                 |
 
                 /* orefinement */
                 ((flags[idx] & TF_ROPANELL) != 0 ? TF_RPANELL : TF_NONE)
                 | ((flags[idx] & TF_ROPANELH) != 0 ? TF_RPANELH : TF_NONE)
                 |
 
                 /* erase old */
                 (flags[idx] & ~(TF_HASPANEL | TF_OPANEL | TF_RPANELL
                         | TF_RPANELH | TF_ROPANELL | TF_ROPANELH));
     }
 
     /*
      * after stepping off a tile, deactivate a panel if there was one there.
      */
     private void checkLeavePanel(int x, int y) {
         /* nb: only for regular panels */
         if (tileAt(x, y) == T_PANEL) {
             swapO(destAt(x, y));
         }
     }
 
     /* actions on the player stepping off of a tile */
     private void checkStepOff(int x, int y) {
         /* nb: only for regular panels */
         checkLeavePanel(x, y);
         if (tileAt(x, y) == T_TRAP1) {
             setTile(x, y, T_HOLE);
         } else if (tileAt(x, y) == T_TRAP2) {
             setTile(x, y, T_TRAP1);
         }
     }
 
     private static int realPanel(int f) {
         if ((f & TF_RPANELH) != 0) {
             if ((f & TF_RPANELL) != 0)
                 return T_RPANEL;
             else
                 return T_GPANEL;
         } else {
             if ((f & TF_RPANELL) != 0)
                 return T_BPANEL;
             else
                 return T_PANEL;
         }
     }
 
     static private boolean isPanel(int t) {
         return (t == T_PANEL || t == T_RPANEL || t == T_GPANEL || t == T_BPANEL);
     }
 
     static private boolean isSphere(int t) {
         return (t == T_SPHERE || t == T_RSPHERE || t == T_GSPHERE || t == T_BSPHERE);
     }
 
     static private boolean isSteel(int t) {
         return (t == T_STEEL || t == T_RSTEEL || t == T_GSTEEL || t == T_BSTEEL);
     }
 
     private void swapTiles(int t1, int t2) {
         for (int i = (width * height) - 1; i >= 0; i--) {
             if (tiles[i] == t1)
                 setTile(i, t2);
             else if (tiles[i] == t2)
                 setTile(i, t1);
         }
     }
 
     public boolean move(int d) {
         return move(d, null);
     }
 
     public boolean move(int d, Effects e) {
         player.setDir(d); // always set dir
         boolean result = realMove(player, d, e);
 
         if (result) {
             if (e != null) {
                 e.doStep();
             }
 
             // move bots
             for (int i = 0; i < bots.length; i++) {
                 Bot b = bots[i];
                 if (b.getBotType() == Entity.B_DELETED) {
                     continue;
                 }
                 IntPair dirs = b.getDirChoices(player);
 
                 if (dirs.x != Entity.DIR_NONE) {
                     boolean bm = realMove(b, dirs.x, e);
 
                     // no good? try 2nd move
                     if (!bm && dirs.y != Entity.DIR_NONE) {
                         realMove(b, dirs.y, e);
                     }
                 }
             }
         } else {
             if (e != null) {
                 e.doNoStep();
             }
         }
 
         isDead(); // update laser cache
         return result;
     }
 
     private boolean realMove(Entity ent, int d, Effects e) {
         final IntPair newP = new IntPair();
         if (travel(ent.getX(), ent.getY(), d, newP)) {
             return maybeDoMove(ent, d, e, newP);
         } else
             return false; // no move for sure
     }
 
     /**
      * @param ent
      * @param d
      * @param e
      * @param newP
      * @return true if move was made, false otherwise
      */
     private boolean maybeDoMove(Entity ent, int d, Effects e, final IntPair newP) {
         final int target;
         switch (target = tileAt(newP.x, newP.y)) {
 
         /* these aren't pressed by the player so act like floor */
         case T_BPANEL:
         case T_GPANEL:
         case T_RPANEL:
 
         /* these are only affected when we step *off* */
         case T_TRAP2:
         case T_TRAP1:
 
         case T_FLOOR:
         case T_ROUGH:
         case T_BDOWN:
         case T_RDOWN:
         case T_GDOWN:
         case T_PANEL:
             return doFloorMove(ent, d, target, newP);
 
         case T_EXIT:
             return doExitMove(ent, newP);
 
         case T_ON:
             return doElectricOffMove(e, newP);
 
         case T_0:
         case T_1:
             return doToggleMove(e, target, newP);
 
         case T_BSPHERE:
         case T_RSPHERE:
         case T_GSPHERE:
         case T_SPHERE:
         case T_GOLD:
             return doSphereGoldMove(d, e, target, newP);
 
         case T_TRANSPORT:
             return doTransportMove(ent, e, newP);
 
         case T_BUTTON:
             return doButtonMove(e, newP);
 
         case T_BROKEN:
             return doBrokenMove(e, newP);
 
         case T_GREEN:
             return doGreenBlockMove(ent, d, newP);
 
         // steel
         case T_STEEL:
         case T_RSTEEL:
         case T_GSTEEL:
         case T_BSTEEL:
             return doSteelMove(ent, d, newP);
 
         // simple pushable blocks use this case
         case T_RED:
         case T_NS:
         case T_NE:
         case T_NW:
         case T_SE:
         case T_SW:
         case T_WE:
 
         case T_LR:
         case T_UD:
 
         case T_GREY:
             return doSimpleBlockMove(ent, d, e, target, newP);
 
         case T_ELECTRIC:
             return doZapMove(ent, newP);
 
         case T_BLUE:
         case T_HOLE:
         case T_LASER:
         case T_STOP:
         case T_RIGHT:
         case T_LEFT:
         case T_UP:
         case T_DOWN:
         case T_BLIGHT:
         case T_RLIGHT:
         case T_GLIGHT:
         case T_RUP:
         case T_BUP:
         case T_GUP:
         case T_OFF:
         case T_BLACK:
         default:
             return false;
         }
     }
 
     /**
      * @param d
      * @param e
      * @param newP
      * @return
      */
     private boolean doSphereGoldMove(int d, Effects e, int target, IntPair newP) {
         /*
          * spheres allow pushing in a line: ->OOOO becomes OOO ---->O
          * 
          * so keep travelling while the tile in the destination direction is a
          * sphere of any sort.
          */
         IntPair t = new IntPair();
         while (isSphere(tileAt(newP.x, newP.y))
                 && !(player.isAt(newP.x, newP.y) || isBotAt(newP.x, newP.y))
                 && travel(newP.x, newP.y, d, t) && isSphere(tileAt(t.x, t.y))) {
             newP = new IntPair(t);
             target = tileAt(newP.x, newP.y);
         }
 
         // can't push if entity there
         if (player.isAt(newP.x, newP.y) || isBotAt(newP.x, newP.y)) {
             //                    return false;
         }
 
         int goldX = newP.x, goldY = newP.y;
 
         /* remove gold block */
         if ((flagAt(goldX, goldY) & TF_HASPANEL) != 0) {
             setTile(goldX, goldY, realPanel(flagAt(goldX, goldY)));
         } else {
             setTile(goldX, goldY, T_FLOOR);
         }
 
         IntPair tGold = new IntPair();
         while (travel(goldX, goldY, d, tGold)) {
 
             int next = tileAt(tGold.x, tGold.y);
             if (!(next == T_ELECTRIC || next == T_PANEL || next == T_BPANEL
                     || next == T_RPANEL || next == T_GPANEL || next == T_FLOOR
                     || isBotAt(tGold.x, tGold.y) || player.isAt(tGold.x,
                     tGold.y))) {
                 break;
             }
 
             goldX = tGold.x;
             goldY = tGold.y;
 
             if (next == T_ELECTRIC)
                 break;
         }
 
         /* goldx is dest, newx is source */
         if (goldX != newP.x || goldY != newP.y) {
 
             int landOn = tileAt(goldX, goldY);
             boolean doSwap = false;
 
             /* untrigger from source */
             if ((flagAt(newP.x, newP.y) & TF_HASPANEL) != 0) {
                 int pan = realPanel(flagAt(newP.x, newP.y));
                 /* any */
                 if (pan == T_PANEL ||
                 /* colors */
                 (target == T_GSPHERE && pan == T_GPANEL)
                         || (target == T_RSPHERE && pan == T_RPANEL)
                         || (target == T_BSPHERE && pan == T_BPANEL))
                     doSwap = true;
             }
 
             /*
              * only the correct color sphere can trigger the colored panels
              */
             boolean doSwapT = triggers(target, landOn);
 
             setTile(goldX, goldY, target);
 
             //            boolean zapped = false;
             if (landOn == T_ELECTRIC) {
                 /*
                  * gold zapped. however, if the electric was the newTarget of a
                  * panel that we just left, the electric has been swapped into
                  * the o world (along with the gold). So swap there.
                  */
                 if (e != null) {
                     e.doZap();
                 }
                 setTile(goldX, goldY, T_ELECTRIC);
 
                 //                zapped = true;
             }
 
             if (doSwapT) {
                 swapO(destAt(goldX, goldY));
             }
 
             if (doSwap) {
                 swapO(destAt(goldX, goldY));
             }
 
             if (e != null) {
                 e.doSlide();
             }
 
             return true;
         } else {
             // didn't move, put it back
             setTile(newP.x, newP.y, target);
 
             return false;
         }
     }
 
     /**
      * @param ent
      * @param newP
      * @return
      */
     private boolean doZapMove(Entity ent, IntPair newP) {
         // some bots are stupid enough to zap themselves
         if (ent != player && ent.zapsSelf()) {
             // move
             ent.setX(newP.x);
             ent.setY(newP.y);
 
             // kill
             ((Bot) ent).delete();
             return true;
         } else
             return false;
     }
 
     /**
      * @param ent
      * @param d
      * @param e
      * @param target
      * @param newP
      * @return
      */
     private boolean doSimpleBlockMove(Entity ent, int d, Effects e, int target,
             IntPair newP) {
         /*
          * we're always stepping onto the panel that the block was on, so we
          * don't need to change its state. (if it's a regular panel, then don't
          * change because our feet are on it. if it's a colored panel, don't
          * change because neither the man nor the block can activate it.) But we
          * do need to put a panel there instead of floor.
          */
 
         int replacement = ((flagAt(newP.x, newP.y) & TF_HASPANEL) == TF_HASPANEL) ? realPanel(flagAt(
                 newP.x, newP.y))
                 : T_FLOOR;
 
         boolean doSwap = false;
         //        boolean zap = false;
         //        boolean hole = false;
         IntPair dest = new IntPair();
 
         if (target == T_LR && (d == Entity.DIR_UP || d == Entity.DIR_DOWN))
             return false;
         if (target == T_UD && (d == Entity.DIR_LEFT || d == Entity.DIR_RIGHT))
             return false;
 
         if (travel(newP.x, newP.y, d, dest)) {
             int destT = tileAt(dest.x, dest.y);
             if (player.isAt(dest.x, dest.y) || isBotAt(dest.x, dest.y)) {
                 return false;
             }
             switch (destT) {
             case T_FLOOR:
                 /* easy */
                 setTile(dest.x, dest.y, target);
                 setTile(newP.x, newP.y, replacement);
                 break;
             case T_ELECTRIC:
                 /* Zap! */
                 if (target != T_LR && target != T_UD) {
                     if (e != null) {
                         e.doZap();
                     }
                     setTile(newP.x, newP.y, replacement);
                 } else
                     return false;
                 //                zap = true;
                 break;
             case T_HOLE:
                 /* only grey blocks into holes */
                 if (target == T_GREY) {
                     if (e != null) {
                         e.doHole();
                     }
                     setTile(dest.x, dest.y, T_FLOOR);
                     setTile(newP.x, newP.y, replacement);
                     //                    hole = true;
                     break;
                 } else
                     return false;
             case T_BPANEL:
             case T_RPANEL:
             case T_GPANEL:
             case T_PANEL:
                 if (target != T_LR && target != T_UD) {
                     /* delay the swap */
                     doSwap = (destT == T_PANEL); // grey down holes
                     setTile(dest.x, dest.y, target);
                     setTile(newP.x, newP.y, replacement);
                 } else
                     return false;
                 break;
             default:
                 return false;
             }
             checkStepOff(ent.getX(), ent.getY());
 
             if (doSwap)
                 swapO(destAt(dest.x, dest.y));
 
             ent.setX(newP.x);
             ent.setY(newP.y);
             return true;
         } else
             return false;
     }
 
     /**
      * @param ent
      * @param d
      * @param newP
      * @return
      */
     private boolean doSteelMove(Entity ent, int d, IntPair newP) {
         /*
          * three phases. first, see if we can push this whole column one space.
          * 
          * if so, generate animations.
          * 
          * then, update panel states. this is tricky.
          */
 
        IntPair dest = newP;
         {
             int curx = newP.x, cury = newP.y;
             /*
              * go until not steel, or if we hit a robot anywhere along this, end
              */
             while (!isBotAt(curx, cury) && !player.isAt(curx, cury)
                     && travel(curx, cury, d, dest)
                     && isSteel(tileAt(dest.x, dest.y))) {
                 curx = dest.x;
                 cury = dest.y;
             }
         }
 
         /* entity in our column or at the end? sorry */
         if (isBotAt(dest.x, dest.y) || player.isAt(dest.x, dest.y))
             return false;
 
         /* what did we hit? */
         int hitTile = tileAt(dest.x, dest.y);
         boolean zap = false;
         switch (hitTile) {
         /*
          * nb if we "hit" steel, then it's steel to the edge of the level, so no
          * push.
          */
         case T_PANEL:
         case T_GPANEL:
         case T_BPANEL:
         case T_RPANEL:
         case T_FLOOR:
             break;
         case T_ELECTRIC:
             zap = true;
             break;
         default:
             return (false);
         }
 
         /*
          * guy destx,desty v v [ ][S][S][S][S][ ] ^ steels starting at newx,newy
          * 
          * d ---->
          */
         int revD = Entity.dirReverse(d);
 
         /* move the steel blocks first. */
         {
             int moveX = dest.x, moveY = dest.y;
             IntPair next = new IntPair();
             while (!(moveX == newP.x && moveY == newP.y)) {
                 travel(moveX, moveY, revD, next);
                 setTile(moveX, moveY, tileAt(next.x, next.y));
                 moveX = next.x;
                 moveY = next.y;
             }
         }
 
         /* and one more, for the tile that we're stepping onto */
         {
             int replacement = ((flagAt(newP.x, newP.y) & TF_HASPANEL) == TF_HASPANEL) ? realPanel(flagAt(
                     newP.x, newP.y))
                     : T_FLOOR;
             setTile(newP.x, newP.y, replacement);
         }
 
         /*
          * reconcile panels.
          * 
          * imagine pushing a row of blocks one space to the right.
          * 
          * we loop over the NEW positions for the steel blocks. If a steel block
          * is on a panel (that it can trigger), then we trigger that panel as
          * long as the thing to its right (which used to be there) couldn't
          * trigger it. this handles new panels that are turned ON.
          * 
          * if we can't trigger the panel, then we check to see if the panel to
          * our right (which used to be there) also can't trigger it. If so, we
          * don't do anything. Otherwise, we "untrigger" the panel.
          * 
          * To simplify, if triggerstatus_now != triggerstatus_old, we trigger.
          * (Trigger has the same effect as untriggering.)
          * 
          * Because these swaps are supposed to be delayed, we set the TF_TEMP
          * flag if the tile should do a swap afterwards.
          */
 
         boolean swapNew = false;
         {
             int lookX = dest.x, lookY = dest.y;
             int prevT = T_FLOOR; /* anything that doesn't trigger */
             IntPair next = new IntPair();
             while (!(lookX == newP.x && lookY == newP.y)) {
 
                 int hereT = tileAt(lookX, lookY);
 
                 /* triggerstatus for this location (lookx, looky) */
                 boolean triggerStatusNow = ((flagAt(lookX, lookY) & TF_HASPANEL) == TF_HASPANEL)
                         && triggers(hereT, realPanel(flagAt(lookX, lookY)));
 
                 boolean triggerStatusOld = ((flagAt(lookX, lookY) & TF_HASPANEL) == TF_HASPANEL)
                         && isSteel(prevT)
                         && triggers(prevT, realPanel(flagAt(lookX, lookY)));
 
                 if (triggerStatusNow != triggerStatusOld) {
                     setFlag(lookX, lookY, flagAt(lookX, lookY) | TF_TEMP);
                     //           printf("Yes swap at %d/%d\n", lookx, looky);
                 } else
                     setFlag(lookX, lookY, flagAt(lookX, lookY) & ~TF_TEMP);
 
                 prevT = hereT;
 
                 travel(lookX, lookY, revD, next);
 
                 lookX = next.x;
                 lookY = next.y;
             }
 
             /* first panel is slightly different */
             {
                 int first = tileAt(newP.x, newP.y);
                 boolean trigNow = (first == T_PANEL);
                 boolean trigOld = isPanel(first)
                         && triggers(prevT, realPanel(flagAt(newP.x, newP.y)));
 
                 if (trigOld != trigNow) {
                     swapNew = true;
                 }
             }
         } /* zap, if necessary, before swapping */
         if (zap) {
             setTile(dest.x, dest.y, T_ELECTRIC);
             /* XXX animate */
         }
 
         /* now we can start swapping. */
         checkStepOff(ent.getX(), ent.getY());
 
         /*
          * this part is now invariant to order, because there is only one
          * destination per location
          */
 
         if (swapNew) {
             swapO(destAt(newP.x, newP.y));
         }
 
         {
             int lookx = dest.x, looky = dest.y;
             IntPair next = new IntPair();
             while (!(lookx == newP.x && looky == newP.y)) {
 
                 if ((flagAt(lookx, looky) & TF_TEMP) == TF_TEMP) {
                     swapO(destAt(lookx, looky));
                     setFlag(lookx, looky, flagAt(lookx, looky) & ~TF_TEMP);
                 }
 
                 /* next */
                 travel(lookx, looky, revD, next);
                 lookx = next.x;
                 looky = next.y;
             }
         }
 
         /* XXX also boundary conditions? (XXX what does that mean?) */
         ent.setX(newP.x);
         ent.setY(newP.y);
 
         return true;
     }
 
     /**
      * @param ent
      * @param d
      * @param newP
      * @return
      */
     private boolean doGreenBlockMove(Entity ent, int d, IntPair newP) {
         IntPair dest = new IntPair();
         if (travel(newP.x, newP.y, d, dest)) {
             if (tileAt(dest.x, dest.y) == T_FLOOR && !isBotAt(dest.x, dest.y)
                     && !player.isAt(dest.x, dest.y)) {
                 setTile(dest.x, dest.y, T_BLUE);
                 setTile(newP.x, newP.y, T_FLOOR);
 
                 checkStepOff(ent.getX(), ent.getY());
 
                 ent.setX(newP.x);
                 ent.setY(newP.y);
                 return true;
             } else
                 return false;
         } else
             return false;
     }
 
     /**
      * @param e
      * @param newP
      * @return
      */
     private boolean doBrokenMove(Effects e, IntPair newP) {
         setTile(newP.x, newP.y, T_FLOOR);
         if (e != null) {
             e.doBroken();
         }
         return true;
     }
 
     /**
      * @param e
      * @param newP
      * @return
      */
     private boolean doButtonMove(Effects e, IntPair newP) {
         for (int dd = Entity.FIRST_DIR; dd <= Entity.LAST_DIR; dd++) {
             /* send a pulse in that direction. */
             IntPair pulse = new IntPair(newP);
             int pd = dd;
 
             while (pd != Entity.DIR_NONE && travel(pulse.x, pulse.y, pd, pulse)) {
                 switch (tileAt(pulse.x, pulse.y)) {
                 case T_BLIGHT:
                     swapTiles(T_BUP, T_BDOWN);
                     pd = Entity.DIR_NONE;
                     break;
                 case T_RLIGHT:
                     swapTiles(T_RUP, T_RDOWN);
                     pd = Entity.DIR_NONE;
                     break;
                 case T_GLIGHT:
                     swapTiles(T_GUP, T_GDOWN);
                     pd = Entity.DIR_NONE;
                     break;
 
                 case T_NS:
                     if (pd == Entity.DIR_UP || pd == Entity.DIR_DOWN)
                         continue;
                     else
                         pd = Entity.DIR_NONE;
                     break;
 
                 case T_WE:
                     if (pd == Entity.DIR_LEFT || pd == Entity.DIR_RIGHT)
                         continue;
                     else
                         pd = Entity.DIR_NONE;
                     break;
 
                 case T_NW:
                     if (pd == Entity.DIR_DOWN)
                         pd = Entity.DIR_LEFT;
                     else if (pd == Entity.DIR_RIGHT)
                         pd = Entity.DIR_UP;
                     else
                         pd = Entity.DIR_NONE;
                     break;
 
                 case T_SW:
                     if (pd == Entity.DIR_UP)
                         pd = Entity.DIR_LEFT;
                     else if (pd == Entity.DIR_RIGHT)
                         pd = Entity.DIR_DOWN;
                     else
                         pd = Entity.DIR_NONE;
                     break;
 
                 case T_NE:
                     if (pd == Entity.DIR_DOWN)
                         pd = Entity.DIR_RIGHT;
                     else if (pd == Entity.DIR_LEFT)
                         pd = Entity.DIR_UP;
                     else
                         pd = Entity.DIR_NONE;
                     break;
 
                 case T_SE:
                     if (pd == Entity.DIR_UP)
                         pd = Entity.DIR_RIGHT;
                     else if (pd == Entity.DIR_LEFT)
                         pd = Entity.DIR_DOWN;
                     else
                         pd = Entity.DIR_NONE;
                     break;
 
                 default:
                     pd = Entity.DIR_NONE;
                 }
             }
         }
 
         if (e != null) {
             e.doPulse();
         }
         return true;
     }
 
     /**
      * @param ent
      * @param e
      * @param newP
      * @return
      */
     private boolean doTransportMove(Entity ent, Effects e, IntPair newP) {
         // not if there's an entity there
         if (player.isAt(newP.x, newP.y) || isBotAt(newP.x, newP.y)) {
             return false;
         }
 
         if (ent.canTeleport() || ent.isPlayer()) {
             IntPair targ;
             targ = where(dests[width * newP.y + newP.x]);
 
             if (e != null) {
                 e.doTransport();
             }
             warp(ent, targ.x, targ.y);
 
             checkBotDeath(targ.x, targ.y, ent);
 
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * @param e
      * @param target
      * @param newP
      * @return
      */
     private boolean doToggleMove(Effects e, int target, IntPair newP) {
         {
             int opp = (target == T_0 ? T_1 : T_0);
 
             swapTiles(T_UD, T_LR);
 
             if (e != null) {
                 e.doSwap();
             }
             setTile(newP.x, newP.y, opp);
 
             return true;
         }
     }
 
     /**
      * @param e
      * @param newP
      * @return
      */
     private boolean doElectricOffMove(Effects e, IntPair newP) {
         if (e != null) {
             e.doElectricOff();
         }
         for (int i = (width * height) - 1; i >= 0; i--) {
             if (tiles[i] == T_ELECTRIC)
                 setTile(i, T_FLOOR);
         }
         setTile(newP.x, newP.y, T_OFF);
         return true;
     }
 
     /**
      * @param ent
      * @param newP
      * @return
      */
     private boolean doExitMove(Entity ent, IntPair newP) {
         // bots don't exit
         if (player.isAt(newP.x, newP.y) || isBotAt(newP.x, newP.y)) {
             return false;
         }
 
         checkStepOff(ent.getX(), ent.getY());
         ent.setX(newP.x);
         ent.setY(newP.y);
         return true;
     }
 
     /**
      * @param ent
      * @param d
      * @param target
      * @param newP
      * @return
      */
     private boolean doFloorMove(Entity ent, int d, int target, IntPair newP) {
         // sometimes we will push
         Entity pushee = null;
         Bot b;
         if (player.isAt(newP.x, newP.y)) { // ent is not player!
             // if player is on bot, no pushing either of them
             if (getBotAt(newP.x, newP.y) != null) {
                 return false;
             }
 
             if (ent.canPushPlayer()) {
                 pushee = player;
             } else if (ent.crushesPlayer()) {
                 pushee = null; // CRUSH! not push
             } else {
                 return false;
             }
         } else if ((b = getBotAt(newP.x, newP.y)) != null) {
             if (ent.canPushBots()) {
                 pushee = b;
             } else if (ent.walksIntoBots()) {
                 pushee = null; // WALK! not push
             } else {
                 return false;
             }
         }
 
         if (pushee != null) {
             // we are pushing, do some sort of recursive push
             IntPair far = new IntPair();
             if (travel(newP.x, newP.y, d, far)) {
                 int fTarget = tileAt(far.x, far.y);
                 switch (fTarget) {
                 case T_ELECTRIC:
                     // only bots can go into electric
                     if (pushee == player) {
                         return false;
                     }
                     break;
                 case T_TRAP2:
                 case T_TRAP1:
                 case T_FLOOR:
                 case T_ROUGH:
                 case T_RDOWN:
                 case T_GDOWN:
                 case T_BDOWN:
                 case T_PANEL:
                 case T_RPANEL:
                 case T_GPANEL:
                 case T_BPANEL:
                     break;
                 default:
                     return false;
                 }
 
                 // can't push 2 entities
                 if (isBotAt(far.x, far.y)) {
                     return false;
                 }
                 if (player.isAt(far.x, far.y)) {
                     return false;
                 }
 
                 // push
                 pushee.setX(far.x);
                 pushee.setY(far.y);
 
                 // zapping
                 if (fTarget == T_ELECTRIC && pushee != player) {
                     ((Bot) pushee).delete();
                 }
 
                 // panels
                 if (fTarget == T_PANEL) {
                     swapO(destAt(far.x, far.y));
                 }
 
                 // handle leaving current (pushed) position
                 if (target == T_PANEL) {
                     // do nothing, or else get a double flip
                     // since pusher is going on here now
                 } else {
                     checkStepOff(newP.x, newP.y);
                 }
 
                 // handle leaving pusher position
                 checkStepOff(ent.getX(), ent.getY());
 
                 // then move
                 ent.setX(newP.x);
                 ent.setY(newP.y);
 
                 // done?
                 return true;
             } else {
                 return false;
             }
         } else {
             checkBotDeath(newP.x, newP.y, ent); // might have stepped
             // onto bot
 
             // panels again
             checkStepOff(ent.getX(), ent.getY());
             if (target == T_PANEL) {
                 swapO(destAt(newP.x, newP.y));
             }
 
             ent.setX(newP.x);
             ent.setY(newP.y);
 
             return true;
         }
     }
 
     private void setFlag(int x, int y, int f) {
         flags[y * width + x] = f;
     }
 
     static private boolean triggers(int tile, int panel) {
         /* "anything" triggers grey panels */
         if (panel == T_PANEL)
             return true;
         if (panel == T_RPANEL) {
             return tile == T_RSPHERE || tile == T_RSTEEL;
         }
         if (panel == T_GPANEL) {
             return tile == T_GSPHERE || tile == T_GSTEEL;
         }
         if (panel == T_BPANEL) {
             return tile == T_BSPHERE || tile == T_BSTEEL;
         }
         /* ? */
         return false;
     }
 
     private void checkBotDeath(int x, int y, Entity ent) {
         if (ent != player) {
             for (int b = 0; b < bots.length; b++) {
                 Bot bb = bots[b];
                 if (ent != bb && bb.getBotType() != Entity.B_DELETED
                         && x == bb.getX() && y == bb.getY()) {
                     bots[b].delete();
                     ((Bot) ent).setToType(Entity.B_BROKEN);
                 }
             }
         }
     }
 
     private boolean isBotAt(int x, int y) {
         return getBotAt(x, y) != null;
     }
 
     private Bot getBotAt(int x, int y) {
         for (int i = 0; i < bots.length; i++) {
             if (bots[i].getBotType() != Entity.B_DELETED && bots[i].isAt(x, y)) {
                 return bots[i];
             }
         }
         return null;
     }
 
     public Level(Level l) {
         width = l.width;
         height = l.height;
 
         author = l.author;
         title = l.title;
 
         player = new Player(l.player.getX(), l.player.getY(), l.player.getDir());
 
         tiles = new int[l.tiles.length];
         oTiles = new int[l.oTiles.length];
         dests = new int[l.dests.length];
         flags = new int[l.flags.length];
 
         System.arraycopy(l.tiles, 0, tiles, 0, tiles.length);
         System.arraycopy(l.oTiles, 0, oTiles, 0, oTiles.length);
         System.arraycopy(l.dests, 0, dests, 0, dests.length);
         System.arraycopy(l.flags, 0, flags, 0, flags.length);
 
         bots = new Bot[l.bots.length];
         for (int i = 0; i < l.bots.length; i++) {
             Bot b = l.bots[i];
             bots[i] = new Bot(b.getX(), b.getY(), b.getDir(), b.getBotType());
         }
 
         dirty = new DirtyList();
 
         isDead();
     }
 
     public Level(BitInputStream in) throws IOException {
         MetaData m = getMetaData(in);
 
         width = m.width;
         height = m.height;
 
         author = m.author;
         title = m.title;
 
         int playerX = in.readInt();
         int playerY = in.readInt();
 
         player = new Player(playerX, playerY, Entity.DIR_DOWN);
 
         tiles = RunLengthEncoding.decode(in, width * height);
         oTiles = RunLengthEncoding.decode(in, width * height);
         dests = RunLengthEncoding.decode(in, width * height);
         flags = RunLengthEncoding.decode(in, width * height);
 
         // load bots if in file
         int bots;
         int botI[] = null;
         int botT[] = null;
         try {
             bots = in.readInt();
             botI = RunLengthEncoding.decode(in, bots);
             botT = RunLengthEncoding.decode(in, bots);
         } catch (EOFException e) {
             bots = 0;
         }
 
         this.bots = new Bot[bots];
 
         for (int i = 0; i < this.bots.length; i++) {
             int x = botI[i] % width;
             int y = botI[i] / width;
             this.bots[i] = new Bot(x, y, Entity.DIR_DOWN, botT[i]);
         }
 
         dirty = new DirtyList();
 
         isDead(); // calculate laser cache
     }
 
     public static MetaData getMetaData(BitInputStream in) throws IOException {
         String magic = Misc.getStringFromData(in, 4);
         if (!magic.equals("ESXL")) {
             throw new IOException("Bad magic");
         }
 
         int width = in.readInt();
         int height = in.readInt();
 
         //        System.out.println("width: " + width + ", height: " + height);
 
         int size;
 
         size = in.readInt();
         String title = Misc.getStringFromData(in, size);
 
         size = in.readInt();
         String author = Misc.getStringFromData(in, size);
 
         return new MetaData(width, height, title, author);
     }
 
     public class DirtyList {
         boolean allDirty;
 
         private final boolean dirty[];
 
         private final int dirtyList[];
 
         private int numDirty;
 
         DirtyList() {
             int n = width * height;
             dirty = new boolean[n];
             dirtyList = new int[n];
 
             setAllDirty();
         }
 
         public void clearDirty() {
             for (int i = dirty.length - 1; i >= 0; i--) {
                 dirty[i] = false;
             }
             numDirty = 0;
             allDirty = false;
         }
 
         public void setDirty(int x, int y) {
             setDirty(index(x, y));
         }
 
         public void setDirty(int i) {
             if (dirty[i]) {
                 return;
             }
 
             dirty[i] = true;
             dirtyList[numDirty] = i;
             numDirty++;
         }
 
         public void setAllDirty() {
             allDirty = true;
         }
 
         public boolean isDirty(int i) {
             return allDirty || dirty[i];
         }
 
         public boolean isDirty(int x, int y) {
             return allDirty || dirty[index(x, y)];
         }
 
         public boolean isAnyDirty() {
             return allDirty || numDirty > 0;
         }
     }
 
     public String toString() {
         return "[\"" + title + "\" by " + author + " (" + width + "x" + height
                 + ")" + " player: (" + this.player.getX() + ","
                 + this.player.getY() + ")]";
     }
 
     public void print(PrintStream p) {
         p.println(toString());
         p.println("\"" + title + "\" by " + author + " (" + width + ","
                 + height + ")" + " player: (" + this.player.getX() + ","
                 + this.player.getY() + ")");
         p.println();
         p.println("tiles");
         printM(p, tiles, width);
 
         p.println();
         p.println("oTiles");
         printM(p, oTiles, width);
 
         p.println();
         p.println("dests");
         printM(p, dests, width);
 
         p.println();
         p.println("flags");
         printM(p, flags, width);
     }
 
     static private void printM(PrintStream p, int[] m, int w) {
         int l = 0;
         for (int i = 0; i < m.length; i++) {
             p.print((char) (m[i] + 32));
             l++;
             if (l == w) {
                 p.println();
                 l = 0;
             }
         }
     }
 
     public int getBotDir(int botIndex) {
         return bots[botIndex].getDir();
     }
 
     public int getPlayerDir() {
         return player.getDir();
     }
 
     public boolean isBotDeleted(int botIndex) {
         return bots[botIndex].getBotType() == Entity.B_DELETED;
     }
 
     public IntTriple getLaser() {
         return laser;
     }
 
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
 
         if (obj instanceof Level) {
             Level l = (Level) obj;
 
             // metadata
             if (!author.equals(l.author)) {
                 return false;
             }
             if (!title.equals(l.title)) {
                 return false;
             }
             if (width != l.width) {
                 return false;
             }
             if (height != l.height) {
                 return false;
             }
 
             // tiles
             for (int i = 0; i < tiles.length; i++) {
                 if (tiles[i] != l.tiles[i] || oTiles[i] != l.oTiles[i]
                         || dests[i] != l.dests[i] || flags[i] != l.flags[i]) {
                     return false;
                 }
             }
 
             // entities
             if (!player.equals(l.player)) {
                 return false;
             }
 
             try {
                 for (int i = 0; i < bots.length; i++) {
                     if (!bots[i].equals(l.bots[i])) {
                         return false;
                     }
                 }
             } catch (ArrayIndexOutOfBoundsException e) {
                 return false;
             }
         }
         return true;
     }
 
     public int hashCode() {
         // Like Tom,
         /*
          * ignore title, author, w/h, dests, flags, since these don't change.
          * also ignore botd and guyd, which are presentational.
          */
 
         FNV32 hash = new FNV32();
 
         // player
         hash.fnv32(player.getX());
         hash.fnv32(player.getY());
 
         // tiles, oTiles
         for (int i = 0; i < tiles.length; i++) {
             hash.fnv32(tiles[i]);
             hash.fnv32(oTiles[i]);
         }
 
         // bots
         for (int i = 0; i < bots.length; i++) {
             Bot b = bots[i];
             hash.fnv32(b.getBotType());
             hash.fnv32(b.getX());
             hash.fnv32(b.getY());
         }
 
         return hash.hval;
     }
 }
