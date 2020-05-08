 package com.howfun.android.antguide;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Chronometer;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AntGuide extends Activity implements OnTouchListener {
    private static final String TAG = "AntGuide";
 
    // private AntView mAntView;
 
    public static int DEVICE_WIDTH;
    public static int DEVICE_HEIGHT;
 
    private static final int SOUND_EFFECT_COLLISION = 0;
    private static final int SOUND_EFFECT_FOOD = 1;
    private static final int SOUND_EFFECT_VICTORY = 2;
    private static final int SOUND_EFFECT_LOST = 3;
 
    private SoundPool mSoundPool;
    private int[] mSounds;
    private int[] mSoundIds;
 
    Intent mIntentService = null;
    Intent mIntentReceiver = null;
 
    private TextView gameScore;
    // private Chronometer gameChronometer;
    private ImageView gamePause;
    private ImageView gamePlay;
    private AntView antView;
    private FrameLayout mGameInfo;
    private TextView mGameInfoText;
 
    private int mScore;
    private static final int FOOD_SCORE = 100;
 
    private GameStatus mGameStatus;
 
    private Handler mHandler = new Handler() {
 
       public void handleMessage(Message msg) {
          switch (msg.what) {
          case Utils.MSG_ANT_HOME:
             playSoundEffect(SOUND_EFFECT_VICTORY);
             stopGame(Utils.ANT_HOME);
             break;
          case Utils.MSG_ANT_LOST:
             playSoundEffect(SOUND_EFFECT_LOST);
             stopGame(Utils.ANT_LOST);
             break;
          case Utils.MSG_ANT_COLLISION:
             playSoundEffect(SOUND_EFFECT_COLLISION);
             break;
 
          case Utils.MSG_ANT_FOOD:
             playSoundEffect(SOUND_EFFECT_FOOD);
             updateScore();
             break;
          case Utils.MSG_ANT_TIMEOUT:
             // TODO: game timeout
             stopGame(Utils.ANT_TIMEOUT);
             break;
 
          case Utils.MSG_SCORE_BOARD:
             Utils.log(TAG, "show scroe board,score====" + msg.arg1);
             showScoreBoard(msg.arg1);
             break;
          default:
             break;
          }
       }
 
    };
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Utils.log(TAG, "onCreate()");
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN);
       this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
       DisplayMetrics dm = new DisplayMetrics();
       this.getWindowManager().getDefaultDisplay().getMetrics(dm);
       // get device pixels
       DEVICE_WIDTH = dm.widthPixels;
       DEVICE_HEIGHT = dm.heightPixels;
 
       setContentView(R.layout.game_view);
 
       findViews();
       setupListeners();
       loadSoundEffects();
       init();
 
    }
 
    @Override
    protected void onResume() {
       super.onResume();
       Utils.log(TAG, "onresume..");
 
       sendBroadcast(mIntentReceiver);
       int gameStatus = mGameStatus.getStatus();
 
       if (gameStatus == GameStatus.GAME_INIT) {
          playGame();
       } else if (gameStatus == GameStatus.GAME_STOPPED) {
          playGame();
       } else if (gameStatus == GameStatus.GAME_PAUSED) {
          resumeGame();
       } else {
          // TODO stop
       }
 
    }
 
    @Override
    protected void onPause() {
       super.onPause();
       Utils.log(TAG, "onPause..");
//      pauseGame();
       //TODO pause bug
       stopService(mIntentService);
 
    }
 
    private void findViews() {
       gameScore = (TextView) findViewById(R.id.game_score);
       gamePause = (ImageView) findViewById(R.id.game_pause);
       gamePlay = (ImageView) findViewById(R.id.game_play);
       antView = (AntView) findViewById(R.id.ant_view);
       mGameInfo = (FrameLayout) findViewById(R.id.game_view_info);
 
       mGameInfoText = (TextView) findViewById(R.id.game_view_info_text);
    }
 
    private void setupListeners() {
 
       if (gamePause != null) {
          gamePause.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                pauseGame();
             }
          });
       }
 
       if (gamePlay != null) {
          gamePlay.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                if (mGameStatus.getStatus() == GameStatus.GAME_PAUSED) {
                   resumeGame();
                } else if (mGameStatus.getStatus() == GameStatus.GAME_STOPPED) {
                   playGame();
                }
             }
          });
       }
 
       if (antView != null) {
          antView.setOnTouchListener(new OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float x = event.getX();
                float y = event.getY();
                switch (action) {
                case MotionEvent.ACTION_DOWN:
                   antView.setDownPos(x, y);
 
                   break;
                case MotionEvent.ACTION_MOVE:
                   break;
                case MotionEvent.ACTION_UP:
                   antView.setUpPos(x, y);
                   antView.showBlockLine();
                   break;
                }
                return true;
             }
 
          });
       }
    }
 
    private void loadSoundEffects() {
       mSounds = new int[] { R.raw.collision, R.raw.food, R.raw.victory,
             R.raw.lost };
       mSoundPool = new SoundPool(mSounds.length, AudioManager.STREAM_MUSIC, 100);
       mSoundIds = new int[] {
             mSoundPool.load(this, mSounds[SOUND_EFFECT_COLLISION], 1),
             mSoundPool.load(this, mSounds[SOUND_EFFECT_FOOD], 1),
             mSoundPool.load(this, mSounds[SOUND_EFFECT_VICTORY], 1),
             mSoundPool.load(this, mSounds[SOUND_EFFECT_LOST], 1) };
    }
 
    private void playSoundEffect(int id) {
       if (mSoundPool != null) {
          mSoundPool.play(mSoundIds[id], 13, 15, 1, 0, 1f);
       }
    }
 
    private void init() {
       mIntentService = new Intent("com.howfun.android.antguide.MusicService");
       mIntentReceiver = new Intent("com.howfun.android.antguide.MusicReceiver");
 
       mGameStatus = new GameStatus();
      antView.setHandler(mHandler);
    }
 
    private void playGame() {
       Utils.log(TAG, "playGame..");
       mGameStatus.setStaus(GameStatus.GAME_RUNNING);
       antView.playGame();
 
       showGamePause();
 
       hideGameInfo();
 
       mScore = 0;
       gameScore.setText(String.valueOf(mScore));
 
       // TODO timing starts
 
    }
 
    private void resumeGame() {
       mGameStatus.setStaus(GameStatus.GAME_RUNNING);
 
       antView.resumeGame();
 
       showGamePause();
       hideGameInfo();
       // TODO timing resume
    }
 
    /**
     * if pause btn is clicked or activity runs onPause(),u should call this func
     */
    private void pauseGame() {
       Utils.log(TAG, "pauseGame..");
       mGameStatus.setStaus(GameStatus.GAME_PAUSED);
       antView.pauseGame();
       hideGamePause();
       showGameInfo("game paused");
       // TODO timing pause
    }
 
    /**
     * stop the game if ant gets home or lost
     * 
     * @param why
     *           why stops the game ,the reasons may be ant gets home or ant gets
     *           disappeared
     */
    private void stopGame(int why) {
       mGameStatus.setStaus(GameStatus.GAME_STOPPED);
       antView.stopGame();
       hideGamePause();
 
       String info = "";
       if (why == Utils.ANT_HOME) {
          info = "Great,Ant got home!";
       } else if (why == Utils.ANT_LOST) {
          info = "Opps,Ant got lost!!!";
       } else if (why == Utils.ANT_TIMEOUT) {
          info = "Oh,Time Out....";
       } else {
          info = "Ehhhhhhhhhhhhh!";
       }
       showGameInfo(info);
       // TODO timing clear
 
       if (isHighScore(mScore)) {
          Message msg = new Message();
          msg.what = Utils.MSG_SCORE_BOARD;
          msg.arg1 = mScore;
          mHandler.sendMessage(msg);
       }
       mScore = 0;
       gameScore.setText(String.valueOf(mScore));
    }
 
    private void showGamePause() {
       gamePause.setVisibility(View.VISIBLE);
       gamePlay.setVisibility(View.INVISIBLE);
    }
 
    private void hideGamePause() {
       gamePause.setVisibility(View.INVISIBLE);
       gamePlay.setVisibility(View.VISIBLE);
    }
 
    private void hideGameInfo() {
       mGameInfo.setVisibility(View.GONE);
    }
 
    /**
     * 
     * @param info
     *           pause or game over
     */
    private void showGameInfo(String info) {
       mGameInfo.setVisibility(View.VISIBLE);
       mGameInfoText.setText(info);
    }
 
    @Override
    public boolean onTouch(View v, MotionEvent event) {
       return false;
    }
 
    private void updateScore() {
       mScore += FOOD_SCORE;
       if (gameScore != null) {
          gameScore.setAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_up_in));
          gameScore.setText(String.valueOf(mScore));
          gameScore.setAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_up_out));
       }
 
    }
 
    private void showScoreBoard(long score) {
       Intent intent = new Intent(this, BigNameActivity.class);
       intent.putExtra(Utils.SCORE, score);
       startActivity(intent);
    }
 
    private boolean isHighScore(long score) {
       boolean flag = false;
       if (score == 0) {
          return false;
       }
       List<Score> l = new ArrayList<Score>();
       DBAdapter db = new DBAdapter(this);
       db.open();
       l = db.getHighScores(Utils.TOP_SCORE_COUNT);
       db.close();
       if (l.size() < Utils.TOP_SCORE_COUNT) {
          flag = true;
       } else {
          long scoreT = l.get(l.size() - 1).getScore();
          if (score >= scoreT) {
             flag = true;
          }
       }
       return flag;
    }
 
 }
