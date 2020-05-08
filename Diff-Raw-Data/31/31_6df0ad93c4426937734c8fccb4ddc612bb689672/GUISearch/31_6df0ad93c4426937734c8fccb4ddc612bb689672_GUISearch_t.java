 //In this package
 package View;
 
 //Import what this class use
 import Model.SearchHandler;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 
 public class GUISearch extends JFrame
 {/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private Container contentPane;
 	private JTextField searchField;
 	private JList<String> resultList;
 	private SearchHandler SearchH;
 	
 	public class ButtonListener implements ActionListener
 	{
 
 		public void actionPerformed(ActionEvent e) // Executes when button pressed
 		{
 			String buttonText = e.getActionCommand();
 			
			if(buttonText.equals("Search Customer"))
 			{
				searchCustomer();
 			}
 			else if(buttonText.equals("Search Item"))
 			{
 				searchItem();
 			}
 			else if(buttonText.equals("Clear"))
 			{
 				clearSearchField();
 			}
 		}
 	}
 
 		private void clearSearchField() 
 		{
 		    // Clears the search fields
 		    this.searchField.setText("");
 			
 		}
 
 		private void searchItem() 
 		{
 			if(this.searchField != null)
 			{
 				String searchString = this.searchField.getText();
 				
 				if(this.SearchH.SearchItem(searchString) != null)
 				{
 					this.resultList.setListData(this.SearchH.SearchItem(searchString));
 				}
 				else
 				{
 					this.resultList.clearSelection();
 					JOptionPane.showMessageDialog(null, "No results were found");
 				}
 			}
 			
 			else
 			{
 				JOptionPane.showMessageDialog(null, "Please enter something to search for");
 			}
 			
 		}
 
		private void searchCustomer()
 		{
 			if(this.searchField != null)
 			{
 				String searchString = this.searchField.getText();
 				
				String[] result = this.SearchH.SearchCustomer(searchString);
				
				if(result != null)
 				{
					this.resultList.setListData(result);
 				}
 				else
 				{
 					this.resultList.clearSelection();
 					JOptionPane.showMessageDialog(null, "No results were found");
 				}
 			}
 			
 			else
 			{
 				JOptionPane.showMessageDialog(null, "Please enter something to search for");
 			}
 			
 		}
 		
 		public GUISearch(SearchHandler SearchHandler) 
 		{
 			super();
 			this.SearchH = SearchHandler;
 			initiateInstanceVariables();
 			configureFrame();
 			buildLeftPanel();
 			addListToTheRight();
 			
 		}
 		
 		private void initiateInstanceVariables()
 		{
 			this.contentPane = this.getContentPane();
 			this.contentPane.setLayout(new GridLayout(1, 2));
 			this.searchField = new JTextField();
 			this.resultList = new JList<String>();
 		}
 		
 		private void configureFrame()
 		{
 			this.setSize(1000, 400);
 			this.setTitle("Search Handler :: Video Rental System 1.0");
 			this.setLocationRelativeTo(null);
 			
 			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		}
 	
 		private void buildLeftPanel()
 		{
 			JPanel panel = new JPanel();
 			panel.setLayout(null);
 			panel.setBorder(BorderFactory.createTitledBorder("Search"));
 			
 			this.searchField.setSize(260, 50);
 			this.searchField.setBorder(BorderFactory.createTitledBorder("Enter search string here"));
 			this.searchField.setLocation(20, 30);
 
 			panel.add(searchField);
 			
 			buildButtonPanel(panel);
 			
 			this.contentPane.add(panel);
 		}
 		
 		private void addListToTheRight()
 		{
 			this.resultList.setBorder(BorderFactory.createTitledBorder("Result list"));
 			this.contentPane.add(resultList);
 			
 		}
 		
 		private void buildButtonPanel(JPanel thePanel)
 		{
 			JPanel buttonPanel = new JPanel();
 			buttonPanel.setLayout(new GridLayout(2, 3, 5, 5)); // 2 rows, 3 columns, 5 pixels in between
 			
			String[] buttonTxt = {"Search Customer", "Search Item", "Clear"};
 			ButtonListener buttonListener = new ButtonListener();
 			
 			JButton button = new JButton(buttonTxt[0]);
 			Dimension dim = button.getPreferredSize();
 			buttonPanel.setSize(3*dim.width + 8, 2 * dim.height + 5);
 			buttonPanel.setLocation(20,270);
 			
 			// Add all buttons to button panel
 			for(String str: buttonTxt)
 			{
 				button = new JButton(str);
 				buttonPanel.add(button);	
 				// connect listener
 				button.addActionListener(buttonListener);
 			}
 			thePanel.add(buttonPanel);
 		}
 	
 }
