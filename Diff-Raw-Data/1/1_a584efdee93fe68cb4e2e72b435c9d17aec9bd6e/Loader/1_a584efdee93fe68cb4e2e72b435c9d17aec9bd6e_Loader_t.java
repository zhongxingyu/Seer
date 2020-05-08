 package org.andengine.extension.rubeloader;
 
 /*
  Author: Chris Campbell - www.iforce2d.net
 
  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.
  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:
  1. The origin of this software must not be misrepresented; you must not
  claim that you wrote the original software. If you use this software
  in a product, an acknowledgment in the product documentation would be
  appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
  misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.
  
  
  This software was modified by Michal Stawinski - michal.stawinski@gmail.com
  Originall JBOX2D version was modified to work as AndEngine plugin
  */
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.ChainShape;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.EdgeShape;
 import com.badlogic.gdx.physics.box2d.Filter;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Joint;
 import com.badlogic.gdx.physics.box2d.JointDef;
 import com.badlogic.gdx.physics.box2d.MassData;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.Shape;
 import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
 import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
 import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
 import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
 import com.badlogic.gdx.physics.box2d.joints.GearJoint;
 import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
 import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
 import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
 import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
 import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
 import com.badlogic.gdx.physics.box2d.joints.PulleyJoint;
 import com.badlogic.gdx.physics.box2d.joints.PulleyJointDef;
 import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
 import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
 import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
 import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
 import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
 import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
 
 /**
  * 
  * 
  */
 public class Loader {
 	public static final int MAX_POLY_VERTS = 8;
 
 	protected boolean m_useHumanReadableFloats;
 
 	protected int m_simulationPositionIterations;
 	protected int m_simulationVelocityIterations;
 	protected float m_simulationFPS;
 
 	protected Map<Integer, Body> m_indexToBodyMap;
 	protected Map<Body, Integer> m_bodyToIndexMap;
 	protected Map<Joint, Integer> m_jointToIndexMap;
 	protected Vector<Body> m_bodies;
 	protected Vector<Joint> m_joints;
 	protected Vector<Image> m_images;
 
 	protected Map<Body, String> m_bodyToNameMap;
 	protected Map<Fixture, String> m_fixtureToNameMap;
 	protected Map<Joint, String> m_jointToNameMap;
 	protected Map<Image, String> m_imageToNameMap;
 
 	// This maps an item (Body, Fixture etc) to a set of custom properties.
 	// Use null for world properties.
 	protected Map<Object, CustomProperties> m_customPropertiesMap;
 	
 	protected Set<Body> m_bodiesWithCustomProperties;
 	protected Set<Fixture> m_fixturesWithCustomProperties;
 	protected Set<Joint> m_jointsWithCustomProperties;
 	protected Set<Image> m_imagesWithCustomProperties;
 	protected Set<PhysicsWorld> m_worldsWithCustomProperties;
 
 	public Loader() {
 		this(true);
 	}
 
 	public Loader(boolean useHumanReadableFloats) {
 
 		if (!useHumanReadableFloats) {
 			// The floatToHex function is not giving the same results
 			// as the original C++ version... not critical so worry about it
 			// later.
 			System.out.println("Non human readable floats are not implemented yet");
 			useHumanReadableFloats = true;
 		}
 
 		m_useHumanReadableFloats = useHumanReadableFloats;
 		m_simulationPositionIterations = 3;
 		m_simulationVelocityIterations = 8;
 		m_simulationFPS = 60;
 
 		m_indexToBodyMap = new HashMap<Integer, Body>();
 		m_bodyToIndexMap = new HashMap<Body, Integer>();
 		m_jointToIndexMap = new HashMap<Joint, Integer>();
 		m_bodies = new Vector<Body>();
 		m_joints = new Vector<Joint>();
 		m_images = new Vector<Image>();
 
 		m_bodyToNameMap = new HashMap<Body, String>();
 		m_fixtureToNameMap = new HashMap<Fixture, String>();
 		m_jointToNameMap = new HashMap<Joint, String>();
 		m_imageToNameMap = new HashMap<Image, String>();
 		
 		m_customPropertiesMap = new HashMap<Object, CustomProperties>();
 		
 		m_bodiesWithCustomProperties = new HashSet<Body>();
 		m_fixturesWithCustomProperties = new HashSet<Fixture>();
 		m_jointsWithCustomProperties = new HashSet<Joint>();
 		m_imagesWithCustomProperties = new HashSet<Image>();
 		m_worldsWithCustomProperties = new HashSet<PhysicsWorld>();
 	}
 
 	// TODO Use this section to enable worlds saving
 //	public JSONObject writeToValue(PhysicsWorld world) throws JSONException {
 //		if (null == world)
 //			return new JSONObject();
 //
 //		return b2j(world);
 //	}
 //
 //	public String worldToString(PhysicsWorld world, int indentFactor) throws JSONException {
 //		if (null == world)
 //			return new String();
 //
 //		return b2j(world).toString(indentFactor);
 //	}
 //
 //	public boolean writeToFile(PhysicsWorld world, String filename, int indentFactor, StringBuilder errorMsg) {
 //		if (null == world || null == filename)
 //			return false;
 //
 //		PrintWriter writer;
 //		try {
 //			writer = new PrintWriter(filename);
 //		} catch (FileNotFoundException e) {
 //			errorMsg.append("Could not open file " + filename + "for writing");
 //			return false;
 //		}
 //
 //		try {
 //			writer.println(b2j(world).toString(indentFactor));
 //		} catch (JSONException e) {
 //			errorMsg.append("Error writing JSON to file: " + filename);
 //		}
 //		writer.close();
 //
 //		return true;
 //	}
 //
 //	public JSONObject b2j(PhysicsWorld world) throws JSONException {
 //		JSONObject worldValue = new JSONObject();
 //
 //		m_bodyToIndexMap.clear();
 //		m_jointToIndexMap.clear();
 //
 //		vecToJson("gravity", world.getGravity(), worldValue);
 //		worldValue.put("allowSleep", world.isAllowSleep());
 //		worldValue.put("autoClearForces", world.getAutoClearForces());
 //		worldValue.put("warmStarting", world.isWarmStarting());
 //		worldValue.put("continuousPhysics", world.isContinuousPhysics());
 //		// worldValue.put("subStepping", world.isSubStepping());
 //		// worldValue["hasDestructionListener"] =
 //		// world->HasDestructionListener();
 //		// worldValue["hasContactFilter"] = world->HasContactFilter();
 //		// worldValue["hasContactListener"] = world->HasContactListener();
 //
 //		JSONArray customPropertyValue = writeCustomPropertiesToJson(null);
 //		if (customPropertyValue.length() > 0)
 //			worldValue.put("customProperties", customPropertyValue);
 //
 //		int i = 0;
 //		for (Body body = world.getBodyList(); null != body; body = body.getNext()) {
 //			m_bodyToIndexMap.put(body, i);
 //			worldValue.append("body", b2j(body));
 //			i++;
 //		}
 //
 //		// need two passes for joints because gear joints reference other joints
 //		i = 0;
 //		for (Joint joint = world.getJointList(); null != joint; joint = joint.getNext()) {
 //			if (joint.getType() == JointType.GEAR)
 //				continue;
 //			m_jointToIndexMap.put(joint, i);
 //			worldValue.append("joint", b2j(joint));
 //			i++;
 //		}
 //		for (Joint joint = world.getJointList(); null != joint; joint = joint.getNext()) {
 //			if (joint.getType() != JointType.GEAR)
 //				continue;
 //			m_jointToIndexMap.put(joint, i);
 //			worldValue.append("joint", b2j(joint));
 //			i++;
 //		}
 //
 //		// Currently the only efficient way to add images to a Jb2dJson
 //		// is by using the R.U.B.E editor. This code has not been tested,
 //		// but should work ok.
 //		i = 0;
 //		for (Image image : m_imageToNameMap.keySet()) {
 //			worldValue.append("image", b2j(image));
 //		}
 //
 //		m_bodyToIndexMap.clear();
 //		m_jointToIndexMap.clear();
 //
 //		return worldValue;
 //	}
 
 	public void setBodyName(Body body, String name) {
 		m_bodyToNameMap.put(body, name);
 	}
 
 	public void setFixtureName(Fixture fixture, String name) {
 		m_fixtureToNameMap.put(fixture, name);
 	}
 
 	public void setJointName(Joint joint, String name) {
 		m_jointToNameMap.put(joint, name);
 	}
 
 	public void setImageName(Image image, String name) {
 		m_imageToNameMap.put(image, name);
 	}
 
 	public JSONObject b2j(Body body) throws JSONException {
 		JSONObject bodyValue = new JSONObject();
 
 		String bodyName = getBodyName(body);
 		if (null != bodyName)
 			bodyValue.put("name", bodyName);
 
 		switch (body.getType()) {
 		case StaticBody:
 			bodyValue.put("type", 0);
 			break;
 		case KinematicBody:
 			bodyValue.put("type", 1);
 			break;
 		case DynamicBody:
 			bodyValue.put("type", 2);
 			break;
 		}
 
 		vecToJson("position", body.getPosition(), bodyValue);
 		floatToJson("angle", body.getAngle(), bodyValue);
 
 		vecToJson("linearVelocity", body.getLinearVelocity(), bodyValue);
 		floatToJson("angularVelocity", body.getAngularVelocity(), bodyValue);
 
 		if (body.getLinearDamping() != 0)
 			floatToJson("linearDamping", body.getLinearDamping(), bodyValue);
 		if (body.getAngularDamping() != 0)
 			floatToJson("angularDamping", body.getAngularDamping(), bodyValue);
 		if (body.getGravityScale() != 1)
 			floatToJson("gravityScale", body.getGravityScale(), bodyValue);
 
 		if (body.isBullet())
 			bodyValue.put("bullet", true);
 		if (!body.isSleepingAllowed())
 			bodyValue.put("allowSleep", false);
 		if (body.isAwake())
 			bodyValue.put("awake", true);
 		if (!body.isActive())
 			bodyValue.put("active", false);
 		if (body.isFixedRotation())
 			bodyValue.put("fixedRotation", true);
 
 		MassData massData = body.getMassData();
 		if (massData.mass != 0)
 			floatToJson("massData-mass", massData.mass, bodyValue);
 		if (massData.center.x != 0 || massData.center.y != 0)
 			vecToJson("massData-center", massData.center, bodyValue);
 		if (massData.I != 0) {
 			floatToJson("massData-I", massData.I, bodyValue);
 		}
 
 
 		ArrayList<Fixture> fixtures = body.getFixtureList();
 		for (Iterator<Fixture> iter = fixtures.iterator(); iter.hasNext();) {
 			Fixture fixture = (Fixture) iter.next();
 			bodyValue.accumulate("fixture", b2j(fixture));
 		}
 
 		JSONArray customPropertyValue = writeCustomPropertiesToJson(body);
 		if (customPropertyValue.length() > 0)
 			bodyValue.put("customProperties", customPropertyValue);
 
 		return bodyValue;
 	}
 
 	public JSONObject b2j(Fixture fixture) throws JSONException {
 		JSONObject fixtureValue = new JSONObject();
 
 		String fixtureName = getFixtureName(fixture);
 		if (null != fixtureName)
 			fixtureValue.put("name", fixtureName);
 
 		if (fixture.getRestitution() != 0)
 			floatToJson("restitution", fixture.getRestitution(), fixtureValue);
 		if (fixture.getFriction() != 0)
 			floatToJson("friction", fixture.getFriction(), fixtureValue);
 		if (fixture.getDensity() != 0)
 			floatToJson("density", fixture.getDensity(), fixtureValue);
 		if (fixture.isSensor())
 			fixtureValue.put("sensor", true);
 
 		Filter filter = fixture.getFilterData();
 		if (filter.categoryBits != 0x0001)
 			fixtureValue.put("filter-categoryBits", filter.categoryBits);
 		if (filter.maskBits != 0xffff)
 			fixtureValue.put("filter-maskBits", filter.maskBits);
 		if (filter.groupIndex != 0)
 			fixtureValue.put("filter-groupIndex", filter.groupIndex);
 
 		Shape shape = fixture.getShape();
 		switch (shape.getType()) {
 		case Circle: {
 			CircleShape circle = (CircleShape) shape;
 			JSONObject shapeValue = new JSONObject();
 			floatToJson("radius", circle.getRadius(), shapeValue);
 			vecToJson("center", circle.getPosition(), shapeValue);
 			fixtureValue.put("circle", shapeValue);
 		}
 			break;
 		case Edge: {
 			EdgeShape edge = (EdgeShape) shape;
 			JSONObject shapeValue = new JSONObject();
 
 			Vector2 v1 = Vector2Pool.obtain();
 			Vector2 v2 = Vector2Pool.obtain();
 			edge.getVertex1(v1);
 			edge.getVertex2(v2);
 			vecToJson("vertex1", v1, shapeValue);
 			vecToJson("vertex2", v2, shapeValue);
 			Vector2Pool.recycle(v1);
 			Vector2Pool.recycle(v2);
 
 			// TODO Use it when B2D extension will start exposing this info
 //			if (edge.m_hasVertex0)
 //				shapeValue.put("hasVertex0", true);
 //			if (edge.m_hasVertex3)
 //				shapeValue.put("hasVertex3", true);
 //			if (edge.m_hasVertex0)
 //				vecToJson("vertex0", edge.m_vertex0, shapeValue);
 //			if (edge.m_hasVertex3)
 //				vecToJson("vertex3", edge.m_vertex3, shapeValue);
 			fixtureValue.put("edge", shapeValue);
 		}
 			break;
 		case Chain: {
 			ChainShape chain = (ChainShape) shape;
 			JSONObject shapeValue = new JSONObject();
 
 			int count = chain.getVertexCount();
 			Vector2 v = Vector2Pool.obtain();
 			for (int i = 0; i < count; ++i) {
 				chain.getVertex(i, v);
 				vecToJson("vertices", v, shapeValue, i);
 			}
 			Vector2Pool.recycle(v);
 
 			// TODO Use it when B2D extension will start exposing this info
 //			if (chain.m_hasPrevVertex)
 //				shapeValue.put("hasPrevVertex", true);
 //			if (chain.m_hasNextVertex)
 //				shapeValue.put("hasNextVertex", true);
 //			if (chain.m_hasPrevVertex)
 //				vecToJson("prevVertex", chain.m_prevVertex, shapeValue);
 //			if (chain.m_hasNextVertex)
 //				vecToJson("nextVertex", chain.m_nextVertex, shapeValue);
 			fixtureValue.put("chain", shapeValue);
 		}
 			break;
 		case Polygon: {
 			PolygonShape poly = (PolygonShape) shape;
 			JSONObject shapeValue = new JSONObject();
 			int vertexCount = poly.getVertexCount();
 
 			Vector2 v = Vector2Pool.obtain();
 			for (int i = 0; i < vertexCount; ++i) {
 				poly.getVertex(i, v);
 				vecToJson("vertices", v, shapeValue, i);
 			}
 			Vector2Pool.recycle(v);
 
 			fixtureValue.put("polygon", shapeValue);
 		}
 			break;
 		default:
 			System.out.println("Unknown shape type : " + shape.getType());
 		}
 
 		JSONArray customPropertyValue = writeCustomPropertiesToJson(fixture);
 		if (customPropertyValue.length() > 0)
 			fixtureValue.put("customProperties", customPropertyValue);
 
 		return fixtureValue;
 	}
 
 	public JSONObject b2j(Joint joint) throws JSONException {
 		JSONObject jointValue = new JSONObject();
 
 		String jointName = getJointName(joint);
 		if (null != jointName)
 			jointValue.put("name", jointName);
 
 		int bodyIndexA = lookupBodyIndex(joint.getBodyA());
 		int bodyIndexB = lookupBodyIndex(joint.getBodyB());
 		jointValue.put("bodyA", bodyIndexA);
 		jointValue.put("bodyB", bodyIndexB);
 
 		// XXX we do not have this info available!
 //		if (joint.getCollideConnected())
 //			jointValue.put("collideConnected", true);
 
 		Body bodyA = joint.getBodyA();
 		Body bodyB = joint.getBodyB();
 
 		switch (joint.getType()) {
 		case RevoluteJoint: {
 			jointValue.put("type", "revolute");
 
 			RevoluteJoint revoluteJoint = (RevoluteJoint) joint;
 			vecToJson("anchorA", bodyA.getLocalPoint(revoluteJoint.getAnchorA()), jointValue);
 			vecToJson("anchorB", bodyB.getLocalPoint(revoluteJoint.getAnchorB()), jointValue);
 			floatToJson("refAngle", bodyB.getAngle() - bodyA.getAngle() - revoluteJoint.getJointAngle(), jointValue);
 			floatToJson("jointSpeed", revoluteJoint.getJointSpeed(), jointValue);
 			jointValue.put("enableLimit", revoluteJoint.isLimitEnabled());
 			floatToJson("lowerLimit", revoluteJoint.getLowerLimit(), jointValue);
 			floatToJson("upperLimit", revoluteJoint.getUpperLimit(), jointValue);
 			jointValue.put("enableMotor", revoluteJoint.isMotorEnabled());
 			floatToJson("motorSpeed", revoluteJoint.getMotorSpeed(), jointValue);
 			// XXX what is this parameter?
 //			floatToJson("maxMotorTorque", revoluteJoint.getMaxMotorTorque(), jointValue);
 			floatToJson("maxMotorTorque", revoluteJoint.getMotorTorque(1), jointValue);
 		}
 			break;
 		case PrismaticJoint: {
 			jointValue.put("type", "prismatic");
 
 			PrismaticJoint prismaticJoint = (PrismaticJoint) joint;
 			vecToJson("anchorA", bodyA.getLocalPoint(prismaticJoint.getAnchorA()), jointValue);
 			vecToJson("anchorB", bodyB.getLocalPoint(prismaticJoint.getAnchorB()), jointValue);
 			// XXX not really sure how I should handle this...
 //			vecToJson("localAxisA", prismaticJoint.getLocalAxisA(), jointValue);
 //			floatToJson("refAngle", prismaticJoint.getReferenceAngle(), jointValue);
 			jointValue.put("enableLimit", prismaticJoint.isLimitEnabled());
 			floatToJson("lowerLimit", prismaticJoint.getLowerLimit(), jointValue);
 			floatToJson("upperLimit", prismaticJoint.getUpperLimit(), jointValue);
 			jointValue.put("enableMotor", prismaticJoint.isMotorEnabled());
 			// XXX what is this parameter?
 //			floatToJson("maxMotorForce", prismaticJoint.getMaxMotorForce(), jointValue);
 			floatToJson("maxMotorForce", prismaticJoint.getMotorForce(1), jointValue);
 			floatToJson("motorSpeed", prismaticJoint.getMotorSpeed(), jointValue);
 		}
 			break;
 		case DistanceJoint: {
 			jointValue.put("type", "distance");
 
 			DistanceJoint distanceJoint = (DistanceJoint) joint;
 			vecToJson("anchorA", bodyA.getLocalPoint(distanceJoint.getAnchorA()), jointValue);
 			vecToJson("anchorB", bodyB.getLocalPoint(distanceJoint.getAnchorB()), jointValue);
 			floatToJson("length", distanceJoint.getLength(), jointValue);
 			floatToJson("frequency", distanceJoint.getFrequency(), jointValue);
 			floatToJson("dampingRatio", distanceJoint.getDampingRatio(), jointValue);
 		}
 			break;
 		case PulleyJoint: {
 			jointValue.put("type", "pulley");
 
 			PulleyJoint pulleyJoint = (PulleyJoint) joint;
 			vecToJson("groundAnchorA", pulleyJoint.getGroundAnchorA(), jointValue);
 			vecToJson("groundAnchorB", pulleyJoint.getGroundAnchorB(), jointValue);
 			Vector2 tmpAnchor = pulleyJoint.getAnchorA();
 			vecToJson("anchorA", bodyA.getLocalPoint(tmpAnchor), jointValue);
 			floatToJson("lengthA", (pulleyJoint.getGroundAnchorA().sub(tmpAnchor)).len(), jointValue);
 			tmpAnchor = pulleyJoint.getAnchorB();
 			vecToJson("anchorB", bodyB.getLocalPoint(tmpAnchor), jointValue);
 			floatToJson("lengthB", (pulleyJoint.getGroundAnchorB().sub(tmpAnchor)).len(), jointValue);
 			floatToJson("ratio", pulleyJoint.getRatio(), jointValue);
 		}
 			break;
 		case MouseJoint: {
 			jointValue.put("type", "mouse");
 
 			MouseJoint mouseJoint = (MouseJoint) joint;
 			vecToJson("target", mouseJoint.getTarget(), jointValue);
 			vecToJson("anchorB", mouseJoint.getAnchorB(), jointValue);
 			floatToJson("maxForce", mouseJoint.getMaxForce(), jointValue);
 			floatToJson("frequency", mouseJoint.getFrequency(), jointValue);
 			floatToJson("dampingRatio", mouseJoint.getDampingRatio(), jointValue);
 		}
 			break;
 		case GearJoint: {
 			jointValue.put("type", "gear");
 			GearJoint gearJoint = (GearJoint)joint;
 			// XXX we do not have this info available!
 //			int jointIndex1 = lookupJointIndex( gearJoint.getJoint1() );
 //			int jointIndex2 = lookupJointIndex( gearJoint.getJoint2() );
 //			jointValue.put("joint1", jointIndex1);
 //			jointValue.put("joint2", jointIndex2);
 			jointValue.put("ratio", gearJoint.getRatio());
 		}
 			break;
 		case WheelJoint: {
 			jointValue.put("type", "wheel");
 			WheelJoint wheelJoint = (WheelJoint)joint;
 			vecToJson("anchorA", bodyA.getLocalPoint(wheelJoint.getAnchorA()), jointValue);
 			vecToJson("anchorB", bodyB.getLocalPoint(wheelJoint.getAnchorB()), jointValue);
 			// XXX not really sure how I should handle this...
 //			vecToJson("localAxisA", wheelJoint.getLocalAxisA(), jointValue);
 			// XXX we do not have this info available!
 //			jointValue.put("enableMotor", wheelJoint.isMotorEnabled());
 			floatToJson("motorSpeed", wheelJoint.getMotorSpeed(), jointValue);
 			floatToJson("maxMotorTorque", wheelJoint.getMaxMotorTorque(), jointValue);
 			floatToJson("springFrequency", wheelJoint.getSpringFrequencyHz(), jointValue);
 			floatToJson("springDampingRatio", wheelJoint.getSpringDampingRatio(), jointValue);
 		}
 			break;
 		case WeldJoint: {
 			jointValue.put("type", "weld");
 
 			WeldJoint weldJoint = (WeldJoint) joint;
 			vecToJson("anchorA", bodyA.getLocalPoint(weldJoint.getAnchorA()), jointValue);
 			vecToJson("anchorB", bodyB.getLocalPoint(weldJoint.getAnchorB()), jointValue);
 			// XXX we do not have this info available!
 //			floatToJson("refAngle", weldJoint.getReferenceAngle(), jointValue);
 		}
 			break;
 		case FrictionJoint: {
 			jointValue.put("type", "friction");
 
 			FrictionJoint frictionJoint = (FrictionJoint) joint;
 			vecToJson("anchorA", bodyA.getLocalPoint(frictionJoint.getAnchorA()), jointValue);
 			vecToJson("anchorB", bodyB.getLocalPoint(frictionJoint.getAnchorB()), jointValue);
 			floatToJson("maxForce", frictionJoint.getMaxForce(), jointValue);
 			floatToJson("maxTorque", frictionJoint.getMaxTorque(), jointValue);
 		}
 			break;
 		case RopeJoint: {
 			jointValue.put("type", "rope");
 			RopeJoint ropeJoint = (RopeJoint)joint;
 			vecToJson("anchorA", bodyA.getLocalPoint(ropeJoint.getAnchorA()), jointValue);
 			vecToJson("anchorB", bodyB.getLocalPoint(ropeJoint.getAnchorB()), jointValue);
 			floatToJson("maxLength", ropeJoint.getMaxLength(), jointValue);
 		}
 			break;
 		case Unknown:
 		default:
 			System.out.println("Unknown joint type : " + joint.getType());
 		}
 
 		JSONArray customPropertyValue = writeCustomPropertiesToJson(joint);
 		if (customPropertyValue.length() > 0)
 			jointValue.put("customProperties", customPropertyValue);
 
 		return jointValue;
 	}
 
 	JSONObject b2j(Image image) throws JSONException {
 		JSONObject imageValue = new JSONObject();
 
 		if (null != image.body)
 			imageValue.put("body", lookupBodyIndex(image.body));
 		else
 			imageValue.put("body", -1);
 
 		if (null != image.name)
 			imageValue.put("name", image.name);
 		if (null != image.file)
 			imageValue.put("file", image.file);
 
 		vecToJson("center", image.center, imageValue);
 		floatToJson("angle", image.angle, imageValue);
 		floatToJson("scale", image.scale, imageValue);
 		if (image.flip)
 			imageValue.put("flip", true);
 		floatToJson("opacity", image.opacity, imageValue);
 		imageValue.put("filter", image.filter);
 		floatToJson("renderOrder", image.renderOrder, imageValue);
 
 		// image->updateCorners();
 		for (int i = 0; i < 4; i++)
 			vecToJson("corners", image.corners[i], imageValue, i);
 
 		// image->updateUVs();
 		for (int i = 0; i < 2 * image.numPoints; i++) {
 			vecToJson("glVertexPointer", image.points[i], imageValue, i);
 			vecToJson("glTexCoordPointer", image.uvCoords[i], imageValue, i);
 		}
 		for (int i = 0; i < image.numIndices; i++)
 			vecToJson("glDrawElements", image.indices[i], imageValue, i);
 
 		JSONArray customPropertyValue = writeCustomPropertiesToJson(image);
 		if (customPropertyValue.length() > 0)
 			imageValue.put("customProperties", customPropertyValue);
 
 		return imageValue;
 	}
 
 	Body lookupBodyFromIndex(int index) {
 		if (m_indexToBodyMap.containsKey(index))
 			return m_indexToBodyMap.get(index);
 		else
 			return null;
 	}
 
 	protected int lookupBodyIndex(Body body) {
 		Integer val = m_bodyToIndexMap.get(body);
 		if (null != val)
 			return val.intValue();
 		else
 			return -1;
 	}
 
 	protected int lookupJointIndex(Joint joint) {
 		Integer val = m_jointToIndexMap.get(joint);
 		if (null != val)
 			return val.intValue();
 		else
 			return -1;
 	}
 
 	public String getBodyName(Body body) {
 		return m_bodyToNameMap.get(body);
 	}
 
 	public String getFixtureName(Fixture fixture) {
 		return m_fixtureToNameMap.get(fixture);
 	}
 
 	public String getJointName(Joint joint) {
 		return m_jointToNameMap.get(joint);
 	}
 
 	public String getImageName(Image image) {
 		return m_imageToNameMap.get(image);
 	}
 
 	public String floatToHex(float f) {
 		int bits = Float.floatToIntBits(f);
 		return Integer.toHexString(bits);
 	}
 
 	public void floatToJson(String name, float f, JSONObject value) throws JSONException {
 		// cut down on file space for common values
 		if (f == 0)
 			value.put(name, 0);
 		else if (f == 1)
 			value.put(name, 1);
 		else {
 			if (m_useHumanReadableFloats)
 				value.put(name, f);
 			else
 				value.put(name, floatToHex(f));
 		}
 	}
 
 	public void vecToJson(String name, int v, JSONObject value, int index) throws JSONException {
 		if (index > -1) {
 			JSONArray array = value.getJSONArray(name);
 			array.put(index, v);
 		} else
 			value.put(name, v);
 	}
 
 	public void vecToJson(String name, float v, JSONObject value, int index) throws JSONException {
 		if (index > -1) {
 			if (m_useHumanReadableFloats) {
 				JSONArray array = value.getJSONArray(name);
 				array.put(index, v);
 			} else {
 				JSONArray array = value.getJSONArray(name);
 				if (v == 0)
 					array.put(index, 0);
 				else if (v == 1)
 					array.put(index, 1);
 				else
 					array.put(index, floatToHex(v));
 			}
 		} else
 			floatToJson(name, v, value);
 	}
 
 	public void vecToJson(String name, Vector2 vec, JSONObject value) throws JSONException {
 		vecToJson(name, vec, value, -1);
 	}
 
 	public void vecToJson(String name, Vector2 vec, JSONObject value, int index) throws JSONException {
 		if (index > -1) {
 			if (m_useHumanReadableFloats) {
 				boolean alreadyHadArray = value.has(name);
 				JSONArray arrayX = alreadyHadArray ? value.getJSONObject(name).getJSONArray("x") : new JSONArray();
 				JSONArray arrayY = alreadyHadArray ? value.getJSONObject(name).getJSONArray("y") : new JSONArray();
 				arrayX.put(index, vec.x);
 				arrayY.put(index, vec.y);
 				if (!alreadyHadArray) {
 					JSONObject subValue = new JSONObject();
 					subValue.put("x", arrayX);
 					subValue.put("y", arrayY);
 					value.put(name, subValue);
 				}
 			} else {
 				boolean alreadyHadArray = value.has(name);
 				JSONArray arrayX = alreadyHadArray ? value.getJSONObject(name).getJSONArray("x") : new JSONArray();
 				JSONArray arrayY = alreadyHadArray ? value.getJSONObject(name).getJSONArray("y") : new JSONArray();
 				if (vec.x == 0)
 					arrayX.put(index, 0);
 				else if (vec.x == 1)
 					arrayX.put(index, 1);
 				else
 					arrayX.put(index, floatToHex(vec.x));
 				if (vec.y == 0)
 					arrayY.put(index, 0);
 				else if (vec.y == 1)
 					arrayY.put(index, 1);
 				else
 					arrayY.put(index, floatToHex(vec.y));
 				if (!alreadyHadArray) {
 					JSONObject subValue = new JSONObject();
 					subValue.put("x", arrayX);
 					subValue.put("y", arrayY);
 					value.put(name, subValue);
 				}
 			}
 		} else {
 			if (vec.x == 0 && vec.y == 0)
 				value.put(name, 0);// cut down on file space for common values
 			else {
 				JSONObject vecValue = new JSONObject();
 				floatToJson("x", vec.x, vecValue);
 				floatToJson("y", vec.y, vecValue);
 				value.put(name, vecValue);
 			}
 		}
 	}
 
 	public void clear() {
 		m_indexToBodyMap.clear();
 		m_bodyToIndexMap.clear();
 		m_jointToIndexMap.clear();
 		m_bodies.clear();
 		m_joints.clear();
 		m_images.clear();
 
 		m_bodyToNameMap.clear();
 		m_fixtureToNameMap.clear();
 		m_jointToNameMap.clear();
 		m_imageToNameMap.clear();
 
 		m_customPropertiesMap.clear();
 		m_bodiesWithCustomProperties.clear();
		m_fixturesWithCustomProperties.clear();
 		m_jointsWithCustomProperties.clear();
 		m_imagesWithCustomProperties.clear();
 		m_worldsWithCustomProperties.clear();
 	}
 
 	public PhysicsWorld readFromJSONObject(JSONObject worldValue) throws JSONException {
 		clear();
 
 		return j2b2PhysicsWorld(worldValue);
 	}
 
 	public PhysicsWorld readFromString(String str, StringBuilder errorMsg) {
 		try {
 			JSONObject worldValue = new JSONObject(str);
 			return j2b2PhysicsWorld(worldValue);
 		} catch (JSONException e) {
 			errorMsg.append("Failed to parse JSON");
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public PhysicsWorld readFromFile(String filename, StringBuilder errorMsg) {
 		if (null == filename)
 			return null;
 
 		String str = new String();
 		try {
 			InputStream fis;
 			fis = new FileInputStream(filename);
 			BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
 			String line;
 			while ((line = br.readLine()) != null) {
 				str += line;
 			}
 		} catch (FileNotFoundException e) {
 			errorMsg.append("Could not open file for reading: " + filename);
 			return null;
 		} catch (IOException e) {
 			errorMsg.append("Error reading file: " + filename);
 			return null;
 		}
 
 		try {
 			JSONObject worldValue = new JSONObject(str);
 			return j2b2PhysicsWorld(worldValue);
 		} catch (JSONException e) {
 			errorMsg.append("\nFailed to parse JSON: " + filename);
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public PhysicsWorld j2b2PhysicsWorld(JSONObject worldValue) throws JSONException {
 		int sim_positionIterations = worldValue.getInt("positionIterations");
 		int sim_velocityIterations = worldValue.getInt("velocityIterations");
 		boolean sim_allowSleep = worldValue.getBoolean("allowSleep");
 
 		PhysicsWorld world = new PhysicsWorld(jsonToVec("gravity", worldValue), sim_allowSleep, sim_positionIterations, sim_velocityIterations);
 
 		world.setAutoClearForces(worldValue.getBoolean("autoClearForces"));
 		world.setWarmStarting(worldValue.getBoolean("warmStarting"));
 		world.setContinuousPhysics(worldValue.getBoolean("continuousPhysics"));
 		// world.setSubStepping( worldValue.getBoolean("subStepping") );
 
 		readCustomPropertiesFromJson(world, worldValue);
 
 		int i = 0;
 		JSONArray bodyValues = worldValue.optJSONArray("body");
 		if (null != bodyValues) {
 			int numBodyValues = bodyValues.length();
 			for (i = 0; i < numBodyValues; i++) {
 				JSONObject bodyValue = bodyValues.getJSONObject(i);
 				Body body = j2b2Body(world, bodyValue);
 				readCustomPropertiesFromJson(body, bodyValue);
 				m_bodies.add(body);
 				m_indexToBodyMap.put(i, body);
 			}
 		}
 
 		// need two passes for joints because gear joints reference other joints
 		JSONArray jointValues = worldValue.optJSONArray("joint");
 		if (null != jointValues) {
 			int numJointValues = jointValues.length();
 			for (i = 0; i < numJointValues; i++) {
 				JSONObject jointValue = jointValues.getJSONObject(i);
 				if (!jointValue.optString("type", "").equals("gear")) {
 					Joint joint = j2b2Joint(world, jointValue);
 					readCustomPropertiesFromJson(joint, jointValue);
 					m_joints.add(joint);
 				}
 			}
 			for (i = 0; i < numJointValues; i++) {
 				JSONObject jointValue = jointValues.getJSONObject(i);
 				if (jointValue.optString("type", "").equals("gear")) {
 					Joint joint = j2b2Joint(world, jointValue);
 					readCustomPropertiesFromJson(joint, jointValue);
 					m_joints.add(joint);
 				}
 			}
 		}
 
 		i = 0;
 		JSONArray imageValues = worldValue.optJSONArray("image");
 		if (null != imageValues) {
 			int numImageValues = imageValues.length();
 			for (i = 0; i < numImageValues; i++) {
 				JSONObject imageValue = imageValues.getJSONObject(i);
 				Image image = j2b2dJsonImage(imageValue);
 				readCustomPropertiesFromJson(image, imageValue);
 				m_images.add(image);
 			}
 		}
 
 		return world;
 	}
 
 	public Body j2b2Body(PhysicsWorld world, JSONObject bodyValue) throws JSONException {
 		BodyDef bodyDef = new BodyDef();
 		switch (bodyValue.getInt("type")) {
 		case 0:
 			bodyDef.type = BodyType.StaticBody;
 			break;
 		case 1:
 			bodyDef.type = BodyType.KinematicBody;
 			break;
 		case 2:
 			bodyDef.type = BodyType.DynamicBody;
 			break;
 		}
 		bodyDef.position.set(jsonToVec("position", bodyValue));
 		bodyDef.angle = jsonToFloat("angle", bodyValue);
 		bodyDef.linearVelocity.set(jsonToVec("linearVelocity", bodyValue));
 		bodyDef.angularVelocity = jsonToFloat("angularVelocity", bodyValue);
 		bodyDef.linearDamping = jsonToFloat("linearDamping", bodyValue, -1, 0);
 		bodyDef.angularDamping = jsonToFloat("angularDamping", bodyValue, -1, 0);
 		bodyDef.gravityScale = jsonToFloat("gravityScale", bodyValue, -1, 1);
 
 		bodyDef.allowSleep = bodyValue.optBoolean("allowSleep", true);
 		bodyDef.awake = bodyValue.optBoolean("awake", false);
 		bodyDef.fixedRotation = bodyValue.optBoolean("fixedRotation");
 		bodyDef.bullet = bodyValue.optBoolean("bullet", false);
 		bodyDef.active = bodyValue.optBoolean("active", true);
 
 		Body body = world.createBody(bodyDef);
 
 		String bodyName = bodyValue.optString("name", "");
 		if ("" != bodyName)
 			setBodyName(body, bodyName);
 
 		int i = 0;
 		JSONArray fixtureValues = bodyValue.optJSONArray("fixture");
 		if (null != fixtureValues) {
 			int numFixtureValues = fixtureValues.length();
 			for (i = 0; i < numFixtureValues; i++) {
 				JSONObject fixtureValue = fixtureValues.getJSONObject(i);
 				Fixture fixture = j2b2Fixture(body, fixtureValue);
 				readCustomPropertiesFromJson(fixture, fixtureValue);
 			}
 		}
 
 		// may be necessary if user has overridden mass characteristics
 		MassData massData = new MassData();
 		massData.mass = jsonToFloat("massData-mass", bodyValue);
 		massData.center.set(jsonToVec("massData-center", bodyValue));
 		massData.I = jsonToFloat("massData-I", bodyValue);
 		body.setMassData(massData);
 
 		return body;
 	}
 
 	Fixture j2b2Fixture(Body body, JSONObject fixtureValue) throws JSONException {
 
 		if (null == fixtureValue)
 			return null;
 
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.restitution = jsonToFloat("restitution", fixtureValue);
 		fixtureDef.friction = jsonToFloat("friction", fixtureValue);
 		fixtureDef.density = jsonToFloat("density", fixtureValue);
 		fixtureDef.isSensor = fixtureValue.optBoolean("sensor", false);
 
 		fixtureDef.filter.categoryBits = (short) fixtureValue.optInt("filter-categoryBits", 0x0001);
 		fixtureDef.filter.maskBits = (short) fixtureValue.optInt("filter-maskBits", 0xffff);
 		fixtureDef.filter.groupIndex = (short) fixtureValue.optInt("filter-groupIndex", 0);
 
 		Fixture fixture = null;
 		if (null != fixtureValue.optJSONObject("circle")) {
 			JSONObject circleValue = fixtureValue.getJSONObject("circle");
 			CircleShape circleShape = new CircleShape();
 			circleShape.setRadius(jsonToFloat("radius", circleValue));
 			circleShape.setPosition((jsonToVec("center", circleValue)));
 			fixtureDef.shape = circleShape;
 			fixture = body.createFixture(fixtureDef);
 		} else if (null != fixtureValue.optJSONObject("edge")) {
 			JSONObject edgeValue = fixtureValue.getJSONObject("edge");
 			EdgeShape edgeShape = new EdgeShape();
 			edgeShape.set(jsonToVec("vertex1", edgeValue), jsonToVec("vertex2", edgeValue));
 
 			// XXX Currently BOX2dAndEngineExtension supports only isolated EdgeShapes 
 //			edgeShape.m_hasVertex0 = edgeValue.optBoolean("hasVertex0", false);
 //			edgeShape.m_hasVertex3 = edgeValue.optBoolean("hasVertex3", false);
 //			if (edgeShape.m_hasVertex0)
 //				edgeShape.m_vertex0.set(jsonToVec("vertex0", edgeValue));
 //			if (edgeShape.m_hasVertex3)
 //				edgeShape.m_vertex3.set(jsonToVec("vertex3", edgeValue));
 
 			fixtureDef.shape = edgeShape;
 			fixture = body.createFixture(fixtureDef);
 		} else if (null != fixtureValue.optJSONObject("loop")) {// support old
 																// format (r197)
 			JSONObject chainValue = fixtureValue.getJSONObject("loop");
 			ChainShape chainShape = new ChainShape();
 			int numVertices = chainValue.getJSONArray("x").length();
 			Vector2 vertices[] = new Vector2[numVertices];
 			for (int i = 0; i < numVertices; i++)
 				vertices[i].set(jsonToVec("vertices", chainValue, i));
 			chainShape.createLoop(vertices);
 			fixtureDef.shape = chainShape;
 			fixture = body.createFixture(fixtureDef);
 		} else if (null != fixtureValue.optJSONObject("chain")) {
 			JSONObject chainValue = fixtureValue.getJSONObject("chain");
 			ChainShape chainShape = new ChainShape();
 			int numVertices = chainValue.getJSONObject("vertices").getJSONArray("x").length();
 			Vector2 vertices[] = new Vector2[numVertices];
 			for (int i = 0; i < numVertices; i++)
 				vertices[i] = jsonToVec("vertices", chainValue, i);
 			chainShape.createChain(vertices);
 			if (chainValue.optBoolean("hasPrevVertex", false)) {
 				chainShape.setPrevVertex(jsonToVec("prevVertex", chainValue));
 			}
 			if (chainValue.optBoolean("hasNextVertex", false)) {
 				chainShape.setPrevVertex(jsonToVec("nextVertex", chainValue));
 			}
 			fixtureDef.shape = chainShape;
 			fixture = body.createFixture(fixtureDef);
 		} else if (null != fixtureValue.optJSONObject("polygon")) {
 			JSONObject polygonValue = fixtureValue.getJSONObject("polygon");
 			int numVertices = polygonValue.getJSONObject("vertices").getJSONArray("x").length();
 			Vector2 vertices[] = new Vector2[numVertices];
 
 			if (numVertices > MAX_POLY_VERTS) {
 				System.out.println("Ignoring polygon fixture with too many vertices.");
 			} else if (numVertices < 2) {
 				System.out.println("Ignoring polygon fixture less than two vertices.");
 			} else if (numVertices == 2) {
 				System.out.println("Creating edge shape instead of polygon with two vertices.");
 				EdgeShape edgeShape = new EdgeShape();
 				edgeShape.set(jsonToVec("vertices", polygonValue, 0), jsonToVec("vertices", polygonValue, 1));
 				fixtureDef.shape = edgeShape;
 				fixture = body.createFixture(fixtureDef);
 			} else {
 				PolygonShape polygonShape = new PolygonShape();
 //				Log.d(getClass().getSimpleName(), "parsing poly n="+numVertices + "...");
 				for (int i = 0; i < numVertices; i++) {
 					vertices[i] = jsonToVec("vertices", polygonValue, i);
 //					Log.d(getClass().getSimpleName(), "v[" + i + "]=" + vertices[i]);
 				}
 				polygonShape.set(vertices);
 //				Log.d(getClass().getSimpleName(), "... done! parsed poly n="+numVertices);
 				fixtureDef.shape = polygonShape;
 				fixture = body.createFixture(fixtureDef);
 			}
 		}
 
 		String fixtureName = fixtureValue.optString("name", "");
 		if (fixtureName != "") {
 			setFixtureName(fixture, fixtureName);
 		}
 
 		return fixture;
 	}
 
 	Joint j2b2Joint(PhysicsWorld world, JSONObject jointValue) throws JSONException {
 		Joint joint = null;
 
 		int bodyIndexA = jointValue.getInt("bodyA");
 		int bodyIndexB = jointValue.getInt("bodyB");
 		if (bodyIndexA >= m_bodies.size() || bodyIndexB >= m_bodies.size())
 			return null;
 
 		// keep these in scope after the if/else below
 		RevoluteJointDef revoluteDef;
 		PrismaticJointDef prismaticDef;
 		DistanceJointDef distanceDef;
 		PulleyJointDef pulleyDef;
 		MouseJointDef mouseDef;
 		GearJointDef gearDef;
 		// WheelJointDef wheelDef;
 		WeldJointDef weldDef;
 		FrictionJointDef frictionDef;
 		// RopeJointDef ropeDef;
 
 		// will be used to select one of the above to work with
 		JointDef jointDef = null;
 
 		Vector2 mouseJointTarget = new Vector2(0, 0);
 		String type = jointValue.optString("type", "");
 		if (type.equals("revolute")) {
 			jointDef = revoluteDef = new RevoluteJointDef();
 			revoluteDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
 			revoluteDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
 			revoluteDef.referenceAngle = jsonToFloat("refAngle", jointValue);
 			revoluteDef.enableLimit = jointValue.optBoolean("enableLimit", false);
 			revoluteDef.lowerAngle = jsonToFloat("lowerLimit", jointValue);
 			revoluteDef.upperAngle = jsonToFloat("upperLimit", jointValue);
 			revoluteDef.enableMotor = jointValue.optBoolean("enableMotor", false);
 			revoluteDef.motorSpeed = jsonToFloat("motorSpeed", jointValue);
 			revoluteDef.maxMotorTorque = jsonToFloat("maxMotorTorque", jointValue);
 		} else if (type.equals("prismatic")) {
 			jointDef = prismaticDef = new PrismaticJointDef();
 			prismaticDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
 			prismaticDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
 			if (jointValue.has("localAxisA"))
 				prismaticDef.localAxisA.set(jsonToVec("localAxisA", jointValue));
 			else
 				prismaticDef.localAxisA.set(jsonToVec("localAxis1", jointValue));
 			prismaticDef.referenceAngle = jsonToFloat("refAngle", jointValue);
 			prismaticDef.enableLimit = jointValue.optBoolean("enableLimit");
 			prismaticDef.lowerTranslation = jsonToFloat("lowerLimit", jointValue);
 			prismaticDef.upperTranslation = jsonToFloat("upperLimit", jointValue);
 			prismaticDef.enableMotor = jointValue.optBoolean("enableMotor");
 			prismaticDef.motorSpeed = jsonToFloat("motorSpeed", jointValue);
 			prismaticDef.maxMotorForce = jsonToFloat("maxMotorForce", jointValue);
 		} else if (type.equals("distance")) {
 			jointDef = distanceDef = new DistanceJointDef();
 			distanceDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
 			distanceDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
 			distanceDef.length = jsonToFloat("length", jointValue);
 			distanceDef.frequencyHz = jsonToFloat("frequency", jointValue);
 			distanceDef.dampingRatio = jsonToFloat("dampingRatio", jointValue);
 		} else if (type.equals("pulley")) {
 			jointDef = pulleyDef = new PulleyJointDef();
 			pulleyDef.groundAnchorA.set(jsonToVec("groundAnchorA", jointValue));
 			pulleyDef.groundAnchorB.set(jsonToVec("groundAnchorB", jointValue));
 			pulleyDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
 			pulleyDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
 			pulleyDef.lengthA = jsonToFloat("lengthA", jointValue);
 			pulleyDef.lengthB = jsonToFloat("lengthB", jointValue);
 			pulleyDef.ratio = jsonToFloat("ratio", jointValue);
 		} else if (type.equals("mouse")) {
 			jointDef = mouseDef = new MouseJointDef();
 			mouseJointTarget = jsonToVec("target", jointValue);
 			mouseDef.target.set(jsonToVec("anchorB", jointValue));// alter after
 																	// creating
 																	// joint
 			mouseDef.maxForce = jsonToFloat("maxForce", jointValue);
 			mouseDef.frequencyHz = jsonToFloat("frequency", jointValue);
 			mouseDef.dampingRatio = jsonToFloat("dampingRatio", jointValue);
 		}
 		// Gear joints are apparently not implemented in JBox2D yet, but
 		// when they are, commenting out the following section should work.
 		/*
 		 * else if ( type.equals("gear") ) { jointDef = gearDef = new
 		 * GearJointDef(); int jointIndex1 = jointValue.getInt("joint1"); int
 		 * jointIndex2 = jointValue.getInt("joint2"); gearDef.joint1 =
 		 * m_joints.get(jointIndex1); gearDef.joint2 =
 		 * m_joints.get(jointIndex2); gearDef.ratio = jsonToFloat("ratio",
 		 * jointValue); }
 		 */
 		// Wheel joints are apparently not implemented in JBox2D yet, but
 		// when they are, commenting out the following section should work.
 		/*
 		 * else if ( type.equals("wheel") ) { jointDef = wheelDef = new
 		 * WheelJointDef(); wheelDef.localAnchorA.set( jsonToVec("anchorA",
 		 * jointValue) ); wheelDef.localAnchorB.set( jsonToVec("anchorB",
 		 * jointValue) ); wheelDef.localAxisA.set( jsonToVec("localAxisA",
 		 * jointValue) ); wheelDef.enableMotor =
 		 * jointValue.optBoolean("enableMotor",false); wheelDef.motorSpeed =
 		 * jsonToFloat("motorSpeed", jointValue); wheelDef.maxMotorTorque =
 		 * jsonToFloat("maxMotorTorque", jointValue); wheelDef.frequencyHz =
 		 * jsonToFloat("springFrequency", jointValue); wheelDef.dampingRatio =
 		 * jsonToFloat("springDampingRatio", jointValue); }
 		 */
 		// For now, we will make do with a revolute joint.
 		else if (type.equals("wheel")) {
 			jointDef = revoluteDef = new RevoluteJointDef();
 			revoluteDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
 			revoluteDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
 			revoluteDef.enableMotor = jointValue.optBoolean("enableMotor", false);
 			revoluteDef.motorSpeed = jsonToFloat("motorSpeed", jointValue);
 			revoluteDef.maxMotorTorque = jsonToFloat("maxMotorTorque", jointValue);
 		} else if (type.equals("weld")) {
 			jointDef = weldDef = new WeldJointDef();
 			weldDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
 			weldDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
 			weldDef.referenceAngle = 0;
 		} else if (type.equals("friction")) {
 			jointDef = frictionDef = new FrictionJointDef();
 			frictionDef.localAnchorA.set(jsonToVec("anchorA", jointValue));
 			frictionDef.localAnchorB.set(jsonToVec("anchorB", jointValue));
 			frictionDef.maxForce = jsonToFloat("maxForce", jointValue);
 			frictionDef.maxTorque = jsonToFloat("maxTorque", jointValue);
 		}
 		// Rope joints are apparently not implemented in JBox2D yet, but
 		// when they are, commenting out the following section should work.
 		/*
 		 * else if ( type.equals("rope") ) { jointDef = ropeDef = new
 		 * RopeJointDef(); ropeDef.localAnchorA.set( jsonToVec("anchorA",
 		 * jointValue) ); ropeDef.localAnchorB.set( jsonToVec("anchorB",
 		 * jointValue) ); ropeDef.maxLength = jsonToFloat("maxLength",
 		 * jointValue); }
 		 */
 
 		if (null != jointDef) {
 			// set features common to all joints
 			jointDef.bodyA = m_bodies.get(bodyIndexA);
 			jointDef.bodyB = m_bodies.get(bodyIndexB);
 			jointDef.collideConnected = jointValue.optBoolean("collideConnected", false);
 
 			joint = world.createJoint(jointDef);
 
 			if (type.equals("mouse"))
 				((MouseJoint) joint).setTarget(mouseJointTarget);
 
 			String jointName = jointValue.optString("name", "");
 			if (!jointName.equals("")) {
 				setJointName(joint, jointName);
 			}
 		}
 
 		return joint;
 	}
 
 	Image j2b2dJsonImage(JSONObject imageValue) throws JSONException {
 		Image img = new Image();
 
 		int bodyIndex = imageValue.optInt("body", -1);
 		if (-1 != bodyIndex)
 			img.body = lookupBodyFromIndex(bodyIndex);
 
 		String imageName = imageValue.optString("name", "");
 		if (!imageName.equals("")) {
 			img.name = imageName;
 			setImageName(img, imageName);
 		}
 
 		String fileName = imageValue.optString("file", "");
 		if (!fileName.equals(""))
 			img.file = fileName;
 
 		img.center = jsonToVec("center", imageValue);
 		img.angle = jsonToFloat("angle", imageValue);
 		img.scale = jsonToFloat("scale", imageValue);
 		img.opacity = jsonToFloat("opacity", imageValue);
 		img.renderOrder = jsonToFloat("renderOrder", imageValue);
 
 		img.flip = imageValue.optBoolean("flip", false);
 
 		img.filter = imageValue.optInt("filter", 1);
 
 		img.corners = new Vector2[4];
 		for (int i = 0; i < 4; i++)
 			img.corners[i] = jsonToVec("corners", imageValue, i);
 
 		JSONArray vertexPointerArray = imageValue.optJSONArray("glVertexPointer");
 		JSONArray texCoordArray = imageValue.optJSONArray("glVertexPointer");
 		if (null != vertexPointerArray && null != texCoordArray && vertexPointerArray.length() == texCoordArray.length()) {
 			int numFloats = vertexPointerArray.length();
 			img.numPoints = numFloats / 2;
 			img.points = new float[numFloats];
 			img.uvCoords = new float[numFloats];
 			for (int i = 0; i < numFloats; i++) {
 				img.points[i] = jsonToFloat("glVertexPointer", imageValue, i);
 				img.uvCoords[i] = jsonToFloat("glTexCoordPointer", imageValue, i);
 			}
 		}
 
 		JSONArray drawElementsArray = imageValue.optJSONArray("glDrawElements");
 		if (null != drawElementsArray) {
 			img.numIndices = drawElementsArray.length();
 			img.indices = new short[img.numIndices];
 			for (int i = 0; i < img.numIndices; i++)
 				img.indices[i] = (short) drawElementsArray.getInt(i);
 		}
 
 		return img;
 	}
 
 	float jsonToFloat(String name, JSONObject value) {
 		return jsonToFloat(name, value, -1, 0);
 	}
 
 	float jsonToFloat(String name, JSONObject value, int index) {
 		return jsonToFloat(name, value, index, 0);
 	}
 
 	float jsonToFloat(String name, JSONObject value, int index, float defaultValue) {
 		if (!value.has(name))
 			return defaultValue;
 
 		if (index > -1) {
 			JSONArray array = null;
 			try {
 				array = value.getJSONArray(name);
 			} catch (JSONException e) {
 			}
 			if (null == array)
 				return defaultValue;
 			Object obj = array.opt(index);
 			if (null == obj)
 				return defaultValue;
 			// else if ( value[name].isString() )
 			// return hexToFloat( value[name].asString() );
 			else
 				return ((Number) obj).floatValue();
 		} else {
 			Object obj = value.opt(name);
 			if (null == obj)
 				return defaultValue;
 			// else if ( value[name].isString() )
 			// return hexToFloat( value[name].asString() );
 			else
 				return ((Number) obj).floatValue();
 		}
 	}
 
 	Vector2 jsonToVec(String name, JSONObject value) throws JSONException {
 		return jsonToVec(name, value, -1, new Vector2(0, 0));
 	}
 
 	Vector2 jsonToVec(String name, JSONObject value, int index) throws JSONException {
 		return jsonToVec(name, value, index, new Vector2(0, 0));
 	}
 
 	Vector2 jsonToVec(String name, JSONObject value, int index, Vector2 defaultValue) throws JSONException {
 		Vector2 vec = defaultValue;
 
 		if (!value.has(name))
 			return defaultValue;
 
 		if (index > -1) {
 			JSONObject vecValue = value.getJSONObject(name);
 			JSONArray arrayX = vecValue.getJSONArray("x");
 			JSONArray arrayY = vecValue.getJSONArray("y");
 			// if ( arrayX[index].isString() )
 			// vec.x = hexToFloat(value[name]["x"][index].asString());
 			// else
 			vec.x = (float) arrayX.getDouble(index);
 
 			// if ( arrayX[index].isString() )
 			// vec.y = hexToFloat(value[name]["y"][index].asString());
 			// else
 			vec.y = (float) arrayY.getDouble(index);
 		} else {
 			JSONObject vecValue = value.optJSONObject(name);
 			if (null == vecValue)
 				return defaultValue;
 			else if (!vecValue.has("x")) // should be zero vector
 				vec.set(0, 0);
 			else {
 				vec.x = jsonToFloat("x", vecValue);
 				vec.y = jsonToFloat("y", vecValue);
 			}
 		}
 
 		return vec;
 	}
 
 	int jsonToInt(String name, JSONObject value) throws JSONException {
 		return jsonToInt(name, value, -1, 0);
 	}
 
 	int jsonToInt(String name, JSONObject value, int index) throws JSONException {
 		return jsonToInt(name, value, index, 0);
 	}
 
 	int jsonToInt(String name, JSONObject value, int index, int defaultValue) throws JSONException {
 		if (!value.has(name))
 			return defaultValue;
 
 		if (index > -1) {
 			JSONArray array = null;
 			try {
 				array = value.getJSONArray(name);
 			} catch (JSONException e) {
 			}
 			if (null == array)
 				return defaultValue;
 			Object obj = array.opt(index);
 			if (null == obj)
 				return defaultValue;
 			// else if ( value[name].isString() )
 			// return hexToFloat( value[name].asString() );
 			else
 				return ((Number) obj).intValue();
 		} else {
 			Object obj = value.opt(name);
 			if (null == obj)
 				return defaultValue;
 			// else if ( value[name].isString() )
 			// return hexToFloat( value[name].asString() );
 			else
 				return ((Number) obj).intValue();
 		}
 	}
 
 	public Body[] getBodiesByName(String name) {
 		Set<Body> keys = new HashSet<Body>();
 		for (Entry<Body, String> entry : m_bodyToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				keys.add(entry.getKey());
 			}
 		}
 		return keys.toArray(new Body[0]);
 	}
 
 	public Fixture[] getFixturesByName(String name) {
 		Set<Fixture> keys = new HashSet<Fixture>();
 		for (Entry<Fixture, String> entry : m_fixtureToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				keys.add(entry.getKey());
 			}
 		}
 		return keys.toArray(new Fixture[0]);
 	}
 
 	public Joint[] getJointsByName(String name) {
 		Set<Joint> keys = new HashSet<Joint>();
 		for (Entry<Joint, String> entry : m_jointToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				keys.add(entry.getKey());
 			}
 		}
 		return keys.toArray(new Joint[0]);
 	}
 
 	public Image[] getImagesByName(String name) {
 		Set<Image> keys = new HashSet<Image>();
 		for (Entry<Image, String> entry : m_imageToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				keys.add(entry.getKey());
 			}
 		}
 		return keys.toArray(new Image[0]);
 	}
 
 	public Vector<Image> getAllImages() {
 		return m_images;
 	}
 
 	public Body getBodyByName(String name) {
 		for (Entry<Body, String> entry : m_bodyToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	public Fixture getFixtureByName(String name) {
 		for (Entry<Fixture, String> entry : m_fixtureToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	public Joint getJointByName(String name) {
 		for (Entry<Joint, String> entry : m_jointToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	public Image getImageByName(String name) {
 		for (Entry<Image, String> entry : m_imageToNameMap.entrySet()) {
 			if (name.equals(entry.getValue())) {
 				return entry.getKey();
 			}
 		}
 		return null;
 	}
 
 	// //// custom properties
 
 	public CustomProperties getCustomPropertiesForItem(Object item, boolean createIfNotExisting) {
 		
 		if (m_customPropertiesMap.containsKey(item))
 			return m_customPropertiesMap.get(item);
 
 		if (!createIfNotExisting)
 			return null;
 
 		CustomProperties props = new CustomProperties();
 		m_customPropertiesMap.put(item, props);
 
 		return props;
 	}
 
 	// setCustomXXX
 
 	protected void setCustomInt(Object item, String propertyName, int val) {
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.put(propertyName, val);
 	}
 
 	protected void setCustomFloat(Object item, String propertyName, float val) {
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.put(propertyName, val);
 	}
 
 	protected void setCustomString(Object item, String propertyName, String val) {
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.put(propertyName, val);
 	}
 
 	protected void setCustomVector(Object item, String propertyName, Vector2 val) {
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.put(propertyName, val);
 	}
 
 	protected void setCustomBool(Object item, String propertyName, boolean val) {
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.put(propertyName, val);
 	}
 	
 	
 	public void setCustomInt(Body item, String propertyName, int val) {
 		m_bodiesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.put(propertyName, val);
 	}
 
 	public void setCustomFloat(Body item, String propertyName, float val) {		
 		m_bodiesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.put(propertyName, val);
 	}
 
 	public void setCustomString(Body item, String propertyName, String val) {
 		m_bodiesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.put(propertyName, val);
 	}
 
 	public void setCustomVector(Body item, String propertyName, Vector2 val) {
 		m_bodiesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.put(propertyName, val);
 	}
 
 	public void setCustomBool(Body item, String propertyName, boolean val) {
 		m_bodiesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.put(propertyName, val);
 	}
 	
 	
 	public void setCustomInt(Fixture item, String propertyName, int val) {
 		m_fixturesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.put(propertyName, val);
 	}
 
 	public void setCustomFloat(Fixture item, String propertyName, float val) {
 		m_fixturesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.put(propertyName, val);
 	}
 
 	public void setCustomString(Fixture item, String propertyName, String val) {
 		m_fixturesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.put(propertyName, val);
 	}
 
 	public void setCustomVector(Fixture item, String propertyName, Vector2 val) {
 		m_fixturesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.put(propertyName, val);
 	}
 
 	public void setCustomBool(Fixture item, String propertyName, boolean val) {
 		m_fixturesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.put(propertyName, val);
 	}
 	
 	
 	public void setCustomInt(Joint item, String propertyName, int val) {
 		m_jointsWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.put(propertyName, val);
 	}
 
 	public void setCustomFloat(Joint item, String propertyName, float val) {
 		m_jointsWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.put(propertyName, val);
 	}
 
 	public void setCustomString(Joint item, String propertyName, String val) {
 		m_jointsWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.put(propertyName, val);
 	}
 
 	public void setCustomVector(Joint item, String propertyName, Vector2 val) {
 		m_jointsWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.put(propertyName, val);
 	}
 
 	public void setCustomBool(Joint item, String propertyName, boolean val) {
 		m_jointsWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.put(propertyName, val);
 	}
 	
 	
 	public void setCustomInt(Image item, String propertyName, int val) {
 		m_imagesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_int.put(propertyName, val);
 	}
 
 	public void setCustomFloat(Image item, String propertyName, float val) {
 		m_imagesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_float.put(propertyName, val);
 	}
 
 	public void setCustomString(Image item, String propertyName, String val) {
 		m_imagesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_string.put(propertyName, val);
 	}
 
 	public void setCustomVector(Image item, String propertyName, Vector2 val) {
 		m_imagesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_vec2.put(propertyName, val);
 	}
 
 	public void setCustomBool(Image item, String propertyName, boolean val) {
 		m_imagesWithCustomProperties.add(item);
 		getCustomPropertiesForItem(item, true).m_customPropertyMap_bool.put(propertyName, val);
 	}
 
 	
 
 	// hasCustomXXX
 
 	public boolean hasCustomInt(Object item, String propertyName) {
 		return getCustomPropertiesForItem(item, false) != null &&
 				getCustomPropertiesForItem(item, false).m_customPropertyMap_int.containsKey(propertyName);
 	}
 
 	public boolean hasCustomFloat(Object item, String propertyName) {
 		return getCustomPropertiesForItem(item, false) != null &&
 				getCustomPropertiesForItem(item, false).m_customPropertyMap_float.containsKey(propertyName);
 	}
 
 	public boolean hasCustomString(Object item, String propertyName) {
 		return getCustomPropertiesForItem(item, false) != null &&
 				getCustomPropertiesForItem(item, false).m_customPropertyMap_string.containsKey(propertyName);
 	}
 
 	public boolean hasCustomVector(Object item, String propertyName) {
 		return getCustomPropertiesForItem(item, false) != null &&
 				getCustomPropertiesForItem(item, false).m_customPropertyMap_vec2.containsKey(propertyName);
 	}
 
 	public boolean hasCustomBool(Object item, String propertyName) {
 		return getCustomPropertiesForItem(item, false) != null &&
 				getCustomPropertiesForItem(item, false).m_customPropertyMap_bool.containsKey(propertyName);
 	}
 
 	// getCustomXXX
 
 	public int getCustomInt(Object item, String propertyName, int defaultVal) {
 		CustomProperties props = getCustomPropertiesForItem(item, false);
 		if (null == props)
 			return defaultVal;
 		if (props.m_customPropertyMap_int.containsKey(propertyName))
 			return (Integer) props.m_customPropertyMap_int.get(propertyName);
 		return defaultVal;
 	}
 
 	public float getCustomFloat(Object item, String propertyName, float defaultVal) {
 		CustomProperties props = getCustomPropertiesForItem(item, false);
 		if (null == props)
 			return defaultVal;
 		if (props.m_customPropertyMap_float.containsKey(propertyName))
 			return (Float)props.m_customPropertyMap_float.get(propertyName);
 		return defaultVal;
 	}
 
 	public String getCustomString(Object item, String propertyName, String defaultVal) {
 		CustomProperties props = getCustomPropertiesForItem(item, false);
 		if (null == props)
 			return defaultVal;
 		if (props.m_customPropertyMap_string.containsKey(propertyName))
 			return (String) props.m_customPropertyMap_string.get(propertyName);
 		return defaultVal;
 	}
 
 	public Vector2 getCustomVector(Object item, String propertyName, Vector2 defaultVal) {
 		CustomProperties props = getCustomPropertiesForItem(item, false);
 		if (null == props)
 			return defaultVal;
 		if (props.m_customPropertyMap_vec2.containsKey(propertyName))
 			return (Vector2) props.m_customPropertyMap_vec2.get(propertyName);
 		return defaultVal;
 	}
 
 	public boolean getCustomBool(Object item, String propertyName, boolean defaultVal) {
 		CustomProperties props = getCustomPropertiesForItem(item, false);
 		if (null == props)
 			return defaultVal;
 		if (props.m_customPropertyMap_bool.containsKey(propertyName))
 			return (Boolean) props.m_customPropertyMap_bool.get(propertyName);
 		return defaultVal;
 	}
 
 	// get by custom property value (vector version, body)
 	public int getBodiesByCustomInt(String propertyName, int valueToMatch, Vector<Body> items) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getBodiesByCustomFloat(String propertyName, float valueToMatch, Vector<Body> items) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getBodiesByCustomString(String propertyName, String valueToMatch, Vector<Body> items) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch) )
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getBodiesByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Body> items) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getBodiesByCustomBool(String propertyName, boolean valueToMatch, Vector<Body> items) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	// get by custom property value (single version, body)
 	Body getBodyByCustomInt(String propertyName, int valueToMatch) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Body getBodyByCustomFloat(String propertyName, float valueToMatch) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Body getBodyByCustomString(String propertyName, String valueToMatch) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
 				return item;
 		}
 		return null;
 	}
 
 	Body getBodyByCustomVector(String propertyName, Vector2 valueToMatch) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Body getBodyByCustomBool(String propertyName, boolean valueToMatch) {
 		Iterator<Body> iterator = m_bodiesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Body item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	// get by custom property value (vector version, Fixture)
 	public int getFixturesByCustomInt(String propertyName, int valueToMatch, Vector<Fixture> items) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getFixturesByCustomFloat(String propertyName, float valueToMatch, Vector<Fixture> items) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getFixturesByCustomString(String propertyName, String valueToMatch, Vector<Fixture> items) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getFixturesByCustomString(String propertyName, Vector<Fixture> items) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).length() > 0)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getFixturesByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Fixture> items) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getFixturesByCustomBool(String propertyName, boolean valueToMatch, Vector<Fixture> items) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	// get by custom property value (single version, Fixture)
 	Fixture getFixtureByCustomInt(String propertyName, int valueToMatch) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Fixture getFixtureByCustomFloat(String propertyName, float valueToMatch) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Fixture getFixtureByCustomString(String propertyName, String valueToMatch) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
 				return item;
 		}
 		return null;
 	}
 
 	Fixture getFixtureByCustomVector(String propertyName, Vector2 valueToMatch) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Fixture getFixtureByCustomBool(String propertyName, boolean valueToMatch) {
 		Iterator<Fixture> iterator = m_fixturesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Fixture item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	// get by custom property value (vector version, Joint)
 	public int getJointsByCustomInt(String propertyName, int valueToMatch, Vector<Joint> items) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getJointsByCustomFloat(String propertyName, float valueToMatch, Vector<Joint> items) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getJointsByCustomString(String propertyName, String valueToMatch, Vector<Joint> items) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getJointsByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Joint> items) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getJointsByCustomBool(String propertyName, boolean valueToMatch, Vector<Joint> items) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	// get by custom property value (single version, Joint)
 	Joint getJointByCustomInt(String propertyName, int valueToMatch) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Joint getJointByCustomFloat(String propertyName, float valueToMatch) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Joint getJointByCustomString(String propertyName, String valueToMatch) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
 				return item;
 		}
 		return null;
 	}
 
 	Joint getJointByCustomVector(String propertyName, Vector2 valueToMatch) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Joint getJointByCustomBool(String propertyName, boolean valueToMatch) {
 		Iterator<Joint> iterator = m_jointsWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Joint item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	// get by custom property value (vector version, Image)
 	public int getImagesByCustomInt(String propertyName, int valueToMatch, Vector<Image> items) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getImagesByCustomFloat(String propertyName, float valueToMatch, Vector<Image> items) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getImagesByCustomString(String propertyName, String valueToMatch, Vector<Image> items) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getImagesByCustomVector(String propertyName, Vector2 valueToMatch, Vector<Image> items) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	public int getImagesByCustomBool(String propertyName, boolean valueToMatch, Vector<Image> items) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				items.add(item);
 		}
 		return items.size();
 	}
 
 	// get by custom property value (single version, Image)
 	Image getImageByCustomInt(String propertyName, int valueToMatch) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomInt(item, propertyName) && getCustomInt( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Image getImageByCustomFloat(String propertyName, float valueToMatch) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomFloat(item, propertyName) && getCustomFloat( item, propertyName, 0 ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Image getImageByCustomString(String propertyName, String valueToMatch) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomString(item, propertyName) && getCustomString( item, propertyName, new String() ).equals(valueToMatch))
 				return item;
 		}
 		return null;
 	}
 
 	Image getImageByCustomVector(String propertyName, Vector2 valueToMatch) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomVector(item, propertyName) && getCustomVector( item, propertyName, new Vector2(0,0) ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	Image getImageByCustomBool(String propertyName, boolean valueToMatch) {
 		Iterator<Image> iterator = m_imagesWithCustomProperties.iterator();
 		while (iterator.hasNext()) {
 			Image item = iterator.next();
 			if (hasCustomBool(item, propertyName) && getCustomBool( item, propertyName, false ) == valueToMatch)
 				return item;
 		}
 		return null;
 	}
 
 	protected JSONArray writeCustomPropertiesToJson(Object item) throws JSONException {
 		JSONArray customPropertiesValue = new JSONArray();
 
 		CustomProperties props = getCustomPropertiesForItem(item, false);
 		if (null == props)
 			return customPropertiesValue;
 
 		int i = 0;
 
 		{
 			Iterator<Entry<String, Integer>> it = props.m_customPropertyMap_int.entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, Integer> pair = (Entry<String, Integer>) it.next();
 				JSONObject propValue = new JSONObject();
 				propValue.put("name", pair.getKey());
 				propValue.put("int", pair.getValue());
 				customPropertiesValue.put(i++, propValue);
 			}
 		}
 		{
 			Iterator<Entry<String, Float>> it = props.m_customPropertyMap_float.entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, Float> pair = (Entry<String, Float>) it.next();
 				JSONObject propValue = new JSONObject();
 				propValue.put("name", pair.getKey());
 				propValue.put("float", pair.getValue());
 				customPropertiesValue.put(i++, propValue);
 			}
 		}
 		{
 			Iterator<Entry<String, String>> it = props.m_customPropertyMap_string.entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, String> pair = (Entry<String, String>) it.next();
 				JSONObject propValue = new JSONObject();
 				propValue.put("name", pair.getKey());
 				propValue.put("string", pair.getValue());
 				customPropertiesValue.put(i++, propValue);
 			}
 		}
 		{
 			Iterator<Entry<String, Vector2>> it = props.m_customPropertyMap_vec2.entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, Vector2> pair = (Entry<String, Vector2>) it.next();
 				JSONObject propValue = new JSONObject();
 				propValue.put("name", pair.getKey());
 				vecToJson("vec2", pair.getValue(), propValue);
 				customPropertiesValue.put(i++, propValue);
 			}
 		}
 		{
 			Iterator<Entry<String, Boolean>> it = props.m_customPropertyMap_bool.entrySet().iterator();
 			while (it.hasNext()) {
 				Entry<String, Boolean> pair = (Entry<String, Boolean>) it.next();
 				JSONObject propValue = new JSONObject();
 				propValue.put("name", pair.getKey());
 				propValue.put("bool", pair.getValue());
 				customPropertiesValue.put(i++, propValue);
 			}
 		}
 
 		return customPropertiesValue;
 	}
 
 	protected void readCustomPropertiesFromJson(Body item, JSONObject value) throws JSONException {
 		if (null == item)
 			return;
 
 		if (!value.has("customProperties"))
 			return;
 
 		int i = 0;
 		JSONArray propValues = value.getJSONArray("customProperties");
 		if (null != propValues) {
 			int numPropValues = propValues.length();
 			for (i = 0; i < numPropValues; i++) {
 				JSONObject propValue = propValues.getJSONObject(i);
 				String propertyName = propValue.getString("name");
 				if (propValue.has("int"))
 					setCustomInt(item, propertyName, propValue.getInt("int"));
 				if (propValue.has("float"))
 					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
 				if (propValue.has("string"))
 					setCustomString(item, propertyName, propValue.getString("string"));
 				if (propValue.has("vec2"))
 					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
 				if (propValue.has("bool"))
 					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
 			}
 		}
 	}
 
 
 	protected void readCustomPropertiesFromJson(Fixture item, JSONObject value) throws JSONException {
 		if (null == item)
 			return;
 
 		if (!value.has("customProperties"))
 			return;
 
 		int i = 0;
 		JSONArray propValues = value.getJSONArray("customProperties");
 		if (null != propValues) {
 			int numPropValues = propValues.length();
 			for (i = 0; i < numPropValues; i++) {
 				JSONObject propValue = propValues.getJSONObject(i);
 				String propertyName = propValue.getString("name");
 				if (propValue.has("int"))
 					setCustomInt(item, propertyName, propValue.getInt("int"));
 				if (propValue.has("float"))
 					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
 				if (propValue.has("string"))
 					setCustomString(item, propertyName, propValue.getString("string"));
 				if (propValue.has("vec2"))
 					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
 				if (propValue.has("bool"))
 					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
 			}
 		}
 	}
 
 	protected void readCustomPropertiesFromJson(Joint item, JSONObject value) throws JSONException {
 		if (null == item)
 			return;
 
 		if (!value.has("customProperties"))
 			return;
 
 		int i = 0;
 		JSONArray propValues = value.getJSONArray("customProperties");
 		if (null != propValues) {
 			int numPropValues = propValues.length();
 			for (i = 0; i < numPropValues; i++) {
 				JSONObject propValue = propValues.getJSONObject(i);
 				String propertyName = propValue.getString("name");
 				if (propValue.has("int"))
 					setCustomInt(item, propertyName, propValue.getInt("int"));
 				if (propValue.has("float"))
 					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
 				if (propValue.has("string"))
 					setCustomString(item, propertyName, propValue.getString("string"));
 				if (propValue.has("vec2"))
 					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
 				if (propValue.has("bool"))
 					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
 			}
 		}
 	}
 
 	protected void readCustomPropertiesFromJson(Image item, JSONObject value) throws JSONException {
 		if (null == item)
 			return;
 
 		if (!value.has("customProperties"))
 			return;
 
 		int i = 0;
 		JSONArray propValues = value.getJSONArray("customProperties");
 		if (null != propValues) {
 			int numPropValues = propValues.length();
 			for (i = 0; i < numPropValues; i++) {
 				JSONObject propValue = propValues.getJSONObject(i);
 				String propertyName = propValue.getString("name");
 				if (propValue.has("int"))
 					setCustomInt(item, propertyName, propValue.getInt("int"));
 				if (propValue.has("float"))
 					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
 				if (propValue.has("string"))
 					setCustomString(item, propertyName, propValue.getString("string"));
 				if (propValue.has("vec2"))
 					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
 				if (propValue.has("bool"))
 					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
 			}
 		}
 	}
 
 	protected void readCustomPropertiesFromJson(PhysicsWorld item, JSONObject value) throws JSONException {
 		if (null == item)
 			return;
 
 		if (!value.has("customProperties"))
 			return;
 
 		int i = 0;
 		JSONArray propValues = value.getJSONArray("customProperties");
 		if (null != propValues) {
 			int numPropValues = propValues.length();
 			for (i = 0; i < numPropValues; i++) {
 				JSONObject propValue = propValues.getJSONObject(i);
 				String propertyName = propValue.getString("name");
 				if (propValue.has("int"))
 					setCustomInt(item, propertyName, propValue.getInt("int"));
 				if (propValue.has("float"))
 					setCustomFloat(item, propertyName, (float) propValue.getDouble("float"));
 				if (propValue.has("string"))
 					setCustomString(item, propertyName, propValue.getString("string"));
 				if (propValue.has("vec2"))
 					setCustomVector(item, propertyName, this.jsonToVec("vec2", propValue));
 				if (propValue.has("bool"))
 					setCustomBool(item, propertyName, propValue.getBoolean("bool"));
 			}
 		}
 	}
 
 }
