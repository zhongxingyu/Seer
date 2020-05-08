 package com.redhat.topicindex.extras.client.local.presenter;
 
 import org.vectomatic.file.File;
 import org.vectomatic.file.FileReader;
 import org.vectomatic.file.FileUploadExt;
 import org.vectomatic.file.events.ErrorHandler;
 import org.vectomatic.file.events.LoadEndEvent;
 import org.vectomatic.file.events.LoadEndHandler;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.Node;
 import com.google.gwt.xml.client.NodeList;
 import com.google.gwt.xml.client.XMLParser;
 
 import javax.enterprise.context.Dependent;
 import javax.inject.Inject;
 
 import com.redhat.topicindex.extras.client.local.Presenter;
 
 @Dependent
 public class TopicImportPresenter implements Presenter
 {
 	public interface Display
 	{
 		Button getGoButton();
 
 		FileUploadExt getUpload();
 
 		Widget asWidget();
 
 		TextArea getFileList();
 
 		TextArea getLog();
 	}
 
 	@Inject
 	private HandlerManager eventBus;
 
 	@Inject
 	private Display display;
 
 	public void bind()
 	{
 		display.getUpload().addChangeHandler(new ChangeHandler()
 		{
 			@Override
 			public void onChange(final ChangeEvent event)
 			{
 				display.getFileList().setText("");
 				final StringBuilder fileNames = new StringBuilder();
 
 				for (final File file : display.getUpload().getFiles())
 				{
 					fileNames.append(file.getName() + "\n");
 				}
 
 				display.getFileList().setText(fileNames.toString());
 			}
 		});
 
 		display.getGoButton().addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(final ClickEvent event)
 			{
 				/* Start processing the files. We create a chain of methods to simulate synchronous processing */
 				final StringBuilder log = new StringBuilder();
 				pocessFiles(0, log);
 			}
 		});
 	}
 
 	/**
 	 * Called when all files have been processed
 	 * 
 	 * @param log
 	 *            The string holding the log
 	 */
 	private void processingDone(final StringBuilder log)
 	{
 		display.getLog().setText(log.toString());
 	}
 
 	/**
 	 * Extract the file from the list of upload files and process it, or call processingDone if there are no more files to process.
 	 * 
 	 * @param index
 	 *            The index of the file to process
 	 * @param log
 	 *            The string holding the log
 	 */
 	private void pocessFiles(final int index, final StringBuilder log)
 	{
 		if (index >= display.getUpload().getFiles().getLength())
 		{
 			processingDone(log);
 		}
 		else
 		{
 			processFile(display.getUpload().getFiles().getItem(index), index, log);
 		}
 	}
 
 	/**
 	 * Load a file and then pass it off to have the XML processed. Once the file is loaded and its contents processed, call pocessFiles to process the next
 	 * file.
 	 * 
 	 * @param file
 	 *            The file to read
 	 * @param index
 	 *            The index off the file
 	 * @param log
 	 *            The string holding the log
 	 */
 	private void processFile(final File file, final int index, final StringBuilder log)
 	{
 		final FileReader reader = new FileReader();
 
 		reader.addErrorHandler(new ErrorHandler()
 		{
 			@Override
 			public void onError(final org.vectomatic.file.events.ErrorEvent event)
 			{
 				pocessFiles(index + 1, log);
 			}
 		});
 
 		reader.addLoadEndHandler(new LoadEndHandler()
 		{
 			@Override
 			public void onLoadEnd(final LoadEndEvent event)
 			{
 				final String result = reader.getStringResult();
 
 				processXML(result, file, index, log);
 			}
 		});
 
 		reader.readAsBinaryString(file);
 	}
 
 	private void processXML(final String result, final File file, final int index, final StringBuilder log)
 	{
 		try
 		{
 			String fixedResult = result;
 			
			/* remove utf-8 BOM marker (if present) */
 			if (fixedResult.startsWith("ï»¿"))
 				fixedResult = fixedResult.replaceFirst("ï»¿", "");
 			
 			/* parse the XML document into a DOM */
 			final Document doc = XMLParser.parse(fixedResult);
 
 			/* what is the top level element */
 			Node toplevelNode = doc.getDocumentElement();
 
 			/* Get the node name */
 			final String toplevelNodeName = toplevelNode.getNodeName();
 
 			/* sections can be imported directly */
 			if (toplevelNodeName.equals("section"))
 			{
 				// no processing required
 			}
 			/* tasks are turned into sections */
 			else if (toplevelNodeName.equals("task"))
 			{
 				log.append(file.getName() + ": This topic has had its document element changed from <task> to <section>.\n");
 				toplevelNode = replaceNodeWithSection(toplevelNode);
 			}
 			/* variablelist are turned into sections */
 			else if (toplevelNodeName.equals("variablelist"))
 			{
 				log.append(file.getName() + ": This topic has had its document element changed from <variablelist> to <section>.\n");
 				toplevelNode = replaceNodeWithSection(toplevelNode);
 			}
 			/* Some unknown node type */
 			else
 			{
 				log.append(file.getName() + ": This topic uses an unrecognised parent node of <" + toplevelNodeName + ">. No processing has been done for this topic, and the XML has been included as is.\n");
 				uploadFile(result, file, index, log);
 				return;
 			}
 
 			/* some additional validity checks */
 			final String errors = isNodeValid(toplevelNode);
 
 			if (errors != null && !errors.isEmpty())
 				log.append(file.getName() + ":" + errors + "\n");
 
 			/* Upload the processed XML */
 			uploadFile(toplevelNode.getOwnerDocument().toString(), file, index, log);
 
 		}
 		catch (final Exception ex)
 		{
 			/* The xml is not valid, so upload as is */
 			log.append(file.getName() + ": This topic has invalid XML, and has been uploaded as is.\n");
 			uploadFile(result, file, index, log);
 		}
 	}
 
 	private void uploadFile(final String topicXML, final File file, final int index, final StringBuilder log)
 	{
 		log.append("-------------------------------------\n");
 		log.append("Uploaded file " + file.getName() + "\n");
 		log.append("XML Contents is:\n");
 		log.append(topicXML + "\n");
 		log.append("-------------------------------------\n");
 
 		pocessFiles(index + 1, log);
 	}
 
 	/**
 	 * Create a new Document with a <section> document lement node. This is to work around the lack of a Document.renameNode() method.
 	 * 
 	 * @param node
 	 *            The node to be replaced
 	 * @return The new node in a new document
 	 */
 	private Node replaceNodeWithSection(final Node node)
 	{
 		final Document doc = XMLParser.createDocument();
 		final Node section = doc.createElement("section");
 		doc.appendChild(section);
 
 		final NodeList children = node.getChildNodes();
 
 		for (int i = 0; i < children.getLength(); ++i)
 		{
 			final Node childNode = children.item(i);
 			final Node importedNode = doc.importNode(childNode, true);
 			section.appendChild(importedNode);
 		}
 
 		return section;
 	}
 
 	/**
 	 * Perform some validity checks on the topic
 	 * 
 	 * @param node
 	 *            The node that holds the topic
 	 * @return any errors that were found
 	 */
 	private String isNodeValid(final Node node)
 	{
 		final StringBuilder builder = new StringBuilder();
 
 		if (getFirstChild(node, "section", true) != null)
 			builder.append(" This topic has illegal nested sections.");
 		if (getFirstChild(node, "xref", true) != null)
 			builder.append(" This topic has illegal xrefs.");
 		if (getFirstChild(node, "xi:include", true) != null)
 			builder.append(" This topic has illegal xi:includes.");
 		if (getFirstChild(node, "title", false) == null)
 			builder.append(" This topic has no title.");
 
 		return builder.toString();
 	}
 
 	/**
 	 * Scans a XML document for a node with the given name
 	 * 
 	 * @param node
 	 *            The node to search
 	 * @param nodeName
 	 *            The name of the node to find
 	 * @param recursive true if a search is to be done on the children as well, false otherwise
 	 * @return null if no node is found, or the first node with the supplied name that was found
 	 */
 	private Node getFirstChild(final Node node, final String nodeName, final boolean recursive)
 	{
 		final NodeList children = node.getChildNodes();
 		for (int i = 0; i < children.getLength(); ++i)
 		{
 			final Node child = children.item(i);
 
 			// TODO: check URI (e.g. for xi:include)
 
 			if (child.getNodeName().equals(nodeName))
 				return child;
 		}
 
 		if (recursive)
 		{
 			for (int i = 0; i < children.getLength(); ++i)
 			{
 				final Node child = children.item(i);
 				final Node namedChild = getFirstChild(child, nodeName, recursive);
 				if (namedChild != null)
 					return namedChild;
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public void go(final HasWidgets container)
 	{
 		bind();
 		container.clear();
 		container.add(display.asWidget());
 	}
 }
