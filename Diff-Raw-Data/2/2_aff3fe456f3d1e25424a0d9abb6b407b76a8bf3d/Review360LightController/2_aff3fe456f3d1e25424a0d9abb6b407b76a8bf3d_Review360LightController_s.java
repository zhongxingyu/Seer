 package org.alt60m.hr.review360.servlet;
 
 import org.alt60m.servlet.*;
 import org.alt60m.util.*;
 import org.alt60m.hr.review360.model.dbio.*;
 import org.alt60m.ministry.model.dbio.Staff;
 import org.alt60m.ministry.model.dbio.OldAddress;
 
 import java.util.*;
 
 import org.apache.log4j.*;
 
 import javax.servlet.http.*;
 import javax.mail.internet.AddressException;
 
 /**
  * Review360Controller
  *
  * Purpose:
  *		Defines a web controller for the Review360 project
  *
  * Dependencies:
  *		StaffUtils SessionBean
  *
  * History:
  *		3/19/01		M Petrotta		Initial Coding --- * 
  *      1/30/02		J Burbage		Conversion from Review360 to Review360Light ---*
  * 		07/03		K O'Brien		DBIO Refactoring and Repackaging --- *
  *		10/04		S Paulis		Added email confirmation functionality
  */
 public class Review360LightController extends Controller
 {	
 
 	// IN *****************************************************************
 	// Standard request parameters tokens
 	private static final String REVIEW_SESSION_ID_TOKEN =	"reviewSessionLightId";
 	private static final String REVIEW_SESSION_FULL_ID_TOKEN =	"reviewSessionId";
 	private static final String REVIEW_360_ID_TOKEN =		"review360LightId";		
 //	private static final String REVIEW_360_FULL_ID_TOKEN =		"review360Id";
 	private static final String LAST_ACTION_TOKEN =			"lastAction";
 
 	private final String VIEWS_FILE = "/WEB-INF/R360Lviews.xml";
 //	private final String USERS_FILE = "/WEB-INF/R360Lusers.xml";
 
     private final String DEFAULT_ACTION = "showIndex";
 
 //    private Hashtable usersRoles = new Hashtable();
 	
 	public Review360LightController()  {
 		
 		log.debug("review360LightController constructor");
 		
 	}
 
 	/**
 	 * Defines functionality common to web controllers.
 	 * 
 	 */	
 	static public void main(String[] args) {
 	}		
 	
 	/**
 	 * Expectatations:
 	 *		
 	 *	Returns:
 	 *		
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
 	public void init()
    {
 	   log.debug("Review360LightController.init()");
 
         super.setViewsFile(getServletContext().getRealPath(VIEWS_FILE));
 		super.setDefaultAction(DEFAULT_ACTION);
 
 		// client = new secant.extreme.Client();
 //		initUsers(false);
 
    }
 
 /*    private void initUsers(boolean verbose) {
 		usersRoles = UsersProcessor.parse( getServletContext().getRealPath(USERS_FILE) );
 		if (verbose) {
 			for (Enumeration e = usersRoles.keys(); e.hasMoreElements();) {
 				String k = (String) e.nextElement();
 				log.debug(k + " " + usersRoles.get(k));
 			}
 			log.debug("finished loading users.");
 		}
     }
 */
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: AccountNo (in session)
 	 *	Returns:
 	 *		tub returns two element vector:
 	 *			-Vector(0) "Assignment list"
 	 *				-Vector(0..n) contain a Hashtable
 	 *	Goes to:
 	 *		View showIndex
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		5/18/2001	MAS		Added ActionResults
 	 *		7/14/2003	KDO		DBIO Refactor
 	*/
    public void showIndex(ActionContext ctx)  throws javax.servlet.ServletException, java.io.IOException
    {
 		// IN TOKENS
 //		final String ACCOUNT_NO_TOKEN = "AccountNo";
 	
 		String accountNo = (String)ctx.getSession().getAttribute("accountNo");
 		ActionResults result = new ActionResults();
 
 		log.debug("Current user accountNo: " + accountNo);
 
 		HttpSession session = ctx.getSession();
 		Hashtable profile = (Hashtable)session.getAttribute("profile");
 
 //		if (usersRoles.containsKey((String)profile.get("UserName"))) {
 			result.putValue("LightAdmin", "true");
 //		} else {
 //			result.putValue("LightAdmin", "false");
 //		}
 
 		try
 		{
 			result.addCollection("AssignmentList", createFullAssignmentList(accountNo));
 			result.addCollection("AssignmentListLight", createAssignmentList(accountNo));
 
 			size2session(ctx.getSession());
 
 			ctx.setReturnValue(result);
 			ctx.goToView("showIndex");
 
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
 
    public void thank(ActionContext ctx) 
    {
 	
 		String accountNo = "";
 		if (ctx.getSession().getAttribute("accountNo")!=null) {
 			accountNo = (String)ctx.getSession().getAttribute("accountNo");
 		} else if (ctx.getProfile() != null) {
 			accountNo = (String) ctx.getProfile().get("AccountNo");
 		}
 		
 		ActionResults result = new ActionResults();
 
 		log.debug("Current user accountNo: " + accountNo);
 
 		try
 		{
 			Collection test = new Vector();
 			Collection testLight = new Vector();
 			
 			if ( !accountNo.equals("") ) {
 				test = createAssignmentList(accountNo);
 				testLight = createAssignmentList(accountNo);
 			}
 
 			result.addCollection("AssignmentList", test);
 			result.addCollection("AssignmentListLight", testLight);
 			
 			HttpSession session = ctx.getSession();
 			
 			size2session(session);
 			
 			Hashtable profile = (Hashtable)session.getAttribute("profile");
 			result.putValue("LightAdmin", "true");
 
 			ctx.setReturnValue(result);
 			ctx.goToView("thank");
 
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
 
    
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: 
 	 *	Returns:
 	 *		
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
 	private Vector createAssignmentList(String sReviewerAcctNo) throws Exception {
 		
 		Vector assignmentList = new Vector();
 		log.debug("createAssignmentList start()");
 
 		try
 		{
 			Review360Light r = new Review360Light();
 			Vector reviews = r.selectList("reviewedByID='"+sReviewerAcctNo+"'"
 											+"and dateCompleted is null"); 
 			Iterator rlist = reviews.iterator();
 
 			while (rlist.hasNext())
 			{
 				// For each review, create a hash table to store return values in
 				Hashtable entry = new Hashtable();
 
 				Review360Light my360 = (Review360Light) rlist.next();
 				ReviewSessionLight mySession = null;
 				try
 				{
 					mySession = my360.getReviewSessionLight();
 					Staff so = mySession.getReviewee();
 
 					entry.put(REVIEW_360_ID_TOKEN, my360.encodeReview360LightID());
 					entry.put("Title", mySession.getName());
 					entry.put("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 					
 					if (my360.getDateStarted() != null)
 						entry.put("StartDate", my360.getDateStarted());
 					
 					if (my360.getDateDue() != null)
 						entry.put("DueDate", mySession.getDateDue());
 					
 					assignmentList.add(entry);
 				
 				} catch (Exception e) {
 					
 					log.error("Failed while attempting to get the session from review360: " + my360.getReview360LightId(), e);
 				}
 
 			}
 		}
 		catch (Exception e)
 		{
 			e.fillInStackTrace();
 			throw e;
 		}
 
 		return assignmentList;
 	}
 
 
 	private Vector createFullAssignmentList(String sReviewerAcctNo) throws Exception {
 		
 		Vector assignmentList = new Vector();
 		log.debug("createFullAssignmentList start() - " + sReviewerAcctNo);
 
 		try
 		{
 			// Generate a list of Reviews that a particular user needs to complete 
 			Review360 r = new Review360();
 			Vector reviews = r.selectList("reviewedByID='"+sReviewerAcctNo+"'"
 											+"and dateCompleted is null"); 
 			Iterator rlist = reviews.iterator();
 
 			while (rlist.hasNext())
 			{
 
 				// For each review, create a hash table to store return values in
 				Hashtable entry = new Hashtable();
 				
 				Review360 my360 = (Review360) rlist.next();
 				try
 				{
 					ReviewSession mySession = my360.getReviewSession();
 						
 					Staff so = mySession.getReviewee();
 					
 					entry.put(REVIEW_360_ID_TOKEN, my360.encodeReview360ID());
 					entry.put("Title", mySession.getName());
 					entry.put("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 					
 					if (my360.getDateStarted() != null)
 						entry.put("StartDate", my360.getDateStarted());
 					
 					if (my360.getDateDue() != null)
 						entry.put("DueDate", mySession.getDateDue());
 					
 					assignmentList.add(entry);				
 				
 				} catch (Exception e) {
 					
 					log.error("Failed while attempting to get the session from review360: " + my360.getReview360Id(), e);
 				}
 
 			}
 		}
 		catch (Exception e)
 		{
 			e.fillInStackTrace();
 			throw e;
 		}
 
 		return assignmentList;
 	}
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: 
 	 *	Returns:
 	 *		
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
 	private Vector createAdminList(String sReviewerAcctNo, boolean includeReviewInfo) throws Exception {
 
 		Vector adminList = new Vector();
 		log.debug("createAdminList start() accountNo = " + sReviewerAcctNo);
 		try
 		{
 			// Generate a list of Reviews that a particular user is administering
 			ReviewSessionLight mySession = new ReviewSessionLight();
 			Vector reviews = mySession.selectList("requestedByID= '"+sReviewerAcctNo
 														+"' OR administratorID= '"+sReviewerAcctNo
 														+"' ORDER BY dateDue DESC");
 
 			log.debug("createAdminList after doQuery - size = " + reviews.size());
 
 			Iterator rlist = reviews.iterator();
 			
 			while (rlist.hasNext())
 			{
 				mySession = (ReviewSessionLight) rlist.next();
 
 				try
 				{
 					// For each review, create a hash table to store return values in
 					Hashtable entry = new Hashtable();					
 					
 					Staff so = mySession.getReviewee();
 
 					entry.put(REVIEW_SESSION_ID_TOKEN, mySession.getReviewSessionLightId());
 					entry.put("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 					entry.put("Title", mySession.getName());
 					entry.put("StartDate", mySession.getDateStarted());
 					entry.put("DueDate", mySession.getDateDue());	
 					
 					if(includeReviewInfo){
 						Vector myReviews = getReviews(mySession);
 
 						entry.put("Reviews", myReviews);
 					}
 
 					adminList.add(entry);
 				}
 				catch (Exception e)
 				{
 					log.error("Failed on ReviewSessionLightID: " + mySession.getRevieweeId(), e);
 
 				}
 				
 			}
 		}
 		catch (Exception e)
 		{
 			e.fillInStackTrace();
 			throw e;
 		}
 
 		return adminList;
 	}
 
 	private Vector createFullAdminList(String sReviewerAcctNo, boolean includeReviewInfo) throws Exception {
 
 		Vector adminList = new Vector();
 		log.debug("createAdminList start() accountNo = " + sReviewerAcctNo);
 		try
 		{
 			// Generate a list of Reviews that a particular user is administering
 			ReviewSession mySession = new ReviewSession();
 			Vector reviews = mySession.selectList("requestedByID= '"+sReviewerAcctNo
 											+"' OR administratorID= '"+sReviewerAcctNo
 											+"' ORDER BY dateDue DESC");
 			log.debug("createAdminList after doQuery - size = " + reviews.size());
 
 			Iterator rlist = reviews.iterator();
 			while (rlist.hasNext())
 			{
 				mySession = (ReviewSession)rlist.next();
 
 				try
 				{
 					// For each review, create a hash table to store return values in
 					Hashtable entry = new Hashtable();					
 					
 					Staff so = mySession.getReviewee();
 
 					entry.put(REVIEW_SESSION_FULL_ID_TOKEN, mySession.getReviewSessionId());
 					entry.put("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 					entry.put("Title", mySession.getName());
 					entry.put("StartDate", mySession.getDateStarted());
 					entry.put("DueDate", mySession.getDateDue());	
 					
 					if(includeReviewInfo){
 						Vector myReviews = getFullReviews(mySession);
 
 						entry.put("Reviews", myReviews);
 					}
 
 					adminList.add(entry);
 				}
 				catch (Exception e)
 				{
 					log.error("Failed on ReviewSessionID: " + mySession.getRevieweeId(), e);
 
 				}
 				
 			}
 		}
 		catch (Exception e)
 		{
 			e.fillInStackTrace();
 			throw e;
 		}
 
 		return adminList;
 	}
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter:	Review360ID 
 	 *			
 	 *	Returns:
 	 *		tub returns hashtable:
 	 *			- Key = key field in Review360 ejb
 	 *			- Value = ejb field value
 	 *	Goes to:
 	 *		View Q360_0
 	 * History:
 	 *		3/20/01		MDP		Initial coding
 	 *		5/18/2001	MAS		Added ActionResults
 	 *		7/14/2003	KDO		DBIO Refactor
 	 */
  /*  public void editReview360(ActionContext ctx) 
    {
 		try
 		{
 			ActionResults result = new ActionResults();
 
 			// Get review to edit
 			String reviewId = (String) ctx.getInputString(REVIEW_360_ID_TOKEN, true);
 			log.debug("Review360ID=" + reviewId);
 		
 			Review360Light my360 = new Review360Light(reviewId);
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(),my360);
 			my360.persist();
 			ReviewSessionLight mySession = my360.getReviewSessionLight();
 			Staff so = mySession.getReviewee();
 			Staff so2 = mySession.getRequestedBy();
 
 			result.addHashtable("Review360", ObjectHashUtil.obj2hash(my360));
 			result.addHashtable("ReviewSession", ObjectHashUtil.obj2hash(mySession));
 			result.putValue("ReviewFor", so.getPreferredName());
 			result.putValue("Reviewee", so.getPreferredName() + " " + so.getLastName());
 			result.putValue("RequestedBy", so2.getPreferredName() + " " + so2.getLastName());
 			result.putValue("IsMale", new Boolean(so.getIsMale()).toString());
 
 			ctx.setReturnValue(result);
 			ctx.goToView("Q360_0");
 
 		}
 		catch (Exception e)
 		{
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform editReview360()",  e);
 		}
    }
 */
 
 	/* added sep 2/15/05
 		Displays an 360 review form to be edited.  This is the entry point for
 		a reviewer access their review form.  The person does NOT have to be
 		logged in.  They can access the review form, and that's it!
 		- To get here, the user clicks on the direct link contained in an invitation email like:
 		http://www....com/.../servlet/Review360Controller?action=rev360FormEncEdit&encRev360ID=1Pd84j3782kds8.
 		- Requires an input string of "encRev360ID" which is the encoded review id.
 	*/
 	public void revFormEncEdit(ActionContext ctx) {
 		try {
 			ActionResults result = new ActionResults();
 
 			Hashtable hs = new Hashtable();
 			String page = "";
 			Review360Light rev = new Review360Light();
 			
 			
 			// get the encrypted review form ID
 			String encryptedRevID = ctx.getInputString("encRevID");
 			log.debug("refFormEncEdit: encryptedReviewID=" + encryptedRevID);
 			if (encryptedRevID == null)	{
 				log.debug("revFormEncEdit: encRevID not specified");
 				page = "revNotFound";
 				ctx.goToView(page);
 				return;
 			}
 
 			// decrypt the encrypted referenceID
 			String revID = rev.decodeReview360LightID(encryptedRevID);
 			rev.setReview360LightId(revID);
 			if (!rev.select()) {
 				rev = null;
 			}
 			if (rev == null) {
 				System.err.println("revFormEncEdit: rev is null: Review360Light id=" + revID + " not found.");
 				page = "revNotFound";
 				ctx.goToView(page);
 				return;
 			}
 
 			// Display an edit page, send object info to it.
 			ReviewSessionLight mySession = rev.getReviewSessionLight();
 			Staff so = mySession.getReviewee();
 			Staff so2 = mySession.getRequestedBy();
 			
 			result.addHashtable("Review360",ObjectHashUtil.obj2hash(rev));
 			result.addHashtable("ReviewSession", ObjectHashUtil.obj2hash(mySession));
 			result.putValue("ReviewFor", so.getPreferredName());
 			result.putValue("Reviewee", so.getPreferredName() + " " + so.getLastName());
 			result.putValue("RequestedBy", so2.getPreferredName() + " " + so2.getLastName());
 			result.putValue("IsMale", new Boolean(so.getIsMale()).toString());
 
 			ctx.setReturnValue(result);
 			ctx.goToView("Q360_0");
 			
 		} catch(Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Exception encountered in Review360LightController.revFormEncEdit()", e);
 		}
 	}
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: Review360ID
 	 *		Parameter: Section	
 	 *	Returns:
 	 *		tub returns hashtable:
 	 *			- Key = field in Review360 ejb
 	 *	Goes to:
 	 *		View Q360_n (where n = 0..# of sections)
 	 * History:
 	 *		3/21/01		MDP		Initial coding
 	 *		5/18/2001	MAS		Added ActionResults
 	 *		7/14/2003	KDO		DBIO Refactor
 	 */
    public void editReview360Question(ActionContext ctx) 
    {
 		
 		final String FILL_OUT_SECTION_TOKEN = "FillOutSection";
 	
 		try
 		{	
 			ActionResults result = new ActionResults();
 
 			String reviewId = ctx.getInputString(REVIEW_360_ID_TOKEN, true);
 			String goToQuestion = ctx.getInputString(FILL_OUT_SECTION_TOKEN, true);
 			
 			Review360Light my360 = new Review360Light(reviewId);
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(),my360);
 			my360.persist();
 			ReviewSessionLight mySession = my360.getReviewSessionLight();
 			
 			Staff so = mySession.getReviewee();
 			Staff so2 = mySession.getRequestedBy();
 			
 			result.addHashtable("Review360", ObjectHashUtil.obj2hash(my360));
 			result.addHashtable("ReviewSession", ObjectHashUtil.obj2hash(mySession));
 			result.putValue("ReviewFor", so.getPreferredName());
 			result.putValue("Reviewee", so.getPreferredName() + " " + so.getLastName());
 			result.putValue("RequestedBy", so2.getPreferredName() + " " + so2.getLastName());
 			result.putValue("IsMale", new Boolean(so.getIsMale()).toString());
 
 			ctx.setReturnValue(result);
 			ctx.goToView("Q360_"+goToQuestion);
 		}
 		catch (Exception e)
 		{
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform editReview360Question()", e);
 		}
    }
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: Review360ID
 	 *
 	 *	Returns:
 	 *		tub returns empty
 	 *	Goes to:
 	 *		Action showIndex
 	 * History:
 	 *		3/20/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
 	public void saveReview360(ActionContext ctx) 
    {		
 		
 		try
 		{
 			String reviewId = (String) ctx.getInputString(REVIEW_360_ID_TOKEN, true);
 			Review360Light rl = new Review360Light(reviewId);
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(),rl);
 			rl.persist();
 //			 TODO: modify post-save behavior
 			size2session(ctx.getSession());
 			showIndex(ctx);
 		}
 		catch (Exception e)
 		{
 			log.error("Failed to perform saveReview360()", e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 	}
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: Review360ID
 	 *
 	 *	Returns:
 	 *		tub returns empty
 	 *	Goes to:
 	 *		Action showIndex
 	 * History:
 	 *		3/20/01		MDP		Initial coding
 	 */
    public void commitReview360(ActionContext ctx) 
    {
 			
 		try
 		{
 			String review360Id = (String) ctx.getInputString(REVIEW_360_ID_TOKEN, true);
 			Review360Light rl = new Review360Light(review360Id);
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(),rl);
 			Date currentDate = new Date(new Date().getTime());
 			rl.setDateCompleted(currentDate);
 			rl.persist();
 			if (!rl.getReviewedById().equals("")) {
 				thank(ctx);
 			}
 			else {
 				ctx.goToView("showIndexNonStaff");
 			}
 
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
 
   
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *	Returns:
 	 *		tub returns empty
 	 *	Goes to:
 	 *		Action listAdminSessions
 	 * History:
 	 *		3/22/01		MDP		Initial coding
 	 */  
 	public void deleteReviewSession(ActionContext ctx) 
    {
 
 		try
 		{
 			String reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN);
 			log.debug("reviewSessionID: " + reviewSessionId);
 
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionId);
 
 			Vector v = mySession.getReview360Lights();
 			Iterator rlist = v.iterator();
 			
 			while (rlist.hasNext())
 			{
 				Review360Light my360 = (Review360Light)rlist.next();
 				my360.delete();
 			}
 			mySession.delete();
 			
 			listAdminSessions(ctx);
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: 
 	 *	Returns:
 	 *		tub returns vector:
 	 *			-Vector(0..n) contain a Hashtable
 	 *	Goes to:
 	 *		View listAdminSessions
 	 * History:
 	 *		3/21/01		MDP		Initial coding
 	 */
 	public void listAdminSessions(ActionContext ctx) 
    {
 		ActionResults result = new ActionResults();
 		
 		try
 		{
 			String accountNo = (String)ctx.getSession().getAttribute("accountNo");
 			HttpSession session = ctx.getSession();
 			Hashtable profile = (Hashtable)session.getAttribute("profile");
 
 //			if (usersRoles.containsKey((String)profile.get("UserName"))) {
 				result.putValue("LightAdmin", "true");
 //			} else {
 //				result.putValue("LightAdmin", "false");
 //			}
 
 			result.addCollection("AdminList", createFullAdminList(accountNo, true));
 			result.addCollection("AdminListLight", createAdminList(accountNo, true));
 
 			ctx.setReturnValue(result);
 			size2session(ctx.getSession());
 			ctx.goToView("listAdminSessions");
 		}
 		catch (Exception e)
 		{
 			log.error("Couldn't list admin sessions", e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 		
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *	Returns:
 	 *		tub returns hashtable:
 	 *			- Key = field in ReviewSession entity bean
 	 *	Goes to:
 	 *		View editReviewSessionDetails
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 */
    public void editReviewSessionDetails(ActionContext ctx) 
    {
 		try
 		{
 			ActionResults result = new ActionResults();
 
 			// Get review to edit
 			String reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN);
 			log.debug("reviewSessionLightID: " + reviewSessionId);
 
 			ReviewSessionLight rso = new ReviewSessionLight(reviewSessionId);
 			result.addHashtable("ReviewSession", ObjectHashUtil.obj2hash(rso));
 			Staff so = rso.getRequestedBy();
 			result.putValue("RequestedByName",  so.getPreferredName() + " " + so.getLastName() + " &lt;" + so.getEmail() + "&gt;");
 			so = rso.getReviewee();
 			result.putValue("ReviewFor",  so.getPreferredName() + " " + so.getLastName() + " &lt;" + so.getEmail() + "&gt;");
 			
 			ctx.setReturnValue(result);
 			ctx.goToView("editReviewSessionDetails");
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *	Returns:
 	 *		Tub returns empty
 	 *	Goes to:
 	 *		View editReviewSessionDetails
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
    public void saveReviewSessionDetails(ActionContext ctx) 
    {
 
 		try
 		{
 			String reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN);
 			log.debug("saveReviewSessionDetails - reviewSessionLightID" + reviewSessionId);
 			
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionId);
 			java.text.SimpleDateFormat myDate = new java.text.SimpleDateFormat();
 			myDate.applyPattern("MM/dd/yyyy"); 
 			mySession.setDateDue(new Date(myDate.parse(ctx.getInputString("dateDue")).getTime()));
 			mySession.setName(ctx.getInputString("name"));
 			mySession.setPurpose(ctx.getInputString("purpose"));
 
 			mySession.persist();
 
 			if(ctx.getInputString("returnAction").equals("listAdminSessions")){
 				listAdminSessions(ctx);
 			} else {
 				editReviewSession(ctx);	
 			}
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: Section (may be empty)
 	 *	Returns:
 	 *		tub returns empty
 	 *	Goes to:
 	 *		View newReviewSession(n) (where n=1 to # of sections)
 	 * History:
 	 *		3/22/01		MDP		Initial coding
 	 *		5/18/2001	MAS		Added ActionResults
 	 *		7/14/2003	KDO		DBIO Refactor
 	 */
    public void newReviewSession(ActionContext ctx) 
    {
 		try
 		{
 			ActionResults result = new ActionResults();
 			String section = ctx.getInputString("section");
 			size2session(ctx.getSession());
 
 
 			if (section == null) {
 				section = "1";	
 			} else if (section.equals("3")){
 				log.debug("adding requestedbyname:" + (String)ctx.getInputString("requestedById", true) +  " for section 3");
 
 				Staff so = new Staff(ctx.getInputString("requestedById", true));
 
 				result.putValue("RequestedByName",  so.getPreferredName() + " " + so.getLastName() + " &lt;" + so.getEmail() + "&gt;");
 
 				so = new Staff(ctx.getInputString("revieweeId", true));
 				result.putValue("ReviewFor",  so.getPreferredName() + " " + so.getLastName() + " &lt;" + so.getEmail() + "&gt;");
 			}
 			
 			ctx.setReturnValue(result);
 			ctx.goToView("newReviewSession"+section);
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *	Returns:
 	 *		tub returns empty
 	 *	Goes to:
 	 *		Action editReviewSession
 	 * History:
 	 *		3/22/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
 	public void saveReviewSession(ActionContext ctx) 
    {
 		log.debug("saveReviewSession");
 
 		try
 		{
 			ReviewSessionLight mySession = new ReviewSessionLight();
 			java.text.SimpleDateFormat myDate = new java.text.SimpleDateFormat();
 			myDate.applyPattern("MM/dd/yyyy"); 
 			mySession.setName(ctx.getInputString("name"));
 			mySession.setPurpose(ctx.getInputString("purpose"));
 			mySession.setDateDue(new Date(myDate.parse(ctx.getInputString("dateDue")).getTime()));
 			mySession.setDateStarted(new Date(new Date().getTime()));
 			mySession.setRevieweeId(ctx.getInputString("revieweeId"));
 			mySession.setAdministratorId((String)ctx.getSession().getAttribute("accountNo"));
 			mySession.setRequestedById(ctx.getInputString("requestedById"));
 			mySession.persist();
 			
 			String reviewSessionId = mySession.getReviewSessionLightId();
 			log.debug("reviewsessionID = " + reviewSessionId);
 
 			ActionResults result = new ActionResults();
 			result.addHashtable("ReviewSession", getReviewSession(reviewSessionId));
 
 			ctx.setReturnValue(result);
 			size2session(ctx.getSession());
 			ctx.goToView("editReviewSession");			
 
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: 
 	 *	Returns:
 	 *		tub contains Vector of Hashtables
 	 *			- Vector ( 0.. n-1) contains a Hashtable
 	 *				-Hashtable represents a reviewer.
 	 *	Goes to:
 	 *		Action showReviewerList
 	 *		
 	 * History:
 	 *		3/22/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
    public void addReviewer(ActionContext ctx) 
    {
 		try
 		{
 			ActionResults result = new ActionResults();
 			ctx.setReturnValue(result);			
 			ctx.goToView("addReviewer");
 		} 
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: 
 	 *	Returns:
 	 *		tub contains Vector of Hashtables
 	 *			- Vector ( 0.. n-1) contains a Hashtable
 	 *				-Hashtable represents a reviewer.
 	 *	Goes to:
 	 *		Action showReviewerList
 	 *		
 	 * History:
 	 *		3/22/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
   public void addNonStaffReviewer(ActionContext ctx) 
   {
 		try
 		{
 			ActionResults result = new ActionResults();
 			ctx.setReturnValue(result);			
 			ctx.goToView("addNonStaffReviewer");
 		} 
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
   }
    
   
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *		Parameter: title
 	 *		Parameter: firstName
 	 *		Parameter: lastName
 	 *		Parameter: email
 	 *		Parameter: isMale
 	 *	Returns:
 	 *
 	 *	Goes to:
 	 *		Action listAdminSessions	
 	 * History:
 	 *		2/22/05		SEP		Initial coding
 	 */
 	public void createNonStaffReviewer(ActionContext ctx) 
  {
 		try
 		{
 			String reviewSessionLightId = ctx.getInputString("reviewSessionLightId", true);
 			
 			String title = ctx.getInputString("title");
 			String firstName = ctx.getInputString("firstName");
 			String lastName = ctx.getInputString("lastName");
 			String email = ctx.getInputString("email");
 			//String isMale = ctx.getInputString("isMale");
 						
 			Review360Light myReview = new Review360Light();
 			
 			// TODO: figure out where to store the name and email address (since we can't get it from the staff record
 			// in the Review360 record?
 			myReview.setReviewedByTitle(title);
 			myReview.setReviewedByFirstName(firstName);
 			myReview.setReviewedByLastName(lastName);
 			myReview.setReviewedByEmail(email);
 			//myReview.setReviewedByIsMale(isMale);
 			
 			
 			Date currentDate = new Date(new Date().getTime());
 			myReview.setDateStarted(currentDate);
 			
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionLightId);
 			log.debug("reviewSessionLightId = " + mySession.getReviewSessionLightId());
 			myReview.setDateDue(mySession.getDateDue());
 			myReview.assocReviewSessionLight(mySession);
 			Vector reviews = mySession.getReview360Lights();
 			log.debug((reviews.contains(myReview) ? "object in set" : "object not in set"));
 			
 			myReview.persist();
 			mySession.persist();
 
 			if(ctx.getInputString("returnAction").equals("listAdminSessions")){
 				listAdminSessions(ctx);
 			} else {
 				editReviewSession(ctx);			
 			}
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
  }
   
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *		Parameter: reviewerID
 	 *	Returns:
 	 *
 	 *	Goes to:
 	 *		Action editReviewSession
 	 * History:
 	 *		3/22/01		MDP		Initial coding
 	 */
 	public void associateReviewer(ActionContext ctx) 
    {
 		try
 		{
 			String reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN, true);
 			String reviewerId = ctx.getInputString("reviewerId", true);
 
 			Review360Light myReview = new Review360Light();
 			Date currentDate = new Date(new Date().getTime());
 
 			myReview.setDateStarted(currentDate);
 			myReview.setReviewedById(reviewerId);
 
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionId);
 			myReview.setDateDue(mySession.getDateDue());
 			
 			myReview.assocReviewSessionLight(mySession);
 
 			myReview.persist();
 
 			if(ctx.getInputString("returnAction").equals("listAdminSessions")){
 				getServletConfig().getServletContext().getRequestDispatcher("/servlet/Review360Controller?action=listAdminSessions").forward(ctx.getRequest(), ctx.getResponse());
 				//listAdminSessions(ctx);
 			} else {
 				editReviewSession(ctx);			
 			}
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
    }
    
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *		Parameter: Review360ID
 	 *	Returns:
 	 * Goes to:
 	 *		Action editReviewSession
 	 * History:
 	 *		3/22/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
 	public void deleteReviewer(ActionContext ctx) 
    {
 
 		try
 		{
 			String reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN);
 			String review360Id = ctx.getInputString(REVIEW_360_ID_TOKEN);
 			
 			String encryptedRevID = ctx.getInputString("encRevId");
 			Review360Light myReview = new Review360Light();
 			
 			// decrypt the encrypted referenceID
 			String revID = myReview.decodeReview360LightID(encryptedRevID);
 			myReview.setReview360LightId(revID);
 			
 			if (!myReview.select()) {
 				myReview = null;
 			}
 			if (myReview == null) {
 				System.err.println("deleteReviewer: rev is null: Review360Light id=" + revID + " not found.");
 				ctx.goToView("revNotFound");
 				return;
 			}
 			
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionId);
 			
 			log.debug("reviewSessionLightID: " + reviewSessionId + ", review360LightID: " + review360Id);
 			
 			mySession.dissocReview360Light(myReview);
 			myReview.delete();
 			
 			// goto action
 			if(ctx.getInputString("lastAction").equals("listAdminSessions")){
 				getServletConfig().getServletContext().getRequestDispatcher("/servlet/Review360Controller?action=listAdminSessions").forward(ctx.getRequest(), ctx.getResponse());
 				//listAdminSessions(ctx);
 			} else {
 				editReviewSession(ctx);			
 			}
 
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
 	
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: name = name substring
 	 *	Returns:
 	 *		
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		5/18/2001	MAS		Added ActionResults
 	 *		7/14/2003	KDO		DBIO Refactor
 	 */
 	public void listStaff(ActionContext ctx)
 	{	
 		// IN TOKENS
 		final String NAME_SUBSTRING_TOKEN = "name";
 
 		try {
 			ActionResults result = new ActionResults();
 
 			String nameSubString = ctx.getInputString(NAME_SUBSTRING_TOKEN, true);
 			nameSubString = TextUtils.formatApostrophe(nameSubString);
 
 			log.debug("listStaff substring: " + nameSubString);
 			
 			Vector list = new Vector();
 			if (nameSubString.length() != 0) {
 				Staff s = new Staff();
 				Vector v = s.selectList("UPPER(lastName) like UPPER('"+nameSubString
 										+"%') AND ((removedFromPeopleSoft='N') OR (isNonUSStaff='T')) order by lastName, preferredName");
 //										+"%') AND (removedFromPeopleSoft='N') order by lastName, preferredName");
 
 				for(Iterator iStaff = v.iterator();iStaff.hasNext();) {
 					Hashtable row = new Hashtable();
 					Staff staff = (Staff) iStaff.next();
 					OldAddress address = staff.getPrimaryAddress();
 					row.put("AccountNo", staff.getAccountNo());
 					row.put("PreferredName", staff.getPreferredName());
 					row.put("LastName", staff.getLastName());
 					if(address == null) {
 						row.put("Address1","");
 						row.put("City","");
 						row.put("State","");
 						row.put("Zip","");
 					}
 					else {
 						row.put("Address1", address.getAddress1());
 						row.put("City", address.getCity());
 						row.put("State", address.getState());
 						row.put("Zip", address.getZip());
 					}
 					list.add(row);
 				}
 			}
 
 			result.addCollection("StaffList",list);
 			
 			ctx.setReturnValue(result);
 			ctx.goToView(ctx.getInputString("lastAction"));
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: ReviewSessionID
 	 *	Returns:
 	 *		tub returns two element Vector
 	 *			-Vector(0) = Hashtable containing session values
 	 *			-Vector(1) = Vector of reviews (0 to n)
 	 *				- Vector(0..n) contain a Hashtable of review values
 	 *	Goes To:
 	 *		View showReviewSummary
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */   
 	public void showReviewSummary(ActionContext ctx)
 	{
 		ActionResults result = new ActionResults();
 
 //		Vector tub = new Vector(2);
 		
 		Vector reviews = new Vector();
 		String review360Id;
 
 		Review360Light myReview;
 		ReviewSessionLight mySession;
 		String reviewSessionId = "";
 		try
 		{
 
 			if(ctx.getInputString(REVIEW_SESSION_ID_TOKEN) != null){
 				reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN);
 				mySession = new ReviewSessionLight(reviewSessionId);
 			} else {
 				String encryptedRevID = ctx.getInputString("encRevID");
 				Review360Light ro = new Review360Light();
 				
 				// decrypt the encrypted referenceID
 				String revID = ro.decodeReview360LightID(encryptedRevID);
 				ro.setReview360LightId(revID);
 				
 				if (!ro.select()) {
 					ro = null;
 				}
 				if (ro == null) {
 					System.err.println("showReviewSummary: rev is null: Review360Light id=" + revID + " not found.");
 					ctx.goToView("revNotFound");
 					return;
 				}
 			
 				mySession = ro.getReviewSessionLight();
 				reviewSessionId = mySession.getReviewSessionLightId();
 			}
 
 			Staff so = mySession.getReviewee();
 			Staff so2 = mySession.getRequestedBy();
 			result.addHashtable("ReviewSession", ObjectHashUtil.obj2hash(mySession));
 			result.putValue("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 			result.putValue("ReviewForPName", so.getPreferredName());
 			result.putValue("RequestedByName", so2.getPreferredName() + " " + so2.getLastName());
 			result.putValue("IsMale", String.valueOf(so.getIsMale()));
 
 			log.debug("showReviewSummary reviewSessionID = " + reviewSessionId);
 
 			if(ctx.getInputString("encRevID") != null)
 			{
 				String encryptedRevID = ctx.getInputString("encRevID");
 				myReview = new Review360Light();
 				
 				// decrypt the encrypted referenceID
 				String revID = myReview.decodeReview360LightID(encryptedRevID);
 				log.debug("showReviewSummary - single review - review360ID = " + revID);
 				
 				myReview.setReview360LightId(revID);
 				
 				if (!myReview.select()) {
 					myReview = null;
 				}
 				if (myReview == null) {
 					System.err.println("showReviewSummary: rev is null: Review360Light id=" + revID + " not found.");
 					ctx.goToView("revNotFound");
 					return;
 				}
 				reviews.add(ObjectHashUtil.obj2hash(myReview));
 
 			} else {
 
 				Collection c = mySession.getReview360Lights();
 				log.debug("showReviewSummary - adding all " + c.size() + " reviews");
 				Iterator rlist = c.iterator();
 				while (rlist.hasNext())
 				{
 					myReview = (Review360Light)rlist.next();
 					reviews.add(ObjectHashUtil.obj2hash(myReview));
 
 				}
 				
 			}
 			result.addCollection("Reviews", reviews);
 
 			ctx.setReturnValue(result);
 			ctx.goToView("showReviewSummary");
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: 
 	 *	Returns:
 	 *		
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		7/14/03		KDO		DBIO Refactor
 	 */
    public void editReviewSession(ActionContext ctx){
 		try
 		{
 			String reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN, true);
 			ActionResults result = new ActionResults();
 
 			log.debug("editReviewSession reviewSessionID = " + reviewSessionId);
 			result.addHashtable("ReviewSession", getReviewSession(reviewSessionId));
 			ctx.setReturnValue(result);
 			ctx.goToView("editReviewSession");
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: 
 	 *	Returns:
 	 *		
 	 * History:
 	 *		3/19/01		MDP		Initial coding
 	 *		7/14/03		KDO		DIBO Refactor
 	 */
 	private Hashtable getReviewSession(String reviewSessionId) throws Exception
    {
    			// For each review, create a hash table to store return values in
 			Hashtable entry = new Hashtable();
 			log.debug("getReviewSession reviewsessionID = " + reviewSessionId);
 
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionId);
 
 			Staff so = mySession.getReviewee();
 
 			entry.put(REVIEW_SESSION_ID_TOKEN, mySession.getReviewSessionLightId());					
 			entry.put("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 			entry.put("Title", mySession.getName());
 			entry.put("StartDate", mySession.getDateStarted());
 			entry.put("DueDate", mySession.getDateDue());	
 			entry.put("Purpose", mySession.getPurpose());	
 
 			Vector myReviews = getReviews(mySession);
 			entry.put("Reviews", myReviews);
 
 			return entry;
 
    }
 
    private void size2session(javax.servlet.http.HttpSession session) throws Exception
    {
 		String accountNo = (String) session.getAttribute("accountNo");
 		
 		session.setAttribute("rsize", new Integer(createFullAssignmentList(accountNo).size() + createAssignmentList(accountNo).size()));
 		session.setAttribute("asize", new Integer(createFullAdminList(accountNo, false).size() + createAdminList(accountNo, false).size()) );
 
    }
 
    private Vector getReviews(ReviewSessionLight mySession) throws Exception
 	{
 		Iterator r360list = mySession.getReview360Lights().iterator();
 		log.debug("# of reviews - " + mySession.getReview360Lights().size());
 
 		Vector myReviews = new Vector();
 		
 		while (r360list.hasNext()){
 			Hashtable review = new Hashtable();
 			Review360Light my360 = (Review360Light)r360list.next();
 			java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat ("MM/dd/yyyy");
 
 			log.debug("looking up staff " + my360.getReviewedById());							
 			
 			try {
 				if("".equals(my360.getReviewedById()))	// no staff number
                 	throw new Exception();
                 else
                 {
 	                Staff so = my360.getReviewedBy();
 					log.debug("found staff " + so.getPreferredName() + " " + so.getLastName());
 					
 					review.put("PsEmail", so.getEmail());
 					review.put("ReviewedByName", so.getPreferredName() + " " + so.getLastName());
                 }
 			} catch (Exception notfound) {
 				log.warn("Didn't find staff: " + my360.getReviewedById(), notfound);
 
 				review.put("PsEmail", "".equals(my360.getReviewedByEmail())?"<not available>":my360.getReviewedByEmail());
                 review.put("ReviewedByName", "".equals(my360.getReviewedByFirstName()+my360.getReviewedByLastName())?"<not available>":my360.getReviewedByFirstName()+" "+my360.getReviewedByLastName());
 
 			}
 
 			// Catch if copmlete date is null
 			String completeDate = "";
 
 			if(my360.getDateCompleted() == null){
 				completeDate = "N/A";
 			} else{
 				completeDate = dateFormatter.format((Date)my360.getDateCompleted());
 			}
 			
 			review.put("DateCompleted", completeDate);
 			//review.put("Review360Id", my360.getReview360LightId());
 			review.put("Review360Id", my360.encodeReview360LightID());
 			review.put("LeadershipLevel", my360.getLeadershipLevel());
 			review.put("Relationship", my360.getRelationship());
 			review.put("CurrentPosition", my360.getCurrentPosition());
 			myReviews.add(review);
 		}
 		return myReviews;
 	}
 
    private Vector getFullReviews(ReviewSession mySession) throws Exception
 	{
 		Iterator r360list = mySession.getReview360s().iterator();
 		log.debug("# of reviews - " + mySession.getReview360s().size());
 
 		Vector myReviews = new Vector();
 
 		while (r360list.hasNext()){
 			Hashtable review = new Hashtable();
 			Review360 my360 = (Review360)r360list.next();
 			java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat ("MM/dd/yyyy");
 
 			log.debug("looking up staff " + my360.getReviewedById());							
 			
 			try {
 				if("".equals(my360.getReviewedById()))	// no staff number
                 	throw new Exception();
                 else
                 {
 	                Staff so = my360.getReviewedBy();
 					log.debug("found staff " + so.getPreferredName() + " " + so.getLastName());
 					
 					review.put("PsEmail", so.getEmail());
 					review.put("ReviewedByName", so.getPreferredName() + " " + so.getLastName());
                 }
 				
 			} catch (Exception notfound) {
 				log.info("Didn't find staff: " + my360.getReviewedById());
 
 				review.put("PsEmail", "".equals(my360.getReviewedByEmail())?"<not available>":my360.getReviewedByEmail());
                 review.put("ReviewedByName", "".equals(my360.getReviewedByFirstName()+my360.getReviewedByLastName())?"<not available>":my360.getReviewedByFirstName()+" "+my360.getReviewedByLastName());
 			}
 
 			// Catch if complete date is null
 			String completeDate = "";
 
 			if(my360.getDateCompleted() == null){
 				completeDate = "N/A";
 			} else{
 				completeDate = dateFormatter.format((Date)my360.getDateCompleted());
 			}
 			
 			review.put("DateCompleted", completeDate);
			review.put("Review360Id", my360.getReview360Id());
 			if (my360.getLeadershipLevel() != null) {
 			    review.put("LeadershipLevel", my360.getLeadershipLevel()); 
 			} else {
 			    review.put("LeadershipLevel", "not specified"); 
 			}
 			if (my360.getRelationship() != null) {
 			    review.put("Relationship", my360.getRelationship());     
 			} else {
 			    review.put("Relationship", "not specified");
 			}
 			if (my360.getCurrentPosition() != null) {
 			    review.put("CurrentPosition", my360.getCurrentPosition());     
 			} else {
 			    review.put("CurrentPosition", "not specified");
 			}
 			myReviews.add(review);
 		}
 		return myReviews;
 	}
 
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter:	reviewSessionID
 	 *					lastAction
 	 *	Returns:
 	 *		ActionResults tub:
 	 *		value = RequestedByName
 	 *		value = RequestedByEmail
 	 *		value = ReviewFor
 	 *		value = Title
 	 *		value = DueDate
 	 *		value = isMale
 	 *		value = to
 	 *		value = from
 	 *		value = subject
 	 *		value = bodytext
 	 *		
 	 * History:
 	 *		5/24/2001	MAS		Initial coding
 	 *		7/14/2003	KDO		DBIO 
 	 *		10/2004		SEP		Added functionality to preserve entered text
 
 	 */
 	public void composeEmail(ActionContext ctx)
 	{	
 
 		String lastAction;
 		String reviewSessionId;
 
 		try {
 			ActionResults result = new ActionResults();
 
 			reviewSessionId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN, true);
 			lastAction = ctx.getInputString(LAST_ACTION_TOKEN);
 
 			log.debug("composeEmail reviewSessionID: " + reviewSessionId);
 
 			java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat ("MM/dd/yyyy");
 
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionId);
 
 			Staff so = mySession.getReviewee();
 			Staff so2 = mySession.getRequestedBy();
 
 			result.putValue("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 			result.putValue("RequestedByName", so2.getPreferredName() + " " + so2.getLastName());
 			result.putValue("RequestedByEmail", so2.getEmail());
 			result.putValue("Title", mySession.getName());
 			result.putValue("DueDate", dateFormatter.format(mySession.getDateDue()));
 			result.putValue("isMale", so.getIsMale() ? "true" : "false");
 
 			
 			if(lastAction!=null)
 				result.putValue("lastAction", lastAction);
 			else
 				result.putValue("lastAction", (String) ctx.getSession().getAttribute("last_last_action"));
 			
 			if(ctx.getInputString("to")!=null)
 				result.putValue("to", (String) ctx.getInputString("to"));
 			if(ctx.getInputString("from")!=null)
 				result.putValue("from", (String) ctx.getInputString("from"));
 			if(ctx.getInputString("subject")!=null)
 				result.putValue("subject", (String) ctx.getInputString("subject"));
 			if(ctx.getInputString("bodytext")!=null)
 				result.putValue("bodytext", ctx.getInputString("bodytext"));
 			
 			ctx.setReturnValue(result);
 			ctx.goToView("composeEmail");
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 	
 	
 	public void sendEmails(ActionContext ctx)
 	{	
 
 		String lastAction;
 		String reviewSessionLightId;
 		String content;
 
 		try {
 			ActionResults result = new ActionResults();
 			
 			reviewSessionLightId = ctx.getInputString(REVIEW_SESSION_ID_TOKEN, true);
 			lastAction = ctx.getInputString(LAST_ACTION_TOKEN);
 			content = ctx.getInputString("content");
 			
 			log.debug("composeEmail reviewSessionID: " + reviewSessionLightId);
 
 			java.text.SimpleDateFormat dateFormatter = new java.text.SimpleDateFormat ("MM/dd/yyyy");
 
 			ReviewSessionLight mySession = new ReviewSessionLight(reviewSessionLightId);
 
 			Staff so = mySession.getReviewee();
 			Staff so2 = mySession.getRequestedBy();
 
 			String reviewFor = so.getPreferredName() + " " + so.getLastName();
 			String requestedByName = so2.getPreferredName() + " " + so2.getLastName();
 			String requestedByEmail = so2.getEmail();
 			String title = mySession.getName();
 			String dueDate = dateFormatter.format(mySession.getDateDue());
 			String isMale = so.getIsMale() ? "true" : "false";
 			
 			if(lastAction!=null)
 				result.putValue("lastAction", lastAction);
 			else
 				result.putValue("lastAction", (String) ctx.getSession().getAttribute("last_last_action"));
 			
 			// send all emails
 
 			// lists only checked emails
 			String emails[]= ctx.getInputStringArray("to");
 			
 			// lists ALL names and RevIDs
 			String names[]= ctx.getInputStringArray("name");
 			String encRevIds[]= ctx.getInputStringArray("encRevId");
 			
 			String strTo="";
 			String strNamesWithoutEmail="";
 			String strConfirmation = "";
 			int emailsNotFound=0;
 			for(int i = 0; i < emails.length; i++)
 			{
 				// index of "_" divider in email field    ex: 12_scott.paulis@uscm.org
 				int indexDiv = emails[i].indexOf("_");
 				
 				// get index # for names and RevIds out of email string  ex: 12
 				int index = Integer.valueOf(emails[i].substring(0,indexDiv)).intValue();
 				
 				//trim index and divider from email address
 				emails[i]=emails[i].substring(indexDiv+1);
 				try
 				{
 					
 					if( emails[i].length() == 0 )
 					{
 						emailsNotFound++;
 						strNamesWithoutEmail+= names[index];
 						throw new AddressException();
 					}
 					else
 					{		
 						// since email is not empty, send email
 						
 						if("invite".equals(content))
 						{
 							sendInvitationEmail(reviewFor,requestedByName,requestedByEmail,title,dueDate,isMale,emails[i],encRevIds[index]);
 						}
 						else// if("remind".equals(content))
 						{
 							sendReminderEmail(reviewFor,requestedByName,requestedByEmail,title,dueDate,isMale,emails[i],encRevIds[index]);
 						}
 						strConfirmation += "Email successfully sent to " + names[index] + " (" + emails[i] +")<br /><br />";
 	
 					}
 				}
 				catch (AddressException ae) {
 					log.error("Failed to send email - Missing or Bad Email address", ae);
 					strConfirmation += "Failed to send email to " + names[index] + " - Missing or Bad Email address<br /><br />";	
 				}
 				catch (Exception e)
 				{
 					log.error("Failed to send email - " , e);
 					strConfirmation += "Failed to send email to " + names[index] + " (" + emails[i] +")<br /><br />";	
 				}
 				
 			}
 			result.putValue("ConfirmationMsgs",strConfirmation);
 			
 			ctx.setReturnValue(result);
 			ctx.goToView("confirmEmails");
 
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 	
 	public void sendInvitationEmail(String reviewFor,String requestedByName,String from,String title,String dueDate,String isMale,String toEmail,String encRevId) throws Exception, AddressException 
 	{
 		ActionResults result = new ActionResults();
 		Boolean isMaleB = new Boolean(isMale);
 		String hisHer = isMaleB.booleanValue() ? "his" : "her";
 
 		String body=
 			"I want to thank you for your leadership and contribution to the mission God has given us. "
 			+ "It is an exciting time to be involved in the Campus Ministry. Students' lives are being changed around the world. "
 			+ "One of the greatest encouragements is His work in our lives, which is why I'm writing you and seeking your help."
 			+ "\n\n"
 			+ "It is our desire in the Campus Ministry to foster a \"feedback rich\" environment where all of us are getting regular input and feedback. "
 			+ "In light of this I would greatly appreciate your filling out the review for "
 			+ reviewFor + ". "
 			+ "This will be an important piece in "
 			+ hisHer 
 			+ " ongoing development and growth. Your review will be kept confidential, and I would really appreciate your honest input."
 			+ "\n\n"
 			+ "All you need to do is click on the shortcut provided below and fill out the online form."
 			+ "\n\n"
 			+ "http://staff.campuscrusadeforchrist.com/servlet/Review360LightController?action=revFormEncEdit&encRevID="+encRevId
 			+ "\n\n"
 			+ "If you could do this before "
 			+ dueDate
 			+ ", I would appreciate it."
 			+ "\n\n"
 			+ "In His grace,\n"
 			+ requestedByName;				
 		
 		String subject= "360 Review for " + reviewFor + " - '" + title + "'";
 		
 		try {
 			// create email message and send
 			SendMessage email = new SendMessage();
 
 			email.setBcc(toEmail);
 			email.setFrom(from);
 			email.setSubject(subject);
 			email.setBody(body);
 			email.send();
 			
 			log.debug("sendEmail to: " + toEmail);
 			
 		}
 		catch (AddressException ae) {
 			log.error("Failed to send email - Missing or Bad Email address", ae);
 			throw ae;
 		}
 		catch (Exception e)
 		{
 			log.error("Failed to send email - " , e);
 			throw e;
 		}			
 	}
 	
 	public void sendReminderEmail(String reviewFor,String requestedByName,String from,String title,String dueDate,String isMale,String toEmail,String encRevId) throws Exception, AddressException 
 	{
 		ActionResults result = new ActionResults();
 		Boolean isMaleB = new Boolean(isMale);
 		String hisHer = isMaleB.booleanValue() ? "his" : "her";
 
 		String body=  "This email is to remind you of your 360 Review of " + reviewFor + ".  Your input is very valuable to us!  If you have not finished this review, please remember to return to the 360 Review form so that you can finish it in a timely manner.  We greatly appreciate the part that you play in " + reviewFor + "'s spiritual life!\n\n"
 		+ "The shortcut provided below will take you to the online form."
 		+ "\n\n"
 		+ "http://staff.campuscrusadeforchrist.com/servlet/Review360LightController?action=revFormEncEdit&encRevID="+encRevId
 		+ "\n\n"
 		+ "If you could do this before "
 		+ dueDate
 		+ ", I would appreciate it."
 		+ "\n\n"
 		+ "Sincerely,\n\n"
 		+ requestedByName;
 		
 		String subject= "Reminder: 360 Review for " + reviewFor + " - '" + title + "'";
 		
 		try {
 			// create email message and send
 			SendMessage email = new SendMessage();
 
 			email.setBcc(toEmail);
 			email.setFrom(from);
 			email.setSubject(subject);
 			email.setBody(body);
 			email.send();
 			
 			log.debug("sendEmail to: " + toEmail);
 			
 		}
 		catch (AddressException ae) {
 			log.error("Failed to send email - Missing or Bad Email address", ae);
 			throw ae;
 		}
 		catch (Exception e)
 		{
 			log.error("Failed to send email - " , e);
 			throw e;
 		}			
 	}
 	
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameters:	last action
 	 *					to
 	 *					from
 	 *					subject
 	 *					bodytext
 	 *	Returns:
 	 *		ActionResults tub:
 	 *		value = lastAction
 	 *		value = errMsg
 	 *		value = to
 	 *		value = from
 	 *		value = subject
 	 *		value = bodytext
 	 *		
 	 * History:
 	 *		5/24/2001	MAS		Initial coding
 	 *		10/11/2004  SEP		Added confirmation for email sent, and passed composed email info
 	 */
 	public void sendEmail(ActionContext ctx)
 	{	
 
 		String lastAction= (String) ctx.getInputString(LAST_ACTION_TOKEN);
 //		String reviewSessionId;
 
 		ActionResults result = new ActionResults();
 		result.putValue("lastAction", lastAction);
 		result.putValue("errMsg", "");
 		
 		// pass email info through to next page in case of error 
 		// and need to come back with user entered info
 		result.putValue("to", (String) ctx.getInputString("to"));
 		result.putValue("from", (String) ctx.getInputString("from"));
 		result.putValue("subject", (String) ctx.getInputString("subject"));
 		result.putValue("bodytext", (String) ctx.getInputString("bodytext"));
 		
 		try {
 			// create email message and send
 			SendMessage email = new SendMessage();
 
 			email.setBcc(ctx.getInputString("to"));
 			email.setFrom(ctx.getInputString("from"));
 			email.setSubject(ctx.getInputString("subject"));
 			email.setBody(ctx.getInputString("bodytext"));
 			email.send();
 
 			log.debug("sendEmail to: " + ctx.getInputString("to"));
 
 		}
 		catch (AddressException e) {
 			log.error("Failed to send email - Missing or Bad Email address", e);
 			result.putValue("errMsg", "Failed to send email - Missing or Bad Email address.");
 		}
 		catch (Exception e)
 		{
 			log.error("Failed to send email - " , e);
 			result.putValue("errMsg", "Failed to send email. Make sure you have the correct address.");
 
 		}
 		ctx.setReturnValue(result);
 		ctx.goToView("confirmEmail");
 		
 	}
 	
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: lastAction
 	 *		
 	 * History:
 	 *		10/12/2004  SEP		Initial coding
 	 */
 	public void confirmEmail(ActionContext ctx)
 	{	
 
 		String lastAction= (String) ctx.getInputString(LAST_ACTION_TOKEN);
 //		String reviewSessionId;
 
 		try {
 			// goto action
 			if(lastAction== null | lastAction.equals("listAdminSessions")){
 				listAdminSessions(ctx);
 			} else {
 				editReviewSession(ctx);			
 			}
 		}
 		catch (Exception e)
 		{
 			log.error("Failed to send email - " , e);
 		}
 	}
 
 	/**
 	 * <B>Controller Action</B>
 	 * Expectatations:
 	 *		Parameter: AccountNo (in session)
 	 *	Returns:
 	 *		tub returns a Vector of 360 info
 	 *
 	 *	Goes to:
 	 *		View showIndex
 	 * History:
 	 *		5/26/2001	MAS		Initial coding
 	 *		7/14/2003	KDO		DBIO Refactor
   	 */
    public void showHistory(ActionContext ctx) 
    {
 		// IN TOKENS
 //		final String ACCOUNT_NO_TOKEN = "AccountNo";
 	
 		String accountNo = (String)ctx.getSession().getAttribute("accountNo");
 		ActionResults result = new ActionResults();
 		
 		log.debug("Current user accoutNo: " + accountNo);
 
 		try
 		{
 		Vector assignmentList = new Vector();
 		log.debug("createAssignmentList start()");
 
 		try
 		{
 			// Generate a list of Reviews that a particular user needs to complete 
 			Review360Light my360 = new Review360Light();
 			Vector v = my360.selectList("reviewedByID='"+accountNo+"'");	
 			Iterator rlist = v.iterator();
 
 			while (rlist.hasNext())
 			{
 				// For each review, create a hash table to store return values in
 				Hashtable entry = new Hashtable();
 
 				my360 = (Review360Light)rlist.next();
 				try
 				{
 					ReviewSessionLight mySession = my360.getReviewSessionLight();
 					Staff so = mySession.getReviewee();
 					
 					entry.put(REVIEW_360_ID_TOKEN, my360.encodeReview360LightID());
 					entry.put("Title", mySession.getName());
 					entry.put("ReviewFor", so.getPreferredName() + " " + so.getLastName());
 					
 					if (my360.getDateStarted() != null)
 						entry.put("StartDate", my360.getDateStarted());
 					
 					if (my360.getDateDue() != null)
 						entry.put("DueDate", mySession.getDateDue());
 					
 					assignmentList.add(entry);				
 				
 				} catch (Exception e) {
 					
 					log.error("Failed while attempting to get the session from review360: " + my360.getReview360LightId(), e);
 				}
 
 			}
 		}
 		catch (Exception e)
 		{
 			e.fillInStackTrace();
 			throw e;
 		}
 
 			result.addCollection("AssignmentList", assignmentList);
 			
 			size2session(ctx.getSession());
 
 			ctx.setReturnValue(result);
 			ctx.goToView("showHistory");
 
 		}
 		catch (Exception e)
 		{
 			log.error(e.getMessage(), e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
    }
 }
