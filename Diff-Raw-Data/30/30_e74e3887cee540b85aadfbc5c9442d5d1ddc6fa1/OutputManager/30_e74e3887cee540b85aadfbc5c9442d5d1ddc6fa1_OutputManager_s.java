 /*
  * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
  * This cross-platform GIS is developed at French IRSTV institute and is able to
  * manipulate and create vector and raster spatial information. OrbisGIS is
  * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
  * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
  * 
  *
  *
  * This file is part of OrbisGIS.
  *
  * OrbisGIS is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
  *
  * For more information, please consult: <http://www.orbisgis.org/>
  *
  * or contact directly:
  * info _at_ orbisgis.org
  */
 package org.orbisgis.view.output;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.varia.DenyAllFilter;
 import org.apache.log4j.varia.LevelMatchFilter;
 import org.orbisgis.utils.I18N;
 import org.orbisgis.view.docking.DockingPanel;
 import org.orbisgis.view.output.filters.AllFilter;
 
 /**
  * The output manager, create then link/unlink appender with LOG4J
  */
 
 
 public class OutputManager {
     public final static String LOG_INFO = "output_info";
     public final static String LOG_ALL = "output_all";
     private Map<String,PanelAppender> outputPanels = new HashMap<String,PanelAppender>();
     private MainOutputPanel mainPanel;
     private static final Logger ROOT_LOGGER = Logger.getRootLogger();
     private static final Logger GUI_LOGGER = Logger.getLogger("gui");
     private PatternLayout loggingLayout = new PatternLayout("%5p [%t] (%F:%L) - %m%n");
     public OutputManager() {
         mainPanel = new MainOutputPanel();
         makeOutputAll();
         makeOutputInfo();
     }
     /**
      * Remove the link between LOG4J and Appenders
      */
     public void dispose() {
         for(PanelAppender appender : outputPanels.values()) {
             ROOT_LOGGER.removeAppender(appender);
         }
     }
     
    
     /**
      * Make the All Output panel
      * This panel accept root     <= Warning   >
      *                   root.gui <= Info      >
      */
     private void makeOutputAll() {
         PanelAppender app = makePanel();
         app.setLayout(loggingLayout);
         app.addFilter(new AllFilter());
         outputPanels.put(LOG_ALL,app);
         ROOT_LOGGER.addAppender(app);
         mainPanel.addSubPanel(I18N.getString("orbisgis.view.log_all_title"), app.getGuiPanel());
     }
     private PanelAppender makePanel() {
         PanelAppender app = new PanelAppender(new OutputPanel());
         return app;
     }
     /**
      * Make the Info Output panel
     * This panel accept root.gui <= Info      >
      */
     private void makeOutputInfo() {
         PanelAppender app = makePanel();
         app.setLayout(loggingLayout);
         LevelMatchFilter filter = new LevelMatchFilter();
         filter.setLevelToMatch(Level.INFO.toString());
         app.addFilter(filter);
         app.addFilter(new DenyAllFilter());
         outputPanels.put(LOG_INFO,app);
         GUI_LOGGER.addAppender(app);
         mainPanel.addSubPanel(I18N.getString("orbisgis.view.log_info_title"), app.getGuiPanel());
     }
     /**
      * Return the panel by its panel Id
      * @param panelName The panel id, the static LOG_* in this class
      * @return 
      */
     public DockingPanel getPanel() {
         return mainPanel;
     }
     
 }
