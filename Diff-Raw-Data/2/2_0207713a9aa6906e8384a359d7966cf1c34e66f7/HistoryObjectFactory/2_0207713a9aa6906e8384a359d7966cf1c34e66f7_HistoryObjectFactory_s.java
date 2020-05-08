 package net.enkun.javatter.history;
 
 import java.awt.BorderLayout;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.JToggleButton;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 import com.orekyuu.javatter.util.BackGroundColor;
 import com.orekyuu.javatter.util.IconCache;
 
 import twitter4j.Status;
 import twitter4j.User;
 
 public class HistoryObjectFactory {
 	/*
 	 * retweet, favorite, unfavorite => user, status
 	 * 
 	 * follow => user
 	 * 
 	 * ほいさほいさ
 	 */
 	
 	private User user;
 	private Status status;
 	private EventType type;
 	
 	public enum EventType {
 		Retweet,
 		Favorite,
 		Unfavorite,
 		Follow
 	}
 	
 	public HistoryObjectFactory(User user, Status status, EventType type) {
 		this.user = user;
 		this.status = status;
 		this.type = type;
 	}
 	
 	public HistoryObjectFactory(User user) {
 		this.user = user;
 		this.status = null;
 	}
 	
 	public JPanel createHistoryObject(HistoryViewObserver view) {
 		JPanel base = new JPanel();
 		base.setBackground(BackGroundColor.color);
 		base.setAlignmentX(0.0F);
 		base.setAlignmentY(0.0F);
 		base.setMaximumSize(new Dimension(375, Integer.MAX_VALUE));
 		base.setLayout(new BorderLayout());
 		
 		base.add(createImage(), "Before");
 
 		base.add(createText(view), "Center");
 		
 		return base;
 	}
 	
 	private JPanel createImage() {
 		IconCache cache = IconCache.getInstance();
 		
 		JPanel panel = new JPanel();
 		panel.setBackground(BackGroundColor.color);
 		panel.setLayout(new BoxLayout(panel, 3));
 		
 		try {
 			URL url = new URL(this.user.getProfileImageURL());
 			panel.add(new JLabel(cache.getIcon(url)));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		panel.setAlignmentX(0.0F);
 		panel.setAlignmentY(0.0F);
 		
 		return panel;
 	}
 	
 	private JPanel createText(HistoryViewObserver view) {
 		JPanel textPanel = new JPanel();
 		textPanel.setMaximumSize(new Dimension(375, Integer.MAX_VALUE));
 		textPanel.setLayout(new BoxLayout(textPanel, 3));
 		
 		JLabel userName = new JLabel();
 		userName.setMaximumSize(new Dimension(375, Integer.MAX_VALUE));
 		Font font = new Font("ＭＳ ゴシック", 1, 13);
 		userName.setFont(font);
 		
 		
 		userName.setText("@" + user.getScreenName() + "に" + type + "されました");
 		textPanel.add(userName);
 		
		String tweet = this.status.getText();
 		JTextPane textArea = new JTextPane();
 		textArea.setContentType("text/html");
 		textArea.setEditable(false);
 		textArea.setText(createHTMLText(tweet));
 		textArea.setBackground(BackGroundColor.color);
 		textArea.setAlignmentX(0.0F);
 		textArea.setAlignmentY(0.0F);
 		textArea.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent e) {
 				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 					URL url = e.getURL();
 					Desktop dp = Desktop.getDesktop();
 					try {
 						dp.browse(url.toURI());
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					} catch (URISyntaxException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 		textPanel.add(textArea);
 		
 		textPanel.add(createButtons(view));
 		
 		textPanel.setAlignmentX(0.0F);
 		textPanel.setAlignmentY(0.0F);
 		textPanel.setBackground(BackGroundColor.color);
 		
 		return textPanel;
 	}
 	
 	private String createHTMLText(String tweet) {
 		final String urlRegex = "(?<![\\w])https?://(([\\w]|[^ -~])+(([\\w\\-]|[^ -~])+([\\w]|[^ -~]))?\\.)+(aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|ss|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|za|zm|zw)(?![\\w])(/([\\w\\.\\-\\$&%/:=#~!]*\\??[\\w\\.\\-\\$&%/:=#~!]*[\\w\\-\\$/#])?)?";
 		Pattern p = Pattern.compile(urlRegex);
 		Matcher m = p.matcher(urlRegex);
 		String t = tweet;
 		while (m.find()) {
 			String s = m.group();
 			t = t.replaceFirst(s, "<a href='" + s + "'>" + s + "</a>");
 		}
 		t = t.replaceAll("\n", "<br>");
 		
 		return t;
 	}
 	
 	private JPanel createButtons(HistoryViewObserver view) {
 		//ButtonClickEventListener model = new ButtonClickEventListener(this.user, this.status, view);
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, 2));
 		
 		/*
 		JButton rep = new JButton("リプ");
 		rep.addActionListener(model);
 		model.setHogeButton(rep);
 		panel.add(rep);
 		*/
 		/*
 		JToggleButton rt = new JToggleButton("RT");
 		rt.addActionListener(model);
 		model.setRtButton(rt);
 		rt.setEnabled(!s.isRetweetedByMe());
 		panel.add(rt);
 
 		JToggleButton fav = new JToggleButton("☆");
 		fav.addActionListener(model);
 		fav.setSelected(s.isFavorited());
 		model.setFavButton(fav);
 		panel.add(fav);
 		 */
 		
 		panel.setAlignmentX(0.0F);
 		panel.setAlignmentY(0.0F);
 		
 		return panel;
 	}
 }
