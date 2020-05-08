 package algorithm;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.UUID;
 
 import crowdtrust.AnnotationType;
 import crowdtrust.BinaryAccuracy;
 import crowdtrust.BinarySubTask;
 import crowdtrust.InputType;
 import crowdtrust.MediaType;
 
 import db.CrowdDb;
 import db.DbInitialiser;
 import db.LoginDb;
 import db.RegisterDb;
 import db.SubTaskDb;
 import db.TaskDb;
 import junit.framework.TestCase;
 
 public class TestAlgorithmNew extends TestCase {
 	protected static int numTasks = 9;
 	protected static int numPeople = 10;
 	protected static AnnotatorModel[] annotators;
 	
 	public TestAlgorithmNew(String name){
 		super(name);
 	}
 	
 	public void testAlgorithmNew(){
 		System.setProperty("test", "true");
 		boolean labs = false;
 		if(labs){
 			//Clean the database 
 			DbInitialiser.init();
 			
 			//Lets create some annotators with id's 1 - numPeople and place them in array
 			annotators = new AnnotatorModel[numPeople];
 			for(int i = 0; i < numPeople; i++){
 				String uuid = UUID.randomUUID().toString();
 				uuid = uuid.replace("-", "");
 				uuid = uuid.substring(0, 12);
 				annotators[i] = new AnnotatorModel(uuid, uuid);
 			}
 			
 			//Set up the annotators so they can answer binary question
 			Random rand = new Random();
 			for(int i = 0; i < numPeople; i++){
 				//int truePos = rand.nextInt(999) + 1;	
 				//int trueNeg = rand.nextInt(999) + 1;
 				//annotators[i].setUpBinary(truePos, trueNeg, totalPos, totalNeg);
 				double percentageNormal = 0.75;
 				if(rand.nextDouble() > percentageNormal){
 					annotators[i].setUpBinary(500, 500, 1000, 1000);
 				}else{
 					annotators[i].setUpBinary(850, 850, 1000, 1000);
 				}
 				
 			}
 			
 			//Create and print their rates and names
 			for(int i = 0; i < numPeople; i++){
 				RegisterDb.addUser("test@test.com", annotators[i].getUsername(), annotators[i].getPassword(), true);
 				annotators[i].setId(LoginDb.checkUserDetails(annotators[i].getUsername(), annotators[i].getPassword()));
 				AnnotatorModel a = annotators[i];
 				System.out.println("annotator " + 
 						a.bee.getId() + 
 						" truePosRate =" + a.binary.truePosRate +
 						" trueNegRate =" + a.binary.trueNegRate);
 			}
 			
 			//Lets make a client
 			RegisterDb.addUser("testClient@test.com", "gio", "gio", false);
 			int accountId = LoginDb.checkUserDetails("gio", "gio");
 			//Lets add a binary task to the database
 			long expiry = getDate();
 			float accuracy = (float)0.7;
 			List<String> testQs = new LinkedList<String>();
 			testQs.add("test q1");
 			testQs.add("test q2");
 			assertTrue(TaskDb.addTask(accountId,"BinaryTestTask", "This is a test?", accuracy, MediaType.IMAGE, AnnotationType.BINARY, InputType.RADIO, numPeople , expiry, testQs, 0, 0, 0)>0);
 			
 			//List of answers
 			LinkedList<AnnotatorSubTaskAnswer> answers = new LinkedList<AnnotatorSubTaskAnswer>();
 			System.out.println("About to get Task id");
 			System.out.println("John Task Id: " + TaskDb.getTaskId("BinaryTestTask"));
 			System.out.println("Got it");
 			
 			//Lets create a linked list of subTasks
 			for(int i = 0; i < numTasks; i++){
 				String uuid = UUID.randomUUID().toString();
 				uuid = uuid.replace("-", "");
 				uuid = uuid.substring(0, 12);
 				SubTaskDb.addSubtask(uuid, TaskDb.getTaskId("BinaryTestTask"));
 				int id = SubTaskDb.getSubTaskId(uuid);
 				System.out.println("Subtask Id: " + id);
 				BinarySubTask bst = new BinarySubTask(id,0.7,0, numPeople);
 				AnnotatorSubTaskAnswer asta = new AnnotatorSubTaskAnswer(bst.getId(), bst, new BinaryTestData(rand.nextInt(2)));
 				answers.add(asta);
 			}
 			
 			//Give all the annotators the answers
 			for(int i = 0; i < numPeople; i++){
 				annotators[i].setTasks(answers);
 			}
 			System.out.println("Given annotators answers");
 			
 			BinarySubTask t;
 			for(int i = 0; i < numTasks; i++){
 				for(int j = 0; j < numPeople; j++){
 					System.out.println("Person " + (j + 1) + " answering task " + i);
 				    t = (BinarySubTask) SubTaskDb.getSequentialSubTask(TaskDb.getTaskId("BinaryTestTask"), annotators[j].bee.getId());
 				    System.out.println("Sending in task " + t.getId());
 					annotators[j].answerTask(t);
 				}
 				System.out.println();
 				System.out.println("Task " + i + " done.");
 				printAnnotators();
 			}
 		
 		
 		}
 	}
 	
 	protected void printAnnotators(){
 		System.out.println("----------Calculating Annotator Rates-----------------");
 		System.out.println("Id |    TPR    |    TNR    |    TPRE    |    TNRE    ");
 			for(int i = 0; i < numPeople; i++){
 				AnnotatorModel annotator = annotators[i];
 				System.out.print(annotator.getBee().getId() +" | " + annotator.getBinaryBehaviour().getTruePosRate() + " | " + annotator.getBinaryBehaviour().getTrueNegRate() + " | " );
 				BinaryAccuracy binAccuracy = CrowdDb.getBinaryAccuracy(annotator.getBee().getId());
 				System.out.print(binAccuracy.getTruePositive() +" | "+ binAccuracy.getTrueNegative());
 				System.out.println("");
 			}
 		System.out.println("------------------------------------------------------");
 	}
 	
 	protected long getDate(){
 		long ret        = 0           ;
 		String str_date = "11-June-15";
 	    DateFormat formatter ; 
 	    Date date ; 
 	    formatter = new SimpleDateFormat("dd-MMM-yy");
 		try {
 			date = (Date)formatter.parse(str_date);
 			ret = date.getTime();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} 
 		return ret;
 	}
 }
