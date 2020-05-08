 package com.jpii.navalbattle.renderer;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.image.*;
 import java.util.*;
 
 import com.jpii.dagen.Engine;
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.game.GameComponent;
 
 /**
  * The OmniMap (previously known as MiniZoomMap). Essential is map of the world and binoculars.
  * @author MKirkby
  *
  */
 public class OmniMap {
     Engine eng;
     int width, height;
     Random r;
     BufferedImage buffer, map;
     public boolean entireWorldMode = true;
     Random treeSeeder;
     public static boolean[][] treeLocs;
     public static int[][] grassLocs;
     public int px, py;
     boolean[][] waveLocations;
     double[] pulseLine;
     Random rw;
     /**
      * Constructs a new instance of OmniMap
      * @param eng The current engine that is being used in the game.
      * @param width The width of the OmniMap.
      * @param height The height of the OmniMap.
      */
     public OmniMap(Engine eng, int width, int height) {
         this.eng = eng;
         this.width = width;
         this.height = height;
         r = new Random(Constants.MAIN_SEED);
         treeSeeder = new Random((-Constants.MAIN_SEED)+22);
         buffer = new BufferedImage(width, height + 25, BufferedImage.TYPE_INT_RGB);
         map = new BufferedImage(width-2, height-2, BufferedImage.TYPE_INT_RGB);
         Graphics g = map.getGraphics();
         int s = 3;
         int swa = Constants.WINDOW_WIDTH / width * 10;
         int sha = Constants.WINDOW_HEIGHT / height * 10;
         treeLocs = new boolean[eng.getWidth()+1][eng.getHeight()+1];
         grassLocs = new int[eng.getWidth()+1][eng.getHeight()+1];
         for (int x = 0; x < eng.getWidth(); x++) {
         	for (int y = 0; y < eng.getHeight(); y++) {
         		double z = eng.getPoint(x, y);
         		if (z >= RenderConstants.GEN_MOUNTAIN_HEIGHT - 0.15 && treeSeeder.nextInt(50) == 1) {
         			treeLocs[x][y] = true;
         		}
         		if (y >= RenderConstants.GEN_WATER_HEIGHT - 0.01 && y <= RenderConstants.GEN_WATER_HEIGHT + 0.05 && treeSeeder.nextInt(50) == 1) {
         			treeLocs[x][y] = true;
                 }
         		if (z > RenderConstants.GEN_WATER_HEIGHT - 0.01 && treeSeeder.nextInt(30) == 1) {
         			grassLocs[x][y] = treeSeeder.nextInt(4902)+2;
         		}
         	}
         }
         rw = new Random(Constants.MAIN_SEED + 42);
 		waveLocations = new boolean[width][height];
 		pulseLine = new double[height];
 		for (int y = 0; y < height; y+=rw.nextInt(2)+2) {
 			for (int x = 0; x < width; x++) {
 					waveLocations[x][y] = true;
 			}
 		}
         for (int xt = 0; xt < eng.getWidth(); xt += swa) {
             for (int zt = 0; zt < eng.getHeight(); zt += sha) {
                 double y = eng.getPoint(xt, zt);
                 int x = xt / swa;
                 int z = zt / sha;
                 boolean flag = y <= RenderConstants.GEN_WATER_HEIGHT;
                 if (flag) {
                     Color waterSample = //Constants.randomise(Constants.GEN_WATER_COLOR, Constants.GEN_COLOR_DIFF,
                     //r,false);
                     Helper.adjust(Helper.randomise(RenderConstants.GEN_WATER_COLOR,
                     RenderConstants.GEN_COLOR_DIFF, r, false), 1 - (y / RenderConstants.GEN_WATER_HEIGHT), 50);
                     if (y >= RenderConstants.GEN_WATER_HEIGHT - 0.05) {
                         double t = RenderConstants.GEN_WATER_HEIGHT - y;
                         waterSample = Helper.Lerp(RenderConstants.GEN_SAND_COLOR, RenderConstants.GEN_SAND_COLOR2 /*waterSample*/ , t / 0.05);
                     }
                     g.setColor(waterSample);
                     g.fillRect(x * s, z * s, s + 1, s + 1);
                 }
                 if (y >= RenderConstants.GEN_WATER_HEIGHT - 0.01 && y <= RenderConstants.GEN_WATER_HEIGHT + 0.05 && r.nextInt(3) == 1) {
                     flag = false;
                 }
                 if (!flag) {
                     Color groundSample = Helper.adjust(Helper.randomise(RenderConstants.GEN_GRASS_COLOR,
                     RenderConstants.GEN_COLOR_DIFF, r, false), y, 50);
 
                     if (y <= RenderConstants.GEN_WATER_HEIGHT + 0.1) {
                         double t = y - RenderConstants.GEN_WATER_HEIGHT;
                         groundSample = Helper.Lerp(RenderConstants.GEN_SAND_COLOR, groundSample, t / 0.1);
                     }
                     if (y >= RenderConstants.GEN_MOUNTAIN_HEIGHT) {
                         double t = y - RenderConstants.GEN_MOUNTAIN_HEIGHT;
                         groundSample = Helper.Lerp(groundSample, RenderConstants.GEN_MOUNTAIN_COLOR,
                         t / (1.0 - RenderConstants.GEN_MOUNTAIN_HEIGHT));
                         groundSample = Helper.adjust(groundSample, t / (1.0 - RenderConstants.GEN_MOUNTAIN_HEIGHT), 30);
                     }
 
                     g.setColor(groundSample);
                     g.fillRect(x * s, z * s, s + 1, s + 1);
                 }
             }
         }
     }
     /**
      * Returns the created buffer for the OmniMap.
      * @return
      */
     public BufferedImage getBuffer() {
         return buffer;
     }
     int mx = 0;
     int my = 0;
     /**
      * The Max variables.
      */
     public int msax, msay;
     /**
      * Is called to update the mouse coordinates.
      * @param me The MouseEvent.
      */
     public void mouse(MouseEvent me) {
         mx = me.getX();
         my = me.getY();
     }
     /**
      * Fired by the mouseClicked event in the game. Checks to see if the mode is toggled.
      * @param me
      */
     public void mouseClick(MouseEvent me) {
         if (me.getX() > px && me.getY() > py && me.getX() < px + width && me.getY() < py + height + 25) {
             entireWorldMode = !entireWorldMode;
         }
     }
     /**
      * Updates the player location in the map, and the location that the mouse is at.
      */
     public void update() {
         buffer = new BufferedImage(width, height + 25, BufferedImage.TYPE_INT_RGB);
         Graphics2D g2 = (Graphics2D) buffer.getGraphics();
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2.drawImage(Helper.GUI_OMNIMAP_BACKGROUND1,0,0,100,125,null);
         if (entireWorldMode) {
             g2.drawImage(map, 1,1, null);
             int vx = (msax - 200) * 100 / (Constants.WINDOW_WIDTH * 4);
             int vy = (msay - 200) * 100 / (Constants.WINDOW_HEIGHT * 4);
             vx += 20;
             vy += 20;
             g2.setColor(Color.red);
             g2.drawRect(vx, vy, Constants.WINDOW_WIDTH / 100 * 2, Constants.WINDOW_HEIGHT / 100 * 2);
         } else {
         	BufferedImage bb3 = new BufferedImage(width-4,height-4,BufferedImage.TYPE_INT_ARGB);
         	Graphics2D g = (Graphics2D) bb3.getGraphics();
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         	r = new Random(Constants.MAIN_SEED);
         	//Point p = GameComponent.game.screenToPoint(mx,my);
         	Point p = GameComponent.game.mouseToPoint();
         	//p.x += 250 + 50;
         	//p.y += 200 + 50;
         	for (int x22 = -20; x22 < 20; x22++) {
                 for (int z22 = -20; z22 < 20; z22++) {
                     int ttx = (x22 + p.x);
                     int tty = (z22 + p.y);
                     int x = x22 + 20;
                     int z = z22 + 20;
                     //if (x * s > )
                     int s = 3;
                     if (ttx < 0 || tty < 0 || ttx > (Constants.WINDOW_WIDTH * 4) || tty > (Constants.WINDOW_HEIGHT * 4)) {
                         int rgb = r.nextInt(100);
                         g.setColor(new Color(rgb, rgb, rgb));
                         g.fillRect(x * s, z * s, s + 1, s + 1);
                     } else {
                         double y = eng.getPoint(ttx, tty);
                         boolean flag = y <= RenderConstants.GEN_WATER_HEIGHT;
                         if (flag) {
                             Color waterSample = RenderConstants.GEN_WATER_COLOR; //Constants.randomise(Constants.GEN_WATER_COLOR, Constants.GEN_COLOR_DIFF,
                             //r,false);
                            waterSample = Helper.adjust(Helper.randomise(RenderConstants.GEN_WATER_COLOR,
                             RenderConstants.GEN_COLOR_DIFF, r, false), 1 - (y / RenderConstants.GEN_WATER_HEIGHT), 50);
                             if (y >= RenderConstants.GEN_WATER_HEIGHT - 0.05) {
                                 double t = RenderConstants.GEN_WATER_HEIGHT - y;
                                 waterSample = Helper.Lerp(RenderConstants.GEN_SAND_COLOR, RenderConstants.GEN_SAND_COLOR2 /*waterSample*/ , t / 0.05);
                                 waterSample = Helper.randomise(waterSample, RenderConstants.GEN_COLOR_DIFF, r, false);
                             }
                             g.setColor(waterSample);
                             //g.fillRect(x * s, z * s, s + 1, s + 1);
                         }
                         if (y >= RenderConstants.GEN_WATER_HEIGHT - 0.01 && y <= RenderConstants.GEN_WATER_HEIGHT + 0.05 && r.nextInt(3) == 1) {
                             flag = false;
                         }
 
                         if (!flag) {
                             Color groundSample = Helper.adjust(Helper.randomise(RenderConstants.GEN_GRASS_COLOR,
                             RenderConstants.GEN_COLOR_DIFF, r, false), y, 50);
 
                             if (y <= RenderConstants.GEN_WATER_HEIGHT + 0.1) {
                                 double t = y - RenderConstants.GEN_WATER_HEIGHT;
                                 groundSample = Helper.Lerp(RenderConstants.GEN_SAND_COLOR, groundSample, t / 0.1);
                                 groundSample = Helper.randomise(groundSample, RenderConstants.GEN_COLOR_DIFF, r, false);
                             }
                             if (y >= RenderConstants.GEN_MOUNTAIN_HEIGHT) {
                                 double t = y - RenderConstants.GEN_MOUNTAIN_HEIGHT;
                                 groundSample = Helper.Lerp(groundSample, RenderConstants.GEN_MOUNTAIN_COLOR,
                                 t / (1.0 - RenderConstants.GEN_MOUNTAIN_HEIGHT));
                                 if (RenderConstants.OPT_RENDERING_QUALITY == RenderingQuality.FullSpeedAhead) groundSample = Helper.randomise(groundSample, RenderConstants.GEN_COLOR_DIFF, r, false);
                                 groundSample = Helper.adjust(groundSample, t / (1.0 - RenderConstants.GEN_MOUNTAIN_HEIGHT), 30);
                             }
 
                             g.setColor(groundSample);
                         }
                         g.fillRect(x * s, z * s, s + 1, s + 1);
                     }
                 }
         	}
         	for (int x22 = -20; x22 < 20; x22++) {
                 for (int z22 = -20; z22 < 20; z22++) {
                     int ttx = (x22 + p.x);
                     int tty = (z22 + p.y);
                     int x = x22 + 20;
                     int z = z22 + 20;
                     if (treeLocs != null) {
                     	
                     	if (ttx > 0 && tty > 0 && ttx < (Constants.WINDOW_WIDTH * 4) &&
                     			tty < (Constants.WINDOW_HEIGHT * 4) && treeLocs[ttx][tty]) {
                     		g.setColor(Color.orange);
                     		g.fillRect(x*3,z*3, 3,5);
                     	}
                     	
                     	if (ttx > 0 && tty > 0 && ttx < (Constants.WINDOW_WIDTH * 4) &&
                     			tty < (Constants.WINDOW_HEIGHT * 4) && grassLocs[ttx][tty] != 0) {
                     		Random t = new Random(grassLocs[ttx][tty]);
                     		for (int v = 0; v < t.nextInt(4)+3; v++) {
 	                    		g.setColor(Color.green.darker().darker());
 	                    		int x1 = t.nextInt(8) - 4;
 	                    		int y1 = t.nextInt(8) - 4;
 	                    		int length = t.nextInt(4) + 4;
 	                    		int diff1 = t.nextInt(2) - 1;
 	                    		int diff2 = t.nextInt(2) - 1;
 	                    		int diff3 = t.nextInt(2) - 1;
 	                    		g.drawLine((x*3)+x1+diff3,(z*3)+diff1+y1, (x*3)+diff2+x1,(z*3)+length+y1);
                     		}
                     	}
                     }
                 }
             }
         	boolean[][] visible = new boolean[width][height];
         	for (int x22 = -20; x22 < 20; x22++) {
                 for (int z22 = -20; z22 < 20; z22++) {
                     int ttx = (x22 + p.x);
                     int tty = (z22 + p.y);
                     int x = x22 + 20;
                     int z = z22 + 20;
                     if (eng.getPoint(ttx,tty) < RenderConstants.GEN_WATER_HEIGHT)
                     	visible[x][z] = true;
                 }
         	}
         	for (int x = 0; x < width; x++) {
     			pulseLine[x] += rw.nextInt(2) + 1;//rdouble(2.5,3.0);
     			for (int y = 0; y < height; y++) {
     				if (waveLocations[x][y] && visible[x][y]) {
    					//double cdfloat = Math.sin(x + pulseLine[x]);
     					//g.setColor(new Color(buffer.getRGB(x*3, y*3)).brighter());
     					//g.setColor(Color.blue);
     					//g.fillRect(x*3, (int)((y *3) + cdfloat), 3,3);
     				}
     			}
     		}
     		
         	g2.drawImage(bb3,2,2,null);
         }
         Graphics g = g2;
         g2.setColor(new Color(100, 78, 47));
         g2.drawRect(1, 1, width - 3, height - 3);
         //g.setColor(new Color(74, 30, 3));
         ///g.drawRect(0, 0, width - 1, height - 1);
         //g.fillRect(0, height, width, 25);
 
         g2.setColor(new Color(100, 78, 47));
         if (entireWorldMode) {
             g2.fillRect(1, height - 1, 49, 25);
             g2.setColor(new Color(175, 137, 86)); //215,197,172));
             g2.drawLine(1, height - 1, 1, height + 23);
             g2.drawLine(2, height - 1, 2, height + 23);
             g2.setColor(new Color(36, 28, 17)); //57,45,28));
             g.drawLine(1, height + 23, 49, height + 23);
             g.drawLine(2, height + 22, 49, height + 22);
             g.drawLine(50, height - 1, 50, height + 23);
             g.drawLine(49, height - 1, 49, height + 23);
         } else {
             g.fillRect(50, height - 1, 49, 25);
             g.setColor(new Color(175, 137, 86));
             g.drawLine(49, height - 1, 49, height + 23);
             g.drawLine(50, height - 1, 50, height + 23);
             g.setColor(new Color(36, 28, 17)); //57,45,28));
             g.drawLine(49, height + 23, 49 + 48, height + 23);
             g.drawLine(50, height + 22, 49 + 48, height + 22);
             g.drawLine(50 + 48, height - 1, 50 + 48, height + 23);
             g.drawLine(49 + 48, height - 1, 49 + 48, height + 23);
         }
         
         g.drawImage(Helper.GUI_OMNIMAP_ICON_WORLD, 17, height+1, null);
 
         g.setColor(new Color(122, 49, 5));
         g.fillOval(49 + 21, height, 10, 10);
         g.setColor(Color.black);
         g.drawOval(49 + 21, height, 10, 10);
         g.drawLine(49 + 23, height + 7, 49 + 15, height + 15);
     }
 }
