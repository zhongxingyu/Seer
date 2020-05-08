 package View;
 
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 
 
 public class WorldRender {
 	WorldHandler worldhandler;
 	
 	private OrthographicCamera camera;
 	
 	ShapeRenderer shapeRenderer;
 
 
 	// font stuff
     SpriteBatch spriteBatch;
     BitmapFont font;
     CharSequence str = "0123456789";
 	
 	
 	public WorldRender(WorldHandler world) {
 		// TODO Auto-generated constructor stub
 	this.worldhandler = world;
 	
 	camera = new OrthographicCamera(50, 50);
 	
 	camera.position.set(25,25, 0);
 	camera.zoom = 1.0f;
 	camera.update();
 	
 	shapeRenderer = new ShapeRenderer();
 	shapeRenderer.setProjectionMatrix(camera.combined);
 	
 	spriteBatch = new SpriteBatch();
 	font = new BitmapFont(Gdx.files.internal("data/fonts/test.fnt"),Gdx.files.internal("data/fonts/test.png"),false);
 	
 
 
 	}
 
 
 	public void render() {
 		// TODO Auto-generated method stub
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		
 	

 		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(99, 0, 0, 1);
 		mapRender();
 		shapeRenderer.end();
 		
 		
 		shapeRenderer.begin(ShapeType.Line);
 		renderOX();
 		shapeRenderer.end();
 		
spriteBatch.begin();
		
		winnerTextRendering();
		spriteBatch.end();

		
 		
 
 
 	}
 
 
 	private void winnerTextRendering() {
 		// TODO Auto-generated method stub
 
        
         switch (worldhandler.winner) {
         case 0:
         	//due nothing
         	break;
 		case 1:
 			font.setScale(0.4f, 1);
 			font.draw(spriteBatch, "the winner is player 1", 70, 200);
 			font.draw(spriteBatch, "H 	for new game", 70, 100);
 			break;
 		case 2:
 			font.setScale(0.4f, 1);
 			font.draw(spriteBatch, "the winner is player 2", 70, 200);
 			font.draw(spriteBatch, "H 	for new game", 70, 100);
 
 			break;
 			
 		}
 	}
 
 
 	private void renderOX() {
 		// TODO Auto-generated method stub
 		
 			for(int i=0;i<worldhandler.positionItem.length;i++){
 			
 			
 			switch (worldhandler.positionItem[i]) {
 			// top
 			case 0:
 				shapeRenderer.line(3, 47, 15.5f, 35);
 				shapeRenderer.line(3, 35, 15.5f, 47);
 				break;
 			case 1:
 				shapeRenderer.line(19, 47, 31.5f, 35);
 				shapeRenderer.line(19, 35, 31.5f, 47);
 				break;
 			case 2:
 				shapeRenderer.line(35, 47, 47.5f, 35);
 				shapeRenderer.line(35, 35, 47.5f, 47);
 				break;
 			// middle	
 			case 3:
 				shapeRenderer.line(3, 31, 15.5f, 20);
 				shapeRenderer.line(3, 20, 15.5f, 31);
 				break;
 			case 4:
 				shapeRenderer.line(19, 31, 31.5f, 20);
 				shapeRenderer.line(19, 20, 31.5f, 31);
 				break;
 			case 5:
 				shapeRenderer.line(35, 31, 47.5f, 20);
 				shapeRenderer.line(35, 20, 47.5f, 31);
 				break;
 				// lower
 			case 6:
 				shapeRenderer.line(3, 16, 15.5f, 3);
 				shapeRenderer.line(3, 3, 15.5f, 16);
 				break;
 			case 7:
 				shapeRenderer.line(19, 16, 31.5f, 3);
 				shapeRenderer.line(19, 3, 31.5f, 16);
 				break;
 			case 8:
 				shapeRenderer.line(35, 16, 47.5f, 3);
 				shapeRenderer.line(35, 3, 47.5f, 16);
 				break;
 				// top
 			case 9:
 				shapeRenderer.circle(8.66f, 41, 5,20);
 				break;
 			case 10:
 				shapeRenderer.circle(25, 41, 5,20);
 				break;
 			case 11:				
 				shapeRenderer.circle(41.5f, 41, 5,20);
 				break;
 				// middle
 			case 12:
 				shapeRenderer.circle(8.66f, 25.5f, 5,20);
 				break;
 			case 13:
 				shapeRenderer.circle(25, 25.5f, 5,20);
 				break;
 			case 14:
 				shapeRenderer.circle(41.5f, 25.5f, 5,20);
 				break;
 				// lower
 			case 15:
 				shapeRenderer.circle(8.66f, 10.5f, 5,20);
 				break;
 			case 16:
 				shapeRenderer.circle(25, 10.5f, 5,20);
 				break;
 			case 17:
 				shapeRenderer.circle(41.5f, 10.5f, 5,20);
 				break;
 		
 			
 			
 			default:
 				break;
 			}
 			}
 
 
 	}
 
 
 	private void mapRender() {
 		// TODO Auto-generated method stub
 		shapeRenderer.rect(0, 0, 2, 50);
 		shapeRenderer.rect(0, 48, 50, 2);
 		shapeRenderer.rect(48,0, 2, 50);
 		shapeRenderer.rect(0, 0, 50, 2);
 		
 		shapeRenderer.rect(16.33f, 0, 2, 50);
 		shapeRenderer.rect(32.666f, 0, 2, 50);
 		
 		shapeRenderer.rect(0, 17.33f, 50, 2);
 		shapeRenderer.rect(0, 32.33f, 50, 2);
 		
 		
 	}
 
 
 	public OrthographicCamera getCamera() {
 		return camera;
 	}
 
 
 	public void setCamera(OrthographicCamera camera) {
 		this.camera = camera;
 	}
 
 
 }
