 package net.davidjkelley.recipes.client;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class MainInterface extends VerticalPanel {
 	Button recipeSaveButton = new Button("Save Recipe");
 	Label selectRecipeLabel = new Label("Select Recipe: ");
 	String[] toBeDBisfied = {"one", "two", "three"};
 	EasyListBox<String> recipeDropDown = new EasyListBox<String>();
 	Button addRecipeButton = new Button("+");
 	FlexTable recipeFlexTable = new FlexTable();
 	
 	public MainInterface() {
 		//TODO - Update this to a dB interaction rather than a for loop of junk strings
 		for (String string : toBeDBisfied ) {
 			recipeDropDown.addItem(string);
 		}
 		
 		recipeDropDown.setSelectedIndex(0);
 		recipeDropDown.addChangeHandler(new ChangeHandler() {
 			public void onChange(ChangeEvent event) {
 			}
 			});
 		
 		this.add(recipeFlexTable);
 		recipeFlexTable.setWidget(0, 0, selectRecipeLabel);
 		recipeFlexTable.setWidget(0,1,recipeDropDown);
 		addRecipeButton.addClickHandler(new AddRecipeListener());
 		recipeFlexTable.setWidget(0,2,addRecipeButton);
         recipeSaveButton.addClickHandler(new SaveRecipeListener());   
 
 		recipeFlexTable.setWidget(0,3,recipeSaveButton);
 
 	}
 	
 	public class RecipeDropDownListener implements ClickHandler {
 		public void onClick(ClickEvent e) {
 			//TODO - will eventually return the name of the recipe and populate the recipe interface
 			System.out.println(e.getSource().toString());
 		}
 	}
 	
 	public class AddRecipeListener implements ClickHandler {
 		public void onClick (ClickEvent e) {
 			final DialogBox addNewRecipeDialogBox = new DialogBox(true);
 			//addNewRecipeDialogBox.setSize("500px", "500px");
 			//addNewRecipeDialogBox.setText("New Recipe");
 			FlexTable newRecipeFlexTable = new FlexTable();
 			
 			final TextBox newRecipeTextBox = new TextBox();
 			
 			int row = 0;
 			
 			//newRecipeFlexTable.setText(row, 0, "Name");
 			newRecipeFlexTable.setWidget(row, 0, newRecipeTextBox);
 			newRecipeFlexTable.getFlexCellFormatter().setColSpan(row, 0, 2);
 
 			Button cancel = new Button("Cancel");
 			Button okay = new Button("Save");
 			cancel.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent e) {
 					//TODO - make better memory management
 					addNewRecipeDialogBox.hide();
 				}
 			});
 			
 			okay.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent e) {
 					if (newRecipeTextBox.getText().length() >= 1){
 						
 //						//TODO - memory mgmt as well as dB interaction needs
 //						//to be implement
 						System.out.println("Closed with a save of: " + newRecipeTextBox.getText());
 						addNewRecipeDialogBox.hide();
 					}
 					else {
 						return;
 					}
					}	
 			});
 			row++;
 			newRecipeFlexTable.setWidget(row, 0, cancel);
 			newRecipeFlexTable.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER,
 					HasVerticalAlignment.ALIGN_MIDDLE);
 			newRecipeFlexTable.setWidget(row, 1, okay);
 			newRecipeFlexTable.getFlexCellFormatter().setAlignment(row, 1, HasHorizontalAlignment.ALIGN_CENTER,
 					HasVerticalAlignment.ALIGN_MIDDLE);
 			addNewRecipeDialogBox.setText("New Recipe");
 			addNewRecipeDialogBox.setWidget(newRecipeFlexTable);
 			addNewRecipeDialogBox.setAnimationEnabled(true);
 			addNewRecipeDialogBox.setGlassEnabled(true);
 			addNewRecipeDialogBox.center();
 			
 		}
 	}
 	
 	public class SaveRecipeListener implements ClickHandler {
 		@Override
 		public void onClick(ClickEvent event) {
 			System.out.println("Save Recipe button clicked");
 			
 		}
 	}
 //	public class CheckBoxListener implements ItemListener  {
 //		
 //		   public void itemStateChanged(ItemEvent e) {
 //			   System.out.println("Changed");
 //		   }
 //		
 //	}
 }
