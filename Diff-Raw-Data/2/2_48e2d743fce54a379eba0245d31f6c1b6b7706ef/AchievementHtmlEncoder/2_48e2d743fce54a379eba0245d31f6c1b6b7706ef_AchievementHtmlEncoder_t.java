 package webEncoder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import dao.AchievementDAO;
 import dao.DAOFactory;
 import entity.Achievement;
 import entity.User;
 
 public class AchievementHtmlEncoder {
 
 	public String writeHtmlUserAchievements(User givenUser){
 		String code="";
 		DAOFactory daoFactory=DAOFactory.getInstance(DAOFactory.MYSQL);
 		AchievementDAO medalDao=daoFactory.createAchievementDAO();
 		ArrayList<Achievement> medals=(ArrayList<Achievement>)medalDao.getUserAchievements(givenUser.getAccountID());
 		
 		if(medals==null||medals.isEmpty()||givenUser.getRole()!=User.Roles.learner.getValue()){
 			code=code.concat("<tr><th> No Badges</th></tr>");
 			return code.concat(getHTML_AchievementTable(medalDao.getUnAttainedAchievements(givenUser.getAccountID()),"To Earn")+
 					"</table>");
 		}
 		
 		return code.concat(getHTML_AchievementTable(medals,"Earned Achievements")+"<hr/>"+
 				getHTML_AchievementTable(medalDao.getUnAttainedAchievements(givenUser.getAccountID()),"Achievements To Earn"));
 	}/**/
 	
 	public String getHTML_AchievementTable(List<Achievement> medals){
 		String code="<table>";
 		for(int ctr=0;ctr<medals.size();ctr++){
 			code=code.concat("<tr><th>"+getImageHTML(medals.get(ctr).getPicUrl(),50,50)+
 					medals.get(ctr).getTitle()+"</th>");
 			code=code.concat("<td>"+medals.get(ctr).getDescription()+"</td></tr>");
 		}
 		
 		return code.concat("</table>");
 	}
 	
 	public String getHTML_AchievementTable(List<Achievement> medals,String caption){
		String code="<table id='tableBorderfeed2' bgcolor='white' align='center' width='60%'><caption>"+caption+"</caption>";
 		for(int ctr=0;ctr<medals.size();ctr++){
 			code=code.concat("<tr align='left'><th>"+getImageHTML(medals.get(ctr).getPicUrl(),50,50)+"<br/>"+
 					medals.get(ctr).getTitle()+"</th>");
 			code=code.concat("<td>"+medals.get(ctr).getDescription()+"</td></tr>");
 		}
 		
 		return code.concat("</table>");
 	}
 	private String getImageHTML(String url,int width,int height){
 		return "<img src='"+url+"' width='"+width+" height='"+height+"/>";
 	}
 }
