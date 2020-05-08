 package exceedvote.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import exceedvote.dao.ConcreteDaoFactory;
 import exceedvote.dao.ConcreteQuestionDAO;
 import exceedvote.dao.ConcreteScoreDAO;
 import exceedvote.dao.ConcreteTeamDAO;
 import exceedvote.dao.ConcreteUserDAO;
 import exceedvote.domain.BothType;
 import exceedvote.domain.Question;
 import exceedvote.domain.Score;
 import exceedvote.domain.TeamBoard;
 import exceedvote.domain.User;
 
 public class CommitteeController {
 	private ConcreteQuestionDAO questionDAO;	//add edit delete
 	private ConcreteUserDAO userDAO;	//add edit delete
 	private ConcreteScoreDAO scoreDAO;	//add edit delete
 	private ConcreteTeamDAO teamDAO;	//add edit delete
 	private List<Question> questions;
 	private List<TeamBoard> teamBoards;
 	private List<User> users;
 		
 	public CommitteeController(){
 		questionDAO = (ConcreteQuestionDAO) ConcreteDaoFactory.getInstance().createDAO("questionDAO");
 		userDAO = (ConcreteUserDAO) ConcreteDaoFactory.getInstance().createDAO("userDAO");
 		scoreDAO = (ConcreteScoreDAO) ConcreteDaoFactory.getInstance().createDAO("scoreDAO");
 		teamDAO = (ConcreteTeamDAO) ConcreteDaoFactory.getInstance().createDAO("teamDAO");
 		questions = questionDAO.find();
 		teamBoards = teamDAO.find();
 		users = userDAO.findAll();
 	}
 	
 	public List<String> getTeamName(){
 		  return teamDAO.getTeamName();
 	}
 	
 	public List<String> getQuestionInstructor(){
 		List<String> instruction = new ArrayList<String>();
 		for(Question q : questions)
 			instruction.add(q.getInstruction());
 		return instruction;
 	}
 	
 	public List<String> getQuestionType(){
 		List<String> types = new ArrayList<String>();
 		for(Question q : questions)
 			types.add(q.getType());
 		return types;
 	}
 	
 	public void addQuestion(String instruction, String type){
 		questionDAO.save(new BothType(instruction,type));
 		questions = questionDAO.find();
 	}
 	
 	public void editQuestion(int questionIndex, String instruction, String type){
 		questions.get(questionIndex).setQuestion(instruction);
 		questions.get(questionIndex).setType(type);
 		questionDAO.update(questions.get(questionIndex));
 		questions = questionDAO.find();
 	}	
 	
 	public void deleteQuestion(int questionIndex){
 		questionDAO.delete(questions.get(questionIndex));
 		questions = questionDAO.find();
 	}
 	
 	public void addTeam(String name, String description){
 		TeamBoard team = new TeamBoard(name,description);
 		teamDAO.save(team);
 		teamBoards = teamDAO.find();
 	}
 	
 	public void editTeam(int teamIndex,String name,String description){
 		teamBoards.get(teamIndex).setName(name);
 		teamBoards.get(teamIndex).setDescription(description);
 		teamDAO.update(teamBoards.get(teamIndex));
 		teamBoards = teamDAO.find();
 	}
 	
 	public void deleteTeam(int teamIndex){
 		teamDAO.delete(teamBoards.get(teamIndex));
 		teamBoards = teamDAO.find();
 	}
 	
 	public void addUser(String id,String password,String type){
 		User user = new User(id,password,type);
 		userDAO.save(user);		
 		users = userDAO.findAll();
 	}
 	
 	public void deleteUser(String userId){
 		int i;
 		for(i=0;i<users.size();i++)
 			if(users.get(i).getUserId().equals(userId))
 				break;				
 		userDAO.delete(users.get(i));
 		users = userDAO.findAll();
 	}
 	
 	public void addScoreTable(){
 		int n = questionDAO.count();
 		StringBuffer point = new StringBuffer("");
 		for(int i=0;i<teamDAO.count();i++)
 			point.append("0,");
 		String tmp = point.substring(0,point.length()-1);
 //		System.out.println("n = "+n);
 		for(int i=0;i<users.size();i++){
 			for(int j=1;j<=n;j++){
 //				System.out.println(users.get(i).getId()+ " "+ j);
 				scoreDAO.save(new Score(users.get(i).getId(),j,tmp));
 			}
 		}
 	}
 	
 	public List<String> getTeamDescription(){
 		List<String> descriptions = new ArrayList<String>();
 		for(TeamBoard t : teamBoards)
 			descriptions.add(t.getDescription());
 		return descriptions;
 	}
 }
