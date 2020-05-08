 
 package org.fcrepo.sequencer.archive;
 
 import static com.google.common.base.Throwables.propagate;
 import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
 import static org.modeshape.jcr.api.JcrConstants.JCR_DATA;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.jcr.Binary;
 import javax.jcr.NamespaceRegistry;
 import javax.jcr.Node;
 import javax.jcr.Property;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import org.apache.commons.compress.archivers.ArchiveEntry;
 import org.apache.commons.compress.archivers.ArchiveException;
 import org.apache.commons.compress.archivers.ArchiveInputStream;
 import org.apache.commons.compress.archivers.ArchiveStreamFactory;
 import org.fcrepo.Datastream;
 import org.fcrepo.exception.InvalidChecksumException;
 import org.modeshape.jcr.api.nodetype.NodeTypeManager;
 import org.modeshape.jcr.api.sequencer.Sequencer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ArchiveSequencer extends Sequencer {
 
     private static final Logger LOG = LoggerFactory
             .getLogger(ArchiveSequencer.class);
 
     public ArchiveSequencer() {
     }
 
     @Override
     public void initialize(final NamespaceRegistry registry,
             final NodeTypeManager nodeTypeManager) throws RepositoryException,
             IOException {
         super.initialize(registry, nodeTypeManager);
         LOG.trace("ArchiveSequencer initialized");
     }
 
     @Override
     public boolean execute(final Property inputProperty, final Node outputNode,
             final Context context) throws RepositoryException, IOException,
             ArchiveException, InvalidChecksumException {
         LOG.debug("Sequencing property change: \"{}\", expecting \"{}\"",
                 inputProperty.getName(), JCR_DATA);
         if (JCR_DATA.equals(inputProperty.getName())) {
             final Binary inputBinary = inputProperty.getBinary();
             try (final InputStream in = inputBinary.getStream()) {
                 final String contents = listContents(in);
                 LOG.debug("contents: '" + contents + "'");
                 if (contents != null && !contents.trim().equals("")) {
                     // saving contents to a new datastream
                     final String outPath =
                             outputNode.getPath() + "_archiveContents";
                     LOG.debug("outPath: " + outPath);
                     final Session session = outputNode.getSession();
                     final Datastream ds = new Datastream(session, outPath);
                     ds.setContent(
                             new ByteArrayInputStream(contents.getBytes()),
                            TEXT_PLAIN, null, null);
                     session.save();
                     LOG.debug("Sequenced output node at path: {}", outPath);
                     return true;
                 } else {
                     LOG.warn("Empty contents in archive at: {}", inputProperty
                             .getPath());
                 }
             }
         }
         LOG.debug("{} was not a {} ", inputProperty.getName(), JCR_DATA);
         return false;
     }
 
     public static String listContents(final InputStream in) throws IOException,
             ArchiveException {
         final ArchiveStreamFactory factory = new ArchiveStreamFactory();
         try (ArchiveInputStream arc =
                 factory.createArchiveInputStream(new BufferedInputStream(in))) {
             final StringBuffer contents = new StringBuffer();
             for (ArchiveEntry entry = null; (entry = arc.getNextEntry()) != null;) {
                 contents.append(entry.getName());
                 contents.append("\n");
             }
             return contents.toString();
         } catch (final Exception ex) {
             LOG.error("Error parsing archive input", ex);
             throw propagate(ex);
         }
 
     }
 }
