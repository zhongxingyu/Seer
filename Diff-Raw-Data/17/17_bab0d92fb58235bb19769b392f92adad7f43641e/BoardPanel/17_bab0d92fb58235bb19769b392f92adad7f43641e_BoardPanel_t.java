 /**
  * Tonic - a chess client for frechess.org chess server.
  * More information is available at http://www.jinchess.com/.
  * Copyright (C) 2002 Alexander Maryanovsky.
  * Copyright (C) 2013 Wojciech Halicki-Piszko
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package free.jin.board;
 
 import free.chess.*;
 import free.chess.event.MoveEvent;
 import free.chess.event.MoveListener;
 import free.jin.Game;
 import free.jin.board.event.UserMoveEvent;
 import free.jin.board.event.UserMoveListener;
 import free.jin.event.*;
 import free.jin.plugin.Plugin;
 import free.util.PlatformUtils;
 import free.util.SquareLayout;
 import free.util.Utilities;
 import free.util.models.ModelUtils;
 import free.util.swing.FullscreenPanel;
 import free.util.swing.NonEditableTableModel;
 import free.workarounds.FixedJPanel;
 import free.workarounds.FixedJTable;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.*;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 
 /**
  * A panel which displays a chess board and all related information, such
  * as opponents' names, clock, opponents' ratings etc. 
  * To use BoardPanel, you must register it as a GameListener to some source of
  * GameEvents, or alternatively, call the methods defined in GameListener
  * directly.
  */
 
 public class BoardPanel extends FixedJPanel implements MoveListener, GameListener, 
     AdjustmentListener, PropertyChangeListener{
 
     
     
   /**
    * The <code>BoardManager</code> this BoardPanel is used by.
    */
 
   protected final BoardManager boardManager;
 
 
 
   /**
    * The Game this BoardPanel is displaying.
    */
 
   protected final Game game;
 
 
 
   /**
    * Is this BoardPanel active, i.e. is the game displayed is in progress?
    */
 
   private boolean isActive = true;
 
 
 
 
   /**
    * Is this BoardPanel flipped.
    */
 
   protected boolean isFlipped = false;
 
 
 
   /**
    * The JinBoard showing the current position.
    */
 
   protected JinBoard board;
 
 
 
   /**
    * True if we're highlighting our own moves. False if only opponent's moves.
    */
 
   private boolean highlightOwnMoves;
 
 
 
 
   /**
    * The current move sending mode. Possible values are defined in
    * <code>BoardManager</code>.
    */
 
   private int moveSendingMode;
 
   
   
 
   /**
    * The list of made moves.
    */
 
   protected final ArrayList madeMoves = new ArrayList();
 
 
 
 
   /**
    * The actual position in the game, this may differ than the one on the board
    * because the one on the board may include moves not yet inspected by the
    * server.
    */
 
   protected final Position realPosition;
 
 
 
 
   /**
    * The number of the move after which the position displayed on the board 
    * occurs.
    */
 
   protected int displayedMoveNumber = 0;
 
 
 
 
   /**
    * The JLabel displaying information about the player with the white pieces.
    */
 
   protected JLabel whiteLabel;
 
 
 
 
   /**
    * The JLabel displaying information about the player with the black pieces.
    */
 
   protected JLabel blackLabel;
 
   /**
    * Popup menu for issuing commands related to player's names.
    */
 
   protected JPopupMenu labelsPopupMenu;
 
   /**
    * Popup menu listener for labelsPopupMenu.
    */
   protected MouseListener popupMenuListener;
 
   /**
    * The Container of the action buttons (the button panel).
    */
 
   protected JPanel buttonPanel;
 
 
 
 
   /**
    * The button that makes the board fullscreen and restores it.
    */
 
   protected JToggleButton fullscreenButton;
   
   
   /*
    * The button that flips the board.
    */
 
   protected JButton flipBoardButton;
 
     /**
    * The AbstractChessClock displaying the time on white's clock.
    */
 
   protected AbstractChessClock whiteClock;
 
 
 
 
   /**
    * The AbstractChessClock displaying the time on black's clock.
    */
 
   protected AbstractChessClock blackClock;
 
 
 
 
   /**
    * The JLabel displaying information about the game.
    */
 
   protected JLabel gameLabel;
 
 
 
   /**
    * The JTable displaying the move list.
    */
 
   protected JTable moveListTable;
 
 
 
 
   /**
    * A boolean specifying whether the moveListTable selection is already being
    * changed and so the listener should ignore any calls to avoid endless
    * recursion.
    */
 
   private boolean isMoveListTableSelectionUpdating = false;
 
 
 
 
   /**
    * Needed because JDK 1.1 doesn't allow setting the selected table cell with
    * one method (and one event fired). The selection listener ignores the call
    * when this is set to true.
    */
 
   private boolean settingMoveListTableSelection = false;
 
 
 
 
   /**
    * The TableModel of the JTable displaying the move list.
    */
 
   protected TableModel moveListTableModel;
 
 
 
   /**
    * The JScrollPane in which we put the moveListTable.
    */
 
   protected JScrollPane moveListTableScrollPane;
 
 
 
   /**
    * The scrollbar which lets you scroll through all the positions that occurred
    * in the game.
    */
 
   protected JScrollBar positionScrollBar;
 
 
   protected Action moveForward;
 
   protected Action moveBackward;
 
   /**
    * A boolean specifying whether the positionScrollBar is being changed
    * programmatically. When it is, the method(s) listening to events from it,
    * should probably ignore them.
    */
 
   protected boolean isPositionScrollBarUpdating = false;
 
 
 
 
   /**
    * True when the board position is being updated programmatically (and not as
    * a result of user interaction) and thus events from it should probably be
    * ignored.
    */
 
   protected boolean isBoardPositionUpdating = false;
 
 
 
   /**
    * The move made when we've fired a UserMadeMove and it hasn't been echoed to
    * us yet. Null if none.
    */
 
   private Move moveEnRoute = null;
 
 
 
 
   /**
    * The queued move - the user may make a move when it's not his turn, and it
    * will then be saved here until his opponent makes a move.
    */
 
   private Move queuedMove = null;
   
   
   
   /**
    * The time when we sent the last user's move to the server. We need this
    * because if the move was illegal, we need to know how much time it spent
    * in transit to subtract that time from the player's clock (we can't count
    * on getting a clock update from the server).
    */
    
   private long sentMoveTimestamp = -1;
 
 
 
   /**
    * The FullscreenPanel allowing us to maximize the board panel.
    */
 
   private final FullscreenPanel fullscreenPanel;
 
 
 
   /**
    * The target of the fullscreen panel, to which we add all the components.
    */
 
   private final ContentPanel contentPanel;
 
   /**
    * Field needed to implement SessinListener. whp 2006
    */
   
   private boolean isConnected = false;
 
   /**
    * Plugins. Just to get BoardManager out of it. whp 2006
    */
   
   private Plugin[] plugins = null;
   
   /**
    * Creates a new <code>BoardPanel</code> which will be used by the given
    * <code>BoardManager</code>, will display the given Game and will have the
    * given move input mode.
    */
 
   public BoardPanel(BoardManager boardManager, Game game){
     this.game = game;
     this.boardManager = boardManager;
     this.realPosition = game.getInitialPosition();
 
     boardManager.addPropertyChangeListener(this);
     game.addPropertyChangeListener(this);
 
     this.contentPanel = new ContentPanel();
     this.fullscreenPanel = new FullscreenPanel(contentPanel);
     fullscreenPanel.setAllowExclusiveMode(false); // Too buggy
 
     this.moveForward = new AbstractAction() {
       @Override
       public void actionPerformed(ActionEvent e) {
         SwingUtilities.invokeLater(new Runnable() {
           public void run() {
             positionScrollBar.setValueIsAdjusting(true);
             int lastValue = positionScrollBar.getValue();
             int newValue = lastValue+1;
             positionScrollBar.setValue(newValue);
             positionScrollBar.setValueIsAdjusting(false);
           }
         });
       }
     };
 
     this.moveBackward = new AbstractAction() {
       @Override
       public void actionPerformed(ActionEvent e) {
         SwingUtilities.invokeLater(new Runnable() {
           public void run() {
             positionScrollBar.setValueIsAdjusting(true);
             int lastValue = positionScrollBar.getValue();
             int newValue = lastValue-1;
             positionScrollBar.setValue(newValue);
             positionScrollBar.setValueIsAdjusting(false);
           }
         });
       }
     };
 
 
     init(game);
 
     setLayout(new BorderLayout());
     add(fullscreenPanel, BorderLayout.CENTER);
 
     // Fullscreen mode locks up the application under OS X (older versions of Java).
     // Fullscreen mode is broken under Java 1.5.0, see
     // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5085648
     if (!PlatformUtils.isJavaBetterThan("1.4") ||
         (PlatformUtils.isMacOSX() && !PlatformUtils.isJavaBetterThan("1.4.2"))||
         ((System.getSecurityManager() != null) && PlatformUtils.isJavaBetterThan("1.5"))){
       fullscreenButton.setEnabled(false);
       fullscreenButton.setToolTipText("Fullscreen board mode is unavailable on your system");
     }
     else{
       KeyStroke fullscreenKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK);
       contentPanel.registerKeyboardAction(new ActionListener(){
         public void actionPerformed(ActionEvent evt){
           fullscreenPanel.getFullscreenModeModel().flip();
         }
       }, fullscreenKeyStroke, WHEN_FOCUSED);
     }
   }
   
 
 
   
   /**
    * Adds the given UserMoveListener to the list of listeners receiving events about
    * moves made on the board by the user. 
    */
 
   public void addUserMoveListener(UserMoveListener listener){
     listenerList.add(UserMoveListener.class, listener);
   }
 
 
 
 
   /**
    * Removes the given UserMoveListener from the list of listeners receiving events
    * about moves made by the user on the board.
    */
 
   public void removeUserMoveListener(UserMoveListener listener){
     listenerList.remove(UserMoveListener.class, listener); 
   }
 
 
 
 
 
   /**
    * Dispatches the given UserMoveEvent to all interested UserMoveListeners of this BoardPanel.
    */
 
   protected void fireUserMadeMove(UserMoveEvent evt){
     Object [] listenerList = this.listenerList.getListenerList();
     for (int i = 0; i < listenerList.length; i += 2){
       if (listenerList[i] == UserMoveListener.class){
         UserMoveListener listener = (UserMoveListener)listenerList[i+1];
         listener.userMadeMove(evt);
       }
     }
   }
 
 
 
 
   /**
    * This method is called by the constructor, it's meant to initialize all the
    * components and add them.
    */
 
   protected void init(Game game){
     isFlipped = game.isBoardInitiallyFlipped();
     highlightOwnMoves = boardManager.isHighlightingOwnMoves();
     moveSendingMode = boardManager.getMoveSendingMode();
     createComponents(game);
     
 //    addComponents(game, isFlipped); 
     // We're not adding them because the layout depends on the size, which is unknown at this point.
     // See the doLayout() method.
   }
 
   /**
    * A mouse listener which pauses other plugins when the mouse moves over the
    * board in order to give the board maximum preference.
    */
    
   private class OtherPluginsPauser implements MouseListener, MouseMotionListener{
     
     private boolean paused = false;
                                   
     private Timer timer = new Timer(1000, new ActionListener(){
       public void actionPerformed(ActionEvent evt){
         boardManager.setOtherPluginsPaused(false);
         paused = false;
       }
     });
     
     private void go(){
       if (!paused){
         paused = true;
         boardManager.setOtherPluginsPaused(true);
       }
       
       if (timer.isRunning())
         timer.restart();
       else{
         timer.setRepeats(false);
         timer.start();
       }
     }
     
     
     public void mouseDragged(MouseEvent e){go();}
     public void mouseMoved(MouseEvent e){go();}
     public void mouseClicked(MouseEvent e){go();}
     public void mouseEntered(MouseEvent e){go();}
     public void mouseExited(MouseEvent e){go();}
     public void mousePressed(MouseEvent e){go();}
     public void mouseReleased(MouseEvent e){go();}
   }
 
 
 
 
 
   /**
    * This method is meant to create all the sub-components used by the BoardPanel.
    * The default implementation delegates the creation to the following methods:
    * <UL>
    *   <LI> {@link #createBoard(Game)}
    *   <LI> {@link #createGameLabel(Game)}
    *   <LI> {@link #createWhiteLabel(Game)}
    *   <LI> {@link #createBlackLabel(Game)}
    *   <LI> {@link #createWhiteClock(Game)}
    *   <LI> {@link #createBlackClock(Game)}
    *   <LI> {@link #createButtonPanel(Game)}
    *   <LI> {@link #createMoveListTableModel(Game)}
    *   <LI> {@link #createMoveListTable(Game, TableModel)}
    *   <LI> {@link #createPositionScrollBar(Game)}
    * </UL>
    */
 
   protected void createComponents(Game game){
     popupMenuListener = createPopMenuListener();
     board = createBoard(game);
     initBoard(game, board);
     board.getPosition().addMoveListener(this);
 
     gameLabel = createGameLabel(game);
     labelsPopupMenu = createLabelsPopMenu();
     whiteLabel = createWhiteLabel(game);
     whiteLabel.addMouseListener(popupMenuListener);
     blackLabel = createBlackLabel(game);
     blackLabel.addMouseListener(popupMenuListener);
     fullscreenButton = createFullscreenButton();
     flipBoardButton = createFlipBoardButton();
     whiteClock = createWhiteClock(game);
     blackClock = createBlackClock(game);
     buttonPanel = createButtonPanel(game);
     moveListTableModel = createMoveListTableModel(game);
     moveListTable = createMoveListTable(game, moveListTableModel);
     moveListTableScrollPane = createMoveListTableScrollPane(game, moveListTable);
     
     // Pauses other plugins when a move is in progress.
     OtherPluginsPauser pauser = new OtherPluginsPauser();
     board.addMouseListener(pauser);
     board.addMouseMotionListener(pauser);
 
     moveListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
      
       public void valueChanged(ListSelectionEvent evt){
         moveListTableSelectionChanged();
       }
       
     });
 
     moveListTable.getColumnModel().addColumnModelListener(new TableColumnModelListener(){
       public void columnAdded(TableColumnModelEvent e){}
       public void columnMarginChanged(ChangeEvent e){}
       public void columnMoved(TableColumnModelEvent e){}
       public void columnRemoved(TableColumnModelEvent e){}
       public void columnSelectionChanged(ListSelectionEvent e){
         moveListTableSelectionChanged();
       }
     });
 
     positionScrollBar = createPositionScrollBar();
     positionScrollBar.addAdjustmentListener(this);
     updateClockActiveness();
   }
 
   private MouseListener createPopMenuListener() {
     return new MouseAdapter() {
       @Override
       public void mousePressed(MouseEvent e) {
         maybeShowPopup(e);
       }
 
       @Override
       public void mouseReleased(MouseEvent e) {
         maybeShowPopup(e);
       }
 
       private void maybeShowPopup(MouseEvent e) {
         if (e.isPopupTrigger()) {
           labelsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
         }
       }
     };
   }
 
   private JPopupMenu createLabelsPopMenu() {
     final JPopupMenu popMenu = new JPopupMenu();
     JMenuItem fingerMenuItem = new JMenuItem(new AbstractAction("finger") {
       @Override
       public void actionPerformed(ActionEvent e) {
         JLabel label = (JLabel) popMenu.getInvoker();
         boardManager.getConn().sendCommand("finger " + label.getText());
       }
     });
     JMenuItem varsMenuItem = new JMenuItem(new AbstractAction("variables") {
       @Override
       public void actionPerformed(ActionEvent e) {
         JLabel label = (JLabel) popMenu.getInvoker();
         boardManager.getConn().sendCommand("vars " + label.getText());
       }
     });
     popMenu.add(fingerMenuItem);
     popMenu.add(varsMenuItem);
 
     return popMenu;
   };
 
 
   /**
    * Creates and returns the <code>JinBoard</code>.
    */
 
   protected JinBoard createBoard(Game game){
     return new JinBoard(game.getInitialPosition());
   }
 
 
 
 
 
   /**
    * Initializes the board.
    */
 
   protected void initBoard(Game game, JinBoard board){
     if (isFlipped())
       board.setFlipped(true);
 
     configureBoardFromGame(game, board);
     configureBoardFromBoardManager(board);
 
     ActionListener escapeListener = new ActionListener(){
       public void actionPerformed(ActionEvent evt){
         JinBoard board = BoardPanel.this.board;
         if (board.isMovingPiece())
           board.cancelMovingPiece();
         else if (queuedMove != null){
           setQueuedMove(null);
           isBoardPositionUpdating = true;
           board.getPosition().copyFrom(realPosition);
           if (isMoveEnRoute())
             board.getPosition().makeMove(moveEnRoute);
           isBoardPositionUpdating = false;
         }
         else
           fullscreenPanel.getFullscreenModeModel().setOff();
       }
     };
 
     // We have no choice but to use WHEN_FOCUSED and to give the focus to the
     // content panel when it is pressed because WHEN_IN_FOCUSED_WINDOW doesn't
     // work in MS VM.
     contentPanel.registerKeyboardAction(escapeListener,
       KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, MouseEvent.BUTTON1_MASK), JComponent.WHEN_FOCUSED);
     contentPanel.registerKeyboardAction(escapeListener, 
       KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
 
     InputMap contentPanelInputMap = contentPanel.getInputMap();
     contentPanelInputMap.put(KeyStroke.getKeyStroke("RIGHT"), "forward");
     ActionMap contentPanelActionMap = contentPanel.getActionMap();
     contentPanelActionMap.put("forward", moveForward);
 
     contentPanelInputMap.put(KeyStroke.getKeyStroke("LEFT"), "backward");
     contentPanelActionMap.put("backward", moveBackward);
     board.addMouseListener(new MouseAdapter(){
       public void mousePressed(MouseEvent evt){
         if (!contentPanel.hasFocus())
           contentPanel.requestFocus();
       }
     });
     
     if (!isUserTurn() && (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE))
       board.setEditable(false);
   }
   
   
   
   /**
    * Configures the board according to the <code>Game</code> settings.
    */
   
   protected void configureBoardFromGame(Game game, JinBoard board){
     board.setMoveInputMode(calcMoveInputMode(game));
   }
   
   
   
   /**
    * Configures the board according to the <code>BoardManager</code> settings.
    */
   
   protected void configureBoardFromBoardManager(JinBoard board){
     board.setPiecePainter(boardManager.getPiecePainter());
     board.setBoardPainter(boardManager.getBoardPainter());
     board.setMoveInputStyle(boardManager.getMoveInputStyle());
     board.setPieceFollowsCursor(boardManager.isPieceFollowsCursor());
     board.setHighlightMadeMoveSquares(boardManager.isHighlightMadeMoveSquares());
     board.setMadeMoveSquaresHighlightColor(boardManager.getMadeMoveSquaresHighlightColor());
     board.setMoveHighlightingStyle(boardManager.getMoveHighlightingStyle());
     board.setCoordsDisplayStyle(boardManager.getCoordsDisplayStyle());
     board.setCoordsDisplayColor(boardManager.getCoordsDisplayColor());
     board.setManualPromote(!boardManager.isAutoPromote());
     board.setMoveHighlightingColor(boardManager.getMoveHighlightingColor());
   }
 
 
 
 
   /**
    * Returns the move input mode that should be used for the specified game.
    */
 
   protected int calcMoveInputMode(Game game){
     if (game.getGameType() == Game.MY_GAME)
       if (game.isPlayed())
         if (game.getUserPlayer() == Player.WHITE_PLAYER)
           return JinBoard.WHITE_PIECES_MOVE;
         else
           return JinBoard.BLACK_PIECES_MOVE;
       else 
         return JinBoard.CURRENT_PLAYER_MOVES; 
     else // This counts for both ISOLATED_BOARD and OBSERVED_GAME.
       return JinBoard.NO_PIECES_MOVE;
   }
 
 
 
 
   /**
    * Creates the JLabel displaying information about the game.
    */
 
   protected JLabel createGameLabel(Game game){
     JLabel gameLabel = new JLabel(createGameLabelText(game));
     gameLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
     return gameLabel;
   }
 
 
 
   /**
    * Returns the text that should be displayed on the game label.
    */
 
   protected String createGameLabelText(Game game){
     return (game.isRated() ? "Rated" : "Unrated") + ' ' + game.getTCString()+ ' ' + game.getVariant().getName();
   }
 
 
 
 
   /**
    * Creates the JLabel displaying information about the player with the white
    * pieces.
    */
 
   protected JLabel createWhiteLabel(Game game){
     JLabel whiteLabel = new JLabel(createWhiteLabelText(game));
     whiteLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
     return whiteLabel;
   }
 
 
 
   /**
    * Returns the text that should be displayed by the white player's label.
    */
 
   protected String createWhiteLabelText(Game game){
     return game.getWhiteName() + game.getWhiteTitles() + ' ' + game.getWhiteRating();
   }
 
 
 
 
   /**
    * Creates the JLabel displaying information about the player with the black
    * pieces.
    */
 
   protected JLabel createBlackLabel(Game game){
     JLabel blackLabel = new JLabel(createBlackLabelText(game));
     String blackLabelFont = (String)boardManager.getPrefs().getString("blacklabel.font-family", "Serif");
     int blackLabelFontSize = boardManager.getPrefs().getInt("blacklabel.font-size", 20);
 
     blackLabel.setFont(new Font(blackLabelFont, Font.BOLD, blackLabelFontSize));
     return blackLabel;
   }
 
 
 
 
   /**
    * Returns the text that should be displayed by the black player's label.
    */
 
   protected String createBlackLabelText(Game game){
     return game.getBlackName() + game.getBlackTitles() + ' ' + game.getBlackRating();
   }
 
 
 
 
   /**
    * Creates the button that makes the board fullscreen and restores it.
    */
 
   protected JToggleButton createFullscreenButton(){
     JToggleButton button = new JToggleButton();
     button.setIcon(new ImageIcon(BoardPanel.class.getResource("images/fullscreen.gif")));
     button.setRequestFocusEnabled(false);
     button.setMargin(new Insets(0, 0, 0, 0));
     button.setToolTipText("Toggle fullscreen mode");
     
     ModelUtils.link(fullscreenPanel.getFullscreenModeModel(), button.getModel());
 
     return button;
   }
   
   /**
    * Creates the button that flips the board. whp
    */
 
   protected JButton createFlipBoardButton(){
       JButton button = new JButton(new ImageIcon(BoardPanel.class.getResource("images/flip.png")));
       button.setMargin(new Insets(0, 0, 0, 0));
       button.setToolTipText("Flips the board");
       button.addActionListener( new ActionListener(){
               public void actionPerformed(ActionEvent evt){
                 toggleFlipped();
               }
       });
       
       return button;
   }
   
   /**
    * Creates the AbstractChessClock which will display the amount of time remaining
    * on white's clock.
    */
 
   protected AbstractChessClock createWhiteClock(Game game){
     return new JChessClock(game.getWhiteTime());
   }
 
 
 
 
   /**
    * Creates the AbstractChessClock which will display the amount of time remaining
    * on black's clock.
    */
 
   protected AbstractChessClock createBlackClock(Game game){
     return new JChessClock(game.getBlackTime());
   }
 
 
 
 
   /**
    * Creates the button panel - a panel with various action buttons on it.
    * If the given Game is not of type Game.MY_GAME, this method returns null.
    * If the given Game is a played game, the actual creation is delegated to the
    * {@link #createPlayedButtonPanel(Game)} method, otherwise (it's an examined game)
    * the creation is delegated to the {@link #createExaminedButtonPanel(Game)} method.
    */
 
   protected JPanel createButtonPanel(Game game){
     JPanel buttonPanel;
     if (game.getGameType() != Game.MY_GAME)
       return null;
     else if (game.isPlayed())
       buttonPanel = createPlayedGameButtonPanel(game); 
     else
       buttonPanel = createExaminedGameButtonPanel(game);
 
     return buttonPanel;
   }
 
 
 
   /**
    * Creates the button panel for a game played by the user.
    */
 
   protected JPanel createPlayedGameButtonPanel(Game game){
     return new PlayedGameButtonPanel(boardManager, game, contentPanel);
   }
 
 
 
   /**
    * Creates the button panel for a game examined by the user.
    */
 
   protected JPanel createExaminedGameButtonPanel(Game game){
     return new ExaminedGameButtonPanel(boardManager, game);
   }
 
 
 
   /**
    * Creates the TableModel for the JTable which will be used for displaying
    * the move list. If you override this method, you should also see if you need
    * to override {@link #addMoveToListTable(Move)} and {@link #updateMoveListTable()}.
    */
 
   protected TableModel createMoveListTableModel(Game game){
     return new NonEditableTableModel(new String[]{"Move No.", "White", "Black"}, 0);
   }
 
 
 
 
   /**
    * Creates the JTable which will display the move list. The JTable must be
    * created with the given TableModel.
    */
 
   protected JTable createMoveListTable(Game game, TableModel moveListTableModel){
     JTable table = new FixedJTable(moveListTableModel);
     table.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
     table.setCellSelectionEnabled(true);
     table.getTableHeader().setPreferredSize(new Dimension(150, 18));
     table.getTableHeader().setReorderingAllowed(false);
     table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     table.setRequestFocusEnabled(false);
 
     return table;
   }
 
 
 
 
   /**
    * Creates the JScrollPane in which we put the moveListTable.
    */
 
   protected JScrollPane createMoveListTableScrollPane(Game game, JTable moveListTable){
     JScrollPane scrollPane = new JScrollPane(moveListTable);
     scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
     scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
     return scrollPane;
   }
 
 
 
 
   /**
    * A Runnable we SwingUtilities.invokeLater() to set the move list table vertical
    * scrollbar value.
    */
 
   private class MoveListScrollBarUpdater implements Runnable{
     public void run(){
       JScrollBar vScroller = moveListTableScrollPane.getVerticalScrollBar();
       int selectedRow = moveListTable.getSelectedRow();
       int selectedColumn = moveListTable.getSelectedColumn();
       if ((selectedRow==-1)||(selectedColumn==-1))
         if (displayedMoveNumber==0)
           vScroller.setValue(vScroller.getMinimum());
         else
           vScroller.setValue(vScroller.getMaximum());
       else{
         moveListTable.scrollRectToVisible(moveListTable.getCellRect(selectedRow, selectedColumn, true));
       }
     }
   }
 
 
 
 
 
   /**
    * Adds a single move to the move list TableModel.
    */
 
   protected void addMoveToListTable(Move move){
     DefaultTableModel model = (DefaultTableModel)moveListTableModel;
     int movesSinceStart = game.getPliesSinceStart()/2;
     int rowCount = moveListTable.getRowCount();
     if ((rowCount == 0) || move.getPlayer().isWhite()){
       model.setNumRows(++rowCount);
       model.setValueAt((rowCount + movesSinceStart)+".", rowCount-1, 0);
     }
     if (move.getPlayer().isWhite())
       model.setValueAt(move, rowCount-1, 1);
     else{
       model.setValueAt(move, rowCount-1, 2);
     }
 
     if (displayedMoveNumber == 0){
       moveListTable.clearSelection();
       positionScrollBar.setValues(0, 1, 0, madeMoves.size() + 1); 
     }
     else{
       boolean isFirstMoveBlack = ((Move)madeMoves.get(0)).getPlayer().isBlack();
       int visualMoveNumber = isFirstMoveBlack ? displayedMoveNumber + 1 : displayedMoveNumber;
       int row = (visualMoveNumber - 1) / 2;
       int column = 2 - (visualMoveNumber%2);
 
       isPositionScrollBarUpdating = true;
       isMoveListTableSelectionUpdating = true;
       setMoveListTableSelection(row, column);
       positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
       isMoveListTableSelectionUpdating = false;
       isPositionScrollBarUpdating = false;
     }
 
     SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
   }
 
 
 
   /**
    * Brings the the move list table up to date with the current move list and
    * displayed position.
    */
 
   protected void updateMoveListTable(){
     DefaultTableModel model = (DefaultTableModel)moveListTableModel;
     int movesSinceStart = game.getPliesSinceStart()/2;
     int moveCount = madeMoves.size();
     boolean isFirstMoveBlack = (moveCount > 0) && ((Move)madeMoves.get(0)).getPlayer().isBlack();
     int numRows = isFirstMoveBlack ? 1+moveCount/2 : (moveCount+1)/2;
     model.setNumRows(numRows);
     for (int i = 0; i < numRows; i++)
       model.setValueAt((i + 1 + movesSinceStart)+".", i, 0);
 
     int row = 0;
     int column = isFirstMoveBlack ? 2 : 1;
     for (int i = 0; i < moveCount; i++){
       Object move = madeMoves.get(i);
       model.setValueAt(move, row, column);
       column++;
       if (column == 3){
         row++;
         column = 1;
       }
     }
 
     if (column == 2) // There's an extra, empty cell, which we need to clear.
       model.setValueAt(null, row, column);
 
 
     if (displayedMoveNumber == 0){
       moveListTable.clearSelection();
       positionScrollBar.setValues(0, 1, 0, madeMoves.size() + 1);
     }
     else{
       int visualMoveNumber = isFirstMoveBlack ? displayedMoveNumber + 1 : displayedMoveNumber;
       row = (visualMoveNumber - 1) / 2;
       column = 2 - (visualMoveNumber%2);
 
       isPositionScrollBarUpdating = true;
       isMoveListTableSelectionUpdating = true;
       setMoveListTableSelection(row, column);
       positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
       isMoveListTableSelectionUpdating = false;
       isPositionScrollBarUpdating = false;
     }
 
     SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
   }
 
 
 
 
   /**
    * Sets the specified cell to be the selected cell in the move list table.
    */
 
   protected void setMoveListTableSelection(int row, int column){
     int oldRow = moveListTable.getSelectedRow();
     int oldColumn = moveListTable.getSelectedColumn();
 
     if (oldRow != row){
       settingMoveListTableSelection = true;
       moveListTable.setColumnSelectionInterval(column, column);
       settingMoveListTableSelection = false;
       moveListTable.setRowSelectionInterval(row, row);
     }
     else if (oldColumn != column){
       settingMoveListTableSelection = true;
       moveListTable.setRowSelectionInterval(row, row);
       settingMoveListTableSelection = false;
       moveListTable.setColumnSelectionInterval(column, column);
     }
   }
 
 
 
 
 
   /**
    * Updates the move highlighting on the board. The boolean argument specifies
    * whether the last made move (on the board, not the real position) is a move
    * made by the user and should therefore be ignored if
    * isHighlightingOwnMoves() returns false. Note that the callers don't 
    * really determine whether the last move is the user's - they only pass
    * true when it's a move that has just been made by the user.
    */
 
   private void updateMoveHighlighting(boolean isOwnMove){
     if ((displayedMoveNumber == 0) || (isOwnMove && !highlightOwnMoves))
       board.setHighlightedMove(null);
     else{
       Move move = (Move)madeMoves.get(displayedMoveNumber - 1);
       board.setHighlightedMove(move);
     }
   }
 
 
 
 
   /**
    * Creates the JScrollBar which controls the position displayed on the board.
    */
 
   protected JScrollBar createPositionScrollBar(){
     JScrollBar scrollbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, 1, 0, 1);
     return scrollbar;
   }
 
 
 
 
   /**
    * The panel actually containing all the components.
    */
 
   private class ContentPanel extends FixedJPanel{
 
 
 
     /**
      * True when we need to re-add all the components.
      */
 
     private boolean reAddComponents = true;
 
 
 
 
     /**
      * Are we currently in vertical layout (width < height)? null for when we
      * don't know yet.
      */
 
     private boolean isVerticalLayout;
 
 
 
     /**
      * The panel containing all the components displaying information about the
      * game (horizontal mode).
      */
 
     private JPanel infoBox;
 
 
 
     /**
      * The top info panel in vertical mode.
      */
 
     private JPanel topInfoBox;
 
 
 
     /**
      * The bottom info panel in vertical mode.
      */
 
     private JPanel bottomInfoBox;
 
     
     /**
      * Panel for my buttons on BoardPanel. whp 2006
      */
     
     private JPanel infoBoxButtonPanel;
 
 
 
 
     /**
      * Adds all the components created by {@link #createComponents(Game)} to this
      * BoardPanel. The isFlipped flag specifies if the layout of the components
      * should be flipped because black should be displayed at the bottom. This method
      * can be called many times because the user may want to flip the board, so
      * it shouldn't do any one time initializations.
      */
 
     protected void addComponents(Game game, boolean flipped){
       this.setLayout(null); // See the doLayout() method
       //JPanel boardPanel = new JPanel();
       //boardPanel.setLayout(new BorderLayout());
       /*Border loweredBevelBorder = BorderFactory.createLoweredBevelBorder();
       Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
       Border compoundBorder = new CompoundBorder(emptyBorder, loweredBevelBorder);
       board.setBorder(compoundBorder);*/
       //boardPanel.add(board, BorderLayout.CENTER);
       this.add(board);
         
 
       infoBoxButtonPanel = new JPanel();
       infoBoxButtonPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
       infoBoxButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
       infoBoxButtonPanel.add(flipBoardButton);
 
       if (isVerticalLayout){
         topInfoBox = new JPanel();
         topInfoBox.setBorder(new EmptyBorder(5,5,5,5));
         topInfoBox.setLayout(new BoxLayout(topInfoBox, BoxLayout.X_AXIS));
 
         bottomInfoBox = new JPanel();
         bottomInfoBox.setBorder(new EmptyBorder(5,5,5,5));
         bottomInfoBox.setLayout(new BoxLayout(bottomInfoBox, BoxLayout.X_AXIS));
 
         whiteClock.setMaximumSize(whiteClock.getPreferredSize());
         blackClock.setMaximumSize(blackClock.getPreferredSize());
 
         int labelWidth = Math.max(whiteLabel.getPreferredSize().width,
                                     blackLabel.getPreferredSize().width);
         int labelHeight = Math.max(whiteLabel.getPreferredSize().height,
                                     blackLabel.getPreferredSize().height);
         whiteLabel.setMinimumSize(new Dimension(labelWidth, labelHeight));
         blackLabel.setMinimumSize(new Dimension(labelWidth, labelHeight));
         whiteLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
         blackLabel.setPreferredSize(new Dimension(labelWidth, labelHeight));
 
         fullscreenButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
         
         if (flipped){
           whiteLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
           whiteClock.setAlignmentY(Component.BOTTOM_ALIGNMENT);
 
           blackLabel.setAlignmentY(Component.TOP_ALIGNMENT);
           blackClock.setAlignmentY(Component.TOP_ALIGNMENT);
 
           topInfoBox.add(whiteLabel);
           topInfoBox.add(Box.createHorizontalStrut(10));
           topInfoBox.add(fullscreenButton);
           
           
             
 
           topInfoBox.add(infoBoxButtonPanel);
           
           topInfoBox.add(Box.createHorizontalGlue());
           topInfoBox.add(whiteClock);
 
           bottomInfoBox.add(blackLabel);
           bottomInfoBox.add(Box.createHorizontalGlue());
           bottomInfoBox.add(blackClock);
         }
         else{
           whiteLabel.setAlignmentY(Component.TOP_ALIGNMENT);
           whiteClock.setAlignmentY(Component.TOP_ALIGNMENT);
 
           blackLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
           blackClock.setAlignmentY(Component.BOTTOM_ALIGNMENT);
 
           topInfoBox.add(blackLabel);
           topInfoBox.add(Box.createHorizontalStrut(10));
           topInfoBox.add(fullscreenButton);
           
           topInfoBox.add(infoBoxButtonPanel);
           
           topInfoBox.add(Box.createHorizontalGlue());
           topInfoBox.add(blackClock);
           
           
 
           bottomInfoBox.add(whiteLabel);
           bottomInfoBox.add(Box.createHorizontalGlue());
           bottomInfoBox.add(whiteClock);
         }
 
         this.add(topInfoBox);
         this.add(bottomInfoBox);
       }
       else{
         whiteClock.setMaximumSize(new Dimension(Integer.MAX_VALUE, whiteClock.getPreferredSize().height));
         blackClock.setMaximumSize(new Dimension(Integer.MAX_VALUE, blackClock.getPreferredSize().height));
 
         // This could have been changed in vertical layout mode
         whiteLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
         whiteClock.setAlignmentY(Component.CENTER_ALIGNMENT);
         blackLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
         blackClock.setAlignmentY(Component.CENTER_ALIGNMENT);
         
         
         fullscreenButton.setAlignmentY(Component.CENTER_ALIGNMENT);
 
         JComponent whiteLabelBox = new JPanel(new BorderLayout());
         whiteLabelBox.add(whiteLabel, BorderLayout.CENTER);
 
         JComponent blackLabelBox = new JPanel(new BorderLayout());
         blackLabelBox.add(blackLabel, BorderLayout.CENTER);
         
         Container fullscreenButtonWrapper = 
           SquareLayout.createSquareContainer(fullscreenButton);
        Container flipBoardButtonWrapper =
                SquareLayout.createSquareContainer(flipBoardButton);
        JPanel boardPanelButtonsPanel = new JPanel();
        boardPanelButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        boardPanelButtonsPanel.add(flipBoardButtonWrapper);
        boardPanelButtonsPanel.add(fullscreenButtonWrapper);
 
         if (flipped){
          whiteLabelBox.add(boardPanelButtonsPanel, BorderLayout.EAST);
           whiteLabelBox.add(infoBoxButtonPanel, BorderLayout.WEST);
         }
         else{
          blackLabelBox.add(boardPanelButtonsPanel, BorderLayout.EAST);
           
           blackLabelBox.add(infoBoxButtonPanel, BorderLayout.WEST);
         }
         
         Box gameLabelBox = Box.createHorizontalBox();
         gameLabelBox.add(gameLabel);
         gameLabelBox.add(Box.createHorizontalGlue());
 
         JPanel upperBox = new JPanel(null);
         upperBox.setLayout(new BoxLayout(upperBox, BoxLayout.Y_AXIS));
         JPanel middleBox = new JPanel(null);
         middleBox.setLayout(new BoxLayout(middleBox, BoxLayout.Y_AXIS));
         JPanel bottomBox = new JPanel(null);
         bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.Y_AXIS));
 
         if (flipped){
           upperBox.add(whiteLabelBox);
           upperBox.add(Box.createVerticalStrut(10));
           upperBox.add(whiteClock);
           middleBox.add(Box.createVerticalStrut(10));
           middleBox.add(gameLabelBox);
           middleBox.add(Box.createVerticalStrut(10));
           middleBox.add(positionScrollBar);
           middleBox.add(Box.createVerticalStrut(5));
           middleBox.add(moveListTableScrollPane);
           middleBox.add(Box.createVerticalStrut(10));
           if (buttonPanel != null){
             middleBox.add(buttonPanel);
             middleBox.add(Box.createVerticalStrut(10));
           }
           bottomBox.add(blackClock);
           bottomBox.add(Box.createVerticalStrut(10));
           bottomBox.add(blackLabelBox);
         }
         else{
           upperBox.add(blackLabelBox);
           upperBox.add(Box.createVerticalStrut(10));
           upperBox.add(blackClock);
           middleBox.add(Box.createVerticalStrut(10));
           middleBox.add(gameLabelBox);
           middleBox.add(Box.createVerticalStrut(10));
           middleBox.add(positionScrollBar);
           middleBox.add(Box.createVerticalStrut(10));
           middleBox.add(moveListTableScrollPane);
           middleBox.add(Box.createVerticalStrut(10));
           if (buttonPanel != null){
             middleBox.add(buttonPanel);
             middleBox.add(Box.createVerticalStrut(10));
           }
           bottomBox.add(whiteClock);
           bottomBox.add(Box.createVerticalStrut(10));
           bottomBox.add(whiteLabelBox);
         }
         infoBox = new JPanel(null);
         infoBox.setBorder(new EmptyBorder(5, 5, 5, 5));
         infoBox.setLayout(new BorderLayout());
 
         infoBox.add(upperBox, BorderLayout.NORTH);
         infoBox.add(middleBox, BorderLayout.CENTER);
         infoBox.add(bottomBox, BorderLayout.SOUTH);
 
         this.add(infoBox);
       }
 
     }
 
 
 
     /**
      * Sets reAddComponents to true.
      */
 
     public void addNotify(){
       reAddComponents = true;
 
       super.addNotify();
     }
 
 
 
     /**
      * Lays out this BoardPanel.
      */
 
     public void doLayout(){
       Dimension size = this.getSize();
 
       boolean newIsVerticalLayout = size.width < size.height;
       if (reAddComponents || (isVerticalLayout != newIsVerticalLayout)){
         this.removeAll();
         reAddComponents = false;
         isVerticalLayout = newIsVerticalLayout;
         addComponents(game, isFlipped());
       }
 
       if (isVerticalLayout){
         int infoBoxHeight = (size.height - size.width) / 2;
         topInfoBox.setBounds(0, 0, size.width, infoBoxHeight);
         board.setBounds(0, infoBoxHeight, size.width, size.width);
         bottomInfoBox.setBounds(0, size.height - infoBoxHeight, size.width, infoBoxHeight);
       }
       else{
         board.setBounds(0, 0, size.height, size.height);
         infoBox.setBounds(size.height, 0, size.width-size.height, size.height);
       }
     }
 
 
   }
 
 
 
 
   /**
    * Returns true if the BoardPanel is flipped (black at bottom).
    */
 
   public final boolean isFlipped(){
     // Final because we directly set the isFlipped *variable* in init(Game) and
     // thus must not let anyone redefine the meaning of "flipped".
 
     return isFlipped;
   }
 
   /*
    * Toggles isFlipped variable. whp
    */
   public void toggleFlipped(){
     if (isFlipped){
         isFlipped = false;
         board.setFlipped(isFlipped);
         
     }
     else{
         isFlipped = true;
         board.setFlipped(isFlipped);
     }
     contentPanel.reAddComponents = true;
     contentPanel.revalidate();
   }
 
 
   /**
    * Sets the flipped state of this BoardPanel.
    */
 
   public void setFlipped(boolean b){
     if (isFlipped() != b){
       isFlipped = b;
       board.setFlipped(isFlipped);
       contentPanel.reAddComponents = true;
       contentPanel.revalidate();
     }
   }
 
 
 
   /**
    * Returns the Game displayed by this BoardPanel.
    */
 
   public Game getGame(){
     return game;
   }
 
 
 
 
   /**
    * Returns the game board.
    */
 
   public JinBoard getBoard(){
     return board;
   }
 
   
   
   
   /**
    * Returns the specified player's clock.
    */
    
   protected AbstractChessClock getClockForPlayer(Player player){
     return player.isWhite() ? whiteClock : blackClock;
   }
   
 
 
 
 
   /**
    * Updates the clock's activeness according to the current player to move on the
    * board.
    */
 
   protected void updateClockActiveness(){
     Player curPlayer = board.getPosition().getCurrentPlayer();
     AbstractChessClock curClock = getClockForPlayer(curPlayer);
     AbstractChessClock oppClock = getClockForPlayer(curPlayer.getOpponent());
     
     curClock.setActive(true);
     oppClock.setActive(false);
   }
   
   
   
   /**
    * Plays the sound corresponding to the specified event.
    */
    
   public void playSound(String eventName){
     boardManager.playSound(eventName);
   }
 
 
 
 
   /**
    * Returns if there is currently a move enRoute. This simply checks that the
    * <code>moveInRoute</code> variable is non-null.
    */
 
   private boolean isMoveEnRoute(){
     return moveEnRoute != null;
   }
 
 
 
 
   /**
    * Sets the queued move to the specified Move. Null is a valid value, which
    * clears the queued move.
    */
 
   private void setQueuedMove(Move move){
     if (queuedMove != null)
       board.setShaded(queuedMove.getEndingSquare(), false);
 
     queuedMove = move;
     if (queuedMove != null)
       board.setShaded(queuedMove.getEndingSquare(), true);
 
     board.setEditable((queuedMove == null) && (displayedMoveNumber == madeMoves.size()));
   }
 
 
 
 
   /**
    * Sets the currently displayed move to the specified move.
    */
 
   private void setDisplayedMove(int moveNum){
     if ((moveNum < 0) || (moveNum > madeMoves.size()))
       throw new IllegalArgumentException("displayed move number out of range");
 
     displayedMoveNumber = moveNum;
 
     if (displayedMoveNumber != madeMoves.size())
       board.setEditable(false);
     else if (moveSendingMode == BoardManager.PREMOVE_MOVE_SENDING_MODE)
       board.setEditable(queuedMove == null);
     else if (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE)
       board.setEditable(isUserTurn());
     else if (moveSendingMode == BoardManager.PREDRAG_MOVE_SENDING_MODE)
       board.setEditable(true);
     else
       throw new IllegalStateException("Unrecognized move sending mode: "+moveSendingMode);
   }
 
 
 
 
   /**
    * GameListener implementation. Note that this method is never actually called
    * because a BoardPanel is created (by the BoardManager) in response to a game
    * start event, so when we register for game events, the game start event has
    * already been dispatched.
    */
 
   public void gameStarted(GameStartEvent evt){}
 
 
 
   /**
    * GameListener implementation. Makes the appropriate move on the board.
    */
 
   public void moveMade(MoveMadeEvent evt){
     if (evt.getGame() != game)
       return;
 
     Move move = evt.getMove();
     
     // Is this an echo to the current move en route?
     boolean moveEnRouteEcho = (moveEnRoute != null) &&
       Utilities.areEqual(move.getStartingSquare(), moveEnRoute.getStartingSquare()) &&
       Utilities.areEqual(move.getEndingSquare(), moveEnRoute.getEndingSquare()) &&
       Utilities.areEqual(move.getPlayer(), moveEnRoute.getPlayer());
 
     boolean shouldUpdateBoard = true; 
 
     // The user is looking at a position other than the last one, so we don't
     // want to update the board when a new move arrives.
     if (displayedMoveNumber != madeMoves.size())
       shouldUpdateBoard = false;
     
     madeMoves.add(move);
     realPosition.makeMove(move);
 
     // This is not the server echoeing a move made on the board
     // Note that this may still be a move done by the user (from the console).
     if (!isMoveEnRoute()){
       if (evt.isNew())
         playAudioClipForMove(move);
 
       if (shouldUpdateBoard){
         isBoardPositionUpdating = true;
         board.getPosition().copyFrom(realPosition);
         isBoardPositionUpdating = false;
       }
     }
 
     if (shouldUpdateBoard){
       setDisplayedMove(madeMoves.size());
       updateMoveHighlighting(isMoveEnRoute());
     }
 
     // queuedMove.getPlayer() == realPosition.getCurrentPlayer() makes sure
     // that we only send the queued on the correct move, not when getting *any* response
     if ((queuedMove != null) && (queuedMove.getPlayer() == realPosition.getCurrentPlayer())){
       Move premove = null;
       try{
         premove = game.getVariant().createMove(realPosition, queuedMove);
         if (!checkLegality(realPosition, premove))
           premove = null;
       } catch (IllegalArgumentException e){}
       if (premove == null){ // Illegal premove
         isBoardPositionUpdating = true;
         board.getPosition().copyFrom(realPosition);
         isBoardPositionUpdating = false;
         setQueuedMove(null);
         playSound("IllegalMove");
       }
       else{
         UserMoveEvent evt2 = new UserMoveEvent(this, premove);
         isBoardPositionUpdating = true;
         board.getPosition().copyFrom(realPosition);
         board.getPosition().makeMove(premove);
         isBoardPositionUpdating = false;
         moveEnRoute = premove;
         setQueuedMove(null);
         fireUserMadeMove(evt2);
       }
     }
     else if (moveEnRouteEcho)
       moveEnRoute = null;
 
     updateClockActiveness();
     addMoveToListTable(move);
   }
 
 
 
 
   /**
    * If the specified move is legal in the specified position, this method
    * returns <code>true</code>. If the specified move is illegal, it may return
    * <code>false</code>. It's not meant as a complete move legality check,
    * instead, it (currently) only detects some obviously illegal moves so that
    * they can be rejected immediately, instead of wasting time by sending them
    * to the server. With time, more complex (and eventually complete) move
    * legality checking will be added. 
    */
 
   protected boolean checkLegality(Position pos, Move move){
     if (move instanceof ChessMove){
       ChessMove cmove = (ChessMove)move;
       
       // Trying to move a piece that isn't your own.
       if (pos.getCurrentPlayer() != cmove.getPlayer())
         return false;
       
       // Trying to capture your own piece
       if (cmove.isCapture() && (cmove.getCapturedPiece().getPlayer() == pos.getCurrentPlayer()))
         return false;
     }
 
     return true;
   }
 
 
 
 
   /**
    * GameListener implementation. Sets the appropriate position on the board.
    */
 
   public void positionChanged(PositionChangedEvent evt){
     if (evt.getGame() != game)
       return;
 
     madeMoves.clear();
     realPosition.copyFrom(evt.getPosition());
 
     isBoardPositionUpdating = true;
     board.getPosition().copyFrom(realPosition);
     isBoardPositionUpdating = false;
 
     setDisplayedMove(0);
 
     updateMoveHighlighting(false);
 
     moveEnRoute = null;   // We shouldn't keep state between 
     setQueuedMove(null);  // such drastic position changes
 
     updateClockActiveness();
     updateMoveListTable();
   }
 
 
 
 
   /**
    * GameListener implementation. Reverts the position on the board to the position
    * that occured on it "the amount of moves taken back" moves ago.
    */
 
   public void takebackOccurred(TakebackEvent evt){
     if (evt.getGame()!=game)
       return;
 
     int takebackCount = evt.getTakebackCount();
     int numMadeMoves = madeMoves.size()-takebackCount;
     for (int i = madeMoves.size()-1; i >= numMadeMoves; i--)
       madeMoves.remove(i);
 
     realPosition.copyFrom(game.getInitialPosition());
     for (int i = 0; i < numMadeMoves; i++)
       realPosition.makeMove((Move)madeMoves.get(i));
 
     moveEnRoute = null;
     setQueuedMove(null);
 
     // Try not to change the board if possible. If, however we were displaying the position
     // after a move that was taken back, we have to update the board.
     if (displayedMoveNumber >= madeMoves.size()){
       isBoardPositionUpdating = true;
       board.getPosition().copyFrom(realPosition);
       isBoardPositionUpdating = false;
       setDisplayedMove(madeMoves.size());
       updateMoveHighlighting(false);
     }
 
     updateClockActiveness();
     updateMoveListTable();
   }
 
 
 
 
   /**
    * GameListener implementation. Returns the position on the board to its current
    * "real" state.
    */
 
   public void illegalMoveAttempted(IllegalMoveEvent evt){
     if (evt.getGame() != game)
       return;
 
     if (!isMoveEnRoute()) // We didn't make this move. 
       return;             // It could've been sent by one of the other plugins.
 
     moveEnRoute = null;
     setQueuedMove(null);
 
     isBoardPositionUpdating = true;
     board.getPosition().copyFrom(realPosition);
     isBoardPositionUpdating = false;
 
     // Subtract the time spent on sending the move from the player's clock
     // and restart the it.
     AbstractChessClock playersClock = getClockForPlayer(board.getPosition().getCurrentPlayer());
     playersClock.setTime((int)(playersClock.getTime() - (System.currentTimeMillis() - sentMoveTimestamp)));
     playersClock.setRunning(true);
     
     updateClockActiveness();
     setDisplayedMove(madeMoves.size());
     updateMoveListTable();
   }
 
 
 
 
   /**
    * GameListener implementation. Adjusts the clock time and activeness.
    */
 
   public void clockAdjusted(ClockAdjustmentEvent evt){
     if (evt.getGame() != game)
       return;
 
     Player player = evt.getPlayer();
     int time = evt.getTime();
     boolean isRunning = evt.isClockRunning();
     AbstractChessClock clock = player.equals(Player.WHITE_PLAYER) ?
       whiteClock : blackClock;
     
 
     clock.setTime(time);
     clock.setRunning(isRunning);
   }
   
   
   
 
   /**
    * GameListener implementation.
    */
 
   public void boardFlipped(BoardFlipEvent evt){
     if (evt.getGame()!=game)
       return;
 
     setFlipped(evt.isFlipped());
   }
 
 
 
 
   /**
    * GameListener implementation.
    */
 
   public void offerUpdated(OfferEvent evt){
 
   }
 
 
 
   /**
    * GameListener implementation.
    */
 
   public void gameEnded(GameEndEvent evt){
     if (evt.getGame()!=game)
       return;
 
     whiteClock.setRunning(false);
     blackClock.setRunning(false);
 
     setInactive();
   }
 
 
 
 
 
   /**
    * Plays the audio clip appropriate for the given <code>Move</code>.
    */
 
   protected void playAudioClipForMove(Move move){
     if (move instanceof ChessMove){
       ChessMove cMove = (ChessMove)move;
       if (cMove.isCapture())
         playSound("Capture");
       else if (cMove.isCastling())
         playSound("Castling");
       else
         playSound("Move");
     }
     else
       playSound("Move");
   }
 
 
 
 
 
   /**
    * This is called when the user makes a move on the board (MoveListener implementation).
    */
 
   public void moveMade(MoveEvent evt){
     if (isBoardPositionUpdating)
       return;
 
     Position source = evt.getPosition();
     Move move = evt.getMove();
 
     if (source == board.getPosition()){
       playAudioClipForMove(move);
       if (game.isPlayed() && (moveSendingMode == BoardManager.PREMOVE_MOVE_SENDING_MODE) &&
           (isMoveEnRoute() || !isUserTurn()))
         setQueuedMove(move);
       else{
         if ((!game.isPlayed()) || checkLegality(realPosition, move)){
           UserMoveEvent evt2 = new UserMoveEvent(this, move);
           fireUserMadeMove(evt2);
           moveEnRoute = evt.getMove();
           
           // Stop the clock of the player who moved
           getClockForPlayer(move.getPlayer()).setRunning(false);
           
           // Remember the time when it was stopped because if the move is illegal
           // we will need to restart the clock with the correct amount of time
           // (including the time the move spent in transit).
           sentMoveTimestamp = System.currentTimeMillis();
           
           // Update clock activeness
           updateClockActiveness();
   
           if (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE)
             board.setEditable(false);
         }
         else{ // Illegal move attempted
           
           // We do this in invokeLater because moveMade is called already in response
           // to a position update, and updating it during an update breaks things
           // (like the board's ability to know which pieces to repaint).
           SwingUtilities.invokeLater(new Runnable(){
             public void run(){
               isBoardPositionUpdating = true;
               board.getPosition().copyFrom(realPosition);
               isBoardPositionUpdating = false;
               playSound("IllegalMove");
             }
           });
         }
       }
     }
   }
 
 
 
 
   /**
    * Returns true if this is currently the user's turn to move. If this is an
    * observed game, returns false. If this is a game examined by the user returns
    * true. If this is a game played by the user returns whether it's currently
    * the user's turn.
    */
 
   protected boolean isUserTurn(){
     if (game.getGameType() == Game.OBSERVED_GAME)
       return false;
     else if (game.getGameType() == Game.ISOLATED_BOARD)
       return false;
     else{ // MY_GAME
       if (game.isPlayed()){
         Player userPlayer = game.getUserPlayer();
         return realPosition.getCurrentPlayer().equals(userPlayer);
       }
       else
         return true;
     }
   }
 
 
 
   /**
    * This method is called when either the row or column selection in the moveListTable
    * changes. Changes the board position to the position after the selected move.
    */
 
   protected void moveListTableSelectionChanged(){
     if (settingMoveListTableSelection)
       return;
 
     if (isMoveListTableSelectionUpdating)
       return;
 
     int row = moveListTable.getSelectedRow();
     int column = moveListTable.getSelectedColumn();
 
     if ((row == -1) || (column == -1))
       return;
     
     isMoveListTableSelectionUpdating = true;
 
     try{
       boolean isFirstMoveBlack = ((Move)madeMoves.get(0)).getPlayer().isBlack();
   
       int moveNum = column + row*2;
       if (isFirstMoveBlack && (moveNum > 0))
         moveNum--;
   
       if (moveNum == madeMoves.size() + 1) // The user pressed the last empty cell
         moveNum--;
   
       if (moveNum > madeMoves.size()) // Shouldn't happen
         throw new IllegalStateException();
   
       Position pos = game.getInitialPosition();
       for (int i = 0; i < moveNum; i++){
         Move move = (Move)madeMoves.get(i);
         pos.makeMove(move);
       }
   
       board.clearShaded();
       
       if ((moveNum == madeMoves.size()) && (queuedMove != null)){
         pos.makeMove(queuedMove);
         board.setShaded(queuedMove.getEndingSquare(), true);
       }
   
       isBoardPositionUpdating = true;
       board.getPosition().copyFrom(pos);
       isBoardPositionUpdating = false;
       setDisplayedMove(moveNum);
   
       if (!isPositionScrollBarUpdating)
         positionScrollBar.setValues(displayedMoveNumber, 1, 0, madeMoves.size() + 1); 
   
       board.setEditable(displayedMoveNumber == madeMoves.size());
   
       updateMoveHighlighting(false);
   
       SwingUtilities.invokeLater(new MoveListScrollBarUpdater());
     } finally{
         isMoveListTableSelectionUpdating = false;
       }
   }
 
 
 
   /**
    * AdjustmentListener implementation. Listens to events from the positionScrollBar
    * and updates the position on the board accordingly.
    */
 
   public void adjustmentValueChanged(AdjustmentEvent evt){
     Object source = evt.getSource();
     if (source == positionScrollBar){
       if (isPositionScrollBarUpdating)
         return;
       
       isPositionScrollBarUpdating = true;
 
       if (madeMoves.size() > 0){
         int moveNum = positionScrollBar.getValue();
 
         boolean isFirstMoveBlack = ((Move)madeMoves.get(0)).getPlayer().isBlack();
         int visualMoveNumber = isFirstMoveBlack ? moveNum + 1 : moveNum;
         int row = (visualMoveNumber-1)/2;
         int column = (visualMoveNumber == 0) ? 0 : 2 - (visualMoveNumber%2);
 
         if (!isMoveListTableSelectionUpdating){
           setMoveListTableSelection(row, column);
         }
       }
 
       isPositionScrollBarUpdating = false;
     }
   }
 
 
 
 
   /**
    * <code>PropertyChangeListener</code> implementation.
    */
 
   public void propertyChange(PropertyChangeEvent evt){
     Object src = evt.getSource();
     String propertyName = evt.getPropertyName();
     if (src == boardManager){
       if ("moveInputStyle".equals(propertyName))
         board.setMoveInputStyle(boardManager.getMoveInputStyle());
       else if ("pieceFollowsCursor".equals(propertyName))
         board.setPieceFollowsCursor(boardManager.isPieceFollowsCursor());
       else if ("highlightMadeMoveSquares".equals(propertyName))
         board.setHighlightMadeMoveSquares(boardManager.isHighlightMadeMoveSquares());
       else if ("madeMoveSquaresHighlightColor".equals(propertyName))
         board.setMadeMoveSquaresHighlightColor(boardManager.getMadeMoveSquaresHighlightColor());
       else if ("moveHighlightingStyle".equals(propertyName))
         board.setMoveHighlightingStyle(boardManager.getMoveHighlightingStyle());
       else if ("autoPromote".equals(propertyName))
         board.setManualPromote(!boardManager.isAutoPromote());
       else if ("moveHighlightingColor".equals(propertyName))
         board.setMoveHighlightingColor(boardManager.getMoveHighlightingColor());
       else if ("highlightingOwnMoves".equals(propertyName))
         highlightOwnMoves = boardManager.isHighlightingOwnMoves();
       else if ("moveSendingMode".equals(propertyName)){
         moveSendingMode = boardManager.getMoveSendingMode();
 
         if (displayedMoveNumber == madeMoves.size()){
           if ((moveSendingMode == BoardManager.PREMOVE_MOVE_SENDING_MODE) || 
               (moveSendingMode == BoardManager.PREDRAG_MOVE_SENDING_MODE))
             board.setEditable(true);
           else if (moveSendingMode == BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE)
             board.setEditable(isUserTurn());
           else
             throw new IllegalStateException("Unrecognized move sending mode: "+moveSendingMode);
         }
       }
       else if ("coordsDisplayStyle".equals(propertyName))
         board.setCoordsDisplayStyle(boardManager.getCoordsDisplayStyle());
       else if ("coordsDisplayColor".equals(propertyName))
         board.setCoordsDisplayColor(boardManager.getCoordsDisplayColor());
       else if ("pieceSet".equals(propertyName) ||
                "whitePieceColor".equals(propertyName) ||
                "blackPieceColor".equals(propertyName) ||
                "whiteOutlineColor".equals(propertyName) ||
                "blackOutlineColor".equals(propertyName)){
         board.setPiecePainter(boardManager.getPiecePainter());
       }
       else if ("boardPattern".equals(propertyName) ||
                "lightSquareColor".equals(propertyName) ||
                "darkSquareColor".equals(propertyName)){
         board.setBoardPainter(boardManager.getBoardPainter());
       }
 
     }
     else if (src == game){
       gameLabel.setText(createGameLabelText(game));
       if ("whiteName".equals(propertyName))
         whiteLabel.setText(createWhiteLabelText(game));
       else if ("blackName".equals(propertyName))
         blackLabel.setText(createBlackLabelText(game));
       else if ("gameType".equals(propertyName) || "played".equals(propertyName)){
         configureBoardFromGame(game, board);
         board.setEditable(isUserTurn() || (moveSendingMode != BoardManager.LEGAL_CHESS_MOVE_SENDING_MODE));
         buttonPanel = createButtonPanel(game);
         gameLabel = createGameLabel(game);
         contentPanel.reAddComponents = true;
         contentPanel.revalidate();
       }
       
         
       // implement the rest of the properties.
     }
   }
 
 
 
 
   /**
    * Returns true if this BoardPanel is active, false otherwise.
    */
 
   public boolean isActive(){
     return isActive;
   }
 
 
 
 
   /**
    * Makes the BoardPanel inactive. Calling this method will make the BoardPanel
    * stop notifying about moves made by the user. It will also change the
    * move input mode to {@link #ALL_PIECES_MOVE}.
    */
 
   public void setInactive(){
     this.isActive = false;
     board.getPosition().removeMoveListener(this);
     board.setMoveInputMode(JinBoard.ALL_PIECES_MOVE);
     board.setEditable(true);
 
     whiteClock.setRunning(false);
     blackClock.setRunning(false);
     
     if (whiteClock instanceof JChessClock){
       ((JChessClock)whiteClock).setActiveForeground(Color.lightGray);
       ((JChessClock)whiteClock).setInactiveForeground(Color.gray);
     }
     if (blackClock instanceof JChessClock){
       ((JChessClock)blackClock).setActiveForeground(Color.lightGray);
       ((JChessClock)blackClock).setInactiveForeground(Color.gray);
     }
   }
 
 
 
   /**
    * This method is called by the <code>BoardManager</code> when this
    * <code>BoardPanel</code> is no longer required.
    */
 
   public void done(){
     boardManager.removePropertyChangeListener(this); 
   }
   
   
   
   /**
    * Before we are removed, if we are in fullscreen mode, we must restore normal
    * mode.
    */
    
   public void removeNotify(){
     if (fullscreenPanel.getFullscreenModeModel().isOn())
       fullscreenPanel.getFullscreenModeModel().set(false);
       
     
     super.removeNotify();
   }
 
 
 
 
 
 }
