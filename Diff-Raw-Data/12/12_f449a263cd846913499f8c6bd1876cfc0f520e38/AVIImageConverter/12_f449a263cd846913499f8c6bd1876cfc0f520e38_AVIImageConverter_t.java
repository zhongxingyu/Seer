 package org.dawnsci.conversion.converters;
 
 import static org.monte.media.FormatKeys.EncodingKey;
 import static org.monte.media.FormatKeys.FrameRateKey;
 import static org.monte.media.FormatKeys.MediaTypeKey;
 import static org.monte.media.VideoFormatKeys.DepthKey;
 import static org.monte.media.VideoFormatKeys.ENCODING_AVI_MJPG;
 import static org.monte.media.VideoFormatKeys.HeightKey;
 import static org.monte.media.VideoFormatKeys.QualityKey;
 import static org.monte.media.VideoFormatKeys.WidthKey;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import org.dawb.common.services.IPlotImageService;
 import org.dawb.common.services.PlotImageData;
 import org.dawb.common.services.PlotImageData.PlotImageType;
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
 import org.eclipse.dawnsci.plotting.api.PlotType;
 import org.eclipse.dawnsci.plotting.api.PlottingFactory;
 import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.ui.services.IDisposable;
 import org.monte.media.Format;
 import org.monte.media.FormatKeys.MediaType;
 import org.monte.media.avi.AVIWriter;
 import org.monte.media.math.Rational;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 /**
  * Class to create a video of slices.
  * 
  * @author fcp94556
  *
  */
 public class AVIImageConverter extends AbstractImageConversion {
 	
 	private static final Logger logger = LoggerFactory.getLogger(AVIImageConverter.class);
 
 	private IImageService        imageService;
 	private IPlotImageService    thumbService;
 
 	/**
 	 * dir where we put the temporary images which we will later write to video.
 	 */	
 	public AVIImageConverter(IConversionContext context) throws Exception {
 		super(context);
 		
 		final File avi = new File(context.getOutputPath());
		
		if (context.getFilePaths().contains(avi.getAbsolutePath())) {
			throw new Exception("The output path is the same as the input path! "+avi);
		}
 		if (!avi.exists()) {
 			avi.getParentFile().mkdirs();
 			try {
 				if (context.getFilePaths().size()<2) {
 					avi.createNewFile();
 				} else {
 					avi.mkdir();
 				}
 			} catch (Throwable ne) {
 				logger.error("Cannot create file "+avi, ne);
 			}
 		}
 		if (!avi.isFile() && context.getFilePaths().size()<2) {
 			throw new RuntimeException("The output path must be a single file when converting one file!");
 		}
 		
 		final Enum sliceType = getSliceType();
 		if (sliceType==null) throw new Exception("A slice type must be set for the video!");
 		if (!(sliceType instanceof PlotType)) throw new Exception("The video export currently does not work with slice "+sliceType);
 		
 		
 		avi.getParentFile().mkdirs();
 				
 		this.imageService = (IImageService)ServiceManager.getService(IImageService.class);
 		this.thumbService = (IPlotImageService)ServiceManager.getService(IPlotImageService.class);	            		
 
 	}
 
 	private File       selected=null;
 	private AVIWriter  out;
 	private Point XYD_PLOT_SIZE = new Point(1024,768);
 
 	private PlotImageData plotImageData;
 	
 	/**
 	 * This convert cannot be asynchronous. (Like most actually)
 	 * @param slice
 	 * @throws Exception
 	 */
 	@Override
 	protected synchronized void convert(IDataset slice) throws Exception {
 		
 		if (getSliceType() == PlotType.SURFACE) {
 			final String plotName = context.getSelectedConversionFile().getName();
 			final IPlottingSystem system = PlottingFactory.getPlottingSystem(plotName);
 			if (system==null) throw new Exception("\nTo export videos to surfaces, please do the following things:\n\n1. Ensure that you are in 'Data Browsing' perspective.\n2. Open the file, and plot an initial surface.\n3. This orientation is then used for the surface orientation during export.\n4. Remember to set the window as desired.\n5. Rerun the conversion, this plot will be used.");
 		}
 
 		try {
 		
 			if (context.getMonitor()!=null && context.getMonitor().isCancelled()) {
 				throw new Exception(getClass().getSimpleName()+" is cancelled");
 			}
 			slice = (slice.getRank()==2 && getSliceType() == PlotType.IMAGE) ? getDownsampled(slice) : slice;
 
 			boolean newAVIFile = selected==null || !selected.equals(context.getSelectedConversionFile());
 			if (context.isExpression() && out!=null) newAVIFile=false;
 			if (newAVIFile) {
 				if (out!=null) out.close();
 
 				final File outputFile = getAVIFile();
 				this.out = new AVIWriter(outputFile);
 							
 				if (context.getMonitor()!=null) {
 					final String selectedName = context.getSelectedConversionFile()!=null ? context.getSelectedConversionFile().getName() : "";
 					context.getMonitor().subTask("Converting '"+selectedName+"' to '"+outputFile.getName()+"'");
 				}
 			}
 			
 			if (plotImageData==null) {
 				plotImageData = new PlotImageData();
 				plotImageData.setConstantRange(true);
 				if (getImageServiceBean()!=null) {
 					plotImageData.setImageServiceBean(getImageServiceBean());
 				} else {
 					// We will generate an image service bean 
 					plotImageData.setImageServiceBean(imageService.createBeanFromPreferences());
 				}
 			}
 						
 			ImageData       data = getImageData(slice, plotImageData);
 			BufferedImage   img  = imageService.getBufferedImage(data);
 			
 			if (newAVIFile) {
 				Format format = new Format(EncodingKey, ENCODING_AVI_MJPG, DepthKey, 24, QualityKey, 1f);
 				format = format.prepend(MediaTypeKey, MediaType.VIDEO, //
 						FrameRateKey, new Rational(getFrameRate(), 1),// frame rate
 						WidthKey,     img.getWidth(), //
 						HeightKey,    img.getHeight());
 
 				out.addTrack(format);
 	        	out.setPalette(0, img.getColorModel());	       
 			}
 			
 	        out.write(0, img, 1);
 	        
 	        if (context.getMonitor()!=null) context.getMonitor().worked(1);
 	        
 		} finally {
 			selected = context.getSelectedConversionFile();
 		}
 	}
 	
 	private IDisposable plotDisposable;
 	
 	private ImageData getImageData(IDataset slice, PlotImageData pdata) throws Exception {
 		
 		pdata.setData(slice);
 		pdata.setWidth(XYD_PLOT_SIZE.x);
 		pdata.setHeight(XYD_PLOT_SIZE.y);
 		
 		// We override the slice name so that update works.
 		final String title = slice.getName(); // Contains information about slice.
 		pdata.setPlotTitle(title);
 		slice.setName("slice");
 
 		if (slice.getRank()==2 && getSliceType()==PlotType.IMAGE) {
 			pdata.setWidth(slice.getShape()[1]);
 			pdata.setHeight(slice.getShape()[0]);
 			if (isAlwaysShowTitle()) {
 				pdata.setType(PlotImageType.IMAGE_PLOT);
 			} else {
 			    pdata.setType(PlotImageType.IMAGE_ONLY);
 			}
 			
 		} else {
 			
 			String plotName = getSliceType()==PlotType.SURFACE
 					        ? context.getSelectedConversionFile().getName()
 					        : null;
 			if (plotDisposable==null) plotDisposable = thumbService.createPlotDisposable(plotName);
 			pdata.setDisposible(plotDisposable);
 			
 			if (getSliceType()==PlotType.XY) {
 				pdata.setType(PlotImageType.XY_PLOT);
 			} else if (getSliceType()==PlotType.SURFACE) {
 				pdata.setType(PlotImageType.SURFACE_PLOT);
 			}
 		}
 		
 		final Image     image = thumbService.getImage(pdata);
 		final ImageData data  = image.getImageData();
 		image.dispose();
 		return data;
 	}
 
 	private File getAVIFile() {
 		if (context.getFilePaths().size()<2 || context.getSelectedConversionFile()==null) {
 			return new File(context.getOutputPath());
 		} else {
 			final String name = getFileNameNoExtension(context.getSelectedConversionFile());
 			return new File(context.getOutputPath()+"/"+name+".avi");
 		}
 	}
 	
 	private long getFrameRate() {
 		if (context.getUserObject()==null) return 1;
 		return ((ConversionInfoBean)context.getUserObject()).getFrameRate();
 	}
 	
 
 	@Override
 	public void close(IConversionContext context) throws IOException {
         if (out!=null) out.close();
         if (plotDisposable!=null && getSliceType()!=PlotType.SURFACE)  {
         	// Surfaces use the live plotter and are not disposable.
         	plotDisposable.dispose();
         }
         plotImageData = null;
 	}
 
 }
