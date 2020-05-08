 package browser;
 
 import game.Game;
 
 import java.io.File;
 import java.nio.ByteBuffer;
 
 import javax.media.opengl.GL2;
 
 import rendering.RenderUpdater;
 import settings.Settings;
 import util.Log;
 
 import com.google.gson.Gson;
 import com.jogamp.newt.event.KeyEvent;
 import com.jogamp.newt.event.MouseEvent;
 import com.jogamp.opengl.util.texture.Texture;
 
 public class AwesomiumWrapper extends Browser {
 	private static final boolean ENGINE_GUI = true;
 
 	public AwesomiumWrapper() {
 		AwesomiumHelper.init(!ENGINE_GUI);
 	}
 
 	@Override
 	public void mouseMoved(int x, int y) {
 		AwesomiumHelper.mouseMoved(x, y);
 	}
 
 	@Override
 	public void mouseButton(int i, boolean down) {
 		// Log.log(this, "mouse down:" + down + ", #" + i);
 		AwesomiumHelper.mouseButton(i, down);
 	}
 
 	@Override
 	public void mouseWheel(MouseEvent e) {
 	}
 
 	@Override
 	public void keyEvent(KeyEvent e, boolean b) {
 	}
 
 	@Override
 	public void debugSite() {
 		if (ENGINE_GUI)
 			AwesomiumHelper.loadFile(new File("gui/gui.html").getPath(),
 					new Runnable() {
 						@Override
 						public void run() {
 							sendObjectsToJS();
 						}
 					});
 		else {
 			AwesomiumHelper.loadUrl("http://www.youtube.com");
 		}
 	}
 
 	protected void sendObjectsToJS() {
 		Log.log(this, "sending Objects to JS");
 		if (Game.INSTANCE.world.gameObjects.size() == 0)
 			return;
 		Gson gson = new Gson();
 		try {
 			String objectString = gson.toJson(Game.INSTANCE.world.gameObjects);
 			AwesomiumHelper.executeJavascript("window.sendReceiveObjects("
 					+ objectString + ")");
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		String settingsString = gson
 				.toJson(((RenderUpdater) Game.INSTANCE.loop.renderer)
 						.getSettings());
 		AwesomiumHelper.executeJavascript("window.sendReceiveSettings("
 				+ settingsString + ")");
 	}
 
 	@Override
 	public void restoreSite() {
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		AwesomiumHelper.keyTyped(e);
 	}
 
 	@Override
 	public Texture getTexture() {
 		return null;
 	}
 
 	@Override
 	public void render(GL2 gl) {
 		gl.glRasterPos2i(0, 0);
 		gl.glPixelZoom(((float) Game.INSTANCE.getWidth() / Settings.WIDTH),
 				-((float) Game.INSTANCE.getHeight() / Settings.HEIGHT));
 		ByteBuffer buffer = AwesomiumHelper.getBuffer();
 		gl.glEnable(GL2.GL_BLEND);
 		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
 		if (buffer != null)
 			gl.glDrawPixels(Settings.WIDTH, Settings.HEIGHT, GL2.GL_BGRA,
 					GL2.GL_UNSIGNED_BYTE, buffer);
 		gl.glDisable(GL2.GL_BLEND);
 		gl.glPixelZoom(1, 1);
 	}
 
 	@Override
 	public void dispose(GL2 gl) {
 		AwesomiumHelper.dispose();
 	}
 
 	@Override
 	public boolean isDummy() {
 		return false;
 	}
 
 }
