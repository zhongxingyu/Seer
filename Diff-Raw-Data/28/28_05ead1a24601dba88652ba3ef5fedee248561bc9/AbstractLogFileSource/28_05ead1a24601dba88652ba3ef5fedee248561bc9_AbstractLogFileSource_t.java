 package hu.sztaki.ilab.longneck.weblog.parser;
 
 import hu.sztaki.ilab.longneck.process.access.Source;
 import hu.sztaki.ilab.longneck.util.BufferedFileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * 
  * @author Molnár Péter <molnarp@ilab.sztaki.hu>
  */
 abstract public class AbstractLogFileSource implements Source {
 
 	/** Indicates source-path set to standard input. */
 	public static final String STDIN = "stdin";
 
    /** The name of the source for identification from property. */
     protected String name;
    
    /** The name of the source for identification. */
    private String path;
 
 	/** List of paths to read log files from. */
 	protected List<String> sourcePath;
 	/** File reader to read files with. */
 	protected BufferedFileReader bf;
 	/** Current datasegment. */
 	protected Deque<String> dataSegment = new LinkedList<String>();
     /** The runtime properties. */
     protected Properties runtimeProperties;
 
     @Override
     public void init() {
         // Set source path
        if (path == null) {
            setSourcePath(runtimeProperties.getProperty(
                    String.format("weblogSource.%1$s.Path", name)));
        } else {
            setSourcePath(path);
        }
        
         if (sourcePath == null || "".equals(sourcePath)) {
            throw new RuntimeException(name!= null?String.format("weblogSource.%1$s.Path is undefined.", name):"Path is undefined");
        }
 
         // Initialize buffered reader
 		try {
 		    boolean stdinEnabled = STDIN.equals(sourcePath.get(0));
 		    bf = new BufferedFileReader.Builder(sourcePath, stdinEnabled).build();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
     }
 
 	public List<String> getSourcePath() {
 		return sourcePath;
 	}
 
 	public void setSourcePath(List<String> sourcePath) {
 		this.sourcePath = sourcePath;
 	}
 
 	public void setSourcePath(String sourcePath) {
        this.sourcePath = Arrays.asList(StringUtils.split(sourcePath,':'));
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

     public Properties getRuntimeProperties() {
         return runtimeProperties;
     }
 
     public void setRuntimeProperties(Properties runtimeProperties) {
         this.runtimeProperties = runtimeProperties;
     }
 }
