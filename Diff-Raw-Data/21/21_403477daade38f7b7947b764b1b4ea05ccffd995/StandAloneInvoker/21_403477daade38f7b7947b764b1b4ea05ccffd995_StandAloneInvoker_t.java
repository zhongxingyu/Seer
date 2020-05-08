 package org.magnolialang.rascal;
 
 public class StandAloneInvoker {
 
 	public static RascalInterpreter getInterpreter() {
 		String rascalPath = System.getenv("FRAGMENT");
 		String metaxaPath = System.getenv("METAXA");
 		if(rascalPath == null)
 			rascalPath = "../../eclipse/plugins/rascal_fragment_linux_0.1.6/";
 		if(metaxaPath == null)
 			metaxaPath = "..";
 
 		System.setProperty("rascal.parsetable.default.file", rascalPath
 				+ "/installed/share/rascal-grammar/rascal.tbl");
 		System.setProperty("rascal.parsetable.header.file", rascalPath
 				+ "/installed/share/rascal-grammar/rascal-header.tbl");
 		System.setProperty("rascal.rascal2table.command", rascalPath
 				+ "/installed/bin/rascal2table");
 		System.setProperty("rascal.rascal2table.dir", rascalPath
 				+ "/installed/bin");
 		System.setProperty("rascal.sdf.library.dir", rascalPath
 				+ "/installed/share/sdf-library/library");
 		System.setProperty("rascal.parsetable.cache.dir",
 				System.getProperty("java.io.tmpdir"));
 		System.out.println(metaxaPath);
 		System.setProperty(
 				"rascal.path",
 				new java.io.File("src").getAbsolutePath()
 						+ ":"
 						+ new java.io.File(
 								"../../rascal/src/org/rascalmpl/library")
 								.getAbsolutePath()
 						+ ":"
						+ new java.io.File(
								"../../rascal-eclipse/src/org/rascalmpl/eclipse/library")
								.getAbsolutePath()
						+ ":"
 						+ new java.io.File(metaxaPath + "/src")
 								.getAbsolutePath());
 		System.out.println(System.getProperty("rascal.path"));
 
 		return RascalInterpreter.getInstance();
 	}
 
 	private StandAloneInvoker() {
 
 	}
 }
