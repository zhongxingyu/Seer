 package data.bonus;
 
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import data.JSONUtils;
 
 import json.simple.JSONArray;
 import json.simple.JSONObject;
 import json.simple.JSONValue;
 import json.simple.parser.ParseException;
 
 /**
  * BonusQuestion is the class that will deal with the numerous bonus questions
  * that users can answer at their leisure during the competition.
  * 
  * @author Graem Littleton, Kevin Brightwell, Jonathan Demelo, Ramesh Raj,
  *         Justin McDonald
  * 
  */
 
 public class BonusQuestion {
 	public static enum BONUS_TYPE {
 		MULTI, SHORT;
 	};
 
 	protected BONUS_TYPE bonusType;
 
 	protected String prompt;
 	protected String answer;
 	protected String[] choices; // TODO: can we somehow implement short answer
 								// and mc together without this?
 	protected int week;
 	protected int number;
 
 	protected static final String KEY_TYPE = "type";
 	protected static final String KEY_PROMPT = "prompt";
 	protected static final String KEY_ANSWER = "answer";
 	protected static final String KEY_CHOICES = "mc_choices";// TODO:need a
 																// better name
 																// for type
 	protected static final String KEY_WEEK = "week";
 	protected static final String KEY_NUMBER = "number";
 
 	/**
 	 * Default constructor for Bonus Question
 	 * 
 	 * @param prompt
 	 *            The question(required)
 	 * @param answer
 	 *            The answer(required)
 	 * @param choices
 	 *            The possible choices(null from short answer, and actual values
 	 *            for MC)
 	 */
 	public BonusQuestion(String prompt, String answer, String[] choices, int week, int number) {
 		this.prompt = prompt;
 		this.answer = answer;
 		this.choices = choices;
 		bonusType = choices == null ? BONUS_TYPE.SHORT : BONUS_TYPE.MULTI;
 		this.week = week;
 		this.number = number;
 		Bonus.addNewQuestion(this);
 		// TODO: do we need to check if answer is in choices?
 	}
 
 	/**
 	 * Only used for fromJsonObject
 	 */
 	public BonusQuestion() {
 	}
 	
 	public BonusQuestion(int week,int number){
 		this.week=week;
 		this.number=number;
 	}
 
 	/**
 	 * Get the type of question.
 	 * 
 	 * @return
 	 */
 	public BONUS_TYPE getBonusType() {
 		return bonusType;
 	}
 
 	/**
 	 * Set the bonus type
 	 * 
 	 * @param bonusType
 	 */
 	public void setBonusType(BONUS_TYPE bonusType) {
 		this.bonusType = bonusType;
 	}
 
 	/**
 	 * Get the question prompt
 	 * 
 	 * @return String of prompt
 	 */
 	public String getPrompt() {
 		return prompt;
 	}
 
 	/**
 	 * Set the question prompt
 	 * 
 	 * @param prompt
 	 */
 	public void setPrompt(String prompt) {
 		this.prompt = prompt;
 	}
 
 	/**
 	 * Get the answer to the question
 	 * 
 	 * @return
 	 */
 	public String getAnswer() {
 		return answer;
 	}
 
 	/**
 	 * Set answer to question
 	 * 
 	 * @param answer
 	 */
 	public void setAnswer(String answer) {
 		this.answer = answer;
 	}
 
 	/**
 	 * Get possible choices or null if question is short answre
 	 * 
 	 * @return
 	 */
 	public String[] getChoices() {
 		return choices;
 	}
 
 	/**
 	 * Set choices. CHANGES TYPE TO MULTIPLE CHOICE!
 	 * 
 	 * @param choices
 	 */
 	public void setChoices(String[] choices) {
 		this.choices = choices;
 		if(choices!=null)
 			bonusType = BONUS_TYPE.MULTI;
 	}
 
 	/**
 	 * Get the week this question was asked
 	 * 
 	 * @return int
 	 */
 	public int getWeek() {
 		return week;
 	}
 
 	/**
 	 * Set the week this question was asked
 	 * 
 	 * @param week
 	 */
 	public void setWeek(int week) {
 		this.week = week;
 	}
 	
 	/**
 	 * Get the question number in a particular week
 	 * 
 	 * @return int
 	 */
 	public int getNumber() {
 		return number;
 	}
 
 	/**
 	 * Set the question number within a particular week
 	 * 
 	 * @param number
 	 */
 	public void setNumber(int number) {
 		this.number = number;
 	}
 	
 	/**
 	 * Converts Contestant object to a json object
 	 * 
 	 * @return a JSON object containing all the data needed
 	 * @throws JSONException
 	 */
 	public JSONObject toJSONObject() throws ParseException {
 		JSONObject obj = new JSONObject();
 		obj.put(KEY_PROMPT, prompt);
 		obj.put(KEY_ANSWER, answer);
 
		if (choices != null) {
			JSONArray sChoice = new JSONArray();
 			for (String c : choices) {
 				if (c != null)
 					sChoice.add(c);
 			}
			obj.put(KEY_CHOICES, sChoice);
		} else {
			obj.put(KEY_CHOICES, null);
 		}
 
 		obj.put(KEY_WEEK, week);
 		obj.put(KEY_NUMBER, number);
 		return obj;
 	}
 
 	public void fromJSONObject(JSONObject o) throws ParseException {
 
 		setPrompt((String)o.get(KEY_PROMPT));
 		setAnswer((String)o.get(KEY_ANSWER));
 
 		JSONArray jChoices = (JSONArray)o.get(KEY_CHOICES);
 		if (jChoices == null) {
 			setChoices(null);
 		} else {
 			String[] choice = new String[jChoices.size()];
 			for (int i = 0; i < jChoices.size(); i++) {
 				choice[i] = (String)jChoices.get(i);
 			}
 			setChoices(choice);
 		}
 		setWeek(((Number)o.get(KEY_WEEK)).intValue());
 		setNumber(((Number)o.get(KEY_NUMBER)).intValue());
 	}
 	
 	/** 
 	 * Drive for BonusQuestion
 	 * @param args
 	 * @throws ParseException
 	 * @throws FileNotFoundException 
 	 */
 	public static void main(String[] args) throws ParseException, FileNotFoundException {
 		/*	************to json test*********************/
 		int weekNum = 3;
 		String[] choices = {"a","b","c","d"};
 		for(int i =0;i<=weekNum;i++){
 			for(int j=0;j<10;j++){
 				String[] s = null;
 				String answer = "answer";
 				if(j%2==0){
 					s = choices;
 					answer = "b";
 				}
 				BonusQuestion b = new BonusQuestion("question "+Integer.toString(i)+
 						Integer.toString(j),answer,s,i,j);
 			}	
 		}
 		List<BonusQuestion> l = Bonus.getAllQuestions();
 		for(int i =0;i<l.size();i++){
 			System.out.println(l.get(i).getPrompt());
 		}
 		
 		for(int i =weekNum;i>=0;i--){
 			for(int j=9;j>=0;j--){
 				BonusQuestion b = Bonus.getQuestion(i, j);
 				System.out.println("i: "+b.getWeek()+" "+Integer.toString(i)+" j:"+
 						b.getNumber()+" "+Integer.toString(j));
 			}	
 		}
 		
 		JSONObject json = Bonus.toJSONObject();
 		System.out.println(json.toJSONString());
 		
 		Bonus.fromJSONObject(json);
 		json = Bonus.toJSONObject();
 		System.out.println(json.toJSONString());
 		JSONUtils.writeJSON(JSONUtils.pathBonus, json);
 	}
 }
