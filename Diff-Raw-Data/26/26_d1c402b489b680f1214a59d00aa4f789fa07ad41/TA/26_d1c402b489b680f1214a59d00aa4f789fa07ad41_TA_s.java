 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 public class TA {
 	
 	TF tf; //data structure containing all tf scores for all words according to websites
 	graph g; //graph created from websites
 	Vector<String> words; //specified words to determine relevance
 	int flag; //indicates the type of the operand - '0' for OR, '1' for AND
 	
 	public TA(TF tf, graph g, Vector<String> words,int flag)
 	{
 		this.tf = tf;
 		this.g = g;
 		this.words = words;
 		Collections.sort(words);
 		this.flag = flag;
 	}
 	
 	public void run()
 	{
 		Comparator<stringScore> comparator = Collections.reverseOrder();
 		Vector<wordWebTf> wwfVec = new Vector<wordWebTf>();   //Vector that contains objects representing words with a list of their TF values in each of the documents
 		// build the table
 		for(int i=0;i<words.size();i++)
 		{
 			String word = words.get(i).toLowerCase();   //change to lowercase to guarantee case insensitivity
 			wordWebTf wwf = new wordWebTf(word);
 			String website;
 			Double tfScoreOfWord;
 			for(Map.Entry<String,HashMap<String,Double>> entry : tf.getDocstf().entrySet()) //iterate over tf scores for each website
 			{
 				HashMap<String,Double> tfMap = entry.getValue();
 				website = entry.getKey();
 				tfScoreOfWord = tfMap.get(word); //get tf score of word in this website
 				if (tfScoreOfWord == null) tfScoreOfWord = 0.0;
 				vertex v = new vertex(website,1);
 				double PR = g.vertexVec.get(g.vertexVec.indexOf(v)).newPR * wdmAss.balancePR; //get the website's PR and balance it
				System.out.println("PR: "+PR);
				System.out.println("TF: "+tfScoreOfWord);

 				tfScoreOfWord = tfScoreOfWord * PR; //augment score to reflect website's PR as well as the tf
 				stringScore wt = new stringScore(website,tfScoreOfWord);
 				wwf.wtVec.add(wt); //add the website name and the word's score in it to the word's list of scores
 			}
 			
 			//sort
 			Collections.sort(wwf.wtVec,comparator);
 			wwfVec.add(wwf);
 		}
 		
 		
 		//TA algo
 	  	Vector<stringScore> resultVec = new Vector<stringScore>();
 		double threshold = 0;
 		int index = 0;
 		String website;
 		double result;
 		double score; //calculated tf score * PR
 
 		boolean over = false;
 		while(!over && index < wwfVec.get(0).wtVec.size()) //as long as we have not found our top 3 websites
 		{
 			for(int j=0;j<wwfVec.size();j++)
 			{
 				result=0;
 				website = wwfVec.get(j).wtVec.get(index).name;
 				score = wwfVec.get(j).wtVec.get(index).score; //get the tf*PR score for word "j" in website "index"
 				threshold += score; //accumulate the scores along the words and afterwards devide by words amount for average
 				stringScore ssTemp = new stringScore(website,score);
 				if(resultVec.contains(ssTemp))
 				{
 					continue;         //skip over websites already added to the result list
 				}
 				result = score;
 				for(int k=0;k<wwfVec.size();k++)
 				{
 					if(k!=j)
 					{
 						double temp = wwfVec.get(k).wtVec.get(wwfVec.get(k).wtVec.indexOf(ssTemp)).score;
 						if(flag==1)   //the AND operator was given
 							result += temp;
 						else         //the OR operator was given
 							result = Math.max(result, temp);
 					}
 				}
 				if(flag==1){ //if AND was specified, divide accumulated result by words amount to get average
 					result = result/wwfVec.size();
 				}
 				ssTemp.score = result;
 				
 				if(resultVec.size()<3)//if we have not found 3 websites yet
 				{
 					resultVec.add(ssTemp);
 					Collections.sort(resultVec);
 				}
 				else
 				{
 					if(ssTemp.score>resultVec.elementAt(0).score) //if the new result is higher than the last of the top k, add it
 					{
 						resultVec.setElementAt(ssTemp,0);
 						Collections.sort(resultVec);
 					}
 				}
 			}
 			index++; //advance to next "row" of websites for each word
 			threshold = threshold/wwfVec.size(); //divide accumulated threshold by the amount of words to get average
 			if(resultVec.size()>=3 && resultVec.elementAt(0).score>=threshold) //top 3 results have been found
 				over=true;
 		}
 		
 		//print
         System.out.println("TA scores:");
 		for(int i=resultVec.size()-1,k=0;k<3;i--,k++)
 		{
 			System.out.println(""+(k+1)+") "+resultVec.elementAt(i).name+" "+wdmAss.df.format(resultVec.elementAt(i).score));
 		}
 	}
 }
