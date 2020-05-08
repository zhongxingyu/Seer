 package spaceshooters.aurora.event.entity;
 
import pl.matterross.engine.gfx.IRenderable;
 import spaceshooters.aurora.entity.IEntity;
 
 public class EntityRenderEvent extends EntityEvent {
 	
 	private IRenderable tex;
 	
 	public EntityRenderEvent(IEntity entity, IRenderable tex) {
 		super(entity);
 		this.tex = tex;
 	}
 	
 	public IRenderable getTexture() {
 		return tex;
 	}
 	
 	public void setTexture(IRenderable tex) {
 		this.tex = tex;
 	}
 }
