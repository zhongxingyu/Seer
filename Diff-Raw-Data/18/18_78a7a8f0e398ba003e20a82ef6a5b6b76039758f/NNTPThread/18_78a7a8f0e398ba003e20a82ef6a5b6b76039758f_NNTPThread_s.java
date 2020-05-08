 import java.io.*;
 import ar.com.ktulu.yenc.*;
 
 class NNTPThread extends Thread {
 	String articleName;
 	NNTPConnection nc;
 	File segmentFile;
 	
 	NNTPThread(String an, NNTPConnection n, File f) {
 		this.articleName = an;
 		this.nc = n;
 		this.segmentFile = f;
 	}
 	
 	public void run() {
 		
 		FileInputStream fis;
 
		int bytes;
 		try {
 			
			nc.setArticleName(articleName);
 			bytes = nc.writeArticleToFile(segmentFile);
 			nc.unlock();
 			
 		} catch (Exception e) {
 			nc.unlock();
 			System.out.println ("NNTP GET: " + e.toString());
 		}
 		
 		/*
 		try {
 			OutputStream out = null;
 			fis = new FileInputStream(segmentFile);
 			YEncDecoder decoder = new YEncDecoder();
 			decoder.setInputStream(new BufferedInputStream(fis), true);
 			out = new BufferedOutputStream(new FileOutputStream(segmentFile.getAbsolutePath().concat(".bin")));
 			decoder.setOutputStream(out);
 			decoder.decode();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 		catch (YEncException e) {
 			System.err.println(e.getMessage());
 		}
 		*/
 	}
 	
 }
 
