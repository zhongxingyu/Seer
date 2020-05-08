 package com.education.flashmath.models;
 
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.education.flashmath.utils.Constants.SubjectCategory;
 
 public class ArithmeticQuestion extends Question {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2924331896021814918L;
 	
 	protected int operand1;
 	protected int operand2;
 	protected String operator;
 	
 	public ArithmeticQuestion(JSONObject json, String subject) {
 		
 		super(json);
 		String tempQuestion = this.questionText.replace('(', ' ').replace(')', ' ').replaceAll("'", "");
 		String[] operandsArray = tempQuestion.split(",");
 		this.operand1 = Integer.valueOf(operandsArray[0].trim());
 		this.operand2 = Integer.valueOf(operandsArray[1].trim());
 		this.subjectId = identifySubjectFromSubjectString(subject);
 		setupOperator(this.subjectId);
 	}
 	
 	private void setupOperator(SubjectCategory subjectId) {
 		switch (subjectId) {
 		case ADDITION:
 			this.operator = "+";
 			break;
 		case SUBTRACTION:
 			this.operator = "-";
 			break;
 		case MULTIPLICATION:
 			this.operator = "×";
 			break;
 		case DIVISION:
 			this.operator = "÷";
 			break;
 		default:
 			this.operator = "+";
 			break;
 		}
 	}
 
 	public static ArrayList<Question> fromJSONArray(JSONArray jsonArray, String subject) {
 		ArrayList<Question> questions = new ArrayList<Question>();
 		for (int i = 0; i < jsonArray.length(); i++) {
 			try {
 				ArithmeticQuestion question = new ArithmeticQuestion(jsonArray.getJSONObject(i), subject);
 				question.setQuestionId(i + 1);
 				questions.add(question);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		return questions;
 	}
 	
 	private static SubjectCategory identifySubjectFromSubjectString(
 			String subject) {
 		if (subject.equalsIgnoreCase("Addition")) {
 			return SubjectCategory.ADDITION;
 		} else if (subject.equalsIgnoreCase("Subtraction")) {
 			return SubjectCategory.SUBTRACTION;
 		} else if (subject.equalsIgnoreCase("Multiplication")) {
 			return SubjectCategory.MULTIPLICATION;
 		} else if (subject.equalsIgnoreCase("Fractions")) {
 			return SubjectCategory.FRACTIONS;
 		} else if (subject.equalsIgnoreCase("Division")) {
 			return SubjectCategory.DIVISION;
 		} else if (subject.equalsIgnoreCase("Geometry")) {
 			return SubjectCategory.GEOMETRY;
 		} else  {
 			return SubjectCategory.GEOMETRY;
 		}
 	}
 
 	public int getOperand1() {
 		return operand1;
 	}
 	public int getOperand2() {
 		return operand2;
 	}
 
 
 
 	public String getOperator() {
 		return operator;
 	}
 
 	public void setOperator(String operator) {
 		this.operator = operator;
 	}
 
 	public void setOperand1(int operand1) {
 		this.operand1 = operand1;
 	}
 	public void setOperand2(int operand2) {
 		this.operand2 = operand2;
 	}
 
 }
