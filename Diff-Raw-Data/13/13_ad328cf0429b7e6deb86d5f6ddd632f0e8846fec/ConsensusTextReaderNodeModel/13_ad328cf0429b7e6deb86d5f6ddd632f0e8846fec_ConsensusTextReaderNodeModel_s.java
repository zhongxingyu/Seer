 /**
  * --------------------------------------------------------------------------
  *                   OpenMS -- Open-Source Mass Spectrometry
  * --------------------------------------------------------------------------
  * Copyright The OpenMS Team -- Eberhard Karls University Tuebingen,
  * ETH Zurich, and Freie Universitaet Berlin 2002-2013.
  * 
  * This software is released under a three-clause BSD license:
  *  * Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of any author or any participating institution
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  * For a full list of authors, refer to the file AUTHORS.
  * --------------------------------------------------------------------------
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL ANY OF THE AUTHORS OR THE CONTRIBUTING
  * INSTITUTIONS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package de.openms.knime.consensusTextReader;
 
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
  * This is the model implementation of ConsensusTextReader.
  * 
  * 
  * @author Stephan Aiche and the OpenMS Team
  */
 public class ConsensusTextReaderNodeModel extends NodeModel {
 
     // the logger instance
     private static final NodeLogger logger = NodeLogger
             .getLogger(ConsensusTextReaderNodeModel.class);
 
     /**
      * Constructor for the node model.
      */
     protected ConsensusTextReaderNodeModel() {
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
 
         TextExporterParser parser = new TextExporterParser("CONSENSUS");
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
