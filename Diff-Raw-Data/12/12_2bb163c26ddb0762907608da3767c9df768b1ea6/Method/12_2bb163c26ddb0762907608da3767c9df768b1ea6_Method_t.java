 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.method;
 
 import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
 import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
 import edu.stanford.cfuller.imageanalysistools.metric.Metric;
 import edu.stanford.cfuller.imageanalysistools.metric.Quantification;
 import edu.stanford.cfuller.imageanalysistools.filter.Filter;
 import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
 import edu.stanford.cfuller.imageanalysistools.frontend.StatusUpdater;
 
 /**
  * An analysis method that can be run in an independent thread of execution.
  *
  * @author Colin J. Fuller
  */
 
 
 public abstract class Method implements Runnable {
 	
 	
 	final static String defaultMethodPackageName = "edu.stanford.cfuller.imageanalysistools.method";
     
 	//fields
 	
 	protected ParameterDictionary parameters;
 	protected java.util.List<Image> storedImages;
 	protected java.util.List<Image> images;
     protected ImageSet imageSet;
 	protected Quantification storedDataOutput;
 	protected StatusUpdater updater;
 	
 	protected String displayName;
 	
 	//protected methods
 
 
     /**
      * Runs through a sequence of Filters and applies them successively to an input image; quantifies images according to the
      * supplied metric and the mask that results from application of the filters.
      * 
      * @param filters       The List of Filters to apply to the image, in order.
      * @param toProcess     The Image that will be processed by the Filters; may be overwritten during the filtering process.
      * @param m             The Metric that will be used to quantify the Images.
      */
 	protected void iterateOnFiltersAndStoreResult(java.util.List<Filter> filters, WritableImage toProcess, Metric m) {
 		
 		
 		int c = 0;
 				
 		if (Boolean.parseBoolean(parameters.getValueForKey("DEBUG"))) {
 			LoggingUtilities.getLogger().info("starting filters");
 		    //toProcess.writeToFile("/Users/cfuller/Desktop/filter_intermediates/" + Integer.toString(c++) + ".ome.tif");
 			String name = Integer.toString(c++);
 			
 			if (ij.IJ.getInstance() == null) {new ij.ImageJ();}
 			
 			ij.ImagePlus ip = (ImageFactory.create(toProcess)).toImagePlus();
 			ip.setTitle(name);
 			ip.show();
 		}
 		for (Filter f : filters) {
 			
 			f.apply(toProcess);
             if (Boolean.parseBoolean(parameters.getValueForKey("DEBUG"))) {
     			LoggingUtilities.getLogger().info("completed filter #" + c);
     			String name = Integer.toString(c);
     			if (this.updater == null) c++;
     			
     			ij.ImagePlus ip = (ImageFactory.create(toProcess)).toImagePlus();
     			ip.setTitle(name);
     			ip.show();
 			   //toProcess.writeToFile("/Users/cfuller/Desktop/filter_intermediates/" + Integer.toString(c++) + ".ome.tif");
             }
             
             if (this.updater != null) {
             	updater.update(++c, filters.size(), null);
             }
 		}
 		if (m != null) {
			this.storeDataOutput(m.quantify(toProcess, this.imageSet));
 		}
 				
 		this.storeImageOutput(ImageFactory.create(toProcess));
 		
 		
 		this.parameters.addIfNotSet("background_calculated", "false");
 		
 	}
 
     /**
      * Stores an Image to an internal list of Images that can later be written to disk.
      * @param im    The Image to add to the list of output Images.
      */
 	protected void storeImageOutput(Image im) {this.storedImages.add(im);}
 

	/**
	* Stores a Quantification to be used as the data for ouptut.
	* @param q The quantification object that will be used as output.
	*/
	protected void storeDataOutput(Quantification q) {
		this.storedDataOutput = q;
	}

     /**
      * Clears all Images that have been stored for output.
      */
     protected void clearImageOutput() {this.storedImages.clear();}
 	
 
     /**
      * Runs the analysis method.
      */
 	public abstract void go();
 
     /**
      * Default constructor; sets up storage for the Images to be processed/quantified as well as for the output Images.
      */
 	public Method() {
 		this.images = new java.util.ArrayList<Image>();
 		this.storedImages = new java.util.ArrayList<Image>();
 		this.updater = null;
 	}
 
     /**
      * Gets the first in the list of stored output Images.
      *
      * Will not remove the image from the list, so successive calls
      * can return the same Image.
      *
      * @return  The first Image in the list of stored output Images, or null if no Images have been stored.
      */
 	public Image getStoredImage() {return (this.storedImages.size() > 0) ? this.storedImages.get(0) : null;}
 
     /**
      * Gets the entire list of stored output Images.
      *
      * If no Images have been stored, this method will return an empty list.  Classes extending Method should retain this behavior
      * so that the users can iterate over the returned (potentially empty) list without having to check for null or
      * special values.
      *
      * @return  The entire list of stored output Images (may be empty).
      */
 	public java.util.List<Image> getStoredImages() {return this.storedImages;}
 
     /**
      * Sets the parameters for this method to those found in a supplied {@link ParameterDictionary}.
      *
      * This retains a reference to the ParameterDictionary and does not copy it, so external changes to the ParameterDictionary
      * will be reflected in the stored one as well.
      *
      * @param params    The ParameterDictionary containing the parameters for running this method.
      */
 	public void setParameters(ParameterDictionary params){this.parameters = params;}
 
 
     /**
      * Sets the Images to be quantified/processed for this method.
      *
      * If there is one Image that is to receive special treatment (for example, one color channel to be segmented, and this
      * used to quantify all the channels), then that Image should be specified in the ImageSet using its setMarkerImage method.
      *
      * This method will not overwrite any Images already passed using prior calls this method, but rather append the supplied
      * Images to the list of those already passed in.  However, if a marker image has already been passed in in a previous ImageSet, it
      * will not be changed by calling this method.
      *
      * @param images    The List of images to be processed/quantified.
      */
 	public void setImages(ImageSet images) {
 
         Integer marker = images.getMarkerIndex();
 
         if (marker != null) {
             this.images.add(images.getMarkerImage());
 
             for (int i = 0; i < images.size(); i++) {
                 if (i == marker) continue;
                 this.images.add(images.getImageForIndex(i));
 
             }
         } else {
 
             this.images.addAll(images);
         }
 
         this.imageSet = images;
 
     }
 
     /**
      * Gets the stored ParameterDictionary used for this method.
      *
      * This will get a reference, not a copy, so modifications will be reflected in the stored ParameterDictionary.
      *
      * @return      A reference to the ParameterDictionary used for this method.
      */
 	public ParameterDictionary getParameters() {return this.parameters;}
 
 
     /**
      * Gets the stored quantification data generated by this method.
      * @return  A Quantification containing the quantification data, in the format specified by {@link Metric}.
      */
     public Quantification getStoredDataOutput() {
         return storedDataOutput;
     }
     
     public void setStatusUpdater(StatusUpdater up) {
     	this.updater = up;
     }
 
     /**
      * Method called when the thread is started.
      */
     @Override
     public void run() {
         this.go();
     }
 
 	/**
 	* Gets the display name associated with the method.  This will be printed
 	* as part of the output filenames from the method.
 	* @return 	a String containing the display name
 	*/
 	public String getDisplayName() {
 		return this.displayName;
 	}
 	
 	/**
 	* Sets the display name associated with the method.  This will be printed
 	* as part of the output filenames from the method.
 	* @param name a String containing the display name.  This should not
 	* 			contain any characters that might cause problems in filenames.
 	*/
 	public void setDisplayName(String name) {
 		this.displayName = name;
 	}
 	
 	/**
      * Retrieves the method to run from its name.
      * 
      * @param methodName	The name of the method to run, either fully qualified or relative to the default method package.
      * @return				A Method object of the type specified in the parameter dictionary.
      */
     public static Method loadMethod(String methodName) {
 
         Method method = null;
        
         //methodName might be fully qualified, in which case we want to use 
         //that; otherwise, we should fall back on a default value
 
 
         if (! methodName.contains(".")) {
 
             methodName = defaultMethodPackageName + "." + methodName;
 
         }
 
         try {
             method = (Method) Class.forName(methodName).newInstance();
         } catch (ClassNotFoundException e) {
             LoggingUtilities.getLogger().severe("Could not find method: " + methodName);
             e.printStackTrace();
         } catch (InstantiationException e) {
             LoggingUtilities.getLogger().severe("Could not instantiate method: " + methodName);
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             LoggingUtilities.getLogger().severe("Could not access method constructor for: " + methodName);
             e.printStackTrace();
         }
 
         return method;
 
     }
 
 }
