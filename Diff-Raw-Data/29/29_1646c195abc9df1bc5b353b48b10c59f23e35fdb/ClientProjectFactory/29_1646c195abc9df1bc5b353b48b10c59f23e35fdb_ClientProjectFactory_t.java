 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.client;
 
 import org.netbeans.api.project.Project;
 import org.netbeans.spi.project.ProjectFactory;
 import org.netbeans.spi.project.ProjectState;
 
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.Properties;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public class ClientProjectFactory implements ProjectFactory {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String PROJECT_DIR = "cidsClient";              // NOI18N
     public static final String PROJECT_PROPFILE = "project.properties"; // NOI18N
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Project loadProject(final FileObject dir, final ProjectState state) throws IOException {
         return isProject(dir) ? new ClientProject(dir, state) : null;
     }
 
     @Override
     public void saveProject(final Project project) throws IOException, ClassCastException {
        final FileObject projectDir = project.getProjectDirectory().getFileObject(PROJECT_DIR);
        if (projectDir == null) {
            throw new IOException("Project dir " + projectDir.getPath() + " deleted, cannot save project"); // NOI18N
         }
         // Force creation of the scenes/ dir if it was deleted
         ((ClientProject)project).getWebinterfaceFolder(true);
        // Find the properties file project.properties,
         // creating it if necessary
        FileObject propertiesFile = projectDir.getFileObject(PROJECT_PROPFILE);
         if (propertiesFile == null) {
             // Recreate the properties file if needed
            propertiesFile = projectDir.createData(PROJECT_PROPFILE);
         }
         final Properties properties = project.getLookup().lookup(Properties.class);
         properties.store(new FileOutputStream(FileUtil.toFile(propertiesFile)), "Cids Client Project Properties"); // NOI18N
     }
 
     @Override
     public boolean isProject(final FileObject projectDirectory) {
         return projectDirectory.getFileObject(PROJECT_DIR) != null;
     }
 }
