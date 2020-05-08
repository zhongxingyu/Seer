 package in.di.ce.monitor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.monitor.FileAlterationListener;
 import org.apache.commons.io.monitor.FileAlterationObserver;
 import org.apache.log4j.Logger;
 import org.springframework.util.Assert;
 
 public class SharedVideosListener implements FileAlterationListener {
 
 	private final Notifier notifier;
 	public Logger logger = Logger.getLogger(getClass());
 
 	public SharedVideosListener(Notifier notifier) {
 		this.notifier = notifier;
 	}
 	
 	/*
 	 * TODO pedirle el user al dimon 
 	 */
 	private static final String userId = "EXAMPLE_USER";
 
 	// here you have to implement a couple of methods and especially this one:
 	public void onDirectoryDelete(File directory) {
 
 		logger.info("El directorio compartido se borro, eliminamos todo del repo");
 
 		for (File file : directory.listFiles()){
 
 			String videoId = md5(file);
 
 			if(isPart(file)) {
 				String [] meta = meta(file);
 				notifier.addCacho(userId, videoId, Long.valueOf(meta[0]), Long.valueOf(meta[1]));
 			} else {
 				notifier.addVideo(videoId, file.getName(), file.length());
 			}
 
 			System.out.println("cachos:");
 			System.out.println(notifier.listCachos(videoId));
 			System.out.println("videos:");
 			System.out.println(notifier.listVideos(videoId));
 		}
 	}
 
 	@Override
 	public void onFileCreate(File file) {
 
 		File dest = null;
 
 		if(file.getName().contains(" ")) {
 			dest = new File(org.apache.commons.lang.StringUtils.replace(file.getAbsolutePath(), " ", "_"));
 			if ( !file.renameTo(dest) ){
 				throw new RuntimeException("no puedo renombrar");
 			}
 			file= dest;
 		}
 
 		String videoId = md5(file);
 
 		if(isPart(file)) {
 
 			String [] meta = meta(file);
 			long start  = Long.valueOf(meta[0]);
 			long lenght = Long.valueOf(meta[1]);
 			
 			if( isComplete(file, lenght) ){
 				notifier.addCacho(userId, videoId, start, lenght);
 			}
 		} else {
 			notifier.addVideo(videoId, file.getName(), file.length());
 		}
 
 		System.out.println("cachos:");
 		System.out.println(notifier.listCachos(videoId));
 		System.out.println("videos:");
 		System.out.println(notifier.listVideos(videoId));
 
 	}
 
 	private boolean isComplete(File file, long lenght) {
 		return file.length() == lenght;
 	}
 
 	private String[] meta(File file) {
 
 		String[] meta = file.getName().split(".");
 
 		Assert.isTrue(meta.length == 3);
 
 		String[] splittedMeta = meta[1].split("-");
 
 		Assert.isTrue(splittedMeta.length == 2);
 
 		return splittedMeta;
 	}
 
 	private boolean isPart(File file) {
 		return file.getName().endsWith(".part");
 	}
 
 	private String md5(File file) {
 
 		byte[] bytes = null;
 		try {
 			bytes = IOUtils.toByteArray(new FileInputStream(file));
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		//new byte[(int) file.length()]
 
 		MessageDigest md = null;
 		try {
 			md = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		md.reset();
 		md.update(bytes);
 		byte[] thedigest = md.digest();
 
 		BigInteger bigInt = new BigInteger(1,thedigest);
 		String hashtext = bigInt.toString(16);
 		// Now we need to zero pad it if you actually want the full 32 chars.
 		while(hashtext.length() < 32 ){
 			hashtext = "0"+hashtext;
 		}
 		System.out.println(hashtext);
 		return hashtext;
 	}
 
 	@Override
 	public void onFileDelete(File file) {
 
 		logger.info("video borrado <"+file.getName()+">, actualizamos repo");
 
 		String videoId = md5(file);
 
 		if(isPart(file)) {
 
 			String [] meta = meta(file);
 
 			notifier.addCacho(userId, videoId, Long.valueOf(meta[0]), Long.valueOf(meta[1]));
 		} else {
 			notifier.addVideo(videoId, file.getName(), file.length());
 		}
 
 		System.out.println("cachos:");
 		System.out.println(notifier.listCachos(videoId));
 		System.out.println("videos:");
 		System.out.println(notifier.listVideos(videoId));
 
 	}
 
 	@Override
 	public void onStart(FileAlterationObserver observer) {
 		System.out
 		.println("FSM.begin().new FileAlterationListener() {...}.onStart()");
 
 	}
 
 	@Override
 	public void onDirectoryCreate(File directory) {
 		System.out
 		.println("FSM.begin().new FileAlterationListener() {...}.onDirectoryCreate()");
 
 	}
 
 	@Override
 	public void onDirectoryChange(File directory) {
 		System.out
 		.println("FSM.begin().new FileAlterationListener() {...}.onDirectoryChange()");			
 	}
 
 	@Override
 	public void onFileChange(File file) {
 		System.out
 		.println("FSM.begin().new FileAlterationListener() {...}.onFileChange()");
 
 	}
 
 	@Override
 	public void onStop(FileAlterationObserver observer) {
 		System.out.println("FSM.begin().new FileAlterationListener() {...}.onStop()");
 	}
 
 }
