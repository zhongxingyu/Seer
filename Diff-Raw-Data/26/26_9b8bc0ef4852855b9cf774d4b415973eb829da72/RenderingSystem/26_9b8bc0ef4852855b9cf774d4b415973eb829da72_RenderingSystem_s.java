 package entitySystems;
 
 import math.Matrix4;
 import components.RenderingComp;
 import components.TransformationComp;
 
 import entityFramework.ComponentMapper;
 import entityFramework.EntitySingleProcessingSystem;
 import entityFramework.IEntity;
 import entityFramework.IEntityWorld;
 import graphics.ShaderEffect;
 import graphics.SpriteBatch;
 
 public class RenderingSystem extends EntitySingleProcessingSystem {
 
 	private SpriteBatch spriteBatch;
 	
 	public RenderingSystem(IEntityWorld world, SpriteBatch graphics) {
 		super(world, TransformationComp.class, RenderingComp.class);
 		this.spriteBatch = graphics;
 	}
 
 	
 	private ComponentMapper<TransformationComp> posCM;
 	private ComponentMapper<RenderingComp> renderCM; 
 	
 	
 	
 	@Override
 	public void initialize() {
 		// TODO Auto-generated method stub
 		posCM = ComponentMapper.create(
 				this.getWorld().getDatabase(), TransformationComp.class);
 		
 		renderCM = ComponentMapper.create(
 				this.getWorld().getDatabase(), RenderingComp.class);
 	}
 
 	@Override
 	protected void processEntity(IEntity entity) {
 		RenderingComp renderC = this.renderCM.getComponent(entity);
 		TransformationComp positionC = this.posCM.getComponent(entity);
 		
 		
 		if(renderC.getEffect() != null) {
 			this.spriteBatch.end();
 			ShaderEffect effect = this.spriteBatch.getActiveEffect();
 			Matrix4 view = this.spriteBatch.getView();
 			
 			this.spriteBatch.begin(renderC.getEffect(),this.spriteBatch.getView());
 
 			draw(renderC, positionC);
 			
 			this.spriteBatch.end();
 			this.spriteBatch.begin(effect, view);
 		} else {
 			this.draw(renderC, positionC);
 		}
 	}
 
 	private void draw(RenderingComp renderC, TransformationComp transformation) {
 		this.spriteBatch.draw(renderC.getTexture(), transformation.getPosition(), renderC.getColor(), renderC.getSource(),
							  transformation.getOrigin(), transformation.getScale(), transformation.getRotation(), transformation.getMirror());
 	}
 
 }
