 package com.razh.tiling.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.razh.tiling.TilingGame;
import com.razh.tiling.ui.Carousel;
 
 public class LevelSelectScreen extends BasicScreen {
 
	private Carousel mCarousel;
 	private Table mContainer;
 
 	public LevelSelectScreen(TilingGame game) {
 		super(game);
 
 		Stage stage = new Stage();
 		setStage(stage);
 		addInputProcessor(stage);
 
 		stage.addListener(new InputListener() {
 			@Override
 			public boolean keyDown(InputEvent event, int keycode) {
 				if (keycode == Keys.BACK ||
 				    keycode == Keys.ESCAPE) {
 					getGame().setScreenByName("MAIN_MENU");
 					return true;
 				}
 
 				return false;
 			}
 		});
 
 		mContainer = new Table();
 		stage.addActor(mContainer);
 		mContainer.setFillParent(true);
 
 		Table table = new Table();
 
		mCarousel = new Carousel(table);
		mContainer.add(mCarousel);
 
 		Skin skin = new Skin();
 		skin.add("image", new NinePatch(new Texture(Gdx.files.internal("data/gray-50-alpha-50-square.png")), 1, 1, 1, 1));
 		skin.load(Gdx.files.internal("ui/buttons.json"));
 
 		table.pad(10.0f, 100.0f, 10.0f, 100.0f).defaults().space(40.0f);
 		Table subTable;
 		TextButton button;
 		TextButton testButton = null;
 		for (int i = 0; i < 5; i++) {
 			subTable = new Table();
 			subTable.padLeft(50.0f).padRight(50.0f).defaults().space(4);
 			for (int j = 0; j < 5; j++) {
 				for (int k = 0; k < 5; k++) {
 					button = new TextButton(i + "" + j + "" + k, skin);
 					button.pad(20.0f);
 
 					subTable.add(button);
 
 					if (i == 0 && j == 0 && k == 0) {
 						testButton = button;
 					}
 				}
 				subTable.row();
 			}
 			table.add(subTable);
 			System.out.println(table.getCell(subTable).getColumn() + ", " + table.getCell(subTable).getPrefWidth());
 		}
 
 		testButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				System.out.println("BUTTON PRESSED.");
 			}
 		});
 
 		System.out.println(table.getPrefWidth() + "," + table.getPrefHeight());
 		System.out.println(table.defaults().getSpaceLeft() + ", " + table.defaults().getSpaceRight());
 	}
 
 	@Override
 	public void render(float delta) {
 		getStage().act(delta);
 
 		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
 		getStage().draw();
 	}
 
 	@Override
 	public void resize(int width, int height) {}
 
 	@Override
 	public void pause() {}
 
 	@Override
 	public void resume() {}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 }
