 package org.rsbot.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 public class CacheWriter {
 	private final List<String> queue = new ArrayList<String>();
 	private final QueueWriter writer;
 
 	public CacheWriter(final String fileName) {
 		writer = new QueueWriter(fileName);
 		writer.start();
 	}
 
 	public void add(String list) {
 		if (list != null) {
 			String[] lines = list.split("\n");
 			if (lines != null) {
 				queue.addAll(Arrays.asList(lines));
 			}
 			lines = null;
 		}
 		list = null;
 	}
 
 	public void destroy() {
 		writer.destroyWriter();
 	}
 
 	private class QueueWriter extends Thread { //For slow systems and reduced lag, let's make writing slow and threaded.
 		private boolean destroy = false;
 		private final File file;
 
 		public QueueWriter(final String fileName) {
 			file = new File(fileName);
 			if (!file.exists()) {
 				try {
 					if (file.createNewFile()) {
 						file.setExecutable(false);
 						file.setReadable(true);
 						file.setWritable(true);
 					}
 				} catch (Exception e) {
 					destroy = true;
 				}
 			}
 		}
 
 		public void run() {
 			List<String> outList = new ArrayList<String>();
			while ((!destroy || queue.size() > 0) && file.exists() && file.canWrite()) {
 				try {
 					FileWriter fileWriter = new FileWriter(file);
 					BufferedWriter out = new BufferedWriter(fileWriter);
 					if (queue.size() > 0) {
 						outList.clear();
 						if (queue.size() >= 1000) {
 							outList.addAll(queue.subList(0, 999));
 							queue.removeAll(outList);
 						} else {
 							outList.addAll(queue);
 							queue.clear();
 						}
 						Iterator<String> outLines = outList.listIterator();
 						while (outLines.hasNext()) {
 							String line = outLines.next();
 							out.write(line + "\n");
 						}
 					}
 					out.flush();
 					out.close();
 					try {
 						Thread.sleep(5000);
 					} catch (InterruptedException ignored) {
 					}
 				} catch (IOException ignored) {
 				}
 			}
 		}
 
 		public void destroyWriter() {
 			destroy = true;
 		}
 	}
 }
