 package br.edu.ifpr.ThiagoRomano.Camaleao;
 
 import java.util.Random;
 
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.shader.constants.ShaderProgramConstants;
 import org.andengine.util.modifier.ease.EaseElasticOut;
 
 import android.graphics.Color;
 import android.view.KeyEvent;
 
 public class NinjaModeScene extends GameScene {
 
 	static final float STARTING_TIME = 30f;
 	static final float TIME_CORRECT = 3f;
 	public final int[] CORES = { 0, 0, 0, 255, 0, 0, 0, 255, 0, 0, 0, 255, 0,
 			255, 255, 255, 255, 0, 100, 100, 100, };
 	static final int X_PONTUACAO_INICIAL = 367;
 	static final int Y_PONTUACAO_INICIAL = 0;
 
 	CamaleaotestActivity activity;
 	Sprite mBlue;
 	Sprite mRed;
 	Sprite mGreen;
 	Sprite mLetraR;
 	Sprite mLetraG;
 	Sprite mLetraB;
 	SliderSprite mSliderBlue;
 	SliderSprite mSliderRed;
 	SliderSprite mSliderGreen;
 	Sprite mAdendo;
 	Sprite mChampShadow;
 	Sprite mTatame;
 	Sprite mLingua;
 	Sprite mPauseButton;
 
 	Chronometer mChronometer;
 
 	Random rand;
 	// Text mTextRemainingTime;
 	Sprite mBackground;
 	Sprite mPlaca;
 	Sprite mPlacaSaindo;
 	Pontuacao mPontuacao;
 
 	int mPlacaColor;
 	int mActualColor = 0;
 	public int theColorLocation = ShaderProgramConstants.LOCATION_INVALID;
 	// public boolean mOverlayed = true;
 	float remainingTime = STARTING_TIME;
 	int score = 0;
 
 	private PlacaNinjaScreen mPlacaStart;
 
 	public NinjaModeScene() {
 
 		rand = new Random();
 		this.setOnAreaTouchTraversalFrontToBack();
 		colors = Color.rgb(255, 255, 255);
 		mPlacaColor = 0xff00ffff;
 		activity = CamaleaotestActivity.getSharedInstance();
 
 		createAssets();
 		createPauseMenu();
 		mConfirmExit = new ConfirmExitScene(this);
 		mConfirmRestart = new ConfirmRestartScene(this);
 		setTouchAreaBindingOnActionDownEnabled(true);
 		registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				if (!iniciando) {
 					remainingTime -= pSecondsElapsed;
 					updateTime();
 				}
 			}
 
 			@Override
 			public void reset() {
 				// TODO Auto-generated method stub
 
 			}
 		});
 	}
 
 	private void createAssets() {
 		/*
 		 * mTextRemainingTime = new Text(10, 10, activity.mFont, "", 10,
 		 * activity.getVertexBufferObjectManager());
 		 */
 
 		updateTime();
 		mBackground = new Sprite(0, 0,
 				activity.mSpritesheetTexturePackTextureRegionLibrary2
 						.get(posicoes2.FUNDO_DOJO_ID),
 				activity.getVertexBufferObjectManager());
 		mLingua = new Sprite(214, 157,
 				activity.mSpritesheetTexturePackTextureRegionLibrary2
 						.get(posicoes2.NINJA_LINGUA_ID),
 				activity.getVertexBufferObjectManager());
 
 		mTatame = new Sprite(35, 399,
 				activity.mSpritesheetTexturePackTextureRegionLibrary2
 						.get(posicoes2.TATAME_ID),
 				activity.getVertexBufferObjectManager());
 
 		mAdendo = new Sprite(-16, -15,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NYLON_DETALHES_ID),
 				activity.getVertexBufferObjectManager());
 
 		mPlaca = new Sprite(83, 119,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NYLON_TROCACOR_ID),
 				activity.getVertexBufferObjectManager());
 
 		mPlaca.setColor(colorIntToFloat(Color.red(mPlacaColor)),
 				colorIntToFloat(Color.green(mPlacaColor)),
 				colorIntToFloat(Color.blue(mPlacaColor)));
 
 		mChampShadow = new Sprite(48, 71,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NINJA_SHADOW_ID),
 				activity.getVertexBufferObjectManager());
 
 		mChamp = new Sprite(48, 71,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.NINJA_BASE_ID),
 				activity.getVertexBufferObjectManager());
 
 		mRed = new BarSprite(0, this, 112, 523, activity.mTextureBar,
 				activity.getVertexBufferObjectManager());
 
 		mGreen = new BarSprite(1, this, 112, 615, activity.mTextureBar,
 				activity.getVertexBufferObjectManager());
 
 		mBlue = new BarSprite(2, this, 112, 706, activity.mTextureBar,
 				activity.getVertexBufferObjectManager());
 
 		mLetraR = new Sprite(67, 492,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.R_ID),
 				activity.getVertexBufferObjectManager());
 		mLetraG = new Sprite(64, 587,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.G_ID),
 				activity.getVertexBufferObjectManager());
 		mLetraB = new Sprite(65, 685,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.B_ID),
 				activity.getVertexBufferObjectManager());
 
 		mSliderRed = new SliderSprite(0, this, 372, 505,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.MARCADOR_ID),
 				activity.getVertexBufferObjectManager());
 
 		mSliderGreen = new SliderSprite(1, this, 372, 597,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.MARCADOR_ID),
 				activity.getVertexBufferObjectManager());
 
 		mSliderBlue = new SliderSprite(2, this, 372, 688,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.MARCADOR_ID),
 				activity.getVertexBufferObjectManager());
 
 		mBlackBehind = new Sprite(0, 0,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.BLACK_BEHIND_ID),
 				activity.getVertexBufferObjectManager());
 		mPauseButton = new Sprite(399, 70,
 				activity.mSpritesheetTexturePackTextureRegionLibrary
 						.get(posicoes.PAUSE_ID),
 				activity.getVertexBufferObjectManager()) {
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				setChildScene(mMenuScene, false, true, true);
 				toggleEscuro(true);
 				return true;
 			}
 		};
 
 		mChronometer = new Chronometer(this,
 				activity.getVertexBufferObjectManager());
 
 		mPontuacao = new Pontuacao(X_PONTUACAO_INICIAL, Y_PONTUACAO_INICIAL, 3,
 				this, activity.getVertexBufferObjectManager());
 
 		this.attachChild(mBackground);
 		this.attachChild(mTatame);
 
 		this.attachChild(mPlaca);
 		mPlaca.attachChild(mAdendo);
 
 		this.attachChild(mLingua);
 
 		this.attachChild(mChamp);
 		this.attachChild(mChampShadow);
 
 		this.attachChild(mRed);
 		this.attachChild(mGreen);
 		this.attachChild(mBlue);
 
 		this.attachChild(mLetraR);
 		this.attachChild(mLetraG);
 		this.attachChild(mLetraB);
 
 		this.attachChild(mSliderRed);
 		registerTouchArea(mSliderRed);
 
 		this.attachChild(mSliderGreen);
 		registerTouchArea(mSliderGreen);
 
 		this.attachChild(mSliderBlue);
 		registerTouchArea(mSliderBlue);
 
 		this.attachChild(mPauseButton);
 		registerTouchArea(mPauseButton);
 
 		// this.attachChild(mTextRemainingTime);
 		this.attachChild(mPontuacao);
 		movements = new MoveModifier(2f, -mPlaca.getWidth(), mPlaca.getX(),
 				mPlaca.getY(), mPlaca.getY(), EaseElasticOut.getInstance());
 
 		this.attachChild(mBlackBehind);
 		mBlackBehind.setVisible(false);
 
 		mPlaca.registerEntityModifier(movements);
 		movements.setAutoUnregisterWhenFinished(false);
 
 		mPlacaStart = new PlacaNinjaScreen(
 				activity.mSpritesheetTexturePackTextureRegionLibrary.get(
 						posicoes.TABUAPAUSA_ID).getTexture(), this);
 		registerTouchArea(mPlacaStart.goMenuItem);
 		this.attachChild(mPlacaStart);
 		this.attachChild(mChronometer);
 		mChronometer.updateTime(STARTING_TIME);
 		// setTouchAreaBindingOnActionMoveEnabled(true);
 		// this.setChildScene(new PlacaNinjaScreen(this), false, true, true);
 
 	}
 
 	@Override
 	public void restart() {
 		remainingTime = STARTING_TIME;
 
 		score = 0;
 		mActualColor = 0;
 		updateTime();
 		updateScore();
 		this.reset();
 		registerTouchArea(mPlacaStart.goMenuItem);
 		mChronometer.restart();
 
 		iniciando = true;
 		// this.setChildScene(new PlacaNinjaScreen(this),false,true,true);
 	}
 
 	private void createPauseMenu() {
 		mMenuScene = new PauseMenu(this);
 	}
 
 	@Override
 	public void ChangeColors(float newColor, int index) {
 		int iNewColor = (int) (newColor * 255);
 		// colors = (0xff00ffff >> (2*index))&((int)Math.floor(newColor*255));
 		int mask = (0xff00ffff >> (8 * index));
 		iNewColor <<= 16;
 		iNewColor >>= index * 8;
 		// a.setColor(pColor)
 
 		colors = (colors & mask);
 		colors |= iNewColor;
 		mChamp.setColor(Color.red(colors) / 256f, Color.green(colors) / 256f,
 				Color.blue(colors) / 256f);
 
 		int redStepsDiference = Math.abs(Color.red(colors) / mSliderRed.mStep
 				- Color.red(mPlacaColor) / mSliderRed.mStep);
 
 		int greenStepsDiference = Math.abs(Color.green(colors)
 				/ mSliderGreen.mStep - Color.green(mPlacaColor)
 				/ mSliderGreen.mStep);
 		int blueStepsDiference = Math.abs(Color.blue(colors)
 				/ mSliderBlue.mStep - Color.blue(mPlacaColor)
 				/ mSliderBlue.mStep);
 		/*
 		 * mTextRemainingTime.setText(Integer.toString(redStepsDiference +
 		 * greenStepsDiference + blueStepsDiference));
 		 */
 		if (redStepsDiference < 50
 				&& greenStepsDiference < 50
 				&& blueStepsDiference < 50
 				&& redStepsDiference + blueStepsDiference + greenStepsDiference < 80)
 			nextColor();
 	}
 
 	MoveModifier movements;
 
 	public void nextColor() {
 		activity.getEngine().vibrate(200);
 		if (score <= 50) {
 			remainingTime += 4;
 		} else if (score <= 100) {
 			remainingTime += 3;
 		} else if (score <= 150) {
 			remainingTime += 2;
 		} else {
 			remainingTime += 1;
 		}
 
 		mPlacaColor = Color.rgb(rand.nextInt(255), rand.nextInt(255),
 				rand.nextInt(255));
 
 		setPlacaColor(mPlacaColor);
 
 		// mPlaca.setX(-mPlaca.getX());
 
 		// animacao entrando
 		movements.reset();
 		score++;
 		updateScore();
 		mActualColor++;
 	}
 
 	private void updateScore() {
 		mPontuacao.updateScore(score);
 	}
 
 	private void updateTime() {
 		if (mChronometer != null) {
 			mChronometer.updateTime(remainingTime);
 		}
 	}
 
 	public int colorFloatToInt(float number) {
 		return (int) (number * 255);
 	}
 
 	public float colorIntToFloat(int number) {
 		return number / 255f;
 	}
 
 	public void setPlacaColor(int color) {
 		mPlaca.setColor(colorIntToFloat(Color.red(mPlacaColor)),
 				colorIntToFloat(Color.green(mPlacaColor)),
 				colorIntToFloat(Color.blue(mPlacaColor)));
 	}
 
 	@Override
 	public boolean handleKeyDown(int pKeyCode, KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_MENU
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if (this.hasChildScene()) {
 				/* Remove the menu and reset it. */
 				this.mMenuScene.back();
 				this.mConfirmExit.back();
 				this.mConfirmRestart.back();
 				if (!this.hasChildScene()) {
 					toggleEscuro(false);
 				}
 
 			} else {
 				/* Attach the menu. */
 				if (!mPlacaStart.isVisible()) {
 					this.setChildScene(this.mMenuScene, false, true, true);
 					toggleEscuro(true);
 				}
 			}
 			return true;
 		} else if (pKeyCode == KeyEvent.KEYCODE_BACK
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if (this.hasChildScene()) {
 				/* Remove the menu and reset it. */
 				clearChildScenes();
 				if (!this.hasChildScene()) {
 					toggleEscuro(false);
 				}
 			} else {
 				/* Attach the confirm. */
 				this.setChildScene(this.mConfirmExit, false, true, true);
 				toggleEscuro(true);
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public void clearChildScenes() {
 		this.mMenuScene.back();
 		this.mConfirmExit.back();
 	}
 
 	StatsScreen stats;
 
 	public void endTime() {
 		// TODO tocaFimDeJogo
 		// TODO showScore
 		// mChronometer.registerEntityModifier(new FadeOutModifier(3));
 		if (stats == null)
 			stats = new StatsScreen(this);
 		mBlackBehind.setVisible(true);
 		stats.updateText(score);
 		this.setChildScene(stats, false, true, true);
 	}
 }
