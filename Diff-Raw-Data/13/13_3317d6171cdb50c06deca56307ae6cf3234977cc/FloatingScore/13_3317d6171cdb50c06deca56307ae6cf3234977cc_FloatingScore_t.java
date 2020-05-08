 package dk.aau.oose.play;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 
 import dk.aau.oose.core.GameElement;
 import dk.aau.oose.util.MathUtils;
 import dk.aau.oose.util.Vec;
 
 public class FloatingScore extends GameElement {
 	
 	private int value;
 	private float moveSpeed = 1.7f;
 	private int noOfUpdates = 0;
 	private static final int MAX_UPDATES = 60;
 	private Color color;
 
 	public FloatingScore(Vector2f position, int value){
 		setPosition(position);
 		this.value = value;
 		color = calculateColor(value);
 	}
 	
 	@Override
 	public void onUpdate() {
 		//move up:
 		this.setPosition(Vec.add(this.getPosition(), new Vector2f(0, -moveSpeed)));
 
		if(++noOfUpdates >= MAX_UPDATES){
			destroy();
		}	
		
 	}
 
 	@Override
 	public void onDraw(Graphics gfx) {
 		
 		color.a = 1.0f - (float)noOfUpdates/MAX_UPDATES;
 		gfx.setColor(color); 
 		gfx.drawString(Integer.toString(value), 0, 0);
 	}
 	
 	private static Color calculateColor(int value){
 		float r = 1.0f,
 			  g = 1.0f;
 		
 		if(value > 50){
 			r = (float)MathUtils.getValueOnLine(value, 50, 1.0, 100, 0.0);
 		} else {
 			g = (float)MathUtils.getValueOnLine(value, 0, 0.0, 50, 1.0);
 		}
 		
 		return new Color(r, g, 0.0f);
 	}
 	
 	
 	
 	
 }
