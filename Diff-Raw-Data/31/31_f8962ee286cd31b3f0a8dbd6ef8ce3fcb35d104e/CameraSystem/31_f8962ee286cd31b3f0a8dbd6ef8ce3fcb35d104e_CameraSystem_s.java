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
 
 package com.stonetolb.engine.system;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.stonetolb.engine.component.position.Position;
 import com.stonetolb.engine.component.render.CameraMount;
 import com.stonetolb.render.Camera;
 
 /**
  * System that handles Camera movement for any Entity with the Camera
  * attached to its CameraMount
  * 
  * @author james.baiera
  *
  */
 public class CameraSystem extends EntityProcessingSystem {
 	private @Mapper ComponentMapper<Position> positionMap;
 	private @Mapper ComponentMapper<CameraMount> mountMap;
 	
 	public CameraSystem() {
 		super(Aspect.getAspectForAll(Position.class, CameraMount.class));
 	}
 	
 	@Override
 	protected void process(Entity arg0) {
 		Position pos = positionMap.get(arg0);
 		CameraMount mnt = mountMap.get(arg0);
 		
 		Camera cam = Camera.getCamera();
 		
 		if (cam.isAttachedTo(mnt)) {
 			cam.updatePosition(
 					  pos.getX() + mnt.getXOffset()
 					, pos.getY() + mnt.getYOffset()
 					);
 		}
 	}

 }
