 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.timeserious.rootcontext;
 
 import com.od.jtimeseries.ui.config.DisplayNamePattern;
 import com.od.jtimeseries.ui.config.DisplayNamePatternConfig;
 import com.od.jtimeseries.ui.config.ExportableConfig;
 import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
 import com.od.jtimeseries.ui.identifiable.DisplayNamesContext;
 import com.od.jtimeseries.ui.identifiable.SettingsContext;
 import com.od.jtimeseries.ui.uicontext.ImportItem;
 import com.od.jtimeseries.util.identifiable.Identifiable;
import com.od.swing.util.UIUtilities;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.List;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 12/05/11
  * Time: 08:33
  */
 public class DisplayNameConfigImportUtility extends ExportableConfigImportUtility {
 
     private DisplayNameCalculator displayNameCalculator;
 
     public DisplayNameConfigImportUtility(DisplayNameCalculator displayNameCalculator) {
         this.displayNameCalculator = displayNameCalculator;
     }
 
     protected void reset() {
     }
 
     protected boolean findUniqueName(Component component, ExportableConfig c, Identifiable target) {
         return false;
     }
 
     protected ImportItem doGetImportDetails(Component component, ExportableConfig c, Identifiable target) {
         return new ImportItem(
             target.getPath() + Identifiable.NAMESPACE_SEPARATOR + SettingsContext.SETTINGS_NODE_NAME + DisplayNamesContext.DISPLAY_NAME_NODE_NAME,
             DisplayNamesContext.DISPLAY_NAME_NODE_NAME,
             DisplayNamesContext.class,
             c
         );
     }
 
     public boolean handlesOwnImport() {
         return true;
     }
 
     protected void doOwnImport(Component component, Identifiable target, ImportItem item) {
        Window window = UIUtilities.getWindowForComponentOrWindow(component);
         int result = JOptionPane.showConfirmDialog(window, "Import Rules?", "Import Display Name Rules", JOptionPane.OK_CANCEL_OPTION);
         if ( result != JOptionPane.CANCEL_OPTION) {
             List<DisplayNamePattern> current = displayNameCalculator.getDisplayNamePatternConfig().getDisplayNamePatterns();
             current.addAll(((DisplayNamePatternConfig)item.getConfigObject()).getDisplayNamePatterns());
 
             int option = JOptionPane.showConfirmDialog(window, "Apply Now?", "Apply Display Name Rules", JOptionPane.YES_NO_OPTION);
             displayNameCalculator.displayPatternsChanged(current, option == JOptionPane.YES_OPTION);
         }
     }
 }
