 package com.steamedpears.comp3004.views;
 
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.models.Player;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Map;
 
 public class ResultsDialog extends GameDialog {
     public ResultsDialog(JFrame f, final SevenWonders controller) {
         super(f,"End of Game Results",controller);
         setLayout(new MigLayout("wrap 2"));
         add(new JLabel("Player"));
         add(new JLabel("Score"));
         Map<Player,Integer> results = (controller.getGame()).tabulateResults();
         for(Player player : results.keySet()) {
            add(new JLabel("" + player.getPlayerId()));
             add(new JLabel("" + results.get(player)));
         }
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent windowEvent) {
                super.windowClosed(windowEvent);
                 controller.openNewGameDialog();
             }
         });
         pack();
     }
 }
