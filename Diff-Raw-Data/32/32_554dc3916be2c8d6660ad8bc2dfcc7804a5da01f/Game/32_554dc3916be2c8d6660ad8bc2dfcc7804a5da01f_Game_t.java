 package com.example.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.xerxes.engine.game.*;
 import com.xerxes.engine.ui.*;
 import com.xerxes.engine.ui.animation.SpriteAnimation;
 import com.xerxes.engine.ui.animation.SpriteImageFrame;
 
 public class Game implements EntryPoint {
 
     private GameTimer timer;
 
     private CollisionRegister register;
 
     public void onModuleLoad() {
         timer = GameTimer.getInstance();
         register = CollisionRegister.getInstance();
         Charset charset = GWT.create(Charset.class);
         KeyboardController tankController = new KeyboardController();
        final GameScreen screen = new GameScreen(new Position(100, 100, 1), new Size(320, 240), "#000000", "#000000", new GameController[]{tankController});
         Sprite martianSprite = new Sprite("martian", 10, 10, 1);
         martianSprite.resize(22, 16);
         martianSprite.addImage("martian", charset.Martian1());
         martianSprite.addImage("martian2", charset.Martian2());
         Sprite tankSprite = new Sprite("tank", 0, 220, 1);
         tankSprite.addImage("tank0", charset.Tank());
         tankSprite.resize(26, 16);
         SpriteAnimation animation = new SpriteAnimation("martianMoving", true);
         animation.setFps(10);
         SpriteImageFrame frame = new SpriteImageFrame("martian");
         SpriteImageFrame secondFrame = new SpriteImageFrame("martian2");
         animation.addFrame(frame, 5);
         animation.addFrame(secondFrame, 6, 10);
         final Actor martian = new Actor(martianSprite);
         martian.addAnimation(animation);
         Actor tank = new Actor(tankSprite);
         tank.addAction("moveLeft", new ActorAction() {
             @Override
             public void doAction(Actor currentActor) {
                 Spriteable sprite = currentActor.getSprite();
                 double currentXPosition = sprite.getPosition().getX();
                 currentXPosition += 5;
                 sprite.getPosition().ChangeLocation(currentXPosition, sprite.getPosition().getY());
             }
         });
         tank.addAction("moveRight", new ActorAction() {
             @Override
             public void doAction(Actor currentActor) {
                 Spriteable sprite = currentActor.getSprite();
                 double currentXPosition = sprite.getPosition().getX();
                 currentXPosition -= 5;
                 sprite.getPosition().ChangeLocation(currentXPosition, sprite.getPosition().getY());
             }
         });
         tank.addAction("fire", new ActorAction() {
             @Override
             public void doAction(Actor currentActor) {
                 Charset charset = GWT.create(Charset.class);
                 Sprite shotSprite = new Sprite("bullet", 0, 0, 1);
                 Actor shotActor = new Actor(shotSprite);
                 register.registerCollision(martian, shotActor, new CollisionAction() {
                     @Override
                     public void doAction(Actor firstActor, Actor secondActor) {
                         Window.alert("shot gotcha!!!!! yeah!");
                     }
                 });
                 shotSprite.resize(4, 4);
                 shotSprite.addImage("shot", charset.Shot());
                 Spriteable sprite = currentActor.getSprite();
                 screen.addSprite(shotSprite);
                 double currentXPosition = sprite.getPosition().getX();
                 double currentYPosition = sprite.getPosition().getY();
                 currentYPosition -= 10;
                 shotSprite.changeLocation(currentXPosition + 10, currentYPosition);
                 shotSprite.render();
                 timer.addRepeatingActorEvent(new TimerActorEvent(new Actor[]{shotActor}, new TimerActorEventAction() {
                     @Override
                     public void doEventAction(Actor[] actors) {
                         Spriteable sprite = actors[0].getSprite();
                         Position shotPosition = sprite.getPosition();
                         double newY = shotPosition.getY() - 15;
                         shotPosition.ChangeLocation(shotPosition.getX(), newY);
                     }
                 }));
             }
         });
         tankController.addActor(tank);
         tankController.addOrModifyKeyEvent(39, "moveLeft");
         tankController.addOrModifyKeyEvent(37, "moveRight");
         tankController.addOrModifyKeyEvent(32, "fire");
         screen.addActor(martian);
         screen.addActor(tank);
         RootPanel.get().add(screen);
         screen.render();
         martian.render();
         martian.play("martianMoving");
         tank.render();
         CollisionRegister.getInstance().registerCollisionWithGameScreen(tank, screen, new GameScreenCollisionAction() {
             @Override
             public void doAction(Actor actor, GameScreen screen) {
                 Spriteable sprite = actor.getSprite();
                if(sprite.getPosition().getX() <= 0){
                     actor.disableAction("moveRight");
                 }
                 else{
                     actor.disableAction("moveLeft");
                 }
             }
         }, new GameScreenCollisionAction() {
                     @Override
                     public void doAction(Actor actor, GameScreen screen) {
                         actor.enableAction("moveRight");
                         actor.enableAction("moveLeft");
                     }
                 });
         timer.start();
     }
 
 }
