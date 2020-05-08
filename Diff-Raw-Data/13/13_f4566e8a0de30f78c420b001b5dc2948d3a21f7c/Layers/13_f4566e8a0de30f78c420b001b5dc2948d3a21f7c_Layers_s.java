 /* gvSIG Mini. A free mobile phone viewer of free maps.
  *
  * Copyright (C) 2009 Prodevelop.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  *
  * For more information, contact:
  *
  *   Prodevelop, S.L.
  *   Pza. Don Juan de Villarrasa, 14 - 5
  *   46001 Valencia
  *   Spain
  *
  *   +34 963 510 612
  *   +34 963 510 968
  *   prode@prodevelop.es
  *   http://www.prodevelop.es
  *
  *   gvSIG Mini has been partially funded by IMPIVA (Instituto de la Pequea y
  *   Mediana Empresa de la Comunidad Valenciana) &
  *   European Union FEDER funds.
  *   
  *   2009.
  *   author Alberto Romeu aromeu@prodevelop.es 
  *   author Ruben Blanco rblanco@prodevelop.es 
  *   
  */
 
 package es.prodevelop.gvsig.mini.map;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import net.sf.microlog.core.Logger;
 import net.sf.microlog.core.LoggerFactory;
 
 import android.content.Context;
 import android.os.Environment;
 
 import es.prodevelop.gvsig.mini.activities.Map;
 import es.prodevelop.gvsig.mini.map.renderer.MapRenderer;
 import es.prodevelop.gvsig.mini.map.renderer.MapRendererFactory;
 import es.prodevelop.gvsig.mini.util.Utils;
 
 /**
  * Class to manage the persistence of Layers. It feeds the LayersActivity class
  * 
  * @author aromeu 
  * @author rblanco
  * 
  */
 public class Layers {
 
 	private static final Logger logger = LoggerFactory.getLogger(Layers.class);
 
 	private static Layers instance;
 	private static Context context;
 
 	private static Hashtable properties;
 	private static Hashtable<Integer, Vector> layers;
 
 	private final static String fileName = "layers.txt";
 	private static LayersSorter mLayersSorter; 
 
 	/**
 	 * A singleton. Creates a new instance of layers and fill the properties
 	 * with the info at
 	 * Environment.getExternalStorageDirectory().getPath()).append(
 	 * File.separator).append(Utils.APP_DIR).append(File.separator)
 	 * .append(Utils.LAYERS_DIR).append(File.separator).append(
 	 * "layers.txt").toString()
 	 * 
 	 * @return
 	 */
 	public static Layers getInstance() {
 		try {
 			if (instance == null) {
 				instance = new Layers();
 				instance.setLayersSorter(new LayersSorter()); 
 				try {
 					File layerFile = new File(getBaseLayerFilePath());
 					boolean exists = layerFile.exists();
 					loadProperties(getBaseLayerFilePath());
 					if (!exists)
 						instance.persist();
 
 				} catch (IOException e) {
 					logger.error("IOException on load layers", e);
 					loadProperties(null);
 				}
 			}
 			return instance;
 		} catch (Exception e) {
 			logger.error("getInstance: ", e);
 			return null;
 		}
 	}
 
 	private static String getBaseLayerFilePath() {
 		return new StringBuffer().append(
 				Environment.getExternalStorageDirectory().getPath()).append(
 				File.separator).append(Utils.APP_DIR).append(File.separator)
 				.append(Utils.LAYERS_DIR).append(File.separator).append(
 						fileName).toString();
 	}
 
 	public static void setContext(Context context) {
 		Layers.context = context;
 	}
 
 	/**
 	 * Parses a layer configuration file.
 	 * 
 	 * @param filePath
 	 *            The path of the layer configuration file to load. If this
 	 *            parameter is null then the 'layers.txt' file in the apk assets
 	 *            will be loaded
 	 * @throws Exception
 	 */
 	public static void loadProperties(String filePath) throws Exception {
 		BufferedReader reader = null;
 		InputStream is = null;
 		try {
 			logger.debug("loadProperties");
 			if (filePath == null) {
 				logger.error("filePath is null");
 				properties = new Hashtable();
 				layers = new Hashtable();
 				File layerFile = new File(getBaseLayerFilePath());
 				boolean exists = layerFile.exists();
 				if (exists) {
 					logger.debug(layerFile.getAbsolutePath());
 					loadProperties(getBaseLayerFilePath());
 					return;
 				} else {
 					logger.debug("load layers.txt from assets");
 					is = context.getAssets().open("layers.txt");
 				}	
 			} else {
 				logger.debug("filePath: " + filePath);
 				// if (properties == null)
 				properties = new Hashtable();
 				// if (layers == null)
 				layers = new Hashtable();
 
 				File layerFile = new File(filePath);
 				if (!layerFile.exists()) {
 					logger.debug("layerFile not exists");
 					loadProperties(null);
 					return;
 					// throw new IOException("File " + filePath
 					// + " does not exist.");
 				}
 
 				is = new FileInputStream(layerFile);
 			}
 
 			reader = new BufferedReader(new InputStreamReader(is));
 			parseLayersFile(reader);
 		} catch (IOException e) {
 			throw e;
 		} catch (Exception e) {
 			logger.error("loadProperties: ", e);
 			throw e;
 		} finally {
 			Utils.closeStream(is);
 		}
 	}
 
 	/**
 	 * Clears the properties Hashtable
 	 */
 	public static void clearProperties() {
 		try {
 			logger.debug("clearProperties");
 			properties = new Hashtable();
 			layers = new Hashtable();
 		} catch (Exception e) {
 			logger.error(e);
 		}
 	}
 
 	/**
 	 * Adds a layer to the current properties. The layer String must be in the format
 	 * defined by the layers.txt file:
 	 * OPEN STREET MAP;0,http://a.tile.openstreetmap.org/,png,17,256
 	 * @param layer
 	 */
 	public static void addLayer(String layer) {
 		try {			
 			String[] part = layer.split(";");
 			properties.put(part[0], part[1]);
 			Integer in = new Integer(part[1].substring(0, 1));
 			if (in >= MapRenderer.OSM_RENDERER && in < MapRenderer.WMS_RENDERER) {
 				in = new Integer(0);
 			} else {
 				in = new Integer(1);
 			}
 			Vector v = layers.get(in);
 			if (v == null) {
 				v = new Vector();
 			}
 			v.add(part[0]);
 			if (in.equals(0)) {
 				layers.put(new Integer(0), v);
 			} else {
 				layers.put(new Integer(1), v);
 			}
 
 			logger.debug("Found: " + part[0] + " with value: " + part[1]);
 		} catch (Exception e) {
 			logger.error("addLayer: ", e);
 		}
 	}
 
 	/**
 	 * Reads the BufferedReader line to line and calls {@link #addLayer(String)}
 	 * @param reader
 	 */
 	public static void parseLayersFile(final BufferedReader reader) {
 		try {
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				addLayer(line);
 			}
 		} catch (Exception e) {
 			logger.error("parseLayersFile: ", e);
 		} finally {
 			Utils.closeStream(reader);
 		}
 	}
 	
 	public Hashtable getLayers() {
 		return properties;
 	}
 	
 	private String getLayerKeyFromName(String layerName) {
 		try {
 			Enumeration keys = properties.keys();
 			
 			String key = null;
 			while(keys.hasMoreElements()) {
				key = keys.nextElement().toString();
				if (key.contains(layerName))
					return key;
 			}
 			return layerName;			
 		} catch (Exception e) {
 			logger.error(e);
 			return null;
 		}
 	}
 
 	/**
 	 * Instantiates a MapRenderer
 	 * @param layerTitle The title of the layer
 	 * @return A MapRenderer
 	 * @throws IOException If the layers file has a bad format
 	 */
 	public MapRenderer getRenderer(String layerTitle) throws IOException {
 		MapRenderer renderer = null;
 		try {
 			String layer = properties.get(getLayerKeyFromName(layerTitle)).toString(); 
 			String[] layerProps = layer.split(",");
 			final int size = layerProps.length;
 
 			if (layerProps.length == 0) {
 				logger.error("Bad layers file!");
 				throw new IOException("Bad layers file");
 			} else {
 				renderer = MapRendererFactory.getMapRenderer(layerTitle,
 						layerProps);
 
 				return renderer;
 			}
 		} catch (IOException ioe) {
 			throw ioe;
 		} catch (Exception e) {
 			logger.error("getRenderer: ", e);
 			return renderer;
 		}
 	}
 
 	public static Hashtable getLayersForView() {
 		try {
 			Enumeration keys = layers.keys();
 			
 			Integer key = null;
 			Vector keyLayers = null;
 			while(keys.hasMoreElements()) {
 				key = (Integer)keys.nextElement();
 				keyLayers = layers.get(key);
 				keyLayers = mLayersSorter.sort(keyLayers);
 				layers.put(key, keyLayers);
 			}
 		} catch (Exception e) {
 			logger.error(e);
 		}
 		return layers;
 	}
 
 	/**
 	 * Persists the Layers properties into a file in the directory:
 	 * SDDIR + File.separator + Utils.APP_DIR
 						+ File.separator + Utils.LAYERS_DIR
 	 * @param fileName The fileName
 	 */
 	public static void persist(String fileName) {
 		BufferedWriter out = null;
 		FileWriter logwriter = null;
 		try {
 			if (properties != null) {
 				String SDDIR = Environment.getExternalStorageDirectory()
 						.getPath();
 				String dirPath = SDDIR + File.separator + Utils.APP_DIR
 						+ File.separator + Utils.LAYERS_DIR + File.separator;
 
 				File f = new File(dirPath + fileName);
 				if (!f.exists()) {
 					File dirFile = new File(dirPath);
 					dirFile.mkdirs();
 					f.createNewFile();
 				} else {
 					f.delete();
 					f.createNewFile();
 				}
 				logwriter = new FileWriter(f, true);
 				out = new BufferedWriter(logwriter);
 
 				Enumeration keys = properties.keys();
 
 				String layerTitle = null;
 				MapRenderer renderer = null;
 
 				while (keys.hasMoreElements()) {
 					try {
 						layerTitle = keys.nextElement().toString();
 						if (layerTitle != null) {
 							renderer = Layers.getInstance().getRenderer(
 									layerTitle);
 							if (renderer != null) {
 								logger.debug(layerTitle + " persisted");
 								out.write(renderer.toString() + "\n");
 							}	
 						}
 					} catch (Exception e) {
 						logger.error("error while writing: " + layerTitle);
 					}
 				}
 			}
 
 		} catch (Exception e) {
 			logger.error("persist: ", e);
 		} finally {
 			Utils.closeStream(out);
 			Utils.closeStream(logwriter);
 		}
 	}
 
 	/**
 	 * Persists the layers properties to the default fileName (layers.txt)
 	 */
 	public static void persist() {
 		persist(fileName);
 	}
 	
 	/**
 	 * Sets the LayersSorter
 	 * @param aLayersSorter
 	 */
 	public void setLayersSorter(LayersSorter aLayersSorter) {
 		this.mLayersSorter = aLayersSorter;
 	}
 }
