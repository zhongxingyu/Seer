 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.domainserver.project;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import org.netbeans.api.project.Project;
 import org.netbeans.spi.project.ProjectFactory;
 import org.netbeans.spi.project.ProjectState;
 
 import org.openide.filesystems.FileObject;
 
 import java.io.IOException;
 
 import java.util.Properties;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public final class DomainserverProjectFactory implements ProjectFactory {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(DomainserverProjectFactory.class);
 
     public static final String PROJECT_DIR = "cidsDomainServer";        // NOI18N
     public static final String PROJECT_PROPFILE = "project.properties"; // NOI18N
 
     static {
        DOMConfigurator.configure(DomainserverProjectFactory.class.getResource("log4j.xml")); // NOI18N
     }
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DomainserverProjectFactory object.
      */
     public DomainserverProjectFactory() {
         if (LOG.isDebugEnabled()) {
             LOG.debug("new DomainserverProjectFactory created"); // NOI18N
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Project loadProject(final FileObject dir, final ProjectState state) throws IOException {
         return isProject(dir) ? new DomainserverProject(dir, state) : null;
     }
 
     @Override
     public void saveProject(final Project project) throws IOException, ClassCastException {
         final Properties properties = project.getLookup().lookup(Properties.class);
         final FileObject fob = project.getProjectDirectory().getFileObject(PROJECT_DIR + "/" + PROJECT_PROPFILE); // NOI18N
         properties.store(fob.getOutputStream(), "Cids Domainserver Project Properties");                          // NOI18N
     }
 
     @Override
     public boolean isProject(final FileObject projectDirectory) {
         if (LOG.isDebugEnabled()) {
             LOG.debug(
                 "isProject: "
                         + projectDirectory.getPath()
                         + " :: " // NOI18N
                         + (projectDirectory.getFileObject(PROJECT_DIR) != null));
         }
         return projectDirectory.getFileObject(PROJECT_DIR) != null;
     }
 }
