 package com.morevaadin.vaadin7.jugdemo;
 
 import com.vaadin.navigator.View;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.Label;
import com.vaadin.ui.Root;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 @SuppressWarnings("serial")
 public class MainView extends CustomComponent implements View {
 
 	private TextField text = new TextField("", "JUG");
 
 	private Label label = new Label();
 
 	public MainView() {
 
 		VerticalLayout layout = new VerticalLayout();
 
 		layout.setMargin(true);
 		layout.setSpacing(true);
 
 		Button button = new Button("Say Hello");
 
 		button.addListener(ClickEvent.class, this, "sayHello");
 
 		layout.addComponent(text);
 		layout.addComponent(button);
 		layout.addComponent(label);
 
		setCompositionRoot(layout);

		Root.getCurrent().getPage().setTitle("Welcome to Vaadin JUG Demo");
 	}
 
 	public void navigateTo(String fragmentParameters) {
 	}
 
 	public void sayHello() {
 
 		String value = text.getValue();
 
 		label.setValue("Hello " + value + "!");
 	}
 }
