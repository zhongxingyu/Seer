 package pl.spaceshooters.entity;
 
 import pl.blackburn.graphics.Animation;
 import pl.blackburn.graphics.SpriteSheet;
 import pl.spaceshooters.aurora.entity.EntityType;
 import pl.spaceshooters.aurora.level.ILevel;
 
 public abstract class AnimatedEntity extends Entity {
 	
 	protected Animation animation;
 	
 	public AnimatedEntity(ILevel level, float startX, float startY, EntityType entityType, Animation animation) {
 		super(level, startX, startY, entityType);
 		this.animation = animation;
		width = animation.getAnimation().getSprite(0, 0).getWidth();
		height = animation.getAnimation().getSprite(0, 0).getHeight();
 	}
 	
 	@Override
 	public void render() {
 		animation.render((int) position.x, (int) position.y);
 	}
 	
 	@Override
 	public SpriteSheet getTexture() {
 		return animation.getAnimation();
 	}
 	
 	public Animation getAnimation() {
 		return animation;
 	}
 	
 	@Override
 	public String getTextureFile() {
 		return "effects/anim.png";
 	}
 }
