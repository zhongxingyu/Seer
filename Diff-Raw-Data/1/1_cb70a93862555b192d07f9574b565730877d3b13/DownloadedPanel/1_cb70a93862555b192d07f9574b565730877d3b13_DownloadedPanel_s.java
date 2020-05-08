 package org.xindle;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.kwt.ui.KWTSelectableLabel;
 
 import com.amazon.kindle.kindlet.event.KindleKeyCodes;
 import com.amazon.kindle.kindlet.ui.KImage;
 import com.amazon.kindle.kindlet.ui.KLabel;
 import com.amazon.kindle.kindlet.ui.KOptionPane;
 import com.amazon.kindle.kindlet.ui.KPanel;
 
 public class DownloadedPanel extends AbstractKPanel {
 	private UIRoot root;
 	private KPanel resultPanel;
 
 	private KPanel picturePanel;
 	private KPanel papersPanel;
 	boolean isPictureShown = false;
 	int pageIndex = 0;
 	Paper paper = null;
 	Logger logger = Logger.getLogger(DownloadedPanel.class);
 	private KeyEventDispatcher eventDispatcher = new KeyEventDispatcher() {
 		public boolean dispatchKeyEvent(KeyEvent evt) {
 			logger.info("Key Pressed.");
 			if (isPictureShown) {
 				if (evt.getKeyCode() == KindleKeyCodes.VK_TURN_PAGE_BACK) {
 					// prev page!
 					if (pageIndex > 1) {
 						pageIndex--;
 						loadPicture(pageIndex);
 						return true;
 					}
 				}
 				if (evt.getKeyCode() == KindleKeyCodes.VK_LEFT_HAND_SIDE_TURN_PAGE || evt.getKeyCode() == KindleKeyCodes.VK_RIGHT_HAND_SIDE_TURN_PAGE) {
 					// next page!
 					if (pageIndex < paper.getPages().length - 1) {
 						pageIndex++;
 						loadPicture(pageIndex);
 					}
 				}
 			}
 			return false;
 		}
 	};
 
 	public DownloadedPanel(UIRoot root) {
 		this.root = root;
 		this.resultPanel = new KPanel();
 		this.papersPanel = new KPanel();
 		this.picturePanel = new KPanel();
 
 		papersPanel.setLayout(new BorderLayout());
 		papersPanel.add(new KLabel("Downloaded"), BorderLayout.NORTH);
 		papersPanel.add(resultPanel, BorderLayout.CENTER);
 		resultPanel.setLayout(new GridLayout(20, 1));
 
 		root.downloadedPanel = this;
 	}
 
 	public void loadPicture(int index) {
 		List bytes = new ArrayList();
 		byte[] ary = new byte[1024 * 1024];
 		int curindex = 0;
 		try {
 			InputStream is = new BufferedInputStream(new FileInputStream(
 					paper.getPages()[index]));
 			while (is.available() > 0) {
 				int result = is.read(ary, curindex, 1024 * 1024 - curindex);
 				curindex += result;
 			}
 		} catch (IOException e) {
 
 		}
 		Image image = Toolkit.getDefaultToolkit().createImage(ary, 0, curindex);
 		picturePanel.removeAll();
 		picturePanel.add(new KImage(image));
 		picturePanel.requestFocus();
 		repaint();
 	}
 
 	public Runnable onStart() {
 		return new Runnable() {
 			public void run() {
 				removeAll();
 				add(papersPanel);
 				paper = null;
 				pageIndex = -1;
 				isPictureShown = false;
 
 				KeyboardFocusManager.getCurrentKeyboardFocusManager()
 						.addKeyEventDispatcher(eventDispatcher);
 				root.rootContainer.repaint();
 				root.currentPanel.requestFocus();
 
 				Util util = new Util(root.context);
 				List papers = util.getAllPapers();
 				for (int i = 0; i < papers.size(); i++) {
 					Paper paper = (Paper) papers.get(i);
 					String paperName = paper.getName();
 					KWTSelectableLabel selectible = new KWTSelectableLabel(
 							paperName);
 					class OpenPaperActionListener implements ActionListener {
 						public OpenPaperActionListener(Paper paper) {
 							this.paper = paper;
 						}
 
 						final Paper paper;
 
 						public void actionPerformed(ActionEvent arg0) {
 							removeAll();
 							add(picturePanel);
 							DownloadedPanel.this.paper = paper;
 							DownloadedPanel.this.pageIndex = 1;
 							DownloadedPanel.this.isPictureShown = true;
 							loadPicture(1);
 						}
 					}
 					selectible.addActionListener(new OpenPaperActionListener(
 							paper));
 					if (i == 0) {
 						// select the first entry.
 						selectible.requestFocus();
 					}
 					resultPanel.add(selectible);
 				}
 			}
 		};
 	}
 
 	public Runnable onStop() {
 		return new Runnable() {
 			public void run() {
 				KeyboardFocusManager.getCurrentKeyboardFocusManager()
 						.removeKeyEventDispatcher(eventDispatcher);
 			}
 		};
 	}
 }
