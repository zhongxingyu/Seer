 /**
  * Swift Ticket -- Back End
  *
  * Copyright (C) 2013, Jonathan Gillett, Daniel Smullen, and Rayan Alfaheid
  * All rights reserved.
  *
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class BackEnd
 {
 
 	/**
 	 * 
 	 */
 	public BackEnd()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 	    if (args.length < 3)
 	    {
 	        System.err.println("Usage:   java BackEnd [current user accounts] [available tickets] "
                              + "[daily transactions directory]");
 	        System.err.println("Example: java BackEnd accounts.cua tickets.atf transactions/");
 	        
 	        System.exit(1);
 	    }
 		try
         {
             AvailableTickets availableTickets = new AvailableTickets(args[0]);
             System.out.println("Current user accounts file read successfully.");
             availableTickets.displayTickets();
             
             CurrentUserAccounts currentAccounts = new CurrentUserAccounts(args[1]);
             System.out.println("Available tickets file read successfully.");
            currentAccounts.diplayUsers();
             
             DailyTransactions transactions = new DailyTransactions(args[2]);
             System.out.println("Daily transactions files read successfully.");
             transactions.displayTransactions();
             
             /* Write the data in memory to file */
             availableTickets.write();
             currentAccounts.write();
         }
         catch (FatalError e)
         {
             System.err.println(e.getMessage());
             System.err.println("Cause of Error: " + e.getCause().getMessage());
             System.exit(1);
         }
 	}
 }
