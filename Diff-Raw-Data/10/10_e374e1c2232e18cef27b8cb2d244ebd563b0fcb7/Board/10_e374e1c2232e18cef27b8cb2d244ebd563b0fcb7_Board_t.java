 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 public class Board extends Canvas implements MouseListener, KeyListener,
 		MouseMotionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4116736011187995285L;
 
 	public static final int LEFT_MARGIN_WIDTH = 256, TOP_MARGIN_HEIGHT = 64,
 			BORDER_WIDTH = 8, TOP_BORDER_HEIGHT = 32, BOTTOM_BORDER_HEIGHT = 8,
 			RIGHT_MARGIN_WIDTH = 64, BOTTOM_MARGIN_HEIGHT = 64;
 	public static final int FPS = 30, SKIP_TICKS = 1000 / FPS;
 
 	private Grid _grid;
 	private BufferedImage _background;
 	private Cursor _cursor;
 	private Font _font;
 
 	private boolean _running, _swapping;
 	private int _cursorGridX, _cursorGridY, _selectedGemX, _selectedGemY,
 			_score;
 
 	public Board() {
 		_grid = new Grid(6, 10);
 		try {
 			_background = ImageIO.read(new File("res/background.png"));
 			InputStream stream = new FileInputStream("res/font.ttf");
 			_font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(32);
 		} catch (IOException e) {
 			System.out.println("Problem with loading background");
 		} catch (FontFormatException e) {
 			e.printStackTrace();
 		}
 		_cursor = new Cursor();
 		_cursorGridX = 2;
 		_cursorGridY = 3;
 		addKeyListener(this);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		_selectedGemX = _selectedGemY = -1;
 		_swapping = false;
 		_score = 0;
 	}
 
 	public void run() {
 		long nextTick = System.currentTimeMillis();
 		long sleepTime = 0;
 		_running = true;
		createBufferStrategy(5);
 		BufferStrategy strategy = getBufferStrategy();
 		while (_running) {
 	        Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
	        
 			update(graphics);
			
 			
 			nextTick += SKIP_TICKS;
 			sleepTime = nextTick - (System.currentTimeMillis());
 			if (sleepTime >= 0) {
 				try {
 					Thread.sleep(sleepTime);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 	        graphics.dispose();
 	        strategy.show();
 		}
 	}
 
 	public void shutdown() {
 		_running = false;
 	}
 
 	public void paint(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		
 		
 		
 		
 		g2.setColor(Color.BLACK);
 		g2.drawImage(_background, 0, 0, 720, 808, 0, 0, 180, 202, null);
 
 		_grid.draw(g2);
 		_cursor.draw(g2);
 		g2.drawString("score:", 32, 460);
 		g2.drawString("" + _score, 32, 480);
 	}
 
 	public void update(Graphics2D g) {
 		_grid.update();
 		for (int x = 0; x < Grid.GRID_WIDTH; x++) {
 			_score += _grid.searchCol(x);
 		}
 		for (int y = 0; y < Grid.GRID_HEIGHT; y++)
 			_score += _grid.searchRow(y);
 		_grid.removeGems();
 		_cursor.update();
 		_cursor.setGridCor(_cursorGridX, _cursorGridY);
 	
 		
 		Graphics offgc;
 	    Image offscreen = null;
 
 	    // create the offscreen buffer and associated Graphics
 	    offscreen = createImage(720,808); //createImage(720, 808);
 	    offgc = offscreen.getGraphics();
 	    
 	    // clear the exposed area
 
 	    // do normal redraw
 	    paint(offgc);
 	    
 	    // transfer offscreen to window
 	    g.drawImage(offscreen, 0, 0, this);
 		
 		
 		
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		mouseMoved(e);
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 
 		x = x - LEFT_MARGIN_WIDTH - BORDER_WIDTH;
 		y = y - TOP_MARGIN_HEIGHT - TOP_BORDER_HEIGHT;
 
 		x /= 64;
 		y /= 64;
 		if (x >= 0 && x <= 5 && y >= 0 && y <= 9) {
 			_cursorGridX = x;
 			_cursorGridY = y;
 		}
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 
 		x = x - LEFT_MARGIN_WIDTH - BORDER_WIDTH;
 		y = y - TOP_MARGIN_HEIGHT - TOP_BORDER_HEIGHT;
 
 		x /= 64;
 		y /= 64;
 		if (x >= 0 && x <= 5 && y >= 0 && y <= 9) {
 			_cursorGridX = x;
 			_cursorGridY = y;
 		}
 
 		boolean selected = _grid.getGem(_cursorGridX, _cursorGridY)
 				.isSelected();
 
 		if (_selectedGemX != -1 && _selectedGemY != -1) {
 			if (_cursorGridX - _selectedGemX == 1
 					&& _cursorGridY - _selectedGemY == 0) {
 				_grid.setGem(_grid.getGem(_selectedGemX, _selectedGemY)
 						.swapRight().deselect(), _selectedGemX, _selectedGemY);
 				_grid.setGem(_grid.getGem(_cursorGridX, _cursorGridY)
 						.swapLeft().deselect(), _cursorGridX, _cursorGridY);
 				_grid.switchGems(_selectedGemX, _selectedGemY, _cursorGridX,
 						_cursorGridY);
 				_selectedGemX = _selectedGemY = -1;
 				_score -= 20;
 				_swapping = true;
 			} else if (_cursorGridX - _selectedGemX == 0
 					&& _cursorGridY - _selectedGemY == 1) {
 				_grid.setGem(_grid.getGem(_selectedGemX, _selectedGemY)
 						.swapDown().deselect(), _selectedGemX, _selectedGemY);
 				_grid.setGem(_grid.getGem(_cursorGridX, _cursorGridY).swapUp()
 						.deselect(), _cursorGridX, _cursorGridY);
 				_grid.switchGems(_selectedGemX, _selectedGemY, _cursorGridX,
 						_cursorGridY);
 				_selectedGemX = _selectedGemY = -1;
 				_score -= 20;
 				_swapping = true;
 			} else if (_cursorGridX - _selectedGemX == -1
 					&& _cursorGridY - _selectedGemY == 0) {
 				_grid.setGem(_grid.getGem(_selectedGemX, _selectedGemY)
 						.swapLeft().deselect(), _selectedGemX, _selectedGemY);
 				_grid.setGem(_grid.getGem(_cursorGridX, _cursorGridY)
 						.swapRight().deselect(), _cursorGridX, _cursorGridY);
 				_grid.switchGems(_selectedGemX, _selectedGemY, _cursorGridX,
 						_cursorGridY);
 				_selectedGemX = _selectedGemY = -1;
 				_score -= 20;
 				_swapping = true;
 			} else if (_cursorGridX - _selectedGemX == 0
 					&& _cursorGridY - _selectedGemY == -1) {
 				_grid.setGem(_grid.getGem(_selectedGemX, _selectedGemY)
 						.swapUp().deselect(), _selectedGemX, _selectedGemY);
 				_grid.setGem(_grid.getGem(_cursorGridX, _cursorGridY)
 						.swapDown().deselect(), _cursorGridX, _cursorGridY);
 				_grid.switchGems(_selectedGemX, _selectedGemY, _cursorGridX,
 						_cursorGridY);
 				_selectedGemX = _selectedGemY = -1;
 				_score -= 20;
 				_swapping = true;
 
 			} else {
 				_grid.setGem(_grid.getGem(_selectedGemX, _selectedGemY)
 						.deselect(), _selectedGemX, _selectedGemY);
 			}
 		}
 
 		if (!selected) {
 			if (!_swapping) {
 				_grid.setGem(_grid.getGem(_cursorGridX, _cursorGridY).select(),
 						_cursorGridX, _cursorGridY);
 				_selectedGemX = _cursorGridX;
 				_selectedGemY = _cursorGridY;
 			}
 			_swapping = false;
 		} else {
 			_grid.setGem(_grid.getGem(_cursorGridX, _cursorGridY).deselect(),
 					_cursorGridX, _cursorGridY);
 			_selectedGemX = _selectedGemY = -1;
 		}
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
