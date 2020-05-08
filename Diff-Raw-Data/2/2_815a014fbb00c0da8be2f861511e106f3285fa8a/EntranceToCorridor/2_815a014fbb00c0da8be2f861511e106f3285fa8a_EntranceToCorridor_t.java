 package net.binaryvibrance.undergrounddomes.generation;
 
 import java.util.logging.Logger;
 
 import net.binaryvibrance.undergrounddomes.generation.maths.Line;
 import net.binaryvibrance.undergrounddomes.generation.maths.Point3D;
 import net.binaryvibrance.undergrounddomes.generation.maths.Vector3;
 import net.binaryvibrance.undergrounddomes.helpers.LogHelper;
 import net.minecraft.util.Vec3;
 
 public class EntranceToCorridor {
 	private static final Logger LOG = LogHelper.getLogger();
 
 	public final Line lineToCorridor;
 	public final Line lineToOrigin;
 	public final Point3D entranceLocation;
 	public final Point3D destinationLocation;
 	private boolean applied = false;
 	private SphereEntrance entrance;
 
 	public EntranceToCorridor() {
 		Point3D corridorStart = new Point3D(0, 0, 0);
 		Point3D originStart = new Point3D(0, 0, 0);
 		Point3D join = new Point3D(0, 0, 0);
 
 		lineToCorridor = new Line(corridorStart, join);
 		lineToOrigin = new Line(join, originStart);
 		entranceLocation = new Point3D(0, 0, 0);
 		destinationLocation = originStart;
 	}
 
 	public void markApplied() {
 		applied = true;
 		entrance.corridorPath = this;
 		Vec3 renderVector = lineToCorridor.getRenderVector();
 		Vec3 extensionVector = Vector3.multiply(renderVector, -1);
		lineToCorridor.start.move(extensionVector);
 	}
 
 	public boolean isApplied() {
 		return applied;
 	}
 
 	public void setAdjustmentVector(Vec3 vector) {
 		entranceLocation.set(lineToCorridor.start);
 		entranceLocation.move(vector);
 	}
 
 	public void setEntrance(SphereEntrance entrance) {
 		this.entrance = entrance;
 	}
 
 	public void setNewEndpoint(Point3D newEndpoint) {
 		Vec3 sphereVector = lineToCorridor.getRenderVector();
 		Vec3 originVector = lineToOrigin.getRenderVector();
 
 		double x = 0;
 		double z = 0;
 
 		// LOG.info(String.format("Creating Line %d/%d %s", currentLine++,
 		// maxLines, path.toString()));
 
 		if (originVector.equals(Vector3.NORTH) || originVector.equals(Vector3.SOUTH)) {
 			z = newEndpoint.zCoord;
 			x = lineToCorridor.start.xCoord;
 
 		} else {
 			x = newEndpoint.xCoord;
 			z = lineToCorridor.start.zCoord;
 		}
 		LOG.info(String.format("Moving Line %s", lineToOrigin));
 		LOG.info(String.format("Previous Render Vector: %s", originVector));
 		lineToOrigin.start.set(x, lineToOrigin.end.y, z);
 		lineToOrigin.end.set(newEndpoint);
 		LOG.info(String.format("To Line %s", lineToOrigin));
 		LOG.info(String.format("new Render Vector: %s", lineToOrigin.getRenderVector()));
 		// TODO: This needs an implementation.
 		// What this needs to do is recalculate the join location
 	}
 }
