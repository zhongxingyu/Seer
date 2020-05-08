 import java.util.*; 
 import java.io.*; 
 
 
 public class AssociationRuleTester
 {
 	BufferedReader rule_file; 
 	BufferedReader data_file; 
 	
 	LinkedList<AssociationRule> rules;   // rules from the rule file
 	LinkedList< LinkedList<Integer> > partitions;  // queries in the log file
 	
 	public static void main(String args[])
 	{
 		if(args.length != 2)
 		{
 			System.out.println("USAGE: java AssociationRuleTester <rule file> <log file>"); 
 			System.exit(1); 
 		}
 		
 		AssociationRuleTester tester = new AssociationRuleTester(args[0], args[1]); 
 		
 		tester.readRuleFile(); 
 		tester.readLogFile(); 
 		
 		tester.run(); 
 	}
 	
 	public AssociationRuleTester(String rule_file_name, String data_file_name)
 	{
 		try 
 		{
 			rule_file = new BufferedReader(new FileReader(rule_file_name)); 
 			data_file = new BufferedReader(new FileReader(data_file_name)); 
 			
 			rules = new LinkedList<AssociationRule>(); 
 			
 			partitions = new LinkedList< LinkedList<Integer> >(); 
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage()); 
 		}
 	}
 	
 	/*
 	public double intersectLHS(LinkedList<Integer> previous_query, AssociationRule rule)
 	{
 		int intersect_count = 0; 
 		
 		for(int i = 0; i < previous_query.size(); i++)
 		{
 			if(rule.lhs.contains(previous_query.get(i)))
 			{
 				intersect_count++; 
 			}
 		}
 		
 		return (((double)intersect_count)/previous_query.size()); 
 	}
 	 */
 	
 	public void readRuleFile()
 	{
 		String line; 
 		StringTokenizer tokenizer; 
 		
 		int rhs; 
 		LinkedList<Integer> lhs; 
 		double confidence; 
 		double support; 
 		
 		int num_tokens_lhs; 
 		String token; 
 		
 		try 
 		{
 			while(rule_file.ready())
 			{
 				line = rule_file.readLine(); 
 				tokenizer = new StringTokenizer(line, " "); 
 				lhs = new LinkedList<Integer>(); 
 				
 				rhs = Integer.parseInt(tokenizer.nextToken()); 
 				tokenizer.nextToken(); // skip the <-
 				
 				num_tokens_lhs = tokenizer.countTokens() - 2; // the confidence and support in () will be 2 tokens 
 				
 				// add tokens for left hand side
 				for(int i = 0; i < num_tokens_lhs; i++)
 				{
 					lhs.add(new Integer(Integer.parseInt(tokenizer.nextToken()))); 
 					//System.out.print(lhs.get(i) + " "); 
 				}
 				
 				// System.out.println("---> " + rhs + "\n" + line);
 				
 				token = tokenizer.nextToken();
 				token = token.substring(1, token.length()-1); // chop off '(' in front and ',' in back
 				
 				confidence = Double.parseDouble(token); 
 				
 				token = tokenizer.nextToken(); 
 				token = token.substring(0, token.length()-1); // chop off the ')' in back
 				
 				support = Double.parseDouble(token); 
 				
 				// for simplicity (for now) I am only adding rules with 1 conidtion on the lhs
 				if(lhs.size() == 1)
 					rules.add(new AssociationRule(line, lhs, rhs, confidence, support)); 
 			}
 			rule_file.close();
 		}
 		catch(Exception e)
 		{
 		}
 	}
 	
 	public void readLogFile()
 	{
 		String line; 
 		StringTokenizer tokenizer; 
 		int lines_read = 0; 
 		
 		try 
 		{
 			while((line = data_file.readLine()) != null)
 			{
 				tokenizer = new StringTokenizer(line, " "); 
 				
 				partitions.add(new LinkedList<Integer>()); 
 				
 				while(tokenizer.hasMoreTokens())
 				{
 					partitions.get(lines_read).add(new Integer(Integer.parseInt(tokenizer.nextToken()))); 
 				}
 				lines_read++; 
 			}
 			
 			System.out.println("\n...read " + lines_read + " queries\n");
 		}
 		catch(Exception e)
 		{
 			System.out.println(e.getMessage()); 
 		}
 	}
 	
 	public HashMap<Integer, Integer> predictNextPartitions(LinkedList<Integer> current_query)
 	{
 		//LinkedList<Integer> predicted_partitions = new LinkedList<Integer>();
 		HashMap<Integer, Integer> predicted_partitions = new HashMap<Integer, Integer>();
 		Integer value; 
 		
 		for(int i = 0; i < rules.size(); i++)
 		{
 			if(current_query.contains(rules.get(i).lhs.get(0)))
 			{
 				value = predicted_partitions.get(rules.get(i).rhs); 
 				
 				if(value == null) // this partition hasn't been predicted yet, so add it to hash table
 				{
 					predicted_partitions.put(rules.get(i).rhs, new Integer(1)); 
 				}
 				else  // increment the count of this partition
 				{
					value++; 
 				}
 				
 				//System.out.println("...predicting partition " + rules.get(i).rhs); 
 			}
 		}
 		return predicted_partitions; 
 	}
 	
 	public void run()
 	{
 		double intersection; 
 		LinkedList<Integer> current_query; 
 		LinkedList<Integer> next_query; 
 		HashMap<Integer, Integer> predicted_partitions; 
 		
 		for(int i = 1; i < partitions.size(); i++) // iterate through each query
 		{
 			current_query = partitions.get(i-1); 
 			next_query = partitions.get(i); 
 			
 			predicted_partitions = predictNextPartitions(current_query); 
 			
 			System.out.print("next query: "); 
 			for(int j = 0; j < next_query.size(); j++)
 			{
				System.out.print(next_query.get(j) + " "); 
 			}
 			System.out.println(); 
 			
 			Iterator it = predicted_partitions.entrySet().iterator(); // iterate through the hashmap
 			
 			System.out.print("predicted partitions <partition_id, frequency>: "); 
 			while(it.hasNext())
 			{
 				Map.Entry entry = (Map.Entry)it.next(); 
 				System.out.print("<" + entry.getKey() + ", " + entry.getValue() + "> "); 
 			}
 			System.out.println(); 

 		}
 	}
 }
 
 class AssociationRule
 {
 	String rule; 
 	List<Integer> lhs;
 	Integer rhs; 
 	
 	double confidence; 
 	double support; 
 	
 	public AssociationRule(String rule_, List<Integer> lhs_, Integer rhs_, double confidence_, double support_)
 	{
 		rule = rule_; 
 		lhs = lhs_; 
 		rhs = rhs_; 
 		confidence = confidence_; 
 		support = support_; 
 	}
 	
 }
