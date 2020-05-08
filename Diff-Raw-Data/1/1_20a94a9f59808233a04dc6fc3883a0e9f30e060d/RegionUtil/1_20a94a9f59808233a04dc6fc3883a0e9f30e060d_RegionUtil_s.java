 package yy.nlsde.buaa.groupmobility;
 
 import java.util.List;
 
 public class RegionUtil {
 	
 	public static void main(String[] args){
 		RegionBean rb=new RegionBean();
 		rb.addPoint(0, 0);
 		rb.addPoint(1, 0);
 		rb.addPoint(1, 1);
 		rb.addPoint(0, 1);
 		System.out.println(RegionUtil.getRegionCenter(rb));
 	}
 	
 	/*********************************************************************************************/
 	/**
 	 * 求区域中心点
 	 * @param rcb 
 	 * 
 	 * @param p
 	 * @param r
 	 * @return
 	 */
 	public static PointBean getRegionCenter(RegionBean rb){
		//TODO:
 		List<PointBean> points=rb.points;
 		double sumx=0;
 		double sumy=0;
 		for (PointBean p:points){
 			sumx+=p.getLon();
 			sumy+=p.getLat();
 		}
 		int size=points.size();
 		
 		return new PointBean(sumx/size,sumy/size);
 	}
 	/*********************************************************************************************/
 	
 	/*********************************************************************************************/
 	/**
 	 * 点在区域内
 	 * 
 	 * @param p
 	 * @param r
 	 * @return
 	 */
 	public static boolean pointInRegion(PointBean p, RegionBean r) {
 		if (point2RegionDistence(p,r)<=0)
 			return true;
 		List<PointBean> edge=r.points;
 		int polySides = edge.size();
 		double[] polyY = new double[polySides];
 		double[] polyX = new double[polySides];
 		for (int i = 0; i < polySides; i++) {
 			polyY[i] = edge.get(i).lat;
 			polyX[i] = edge.get(i).lon;
 		}
 		return pointInPolygon(polySides, polyY, polyX, p.getLat(), p.getLon());
 	}
 
 	private static boolean pointInPolygon(int polySides, double polyY[],
 			double polyX[], double x, double y) {
 		int i;
 		boolean oddNodes = false;
 		for (i = 0; i < polySides - 1; i++) {
 			if (polyY[i] < y && polyY[i + 1] >= y) {
 				if (polyY[i] * polyX[i + 1] + polyY[i + 1] * x + y * polyX[i] <= polyX[i + 1]
 						* y + polyY[i + 1] * polyX[i] + polyY[i] * x) {
 					oddNodes = !oddNodes;
 				}
 			}
 			if (polyY[i] > y && polyY[i + 1] <= y) {
 				if (polyY[i] * polyX[i + 1] + polyY[i + 1] * x + y * polyX[i] >= polyX[i + 1]
 						* y + polyY[i + 1] * polyX[i] + polyY[i] * x) {
 					oddNodes = !oddNodes;
 				}
 			}
 		}
 		return oddNodes;
 	}
 
 	/*********************************************************************************************/
 	
 	/*********************************************************************************************/
 	/**
 	 * 点到区域距离
 	 * 
 	 * @param p
 	 * @param r
 	 * @return
 	 */
 	public static double point2RegionDistence(PointBean p, RegionBean r) {
 		double mind=-1;
 		for (PointBean dp:r.points){
 			if (mind<0){
 				mind=distence(dp,p);
 			}else{
 				double cd=distence(dp,p);
 				if (cd<mind){
 					mind=cd;
 				}
 			}
 		}
 		return mind>0?mind:0;
 	}
 	public static double distence(PointBean dp,PointBean p){
 		return distence(dp.lon,dp.lat,p.lon,p.lat);
 	}
 
 	private static double distence(double x1, double y1, double x2, double y2) {
 		return Math.sqrt(Math.pow(
 				111000 * Math.abs(x1 - x2)
 						* Math.cos((y1 + y2) / 2 * Math.PI / 180), 2)
 				+ Math.pow(111000 * Math.abs(y1 - y2), 2));
 	}
 
 	/*********************************************************************************************/
 }
