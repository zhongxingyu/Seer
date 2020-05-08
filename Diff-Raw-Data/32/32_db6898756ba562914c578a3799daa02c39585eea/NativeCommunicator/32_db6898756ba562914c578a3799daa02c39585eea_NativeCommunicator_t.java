 package nl.tue.fingerpaint.server.simulator;
 
 import java.io.File;
 
 import nl.tue.fingerpaint.shared.simulator.SimulatorService;
 
 /**
  * Class that is able to communicate with the native simulation-service code.
  * Use this class only indirectly through {@link SimulatorService}
  * 
  * @author Group Fingerpaint
  */
 class NativeCommunicator {
 	
 	private static NativeCommunicator instance = new NativeCommunicator();
 	
 	private static String matrixDir = new File(new File(NativeCommunicator.class.getProtectionDomain()
 			.getCodeSource().getLocation().getPath()).getParent() + File.separator + "matrices" + File.separator).getPath().replace("%20", " ");
 	
 	static {
 		//System.load("C:\\NativeCommunicator.dll");
 		System.load(new File(new File(NativeCommunicator.class.getProtectionDomain()
 					.getCodeSource().getLocation().getPath())
 				.getParent() + File.separator + "lib" + File.separator + System.mapLibraryName("NativeCommunicator")).getPath().replace("%20", " "));
     }
 	
 //    /**
 //     * Loads library from current JAR archive
 //     * 
 //     * The file from JAR is copied into system temporary directory and then 
 //     * loaded. The temporary file is deleted after exiting. Method uses String 
 //     * as filename because the pathname is "abstract", not system-dependent.
 //     * 
 //     * @param path The path to the file inside jar
 //     * @param filename The filename inside JAR, without the extension (this is
 //     * 			added specific to the operating system)
 //     * @throws IOException If temporary file creation or read/write operation fails
 //     * @throws IllegalArgumentException If source file (param path) does not exist
 //     * @throws IllegalArgumentException If the path is not absolute or if the 
 //     * 			filename is shorter than three characters (restriction of 
 //     * 			{@link File#createTempFile(java.lang.String, java.lang.String)}).
 //     */
 //    public static void loadLibraryFromJar(String path, String filename) throws IOException {
 // 
 //        if (!path.startsWith("/")) {
 //            throw new IllegalArgumentException(
 //            		"The path to be absolute (start with '/').");
 //        }
 // 
 //        // Check if the filename is okay
 //        if (filename == null || filename.length() < 3) {
 //            throw new IllegalArgumentException(
 //            		"The filename has to be at least 3 characters long.");
 //        }
 //        
 //        String suffix = System.getProperty("os.name").startsWith("Windows") ? ".dll" : ".so";
 // 
 //        // Prepare temporary file
 //        File temp = File.createTempFile(filename, suffix);
 //        temp.deleteOnExit();
 // 
 //        if (!temp.exists()) {
 //            throw new FileNotFoundException(
 //            		"File " + temp.getAbsolutePath() + " does not exist.");
 //        }
 // 
 //        // Prepare buffer for data copying
 //        byte[] buffer = new byte[1024];
 //        int readBytes;
 // 
 //        // Open output stream and copy data between source file in JAR and the temporary file
 //        try (
 //        		OutputStream os = new FileOutputStream(temp);
 //        		InputStream is = NativeCommunicator.class.getResourceAsStream(
 //        				path + filename + suffix)) {
 //        	if (is == null) {
 //                throw new FileNotFoundException(
 //                		"File " + path + " was not found inside JAR.");
 //            }
 //            while ((readBytes = is.read(buffer)) != -1) {
 //                os.write(buffer, 0, readBytes);
 //            }
 //        }
 // 
 //        // Finally, load the library
 //        System.load(temp.getAbsolutePath());
 //    }
     
 //    private static void initializeNativeCode() throws IOException {
 //    	final Path fromPath = Paths.get(NativeCommunicator.class.getResource("/war/WEB-INF/matrices/").getPath());
 //	    final Path toPath = Files.createTempDirectory("native");
 //	    toPath.toFile().deleteOnExit();
 //    	final StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
 //    	
 //    	Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {
 //            @Override
 //            
 //            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
 //            	Path targetPath = toPath.resolve(fromPath.relativize(dir));
 //		        if(!Files.exists(targetPath)){
 //		            Files.createDirectory(targetPath);
 //		        }
 //                return FileVisitResult.CONTINUE;
 //            }
 // 
 //            @Override
 //            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
 //            	Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
 //                return FileVisitResult.CONTINUE;
 //            }
 //        });
 //    	
 //    	String suffix = System.getProperty("os.name").startsWith("Windows") ? ".dll" : ".so";
 //    	
 //    	Path lib = Paths.get(NativeCommunicator.class.getResource(
 //				"\\war\\WEB-INF\\lib\\NativeCommunicator" + suffix).getPath());
 //    	Path copiedLib = Files.copy(lib, toPath.resolve(fromPath.relativize(lib)), copyOption);
 //    	
 //    	System.load(copiedLib.toFile().getAbsolutePath());
 //    }
     
 //    private static FileSystem getJarFileSystem(String resource)
 //    		throws IOException, URISyntaxException {
 //
 //    	System.out.println(NativeCommunicator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
 //    	
 //    		    FileSystem fs = FileSystems.newFileSystem(
 //    		    		NativeCommunicator.class.getProtectionDomain().getCodeSource().getLocation().toURI(),
 //    		    		Collections.<String, Object>emptyMap());
 //    		    return fs;
 //    		}
 	
 	private NativeCommunicator() {
 		
 	}
 	
 	/**
 	 * Return the singleton instance of this class.
 	 * 
 	 * @return singleton instance of the {@link NativeCommunicator}
 	 */
 	public static NativeCommunicator getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Communicates with the native simulation-service code to simulate a
 	 * step on a given concentration vector. The given vector is modified
 	 * with the result.
 	 * 
 	 * @param geometry The geometry used for the simulation
 	 * @param mixer The mixer used for the simulation
 	 * @param concentrationVector
 	 * 				The concentration vector that used for the simulation;
 	 * 				note that results are returned in this parameter
 	 * @param stepSize The size of the step that is performed
 	 * @param stepName The name of the step to simulate
 	 * @return The segregation of the simulation
 	 */
 	public synchronized double simulate(String geometry, 
 			String mixer, 
 			double[] concentrationVector, 
 			double stepSize, 
 			String stepName) {
 		return simulate(
 				geometry, mixer, concentrationVector, 
 				stepSize, stepName, matrixDir);
 	}
 	
 	/**
 	 * Communicates with the native simulation-service code to simulate a
 	 * step on a given concentration vector. The given vector is modified
 	 * with the result.
 	 * 
 	 * @param geometry The geometry used for the simulation
 	 * @param mixer The mixer used for the simulation
 	 * @param concentrationVector
 	 * 				The concentration vector that used for the simulation;
 	 * 				note that results are returned in this parameter
 	 * @param stepSize The size of the step that is performed
 	 * @param stepName The name of the step to simulate
 	 * @return The segregation of the simulation
 	 */
 	private native synchronized double simulate(String geometry, 
 								String mixer, 
 								double[] concentrationVector, 
 								double stepSize, 
 								String stepName,
 								String dir);
 }
