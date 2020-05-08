 /**
  *
  */
 package ca.eandb.jmist.framework.gi2;
 
 import ca.eandb.jmist.framework.Lens;
 import ca.eandb.jmist.framework.Light;
 import ca.eandb.jmist.framework.Random;
 import ca.eandb.jmist.math.Point2;
 
 /**
  * @author Brad
  *
  */
 public final class PathTracingStrategy implements BidiPathStrategy {
 
 	private final int maxDepth;
 
 	public PathTracingStrategy(int maxDepth) {
 		this.maxDepth = maxDepth;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.gi2.BidiPathStrategy#getWeight(ca.eandb.jmist.framework.gi2.PathNode, ca.eandb.jmist.framework.gi2.PathNode)
 	 */
 	public double getWeight(PathNode lightNode, PathNode eyeNode) {
		if (eyeNode == null) {
			return 0.0;
		}
 		if (lightNode == null) {
 			int eyeDepthDiffuse = eyeNode.getDepth();
 			while (eyeNode.getDepth() > 0) {
 				if (eyeNode.isSpecular()) {
 					eyeDepthDiffuse--;
 				}
 				eyeNode = eyeNode.getParent();
 			}
 			return eyeDepthDiffuse == 1 ? 1.0 : 0.0;
 		}
		return lightNode.getDepth() == 0 && eyeNode.getDepth() > 0 ? 1.0 : 0.0;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.gi2.BidiPathStrategy#traceEyePath(ca.eandb.jmist.framework.Lens, ca.eandb.jmist.math.Point2, ca.eandb.jmist.framework.gi2.PathInfo, ca.eandb.jmist.framework.Random)
 	 */
 	public PathNode traceEyePath(Lens lens, Point2 p, PathInfo pathInfo,
 			Random rnd) {
 		PathNode head = lens.sample(p, pathInfo, rnd);
 		return PathUtil.expand(head, maxDepth - 1, rnd);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.gi2.BidiPathStrategy#traceLightPath(ca.eandb.jmist.framework.Light, ca.eandb.jmist.framework.gi2.PathInfo, ca.eandb.jmist.framework.Random)
 	 */
 	public PathNode traceLightPath(Light light, PathInfo pathInfo, Random rnd) {
 		return light.sample(pathInfo, rnd);
 	}
 
 }
