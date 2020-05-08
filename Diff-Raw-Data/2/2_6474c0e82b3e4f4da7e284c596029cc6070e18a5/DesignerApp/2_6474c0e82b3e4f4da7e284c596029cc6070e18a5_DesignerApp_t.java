 package org.openxdata.designer;
 
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.util.Locale;
 
 import org.apache.pivot.beans.BXML;
 import org.apache.pivot.beans.BXMLSerializer;
 import org.apache.pivot.collections.ArrayList;
 import org.apache.pivot.collections.HashMap;
 import org.apache.pivot.collections.List;
 import org.apache.pivot.collections.Map;
 import org.apache.pivot.collections.Sequence;
 import org.apache.pivot.io.FileList;
 import org.apache.pivot.serialization.SerializationException;
 import org.apache.pivot.util.Resources;
 import org.apache.pivot.wtk.Application;
 import org.apache.pivot.wtk.CardPane;
 import org.apache.pivot.wtk.Clipboard;
 import org.apache.pivot.wtk.Display;
 import org.apache.pivot.wtk.DropAction;
 import org.apache.pivot.wtk.HorizontalAlignment;
 import org.apache.pivot.wtk.Label;
 import org.apache.pivot.wtk.Manifest;
 import org.apache.pivot.wtk.MenuHandler;
 import org.apache.pivot.wtk.Prompt;
 import org.apache.pivot.wtk.TableView;
 import org.apache.pivot.wtk.TextArea;
 import org.apache.pivot.wtk.Theme;
 import org.apache.pivot.wtk.TreeView;
 import org.apache.pivot.wtk.VerticalAlignment;
 import org.apache.pivot.wtk.Window;
 import org.apache.pivot.wtk.effects.OverlayDecorator;
 import org.apache.pivot.xml.Element;
 import org.apache.pivot.xml.Node;
 import org.apache.pivot.xml.TextNode;
 import org.apache.pivot.xml.XMLSerializer;
 import org.fcitmuk.epihandy.FormDef;
 import org.fcitmuk.epihandy.xform.EpihandyXform;
 import org.openxdata.designer.util.Form;
 import org.openxdata.designer.util.Option;
 import org.openxdata.designer.util.Question;
 
 /**
  * The main entry point of the form designer application.
  * 
  * @author brent
  * 
  */
 public class DesignerApp implements Application {
 
 	public static final String LANGUAGE_KEY = "language";
 	public static final String APPLICATION_KEY = "application";
 
 	@BXML
 	private TreeView formTree;
 
 	@BXML
 	private TreeView designTree;
 
 	@BXML
 	private CardPane propertiesCardPane;
 
 	@BXML
 	private TableView namespacesTableView;
 
 	@BXML
 	private TableView attributesTableView;
 
 	@BXML
 	private TextArea textArea;
 
 	private Window window;
 
 	private OverlayDecorator promptDecorator = new OverlayDecorator();
 
 	private Locale locale;
 
 	private Resources resources;
 
 	public void startup(Display display, Map<String, String> properties)
 			throws Exception {
 
 		String language = properties.get(LANGUAGE_KEY);
 		locale = (language == null) ? Locale.getDefault()
 				: new Locale(language);
 		resources = new Resources(getClass().getName(), locale);
 
 		Theme theme = Theme.getTheme();
 		Font font = theme.getFont();
 
 		// Search for a font that can support the sample string
 		String sampleResource = (String) resources.get("greeting");
 		if (font.canDisplayUpTo(sampleResource) != -1) {
 			Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
 					.getAllFonts();
 
 			for (int i = 0; i < fonts.length; i++) {
 				if (fonts[i].canDisplayUpTo(sampleResource) == -1) {
 					theme.setFont(fonts[i].deriveFont(Font.PLAIN, 12));
 					break;
 				}
 			}
 		}
 
 		BXMLSerializer bxmlSerializer = new BXMLSerializer();
 
 		// Install this object as "application" in the default namespace
 		bxmlSerializer.getNamespace().put(APPLICATION_KEY, this);
 
 		window = (Window) bxmlSerializer.readObject(
 				DesignerApp.class.getResource("designer.bxml"), resources);
 
 		// Apply the binding annotations to this object
 		bxmlSerializer.bind(this);
 
 		// Apply the binding annotations to the menu handler
 		MenuHandler designMenuHandler = designTree.getMenuHandler();
 		if (designMenuHandler != null)
 			bxmlSerializer.bind(designMenuHandler);
 
 		Label prompt = new Label("Drag or paste XML here");
 		prompt.getStyles().put("horizontalAlignment",
 				HorizontalAlignment.CENTER);
 		prompt.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
 		promptDecorator.setOverlay(prompt);
 		formTree.getDecorators().add(promptDecorator);
 		designTree.getDecorators().add(promptDecorator);
 
 		window.open(display);
 	}
 
 	public void paste() {
 		Manifest clipboardContent = Clipboard.getContent();
 
 		if (clipboardContent != null && clipboardContent.containsText()) {
 			String xml = null;
 			try {
 				xml = clipboardContent.getText();
 				ByteArrayInputStream is = new ByteArrayInputStream(
 						xml.getBytes());
 				setDocument(is);
 			} catch (Exception exception) {
 				Prompt.prompt(exception.getMessage(), window);
 			}
 
 			window.setTitle((String) resources.get("title"));
 		}
 	}
 
 	public DropAction drop(Manifest dragContent) {
 		DropAction dropAction = null;
 
 		try {
 			if (dragContent.containsValue("targetPath")) {
 
 				@SuppressWarnings("unchecked")
 				List<Object> treeData = (List<Object>) designTree.getTreeData();
 
 				Object draggedObject = dragContent.getValue("node");
 				Sequence.Tree.Path draggedPath = (Sequence.Tree.Path) dragContent
 						.getValue("path");
 				Sequence.Tree.Path draggedParentPath = new Sequence.Tree.Path(
 						draggedPath, draggedPath.getLength() - 1);
 
 				Sequence.Tree.Path targetPath = (Sequence.Tree.Path) dragContent
 						.getValue("targetPath");
 				Object targetObject = Sequence.Tree.get(treeData, targetPath);
 				Sequence.Tree.Path targetParentPath = new Sequence.Tree.Path(
 						targetPath, targetPath.getLength() - 1);
 
 				Sequence.Tree.remove(treeData, draggedObject);
 
 				boolean acceptsAdd = (draggedObject instanceof Option
 						&& targetObject instanceof Question && ((Question) targetObject)
 						.isStaticOptionList())
						|| (draggedObject instanceof Question
 								&& targetObject instanceof Question && ((Question) targetObject)
 								.isQuestionList())
 						|| (targetObject instanceof List && !(targetObject instanceof Question));
 
 				if (acceptsAdd)
 					Sequence.Tree.add(treeData, draggedObject, targetPath);
 				else {
 					int insertLocation = targetPath
 							.get(targetPath.getLength() - 1) + 1;
 
 					// Shuffle down by one, if removal shortened target list
 					int draggedIndex = draggedPath
 							.get(draggedPath.getLength() - 1);
 					int targetIndex = targetPath
 							.get(targetPath.getLength() - 1);
 
 					// Class doesn't implement equals properly.
 					boolean sharedParent = targetParentPath.toString().equals(
 							draggedParentPath.toString());
 
 					if (sharedParent && draggedIndex <= targetIndex)
 						insertLocation -= 1;
 
 					Sequence.Tree.insert(treeData, draggedObject,
 							targetParentPath, insertLocation);
 				}
 
 			} else if (dragContent.containsFileList()) {
 				FileList fileList = dragContent.getFileList();
 				if (fileList.getLength() == 1) {
 					File file = fileList.get(0);
 
 					FileInputStream fileInputStream = null;
 					try {
 						try {
 							fileInputStream = new FileInputStream(file);
 							setDocument(fileInputStream);
 						} finally {
 							if (fileInputStream != null) {
 								fileInputStream.close();
 							}
 						}
 					} catch (Exception exception) {
 						Prompt.prompt(exception.getMessage(), window);
 					}
 
 					window.setTitle((String) resources.get("title") + "-"
 							+ file.getName());
 
 					dropAction = DropAction.COPY;
 				} else {
 					Prompt.prompt("Multiple files not supported.", window);
 				}
 			}
 		} catch (IOException exception) {
 			Prompt.prompt(exception.getMessage(), window);
 		}
 
 		return dropAction;
 	}
 
 	private void setDocument(InputStream documentStream) throws IOException,
 			SerializationException {
 
 		// Remove prompt decorator
 		if (promptDecorator != null) {
 			formTree.getDecorators().remove(promptDecorator);
 			designTree.getDecorators().remove(promptDecorator);
 			promptDecorator = null;
 		}
 
 		// Slurp input stream into String so we can parse twice
 		BufferedReader br = new BufferedReader(new InputStreamReader(
 				documentStream));
 		StringBuilder sb = new StringBuilder();
 		String line;
 		while ((line = br.readLine()) != null) {
 			sb.append(line);
 		}
 
 		XMLSerializer xs = new XMLSerializer();
 		Element document = (Element) xs.readObject(new StringReader(sb
 				.toString()));
 
 		ArrayList<Element> xmlData = new ArrayList<Element>();
 		xmlData.add(document);
 		formTree.setTreeData(xmlData);
 
 		Sequence.Tree.Path path = new Sequence.Tree.Path(0);
 		formTree.expandBranch(path);
 		formTree.setSelectedPath(path);
 
 		StringReader xmlReader = new StringReader(sb.toString());
 		FormDef formDef = EpihandyXform.fromXform2FormDef(xmlReader);
 		Form form = new Form(formDef);
 		ArrayList<Form> designData = new ArrayList<Form>();
 		designData.add(form);
 		designTree.setTreeData(designData);
 
 		path = new Sequence.Tree.Path(0);
 		designTree.expandBranch(path);
 		designTree.setSelectedPath(path);
 	}
 
 	public void updateProperties() {
 		Node node = (Node) formTree.getSelectedNode();
 
 		if (node instanceof TextNode) {
 			TextNode textNode = (TextNode) node;
 			textArea.setText(textNode.getText());
 			propertiesCardPane.setSelectedIndex(1);
 		} else if (node instanceof Element) {
 			Element element = (Element) node;
 
 			// Populate the namespaces table
 			ArrayList<HashMap<String, String>> namespacesTableData = new ArrayList<HashMap<String, String>>();
 
 			String defaultNamespaceURI = element.getDefaultNamespaceURI();
 			if (defaultNamespaceURI != null) {
 				HashMap<String, String> row = new HashMap<String, String>();
 				row.put("prefix", "(default)");
 				row.put("uri", defaultNamespaceURI);
 				namespacesTableData.add(row);
 			}
 
 			Element.NamespaceDictionary namespaceDictionary = element
 					.getNamespaces();
 			for (String prefix : namespaceDictionary) {
 				HashMap<String, String> row = new HashMap<String, String>();
 				row.put("prefix", prefix);
 				row.put("uri", namespaceDictionary.get(prefix));
 				namespacesTableData.add(row);
 			}
 
 			namespacesTableView.setTableData(namespacesTableData);
 
 			// Populate the attributes table
 			ArrayList<HashMap<String, String>> attributesTableData = new ArrayList<HashMap<String, String>>();
 
 			for (Element.Attribute attribute : element.getAttributes()) {
 				HashMap<String, String> row = new HashMap<String, String>();
 
 				String attributeName = attribute.getName();
 				row.put("name", attributeName);
 				row.put("value", element.get(attributeName));
 				attributesTableData.add(row);
 			}
 
 			attributesTableView.setTableData(attributesTableData);
 
 			propertiesCardPane.setSelectedIndex(0);
 		} else {
 			throw new IllegalStateException();
 		}
 	}
 
 	public boolean shutdown(boolean optional) throws Exception {
 
 		if (window != null)
 			window.close();
 
 		return false;
 	}
 
 	public void suspend() throws Exception {
 		// TODO Auto-generated method stub
 	}
 
 	public void resume() throws Exception {
 		// TODO Auto-generated method stub
 	}
 }
