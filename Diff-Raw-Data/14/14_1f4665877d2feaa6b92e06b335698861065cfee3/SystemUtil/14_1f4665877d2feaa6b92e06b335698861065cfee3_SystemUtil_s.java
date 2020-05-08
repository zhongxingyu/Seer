 package rxr.util;
 
 import java.awt.*;
 import java.awt.datatransfer.*;
 import java.io.*;
 
 public class SystemUtil
 {
 	public static ClipboardOwner owner;
 
 	/**
 	 * Place a String on the clipboard, and make this class the owner of the
 	 * Clipboard's contents.
 	 */
 	public static void setClipboardContents(String str)
 	{
 		StringSelection stringSelection = new StringSelection(str);
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		clipboard.setContents(stringSelection, owner);
 	}
 
 	/**
 	 * Get the String residing on the clipboard.
 	 * 
 	 * @return any text found on the Clipboard; if none found, return an empty
 	 *         String.
 	 */
 	public static String getClipboardContents()
 	{
 		String result = "";
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		//odd: the Object param of getContents is not currently used
 		Transferable contents = clipboard.getContents(null);
 		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
 		if(hasTransferableText)
 		{
 			try
 			{
 				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
 			}
 			catch(UnsupportedFlavorException ex)
 			{
 				//highly unlikely since we are using a standard DataFlavor
 				java.lang.System.out.println(ex);
 			}
 			catch(IOException ex)
 			{
 				java.lang.System.out.println(ex);
 			}
 		}
 		return result;
 	}
 
 	public static void sleep(int m)
 	{
 		try
 		{
 			Thread.sleep(m);
 		}
 		catch(Exception e)
 		{
 			//
 		}
 	}
 
 	/**
 	 * 
 	 * @param f
 	 * @return true if the file had to be created, false otherwise.
 	 * @throws IOException
 	 */
 	public static boolean createFile(File f) throws IOException
 	{
 		if(f.exists())
 		{
 			return false;
 		}
		if(!f.getParentFile().exists())
 		{
 			f.getParentFile().mkdirs();
 		}
 		f.createNewFile();
 		return true;
 	}
 }
