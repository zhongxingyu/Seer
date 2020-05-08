 /**
    TODO:
    --- OUT
    1 : create txt
    --- automaton
    2: double trace
 **/
 import java.util.LinkedList;
 import java.util.Iterator;
 import java.util.HashSet;
 import java.util.Set;
 import java.io.File;
 import java.io.IOException;
 import java.io.FilenameFilter;
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.FileWriter;
 import java.io.FileReader;
 import java.io.RandomAccessFile;
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JCheckBox; 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import java.awt.BorderLayout; 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.FileDialog;
 import java.awt.image.BufferedImage;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 public class Automatika extends JFrame implements MouseListener, KeyListener
 {
     int mode = 0; // 1 = no orient, 2 = orient, 3 = list
     int indice = 0;
     int id_hist = -1;
     int mousePressed_x = 0;
     int mousePressed_y = 0;
     JPanel draw_surface = new JPanel();
     boolean edit = false;
     boolean suppr = false;
     Node trait_origin = null;
     LinkedList<Trace> trace = new LinkedList<Trace>();
     LinkedList<Node> coord = new LinkedList<Node>();
     LinkedList<Action> actions = new LinkedList<Action>();
     int width = 789;
     int height = 456;
     
     /*
       In this constructor deserve to choose wich mod we will use 
       At this moment, we have just the mod no oriented graph
      */
     Automatika(int mode)
     {
 	if(mode == 1) // graphe non oriente
 	    this.mode = 1;
 	else if(mode == 2) // graphe oriente
 	    this.mode = 2;
 	else if(mode == 3)// automaton
 	    this.mode = 3;
 	setSize(width, height);
 	this.setContentPane(draw_surface);
 	this.addMouseListener(this);
 	this.addKeyListener(this);
 	setDefaultCloseOperation(EXIT_ON_CLOSE);
 	setVisible(true);
     }
 
     /***********************
      ***   CLASS  EDIT   ***
      ***********************/
     //this class is use for edit a node or a line in the paint
     public class Edit extends JFrame implements ActionListener
     {
 	JPanel pan = new JPanel(new BorderLayout());
 	JPanel pan_valeur = new JPanel();
 	JPanel pan_line = new JPanel();
 	JPanel pan_btn = new JPanel();
 	JPanel pan_node = new JPanel();
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
 
 	//if object is a node
 	public void node()
 	{
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
 
 	//if object is a line
 	public void line()
 	{
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
 	
 	public void edit_line()//TODO: complete
 	{
 	    trace.get(num).setName(txt.getText());
 	    //System.out.println("edit trace");
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{  
 	    if(e.getSource() == ok)
 		{
 		    //System.out.println("btn -> ok, " + start.isSelected() + " " + end.isSelected());
 		    id_hist++;
 		    if(is_node)
 			{
 			    actions.add(new Action(5, coord.get(num), 
 						   coord.get(num).getStart(), coord.get(num).getEnd(), coord.get(num).getName(),
 						   start.isSelected(), end.isSelected(), txt.getText())
 					);
 			    edit_node();
 			}
 		    else
 			{
 			    actions.add(new Action(6, trace.get(num), trace.get(num).getName(), txt.getText()));
 			    edit_line();
 			}
 		    setVisible(false);
 		    repaint();
 		}
 	    else if(e.getSource() == cancel){
 		//System.out.println("btn -> cancel");
 		setVisible(false);
 	    }
 	}
     }
     
     /** end class edit**/
 
     
     // to do it again, jpg
     public void Save()
     {
 	BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	Graphics2D g = bi.createGraphics();
 	g.setColor(draw_surface.getBackground());
 	g.fillRect(0, 0, 789, 456);
  	g.setColor(Color.black);
 	for(int i = 0; i < trace.size(); i++)
 	    {
 		int x1 = trace.get(i).getX1(),
 		    y1 = trace.get(i).getY1(),
 		    x2 = trace.get(i).getX2(),
 		    y2 = trace.get(i).getY2();
 		g.drawLine(x1, y1, x2, y2);
 		if(dist(x1, y1, x2, y2) > 80)
 		    g.drawString(trace.get(i).name, (x1+x2)/2, (y1+y2)/2);
 			
 	    }
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		g.setColor(draw_surface.getBackground());
 		g.fillOval(coord.get(i).x-26, coord.get(i).y-26, 50, 50);
 		g.setColor(Color.black);
 		g.drawOval(coord.get(i).x-25, coord.get(i).y-25, 50, 50);
 		if(coord.get(i).isEnd())
 		    {
 			g.drawOval(coord.get(i).x-20, coord.get(i).y-20, 40, 40);			
 		    }
 		if(coord.get(i).isStart())
 		    {
 			g.drawLine(coord.get(i).x - 25, coord.get(i).y, coord.get(i).x - 100, coord.get(i).y);
 			g.drawLine(coord.get(i).x - 25, coord.get(i).y, coord.get(i).x - 50, coord.get(i).y + 15);
 			g.drawLine(coord.get(i).x - 25, coord.get(i).y, coord.get(i).x - 50, coord.get(i).y - 15);
 		    }
 		g.drawString(coord.get(i).name, coord.get(i).x-5, coord.get(i).y+5);			
 	    }
 	FileDialog fc = new FileDialog(this, "Save", FileDialog.SAVE);
 	fc.setFile("untitled.png");	
 	fc.setDirectory("c://");
 	fc.setVisible(true);
 	String fn = fc.getFile();
 	if(fn == null)
 	    {
 		//System.out.println("cancel");
 	    }
 	else
 	    {
 		try{
 		    ImageIO.write(bi, "png", new File(fn));
 		}
 		catch(IOException ioe)
 		    {
 			ioe.printStackTrace();
 		    }
 	    }
 	fc.setVisible(false);	
 	write_graph(fn.substring(0, fn.length() - 4));
     }
     
     public void write_graph(String nomFic)
     {
 	String adressedufichier = System.getProperty("user.dir") + "/"+ nomFic+".atk";
 	String str = "graph " + nomFic + "\n";
 	for(int i = 0; i < coord.size(); i++)
 	    str += coord.get(i).getName() + " " + coord.get(i).getX() + "," + coord.get(i).getY() + "\n";
 	str += "\n";
 	for(int i = 0; i < trace.size(); i++)
 	    str += trace.get(i).getN1().getName() + " - " + trace.get(i).getN2().getName() + " (" + trace.get(i).getName() + ")\n";
 	try{
 	    new RandomAccessFile(nomFic+".atk", "rw").setLength(0); // clear content
 	    FileWriter fw = new FileWriter(adressedufichier, true); 
 	    BufferedWriter output = new BufferedWriter(fw);
 	    output.write(str);
 	    output.flush();
 	    output.close();
 	}
 	catch(IOException ioe){ioe.printStackTrace();}
     }
     
     public void open_graph()
     {
 	FileDialog fc = new FileDialog(this, "Open", FileDialog.LOAD);
 	fc.setFile("untitled.png");	
 	fc.setDirectory("c://");
 	fc.setVisible(true);
 	String fn = fc.getFile();
 	if(fn == null)
 	    {
 		System.out.println("cancel");
 	    }
 	else
 	    {
 		BufferedReader br = null;
 		FileReader fr = null;
 		try {
 		    fr = new FileReader(fc.getFile());
 		    br = new BufferedReader(fr);
 		    String line;
 		    String [] str_split;
 		    String [] str_pos;
 		    int lineNo = 0;
 		    if((line = br.readLine()) == null || !line.split(" ")[0].equals("graph"))
 			{
 			    System.out.println("not graph");
 			    return ;
 			}
 		    while(((line = br.readLine()) != null) && line.length() != 0) 
 			{
 			    str_split = line.split(" ");
 			    str_pos   = str_split[1].split(",");
 			    //System.out.println("nom : " + str_split[0] + " (" + str_pos[0] + "," + str_pos[1] + ")");
 			    coord.add(new Node(Integer.parseInt(str_pos[0]), Integer.parseInt(str_pos[1]), str_split[0]));
 			}
 		    while ((line = br.readLine()) != null) 
 			{
 			    str_split = line.split(" ");
 			    trace.add(new Trace(getNodeByName(str_split[0]), getNodeByName(str_split[2])));
 			    trace.get(trace.size() - 1).setName(str_split[3].substring(1, str_split[3].length() - 1));
 			    //System.out.println("N1: " + str_split[0] + ", N2: " + str_split[2] + " str: " + str_split[3].substring(1, str_split[3].length() - 1));			    
 			}
 		    repaint();
 		}
 		catch (Exception x) {
 		    x.printStackTrace();
 		}
 		finally {
 		    if (fr != null) {
 			try {br.close();} catch (Exception ignoreMe) {}
 			try {fr.close();} catch (Exception ignoreMe) {}
 		    }
 		}
 	    }
 	fc.setVisible(false);	
 	//write_graph(fn);	
     }
     
     public int dist(int x1, int y1, int x2, int y2)
     {
 	return (int)(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
     }
 
     //actualize the panel after moved a node
     public void repaint()
     {
 	Graphics g = draw_surface.getGraphics();
 	g.setColor(draw_surface.getBackground());
 	g.fillRect(0, 0, 789, 456);
  	g.setColor(Color.black);
 	for(int i = 0; i < trace.size(); i++)
 	    {
 		if(trace.get(i).isLoop())
 		    {
 			g.drawOval(trace.get(i).getX1()-50, trace.get(i).getY1()-50, 50, 50);
 			g.drawString(trace.get(i).name, trace.get(i).getX1()-40-(int)(trace.get(i).name.length()*2.8), trace.get(i).getY1()-50);
 		    }
 		else
 		    {
 			int x1 = trace.get(i).getX1(),
 			    y1 = trace.get(i).getY1(),
 			    x2 = trace.get(i).getX2(),
 			    y2 = trace.get(i).getY2();
 			if(dist(x1, y1, x2, y2) > 80)
 			    g.drawString(trace.get(i).name, (x1+x2)/2, (y1+y2)/2);
 			draw_surface.getGraphics().drawLine(x1, y1, x2, y2);
 		    }
 	    }
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		g.setColor(draw_surface.getBackground());
 		g.fillOval(coord.get(i).x-26, coord.get(i).y-26, 50, 50);
 		g.setColor(Color.black);
 		g.drawOval(coord.get(i).x-25, coord.get(i).y-25, 50, 50);
 		if(coord.get(i).isEnd())
 		    {
 			g.drawOval(coord.get(i).x-20, coord.get(i).y-20, 40, 40);			
 		    }
 		if(coord.get(i).isStart())
 		    {
 			g.drawLine(coord.get(i).x - 25, coord.get(i).y, coord.get(i).x - 100, coord.get(i).y);
 			g.drawLine(coord.get(i).x - 25, coord.get(i).y, coord.get(i).x - 50, coord.get(i).y + 15);
 			g.drawLine(coord.get(i).x - 25, coord.get(i).y, coord.get(i).x - 50, coord.get(i).y - 15);
 		    }
 		g.drawString(coord.get(i).name, coord.get(i).x-5, coord.get(i).y+5);			
 	    }
 	//System.out.println("repaint");		
     }
 
     public void ctrl_z()
     {	
 	if(id_hist >= 0 )
 	    {
 		Action a = actions.get(id_hist);
 		switch(actions.get(id_hist).getNum())
 		    {
 		    case 1:
 			delete(actions.get(id_hist).getNode());
 			break;
 		    case 2:
 			coord.add(actions.get(id_hist).getNode());
 			trait_origin = a.getNode();
 			Iterator<Node> it_node = a.getNode().transitions.iterator();
 			Node s = null;
 			while(it_node.hasNext())
 			    {
 				s = it_node.next();
 				addTrace(s);
 			    }			
 			break;
 		    case 3:
 			delTrace(actions.get(id_hist).getTrace());
 			break;
 		    case 4:
 			Trace t = actions.get(id_hist).getTrace(); 
 			trait_origin = t.getN1();
 			addTrace(t.getN2());
 			t.getN1().add_transition(t.getN2());
 			t.getN2().add_transition(t.getN1());
 			break;
 		    case 5:
 			a.getNode().setStart(a.getOldStart());
 			a.getNode().setEnd(a.getOldEnd());
 			a.getNode().setName(a.getOldName());
 			break;
 		    case 6:
 			actions.get(id_hist).getTrace().setName(actions.get(id_hist).getOldName());
 			break;
 		    case 7:
 			trait_origin = a.getNode();
 			Move(a.getOldX(), a.getOldY());
 			repaint();
 			break;
 		    default:
 			//System.out.println("error");
 		    }
 		id_hist--;
 	    }
     }
     
     public void ctrl_y()
     {
 	if(id_hist < actions.size()-1)
 	    {
 		id_hist++;
 		Action a = actions.get(id_hist);
 		switch(a.getNum())
 		{
 		case 1:
 		    coord.add(a.getNode());
 		    break;
 		case 2:
 		    delete(a.getNode());
 		    break;
 		case 3:
 		    trace.add(a.getTrace());
 		    break;
 		case 4:
 		    delTrace(a.getTrace());
 		    break;
 		case 5:
 		    a.getNode().setStart(a.getNewStart());
 		    a.getNode().setEnd(a.getNewEnd());
 		    a.getNode().setName(a.getNewName());
 		    break;
 		case 6:
 		    actions.get(id_hist).getTrace().setName(actions.get(id_hist).getNewName());
 		    break;
 		case 7:
 		    trait_origin = a.getNode();
 		    Move(a.getNewX(), a.getNewY());
 		    repaint();
 		    break;
 		}
 	    }
     }
 
     /***************************
      *---      TESTS        ---*
      ***************************/    	
 
     /* definition: test if a < b < c */
     //call by: getTrace, getNumTrace
     public boolean isBetween(int a, int b, int c)
     { 
 	return (a <= b && b <= c) || (a >= b && b >= c);
     }
 
 
     /*definition: test if point is out of the screen*/
     //call by: mouseReleased
     public boolean isOut(int click_x, int click_y)
     {
 	return (click_x < 0 || click_y < 0 || click_x >= 789 || click_y >= 456);
     }
     
     /***************************
      *---     GET/SET       ---*
      ***************************/
     
     //use colinears vector
     /*
       return the trace where the user has cliked
      */
     public Trace getTrace(int click_x, int click_y)
     {
 	int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
 	for(int i = 0; i < trace.size(); i++)
 	    {
 		if(trace.get(i).isLoop() && 
 		   click_y >= trace.get(i).getY1() - 60 &&
 		   click_y <= trace.get(i).getY1() + 10 &&
 		   click_x >= trace.get(i).getX1() - 60 &&
 		   click_x <= trace.get(i).getX1() + 10)
 		    {
 			return trace.get(i);
 		    }
 		else
 		    {
 			x1 = trace.get(i).getX2() - trace.get(i).getX1();
 			y1 = trace.get(i).getY2() - trace.get(i).getY1();
 			x2 = click_x - trace.get(i).getX1();
 			y2 = click_y - trace.get(i).getY1();
			/*if(isBetween(-2000,x1*y2 - y1*x2,2000) && 
 			   isBetween(trace.get(i).getX1(), click_x,trace.get(i).getX2()) && 
 			   isBetween(trace.get(i).getY1(), click_y,trace.get(i).getY2())
 			   )
			*/
			if(isBetween(-2000,x1*y2 - y1*x2,2000))
 			    return trace.get(i);
 		    }
 	    }
 	return null;
     }
 
     /*
       return the number of the trace where the user has clicked
       call by: mouseClicked
      */
     public int getNumTrace(int click_x, int click_y)
     {
 	int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
	System.out.println(click_x + ", " + click_y);
 	for(int i = 0; i < trace.size(); i++)
 	    {
 		if(trace.get(i).isLoop() && 
 		   click_y >= trace.get(i).getY1() - 60 &&
 		   click_y <= trace.get(i).getY1() + 10 &&
 		   click_x >= trace.get(i).getX1() - 60 &&
 		   click_x <= trace.get(i).getX1() + 10)
 		    {
 			return (i);
 		    }
 		else
 		    {
 			x1 = trace.get(i).getX2() - trace.get(i).getX1();
 			y1 = trace.get(i).getY2() - trace.get(i).getY1();
 			x2 = click_x - trace.get(i).getX1();
 			y2 = click_y - trace.get(i).getY1();
			/*
 			if(isBetween(-2000,x1*y2 - y1*x2,2000) && 
 			   isBetween(trace.get(i).getX1(), click_x,trace.get(i).getX2()) && 
 			   isBetween(trace.get(i).getY1(), click_y,trace.get(i).getY2()))
 			    return i;
			*/
			if(isBetween(-2000,x1*y2 - y1*x2,2000))
			    return i;
 		    }
 	    }
 	return -1;
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
 
     public int getNodeId(Node n)
     {
 	for(int i = 0; i < coord.size(); i++)
 	    {
 		if(coord.get(i) == n)
 		    {
 			return i;
 		    }
 	    }
 	return -1;
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
 
     public Node getNodeByName(String name)
     {
 	for(int i = 0 ; i < coord.size(); i++)
 	    {
 		if(name.equals(coord.get(i).getName()))
 		    return coord.get(i);
 	    }
 	return null;
     }
 
     /***************************
      *---     ACTIONS       ---*
      ***************************/
 
     //create a node
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
 	    }
     }
 
     /*
      * trait_origin is no null
      * n is the node released
      * definition: to make a trace between two nodes
      */
     // TODO: split
     public void newTrait(Node n)
     {
 	if(n == null){ return;} // if node not found
 	int x_1 = trait_origin.x, y_1 = trait_origin.y, x_2 = n.x, y_2 = n.y;
 	trait_origin.add_transition(n);
 	n.add_transition(trait_origin);
 	addTrace(n);
 	//repaint();
     }
 
     /*
      * Remove a node and his traces from others nodes
      */
     public void delete(Node n)
     {
 	int i = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0;
 	for(; i < coord.size() && !coord.get(i).equals(n); i++){}
 	if(i == coord.size()){ return;}
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
 		    if(trace.get(j).getN1() == n || trace.get(j).getN2() == n)
 			trace.remove(j);				
 	    }
 	coord.remove(i);
 	return;
     }
 
     public void delTrace(Trace t)
     {
 	if(t == null){return ;}
 	if(t.getN1() == t.getN2())
 	    {
 		t.getN1().suppr_transition(t.getN1());
 	    }
 	else
 	    {
 		t.getN1().suppr_transition(t.getN2());
 		t.getN2().suppr_transition(t.getN1());
 	    }
 	for(int i = 0 ; i < trace.size(); i++)
 	    {
 		if(trace.get(i).getN1() == t.getN1() && trace.get(i).getN2() == t.getN2())
 		    {
 			trace.remove(i);
 		    }
 	    }		
     }
 
     public void addTrace(Node n)
     {
 	boolean exist = false;
 	int x1, x2, y1, y2;
 	for(int k = 0; k < trace.size(); k++)
 	    {
 		if((trace.get(k).getN1() == trait_origin && trace.get(k).getN2() == n) ||
 		   (trace.get(k).getN1() == n && trace.get(k).getN2() == trait_origin))
 		    {
 			exist = true;
 			break;
 		    }
 	    }
 	if(!exist){
 	    trace.add(new Trace(trait_origin, n));
 	}
     }
 
 
     /*
       to move a node
      */
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
     }
 
     /***************************
      *---     CONTROLS      ---*
      ***************************/
 
     public void keyTyped(KeyEvent e){}
     public void keyReleased(KeyEvent e){
 	if(e.getKeyCode() == KeyEvent.VK_CONTROL && edit) edit = false;
 	if(e.getKeyCode() == KeyEvent.VK_DELETE && suppr) suppr = false;
     }
     public void keyPressed(KeyEvent e){
 	if(e.isControlDown()) edit = true;
 	if(e.getKeyCode() == KeyEvent.VK_Z && edit){repaint(); ctrl_z(); repaint();}
 	if(e.getKeyCode() == KeyEvent.VK_Y && edit){repaint();ctrl_y(); repaint();}
 	if(e.getKeyCode() == KeyEvent.VK_O && edit){open_graph();}
 	if(e.getKeyCode() == KeyEvent.VK_S && edit){Save();}
 	if(e.getKeyCode() == KeyEvent.VK_DELETE){suppr=true;}
     }
 
     public void mouseClicked(MouseEvent e)
     {
 	if(e.getClickCount() == 2 && edit == true)
 	    {
 		int num_trace = getNumTrace(e.getX(), e.getY() - 25);
 	        int num_node  = getNumNode(e.getX(), e.getY() - 25);
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
 	int dist_mouse = dist(mousePressed_x, mousePressed_y, e.getX(), e.getY()-25);
 	if(isOut(e.getX(), e.getY() - 25))
 	    {
 		return;
 	    }	
 	if(suppr == true && trait_origin != null)
 	    {
 		Node to_del = getNode(e.getX(), e.getY() - 25);
 		actions.add(new Action(2, to_del));	
 		delete(to_del);
 		id_hist++;
 	    }
 	else if(suppr)
 	    {
 		Trace to_del = getTrace(e.getX(), e.getY() - 25);
 		delTrace(to_del);
 		actions.add(new Action(4, to_del));
 		id_hist++;
 	    }
 	else if(edit == true && trait_origin != null && dist_mouse > 2)
 	    {
 		actions.add(new Action(7, trait_origin, trait_origin.x, trait_origin.y, e.getX(), e.getY() - 25));
 		Move(e.getX(), e.getY() - 25);
 		id_hist++;
 	    }
 	else if(trait_origin != null && edit == false)
 	    {
 		newTrait(getNode(e.getX(), e.getY() - 25));
 		actions.add(new Action(3, trace.get(trace.size()-1)));
 		id_hist++;
 		trait_origin = null;
 	    }
 	else if(edit == false)
 	    {
 		String name = "q" + indice++;
 		int x = e.getX(), y = e.getY() - 25;
 		newNode(x, y, name);
 		id_hist++;
 		coord.add(new Node(x, y, name));
 		actions.add(new Action(1, coord.get(coord.size() - 1), x, y, x, y));
 	    }
 	repaint();
 	if(id_hist < actions.size() - 1)
 	    {
 		for(int i = id_hist; id_hist+1 < actions.size(); i++)
 		    {
 			actions.remove(id_hist);
 		    }
 		/*System.out.println("liste actions : ");
 		for(int i = 0; i < actions.size(); i++)
 		    {
 			System.out.println(i + ": " +actions.get(i));
 		    }
 		System.out.println("--------------------------------------\n");
 		*/
 	    }
 	else
 	    {
 		/*
 		for(int i = 0; i < actions.size(); i++)
 		    {
 			System.out.println(((id_hist == i) ? "> ": "") + i + ": " +actions.get(i));
 		    }
 		System.out.println("--------------------------------------\n");
 		*/
 	    }
     }
     
     public void mousePressed(MouseEvent e){
 	mousePressed_x = e.getX();
 	mousePressed_y = e.getY() - 25;
 	trait_origin = getNode(mousePressed_x, mousePressed_y);
     }
     public void mouseExited(MouseEvent e){}
     public void mouseEntered(MouseEvent e){}
 }
