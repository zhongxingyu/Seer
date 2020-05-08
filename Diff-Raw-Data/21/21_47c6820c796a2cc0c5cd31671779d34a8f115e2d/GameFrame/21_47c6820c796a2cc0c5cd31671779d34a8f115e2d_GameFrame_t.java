 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import net.phys2d.math.Vector2f;
 
 import view.world.GameLevel;
 import view.world.GameWorld;
 import view.world.level1.L1M1;
 
 public class GameFrame extends JFrame implements KeyListener
 {
 	private static final long	serialVersionUID	= 1L;
 	private GameLevel content,previousMap;
 	private LoadScreen loading;
 	private int leftKey,rightKey,switchKey,enterKey,selectedKey;
 	private boolean keybinding = false;
	private GameWorld world;
 
 	public GameFrame(GameWorld world)
 	{
 		super("Gravity Control");
		this.world = world;
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		loading = new LoadScreen(world,this);
 		content = loading;
 		setFocusable(true);
 		addKeyListener(this);
 		requestFocus();
 		setContentPane(content);
 		leftKey = KeyEvent.VK_LEFT;
 		rightKey = KeyEvent.VK_RIGHT;
 		switchKey = KeyEvent.VK_Z;
 		enterKey = KeyEvent.VK_SPACE;
 	}
 
 	@Override
 	public void keyPressed(KeyEvent ke)
 	{
 		if(!keybinding)
 		{
 			if(ke.getKeyCode() == switchKey)
 				content.up();
			else if(ke.getKeyCode() == leftKey)
 				content.left();
			else if(ke.getKeyCode() == rightKey)
 				content.right();
 			else if(ke.getKeyCode() == KeyEvent.VK_UP)
 				content.up();
 			else if(ke.getKeyCode() == KeyEvent.VK_DOWN)
 				content.down();
 		}
 		else
 			changeKey(ke.getKeyCode());
 	}
 
 	@Override
 	public void keyReleased(KeyEvent ke)
 	{
 		if(ke.getKeyCode() == KeyEvent.VK_ENTER)
 			content.enter();
 		else if(ke.getKeyCode() == enterKey)
 			content.enter();
 		else if(ke.getKeyCode() == KeyEvent.VK_ESCAPE)
 			content.escape();
 	}
 
 	@Override
 	public void keyTyped(KeyEvent ke)
 	{
 	}
 	
 	public void loadMap(GameLevel map)
 	{
 		if(!content.getClass().equals(MenuPanel.class) &&
 		   !content.getClass().equals(AdvancedControlPanel.class) &&
 		   !content.getClass().equals(LoadScreen.class))
				previousMap = content;
 		content.stop();
 		content = map;
 		setContentPane(content);
 		content.start();
 		validate();
 		repaint();
 	}
 
 	public int getLeftKey()
 	{
 		return leftKey;
 	}
 
 	public void setLeftKey(int leftKey)
 	{
 		this.leftKey = leftKey;
 	}
 
 	public int getRightKey()
 	{
 		return rightKey;
 	}
 
 	public void setRightKey(int rightKey)
 	{
 		this.rightKey = rightKey;
 	}
 
 	public int getSwitchKey()
 	{
 		return switchKey;
 	}
 
 	public void setSwitchKey(int switchKey)
 	{
 		this.switchKey = switchKey;
 	}
 
 	public int getEnterKey()
 	{
 		return enterKey;
 	}
 
 	public void setEnterKey(int enterKey)
 	{
 		this.enterKey = enterKey;
 	}
 	
 	public boolean isKeybinding()
 	{
 		return keybinding;
 	}
 
 	public int getSelectedKey()
 	{
 		return selectedKey;
 	}
 
 	public void setSelectedKey(int selectedKey)
 	{
 		this.selectedKey = selectedKey;
 	}
 
 	public void setKeybinding(boolean keybinding)
 	{
 		this.keybinding = keybinding;
 	}
 	
 	public void changeKey(int keyCode)
 	{
 		if(keyCode != KeyEvent.VK_ESCAPE)
 		{
 			switch(selectedKey)
 			{
 			case 0: // do nothing
 				break;
 			case 1: //left
 				if(keyCode != KeyEvent.VK_RIGHT && keyCode != KeyEvent.VK_DOWN && keyCode != KeyEvent.VK_ENTER)
 				{
 					leftKey = keyCode;
 					if(keyCode == rightKey)
 						rightKey = KeyEvent.VK_RIGHT;
 					if(keyCode == switchKey)
 						switchKey = KeyEvent.VK_DOWN;
 					if(keyCode == enterKey)
 						enterKey = KeyEvent.VK_ENTER;
 				}
 				break;
 			case 2: //right
 				if(keyCode != KeyEvent.VK_LEFT && keyCode != KeyEvent.VK_DOWN && keyCode != KeyEvent.VK_ENTER)
 				{
 					rightKey = keyCode;
 					if(keyCode == leftKey)
 						leftKey = KeyEvent.VK_LEFT;
 					if(keyCode == switchKey)
 						switchKey = KeyEvent.VK_DOWN;
 					if(keyCode == enterKey)
 						enterKey = KeyEvent.VK_ENTER;
 				}
 				break;
 			case 3: //switch
 				if(keyCode != KeyEvent.VK_LEFT && keyCode != KeyEvent.VK_RIGHT && keyCode != KeyEvent.VK_ENTER)
 				{
 					switchKey = keyCode;
 					if(keyCode == leftKey)
 						leftKey = KeyEvent.VK_LEFT;
 					if(keyCode == rightKey)
 						rightKey = KeyEvent.VK_RIGHT;
 					if(keyCode == enterKey)
 						enterKey = KeyEvent.VK_ENTER;
 				}
 				break;
 			case 4: //enter
 				if(keyCode != KeyEvent.VK_LEFT && keyCode != KeyEvent.VK_RIGHT && keyCode != KeyEvent.VK_DOWN)
 				{
 					enterKey = keyCode;
 					if(keyCode == leftKey)
 						leftKey = KeyEvent.VK_LEFT;
 					if(keyCode == rightKey)
 						rightKey = KeyEvent.VK_RIGHT;
 					if(keyCode == switchKey)
 						switchKey = KeyEvent.VK_DOWN;
 				}
 				break;
 			}
 			selectedKey = 0;
 			keybinding = false;
 		}
 	}
 	
 	public GameLevel getPreviousMap()
 	{
		if(previousMap == null)
			previousMap = new L1M1(world,this, new Vector2f(400f,320));	
 		return previousMap;
 	}
 
 	private class LoadScreen extends GameLevel implements ActionListener
 	{
 		private static final long	serialVersionUID	= 1L;
 		private double direction;
 		private Timer timer;
 		
 		public LoadScreen(GameWorld world,GameFrame gframe)
 		{
 			super(world,gframe);
 			setPreferredSize(new Dimension(900,600));
 			direction = 0;
 			timer = new Timer(1000/60, this);
 			this.start();
 		}
 		
 		public void paintComponent(Graphics g)
 		{
 			super.paintComponent(g);
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.translate(450, 250);
 			Ellipse2D circle = new Ellipse2D.Double(-25, -25, 50, 50);
 			for(int i = 0; i < 8; i++)
 			{
 				g2.setColor(new Color(0,0,0,25*i+25));
 				Ellipse2D ball = new Ellipse2D.Double(-15, -15, 10, 10);
 				AffineTransform tr = new AffineTransform();
 				tr.rotate(direction+i*(2*Math.PI/8),0,0);
 				g2.fill(tr.createTransformedShape(ball));
 			}
 			g2.setColor(Color.BLACK);
 			g2.draw(circle);
 		}
 		@Override
 		public void up()
 		{
 		}
 
 		@Override
 		public void down()
 		{
 		}
 
 		@Override
 		public void left()
 		{
 		}
 
 		@Override
 		public void right()
 		{
 		}
 
 		@Override
 		public void enter()
 		{
 		}
 		public void escape()
 		{
 			System.exit(0);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent arg0)
 		{
 			this.requestFocus();
 			update();
 		}
 		
 		public void stop()
 		{
 			timer.stop();
 		}
 		public void start()
 		{
 			timer.start();
 		}
 		public void update()
 		{
 			direction = (direction+Math.PI/90)%(Math.PI*2);
 			repaint();
 		}
 	}
 }
