 /*
  * Copyright 2005-7 Pi4 Technologies Ltd
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  * Change History:
  * Feb 26, 2007 : Initial version created by gary
  */
 package org.savara.tools.scenario.designer.simulate;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.swt.widgets.Display;
 import org.savara.common.logging.MessageFormatter;
 import org.savara.scenario.model.Event;
 import org.savara.scenario.model.Role;
 import org.savara.tools.scenario.osgi.Activator;
 
 /**
  * This class is derived from the scenario simulation launcher with the
  * ability to present the results in a graphical form.
  */
 public class ScenarioDesignerSimulationLauncher extends ScenarioSimulationLauncher {
 
 	private static final Logger logger=Logger.getLogger(ScenarioDesignerSimulationLauncher.class.getName());
 
 	private Display m_display=null;
 	private ScenarioSimulation m_scenarioSimulation=null;
 	private org.savara.scenario.model.Scenario m_scenario=null;
 	private int m_currentPosition=0;
 	private StringBuffer m_log=new StringBuffer();
 	
 	public ScenarioDesignerSimulationLauncher(Display display,
 			org.savara.scenario.model.Scenario scenario,
 					ScenarioSimulation simulation) {
 		m_display = display;
 		m_scenario = scenario;
 		m_scenarioSimulation = simulation;
 	}
 	
 	/**
 	 * This method handles the results produced by the launched
 	 * test.
 	 * 
 	 * @param results The results
 	 * @param errorStream Whether the results are from the error
 	 * 						stream
 	 */
 	protected void handleResults(String results, boolean errorStream) {		
 		if (logger.isLoggable(Level.FINER)) {
			logger.finer(">>(err? "+errorStream+"): "+results);
 		}
 		
 		if (errorStream) {
 			
 			m_scenarioSimulation.appendLogEntry(results);
 			
 			m_log.append(results);
 			
 			try {
 				m_display.asyncExec(new Runnable() {
 					public void run() {				
 						processResults();
 					}			
 				});
 			} catch(Throwable e) {
 				org.savara.tools.scenario.osgi.Activator.logError(
 						"Failed to display scenario test results", e);
 			}
 		}
 	}
 	
 	protected synchronized void processResults() {
 		int infoPos=0;
 		
 		while ((infoPos=m_log.indexOf(">>> ",
 					m_currentPosition)) != -1) {
 			
 			errorCheck(m_currentPosition, infoPos);
 			
 			int newlinePos=0;
 			
 			// Check if newline found
 			if ((newlinePos=m_log.indexOf("\r",
 					infoPos)) != -1 ||
 				(newlinePos=m_log.indexOf("\n",
 							infoPos)) != -1) {
 			
 				// Complete line found
 				processResultLine(infoPos+4,
 						newlinePos);
 				
 				m_currentPosition = newlinePos;
 			} else {
 				// Line is not complete, so skip until
 				// further text is available
 				break;
 			}
 		}
 	}
 	
 	protected boolean errorCheck(int startPos, int endPos) {
 		boolean ret=false;
 		
 		if (m_log.indexOf("java.lang.UnsupportedOperationException: Cannot create XMLStreamReader", startPos) != -1) {
 			
 			// Check JDK/JRE version
 			String error=MessageFormatter.format(java.util.PropertyResourceBundle.getBundle(
 					"org.savara.tools.scenario.Messages"), "SAVARA-SCENARIOTOOLS-00001",
 					"Cannot create XMLStreamReader or XMLEventReader from a javax.xml.transform.stream.StreamSource");
 			
 			Activator.logError(error, null);
 		}
 		
 		return(ret);
 	}
 	
 	protected void processResultLine(int start, int end) {
 		String tag=null;
 		String line=m_log.substring(start, end);
 		
 		int tagEndPos=line.indexOf(' ');
 		tag = line.substring(0, tagEndPos);
 		
 		if (tag.startsWith("ROLE_")) {
 			
 			int roleStartPos=line.indexOf('[', tagEndPos);
 			int roleEndPos=line.indexOf(']', tagEndPos);
 			
 			if (roleStartPos != -1 && roleEndPos != -1) {
 				String roleName=line.substring(roleStartPos+1, roleEndPos);
 				Role role=null;
 				
 				for (Role r : m_scenario.getRole()) {
 					if (r.getName().equals(roleName)) {
 						role = r;
 						break;
 					}
 				}
 				
 				if (role != null) {
 					SimulationEntity se=getScenarioEntity(role);
 
 					if (se == null) {
 						// TODO: error
 					} else if (tag.equals("ROLE_START")) {
 						se.processing();
 						se.setLogStartPosition(start-4);
 					} else if (tag.equals("ROLE_INIT")) {
 						se.successful();
 						se.setLogEndPosition(end);
 					} else if (tag.equals("ROLE_FAIL")) {
 						se.unsuccessful();
 						se.setLogEndPosition(end);
 					}				
 				}
 			} else {
 				// TODO: error
 			}
 		} else {
 		
 			int idstart=line.indexOf("[ID=");
 			int idend=line.indexOf(']');
 			
 			String id=line.substring(idstart+4, idend);
 			
 			// Get scenario entity
 			SimulationEntity se=getScenarioEntity(id);
 			
 			if (se != null) {
 				if (tag.equals("START")) {
 					se.processing();
 					se.setLogStartPosition(start-4);
 				} else if (tag.equals("END")) {
 					se.setLogEndPosition(end);
 				} else if (tag.equals("SUCCESS")) {
 					se.successful();
 				} else if (tag.equals("FAIL")) {
 					se.unsuccessful();
 				} else if (tag.equals("NO_SIMULATOR")) {
 					se.reset();
 				}
 			}
 		}
 	}
 	
 	protected SimulationEntity getScenarioEntity(Role role) {
 		return (m_scenarioSimulation.getSimulationEntity(role, false));
 	}
 	
 	protected SimulationEntity getScenarioEntity(String id) {
 		SimulationEntity ret=null;
 		
 		for (Event event : m_scenario.getEvent()) {
 			if (event.getId().equals(id)) {
 				ret = m_scenarioSimulation.getSimulationEntity(event, false);
 				break;
 			}
 		}
 		
 		return(ret);
 	}
 }
