 package org.xindle;
 
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.kwt.ui.KWTSelectableLabel;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.amazon.kindle.kindlet.ui.KButton;
 import com.amazon.kindle.kindlet.ui.KLabel;
 import com.amazon.kindle.kindlet.ui.KPanel;
 import com.amazon.kindle.kindlet.ui.KTextArea;
 import com.amazon.kindle.kindlet.ui.KTextField;
 import com.amazon.kindle.kindlet.ui.KindletUIResources;
 import com.amazon.kindle.kindlet.ui.KindletUIResources.KFontFamilyName;
 import com.amazon.kindle.kindlet.ui.border.KLineBorder;
 
 public class HomePanel extends AbstractKPanel {
 	KindletUIResources res = KindletUIResources.getInstance();
 	final KLabel label = new KLabel("Newest Papers: (loading...)");
 	private UIRoot root;
 
 	public HomePanel(UIRoot root) {
 		this.root = root;
 		final KButton get_btn = new KButton("Get Papers");
 		final KButton browse_btn = new KButton("My Papers");
 
 		// download and parse the feed
 		final KTextArea new_abstracts[] = new KTextArea[3];
 		final KWTSelectableLabel new_titles[] = new KWTSelectableLabel[3];
 		final String str_titles[] = new String[3];
 		final String str_abstracts[] = new String[3];
 
 		get_btn.setFont(res.getFont(KFontFamilyName.MONOSPACE, 30));
 		browse_btn.setFont(res.getFont(KFontFamilyName.MONOSPACE, 30));
 
 		label.setFont(res.getFont(KFontFamilyName.SANS_SERIF, 25));
 
 		Runnable run = new Runnable() {
 			public void run() {
 				try {
 					DocumentBuilderFactory factory = DocumentBuilderFactory
 							.newInstance();
 					DocumentBuilder parser = factory.newDocumentBuilder();
 					Document document = parser
 							.parse("http://export.arxiv.org/rss/math");
 					NodeList items = document.getChildNodes().item(0)
 							.getChildNodes();
 					int item_num = 0;
 					for (int n = 0; n < items.getLength() && item_num < 3; n++) {
 						Node item = items.item(n);
 						if (item.getNodeName() == "item") {
 							NodeList data = item.getChildNodes();
 							for (int m = 0; m < data.getLength(); m++) {
 								Node each_data = data.item(m);
 								if (each_data.getNodeName() == "title") {
 									str_titles[item_num] = each_data
 											.getTextContent();
 								}
 								if (each_data.getNodeName() == "description") {
 									str_abstracts[item_num] = each_data
 											.getTextContent()
 											.substring(3)
 											.substring(
 													0,
 													each_data.getTextContent()
 															.length() - 8).replace('\n', ' ');
 								}
 							}
 							item_num++;
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				EventQueue.invokeLater(new Runnable() {
 					public void run() {
 						// build new stuff list
 						GridBagConstraints gbc = new GridBagConstraints();
 						gbc.gridy = 2;
 						gbc.gridx = 0;
 						gbc.gridwidth = 2;
 						gbc.weightx = 1;
 						gbc.insets = new Insets(0, 0, 0, 0);
 						for (int n = 0; n < 3; n++) {
 							// trim abstracts
							if (str_abstracts[n] != null && str_abstracts[n].length() > 250) {
 								str_abstracts[n] = str_abstracts[n].substring(
 										0, 250) + "...";
 							}
 							// build ui elements
 							new_abstracts[n] = new KTextArea(str_abstracts[n]);
 							new_abstracts[n].setEditable(false);
 							new_abstracts[n].setEnabled(false);
 							new_titles[n] = new KWTSelectableLabel(
 									str_titles[n]);
 							gbc.gridy++;
 							add(new_titles[n], gbc);
 							gbc.gridy++;
 							add(new_abstracts[n], gbc);
 						}
 						label.setText("Newest Papers:");
 						repaint();
 					}
 				});
 
 			}
 		};
 		Thread t = new Thread(run);
 
 		setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.anchor = GridBagConstraints.NORTH;
 		gbc.gridy = 0;
 		gbc.weightx = 0.5;
 
 		gbc.gridx = 0;
 		gbc.insets = new Insets(30, 30, 30, 30);
 		add(browse_btn, gbc);
 
 		gbc.gridx = 1;
 		add(get_btn, gbc);
 
 		gbc.insets = new Insets(0, 0, 0, 0);
 		gbc.weightx = 1;
 		gbc.gridx = 0;
 		gbc.gridy = 1;
 		gbc.gridwidth = 2;
 		add(label, gbc);
 		
 		t.start();
 		root.homePanel = this;
 	}
 
 	public Runnable onStart() {
 		return new Runnable() {
 			public void run() {
 			}
 		};
 	}
 }
