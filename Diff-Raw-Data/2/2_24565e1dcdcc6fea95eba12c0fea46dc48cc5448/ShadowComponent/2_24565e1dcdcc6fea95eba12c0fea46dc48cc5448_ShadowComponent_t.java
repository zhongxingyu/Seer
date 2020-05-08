 package com.kingx.dungeons.engine.component;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.math.Vector3;
 import com.kingx.artemis.Component;
 import com.kingx.dungeons.App;
 import com.kingx.dungeons.engine.component.dynamic.MoveComponent;
 import com.kingx.dungeons.engine.component.dynamic.PositionComponent;
 import com.kingx.dungeons.geom.Collision;
 
 public class ShadowComponent extends Component {
 
     private final Camera[] lights;
     private float lastAngle;
 
     public ShadowComponent() {
 
         lights = new Camera[4];
         int offset = 1;
         for (int i = 0; i < lights.length; i++) {
            lights[i] = new PerspectiveCamera(91, 512, 512);
             lights[i].near = 0.0001f;
             lights[i].far = 500;
             lights[i].direction.x = Math.round(Math.cos(Math.PI / 2 * (i + offset)));
             lights[i].direction.y = Math.round(Math.sin(Math.PI / 2 * (i + offset)));
             lights[i].direction.z = 0.01f;
             lights[i].position.z = App.PLAYER_OFFSET * App.UNIT;
         }
     }
 
     public Camera[] getLights() {
         return lights;
     }
 
     public void move(PositionComponent position) {
         for (Camera light : lights) {
             light.position.x = position.getX();
             light.position.y = position.getY();
             light.position.z = position.getZ();
             Collision.correct(light.position, App.LIGHT_OFFSET);
         }
     }
 
     public void rotate(MoveComponent mc) {
         float angle = mc.getRotation();
         float difference = 0;
 
         if (lastAngle != angle) {
             difference = angle - lastAngle;
         }
         lastAngle = angle;
 
         for (int i = 0; i < lights.length; i++) {
             if (difference != 0) {
                 Vector3 temp = lights[i].position.cpy();
                 lights[i].translate(temp.cpy().mul(-1));
                 lights[i].rotate(Vector3.Y, difference);
                 lights[i].translate(temp);
             }
             lights[i].update();
         }
     }
 }
