 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2011-1012  Dimitrios Desyllas (pc_magas)
 *
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 
 package guma.arithmetic;
 
 import java.util.Random;
 import java.io.Serializable;
 import java.util.Arrays;
 
 /**
 *A class that simulates a basic Arithmetic Praxis
 */
 public abstract class Praxis implements Serializable 
 {
 	/**
 	*The first operator of an arithmatic praxis. In case of substraction can be the second operator.
 	*/
 	private int telestis1;
 	/**
 	*The second Operator of a arithmetic praxis.  In case of substraction can be the first operator.
 	*/
 	private int telestis2;
 
 	/**
 	*The results of an arithmetic praxis
 	*/
 	protected int[] apotelesma=new int[2];
 
 	/**
 	This variable helps us to count how many resurs are required in order to check oif this arithmetic praxis is correct
 	*/
 
 	protected int results=1;
 
 	/**
 	*The symbol of ADDING
 	*/
 	public static final char ADDING='+';
 	
 	/**
 	*The symbol of Substraction
 	*/
 	public static final char SUBSTRACTION='-';
 
 	/**
 	*The symbol of multiplication
 	*/
 	public static final char MULTIPLICATION='*';
 
 	/**
 	*The symbol of division
 	*/
 	public static final char DIVISION='/';
 
 	/**
 	*Modulo Position
 	*/
 	public static final int MODULO_POS=1;
 	
 	/**
 	*Returns the praxis type
 	*/	
 	protected char praxistype;
 	
 	/**
 	*Constructor method that creates a random arithmetic praxis eg 2+2
 	*@param maxNum: The maximum value that can have a number as Operator.
  	*/
 
 	public Praxis(int maxNum)
 	{
 		Random r=new Random();
 		apotelesma=new int[results];
 		setTelestes(r.nextInt(maxNum),r.nextInt(maxNum));
 	}
 
 	/**
 	*Constructor method that creates a random arithmetic praxis eg. 2+2,5-3,12/3 etc etc
 	*The operators will get values : minNum<=operator<=maxNum
 	*@param maxNum: The maximum value that can have a number as operator.
 	*@param minNUM: The minumum value that can have a number as operator.
  	*/
 
 	public Praxis(int maxNum,int minNum,int results)
 	{
 		Random r=new Random();
 		setTelestes(r.nextInt(maxNum-minNum-1)+1,r.nextInt(maxNum-minNum-1)+1);
 		this.results=results;
 	}
 	
 	/**
 	*Constructor methos that creates a basic arithmetic praxis with operetors telestis1 and telestis2
 	*@param telestis1: The first operator of an arithmetic praxis
 	*@param telestis2: The second operator of an arithmetic praxis 
 	*/
 	
 	public Praxis(int telestis1, int telestis2)
 	{
 		setTelestes(telestis1,telestis2);
 	}
 	
 	/**
 	*Compares and tells us if thew giver result is correct
 	*@param apotelesma: The result that we want to check if it is correct
 	*/
 	public boolean checkResult(int[] apotelesma)
 	{
 		
 		return (getWrongResult(apotelesma)<0)?true:false;
 	}
 
 	/**
 	*Returns the position of the wrong result
 	*@param apotelesma: The results that we want to search the wrong one
 	*/
 	public int getWrongResult(int[] apotelesma)
 	{
 		for(int i=0;i<results;i++)
 		{
 			if(this.apotelesma[i]!=apotelesma[i])
 			{
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	*With this method we set the telestes od an arithmetic praxis
 	*@param telestis1: The first operator of an arithmetic praxis
 	*@param telestis2: The second operator of an arithmetic praxis 
 	*/
 	public void setTelestes(int telestis1,int telestis2)
 	{
 		this.telestis1=telestis1;
 		this.telestis2=telestis2;
 		doPraxis();
 	}
 	
 	/**
 	*With this method we get the first parameter of an arithmetic praxis
 	*/
 	public int getTelestis1()
 	{
 		return telestis1;
 	}
 	
 	/**
 	*With this method we get the second parameter of an arithmetic praxis
 	*/
 	public int getTelestis2()
 	{
 		return telestis2;
 	}
 
 	/**
 	*With this we get the result of an arithmetic praxis
 	*/
 	public int getApotelesma()
 	{
 		return apotelesma[0];
 	}
 	
 	/**
 	*Getting the results of the Game 
 	*/
 	public int[] getResultsVal()
 	{
 		 return Arrays.copyOf(apotelesma,apotelesma.length);
 	}
 
 	/**
 	*We check if an arithmetic praxis equals with an another one
 	*/
 	public boolean equals(Praxis other)
 	{
 		if(((telestis1==other.getTelestis1())&& (telestis2==other.getTelestis2())) && apotelesma[0]==other.getApotelesma())
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	/**
 	*This method returns the number ot the results required
 	*/
 
 	public int getResults()
 	{
 		return results;
 	}
 	
 	/**
 	*Returns the operationSymbol
 	*/
 	public char getPraxisType()
 	{
 		return praxistype;
 	}
 	
 	/**
 	*This method executes the arithmetic praxis 
 	*/
 	protected abstract void doPraxis();
 	
 	/**
 	*Return a string form of the Praxis
 	*/
 	public abstract String toString();
 
 	/**
 	*Returns a string form of the praxis Including the result
 	*/
 	public abstract String toFullString();
 
 	/**
 	*Create the correct type of arithmetic Operation
 	*@param praxistype: This param tells us what kind of arithmetic Operation you want to have
 	*@param telestis1: This param tells you the first numper that will participate into this Operation
 	*@param telestis2: This param tells you the second number that will participate into this Operation
 	*/
 	public static Praxis makePraxis(char praxisType,int telestis1, int telestis2)
 	{
 		Praxis p=null;
 		switch(praxisType)
 		{
 			case Praxis.ADDING: 
 			p=new Prosthesis(telestis1,telestis2);	
 			break;
 			case Praxis.SUBSTRACTION:
 			p=new Afairesis(telestis1,telestis2);
 			break;
 			case Praxis.DIVISION:
 			p=new Diairesis(telestis1,telestis2);
 			break;
 			case Praxis.MULTIPLICATION:
 			p=new Multiplication(telestis1,telestis2);
 		}
 		return p;
 	}
 
 	/**
 	*Create the correct type of arithmetic Operation
 	*@param praxistype: This param tells us what kind of arithmetic Operation you want to have
 	*@param telestis1: This param tells you the first numbers that will participate into this Operation
 	*/
 	public static Praxis makePraxis(char praxisType,int maxNum)
 	{
 		Praxis p=null;
 		switch(praxisType)
 		{
 			case Praxis.ADDING: 
				p=new Prosthesis(maxNum+1);	
 				break;
 			case Praxis.SUBSTRACTION:
				p=new Afairesis(maxNum+1);
 				break;
 
 			case Praxis.DIVISION:
				p=new Diairesis(maxNum+1);
 				break;
 
 			case Praxis.MULTIPLICATION:
				p=new Multiplication(maxNum+1);
 		}
 		return p;
 	}
 }
