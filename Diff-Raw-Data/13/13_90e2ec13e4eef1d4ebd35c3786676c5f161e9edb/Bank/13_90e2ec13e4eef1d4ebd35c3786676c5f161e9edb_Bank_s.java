 package org.ojim.logic.accounting;
 
 public class Bank implements IMoneyPartner {
 
 	private int balance;
 	
 	public Bank() {
 		// All available amount of money!
 		//TODO: Finish this!
 		this.balance = 1000000;
 	}
 	
 	@Override
 	public void transferMoney(int amount) {
 		this.balance += amount;
 	}
 
 	/**
	 * Transfers the money amount from player1 to player2. Information: If the
	 * amount is negative the player1 will get cash and the player2 has to pay
 	 * it.
 	 * 
	 * @param player1
	 *            The money of this player will be decreased my the amount.
	 * @param player2
	 *            The money of this player will be increased my the amount.
 	 * @param amount
 	 *            The money amount.
 	 */
 	//TODO: Move to a better position;
 	public static void exchangeMoney(IMoneyPartner payer, IMoneyPartner payee, int amount) {
 		payer.transferMoney(-amount);
 		payee.transferMoney(amount);
 	}
 
 }
