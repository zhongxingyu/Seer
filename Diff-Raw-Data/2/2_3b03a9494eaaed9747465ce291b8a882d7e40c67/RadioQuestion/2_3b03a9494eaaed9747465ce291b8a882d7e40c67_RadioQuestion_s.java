 package quizsite.models.questions;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import quizsite.models.Question;
 import quizsite.util.Activity;
 
 public class RadioQuestion extends Question {
 
 	private List<String> options;
 	
 	public RadioQuestion(Set<String> answers, String text, int quiz_id, List<String> opts) throws SQLException {
 		super(text, answers, quiz_id);
		setType(Type.CHECKBOX);
 		setOptions(opts);
 	}
 	
 	public RadioQuestion() throws SQLException { super(); }
 	
 	public void setOptions(List<String> newOPt)
 	{ options = newOPt; }
 	
 	public List<String> getOptions()
 	{ return options; }
 
 	@Override
 	protected String getAuxiliary() { 
 		return serialize(getOptions()); 
 	}
 	
 	@Override
 	public void parse(List<String> dbEntry) throws IllegalArgumentException, SQLException {
 		super.parse(dbEntry);
 		
 		String opt = dbEntry.get(I_AUXILIARY);
 		setOptions(unserialize(opt));
 	}
 	
 	private List<String> unserialize(String opt) {
 		List<String> opts = new ArrayList<String>(Arrays.asList(opt.trim().split("<>!<>")));
 		return opts;
 	}
 
 	private String serialize(List<String> opt)
 	{
 		String ser = "";
 	
 		for (int i = 0; i < opt.size(); i++) {
 			ser += opt.get(i) + "<>!<>";
 		}
 		
 		return ser.substring(0, ser.length()-5);
 	}
 
 	@Override
 	public Activity getActivity() {
 		//THIS DOES NOT NEED TO BE IMPLEMENTED
 		return null;
 	}
 }
