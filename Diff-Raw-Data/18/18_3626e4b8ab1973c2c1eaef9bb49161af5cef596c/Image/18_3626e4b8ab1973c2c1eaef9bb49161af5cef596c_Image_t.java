 package pl.shockah.easyslick;
 
 import java.io.InputStream;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.ImageBuffer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.opengl.renderer.SGL;
 
 public class Image extends org.newdawn.slick.Image {
 	protected float oX = 0f, oY = 0f;
 	
 	public Image() {super();}
 	public Image(String path) throws SlickException {super(path);}
 	public Image(int width, int height) throws SlickException {super(width,height);}
 	public Image(ImageBuffer buf) {super(buf);}
 	public Image(InputStream in, String ref, boolean flipped) throws SlickException {super(in,ref,flipped);}
 	
 	public static Image fromIntArray(int[] cols, int w, int h) {
 		if (cols == null) return null;
 		ImageBuffer buf = new ImageBuffer(w,h);
 		
 		for (int x = 0; x < w; x++)
 		for (int y = 0; y < h; y++) {
 			int rgb = cols[x+(y*w)];
 			buf.setRGBA(x,y,(rgb >> 16) & 0xff,(rgb >> 8) & 0xff,rgb & 0xff,(rgb >> 24) & 0xff);
 		}
 		return new Image(buf);
 	}
 	
 	public void draw(float x, float y, float x2, float y2, float srcx, float srcy, float srcx2, float srcy2, Color filter) {
 		float sH = (x2-x)/getWidth(), sV = (y2-y)/getHeight();
		x -= oX*sH; x2 -= oX*sH; x -= centerX*sH; x2 -= centerX*sH;
		y -= oY*sV; y2 -= oY*sV; y -= centerY*sV; y2 -= centerY*sV;
 		
 		init();
 
     	if (alpha != 1) {
     		if (filter == null) filter = Color.white;
     		filter = new Color(filter);
     		filter.a *= alpha;
     	}
 		filter.bind();
 		texture.bind();
 		
 		GL.glTranslatef(x, y, 0);
         if (angle != 0) {
 	        GL.glTranslatef(centerX*sH, centerY*sV, 0.0f); 
 	        if (angle != 0) GL.glRotatef(angle, 0.0f, 0.0f, 1.0f); 
 	        GL.glTranslatef(-centerX*sH, -centerY*sV, 0.0f); 
         }
         
         GL.glBegin(SGL.GL_QUADS); 
         drawEmbedded(0,0,x2-x,y2-y,srcx,srcy,srcx2,srcy2);
         GL.glEnd(); 
         
         if (angle != 0) {
 	        GL.glTranslatef(centerX*sH, centerY*sV, 0.0f); 
 	        if (angle != 0) GL.glRotatef(-angle, 0.0f, 0.0f, 1.0f); 
 	        GL.glTranslatef(-centerX*sH, -centerY*sV, 0.0f); 
         }
         GL.glTranslatef(-x, -y, 0);
 	}
 	
 	public void draw(float x, float y, float width, float height, Color filter) {
 		float sH = width/getWidth(), sV = height/getHeight();
		x -= oX*sH; x -= centerX*sH;
		y -= oY*sV; y -= centerY*sH;
 		
 		if (alpha != 1) {
     		if (filter == null) filter = Color.white;
     		filter = new Color(filter);
     		filter.a *= alpha;
     	}
         if (filter != null) filter.bind(); 
        
         texture.bind(); 
         GL.glTranslatef(x, y, 0);
         
         GL.glTranslatef(centerX*sH, centerY*sV, 0.0f); 
         if (angle != 0) GL.glRotatef(angle, 0.0f, 0.0f, 1.0f); 
         GL.glTranslatef(-centerX*sH, -centerY*sV, 0.0f);
         
         GL.glBegin(SGL.GL_QUADS); 
         drawEmbedded(0,0,width,height); 
         GL.glEnd(); 
         
         GL.glTranslatef(centerX*sH, centerY*sV, 0.0f); 
         if (angle != 0) GL.glRotatef(-angle, 0.0f, 0.0f, 1.0f); 
         GL.glTranslatef(-centerX*sH, -centerY*sV, 0.0f);
 	        
         GL.glTranslatef(-x, -y, 0);
 	}
 	public void drawFlash(float x, float y, float width, float height, Color col) {
 		super.drawFlash(x-oX,y-oY,width,height,col);
 	}
 	public void drawSheared(float x, float y, float hshear, float vshear) {
 		super.drawSheared(x-oX,y-oY,hshear,vshear);
 	}
 	
 	public Image getSubImage(int x,int y,int width,int height) {
 		init();
 		
 		Image sub = new Image();
 		sub.inited = true;
 		sub.texture = this.texture;
 		sub.textureOffsetX = ((x/(float)this.width)*textureWidth)+textureOffsetX;
 		sub.textureOffsetY = ((y/(float)this.height)*textureHeight)+textureOffsetY;
 		sub.textureWidth = ((width/(float)this.width)*textureWidth);
 		sub.textureHeight = ((height/(float)this.height)*textureHeight);
 		
 		sub.width = width;
 		sub.height = height;
 		sub.ref = ref;
 		sub.centerX = width/2;
 		sub.centerY = height/2;
 		
 		return sub;
 	}
 	
 	public float getRotation() {return -angle;}
 	public void setRotation(float angle) {super.setRotation(-angle);}
 	public void rotate(float angle) {super.rotate(-angle);}
 	
 	public void setOffset(float x, float y) {
 		oX = x; oY = y;
 	}
 	public void center() {
 		setCenterOfRotation(getWidth()/2f,getHeight()/2f);
 	}
 	
 	public void drawTiled(float x, float y) {drawTiled(x,y,Color.white);}
 	public void drawTiled(float x, float y, Color blend) {
 		x %= getWidth(); x -= getWidth();
 		y %= getHeight(); y -= getHeight();
 		
 		for (float yy = y; yy < Room.get().size.y; yy += getHeight())
 		for (float xx = x; xx < Room.get().size.x; xx += getWidth()) draw(xx,yy,blend);
 	}
 	
 	public int[] toIntArray() {
 		int w = getWidth(), h = getHeight();
 		int[] ret = new int[w*h];
 		
 		for (int x = 0; x < w; x++)
 		for (int y = 0; y < h; y++) {
 			Color c = getColor(x,y);
 			ret[x+(y*w)] = (c.getAlpha() << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
 		}
 		return ret;
 	}
 }
