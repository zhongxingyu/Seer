package fp;
 
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 public class GUI implements MouseMotionListener  
 {
     private JPopupMenu Pmenu;
     private JFrame frame;
     private Container content;
     private JLabel label3;
 
     public static void main(String[] args)
     {
         GUI gui = new GUI();
     }
 
     public GUI()
     {
         frame = new JFrame("Festival Planner");
         content = frame.getContentPane();
         content.setLayout( new BorderLayout());
         initMenuBar();
         PopUpMenu();
         initTimeline();
         initFoto();
         initRating();
         initNaam();
         initWensen();
         initBeschrijving();
 
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(500, 500);
         frame.setVisible(true);
     }
 
     private void initBeschrijving() {
 		// TODO Auto-generated method stub
     	JLabel label = new JLabel("label");
     	content.add(label, BorderLayout.EAST);
     	JLabel label1 = new JLabel("label1");
     	content.add(label1,BorderLayout.WEST);
     	JLabel label2 = new JLabel("label2");
     	content.add(label2,BorderLayout.SOUTH);
     	label3 = new JLabel("Label3 klik hier :P ");
     	content .add(label3, BorderLayout.NORTH);
 		
 	}
 
 	private void initWensen() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void initNaam() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void initRating() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void initFoto() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void initTimeline() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void initMenuBar() 
     {   
         JMenuBar menuBar = new JMenuBar();
         JMenu file = new JMenu("File");
         JMenuItem nieuw = new JMenuItem("New");
         nieuw.addActionListener(new ActionListener() 
             {
                 public void actionPerformed(ActionEvent e)
                 {
                     nieuw();    
                 }
             });
         JMenuItem open = new JMenuItem("Open");
         open.addActionListener(new ActionListener()
             {
                 public void actionPerformed(ActionEvent e)
                 {
                     open();
                 }
             });
         JMenuItem save = new JMenuItem("Save");
         save.addActionListener(new ActionListener()
             {
                 public void actionPerformed(ActionEvent e)
                 {
                     save();
                 }
             });
         JMenuItem exit = new JMenuItem("Exit");
         exit.addActionListener(new ActionListener()
             {
                 public void actionPerformed(ActionEvent e)
                 {
                     exit();
                 }
             });
         file.add(nieuw);
         file.addSeparator();
         file.add(open);
         file.add(save);
         file.addSeparator();
         file.add(exit);
         menuBar.add(file);
         frame.setJMenuBar(menuBar);
     }
 
     protected void exit() {
     	System.exit(0);
 
     }
 
     protected void save() {
         // TODO Auto-generated method stub
 
     }
 
     protected void open() {
         // TODO Auto-generated method stub
 
     }
 
     protected void nieuw() 
     {
         // TODO Auto-generated method stub
 
     }
 
     public void PopUpMenu(){
         Pmenu = new JPopupMenu();
         JMenuItem addAct = new JMenuItem("Add Act");
         addAct.addActionListener(new ActionListener()
         {
         	public void actionPerformed(ActionEvent e)
         	{
         		addAct();
         	}
         });
         Pmenu.add(addAct);
         JMenuItem removeAct = new JMenuItem("Remove Act");
         removeAct.addActionListener(new ActionListener()
         {
         	public void actionPerformed(ActionEvent e)
         	{
         		removeAct();
         	}
         });
         Pmenu.add(removeAct);
         final JMenuItem editAct = new JMenuItem("Edit Act");
         editAct.addActionListener(new ActionListener()
         {
         	public void actionPerformed(ActionEvent e)
         	{
         		editAct();
         	}
         });
         editAct.setEnabled(false);
                 
         
         
         Pmenu.addSeparator();
       JMenuItem addStage = new JMenuItem("Add Stage");
       addStage.addActionListener(new ActionListener()
       {
       	public void actionPerformed(ActionEvent e)
       	{
       		addStage();
       	}
       });
       Pmenu.add(addStage);
       JMenuItem removeStage = new JMenuItem("Remove Stage");
       removeStage.addActionListener(new ActionListener()
       {
       	public void actionPerformed(ActionEvent e)
       	{
       		removeStage();
       	}
       });
       Pmenu.add(removeStage);
         
         
         label3.addMouseListener(new MouseAdapter(){
                 public void mouseReleased(MouseEvent Me){
                 	try{
                 	if(label3.contains(editAct.getMousePosition()));
                 	{
                 		editAct.setEnabled(true);
                     Pmenu.add(editAct);
                 	}
                 	}
                 	catch(Exception e){};
                      	 if(Me.getButton() == MouseEvent.BUTTON3){
                                 Pmenu.show(Me.getComponent(), Me.getX(), Me.getY());
                         	
                     }
                 }
             });
     }
 
     protected void removeStage() 
     {
 		// TODO Auto-generated method stub
 		
 	}
 
 	protected void addStage() 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	protected void editAct() 
 	{
 		// TODO Auto-generated method stub
 		System.out.println("aaaaaaaaaa");
 	}
 
 	protected void removeAct() 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	protected void addAct() 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
     public void mouseDragged(MouseEvent arg0) 
 	{
         
 		// TODO Auto-generated method stub
     }
 
     @Override
     public void mouseMoved(MouseEvent arg0) 
     {
     	// TODO Auto-generated method stub
     	
     }
 }
