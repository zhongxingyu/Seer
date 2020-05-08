 package stackiter.sim;
 
 import static stackiter.sim.Util.*;
 
 import java.awt.*;
 import java.awt.Shape;
 import java.awt.geom.*;
 import java.util.*;
 import java.util.List;
 
 import org.jbox2d.collision.*;
 import org.jbox2d.collision.shapes.*;
 import org.jbox2d.common.*;
 import org.jbox2d.dynamics.*;
 import org.jbox2d.dynamics.joints.*;
 
 public class Block implements Item {
 
 	private boolean alive;
 
 	private double angularAcceleration;
 
 	private boolean angularAccelerationCrossedZero;
 
 	/**
 	 * Bogus just for copies.
 	 */
 	private double angularVelocity = 0;
 
 	private Body body;
 
 	private BodyDef bodyDef;
 
 	private Color color;
 
 	private boolean debugPaint;
 
 	private List<Block> fixations = new ArrayList<Block>();
 
 	private Point2D linearAcceleration = point();
 
 	private boolean linearAccelerationCrossedZero;
 
 	private Point2D linearJerk = point();
 
 	// TODO Do I care? Seems like accel is good enough?
 	// private boolean linearJerkCrossedZero;
 
 	/**
 	 * Bogus just for copies.
 	 */
 	private Point2D linearVelocity = point();
 
 	private MouseJoint mainJoint;
 
 	private PolygonDef shapeDef;
 
 	private Soul soul = new Soul();
 
 	public Block() {
 		// Placeholders.
 		bodyDef = new BodyDef();
 		shapeDef = new PolygonDef();
 		// Some default values.
 		shapeDef.setAsBox(1, 1);
 		shapeDef.density = 1;
 		shapeDef.restitution = 0.25f;
 		shapeDef.friction = 0.5f;
 	}
 
 	private MouseJoint addDragJoint(Point2D point, double forceScale) {
 		MouseJointDef jointDef = new MouseJointDef();
 		jointDef.body1 = body.getWorld().getGroundBody();
 		jointDef.body2 = body;
 		jointDef.dampingRatio = (float)(Math.pow(body.getMass(), 0.5) / 2);
 		jointDef.frequencyHz = 2;
 		jointDef.target.set((float)point.getX(), (float)point.getY());
 		// Constant force, but 5 is _near_ the top range of our masses.
 		jointDef.maxForce = (float)(forceScale * 5); // * body.getMass());
 		return (MouseJoint)body.getWorld().createJoint(jointDef);
 	}
 
 	private void addFixConstraints(Block other) {
 		if (body != null && other.body != null) {
 			// TODO Verify the joints aren't already added.
 			// TODO Then add the joints.
 			body.getJointList();
 		}
 	}
 
 	public void addTo(World world) {
 		setAlive(true);
 		body = world.getDynamicsWorld().createBody(bodyDef);
 		body.createShape(shapeDef);
 		body.setMassFromShapes();
 		for (Block fixed: fixations) {
 			addFixConstraints(fixed);
 		}
 	}
 
 	public void affix(Block other) {
 		fixations.add(other);
 		other.fixations.add(this);
 		addFixConstraints(other);
 	}
 
 	@Override
 	public Block clone() {
 		Block copied = new Block();
 		copied.alive = alive;
 		copied.angularAcceleration = getAngularAcceleration();
 		copied.angularVelocity = getAngularVelocity();
 		copied.color = color;
 		copied.linearAcceleration.setLocation(getLinearAcceleration());
 		copied.linearVelocity.setLocation(getLinearVelocity());
 		copied.soul = soul;
 		copied.setAngle(getAngle());
 		copied.setPosition(getPosition());
 		copied.setExtent(getExtent());
 		return copied;
 	}
 
 	@Override
 	public boolean contains(Point2D point) {
 		Shape shape = transformedShape();
 		return shape.contains(point);
 	}
 
 	/**
 	 * @return angle in rats (radians / pi).
 	 */
 	@Override
 	public double getAngle() {
 		if (body == null) {
 			return bodyDef.angle / Math.PI;
 		} else {
 			XForm xForm = body.getXForm();
 			double angle = -Math.signum(xForm.R.col2.x) * Math.acos(xForm.R.col1.x);
 			return angle / Math.PI;
 		}
 	}
 
 	@Override
 	public double getAngularAcceleration() {
 		return angularAcceleration;
 	}
 
 	/**
 	 * In rats/second (where rats = rads/pi).
 	 */
 	@Override
 	public double getAngularVelocity() {
 		if (body == null) {
 			return angularVelocity;
 		} else {
 			return body.m_angularVelocity / Math.PI;
 		}
 	}
 
 	@Override
 	public Rectangle2D getBounds() {
 		Point2D extent = getExtent();
 		Rectangle2D rectangle = new Rectangle2D.Double(-extent.getX(), -extent.getY(), 2 * extent.getX(), 2 * extent.getY());
 		return rectangle;
 	}
 
 	@Override
 	public Color getColor() {
 		return color;
 	}
 
 	@Override
 	public Point2D getExtent() {
 		// TODO Just store our own shape info instead of this dual mess.
 		double width;
 		double height;
 		if (body == null) {
 			double minX = Double.POSITIVE_INFINITY;
 			double minY = Double.POSITIVE_INFINITY;
 			double maxX = Double.NEGATIVE_INFINITY;
 			double maxY = Double.NEGATIVE_INFINITY;
 			for (Vec2 point: shapeDef.getVertexArray()) {
 				minX = Math.min(point.x, minX);
 				minY = Math.min(point.y, minY);
 				maxX = Math.max(point.x, maxX);
 				maxY = Math.max(point.y, maxY);
 			}
 			width = maxX - minX;
 			height = maxY - minY;
 		} else {
 			XForm xForm = new XForm();
 			xForm.setIdentity();
 			AABB aabb = new AABB();
 			body.getShapeList().computeAABB(aabb, xForm);
 			width = aabb.upperBound.x - aabb.lowerBound.x;
 			height = aabb.upperBound.y - aabb.lowerBound.y;
 		}
 		return point(width / 2, height / 2);
 	}
 
 	/**
 	 * Grasp position relative to the object, or null if there is no grasp.
 	 *
 	 * TODO If allow multiple grasp, parameterize this.
 	 */
 	public Point2D getGraspPosition() {
 		if (!isGrasped()) {
 			return null;
 		}
 		Vec2 anchor = mainJoint.m_localAnchor;
 		return point(anchor.x, anchor.y);
 	}
 
 	public Point2D getLinearAcceleration() {
 		return linearAcceleration;
 	}
 
 	public Point2D getLinearJerk() {
 		return linearJerk;
 	}
 
 	@Override
 	public Point2D getLinearVelocity() {
 		if (body == null) {
 			return linearVelocity;
 		} else {
 			return scaled(World.TIME_SCALE, point(body.m_linearVelocity.x, body.m_linearVelocity.y));
 		}
 	}
 
 	@Override
 	public Point2D getPosition() {
 		Vec2 position;
 		if (body == null) {
 			position = bodyDef.position;
 		} else {
 			position = body.getXForm().position;
 		}
 		return point(position.x, position.y);
 	}
 
 	@Override
 	public Soul getSoul() {
 		return soul;
 	}
 
 	public AffineTransform getTransform() {
 		return getTransform(null);
 	}
 
 	private AffineTransform getTransform(AffineTransform transform) {
 		if (transform == null) {
 			transform = new AffineTransform();
 		} else {
 			transform.setToIdentity();
 		}
 		worldToBlockTransform(transform);
 		return transform;
 	}
 
 	public void grasp(Point2D point) {
 		// Assume horizontal grasps, so find if X or Y local axis is horizontal.
 		// TODO Also bias for thinner sides? Best for crazy shapes would be to look nearby for closest opposing edges.
 		AffineTransform transform = getTransform();
 		// Grasp points in block coordinate frame.
 		Point2D blockPoint = appliedInv(transform, point);
 		Point2D blockPointMin = new Point2D.Double();
 		Point2D blockPointMax = new Point2D.Double();
 		// Find the wrapping points.
 		Rectangle2D bounds = getBounds();
 		if (bounds.getWidth() < bounds.getHeight()) {
 			blockPointMin.setLocation(bounds.getMinX(), blockPoint.getY());
 			blockPointMax.setLocation(bounds.getMaxX(), blockPoint.getY());
 		} else {
 			blockPointMin.setLocation(blockPoint.getX(), bounds.getMinY());
 			blockPointMax.setLocation(blockPoint.getX(), bounds.getMaxY());
 		}
 		Point2D pointMin = applied(transform, blockPointMin);
 		Point2D pointMax = applied(transform, blockPointMax);
 		// Calculate forces. Less stable the further from the center.
 		double force = 50;
 		double distX = Math.abs(blockPoint.getX() / bounds.getWidth());
 		double distY = Math.abs(blockPoint.getY() / bounds.getHeight());
 		double dist = 2 * Math.max(distX, distY); // 2x because earlier div by diameter, not radius.
 		double supportForce = 0.6 * force * Math.pow((1 - dist), 5);
 		// Normalize total max force for gain control.
 		double ratio = 100 / (force + 2 * supportForce);
 		force *= ratio;
 		supportForce *= ratio;
 		// Add drag joints.
 		mainJoint = addDragJoint(point, force);
 		if (supportForce > 0.01) {
 			addDragJoint(pointMin, supportForce);
 			addDragJoint(pointMax, supportForce);
 		}
 		// Wake up the body. It's awake if grasped.
 		body.wakeUp();
 	}
 
 	private double inverseTransformedWidth(AffineTransform transform, double width) {
 		try {
 			// TODO Look for an easier way.
 			double[] out = new double[4];
 			transform.inverseTransform(new double[] {0, 0, width, 0}, 0, out, 0, 2);
 			double dX = out[2] - out[0];
 			double dY = out[3] - out[1];
 			return Math.sqrt(dX * dX + dY * dY);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public boolean isAlive() {
 		return alive;
 	}
 
 	public boolean isGrasped() {
 		// TODO If we support other kinds of constraints (that 'affix' stuff), then this won't be good enough.
 		return body.getJointList() != null;
 	}
 
 	private boolean isRotated() {
 		if (body == null) {
 			return bodyDef.angle != 0;
 		} else {
 			XForm xForm = body.getXForm();
 			return !(xForm.R.col1.y == 0 && xForm.R.col2.x == 0);
 		}
 	}
 
 	/**
 	 * Only meaningful if previously grasped?
 	 */
 	public void moveTo(Point2D point) {
 		Vec2 mainTargetNew = new Vec2((float)point.getX(), (float)point.getY());
 		Vec2 mainTargetOld = mainJoint.getAnchor1().clone();
 		for (JointEdge j = body.getJointList(); j != null; j = j.next) {
 			MouseJoint joint = (MouseJoint)j.joint;
 			joint.getAnchor1().addLocal(mainTargetNew.sub(mainTargetOld));
 		}
 		body.wakeUp();
 	}
 
 	@Override
 	public void paint(Graphics2D graphics) {
 		Graphics2D g = copy(graphics);
 		try {
 			// Draw the block.
 			// Fill the full shape to avoid distance between fill and stroke.
 			Color color = this.color;
 			if (angularAccelerationCrossedZero || linearAccelerationCrossedZero) {
				color = Color.WHITE;
 			}
 			g.setColor(color);
 			g.fill(transformedShape());
 			// Shape information.
 			double strokeWidth = 0.1f;
 			g.setColor(color.darker());
 			g.setStroke(new BasicStroke((float)strokeWidth));
 			if (isRotated()) {
 				// Seems a bit too slow to do antialiasing. Even in this specific context.
 				// g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			}
 			// Stroke inset shape to avoid border overlap.
 			Shape insetShape = transformedShape(new AffineTransform(), strokeWidth);
 			g.draw(insetShape);
 			if (debugPaint) {
 				paintJoints(g);
 			}
 		} finally {
 			g.dispose();
 		}
 	}
 
 	private void paintJoints(Graphics2D graphics) {
 		if (isAlive()) {
 			graphics.setColor(Color.BLACK);
 			int j = 0;
 			JointEdge edge = body.getJointList();
 			while (edge != null) {
 				MouseJoint joint = (MouseJoint)edge.joint;
 				Vec2 target = joint.getAnchor1();
 				Point2D point = point(target.x, target.y);
 				graphics.drawString(String.valueOf(j), (float)point.getX(), (float)point.getY());
 				edge = edge.next;
 				j++;
 			}
 		}
 	}
 
 	public void release() {
 		while (body.getJointList() != null) {
 			body.getWorld().destroyJoint(body.getJointList().joint);
 		}
 	}
 
 	public void removeFromWorld() {
 		setAlive(false);
 		if (body != null) {
 			// Copy over the transform to remember the live state.
 			bodyDef.angle = (float)getAngle();
 			bodyDef.position.x = (float)getPosition().getX();
 			bodyDef.position.y = (float)getPosition().getY();
 			// TODO Could copy over other info.
 			// Now wipe out the body.
 			body.getWorld().destroyBody(body);
 			body = null;
 		}
 	}
 
 	/**
 	 * Manually control the alive setting rather than depending on connection to
 	 * world.
 	 */
 	public void setAlive(boolean alive) {
 		this.alive = alive;
 	}
 
 	/**
 	 * @param angle in rats (radians / pi).
 	 */
 	@Override
 	public void setAngle(double angle) {
 		bodyDef.angle = (float)(angle * Math.PI);
 	}
 
 	@Override
 	public void setAngularAcceleration(double angularAcceleration) {
 		// Track to/from zero.
 		// TODO Epsilon here is somewhat arbitrary. Learn cutoffs for interesting sample times automatically?
 		angularAccelerationCrossedZero = crossedZero(angularAcceleration, this.angularAcceleration, FilterLogger.ANGULAR_ACCELERATION_EPSILON);
 		// Now do the update.
 		this.angularAcceleration = angularAcceleration;
 	}
 
 	/**
 	 * Bogus just for copies.
 	 */
 	@Override
 	public void setAngularVelocity(double angularVelocity) {
 		this.angularVelocity = angularVelocity;
 	}
 
 	@Override
 	public void setColor(Color color) {
 		this.color = color;
 	}
 
 	public void setDensity(double density) {
 		shapeDef.density = (float)density;
 	}
 
 	public void setExtent(double extentX, double extentY) {
 		shapeDef.setAsBox((float)extentX, (float)extentY);
 	}
 
 	@Override
 	public void setExtent(Point2D extent) {
 		setExtent(extent.getX(), extent.getY());
 	}
 
 	public void setLinearAcceleration(Point2D linearAcceleration) {
 		// Track to/from zero.
 		// This is dependent on world axes, but ground and gravity gives some excuse for the distinction.
 		// TODO Epsilon here is somewhat arbitrary. Learn cutoffs for interesting sample times automatically?
 		linearAccelerationCrossedZero = crossedZero(linearAcceleration, this.linearAcceleration, FilterLogger.LINEAR_ACCELERATION_EPSILON);
 		// Now do the update.
 		this.linearAcceleration.setLocation(linearAcceleration);
 	}
 
 	/**
 	 * TODO Do I really care about explicit jerk?
 	 */
 	public void setLinearJerk(Point2D linearJerk) {
 		//		// Track to/from zero.
 		//		// This is dependent on world axes, but ground and gravity gives some excuse for the distinction.
 		//		int xSignNew = signApprox(linearJerk.getX(), EPSILON);
 		//		int ySignNew = signApprox(linearJerk.getY(), EPSILON);
 		//		int xSignOld = signApprox(this.linearJerk.getX(), EPSILON);
 		//		int ySignOld = signApprox(this.linearJerk.getY(), EPSILON);
 		//		linearJerkCrossedZero = !(xSignNew == xSignOld && ySignNew == ySignOld);
 		//		// Now do the update.
 		this.linearJerk.setLocation(linearJerk);
 	}
 
 	/**
 	 * Bogus just for copies.
 	 */
 	@Override
 	public void setLinearVelocity(Point2D linearVelocity) {
 		this.linearVelocity.setLocation(linearVelocity);
 	}
 
 	public void setPosition(double x, double y) {
 		bodyDef.position = new Vec2((float)x, (float)y);
 	}
 
 	public void setPosition(Point2D position) {
 		setPosition(position.getX(), position.getY());
 	}
 
 	public void setRotation(double rotation) {
 		bodyDef.angle = (float)(rotation * Math.PI);
 	}
 
 	public Shape transformedShape() {
 		return transformedShape(new AffineTransform(), 0);
 	}
 
 	/**
 	 * @param transform from display to block. Will be modified!
 	 * @param inset in the display frame.
 	 */
 	private Shape transformedShape(AffineTransform transform, double inset) {
 		worldToBlockTransform(transform);
 		// Size, including inset amount.
 		inset = inverseTransformedWidth(transform, inset);
 		Rectangle2D rectangle = getBounds();
 		rectangle.setRect(
 			rectangle.getX() + inset/2, rectangle.getY() + inset/2,
 			rectangle.getWidth() - inset, rectangle.getHeight() - inset
 		);
 		// Transformed rectangle.
 		GeneralPath path = new GeneralPath(rectangle);
 		Shape shape = path.createTransformedShape(transform);
 		return shape;
 	}
 
 	private void worldToBlockTransform(AffineTransform transform) {
 		if (body == null) {
 			transform.translate(bodyDef.position.x, bodyDef.position.y);
 			transform.rotate(bodyDef.angle);
 		} else {
 			// Position.
 			XForm xForm = body.getXForm();
 			transform.translate(xForm.position.x, xForm.position.y);
 			// Rotation.
 			transform.concatenate(new AffineTransform(new double[] {xForm.R.col1.x, xForm.R.col1.y, xForm.R.col2.x, xForm.R.col2.y}));
 		}
 	}
 
 }
