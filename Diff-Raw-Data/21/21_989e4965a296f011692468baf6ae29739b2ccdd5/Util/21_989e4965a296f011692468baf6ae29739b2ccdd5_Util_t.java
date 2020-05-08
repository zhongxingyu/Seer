 package de.tum.in.cindy3dplugin.jogl;
 
 import java.awt.Color;
 import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.CodeSource;
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.swing.JFileChooser;
 
 import org.apache.commons.math.geometry.Vector3D;
 import org.apache.commons.math.linear.RealMatrix;
 
 import com.jogamp.common.jvm.JNILibLoaderBase;
 import com.jogamp.common.jvm.JNILibLoaderBase.LoaderAction;
 import com.jogamp.gluegen.runtime.NativeLibLoader;
 import com.jogamp.opengl.util.glsl.ShaderCode;
 import com.jogamp.opengl.util.glsl.ShaderProgram;
 
 public class Util {
 	private static final String SHADER_PATH = "/de/tum/in/cindy3dplugin/resources/shader/";
 	private static final boolean FILE_LOGGING = false;
 	public static final int SIZEOF_DOUBLE = 8;
 	public static final int SIZEOF_INT = 4;
 
 	private static String shaderLightFillIn = "";
 	public static Logger logger;
 
 	public static float[] matrixToFloatArray(RealMatrix m) {
 		int rows = m.getRowDimension();
 		int cols = m.getColumnDimension();
 
 		float[] result = new float[rows * cols];
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
 
 		float[] result = new float[rows * cols];
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
 		return new double[] { v.getX(), v.getY(), v.getZ() };
 	}
 	
 	public static float[] vectorToFloatArray(Vector3D v) {
 		return new float[] { (float) v.getX(), (float) v.getY(),
 				(float) v.getZ() };
 	}
 	
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
 	
 	public static ShaderProgram loadShaderProgram(GL2 gl2,
 			String vertexShaderFileName, String fragmentShaderFileName) {
 		ShaderProgram program = new ShaderProgram();
 		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		
 		ShaderCode vertexShader = loadShader(GL2.GL_VERTEX_SHADER,
 				vertexShaderFileName);
		if (!vertexShader.compile(gl2, ps)) {
			Util.logger.info("Compile log for '" + vertexShaderFileName + "': "
					+ baos.toString());
 			return null;
 		}
 	
 		ShaderCode fragmentShader = loadShader(GL2.GL_FRAGMENT_SHADER,
 				fragmentShaderFileName);
		if (!fragmentShader.compile(gl2, ps)) {
			Util.logger.info("Compile log for '" + fragmentShaderFileName
					+ "': " + baos.toString());
 			return null;
 		}
 	
 		if (!program.add(vertexShader)) {
 			return null;
 		}
 		if (!program.add(fragmentShader)) {
 			return null;
 		}
		if (!program.link(gl2, ps)) {
			Util.logger.info("Link log: " + baos.toString());
 			return null;
 		}
 	
 		return program;
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
 			Util.logger.log(Level.INFO, e.toString(), e);
 			return;
 		}
 		
 		File jarFile = new File(jarPath);
 		if (!jarFile.isFile()) {
 			// Not loaded from JAR file, do nothing
 			Util.logger.info("Not loaded from jar");
 			return;
 		}
 		final String basePath = jarFile.getParent();
 		Util.logger.info("Base path: " + basePath);
 
 		// Prevent gluegen from trying to load native library via
 		// System.loadLibrary("gluegen-rt").
 		NativeLibLoader.disableLoading();
 
 		// Instead, (try to) load it ourselves from JAR directory
 		String path = basePath + File.separator
 				+ System.mapLibraryName("gluegen-rt");
 		System.load(path);
 		
 		Util.logger.info("Loaded " + path);
 
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
 				Util.logger.info("Requested library " + libname);
 				try {
 					// Load JNI library from JAR directory
 					String path = basePath + File.separator
 							+ System.mapLibraryName(libname);
 					System.load(path);
 					Util.logger.info("Loaded " + path);
 				} catch (UnsatisfiedLinkError e) {
 //					Util.logger.log(Level.INFO, e.toString(), e);
 					Util.logger.info("Library load failed, trying fallback to System.loadLibrary");
 					try {
 						System.loadLibrary(libname);
 						Util.logger.info("Loaded system library " + libname);
 					} catch (UnsatisfiedLinkError e2) {
 						Util.logger.info("System library load failed");
 //						Util.logger.log(Level.INFO, e.toString(), e2);
 						result = false;
 						if (!ignoreError) {
 							throw e2;
 						}
 					}
 				}
 				return result;
 			}
 		});
 	}
 	
 	public static void initLogger() {
 		try {
 			logger = Logger.getLogger("log");
 			if (FILE_LOGGING) {
 				JFileChooser fileChooser = new JFileChooser();
 				fileChooser.setDialogTitle("Select log file");
 				int returnVal = fileChooser.showSaveDialog(null);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					File logFile = fileChooser.getSelectedFile();
 					FileHandler fh = new FileHandler(logFile.getAbsolutePath(),
 							false);
 					fh.setFormatter(new SimpleFormatter());
 					logger.addHandler(fh);
 				}
 			}
 			//log.setLevel(Level.ALL);
 			logger.info("Log started");
 			Properties p = System.getProperties();
 			String props = "";
 			for (Object key : p.keySet()) {
 				props += key + ": ";
 				props += p.get(key);
 				props += System.getProperty("line.separator");
 			}
 			logger.info("System properties:"
 					+ System.getProperty("line.separator") + props);
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void setMaterial(GL gl, Color color, double shininess) {
 		gl.getGL2().glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE,
 				color.getComponents(null), 0);
 		gl.getGL2().glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS,
 				(float) shininess);
 	}
 	
 	public static Vector3D transformPoint(RealMatrix matrix, Vector3D vec) {
 		double[] tmp = matrix.operate(new double[] { vec.getX(), vec.getY(),
 				vec.getZ(), 1 });
 		return new Vector3D(tmp[0], tmp[1], tmp[2]);
 	}
 	
 	public static Vector3D transformVector(RealMatrix matrix, Vector3D vec) {
 		double[] tmp = matrix.operate(new double[] { vec.getX(), vec.getY(),
 				vec.getZ(), 0 });
 		return new Vector3D(tmp[0], tmp[1], tmp[2]);
 	}
 }
