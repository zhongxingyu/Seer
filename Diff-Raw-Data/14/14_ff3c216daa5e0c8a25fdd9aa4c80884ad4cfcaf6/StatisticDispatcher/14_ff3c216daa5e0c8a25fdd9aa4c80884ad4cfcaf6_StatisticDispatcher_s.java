 package statistic;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import starter.Shared;
 
 public class StatisticDispatcher {
 	
 	private String fileLocation = Shared.DEFAULT_STATISTIC_FILE;
 	private LinkedList<StatisticPackage> packagesBuffer = new LinkedList<StatisticPackage>();
 	private LinkedList<StatisticPackage> currentlyWritingPackages = new LinkedList<StatisticPackage>();
 	private int packageBufferSize = Shared.DEFAULT_PACKAGE_BUFFER;
 	
 	private Lock packageBufferLock = new ReentrantLock();
 	private Lock currentlyWritingPackagesLock = new ReentrantLock();
 
 	public StatisticDispatcher(String curStatisticFileURL) {
 		fileLocation = curStatisticFileURL;
 	}
 
 	public void addPackage(StatisticPackage statisticPackage) {
 		packageBufferLock.lock();
 		packagesBuffer.add(statisticPackage);
 		packageBufferLock.unlock();
 		if (packagesBuffer.size() == packageBufferSize)
 			tryToExportStatistic();
 	}
 	
 	public void flush() {
 		while (!tryToExportStatistic()) {
 			try {
 			    Thread.sleep(200);
 			} catch(InterruptedException ex) {
 			    Thread.currentThread().interrupt();
 			}
 		}
 		while (!currentlyWritingPackagesLock.tryLock()) {	// wait for finish of writing
 			try {
 			    Thread.sleep(200);
 			} catch(InterruptedException ex) {
 			    Thread.currentThread().interrupt();
 			}
 		}
 		currentlyWritingPackagesLock.unlock();
 	}
 	
 	private boolean tryToExportStatistic() {
		if (currentlyWritingPackagesLock.tryLock()) {  // it will be unlocked in startStatisticWritingTo(final File file)
 			exportStatistic();
 			return true;
 		}
 		return false;
 	}
 	
 	private void exportStatistic() {
 		packageBufferLock.lock();
 		currentlyWritingPackages.addAll(packagesBuffer);
 		packagesBuffer.clear();
 		packageBufferLock.unlock();
 		try {
 			File file = createFile();
 			startStatisticWritingTo(file);
 		} catch (IOException e) {
 			e.printStackTrace();
		} finally {
 		}
 	}
 	
 	private void startStatisticWritingTo(final File file) {
 		new Thread( new Runnable() {
 			@Override
 			public void run() {
 				writeStatisticTo(file);
 				currentlyWritingPackagesLock.unlock();
 			}
 		}).start();
 	}
 	
 	private void writeStatisticTo(File file) {
 		try {
 			BufferedWriter bw = new BufferedWriter(new FileWriter(file,true));
 			for (StatisticPackage pack : currentlyWritingPackages)
 				bw.write(pack.toString());
 			bw.flush();
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private File createFile() throws IOException {
 		File file = new File(fileLocation);
 		if (!file.exists())
 			file.createNewFile();
 		return file;
 	}
 }
