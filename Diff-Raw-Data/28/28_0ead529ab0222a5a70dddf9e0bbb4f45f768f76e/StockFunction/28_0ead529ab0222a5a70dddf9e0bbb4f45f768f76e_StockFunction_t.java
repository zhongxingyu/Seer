 package com.binar.generalFunction;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import com.avaje.ebean.EbeanServer;
 import com.binar.entity.DeletedGoods;
 import com.binar.entity.Goods;
 import com.binar.entity.GoodsConsumption;
 import com.binar.entity.GoodsReception;
 
 /* Kelas untuk mengatur aktif tidaknya fungsi-fungsi dalam manajemen stok (penerimaan, konsumsi dan penghapusan) */
 
 public class StockFunction {
 	
 	GeneralFunction function;
 	EbeanServer server;
 	AcceptancePyramid accept;
 	public StockFunction(GeneralFunction function) {
 		this.function=function;
 		this.server=function.getServer();
 		this.accept=function.getAcceptancePyramid();
 	}
 	
 	public enum StockType{
 		CONSUMPTION, RECEPTION, DELETION
 	}
 	/*
 	 * Jika input adalah reception, maka data reception dihari yang sama tidak dihitung
 	 * namun deletion dan consumption akan dihitung. Tidak berlaku untuk kombinasi lainnya.
 	 * 
 	 * Sementara, untuk konsumsi, jika time stampnya menunjukan waktu input paling baru, maka bisa di edit
 	 * 
 	 * Mengembalikan nilai true jika ada entry baru setelah entry yang di input
 	 * 
 	 * jika true: tidak bisa diedit jumlahnya dan tidak bisa dihapus
 	 */
 	public boolean isAnyNewestItem(GoodsConsumption consumption){
 		return isAnyNewestItem(consumption.getConsumptionDate(), consumption.getGoods().getIdGoods(), StockType.CONSUMPTION, consumption.getTimestamp());
 	}	
 	public boolean isAnyNewestItem(GoodsReception reception){
 		String idGoods=reception.getInvoiceItem().getPurchaseOrderItem().getSupplierGoods().getGoods().getIdGoods();
 		
 		return isAnyNewestItem(reception.getDate(), idGoods, StockType.RECEPTION, null);
 	}
 	public boolean isAnyNewestItem(DeletedGoods deletion){
 		return isAnyNewestItem(deletion.getApprovalDate(), deletion.getGoods().getIdGoods(), StockType.DELETION, null);
 	}
 	private boolean isAnyNewestItem(Date date, String idGoods, StockType stockType, Date consumptionTimeStamp){
 	
 		DateTime date2=new DateTime();
 		//Variabel untuk tanggal mulai hari. untuk menghitung juga data pada hari yang sama
 		DateTime dateTimeStartDay=new DateTime(date).withMillisOfDay(0);
 		//variabel untuk tanggal akhir hari. Agar tidak menghitung tanggal pada hari yang sama.
 		DateTime dateTimeEndDay=new DateTime(date).withMillisOfDay(date2.millisOfDay().getMaximumValue());
 
 		Goods goods=server.find(Goods.class, idGoods);
 		
 		boolean receptionResult;
 		boolean deletionResult;
 		boolean consumptionResult;
 		switch (stockType) {
 		case CONSUMPTION:
 						receptionResult=isAnyReception(dateTimeStartDay, goods);
 						deletionResult=isAnyDeletion(dateTimeStartDay, goods);
 						consumptionResult=isAnyConsumption(dateTimeEndDay, goods);
 						//jika tidak ditemukan konsumsi, maka pastikan tidak ada konsumsi lain pada hari yang sama
 						//yang di input setelah konsumsi ini.
 						if(!consumptionResult){
 							consumptionResult=isConsumptionNotLastOfTheDay(dateTimeStartDay,
 									goods, consumptionTimeStamp);
 						}
 						break;
 		case RECEPTION:	receptionResult=isAnyReception(dateTimeEndDay, goods); //beda variabel
 						deletionResult=isAnyDeletion(dateTimeStartDay, goods);
 						consumptionResult=isAnyConsumption(dateTimeStartDay, goods);
 						break;
 		case DELETION:
 						receptionResult=isAnyReception(dateTimeStartDay, goods);
 						deletionResult=isAnyDeletion(dateTimeStartDay, goods);
 						consumptionResult=isAnyConsumption(dateTimeStartDay, goods);
 						break;
 		default:
 			receptionResult=false;
 			deletionResult=false;
 			consumptionResult=false;
 			break;
 		}
 		
 		//jika ada recepsi, deletion, dan konsumsi : maka true
 		return receptionResult || deletionResult || consumptionResult?true:false;
 		
 	}
 	//Untuk menentukan suatu konsumsi merupakan yang terakhir di input
 	private boolean isConsumptionNotLastOfTheDay(DateTime day, Goods goods, Date timestamp){
 		Date startDate=day.withMillisOfDay(0).toDate();
 		Date endDate=day.withMillisOfDay(day.millisOfDay().getMaximumValue()).toDate();
 		
 		List<GoodsConsumption> goodList=server.find(GoodsConsumption.class).where().
 				between("consumptionDate", startDate, endDate).eq("goods", goods).findList();
 		for(GoodsConsumption consumption:goodList){
 			DateTime consumptionTimeStamp=new DateTime(consumption.getTimestamp());
 			DateTime currentTimeStamp=new DateTime(timestamp);
 			if(consumptionTimeStamp.isAfter(currentTimeStamp)){
 				return true;
 			}
 		}
 		
 		return false;
 		
 	}
 	private boolean isAnyReception(DateTime date, Goods goods){
 		 int size=server.find(GoodsReception.class).where().
 				eq("invoiceItem.purchaseOrderItem.supplierGoods.goods", goods).
 				gt("date", date.toDate()).findRowCount();
 		
 		return size>0?true:false;
 	}
 	
 	private boolean isAnyDeletion(DateTime date, Goods goods){
 
 		int size=server.find(DeletedGoods.class).where().eq("goods",goods).gt("approvalDate", date.toDate()).
 				  eq("acceptance", accept.getAcceptedByAllCriteria()).findRowCount();
 		return size>0?true:false;
 		
 	}
 	private boolean isAnyConsumption(DateTime date,Goods goods){
 		int size=server.find(GoodsConsumption.class).where().
 				gt("consumptionDate", date.toDate()).eq("goods", goods).findRowCount();
 
 		return size>0?true:false;
 	}
 
 	public boolean isDateValid(Date date, String idGoods){
		if(idGoods==null){
			System.out.println("goods null");
			return true;
		}
		
 		DateTime date2=new DateTime();
  
 		//variabel untuk tanggal akhir hari. Agar tidak menghitung tanggal pada hari yang sama.
 		DateTime dateTimeEndDay=new DateTime(date).withMillisOfDay(date2.millisOfDay().getMaximumValue());
 		Goods goods=server.find(Goods.class, idGoods);
 
 		boolean anyConsumption=isAnyConsumption(dateTimeEndDay, goods);
 		boolean anyDeletion=isAnyDeletion(dateTimeEndDay, goods);
 		boolean anyReception=isAnyReception(dateTimeEndDay, goods);
		System.out.println("GOods "+idGoods);
		System.out.println("anyConsumption " +anyConsumption);
		System.out.println("anyDeletion " +anyDeletion);
		System.out.println("anyReception " +anyReception);
 		return anyConsumption || anyDeletion || anyReception?false:true;
 		
 	}
 	
 	public static void main(String[] args) {
 		Calendar date =	Calendar.getInstance();
 		date.set(2005, 11,1);		
 		System.out.println(new DateTime(date.getTime()).withMillisOfDay(0));
 		DateTime dates=new DateTime();
 		DateTime date2=dates.minusMillis(10);
 		System.out.println(dates.isAfter(date2));
 	}
 }
