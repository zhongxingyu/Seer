 package com.IntsArithmetic.DataStructure;
 /**
  * @file BigInt.java
  * @brief BigInt class
  *
  * @version 0.1
  * @author Jos Ignacio Carmona Villegas <joseicv@correo.ugr.es>
  * @author Juan Hernandez Garca <juanhg@correo.ugr.es>
  * @date 07/October/2013
  *
  * @section LICENSE
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation; either version 2 of
  * the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details at
  * http://www.gnu.org/copyleft/gpl.html
  * 
  */
 
 import java.util.Vector;
 import java.util.Collections;
 
 /**
  * @brief Big Integer.
  * Represents a Big Integer of variable length.
  */
 public class BigInt
 {
 	// Maximum number of digits of any valid number in our base
 	private final int DIGITS_PER_PART = 9;
 	// Base
 	private final int MAX_INTEGER_PER_PART = 1000000000;
 	
 	private Vector<Integer> data;
 	private boolean isNegative;
 	
 	
 	public BigInt(String input)
 	{	
 		data = new Vector<Integer>(0);
 		isNegative = false;
 
 		
 		// The first chars are the most significative.
 		// Read DIGITS_PER_PART decimal digits, and add them to an Integer.
 		
 		// Since the number may not be composed of a number of digits divisible by DIGITS_PER_PART,
 		// first the rest will be read (most significative) and then the divisible amounts
 		// as blocks of DIGITS_PER_PART characters (least significative).
 		int quotant = (int) (input.length()/DIGITS_PER_PART);
 		int rest = (int) (input.length()%DIGITS_PER_PART);
 		
 		//System.out.println("input length = "+input.length()+" quotant = "+quotant+" and rest = "+rest);
 		
 		String aux = "";
 		for(int i=0; i<rest; ++i)
 		{
 			aux += input.charAt(i);
 		}
 		//System.out.println(aux);
 		if(aux != "")
 		{
 			data.add(new Integer(Integer.parseInt(aux)));
 		}
 		
 		int offset = rest;
 		for(int j=0; j<quotant; ++j)
 		{
 			aux = "";
 			for(int i=offset; i<offset+DIGITS_PER_PART; ++i)
 			{
 				aux += input.charAt(i);
 			}
 			//System.out.println(aux);
 			data.add(new Integer(Integer.parseInt(aux)));
 			
 			offset += DIGITS_PER_PART;
 		}
 		
 		//System.out.println("Vector size : "+data.size());
 	}
 	
 	public BigInt()
 	{
 		data = new Vector<Integer>(0);
 		isNegative = false;
 	}
 	
 	public BigInt(BigInt data)
 	{
 		// TODO Copy constructor, implement if needed
 	}
 	
 	@Override
 	public String toString()
 	{
 		String s = "";
 		boolean print0 = false;
 		
 		if(isNegative)
 		{
 			s += "-";
 		}
 		else
 		{
 			s += " ";
 		}
 		for(int i=0; i<data.size(); ++i)
 		{
 			// Fill with zeroes until the maximum number of digits of any given number in our base.
 			// The first element is exempt from this rule.
 			if(i != 0)
 			{
 				int digits;
 				if(data.elementAt(i) == 0)
 				{
 					digits = 1;
 				}
 				else
 				{
 					digits = (int)(Math.log10(data.elementAt(i))+1);
 				}
 				for(int j=digits; j<DIGITS_PER_PART /*&& print0*/; ++j)
 				{
 					s += "0";
 				}
 			}
 			
 			/*
 			if(this.data.elementAt(i) == 0 && !print0)
 			{
 				
 			}
 			else
 			{
 				print0 = true;
 				s += data.elementAt(i).toString();
 				//s += " ";
 			}
 			*/
 			
 			s += data.elementAt(i).toString();
 			//s += " ";
 		}
 		return s;
 	}
 	
 	/**
 	 * Sum two numbers with the School algorithm
 	 * @param data2 number added to "this"
 	 * @return BigInt with the sum
 	 */
 	public BigInt add(BigInt data2)
 	{
 		BigInt result;
 		
 		if(this.isNegative && !data2.isNegative)
 		{
 			this.isNegative = false;
 			result = data2.subtract(this);
 			this.isNegative = true;
 		}
 		else if(!this.isNegative && data2.isNegative)
 		{
 			data2.isNegative = false;
 			result = this.subtract(data2);
 			data2.isNegative = true;
 		}
 		else if(this.isNegative && data2.isNegative)
 		{
 			this.isNegative = false;
 			data2.isNegative = false;
 			result = this.addKernel(data2);
 			this.isNegative = true;
 			data2.isNegative = true;
 			result.isNegative = true;
 		}
 		else // !this.isNegative && !data2.isNegative
 		{
 			result = this.addKernel(data2);
 		}
 		
 		return result;
 	}
 	
 	private BigInt addKernel(BigInt data2)
 	{
 		// School algorithm
 		boolean done = false;
 		int index1 = this.data.size()-1;
 		int index2 = data2.data.size()-1;
 		long temp;
 		int int1, int2;
 		int carry = 0;
 		
 		BigInt result = new BigInt();
 		while(!done)
 		{
 			if(index1 < 0)
 			{
 				int1 = 0;
 			}
 			else
 			{
 				int1 = this.data.elementAt(index1);
 			}
 			if(index2 < 0)
 			{
 				int2 = 0;
 			}
 			else
 			{
 				int2 = data2.data.elementAt(index2);
 			}
 			
 			temp = int1 + int2 + carry;
 			
 			carry = (int) (temp/MAX_INTEGER_PER_PART);
 			result.data.add(new Integer((int) (temp%MAX_INTEGER_PER_PART)));
 			
 			/*
 			System.out.println("int1 = "+int1);
 			System.out.println("int2 = "+int2);
 			System.out.println("carry = "+carry);
 			System.out.println("quotant = "+result.toString());
 			*/
 			
 			--index1;
 			--index2;
 			if(index1 < 0 && index2 < 0)
 			{
 				done = true;
 			}
 		}
 		
 		// If once it's over, if there is some carry, add it in the next position.
 		if(carry != 0)
 		{
 			result.data.add(new Integer(carry));
 		}
 		
 		Collections.reverse(result.data);
 		return result;
 	}
 	
 	public boolean isGreaterThan(BigInt data2)
 	{
 		if(this.isNegative && !data2.isNegative)
 		{
 			return false;
 		}
 		else if(!this.isNegative && data2.isNegative)
 		{
 			return true;
 		}
 		else if(this.isNegative && data2.isNegative)
 		{
 			if(this.data.size() < data2.data.size())
 			{
 				return true;
 			}
 			else if(this.data.size() > data2.data.size())
 			{
 				return false;
 			}
 			else
 			{
 				// Equal size, check digit by digit
 				for(int i=0; i<this.data.size(); ++i)
 				{
 					if(this.data.elementAt(i) < data2.data.elementAt(i))
 					{
 						return true;
 					}
 					else if(this.data.elementAt(i) > data2.data.elementAt(i))
 					{
 						return false;
 					}
 				}
 				return false;
 			}
 		}
 		else if(!this.isNegative && !data2.isNegative)
 		{	
 			if(this.data.size() < data2.data.size())
 			{
 				return false;
 			}
 			else if(this.data.size() > data2.data.size())
 			{
 				return true;
 			}
 			else
 			{
 				// Equal size, check digit by digit
 				for(int i=0; i<this.data.size(); ++i)
 				{
 					if(this.data.elementAt(i) < data2.data.elementAt(i))
 					{
 						return false;
 					}
 					else if(this.data.elementAt(i) > data2.data.elementAt(i))
 					{
 						return true;
 					}
 				}
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 	/**
 	 * @post IF NUMBERS HAVE NO-USEFULL DIGIT (ON THE LEFT), IT WILL BE ERASED
 	 */
 
 	public BigInt subtract(BigInt data2)
 	{
 		BigInt result;
 		
 		this.cleanZeros();
 		data2.cleanZeros();
 		
 		if(this.isNegative && data2.isNegative)
 		{
 			this.isNegative = false;
 			data2.isNegative = false;
 			if(this.isGreaterThan(data2))
 			{
 				result = this.subtractKernel(data2);
 				result.isNegative = true;
 			}
 			else
 			{
 				result = data2.subtractKernel(this);
 				result.isNegative = false;
 			}
 			this.isNegative = true;
 			data2.isNegative = true;
 		}
 		else if(!this.isNegative && data2.isNegative)
 		{
 			data2.isNegative = false;
 			result = this.add(data2);
 			result.isNegative = false;
 			data2.isNegative = true;
 		}
 		else if(this.isNegative && !data2.isNegative)
 		{
 			this.isNegative = false;
 			result = this.add(data2);
 			result.isNegative = true;
 			this.isNegative = true;
 		}
 		else // if(!this.isNegative && !data2.isNegative) (both positive)
 		{
 			if(data2.isGreaterThan(this))
 			{
 				result = data2.subtractKernel(this);
 				result.isNegative = true;
 			}
 			else
 			{
 				result = this.subtractKernel(data2);
 				result.isNegative = false;
 			}
 		}
 		
 		// Eliminate zeroes on the left
 		for(int i=0; i<result.data.size()-1; ++i)
 		{
 			if(result.data.elementAt(i) == 0 && i==0)
 			{
 				result.data.remove(i);
 				--i;
 			}
 		}
 		
 		return result;
 	}
 	
 	private BigInt subtractKernel(BigInt data2)
 	{
 		// School algorithm
 		boolean done = false;
 		int index1 = this.data.size()-1;
 		int index2 = data2.data.size()-1;
 		long temp;
 		int int1, int2;
 		int carry = 0;
 		
 		BigInt result = new BigInt();
 		while(!done)
 		{
 			if(index1 < 0)
 			{
 				int1 = 0;
 			}
 			else
 			{
 				int1 = this.data.elementAt(index1);
 			}
 			if(index2 < 0)
 			{
 				int2 = 0;
 			}
 			else
 			{
 				int2 = data2.data.elementAt(index2);
 			}
 			
 			temp = int1 - int2 - carry;
 			
 			if(temp < 0)
 			{
 				temp = MAX_INTEGER_PER_PART + temp;
 				carry = 1;
 			}
 			else
 			{
 				carry = 0;
 			}
 			
 			result.data.add(new Integer((int) (temp)));
 			
 			/*
 			System.out.println("int1 = "+int1);
 			System.out.println("int2 = "+int2);
 			System.out.println("carry = "+carry);
 			System.out.println("quotant = "+result.toString());
 			*/
 			
 			--index1;
 			--index2;
 			if(index1 < 0 && index2 < 0)
 			{
 				done = true;
 			}
 		}
 		
 		Collections.reverse(result.data);
 		return result;
 	}
 	
 
 	public BigInt multiplySchool(BigInt data2)
 	{
 		//this.cleanZeros();
 		//data2.cleanZeros();
 		
 		BigInt result = this.multiplySchoolKernel(data2);
 		if( (this.isNegative && data2.isNegative) || (!this.isNegative && !data2.isNegative) )
 		{
 			result.isNegative = false;
 		}
 		else if( (!this.isNegative && data2.isNegative) || (this.isNegative && !data2.isNegative) )
 		{
 			result.isNegative = true;
 		}
 		return result;
 	}
 	
 	private BigInt multiplySchoolKernel(BigInt data2)
 	{
 		BigInt result = new BigInt();
 		long temp;
 		int carry = 0;
 		int resultIndex = 0;
 		int resultOffset = 0;
 		
 		// @pre Doesn't admit negative numbers
 		// result = this * data2
 		for(int i=data2.data.size()-1; i>=0; --i)
 		{
 			carry = 0;
 			resultIndex = 0;
 			for(int j=this.data.size()-1; j>=0; --j)
 			{
 				temp = ((long)(this.data.elementAt(j)) * (long)(data2.data.elementAt(i))) + (long)(carry);
 				//System.out.println("("+this.data.elementAt(j)+" * "+data2.data.elementAt(i)+") + "+carry+" = "+temp);
 				
 				if(result.data.size() <= resultIndex + resultOffset)
 				{
 					// If there's no result value in this position, store as much as possible.
 					// POSIBLE FAILURE WITH NEGATIVES NUMBERS. OR 0 NUMBERS.
 					result.data.add(new Integer((int) (temp%MAX_INTEGER_PER_PART)));
 					//System.out.println("Adding new "+(int) (temp%MAX_INTEGER_PER_PART)+" to the vector ("+result.data.lastElement()+")");
 				}
 				else
 				{
 					// If there was a result value in this position, add it to the result, and store as much as possible.
 					temp += result.data.elementAt(resultIndex + resultOffset);
 					result.data.set(resultIndex + resultOffset, new Integer((int) (temp%MAX_INTEGER_PER_PART)));
 					//System.out.println("Adding mod "+(int) (temp%MAX_INTEGER_PER_PART)+" to the vector ("+result.data.lastElement()+")");
 				}
 				
 				// Whatever the case, keep the rest for the next iteration.
 				carry = (int) (temp/MAX_INTEGER_PER_PART);
 				//System.out.println("Carry = "+(int) (temp/MAX_INTEGER_PER_PART));
 				
 				++resultIndex;
 			}
 			// If once it's over, if there is some carry, add it in the next position.
 			if(carry != 0)
 			{
 				if(result.data.size() <= resultIndex + resultOffset)
 				{
 					result.data.add(new Integer(carry));
 				}
 				else
 				{
 					temp = carry + result.data.elementAt(result.data.size()-1);
 					result.data.set(resultIndex + resultOffset, new Integer((int) (temp%MAX_INTEGER_PER_PART)));
 					carry = (int) (temp/MAX_INTEGER_PER_PART);
 					if(carry != 0)
 					{
 						result.data.add(new Integer(carry));
 					}
 				}
 			}
 			++resultOffset;
 		}
 		
 		Collections.reverse(result.data);
 		return result;
 	}
 	
 	/**
 	 * Calculate the exponent "m" that makes the expresion "2^m"
 	 * bigger than this.data.size()
 	 * @return Integer value of m
 	 */
 	int nextPow2(){
 		int m = 0;
 		int size = this.data.size();
 		boolean completed = false;
 		
 		while(!completed){
 			if(size <= (int)Math.pow(2.0, (double)m)){
 				completed = true;
 			}
 			else
 			{
 				m++;
 			}
 		}
 		return m;
 	}
 	
 	/**
 	 * Add left zeros this.data until this.data.size() is 2^m
 	 * @param m 
 	 */
 	@SuppressWarnings("unchecked")
 	void fillDigits(int m){
 		if (this.data.size() != Math.pow(2.0, (double) m)) {
 			BigInt reverse = this.clone();
 			Collections.reverse(reverse.data);
 
 			do{
 				reverse.data.add(0);
 			}while (reverse.data.size() != Math.pow(2.0, (double) m));
 
 			Collections.reverse(reverse.data);
 			this.data = (Vector<Integer>) reverse.data.clone();
 		}
 	}
 	
 	public BigInt multiplyKaratsuba(BigInt data2)
 	{
 		BigInt a = this.clone();
 		BigInt b = data2.clone();
 		BigInt result;
 		
 		int size1 = a.data.size();
 		int size2 = b.data.size();
 		int m = 0;
 		
 		if(size1 > size2)
 		{
 			m = a.nextPow2();
 		}
 		else{
 			m = b.nextPow2();
 		}
 		
 		a.fillDigits(m);
 		b.fillDigits(m);
 		
 		result = this.multiplyKaratsubaKernel(a, b, m);
 		return result;
 	}
 	
 	
 	/**
 	 * Extracts a subvector from a vector (index included)
 	 * @param init Init index
 	 * @param end Final index
 	 * @return Subvector
 	 */
 	public Vector<Integer> subVector(int init, int end){
 		Vector<Integer> aux = new Vector<Integer>();
 		
 		for(int i = init; i <= end; i++){
 			aux.add(this.data.elementAt(i));
 		}
 		
 		return aux;
 	}
 	
 	/**
 	 * Split the number and obtain his left part
 	 * @return BigInt 
 	 */
 	public BigInt leftSplit(){
 		BigInt result = new BigInt();
 		int size = this.data.size();
 		int end = size/2;
 		
 		if(end > 0)
 		{	
 			end = end - 1;
 		}
 		
 		result.data = this.subVector(0, end);
 		result.isNegative = false;
 		
 		//For avoid the case -0;
 		for(int i = 0; i < result.data.size(); i++){
 			if(result.data.elementAt(i) != 0){
 				result.isNegative = this.isNegative;
 			}
 		}
 		
 		return result;
 	}
 	
 	
 	
 	/**
 	 * Split the number and obtain his left part
 	 * @return BigInt 
 	 */
 	public BigInt rightSplit(){
 		BigInt result = new BigInt();
 		int size = this.data.size();
 		int begin = size/2;
 
 		result.data = this.subVector(begin, size-1);
 		result.isNegative = this.isNegative;
 		
 		return result;
 	}
 	
 	/**
 	 * Added times zeros in this.data 
 	 * @param times Zeros to be added
 	 * @return BigInt with "times" mores zeros in data than the original
 	 */
 	public BigInt multiplyShift(int times)
 	{
 		BigInt result = this.clone();
 		
 		for(int i = 0; i < times; i++){
 			result.data.add(0);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Clean left-zeros
 	 */
 	private void cleanZeros()
 	{
 		if(this.data.size() > 0)
 		{	
 			while (this.data.elementAt(0) == 0 && this.data.size() > 1) 
 			{
 				this.data.remove(0);
 			}
 		}
 	}
 	
 	/**
 	 * Unify the number of digits adding left-zeros
 	 * @param data2
 	 */
 	
 	private void unifyDigits(BigInt data2)
 	{
 		int size1 = this.data.size();
 		int size2 = data2.data.size();
 		
 		if(size1 > size2)
 		{
 			Collections.reverse(data2.data);
 			while(data2.data.size() < size1)
 			{
 				data2.data.add(0);
 			}
 			Collections.reverse(data2.data);
 		}
 		else
 		{
 			Collections.reverse(this.data);
 			while(this.data.size() < size2)
 			{
 				this.data.add(0);
 			}
 			Collections.reverse(this.data);
 		}	
 	}
 	
 	private boolean isZero()
 	{
 		for(int i = 0; i < this.data.size();i++)
 		{
 			if(this.data.elementAt(i) != 0)
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public BigInt multiplyKaratsubaKernel(BigInt a, BigInt b, int m)
 	{
 		if(m == 0){
 			return a.multiplySchool(b);
 		}
 		else
 		{	
 			//Cuando se parte todo debe tener el mismo signo.
 			BigInt a1 = a.leftSplit();
 			BigInt a0 = a.rightSplit();
 			BigInt b1 = b.leftSplit();
 			BigInt b0 = b.rightSplit();
 			
 			//if((a1.data.elementAt(0) < 0 || a1.isNegative) && !a0.isZero() ){ a0.isNegative = true;}
 			//if((b1.data.elementAt(0) < 0 || b1.isNegative) && !b0.isZero() ){ b0.isNegative = true;}
 			
 			BigInt a1_sub_a0 = a1.subtract(a0);
 			BigInt b0_sub_b1 = b0.subtract(b1);
 			
 			a1_sub_a0.fillDigits(m-1);
 			b0_sub_b1.fillDigits(m-1);
 			
 			//SERIA POSIBLE OPTIMIZAR ESTAS CUATRO LLAMADAS, HACIENDO QUE EL SUBTRACT NO MODIFICARA LAS VARIABLES DE ENTRADA
 			a1.fillDigits(m-1);
 			b1.fillDigits(m-1);
 			b0.fillDigits(m-1);
 			a0.fillDigits(m-1);
 			
 			BigInt t1 = this.multiplyKaratsubaKernel(a1, b1, m-1);
 			BigInt t2 = this.multiplyKaratsubaKernel(a1_sub_a0, b0_sub_b1, m-1);
 			BigInt t3 = this.multiplyKaratsubaKernel(a0, b0, m-1);
 			
 			BigInt t1_t2_t3 = t1.add(t2);
 			t1_t2_t3 = t1_t2_t3.add(t3);
 			
 			BigInt result1 = t1.multiplyShift((int)Math.pow(2.0,(double)m));
 			BigInt result2 = t1_t2_t3.multiplyShift((int)Math.pow(2.0,(double)(m-1)));
 			
 			BigInt result = result1.add(result2);
 			result = result.add(t3);
 			
 			return result;
 		}
 	}
 	
 	public BigInt multiplyModular(BigInt data2)
 	{
 		return null;
 	}
 	
 	public BigInt divisionSchool(BigInt divisor, BigInt quot)
 	{
 		BigInt data1 = this.clone();
 		BigInt data2 = divisor.clone();
 		BigInt result;
 		
 		if(quot == null){
 			quot = new BigInt("0");
 		}
 		
		if(this.isGreaterOrEqualThan(divisor))
 		{
 			if((data2.data.size()-2) >= 0){
 				result = data1.divisionSchoolKernel(data2, quot);
 			}
 			else{
 				result = data1.divisionSchoolBaseCase(data2, quot);
 			}
 		}
 		else{
 			result = this.clone();
 		}
 		
 		return result;
 	}
 	
 	public BigInt elementAsBigInt(int index)
 	{
 		BigInt result = new BigInt();
 		result.isNegative = this.isNegative;
 		result.data.add(this.data.elementAt(index));
 		return result;
 	}
 	
 	public boolean isGreaterOrEqualThan(BigInt data2)
 	{
 		if(this.isNegative && !data2.isNegative)
 		{
 			return false;
 		}
 		else if(!this.isNegative && data2.isNegative)
 		{
 			return true;
 		}
 		else if(this.isNegative && data2.isNegative)
 		{
 			if(this.data.size() < data2.data.size())
 			{
 				return true;
 			}
 			else if(this.data.size() > data2.data.size())
 			{
 				return false;
 			}
 			else
 			{
 				// Equal size, check digit by digit
 				for(int i=0; i<this.data.size(); ++i)
 				{
 					if(this.data.elementAt(i) < data2.data.elementAt(i))
 					{
 						return true;
 					}
 					else if(this.data.elementAt(i) > data2.data.elementAt(i))
 					{
 						return false;
 					}
 				}
 				return true;
 			}
 		}
 		else if(!this.isNegative && !data2.isNegative)
 		{	
 			if(this.data.size() < data2.data.size())
 			{
 				return false;
 			}
 			else if(this.data.size() > data2.data.size())
 			{
 				return true;
 			}
 			else
 			{
 				// Equal size, check digit by digit
 				for(int i=0; i<this.data.size(); ++i)
 				{
 					if(this.data.elementAt(i) < data2.data.elementAt(i))
 					{
 						return false;
 					}
 					else if(this.data.elementAt(i) > data2.data.elementAt(i))
 					{
 						return true;
 					}
 				}
 				return true;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/** Esto lo dejo aqu por rigor histrico. Pero no se usar nunca **/
 	public BigInt divisionSubstract(BigInt divisor, BigInt quot){
 		BigInt data1 = this.clone();
 		BigInt data2 = divisor.clone();
 		BigInt zero = new BigInt("0");
 		
 		if(divisor.isGreaterThan(this)){
 			quot = zero;
 			return this;
 		}
 		else{
 			Integer counter = new Integer(0);
 			
 			while(data1.isGreaterOrEqualThan(data2)){
 				data1 = data1.subtract(data2);
 				counter++;
 			}
 			
 			quot.data.clear();
 			quot.data.add(counter);
 			return data1;
 		}
 	}
 	
 	private BigInt divisionSchoolBaseCase(BigInt divisor, BigInt quot)
 	{
 		int m = this.data.size();
 		int n = divisor.data.size();
 		BigInt r = new BigInt("0");
 		BigInt quotAux = new BigInt("0");
 		BigInt aux;
 		int sizeA = this.data.size();
 		int sizeB = divisor.data.size();
 		int sizeR = 0;
 		int count = 0;
 		Double partialD = new Double(0);
 		Double partiald = new Double(0);
 		Double partialR = new Double(0);
 		
 		
 		
 		r = this.elementAsBigInt(0);
 		if(divisor.isGreaterThan(r)){
 			r = r.multiplyShift(1).add(this.elementAsBigInt(1));
 			count = 1;
 		}
 		
 		for(int j = m-n-count; j >= 0; j--)
 		{
 			if(j != m-n-count){
 				r = r.multiplyShift(1).add(this.elementAsBigInt((sizeA-1) - j));
 			}
 			
 			if(r.isGreaterOrEqualThan(divisor))
 			{
 				r.cleanZeros();
 				sizeR = r.data.size();
 				if(r.data.size() > n){
 					quotAux = r.elementAsBigInt((sizeR-1) - n).multiplyShift(1).add(r.elementAsBigInt((sizeR-1) - (n-1)));
 				}
 				else{
 					quotAux = r.elementAsBigInt((sizeR-1) - (n-1));
 				}
 				
 				if(quotAux.data.size() != 1){
 					
 					partialD = ((r.elementAsBigInt((sizeR-1) - n).multiplyShift(1)).add(r.elementAsBigInt((sizeR-1) - (n-1)))).toDouble();
 					partiald = divisor.elementAsBigInt((sizeB-1) - (n-1)).toDouble();
 					partialR = partialD / partiald;
 					quotAux.data.clear();
 					quotAux.data.add(partialR.intValue());
 				
 				}
 				else{
 					quotAux.data.setElementAt(quotAux.data.elementAt(0) / divisor.data.elementAt((sizeB-1) - (n-1)),0);
 				}
 				aux = divisor.multiplySchool(quotAux);
 				while(aux.isGreaterThan(r))
 				{
 					aux = new BigInt("1");
 					quotAux = quotAux.subtract(aux);
 				}
 				aux = divisor.multiplySchool(quotAux);
 				r = r.subtract(aux);
 				quot.data.add(quotAux.data.elementAt(0));
 			}
 			else
 			{
 				quot.data.add(0);
 			}
 			
 		}	
 		
 		if(r.isGreaterOrEqualThan(divisor)){
 			BigInt partialResult = new BigInt("0");
 		    r = r.divisionSchool(divisor, partialResult);
 		    quot.data = (quot.add(partialResult)).data;
     
 		}
 		
 		return r;
 	}
 	
 	private BigInt divisionSchoolKernel(BigInt divisor, BigInt quot)
 	{
 		int m = this.data.size();
 		int n = divisor.data.size();
 		boolean completed = false;
 		BigInt r = new BigInt("0");
 		BigInt quotAux = new BigInt("0");
 		BigInt aux;
 		int i = m-1;
 		int l = n-2;
 		int sizeA = this.data.size();
 		int sizeB = divisor.data.size();
 		int sizeR = 0;
 		
 		Double partialD = new Double(0);
 		Double partiald = new Double(0);
 		Double partialR = new Double(0);
 		
 		
 		while(!completed)
 		{
 			r = r.add(this.elementAsBigInt((sizeA-1) - i).multiplyShift(l));
 			if(i == (m-n+1))
 			{
 				completed = true;
 			}
 			i--; 
 			l--;
 		}
 		
 		for(int j = m-n; j >= 0; j--)
 		{
 			r = r.multiplyShift(1).add(this.elementAsBigInt((sizeA-1) - j));
 			if(r.isGreaterOrEqualThan(divisor))
 			{
 				r.cleanZeros();
 				sizeR = r.data.size();
 				if(r.data.size() > n){
 					quotAux = r.elementAsBigInt((sizeR-1) - n).multiplyShift(1).add(r.elementAsBigInt((sizeR-1) - (n-1)));
 				}
 				else{
 					quotAux = r.elementAsBigInt((sizeR-1) - (n-1));
 				}
 				
 				if(quotAux.data.size() != 1){
 					
 					partialD = ((r.elementAsBigInt((sizeR-1) - n).multiplyShift(1)).add(r.elementAsBigInt((sizeR-1) - (n-1)))).toDouble();
 					partiald = divisor.elementAsBigInt((sizeB-1) - (n-1)).toDouble();
 					partialR = partialD / partiald;
 					quotAux.data.clear();
 					quotAux.data.add(partialR.intValue());
 					
 				}
 				else{
 					quotAux.data.setElementAt(quotAux.data.elementAt(0) / divisor.data.elementAt((sizeB-1) - (n-1)),0);
 				}
 				aux = divisor.multiplySchool(quotAux);
 				while(aux.isGreaterThan(r))
 				{
 					aux = new BigInt("1");
 					quotAux = quotAux.subtract(aux);
 					aux = divisor.multiplySchool(quotAux);
 				}
 				aux = divisor.multiplySchool(quotAux);
 				r = r.subtract(aux);
 				quot.data.add(quotAux.data.elementAt(0));
 			}
 			else
 			{
 				quot.data.add(0);
 			}
 			
 		}	
 		
 		if(r.isGreaterOrEqualThan(divisor)){
 			BigInt partialResult = new BigInt("0");
 		    r = r.divisionSchool(divisor, partialResult);
 		    quot.data = (quot.add(partialResult)).data;
     
 		}
 		
 		return r;
 	}
 	
 	
 	/**
 	 * Cast a BigInt WITH DATA.SIZE() <= 2 to a Double value.
 	 * @return a The double value of BigInt
 	 */
 	public Double toDouble(){
 		Double acumulator = new Double(0.0);
 		if(this.data.size() <= 2){
 			for(int i = 0; i < this.data.size(); i++){
 				acumulator = acumulator * this.MAX_INTEGER_PER_PART + this.data.elementAt(i);
 			}
 		}
 		return (double) acumulator;
 	}
 	
 	/**
 	 * Obtain the rest of the operation this / divisor. 
 	 * @param divisor 1 Digit divisor
 	 * @return Division's module
 	 */
 	public BigInt mod(BigInt divisor){
 		BigInt result = new BigInt();
 
 		Double doubleR = new Double(0);
 		Double doubleD = new Double(divisor.toDouble());
 		
 		for(int i = 0; i < this.data.size(); i++){
 			doubleR = doubleR * this.MAX_INTEGER_PER_PART + this.data.elementAt(i);
 			doubleR = doubleR % doubleD;
 		}
 		
 		result.data.add(doubleR.intValue());
 		return result;
 	}
 	
 	@Override
     @SuppressWarnings(value = "unchecked")
     protected BigInt clone(){
 		BigInt cloned = new BigInt();
 		cloned.data = (Vector<Integer>) this.data.clone();
 		cloned.isNegative = this.isNegative;
 		return cloned;
 	}
 
 	public boolean isNegative() 
 	{
 		return isNegative;
 	}
 
 	public void setIsNegative(boolean isNegative) 
 	{
 		this.isNegative = isNegative;
 	}
 	
 	
 	public static class ResultExtendedEuclidean
 	{
 		public BigInt d, u, v;
 		
 		public ResultExtendedEuclidean(BigInt d, BigInt u, BigInt v)
 		{
 			this.d = d;
 			this.u = u;
 			this.v = v;
 		}
 	}
 	
 	public ResultExtendedEuclidean extendedEuclidean(BigInt data2)
 	{
 		BigInt d, u, v, rest, quotant = null;
 		
 		BigInt a = this.clone();
 		BigInt b = data2.clone();
 		
 		BigInt u1 = new BigInt();
 		u1.data.add(1);
 		BigInt v1 = new BigInt();
 		v1.data.add(0);
 		BigInt u2 = new BigInt();
 		u2.data.add(0);
 		BigInt v2 = new BigInt();
 		v2.data.add(1);
 		
 		while( !b.isZero() )
 		{
 			quotant = new BigInt();
 			rest = a.divisionSchool(b, quotant);
 			a = b.clone();
 			b = rest.clone();
 			u = u1.subtract(quotant.multiplySchool(u2));
 			v = v1.subtract(quotant.multiplySchool(v2));
 			u1 = u2.clone();
 			u2 = u.clone();
 			v1 = v2.clone();
 			v2 = v.clone();
 		}
 		
 		return new ResultExtendedEuclidean(a, u1, v1);
 	}
 	
 	
 }
 
 
