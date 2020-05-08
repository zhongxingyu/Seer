 import java.util.Random;
 
 /* Classes Used:
 
 yy - Block
 add - BlockStationary
 agw - BlockFluid
 p - Material
 aav - MapColor
 
 */
 
 public class add extends agw // extends yy
 {
	private p matinstance; 
 	protected add(int paramInt, p/*Material*/ paramp)
 	{
 		super(paramInt, paramp);
 		matInstance = new p(aav.b); 
 		b(true);//setTickOnLoad
 		//if (paramp == matInstance.h)
 		//{ // if lava
 		//	b(true);//setTickOnLoad
 		//}
 	}
 
 	public void a(ry world, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
 	{ // onNeighborBlockChange
 		super.a(world, paramInt1, paramInt2, paramInt3, paramInt4);
 		if (world.a(paramInt1, paramInt2, paramInt3) == this.bM/*blockID*/)
 			j(world, paramInt1, paramInt2, paramInt3);
 	}
 
 	private void j(ry world, int paramInt1, int paramInt2, int paramInt3)
 	{ // makeFlowing
 		int i = world.d(paramInt1, paramInt2, paramInt3);
 		world.t = true;
 		world.b(paramInt1, paramInt2, paramInt3, this.bM - 1 /*block id - 1*/, i);
 		world.c(paramInt1, paramInt2, paramInt3, paramInt1, paramInt2, paramInt3);
 		world.a(paramInt1, paramInt2, paramInt3, this.bM - 1, d()/*Block.tickRate*/);
 		world.t = false;
 	}
 
 	public void a(ry world, int posx, int paramInt2, int paramInt3, Random paramRandom)
 	{ // updateTick
 		if (this.bZ == matInstance.h)
 		{ // if lava
 			int i = paramRandom.nextInt(3);
 			for (int j = 0; j < i; j++)
 			{
 				posx += paramRandom.nextInt(3) - 1;
 				paramInt2++;
 				paramInt3 += paramRandom.nextInt(3) - 1;
 				int k = world.a(posx, paramInt2, paramInt3);
 				if (k == 0)
 				{
 					if ((!k(world, posx - 1, paramInt2, paramInt3)) && (!k(world, posx + 1, paramInt2, paramInt3)) &&
 					    (!k(world, posx, paramInt2, paramInt3 - 1)) && (!k(world, posx, paramInt2, paramInt3 + 1)) &&
 					    (!k(world, posx, paramInt2 - 1, paramInt3)) && (!k(world, posx, paramInt2 + 1, paramInt3))
 					   )
 							continue;
 					world.g(posx, paramInt2, paramInt3, yy.ar.bM);
 					return;
 				}
 				if (yy.k[k].bZ.d())
 					return;
 			}
 		}
 		else
 		{
			a(0f,0f,0f,0f,mod_oceanwaves.getheight(world, posx, paramInt2, paramInt3, paramRandom), 0f);
 			//scheduleUpdate();
 		}
 	}
 
 	private boolean k(ry world, int paramInt1, int paramInt2, int paramInt3)
 	{
 		return world.e(paramInt1, paramInt2, paramInt3).g();
 	}
 }
