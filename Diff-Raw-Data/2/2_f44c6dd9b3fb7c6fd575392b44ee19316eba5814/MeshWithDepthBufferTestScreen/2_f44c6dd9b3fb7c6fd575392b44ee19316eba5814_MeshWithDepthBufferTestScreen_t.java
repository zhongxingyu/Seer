 package com.gemserk.libgdx.tests.customsprite;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes.Usage;
 import com.badlogic.gdx.graphics.g2d.PolygonRegion;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.gemserk.libgdx.tests.ShowStageInputProcessor;
 import com.gemserk.libgdx.tests.TestBaseWindow;
 import com.gemserk.libgdx.tests.TestScreen;
 
 public class MeshWithDepthBufferTestScreen extends TestScreen {
 
 	public static class MeshSpriteComparator implements Comparator<MeshSprite> {
 		@Override
 		public int compare(MeshSprite o1, MeshSprite o2) {
 			if (o1.getZ() > o2.getZ())
 				return -1;
 			if (o1.getZ() < o2.getZ())
 				return 1;
 			return 0;
 		}
 	}
 
 	public static class MeshSpriteInverseComparator implements Comparator<MeshSprite> {
 		@Override
 		public int compare(MeshSprite o1, MeshSprite o2) {
 			if (o1.getZ() > o2.getZ())
 				return 1;
 			if (o1.getZ() < o2.getZ())
 				return -1;
 			return 0;
 		}
 	}
 
 	TextureAtlas skinAtlas;
 	TextureAtlas atlas;
 	Skin skin;
 	Stage stage;
 
 	boolean blending;
 
 	OrthographicCamera camera;
 	MeshSpriteBatch meshSpriteBatch;
 
 	ShapeRenderer shapeRenderer;
 
 	ArrayList<MeshSprite> opaqueSprites;
 	ArrayList<MeshSprite> transparentSprites;
 
 	PolygonRegion insidePolygonDefinition;
 	PolygonRegion borderPolygonDefinition;
 	Texture texture;
 
 	@Override
 	public void create() {
 		Gdx.gl.glClearColor(0.69f, 0.86f, 0.81f, 1f);
 
 		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 
 		int width = Gdx.graphics.getWidth();
 		int height = Gdx.graphics.getHeight();
 
 		skinAtlas = new TextureAtlas(Gdx.files.internal("data/ui/uiskin.atlas"));
 		skin = new Skin(Gdx.files.internal("data/ui/uiskin.json"), skinAtlas);
 
 		atlas = new TextureAtlas(Gdx.files.internal("data/images/polygon.atlas"));
 
 		Sprite wormSprite = atlas.createSprite("worm");
 
 		texture = wormSprite.getTexture();
 
 		opaqueSprites = new ArrayList<MeshSprite>();
 		transparentSprites = new ArrayList<MeshSprite>();
 
 		insidePolygonDefinition = new PolygonRegion(wormSprite, Gdx.files.internal("data/polygons/worm-inside"));
 		borderPolygonDefinition = new PolygonRegion(wormSprite, Gdx.files.internal("data/polygons/worm-border"));
 
 		generateElements(100);
 
 		meshSpriteBatch = new MeshSpriteBatch();
 
 		stage = new Stage(width, height, false);
 
 		blending = false;
 
 		Table optionsContainer = new Table();
 		optionsContainer.setTransform(false);
 
 		{
 			{
 				final CheckBox actor = new CheckBox("Blending: " + blending, skin);
 				actor.setName("Blending");
 				actor.addListener(new ClickListener() {
 					@Override
 					public void clicked(InputEvent event, float x, float y) {
 						super.clicked(event, x, y);
 						blending = !blending;
 						actor.setText("Blending: " + blending);
 					}
 				});
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f).colspan(5);
 			}
 
 			optionsContainer.row();
 
 			{
 				TextButton actor = new TextButton("Sprites: " + opaqueSprites.size(), skin);
 				actor.setName("SpritesCount");
 				actor.setTouchable(Touchable.disabled);
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f).colspan(5);
 			}
 
 			optionsContainer.row();
 
 			{
 				TextButton actor = new TextButton("-", skin);
 				actor.addListener(new ClickListener() {
 					@Override
 					public void clicked(InputEvent event, float x, float y) {
 						super.clicked(event, x, y);
 						removeElements(50);
 					}
 				});
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f);
 			}
 
 			{
 				TextButton actor = new TextButton("+", skin);
 				actor.addListener(new ClickListener() {
 					@Override
 					public void clicked(InputEvent event, float x, float y) {
 						super.clicked(event, x, y);
 						generateElements(50);
 					}
 				});
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f);
 			}
 
 		}
 
 		Table baseWindowContainer = new TestBaseWindow("Options", skin, parent);
 
 		baseWindowContainer.setTransform(false);
 		baseWindowContainer.setSize(width * 0.8f, height * 0.8f);
 		baseWindowContainer.setPosition(width * 0.1f, height * 0.1f);
 
 		stage.addActor(baseWindowContainer);
 
 		Table innerContainer = (Table) baseWindowContainer.findActor(TestBaseWindow.INNER_CONTAINER_NAME);
 		innerContainer.add(optionsContainer).fill().expand();
 
 		stage.getRoot().setVisible(false);
 
 		Gdx.input.setInputProcessor(new InputMultiplexer(new ShowStageInputProcessor(stage, parent), stage));
 		Gdx.input.setCatchBackKey(true);
 
 		shapeRenderer = new ShapeRenderer();
 	}
 
 	private void generateElements(int count) {
 		for (int i = 0; i < count; i++) {
 			MeshSprite insideSprite = createMeshSprite(insidePolygonDefinition, texture);
 			MeshSprite borderSprite = createMeshSprite(borderPolygonDefinition, texture);
 
 			float z = -MathUtils.random(camera.near, camera.far);
 			float x = MathUtils.random(-Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getWidth() * 0.5f);
 			float y = MathUtils.random(-Gdx.graphics.getHeight() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
 
 			// float angle = MathUtils.random(0f, 360f);
 
 			insideSprite.setZ(z);
 			borderSprite.setZ(z);
 
 			insideSprite.setPosition(x, y);
 			borderSprite.setPosition(x, y);
 
 			// insideSprite.setRotation(angle);
 			// borderSprite.setRotation(angle);
 
 			opaqueSprites.add(insideSprite);
 			transparentSprites.add(borderSprite);
 		}
 
 		Collections.sort(opaqueSprites, new MeshSpriteComparator());
 		Collections.sort(transparentSprites, new MeshSpriteInverseComparator());
 
 		if (stage == null)
 			return;
 
 		TextButton button = (TextButton) stage.getRoot().findActor("SpritesCount");
 		if (button != null)
 			button.setText("Sprites: " + opaqueSprites.size());
 	}
 
 	private void removeElements(int count) {
 
 		while (!opaqueSprites.isEmpty() && count > 0) {
 			opaqueSprites.remove(0);
			transparentSprites.remove(transparentSprites.size() - 1);
 			count--;
 		}
 
 		TextButton button = (TextButton) stage.getRoot().findActor("SpritesCount");
 		if (button != null)
 			button.setText("Sprites: " + opaqueSprites.size());
 	}
 
 	private MeshSprite createMeshSprite(PolygonRegion wormInsidePolygon, Texture texture) {
 		return new MeshSprite(wormInsidePolygon.getLocalVertices(), wormInsidePolygon.getTextureCoords(), texture);
 	}
 
 	@Override
 	public void dispose() {
 		stage.dispose();
 		stage = null;
 		skin.dispose();
 		skin = null;
 		skinAtlas.dispose();
 		skinAtlas = null;
 		atlas.dispose();
 		atlas = null;
 		meshSpriteBatch.dispose();
 		meshSpriteBatch = null;
 		shapeRenderer.dispose();
 		shapeRenderer = null;
 	}
 
 	@Override
 	public void render() {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 		// Gdx.gl.glClearDepthf(1f);
 
 		meshSpriteBatch.setDepthTestEnabled(true);
 		meshSpriteBatch.disableBlending();
 
 		meshSpriteBatch.setProjectionMatrix(camera.combined);
 		meshSpriteBatch.begin();
 		for (int i = 0; i < opaqueSprites.size(); i++) {
 			MeshSprite sprite = opaqueSprites.get(i);
 			// sprite.setZ(0f);
 			meshSpriteBatch.draw(sprite.getTexture(), sprite.getVertices());
 		}
 		meshSpriteBatch.end();
 
 		meshSpriteBatch.setDepthTestEnabled(true);
 		if (blending)
 			meshSpriteBatch.enableBlending();
 		else
 			meshSpriteBatch.disableBlending();
 
 		meshSpriteBatch.begin();
 		for (int i = 0; i < transparentSprites.size(); i++) {
 			MeshSprite sprite = transparentSprites.get(i);
 			// sprite.setZ(0f);
 			meshSpriteBatch.draw(sprite.getTexture(), sprite.getVertices());
 		}
 		meshSpriteBatch.end();
 
 		meshSpriteBatch.setDepthTestEnabled(false);
 
 		stage.draw();
 	}
 
 	@Override
 	public void update() {
 		stage.act();
 	}
 
 	public static Mesh translate(Mesh mesh, float x, float y) {
 		VertexAttribute posAttr = mesh.getVertexAttribute(Usage.Position);
 		int offset = posAttr.offset / 4;
 		int numVertices = mesh.getNumVertices();
 		int vertexSize = mesh.getVertexSize() / 4;
 
 		float[] vertices = new float[numVertices * vertexSize];
 		mesh.getVertices(vertices);
 
 		int idx = offset;
 		for (int i = 0; i < numVertices; i++) {
 			vertices[idx] += x;
 			vertices[idx + 1] += y;
 			idx += vertexSize;
 		}
 
 		mesh.setVertices(vertices);
 
 		return mesh;
 	}
 
 }
