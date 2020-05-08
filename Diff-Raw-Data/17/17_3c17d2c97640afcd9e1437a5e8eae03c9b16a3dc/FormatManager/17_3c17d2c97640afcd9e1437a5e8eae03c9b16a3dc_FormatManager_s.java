 package net.coobird.paint.io;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.filechooser.FileFilter;
 
 /**
  * 
  * @author coobird
  *
  */
 public final class FormatManager
 {
 	/**
 	 * Manage the supported formats
 	 * 
 	 * What is this class really for?
 	 * Perhaps tell it a desired format and return an approriate I/O filter?
 	 */
 	
 	private static List<ImageInput> inputList;
 	private static List<ImageOutput> outputList;
 	private static List<FileFilter> inputFilters;
 	private static List<FileFilter> outputFilters;
 	
 	/*
 	 * Initialize the FileFilter and ImageInput/ImageOutput Lists.
 	 */
 	static
 	{
 		inputList = new ArrayList<ImageInput>();
 		outputList = new ArrayList<ImageOutput>();
 		
 		inputList.add(new DefaultImageInput());
 		inputList.add(new JavaSupportedImageInput());
 		
 		outputList.add(new DefaultImageOutput());
 		outputList.add(new JavaSupportedImageOutput());
 		
 		inputFilters = new ArrayList<FileFilter>();
 		outputFilters = new ArrayList<FileFilter>();
 		
 		for (ImageInput ii : inputList)
 		{
 			for (FileFilter f : ii.getFileFilters())
 			{
 				inputFilters.add(f);
 			}
 		}
 		
 		for (ImageOutput io : outputList)
 		{
 			for (FileFilter f : io.getFileFilters())
 			{
 				outputFilters.add(f);
 			}
 		}
 	}
 	
 	/**
 	 * Returns an {@link ImageInput} object which can handle the file format of
 	 * {@link File} object.
 	 * @param f				{@code File} object for which to find an
 	 * 						{@code ImageInput} for.
 	 * @return				The {@code ImageInput} which can read the given
	 * 						file, or {@code null} if an adaquate
 	 * 						{@code ImageInput} cannot be found.
 	 */
 	public static ImageInput getImageInput(File f)
 	{
 		for (ImageInput imgInput : inputList)
 		{
 			if (imgInput.supportsFile(f))
 			{
 				return imgInput;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Returns an {@link ImageOutput} object which can handle the file format of
 	 * {@link File} object.
 	 * @param f				{@code File} object for which to find an
 	 * 						{@code ImageOutput} for.
 	 * @return				The {@code ImageOutput} which can read the given
	 * 						file, or {@code null} if an adaquate
 	 * 						{@code ImageOutput} cannot be found.
 	 */
 	public static ImageOutput getImageOutput(File f)
 	{
 		for (ImageOutput imgOutput : outputList)
 		{
 			if (imgOutput.supportsFile(f))
 			{
 				return imgOutput;
 			}
 		}
 		
 		return null;		
 	}
 	
 	/**
 	 * Returns a {@link List} of {@link FileFilters} for the formats supported
 	 * for input.
 	 * @return				A {@code List} of {@code FileFilter}s which can be
 	 * 						used to input an image.
 	 */
 	public static List<FileFilter> getInputFileFilters()
 	{
 		return inputFilters;		
 	}
 	
 	/**
 	 * Returns a {@link List} of {@link FileFilters} for the formats supported
 	 * for output.
 	 * @return				A {@code List} of {@code FileFilter}s which can be
 	 * 						used to output an image.
 	 */
 	public static List<FileFilter> getOutputFileFilters()
 	{
 		return outputFilters;		
 	}
 }
