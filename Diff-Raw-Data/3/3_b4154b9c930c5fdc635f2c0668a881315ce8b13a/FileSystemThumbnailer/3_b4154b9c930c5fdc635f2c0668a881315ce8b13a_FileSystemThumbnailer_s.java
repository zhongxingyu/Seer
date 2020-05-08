 /**
  *  Main class for loading HTML content of Web page, Generate images and store it on local file system.
  */
 package com.sitegraph.core;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.sitegraph.core.attributes.ImageAttributes;
 import com.sitegraph.core.attributes.PNGImageAttributes;
 import com.sitegraph.core.util.Constants;
 import com.sitegraph.core.util.WebAppUtils;
 import com.trolltech.qt.core.QObject;
 import com.trolltech.qt.core.QSize;
 import com.trolltech.qt.core.Qt.AspectRatioMode;
 import com.trolltech.qt.core.Qt.Orientation;
 import com.trolltech.qt.core.Qt.ScrollBarPolicy;
 import com.trolltech.qt.core.Qt.TransformationMode;
 import com.trolltech.qt.gui.QApplication;
 import com.trolltech.qt.gui.QColor;
 import com.trolltech.qt.gui.QImage;
 import com.trolltech.qt.gui.QPainter;
 import com.trolltech.qt.network.QNetworkRequest;
 import com.trolltech.qt.webkit.QWebPage;
 
 public class FileSystemThumbnailer extends SiteGraphThumbnailer {
 
 	private static final Logger logger = Logger.getLogger(FileSystemThumbnailer.class);
 	
 	/*
 	 * Default constructor forcefully added for aop scoped auto proxy 
 	 */
 	public FileSystemThumbnailer(){
 		super();
 	}
 	/**
 	 * @param url URL of Web Page in String
 	 */
 	public FileSystemThumbnailer(String url){
 		super(null,url,Arrays.asList(new ImageAttributes[]{ new PNGImageAttributes()}));
 	}
 	
 	/**
 	 * @param url URL of Web Page in String
 	 * @param imageAttributes object of ImageAttribute Class to provide specific image related information
 	 */
 	public FileSystemThumbnailer(String url,ImageAttributes imageAttribute){
 		super(null,url,Arrays.asList(imageAttribute));
 	}
 	
 	/**
 	 * @param url URL of Web Page in String
 	 * @param imageAttributes List of ImageAttribute Class to provide specific image related information
 	 */
 	public FileSystemThumbnailer(String url,List<ImageAttributes> imageAttributes){
 		super(null,url,imageAttributes);
 	}
 	
 	/**
 	 * @param object object of QObject class 
 	 * @param url URL of Web Page in String
 	 * @param imageAttributes object of ImageAttribute Class to provide specific image related information
 	 */
 	public FileSystemThumbnailer(QObject obj,String url,List<ImageAttributes> imageAttributes){
 		super(obj,url,imageAttributes);
 	}
 
 	/**
 	 * Method to load html content from provided url   
 	 */
 	public boolean makeSnap(){
 		try{
 		if(logger.isDebugEnabled())
 			logger.debug("Connecting to url : "+this.url);
		QApplication.initialize(new String[] { });
 		page = new QWebPage(null);
 		page.mainFrame().load(new QNetworkRequest(this.url));
 		logger.debug("Page Loaded");
 		page.loadStarted.connect(this, "loadStarted()");
 		page.loadProgress.connect(this, "loadProgress()");
 		page.loadFinished.connect(this, "loadDone()");
 		logger.debug("Load Finished");
 		finished.connect(QApplication.instance(), "quit()");
 		logger.debug("image created");
 		QApplication.exec();
     	}catch(Exception exp){
 			logger.error(exp.getMessage()+ " Error While taking a snap");
 			return false;
 		}
 		return true;
 	}
 	private void loadStarted(){
 		logger.debug("Part in Started");
 	}
 	private void loadProgress(){
 		logger.debug("Part in Progress");
 	}
 	/**
 	 * Called internally by makeSnap() method to save loaded image(s) based on provided ImageAttribute details.  
 	 */
 	private boolean loadDone() {
 		logger.debug("Loading for page url : "+ this.url);
 		for(ImageAttributes imageAttribute: this.imageAttributes){
 			logger.debug("Loading for page url : "+ this.url);
 			page.setViewportSize(imageAttribute.getImageSize());
 			page.mainFrame().setScrollBarPolicy(Orientation.Horizontal, ScrollBarPolicy.ScrollBarAlwaysOff);
 			page.mainFrame().setScrollBarPolicy(Orientation.Vertical, ScrollBarPolicy.ScrollBarAlwaysOff);
 			page.setViewportSize(new QSize(Constants.DEFAULT_IMAGE_WIDTH, Constants.DEFAULT_IMAGE_HEIGHT));
 		    QImage image = new QImage(page.viewportSize(), QImage.Format.Format_ARGB32);
 		    image.fill(QColor.white.rgb());
 		    QPainter painter = new QPainter(image);
 		    page.mainFrame().render(painter);
 		    painter.end();
 		    image = image.scaled(imageAttribute.getImageSize(),AspectRatioMode.IgnoreAspectRatio,TransformationMode.FastTransformation);
 		    if(imageAttribute.isMirrored())
 		    	image = image.mirrored();
 		    String imageName= WebAppUtils.resolveImageStoragePath(imageAttribute, this.url.toString());
 		    logger.debug("Preparing image : "+ imageName);
 		    logger.info("Image prepared: "+image.save(imageName));
 		}
 	    finished.emit();
 	    return true;
     }
 	
 	/*
 	public static void main(String[] args) {
 		  new FileSystemThumbnailer("http://www.google.com").makeSnap();
 		  new FileSystemThumbnailer("http://www.google.com",new PNGImageAttributes()).makeSnap();
 		  new FileSystemThumbnailer("http://www.google.com",new PNGImageAttributes("C:\\temp\\new")).makeSnap();
 		  List<ImageAttributes> imageAttributes = new ArrayList<ImageAttributes>();
 		  imageAttributes.add(new JPEGImageAttributes("C:\\temp\\JPEGImage"));
 		  imageAttributes.add(new JPEGImageAttributes(new QSize(800,600),"C:\\temp\\JPEGImage_800_600"));
 		  imageAttributes.add(new PNGImageAttributes(new QSize(800,600),"C:\\temp\\PNGImage_800_600"));
 		  new FileSystemThumbnailer("http://www.facebook.com",imageAttributes).makeSnap();
 	}
 	*/
 }
