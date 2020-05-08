 
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 public class EditorUi extends JFrame
 {
 	
 
 
   private static final long serialVersionUID = 5290364889985425653L;
 
   DefaultListModel listTriggerModel = new DefaultListModel();
   static DefaultListModel listTItemModel = new DefaultListModel();
   DefaultListModel listAdditionalModel = new DefaultListModel();
   DefaultListModel listAItemModel = new DefaultListModel();
 
   JTabbedPane tabbedPane = new JTabbedPane();
   
   static JPanel listTriggerPanel = new JPanel(new GridBagLayout());
   static JPanel listadditionalPanel = new JPanel(new GridBagLayout());
   
   static JPanel detailTriggerPanel = new JPanel();
   static JPanel detailAdditionalPanel = new JPanel();
 		  
   JLabel triggersLabel = new JLabel("Triggers: ");
   JLabel additionalsLabel = new JLabel("Additionals: ");
   
   JList triggersList = new JList();
   JList tItemsList = new JList(listTItemModel);
   JList additionalsList = new JList(listAdditionalModel);
   JList aItemsList = new JList(listAItemModel);
   
   static JPanel tab1Lists = new JPanel();
   static JPanel tab1Details = new JPanel();
   
   static JButton applyButton = new JButton("Apply Modifications!");
   
   static JTextField detailText[] = new JTextField[30];
   
   public static String detailTItem[][][];
   
   int cellWidth = 300;
   
   
   
 EditorUi() // the frame constructor method
 	  {
 	    super("EditorUi"); 	   
 	    setBounds(100,100,700,700);
 	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    getContentPane().add(tabbedPane);
 	    
 	    final JPanel tab1 = new JPanel();  
 	    tabbedPane.addTab("Triggers", tab1);
 	    tab1.setLayout(new GridLayout(2,0));
 	    tab1Details.setLayout(new GridLayout(6,1));
 	    tab1.add(tab1Lists);
 	    tab1.add(tab1Details);
 	    
 	    triggersList.setVisibleRowCount(15);
 	    triggersList.setFixedCellWidth(cellWidth);
 	    tItemsList.setVisibleRowCount(15);
 	    tItemsList.setFixedCellWidth(cellWidth);
 	    
 	    final JPanel tab2 = new JPanel();    
 	    tabbedPane.addTab("Additionals", tab2);
 	    additionalsList.setVisibleRowCount(15);
 	    additionalsList.setFixedCellWidth(cellWidth);
 	    aItemsList.setVisibleRowCount(15);
 	    aItemsList.setFixedCellWidth(cellWidth);
 	    
 	    triggersList.addListSelectionListener(new ListSelectionListener()
 	    {
 	    	@Override
 	    	public void valueChanged(ListSelectionEvent arg0)
 	    	{
 	    		if (!arg0.getValueIsAdjusting())
 	    		{
 	    			listTItemModel.clear();
	    			for (int x = 0; x < XmlParser.currentTrigger.length; x++)
 	    			{
	    				if (XmlParser.currentTrigger!= null && triggersList.getSelectedValue().equals(XmlParser.currentTrigger[x].getTriggerName()))
 	    				{
	    	    			detailTItem = XmlParser.currentTrigger[x].getDetailedItems();
 	    				}
 	    			}
 	    			tab1.revalidate();
 	    		}
 	    	}
 	    });
 	    tItemsList.addListSelectionListener(new ListSelectionListener()
 	    {
 	    	@Override
 	    	public void valueChanged(ListSelectionEvent arg0)
 	    	{
 	    		if (!arg0.getValueIsAdjusting())
 	    		{
 	    			showItemDetails(tItemsList.getSelectedIndex(), detailTItem);
 	    			tab1.revalidate();
 	    		}
 	    	}
 
 	    });
 	    additionalsList.addListSelectionListener(new ListSelectionListener()
 	    {
 	    	@Override
 	    	public void valueChanged(ListSelectionEvent arg1)
 	    	{
 	    		if (!arg1.getValueIsAdjusting())
 	    		{
 	    			listAItemModel.clear();
 	    			XmlParser.populateAItemList(additionalsList.getSelectedValue().toString());
 	    			tab2.revalidate();
 	    		}
 	    	}
 	    });    
 	    aItemsList.addListSelectionListener(new ListSelectionListener()
 	    {
 	    	@Override
 	    	public void valueChanged(ListSelectionEvent arg0)
 	    	{
 	    		if (!arg0.getValueIsAdjusting())
 	    		{
 	    			showItemDetails(aItemsList.getSelectedIndex(), null);
 	    			tab2.revalidate();
 	    		}
 	    	}
 
 	    });
 	    
 	    
 
 	    tab1Lists.add(new JScrollPane(triggersList) );
 	    tab1Lists.add(new JScrollPane(tItemsList) );
 	      
 	    tab2.add(new JScrollPane(additionalsList) );
 	    tab2.add(new JScrollPane(aItemsList) );
 	    tab2.add(new JScrollPane(detailAdditionalPanel));
 
 	    setVisible(true); // display this frame
 	  }
   		
 		
   
   
   public void addTriggerListItem(String newListItem)
   {
 	  listTriggerModel.addElement(newListItem);
 	  triggersList.setModel(listTriggerModel);
   }
   
   public static void addTItemListItem(String newListItem)
   {
 	  listTItemModel.addElement(newListItem);
   }
  
   public void addAdditionalListItem(String newListItem)
   {
 	  listAdditionalModel.addElement(newListItem);
 	  triggersList.setModel(listTriggerModel);
   }
   
   public void addAItemListItem(String newListItem)
   {
 	  listAItemModel.addElement(newListItem);
   }
   
 
   public void showItemDetails(final int selectedIndex, final String detailTItem[][][]) 
   {
 	  tab1Details.removeAll();
 	  int totalX = 12;
 	  Arrays.fill(detailText, null);
 
 	  if (selectedIndex!=-1 && detailTItem[selectedIndex][0][0]!=null)
 	  { 
 		  tab1Details.add(new JLabel("Trigger type: "));
 		  tab1Details.add(new JLabel(detailTItem[selectedIndex][0][0]));
 		  for (int x=0; x<6; x++)
 		  {
 		  tab1Details.add(new JLabel());
 		  }
 		  for (int x = 0; x < detailTItem[3].length; x++)
 		  {
 			if (detailTItem[selectedIndex][1][x]!=null)
 			{
 			tab1Details.add(new JLabel(detailTItem[selectedIndex][1][x]));	
 			detailText[x] = new JTextField(detailTItem[selectedIndex][2][x]);
 			tab1Details.add(detailText[x]);
 			totalX--;
 			}
 		  }
 		  for (int x= 0; x<totalX;x++)
 		 {
 			  tab1Details.add(new JLabel());
 		 }
 		  tab1Details.add(new JLabel());
 		  tab1Details.add(applyButton);
 		  applyButton.addActionListener(new ActionListener() { 
 			  public void actionPerformed(ActionEvent e) { 
 				 	String updatedItem = "";
 				  	for (int i = 0; i < detailText.length; i++)
 				 	{
 				 		if (detailText[i]!=null)
 				 		{
 				 			if (updatedItem =="")
 				 			{
 				 				updatedItem = detailTItem[selectedIndex][0][0] + "=" + detailText[i].getText();
 				 			}
 				 			else{
 					 			updatedItem = updatedItem +"|"+ detailText[i].getText();
 					 			System.out.println(updatedItem);
 				 			}
 				 			
 				 		}
 				 	}
 			  } 
 		  } );
 	  }
 
 	  
 }
   
   
   
   
 
 /*
   public void actionPerformed(ActionEvent event)
   {
 	  Object source =event.getSource();
  if (source == applyButton)
 	  {
 
 	 	}
 	  }
 
 	 
   }
 */
   
 }
