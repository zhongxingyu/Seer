 package com.kingx.dungeons.engine.component;
 
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.kingx.artemis.Component;
import com.kingx.dungeons.App;
 
 public class ShadowComponent extends Component {
 
     private final Camera[] lights;
 
     public ShadowComponent() {
 
         lights = new Camera[4];
         int offset = 1;
         for (int i = 0; i < lights.length; i++) {
             lights[i] = new PerspectiveCamera(90, 512, 512);
             lights[i].near = 0.0001f;
             lights[i].far = 500;
             lights[i].direction.x = Math.round(Math.cos(Math.PI / 2 * (i + offset)));
             lights[i].direction.y = Math.round(Math.sin(Math.PI / 2 * (i + offset)));
             lights[i].direction.z = 0.01f;
            lights[i].position.z = 0.1f * App.MAZE_WALL_SIZE;
         }
     }
 
     public Camera[] getLights() {
         return lights;
     }
 
 }
