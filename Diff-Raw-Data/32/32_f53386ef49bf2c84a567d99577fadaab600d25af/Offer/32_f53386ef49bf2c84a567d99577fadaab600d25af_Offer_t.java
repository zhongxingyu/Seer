 import java.util.Date;
import java.util.Map;
 
 /**
  * @author Jarosław Szczęśniak
  */
 public class Offer {
 	
	private Map<Good, Date> goodsIn;
	private Map<Good, Date> goodsOut;
 	
	public Map<Good, Date> getGoodsIn() {
		return goodsIn;
 	}
	public void setGoodsIn(Map<Good, Date> goodsIn) {
		this.goodsIn = goodsIn;
 	}
	public Map<Good, Date> getGoodsOut() {
		return goodsOut;
 	}
	public void setGoodsOut(Map<Good, Date> goodsOut) {
		this.goodsOut = goodsOut;
	}	
 	
 }
