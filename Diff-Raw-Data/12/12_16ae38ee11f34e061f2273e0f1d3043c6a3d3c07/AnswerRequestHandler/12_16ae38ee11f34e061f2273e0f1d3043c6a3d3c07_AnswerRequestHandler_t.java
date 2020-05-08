 package org.ebag.runtime.handler;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.mina.common.IoSession;
 import org.ebag.net.obj.answer.AnswerObj;
 import org.ebag.net.request.AnswerRequest;
 import org.ebag.net.response.AnswerResponse;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import ebag.dbbase.HibernateSessionFactory;
 import ebag.pojo.Eanswer;
 import ebag.pojo.EanswerDAO;
 import ebag.pojo.Eexam;
 import ebag.pojo.EexamDAO;
 import ebag.pojo.Eproblem;
 import ebag.pojo.EproblemDAO;
 import ebag.pojo.Euser;
 import ebag.pojo.EuserDAO;
 
 public class AnswerRequestHandler extends BasicHandler<AnswerRequest> {
 
 	public AnswerRequestHandler(IoSession session, Object message) {
 		super(session, message);
 	}
 
 	public void handle(int i) {
 		AnswerResponse res = new AnswerResponse();
 		ArrayList<AnswerObj> lst = new ArrayList<AnswerObj>();
 		res.setExamList(lst);
 		Session session = HibernateSessionFactory.getSession();
 		Transaction trans = session.beginTransaction();
 		Eexam exam = new EexamDAO().findById(message.examId);
 		Iterator<Eproblem> it = exam.getEproblems().iterator();
 		EanswerDAO ansDao = new EanswerDAO();
 		Euser user = new EuserDAO().findById(message.uid);
 		while (it.hasNext()) {
 			Eanswer example = new Eanswer();
 			example.setEuser(user);
 			example.setProblemId(it.next().getId());
 			List<Eanswer> list = ansDao.findByExample(example);
 			try {
 				lst.add(toObj(list.get(0)));
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		this.session.write(res);
 	}
 
 	public void handle() {
		System.out.println(message.examId+","+message.uid);
 		AnswerResponse res = new AnswerResponse();
 		ArrayList<AnswerObj> lst = new ArrayList<AnswerObj>();
 		res.setExamList(lst);
 		Session session = HibernateSessionFactory.getSession();
 		Transaction trans = session.beginTransaction();
 		Eexam exam = new EexamDAO().findById(message.examId);
 		Iterator<Eproblem> it = exam.getEproblems().iterator();
 		String hql="from  Eanswer ea where ea.euser="+message.uid+" and ea.problemId in (";
 		while(it.hasNext()){
 			hql+=it.next().getId()+",";
 		}
 		if(hql.endsWith(",")){
 			hql=hql.substring(0,hql.length()-1);
 		}
 		hql+=")";
		System.out.println(hql);
 		List<Eanswer> answers=session.createQuery(hql).list();
 		for(Eanswer ans:answers){
 			lst.add(toObj(ans));
 		}
 		this.session.write(res);
 	}
 	AnswerObj toObj(Eanswer ans) {
 		AnswerObj res = new AnswerObj();
 		res.setId(ans.getId());
 		res.setPicAnswerUrl(ans.getPicanswer());
 		res.setPicOfTeacherUrl(ans.getPicofteacher());
 		res.setPoint(Integer.parseInt(ans.getPoint()==null?"0":ans.getPoint()));
 		res.setProblemId(ans.getProblemId());
 		res.setState(ans.getState());
 		res.setTextAnswer(ans.getTextanswer());
 		res.setTextOfTeacher(ans.getTextofteacher());
 		res.setUid(ans.getEuser().getId());
 		try{
 		res.setScore(Double.parseDouble((new EproblemDAO().findById(ans.getProblemId()).getPoint())));
 		}catch (Exception e){
 			res.setScore(1);
 		}return res;
 	}
	
	public static void main(String[] args){
		AnswerRequest request=new AnswerRequest();
		request.examId=5;
		request.uid=2;
		new AnswerRequestHandler(null, request).handle();
		
	}
 }
