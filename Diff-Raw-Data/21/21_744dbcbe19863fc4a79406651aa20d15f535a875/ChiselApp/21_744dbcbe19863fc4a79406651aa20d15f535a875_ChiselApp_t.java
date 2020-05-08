 package com.sastraxi.chisel;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.graphics.g3d.*;
 import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
 import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
 import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
 import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
 import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
 import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
 import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.utils.Array;
 import com.sastraxi.chisel.map.Brush;
 import com.sastraxi.chisel.math.HalfspacePolygon;
 
 public class ChiselApp implements ApplicationListener {
 
 	public static float FIELD_OF_VIEW = 90.0f;
 
 	private CameraInputController camController;
 	private PerspectiveCamera camera;
 	private Environment environment;
 
 	private Model box;
 	private ModelInstance boxInstance;
 	private Brush brush;
 
 	private ModelBatch batch;
 	private DefaultShaderProvider shaderProvider;
 
 	private Stage stage;
 
 	@Override
 	public void create() {
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 
 		Gdx.gl.glFrontFace(GL10.GL_CCW);
 		Gdx.gl.glEnable(GL10.GL_CULL_FACE);
 		Gdx.gl.glCullFace(GL10.GL_BACK);
 
 		camera = new PerspectiveCamera(FIELD_OF_VIEW, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		camera.position.set(10f, 10f, 10f);
 		camera.lookAt(0f, 0f, 0f);
 		camera.near = 0.1f;
 		camera.far = 300f;
 		camera.update();
 
 		camController = new CameraInputController(camera);
 		Gdx.input.setInputProcessor(camController);
 
 		environment = new Environment();
 		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
 		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
 
 		// the box, as made by libGDX
 		ModelBuilder modelBuilder = new ModelBuilder();
 		box = modelBuilder.createBox(5f, 5f, 5f,
 			  new Material(ColorAttribute.createDiffuse(Color.GREEN)),
 			  VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
 		boxInstance = new ModelInstance(box);
		boxInstance.transform.setToTranslation(-20.0f, 0f, -20f);
 
 		// the box, as made by us
 		Array<Plane> planes = new Array<Plane>();
 		planes.add(new Plane(new Vector3(0f,  1f, 0f), new Vector3(0f,  1f, 0f)));
 		planes.add(new Plane(new Vector3(0f, -1f, 0f), new Vector3(0f, -1f, 0f)));
 		planes.add(new Plane(new Vector3( 1f, 0f, 0f), new Vector3( 1f, 0f, 0f)));
 		planes.add(new Plane(new Vector3(-1f, 0f, 0f), new Vector3(-1f, 0f, 0f)));
 		planes.add(new Plane(new Vector3(0f, 0f,  1f), new Vector3(0f, 0f,  1f)));
 		planes.add(new Plane(new Vector3(0f, 0f, -1f), new Vector3(0f, 0f, -1f)));
 		brush = HalfspacePolygon.toConvex(planes);
 
		Quaternion q = new Quaternion();
		q.setEulerAngles(45f, 45f, 0f);
		System.out.println(q.transform(new Vector3(1f, 0f, 0f)));

 		shaderProvider = new DefaultShaderProvider();
 		batch = new ModelBatch(shaderProvider);
 
 		//createUI();
 	}
 
 	private void createUI() {
 
 		stage = new Stage();
 		Gdx.input.setInputProcessor(stage); // TODO multiplex input
 
 		Table table = new Table();
 		table.setFillParent(true);
 		stage.addActor(table);
 
 		// TODO Add widgets to the table here.
 
 	}
 
 	@Override
 	public void dispose() {
 		batch.dispose();
 		stage.dispose();
 		box.dispose();
 	}
 
 	@Override
 	public void render() {
 		camController.update();
 
 		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 
 		batch.begin(camera);
		batch.render(boxInstance, environment);
 		batch.render(brush, environment);
 		batch.end();
 
 		//stage.act(Gdx.graphics.getDeltaTime());
 		//stage.draw();
 
 		//Table.drawDebug(stage); // This is optional, but enables debug lines for tables.
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		//stage.setViewport(width, height, true);
 		camera.viewportWidth = (float) width;
 		camera.viewportHeight = (float) height;
 		camera.update();
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
