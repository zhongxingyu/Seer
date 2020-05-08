 package edu.utwente.vb.example;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
import static com.google.common.base.Preconditions.checkArgument;

 public class Builtins {
 	BufferedReader reader;
 	
 	public Builtins(){
 		reader = new BufferedReader(new InputStreamReader(System.in));
 	}
 	
 	protected int read(int i){
 		try{
 			return reader.read();
 		} catch (IOException e) {
 			throw new RuntimeException("Internal error while reading a int", e);
 		}
 	}
 	
 	protected char read(char c){
 		return (char)read(0);
 	}
 	
 	protected String read(String s){
 		try{
 			return reader.readLine();
 		} catch (IOException e) {
 			throw new RuntimeException("Internal error while reading a int", e);
 		}
 	}
 	
 	protected void print(int i){
 		System.out.println(i);
 	}
 	
 	protected void print(char c){
 		System.out.println(c);
 	}
 	
 	protected void print(String line){
 		System.out.println(line);
 	}
 	
 	protected void print(boolean b){
 		System.out.println(b);
 	}
 	
 	protected String stringAppend(String lhs, String rhs){
 		return lhs + rhs;
 	}
 	
 	protected String stringAppend(String lhs, char rhs){
 		return lhs + rhs;
 	}
 	
 	protected String stringAppend(String lhs, int rhs){
 		return lhs + rhs;
 	}
 	
 	protected String stringAppend(String lhs, boolean rhs){
 		return lhs + rhs;
 	}
 	
 	protected boolean stringEQ(String lhs, String rhs){
 		return lhs.equals(rhs);
 	}
 	
 	protected boolean stringNE(String lhs, String rhs){
 		return !lhs.equals(rhs);
 	}
 	
 	protected void ensure(boolean expr){
		checkArgument(expr);
 	}
 }
