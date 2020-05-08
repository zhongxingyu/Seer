 package org.easysms.android.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easysms.android.R;
 import org.easysms.android.util.TextToSpeechManager;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class KaraokeLayout extends ViewGroup {
 
 	public static class LayoutParams extends ViewGroup.LayoutParams {
 
 		public final int horizontal_spacing;
 		public final int vertical_spacing;
 
 		/**
 		 * @param horizontal_spacing
 		 *            Pixels between items, horizontally
 		 * @param vertical_spacing
 		 *            Pixels between items, vertically
 		 */
 		public LayoutParams(int horizontal_spacing, int vertical_spacing) {
 			super(0, 0);
 			this.horizontal_spacing = horizontal_spacing;
 			this.vertical_spacing = vertical_spacing;
 		}
 	}
 
 	private Handler handler;
 	private List<Button> mButtonList;
 	private int mLineHeight;
 	private ImageView mPlayButton;
 	/** Text that encloses the bubble. */
 	private String mText;
 	private int timesKaraoke = 0;
 
 	public KaraokeLayout(Context context) {
 		super(context);
 
 		// default text
 		mText = "";
 
 		// handler used in the karaoke.
 		handler = new Handler();
 
 		// initializes the button list.
 		mButtonList = new ArrayList<Button>();
 
 		// adds the play button.
 		addPlayButton();
 
 	}
 
 	public KaraokeLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		// default text
 		mText = "";
 
 		// handler used in the karaoke.
 		handler = new Handler();
 
 		// initializes the button list.
 		mButtonList = new ArrayList<Button>();
 
 		// adds the play button.
 		addPlayButton();
 
 		readStyleParameters(context, attrs);
 	}
 
 	private void addPlayButton() {
 
 		// creates the button used to play the text
 		mPlayButton = new ImageView(this.getContext());
 		mPlayButton.setBackgroundResource(R.drawable.playsmsclick);
 		mPlayButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				playKaraoke();
 			}
 		});
 
 		// adds the button.
 		addView(mPlayButton);
 	}
 
 	private void addTextButtons() {
 
 		// cleans the current layout.
 		for (int x = 0; x < mButtonList.size(); x++) {
 			// removes all the buttons available.
 			removeView(mButtonList.get(x));
 		}
 
 		// cleans the list.
 		mButtonList.clear();
 
 		// no valid text
		if (mText == null || mText.trim().equals(""))
 			return;
 
 		// parse the sentence into words and put it into an array of words
 		String[] words = mText.split("\\s+");
 
 		// create a button for each words and append it to the
 		// bubble composition
 		for (int i = 0; i < words.length; i++) {
 
 			final Button btn = new Button(getContext());
 			btn.setText(words[i]);
 			btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 					LayoutParams.WRAP_CONTENT));
 			btn.setTextColor(getResources().getColor(android.R.color.white));
 
 			// before being clicked the button is grey
 			btn.setBackgroundResource(R.drawable.button);
 
 			final String toSay = words[i];
 
 			// play each button
 			btn.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					// plays the audio.
 					TextToSpeechManager.getInstance().say(toSay);
 				}
 			});
 
 			// on long click, delete the button
 			btn.setOnLongClickListener(new OnLongClickListener() {
 				@Override
 				public boolean onLongClick(View v) {
 					KaraokeLayout.this.removeView(btn);
 					return true;
 				}
 			});
 
 			// adds the button to the container.
 			addView(btn);
 
 			// adds the new button to the list.
 			mButtonList.add(btn);
 		}
 	}
 
 	@Override
 	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
 		if (p instanceof LayoutParams) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
 		return new LayoutParams(1, 1); // default of 1px spacing
 	}
 
 	public String getText() {
 		return mText;
 	}
 
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 
 		final int count = getChildCount();
 		final int width = r - l;
 		int xpos = getPaddingLeft();
 		int ypos = getPaddingTop();
 
 		for (int i = 0; i < count; i++) {
 			final View child = getChildAt(i);
 			if (child.getVisibility() != GONE) {
 				final int childw = child.getMeasuredWidth();
 				final int childh = child.getMeasuredHeight();
 				final LayoutParams lp = (LayoutParams) child.getLayoutParams();
 				if (xpos + childw > width) {
 					xpos = getPaddingLeft();
 					ypos += mLineHeight;
 				}
 				child.layout(xpos, ypos, xpos + childw, ypos + childh);
 				xpos += childw + lp.horizontal_spacing;
 			}
 		}
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);
 
 		final int width = MeasureSpec.getSize(widthMeasureSpec)
 				- getPaddingLeft() - getPaddingRight();
 		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()
 				- getPaddingBottom();
 		final int count = getChildCount();
 		int line_height = 0;
 
 		int xpos = getPaddingLeft();
 		int ypos = getPaddingTop();
 
 		int childHeightMeasureSpec;
 		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
 			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
 					MeasureSpec.AT_MOST);
 		} else {
 			childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
 					MeasureSpec.UNSPECIFIED);
 		}
 
 		for (int i = 0; i < count; i++) {
 			final View child = getChildAt(i);
 			if (child.getVisibility() != GONE) {
 				final LayoutParams lp = (LayoutParams) child.getLayoutParams();
 				child.measure(
 						MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
 						childHeightMeasureSpec);
 				final int childw = child.getMeasuredWidth();
 				line_height = Math.max(line_height, child.getMeasuredHeight()
 						+ lp.vertical_spacing);
 
 				if (xpos + childw > width) {
 					xpos = getPaddingLeft();
 					ypos += line_height;
 				}
 
 				xpos += childw + lp.horizontal_spacing;
 			}
 		}
 		this.mLineHeight = line_height;
 
 		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
 			height = ypos + line_height;
 
 		} else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
 			if (ypos + line_height < height) {
 				height = ypos + line_height;
 			}
 		}
 		setMeasuredDimension(width, height);
 	}
 
 	public void playKaraoke() {
 		timesKaraoke++;
 
 		if (timesKaraoke <= 1) {
 
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 
 					for (int i = 1; i < getChildCount(); ++i) {
 						if (!(getChildAt(i) instanceof Button))
 							continue;
 						final Button btn = (Button) getChildAt(i);
 						btn.setFocusableInTouchMode(true);
 						try {
 							Thread.sleep(1000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						handler.post(new Runnable() {
 							@Override
 							public void run() {
 
 								btn.requestFocus();
 
 								// plays the audio.
 								TextToSpeechManager.getInstance().say(
 										(String) btn.getText());
 							}
 						});
 					}
 					timesKaraoke = 0;
 				}
 			};
 			new Thread(runnable).start();
 		}
 	}
 
 	private void readStyleParameters(Context context, AttributeSet attributeSet) {
 		TypedArray a = context.obtainStyledAttributes(attributeSet,
 				R.styleable.KaraokeLayout);
 		try {
 			String tmpStr = a.getString(R.styleable.KaraokeLayout_text);
 
 			if (tmpStr != null) {
 				setText(tmpStr);
 			}
 		} finally {
 			a.recycle();
 		}
 	}
 
 	public void setText(String text) {
 
		if (mText != text) {
			mText = text;
 
 			// adds the text buttons.
 			addTextButtons();
 		}
 	}
 }
