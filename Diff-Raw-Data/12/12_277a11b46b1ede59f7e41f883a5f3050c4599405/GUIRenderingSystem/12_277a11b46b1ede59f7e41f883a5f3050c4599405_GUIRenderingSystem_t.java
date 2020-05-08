 package entitySystems;
 
 import utils.Rectangle;
 import math.Matrix4;
 import math.Vector2;
 import components.GUIComp;
 
 import entityFramework.EntitySingleProcessingSystem;
 import entityFramework.IEntity;
 import entityFramework.IEntityWorld;
 import graphics.ShaderEffect;
 import graphics.SpriteBatch;
 import graphics.TextureFont;
 
 public class GUIRenderingSystem extends EntitySingleProcessingSystem {
 	private SpriteBatch spriteBatch;
 
 	public GUIRenderingSystem(IEntityWorld world, SpriteBatch sb) {
 		super(world, GUIComp.class);
 		this.spriteBatch = sb;
 	}
 
 	@Override
 	public void initialize() {
 
 	}
 
 	@Override
 	protected void processEntity(IEntity entity) {
 		GUIComp comp = entity.getComponent(GUIComp.class);
 
 		//Make sure we draw to the view and not world
 		Matrix4 transform = spriteBatch.getView();
 		ShaderEffect effect = spriteBatch.getActiveEffect();
 		this.spriteBatch.end();		
 		this.spriteBatch.begin();
 
 
 		//Draw stuff
 		if(comp.getBackground() != null)
 			this.spriteBatch.draw(comp.getBackground(), comp.getPosition(), comp.getBgColor(), null);
 
 		if(comp.getMiddleground() != null)
 			this.spriteBatch.draw(comp.getMiddleground(), comp.getPosition(), comp.getMgColor(), null);
 
 		if(comp.getForeground() != null)
 			this.spriteBatch.draw(comp.getForeground(), comp.getPosition(), comp.getFgColor(), null);
 
 		
 		
 		if(comp.getText() != null) {
 			Vector2 measure = comp.getTextFont().meassureString(comp.getText());
 			String text = comp.getText();
			if(measure.X >= comp.getPosition().Width-40) {
 				text = fixText(comp.getTextFont(), comp.getText(), comp.getPosition());
 			}
 
 			Rectangle topLeft = comp.getPosition();
 			Vector2 textPosition = new Vector2(topLeft.X + topLeft.Width / 2, topLeft.Y + topLeft.Height / 2);
 
 			measure = Vector2.mul(0.5f, comp.getTextFont().meassureString(text));
 			this.spriteBatch.drawString(comp.getTextFont(), text, textPosition, comp.getTextColor(), measure);
 
 		}
 		
 		this.spriteBatch.end();
 		this.spriteBatch.begin(effect, transform);
 	}
 
 	private String fixText(TextureFont textFont, String text, Rectangle position) {
 		String newText = "";
 
 		String dotString = "...";
 		float dots = textFont.meassureString(dotString).X;
 
 		for(int i = text.length() - 1; i >= 0; i--) {
 			String substr = text.substring(0, i);
			if(textFont.meassureString(substr).X + dots < position.Width-40) {
 				newText = substr + dotString;
 				return newText;
 			}
 		}
 
 		return newText;
 	}
 
 }
