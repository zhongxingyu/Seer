 /*
  * Copyright (C) 2012 Joakim Persson, Daniel Augurell, Adrian Bjugard, Andreas Rolen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.chalmers.dat255.group09.Alarmed.modules.memoryModule.activity;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import edu.chalmers.dat255.group09.Alarmed.R;
 import edu.chalmers.dat255.group09.Alarmed.activity.BaseActivationActivity;
 import edu.chalmers.dat255.group09.Alarmed.modules.memoryModule.adapter.MemoryAdapter;
 import edu.chalmers.dat255.group09.Alarmed.modules.memoryModule.controller.MemoryController;
 import edu.chalmers.dat255.group09.Alarmed.modules.memoryModule.util.GameboardGenerator;
 import edu.chalmers.dat255.group09.Alarmed.modules.memoryModule.views.CardImage;
 
 /**
  * 
  * @author Joakim Persson
  * 
  */
 public class MemoryActivity extends BaseActivationActivity implements
 		OnItemClickListener {
 
	private static final int NBR_OF_PAIRS = 8;
	private static final int GRID_COLUMNS = 4;
	private static final int DELAY = 500;
 	private boolean isFirstCard = true;
 	private Timer timer;
 	private CardImage firstCard = null;
 	private MemoryController controller;
 	private GameboardGenerator gameboardGenerator;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_memory);
 		gameboardGenerator = new GameboardGenerator(this);
 		controller = new MemoryController(NBR_OF_PAIRS);
 
 		initGridView();
 
 		timer = new Timer();
 	}
 
 	/**
 	 * Init the gridview, by generating the card images and then via an adapter
 	 * pushing them to the view. Also configured the number of columns and
 	 * attached an otItemClickListener.
 	 */
 	private void initGridView() {
 		GridView gridView = (GridView) findViewById(R.id.activity_memory_grid_view);
		List<CardImage> images = gameboardGenerator.getGameBoard(NBR_OF_PAIRS,
				false);
 		MemoryAdapter memoryAdapter = new MemoryAdapter(images);
 		gridView.setAdapter(memoryAdapter);
 		gridView.setNumColumns(GRID_COLUMNS);
 		gridView.setOnItemClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_memory, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View view, int position,
 			long id) {
 		CardImage cardImage = (CardImage) view;
 		if (isValidPress(cardImage)) {
 			handleButtonPressed(cardImage);
 		}
 	}
 
 	/**
 	 * Determines of the pressed card image was an valid press, by checking if
 	 * the card had been pressed before or it it was disabled.
 	 * 
 	 * @param cardImage
 	 *            The pressed card image.
 	 * @return If it was an valid press.
 	 */
 	private boolean isValidPress(CardImage cardImage) {
 		return !cardImage.isDisabled() && !cardImage.equals(firstCard);
 	}
 
 	/**
 	 * Handle the logic for when an card image was pressed in the view.
 	 * 
 	 * @param cardImage
 	 *            The pressed card image.
 	 */
 	private void handleButtonPressed(CardImage cardImage) {
 
 		if (isFirstCard) {
 			handleFirstCardPressed(cardImage);
 		} else {
 			handleSecondCardPressed(cardImage);
 		}
 
 	}
 
 	/**
 	 * Handle the logic for the first pressed card image.
 	 * 
 	 * @param cardImage
 	 *            The first pressed card image.
 	 */
 	private void handleFirstCardPressed(CardImage cardImage) {
 		cardImage.cardPressed();
 		firstCard = cardImage;
 		isFirstCard = false;
 	}
 
 	/**
 	 * Handle the logic for the second pressed card image.
 	 * 
 	 * @param cardImage
 	 *            The second pressed card image.
 	 */
 	private void handleSecondCardPressed(CardImage cardImage) {
 		CardImage secondCard = cardImage;
 		secondCard.cardPressed();
 
 		timer.schedule(new MemoryTask(firstCard, secondCard), DELAY);
 
 		isFirstCard = true;
 		firstCard = null;
 		secondCard = null;
 	}
 
 	/**
 	 * A timertask for handling the logic when two card images has been pressed
 	 * in a row. The timertask is responsible for updating the game by checking
 	 * if the cards where equal and update the card images status.
 	 * 
 	 * @author Joakim Persson
 	 * 
 	 */
 	private class MemoryTask extends TimerTask {
 
 		private final CardImage cardOne;
 		private final CardImage cardTwo;
 		private Handler handler = new Handler();
 
		/**
		 * Create a new instance the memory task.
		 * 
		 * @param firstCard
		 *            The first card that was pressed.
		 * @param secondCard
		 *            The second card that was pressed.
		 */
 		public MemoryTask(CardImage firstCard, CardImage secondCard) {
 			this.cardOne = firstCard;
 			this.cardTwo = secondCard;
 		}
 
 		@Override
 		public void run() {
 			// Must use an handler to make changes to the ui thread
 			handler.post(new Runnable() {
 
 				@Override
 				public void run() {
 					if (controller.isCardsEqual(cardOne, cardTwo)) {
 						controller.correctGuess();
 						cardOne.setDisabled();
 						cardTwo.setDisabled();
 
 					} else {
 						cardOne.cardPressed();
 						cardTwo.cardPressed();
 					}
 
 					if (controller.isGameOver()) {
 						MemoryActivity.super.stopAlarm();
 					}
 
 				}
 			});
 		}
 	}
 }
