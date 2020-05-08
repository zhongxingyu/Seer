 /**
 * http://leftnode.com/entry/algorithmically-estimating-developer-time
 *
 */
 public class Task
 {
 	private int id;
 	private String name;
 	private String description;
 	private double pointValue;
 	private double estimateHours;
 	private double actualHours;	
 	private String status;
 	static int taskCounter;
 	
 	public Task(int id,String name,String description,double pointValue,double estimateHours,double actualHours,String status)
 	{
 		taskCounter++;
 		this.id = id;
 		this.name = name;
 		this.description = description;
 		this.pointValue = pointValue;
 		this.estimateHours = estimateHours;
 		this.actualHours = actualHours;
 		this.status = status;
 	}
 	
 	public double calculateEfficencyRatio()
 	{
 		return 	this.estimateHours / this.actualHours;
 	}
 	
 	public double calculateDeveloperEffectiveness(double efficiencyRatio)
 	{
 		return efficiencyRatio * this.pointValue;
 	}
 	
 	public void printSummary()
 	{
 		String summary = "";
 		summary += "ID: "+this.id;
 		summary += "\nName: "+this.name;
 		summary += "\ndesciption: "+this.description;
 		summary += "\nPoint Value: "+this.pointValue;
 		summary += "\nEstimated Hours: "+this.estimateHours;
 		summary += "\nActual Hours: "+this.actualHours;
 		summary += "\nStatus: "+this.status;
 		summary += "\nEfficiency Ratio: "+this.calculateEfficencyRatio();
 		summary += "\nDeveloper Effectiveness: "+this.calculateDeveloperEffectiveness(this.calculateEfficencyRatio());
		
 		System.out.println(summary);
 		
 	}
 	
 	public static double calculateGeneralEffieciencyRatio(Task[] tasks)
 	{
 		double sum_er = 0.0;
 		Task t;
 		for(int i = 0; i < taskCounter; i++)
 		{
 			t = tasks[i];//.calculateEfficencyRatio();
 			sum_er += t.calculateEfficencyRatio();
 		}
 		return sum_er/taskCounter;
 	}
 	
 	public static double calculateGeneraldeveloperEffectiveness(Task[] tasks)
 	{
 		double sum_de = 0.0; 
 		Task t;
 		for(int i = 0; i < taskCounter; i++)
 		{
 			t = tasks[i];
 			sum_de += t.calculateDeveloperEffectiveness(t.calculateEfficencyRatio());
 		}
 		
 		return sum_de;
 	}
 	
 	public static double calculateGeneralPointValue(Task[] tasks)
 	{
 		double sum_pv = 0.0; 
 		Task t;
 		for(int i = 0; i < taskCounter; i++)
 		{
 			t = tasks[i];
 			sum_pv += t.pointValue;
 		}
 		
 		return sum_pv;
 	}
 	
 	
 	public void setPV(double pointValue)
 	{
 		this.pointValue = pointValue;	
 	}
 	
 	public double getPV()
 	{
 		return this.pointValue;	
 	}
 	
 	public void setEstimate(double estimateHours)
 	{
 		this.estimateHours = estimateHours;	
 	}
 	
 	
 }
