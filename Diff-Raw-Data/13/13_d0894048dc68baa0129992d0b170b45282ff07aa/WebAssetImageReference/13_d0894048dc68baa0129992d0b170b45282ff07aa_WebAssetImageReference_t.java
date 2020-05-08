 package com.untzuntz.e2components.app;
 
 import nextapp.echo2.app.ApplicationInstance;
 import nextapp.echo2.app.Extent;
 import nextapp.echo2.app.HttpImageReference;
 
 public class WebAssetImageReference extends HttpImageReference {
 
 	private static final long serialVersionUID = 13939932222L;
 	
 	private String uri;
 	private Extent width, height;
 	private String id;
 
 	/**
 	 * Creates a reference to an image at the specified URI of unknown size.
 	 * 
 	 * @param uri
 	 *            a URI from which the image data may be obtained
 	 */
 	public WebAssetImageReference(String uri) {
 		this(uri, null, null);
 	}
 
 	/**
 	 * Creates a reference to an image at the specified URI of the given width
 	 * and height. If the image is not of the given width and height, it will be
 	 * scaled to the given width and height.
 	 * 
 	 * @param uri
 	 *            a URI from which the image data may be obtained
 	 * @param width
 	 *            The width at which to render the image
 	 * @param height
 	 *            The height at which to render the image
 	 */
 	public WebAssetImageReference(String uri, Extent width, Extent height) {
 		super(uri);
		
		this.uri = uri;
		this.width = width;
		this.height = height;

		if (uri == null)
			return;
 
 		StringBuffer fullNewLocation = new StringBuffer();
 		if (!uri.startsWith("/EvenFlow/"))
 		{
 			fullNewLocation.append( System.getProperty("WebAsset.Base") );
 			fullNewLocation.append( System.getProperty("WebAsset.Environment") );
 			fullNewLocation.append("/web-assets");
 			fullNewLocation.append(uri);
 			
 			System.out.println("Resolved Location of Image => " + fullNewLocation.toString());
 		}
 		else
 			fullNewLocation.append(uri);
 			
 		this.uri = fullNewLocation.toString();
 		id = ApplicationInstance.generateSystemId();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object o) {
 		if (!(o instanceof WebAssetImageReference)) {
 			return false;
 		}
 		WebAssetImageReference that = (WebAssetImageReference) o;
 		if (!(this.uri == that.uri || (this.uri != null && this.uri
 				.equals(that.uri)))) {
 			return false;
 		}
 		if (!(this.width == that.width || (this.width != null && this.width
 				.equals(that.width)))) {
 			return false;
 		}
 		if (!(this.height == that.height || (this.height != null && this.height
 				.equals(that.height)))) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @see nextapp.echo2.app.ImageReference#getHeight()
 	 */
 	public Extent getHeight() {
 		return height;
 	}
 
 	/**
 	 * @see nextapp.echo2.app.RenderIdSupport#getRenderId()
 	 */
 	public String getRenderId() {
 		return id;
 	}
 
 	/**
 	 * Returns the URI from which the image may be obtained.
 	 * 
 	 * @return the URI of the image
 	 */
 	public String getUri() {
		return uri; 
 	}
 
 	/**
 	 * @see nextapp.echo2.app.ImageReference#getWidth()
 	 */
 	public Extent getWidth() {
 		return width;
 	}	
 
 }
