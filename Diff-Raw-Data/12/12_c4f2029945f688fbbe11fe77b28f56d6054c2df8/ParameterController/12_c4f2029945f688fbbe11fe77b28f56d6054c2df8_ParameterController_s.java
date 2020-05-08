 package gui.controller;
 
 import interpreter.AssertionFailureException;
 
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 
 import ast.ArrayType;
 import ast.BooleanType;
 import ast.FunctionParameter;
 import ast.IntegerType;
 import ast.Type;
 
 import misc.ExecutionHandler;
 import gui.MiscConsole;
 import gui.ParameterFrame;
 import gui.RandomTestFrame;
 
 public class ParameterController implements SelectionListener {
 	private ParameterFrame parameterframe;
 	private RandomTestFrame randomtestframe;
 	private MiscConsole console;
 	private ExecutionHandler executionHandler;
 	
 	public ParameterController(ExecutionHandler executionHandler, MiscConsole console) {
 		this.console = console;
 		this.executionHandler = executionHandler;
 	}
 
 	public void addParameterFrame(ParameterFrame parameterframe) {
 		if (this.executionHandler.getAST() != null && this.executionHandler.getAST().getMainFunction().getParameters() != null) {
 			this.parameterframe = parameterframe;
 			this.parameterframe.createFrame(this.executionHandler.getAST().getMainFunction().getParameters());
 			this.parameterframe.getOkButton().addSelectionListener(this);
 		}		
 	}
 	
 	public void addRandomTestFrame(RandomTestFrame randomtestframe, String source) {
 		this.randomtestframe = randomtestframe;
 		this.executionHandler.parse(source);
 		if (this.executionHandler.getAST() == null) {
 			this.randomtestframe.createEmptyFrame("Please make sure that the syntax is correct!");
 		}
 		else if (this.executionHandler.getAST().getMainFunction().getParameters() == null
 				|| this.executionHandler.getAST().getMainFunction().getParameters().length == 0) {
 			this.randomtestframe.createEmptyFrame("Main function does not have parameters!");
 			this.executionHandler.setAST(null);
 		}
 		else {
 			this.randomtestframe.createFrame(this.executionHandler.getAST().getMainFunction().getParameters()); 
 			this.randomtestframe.getStartButton().addSelectionListener(this);
 		}
 	}
 	
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (this.parameterframe != null && e.getSource() == this.parameterframe.getOkButton()) {
 			String[] parameterValues = new String[this.parameterframe.getValues().length];
 			for (int i = 0; i < this.parameterframe.getValues().length; i++) {
 				parameterValues[i] = "";
 				parameterValues[i] += this.parameterframe.getValues()[i].getText();
 			}
 			this.executionHandler.setParameterValues(parameterValues);
 			this.parameterframe.getShell().dispose();
 		}
 		else if (this.randomtestframe != null && e.getSource() == this.randomtestframe.getStartButton()) {			
 			String s = this.getRandomtestframe().getCount().getText();
 			int count;
 			try {
 				count = Integer.parseInt(s);
 			}
 			catch (NumberFormatException ne) {
 				count = 0;
 			}
 			String[] result = new String[3];
 			for (int i = 0; i < count; i++) {
 				FunctionParameter[] parameters = this.executionHandler.getAST().getMainFunction().getParameters();
 				String[] parameterValues = this.createRandomTestValues(parameters);
 				result[0] = "" + (i + 1);
 				result[1] = "";
 				for (int j = 0; j < parameterValues.length; j++) {
 					result[1] = result[1] + parameters[j].getName().toString() + " = " 
 					+ parameterValues[j] + "; ";
 				}
 				this.executionHandler.setParameterValues(parameterValues);
 				try {
 					this.executionHandler.run(null, null);
 					result[2] = "success";
 				}
 				catch (AssertionFailureException ae) {
 					result[2] = ae.getMessage() + " (line " + ae.getPosition() + ")";
 				}
 				this.console.printRandomTestResult(result);
 			}
 			this.executionHandler.setAST(null);
 			this.randomtestframe.getShell().dispose();
 		}
 	}
 
 	private String[] createRandomTestValues(FunctionParameter[] parameters) {
 		String[] parameterValues = new String[parameters.length];
 		for (int i = 0; i < parameters.length; i++) {
 			if (parameters[i].getType() instanceof IntegerType) {
 				String beginString = this.getRandomtestframe().getIntervals()[i][0].getText();
 				String endString = this.getRandomtestframe().getIntervals()[i][1].getText();
 				int begin;
 				int end;
 				int random;
 				try {
 					begin = Integer.parseInt(beginString);
 					end = Integer.parseInt(endString);
 					if (begin < end) {
 						random = this.createRandomIntegerValue(begin, end);
 					}
 					else {
 						random = 0;
 					}
 				}
 				catch (NumberFormatException ne) {
 					random = 0;
 				}
 				parameterValues[i] = random + "";
 			}
 			else if (parameters[i].getType() instanceof BooleanType) {
 				parameterValues[i] = this.createRandomBooleanValue() + "";
 			}
 			else if (parameters[i].getType() instanceof ArrayType) {
 				String sizeString = this.getRandomtestframe().getIntervals()[i][2].getText();
 				int size;
 				try {
 					size = Integer.parseInt(sizeString);
 				}
 				catch (NumberFormatException ne) {
 					size = 1;
 				}
 				Type tmp = parameters[i].getType();
 				int dimension = 0;
 				while (tmp instanceof ArrayType) {
 					tmp = ((ArrayType) tmp).getType();
 					dimension++;
 				}
 				String value = "";
 				if (tmp instanceof IntegerType) {
 					String beginString = this.getRandomtestframe().getIntervals()[i][0].getText();
 					String endString = this.getRandomtestframe().getIntervals()[i][1].getText();
 					int begin;
 					int end;
 					try {
 						begin = Integer.parseInt(beginString);
 						end = Integer.parseInt(endString);
 						if (begin < end) {
 							for (int j = 0; j < Math.pow(size, dimension - 1); j++) {
 								for (int k = 1; k < dimension; k++) {
 									if (j % Math.pow(size, k) == 0) {
 										value += "{";
 									}
 								}
 								value += this.createRandomIntegerArray(begin, end, size);
 								for (int l = 1; l < dimension; l++) {
 									if (j % Math.pow(size, l) == Math.pow(size, l) - 1) {
 										value += "}";
 									}
 									else {
 										value += ", ";
 										l = dimension;
 									}
 								}
 							}
 						}
 						else {
 							value = "0";
 							for (int a = 0; a < size; a++) {
 								value = "{" + value +"}";
 							}
 						}
 					}
 					catch (NumberFormatException ne) {
 						value = "0";
 						for (int a = 0; a < size; a++) {
 							value = "{" + value +"}";
 						}
 					}
 				}
 				else if (tmp instanceof BooleanType) {
 					for (int j = 0; j < Math.pow(size, dimension - 1); j++) {
 						for (int k = 1; k < dimension; k++) {
 							if (j % Math.pow(size, k) == 0) {
 								value += "{";
 							}
 						}
 						value += this.createRandomBooleanArray(size);
 						for (int l = 1; l < dimension; l++) {
 							if (j % Math.pow(size, l) == Math.pow(size, l) - 1) {
 								value += "}";
 							}
 							else {
 								value += ", ";
 								l = dimension;
 							}
 						}
 					}
 				}
 				parameterValues[i] = value;
 			}
 		}
 		return parameterValues;
 	}
 		
 	
 	private int createRandomIntegerValue(int begin, int end) {
 		return (int) (Math.random() * (end - begin + 1)) + begin;
 	}
 	
 	private boolean createRandomBooleanValue() {
 		float random = (float) Math.random();
 		return random > 0.5; 
 	}
 	
 	private String createRandomIntegerArray(int begin, int end, int size) {
 		String random = "";
 		for (int i = 0; i < size; i++) {
 			if (i == size - 1) {
 				random += this.createRandomIntegerValue(begin, end);
 			}
 			else {
 				random += this.createRandomIntegerValue(begin, end) + ", ";
 			}
 		}
 		random = "{" + random + "}";
 		return random;
 	}
 	
 	private String createRandomBooleanArray(int size) {
 		String random = "";
 		for (int i = 0; i < size; i++) {
 			if (i == size - 1) {
 				random += this.createRandomBooleanValue();
 			}
 			else {
 				random += this.createRandomBooleanValue() + ", ";
 			}
 		}
 		random = "{" + random + "}";
 		return random;
 	}
 
 	public ParameterFrame getParameterframe() {
 		return this.parameterframe;
 	}
 	
 	public RandomTestFrame getRandomtestframe() {
 		return this.randomtestframe;
 	}
 	
 	@Override
 	public void widgetDefaultSelected(SelectionEvent arg0) {
 		// TODO Auto-generated method stub	
 	}
 }
