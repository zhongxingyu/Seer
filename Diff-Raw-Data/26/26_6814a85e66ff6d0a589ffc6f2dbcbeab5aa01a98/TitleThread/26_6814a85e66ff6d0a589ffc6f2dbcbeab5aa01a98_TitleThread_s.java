 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Paint;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 
 public class TitleThread extends GameThread {
 
 
 	private BufferedImage img;
 	private BufferedImage textImage;
 	private BufferedImage lightningImage;
 	private BufferedImage rainTileImage;
 	private Sprite coinImage;
 	private BufferedImage startImage;
 
 	private long lastLightningTime = 0;
 	private int randomLightning;
 	private int lightningPos = 0;
 	private int lightningFlip = -1;
 	private long lastBlinkTime = 0;
 	private boolean coinDisplay = false;
 
 
 	private int rainX = 0, rainY = 0;
 
 	public TitleThread(Skeleton parent){
 		super(parent);
 
 		try {
 			img = ImageIO.read(new File("img/title_bg.png"));
 			textImage = ImageIO.read(new File("img/title_text.png"));
 			lightningImage = ImageIO.read(new File("img/lightning.png"));
 			rainTileImage = ImageIO.read(new File("img/rainTile.png"));
 
 			coinImage = parent.spriteBank.getSpriteByName("CoinImage");
 			startImage = ImageIO.read(new File("img/pressStart.png"));
 

 		} catch (IOException e) {
 			System.out.println("fucked");
 		}
 		
 		isReady = true;
 
 	}
 	public void start(){
 		System.out.println("starting title...");
 		super.start();
 		
 		soundManager.playSound(SoundClip.LIGHTNING);
 		randomLightning = 150 + (int) (Math.random() * 1000);
 		lightningPos = 100 + (int)(Math.random() * 400.0f);
 		soundManager.setBgm(SoundClip.RAINBGM);
 	}
 
 	public void stop(){
 		super.stop();
 		
 	}
 
 	public void updateState(){	
 		super.updateState(); // needed for sound manager to trigger
 		int keyMask = inputEngine.getKeyMask();
 		if(keyMask != 0){
 
 
 		}
 
 		//lightning logic
 		if(lastLightningTime  + randomLightning < System.currentTimeMillis()){
 			randomLightning = 150 + (int) (Math.random() * 1500.0f);
 			lastLightningTime = System.currentTimeMillis();
 			soundManager.playSound(SoundClip.LIGHTNING);
 			lightningPos = 100 + (int)(Math.random() * 400.0f);
 			lightningFlip = Math.random() < 0.5 ? -1 : 1;
 
 		}
 
 
 		//update rain pos
 		rainX += -4;
 		rainX %= 32;
 		rainY += 9;
 		rainY %= 32;
 
 		if(lastBlinkTime + 1000 < System.currentTimeMillis()){
 			lastBlinkTime = System.currentTimeMillis();
 			coinDisplay = !coinDisplay;
 		} 
 
 		
 	}
 
 	public void handleInputEvent(int evt){
 		if(isRunning){
 			if(evt == InputEngine.KEY_COIN){
 				parent.insertCoin();
 				soundManager.playSound(SoundClip.COININSERT);
 			} else if (evt == InputEngine.KEY_ESC){
 				parent.quit();
 			} else if (evt == InputEngine.KEY_START ){
 				if(parent.credits > 0){
 					soundManager.setBgm(null);
 					parent.nextState();
 				} else {
 					coinDisplay = true;
 				}
 			}
 		}
 	}
 
 	public void repaint()
 	{
 		if(isRunning){
 			//System.out.println("thread draw");
 			Graphics2D g2 = (Graphics2D) bufferGraphics;
 			g2.drawImage(img,0,0,800,600, null);
 
 
 			//draw the rain pattern
 
 			for(int x = 0; x < 896 / 64; x++){
 				for(int y = 0; y < 640 / 64; y++){
 					g2.drawImage(rainTileImage, x * 64 + rainX, y * 64 + rainY, 64, 64, null);
 
 				}
 			}
 
 
 			//draw lightning overlay
 			int alpha = (int)(System.currentTimeMillis() - lastLightningTime);
 			if(alpha < 255 && alpha >= 0){
 				g2.setColor(new Color(255,255,255,255 - alpha));
 
 				g2.drawImage(lightningImage, lightningPos, 170, lightningImage.getWidth() * lightningFlip * 2, lightningImage.getHeight() * 2, null);
 
 				g2.fillRect(0, 0, 800, 600);
 
 			}
 
 			//draw title text
 			g2.setColor(new Color(255,255,255,255));
 			g2.drawImage(textImage, 10,10,textImage.getWidth() * 4, textImage.getHeight() * 4, null);
 
 			if(coinDisplay ){
 				if(parent.credits == 0){
 					g2.drawImage(coinImage.imageData, 280, 480, coinImage.imageData.getWidth() * 4, coinImage.imageData.getHeight() * 4, null);
 				} else {
 					g2.drawImage(startImage, 280, 480, startImage.getWidth() * 4, startImage.getHeight() * 4, null);
 				}
 			}
 
 			
 		}
 	}
 
 
 }
