 package com.surveybond.surveytool.business.rest;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.surveybond.model.surveytool.AnswerImpl;
 import com.surveybond.model.surveytool.Connection;
 import com.surveybond.model.surveytool.MatrixQuestion;
 import com.surveybond.model.surveytool.MultiChoicesMultiAnswer;
 import com.surveybond.model.surveytool.MultiChoicesOneAnswer;
 import com.surveybond.model.surveytool.Question;
 import com.surveybond.model.surveytool.Survey;
 import com.surveybond.model.surveytool.TextInputQuestion;
 import com.surveybond.model.surveytool.enums.MatrixTypeEnum;
 import com.surveybond.model.surveytool.enums.QuestionType;
 import com.surveybond.model.surveytool.inf.Answer;
 import com.surveybond.persistence.mongo.repositories.ConnectionRepository;
 import com.surveybond.persistence.mongo.repositories.QuestionRepository;
 import com.surveybond.persistence.mongo.repositories.SurveyRepository;
 import com.surveybond.surveytool.dto.ActionResultDTO;
 import com.surveybond.surveytool.dto.Message;
 import com.surveybond.surveytool.dto.MessageCriticalLevelEnum;
 import com.surveybond.surveytool.dto.NextPageResultDTO;
 import com.surveybond.surveytool.inf.ConnectionService;
 import com.surveybond.surveytool.inf.SurveyService;
 
 
 @Path("survey")
 public class SurveyServiceImpl implements SurveyService {
 	
 	
 	@Autowired
 	QuestionRepository  qr;
 	@Autowired
 	SurveyRepository  sr;
 
 	@Autowired
 	ConnectionService  cs;
 	
 	@Autowired
 	ConnectionRepository  cr;
 	
 	
 	@Autowired
 	MongoOperations op;
 
 	@POST
 	@Path("/")
 	@Produces({"application/json","text/xml"})
 	public Survey createSurvey(String surveyTitle) {
 		// TODO Auto-generated method stub
 		Survey s = new Survey(surveyTitle);
 		sr.save(s);
 		cs.createInitConnection(s.getId());
 		return  s;
 	}
 	
 
 	
 	
 	
 	
 	
 	
 	@GET
 	@Path("/question")
 	@Produces({"application/json","text/xml" })
 	public Question getQuestion()
 	{
 		//return QuestionCreateHelper.createSingleChoiceQuestion();
 		return null;
 	}
 	
 	
 	public Question createQuestion(String surveyId, QuestionType qtype) 
 	{
 		return createQuestion(surveyId,qtype,null);
 	}
 	
 	public Question appendQuestion(String surveyId, QuestionType qtype)
 	{
 		return appendQuestion(surveyId,qtype,null);
 	}
 	
 	@POST
 	@Path("/{surveyId}/{qtype}")
 	@Consumes({"text/xml", "application/json" })
 	public Question appendQuestion(@PathParam("surveyId")String surveyId, @PathParam("qtype")QuestionType qtype,@QueryParam("subType")String subType)
 	{
 		
 		Question q=createQuestion(surveyId,qtype,subType);
 		cs.appendConnection(q);
 		return q;
 	}
 
 
 	public Question createQuestion(String surveyId,QuestionType qtype,String subType) {
 
 		
 		Question q = null;
 		if(qtype==QuestionType.MultiChoicesOneAnswer)
 		{
 			 q = new MultiChoicesOneAnswer(surveyId);
 		}
 		if(qtype==QuestionType.MultiChoicesMultiAnswer)
 		{
 			 q = new MultiChoicesMultiAnswer(surveyId);
 		}
 		if(qtype==QuestionType.Matrix)
 		{
 			if(subType!=null)
 			{
 				q = new MatrixQuestion(surveyId,MatrixTypeEnum.valueOf(subType));
 			}
 			else
 				q = new MatrixQuestion(surveyId);
 		}
 		
 		if(qtype==QuestionType.TextInput)
 		{
 			if(subType!=null)
 			{
 				q = new TextInputQuestion(surveyId,com.surveybond.model.surveytool.enums.TextInputTypeEnum.valueOf(subType));
 			}
 		}
 	    
 		qr.save(q);
 		//cs.appendConnection(q);
 		return q;
 	}
 	
 	
 	@DELETE
 	@Path("/{surveyId}/{questionId}")
 	@Produces({"application/json","text/xml" })
 	public Set<Question> deleteQuestion(@PathParam("questionId")String questionId) {
 		// TODO Auto-generated method stub
 		//delete all connections associate with this question
 		//delete connections of source
 		//cs.deleteConnectionBySink(questionId);
 		//cs.deleteConnectionBySource(questionId);
 		Set<Question> returns=new LinkedHashSet<Question>();
 		//check if this question has incoming connection (no insert order connection) sinkId=questionId
 		List<Question> dependents=cs.getDependentQuestion(questionId);
 		if(dependents!=null&&dependents.size()>0)
 		{
 		    returns.addAll(dependents);
 		    return returns;
 		}
 		
 		cs.deleteConnectionsByQuestion(questionId);
 		qr.delete(questionId);
 		
 		return returns;
 	}
 
 	
 	@PUT
 	@Path("/{surveyId}/{questionId}")
 	@Consumes({"text/xml", "application/json" })
 	public void saveUpdateQuestion(Question question) {
 		// TODO Auto-generated method stub
 		
 		//cs.appendConnection(question);
 		qr.save(question);
 		
 	}
 
 
 
 	public Question getFirstQuestion(String surveyId) {
 		// TODO Auto-generated method stub
 		return  cs.getFirstQuestion(surveyId);
 	}
 
 
 
 
 
 	public Question getNextQuestion(String qid, Answer answer) {
 		// TODO Auto-generated method stub
 		return cs.getNextQuestion(qid, answer);
 	}
 
 
 
 
 	@GET
 	@Path("/{id}")
 	@Produces({"application/json","text/xml" })
 	public Survey printSurveyEditMode(@PathParam("id")String id) {
 		// TODO Auto-generated method stub
 		Survey s = sr.findOne(id);
 	    List<Question> qs = new ArrayList<Question>();
 		Question first = getFirstQuestion(id);
 		Question next = first;
 		do
 		{
 			 if(next==null)
 			 {
 			        break;
 			 }
 			 else
 			    	qs.add(next);
 			 
 			next = getNextQuestion(next.getId(),null);
 		
 		   
 		}while(true);
 	
 		s.setQuestions(qs);
 		
 		return s;
 	}
 
 
 
 
 
 
 
 
 	public List<Survey> getSurveys() {
 		// TODO Auto-generated method stub
 		
 		Iterator<Survey> iter=sr.findAll().iterator();
 		List<Survey> ss = new ArrayList<Survey>();
 		while(iter.hasNext())
 		{
 			ss.add(iter.next());
 		}
 		
 		return ss;
 	}
 
 
 
 
 
 
 
 /*
 	public void addPageBreak(String qId,LogicEdge logicEdge) {
 		// TODO Auto-generated method stub
 		List<Connection> clist = cr.findBySourceId(qId);
 		for(Connection c:clist)
 		{
 			c.setPageBreak("yes");
 			cr.save(c);
 		}
 	}
 
 
 	public void removePageBreak(String qId,LogicEdge logicEdge) {
 		// TODO Auto-generated method stub
 		List<Connection> clist = cr.findBySourceId(qId);
 		for(Connection c:clist)
 		{
 			c.setPageBreak("no");
 		}
 	}
 */
 	@GET
 	@Path("/dist/{id}")
 	@Produces({"application/json","text/xml" })
     public List<Question> firstPage(@PathParam("id")String id)
     {
 		List<Question> questions=new ArrayList<Question>();
 		String qid=this.getFirstQuestion(id).getId();
 		questions=this.fetchQuestionByPage(qid);
 		return questions;
     	
     }
 	
 	@POST
 	@Path("/dist/nextpage/{lqid}/{opid}")
 	@Produces({"application/json","text/xml" })
 	@Consumes({"text/xml", "application/json" })
 	public NextPageResultDTO nextPage(@PathParam("lqid")String lqid,@PathParam("opid")String opid,Map<String,List<AnswerImpl>> answersMap)
 	{
 		NextPageResultDTO npResult=new NextPageResultDTO();
 		//validate each question's answer
 		Iterator<String> ipIterIds=answersMap.keySet().iterator();
 		while(ipIterIds.hasNext())
 		{
 			String qid=ipIterIds.next();
 			List<AnswerImpl> answers = answersMap.get(qid);
 			Question q = qr.findOne(qid);
 			List<Message> mss = new ArrayList<Message>();
 			if(q.isQuestionRequired()&&answers.size()==0)
 			{
 				//required error
 				mss.add(new Message("Please complete the required question",MessageCriticalLevelEnum.error));
 				npResult.getQuestionMessages().put(qid, mss);
 			}
 		
 		}
 		
 		if(npResult.getQuestionMessages().size()>0)
 			return npResult;
 		
 	
 		List<Question> list=fetchNextQuestion(lqid,opid);
 		
 	    npResult.setQuestions(list);
 	    
 	    return npResult;
 	}
 	
 	@GET
 	@Path("/dist/nextpage/{lqid}/{opid}")
 	@Produces({"application/json","text/xml" })
 	@Consumes({"text/xml", "application/json" })
 	public List<Question> nextPage(@PathParam("lqid")String lqid,@PathParam("opid")String opid)
 	{
 		return fetchNextQuestion(lqid,opid);
 	}
 	
 	private List<Question> fetchNextQuestion(String lqid,String opid)
 	{
 		List<Question> questions=new ArrayList<Question>();
 		Question q=null;
 		if(opid.equals("-1"))
 		
 			q=cs.getNextQuestion(lqid, null,false);
 		else
 		    
 			q=cs.getNextQuestion(lqid, new AnswerImpl(opid),false);
 			
 		if(q!=null)  //q==null end of survey
 			questions=this.fetchQuestionByPage(q.getId());
 	
 		
 		return questions;
 	}
 
 	public List<Question> fetchQuestionByPage(String qid) {
 		// TODO Auto-generated method stub
 		    List<Question> qs = new ArrayList<Question>();
 			Question first = qr.findOne(qid);
 			Question next = first;
 			
 			do
 			{
 				 if(next==null)
 				 {
 				        break;
 				 }
 				 else
 				 {
 				    	qs.add(next);
 				    	
 				 }
 
 				List<Connection> ccs=cr.findBySourceId(next.getId());
 				if (ccs.size()==1)
 				{
 					//only has the default connection
 					Connection c = ccs.get(0);
 					if(c.isPageBreakYes())
 					{
 						//add the last question
 						///next = getNextQuestion(next.getId(),null);
 						break;
 					}
 					next = getNextQuestion(next.getId(),null);
 					//next = cs.getNextQuestion(qid, null,false);
 					
 				}
 				else  //questions with skip logic 
 				{
 					break;
 				}
 			    
 			   
 			}while(true);
 		
 			return qs;
 		
 	}
 
 
 
 	@DELETE
 	@Path("/connection/{cid}")
 	///@Produces({"application/json","text/xml" })
 	//@Consumes({"text/xml", "application/json" })
    public void deleteConnection(@PathParam("cid")String cid)
    {
	    /*
		Connection c = cr.findOne(cid);
	    Question q = qr.findOne(c.getSourceId());
	    
	    List<Connection> outLinks =cr.findBySourceId(q.getId());
	    
	    boolean noskip=true;
	    for(Connection cc:outLinks)
	    {
	    	if(cc.getLogicEdge()!=null&&!cc.getLogicEdge().isInsertOrder()&&!cc.getLogicEdge().unconditoinal())
	    		noskip=false;
	    }
	    if(noskip)
	    {
	    	q.setRequired("no");
	 	    qr.save(q);
	    }
	   
	   */
		cr.delete(cid);
	   
    }
    
    
 	@PUT
 	@Path("/connection")
 	@Produces({"application/json","text/xml" })
 	@Consumes({"text/xml", "application/json" })
    public Connection saveOrUpdateConnection(Connection c)
    {
 	   cr.save(c);
 	   return c;
    }
 
 
 
 	public void deleteSurvey(String sid) {
 		// TODO Auto-generated method stub
 		sr.delete(sid);
 		op.remove(new Query(Criteria.where("surveyId").is(sid)), Connection.class);
 		op.remove(new Query(Criteria.where("surveyId").is(sid)), Question.class);
 	}
 
 
 
 
 
 
 
 	@PUT
 	@Path("/markrequired")
 	@Produces({"application/json","text/xml" })
 	@Consumes({"text/xml", "application/json" })
 	public void markRequired(Question q) {
 		// TODO Auto-generated method stub
 		
 		Question qdb =  qr.findOne(q.getId());
 		
 		qdb.setRequired(q.getRequired());
 		
 		qr.save(qdb);
 		
 		
 	}
 
 
 
 
 
 
 
 
 	public ActionResultDTO copyTo(String qid, String targetQId) {
 		// TODO Auto-generated method stub
 		Question q=qr.findOne(qid);
 		q.setId(null);
 		qr.save(q);
 		
 		insertAfter(q,targetQId);
 		
 		return null;
 	}
 
 
 
 
 
 
 
 
 	public ActionResultDTO moveTo(String qid, String targetQId) {
 		// TODO Auto-generated method stub
 		//Question q=qr.findOne(qid);
 		//q.setId(null);
 		//deleteQuestion(qid);
 		//insertAfter(q,targetQId);
 		
 		return null;
 	}
 
 
 
 
 
 
 
 
 	public void insertAfter(Question question, String targetQId) {
 		// TODO Auto-generated method stub
 		//targetQId
 		//find outgoing connection
 		List<Connection> outgoings = cr.findBySourceId(targetQId);
 		String surveyId=question.getSurveyId();
 		String qid=question.getId();
         Connection newC = new Connection(surveyId,targetQId,qid);
         
         for(Connection c:outgoings)
         {
         	c.setSourceId(qid);
         }
         
         cr.save(outgoings);
         cr.save(newC);
 		
 	}
 
 
 
 
 
 
 
 
 	public void detachQuestion(String qId) {
 		// TODO Auto-generated method stub
 		
 		
 		
 	}
 
 
 
 
 
 
 
 	@PUT
 	@Path("/markpagebreak")
 	@Produces({"application/json","text/xml" })
 	@Consumes({"text/xml", "application/json" })
 	public ActionResultDTO markPageBreak(Question q) {
 		// TODO Auto-generated method stub
 		List<Connection> clist = cr.findBySourceId(q.getId());
 		ActionResultDTO actRslt=new ActionResultDTO();
 		List<Message> msgs = new ArrayList<Message>();
 		actRslt.setMessages(msgs);
 		boolean hasSkipLogic=false;
 		boolean isPageBreak=false;
 		for(Connection c:clist)
 		{
 			if(c.getLogicEdge()!=null&&!c.getLogicEdge().unconditoinal())
 			{
 				hasSkipLogic=true;
 				break;
 			}
 			if(c.getLogicEdge()==null||c.getLogicEdge().unconditoinal())
 			{
 				if(c.isPageBreakYes())
 			        	isPageBreak=true;
 				
 			}
 		}
 		
 		if(hasSkipLogic)
 		{
 			actRslt.setAction("noaction");
 			msgs.add(new Message("This question contains skip logic and is a page breaker by default",MessageCriticalLevelEnum.info));
 			return actRslt;
 		}
 		
 		
 		if(isPageBreak)
 		{
 			//remove pagebreak;
 			for(Connection c:clist)
 			{
 				if(c.getLogicEdge()==null||c.getLogicEdge().unconditoinal())
 				{
 					c.setPageBreak("no");
 					cr.save(c);
 				}
 				
 			}
 			actRslt.setAction("removepagebreak");
 		}
 		else
 		{
 			for(Connection c:clist)
 			{
 				if(c.getLogicEdge()==null||c.getLogicEdge().unconditoinal())
 				{
 					c.setPageBreak("yes");
 					cr.save(c);
 				}
 			}
 			actRslt.setAction("markpagebreak");
 		}
 		
 		
 		return actRslt;
 	}
 
 
 
 
 
 
 
 	@PUT
 	@Path("/removepagebreak")
 	@Produces({"application/json","text/xml" })
 	@Consumes({"text/xml", "application/json" })
 	public void removePageBreak(Question q) {
 		// TODO Auto-generated method stub
 		List<Connection> clist = cr.findBySourceId(q.getId());
 		for(Connection c:clist)
 		{
 			if(c.getLogicEdge()==null||c.getLogicEdge().unconditoinal())
 			{
 				c.setPageBreak("no");
 				cr.save(c);
 			}
 		}
 	}
 
 
 
 
 
 
 
 
 
 
 
 	
 
 
 
 	
 	
 
 }
