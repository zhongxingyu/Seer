 package br.com.s1mbi0se.zorbawrapper;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 public class JsonTransformation {
 	private static boolean librariesLoaded = false;
 	private static boolean filesExtracted = false;
 	private static ZorbaWrapper zorbaWrapper;
 
 	public static void finishAll() {
 		ZorbaWrapper.disconnectAll();
 	}
 
 	private List<String> loadFileList(String filename, boolean useOriginalPath)
 			throws IOException, JsonTransformationException {
 		InputStream is = getClass().getResourceAsStream(filename);
 		if (is == null)
 			throw new JsonTransformationException("Could not load '" + filename
 					+ "' file from jar");
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		String strLine;
 		List<String> result = new ArrayList<String>();
 		// Read File Line By Line
 		while ((strLine = br.readLine()) != null) {
 			if (strLine.trim().length() > 0) {
 				if (useOriginalPath)
 					result.add(0, strLine.trim());// prepend
 				else
 					result.add(0,
 							strLine.substring(strLine.lastIndexOf("/") + 1)
 									.trim());// prepend
 			}
 		}
 
 		return result;
 	}
 
 	private String extractFile(String name) throws JsonTransformationException {
 		try {
 			System.out.println(" ==> Will extract file " + name);
 			InputStream in = getClass().getResourceAsStream(name);
 			if (in == null)
 				throw new JsonTransformationException(
 						"Could not load resource '" + name + "'");
 			File fileOut = new File(System.getProperty("java.io.tmpdir") + name);
 			OutputStream out = FileUtils.openOutputStream(fileOut);
 			IOUtils.copy(in, out);
 			in.close();
 			out.close();
 			System.out.println(" ==> File extracted to "
 					+ fileOut.getAbsolutePath());
 			System.out.println(" ============================================");
 			System.out.flush();
 			return fileOut.getAbsolutePath();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new JsonTransformationException("Could not extract file '"
 					+ name + "' ", e);
 		}
 	}
 
 	private void extractFiles() throws JsonTransformationException, IOException {
 		if (filesExtracted)
 			return;
 		List<String> files = loadFileList("/filelist", true);
 		for (String file : files) {
 			file = file.substring(1);
 			extractFile(file);
 		}
 		zorbaWrapper = new ZorbaWrapper();
 		zorbaWrapper.setLibPaths(new File(System.getProperty("java.io.tmpdir")
 				+ "/LIB_PATH/").getAbsolutePath());
 		zorbaWrapper.setUriPaths(new File(System.getProperty("java.io.tmpdir")
 				+ "/URI_PATH/").getAbsolutePath());
 		zorbaWrapper.setModulePaths(new File(System
 				.getProperty("java.io.tmpdir") + "/URI_PATH/")
 				.getAbsolutePath());
 
 		// ZorbaJavaWrapperSWIG.setUriPaths("/usr/share/zorba/uris/core/2.7.0");
 		// ZorbaJavaWrapperSWIG.setModulePaths("/usr/share/zorba/uris/core/2.7.0");
 		// ZorbaJavaWrapperSWIG.setLibPaths("/usr/lib/zorba/core/2.7.0");
 
 		filesExtracted = true;
 	}
 
 	private void loadLibraries() throws JsonTransformationException,
 			IOException {
 		if (librariesLoaded)
 			return;
 
 		List<String> libs = loadFileList("/dll_deps", false);
 		List<String> libsfp = new ArrayList<String>();
 		String fullPath = null;
 		for (String lib : libs) {
 			fullPath = extractFile("/" + lib);
 			libsfp.add(fullPath);
 
 			System.out.println(" ----> DLL " + fullPath
 					+ " extracted successfully");
 			System.out
 					.println("===============================================");
 		}
 		// System.loadLibrary("zorbawrapper");
 		// loadLib("libzorba_simplestore.so.2.7.0");
 		fullPath = extractFile("/libzorbawrapper.so");
 		libsfp.add(fullPath);
 		System.out
 				.println(" ----> DLL " + fullPath + " extracted successfully");
 		System.out.println("===============================================");
 		try {
 
 			for (String lib : libsfp) {
 				fullPath = lib;
 				System.load(fullPath);// loading goes here
 				System.out.println(" ----> DLL " + fullPath
 						+ " loaded successfully");
 				System.out
 						.println("===============================================");
 			}
 		} catch (Throwable e) {
 			e.printStackTrace();
 			throw new JsonTransformationException("Could not load file '"
 					+ fullPath + "' ", e);
 		}
 		librariesLoaded = true;
 	}
 
 	public JsonTransformation(String transformation)
 			throws JsonTransformationException, IOException {
 		loadLibraries();
 
 		extractFiles();
 		zorbaWrapper.create_transformation(transformation);
 	}
 
 	public JsonTransformation(InputStream Transformation) throws IOException,
 			JsonTransformationException {
 		this(readFile(Transformation));
 	}
 
 	public String transform(String origin) throws JsonTransformationException {
 		if (zorbaWrapper == null)
 			throw new JsonTransformationException(
 					"Instance not created, please connect first");
		return zorbaWrapper.transform_data(origin);
 	}
 
 	public String transform(InputStream origin) throws IOException,
 			JsonTransformationException {
 		return transform(readFile(origin));
 	}
 
 	public static String readFile(InputStream inputStream) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				inputStream));
 		StringBuilder fileData = new StringBuilder();
 		char[] buf = new char[1024];
 		int numRead = 0;
 		while ((numRead = reader.read(buf)) != -1) {
 			fileData.append(buf, 0, numRead);
 		}
 		reader.close();
 		return fileData.toString();
 	}
 
 }
