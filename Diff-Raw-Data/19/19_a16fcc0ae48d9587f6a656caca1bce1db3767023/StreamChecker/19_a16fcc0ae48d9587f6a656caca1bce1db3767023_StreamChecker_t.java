 package com.im1x;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 /**
  * 
  * @author Vitaly Batrakov
  *
  */
 public class StreamChecker implements Runnable {
 	private String inputDir = "", outputDir = "";
 	private List<String> allOut = new ArrayList<String>();
 
 	@Override
 	public void run() {
 		if (checkDirs() && existOrCreateDirs()) {
 			readAndProcessing(getFilesNames());
 		}
 	}
 	
 	/**
 	 * set input dir
 	 * 
 	 * @param inputDir - path
 	 */
 	public void setInputDir(String inputDir) {
 		this.inputDir = inputDir;
 	}
 
 	/**
 	 * set output file
 	 * 
 	 * @param outputDir - path
 	 */
 	public void setOutputDir(String outputDir) {
 		this.outputDir = outputDir;
 	}
 	
 	/**
 	 * Test TextFields
 	 * 
 	 * @return false if inputDir or outputDir is empty
 	 */
 	protected boolean checkDirs() {
 		if (inputDir.isEmpty() || outputDir.isEmpty()) {
 			App.window.btnSetEnabled();
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Check folder for exist, if not exist - create
 	 * 
 	 * @return true - folders exist, or create
 	 */
 	protected boolean existOrCreateDirs() {
 		File inD = new File(inputDir);
 		File outD = new File(outputDir);
 
 		try {
 			if (!inD.exists()) {
 				inD.mkdirs();
 			}
 
 			if (!outD.getParentFile().exists()) {
 				outD.getParentFile().mkdirs();
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Return list of files
 	 * 
 	 * @return return File[]
 	 */
 	protected File[] getFilesNames() {
 		File inDir = new File(inputDir);
 		return inDir.listFiles();
 	}
 	
 	/**
 	 * Reads a line from the file and sends it to the processing
 	 * 
 	 * @param files - filelist
 	 */
 	protected void readAndProcessing(File[] files) {
 		CheckLink checkLink = new CheckLink();
 		int timeout = App.window.getTimeout();
 		boolean checkCanalnames = App.window.getCheckCanelNames();
 
 		for (File fls : files) {
 			if (fls.isFile()) {
 				String nameStream, urlStream = null;
 				BufferedReader br = null;
 				try {
 					br = new BufferedReader(new InputStreamReader(new FileInputStream(fls.getAbsolutePath()), "Cp1251"));
 					while ((nameStream = br.readLine()) != null) {
 						if (nameStream.contains("EXTINF")) {
 							urlStream = br.readLine();
 							App.window.setLblAll(App.window.getLblAll() + 1);
 							
 							if (checkCanalnames) {
 								if (!allOut.contains(urlStream)	&& (!allOut.contains(nameStream))) {
 									putParam(nameStream, urlStream, checkLink, timeout, checkCanalnames);
 								} else {
 									App.window.setLblDup(App.window.getLblDup() + 1);
 								}
 							} else {
 								if (!allOut.contains(urlStream)) {
 									putParam(nameStream, urlStream, checkLink, timeout, checkCanalnames);
 								} else {
 									App.window.setLblDup(App.window.getLblDup() + 1);
 								}
 							}
 						}
 					}
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				} finally {
 					if (br != null) {
 						try {
 							br.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 			}
 		}
 		writeToPlayList();
 	}
 	
 	/**
 	 * Write data to file
 	 */
 	private void writeToPlayList() {
 		BufferedWriter out = null;
 		try {
 
 			out = new BufferedWriter(new FileWriter(outputDir));
			out.write("#EXTM3U");
			out.newLine();
 
 			for (int i = 0; i <= allOut.size() - 1; i++) {
				out.write(allOut.get(i));
				out.newLine();
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			App.window.btnSetEnabled();
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Put parameters in CheckLink.check
 	 * 
 	 * @param nameStream - channel name
 	 * @param urlStream - channel url
 	 * @param checkLink - class for check
 	 * @param timeout - timeout
 	 * @param checkCanalnames - check for duplicate channel names
 	 * @throws ConnectException if wrong URL
 	 */
 	private void putParam(String nameStream, String urlStream, CheckLink checkLink, int timeout, boolean checkCanalnames) throws ConnectException {
 
 		int checkRes = checkLink.check(urlStream, timeout);
 
 		if (checkRes == 1) {
 			allOut.add(nameStream);
 			allOut.add(urlStream);
 			App.window.setLblGood(App.window.getLblGood() + 1);
 		} else if (checkRes == 2) {
 			allOut.add(nameStream);
 			allOut.add(urlStream);
 			App.window.setLblGood(App.window.getLblGood() + 1);
 			App.window.setLblPass(App.window.getLblPass() + 1);
 		} else {
 			App.window.setLblBad(App.window.getLblBad() + 1);
 		}
 
 	}
 
 }
