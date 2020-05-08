 package cytoscape.init;
 
 import java.util.List;
 
 /**
  * An interface that describes the initialization parameters needed 
  * by cytoscape.
  */
 public interface CyInitParams {
 
    public String getPropsFile();
 
    public String getVizPropsFile();
 
    public String getBioDataServer();
 
    public List getResourcePlugins();
 
    public List getExpressionFiles();
 
    public List getGraphFiles();
 
    public List getEdgeAttributeFiles();
 
    public List getNodeAttributeFiles();
 
    public List getPluginURLs();
 
    public int getMode();
 
    public static final int GUI = 1;
    public static final int TEXT = 2;
    public static final int LIBRARY = 3;
    public static final int EMBEDDED_WINDOW = 4;
 
    public Integer getViewThreshold();
    public boolean canonicalizeNames();
    public String getSpecies();
   public String[] getArgs();
 }
 
 
