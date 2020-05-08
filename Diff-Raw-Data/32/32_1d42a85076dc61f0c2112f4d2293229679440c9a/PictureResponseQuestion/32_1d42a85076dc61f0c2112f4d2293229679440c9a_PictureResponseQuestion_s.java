 package site;
 
 public class PictureResponseQuestion implements Question {
 	private int questionId;
     private int quizId;
     private int pointValue;
     private int questionType;
     private Answer answer;
     
     private int pictureResponseId;
     private String fileName;
     
     public PictureResponseQuestion(int questionId, int quizId, int pointValue, int questionType, 
     		Answer answer, int pictureResponseId, String fileName) {
     	this.setQuestionId(questionId);
         this.setQuizId(quizId);
         this.setPointValue(pointValue);
         this.setQuestionType(questionType);
         this.setAnswer(answer);
         this.setPictureResponseId(pictureResponseId);
         this.setFileName(fileName);
     }
     
    public PictureResponseQuestion(int pointValue, int questionType, Answer answer, String fileName) {
         this.setPointValue(pointValue);
         this.setQuestionType(questionType);
         this.setAnswer(answer);
         this.setFileName(fileName);
     }
     
     public int getQuestionId() {
 		return questionId;
 	}
 
 	public void setQuestionId(int questionId) {
 		this.questionId = questionId;
 	}
 
 	public int getQuizId() {
 		return quizId;
 	}
 
 	public void setQuizId(int quizId) {
 		this.quizId = quizId;
 	}
 	
 	public int getPointValue() {
 		return pointValue;
 	}
 
 	public void setPointValue(int pointValue) {
 		this.pointValue = pointValue;
 	}
 
 	public int getQuestionType() {
 		return questionType;
 	}
 
 	public void setQuestionType(int questionType) {
 		this.questionType = questionType;
 	}
 
 	public Answer getAnswer() {
 		return answer;
 	}
 
 	public void setAnswer(Answer answer) {
 		this.answer = answer;
 	}
 
 	public int getPictureResponseId() {
 		return pictureResponseId;
 	}
 
 	public void setPictureResponseId(int pictureResponseId) {
 		this.pictureResponseId = pictureResponseId;
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 }
