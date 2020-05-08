 package genlab.igraph.natjna;
 
 import genlab.core.commons.FileUtils;
 import genlab.core.commons.ProgramException;
 import genlab.core.usermachineinteraction.GLLogger;
 import genlab.igraph.Activator;
 
 import java.io.File;
 import java.io.InputStream;
 
 import com.sun.jna.Native;
 import com.sun.jna.NativeLibrary;
 import com.sun.jna.NativeLong;
 import com.sun.jna.Pointer;
 import com.sun.jna.ptr.DoubleByReference;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.PointerByReference;
 
 /*
  * igraph_types.h
  * 
 typedef int    igraph_integer_t;
 typedef double igraph_real_t;
 typedef int    igraph_bool_t;
  */
 /**
  * 
  * For debug: VM option jna.debug_load=true .
  * Allows multi thread access. But problem of random number generator shared (or not ?) between instances.
  * On how to store a native lib: use OSGI bunder
  * http://wiki.eclipse.org/Eclipse_Plug-in_Development_FAQ#How_do_I_include_native_libraries_in_my_bundle.3F
  * http://blog.vogella.com/2010/07/27/osgi/
  * http://holistictendencies.wordpress.com/2011/03/28/bundle-nativecode-using-platform-specific-dlls-from-osgi/
  * http://wiki.apidesign.org/wiki/OSGi
  * 
  * TODO progress handler ! :-)
  * 
  * @author Samuel Thiriot
  *
  */
 public class IGraphRawLibrary {
 
 	public static Boolean isAvailable = false;
 
 	public static final String LIB_UNDECORATED_NAME = "igraph";
 	
 	
 	
 	public static final String[] LIBRARIES_LINUX = new String[] {
 		"libigraph.so"
 		};
 	public static final String[] LIBRARIES_WINDOWS_32 = new String[] {
 		"cygwin1.dll",
 		"cyggcc_s-1.dll", // needs cygwin1		
 		"cyggmp-10.dll", // needs cygwin1.dll
 		"cygstdc++-6.dll",
 		
 		"cyglzma-5.dll", 
 		"CYGICONV-2.DLL", 
 		"CYGZ.DLL",
 		
 		"cygxml2-2.dll",
 		"igraph.dll"
 		};
 	public static final String[] LIBRARIES_WINDOWS_64 = new String[] {
 
 		"cygwin1.dll",
 		
 		"cyggcc_s-seh-1.dll",
 
 		"cyggmp-10.dll", // needs cygwin1.dll
 		"cygstdc++-6.dll",
 		
 		"cyglzma-5.dll", 
 		"CYGICONV-2.DLL", 
 		"CYGZ.DLL",
 		
 		"cygxml2-2.dll",
 		"igraph.dll"
 		
 		};
 	public static final String[] LIBRARIES_MAC = new String[] {
 		"libigraph.dylib"
 		};
 
 	/**
 	 * Copy a library stored in the original path to the destination folder
 	 * @param originalPath
 	 * @param libraryName
 	 * @param destinationFolder
 	 */
 	public static void copyLibrary(String originalPath, String libraryName, File destinationFolder) {
 		
 		libraryName = libraryName.toLowerCase();
 		GLLogger.traceTech("copy of "+originalPath+"/"+libraryName+" to "+destinationFolder, IGraphRawLibrary.class);
 		InputStream originalDllStream = null;
 		if (Activator.getDefault() != null) {
 			GLLogger.traceTech("was launched with a plugin context, let's load the resource from the plugin", IGraphRawLibrary.class);
 			ClassLoader classLoader = Activator.getDefault().getClass().getClassLoader();
 			originalDllStream = classLoader.getResourceAsStream(
 					FileUtils.osPath2ressourcePath(originalPath)+libraryName
 					);
 		} else {
 			GLLogger.traceTech("was launched without a plugin context, let's load the resource from the default class loader", IGraphRawLibrary.class);
 			final String thepath = FileUtils.osPath2ressourcePath(originalPath)+libraryName;
 			try {
 				originalDllStream = IGraphRawLibrary.class.getResourceAsStream(thepath);
 				/*
 				originalDllStream = new FileInputStream(					
 						thepath
 						);
 						*/
 			} catch (RuntimeException e) {
 				final String msg = "unable to read the native library from path "+thepath;
 				GLLogger.errorTech(msg, IGraphRawLibrary.class, e);
 				throw new ProgramException(msg, e);
 			}
 
 		}
 		
 		GLLogger.traceTech("was launched with a plugin context, let's load the resource from the plugin", IGraphRawLibrary.class);
 	
 		if (originalDllStream == null) {
 			final String msg = "unable to find the DLL path: "+libraryName;
 			GLLogger.errorTech(msg, IGraphRawLibrary.class);
 			throw new ProgramException(msg);
 		}
  
 		GLLogger.traceTech("found the stream from this ressource", IGraphRawLibrary.class);
 		
 
 		// found; let's copy it somewhere
 		final File destinationFile = new File(destinationFolder, libraryName);
 		GLLogger.traceTech("will copy the library to tmp file: "+destinationFile.getAbsolutePath(), IGraphRawLibrary.class);
 		System.err.println("lib to "+destinationFile.getAbsolutePath());
 		FileUtils.copyFiles(originalDllStream, destinationFile);
 		destinationFile.deleteOnExit();
 		
 		GLLogger.traceTech("library copied to: "+destinationFile.getAbsolutePath(), IGraphRawLibrary.class);
 		
 		GLLogger.traceTech("it exists: "+(new File(destinationFile.getAbsolutePath()).exists()), IGraphRawLibrary.class);
 		GLLogger.traceTech("it can be read: "+(new File(destinationFile.getAbsolutePath()).canRead()), IGraphRawLibrary.class);
 		
 		GLLogger.traceTech("calling system.loadlibrary to increase our chances of success", IGraphRawLibrary.class);
 		try {
 			//Thread.sleep(300); // wait for the file to exist ??? 
 			System.load(destinationFile.getAbsolutePath());	
 			//System.loadLibrary(tmpLibraryUndecoratedName);
 		} catch (Throwable e) {
 			final String msg = "unable to load the native libray: "+destinationFile.getAbsolutePath();
 			GLLogger.errorTech(msg, IGraphRawLibrary.class, e);
 			throw new ProgramException(msg);
 		}
 	}
 	
 	public static void copyLibraries(String originalPath, String[] librariesNames, File destinationFolder) {
 		
 		GLLogger.traceTech("according to the OS and architecture, we are going to use the libraries stored in: "+originalPath, IGraphRawLibrary.class);
 
 		for (String libraryName: librariesNames) {
 			copyLibrary(originalPath, libraryName, destinationFolder);
 		}
 	}
 	
 	
 	static {
 		
 		try {
 			// in this block, register which native library is supposed to be directly mapped to this method
 			Native.setProtected(false);
 	
 			File tmpDirectory = FileUtils.getGenlabTmpUniqueDirectory();
 			{
 				StringBuffer sb = new StringBuffer();
 				sb.append(File.separator).append("ext").append(File.separator).append("native").append(File.separator);
 				// detect system & arch
 				final String os = System.getProperty("os.name").toLowerCase();
 				final String arch = System.getProperty("os.arch").toLowerCase();
 				
 				if (os.contains("win")) {
 					sb.append("windows").append(File.separator);
 					// windows does not informs the JVM of the good architecture, so correct it.
 					// (sure, I love windows !)
 					// see http://stackoverflow.com/questions/4748673/how-can-i-check-the-bitness-of-my-os-using-java-j2se-not-os-arch
 					final String envArch = System.getenv("PROCESSOR_ARCHITECTURE");
 					final String envArch64 = System.getenv("PROCESSOR_ARCHITEW6432");
 					if (envArch.endsWith("64") || 
 							(envArch64 != null && envArch64.endsWith("64"))
 							) {
 						sb.append("x86_64").append(File.separator);
 						copyLibraries(sb.toString(), LIBRARIES_WINDOWS_64, tmpDirectory);
 					} else { 
 						sb.append("x86").append(File.separator);
 						copyLibraries(sb.toString(), LIBRARIES_WINDOWS_32, tmpDirectory);
 					}
 					
 					//sb.append(LIB_UNDECORATED_NAME).append(".dll");
 				} else if (os.contains("nux") || os.contains("nix") || os.contains("aix")) {
 					sb.append("linux").append(File.separator);
 					if (arch.endsWith("64"))
 							sb.append("x86_64").append(File.separator);
 					else 
 						sb.append("x86").append(File.separator);
 					//sb.append("lib").append(LIB_UNDECORATED_NAME).append(".so");
 					copyLibraries(sb.toString(), LIBRARIES_LINUX, tmpDirectory);
 				} else if (os.contains("mac")) {
 					sb.append("macosx").append(File.separator);
 					//sb.append("lib").append(LIB_UNDECORATED_NAME).append(".dylib");
 					copyLibraries(sb.toString(), LIBRARIES_MAC, tmpDirectory);
 				} else {
 					throw new ProgramException("platform not supported: "+os);
 				}
 				
 			}
 			
 						
 			String tmpLibraryUndecoratedName = LIB_UNDECORATED_NAME;
 		
 			
 			GLLogger.debugTech("adding as a search path :"+tmpDirectory.getAbsolutePath()+" for library named "+tmpLibraryUndecoratedName, IGraphRawLibrary.class);
 			NativeLibrary.addSearchPath(tmpLibraryUndecoratedName, tmpDirectory.getAbsolutePath());
 			// change the JNA path
 			try{
 	    		final String previousValue = System.getProperty("jna.library.path");
 	    		StringBuffer sb = new StringBuffer();
 	    		sb.append(tmpDirectory.getAbsolutePath()).append(File.pathSeparator);
 	    		// add previous value
 	    		if (previousValue != null)
 	    			sb.append(previousValue);
 	    		// actually set the value
 	    		System.setProperty("jna.library.path", sb.toString());
 	    		GLLogger.traceTech("jna.library.path is now: "+System.getProperty("jna.library.path"), IGraphRawLibrary.class);
 			} catch(IllegalStateException ise){
 	    		GLLogger.errorTech(
 	    				"error during the initialization of the system property jna.library.path", 
 	    				IGraphRawLibrary.class
 	    				);
 	    	}
 			try{
 	    		final String previousValue = System.getProperty("java.library.path");
 	    		StringBuffer sb = new StringBuffer();
 	    		sb.append(tmpDirectory.getAbsolutePath()).append(File.pathSeparator);
 	    		// add previous value
 	    		if (previousValue != null)
 	    			sb.append(previousValue);
 	    		// actually set the value
 	    		System.setProperty("java.library.path", sb.toString());
 	    		GLLogger.traceTech("java.library.path is now: "+System.getProperty("java.library.path"), IGraphRawLibrary.class);
 			} catch(IllegalStateException ise){
 	    		GLLogger.errorTech(
 	    				"error during the initialization of the system property java.library.path", 
 	    				IGraphRawLibrary.class
 	    				);
 	    	}
 			
 			
 			GLLogger.traceTech("registering the library for JNA", IGraphRawLibrary.class);
 			Native.register(LIB_UNDECORATED_NAME);
 			
 			GLLogger.debugTech("library loaded", IGraphRawLibrary.class);
 
 			/*
 			// is osgi aware :-)
 			System.err.println("attempting to system load library "+LIB_UNDECORATED_NAME);
 		    System.loadLibrary(LIB_UNDECORATED_NAME);
 			// not osgi aware, but will found the library because of the previous call
 		    System.err.println("attempting to JNA register library "+LIB_UNDECORATED_NAME);
 			Native.register(LIB_UNDECORATED_NAME);
 			*/
 			isAvailable = true;
		} catch (RuntimeException e) {
 			// failure; but maybe it was already available ? 
 			GLLogger.errorTech("unable to initialize igraph: "+e.getMessage(), IGraphRawLibrary.class, e);
 			isAvailable = false;
 		}
 		
 	}
 	
 	
 	/*
 	 * int igraph_vector_init      (igraph_vector_t* v, int long size);
      */
 	public static native  int igraph_vector_init (InternalVectorStruct v, int size);
 
 	/*
 	 * int igraph_vector_init_copy(igraph_vector_t *v, 
 				      igraph_real_t *data, long int length);
 	 */
 	public static native  int igraph_vector_init_copy(
 			InternalVectorStruct v, 
 		    Pointer data, 
 		    int length
 		    );
 	
 	/*
 	 * void igraph_vector_destroy   (igraph_vector_t* v);
 	 */
 	public static native  void igraph_vector_destroy(InternalVectorStruct v);
 
 	
 	/*
 	 * 
      */
 	public static native  int igraph_vector_size (InternalVectorStruct v);
 
 	/*
 	 * igraph_version.h
 	 */
 	/*
 	 * 
 int igraph_version(const char **version_string,
 		   int *major,
 		   int *minor,
 		   int *subminor);
 	 */
 	public static native  int igraph_version(				
 				PointerByReference version_string,
 				IntByReference major,
 				IntByReference minor,
 				IntByReference subminor
 			  	);
 
 	/*
 	int igraph_watts_strogatz_game(igraph_t *graph, igraph_integer_t dim,
 		       igraph_integer_t size, igraph_integer_t nei,
 		       igraph_real_t p, igraph_bool_t loops, 
 		       igraph_bool_t multiple);
 	*/
 	/**
 	 * @see http://igraph.sourceforge.net/doc/html/ch09s02.html#igraph_watts_strogatz_game
 	 */
 	public static native  int igraph_watts_strogatz_game (
 			InternalGraphStruct graph,
 			int dim,
 			int size,
 			int nei,
 			double p,
 			boolean loops,
 			boolean multiple
 			);
 	
 		/**
 	 * @see http://igraph.sourceforge.net/doc/html/ch09s02.html#igraph_grg_game
 	 */
 	/*
 	int igraph_grg_game(
 			igraph_t *graph, 
 			igraph_integer_t nodes,
 		    igraph_real_t radius, 
 		    igraph_bool_t torus,
 		    igraph_vector_t *x, 
 		    igraph_vector_t *y
 		    );
 	
 */
 	public static native  int igraph_grg_game(
 			InternalGraphStruct graph, 
 			int nodes,
 		    double radius, 
 		    boolean torus,
 		    InternalVectorStruct x, 
 		    InternalVectorStruct y
 		    );
 
 	/*
 	int igraph_rewire(igraph_t *graph, igraph_integer_t n, igraph_rewiring_t mode);
 	IGRAPH_REWIRING_SIMPLE=0 
 	 */
 	public static native  int igraph_rewire(Pointer graph, int n, int mode);
 
 	
 	/*
 	 * igraph_game.h
 	 */
 /*
 int igraph_barabasi_game(igraph_t *graph, igraph_integer_t n,
 			 igraph_real_t power, 
 			 igraph_integer_t m,
 			 const igraph_vector_t *outseq,
 			 igraph_bool_t outpref,
 			 igraph_real_t A,
 			 igraph_bool_t directed,
 			 igraph_barabasi_algorithm_t algo,
 			 const igraph_t *start_from);
 int igraph_nonlinear_barabasi_game(igraph_t *graph, igraph_integer_t n,
 				   igraph_real_t power,
 				   igraph_integer_t m,  
 				   const igraph_vector_t *outseq,
 				   igraph_bool_t outpref,
 				   igraph_real_t zeroappeal,
 				   igraph_bool_t directed);
 int igraph_erdos_renyi_game(igraph_t *graph, igraph_erdos_renyi_t type,
 			    igraph_integer_t n, igraph_real_t p,
 			    igraph_bool_t directed, igraph_bool_t loops);
 int igraph_erdos_renyi_game_gnp(igraph_t *graph, igraph_integer_t n, igraph_real_t p,
 				igraph_bool_t directed, igraph_bool_t loops);
 
 int igraph_degree_sequence_game(igraph_t *graph, const igraph_vector_t *out_deg,
 				const igraph_vector_t *in_deg, 
 				igraph_degseq_t method);
 int igraph_growing_random_game(igraph_t *graph, igraph_integer_t n, 
 			       igraph_integer_t m, igraph_bool_t directed, igraph_bool_t citation);
 int igraph_barabasi_aging_game(igraph_t *graph, 
 			       igraph_integer_t nodes,
 			       igraph_integer_t m,
 			       const igraph_vector_t *outseq,
 			       igraph_bool_t outpref,
 			       igraph_real_t pa_exp,
 			       igraph_real_t aging_exp,
 			       igraph_integer_t aging_bin,
 			       igraph_real_t zero_deg_appeal,
 			       igraph_real_t zero_age_appeal,
 			       igraph_real_t deg_coef,
 			       igraph_real_t age_coef,
 			       igraph_bool_t directed);
 int igraph_recent_degree_game(igraph_t *graph, igraph_integer_t n,
 			      igraph_real_t power,
 			      igraph_integer_t window,
 			      igraph_integer_t m,  
 			      const igraph_vector_t *outseq,
 			      igraph_bool_t outpref,
 			      igraph_real_t zero_appeal,
 			      igraph_bool_t directed);
 int igraph_recent_degree_aging_game(igraph_t *graph,
 				    igraph_integer_t nodes,
 				    igraph_integer_t m, 
 				    const igraph_vector_t *outseq,
 				    igraph_bool_t outpref,
 				    igraph_real_t pa_exp,
 				    igraph_real_t aging_exp,
 				    igraph_integer_t aging_bin,
 				    igraph_integer_t window,
 				    igraph_real_t zero_appeal,
 				    igraph_bool_t directed);
 int igraph_callaway_traits_game (igraph_t *graph, igraph_integer_t nodes, 
 				 igraph_integer_t types, igraph_integer_t edges_per_step, 
 				 igraph_vector_t *type_dist,
 				 igraph_matrix_t *pref_matrix,
 				 igraph_bool_t directed);
 int igraph_establishment_game(igraph_t *graph, igraph_integer_t nodes,
 			      igraph_integer_t types, igraph_integer_t k,
 			      igraph_vector_t *type_dist,
 			      igraph_matrix_t *pref_matrix,
 			      igraph_bool_t directed);
 
 int igraph_preference_game(igraph_t *graph, igraph_integer_t nodes,
 			   igraph_integer_t types, 
 			   const igraph_vector_t *type_dist,
 			   igraph_bool_t fixed_sizes,
 			   const igraph_matrix_t *pref_matrix,
 			   igraph_vector_t *node_type_vec,
 			   igraph_bool_t directed, igraph_bool_t loops);
 int igraph_asymmetric_preference_game(igraph_t *graph, igraph_integer_t nodes,
 				      igraph_integer_t types,
 				      igraph_matrix_t *type_dist_matrix,
 				      igraph_matrix_t *pref_matrix,
 				      igraph_vector_t *node_type_in_vec,
 				      igraph_vector_t *node_type_out_vec,
 				      igraph_bool_t loops);
 
 int igraph_rewire_edges(igraph_t *graph, igraph_real_t prob, 
 			igraph_bool_t loops, igraph_bool_t multiple);
 
 int igraph_lastcit_game(igraph_t *graph, 
 			igraph_integer_t nodes, igraph_integer_t edges_per_node, 
 			igraph_integer_t agebins,
 			const igraph_vector_t *preference, igraph_bool_t directed);
 
 int igraph_cited_type_game(igraph_t *graph, igraph_integer_t nodes,
 			   const igraph_vector_t *types,
 			   const igraph_vector_t *pref,
 			   igraph_integer_t edges_per_step,
 			   igraph_bool_t directed);
 
 int igraph_citing_cited_type_game(igraph_t *graph, igraph_integer_t nodes,
 				  const igraph_vector_t *types,
 				  const igraph_matrix_t *pref,
 				  igraph_integer_t edges_per_step,
 				  igraph_bool_t directed);
 
 */
 	
 	/*
 	int igraph_forest_fire_game(igraph_t *graph, igraph_integer_t nodes,
 				    igraph_real_t fw_prob, igraph_real_t bw_factor,
 				    igraph_integer_t ambs, igraph_bool_t directed);
 	*/
 	public static native  int igraph_forest_fire_game(InternalGraphStruct graph, int nodes,
 		    double fw_prob, double bw_factor,
 		    int pambs, boolean directed);
 
 	/*
 	int igraph_simple_interconnected_islands_game(
 				igraph_t *graph, 
 				igraph_integer_t islands_n, 
 				igraph_integer_t islands_size,
 				igraph_real_t islands_pin, 
 				igraph_integer_t n_inter);
 	 */
 	public static native int igraph_simple_interconnected_islands_game(
 			InternalGraphStruct graph, 
 			int islands_n, 
 			int islands_size,
 			double islands_pin, 
 			int n_inter
 			);
 
 	/*
 	 * int igraph_simplify(igraph_t *graph, igraph_bool_t multiple, igraph_bool_t loops);
 	 */
 	public static native int igraph_simplify(
 			InternalGraphStruct graph, 
 			boolean multiple, 
 			boolean loops
 			);
 
 	/*
 int igraph_static_fitness_game(igraph_t *graph, igraph_integer_t no_of_edges,
                 igraph_vector_t* fitness_out, igraph_vector_t* fitness_in,
                 igraph_bool_t loops, igraph_bool_t multiple);
 
 int igraph_static_power_law_game(igraph_t *graph,
     igraph_integer_t no_of_nodes, igraph_integer_t no_of_edges,
     igraph_real_t exponent_out, igraph_real_t exponent_in,
     igraph_bool_t loops, igraph_bool_t multiple,
     igraph_bool_t finite_size_correction);
 
 int igraph_k_regular_game(igraph_t *graph,
     igraph_integer_t no_of_nodes, igraph_integer_t k,
     igraph_bool_t directed, igraph_bool_t multiple);
 */
 	
 	/*
 	 * igraph_interface.c
 	 */
 	
 	/*
 	 * int igraph_empty(igraph_t *graph, igraph_integer_t n, igraph_bool_t directed);
 	 */
 	/**
 	 * Creates an empty graph
 	 * @param graph
 	 * @param n
 	 * @param directed
 	 * @return
 	 */
 	public static native  int igraph_empty(InternalGraphStruct graph, int n, boolean directed);
 
 	/*
 	int igraph_erdos_renyi_game_gnp(igraph_t *graph, igraph_integer_t n, igraph_real_t p,
 			igraph_bool_t directed, igraph_bool_t loops);
 	*/
 	public static native  int igraph_erdos_renyi_game_gnp(
 			InternalGraphStruct graph, 
 			int n, 
 			double p,
 			boolean directed, 
 			boolean loops
 			);
 	public static native  int igraph_erdos_renyi_game_gnm(
 			InternalGraphStruct graph, 
 			int n, 
 			double m,
 			boolean directed, 
 			boolean loops
 			);
 	
 	/**
 	 * maps the enum from Igraph to something else
 	 * 
 	 * typedef enum { IGRAPH_BARABASI_BAG = 0,
 	       IGRAPH_BARABASI_PSUMTREE, 
 	       IGRAPH_BARABASI_PSUMTREE_MULTIPLE} igraph_barabasi_algorithm_t;
 
 	 * @author Samuel Thiriot
 	 *
 	 */
 	public enum IGraphBarabasiAlgorithm {
 		IGRAPH_BARABASI_BAG,
 		IGRAPH_BARABASI_PSUMTREE, 
 		IGRAPH_BARABASI_PSUMTREE_MULTIPLE;
 		
 	}
 
 	/*
 	 * int igraph_barabasi_game(igraph_t *graph, igraph_integer_t n,
 			 igraph_real_t power, 
 			 igraph_integer_t m,
 			 const igraph_vector_t *outseq,
 			 igraph_bool_t outpref,
 			 igraph_real_t A,
 			 igraph_bool_t directed,
 			 igraph_barabasi_algorithm_t algo,
 			 const igraph_t *start_from);
 	 */
 	public static native int igraph_barabasi_game(
 			InternalGraphStruct graph, 
 			int n,
 		    double power, 
 		    int m,
 		    PointerByReference outseq,
 		    boolean outpref,
 		    double A,
 		    boolean directed,
 		    int algo,
 		    InternalGraphStruct start_from
 		    );
 
 	/*
 	 * int igraph_lcf_vector(igraph_t *graph, igraph_integer_t n,
 		      const igraph_vector_t *shifts, 
 		      igraph_integer_t repeats);
 	 */
 	public static native  int igraph_lcf_vector(
 			InternalGraphStruct graph, 
 			int n,
 		    InternalVectorStruct shifts, 
 		    int repeats
 		    );
 	
 	/*
 	 * igraph_integer_t igraph_vcount(const igraph_t *graph);
 	 */
 	/**
 	 * Returns the vertex count
 	 * @param graph
 	 * @return
 	 */
 	public static native  int igraph_vcount(Pointer graph);
 	
 	 
 	/*
 	 * igraph_integer_t igraph_ecount(const igraph_t *graph);
 	 */
 	/**
 	 * Returns the edge count
 	 * @param graph
 	 * @return
 	 */
 	public static native  int igraph_ecount(Pointer graph);
 		
 	/*
 	 * igraph_bool_t igraph_is_directed(const igraph_t *graph);
 	 */
 	public static native  boolean igraph_is_directed(Pointer graph);
 
 	/*
 	 * int igraph_copy(igraph_t *to, const igraph_t *from);
 	 */
 	public static native  int igraph_copy(InternalGraphStruct to, InternalGraphStruct from);
 
 
 	/*
 	 * int igraph_destroy(igraph_t *graph);
 	 */
 	public static native  int igraph_destroy(Pointer graph);
 
 	public static native  int igraph_add_vertices(Pointer graph, int nv, Pointer attr);
 
 	/*
 	 * int igraph_add_edge(igraph_t *graph, igraph_integer_t from, igraph_integer_t to);
 	 */
 	public static native  int igraph_add_edge(Pointer graph, int from, int to);
 	
 	public static native  int igraph_add_edges(Pointer graph, InternalVectorStruct edges, Pointer attr);
 
 	/*
 	 * int igraph_edge(const igraph_t *graph, igraph_integer_t eid, 
 		igraph_integer_t *from, igraph_integer_t *to);
 	 */
 	public static native  int igraph_edge(Pointer graph, int eid, 
 			IntByReference from, IntByReference to);
 	
 	/*
 	 * int igraph_average_path_length(const igraph_t *graph, igraph_real_t *res,
 			       igraph_bool_t directed, igraph_bool_t unconn);
 	 */
 	public static native  int igraph_average_path_length(Pointer graph, DoubleByReference res,
 		       boolean directed, boolean unconn);
 
 	/*
 	 * int igraph_diameter(const igraph_t *graph, igraph_integer_t *pres, 
 		    igraph_integer_t *pfrom, igraph_integer_t *pto, 
 		    igraph_vector_t *path,
 		    igraph_bool_t directed, igraph_bool_t unconn);
 	 */
 	public static native  int igraph_diameter(Pointer graph, IntByReference pres, 
 			IntByReference pfrom,  IntByReference pto, 
 		    PointerByReference path,
 		    boolean directed, boolean unconn);
 	
 	/*
 	 * int igraph_is_connected(const igraph_t *graph, igraph_bool_t *res, 
 			igraph_connectedness_t mode);
 	 * mode: IGRAPH_WEAK=1, IGRAPH_STRONG=2 (ignored for undirected)
 	 */
 	public static native  int igraph_is_connected(Pointer graph, IntByReference res, 
 			int mode);
 	
 	/*
 	 * int igraph_clusters(const igraph_t *graph, igraph_vector_t *membership, 
 		    igraph_vector_t *csize, igraph_integer_t *no,
 		    igraph_connectedness_t mode);
 		    mode: IGRAPH_WEAK=1, IGRAPH_STRONG=2 (ignored for undirected)
 	 */
 	public static native  int igraph_clusters(Pointer graph, InternalVectorStruct membership, 
 			InternalVectorStruct csize, IntByReference no,
 		    int mode);
 	
 	
 	/*
 	 * int igraph_vs_none(igraph_vs_t *vs);
 	 */
 	public static native int igraph_vs_none(Pointer vs);
 	
 	/*
 	 * igraph_vs_t igraph_vss_all(void);
 	 */
 	public static native Pointer igraph_vss_all();
 
 	/*
 	 * int igraph_vs_all(igraph_vs_t *vs);
 	 */
 	public static native int igraph_vss_all(Pointer vs);
 
 	/*
 	 * igraph_vs_t igraph_vss_none(void);
 	 */
 	public static native Pointer igraph_vss_none();
 
 	
 	/*
 	 * void igraph_vs_destroy(igraph_vs_t *vs);
 	 */
 	public static native void igraph_vs_destroy(Pointer vs);
 	
 	/* TODO not stable :-(
 	 * int igraph_betweenness_estimate(const igraph_t *graph, igraph_vector_t *res, 
 				const igraph_vs_t vids, igraph_bool_t directed,
 				igraph_real_t cutoff, 
 				const igraph_vector_t *weights, 
 				igraph_bool_t nobigint);
 	 */
 	public static native int igraph_betweenness_estimate(
 			Pointer graph, 
 			InternalVectorStruct res, 
 			Pointer vids, 
 			boolean directed,
 			double cutoff, 
 			InternalVectorStruct weights, 
 			boolean nobigint
 			);
 	
 	/* TODO not stable
 	 * int igraph_betweenness(const igraph_t *graph, igraph_vector_t *res, 
                        const igraph_vs_t vids, igraph_bool_t directed,
 		       const igraph_vector_t *weights, igraph_bool_t nobigint);
 	 */
 	public static native int igraph_betweenness(
 			Pointer graph, 
 			InternalVectorStruct res,  
 			InternalVertexSelector vids, 
             boolean directed,
             InternalVectorStruct weights, 
             boolean nobigint
             );
 	
 	/*
 	 * int igraph_edge_betweenness_estimate(const igraph_t *graph, igraph_vector_t *result,
 				     igraph_bool_t directed, igraph_real_t cutoff,
 				     const igraph_vector_t *weights);
 	 */
 	public static native int igraph_edge_betweenness_estimate(
 			Pointer graph, 
 			InternalVectorStruct result,
 		    boolean directed, 
 		    double cutoff,
 		    InternalVectorStruct weights
 		    );
 	
 	/*
 	 * int igraph_edge_betweenness(const igraph_t *graph, igraph_vector_t *result,
                             igraph_bool_t directed, 
 			    const igraph_vector_t *weights);
 	 */
 	public static native int igraph_edge_betweenness(
 			Pointer graph, 
 			InternalVectorStruct result,
             boolean directed,
             InternalVectorStruct weights
             );
 	
 	/*
 	 * int igraph_transitivity_undirected(const igraph_t *graph,
 				   igraph_real_t *res,
 				   igraph_transitivity_mode_t mode);
 				   
 				   mode: Defines how to treat graphs with no connected triples. IGRAPH_TRANSITIVITY_NAN=0 returns NaN in this case, IGRAPH_TRANSITIVITY_ZERO=1 returns zero.
 	 */
 	public static native  int igraph_transitivity_undirected(Pointer graph, DoubleByReference res, int mode);
 	
 	/*
 	 * int igraph_isomorphic(const igraph_t *graph1, const igraph_t *graph2,
 		      igraph_bool_t *iso);
 
 	 */
 	public static native  int igraph_isomorphic(Pointer graph1, Pointer graph2,
 		      IntByReference iso);
 
 	/*
 	 * int igraph_count_isomorphisms_vf2(const igraph_t *graph1, const igraph_t *graph2, 
 				  const igraph_vector_int_t *vertex_color1,
 				  const igraph_vector_int_t *vertex_color2,
 				  const igraph_vector_int_t *edge_color1,
 				  const igraph_vector_int_t *edge_color2,
 				  igraph_integer_t *count,
 				  igraph_isocompat_t *node_compat_fn,
 				  igraph_isocompat_t *edge_compat_fn,
 				  void *arg);
 
 	 */
 	public static native  int igraph_count_isomorphisms_vf2(Pointer graph1, Pointer graph2, 
 			  InternalVectorStruct vertex_color1,
 			  InternalVectorStruct vertex_color2,
 			  InternalVectorStruct edge_color1,
 			  InternalVectorStruct edge_color2,
 			  IntByReference count,
 			  Pointer node_compat_fn,
 			  Pointer edge_compat_fn,
 			  Pointer arg
 			  );
 	/*
 	 * int igraph_isomorphic_vf2(const igraph_t *graph1, const igraph_t *graph2, 
 			  const igraph_vector_int_t *vertex_color1,
 			  const igraph_vector_int_t *vertex_color2,
 			  const igraph_vector_int_t *edge_color1,
 			  const igraph_vector_int_t *edge_color2,
 			  igraph_bool_t *iso, igraph_vector_t *map12, 
 			  igraph_vector_t *map21,
 			  igraph_isocompat_t *node_compat_fn,
 			  igraph_isocompat_t *edge_compat_fn,
 			  void *arg);
 	 */
 	public static native  int igraph_isomorphic_vf2(Pointer graph1, Pointer graph2, 
 			  InternalVectorStruct vertex_color1,
 			  InternalVectorStruct vertex_color2,
 			  InternalVectorStruct edge_color1,
 			  InternalVectorStruct edge_color2,
 			  IntByReference iso, 
 			  InternalVectorStruct map12, 
 			  InternalVectorStruct map21,
 			  Pointer node_compat_fn,
 			  Pointer edge_compat_fn,
 			  Pointer arg
 			  );
 	
 	/*
 	public static native  int int igraph_transitivity_local_undirected(const igraph_t *graph,
 			 igraph_vector_t *res,
 			 const igraph_vs_t vids,
 			 igraph_transitivity_mode_t mode);
 			 TODO
 	*/
 	
 	/*
 	int igraph_transitivity_avglocal_undirected(const igraph_t *graph,
 		    igraph_real_t *res,
 		    igraph_transitivity_mode_t mode);
 	*/
 	public static native  int igraph_transitivity_avglocal_undirected(Pointer graph,
 			DoubleByReference res,
 		    int mode);
 	
 	// randomness
 	
 	/*
 	 * igraph_rng_t *igraph_rng_default(void);
 	 */
 	/**
 	 * Returns a pointer to the default igraph random number generator.
 	 * Nota: always set seed BEFORE actual use.
 	 * @return
 	 */
 	public static native Pointer igraph_rng_default();
 	
 	/*
 	 * const char *igraph_rng_name(igraph_rng_t *rng);
 	 */
 	/**
 	 * Returns the name of a random number generator
 	 * @param rng
 	 * @return
 	 */
 	public static native String igraph_rng_name(Pointer rng);
 	
 	
 	/*
 	 * int igraph_rng_seed(igraph_rng_t *rng, unsigned long int seed);
 	 */
 	/**
 	 * Sets the seed of the random number generator passed as parameter
 	 * @param rng
 	 * @param seed
 	 * @return
 	 */
 	public static native int igraph_rng_seed(Pointer rng, NativeLong seed);
 
 	/*
 	 * long int igraph_rng_get_integer(igraph_rng_t *rng,
 				long int l, long int h);
 	 */
 	/**
 	 * Gets an integer from the random number generator passed as parameter. Note that both l and h are inclusive.
 	 * Also note that h has to be >= 1
 	 * @param rng
 	 * @param l
 	 * @param h
 	 * @return
 	 */
 	public static native NativeLong igraph_rng_get_integer(Pointer rng, NativeLong l, NativeLong h);
 	
 	/*
 	 * igraph_real_t igraph_rng_get_unif(igraph_rng_t *rng, 
 				  igraph_real_t l, igraph_real_t h);
 	 */
 	/**
 	 * returns a double in a uniform rand
 	 * @param rng
 	 * @param l
 	 * @param h
 	 * @return
 	 */
 	public static native double igraph_rng_get_unif(Pointer rng, double l, double h);
 	
 
 	/*
 	 * igraph_progress_handler_t * igraph_set_progress_handler(igraph_progress_handler_t new_handler);
 	 */
 	public static native Pointer igraph_set_progress_handler(IIGraphProgressCallback new_handler);
 	
 
 }
