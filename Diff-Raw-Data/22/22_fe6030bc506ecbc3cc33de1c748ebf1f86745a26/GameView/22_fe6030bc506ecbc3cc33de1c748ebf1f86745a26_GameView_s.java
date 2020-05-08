 package com.undi.crazy8;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.undi.crazy8.CardRef.Rank;
 import com.undi.crazy8.ui.UIButton;
 import com.undi.crazy8.ui.UICallback;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GameView extends View {
 	private AnimatedCardGraphic cardGraphic;
 	private UIButton nextCardButton;
 	private Context context;
 	private float scale;
 	private Paint whitePaint;
 	private Crazy8Game game;
 	private int screenH, screenW;
 	private int movingCardIdx = -1;
 	private int movingCardX, movingCardY;
 	Dialog endHandDialog = null;
 	private boolean showingChooseSuit = false;
 	
 	public GameView(Context context) {
 		super(context);
 		this.context = context;
 		
 		cardGraphic = new AnimatedCardGraphic(this.context, 0.1f);
 		
 		scale = context.getResources().getDisplayMetrics().scaledDensity;
 		whitePaint = new Paint();
 		whitePaint.setAntiAlias(true);
 		whitePaint.setColor(Color.WHITE);
 		whitePaint.setStyle(Paint.Style.STROKE);
 		whitePaint.setTextAlign(Paint.Align.LEFT);
 		//Set to roughly 15pt font, scaled for the screen
 		whitePaint.setTextSize(scale * 15);
 		
 		Bitmap nextCardGraphic = BitmapFactory.decodeResource(getResources(), R.drawable.next_button);
 		nextCardButton = new UIButton(nextCardGraphic, 0.8, 0.8, 0.1, screenW, screenH, new UICallback() {
 			@Override
 			public void run(Object... args) {
 				//Rotate the cards in the player's hand
 				Collections.rotate(game.getPlayerHand(), 1);
 			}
 		});
 		
 		game = new Crazy8Game(context);
 		game.startGame();
 		if(!game.isPlayerTurn()){
 			showComputerTurn();
 		}
 		//Testing code for win dialog
 		//CardMgr.reshuffle(game.getDeck(), game.getOppHand());
 		//CardMgr.reshuffle(game.getDeck(), game.getPlayerHand());
 	}
 	
 	private int getPlayerCardDrawX(int idx){
 		return 5 + (cardGraphic.getCardWidth() + 5) * idx;
 	}
 	private int getPlayerCardDrawY(){
 		return (int) (screenH - cardGraphic.getCardHeight() - whitePaint.getTextSize() - (50 * scale));
 	}
 	
 	private int getOpponentCardDrawY(){
 		return (int) (whitePaint.getTextSize() + (50 * scale));
 	}
 	private int getOpponentCardDrawX(){
 		return 5;
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas){
 		canvas.drawText("Player Score: " + Integer.toString(game.getPlayerScore()), 10, 
 				screenH - whitePaint.getTextSize() - 10 , whitePaint);
 		canvas.drawText("Computer Score: " + Integer.toString(game.getOppScore()), 10, 
 				whitePaint.getTextSize() + 10 , whitePaint);
 		
 		//draw the player's hand
 		int drawY = getPlayerCardDrawY();
 		List<CardRef> playerHand = game.getPlayerHand();
 		int cardsToDraw = Math.min(7, playerHand.size());
 		for(int i = 0; i < cardsToDraw; i++){
 			if(i != movingCardIdx){
 				CardRef card = playerHand.get(i);
 				cardGraphic.drawCard(canvas, card, getPlayerCardDrawX(i), drawY);
 			}
 		}
 		if(playerHand.size() > 7){
 			nextCardButton.draw(canvas);
 		}
 
 		//draw the opponent's hand
 		int drawX = getOpponentCardDrawX();
 		drawY = getOpponentCardDrawY();
 		cardsToDraw = game.getOppHand().size();
 		for(int i = 0; i < cardsToDraw; i++){
 			cardGraphic.drawCardBack(canvas, drawX, drawY);
 			drawX += 5;
 		}
 		
 		//Draw the draw pile
 		drawX = (screenW / 2) - cardGraphic.getCardWidth() - 10;
 		drawY = (screenH / 2) - (cardGraphic.getCardHeight() / 2);
 		cardGraphic.drawCardBack(canvas, drawX, drawY);
 		
 		//Draw the discard pile
 		if(!game.getDiscardPile().isEmpty()){
 			drawX = (screenW / 2) + 10;
 			drawY = (screenH / 2) - (cardGraphic.getCardHeight() / 2);
 			cardGraphic.drawCard(canvas, game.getDiscardPile().get(0), drawX, drawY);
 		}
 		
 		//Draw the animations
 		cardGraphic.drawAnimations(canvas, this);
 		
 		//Draw the moving card
 		if(movingCardIdx != -1){
 			cardGraphic.drawCard(canvas, game.getPlayerHand().get(movingCardIdx), 
 					//movingCardX - cardGraphic.getCardWidth() / 2, movingCardY - cardGraphic.getCardHeight() / 2); //center of card
 					//movingCardX, movingCardY); //top left of card
 					movingCardX - cardGraphic.getCardWidth(), movingCardY - cardGraphic.getCardHeight());
 		}
 	}
 	
 	@Override
 	public void onSizeChanged(int w, int h, int oldw, int oldh){
 		nextCardButton.rescaleGraphic(w, h);
 		cardGraphic.onResize(w, h);
 		screenH = h;
 		screenW = w;
 	}
 
 	/**
 	 * Returns the index into the player's hand under x, y.. or -1 if no card
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	private int playerCardAtLocation(int x, int y){
 		int startX = 5;
 		int startY = (int) (screenH - cardGraphic.getCardHeight() - whitePaint.getTextSize() - (50 * scale));
 		int height = cardGraphic.getCardHeight();
 		//7 cards, plus 6 5-pixel gaps
 		int cardWidth = cardGraphic.getCardWidth() + 5;
 		int width = cardWidth * Math.min(7, game.getPlayerHand().size());
 		if(x > startX && x <= startX + width &&
 				y > startY && y <= startY + height){
 			return (x - startX) / cardWidth;
 		}
 		return -1;
 	}
 	
 	private void showChooseSuitDialog(){
 		final Dialog chooseSuitDialog = new Dialog(context);
 		chooseSuitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		chooseSuitDialog.setContentView(R.layout.choose_suit_dialog);
 		final Spinner suitSpinner = (Spinner) chooseSuitDialog.findViewById(R.id.suitSpinner);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.suits, 
 				android.R.layout.simple_spinner_dropdown_item);
 		suitSpinner.setAdapter(adapter);
 		Button okButton = (Button) chooseSuitDialog.findViewById(R.id.okButton);
 		okButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				CharSequence selectedSuitName = (CharSequence)suitSpinner.getSelectedItem();
 				CardRef.Suit selectedSuit = null;
 				try{
 					selectedSuit = CardRef.Suit.valueOf(CardRef.Suit.class, selectedSuitName.toString().toUpperCase());
 				}catch(IllegalArgumentException e){ }
 				if(selectedSuit != null){
 					showingChooseSuit = false;
 					chooseSuitDialog.dismiss();
 					Toast.makeText(context, "You Chose: " + selectedSuit.toString(), Toast.LENGTH_SHORT).show();
 					game.setWildSuit(selectedSuit);
 					//Display the computer's turn
 					showComputerTurn();
 				}else{
 					Toast.makeText(context, "Error selecting suit... " + selectedSuitName.toString(), Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		showingChooseSuit = true;
 		chooseSuitDialog.show();
 	}
 	
 	public boolean isNearCenter(int x, int y){
 		int dropRadius = (int) (100 * scale);
 		int screenHalfW = screenW / 2;
 		int screenHalfH = screenH / 2;
 		return(x > screenHalfW - dropRadius &&
 				x < screenHalfW + dropRadius &&
 				y > screenHalfH - dropRadius &&
 				y < screenHalfH + dropRadius);
 	}
 	
 	private void endHand(){
 		endHandDialog = new Dialog(context);
 		endHandDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		endHandDialog.setContentView(R.layout.end_hand_dialog);
 		TextView endHandText = (TextView) endHandDialog.findViewById(R.id.endHandText);
 		int pointsThisHand = game.updateScores();
 		if(game.getPlayerHand().isEmpty()){
 			endHandText.setText("You went out and were awarded: " + pointsThisHand + " points!");
 		}else{
 			endHandText.setText("The computer won and was awarded: " + pointsThisHand + " points!");
 		}
 		Button nextHandButton = (Button) endHandDialog.findViewById(R.id.nextHandButton);
 		nextHandButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				game.startGame();
 				endHandDialog.dismiss();
 				endHandDialog = null;
 				postInvalidate();
 			}
 		});
 		endHandDialog.show();
 	}
 	
 	/**
 	 * Run the computer's turn, flag a redraw, and
 	 * show endHand if needed
 	 */
 	private Runnable doComputerTurn = new Runnable() {
 		@Override
 		public void run() {
 			game.runComputerTurn();
 			invalidate();
 			if(game.isGameOver() && endHandDialog == null){
 				endHand();
 			}
 		}
 	};
 	
 	/**
 	 * Displays an animation of the computer's turn, providing a callback to
 	 *   doComputerTurn
 	 */
 	private void showComputerTurn(){
 		CardRef computerCard = game.getComputerPlay();
 		if(computerCard != null){
 			//Show computer playing card
 			cardGraphic.animateCardBack(
 					new Point(getOpponentCardDrawX(), getOpponentCardDrawY()),
 					new Point(screenW / 2, screenH / 2),
 					200, doComputerTurn);
 		}else{
 			//Show computer drawing card
 			cardGraphic.animateCardBack(
 					new Point(screenW / 2, screenH / 2),
 					new Point(getOpponentCardDrawX(), getOpponentCardDrawY()),
 					200, doComputerTurn);
 		}
 		invalidate();
 	}
 	
 	private void animateMovingCardBack(int x, int y){
 		CardRef card = game.getPlayerHand().get(movingCardIdx);
 		animateMovingCardBack(card, x, y);
 	}
 	private void animateMovingCardBack(CardRef card, int x, int y){
 		//Move the moving card graphic off screen
 		movingCardX = -1000;
 		Point to = new Point(getPlayerCardDrawX(movingCardIdx),
 				getPlayerCardDrawY());
 		cardGraphic.animateCard(card, new Point(x, y), to, 100,
 				new Runnable() {
 			@Override
 			public void run() {
 				movingCardIdx = -1;
 				invalidate();
 			}
 		});
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent evt){
 		int eventAction = evt.getAction();
 		int x = (int)evt.getX();
 		int y = (int)evt.getY();
 		
 		//Make sure player isn't playing while animations are
 		if(!cardGraphic.isAnimationRunning()){
 			switch(eventAction){
 			case MotionEvent.ACTION_DOWN:
 				nextCardButton.checkPressed(x, y);
 				if(game.isPlayerTurn()){
 					movingCardIdx = playerCardAtLocation(x, y);
 					movingCardX = x;
 					movingCardY = y;
 					if(isNearCenter(x, y)){
 						if(game.hasValidMove(game.getPlayerHand())){
 							Toast.makeText(context, "You cannot draw if you have a valid move", Toast.LENGTH_SHORT).show();
 						}else{
 							game.drawCard(game.getPlayerHand());
 							game.changeTurn();
 						}
 					}
 				}
 				break;
 			case MotionEvent.ACTION_MOVE:
 				movingCardX = x;
 				movingCardY = y;
 				nextCardButton.checkPressed(x, y);
 				break;
 			case MotionEvent.ACTION_UP:
 				if(game.isPlayerTurn()){
					if(isNearCenter(x, y) && movingCardIdx != -1){
						CardRef card = game.getPlayerHand().get(movingCardIdx);
						if(game.playCard(game.getPlayerHand(), card)){
							//move successful
							if(card.rank == Rank.EIGHT && !game.isGameOver()){
								showChooseSuitDialog();
 							}
 						}else{
							animateMovingCardBack(card, x, y);
 						}
					}else{
						animateMovingCardBack(x, y);
 					}
 				}
 				nextCardButton.checkReleased(x, y);
 				if(!cardGraphic.isAnimationRunning()){
 					movingCardIdx = -1;
 				}
 				break;
 			}
 		}
 		if(game.isGameOver() && endHandDialog == null){
 			endHand();
 		}
 		//Show the computer's turn if we're not choosing a suit
 		if(!game.isGameOver() &&
 			!game.isPlayerTurn() &&
 			!showingChooseSuit){
 			showComputerTurn();
 		}
 		
 		invalidate();
 		return true;
 	}
 }
