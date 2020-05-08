 package GPFinalProject;
 
 public class GPNodeOperator extends GPNode 
 {
 
 	protected GPNode m_left;
 	protected GPNode m_right;
 	protected String m_operator;
 	
 	public GPNodeOperator(String operator, GPNode left, GPNode right) {
 		// TODO Auto-generated constructor stub
 		m_operator = operator;
 		m_left = left;
 		m_right = right;
 	}
 	
     public double EvaluateFitnessValue(double x)
     {
         double leftValue = m_left.EvaluateFitnessValue(x);
         double rightValue = m_right.EvaluateFitnessValue(x);
         double output = Double.MAX_VALUE;
         if (m_operator.equals("+"))
         {
         	output = leftValue + rightValue;
         }
         
         if (m_operator.equals("-"))
         {
         	output = leftValue - rightValue;
         }
         
         if (m_operator.equals("*"))
         {
         	output = leftValue * rightValue;
         }
         
         if (m_operator.equals("/"))
         {
         	if (rightValue != 0.0)
         	{
         		output = leftValue / rightValue;
         	}
         }
         
         return output;
     }
     
     public int GetGPDepth()
     {
     	int leftDepth = m_left.GetGPDepth();
     	int rightDepth = m_right.GetGPDepth();
     	
    	if (leftDepth < rightDepth)
     	{
     		return leftDepth + 1;
     	}
     	else
     	{
     		return rightDepth + 1;
     	}
     }
     
     public int GetGPNodeCount()
     {
     	return m_left.GetGPNodeCount() + m_right.GetGPNodeCount() + 1;
     }
     
     public String GetGPString()
     {
         return m_left.GetGPString() + " " + m_operator + " " + m_right.GetGPString();
     }	
 }
