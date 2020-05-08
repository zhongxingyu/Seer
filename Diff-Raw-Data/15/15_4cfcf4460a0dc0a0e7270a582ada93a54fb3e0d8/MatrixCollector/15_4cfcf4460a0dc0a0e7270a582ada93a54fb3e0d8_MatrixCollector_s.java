 /* libnp
  * Copyright (c) 2013, Lloyd T. Elliott and Yee Whye Teh
  */
 
 package libnp.mcmc.collectors;
 
 import java.io.*;
 
 public class MatrixCollector implements Collector {
 	private Collectable cc;
 	private String property;
 	private Object arg = null;
 	private int index = 0;
 	boolean collapse = false; // only use one
 	String prefix;
	
 
	public MatrixCollector(Collectable cc, String property, String prefix, Object arg) {
 		this.cc = cc;
 		this.property = property;
 		this.prefix = prefix;
		this.arg = arg;
 	}
 
	public MatrixCollector(Collectable cc, String property, String prefix) {
 		this.cc = cc;
 		this.property = property;
 		this.prefix = prefix;
 	}
 
	public MatrixCollector(Collectable cc, String property, String prefix, Object arg,
			boolean collapse) {
 		
 		this(cc, property, prefix);
 		this.collapse = collapse;
 		this.arg = arg;
 
 		if (collapse) {
 			if (new File(prefix).exists() && !new File(prefix).delete()) {
 				System.out.println("Could not delete existing prefix: "
 						+ prefix);
 				System.exit(-1);
 			}
 		} else {
 			new File(prefix).mkdirs();
 		}
 	}
 
 	protected void process(Object[][] returned) {
 		String filename = prefix;
 		if (!collapse) {
 			filename += String.format("/%06d", index++);
 		}
 
 		PrintStream output = null;
 		try {
 			output = new PrintStream(new BufferedOutputStream(
 					new FileOutputStream(new File(filename), collapse)));
 
 		} catch (IOException ee) {
 			System.err.println("Unable to open " + filename + ": "
 					+ ee.getMessage());
 		}
 		
 		Integer rows = returned.length;
 		Integer cols = returned[0].length;
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				output.print(returned[i][j]);
 				if (j < cols - 1) {
 					output.print(" ");
 				}
 			}
 			output.println();
 		}
 		output.close();		
 	}
 	@Override
 	public void collect() {
 		Object[][] returned = null;
 		if (arg != null) {
 			returned = (Object[][]) cc.get(property, arg);
 		} else {
 			returned = (Object[][]) cc.get(property);
 		}
 		assert returned != null : property;
 		process(returned);
 	}
 
 	@Override
 	public void close() {
 	}
 }
