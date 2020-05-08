 package entitySystems;
 
 import utils.Rectangle;
 import components.HealthComponent;
 
import math.Matrix4;
 import math.Vector2;
 import entityFramework.ComponentMapper;
 import entityFramework.EntitySystem;
 import entityFramework.IEntity;
 import entityFramework.IEntityWorld;
 import graphics.Color;
import graphics.ShaderEffect;
 import graphics.SpriteBatch;
 import graphics.Texture2D;
 
 public class HUDRenderingSystem extends EntitySystem {
 
 	private final String playerLabel;
 	private SpriteBatch batch;
 	private Vector2 healthPos;
 	
 	public HUDRenderingSystem(IEntityWorld world, SpriteBatch batch, String playerLabel) {
 		super(world);
 		this.batch = batch;
 		this.playerLabel = playerLabel;		
 		this.healthPos = new Vector2(100, 100);
 	}
 
 	private ComponentMapper<HealthComponent> healthCM;
 	
 	@Override
 	public void initialize() {
 		healthCM = ComponentMapper.create(this.getWorld().getDatabase(), HealthComponent.class);
 		
 	}
 
 	@Override
 	public void process() {
 		IEntity entity = this.getWorld().getEntityManager().getEntity(this.playerLabel);
 		HealthComponent comp = this.healthCM.getComponent(entity);
 		float ratio = comp.getHealthPoints() / comp.getMaxHealth();
 		
		this.batch.end();
		ShaderEffect effect = this.batch.getActiveEffect();
		Matrix4 view = this.batch.getView();
		
		this.batch.begin(effect, Matrix4.Identity);

 		Rectangle rect = new Rectangle((int)this.healthPos.X, (int)this.healthPos.Y, (int)(100.0f * ratio), 20);
 		this.batch.draw(Texture2D.getPixel(), rect, Color.White, null);
 		
		this.batch.end();
		this.batch.begin(effect, view);

		
	
 	}
 
 }
