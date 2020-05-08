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
 
 	public static final String FILE_PATH = "res/data/bonus.dat";
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
 		BonusQuestion b = new BonusQuestion("question week 3", "answer", null, 3, 1);
 		String shortActive = b.toJSONObject().toString();
 		System.out.println(shortActive);
 
 		b = new BonusQuestion("question week 2", "answer", null, 2, 1);
 
 		b = new BonusQuestion("question week 2", "answer", null, 2, 1);
 		String shortNotActive = b.toJSONObject().toString();
 		System.out.println(shortNotActive);
 
 		String[] choices = { "one", "two", "three", "answer" };
 
 		b = new BonusQuestion("question week 1", "answer", choices, 1, 1);
 		b = new BonusQuestion("question week 4", "answer", choices, 4, 2);
 		String mcActive = b.toJSONObject().toString();
 		System.out.println(mcActive);
 
 		b = new BonusQuestion("question week 1", "answer", choices, 1, 2);
 		String mcNotActive = b.toJSONObject().toString();
 		System.out.println(mcNotActive);
 		
 		System.out.println(Bonus.toJSONObject().toString());
 		
 		/* **from json test**********************/
 		System.out.println("\n\n\nGenerating fromJSONObject");
 		JSONObject o = (JSONObject)JSONValue.parse(shortActive);
 		BonusQuestion bq = new BonusQuestion();
 		bq.fromJSONObject(o);
 		System.out.println(bq.toJSONObject().toString());
 
 		o = (JSONObject)JSONValue.parse(shortNotActive);
 		bq = new BonusQuestion();
 		bq.fromJSONObject(o);
 		System.out.println(bq.toJSONObject().toString());
 
 		o = (JSONObject)JSONValue.parse(mcActive);
 		bq = new BonusQuestion();
 		bq.fromJSONObject(o);
 		System.out.println(bq.toJSONObject().toString());
 
 		o = (JSONObject)JSONValue.parse(mcNotActive);
 		bq = new BonusQuestion();
 		bq.fromJSONObject(o);
 		System.out.println(bq.toJSONObject().toString());
 		System.out.println(Bonus.toJSONObject().toString());
 		
 		/* ****************TESTING SORT*************************/
 		System.out.println("\n\nTesting sort**************************");
 		List<BonusQuestion> list = Bonus.getAllQuestions();
 		for(BonusQuestion quest: list){
 			System.out.println(quest.getWeek());
 		}
 		
 		BonusQuestion byWeek = Bonus.getQuestionByWeek(2);
 		System.out.println(byWeek.getPrompt());
 		System.out.println(byWeek.getBonusType());
 		System.out.println(Bonus.toJSONObject().toJSONString());
 		JSONUtils.writeJSON(Bonus.filePath, Bonus.toJSONObject());
		//JSONObject fileO = JSONUtils.readFile(Bonus.filePath+".as");
		//System.out.println(fileO.toJSONString());
		//Bonus.fromJSONObject(fileO);
 		
 	}
 }
