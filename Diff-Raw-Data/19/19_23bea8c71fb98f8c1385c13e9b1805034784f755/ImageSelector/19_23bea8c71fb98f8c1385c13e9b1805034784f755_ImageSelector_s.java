 package org.fiz;
 
 /**
  * An ImageSelector outputs HTML for one of several images, chosen based on a
  * data value and a mapping dataset.
  * <p>
  * ImageSelector supports the following properties:
 * map -	(required) A dataset of mappings. See below for format.
 * id -		(required) Name of column in dataset we are mapping to an image
  * <p>
  * The map dataset contains key - value pairs, where a key is one of the
  * possible strings we will use to select an image. A value can either be a
  * string in which case it represents the src tag for an image, or it can be a
  * Dataset which contains src and alt elements, describing the URL for the
  * image and an alt tag. Both src and alt values are expanded as templates.
  * Map recognizes one special key, which is "default". If that key exists, it
  * is used to find the default image if a particular key is not found.
  */
 public class ImageSelector implements Formatter {
 
 	/* Constructor properties */
     protected String id;
     protected Dataset map;
 
 	protected static String DEFAULT_KEY = "default";
 
 	/**
 	 * Construct an ImageSelector object with the given set of properties.
 	 * @param properties		A collection of values describing the
 	 * 							configuration of the ImageSelector; see above
 	 *							for the supported values.
 	 */
     public ImageSelector(Dataset properties) {
 		map = properties.getChild("map");
 		id = properties.get("id");
     }
 
 	/**
 	 * Construct an ImageSelector given values for the {@code id} and {@code map}
 	 * properties.
 	 * @param id				Value for the {@code id} property.
 	 * @param map				Value for the {@code map} property.
 	 */
 	public ImageSelector(String id, Dataset map) {
 		this.id = id;
 		this.map = map;
 	}
 
 	/**
 	 * Generates HTML for the the ImageSelector, using properties passed to the
 	 * constructor and data passed into this method
      * @param cr             Overall information about the client
      *                       request being serviced.
      * @param data           Values in this dataset are used to expand
 	 * 						 templates. We also use {@code id} passed to the
 	 * 						 constuctor to reference a column in this row which
 	 * 						 is used to select an image to display.
      * @param out            HTML for the ImageSelector is appended here.
 	 */
     public void render(ClientRequest cr, Dataset data, StringBuilder out) {
 
 		String src, alt = null;
 		String key = data.get(id);
 		Object value = map.lookup(key);
 
 		if (value == null) {
 			value = map.lookup(DEFAULT_KEY);
 
 			if (value == null) {
 				throw new Dataset.MissingValueError(key);
 			}
 		}
 
 		if (value instanceof String) {
 			src = (String) value;
 		} else if (value instanceof Dataset) {
 			Dataset imgData = (Dataset) value;
 			src = imgData.get("src");
 			alt = imgData.check("alt");
 		} else {
 			throw new InternalError("ImageSelector does not support class " +
 				value.getClass().getSimpleName() + ". It only supports " +
 				"string and dataset values. ");
 		}
 
 		if (alt == null) {
 			alt = key;
 		}
 
 		Template.expand("<img src=\"@1\" alt=\"@2\" />", out,
 			Template.expand(src, data, Template.SpecialChars.URL),
 			Template.expand(alt, data, Template.SpecialChars.NONE));
     }
 }
