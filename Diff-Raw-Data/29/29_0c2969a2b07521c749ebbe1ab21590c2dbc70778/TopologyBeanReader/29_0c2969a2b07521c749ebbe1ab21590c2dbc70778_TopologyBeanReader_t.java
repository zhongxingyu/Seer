 package de.unikassel.ann.io;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.supercsv.cellprocessor.Optional;
 import org.supercsv.cellprocessor.ParseBool;
 import org.supercsv.cellprocessor.ParseDouble;
 import org.supercsv.cellprocessor.ParseInt;
 import org.supercsv.cellprocessor.Token;
 import org.supercsv.cellprocessor.constraint.StrMinMax;
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.io.CsvBeanReader;
 import org.supercsv.io.ICsvBeanReader;
 
 import de.unikassel.ann.io.beans.TopologyBean;
 
 public class TopologyBeanReader extends BasicCsvReader {
 
 	/* CSV */
 	/* "id";"layer";"bias";"function" */
 	
 	static String[] header2beanMapping = new String[]{"id", "layer", "bias", "function"};
 	static CellProcessor[] processor = new CellProcessor[] {new ParseInt(), new ParseInt(), new ParseBool(), new StrMinMax(1, 50)};
 	
 	
 	public static List<TopologyBean> readData(BufferedReader bufferedReader) {
 		final List<TopologyBean> list = new ArrayList<TopologyBean>();
 		final ICsvBeanReader reader = new CsvBeanReader(bufferedReader, pref);
 		try {
 			String[] header = reader.getCSVHeader(true);
 			if (ArrayUtils.isEquals(header2beanMapping, header) == false) {
 				throw new IllegalArgumentException("wrong header\n"+header);	
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 		TopologyBean bean;
 		
 		try {
 			while ((bean = reader.read(TopologyBean.class, header2beanMapping, processor)) != null) {
 				list.add(bean);
 			}
 		} catch (Exception e) {
			checkEndOfFile(e, bufferedReader);
 		}
 		return list;
 	}
 	
 	
 
 }
