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
 	*The result of an arithmetic praxis
 	*/
 	protected int apotelesma;
 
 	/**
 	*Constructor method that creates a random arithmetic praxis eg 2+2
 	*@param maxNum: The maximum value that can have a number as Operator.
  	*/
 
 	public Praxis(int maxNum)
 	{
 		Random r=new Random();
 		telestis1=r.nextInt(maxNum);
 		telestis2=r.nextInt(maxNum);
 		doPraxis();
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
 	public boolean checkResult(int apotelesma)
 	{
 		return this.apotelesma==apotelesma?true:false;
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
 		return apotelesma;
 	}
 	
 	/**
 	*We check if an arithmetic praxis equals with an another one
 	*/
 	public boolean equals(Praxis other)
 	{
 		if(((telestis1==other.getTelestis1())&& (telestis2==other.getTelestis2()))||
 		   ((telestis2==other.getTelestis1())&&(telestis1==other.getTelestis2())))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
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
 	
 }
