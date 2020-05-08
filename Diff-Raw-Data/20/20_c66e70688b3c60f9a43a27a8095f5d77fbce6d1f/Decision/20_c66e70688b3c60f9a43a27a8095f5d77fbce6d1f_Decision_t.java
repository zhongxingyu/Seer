 package org.jdominion.decisions;
 
 import java.io.Serializable;
 
 import org.jdominion.Hand;
 import org.jdominion.Supply;
 import org.jdominion.Turn;
 
 public abstract class Decision<E> implements Serializable {
 
 	private String userMessage;
 	private E answer = null;
 	private boolean cancelable;
 	private boolean canceled;
 
 	public String getUserMessage() {
 		return userMessage;
 	}
 
 	public void setAnswer(E answer) {
		assert isValidAnswer(answer) : "setting an invalid Answer";
 		if (isValidAnswer(answer)) {
 			this.answer = answer;
 		} else {
 			throw new UnsupportedOperationException("setting an invalid Answer");
 		}
 	}
 
 	public E getAnswer() {
 		return this.answer;
 	}
 
 	public boolean isCancelable() {
 		return cancelable;
 	}
 
 	public boolean isCanceled() {
 		return canceled;
 	}
 
 	public void setCanceled(boolean canceled) {
 		this.canceled = canceled;
 	}
 
 	public Decision(String userMessage, boolean cancelable) {
 		this.userMessage = userMessage;
 		this.cancelable = cancelable;
 	}
 
 	public abstract boolean isValidAnswer(E answer);
 
 	public abstract void chooseDefaultAnswer(Hand hand, Turn currentTurn, Supply supply);
 
 	public boolean isAnswered() {
 		if (canceled) {
 			return true;
 		}
 		return answer != null;
 	}
 
 }
