 package eu.imagine.framework;
 
 import org.opencv.calib3d.Calib3d;
 import org.opencv.core.*;
 import org.opencv.imgproc.Imgproc;
 
 import java.util.ArrayList;
 
 /**
  * Class encapsulates the main work method, allowing a simpler view from
  * outside.
  */
 class Detector {
 
     private final MainInterface mainInterface;
     private Messenger log;
     private final String TAG = "Detector";
 
     // FLAGS (compositeFrameOut order!)
     protected static boolean USE_CANNY = false;
     protected static boolean USE_ADAPTIVE = false;
     protected static boolean DEBUG_PREP_FRAME = false;
     protected static boolean DEBUG_CONTOURS = false;
     protected static boolean DEBUG_POLY = false;
     protected static boolean DEBUG_DRAW_MARKERS = false;
     protected static boolean DEBUG_DRAW_MARKER_ID = false;
     protected static boolean DEBUG_DRAW_SAMPLING = false;
 
     // Important numbers, shouldn't be changed at runtime!
     private final int MARKER_GRID = 6;
     private final int MARKER_SQUARE = 5;
     private final int MARKER_SIZE = MARKER_GRID * MARKER_SQUARE;
     private int BINARY_THRESHOLD;
     private final int SAMPLE_THRESHOLD = (MARKER_SQUARE * MARKER_SQUARE) / 2;
     private final int step = MARKER_SIZE / MARKER_GRID;
     private final int half = step / 2;
 
     private Mat camMatrix;
     private MatOfDouble distCoeff;
 
     // Important reused vars
     private Mat out, rotMat, Rvec, Tvec;
     private Mat compositeFrameOut;
     private ArrayList<MatOfPoint> contours, contoursAll;
     private ArrayList<Marker> markerCandidates;
     private MatOfPoint2f result, standardMarker;
     private MatOfPoint3f objectPoints;
 
     // Colors
     private final double[] WHITE = new double[]{255, 255, 255, 255};
     private final double[] BLACK = new double[]{0, 0, 0, 255};
 
     /**
      * Constructor of Detector.
      *
      * @param mainInterface The MainInterface instance; used for getting
      *                      values.
      */
     protected Detector(MainInterface mainInterface) {
         this.log = Messenger.getInstance();
         this.mainInterface = mainInterface;
         this.out = new Mat();
         this.rotMat = new Mat();
         this.Rvec = new Mat();
         this.Tvec = new Mat();
         this.compositeFrameOut = new Mat();
         this.contours = new ArrayList<MatOfPoint>();
         this.contoursAll = new ArrayList<MatOfPoint>();
         this.markerCandidates = new ArrayList<Marker>();
         this.result = new MatOfPoint2f();
         // Prepare standard marker:
         this.standardMarker = new MatOfPoint2f();
         this.standardMarker.fromArray(new Point(0, MARKER_SIZE),
                 new Point(0, 0),
                 new Point(MARKER_SIZE, 0), new Point(MARKER_SIZE, MARKER_SIZE));
         // Prepare standard 3d object points:
         this.objectPoints = new MatOfPoint3f();
         this.objectPoints.fromArray(new Point3(1f, -1f, 0f),
                 new Point3(-1f, -1f, 0f), new Point3(-1f, 1f, 0f),
                 new Point3(1f, 1f, 0f));
         // Set calibration things:
         ConvertHelper CONVERT = ConvertHelper.getInstance();
         this.camMatrix = CONVERT.float2ToMatFloat(mainInterface.camMatrix);
         this.distCoeff = CONVERT.float1ToMatDouble(mainInterface.distCoef);
         this.BINARY_THRESHOLD = mainInterface.threshold;
         // Correctly set flags
         this.updateFlags();
     }
 
     /**
      * Main method where OpenCV detects the markers.
      *
      * @param gray Grayscale mat.
      * @param rgba RGBA mat.
      * @return The rgba mat with any debugging stuff drawn on it.
      */
     protected Mat detect(Mat gray, Mat rgba) {
 
         if (MainInterface.DEBUG_FRAME_LOGGING)
             log.pushTimer(this, "frame");
         contours.clear();
         contoursAll.clear();
         markerCandidates = new ArrayList<Marker>();
 
         if (USE_CANNY) {
             // best
             Imgproc.Canny(gray, out, BINARY_THRESHOLD - 50, BINARY_THRESHOLD + 50);
         } else if (USE_ADAPTIVE) {
             // speed: ~88ms
             Imgproc.adaptiveThreshold(gray, out, 255,
                     Imgproc.ADAPTIVE_THRESH_MEAN_C,
                     Imgproc.THRESH_BINARY, 7, 7);
         } else {
             // Standard
             // Speed: ~8ms
             Imgproc.threshold(gray, out, BINARY_THRESHOLD, 255, Imgproc.THRESH_BINARY);
         }
 
         if (DEBUG_PREP_FRAME)
             return out;
 
         // Speed: ~22ms
         Imgproc.findContours(out, contoursAll, new Mat(),
                 Imgproc.RETR_LIST,
                 Imgproc.CHAIN_APPROX_NONE);
 
         // Remove too small contours:
         // Speed: ~0ms
         for (MatOfPoint contour : contoursAll) {
             if (contour.total() <= 300)
                 continue;
             contours.add(contour);
         }
 
         compositeFrameOut = rgba;
 
         // DEBUG_LOGGING
         if (DEBUG_CONTOURS) {
             Imgproc.drawContours(compositeFrameOut, contours, -1, new Scalar(255, 0, 0), 2);
             return compositeFrameOut;
         }
 
         // Do candidate processing:
         // Speed: ~100ms for one marker (+- 50ms)
         for (MatOfPoint contour : contours) {
             MatOfPoint2f input = new MatOfPoint2f(contour.toArray());
             // speed: ~2ms
             Imgproc.approxPolyDP(input, result, input.total() * 0.10,
                     true);
             // Only take contours with ==4 points
             // speed: ~1ms
             if (result.total() != 4 || !Imgproc.isContourConvex(new
                     MatOfPoint(result.toArray()))) {
                 continue;
             }
             // Calculate perspective transform
             // speed: ~0ms
             Mat tempPerspective = Imgproc.getPerspectiveTransform(result,
                     standardMarker);
             // Apply to get marker texture
             // speed: ~12ms
             Imgproc.warpPerspective(rgba, out, tempPerspective,
                     new Size(MARKER_SIZE, MARKER_SIZE));
             // Check if marker
             // speed: ~9ms (range: 5ms to 30ms!)
             Marker mark = isMarker(result, out);
             if (mark == null)
                 continue;
             // Save area
             int area = (int) Imgproc.contourArea(contour);
             mark.setArea(area);
             // Save marker candidate
             markerCandidates.add(mark);
         }
 
         // Remove double markers that arise from using Canny or adaptive
         // threshold (inside & outside contour are found,
         // we remove inside to keep only one contour)
         ArrayList<Marker> toRemove = new ArrayList<Marker>();
         for (int i = 0; i < markerCandidates.size(); i++) {
             Marker mark = markerCandidates.get(i);
             for (int j = i + 1; j < markerCandidates.size(); j++) {
                 Marker two = markerCandidates.get(j);
                 if (!markersClose(mark, two))
                     continue;
                 if (mark.getArea() > two.getArea())
                     toRemove.add(two);
                 else
                     toRemove.add(mark);
             }
         }
         for (Marker mark : toRemove)
             markerCandidates.remove(mark);
 
         // Now calculate the perspective transform for all markers:
         // Speed: ~2ms per detected marker
         for (Marker marker : markerCandidates) {
             Calib3d.solvePnP(objectPoints, marker.getCorners(), camMatrix,
                     distCoeff, Rvec, Tvec);
             // Now to convert from OpenCV to OpenGL coordinates:
             // Convert rotation:
             Rvec.put(0, 1, Rvec.get(1, 0)[0] * -1.0f);
             Rvec.put(0, 2, Rvec.get(2, 0)[0] * -1.0f);
             // Calculate rotation matrix:
             Calib3d.Rodrigues(Rvec, rotMat);
             // Build OpenGL ready matrix:
             float[] translation = new float[]{
                     (float) rotMat.get(0, 0)[0], (float) rotMat.get(1, 0)[0],
                     (float) rotMat.get(2, 0)[0], 0.0f,
                     (float) rotMat.get(0, 1)[0], (float) rotMat.get(1, 1)[0],
                     (float) rotMat.get(2, 1)[0], 0.0f,
                     (float) rotMat.get(0, 2)[0], (float) rotMat.get(1, 2)[0],
                     (float) rotMat.get(2, 2)[0], 0.0f,
                    (float) Tvec.get(0, 0)[0] + 0.2f, -(float) Tvec.get(1, 0)[0],
                     -(float) Tvec.get(2, 0)[0], 1.0f
             };
 
             marker.setRotTranslation(translation);
         }
 
         if (DEBUG_POLY) {
             ArrayList<MatOfPoint> temp = new ArrayList<MatOfPoint>();
             for (Marker mark : markerCandidates) temp.add(mark.getMOPCorners());
             Core.polylines(compositeFrameOut, temp, true, new Scalar(255, 0, 0), 2);
         }
 
         if ((DEBUG_DRAW_MARKERS || DEBUG_DRAW_MARKER_ID) && !markerCandidates
                 .isEmpty()) {
             int RENDER_SCALE = 3;
             if (DEBUG_DRAW_MARKERS) {
                 int count = 0;
                 for (Marker mark : markerCandidates) {
                     Mat tempText = mark.grayTexture;
                     // Might, but shouldn't be null
                     if (tempText == null) {
                         log.debug(TAG, "DEBUG_DRAW_MARKERS: Texture NULL!");
                         continue;
                     }
                     int xoffset = RENDER_SCALE * MARKER_SIZE * count;
                     count++;
                     for (int i = 0; i < RENDER_SCALE * MARKER_SIZE; i++)
                         for (int j = 0; j < RENDER_SCALE * MARKER_SIZE; j++) {
                             compositeFrameOut.put(i, xoffset + j,
                                     tempText.get(i / RENDER_SCALE,
                                             j / RENDER_SCALE));
                         }
                 }
             }
             if (DEBUG_DRAW_MARKER_ID) {
                 int count = 0;
                 for (Marker mark : markerCandidates) {
                     boolean[][] tempBool = mark.getPattern();
                     // Might, but shouldn't happen
                     if (tempBool == null) {
                         log.debug(TAG, "DEBUG_DRAW_MARKER_ID: Pattern NULL!");
                         continue;
                     }
                     int yoffset = RENDER_SCALE * MARKER_SIZE *
                             (DEBUG_DRAW_MARKERS ? 1 : 0);
                     int xoffset = RENDER_SCALE * MARKER_SIZE * count;
                     count++;
                     for (int i = 0; i < RENDER_SCALE * MARKER_SIZE; i++)
                         for (int j = 0; j < RENDER_SCALE * MARKER_SIZE; j++) {
                             int x = i / MARKER_SQUARE / RENDER_SCALE,
                                     y = j / MARKER_SQUARE / RENDER_SCALE;
                             if (x == 0 || y == 0 || x == MARKER_GRID - 1 || y ==
                                     MARKER_GRID - 1)
                                 compositeFrameOut.put(yoffset + i, xoffset + j,
                                         BLACK);
                             else
                                 compositeFrameOut.put(yoffset + i, xoffset + j,
                                         tempBool[x - 1][y - 1] ?
                                                 WHITE : BLACK);
                         }
                 }
             }
         }
 
         if (MainInterface.DEBUG_FRAME_LOGGING) {
             TimerResult timer = log.popTimer(this);
             log.debug(TAG, "Detected " + markerCandidates.size() + " markers " +
                     "in " + timer.time + "ms.");
         }
 
         // Pass detected markers up
         mainInterface.updateList(markerCandidates);
 
         // Return frame (only used in frame debugging mode)
         return compositeFrameOut;
     }
 
     /**
      * Checks if two markers have all corners close to each other.
      *
      * @param one Marker one.
      * @param two Marker two.
      * @return True if close.
      */
     private boolean markersClose(Marker one, Marker two) {
         if (one.getID() == two.getID()) {
             if (one.getArea() - two.getArea() < 20 && one.getArea() - two
                     .getArea
                             () > -20) {
                 log.debug(TAG, "Removing double marker! ID: " + one.getID());
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Tries to read the texture to detect a marker.
      *
      * @param result  Points to write into marker, if detected.
      * @param texture The texture to check.
      * @return Marker object if marker was detected, else null.
      */
     private Marker isMarker(MatOfPoint2f result, Mat texture) {
         boolean[][] pattern = new boolean[4][4];
 
         // reset error allowance
         int errorAllowance = 0;
         // Check border:
         for (int i = 1; i < MARKER_GRID - 1; i++) {
             if (testSample(half + (i * step), half,
                     texture) > 0)
                 errorAllowance++;
             if (testSample(half, half + (i * step), texture) > 0)
                 errorAllowance++;
             if (testSample(half + (i * step), MARKER_SIZE - 1 - half,
                     texture) > 0)
                 errorAllowance++;
             if (testSample(MARKER_SIZE - 1 - half, half + (i * step),
                     texture) > 0)
                 errorAllowance++;
         }
 
         // we'll allow 4 incorrect border pieces but no more
         if (errorAllowance > 4) {
             log.debug(TAG, "Discarding over incomplete border detection!");
             // Return null if we're not debugging – we're done in that case.
            if (!(DEBUG_DRAW_MARKER_ID || DEBUG_DRAW_MARKERS)) {
                 return null;
            }
         }
         // Now read pattern:
         // time: ~3ms
         for (int i = 0; i < 4; i++)
             for (int j = 0; j < 4; j++) {
                 pattern[i][j] = (testSample(half + (i + 1) * step,
                         half + (j + 1) * step, texture) > 0);
             }
 
         // Check corners, get rotation, and rotate pattern to correct
         // orientation:
         // time: ~0ms
         int angle;
         if (!pattern[0][0] && pattern[0][3] && pattern[3][0] && pattern[3][3]) {
             angle = -90;
             pattern = rotatePattern(3, pattern);
         } else if (pattern[0][0] && !pattern[0][3] &&
                pattern[3][0] && pattern[3][3]) {
             angle = 0;
         } else if (pattern[0][0] && pattern[0][3] &&
                 !pattern[3][0] && pattern[3][3]) {
             angle = 180;
             pattern = rotatePattern(2, pattern);
         } else if (pattern[0][0] && pattern[0][3] &&
                 pattern[3][0] && !pattern[3][3]) {
             angle = 90;
             pattern = rotatePattern(1, pattern);
         } else {
             // This happens when a black border is found but has no
             // orientation information.
             log.debug(TAG, "Discarding over missing orientation information!");
             angle = -1;
            if (!(DEBUG_DRAW_MARKER_ID || DEBUG_DRAW_MARKERS)) {
                 return null;
            }
         }
 
         // Get Hamming code corrected id:
         // time: ~0ms
         int id = MarkerPatternHelper.getID(pattern);
 
         // Check if id is valid.
         if (id < 0) {
             log.debug(TAG, "Discarding over illegal ID!");
             return null;
         }
 
         Marker marker = new Marker(result, angle, id);
 
         // For debug, we need to remember the grayTexture, otherwise not.
         if (mainInterface.DEBUG_FRAME && (DEBUG_DRAW_MARKERS || DEBUG_POLY)) {
             // INFO: Debugging with this HALFS the framerate!
             marker.setDebugParameters(pattern, texture);
             return marker;
         }
 
         return marker;
     }
 
     /**
      * @param x       X-Coordinate.
      * @param y       Y-Coordinate.
      * @param texture Reference to grayTexture.
      * @return Negative for black, positive for white
      */
     private int testSample(int x, int y, Mat texture) {
         int countBlack = 0;
         for (int i = -MARKER_SQUARE / 2; i <= MARKER_SQUARE / 2; i++)
             for (int j = -MARKER_SQUARE / 2; j <= MARKER_SQUARE / 2; j++) {
                 double[] value = texture.get(x + i, y + j);
                 int mean = (int) ((value[0] + value[1] + value[2]) / 3);
                 if (mean < BINARY_THRESHOLD)
                     countBlack++;
             }
         if (countBlack > SAMPLE_THRESHOLD) {
             if (DEBUG_DRAW_SAMPLING)
                 texture.put(x, y, WHITE);
             return -1;
         } else {
             if (DEBUG_DRAW_SAMPLING)
                 texture.put(x, y, BLACK);
             return 1;
         }
     }
 
     /**
      * Helper function for rotating the pattern correctly.
      *
      * @param steps   Set to number of clockwise steps to rotate.
      * @param pattern The pattern to rotate.
      * @return The rotated pattern.
      */
     private boolean[][] rotatePattern(int steps, boolean[][] pattern) {
         if (steps < 1)
             return pattern;
         boolean[][] rotated = new boolean[4][4];
         if (steps == 1) {
             for (int i = 0; i < pattern.length; i++)
                 for (int j = 0; j < pattern[i].length; j++)
                     rotated[i][j] = pattern[j][pattern.length - 1 - i];
         } else if (steps == 2) {
             for (int i = 0; i < pattern.length; i++)
                 for (int j = 0; j < pattern[i].length; j++)
                     rotated[i][j] = pattern[pattern.length - 1 - i][pattern
                             .length - 1 - j];
         } else {
             for (int i = 0; i < pattern.length; i++)
                 for (int j = 0; j < pattern[i].length; j++)
                     rotated[i][j] = pattern[pattern.length - 1 - j][i];
         }
         return rotated;
     }
 
     /**
      * Method sets flags corresponding to whether they will have any effect.
      * Basically, if DEBUG_FRAME from MainInterface is not set to true,
      * all the flags will automatically be set to false.
      */
     protected void updateFlags() {
         USE_CANNY = USE_CANNY && mainInterface.DEBUG_FRAME;
         DEBUG_PREP_FRAME = DEBUG_PREP_FRAME && mainInterface.DEBUG_FRAME;
         DEBUG_CONTOURS = DEBUG_CONTOURS && mainInterface.DEBUG_FRAME;
         DEBUG_POLY = DEBUG_POLY && mainInterface.DEBUG_FRAME;
         DEBUG_DRAW_MARKERS = DEBUG_DRAW_MARKERS && mainInterface.DEBUG_FRAME;
         DEBUG_DRAW_MARKER_ID = DEBUG_DRAW_MARKER_ID && mainInterface.DEBUG_FRAME;
         DEBUG_DRAW_SAMPLING = DEBUG_DRAW_SAMPLING && mainInterface.DEBUG_FRAME;
     }
 }
