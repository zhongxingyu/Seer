 /*
  * JNI_Pegasos - A Java Native Interface for Pegasos SVM
  * 
  * Copyright (C) 2012
  * Deepak Nayak 
  * Columbia University, Computer Science MS'13
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 51
  * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  */
 
 package jnipegasos;
 
 /**
  * Class to combine all learning parameters together.
  * 
  * @author Deepak Nayak
  *
  */
 public class LearningParameter {
 	private double lambda;	// learning rate
 	private int max_iter; 	// maximum iteration
 	private int exam_per_iter; // No. of examples per iter for stochastics gradient

 	public LearningParameter() {
 		lambda = 1;
 		max_iter = 10;
 		exam_per_iter = 1;
 	}

 	public LearningParameter(double l, int i, int k) {
 		lambda = l;
 		max_iter = i;
 		exam_per_iter = k;		
 	}

 	public LearningParameter getLearningParameter() {
 		return this;
 	}

 	public void setLambda(double l) {
 		lambda = l;		
 	}

 	public void setMaxIter(int i) {
 		max_iter = i;
 	}

 	public void setExamPerIter(int k) {
 		exam_per_iter = k;
 	}
 
 }
