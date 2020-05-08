 package gdp.racetrack;
 
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayDeque;
 import java.util.Deque;
 import javax.imageio.ImageIO;
 import gdp.racetrack.Point;
 
 public class MapGenerator {
 
 	/**
 	 * Generate a new map by the given seed.
 	 * @param seed The seed to be used for generating the map
 	 * @return The new generated map
 	 */
 	public Map generateMap(int seed, Vec2D size, int numberPlayers) {
 		BufferedImage mapImage = new BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_RGB);
 		
 		for(int x=0; x<size.x; x++){
 			for(int y=0; y<size.y; y++){
 				mapImage.setRGB(x,y,Map.COLOR_BACKGROUND);
 			}
 		}
 		
 		PerlinNoise noise1D = new PerlinNoise(1, 1, 1, seed);
 		Vec2D relPoints[][] = new Vec2D[4][4];
 		for(int i=0; i<4; i++)
 		{
 			for(int j=0; j<4; j++)
 			{
 				double pos[] = new double[1];
 				pos[0] = i*8+j*2;
 				int x = (int) ((noise1D.getPerlinNoise(pos)+1)/2*size.x/8 + size.x/16);
 				pos[0] = i*8+j*2+1;
 				int y = (int) ((noise1D.getPerlinNoise(pos)+1)/2*size.y/8 + size.y/16);
 				relPoints[i][j] = new Vec2D(x,y);
 			}
 		}
 		
 		Vec2D absPoints[][] = new Vec2D[4][4];
 		for(int i=0; i<4; i++)
 		{
 			for(int j=0; j<4; j++)
 			{
 				absPoints[i][j] = new Vec2D(relPoints[i][j].x + (i%2)*size.x/2 + (j%2)*size.x/4, relPoints[i][j].y + (i/2)*size.y/2 + (j/2)*size.y/4);
 			}
 		}
 		absPoints[0][0] = new Vec2D(absPoints[0][0].x - absPoints[0][0].x%16, absPoints[0][0].y - absPoints[0][0].y%16);
 		absPoints[0][2] = new Vec2D(absPoints[0][0].x, absPoints[0][0].y+(numberPlayers+2)*16);
 		
 		
 		Vec2D startPoints[] = new Vec2D[16];
 		Vec2D endPoints[] = new Vec2D[16];
 
 		startPoints[0]  = absPoints[0][0];
 		startPoints[1]  = absPoints[0][1];
 		startPoints[2]  = absPoints[1][0];
 		startPoints[3]  = absPoints[1][1];
 		startPoints[4]  = absPoints[1][3];
 		startPoints[5]  = absPoints[3][1];
 		startPoints[6]  = absPoints[3][3];
 		startPoints[7]  = absPoints[3][2];
 		startPoints[8]  = absPoints[2][3];
 		startPoints[9]  = absPoints[2][2];
 		startPoints[10] = absPoints[2][0];
 		startPoints[11] = absPoints[2][1];
 		startPoints[12] = absPoints[3][0];
 		startPoints[13] = absPoints[1][2];
 		startPoints[14] = absPoints[0][3];
 		startPoints[15] = absPoints[0][2];
 		for(int i=1; i<17; i++)
 		{
 			endPoints[i%16] = startPoints[i-1];
 		}
 		
 		for(int i=0; i<16; i++)
 		{
 			if(i==0)
 				drawLine(startPoints[i], endPoints[i], mapImage, seed+i*2, Map.COLOR_START, 0);
 			else if(i==10)
 				drawLine(startPoints[i], endPoints[i], mapImage, seed+i*2, Map.COLOR_FINISH, 0);
 			else
 				drawLine(startPoints[i], endPoints[i], mapImage, seed+i*2, 0x000000, Math.min(mapImage.getHeight(), mapImage.getWidth())/32);
 				
 		}
 		fillImage(mapImage, (absPoints[0][0].x + absPoints[0][3].x)/2, (absPoints[0][0].y + absPoints[0][3].y)/2);
 		
 		AffineTransform at = AffineTransform.getTranslateInstance(0,0);  
         at.rotate(Math.toRadians(90)*(seed%4), mapImage.getWidth()/2, mapImage.getHeight()/2);  
         AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
         mapImage = op.filter(mapImage, null);
                 
 		Map map = new Map(mapImage);
 		return map;
 	}
 	
 	private void drawLine(Vec2D p1, Vec2D p2, BufferedImage image, int seed, int color, int scattering)
 	{
 		int max = Math.max(image.getHeight(), image.getWidth());
 		
 		PerlinNoise noiseX = new PerlinNoise(1, 0.5, 9, seed);		
 		PerlinNoise noiseY = new PerlinNoise(1, 0.5, 9, seed+1);
 		double pos[] = new double[1];
 		pos[0] = 0;
 		int startX = (int) (noiseX.getPerlinNoise(pos)*scattering);
 		int startY = (int) (noiseY.getPerlinNoise(pos)*scattering);
 		pos[0] = 8/Math.PI;
 		int endX = (int) (noiseX.getPerlinNoise(pos)*scattering);
 		int endY = (int) (noiseY.getPerlinNoise(pos)*scattering);
 		
 		for(int i=0; i<=max; i++)
 		{
 			pos[0] = i*8/Math.PI/max;
 			int x = (int) (p1.x + (p2.x - p1.x)*i/max + noiseX.getPerlinNoise(pos)*scattering - startX - (endX - startX)*i/max);
 			int y = (int) (p1.y + (p2.y - p1.y)*i/max + noiseY.getPerlinNoise(pos)*scattering - startY - (endY - startY)*i/max);
 			for(int dx = -1; dx<=1; dx++)
 			{
 				for(int dy = -1; dy<=1; dy++)
 				{
 					image.setRGB(x+dx, y+dy, color);			
 				}
 			}
 		}
 		
 	}
 	
 	private void fillImage(BufferedImage image, int x, int y)
 	{
 		Deque<Integer> posX = new ArrayDeque<Integer>();
 		Deque<Integer> posY = new ArrayDeque<Integer>();
 		posX.push(x);
 		posY.push(y);
 		while(!posX.isEmpty())
 		{
 			x = posX.pop();
 			y = posY.pop();
 			if(x == 0 || x == image.getWidth()-1 || y == 0 || y == image.getHeight()-1) continue;
 			
 			image.setRGB(x, y, Map.COLOR_TRACK);
 			if((image.getRGB(x-1, y)&0xFFFFFF) == Map.COLOR_BACKGROUND)
 			{
 				posX.push(x-1);
 				posY.push(y);
 			}
 			if((image.getRGB(x+1, y)&0xFFFFFF) == Map.COLOR_BACKGROUND)
 			{
 				posX.push(x+1);
 				posY.push(y);
 			}
 			if((image.getRGB(x, y-1)&0xFFFFFF) == Map.COLOR_BACKGROUND)
 			{
 				posX.push(x);
 				posY.push(y-1);
 			}
 			if((image.getRGB(x, y+1)&0xFFFFFF) == Map.COLOR_BACKGROUND)
 			{
 				posX.push(x);
 				posY.push(y+1);
 			}
 		}
 	}
 	
 	public static void main(String args[])
 	{
 		MapGenerator mapGen = new MapGenerator();
 		int maps[][] = new int[16][4];
 		for(int i=0; i<maps.length; i++)
 		{
 			maps[i][0] = 512;
 			maps[i][1] = 512;
 			maps[i][2] = 3;
 			maps[i][3] = i;
 		}
 		try {
 			for(int i=0; i<maps.length; i++)
 			{
 				Map map = mapGen.generateMap(maps[i][3], new Vec2D(maps[i][0], maps[i][1]), maps[i][2]);
 				ImageIO.write((RenderedImage) map.getImage(), "png", new File("map" + maps[i][3] + ".png"));
 				
 				BufferedImage metaImage = new BufferedImage(map.getSize().x, map.getSize().y, BufferedImage.TYPE_INT_RGB);
 				for(int x=0; x<map.getSize().x; x++)
 				{
 					for(int y=0; y<map.getSize().y; y++)
 					{
 						if(y%16 == 0 && x%16 == 0)
 						{
							switch(map.getPointType(new Point(x/16, y/16, map))){
 							case TRACK:
 								metaImage.setRGB(x, y, Map.COLOR_TRACK);
 								break;
 							case START:
 								metaImage.setRGB(x, y, Map.COLOR_START);
 								break;
 							case FINISH:
 								metaImage.setRGB(x, y, Map.COLOR_FINISH);
 								break;
 							default:
 								metaImage.setRGB(x, y, 0);
 							}
 						}else
 						{
 							metaImage.setRGB(x,y,0);
 						}
 					}
 				}
 				ImageIO.write((RenderedImage) metaImage, "png", new File("map" + maps[i][3] + "meta.png"));
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
