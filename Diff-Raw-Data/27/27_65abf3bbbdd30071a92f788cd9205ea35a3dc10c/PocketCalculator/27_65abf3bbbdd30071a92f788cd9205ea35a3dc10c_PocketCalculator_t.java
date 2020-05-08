 package calculator.model;
 
 import calculator.model.events.DisplayEventListener;
 
 public class PocketCalculator {
 
 	private enum State {
 		CLEAR,
 		FIRST_OPERAND_STARTED,
 		FIRST_OPERAND_FINISHED,
 		OPERATOR_SET,
 		SECOND_OPERAND_STARTED,
		SECOND_OPERAND_FINISHED,
		FINISHED;
 	}
 
 	private Display display;
 	private double operand;
 	private double lastOperand;
 	private State state = State.CLEAR;
 	private Operator operator;
 
 	public PocketCalculator() {
 		display = new Display();
 	}
 
 	public void addListener(DisplayEventListener listener) {
 		display.addListener(listener);		
 	}
 
 	public void removeListener(DisplayEventListener listener) {
 		display.removeListener(listener);		
 	}
 
 	public void setOperator(Operator operator) {
 		switch(state) {
 		case CLEAR:
			if(operator != Operator.MINUS) {
				display.error();
				return;
			}
		case FINISHED:
 		case FIRST_OPERAND_STARTED:
 		case FIRST_OPERAND_FINISHED:
 			if(operator == Operator.SQUARE_ROOT){
 				Double result = Calculation.calculateResult(operator, operand);
 				setResult(result);
 				state  = State.FIRST_OPERAND_FINISHED;
 				return;
 			}
 			this.operator = operator;
 			state = State.OPERATOR_SET;
 			lastOperand = operand;	
 			break;
 		case OPERATOR_SET:			
 			this.operator = operator;
 			break;
 		case SECOND_OPERAND_STARTED:
 		case SECOND_OPERAND_FINISHED:
 			if(operator == Operator.SQUARE_ROOT){
 				Double result = Calculation.calculateResult(operator, operand);
 				setResult(result);
 				state  = State.SECOND_OPERAND_FINISHED;
 				return;
 			}
 			calculate(false);
 			this.operator = operator;
 			state = State.OPERATOR_SET;
 			lastOperand = operand;	
 			break;
 		}
 	}
 
 	public void calculateFromEqualSign() {
 		calculate(true);
 	}
 
 	private void calculate(boolean fromEqualSign) {
 		switch(state) {
 		case CLEAR:
 		case FIRST_OPERAND_STARTED:
 		case OPERATOR_SET:
		case FINISHED:
 			return;
 		case SECOND_OPERAND_STARTED:
 		case SECOND_OPERAND_FINISHED:
 			Double result = Calculation.calculateResult(lastOperand, operator, operand);
 			setResult(result);
 			deleteOperator();
 			if(fromEqualSign) {
				state = State.FINISHED;
 			} else {
 				state = State.SECOND_OPERAND_STARTED;
 			}
 		}	
 
 	}
 
 	private void setResult(Double result) {
 		display.setDisplay(result);
 		operand = result;
 	}
 
 	private void deleteOperator() {
 		operator = null;
 	}
 
 	public void addInput(String input) {
 		switch(state) {
 		case CLEAR:
 		case OPERATOR_SET:
 		case FIRST_OPERAND_FINISHED:
 		case SECOND_OPERAND_FINISHED:
		case FINISHED:
 			display.clear();
 		}
 		display.addContent(input);
 		switch(state) {
 		case CLEAR:
 		case FIRST_OPERAND_FINISHED:
 		case FIRST_OPERAND_STARTED:
 		case SECOND_OPERAND_FINISHED:
		case FINISHED:
 			state = State.FIRST_OPERAND_STARTED;
 			break;
 		case OPERATOR_SET:
 			state = State.SECOND_OPERAND_STARTED;
 			break;
 		case SECOND_OPERAND_STARTED:		
 			state = State.SECOND_OPERAND_STARTED;
 			break;
 		}		
 		operand = display.getNumber();
 	}
 
 	public Display getDisplay() {
 		return display;
 	}
 
 	public void executeClear() {
 		lastOperand = operand = 0;
 		state = State.CLEAR;
 		display.clear();
 	}
 
 }
