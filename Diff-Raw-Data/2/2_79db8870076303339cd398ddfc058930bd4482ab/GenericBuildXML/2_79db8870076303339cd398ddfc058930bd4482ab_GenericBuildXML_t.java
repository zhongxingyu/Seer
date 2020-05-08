 package testutils.utils;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.security.CodeSource;
 import java.util.Scanner;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 
 import org.apache.commons.io.IOUtils;
 
 import testutils.PTToJavaPackage;
 import testutils.exceptions.FatalErrorException;
 
 public class GenericBuildXML {
 
	private static final String GENERIC_BUILD_XML_LOCATION = "txt" + File.separator + "generic-build.xml";
 
 	private static String readGenericBuildFile(File jarPath) {
 		StringWriter writer = new StringWriter();
 		try {
 			JarFile jar = new JarFile(jarPath);
 			ZipEntry entry = jar.getEntry(GENERIC_BUILD_XML_LOCATION);
 			InputStream stream = jar.getInputStream(entry);
 			IOUtils.copy(stream, writer);
 		} catch (ZipException e) {
 			// happens when run from classfiles and not jar file
 			Scanner f;
 			try {
 				f = new Scanner(new File("resources/txt/generic-build.xml"));
 			} catch (FileNotFoundException e1) {
 				f = null;
 				throw new FatalErrorException("generic-build.xml not found.");
 			}
 			StringBuffer sb = new StringBuffer();
 			while (f.hasNextLine()) {
 				sb.append(f.nextLine() + "\n");
 			}
 			return sb.toString();
 		} catch (IOException e) {
 			throw new FatalErrorException(String.format(
 					"Couldn't read file %s from jarfile at path: %s",
 					GENERIC_BUILD_XML_LOCATION, jarPath));
 		}
 		return writer.toString();
 	}
 
 	private static File getJarPath() {
 		CodeSource codeSource = PTToJavaPackage.class.getProtectionDomain()
 				.getCodeSource();
 		String jarPath = codeSource.getLocation().getPath();
 		return new File(jarPath);
 	}
 
 	public static String generateBuildFile(String outputFolderName) {
 		File jarPath = getJarPath();
 		String genericBuildFile = readGenericBuildFile(jarPath);
 		String buildFileText = genericBuildFile.replaceFirst("PROJECTNAME",
 				outputFolderName);
 		return buildFileText;
 	}
 
 }
