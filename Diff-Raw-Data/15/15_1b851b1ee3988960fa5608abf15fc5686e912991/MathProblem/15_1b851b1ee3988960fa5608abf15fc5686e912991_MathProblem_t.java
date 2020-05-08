 package bu.edu.cs673.edukid.settings.utils;
 
 public enum MathProblem {
 
	MATH_PROBLEM_1("What is 13 + 72 ?", 85), MATH_PROBLEM_2("What is 2 - 2 ?", 0);
 
 	private String question;
 
 	private int answer;
 
 	private MathProblem(String question, int answer) {
 		this.question = question;
 		this.answer = answer;
 	}
 
 	public String getQuestion() {
 		return question;
 	}
 
 	public int getAnswer() {
 		return answer;
 	}
 
 }
