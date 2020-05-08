 package m2glre.marsupilami.moodlexmlapi.presenter;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import m2glre.marsupilami.moodlexmlapi.core.InvalidQuizFormatException;
 import m2glre.marsupilami.moodlexmlapi.core.InvalidStreamSizeException;
 import m2glre.marsupilami.moodlexmlapi.core.QuizImportExportService;
 import m2glre.marsupilami.moodlexmlapi.core.data.CategoryQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.GenericQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.IImportedQuiz;
 import m2glre.marsupilami.moodlexmlapi.core.data.IQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.IQuiz;
 import m2glre.marsupilami.moodlexmlapi.core.data.QuestionError;
 import m2glre.marsupilami.moodlexmlapi.core.data.QuestionText;
 import m2glre.marsupilami.moodlexmlapi.core.data.QuestionTextFormat;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.Answer;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.AnswerCalculated;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.AnswerNumbering;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.AnswerNumerical;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.CalculatedQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.CalculatedQuestionDataSetDefinition;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.CalculatedQuestionDataSetItem;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.ClozeQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.DescriptionQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.EssayQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.MatchingQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.MultipleChoiceQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.NumericalQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.QuestionImpl;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.Quiz;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.ShortAnswerQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.Subquestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.TrueFalseQuestion;
 import m2glre.marsupilami.moodlexmlapi.core.data.impl.Unit;
 import m2glre.marsupilami.moodlexmlapi.impl.ImportedQuizImpl;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class QuizImportExportServiceImpl implements QuizImportExportService {
 
 	private static String QUESTION = "question";
 	private static String SUB_QUESTION = "subquestion";
 	private static String TYPE = "type";
 	private static String DESCRIPTION = "description";
 	private static String ESSAY = "essay";
 	private static String CALCULATED = "calculated";
 	private static String MATCHING = "matching";
 	private static String CLOZE = "cloze";
 	private static String MULTI_CHOICE = "multichoice";
 	private static String NUMERICAL = "numerical";
 	private static String SHORT_ANSWER = "shortanswer";
 	private static String TRUE_FALSE = "truefalse";
 	private static String CATEGORY = "category";
 	private static String NAME = "name";
 	private static String QUESTION_TEXT = "questiontext";
 	private static String FORMAT = "format";
 	private static String IMAGE = "image";
 	private static String IMAGE_URL = "image";
 	private static String IMAGE_BASE_64 = "image_base64";
 	private static String GENERAL_FEEDBACK = "generalfeedback";
 	private static String CORRECT_FEEDBACK = "correctfeedback";
 	private static String INCORRECT_FEEDBACK = "incorrectfeedback";
 	private static String PARTIALLY_CORRECT_FEEDBACK = "partiallycorrectfeedback";
 	private static String DEFAULT_GRAD = "defaultgrade";
 	private static String PENALTY = "penalty";
 	private static String HIDDEN = "hidden";
 	private static String SHUFFLE_ANSWERS = "shuffleanswers";
 	private static String ANSWER_NUMBERING = "answernumbering";
 	private static String ANSWER = "answer";
 	private static String FEEDBACK = "feedback";
 	private static String FRACTION = "fraction";
 	private static String TOLERANCE = "tolerance";
 	private static String TOLERANCE_TYPE = "tolerancetype";
 	private static String CORRECT_ANSWER_FORMAT = "correctanswerformat";
 	private static String CORRECT_ANSWER_LENGTH = "correctanswerlength";
 	private static String UNITS = "units";
 	private static String UNIT = "unit";
 	private static String UNIT_MULTIPLIER = "multiplier";
 	private static String UNIT_NAME = "unit_name";
 	private static String DATASET_DEFINITIONS = "dataset_definitions";
 	private static String DATASET_DEFINITION = "dataset_definition";
 	private static String STATUS = "status";
 	private static String DISTRIBUTION = "distribution";
 	private static String MINIMUM = "minimum";
 	private static String MAXIMUM = "maximum";
 	private static String DECIMALS = "decimals";
 	private static String ITEM_COUNT = "itemcount";
 	private static String NUMBER_OF_ITEM = "number_of_items";
 	private static String DATASET_ITEMS = "dataset_items";
 	private static String DATASET_ITEM = "dataset_item";
 	private static String NUMBER = "number";
 	private static String VALUE = "value";
 	private static String USECASE = "usecase";
 	
 
 	public QuizImportExportServiceImpl() {
 		super();
 	}
 
 	public IImportedQuiz importQuiz(InputStream is)
 			throws InvalidQuizFormatException, InvalidStreamSizeException {
 
 		ImportedQuizImpl importedQuiz = new ImportedQuizImpl();
 
 		importedQuiz.setQuestionList(new ArrayList<QuestionImpl>());
 		importedQuiz.setExtractedQuestionList(new ArrayList<IQuestion>());
 		importedQuiz.setNonExtractedQuestionList(new ArrayList<IQuestion>());
 		importedQuiz.setProcessedQuestionList(new ArrayList<IQuestion>());
 
 		CategoryQuestion categoryQuestion;
 		GenericQuestion genericQuestion;
 		DescriptionQuestion descriptionQuestion;
 		ClozeQuestion clozeQuestion;
 		EssayQuestion essayQuestion;
 		MatchingQuestion matchingQuestion;
 		MultipleChoiceQuestion multipleChoiceQuestion;
 		NumericalQuestion numericalQuestion;
 		ShortAnswerQuestion shortAnswerQuestion;
 		TrueFalseQuestion trueFalseQuestion;
 		CalculatedQuestion calculatedQuestion;
 
 		QuestionText questionText = null;
 		QuestionTextFormat DEFAULT_QUESTION_TEXT_FORMAT = QuestionTextFormat.plain_text;
 		Answer answer;
 		AnswerNumerical answerNumerical;
 		AnswerCalculated answerCalculated;
 		Unit unit;
 		CalculatedQuestionDataSetDefinition calculatedQuestionDataSetDefinition;
 		CalculatedQuestionDataSetItem calculatedQuestionDataSetItem;
 		Subquestion subquestion;
 		
 
 		try {
 			DocumentBuilderFactory builderFactory =
 			        DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = builderFactory.newDocumentBuilder();
 			Document document = builder.parse(is);
 			//parser.parse(is);
 		//	Document document = parser.getDocument();
 			Element quiz = document.getDocumentElement();
 			NodeList questionsList = quiz.getElementsByTagName(QUESTION);
 			NodeList questionChildNodesList;
 			NodeList answerChildNodesList;
 			NodeList subquestionChildNodesList;
 			NodeList unitsChildNodesList;
 			NodeList unitChildNodesList;
 			NodeList dataSetDefinitionsChildNodesList;
 			NodeList dataSetDefinitionChildNodesList;
 			NodeList dataSetItemsChildNodesList;
 			NodeList dataSetItemChildNodesList;
 			String questionName = null;
 			String questionType = null;
 			String generalFeedBack = null;
 			float penalty = 0;
 			float defaultGrade = 0 ;
 			boolean isHidden = false;
 			String imageUrl = null;
 			String imageBase64 = null;
 			List<QuestionError> errors;
 			QuestionError questionError;
 			
 			for (int i = 0; i < questionsList.getLength(); i++) {
 				genericQuestion = new GenericQuestion();
 				questionChildNodesList = questionsList.item(i).getChildNodes();
 				errors = new ArrayList<QuestionError>();
 				if (questionsList.item(i).getAttributes().getNamedItem(TYPE) != null) {
 					questionType = questionsList.item(i).getAttributes()
 							.getNamedItem(TYPE).getNodeValue();
 					if (questionType.equalsIgnoreCase(CATEGORY)) {
 						categoryQuestion = new CategoryQuestion();
 						categoryQuestion.setCategory(CATEGORY);
 						for (int j = 0; j < questionChildNodesList.getLength(); j++) {
 							if (questionChildNodesList.item(j).getNodeName()
 									.equalsIgnoreCase(CATEGORY)) {
 								categoryQuestion
 										.setCourse(questionChildNodesList.item(
 												j).getTextContent());
 							}
 						}
 						importedQuiz.getQuestionList().add(categoryQuestion);
 					} else {
 						for (int j = 0; j < questionChildNodesList.getLength(); j++) {
 							if (questionChildNodesList.item(j).getNodeName()
 									.equalsIgnoreCase(NAME)) {
 								questionName = questionChildNodesList
 										.item(j).getTextContent();
 							} else if (questionChildNodesList.item(j)
 									.getNodeName()
 									.equalsIgnoreCase(QUESTION_TEXT)) {
 								if (questionChildNodesList.item(j)
 										.getAttributes().getNamedItem(FORMAT) != null) {
 									questionText = new QuestionText(
 											questionChildNodesList.item(j)
 													.getTextContent(),
 											QuestionTextFormat
 													.valueOf(questionChildNodesList
 															.item(j)
 															.getAttributes()
 															.getNamedItem(
 																	FORMAT)
 															.getNodeValue()));
 								} else {
 									questionText = new QuestionText(
 											questionChildNodesList.item(j)
 													.getTextContent(),
 											DEFAULT_QUESTION_TEXT_FORMAT);
 								}
 							} else if (questionChildNodesList.item(j)
 									.getNodeName().equalsIgnoreCase(IMAGE_URL)) {
 								imageUrl = questionChildNodesList
 												.item(j).getNodeValue();
 							} else if (questionChildNodesList.item(j)
 									.getNodeName().equalsIgnoreCase(IMAGE_BASE_64)) {
 								imageBase64 = questionChildNodesList
 												.item(j).getNodeValue();
 							} else if (questionChildNodesList.item(j)
 									.getNodeName()
 									.equalsIgnoreCase(GENERAL_FEEDBACK)) {
 								generalFeedBack = questionChildNodesList
 												.item(j).getTextContent();
 							} else if (questionChildNodesList.item(j)
 									.getNodeName()
 									.equalsIgnoreCase(DEFAULT_GRAD) && questionChildNodesList.item(j).getNodeValue()!=null) {
 								defaultGrade = Float
 										.valueOf(questionChildNodesList.item(j)
 												.getNodeValue());
 							} else if (questionChildNodesList.item(j)
 									.getNodeName().equalsIgnoreCase(PENALTY) && questionChildNodesList.item(j).getNodeValue()!=null) {
 								penalty = Float
 										.valueOf(questionChildNodesList.item(j)
 												.getNodeValue());
 							} else if (questionChildNodesList.item(j)
 									.getNodeName().equalsIgnoreCase(HIDDEN) && questionChildNodesList.item(j).getNodeValue()!=null) {
 								isHidden = (Integer
 										.getInteger(questionChildNodesList
 												.item(j).getNodeValue()) == 1 ? true
 										: false);
 							}
 							
 							//question type
 							
 							else if (questionType.equalsIgnoreCase(ESSAY)) {
 								essayQuestion = new EssayQuestion();
 								essayQuestion.setName(questionName);
 								essayQuestion.setQuestionText(questionText);
 								essayQuestion.setGeneralFeedBack(generalFeedBack);
 								essayQuestion.setPenalty(penalty);
 								essayQuestion.setDefaultGrade(defaultGrade);
 								essayQuestion.setIsHidden(isHidden);
 								essayQuestion.setImageUrl(imageUrl);
 								essayQuestion.setImageBase64(imageBase64);
 								if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j).getNodeValue()!=null) {
 									essayQuestion
 											.setShuffleanswers(Integer
 													.getInteger(questionChildNodesList
 															.item(j)
 															.getNodeValue()) == 1 ? true
 													: false);
 								} else if (questionChildNodesList.item(j)
 										.getNodeName().equalsIgnoreCase(ANSWER)) {
 									answer = new Answer();
 									answer.setText(questionChildNodesList.item(j).getTextContent());
 									answerChildNodesList = questionChildNodesList
 											.item(j).getChildNodes();
 									if (answerChildNodesList != null) {
 										if(questionChildNodesList.item(j).getAttributes()!=null && questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION)!=null){
 											answer.setFraction(Float.valueOf(questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION).getNodeValue()));
 										}
 										for (int k = 0; k < answerChildNodesList
 												.getLength(); k++) {
 											if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(FEEDBACK)) {
 												answer.setFeedback(answerChildNodesList.item(k).getTextContent());
 											}
 										}
 									}
 									essayQuestion.setAnswer(answer);
 								}
 								essayQuestion.setErrors(errors);
 								importedQuiz.getQuestionList().add(essayQuestion);
 								importedQuiz.getNonExtractedQuestionList().add(essayQuestion);
 							} else if (questionType.equalsIgnoreCase(MATCHING)) {
 								
 								matchingQuestion = new MatchingQuestion();
 								matchingQuestion.setName(questionName);
 								matchingQuestion.setQuestionText(questionText);
 								matchingQuestion.setGeneralFeedBack(generalFeedBack);
 								matchingQuestion.setPenalty(penalty);
 								matchingQuestion.setDefaultGrade(defaultGrade);
 								matchingQuestion.setIsHidden(isHidden);
 								matchingQuestion.setImageUrl(imageUrl);
 								matchingQuestion.setImageBase64(imageBase64);
 								matchingQuestion.setSubquestion(new ArrayList<Subquestion>());
 								if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j).getNodeValue()!=null) {
 									matchingQuestion
 											.setShuffleanswers(Integer
 													.getInteger(questionChildNodesList
 															.item(j)
 															.getNodeValue()) == 1 ? true
 													: false);
 								} else if (questionChildNodesList.item(j)
 										.getNodeName().equalsIgnoreCase(SUB_QUESTION)) {
 									 subquestion = new Subquestion();
 									subquestion.setText(questionChildNodesList.item(j).getTextContent());
 									subquestionChildNodesList = questionChildNodesList
 											.item(j).getChildNodes();
 									if (subquestionChildNodesList != null) {
 										for (int k = 0; k < subquestionChildNodesList
 												.getLength(); k++) {
 											if (subquestionChildNodesList.item(k).getNodeName().equalsIgnoreCase(ANSWER)) {
 												answer = new Answer();
 												answer.setText(subquestionChildNodesList.item(k).getTextContent());
 												subquestion.setAnswer(answer);
 											}
 										}
 									}
 									matchingQuestion.getSubquestion().add(subquestion);
 								}
 								matchingQuestion.setErrors(errors);
 								importedQuiz.getQuestionList().add(matchingQuestion);
 								importedQuiz.getExtractedQuestionList().add(matchingQuestion);
 							}else if (questionType.equalsIgnoreCase(MULTI_CHOICE)) {
 
 								multipleChoiceQuestion = new MultipleChoiceQuestion();
 								multipleChoiceQuestion.setName(questionName);
 								multipleChoiceQuestion.setQuestionText(questionText);
 								multipleChoiceQuestion.setGeneralFeedBack(generalFeedBack);
 								multipleChoiceQuestion.setPenalty(penalty);
 								multipleChoiceQuestion.setDefaultGrade(defaultGrade);
 								multipleChoiceQuestion.setIsHidden(isHidden);
 								multipleChoiceQuestion.setImageUrl(imageUrl);
 								multipleChoiceQuestion.setImageBase64(imageBase64);
 								multipleChoiceQuestion.setAnswer(new ArrayList<Answer>());
 								if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j).getNodeValue()!=null) {
 									multipleChoiceQuestion
 											.setShuffleanswers(Integer
 													.getInteger(questionChildNodesList
 															.item(j)
 															.getNodeValue()) == 1 ? true
 													: false);
 								} else if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(CORRECT_FEEDBACK)) {
 									multipleChoiceQuestion
 											.setCorrectfeedback(questionChildNodesList
 															.item(j)
 															.getTextContent());
 								} else if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(PARTIALLY_CORRECT_FEEDBACK)) {
 									multipleChoiceQuestion
 											.setPartiallycorrectfeedback(questionChildNodesList
 															.item(j)
 															.getTextContent());
 								} else if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(INCORRECT_FEEDBACK)) {
 									multipleChoiceQuestion
 											.setIncorrectfeedback(questionChildNodesList
 															.item(j)
 															.getTextContent());
 								}  else if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(ANSWER_NUMBERING)) {
 									if (questionChildNodesList.item(j).getNodeValue()!=null) {
 										multipleChoiceQuestion
 											.setAnswernumbering(AnswerNumbering.valueOf(questionChildNodesList
 															.item(j)
 															.getTextContent()));
 									}
 								} else if (questionChildNodesList.item(j)
 										.getNodeName().equalsIgnoreCase(ANSWER)) {
 									answer = new Answer();
 									answer.setText(questionChildNodesList.item(j).getTextContent());
 									answerChildNodesList = questionChildNodesList
 											.item(j).getChildNodes();
 									if (answerChildNodesList != null) {
 										if(questionChildNodesList.item(j).getAttributes()!=null && questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION)!=null){
 											answer.setFraction(Float.valueOf(questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION).getNodeValue()));
 										}
 										for (int k = 0; k < answerChildNodesList
 												.getLength(); k++) {
 											if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(FEEDBACK)) {
 												answer.setFeedback(answerChildNodesList.item(k).getTextContent());
 											}
 										}
 									}
 									multipleChoiceQuestion.getAnswer().add(answer);
 								}
 								multipleChoiceQuestion.setErrors(errors);
 								importedQuiz.getQuestionList().add(multipleChoiceQuestion);
 								importedQuiz.getExtractedQuestionList().add(multipleChoiceQuestion);
 							} else if (questionType.equalsIgnoreCase(NUMERICAL)) {
 								numericalQuestion = new NumericalQuestion();
 								numericalQuestion.setName(questionName);
 								numericalQuestion.setQuestionText(questionText);
 								numericalQuestion.setGeneralFeedBack(generalFeedBack);
 								numericalQuestion.setPenalty(penalty);
 								numericalQuestion.setDefaultGrade(defaultGrade);
 								numericalQuestion.setIsHidden(isHidden);
 								numericalQuestion.setImageUrl(imageUrl);
 								numericalQuestion.setImageBase64(imageBase64);
 								numericalQuestion.setUnit(new ArrayList<Unit>());
 								numericalQuestion.setAnswer(new ArrayList<AnswerNumerical>());
 								if (questionChildNodesList.item(j)
 										.getNodeName()
 										.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j).getNodeValue()!=null) {
 									numericalQuestion
 											.setShuffleanswers(Integer
 													.getInteger(questionChildNodesList
 															.item(j)
 															.getNodeValue()) == 1 ? true
 													: false);
 								} else if (questionChildNodesList.item(j)
 										.getNodeName().equalsIgnoreCase(ANSWER)) {
 									answerNumerical = new AnswerNumerical();
 									answerNumerical.setText(questionChildNodesList.item(j).getTextContent());
 									answerChildNodesList = questionChildNodesList
 											.item(j).getChildNodes();
 									if (answerChildNodesList != null) {
 										if(questionChildNodesList.item(j).getAttributes()!=null && questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION)!=null){
 											answerNumerical.setFraction(Float.valueOf(questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION).getNodeValue()));
 										}
 										for (int k = 0; k < answerChildNodesList
 												.getLength(); k++) {
 											if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(FEEDBACK)) {
 												answerNumerical.setFeedback(answerChildNodesList.item(k).getTextContent());
 											}
 											if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(TOLERANCE) && answerChildNodesList.item(k).getNodeValue()!=null) {
 												answerNumerical.setTolerance(Float.valueOf(answerChildNodesList.item(k).getNodeValue()));
 											}
 										}
 									}
 									numericalQuestion.getAnswer().add(answerNumerical);
 								}
 								 else if (questionChildNodesList.item(j)
 											.getNodeName().equalsIgnoreCase(UNITS)) {
 									 unitsChildNodesList = questionChildNodesList.item(j).getChildNodes();
 											for (int k = 0; k < unitsChildNodesList
 													.getLength(); k++) {
 												if(unitsChildNodesList.item(k).getNodeName().equalsIgnoreCase(UNIT)){
 												unit = new Unit();
 												unitChildNodesList = unitsChildNodesList.item(k).getChildNodes();
 												for (int l = 0; l < unitChildNodesList.getLength(); l++) {
 													if (unitChildNodesList.item(l).getNodeName().equalsIgnoreCase(UNIT_MULTIPLIER) && unitChildNodesList.item(l).getNodeValue()!=null) {
 														unit.setMultiplier(Integer.valueOf(unitChildNodesList.item(l).getNodeValue()));
 													}
 													else if (unitChildNodesList.item(l).getNodeName().equalsIgnoreCase(UNIT_NAME) && unitChildNodesList.item(l).getNodeValue()!=null) {
 														unit.setUnit_name(unitChildNodesList.item(l).getNodeValue());
 													}
 												}
 												numericalQuestion.getUnit().add(unit);
 											}
 											}
 										}
 								numericalQuestion.setErrors(errors);
 								importedQuiz.getQuestionList().add(numericalQuestion);
 								importedQuiz.getExtractedQuestionList().add(numericalQuestion);
 									} else if (questionType.equalsIgnoreCase(SHORT_ANSWER)) {
 
 										shortAnswerQuestion = new ShortAnswerQuestion();
 										shortAnswerQuestion.setName(questionName);
 										shortAnswerQuestion.setQuestionText(questionText);
 										shortAnswerQuestion.setGeneralFeedBack(generalFeedBack);
 										shortAnswerQuestion.setPenalty(penalty);
 										shortAnswerQuestion.setDefaultGrade(defaultGrade);
 										shortAnswerQuestion.setIsHidden(isHidden);
 										shortAnswerQuestion.setImageUrl(imageUrl);
 										shortAnswerQuestion.setImageBase64(imageBase64);
 										shortAnswerQuestion.setAnswer(new ArrayList<Answer>());
 										if (questionChildNodesList.item(j)
 												.getNodeName()
 												.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j).getNodeValue()!=null) {
 											shortAnswerQuestion
 													.setShuffleanswers(Integer
 															.getInteger(questionChildNodesList
 																	.item(j)
 																	.getNodeValue()) == 1 ? true
 															: false);
 										} else if (questionChildNodesList.item(j)
 												.getNodeName()
 												.equalsIgnoreCase(USECASE) && questionChildNodesList.item(j).getNodeValue()!=null) {
 											shortAnswerQuestion.setUsecase(Integer
 													.getInteger(questionChildNodesList
 															.item(j)
 															.getNodeValue()) == 1 ? true
 													: false);
 										} else if (questionChildNodesList.item(j)
 												.getNodeName().equalsIgnoreCase(ANSWER)) {
 											answer = new Answer();
 											answer.setText(questionChildNodesList.item(j).getTextContent());
 											answerChildNodesList = questionChildNodesList
 													.item(j).getChildNodes();
 											if (answerChildNodesList != null) {
 												if(questionChildNodesList.item(j).getAttributes()!=null && questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION)!=null){
 													answer.setFraction(Float.valueOf(questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION).getNodeValue()));
 												}
 												for (int k = 0; k < answerChildNodesList
 														.getLength(); k++) {
 													if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(FEEDBACK)) {
 														answer.setFeedback(answerChildNodesList.item(k).getTextContent());
 													}
 												}
 											}
 											shortAnswerQuestion.getAnswer().add(answer);
 										}
 										shortAnswerQuestion.setErrors(errors);
 										importedQuiz.getQuestionList().add(shortAnswerQuestion);
 										importedQuiz.getExtractedQuestionList().add(shortAnswerQuestion);
 									}  else if (questionType.equalsIgnoreCase(TRUE_FALSE)) {
 
 										trueFalseQuestion = new TrueFalseQuestion();
 										trueFalseQuestion.setName(questionName);
 										trueFalseQuestion.setQuestionText(questionText);
 										trueFalseQuestion.setGeneralFeedBack(generalFeedBack);
 										trueFalseQuestion.setPenalty(penalty);
 										trueFalseQuestion.setDefaultGrade(defaultGrade);
 										trueFalseQuestion.setIsHidden(isHidden);
 										trueFalseQuestion.setImageUrl(imageUrl);
 										trueFalseQuestion.setImageBase64(imageBase64);
 										trueFalseQuestion.setAnswer(new ArrayList<Answer>());
 										if (questionChildNodesList.item(j)
 												.getNodeName().equalsIgnoreCase(ANSWER)) {
 											answer = new Answer();
 											answer.setText(questionChildNodesList.item(j).getTextContent());
 											answerChildNodesList = questionChildNodesList
 													.item(j).getChildNodes();
 											if (answerChildNodesList != null) {
 												if(questionChildNodesList.item(j).getAttributes()!=null && questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION)!=null){
 													answer.setFraction(Float.valueOf(questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION).getNodeValue()));
 												}
 												for (int k = 0; k < answerChildNodesList
 														.getLength(); k++) {
 													if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(FEEDBACK)) {
 														answer.setFeedback(answerChildNodesList.item(k).getTextContent());
 													}
 												}
 											}
 											trueFalseQuestion.getAnswer().add(answer);
 										}
 										trueFalseQuestion.setErrors(errors);
 										importedQuiz.getQuestionList().add(trueFalseQuestion);
 										importedQuiz.getExtractedQuestionList().add(trueFalseQuestion);
 									} else if (questionType.equalsIgnoreCase(CLOZE)) {
 										clozeQuestion = new ClozeQuestion();
 										clozeQuestion.setName(questionName);
 										clozeQuestion.setQuestionText(questionText);
 										clozeQuestion.setGeneralFeedBack(generalFeedBack);
 										if (questionChildNodesList.item(j)
 												.getNodeName()
 												.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j).getNodeValue()!=null) {
 											clozeQuestion
 													.setShuffleanswers(Integer
 															.getInteger(questionChildNodesList
 																	.item(j)
 																	.getNodeValue()) == 1 ? true
 															: false);
 										}
 										clozeQuestion.setErrors(errors);
 										importedQuiz.getQuestionList().add(clozeQuestion);
 										importedQuiz.getExtractedQuestionList().add(clozeQuestion);
 									}  else if (questionType.equalsIgnoreCase(DESCRIPTION)) {
 										descriptionQuestion = new DescriptionQuestion();
 										descriptionQuestion.setName(questionName);
 										descriptionQuestion.setQuestionText(questionText);
 										descriptionQuestion.setGeneralFeedBack(generalFeedBack);
 										descriptionQuestion.setPenalty(penalty);
 										descriptionQuestion.setDefaultGrade(defaultGrade);
 										descriptionQuestion.setIsHidden(isHidden);
 										if (questionChildNodesList.item(j)
 												.getNodeName()
 												.equalsIgnoreCase(IMAGE) ) {
 											descriptionQuestion
 													.setImage(questionChildNodesList
 																	.item(j)
 																	.getNodeValue());
 										} else if (questionChildNodesList.item(j)
 												.getNodeName()
 												.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j).getNodeValue()!=null) {
 											descriptionQuestion
 													.setShuffleanswers(Integer
 															.getInteger(questionChildNodesList
 																	.item(j)
 																	.getNodeValue()) == 1 ? true
 															: false);
 										} 
 										
 										descriptionQuestion.setErrors(errors);
 										importedQuiz.getQuestionList().add(descriptionQuestion);
 										importedQuiz.getExtractedQuestionList().add(descriptionQuestion);
 									}  else if (questionType.equalsIgnoreCase(CALCULATED)) {
 										
 										calculatedQuestion = new CalculatedQuestion();
 										calculatedQuestion.setName(questionName);
 										calculatedQuestion.setQuestionText(questionText);
 										calculatedQuestion.setGeneralFeedBack(generalFeedBack);
 										calculatedQuestion.setPenalty(penalty);
 										calculatedQuestion.setDefaultGrade(defaultGrade);
 										calculatedQuestion.setIsHidden(isHidden);
 										calculatedQuestion.setUnit(new ArrayList<Unit>());
 										calculatedQuestion.setAnswerCalculatedList(new ArrayList<AnswerCalculated>());
 										calculatedQuestion.setCalculatedQuestionDataSetDefinitionList(new ArrayList<CalculatedQuestionDataSetDefinition>());
 										if (questionChildNodesList.item(j)
 												.getNodeName()
 												.equalsIgnoreCase(SHUFFLE_ANSWERS) && questionChildNodesList.item(j)
 												.getNodeValue()!=null) {
 											calculatedQuestion
 													.setShuffleanswers(Integer
 															.getInteger(questionChildNodesList
 																	.item(j)
 																	.getNodeValue()) == 1 ? true
 															: false);
 										} else if (questionChildNodesList.item(j)
 												.getNodeName().equalsIgnoreCase(ANSWER)) {
 											answerCalculated = new AnswerCalculated();
 											answerCalculated.setText(questionChildNodesList.item(j).getTextContent());
 											answerChildNodesList = questionChildNodesList
 													.item(j).getChildNodes();
 											if (answerChildNodesList != null) {
 												if(questionChildNodesList.item(j).getAttributes()!=null && questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION)!=null){
 													answerCalculated.setFraction(Float.valueOf(questionChildNodesList.item(j).getAttributes().getNamedItem(FRACTION).getNodeValue()));
 												}
 												for (int k = 0; k < answerChildNodesList
 														.getLength(); k++) {
 													if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(FEEDBACK)) {
 														answerCalculated.setFeedback(answerChildNodesList.item(k).getTextContent());
 													} else if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(TOLERANCE) && answerChildNodesList.item(k).getNodeValue()!=null) {
 														answerCalculated.setTolerance(Float.valueOf(answerChildNodesList.item(k).getNodeValue()));
 													} else if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(TOLERANCE_TYPE) && answerChildNodesList.item(k).getNodeValue()!=null) {
 														answerCalculated.setTolerancetype(Integer.valueOf(answerChildNodesList.item(k).getNodeValue()));
 													} else if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(CORRECT_ANSWER_FORMAT) && answerChildNodesList.item(k).getNodeValue()!=null) {
 														answerCalculated.setCorrectanswerformat(Integer.valueOf(answerChildNodesList.item(k).getNodeValue()));
 													} else if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(CORRECT_ANSWER_LENGTH) && answerChildNodesList.item(k).getNodeValue()!=null) {
 														answerCalculated.setCorrectanswerlength(Integer.valueOf(answerChildNodesList.item(k).getNodeValue()));
 													} else if (answerChildNodesList.item(k).getNodeName().equalsIgnoreCase(FEEDBACK)) {
 														answerCalculated.setFeedback(answerChildNodesList.item(k).getTextContent());
 													}
 												}
 											}
 											calculatedQuestion.getAnswerCalculatedList().add(answerCalculated);
 										} else if (questionChildNodesList.item(j)
 													.getNodeName().equalsIgnoreCase(UNITS)) {
 											 unitsChildNodesList = questionChildNodesList.item(j).getChildNodes();
 													for (int k = 0; k < unitsChildNodesList
 															.getLength(); k++) {
 														if(unitsChildNodesList.item(k).getNodeName().equalsIgnoreCase(UNIT)){
 														unit = new Unit();
 														unitChildNodesList = unitsChildNodesList.item(k).getChildNodes();
 														for (int l = 0; l < unitChildNodesList.getLength(); l++) {
 															if (unitChildNodesList.item(l).getNodeName().equalsIgnoreCase(UNIT_MULTIPLIER) && unitChildNodesList.item(l).getNodeValue()!=null) {
 																unit.setMultiplier(Integer.valueOf(unitChildNodesList.item(l).getNodeValue()));
 															}
 															else if (unitChildNodesList.item(l).getNodeName().equalsIgnoreCase(UNIT_NAME) && unitChildNodesList.item(l).getNodeValue()!=null) {
 																unit.setUnit_name(unitChildNodesList.item(l).getNodeValue());
 															}
 														}
 														calculatedQuestion.getUnit().add(unit);
 													}
 													}
 												} else if (questionChildNodesList.item(j)
 														.getNodeName().equalsIgnoreCase(DATASET_DEFINITIONS)) {
 													calculatedQuestion.setCalculatedQuestionDataSetDefinitionList(new ArrayList<CalculatedQuestionDataSetDefinition>());
 													 dataSetDefinitionsChildNodesList = questionChildNodesList.item(j).getChildNodes();
 													if(dataSetDefinitionsChildNodesList!=null) {		
 													 for (int k = 0; k < dataSetDefinitionsChildNodesList
 																	.getLength(); k++) {
 																if(dataSetDefinitionsChildNodesList.item(k).getNodeName().equalsIgnoreCase(DATASET_DEFINITION)){
 																calculatedQuestionDataSetDefinition = new CalculatedQuestionDataSetDefinition();
 																dataSetDefinitionChildNodesList = dataSetDefinitionsChildNodesList.item(k).getChildNodes();
 																if(dataSetDefinitionChildNodesList!=null) {
 																for (int l = 0; l < dataSetDefinitionChildNodesList.getLength(); l++) {
 																	if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(STATUS)) {
 																		calculatedQuestionDataSetDefinition.setStatus(dataSetDefinitionChildNodesList.item(l).getTextContent());
 																	}
 																	else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(NAME)) {
 																		calculatedQuestionDataSetDefinition.setName(dataSetDefinitionChildNodesList.item(l).getTextContent());
 																	}
 																	else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(TYPE)) {
 																		calculatedQuestionDataSetDefinition.setType(dataSetDefinitionChildNodesList.item(l).getNodeValue());
 																	} else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(DISTRIBUTION)) {
 																		calculatedQuestionDataSetDefinition.setDistribution(dataSetDefinitionChildNodesList.item(l).getTextContent());
 																	} else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(MINIMUM) && dataSetDefinitionChildNodesList.item(l).getTextContent()!=null) {
 																		calculatedQuestionDataSetDefinition.setMin(Float.valueOf(dataSetDefinitionChildNodesList.item(l).getTextContent()));
 																	} else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(MAXIMUM) && dataSetDefinitionChildNodesList.item(l).getTextContent()!=null) {
 																		calculatedQuestionDataSetDefinition.setMax(Float.valueOf(dataSetDefinitionChildNodesList.item(l).getTextContent()));
 																	} else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(DECIMALS) && dataSetDefinitionChildNodesList.item(l).getTextContent()!=null) {
 																		calculatedQuestionDataSetDefinition.setDecimals(Float.valueOf(dataSetDefinitionChildNodesList.item(l).getTextContent()));
 																	} else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(ITEM_COUNT) && dataSetDefinitionChildNodesList.item(l).getNodeValue()!=null) {
 																		calculatedQuestionDataSetDefinition.setItemcount(Integer.valueOf(dataSetDefinitionChildNodesList.item(l).getNodeValue()));
 																	} else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(DATASET_ITEMS)) {
 																		calculatedQuestionDataSetDefinition.setCalculatedQuestionDataSetItemList(new ArrayList<CalculatedQuestionDataSetItem>());
 																		dataSetItemsChildNodesList = dataSetDefinitionChildNodesList.item(l).getChildNodes();
 																		if (dataSetItemsChildNodesList != null ) {
 																			for (int m = 0; m < dataSetItemsChildNodesList.getLength(); m++) {
 																				if(dataSetItemsChildNodesList.item(m).getNodeName().equalsIgnoreCase(DATASET_ITEM)){
 																					calculatedQuestionDataSetItem = new CalculatedQuestionDataSetItem();
 																				dataSetItemChildNodesList = dataSetItemsChildNodesList.item(m).getChildNodes();
 																				if(dataSetItemChildNodesList!=null){
 																					for (int n = 0; n < dataSetItemChildNodesList.getLength(); n++) {
 																						if (dataSetItemChildNodesList.item(n).getNodeName().equalsIgnoreCase(NUMBER) && dataSetItemChildNodesList.item(n).getNodeValue() != null) {
 																							calculatedQuestionDataSetItem.setNumber(Integer.getInteger(dataSetItemChildNodesList.item(n).getNodeValue()));
 																						} else if (dataSetItemChildNodesList.item(n).getNodeName().equalsIgnoreCase(VALUE) && dataSetItemChildNodesList.item(n).getNodeValue() != null) {
 																							calculatedQuestionDataSetItem.setValue(Float.valueOf(dataSetItemChildNodesList.item(n).getNodeValue()));
 																						}
 																					}
 																				}
 																				calculatedQuestionDataSetDefinition.getCalculatedQuestionDataSetItemList().add(calculatedQuestionDataSetItem);
 																			}
 																			}
 																		}
 																	} else if (dataSetDefinitionChildNodesList.item(l).getNodeName().equalsIgnoreCase(NUMBER_OF_ITEM) && dataSetDefinitionChildNodesList.item(l).getNodeValue()!=null) {
 																		calculatedQuestionDataSetDefinition.setNumberOfItems(Integer.valueOf(dataSetDefinitionChildNodesList.item(l).getNodeValue()));
 																	}
 																}
 																
 																}
 																calculatedQuestion.getCalculatedQuestionDataSetDefinitionList().add(calculatedQuestionDataSetDefinition);
 															}
 															}
 													 
 												}
 														}
 										calculatedQuestion.setErrors(errors);
 										importedQuiz.getQuestionList().add(calculatedQuestion);
 										importedQuiz.getExtractedQuestionList().add(calculatedQuestion);
 									}  else {
 										importedQuiz.getExtractedQuestionList().add(genericQuestion);
 									}
 									
 
 									}
 							}
 
 							}
 							importedQuiz.getProcessedQuestionList().add(genericQuestion);
 							}
							System.out.println("processed = "+importedQuiz.getProcessedQuestionCount());
							System.out.println("extracted = "+importedQuiz.getExtractedQuestionCount());
							System.out.println("nonExcracted = "+importedQuiz.getNonExtractedQuestionCount());
			 for (int i = 0; i < importedQuiz.getQuestionList().size(); i++) {
			 if (importedQuiz.getQuestionList().get(i) instanceof
 GenericQuestion) {
					System.out.println(((GenericQuestion) importedQuiz
							.getQuestionList().get(i)).getName());
					System.out.println(((GenericQuestion) importedQuiz
							.getQuestionList().get(i)).getImageUrl());
				}
			 }
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return importedQuiz;
 	}
 
 	public OutputStream exportQuiz(IQuiz quiz) {
 		//TODO priorité 3 - externaliser le path
 		final String PATH_FILE = "./quiz-jaxb.xml";
 
 
 		// create JAXB context and instantiate marshaller
 
 		JAXBContext context;
 		OutputStream os = null;
 		try {
 
 			context = JAXBContext.newInstance(Quiz.class);
 			Marshaller m = context.createMarshaller();
 			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 			// m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,"");
 			os = new FileOutputStream(PATH_FILE);
 			m.marshal(quiz, os);
 			//ligne suivante à enlever
 			m.marshal(quiz, System.out);
 
 		} catch (JAXBException e1) {
 			//TODO Priorité: 3 - externaliser les msgs
 			System.err.println("Marsupilami's Project: Erreur JAXB - " +
 					"Survenue lors de la sérialisation (Objet Java -> Moodle Xml)");
 			e1.printStackTrace();
 		} catch (FileNotFoundException e) {
 			System.err.println("Marsupilami's Project:");
 			e.printStackTrace();
 		}
 
 		return os;
 	}
 
 }
