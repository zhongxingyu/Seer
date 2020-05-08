 import java.util.Random;
 
 /* Classes used:
 
 ry - World
 yy - Block
 
 */
 
 public class mod_oceanwaves extends BaseMod {
 
 	public String getVersion() { return "Minecraft 1.0.0 mod v0.2"; }
 
 	private float[] heights = {0.2f,0.3f,0.5f,0.8f,1.0f,1.1f,0.9f,0.3f};
 
 	public void load() {
 		// this is the BaseMod method to put all your shit in, like how mod_whatever() used to be.
 	}
 
	public float getHeight(ry world, int posx, int posy, int posz, Random rand)
	{
 		int tempid = world.a(posx,posy+1,posz);
 		if(tempid == yy.A.bM || tempid == yy.B.bM)
 		{
 			return 1f; //don't want water in the middle of ponds changing
 		}
 		return heights[((int)(world.u() % 8)+posx) % 8];
 	}
 }
