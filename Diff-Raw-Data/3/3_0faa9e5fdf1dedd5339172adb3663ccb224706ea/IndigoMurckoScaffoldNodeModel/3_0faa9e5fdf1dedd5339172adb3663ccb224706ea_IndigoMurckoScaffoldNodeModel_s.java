 package com.ggasoftware.indigo.knime.murcko;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.knime.core.data.*;
 import org.knime.core.data.container.CloseableRowIterator;
 import org.knime.core.data.def.DefaultRow;
 import org.knime.core.node.*;
 
 import com.ggasoftware.indigo.IndigoException;
 import com.ggasoftware.indigo.IndigoObject;
 import com.ggasoftware.indigo.knime.cell.IndigoMolCell;
 import com.ggasoftware.indigo.knime.cell.IndigoMolValue;
 import com.ggasoftware.indigo.knime.common.IndigoNodeModel;
 import com.ggasoftware.indigo.knime.plugin.IndigoPlugin;
 
 public class IndigoMurckoScaffoldNodeModel extends IndigoNodeModel
 {
    private IndigoMurckoScaffoldSettings _settings = new IndigoMurckoScaffoldSettings();
 
    private static final NodeLogger LOGGER = NodeLogger.getLogger(IndigoMurckoScaffoldNodeModel.class);
    
    protected DataTableSpec getDataTableSpec (DataTableSpec inputTableSpec) throws InvalidSettingsException
    {
       if (_settings.appendColumn)
          if (_settings.newColName == null || _settings.newColName.length() < 1)
             throw new InvalidSettingsException("New column name must be specified");
       
       DataColumnSpec[] specs;
       
       if (_settings.appendColumn)
          specs = new DataColumnSpec[inputTableSpec.getNumColumns() + 1];
       else
          specs = new DataColumnSpec[inputTableSpec.getNumColumns()];
 
       int i;
       
       for (i = 0; i < inputTableSpec.getNumColumns(); i++)
          specs[i] = inputTableSpec.getColumnSpec(i);
       
       if (_settings.appendColumn)
          specs[i] = new DataColumnSpecCreator(_settings.newColName, IndigoMolCell.TYPE).createSpec();
       
       return new DataTableSpec(specs);
    }
    
    protected void calculateMurckoScaffold (IndigoObject mol)
    {
       int natoms;
    
       do // have to to the mess in a loop because of tricky CC(C)C case and such
       {
          natoms = mol.countAtoms();
          ArrayList<IndigoObject> hanging_atoms = new ArrayList<IndigoObject>();
          
          for (IndigoObject atom : mol.iterateAtoms())
             if (atom.degree() <= 1)
                hanging_atoms.add(atom);
          
          while (hanging_atoms.size() > 0)
          {
             ArrayList<IndigoObject> to_remove = new ArrayList<IndigoObject>();
             ArrayList<IndigoObject> hanging_next = new ArrayList<IndigoObject>();
             
             for (IndigoObject atom : hanging_atoms)
             {
                if (atom.degree() == 0)
                   to_remove.add(atom);
                else
                {
                   IndigoObject nei = atom.iterateNeighbors().next();
                   
                   if (nei.degree() <= 2 || nei.bond().bondOrder() == 1)
                      to_remove.add(atom);
                   else
                   {
                      if (!hanging_next.contains(atom))
                         hanging_next.add(atom);
                   }
                }
             }
    
             for (IndigoObject atom : to_remove)
             {
                if (atom.degree() > 0)
                {
                   IndigoObject nei = atom.iterateNeighbors().next();
                   
                   if (nei.degree() == 2)
                   {
                      boolean found = false;
                      
                      for (IndigoObject a : to_remove)
                         if (a.index() == nei.index())
                         {
                            found = true;
                            break;
                         }
                      
                      if (!found)
                      {
                         for (IndigoObject a : hanging_next)
                            if (a.index() == nei.index())
                            {
                               found = true;
                               break;
                            }
                         if (!found)
                            hanging_next.add(nei);
                      }
                   }
                }
             }
             
             if (to_remove.isEmpty())
                break;
             
             for (IndigoObject atom : to_remove)
                atom.remove();
             
             hanging_atoms = hanging_next;
          }
       } while (natoms > mol.countAtoms());
    }
    
    protected boolean removeTerminalRing (IndigoObject mol)
    {
       if (!_settings.removeTerminalRings3 && !_settings.removeTerminalRings4)
          return false;
       
       for (IndigoObject ring : mol.iterateSSSR())
       {
          int nsubst = 0;
          boolean ok = true;
          
          if (_settings.removeTerminalRings3 && ring.countAtoms() == 3)
             ;
          else if (_settings.removeTerminalRings4 && ring.countAtoms() == 4)
             ;
          else
             continue;
          
          for (IndigoObject atom : ring.iterateAtoms())
          {
             if (atom.degree() == 3)
                nsubst++;
             else if (atom.degree() > 3)
             {
                ok = false;
                break;
             }
          }
               
          if (ok && nsubst <= 1)
          {
             for (IndigoObject atom : ring.iterateAtoms())
                atom.remove();
             return true;
          }
       }
       return false;
    }
    
    protected boolean removeO2Group (IndigoObject mol)
    {
       IndigoObject query = mol.getIndigo().loadQueryMolecule("[*D1]=[*]=[*D1]");
       IndigoObject match = mol.getIndigo().substructureMatcher(mol).match(query);
       
       if (match != null && match.mapAtom(query.getAtom(1)).degree() < 4)
       {
          match.mapAtom(query.getAtom(0)).remove();
          match.mapAtom(query.getAtom(2)).remove();
          return true;
       }
       return false;
    }
    
    protected boolean removeOfromNO (IndigoObject mol)
    {
       IndigoObject query = mol.getIndigo().loadQueryMolecule("[OD1]=[N+0]");
       IndigoObject match = mol.getIndigo().substructureMatcher(mol).match(query);
       
       if (match != null)
       {
          match.mapAtom(query.getAtom(0)).remove();
          return true;
       }
       
       query = mol.getIndigo().loadQueryMolecule("[O-D1]-[N+]");
       match = mol.getIndigo().substructureMatcher(mol).match(query);
       
       if (match != null)
       {
          int h = match.mapAtom(query.getAtom(1)).countImplicitHydrogens();
          match.mapAtom(query.getAtom(0)).remove();
          match.mapAtom(query.getAtom(1)).resetCharge();
          match.mapAtom(query.getAtom(1)).setImplicitHCount(h);
          return true;
       }
       
       return false;
    }
    
    /**
     * Constructor for the node model.
     */
    protected IndigoMurckoScaffoldNodeModel()
    {
       super(1, 1);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
          final ExecutionContext exec) throws Exception
    {
       DataTableSpec inputTableSpec = inData[0].getDataTableSpec();
 
       BufferedDataContainer outputContainer = exec.createDataContainer(getDataTableSpec(inputTableSpec));
 
       int colIdx = inputTableSpec.findColumnIndex(_settings.colName);
 
       if (colIdx == -1)
          throw new Exception("column not found");
       
       CloseableRowIterator it = inData[0].iterator();
       int rowNumber = 1;
 
       while (it.hasNext())
       {
          DataRow inputRow = it.next();
          IndigoObject target;
          DataCell cell = inputRow.getCell(colIdx); 
          
          if (cell.isMissing())
             target = null;
          else
          {
             target = ((IndigoMolCell)(inputRow.getCell(colIdx))).getIndigoObject();
    
             try
             {
                IndigoPlugin.lock();
                target = target.clone();
                while (true)
                {
                   if (removeOfromNO(target))
                      continue;
                   
                   calculateMurckoScaffold(target);
                   
                   if (removeTerminalRing(target))
                      continue;
                   
                   if (removeO2Group(target))
                      continue;
                   
                   break;
                }
             }
             catch (IndigoException ex)
             {
                LOGGER.error("Exception during processing row " + 
                      inputRow.getKey() + ": " + ex.getMessage() + 
                      ". Replaced with missing cell.", ex);
                cell = DataType.getMissingCell();
                target = null;
             }
             finally
             {
                IndigoPlugin.unlock();
             }
          }
          
          if (_settings.appendColumn)
          {
             DataCell[] cells = new DataCell[inputRow.getNumCells() + 1];
             int i;
             
             for (i = 0; i < inputRow.getNumCells(); i++)
                cells[i] = inputRow.getCell(i);
             if (target == null)
                cells[i] = cell;
            cells[i] = new IndigoMolCell(target);
             outputContainer.addRowToTable(new DefaultRow(inputRow.getKey(), cells));
          }
          else
          {
             DataCell[] cells = new DataCell[inputRow.getNumCells()];
             int i;
             
             for (i = 0; i < inputRow.getNumCells(); i++)
             {
                if (i == colIdx)
                {
                   if (target == null)
                      cells[i] = cell;
                   else
                      cells[i] = new IndigoMolCell(target);
                }
                else
                   cells[i] = inputRow.getCell(i);
             }
             outputContainer.addRowToTable(new DefaultRow(inputRow.getKey(), cells));
          }
          exec.checkCanceled();
          exec.setProgress(rowNumber / (double) inData[0].getRowCount(),
                "Adding row " + rowNumber);
          rowNumber++;
       }
 
       outputContainer.close();
       return new BufferedDataTable[] { outputContainer.getTable() };
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset()
    {
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
          throws InvalidSettingsException
    {
       _settings.colName = searchIndigoColumn(inSpecs[0], _settings.colName, IndigoMolValue.class);
       return new DataTableSpec[] { getDataTableSpec(inSpecs[0]) };
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
    {
       _settings.saveSettings(settings);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
          throws InvalidSettingsException
    {
       _settings.loadSettings(settings);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
          throws InvalidSettingsException
    {
       IndigoMurckoScaffoldSettings s = new IndigoMurckoScaffoldSettings();
       s.loadSettings(settings);
       if (s.colName == null || s.colName.length() < 1)
          throw new InvalidSettingsException("column name must be specified");
       if (s.appendColumn && (s.newColName == null || s.newColName.length() < 1))
          throw new InvalidSettingsException("new column name must be specified");
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
          final ExecutionMonitor exec) throws IOException,
          CanceledExecutionException
    {
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
          final ExecutionMonitor exec) throws IOException,
          CanceledExecutionException
    {
    }
 }
