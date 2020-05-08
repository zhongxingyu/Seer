 package com.steamedpears.comp3004.views;
 
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.Card;
 import com.steamedpears.comp3004.models.PlayerCommand;
 import com.steamedpears.comp3004.models.SevenWondersGame;
 import com.steamedpears.comp3004.models.players.Player;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Set;
 
 public class DiscardView extends JPanel {
     private static final int SELECTED_MULTIPLIER = 3;
 
     private SevenWonders controller;
     private CardView selectedCardView;
 
     public DiscardView(SevenWonders controller) {
         this.controller = controller;
         setLayout(new MigLayout("wrap 9"));
         update();
     }
 
     /**
      * Update the view.
      */
     public void update() {
         removeAll();
         selectedCardView = null;
 
         Set<Card> discardedCards = controller.getGame().getDiscard();
         if(discardedCards.size() > 0) {
 
             selectedCardView = new CardView((Card)discardedCards.toArray()[0],
                     CardView.DEFAULT_WIDTH * SELECTED_MULTIPLIER);
 
 
             final JCheckBox undiscard = new JCheckBox("Undiscard");
             undiscard.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent actionEvent) {
                     if(undiscard.isSelected()) {
                         controller.setUndiscardCommand(new PlayerCommand(
                                 PlayerCommand.PlayerCardAction.UNDISCARD,
                                 selectedCardView.getCard().getId()
                         ));
                     } else {
                         controller.setUndiscardCommand(null);
                     }
                 }
             });
 
             boolean firstCard = true;
             for(Card c : discardedCards) {
                 CardView cv = new CardView(c);
                 cv.setSelectionListener(new CardSelectionListener() {
                     @Override
                     public void handleSelection(Card card) {
                         selectedCardView.setCard(card);
                         Player player = controller.getLocalPlayer();
                        PlayerCommand command = new PlayerCommand(
                                 PlayerCommand.PlayerCardAction.UNDISCARD,
                                 selectedCardView.getCard().getId()
                        );
                        undiscard.setEnabled(player.isValid(command));
                        if(undiscard.isEnabled() && undiscard.isSelected()) {
                            controller.setUndiscardCommand(command);
                        } else {
                            controller.setUndiscardCommand(null);
                            undiscard.setSelected(false);
                        }
                     }
                 });
                 if(firstCard) {
                     firstCard = false;
                     cv.fireSelectionListener();
                 }
                 add(cv);
             }
             add(selectedCardView,"newline, span " + SELECTED_MULTIPLIER + " " + SELECTED_MULTIPLIER);
             add(undiscard);
         }
     }
 }
