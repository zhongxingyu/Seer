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
 
 /**
  * Refund Provides methods and attributes for processing the refund
  * transaction.
  * 
  * @author Jonathan Gillett
  * @author Daniel Smullen
  * @author Rayan Alfaheid
  */
 public class Refund implements Transaction
 {
     private String buyer;
     private String seller;
     private Double credit;
     private String transaction;
 
     /**
      * Constructor for the class. Accepts and populates class attributes with values passed in.
      * 
      * @param buyer String for the username of the buyer to refund credit to.
      * @param seller String for the username of the seller to refund credit from.
      * @param credit Dollar value for the amount of credit to refund.
      * @param transaction The transaction string for the refund.
      */
     public Refund(String buyer, String seller, Double credit, String transaction)
     {
         this.buyer = buyer;
         this.seller = seller;
         this.credit = credit;
         this.transaction = transaction;
     }
 
     /**
      * Performs the refund transaction. Refunds the credit specified to the
      * buyer specified, and deducts that amount from the original seller.
      * 
      * @param cua A reference to the CurrentUserAccounts object used by the back-end.
      * @param atf A reference to the AvailableTickets object used by the back-end.
      * 
      * @throws FailedConstraint Throws a failed constraint under the following circumstances:
      * 			<br>If the user name specified does not exist.
      * 			<br>If the amount of credit added results in a credit overflow for the buyer.
      * 			<br>If the amount of credit added will result in a credit underflow for the seller.
      * 
      * @see Transaction#execute(CurrentUserAccounts, AvailableTickets)
      */
     public void execute(CurrentUserAccounts cua, AvailableTickets atf) throws FailedConstraint
     {
     	if (cua.hasUser(buyer) && cua.hasUser(seller))
     	{
     		if (cua.getUser(buyer).getCredit() + credit < 999999.99)
     		{
    			if (cua.getUser(seller).getCredit() - credit > 0.00)
     			{
     				cua.getUser(buyer).setCredit(cua.getUser(buyer).getCredit() + credit);
     				cua.getUser(seller).setCredit(cua.getUser(seller).getCredit() - credit);
     			}
     			else
     			{
     				throw new FailedConstraint(ExceptionCodes.USER_CREDIT_NEGATIVE, transaction);
     			}
     		}
     		else 
     		{
     			throw new FailedConstraint(ExceptionCodes.USER_CREDIT_OVERFLOW, transaction);
     		}
     	}
     	else
     	{
     		throw new FailedConstraint(ExceptionCodes.UNKNOWN_USER, transaction);
     	}
     }
 
 	/* (non-Javadoc)
 	 * @see Transaction#getTransaction()
 	 */
 	public String getTransaction() {
 		return transaction;
 	}
 
 }
