 package org.racenet.framework;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.opengles.GL10;
 
 public class Camera2 {
 	
     public final Vector2 position;
     public float zoom;
     public final float frustumWidth;
     public final float frustumHeight;
     final GLGraphics glGraphics;
     protected List<HudItem> hudItems = new ArrayList<HudItem>();
     
     public Camera2(GLGraphics glGraphics, float frustumWidth, float frustumHeight) {
     	
         this.glGraphics = glGraphics;
         this.frustumWidth = frustumWidth;
         this.frustumHeight = frustumHeight;
         this.position = new Vector2(frustumWidth / 2, frustumHeight / 2);
         this.zoom = 1.0f;
     }
 
 	public void setPosition(float x, float y) {
 		
 		this.position.set(x, y);
 		
		for (int i = 0; i < hudItems.size(); i++) {
 			
 			HudItem item = hudItems.get(i);
 			
 			item.setPosition(new Vector2(x + item.cameraX, y + item.cameraY));
 		}
 	}
     
 	public void setZoom(float factor) {
 		
 		this.zoom = factor;
 		
		for (int i = 0; i < hudItems.size(); i++) {
 			
 			HudItem item = hudItems.get(i);
 			item.setPosition(new Vector2(item.cameraX * factor, item.cameraY * factor));
 		}
 	}
 	
 	public void addHud(HudItem item) {
 		
 		hudItems.add(item);
 	}
 	
     public void removeHud(HudItem item) {
     	
     	hudItems.remove(item);
     }
     
     public void setViewportAndMatrices() {
     	
         GL10 gl = glGraphics.getGL();
         gl.glViewport(0, 0, glGraphics.getWidth(), glGraphics.getHeight());
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glOrthof(position.x - frustumWidth * zoom / 2, 
                     position.x + frustumWidth * zoom/ 2, 
                     position.y - frustumHeight * zoom / 2, 
                     position.y + frustumHeight * zoom/ 2, 
                     1, -1);
         gl.glMatrixMode(GL10.GL_MODELVIEW);
         gl.glLoadIdentity();
     }
     
     public void drawHud() {
     	
		for (int i = 0; i < hudItems.size(); i++) {
 			
 			hudItems.get(i).draw();
 		}
     }
     
     public void touchToWorld(Vector2 touch) {
     	
     	touch.x = (touch.x / (float) glGraphics.getWidth()) * frustumWidth * zoom;
     	touch.y = (1 - touch.y / (float) glGraphics.getHeight()) * frustumHeight * zoom;
     	touch.add(position).subtract(frustumWidth * zoom / 2, frustumHeight * zoom / 2);
     }
     
     public void reloadTextures() {
     	
    	for (int i = 0; i < hudItems.size(); i++) {
     		
     		this.hudItems.get(i).reloadTexture();
     	}
     }
     
     public void dispose() {
     	
    	for (int i = 0; i < hudItems.size(); i++) {
     		
     		this.hudItems.get(i).dispose();
     	}
     }
 }
