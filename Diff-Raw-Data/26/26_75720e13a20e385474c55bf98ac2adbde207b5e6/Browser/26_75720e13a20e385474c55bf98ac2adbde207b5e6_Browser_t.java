 package browser;
 
 import javax.media.opengl.GL2;
 
 import com.jogamp.newt.event.KeyEvent;
 import com.jogamp.newt.event.MouseEvent;
 import com.jogamp.opengl.util.texture.Texture;
 
 public abstract class Browser {
 
 	public static final String TEXTURE_NAME = "browserTexture";
 
 	public abstract void mouseMoved(int x, int y);
 
 	public abstract void mouseButton(int i, boolean b);
 
 	public abstract void mouseWheel(MouseEvent e);
 
 	public abstract void keyEvent(KeyEvent e, boolean b);
 
 	public abstract void debugSite();
 
 	public abstract void restoreSite();
 
 	public abstract void keyTyped(KeyEvent e);
 
 	public abstract Texture getTexture();
 
 	public abstract void render(GL2 gl);
 
 	public abstract void dispose(GL2 gl);
 
 	public abstract boolean isDummy();
 
 	public static final class Dummy extends Browser {
 		@Override
 		public void mouseMoved(int x, int y) {
 		}
 
 		@Override
 		public void mouseButton(int i, boolean b) {
 		}
 
 		@Override
 		public void mouseWheel(MouseEvent e) {
 		}
 
 		@Override
 		public void keyEvent(KeyEvent e, boolean b) {
 		}
 
 		@Override
 		public void debugSite() {
 		}
 
 		@Override
 		public void restoreSite() {
 		}
 
 		@Override
 		public void keyTyped(KeyEvent e) {
 		}
 
 		@Override
 		public Texture getTexture() {
 			return null;
 		}
 
 		@Override
 		public void render(GL2 gl) {
 		}
 
 		@Override
 		public void dispose(GL2 gl) {
 		}
 
 		@Override
 		public boolean isDummy() {
 			return true;
 		}
 	}
 
 }
