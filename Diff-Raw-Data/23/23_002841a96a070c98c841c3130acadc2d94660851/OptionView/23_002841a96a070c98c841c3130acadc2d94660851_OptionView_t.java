 package se.chalmers.kangaroo.view;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.*;
 
 import se.chalmers.kangaroo.controller.CustomKeys;
 import se.chalmers.kangaroo.constants.*;
 
 public class OptionView extends JPanelWithBackground implements ActionListener, KeyListener, MouseListener{
 	private boolean isPainted = false;
 	private int pressedKeyId;
 	private JButton left = new JButton("GO LEFT");
 	private JButton right = new JButton("GO RIGHT");
 	private JButton jump = new JButton("JUMP");
 	private JButton item = new JButton("USE ITEM");
 	private CustomKeys ck;
 	private JPanel mainPanel;
 	private JPanel keyGrid;
 	//TODO give the variable a proper name
 	private Menuebutton back;
 	private ChangeView cv;
 
 	public OptionView(String imagepath, ChangeView cv) {
 		super(imagepath);
 		this.cv = cv;
 		ck = CustomKeys.getInstance();
 		back = new Menuebutton("resources/images/buttons/back.png");
 		back.addMouseListener(this);
 	}
 	
 	@Override
 	public void paintComponent(java.awt.Graphics g) {
 		super.paintComponent(g);
 
 		//TODO Ask what to do with the paint-spam
 		if(isPainted == false){
 		
 			
 			
 			//mainPanel holds everything
 			mainPanel = new JPanel();
 			mainPanel.setLayout(new BorderLayout());
 			this.add(mainPanel);
 			
 			//header contains the title and a back-button
 			JPanel header = new JPanel();
 			header.setLayout(new BorderLayout());
 			mainPanel.add(header, BorderLayout.NORTH);
 			
 			//backPanel is where the back button is.
 			JPanel backPanel = new JPanel();
 			backPanel.add(back);
 			header.add(backPanel, BorderLayout.WEST);
 			
 			//titlePanel is where the title is.
 			JPanel titlePanel = new JPanel();
 			JLabel title = new JLabel("<h1>Options</h1>");
 			titlePanel.add(title);
 			header.add(titlePanel, BorderLayout.EAST);
 			
 			//contentPanel is where the rest of the content is
 			JPanel contentPanel = new JPanel();
 			contentPanel.setLayout(new BorderLayout());
 			mainPanel.add(contentPanel, BorderLayout.SOUTH);
 			
 			//ckPanel is where you will be able to customize the keys
 			JPanel ckPanel = new JPanel();
 			ckPanel.setLayout(new BorderLayout());
 			contentPanel.add(ckPanel, BorderLayout.WEST);
 			
 			//it the title of custom keys section
 			ckPanel.add(new JLabel("CUSTOM KEYS"), BorderLayout.NORTH);
 			
 			//keyGrid is where you change the keys
 			keyGrid = new JPanel();
 			keyGrid.setLayout(new GridLayout(4,2));
 			
 			//Left
 			left.addActionListener(this);
 			keyGrid.add(left);
 			
 			JLabel currentLeft = new JLabel("LEFT_KEY");
 			keyGrid.add(currentLeft);
 			
 			//Right
 			right.addActionListener(this);
 			keyGrid.add(right);
 			
 			JLabel currentRight = new JLabel("RIGHT_KEY");
 			keyGrid.add(currentRight);
 			
 			//Jump
 			jump.addActionListener(this);
 			keyGrid.add(jump);
 			
 			JLabel currentJump = new JLabel("UP_KEY");
 			keyGrid.add(currentJump);
 			
 			//Item
 			item.addActionListener(this);
 			keyGrid.add(item);
 			
 			JLabel currentItem = new JLabel("C");
 			keyGrid.add(currentItem);
 			
 			ckPanel.add(keyGrid, BorderLayout.SOUTH);
 			
 			//a placeholder for upcoming options
 			contentPanel.add(new JButton("Placeholder"), BorderLayout.EAST);
 			
 			isPainted = true;
 			
 			
 		}
 		this.setVisible(true);
 	}
 	
     public void actionPerformed(ActionEvent e){
     	Object src = e.getSource();
     	if(src == left){
     		System.out.println("You pressed left.");
     	
     	}else if(src == right){
     		System.out.println("You pressed right.");
 
     	}else if(src == jump){
     		System.out.println("You pressed jump.");
 
     	}else if(src == item){
     		System.out.println("You pressed item.");
     	}
         
         
         
     }
 
 	@Override
 	public void keyPressed(KeyEvent key) {
 			System.out.println("In da if");
 			System.out.println(ck.getLeftKey());
 			ck.setleftKey(key.getKeyCode());
 			System.out.println(ck.getLeftKey());
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (e.getSource() == back)
 			back.setIcon(new ImageIcon("resources/images/buttons/back_onHover.png"));
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if (e.getSource() == back){
 			back.setIcon(new ImageIcon("resources/images/buttons/back.png"));
 		}
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.getSource() == back){
 			back.setIcon(new ImageIcon("resources/images/buttons/back_onSelect.png"));
 			cv.menuView();
 		}
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.getSource() == back){
 			back.setIcon(new ImageIcon("resources/images/buttons/back.png"));
 			cv.menuView();
 		}
 		
 	}
 
 }
