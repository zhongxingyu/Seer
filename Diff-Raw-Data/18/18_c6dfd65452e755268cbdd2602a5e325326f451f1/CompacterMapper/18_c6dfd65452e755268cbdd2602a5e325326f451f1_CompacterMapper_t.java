 /*
 Copyright 2011 Matt DeVore
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package com.github.matvore.andradion2.build;
 
 import com.github.matvore.andradion2.data.BinaryOutputStream;
 import com.github.matvore.andradion2.data.Copier;
 import com.github.matvore.andradion2.data.ImmutableList;
 import com.github.matvore.andradion2.data.Lists;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class CompacterMapper {
   private static final class ByteMatrix implements Cloneable {
     public final byte[] bytes;
     public final int width, height;
 
     public ByteMatrix(int width, int height) {
       bytes = new byte[width * height];
       this.width = width;
       this.height = height;
     }
 
     public int index(int x, int y) {
       return y * width + x;
     }
 
     @Override
     public ByteMatrix clone() {
       ByteMatrix copy = new ByteMatrix(width, height);
       System.arraycopy(bytes, 0, copy.bytes, 0, bytes.length);
       return copy;
     }
   }
 
   private static final class TransparentOpaquePair {
     private final int transparentPixels;
     private final byte[] opaquePixels;
 
     private TransparentOpaquePair(int transparentPixels, byte[] opaquePixels) {
       this.opaquePixels = opaquePixels;
       this.transparentPixels = transparentPixels;
     }
 
     public int getTransparentPixels() {
       return transparentPixels;
     }
 
     public int getOpaquePixelCount() {
       return opaquePixels.length;
     }
 
     public void writeOpaquePixelsTo(BinaryOutputStream stream)
         throws IOException {
       stream.putBytes(opaquePixels);
     }
 
     public static TransparentOpaquePair readFrom(
         ByteMatrix data, int x, int y, byte transparentColor) {
       int dataIndex = data.index(x, y);
       int transparentPixels = 0;
 
       while (true) {
         if (x + transparentPixels == data.width) {
           return null;
         }
         if (data.bytes[dataIndex + transparentPixels] != transparentColor) {
           break;
         }
         transparentPixels++;
       }
 
       int opaquePixels = 0;
       while (true) {
         if (x + transparentPixels + opaquePixels == data.width ||
             data.bytes[dataIndex + transparentPixels + opaquePixels] ==
                 transparentColor) {
           break;
         }
         opaquePixels++;
       }
 
       byte[] opaqueData = new byte[opaquePixels];
       System.arraycopy(data.bytes, dataIndex + transparentPixels,
           opaqueData, 0, opaquePixels);
 
       return new TransparentOpaquePair(transparentPixels, opaqueData);
     }
   }
 
   private enum ColorThat {IS, IS_NOT}
 
   private final byte transparentColor;
   private final int minimumColorBlockArea;
   private final List<Dimension> patternSizes;
   private final int minimumPatternArea;
 
   private CompacterMapper(byte transparentColor,
       int minimumColorBlockArea, List<Dimension> patternSizes,
       int minimumPatternArea) {
     this.transparentColor = transparentColor;
     this.minimumColorBlockArea = minimumColorBlockArea;
     this.patternSizes = patternSizes;
     this.minimumPatternArea = minimumPatternArea;
   }
 
   public static CompacterMapper of(byte transparentColor,
       int minimumColorBlockArea, List<Dimension> patternSizes,
       int minimumPatternArea) {
     return new CompacterMapper(transparentColor, minimumColorBlockArea,
         ImmutableList.copyOf(patternSizes, Copier.FOR_DIMENSION),
         minimumPatternArea);
   }
 
   private static void drawRectangle(
       ByteMatrix data, byte color, int x, int y, int width, int height) {
     int index = data.index(x, y);
     for (int cy = 0; cy < height; cy++) {
       for (int cx = 0; cx < width; cx++) {
         data.bytes[index + cx] = color;
       }
       index += data.width;
     }
   }
 
   private static void drawRectangle(
       ByteMatrix data, byte color, Rectangle area) {
     drawRectangle(data, color, area.x, area.y, area.width, area.height);
   }
 
   private static void drawRectangle(
       ByteMatrix data, byte color, Point location, Dimension size) {
     drawRectangle(data, color, location.x, location.y, size.width, size.height);
   }
 
   private static ByteMatrix makeCopy(
       ByteMatrix source, Point location, Dimension size) {
     ByteMatrix result = new ByteMatrix(size.width, size.height);
     int sourceIndex = source.index(location.x, location.y);
     int resultIndex = 0;
     for (int y = 0; y < size.height; y++) {
       System.arraycopy(
           source.bytes, sourceIndex, result.bytes, resultIndex, size.width);
       resultIndex += result.width;
       sourceIndex += source.width;
     }
     return result;
   }
 
   private static boolean areaIs(ColorThat target, byte color,
       ByteMatrix data, int x, int y, int width, int height) {
     boolean xor = (target == ColorThat.IS_NOT);
     int index = data.index(x, y);
     for (int cy = 0; cy < height; cy++) {
       for (int cx = 0; cx < width; cx++) {
         if ((data.bytes[index + cx] != color) ^ xor) {
           return false;
         }
       }
       index += data.width;
     }
     return true;
   }
 
   private static boolean areaIs(ColorThat target, byte color, ByteMatrix data,
       Rectangle area) {
     return areaIs(target, color, data, area.x, area.y, area.width, area.height);
   }
 
   private static boolean areaIs(ColorThat target, byte color, ByteMatrix data,
       Point point, Dimension size) {
     return areaIs(
         target, color, data, point.x, point.y, size.width, size.height);
   }
 
   /**
    * Finds rectangles of the color at {@code startX, startY} and puts them in a
    * list of {@code Rectangles}. Will only return rectangles whose upper-left
    * coordinates are after or at the starting coordinates, and replacing them
    * with {@code transparentColor} at the same time.  Ignores rectangles whose
    * areas are less than {@code minimumColorBlockArea}.
    */
   private List<Rectangle> findAndClearColorBlocks(
       ByteMatrix data, int startX, int startY) {
     List<Rectangle> result = Lists.newArrayList();
     int currX = startX, currY = startY;
     int dataPosition = data.index(currX, currY);
     byte baseColor = data.bytes[dataPosition];
     Rectangle scanningArea = new Rectangle();
 
     for (; dataPosition < data.bytes.length; dataPosition++) {
       if (baseColor == data.bytes[dataPosition]) {
         scanningArea.x = currX;
         scanningArea.width = 1;
         scanningArea.y = currY;
         scanningArea.height = 1;
 
         // Expand to the right.
         do {
           scanningArea.x++;
         } while (scanningArea.x < data.width &&
             areaIs(ColorThat.IS, baseColor, data, scanningArea));
 
         scanningArea.width = scanningArea.x - currX;
         scanningArea.x = currX;
 
         // Expand downward.
         do {
           scanningArea.y++;
         } while (scanningArea.y < data.height &&
             areaIs(ColorThat.IS, baseColor, data, scanningArea));
 
         scanningArea.height = scanningArea.y - currY;
         scanningArea.y = currY;
 
         if (scanningArea.width * scanningArea.height >= minimumColorBlockArea) {
           result.add(scanningArea);
           drawRectangle(data, transparentColor, scanningArea);
           scanningArea = new Rectangle();
         }
       }
 
       // Increment x and wrap around if appropriate.
       if (++currX >= data.width) {
         currX = 0;
         currY++;
       }
     }
     return result;
   }
 
   /**
    * Returns {@code true} iff two areas have the same pixel data.
    */
   private static boolean sameData(
       ByteMatrix data, Dimension dimension, Point coors1, Point coors2) {
     int index1 = data.index(coors1.x, coors1.y);
     int index2 = data.index(coors2.x, coors2.y);
     int changingRows = data.width - dimension.width;
 
     for (int y = 0; y < dimension.height; y++) {
       for (int x = 0; x < dimension.width; x++) {
         if (data.bytes[index1++] != data.bytes[index2++]) {
           return false;
         }
       }
       index1 += changingRows;
       index2 += changingRows;
     }
 
     return true;
   }
 
   private static boolean sameAndSeparate(
       ByteMatrix data, Dimension dimension, List<Point> coors) {
     // compare similar data
     Point first = coors.get(0);
     for (int i = coors.size() - 1; i >= 1; i--) {
       Point second = coors.get(i);
       if (!sameData(data, dimension, first, second)) {
         return false;
       }
     }
 
     // look for overlapping (the first coor does not overlap at all)
     for (int c1 = 0; c1 < coors.size(); c1++) {
       Point c1Coor = coors.get(c1);
       int c1Left = c1Coor.x;
       int c1Right = c1Coor.x + dimension.width;
       int c1Top = c1Coor.y;
       int c1Bottom = c1Coor.y + dimension.height;
 
       for (int c2 = c1 + 1; c2 < coors.size(); c2++) {
         Point c2Coor = coors.get(c2);
 
         if (!((c1Left >= c2Coor.x + dimension.width) ||
             (c1Right <= c2Coor.x) ||
             (c1Top >= c2Coor.y + dimension.height) ||
             (c1Bottom <= c2Coor.y))) {
           return false;
         }
       }
     }
 
     return true;
   }
 
   private static Point findPixel(ColorThat target, byte color,
       ByteMatrix data, int startX, int startY) {
     int dataIndex = data.index(startX, startY);
     int coorX = startX, coorY = startY;
     boolean xor = target == ColorThat.IS_NOT;
 
     while (dataIndex < data.bytes.length) {
       if ((data.bytes[dataIndex] == color) ^ xor) {
         return new Point(coorX, coorY);
       }
 
       // Increment current position.
       if (++coorX >= data.width) {
         coorX = 0;
         ++coorY;
       }
 
       dataIndex++;
     }
 
     return null;
   }
 
   public void compact(BufferedImage source, List<Color> palette,
       OutputStream output) throws IOException {
     int width = source.getWidth(), height = source.getHeight();
     if (width > 256 || height > 256) {
       throw new IllegalArgumentException(
           "Bitmap is too large; dimensions must be <= 256: " +
           width + ", " + height);
     }
     if (palette.isEmpty() || palette.size() > 256) {
       throw new IllegalArgumentException(
           "palette is too large; must be in the range of [1, 256]: " +
           palette.size());
     }
 
     // Translate bitmap data into 8-bit palette entries.
     int[] rgbRow = new int[width];
     ByteMatrix eightBitImage = new ByteMatrix(width, height);
     int pixelIndex = 0;
     for (int y = 0; y < height; y++) {
       source.getRGB(0, y, width, 1, rgbRow, 0, width);
       for (int x = 0; x < width; x++, pixelIndex++) {
         int r = (rgbRow[x] >> 16) & 0xff;
         int g = (rgbRow[x] >> 8) & 0xff;
         int b = rgbRow[x] & 0xff;
         int bestMatch = -1, closeness = Integer.MAX_VALUE;
 
         int i = 0;
         for (Color thisColor : palette) {
           int thisCloseness =
               Math.abs(r - thisColor.getRed()) +
               Math.abs(g - thisColor.getGreen()) +
               Math.abs(b - thisColor.getBlue());
 
           if (thisCloseness < closeness) {
             bestMatch = i;
             closeness = thisCloseness;
             if (closeness == 0) {
               break;
             }
           }
           i++;
         }
         eightBitImage.bytes[pixelIndex] = (byte)bestMatch;
       }
     }
     compact(eightBitImage, output);
   }
 
   private void compact(ByteMatrix data, OutputStream output)
       throws IOException {
     Map<Byte, List<Rectangle>> colorBlocks =
         new HashMap<Byte, List<Rectangle>>();
     List<ByteMatrix> patterns = Lists.newArrayList();
     List<List<Point>> patternCoors = Lists.newArrayList();
     data = data.clone();
 
     // Search for color blocks.
 
     int locX = 0, locY = 0;
 
     // Contains all checked pixel colors.
     Point nonTransparentCoor;
 
     while ((nonTransparentCoor = findPixel(
         ColorThat.IS_NOT, transparentColor, data, locX, locY)) != null) {
       locX = nonTransparentCoor.x;
       locY = nonTransparentCoor.y;
       Byte colorHere = data.bytes[data.index(locX, locY)];
 
       // Only process if we have not processed this color before.
       if (!colorBlocks.containsKey(colorHere)) {
         List<Rectangle> colorBlockAreas = findAndClearColorBlocks(
            data, locX, locY);
 
         colorBlocks.put(colorHere, colorBlockAreas);
       }
 
       if (++locX >= data.width) {
         locX = 0;
         if (++locY >= data.height) {
           break;
         }
       }
     }
 
     // Find patterns.
     for (Dimension patternSize : patternSizes) {
       locX = 0;
       locY = 0;
 
       while ((nonTransparentCoor = findPixel(
           ColorThat.IS_NOT, transparentColor, data, locX, locY)) != null) {
         locX = nonTransparentCoor.x;
         locY = nonTransparentCoor.y;
         byte baseColor = data.bytes[data.index(locX, locY)];
         if (locX + patternSize.width > data.width) {
           // Over the right-hand side, must go to start of next line
           locX = 0;
           locY++;
           continue;
         }
 
         if (locY + patternSize.height > data.height) {
           // Over the bottom edge, start over with different pattern size
           break;
         }
 
         if (!areaIs(ColorThat.IS_NOT, transparentColor, data,
             locX, locY, patternSize.width, patternSize.height)) {
           if (++locX >= data.width) {
             locX = 0;
             if (++locY >= data.height) {
               break;
             }
           }
           continue;
         }
 
         List<Point> currentPatternCoors = Lists.newArrayList();
         currentPatternCoors.add(new Point(locX, locY));
 
         // Increment x to avoid overlapping with first instance.
         int patternCoorX = locX + patternSize.width;
         int patternCoorY = locY;
         if (patternCoorX + patternSize.width > data.width) {
           patternCoorX = 0;
           patternCoorY++;
         }
 
         Point max = (Point)currentPatternCoors.get(0).clone();
 
         Point matchFirstPixelCoor;
         while ((matchFirstPixelCoor = findPixel(ColorThat.IS, baseColor,
             data, patternCoorX, patternCoorY)) != null) {
           patternCoorX = matchFirstPixelCoor.x;
           patternCoorY = matchFirstPixelCoor.y;
           if (patternCoorY + patternSize.height > data.height) {
             break;
           }
 
           if (patternCoorX + patternSize.width <= data.width &&
               sameAndSeparate(data, patternSize,
                   Lists.listPlusOneElement(
                       currentPatternCoors, matchFirstPixelCoor))) {
             currentPatternCoors.add(matchFirstPixelCoor);
             max.y = matchFirstPixelCoor.y;
             if (matchFirstPixelCoor.x > max.x) {
               max.x = matchFirstPixelCoor.x;
             }
           }
 
           if (++patternCoorX + patternSize.width > data.width) {
             patternCoorX = 0;
             if (++patternCoorY + patternSize.height > data.height) {
               // We're done finding extra instances.
               break;
             }
           }
         }
 
         if (currentPatternCoors.size() > 1) {
           // Spread this pattern outwards (down and to the right)
           Dimension expandedPatternSize = (Dimension)patternSize.clone();
 
           // First spread to the right
           while (++expandedPatternSize.width + max.x <= data.width &&
               sameAndSeparate(data, expandedPatternSize,
                   currentPatternCoors) &&
               areaIs(ColorThat.IS_NOT, transparentColor, data,
                   currentPatternCoors.get(0), expandedPatternSize)) {
             // Do nothing.
           }
           expandedPatternSize.width--;
 
           // Now spread downward
           while (++expandedPatternSize.height + max.y <= data.height &&
               sameAndSeparate(data, expandedPatternSize,
                   currentPatternCoors) &&
               areaIs(ColorThat.IS_NOT, transparentColor, data,
                   currentPatternCoors.get(0), expandedPatternSize)) {
             // Do nothing.
           }
           expandedPatternSize.height--;
 
           if (expandedPatternSize.width * expandedPatternSize.height >=
               minimumPatternArea) {
             patterns.add(makeCopy(
                 data, currentPatternCoors.get(0), expandedPatternSize));
             patternCoors.add(currentPatternCoors);
 
             for (Point patternCoor : currentPatternCoors) {
               drawRectangle(
                   data, transparentColor, patternCoor, expandedPatternSize);
             }
           }
         }
 
         if (++locX + patternSize.width > data.width) {
           locX = 0;
           if (++locY + patternSize.height > data.height) {
             break;
           }
         }
       }
     }
 
     // Compile left over data pieces.
     List<List<TransparentOpaquePair>> leftOvers = Lists.newArrayList();
 
     for (int y = 0; y < data.height; y++) {
       List<TransparentOpaquePair> leftOverRow = Lists.newArrayList();
       int x = 0;
       TransparentOpaquePair next = null;
       while ((next = TransparentOpaquePair.readFrom(
           data, x, y, transparentColor)) != null) {
         leftOverRow.add(next);
         x += next.getTransparentPixels() +
             next.getOpaquePixelCount();
       }
       leftOvers.add(leftOverRow);
     }
 
     // shrink down the leftOvers vector if
     //  some of the last elements are not needed
     while (!leftOvers.isEmpty() &&
         leftOvers.get(leftOvers.size() - 1).isEmpty()) {
       leftOvers.remove(leftOvers.size() - 1);
     }
 
     // Write to file
     BinaryOutputStream out = BinaryOutputStream.of(output);
 
    // Calculate number of block colors with at least one block,
    // then write that count to the stream.
    for (Iterator<List<Rectangle>> blockColors =
         colorBlocks.values().iterator(); blockColors.hasNext(); ) {
      if (blockColors.next().isEmpty()) {
        blockColors.remove();
       }
    }
    out.putByte(colorBlocks.size());
 
    for (Map.Entry<Byte, List<Rectangle>> blockColors :
        colorBlocks.entrySet()) {
       // print the color
       out.putByte(blockColors.getKey().intValue());
 
       // how many blocks of it we have
       out.putWordThatIsUsuallyByte(blockColors.getValue().size());
 
       for (Rectangle block : blockColors.getValue()) {
         out.putByte(block.x);
         out.putByte(block.y);
         out.putByte(block.width - 1);
         out.putByte(block.height -1);
       }
     }
 
     // now print out stuff about our patterns to the file
     out.putWordThatIsUsuallyByte(patterns.size());
     for (int i = 0; i < patterns.size(); i++) {
       // print the pattern definition
       out.putWordThatIsUsuallyByte(patterns.get(i).width);
       out.putWordThatIsUsuallyByte(patterns.get(i).height);
       out.putBytes(patterns.get(i).bytes);
 
       // print how many rects we have of it
       List<Point> coors = patternCoors.get(i);
       out.putWordThatIsUsuallyByte(coors.size());
 
       // print the rects' locations
       for (Point coor : coors) {
         out.putByte(coor.x);
         out.putByte(coor.y);
       }
     }
 
     // now print out stuff about the left over number of rows
     // In the left overs, each row is a vector of BYTES with a size of at least
     //  1. If the first BYTE is 0, then no pixels are on the line
     //  If the first BYTE is x, where x > 0, then there are x
     //  trans/opaque pairs
     // a trans/opaque pair has a run of transparent
     // pixels, and a run of non-transparent pixels.  For each
     // trans/opaque pair, there is a number which specifies the number
     // of transparent pixels, then a number which specifies the
     // number of opaque pixels - 1, then the opaque pixel data.
 
     out.putWordThatIsUsuallyByte(leftOvers.size());
 
     for (List<TransparentOpaquePair> leftOverRow : leftOvers) {
       out.putByte(leftOverRow.size());
 
       for (TransparentOpaquePair pair : leftOverRow) {
         out.putByte(pair.getTransparentPixels());
         out.putByte(pair.getOpaquePixelCount() - 1);
         pair.writeOpaquePixelsTo(out);
       }
     }
   }
 }
