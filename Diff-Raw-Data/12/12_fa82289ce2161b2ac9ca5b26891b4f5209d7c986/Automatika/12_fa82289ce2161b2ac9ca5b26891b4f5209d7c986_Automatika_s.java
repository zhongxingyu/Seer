 import java.util.LinkedList;
 import java.util.Iterator;
 import java.util.HashSet;
 import java.util.Set;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JCheckBox; 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import java.awt.BorderLayout; 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 public class Automatika extends JFrame implements MouseListener, KeyListener
 {
     int indice = 0;
     JPanel draw_surface = new JPanel();
     boolean edit = false;
     boolean suppr = false;
     Node trait_origin = null;
     LinkedList<Trace> trace = new LinkedList<Trace>();
     LinkedList<Node> coord = new LinkedList<Node>();
     LinkedList<Action> actions = new LinkedList<Action>();
 
     Automatika(int mode)
     {
 	if(mode == 1) // graphe non oriente
 	    System.out.println("g");
 	else if(mode == 2) // graphe oriente
 	    System.out.println("o");
 	else if(mode == 3)// automaton
 	    System.out.println("a");
 	setSize(789, 456);
 	this.setContentPane(draw_surface);
 	this.addMouseListener(this);
 	this.addKeyListener(this);
 	setDefaultCloseOperation(EXIT_ON_CLOSE);
 	setVisible(true);
     }
 
     private class Action
     {
 	/*
 	 * 1: create node
 	 * 2: suppr node
 	 * 3: create path
 	 * 4: suppr path
 	 * 5: edit node
 	 * 6: edit path
 	 */
 	int num_action = 0;
 	Node n = null;
 	Trace t = null;
 	boolean start = false;
 	boolean end = false;
 	String value = null;
 	//1, 2
 	Action(int num, Node n)
 	{
 	    num_action = num;
 	    this.n = n;
 	}
 	//3, 4
 	Action(int num, Trace t)
 	{			
 	    num_action = num;
 	    this.t = t;
 	}
 	//5
 	Action(int num, boolean start, boolean end, String name)
 	{			
 	    num_action = num;
 	    this.start = start;
 	    this.end = end;
 	    value = name;
 	}
 	//6
 	Action(int num, String value)
 	{
 	    num_action = num;
 	    this.value = value;
 	}
 		
 	public int getNum()
 	{
 	    return num_action;
 	}
     }
     
     public class Edit extends JFrame implements ActionListener
     {
 	JTextField txt = new JTextField(10);
 	JCheckBox start = new JCheckBox("Start");
 	JCheckBox end = new JCheckBox("End");
 	JCheckBox inversion = new JCheckBox("Inversion");
 	JButton ok = new JButton("Ok");
 	JButton cancel = new JButton("Cancel");
 	int num;
 	boolean is_node;
 	Edit(boolean b, int n)// true => node; false => line
 	{
 	    this.is_node = b;
 	    this.num = n;
 	    setSize(300, 150);
 	    if(b)
 		node();
 	    else
 		line();
 	    setDefaultCloseOperation(EXIT_ON_CLOSE);
 	    setVisible(true);
 	}
 
 	public void node()
 	{
 	    JPanel pan = new JPanel(new BorderLayout());
 	    JPanel pan_valeur = new JPanel();
 	    JPanel pan_node = new JPanel();
 	    JPanel pan_btn = new JPanel();
 	    pan_valeur.add(new JLabel("Valeur"));
 	    pan_valeur.add("jtxtfld", txt);
 	    pan_node.add(start);   
 	    pan_node.add(end); 
 	    pan_btn.add(cancel);
 	    pan_btn.add(ok);
 	    cancel.addActionListener(this);
 	    ok.addActionListener(this);
 	    pan.add("North", pan_valeur);
 	    pan.add("Center", pan_node);
 	    pan.add("South", pan_btn);
 	    add(pan);	    
 	}
 	
 	public void line()
 	{
 	    JPanel pan = new JPanel(new BorderLayout());
 	    JPanel pan_valeur = new JPanel();
 	    JPanel pan_line = new JPanel();
 	    JPanel pan_btn = new JPanel();
 	    pan_valeur.add(new JLabel("Valeur"));
 	    pan_valeur.add("jtxtfld", txt);
 	    pan_line.add(inversion); 
 	    pan_btn.add(cancel);
 	    pan_btn.add(ok);
 	    cancel.addActionListener(this);
 	    ok.addActionListener(this);
 	    pan.add("North", pan_valeur);
 	    pan.add("Center", pan_line);
 	    pan.add("South", pan_btn);
 	    add(pan);	    
 	}
 	
 	public void edit_node()
 	{
 	    coord.get(num).setStart(start.isSelected());
 	    coord.get(num).setEnd(end.isSelected());	    
 	    coord.get(num).setName(txt.getText());
 	}
 
 	public void edit_line()
 	{
 	    trace.get(num).setName(txt.getText());
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{  
 	    if(e.getSource() == ok)
 		{
 		    //System.out.println("btn -> ok, " + start.isSelected() + " " + end.isSelected());
 		    if(is_node)
 			edit_node();
 		    else
 			edit_line();
 		    setVisible(false);
 		    repaint();
 		}
 	    else if(e.getSource() == cancel){
 		System.out.println("btn -> cancel");
 		setVisible(false);
 	    }
 	}
     }
     
     public void repaint()
     {
 	//Iterator<Trace> it_trace = trace.iterator();
 	//Iterator<Node> it_node = coord.iterator();		
 	for(int i = 0; i < trace.size(); i++)
 	    {
 		int x1 = trace.get(i).getX1(),
 		    y1 = trace.get(i).getY1(),
 		    x2 = trace.get(i).getX2(),
 		    y2 = trace.get(i).getY2();
 		draw_surface.getGraphics().drawLine(x1, y1, x2, y2);
 		//System.out.println(trace.get(i));
 	    }
 	Graphics g = draw_surface.getGraphics();
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		g.setColor(draw_surface.getBackground());
 		g.fillOval(coord.get(i).x-26, coord.get(i).y-26, 50, 50);
 		g.setColor(Color.black);
 		g.drawOval(coord.get(i).x-25, coord.get(i).y-25, 50, 50);
		if(coord.get(i).isStart())
 		    {
 			g.drawOval(coord.get(i).x-20, coord.get(i).y-20, 40, 40);			
 		    }
 		g.drawString(coord.get(i).name, coord.get(i).x-5, coord.get(i).y+5);			
 	    }
 	System.out.println("repaint");		
     }
     
  public void delete(Node n)
     {
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		if(coord.get(i).equals(n))
 		    {
 			System.out.println("suppression");				
 			Graphics g = draw_surface.getGraphics();
 			g.setColor(draw_surface.getBackground());
 			g.fillOval(coord.get(i).x-26, coord.get(i).y-26, 52, 52);
 			Iterator<Node> it_node = n.transitions.iterator();
 			Node s = null;
 			while(it_node.hasNext())
 			    {
 				s = it_node.next();
 				g.drawLine(coord.get(i).x, coord.get(i).y, s.x, s.y);
 				for(int j = 0; j < trace.size(); j++)
 				    {
 					int x1 = trace.get(j).getX1(),
 					    x2 = trace.get(j).getX2(),
 					    y1 = trace.get(j).getY1(),
 					    y2 = trace.get(j).getY2();
 					if((x1 == coord.get(i).x && y1 == coord.get(i).y && x2 == s.x && y2 == s.y) || (x1 == s.x && y1 == s.y && x2 == coord.get(i).x && y2 == coord.get(i).y))
 					    {
 						System.out.println("DEL");
 						trace.remove(j);
 					    }
 				    }
 			    }
 			coord.remove(i);
 			return;
 		    }
 	    }
     }
 	
     public void newNode(int x, int y, String name)
     {
 	boolean go = true;
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		if( y >= coord.get(i).y - 25 &&
 		    y <= coord.get(i).y + 25 &&
 		    x >= coord.get(i).x - 25 &&
 		    x <= coord.get(i).x + 25)
 		    {
 			go = false;
 		    }
 	    }
 	if(go)
 	    {
 		draw_surface.getGraphics().drawString(name, x-5, y+5);
 		draw_surface.getGraphics().drawOval(x-25, y-25, 50, 50);
 		//System.out.println("q"+ coord.size());
 		//System.out.println("coord.size:" + coord.size());
 	    }
     }
 
     // TODO: split
     public void newTrait(int x, int y)
     {
 	int x_1 = trait_origin.x, y_1 = trait_origin.y, x_2 = 0, y_2 = 0;
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		if( y >= coord.get(i).y - 25 &&
 		    y <= coord.get(i).y + 25 &&
 		    x >= coord.get(i).x - 25 &&
 		    x <= coord.get(i).x + 25)
 		    {
 			x_2 = coord.get(i).x;
 			y_2 = coord.get(i).y;
 			draw_surface.getGraphics().setColor(Color.red);
 			//System.out.println("new trait: ok");
 			draw_surface.getGraphics().drawLine(trait_origin.x,trait_origin.y,coord.get(i).x, coord.get(i).y);
 			Graphics g = draw_surface.getGraphics();
 			g.setColor(draw_surface.getBackground());
 			g.fillOval(trait_origin.x-25, trait_origin.y-25, 50, 50);
 			g.fillOval(coord.get(i).x-25, coord.get(i).y-25, 50, 50);
 			g.setColor(Color.black);
 			g.drawOval(trait_origin.x-25, trait_origin.y-25, 50, 50);
 			g.drawOval(coord.get(i).x-25, coord.get(i).y-25, 50, 50);
 			draw_surface.getGraphics().drawString(coord.get(i).name, coord.get(i).x-5, coord.get(i).y+5);
 			draw_surface.getGraphics().drawString(trait_origin.name, trait_origin.x-5, trait_origin.y+5);
 			trait_origin.add_transition(coord.get(i));
 			coord.get(i).add_transition(trait_origin);
 			repaint();
 			int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
 			if(trace.size() == 0)
 			    {
 				trace.add(new Trace(x_1, y_1, x_2, y_2));
 			    }
 			else
 			    {
 				boolean exist = false;
 				for(int k = 0; k < trace.size(); k++)
 				    {
 					x1 = trace.get(k).getX1();
 					x2 = trace.get(k).getX2();
 					y1 = trace.get(k).getY1();
 					y2 = trace.get(k).getY2();
 					//System.out.println("[" + x1 + " " + y1 + ", " + x2 + " " + y2 + "]\t[" + x_1 + " " + y_1 + " ," + x_2 + " " + y_2 +"]");
 					if((x1 == x_1 && x2 == x_2 && y1 == y_1 && y2 == y_2) || (x1 == x_2 && x2 == x_1 && y1 == y_2 && y2 == y_1))
 					    {
 						exist = true;
 						System.out.println("exist");
 						break;
 					    }
 				    }
 				if(!exist)
 				    {
 					//System.out.println("add new path => (" + x_1 + ", " + y_1 + "); (" + x_2 + ", " + y_2 + ")");
 					trace.add(new Trace(x_1, y_1, x_2, y_2));
 					return;
 				    }
 			    }
 			//System.out.println(coord.get(i).print_transitions());
 			//System.out.println(trait_origin.print_transitions());
 			return;
 		    }
 		else
 		    {
 			//System.out.println("new trait : fail");
 		    }
 	    }
     }
 	
     public Node getNode(int x, int y)
     {
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		if( y >= coord.get(i).y - 25 &&
 		    y <= coord.get(i).y + 25 &&
 		    x >= coord.get(i).x - 25 &&
 		    x <= coord.get(i).x + 25)
 		    {
 			return coord.get(i);
 		    }
 	    }
 	return null;
     }
 
     public int getNumNode(int x, int y)
     {
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		if( y >= coord.get(i).y - 25 &&
 		    y <= coord.get(i).y + 25 &&
 		    x >= coord.get(i).x - 25 &&
 		    x <= coord.get(i).x + 25)
 		    {
 			return i;
 		    }
 	    }
 	return -1;
     }
 
     // a < b < c
     public boolean isBetween(int a, int b, int c){ return (a <= b && b <= c) || (a >= b && b >= c);}
     
     public Trace getTrace(int click_x, int click_y)
     {
 	int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
 	for(int i = 0; i < trace.size(); i++)
 	    {
 		x1 = trace.get(i).getX2() - trace.get(i).getX1();
 		y1 = trace.get(i).getY2() - trace.get(i).getY1();
 		x2 = click_x - trace.get(i).getX1();
 		y2 = click_y - trace.get(i).getY1();
 		if(isBetween(-2000,x1*y2 - y1*x2,2000) && 
 		   isBetween(trace.get(i).getX1(), click_x,trace.get(i).getX2()) && 
 		   isBetween(trace.get(i).getY1(), click_y,trace.get(i).getY2())
 		   )
 		    return trace.get(i);
 	    }
 	return null;
     }
     
     public int getNumTrace(int click_x, int click_y)
     {
 	int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
 	for(int i = 0; i < trace.size(); i++)
 	    {
 		x1 = trace.get(i).getX2() - trace.get(i).getX1();
 		y1 = trace.get(i).getY2() - trace.get(i).getY1();
 		x2 = click_x - trace.get(i).getX1();
 		y2 = click_y - trace.get(i).getY1();
 		if(isBetween(-2000,x1*y2 - y1*x2,2000) && 
 		   isBetween(trace.get(i).getX1(), click_x,trace.get(i).getX2()) && 
 		   isBetween(trace.get(i).getY1(), click_y,trace.get(i).getY2()))
 		    return i;
 	    }
 	return -1;
     }
 
     public void Move(int x, int y)
     {
 	Iterator<Node> it = trait_origin.transitions.iterator();
 	Graphics g = draw_surface.getGraphics();
 	Node n = null;
 	int old_x = trait_origin.x;
 	int old_y = trait_origin.y;
 	newNode(x, y, trait_origin.name);	
 	g.setColor(draw_surface.getBackground());
 	trait_origin.x = x;
 	trait_origin.y = y;
 	g.drawOval(old_x-25, old_y-25, 50, 50);
 	g.fillOval(old_x-25, old_y-25, 50, 50);
 	while(it.hasNext())
 	    {
 		g.setColor(draw_surface.getBackground());
 		n = it.next();
 		//deleting phase
 		for(int i = 0; i < trace.size(); i++)
 		    {
 			int x1 = trace.get(i).getX1(), 
 			    x2 = trace.get(i).getX2(), 
 			    y1 = trace.get(i).getY1(), 
 			    y2 = trace.get(i).getY2();
 			Node t = getNode(n.x, n.y);
 			if(t != null && ((x1 == old_x && x2 == t.x && y1 == old_y && y2 == t.y) || (x1 == t.x && x2 == old_x && y1 == t.y && y2 == old_y)))
 			    {
 				trace.set(i, new Trace(trait_origin.x,trait_origin.y, n.x, n.y));
 				break;
 			    }
 		    }
 		g.drawLine(old_x,old_y, n.x, n.y);
 		g.drawOval(n.x-25, n.y-25, 50, 50);
 		//add phase
 		//System.out.println(trait_origin.name);
 		g.setColor(Color.BLACK);	
 		newTrait(n.x, n.y);
 		//System.out.println(n);
 	    }
     }
 
     public void Back()
     {
 		
     }
 
     //If point is out of the screen
     public boolean isOut(int click_x, int click_y)
     {
 	if(click_x < 0 || click_y < 0 || click_x >= 789 || click_y >= 456)
 	    {
 		System.out.println("OUT OF THE SCREEN");
 		return true;
 	    }
 	return false;
     }
 	
     public void keyTyped(KeyEvent e){/*System.out.println("keytyped");*/}
     public void keyReleased(KeyEvent e){
 	if(e.getKeyCode() == KeyEvent.VK_CONTROL && edit) edit = false;
 	if(e.getKeyCode() == KeyEvent.VK_DELETE && suppr) suppr = false;
     }
     public void keyPressed(KeyEvent e){
 	if(e.isControlDown()) edit = true;
 	if(e.getKeyCode() == KeyEvent.VK_Z && edit){Back();}
 	if(e.getKeyCode() == KeyEvent.VK_Y && edit){System.out.println("Move");}
 	if(e.getKeyCode() == KeyEvent.VK_O && edit){System.out.println("Open");}
 	if(e.getKeyCode() == KeyEvent.VK_S && edit){System.out.println("Save");}
 	if(e.getKeyCode() == KeyEvent.VK_DELETE)   { suppr = true;}
     }
 
     public void mouseClicked(MouseEvent e)
     {
 	if(e.getClickCount() == 2 && edit == true)
 	    {
 		int num_trace = getNumTrace(e.getX(), e.getY() - 25);
 	        int num_node = getNumNode(e.getX(), e.getY() - 25);
 		if(num_trace != -1)
 		    if(num_node != -1)
 			new Edit(true, num_node);
 		    else
 			new Edit(false, num_trace);
 		else
 		    if(num_node != -1)
 			new Edit(true, num_node);
 	    }
     }
     
     public void mouseReleased(MouseEvent e)
     {
 	if(isOut(e.getX(), e.getY() - 25))
 	    {
 		return;
 	    }
 	
 	
 	if(suppr == true && trait_origin != null)
 	    {
 		delete(getNode(e.getX(), e.getY() - 25));
 		repaint();
 	    }
 	else if(edit == true && trait_origin != null)
 	    {
 		Move(e.getX(), e.getY() - 25);
 		repaint();
 	    }	
 	else if(trait_origin != null && edit == false)
 	    {
 		newTrait(e.getX(), e.getY() - 25);
 		trait_origin = null;
 	    }
 	else if(edit == false)
 	    {
 		String name = "q" + indice++;
 		int x = e.getX(), y = e.getY() - 25;
 		newNode(x, y, name);
 		coord.add(new Node(x, y, name));
 	    }
     }
 
     public void mousePressed(MouseEvent e){
 	trait_origin = getNode(e.getX(), e.getY() - 25);
     }
     public void mouseExited(MouseEvent e){}
     public void mouseEntered(MouseEvent e){}
 }
