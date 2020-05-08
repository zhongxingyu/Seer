 package net.as.panes;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import net.as.utils.LinkUtils;
 
 public class GameInfoPane extends JPanel {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final JEditorPane gameInfo;
 	private JLabel logo;
 
 	public GameInfoPane() {
 		super();
 		this.setBorder(new EmptyBorder(5, 5, 5, 5));
 		this.setLayout(null);
 		this.setBackground(new Color(218, 111, 5));
 		gameInfo = new JEditorPane();
 		gameInfo.setEditable(false);
 		gameInfo.setContentType("text/html");
 		gameInfo.addHyperlinkListener(new HyperlinkListener() {
 			@Override
 			public void hyperlinkUpdate(HyperlinkEvent event) {
 				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 				}
 			}
 		});
 		String text = "<html><h2><center>Alternate Steam</center></h2><p>Alternate Steam  un'alternativa al noto programma Steam,con esso potrete scaricare i giochi ( anche a pagamento ) in modo facile e veloce senza faticosi passaggi, ed ovviamente <strong>TUTTO GRATUITO</strong></p></html>";
 		gameInfo.setText(text);
 		gameInfo.setBounds(10, 210, 410, 90);
 		add(gameInfo);
 		gameInfo.revalidate();
 		gameInfo.repaint();
 		JScrollPane infoScroll = new JScrollPane();
 		infoScroll.setBounds(10, 210, 560, 240);
 		infoScroll
 				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		infoScroll
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		infoScroll.setWheelScrollingEnabled(true);
 		infoScroll.setViewportView(gameInfo);
 		infoScroll.setOpaque(false);
 		add(infoScroll);
 		Image logoimg;
 		try {
 			logoimg = Toolkit.getDefaultToolkit().createImage(
 					new URL(LinkUtils.getGithubLink("altersteam.png")));
 			logoimg = logoimg.getScaledInstance(338, 190, Image.SCALE_DEFAULT);
 			logo = new JLabel(new ImageIcon(logoimg));
 			logo.setBounds(10, 10, 560, 190);
 			add(logo);
 		} catch (MalformedURLException e) {
 		}
 
 	}
 
 	public void setInfo(String info, String image) {
 		gameInfo.setText(info);
 		Image logoimg;
 		try {
			logoimg = Toolkit.getDefaultToolkit().createImage(
					new URL(LinkUtils.getGithubLink(image)));
 			remove(logo);
 			logo = new JLabel(new ImageIcon(logoimg));
 			logo.setBounds(10, 10, 560, 190);
 			add(logo);
 		} catch (MalformedURLException e) {
 		}
 	}
 }
