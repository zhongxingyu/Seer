 package biz.vidal.shrinkwrap.uberjar.base;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.jar.Manifest;
 import java.util.logging.Logger;
 
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ArchiveFormat;
 import org.jboss.shrinkwrap.api.ArchivePath;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.Filter;
 import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
 import org.jboss.shrinkwrap.api.asset.Asset;
 import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
 import org.jboss.shrinkwrap.api.exporter.ZipExporter;
 import org.jboss.shrinkwrap.impl.base.container.ContainerBase;
 import org.jboss.shrinkwrap.impl.base.path.BasicPath;
 
 import biz.vidal.shrinkwrap.uberjar.api.UberjarArchive;
 
 /**
  * WebArchiveImpl
  *
  * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
  * @version $Revision: $
  */
 public class UberjarArchiveImpl extends ContainerBase<UberjarArchive> implements UberjarArchive {
     // -------------------------------------------------------------------------------------||
     // Class Members ----------------------------------------------------------------------||
     // -------------------------------------------------------------------------------------||
 
     @SuppressWarnings("unused")
     private static final Logger log = Logger.getLogger(UberjarArchiveImpl.class.getName());
 
     /**
      * Path to the web inside of the Archive.
      */
     private static final ArchivePath PATH_UBER = ArchivePaths.root();
 
     /**
      * Path to the WEB-INF inside of the Archive.
      */
     private static final ArchivePath PATH_WORLDS_INF = ArchivePaths.create("WORLDS-INF");
 
     /**
      * Path to the resources inside of the Archive.
      */
     private static final ArchivePath PATH_RESOURCE = PATH_UBER;
 
     /**
      * Path to the libraries inside of the Archive.
      */
     private static final ArchivePath PATH_LIBRARY = ArchivePaths.create(PATH_WORLDS_INF, "lib");
 
     private static final ArchivePath PATH_CONF = ArchivePaths.create(PATH_WORLDS_INF, "conf");
 
     /**
      * Path to the classes inside of the Archive.
      */
     private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_WORLDS_INF, "classes");
 
     /**
      * Path to the manifests inside of the Archive.
      */
     private static final ArchivePath PATH_MANIFEST = ArchivePaths.create("META-INF");
 
 	private List<ArchivePath> libraries = new ArrayList<ArchivePath>();
 
 	private String mainClass;
 
     // -------------------------------------------------------------------------------------||
     // Instance Members -------------------------------------------------------------------||
     // -------------------------------------------------------------------------------------||
 
     // -------------------------------------------------------------------------------------||
     // Constructor ------------------------------------------------------------------------||
     // -------------------------------------------------------------------------------------||
 
     /**
      * Create a new WebArchive with any type storage engine as backing.
      *
      * @param delegate
      *            The storage backing.
      */
     public UberjarArchiveImpl(final Archive<?> delegate) {
         super(UberjarArchive.class, delegate);
        setManifest(new StringAsset("Main-Class: " + "org.codehaus.classworlds.uberjar.boot.Bootstrapper"));
     }
 
     // -------------------------------------------------------------------------------------||
     // Required Implementations -----------------------------------------------------------||
     // -------------------------------------------------------------------------------------||
 
     /**
      * {@inheritDoc}
      *
      * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getManifestPath()
      */
     @Override
     protected ArchivePath getManifestPath() {
         return PATH_MANIFEST;
     }
 
     /**
      * {@inheritDoc}
      *
      * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getClassesPath()
      */
     @Override
     protected ArchivePath getClassesPath() {
         return PATH_CLASSES;
     }
 
     /**
      * {@inheritDoc}
      *
      * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getResourcePath()
      */
     @Override
     protected ArchivePath getResourcePath() {
         return PATH_RESOURCE;
     }
 
     /**
      * {@inheritDoc}
      *
      * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getLibraryPath()
      */
     @Override
     protected ArchivePath getLibraryPath() {
         return PATH_LIBRARY;
     }
 
     public UberjarArchive addAsLibrary(Asset resource, ArchivePath target) throws IllegalArgumentException {
     	this.libraries.add(target);
     	return super.addAsLibrary(resource, target);
     }
 
     public UberjarArchive addAsLibrary(final Archive<?> archive) throws IllegalArgumentException {
     	this.libraries.add(new BasicPath(archive.getName()));
     	return super.addAsLibrary(archive);
     }
 
     @Override
 	public <X extends Archive<X>> Collection<X> getAsType(Class<X> type, Filter<ArchivePath> filter) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <X extends Archive<X>> Collection<X> getAsType(Class<X> type, Filter<ArchivePath> filter, ArchiveFormat archiveFormat) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public UberjarArchive createConfiguration() throws IllegalArgumentException {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		writeClassworldsConfiguration(baos, mainClass, libraries);
 		add(new ByteArrayAsset(baos.toByteArray()), new BasicPath(PATH_CONF, "classworlds.conf"));
 		return covarientReturn();
 	}
 
 	public void writeClassworldsConfiguration(OutputStream os, String mainTypeName, List<ArchivePath> paths) {
 		PrintStream print = new PrintStream(os);
 		print.println("main is " + mainTypeName + " from app");
 		print.println("[app]");
 		for (ArchivePath entry : paths) {
 			String path = "${classworlds.lib}" + entry.get();
 			print.println("\tload " + path);
 		}
 	}
 
 	@Override
 	public UberjarArchive setMainClass(String type) throws IllegalArgumentException {
 		this.mainClass = type;
 		return covarientReturn();
 	}
 
 	@Override
 	public UberjarArchive setClassworlds(Archive<?> archive) throws IllegalArgumentException {
 		add(new ArchiveAsset(archive, ZipExporter.class), new BasicPath(PATH_WORLDS_INF, "classworlds.jar"));
 		return covarientReturn();
 	}
 
 }
