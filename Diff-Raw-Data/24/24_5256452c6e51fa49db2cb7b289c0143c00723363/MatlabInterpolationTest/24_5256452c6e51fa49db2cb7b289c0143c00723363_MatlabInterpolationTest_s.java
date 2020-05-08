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
 
     public void setPoints(double x[], double y[])
     {
         this.x = x;
         this.y = y;
         spliner = new CubicSplineFast(x, y);
     }
 
     public void setPoints(double x[], MathExpression expr)
     {
         double y[] = new double[x.length];
         for(int i = 0; i < x.length; i++)
             y[i] = expr.y(x[i]);
 
         setPoints(x, y);
     }
 
     public void setTestPoints(double points[])
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
 
     public String generateM()
     {
         interpolate();
 
         String script = "clear; \r\n";
         script += MatlabHelper.array("knownX", x) + "\r\n";
         script += MatlabHelper.array("knownY", y) + "\r\n";
 
         script += MatlabHelper.array("intX", testX) + "\r\n";
         script += MatlabHelper.array("intY", results) + "\r\n";
 
        script += "matlabY = spline(knownX, knownY, intX);\r\n";

        script += "diff = intY - matlabY;\r\n";

        script += "plot(intX, diff);\r\n";

        /*script += "plot(knownX, knownY);\r\n"
                + "hold on;\r\n"
                + "plot(intX, intY);\r\n"
                + "plot(intX, matlabY);\r\n";*/
 
         return script;
     }
 }
