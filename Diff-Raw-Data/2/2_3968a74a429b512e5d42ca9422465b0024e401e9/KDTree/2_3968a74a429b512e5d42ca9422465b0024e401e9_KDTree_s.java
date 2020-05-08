 import java.io.*;
 import java.util.*;
 import java.util.zip.DataFormatException;
 
 
 public class KDTree implements Serializable {
 
     private final int MAX_NODE_SIZE;
     private final int MAX_DIMENSIONALITY;
     private final int MAX_POINTS_NUMBER;
 
     private final Node root;
     private final List<Point> data;
 
     private List<Point> readData(String fileName) throws IOException, DataFormatException {
         List<Point> data = new ArrayList<Point>();
         BufferedReader input = new BufferedReader(new FileReader(fileName));
         try {
             String line = input.readLine();
             Integer prevLength = null;
             int currentPointNumber = 0;
             int inputDimensionality = line.split("\t").length;
             int currentDimensionality = Math.min(inputDimensionality, MAX_DIMENSIONALITY);
             while (line != null && currentPointNumber < MAX_POINTS_NUMBER) {
                 ++currentPointNumber;
                 String[] asStrings = line.split("\t");
                 float[] asFloats = new float[currentDimensionality];
                 for (int i = 0; i < asFloats.length; ++i) {
                     asFloats[i] = Float.parseFloat(asStrings[i]);
                 }
                 if (null != prevLength && asFloats.length != prevLength) {
                     throw new DataFormatException("Dimensions of points are not same!");
                 }
                 data.add(new Point(asFloats));
                 prevLength = asFloats.length;
                 assert null != prevLength;
                 line = input.readLine();
             }
         } finally {
             input.close();
         }
         return data;
     }
 
     public KDTree(int maxNodeSize, int maxDimensionality, int maxPointsNumber, String inputFileName) throws IOException, DataFormatException {
         MAX_NODE_SIZE = maxNodeSize;
         MAX_DIMENSIONALITY = maxDimensionality;
         MAX_POINTS_NUMBER = maxPointsNumber;
         data = readData(inputFileName);
         root = new Node(data);
     }
 
     public List<Point> getNearestK(int k) {
         return root.getNearestK(data.get(new Random().nextInt(data.size())), k);
     }
 
     public void saveToFile(String fileName) throws IOException {
         ObjectOutputStream oos = null;
         try {
             oos = new ObjectOutputStream(new FileOutputStream(fileName));
             oos.writeObject(this);
         } finally {
             if (null != oos) {
                 oos.close();
             }
         }
     }
 
     public static KDTree readFromFile(String filename) throws IOException, ClassNotFoundException {
         ObjectInputStream ois = null;
         try {
             ois = new ObjectInputStream(new FileInputStream(filename));
             return (KDTree) ois.readObject();
         } finally {
             if (null != ois) {
                 ois.close();
             }
         }
     }
 
     private class Node implements Serializable {
         private final Node left;
         private final Node right;
 
         private final List<Point> data;
 
         private final DimensionRange[] ranges;
         private final Integer widestDimension;
 
         public Node(List<Point> data) {
             this.data = new ArrayList<Point>(data);
             ranges = computeRanges();
            if (data.size() > MAX_NODE_SIZE) {
                 widestDimension = findWidestDimension();
                 Collections.sort(data, new Comparator<Point>() {
                     @Override
                     public int compare(Point point1, Point point2) {
                         return Float.compare(point1.get(widestDimension), point2.get(widestDimension));
                     }
                 });
                 int medianIndex = data.size() / 2;
                 while (data.get(medianIndex).get(widestDimension) >= data.get(medianIndex + 1).get(widestDimension) && medianIndex < data.size() - 2) {
                     ++medianIndex;
                 }
                 left = new Node(data.subList(0, medianIndex + 1));
                 right = new Node(data.subList(medianIndex + 1, data.size()));
             } else {
                 left = right = null;
                 widestDimension = null;
             }
         }
 
         private DimensionRange[] computeRanges() {
             final int dimensionality = data.get(0).getDimensionality();
             DimensionRange[] ranges = new DimensionRange[dimensionality];
             for (int i = 0; i < ranges.length; ++i) {
                 ranges[i] = new DimensionRange();
             }
             for (Point point : data) {
                 for (int i = 0; i < dimensionality; ++i) {
                     ranges[i].update(point.get(i));
                 }
             }
             return ranges;
         }
 
         private int findWidestDimension() {
             int widestDimensionNumber = 0;
             for (int i = 0; i < ranges.length; ++i) {
                 if (ranges[i].getWideness() > ranges[widestDimensionNumber].getWideness()) {
                     widestDimensionNumber = i;
                 }
             }
             return widestDimensionNumber;
         }
 
         private void sortByDistace(List<Point> points, final Point target) {
             Collections.sort(points, new Comparator<Point>() {
                 @Override
                 public int compare(Point point1, Point point2) {
                     int result = 0;
                     double dist1 = point1.getDist(target);
                     double dist2 = point2.getDist(target);
                     return Double.compare(dist1, dist2);
                 }
             });
         }
 
         public List<Point> getNearestK(Point target, int k) {
             List<Point> result;
             if (null == left && null == right) {
                 //Node is leaf!
                 result = new ArrayList<Point>(data);
             } else {
                 //Node isn't leaf!
                 assert left != null;
                 if (target.get(widestDimension) < data.get(data.size() / 2).get(widestDimension)) {
                     result = left.getNearestK(target, k);
                     double farthestPointDist = result.get(result.size() - 1).getDist(target);
                     if (right.ranges[widestDimension].lower - target.get(widestDimension) < farthestPointDist || result.size() < k) {
                         result.addAll(right.getNearestK(target, k));
                     }
                 } else {
                     result = right.getNearestK(target, k);
                     double farthestPointDist = result.get(result.size() - 1).getDist(target);
                     if (target.get(widestDimension) - left.ranges[widestDimension].upper < farthestPointDist || result.size() < k) {
                         result.addAll(left.getNearestK(target, k));
                     }
                 }
             }
             sortByDistace(result, target);
             return result.subList(0, Math.min(k, result.size()));
         }
 
         private class DimensionRange implements Serializable {
             private float lower;
             private float upper;
             private boolean initialized = false;
 
             public void update(float value) {
                 if (!initialized) {
                     lower = value;
                     upper = value;
                     initialized = true;
                 } else {
                     lower = Math.min(lower, value);
                     upper = Math.max(upper, value);
                 }
             }
 
             public double getWideness() {
                 return (double) upper - (double) lower;
             }
         }
     }
 
     public class Point implements Serializable {
         private float[] impl;
 
         public Point(float[] floatList) {
             impl = floatList;
         }
 
         public double getDist(Point point) {
             double sumOfSquares = 0;
             for (int i = 0; i < impl.length; ++i) {
                 sumOfSquares += Math.pow((double) impl[i] - (double) point.impl[i], 2);
             }
             return Math.sqrt(sumOfSquares);
         }
 
         public float get(int dimensionIndex) {
             return impl[dimensionIndex];
         }
 
         public int getDimensionality() {
             return impl.length;
         }
 
         @Override
         public String toString() {
             return "Point{" +
                     Arrays.toString(impl) +
                     '}';
         }
     }
 }
