 package hci.viewer;
 
 import hci.polygon.*;
 import hci.tutorial.*;
 import hci.util.Point;
 import hci.menu.*;
 import hci.menu.filedialog.DialogHandler;
 
 import javax.swing.*;
 import java.io.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.util.*;
 
 public class Viewer extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
 
 	private static final String TITLE = "HCI VIEWER";
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private boolean labelVisible = false;
 	
 	private TaggedImage image;
 
 	private JFrame container;
 
 	private PolygonManager polman;
 	
 	private RadialMenu contextMenu;
 	private MainMenu mainMenu;
 	
 	private Queue<Painter> painterQueue = new LinkedList<Painter>();
 
 	private FeedbackHandler feedback;
 	private LabelBox labelbox;
 	
 	private int width, height;
 	
 	private DialogHandler dialogHandler;
 	
 	private Tutorial tutorial;
 
 	public Viewer(int w, int h, String file) throws IOException, FileNotFoundException
 	{
 
 		container = new JFrame(TITLE);
 
 		container.getContentPane().add(this);
 
 		container.setSize(w + container.getInsets().left + container.getInsets().right, h + container.getInsets().top + container.getInsets().bottom + 25);
 
 		polman = new PolygonManager();
 
 		image = new TaggedImage(w, h, file, polman);
 
 		contextMenu = new RadialMenu(w,h);
 		
 		dialogHandler = new DialogHandler(this);
 		mainMenu = new MainMenu(this, dialogHandler);
 
 		container.setResizable(false);
 		
 		this.width = w;
 		this.height = h;
 		
 		tutorial = new Tutorial(this, contextMenu);
 
 		labelbox = new LabelBox(this);
 		this.add(labelbox);
 
 		labelbox.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 				
 			}
 
 			@Override
 			public void keyReleased(KeyEvent Ke) {
 				tutorial.keyPressed();
 				
 				if (KeyEvent.VK_ENTER == Ke.getKeyCode() && labelbox.isVisible()) {
 					
 					if (tutorial.isEnabled())
 						tutorial.stop();
 					
 					try{
 
 						polman.currentlyEditedPolygon().setNameOfPolygon(labelbox.editLabelBox()); // Null pointer because on initial polygon creation it is not being "edited"
 						labelbox.setVisible(false);
 						labelVisible = false;
 						polman.finishPolygon();
 						repaint();
 						
 						
 						
 					}
 					catch(NullPointerException e){}
 					try{
 
 						polman.getNewPolygon().setNameOfPolygon(labelbox.editLabelBox());
 						labelbox.setVisible(false);
 						labelVisible = false;
 						polman.finishPolygon();
 						repaint();
 					}
 					catch(NullPointerException n){}
 				}
 				
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				
 			}
 		});
 
 		container.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent event) {
 
 				// save polygon data
 				quit();
 
 			}
 		});
 
 		labelbox.setVisible(false);
 		labelVisible = false;
 
 		feedback = new FeedbackHandler(this);
 
 		addMouseListener(this);
 		addMouseMotionListener(this);
 
 		addMouseListener(mainMenu);
 		addMouseMotionListener(mainMenu);
 		
 		container.setVisible(true);
 
 	}
 	
 	public boolean labelboxVisible()
 	{
 		return labelVisible;
 	}
 	
 	public void quit()
 	{
 		
 		if (polman.changed())
 		{
 			
 			if (dialogHandler.showSaveDialog())
 				save(dialogHandler.getFilename());
 			
 		}
 		
 		System.exit(0);
 	}
 	
 	public void startTutorial()
 	{
 		tutorial.reset();
 	}
 	
 	public void save(String filename)
 	{
 		
 		if (filename == null) 
 			return;
 		
 		try
 		{
 			
 			image.saveImage(filename);
 			
 			String newFilename = filename.replaceFirst("\\.[^\\.]*$", ".dat");
 			polman.savePolygons(newFilename);
 			
 		}
 		catch (IOException e)
 		{
 			
 			
 			// TODO: handle exception proeprly
 			
 			
 		}
 		
 	}
 	
 	public void load(String filename)
 	{
 		
 		// TODO: handle exceptions properly
 		// TODO: load polygons
 		
 		//polman.loadPolygons(filename);
 		try {
 			
 			image = new TaggedImage(width,height,filename,polman);
 			
 			String newFilename = filename.replaceFirst("\\.[^\\.]*$", ".dat");
 			//"^.*\.[^\.]*$"
 			
 			if (new File(newFilename).exists())
 			{
 				polman.loadPolygons(newFilename);
 			}
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		repaint();
 		
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 
 		// draw image and polygons
 		image.draw(g);
 
 		// handle painter queue
 		Painter p;
 		Queue<Painter> requeue = new LinkedList<Painter>();
 		while ((p = painterQueue.poll()) != null)
 		{
 			p.draw(g);
 			if (p.requeue())
 				requeue.add(p);
 		}
 		for (Painter repaint : requeue)
 		{
 			painterQueue.add(repaint);
 		}
 
 		// draw menu
 		if (mainMenu.showing())
 		{
 			mainMenu.draw(g, getWidth(), getHeight());
 		}
 		else
 		{
 			contextMenu.draw(g);
 		}
 		
 	}
 	
 	public void hideMainMenu()
 	{
 		if (image.isImageLoaded())
 		{
 			mainMenu.hide();
 			repaint();
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent me) {
 		
 	}
 
 	public void repaint(Painter p)
 	{
 		if (!painterQueue.contains(p))
 			painterQueue.add(p);
 		repaint();
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) {
 		
 		if (mainMenu.showing())
 		{
 			return;
 		}
 		
 		switch (me.getButton())
 		{
 		case MouseEvent.BUTTON1:
 
 			if (polman.isEditing())
 			{
 
 				polman.highlightPoint(new Point(me.getX(),me.getY()));
 				repaint();
 
 			}
 			else
 			{
 				if (!contextMenu.showing())
 				{
 					polman.addNewPoint(new Point(me.getX(), me.getY()));
 					repaint();
 				}
 
 				// unhighlight all polygons
 				if (polman.openPolygon())
 				{
 
 					if (polman.removeHighlights())
 					{
 						repaint();
 					}
 
 				}
 			}
 
 			break;
 		case MouseEvent.BUTTON3:
 			contextMenu.show(new Point(me.getX(), me.getY()));
 			setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
 					new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new java.awt.Point(0, 0),
 					"null"));
 			repaint();
 			break;
 		}
 
 	}
 	
 	public Point getDimensions()
 	{
 		
 		return new Point(width,height);
 		
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent me) {
 		
 		if (mainMenu.showing())
 		{
 			return;
 		}
 
 		switch (me.getButton())
 		{
 		case MouseEvent.BUTTON1:
 			polman.resetPointHighlight();
 			break;
 		case MouseEvent.BUTTON3:
 
 			boolean showFeedback = false;
 
 			
 			if (contextMenu.showing())
 			{
 				
 				int state = contextMenu.close();
					

 				switch (state)
 				{
 				case -1:
 					showFeedback = true;
 					break;
 				case 0:
 					break;
 				case 1:
 
 					System.out.println("Entered edit mode");
 
 					if(polman.getHighlighted() != null){
 						labelbox.setVisible(true);
 						labelVisible = true;
 						labelbox.requestFocus();
 						labelbox.setText(polman.getHighlighted().getNameOfPolygon());
 
 						Point point = contextMenu.getPosition();
 						Point myPoint = new Point(point.getX(), point.getY());
 
 						labelbox.setPosition(myPoint);
 						
 					}
 
 					repaint();
 
 					if (polman.isEditing() || !polman.editHighlighted())
 					{
 						showFeedback = true;
 						labelbox.setVisible(false);
 						labelVisible = false;
 					}
 					break;
 				case 3:
 					
 					if (labelbox.isVisible()){
 						
 						try{
							polman.currentlyEditedPolygon().setNameOfPolygon(labelbox.editLabelBox());
 							polman.finishPolygon();
 						}
 						catch(NullPointerException e){}
 
						labelbox.setVisible(false);
						labelVisible = false;
						labelbox.setText("");
 						if (tutorial.isEnabled())
 							tutorial.stop();
 
 					}
 					else if (polman.openPolygon())
 					{
 
 						Point point = contextMenu.getPosition();
 
 						Point myPoint = new Point(point.getX(), point.getY());
 						labelbox.setPosition(myPoint);
 						labelbox.requestFocus();
 
 						if (!polman.finishPolygon())
 						{
 							showFeedback = true;
 						}
 						else
 						{
 							labelbox.setVisible(true);
 							labelVisible = true;
 							labelbox.setText("");
 							labelbox.requestFocus();
 						}
 						
 					}
 					else
 					{
 						showFeedback = true;
 					}
 					break;
 				case 5:
 					mainMenu.show();
 					repaint();
 					break;
 				case 7:
 
 					if (!polman.isEditing())
 					{
 						if (polman.openPolygon())
 						{
 							if (polman.removeLastPoint())
 							{
 								repaint();
 							}
 							else
 							{
 								showFeedback = true;
 							}
 						}
 						else
 						{
 							if (polman.removeHighlighted())
 							{
 								repaint();
 								labelbox.setVisible(false);
 								labelVisible = false;
 							}
 							else
 							{
 								showFeedback = true;
 							}
 						}
 					}
 					else
 					{
 						showFeedback = true;
 					}
 
 					break;
 				}
 
 				// attempt to move the mouse back to where it was 
 				// this should probably be adapted to put the mouse to a more meaningful position
 				try
 				{
 
 					Robot r = new Robot();
 					r.mouseMove((int)(contextMenu.getPosition().getX() + this.getLocationOnScreen().getX()), (int)(contextMenu.getPosition().getY() + this.getLocationOnScreen().getY()));
 					
 
 				}
 				catch (Exception e)
 				{
 
 				}
 
 				if (showFeedback)
 				{
 					feedback.reset(contextMenu.getPosition());
 				}
 
 				repaint();
 			}
 
 			setCursor(Cursor.getDefaultCursor());
 		}
 
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent me) {
 
 		
 		if (mainMenu.showing())
 		{
 			return;
 		}
 		
 		if (contextMenu.showing())
 		{
 			if (contextMenu.updateMousePosition(new Point(me.getX(), me.getY())))
 			{
 				repaint();
 			}
 		}
 		else
 		{
 
 			if (polman.updatePoint(new Point(me.getX(), me.getY())))
 			{
 				repaint();
 			}
 
 		}
 
 	}
 	
 	public boolean isImageLoaded()
 	{
 		return image.isImageLoaded();
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent me) {
 
 		if (mainMenu.showing())
 		{
 			return;
 		}
 		
 		if (!polman.openPolygon())
 		{
 
 			if (polman.updateHighlights(new Point(me.getX(), me.getY())))
 			{
 				repaint();
 			}
 
 		}
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		
 	}
 }
