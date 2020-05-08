 package edu.ncu.oolab;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class GradeSystem {
 	
 	private class Grade {
 		
 		private String name_, id_;
 		private Integer lab1_, lab2_, lab3_, midTerm_, final_, grade_;
 		
 		public Grade(String line) {
 			byte[] bytes = null;
 			try {
 				bytes = line.getBytes("UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			InputStream in = new ByteArrayInputStream(bytes);
 			Scanner sin = new Scanner(in, "UTF-8");
 			this.id_ = sin.next();
 			this.name_ = sin.next();
 			this.lab1_ =  sin.nextInt();
 			this.lab2_ = sin.nextInt();
 			this.lab3_ = sin.nextInt();
 			this.midTerm_ = sin.nextInt();
 			this.final_ = sin.nextInt();
 			sin.close();
 			try {
 				in.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			this.calculateGrade();
 		}
 		
 		private void calculateGrade() {
 			Double a = ( this.lab1_ * GradeSystem.this.weights_[0] );
 			Double b = ( this.lab2_ * GradeSystem.this.weights_[1] );
 			Double c = ( this.lab3_ * GradeSystem.this.weights_[2] );
 			Double d = ( this.midTerm_ * GradeSystem.this.weights_[3] );
 			Double e = ( this.final_ * GradeSystem.this.weights_[4] );
 			this.grade_ = (int) Math.floor( a + b + c + d + e );
 		}
 		
 	}
 	
 	private ArrayList< Grade > grades_;
 	private Double[] weights_;
 	private Grade cUser_;
 	
 	public GradeSystem() {
 		this.grades_ = new ArrayList< Grade >();
 		this.weights_ = new Double[5];
 		this.weights_[0] = 0.1;
 		this.weights_[1] = 0.1;
 		this.weights_[2] = 0.1;
 		this.weights_[3] = 0.3;
 		this.weights_[4] = 0.4;
 		this.cUser_ = null;
 		
 		FileInputStream f = null;
 		try {
 			f = new FileInputStream("input.txt");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Scanner fin = new Scanner(f, "UTF-8");
 		while (fin.hasNextLine()) {
 			String line = fin.nextLine();
			if (line.isEmpty()) {
				break;
			}
 			this.grades_.add(new Grade(line));
 		}
 		fin.close();
 		try {
 			f.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public Boolean selectByID(String id) {
 		for (Grade grade : this.grades_) {
 			if (grade.id_.equals(id)) {
 				this.cUser_ = grade;
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public Double[] getWeights() {
 		return this.weights_;
 	}
 	
 	public void setWeights(Double[] weights) {
 		this.weights_ = weights;
 		for (Grade g : this.grades_) {
 			g.calculateGrade();
 		}
 	}
 	
 	public String getUserName() {
 		return this.cUser_.name_;
 	}
 	
 	public void showGrade() {
 		System.out.printf("Grade of %s:\n", this.cUser_.name_);
 		System.out.printf("Lab1: %d\n", this.cUser_.lab1_);
 		System.out.printf("Lab2: %d\n", this.cUser_.lab2_);
 		System.out.printf("Lab3: %d\n", this.cUser_.lab3_);
 		System.out.printf("Mid-term exam: %d\n", this.cUser_.midTerm_);
 		System.out.printf("Final exam: %d\n", this.cUser_.final_);
 		System.out.printf("Total: %d\n", this.cUser_.grade_);
 	}
 	
 	public void showRank() {
 		Integer grade = this.cUser_.grade_;
 		Integer rank = 1;
 		for (Grade g : this.grades_) {
 			if (g.grade_ > grade) {
 				rank += 1;
 			}
 		}
 		System.out.printf("Rank of %s is %d.\n", this.cUser_.name_, rank);
 	}
 
 }
