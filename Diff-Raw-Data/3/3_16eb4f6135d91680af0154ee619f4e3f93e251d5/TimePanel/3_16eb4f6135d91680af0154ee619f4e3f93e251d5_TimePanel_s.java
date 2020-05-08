 import javax.swing.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 
 
 public class TimePanel extends JPanel implements TimeLinePanel, MouseListener, MouseMotionListener
 {
 
 	private static final long serialVersionUID = 4466302497626327762L;
 	private JPopupMenu popupMenu1;
 	public String title;
 	protected GUI gui;
     private JMenuItem editAct;
     private TimePanel arg;
     private boolean drawn = false;
     private Graphics g;
     private boolean removed = false;
     private int stage;
     private ArrayList<Integer> acts = new ArrayList<Integer>();
     private Interface iface;
 	protected ArrayList<Shape> shapes = new ArrayList<Shape>();
 	
 	private int dragObject = -1;
 	private Point lastMousePosition = null;
     
 	public TimePanel(GUI gui)
 	{
 		this(gui, 800, 100, "null", 0, null);
 	}
 	public TimePanel(GUI gui, int width, int height, String title, int stage, Interface iface)
 	{
 		System.out.println(stage);
 		this.iface = iface;
 		this.stage = stage;
 		this.arg = this;
 		this.gui = gui;
 		this.title = title;
 		this.addMouseListener(new MouseListener(){
 			public void mouseReleased(MouseEvent e)
 			{
 				if(e.getButton() == MouseEvent.BUTTON3)
 				{
 					if(e.getButton() == MouseEvent.BUTTON3)
 						popupMenu1.show(e.getComponent(), e.getX(),e.getY());
 				}
 			}
 
 			public void mouseClicked(MouseEvent arg0) {}
 			public void mouseEntered(MouseEvent arg0) {}
 			public void mouseExited(MouseEvent arg0) {}
 			public void mousePressed(MouseEvent arg0) {}
 		});
 		
 		this.setPreferredSize(new Dimension(width, height));
 		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		
 		popupMenu1();
 		
 	}
 	
 	public void update()
 	{
 		acts = iface.getAllActs(stage);
 		repaint();
 	}
 	
 	public boolean drawn()
 	{
 		return drawn;
 	}
 	public boolean removed()
 	{
 		return removed;
 	}
 	public void drawn (boolean drawn)
 	{
 		this.drawn = drawn;
 	}
 	public void removed(boolean removed)
 	{
 		this.removed = removed;
 	}
 	
 	public JPopupMenu popupMenu1(){
 	    popupMenu1 = new JPopupMenu();
 	    JMenuItem addArtist = new JMenuItem("Add Artist");
 		addArtist.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				gui.addArtist();
 			}
 		});
 		popupMenu1.add(addArtist);
 		JMenuItem removeArtist = new JMenuItem("Remove Artist");
 		removeArtist.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				gui.removeArtist();
 			}
 		});
 		popupMenu1.add(removeArtist);
 		JMenuItem editArtist = new JMenuItem("Edit Artist");
 		editArtist.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				gui.editArtist();
 			}
 		});
 		popupMenu1.add(editArtist);
 	    JMenuItem addAct = new JMenuItem("Add Act");
 	    addAct.addActionListener(new ActionListener()
 	    {
 	    	public void actionPerformed(ActionEvent e)
 	    	{
 	    		gui.addAct(arg);
 	    	}
 	    });
 	    popupMenu1.add(addAct);
 	    JMenuItem removeAct = new JMenuItem("Remove Act");
 	    removeAct.addActionListener(new ActionListener()
 	    {
 	    	public void actionPerformed(ActionEvent e)
 	    	{
 	    		gui.removeAct();
 	    	}
 	    });
 	    
 	    removeAct.setEnabled(false);
 	    popupMenu1.add(removeAct);
 	    editAct = new JMenuItem("Edit Act");
 	    editAct.addActionListener(new ActionListener()
 	    {
 	    	public void actionPerformed(ActionEvent e)
 	    	{
 	    		gui.editAct();
 	    	}
 	    });
 	    editAct.setEnabled(false);
 	    popupMenu1.add(editAct);
 	    
 	    
 	    popupMenu1.addSeparator();
 	  JMenuItem addStage = new JMenuItem("Add Stage");
 	  addStage.addActionListener(new ActionListener()
 	  {
 	  	public void actionPerformed(ActionEvent e)
 	  	{
 	  		gui.addStage();
 	  	}
 	  });
 	  popupMenu1.add(addStage);
 	  JMenuItem removeStage = new JMenuItem("Remove Stage");
 	  removeStage.addActionListener(new ActionListener()
 	  {
 	  	public void actionPerformed(ActionEvent e)
 	  	{
 		  		gui.removeStage(arg);
 	  	}
 	  });
 	  popupMenu1.add(removeStage);
 	  return popupMenu1;
 	}
 	
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	public int getID()
 	{
 		return stage;
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		this.g = g;
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g;
 		g.setColor(Color.red);
 		g2.drawLine(0, this.getHeight()/2, this.getWidth(), this.getHeight()/2);
		g2.translate(0, height()/2);
 		for(int i = -this.getWidth();i<24;i++)
 		{
 			g2.drawLine(i*(this.getWidth()/24), this.getHeight()/2-this.getHeight()/8, i*(this.getWidth()/24), (this.getHeight()/2)+(this.getHeight()/8));
 		}
 		g2.drawString(title, 4, 3*(this.getHeight()/8));
 
 		g2.translate(0, height()/2);
 		for(int act : acts)
 		{
 			ActPaint a = new ActPaint(act, iface, this);
 			a.paintComponent(g2);
 		}
		g2.translate(0,-height()/2);
 		
 		// TODO add act drawing code.
 	}
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if(dragObject != -1)
 		{
 			
 			System.out.println("hij vind hem wel met draggen");
 			Shape s = shapes.get(dragObject);
 			AffineTransform tr = new AffineTransform();
 			if(((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0))
 			{
 				tr.translate(e.getPoint().x - lastMousePosition.x, e.getPoint().y - lastMousePosition.y);
 			}
 			else
 			{
 				tr.translate(s.getBounds().getCenterX(), s.getBounds().getCenterY());
 				tr.rotate(e.getPoint().x - lastMousePosition.x/100.0);
 				tr.translate(-s.getBounds().getCenterX(), -s.getBounds().getCenterX());
 			}
 			s = tr.createTransformedShape(s);
 			shapes.set(dragObject, s);
 			lastMousePosition = e.getPoint();
 		}
 		repaint();
 		updateUI();
 		
 	}
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mouseClicked(MouseEvent e) {
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
 	@Override
 	public void mousePressed(MouseEvent e) {
 		for(int i = 0; i < shapes.size(); i++)
 		{
 			if(shapes.get(i).contains(e.getPoint()))
 			{
 				System.out.println("pressed");
 				dragObject = i;
 				lastMousePosition = e.getPoint();
 				break;
 			}
 		}
 		
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 	public double height()
 	{
 		return (double)this.getHeight();
 	}
 	public double width()
 	{
 		return (double)this.getWidth();
 	}
 }
