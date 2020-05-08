 package lt.ltech.numbers.android;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.UUID;
 
 import lt.ltech.numbers.GameException;
 import lt.ltech.numbers.android.entity.Stats;
 import lt.ltech.numbers.android.game.GameType;
 import lt.ltech.numbers.android.log.Logger;
 import lt.ltech.numbers.android.persistence.StatsDao;
 import lt.ltech.numbers.android.persistence.mapping.StatsMapper;
 import lt.ltech.numbers.game.Answer;
 import lt.ltech.numbers.game.GameConfiguration;
 import lt.ltech.numbers.game.GameState;
 import lt.ltech.numbers.game.GameStep;
 import lt.ltech.numbers.game.Number;
 import lt.ltech.numbers.game.Round;
 import lt.ltech.numbers.player.ArtificialPlayer;
 import lt.ltech.numbers.player.DefaultPlayer;
 import lt.ltech.numbers.player.Player;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class GameActivity extends Activity {
     public static final String PLAYER = "player";
     public static final String GAME_TYPE = "gameType";
     private static final String GAME_STATE = "gameState";
     private static final String HUMAN_PLAYER = "humanPlayer";
     private static final String COMPUTER_PLAYER = "computerPlayer";
     private static final String ARTIFICIAL_PLAYERS = "artificialPlayers";
     private static final String GUESS_TEXT = "guessText";
 
     private static final Logger logger = new Logger(
             GameActivity.class.getName());
 
     private ScrollView scrollView;
     private LinearLayout playerColumns;
     private LinearLayout guessListLeft;
     private LinearLayout guessListRight;
     private LinearLayout lowerButtonLayout;
     private LinearLayout upperButtonLayout;
     private Button guessButton;
     private Button[] buttonArray;
     private Button clearButton;
     private EditText guessText;
     private TextView hintText;
 
     private GameState gameState;
     private GameType gameType;
 
     private Player humanPlayer;
     private Player computerPlayer;
     private HashMap<Player, ArtificialPlayer> artificialPlayers;
 
     private TextView numberLeft;
     private TextView numberRight;
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putSerializable(GAME_STATE, this.gameState);
         outState.putSerializable(GAME_TYPE, this.gameType);
         outState.putSerializable(HUMAN_PLAYER, this.humanPlayer);
         outState.putSerializable(COMPUTER_PLAYER, this.computerPlayer);
         outState.putSerializable(ARTIFICIAL_PLAYERS, this.artificialPlayers);
         outState.putString(GUESS_TEXT, this.guessText.getText().toString());
     }
 
     @Override
     public void onCreate(Bundle savedState) {
         super.onCreate(savedState);
         this.setContentView(R.layout.game);
         this.guessButton = (Button) this.findViewById(R.id.gameGuessButton);
 
         this.scrollView = (ScrollView) this.findViewById(R.id.scrollView);
         this.playerColumns = (LinearLayout) this
                 .findViewById(R.id.playerColumns);
 
         LayoutInflater inflater = (LayoutInflater) this
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         this.guessListLeft = (LinearLayout) inflater.inflate(
                 R.layout.player_column, null);
         this.guessListLeft.setLayoutParams(new LayoutParams(
                 LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
         this.playerColumns.addView(this.guessListLeft, 0);
 
         this.guessListRight = (LinearLayout) inflater.inflate(
                 R.layout.player_column, null);
         this.guessListRight.setLayoutParams(new LayoutParams(
                 LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
         this.playerColumns.addView(this.guessListRight, 1);
 
         this.hintText = (TextView) this.findViewById(R.id.hintText);
 
         this.guessText = (EditText) this.findViewById(R.id.guessText);
         this.guessText.setEnabled(false);
         this.guessText.setClickable(false);
         this.guessText.setFocusable(false);
 
         this.guessButton.setOnClickListener(this.getOnClickListener());
 
         this.upperButtonLayout = (LinearLayout) this
                 .findViewById(R.id.gameButtonLayoutUpper);
         this.lowerButtonLayout = (LinearLayout) this
                 .findViewById(R.id.gameButtonLayoutLower);
 
         this.buttonArray = new Button[10];
         for (int i = 0; i < 5; i++) {
             Button b = new Button(this);
             b.setText(String.valueOf(i));
             b.setOnClickListener(this.getButtonOnClickListener(i));
             LayoutParams lp = new LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
             this.upperButtonLayout.addView(b, lp);
             this.buttonArray[i] = b;
         }
 
         for (int i = 5; i < 10; i++) {
             Button b = new Button(this);
             b.setText(String.valueOf(i));
             b.setOnClickListener(this.getButtonOnClickListener(i));
             LayoutParams lp = new LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
             this.lowerButtonLayout.addView(b, lp);
             this.buttonArray[i] = b;
         }
 
         this.clearButton = (Button) this.findViewById(R.id.gameClearButton);
         this.clearButton.setOnClickListener(this.getClearOnClickListener());
 
         this.numberLeft = (TextView) this.findViewById(R.id.gameNumberLeft);
         this.numberRight = (TextView) this.findViewById(R.id.gameNumberRight);
 
         if (savedState != null) {
             this.gameState = (GameState) savedState.getSerializable(GAME_STATE);
             this.gameType = (GameType) savedState.getSerializable(GAME_TYPE);
             this.humanPlayer = (Player) savedState
                     .getSerializable(HUMAN_PLAYER);
             this.computerPlayer = (Player) savedState
                     .getSerializable(COMPUTER_PLAYER);
             this.artificialPlayers = (HashMap<Player, ArtificialPlayer>) savedState
                     .getSerializable(ARTIFICIAL_PLAYERS);
             boolean gsnn = this.gameState != null;
             boolean hpnn = this.humanPlayer != null;
             boolean cpnn = this.computerPlayer != null;
             boolean apnn = this.artificialPlayers != null;
            if (gsnn && hpnn && cpnn && apnn) {
                 boolean cpap = this.artificialPlayers.get(this.computerPlayer) != null;
                 boolean hpgs = this.gameState.containsPlayer(this.humanPlayer);
                 boolean cpgs = this.gameState
                         .containsPlayer(this.computerPlayer);
                 if (cpap && hpgs && cpgs) {
                     this.resumeGame();
                 } else {
                     this.startNewGame();
                 }
             } else {
                 this.startNewGame();
             }
 
             String guess = savedState.getString(GUESS_TEXT);
             if (guess != null) {
                 this.guessText.setText(guess);
                 this.resetButtonState();
             }
         } else {
             this.startNewGame();
         }
 
         ((TextView) this.findViewById(R.id.gameNameLeft))
                 .setText(this.humanPlayer.getName());
         ((TextView) this.findViewById(R.id.gameNameRight))
                 .setText(this.computerPlayer.getName());
     }
 
     private void startNewGame() {
         this.gameState = new GameState();
 
         Bundle extras = this.getIntent().getExtras();
         this.gameType = (GameType) extras.getSerializable(GAME_TYPE);
         this.humanPlayer = (Player) extras.getSerializable(PLAYER);
 
         this.computerPlayer = new Player(UUID.randomUUID(), "Computer player");
         this.computerPlayer.setId(2l);
 
         this.artificialPlayers = new HashMap<Player, ArtificialPlayer>();
         this.artificialPlayers.put(this.computerPlayer, new DefaultPlayer(
                 this.computerPlayer));
 
         try {
             this.gameState.addPlayer(humanPlayer);
             this.gameState.addPlayer(computerPlayer);
         } catch (GameException ge) {
             logger.e(ge.getMessage());
         }
 
         this.updateHint();
     }
 
     private void resumeGame() {
         if (this.humanPlayer.getNumber() != null) {
             this.numberLeft.setText(this.humanPlayer.getNumber().toString());
             this.guessButton.setText(this.getString(R.string.game_make_guess));
 
             if (this.computerPlayer.getNumber() != null) {
                 this.numberRight.setText(this.computerPlayer.getNumber()
                         .toString());
             } else {
                 this.callArtifialPlayers();
             }
 
             for (Round round: this.gameState.getRounds()) {
                 for (Player player: round.getAnswers().keySet()) {
                     Number guess = round.getGuesses().get(player);
                     Answer answer = round.getAnswers().get(player);
                     if (player.equals(this.humanPlayer)) {
                         this.addGuessAndAnswer(this.guessListLeft, guess,
                                 answer);
                     } else if (player.equals(this.computerPlayer)) {
                         this.addGuessAndAnswer(this.guessListRight, guess,
                                 answer);
                     }
                 }
             }
         }
 
         this.updateHint();
     }
 
     private OnClickListener getOnClickListener() {
         final GameActivity a = this;
         final LinearLayout gl = this.guessListLeft;
         final EditText gt = this.guessText;
         return new OnClickListener() {
             @Override
             public void onClick(View v) {
                 String text = gt.getText().toString();
 
                 Number guess = null;
                 try {
                     String[] splitLine = text.split("");
                     Integer[] guessArray = new Integer[splitLine.length - 1];
                     for (int i = 1; i < splitLine.length; i++) {
                         guessArray[i - 1] = Integer.valueOf(splitLine[i]);
                     }
                     guess = new Number(Arrays.asList(guessArray));
                 } catch (NumberFormatException nfe) {
                 } catch (GameException ge) {
                     logger.e(ge.getMessage());
                 }
                 if (guess == null) {
                     return;
                 } else {
                     a.guessText.setText("");
                     a.resetButtonState();
                 }
 
                 LinearLayout ll = null;
                 Answer answer = null;
                 GameStep gs = a.gameState.getGameStep();
                 final GameStep.Type type = gs.type();
                 switch (type) {
                     case SET_NUMBER:
                         try {
                             a.gameState.setNumber(a.humanPlayer, guess);
                             logger.i("%s has chosen %s", a.humanPlayer,
                                     a.humanPlayer.getNumber());
                             a.numberLeft.setText(a.humanPlayer.getNumber()
                                     .toString());
                             a.guessButton.setText(a
                                     .getString(R.string.game_make_guess));
                         } catch (GameException ge) {
                             logger.e(ge.getMessage());
                         }
                         break;
                     case GUESS:
                         try {
                             ll = gl;
                             a.gameState.guess(a.humanPlayer, guess);
                             answer = a.gameState.getLastRound().getAnswers()
                                     .get(a.humanPlayer);
                         } catch (GameException ge) {
                             logger.e(ge.getMessage());
                         }
                         break;
                 }
 
                 if (ll != null) {
                     a.addGuessAndAnswer(ll, guess, answer);
                 }
 
                 a.callArtifialPlayers();
 
                 if (a.isGameOver()) {
                     Player winner = a.gameState.getWinner();
 
                     updateStats(a.humanPlayer, a.humanPlayer.equals(winner),
                             false);
 
                     AlertDialog.Builder b = new AlertDialog.Builder(a);
                     int turns = a.gameState.getRounds().size();
                     b.setTitle("Game Over");
                     String message = a.getString(R.string.game_winner);
                     b.setMessage(String.format(message, winner.getName(), turns));
                     b.setNeutralButton(getString(R.string.ok),
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int which) {
                                     a.finish();
                                 }
                             });
                     b.show();
                 }
             }
         };
     }
 
     private void updateStats(Player player, boolean isWinner, boolean isDraw) {
         StatsDao statsDao = new StatsDao(this);
         Stats stats = statsDao.findByPlayer(player);
         stats.setGamesPlayed(stats.getGamesPlayed() + 1);
         if (isWinner) {
             stats.setGamesWon(stats.getGamesWon() + 1);
         }
         if (isDraw) {
             stats.setGamesDrawn(stats.getGamesDrawn() + 1);
         }
         int averageGuesses = stats.getAverageGuesses();
         int correctGuesses = stats.getCorrectGuesses();
         if (isWinner || isDraw) {
             int guessCount = this.gameState.getRounds().size();
             int totalGuesses = averageGuesses * correctGuesses + guessCount;
             correctGuesses++;
             averageGuesses = totalGuesses / correctGuesses;
             stats.setCorrectGuesses(correctGuesses);
             stats.setAverageGuesses(averageGuesses);
         }
         statsDao.update(stats, stats.getId(), new StatsMapper());
     }
 
     private void callArtifialPlayers() {
         Player p = null;
         ArtificialPlayer ap = null;
         while ((ap = this.artificialPlayers.get(p = this.gameState
                 .getGameStep().player())) != null) {
             final GameStep.Type t = this.gameState.getGameStep().type();
             switch (t) {
                 case SET_NUMBER:
                     try {
                         this.gameState.setNumber(p, ap.inventNumber());
                         logger.i("%s has chosen %s", this.computerPlayer,
                                 this.computerPlayer.getNumber());
                         StringBuilder secretNumber = new StringBuilder();
                         for (int i = 0; i < GameConfiguration.numberLength(); i++) {
                             secretNumber.append("?");
                         }
                         this.numberRight.setText(secretNumber);
                         this.updateHint();
                     } catch (GameException ge) {
                         logger.d(ge.getMessage());
                     }
                     break;
                 case GUESS:
                     try {
                         Number g = ap.makeGuess(this.gameState);
                         this.gameState.guess(p, g);
                         Answer ans = this.gameState.getLastRound().getAnswers()
                                 .get(this.computerPlayer);
                         this.addGuessAndAnswer(this.guessListRight, g, ans);
                     } catch (GameException ge) {
                         logger.d(ge.getMessage());
                     }
                     break;
             }
         }
     }
 
     private void addGuessAndAnswer(LinearLayout ll, Number guess, Answer answer) {
         TextView tv = new TextView(this);
         tv.setTextAppearance(this, R.style.GuessText);
         tv.setText(String.format("%s %s", guess, answer));
         ll.addView(tv);
        this.scrollView.fullScroll(View.FOCUS_DOWN);
        // TODO make the scroll view scroll all the way to the
        // bottom
     }
 
     private OnClickListener getButtonOnClickListener(final int buttonNumber) {
         final GameActivity a = this;
         return new OnClickListener() {
             @Override
             public void onClick(View v) {
                 String text = a.guessText.getText().toString();
                 a.guessText.setText(text + buttonNumber);
                 a.buttonArray[buttonNumber].setEnabled(false);
                 a.resetButtonState();
             }
         };
     }
 
     private OnClickListener getClearOnClickListener() {
         final GameActivity a = this;
         return new OnClickListener() {
             @Override
             public void onClick(View v) {
                 String text = a.guessText.getText().toString();
                 if (text.length() > 0) {
                     a.guessText.setText(text.substring(0, text.length() - 1));
                 }
                 a.resetButtonState();
             }
         };
     }
 
     private void resetButtonState() {
         String text = this.guessText.getText().toString();
         if (text.length() >= GameConfiguration.numberLength()) {
             this.setButtonsEnabled(false);
             return;
         } else {
             this.setButtonsEnabled(true);
         }
         for (String s: text.split("")) {
             if (s.equals("")) {
                 continue;
             }
             try {
                 int n = Integer.parseInt(s);
                 this.buttonArray[n].setEnabled(false);
             } catch (NumberFormatException e) {
                 logger.d(e.getMessage());
                 this.guessText.setText("");
                 setButtonsEnabled(true);
             }
         }
     }
 
     private void updateHint() {
         GameStep.Type type = this.gameState.getGameStep().type();
         if (type == GameStep.Type.SET_NUMBER) {
             this.hintText.setText(R.string.hint_number);
         } else if (type == GameStep.Type.GUESS) {
             this.hintText.setText(R.string.hint_guess);
         }
     }
 
     private void setButtonsEnabled(boolean enabled) {
         for (Button b: this.buttonArray) {
             b.setEnabled(enabled);
         }
     }
 
     private boolean isGameOver() {
         return this.gameState.isGameOver();
     }
 }
