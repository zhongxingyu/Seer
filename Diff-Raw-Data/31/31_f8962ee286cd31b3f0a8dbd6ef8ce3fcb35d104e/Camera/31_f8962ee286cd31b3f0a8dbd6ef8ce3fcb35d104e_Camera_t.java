 /* 
  * Copyleft (o) 2012 James Baiera
  * All wrongs reserved.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.stonetolb.render;
 
 import org.lwjgl.opengl.GL11;
 
import com.artemis.Entity;
 import com.stonetolb.engine.component.render.CameraMount;
 import com.stonetolb.util.Pair;
 
 /**
  * The Camera is a special class that controls where the player is looking at the moment.
  * <p>
  * Camera is a singleton since there can only ever be one camera at a time. The Camera can
  * be attached to a {@link CameraMount} component, providing methods to test which mount it
  * is attached to as well as methods to move the camera around the screen.
  * 
  * @author james.baiera
  *
  */
 public class Camera {
 	
 	private static Camera INSTANCE = null;
 	private static Pair<Float, Float> ORIGIN = new Pair<Float, Float>(0F, 0F);
 	
 	private Pair<Float, Float> position;
 	private int screenWidth;
 	private int screenHeight;
	private com.stonetolb.engine.Entity parent;
 	private CameraMount mount;
 	
 	public static synchronized void createCamera(int pWidth, int pHeight) {
 		if (INSTANCE == null) {
 			INSTANCE = new Camera(pWidth, pHeight);
 		}
 	}
 	
 	public static Camera getCamera() {
 		return INSTANCE;
 	}
 	
 	private Camera(int pWidth, int pHeight) {
 		screenWidth = pWidth;
 		screenHeight = pHeight;
 		position = ORIGIN;
 		parent = null;
 		mount = null;
 	}
 	
 	public CameraMount attachTo(CameraMount pMount) {
 		mount = pMount;
 		return mount;
 	}
 	
 	public void detach() {
 		mount = null;
		updatePosition(ORIGIN.x.floatValue(), ORIGIN.y.floatValue());
 	}
 	
 	public boolean isAttachedTo(CameraMount pMount) {
 		return mount == pMount;
 	}
 	
 	@Deprecated
	public void setParent(com.stonetolb.engine.Entity pParent) {
 		parent = pParent;
 		updatePosition();
 	}
 	
 	@Deprecated
 	private void updatePosition() {
 		if (parent != null) {
 			position.x = parent.getAbsolute().x - ((float)screenWidth / 2.0F);
 			position.y = parent.getAbsolute().y - ((float)screenHeight / 2.0F);
 		}
 		else {
 			position = ORIGIN;
 		}
 	}
 	
 	public void updatePosition(float x, float y) {
 		position.x = x - ((float)screenWidth / 2.0F);
 		position.y = y - ((float)screenHeight / 2.0F);
 	}
 	
 	public void moveCamera() {
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(
 				  position.x.doubleValue()
 				, position.x.doubleValue() + (double)screenWidth
 				, position.y.doubleValue() + (double)screenHeight
 				, position.y.doubleValue()
 				, screenHeight
 				, screenHeight * -1
 			);
 		
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 	}
 }
