 package se.chalmers.kangaroo.view;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import se.chalmers.kangaroo.io.Highscore;
 import se.chalmers.kangaroo.utils.GameSound;
 
 public class VictoryView extends JPanelWithBackground implements MouseListener {
 
 	private Menubutton nextlevel, submit;
 	private JTextField namefield;
 	private String name;
 	private GameView gameview;
	private int time;
	private int level;
	private int deathcount;
 	private GameSound s;
 
 	public VictoryView(String imagepath, int deathcount, double time,
 			GameView gv, int level) {
 		super(imagepath);
 		this.time = (int)(time*1000);
 		this.level = level;
		this.deathcount = deathcount;
 		this.s = GameSound.getInstance();
 		int with = 130;
 		int height = 40;
 		Font stats = new Font("Verdana", Font.BOLD, 20);
 		Font submitFont = new Font("Verdana", Font.PLAIN, 28);
 		JLabel timeLabel = new JLabel("Time: " + time);
 		timeLabel.setFont(stats);
 		timeLabel.setForeground(Color.red);
 		JLabel deathLabel = new JLabel("Deaths: " + deathcount);
 		deathLabel.setFont(stats);
 		deathLabel.setForeground(Color.red);
 
 		this.gameview = gv;
 		nextlevel = new Menubutton("resources/gfx/buttons/nextlevel.png");
 		submit = new Menubutton("resources/gfx/buttons/submit.png");
 		namefield = new JTextField();
 		namefield.setSize(with, height);
 		namefield.setMinimumSize(new Dimension(with, height));
 		namefield.setMaximumSize(new Dimension(with, height));
 		namefield.setPreferredSize(new Dimension(with, height));
 		namefield.setFont(submitFont);
 		namefield.setFocusable(true);
 		JPanel jp1 = new JPanel() {
 			@Override
 			public void paintComponent(Graphics g) {
 				((Graphics2D) g).setComposite(AlphaComposite.getInstance(
 						AlphaComposite.SRC_OVER, 0.0f)); // draw transparent
 															// background
 				super.paintComponent(g);
 				((Graphics2D) g).setComposite(AlphaComposite.getInstance(
 						AlphaComposite.SRC_OVER, 1.0f)); // turn on opacity
 			}
 		};
 		jp1.add(timeLabel);
 		jp1.add(deathLabel);
 		JPanel jp2 = new JPanel() {
 			@Override
 			public void paintComponent(Graphics g) {
 				((Graphics2D) g).setComposite(AlphaComposite.getInstance(
 						AlphaComposite.SRC_OVER, 0.0f)); // draw transparent
 															// background
 				super.paintComponent(g);
 				((Graphics2D) g).setComposite(AlphaComposite.getInstance(
 						AlphaComposite.SRC_OVER, 1.0f)); // turn on opacity
 			}
 		};
 		jp2.add(namefield);
 		jp2.add(submit);
 
 		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
 		this.setLayout(layout);
 		this.setSize(1024, 576);
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/congratulations.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(jp1);
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(jp2);
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(nextlevel);
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/transparent.png"));
 		this.add(new Menubutton("resources/gfx/misc/stretchbar.png"));
 		nextlevel.addMouseListener(this);
 		submit.addMouseListener(this);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// Empty method
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (e.getSource() == nextlevel)
 			nextlevel.setIcon(new ImageIcon(
 					"resources/gfx/buttons/nextlevel_onHover.png"));
 		if (e.getSource() == submit)
 			submit.setIcon(new ImageIcon("resources/gfx/buttons/submit_onHover.png"));
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if (e.getSource() == nextlevel)
 			nextlevel.setIcon(new ImageIcon("resources/gfx/buttons/nextlevel.png"));
 		if (e.getSource() == submit)
 			submit.setIcon(new ImageIcon("resources/gfx/buttons/submit.png"));
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.getSource() == nextlevel)
 			nextlevel.setIcon(new ImageIcon(
 					"resources/gfx/buttons/nextlevel_onSelect.png"));
 		if (e.getSource() == submit)
 			submit.setIcon(new ImageIcon("resources/gfx/buttons/submit_onSelect.png"));
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.getSource() == nextlevel)
 			nextlevel.setIcon(new ImageIcon("resources/gfx/buttons/nextlevel.png"));
 		s.playBgMusic("level_" + (level+2));
 		gameview.setNewLevel(true);
 		if (e.getSource() == submit) {
 			submit.setIcon(new ImageIcon("resources/gfx/buttons/submit.png"));
 		s.playBgMusic("level_" + (level+2));
 			try {
 				name = removeSpaces(namefield.getText());
 				Highscore h = Highscore.getInstance();
				h.setHighscore(name, level, time, deathcount);
 			} catch (NullPointerException exc) {
 			}
 
 		}
 	}
 
 	public String removeSpaces(String name) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < name.length(); i++) {
 			if (!((Character) name.charAt(i)).equals(' '))
 				sb.append(name.charAt(i));
 		}
 		return sb.toString();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 }
