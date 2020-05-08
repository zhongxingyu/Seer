  import java.awt.Color;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import data.MdfData;
 import draw.*;
 
 
 public class driver {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 * @throws SizeException 
 	 */
 	public static void main(String[] args) throws Exception {
 		
 //		ShapeFile s = ShapeFile.Import("Florida_State_Borders.tqSHP", Format.mshp);
 //		
 //		WxImage i = new WxImage(520,720);
 //		
 //		Draw.DrawShape(s, i, 15, 15);
 //		
 //		Draw.drawWindBarb(400, 400, i, 40, 239);
 //		
 //		i.Print("test");
 		
 		
 		//******************************************//
 //		Projection proj = new MercatorProjection();
 //		proj.setProjectionLatitudeDegrees(28.0908);
 //		proj.setProjectionLongitudeDegrees(-81.9604);
 //		proj.initialize();
 //		
 //		double y =  28.0908;
 //		double x = -80.9604;
 //		
 //		y = y *.0174532925;
 //		x = x *.0174532925;
 //		
 //		java.awt.geom.Point2D.Double dst = new java.awt.geom.Point2D.Double();
 //		
 //		proj.project(x, y, dst);
 //	
 //		System.out.println("x:" + x + ", y:" + y);
 //		System.out.println(dst);
 //		
 //		proj.transformRadians(dst, dst);
 //		
 //		System.out.println(dst);
 		
 		Point2D.Double cent = new Point2D.Double(-83.911193,28.06);
 		WxImage image = new WxImage(720,520,BufferedImage.TYPE_INT_RGB, cent);
 		
 		Point2D.Double[] points = new Point2D.Double[22];
 		points[0] = new Point2D.Double(-80.6, 25.185);
 		points[1] = new Point2D.Double(-80.2, 27.1276);
 		points[2] = new Point2D.Double(-81.46, 30.685);
 		points[3] = new Point2D.Double(-81.98, 30.798);
 		points[4] = new Point2D.Double(-82.01, 30.52);
 		points[5] = new Point2D.Double(-84.874878, 30.7);
 		points[6] = new Point2D.Double(-85, 30.9776);
 		points[7] = new Point2D.Double(-87.6, 30.99);
 		points[8] = new Point2D.Double(-87.47, 30.36);
 		points[9] = new Point2D.Double(-85.34, 29.741);
 		points[10] = new Point2D.Double(-84.02, 30.0976);
 		points[11] = new Point2D.Double(-83.01, 29.181);
 		points[12] = new Point2D.Double(-82.81, 29.15);
 		points[13] = new Point2D.Double(-82.64, 28.676);
 		points[14] = new Point2D.Double(-82.85, 27.89);
 		points[15] = new Point2D.Double(-82.678, 27.7);
 		points[16] = new Point2D.Double(-82.6, 27.98);
 		points[17] = new Point2D.Double(-82.5, 27.834);
 		points[18] = new Point2D.Double(-82.38, 27.91);
 		points[19] = new Point2D.Double(-82.672, 27.489);
 		points[20] = new Point2D.Double(-81.72, 25.998);
 		points[21] = new Point2D.Double(-80.99, 25.145);
 		
 		//System.out.println(points[15]);
 		points = image.drawShape(points);
 
 		//for(int i = 0; i < points.length; i++){
 		//	System.out.println(points[i]);
 		//}
 		System.out.println(image.projToDisplayRatio);
 		//image.drawVector(100, 50, 270, 28.06, -83.911193, Color.white);
 		image.drawWindBarb(78, 280, 28.06, -83.911193, Color.white);
 		image.testPrint("testmap1");
 		
 		data.Data testing = new MdfData("test.mdf", "siteinfo.xml");
 		testing.importData();
 		
		System.out.println(testing.getDatum("ALTU", "TAIR"));
		
 		
 		
 		
 		
 		
 		
 
 	}
 
 }
