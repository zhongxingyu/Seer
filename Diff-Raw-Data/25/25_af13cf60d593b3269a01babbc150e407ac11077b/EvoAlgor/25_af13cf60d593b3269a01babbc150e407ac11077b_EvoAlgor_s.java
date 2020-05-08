 import java.io.IOException;
 import java.util.*;
 
 
 
 public class EvoAlgor {
 	/**
 	 * 
 	 * Class uses Evolutionary Algorithm to find order qty array
 	 */
 	static int orderSize;
 	static int rand;
 	static double[] allNumbers;
 	static int[] sequenceQtyCand;
 	static int[] sequencePriceCand;
 	static double[] qtyCandidates;
 	static double[] priceCandidates;
 	static List <Double> qtyPool;
 	static List <Double> pricePool;
 	static Set <Double> totalPool;
 	static Random randGenerator;
 	static List<Double> qtySol;
 	static List<Double> priceSol;
 	static Set<List<Double>> possibleSolutions;
 //	static List<List<Double>> possiblePriceSolutions;
 	static List<List<Integer>> possibleSolutionsIndexes;
 	static List <Integer> localIndexes;
 	static List <Integer> seqQtyCandSet;
 //	static List <Integer> seqPriceCandSet;
 	static List <Double> possiSolDiff;
 	static List<ArrayList<Double>> finalSol;
 	
 	@SuppressWarnings("unchecked")
 	public static List<ArrayList<Double>> main(int oSize, Object[] allNumb, String totalAmt, String SoldTo, List<Double> prices) throws IOException{
 		double totalSum = 0;
 		boolean fitnessFound = false;
 		int iterations = 0;
 		int localRand;
 		int shortestRangePos = 0;
 		final double minPrice = 13.0;
 		final double maxPrice = 9000.0; //9000
 		final double minQty = 1.0;
 		double maxQty = 1500.0; //350, it will change bellow for CTIS, that's why it's not final
 		qtySol = new ArrayList<Double>();
 		qtyPool = new ArrayList<Double>();
 		pricePool = new ArrayList<Double>();
 		totalPool = new HashSet<Double>();
 		possibleSolutions = new LinkedHashSet<List<Double>>();
 		finalSol = new ArrayList<ArrayList<Double>>();
 		possibleSolutionsIndexes = new ArrayList<List<Integer>>();
 		localIndexes = new ArrayList<Integer>();
 		randGenerator = new Random();
 		
 		orderSize = oSize;
 		String soldTo = SoldTo;
 		
 		sequenceQtyCand = new int[orderSize];
 		sequencePriceCand = new int[orderSize];
 		qtyCandidates = new double[orderSize];
 		priceCandidates = new double[orderSize];
 		seqQtyCandSet = new ArrayList<Integer>();
 		possiSolDiff = new ArrayList<Double>();
 		
 		allNumbers = new double[allNumb.length];
 		//build pools
 		double currentYear = Calendar.getInstance().get(Calendar.YEAR);
 		
 		//This is to adjust the number format only used by CTIS for qty only, as price format is ok
 		if (soldTo.equals("673333") ) {
 			maxQty = 610000.0;
 		}
 		
 		for (int i = 0; i<allNumb.length; i++) {
 			allNumbers[i] = (Double) allNumb[i];
 			if (Double.parseDouble((allNumb[i]).toString()) >= minQty && Double.parseDouble((allNumb[i]).toString()) <= maxQty && 
 					Double.parseDouble((allNumb[i]).toString()) - Double.valueOf((allNumb[i]).toString()).intValue()== 0 &&
 					Double.parseDouble((allNumb[i]).toString()) != currentYear) {
 				qtyPool.add((Double) allNumb[i]);	//excluding current year
 			}
 			if (Double.parseDouble((allNumb[i]).toString()) >= minPrice && Double.parseDouble((allNumb[i]).toString()) <= maxPrice && Double.parseDouble((allNumb[i]).toString()) != currentYear) {
 				pricePool.add((Double) allNumb[i]);	//excluding current year
 			}
 		}
 		//This is to adjust the number format only used by CTIS for qty only, as price format is ok
 		if (soldTo.equals("673333") ) {
 			List <Double> tempQty1000 = new ArrayList<Double>();
 			for (Double tempQty: qtyPool) {
 				tempQty1000.add(tempQty/1000);
 			}
 			qtyPool.clear();
 			for (double tempQty: tempQty1000) {
 				qtyPool.add(tempQty);
 			}
 		}
 		//exclude from pricepool the elements that are also in qtypool:UPDATE: humm, not a good idea, prices are integer sometimes
 /*		for(Iterator<Double> it = pricePool.iterator() ; it.hasNext() ; ) {
 			if (qtyPool.contains(it.next())) {
 				it.remove();
 			}
 		}	*/
 		
 		//let's use only the predefined prices
		try {
 		pricePool.clear();
 		for (double pri : prices) {
 			pricePool.add(pri);
 		}
 		} catch (NullPointerException e) {
			System.err.println("Prices array was null in EvoAlgor(), filling prices with 0");
			for (int i = 0; i<orderSize; i++) {
				pricePool.add(0.0);
				prices.add(i, 0.0);
			}
			prices.remove(orderSize);
 		}
 		
 		
 		//totalPool can only be created after we have the pricePool
 		if( Double.parseDouble(totalAmt) == 0.0) {
 			double minPriceInPool = Collections.min(pricePool);
 			double minQtyPool = Collections.min(qtyPool);
 		
 			for (int i = 0; i<allNumbers.length; i++) {
 			
 				if(allNumbers[i] >= minPriceInPool*orderSize*minQtyPool && allNumbers[i] <= 20000000.0 && allNumbers[i] != currentYear) {    // limiting to 20,000,000.00 and excluding current year
 					totalPool.add(allNumbers[i]);
 				}
 			}
 		} else {
 			//testing to remove IPI for bompreco 
 			if (soldTo.equals("664681") || soldTo.equals("664683")) {
 				totalPool.add(Double.parseDouble(totalAmt)/100/1.15);
 			} else {
 				totalPool.add(Double.parseDouble(totalAmt)/100); // divide by 100 to account for cents
 			}
 		}
 		
 		System.out.println("qtyPool = " + qtyPool);
 		System.out.println("pricePool = " + pricePool);
 		System.out.println("totalPool = " + totalPool);
 		
 		while (iterations < 50000000) {
 			iterations++;
 			totalSum = 0;
 			int qc = 0;
 			int pc = 0;
 			
 			//reset arrays and lists
 			for (int i = 0; i < orderSize; i++) {
 				qtyCandidates[i] = -1;
 				sequenceQtyCand[i] = -1;
 				priceCandidates[i] = -1;
 				sequencePriceCand[i] = -1;
 			}
 			seqQtyCandSet.clear();
 //			seqPriceCandSet.clear();
 			
 		// build candidates, ordering sequences
 			//quantity related
 			for (int i = 0; i < orderSize; i++) {
 				localRand = GenerateValidRand("qtity");
 				seqQtyCandSet.add(localRand);
 			}
 			
 			Collections.sort(seqQtyCandSet);
 			
 			for (int i : seqQtyCandSet) {
 				qtyCandidates[qc] = qtyPool.get(i);
 				qc++;
 			}
 			
 			//price related
 
 			
 			for (double pri : prices) {
 				priceCandidates[pc] = pri;
 				pc++;
 			}
 			
 			//total amount related
 			for (int m = 0; m < orderSize; m++) {
 				totalSum += qtyCandidates[m]*priceCandidates[m];
 			}
 			
 			//test against the whole total pool
 			for (double totalAmount2 : totalPool) {
 				
 				double lowerLimit = 0.0;
 				double upperLimit = 0.0;
 				
 				if (totalAmount2 < 1000) {
 					lowerLimit = 0.99*totalAmount2;
 					upperLimit = 1.01*totalAmount2;
 				} else if (totalAmount2 < 100000) {
 					lowerLimit = 0.999*totalAmount2;
 					upperLimit = 1.001*totalAmount2;
 				} else if (totalAmount2 < 1000000) {
 					lowerLimit = 0.999*totalAmount2;
 					upperLimit = 1.001*totalAmount2;
 				} else if (totalAmount2 < 20000000) {
 					lowerLimit = 0.9999*totalAmount2;
 					upperLimit = 1.0001*totalAmount2;	//leaving the same limits as for orders above 100K for testing, since I'm having trouble with orders >1M
 				}
 				
 				fitnessFound = (totalSum >= lowerLimit && totalSum <= upperLimit);
 				if (fitnessFound) {
 					qtySol = new ArrayList<Double>();
 					priceSol = new ArrayList<Double>();
 					localIndexes = new ArrayList<Integer>();
 						//now the seqQtyCandSet is already ordered, no need to use GetQty2()
 						for (int i : seqQtyCandSet) {
 							qtySol.add(qtyPool.get(i));
 							localIndexes.add(i);
 						}
 						for (double pri: prices) {
 							priceSol.add(pri);
 						}
 
 						if (possibleSolutions.add(qtySol)) {
 							possibleSolutionsIndexes.add(localIndexes);
 							possiSolDiff.add(java.lang.Math.abs(totalAmount2 - totalSum));
 						}
 						
 			//		break whileit;
 				}
 			}
 
 		}
 		System.out.print("\npossibleSolutions size = "+possibleSolutions.size() + ": "); 
 		if (possibleSolutions.size()>0) {
 			System.out.print("\npossiSolDiff min = "+Collections.min(possiSolDiff) + ": ");
 			shortestRangePos = findShortestRange2();
 			finalSol.add((ArrayList<Double>) possibleSolutions.toArray()[shortestRangePos]);
 			finalSol.add((ArrayList<Double>) prices);
 		} else {
 			System.out.println("\nWe're in Evoalgor and no solution was found for this PO!!!!!");
 		}
 		
 		
 		return finalSol;
 	}
 	private static int GenerateValidRand (String rqtr) {
 		int found = 0;
 		
 		do {
 		if (rqtr.equals("total")) {
 			
 			rand = randGenerator.nextInt(allNumbers.length-(orderSize*2)) + orderSize*2;
 			
 		} else if (rqtr.equals("total2")){	
 			
 			rand = randGenerator.nextInt(totalPool.size());
 				
 		} else if (rqtr.equals("qtity")){	
 			found = 0;
 			rand = randGenerator.nextInt(qtyPool.size());
 			for (int j : seqQtyCandSet) {
 				if (rand == j) {
 					found++;
 				}
 			}
 		} 
 		} while (!(found == 0));
 		return rand;
 	}
 
 	private static int findShortestRange2() {
 		int final_index;
 		double minDif;
 		List<Integer> rangeList = new ArrayList<Integer>();
 		List<Integer> rangeListIndex = new ArrayList<Integer>();
 		
 		minDif = Collections.min(possiSolDiff);
 		
 		for (int i = 0; i < possiSolDiff.size(); i++ ){
 			if (possiSolDiff.get(i) == minDif){
 				rangeList.add(Collections.max(possibleSolutionsIndexes.get(i)) - Collections.min(possibleSolutionsIndexes.get(i)));
 				rangeListIndex.add(i);
 			}
 		}
 		
 		final_index = rangeListIndex.get(rangeList.lastIndexOf(Collections.min(rangeList)));
 		
 		return final_index;
 	}
 }
