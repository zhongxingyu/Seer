 package net.chiisana.builddit.generator;
 
 import net.chiisana.builddit.model.PlotConfiguration;
 
 /**
  * Created with IntelliJ IDEA.
  * User: andy
  * Date: 2013-02-10
  * Time: 5:01 PM
  */
 public class PlotHelper {
 	/*
 	boolean isRoad(int cxx, int czz, int lx, int lz)
 		input:  cxx - chunk x << 4
 				czz - chunk z << 4
 				lx - local x
 				lz - local z
 		output:
 				returns true if tile is supposed to be road
 				returns false for all other tiles
 	*/
 	public static boolean isRoad(int cxx, int czz, int lx, int lz) {
 		int plotSize = PlotConfiguration.intPlotSize + PlotConfiguration.intRoadWidth + 2;  // +2 for wall
 		if  (
				((modulus((cxx+lx), plotSize) < plotSize - 1) && (modulus((cxx+lx), plotSize) > plotSize - 2 - PlotConfiguration.intRoadWidth))
 				||
				((modulus((czz+lz), plotSize) < plotSize - 1) && (modulus((czz+lz), plotSize) > plotSize - 2 - PlotConfiguration.intRoadWidth))
 			)
 		{
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	boolean isWall(int cxx, int czz, int lx, int lz)
 		input:  cxx - chunk x << 4
 				czz - chunk z << 4
 				lx - local x
 				lz - local z
 		output:
 				returns true if tile is supposed to be wall
 				returns false for all other tiles
 	*/
 	public static boolean isWall(int cxx, int czz, int lx, int lz) {
 		int plotSize = PlotConfiguration.intPlotSize + PlotConfiguration.intRoadWidth + 2;  // +2 for wall
 		if  (
				((modulus((cxx+lx), plotSize) == plotSize - 1) || (modulus((cxx+lx), plotSize) == plotSize - 2 - PlotConfiguration.intRoadWidth))
 				||
				((modulus((czz+lz), plotSize) == plotSize - 1) || (modulus((czz+lz), plotSize) == plotSize - 2 - PlotConfiguration.intRoadWidth))
 			)
 		{
 			return true;
 		}
 		return false;
 	}
 
 	public static int modulus(int n, int m) {
 		return (n < 0) ? (m - (StrictMath.abs(n) % m) ) % m : (n % m);
 	}
 }
