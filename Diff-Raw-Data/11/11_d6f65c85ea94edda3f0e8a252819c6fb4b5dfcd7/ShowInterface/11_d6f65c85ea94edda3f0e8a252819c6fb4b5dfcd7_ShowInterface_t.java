 package gui;
 
 import glare.ClassFactory;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 
 import javax.swing.*;
 
 import bll.*;
 
 public class ShowInterface extends JFrame implements KeyListener {
 	/**
 	 * 
 	 * @author Andreas J
 	 */
 	private ViewController viewCtrl;
 	private JButton settingsButton;
 	private LoginDialog ld = null;
 	private Dimension dim=null;
 	private ImageSlider slider;
 	private JFrame fr = null;
 	private ImageShow show;
 	private Constraints gbc = null;
 	private GraphicsDevice device;
 
 	// private final ImageShow show;
 
 	public ShowInterface(ViewController viewCtrl) throws IOException {
 		
 		fr = this;
 		this.viewCtrl = viewCtrl;
 		
 
 		slider = new ImageSlider();
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setAlwaysOnTop(true);
 		setAutoRequestFocus(true);
 		addKeyListener(this);
 		getContentPane().add(slider);
 		setUndecorated(true);
 		setVisible(true);
 		pack();
 		setFullScreen();
 		
 	}
 
 	private void setFullScreen() {
 		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
 				.getLocalGraphicsEnvironment();
 		device = graphicsEnvironment.getDefaultScreenDevice();
 		if (device.isFullScreenSupported()) {
 			device.setFullScreenWindow(this);
 			this.validate();
 		}
 	}
 
 	class ImageSlider extends JPanel implements ActionListener, Runnable {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1;
 		boolean stop = false;
 		private Thread th;
 		private JLabel backgroundImageLabel;
 		private ImageIcon icon;
 		private Action escapeAction;
 
 		public ImageSlider() throws IOException {
 			dim =Toolkit.getDefaultToolkit().getScreenSize();
 		
 			show = new ImageShow(viewCtrl, (int) dim.getWidth(),
 					(int) dim.getHeight());
 			escapeAction = new EscapeAction();
 			setPreferredSize(new Dimension(dim.width, dim.height));
 			getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
 					"doEscapeAction");
 
 			getActionMap().put("doEscapeAction", escapeAction);
 			setDoubleBuffered(true);
 			setFocusable(true);
 			setBackground(Color.blue);
 			init();
 			setLayout(null);
 			
 
 		}
 
 		private void init() {
 
 		// set settingsButton properties
 			settingsButton = new JButton();
 			settingsButton.setIcon(new ImageIcon(getClass().getResource(
 					"/resource/img/settings.gif")));
 			settingsButton.setBorderPainted(false);
 			settingsButton.setContentAreaFilled(false);
 			settingsButton.addActionListener(this); 
 			int w=150,h=150;
 			System.out.println(getSize().getHeight());
 			settingsButton.setBounds(0, (int) (dim.getHeight()-h), w,h);
 			add(settingsButton);
 			
 			// 
 			backgroundImageLabel= new JLabel();
 			
 			backgroundImageLabel.setIcon(new ImageIcon(getClass().getResource(
 					"/resource/img/glare.jpg")));;
 			backgroundImageLabel.setIconTextGap(0);
 			//backgroundImageLabel.setPreferredSize(dim);
 			backgroundImageLabel.setBounds(0, 0,(int) dim.getWidth(), (int)dim.getHeight());
 			add(backgroundImageLabel);
			start();
 
 		}
 		public void start(){
 			th= new Thread(this);
 			th.start();
 			
 		}
 
 		
 
 		@Override
 		public void paintComponent(Graphics g) {
             super.paintComponent(g);
             if (show != null) {
 				show.paint(g);
 			}
          
         }
 	
 
 		@Override
 		public void run() {
 			System.out.println("run()");
 
 			try {
 				while (stop!=true) {
 				
 						System.out.println("before thread");
 						Thread.sleep(viewCtrl.getDisplayTime());
 						System.out.println("after thread");
 						show.moveNext();
 						repaint();
 						System.out.println("ShowINterface, kaller moveNext()");
 					
 
 				}
 			} catch (InterruptedException ie) {
 				System.out.println("Interrupted slide show...");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				th.join();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 	   public void stopClick() {
 			stop = true;
 
 		}
 
 		private void openLoginBox() {
 
 			boolean loggedIn = new LoginDialog(fr).getSucceeded();
 			System.out.println("Login return equals" + loggedIn);
 			if (loggedIn)
 				openSettingsFrame();
 		}
 
 		private void openSettingsFrame() {
 			System.out.println("Tries to open settingsframe");
 
 			SettingsFrame intfr = new SettingsFrame(viewCtrl);
 			getContentPane().add(intfr);
 			pack();
 			repaint();
 
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource().equals(settingsButton)) {
 				stopClick();
 				openLoginBox();
 
 			}
 			repaint();
 
 		}
 
 		class EscapeAction extends AbstractAction {
 			public void actionPerformed(ActionEvent tf) {
 			
 			 System.exit(1);
 
 				
 				System.out.println("Escape");
 
 			} // end method actionPerformed()
 
 		}
 
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 	if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
 		
 		System.exit(1);
 	}
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
