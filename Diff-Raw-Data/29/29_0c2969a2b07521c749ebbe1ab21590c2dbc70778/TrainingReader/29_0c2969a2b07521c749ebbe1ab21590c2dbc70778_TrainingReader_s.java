 package de.unikassel.ann.io;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.supercsv.cellprocessor.ParseDouble;
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.io.CsvBeanReader;
 import org.supercsv.io.ICsvBeanReader;
 
 import de.unikassel.ann.gen.DynamicBeanCreator;
 import de.unikassel.ann.io.beans.TrainingBean;
 import de.unikassel.ann.model.DataPairSet;
 
 public class TrainingReader extends BasicCsvReader {
 
 	static String[] header2beanMapping;
 	static CellProcessor[] processor;
 	
 	public static Class<?> generateBean(int inputSize, int outputSize) {
 		String className = null;
 		try {
 			className = DynamicBeanCreator.createAndWriteClass(inputSize, outputSize);
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		// load class file
 		ClassLoader classLoader = TrainingReader.class.getClassLoader();
 		
 		Class<?> loadedClass = null;
 		try {
 			loadedClass = classLoader.loadClass(className);
 		} catch (ClassNotFoundException e) {
 			System.err.println("could not load class: "+className);
 			e.printStackTrace();
 		}
 
 		try {
 			Method method = loadedClass.getDeclaredMethod("setSize", new Class[]{int.class, int.class});
 			method.invoke(null, new Object[]{inputSize, outputSize});
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		return loadedClass;
 	}
 	
 	public static List<TrainingBean> readData(final BufferedReader bufferedReader)
 			throws IOException, ClassNotFoundException {
 
 		final List<TrainingBean> list = new ArrayList<TrainingBean>();
 		
 		final ICsvBeanReader reader = new CsvBeanReader(bufferedReader, pref);
 		header2beanMapping = reader.getCSVHeader(true);
 		
 		int inputSize = 0;
 		int outputSize = 0;
 		int index = 0;
 		for (String s : header2beanMapping) {
 			if (s.charAt(0) == 'i' || s.charAt(0) == 'I') {
 				header2beanMapping[index] = s+inputSize;
 				inputSize++;
 			} else if (s.charAt(0) == 'o' || s.charAt(0) == 'O' ) {
 				header2beanMapping[index] = s=s+outputSize;
 				outputSize++;
 			}
 			index++;
 		}
 		processor = new CellProcessor[inputSize+outputSize];
 		for (int i=0; i<inputSize+outputSize; i++) {
 			processor[i] = new ParseDoubleUni();
 		}
 	
 		Class<?> loadedClass = generateBean(inputSize, outputSize);
 		TrainingBean instance;
 		
 		try {
 			while ((instance = (TrainingBean) reader.read(loadedClass, header2beanMapping, processor)) != null) {
 				list.add(instance);
 			}
 		} catch (Exception e) {
			checkEndOfFileAndMarkPosition(e, bufferedReader);
 		}
 		return list;
 	}
 	
 
 }
