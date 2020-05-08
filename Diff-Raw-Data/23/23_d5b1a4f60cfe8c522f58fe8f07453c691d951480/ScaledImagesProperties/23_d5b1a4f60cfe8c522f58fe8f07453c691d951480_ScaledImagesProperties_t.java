 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  ******************************************************************************/
 package org.sociotech.communitymashup.source.scaledimages.properties;
 
 import org.imgscalr.Scalr;
 
 /**
  * This class contains string constants for properties used by the
  * ScaledImagesSourceService service.
  * 
  * @author Peter Lachenmaier
  */
 public class ScaledImagesProperties {
 
 		/**
	 * Whether to enrich content images or not.
 	 */
 	public static final String ENRICH_CONTENT_IMAGE_PROPERTY = "enrichContent";
 	public static final String ENRICH_CONTENT_IMAGE_DEFAULT = "true";
 	
 	/**
	 * Whether to enrich person images or not.
 	 */
 	public static final String ENRICH_PERSON_IMAGE_PROPERTY = "enrichPerson";
 	public static final String ENRICH_PERSON_IMAGE_DEFAULT = "true";
 	
 	/**
 	 * Whether to enrich organization images or not
 	 */
 	public static final String ENRICH_ORGANISATION_IMAGE_PROPERTY = "enrichOrganisation";
 	public static final String ENRICH_ORGANISATION_IMAGE_DEFAULT = "true";
 	
 	/**
 	 * Specific meta tag that is needed for a information object to be processed.
 	 */
 	public static final String PROCESS_ONLY_WITH_METATAG_PROPERTY = "neededIOMetaTag";
 	
 	/**
 	 * Specific meta tag that is needed for a image to be processed.
 	 */
 	public static final String PROCESS_ONLY_IMAGES_WITH_METATAG_PROPERTY = "neededImageMetaTag";
 	
 	/**
	 * Switch to turn on writing of dimensions to original images.
 	 */
 	public static final String WRITE_DIMENSIONS_TO_ORIG_IMAGE_PROPERTY = "writeDimension";
 	public static final String WRITE_DIMENSIONS_TO_ORIG_IMAGE_DEFAULT = "false";
 	
 	/**
	 * Width to scale images to.
 	 */
 	public static final String SCALED_IMAGE_WIDTH_PROPERTY = "imageWidth";
 	public static final String SCALED_IMAGE_WIDTH_DEFAULT = "50";
 	
 	/**
	 * Height to scale images to.
 	 */
 	public static final String SCALED_IMAGE_HEIGHT_PROPERTY = "imageHeight";
 	public static final String SCALED_IMAGE_HEIGHT_DEFAULT = "50";
 
 	/**
 	 * Mode of scaling according to {@link Scalr.Mode}, AUTOMATIC, FIT_EXACT, FIT_TO_HEIGHT, FIT_TO_WIDTH
 	 */
 	public static final String SCALE_MODE_PROPERTY = "scaleMode";
 	public static final String SCALE_MODE_DEFAULT = "AUTOMATIC";
 	
 	/**
	 * Method of scaling according to {@link Scalr.Method}, AUTOMATIC, BALANCED, QUALITY, SPEED, ULTRA_QUALITY 
 	 */
 	public static final String SCALE_METHOD_PROPERTY = "scaleMethod";
 	public static final String SCALE_METHOD_DEFAULT = "AUTOMATIC";
 	
 	/**
	 * Switch to turn off creation of scaled images. E. g. to write only dimensions.
 	 */
 	public static final String CREATE_SCALED_IMAGE_PROPERTY = "createScaledImages";
 	public static final String CREATE_SCALED_IMAGE_DEFAULT = "true";
 	
 	/**
	 * Type of the resulting scaled image. Can be jpg, png, gif according javax.imageio.ImageIO .
 	 */
 	public static final String IMAGE_TYPE_PROPERTY = "imageType";
 	public static final String IMAGE_TYPE_DEFAULT = "png";
 	
 }
