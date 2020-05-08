 package initialpackage;
 
 public class Background {
 	private int backgroundX;
 	private int backgroundY;
 	private int backgroundSpeedX;
 	
 	private static int MAGIC_NUMBER = -2160;
 	
 	public Background(int x, int y){
 		this.backgroundX = x;
 		this.backgroundY =y;
 		this.backgroundSpeedX =0;
 	}
 	
 	public void update(){
 		this.backgroundX += this.backgroundSpeedX;
 		
 		if(this.backgroundX <= MAGIC_NUMBER){
			this.backgroundX += (MAGIC_NUMBER * MAGIC_NUMBER);
 		}
 	}
 
 	/**
 	 * @return the backgroundX
 	 */
 	public int getBackgroundX() {
 		return backgroundX;
 	}
 
 	/**
 	 * @return the backgroundY
 	 */
 	public int getBackgroundY() {
 		return backgroundY;
 	}
 
 	/**
 	 * @return the backgroundSpeedX
 	 */
 	public int getBackgroundSpeedX() {
 		return backgroundSpeedX;
 	}
 
 	/**
 	 * @param backgroundSpeedX the backgroundSpeedX to set
 	 */
 	public void setBackgroundSpeedX(int backgroundSpeedX) {
 		this.backgroundSpeedX = backgroundSpeedX;
 	}
 }
