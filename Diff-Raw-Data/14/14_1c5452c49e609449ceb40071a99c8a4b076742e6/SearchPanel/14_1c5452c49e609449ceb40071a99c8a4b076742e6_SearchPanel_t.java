 package org.xindle;
 
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.log4j.Logger;
 import org.kwt.ui.KWTSelectableLabel;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.amazon.kindle.kindlet.KindletContext;
 import com.amazon.kindle.kindlet.event.KindleKeyCodes;
 import com.amazon.kindle.kindlet.net.ConnectivityHandler;
 import com.amazon.kindle.kindlet.net.NetworkDisabledDetails;
 import com.amazon.kindle.kindlet.ui.KButton;
 import com.amazon.kindle.kindlet.ui.KLabel;
 import com.amazon.kindle.kindlet.ui.KPanel;
 import com.amazon.kindle.kindlet.ui.KTextField;
 import com.amazon.kindle.kindlet.ui.KindletUIResources;
 import com.amazon.kindle.kindlet.ui.KindletUIResources.KFontFamilyName;
 import com.amazon.kindle.kindlet.ui.border.KLineBorder;
 
 public class SearchPanel extends AbstractKPanel {
 	KTextField searchField;
 	KindletUIResources res = KindletUIResources.getInstance();
 	KPanel resultPanel = new KPanel();
 	private UIRoot root;
 	private String currentSearch;
 	private int currentPage = 0;
 	private int totalPages = 0;
 	public static final int DEFAULT_SEARCH_SIZE = 10;
 
 	Logger logger = Logger.getLogger(SearchPanel.class);
 
 	KeyEventDispatcher eventDispatcher = new KeyEventDispatcher() {
 		public boolean dispatchKeyEvent(KeyEvent evt) {
 			if ((evt.getKeyCode() == KindleKeyCodes.VK_RIGHT_HAND_SIDE_TURN_PAGE
 					|| evt.getKeyCode() == KindleKeyCodes.VK_LEFT_HAND_SIDE_TURN_PAGE) && currentPage < totalPages) {
 				// pagination of search results
 				currentPage++;
 				search(currentSearch);
 				return true;
 			}
 			if (evt.getKeyCode() == KindleKeyCodes.VK_TURN_PAGE_BACK && currentPage > 0) {
 				// pagination of search results
 				currentPage--;
 				search(currentSearch);
 				return true;
 			}
 			return false;
 		}
 	};
 
 	public SearchPanel(UIRoot root) {
 		this.root = root;
 		searchField = new KTextField(20);
 		final KButton searchBtn = new KButton("Search");
 		final KLabel label = new KLabel("Search:");
 
 		searchField.setBorder(new KLineBorder(1, true));
 		label.setFont(res.getFont(KFontFamilyName.MONOSPACE, 30));
 		resultPanel.setLayout(new GridLayout(DEFAULT_SEARCH_SIZE * 2, 1));
 
 		setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.anchor = GridBagConstraints.NORTH;
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.weighty = 0;
 		gbc.weightx = 0;
 		gbc.insets = new Insets(10, 10, 10, 10);
 		add(label, gbc);
 		gbc.gridx = 1;
 		gbc.ipadx = 300;
 		add(searchField, gbc);
 		gbc.gridx = 2;
 		add(searchBtn, gbc);
 		// setup search panel.
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridy = 1;
 		gbc.gridx = 0;
 		gbc.gridwidth = 0;
 		gbc.weighty = 1.0;
 		gbc.weightx = 3;
 		gbc.ipady = 300;
 		gbc.insets = new Insets(25, 25, 25, 25);
 		add(resultPanel, gbc);
 
 		searchField.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				String searchTerm = searchField.getText();
 				searchField.setText("");
 				currentPage = 0;
 				currentSearch = searchTerm;
 				search(searchTerm);
 				searchField.repaint();
 				resultPanel.repaint();
 			}
 		});
 		root.searchPanel = this;
 	}
 
 	public void search(String term) {
 		logger.info("Searcing for the term:" + term);
 		try {
 			KindletContext context = root.context;
 			context.getProgressIndicator().setString("Connecting");
 			new SearchHandler(term).connected();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	static String generateUrl(String searchTerm, int startIndex, int resultSize) {
 		String urlstr = "http://export.arxiv.org/api/query?search_query="
 				+ searchTerm + "&start=" + startIndex + "&max_results="
 				+ resultSize;
 		return urlstr;
 	}
 
 	public List searchUrl(String url) {
 		List results = new ArrayList();
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder parser = factory.newDocumentBuilder();
 			Document document = parser.parse(url);
 			NodeList items = document.getChildNodes().item(0).getChildNodes();
 
 			for (int i = 0; i < items.getLength(); i++) {
 				Node item = items.item(i);
 				if(item.getNodeName().equals("opensearch:totalResults")){
 					totalPages =  Integer.parseInt(item.getTextContent()) / DEFAULT_SEARCH_SIZE;
 					logger.info(""+totalPages);
 				}
 				if (item.getNodeName().equals("entry")) {
 					NodeList childrens = item.getChildNodes();
 					String id = "";
 					String title = "";
 					String summary = "";
 					for (int j = 0; j < childrens.getLength(); j++) {
 						Node childItem = childrens.item(j);
 						if (childItem.getNodeName().equals("title")) {
 							title = childItem.getTextContent();
 						} else if (childItem.getNodeName().equals("summary")) {
 							summary = childItem.getTextContent();
 						} else if (childItem.getNodeName().equals("id")) {
 							id = childItem.getTextContent()
 									.substring(
 											childItem.getTextContent()
 													.lastIndexOf('/') + 1);
 							if (id.endsWith("v1") || id.endsWith("v2")) {
 								id = id.substring(0, id.length() - 2);
 							}
 						}
 					}
 					results.add(new Result(title, summary, id));
 				}
 			}
 			logger.info(results);
 			return results;
 		} catch (Exception e) {
 			logger.error("Error while searchUrl()");
 		}
 		return results;
 	}
 
 	/** Add a search result to display. */
 	public void addResult(final Result result, boolean focus) {
 		KWTSelectableLabel selectable = new KWTSelectableLabel(result.title);
 		selectable.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				// when the item is selected.
 				root.setCurrentPanel(root.moreInfoPanel);
 				root.moreInfoPanel.setResult(result);
 			}
 		});
 		resultPanel.add(selectable);
         if (focus) { selectable.requestFocus(); }
 		resultPanel.add(new KLabel(result.summary));
 	}
 
 	class SearchHandler implements ConnectivityHandler {
 		private String term;
 
 		public SearchHandler(String term) {
 			this.term = term;
 		}
 
 		public void connected() throws InterruptedException {
 			KindletContext context = root.context;
 			String url = generateUrl(term, currentPage * DEFAULT_SEARCH_SIZE,
 					DEFAULT_SEARCH_SIZE);
 			context.getProgressIndicator().setString("");
 			final List results = searchUrl(url);
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					resultPanel.removeAll();
 					for (int i = 0; i < results.size(); i++) {
 						Result item = (Result) results.get(i);
 						addResult(item, i == 0);
 					}
 					repaint();
 				}
 			});
 		}
 
 		public void disabled(NetworkDisabledDetails detail)
 				throws InterruptedException {
 			// do nothing.
 		}
 	}
 
 	public Runnable onStart() {
 		return new Runnable() {
 			public void run() {
 				searchField.requestFocus();
 				// addResult(new Result("Foo", "Bar"));
 				//remove pagination listener
 				KeyboardFocusManager.getCurrentKeyboardFocusManager()
 						.addKeyEventDispatcher(eventDispatcher);
 				root.rootContainer.repaint();
 			}
 		};
 	}
 	public Runnable onStop(){
 		return new Runnable(){
 			public void run(){
 				//remove pagination listener
                 KeyboardFocusManager.getCurrentKeyboardFocusManager()
                         .removeKeyEventDispatcher(eventDispatcher);
 			}
 		};
 	}
 }
