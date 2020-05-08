 package com.savvis.gsd.composer;
 
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.RichTextArea;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.Tree;
 
 public class ComposerComponentFactory {
 
 	public static Tree createTemplateTree() {
 		Tree tree = new Tree();
 		tree.setId("templateTree");
 		tree.setSizeUndefined();
 		tree.setImmediate(true);
 		return tree;
 	}
 	
 	public static ComboBox createTemplateSearchBox() {
 		ComboBox templateSearch = new ComboBox();
 		templateSearch.setImmediate(true);  //This setting ensures that change events fire immediately
 		templateSearch.setId("templateSearch");
 		templateSearch.setInputPrompt("Search Templates");
 		templateSearch.addItem("Global");
 		templateSearch.addItem("Coke");
 		return templateSearch;
 	}
 	
 	public static RichTextArea createEmailEditor() {
 		RichTextArea emailBody = new RichTextArea();
 		emailBody.setId("emailBody");
 		emailBody.setWidth("600px");
 		return emailBody;
 	}
 
 	public static TextArea createEmailHeaderComponent() {
 		TextArea headerArea = new TextArea();
 		headerArea.setId("emailHeader");
 		headerArea.setValue("From: \nReply To: \nSubject: \nTo: \nCC: \nBCC: ");
 		return headerArea;
 	}
 
 	public static Button createButton(String buttonText, String uniqueElementId) {
 		Button button = new Button(buttonText);
 		button.setId(uniqueElementId);
 		return button;
 	}
 
 	public static Label createLabel(String uniqueElementId) {
 		Label label = new Label(" ");
 		label.setStyleName("header-field");
 		label.setId(uniqueElementId);
 		return label;
 	}
 
 	public static TextField createTextField(String uniqueElementId) {
 		TextField tf = new TextField();
 		tf.setId(uniqueElementId);
 		tf.setStyleName("header-field");
 		tf.setVisible(false);
 		return tf;
 	}
 	
 	public static HorizontalLayout createHeaderSection(String uniqueIdentified, String sectionLabel, Component displayComponent, Component editorComponent) {
 		final HorizontalLayout section = new HorizontalLayout();
 		section.setId(uniqueIdentified);
 		section.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
 		section.addComponent(new Label(sectionLabel));
 		section.addComponent(displayComponent);
 		section.addComponent(editorComponent);
 		return section;
 	}
 }
