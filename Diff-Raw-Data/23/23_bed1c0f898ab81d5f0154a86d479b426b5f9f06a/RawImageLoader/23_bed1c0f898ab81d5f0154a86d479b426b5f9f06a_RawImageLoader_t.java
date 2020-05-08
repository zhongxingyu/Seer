 package com.cateye.core.native_;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cateye.core.BadCropException;
 import com.cateye.core.BadFileException;
 import com.cateye.core.CorruptedImageException;
 import com.cateye.core.IImageLoader;
 import com.cateye.core.IProgressListener;
 import com.cateye.core.Image;
 import com.cateye.core.LoadingCancelledException;
 import com.cateye.core.UnsupportedFormatException;
 
 class RawImageLoader implements IImageLoader
 {
 	//# Progress listeners
 	private final List<IProgressListener> progressListeners = new ArrayList<IProgressListener>();
 	private final ExtractingProgressReporter progressReporter = new ExtractingProgressReporter(this);
 	
 	@Override
 	public void addProgressListener(IProgressListener listener)
 	{
 		progressListeners.add(listener);
 	}
 	
 	@Override
 	public void removeProgressListener(IProgressListener listener)
 	{
 		progressListeners.remove(listener);
 	}
 	
 	protected void raiseProgress(float progress)
 	{
 		for (IProgressListener listener : progressListeners)
 			listener.invoke(this, progress);
 	}
 	
 	//# Methods
 	@Override
 	public Boolean canLoad(String fileName)
 	{
 		return true; // TODO: implement a check of loading possibility
 	}
 	
 	@Override
 	public Image load(String fileName)
 	{
 		// load description
 		RawImageDescription description = loadDescription(fileName);
 		
 		// load bitmap
 		PreciseBitmap bitmap = new PreciseBitmap();
 		checkLoadingResult(LoadFromFile(getPathToFile(fileName), true, bitmap, progressReporter));
 		
 		// raise an event
 		return new Image(description, bitmap);
 	}
 	
 	@Override
 	public RawImageDescription loadDescription(String fileName)
 	{
 		RawImageDescription description = new RawImageDescription();
 		LoadDescriptionFromFile(getPathToFile(fileName), description);
 		
 		return description;
 	}
 	
 	/**
 	 * Returns a path to the file
 	 */
 	protected String getPathToFile(String fileName)
 	{
 		return new java.io.File(fileName).getAbsolutePath();
 	}
 	
 	protected void checkLoadingResult(int resultCode)
 	{
 		switch (resultCode)
 		{
 			case EXTRACTING_RESULT_OK:
 				return;
 			case EXTRACTING_RESULT_UNSUFFICIENT_MEMORY:
 				throw new OutOfMemoryError();
 			case EXTRACTING_RESULT_DATA_ERROR:
 				throw new CorruptedImageException();
 			case EXTRACTING_RESULT_IO_ERROR:
 				throw new BadFileException();
 			case EXTRACTING_RESULT_CANCELLED_BY_CALLBACK:
 				throw new LoadingCancelledException();
 			case EXTRACTING_RESULT_BAD_CROP:
 				throw new BadCropException();
 			case EXTRACTING_RESULT_UNSUPPORTED_FORMAT:
 				throw new UnsupportedFormatException();
 			default:
 				throw new UnknownResultException(resultCode);
 		}
 	}
 	
 	//# Native
 	static final int EXTRACTING_RESULT_OK = 0;
 	static final int EXTRACTING_RESULT_UNSUFFICIENT_MEMORY = 1;
 	static final int EXTRACTING_RESULT_DATA_ERROR = 2;
 	static final int EXTRACTING_RESULT_IO_ERROR = 3;
 	static final int EXTRACTING_RESULT_CANCELLED_BY_CALLBACK = 4;
 	static final int EXTRACTING_RESULT_BAD_CROP = 5;
 	static final int EXTRACTING_RESULT_UNSUPPORTED_FORMAT = 6;
 	static final int EXTRACTING_RESULT_UNKNOWN = 100;
 	
 	static native int LoadFromFile(String fileName, boolean divideBy2, PreciseBitmap res, ExtractingProgressReporter progressReporter);
 	static native int LoadDescriptionFromFile(String filename, RawImageDescription res);
 	static native void FreeDescription(RawImageDescription description); // TODO: Do we need it?
 	
 	static
 	{
		LibraryLoader.attach("CatEyeB.RawImageLoader", "raw.CatEyeLoader");
 	}
 	
 	/**
 	 * Implementation of native extracting progress reporter
 	 */
 	static class ExtractingProgressReporter
 	{
 		private final RawImageLoader loader;
 		
 		public ExtractingProgressReporter(RawImageLoader loader)
 		{
 			this.loader = loader;
 		}
 		
 		public boolean invoke(float progress)
 		{
 			loader.raiseProgress(progress);
 			return true;
 		}
 	}
 }
