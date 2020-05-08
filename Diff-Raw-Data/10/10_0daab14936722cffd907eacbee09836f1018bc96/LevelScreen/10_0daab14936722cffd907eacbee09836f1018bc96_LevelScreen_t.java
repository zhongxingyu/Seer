 package com.tooflya.signs.screens;
 
 import org.anddev.andengine.entity.modifier.MoveModifier;
 
 import android.util.FloatMath;
 
 import com.tooflya.signs.Game;
 import com.tooflya.signs.Options;
 import com.tooflya.signs.Options.Resolutions;
 import com.tooflya.signs.Resources;
 import com.tooflya.signs.entities.Apple;
 import com.tooflya.signs.entities.BonusHolder;
 import com.tooflya.signs.entities.ButtonScaleable;
 import com.tooflya.signs.entities.Entity;
 import com.tooflya.signs.entities.ExesNumber;
 import com.tooflya.signs.entities.PanelStar;
 import com.tooflya.signs.entities.Rectangle;
 import com.tooflya.signs.entities.Shows;
 import com.tooflya.signs.entities.Sign;
 import com.tooflya.signs.entities.Star;
 import com.tooflya.signs.managers.ArrayEntityManager;
 import com.tooflya.signs.managers.EntityManager;
 import com.tooflya.signs.managers.ListEntityManager;
 
 /**
  * @author Tooflya.com
  * @since
  */
 public class LevelScreen extends Screen {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static float POSITION_START_PADDIG_X;
 	private static float POSITION_START_PADDIG_Y;
 	private static float POSITION_ELEMENTS_PADDIG;
 	private static float POSITION_LEFT_ICONS_BACKGROUND_X;
 	private static float POSITION_LEFT_ICONS_Y;
 	private static float POSITION_LEFT_ICONS_X;
 	private static float POSITION_LEFT_ICONS_BACKGROUND_Y_1;
 	private static float POSITION_LEFT_ICONS_BACKGROUND_Y_2;
 	private static float POSITION_NUMBERS_Y;
 	private static float POSITION_SCORE_TEXT_X;
 	private static float POSITION_SCORE_TEXT_Y;
 	private static float POSITION_SEC_TEXT_X;
 	private static float POSITION_SEC_TEXT_Y;
 	private static float POSITION_TIMER_NUMBER_X1;
 	private static float POSITION_TIMER_NUMBER_X2;
 	private static float POSITION_TIMER_NUMBER_X3;
 	private static float POSITION_TIMER_NUMBER_Y;
 	private static float POSITION_PANEL_STARS_X_1;
 	private static float POSITION_PANEL_STARS_X_2;
 	private static float POSITION_PANEL_STARS_Y_1;
 	private static float POSITION_PANEL_STARS_Y_2;
 	private static float POSITION_PANEL_ANIMATION_X;
 	private static float POSITION_PANEL_ANIMATION_Y;
 	private static float POSITION_EXES_ANIMATION_X_1;
 	private static float POSITION_EXES_ANIMATION_X_2;
 	private static float POSITION_EXES_ANIMATION_Y;
 	private static float POSITION_EXES_NUMBERS_X_1;
 	private static float POSITION_EXES_NUMBERS_X_2;
 	private static float POSITION_EXES_NUMBERS_X_3;
 	private static float POSITION_EXES_NUMBERS_X_4;
 	private static float POSITION_EXES_NUMBERS_X_5;
 	private static float POSITION_EXES_NUMBERS_Y;
 	private static float POSITION_SCORE_NUMBER_X_1;
 	private static float POSITION_SCORE_NUMBER_X_2;
 	private static float POSITION_SCORE_NUMBER_X_3;
 	private static float POSITION_SCORE_NUMBER_X_4;
 	private static float POSITION_SCORE_NUMBER_X_5;
 	private static float POSITION_SCORE_NUMBER_X_6;
 	private static float POSITION_SCORE_NUMBER_X_7;
 	private static float POSITION_SCORE_NUMBER_Y;
 	private static float POSITION_NUMBERS_X_1;
 	private static float POSITION_NUMBERS_X_2;
 	private static float POSITION_NUMBERS_X_3;
 	private static float POSITION_NUMBERS_X_4;
 	private static float POSITION_NUMBERS_X_5;
 	private static float POSITION_NUMBERS_X_6;
 	private static float POSITION_NUMBERS_X_7;
 
 	static {
 		switch (Options.Resolution) {
 		case Resolutions.SD:
 			POSITION_START_PADDIG_X = 50f;
 			POSITION_START_PADDIG_Y = 42f;
 			POSITION_ELEMENTS_PADDIG = 83f;
 			POSITION_LEFT_ICONS_BACKGROUND_X = 15f;
 			POSITION_LEFT_ICONS_Y = 7f;
 			POSITION_LEFT_ICONS_X = 4f;
 			POSITION_LEFT_ICONS_BACKGROUND_Y_1 = 130f;
 			POSITION_LEFT_ICONS_BACKGROUND_Y_2 = 65f;
 			POSITION_NUMBERS_Y = 6f;
 			POSITION_SCORE_TEXT_X = 80f;
 			POSITION_SCORE_TEXT_Y = 4f;
 			POSITION_SEC_TEXT_X = 70f;
 			POSITION_SEC_TEXT_Y = 37f;
 			POSITION_TIMER_NUMBER_X1 = 58f;
 			POSITION_TIMER_NUMBER_X2 = 76f;
 			POSITION_TIMER_NUMBER_X3 = 67f;
 			POSITION_TIMER_NUMBER_Y = 67f;
 			POSITION_PANEL_STARS_X_1 = 42f;
 			POSITION_PANEL_STARS_X_2 = 5f;
			POSITION_PANEL_STARS_Y_1 = 40f;
			POSITION_PANEL_STARS_Y_2 = 10f;
 			POSITION_PANEL_ANIMATION_X = 180f;
 			POSITION_PANEL_ANIMATION_Y = 120f;
 			POSITION_EXES_ANIMATION_X_1 = 21f;
 			POSITION_EXES_ANIMATION_X_2 = 58f;
 			POSITION_EXES_ANIMATION_Y = 100f;
 			POSITION_EXES_NUMBERS_X_1 = 20f;
 			POSITION_EXES_NUMBERS_X_2 = 34f;
 			POSITION_EXES_NUMBERS_X_3 = 10f;
 			POSITION_EXES_NUMBERS_X_4 = 24f;
 			POSITION_EXES_NUMBERS_X_5 = 38f;
 			POSITION_EXES_NUMBERS_Y = 27f;
 			POSITION_SCORE_NUMBER_X_1 = 3f;
 			POSITION_SCORE_NUMBER_X_2 = 23f;
 			POSITION_SCORE_NUMBER_X_3 = 43f;
 			POSITION_SCORE_NUMBER_X_4 = 63f;
 			POSITION_SCORE_NUMBER_X_5 = 83f;
 			POSITION_SCORE_NUMBER_X_6 = 103f;
 			POSITION_SCORE_NUMBER_X_7 = 123f;
 			POSITION_SCORE_NUMBER_Y = 6f;
 			POSITION_NUMBERS_X_1 = 3f;
 			POSITION_NUMBERS_X_2 = 33f;
 			POSITION_NUMBERS_X_3 = 63f;
 			POSITION_NUMBERS_X_4 = 93f;
 			POSITION_NUMBERS_X_5 = 123f;
 			POSITION_NUMBERS_X_6 = 153f;
 			POSITION_NUMBERS_X_7 = 183f;
 			break;
 		case Resolutions.HD:
 			POSITION_START_PADDIG_X = 93f;
 			POSITION_START_PADDIG_Y = 80f;
 			POSITION_ELEMENTS_PADDIG = 157f;
 			POSITION_LEFT_ICONS_BACKGROUND_X = 15f;
 			POSITION_LEFT_ICONS_Y = 7f;
 			POSITION_LEFT_ICONS_X = 4f;
 			POSITION_LEFT_ICONS_BACKGROUND_Y_1 = 260f;
 			POSITION_LEFT_ICONS_BACKGROUND_Y_2 = 130f;
 			POSITION_NUMBERS_Y = 0f;
 			POSITION_SCORE_TEXT_X = 130f;
 			POSITION_SCORE_TEXT_Y = 10f;
 			POSITION_SEC_TEXT_X = 95f;
 			POSITION_SEC_TEXT_Y = 54f;
 			POSITION_TIMER_NUMBER_X1 = 81f;
 			POSITION_TIMER_NUMBER_X2 = 107f;
 			POSITION_TIMER_NUMBER_X3 = 94f;
 			POSITION_TIMER_NUMBER_Y = 104f;
 			POSITION_PANEL_STARS_X_1 = 42f;
 			POSITION_PANEL_STARS_X_2 = 10f;
 			POSITION_PANEL_STARS_Y_1 = 80f;
			POSITION_PANEL_STARS_Y_2 = 20f;
 			POSITION_PANEL_ANIMATION_X = 360f;
 			POSITION_PANEL_ANIMATION_Y = 240f;
 			POSITION_EXES_ANIMATION_X_1 = 38f;
 			POSITION_EXES_ANIMATION_X_2 = 110f;
 			POSITION_EXES_ANIMATION_Y = 180f;
 			POSITION_EXES_NUMBERS_X_1 = 34f;
 			POSITION_EXES_NUMBERS_X_2 = 64f;
 			POSITION_EXES_NUMBERS_X_3 = 23f;
 			POSITION_EXES_NUMBERS_X_4 = 48f;
 			POSITION_EXES_NUMBERS_X_5 = 73f;
 			POSITION_EXES_NUMBERS_Y = 50f;
 			POSITION_SCORE_NUMBER_X_1 = 6f;
 			POSITION_SCORE_NUMBER_X_2 = 46f;
 			POSITION_SCORE_NUMBER_X_3 = 86f;
 			POSITION_SCORE_NUMBER_X_4 = 126f;
 			POSITION_SCORE_NUMBER_X_5 = 166f;
 			POSITION_SCORE_NUMBER_X_6 = 206f;
 			POSITION_SCORE_NUMBER_X_7 = 246f;
 			POSITION_SCORE_NUMBER_Y = 12f;
 			POSITION_NUMBERS_X_1 = 6f;
 			POSITION_NUMBERS_X_2 = 66f;
 			POSITION_NUMBERS_X_3 = 126f;
 			POSITION_NUMBERS_X_4 = 186f;
 			POSITION_NUMBERS_X_5 = 246f;
 			POSITION_NUMBERS_X_6 = 306f;
 			POSITION_NUMBERS_X_7 = 366f;
 			break;
 		}
 	}
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	public static int FAKE_SHOWS;
 
 	private final Rectangle mBackground;
 
 	private final Entity mBackgroundPicture;
 
 	private final Entity mLeftPanel;
 	private final Entity mRightPanel;
 
 	private final Entity mScoreText;
 
 	private final Entity mShopButtonBackground;
 	private final Entity mAchievementsButtonBackground;
 	private final Entity mMenuButtonBackground;
 	private final Entity mRestartButtonBackground;
 	private final Entity mPauseButtonBackground;
 	private final Entity mRollButtonBackground;
 	private final Entity mExesBackground;
 
 	private final ButtonScaleable mShopButton;
 	private final ButtonScaleable mAchievementsButton;
 	private final ButtonScaleable mMenuButton;
 	private final ButtonScaleable mRestartButton;
 	private final ButtonScaleable mPauseButton;
 	private final ButtonScaleable mRollButton;
 
 	private final Entity mTable;
 
 	public final EntityManager<Shows> mShows;
 	public final EntityManager<Sign> mSigns;
 	public final EntityManager<Star> mStars;
 	public final EntityManager<PanelStar> mExes;
 	public final EntityManager<Entity> mSignsEyes;
 	public final EntityManager<Apple> mSignsEyesApples;
 	private final EntityManager<Entity> mScoreNumbers;
 	public final EntityManager<Entity> mDecorators;
 	private final EntityManager<ExesNumber> mExesNumbers;
 	private final EntityManager<ExesNumber> mTimerNumbers;
 
 	private final BonusHolder mBonusNumbersHolder;
 	private final EntityManager<Entity> mBonusNumbers;
 
 	private final ExesNumber mTimerSecText;
 
 	private final Entity mAlertBackground;
 	private final Entity mOneMinuteAlert;
 	private final Entity mHarryUpAlert;
 	private final Entity mBestScoreAlert;
 
 	private float mTimeBeforeHaveOneMinuteAlert;
 
 	private boolean mIsHaveOneMinuteAnimationShows;
 	private boolean mIsHarryUpAnimationShows;
 	private boolean mIsBestScoreAnimationShows;
 
 	private final MoveModifier mHaveOneMinuteAnimationOn = new MoveModifier(0.5f, Options.cameraWidth, Options.cameraWidth - POSITION_PANEL_ANIMATION_X, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y) {
 
 		/* (non-Javadoc)
 		 * @see org.anddev.andengine.util.modifier.BaseDurationModifier#onStarted()
 		 */
 		@Override
 		public void onStarted() {
 			LevelScreen.this.mOneMinuteAlert.create();
 		}
 	};
 
 	private final MoveModifier mHaveOneMinuteAnimationOff = new MoveModifier(0.5f, Options.cameraWidth - POSITION_PANEL_ANIMATION_X, Options.cameraWidth, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y) {
 
 		/* (non-Javadoc)
 		 * @see org.anddev.andengine.util.modifier.BaseDurationModifier#onFinished()
 		 */
 		@Override
 		public void onFinished() {
 			LevelScreen.this.mOneMinuteAlert.destroy();
 		}
 	};
 
 	private final MoveModifier mBestScoreAnimationOn = new MoveModifier(0.5f, Options.cameraWidth, Options.cameraWidth - POSITION_PANEL_ANIMATION_X, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y) {
 
 		/* (non-Javadoc)
 		 * @see org.anddev.andengine.util.modifier.BaseDurationModifier#onStarted()
 		 */
 		@Override
 		public void onStarted() {
 			LevelScreen.this.mBestScoreAlert.create();
 		}
 	};
 
 	private final MoveModifier mBestScoreAnimationOff = new MoveModifier(0.5f, Options.cameraWidth - POSITION_PANEL_ANIMATION_X, Options.cameraWidth, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y) {
 
 		/* (non-Javadoc)
 		 * @see org.anddev.andengine.util.modifier.BaseDurationModifier#onFinished()
 		 */
 		@Override
 		public void onFinished() {
 			LevelScreen.this.mBestScoreAlert.destroy();
 		}
 	};
 
 	private final MoveModifier mHarryUpAnimationOn = new MoveModifier(0.5f, Options.cameraWidth, Options.cameraWidth - POSITION_PANEL_ANIMATION_X, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y) {
 
 		/* (non-Javadoc)
 		 * @see org.anddev.andengine.util.modifier.BaseDurationModifier#onStarted()
 		 */
 		@Override
 		public void onStarted() {
 			LevelScreen.this.mHarryUpAlert.create();
 		}
 	};
 
 	private final MoveModifier mHarryUpAnimationOff = new MoveModifier(0.5f, Options.cameraWidth - POSITION_PANEL_ANIMATION_X, Options.cameraWidth, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y, Options.cameraHeight - POSITION_PANEL_ANIMATION_Y) {
 
 		/* (non-Javadoc)
 		 * @see org.anddev.andengine.util.modifier.BaseDurationModifier#onFinished()
 		 */
 		@Override
 		public void onFinished() {
 			LevelScreen.this.mHarryUpAlert.destroy();
 		}
 	};
 
 	private final MoveModifier mExesBackgroundMoveAnimationOn = new MoveModifier(0.2f, Options.cameraWidth + POSITION_EXES_ANIMATION_X_1, Options.cameraWidth - POSITION_EXES_ANIMATION_X_2, Options.cameraCenterY - POSITION_EXES_ANIMATION_Y, Options.cameraCenterY - POSITION_EXES_ANIMATION_Y);
 	private final MoveModifier mExesBackgroundMoveAnimationOff = new MoveModifier(0.2f, Options.cameraWidth - POSITION_EXES_ANIMATION_X_2, Options.cameraWidth + POSITION_EXES_ANIMATION_X_1, Options.cameraCenterY - POSITION_EXES_ANIMATION_Y, Options.cameraCenterY - POSITION_EXES_ANIMATION_Y);
 
 	private float mTime;
 	private float mHarryTime;
 	private float mBestScoreTime;
 	private float mLastFindTime;
 	private float mBlockTime;
 	private float mTimerTime;
 	private float mTimerAnimationTime;
 
 	public boolean running = true;
 
 	private int score;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	/**
 	 * 
 	 */
 	public LevelScreen() {
 		this.mBackgroundPicture = new Entity(Resources.mMainMenuBackgroundTextureRegion, this);
 
 		this.mBackground = new Rectangle();
 		this.attachChild(this.mBackground);
 		this.mBackground.setBackgroundCenterPosition();
 
 		this.mScoreText = new Entity(Resources.mScoreTextTextureRegion, this.mBackground);
 		this.mScoreText.create().create().setPosition(POSITION_SCORE_TEXT_X, POSITION_SCORE_TEXT_Y);
 
 		this.mScoreNumbers = new ArrayEntityManager<Entity>(7, new Entity(Resources.mScoreNumbersTextureRegion, this.mBackground));
 		this.mScoreNumbers.create().setPosition(this.mScoreText.getX() + this.mScoreText.getWidth() + POSITION_SCORE_NUMBER_X_1, this.mScoreText.getY() + POSITION_SCORE_NUMBER_Y, true);
 		this.mScoreNumbers.create().setPosition(this.mScoreText.getX() + this.mScoreText.getWidth() + POSITION_SCORE_NUMBER_X_2, this.mScoreText.getY() + POSITION_SCORE_NUMBER_Y, true);
 		this.mScoreNumbers.create().setPosition(this.mScoreText.getX() + this.mScoreText.getWidth() + POSITION_SCORE_NUMBER_X_3, this.mScoreText.getY() + POSITION_SCORE_NUMBER_Y, true);
 		this.mScoreNumbers.create().setPosition(this.mScoreText.getX() + this.mScoreText.getWidth() + POSITION_SCORE_NUMBER_X_4, this.mScoreText.getY() + POSITION_SCORE_NUMBER_Y, true);
 		this.mScoreNumbers.create().setPosition(this.mScoreText.getX() + this.mScoreText.getWidth() + POSITION_SCORE_NUMBER_X_5, this.mScoreText.getY() + POSITION_SCORE_NUMBER_Y, true);
 		this.mScoreNumbers.create().setPosition(this.mScoreText.getX() + this.mScoreText.getWidth() + POSITION_SCORE_NUMBER_X_6, this.mScoreText.getY() + POSITION_SCORE_NUMBER_Y, true);
 		this.mScoreNumbers.create().setPosition(this.mScoreText.getX() + this.mScoreText.getWidth() + POSITION_SCORE_NUMBER_X_7, this.mScoreText.getY() + POSITION_SCORE_NUMBER_Y, true);
 
 		this.mScoreNumbers.getByIndex(3).setCurrentTileIndex(1);
 
 		this.mTable = new Entity(Resources.mGameTableTextureRegion, this.mBackground);
 		this.mTable.create().setPosition((Options.cameraWidth - this.mTable.getWidth()) / 2, Options.cameraHeight - this.mTable.getHeight() - 10f, true);
 		
 		this.mShows = new ListEntityManager<Shows>(new Shows(Resources.mShowTextureRegion, this.mTable));
 		this.mSigns = new ListEntityManager<Sign>(new Sign(Resources.mSignTextureRegion, this.mTable));
 		this.mSignsEyes = new ListEntityManager<Entity>(new Entity(Resources.mSignETextureRegion, this.mTable));
 		this.mSignsEyesApples = new ListEntityManager<Apple>(new Apple(Resources.mEyeAppleTextureRegion, this.mTable));
 		this.mDecorators = new ListEntityManager<Entity>(new Entity(Resources.mGameTopElementsTextureRegion, this.mTable));
 		this.mStars = new ListEntityManager<Star>( new Star(Resources.mStarsTextureRegion, this.mTable));
 
 		this.mAlertBackground = new Entity(Resources.mAlertBackgroundTextureRegion, this.mBackground);
 		this.mOneMinuteAlert = new Entity(Resources.mHaveOneMinuteTextureRegion, this.mAlertBackground);
 		this.mHarryUpAlert = new Entity(Resources.mHarryUpTextureRegion, this.mAlertBackground);
 		this.mBestScoreAlert = new Entity(Resources.mBestScoreTextureRegion, this.mAlertBackground);
 
 		this.mLeftPanel = new Entity(Resources.mPanelTextureRegion, this.mBackground);
 		this.mRightPanel = new Entity(Resources.mPanelTextureRegion, this.mBackground);
 
 		this.mExes = new ListEntityManager<PanelStar>(new PanelStar(Resources.mPanelStarTextureRegion, this.mRightPanel));
 
 		this.mTimerNumbers = new ArrayEntityManager<ExesNumber>(2, new ExesNumber(Resources.mBonusNumbersTextureRegion, this.mRightPanel));
 		this.mTimerSecText = new ExesNumber(Resources.mTimerSecTextureRegion, this.mRightPanel);
 
 		this.mExesBackground = new Entity(Resources.mBonusBackgroundTextureRegion, this.mBackground);
 		this.mShopButtonBackground = new Entity(Resources.mSoundBackgroundTextureRegion, this.mBackground);
 		this.mAchievementsButtonBackground = new Entity(Resources.mSoundBackgroundTextureRegion, this.mBackground);
 		this.mMenuButtonBackground = new Entity(Resources.mSoundBackgroundTextureRegion, this.mBackground);
 		this.mRestartButtonBackground = new Entity(Resources.mSoundBackgroundTextureRegion, this.mBackground);
 		this.mPauseButtonBackground = new Entity(Resources.mSoundBackgroundTextureRegion, this.mBackground);
 		this.mRollButtonBackground = new Entity(Resources.mGameRollBackgroundTextureRegion, this.mBackground);
 
 		this.mExesNumbers = new ArrayEntityManager<ExesNumber>(3, new ExesNumber(Resources.mBonusNumbersTextureRegion, this.mExesBackground));
 
 		this.mShopButton = new ButtonScaleable(Resources.mGameShopButtonTextureRegion, this.mBackground) {
 
 			/* (non-Javadoc)
 			 * @see com.tooflya.signs.entities.ButtonScaleable#onClick()
 			 */
 			@Override
 			public void onClick() {
 				PauseScreen.ACTION = 3;
 
 				Game.mScreens.setChildScreen(Game.mScreens.get(Screen.PAUSE), false, false, true);
 			}
 		};
 
 		this.mAchievementsButton = new ButtonScaleable(Resources.mGameAchievementsButtonTextureRegion, this.mBackground) {
 
 			/* (non-Javadoc)
 			 * @see com.tooflya.signs.entities.ButtonScaleable#onClick()
 			 */
 			@Override
 			public void onClick() {
 				PauseScreen.ACTION = 2;
 
 				Game.mScreens.setChildScreen(Game.mScreens.get(Screen.PAUSE), false, false, true);
 			}
 		};
 
 		this.mMenuButton = new ButtonScaleable(Resources.mGameMenuButtonTextureRegion, this.mBackground) {
 
 			/* (non-Javadoc)
 			 * @see com.tooflya.signs.entities.ButtonScaleable#onClick()
 			 */
 			@Override
 			public void onClick() {
 				Game.mScreens.setChildScreen(Game.mScreens.get(Screen.MAIN), false, false, true);
 			}
 		};
 
 		this.mRestartButton = new ButtonScaleable(Resources.mGameRestartButtonTextureRegion, this.mBackground) {
 
 			/* (non-Javadoc)
 			 * @see com.tooflya.signs.entities.ButtonScaleable#onClick()
 			 */
 			@Override
 			public void onClick() {
 				Game.mScreens.setChildScreen(Game.mScreens.get(Screen.RESTART), false, false, true);
 			}
 		};
 
 		this.mPauseButton = new ButtonScaleable(Resources.mGamePauseButtonTextureRegion, this.mBackground) {
 
 			/* (non-Javadoc)
 			 * @see com.tooflya.signs.entities.ButtonScaleable#onClick()
 			 */
 			@Override
 			public void onClick() {
 				PauseScreen.ACTION = 1;
 
 				Game.mScreens.setChildScreen(Game.mScreens.get(Screen.PAUSE), false, false, true);
 			}
 		};
 
 		this.mRollButton = new ButtonScaleable(Resources.mGameRollButtonTextureRegion, this.mBackground) {
 
 			/* (non-Javadoc)
 			 * @see com.tooflya.signs.entities.ButtonScaleable#onClick()
 			 */
 			@Override
 			public void onClick() {
 				LevelScreen.this.roll();
 			}
 		};
 
 		this.mBonusNumbersHolder = new BonusHolder(0, 0, 0, 0);
 		this.mBackground.attachChild(this.mBonusNumbersHolder);
 
 		this.mBonusNumbers = new ArrayEntityManager<Entity>(10, new Entity(Resources.mScoreNumbersBigTextureRegion, this.mBonusNumbersHolder));
 		this.mBonusNumbers.create().setPosition(POSITION_NUMBERS_X_1, POSITION_NUMBERS_Y);
 		this.mBonusNumbers.create().setPosition(POSITION_NUMBERS_X_2, POSITION_NUMBERS_Y);
 		this.mBonusNumbers.create().setPosition(POSITION_NUMBERS_X_3, POSITION_NUMBERS_Y);
 		this.mBonusNumbers.create().setPosition(POSITION_NUMBERS_X_4, POSITION_NUMBERS_Y);
 		this.mBonusNumbers.create().setPosition(POSITION_NUMBERS_X_5, POSITION_NUMBERS_Y);
 		this.mBonusNumbers.create().setPosition(POSITION_NUMBERS_X_6, POSITION_NUMBERS_Y);
 		this.mBonusNumbers.create().setPosition(POSITION_NUMBERS_X_7, POSITION_NUMBERS_Y);
 		this.mBonusNumbers.clear();
 
 		this.mBonusNumbers.getByIndex(0).setCurrentTileIndex(10);
 
 		this.mBackgroundPicture.create().setBackgroundCenterPosition();
 
 		this.mLeftPanel.create().setPosition(0, 0);
 		this.mRightPanel.create().setPosition(Options.cameraWidth - this.mRightPanel.getWidth(), 0);
 		this.mRightPanel.getTextureRegion().setFlippedHorizontal(true);
 
 		this.mShopButtonBackground.create().setCenterPosition(this.mLeftPanel.getCenterX() - POSITION_LEFT_ICONS_BACKGROUND_X, Options.cameraCenterY - POSITION_LEFT_ICONS_BACKGROUND_Y_1);
 		this.mAchievementsButtonBackground.create().setCenterPosition(this.mLeftPanel.getCenterX() - POSITION_LEFT_ICONS_BACKGROUND_X, Options.cameraCenterY - POSITION_LEFT_ICONS_BACKGROUND_Y_2);
 		this.mMenuButtonBackground.create().setCenterPosition(this.mLeftPanel.getCenterX() - POSITION_LEFT_ICONS_BACKGROUND_X, Options.cameraCenterY);
 		this.mRestartButtonBackground.create().setCenterPosition(this.mLeftPanel.getCenterX() - POSITION_LEFT_ICONS_BACKGROUND_X, Options.cameraCenterY + POSITION_LEFT_ICONS_BACKGROUND_Y_2);
 		this.mPauseButtonBackground.create().setCenterPosition(this.mLeftPanel.getCenterX() - POSITION_LEFT_ICONS_BACKGROUND_X, Options.cameraCenterY + POSITION_LEFT_ICONS_BACKGROUND_Y_1);
 
 		this.mShopButton.create().setCenterPosition(this.mShopButtonBackground.getCenterX() - POSITION_LEFT_ICONS_X, this.mShopButtonBackground.getCenterY() - POSITION_LEFT_ICONS_Y);
 		this.mAchievementsButton.create().setCenterPosition(this.mAchievementsButtonBackground.getCenterX() - POSITION_LEFT_ICONS_X, this.mAchievementsButtonBackground.getCenterY() - POSITION_LEFT_ICONS_Y);
 		this.mMenuButton.create().setCenterPosition(this.mMenuButtonBackground.getCenterX() - POSITION_LEFT_ICONS_X, this.mMenuButtonBackground.getCenterY() - POSITION_LEFT_ICONS_Y);
 		this.mRestartButton.create().setCenterPosition(this.mRestartButtonBackground.getCenterX() - POSITION_LEFT_ICONS_X, this.mRestartButtonBackground.getCenterY() - POSITION_LEFT_ICONS_Y);
 		this.mPauseButton.create().setCenterPosition(this.mPauseButtonBackground.getCenterX() - POSITION_LEFT_ICONS_X, this.mPauseButtonBackground.getCenterY() - POSITION_LEFT_ICONS_Y);
 
 		this.mExesBackground.create().setCenterPosition(Options.cameraWidth + 21f, Options.cameraCenterY - 70f);
 
 		this.mRollButtonBackground.create().setCenterPosition(this.mRightPanel.getCenterX() + 18f, Options.cameraCenterY);
 		this.mRollButton.create().setCenterPosition(this.mRollButtonBackground.getCenterX() + 2f, this.mRollButtonBackground.getCenterY() - 6f);
 
 		this.mAlertBackground.create().setPosition(Options.cameraWidth, Options.cameraHeight - 120f);
 		this.mOneMinuteAlert.setCenterPosition(this.mAlertBackground.getWidth() / 2, this.mAlertBackground.getHeight() / 2);
 		this.mHarryUpAlert.setCenterPosition(this.mAlertBackground.getWidth() / 2, this.mAlertBackground.getHeight() / 2);
 		this.mBestScoreAlert.setCenterPosition(this.mAlertBackground.getWidth() / 2, this.mAlertBackground.getHeight() / 2);
 
 		this.mAlertBackground.registerEntityModifier(this.mHaveOneMinuteAnimationOn);
 		this.mAlertBackground.registerEntityModifier(this.mHaveOneMinuteAnimationOff);
 		this.mAlertBackground.registerEntityModifier(this.mHarryUpAnimationOn);
 		this.mAlertBackground.registerEntityModifier(this.mHarryUpAnimationOff);
 		this.mAlertBackground.registerEntityModifier(this.mBestScoreAnimationOn);
 		this.mAlertBackground.registerEntityModifier(this.mBestScoreAnimationOff);
 
 		this.mExesBackground.registerEntityModifier(this.mExesBackgroundMoveAnimationOn);
 		this.mExesBackground.registerEntityModifier(this.mExesBackgroundMoveAnimationOff);
 
 		this.mExesNumbers.create().setCenterPosition(POSITION_EXES_NUMBERS_X_1, POSITION_EXES_NUMBERS_Y);
 		this.mExesNumbers.create().setCenterPosition(POSITION_EXES_NUMBERS_X_2, POSITION_EXES_NUMBERS_Y);
 		this.mExesNumbers.create().setCenterPosition(POSITION_EXES_NUMBERS_X_3, POSITION_EXES_NUMBERS_Y);
 		this.mExesNumbers.getByIndex(0).setCurrentTileIndex(10);
 		this.mExesNumbers.getByIndex(1).setCurrentTileIndex(3);
 
 		this.mTimerNumbers.create().setCenterPosition(POSITION_TIMER_NUMBER_X1, Options.cameraHeight - POSITION_TIMER_NUMBER_Y);
 		this.mTimerNumbers.create().setCenterPosition(POSITION_TIMER_NUMBER_X2, Options.cameraHeight - POSITION_TIMER_NUMBER_Y);
 
 		this.mTimerSecText.create().setCenterPosition(POSITION_SEC_TEXT_X, Options.cameraHeight - POSITION_SEC_TEXT_Y);
 	}
 
 	public static int X;
 	public static int mMustAddScore;
 	public static int Score;
 	public static int STARS;
 
 	public static int mBestScore;
 
 	public Sign[][] mElements = new Sign[7][4];
 
 	public void init() {
 		mBestScore = Game.mDatabase.getCurrentBestScore();
 
 		this.mTime = 0f;
 		this.mHarryTime = 0f;
 		this.mBestScoreTime = 0f;
 		this.mLastFindTime = 0f;
 		this.mBlockTime = 0f;
 		this.mTimerTime = 61f;
 		this.mTimerAnimationTime = 1f;
 
 		this.mIsHaveOneMinuteAnimationShows = false;
 		this.mIsHarryUpAnimationShows = false;
 		this.mIsBestScoreAnimationShows = false;
 		this.mTimeBeforeHaveOneMinuteAlert = 0f;
 
 		this.mExesBackgroundMoveAnimationOff.reset();
 
 		X = 0;
 		Score = 0;
 		score = 0;
 		STARS = 0;
 		FAKE_SHOWS = 0;
 
 		this.mSigns.clear();
 		this.mShows.clear();
 		this.mExes.clear();
 		this.mSignsEyes.clear();
 		this.mSignsEyesApples.clear();
 		this.mDecorators.clear();
 
 		int max = 0;
 		for (int i = 0; i < Options.mElementsCount; i++) {
 			if (max <= 1) {
 				final int index = Game.random.nextInt(28);
 
 				if (!Game.mDatabase.isItemAvailable(index + 1)) {
 					Sign.ITEMS[i] = index;
 					max++;
 				} else {
 					Sign.ITEMS[i] = -1;
 				}
 			} else {
 				Sign.ITEMS[i] = -1;
 			}
 		}
 
 		int y = 0;
 		int o = 0;
 		for (int i = 0; i < 28; i++) {
 			if (i % 7 == 0 && i != 0) {
 				y++;
 				o = 0;
 			}
 
 			final int state = Game.random.nextInt(Options.mElementsCount);
 
 			final Sign e = this.mSigns.create();
 			e.setCenterPosition(POSITION_START_PADDIG_X + POSITION_ELEMENTS_PADDIG * o, POSITION_START_PADDIG_Y + POSITION_ELEMENTS_PADDIG * y);
 			e.state = state;
 
 			e.setCurrentTileIndex(state);
 
 			o++;
 
 			mElements[o - 1][y] = e;
 
 			e.r = o - 1;
 			e.c = y;
 		}
 
 		this.mTimerSecText.changeTextureRegion(Resources.mTimerSecTextureRegion);
 		for (int i = 0; i < this.mTimerNumbers.getCount(); i++) {
 			this.mTimerNumbers.getByIndex(i).changeTextureRegion(Resources.mBonusNumbersTextureRegion);
 		}
 	}
 
 	public boolean block = false;
 	public boolean cblock = false;
 
 	public void findPairs(final int pFindedState, final int pRow, final int pCell, final boolean pIsRecursive) {
 
 		if (pFindedState == -1 || pRow == -1 || pCell == -1) {
 			return;
 		}
 
 		if (!pIsRecursive) {
 			if (!this.hasNearby(pFindedState, pRow, pCell)) {
 				this.mElements[pRow][pCell].lookFromLeftToRight();
 
 				return;
 			} else {
 				this.markAll(false);
 				this.mShows.clear();
 			}
 
 			this.mLastFindTime = 0f;
 			this.block = true;
 
 			mMustAddScore = 50;
 		}
 
 		if (pRow > 0 && (pRow - 1) != 0 && (pRow - 1) != 6)
 			if (this.mElements[pRow - 1][pCell] != null && !this.mElements[pRow - 1][pCell].w) {
 				if (this.mElements[pRow - 1][pCell].state == pFindedState) {
 					this.mElements[pRow - 1][pCell].destroy(true);
 
 					this.findPairs(pFindedState, pRow - 1, pCell, true);
 
 					mMustAddScore += 50;
 				}
 			}
 
 		if (pRow < 6 && (pRow + 1) != 0 && (pRow + 1) != 6)
 			if (this.mElements[pRow + 1][pCell] != null && !this.mElements[pRow + 1][pCell].w) {
 				if (this.mElements[pRow + 1][pCell].state == pFindedState) {
 					this.mElements[pRow + 1][pCell].destroy(true);
 
 					this.findPairs(pFindedState, pRow + 1, pCell, true);
 
 					mMustAddScore += 50;
 				}
 			}
 
 		if (pCell > 0 && pRow != 0 && pRow != 6)
 			if (this.mElements[pRow][pCell - 1] != null && !this.mElements[pRow][pCell - 1].w) {
 				if (this.mElements[pRow][pCell - 1].state == pFindedState) {
 					this.mElements[pRow][pCell - 1].destroy(true);
 
 					this.findPairs(pFindedState, pRow, pCell - 1, true);
 
 					mMustAddScore += 50;
 				}
 			}
 
 		if (pCell < 3 && pRow != 0 && pRow != 6)
 			if (this.mElements[pRow][pCell + 1] != null && !this.mElements[pRow][pCell + 1].w) {
 				if (this.mElements[pRow][pCell + 1].state == pFindedState) {
 					this.mElements[pRow][pCell + 1].destroy(true);
 
 					this.findPairs(pFindedState, pRow, pCell + 1, true);
 
 					mMustAddScore += 50;
 				}
 			}
 
 		if (!pIsRecursive) {
 			this.block = false;
 
 			if (mMustAddScore > 50) {
 				X++;
 
 				if (this.mExesBackground.getX() > Options.cameraWidth) {
 					this.mExesBackgroundMoveAnimationOn.reset();
 				}
 
 				if (X < 10) {
 					this.mExesNumbers.getByIndex(0).setCurrentTileIndex(10);
 					this.mExesNumbers.getByIndex(1).setCurrentTileIndex(X);
 
 					this.mExesNumbers.getByIndex(2).setVisible(false);
 
 					this.mExesNumbers.getByIndex(0).setCenterPosition(POSITION_EXES_NUMBERS_X_1, POSITION_EXES_NUMBERS_Y);
 					this.mExesNumbers.getByIndex(1).setCenterPosition(POSITION_EXES_NUMBERS_X_2, POSITION_EXES_NUMBERS_Y);
 				} else {
 					this.mExesNumbers.getByIndex(0).setCurrentTileIndex(10);
 					this.mExesNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor(X / 10));
 					this.mExesNumbers.getByIndex(2).setCurrentTileIndex((int) (X - FloatMath.floor(X / 10) * 10));
 
 					this.mExesNumbers.getByIndex(2).setVisible(true);
 
 					this.mExesNumbers.getByIndex(0).setCenterPosition(POSITION_EXES_NUMBERS_X_3, POSITION_EXES_NUMBERS_Y);
 					this.mExesNumbers.getByIndex(1).setCenterPosition(POSITION_EXES_NUMBERS_X_4, POSITION_EXES_NUMBERS_Y);
 					this.mExesNumbers.getByIndex(2).setCenterPosition(POSITION_EXES_NUMBERS_X_5, POSITION_EXES_NUMBERS_Y);
 				}
 
 				for (int i = 0; i < this.mExesNumbers.getCount(); i++) {
 					this.mExesNumbers.getByIndex(i).animate();
 				}
 
 				mMustAddScore *= X;
 
 				Score += mMustAddScore;
 
 				this.mBonusNumbers.getByIndex(0).setVisible(true);
 				
 				if (mMustAddScore < 100) {
 					this.mBonusNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore / 10));
 					this.mBonusNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore % 10));
 					this.mBonusNumbers.getByIndex(1).setVisible(true);
 					this.mBonusNumbers.getByIndex(2).setVisible(true);
 					this.mBonusNumbers.getByIndex(3).setVisible(false);
 					this.mBonusNumbers.getByIndex(4).setVisible(false);
 					this.mBonusNumbers.getByIndex(5).setVisible(false);
 					this.mBonusNumbers.getByIndex(6).setVisible(false);
 					this.mBonusNumbers.getByIndex(7).setVisible(false);
 				} else if (mMustAddScore < 1000) {
 					this.mBonusNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore / 100));
 					this.mBonusNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 100) * 100) / 10));
 					this.mBonusNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore % 10));
 					this.mBonusNumbers.getByIndex(1).setVisible(true);
 					this.mBonusNumbers.getByIndex(2).setVisible(true);
 					this.mBonusNumbers.getByIndex(3).setVisible(true);
 					this.mBonusNumbers.getByIndex(4).setVisible(false);
 					this.mBonusNumbers.getByIndex(5).setVisible(false);
 					this.mBonusNumbers.getByIndex(6).setVisible(false);
 					this.mBonusNumbers.getByIndex(7).setVisible(false);
 				} else if (mMustAddScore < 10000) {
 					this.mBonusNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore / 1000));
 					this.mBonusNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 1000) * 1000) / 100));
 					this.mBonusNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 100) * 100) / 100));
 					this.mBonusNumbers.getByIndex(4).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore % 10));
 					this.mBonusNumbers.getByIndex(1).setVisible(true);
 					this.mBonusNumbers.getByIndex(2).setVisible(true);
 					this.mBonusNumbers.getByIndex(3).setVisible(true);
 					this.mBonusNumbers.getByIndex(4).setVisible(true);
 					this.mBonusNumbers.getByIndex(5).setVisible(false);
 					this.mBonusNumbers.getByIndex(6).setVisible(false);
 					this.mBonusNumbers.getByIndex(7).setVisible(false);
 				} else if (mMustAddScore < 100000) {
 					this.mBonusNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore / 10000));
 					this.mBonusNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 10000) * 10000) / 1000));
 					this.mBonusNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 1000) * 1000) / 100));
 					this.mBonusNumbers.getByIndex(4).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 100) * 100) / 100));
 					this.mBonusNumbers.getByIndex(5).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore % 10));
 					this.mBonusNumbers.getByIndex(1).setVisible(true);
 					this.mBonusNumbers.getByIndex(2).setVisible(true);
 					this.mBonusNumbers.getByIndex(3).setVisible(true);
 					this.mBonusNumbers.getByIndex(4).setVisible(true);
 					this.mBonusNumbers.getByIndex(5).setVisible(true);
 					this.mBonusNumbers.getByIndex(6).setVisible(false);
 					this.mBonusNumbers.getByIndex(7).setVisible(false);
 				} else {
 					this.mBonusNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore / 100000));
 					this.mBonusNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 100000) * 100000) / 10000));
 					this.mBonusNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 10000) * 10000) / 1000));
 					this.mBonusNumbers.getByIndex(4).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 1000) * 1000) / 1000));
 					this.mBonusNumbers.getByIndex(5).setCurrentTileIndex((int) FloatMath.floor((mMustAddScore - FloatMath.floor(mMustAddScore / 100) * 100) / 100));
 					this.mBonusNumbers.getByIndex(6).setCurrentTileIndex((int) FloatMath.floor(mMustAddScore % 10));
 					this.mBonusNumbers.getByIndex(1).setVisible(true);
 					this.mBonusNumbers.getByIndex(2).setVisible(true);
 					this.mBonusNumbers.getByIndex(3).setVisible(true);
 					this.mBonusNumbers.getByIndex(4).setVisible(true);
 					this.mBonusNumbers.getByIndex(5).setVisible(true);
 					this.mBonusNumbers.getByIndex(6).setVisible(true);
 					this.mBonusNumbers.getByIndex(7).setVisible(false);
 				}
 
 				this.mBonusNumbersHolder.setPosition(this.mElements[pRow][pCell].getX(), this.mElements[pRow][pCell].getY());
 
 				if (Options.isSoundEnabled) {
 					switch (X) {
 					default:
 						Options.mSmashSound6.play();
 						break;
 					case 1:
 						Options.mSmashSound1.play();
 						break;
 					case 2:
 						Options.mSmashSound2.play();
 						break;
 					case 3:
 						Options.mSmashSound3.play();
 						break;
 					case 4:
 						Options.mSmashSound4.play();
 						break;
 					case 5:
 						Options.mSmashSound5.play();
 						break;
 					case 6:
 						Options.mSmashSound6.play();
 						break;
 					}
 				}
 
				this.mExes.create().setCenterPosition(POSITION_PANEL_STARS_X_1 + (POSITION_PANEL_STARS_X_2 * ((STARS - FloatMath.floor(STARS / 9) * 9))), Options.cameraCenterY + POSITION_PANEL_STARS_Y_1 + (POSITION_PANEL_STARS_Y_2 * FloatMath.floor(STARS / 9)));
 
 				STARS++;
 			}
 		}
 	}
 
 	public void findPairs(final int pFindedState, final int pRow, final int pCell) {
 		this.findPairs(pFindedState, pRow, pCell, false);
 	}
 
 	boolean z = true;
 
 	private void roll() {
 		if (FAKE_SHOWS == 999) {
 			return;
 		}
 
 		X = 0;
 		FAKE_SHOWS = 999;
 
 		if (this.mExesBackground.getX() < Options.cameraWidth) {
 			this.mExesBackgroundMoveAnimationOff.reset();
 		}
 
 		this.markAll(false);
 		this.mShows.clear();
 		this.mLastFindTime = 0f;
 
 		for (int j = 0; j < this.mElements[0].length; j++) {
 			for (int i = 0; i < this.mElements.length; i++) {
 				this.mElements[i][j].addMove(z ? (-POSITION_ELEMENTS_PADDIG * 7) : (POSITION_ELEMENTS_PADDIG * 7));
 				this.mElements[i][j].state = -2;
 			}
 
 			for (int k = 0; k < 7; k++) {
 				final int state = Game.random.nextInt(Options.mElementsCount);
 
 				final Sign e = this.mSigns.create();
 				e.setCenterPosition(POSITION_START_PADDIG_X + (z ? (+POSITION_ELEMENTS_PADDIG * (k + 7)) : (-POSITION_ELEMENTS_PADDIG * (k + 1))), POSITION_START_PADDIG_Y + POSITION_ELEMENTS_PADDIG * j);
 				e.state = state;
 
 				e.setCurrentTileIndex(state);
 
 				if (z) {
 					e.r = k;
 				} else {
 					e.r = (6 - k);
 				}
 
 				e.c = j;
 				e.addMove(z ? (-POSITION_ELEMENTS_PADDIG * 7) : (POSITION_ELEMENTS_PADDIG * 7));
 
 				e.state = -4;
 
 				if (z) {
 					this.mElements[k][j] = e;
 				} else {
 					this.mElements[(6 - k)][j] = e;
 				}
 
 			}
 
 			z = !z;
 		}
 
 		z = !z;
 	}
 
 	private boolean showPair() {
 		final int r = 1 + Game.random.nextInt(5);
 		final int c = Game.random.nextInt(4);
 
 		if (this.hasNearby(this.mElements[r][c].state, r, c)) {
 			final Shows entity = this.mShows.create();
 			entity.setCenterPosition(this.mElements[r][c].getCenterX(), this.mElements[r][c].getCenterY());
 			entity.setCurrentTileIndex(this.mElements[r][c].state);
 			entity.doNotFade = true;
 
 			FAKE_SHOWS++;
 
 			this.mElements[r][c].marked = true;
 
 			this.markNearby(this.mElements[r][c].state, r, c);
 
 			return true;
 		}
 
 		return false;
 	}
 
 	private boolean hasNearby(final int pFindedState, final int pRow, final int pCell) {
 		if (pRow > 0 && (pRow - 1) != 0 && (pRow - 1) != 6)
 			if (this.mElements[pRow - 1][pCell] != null && !this.mElements[pRow - 1][pCell].w) {
 				if (this.mElements[pRow - 1][pCell].state == pFindedState) {
 					return true;
 				}
 			}
 
 		if (pRow < 6 && (pRow + 1) != 0 && (pRow + 1) != 6)
 			if (this.mElements[pRow + 1][pCell] != null && !this.mElements[pRow + 1][pCell].w) {
 				if (this.mElements[pRow + 1][pCell].state == pFindedState) {
 					return true;
 				}
 			}
 
 		if (pCell > 0 && pRow != 0 && pRow != 6)
 			if (this.mElements[pRow][pCell - 1] != null && !this.mElements[pRow][pCell - 1].w) {
 				if (this.mElements[pRow][pCell - 1].state == pFindedState) {
 					return true;
 				}
 			}
 
 		if (pCell < 3 && pRow != 0 && pRow != 6)
 			if (this.mElements[pRow][pCell + 1] != null && !this.mElements[pRow][pCell + 1].w) {
 				if (this.mElements[pRow][pCell + 1].state == pFindedState) {
 					return true;
 				}
 			}
 
 		return false;
 	}
 
 	public void markNearby(final int pFindedState, final int pRow, final int pCell, final boolean pIsRecursive) {
 
 		if (pFindedState == -1 || pRow == -1 || pCell == -1) {
 			return;
 		}
 
 		if (!pIsRecursive) {
 		}
 
 		if (pRow > 0 && (pRow - 1) != 0 && (pRow - 1) != 6)
 			if (this.mElements[pRow - 1][pCell] != null && !this.mElements[pRow - 1][pCell].w && !this.mElements[pRow - 1][pCell].marked) {
 				if (this.mElements[pRow - 1][pCell].state == pFindedState) {
 					this.mElements[pRow - 1][pCell].marked = true;
 
 					final Shows entity = this.mShows.create();
 					entity.setCenterPosition(this.mElements[pRow - 1][pCell].getCenterX(), this.mElements[pRow - 1][pCell].getCenterY());
 					entity.setCurrentTileIndex(this.mElements[pRow - 1][pCell].state);
 					entity.doNotFade = true;
 
 					FAKE_SHOWS++;
 
 					this.markNearby(pFindedState, pRow - 1, pCell, true);
 
 					this.mElements[pRow - 1][pCell].lookRight();
 
 					if (!pIsRecursive) {
 						this.mElements[pRow][pCell].lookLeft();
 					}
 				}
 			}
 
 		if (pRow < 6 && (pRow + 1) != 0 && (pRow + 1) != 6)
 			if (this.mElements[pRow + 1][pCell] != null && !this.mElements[pRow + 1][pCell].w && !this.mElements[pRow + 1][pCell].marked) {
 				if (this.mElements[pRow + 1][pCell].state == pFindedState) {
 					this.mElements[pRow + 1][pCell].marked = true;
 
 					final Shows entity = this.mShows.create();
 					entity.setCenterPosition(this.mElements[pRow + 1][pCell].getCenterX(), this.mElements[pRow + 1][pCell].getCenterY());
 					entity.setCurrentTileIndex(this.mElements[pRow + 1][pCell].state);
 					entity.doNotFade = true;
 
 					FAKE_SHOWS++;
 
 					this.markNearby(pFindedState, pRow + 1, pCell, true);
 
 					this.mElements[pRow + 1][pCell].lookLeft();
 
 					if (!pIsRecursive) {
 						this.mElements[pRow][pCell].lookRight();
 					}
 				}
 			}
 
 		if (pCell > 0 && pRow != 0 && pRow != 6)
 			if (this.mElements[pRow][pCell - 1] != null && !this.mElements[pRow][pCell - 1].w && !this.mElements[pRow][pCell - 1].marked) {
 				if (this.mElements[pRow][pCell - 1].state == pFindedState) {
 					this.mElements[pRow][pCell - 1].marked = true;
 
 					final Shows entity = this.mShows.create();
 					entity.setCenterPosition(this.mElements[pRow][pCell - 1].getCenterX(), this.mElements[pRow][pCell - 1].getCenterY());
 					entity.setCurrentTileIndex(this.mElements[pRow][pCell - 1].state);
 					entity.doNotFade = true;
 
 					FAKE_SHOWS++;
 
 					this.markNearby(pFindedState, pRow, pCell - 1, true);
 
 					this.mElements[pRow][pCell - 1].lookDown();
 
 					if (!pIsRecursive) {
 						this.mElements[pRow][pCell].lookUp();
 					}
 				}
 			}
 
 		if (pCell < 3 && pRow != 0 && pRow != 6)
 			if (this.mElements[pRow][pCell + 1] != null && !this.mElements[pRow][pCell + 1].w && !this.mElements[pRow][pCell + 1].marked) {
 				if (this.mElements[pRow][pCell + 1].state == pFindedState) {
 					this.mElements[pRow][pCell + 1].marked = true;
 
 					final Shows entity = this.mShows.create();
 					entity.setCenterPosition(this.mElements[pRow][pCell + 1].getCenterX(), this.mElements[pRow][pCell + 1].getCenterY());
 					entity.setCurrentTileIndex(this.mElements[pRow][pCell + 1].state);
 					entity.doNotFade = true;
 
 					FAKE_SHOWS++;
 
 					this.markNearby(pFindedState, pRow, pCell + 1, true);
 
 					this.mElements[pRow][pCell + 1].lookUp();
 
 					if (!pIsRecursive) {
 						this.mElements[pRow][pCell].lookDown();
 					}
 				}
 			}
 
 	}
 
 	public void markNearby(final int pFindedState, final int pRow, final int pCell) {
 		this.markNearby(pFindedState, pRow, pCell, false);
 	}
 
 	public void markAll(final boolean mark) {
 		for (int j = 0; j < this.mElements[0].length; j++) {
 			for (int i = 0; i < this.mElements.length; i++) {
 				this.mElements[i][j].marked = mark;
 			}
 		}
 	}
 
 	// ===========================================================
 	// Virtual methods
 	// ===========================================================
 
 	@Override
 	public void onManagedUpdate(final float pSecondsElapsed) {
 		super.onManagedUpdate(pSecondsElapsed);
 
 		if (!this.running) {
 			return;
 		}
 
 		/* Score */
 		if (score < Score) {
 			if (Score - score > 1000) {
 				score += 1111;
 			} else if (Score - score > 100) {
 				score += 111;
 			} else if (Score - score > 10) {
 				score += 11;
 			} else {
 				score++;
 			}
 		}
 
 		if (score < 10) {
 			this.mScoreNumbers.getByIndex(0).setCurrentTileIndex(score);
 			this.mScoreNumbers.getByIndex(0).setVisible(true);
 			this.mScoreNumbers.getByIndex(1).setVisible(false);
 			this.mScoreNumbers.getByIndex(2).setVisible(false);
 			this.mScoreNumbers.getByIndex(3).setVisible(false);
 			this.mScoreNumbers.getByIndex(4).setVisible(false);
 			this.mScoreNumbers.getByIndex(5).setVisible(false);
 			this.mScoreNumbers.getByIndex(6).setVisible(false);
 		} else if (score < 100) {
 			this.mScoreNumbers.getByIndex(0).setCurrentTileIndex((int) FloatMath.floor(score / 10));
 			this.mScoreNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor(score % 10));
 			this.mScoreNumbers.getByIndex(0).setVisible(true);
 			this.mScoreNumbers.getByIndex(1).setVisible(true);
 			this.mScoreNumbers.getByIndex(2).setVisible(false);
 			this.mScoreNumbers.getByIndex(3).setVisible(false);
 			this.mScoreNumbers.getByIndex(4).setVisible(false);
 			this.mScoreNumbers.getByIndex(5).setVisible(false);
 			this.mScoreNumbers.getByIndex(6).setVisible(false);
 		} else if (score < 1000) {
 			this.mScoreNumbers.getByIndex(0).setCurrentTileIndex((int) FloatMath.floor(score / 100));
 			this.mScoreNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 100) * 100) / 10));
 			this.mScoreNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor(score % 10));
 			this.mScoreNumbers.getByIndex(0).setVisible(true);
 			this.mScoreNumbers.getByIndex(1).setVisible(true);
 			this.mScoreNumbers.getByIndex(2).setVisible(true);
 			this.mScoreNumbers.getByIndex(3).setVisible(false);
 			this.mScoreNumbers.getByIndex(4).setVisible(false);
 			this.mScoreNumbers.getByIndex(5).setVisible(false);
 			this.mScoreNumbers.getByIndex(6).setVisible(false);
 		} else if (score < 10000) {
 			this.mScoreNumbers.getByIndex(0).setCurrentTileIndex((int) FloatMath.floor(score / 1000));
 			this.mScoreNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 1000) * 1000) / 100));
 			this.mScoreNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 100) * 100) / 10));
 			this.mScoreNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor(score % 10));
 			this.mScoreNumbers.getByIndex(0).setVisible(true);
 			this.mScoreNumbers.getByIndex(1).setVisible(true);
 			this.mScoreNumbers.getByIndex(2).setVisible(true);
 			this.mScoreNumbers.getByIndex(3).setVisible(true);
 			this.mScoreNumbers.getByIndex(4).setVisible(false);
 			this.mScoreNumbers.getByIndex(5).setVisible(false);
 			this.mScoreNumbers.getByIndex(6).setVisible(false);
 		} else if (score < 100000) {
 			this.mScoreNumbers.getByIndex(0).setCurrentTileIndex((int) FloatMath.floor(score / 10000));
 			this.mScoreNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 10000) * 10000) / 1000));
 			this.mScoreNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 1000) * 1000) / 100));
 			this.mScoreNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 100) * 100) / 10));
 			this.mScoreNumbers.getByIndex(4).setCurrentTileIndex((int) FloatMath.floor(score % 10));
 			this.mScoreNumbers.getByIndex(0).setVisible(true);
 			this.mScoreNumbers.getByIndex(1).setVisible(true);
 			this.mScoreNumbers.getByIndex(2).setVisible(true);
 			this.mScoreNumbers.getByIndex(3).setVisible(true);
 			this.mScoreNumbers.getByIndex(4).setVisible(true);
 			this.mScoreNumbers.getByIndex(5).setVisible(false);
 			this.mScoreNumbers.getByIndex(6).setVisible(false);
 		} else if (score < 1000000) {
 			this.mScoreNumbers.getByIndex(0).setCurrentTileIndex((int) FloatMath.floor(score / 100000));
 			this.mScoreNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 100000) * 100000) / 10000));
 			this.mScoreNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 10000) * 10000) / 1000));
 			this.mScoreNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 1000) * 1000) / 100));
 			this.mScoreNumbers.getByIndex(4).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 100) * 100) / 10));
 			this.mScoreNumbers.getByIndex(5).setCurrentTileIndex((int) FloatMath.floor(score % 10));
 			this.mScoreNumbers.getByIndex(0).setVisible(true);
 			this.mScoreNumbers.getByIndex(1).setVisible(true);
 			this.mScoreNumbers.getByIndex(2).setVisible(true);
 			this.mScoreNumbers.getByIndex(3).setVisible(true);
 			this.mScoreNumbers.getByIndex(4).setVisible(true);
 			this.mScoreNumbers.getByIndex(5).setVisible(true);
 			this.mScoreNumbers.getByIndex(6).setVisible(false);
 		} else {
 			this.mScoreNumbers.getByIndex(0).setCurrentTileIndex((int) FloatMath.floor(score / 1000000));
 			this.mScoreNumbers.getByIndex(1).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 1000000) * 1000000) / 100000));
 			this.mScoreNumbers.getByIndex(2).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 100000) * 100000) / 10000));
 			this.mScoreNumbers.getByIndex(3).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 10000) * 10000) / 1000));
 			this.mScoreNumbers.getByIndex(4).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 1000) * 1000) / 100));
 			this.mScoreNumbers.getByIndex(5).setCurrentTileIndex((int) FloatMath.floor((score - FloatMath.floor(score / 100) * 100) / 10));
 			this.mScoreNumbers.getByIndex(6).setCurrentTileIndex((int) FloatMath.floor(score % 10));
 			this.mScoreNumbers.getByIndex(0).setVisible(true);
 			this.mScoreNumbers.getByIndex(1).setVisible(true);
 			this.mScoreNumbers.getByIndex(2).setVisible(true);
 			this.mScoreNumbers.getByIndex(3).setVisible(true);
 			this.mScoreNumbers.getByIndex(4).setVisible(true);
 			this.mScoreNumbers.getByIndex(5).setVisible(true);
 			this.mScoreNumbers.getByIndex(6).setVisible(true);
 		}
 
 		this.mTimeBeforeHaveOneMinuteAlert += pSecondsElapsed;
 		this.mHarryTime += pSecondsElapsed;
 		this.mTime += pSecondsElapsed;
 		this.mLastFindTime += pSecondsElapsed;
 		this.mTimerTime -= pSecondsElapsed;
 		this.mTimerAnimationTime += pSecondsElapsed;
 
 		if (!this.mIsHaveOneMinuteAnimationShows) {
 			if (this.mTimeBeforeHaveOneMinuteAlert >= 1f && !this.mBestScoreAlert.isAvailable()) {
 				this.mHaveOneMinuteAnimationOn.reset();
 
 				this.mTimeBeforeHaveOneMinuteAlert = 0;
 				this.mIsHaveOneMinuteAnimationShows = true;
 			}
 		} else {
 			if (this.mTimeBeforeHaveOneMinuteAlert >= 1f) {
 				this.mHaveOneMinuteAnimationOff.reset();
 
 				this.mTimeBeforeHaveOneMinuteAlert = -Float.MAX_VALUE;
 			}
 		}
 
 		if (this.mHarryTime >= 50f) {
 			if (!this.mIsHarryUpAnimationShows) {
 				this.mHarryUpAnimationOn.reset();
 
 				this.mIsHarryUpAnimationShows = true;
 				this.mHarryTime = 49f;
 			} else {
 				this.mHarryUpAnimationOff.reset();
 
 				this.mHarryTime = -Float.MAX_VALUE;
 			}
 		}
 
 		if (!this.mIsBestScoreAnimationShows) {
 			if (Score > mBestScore && mBestScore != 0 && !this.mHarryUpAlert.isAvailable() && !this.mOneMinuteAlert.isAvailable()) {
 				this.mBestScoreAnimationOn.reset();
 
 				this.mIsBestScoreAnimationShows = true;
 			}
 		} else {
 			this.mBestScoreTime += pSecondsElapsed;
 
 			if (this.mBestScoreTime >= 1f) {
 				this.mBestScoreAnimationOff.reset();
 
 				this.mBestScoreTime = -Float.MAX_VALUE;
 			}
 		}
 
 		if (this.mTime >= 60f && !this.hasChildScene()) {
 			Game.mScreens.setChildScreen(Game.mScreens.get(Screen.LEVELEND), false, false, true);
 		}
 
 		if (this.mLastFindTime >= 3f) {
 			if (this.showPair()) {
 
 				this.mLastFindTime = -Float.MAX_VALUE;
 			} else {
 				if (this.mLastFindTime >= 5f) {
 					this.roll();
 				}
 			}
 		}
 
 		if (FAKE_SHOWS == 999) {
 			this.mBlockTime += pSecondsElapsed;
 
 			if (this.mBlockTime >= 1.5f) {
 				FAKE_SHOWS = 0;
 				this.mBlockTime = 0;
 			}
 		}
 
 		if (this.mTimerAnimationTime >= 1f) {
 			this.mTimerAnimationTime = 0f;
 
 			if (this.mTimerTime <= 11) {
 				this.mTimerSecText.animate();
 
 				if (this.mTimerNumbers.getByIndex(0).getTextureRegion().e(Resources.mBonusNumbersTextureRegion)) {
 
 					this.mTimerSecText.changeTextureRegion(Resources.mRedTimerSecTextureRegion);
 					for (int i = 0; i < this.mTimerNumbers.getCount(); i++) {
 						this.mTimerNumbers.getByIndex(i).changeTextureRegion(Resources.mRedBonusNumbersTextureRegion);
 					}
 				}
 			}
 
 			if (this.mTimerTime < 10) {
 				this.mTimerNumbers.getByIndex(0).setCurrentTileIndex((int) this.mTimerTime);
 
 				this.mTimerNumbers.getByIndex(1).setVisible(false);
 
 				this.mTimerNumbers.getByIndex(0).setCenterPosition(POSITION_TIMER_NUMBER_X3, Options.cameraHeight - POSITION_TIMER_NUMBER_Y);
 
 				if (Options.isSoundEnabled) {
 					Options.mClockSound.play();
 				}
 			} else {
 				this.mTimerNumbers.getByIndex(0).setCurrentTileIndex((int) FloatMath.floor(this.mTimerTime / 10));
 				this.mTimerNumbers.getByIndex(1).setCurrentTileIndex((int) (this.mTimerTime - FloatMath.floor(this.mTimerTime / 10) * 10));
 
 				this.mTimerNumbers.getByIndex(1).setVisible(true);
 
 				this.mTimerNumbers.getByIndex(0).setCenterPosition(POSITION_TIMER_NUMBER_X1, Options.cameraHeight - POSITION_TIMER_NUMBER_Y);
 				this.mTimerNumbers.getByIndex(1).setCenterPosition(POSITION_TIMER_NUMBER_X2, Options.cameraHeight - POSITION_TIMER_NUMBER_Y);
 			}
 
 			for (int i = 0; i < this.mTimerNumbers.getCount(); i++) {
 				this.mTimerNumbers.getByIndex(i).animate();
 			}
 		}
 	}
 
 	@Override
 	public void onAttached() {
 		super.onAttached();
 
 		this.init();
 
 		if (Options.isMusicEnabled) {
 			Options.mLevelSound.play();
 
 			if (Options.mMainSound.isPlaying()) {
 				Options.mMainSound.pause();
 			}
 
 			if (Options.mEndSound.isPlaying()) {
 				Options.mEndSound.pause();
 			}
 		}
 	}
 
 	@Override
 	public void onPostAttached() {
 		Game.mAdvertisementManager.showSmall();
 	}
 
 	@Override
 	public void onDetached() {
 		super.onDetached();
 		
 		Game.mAdvertisementManager.hideSmall();
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (this.hasChildScene()) {
 			if (this.getChildScene().equals(Game.mScreens.get(Screen.RESTART))) {
 
 			} else {
 				Game.mScreens.clearChildScreens();
 			}
 		} else {
 			Game.mScreens.setChildScreen(Game.mScreens.get(Screen.PAUSE), false, false, true);
 		}
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 }
