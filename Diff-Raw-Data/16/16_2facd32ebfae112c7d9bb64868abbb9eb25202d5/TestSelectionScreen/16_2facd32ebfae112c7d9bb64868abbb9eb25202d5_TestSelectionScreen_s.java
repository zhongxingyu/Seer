 package com.gemserk.libgdx.tests;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.List;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Window;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 
 public class TestSelectionScreen extends TestScreen {
 
 	private Skin skin;
 	private TextureAtlas atlas;
 	private Stage stage;
 
 	@Override
 	public void create() {
 		super.create();
 		atlas = new TextureAtlas(Gdx.files.internal("data/ui/uiskin.atlas"));
 		skin = new Skin(Gdx.files.internal("data/ui/uiskin.json"), atlas);
 
		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 		Gdx.input.setInputProcessor(stage);
 
 		Window window = new Window("SELECT A TEST", skin);
 
		window.setSize(700f, 400f);
		window.setPosition(50f, 40f);
 
 		final List list = new List(new String[] { "Hello", "World", "Bye" }, skin);
 		list.setTouchable(Touchable.enabled);
 
 		ScrollPane scrollPane = new ScrollPane(list);
 		scrollPane.setTouchable(Touchable.childrenOnly);
 
 		window.add(scrollPane).pad(10f).expand().fill();
 
 		window.row();
 		
 		TextButton startButton = new TextButton("START", skin);
 		startButton.addListener(new ClickListener() { 
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				super.clicked(event, x, y);
 				parent.setTestScreen(new BlendingPerformanceTestScreen());
 			}
 		});
 		
 		window.add(startButton).center().expandX().fillX();
 
 		stage.addActor(window);
 
 	}
 
 	@Override
 	public void render() {
 		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		stage.draw();
 	}
 
 	@Override
 	public void update() {
 		stage.act();
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		skin.dispose();
 		atlas.dispose();
 		stage.dispose();
 	}
 
 }
