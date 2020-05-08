 package se.chalmers.tda367.std.utilities;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Path;
 import java.util.logging.Logger;
 
 /**
  * A class used for loading java classes from files on the hard drive.
  * @author Emil Edholm
  * @date   Apr 22, 2012
  */
 public final class ExtendedClassLoader extends ClassLoader {
 	private final String intendedPackage;
 	private final Path containingFolder;
 	
 	/**
 	 * The extended class loader. Used for retrieving class files from the hard drive.
 	 * @param intendedPackage the package of the files about the be read.
 	 * @param containingFolder the path to the 
 	 */
 	public ExtendedClassLoader(String intendedPackage, Path containingFolder) {
 		super();
 		this.intendedPackage = intendedPackage;
 		this.containingFolder = containingFolder;
 	}
 	
 	@Override
 	public Class<?> findClass (String binaryName) {
 		byte[] classData = loadClassData (containingFolder, binaryName);
 		
 		// This is where the actual conversion between a heap of bytes to an actual class.
 		return defineClass(intendedPackage + "." + binaryName, classData, 0, classData.length);
 	}
 
 	/**
 	 * Loads the data of a specified file and path.
 	 * @param searchPath the path to the folder where {@code className} resides in
 	 * @param className the name of the class to try to load (without the extension)
 	 * @return a byte array of the read class file. Empty if file not found, etc.
 	 */
 	private byte[] loadClassData (Path searchPath, String className) {
		File f = searchPath.resolve(className + ".class").toFile();
 		if(!f.isFile()){
 			Logger.getLogger("se.chalmers.tda367.std.utilities").severe("Unable to load the class data from file: " + f.toString());
 			return new byte[0];
 		}
 		
 		ByteArrayOutputStream classBuffer = new ByteArrayOutputStream();
 		try {
 			InputStream stream = new FileInputStream(f);
 			int readByte = 0;
 			while((readByte = stream.read()) != -1){
 				classBuffer.write(readByte);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return classBuffer.toByteArray();
 	}
 }
