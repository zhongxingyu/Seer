 package linewars.display.panels;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.DefaultButtonModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JViewport;
 
 import linewars.display.IconConfiguration;
 import linewars.display.IconConfiguration.IconType;
 import linewars.display.ImageDrawer;
 import linewars.gamestate.Position;
 import linewars.gamestate.tech.CycleException;
 import linewars.gamestate.tech.TechConfiguration;
 import linewars.gamestate.tech.TechGraph;
 import linewars.gamestate.tech.TechGraph.TechNode;
 import configuration.Configuration;
 import editor.GenericSelector;
 import editor.URISelector;
 
 @SuppressWarnings("serial")
 public class TechDisplay extends JViewport
 {
 	private final static int TECH_BUTTON_SIZE = 50;
 	private final static int BUTTON_BORDER_SIZE = 5;
 	
 	private int pID;
 	private TechGraph techGraph;
 	private JPanel treeDisplay;
 	private TechButton[] buttons;
 	
 	private GridBagLayout treeLayout;
 
 	private boolean editorNOTgame;
 	
 	private TechNode activeTech;
 	private GenericSelector<Configuration> techSelector;
 	private URISelector unlockStrategySelector;
 	
 	/**
 	 * Constructs the TechDisplay for the editors, allows all elements to be edited.
 	 * @param techGraph The TechGraph this TechDisplay will show and edit.
 	 */
 	public TechDisplay(TechGraph techGraph)
 	{
 		this.editorNOTgame = true;
 		this.techGraph = techGraph;
 		
 		initializeDisplay();
 		
 		ViewportDragger dragger = new ViewportDragger();
 		addMouseListener(dragger);
 		addMouseMotionListener(dragger);
 	}
 	
 	/**
 	 * Constructs the TechDisplay for use in the game, does not allow editing, displays the state of techs, and researches them.
 	 * @param pID The ID of the player this TechPanel is displayed for.
 	 * @param techGraph The TechGraph this TechDisplay will show.
 	 */
 	public TechDisplay(int pID, TechGraph techGraph)
 	{
 		this.editorNOTgame = false;
 		this.pID = pID;
 		this.techGraph = techGraph;
 		
 		initializeDisplay();
 		
 		ViewportDragger dragger = new ViewportDragger();
 		addMouseListener(dragger);
 		addMouseMotionListener(dragger);
 	}
 
 	private void initializeDisplay()
 	{
 		setOpaque(false);
 		
 		treeLayout = new GridBagLayout();
 		treeDisplay = new JPanel(treeLayout);
 		treeDisplay.setOpaque(false);
 		add(treeDisplay);
 		
 		refreshDisplay(true);
 	}
 	
 	private void refreshDisplay(boolean initialization)
 	{
 		if(!initialization)
 		{
 			treeDisplay.removeAll();
 		}
 		
 		int xSize;
 		int ySize;
 		if(editorNOTgame)
 		{
 			xSize = techGraph.getMaxX() + BUTTON_BORDER_SIZE;
 			ySize = techGraph.getMaxY() + BUTTON_BORDER_SIZE;
 		}
 		else
 		{
 			xSize = techGraph.getMaxX();
 			ySize = techGraph.getMaxY();
 		}
 		
 		treeDisplay.setPreferredSize(new Dimension(xSize * TECH_BUTTON_SIZE, ySize * TECH_BUTTON_SIZE));
 		
 		List<TechNode> orderedTechList = techGraph.getOrderedList();
 		Iterator<TechNode> orderedListIterator = orderedTechList.iterator();
 		
 		if(editorNOTgame)
 			buttons = new TechButton[xSize * ySize];
 		else
 			buttons = new TechButton[orderedTechList.size()];
 		
 		TechNode current = null;
 		if(orderedListIterator.hasNext())
 			current = orderedListIterator.next();
 
 		GridBagConstraints treeConstraints = new GridBagConstraints();
 		treeConstraints.gridwidth = 1;
 		treeConstraints.gridheight = 1;
 		treeConstraints.gridy = 0;
 		int i = 0;
 		for(int r = 0; r < ySize; ++r)
 		{
 			treeConstraints.gridx = 0;
 			for(int c = 0; c < xSize; ++c)
 			{
 				if(current != null && current.getX() == c && current.getY() == r)
 				{
 					buttons[i] = new TechButton(current, r, c);
 					buttons[i].setOpaque(false);
 					
 					if(editorNOTgame)
 					{
 						EditorButtonListener dragger = new EditorButtonListener(buttons[i]);
 						buttons[i].addMouseListener(dragger);
 						buttons[i].addMouseMotionListener(dragger);
 					}
 					else
 						buttons[i].addActionListener(new ResearchTechHandler(i));
 					
 					treeDisplay.add(buttons[i]);
 					treeLayout.addLayoutComponent(buttons[i], treeConstraints);
 					
 					++i;
 					if(orderedListIterator.hasNext())
 						current = orderedListIterator.next();
 					else
 						current = null;
 				}
 				else if(editorNOTgame)
 				{
 					buttons[i] = new TechButton(null, r, c);
 					buttons[i].setOpaque(false);
 					
 					EditorButtonListener dragger = new EditorButtonListener(buttons[i]);
 					buttons[i].addMouseListener(dragger);
 					buttons[i].addMouseMotionListener(dragger);
 					
 					treeDisplay.add(buttons[i]);
 					treeLayout.addLayoutComponent(buttons[i], treeConstraints);
 					
 					++i;
 				}
 				else
 				{
 					Component box = Box.createRigidArea(new Dimension(TECH_BUTTON_SIZE, TECH_BUTTON_SIZE));
 					treeDisplay.add(box);
 					treeLayout.addLayoutComponent(box, treeConstraints);
 				}
 				
 				++treeConstraints.gridx;
 			}
 
 			++treeConstraints.gridy;
 		}
 		
 		validate();
 		repaint();
 	}
 	
 	public TechGraph getTechGraph()
 	{
 		return techGraph;
 	}
 	
 	public TechNode getActiveTech()
 	{
 		return activeTech;
 	}
 	
 	public void setTechSelector(GenericSelector<Configuration> techSelector)
 	{
 		this.techSelector = techSelector;
 	}
 	
 	public void setUnlockStrategySelector(URISelector unlockStrategySelector)
 	{
 		this.unlockStrategySelector = unlockStrategySelector;
 	}
 	
 	@Override
 	public void paint(Graphics g)
 	{
 		super.paint(g);
 		
 		((Graphics2D)g).setStroke(new BasicStroke(5));
 		
 		techGraph.unmarkAll();
 		TechNode root = techGraph.getRoot();
 		while(root != null)
 		{
 			drawDependencyLines(g, root);
 			root = techGraph.getNextRoot();
 		}
 		
 		techGraph.unmarkAll();
 		
 	}
 	
 	private void drawDependencyLines(Graphics g, TechNode node)
 	{
 		if(node.isMarked())
 			return;
 		
 		node.mark();
 		
 		TechNode child = node.getChild();
 		while(child != null)
 		{
 			Point offset = getViewPosition();
 			
 			int startX = node.getX() * TECH_BUTTON_SIZE + TECH_BUTTON_SIZE - offset.x;
 			int startY = node.getY() * TECH_BUTTON_SIZE + TECH_BUTTON_SIZE / 2 - offset.y;
 			int endX = child.getX() * TECH_BUTTON_SIZE - offset.x;
 			int endY = child.getY() * TECH_BUTTON_SIZE + TECH_BUTTON_SIZE / 2 - offset.y;
 			
 			Position vector = new Position(startX - endX, startY - endY);
 			vector = vector.normalize().scale(15);
 			
 			g.drawLine(startX, startY, endX, endY);	
 
 			vector = vector.rotateAboutPosition(new Position(0, 0), Math.PI / 4);
 			g.drawLine(endX, endY, endX + (int)vector.getX(), endY + (int)vector.getY());
 
 			vector = vector.rotateAboutPosition(new Position(0, 0), -Math.PI / 2);
 			g.drawLine(endX, endY, endX + (int)vector.getX(), endY + (int)vector.getY());
 			
 			drawDependencyLines(g, child);
 			child = node.getNextChild();
 		}
 	}
 	
 	private void moveViewport(Point vector)
 	{
 		Point p = getViewPosition();
 		p.translate(-vector.x, -vector.y);
 		
 		if(p.x < 0)
 			p.setLocation(0, p.y);
 		else if(p.x > treeDisplay.getWidth() - getWidth())
 			p.setLocation(treeDisplay.getWidth() - getWidth(), p.y);
 		
 		if(p.y < 0)
 			p.setLocation(p.x, 0);
 		else if(p.y > treeDisplay.getHeight() - getHeight())
 			p.setLocation(p.x, treeDisplay.getHeight() - getHeight());
 		
 		setViewPosition(p);
 	}
 
 	/**
 	 * A button for the command card.
 	 * 
 	 * @author Ryan Tew
 	 * 
 	 */
 	private class TechButton extends JButton
 	{
 		private TechNode tech;
 		private int row;
 		private int col;
 		
 		public TechButton(TechNode tech, int row, int col)
 		{
 			this.row = row;
 			this.col = col;
 			this.tech = tech;
 			
 			Dimension size = new Dimension(TECH_BUTTON_SIZE, TECH_BUTTON_SIZE);
 			
 			setSize(size);
 			setMaximumSize(size);
 			setPreferredSize(size);
 			setMinimumSize(size);
 			
 			if(tech != null)
 				setInfoFromTech(tech.getTechConfig());
 			else
 				setInfoFromTech(null);
 		}
 		
 		public void setTech(TechNode tech)
 		{
 			this.tech = tech;
 			
 			if(tech != null)
 				setInfoFromTech(tech.getTechConfig());
 			else
 				setInfoFromTech(null);
 		}
 		
 		private void setInfoFromTech(TechConfiguration tech)
 		{
 			if(tech != null)
 			{
 				IconConfiguration icons = tech.getIcons();
 				
 				setIcon(new ButtonIcon(this, icons.getIconURI(IconType.regular)));
 				setPressedIcon(new ButtonIcon(this, icons.getIconURI(IconType.pressed)));
 				setRolloverIcon(new ButtonIcon(this, icons.getIconURI(IconType.rollover)));
 				setSelectedIcon(new ButtonIcon(this, icons.getIconURI(IconType.highlighted)));
 				setDisabledIcon(new ButtonIcon(this, icons.getIconURI(IconType.disabled)));
 				
 				setToolTipText(tech.getTooltip());
 			}
 			else
 			{
 				setIcon(new ButtonIcon(this, null));
 				setPressedIcon(new ButtonIcon(this, null));
 				setRolloverIcon(new ButtonIcon(this, null));
 				setSelectedIcon(new ButtonIcon(this, null));
 				setDisabledIcon(new ButtonIcon(this, null));
 				
 				setToolTipText(null);
 			}
 		}
 		
 		public Point buttonToViewPort(Point p)
 		{
 			Point treeDispPos = new Point(col * TECH_BUTTON_SIZE + p.x, row * TECH_BUTTON_SIZE + p.y);
 			Point viewPos = getViewPosition();
 			treeDispPos.translate(-viewPos.x, -viewPos.y);
 			return treeDispPos;
 		}
 		
 		public Point buttonToTreeDisplay(Point p)
 		{
 			Point treeDispPos = new Point(col * TECH_BUTTON_SIZE + p.x, row * TECH_BUTTON_SIZE + p.y);
 			return treeDispPos;
 		}
 
 		@Override
 		public void paint(Graphics g)
 		{
 			if(editorNOTgame)
 			{
 				if(tech == null)
 					g.setColor(Color.red);
 				else if(tech == activeTech)
 					g.setColor(Color.blue);
 				else
 					g.setColor(Color.orange);
 				
 				g.fillRect(0, 0, getWidth(), getHeight());
 				
 				g.setColor(Color.black);
 				g.drawRect(0, 0, getWidth(), getHeight());
 			}
 			
 			DefaultButtonModel model = (DefaultButtonModel)getModel();
 			if(tech != null && !tech.isUnlocked())
				getDisabledIcon().paintIcon(this, g, 0, 0);
 			else if(model.isPressed())
				getPressedIcon().paintIcon(this, g, 0, 0);
 			else if(model.isSelected())
				getSelectedIcon().paintIcon(this, g, 0, 0);
 			else if(model.isRollover())
				getRolloverIcon().paintIcon(this, g, 0, 0);
 			else
				getIcon().paintIcon(this, g, 0, 0);
 		}
 	}
 	
 	/**
 	 * An icon for a Command Button
 	 * 
 	 * @author Ryan Tew
 	 * 
 	 */
 	private class ButtonIcon implements Icon
 	{
 		private JButton button;
 		private String uri;
 
 		/**
 		 * Constructs the icon.
 		 * 
 		 * @param b
 		 *            The button this icon is on.
 		 */
 		public ButtonIcon(JButton b, String uri)
 		{
 			this.button = b;
 			this.uri = uri;
 		}
 
 		@Override
 		public int getIconHeight()
 		{
 			return getHeight();
 		}
 
 		@Override
 		public int getIconWidth()
 		{
 			return getWidth();
 		}
 
 		@Override
 		public void paintIcon(Component c, Graphics g, int x, int y)
 		{
 			ImageDrawer.getInstance().draw(g, uri + button.getWidth() + button.getHeight(), new Position(x, y), 1);
 		}
 	}
 
 	private class EditorButtonListener extends MouseAdapter
 	{
 		private Point lastPoint;
 		private TechButton button;
 		private boolean movingTech;
 		private boolean creatingDependency;
 		
 		public EditorButtonListener(TechButton b)
 		{
 			this.button = b;
 			this.movingTech = false;
 			this.creatingDependency = false;
 		}
 		
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			int mButton = e.getButton();
 			if(mButton == MouseEvent.BUTTON1)
 			{
 				creatingDependency = true;
 				
 				if(button.tech == null)
 				{
 					button.setTech(techGraph.addNode());
 					button.tech.setPosition(button.col, button.row);
 					
 					refreshDisplay(false);
 				}
 				
 				activeTech = button.tech;
 			}
 			else if(mButton == MouseEvent.BUTTON3)
 			{
 				if(button.tech == null)
 				{
 					lastPoint = button.buttonToViewPort(e.getPoint());
 				}
 				else
 				{
 					movingTech = true;
 					
 					activeTech = button.tech;
 				}
 			}
 			
 			if(activeTech != null)
 			{
 				techSelector.setSelectedObject(activeTech.getTechConfig());
 				unlockStrategySelector.setSelectedURI(activeTech.getUnlockStrategy().toString());
 			}
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e)
 		{
 			if(!movingTech && !creatingDependency)
 			{
 				Point vector = button.buttonToViewPort(e.getPoint());
 				vector.translate(-lastPoint.x, -lastPoint.y);
 				lastPoint = button.buttonToViewPort(e.getPoint());
 				moveViewport(vector);
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e)
 		{
 			Component atPoint = treeDisplay.getComponentAt(button.buttonToTreeDisplay(e.getPoint()));
 			if(atPoint == null || !(atPoint instanceof TechButton))
 				return;
 			
 			TechButton releasedOver = (TechButton)atPoint;
 			
 			int mButton = e.getButton();
 			if(mButton == MouseEvent.BUTTON1)
 			{
 				if(releasedOver.tech != null)
 				{
 					try
 					{
 						button.tech.addChild(releasedOver.tech);
 					}
 					catch (CycleException e1)
 					{}
 				}
 				
 				creatingDependency = false;
 			}
 			else if(mButton == MouseEvent.BUTTON3)
 			{
 				if(movingTech && releasedOver.tech == null)
 				{
 					releasedOver.setTech(button.tech);
 					button.setTech(null);
 					
 					releasedOver.tech.setPosition(releasedOver.col, releasedOver.row);
 					
 					refreshDisplay(false);
 				}
 				
 				movingTech = false;
 			}
 			
 			TechDisplay.this.repaint();
 		}
 	}
 		
 	private class ViewportDragger extends MouseAdapter
 	{
 		private Point lastPoint;
 		
 		@Override
 		public void mousePressed(MouseEvent e)
 		{
 			lastPoint = e.getPoint();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e)
 		{
 			Point vector = e.getPoint();
 			vector.translate(-lastPoint.x, -lastPoint.y);
 			moveViewport(vector);
 			lastPoint = e.getPoint();
 		}
 	}
 	
 	private class ResearchTechHandler implements ActionListener
 	{
 		private int index;
 		
 		public ResearchTechHandler(int index)
 		{
 			this.index = index;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent e)
 		{
 			if(!buttons[index].tech.isUnlocked())
 				return;
 			
 			if(buttons[index].tech.isResearched())
 				return;
 			
 			buttons[index].tech.research();
 		}
 	}
 }
