 package poomonkeys.common;
 
 import javax.media.opengl.GL2;
 
 public class Tank extends Drawable
 {
 	static final float TURRENT_LENGTH = 1f;
 	static final float WIDTH = 4f;
 	static final float HEIGHT = 4f;
 	float GRAVITY = .01f;
 	float tFriction = .1f;
 	float acceleration = 0;
 	float groundSpeed = 5;
 	float baseGeometry[];
 
 	public Drawable turret = new Drawable();
 	float turretLength = 1;
 
 	public Tank()
 	{
 		width = WIDTH;
 		height = HEIGHT;
 		m = 3;
 		this.registerDrawable(turret);
 	}
 
 	public void buildGeometry(float viewWidth, float viewHeight)
 	{
 		float[] baseGeometry = {
 				// X, Y
 				-width / 2, -height / 2, 0, width / 2, -height / 2f, 0, 0, height / 2, 0 };
 
 		this.drawMode = GL2.GL_LINE_LOOP;
 		super.vertices = baseGeometry;
 
 		turretLength = TURRENT_LENGTH;
 		float turretGeometry[] = { 0, 0, 0, 0, turretLength, 0 };
 		turret.drawMode = GL2.GL_LINES;
 		turret.vertices = turretGeometry;
 		turret.p[0] = 0;
 		turret.p[1] = height / 2;
 	}
 
 	public void intersectTerrain(Terrain t, float[] intersect)
 	{
 		if (intersect[2] > -.00001 && intersect[2] < .00001)
 		{
 			this.needsPositionUpdated = true;
 		}
 		this.p[0] += this.v[0] * intersect[2];
 		this.p[1] += this.v[1] * intersect[2];
 		System.out.println("t" + intersect[2]);
 		// removeFromPhysicsEngine = true;
 		float velocityVector = (float) Math.sqrt((this.v[0] * this.v[0]) + this.v[1] * this.v[1]);
 		float force = velocityVector * m;
 		if (force > 1000)
 		{
 			float xDistance = (.5f * m * (this.v[0] * this.v[0]));
 			float yDistance = (.5f * m * (this.v[1] * this.v[1]));
 			if (this.v[0] < 0)
 			{
 				xDistance = -xDistance;
 			}
 			if (this.v[1] < 0)
 			{
 				yDistance = -yDistance;
 			}
 			this.p[0] += xDistance;
 			this.p[1] += yDistance;
 			t.explodeRectangle(this.p[0], this.p[1], width / 2);
 			this.v[0] = 0;
 			this.v[1] = 0;
 			// System.out.println("x" + this.v[0]);
 			// System.out.println(this.v[1]);
 			removeFromPhysicsEngine = true;
 		} else
 		{
 			// if not exactly on segmentwidth then it might round down and cause
 			// horrible
 			// on the equalling segmentwidth down below
 			int index = (int) (intersect[0] / t.segmentWidth);
 			// attempt to fix error explained two lines above
 			if (intersect[0] - (index-1) * t.segmentWidth > t.segmentWidth - .001 && intersect[0] - (index-1) * t.segmentWidth < t.segmentWidth + .001)
 			{
				index--;
 				// removeFromPhysicsEngine = true;
 			}
 			index = Math.max(0, index);
 			index = Math.min(t.points.length - 2, index);
 
 			float vectorToLeftTerrainPointX = index * t.segmentWidth - intersect[0];
 			float vectorToLeftTerrainPointY = (t.points[index] + t.offsets[index]) - intersect[1];
 			// if its on an endpoint, its on an endpoint for two different
 			// linesegments so it chooses the one thats exactly an endpoint and
 			// these end up as 0 in denominator
 			if (vectorToLeftTerrainPointX + vectorToLeftTerrainPointY >-.0001 && vectorToLeftTerrainPointX + vectorToLeftTerrainPointY < .0001)
 			{
 				vectorToLeftTerrainPointX = t.segmentWidth;
 				// lX = (index - 1) * t.segmentWidth - intersect[0];
 				// lY = (t.points[index - 1] + t.offsets[index - 1]) -
 				// intersect[1];
 			}
 			// if width is = segmentWidth
 			if (vectorToLeftTerrainPointX > t.segmentWidth - .1 && vectorToLeftTerrainPointX < t.segmentWidth + .1)
 			{
 				//removeFromPhysicsEngine = true;
 				if (this.v[0] > 0 && t.points[index + 2] < t.points[index + 1])
 				{
 					return;
 				} else if (this.v[0] > 0 && t.points[index + 2] >= t.points[index + 1])
 				{
 					// actually rX and rY
 					vectorToLeftTerrainPointX = (index + 2) * t.segmentWidth - intersect[0];
 					vectorToLeftTerrainPointY = (t.points[index + 2] + t.offsets[index + 2]) - intersect[1];
 				} else if (this.v[0] <= 0
 						&& !isLeft(index * t.segmentWidth, t.points[index], (index - 1) * t.segmentWidth, t.points[index - 1], intersect[0] + this.v[0],
 								intersect[1] + this.v[1]))
 				{
 					return;
 				} else if (this.v[0] <= 0
 						&& isLeft(index * t.segmentWidth, t.points[index], (index - 1) * t.segmentWidth, t.points[index - 1], intersect[0] + this.v[0],
 								intersect[1] + this.v[1]))
 				{
 					System.out.println("boobs");
 					// removeFromPhysicsEngine = true;
 					vectorToLeftTerrainPointX = (index - 1) * t.segmentWidth - index * t.segmentWidth;
 					vectorToLeftTerrainPointY = (t.points[index - 1] + t.offsets[index - 1]) - (t.points[index] + t.offsets[index]);
 				}
 
 			}
 			float dX = this.v[0];
 			float dY = this.v[1];
 			float dotProduct = vectorToLeftTerrainPointX * dX + vectorToLeftTerrainPointY * dY;
 			float angle = (float) Math.acos(dotProduct
 					/ (Math.sqrt(vectorToLeftTerrainPointX * vectorToLeftTerrainPointX + vectorToLeftTerrainPointY * vectorToLeftTerrainPointY) * Math.sqrt(dX
 							* dX + dY * dY)));
 			if (angle > Math.PI / 2)
 			{
 				float normalDistance = (float) Math.sqrt(vectorToLeftTerrainPointX * vectorToLeftTerrainPointX + vectorToLeftTerrainPointY
 						* vectorToLeftTerrainPointY);
 				float normalLX = vectorToLeftTerrainPointX / normalDistance;
 				float normalLY = vectorToLeftTerrainPointY / normalDistance;
 				this.v[0] = -velocityVector * normalLX;
 				this.v[1] = -velocityVector * normalLY;
 			} else
 			{
 				// removeFromPhysicsEngine = true;
 				float normalDistance = (float) Math.sqrt(vectorToLeftTerrainPointX * vectorToLeftTerrainPointX + vectorToLeftTerrainPointY
 						* vectorToLeftTerrainPointY);
 				// System.out.println(normalDistance);
 				float normalLX = vectorToLeftTerrainPointX / normalDistance;
 				float normalLY = vectorToLeftTerrainPointY / normalDistance;
 
 				this.v[0] = velocityVector * normalLX;
 				this.v[1] = velocityVector * normalLY;
 			}
 
 		}
 	}
 
 	public void underTerrain(Terrain t)
 	{
 		float leftX = this.p[0] - width / 2;
 		float leftY = this.p[1] - height / 2;
 		float rightX = this.p[0] + width / 2;
 		float rightY = this.p[1] - height / 2;
 		System.out.println("x" + this.v[0]);
 		System.out.println(this.v[1]);
 		System.out.println(leftX * t.segmentWidth);
 		System.out.println((int) (leftX * t.segmentWidth));
 		System.out.println("underT");
 		float rPercentX = ((this.p[0] + width / 2) / t.segmentWidth) - (int) ((this.p[0] + width / 2) / t.segmentWidth);
 		float rLandY = t.points[(int) (rightX / t.segmentWidth)] + (t.points[(int) (rightX / t.segmentWidth) + 1] - t.points[(int) (rightX / t.segmentWidth)])
 				* rPercentX;
 		float lPercentX = (((this.p[0] - width / 2) / t.segmentWidth) - (int) ((this.p[0] - width / 2) / t.segmentWidth));
 		float lLandY = t.points[(int) (leftX / t.segmentWidth)] + (t.points[(int) (leftX / t.segmentWidth) + 1] - t.points[(int) (leftX / t.segmentWidth)])
 				* lPercentX;
 		if (rLandY - rightY >= lLandY - leftY)
 		{
 			this.p[0] = rightX - width / 2 + 1f;
 			this.p[1] = rLandY + height / 2 + 1f;
 		} else if (rLandY - rightY < lLandY - leftY)
 		{
 			this.p[0] = leftX + width / 2 + 1f;
 			this.p[1] = lLandY + height / 2 + 1f;
 		}
 		// removeFromPhysicsEngine = true;
 	}
 
 	// checks to see if point C lies to the left of line segment AB
 	public boolean isLeft(float aX, float aY, float bX, float bY, float cX, float cY)
 	{
 		return ((bX - aX) * (cY - aY) - (bY - aY) * (cX - aX)) > 0;
 	}
 }
