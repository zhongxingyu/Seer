 package pacmon.view.screen.effect;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import pacmon.SpriteManager;
 import pacmon.view.screen.Screen;
 
 public class MainMenuScreenEffect extends ScreenEffect {
 
 	public static final int EFFECT_TILE_SIZE = 35;
 
 	private int t;
 	
 	private List<EffectTile[]> tileRows;
 	private List<BufferedImage> spriteImages;
 	
 	private Random randGen;
 	
 	public MainMenuScreenEffect()
 	{
 		t = -EFFECT_TILE_SIZE;
 		tileRows = new LinkedList<EffectTile[]>();
 		spriteImages = new ArrayList<BufferedImage>();
 		
 		//init();
 		
 		//SpriteManager.getBonusSpriteImage().getSubimage(x, y, w, h);
 		BufferedImage bonusSpriteImage = SpriteManager.getBonusSpriteImage();
 		
 		int imagesSize = bonusSpriteImage.getHeight();		
 		int imagesCount = bonusSpriteImage.getWidth() / imagesSize;
 		
 		System.out.println(String.format("sprite images: %s", imagesCount));
 		
 		for (int i=0; i<imagesCount; ++i)
 		{
 			spriteImages.add(bonusSpriteImage.getSubimage(i * imagesSize , 0, imagesSize, imagesSize));
 		}
 	}
 	
 	public void screenShow(Screen screen)
 	{
 		t = -EFFECT_TILE_SIZE;
 		
 		init(screen);
 	}
 	
 	private void init(Screen screen)
 	{
		randGen = new Random();
		
 		screen.getRootManager().getWidth();
 		
 		tileRows.clear();
 		for (int i=0; i < screen.getRootManager().getHeight() / EFFECT_TILE_SIZE + 1; ++i)
 		{
 			tileRows.add(generateRandomRow(screen));
 		}
 	}
 	
 	private void shiftRowsDown(Screen screen)
 	{
 		tileRows.remove(tileRows.size() - 1);
 		tileRows.add(0, generateRandomRow(screen));
 	}
 	
 	private EffectTile[] generateRandomRow(Screen screen)
 	{
		Random randGen = new Random();
 		
 		int cols = screen.getRootManager().getWidth() / EFFECT_TILE_SIZE; 
 		
 		EffectTile[] result = new EffectTile[cols];
 		
 		for (int i=0; i< cols; ++i)
 		{
			result[i] = new EffectTile(spriteImages.get(randGen.nextInt(spriteImages.size())));
 		}
 		
 		return result;
 	}
 	
 	public void screenHide(Screen screen)
 	{
 		
 	}
 	
 	public void render(Screen screen)
 	{
 		BufferedImage screenImage = screen.getImage();
 		 
 		Graphics2D g2 = (Graphics2D)screenImage.getGraphics();
 		
 		// clear screen
 		g2.setColor(Color.BLACK);
 		g2.fillRect(0,  0, screenImage.getWidth(), screenImage.getHeight());
 		
 		for (int rowIdx=0; rowIdx<tileRows.size(); ++rowIdx)
 		{
 			EffectTile[] row = tileRows.get(rowIdx);
 			for (int tileIdx=0; tileIdx<row.length; ++tileIdx)
 			{
 				EffectTile tile = row[tileIdx];
 				
				g2.drawImage(tile.image, 
						(tileIdx * EFFECT_TILE_SIZE) + EFFECT_TILE_SIZE / 2, 
						(rowIdx * EFFECT_TILE_SIZE) + EFFECT_TILE_SIZE / 2 + t, 
						null);
 			}
 		}
 	}
 	
 	public void update(Screen screen)
 	{
 		t++;
 		
 		if (t == 0)
 		{
 			t = -EFFECT_TILE_SIZE;
 			shiftRowsDown(screen);
 		}
 	}
 	
 	static class EffectTile
 	{
 		public BufferedImage image;
 		
 		public EffectTile(BufferedImage image)
 		{
 			this.image = image;
 		}
 	}
 	
 }
