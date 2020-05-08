 /**
 *GUMA a simple math game for elementary school students
 *	Copyright (C) 2011-1012  Dimitrios Desyllas (pc_magas)
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *Contact with me by main at thes address: pc_magas@yahoo.gr
 */
 
 package guma.ui.simulator;
 
 public class SimulatorUI
 {
 	/**
 	*Shows The current status message from the simulation
 	*/
 	private String message="";
 	
 	/**
 	*Shows the list of caries during an arithmetic operation
 	*/
	private String carryList="";
 	
 	/**
 	*Stores the operation as String
 	*/
 	private String operation="";
 	
 	/**
 	*Shows if you can do the next step of the simulation
 	*/
 	private boolean next=false;
 	
 	/**
 	*Setting a value to carryList
 	*@param c: the current carry of operation 
 	*/
 	
 	public void setCarryList(int c)
 	{
 		//Strikingthrough the previous carries	
 		carryList.replace("->","<del>");
 		carryList.replace("<br>","<br></del>");
 		
 		if(c!=0)
 		{
 			carryList+="->"+c+"<br>";
 		}
 		else
 		{
 			carryList+="-><br>";
 		}
 	}
 	
 	/**
 	*Setting a value to carryList
 	*/
 	public void setCarryList(String carryList)
 	{
 		this.carryList=carryList;
 	}
 	
 	/**
 	*Simple Constructor method
 	*/
 	public SimulatorUI()
 	{
 	}
 	
 	
 	/**
 	*Advanced Simulator method
 	*@param message: the message we get from simulator
 	*@param carryList: the current carry of operation 
 	*@param operation: the operation as string that we get from simulator
 	*@param next: tells  on ui if we have next step or not
 	*/
 	public SimulatorUI(String message, String carryList, String operation,boolean next)
 	{
 		System.out.println("UI Message: "+message);
 		setMessage(message);
 		setCarryList(carryList);
 		setOperation(operation);
 		setNext(next);
 	}
 	
 	/**
 	*Advanced Simulator method 2
 	*@param message: the message we get from simulator
 	*@param carry: the current carry of operation 
 	*@param operation: the operation as string that we get from simulator
 	*@param next: tells  on ui if we have next step or not
 	*/
 	public SimulatorUI(String message, int carry, String operation,boolean next)
 	{
 		setMessage(message);
 		setCarryList(carry);
 		setOperation(operation);
 		setNext(next);
 	}
 	
 	
 	/**
 	*Cloning Constructor Method
 	*@param other:The simulator UI we want to clone
 	*/
 	public SimulatorUI(SimulatorUI other)
 	{
 		this(other.getMessage(),other.getCarryList(),other.getOperation(),other.getNext());
 	}
 	
 	
 	/**
 	*Setting the message that we get from simulator
 	*@param message: the message we get from simulator
 	*/
 	public void setMessage(String message)
 	{
 		this.message=message;
 	}
 	
 	/**
 	*We set the operation as String form as we get it from simulator
 	*@param operation: the operation as string that we get from simulator
 	*/
 	public void setOperation(String operation)
 	{
 		this.operation=operation;
 	}
 	
 	/**
 	*Usefull for geting the operation ans importing into the Uset Interface
 	*/
 	public String getOperation()
 	{
 		return operation;
 	}
 	
 	/**
 	*Usefull to get the message that simulator returned
 	*/
 	public String getMessage()
 	{
 		return message;
 	}
 	
 	/**
 	*Getting the carryList containing the carries of the current arithmetc operation
 	*/
 	public String getCarryList()
 	{
 		return carryList;
 	}
 	
 	/**
 	*Sets a value to next
 	*/
 	public void setNext(boolean value)
 	{
 		next=value;
 	}
 	
 	/**
 	*Gets if we can go to the next step
 	*/
 	public boolean getNext()
 	{
 		return next;
 	}
 	
 	/**
 	*Toggles next from true to false
 	*/
 	public void toggleNext()
 	{
 		setNext(!next);
 	}
 	
 	/**
 	*Cloning the current class
 	*/
 	public SimulatorUI clone()
 	{
 		return new SimulatorUI(this);
 	}
 	
 	/**
 	*Updates Simulator UI
 	*@param message: the message we get from simulator
 	*@param carry: the current carry of operation 
 	*@param operation: the operation as string that we get from simulator
 	*@param next: tells  on ui if we have next step or not
 	*/
 	public void update(String message, int carry, String operation,boolean next)
 	{
 		setMessage(message);
 		setCarryList(carry);
 		setOperation(operation);
 		setNext(next);
 	}
 	
 	/**
 	*Updates Simulator UI
 	*@param message: the message we get from simulator
 	*@param carry: the current carry of operation 
 	*@param operation: the operation as string that we get from simulator
 	*@param next: tells  on ui if we have next step or not
 	*/
 	public void update(String message, String carryList, String operation,boolean next)
 	{
 		setMessage(message);
 		setCarryList(carryList);
 		setOperation(operation);
 		setNext(next);
 	}
 }
