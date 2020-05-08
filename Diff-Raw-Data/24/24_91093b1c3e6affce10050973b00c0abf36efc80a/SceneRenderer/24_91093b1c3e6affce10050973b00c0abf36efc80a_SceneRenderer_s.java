 package com.ggervais.gameengine.render;
 
 import java.awt.*;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import com.ggervais.gameengine.material.texture.Texture;
 import com.ggervais.gameengine.physics.boundingvolumes.BoundingBox;
 import com.ggervais.gameengine.scene.Scene;
 import com.ggervais.gameengine.scene.scenegraph.*;
 import com.ggervais.gameengine.scene.scenegraph.renderstates.*;
 
 // Render the scene onto a "canvas" (in our case, a Component).
 public abstract class SceneRenderer implements Observer {
 	
 	protected Scene scene;
 	protected Component canvas;
 	
 	public SceneRenderer() {
 		
 	}
 	
 	public SceneRenderer(Scene scene, Component canvas) {
 		this.scene = scene;
 		this.canvas = canvas;
 		this.scene.addObserver(this);
 	}
 	
 	public abstract void update(Observable source, Object args);
 
 	public void setScene(Scene scene) {
 		this.scene = scene;
 	}
 
 	public Scene getScene() {
 		return scene;
 	}
 
 	public void setCanvas(Component canvas) {
 		this.canvas = canvas;
 	}
 	
 	public Component getCanvas() {
 		return this.canvas;
 	}
 
     public void drawSceneGraph(Node root) {
         beginRendering();
         root.onDraw(this);
         endRendering();
     }
 
     protected void setState(Map<GlobalStateType, GlobalState> states) {
         if (states.get(GlobalStateType.ALPHA_BLENDING) != null) {
             setAlphaBlendingState((AlphaBlendingState) states.get(GlobalStateType.ALPHA_BLENDING));
         }
 
         if (states.get(GlobalStateType.LIGHTING) != null) {
             setLightingState((LightingState) states.get(GlobalStateType.LIGHTING));
         }
 
         if (states.get(GlobalStateType.WIREFRAME) != null) {
             setWireframeState((WireframeState) states.get(GlobalStateType.WIREFRAME));
         }
 
         if (states.get(GlobalStateType.ZBUFFER) != null) {
             setZBufferState((ZBufferState) states.get(GlobalStateType.ZBUFFER));
         }
     }
 
     public void drawGeometry(Geometry geometry) {
         setState(geometry.getStates());
         setWorldTransformations(geometry.getWorldTransformation());
 
         if (geometry.getEffect() != null) {
             enableTextures(geometry.getEffect());
         }
 
         drawElements(geometry);
 
         if (geometry.getEffect() != null) {
             disableTextures(geometry.getEffect());
         }
 
         restoreWorldTransformations();
 
         // Bounding box is already in world space, so we must restore the previous world transformations
         // before drawing it.
         drawBoundingBox(geometry.getBoundingBox(), geometry.isPickedInCurrentUpdate());
 
         resetColor();
     }
 
 
     public void enableTextures(Effect effect) {
         for (int i = 0; i < effect.nbTextures(); i++) {
             Texture texture = effect.getTexture(i);
             bindTexture(i, texture);
         }
     }
 
     public void disableTextures(Effect effect) {
        if (effect.nbTextures() > 0) {
            Texture texture = effect.getTexture(0);
            unbindTexture(texture);
         }
     }
 
     public abstract void beginRendering();
     public abstract void endRendering();
     public abstract void setWorldTransformations(Transformation worldTransformation);
     public abstract void restoreWorldTransformations();
     public abstract void drawElements(Geometry geometry);
     public abstract void setLightingState(LightingState state);
     public abstract void setAlphaBlendingState(AlphaBlendingState state);
     public abstract void setWireframeState(WireframeState state);
     public abstract void setZBufferState(ZBufferState state);
     public abstract void setColor(Color color);
     public abstract void resetColor();
     public abstract void bindTexture(Texture texture);
     public abstract void bindTexture(int index, Texture texture);
     public abstract void unbindTexture(Texture texture);
     public abstract void drawBoundingBox(BoundingBox box, boolean isPicked); // TODO temporary code
     public abstract void enableLight(Light light);
 
 }
