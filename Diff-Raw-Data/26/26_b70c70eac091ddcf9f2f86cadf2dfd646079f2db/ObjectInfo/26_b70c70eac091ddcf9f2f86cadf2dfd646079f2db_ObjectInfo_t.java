 package kinect.kinect;
 
 import april.vis.*;
 import april.jmat.*;
 import april.util.UnionFindSimple;
 
 import kinect.lcmtypes.*;
 import lcm.lcm.*;
 
 import java.io.*;
 import java.nio.*;
 import javax.swing.*;
 import java.awt.*;
 import java.util.*;
 import java.awt.image.*;
 
 
 public class ObjectInfo{
 
     public double numPoints;
     public int color;
     public int repID;
     public int ufsID;
     public double[] sumPoints;
     public int[] sumColor;
     public double leftmost;
     public double rightmost;
     public double uppermost;
     public double lowermost;
     public  BufferedImage image = null;
     
     public Rectangle projBBox = null;
 
     public String colorFeatures;
     public String shapeFeatures;
     public String sizeFeatures;
     public ArrayList<double[]> points;
 
     public ObjectInfo(){
     }
 
     /** Create a new object with info about it. Objects begin with a single point.**/
     public ObjectInfo(int color, int repID, double[] point)
     {
         Random r = new Random();
         this.repID = r.nextInt();
         this.numPoints = 1;
         this.color = color;
         this.ufsID = repID;
         // XXXX - Need to transform everything with respect to the ground
         // plane and the kinect
         this.leftmost = point[0];
         this.rightmost = point[0];
         this.uppermost = point[1];
         this.lowermost = point[1];
 
         sumPoints = new double[]{point[0], point[1], point[2]};
         Color c = new Color((int) point[3]);
         sumColor = new int[]{c.getRed(), c.getBlue(), c.getGreen()};
 
         this.points = new ArrayList<double[]>();
         this.points.add(point);
     }
 
     /** Add a new point to this object. **/
     public void update(double[] point){
         if(this.leftmost > point[0])
             this.leftmost = point[0];
         if(this.rightmost < point[0])
             this.rightmost = point[0];
         if(this.uppermost > point[1])
             this.uppermost = point[1];
         if(this.lowermost < point[1])
             this.lowermost = point[1];
 
         this.numPoints ++;
         for(int i=0; i<sumPoints.length; i++){
             sumPoints[i] += point[i];
         }
         Color c = new Color((int)point[3]);
         sumColor[0] += c.getRed();
         sumColor[1] += c.getBlue();
         sumColor[2] += c.getGreen();
 
         this.points.add(point);
     }
 
     /** Get the center of the object (mean x, y,z). **/
     public double[] getCenter()
     {
         double[] center = new double[sumPoints.length];
         for(int i=0; i<sumPoints.length; i++){
             center[i] = sumPoints[i]/numPoints;
         }
         
         return center;
     }
 
     /** Get the average color of the object as an array [r, g, b]. **/
     public double[] avgColor()
     {
         double[] avg = new double[sumColor.length];
         for(int i=0; i<avg.length; i++){
             avg[i] = sumColor[i]/numPoints;
         }
         return avg;
     }
 
     /** Given a hashmap of objects, find the one that is most similar to this
         object. The most similar one will be the object that has the closest
         center and with a mean color that is within a threshold of this object.**/
     // XXX - probably want to take complete feature vector into account, not only colors
     public int mostSimilar(HashMap<Integer, ObjectInfo> objects)
     {
         int best = -1;
         double minDist = 100000;
         double minColorDist = 30;
 
         Collection c = objects.values();
         for(Iterator itr = c.iterator(); itr.hasNext(); ){
             ObjectInfo obj2 = (ObjectInfo)itr.next();
             double centerDist = LinAlg.distance(getCenter(), obj2.getCenter());
             double colorDist = LinAlg.distance(avgColor(), obj2.avgColor());
             if (centerDist < minDist && colorDist < minColorDist){
                 minDist = centerDist;
                 best = obj2.ufsID;
             }
         }
         return best;
     }
 
     /** Say this object is the same as a past one by giving it the old object's
         ID. Eventually this method might allow us to store the old object in the
         history of this object? **/
     public void equateObject(int newID, int newColor)
     {
         repID = newID;
         color = newColor;
     }
     
     public static BufferedImage getImage(ArrayList<double[]> points, Rectangle projBBox){
     	BufferedImage image;
 		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
 		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
 		for(double[] pt : points){
 			double[] pixel = KUtils.getPixel(pt);
 			minX = (pixel[0] < minX ? (int)Math.round(pixel[0]) : minX);
 			maxX = (pixel[0] > maxX ? (int)Math.round(pixel[0]) : maxX);
 			minY = (pixel[1] < minY ? (int)Math.round(pixel[1]) : minY);
 			maxY = (pixel[1] > maxY ? (int)Math.round(pixel[1]) : maxY);
 		}
 		int margin = 5;
 		if(projBBox != null){
 			projBBox.setBounds(minX - margin, minY - margin, maxX - minX + 1 + margin*2, maxY - minY + 1 + margin*2);
 		}
 		image = new BufferedImage((maxX - minX + 1) + 2*margin, (maxY - minY + 1) + 2*margin, BufferedImage.TYPE_3BYTE_BGR);
 		for(int i = 0; i < points.size(); i++){
 			double[] pixel = KUtils.getPixel(points.get(i));
 			try{
 				Color c =  new Color((int)points.get(i)[3]);
 				Color rc = new Color(c.getBlue(), c.getGreen(), c.getRed());
     			image.setRGB((int)Math.round(pixel[0])+margin-minX, (int)Math.round(pixel[1])+margin-minY, rc.getRGB());
 			} catch (Exception e){
				//System.out.println("Out of Bounds pixel in ObjectInfo: " + pixel[0] + ", " + pixel[1]);
				//System.out.println(points.get(i)[0] + ", " + points.get(i)[1] + ", " + points.get(i)[2]);
 			}
 		}
     	return image;
     }
     
     public Rectangle getProjectedBBox(){
     	if(projBBox == null){
     		getImage();
     	} 
     	return projBBox;
     }
     
     public BufferedImage getImage(){
     	if(image == null){
     		projBBox = new Rectangle();
     		image = getImage(points, projBBox);
     	} 
     	return image;
     }
 }
