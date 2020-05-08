 package org.genericsystem.web.util;
 
 import org.jboss.seam.faces.rewrite.FacesRedirect;
 import org.jboss.seam.faces.view.config.ViewConfig;
 import org.jboss.seam.faces.view.config.ViewPattern;
 
 /**
 * @author Nicolas Feybesse
 * 
  */
 @ViewConfig
 public interface GsViewConfig {
 
 	static enum Pages {
 
 		@FacesRedirect
 		@ViewPattern("/gsmyadmin/pages/index.xhtml")
 		HOME,
 
 		@FacesRedirect
 		@ViewPattern("/*")
 		ALL;
 
 	}
 }
