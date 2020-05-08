 package org.bodytrack.client;
 
 import gwt.g2d.client.math.Vector2;
 
 import org.bodytrack.client.PhotoDataPlot.PhotoAlertable;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 /**
  * A class to download a single photo and return the appropriate
  * graphics both before and after the photo is downloaded.  This
  * class also handles resizing images as necessary.
  *
  * <p>A future implementation of this class will be able to handle
  * multiple photo sizes, and automatically decide, when asked for
  * a photo of a certain size, whether to download a new photo or
  * simply to scale the current photo.  To implement this, we will
  * need to change img from a single <tt>Image</tt> object to a list
  * of <tt>Image</tt> objects.  For now, though, this just
  * handles single photo downloads.</p>
  *
  * <p>This class also maintains the logic for building photo
  * download URLs, given the appropriate information.</p>
  */
 public final class PhotoGetter extends JavaScriptObject {
 	/**
 	 * At least for now, we always download images at size
 	 * DEFAULT_WIDTH and do not use other image sizes.
 	 */
 	public static final int DEFAULT_WIDTH = 300;
 
 	/* Overlay types always have protected zero-arg constructors. */
 	protected PhotoGetter() { }
 
 	/**
 	 * Creates a new PhotoGetter.
 	 *
 	 * @param userId
 	 * 		the ID of the user who owns the specified image
 	 * @param imageId
 	 * 		the ID of the specified image
 	 * @param time
 	 * 		the time at which this image should appear
 	 * @param callback
 	 * 		the object that will get a callback whenever the photo loads
 	 * 		or an error occurs.  If this is <tt>null</tt>, no exception
 	 * 		will occur, and callback will simply be ignored.
 	 * @return
 	 * 		a new PhotoGetter that will get the specified image
 	 */
 	// TODO: Don't really want to pass in a PhotoAlertable, but I don't
 	// know how JSNI could handle it otherwise, because it wouldn't compile
 	// when I tried to use Alertable in JSNI
 	public native static PhotoGetter buildPhotoGetter(int userId,
 			int imageId, double time, PhotoAlertable callback) /*-{
 		// Declare this constant, and these functions, inside this
 		// function so we don't pollute the global namespace
 
 		var DEFAULT_WIDTH = 300;
 
 		// TODO: Possibly inline these functions for speed, since
 		// the inlining is trivial here
 		var getBaseUrl = function() {
 			return "/users/" + userId + "/logphotos/" + imageId + ".";
 		}
 
 		var getUrl = function(baseUrl) {
 			return baseUrl + DEFAULT_WIDTH + ".jpg";
 		}
 
 		var getter = {};
 		getter.userId = userId;
 		getter.imageId = imageId;
 		getter.time = time;
 		getter.callback = callback;
 		getter.imageLoaded = false;
 		getter.loadFailed = false;
 		getter.baseUrl = getBaseUrl();
 		getter.url = getUrl(getter.baseUrl);
 		getter.originalImgWidth = -1;
 		getter.originalImgHeight = -1;
 
 		getter.img = new Image();
 		getter.img.onload = function() {
 			getter.imageLoaded = true;
 			getter.loadFailed = false;
 
 			if (getter.img.width && getter.img.width > 0)
 				getter.originalImgWidth = getter.img.width;
 
 			if (getter.img.height && getter.img.height > 0)
 				getter.originalImgHeight = getter.img.height;
 
 			if (getter.callback)
 				// In Java-like style:
 				// getter.callback.onSuccess(getter);
 				getter.callback.@org.bodytrack.client.PhotoDataPlot.PhotoAlertable::onSuccess(Lorg/bodytrack/client/PhotoGetter;)(getter);
 		}
 		getter.img.onerror = function() {
 			if (! getter.imageLoaded)
 				getter.loadFailed = true;
 
 			if (getter.callback)
 				// In Java-like style:
 				// getter.callback.onFailure(getter);
 				getter.callback.@org.bodytrack.client.PhotoDataPlot.PhotoAlertable::onFailure(Lorg/bodytrack/client/PhotoGetter;)(getter);
 		}
 
 		// Actually request that the browser load the image
 		getter.img.src = getter.url;
 
 		return getter;
 	}-*/;
 
 	/**
 	 * Returns the user ID used to initialize this <tt>PhotoGetter</tt>.
 	 *
 	 * @return
 	 * 		the user ID passed to the factory method when this
 	 * 		<tt>PhotoGetter</tt> was created
 	 */
 	public native int getUserId() /*-{
 		return this.userId;
 	}-*/;
 
 	/**
 	 * Returns the image ID used to initialize this <tt>PhotoGetter</tt>.
 	 *
 	 * @return
 	 * 		the image ID passed to the factory method when this
 	 * 		<tt>PhotoGetter</tt> was created
 	 */
 	public native int getImageId() /*-{
 		return this.imageId;
 	}-*/;
 
 	/**
 	 * Returns the time parameter used to initialize this <tt>PhotoGetter</tt>.
 	 *
 	 * @return
 	 * 		the time parameter passed to the factory method when this
 	 * 		<tt>PhotoGetter</tt> was created
 	 */
 	public native double getTime() /*-{
		return this.time;
 	}-*/;
 
 	/**
 	 * Returns the URL built up from the userId and imageId parameters
 	 * to {@link #buildPhotoGetter(int, int, PhotoAlertable)}.
 	 *
 	 * @return
 	 * 		the URL this <tt>PhotoGetter</tt> uses to request its image
 	 */
 	public native String getUrl() /*-{
 		return this.url;
 	}-*/;
 
 	/**
 	 * Returns <tt>true</tt> if the requested image has loaded,
 	 * <tt>false</tt> otherwise.
 	 *
 	 * @return
 	 * 		<tt>true</tt> if and only if the requested image
 	 * 		has loaded
 	 */
 	public native boolean imageLoaded() /*-{
 		return this.imageLoaded;
 	}-*/;
 
 	/**
 	 * Returns <tt>true</tt> if and only if the attempt to load the
 	 * image failed.
 	 *
 	 * @return
 	 * 		<tt>true</tt> if the image load encountered an error, and
 	 * 		the image has failed to load.  If the image loads despite
 	 * 		an error, this will return <tt>false</tt>
 	 */
 	public native boolean loadFailed() /*-{
 		return this.loadFailed;
 	}-*/;
 
 	/**
 	 * Returns the width at which the image was sent over the wire, or a
 	 * negative value if the image hasn't loaded.
 	 *
 	 * @return
 	 * 		the width of the image, if it has loaded (i.e. if
 	 * 		{@link #imageLoaded()} returns <tt>true</tt>
 	 */
 	public native double getOriginalWidth() /*-{
 		return this.originalImgWidth;
 	}-*/;
 
 	/**
 	 * Returns the height at which the image was sent over the wire, or a
 	 * negative value if the image hasn't loaded.
 	 *
 	 * @return
 	 * 		the height of the image, if it has loaded (i.e. if
 	 * 		{@link #imageLoaded()} returns <tt>true</tt>
 	 */
 	public native double getOriginalHeight() /*-{
 		return this.originalImgHeight;
 	}-*/;
 
 	/**
 	 * Draws the image with the specified <strong>center</strong> location
 	 * and dimensions.
 	 *
 	 * @param canvasId
 	 * 		the value of the ID attribute on the canvas we should use to
 	 * 		draw the image
 	 * @param x
 	 * 		the X-position of the <em>center</em> of the image, in pixels
 	 * 		from the left edge of the canvas
 	 * @param y
 	 * 		the Y-position of the <em>center</em> of the image, in pixels
 	 * 		from the top edge of the canvas
 	 * @param width
 	 * 		the width of the image
 	 * @param height
 	 * 		the height of the image
 	 * @return
 	 * 		<tt>true</tt> if and only if the image was successfully drawn,
 	 * 		meaning that {@link #imageLoaded()} is <tt>true</tt> and that
 	 * 		canvasId is the actual ID for a valid HTML canvas
 	 */
 	public native boolean drawImage(String canvasId, double x, double y,
 			double width, double height) /*-{
 		if (! this.imageLoaded) return false;
 
 		var canvas = $doc.getElementById(canvasId);
 		if (! canvas) return false;
 
 		var ctx = canvas.getContext('2d');
 		if (! ctx) return false;
 
 		ctx.drawImage(this.img, x - width / 2, y - height / 2, width, height);
 		return true;
 	}-*/;
 
 	/**
 	 * A handy shortcut to the native
 	 * {@link #drawImageClipped(String, double, double, double, double,
 	 * double, double, double, double) drawImageClipped} method.
 	 *
 	 * <p>This gets the position, width, and height of bounds, and uses
 	 * that information to call the native <tt>drawImageClipped</tt>
 	 * with the correct parameters.</p>
 	 *
 	 * @param canvasId
 	 * 		the value of the ID attribute on the canvas we should use to
 	 * 		draw the image
 	 * @param x
 	 * 		the X-position of the <em>center</em> of the image, in pixels
 	 * 		from the left edge of the canvas
 	 * @param y
 	 * 		the Y-position of the <em>center</em> of the image, in pixels
 	 * 		from the top edge of the canvas
 	 * @param width
 	 * 		the width of the image
 	 * @param height
 	 * 		the height of the image
 	 * @param bounds
 	 * 		a {@link org.bodytrack.client.BoundedDrawingBox
 	 * 		BoundedDrawingBox} with the same bounds that we should use to
 	 * 		constrain the image drawing
 	 * @return
 	 * 		<tt>true</tt> if and only if the image was successfully drawn,
 	 * 		meaning that {@link #imageLoaded() imageLoaded} returns
 	 * 		<tt>true</tt> and that canvasId is the actual ID for a valid
 	 * 		HTML canvas.  Note that this does <em>not</em> return
 	 * 		<tt>false</tt> if everything else is fine but the image is
 	 * 		outside the bounding box; a caller can check for that
 	 * 		using arithmetic, so we do not alert a caller to that event
 	 * @see #drawImageClipped(String, double, double, double, double,
 	 * double, double, double, double)
 	 */
 	public boolean drawImageClipped(String canvasId, double x, double y,
 			double width, double height, BoundedDrawingBox bounds) {
 		Vector2 topLeft = bounds.getTopLeft();
 		Vector2 bottomRight = bounds.getBottomRight();
 
 		return drawImageClipped(canvasId, x, y, width, height,
 			topLeft.getX(),
 			topLeft.getY(),
 			bottomRight.getX() - topLeft.getX(),
 			bottomRight.getY() - topLeft.getY());
 	}
 
 	/**
 	 * Draws the image with the specified <strong>center</strong> location
 	 * and dimensions, and only inside the specified box.
 	 *
 	 * <p>This is exactly like
 	 * {@link #drawImage(String, double, double, double, double) drawImage},
 	 * except that it also takes parameters for the bounds of the region
 	 * where we might draw the image.</p>
 	 *
 	 * @param canvasId
 	 * 		the value of the ID attribute on the canvas we should use to
 	 * 		draw the image
 	 * @param x
 	 * 		the X-position of the <em>center</em> of the image, in pixels
 	 * 		from the left edge of the canvas
 	 * @param y
 	 * 		the Y-position of the <em>center</em> of the image, in pixels
 	 * 		from the top edge of the canvas
 	 * @param width
 	 * 		the width of the image
 	 * @param height
 	 * 		the height of the image
 	 * @param minX
 	 * 		the minimum X-value that is within the bounds
 	 * @param minY
 	 * 		the minimum Y-value that is within the bounds
 	 * @param boundsWidth
 	 * 		the width of the clipping region i.e. the width of the region
 	 * 		in which we will draw an image
 	 * @param boundsHeight
 	 * 		the height of the clipping region i.e. the height of the
 	 * 		region in which we will draw an image
 	 * @return
 	 * 		<tt>true</tt> if and only if the image was successfully drawn,
 	 * 		meaning that {@link #imageLoaded() imageLoaded} returns
 	 * 		<tt>true</tt> and that canvasId is the actual ID for a valid
 	 * 		HTML canvas.  Note that this does <em>not</em> return
 	 * 		<tt>false</tt> if everything else is fine but the image is
 	 * 		outside the bounding box; a caller can check for that
 	 * 		using arithmetic, so we do not alert a caller to that event
 	 */
 	public native boolean drawImageClipped(String canvasId, double x, double y,
 			double width, double height, double minX, double minY,
 			double boundsWidth, double boundsHeight) /*-{
 		// Same as drawImage, except with clipping also enabled
 
 		if (! this.imageLoaded) return false;
 
 		var canvas = $doc.getElementById(canvasId);
 		if (! canvas) return false;
 
 		var ctx = canvas.getContext('2d');
 		if (! ctx) return false;
 
 		ctx.stroke();
 
 		// Begin of save/restore block
 		ctx.save();
 		ctx.beginPath();
 		ctx.rect(minX, minY, boundsWidth, boundsHeight);
 		ctx.clip();
 
 		ctx.drawImage(this.img, x - width / 2, y - height / 2, width, height);
 
 		ctx.restore();
 		// End of save/restore block
 
 		ctx.beginPath();
 
 		return true;
 	}-*/;
 }
