 package com.ceejii.gui.component;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JButton;
 
 import com.ceejii.gui.SearchSuggestionListener;
 import com.ceejii.gui.data.SearchSuggestion;
 
 public class DefaultSearchResultButton extends JButton implements SearchSuggestion {
 
 	private String id;
 	private String clue;
 	private String name;
 	private SearchSuggestionListener searchSuggestionListener;
 
 	public DefaultSearchResultButton(String name, String id, String clue) {
 		super(name);
 		this.name = name;
 		this.id = id;
 		this.setToolTipText(this.clue);
 		this.clue = clue;
 		this.addMouseListener(new MouseListener(){
 
 			public void mouseClicked(MouseEvent arg0) {
 				System.out.println("Mouse Clicked on button: " + DefaultSearchResultButton.this.getText() + " " + DefaultSearchResultButton.this.name);
 				searchSuggestionListener.resultChosen(new ActionEvent(DefaultSearchResultButton.this,0,DefaultSearchResultButton.this.getText()));
 			}
 
 			public void mouseEntered(MouseEvent arg0) {
 				System.out.println("Mouse Entered over button: " + DefaultSearchResultButton.this.getText() + " " + DefaultSearchResultButton.this.name);
 				if(searchSuggestionListener != null){
 					searchSuggestionListener.resultHovered(new ActionEvent(DefaultSearchResultButton.this,0,DefaultSearchResultButton.this.getToolTipText()));
 				}
 			}
 
 			public void mouseExited(MouseEvent arg0) {
 				System.out.println("Mouse Exited over button: " + DefaultSearchResultButton.this.getText() + " " + DefaultSearchResultButton.this.name);
 				if(searchSuggestionListener != null){
					searchSuggestionListener.resultHovered(new ActionEvent(DefaultSearchResultButton.this,0,DefaultSearchResultButton.this.getToolTipText()));
 				}
 			}
 
 			public void mousePressed(MouseEvent arg0) {
 			}
 
 			public void mouseReleased(MouseEvent arg0) {
 			}
 		});
 	}
 
 	public String getSuggestionId() {
 		return this.id;
 	}
 
 	public String getSuggestionClue() {
 		return this.clue;
 	}
 
 	public String getSuggestionName() {
 		return this.name;
 	}
 
 	public void addSearchSuggestionListener(SearchSuggestionListener listener) {
 		this.searchSuggestionListener = listener;
 	}
 }
