 package se.chalmers.kangaroo.view;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import se.chalmers.kangaroo.constants.Constants;
 import se.chalmers.kangaroo.io.Highscore;
 
 public class ShowHighscoreView extends JPanelWithBackground implements
 		MouseListener {
 	private Menubutton back;
 	private ChangeView cv;
 	private String[] names;
 	private int[] times;
 	private int[] deaths;
 	private Highscore hs;
 	private int level;
 
 	public ShowHighscoreView(String imagepath, ChangeView cv, int level) {
 		super(imagepath);
 		this.cv = cv;
 		hs = Highscore.getInstance();
 		this.setFocusable(true);
 		back = new Menubutton("resources/images/buttons/back.png");
 		back.addMouseListener(this);
 		this.setLayout(new BorderLayout());
 
 		// Header
 		JPanel headerPanel = new JPanel();
 		headerPanel.setLayout(new BorderLayout());
 		int titleHeight = 100;
 
 		this.add(headerPanel, BorderLayout.NORTH);
 		this.setMinimumSize(Constants.RESOLUTION);
 		this.setMaximumSize(Constants.RESOLUTION);
 		this.setPreferredSize(Constants.RESOLUTION);
 
 		// Back-button
 		JPanel backPanel = new JPanel(new BorderLayout());
 		backPanel.add(back, BorderLayout.WEST);
 		backPanel.setMinimumSize(new Dimension(Constants.RESOLUTION_WIDTH / 3,
 				titleHeight));
 		backPanel.setMaximumSize(new Dimension(Constants.RESOLUTION_WIDTH / 3,
 				titleHeight));
 		backPanel.setPreferredSize(new Dimension(
 				Constants.RESOLUTION_WIDTH / 3, titleHeight));
 		headerPanel.add(backPanel, BorderLayout.WEST);
 
 		// Title
 		JPanel titlePanel = new JPanel();
 		JLabel title = new JLabel(Constants.TITLE_START + "Highscore - Level "
 				+ level + Constants.TITLE_END);
 		titlePanel.add(title);
 		headerPanel.add(title, BorderLayout.CENTER);
 
 		headerPanel.setMinimumSize(new Dimension(Constants.RESOLUTION_WIDTH,
 				titleHeight));
 		title.setMaximumSize(new Dimension(Constants.RESOLUTION_WIDTH,
 				titleHeight));
 		title.setPreferredSize(new Dimension(Constants.RESOLUTION_WIDTH,
 				titleHeight));
 
 		// Content
 		JPanel contentPanel = new JPanel(new GridLayout(6, 6));
 		this.add(contentPanel, BorderLayout.SOUTH);
 
 		// Information Labels
 		for (int i = 0; i < 2; i++) {
 			contentPanel
 					.add(new JLabel("<html><body><b>Name</b></body></html>"));
 			contentPanel
 					.add(new JLabel("<html><body><b>Time</b></body></html>"));
 			contentPanel.add(new JLabel(
 					"<html><body><b>Deaths</b></body></html>"));
 		}
 		
 		this.level = level;
 		names = hs.getNames(level);
 		times = hs.getTimes(level);
 		 deaths = hs.getDeaths(level);
 
 		// Writes out labels on every person in highscore, may have to do after
 		// setLevel
 		for (int i = 0; i < Constants.NUMBER_OF_HIGHSCORENAMES/2 ; i++) {
 			contentPanel.add(new JLabel(names[i]));
 			contentPanel.add(new JLabel(""
 					+ times[i]/1000.0));
 			contentPanel.add(new JLabel("" + deaths[i]));
 			contentPanel.add(new JLabel(names[i+5]));
 			contentPanel.add(new JLabel(""
 					+ times[i+5]/1000.0));
 			contentPanel.add(new JLabel("" + deaths[i+5]));
 		}
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		if (e.getSource() == back)
 			back.setIcon(new ImageIcon(
 					"resources/images/buttons/back_onHover.png"));
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		if (e.getSource() == back) {
 			back.setIcon(new ImageIcon("resources/images/buttons/back.png"));
 		}
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		if (e.getSource() == back) {
 			back.setIcon(new ImageIcon(
 					"resources/images/buttons/back_onSelect.png"));
 		}
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if (e.getSource() == back) {
 			back.setIcon(new ImageIcon("resources/images/buttons/back.png"));
			cv.back();
 		}
 
 	}
 
 }
