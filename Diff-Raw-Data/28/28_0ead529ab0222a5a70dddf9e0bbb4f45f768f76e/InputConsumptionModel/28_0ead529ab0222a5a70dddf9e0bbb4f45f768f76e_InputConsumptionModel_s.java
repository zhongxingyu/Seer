 package com.binar.core.inventoryManagement.consumptionList.inputConsumption;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.persistence.OptimisticLockException;
 
 import com.avaje.ebean.EbeanServer;
 import com.binar.core.requirementPlanning.inputRequierementPlanning.inputEditForm.FormData;
 import com.binar.entity.Goods;
 import com.binar.entity.GoodsConsumption;
 import com.binar.entity.Manufacturer;
 import com.binar.entity.ReqPlanning;
 import com.binar.entity.Setting;
 import com.binar.entity.Supplier;
 import com.binar.entity.SupplierGoods;
 import com.binar.generalFunction.GeneralFunction;
 import com.binar.generalFunction.GetSetting;
 import com.binar.generalFunction.StockFunction;
 import com.binar.generalFunction.TextManipulator;
 import com.google.gwt.resources.css.ast.CssProperty.StringValue;
 
 public class InputConsumptionModel {
 	GeneralFunction function;
 	EbeanServer server;
 	TextManipulator text;
 	GetSetting setting;
 	StockFunction stock;
 	
 	public InputConsumptionModel(GeneralFunction function){
 		this.function=function;
 		this.server=function.getServer();
 		this.setting=function.getSetting();
 		this.stock=function.getStock();
 	}
 	
 	public Map<String,String> getGoodsData(){
 		List<Goods> goodsList=server.find(Goods.class).findList();
 		Map<String, String> data=new TreeMap<String, String>();
 		for(Goods goods:goodsList){
 			String insurance="";
 			if(goods.getInsurance()!=null){
 				insurance=" - "+goods.getInsurance().getName();				
 			}
 			data.put(goods.getIdGoods(), goods.getName()+insurance);
 		}
 		return data;
 	}
 	
 	public Map<String,String> getWardData(){
 		return setting.getWard();
 	}
 	
 	public String insertData(FormConsumption data){
 		//mulai transaksi
 		
 		server.beginTransaction();
 		try {
			if(stock.isDateValid(data.getConsumptionDate(), data.getGoodsId())){
				return "Tanggal tidak valid";
			}
 			Goods goods=server.find(Goods.class, data.getGoodsId());			
 			//mulai insert
 			GoodsConsumption consumption = new GoodsConsumption();
 			consumption.setQuantity(Integer.parseInt(data.getQuantity()));
 			
 			consumption.setConsumptionDate(data.getConsumptionDate());
 			consumption.setWard(data.getWard());
 			consumption.setTimestamp(new Date());	
 			consumption.setInformation(data.getInformation());
 
 			// untuk mengurangi stock obat ketika ada konsumsi baru
 			Goods goodsStock=server.find(Goods.class, data.getGoodsId());			
 			int currentStock=goodsStock.getCurrentStock();
 			currentStock=currentStock-Integer.parseInt(data.getQuantity());
 			goodsStock.setCurrentStock(currentStock);
 			server.update(goodsStock);
 
 			consumption.setStockQuantity(currentStock);
 			consumption.setGoods(goodsStock);
 
 			server.save(consumption);
 			function.getMinimumStock().update(goodsStock.getIdGoods());
 			
 			
 			server.commitTransaction();
 			return null;
 			
 		} catch (NumberFormatException e) {
 			server.rollbackTransaction();
 			e.printStackTrace();
 			return "Kesalahan pengisian angka :"+e.getMessage();
 		} catch (OptimisticLockException e) {
 			server.rollbackTransaction();
 			e.printStackTrace();
 			return "Kesalahan tulis data ke database " + e.getMessage();
 		}catch(Exception e){
 			server.rollbackTransaction();
 			e.printStackTrace();
 			return "Kesalahan submit : " + e.getMessage();
 		
 		}finally{
 			server.endTransaction();			
 		}
 	}
 	
 	public String getGoodsUnit(String goodsId){
 		Goods goods=server.find(Goods.class,goodsId);
 		if(goods!=null){
 			return goods.getUnit();
 		}else{
 			return "";
 		}
 	}
 	
 	public String saveEdit(FormConsumption data, int idCons){
 		//mulai transaksi
 		
 		server.beginTransaction();
 		try {
 			GoodsConsumption consumption=server.find(GoodsConsumption.class, idCons);
 			int beforeUpdateQuantity=consumption.getQuantity();
 			int newQuantity=Integer.parseInt(data.getQuantity());
 			consumption.setQuantity(newQuantity);
 			consumption.setWard(data.getWard());
 			consumption.setConsumptionDate(data.getConsumptionDate());
 			consumption.setInformation(data.getInformation());			
 			
 			// untuk mengurangi stock obat ketika ada konsumsi baru
 			Goods goodsStock=server.find(Goods.class, data.getGoodsId());
 			
 			int currentStock=goodsStock.getCurrentStock();
 			currentStock=currentStock+beforeUpdateQuantity;
 			currentStock=currentStock-Integer.parseInt(data.getQuantity());
 			goodsStock.setCurrentStock(currentStock);
 			
 			consumption.setStockQuantity(currentStock);
 			server.update(goodsStock);
 			
 			server.update(consumption);
 			
 			function.getMinimumStock().update(goodsStock.getIdGoods());
 
 			server.commitTransaction();
 			return null;
 		} catch (NumberFormatException e) {
 			server.rollbackTransaction();
 			e.printStackTrace();
 			return "Kesalahan pengisian angka :"+e.getMessage();
 		} catch (OptimisticLockException e) {
 			server.rollbackTransaction();
 			e.printStackTrace();
 			return "Kesalahan tulis data ke database " + e.getMessage();
 		}catch(Exception e){
 			server.rollbackTransaction();
 			e.printStackTrace();
 			return "Kesalahan submit : " + e.getMessage();
 		
 		}finally{
 			server.endTransaction();			
 		}
 	}
 	
 	public GoodsConsumption getSingleConsumption(int id){
 		return server.find(GoodsConsumption.class, id);
 	}
 }
