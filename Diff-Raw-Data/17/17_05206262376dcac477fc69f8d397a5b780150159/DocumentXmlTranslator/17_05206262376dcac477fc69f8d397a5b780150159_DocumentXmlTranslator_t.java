 /*
  * Feb 27, 2005
  */
 package com.thinkparity.model.parity.api.document.xml;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.UUID;
 import java.util.Vector;
 
 import com.thinkparity.codebase.CompressionUtil;
 import com.thinkparity.codebase.CompressionUtil.Level;
 
 import com.thinkparity.model.parity.api.ParityObject;
 import com.thinkparity.model.parity.api.ParityXmlTranslator;
 import com.thinkparity.model.parity.api.document.DocumentVersion;
 import com.thinkparity.model.parity.model.document.Document;
 import com.thinkparity.model.parity.util.Base64;
 import com.thinkparity.model.parity.xml.XmlTranslator;
 import com.thinkparity.model.xstream.XStreamUtil;
 
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 
 /**
  * DocumentXmlTranslator
  * @author raykroeker@gmail.com
  * @version 1.1
  */
 public class DocumentXmlTranslator extends ParityXmlTranslator implements
 		XmlTranslator {
 
 	private class Content {
 		private byte[] content;
 		private String contentChecksum;
 	}
 
 	/**
 	 * Compression level to use when writing the content to the version file.
 	 */
 	private static final Level compressionLevel = Level.Nine;
 
 	/**
 	 * Create a DocumentXmlTranslator
 	 */
 	public DocumentXmlTranslator() { super("document", Document.class); }
 
 	/**
 	 * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
 	 */
 	public void marshal(Object source, HierarchicalStreamWriter writer,
 			MarshallingContext context) {
 		logger.info("marshal(Object, HierarchicalStreamWriter)");
 		logger.debug(source);
 		final Document document = (Document) source;
 		writeName(document.getName(), writer);
 		writeCreatedBy(document.getCreatedBy(), writer);
 		writeKeyHolder(document, writer);
 		writeId(document.getId(), writer);
 		writeCreatedOn(document.getCreatedOn(), writer);
 		writeDescription(document.getDescription(), writer);
 		writeDirectory(document.getDirectory(), writer);
 		try { writeContent(document, writer); }
 		catch(IOException iox) {
 			fatal((ParityObject) source, "Could not serialize file content.", iox);
 		}
 		writeNotes(document, writer);
 		writeVersions(document.getVersions(), writer);
 		writeCustomProperties(document, writer);
 	}
 
 	/**
 	 * Marshal the document for the version translator. This will omit the
 	 * version listings from the writing.
 	 * 
	 * @param document
	 *            The document.
	 * @param version
	 *            The document version.
 	 * @param writer
 	 *            The output writer.
 	 * @param context
 	 *            The output context.
 	 */
 	void marshalForVersion(final DocumentVersion version,
 			final Document document, final HierarchicalStreamWriter writer,
 			final MarshallingContext context) {
 		logger.info("marshal(Object, HierarchicalStreamWriter)");
 		logger.debug(document);
 		writeName(document.getName(), writer);
 		writeCreatedBy(document.getCreatedBy(), writer);
 		writeKeyHolder(document, writer);
 		writeId(document.getId(), writer);
 		writeCreatedOn(document.getCreatedOn(), writer);
 		writeDescription(document.getDescription(), writer);
 		writeDirectory(document.getDirectory(), writer);
 		try { writeContent(document, writer); }
 		catch(IOException iox) {
 			fatal((ParityObject) document, "Could not serialize file content.", iox);
 		}
 		// get a list of the versions minus the current one
 		final Collection<DocumentVersion> excludeList =
 			new Vector<DocumentVersion>(1);
 		excludeList.add(version);
 		final Collection<DocumentVersion> versions =
 			document.getVersionsExclude(excludeList);
 		writeVersions(versions, writer);
 		writeNotes(document, writer);
 		writeCustomProperties(document, writer);
 	}
 
 	/**
 	 * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
 	 */
 	public Object unmarshal(HierarchicalStreamReader reader,
 			UnmarshallingContext context) {
 		Document document = null;
 		try {
 			final String name = readName(reader);
 			final String createdBy = readCreatedBy(reader);
 			final String keyHolder = readKeyHolder(reader);
 			final UUID id = readId(reader);
 			final Calendar createdOn = readCreatedOn(reader);
 			final String description = readDescription(reader);
 			final File directory = readDirectory(reader);
 			final Content content = readContent(reader);
 			document = new Document(name, createdOn, createdBy, keyHolder,
 					description, directory, id, content.content, content.contentChecksum);
 			readNotes(document, reader);
 			readVersions(document, reader);
 			readCustomProperties(document, reader);
 		}
 		catch(Exception x) { fatal(document, "An unknown error occured parsing document xml.", x); }
 		return document;
 	}
 
 
 	protected final void readVersion(final Document document,
 			final HierarchicalStreamReader reader) throws FileNotFoundException {
 		reader.moveDown();
 		final File versionMetaDataFile = new File(reader.getValue());
 		document.add((DocumentVersion) XStreamUtil
 				.fromXML(new InputStreamReader(new FileInputStream(
 						versionMetaDataFile))));
 		reader.moveUp();
 	}
 
 	protected final void readVersions(final Document document,
 			final HierarchicalStreamReader reader) throws FileNotFoundException {
 		reader.moveDown();
 		while(reader.hasMoreChildren()) { readVersion(document, reader); }
 		reader.moveUp();
 	}
 
 	private Content readContent(final HierarchicalStreamReader reader)
 			throws IOException {
 		reader.moveDown();										// <content>
 		final String byteContentChecksum = reader.getAttribute("checksum");
 		final String encodedCompressedContent =
 			reader.getValue();
 		final byte[] decodedContent = Base64.decodeBytes(encodedCompressedContent);
 		final byte[] byteContent = CompressionUtil.decompress(decodedContent);
 		reader.moveUp();										// </content>
 		final Content content = new Content();
 		content.content = byteContent;
 		content.contentChecksum = byteContentChecksum;
 		return content;
 	}	
 
 	private void writeContent(final Document document,
 			final HierarchicalStreamWriter writer) throws IOException {
 		writer.startNode("content");								// <content>
 		writer.addAttribute("encoding", Base64.getEncoder());
 		writer.addAttribute("compressionlevel", compressionLevel.toString());
 		writer.addAttribute("checksum", document.getContentChecksum());
 		final byte[] content = document.getContent();
 		final byte[] compressedContent =
 			CompressionUtil.compress(content, compressionLevel);
 		final String encodedContent = Base64.encodeBytes(compressedContent);
 		writer.setValue(encodedContent);
 		writer.endNode();											// </content>
 	}
 
 	/**
 	 * Write the version to the xml writer.
 	 * 
 	 * @param version
 	 *            The version to write.
 	 * @param writer
 	 *            The writer to write to.
 	 */
 	private void writeVersion(final DocumentVersion version,
 			final HierarchicalStreamWriter writer) {
 		final Document document = version.getDocument();
 		writer.startNode("version");
 		writer.addAttribute("versionId", version.getVersion());
 		writer.setValue(version.getMetaDataFile(
 				document.getMetaDataDirectory()).getAbsolutePath());
 		writer.endNode();		
 	}
 
 	/**
 	 * Write the document's versions to the xml writer.
 	 * 
 	 * @param versions
 	 *            The document versions to write.
 	 * @param writer
 	 *            The writer to write to.
 	 */
 	private void writeVersions(final Collection<DocumentVersion> versions,
 			final HierarchicalStreamWriter writer) {
 		writer.startNode("versions");
 		for(DocumentVersion version : versions) {
 			writeVersion(version, writer);
 		}
 		writer.endNode();
 	}
 }
