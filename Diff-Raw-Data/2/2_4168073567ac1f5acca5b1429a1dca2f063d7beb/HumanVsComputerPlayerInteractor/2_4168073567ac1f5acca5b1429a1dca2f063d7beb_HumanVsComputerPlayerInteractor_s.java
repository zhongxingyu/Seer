 package com.crudetech.tictactoe.client.swing;
 
 import com.crudetech.event.Event;
 import com.crudetech.event.EventHookingBean;
 import com.crudetech.event.EventListener;
 import com.crudetech.tictactoe.client.swing.grid.JTicTacToeGrid;
 import com.crudetech.tictactoe.game.ComputerPlayer;
 import com.crudetech.tictactoe.game.Grid;
 import com.crudetech.tictactoe.game.Player;
 import com.crudetech.tictactoe.game.TicTacToeGame;
 import com.crudetech.tictactoe.ui.UiFeedbackChannel;
 import com.crudetech.tictactoe.ui.UiPlayer;
 import com.crudetech.tictactoe.ui.UiView;
 
 import static java.util.Arrays.asList;
 
abstract class HumanVsComputerPlayerInteractor {
     private final TicTacToeGame game;
     private final Player humanUiPlayer;
     private final EventHookingBean<JTicTacToeGrid.CellClickedEventObject> eventHooker;
 
     public HumanVsComputerPlayerInteractor(ComputerPlayer computerPlayer, Event<JTicTacToeGrid.CellClickedEventObject> cellClickedEvent) {
         humanUiPlayer = createHumanUiPlayer();
         eventHooker = connectCellClicked(cellClickedEvent);
 
         game = new TicTacToeGame(humanUiPlayer, computerPlayer);
         computerPlayer.setGame(game);
     }
 
     private EventHookingBean<JTicTacToeGrid.CellClickedEventObject> connectCellClicked(Event<JTicTacToeGrid.CellClickedEventObject> cellClickedEvent) {
         EventListener<JTicTacToeGrid.CellClickedEventObject> cellClickedListener
                 = new EventListener<JTicTacToeGrid.CellClickedEventObject>() {
             @Override
             public void onEvent(JTicTacToeGrid.CellClickedEventObject e) {
                 addMark(humanUiPlayer, e.getClickedCellLocation());
             }
         };
 
         return new EventHookingBean<JTicTacToeGrid.CellClickedEventObject>(cellClickedEvent, asList(cellClickedListener));
     }
 
     void startWithHumanPlayer(Grid.Mark mark) {
         game.startWithPlayer(humanUiPlayer, mark);
     }
 
     private void addMark(Player player, Grid.Location location) {
         game.addMark(player, location);
     }
 
 
     public void destroy() {
         eventHooker.destroy();
     }
 
     private Player createHumanUiPlayer() {
         UiFeedbackChannel uiFeedback = createUiFeedback();
         UiView view = createUiView();
         return new UiPlayer(view, uiFeedback);
     }
 
     protected abstract UiView createUiView();
 
     protected abstract UiFeedbackChannel createUiFeedback();
 }
