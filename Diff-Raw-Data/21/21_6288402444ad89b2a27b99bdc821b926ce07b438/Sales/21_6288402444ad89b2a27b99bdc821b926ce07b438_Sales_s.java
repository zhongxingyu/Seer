 class Sales{
 	float totalSalesAmount;
 	float salesAmount;
 	int productCounter;
 	int totalSells;
 	float unitBuyPrice;
 	float unitSellPrice;
 	static float totalProfit;
 	
 	//@ Set methods 
 	void addProduct(int quantity){
 		this.productCounter += quantity;
 		
 	}
 	
 	void doSells(int quantitySold){
 		this.productCounter -= quantitySold;
 		this.salesAmount = unitSellPrice * quantitySold;
 		this.totalSalesAmount += salesAmount;
 		this.totalSells += quantitySold;
 	}
 	
 	void setUnitBuyPrice(float newPrice){
 		this.unitBuyPrice = newPrice;
 	}
 	
 	void setUintSellPrice(float newPrice){
 		this.unitSellPrice = newPrice;
 		
 	}
 	
 	
 	//@ Show or get methods
 	void showBills(){
 		System.out.println("Unit sell price     : "+this.unitSellPrice);
 		System.out.println("Sales Amount        : "+this.salesAmount);
 	}
 	
 	void showTotalSells(){
 		System.out.println("Total Sells quntity : "+this.totalSells);
		System.out.println("Total Sells amount  : "+this.totalSalesAmount)
 	}
 	
 	void showTotalProfits(){
		this.totalProfit += (this.unitSellPrice - this.unitBuyPrice) * this.salesAmount;
 		System.out.println("Total Profit        : "+this.totalProfit);
 	}
 	
 
 	
 	//@main methods
 	
 	public static void main(String args[]){
		System.out.println("Sales class is working");
 	}
}
