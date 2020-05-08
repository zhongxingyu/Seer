 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.example.vaadin.components.popups;
 
 import com.example.vaadin.components.CorpusDataComponent;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.PopupView;
 import com.vaadin.ui.VerticalLayout;
 import org.bushbank.bushbank.core.Token;
 
 /**
  *
  * @author Mato
  */
 public class CreateAnaphoraPopUp implements PopupView.Content {
 
     private VerticalLayout root = new VerticalLayout();
     private final Token selectedPronoun;
     private final Token targetToken;
 
     public CreateAnaphoraPopUp(final Token selectedPronoun, final Token targetToken, final CorpusDataComponent data) {
         this.selectedPronoun = selectedPronoun;
         this.targetToken = targetToken;
 
         root.setSizeUndefined();
         root.setSpacing(true);
         root.setMargin(true);
 
 
         HorizontalLayout horizontalTokensPart = new HorizontalLayout();
         HorizontalLayout horizontalButtonPart = new HorizontalLayout();
         Label questionLabel = new Label("Skutočne chceš vytvoriť anaforu medzi nasledujúcimi tokenmi?");
         root.addComponent(questionLabel);
         root.addComponent(horizontalTokensPart);
         root.addComponent(horizontalButtonPart);
 
         VerticalLayout token1 = new VerticalLayout();
         horizontalTokensPart.addComponent(token1);
         Label titleLabel = new Label("1.Token");
         token1.addComponent(titleLabel);
         Label tokenIdLabel = new Label("ID: " + selectedPronoun.getID());
         token1.addComponent(tokenIdLabel);
         Label tokenWordFormLabel = new Label("Slovo: " + selectedPronoun.getWordForm());
         token1.addComponent(tokenWordFormLabel);
 
         VerticalLayout token2 = new VerticalLayout();
         horizontalTokensPart.addComponent(token2);
         titleLabel = new Label("2.Token");
         token2.addComponent(titleLabel);
        tokenIdLabel = new Label("ID: " + selectedPronoun.getID());
         token2.addComponent(tokenIdLabel);
        tokenWordFormLabel = new Label("Slovo: " + selectedPronoun.getWordForm());
         token2.addComponent(tokenWordFormLabel);
 
         Button createButton = new Button("Ano");
         createButton.addListener(new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                 data.approvedAnaphora(selectedPronoun, targetToken);
                 
             }
         });
         
         horizontalButtonPart.addComponent(createButton);
         horizontalButtonPart.setComponentAlignment(createButton, Alignment.MIDDLE_LEFT);
         Button cancelButton = new Button("Ne");
         cancelButton.addListener(new Button.ClickListener() {
             @Override
             public void buttonClick(Button.ClickEvent event) {
                root.setVisible(false);
                data.notApprovedAnaphora();
             }
         });
         horizontalButtonPart.addComponent(cancelButton);
         horizontalButtonPart.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
 
     }
 
     @Override
     public String getMinimizedValueAsHTML() {
         return "";
     }
 
     @Override
     public Component getPopupComponent() {
         return root;
     }
 }
