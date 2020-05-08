 package pt.com.broker.performance.distributed;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
 import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class TestResult
 {	
 	private static final Logger log = LoggerFactory.getLogger(TestResult.class);
 	
 	private ActorType actorType;
 	private String actorName;
 	private String testName;
 	private int messages;
 	private long startTime;
 	private long stopTime;
 
 
 	public enum ActorType{ Procucer, Consumer};
 	
 	
 	private TestResult()
 	{
 		
 	}
 	
 	public TestResult(ActorType actorType, String actorName, String testName, int messages, long startTime, long stopTime)
 	{
 		this.actorType = actorType;
 		this.actorName = actorName;
 		this.testName = testName;
 		this.messages = messages;
 		this.startTime = startTime;
 		this.stopTime = stopTime;
 	}
 	
 	public TestResult(ActorType actorType, String actorName, String testName)
 	{
 		this.actorType = actorType;
 		this.actorName = actorName;
 		this.testName = testName;
 		this.messages = -1;
 		this.startTime = 0;
 		this.stopTime = 0;
 	}
 
 	public ActorType getActorType()
 	{
 		return actorType;
 	}
 
 	public String getActorName()
 	{
 		return actorName;
 	}
 
 	public String getTestName()
 	{
 		return testName;
 	}
 
 	public int getMessages()
 	{
 		return messages;
 	}
	
	public void setMessages(int messages)
	{
		this.messages = messages;
	}
 
 	public long getStartTime()
 	{
 		return startTime;
 	}
 	
 	public void setStartTime(long startTime)
 	{
 		this.startTime = startTime;
 	}
 	
 	public double getStopTime()
 	{
 		return stopTime;
 	}
 	
 	public void setStopTime(long stopTime)
 	{
 		this.stopTime = stopTime;
 	}
 	
 	public byte[] serialize()
 	{
 		byte[] data = null;
 		try
 		{
 			UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
 			ObjectOutputStream outputObj = new ObjectOutputStream(bout);
 			outputObj.writeUTF(testName);
 			outputObj.writeUTF(actorName);
 			outputObj.writeUTF(actorType.toString());
 			outputObj.writeInt(messages);
 			outputObj.writeLong(startTime);
 			outputObj.writeLong(stopTime);
 			
 			outputObj.flush();
 
 			data = bout.toByteArray();			
 		}
 		catch (IOException e)
 		{
 			log.error("Failed to serialize object", e);
 		}
 		return data;
 	}
 	
 	public static TestResult deserialize(byte[] data)
 	{
 		TestResult testResult = new TestResult();
 		
 		try
 		{
 			ObjectInputStream inputObj;
 			inputObj = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));
 			
 			testResult.testName = inputObj.readUTF();
 			testResult.actorName = inputObj.readUTF();
 			testResult.actorType = ActorType.valueOf( inputObj.readUTF() );
 			testResult.messages = inputObj.readInt();
 			testResult.startTime = inputObj.readLong();
 			testResult.stopTime = inputObj.readLong();
 		}
 		catch (Throwable e)
 		{
 			log.error("Failed to deserialize object", e);
 			return null;
 		}
 		
 		return testResult;
 	}
 }
 
