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
 	private int instance;
 	private static boolean librariesLoaded = false;
 
 	private List<String> loadLibList() throws IOException,
 			JsonTransformationException {
 		InputStream is = getClass().getResourceAsStream("/dll_deps");
 		if (is == null)
 			throw new JsonTransformationException(
 					"Could not load dll_deps file from jar");
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		String strLine;
 		List<String> result = new ArrayList<String>();
 		// Read File Line By Line
 		while ((strLine = br.readLine()) != null) {
 			if (strLine.trim().length() > 0)
 				result.add(0, strLine.trim());// prepend
 		}
 		return result;
 	}
 
 	private void loadLib(String name) throws JsonTransformationException {
 		try {
 			System.out.println(" ==> Will extract DLL " + name);
 			InputStream in = getClass().getResourceAsStream("/" + name);
 			if (in == null)
 				throw new JsonTransformationException(
 						"Could not load resource '" + name + "'");
 			File fileOut = new File(System.getProperty("java.io.tmpdir") + "/"
 					+ name);
 			OutputStream out = FileUtils.openOutputStream(fileOut);
 			IOUtils.copy(in, out);
 			in.close();
 			out.close();
 			System.out.println(" ==> DLL extracted to "
 					+ fileOut.getAbsolutePath());
 			System.load(fileOut.toString());// loading goes here
 			System.out.println(" ----> DLL " + name + " loaded successfully");
 			System.out.println(" ============================================");
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new JsonTransformationException("Could not load library '"
 					+ name + "' ", e);
 		}
 	}
 
 	private void loadLibraries() throws JsonTransformationException,
 			IOException {
 		if (librariesLoaded)
 			return;
 		List<String> libs = loadLibList();
 		for (String lib : libs) {
 			lib = lib.substring(lib.lastIndexOf("/") + 1).trim();
 			loadLib(lib);
 		}
 		// System.loadLibrary("zorbawrapper");
 		// loadLib("libzorba_simplestore.so.2.7.0");
 		loadLib("libzorbawrapper.so");
 		librariesLoaded = true;
 	}
 
 	public JsonTransformation(String transformation)
 			throws JsonTransformationException, IOException {
 		loadLibraries();
 		instance = ZorbaJavaWrapperSWIG.create_transformation(transformation);
 	}
 
 	public JsonTransformation(InputStream Transformation) throws IOException,
 			JsonTransformationException {
 		this(readFile(Transformation));
 	}
 
 	public void destroy() {
 		ZorbaJavaWrapperSWIG.disconnect(instance);
 	}
 
 	public String transform(String origin) throws JsonTransformationException {
 		if (instance < 0)
 			throw new JsonTransformationException(
 					"Instance not created, please connect first");
 		return ZorbaJavaWrapperSWIG.transform_data(instance, origin);
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
