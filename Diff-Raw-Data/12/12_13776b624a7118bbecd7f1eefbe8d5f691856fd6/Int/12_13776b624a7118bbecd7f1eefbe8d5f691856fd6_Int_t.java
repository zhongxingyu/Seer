 
 public class Int extends Data
 {
 	private int data;
 	
 	//create an int type with the data, and the length in digits of that data
 	public Int(int data, int length)
 	{
 		this.data = data;
 	}
 	
 	/**
 	 * @return the data
 	 */
 	public int getData()
 	{
 		return data;
 	}
 
 	/**
 	 * @param data the data to set
 	 */
 	public void setData(int data)
 	{
 		this.data = data;
 	}
 
 
 
 	/*
 	 * if the length in digits of the data is longer than the length specified
 	 * return false
 	 * else return true
 	 */
 	public boolean isValid(int length)
 	{
 		String testData = "" + data;
 		return !(testData.length()>length);
 	}
 	
 	public String toString()
 	{
		return data + "";
 	}
 	
 }
