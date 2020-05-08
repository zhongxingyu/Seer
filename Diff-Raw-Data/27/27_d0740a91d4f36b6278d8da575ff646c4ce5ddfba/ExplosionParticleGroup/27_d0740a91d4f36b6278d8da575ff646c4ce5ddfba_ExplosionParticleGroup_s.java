 package steve4448.livetextbackground.background.object;
 
 import android.graphics.Color;
 
 public class ExplosionParticleGroup {
 	public ExplosionParticle[] arr;
 	public int color;
 	public int alpha;
 	
	public ExplosionParticleGroup(float x, float y, float width, float height, int color) {
 		arr = new ExplosionParticle[(int)Math.min((width * height) + 1, 30)];
 		for(int i = 0; i < arr.length; i++)
			arr[i] = new ExplosionParticle(x + (float)(Math.random() * width), y + (float)(Math.random() * height), 2);
 		this.color = color;
 		this.alpha = Color.alpha(color);
 	}
 }
