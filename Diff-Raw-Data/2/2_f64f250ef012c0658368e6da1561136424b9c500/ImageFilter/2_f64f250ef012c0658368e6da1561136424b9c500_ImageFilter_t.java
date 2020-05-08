 package org.gvlabs.image.utils.filter;
 
 import java.awt.image.BufferedImage;
 
 /**
  * Common interface to filters
  * 
  * @author Thiago Galbiatti Vespa - <a
  *         href="mailto:thiago@thiagovespa.com.br">thiago@thiagovespa.com.br</a>
  * @version 1.1
  *
  */
 public interface ImageFilter {
 	/**
 	 * Apply image filter
 	 * @param src original image
 	 * @return transformed image
 	 */
	BufferedImage applyTo(BufferedImage src);
 }
