 package com.blastedstudios.crittercaptors.util;
 
 import java.awt.image.BufferedImage;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g3d.model.Model;
 import com.badlogic.gdx.math.Vector3;
 
 public class RenderUtil {
 	public static void drawModel(Model model, Texture texture, Vector3 pos,
 			Vector3 direction){
 		Gdx.gl10.glEnable(GL10.GL_DEPTH_TEST);
 		Gdx.gl10.glFrontFace(GL10.GL_CW);
 		if(texture != null){
 			Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
 			texture.bind();
 		}
 		Gdx.gl10.glPushMatrix();
 		Gdx.gl10.glTranslatef(pos.x, pos.y, pos.z);
 		Gdx.gl10.glRotatef((float)Math.toDegrees(Math.atan2(direction.x, direction.z)), 0, 1, 0);
 		model.render();
 		Gdx.gl10.glPopMatrix();
 	}
 	
 	public static Camera resize(int width, int height){
         float aspectRatio = (float) width / (float) height;
         Camera camera = new PerspectiveCamera(67, 2f * aspectRatio, 2f);
         camera.translate(0, 1, 0);
         camera.direction.set(0, 0, 1);
         camera.update();
         camera.apply(Gdx.gl10);
         return camera;
 	}
 
 	public static void drawSky(Model model, Texture skyTexture, Vector3 cameraPosition){
 		Gdx.gl10.glEnable(GL10.GL_TEXTURE_2D);
 		Gdx.gl10.glDisable(GL10.GL_DEPTH_TEST);
 		Gdx.gl10.glPushMatrix();
 		Gdx.gl10.glTranslatef(cameraPosition.x, cameraPosition.y, cameraPosition.z);
 		Gdx.gl10.glScalef(10f, 10f, 10f);
 		skyTexture.bind();
 		model.render();
 		Gdx.gl10.glPopMatrix();
 	}
 	
 	public static Color[] imageFromURL(URL url){
 		Color[] colors = imageFromURLPC(url);
 		if(colors == null)
 			colors = imageFromURLAndroid(url);
 		if(colors == null)
 			colors = imageFromURLGDX(url);
 		return colors;
 	}
 	
 	private static Color[] imageFromURLAndroid(URL url){
 		try{
 			int width = 64;
 			Color[] colors = new Color[width*width];
 			Class<?> bitmapFactoryClass = Class.forName("android.graphics.BitmapFactory");
 			Method meth = bitmapFactoryClass.getMethod("decodeStream", InputStream.class);
 			Object bitmap = meth.invoke(bitmapFactoryClass, url.openStream());
			Method getPixelMethod = bitmap.getClass().getMethod("getPixel", Integer.class, Integer.class);
 			for(int y=0; y<width; y++)
 				for(int x=0; x<width; x++){
 					int col = (Integer) getPixelMethod.invoke(bitmap, x, y);
 					Color colo = new Color(0,(col>>16)&0xFF, (col>>8)&0xFF, col&0xFF);
 					colors[y*width + x] = colo;
 				}
 			return colors;
 		}catch(Exception e){
 			Gdx.app.error("imageFromURLAndroid", "Failed to retreieve image with message: " + e.getMessage());
 		}
 		return null;
 	}
 	
 	private static Color[] imageFromURLPC(URL url){
 		try{
 			Class<?> imageIOClass = Class.forName("javax.imageio.ImageIO");
 			Method readMeth = imageIOClass.getMethod("read", URL.class);
 			BufferedImage bufferedImage = (BufferedImage) readMeth.invoke(imageIOClass, url);
 			Color[] colors = new Color[bufferedImage.getWidth()*bufferedImage.getHeight()];
 			for(int y=0; y<bufferedImage.getHeight(); y++)
 				for(int x=0; x<bufferedImage.getWidth(); x++){
 					int col = bufferedImage.getRGB(x, y);
 					Color colo = new Color((col>>16)&0xFF, (col>>8)&0xFF, col&0xFF,0);
 					colors[y*bufferedImage.getHeight() + x] = colo;
 				}
 			return colors;
 		}catch(Exception e){
 			Gdx.app.error("imageFromURLPC", "Failed to retreieve image with message: " + e.getMessage());
 		}
 		return null;
 	}
 	
 	/**
 	 * Does not work, throwing an exception on the first line. Would be superior
 	 */
 	private static Color[] imageFromURLGDX(URL url){
 		try{
 			Pixmap map = new Pixmap(new HTMLUtil.URLHandle(url));
 			Color[] colors = new Color[map.getWidth()*map.getHeight()];
 			for(int y=0; y<map.getWidth(); y++)
 				for(int x=0; x<map.getWidth(); x++){
 					int col = map.getPixel(x, y);
 					Color color = new Color(0,(col>>16)&0xFF, (col>>8)&0xFF, col&0xFF);
 					colors[y*map.getWidth() + x] = color;
 				}
 			return colors;
 		}catch(Exception e){
 			Gdx.app.error("imageFromURLGDX", "Failed to retreieve image with message: " + e.getMessage());
 		}
 		return null;
 	}
 }
