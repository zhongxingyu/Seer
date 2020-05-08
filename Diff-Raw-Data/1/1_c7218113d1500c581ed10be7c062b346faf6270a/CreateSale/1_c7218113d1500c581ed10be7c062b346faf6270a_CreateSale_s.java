 package springapp.fitnesse;
 

 import springapp.domain.Sale;
 
 public class CreateSale extends SpringFitnesseFixture {
 
 	private Sale sale;
 	private String salesAssistant;
 	private String timeStamp;
 	private String tillId;
 
 	public void setSalesAssistant(String value) {
 		salesAssistant = value;
 	}
 
 	public void setTimeStamp(String value) {
 		timeStamp = value;
 	}
 
 	public void setTillId(String value) {
 		tillId = value;
 	}
 
 	public long saleId() {
 		return sale.getId();
 	}
 
 	public void execute() {
 		sale = new Sale(salesAssistant, timeStamp, tillId);
 		saleService.openSale(sale);
 	}
 
 }
