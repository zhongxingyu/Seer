 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package invenio.pdf.features;
 
 import invenio.common.Pair;
 import invenio.pdf.core.ExtractorParameters;
 import invenio.pdf.core.FeatureNotPresentException;
 import invenio.pdf.core.IPDFPageFeature;
 import invenio.pdf.core.IPDFPageFeatureProvider;
 import invenio.pdf.core.PDFPageManager;
 import java.awt.Rectangle;
 import java.awt.image.Raster;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  *
  * @author piotr
  */
 public class PageLayoutProvider implements IPDFPageFeatureProvider {
 
     private ExtractorParameters parameters;
     private double horizontalEmptinessRadius;
     private double verticalEmptinessRadius;
     private double minimalFractionOfColumnSeparatorHeight;
     private double minimalFractionOfMarginWidth;
 
     public PageLayoutProvider() {
         this.parameters = ExtractorParameters.getExtractorParameters();
         this.horizontalEmptinessRadius = this.parameters.getHorizontalEmptinessRadius();
         this.verticalEmptinessRadius = this.parameters.getVerticalEmptinessRadius();
 
         this.minimalFractionOfColumnSeparatorHeight = this.parameters.getMinimalVerticalSeparatorHeight();
         this.minimalFractionOfMarginWidth = this.parameters.getMinimalMarginWidth(); // minimal height of an area separating columns
     }
 
     /**
     Useful for isolating unit tests from the rest of the environment
     
     her == getHorizontalEmptinessRadius();
     ver == getVerticalEmptinessRadius();
     
     mvsh == getMinimalVerticalSeparatorHeight();
     mmw == getMinimalMarginWidth(); // minimal height of an area separating columns
      * @param her
      * @param ver
      * @param mvsh
      * @param mmw
      */
     public PageLayoutProvider(double her, double ver, double mvsh, double mmw) {
         this.horizontalEmptinessRadius = her;
         this.verticalEmptinessRadius = ver;
 
         this.minimalFractionOfColumnSeparatorHeight = mvsh;
         this.minimalFractionOfMarginWidth = mmw;
     }
 
     /**
      * Determines if the pixel can be considered empty. This is the case if
      * its distance from being white is not high
      * @param px
      * @return
      */
     private static boolean isPixelEmpty(int[] px) {
         int res = 0;
         for (int i = 0; i < 3; ++i) {
             res += (255 - px[i]) * (255 - px[i]);
         }
         //return res == 0;
 
         return Math.sqrt(res) < 10;
     }
 
     /**
      * Returns an information if the area surrounding a given point is empty.
      * The surrounding is a horizontal interval having a given point in its center.
      * The length of the interval is determined in a configuration file.
      *
      * @param x
      * @param y
      * @param raster
      * @return
      */
     private boolean isAreaEmpty(int x, int y, Raster raster) {
         int[] currentPixel = new int[3];
         int r = (int) (this.horizontalEmptinessRadius * raster.getWidth());
         int minOffset = (int) (r / 2);
 
         for (int dx = 0; dx < r; ++dx) {
             currentPixel = raster.getPixel(x + dx - minOffset, y, currentPixel);
             if (!isPixelEmpty(currentPixel)) {
                 return false;
             }
         }
         return true;
     }
     // variables allowing a faster checks if area is empty
     // (no more reading of the same page pixels again!)
     private int[] emptinessColumn; // number of empty fields until current x (inclusive)
     private int emptinessX; // for which X is the data calculated
     private int[] emptinessCurrentPixel = new int[3];
     private int emptinessRadius;
     private int emptinessDiameter;
 
     /** Initialises the emptiness data - calculate the required emptiness radius
      */
     private void emptinessInitialise(Raster r) {
         emptinessDiameter = (int) (this.horizontalEmptinessRadius * r.getWidth());
         emptinessRadius = (int) emptinessDiameter / 2;
     }
 
     private void emptinessDataReset(Raster r) {
         emptinessX = -1;
         emptinessColumn = new int[r.getHeight()];
         for (int y = 0; y < r.getHeight(); ++y) {
             emptinessColumn[y] = r.getWidth();
         }
     }
 
     private void emptinessDataMove(Raster r) {
         // moves the emptiness index by 1
         emptinessX++;
         for (int y = 0; y < r.getHeight(); y++) {
             emptinessCurrentPixel = r.getPixel(emptinessX, y, emptinessCurrentPixel);
             if (isPixelEmpty(emptinessCurrentPixel)) {
                 emptinessColumn[y]++;
             } else {
                 emptinessColumn[y] = 0;
             }
         }
     }
 
     private boolean emptinessIsAreaEmpty(int x, int y, Raster raster) {
         int checkedX = x + emptinessRadius;
         while (emptinessX != checkedX) {
             emptinessDataMove(raster);
         }
         return emptinessColumn[y] >= emptinessDiameter;
     }
 
     /**
      * Determines if a given horizontal line might be considered a
      * horizontal separator
      *
      * @param x x coordinate of the left beginning of the line
      * @param y y coordinate if the left beginning of the line
      * @param width width of the line
      * @param raster raster on which we operate
      * @return
      */
     private boolean isHorizontalSeparator(int x, int y, int width, Raster raster) {
         int er = (int) (this.verticalEmptinessRadius * raster.getHeight());
         int minOffset = (int) er / 2;
         int[] currentPixel = new int[3];
 
         for (int dx = 0; dx < width; dx++) {
             for (int dy = 0; dy < er; dy++) {
                 if (x + dx >= 0 && x + dx < raster.getWidth() && y + dy - minOffset >= 0
                         && y + dy - minOffset < raster.getHeight()) {
                     currentPixel = raster.getPixel(x + dx, y + dy - minOffset, currentPixel);
 
                     if (!isPixelEmpty(currentPixel)) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     public List<Rectangle> getPageColumns(Raster raster) {
         LinkedList<Rectangle> separators = new LinkedList<Rectangle>();
         return getPageColumns(raster, separators);
     }
 
     /** Detects a layout of the page by searching high consistent blocks of
      *  empty space in the rendered version of the page. (a minimal width and
      *  height are set.
      *
      *  Each column is a rectangle
      *
      * @param pageManager
      * @return
      */
     public List<Rectangle> getPageColumns(Raster raster, List<Rectangle> verticalSeparators) {
         int minimalHeight = (int) (raster.getHeight() * this.minimalFractionOfColumnSeparatorHeight);
         //       int minimalWidth = (int) (raster.getWidth() * minimalFractionOfColumnSeparatorWidth);
         int minimalLeft = (int) (raster.getWidth() * this.minimalFractionOfMarginWidth);
         int maximalLeft = (int) (raster.getWidth() * (1 - this.minimalFractionOfMarginWidth));
 
 
         //ArrayList<ArrayList<Pair<Integer, Integer>>>  = new ArrayList<ArrayList<Pair<Integer, Integer>>>();
 
         ArrayList<Rectangle> columns = new ArrayList<Rectangle>();
         ArrayList<Rectangle> startedColumns = new ArrayList<Rectangle>(); // started columns. Width is invalid as we do not know them yet
         ArrayList<Integer> emptyAreas = new ArrayList<Integer>();  // empty areas in current column
         startedColumns.add(new Rectangle(0, 0, raster.getWidth(), raster.getHeight()));
 
 
         boolean inMaximisationMode = false; // the mode where we choose the biggest separator.
         int currentColumnEvaluation = 0;
 
         int xOfMaximum = 0;
         int maximalWeight = 0;
 
         ArrayList<Integer> maximalSeparators = new ArrayList<Integer>();
 
         emptinessInitialise(raster);
         emptinessDataReset(raster);
 
         for (int x = minimalLeft; x < maximalLeft; ++x) { // iterating over the columns
             // calculating signature of a line
             int emptyAreaBeginning = 0;
 
             emptyAreas.clear();
             currentColumnEvaluation = 0;
 
             for (int y = 0; y <= raster.getHeight(); ++y) {
                 if ((y == raster.getHeight()) || !emptinessIsAreaEmpty(x, y, raster)) {
                     if ((y - emptyAreaBeginning - 1) > minimalHeight) {
                         emptyAreas.add(emptyAreaBeginning);
 
                         emptyAreas.add((y == raster.getHeight()) ? y : (y - 1));
                         currentColumnEvaluation += y - emptyAreaBeginning;
                     }
                     emptyAreaBeginning = y + 1;
                 }
             }
 
             // we want to maintain the information of the longest separator line
             if (maximalWeight < currentColumnEvaluation) {
                 maximalWeight = currentColumnEvaluation;
                 maximalSeparators.clear();
                 maximalSeparators.addAll(emptyAreas);
                 xOfMaximum = x;
             }
 
             // now we have to look, what impact do our lines have on columns that have been begun
             // we have to find begun rectangles intersecting with the line.
 
             if (inMaximisationMode) {
                 if (emptyAreas.isEmpty()) {
                     // apply the maximal separator
                     startedColumns = updateCurrentColumns(columns,
                             maximalSeparators, startedColumns, xOfMaximum);
 
                     // now reseting maximal values
                     maximalWeight = 0;
                     inMaximisationMode = false;
 
 
                     // now updating the list of vertical separators
 
                     int pos = 0;
                     while (pos < maximalSeparators.size()) {
                         verticalSeparators.add(new Rectangle(xOfMaximum,
                                 maximalSeparators.get(pos), 0,
                                 maximalSeparators.get(pos + 1) - maximalSeparators.get(pos)));
                         pos += 2;
                     }
                 }
             } else {
                 if (!emptyAreas.isEmpty()) {
                     inMaximisationMode = true;
                 }
             }
         }
 
         // now closing all the unfinished columns -> equivalent to a separator going from the top to the bottom
         ArrayList<Integer> finalSeparator = new ArrayList<Integer>();
 
         finalSeparator.add(0);
         finalSeparator.add(raster.getHeight());
 
         updateCurrentColumns(columns, finalSeparator, startedColumns, raster.getWidth()); // we do not care
         // of the currently
         // open columns any more
 
         verticalSeparators.add(new Rectangle(0, 0, 0, raster.getHeight()));
         verticalSeparators.add(new Rectangle(raster.getWidth(), 0, 0, raster.getHeight()));
 
         return columns;
 
     }
 
     /**
      * Updates information about current columns and unfinished columns based on
      * the currently detected separators and on existing unfinished columns
      *
      * @param columns
      * @param separatorPoints
      * @param startedColumns
      * @param curX - x coordinate at which we are currently
      * @return
      */
     public ArrayList<Rectangle> updateCurrentColumns(ArrayList<Rectangle> columns, ArrayList<Integer> separatorPoints,
             ArrayList<Rectangle> startedColumns, int curX) {
 
         if (separatorPoints.isEmpty()) {
             return startedColumns;
         }
 
         ArrayList<Rectangle> newColumns = new ArrayList<Rectangle>();
         boolean onSeparator = false;
         int curPoint = separatorPoints.get(0);
         int curPointIndex = 0;
 
         Rectangle curRectangle = new Rectangle(curX, 0, 0, 0);
         // this is used to generate open recangles after the separator
 
 
         for (Rectangle curCol : startedColumns) {
             int curY = curCol.y; // this is used to close existing parts of rectangles
             while (curPoint >= curCol.y && curPoint <= curCol.y + curCol.height) {
                 if (onSeparator) { // the sparator is just ending
                     // we are at the separator... we have to split our current
                     // rectangle until the current point
                     /// adding height to a new rectangle
                     /// adding width to the rectangle being closed
 
                     curRectangle.setBounds(curRectangle.x, curRectangle.y, 0,
                             curPoint - curRectangle.y);
                     if (curRectangle.height > 0) {
                         newColumns.add(curRectangle);
                     }
 
                     if (curPoint > curY) {
                         columns.add(new Rectangle(curCol.x, curY, curX - curCol.x,
                                 curPoint - curY));
                     }
                 } else { // the separator is just beginning
                     if (curPoint > curY) {
                         newColumns.add(new Rectangle(curCol.x, curY, 0,
                                 curPoint - curY));
 
                         // a rectangle starting at the x of old one
                     }
                     curRectangle = new Rectangle(curX, curPoint, 0, 0);
                     // width and height are filled later
                 }
 
                 onSeparator = !onSeparator;
                 curY = curPoint;
                 curPointIndex++;
 
                 if (curPointIndex < separatorPoints.size()) {
                     curPoint = separatorPoints.get(curPointIndex);
                 } else {
                     // we are choosing a point from behind the page - it will
                     // never be processed
                     curPoint = (int) startedColumns.get(
                             startedColumns.size() - 1).getMaxY() + 10;
                 }
             }
 
             // do something with the remainder of this column
             if (onSeparator) {
                 // we have to split the remainder of the interval
                 if (curCol.y + curCol.height - curY > 0) {
                     columns.add(new Rectangle(curCol.x, curY, curX - curCol.x,
                             curCol.y + curCol.height - curY));
                 }
             } else {
                 // we have to add the remainder of the interval intact
                 curCol.setBounds(curCol.x, curY, curCol.width, curCol.height
                         + curCol.y - curY);
                 if (curCol.height > 0) {
                     newColumns.add(curCol);
                 }
             }
         }
         // current rectangle should not exist at this time
         return newColumns;
     }
 
     /**
      * 
      * this method adds separator to the data structure.
      * 
      * 
      * Sometimes we need to split existing horizontal separators in order to accomodate new
      * Such a situation has place when we add a shorter separator being part of a bigger one.
      * (From the point of view of 2 adjacent rectangles)
      * 
      * @param existingSeparators : a map Y coordinate -> Separator (as a rectangle) -> Pair of top and pottom areas adjacent to it
      * @param currentRectangle
      * @param upperArea - the identifier of the area above the current separator (-1 in the case of missing area
      * @param lowerArea - the identifier of the area below the current separator (-1 in the case of missing area
      */
     public void updateHorizontalSeparators(
             Map<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>> existingSeparators,
             Rectangle currentRectangle, Integer upperArea, Integer lowerArea) {
 
 
         if (!existingSeparators.containsKey(currentRectangle.y)) {
             existingSeparators.put(currentRectangle.y, new TreeMap<Rectangle, Pair<Integer, Integer>>(new Comparator<Rectangle>() {
 
                 @Override
                 public int compare(Rectangle t, Rectangle t1) {
                     return t.x - t1.x;
                 }
             }));
         }
 
         LinkedList<Rectangle> intersecting = new LinkedList<Rectangle>();
         TreeMap<Rectangle, Pair<Integer, Integer>> currentLine = existingSeparators.get(currentRectangle.y);
         // 1) get all the intersecting separators from the tree -> all between beginning and end of our interval
         // but also possibly one before the beginning if it intersects
 
         Rectangle previous = currentLine.floorKey(new Rectangle(
                 currentRectangle.x - 1, currentRectangle.y, 1, 0));
 
         if (previous != null && previous.x + previous.width > currentRectangle.x) {
             intersecting.addFirst(previous);
         }
 
         for (Rectangle r : currentLine.subMap(currentRectangle,
                 new Rectangle(currentRectangle.x + currentRectangle.width,
                 currentRectangle.y, 1, 0)).keySet()) {
             intersecting.addLast(r);
         }
 
         // we remove first and last intersecting interval and the rest remains the same
 
         if (intersecting.size() == 0) {
             // there are no intersecting separators -> simply add a new one
             currentLine.put(currentRectangle, new Pair<Integer, Integer>(upperArea, lowerArea));
             return;
         }
 
         // based on this, locate all the points of interest and remove corresponding rectangles
         // from the the data structrure describing a line
         // point of interest - a point where an interval begins or ends
 
         TreeMap<Integer, Pair<Integer, Integer>> points =
                 new TreeMap<Integer, Pair<Integer, Integer>>(); // queue of the points
 
 
         for (Rectangle r : intersecting) {
             // we rely on the fact that the iteration happens in increasing order
             points.put(r.x, currentLine.get(r));
             points.put(r.x + r.width, new Pair<Integer, Integer>(-1, -1));
             currentLine.remove(r);
         }
 
         if (!points.containsKey(currentRectangle.x)) {
             if (currentRectangle.x < points.firstKey()) {
                 points.put(currentRectangle.x, new Pair<Integer, Integer>(upperArea, lowerArea));
             } else {
                 // we are splitting an interval -> have to preserve part of the information
                 Pair<Integer, Integer> v = points.floorEntry(currentRectangle.x - 1).getValue();
                 points.put(currentRectangle.x, new Pair<Integer, Integer>(v.first, v.second));
             }
         }
 
         if (!points.containsKey(currentRectangle.x + currentRectangle.width)) {
             if (currentRectangle.x + currentRectangle.width > points.lastKey()) {
                 points.put(currentRectangle.x + currentRectangle.width, new Pair<Integer, Integer>(-1, -1));
             } else {
                 // we are splitting an interval -> have to preserve part of the information
                 Pair<Integer, Integer> v = points.floorEntry(currentRectangle.x + currentRectangle.width - 1).getValue();
                 points.put(currentRectangle.x + currentRectangle.width, new Pair<Integer, Integer>(v.first, v.second));
             }
         }
 
         int prevPoint = -1;
 
         // processing points of interest ! 
         for (Integer point : points.keySet()) {
             // ERROR TODO: We are relying on the iteration in increasing order !!! keySet might not assure this
             // adding the interval (prevPoint, currentPoint)
             if (prevPoint != -1) {
                 Rectangle sep = new Rectangle(prevPoint, currentRectangle.y, point - prevPoint, 0);
                 Pair<Integer, Integer> val = new Pair<Integer, Integer>(points.get(prevPoint).first, points.get(prevPoint).second);
 
                 if (currentRectangle.x <= prevPoint && currentRectangle.x + currentRectangle.width >= point) {
                     if (upperArea >= 0) {
                         val.first = upperArea;
                     }
 
                     if (lowerArea >= 0) {
                         val.second = lowerArea;
                     }
                 }
                 currentLine.put(sep, val);
             }
             prevPoint = point;
         }
     }
 
     /** connect two areas inside a graph
      * 
      * @param areasConnections
      * @param id1
      * @param id2 
      */
     private void connectAreas(TreeMap<Integer, LinkedList<Integer>> areasConnections, int id1, int id2) {
         if (!areasConnections.containsKey(id1)) {
             areasConnections.put(id1, new LinkedList<Integer>());
         }
         if (!areasConnections.containsKey(id2)) {
             areasConnections.put(id2, new LinkedList<Integer>());
         }
 
         areasConnections.get(id1).add(id2);
         areasConnections.get(id2).add(id1);
     }
 
     public void verify(ArrayList<Rectangle> layoutElements, HashMap<Rectangle, Integer> upperRectangles, HashMap<Rectangle, Integer> lowerRectangles, HashSet<Rectangle> horizontalSeparators) {
         for (Rectangle r1 : layoutElements) {
             for (Rectangle r2 : layoutElements) {
                 if (r1 != r2 && r1.intersects(r2)) {
                     System.out.println("we have a problem");
                 }
             }
         }
 
         for (Rectangle sep : horizontalSeparators) {
             if (!lowerRectangles.containsKey(sep)) {
                 System.out.println("we have a problem");
             }
             if (!upperRectangles.containsKey(sep)) {
                 System.out.println("we have a problem");
             }
         }
     }
     // Here comes the code for correcting the horizontal separators.
     // After the initial processing, vertical separators are correct but
     // horizontal separators might intersect some data points.
     // Those functions move horizontal boundaries in such a way that they
     // do not break in the middle of the data
 
     public PageLayout fixHorizontalSeparators(List<Rectangle> preliminaryAreas,
             List<Rectangle> verticalSeparators, Raster raster) {
 
         // a set of separators that do not have to be processed again!
         HashSet<Rectangle> bannedSeparators = new HashSet<Rectangle>();
 
         // declarations of data structures required by the algorithm:
 
 
         // the upper limit on the number of areas is fixed at this point.
         // We create an array whose indices will be area identifiers at the same
         // time
 
         ArrayList<Rectangle> layoutElements =
                 new ArrayList<Rectangle>(preliminaryAreas.size());
         int newElemId = 0;
 
 
         // mapping from separators to rectangles (identified by their Id's)
 
 //        HashMap<Rectangle, Integer> upperRectangles = new HashMap<Rectangle, Integer>();
 //        HashMap<Rectangle, Integer> lowerRectangles = new HashMap<Rectangle, Integer>();
 
         // mapping to the parent of a given area. Parent and a child belong to the same layout element
         TreeMap<Integer, LinkedList<Integer>> areasConnections = new TreeMap<Integer, LinkedList<Integer>>();
 
         // vertical separators touching those horizontal
         HashMap<Rectangle, List<Rectangle>> adjacentVSeparators = new HashMap<Rectangle, List<Rectangle>>();
 
         //
         //   Prefilling the initial algorithm data
         //
 
         // all the separators are represented as Rectangles having either width or height set to 0
         // 1) locate all the horizontal lines and sort them
 
 
 
         /**
          *  This variable remembers:
          *   y coordinate of the horizontal separator -> (rectangle (the separator ... sorted by initial x) -> pair of numbers (adjacent vertical separators)
          */
         HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>> hSeparators = new HashMap<Integer, TreeMap<Rectangle, Pair<Integer, Integer>>>();
 
 
         /** Assigning numbers to layout elements and mapping horizontal separators to adjacent areas*/
         for (Rectangle rectangle : preliminaryAreas) {
             // add upper and lower edges to the data structure
 
             int position = newElemId;
             newElemId++;
             layoutElements.add(position, rectangle);
 
             // now treating the borders
             Rectangle upperSeparator = new Rectangle(rectangle.x, rectangle.y,
                     rectangle.width, 0);
 
             Rectangle lowerSeparator = new Rectangle(rectangle.x,
                     rectangle.y + rectangle.height, rectangle.width, 0);
 
             updateHorizontalSeparators(hSeparators, lowerSeparator, position, -1);
             updateHorizontalSeparators(hSeparators, upperSeparator, -1, position);
         }
 
         // now building a uniform map of horizontal separators - not aggregated by their y coordinate
         HashSet<Rectangle> horizontalSeparators = new HashSet<Rectangle>();
         HashMap<Rectangle, Integer> upperRectangles = new HashMap<Rectangle, Integer>();
         HashMap<Rectangle, Integer> lowerRectangles = new HashMap<Rectangle, Integer>();
 
         //  verify(layoutElements, lowerRectangles, upperRectangles, horizontalSeparators);
 
         for (Integer y : hSeparators.keySet()) {
             for (Rectangle sep : hSeparators.get(y).keySet()) {
                 horizontalSeparators.add(sep);
                 upperRectangles.put(sep, hSeparators.get(y).get(sep).first);
                 lowerRectangles.put(sep, hSeparators.get(y).get(sep).second);
             }
         }
 
 
 
         // now detecting which vetical separators are adjacent to horizontal ones
         for (Rectangle hSeparator : horizontalSeparators) {
             LinkedList<Rectangle> adj = new LinkedList<Rectangle>();
             for (Rectangle vSeparator : verticalSeparators) { // TODO: this could be optimized in order to have a time better than quadratic
 
                 if ((hSeparator.x == vSeparator.x || hSeparator.x + hSeparator.width == vSeparator.x)
                         && (vSeparator.y <= hSeparator.y && vSeparator.y + vSeparator.height >= hSeparator.y)) {
                     adj.add(vSeparator);
                 }
 
             }
             adjacentVSeparators.put(hSeparator, adj);
         }
 
         //verify(layoutElements, lowerRectangles, upperRectangles, horizontalSeparators);
 
         //
         //    II) Moving horizontal lines
         //
 
         for (Rectangle hSeparator : horizontalSeparators) {
             int x = hSeparator.x;
             int y = hSeparator.y;
             int width = hSeparator.width;
 
             /** Cases :
             0) We already have a correct separator -> we do not have to do anything
             1) Moving down and removed part of the rectangle -> add new separator and split the area (correcting all the linkings)
             2) Moving down and removed the entire rectangle -> both upper and current areas are set to be children of the bottom one
             3) Moving up, like 1
             4) Moving up, like 2
              */
             if (!isHorizontalSeparator(x, y, width, raster) && !bannedSeparators.contains(new Rectangle(x, y, width, 0))) {
                 // we do not have a corerct horizontal separator -> it has to be moved. there is always only one correct movement direction
                 boolean moveDown = false;
                 Rectangle moveUsing = null;
                 int lowerId = lowerRectangles.get(hSeparator);
                 int upperId = upperRectangles.get(hSeparator);
 
                 if (lowerId >= 0 && layoutElements.get(lowerId).width == hSeparator.width) {
                     // we will be moving down
                     moveDown = true;
                     moveUsing = layoutElements.get(lowerId);
                 }
 
                 if (upperId >= 0 && layoutElements.get(upperId).width == hSeparator.width) {
                     // we will be moving up
                     moveDown = false;
                     moveUsing = layoutElements.get(upperId);
                 }
 
                 int separatorInc = moveDown ? 1 : -1;
                 Rectangle cSeparator = new Rectangle(hSeparator.x, hSeparator.y, hSeparator.width, 0);
                 boolean succededWithMove = false;
                 int movedBy = 0;
 
 
                 while (isValidSeparatorPositionV(cSeparator, moveUsing)
                         && !succededWithMove) {
                     // while we are still adjacent to the same vertical separators as the original one
                     cSeparator.y += separatorInc;
                     succededWithMove = isHorizontalSeparator(cSeparator.x, cSeparator.y, cSeparator.width, raster);
                     movedBy++;
                 }
                 // we have a separator of a limit of the movement -> in order to avoid potential empty space, we
                 // will move the separator as far as possible as long as it is a separator
 
 
                 while (isValidSeparatorPositionV(new Rectangle(cSeparator.x, cSeparator.y + separatorInc, cSeparator.width, 0),
                         moveUsing)
                         && isHorizontalSeparator(cSeparator.x, cSeparator.y + separatorInc,
                         cSeparator.width, raster) && cSeparator.y + separatorInc < raster.getHeight()) {
 
                     cSeparator.y += separatorInc;
                     movedBy++;
                 }
 
                 if (cSeparator.y == raster.getHeight() - 1) {
                     // if we are at the very bottom, we are interested in moving the separator one more
                     // to go outside of the page
                     ++cSeparator.y;
                 }
                 if (!succededWithMove) {
                     // we are in the first invalid position -> we have to go back by one
 
                     cSeparator.y -= separatorInc;
                 }
 
                 // retrieving the area that will be affected by the move
                 Integer shRectangleId = (moveDown ? lowerRectangles : upperRectangles).get(hSeparator);
                 //     Integer shRectangleId = moveDown ? hSeparators.get(hSeparator.y).get(hSeparator). : hSeparators.get(hSeparator.y).get(hSeparator).first;
                 Rectangle shRectangle = layoutElements.get(shRectangleId);
 
 
 
                 // We have treid to move as far as we could.... now we have to decide what to do next... divide areas, join ? 
 
                 if (shRectangle.y == cSeparator.y || shRectangle.y + shRectangle.height == cSeparator.y) {
                     // if we moved too far -> which means, the adjacent rectangle will be shrinked to zero
                     // what about the borders ?! in this case there will be no lower area !
 
                     if (!succededWithMove) {
                         // TODO: This became a bit unefficient after verifying one assumption ... intervals data structure should be replaced ... probably the oriinal one without flattening could be perfect
                         List<Integer> btmIds = new LinkedList<Integer>();
                         List<Integer> uppIds = new LinkedList<Integer>();
 
 
                         // retrieve all separators from cSeparator
                         List<Rectangle> seps = new LinkedList<Rectangle>();
 
                         int last = cSeparator.x;
                         while (last < cSeparator.x + cSeparator.width) {
                             for (Rectangle separator : horizontalSeparators) {
                                 if (separator.x == last && separator.y == cSeparator.y) {
                                     seps.add(separator);
                                     last = separator.x + separator.width;
                                 }
                             }
                         }
 
                         if (moveDown) {
                             uppIds.add(upperRectangles.get(hSeparator));
                             for (Rectangle s : seps) {
                                 btmIds.add(lowerRectangles.get(s));
                             }
                         } else {
                             btmIds.add(lowerRectangles.get(hSeparator));
                             for (Rectangle s : seps) {
                                 uppIds.add(upperRectangles.get(s));
                             }
                         }
 
                         //Integer btmRecId = (moveDown ? lowerRectangles.get(cSeparator) : lowerRectangles.get(hSeparator));
                         //Integer uppRecId = (moveDown ? upperRectangles.get(hSeparator) : upperRectangles.get(cSeparator));
 
                         for (int id1 : uppIds) {
 
                             connectAreas(areasConnections, id1, shRectangleId);
 
                         }
                         for (int id1 : btmIds) {
 
                             connectAreas(areasConnections, id1, shRectangleId);
 
                         }
 
                     } else {
                         // top/btm remains distinct -> only the shrinked rectangle gets attached to one of them
                         Integer btmRecId = (moveDown ? lowerRectangles.get(cSeparator) : lowerRectangles.get(hSeparator));
                         Integer uppRecId = (moveDown ? upperRectangles.get(hSeparator) : upperRectangles.get(cSeparator));
                         connectAreas(areasConnections, shRectangleId, moveDown ? uppRecId : btmRecId);
 
                     }
                     // we do not want to process this separator again... it will appear once again
                     bannedSeparators.add(cSeparator);
                 } else {
                     // we were sucessful with moving .. we are on a real separator !
                     if (moveDown) {
                         // create a new rectangle, attach it to the upper area and leave the bottom rectangle shrinked
                         if (shRectangle.x == 378) {
                             System.out.println("our suspicious rectangle");
                         }
 
                         Rectangle newShrinkedRectangle = new Rectangle(shRectangle.x, cSeparator.y, shRectangle.width, shRectangle.height - movedBy);
                         Rectangle newRectangle = new Rectangle(shRectangle.x, shRectangle.y, shRectangle.width, movedBy);
 
                         int newId = newElemId;
                         newElemId++;
                         layoutElements.add(newId, newRectangle);
                         layoutElements.set(shRectangleId, newShrinkedRectangle);
 
                         upperRectangles.put(cSeparator, newId);
                         lowerRectangles.put(cSeparator, shRectangleId);
 
                         // updating existing informaions !
 
                         lowerRectangles.put(hSeparator, newId);
 
                         // and now establishing the parental relation
                         connectAreas(areasConnections, newId, upperRectangles.get(hSeparator));
                         //  verify(layoutElements, lowerRectangles, upperRectangles, horizontalSeparators);
 
 
                     } else {
                         Rectangle newShrinkedRectangle = new Rectangle(shRectangle.x, shRectangle.y, shRectangle.width, shRectangle.height - movedBy);
                         Rectangle newRectangle = new Rectangle(shRectangle.x, cSeparator.y, shRectangle.width, movedBy);
                         int newId = newElemId;
                         newElemId++;
                         layoutElements.add(newId, newRectangle);
                         layoutElements.set(shRectangleId, newShrinkedRectangle);
 
                         upperRectangles.put(cSeparator, shRectangleId);
                         lowerRectangles.put(cSeparator, newId);
 
 //                        areaUpperSeparator.put(newId, cSeparator);
 //                        areaLowerSeparator.put(newId, hSeparator);
 
                         // updating existing informaions !
 
                         upperRectangles.put(hSeparator, newId);
                         //                       areaLowerSeparator.put(shRectangleId, cSeparator);
                         // and finally establishing the parental relation
 
                         connectAreas(areasConnections, newId, lowerRectangles.get(hSeparator));
                         //  verify(layoutElements, lowerRectangles, upperRectangles, horizontalSeparators);
 
 
                     }
                 }
             }
         }
 
 
         //
         //   III) Pass the entire graph searching for consistent parts ... perform as many DF'ses as necessary... 
         // all results of a single DFS go to a single cluster
         PageLayout pageLayout = new PageLayout();
         pageLayout.areas = new LinkedList<List<Rectangle>>();
         HashSet<Integer> usedRectangles = new HashSet<Integer>(); // already consumed rectangles
 
         for (int recNum = 0; recNum < layoutElements.size(); ++recNum) {
             if (!usedRectangles.contains(recNum)) {
                 // we have not used this rectangle... what a miracle, we can start graph search from it
                 LinkedList<Rectangle> consistentArea = new LinkedList<Rectangle>();
                 LinkedList<Integer> bfsQueue = new LinkedList<Integer>();
                 bfsQueue.add(recNum);
                 usedRectangles.add(recNum);
                 while (!bfsQueue.isEmpty()) {
                     int curRec = bfsQueue.getFirst();
                     bfsQueue.removeFirst();
                     consistentArea.add(layoutElements.get(curRec));
                     if (areasConnections.containsKey(curRec)) {
                         for (int adjArea : areasConnections.get(curRec)) {
                             if (!usedRectangles.contains(adjArea)) {
                                 bfsQueue.addLast(adjArea);
                                 usedRectangles.add(adjArea);
                             }
                         }
                     }
                 }
                 pageLayout.areas.add(consistentArea);
             }
         }
 
         return pageLayout;
     }
 
     private boolean canMoveSeparatorDown(Rectangle hSeparator, List<Rectangle> vSeparators) {
         for (Rectangle vSeparator : vSeparators) {
             if (hSeparator.y >= vSeparator.y + vSeparator.height) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Check if the new vertical placement of a separator is correct.
      * The horizontal placement is completely ignored.
      *
      * A position is considered correct if is within the y range of guiding rectangle (moveUsing)
      *
      * @param hSeparator
      * @param moveUsing
      * @return
      */
     private boolean isValidSeparatorPositionV(Rectangle hSeparator, Rectangle moveUsing) {
         return hSeparator.y >= moveUsing.y && hSeparator.y <= moveUsing.y + moveUsing.height;
     }
 
     /** 
      * 
      * Methods allowing to further refine the layout extraction after fixing
      * position of horizontal separators
      * 
      */
     public int calculateLayoutAreaWidth(List<Rectangle> area) {
         if (area.isEmpty()) {
             return 0;
         }
         TreeMap<Integer, List<Integer>> widths = new TreeMap<Integer, List<Integer>>();
         for (Rectangle rec : area) {
             if (!widths.containsKey(rec.y)){
                 widths.put(rec.y, new LinkedList<Integer>());
             }
             if (!widths.containsKey(rec.y + rec.height)){
                 widths.put(rec.y + rec.height, new LinkedList<Integer>());
             }
             
             widths.get(rec.y).add(rec.width);
             widths.get(rec.y + rec.height).add(-rec.width);
         }
         
         int minWidth = Integer.MAX_VALUE;
         int curWidth =0;
         for (Integer val: widths.navigableKeySet()){
             for (Integer delta: widths.get(val)){
                 curWidth += delta;
             }
             if (curWidth < minWidth && curWidth > 0){
                 minWidth = curWidth;
             }
         }
         return minWidth;
     }
 
     /**
      * Returns the number of an area that is too narrow
      * @return 
      */
     private int getTooNarrowArea(PageLayout layout, int pageWidth) {
         ExtractorParameters parameters = ExtractorParameters.getExtractorParameters();
         double width = parameters.getMinimalPageLayoutColumnWidth() * pageWidth;
         for (int area=0; area < layout.areas.size(); ++area){
             if (width > this.calculateLayoutAreaWidth(layout.areas.get(area))){
                 return area;
             }
         }
         return -1;
     }
 
     /** Process columns that are too small to be standalone.
      *  Join them with already existing columns.
      * 
      *  Column width = width in the narrowest point.
      *  Height = height in the lowest point
      * 
      * @param layout
      * @return 
      */
     public PageLayout unifyNarrowColumns(PageLayout layout,  Raster raster) {
         int tooNarrowArea = getTooNarrowArea(layout, raster.getWidth());
         while (tooNarrowArea != -1) {
             //connect with the most promising left area
             // electing the most promising candidate -> the one maximising the width
             int newWidth = 0;
             int maxArea = -1;
             HashSet<Integer> candidates = layout.getLeftAreas(tooNarrowArea);
             candidates.addAll(layout.getLeftAreas(tooNarrowArea));
             for (int adjArea: candidates){
                 int tmpWidth = getCombinedAreaWidth(layout, adjArea, tooNarrowArea);
                 if (tmpWidth > newWidth){
                     maxArea = adjArea;
                     newWidth = tmpWidth;
                 }
             }
             
             if (newWidth == 0){
                 // we are very sorry but it is impossible to improve the situation... maybe the conditions are too strict
                 System.err.println("The requirement of having columns wide enough can not be satisfied. too narrow area: " + tooNarrowArea + " area width " + this.calculateLayoutAreaWidth(layout.areas.get(tooNarrowArea)));
                 break;
             } else {
                 // combining with maxArea
                 layout.areas.get(tooNarrowArea).addAll(layout.areas.get(maxArea));
                 layout.areas.remove(maxArea);
             }
             tooNarrowArea = getTooNarrowArea(layout, raster.getWidth());
         }
         
         
         
         // processing too low areas
         // TODO: this should be implemented if necessary
         return layout;
     }
 
     private int getCombinedAreaWidth(PageLayout layout, int area1, int area2) {
         LinkedList<Rectangle> newCandidate = new LinkedList<Rectangle>();
         for (Rectangle rec: layout.areas.get(area1)){
             newCandidate.add(rec);
         }
         for (Rectangle rec: layout.areas.get(area2)){
             newCandidate.add(rec);
         }
         return this.calculateLayoutAreaWidth(newCandidate);
     }
     // a general interface of the provider
 
     @Override
     public <T> IPDFPageFeature calculateFeature(PDFPageManager<T> pageManager)
             throws FeatureNotPresentException, Exception {
 
         Raster raster = pageManager.getRenderedPage().getData();
         LinkedList<Rectangle> verticalSeparators = new LinkedList<Rectangle>();
         List<Rectangle> preliminaryColumns = getPageColumns(raster, verticalSeparators);
         PageLayout layout = fixHorizontalSeparators(preliminaryColumns, verticalSeparators, raster);
         layout = unifyNarrowColumns(layout, raster);
         return layout;
     }
 
     @Override
     public String getProvidedFeatureName() {
         return PageLayout.featureName;
     }
 }
