 package com.pkelly.screens;
 
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenManager;
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.pkelly.tween.ActorAccessor;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: PeKay
  * Date: 22/06/13
  * Time: 16:54
  * To change this template use File | Settings | File Templates.
  */
 public class MainMenu implements Screen
 {
 
     private Stage stage;
     private TextureAtlas textureAtlas;
     private Skin skin;
     private Table table;
     private TextButton exitButton, playButton;
     private BitmapFont white, black;
     private Label heading;
 
     private TweenManager tweenManager;
 
     @Override
     public void render(float delta)
     {
         Gdx.gl.glClearColor(0, 0, 0, 1);
         Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
         tweenManager.update(delta);
 
         stage.act(delta);
         stage.draw();
     }
 
     @Override
     public void resize(int width, int height)
     {
         stage.setViewport(width, height, true);
         table.invalidateHierarchy();
         table.setSize(width, height);
     }
 
     @Override
     public void show()
     {
         stage = new Stage();
 
         Gdx.input.setInputProcessor(stage);
 
        textureAtlas = new TextureAtlas("ui/button.pack");
 
         skin = new Skin(textureAtlas);
 
         table = new Table(skin);
         table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 
        white = new BitmapFont(Gdx.files.internal("font/white.fnt"), false);
        black = new BitmapFont(Gdx.files.internal("font/black.fnt"), false);
 
         LabelStyle labelHeading = new LabelStyle(white, Color.WHITE);
         heading = new Label("Breakout", labelHeading);
 
         exitButton = getTextButton("EXIT");
         exitButton.addListener(new ClickListener()
         {
             @Override
             public void clicked(InputEvent event, float x, float y)
             {
                 Gdx.app.exit();
             }
         });
 
         playButton = getTextButton("PLAY");
         playButton.addListener(new ClickListener()
         {
             @Override
             public void clicked(InputEvent event, float x, float y)
             {
                 ((Game) Gdx.app.getApplicationListener()).setScreen(new Play());
             }
         });
 
         table.add(heading);
         table.getCell(heading).spaceBottom(100);
         table.row();
         table.add(playButton);
         table.row();
         table.add(exitButton);
         stage.addActor(table);
 
         FadeInMenu();
 
     }
 
     private void FadeInMenu()
     {
         tweenManager = new TweenManager();
 
         Tween.registerAccessor(Actor.class, new ActorAccessor());
 
         heading.setColor(heading.getColor().r, heading.getColor().g, heading.getColor().b, 0);
         playButton.setColor(playButton.getColor().r, playButton.getColor().g, playButton.getColor().b, 0);
         exitButton.setColor(exitButton.getColor().r, exitButton.getColor().g, exitButton.getColor().b, 0);
 
         Timeline timeline = Timeline.createSequence().beginSequence() ;
         timeline.push(Tween.to(heading, ActorAccessor.ALPHA, .2f).target(1));
         timeline.push(Tween.to(playButton, ActorAccessor.ALPHA, .2f).target(1));
         timeline.push(Tween.to(exitButton, ActorAccessor.ALPHA, .2f).target(1));
         timeline.end();
         timeline.start(tweenManager);
     }
 
     private TextButton getTextButton(String text)
     {
         TextButton button = new TextButton(text, getTextButtonStyle());
         button.pad(16);
         return button;
     }
 
     private TextButtonStyle getTextButtonStyle()
     {
         TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
         textButtonStyle.up = skin.getDrawable("button.up");
         textButtonStyle.down = skin.getDrawable("button.focus.down");
         textButtonStyle.over = skin.getDrawable("button.focus.up");
         textButtonStyle.pressedOffsetX = 1;
         textButtonStyle.pressedOffsetY = -1;
         textButtonStyle.font = black;
         return textButtonStyle;
     }
 
     @Override
     public void hide()
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void pause()
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void resume()
     {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void dispose()
     {
         stage.dispose();
         textureAtlas.dispose();
         skin.dispose();
         white.dispose();
         black.dispose();
     }
 }
