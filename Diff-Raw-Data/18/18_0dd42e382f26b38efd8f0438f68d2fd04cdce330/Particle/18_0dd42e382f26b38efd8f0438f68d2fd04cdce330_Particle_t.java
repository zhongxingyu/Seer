 package cz.opt.pEngine;
 
 import org.lwjgl.util.vector.Vector2f;
 
 import cz.opt.pEngine.enums.SpreadType;
 
 public abstract class Particle
 {	
 	private final float defX, defY;
 	private final float transFactor = 0.01f;
 	
 	protected float x, y;
 	protected float dx, dy;
 	protected float range;
 	protected float alpha;
 	protected float fadeFactor;
 	protected float angle;
 	protected float rotateFactor;
 	
 	protected long launchTime;
 	protected long second = 1000000000L;
 	
 	protected Pengine eng;
 	protected Color col;
 	
 	private boolean r_done = false, g_done = false, b_done = false;
 	
 	public Particle(float dx, float dy, float rotateFactor, Pengine eng)
 	{
 		this.eng = eng;
 		Vector2f tmp = eng.pVector.getPos();
 		this.x = tmp.x;
 		this.y = tmp.y;
 		this.dx = dx;
 		this.dy = dy;
 		this.range = eng.range;
 		this.col = new Color(1f, 1f, 0f);
 		this.alpha = 1.0f;
 		this.fadeFactor = 0f;
 		this.angle = Pengine.getRandom(0, 359);
 		this.rotateFactor = rotateFactor;
 		this.launchTime = System.nanoTime();
 		
 		this.defX = x;
 		this.defY = y;
 		
 		if(eng.transition)
 			col = (eng.ct.getFirst().copy());
 	}
 	
 	public final void update()
 	{
 		x += dx;
 		y += dy;
 		
		if(System.nanoTime() >= launchTime+second*1.0)
 			fadeFactor = Pengine.getRandom(0.05f, 0.1f);
 		
 		angle += rotateFactor;
 		if(angle >= 360)
 			angle = 0;
 		
 		if(eng.s_type == SpreadType.SQUARE)
 		{
 			if(x >= defX+range || x <= defX-range || y >= defY+range || y <= defY-range)
 				fadeFactor = Pengine.getRandom(0.05f, 0.1f);
 		} else if(eng.s_type == SpreadType.ROUND)
 		{
 			if( ( Math.pow((x - defX), 2) + Math.pow((y - defY), 2) ) >= Math.pow(range, 2))
 				fadeFactor = Pengine.getRandom(0.05f, 0.1f);
 		}
 		
 		if(alpha <= 0.001f)
 			Pengine.particles.remove(this);
 		
 		if(eng.transition)
 			updateColor();
 	}
 	
 	private void updateColor()
 	{
 		boolean r_inc = false, g_inc = false, b_inc = false;
 		
 		if(col.getR() < eng.ct.getSecond().getR())
 			r_inc = true;
 		else if(col.getR() == eng.ct.getSecond().getR())
 			r_done = true;
 		else r_inc = false;
 		
 		if(col.getG() < eng.ct.getSecond().getG())
 			g_inc = true;
 		else if(col.getG() == eng.ct.getSecond().getG())
 			g_done = true;
 		else g_inc = false;
 		
 		if(col.getB() < eng.ct.getSecond().getB())
 			b_inc = true;
 		else if(col.getB() == eng.ct.getSecond().getB())
 			b_done = true;
 		else b_inc = false;
 		
 		
 		if(!r_done)
 		{
 			if(r_inc)
 				col.setR(col.getR() + transFactor);
 			else
 				col.setR(col.getR() - transFactor);
 		}
 		
 		if(!g_done)
 		{
 			if(g_inc)
 				col.setG(col.getG() + transFactor);
 			else
 				col.setG(col.getG() - transFactor);
 		}
 		
 		if(!b_done)
 		{
 			if(b_inc)
 				col.setB(col.getB() + transFactor);
 			else
 				col.setB(col.getB() - transFactor);
 		}
 	}
 	
 	public abstract void render();
 }
