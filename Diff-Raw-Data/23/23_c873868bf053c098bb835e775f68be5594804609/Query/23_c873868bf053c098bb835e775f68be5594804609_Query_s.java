 import java.util.ArrayList;
 import java.util.Collections;
 
 public class Query {
 	Csv csv;
 	Query(Csv input){
 		csv = input;
 	}
 	
 	public float MeanUtility(int userID, int itemID){
 		//blank out the user's item specified by param
 		csv.data.get(userID).ratings().set(itemID, (float)99);
 		
 		//for each item with valid rating for all users, sum the ratings up
 		float sum = 0;
 		int numOfData = 0;
 		for(int userIdx = 0; userIdx < csv.data.size(); userIdx++){
 			if(csv.data.get(userIdx).ratings().get(itemID) != 99){
 				sum += csv.data.get(userIdx).at(itemID);
 				numOfData++;
 			}
 		}
 		
 		//now divide it to get the average
 		sum = sum / (float)numOfData;
 		
 		return sum;
 	}
 	
 	 public float MeanUtilityWRank(User u, int itemID, int N){
 	        //blank out the user's item specified by param
 	        u.ratings().set(itemID, (float)99);
 	        
 	        //for each item with valid rating for all users, sum the ratings up
 	        float sum = 0;
 	        int numOfData = 0;
 	        
 	        ArrayList<User> tempUsers = getRank(u,N);
 	        
 	        for(int userIdx = 0; userIdx < tempUsers.size(); userIdx++){
 	            if(tempUsers.get(userIdx).at(itemID) != 99){
 	                sum += tempUsers.get(userIdx).at(itemID);
 	                numOfData++;
 	            }
 	        }
 	        
 	        //now divide it to get the average
 	        sum = sum / (float)numOfData;
 	        
 	        return sum;
 	    }
 	 
 	
     public float AdjustedWeightedSum(int userID, int itemID){
         //user's average rating + k * sim * (u(c',s) - uc') 
         float userAverage = csv.data.get(userID).averageRating();
         //calculate k
         float k = K(csv.data.get(userID),itemID, csv.data);
         
         //calculate the sum of (sim(u[c],u[c']) * (u(c',s) - uc'))
         float sum = 0;
         for(int cp = 0; cp < csv.data.size(); cp++){
             if(cp != userID && (csv.data.get(cp).at(itemID) != 99)){
                 float CPAverage = csv.data.get(cp).averageRating();
                 sum += (PearsonCorrelation(csv.data.get(userID),csv.data.get(cp)) * (csv.data.get(cp).at(itemID) - CPAverage));
             }
         }
         return userAverage + k * sum;
     }
     
     public float AdjustedWeightedSumRanked(int userID, int itemID, int N){
         //get the user in question
         User curruser = csv.data.get(userID);
         
         //user's average rating + k * sim * (u(c',s) - uc') 
         float userAverage = curruser.averageRating();
         
         ArrayList<User> tempUser = getRank(curruser, N);
         
         //calculate k
         float k = K(curruser,itemID,tempUser);
         
         //calculate the sum of (sim(u[c],u[c']) * (u(c',s) - uc'))
         float sum = 0;
         for(int cp = 0; cp < tempUser.size(); cp++){
             if(tempUser.get(cp).at(itemID) != 99){
                 float CPAverage = tempUser.get(cp).averageRating();
                 sum += (PearsonCorrelation(curruser,tempUser.get(cp)) * (tempUser.get(cp).at(itemID) - CPAverage));
             }
         }
         return userAverage + k * sum;
     }
 	
 	public float AdjustedWeightedSumTransformed(int userID, int itemID, ArrayList<Item> items){
         //user's average rating + k * sim * (u(c',s) - uc') 
 	    float userAverage = csv.data.get(userID).averageRating();
 	    //calculate k
 	    float k = K(csv.data.get(userID),itemID, csv.data);
 	    
 	    //calculate the sum of (sim(u[c],u[c']) * (u(c',s) - uc'))
 	    float sum = 0;
         for(int cp = 0; cp < csv.data.size(); cp++){
             if(cp != userID && (csv.data.get(cp).at(itemID) != 99)){
                 float CPAverage = csv.data.get(cp).averageRating();
                 sum += (PearsonCorrelationTransformed(userID,cp, items) 
                 		* (csv.data.get(cp).at(itemID) - CPAverage));
             }
         }
         return userAverage + k * sum;
     }
 	
     public float K(User u, int itemID, ArrayList<User> UA){
         float sum = 0;
         for(int i = 0; i < UA.size(); i++){
             if(u.getUserID() != UA.get(i).getUserID()){
                 sum += Math.abs(PearsonCorrelation(u,UA.get(i)));
             }
         }
         return 1f/sum;
     }
 	
     public float PearsonCorrelation(User u1, User u2){
         // sum1 / sqrt( (sum2)^2 * (sum3)^2)
         
         float sum1 = 0;
         float sum2 = 0;
         float sum3 = 0;
         for(int i = 0; i < u1.ratings().size(); i++){
         	if(u1.at(i) != 99 && u2.at(i) != 99){
                 sum1 += (u1.at(i) - u1.averageRating()) *
                 (u2.at(i) - u2.averageRating()) ;
         	}
 
             if(u1.at(i) != 99){
                 sum2 += Math.pow((u1.at(i) - u1.averageRating()), 2);
             }
             
             if(u2.at(i) != 99){
                 sum3 += Math.pow((u2.at(i) - u2.averageRating()), 2);
             }
             
             if(sum2 == 0)
             {
             	sum2+=.0001;
             }
             if(sum3 ==0)
             {
             	sum3 += .0001;
             }
         }
         
         if(Math.sqrt(sum2*sum3) == 0)
         {
         	System.err.println("Error, denominator for Pearson Correlation 0!");
         	System.exit(-1);
         }
         
         float result = (float) (sum1 / Math.sqrt(sum2*sum3));
         if(result < -1 || result > 1)
         {
         	System.err.println("BAD Pearson Correlation!! No din din for you!");
         	System.exit(-1);
         }
         return result;
     }
     
     public float PearsonCorrelationTransformed(int UID1, int UID2, ArrayList<Item> items){
         // sum1 / sqrt( (sum2)^2 * (sum3)^2)
         
         float sum1 = 0;
         float sum2 = 0;
         float sum3 = 0;
         float sum4 = 0f;
         float sum5 = 0f;
         float sum7 = 0f;
         float sum8 = 0f;
         float fjAll = 0f;
         
         
         for(int i = 0; i < csv.data.get(0).ratings().size(); i++)
         {
         	float fj1 = items.get(i).invUserFreq();
         	fjAll += fj1;
         	
         	if(csv.data.get(UID1).at(i) != 99 && csv.data.get(UID2).at(i) != 99)
         	{	
                 sum1 += fj1 * (csv.data.get(UID1).at(i)) * (csv.data.get(UID2).at(i));
         	}
 
             if(csv.data.get(UID1).at(i) != 99)
             {
                 sum2 += fj1 * (csv.data.get(UID1).at(i) );
                 sum4 += fj1 *  Math.pow((csv.data.get(UID1).at(i)), 2);
             }
             
             if(csv.data.get(UID2).at(i) != 99)
             {
                 sum3 += fj1 * (csv.data.get(UID2).at(i));
                 sum5 += fj1 * Math.pow((csv.data.get(UID2).at(i) ), 2);
             }
             
             if(sum2 == 0)
             {
             	sum2+=.00001;
             }
             if(sum3 ==0)
             {
             	sum3 += .00001;
             }
             if(sum4 == 0)
             {
             	sum4 += .00001;
             }
             if(sum5 == 0)
             {
             	sum5 += .00001;
             }
             if(sum1 == 0)
             {
             	sum1 += .00001;
             }
         }
         for(int i = 0; i < csv.data.get(0).ratings().size(); i++)
         {
         	float fj1 = items.get(i).invUserFreq();
         	sum7 += fj1*(csv.data.get(UID1).at(i) ) - sum2;
         	sum8 += fj1*(csv.data.get(UID2).at(i) ) - sum3;
         }
         float result = (float) ((fjAll*sum1 - sum2*sum3 ) / 
         		Math.sqrt(  Math.pow(sum7, 2)     // U
         	              * Math.pow(sum8, 2))) ;  // V
         
         //float result = (float) ((fjAll*sum1 - sum2*sum3 ) / 
         //        		Math.sqrt( ((fjAll * (Math.pow(sum2,2)) - sum4)     // U
         //        	              * (fjAll * (Math.pow(sum3,2)) - sum5) )));  // V
         
         //float result = (float) ((fjAll*sum1 - sum2*sum3 ) / 
         //		Math.sqrt( ((fjAll * (sum4 - Math.pow(sum2,2)))     // U
         //	              * (fjAll * (sum5 - Math.pow(sum3,2))))));  // V
         
         //float result = (float) ((fjAll*sum1 - sum2*sum3 ) / 
         //				 ((fjAll * (sum4 - Math.pow(sum2,2)))     // U
         //	            * (fjAll * (sum5 - Math.pow(sum3,2)))));  // V
         
         if( sum4 < Math.pow(sum2,2) && Math.random() > .95)
         {
         	//System.err.println(fjAll + "DERP " + sum1 + " " +sum2 + " " +sum3 + " " +sum4 + " " +sum5 
         	//		+ " \n" + result);
         	//System.exit(-1);
         }
         if( Math.abs(result) > 1 )//&& Math.random() > .95)
         {
         	//System.err.println( sum1 + " " +sum2 + " " +sum3 + " " +sum4 + " " +sum5  + " " +sum7 + " " +sum8
         	//		+ " \n" + result);
         	//System.exit(-1);
         }
         
         if(result > 1.01)
         {
         	result = 1;
         }
         else if(result < -1.01)
         {
         	result = -1;
         }
         
         return result;
     }
     
    /*public float CosineSimilarity(int UID1, int UID2){
         // sum1 / sqrt( (sum2)^2 * (sum3)^2)
         
         float sum1 = 0;
         float sum2 = 0;
         float sum3 = 0;
         for(int i = 0; i < csv.data.get(0).ratings().size(); i++){
            if(csv.data.get(UID1).at(i) != 99 && csv.data.get(UID2).at(i) != 99){
                sum1 += csv.data.get(UID1).at(i) * csv.data.get(UID2).at(i);
             }
 
            if(csv.data.get(UID1).at(i) != 99){
                sum2 += Math.pow(csv.data.get(UID1).at(i), 2);
             }
             
            if(csv.data.get(UID2).at(i) != 99){
                sum3 += Math.pow(csv.data.get(UID2).at(i), 2);
             }
             
             if(sum2 == 0)
             {
                 sum2+=.0001;
             }
             if(sum3 ==0)
             {
                 sum3 += .0001;
             }
         }
         
         if(Math.sqrt(sum2*sum3) == 0)
         {
            System.err.println("oopz:" + sum2 + " " + sum3 + "UID2=" + UID2 + " UID1=" + UID1);
             System.exit(-1);
         }
         
         float result = (float) (sum1 / Math.sqrt(sum2*sum3));
         if(result < -1 || result > 1)
         {
             System.err.println("BAD Cosine Similarity!! No din din for you!");
             System.exit(-1);
         }
         return result;
    }*/
 	
     private ArrayList<User> getRank(User u, int N){
         ArrayList<User> answer = new ArrayList<User>();
         ArrayList<Pair> pairList= new ArrayList<Pair>();
         for(int i = 0; i < csv.data.size(); i++){
             if(u.getUserID() != csv.data.get(i).getUserID()){
                 pairList.add(new Pair(PearsonCorrelation(u,csv.data.get(i)), csv.data.get(i)));
             }
         }
         Collections.sort(pairList);
         
         for(int i = 0; i < N; i++){
             //System.out.println("simVar="+pairList.get(i).getSimValue());
             answer.add(pairList.get(i).getUser());
         }
 
         return answer;
     }
     
 }
