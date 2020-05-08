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
 
 import org.knime.core.node.*;
 
 public class IndigoSubstructureMatcherSettings
 {
    enum Mode
    {
       Normal, Tautomer, Resonance
    }
    
    public String colName;
    public String colName2;
    public Mode mode = Mode.Normal;
    public boolean exact = false;
    public boolean align = false;
    public boolean alignByQuery = false;
    public boolean highlight = false;
    public boolean appendColumn = false;
    public String newColName;
    
    public boolean appendQueryKeyColumn = false;
    public String queryKeyColumn;
 
    public boolean appendQueryMatchCountKeyColumn = false;
    public String queryMatchCountKeyColumn;
    
   public boolean matchAllSelected;
   public boolean matchAnyAtLeastSelected;
   public int matchAnyAtLeast;
    
    public void loadSettings (final NodeSettingsRO settings)
          throws InvalidSettingsException
    {
       loadSettingsForDialog(settings);
    }
 
    public void loadSettingsForDialog (final NodeSettingsRO settings)
    {
       colName = settings.getString("colName", null);
       colName2 = settings.getString("colName2", null);
       newColName = settings.getString("newColName", null);
       mode = Mode.valueOf(settings.getString("mode", Mode.Normal.name()));
       align = settings.getBoolean("align", false);
       alignByQuery = settings.getBoolean("alignByQuery", false);
       exact = settings.getBoolean("exact", false);
       highlight = settings.getBoolean("highlight", false);
       appendColumn = settings.getBoolean("appendColumn", false);
 
       appendQueryKeyColumn = settings.getBoolean("appendQueryKeyColumn", false);
       queryKeyColumn = settings.getString("queryKeyColumn", null);
 
       appendQueryMatchCountKeyColumn = settings.getBoolean("appendQueryMatchCountKeyColumn", false);
       queryMatchCountKeyColumn = settings.getString("queryMatchCountKeyColumn", null);
       
       matchAllSelected = settings.getBoolean("matchAllExceptSelected", false);
       
       matchAnyAtLeastSelected = settings.getBoolean("matchAnyAtLeastSelected", true);
       matchAnyAtLeast = settings.getInt("matchAnyAtLeast", 1);
    }
 
    public void saveSettings (final NodeSettingsWO settings)
    {
       if (colName != null)
          settings.addString("colName", colName);
       if (colName2 != null)
          settings.addString("colName2", colName2);
       if (newColName != null)
          settings.addString("newColName", newColName);
       if (mode != null)
          settings.addString("mode", mode.name());
       settings.addBoolean("appendColumn", appendColumn);
       settings.addBoolean("exact", exact);
       settings.addBoolean("align", align);
       settings.addBoolean("alignByQuery", alignByQuery);
       settings.addBoolean("highlight", highlight);
       
       settings.addBoolean("appendQueryKeyColumn", appendQueryKeyColumn);
       if (queryKeyColumn != null)
          settings.addString("queryKeyColumn", queryKeyColumn);
 
       settings.addBoolean("appendQueryMatchCountKeyColumn", appendQueryMatchCountKeyColumn);
       if (queryMatchCountKeyColumn != null)
          settings.addString("queryMatchCountKeyColumn", queryMatchCountKeyColumn);
       
       settings.addBoolean("matchAllExceptSelected", matchAllSelected);
       
       settings.addBoolean("matchAnyAtLeastSelected", matchAnyAtLeastSelected);
       settings.addInt("matchAnyAtLeast", matchAnyAtLeast);
    }
 }
