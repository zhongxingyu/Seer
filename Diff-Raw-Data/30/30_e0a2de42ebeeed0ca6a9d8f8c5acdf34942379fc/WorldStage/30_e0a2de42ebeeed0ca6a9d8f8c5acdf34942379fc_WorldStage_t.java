 package se.chalmers.dat255.risk.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.risk.model.IProvince;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.input.GestureDetector.GestureListener;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 
 public class WorldStage extends AbstractStage implements GestureListener {
 	private Image background;
 	private Group provinceGroup;
 	private OrthographicCamera camera;
 	private float initialZoom;
 	private float width;
 	private float height;
 	private InputMultiplexer multi;
	private float zMax;
	private float zMin;
 
 	public WorldStage(List<IProvince> provinces, FileHandle positionsOnMap) {
 
 		background = new Image(Resource.getInstance().backGround);
 		camera = new OrthographicCamera();
 		provinceGroup = new Group();
 		multi = new InputMultiplexer(new GestureDetector(this), this);
 		camera.setToOrtho(false);
 
 		setCamera(camera);
 
 		width = background.getWidth();
 		height = background.getHeight();
 
 		camera.position.set(background.getWidth() / 2,
 				background.getHeight() / 2, 0);
 
 		views = new ArrayList<AbstractView>();
 
 		String wholeFile = positionsOnMap.readString();
 		String[] array = wholeFile.split("\\n");
 		int temp = 0;
 		for (String line : array) {
 			String[] lines = line.split("-");
 			String xCord = lines[0].trim();
 			String yCord = lines[1].trim();
 			int intXCord = Integer.parseInt(xCord);
 			int intYCord = Integer.parseInt(yCord);
 			ProvinceView provinceView = new ProvinceView(provinces.get(temp),
 					intXCord, intYCord);
 			views.add(provinceView);
 			temp++;
 		}
 
 		for (int i = 0; i < views.size(); i++) {
 			provinceGroup.addActor(views.get(i));
 		}
 
 		addActor(background);
 		addActor(provinceGroup);
		zMax = (background.getHeight()+background.getWidth()) /(Gdx.graphics.getWidth()+Gdx.graphics.getHeight());
		zMin = (Gdx.graphics.getWidth()+Gdx.graphics.getHeight())/(background.getHeight()+background.getWidth());
 	}
 
 	@Override
 	public InputProcessor getProcessor() {
 		return multi;
 	}
 
 	@Override
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		initialZoom = camera.zoom;
 		super.touchDown((int) x, (int) y, pointer, button);
		
 		return false;
 	}
 
 	@Override
 	public boolean tap(float x, float y, int count, int button) {
 
 		return false;
 	}
 
 	@Override
 	public boolean longPress(float x, float y) {
 		return false;
 	}
 
 	@Override
 	public boolean fling(float velocityX, float velocityY, int button) {
 		return false;
 	}
 
 	@Override
 	public boolean pan(float x, float y, float deltaX, float deltaY) {
 		camera.position.y += deltaY;
 		camera.position.x -= deltaX;
 		calcCam();
 		return false;
 	}
 
 	@Override
 	public boolean zoom(float initialDistance, float distance) {
 		float ratio = initialDistance / distance;
		if (initialZoom * ratio >= zMin && initialZoom * ratio <= zMax) {
 			camera.zoom = initialZoom * ratio;
 		}
 		calcCam();
 
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		initialZoom = camera.zoom;
 		float ratio = amount < 0 ? 0.9f : 1.1f;
		if (initialZoom * ratio >= zMin && initialZoom * ratio <= zMax) {
 			camera.zoom = initialZoom * ratio;
 		}
 
 		calcCam();
 		return false;
 	}
 
 	@Override
 	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
 			Vector2 pointer1, Vector2 pointer2) {
 		return false;
 	}
 
 	/*
 	 * This method is taken from this Url:
 	 * 
 	 * http://stackoverflow.com/questions/12039465/keep-libgdx-camera-inside-
 	 * boundaries-when-panning-and-zooming
 	 * 
 	 * and the code was supplied by user: Sean Clifford.
 	 */
 	private void calcCam() {
 		float camX = camera.position.x;
 		float camY = camera.position.y;
 
 		Vector2 camMin = new Vector2(camera.viewportWidth,
 				camera.viewportHeight);
 		camMin.scl(camera.zoom / 2); // bring to center and scale by the zoom
 		// level
 		Vector2 camMax = new Vector2(width, height);
 		camMax.sub(camMin); // bring to center
 
 		// keep camera within borders
 		camX = Math.min(camMax.x, Math.max(camX, camMin.x));
 		camY = Math.min(camMax.y, Math.max(camY, camMin.y));
 
 		camera.position.set(camX, camY, camera.position.z);
 
 	}
 
 }
