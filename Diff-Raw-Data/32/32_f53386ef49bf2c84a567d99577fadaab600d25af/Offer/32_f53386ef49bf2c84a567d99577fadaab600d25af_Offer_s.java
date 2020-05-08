 import java.util.Date;
 
 /**
  * @author Jarosław Szczęśniak
  */
 public class Offer {
 	
	private Good goodIn;
	private Good goodOut;
	private Date dueDate;
 	
	public void setGoodsIn(Good good) {
		this.goodIn = good;
 	}
	
	public Good getGoodIn() {
		return this.goodIn;
	}
	
	public Good getGoodOut() {
		return this.goodOut;
 	}
	
	public Date getDueDate() {
		return this.dueDate;
 	}
 	
 }
