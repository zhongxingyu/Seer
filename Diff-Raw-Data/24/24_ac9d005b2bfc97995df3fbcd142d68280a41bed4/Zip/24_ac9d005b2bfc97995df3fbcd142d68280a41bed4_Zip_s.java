 package org.wslhk.io;
 
 import de.schlichtherle.io.File;
 
 import de.schlichtherle.io.ArchiveDetector;
 import de.schlichtherle.io.DefaultArchiveDetector;
 import de.schlichtherle.io.archive.tar.TarBZip2Driver;
 import de.schlichtherle.io.archive.tar.TarDriver;
 import de.schlichtherle.io.archive.tar.TarGZipDriver;
 import de.schlichtherle.io.archive.zip.CheckedZip32Driver;
 
 public class Zip {
 
 	public static void main(String[] args) { 
         
 		int a = zip("E:/workspace/fncms/WEB-INF/src/fncms/io/", "e:/zz/xx22.zip"); 
 		
 		//unzip("e:/aaaa.zip","e:/zz");
 		//int a = zipSingleFile("e:\\aaaa.zip","E:/zz/"); 
     } 
 
      
     /** 
      * ѹ��һ���ļ� 
      *  
      * @param srcFileName 
      * @param zipFileName 
      * @return 
      */ 
     public static int zip(String srcFileName, String zipFileName) { 
         return zipSingleFile(srcFileName, zipFileName, "GBK"); 
     } 
 
     public static int zipSingleFile(String srcFileName, String zipFileName, 
             String encoding) { 
         ArchiveDetector detector = new DefaultArchiveDetector( 
                ArchiveDetector.ALL, new Object[] { "zip", 
                        new CheckedZip32Driver(encoding), "tar", 
                        new TarDriver(encoding), "tgz|tar.gz", 
                        new TarGZipDriver(encoding), "tbz|tar.bz2", 
                        new TarBZip2Driver(encoding) }); 
 System.out.println("-->"+srcFileName);
         String srcShortFilename = srcFileName.substring(srcFileName .lastIndexOf("/")); 
 
         boolean result = new de.schlichtherle.io.File(srcFileName, detector) 
                 .copyAllTo(new de.schlichtherle.io.File(zipFileName + "/" 
                         + srcShortFilename, detector)); 
         if (result) 
             return 0; 
         else 
             return -1; 
     } 
 
     
     public static int unzip(String zipFileName,String srcFileName){
 		return unzipSrcFile(zipFileName,srcFileName,"GBK");
 	}
 	private static int unzipSrcFile(String zipFileName, String srcFileName,String encoding){
 		ArchiveDetector detector = new DefaultArchiveDetector(
 				ArchiveDetector.ALL,new Object[]{"zip",new CheckedZip32Driver(encoding)});
			boolean result = new File(zipFileName,detector).copyAllTo(new File(srcFileName,detector));
 			if(result){
 				return 0;
 			}else{
 				return -1;
 			}
 	}
 }
