 package splinetester;
 
 import flanagan.interpolation.CubicSplineFast;
 
 
 /**
  *
  * @author zapu
  */
 public class MatlabInterpolationTest
 {
     private CubicSplineFast spliner = null;
     private double[] x = null;
     private double[] y = null;
 
     private double[] testX = null;
     private double[] results = null;
 
     public MatlabInterpolationTest()
     {
         
     }
 
     public MatlabInterpolationTest(double x[], MathExpression expr, double points[])
     {
         setPoints(x, expr);
         setTestPoints(points);
     }
 
     public MatlabInterpolationTest(double x[], double[] y, double points[])
     {
         setPoints(x, y);
         setTestPoints(points);
     }
 
     public final void setPoints(double x[], double y[])
     {
         this.x = x;
         this.y = y;
         spliner = new CubicSplineFast(x, y);
     }
 
     public final void setPoints(double x[], MathExpression expr)
     {
         double y[] = new double[x.length];
         for(int i = 0; i < x.length; i++)
             y[i] = expr.y(x[i]);
 
         setPoints(x, y);
     }
 
     public final void setTestPoints(double points[])
     {
         testX = points;
     }
 
     private void interpolate()
     {
         results = new double[testX.length];
         for(int i = 0; i < testX.length; i++)
         {
             results[i] = spliner.interpolate(testX[i]);
         }
     }
 
     public static String testScript =
         "matlabY = spline(knownX, knownY, intX);\r\n" +
         "diff = intY - matlabY;\r\n" +
        "disp(['Largest error: ' num2str(max(diff))]);\r\n" +
         "for i = 2:length(intX)\r\n" +
         "    diffInt = intY(i) - intY(i-1);\r\n" +
         "    diffMat = matlabY(i) - matlabY(i-1);\r\n" +
         "    if(sign(diffInt) ~= sign(diffMat))\r\n" +
         "        disp(['Monotony fail: ' num2str(i) ' int: ' num2str(diffInt) ' matlab: ' num2str(diffMat)])\r\n" +
         "    end\r\n" +
         "end";
 
     public String generateM()
     {
         interpolate();
 
         String script = "clear; \r\n";
         script += MatlabHelper.array("knownX", x) + "\r\n";
         script += MatlabHelper.array("knownY", y) + "\r\n";
 
         script += MatlabHelper.array("intX", testX) + "\r\n";
         script += MatlabHelper.array("intY", results) + "\r\n";
 
         script += testScript;
 
         return script;
     }
 }
