 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 public class Bomb {
 	
 	static boolean death=false;
 	
 	//Read actual position
 	static double x;
 	static double y;
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws InterruptedException 
 	 */
 	public static void use() throws IOException, InterruptedException {
 		// TODO Auto-generated method stub
 
 		//final double x=Globals.x;
 		//final double y=Globals.y;
 		x=Globals.x;
 		y=Globals.y;
 		
 		if (Globals.weapon>0)
 		{
 			Globals.weapon--;
 		
 	
 		//Paint bomb
 		Globals.draw.bomb=true;
 		
 		//class for explosion
 				class explode extends TimerTask 
 				{
 					@Override public void run()
 					{
 						//stop painting the bomb
 						Globals.draw.bomb=false;
 						
 						//paint explosion
 						Globals.draw.explosion=true;
 						
 						//checking potential damages
 						//checking player:
 						if (Math.abs(x-Globals.x)<=0.1 && Math.abs(y-Globals.y)<=0.1);
 						{
 							//Death
 							if (Globals.life>0)
 							{
 								//Losing one life:
 								Globals.life--;
 								
 								//Back to start position
 								Globals.x=Globals.startx;
 								Globals.y=Globals.starty;
 							}
 							else //No more lifes 
 							{
 								death=true;
 							}
 						}
 						//checking boss
 						if (Globals.boss!=null && Math.abs(x-Globals.boss[0])<=0.1 && Math.abs(y-Globals.boss[1])<=0.1){
							Globals.health_boss--;
							if (Globals.health_boss==0) Globals.boss=null;
 						}
 					}
 					
 				}
 				
 				//class for ending explosion
 				class end extends TimerTask 
 				{
 					@Override public void run()
 					{
 						//stop painting explosion
 						Globals.draw.explosion=false;	
 					}
 				}
 		
 			
 				//wait 4 seconds until explosion
 				Timer timer = new Timer();
 	    		timer.schedule(new explode(), 4000 );
 	    		if (death)
 	    		{
 	    			//Back to menu
 	    			Menue.main(null);
 	    		}
 	    		
 	    		//display explosion for 1 second
 	    		timer.schedule(new end(), 100 );
 	    		
 	    		
 		}
 
 	
 	}
 	
 }
