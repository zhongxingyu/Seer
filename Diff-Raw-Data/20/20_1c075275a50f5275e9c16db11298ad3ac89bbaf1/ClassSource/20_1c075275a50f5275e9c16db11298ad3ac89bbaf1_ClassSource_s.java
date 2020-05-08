 package net.sf.jlayercheck.util.model;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 
import net.sf.jlayercheck.util.DependencyVisitor;
 
 public interface ClassSource {
 	/**
 	 * Runs the given DependencyVisitor on the classes of this source.
 	 * 
 	 * @param v
 	 * @throws IOException
 	 */ 
    public void call(DependencyVisitor v) throws IOException;
 
     /**
      * Returns a map that contains a mapping from the classname to
      * the source file for a class for all classes found in the
      * specified ClassSource.
      * 
      * @return
      */
 	public Map<String, URL> getSourceFiles();
 }
