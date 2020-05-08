 package display;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 public class IntroFrame extends JFrame {
 
 	private PanelBackground	contentPane;
 
 	public IntroFrame()
 	{
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(0, 0, 500, 300);
 		setLocationRelativeTo(null);
 		setUndecorated(true);
 		contentPane = new PanelBackground("img/intro-image.png");
 		setContentPane(contentPane);
 		getContentPane().setLayout(null);
 
 		contentPane.updateUI();
 
 		setVisible(true);
 	}
 
 	public static void main(String[] args)
 	{
		IntroFrame frame = new IntroFrame();
		try
		{
			Thread.sleep(5100);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		frame.dispose();
 
 		try
 		{
 			UIManager.setLookAndFeel(utils.Preferences.getLookAndFeel());
 		}
 		catch (ClassNotFoundException | InstantiationException
 		| IllegalAccessException | UnsupportedLookAndFeelException e)
 		{
 			e.printStackTrace();
 		}
 		Window.getInstance().setContentPane(new Start());
 		Window.getInstance().setVisible(true);
 		((Start) Window.getInstance().getContentPane()).getTabbedPane()
 		.requestFocus();
 	}
 
 	class PanelBackground extends JPanel {
 
 		private ImageIcon	image;
 		private String		name;
 
 		public PanelBackground(String name)
 		{
 			this.name = name;
 		}
 
 		@Override
 		public void paint(Graphics g)
 		{
 			Dimension size = getSize();
 			image = new ImageIcon(name);
 			g.drawImage(image.getImage(), 0, 0, size.width, size.height, null);
 			setOpaque(false);
 			super.paint(g);
 		}
 	}
 }
