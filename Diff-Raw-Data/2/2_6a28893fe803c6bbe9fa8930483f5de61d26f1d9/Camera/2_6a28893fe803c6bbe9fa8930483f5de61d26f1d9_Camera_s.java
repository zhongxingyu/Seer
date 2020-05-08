 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package raycaster;
 
 import Utilities.Image2D;
 import Utilities.ImageCollection;
 import Utilities.Rect;
 import Utilities.Vector2;
 import java.awt.Color;
 import java.awt.Rectangle;
 import level.Level;
 import objects.Player;
 
 /**
  *
  * @author pcowal15
  */
 public class Camera {
     
     public static Camera instance;
     //OH GOD THAT'S A LOT OF VARIABLES
 
     double fov;//the angle of the field of view.  Probably should be something like pi/3 or pi/4
     int rayCount;//The number of rays, I think
     double[] rays;//The array containing the distances calculated, probably
     int[] rayTexs;//The array containing the detected texture integers established in the Level class
     Image2D[] rayImages;//Not used, I think
     double screenWidth;
     double screenHeight;
     double xStep;//The change in x position ON THE SCREEN
     double screenX;//The current x position of the ray ON THE SCREEN
     double cellSize = 64;
     double x;//The current x position of the ray IN THE LEVEL
     double dx;//When I'm casting rays, this is the change in x over one iteration IN THE LEVEL
     //i.e. x+=dx, check cell, x+=dx, check cell, ad infinitum (until wall)
     double y;//See above
     double dy;//See above
     double zoffset = 0;//The offset of the player along the z-axis
     //Note- z is relative to the level, so it actually corresponds to a change in y relative to the screen
     double zangle = 0;//The degree above or below the horizon line
     double angle;//The angle the camera is facing
     double angleOffset;//The angular offset 
     double angleStep;//The change in angle after a ray is finished
     int[][] level;//The array containing all of the texture integers
     Image2D[] brick;//The image array containing all of the brick images
     Image2D[] brick2;//See above (this one's slightly darker)
     Image2D[] stone;//See above
     Image2D[] metal;//See above
     Image2D[] elderfloors;
     double sf;//Scale factor: used for scaling the images when they're being drawn
     boolean floor;//Whether or not I'm drawing the (somewhat glitchy) floor
     double clip;//The maximum distance a ray goes before petering out
     Rect rect;//Just a rectangle, used when determining the part of the image being drawn
     boolean textures;//Whether or not I'm drawing textures
     boolean sprites = true;//Whether or not I'm drawing sprites
     boolean elderscrolls = false;
     double floorindex;
 
     public Camera(double FOV, int RayCount, double Width, double Height) {
         instance=this;
         textures = true;
         fov = FOV;
         rayCount = RayCount;
         angleOffset = -fov / 2;//(It goes from -fov/2 to +fov/2, see)
         angleStep = fov / rayCount;//So when (n) rays are cast, the angle smoothly pans from -fov/2 to fov/2
         rays = new double[rayCount];
         rayTexs = new int[rayCount];
         brick = new Image2D[rayCount];
         brick2 = new Image2D[rayCount];
         stone = new Image2D[rayCount];
         metal = new Image2D[rayCount];
         elderfloors = new Image2D[]{new Image2D("src/Resources/lol elder scrolls 1.png"),
             new Image2D("src/Resources/lol elder scrolls 2.png"),
             new Image2D("src/Resources/lol elder scrolls 3.png")};
         floorindex = 0;
         for (int i = 0; i < rayCount; i++) {
             rays[i] = -1;
             rayTexs[i] = -1;
             if (textures) {
                 //This is a lot of images.  4*rayCount, to be exact.  So that's like 1280
                brick[i] = new Image2D("src/Resources/brick.png");
                 brick2[i] = new Image2D("src/Resources/brick2.png");
                 stone[i] = new Image2D("src/Resources/metal.png");
                 metal[i] = new Image2D("src/Resources/metal_dark.png");
             }
         }
         rayImages = new Image2D[rayCount];//I think this was before I used multiple textures
         screenWidth = Width;
         screenHeight = Height;
         xStep = screenWidth / rayCount;
         sf = screenWidth * cellSize * 0.5 / Math.tan(fov / 2);//Maths.  I did the trigonometry, and it turned out okay.
         floor = !true;//Stupid floor //BUT REALLY COOL, IF IT WORKED(kyle)
         clip = 3000;
     }
 
     public void setLevel(int[][] Level) {
         level = Level;
     }
 
     public void setFOV(double FOV) {
         fov = FOV;
         sf = screenWidth * cellSize * 0.5 / Math.tan(fov / 2);//the scalefactor calculation again
         angleStep = fov / rayCount;//updates this as well
     }
 
     public void screwFloor(double amount) {
         floorindex += amount;
         while (floorindex < 0) {
             floorindex += 3;
         }
         floorindex %= 3;
     }
 
     public void castRays(ImageCollection batch, double X, double Y, double Angle) {
         //This is an ungodly mess.  I still can't believe it worked/works.
         angle = Angle;
         angleOffset = -fov / 2;
         screenX = 0;
         //Trigonometry for optimization purposes
         double cos;
         double sin;
         double tan;
         double cot;
         //The length of the current ray
         double dist;
         //Since, in my algorithm, I search for horizontal and vertical walls separately, 
         //I store the closer value here.
         double mindist;
         //The height of the current ray on the screen
         double rayHeight;
 
         double theta;
         boolean done;//Whether or not a wall is detected
         Rect rect;//Wait, I'm not sure why this is here
         //Draws a nice little border
         batch.fillRect(new Vector2(0, screenHeight / 2), 2000, 2000, Color.GRAY, 1);
         batch.fillRect(new Vector2(0, 0), 2000, (int) screenHeight / 2, Color.LIGHT_GRAY, 1);
         batch.fillRect(new Vector2(screenWidth, 0), 2000, 2000, Color.BLACK, 100000);
         batch.fillRect(new Vector2(0, screenHeight), 2000, 2000, Color.BLACK, 100000);
         //LOL ELDER SCROLLS
         if (elderscrolls) {
             batch.Draw(elderfloors[(int) Math.floor(floorindex)], new Vector2(screenWidth / 2, screenHeight), 0, (int) screenWidth / 128, (int) screenHeight / 64, 2);
         }
         /*
         //trying to get the Raycasting code to be split into two... oh well
         int half=rayCount/2;
         CameraHelper A=new CameraHelper(0, half,  X,  Y, textures,  sf,  batch, angleOffset,  angleStep,  screenX,  xStep,screenHeight,angle);
         CameraHelper B=new CameraHelper(half-1, rayCount,  X,  Y, textures,  sf,  batch, angleOffset,  angleStep,  screenX,  xStep,screenHeight,angle);
         
         Thread tA=new Thread(A);
         Thread tB=new Thread(B);
         synchronized(this){
             tA.start();
             tB.start();
         }
         */
         
         //This is a raycasting algorithm.  Email me if you have any questions, I don't feel like commenting it all...
         for (int i = 0; i < rayCount; i++) {
             mindist = 10000;
             theta = (angle - angleOffset);
             if (theta > Math.PI * 2) {
                 theta -= Math.PI * 2;
             }
             if (theta < 0) {
                 theta += Math.PI * 2;
             }
             if (theta == Math.PI * 0.5 || theta == Math.PI * 1 / 5) {
                 tan = 1000000;
                 cot = 0;
             } else if (theta == 0 || theta == Math.PI) {
                 tan = 0;
                 cot = 1000000;
             } else {
                 tan = Math.tan(theta);
                 cot = 1 / tan;
             }
 
             x = X;
             y = Y;
             if (theta > Math.PI) {
                 dy = cellSize * Math.ceil(y / cellSize) - y;
             } else {
                 dy = cellSize * Math.floor(y / cellSize) - y;
             }
             dx = -cot * dy;
             x += dx;
             y += dy;
             //dist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
             if ((cell(x, y + dy / 2) > 0)) {
                 mindist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y)) + 1;
             } else {
                 done = false;
                 if (dy > 0) {
                     dy = cellSize;
                 } else {
                     dy = -cellSize;
                 }
                 dx = -cot * dy;
                 while (!done) {
                     x += dx;
                     y += dy;
                     dist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
                     if ((cell(x, y + dy / 2) > 0) && !done) {
                         mindist = dist;
                         done = true;
                     }
                 }
             }
 
             x = X;
             y = Y;
 
 
             //check the nearest vertical wall
             if (theta < Math.PI * 0.5 || theta > Math.PI * 1.5) {
                 dx = cellSize * Math.ceil(x / cellSize) - x;
             } else {
                 dx = cellSize * Math.floor(x / cellSize) - x;
             }
             dy = -tan * dx;
             x += dx;
             y += dy;
             dist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
 
             if (cell(x + dx / 2, y) > 0 && dist <= mindist) {
                 //rayTexs[i]=cell(x+dx/2,y);
                 rayHeight = (sf / (dist * Math.cos(angleOffset)));
                 if (!textures) {
                     rect = new Rect(new Vector2(screenX - xStep / 2, screenHeight / 2 - rayHeight / 2), (int) xStep, (int) rayHeight);
                     fillRect(batch, rect, getCol(cell(x + dx / 2, y), dist, false), (int) rayHeight + 1000);
                 } else {
                     drawTex(batch, i, cell(x + dx / 2, y), screenX, screenHeight / 2, x + y, rayHeight, false);
                 }
                 mindist = dist;
             } else {
                 done = false;
                 if (dx > 0) {
                     dx = cellSize;
                 } else {
                     dx = -cellSize;
                 }
                 dy = -tan * dx;
                 while (!done) {
                     x += dx;
                     y += dy;
                     dist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
                     if (dist > mindist) {
                         done = true;
                     }
                     if (cell(x + dx / 2, y) > 0 && !done) {
                         rayHeight = (sf / (dist * Math.cos(angleOffset)));
                         if (!textures) {
                             rect = new Rect(new Vector2(screenX - xStep / 2, screenHeight / 2 - rayHeight / 2), (int) xStep, (int) rayHeight);
                             fillRect(batch, rect, getCol(cell(x + dx / 2, y), dist, false), (int) rayHeight + 1000);
                         } else {
                             drawTex(batch, i, cell(x + dx / 2, y), screenX, screenHeight / 2, x + y, rayHeight, false);
                         }
                         mindist = dist;
                         done = true;
                     } else if (!done && dist < 2000) {
                         rayHeight = (sf / (dist * Math.cos(angleOffset)));
                         rect = new Rect(new Vector2(screenX - xStep / 2, screenHeight / 2), (int) xStep, (int) rayHeight / 2);
                         if (floor) {
                             fillRect(batch, rect, getCol(cell(x + dx / 2, y), dist, true), (int) dist / 2);
                         }
                     }
                 }
             }
             //checks the nearest horizontal wall
             x = X;
             y = Y;
             if (theta > Math.PI) {
                 dy = cellSize * Math.ceil(y / cellSize) - y;
             } else {
                 dy = cellSize * Math.floor(y / cellSize) - y;
             }
             dx = -cot * dy;
             x += dx;
             y += dy;
             dist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
             if ((cell(x, y + dy / 2) > 0)) {
                 //dist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
                 if (dist < mindist) {
 
                     rayHeight = (sf / (dist * Math.cos(angleOffset)));
                     if (!textures) {
                         rect = new Rect(new Vector2(screenX - xStep / 2, screenHeight / 2 - rayHeight / 2), (int) xStep, (int) rayHeight);
                         fillRect(batch, rect, getCol(cell(x, y + dy / 2), dist, true), (int) rayHeight + 1000);
                     } else {
                         drawTex(batch, i, cell(x, y + dy / 2), screenX, screenHeight / 2, x + y, rayHeight, true);
                     }
                 }
             } else {
                 done = false;
                 if (dy > 0) {
                     dy = cellSize;
                 } else {
                     dy = -cellSize;
                 }
                 dx = -cot * dy;
                 while (!done) {
                     x += dx;
                     y += dy;
                     dist = Math.sqrt((x - X) * (x - X) + (y - Y) * (y - Y));
                     if (dist > mindist) {
                         done = true;
                     }
                     if ((cell(x, y + dy / 2) > 0) && !done) {
                         rayHeight = (sf / (dist * Math.cos(angleOffset)));
                         if (!textures) {
                             rect = new Rect(new Vector2(screenX - xStep / 2, screenHeight / 2 - rayHeight / 2), (int) xStep, (int) rayHeight);
                             fillRect(batch, rect, getCol(cell(x, y + dy / 2), dist, true), (int) rayHeight + 1000);
                         } else {
                             drawTex(batch, i, cell(x, y + dy / 2), screenX, screenHeight / 2, x + y, rayHeight, true);
                         }
                         done = true;
                     } else if (!done && dist < 2000) {
                         rayHeight = (sf / (dist * Math.cos(angleOffset)));
                         rect = new Rect(new Vector2(screenX - xStep / 2, screenHeight / 2), (int) xStep, (int) rayHeight / 2);
                         if (floor) {
                             fillRect(batch, rect, getCol(cell(x, y + dy / 2), dist, true), (int) dist / 2);
                         }
                     }
                 }
             }
             angleOffset += angleStep;
             screenX += xStep;
 
         }
 
     }
 
     public int cell(double X, double Y) {
         int cellX = (int) Math.floor(X / cellSize);
         int cellY = (int) Math.floor(Y / cellSize);
         try {
             return level[cellX][cellY];
         } catch (Exception e) {
             return 1;
         }
     }
 
     public void drawTex(ImageCollection batch, int i, int tex, double x, double y, double offset, double depth, boolean horizontal) {
         Rect part = new Rect(new Vector2(offset % cellSize, 0), 1, 64);
         y += zoffset * depth + zangle;
         switch (tex) {
             case 1:
                 if (horizontal) {
                     batch.Draw(brick[i], new Vector2(x + xStep * cellSize * 0.5, y), 0, (float) xStep, (float) (depth / cellSize), part, (int) depth + 1000);
                 } else {
                     batch.Draw(brick2[i], new Vector2(x + xStep * cellSize * 0.5, y), 0, (float) xStep, (float) (depth / cellSize), part, (int) depth + 1000);
                 }
                 break;
             case 2:
                 batch.Draw(stone[i], new Vector2(x + xStep * cellSize * 0.5, y), 0, (float) xStep, (float) (depth / cellSize), part, (int) depth + 1000);
                 break;
             case 4:
                 batch.Draw(metal[i], new Vector2(x + xStep * cellSize * 0.5, y), 0, (float) xStep, (float) (depth / cellSize), part, (int) depth + 1000);
                 break;
             default:
                 if (horizontal) {
                     batch.Draw(brick[i], new Vector2(x + xStep * cellSize * 0.5, y), 0, (float) xStep, (float) (depth / cellSize), part, (int) depth + 1000);
                 } else {
                     batch.Draw(brick2[i], new Vector2(x + xStep * cellSize * 0.5, y), 0, (float) xStep, (float) (depth / cellSize), part, (int) depth + 1000);
                 }
                 break;
         }
 
     }
     //just another alternate method of drawing a rectangle
     public void fillRect(ImageCollection batch, Rect rect, Color color, int depth) {
         batch.fillRect(rect.UpperLeftCorner(), rect.width, rect.height, color, depth);
     }
     
     //gets the color of the cell (if I'm not using textures)
     public Color getCol(int cell, double dist, boolean horizontal) {
         int r, g, b;
         switch (cell) {
             case 0:
                 r = (int) (100 - dist / 10);
                 g = (int) (100 - dist / 10);
                 b = (int) (100 - dist / 10);
                 break;
             case 1:
                 r = (int) (200 - dist / 5);
                 g = (int) (200 - dist / 5);
                 b = (int) (200 - dist / 5);
                 break;
             case 2:
                 r = (int) (200 - dist / 5);
                 g = (int) (0);
                 b = (int) (0);
                 break;
             case 3:
                 r = (int) (0);
                 g = (int) (200 - dist / 5);
                 b = (int) (0);
                 break;
             case 4:
                 r = (int) (0);
                 g = (int) (0);
                 b = (int) (200 - dist / 5);
                 break;
             case -1:
                 r = (int) (100 - dist / 10);
                 g = (int) (0);
                 b = (int) (0);
                 break;
             case -2:
                 r = (int) (0);
                 g = (int) (100 - dist / 10);
                 b = (int) (0);
                 break;
             case -3:
                 r = (int) (0);
                 g = (int) (0);
                 b = (int) (100 - dist / 10);
                 break;
             default:
                 r = (int) (200 - dist / 5);
                 g = (int) (200 - dist / 5);
                 b = (int) (200 - dist / 5);
                 break;
         }
         if (horizontal) {
             r *= 0.65;
             g *= 0.65;
             b *= 0.65;
         }
         r = Math.min(Math.max(0, r), 255);
         g = Math.min(Math.max(0, g), 255);
         b = Math.min(Math.max(0, b), 255);
         return new Color(r, g, b, 255);
     }
 
     public void drawImage(ImageCollection batch, Level level, Player player, String sprite, double X, double Y, boolean fog) {
         x = player.getX();
         y = player.getY();
         angle = player.getDir();
         double angle2 = -Math.atan2(Y - y, X - x);
 
         double dist = Math.sqrt((X - x) * (X - x) + (Y - y) * (Y - y));
         angleOffset = (angle2 - angle - Math.PI) % (Math.PI * 2) + Math.PI;
 
         int imageHeight = (int) (sf / (dist * Math.cos(angleOffset)));
         if (Math.abs(angleOffset) < fov * 0.6 && dist < clip) {
             screenX = screenWidth / 2 - angleOffset * screenWidth / fov;
             Image2D s = new Image2D(sprite);
 
             batch.Draw(s, new Vector2(screenX, screenHeight / 2 + zoffset * imageHeight + zangle), 0, (float) (imageHeight / cellSize), (float) (imageHeight / cellSize), imageHeight + 1000);
         }
     }
 
     public void drawImage(ImageCollection batch, Level level, Player player, Image2D sprite, double X, double Y, boolean fog) {
         x = player.getX();
         y = player.getY();
         angle = player.getDir();
         double angle2 = -Math.atan2(Y - y, X - x);
 
         double dist = Math.sqrt((X - x) * (X - x) + (Y - y) * (Y - y));
         angleOffset = (angle2 - angle - Math.PI) % (Math.PI * 2) + Math.PI;
 
         int imageHeight = (int) (sf / (dist * Math.cos(angleOffset)));
         if (Math.abs(angleOffset) < fov * 0.6 && dist < clip) {
             screenX = screenWidth / 2 - angleOffset * screenWidth / fov;
 
             if (sprites) {
                 batch.Draw(sprite, new Vector2(screenX, screenHeight / 2 + zoffset * imageHeight + zangle), 0, (float) (imageHeight / cellSize), (float) (imageHeight / cellSize), imageHeight + 1000);
             }
 
         }
     }
 
     public void setZ(double z, double zAngle) {
         zoffset = z / cellSize - 0.5;
         zangle = Math.tan(zAngle);
     }
 
     public void drawParticle(ImageCollection batch, Player player, double X, double Y, double Z, Color color) {
         x = player.getX();
         y = player.getY();
         angle = player.getDir();
         double angle2 = -Math.atan2(Y - y, X - x);
         Z = (cellSize / 2 - Z) / cellSize + zoffset;
         double dist = Math.sqrt((X - x) * (X - x) + (Y - y) * (Y - y));
         angleOffset = (angle2 - angle - Math.PI) % (Math.PI * 2) + Math.PI;
         double rectWidth = (sf / (dist * Math.cos(angleOffset))) / cellSize;
 
         if (Math.abs(angleOffset) < fov * 0.6 && dist < clip) {
             screenX = screenWidth / 2 - angleOffset * screenWidth / fov;
 
             batch.fillRect(new Vector2(screenX - rectWidth / 2, screenHeight / 2 + Z * rectWidth * cellSize + zangle - rectWidth / 2), (int) rectWidth, (int) rectWidth, color, (int) (rectWidth * cellSize) + 1000);
 
         }
     }
 }
