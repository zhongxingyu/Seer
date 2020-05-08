 package net.fikovnik.projects.taco.core.internal.graphwiz;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.fikovnik.projects.taco.core.graphwiz.GraphwizException;
 import net.fikovnik.projects.taco.core.graphwiz.IGraphwiz;
 import net.fikovnik.projects.taco.core.graphwiz.IGraphwiz.GraphwizOutputType;
 import net.fikovnik.projects.taco.core.graphwiz.IGraphwizBuilder;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 public final class GraphwizBuilder implements IGraphwizBuilder {
 
 	private final IGraphwiz graphwiz;
 	private final GraphwizOutputType type;
 
 	private Map<File, Long> registeredFiles = new HashMap<File, Long>();
 	private File sourceFolder;
 
 	public GraphwizBuilder(IGraphwiz graphwiz, GraphwizOutputType type) {
 		// TODO: assert
 		this.graphwiz = graphwiz;
 		this.type = type;
 	}
 
 	@Override
 	public void regenerate(File targetFolder, boolean ignoreFailures, IProgressMonitor monitor) throws IOException, GraphwizException {
 		// TODO: assert
 		if (!targetFolder.exists()) {
 			if (!targetFolder.mkdirs()) {
 				throw new IOException("Unable to create output folder: "+targetFolder);
 			}
 		}
 
 		Collection<File> files = FileUtils.listFiles(sourceFolder, new String[] {IGraphwiz.DOT_FILE_EXT}, false);
 
 		monitor.beginTask("Generating class diagrams", files.size());
 		
 		for (File source : files) {
 			Long oChecksum = registeredFiles.remove(source);
 			if (oChecksum != null) {
 				Long nChecksum = FileUtils.checksumCRC32(source);
				if (oChecksum.equals(nChecksum)) {
 					// file has not changed
 					monitor.worked(1);
 					continue;
 				}
 			}
 			
 			try {
				String name = FilenameUtils.removeExtension(source.getName());
				String targetFileName = name + "." + type.getFileExtension();
				File target = new File(targetFolder, targetFileName);

 				monitor.subTask("Generating: " + targetFileName);
 				graphwiz.generate(source, target, type);
 				monitor.worked(1);
 			} catch (GraphwizException e) {
 				if (!ignoreFailures) {
 					throw e;
 				}
 			}
 		}
 
 		monitor.done();
 	}
 
 	@Override
 	public void registerExistingFiles(File sourceFolder, IProgressMonitor monitor) throws IOException {
 		// TODO: assert monitor
 		this.sourceFolder = sourceFolder;
 		
 		if (!sourceFolder.isDirectory()) {
 			// perhaps it have not yet generated
 			return;
 		}
 		if (!sourceFolder.canRead()) {
 			throw new IOException("Unable to list files in folder: "+sourceFolder);
 		}
 		
 		Collection<File> files = FileUtils.listFiles(sourceFolder, new String[] {IGraphwiz.DOT_FILE_EXT}, false);
 		
 		monitor.beginTask("Registering files", files.size());
 		
 		for (File f : files) {
 			Long checksum = FileUtils.checksumCRC32(f);
 			registeredFiles.put(f, checksum);
 			monitor.worked(1);
 		}
 		
 		monitor.done();
 	}
 
 }
