 package model;
 
 import java.util.GregorianCalendar;
 
 public class Student
 {
 
 	private String id = "";
 	private String password = "";
 
 	private int timeLeft;
 	private int dayCount;
 	
 	private GregorianCalendar lastPlayed;
 
 	private boolean activeStudent;
 
 	public Student(String id, String password)
 	{
 		this.setId(id);
 		this.password = password;
 		this.timeLeft = 1500*60;
 		this.dayCount = 0;
 		
 	}
 
 	public Boolean allowedToPlay()
 	{
 		if(dayCount < 2)
 			return true;
 		return false;
 	}
 
 	public Boolean activeStudent()
 	{
 		// TODO implement method
 		return true;
 	}
 
 	/*******************************************************************************************/
 	/* SETTERS AND GETTERS FOR CLASS Student												   */
 	/*******************************************************************************************/
 
 	public void setLastPlayed()
 	{
 		this.lastPlayed = new GregorianCalendar();
 	}
 	
 	public GregorianCalendar getLastPlayed()
 	{
 		return this.lastPlayed;
 	}
 	
 	public int getTimeLeft()
 	{
 		return timeLeft;
 	}
 
 	public void setTimeLeft(int timeLeft)
 	{
 		this.timeLeft = timeLeft;
 	}
 	
 	public void subtractFromTimeLeft(int newTime) {
 		timeLeft -= newTime;
 	}
 
 	public int getDayCount()
 	{
 		return dayCount;
 	}
 
 	public void setDayCount(int dayCount)
 	{
 		this.lastPlayed = new GregorianCalendar();
 		this.dayCount = dayCount;
 	}
 
 	public boolean isActiveStudent()
 	{
 		return activeStudent;
 	}
 
 	public void setActiveStudent(boolean activeStudent)
 	{
 		this.activeStudent = activeStudent;
 	}
 
 	public String getPassword()
 	{
 		return password;
 	}
 
 	public void setPassword(String password)
 	{
 		this.password = password;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 }
