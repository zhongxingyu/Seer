 package explorer.ui.tree.actions;
 
 import static org.osgi.service.event.EventConstants.EVENT_TOPIC;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 
 import javax.jcr.Binary;
 import javax.jcr.Node;
 import javax.jcr.RepositoryException;
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.jackrabbit.util.Text;
 import org.apache.sling.api.resource.Resource;
 import org.apache.tika.config.TikaConfig;
 import org.apache.tika.exception.TikaException;
 import org.apache.tika.io.TikaInputStream;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.mime.MediaType;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import explorer.core.api.ResourceTreeModel;
 import explorer.core.api.SessionProvider;
 import explorer.node.NodeTypeUtil;
 import explorer.ui.EventTypes;
 
 @SuppressWarnings("serial")
 @Component(name = "Sling Explorer Menu Action - Import File", description = "Updates the Editor Pane with the correct view")
 @Service(value = { AbstractAction.class, EventHandler.class })
@Properties(value = { @Property(name = EVENT_TOPIC, value = EventTypes.TREE_SELECTION),
 		@Property(name = "menuType", value = "TREEMENU") })
 public class ImportFile extends AbstractAction implements EventHandler {
 
 	private Resource selectedResource;
 
 	private static final Logger log = LoggerFactory.getLogger(ImportFile.class);
 
 	@Reference
 	SessionProvider sessionProvider;
 
 	@Reference
 	ResourceTreeModel treeModel;
 
 	public ImportFile() {
 		super("Import Files");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 
 		Node node = selectedResource.adaptTo(Node.class);
 		final JFileChooser fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 			try {
 				if (node.isNodeType("nt:folder")) {
 					File file = fc.getSelectedFile();
 					if (file.isFile()) {
 						importFile(node, file);
 					} else if (file.isDirectory()) {
 						importFolder(node, file);
 					}
 				}
 				treeModel.fireStructureChanged(selectedResource);
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	private Node importFile(Node parentnode, File file) throws RepositoryException, IOException {
 		
 		String mimeTypes = null;
 		try {
 			mimeTypes = determineMimeType(new FileInputStream(file));
 		} catch (TikaException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} // mimes.getMimeType(file.getName());
 		
 		String fileName = Text.escapeIllegalJcrChars(file.getName());
 		
 		Node fileNode = parentnode.addNode(fileName, "nt:file");
 
 		Node resNode = fileNode.addNode("jcr:content", "nt:resource");
 		Binary binary = parentnode.getSession().getValueFactory().createBinary(new FileInputStream(file));
 		Calendar lastModified = Calendar.getInstance();
 		lastModified.setTimeInMillis(file.lastModified());
 		
 		resNode.setProperty("jcr:mimeType", mimeTypes);
 		resNode.setProperty("jcr:data", binary);
 		resNode.setProperty("jcr:lastModified", lastModified);
 		sessionProvider.save();
 
 		return fileNode;
 	}
 
 	private String determineMimeType(InputStream is) throws TikaException, IOException {
 		TikaConfig tika = new TikaConfig();
 		Metadata data = new Metadata();
 		MediaType mimetype = tika.getDetector().detect(TikaInputStream.get(is), data);
 		return mimetype.toString();
 	}
 
 	
 	private void importFolder(Node destinationNode, File folderToImport) throws RepositoryException, IOException {
 		File children[] = folderToImport.listFiles();
 		String folderName = Text.escapeIllegalJcrChars(folderToImport.getName());
 		Node newFolderNode = destinationNode.addNode(folderName, "sling:Folder");
 		sessionProvider.save();
 		System.out.println(destinationNode.getPath());
 		for (int i = 0; i < children.length; i++) {
 			File child = children[i];
 			if (child.isDirectory()) {
 				importFolder(newFolderNode, child);
 			} else {
 				importFile(newFolderNode, child);
 			}
 		}
 
 	}
 
 	@Override
 	public void handleEvent(Event event) {
 		selectedResource = (Resource) event.getProperty("data");
 		setEnabled(NodeTypeUtil.isType(selectedResource, "nt:folder"));
 	}
 }
