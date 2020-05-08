 import java.awt.Graphics2D;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 
 
 public class Gem{
 	
 	private static final int SIZE = 64;
 	private static final int SPEED = 4;
 	
 	private static final HashMap<String,BufferedImage> IMAGES = new HashMap<String,BufferedImage>();
 	private static final String[] TYPES = {"ruby", "sapphire", "emerald", "topaz", "diamond"};
 	private static BufferedImage SELECTED = null;
 	
 	private String _type;
 	private int _xGridCor, _yGridCor;
 	private double _xOffset, _yOffset, _yVel, _xVel;
 	private boolean _isFalling, _onGrid, _selected;
 	private int _sliding; //-1, 0N, 1E, 2S, 3W
 	
 	public static void setup(){
 		BufferedImage ruby = null, sapphire = null, emerald = null,
 					  topaz = null, diamond = null;
 		try {
 		    ruby = ImageIO.read(new File("res/ruby.png"));
 		    sapphire = ImageIO.read(new File("res/sapphire.png"));
 		    emerald = ImageIO.read(new File("res/emerald.png"));
 		    topaz = ImageIO.read(new File("res/shield.png"));
 		    diamond = ImageIO.read(new File("res/diamond.png"));
 		    SELECTED = ImageIO.read(new File("res/selected.png"));
 		} catch (IOException e) {
 			System.out.println("Image imports broken");
 		}
 		IMAGES.put("ruby", ruby);
 		IMAGES.put("sapphire", sapphire);
 		IMAGES.put("emerald", emerald);
 		IMAGES.put("topaz", topaz);
 		IMAGES.put("diamond", diamond);
 	}
 	
 	public Gem(int x, int y, String type){
 		_type = type;
 		_xGridCor = x;
 		_yGridCor = y /* help */;
 		_yVel = 0;
 		_xVel = 0;
 		_isFalling = false;
 		_onGrid = false;
 		_sliding = -1;
 	}
 	
 	public int getXGridCor(){
 		return _xGridCor;
 	}
 	
 	public int getYGridCor(){
 		return _yGridCor;
 	}
 	
 	public static String getType(int pos){
 		return TYPES[pos];
 	}
 	
 	public void draw(Graphics2D g){
 		g.drawImage(IMAGES.get(_type),
 				   Board.BORDER_WIDTH + Board.LEFT_MARGIN_WIDTH      + _xGridCor * SIZE + (int)_xOffset, 
 				   Board.TOP_BORDER_HEIGHT + Board.TOP_MARGIN_HEIGHT + _yGridCor * SIZE + (int)_yOffset,
 				   Board.BORDER_WIDTH + Board.LEFT_MARGIN_WIDTH      + (_xGridCor + 1) * SIZE + (int)_xOffset, 
 				   Board.TOP_BORDER_HEIGHT + Board.TOP_MARGIN_HEIGHT + (_yGridCor + 1) * SIZE + (int)_yOffset,
 			       0, 0, 16, 16,
 			       null);
 		if (_selected){
 			g.drawImage(SELECTED,
 					   Board.BORDER_WIDTH + Board.LEFT_MARGIN_WIDTH      + _xGridCor * SIZE, 
 					   Board.TOP_BORDER_HEIGHT + Board.TOP_MARGIN_HEIGHT + _yGridCor * SIZE,
 					   Board.BORDER_WIDTH + Board.LEFT_MARGIN_WIDTH      + (_xGridCor + 1) * SIZE, 
 					   Board.TOP_BORDER_HEIGHT + Board.TOP_MARGIN_HEIGHT + (_yGridCor + 1) * SIZE,
 				       0, 0, 16, 16,
 				       null);
 		}
 	}
 	
 	public void update(){
 		_yOffset += _yVel;
 		_xOffset += _xVel;
 		if (_isFalling) {
 			_yVel = .001;		
 			System.out.println("isfalling");
 		}
 		
 		if (_isFalling && _yOffset + _yVel >= _yGridCor * SIZE + Board.TOP_BORDER_HEIGHT + 
 											  Board.TOP_MARGIN_HEIGHT){
 			_isFalling = false;
 			_yOffset = 0;
 			System.out.println("wasfalling");
 		}
 		
		if(_sliding != -1) {
			if (_sliding == 0 && _yOffset <= 0 || _sliding == 1 && _xOffset >= 0 ||
				_sliding == 2 && _yOffset >= 0 || _sliding == 3 && _xOffset <= 0){
				_sliding = -1;
				_xVel = _yVel = 0;
				_xOffset = _yOffset = 0;
					
			}
 		}
 				
 		
 	}
 	
 	public Gem select(){
 		_selected = true;
 		return this;
 	}
 	public Gem deselect(){
 		_selected = false;
 		return this;
 	}
 	public boolean isSelected(){
 		return _selected;
 	}
 	
 	public Gem swapUp(){
 		_sliding = 0;
 		_yGridCor--;
 		_yOffset = SIZE;
 		_yVel = -.0008;
 		return this;
 	}
 	public Gem swapDown(){
 		_sliding = 2;
 		_yGridCor++;
 		_yOffset = -SIZE;
 		_yVel = .0008;
 		return this;
 	}
 	public Gem swapLeft(){
 		_sliding = 3;
 		_xGridCor--;
 		_xOffset = SIZE;
 		_xVel = -.0008;
 		return this;
 	}
 	public Gem swapRight(){
 		_sliding = 1;
 		_xGridCor++;
 		_xOffset = -SIZE;
 		_xVel = .0008;
 		return this;
 	}
 
 }
