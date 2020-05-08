 package de.openms.knime.featureTextReader;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.List;
 
 import org.knime.core.data.DataTableSpec;
 import org.knime.core.data.uri.IURIPortObject;
 import org.knime.core.data.uri.URIContent;
import org.knime.core.data.uri.URIPortObject;
 import org.knime.core.node.BufferedDataTable;
 import org.knime.core.node.CanceledExecutionException;
 import org.knime.core.node.ExecutionContext;
 import org.knime.core.node.ExecutionMonitor;
 import org.knime.core.node.InvalidSettingsException;
 import org.knime.core.node.NodeLogger;
 import org.knime.core.node.NodeModel;
 import org.knime.core.node.NodeSettingsRO;
 import org.knime.core.node.NodeSettingsWO;
 import org.knime.core.node.port.PortObject;
 import org.knime.core.node.port.PortObjectSpec;
 import org.knime.core.node.port.PortType;
 
 import de.openms.knime.TextExporterParser;
 
 /**
  * This is the model implementation of FeatureTextReader. Reads files exported
  * b...
  * 
  * @author The OpenMS Team
  */
 public class FeatureTextReaderNodeModel extends NodeModel {
 
     // the logger instance
     private static final NodeLogger logger = NodeLogger
             .getLogger(FeatureTextReaderNodeModel.class);
 
     /**
      * Constructor for the node model.
      */
     protected FeatureTextReaderNodeModel() {
         super(new PortType[] { new PortType(IURIPortObject.class) },
                 new PortType[] { new PortType(BufferedDataTable.class) });
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected BufferedDataTable[] execute(final PortObject[] inObjects,
             final ExecutionContext exec) throws Exception {
 
        IURIPortObject obj = (URIPortObject) inObjects[0];
         List<URIContent> uris = obj.getURIContents();
         if (uris.size() == 0) {
             throw new Exception(
                     "No URI was supplied in URIPortObject at input port 0");
         } else if (uris.size() != 1) {
             throw new Exception(String.format(
                     "We can only demangle a single file but got %d.",
                     uris.size()));
         }
 
         URI relURI = uris.get(0).getURI();
         File cXMLFile = new File(relURI);
 
         TextExporterParser parser = new TextExporterParser("FEATURE");
         BufferedDataTable out = parser.parseFile(cXMLFile, exec);
 
         return new BufferedDataTable[] { out };
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void reset() {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
             throws InvalidSettingsException {
         return new DataTableSpec[] { null };
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void saveSettingsTo(final NodeSettingsWO settings) {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
             throws InvalidSettingsException {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void validateSettings(final NodeSettingsRO settings)
             throws InvalidSettingsException {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void loadInternals(final File internDir,
             final ExecutionMonitor exec) throws IOException,
             CanceledExecutionException {
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void saveInternals(final File internDir,
             final ExecutionMonitor exec) throws IOException,
             CanceledExecutionException {
     }
 
 }
