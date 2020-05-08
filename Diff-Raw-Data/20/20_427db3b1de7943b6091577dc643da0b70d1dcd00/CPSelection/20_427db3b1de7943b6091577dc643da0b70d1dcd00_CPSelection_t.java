 /*
 	ChibiPaintMod
 
    Copyright (c) 2012-2013 Sergey Semushin
    Copyright (c) 2006-2008 Marc Schefer
 
     This file is part of ChibiPaintMod (previously ChibiPaint).
 
     ChibiPaintMod is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     ChibiPaintMod is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with ChibiPaintMod. If not, see <http://www.gnu.org/licenses/>.
 
  */
 
 package chibipaint.engine;
 
 import chibipaint.gui.CPCanvas;
 import chibipaint.util.CPRect;
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Stack;
 
 public class CPSelection extends CPGreyBmp
 {
 private final int PIXEL_IN_SELECTION_THRESHOLD = 64;     // Pixels with value > this treated as once that belong to selection
 final float DASH_ZOOM_INDEPENDENT_LENGTH = 8.2f; // Dash final length, independent from current zoom
 private byte[] markerArray;
 private float initialDashPiece = 0.0f;
 private boolean initialDashDrawn = false;
 private ArrayList<PixelSingleLine> CurPixelLines;
 private int minX;
 private int minY;
 private int maxX;
 private int maxY;
 
 public CPSelection (int width, int height)
 {
   super (width, height);
   markerArray = new byte[(width + 1) * (height + 1)];
   Arrays.fill (data, (byte) 0);
 }
 
 public void RaiseInitialDash ()
 {
   initialDashPiece += 0.1;
   if (initialDashPiece >= 1.0)
     {
       initialDashDrawn = !initialDashDrawn;
       initialDashPiece -= 1.0;
     }
 }
 
 public CPSelection (CPSelection original)
 {
   super (original);
   System.arraycopy (original.markerArray, 0, markerArray, 0, width * height);
 }
 
 public void AddToSelection (CPSelection otherSelection)
 {
   for (int i = 0; i < height * width; i++)
     {
       if ((data[i] & 0xff) < (otherSelection.data[i] & 0xff))
         data[i] = otherSelection.data[i];
     }
   precalculateSelection ();
 }
 
 
 public void SubtractFromSelection (CPSelection otherSelection)
 {
   for (int i = 0; i < height * width; i++)
     {
       int difference = data[i] & 0xff - otherSelection.data[i] & 0xff;
       data[i] = difference < 0 ? 0 : (byte) difference;
     }
   precalculateSelection ();
 }
 
 
 public void IntersectWithSelection (CPSelection otherSelection)
 {
   for (int i = 0; i < height * width; i++)
     {
       data[i] = (byte) Math.min (data[i] & 0xff, otherSelection.data[i] & 0xff);
     }
   precalculateSelection ();
 }
 
 public void makeRectangularSelection (CPRect rect)
 {
   for (int j = 0; j < height; j++)
     for (int i = 0; i < width; i++)
       {
         if ((i >= rect.left) && (i < rect.right) &&
                 (j >= rect.top) && (j < rect.bottom)
                 )
           data[j * width + i] = (byte) 0xff;
         else
           data[j * width + i] = 0;
       }
   precalculateSelection ();
 }
 
 public void make (CPColorBmp src, int offsetX, int offsetY)
 {
   makeEmpty ();
   for (int j = 0; j < src.getHeight (); j++)
     {
       if (j + offsetY >= height || j + offsetY < 0)
         continue;
       int offset = (j + offsetY) * width + offsetX;
       int srcOffset = j * src.getWidth ();
       for (int i = 0; i < src.getWidth (); i++, offset++, srcOffset++)
         {
           if (i + offsetX >= width || i + offsetX < 0)
             continue;
           data[offset] = (byte) ((src.getData ()[srcOffset] >> 24) & 0xFF);
         }
     }
   precalculateSelection ();
 }
 
 public void makeSelectionFromPolygon (Path2D polygon, AffineTransform canvasTransform)
 {
   BufferedImage bImage = new BufferedImage (width, height, BufferedImage.TYPE_BYTE_GRAY);
   Graphics2D g = bImage.createGraphics ();
   try
     {
       g.transform (canvasTransform.createInverse ());
     }
   catch (NoninvertibleTransformException e)
     {
       e.printStackTrace ();  // Shouldn't be here
     }
   g.setColor (Color.WHITE);
   polygon.setWindingRule (Path2D.WIND_EVEN_ODD);
   g.fill (polygon);
   data = ((DataBufferByte) bImage.getData ().getDataBuffer ()).getData ();
   precalculateSelection ();
 }
 
 private boolean getIsActive (int i, int j)
 {
   return isInside (i, j) && getIsActiveInBounds (i, j);
 }
 
 private boolean getIsActiveInBounds (int i, int j)
 {
   return ((data[j * width + i] & 0xff) > PIXEL_IN_SELECTION_THRESHOLD);
 }
 
 private boolean isActive (PixelCoords px)
 {
   return isInside (px.x, px.y) && getIsActiveInBounds (px.x, px.y);
 }
 
 private int isMarked (PixelCoords px)
 {
   if (isInside (px.x, px.y))
     return (markerArray[px.y * width + px.x]);
   else
     return 15;
 }
 
 private static void drawLine (Graphics2D g2d, CPCanvas canvas, float x1, float y1, float x2, float y2)
 {
   Point2D.Float p1 = canvas.coordToDisplay (new Point2D.Float (x1, y1));
   Point2D.Float p2 = canvas.coordToDisplay (new Point2D.Float (x2, y2));
   g2d.draw (new Line2D.Float (p1, p2));
 }
 
 
 private final int[][] Mv = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
 private final int[][] Corners = {{1, 1}, {0, 1}, {0, 0}, {1, 0}};
 private final int[] OppositeDirection = {2, 3, 0, 1};
 private final int[] PowersOf2 = {1, 2, 4, 8};
 
 public void makeEmpty ()
 {
   Arrays.fill (data, (byte) 0);
   minX = 1;
   maxX = 0;
   minY = 1;
   maxY = 0;
 }
 
 public void selectAll ()
 {
   Arrays.fill (data, (byte) -1);
   precalculateSelection ();
 }
 
 public CPSelection copy ()
 {
   CPSelection copySelection = new CPSelection (width, height);
   copySelection.data = data.clone ();
   copySelection.precalculateSelection ();
   return copySelection;
 }
 
 public void copyFrom (CPSelection selection)
 {
   if (selection.width != width || selection.height != height)
     {
       width = selection.width;
       height = selection.height;
       data = new byte[width * height];
       markerArray = new byte[(width + 1) * (height + 1)];
     }
   System.arraycopy (selection.data, 0, data, 0, data.length);
 
   precalculateSelection ();
 }
 
public void applySelectionToData (int dataArg[])
 {
   for (int j = 0; j < height; j++)
     for (int i = 0; i < width; i++)
       {
        int selectionValue = getData (i, j);
        int sourceAlpha = ((dataArg[j * width + i] & (0xff000000)) >>> 24);
        dataArg[j * width + i] = (dataArg[j * width + i] & (0x00ffffff)) | ((int) (sourceAlpha >= selectionValue ? selectionValue : sourceAlpha) << 24);
       }
 }
 
 public void makeSelectionFromAlpha (int[] dataArg)
 {
   for (int j = 0; j < height; j++)
     for (int i = 0; i < width; i++)
       {
         data[j * width + i] = (byte) ((dataArg[j * width + i] & (0xff000000)) >> 24);
       }
   precalculateSelection ();
 }
 
 public int getData (int i, int j)
 {
   return data[j * width + i];
 }
 
 public int cutOpacity (int value, int i, int j)
 {
   if (isEmpty ())
     return value;
   int newValue = value;
   int selection = (data[j * width + i] & 0xFF) * 255;
   if (selection < newValue)
     newValue = selection;
   return newValue;
 }
 
 
 class PixelCoords implements Comparable<PixelCoords>
 {
   int x;
   final int y;
 
   PixelCoords (int xArg, int yArg)
   {
     x = xArg;
     y = yArg;
   }
 
   public PixelCoords (PixelCoords point)
   {
     x = point.x;
     y = point.y;
   }
 
   PixelCoords left ()
   {
     return new PixelCoords (x - 1, y);
   }
 
   PixelCoords right ()
   {
     return new PixelCoords (x + 1, y);
   }
 
   PixelCoords up ()
   {
     return new PixelCoords (x, y - 1);
   }
 
   PixelCoords down ()
   {
     return new PixelCoords (x, y + 1);
   }
 
   PixelCoords MoveByMv (int num)
   {
     return new PixelCoords (x + Mv[num % 4][0], y + Mv[num % 4][1]);
   }
 
   PixelCoords MoveToCorner (int num)
   {
     return new PixelCoords (x + Corners[num % 4][0], y + Corners[num % 4][1]);
   }
 
   @Override
   public int compareTo (PixelCoords o)
   {
     if (x != o.x)
       return (x - o.x);
     else
       return (o.y - y);
   }
 
 
 }
 
 private class Lump extends ArrayList<PixelCoords>
 {
 }
 
 private class PixelSingleLine extends ArrayList<PixelCoords>
 {
   boolean backwards = false;
 }
 
 private static final int directionsNum = 4;
 
 enum Directions
 {
   Right,
   Down,
   Left,
   Top,
 }
 
 
 int GetNextDirectionNum (PixelCoords currentPoint, PixelCoords prevPoint)
 {
   int result = -1;
   for (int i = 0; i < directionsNum; i++)
     if (currentPoint.x + Corners[i][0] == prevPoint.x &&
             currentPoint.y + Corners[i][1] == prevPoint.y)
       {
         result = i;
         break;
       }
   return result;
 }
 
 public Rectangle getBoundingBox (CPCanvas canvas)
 {
   Point2D.Float[] points = {canvas.coordToDisplay (new Point2D.Float (minX, minY)), canvas.coordToDisplay (new Point2D.Float (minX, maxY + 2.f)),
           canvas.coordToDisplay (new Point2D.Float (maxX + 2.f, maxY + 2.f)), canvas.coordToDisplay (new Point2D.Float (maxX + 2.f, minY))};
   Path2D.Float path = new Path2D.Float ();
   path.moveTo (points[0].x, points[0].y);
   path.lineTo (points[1].x, points[1].y);
   path.lineTo (points[2].x, points[2].y);
   path.lineTo (points[3].x, points[3].y);
   path.closePath ();
   Rectangle2D rect = path.getBounds2D ();
   return new Rectangle ((int) rect.getX (), (int) rect.getY (), (int) Math.ceil (rect.getMaxX ()) - (int) rect.getX (),
                         (int) Math.ceil (rect.getMaxY ()) - (int) rect.getY ());
 }
 
 public CPRect getBoundingRect ()
 {
   return new CPRect (minX, minY, maxX + 1, maxY + 1);
 }
 
 void convertLumpToPixelSingleLines (ArrayList<PixelSingleLine> pixelLinesTarget, Lump x)
 {
   // TODO: Rewrite using scanline technique
   int fillMode = 1;
   PixelCoords startingPoint = Collections.min (x);
   PixelSingleLine sL = new PixelSingleLine ();
   sL.add (startingPoint.right ().down ());
   do
     {
       PixelCoords currentPoint = new PixelCoords (startingPoint);
       boolean Finished = false;
       do
         {
           int scanningDirection = GetNextDirectionNum (currentPoint, sL.get (sL.size () - 1));
           if (scanningDirection == -1)
             break;
           for (int i = 1; i < directionsNum; i++)
             {
               scanningDirection = (scanningDirection + 1) % 4;
               if (isActive (currentPoint.MoveByMv (scanningDirection)) == (fillMode == 0))
                 {
                   if (fillMode == 1)
                     markerArray[currentPoint.y * width + currentPoint.x] |= PowersOf2[scanningDirection];
                   else
                     markerArray[(currentPoint.y + Mv[scanningDirection][1]) * width + currentPoint.x + Mv[scanningDirection][0]] |= (PowersOf2[OppositeDirection[scanningDirection]]);
                   if (currentPoint.MoveToCorner (scanningDirection).compareTo (sL.get (0)) != 0)
                     sL.add (currentPoint.MoveToCorner (scanningDirection));
                   else
                     {
                       Finished = true;
                       break;
                     }
                 }
               else
                 {
                   PixelCoords nextPoint = currentPoint.MoveByMv (scanningDirection);
                   PixelCoords pointAfterNext = nextPoint.MoveByMv (GetNextDirectionNum (nextPoint, sL.get (sL.size () - 1)) + 1);
                   if (isActive (pointAfterNext) == (fillMode == 0))
                     currentPoint = nextPoint;
                   else
                     currentPoint = pointAfterNext;
                   break;
                 }
             }
           if (Finished)
             break;
 
         }
       while (true);
       if (fillMode == 0)
         Collections.reverse (sL);
       pixelLinesTarget.add (sL);
       fillMode = 0;
       boolean holeFound = false;
       // Checking for inner holes
       for (int i = 0; i < x.size (); i++)
         {
           for (int j = 0; j < directionsNum; j++)
             {
               PixelCoords px = x.get (i).MoveByMv (j);
               if (!isActive (px) && ((isMarked (x.get (i)) & PowersOf2[j]) == 0))
                 {
                   startingPoint = px;
                   sL = new PixelSingleLine ();
                   for (int k = 0; k < directionsNum; k++)
                     {
                       if (isActive (startingPoint.MoveByMv (k)))
                         {
                           sL.add (startingPoint.MoveToCorner ((k + 3) % 4));
                           break;
                         }
                     }
 
                   holeFound = true;
                   break;
                 }
             }
           if (holeFound)
             break;
         }
       if (!holeFound)
         break;
     }
   while (true);
 }
 
 public boolean isEmpty ()
 {
   return (minX >= maxX || minY >= maxY);
 }
 
 public void drawItself (Graphics2D g2d, CPCanvas canvas)
 {
         /*
     g2d.setXORMode(new Color(0x808080));
 		Stroke stroke = g2d.getStroke();
 		// float dashSize = 1.f / canvas.getZoom();
 		g2d.setStroke (new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f,
 				new float[] { 8.0f, 8.0f}, 0f));
 		 */
 
   if (minX >= maxX || minY >= maxY)
     return;
 
   Color prevColor = g2d.getColor ();
   float defaultDashLength = DASH_ZOOM_INDEPENDENT_LENGTH / canvas.getZoom ();
   float dashLength = defaultDashLength;
 
   for (int i = 0; i < CurPixelLines.size (); i++)
     {
       /*
       float newDashLength = defaultDashLength;
 
 			int NumberOfDashes = (int) Math.ceil(((CurPixelLines.get(i).size ()) / dashLength));
 			if (NumberOfDashes != 0)
 				newDashLength = CurPixelLines.get(i).size () / NumberOfDashes;
 
 			if (Math.abs (newDashLength) > Float.MIN_VALUE)
 				dashLength = newDashLength;
 			else
 				dashLength = defaultDashLength;
 			 */
 
       boolean dashDrawn = initialDashDrawn;
       float dashCurLength = initialDashPiece * dashLength;
       for (int j = 0; j < CurPixelLines.get (i).size (); j++)
         {
           PixelSingleLine CurPSL = CurPixelLines.get (i);
           PixelCoords FirstPoint = CurPSL.get (j);
           PixelCoords SecondPoint = CurPSL.get ((j + 1) % CurPixelLines.get (i).size ());
           if (dashLength - dashCurLength > 1.0f)
             {
               if (dashDrawn)
                 g2d.setColor (Color.black);
               else
                 g2d.setColor (Color.white);
 
               drawLine (g2d, canvas, FirstPoint.x, FirstPoint.y, SecondPoint.x, SecondPoint.y);
               dashCurLength += 1.0f;
             }
           else
             {
               float segmentStart = 0.0f;
               boolean leaveCycle = false;
               while (true)
                 {
                   float segmentEnd = segmentStart + (dashLength - dashCurLength);
                   if (segmentEnd > 1.0f)
                     {
                       dashCurLength = 1.0f - segmentStart;
                       segmentEnd = 1.0f;
                       leaveCycle = true;
                     }
                   if (dashDrawn)
                     g2d.setColor (Color.black);
                   else
                     g2d.setColor (Color.white);
 
                   drawLine (g2d, canvas, FirstPoint.x + (SecondPoint.x - FirstPoint.x) * (segmentStart), FirstPoint.y + (SecondPoint.y - FirstPoint.y) * (segmentStart),
                             FirstPoint.x + (SecondPoint.x - FirstPoint.x) * (segmentEnd),
                             FirstPoint.y + (SecondPoint.y - FirstPoint.y) * (segmentEnd));
 
 
                   if (leaveCycle)
                     break;
                   segmentStart = segmentEnd;
                   dashDrawn = !dashDrawn;
                   dashCurLength = 0.0f;
                 }
             }
         }
     }
 
   g2d.setColor (prevColor);
   // Preparing
   // And then the MAGICS!
   // g2d.setStroke(stroke);
 }
 
 private void CalculateBoundingBox ()
 {
   for (int i = 0; i < width; i++)
     for (int j = 0; j < height; j++)
       {
         if (data[j * width + i] != 0)
           {
             minX = Math.min (minX, i);
             maxX = Math.max (maxX, i);
             minY = Math.min (minY, j);
             maxY = Math.max (maxY, j);
           }
       }
 }
 
 public void precalculateSelection ()
 {
 
   minX = width;
   maxX = 0;
   minY = height;
   maxY = 0;
   CalculateBoundingBox (); // Warning: We count everything non-zero into bounding box, so the function is separate.
   // First step: we're dividing everything on separate 4-connected regions
   ArrayList<Lump> lumps = new ArrayList<Lump> ();
   Arrays.fill (markerArray, (byte) 0);
   for (int i = 0; i < width; i++)
     for (int j = 0; j < height; j++)
       {
         if (getIsActive (i, j) && markerArray[j * width + i] == 0) // If something active and not marked found then we're making Depth-first search
           {
             lumps.add (MakeLumpByScanLines (i, j));
           }
       }
 
   Arrays.fill (markerArray, (byte) 0);
   // Now we've got our pixels lumped according to 4-connections, now let's build their boundary
   // They will be converted to pixel single lines class which is basically - lines to be drawn.
   CurPixelLines = new ArrayList<PixelSingleLine> ();
   for (int i = 0; i < lumps.size (); i++)
     {
       convertLumpToPixelSingleLines (CurPixelLines, lumps.get (i));
     }
 }
 
 private Lump MakeLumpByScanLines (int x, int y)
 {
   Stack<PixelCoords> S = new Stack<PixelCoords> ();
   PixelCoords newPx = new PixelCoords (x, y);
   S.push (newPx);
   Lump ResultingLump = new Lump ();
   while (!S.empty ())
     {
       PixelCoords px = S.pop ();
       // Skipping all the stuff we should add into our lump from the left
       while (px.x >= 0 && getIsActiveInBounds (px.x, px.y))
         px.x--;
       px.x++; // Now we find the left side of this part
       // careful: px.x is used as an iterator and px.y is constant
       boolean spanTop = false;
       boolean spanBottom = false;
 
       while (px.x < width && getIsActiveInBounds (px.x, px.y) && markerArray[px.y * width + px.x] == 0)
         {
           ResultingLump.add (new PixelCoords (px));
           markerArray[px.y * width + px.x] = 1;
           if (!spanTop && px.y > 0 && getIsActiveInBounds (px.x, px.y - 1) && markerArray[px.y * width - width + px.x] == 0)
             {
               newPx = new PixelCoords (px.x, px.y - 1);
               S.push (newPx);
               spanTop = true;
             }
           else if (spanTop && px.y > 0 && (!getIsActiveInBounds (px.x, px.y - 1) || markerArray[px.y * width - width + px.x] == 1))
             {
               spanTop = false;
             }
           if (!spanBottom && px.y < height - 1 && getIsActiveInBounds (px.x, px.y + 1) && markerArray[px.y * width + width + px.x] == 0)
             {
               newPx = new PixelCoords (px.x, px.y + 1);
               S.push (newPx);
               spanBottom = true;
             }
           else if (spanBottom && px.y < height - 1 && (!getIsActiveInBounds (px.x, px.y + 1) || markerArray[px.y * width + width + px.x] == 1))
             {
               spanBottom = false;
             }
           px.x++;
         }
     }
   return ResultingLump;
 }
 
 private Lump MakeLump (int x, int y)
 {
   Stack<PixelCoords> S = new Stack<PixelCoords> ();
   PixelCoords newPx = new PixelCoords (x, y);
   S.push (newPx);
   Lump ResultingLump = new Lump ();
   while (!S.empty ())
     {
       PixelCoords px = S.pop ();
       markerArray[px.y * width + px.x] = 1;
       ResultingLump.add (px);
       for (int i = 0; i < Mv.length; i++)
         {
           int newY = px.y + Mv[i][1];
           int newX = px.x + Mv[i][0];
           if (!isInside (newX, newY))
             continue;
           if (markerArray[newY * width + newX] == 0 && getIsActiveInBounds (newX, newY))
             S.push (new PixelCoords (newX, newY));
         }
     }
   return ResultingLump;
 }
 }
