 /*
 * $Id: CheckData.java,v 1.8 2004/01/20 18:50:14 jcbarret Exp $
  * WHITEHEAD INSTITUTE
  * SOFTWARE COPYRIGHT NOTICE AGREEMENT
  * This software and its documentation are copyright 2003 by the
  * Whitehead Institute for Biomedical Research.  All rights are reserved.
  *
  * This software is supplied without any warranty or guaranteed support
  * whatsoever.  The Whitehead Institute can not be responsible for its
  * use, misuse, or functionality.
  */
 
 package edu.mit.wi.pedfile;
 
 import java.util.*;
 
 /**
  * <p>Title: CheckData.java </p>
  * <p>Description: Used to check pedigree file and return information about observed heterozyosity,
  * predicted heterozygosity, Hardy-Weinberg test p-value, genotyped percent,
  * number of families with a fully genotyped trio and number of Mendelian inheritance errors.</p>
  * @author Hui Gong
  * @version $Revision 1.2 $
  */
 
 public class CheckData {
 	private PedFile _pedFile;
 
     static public double hwCut = 0.001;
     static public int failedGenoCut = 75;
     static public int numMendErrCut = 1;
     static public double mafCut = 0.00;
 	//private int _size;
 	//private Vector _pedFileEntries;
 	//private Hashtable pedFileHash;
 
 	public CheckData(PedFile pedFile) {
 		this._pedFile = pedFile;
 	}
 
 	/**
 	 * checks the pedigree file
 	 * @return Vector includes information about observed heterozyosity,
 	 * predicted heterozygosity, Hardy-Weinberg test p-value, genotyped percent,
 	 * number of families with a fully genotyped trio and number of Mendelian inheritance errors.
 	 */
 	public Vector check() throws PedFileException{
 		Vector results = new Vector();
 		//_size = _pedFile.getNumIndividuals();
 
 		int numOfMarkers = _pedFile.getNumMarkers();
 		//not worrying about names right now
 		//TODO: store and use marker names
 		//Vector names = this._pedFile.getMarkerNames();
 		Vector names = null;
 		boolean withName=false;
 		if(names!=null && names.size()==numOfMarkers) {
 			withName = true;
 		}
 		for(int i= 0; i < numOfMarkers; i++){
 			MarkerResult markerResult;
 			if(withName) {
 				markerResult = checkMarker(i, (String)names.get(i));
 			}else{
 				markerResult = checkMarker(i, new String("Marker " + (i+1)));
 			}
 			results.add(markerResult);
 		}
 		return results;
 	}
 
 	private MarkerResult checkMarker(int loc, String name)throws PedFileException{
 		MarkerResult result = new MarkerResult();
 		Individual currentInd;
 		//int indivgeno=0,
         int missing=0, parenthet=0, mendErrNum=0;
 		int allele1=0, allele2=0, hom=0, het=0;
 		//Hashtable allgenos = new Hashtable();
 		Hashtable numindivs=new Hashtable();
 		Hashtable parentgeno = new Hashtable();
 		Hashtable kidgeno = new Hashtable();
 		Hashtable parenthom = new Hashtable();
 		Hashtable count = new Hashtable();
 		String allele1_string, allele2_string;
 
 		//loop through each family, check data for marker loc
 		Enumeration famList = _pedFile.getFamList();
 		while(famList.hasMoreElements()){
 			Family currentFamily = _pedFile.getFamily((String)famList.nextElement());
 			Enumeration indList = currentFamily.getMemberList();
 			//loop through each individual in the current Family
 			while(indList.hasMoreElements()){
 				currentInd = currentFamily.getMember((String)indList.nextElement());
 				if (currentInd.getIsTyped()){
 					byte[] markers = currentInd.getMarker(loc);
 					allele1 = markers[0];
 					allele1_string = Integer.toString(allele1);
 					allele2 = markers[1];
 					allele2_string = Integer.toString(allele2);
 
 					String familyID = currentInd.getFamilyID();
 
 					incOrSetOne(numindivs,familyID);
 
 					//no allele data missing
 					if(allele1 > 0 && allele2 >0){
 						//make sure entry has parents
 						if(!(currentInd.getMomID().equals("0") || currentInd.getDadID().equals("0"))){
 							//do mendel check
 							//byte[] marker = ((Individual)pedFileHash.get(familyID + " " + currentInd.getMomID())).getMarker(loc);
 							byte[] marker = (currentFamily.getMember(currentInd.getMomID())).getMarker(loc);
 							int momAllele1 = marker[0];
 							int momAllele2 = marker[1];
 							//marker = ((Individual)pedFileHash.get(familyID + " " + currentInd.getDadID())).getMarker(loc);
 							marker = (currentFamily.getMember(currentInd.getDadID())).getMarker(loc);
 							int dadAllele1 = marker[0];
 							int dadAllele2 = marker[1];
 
 							//don't check if parents are missing any data
 							if (!(momAllele1 == 0 || momAllele2 == 0 || dadAllele1 == 0 || dadAllele2 ==0)){
 								//mom hom
 								if(momAllele1 == momAllele2){
 									//both parents hom
 									if (dadAllele1 == dadAllele2){
 										//both parents hom same allele
 										if (momAllele1 == dadAllele1){
 											//kid must be hom same allele
 											if (allele1 != momAllele1 || allele2 != momAllele1) {
                                                 mendErrNum ++;
                                                 currentInd.zeroOutMarker(loc);
                                                 currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                                 currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                             }
 											//parents hom diff allele
 										}else{
 											//kid must be het
 											if (allele1 == allele2) {
                                                 mendErrNum++;
                                                 currentInd.zeroOutMarker(loc);
                                                 currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                                 currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                             }
 										}
 										//mom hom dad het
 									}else{
 										//kid can't be hom for non-momallele
 										if (allele1 != momAllele1 && allele2 != momAllele1){
                                             mendErrNum++;
                                             currentInd.zeroOutMarker(loc);
                                             currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                             currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                         }
 									}
 									//mom het
 								}else{
 									//dad hom
 									if (dadAllele1 == dadAllele2){
 										//kid can't be hom for non-dadallele
 										if(allele1 != dadAllele1 && allele2 != dadAllele1){
                                             mendErrNum++;
                                             currentInd.zeroOutMarker(loc);
                                             currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                             currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                         }
 									}
 									//both parents het no mend err poss
 								}
 							}
 						}
 						//end mendel check
                     }
                 }
             }
             indList = currentFamily.getMemberList();
             //loop through each individual in the current Family
             while(indList.hasMoreElements()){
                 currentInd = currentFamily.getMember((String)indList.nextElement());
                 if (currentInd.getIsTyped()){
                     byte[] markers;
                     byte[] zeroArray = {0,0};
                     if (currentInd.getZeroed(loc)){
                         markers = zeroArray;
                     }else{
                       markers = currentInd.getMarker(loc);
                     }
                     allele1 = markers[0];
                     allele1_string = Integer.toString(allele1);
                     allele2 = markers[1];
                     allele2_string = Integer.toString(allele2);
 
                     String familyID = currentInd.getFamilyID();
 
                     incOrSetOne(numindivs,familyID);
 
                     //no allele data missing
                     if(allele1 > 0 && allele2 >0){
                         //indiv has parents
                         if(currentInd.getMomID().compareTo(Individual.DATA_MISSING)==0 && currentInd.getDadID().compareTo(Individual.DATA_MISSING)==0){
                             //$parentgeno{$ped}++
 							//set parentgeno
 							incOrSetOne(parentgeno,familyID);
 							if(allele1 != allele2) {
 								parenthet++;
 							}
 							else{
 								incOrSetOne(parenthom,allele1_string);
 							}
 						}
 						else{//$kidgeno{$ped}++
 							incOrSetOne(kidgeno,familyID);
 						}
 						if(allele1 == allele2) {
 							hom++;
 						}
 						else {
 							het++;
 						}
 						//count number of allele
 						incOrSetOne(count,allele1_string);
 						incOrSetOne(count,allele2_string);
 					}
 					//missing data
 					else missing++;
 				}
 			}
 		}
 		double obsHET = getObsHET(het, hom);
 		double[] freqStuff = getFreqStuff(count);
         double preHET = freqStuff[0];
         double maf = freqStuff[1];
 
 		//HW p value
 		double pvalue = getPValue(parenthom, parenthet);
 
 		//geno percent
 		double genopct = getGenoPercent(het, hom, missing);
 
 		// num of families with a fully genotyped trio
 		//int famTrio =0;
 		int famTrio = getNumOfFamTrio(numindivs, parentgeno, kidgeno);
 
 		//rating
 		int rating = this.getRating(genopct, pvalue, obsHET, mendErrNum,maf);
 
 		result.setObsHet(obsHET);
 		result.setPredHet(preHET);
         result.setMAF(maf);
 		result.setHWpvalue(pvalue);
 		result.setGenoPercent(genopct);
 		result.setFamTrioNum(famTrio);
 		result.setMendErrNum(mendErrNum);
 		result.setRating(rating);
 		result.setName(name);
 		return result;
 	}
    /**
     * checks for a value with key key in the hashtable ht. if it exists, it is incremented
     * if there is no value the value is set to one
     * @param ht hashtable
     * @param key key of the value we want to increment or set to one
     */
 	private void incOrSetOne(Hashtable ht, String key){
 		if(ht.containsKey(key)){
 			int value = Integer.parseInt((String)ht.get(key));
 			ht.put(key, Integer.toString(++value));
 		}
 		else{
 			ht.put(key, "1");
 		}
 	}
 
 	/**
 	 * Gets observed heterozygosity
 	 */
 	private double getObsHET(int het, int hom){
         double obsHET;
         if (het+hom == 0){
             obsHET = 0;
         }else{
             obsHET = het/(het+hom+0.0);
         }
 		return obsHET;
 	}
 
 	private double[] getFreqStuff(Hashtable count){
         double[] freqStuff = new double[2];
 		int sumsq=0, sum=0, num=0, mincount = -1;
 		Enumeration enu = count.elements();
 		while(enu.hasMoreElements()){
 			num = Integer.parseInt((String)enu.nextElement());
 			sumsq += num*num;
 			sum += num;
             if (mincount < 0 || mincount > num){
                 mincount = num;
             }
 		}
         if (sum == 0){
             freqStuff[0] = 0;
             freqStuff[1] = 0;
         }else{
             freqStuff[0] = 1.0 - (sumsq/((sum*sum)+0.0));
             freqStuff[1] = mincount/(sum+0.0);
         }
 		return freqStuff;
 	}
 
 
 	private double getPValue(Hashtable parentHom, int parentHet) throws PedFileException{
 		//ie: 11 13 31 33 -> homA =1 homB = 1 parentHet=2
 		int homA=0, homB=0, num;
 		double pvalue=0;
 		Enumeration enu = parentHom.elements();
 		while(enu.hasMoreElements()){
 			num = Integer.parseInt((String)enu.nextElement());
 			if(homA>0) homB = num;
 			else homA = num;
 		}
 		//caculate p value from homA, parentHet and homB
 		// using hw
 		//System.out.println("homA="+homA+" homB="+homB+" parentHet="+parentHet);
 		//HW hw = new HW((double)homA, (double)parentHet, (double)homB);
 			//hw.caculate();
        if (homA + parentHet + homB <= 0){
            pvalue=0;
        }else{
 			pvalue = hwCalculate(homA, parentHet, homB);
        }
 		return pvalue;
 	}
 
 
 	private double hwCalculate(int obsAA, int obsAB, int obsBB) throws PedFileException{
         //Calculates exact two-sided hardy-weinberg p-value. Parameters
         //are number of genotypes, number of rare alleles observed and
         //number of heterozygotes observed.
         //
         // (c) 2003 Jan Wigginton, Goncalo Abecasis
         int diplotypes =  obsAA + obsAB + obsBB;
         int rare = (obsAA*2) + obsAB;
         int hets = obsAB;
 
 
         //make sure "rare" allele is really the rare allele
         if (rare > diplotypes){
             rare = 2*diplotypes-rare;
         }
 
         //make sure numbers aren't screwy
         if (hets > rare){
             throw new PedFileException("HW test: " + hets + "heterozygotes but only " + rare + "rare alleles.");
         }
         double[] tailProbs = new double[rare+1];
         for (int z = 0; z < tailProbs.length; z++){
             tailProbs[z] = 0;
         }
 
         //start at midpoint
         int mid = rare * (2 * diplotypes - rare) / (2 * diplotypes);
 
         //check to ensure that midpoint and rare alleles have same parity
         if (((rare & 1) ^ (mid & 1)) != 0){
             mid++;
         }
         int het = mid;
         int hom_r = (rare - mid) / 2;
         int hom_c = diplotypes - het - hom_r;
 
         //Calculate probability for each possible observed heterozygote
         //count up to a scaling constant, to avoid underflow and overflow
         tailProbs[mid] = 1.0;
         double sum = tailProbs[mid];
         for (het = mid; het > 1; het -=2){
             tailProbs[het-2] = (tailProbs[het] * het * (het-1.0))/(4.0*(hom_r + 1.0) * (hom_c + 1.0));
             sum += tailProbs[het-2];
             //2 fewer hets for next iteration -> add one rare and one common homozygote
             hom_r++;
             hom_c++;
         }
 
         het = mid;
         hom_r = (rare - mid) / 2;
         hom_c = diplotypes - het - hom_r;
         for (het = mid; het <= rare - 2; het += 2){
             tailProbs[het+2] = (tailProbs[het] * 4.0 * hom_r * hom_c) / ((het+2.0)*(het+1.0));
             sum += tailProbs[het+2];
             //2 more hets for next iteration -> subtract one rare and one common homozygote
             hom_r--;
             hom_c--;
         }
 
         for (int z = 0; z < tailProbs.length; z++){
             tailProbs[z] /= sum;
         }
 
         double top = tailProbs[hets];
         for (int i = hets+1; i <= rare; i++){
             top += tailProbs[i];
         }
         double otherSide = tailProbs[hets];
         for (int i = hets-1; i >= 0; i--){
             otherSide += tailProbs[i];
         }
 
         if (top > 0.5 && otherSide > 0.5){
             return 1.0;
         }else{
             if (top < otherSide){
                 return top * 2;
             }else{
                 return otherSide * 2;
             }
         }
     }
 
 	private double getGenoPercent(int het, int hom, int missing){
 		double genoPct = 100.0*(het+hom)/(het+hom+missing);
 		return genoPct;
 	}
 
 	private int getNumOfFamTrio(Hashtable numindivs, Hashtable parentgeno, Hashtable kidgeno){
 		//int totalfams = 0;
         int tdtfams =0;
 		Enumeration enuKey = numindivs.keys();
 		while(enuKey.hasMoreElements()){
 			//totalfams++;
 			int parentGeno=0, kidsGeno =0;
 			String key = (String)enuKey.nextElement();
 			Object pGeno = parentgeno.get(key);
 			Object kGeno = kidgeno.get(key);
 			if(pGeno != null) parentGeno = Integer.parseInt((String)pGeno);
 			if(kGeno != null) kidsGeno = Integer.parseInt((String)kGeno);
 			if(parentGeno>=2 && kidsGeno>=1) tdtfams += parentGeno/2;
 		}
 		return tdtfams;
 	}
 
 	private int getRating(double genopct, double pval, double obsHet, int menderr, double maf){
 		int rating = 0;
 		if (obsHet < 0.01){
 			rating -= 1;
 		}
         if (genopct < failedGenoCut){
 			rating -= 2;
 		}
         if (pval < hwCut){
 			rating -= 4;
 		}
         if (menderr > numMendErrCut){
 			rating -= 8;
 		}
         if (maf < mafCut){
             rating -= 16;
         }
         if (rating == 0){
 			rating = 1;
 		}
 		return rating;
 	}
 }
