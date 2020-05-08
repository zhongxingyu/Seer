 /****************************************************************************
  * Copyright (C) 2011 GGA Software Services LLC
  *
  * This file may be distributed and/or modified under the terms of the
  * GNU General Public License version 3 as published by the Free Software
  * Foundation.
  *
  * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
  * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see <http://www.gnu.org/licenses>.
  ***************************************************************************/
 
 package com.ggasoftware.indigo.knime.common;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.knime.core.data.*;
 import org.knime.core.data.container.*;
 import org.knime.core.node.*;
 
 import com.ggasoftware.indigo.*;
 import com.ggasoftware.indigo.knime.IndigoNodeSettings;
 import com.ggasoftware.indigo.knime.IndigoNodeSettings.STRUCTURE_TYPE;
 import com.ggasoftware.indigo.knime.cell.IndigoDataValue;
 import com.ggasoftware.indigo.knime.cell.IndigoMolValue;
 import com.ggasoftware.indigo.knime.cell.IndigoReactionValue;
 import com.ggasoftware.indigo.knime.plugin.IndigoPlugin;
 
 public class IndigoSimpleNodeModel extends IndigoNodeModel
 {
 
    public static abstract class Transformer
    {
       public abstract void transform (IndigoObject io, boolean reaction);
    }
 
    // the logger instance
   private static final NodeLogger logger = NodeLogger
         .getLogger(IndigoSimpleNodeModel.class);
 
    private final IndigoSimpleSettings _settings;
    Transformer _transformer;
    String _message;
 
    public IndigoSimpleNodeModel(String message, IndigoSimpleSettings settings, Transformer transformer)
    {
       super(1, 1);
       _message = message;
       _transformer = transformer;
       _settings = settings;
    }
    
    public IndigoSimpleNodeModel(String message, Transformer transformer)
    {
       this(message, new IndigoSimpleSettings(), transformer);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute (final BufferedDataTable[] inData,
          final ExecutionContext exec) throws Exception
    {
 
       BufferedDataTable bufferedDataTable = inData[IndigoSimpleSettings.INPUT_PORT];
       _defineStructureType(bufferedDataTable.getDataTableSpec());
       
       ColumnRearranger crea = createRearranger(bufferedDataTable.getDataTableSpec());
 
       return new BufferedDataTable[] { exec.createColumnRearrangeTable(
             bufferedDataTable, crea, exec) };
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset ()
    {
    }
 
    class Converter implements CellFactory
    {
       int _colIndex;
       private final DataColumnSpec[] m_colSpec;
 
       Converter(final DataTableSpec inSpec, final DataColumnSpec cs, final IndigoSimpleSettings settings, final int colIndex) {
          _colIndex = colIndex;
 
          if (settings.appendColumn.getBooleanValue()) {
             m_colSpec = new DataColumnSpec[] { _createNewColumnSpec(DataTableSpec.getUniqueColumnName(inSpec, settings.newColName.getStringValue()),
                   settings.structureType) };
          } else {
             m_colSpec = new DataColumnSpec[] { _createNewColumnSpec(settings.colName.getStringValue(), settings.structureType) };
          }
       }
 
       public DataCell getCell (final DataRow row)
       {
          DataCell cell = row.getCell(_colIndex);
          if (cell.isMissing())
             return cell;
          else
          {
             try
             {
                IndigoPlugin.lock();
                
                IndigoDataValue iv = (IndigoDataValue) cell;
                IndigoObject io = iv.getIndigoObject().clone();
                /*
                 * Transform object
                 */
                _transformer.transform(io, _settings.structureType.equals(STRUCTURE_TYPE.Reaction));
                
                return _createNewDataCell(io, _settings.structureType);
             }
             catch (IndigoException ex)
             {
               logger.error("Could not " + _message + " for " + row.getKey() + 
                     ": " + ex.getMessage(), ex);
                return DataType.getMissingCell();
             }
             finally
             {
                IndigoPlugin.unlock();
             }
          }
       }
 
       @Override
       public DataCell[] getCells (DataRow row)
       {
          return new DataCell[] { getCell(row) };
       }
 
       @Override
       public DataColumnSpec[] getColumnSpecs ()
       {
          return m_colSpec;
       }
 
       @Override
       public void setProgress (int curRowNr, int rowCount, RowKey lastKey,
             ExecutionMonitor exec)
       {
          exec.setProgress((double)curRowNr / rowCount);
       }
    }
 
    private ColumnRearranger createRearranger (final DataTableSpec inSpec)
    {
       ColumnRearranger crea = new ColumnRearranger(inSpec);
 
       DataColumnSpec cs;
       if (_settings.appendColumn.getBooleanValue())
       {
          String name = DataTableSpec.getUniqueColumnName(inSpec,
                _settings.newColName.getStringValue());
          cs = _createNewColumnSpec(name, _settings.structureType);
       }
       else
       {
          cs = _createNewColumnSpec(_settings.colName.getStringValue(), _settings.structureType);
       }
 
       Converter conv = new Converter(inSpec, cs, _settings,
             inSpec.findColumnIndex(_settings.colName.getStringValue()));
 
       if (_settings.appendColumn.getBooleanValue())
       {
          crea.append(conv);
       }
       else
       {
          crea.replace(conv, _settings.colName.getStringValue());
       }
 
       return crea;
    }
    
    private STRUCTURE_TYPE _defineStructureType(DataTableSpec tSpec) {
       STRUCTURE_TYPE stype = IndigoNodeSettings.getStructureType(tSpec, _settings.colName.getStringValue());
       _settings.structureType = stype;
       return stype;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure (final DataTableSpec[] inSpecs)
          throws InvalidSettingsException
    {
       DataTableSpec inSpec = inSpecs[IndigoSimpleSettings.INPUT_PORT];
       
       searchMixedIndigoColumn(inSpec, _settings.colName, IndigoMolValue.class, IndigoReactionValue.class);
       
       STRUCTURE_TYPE stype = _defineStructureType(inSpec);
       
       if(stype.equals(STRUCTURE_TYPE.Unknown)) 
          throw new InvalidSettingsException("can not define structure type: reaction or molecule columns");
       /*
        * Set loading parameters warning message
        */
       if(_settings.warningMessage != null) {
          setWarningMessage(_settings.warningMessage);
       }
       
       return new DataTableSpec[] { createRearranger(inSpec).createSpec() };
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo (final NodeSettingsWO settings)
    {
       _settings.saveSettingsTo(settings);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom (final NodeSettingsRO settings)
          throws InvalidSettingsException
    {
       _settings.loadSettingsFrom(settings);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings (final NodeSettingsRO settings)
          throws InvalidSettingsException
    {
       IndigoSimpleSettings s = new IndigoSimpleSettings();
       s.loadSettingsFrom(settings);
       if (s.colName.getStringValue() == null || s.colName.getStringValue().length() < 1)
          throw new InvalidSettingsException("No column name given");
       
       if (s.appendColumn.getBooleanValue() && ((s.newColName.getStringValue() == null) || (s.newColName.getStringValue().length() < 1)))
          throw new InvalidSettingsException("No name for new column given");
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals (final File internDir,
          final ExecutionMonitor exec) throws IOException,
          CanceledExecutionException
    {
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals (final File internDir,
          final ExecutionMonitor exec) throws IOException,
          CanceledExecutionException
    {
    }
 }
