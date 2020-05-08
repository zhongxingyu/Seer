 package com.chalmers.speedtype.model;
 
 //TODO Resuming a paused game always results in a new word.
 
 import java.util.LinkedList;
 
 import com.chalmers.speedtype.R;
 import com.chalmers.speedtype.util.Dictionary;
 
 import android.view.KeyEvent;
 
 public class FallingWordsModel extends GameModel {
 
 	private static final int LAYOUT_ID = R.layout.falling_words_layout;
 	private static final int VIEW_ID = R.id.falling_words_view;
 
 	private static final int WORD_FREQUENCY = 3000;
 	private static final int UPDATE_FREQUENCY = 30;
 
 	private long lastWordTimeMillis;
 	private long lastUpdateMillis;
 
 	private LinkedList<Word> visibleWords = new LinkedList<Word>();
 
 	public FallingWordsModel() {
 		super();
 	}
 
 	@Override
 	public int getLayoutId() {
 		return LAYOUT_ID;
 	}
 
 	@Override
 	public int getViewId() {
 		return VIEW_ID;
 	}
 
 	@Override
 	public void onInput(KeyEvent event) {
 		if (visibleWords.size() > 0) {
 			char inputChar = (char) event.getUnicodeChar();
 			if (visibleWords.getFirst().charAt(currentCharPos) == inputChar) {
 				onCorrectChar();
 				if (isWordComplete()) {
 					onCorrectWord();
 				} else {
 					incCurrentCharPos();
 				}
 			}
 			onIncorrectChar();
 		}
 	}
 
 	protected void onCorrectChar() {
 		super.onCorrectChar();
 		incScore(1);
 	}
 
 	protected void onCorrectWord() {
 		updateWord();
 		super.onCorrectWord();
 	}
 
 	protected void onIncorrectChar() {
 		super.onIncorrectChar();
 	}
 
 	@Override
 	public boolean isContinuous() {
 		return true;
 	}
 
 	@Override
 	protected void updateWord() {
 		visibleWords.removeFirst();
 		currentCharPos = 0;
 	}
 
 	@Override
 	public void update() {
 		if (System.currentTimeMillis() - lastWordTimeMillis > WORD_FREQUENCY) {
 			int wordSize = (int) (Math.random() * 50 + 20);
 			Word newWord = new Word(Dictionary.getNextWord(), wordSize,
 					(int) (Math.random() * 100), 0);
 			visibleWords.addLast(newWord);
 
 			lastWordTimeMillis = System.currentTimeMillis();
 			listener.propertyChange(null);
 		}
 
 		if (System.currentTimeMillis() - lastUpdateMillis > UPDATE_FREQUENCY) {
 			for (Word w : visibleWords) {
 				w.setY(w.getY() + 1);
 				if (w.getY() > displayHeight / 2)
 					isGameOver = true;
 			}
 
 			lastUpdateMillis = System.currentTimeMillis();
 			listener.propertyChange(null);
 		}
 	}
 
 	@Override
 	public Word getActiveWord() {
 		if (!visibleWords.isEmpty())
 			return visibleWords.getFirst();
 		return null;
 	}
 
 	@Override
 	protected boolean isWordComplete() {
 		return currentCharPos == visibleWords.getFirst().length() - 1;
 	}
 
 	public LinkedList<Word> getVisibleWords() {
 		return visibleWords;
 	}
 
 	@Override
 	public boolean isSensorDependent() {
 		return false;
 	}
 }
