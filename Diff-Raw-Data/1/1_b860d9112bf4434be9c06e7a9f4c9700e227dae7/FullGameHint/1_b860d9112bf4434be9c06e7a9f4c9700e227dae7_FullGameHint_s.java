 package com.turpgames.ichigu.model;
 
 import com.turpgames.framework.v0.IDrawable;
 import com.turpgames.framework.v0.effects.IEffectEndListener;
 import com.turpgames.framework.v0.forms.xml.Toast;
 import com.turpgames.framework.v0.util.Timer;
 import com.turpgames.framework.v0.util.Vector;
 import com.turpgames.ichigu.utils.R;
 
 class FullGameHint implements IDrawable, IEffectEndListener, Toast.IToastListener {
 	private final static int notificationInterval = 30;
 
 	private IchiguImageButton button;
 	private FullGameIchiguInfo ichiguInfo;
 	private Card[] cards;
 	private String text;
 	private int hintIndex;
 	private boolean isActive;
 	private Timer notificationTimer;
 	private int colorIndex;
 
 	private Toast toast;
 
 	FullGameHint() {
 		button = new IchiguImageButton();
 		button.setTexture(R.game.textures.hint);
 		button.setListener(new IIchiguButtonListener() {
 			@Override
 			public void onButtonTapped() {
 				showNextHint();
 				restartNotificationTimer();
 			}
 		});
 
 		ichiguInfo = new FullGameIchiguInfo();
 
 		notificationTimer = new Timer();
 		notificationTimer.setInterval(notificationInterval);
 		notificationTimer.setTickListener(new Timer.ITimerTickListener() {
 			@Override
 			public void timerTick(Timer timer) {
 				button.blink(null, false);
 			}
 		});
 
 		toast = new Toast();
 		toast.setListener(this);
 	}
 
 	public Vector getLocation() {
 		return button.getLocation();
 	}
 
 	public float getWidth() {
 		return button.getWidth();
 	}
 
 	public float getHeight() {
 		return button.getHeight();
 	}
 
 	public void restartNotificationTimer() {
 		notificationTimer.restart();
 	}
 
 	public void update(Card[] cards) {
 		ichiguInfo.update(cards);
 		updateText();
 		hintIndex = 0;
 		isActive = false;
 		this.cards = cards;
 	}
 
 	public int getIchiguCount() {
 		return ichiguInfo.getIchiguCount();
 	}
 
 	private void showNextHint() {
 		if (isActive) {
 			toast.hide();
 			return;
 		}
 
 		if (hintIndex == 0) {
 			setToastColor();
 			toast.show(text, 3000);
 		}
 		else {
 			int cardIndex = ichiguInfo.getIchiguCardIndex(hintIndex - 1, 1);
 			cards[cardIndex].blink(this, false);
 		}
 
 		isActive = true;
 	}
 
 	private void setToastColor() {
 		colorIndex++;
 
 		if (colorIndex % 3 == 0)
 			toast.getColor().set(R.colors.ichiguRed);
 		else if (colorIndex % 3 == 1)
 			toast.getColor().set(R.colors.ichiguGreen);
 		else if (colorIndex % 3 == 2)
 			toast.getColor().set(R.colors.ichiguBlue);
 
 		toast.getColor().a = 0.85f;
 	}
 
 	private void updateText() {
 		int count = ichiguInfo.getIchiguCount();
 		if (count < 1)
 			text = "No ichigu exists on table";
 		else if (count == 1)
 			text = "1 ichigu exists on table";
 		else
 			text = count + " ichigus exist on table";
 	}
 
 	private void hintEnd() {
 		hintIndex = (hintIndex + 1) % (1 + ichiguInfo.getIchiguCount());
 		isActive = false;
 	}
 
 	@Override
 	public boolean onEffectEnd(Object card) {
 		hintEnd();
 		return true;
 	}
 
 	@Override
 	public void onToastHidden(Toast toast) {
 		hintEnd();
 	}
 
 	@Override
 	public void draw() {
 		drawButton();
 	}
 
 	private void drawButton() {
 		button.draw();
 	}
 
 	public void activate() {
 		button.listenInput(true);
 	}
 
 	public void deactivate() {
 		button.listenInput(false);
 		toast.dispose();
 	}
 }
