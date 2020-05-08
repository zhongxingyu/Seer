 package menu;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public abstract class AbstractMenuPanel extends JPanel {
 
 	private static transient Image background = new ImageIcon("Assets/Menus/Background.png").getImage();
 	private static transient Image planet = new ImageIcon("Assets/Menus/planet.png").getImage();
 	private static transient Image logo = new ImageIcon("Assets/Menus/GameLogo.png").getImage();
 
 	private int y = 0;
 
 
 	public void addButton(final MainFrame frame, ActionListener listener, String path) {
 
 		ImageIcon icon = new ImageIcon("Assets/Menus/" + path + ".png");
 		JButton button = new JButton(icon);
 
 		this.add(button);
 		button.setBorder(null);
 		button.setBackground(new Color(255, 255, 255, 0));
 		button.addActionListener(listener);
 		button.setOpaque(false);
 		button.setContentAreaFilled(false);
 		y++;
 		button.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 			}
 		});
 	}
 
 	public void addLabel(final MainFrame frame, String path) {
 		// this.setLayout(bl);
 		// fl.setAlignment(FlowLayout.CENTER);
 
 		ImageIcon icon = new ImageIcon("Assets/Menus/" + path + ".png");
 		JLabel label = new JLabel(icon);
 
 		this.add(label);
 		label.setBorder(null);
 		label.setBackground(new Color(255, 255, 255, 0));
 		y++;
 	}
 
 	public void addComponent(JComponent comp) {
 		this.add(comp);
 		y++;
 	}
 
 	protected void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, null);
		g.drawImage(planet, 0, 0, null);
 		float zoom = 0.6f;
 		g.drawImage(logo,
				(int)((this.getWidth() - logo.getWidth(null) * zoom) / 2),
				(int)((this.getHeight() - logo.getHeight(null)) / 2),
				(int)(logo.getWidth(null) * zoom),
				(int)(logo.getHeight(null) * zoom),
 				null);
 	}
 }
