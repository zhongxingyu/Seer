 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan;
 
 import java.io.File;
 import java.util.StringTokenizer;
 import org.openide.cookies.OpenCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectNotFoundException;
 
 /**
  * Generic utilities
  * @author thibautc
  */
 public class FanUtilities
 {
 
     /**
      * Opens the given file in the editor.
      * @param newFile
      * @throws DataObjectNotFoundException
      */
     public static void openFileInEditor(File newFile) throws DataObjectNotFoundException
     {
 	FileObject fob = FileUtil.toFileObject(newFile);
 	openFileInEditor(fob);
     }
 
     private static void openFileInEditor(FileObject fob) throws DataObjectNotFoundException
     {
 	if (fob != null)
 	{
 	    DataObject dob = DataObject.find(fob);
 	    OpenCookie oc = dob.getCookie(OpenCookie.class);
 	    if (oc != null)
 	    {
 		oc.open();
 	    }
 	}
     }
 
     /**
      * Find a file object relative to a current fileobject.
      * @param fo
      * @param path
      * @return
      */
     public static FileObject getRelativeFileObject(final FileObject fo, String relativePath)
     {
	StringTokenizer st = new StringTokenizer(relativePath.replaceAll("\\","/"), "/");
 	// If fo is a  file, we start from the parent folder
 	FileObject curFile= fo.isFolder()?fo:fo.getParent();
 	// folow the relative path to try and find the requested file
 	while ((curFile != null) && st.hasMoreTokens())
 	{
 	    String nameExt = st.nextToken();
 	    // if "." do nothing.
 	    if( ! nameExt.equals("."))
 	    {
 		if(nameExt.equals(".."))
 		    curFile = curFile.getParent();
 		else
 		    curFile = curFile.getFileObject(nameExt);
 	    }
 	}
 	return curFile;
     }
 
     /**
      * Debug fileObject
      * @param fo
      */
     public static void dumpFileObject(FileObject fo)
     {
 	System.err.println("---- FileObject DUMP -----");
 	try
 	{
 	    dumpFileObject(fo, "");
 	    System.err.println("**---- FileObject root dump -----");
 	    //dumpFileObject(fo.getFileSystem().getRoot(),"**");
 	} catch (Exception e)
 	{
 	    e.printStackTrace();
 	}
     }
 
     private static void dumpFileObject(FileObject fo, String indent) throws Exception
     {
 	System.err.println(indent + "toStr: " + fo.toString());
 	System.err.println(indent + "Name: " + fo.getNameExt());
 	System.err.println(indent + "Path: " + fo.getPath());
 	System.err.println(indent + "URL: " + fo.getURL());
 	FileObject[] children = fo.getChildren();
 	//indent+="  ";
 	//for(int i=0;i!=children.length;i++)
 	//    dumpFileObject(children[i],indent);
     }
 }
