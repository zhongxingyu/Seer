 package de.redlion.civilwar.controls;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Quaternion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.Ray;
 import de.redlion.civilwar.Constants;
 import de.redlion.civilwar.GameSession;
 import de.redlion.civilwar.SinglePlayerGameScreen;
 import de.redlion.civilwar.units.DefaultAI;
 import de.redlion.civilwar.units.PlayerSoldier;
 import de.redlion.civilwar.units.Soldier;
 
 public class DrawController extends GestureAdapter implements InputProcessor {
 	
 	public final OrthographicCamera camera;
 	final Vector3 curr = new Vector3();
 	public final Vector2 last = new Vector2(0, 0);
 	public final Vector2 delta = new Vector2();
 	
 	public static boolean fling = false;
 	private boolean oneFingerDown = false;
 	private boolean twoFingerDown = false;
 	private boolean threeFingerDown = false;
 	private boolean fourFingerDown = false;
 	private boolean fiveFingerDown = false;
 	private int howmanyfingers = 0;
 	
 	final Vector2 lastPoint = new Vector2();
 	
 //	final Dollar dollar = new Dollar(4);
 //	final DollarListener listener;
 //	final double MINSCORE = 0.82;
 	final float MAX_DISTANCE = 45.0f; //used for circleTesting
 	final int SMOOTHING_ITERATIONS = 2;
 	final float MIN_DISTANCE = 10.0f; //used for doodling
 	final int MAX_THICKNESS_VERTICAL = 7;
 	final int MAX_THICKNESS_HORIZONTAL = 5;
 	final int MIN_FLICK_VELOCITY = 3000;
 	
 	ArrayList<Vector3> deletePath = new ArrayList<Vector3>();
 	
 	Vector2 tmp = new Vector2();
 	ArrayList<Vector2> currentTriangleStrip = new ArrayList<Vector2>();
 	
 	Ray picker;
 	
 	Polygon picked;
 
 	public DrawController (final OrthographicCamera camera) {
 		this.camera = camera;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean touchDragged (int x, int y, int pointer) {		
 //		Gdx.app.log("fling", "" + "drag");
 		boolean move = false;
 		
 		if(Gdx.app.getType().equals(ApplicationType.Android) && howmanyfingers == 2 && pointer == 0)
 			move = true;
 		else if(Gdx.app.getType().equals(ApplicationType.Desktop) && SinglePlayerGameScreen.paused && Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
 			move = true;
 		
 		boolean draw = false;
 		
 		if(Gdx.app.getType().equals(ApplicationType.Android) && howmanyfingers == 1 && !fling && pointer == 0)
 			draw = true;
 		else if(Gdx.app.getType().equals(ApplicationType.Desktop) && SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
 				draw = true;
 		
 		boolean del = false;
 		
 		if(Gdx.app.getType().equals(ApplicationType.Android) && howmanyfingers == 1 && fling)
 			del = true;
 		else if(Gdx.app.getType().equals(ApplicationType.Desktop) && SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
 			del = true;
 		
 		boolean shooting = false;
 		
 		if(Gdx.app.getType().equals(ApplicationType.Desktop) && SinglePlayerGameScreen.paused && Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
 			if(picked != null && SinglePlayerGameScreen.circles.get(picked).get(0).ai.state.equals(DefaultAI.STATE.SHOOTING)) {
 				shooting = true;
 				del = false;
 				draw = false;
 				move = false;
 			}
 				
 		}
 		
 		if(move) {
 			delta.set(x, y).sub(last);
 			delta.mul(0.01f * Constants.MOVESPEED);
 			Vector3 temp = new Vector3(delta.y, 0, -delta.x);
 			Quaternion rotation = new Quaternion();
 			camera.combined.getRotation(rotation);
 			rotation.transform(temp);
 			camera.translate(temp);
 			camera.update();
 			
 			updateDoodles(x,y);
 			last.set(x, y);
 			
 			
 		}
 		else if(draw){
 			Vector2 temp = new Vector2(x, y);
 			
 			// convert to screen space
 			y = -y + Gdx.graphics.getHeight();			
 			
 			temp = new Vector2(x, y);
 			
 			if(temp.dst(lastPoint) > MIN_DISTANCE || SinglePlayerGameScreen.currentDoodle.isEmpty()) {
 				SinglePlayerGameScreen.currentDoodle.add(temp);
 				lastPoint.set(temp);
 				SinglePlayerGameScreen.currentTriStrip.clear();
 				makeTriangleStrip((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone(), false);
 				
 				SinglePlayerGameScreen.currentTriStrip.addAll(currentTriangleStrip);
 			}
 			last.set(x,y);
 			
 		}
 		else if(del) {
 			
 //			x = Math.max(Math.min(x, Gdx.graphics.getWidth()), 0);
 //			y = Math.max(Math.min(y, Gdx.graphics.getHeight()), 0);
 			
 			Vector2 temp = new Vector2(x, y);
 			
 			Vector3 projected = new Vector3();
 			
 			picker = camera.getPickRay(temp.x, temp.y);
 
 			Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
 			
 			deletePath.add(projected);
 			lastPoint.set(temp);
 			last.set(x,y);
 		}
 		else if(shooting) {
 			
 			Vector3 projected = new Vector3();
 			
 			picker = camera.getPickRay(x, y);
 
 			Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
 			
 			Vector2 median = new Vector2();
 			
 			for(PlayerSoldier p: SinglePlayerGameScreen.circles.get(picked)) {
 				median.add(p.position);
 			}
 			
 			median.div(SinglePlayerGameScreen.circles.get(picked).size());
 
 			for(PlayerSoldier p: SinglePlayerGameScreen.circles.get(picked)) {
 				if(p.alive) {
 					Vector2 temp = new Vector2(projected.x, projected.z);
 					temp.set(median.cpy().sub(temp));
 					temp.x = -temp.x;
 					temp.y = -temp.y;
 	
 					p.facing.set(temp);
 				}
 				
 			}
			SinglePlayerGameScreen.paths.remove(picked);
			SinglePlayerGameScreen.doodles.remove(picked);
 			
 		}
 		
 		if(howmanyfingers == 1 || (howmanyfingers >= 2 && pointer == 0))
 			last.set(x,y);
 		
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean touchUp (int x, int y, int pointer, int button) {
 		
 //		Gdx.app.log("fling", "up");		
 		switch(pointer) {
 		case 0: oneFingerDown = false;
 				howmanyfingers--;
 				break;
 		case 1: twoFingerDown = false;
 				howmanyfingers--;
 				break;
 		case 2: threeFingerDown = false;
 				howmanyfingers--;
 				break;
 		case 3: fourFingerDown = false;
 				howmanyfingers--;
 				break;
 		case 4: fiveFingerDown = false;
 				howmanyfingers--;
 				break;
 		default: break;
 		}
 		
 		if(howmanyfingers == 0)
 			last.set(0, 0);
 		
 		boolean draw = false;
 		
 		if(Gdx.app.getType().equals(ApplicationType.Android) && howmanyfingers == 0 && !fling && pointer == 0)
 			draw = true;
 		else if(Gdx.app.getType().equals(ApplicationType.Desktop) && SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
 				draw = true;
 		
 		boolean del = false;
 		
 		if(Gdx.app.getType().equals(ApplicationType.Android) && howmanyfingers == 0 && fling)
 			del = true;
 		else if(Gdx.app.getType().equals(ApplicationType.Desktop) && SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
 			del = true;
 		
 		if(draw) {
 //			!! commenting this might crash the game !!
 //			if(SinglePlayerGameScreen.currentDoodle.size()%2 != 0)
 //				SinglePlayerGameScreen.currentDoodle.add(SinglePlayerGameScreen.currentDoodle.get(SinglePlayerGameScreen.currentDoodle.size() - 1));
 			
 			ArrayList<Vector3> tempList = new ArrayList<Vector3>();
 			
 			if(!SinglePlayerGameScreen.currentDoodle.isEmpty()) {
 				for(int h=0;h<SMOOTHING_ITERATIONS;h++) {
 					ArrayList<Vector2> smoothedDoodle = smooth(SinglePlayerGameScreen.currentDoodle);
 					SinglePlayerGameScreen.currentDoodle.clear();
 					SinglePlayerGameScreen.currentDoodle = smoothedDoodle;
 				}
 				makeTriangleStrip((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone(),true);	
 			}	
 			
 			
 			for(Vector2 temp : SinglePlayerGameScreen.currentDoodle) {
 			
 				Vector3 projected = new Vector3();
 				
 				float yTemp = -temp.y + Gdx.graphics.getHeight();
 	
 				picker = camera.getPickRay(temp.x, yTemp);
 				
 				Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
 				
 				tempList.add(projected);
 			}
 			
 			if(SinglePlayerGameScreen.currentDoodle.size() > 11) {
 				if(SinglePlayerGameScreen.circles.isEmpty() && circleTest((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone())) {
 					
 					float[] points = new float[tempList.size() * 2 + 2];
 					
 					int i = 0;
 					for(Vector3 v : tempList) {
 					
 						points[i] = v.x;
 						points[i+1] = v.z;
 						
 						i+=2;
 						
 					}
 					
 					points[i] = tempList.get(0).x;
 					points[i+1] = tempList.get(0).z;		
 					
 					Polygon poly = new Polygon(points);
 					
 					ArrayList<PlayerSoldier> so = soldierTest(poly);
 					
 					if(!so.isEmpty()) {
 						SinglePlayerGameScreen.doodles.put(poly, (ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
 						SinglePlayerGameScreen.triangleStrips.put(poly, (ArrayList<Vector2>) currentTriangleStrip.clone());
 						SinglePlayerGameScreen.circles.put(poly, so);
 						SinglePlayerGameScreen.currentTriStrip.clear();
 //						("POLYGON ADDED: ", "Polygon Number: " + SinglePlayerGameScreen.circles.size() + " with id " + poly.toString());
 					}
 					else
 						SinglePlayerGameScreen.currentDoodle.clear();
 				}
 				else if(!SinglePlayerGameScreen.circles.isEmpty()) {
 					
 					if(circleTest((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone())) {
 					
 						float[] points = new float[tempList.size() * 2 + 2];
 						
 						int i = 0;
 						for(Vector3 v : tempList) {
 						
 							points[i] = v.x;
 							points[i+1] = v.z;
 							
 							i+=2;
 							
 						}
 						
 						points[i] = tempList.get(0).x;
 						points[i+1] = tempList.get(0).z;
 						
 						Polygon poly = new Polygon(points);
 						
 						boolean disjoint = checkDisjoint(poly);
 						
 						if(disjoint) {
 							ArrayList<PlayerSoldier> so = soldierTest(poly);
 							if(!so.isEmpty()) {
 								SinglePlayerGameScreen.doodles.put(poly, (ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
 								SinglePlayerGameScreen.triangleStrips.put(poly, (ArrayList<Vector2>) currentTriangleStrip.clone());
 								SinglePlayerGameScreen.currentTriStrip.clear();
 								SinglePlayerGameScreen.circles.put(poly, so);
 								
 //								("POLYGON ADDED: ", "Polygon Number: " + SinglePlayerGameScreen.circles.size() + " with id " + poly.toString());
 							}
 							else {
 								SinglePlayerGameScreen.currentDoodle.clear();
 								SinglePlayerGameScreen.currentTriStrip.clear();
 								currentTriangleStrip.clear();
 							}
 						}
 						else {
 							SinglePlayerGameScreen.currentDoodle.clear();
 							SinglePlayerGameScreen.currentTriStrip.clear();
 							currentTriangleStrip.clear();
 						}
 					}
 					else {
 						boolean deletedoodle = true;
 						for(Polygon p : SinglePlayerGameScreen.circles.keySet()) {
 							if(p.contains(tempList.get(0).x, tempList.get(0).z) && SinglePlayerGameScreen.paths.get(p) == null) {
 								
 								if(SinglePlayerGameScreen.circleHasPath.indexOf(p) == -1) {
 									//workaround because same polygon can't be in doodles twice
 									
 									Polygon pCopy = new Polygon(p.getTransformedVertices());
 									SinglePlayerGameScreen.circles.put(pCopy, SinglePlayerGameScreen.circles.get(p));
 									SinglePlayerGameScreen.doodles.put(pCopy,(ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
 									SinglePlayerGameScreen.triangleStrips.put(pCopy, (ArrayList<Vector2>) currentTriangleStrip.clone());
 									SinglePlayerGameScreen.paths.put(pCopy, (ArrayList<Vector3>) tempList.clone());
 									SinglePlayerGameScreen.currentTriStrip.clear();
 									
 									for(PlayerSoldier pS : SinglePlayerGameScreen.circles.get(p)) {
 										pS.wayPoints.clear();
 										pS.ai.setState(DefaultAI.STATE.MOVING);
 										pS.wayPoints.addAll(SinglePlayerGameScreen.paths.get(pCopy));
 										Vector3 direction = pS.wayPoints.get(pS.wayPoints.size()-2).cpy().sub(pS.wayPoints.get(pS.wayPoints.size()-1));
 										direction.nor();
 										direction.mul(-1000);
 										Vector3 last = pS.wayPoints.get(pS.wayPoints.size()-1).cpy().add(direction);
 										pS.wayPoints.add(last);
 									}
 									
 									SinglePlayerGameScreen.circleHasPath.add(p);
 									
 //									("PATH ADDED: ", "Path Number: " + SinglePlayerGameScreen.paths.size() + " from Polygon " + p.toString());
 //									("","" + SinglePlayerGameScreen.currentDoodle.get(0));
 									deletedoodle = false;
 									break;
 								}
 							}
 						}
 						if(deletedoodle) {
 							SinglePlayerGameScreen.currentDoodle.clear();
 							SinglePlayerGameScreen.currentTriStrip.clear();
 							currentTriangleStrip.clear();
 						}
 					}
 					
 				}
 				else {
 					SinglePlayerGameScreen.currentDoodle.clear();
 					SinglePlayerGameScreen.currentTriStrip.clear();
 					currentTriangleStrip.clear();
 				}
 			}
 			else {
 				SinglePlayerGameScreen.currentDoodle.clear();
 				SinglePlayerGameScreen.currentTriStrip.clear();
 				currentTriangleStrip.clear();
 			}
 			
 			tempList.clear();
 		}
 		else if(del) {			
 				
 			
 			ArrayList<Polygon> toDelete = new ArrayList<Polygon>();
 			ArrayList<Polygon> pathsToDelete = new ArrayList<Polygon>();
 			
 			//deletePath must start and end outside polygon but must contain at least one point inside polygon
 			boolean checkPoly = false;
 				
 			if(!deletePath.isEmpty()) {
 				
 				for(Polygon pol : SinglePlayerGameScreen.circles.keySet()) {
 					
 					if(!pol.contains(deletePath.get(0).x, deletePath.get(0).z) && !pol.contains(deletePath.get(deletePath.size()-1).x, deletePath.get(deletePath.size()-1).z))
 						checkPoly = true;
 					
 					if(checkPoly) {
 						for(Vector3 vec : deletePath) {
 							
 							if(pol.contains(vec.x, vec.z) && deletePath.indexOf(vec) != 0 && deletePath.indexOf(vec) != deletePath.size() -1) {
 								toDelete.add(pol);
 //								("", "Circle deleted :(");
 								break;
 							}
 							
 						}
 					}
 					
 				}
 				
 				for(Polygon polyg : SinglePlayerGameScreen.paths.keySet()) {
 					
 					ArrayList<Vector3> trail = SinglePlayerGameScreen.paths.get(polyg);	
 					if(!trail.isEmpty()) {
 						
 						if(trail.size()%2 != 0)
 							trail.add(trail.get(trail.size() - 1));
 						
 						Vector2 start = new Vector2(deletePath.get(0).x,deletePath.get(0).z);
 						Vector2 end = new Vector2(deletePath.get(deletePath.size()-1).x,deletePath.get(deletePath.size()-1).z);
 						
 						for(int i=0; i<trail.size();i+=2) {
 							
 							Vector2 point1 = new Vector2(trail.get(i).x,trail.get(i).z);
 							Vector2 point2 = new Vector2(trail.get(i+1).x,trail.get(i+1).z);
 							
 							if(Intersector.intersectSegments(point1, point2, start, end, new Vector2()))
 								pathsToDelete.add(polyg);
 							
 						}						
 					}					
 				}
 				
 			}
 			
 			if(!toDelete.isEmpty()) {
 				for(Polygon pop : toDelete) {
 
 					for(PlayerSoldier a : SinglePlayerGameScreen.circles.remove(pop)) {
 						a.circled  = false;
 						a.wayPoints.clear();
 					}
 					SinglePlayerGameScreen.doodles.remove(pop);
 					SinglePlayerGameScreen.paths.remove(pop);
 					
 				}
 			}
 			if(!pathsToDelete.isEmpty()) {
 				for(Polygon pop : pathsToDelete) {
 					SinglePlayerGameScreen.doodles.remove(pop);
 					SinglePlayerGameScreen.paths.remove(pop);
 					
 					if(!SinglePlayerGameScreen.circles.isEmpty()) {
 						for(PlayerSoldier pS : SinglePlayerGameScreen.circles.get(pop)) {
 							pS.wayPoints.clear();
 						}
 					}
 					
 				}
 			}
 		}
 		picked = null;
 		fling = false;
 		return false;
 	}
 	
 	@Override
 	public boolean touchDown (int x, int y, int pointer, int button) {
 //		Gdx.app.log("fling", "down 1 " + howmanyfingers);
 		switch(pointer) {
 		case 0: oneFingerDown = true;
 				howmanyfingers = 1;
 				break;
 		case 1: twoFingerDown = true;
 				howmanyfingers = 2;
 				break;
 		case 2: threeFingerDown = true;
 				howmanyfingers = 3;
 				break;
 		case 3: fourFingerDown = true;
 				howmanyfingers = 4;
 				break;
 		case 4: fiveFingerDown = true;
 				howmanyfingers = 5;
 				break;
 		default: break;
 		}
 		
 		if(howmanyfingers == 1  && pointer == 0) {
 			last.set(x, y);
 	
 			if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
 			//pick polygon
 			Vector3 projected = new Vector3();
 			
 			picker = camera.getPickRay(x, y);
 
 			Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
 			
 			
 				for(Polygon p : SinglePlayerGameScreen.circles.keySet()) {
 					if(p.contains(projected.x, projected.z)) {
 						picked = p;
 						for(PlayerSoldier pS: SinglePlayerGameScreen.circles.get(p)) {
 							pS.ai.setState(DefaultAI.STATE.SHOOTING);
 							pS.stop();
 						}
 					}
 					
 				}
 			}
 		}
 		
 		if(howmanyfingers == 5)
 			SinglePlayerGameScreen.paused = !SinglePlayerGameScreen.paused;
 	
 		if(howmanyfingers != 1) {
 			currentTriangleStrip.clear();
 			SinglePlayerGameScreen.currentDoodle.clear();
 			SinglePlayerGameScreen.currentTriStrip.clear();
 			deletePath.clear();
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public boolean scrolled (int amount) {
 		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 50;
 		camera.update();
 		
 //		("", amount + "");
 		
 		for(ArrayList<Vector2> doodle : SinglePlayerGameScreen.doodles.values()) {
 			
 			
 			ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
 			for(Vector2 v : doodle) {
 				
 				v.add(amount,amount);
 				
 				newDoodle.add(v);
 				
 			}
 			
 			doodle.clear();
 			doodle.addAll(newDoodle);
 			
 		}
 		
 		
 		return true;
 	}
 	
 	boolean circleTest(ArrayList<Vector2> doodle) {
 		
 		if(doodle.get(0).dst(doodle.get(doodle.size() -1)) < MAX_DISTANCE)
 			return true;
 		else
 			return false;
 	}
 	
 	ArrayList<PlayerSoldier> soldierTest(Polygon polygon) {
 		
 		ArrayList<PlayerSoldier> soldiers = new ArrayList<PlayerSoldier>();
 		
 		for(Soldier s : GameSession.getInstance().soldiers) {
 			if(s instanceof PlayerSoldier) {
 				PlayerSoldier p = (PlayerSoldier) s;
 				if(polygon.contains(p.position.x, p.position.y)) {
 					p.circled = true;
 					soldiers.add(p);
 				}
 			}
 		}
 		
 		return soldiers;
 	}
 	
 	boolean checkDisjoint(Polygon polygon) {
 		
 		float[] list = polygon.getTransformedVertices();
 		
 		for(int j=0;j<list.length;j+=2) {
 			for(Polygon po : SinglePlayerGameScreen.circles.keySet()) {
 				if(po.contains(list[j], list[j+1]))
 					return false;
 			}
 		}
 		
 		ArrayList<Polygon> contained = new ArrayList<Polygon>();
 		for(Polygon po : SinglePlayerGameScreen.circles.keySet()) {
 			list = po.getTransformedVertices();
 			for(int j=0;j<list.length;j+=2) {
 				if(polygon.contains(list[j], list[j+1]))
 					contained.add(po);
 			}
 		}
 		
 		if(!contained.isEmpty()) {
 			for(Polygon pop : contained) {
 				list = polygon.getTransformedVertices();
 				for(int j=0;j<list.length;j+=2) {
 					if(pop.contains(list[j], list[j+1]))
 						return false;
 				}
 				SinglePlayerGameScreen.circles.remove(pop);
 				SinglePlayerGameScreen.doodles.remove(pop);
 				SinglePlayerGameScreen.paths.remove(pop);
 			}
 			
 		}
 		
 		return true;
 	}
 	
 	private void updateDoodles(int x, int y) {
 		
 		Vector2 trans = new Vector2();
 		
 		trans.set(x,y).sub(last);
 		trans.y *= -1;		
 		
 		for(ArrayList<Vector2> doodle : SinglePlayerGameScreen.doodles.values()) {
 			
 			
 			ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
 			for(Vector2 v : doodle) {
 				
 				v.add(trans);
 				
 				newDoodle.add(v);
 				
 			}
 			
 			doodle.clear();
 			doodle.addAll(newDoodle);
 			
 		}
 		
 		for(ArrayList<Vector2> triStrip : SinglePlayerGameScreen.triangleStrips.values()) {
 			
 			
 			ArrayList<Vector2> newStrip = new ArrayList<Vector2>();
 			for(Vector2 v : triStrip) {
 				
 				v.add(trans);
 				
 				newStrip.add(v);
 				
 			}
 			
 			triStrip.clear();
 			triStrip.addAll(newStrip);
 			
 		}
 		
 	}
 	
 	private ArrayList<Vector2> smooth(ArrayList<Vector2> input) {
 		
 		ArrayList<Vector2> output = new ArrayList<Vector2>();
 		output.ensureCapacity(input.size() * 2);
 		
 		output.add(input.get(0));
 		
 		for (int i=0; i<input.size()-1; i++) {
 			Vector2 p0 = input.get(i);
 			Vector2 p1 = input.get(i+1);
 
 			Vector2 Q = new Vector2(0.75f * p0.x + 0.25f * p1.x, 0.75f * p0.y + 0.25f * p1.y);
 			Vector2 R = new Vector2(0.25f * p0.x + 0.75f * p1.x, 0.25f * p0.y + 0.75f * p1.y);
 	        	output.add(Q);
 		        output.add(R);
 		}
 		
 		output.add(input.get(input.size()-1));
 		
 		return output;
 		
 	}
 	
 	private void makeTriangleStrip(ArrayList<Vector2> input, boolean taper) {
 		
 		Vector2 p1 = new Vector2();
 //		Vector2 p2 = new Vector2();
 		tmp = new Vector2();
 		currentTriangleStrip.clear();
 		
 		if(input.size() % 2 != 0)
 			input.add(input.get(input.size()-1));
 		
 		float thickness_v = MAX_THICKNESS_VERTICAL;
 		float thickness_h = 5;
 		
 		
 		for(int j=0;j<input.size();j++) {
 			
 			p1 = input.get(j);
 //			p2 = input.get(j+1);
 			
 //			if(thickness < MAX_THICKNESS)
 //				thickness += (input.size() -j) / 100;
 			
 //			if(input.size() - j < 15 && taper && thickness_v > 0) {
 //				thickness_v -= (input.size() - j) / 12;
 //				thickness_h = 1;
 //			}
 			
 			
 //			tmp.set(p2).sub(p1).nor();
 			tmp.set(0,-1);
 			tmp.mul(thickness_v);
 			
 			Vector2 a = p1.cpy().add(tmp);
 			Vector2 b = p1.cpy().sub(tmp);
 			
 			tmp.set(1,0);
 			tmp.mul(thickness_h);
 			
 			a.add(tmp);
 			b.sub(tmp);
 
 			currentTriangleStrip.add(a);
 			currentTriangleStrip.add(b);
 
 			
 		}
 		
 	}
 	
 	@Override
 	public boolean touchDown (float x, float y, int pointer, int button) {
 //		Gdx.app.log("fling", "down 2 " + howmanyfingers);
 		currentTriangleStrip.clear();
 		SinglePlayerGameScreen.currentDoodle.clear();
 		SinglePlayerGameScreen.currentTriStrip.clear();
 		deletePath.clear();
 		
 		return false;
 	}
 	
 	@Override
 	public boolean fling(float velocityX, float velocityY, int button) {
 		
 		if((Math.abs(velocityY) + Math.abs(velocityX)) > MIN_FLICK_VELOCITY) {
 			fling = true;
 		}
 //		Gdx.app.log("fling", "" + fling);
 		return true;
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 }
