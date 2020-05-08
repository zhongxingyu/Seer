 /*******************************************************************************
  * Copyright (C) 2007 The University of Manchester   
  * 
  *  Modifications to the initial code base are copyright of their
  *  respective authors, or their employers as appropriate.
  * 
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  *    
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *    
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  ******************************************************************************/
 
 package net.sf.taverna.t2.provenance.lineageservice.derby;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import net.sf.taverna.t2.provenance.item.ProvenanceItem;
 import net.sf.taverna.t2.provenance.lineageservice.EventProcessor;
 import net.sf.taverna.t2.provenance.lineageservice.Provenance;
 import net.sf.taverna.t2.provenance.lineageservice.types.ProvenanceEventType;
 import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
 
 import org.apache.log4j.Logger;
 
 /**
  * Uses Apache Derby to store provenance data
  * 
  * @author Paolo Missier
  * @author Ian Dunlop
  * 
  */
 //FIXME this class is not needed, just use Provenance super class
 public class DerbyProvenance extends Provenance {
 
 	private static Logger logger = Logger.getLogger(DerbyProvenance.class);
 
 	public DerbyProvenance(EventProcessor eventProcessor, String location) {
 		super(eventProcessor, location);
 	}
 
 	public String toString() {
 		return "Derby Provenance Service";
 	}
 
 	/**
 	 * not used --
 	 * 
 	 * @see #acceptRawProvenanceEvent(String, String)
 	 * @param eventType
 	 * @param content
 	 */
 	public void acceptProvenanceEvent(ProvenanceEventType eventType,
 			String content) {
 		; // TODO
 	}
 
 	/**
 	 * maps each incoming event to an insert query into the provenance store
 	 * 
 	 * @param eventType
 	 * @param content
 	 * @throws SQLException
 	 * @throws IOException
 	 */
 	public void acceptRawProvenanceEvent(SharedVocabulary eventType,
 			ProvenanceItem provenanceItem) throws SQLException, IOException {
 
 		processEvent(provenanceItem, eventType);
 
 	}
 
 	/**
 	 * parse d and generate SQL insert calls into the provenance DB
 	 * 
 	 * @param d
 	 *            DOM for the event
 	 * @param eventType
 	 *            see {@link SharedVocabulary}
 	 * @throws SQLException
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public void processEvent(ProvenanceItem provenanceItem,
 			SharedVocabulary eventType) throws SQLException, IOException {
 
 		if (eventType.equals(SharedVocabulary.WORKFLOW_EVENT_TYPE)) {
 			// process the workflow structure
 
 			String workflowID = getEp()
 					.processWorkflowStructure(provenanceItem);
 
 			// add propagation of anl code here
 			if (workflowID != null)
 				getEp().propagateANL(workflowID); // operates on the DB
 
 		} else if (eventType.equals("EOW")) { // SharedVocabulary.END_WORKFLOW_EVENT_TYPE))
 			// {
 			// use this event to do housekeeping on the input/output varbindings
 			// TODO change null to whatever it should be
 			getEp().fillInputVarBindings(null); // indep. of current event
 			getEp().fillOutputVarBindings(null);
 
 			getEp().patchTopLevelnputs();
 			// load up any annotations associated with this workflow TODO
 
 		} else {
 
 			// parse the event into DOM
 			// SAXBuilder b = new SAXBuilder();
 			// Document d;
 			//
 			// try {
 			// d = b.build(new StringReader(content));
 
			//not used anywhere so not needed
//			getEp().processProcessEvent(provenanceItem);
 
 			// } catch (JDOMException e) {
 			// logger.warn("Process event problem: " + e);
 			// } catch (IOException e) {
 			// logger.warn("Process event problem: " + e);
 			// }
 
 		}
 
 	}
 
 	public void clearDB() throws SQLException {
 		getPw().clearDBStatic();
 		getPw().clearDBDynamic();
 	}
 
 	public void setEp(EventProcessor ep) {
 		this.ep = ep;
 	}
 
 	public EventProcessor getEp() {
 		return ep;
 	}
 
 	public String getSaveEvents() {
 		return null;
 	}
 
 	public void setSaveEvents(String saveEvents) {
 
 	}
 
 }
