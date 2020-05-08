 package org.andengine.extension.rubeloader.parser;
 
 import org.andengine.extension.rubeloader.json.AutocastMap;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.ChainShape;
 import com.badlogic.gdx.physics.box2d.CircleShape;
 import com.badlogic.gdx.physics.box2d.EdgeShape;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 
 
 public class ParserFixtureDef extends ParserDef<FixtureDef> {
 
 	public static int MAX_POLY_VERTS = 8;
 	@Override
 	protected FixtureDef doParse(AutocastMap pMap) {
 
 		if (null == pMap)
 			return null;
 
 		FixtureDef fixtureDef = new FixtureDef();
 
 		fixtureDef.restitution = pMap.getFloat("restitution", 0.0f);
		fixtureDef.friction = pMap.getFloat("friction");
 		fixtureDef.density = pMap.getFloat("density", 0);
 		fixtureDef.isSensor = pMap.getBool("sensor", false);
 		fixtureDef.filter.categoryBits = (short) pMap.getInt("filter-categoryBits", 0x0001);
 		fixtureDef.filter.maskBits = (short) pMap.getInt("filter-maskBits", 0xffff);
 		fixtureDef.filter.groupIndex = (short) pMap.getInt("filter-groupIndex", 0);
 
 		if (pMap.has("circle")) {
 			AutocastMap circleValue = (AutocastMap) pMap.get("circle");
 			CircleShape circleShape = new CircleShape();
 			circleShape.setRadius(circleValue.getFloat("radius"));
 			circleShape.setPosition(circleValue.getVector2("center"));
 			fixtureDef.shape = circleShape;
 		} else if (pMap.has("edge")) {
 			AutocastMap edgeValue = (AutocastMap) pMap.get("edge");
 			EdgeShape edgeShape = new EdgeShape();
 			edgeShape.set(edgeValue.getVector2("vertex1"), edgeValue.getVector2("vertex2"));
 
 			// XXX Currently BOX2dAndEngineExtension supports only isolated EdgeShapes 
 			//	edgeShape.m_hasVertex0 = edgeValue.optBoolean("hasVertex0", false);
 			//	edgeShape.m_hasVertex3 = edgeValue.optBoolean("hasVertex3", false);
 			//	if (edgeShape.m_hasVertex0)
 			//		edgeShape.m_vertex0.set(jsonToVec("vertex0", edgeValue));
 			//	if (edgeShape.m_hasVertex3)
 			//		edgeShape.m_vertex3.set(jsonToVec("vertex3", edgeValue));
 
 			fixtureDef.shape = edgeShape;
 		} else if (pMap.has("loop")) { // support old format r197
 			AutocastMap chainValue = (AutocastMap) pMap.get("loop");
 			Vector2[] vertices = chainValue.getVector2Array("vertices");
 
 			ChainShape chainShape = new ChainShape();
 			chainShape.createLoop(vertices);
 			fixtureDef.shape = chainShape;
 		} else if (pMap.has("chain")) {
 			AutocastMap chainValue = (AutocastMap) pMap.get("chain");
 			Vector2[] vertices = chainValue.getVector2Array("vertices");
 
 			ChainShape chainShape = new ChainShape();
 			chainShape.createChain(vertices);
 			if (chainValue.getBool("hasPrevVertex", false)) {
 				chainShape.setPrevVertex(chainValue.getVector2("prevVertex"));
 			}
 			if (chainValue.getBool("hasNextVertex", false)) {
 				chainShape.setPrevVertex(chainValue.getVector2("nextVertex"));
 			}
 			fixtureDef.shape = chainShape;
 		} else if (pMap.has("polygon")) {
 			AutocastMap polygonValue = (AutocastMap) pMap.get("polygon");
 			Vector2[] vertices = polygonValue.getVector2Array("vertices");
 
 			int numVertices = vertices.length;
 			if (numVertices > MAX_POLY_VERTS) {
 				System.out.println("Ignoring polygon fixture with too many vertices.");
 			} else if (numVertices < 2) {
 				System.out.println("Ignoring polygon fixture less than two vertices.");
 			} else if (numVertices == 2) {
 				System.out.println("Creating edge shape instead of polygon with two vertices.");
 				EdgeShape edgeShape = new EdgeShape();
 				edgeShape.set(vertices[0], vertices[1]);
 				fixtureDef.shape = edgeShape;
 			} else {
 				PolygonShape polygonShape = new PolygonShape();
 				polygonShape.set(vertices);
 				fixtureDef.shape = polygonShape;
 			}
 		}
 
 		return fixtureDef;
 	}
 }
