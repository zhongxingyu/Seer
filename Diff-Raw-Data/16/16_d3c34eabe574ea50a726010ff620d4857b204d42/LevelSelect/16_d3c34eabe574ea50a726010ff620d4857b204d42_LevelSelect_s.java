 package com.dat255.Wood.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
 import com.dat255.Wood.WoodGame;
 import com.esotericsoftware.tablelayout.Cell;
 
 public class LevelSelect implements Screen {
 
 	private Stage stage;
 	private Skin skin;
 	private BitmapFont white;
 	private TextureAtlas atlas;
 	private Table table;
 	private ImageButton button1, button2;
 	private WoodGame game;
 	private Sprite backgroundSprite;
 	private SpriteDrawable backgroundSpriteDraw;
 
 
 	public LevelSelect(WoodGame game){
 		this.game = game;
 	}
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0, 0, 0, 1);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		stage.act(delta);
 		//table.drawDebug(stage);
 		stage.draw();
 
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		stage.setViewport(width, height, true);
 
 
 	}
 
 	@Override
 	public void show() {
 		stage = new Stage();
 
 		Gdx.input.setInputProcessor(stage);
 
 		atlas = new TextureAtlas("textures/button.pack");
 		skin = new Skin(atlas);
 		table = new Table(skin);
 		table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		
 		backgroundSprite = new Sprite(new Texture("images/MenuBackground.png"));
 		backgroundSpriteDraw = new SpriteDrawable(backgroundSprite);
 
		white = new BitmapFont(Gdx.files.internal("fonts/font.fnt"), false);
 		
 		TextButtonStyle levelTextButtonStyle = new TextButtonStyle();
 		levelTextButtonStyle.up = skin.getDrawable("buttonPlay.up");
 		levelTextButtonStyle.down = skin.getDrawable("buttonPlay.down");
 		levelTextButtonStyle.font = white;
 		
 		int k = 0;
 
 		final FileHandle[] files = Gdx.files.internal("levels/").list();
 
 		for(int i = 0; i < files.length; i++) {
 
 			final int index = i+1; //+1 is so that level1 is first, not level0
 			TextButton temp = new TextButton("" + index, levelTextButtonStyle);
 
 			temp.addListener(
 					new ClickListener()
 					{
 						@Override
 						public void clicked(InputEvent event, float x, float y)
 						{
 							game.setScreen(new GameScreen(index,game));
 						}
 					});
 
 			table.add(temp);
 			k++;
 			if(k > 3)
 			{
 				table.row();
 				k=0;
 			}
 			
 		}
 		
 		
 		for(Cell tempCell : table.getCells())
 		{
 			tempCell.height(Gdx.graphics.getWidth()/(table.getCells().size() + 1));
 			tempCell.width(Gdx.graphics.getWidth()/(table.getCells().size() + 1));
 			tempCell.space(10f);
 		}
 		table.setBackground(backgroundSpriteDraw);
 		table.validate();
 		stage.addActor(table);
 	}
 
 	@Override
 	public void hide() {
 
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void dispose() {
 		stage.dispose();
 		atlas.dispose();
 		skin.dispose();
 	}
 
 }
