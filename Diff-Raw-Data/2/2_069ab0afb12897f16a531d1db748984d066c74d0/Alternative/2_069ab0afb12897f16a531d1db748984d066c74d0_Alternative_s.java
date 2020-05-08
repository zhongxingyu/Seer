 package edu.wpi.cs.jburge.SEURAT.rationaleData;
 
 import instrumentation.DataLog;
 
 import java.util.*;
 import java.io.*;
 
 import java.sql.Connection;  
 import java.sql.SQLException; 
 import java.sql.Statement;
 import java.sql.ResultSet;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.w3c.dom.*;
 import edu.wpi.cs.jburge.SEURAT.editors.EditAlternative;
 import edu.wpi.cs.jburge.SEURAT.inference.AlternativeInferences;
 
 import SEURAT.events.RationaleElementUpdateEventGenerator;
 import SEURAT.events.RationaleUpdateEvent;
 
 /**
  * Defines an Alternative (alternatives are alternative methods of addressing
  * a design decision)
  * @author burgeje
  *
  */
 public class Alternative extends RationaleElement implements Serializable {
 	// class variables
 	
 	/**
 	 * Auto generated
 	 */
 	private static final long serialVersionUID = 4699219499868590420L;
 	// instance variables
 	/**
 	 * The evaluations "score" for the alternative
 	 */
 	double evaluation;
 	/**
 	 * The status of the alternative (selected, at issue, etc.)
 	 */
 	AlternativeStatus status; 
 	/**
 	 * The artifacts associated with the alternative
 	 */
 	Vector<String> artifacts;
 	/**
 	 * The ID (database ID) of the parent (decision)
 	 */
 	int parent;
 	/**
 	 * The designer who proposed the alternative
 	 */
 	Designer designer;
 	/**
 	 * The contingency level for the alternative. Typically a %. This is
 	 * from the Orca modifications and is not usually used in SEURAT.
 	 */
 	Contingency contingency;
 	/**
 	 * The type of the parent
 	 */
 	RationaleElementType ptype;
 	/**
 	 * A vector containing all arguments about the alternative
 	 */
 	Vector<Argument> arguments;
 	/**
 	 * The arguments against the alternative
 	 */
 	Vector<Argument> argumentsAgainst;
 	/**
 	 * The arguments for the alternative (supporting)
 	 */
 	Vector<Argument> argumentsFor;
 	/**
 	 * Arguments that specify requirements for a specific alternative - dependency and
 	 * conflict relationships. These only affect the evaluation if the alternative
 	 * required is selected.
 	 */
 	Vector<Argument> relationships; 
 	/**
 	 * Decisions that need to be made if this alternative is selected.
 	 */
 	Vector<Decision> subDecisions;
 	/**
 	 * Questions that have to be answered in order to evaluate the alternative
 	 */
 	Vector<Question> questions;
 
 	private RationaleElementUpdateEventGenerator<Alternative> m_eventGenerator = 
 		new RationaleElementUpdateEventGenerator<Alternative>(this);
 	
 	/**
 	 * Our constructor. Initially our status is "at issue"
 	 *
 	 */
 	public Alternative() {
 		super();
 		status = AlternativeStatus.ATISSUE;
 		argumentsAgainst = new Vector<Argument>();
 		argumentsFor = new Vector<Argument>();
 		relationships = new Vector<Argument>();
 		subDecisions = new Vector<Decision>();
 		questions = new Vector<Question>();
 		artifacts = new Vector<String>();
 		arguments = new Vector<Argument>();
 	}
 	public Element toXML(Document ratDoc) {
 		Element altE;
 		RationaleDB db = RationaleDB.getHandle();
 		String altID = db.getRef(id);
 		if (altID == null) {
 			altID = db.addRef(id);
 			altE = ratDoc.createElement("DR:alternative");
 			altE.setAttribute("id", altID);
 			altE.setAttribute("name", name);
 			Float tempf = new Float(evaluation);
 			altE.setAttribute("evaluation", tempf.toString());
 			altE.setAttribute("status", status.toString());
 
 			//save our description
 			Element descE = ratDoc.createElement("DR:description");
 			//set the reference contents
 			Text descText = ratDoc.createTextNode(description);
 			descE.appendChild(descText);
 			altE.appendChild(descE);
 
 			Enumeration args = arguments.elements();
 			while (args.hasMoreElements()) {
 				Argument arg = (Argument) args.nextElement();
 				altE.appendChild(arg.toXML(ratDoc));
 			}
 
 			Enumeration quests = questions.elements();
 			while (quests.hasMoreElements()) {
 				Question quest = (Question) quests.nextElement();
 				altE.appendChild(quest.toXML(ratDoc));
 			}
 
 			Enumeration decs = subDecisions.elements();
 			while (decs.hasMoreElements()) {
 				Decision dec = (Decision) decs.nextElement();
 				altE.appendChild(dec.toXML(ratDoc));
 			}
 
 			//finally, the history
 
 			Element ourHist = ratDoc.createElement("DR:history");
 			Enumeration hist = history.elements();
 			while (hist.hasMoreElements()) {
 				History his = (History) hist.nextElement();
 				ourHist.appendChild(his.toXML(ratDoc));
 			}
 			altE.appendChild(ourHist);
 		} else {
 			altE = ratDoc.createElement("altref");
 			//set the reference contents
 			Text text = ratDoc.createTextNode(altID);
 			altE.appendChild(text);
 		}
 		return altE;
 	}
 	public RationaleElementType getElementType()
 	{
 		return RationaleElementType.ALTERNATIVE;
 	}
 	
 	public double getEvaluation() {
 		return evaluation;
 	}
 	
 	public void setEvaluation(double ev) {
 		evaluation = ev;
 	}
 	
 	public int getParent() {
 		return parent;
 	}
 	
 	public RationaleElementType getPtype() {
 		return ptype;
 	}
 	public AlternativeStatus getStatus() {
 		return status;
 	}
 	public void setStatus(AlternativeStatus stat) {
 		status = stat;
 	}
 	
 	
 	public Contingency getContingency() {
 		return contingency;
 	}
 	
 	public void setContingency(Contingency contingency) {
 		this.contingency = contingency;
 	}
 	
 	public Designer getDesigner() {
 		return designer;
 	}
 	
 	public void setDesigner(Designer designer) {
 		this.designer = designer;
 	}
 	
 	public Vector getArgumentsFor() {
 		return argumentsFor;
 	}
 	public Vector getArgumentsAgainst() {
 		return argumentsAgainst;
 	}
 	
 	public Vector getRelationships() {
 		return relationships;
 	}
 	public Vector getSubDecisions() {
 		return subDecisions;
 	}
 	public Vector getQuestions() {
 		return questions;
 	}
 	public void addArgumentFor(Argument alt) {
 		argumentsFor.addElement(alt);
 	}
 	
 	public void addArgumentAgainst(Argument alt) {
 		argumentsAgainst.addElement(alt);
 	}
 	
 	public void addArgument(Argument alt) {
 		if ((alt.type == ArgType.ADDRESSES) || (alt.type == ArgType.SUPPORTS) || (alt.type == ArgType.SATISFIES))
 		{
 			addArgumentFor(alt);
 		}
 		else if ((alt.type == ArgType.VIOLATES) || (alt.type == ArgType.DENIES))
 		{
 			addArgumentAgainst(alt);
 		}
 		else
 		{
 			relationships.addElement(alt);
 		}
 	}
 	
 	public void addSubDecision(Decision dec) {
 		subDecisions.addElement(dec);
 	}
 	public void addQuestion(Question quest) {
 		questions.addElement(quest);
 	}
 	public void delArgumentFor(Argument alt) {
 		argumentsFor.remove(alt);
 	}
 	
 	public void delArgumentAgainst(Argument alt)
 	{
 		argumentsAgainst.remove(alt);
 	}
 	public void delSubDecision(Decision dec) {
 		subDecisions.remove(dec);
 	}
 	public void delQuestion(Question quest) {
 		questions.remove(quest);
 	}
 	
 	/**
 	 * Generic function to get a list of sub-elements of a particular type -
 	 * decisions, arguments, or questions.
 	 * @param type - the type of sub-element to be returned.
 	 */
 	public Vector getList(RationaleElementType type) {
 		if (type.equals(RationaleElementType.DECISION)) {
 			return subDecisions;
 		} else if (type.equals(RationaleElementType.ARGUMENT)) {
 			return getAllArguments();
 		} else if (type.equals(RationaleElementType.QUESTION)) {
 			return questions;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Get all our arguments
 	 * @return a list of arguments
 	 */
 	public Vector<Argument> getAllArguments() {
 		Vector<Argument> args = new Vector<Argument>();
 		args.addAll(argumentsFor);
 		args.addAll(argumentsAgainst);
 		args.addAll(relationships);
 		return args;
 	}
 	
 	public double getImportanceVal()
 	{
 		if (status == AlternativeStatus.ADOPTED)
 		{
 			return 1.0;
 		}
 		else
 		{
 			return 0.0;
 		}
 	}
 	
 	public Vector getArtifacts()
 	{
 		return artifacts;
 	}
 	
 	public double evaluate()
 	{
 		return evaluate(true);
 	}
 	/**
 	 * Based on the arguments for and against our alternative, calculate the
 	 * argument score. This also involves going to the DB to see if there is
 	 * anyone who refers to this alternative in an argument (pre-supposing or 
 	 * opposing)
 	 * @return the evaluation score. High is good.
 	 */
 	public double evaluate(boolean pSave)
 	{
 		double result = 0.0;
 		Enumeration args = getAllArguments().elements();
 		while (args.hasMoreElements()) {
 			Argument arg = (Argument) args.nextElement();
 //			System.out.println("relevant argument: " + arg.toString());
 			result += arg.evaluate();
 		}
 		
 		//should we take into account anyone pre-supposing or opposing us?
 		RationaleDB db = RationaleDB.getHandle();
 		Vector dependent = db.getDependentAlternatives(this, ArgType.OPPOSES);
 		Iterator depI = dependent.iterator();
 		while (depI.hasNext())
 		{
 			Alternative depA = (Alternative) depI.next();
 			if (depA.getStatus() == AlternativeStatus.ADOPTED)
 			{
 				result += -10; //assume amount is MAX
 			}
 		}
 		dependent = db.getDependentAlternatives(this, ArgType.PRESUPPOSES);
 		depI = dependent.iterator();
 		while (depI.hasNext())
 		{
 			Alternative depA = (Alternative) depI.next();
 			if (depA.getStatus() == AlternativeStatus.ADOPTED)
 			{
 				result += 10; //assume amount is MAX
 			}
 		}
 		
 		
 //		System.out.println("setting our evaluation = " + new Double(result).toString());
 		setEvaluation(result);
 		
 		if( pSave )
 		{
 			///
 			// TODO This was added to get the integrated editors
 			// updating correctly, however would be better suited
 			// in a seperate function which is then refactored
 			// outward so other places in SEURAT use it.
 			Connection conn = db.getConnection();
 			Statement stmt = null; 
 			ResultSet rs = null; 
 			try
 			{
 				stmt = conn.createStatement(); 
 				
 				// TODO Function call here is really hacky, see
 				// the function inDatabase()
 				if (inDatabase())
 				{
 					String updateParent = "UPDATE alternatives " +
 					"SET evaluation = " + new Double(evaluation).toString() +
 					" WHERE " +
 					"id = " + this.id + " " ;
 					stmt.execute(updateParent);
 					
 					// Broadcast Notification That Score Has Been Recomputed
 					m_eventGenerator.Updated();		
 				}
 				else
 				{
 					// If The RationaleElement Wasn't In The Database There 
 					// Is No Reason To Attempt To Save The Evaluation Score
 					// Or Broadcast A Notification About It
 				}
 			}
 			catch( Exception e )
 			{
 				System.out.println("Exception while saving evaluation score to database");
 			}			
 			finally { 
 				RationaleDB.releaseResources(stmt, rs);
 			}
 		}
 		
 		return result;
 	}
 	
 	public Vector getOntEntriesFor()
 	{
 		Iterator argI = argumentsFor.iterator();
 		return getOntEntryArgs(argI);
 		
 	}
 	
 	public Vector getOntEntriesAgainst()
 	{
 		Iterator argI = argumentsAgainst.iterator();
 		return getOntEntryArgs(argI);
 		
 	}
 	
 	public Vector<OntEntry> getOntEntryArgs(Iterator argI)
 	{
 		Vector<OntEntry> ourOntEntries = new Vector<OntEntry>();
 		while (argI.hasNext())
 		{
 			Argument ourArg = (Argument) argI.next();
 			if (ourArg.getClaim() != null)
 			{
 				ourOntEntries.add(ourArg.getClaim().getOntology());
 			}
 		}
 		
 		return ourOntEntries;
 		
 	}
 	
 	/**
 	 * Gets alternatives that have a particular type of relationship with this
 	 * alternative.
 	 * @param typeWanted - the type of argument relationship
 	 * @return a list of relevant alternatives
 	 */
 	public Vector<Alternative> getAlts(ArgType typeWanted)
 	{
 		Vector<Alternative> presup = new Vector<Alternative>();
 		Iterator argI;
 		if ((typeWanted == ArgType.PRESUPPOSES) || (typeWanted == ArgType.OPPOSES))
 		{
 //			System.out.println("Relationships: " + relationships.size());
 			argI = relationships.iterator();
 		}
 		/*		else if (typeWanted == ArgType.PRESUPPOSEDBY)
 		 {
 		 argI = argumentsFor.iterator();
 		 } */
 		else
 		{
 			argI = argumentsAgainst.iterator();
 		}
 		while (argI.hasNext())
 		{
 			Argument ourArg = (Argument) argI.next();
 			Alternative preAlt = ourArg.getAlternative();
 //			System.out.println("pre arg type = " + ourArg.getType().toString());
 			if (preAlt != null)
 			{
 //				System.out.println("pre arg type = " + ourArg.getType().toString());
 				if (ourArg.getType() == typeWanted)
 				{
 					presup.add(preAlt);
 				}
 			}
 		}
 		
 		return presup;
 	}
 	
 	/**
 	 * Get all the arguments that refer to claims. This is used to check for 
 	 * tradeoff violations.
 	 */
 	public Vector<Claim> getClaims()
 	{
 		Vector<Claim> ourClaims = new Vector<Claim>();
 		Iterator argI;
 		
 		argI = this.getAllArguments().iterator();
 		while (argI.hasNext())
 		{
 			Claim nextClaim = ((Argument) argI.next()).getClaim();
 			if (nextClaim != null)
 			{
 				ourClaims.add(nextClaim);
 			}
 		}
 		return ourClaims;
 	}
 	
 	/**
 	 * Save our alternative to the database.
 	 * @param parentID - the parent of the alternative
 	 * @param ptype - the parent type
 	 * @return the ID (from the DB) of our alternative
 	 */
 	public int toDatabase(int parentID, RationaleElementType ptype)
 	{
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		// Update Event To Inform Subscribers Of Changes
 		// To Rationale
 		RationaleUpdateEvent l_updateEvent;
 		
 		int ourid = 0;
 		
 		//find out if this requirement is already in the database
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		
 		String updateC;
 		if (contingency == null)
 			updateC = "null";
 		else
 			updateC = new Integer(contingency.getID()).toString();
 		
 		evaluation = this.evaluate(false); // might as well make sure we are up to date!
 		
 		try {
 			stmt = conn.createStatement(); 
 			
 			if (inDatabase(parentID, ptype))
 			{
 				String updateParent = "UPDATE alternatives R " +
 				"SET R.parent = " + new Integer(parentID).toString() +
 				", R.ptype = '" + ptype.toString() +
 				"', R.name = '" + RationaleDBUtil.escape(this.name) +
 				"', R.description = '" + RationaleDBUtil.escape(this.description) +
 				"', R.status = '" + status.toString() +
 				"', R.evaluation = " + new Double(evaluation).toString() +
 				", R.designType = " + updateC +
 				" WHERE " +
 				"R.id = " + this.id + " " ;
 //				System.out.println(updateParent);
 				stmt.execute(updateParent);
 				
 				l_updateEvent = m_eventGenerator.MakeUpdated();
 //				return ourid;
 			}
 			else 
 			{
 				
 				//now, we have determined that the requirement is new
 				String parentSt;
 				if (this.parent < 0)
 				{
 					parentSt = "NULL";
 				}
 				else
 				{
 					parentSt = new Integer(this.parent).toString();
 				}
 				
 				String updateD;
 				
 				if (designer == null)
 					updateD = "null";
 				else
 					updateD = new Integer(designer.getID()).toString();
 				
 				
 				
 				
 				String newAltSt = "INSERT INTO Alternatives "+
 				"(name, description, status, ptype, parent, evaluation, designer, designType) " +
 				"VALUES ('" +
 				RationaleDBUtil.escape(this.name) + "', '" +
 				RationaleDBUtil.escape(this.description) + "', '" +
 				this.status.toString() + "', '" +
 				ptype.toString() + "', " +
 				parentSt + ", " +
 				new Double(evaluation).toString() + ", " +
 				updateD + "," + updateC + ")";
 //				***			   System.out.println(newAltSt);
 				stmt.execute(newAltSt); 
 				
 				l_updateEvent = m_eventGenerator.MakeCreated();
 			}
 			
 			//in either case, we want to update any sub-requirements in case
 			//they are new!
 			//now, we need to get our ID
 			String findQuery2 = "SELECT id FROM alternatives where name='" +
 			RationaleDBUtil.escape(this.name) + "'";
 			rs = stmt.executeQuery(findQuery2); 
 			
 			if (rs.next())
 			{
 				ourid = rs.getInt("id");
 				rs.close();
 			}
 			else
 			{
 				ourid = -1;
 			}
 			this.id = ourid;
 			
 			Enumeration args = getAllArguments().elements();
 			while (args.hasMoreElements()) {
 				Argument arg = (Argument) args.nextElement();
 //				System.out.println("Saving arg from alternative");
 				arg.toDatabase(ourid, RationaleElementType.ALTERNATIVE);
 			}
 			
 			Enumeration quests = questions.elements();
 			while (quests.hasMoreElements()) {
 				Question quest = (Question) quests.nextElement();
 				quest.toDatabase(ourid, RationaleElementType.ALTERNATIVE);
 			}
 			
 			Enumeration decs = subDecisions.elements();
 			while (decs.hasMoreElements()) {
 				Decision dec = (Decision) decs.nextElement();
 				dec.toDatabase(ourid, RationaleElementType.ALTERNATIVE);
 			}
 			
 			//finally, the history
 			
 			Enumeration hist = history.elements();
 			while (hist.hasMoreElements())
 			{
 				History his = (History) hist.nextElement();
 				his.toDatabase(ourid, RationaleElementType.ALTERNATIVE);
 			}
 			
 			m_eventGenerator.Broadcast(l_updateEvent);
 		} catch (SQLException ex) {
 			// handle any errors 
 			RationaleDB.reportError(ex, "Alternative.toDatabase", "Bad query");
 		}
 		
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		
 		return ourid;	
 		
 	}	
 
 	public RationaleElement getParentElement()
 	{
 		return RationaleDB.getRationaleElement(this.parent, this.ptype);
 	}
 	
 	/**
 	 * Read in our alternative from the database
 	 * @param id - the database ID
 	 */
 	public void fromDatabase(int id)
 	{
 		
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		this.id = id;
 		
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			
 			findQuery = "SELECT *  FROM " +
 			"alternatives where id = " +
 			new Integer(id).toString();
 //			***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			
 			if (rs.next())
 			{
 				name = RationaleDBUtil.decode(rs.getString("name"));
 				rs.close();
 				this.fromDatabase(name);
 			}
 			
 		} catch (SQLException ex) {
 			// handle any errors 
 			RationaleDB.reportError(ex, "Alternative.fromDatabase(int)", 
 					findQuery);
 		}
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		
 	}	
 	
 	/**
 	 * Read in our alternative from the database.
 	 * @param name - the alternative name
 	 */
 	public void fromDatabase(String name)
 	{
 		
 		RationaleDB db = RationaleDB.getHandle();
 		Connection conn = db.getConnection();
 		
 		this.name = name;
 		name = RationaleDBUtil.escape(name);
 		
 		Statement stmt = null; 
 		ResultSet rs = null; 
 		String findQuery = "";
 		try {
 			stmt = conn.createStatement();
 			findQuery = "SELECT *  FROM " +
 			"alternatives where name = '" +
 			name + "'";
 //			***			System.out.println(findQuery);
 			rs = stmt.executeQuery(findQuery);
 			
 			if (rs.next())
 			{
 				id = rs.getInt("id");
 				description = RationaleDBUtil.decode(rs.getString("description"));
 				ptype = RationaleElementType.fromString(rs.getString("ptype"));
 				parent = rs.getInt("parent");
 //				enabled = rs.getBoolean("enabled");
 				status = (AlternativeStatus) AlternativeStatus.fromString(rs.getString("status"));
 				evaluation = rs.getFloat("evaluation");
 				
 				try {
 					int desID = rs.getInt("designer");
 					
 					if( rs.wasNull() )
 						throw new SQLException();
 					
 					designer = new Designer();
 					designer.fromDatabase(desID);
 				} catch (SQLException ex)
 				{
 					designer = null; //nothing...
 				}
 				
 				try {
 					int contID = rs.getInt("designType");
 					
 					if( rs.wasNull() )
 						throw new SQLException();
 					
 					contingency = new Contingency();
 					contingency.fromDatabase(contID);
 				} catch (SQLException ex)
 				{
 					contingency = null; //nothing...
 				}
 				
 				//need to read in the rest - recursive routines?
 				//Now, we need to get the lists of arguments for and against
 				//first For
 				String findFor = "SELECT id FROM " + RationaleDBUtil.escapeTableName("arguments")
 				+" where ptype = 'Alternative' and " +
 				"parent = " + 
 				new Integer(this.id).toString() + " and " +
 				"(type = 'Supports' or " +
 				"type = 'Addresses' or " +
 				"type = 'Satisfies' or " +
 				"type = 'Pre-supposed-by')";
 //				***				System.out.println(findFor);
 				rs = stmt.executeQuery(findFor); 
 				Vector<Integer> aFor = new Vector<Integer>();
 				Vector<Integer> aAgainst = new Vector<Integer>();
 				Vector<Integer> aRel = new Vector<Integer>();
 				while (rs.next())
 				{
 					aFor.addElement(new Integer(rs.getInt("id")));
 				}
 				rs.close();
 				
 				//Now, the arguments against
 				String findAgainst = "SELECT id FROM "+RationaleDBUtil.escapeTableName("arguments")
 				+" where ptype = 'Alternative' and " +
 				"parent = " + 
 				new Integer(this.id).toString() + " and " +
 				"(type = 'Denies' or " +
 				"type = 'Violates' or " +
 				"type = 'Opposed-by')";
 //				***				System.out.println(findAgainst);
 				rs = stmt.executeQuery(findAgainst); 
 				
 				while (rs.next())
 				{
 					aAgainst.addElement(new Integer(rs.getInt("id")));
 				}
 				rs.close();	
 				
 				//Now, any other useful relationships
 				//Now, the arguments against
 				String findRel = "SELECT id FROM "+ RationaleDBUtil.escapeTableName("arguments")
 				+" where ptype = 'Alternative' and " +
 				"parent = " + 
 				new Integer(this.id).toString() + " and " +
 				"(type = 'Opposed' or " +
 				"type = 'Pre-supposes')";
 //				***				System.out.println(findRel);
 				rs = stmt.executeQuery(findRel); 
 				
 				while (rs.next())
 				{
 					aRel.addElement(new Integer(rs.getInt("id")));
 				}
 				rs.close();	
 
 				//..Treat non-affiliated arguments as relationships (for now)
 				String findUnafil = "SELECT id FROM Arguments where " +
 				"ptype = 'Alternative' and " +
 				"parent = " + 
 				new Integer(this.id).toString() + " and " +
 				"(type = 'NONE' )";
 				//**				System.out.println(findUnafil);
 				rs = stmt.executeQuery(findUnafil); 
 				
 				while (rs.next())
 				{
 					aRel.addElement(new Integer(rs.getInt("id")));
 				}
 				rs.close();			
 
 				// Cleanup before loading arguments
 				argumentsFor.removeAllElements();
 				//Now that we have the IDs, create the arguments
 				Enumeration args = aFor.elements();
 				while (args.hasMoreElements())
 				{
 					Argument arg = new Argument();
 					arg.fromDatabase(((Integer) args.nextElement()).intValue());
 					if (arg.getParent() == this.id)
 						argumentsFor.add(arg);
 					else
 					{
 						System.out.println("argparent = " + arg.getParent() + "not equal" + this.id);
 					}
 				}
 
 				// Cleanup before loading arguments
 				argumentsAgainst.removeAllElements();
 				args = aAgainst.elements();
 				while (args.hasMoreElements())
 				{
 					Argument arg = new Argument();
 					arg.fromDatabase(((Integer) args.nextElement()).intValue());
 					if (arg.getParent() == this.id)
 						argumentsAgainst.add(arg);
 				}
 
 				// Cleanup before loading arguments
 				relationships.removeAllElements();
 				args = aRel.elements();
 				while (args.hasMoreElements())
 				{
 					Argument arg = new Argument();
 					arg.fromDatabase(((Integer) args.nextElement()).intValue());
 					if (arg.getParent() == this.id)
 						relationships.add(arg);
 				}
 				
 				
 				Vector<String> decNames = new Vector<String>();
 				String findQuery2 = "SELECT name from DECISIONS where " +
				"ptype = '" + RationaleElementType.DECISION.toString() +
 				"' and parent = " + new Integer(id).toString();
 //				***				System.out.println(findQuery2);
 				rs = stmt.executeQuery(findQuery2);	
 				while (rs.next())				
 				{
 					decNames.add(RationaleDBUtil.decode(rs.getString("name")));
 				}
 				Enumeration decs = decNames.elements();
 				
 				// Cleanup before loading subdecisions
 				subDecisions.removeAllElements();
 				while (decs.hasMoreElements())
 				{
 					Decision subDec = new Decision();
 					subDec.fromDatabase((String) decs.nextElement());
 					subDecisions.add(subDec);
 				}				
 				
 				//need to do questions too 
 				Vector<String> questNames = new Vector<String>();
 				String findQuery3 = "SELECT name from QUESTIONS where " +
 				"ptype = '" + RationaleElementType.ALTERNATIVE.toString() +
 				"' and parent = " + new Integer(id).toString();
 //				***			System.out.println(findQuery3);
 				rs = stmt.executeQuery(findQuery3);
 				while (rs.next())
 				{
 					questNames.add(RationaleDBUtil.decode(rs.getString("name")));
 				}
 				Enumeration quests = questNames.elements();
 				// Cleanup before loading questions
 				questions.removeAllElements();
 				while (quests.hasMoreElements())
 				{
 					Question quest = new Question();
 					quest.fromDatabase((String) quests.nextElement());
 					questions.add(quest);
 				}
 				
 				//Last, but not least, look for any associations
 				String findQuery4 = "SELECT artName from ASSOCIATIONS where " +
 				"alternative = " + Integer.toString(id);
 				rs = stmt.executeQuery(findQuery4);
 				
 				// Cleanup before loading artifacts
 				artifacts.removeAllElements();
 				while (rs.next())
 				{
 					artifacts.add(rs.getString("artName"));
 				}
 				
 				//no, not last - need history too
 				String findQuery5 = "SELECT * from HISTORY where ptype = 'Alternative' and " +
 				"parent = " + Integer.toString(id);
 //				***			  System.out.println(findQuery5);
 				rs = stmt.executeQuery(findQuery5);
 
 				// Cleanup before loading history
 				history.removeAllElements();
 				
 				while (rs.next())
 				{
 					History nextH = new History();
 					nextH.setStatus(rs.getString("status"));
 					nextH.setReason(RationaleDBUtil.decode(rs.getString("reason")));
 					nextH.dateStamp = rs.getTimestamp("date");
 //					nextH.dateStamp = rs.getDate("date");
 					history.add(nextH);
 				}
 			}
 		} catch (SQLException ex) {
 			// handle any errors 
 			RationaleDB.reportError(ex, "Alternative.fromDatabase(String)", "Error in a query"); 
 		}
 		finally { 
 			RationaleDB.releaseResources(stmt, rs);
 		}
 		
 	}
 	/*
 	 public boolean display()
 	 {
 	 Frame lf = new Frame();
 	 AlternativeGUI ar = new AlternativeGUI(lf, this, false);
 	 ar.show();
 	 return ar.getCanceled();
 	 }
 	 */	
 	/**
 	 * Used to edit our alternative - invokes the editor display
 	 * @param disp - points back to the display
 	 * @return - true if cancelled by the user
 	 */
 	public boolean display(Display disp)
 	{
 		EditAlternative ar = new EditAlternative(disp, this, false);
 //		System.out.println("this after = " + this.getStatus().toString());
 //		System.out.println(ar.getCanceled());
 		String msg = "Edited alternative " + this.getName() + " " + ar.getCanceled();
 		DataLog d = DataLog.getHandle();
 		d.writeData(msg);
 		return ar.getCanceled(); //can I do this?
 		
 	}
 	
 	/**
 	 * Used to bring up an editor and create a new alternative
 	 * @param disp - points back to the display
 	 * @param parent - the parent element
 	 * @return true if cancelled by the user
 	 */
 	public boolean create(Display disp, RationaleElement parent)
 	{
 //		System.out.println("create alternative");
 		if (parent != null)
 		{
 			this.parent = parent.getID();
 			this.ptype = parent.getElementType();
 		}
 		EditAlternative ar = new EditAlternative(disp, this, true);
 		return ar.getCanceled(); //can I do this?
 	}
 
 	/**
 	 * Used to set the parent data of the rationale element without
 	 * brigning up the edit alternative GUI (in conjunction with the
 	 * new editor GUI).
 	 * @param parent
 	 */
 	public void setParent(RationaleElement parent) {
 		if (parent != null)
 		{
 			this.parent = parent.getID();
 			this.ptype = parent.getElementType();
 		}
 	}
 	
 	/**
 	 * Delete our alternative from the database. This will only happen if there
 	 * are no dependencies (no arguments, questions, or sub-decisions)
 	 * @return true if sucessful
 	 */
 	public boolean delete()
 	{
 		//need to have a way to inform if delete did not happen
 		//can't delete if there are dependencies...
 		if ((this.argumentsAgainst.size() > 0) ||
 				(this.argumentsFor.size() > 0) ||
 				(this.relationships.size() > 0) ||
 				(this.questions.size() > 0) ||
 				(this.subDecisions.size() > 0))
 		{
 			MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when there are sub-elements.");
 			
 			return true;
 		}
 		
 		if (this.artifacts.size() > 0)
 		{
 			MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when code is associated!");
 			return true;
 		}
 		RationaleDB db = RationaleDB.getHandle();
 		
 		//are there any dependencies on this item?
 		if (db.getDependentAlternatives(this).size() > 0)
 		{
 			MessageDialog.openError(new Shell(),	"Delete Error",	"Can't delete when there are depencencies.");
 			return true;
 		}
 		
 		m_eventGenerator.Destroyed();
 		
 		db.deleteRationaleElement(this);
 		return false;
 		
 	}
 	
 	/**
 	 * Update our status by inferenging over the rationale
 	 * @return new status values
 	 */
 	public Vector<RationaleStatus> updateStatus()
 	{
 		AlternativeInferences inf = new AlternativeInferences();
 		Vector<RationaleStatus> newStat = inf.updateAlternative( this);
 		return newStat;
 	}
 	
 	/**
 	 * Update status when the element is deleted.
 	 * @return new status values
 	 */
 	public Vector<RationaleStatus> updateOnDelete()
 	{
 		AlternativeInferences inf = new AlternativeInferences();
 		Vector<RationaleStatus> newStat = inf.updateOnDelete( this);
 		return newStat;
 	}
 	
 	/**
 	 * Is this alternative enabled? For alternatives, an alternative is 
 	 * enabled if it has been Adopted (Selected)
 	 * @return true if selected
 	 */
 	public boolean getEnabled()
 	{
 		if (status == AlternativeStatus.ADOPTED)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * Read in an alternative from an XML representation
 	 * @param altN - the alternative in XML
 	 */
 	public void fromXML(Element altN) {
 		this.fromXML = true;
 		RationaleDB db = RationaleDB.getHandle();
 		
 		//add idref ***from the XML***
 		String idref = altN.getAttribute("id");
 		
 		//get our name
 		name = altN.getAttribute("name");
 		
 		//get our status
 		status = AlternativeStatus.fromString(altN.getAttribute("status"));
 		
 		//get our evaluation
 		evaluation = Float.parseFloat(altN.getAttribute("evaluation"));
 		
 		Node descN = altN.getFirstChild();
 		//get the description
 		//the text is actually the child of the element, odd...
 		Node descT = descN.getFirstChild();
 		if (descT instanceof Text) {
 			Text text = (Text) descT;
 			String data = text.getData();
 			setDescription(data);
 		}
 		
 		//and last....
 		db.addRef(idref, this); //important to use the ref from the XML file!
 		
 		Element child = (Element) descN.getNextSibling();
 		
 		while (child != null) {
 			
 			String nextName;
 			
 			nextName = child.getNodeName();
 			//here we check the type, then process
 			if (nextName.compareTo("DR:argument") == 0) {
 				Argument arg = new Argument();
 				db.addArgument(arg);
 				addArgument(arg);
 				arg.fromXML(child);
 			}   else if (nextName.compareTo("DR:question") == 0) {
 				Question quest = new Question();
 				db.addQuestion(quest);
 				addQuestion(quest);
 				quest.fromXML(child);
 			}  else if (nextName.compareTo("DR:decisionproblem") == 0) {
 				Decision dec = new Decision();
 				db.addDecision(dec);
 				addSubDecision(dec);
 				dec.fromXML(child);
 			} else if (nextName.compareTo("DR:history") == 0) {
 				historyFromXML(child);
 			} else if (nextName.compareTo("argref") == 0) {
 				Node childRef = child.getFirstChild(); //now, get the text
 				//decode the reference
 				Text refText = (Text) childRef;
 				String stRef = refText.getData();
 				addArgument((Argument) db.getRef(stRef));
 				
 			} else if (nextName.compareTo("decref") == 0) {
 				Node childRef = child.getFirstChild(); //now, get the text
 				//decode the reference
 				Text refText = (Text) childRef;
 				String stRef = refText.getData();
 				addSubDecision((Decision) db.getRef(stRef));
 				
 			} else if (nextName.compareTo("questref") == 0) {
 				Node childRef = child.getFirstChild(); //now, get the text
 				//decode the reference
 				Text refText = (Text) childRef;
 				String stRef = refText.getData();
 				addQuestion((Question) db.getRef(stRef));
 				
 			} else {
 				System.out.println("unrecognized element under alternative!");
 			}
 			
 			child = (Element) child.getNextSibling();
 		} 
 	}
 	
 	/**
 	 * Forwarding Function Which Makes The Assumption THat The Two
 	 * Parameters Are Ignored.
 	 * 
 	 * @return
 	 */
 	private boolean inDatabase()
 	{
 		return inDatabase(0, null);
 	}
 	
 	/**
 	 * Check if our element is already in the database. The check is different
 	 * if you are reading it in from XML because you can do a query on the name.
 	 * Otherwise you can't because you run the risk of the user having changed the
 	 * name.
 	 * @param parentID the parent ID
 	 * @param ptype the parent type
 	 * @return true if in the database already
 	 */	
 	private boolean inDatabase(int parentID, RationaleElementType ptype)
 	{
 		boolean found = false;
 		String findQuery = "";
 		
 		if (fromXML)
 		{
 			RationaleDB db = RationaleDB.getHandle();
 			Connection conn = db.getConnection();
 			
 			//find out if this alternative is already in the database
 			Statement stmt = null; 
 			ResultSet rs = null; 
 			
 			try {
 				stmt = conn.createStatement(); 
 				findQuery = "SELECT id, parent FROM alternatives where name='" +
 				this.name + "'";
 				System.out.println(findQuery);
 				rs = stmt.executeQuery(findQuery); 
 				
 				if (rs.next())
 				{
 					int ourid;
 					ourid = rs.getInt("id");
 					this.id = ourid;
 					found = true;
 				}
 			}
 			catch (SQLException ex) {
 				// handle any errors 
 				RationaleDB.reportError(ex, "Alternative.inDatabase", findQuery); 
 			}
 			finally { 
 				RationaleDB.releaseResources(stmt, rs);
 			}
 		}
 		//If we aren't reading it from the XML, just check the ID
 		//checking the name like above won't work because the user may 
 		//have modified the name!
 		else if (this.getID() >= 0)
 		{
 			found = true;
 		}
 		return found;
 	}
 	
 	public void setParent(int parent) {
 		this.parent = parent;
 	}
 
 	public void setPtype(RationaleElementType ptype) {
 		this.ptype = ptype;
 	}
 	
 	/**
 	 * Method to add an artifact to this alternative and publish an event to the same effect.
 	 * (Doesn't actually save the alternative to the DB, because artifacts are stored in the associations table.)
 	 * 
 	 * @param artName - artifact name
 	 */
 	public void addArtifact(String artName) {
 		artifacts.add(artName);
 		RationaleUpdateEvent l_updateEvent = m_eventGenerator.MakeUpdated();
 		l_updateEvent.setTag("artifacts");
 		m_eventGenerator.Broadcast(l_updateEvent);
 	}
 	
 	/**
 	 * Method to publish an event that this alternative has been affected by artifact removal.
 	 * (Doesn't actually change the alternative, as we have no way of knowing how many artifacts were removed
 	 * and also artifacts/associations are stored separately in the DB.)
 	 */
 	public void removeArtifact() {
 		RationaleUpdateEvent l_updateEvent = m_eventGenerator.MakeUpdated();
 		l_updateEvent.setTag("artifacts");
 		m_eventGenerator.Broadcast(l_updateEvent);
 	}
 }
