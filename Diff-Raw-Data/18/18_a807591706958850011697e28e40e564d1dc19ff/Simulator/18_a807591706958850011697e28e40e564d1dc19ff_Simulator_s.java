 package bouncingballs;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 
 public class Simulator implements ApplicationListener, InputProcessor {
 	
 	private Model model;
 	private ShapeRenderer shapeRenderer;
 	private OrthographicCamera camera;
 
 	@Override
 	public void create() {
 		model = new Model();
 		shapeRenderer = new ShapeRenderer();
 		camera = new OrthographicCamera();
 		camera.update();
 		Gdx.input.setInputProcessor(this);
 	}
 
 	@Override
 	public void render() {
 		// Clear screen
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		// Update the model
 		// It's possible to get the delta time with Gdx.graphics.getDeltaTime().
 		// But we saw that it runs better with a delta that doesn't change
 		// between updates.
 		model.update(1.0f/60); 
 		
 		// Draw background for the ball area
 		shapeRenderer.begin(ShapeType.FilledRectangle);
 		shapeRenderer.setColor(Color.WHITE);
 		shapeRenderer.filledRect(-Constants.WIDTH / 2, -Constants.HEIGHT / 2, Constants.WIDTH, Constants.HEIGHT);
 		shapeRenderer.end();
 		
 		// Draw balls
 		shapeRenderer.begin(ShapeType.FilledCircle);
 		for (Ball ball : model.getBalls()) {
 			shapeRenderer.setColor(ball.getColor());
 			Vector2 position = ball.getPosition();
 			shapeRenderer.filledCircle(position.x, position.y, ball.getRadius());
 		}
 		shapeRenderer.end();
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		float aspectRatio = width / (float)height;
 		// If the window is wider than the model
 		if (aspectRatio > Constants.ASPECT_RATIO) {
 			camera.viewportHeight = Constants.HEIGHT;
 			camera.viewportWidth = Constants.WIDTH * (aspectRatio / Constants.ASPECT_RATIO);
 		} else {
 			camera.viewportHeight = Constants.HEIGHT * (Constants.ASPECT_RATIO / aspectRatio);
 			camera.viewportWidth = Constants.WIDTH;
 		}
 		camera.update();
 		shapeRenderer.setProjectionMatrix(camera.combined);
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 		shapeRenderer.dispose();
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (Input.Keys.ESCAPE == keycode) {
 			Gdx.app.exit();
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int arg0, int arg1) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		model.clickRegistered(screenX - Constants.WIDTH / 2 , Constants.HEIGHT / 2 - screenY);
 		return false;
 	}
 }
