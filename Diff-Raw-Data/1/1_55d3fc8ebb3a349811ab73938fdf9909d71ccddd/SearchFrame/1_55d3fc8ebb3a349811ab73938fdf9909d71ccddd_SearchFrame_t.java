 /*
  * Copyright 2013 Tim Roes <mail@timroes.de>.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.timroes.startplz.ui;
 
 import de.timroes.startplz.PluginManager;
 import de.timroes.startplz.Result;
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import javax.swing.AbstractListModel;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListModel;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.plaf.basic.BasicScrollBarUI;
 
 /**
  *
  * @author Tim Roes <mail@timroes.de>
  */
 public final class SearchFrame extends javax.swing.JFrame {
 
 	private final static String DEFAULT_SEARCH_HINT = "Type to search...";
 	
 	private final static Color SEARCH_FOREGROUND_COLOR = Color.BLACK;
 	private final static Color SEARCH_HINT_COLOR = Color.LIGHT_GRAY;
 	private final static Color HIGHLIGHT_COLOR = new Color(51, 181, 229);
 	private final static Color HIGHLIGHT_COLOR_LIGHT = new Color(51, 181, 229, 70);
 	
 	private boolean hasTyped;
 	
 	private ResultListModel resultListModel = new ResultListModel();
 	private PluginManager pluginManager = PluginManager.get();
 	private DocumentListener searchBoxListener = new DocumentListener() {
 
 		@Override
 		public void insertUpdate(DocumentEvent de) {
 			startSearch();
 		}
 
 		@Override
 		public void removeUpdate(DocumentEvent de) {
 			// Ignore the clearing of the text field, when user starts interacting
 			// with it.
 			if(hasTyped) {
 				startSearch();
 			}
 		}
 
 		@Override
 		public void changedUpdate(DocumentEvent de) {
 			startSearch();
 		}
 	
 	};
 	
 	/**
 	 * Creates new form SearchFrame
 	 */
 	public SearchFrame() {
 		setUndecorated(true);
 		initComponents();
 		
 		// Set custom scrollbar UI
 		jScrollPane1.getVerticalScrollBar().setUI(new ResultScrollBarUI());
 		
 		// Remove icon from window
 		setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE));
 	
 		// Set background color
 		getContentPane().setBackground(Color.WHITE);
 		
 		// Register keylsitener for hotkeys
 		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		manager.addKeyEventDispatcher(new CustomKeyDispatcher());
 		
 		// Register change listener on search field
 		searchField.getDocument().addDocumentListener(searchBoxListener);
 		
 		// Never let the search field unfocus
 		searchField.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent fe) { }
 
 			@Override
 			public void focusLost(FocusEvent fe) {
 				searchField.requestFocus();
 			}
 			
 		});
 		
 		// Set cell renderer for result list
 		resultList.setPrototypeCellValue(null);
 		resultList.setCellRenderer(new ResultCellRenderer());
 		
 		// Register a listener to close the window on focus changes
 		addWindowFocusListener(new FocusLostListener());
 	}
 	
 	/**
 	 * Do everything needed to pretend this is a fresh instance.
 	 * Clears the input, results, and center the window again.
 	 */
 	public void clearInstance() {
 		
 		// Center and resize screen
 		setSize(480, 380);
 		setLocationRelativeTo(null);
 		
 		// Reset search field
 		// Remove listener, so the clear won't trigger a search
 		searchField.getDocument().removeDocumentListener(searchBoxListener);
 		searchField.setText(DEFAULT_SEARCH_HINT);
 		hasTyped = false;
 		searchField.setForeground(SEARCH_HINT_COLOR);
 		searchField.getDocument().addDocumentListener(searchBoxListener);
 		
 		// Reset result list
 		resultListModel.clear();
 		((CardLayout)resultPanel.getLayout()).show(resultPanel, "NO_RESULTS");
 		
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 	@SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         searchField = new javax.swing.JTextField();
         resultPanel = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         resultList = new javax.swing.JList();
 
         setTitle("start-plz");
 
         searchField.setFont(searchField.getFont().deriveFont(searchField.getFont().getStyle() | java.awt.Font.BOLD, searchField.getFont().getSize()+5));
         searchField.setBorder(BorderFactory.createCompoundBorder(searchField.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
         searchField.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 searchFieldMousePressed(evt);
             }
         });
         searchField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 searchFieldKeyTyped(evt);
             }
         });
 
         resultPanel.setBackground(java.awt.Color.white);
         resultPanel.setLayout(new java.awt.CardLayout());
 
         jLabel1.setBackground(java.awt.Color.white);
         jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD, jLabel1.getFont().getSize()+10));
         jLabel1.setForeground(new java.awt.Color(130, 130, 130));
         jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jLabel1.setText("No results found.");
         jLabel1.setOpaque(true);
         resultPanel.add(jLabel1, "NO_RESULTS");
 
         jScrollPane1.setBackground(java.awt.Color.white);
         jScrollPane1.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
         jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jScrollPane1.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jScrollPane1.getViewport().setBackground(Color.white);
 
         resultList.setBackground(java.awt.Color.white);
         resultList.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
         resultList.setModel(resultListModel);
         resultList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         resultList.setOpaque(false);
         jScrollPane1.setViewportView(resultList);
 
         resultPanel.add(jScrollPane1, "RESULT_LIST");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                     .addComponent(searchField))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void searchFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyTyped
 		clearFieldOnInteraction();
     }//GEN-LAST:event_searchFieldKeyTyped
 
     private void searchFieldMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchFieldMousePressed
 		clearFieldOnInteraction();
     }//GEN-LAST:event_searchFieldMousePressed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel jLabel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JList resultList;
     private javax.swing.JPanel resultPanel;
     private javax.swing.JTextField searchField;
     // End of variables declaration//GEN-END:variables
 
 	/**
 	 * Clears the text field if the user interacts with it, and the default hint
 	 * is still in the textfield.
 	 */
 	private void clearFieldOnInteraction() {
 		if(!hasTyped) {
 			searchField.setText("");
 			searchField.setForeground(SEARCH_FOREGROUND_COLOR);
 			hasTyped = true;
 		}
 	}
 	
 	/**
 	 * Closes the window either because the users requested it, or because he 
 	 * finished a command.
 	 */
 	private void closeWindow() {
 		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
 	}
 	
 	/**
 	 * Initiates a search for the current query in the search field.
 	 */
 	private void startSearch() {
 		// Get search results
 		List<Result> results = pluginManager.search(searchField.getText().trim());
 
 		// Show either no search results or result list
 		CardLayout cl = (CardLayout)(resultPanel.getLayout());
 		if(results.isEmpty() || searchField.getText().trim().isEmpty()) {
 			cl.show(resultPanel, "NO_RESULTS");
 		} else {
 			cl.show(resultPanel, "RESULT_LIST");
 		}
 		
 		// Apply new results
 		resultListModel.setResults(results);
 		
 		// Select first element and scroll to it.
 		resultList.setSelectedIndex(0);
 		resultList.ensureIndexIsVisible(0);		
 	}
 	
 	/**
 	 * Changes the selection in the result list for a given delta of elements.
 	 * 
 	 * @param delta The delta of elements to jump. Positive means down, negative upwards.
 	 */
 	private void changeSelection(int delta) {
 		int index = resultList.getSelectedIndex();
 		index += delta;
 		index = Math.max(0, Math.min(resultList.getModel().getSize() - 1, index));
 		resultList.setSelectedIndex(index);
 		resultList.ensureIndexIsVisible(index);
 	}
 	
 	private class CustomKeyDispatcher implements KeyEventDispatcher {
 
 		@Override
 		public boolean dispatchKeyEvent(KeyEvent ke) {
 
 			if(ke.getID() == KeyEvent.KEY_RELEASED) {
 				
 				switch(ke.getKeyCode()) {
 					case KeyEvent.VK_DOWN:
 						changeSelection(1);
 						return true;
 					case KeyEvent.VK_UP:
 						changeSelection(-1);
 						return true;
 					case KeyEvent.VK_PAGE_DOWN:
 						changeSelection(6);
 						return true;
 					case KeyEvent.VK_PAGE_UP:
 						changeSelection(-6);
 						return true;
 					case KeyEvent.VK_ESCAPE:
 						closeWindow();
 						return true;
 					case KeyEvent.VK_ENTER:
 						Object selection = resultList.getSelectedValue();
 						if(selection != null) {
 							((Result)selection).execute();
 							closeWindow();
 						}
 						return true;
 				}
 				
 			}
 			
 			return false;
 		}
 		
 	}
 	
 	/**
 	 * The {@link ListCellRenderer} used to display results in the list.
 	 */
 	private class ResultCellRenderer extends DefaultListCellRenderer {
 
 		final SelectorPanel panel = new SelectorPanel(new BorderLayout(5, 0));
 		final JPanel iconPanel = new JPanel(new BorderLayout());
 		final JPanel textPanel = new JPanel(new BorderLayout(0, 1));
 		final JLabel title = new JLabel();
 		final JLabel subtitle = new JLabel();
 		final JLabel icon = new JLabel();
 		
 		public ResultCellRenderer() {
 			
 			// Set fonts of text fieldsnch CMD.EXE, grab stdin/stdout and push to stdin command to b
 			title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize() + 5));
 			subtitle.setForeground(Color.GRAY);
 			
 			iconPanel.setOpaque(false);
 			textPanel.setOpaque(false);
 			
 			panel.setBackground(new Color(235, 235, 235));
 			icon.setPreferredSize(new Dimension(32, 32));
 			
 			textPanel.add(title, BorderLayout.NORTH);
 			textPanel.add(subtitle, BorderLayout.SOUTH);
 			panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
 			panel.add(icon, BorderLayout.WEST);
 			panel.add(textPanel, BorderLayout.CENTER);
 			
 		}
 
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value, 
 				int index, boolean isSelected, boolean hasFocus) {
 			
 			Result res = (Result)value;
 			
 			// Set title
 			title.setText(res.getTitle());
 			
 			// Set subtitle
 			String subtext = res.getSubtitle();
 			if(subtext != null) {
 				subtitle.setText(subtext);
 			} else {
 				subtitle.setText(" ");
 			}
 			
 			// Set icon
 			ImageIcon image = res.getIcon();
 			// Scale down icon to 32x32 if it's too large.
 			if(image != null && (image.getIconHeight() > 32 || image.getIconWidth() > 32)) {
 				image.setImage(image.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
 			}
 			icon.setIcon(image);
 			
 			panel.setSelected(isSelected);
 			
 			return panel;
 		}
 		
 	}
 	
 	/**
 	 * A {@link WindowFocusListener} that closes the window, when the focus is lost.
 	 */
 	private class FocusLostListener implements WindowFocusListener {
 
 		@Override
 		public void windowGainedFocus(WindowEvent we) {	}
 
 		@Override
 		public void windowLostFocus(WindowEvent we) {
 			closeWindow();
 		}
 		
 	}
 	
 	/**
 	 * A custom {@link JPanel} implementation, that draws its background, when 
 	 * selected and a pair of custom markers around it.
 	 */
 	private class SelectorPanel extends JPanel {
 		
 		private boolean isSelected;
 		
 		private final static int SELECTOR_WIDTH = 2;
 		private final static int SELECTOR_LENGTH_LONG = 40;
 		private final static int SELECTOR_LENGTH_SHORT = 15;
 
 		public SelectorPanel(LayoutManager lm) {
 			super(lm);
 		}
 
 		@Override
 		protected void paintComponent(Graphics grphcs) {
 			// If this panel is selected draw background und selectors.
 			if(isSelected) {
 				// Let JPanel draw background
 				super.paintComponent(grphcs);
 				// Get size of panel
 				Dimension size = getSize();
 				int height = size.height;
 				int width = size.width;
 				
 				Graphics2D canvas = (Graphics2D)grphcs;
 				canvas.setColor(HIGHLIGHT_COLOR);
 				// Draw left selector
 				canvas.fillRect(0, 0, SELECTOR_LENGTH_LONG, SELECTOR_WIDTH);
 				canvas.fillRect(0, 0, SELECTOR_WIDTH, height);
 				canvas.fillRect(SELECTOR_WIDTH, height - SELECTOR_WIDTH, SELECTOR_LENGTH_SHORT, height);
 				// Draw right selector
 				canvas.fillRect(width - SELECTOR_LENGTH_SHORT, 0, width, SELECTOR_WIDTH);
 				canvas.fillRect(width - SELECTOR_WIDTH, SELECTOR_WIDTH, width, width);
 				canvas.fillRect(width - SELECTOR_LENGTH_LONG, height - SELECTOR_WIDTH, width - SELECTOR_WIDTH, height);
 			}
 		}
 
 		@Override
 		public Dimension getPreferredSize() {
 			// TODO: Improve this somehow..
 			Dimension d = super.getPreferredSize();
 			if(d.width > jScrollPane1.getViewport().getWidth()) {
 				d.width = jScrollPane1.getViewport().getWidth();
 			}
 			return d;
 		}
 		
 		public void setSelected(boolean selected) {
 			isSelected = selected;
 		}
 		
 	}
 	
 	/**
 	 * The {@link ListModel} used to show the results.
 	 */
 	private class ResultListModel extends AbstractListModel {
 
 		private List<Result> results;
 		
 		public void clear() {
 			results = null;
 			fireContentsChanged(this, 0, 0);
 		}
 		
 		public void setResults(List<Result> results) {
 			this.results = results;
 			fireContentsChanged(this, 0, results.size() - 1);
 		}
 		
 		@Override
 		public int getSize() {
 			return (results != null) ? results.size() : 0;
 		}
 
 		@Override
 		public Object getElementAt(int i) {
 			return (results != null) ? results.get(i) : null;
 		}
 		
 	}
 	
 	private class ResultScrollBarUI extends BasicScrollBarUI {
 
 		private final JButton noButton = new JButton();
 		
 		public ResultScrollBarUI() {
 			noButton.setPreferredSize(new Dimension(0,0));
 		}
 		
 		/**
 		 * Disable up button.
 		 */
 		@Override
 		protected JButton createDecreaseButton(int i) {
 			return noButton;
 		}
 
 		/**
 		 * Disable down button.
 		 */
 		@Override
 		protected JButton createIncreaseButton(int i) {
 			return noButton;
 		}
 
 		/**
 		 * Draw white background (invisible).
 		 */
 		@Override
 		protected void paintTrack(Graphics grphcs, JComponent jc, Rectangle r) {
 			grphcs.setColor(Color.WHITE);
 			grphcs.fillRect(r.x, r.y, r.width, r.height);
 		}
 
 		/**
 		 * Draw small rounded rectangle shape in highlight color.
 		 */
 		@Override
 		protected void paintThumb(Graphics grphcs, JComponent jc, Rectangle rctngl) {
 			grphcs.setColor(HIGHLIGHT_COLOR_LIGHT);
 			// Enable antialiasing
 			((Graphics2D)grphcs).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 			grphcs.fillRoundRect(rctngl.x + 5, rctngl.y, rctngl.width - 5, rctngl.height, 5, 5);
 		}
 		
 	}
 
 }
