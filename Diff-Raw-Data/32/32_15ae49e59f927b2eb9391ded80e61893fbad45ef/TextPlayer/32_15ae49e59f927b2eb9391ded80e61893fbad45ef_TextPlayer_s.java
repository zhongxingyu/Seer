 package edu.nku.cs.csc440.team2.player;
 
 import android.widget.TextView;
 import edu.nku.cs.csc440.team2.provider.MediaProvider;
 
 /**
  * Player responsible for rendering text to the screen.
  * 
  * @author John Murray
  * @version 1.0 4/24/11
  * 
  */
 public class TextPlayer extends SingleInstancePlayer {
 	/**
 	 * The text that will be displayed.
 	 */
 	private String text;
 	/**
 	 * The view that will hold the text
 	 */
 	private TextView txtView;
 
 	/**
 	 * Initialize the text player object
 	 * 
 	 * @param resource
 	 *            The URI to the text file taht will be used
 	 * @param start
 	 *            The time the text should be rendered in the SMIL document
 	 * @param duration
 	 *            The time is should display until being unRendered from the
 	 *            SMIL document.
 	 */
 	public TextPlayer(String resource, double start, double duration) {
 		this.resourceURL = resource;
 		this.start = start;
 		this.duration = duration;
 	}
 
 	/**
 	 * Display the text to the screen (render) if it has not been already,
 	 * otherwise you dont' need to do anything... it's text.
 	 */
 	public void play() {
 		if (!this.isPlaying) {
 			this.isPlaying = true;
 			this.render();
 		}
 		this.incrementPlaybackTime();
 	}
 
 	/**
 	 * Not used.
 	 */
 	public void pause() {
 		// do absolutely nothing!
 	}
 
 	/**
 	 * Not implemented
 	 */
 	public void seekForward() {
 		// not implemented yet
 	}
 
 	/**
 	 * Not implemented
 	 */
 	public void seekBackward() {
 		// not implemented yet
 	}
 
 	/**
 	 * Display the text to the screen in a textView object
 	 */
 	public void render() {
 		this.layout.post(new Runnable() {
 			public void run() {
 				txtView = new TextView(layout.getContext());
 				txtView.setText(text);
 				layout.addView(txtView);
 			}
 		});
 	}
 
 	/**
 	 * Remove the text from the screen.
 	 */
 	public void unRender() {
 		this.layout.post(new Runnable() {
 			public void run() {
 				layout.removeView(txtView);
 			}
 		});
 	}
 
 	/**
 	 * Download the text file... pretty simple really.
 	 */
 	public void prepare() {
 		this.text = (new MediaProvider()).getText(this.resourceURL);
 	}
 
 }
