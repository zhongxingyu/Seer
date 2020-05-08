 package edu.nku.cs.csc440.team2.player;
 
 import android.graphics.Bitmap;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import edu.nku.cs.csc440.team2.provider.MediaProvider;
 
 /**
  * Player responsile for rendering and playing Image objects
  * 
  * @author John Muray
  * @version 1.0 4/24/11
  */
 public class ImagePlayer extends SingleInstancePlayer {
 	/**
 	 * The android view that will contain our image.
 	 */
 	private ImageView imImage;
 	/**
 	 * The bitmap object that will hold the actual image data
 	 */
 	private Bitmap bmImage;
 
 	/**
 	 * Constructor for the ImagePlayer ojbect.
 	 * 
 	 * @param resource
 	 *            URI to the image resource
 	 * @param start
 	 *            The time to start playing the image
 	 * @param duration
 	 *            The length of time to continue playing the image
 	 */
 	public ImagePlayer(String resource, double start, double duration) {
 		this.duration = duration;
 		this.resourceURL = resource;
 		this.start = start;
 	}
 
 	/**
 	 * Starts and/or continue playback on the Image
 	 */
 	public void play() {
 		if (!this.isPlaying) {
 			this.isPlaying = true;
 			this.render();
 		}
 		this.incrementPlaybackTime();
 	}
 
 	/**
 	 * Doesn't need to do anything
 	 */
 	public void pause() {
 		// it's an image... really... -_-
 	}
 
 	/**
 	 * Doesn't need to do anything
 	 */
 	public void seekForward() {
 		// it's an image... really... -_-
 	}
 
 	/**
 	 * Doesn't need to do anything
 	 */
 	public void seekBackward() {
 		// it's an image... really... -_-
 	}
 
 	/**
 	 * Display the image to teh screen. This involves drawing the View and
 	 * placing the image into the view as well as scaling the image
 	 * appropriately
 	 */
 	public void render() {
 		boolean res = this.layout.post(new Runnable() {
 			public void run() {
 				layout.addView(imImage);
 			}
 		});
 		Log.w("ImagePlyaer", "Result of render: " + ((Boolean) res).toString());
 		Log.w("ImagePlayer", this.layout.toString());
 		Log.w("ImagePlayer", "Duration: " + this.duration);
 		Log.w("ImagePlayer", "playback time: " + this.timePlayed);
 	}
 
 	/**
 	 * Remove the view from teh screen, but dont' destroy any of the resources
 	 */
 	public void unRender() {
 		Log.w("ImagePlayer", "unrendering");
 		this.layout.post(new Runnable() {
 			public void run() {
 				layout.removeView(imImage);
 			}
 		});
 	}
 
 	/**
 	 * Download the image and generate the view necessary to display the image.
 	 * However, don't display anything yet.
 	 */
 	public void prepare() {
 		this.bmImage = (new MediaProvider()).getImage(this.resourceURL);
 
 		this.layout.post(new Runnable() {
 			public void run() {
 				imImage = new ImageView(layout.getContext());
 				imImage.setScaleType(ImageView.ScaleType.FIT_XY);
 				imImage.setAdjustViewBounds(false);
 				imImage.setMaxHeight(layout.getHeight());
 				imImage.setMaxWidth(layout.getHeight());
 
 				imImage.setImageBitmap(bmImage);
 			}
 		});
 
 	}
 
 }
