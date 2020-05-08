 package se.chalmers.kangaroo.view;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 
 public class VictoryView extends JPanelWithBackground implements MouseListener {
 
 	private Menubutton nextlevel, submit;
 	private JTextField namefield;
 	private String name;
 	private GameView gameview;
 
 	public VictoryView(String imagepath, int deathcount, double time, GameView gv) {
 		super(imagepath);
 
 		this.gameview = gv;
 		nextlevel = new Menubutton("resources/images/nextlevel.png");
 		submit = new Menubutton("resources/images/submit.png");
 		namefield = new JTextField();
 		JPanel jp = new JPanel();
 		jp.add(namefield);
 		jp.add(submit);
 
 		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
 		this.setLayout(layout);
 		this.setSize(1024, 576);
		this.add(Box.createVerticalGlue());
 		this.add(new Menubutton("resources/images/victory_logo.png"));
		this.add(Box.createVerticalGlue());
		this.add(Box.createVerticalGlue());
		this.add(Box.createVerticalGlue());
 		this.add(new Menubutton("resources/images/congratulations.png"));
		this.add(Box.createVerticalGlue());
		this.add(Box.createVerticalGlue());
 		this.add(new JLabel("Deaths: " + deathcount));
		this.add(Box.createVerticalGlue());
 		this.add(new JLabel("Time: " + time));
 		this.add(jp);
		this.add(Box.createVerticalGlue());
		this.add(new Menubutton("resources/images/nextlevel.png"));
		this.add(Box.createVerticalGlue());
		this.add(Box.createVerticalGlue());
		this.add(Box.createVerticalGlue());

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
 					"resources/images/nextlevel_onHover.png"));
 		if (e.getSource() == submit)
 			submit.setIcon(new ImageIcon("resources/images/submit_onHover.png"));
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if (e.getSource() == nextlevel)
 			nextlevel.setIcon(new ImageIcon("resources/images/nextlevel.png"));
 		if (e.getSource() == submit)
 			submit.setIcon(new ImageIcon("resources/images/submit.png"));
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.getSource() == nextlevel)
 			nextlevel.setIcon(new ImageIcon(
 					"resources/images/nextlevel_onSelect.png"));
 		if (e.getSource() == submit)
 			submit.setIcon(new ImageIcon("resources/images/submit_onSelect.png"));
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.getSource() == nextlevel)
 			nextlevel.setIcon(new ImageIcon("resources/images/nextlevel.png"));
 		gameview.setNewLevel(true);
 		if (e.getSource() == submit) {
 			submit.setIcon(new ImageIcon("resources/images/submit.png"));
 			try {
 				name = removeSpaces(namefield.getText());
 
 			} catch (NullPointerException exc) { };
 
 		}
 	}
 
 	public String removeSpaces(String name) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < name.length(); i++) {
 			if (!((Character) name.charAt(i)).equals(' ')
 					|| !((Character) name.charAt(i)).equals(''))
 				sb.append(name.charAt(i));
 		}
 		return sb.toString();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 }
