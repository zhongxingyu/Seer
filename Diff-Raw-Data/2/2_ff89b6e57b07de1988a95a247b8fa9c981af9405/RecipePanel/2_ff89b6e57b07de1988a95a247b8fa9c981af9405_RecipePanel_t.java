  package edu.ucsb.cs56.S12.m_a_p.cp3;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.*;
 import java.awt.Graphics;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.*;
 
 /** RecipePanel is panel that holds all the components for the recipe cookbook 
 
 
  */
 
 
 public class RecipePanel extends JPanel implements ActionListener, ListSelectionListener{
 
     //debug boolean variable
     public static final boolean debug=true;
     
     //Boolean to check when different list items are being selected
     boolean isChanging = false;
 
     //RecipeList that holds all recipes that exists or added by user
     RecipeList list = loadList();
     RecipeList temp;
     
     //JLists for main listNames and search list
     JList listNames;
     JList searchedNames = new JList();
     RecipeList searchedList2 = new RecipeList();
     JList pictureList;
     DefaultListModel listModel;
     
     //JLabels to hold recipe information and images
     JLabel recipeInfo;
     JLabel recipeImage;
     
     //indexes that hold the selectedIndexes for list items
     int index = 0;
     int index2;
     
     //JPanels for components inside main JFrame
     JPanel searchedPanel;
     JPanel RecipesListed;   
     JPanel recipeBox;
     JPanel picture;
     //Buffered Image/ImageIcon to accept and load images for recipes
     BufferedImage image;
     ImageIcon recipeIcon;
     
     //FileChoosers for files and images to be loaded 
     JFileChooser fc;
     JFileChooser ic;
     
     //Main panel that holds everything
     JPanel contents = new JPanel(new BorderLayout());
     
 	/**
     no-arg constructor constructs the JPanel and adds all the components
 	 */
 
     public RecipePanel(){
 	    super(new BorderLayout());  
 
 		//title image
 		JLabel titleLabel = new JLabel(new ImageIcon(this.getClass().getResource("images/title.jpg"), "title"),JLabel.CENTER);
 
 		//Label that actually contains the recipe info
 		recipeInfo = new JLabel(printInfo());
 
 		// set size and background of recipe info label
 		Dimension preferredSize  = new Dimension(300,list.get(index).getList().size()*35);
 		recipeInfo.setPreferredSize(preferredSize);
 		recipeInfo.setBackground(Color.WHITE);
 		recipeInfo.setOpaque(true);
 
 		//make new scroll pane to hold recipe info
 		JScrollPane RecipeInfoScroller = new JScrollPane(recipeInfo);
 		Border titled = new TitledBorder("Info:");
 		RecipeInfoScroller .setBorder(titled); 
 		RecipeInfoScroller .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		RecipeInfoScroller .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  
 		RecipeInfoScroller .setBackground(Color.WHITE);
 
 		//set up a list of recipes
 		listModel = new DefaultListModel();
 		String[] listMembers = makeRecipeList();
 		for(String s : listMembers)
 			listModel.addElement(s);
 
 		//use list of recipes to make a JList and set up how it works
 		listNames = new JList(listModel);
 		JScrollPane scroller = new JScrollPane(listNames);
 		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  
 		listNames.setVisibleRowCount(10);
 		listNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		listNames.addListSelectionListener(this);
 		listNames.setSelectedIndex(0);
 
 		//make a jpanel for the new JList and give it a border
 		RecipesListed = new JPanel(new BorderLayout());
 		RecipesListed.add(listNames, BorderLayout.CENTER);
 		Border titled2 = new TitledBorder("Recipe's");
 		RecipesListed.setBorder(titled2); 
 		RecipesListed.setBackground(Color.WHITE);
 		
 		//make a jpanel for the new SEARCH JList with a border
 		searchedPanel = new JPanel(new BorderLayout());
 		searchedPanel.add(searchedNames, BorderLayout.CENTER);
 		Border titled3 = new TitledBorder("Searched Recipes'");
 		searchedPanel.setBorder(titled3);
 		searchedPanel.setBackground(Color.WHITE);
 
 		//make new jpanel that holds the title label and recipe info 
 		recipeIcon = new ImageIcon(); 
 		contents.add(titleLabel , BorderLayout.NORTH);
 		contents.add(RecipeInfoScroller  , BorderLayout.CENTER);
 		contents.setBackground(Color.WHITE);
 
 
 
 		//makes a menu at top of frame
 		JMenuBar menuBar = new JMenuBar();
 		JMenu m = new JMenu("File");
 
 		//makes menu items 
 		JMenuItem newMenuItem = new JMenuItem("Add New Recipe");
 		JMenuItem newMenuItemDel = new JMenuItem("Delete Selected Recipe");
 		JMenuItem newMenuItemLoadList = new JMenuItem("Load a recipe list");
 		JMenuItem newMenuItemSaveList = new JMenuItem("Save recipe list");
 		JMenuItem newMenuItemImageLoad = new JMenuItem("Load selected recipe image"); 
 		JMenuItem newMenuItemDeleteImage = new JMenuItem("Delete selected recipe image");
 		JMenuItem newMenuItemSearchBox = new JMenuItem("Search for a recipe");
 		JMenuItem newMenuItemSearchIngredientsBox = new JMenuItem("Search for ingredients");
 
 		//add action listeners for menu items
 		newMenuItem.addActionListener(this);
 		newMenuItemDel.addActionListener(new deleteRecipe());
 		newMenuItemLoadList.addActionListener(new fileLoader());
 		newMenuItemSaveList.addActionListener(new fileSaver());
 		newMenuItemImageLoad.addActionListener(new ImageLoader());
 		newMenuItemDeleteImage.addActionListener(new DeleteImage());
 		newMenuItemSearchBox.addActionListener(new SearchBox());
 		newMenuItemSearchIngredientsBox.addActionListener(new SearchIngredientsBox());
 		//add menu items to menu and add menu to menubar
 		m.add(newMenuItem);
 		m.add(newMenuItemDel);
 		m.add(newMenuItemLoadList);
 		m.add(newMenuItemSaveList);
 		m.add(newMenuItemImageLoad);
 		m.add(newMenuItemDeleteImage);
 		m.add(newMenuItemSearchBox);
 		m.add(newMenuItemSearchIngredientsBox);
 		menuBar.add(m);
 
 		//add everything to this JPanel
 		add(RecipesListed, BorderLayout.LINE_START);
 		add(searchedPanel, BorderLayout.SOUTH);
 		add(menuBar , BorderLayout.PAGE_START);
 		add(contents , BorderLayout.CENTER);
 
 
 	}//end RecipePanel() no arg constructor
 
 	/**
     action performed method that listens for the user to puch the button to add a recipe to the current list
 	 */
 
 	public void actionPerformed(ActionEvent event) {
 		RecipeAdder adder = new RecipeAdder(list, listModel, listNames);
 
 	}//end actionPerformed method
 
 	/**
     valueChanged listens for the user to select something on the JList 
 	 */
 
 
 	public void valueChanged(ListSelectionEvent lse){
 
        	    //if (debug) { System.out.println("In RecipePanel.valueChanged..."); } DEBUG!
 	    if(!isChanging)
 		{
 		    // if list item is selected in main list, then deselect from Searched List
 		    isChanging = true;
 		    searchedNames.clearSelection();
 		    isChanging = false;
 		    
 		    index = listNames.getSelectedIndex();
 		    
 		    //if (debug) { System.out.println("In RecipePanel.valueChanged, inside if, index="+index); } DEBUG!
 		    
 		    String info = printInfo() + " ";
 		    
 		    recipeInfo.setText(info);                 
 		    Dimension preferredSize  = new Dimension(300,info.lastIndexOf(" ")/2);
 		    recipeInfo.setPreferredSize(preferredSize);
 		    
 		    if(index >= 0)// makes sure index doesn't go out of bounds
 			{
 			    recipeInfo.setIcon(list.get(index).getRecipeIcon());
 			    recipeInfo.repaint();
 			    recipeInfo.revalidate();
 			}
 		    
 		    
 		}
 	    
 	    
 	}   
     
     
 	/**
     printInfo gets the selected recipe and returns a string with that information 
 	 */
 
 	public String printInfo(){
 		if(index > (list.size() - 1) || index < 0)
 			return "error";
 		return list.get(index).printRecipe();      
 	}
 
 
 
 	/**
     makes a list of the recipe names in a string array
 	 */
 
 	public String[] makeRecipeList(){
 		String[] listEntries;
 		listEntries = new String[list.size()];
 
 		for(int i = 0; i<list.size();i++)
 			listEntries[i] = list.get(i).getName();
 
 		return listEntries;
 	}
 
 
 	/**
     an inner class the loades a recipeList from a file when the program starts
 	 */
 	public RecipeList loadList(){
 
 		RecipeList recipes = new RecipeList(new Recipe("My First recipe"));
 
 		try {
			URL url = new URL("https://github.com/UCSB-CS56-Projects/cs56-misc-recipe-manager/raw/master/list.ser"); 
 			ObjectInputStream is = new ObjectInputStream(url.openStream());
 			recipes = (RecipeList) is.readObject();
 			return recipes;
 		} catch(Exception ex){
 			ex.printStackTrace();
 		}
 
 
 		return recipes;
 	}
 
 	/**
     an inner class that deletes a recipe based on when the user presses the appropiate button 
 	 */
     public class deleteRecipe implements ActionListener{
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 	   
 	    index = listNames.getSelectedIndex();  
 	    list.get(index).setRecipeIcon(null);
 	    list.remove(index);	
 	    listModel.remove(index);
 	    
 	    listNames.setModel(listModel);
 	    listNames.setSelectedIndex(0);
 	}
 
     }
 
    
     //Search for recipes
     public class SearchBox implements ActionListener, ListSelectionListener{
 	@Override
 	public void actionPerformed(ActionEvent arg0){
 	    String userInput = JOptionPane.showInputDialog("Search for a recipe : ");
 	    String lowerUserInput = userInput.toLowerCase();
 	    String delims = "[ ]+";
 	    String[] lowerUserInputTokens = lowerUserInput.split(delims);
 	    RecipeList searchedList = new RecipeList();
 	   
     
 	    try{
 		String delims2 = "[,.! ]+"; //get rid of these symbols in recipe
 		for(int i = 0; i<list.size(); i++) //loops through individual recipes and searches through them to match user's input
 		    {
 			String[] recipeTokens = list.get(i).getName().toLowerCase().split(delims2); // array of strings that make up the recipe
 	        
 			for(String s: lowerUserInputTokens) //compares input to recipe
 				{
 				    for(String s2: recipeTokens)
 					{    
 					    if(s.matches(s2)) //add to list if match for input
 						{
 						    searchedList.add(list.get(i));
 						}
 					}
 				}
 		    }
 				   
 		
 	    }
 	    catch(Exception ex){
 		ex.printStackTrace();
 
 	    }
 	    finally{
 		DefaultListModel searchedListStrings = new DefaultListModel();
 		searchedList2 = searchedList;
 		if(searchedList.isEmpty())
 			    {
 				JOptionPane.showMessageDialog(null, "Recipe does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
 				
 			    }
 		else{
 		    for(int i=0; i<searchedList.size(); i++)
 			{
 			    searchedListStrings.addElement(searchedList.get(i).toString());
 			}
 		    searchedNames.setModel(searchedListStrings);
 		    searchedNames.setVisibleRowCount(10);
 		    searchedNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		    searchedNames.addListSelectionListener(this);
 		    searchedNames.setSelectedIndex(0);
 		}
 	    }
 	}
 	public void valueChanged(ListSelectionEvent lse){
 
 	    // if (debug) { System.out.println("In SearchBox.valueChanged..."); } DEBUG TEST
 	    
 	    if(!isChanging)
 		{
 		    isChanging = true;
 		    listNames.clearSelection();
 		    isChanging = false;
 		    
 		    index2 = searchedNames.getSelectedIndex();
 		    
 		    //if (debug) { System.out.println("In SearchBox.valueChanged, inside if, index="+index); } DEBUG TEST
 		    String info = searchedList2.get(index2).printRecipe()+" ";
 		    
 		    recipeInfo.setText(info);                 
 		    Dimension preferredSize  = new Dimension(300,info.lastIndexOf(" ")/2);
 		    recipeInfo.setPreferredSize(preferredSize);
 		    
 		    if(index2 >= 0)
 			{
 			    recipeInfo.setIcon(searchedList2.get(index2).getRecipeIcon());
 			    recipeInfo.repaint();
 			    recipeInfo.revalidate();
 			}
 	        
 		}
 	}
     }// end of Search
 
 
 
     //Search for recipes
     public class SearchIngredientsBox implements ActionListener, ListSelectionListener{
 	@Override
 	public void actionPerformed(ActionEvent arg0){
 	    String userInput = JOptionPane.showInputDialog("Search for an Ingredient: ");
 	    String lowerUserInput = userInput.toLowerCase();
 	    String delims = "[ ]+";
 	    String[] lowerUserInputTokens = lowerUserInput.split(delims);
 	    RecipeList searchedList = new RecipeList();
 	   
     
 	    try{
 		String delims2 = "[,-.! ]+"; //get rid of these symbols in recipe
 		for(int i = 0; i<list.size(); i++) //loops through individual recipes and searches through them to match user's input
 		    {
 			String[] ingredientTokens = list.get(i).getDirections().toLowerCase().split(delims2); // array of strings that make up the recipe
 	        
 			for(String s: lowerUserInputTokens) //compares each string to each ingredient token
 				{
 				    for(String s2: ingredientTokens)
 					{    
 					    if(s.matches(s2)) //if input matches ingredient, add list
 						{
 						    searchedList.add(list.get(i));
 						}
 					}
 				}
 		    }
 		
 	    }
 	    catch(Exception ex){
 		ex.printStackTrace();
 
 	    }
 	    finally{
 		DefaultListModel searchedListStrings = new DefaultListModel();
 		searchedList2 = searchedList;
 		
 		
 		if(searchedList.isEmpty())
 		    {
 			JOptionPane.showMessageDialog(null, "Recipe does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
 			
 		    }
 		else{
 		    for(int i=0; i<searchedList.size(); i++)
 			{
 			    if(!searchedListStrings.contains(searchedList.get(i).toString()))
 				{
 				    searchedListStrings.addElement(searchedList.get(i).toString());
 				}
 			}
 		    searchedNames.setModel(searchedListStrings);
 		    searchedNames.setVisibleRowCount(10);
 		    searchedNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		    searchedNames.addListSelectionListener(this);
 		    searchedNames.setSelectedIndex(0);
 		}
 	    }
 	}
 	public void valueChanged(ListSelectionEvent lse){
 	    
        	    //if (debug) { System.out.println("In SearchIngredientsBox.valueChanged..."); } DEBUG!
 	    
 	    if(!isChanging)
 		{
 		    // if list item in Searched List is selected, deselect from Main List
 		    isChanging = true;
 		    listNames.clearSelection();
 		    isChanging = false;
 		    
 		    index2 = searchedNames.getSelectedIndex();
 		    
 		    //  if (debug) { System.out.println("In SearchIngredientsBox.valueChanged, inside if, index="+index); } DEBUG!
 		    
 
 		    if(!(index2<0)) //Makes sure that index2 doesn't go out of bounds
 			{
 			    String info = searchedList2.get(index2).printRecipe()+" "; //prints recipe
 			    
 			    recipeInfo.setText(info);                 
 			    Dimension preferredSize  = new Dimension(300,info.lastIndexOf(" ")/2);
 			    recipeInfo.setPreferredSize(preferredSize);
 			    
 			    recipeInfo.setIcon(searchedList2.get(index2).getRecipeIcon()); //sets image
 			    recipeInfo.repaint();
 			    recipeInfo.revalidate();
 			}
 		    
 		}
 	}
     }// end of Search Ingredients
     
     
     
     
     public class ImageLoader implements ActionListener{
 	@Override
 	public void actionPerformed(ActionEvent arg0)
 	{
 	    ic = new JFileChooser();
 	    int returnVal = ic.showOpenDialog(listNames);
 	    if (returnVal == JFileChooser.APPROVE_OPTION){
 		File file = ic.getSelectedFile();
 		    try{
 			image = ImageIO.read(file);
 			index = listNames.getSelectedIndex();
 			recipeIcon = new ImageIcon(image);
 			list.get(index).setRecipeIcon(recipeIcon);		
 			recipeInfo.setIcon(list.get(index).getRecipeIcon());
 		    }catch(IOException ex){
 			ex.printStackTrace();
 		    }
 		    
 	    }
 	    
 	}
 	
     }// end of ImageLoader
 
     public class DeleteImage implements ActionListener{
 	@Override
 	public void actionPerformed(ActionEvent arg0)
 	{
 	    try{
 		index = listNames.getSelectedIndex();
 	    }
 	    catch(Exception ex)
 		{
 		    ex.printStackTrace();
 		}
 	    finally{
 		list.get(index).setRecipeIcon(null);
 		recipeInfo.setIcon(null);
 		recipeInfo.repaint();
 		recipeInfo.revalidate();
 	    }
 	}
     }//end of DeleteImage
 
 	/**
     an inner class that opens a recipeList based when the user presses the appropiate button 
 	 */
 	public class fileLoader implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			fc = new JFileChooser();
 			int returnVal = fc.showOpenDialog(listNames);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				File file = fc.getSelectedFile();
 				try {
 					ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
 					list = (RecipeList) is.readObject();
 				} catch(Exception ex){
 					ex.printStackTrace(); 
 
 				} finally{
 					listModel = listModel = new DefaultListModel();
 					String[] listMembers = makeRecipeList();
 					for(String s : listMembers)
 						listModel.addElement(s);
 					listNames.setModel(listModel);
 					listNames.setSelectedIndex(0);
 				}
 			}
 		}
 
 	}//end file loader
 
 	/**
     an inner class that saves a recipeList based when the user presses the appropiate button 
 	 */
 	public class fileSaver implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			fc = new JFileChooser();
 			int returnVal = fc.showSaveDialog(listNames);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				File file = fc.getSelectedFile();
 
 				try {
 					FileOutputStream fs = new FileOutputStream(file);
 			ObjectOutputStream os = new ObjectOutputStream(fs);
 
 					os.writeObject(list);
 					os.close();
 				} catch(Exception ex) {
 					ex.printStackTrace();
 				}                  
 
 			}
 		}
 
 	}//end of fileSaver
 
 
 
 }
