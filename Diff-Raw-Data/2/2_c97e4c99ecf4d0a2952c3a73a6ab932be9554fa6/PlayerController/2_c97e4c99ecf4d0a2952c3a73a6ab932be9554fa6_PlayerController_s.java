 package com.game.rania.controller;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.Config;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.Planet;
 import com.game.rania.model.Player;
 import com.game.rania.model.Star;
 import com.game.rania.model.Target;
 import com.game.rania.model.User;
 import com.game.rania.screen.part.Parts;
 import com.game.rania.view.Camera;
 
 public class PlayerController extends UpdateController
 {
 
   private Vector2 touchPoint = new Vector2(0, 0);
 
   private Player  player  = null;
   private Camera  camera  = null;
 
   public PlayerController(Player player)
   {
     this.player = player;
     camera = RaniaGame.mView.getCamera();
   }
 
   @Override
   public void stopContoller()
   {
     player.stop();
   }
 
   @Override
   public boolean touchUp(int x, int y, int pointer, int button)
   {
     touchPoint.set(x, Gdx.graphics.getHeight() - y);
     RaniaGame.mView.getCamera().toCameraCoord(touchPoint);
 
     Target currentTarget = getTarget(touchPoint.x, touchPoint.y);
     if (currentTarget.type != Target.none &&
         (currentTarget.id != player.target.id ||
         currentTarget.type != player.target.type))
     {
       player.target = currentTarget;
       Parts.getInfoPanel().setTargetInfo(currentTarget);
       Controllers.netController.sendTarget(currentTarget);
       return true;
     }
 
    Controllers.netController.sendTouchPoint((int) touchPoint.x, (int) touchPoint.y, (int) player.position.x, (int) player.position.y);
     player.setPositionTarget(touchPoint);
 
     return true;
   }
 
   protected Vector2 deltaPosition = new Vector2();
 
   @Override
   public void update(float deltaTime)
   {
     if (player != null)
     {
       camera.position.set(player.position.x, player.position.y, 0);
 
       if (player.target.type != Target.none)
       {
         deltaPosition.set(player.target.object.position);
         deltaPosition.sub(player.position);
         if (deltaPosition.len() > Config.nebulaRadius || !RaniaGame.mController.getObjects().contains(player.target.object))
         {
           player.target = new Target(0, Target.none, null);
           Controllers.netController.sendTarget(player.target);
         }
       }
     }
   }
 
   protected Target getTarget(float x, float y)
   {
     for (User user : Controllers.locController.getUsers())
     {
       if (user.intersectObject(x, y))
       {
         return new Target(user.id, Target.user, user);
       }
     }
 
     for (Planet planet : Controllers.locController.getPlanets())
     {
       if (planet.intersectObject(x, y))
       {
         return new Target(planet.id, Target.planet, planet);
       }
     }
 
     Star star = Controllers.locController.getStar();
     if (star.intersectObject(x, y))
     {
       return new Target(star.id, Target.star, star);
     }
     return new Target(0, Target.none, null);
   }
 }
