 package jp.thisnor.dre.bindup;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 
 import jp.thisnor.dre.core.FileEntry;
 import jp.thisnor.dre.core.Measurer;
 import jp.thisnor.dre.core.MeasureOptionEntry;
 
 public class BinDupMeasurer implements Measurer {
 	@Override
 	public void init(Map<String, MeasureOptionEntry> optionMap) {
 	}
 
 	@Override
 	public Object convert(FileEntry entry) throws Exception {
 		return entry;
 	}
 
 	@Override
 	public int measure(Object data1, Object data2, int threshold) {
 		FileEntry entry1 = (FileEntry)data1;
 		FileEntry entry2 = (FileEntry)data2;
 		if (entry1.equals(entry2)) return 0;
 		int dSize = Math.abs((int)(entry2.getSize() - entry1.getSize()));
 		if (dSize == 0) {
 			InputStream in1 = null, in2 = null;
 			try {
 				in1 = new BufferedInputStream(entry1.open());
 				in2 = new BufferedInputStream(entry2.open());
 				int read1, read2;
 				do {
 					read1 = in1.read();
 					read2 = in2.read();
 				} while (read1 == read2 && read1 != -1);
 				if (read1 == -1) {
 					return 0;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 				try {
 					if (in1 != null) in1.close();
 				} catch (IOException e) {}
 				try {
 					if (in2 != null) in2.close();
 				} catch (IOException e) {}
 			}
 		}
 		return dSize + 1;
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
