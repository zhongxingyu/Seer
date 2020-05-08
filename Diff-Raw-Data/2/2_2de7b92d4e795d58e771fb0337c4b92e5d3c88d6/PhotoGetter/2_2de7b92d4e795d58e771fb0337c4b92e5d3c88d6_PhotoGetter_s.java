 package org.bodytrack.client;
 
 import java.util.Comparator;
 
 import gwt.g2d.client.math.Vector2;
 
 import org.bodytrack.client.PhotoSeriesPlot.PhotoAlertable;
 import org.bodytrack.client.PlottablePoint.DateComparator;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.user.client.Element;
 
 /**
  * A class to download a single photo and return the appropriate
  * graphics both before and after the photo is downloaded.  This
  * class also handles resizing images as necessary.
  *
  * <p>A future implementation of this class will be able to handle
  * multiple photo sizes, and automatically decide, when asked for
  * a photo of a certain size, whether to download a new photo or
  * simply to scale the current photo.  To implement this, we will
  * need to change img from a single JavaScript Image object to a
  * list of Image objects.  For now, though, this just
  * handles single photo downloads.</p>
  *
  * <p>This class also maintains the logic for building photo
  * download URLs, given the appropriate information.</p>
  */
 
 // TODO: Add a PhotoManager class to ensure that no photo is ever downloaded
 // twice, with some logic for dropping photos from the cache after the cache
 // is too full (possibly use reference counting to make sure that the cache
 // doesn't drop photos that someone is using)
 
 public final class PhotoGetter extends JavaScriptObject implements Comparable<PhotoGetter> {
 	private static final int DUMMY_IMAGE_ID = -1;
 	private static final int DEFAULT_COUNT = 1;
 	private static final Comparator<Double> DATE_COMPARATOR = new DateComparator();
 
 	/* Overlay types always have protected zero-arg constructors. */
 	protected PhotoGetter() { }
 
 	public static PhotoGetter buildDummyPhotoGetter(final int userId,
 			final PhotoDescription desc) {
 		return buildPhotoGetter(userId, DUMMY_IMAGE_ID,
 				desc.getBeginDate(), DEFAULT_COUNT, null, false);
 	}
 
 	public static PhotoGetter buildPhotoGetter(final int userId,
 			final PhotoDescription desc,
 			final PhotoAlertable callback,
 			final boolean download) {
 		return buildPhotoGetter(userId, desc.getId(), desc.getBeginDate(),
				DEFAULT_COUNT, callback, download);
 	}
 
 	/**
 	 * Creates a new PhotoGetter
 	 *
 	 * @param userId
 	 * 	The ID of the user who owns the specified image
 	 * @param imageId
 	 * 	The ID of the specified image.  If this is negative, the PhotoGetter
 	 * 	is created normally except that no image will ever actually be loaded
 	 * 	from the server, meaning that callback is never called
 	 * @param time
 	 * 	The time at which this image should appear
 	 * @param count
 	 * 	The count field to store on the newly created object.  The count
 	 * 	field does not in any way affect how the photo is downloaded or drawn
 	 * @param callback
 	 * 	The object that will get a callback whenever the photo loads
 	 * 	or an error occurs.  If this is <code>null</code>, no exception
 	 * 	will occur, and callback will simply be ignored
 	 * @param download
 	 * 	A boolean that is <code>true</code> if and only if the image should
 	 * 	be downloaded immediately.  If this is <code>false</code>, the image
 	 * 	can still be downloaded later by calling {@link #download()}
 	 * @return
 	 * 	A new {@link PhotoGetter} that will download the specified image
 	 * 	iff imageId is nonnegative and download is <code>true</code>
 	 */
 	// TODO: Don't really want to pass in a PhotoAlertable, but I don't
 	// know how JSNI could handle it otherwise, because it wouldn't compile
 	// when I tried to use Alertable in JSNI
 	public native static PhotoGetter buildPhotoGetter(final int userId,
 			final int imageId,
 			final double time,
 			final int count,
 			final PhotoAlertable callback,
 			final boolean download) /*-{
 		// Declare this constant, and these functions, inside this
 		// function so we don't pollute the global namespace
 
 		var DEFAULT_WIDTH = 150;
 
 		var baseUrl = "/users/" + userId + "/logphotos/" + imageId + ".";
 		var url = baseUrl + DEFAULT_WIDTH + ".jpg";
 
 		var getter = {};
 		getter.userId = userId;
 		getter.imageId = imageId;
 		getter.time = time;
 		getter.count = count;
 		getter.callback = callback;
 		getter.loadStarted = download;
 		getter.imageLoaded = false;
 		getter.loadFailed = false;
 		getter.baseUrl = baseUrl;
 		getter.url = url;
 		getter.originalImgWidth = -1;
 		getter.originalImgHeight = -1;
 
 		getter.img = new Image();
 
 		if (imageId < 0) {
 			// No need to create extra closures that will never be called
 			return getter;
 		}
 
 		getter.img.onload = function() {
 			getter.imageLoaded = true;
 			getter.loadFailed = false;
 
 			if (getter.img.width && getter.img.width > 0)
 				getter.originalImgWidth = getter.img.width;
 
 			if (getter.img.height && getter.img.height > 0)
 				getter.originalImgHeight = getter.img.height;
 
 			if (!!getter.callback)
 				// In Java-like style:
 				// getter.callback.onSuccess(getter);
 				getter.callback.@org.bodytrack.client.PhotoSeriesPlot.PhotoAlertable::onSuccess(Lorg/bodytrack/client/PhotoGetter;)(getter);
 		}
 		getter.img.onerror = function() {
 			if (!getter.imageLoaded)
 				getter.loadFailed = true;
 
 			if (!!getter.callback)
 				// In Java-like style:
 				// getter.callback.onFailure(getter);
 				getter.callback.@org.bodytrack.client.PhotoSeriesPlot.PhotoAlertable::onFailure(Lorg/bodytrack/client/PhotoGetter;)(getter);
 		}
 
 		if (download) {
 			// Actually request that the browser load the image
 			getter.img.src = getter.url;
 		}
 
 		return getter;
 	}-*/;
 
 	/**
 	 * Returns the user ID used to initialize this {@link PhotoGetter}
 	 *
 	 * @return
 	 * 	The user ID passed to the factory method when this {@link PhotoGetter}
 	 * 	was created
 	 */
 	public native int getUserId() /*-{
 		return this.userId;
 	}-*/;
 
 	/**
 	 * Returns the image ID used to initialize this {@link PhotoGetter}
 	 *
 	 * @return
 	 * 	The image ID passed to the factory method when this {@link PhotoGetter}
 	 * 	was created
 	 */
 	public native int getImageId() /*-{
 		return this.imageId;
 	}-*/;
 
 	/**
 	 * Returns the time parameter used to initialize this {@link PhotoGetter}
 	 *
 	 * @return
 	 * 	The time parameter passed to the factory method when this
 	 * 	{@link PhotoGetter} was created
 	 */
 	public native double getTime() /*-{
 		return this.time;
 	}-*/;
 
 	/**
 	 * Returns the count parameter used to initialize this {@link PhotoGetter}
 	 *
 	 * @return
 	 * 	The count parameter passed to the factory method when this
 	 * 	{@link PhotoGetter} was created
 	 */
 	public native int getCount() /*-{
 		return this.count;
 	}-*/;
 
 	/**
 	 * Returns the URL built up from the userId and imageId parameters
 	 * to the factory method that created this {@link PhotoGetter}
 	 *
 	 * @return
 	 * 	The URL this {@link PhotoGetter} uses to request its image
 	 */
 	public native String getUrl() /*-{
 		return this.url;
 	}-*/;
 
 	/**
 	 * Returns <code>true</code> if and only if this {@link PhotoGetter} has
 	 * started loading an image
 	 *
 	 * @return
 	 * 	<code>true</code> if and only if this object has started loading an
 	 * 	image.  Says nothing about whether that load operation has completed
 	 */
 	public native boolean loadStarted() /*-{
 		return this.loadStarted;
 	}-*/;
 
 	/**
 	 * Returns <code>true</code> if the requested image has loaded,
 	 * <code>false</code> otherwise
 	 *
 	 * @return
 	 * 	<code>true</code> if and only if the requested image has loaded
 	 */
 	public native boolean imageLoaded() /*-{
 		return this.imageLoaded;
 	}-*/;
 
 	/**
 	 * Returns <code>true</code> if and only if the attempt to load the
 	 * image failed
 	 *
 	 * @return
 	 * 	<code>true</code> if the image load encountered an error, and
 	 * 	the image has failed to load.  If the image loads despite
 	 * 	an error, this will return <code>false</code>
 	 */
 	public native boolean loadFailed() /*-{
 		return this.loadFailed;
 	}-*/;
 
 	/**
 	 * Returns the width at which the image was sent over the wire, or a
 	 * negative value if the image hasn't loaded
 	 *
 	 * @return
 	 * 	The width of the image, if it has loaded (i.e. if
 	 * 	{@link #imageLoaded()} returns <code>true</code>
 	 */
 	public native double getOriginalWidth() /*-{
 		return this.originalImgWidth;
 	}-*/;
 
 	/**
 	 * Returns the height at which the image was sent over the wire, or a
 	 * negative value if the image hasn't loaded
 	 *
 	 * @return
 	 * 	The height of the image, if it has loaded (i.e. if
 	 * 	{@link #imageLoaded()} returns <code>true</code>
 	 */
 	public native double getOriginalHeight() /*-{
 		return this.originalImgHeight;
 	}-*/;
 
 	/**
 	 * Begins downloading the image
 	 *
 	 * @return
 	 * 	<code>true</code> if and only if calling this method actually
 	 * 	started a download.  If this method call came to a {@link PhotoGetter}
 	 * 	that had already begun or finished a download, or had a negative
 	 * 	image ID, returns <code>false</code>
 	 */
 	public native boolean download() /*-{
 		if (this.imageId >= 0 && !this.loadStarted) {
 			this.loadStarted = true;
 			this.img.src = this.url;
 			return true;
 		}
 
 		return false;
 	}-*/;
 
 	/**
 	 * A handy shortcut to the native drawImageBounded method
 	 *
 	 * <p>This gets the position, width, and height of bounds, and uses
 	 * that information to call the other
 	 * {@link #drawImageBounded(String, double, double, double, double,
 	 * double, double, double, double) drawImageBounded} with the correct
 	 * parameters.</p>
 	 *
 	 * @param canvas
 	 * 	The canvas we should use to draw the image
 	 * @param x
 	 * 	The X-position of the <em>center</em> of the image, in pixels
 	 * 	from the left edge of the canvas
 	 * @param y
 	 * 	The Y-position of the <em>center</em> of the image, in pixels
 	 * 	from the top edge of the canvas
 	 * @param width
 	 * 	The width of the image
 	 * @param height
 	 * 	The height of the image
 	 * @param bounds
 	 * 	A {@link BoundedDrawingBox} with the bounds that we should
 	 * 	use to constrain the image drawing
 	 * @return
 	 * 	<code>true</code> if and only if the image was successfully drawn,
 	 * 	meaning that {@link #imageLoaded() imageLoaded} returns
 	 * 	<code>true</code> and that canvas is a valid HTML canvas.  Note that
 	 * 	this does <em>not</em> return <tt>false</tt> if everything else
 	 * 	is fine but the image is outside the bounding box; a caller can
 	 * 	check for this using arithmetic, so we do not alert a caller to
 	 * 	that event
 	 */
 	public boolean drawImageBounded(Element canvas, double x, double y,
 			double width, double height, BoundedDrawingBox bounds) {
 		Vector2 topLeft = bounds.getTopLeft();
 
 		return drawImageBounded(canvas, x, y, width, height,
 				topLeft.getX(),
 				topLeft.getY(),
 				bounds.getWidth(),
 				bounds.getHeight());
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
 	 * @param canvas
 	 * 	The canvas we should use to draw the image
 	 * @param x
 	 * 	The X-position of the <em>center</em> of the image, in pixels
 	 * 	from the left edge of the canvas
 	 * @param y
 	 * 	The Y-position of the <em>center</em> of the image, in pixels
 	 * 	from the top edge of the canvas
 	 * @param width
 	 * 	The width of the image
 	 * @param height
 	 * 	The height of the image
 	 * @param minX
 	 * 	The minimum X-value that is within the bounds
 	 * @param minY
 	 * 	The minimum Y-value that is within the bounds
 	 * @param boundsWidth
 	 * 	The width of the clipping region i.e. the width of the region
 	 * 	in which we will draw an image
 	 * @param boundsHeight
 	 * 	The height of the clipping region i.e. the height of the
 	 * 	region in which we will draw an image
 	 * @return
 	 * 	<code>true</code> if and only if the image was successfully drawn,
 	 * 	meaning that {@link #imageLoaded()} returns <code>true</code> and
 	 * 	that canvas is a valid HTML canvas.  Note that this does <em>not</em>
 	 * 	return <code>false</code> if everything else is fine but the image
 	 * 	is outside the bounding box; a caller can check for that using
 	 * 	arithmetic, so this method does not alert a caller to that event
 	 */
 	public native boolean drawImageBounded(Element canvas, double x, double y,
 			double width, double height, double minX, double minY,
 			double boundsWidth, double boundsHeight) /*-{
 		// Same as drawImage, except with clipping also enabled
 		if (!this.imageLoaded) return false;
 		if (!(canvas && canvas.getContext)) return false;
 
 		var ctx = canvas.getContext('2d');
 		if (!ctx) return false;
 
 		ctx.save();
 		ctx.beginPath();
 		ctx.rect(minX, minY, boundsWidth, boundsHeight);
 		ctx.closePath();
 		ctx.clip();
 
 		ctx.drawImage(this.img, x - width / 2, y - height / 2, width, height);
 
 		ctx.restore();
 
 		return true;
 	}-*/;
 
 	@Override
 	public int compareTo(PhotoGetter other) {
 		if (other == null)
 			return 1;
 
 		return DATE_COMPARATOR.compare(getTime(), other.getTime());
 	}
 }
