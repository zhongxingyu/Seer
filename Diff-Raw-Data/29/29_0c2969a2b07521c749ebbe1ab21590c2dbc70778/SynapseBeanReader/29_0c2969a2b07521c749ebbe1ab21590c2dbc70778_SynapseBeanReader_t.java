 package de.unikassel.ann.io;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.supercsv.cellprocessor.ParseBool;
 import org.supercsv.cellprocessor.ParseDouble;
 import org.supercsv.cellprocessor.ParseInt;
 import org.supercsv.cellprocessor.Token;
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.io.CsvBeanReader;
 import org.supercsv.io.ICsvBeanReader;
 
 import de.unikassel.ann.io.beans.SynapseBean;
 
 
 public class SynapseBeanReader extends BasicCsvReader {
 	
 	/* CSV */
 	/* "from";"to";"value";"random" */
 	
 	static String[] header2beanMapping = new String[]{"from", "to", "value", "random"};
 	static CellProcessor[] processor = new CellProcessor[] {new ParseInt(), new ParseInt(), new Token("", Double.NaN, new ParseDoubleUni()), new Token("", false, new ParseBool())};
 	
 	
 	public static List<SynapseBean> readData(BufferedReader bufferedReader) {
 		final List<SynapseBean> list = new ArrayList<SynapseBean>();
 		
 		final ICsvBeanReader reader = new CsvBeanReader(bufferedReader, pref);
 		try {
 			String[] header = reader.getCSVHeader(true);
 			if (ArrayUtils.isEquals(header2beanMapping, header) == false) {
 				throw new IllegalArgumentException("wrong header\n"+header);	
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 		SynapseBean bean;
 		
 		try {
 			while ((bean = reader.read(SynapseBean.class, header2beanMapping, processor)) != null) {
 				list.add(bean);
 			}
 		} catch (Exception e) {
			checkEndOfFile(e, bufferedReader);
 		}
 		return list;
 	}
 
 }
