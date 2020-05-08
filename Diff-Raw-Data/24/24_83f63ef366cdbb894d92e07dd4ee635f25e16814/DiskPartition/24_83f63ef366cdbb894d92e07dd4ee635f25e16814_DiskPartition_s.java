 package org.araqne.linux.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DiskPartition {
 	private String name;
 	private long used;
 	private long available;
 	private String path;
 
 	public String getName() {
 		return name;
 	}
 
 	public long getTotal() {
 		return this.used + this.available;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public long getUsed() {
 		return used;
 	}
 
 	public void setUsed(long used) {
 		this.used = used;
 	}
 
 	public long getAvailable() {
 		return available;
 	}
 
 	public void setAvailable(long available) {
 		this.available = available;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public static List<DiskPartition> getDiskPartitions() {
 		List<String> lines = readPartitions();
 		return getDiskPartitions(lines);
 	}
 
 	public static List<DiskPartition> getDiskPartitions(List<String> lines) {
 		List<DiskPartition> stats = new ArrayList<DiskPartition>();
 
 		for (int index = 0; index < lines.size(); index++) {
 			String line = lines.get(index);
 			String[] parts = line.split("[ ]+");
 			while (parts.length != 6) {
 				line += " " + lines.get(++index);
 				parts = line.split("[ ]+");
 			}
 
 			stats.add(getDiskStat(parts));
 		}
 		return stats;
 	}
 
 	private static List<String> readPartitions() {
 		java.lang.Process p = null;
 		BufferedReader br = null;
 		List<String> lines = new ArrayList<String>();
 		try {
 			p = Runtime.getRuntime().exec("df");
 			br = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("utf-8")));
 			br.readLine();
 			String line = null;
 			while ((line = br.readLine()) != null) {
 				lines.add(line);
 			}
 		} catch (IOException e) {
 		} finally {
 			if (p != null)
 				p.destroy();
 			if (br != null)
 				try {
 					br.close();
 				} catch (IOException e) {
 				}
 		}
 		return lines;
 	}
 
 	private static DiskPartition getDiskStat(String[] parts) {
 		DiskPartition stat = new DiskPartition();
 		stat.setName(parts[0]);
		stat.setUsed(Long.valueOf(parts[2]));
		stat.setAvailable(Long.valueOf(parts[3]));
 		stat.setPath(parts[5]);
 		return stat;
 	}
 }
