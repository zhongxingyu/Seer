 package de.redlion.rts;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputAdapter;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Quaternion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.dollar.Dollar;
 import com.dollar.DollarListener;
 
 public class DrawController extends InputAdapter{
 
 	final OrthographicCamera camera;
 	final Vector3 curr = new Vector3();
 	final Vector2 last = new Vector2(0, 0);
 	final Vector2 delta = new Vector2();
 	
 	final Vector2 lastPoint = new Vector2();
 	
 	final Dollar dollar = new Dollar(4);
 	final DollarListener listener;
 	final double MINSCORE = 0.6;
 
 	public DrawController (final OrthographicCamera camera) {
 		this.camera = camera;
 		listener  = new DollarListener() {
 			
 			@Override
 			public void dollarDetected(Dollar dollar) {
 				// TODO Auto-generated method stub
 //				Gdx.app.log("", dollar.getName() + " " + dollar.getScore());
 				
 				if((dollar.getName().equals("circle CW") || dollar.getName().equals("circle CCW")) && dollar.getScore() > MINSCORE) {
 					int[] rec = dollar.getBounds();
 					
 //					Rectangle rec = dollar.getBoundingBox();
 					
 					int x = rec[0];
 					int y = rec[1];
 					int x1 = rec[2];
 					int y1 = rec[3];
 					
					y = -y + Gdx.graphics.getHeight();
					y1 = -y1 + Gdx.graphics.getHeight();
					
 					SinglePlayerGameScreen.circleRay = camera.getPickRay(dollar.getPosition().x,-dollar.getPosition().y+Gdx.graphics.getHeight());
 					
 					
 					Vector3 p1 = new Vector3(x,y,0);
 					Vector3 p2 = new Vector3(x1,y1,0);
				
					camera.unproject(p1);
					camera.unproject(p2);
					
//					Gdx.app.log("", "" + x + " " + y);
					
//					float width = rec.width;
//					float height = rec.height;
 					
//					double d = (width * width) + (height * height);
 					double d = p1.dst(p2);
 					
 					double rad = Math.sqrt(d) / 2;
 					
 					SinglePlayerGameScreen.circleRadius = (float) rad;
 					
 				}
 			}
 		};
 		
 		dollar.setListener(listener);
 	}
 
 	@Override
 	public boolean touchDragged (int x, int y, int pointer) {
 		y = -y + Gdx.graphics.getHeight();
 		
 		if(SinglePlayerGameScreen.paused && Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ) {
 			delta.set(x, y).sub(last);
 			delta.mul(0.01f);
 			Vector3 temp = new Vector3(-delta.x,delta.y,0);
 			Quaternion rotation = new Quaternion();
 			camera.combined.getRotation(rotation);
 			rotation.transform(temp);
 			camera.translate(temp);
 			camera.update();
 			last.set(x, y);
 		}
 		else {
 			Vector2 temp = new Vector2(x, y);
 			if(temp.dst(lastPoint) > 10) {
 //				Gdx.app.log("point", temp.toString());
 				Vector3 projected = new Vector3();
 				camera.unproject(projected.set(temp.x,temp.y,0));
 //				Gdx.app.log("proj", projected.toString());		
 				SinglePlayerGameScreen.path.put(temp,projected);
 				lastPoint.set(temp);
 				dollar.pointerDragged(x, y);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean touchUp (int x, int y, int pointer, int button) {
 		last.set(0, 0);
 		
 		if(SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
 			//add dummy point
 			SinglePlayerGameScreen.path.put(new Vector2(-1,-1),new Vector3(-1,-1,-1));
 			dollar.pointerReleased(x, y);
 			dollar.setActive(false);
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public boolean touchDown (int x, int y, int pointer, int button) {
 		last.set(x, y);
 		dollar.pointerPressed(x, y);
 		dollar.setActive(true);
 		return true;
 	}
 	
 	@Override
 	public boolean scrolled (int amount) {
 		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 50;
 		camera.update();
 		return true;
 	}
 }
