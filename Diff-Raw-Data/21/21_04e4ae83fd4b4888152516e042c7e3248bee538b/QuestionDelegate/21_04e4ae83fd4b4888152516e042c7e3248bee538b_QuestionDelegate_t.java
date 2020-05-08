 package delegates;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 import pojos.QuestionBean;
 import utils.PlayDozerMapper;
 
 public class QuestionDelegate {
 
 	static final Map<Integer, String> questionChapterDecades  = new HashMap<Integer, String>();
 	
 	static {
 		questionChapterDecades.put(0, "CHILDHOOD");
 		questionChapterDecades.put(1, "CHILDHOOD");
 		questionChapterDecades.put(2, "CHILDHOOD"); // ALSO YOUTH
 		questionChapterDecades.put(3, "YOUTH");
 		questionChapterDecades.put(4, "ADULTHOOD");
 		questionChapterDecades.put(5, "ADULTHOOD");
 		questionChapterDecades.put(6, "ADULTHOOD");
 		questionChapterDecades.put(7, "OLDAGE");
 		questionChapterDecades.put(8, "OLDAGE");
 		questionChapterDecades.put(9, "OLDAGE");
 	}
 	
 	public static QuestionDelegate getInstance() {
 		return new QuestionDelegate();
 	}
 
 	public List<QuestionBean> getAll() {
 		List<models.Question> modelQuestions = models.Question.all();
 		List<QuestionBean> pojosQuestions = new ArrayList<QuestionBean>();
 		for (models.Question question : modelQuestions) {
 			QuestionBean questionBean = PlayDozerMapper.getInstance().map(
 					question, QuestionBean.class);
 			pojosQuestions.add(questionBean);
 		}
 		return pojosQuestions;
 	}
 
 	public QuestionBean getQuestion(Long id) {
 		models.Question question = models.Question.read(id);
 		if (question != null) {
 			QuestionBean questionBean = PlayDozerMapper.getInstance().map(
 					question, QuestionBean.class);
 			return questionBean;
 		} else {
 			return null;
 		}
 	}
 
 	public void create(QuestionBean questionBean) {
 		models.Question question = PlayDozerMapper.getInstance().map(
 				questionBean, models.Question.class);
 		models.Question.create(question);
 		question = models.Question.read(question.getQuestionId());
 		PlayDozerMapper.getInstance().map(question, questionBean);
 	}
 
 	public void update(QuestionBean bean, Long id) {
 		models.Question question = PlayDozerMapper.getInstance().map(bean,
 				models.Question.class);
 		question.update(id);
 		question = models.Question.read(question.getQuestionId());
 		PlayDozerMapper.getInstance().map(question, bean);
 	}
 
 	public void deleteQuestion(Long id) {
 		models.Question.delete(id);
 	}
 
 	public static List<QuestionBean> getQuestionsForLifeChapter(String chapter) {
 		List<models.Question> modelQuestions = models.Question.readByChapter(chapter);
 		List<QuestionBean> pojosQuestions = new ArrayList<QuestionBean>();
 		for (models.Question question : modelQuestions) {
 			QuestionBean questionBean = PlayDozerMapper.getInstance().map(
 					question, QuestionBean.class);
 			pojosQuestions.add(questionBean);
 		}
 		
 		// Add some random questions for alltimes 
 		addAllTimesQuestions(pojosQuestions);
 		return pojosQuestions;
 	}
 	
 	public static void addAllTimesQuestions(List<QuestionBean> pojosQuestions) {
 		List<models.Question> modelQuestions = models.Question.readByChapter("ALLTIMES");
 		List<QuestionBean> allTimeQuestions = new ArrayList<QuestionBean>();
 		for (models.Question question : modelQuestions) {
 			QuestionBean questionBean = PlayDozerMapper.getInstance().map(
 					question, QuestionBean.class);
 			allTimeQuestions.add(questionBean);
 		}
 		pojosQuestions.addAll(allTimeQuestions);
 	}
 
 	public static List<QuestionBean> getQuestionsForLifeDecade(Integer birthYear, Integer focusYear) {
 		Integer focusDecade = ((focusYear - birthYear) % 100)/10;
		
		if (focusDecade >= 0) {
			String chapter = questionChapterDecades.get(focusDecade);
			return getQuestionsForLifeChapter(chapter);
		} else {
			// questions to send if the focus decade is set to before the birth of the person
			return getQuestionsForLifeChapter("ALLTIMES");
		}
 	}
 }
