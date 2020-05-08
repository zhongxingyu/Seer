 package org.ebag.web;
 
 import org.ebag.net.obj.I;
 
 import ebag.pojo.Eproblem;
 import ebag.pojo.EproblemDAO;
 
 public class ProblemHelper {
 
 	public static String getHtml(String pid,String type){
 		String res="";
 		try{
 		EproblemDAO epdao=new EproblemDAO();
 		Eproblem p=epdao.findById(Integer.parseInt(pid.trim()));
 		if(I.url.analysis.equals(type)){
 			res=p.getAnalysis();
 		}else if(I.url.ans.equals(type)){
 			res=p.getAns();
 		}else if(I.url.aspect.equals(type)){
 			res=p.getAspect();
 		}else if(I.url.difficulty.equals(type)){
 			res=p.getDifficulty();
 		}else if(I.url.hint.equals(type)){
 			res=p.getHint();
 		}else if(I.url.problem.equals(type)){
 			res=p.getProblem();
 		}else if(I.url.request.equals(type)){
 			res=p.getRequests();
 		}}catch (Exception e) {
 			return "";
 		}
		//System.out.println(res);
 		return res;
 	}
 }
