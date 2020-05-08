 package com.example.seamerchant;
 
 import java.util.ArrayList;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.input.touch.TouchEvent;
 
 import com.example.seamerchant.game.Game;
 import com.example.seamerchant.game.Game.OnGameChangeListener;
 import com.example.seamerchant.game.PricedItem;
 import com.example.seamerchant.game.ScoreHandler;
 import com.example.seamerchant.game.Scores;
 import com.example.seamerchant.scene.Base;
 import com.example.seamerchant.scene.Base.OnActionDownListener;
 import com.example.seamerchant.scene.Buy;
 import com.example.seamerchant.scene.Buy.OnBuyItemListener;
 import com.example.seamerchant.scene.EndGame;
 import com.example.seamerchant.scene.EndGame.OnEndDialogClickListener;
 import com.example.seamerchant.scene.GameStart;
 import com.example.seamerchant.scene.HighScore;
 import com.example.seamerchant.scene.LowerBanner;
 import com.example.seamerchant.scene.NewDay;
 import com.example.seamerchant.scene.Options;
 import com.example.seamerchant.scene.Options.OnOptionClickListener;
 import com.example.seamerchant.scene.Rest;
 import com.example.seamerchant.scene.Sell;
 import com.example.seamerchant.scene.Sell.OnSellItemListener;
 import com.example.seamerchant.scene.SideBanner;
 import com.example.seamerchant.scene.StormToss;
 import com.example.seamerchant.scene.Travel;
 import com.example.seamerchant.scene.TurnBack;
 import com.example.seamerchant.scene.Weather;
 import com.example.seamerchant.scene.Welcome;
 
 public class SceneManager implements OnOptionClickListener, OnGameChangeListener, OnBuyItemListener, OnSellItemListener {
 
 	private Engine mEngine;
 	private MainActivity mMainActivity;
 	private Welcome mWelcomeGameScene;
 	protected SideBanner mSideBanner;
 	protected LowerBanner mLowerBanner;
 	private Game mGame;
 	private int mContinueTravelDest;
 
 	public enum SceneType
 	{
 		WELCOME,
 		GAMESTART,
 		NEWDAY,
 		WEATHER,
 		OPTIONS,
 		SELL,
 		BUY,
 		//PIRATES,
 		//PIRATERESULT,
 		REST,
 		TRAVEL,
 		HIGHSCORE,
 		ENDGAME, 
 		TURNBACK,
 		STORM
 	}
 
 	public SceneManager(Game game, MainActivity baseActivity) {
 		mEngine = baseActivity.getEngine();
 		mMainActivity = baseActivity;
 		mWelcomeGameScene = new Welcome(mMainActivity);
 		mGame = game;
 		mGame.setGameChangeListener(this);
 		mSideBanner = new SideBanner(mMainActivity,mGame);
 		mLowerBanner = new LowerBanner(mMainActivity,mGame);
 	}
 
 	public Scene getWelcomeScene() {
 		final Base ws = mWelcomeGameScene;
 		final Scene scene = ws.loadScene();
 		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
 			
 			@Override
 			public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
 					setCurrentScene(SceneType.GAMESTART);
 					return true;
 					}
 				return false;
 			}
 		});
  	
 		return scene;
 	}
 	
 	//Method allows you to set the currently active scene
 	private void setCurrentScene(SceneType scene) {
 		switch (scene)
 		{
 		case WELCOME:
 			break;
 		case GAMESTART:
 			startGameScene();
 			mWelcomeGameScene.detachAndUnload();
 			break;
 		case NEWDAY:
 			startNewDayScene();
 			break;
 		case WEATHER:
 			startWeatherScene();
 			break;
 		case OPTIONS:
 			startOptionsScene();
 			break;
 		case BUY:
 			startBuyScene();
 			break;
 		case SELL:
 			startSellScene();
 			break;
 		case REST:
 			startRestScene();
 			break;
 		case TRAVEL:
 			startTravelScene();
 			break;
 		case HIGHSCORE:
 			startHighScoreScene();
 			break;
 		case ENDGAME:
 			startEndGameScene();
 			break;
 		case TURNBACK:
 			startTurnBackScene();
 		case STORM:
 			startStormScene();
 		default:
 			break;
 		}
 	}
 
 
 	private void startStormScene() {
 		final StormToss st = new StormToss(mMainActivity, mSideBanner, mLowerBanner,mGame);
 		st.loadResourcesAndScene();
 		mEngine.setScene(st.getScene());
 		st.setOnActionDown(new OnActionDownListener() {
 			@Override
 			public void onAcionDown(Base base) {
 				st.detachAndUnload();
 				final Travel tl = new Travel(mMainActivity, mSideBanner, mLowerBanner, mGame,mContinueTravelDest);
 				tl.loadResourcesAndScene();
 				mEngine.setScene(tl.getScene());
 			}
 		});
 	}
 
 	private void startTurnBackScene() {
 		final TurnBack tb = new TurnBack(mMainActivity, mSideBanner, mLowerBanner, mContinueTravelDest, mGame.getPlayer().getLocation());
 		tb.loadResourcesAndScene();
 		mEngine.setScene(tb.getScene());
 		tb.setOnActionDown(new OnActionDownListener() {
 			@Override
 			public void onAcionDown(Base base) {
 				tb.detachAndUnload();
 				final Travel tl = new Travel(mMainActivity, mSideBanner, mLowerBanner, mGame,mContinueTravelDest);
 				tl.loadResourcesAndScene();
 				mEngine.setScene(tl.getScene());
 			}
 		});
 	}
 
 	private void startEndGameScene() {
 		final ArrayList<Scores> highScores = ScoreHandler.getScoreFileContents(mMainActivity.getApplicationContext());
 //		int lowest = 0;
 //		if (!highScores.isEmpty()) {
 //			lowest = highScores.get(highScores.size()-1).getScore();
 //		}
 		final EndGame eg = new EndGame(mMainActivity, mGame.getPlayer().getMoney(),highScores);
 		eg.loadResourcesAndScene();
 		mEngine.setScene(eg.getScene());
 		eg.setOnEndDialogClickListener(new OnEndDialogClickListener() {
 			@Override
 			public void onDialogClick(boolean result, EndGame endGame) {
 				eg.detachAndUnload(); 
 				if (result) {
 					mGame.restart();
 				} else {
 					mMainActivity.finish();
 				}
 			}
 		});
 		
 		mMainActivity.showUsernameDialog(new MainActivity.OnUsernameEnterListener() {
 			@Override
 			public void onName(String name) {
 				Scores newScore = new Scores(name, mGame.getPlayer().getMoney());
 				newScore.writeScore(mMainActivity.getApplicationContext());
 				eg.addScore(newScore);
 			}
 		});
 		
 		eg.setOnActionDown(new OnActionDownListener() {
 			@Override
 			public void onAcionDown(Base base) {
 				eg.showAnotherGameMessage();
 			}
 		});
 	}
 
 	
 	private void startHighScoreScene() {
 		final HighScore hs = new HighScore(mMainActivity, mGame);
 		hs.loadResourcesAndScene();
 		mEngine.setScene(hs.getScene());
 		
 	}
 
 	private void startTravelScene() {
 		final Travel tl = new Travel(mMainActivity, mSideBanner, mLowerBanner, mGame,0);
 		tl.loadResourcesAndScene();
 		mEngine.setScene(tl.getScene());
 	}
 
 	private void startBuyScene() {
 		final Buy buy = new Buy(mMainActivity, mSideBanner, mLowerBanner, mGame, this);
 		buy.loadResourcesAndScene();
 		mEngine.setScene(buy.getScene());
 	}
 	private void startSellScene() {
 		final Sell sell = new Sell(mMainActivity, mSideBanner, mLowerBanner, mGame, this);
 		sell.loadResourcesAndScene();
 		mEngine.setScene(sell.getScene());
 	}
 
 	private void startRestScene() {
 		final Rest rt = new Rest(mMainActivity, mSideBanner, mLowerBanner);
 		rt.loadResourcesAndScene();
 		mEngine.setScene(rt.getScene());
 		rt.setOnActionDown(new OnActionDownListener() {
 			@Override
 			public void onAcionDown(Base base) {
 				rt.detachAndUnload(); 
 				setCurrentScene(SceneType.NEWDAY);
 
 			}
 		});
 		
 	}
 
 //	private void startNextDayScene() {
 //		final Base ed = new EndDay(mBaseActivity);
 //		ed.loadResourcesAndScene();
 //		mEngine.setScene(ed.getScene());
 //		ed.setOnActionDown(new OnActionDownListener() {
 //			@Override
 //			public void onAcionDown(Base base) {
 //				mGame.nextDay();
 //				setCurrentScene(SceneType.NEWDAY);
 //				ed.detachAndUnload();
 //			}
 //		});
 //
 //	}
 
 	private void startOptionsScene() {
 		final Options op = new Options(mMainActivity, mSideBanner, mLowerBanner);
 		op.loadResourcesAndScene();
 		op.setOnOptionClickListener(this);
 		mEngine.setScene(op.getScene());
 		mSideBanner.refresh();
 		mLowerBanner.refresh();
 	}
 
 	private void startWeatherScene() {
 		final Weather wr = new Weather(mMainActivity);
 		wr.setWeather(mGame.getCurrentWeather(mGame.getStormyWeather()),mGame.getStormyWeather());
 		wr.loadResourcesAndScene();
 		mEngine.setScene(wr.getScene());
 		wr.setOnActionDown(new OnActionDownListener() {
 			@Override
 			public void onAcionDown(Base base) {
 				setCurrentScene(SceneType.OPTIONS);
 				wr.detachAndUnload();
 			}
 		});
 	}
 
 	private void startNewDayScene() {
 		final Base nd = new NewDay(mMainActivity,mGame.getCurrentDay());
 		nd.loadResourcesAndScene();
 		mEngine.setScene(nd.getScene());
 		nd.setOnActionDown(new OnActionDownListener() {
 			@Override
 			public void onAcionDown(Base base) {
 				setCurrentScene(SceneType.WEATHER);
 				nd.detachAndUnload();
 			}
 		});
 	}
 
 	protected void startGameScene() {
 		mGame.nextDay();
 		final Base gs = new GameStart(mMainActivity);
 		gs.loadResourcesAndScene();
 		mEngine.setScene(gs.getScene());
 		gs.setOnActionDown(new OnActionDownListener() {
 			@Override
 			public void onAcionDown(Base base) {
 				setCurrentScene(SceneType.NEWDAY);
 				gs.detachAndUnload();
 			}
 		});
 	}
 
 	public void loadRecources() {
 		mWelcomeGameScene.loadResources();
 	}
 
 	@Override
 	public void onOptionClick(final Options options, int option) {
 		switch (option) {
 		case Options.MENU_REST:
 			options.detachAndUnload();
 			if(mGame.nextDay())
 				setCurrentScene(SceneType.REST);
 			break;
 		case Options.MENU_BUY:
 			// TODO consider adding a message for buying if you have no money
 			setCurrentScene(SceneType.BUY);
 			options.detachAndUnload();
 			break;
 		case Options.MENU_SELL:
 			if (!mGame.getPlayer().hasGoods()) {
 				options.showNoGoodsMessage();
 			} else {
 				setCurrentScene(SceneType.SELL);
 				options.detachAndUnload();
 			}
 			break;
 		case Options.MENU_TRAVEL:
 			if(!mGame.canTravel()){
 				options.showCantTravelMessage();
 			}
 			else{
 				setCurrentScene(SceneType.TRAVEL);
 				options.detachAndUnload();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 	@Override
 	public void onGameFinish() {
 		setCurrentScene(SceneType.ENDGAME);
 	}
 
 	@Override
 	public void onBuyItem(PricedItem item, int count, Buy buy) {
 		mGame.buyItem(item, count);
 		setCurrentScene(SceneType.OPTIONS);
 		buy.detachAndUnload();
 	}
 
 	@Override
 	public void onSellItem(PricedItem item, int count, Sell sell) {
 		mGame.sellItem(item, count);
 		setCurrentScene(SceneType.OPTIONS);
 		sell.detachAndUnload();	}
 
 	@Override
 	public void onTravelEnded() {
 		setCurrentScene(SceneType.OPTIONS);
 		
 	}
 
 	@Override
 	public void onGameRestart() {
 		setCurrentScene(SceneType.NEWDAY);
 	}
 
 	@Override
 	public void onContinueTravel(int dest) {
		// TODO temporary until i fix the event
 		final Travel tl = new Travel(mMainActivity, mSideBanner, mLowerBanner, mGame,dest);
 		tl.loadResourcesAndScene();
 		mEngine.setScene(tl.getScene());
 		
 	}
 
 	@Override
 	public void onGameEventStormTossed(int dest) {
 		mContinueTravelDest = dest;
 		setCurrentScene(SceneType.STORM);
 	}
 
 	@Override
 	public void onGameEventStormBack(int dest) {
 		mContinueTravelDest = dest;
 		setCurrentScene(SceneType.TURNBACK);
 	}
 
 	@Override
 	public void onGameEventPirates(int dest) {
		// TODO Future addition pirate attack
 		mContinueTravelDest = dest;
 		final Travel tl = new Travel(mMainActivity, mSideBanner, mLowerBanner, mGame,dest);
 		tl.loadResourcesAndScene();
 		mEngine.setScene(tl.getScene());
 	}
 
 }
