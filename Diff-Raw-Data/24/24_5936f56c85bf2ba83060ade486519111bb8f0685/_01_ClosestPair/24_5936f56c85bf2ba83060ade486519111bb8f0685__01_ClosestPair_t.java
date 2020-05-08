 public class _01_ClosestPair
 {
  private static void usageError()
  {
     System.err.println("Usage: <number of points to generate>");
     System.exit(1);
  }
   private static void printPair(Point2D a, Point2D b)
   {
     StdOut.printf("%s to %s\nDistance: %f\n", a, b, a.distanceTo(b));
   }
   private static Point2D[] generatePoints(int n)
   {
     Point2D[] points = new Point2D[n];
 
     StdRandom.setSeed(System.currentTimeMillis());
 
     for (int i = 0; i < n; i++)
       points[i] = new Point2D(StdRandom.random(), StdRandom.random());
     return points;
   }
 
   private static Point2D[] computeClosestPair(Point2D[] points)
   {
     Point2D[] pair = new Point2D[2];
     double closest = 2.1;  // should be greater than the diagonal, i.e. MAX
     double d;
 
     for (int a = 0; a < points.length - 1; a++)
       for (int b = a + 1; b < points.length; b++) {
         printPair(points[a], points[b]);
         if ((d = points[a].distanceTo(points[b])) < closest) {
           closest = d;
           pair[0] = points[a];
           pair[1] = points[b];
         }
       }
 
     return pair;
   }
 
  public static void main(String[] args)
  {
    if (args.length != 1)
      usageError();
 
    int N = Integer.parseInt(args[0]);
    Point2D[] closest = computeClosestPair(generatePoints(N));
 
    StdOut.println("--------------------------------------------------");
    printPair(closest[0], closest[1]);
 
   }
 }
