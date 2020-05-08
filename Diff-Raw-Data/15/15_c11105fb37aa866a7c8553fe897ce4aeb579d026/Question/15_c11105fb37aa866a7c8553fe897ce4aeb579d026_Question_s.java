 package com.jozeflang.android.germanirregularverbs.main;
 
 import com.jozeflang.android.germanirregularverbs.db.VerbDTO;
 import com.jozeflang.android.germanirregularverbs.validator.AnswerType;
 
 /** 
 * Entry point activity
  * @author Jozef Lang (developer@jozeflang.com)
  */
 public class Question {
 	
 	private final AnswerType answerType;
 	private final VerbDTO verb;
 	
 	public Question(AnswerType answerType, VerbDTO verb) {
 		this.answerType = answerType;
 		this.verb = verb;
 	}
 	
 	public AnswerType getAnswerType() {
 		return answerType;
 	}
 	
 	public VerbDTO getVerb() {
 		return verb;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		if (!(o instanceof Question))
 			return false;
 		Question oQuestion = (Question) o;
 		return this.answerType.equals(oQuestion.answerType) && this.verb.equals(oQuestion.verb);
 	}
 	
 	@Override
 	public int hashCode() {
 		int prime = 17;
 		int result = 1;
 		result = (result*prime) + (answerType == null ? 0 : answerType.hashCode());
 		result = (result*prime) + (verb == null ? 0 : verb.hashCode());
 		return result;
 	}
 }
