 package Snake;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class SnakeKeyListener implements KeyListener {
 	Snake game;
 	public SnakeKeyListener(Snake g){
 		game = g;
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(game.over && e.getKeyChar() == 'r'){
 			game.setup();
 			return;
 		}
 		
 		if(e.getKeyChar() == 'w' && Snake.snake.get(0).yD == 0){
 			Snake.snake.get(0).xD = 0;
 			Snake.snake.get(0).yD = -1;
 			
 			SnakePart s = Snake.snake.get(0);
 
 			if(s.x >= Snake.wa || s.y >= Snake.ha || s.x < 0 || s.y < 0){
 				game.gameEnd();
 				return;
 			}
 			
 			game.board[(int) Snake.snake.get(0).x][(int) Snake.snake.get(0).y] = 3;
			game.tick();
			game.render();
			game.repaint();
 		}else if(e.getKeyChar() == 's' && Snake.snake.get(0).yD == 0){
 			Snake.snake.get(0).xD = 0;
 			Snake.snake.get(0).yD = 1;
 			
 			SnakePart s = Snake.snake.get(0);
 
 			if(s.x >= Snake.wa || s.y >= Snake.ha || s.x < 0 || s.y < 0){
 				game.gameEnd();
 				return;
 			}
 			
 			game.board[(int) Snake.snake.get(0).x][(int) Snake.snake.get(0).y] = 1;
			game.tick();
			game.render();
			game.repaint();
 		}else if(e.getKeyChar() == 'a' && Snake.snake.get(0).xD == 0){
 			Snake.snake.get(0).xD = -1;
 			Snake.snake.get(0).yD = 0;
 			
 			SnakePart s = Snake.snake.get(0);
 			
 			if(s.x >= Snake.wa || s.y >= Snake.ha || s.x < 0 || s.y < 0){
 				game.gameEnd();
 				return;
 			}
 			
 			game.board[(int) Snake.snake.get(0).x][(int) Snake.snake.get(0).y] = 2;
			game.tick();
			game.render();
			game.repaint();
 		}else if(e.getKeyChar() == 'd' && Snake.snake.get(0).xD == 0){
 			Snake.snake.get(0).xD = 1;
 			Snake.snake.get(0).yD = 0;
 			
 			SnakePart s = Snake.snake.get(0);
 
 			if(s.x >= Snake.wa || s.y >= Snake.ha || s.x < 0 || s.y < 0){
 				game.gameEnd();
 				return;
 			}
 			
 			game.board[(int) Snake.snake.get(0).x][(int) Snake.snake.get(0).y] = 4;
			game.tick();
			game.render();
			game.repaint();
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		
 	}
 	
 
 }
