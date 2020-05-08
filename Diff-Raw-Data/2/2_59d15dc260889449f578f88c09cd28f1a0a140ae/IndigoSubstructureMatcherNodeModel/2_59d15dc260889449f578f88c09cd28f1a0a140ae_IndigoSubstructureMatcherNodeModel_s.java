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
 
 package com.ggasoftware.indigo.knime.submatcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.knime.core.data.*;
 import org.knime.core.data.def.DefaultRow;
 import org.knime.core.data.def.IntCell;
 import org.knime.core.data.def.StringCell;
 import org.knime.core.node.*;
 
 import com.ggasoftware.indigo.*;
 import com.ggasoftware.indigo.knime.IndigoNodeSettings;
 import com.ggasoftware.indigo.knime.IndigoNodeSettings.STRUCTURE_TYPE;
 import com.ggasoftware.indigo.knime.cell.IndigoDataCell;
 import com.ggasoftware.indigo.knime.cell.IndigoDataValue;
 import com.ggasoftware.indigo.knime.cell.IndigoMolValue;
 import com.ggasoftware.indigo.knime.cell.IndigoQueryMolValue;
 import com.ggasoftware.indigo.knime.cell.IndigoQueryReactionValue;
 import com.ggasoftware.indigo.knime.cell.IndigoReactionValue;
 import com.ggasoftware.indigo.knime.common.IndigoNodeModel;
 import com.ggasoftware.indigo.knime.plugin.IndigoPlugin;
 import com.ggasoftware.indigo.knime.submatcher.IndigoSubstructureMatcherSettings.MoleculeMode;
 import com.ggasoftware.indigo.knime.submatcher.IndigoSubstructureMatcherSettings.ReactionMode;
 
 public class IndigoSubstructureMatcherNodeModel extends IndigoNodeModel
 {
    public static final int TARGET_PORT = 0;
    public static final int QUERY_PORT = 1;
    
    IndigoSubstructureMatcherSettings _settings = new IndigoSubstructureMatcherSettings();
 
    private static final NodeLogger LOGGER = NodeLogger.getLogger(IndigoSubstructureMatcherNodeModel.class);
    
    /**
     * Constructor for the node model.
     */
    protected IndigoSubstructureMatcherNodeModel()
    {
       super(2, 2);
    }
    
    protected DataTableSpec getDataTableSpec (DataTableSpec inputTableSpec) throws InvalidSettingsException
    {
       if (_settings.appendColumn.getBooleanValue())
          if (_settings.newColName.getStringValue().length() < 1)
             throw new InvalidSettingsException("New column name must be specified");
       
       DataColumnSpec[] specs;
       
       int columnsCount = inputTableSpec.getNumColumns();
       if (_settings.appendColumn.getBooleanValue())
          columnsCount++;
       if (_settings.appendQueryKeyColumn.getBooleanValue())
          columnsCount++;
       if (_settings.appendQueryMatchCountKeyColumn.getBooleanValue())
          columnsCount++;
       
       specs = new DataColumnSpec[columnsCount];
 
       int i;
       
       for (i = 0; i < inputTableSpec.getNumColumns(); i++)
          specs[i] = inputTableSpec.getColumnSpec(i);
       
       if (_settings.appendColumn.getBooleanValue())
          specs[i++] = _createNewColumnSpec(_settings.newColName.getStringValue(), _settings.structureType);
       if (_settings.appendQueryKeyColumn.getBooleanValue())
          specs[i++] = new DataColumnSpecCreator(_settings.queryKeyColumn.getStringValue(), StringCell.TYPE).createSpec();
       if (_settings.appendQueryMatchCountKeyColumn.getBooleanValue())
          specs[i++] = new DataColumnSpecCreator(_settings.queryMatchCountKeyColumn.getStringValue(), IntCell.TYPE).createSpec();
       
       return new DataTableSpec(specs);
    }
    
 
    private QueryWithData[] _loadQueries(BufferedDataTable queriesTableData)
          throws InvalidSettingsException
    {
       DataTableSpec queryItemsSpec = queriesTableData.getDataTableSpec();
 
       int queryColIdx = _settings.getQueryColIdx(queryItemsSpec);
 
       QueryWithData[] queries = new QueryWithData[queriesTableData.getRowCount()];
       
       if (queries.length == 0)
          LOGGER.warn("There are no query molecules in the table");
 
       int index = 0;
       boolean warningPrinted = false;
       
       for(DataRow row : queriesTableData) {
          
          queries[index] = new QueryWithData(row, queryColIdx, _settings.structureType.equals(STRUCTURE_TYPE.Reaction));
          
          if (queries[index].query == null && !warningPrinted) {
             LOGGER.warn("query table contains missing cells");
             warningPrinted = true;
          }
          index++;
       }
       return queries;
    }
 
    private IndigoObject getIndigoQueryStructureOrNull(DataCell cell) {
       if (cell.isMissing())
          return null;
       return ((IndigoDataValue)cell).getIndigoObject();
    }
    
    class AlignTargetQueryData
    {
       final IndigoObject query;
       boolean first = true;
       int natoms_align = 0;
       int[] atoms = null;
       float[] xyz = null;
       
       public AlignTargetQueryData(IndigoObject mol) {
          query = mol;
       }
 
       public void align (IndigoObject target, IndigoObject match)
       {
          int i = 0;
          
          if (!target.hasCoord())
             target.layout();
 
          if (first)
          {
             for (IndigoObject atom : query.iterateAtoms())
             {
                IndigoObject mapped = match.mapAtom(atom);
                if (mapped != null && (mapped.isPseudoatom() || mapped.atomicNumber() != 1))
                   natoms_align++;
             }
             if (natoms_align > 1)
             {
                atoms = new int[natoms_align];
                xyz = new float[natoms_align * 3];
                
                for (IndigoObject atom : query.iterateAtoms())
                {
                   IndigoObject mapped = match.mapAtom(atom);
                   
                   IndigoObject atomForAlign;
                   if (_settings.alignByQuery.getBooleanValue())
                      atomForAlign = atom;
                   else 
                      atomForAlign = mapped;
                      
                   if (mapped != null && (mapped.isPseudoatom() || mapped.atomicNumber() != 1))
                   {
                      atoms[i] = mapped.index();
                      System.arraycopy(atomForAlign.xyz(), 0, xyz, i++ * 3, 3);
                   }
                }
                if (_settings.alignByQuery.getBooleanValue())
                   target.alignAtoms(atoms, xyz);
             }
             first = false;
          }
          else
          {
             if (atoms != null)
             {
                for (IndigoObject atom : query.iterateAtoms())
                {
                   IndigoObject mapped = match.mapAtom(atom);
                   if (mapped != null && (mapped.isPseudoatom() || mapped.atomicNumber() != 1))
                      atoms[i++] = mapped.index();
                }
 
                target.alignAtoms(atoms, xyz);
             }
          }
       }
    }
    
    class QueryWithData
    {
       IndigoObject query;
       String rowKey;
       final ArrayList<AlignTargetQueryData> alignData = new ArrayList<AlignTargetQueryData>();
       private final boolean _reaction; 
       
       public QueryWithData(DataRow row, int colIdx, boolean reaction) {
          _reaction = reaction;
          query = getIndigoQueryStructureOrNull(row.getCell(colIdx));
          rowKey = row.getKey().toString();
          /*
           * Prepare align data
           */
          if (query != null) {
             if (_reaction) {
                for(IndigoObject mol : query.iterateMolecules())
                   alignData.add(new AlignTargetQueryData(mol));
             } else {
                alignData.add(new AlignTargetQueryData(query));
             }
          }
       }
       public void align (IndigoObject target, IndigoObject match) {
          for (AlignTargetQueryData qdata : alignData) {
             if (_reaction)
                target = match.mapMolecule(qdata.query);
             qdata.align(target, match);
          }
       }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute (final BufferedDataTable[] inData,
          final ExecutionContext exec) throws Exception
    {
       DataTableSpec inputTableSpec = inData[TARGET_PORT].getDataTableSpec();
       DataTableSpec queryTableSpec = inData[QUERY_PORT].getDataTableSpec();
       
       _defineStructureType(inputTableSpec, queryTableSpec);
       
       BufferedDataContainer validOutputContainer = exec
             .createDataContainer(getDataTableSpec(inputTableSpec));
       
       BufferedDataContainer invalidOutputContainer = exec
             .createDataContainer(inputTableSpec);
 
       int colIdx = _settings.getTargetColIdx(inputTableSpec);
 
       QueryWithData[] queries = _loadQueries(inData[QUERY_PORT]);
       
       int rowNumber = 1;
       boolean warningPrinted = false;
       
       for (DataRow inputRow : inData[TARGET_PORT]) {
          DataCell inputCell = inputRow.getCell(colIdx);
          
          boolean hasMatch = true;
          int matchCount = 0;
          
          if(inputCell.isMissing()) {
             if(!warningPrinted)
                LOGGER.warn("target table contains missing cells");
             warningPrinted = true;
             hasMatch = false;
          }
          IndigoObject target = null;
          StringBuilder queriesRowKey = new StringBuilder();
          /*
           * Count matches
           */
          if (hasMatch) {
             target = ((IndigoDataCell) inputCell).getIndigoObject();
             /*
              * Clone target
              */
             try {
                IndigoPlugin.lock();
                target = target.clone();
             } finally {
                IndigoPlugin.unlock();
             }
             matchCount = _getMatchCount(target, queries, inputRow.getKey().getString(), queriesRowKey);
 
             /*
              *  Check matchCount
              */
             if (_settings.matchAnyAtLeastSelected.getBooleanValue())
                hasMatch = (matchCount >= _settings.matchAnyAtLeast.getIntValue());
             if (_settings.matchAllSelected.getBooleanValue())
                hasMatch = (matchCount == queries.length);
          }
          /*
           * Create output
           */
          if (hasMatch) {
             int columnsCount = inputRow.getNumCells();
             if (_settings.appendColumn.getBooleanValue())
                columnsCount++;
             if (_settings.appendQueryKeyColumn.getBooleanValue())
                columnsCount++;
             if (_settings.appendQueryMatchCountKeyColumn.getBooleanValue())
                columnsCount++;
             
             DataCell[] cells = new DataCell[columnsCount];
             int i;
 
             for (i = 0; i < inputRow.getNumCells(); i++) {
                if (!_settings.appendColumn.getBooleanValue() && i == colIdx)
                   cells[i] = _createNewDataCell(target, _settings.structureType);
                else
                   cells[i] = inputRow.getCell(i);
             }
             if (_settings.appendColumn.getBooleanValue())
                cells[i++] = _createNewDataCell(target, _settings.structureType);
             if (_settings.appendQueryKeyColumn.getBooleanValue())
                cells[i++] = new StringCell(queriesRowKey.toString());
             if (_settings.appendQueryMatchCountKeyColumn.getBooleanValue())
                cells[i++] = new IntCell(matchCount);
             
             validOutputContainer.addRowToTable(new DefaultRow(inputRow
                   .getKey(), cells));
          } else {
             invalidOutputContainer.addRowToTable(inputRow);
          }
          
          exec.checkCanceled();
          exec.setProgress(rowNumber / (double) inData[TARGET_PORT].getRowCount(),
                "Processing row " + rowNumber);
          rowNumber++;
       }
 
       validOutputContainer.close();
       invalidOutputContainer.close();
       return new BufferedDataTable[] { validOutputContainer.getTable(),
             invalidOutputContainer.getTable() };
    }
 
    private int _getMatchCount(IndigoObject target, QueryWithData[] queries, String inputRowKey, StringBuilder queriesRowKey) {
       int matchCount = 0;
       
       for (QueryWithData query : queries) {
          if (query.query == null)
             continue;
 
          try {
             IndigoPlugin.lock();
             if (_matchTarget(query, target)) {
                matchCount++;
                if (queriesRowKey.length() > 0)
                   queriesRowKey.append(", ");
                
                queriesRowKey.append(query.rowKey);
             }
          } catch (IndigoException e) {
             LOGGER.warn("indigo error while matching: target key='" + inputRowKey + "' query key='" + query.rowKey + "': " + e.getMessage());
          } finally {
             IndigoPlugin.unlock();
          }
       }
       return matchCount;
    }
 
    private boolean _matchTarget(QueryWithData queryData, IndigoObject target) {
       switch (_settings.structureType) {
       case Molecule:
          return _matchMoleculeTarget(queryData, target);
       case Reaction:
          return _matchReactionTarget(queryData, target);
       case Unknown:
          throw new RuntimeException("Structure type is not defined");
       }
       return false;
    }
 
    private boolean _matchReactionTarget(QueryWithData queryData, IndigoObject target) {
 
       IndigoObject match = null;
       String mode = "";
       
       IndigoObject query = queryData.query;
       Indigo indigo = IndigoPlugin.getIndigo();
       
       if(_settings.mode.getStringValue().equals(ReactionMode.DaylightAAM.toString()))
          mode = "Daylight-AAM";
 
       match = indigo.substructureMatcher(target, mode).match(query);
       
       if (match != null && _settings.exact.getBooleanValue()) {
          // test that the target does not have unmapped heavy atoms
          int nmapped_heavy = 0;
          int target_heavy = 0;
          
          for (IndigoObject mol : query.iterateMolecules()) {
             for (IndigoObject atom : mol.iterateAtoms()) {
                IndigoObject mapped = match.mapAtom(atom);
                if (mapped != null)
                   if (mapped.isRSite() || mapped.isPseudoatom() || mapped.atomicNumber() > 1)
                      nmapped_heavy++;
             }
          }
 
          for (IndigoObject mol : target.iterateMolecules()) 
             target_heavy += mol.countHeavyAtoms();
          
          if (nmapped_heavy < target_heavy)
             match = null;
       }
       if (match != null) {
          if (_settings.highlight.getBooleanValue()) {
             for (IndigoObject mol : query.iterateMolecules()) {
                for (IndigoObject atom : mol.iterateAtoms()) {
                   IndigoObject mapped = match.mapAtom(atom);
                   if (mapped != null)
                      mapped.highlight();
                }
                for (IndigoObject bond : mol.iterateBonds()) {
                   IndigoObject mapped = match.mapBond(bond);
                   if (mapped != null)
                      mapped.highlight();
                }
             }
          }
          if (_settings.align.getBooleanValue())
             queryData.align(target, match);
       }
       
       
       return (match != null);
    }
 
    private boolean _matchMoleculeTarget(QueryWithData queryData, IndigoObject target)
    {
       Indigo indigo = IndigoPlugin.getIndigo();
       IndigoObject query = queryData.query;
       
       String mode = "";
       
       IndigoObject match = null;
       
       if (!_settings.exact.getBooleanValue() || target.countHeavyAtoms() <= query.countAtoms())
       {
          if (_settings.mode.getStringValue().equals(MoleculeMode.Resonance.toString()))
             mode = "RES";
         else if (_settings.mode.getStringValue().equals(MoleculeMode.Tautomer.ordinal()))
          {
             mode = "TAU R* R-C";
  
             indigo.clearTautomerRules();
             indigo.setTautomerRule(1, "N,O,P,S,As,Se,Sb,Te", "N,O,P,S,As,Se,Sb,Te");
             indigo.setTautomerRule(2, "0C", "N,O,P,S");
             indigo.setTautomerRule(3, "1C", "N,O");
          }
       
          match = indigo.substructureMatcher(target, mode).match(query);
       }
       
       if (match != null && _settings.exact.getBooleanValue())
       {
          // test that the target does not have unmapped heavy atoms
          int nmapped_heavy = 0;
          
          for (IndigoObject atom : query.iterateAtoms())
          {
             IndigoObject mapped = match.mapAtom(atom);
             if (mapped != null)
                if (mapped.isRSite() || mapped.isPseudoatom() || mapped.atomicNumber() > 1)
                   nmapped_heavy++;
          }
          
          if (nmapped_heavy < target.countHeavyAtoms())
             match = null;
       }
       
       if (match != null)
       {
          if (_settings.highlight.getBooleanValue())
          {
             for (IndigoObject atom : query.iterateAtoms())
             {
                IndigoObject mapped = match.mapAtom(atom);
                if (mapped != null)
                   mapped.highlight();
             }
             for (IndigoObject bond : query.iterateBonds())
             {
                IndigoObject mapped = match.mapBond(bond);
                if (mapped != null)
                   mapped.highlight();
             }
          }
          
          if (_settings.align.getBooleanValue())
             queryData.align(target, match);
       }
       return (match != null);
    }
    
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset ()
    {
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure (final DataTableSpec[] inSpecs)
          throws InvalidSettingsException
    {
       searchMixedIndigoColumn(inSpecs[TARGET_PORT], _settings.targetColName, IndigoMolValue.class, IndigoReactionValue.class);
       searchMixedIndigoColumn(inSpecs[QUERY_PORT], _settings.queryColName, IndigoQueryMolValue.class, IndigoQueryReactionValue.class);
       
       STRUCTURE_TYPE stype = _defineStructureType(inSpecs[TARGET_PORT], inSpecs[QUERY_PORT]);
       if(stype.equals(STRUCTURE_TYPE.Unknown)) 
          throw new InvalidSettingsException("can not define structure type: reaction or molecule columns");
       
       /*
        * Set loading parameters warning message
        */
       if(_settings.warningMessage != null) {
          setWarningMessage(_settings.warningMessage);
       }
       
       return new DataTableSpec[] { null, null };
    }
 
    private STRUCTURE_TYPE _defineStructureType(DataTableSpec tSpec, DataTableSpec qSpec) {
       STRUCTURE_TYPE stype = IndigoNodeSettings.getStructureType(tSpec, qSpec,
             _settings.targetColName.getStringValue(), _settings.queryColName.getStringValue());
       _settings.structureType = stype;
       return stype;
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
       IndigoSubstructureMatcherSettings s = new IndigoSubstructureMatcherSettings();
       s.loadSettingsFrom(settings);
 
       if (s.targetColName.getStringValue() == null || s.targetColName.getStringValue().length() < 1)
          throw new InvalidSettingsException("column name must be specified");
       if (s.queryColName.getStringValue() == null || s.queryColName.getStringValue().length() < 1)
          throw new InvalidSettingsException("query column name must be specified");
       if (s.appendColumn.getBooleanValue() && (s.newColName.getStringValue() == null || s.newColName.getStringValue().length() < 1))
          throw new InvalidSettingsException("new column name must be specified");
       if (s.appendQueryKeyColumn.getBooleanValue() && (s.queryKeyColumn.getStringValue() == null || s.queryKeyColumn.getStringValue().length() < 1))
          throw new InvalidSettingsException("query key column name must be specified");
       if (s.appendQueryMatchCountKeyColumn.getBooleanValue() && (s.queryMatchCountKeyColumn.getStringValue() == null || s.queryMatchCountKeyColumn.getStringValue().length() < 1))
          throw new InvalidSettingsException("query match count column name must be specified");
       if (!s.matchAllSelected.getBooleanValue() && !s.matchAnyAtLeastSelected.getBooleanValue())
          throw new InvalidSettingsException("At least one match option should be selected: match any or match all");
       if (s.appendColumn.getBooleanValue() && !s.highlight.getBooleanValue() && !s.align.getBooleanValue())
          throw new InvalidSettingsException("without highlighting or alignment, appending new column makes no sense");
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
