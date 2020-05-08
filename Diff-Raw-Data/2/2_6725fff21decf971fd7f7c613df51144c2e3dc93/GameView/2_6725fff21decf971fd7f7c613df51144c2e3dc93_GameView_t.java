 package com.taugame.tau.client;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.taugame.tau.shared.Card;
 
 public final class GameView {
 
     private final GameModel model;
     private final FlexTable table = new FlexTable();
 
     public GameView(GameModel model) {
         this.model = model;
     }
 
     public Widget getWidget() {
         return table;
     }
 
     public void redraw() {
         int columns = getNumberOfCardColumns();
         int rows = getNumberOfCardRows();
 
         table.removeAllRows();
         List<Card> cards = model.getCards();
         final Map<Integer, CardPanel> cardPanelMap = new HashMap<Integer, CardPanel>();
         for (int row = 0; row < rows; row++) {
             table.insertRow(row);
             for (int column = 0; column < columns; column++) {
                final int cardPosition = row + column * rows;
                 table.addCell(row);
                 Card card = cards.get(cardPosition);
                 final CardPanel cardPanel = new CardPanel();
                 cardPanelMap.put(cardPosition, cardPanel);
                 if (card != null) {
                     cardPanel.addStyleName("realCard");
                     if (model.isSelected(cardPosition)) {
                         cardPanel.addStyleName("selectedCard");
                     } else {
                         cardPanel.addStyleName("unselectedCard");
                     }
                     cardPanel.add(new Label(card.toString()));
                     cardPanel.addClickHandler(new ClickHandler() {
                         @Override
                         public void onClick(ClickEvent event) {
                             int deselected = model.selectCard(cardPosition);
                             if (deselected != -1) {
                                 CardPanel deselectedCardPanel = cardPanelMap.get(deselected);
                                 deselectedCardPanel.removeStyleName("selectedCard");
                                 deselectedCardPanel.addStyleName("unselectedCard");
                             }
                             if (model.isSelected(cardPosition)) {
                                 cardPanel.removeStyleName("unselectedCard");
                                 cardPanel.addStyleName("selectedCard");
                             } else {
                                 cardPanel.removeStyleName("selectedCard");
                                 cardPanel.addStyleName("unselectedCard");
                             }
                         }
                     });
                 }
                 table.setWidget(row, column, cardPanel);
             }
         }
     }
 
     private int getNumberOfCardColumns() {
         return model.getCards().size() / getNumberOfCardRows();
     }
 
     private int getNumberOfCardRows() {
         return 3;
     }
 }
