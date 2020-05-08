 package edu.mit.wi.haploview;
 
 import java.util.*;
 
 class FindBlocks {
     String[][] dPrime;
 
     FindBlocks(String[][] data){
 	dPrime = data;
     }
 
     Vector doSFS(){
 	float cutHighCI = 0.98f;
 	float cutLowCI = 0.70f;
 	float recHighCI = 0.90f;
 	
	int numStrong = 0; int numRec = 0; 
 	Vector blocks = new Vector();
 	Vector strongPairs = new Vector();
 	
 	//first make a list of marker pairs in "strong LD", sorted by distance apart
 	for (int x = 0; x < dPrime.length-1; x++){
 	    for (int y = x+1; y < dPrime.length; y++){
 		StringTokenizer st = new StringTokenizer(dPrime[x][y]);
 		//get the right bits from the string
 		st.nextToken();
 		float lod = Float.parseFloat(st.nextToken());
 		st.nextToken();
 		float lowCI = Float.parseFloat(st.nextToken());
 		float highCI = Float.parseFloat(st.nextToken());
 		if (lod < -90) continue; //missing data
 		if (highCI < cutHighCI || lowCI < cutLowCI) continue; //must pass "strong LD" test
 
 		Vector addMe = new Vector(); //a vector of x, y, separation
 		int sep = y - x - 1; //compute separation of two markers
 		addMe.add(String.valueOf(x)); addMe.add(String.valueOf(y)); addMe.add(String.valueOf(sep));
 		if (strongPairs.size() == 0){ //put first pair first
 		    strongPairs.add(addMe);
 		}else{
 		    //sort by descending separation of markers in each pair
 		    for (int v = 0; v < strongPairs.size(); v ++){
 			if (sep >= Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(2))){
 			    strongPairs.insertElementAt(addMe, v);
 			    break;
 			}
 		    }
 		}
 	    }
 	}
 
 	//now take this list of pairs with "strong LD" and construct blocks
 	boolean[] usedInBlock = new boolean[dPrime.length + 1];
 	for (int v = 0; v < strongPairs.size(); v++){
 	    int first = Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(0));
 	    int last = Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(1));
 	    //first see if this block overlaps with another:
 	    if (usedInBlock[first] || usedInBlock[last]) continue;
 	    //test this block. requires 95% of informative markers to be "strong"
 	    for (int y = first+1; y <= last; y++){
 		//loop over columns in row y
 		for (int x = first; x < y; x++){
 		    StringTokenizer st = new StringTokenizer(dPrime[x][y]);
 		    //get the right bits
 		    st.nextToken();
 		    float lod = Float.parseFloat(st.nextToken());
 		    st.nextToken();
 		    float lowCI = Float.parseFloat(st.nextToken());
 		    float highCI = Float.parseFloat(st.nextToken());
 		    if (lod < -90) continue;   //monomorphic marker error
 		    if (lod == 0 && lowCI == 0 && highCI == 0) continue; //skip bad markers
		    if (lowCI > cutLowCI && highCI > cutHighCI) numStrong++; //strong LD
 		    if (highCI < recHighCI) numRec++; //recombination
 		}
 	    }
	    if (numStrong + numRec == 0) continue;
 	    if (numStrong/(numStrong + numRec) > 0.95){ //this qualifies as a block
 		//add to the block list, but in order by first marker number:		
 		if (blocks.size() == 0){ //put first block first
 		    blocks.add(first + " " + last);
 		}else{
 		    //sort by ascending separation of markers in each pair
 		    boolean placed = false;
 		    for (int b = 0; b < blocks.size(); b ++){
 			StringTokenizer st = new StringTokenizer((String)blocks.elementAt(b));
 			if (first < Integer.parseInt(st.nextToken())){
 			    blocks.insertElementAt(first + " " + last, b);
 			    placed = true;
 			    break;
 			}
 		    }
 		    //make sure to put in blocks which fall on the tail end
 		    if (!placed) blocks.add(first + " " + last);
 		}
 		for (int used = first; used <= last; used++){
 		    usedInBlock[used] = true;
 		}
 	    }
	    numStrong = 0; numRec = 0;
 	}
 	return stringVec2intVec(blocks);
     }
 
     Vector doMJD(){
 	// find blocks by searching for stretches between two markers A,B where
 	// D prime is > 0.8 for all informative combinations of A, (A+1...B)
 
 	int baddies;
 	int verticalExtent=0; 
 	int horizontalExtent=0;
 	Vector blocks = new Vector();
 	for (int i = 0; i < dPrime.length; i++){
 	    baddies=0;
 	    //find how far LD from marker i extends
 	    for (int j = i+1; j < dPrime[i].length; j++){
 		StringTokenizer st = new StringTokenizer(dPrime[i][j]);
 		//LD extends if D' > 0.8
 		if (Float.parseFloat(st.nextToken()) < 0.8){
 		    //LD extends through one 'bad' marker
 		    if (baddies < 1){
 			baddies++;
 		    } else {
 			verticalExtent = j-1;
 			break;
 		    }
 		}
 		verticalExtent=j;
 	    }
 	    //now we need to find a stretch of LD of all markers between i and j
 	    //start with the longest possible block of LD and work backwards to find
 	    //one which is good
 	    for (int m = verticalExtent; m > i; m--){
 		for (int k = i; k < m; k++){
 		    StringTokenizer st = new StringTokenizer(dPrime[k][m]);
 		    if(Float.parseFloat(st.nextToken()) < 0.8){
 			if (baddies < 1){
 			    baddies++;
 			} else {
 			    break;
 			}
 		    }
 		    horizontalExtent=k+1;
 		}
 		//is this a block of LD?
 
 		//previously, this algorithm was more complex and made some calls better
 		//but caused major problems in others. since the guessing is somewhat
 		//arbitrary, this new and simple method is fine.
 
 		if(horizontalExtent == m){
 		    blocks.add(i + " " + m);
 		    i=m;
 		}
 	    }
 	}
 	return stringVec2intVec(blocks);
     }
     Vector stringVec2intVec(Vector inVec){
 	//instead of strings with starting and ending positions convert blocks
 	//to int arrays
 	
 	Vector outVec = new Vector();
 	for (int i = 0; i < inVec.size(); i++){
 	    StringTokenizer st = new StringTokenizer((String)inVec.elementAt(i));
 	    int start = Integer.parseInt(st.nextToken()); int fin = Integer.parseInt(st.nextToken());
 	    int[] ma = new int[(fin-start)+1];
 	    for (int j = start; j <= fin; j++){
 		ma[j-start]=j;
 	    }
 	    outVec.add(ma);
 	}    
 	return outVec;
     }
 }
 
