 package de.tum.in.cindy3dplugin.jogl;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import java.security.CodeSource;
 import java.security.ProtectionDomain;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import org.apache.commons.math.geometry.Vector3D;
 import org.apache.commons.math.linear.RealMatrix;
 
 import com.jogamp.common.jvm.JNILibLoaderBase;
 import com.jogamp.common.jvm.JNILibLoaderBase.LoaderAction;
 import com.jogamp.gluegen.runtime.NativeLibLoader;
 import com.jogamp.opengl.util.glsl.ShaderCode;
 
 public class Util {
 	private static final String SHADER_PATH = "/de/tum/in/cindy3dplugin/resources/shader/";
 	private static final boolean FILE_LOGGING = false;
 	
 	public static float[] matrixToFloatArray(RealMatrix m) {
 		int rows = m.getRowDimension();
 		int cols = m.getColumnDimension();
 		
 		float[] result = new float[rows*cols];
 		double[][] data = m.getData();
 		int offset = 0;
 		for (int row = 0; row < rows; ++row) {
 			for (int col = 0; col < cols; ++col, ++offset) {
 				result[offset] = (float) data[row][col];
 			}
 		}
 		
 		return result;
 	}
 
 	public static float[] matrixToFloatArrayTransposed(RealMatrix m) {
 		int rows = m.getRowDimension();
 		int cols = m.getColumnDimension();
 		
 		float[] result = new float[rows*cols];
 		double[][] data = m.getData();
 		int offset = 0;
 		for (int row = 0; row < rows; ++row) {
 			for (int col = 0; col < cols; ++col, ++offset) {
 				result[offset] = (float) data[col][row];
 			}
 		}
 		
 		return result;
 	}
 	
 	public static double[] vectorToDoubleArray(Vector3D v) {
 		return new double[] {v.getX(), v.getY(), v.getZ()};
 	}
 	
 	private static String shaderLightFillIn = "";
 		
 	public static void readShaderSource(ClassLoader context, URL url,
 			StringBuffer result) {
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					url.openStream()));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				if (line.startsWith("#pragma include ")) {
 					String includeFile = line.substring(16).trim();
 					// Try relative path first
 					URL nextURL = null;
 					try {
 						nextURL = new URL(url, includeFile);
 					} catch (MalformedURLException e) {
 					}
 					if (nextURL == null) {
 						// Try absolute path
 						try {
 							nextURL = new URL(includeFile);
 						} catch (MalformedURLException e) {
 						}
 					}
 					if (nextURL == null) {
 						// Fail
 						throw new FileNotFoundException(
 								"Can't find include file " + includeFile);
 					}
 					readShaderSource(context, nextURL, result);
 				} else if (line.startsWith("#pragma lights")) {
 					result.append(shaderLightFillIn + "\n");
 				} else {
 					result.append(line + "\n");
 				}
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	public static ShaderCode loadShader(int type, String name) {
 		StringBuffer buffer = new StringBuffer();
 		URL url = Util.class.getResource(SHADER_PATH + name);
 		readShaderSource(Util.class.getClassLoader(), url, buffer);
 		ShaderCode shader = new ShaderCode(type, 1,
 				new String[][] { { buffer.toString() } });
 		return shader;
 	}
 	
 	public static Color toColor(double[] vec) {
 
 		if (vec.length != 3) {
 			return null;
 		}
 		return new Color(
 				(float)Math.max(0, Math.min(1, vec[0])),
 				(float)Math.max(0, Math.min(1, vec[1])),
 				(float)Math.max(0, Math.min(1, vec[2])));
 	}
 	
 	public static Vector3D toVector(double[] vec) {
 		if (vec.length != 3) {
 			return null;
 		}
 		return new Vector3D(vec[0], vec[1], vec[2]);
 	}
 	
 	public static Color toColor(ArrayList<Double> vec) {
 		if (vec.size() != 3) {
 			return null;
 		}
 		return new Color(
 				(float)Math.max(0, Math.min(1, vec.get(0))),
 				(float)Math.max(0, Math.min(1, vec.get(1))),
 				(float)Math.max(0, Math.min(1, vec.get(2))));
 	}
 
 	public static void setShaderLightFillIn(String shaderLightFillIn) {
 		Util.shaderLightFillIn = shaderLightFillIn;
 	}
 
 	/**
 	 * Modifies gluegen's class loading to load native libraries from the
 	 * current JAR's directory.  
 	 */
 	public static void setupGluegenClassLoading() {
 		// Try to get JAR path
 		
 		String jarPath = null;
 		
 		URL jarURL = null;
 		try {
 			ProtectionDomain pd = Util.class.getProtectionDomain();
 			CodeSource cs = pd.getCodeSource();
 			jarURL = cs.getLocation();
 			//jarPath = jarURL.toURI().getPath();
 			jarPath = jarURL.getPath();
 		} /*catch (URISyntaxException e) {
 			
 			logger.info(jarURL.toString());
 			logger.info("Hallo2");
 			e.printStackTrace();
 			return;
 		}*/ catch (SecurityException e) {
 			// Can't get protection domain. This is the case if Cindy3D is
 			// running inside an applet. But that's ok, as JNLP handles the
 			// class and native libraries loading for us.
 			return;
 		}
 		
 		File jarFile = new File(jarPath);
 		if (!jarFile.isFile()) {
 			// Not loaded from JAR file, do nothing
 			return;
 		}
 		final String basePath = jarFile.getParent();
 
 		// Prevent gluegen from trying to load native library via
 		// System.loadLibrary("gluegen-rt").
 		NativeLibLoader.disableLoading();
 
 		// Instead, (try to) load it ourselves from JAR directory
 		String path = basePath + File.separator
 				+ System.mapLibraryName("gluegen-rt");
 		System.load(path);
 
 		// Next, override the gluegen JNI library loader action
 		JNILibLoaderBase.setLoadingAction(new LoaderAction() {
 			@Override
 			public void loadLibrary(String libname, String[] preload,
 					boolean preloadIgnoreError) {
 				if (preload != null) {
 					for (String preloadLibname : preload) {
 						loadLibrary(preloadLibname, preloadIgnoreError);
 					}
 				}
 				loadLibrary(libname, false);
 			}
 
 			@Override
 			public boolean loadLibrary(String libname, boolean ignoreError) {
 				boolean result = true;
 				try {
 					// Load JNI library from JAR directory
 					String path = basePath + File.separator
 							+ System.mapLibraryName(libname);
 					System.load(path);
 				} catch (UnsatisfiedLinkError e) {
 					result = false;
 					if (!ignoreError) {
 						throw e;
 					}
 				}
 				return result;
 			}
 		});
 	}
 	
 	public static Logger logger;
 
 	public static void initLogger() {
 		try {
 			logger = Logger.getLogger("log");
 			if (FILE_LOGGING) {
 				FileHandler fh = new FileHandler("C:\\tmp\\cindy.log", false);
 				fh.setFormatter(new SimpleFormatter());
 				logger.addHandler(fh);
 			}
 			//log.setLevel(Level.ALL);
 			logger.log(Level.INFO, "Log started");
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void setMaterial(GL gl, Color color, int shininess) {
 		gl.getGL2().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE,
				color.getColorComponents(null), 0);
 		gl.getGL2().glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS,
 				shininess);
 	}
 }
