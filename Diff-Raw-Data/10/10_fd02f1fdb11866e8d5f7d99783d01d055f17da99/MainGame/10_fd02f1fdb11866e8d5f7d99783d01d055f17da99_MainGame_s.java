 package game;
 
 import java.awt.Graphics2D;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 
 import fr.umlv.zen2.Application;
 import fr.umlv.zen2.ApplicationCode;
 import fr.umlv.zen2.ApplicationContext;
 import fr.umlv.zen2.ApplicationRenderCode;
 import fr.umlv.zen2.MotionEvent;
 
 public class MainGame {
 
 	public static void main(String[] args) {
 		final Game game;
 
 		try {
 			game = new Game();
 		} catch (IOException e1) {
 			System.out.println("Lanch failure");
 			e1.printStackTrace();
 			return;
 		}
 
 		Application.run(Variables.APPLICATION_NAME, Variables.SCREEN_WIDTH, Variables.SCREEN_HEIGHT, new ApplicationCode() {
 
 			@Override
 			public void run(final ApplicationContext context) {   
 				/*
 				 * Initialisation
 				 */
 				context.render(
 						new ApplicationRenderCode() {  
 							@Override
 							public void render(Graphics2D graphics) {
 								JFrame.getFrames()[0].setLocationRelativeTo(null);
 								game.init(graphics);
 							}
 						}
 				);
 		
 				
 				/*
 				 * La boucle principale
 				 */
 				for(;;) {
 					
 					/*
 					 * Events
 					 */
					final MotionEvent event = context.pollMotion();
					if (event != null) {
 						game.event(event);
 					}
 					
 					/*
 					 * L'affichage
 					 */
 					context.render(
 							new ApplicationRenderCode() {
 								@Override
 								public void render(Graphics2D graphics) {
 									game.run(graphics);
 								}
 							}
 					);
 				}
 
 			}
 		});
 
 	}
 }
