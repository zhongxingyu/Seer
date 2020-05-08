 package poomonkeys.common;
 
 import java.util.ArrayList;
 
 public class ExplosionController
 {
 	static ArrayList<Dirt> dirt = new ArrayList<Dirt>();
 
	static float DIRT_SIZE = .3f;
 	
 	public static void explode(Terrain t, float x, float y, float r, Renderer renderer)
 	{
 		Dirt test = new Dirt();
 		test.x = x;
 		test.y = y;
 		test.width = 1;
 		test.height = 1;
 		
 		dirt.add(test);
 		renderer.registerDrawable(test);
 		
 		float viewWidth = renderer.getViewWidth();
 		float viewHeight = renderer.getViewHeight();
 		
 		int minX = (int) ((x+viewWidth/2 - r) / viewWidth * (t.NUM_POINTS-1))+1;
 		int maxX = (int) ((x+viewWidth/2 + r) / viewWidth * (t.NUM_POINTS-1));
 		
 		
 		if (maxX > t.NUM_POINTS-1) 
 		{
 			maxX = t.NUM_POINTS-1;
 		}
 		if (minX < 0) 
 		{
 			minX = 0;
 		}
 
 		float minViewX = (minX / (t.NUM_POINTS-1.f)) * viewWidth + t.x;
 		float maxViewX = (maxX / (t.NUM_POINTS-1.f)) * viewWidth + t.x;
 		
 		float explosion_width = Math.abs(maxViewX - minViewX);
 		float w = viewWidth/(t.NUM_POINTS-1);
 		
		int num_dirt_columns = 1 + (int)( explosion_width/(2*DIRT_SIZE) );
		float gap = explosion_width%(2*DIRT_SIZE) / (num_dirt_columns);
 		
		for (float dirt_x = minViewX; dirt_x <= maxViewX; dirt_x += DIRT_SIZE*2+gap) 
 		{
 			float offset = (float) Math.sqrt(r*r - Math.pow(dirt_x - x, 2));
 			float tCircleY = (float) (offset + y);
 			int iFromX = (int) ((t.NUM_POINTS-1)*(dirt_x+viewWidth/2)/viewWidth);
 			
 			float xPercent = (dirt_x - (iFromX*w + t.x))/w;
 			float p1y = t.yValues[iFromX]*viewHeight + t.y;
 			float p2y = t.yValues[iFromX+1]*viewHeight + t.y;
 			float top = p1y + (p2y - p1y)*xPercent;
 			if (top > tCircleY) 
 			{
 				float j = 0;
 				while(tCircleY + j <= top)
 				{
 					Dirt dirtPoint = new Dirt();
 					dirtPoint.x = dirt_x;
 					dirtPoint.y = tCircleY+j;
 					dirtPoint.width = DIRT_SIZE*2;
 					dirtPoint.height = DIRT_SIZE*2;
 					dirt.add(dirtPoint);
 					renderer.registerDrawable(dirtPoint);
					j += DIRT_SIZE;
 				}
 			}
 		}
 		
 		for (int i = minX; i <= maxX; i++) 
 		{
 			float xFromI = (i/(t.NUM_POINTS-1.f))*viewWidth+t.x;
 			float offset = (float) Math.sqrt(r*r - Math.pow(xFromI - x, 2));
 			float bCircleY = (float) (y - offset);
 			if (t.yValues[i]*viewHeight+t.y > bCircleY) 
 			{
 				t.yValues[i] = (bCircleY+viewHeight/2)/viewHeight;
 			}
 		}
 		t.buildGeometry(viewWidth, viewHeight);
 		t.finalizeGeometry();
 	}
 }
