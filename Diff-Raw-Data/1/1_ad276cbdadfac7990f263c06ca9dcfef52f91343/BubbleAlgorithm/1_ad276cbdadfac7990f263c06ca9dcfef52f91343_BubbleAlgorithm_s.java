 package edu.ucsc.cs.mturk.lib.topone;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 
 import com.amazonaws.mturk.service.axis.RequesterService;
 import com.amazonaws.mturk.util.PropertiesClientConfig;
 import com.amazonaws.mturk.requester.HIT;
 import com.amazonaws.mturk.requester.HITStatus;
 
 /**
  * <p>This class is the implementation of bubble algorithm. It simulates the 
  * process of the bubble algorithm. Users build an object of the bubble 
  * algorithm through this class, put questions into this object, and 
  * the object takes care the rest job. Finally, it returns the final answer 
  * of this running instance.</p>
  * 
  * <p>Since some operations, such as creating a HIT and getting answers from 
  * workers etc., should be customized by library users, we use callback 
  * routine to make it. Therefore, library users need to implement <i><b>
  * MyHit</b></i> interface, build an instance of the class and use this instance 
  * as a parameter in the constructor.</p>
  * 
  * Also, generally there are two ways to enable the library to have the access to 
  * your Amazon Mechanical Turk account. The first one is to pass <tt>service</tt> 
  * of type <tt>import com.amazonaws.mturk.service.axis.RequesterService</tt> 
  * into the constructor. The second one is to locate your Amazon Mechanical 
  * Turk property file, which stores your <tt>access_key</tt>, <tt>secret_key</tt> and 
  * <tt>service_url</tt>, in the same directory of the source file and to pass the 
  * file name as a parameter in the constructor.
  * 
  * <p>Finally, an instance of this class should be created in a Builder 
  * pattern. To create an instance of the algorithm, a static inner class 
  * Builder may be used like this:<br/>
  * <pre>BubbleAlgorithm bubble = new BubbleAlgorithm.Builder(questions, myHit).
  * propertyFile("mturk.properties").inputSize(6).outputSize(2).
  * numberOfAssignments(5).isLogged(true).logName("Log.txt").
  * jobId("E9FG6X9LO");</pre> <br />
  * Note that you do not need to configure all the parameters. You can choose 
  * to configure only what you need. However, questions and myHit are still 
  * required.
  * </p>
  * 
  * @author Kerui Huang
  * @version 1.1
  *
  */
 public class BubbleAlgorithm {
     
     /* The inputs and output of the algorithm*/
     private final RequesterService service;	//MTurk service
     private final MyHit myHit;	// the object for callback
     private final ArrayList<Object> questions;
     private final int nInput;		// number of outputs of a HIT
     private int nOutput;	// number of outputs of a HIT
     private final int nAssignment;	// number of assignments of a normal HIT
     private final int nTieAssignment;	// number of assignments of a tie-solving HIT 
     private final boolean isShuffled;	// shuffle the inputs
     private final boolean isLogged;	// whether generate log automatically
     private final String logName;	// the name of the log file
     private final String jobId;	// programmers can assign an ID to this job.
     
     private boolean isDone;
     private Object finalAnswer;
     
     public static class Builder {
 	
 	// Required parameters.
 	private final MyHit myHit;
 	private final ArrayList<Object> questions;
 	
 	// Optional parameters.
 	private RequesterService service = new RequesterService(
 		new PropertiesClientConfig(
 			System.getProperty("user.dir") + 
 			java.io.File.separator + 
 			"mturk.properties"));
 	private int nInput = 6;
 	private int nOutput = 2;
 	private int nAssignment = 5;
 	private int nTieAssignment = 1;
 	private boolean isShuffled = true;
 	private boolean isLogged = true;
 	private String logName = "Bubble_Algorithm_" + new Date().hashCode() + ".txt";;
 	private String jobId = null;
 	
 	/**
 	 * Constructs a Builder with questions and myHit.
 	 * @param questions the questions to be solved by workers.
 	 * @param myHit the object which implements <i>MyHit</i> interface and 
 	 * provides callback functions.
 	 */
 	public Builder(ArrayList<Object> questions, MyHit myHit) {
 	    this.questions = questions;
 	    this.myHit = myHit;
 	}
 	
 	/**
 	 * Set the service object of the library user.
 	 * @param service the service object of the library user.
 	 * @return the Builder object itself.
 	 */
 	public Builder service(RequesterService service) {
 	    this.service = service;
 	    return this;
 	}
 	
 	/**
 	 * Set the name of the the name of the MTurk property file.
 	 * @param propertyFileName the name of the property file.
 	 * @return the Builder object itself.
 	 */
 	public Builder propertyFile(String propertyFileName) {
 	    this.service  =  new RequesterService(
 		    new PropertiesClientConfig(
 			    System.getProperty("user.dir") + 
 			    java.io.File.separator + 
 			    propertyFileName));
 	    return this;
 	}
 	
 	/**
 	 * Set the number of inputs of a HIT.
 	 * @param inputSize the number of inputs of a HIT.
 	 * @return the Builder object itself.
 	 */
 	public Builder inputSize(int inputSize) {
 	    this.nInput = inputSize;
 	    return this;
 	}
 	
 	/**
 	 * Set the number of outputs of a HIT.
 	 * @param outputSize the number of outputs of a HIT.
 	 * @return the Builder object itself.
 	 */
 	public Builder outputSize(int outputSize) {
 	    this.nOutput = outputSize;
 	    return this;
 	}
 	
 	/**
 	 * Set the number of assignments for a normal HIT.
 	 * @param numberOfAssignments the number of assignments for a normal 
 	 * HIT.
 	 * @return the Builder object itself.
 	 */
 	public Builder numberOfAssignments(int numberOfAssignments) {
 	    this.nAssignment = numberOfAssignments;
 	    return this;
 	}
 	
 	/**
 	 * Set the number of assignments for a tie-solving HIT.
 	 * @param numberOfTieAssignments the number of assignments for a 
 	 * tie-solving HIT.
 	 * @return the Builder object itself.
 	 */
 	public Builder numberOfTieAssignments(int numberOfTieAssignments) {
 	    this.nTieAssignment = numberOfTieAssignments;
 	    return this;
 	}
 	
 	/**
 	 * Set whether the inputs are shuffled by the algorithm.
 	 * @param isShuffled <tt>true</tt> if the inputs are shuffled by the algorithm.
 	 * @return the Builder object itself.
 	 */
 	public Builder isShuffled(boolean isShuffled) {
 	    this.isShuffled = isShuffled;
 	    return this;
 	}
 	
 	/**
 	 * Set whether the algorithm records the computation process.
 	 * @param isLogged <tt>true</tt> if the algorithm records the computation 
 	 * process.
 	 * @return the Builder object itself.
 	 */
 	public Builder isLogged(boolean isLogged) {
 	    this.isLogged = isLogged;
 	    return this;
 	}
 	
 	/**
 	 * Set the name of the log file.
 	 * @param logName the name of the log file.
 	 * @return the Builder object itself.
 	 */
 	public Builder logName(String logName) {
 	    this.logName = logName;
 	    return this;
 	}
 	
 	/**
 	 * Set the job ID for this algorithm instance.
 	 * @param jobId the job ID for this algorithm instance.
 	 * @return the Builder object itself.
 	 */
 	public Builder jobId(String jobId) {
 	    this.jobId = jobId;
 	    return this;
 	}
 	
 	/**
 	 * Create a new instance of TreeAlgorithm.
 	 * @return the newly created instance of TreeAlgorithm.
 	 */
 	public BubbleAlgorithm build() {
 	    validateInitialization(questions, nInput, nOutput, 
 		    nAssignment, nTieAssignment);
 	    return new BubbleAlgorithm(this);
 	}
 	
 	/*
 	 * This function validates the values of parameters input by library users.
 	 */
 	private void validateInitialization(ArrayList<Object> questions,
 		int numberOfInputs,int numberOfOutputs, 
 		int numberOfAssignments, int numberOfTieAssignments) {
 	    if (questions.size() == 0) {
 		throw new BubbleAlgorithmException("The size of questions is 0." +
 			" [questions.size() == 0]");
 	    }
 	    if (questions.size() < numberOfInputs) {
 		throw new BubbleAlgorithmException("The size of questions is" +
 			" less than the number of inputs of a HIT." +
 			" [questions.size() < numberOfInputs]");
 	    }
 	    if (questions.size() < numberOfOutputs) {
 		throw new BubbleAlgorithmException("The size of questions is" +
 			" less than the number of outputs of a HIT." +
 			" [questions.size() < numberOfOutputs]");
 	    }
 	    if (numberOfInputs < numberOfOutputs) {
 		throw new BubbleAlgorithmException("The number of inputs of a HIT" +
 			" is less than the number of outputs of a HIT." +
 			" [numberOfInputs < numberOfOutputs]");
 	    }
 	    if (numberOfInputs < 0) {
 		throw new BubbleAlgorithmException("The number of inputs of a HIT" +
 			" is negative." +
 			" [numberOfInputs < 0]");
 	    }
 	    if (numberOfOutputs < 0) {
 		throw new BubbleAlgorithmException("The number of outputs of a HIT" +
 			" is negative." +
 			" [numberOfOutputs < 0]");
 	    }
 	    if (numberOfAssignments < 1) {
 		throw new BubbleAlgorithmException("The number of assignments of " +
 			"a normal HIT is less than 1." +
 			" [numberOfAssignments < 1]");
 	    }
 	    if (numberOfTieAssignments < 1) {
 		throw new BubbleAlgorithmException("The number of assignments of " +
 			"a tie-solving HIT is less than 1." +
 			" [numberOfTieAssignments < 1]");
 	    }
 	}
     }
     
     private BubbleAlgorithm(Builder builder) {
 	this.questions = builder.questions;
 	this.myHit = builder.myHit;
 	this.service = builder.service;
 	this.nInput = builder.nInput;
 	this.nOutput = builder.nOutput;
 	this.nAssignment = builder.nAssignment;
 	this.nTieAssignment = builder.nTieAssignment;
 	this.isShuffled = builder.isShuffled;
 	this.isLogged = builder.isLogged;
 	this.logName = builder.logName;
 	this.jobId = builder.jobId;
 	this.isDone = false;
 	this.finalAnswer = null;
     }
     
     public void start() {
 	if (isShuffled) {
 	    Collections.shuffle(questions);
 	}
 	
 	if (isLogged) {
 	    String log = "";
 	    
 	    LogWriter.createOrResetLog(logName);
 	    log += "The bubble algorithm started at " + new Date().toString() + "\n\n";
 	    log += "The Table of Parameters \n" + 
 	           "+-----------------------------------------------+-------------------------------+ \n" +  
 		   "| Number of Inputs of a Normal HIT              | " + nInput + "\n" + 
 		   "+-----------------------------------------------+-------------------------------+ \n" +
 		   "| Number of Outputs of a Normal HIT             | " + nOutput + "\n" + 
 		   "+-----------------------------------------------+-------------------------------+ \n" +
 		   "| Number of Assignments of a Normal HIT         | " + nAssignment + "\n" + 
 		   "+-----------------------------------------------+-------------------------------+ \n" +
 		   "| Number of Assignments of a Tie-Solving HIT    | " + nTieAssignment + "\n" +
 		   "+-----------------------------------------------+-------------------------------+ \n" +
 		   "| Inputs Are Shuffled                           | " + isShuffled + "\n" + 
 		   "+-----------------------------------------------+-------------------------------+ \n" +
 		   "| Generate Automatic Log                        | " + isLogged + "\n" +
 		   "+-----------------------------------------------+-------------------------------+ \n" +
 		   "| The Name of the Automatic Log                 | " + logName + "\n" +
 		   "+-----------------------------------------------+-------------------------------+ \n" +
 		   "| Job ID                                        | " + jobId + "\n" + 
 		   "+-----------------------------------------------+-------------------------------+ \n\n";
 	    LogWriter.writeLog(log, logName);
 	    System.out.println(log);
 	}
 	
 	while (questions.size() > 1) {
 	    ArrayList<Object> answers = new ArrayList<Object>();
 	    ArrayList<Object> inputs = new ArrayList<Object>();
 	    
 	    if (questions.size() >= nInput) {
 		for (int i = 0; i < nInput; i++) {
 		    inputs.add(questions.get(0));
 		    questions.remove(0);
 		}
 	    } else {
 		nOutput = 1;
 		int questionSize = questions.size();
 		for (int i = 0; i < questionSize; i++) {
 		    inputs.add(questions.get(0));
 		    questions.remove(0);
 		}
 	    }
 	    
 	    
 	    // Create a new HIT.
 	    String hitId = myHit.createMyHit(service, inputs, nOutput, nAssignment);
 	    
 	    if (isLogged) {
 		LogWriter.writeBubbleCreateHitLog(service, hitId, nAssignment, nOutput, inputs, logName);
 	    }
 	    
 	    // Keep waiting and checking the status of this new HIT, until it is done.
 	    HIT hit = service.getHIT(hitId);
 	    while (hit.getHITStatus() != HITStatus.Reviewable) {
 		try {
 		    Thread.sleep(1000*5);
 		} catch (InterruptedException e) {
 		    e.printStackTrace();
 		}
 		hit = service.getHIT(hitId);
 	    }
 	    
 	    // Retrieve the answers of the HIT.
 	    answers =  AnswerProcessor.refineRawAnswers(
 		    		myHit.getMyHitAnswers(service, hitId), 
 		    		nOutput, service, myHit, nTieAssignment, 
 		    		isLogged, logName, AnswerProcessor.BUBBLE_ALGORITHM);
 	    
 	    if (isLogged) {
 		LogWriter.writeGetAnswerLog(hitId, answers, logName);
 	    }
 	    
 	    // Put the new answers into the questions queue.
 	    for (int i = answers.size() -1; i >= 0; i--) {
 		questions.add(0, answers.get(i));
 	    }
 	    
 	    // Deal with the this already used HIT.
 	    myHit.dumpPastHit(service, hitId);
 	}
 	
 	finalAnswer = questions.get(0);
 	
 	String info = "The bubble algorithm ended at " + 
     		new Date().toString() + "\n\n";;
 	info = "The final answer is: " + finalAnswer + "\n";
 	LogWriter.writeLog(info, logName);
 	System.out.println(info);
     }
     
     /**
      * Indicate whether this instance is done.
      * 
      * @return <tt>true</tt> if the program is done.
      */
     public boolean isDone() {
 	return isDone;
     }
     
     /**
      * Return the final answer.
      * 
      * @return The final answer.
      */
     public Object getFinalAnswer() {
 	return finalAnswer;
     }
 }
