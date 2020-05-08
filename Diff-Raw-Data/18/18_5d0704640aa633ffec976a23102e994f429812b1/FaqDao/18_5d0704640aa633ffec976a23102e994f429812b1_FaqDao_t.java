 package com.zhaoyang.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.zhaoyang.data.Faq;
 import com.zhaoyang.data.Student;
 import com.zhaoyang.data.Teacher;
 import com.zhaoyang.orm.Rule;
 
 public class FaqDao {
 	private RuleDao ruleDao;
 	
 	public RuleDao getRuleDao() {
 		return ruleDao;
 	}
 
 	public void setRuleDao(RuleDao ruleDao) {
 		this.ruleDao = ruleDao;
 	}
 
 	public List<Faq> findAll() throws Exception{
 		Rule rule =ruleDao.findRuleByRuleId("FaqList");
 		JSONArray array=new JSONArray(rule.getRuleDef());
 		List<Faq> students=new ArrayList<Faq>();
 		for(int i=0;i<array.length();i++){
 			JSONObject jobj=(JSONObject)array.get(i);
 			Faq student=new Faq();
			
 			student.setQueNo(jobj.getLong("queNo"));
 			student.setQuestion(jobj.getString("question"));
 			student.setAnswer(jobj.getString("answer"));
 			students.add(student);
 		}
 		return students;
 	}
 	public void save(Faq studentTmp) throws Exception{
 		List<Faq> students=findAll();
 		Long newid;
 		if(students.size()>0){
 			newid=students.get(students.size()-1).getQueNo()+1;
 		}else{
 			newid=1l;
 		}
 		studentTmp.setQueNo(newid);
 		students.add(studentTmp);
 		net.sf.json.JSONArray jsonArray=net.sf.json.JSONArray.fromObject(students);
 		
 		String AAA=jsonArray.toString();
 		ruleDao.update("FaqList", AAA);
 	}
 	public boolean delete(Long id)throws Exception{
 		List<Faq> students=findAll();
 		for (Faq student : students) {
 			if(student.getQueNo().equals(id)){
 				students.remove(student);
 				net.sf.json.JSONArray jsonArray=net.sf.json.JSONArray.fromObject(students);
 				String AAA=jsonArray.toString();
 				ruleDao.update("FaqList", AAA);
 				return true;
 			}
 		}
 		return false;
 	}
 	public boolean update(Faq studentTmp)throws Exception{
 		List<Faq> students=findAll();
 		for (int i = 0; i < students.size(); i++) {
 			if(students.get(i).getQueNo().equals(studentTmp.getQueNo())){
 				if(studentTmp.getQuestion()!=null){
 					students.get(i).setQuestion(studentTmp.getQuestion());
 				}
 				if(studentTmp.getAnswer()!=null){
 					students.get(i).setAnswer(studentTmp.getAnswer());
 				}
 				net.sf.json.JSONArray jsonArray=net.sf.json.JSONArray.fromObject(students);
 				String AAA=jsonArray.toString();
 				ruleDao.update("FaqList", AAA);
 				return true;
 			}
 		}
 		return false;
 		
 	}
 
 	public Faq findById(Long id) throws Exception{
 		List<Faq> students=findAll();
 		for (Faq student : students) {
 			if(student.getQueNo().equals(id)){
 				return student;
 			}
 		}
 		return null;
 	}
 
 }
