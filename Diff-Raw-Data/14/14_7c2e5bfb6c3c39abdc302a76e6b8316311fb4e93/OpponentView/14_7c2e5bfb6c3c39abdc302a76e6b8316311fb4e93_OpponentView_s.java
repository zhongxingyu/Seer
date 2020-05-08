 
 package phoenix.game.battleship.views;
 
 import phoenix.game.battleship.R;
 import phoenix.game.battleship.actors.EntityConstants;
 import phoenix.game.battleship.actors.Game;
 import phoenix.game.battleship.actors.Player;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.LinearLayout;
 
 public class OpponentView extends LinearLayout implements OnOpponentControlActionListener {
 
     private BoardView boardView;
 
     private OpponentControlView opponentControlView;
 
     private Game game;
 
     private OnPlayerEventListener onPlayerEventListener;
 
     public OpponentView(Context context) {
         super(context);
         init();
     }
 
     public OpponentView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public OpponentView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     private void init() {
         inflate(getContext(), R.layout.opponent_view, this);
         setBoardView((BoardView) findViewById(R.id.boardView));
         setOpponentControlView((OpponentControlView) findViewById(R.id.opponentControlView));
         getOpponentControlView().setOnOpponentControlActionListener(this);
     }
 
     public BoardView getBoardView() {
         return boardView;
     }
 
     public void setBoardView(BoardView boardView) {
         this.boardView = boardView;
     }
 
     public OpponentControlView getOpponentControlView() {
         return opponentControlView;
     }
 
     public void setOpponentControlView(OpponentControlView opponentControlView) {
         this.opponentControlView = opponentControlView;
     }
 
     public void setOnOpponentControlActionListener(
             OnOpponentControlActionListener onOpponentControlActionListener) {
         getOpponentControlView()
                 .setOnOpponentControlActionListener(onOpponentControlActionListener);
 
     }
 
     public OnOpponentControlActionListener getOnOpponentControlActionListener() {
         return getOpponentControlView().getOnOpponentControlActionListener();
 
     }
 
     public Game getGame() {
         return game;
     }
 
     public void setGame(Game game) {
         this.game = game;
         initGame(game);
     }
 
     public OnPlayerEventListener getOnPlayerEventListener() {
         return onPlayerEventListener;
     }
 
     public void setOnPlayerEventListener(OnPlayerEventListener onPlayerEventListener) {
         this.onPlayerEventListener = onPlayerEventListener;
     }
 
     public void initGame(Game game) {
         getBoardView().initGame(game);
     }
 
     public void loadPlayerPerspective() {
         setBoardForOpponent(getGame().getPerspectivePlayer());
     }
 
     public void setBoardForOpponent(Player player) {
         Player opponentPlayer = getGame().getOpponentPlayerOf(player);
         getOpponentControlView().setPlayerName(opponentPlayer.getName());
         // hiding board details from actual player
         getBoardView().setBoard(opponentPlayer.getBoard(), false);
         getBoardView().setEnabled(
                opponentPlayer.getState() != EntityConstants.STATE_SETUP
                         && getGame().getPlayerTurn() == player.getPlayerId());
         // getOnOpponentEventListener().refreshPlayerBoard();
     }
 
     @Override
     public void onClickLeft() {
         if (getOnPlayerEventListener() != null) {
             getOnPlayerEventListener().switchToPlayerBoard();
         }
     }
 
     public void update() {
         loadPlayerPerspective();
         if (getOnPlayerEventListener() != null) {
             getOnPlayerEventListener().refresh();
         }
     }
 
     public void refresh() {
         getBoardView().refresh();
         updateDamage();
     }
 
     public void updateDamage() {
         String[] damages = getGame().getOpponentPlayerOf(getGame().getPerspectivePlayer())
                 .getBoard().getDamages();
         for (int i = 0; i < damages.length; i++) {
             getOpponentControlView().updateDamage(i, damages[i]);
         }
 
     }
 
     public void setControlsForTablet(boolean isTablet) {
         if (isTablet) {
             getOpponentControlView().getLeftButton().setVisibility(View.GONE);
         } else {
             getOpponentControlView().getLeftButton().setVisibility(View.VISIBLE);
         }
     }
 }
