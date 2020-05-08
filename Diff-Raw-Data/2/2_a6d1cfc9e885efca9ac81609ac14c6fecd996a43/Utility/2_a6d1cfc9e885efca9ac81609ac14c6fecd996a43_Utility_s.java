 package emoAlgorithms;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 /** Utility Class
  * 
  * Contains some general static functions
  * 
  * @author Shahab
  *
  */
 public class Utility {
 	
 	public enum SortType {
 		ASC,DSC
 	}
 	
 	/** Sum Elements
 	 * summing an arrays elements
 	 * @param vect  an array 
 	 * @return a single value
 	 */
 	public static double sum(double[] vect) {
 		double ret = 0;		//accumulating values
 		for(double val : vect) {
 			ret += val;
 		}
 		return ret;
 	}
 	/** 
 	 *  same as the other overload
 	 * @param lb lower bound for sum
 	 */
 	public static double sum(double[] vect, int lb) {
 		double ret = 0;
 		for (int i = lb;i < vect.length;i++) {
 			ret += vect[i];
 		}
 		return ret;
 	}
 	
 	/**
 	 * sorts an array of chromosomes in descending order according to given objective index 
 	 * @param objective_index	index for the desired objective
 	 */
 	public static void chr_sort(Chromosome[] chrom_list,int objective_index,SortType type) { 
 		for (int i = 0;i < chrom_list.length - 1;i++) {		//determining correct value of each position 
 			for (int j = i+1;j < chrom_list.length;j++) {		//comparing the current value of position i to all other positions
 				switch (type) {
 				case ASC:
 					if (chrom_list[i].fitness_vector[objective_index] < chrom_list[j].fitness_vector[objective_index]) {
 						// we may swap chromosomes at positions i and j
 						Chromosome temp = chrom_list[i];
 						chrom_list[i] = chrom_list[j];
 						chrom_list[j] = temp;
 					}
 					break;
 				case DSC:
 					if (chrom_list[i].fitness_vector[objective_index] > chrom_list[j].fitness_vector[objective_index]) {
 						// we may swap chromosomes at positions i and j
 						Chromosome temp = chrom_list[i];
 						chrom_list[i] = chrom_list[j];
 						chrom_list[j] = temp;
 					}
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * determine whether first vector dominates the second one.
 	 * we need to minimize each dimension
 	 * @return	a boolean value true if vector1 dominates vector2 otherwise false 
 	 */
 	public static boolean pareto_dominate(double vector1[], double vector2[]) {
 		boolean flag = false;
 		for (int i = 0;i < vector1.length;i++) {
 			if (vector1[i] > vector2[i]) {
 				// this means vector1 can not dominate vector2
 				flag = true;//set a flag to break the calculation later
 				break;
 			}
 		}
 		// if each element of vector1 is less than or equal to corresponding element in vector2
 		// then we should continue
 		boolean ret = false;
 		if (!flag) {
 			for (int i = 0;i < vector1.length;i++) {
 				if (vector1[i] < vector2[i]) {
 					// this is enough for vector1 to dominate vector2
 					ret = true;
 					break;
 				}
 			}
 		}
 		return ret;
 	}
 	
 	/**
 	 * returns a string representation of an array
 	 * @param array	a double array	
 	 * @return	
 	 */
 	public static String arr2str(double array[]) {
 		String ret = "< ";
 		for (double d : array) {
 			ret += d + " ";
 		}
 		ret += ">";
 		return ret;
 	}
 	
 	/**
 	 * This function will create a deep copy of the argument
 	 * @param arg_obj the object to be copied
 	 * @return a reference to new object
 	 */
 	public static Object deep_copy(Object arg_obj) {
 		Object copy_obj = null;
 		try {
 			ByteArrayOutputStream baOut = new ByteArrayOutputStream();
 			ObjectOutputStream oOut = new ObjectOutputStream(new BufferedOutputStream(baOut));
 			oOut.writeObject(arg_obj);
 			oOut.flush();
 			ByteArrayInputStream baIn = new ByteArrayInputStream(baOut.toByteArray());
 			ObjectInputStream oIn = new ObjectInputStream(baIn);
 			copy_obj = oIn.readObject();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return copy_obj;
 	}
 }
