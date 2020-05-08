 package com.mntnorv.wrdl_holo.views;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.Gravity;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.mntnorv.wrdl_holo.GridSequenceTouchListener;
 import com.mntnorv.wrdl_holo.R;
 import com.tomgibara.android.util.SquareGridLayout;
 
 public class TileGridView extends RelativeLayout {
 	/* FIELDS */
 	private TileView[] tiles;
 	private int columns;
 	private int rows;
 	private float width;
 	private float height;
 	private boolean touch;
 	
 	private GridIndicatorView indicators;
 	private ViewGroup tileViewGroup;
 	private GridSequenceTouchListener touchListener;
 	
 	private String[] letters;
 	private int tileColor;
 	private int tileHighlightColor;
 	private int tileTextColor;
 	private int indicatorColor;
 	private float indicatorHeight;
 	
 	private String currentWord;
 	
 	private OnWordChangeListener wordChangeListener;
 	private OnWordSelectedListener wordSelectedListener;
 	
 	/* CONSTRUCTORS */
 	public TileGridView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initGridView();
 
 		TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.TileGridView);
 		
 		int c = a.getInt(R.styleable.TileGridView_columns, 0);
 		if (c > 0) {
 			columns = c;
 		}
 		
 		int r = a.getInt(R.styleable.TileGridView_rows, 0);
 		if (r > 0) {
 			rows = r;
 		}
 		
 		tileColor = a.getColor(R.styleable.TileGridView_tileColor, 0xFFAAAAAA);
 		tileHighlightColor = a.getColor(R.styleable.TileGridView_tileHighlightColor, 0xFFAAAAAA);
 		tileTextColor = a.getColor(R.styleable.TileGridView_tileTextColor, 0xFF000000);
 		indicatorColor = a.getColor(R.styleable.TileGridView_indicatorColor, 0xBB63BAF9);
 		
 		int ih = a.getDimensionPixelSize(R.styleable.TileGridView_indicatorHeight, 0);
 		if (ih != 0) {
 			indicatorHeight = ih;
 		}
 		
 		touch = a.getBoolean(R.styleable.TileGridView_respondToTouch, false);
 		
 		if (isInEditMode()) {
 			
 			TextView label = new TextView(context);
 			label.setWidth((int) width);
 			label.setHeight((int) height);
 			label.setText("TileGridView");
 			label.setGravity(Gravity.CENTER);
 			this.addView(label);
 		} else {
 			createGridView(context);
 		}
 		
 		a.recycle();
 	}
 	
 	/* INIT */
 	private void initGridView() {
 		tileColor = 0xFFAAAAAA;
 		tileHighlightColor = 0xFFAAAAAA;
 		tileTextColor = 0xFF000000;
 		indicatorColor = 0xBB63BAF9;
 		indicatorHeight = 8 * getResources().getDisplayMetrics().density;
 		
 		width = 100;
 		height = 100;
 		columns = 1;
 		rows = 1;
 		
 		currentWord = "";
 		
 		wordChangeListener = null;
 		wordSelectedListener = null;
 	}
 	
 	/* CREATE */
 	private void createGridView(Context context) {
 		if (width > 0 && height > 0 && columns > 0 && rows > 0) {
 			FrameLayout frame = new FrameLayout(context);
 			//ViewGroup tileTable = generateTileGrid(context);
 			tileViewGroup = generateTileGrid(context);
 			
 			if (touch) {
 				indicators = new GridIndicatorView(context,
 						width/columns, height/rows, columns, rows,
 						indicatorHeight, indicatorColor);
 				addTouchListener();
 				frame.addView(indicators);
 			}
 			
 			frame.addView(tileViewGroup);
 			
 			this.addView(frame);
 		} else {
 			throw new IllegalStateException("GridView must be initialized with a width, height, number of columns and rows");
 		}
 	}
 	
 	/* ADD TOUCH LISTENER */
 	private void addTouchListener() {
 		touchListener = new GridSequenceTouchListener(width/columns, height/rows, columns, rows) {
 			@Override
 			protected void sequenceChanged(ArrayList<Integer> sequence, byte changeType, int elemChanged) {
 				if (changeType == GridSequenceTouchListener.ELEMENT_ADDED) {
					currentWord += letters[elemChanged];
 					
 					if (sequence.size() > 1) {
 						indicators.addIndicator(sequence.get(1)%columns, sequence.get(1)/rows, elemChanged%columns, elemChanged/rows);
 					}
 					
 					tiles[elemChanged].setHighlighted(true);
 				} else if (changeType == GridSequenceTouchListener.ELEMENT_REMOVED) {
 					currentWord = currentWord.substring(0, currentWord.length() - letters[elemChanged].length());
 					indicators.removeLastIndicator();
 					tiles[elemChanged].setHighlighted(false);
 				} else {
 					if (wordSelectedListener != null) {
 						wordSelectedListener.onWordSelected(currentWord);
 					}
 					
 					currentWord = "";
 					indicators.clearIndicators();
 					
 					for (TileView tile: tiles) {
 						tile.setHighlighted(false);
 					}
 				}
 				
 				if (wordChangeListener != null) {
 					wordChangeListener.onWordChange(currentWord);
 				}
 			}
         };
         
         this.setOnTouchListener(touchListener);
 	}
 	
 	/* GENERATE A TILE GRID */
 	private ViewGroup generateTileGrid(Context context) {
 		tiles = new TileView[rows*columns];
 		letters = new String[rows*columns];
 		SquareGridLayout layout = new SquareGridLayout(context);
 		layout.setSize(columns);
 		
 		for (int i = 0; i < rows; i++) {
         	for (int j = 0; j < columns; j++) {
         		TileView tile = new TileView(context);
         		tile.setText("A");
         		tile.setColor(tileColor);
         		tile.setHighlightColor(tileHighlightColor);
         		tile.setTextColor(tileTextColor);
         		tile.setTextSize((int) (36*getResources().getDisplayMetrics().density));
         		tile.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
         		layout.addView(tile);
         		
         		tiles[i*columns+j] = tile;
         		letters[i*columns+j] = "A";
         	}
         }
 		
 		return layout;
 	}
 	
 	/* SETTERS */
 	/**
 	 * @param letters - an array of strings, each element corresponding to a tile.
 	 * The letter of a tile in the {@code m}-th column and the {@code n}-th row is
 	 * element number {@code n*numberOfColumns + m}. Size MUST be equal or bigger
 	 * than {@code columns*rows}.
 	 */
 	public void setLetters(String[] letters) {
 		if (letters.length >= columns*rows) {
 			for (int i = 0; i < columns*rows; i++) {
 				tiles[i].setText(letters[i]);
 				this.letters[i] = letters[i];
 			}
 		} else {
 			throw new IllegalArgumentException("Size of letter array MUST be equal or bigger than columns*rows");
 		}
 	}
 	
 	public void setWidth(float width) {
 		this.width = width;
 		if (!isInEditMode() && tiles != null) {
 			if (touch) {
 				indicators.setWidth(width);
 				touchListener.setWidth(width);
 			}
 			
 			if (width/columns < height/rows) {
 				for (TileView tile: tiles) {
 					tile.setSize(width/columns);
 				}
 			}
 		}
 	}
 	
 	public void setHeight(float height) {
 		this.height = height;
 		
 		if (!isInEditMode() && tiles != null) {
 			if (touch) {
 				indicators.setHeight(height);
 				touchListener.setHeight(height);
 			}
 			
 			if (height/rows < width/columns) {
 				for (TileView tile: tiles) {
 					tile.setSize(height/rows);
 				}
 			}
 		}
 	}
 	
 	public void setTileBackground(int color) {
 		tileColor = color;
 		for (TileView tile: tiles) {
 			tile.setColor(color);
 		}
 	}
 	
 	public void setTileHighlighColor(int color) {
 		tileHighlightColor = color;
 		for (TileView tile: tiles) {
 			tile.setHighlightColor(color);
 		}
 	}
 	
 	public void setTileTextColor(int color) {
 		tileTextColor = color;
 		for (TileView tile: tiles) {
 			tile.setTextColor(color);
 		}
 	}
 	
 	public void setOnWordChangeListener(OnWordChangeListener listener) {
 		wordChangeListener = listener;
 	}
 	
 	public void setOnWordSelectedListener(OnWordSelectedListener listener) {
 		wordSelectedListener = listener;
 	}
 	
 	/* INTERFACES */
 	public interface OnWordChangeListener {
 		public abstract void onWordChange(String word);
 	}
 	
 	public interface OnWordSelectedListener {
 		public abstract void onWordSelected(String word);
 	}
 	
 	/* MEASURE */
 	@Override
 	public void onMeasure (int widthSpec, int heightSpec) {
 		super.onMeasure(widthSpec, heightSpec);
 		
 		this.setWidth(getMeasuredWidth());
 		this.setHeight(getMeasuredHeight());
 	}
 }
