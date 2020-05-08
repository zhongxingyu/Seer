 package hu.advancedweb.lms.evaluation;
 
 import hu.advancedweb.model.ExamAnswer;
 import hu.advancedweb.model.ExamConfig;
 import hu.advancedweb.pilot.rhino.ValidationBean;
 import hu.advancedweb.service.ExamAnswerLocalServiceUtil;
 import hu.advancedweb.service.ExamConfigLocalServiceUtil;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.javatuples.Pair;
 import org.json.simple.JSONObject;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 
 public class ExamEvaluator {
 
 	public static void main(String[] args) {
 		ExamValidationResult result = new ExamEvaluator().evaluate(appendAnswers("", "page1", ImmutableMap.of("exc1", "corr1", "exc2", "corr2")), generateDefaultEvaluatorJavascript(new DefaultExamEvaluatorLogic() {
 			{
 				addCorrectAnswer("page1", "exc1", "corr1", 1);
 				addCorrectAnswer("page1", "exc2", "corr2", 2);
 			}
 		}));
 		System.out.println(result);
 	}
 
 	private static String appendAnswers(String oldAnswers, String pageName, Map<String, String> newAnswers) {
 		if (oldAnswers == null || oldAnswers.trim().compareTo("") == 0) {
 			oldAnswers = "{}";
 		}
 		ExamAnswers ans = new ExamAnswers(oldAnswers);
 		ans.answers.put(pageName, Maps.newHashMap(newAnswers));
 		return JSONObject.toJSONString(ans.answers);
 	}
 
 	public static void appendAnswers(long companyId, long groupId, long userId, long examConfigId, String pageName, Map<String, String> newAnswers) throws SystemException {
 		ExamAnswer answer = ExamAnswerLocalServiceUtil.getExamAnswer(companyId, groupId, userId, examConfigId);
		answer.setAnswers(appendAnswers(answer.getAnswers(), pageName, newAnswers));
		ExamAnswerLocalServiceUtil.updateExamAnswer(answer);
 	}
 
 	public static ExamConfig createExamConfig(long companyId, long groupId, ExamTest test, Optional<String> evaluator, Optional<DefaultExamEvaluatorLogic> evaluatorLogic) throws SystemException {
 		Preconditions.checkArgument(evaluator.isPresent() && evaluatorLogic.isPresent() == false || evaluator.isPresent() == false && evaluatorLogic.isPresent(), "Evaluator must be present iff evaluatorlogic is absent");
 
 		String evaluatorString = evaluator.isPresent() ? evaluator.get() : generateDefaultEvaluatorJavascript(evaluatorLogic.get());
 
 		String questions = JSONObject.toJSONString(test.tests);
 
 		System.out.println("company: " + companyId);
 		System.out.println("groupId: " + groupId);
 		System.out.println("questions: " + questions);
 		System.out.println("evaluatorString: " + evaluatorString);
 		
 		return ExamConfigLocalServiceUtil.createExamConfig(companyId, groupId, questions, evaluatorString);
 	}
 
 	public static ExamConfig updateExamConfig(long id, ExamTest test, Optional<String> evaluator, Optional<DefaultExamEvaluatorLogic> evaluatorLogic) throws PortalException, SystemException {
 		Preconditions.checkArgument(evaluator.isPresent() && evaluatorLogic.isPresent() == false || evaluator.isPresent() == false && evaluatorLogic.isPresent(), "Evaluator must be present iff evaluatorlogic is absent");
 
 		ExamConfig config = ExamConfigLocalServiceUtil.getExamConfig(id);
 
 		String evaluatorString = evaluator.isPresent() ? evaluator.get() : generateDefaultEvaluatorJavascript(evaluatorLogic.get());
 
 		String questions = JSONObject.toJSONString(test.tests);
 
 		config.setEvaluator(evaluatorString);
 		config.setQuestions(questions);
 		
 		System.out.println("id: " + id);
 		System.out.println("questions: " + questions);
 		System.out.println("evaluatorString: " + evaluatorString);
 
 		return ExamConfigLocalServiceUtil.updateExamConfig(config);
 	}
 
 	public static ExamValidationResult evaluate(long companyId, long groupId, long userId, long examConfigId) throws PortalException, SystemException {
 		ExamConfig config = ExamConfigLocalServiceUtil.getExamConfig(examConfigId);
 
 		ExamAnswer answer = ExamAnswerLocalServiceUtil.getExamAnswer(companyId, groupId, userId, examConfigId);
 
 		return evaluate(answer.getAnswers(), config.getEvaluator());
 	}
 
 	private static String generateDefaultEvaluatorJavascript(DefaultExamEvaluatorLogic logic) {
 		StringBuilder result = new StringBuilder();
 
 		result.append("function validate(){");
 		for (Entry<String, Map<String, Pair<String, Integer>>> pages : logic.correctAnswers.entrySet()) {
 			for (Entry<String, Pair<String, Integer>> exercises : pages.getValue().entrySet()) {
 				result.append("defaultEvaluatorLogic.addCorrectAnswer('" + pages.getKey() + "', '" + exercises.getKey() + "', '" + exercises.getValue().getValue0() + "', " + exercises.getValue().getValue1() + ");");
 			}
 		}
 
 		result.append("defaultEvaluator.evaluate(examAnswers,defaultEvaluatorLogic,validationDataObj);}");
 
 		return result.toString();
 	}
 
 	private static ExamValidationResult evaluate(String answers, String evaluationLogic) {
 		Context cx = Context.enter();
 
 		try {
 			Scriptable scope = cx.initStandardObjects();
 
 			/*
 			 * Loads the helper js functions to the scope.
 			 */
 			// Script javascriptFrameworkScript = Context.getCurrentContext().compileString(getJavascriptFramework(), "Framework Script", 1, null);
 			// javascriptFrameworkScript.exec(cx, scope);
 
 			/*
 			 * Loads the exam data object to the scope.
 			 */
 			Scriptable examData = cx.getWrapFactory().wrapAsJavaObject(cx, scope, new ExamAnswers(answers), ExamAnswers.class);
 			scope.put("examAnswers", scope, examData);
 
 			/*
 			 * Loads the validations js function to the scope.
 			 */
 			Script validationScript = Context.getCurrentContext().compileString(evaluationLogic, "Validation Script", 1, null);
 			validationScript.exec(cx, scope);
 
 			/* Loads the default evaluator */
 			Scriptable defaultEvaluator = cx.getWrapFactory().wrapAsJavaObject(cx, scope, new DefaultExamEvaluator(), DefaultExamEvaluator.class);
 			scope.put("defaultEvaluator", scope, defaultEvaluator);
 
 			Scriptable defaultEvaluatorLogic = cx.getWrapFactory().wrapAsJavaObject(cx, scope, new DefaultExamEvaluatorLogic(), DefaultExamEvaluatorLogic.class);
 			scope.put("defaultEvaluatorLogic", scope, defaultEvaluatorLogic);
 
 			/*
 			 * Loads the validation data object to the scope.
 			 */
 
 			ExamValidationResult validationBean = new ExamValidationResult();
 			Scriptable validationData = cx.getWrapFactory().wrapAsJavaObject(cx, scope, validationBean, ValidationBean.class);
 			scope.put("validationDataObj", scope, validationData);
 
 			/*
 			 * Validate.
 			 */
 			cx.evaluateString(scope, "validate();", "Main Script", 1, null);
 
 			return validationBean;
 
 			/*
 			 * Do something with the filled validation bean.
 			 */
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			Context.exit();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns the helper JS functions. (dummy)
 	 * 
 	 * I think it should contain basic framework functions, to validate all the pages, and DSL like accessor functions to the result object for common validation tasks eg.: -
 	 * check(1, questionOne, {'answerOne':1,'answerTwo':0.5}, 'rightAnswer') to validate input 'questionOne' on page 1, or something
 	 * 
 	 * functions for debugging (something, that adds an entry to the result, and be displayed somewhere)
 	 */
 	private String getJavascriptFramework() {
 		BufferedReader input = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/evaluatorframework.js")));
 		StringBuilder contents = new StringBuilder();
 		try {
 			try {
 				String line = null;
 				while ((line = input.readLine()) != null) {
 					contents.append(line);
 					contents.append(System.getProperty("line.separator"));
 				}
 			} finally {
 				input.close();
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		return contents.toString();
 	}
 
 }
