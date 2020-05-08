 package com.github.tosdan.utils.varie;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.supercsv.io.CsvBeanReader;
 import org.supercsv.io.CsvListReader;
 import org.supercsv.io.CsvMapReader;
 import org.supercsv.io.ICsvBeanReader;
 import org.supercsv.io.ICsvListReader;
 import org.supercsv.io.ICsvMapReader;
 import org.supercsv.prefs.CsvPreference;
 
 public class CSVReader {
 
 	public static void main( String[] args ) throws IOException {
 		InputStream in = CSVReader.class.getResourceAsStream("file.csv");
 		List<Map<String, String>> result = read(in);
 
 		for(Map<String, String> map : result) { 
 			System.out.println(String.format("campoA=%s, campoB=%s", map.get("campoA"), map.get("campoB")));
 		}
 		
 		System.out.println("*\n*\n*\n**** BeanReader");
 		in = CSVReader.class.getResourceAsStream("file.csv");
 		List<CSVObjectTest> list = readToBean(in, CSVObjectTest.class);
 		for( CSVObjectTest obj : list ) {
 			System.out.println(String.format("campoA=%s, campoB=%s", obj.getCampoA(), obj.getCampoB()));
 		}
 		
 		System.out.println("*\n*\n*\n**** readToList");
 		in = CSVReader.class.getResourceAsStream("file-vr.csv");
		List<List<String>> llista = readToListOfRow(in, true);
 		for( List<String> l : llista ) {
 			System.out.println(l);
 		}
 	}
 
 	/**
 	 * 
	 * @param in Chiuso al termine della lettura del file
 	 * @return
 	 * @throws IOException
 	 */
 	public static List<Map<String, String>> read(InputStream in) throws IOException {
 		List<Map<String, String>> retVal = new ArrayList<Map<String,String>>();
 		
 		ICsvMapReader mapReader = null;
         try {
         	mapReader = new CsvMapReader(new InputStreamReader(in), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
             
         	final String[] header = mapReader.getHeader(true);
         	Map<String, String> temp;
         	while( (temp = mapReader.read(header)) != null )
         		retVal.add(temp);        		
         	
         } finally {
         	IOUtils.closeQuietly(mapReader);
         }
         
         return retVal;
 	}
 	
 	/**
 	 * Utile per la lettura dei csv con numero colonne variabili
	 * @param in Chiuso al termine della lettura del file
 	 * @return
 	 * @throws IOException
 	 */
	public static List<List<String>> readToListOfRow(InputStream in, boolean ignoreHeader) throws IOException {
 		List<List<String>> listaRighe = new ArrayList<List<String>>();
 		ICsvListReader listReader = null;
 		try {
 			listReader = new CsvListReader(new InputStreamReader(in), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
 			listReader.getHeader(ignoreHeader); // se true salta l'header: non e' consentito con CsvListReader
 			List<String> riga;
 			
 			while ( (listReader.read()) != null ) {
 				
 				riga = new ArrayList<String>();
 				listaRighe.add(riga);
 								
 				for(int i = 1 ; i <= listReader.length() ; i++)
 					riga.add(listReader.get(i));
 			}
 
 		} finally {
 			if ( listReader != null ) {
 				listReader.close();
 			}
 		}
 		return listaRighe;
 	}
 	
 	/**
 	 * 
	 * @param in Chiuso al termine della lettura del file
	 * @param clazz Classe dell'oggetto in cui viene trasformata ogni riga del csv
 	 * @return
 	 * @throws IOException
 	 */
 	public static <T> List<T> readToBean(InputStream in, Class<T> clazz) throws IOException {
 		List<T> retVal = new ArrayList<T>();
 		
 		ICsvBeanReader beanReader = null;
 		try {
 			beanReader = new CsvBeanReader(new InputStreamReader(in), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
 			
 			final String[] header = beanReader.getHeader(true);
 			T temp;
 			while( (temp = beanReader.read(clazz, header)) != null ) {
 				retVal.add(temp);        		
 			}
 			
 		} finally {
 			IOUtils.closeQuietly(beanReader);
 		}
 		
 		return retVal;
 	}
 	
 }
