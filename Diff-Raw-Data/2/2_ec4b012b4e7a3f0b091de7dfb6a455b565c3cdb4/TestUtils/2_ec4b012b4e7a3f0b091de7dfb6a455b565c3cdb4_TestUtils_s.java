 package utils;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 public class TestUtils {
 	private static final String PATH = "test/";
 
 	public static InputStream getInputStream(String filename, Class<? extends Object> clazz)
 			throws FileNotFoundException {
 		return getInputStream(PATH + clazz.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar
 				+ filename);
 	}
 
 	public static InputStream getInputStream(String path) throws FileNotFoundException {
 		return new FileInputStream(path);
 	}
 
 	public static List<String> getFileContent(String filename, Class<? extends Object> clazz)
 			throws FileNotFoundException {
 		List<String> res = new ArrayList<String>();
 
 		// get the InputStream of the file
 		Scanner in = null;
 		try {
 			in = new Scanner(getInputStream(filename, clazz));
 			// now read all the line of the file and return them in a list
 			while (in.hasNextLine())
 				res.add(in.nextLine());
 		} finally {
 			if (in != null)
 				in.close();
 		}
 
 		return res;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void executeTest(String inFile, String outFile, Class<? extends Object> testClazz,
 			Class<? extends Object> clazzToTest) throws Exception {
 		InputStream isIn = TestUtils.getInputStream(inFile, testClazz);
 
 		// execute the test by invoking the doIt method in the clazzToTest
 		Method doIt = clazzToTest.getMethod("doIt", InputStream.class);
 		Object res = doIt.invoke(clazzToTest.newInstance(), isIn);
 
 		// now we compare the calculated results with the expected
 		assertResult(outFile, (List<? extends Object>) res, testClazz);
 	}
 
 	public static void assertResult(String filename, List<? extends Object> res, Class<? extends Object> testClazz)
 			throws FileNotFoundException {
 		List<String> expectedRes = TestUtils.getFileContent(filename, testClazz);
 
 		assertEquals("Should be the same number of results!", expectedRes.size(), res.size());
 		for (int i = 0; i < res.size(); i++)
			assertEquals("Result in line " + i + " are not the same!", expectedRes.get(i), res.get(i).toString());
 	}
 }
