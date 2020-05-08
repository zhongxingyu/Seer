 package stv6.handlers.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 
 import stv6.Profile;
 import stv6.http.request.Request;
 import stv6.http.request.RequestHandler;
 import stv6.http.request.Response;
 import stv6.series.BasicSeries;
 import stv6.series.Series;
 
 /**
  * This handler tries to serve cover IMAGES. 
  * 
  * @author Daniel
  *
  */
 public class CoverHandler implements RequestHandler {
 	public static final String HANDLED_PAGE = "covers";
 	
 	/** Filename filter used to find cover images */
 	public static final FilenameFilter FILENAME_FILTER = new FilenameFilter() {
 
 		@Override
 		public boolean accept(File dir, String name) {
 			return name.matches("^[cC][oO][vV][eE][rR]\\..*$");
 		}
 		
 	};
 	
 	@Override
 	public boolean handle(Request request, Response response) {
 		// get the series 
 		BasicSeries series = null;
 		try {
 			String id_ = request.path.substring(
 					request.path.lastIndexOf('/')+1);
 			int id = Integer.parseInt(id_);
 			
 
 			Series s = Profile.getInstance()
 				.getSeries().getById(id);
 			if (!s.isManaged()) {
 				System.err.println("unmanaged series");
 				return showDefault(response);
 			}
 			
 			series = (BasicSeries) s;
 		} catch (StringIndexOutOfBoundsException e) {
 			// no valid id
 			return showDefault(response);
 		} catch (NumberFormatException e) {
 			// not valid id
 			return showDefault(response);
 		} catch (ClassCastException e) {
 			// untracked, no local path
 			return showDefault(response);
 		}
 		
//		if (series == null) {
//			return showDefault(response);
//		}
 		
 		String localPath = series.getLocalPath();
 		File localFile = new File(localPath);
 		
 		File[] files = localFile.listFiles(FILENAME_FILTER);
 		if (files == null || files.length == 0) {
 			return showDefault(response);
 		}
 		
 		File theFile = files[0]; // pick first candidate
 		if (!theFile.exists()) {		
 //			System.err.println("Cannot find: " + path + " (" +
 //			        theFile.getAbsolutePath() + ")");
 			System.err.println("No such file: " + theFile.getAbsolutePath());
 			return showDefault(response);
 		}
 		if (!theFile.canRead()) {
 //			System.err.println("Cannot read: " + theFile.getAbsolutePath());
 			return showDefault(response);
 		}
 		
 		return showFile(response, theFile);
 	}
 
 	private boolean showDefault(Response response) {
	    File def = new File("default-cover.png");
	    if (def.exists())
	        return showFile(response, def);
	    
	    return false;
 	}
 	
 	private boolean showFile(Response response, File theFile) {
 		String name = theFile.getName();
 		String ext = name.substring(name.lastIndexOf('.')+1);
 		response.setContentType("image/"+ext);
 		byte[] imgdata = null;
 		try {
 			imgdata = getBytesFromFile(theFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return showDefault(response);
 		}
 			
 		if (imgdata == null)
 			return showDefault(response);
 		
 		response.setBody(imgdata);
 		return true;
 	}
 
 	/** 
 	 * Thanks to laziness and Java Developer's Almanac
 	 * @param file
 	 * @return
 	 * @throws IOException
 	 */
     public static byte[] getBytesFromFile(File file) throws IOException {
         InputStream is = new FileInputStream(file);
     
         // Get the size of the file
         long length = file.length();
     
         // You cannot create an array using a long type.
         // It needs to be an int type.
         // Before converting to an int type, check
         // to ensure that file is not larger than Integer.MAX_VALUE.
         if (length > Integer.MAX_VALUE) {
             // File is too large.... if you're trying to serve this
         	//	file from SeriesTracker, you might be retarded :P
         	return new byte[]{};
         }
     
         // Create the byte array to hold the data
         byte[] bytes = new byte[(int)length];
     
         // Read in the bytes
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
             offset += numRead;
         }
     
         // Ensure all the bytes have been read in
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file "+file.getName());
         }
     
         // Close the input stream and return bytes
         is.close();
         return bytes;
     }
 
 	@Override
 	public String getHandledPage() {
 		return HANDLED_PAGE;
 	}
     
 }
