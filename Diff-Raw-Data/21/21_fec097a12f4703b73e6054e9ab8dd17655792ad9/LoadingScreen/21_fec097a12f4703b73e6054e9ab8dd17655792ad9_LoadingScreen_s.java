 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.application;
 
 import com.jme.image.Texture;
import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
 import com.jme.scene.Node;
 import com.jme.scene.shape.Quad;
import com.jme.scene.shape.Sphere;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.TextureState;
 import com.jme.system.DisplaySystem;
 import com.jme.util.TextureManager;
 import edu.gatech.statics.objects.representations.PointRepresentation;
 import java.net.URL;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class LoadingScreen extends Node {
 
     public LoadingScreen() {
 
         String imagePath = "rsrc/logo.png";
 
         URL textureLoc = PointRepresentation.class.getClassLoader().getResource(imagePath);
         Texture pointTexture = TextureManager.loadTexture(textureLoc, Texture.MM_LINEAR, Texture.FM_LINEAR);
 
         Quad pointQuad = new Quad("", .5f, .5f);
         TextureState textureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
         textureState.setTexture(pointTexture);
         pointQuad.setRenderState(textureState);
         pointQuad.setLightCombineMode(LightState.OFF);
         pointQuad.setLocalTranslation(0, 0, 1);
 
         pointQuad.setLocalScale(new Vector3f(-3.0f, 3.0f, 1));
         attachChild(pointQuad);
 
 
         updateRenderState();
     }
 }
