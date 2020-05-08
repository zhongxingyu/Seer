 package client;
 
 import java.awt.BorderLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 import javax.swing.*;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import java.awt.*;
 import java.awt.image.*;
 
 class ConnectionButtonHandler implements ActionListener {
 
     GUI gui;
     int camera;
 
     public ConnectionButtonHandler(GUI gui, int cam) {
         this.gui = gui;
         this.camera = cam;
     }
 
     public void actionPerformed(ActionEvent evt) {
     	gui.pack();
         gui.toggleConnection(camera);
     }
 }
 
 class MovementButtonHandler implements ActionListener {
 
     GUI gui;
     int button;
 
     public MovementButtonHandler(GUI gui, int button) {
         this.gui = gui;
         this.button = button;
     }
 
     public void actionPerformed(ActionEvent evt) {
         gui.selectMovement(button);
     }
 }
 
 class SynchronizeButtonHandler implements ActionListener {
 
     GUI gui;
     int button;
 
     public SynchronizeButtonHandler(GUI gui, int button) {
         this.gui = gui;
         this.button = button;
     }
 
     public void actionPerformed(ActionEvent evt) {
         gui.selectSync(button);
     }
 }
 
 class GUI extends JFrame {
 
 
 	public static void main(String [] args){
 		new GUI();
 	}
 
     JLabel camera1;
     JLabel camera2;
     JButton button;
     JButton connectCamera1;
     JButton connectCamera2;
     JButton disconnectCamera1;
     JButton disconnectCamera2;
     JLabel delay1, fps1, movement1;
         
     JLabel delay2, fps2, movement2;
     
     JRadioButton movie, idle, auto, sync, async;
     
     boolean camera1connected = false;
     boolean camera2connected = false;
     
    byte [] jpeg = new byte[Axis211A.IMAGE_BUFFER_SIZE];

     public GUI() {
         super();
         
         ImageIcon icon = new ImageIcon("../camera.jpeg");
         
     	camera1 = new JLabel(icon);
     	camera2 = new JLabel(icon);
         
         connectCamera1 = new JButton("Camera 1 Connect");
         connectCamera2 = new JButton("Camera 2 Connect");
         disconnectCamera1 = new JButton("Camera 1 Disconnect");
         disconnectCamera2 = new JButton("Camera 2 Disconnect");
         
         delay1 = new JLabel("Delay: 0.0s");
         fps1 = new JLabel("FPS: 60.0");
         movement1 = new JLabel("Movement: Idle");
         
         delay2 = new JLabel("Delay: 0.0s");
         fps2 = new JLabel("FPS: 60.0");
         movement2 = new JLabel("Movement: Idle");
         
         // Radio Buttons
         movie = new JRadioButton("Movie");
         idle = new JRadioButton("Idle");
         auto = new JRadioButton("Auto");
         
         sync = new JRadioButton("Synchronus");
         async = new JRadioButton("Asynchronus");
         
         
         connectCamera1.addActionListener(new ConnectionButtonHandler(this, 1));
         disconnectCamera1.addActionListener(new ConnectionButtonHandler(this, 1));
         connectCamera2.addActionListener(new ConnectionButtonHandler(this, 2));
         disconnectCamera2.addActionListener(new ConnectionButtonHandler(this, 2));
         
         movie.addActionListener(new MovementButtonHandler(this, 1));
         idle.addActionListener(new MovementButtonHandler(this, 2));
         auto.addActionListener(new MovementButtonHandler(this, 3));
         
         sync.addActionListener(new SynchronizeButtonHandler(this, 1));
         async.addActionListener(new SynchronizeButtonHandler(this, 2));
 		
 		Container pane = this.getContentPane();
 		
 		Container left = new Container();
 		left.setLayout(new GridBagLayout());
 		Container right = new Container();
 		right.setLayout(new GridBagLayout());
 		
 		Container bottom_left = new Container();
 		bottom_left.setLayout(new GridBagLayout());
 		
 		Container bottom_right = new Container();
 		bottom_right.setLayout(new GridBagLayout());
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		left.add(connectCamera1, c);
 		right.add(connectCamera2, c);
 		
 		c.gridy = 1;
 		left.add(disconnectCamera1, c);
 		right.add(disconnectCamera2, c);
 		
 		c.gridy = 2;
 		left.add(delay1, c);
 		right.add(delay2, c);
 		
 		c.gridy = 3;
 		left.add(fps1, c);
 		right.add(fps2, c);
 		
 		c.gridy = 4;
 		left.add(movement1, c);
 		right.add(movement2, c);
 		
 		
 		// just reusing this, new object for default vals
 		c = new GridBagConstraints();
 		
 		c.gridx = 0;
 		c.gridy = 0;
 		bottom_left.add(movie, c);
 		bottom_right.add(sync, c);
 		
 		c.gridy = 1;
 		bottom_left.add(idle, c);
 		bottom_right.add(async, c);
 		
 		c.gridy = 2;
 		bottom_left.add(auto, c);
 		
 		
 		pane.setLayout(new GridBagLayout());
 		c = new GridBagConstraints();
         
         c.ipadx = 50;
         c.ipady = 50;
         
         c.gridx = 0;
 		c.gridy = 0;
         pane.add(left, c);
         
         c.gridwidth = 1;
 		c.gridx = 1;
 		c.gridy = 0;
         pane.add(camera1, c);
         
 		c.gridx = 2;
 		c.gridy = 0;
         pane.add(camera2, c);
         
         c.gridx = 3;
         c.gridy = 0;
         pane.add(right, c);
         
         c.gridwidth = 1;
         c.gridx = 1;
         c.gridy = 1;
         pane.add(bottom_left, c);
         
         c.gridx = 2;
         
         pane.add(bottom_right, c);
         
         this.setLocationRelativeTo(null);
         this.pack();
         setVisible(true);
         updateButtons();
     }
     
     // Toggles connection. 1 for camera 1 and 2 for camera 2.
     // NOTE: We likely want to gray out ALL buttons during the connection and disconnection process. Implement this when we get there.
     public void toggleConnection(int camera){
     	if(camera == 1)
     		camera1connected = !camera1connected;
     	else if (camera == 2)
     		camera2connected = !camera2connected;
     		
         updateButtons();
     }
     
     // Based on whether the cameras are connected or not, updates the buttons to be grayed out
     public void updateButtons(){
     	connectCamera1.setEnabled(!camera1connected);
     	connectCamera2.setEnabled(!camera2connected);
     	disconnectCamera1.setEnabled(camera1connected);
     	disconnectCamera2.setEnabled(camera2connected);
     }
     
     //sets what will be displayed visually for the delay val
     public void setDelay(double delay, int camera){
     	JLabel j = (camera == 1) ? delay1 : delay2;
     	j.setText("Delay: " + delay);
 	}
 	
 	public void setFPS(double fps, int camera){
     	JLabel j = (camera == 1) ? fps1 : fps2;
     	j.setText("FPS: " + fps);
 	}
 	
 	public void setMovement(String movement, int camera){
     	JLabel j = (camera == 1) ? movement1 : movement2;
     	j.setText("Movement: " + movement);
 	}
 	
 	// sets the movement mode based on a number 1 - 2 - 3 (sequentially vertical)
 	public void selectMovement(int num){
 		 //deselect all
 		 movie.setSelected(false);
 		 idle.setSelected(false);
 		 auto.setSelected(false);
 		 
 		 switch(num){
 		 	case 1:
 		 		movie.setSelected(true);
 		 		break;
 		 	case 2:
 		 		idle.setSelected(true);
 		 		break;
 		 	case 3:
 		 		auto.setSelected(true);
 		 		break;
 		 }
 	}
 	
 	// sets the Sync mode based on a number 1 - 2 (sequentially vertical)
 	public void selectSync(int num){
 		 //deselect all
 		 sync.setSelected(false);
 		 async.setSelected(false);
 		 
 		 switch(num){
 		 	case 1:
 		 		sync.setSelected(true);
 		 		break;
 		 	case 2:
 		 		async.setSelected(true);
 		 		break;
 		 }
 	}
 	
 	
 	public void updateCamera1(byte[] data) {
         Image theImage = getToolkit().createImage(data);
         getToolkit().prepareImage(theImage,-1,-1,null);     
         camera1.setIcon(new ImageIcon(theImage));
     }
     
     public void updateCamera2(byte[] data) {
         Image theImage = getToolkit().createImage(data);
         getToolkit().prepareImage(theImage,-1,-1,null);     
         camera2.setIcon(new ImageIcon(theImage));
     }
 	
 }
