 package com.stuckinadrawer.dungeongame.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.stuckinadrawer.dungeongame.*;
 import com.stuckinadrawer.dungeongame.actors.Player;
 import com.stuckinadrawer.dungeongame.actors.enemies.Enemy;
 import com.stuckinadrawer.dungeongame.levelGeneration.LevelCreator;
 import com.stuckinadrawer.dungeongame.render.Renderer;
 
 public class GameScreen extends AbstractScreen {
 
     private OrthographicCamera camera;
     private Renderer renderer;
 
     private Level level;
     private Player player;
 
     Stage stage;
 
     float timer = 0;
 
     boolean playerMenuOpen = false;
 
     Table playerMenu;
     Label s;
     Label p;
     Label e;
     Label c;
     Label i;
     Label a;
     Label l;
 
     public GameScreen(DungeonGame dungeonGame) {
         super(dungeonGame);
 
         LevelCreator levelCreator = new LevelCreator();
         level = levelCreator.getNewLevel();
         player = level.getPlayer();
         camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 
         renderer = new Renderer(level, camera, font);
         //Gdx.input.setInputProcessor(new GestureDetector(new GestureDetection(camera, level)));
         camera.position.set(player.getPosition().getX()*Constants.TILE_SIZE, player.getPosition().getY()*Constants.TILE_SIZE, 0);
 
 
 
     }
     @Override
     public void show() {
         System.out.println("I'm showing!");
         stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
         GestureDetector gd = new GestureDetector(new GestureDetection(camera, level));
         InputMultiplexer im = new InputMultiplexer(stage, gd);
         Gdx.input.setInputProcessor(im);
 
 
         // TESTBUTTON
         final TextButton button = new TextButton("Heal Me!", skin);
         button.setPosition(10, 10);
         button.setSize(100, 25);
 
         stage.addActor(button);
 
         button.addListener(new ChangeListener() {
             @Override
             public void changed(ChangeEvent event, Actor actor) {
                 player.currentHP = player.maxHP;
                 player.healthbar.setValue(player.currentHP);
             }
         });
 
         final TextButton playerButton = new TextButton("Player Info", skin);
         playerButton.setSize(100, 25);
         playerButton.setPosition(Gdx.graphics.getWidth()-110, 25);
         stage.addActor(playerButton);
         playerButton.addListener(new ChangeListener() {
             @Override
             public void changed(ChangeEvent event, Actor actor) {
                 if(playerMenuOpen){
                     playerMenu.remove();
 
                 }else{
                     s.setText(player.strength+"");
                     p.setText(player.perception+"");
                     e.setText(player.endurance+"");
                     c.setText(player.charisma+"");
                     i.setText(player.intelligence+"");
                     a.setText(player.agility+"");
                     l.setText(player.luck+"");
 
 
 
                     stage.addActor(playerMenu);
 
                 }
                 playerMenuOpen = !playerMenuOpen;
             }
         });
 
         //HEALTHBAR
         Slider healthbar = new Slider(0, player.maxHP, 1, false, skin, "healthbar");
         healthbar.setSize(Gdx.graphics.getWidth()/3, 50);
         healthbar.setPosition(5, Gdx.graphics.getHeight() - 55);
         healthbar.setValue(player.maxHP);
         healthbar.setAnimateDuration(.5f);
         healthbar.setTouchable(Touchable.disabled);
 
         stage.addActor(healthbar);
 
         player.healthbar = healthbar;
 
 
         //XP BAR
         Slider XPBar = new Slider(0, player.XPToNextLevel, 1, false, skin, "XPBar");
         XPBar.setSize(Gdx.graphics.getWidth()-10, 50);
         XPBar.setPosition(5, Gdx.graphics.getHeight()-30);
         XPBar.setValue(player.currentXP);
         XPBar.setAnimateDuration(.5f);
         healthbar.setTouchable(Touchable.disabled);
         stage.addActor(XPBar);
         player.XPBar = XPBar;
 
 
 
         level.updateFOV();
 
 
         playerMenu = new Table(skin);
         playerMenu.add("You're SPECIAL!");
         playerMenu.row();
 
         s = new Label(player.strength+"", skin);
         playerMenu.add("Strength: ");
         playerMenu.add(s);
         playerMenu.row();
 
         p = new Label(player.perception+"", skin);
         playerMenu.add("Perception: ");
         playerMenu.add(p);
         playerMenu.row();
 
         e = new Label(player.endurance+"", skin);
         playerMenu.add("Endurance: ");
         playerMenu.add(e);
         playerMenu.row();
 
         c = new Label(player.charisma+"", skin);
         playerMenu.add("Charisma: ");
         playerMenu.add(c);
         playerMenu.row();
 
         i = new Label(player.intelligence+"", skin);
         playerMenu.add("Intelligence: ");
         playerMenu.add(i);
         playerMenu.row();
 
         a = new Label(player.agility+"", skin);
         playerMenu.add("Agility: ");
         playerMenu.add(a);
         playerMenu.row();
 
         l = new Label(player.luck+"", skin);
         playerMenu.add("Luck: ");
         playerMenu.add(l);
         playerMenu.row();
 
        playerMenu.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
 
         //stage.addActor(playerMenu);
 
 
 


     }
 
     @Override
     public void dispose(){
         stage.dispose();
     }
 
     private void update(float delta){
         timer+=delta;
         if(timer>0.1){
             //moves player if his movement queue is not empty
             if(player.action()){
                 camera.position.set(player.getPosition().getX()*Constants.TILE_SIZE, player.getPosition().getY()*Constants.TILE_SIZE, 0);
                 processTurn();
             }
             timer = 0;
         }
     }
 
     private void processTurn() {
         for(Enemy e: level.getEnemies()){
             if(e.dead){
                 //level.removeEnemy(e);
             }else{
                 e.doTurn(level);
             }
         }
 
     }
 
     @Override
     public void render(float delta) {
         super.render(delta);
         update(delta);
         stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
 
         camera.update();
         renderer.update(delta);
         stage.draw();

 
     }
 }
