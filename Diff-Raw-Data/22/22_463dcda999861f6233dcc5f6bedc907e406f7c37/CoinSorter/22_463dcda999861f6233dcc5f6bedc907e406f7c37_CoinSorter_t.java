 package uebung08;
 
 public class CoinSorter {
 	private int[] validValues;
 	private int[] coinCount;
 	private int transactionCount = 0;
 	
 	private int unknownCoinsValue = 0;
 	private int unknownCoinsCount = 0;
 	
 	public CoinSorter (int[] validValues) {
 		this.validValues = validValues;
 		this.coinCount = new int[validValues.length];
 	}
 	
 	protected boolean isValidValue(int value) {
 		for (int i=0; i<validValues.length && validValues[i] <= value; i++) {
 			if (value == validValues[i]) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
	protected int insertCoin(Coin coin) {
 		if (isValidValue(coin.getValue())) {
			return coin.getValue();
 		}
		return 0;
 	}
 	
 	public int insertCoins(Coin[] coins) {
 		if (coins == null) {
 			return 0;
 		}
		int countedSum = 0;
 		for (int i=0; i<coins.length; i++) {
			countedSum += insertCoin(coins[i]);
 		}
		if (countedSum > 0) {
 			transactionCount++;
 		}
		return countedSum;
 	}
 	
 	public int getNumberOfCoinsWithValue(int value) throws Exception {
 		for (int i=0; i<validValues.length && validValues[i] <= value; i++) {
 			if (validValues[i] == value) {
 				return coinCount[i];
 			}
 		}
 		throw new Exception("Sorry, Falschgeld haben wir nicht ;-)");
 	}
 	
 	public int getNumberOfUnknownCoins() {
 		return unknownCoinsCount;
 	}
 	
 	public int getSum() {
 		int sum = unknownCoinsValue;
 		for (int i=0; i<validValues.length; i++) {
 			sum += validValues[i] * coinCount[i];
 		}
 		return sum;
 	}
 	
 	public int getTransactionCount() {
 		return transactionCount;
 	}
 	
 	public String toString() {
 		String representation = "";
 		for (int i=0; i < validValues.length; i++) {
 			try {
 				representation = representation + getNumberOfCoinsWithValue(validValues[i]) + "x " + validValues[i] + "\n";
 			} catch (Exception e) {
 				// If this ever happens, something _really_ terrible happend *G*
 			}
 		}
 		representation = representation + getNumberOfUnknownCoins() + " unknown coins\n";
 		representation = representation + "Total value: " + getSum();
 		return representation;
 	}
 }
