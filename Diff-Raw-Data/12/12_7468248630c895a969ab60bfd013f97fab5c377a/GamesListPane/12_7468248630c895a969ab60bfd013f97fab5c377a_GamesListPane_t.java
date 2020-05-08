 package net.as.panes;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 
 import net.as.data.Game;
 import net.as.data.events.GameListener;
 import net.as.gui.MainFrame;
import net.as.utils.LinkUtils;
 
 public class GamesListPane extends JPanel implements ILauncherPane,
 		GameListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final JPanel games;
 	private final JScrollPane gamesScroll;
 	private int selectedGame = 0;
 	public static ArrayList<JPanel> gamePanels;
 
 	public GamesListPane() {
 		super();
 		this.setBorder(new EmptyBorder(5, 5, 5, 5));
 		this.setLayout(null);
 		games = new JPanel();
 		games.setLayout(null);
 		games.setOpaque(false);
 		gamesScroll = new JScrollPane();
 		gamesScroll.setBounds(0, 30, 220, 450);
 		gamesScroll
 				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		gamesScroll
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		gamesScroll.setWheelScrollingEnabled(true);
 		gamesScroll.setOpaque(false);
 		gamesScroll.setViewportView(games);
 		add(gamesScroll);
 		gamePanels = new ArrayList<JPanel>();
 	}
 
 	@Override
 	public void onVisible() {
 	}
 
 	@Override
 	public void onGameAdded(Game game) {
 		System.out.println(game.getName() + " " + game.getDesc() + " "
 				+ game.getLogo());
 		addGame(game);
 	}
 
 	public void addGame(final Game game) {
 		final JPanel p = new JPanel();
 		final int gameIndex = gamePanels.size();
 		p.setBounds(0, (gameIndex * 55), 300, 55);
 		p.setLayout(null);
 		p.setBackground(new Color(218, 111, 5));
 		MouseListener lin = new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				selectedGame = gameIndex;
 				selectGame();
 				MainFrame.getGameInfoInstance().setInfo(game.getDesc(),
 						game.getSplash());
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				selectedGame = gameIndex;
 				selectGame();
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 			}
 		};
 		JLabel logo;
 		try {
 			Image logoimg = Toolkit.getDefaultToolkit().createImage(
					new URL(LinkUtils.getGithubLink(game.getLogo())));
 			logoimg = logoimg.getScaledInstance(42, 42, Image.SCALE_DEFAULT);
 			logo = new JLabel(new ImageIcon(logoimg));
 			logo.setBounds(6, 6, 42, 42);
 			logo.setVisible(true);
 			logo.addMouseListener(lin);
 			p.add(logo);
 		} catch (MalformedURLException e) {
 		}
 		JLabel filler = new JLabel("<html><strong>" + game.getName()
 				+ "</strong></html>");
 		filler.setBorder(null);
 		filler.setForeground(Color.white);
 		filler.setBounds(58, 6, 378, 42);
 		filler.setBackground(new Color(255, 255, 255, 0));
 		p.add(filler);
 		p.addMouseListener(lin);
 		filler.addMouseListener(lin);
 		gamePanels.add(p);
 		games.add(p);
 		gamesScroll.revalidate();
 		games.repaint();
 		System.out.println("Added : " + game.getName() + " ID : " + gameIndex);
 	}
 
 	public void selectGame() {
 		for (int i = 0; i < gamePanels.size(); i++) {
 			if (selectedGame == i) {
 				gamePanels.get(i)
 						.setBackground(new Color(218, 111, 5).darker());
 				gamePanels.get(i).setCursor(
 						Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 			} else {
 				gamePanels.get(i).setBackground(new Color(218, 111, 5));
 				gamePanels.get(i).setCursor(
 						Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			}
 		}
 	}
 }
