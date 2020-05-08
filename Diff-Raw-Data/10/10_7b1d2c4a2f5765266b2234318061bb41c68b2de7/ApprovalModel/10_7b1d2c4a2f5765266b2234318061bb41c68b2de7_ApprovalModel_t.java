 package com.binar.core.requirementPlanning.approval;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.joda.time.DateTime;
 
 import com.avaje.ebean.EbeanServer;
 import com.binar.entity.ReqPlanning;
 import com.binar.generalFunction.AcceptancePyramid;
 import com.binar.generalFunction.GeneralFunction;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.ui.CheckBox;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 
 public class ApprovalModel {
 	
 	GeneralFunction function;
 	EbeanServer server;
 	
 	AcceptancePyramid accept;
 	
 	public class AcceptData{
 		private int idReq;
 		private int accepted;
 		private int quantityAccepted;
 		private String comment;
 		protected int getIdReq() {
 			return idReq;
 		}
 		protected void setIdReq(int idReq) {
 			this.idReq = idReq;
 		}
 		protected int getAccepted() {
 			return accepted;
 		}
 		protected void setAccepted(int accepted) {
 			this.accepted = accepted;
 		}
 		protected int getQuantityAccepted() {
 			return quantityAccepted;
 		}
 		protected void setQuantityAccepted(int quantityAccepted) {
 			this.quantityAccepted = quantityAccepted;
 		}
 		public String getComment() {
 			return comment;
 		}
 		public void setComment(String comment) {
 			this.comment = comment;
 		}
 		
 	}
 	
 	public ApprovalModel(GeneralFunction function){
 		this.function=function;
 		this.accept=function.getAcceptancePyramid();
 		this.server=function.getServer();
 	}
 	//mendapatkan tabel req planning sesuai bulan yang dipilih
 	public List<ReqPlanning> getTableData(DateTime periode){
 		Date startDate=periode.toDate();
 		Date endDate=periode.withDayOfMonth(periode.dayOfMonth().getMaximumValue()).toDate();
 		List<ReqPlanning> returnValue=server.find(ReqPlanning.class).where().
 				between("period", startDate, endDate).findList();
 		return filterReqPlanning(returnValue);
 	}
 	//memfilter requirement planning sesuai dengan role (PPK, IFRS, PNJ)
 	private List<ReqPlanning> filterReqPlanning(List<ReqPlanning> list){
 		List<ReqPlanning> returnValue=new ArrayList<ReqPlanning>();
 		for(ReqPlanning req:list){
 			if(accept.isShow(req.getAcceptance())){
 				returnValue.add(req);
 			}
 		}
 		return  returnValue;
 	}
 	//mendapatkan data tabel yang mau disimpan
 	private List<AcceptData> getTableData(Container container) {
 		List<AcceptData> returnValue=new ArrayList<AcceptData>();
 		Collection<?> tableItemId=container.getItemIds();
 		for(Object itemId:tableItemId){
 			Item item=container.getItem(itemId);
 			
 			AcceptData acceptData=new AcceptData();
 			ComboBox comboResult=(ComboBox)item.getItemProperty("Disetujui?").getValue();
 			TextField textfieldResult=(TextField)item.getItemProperty("Jumlah Disetujui").getValue();
 			TextArea textAreaResult=(TextArea)item.getItemProperty("Keterangan").getValue();
 			
 			int quantityAccepted;
 			try {
 				quantityAccepted=Integer.parseInt(textfieldResult.getValue());
 				
 			} catch (NumberFormatException e) {
 				//jika ada field yang tidak sesuai format misalnya ada teksnya, maka langsung return null
 				//maka nantinya nampilin pesan error
 				return null;
 			}
 			System.out.println("Accepted or not di web"+accept.acceptedOrNot((Integer)comboResult.getValue()));
 			acceptData.setAccepted(accept.acceptedOrNot((Integer)comboResult.getValue()));
 			acceptData.setIdReq((Integer)itemId);
 			acceptData.setQuantityAccepted(quantityAccepted);
 			acceptData.setComment((String)textAreaResult.getValue());
 			
 			returnValue.add(acceptData);
 		}
 		return returnValue;
 	}
 
 	//untuk menerima kebutuhan
 	public String acceptData(Container container){
 		List<AcceptData> acceptData=getTableData(container);
 		if(acceptData==null){
 			return "Jumlah kebutuhan harus berupa angka";
 		}
 		//mulai transaksi
 		server.beginTransaction();
 
 		try {
 			for(AcceptData data:acceptData){
 				ReqPlanning planning=server.find(ReqPlanning.class, data.getIdReq());
 				planning.setAcceptedQuantity(data.getQuantityAccepted());
 				
 				//Perbaikan bugs, jika sudah disetujui oleh yang lebih tinggi, persetujuan tidak akan turun pangkat, 
 				//jika yang lebih rendah menyimpan data baru. Kecuali jika checknya dihapus.
 				int currentValue=planning.getAcceptance();
 				
 			
 				//Jika nilai saat ini lebih besar data yang diset valuenya dan tidak sedang ditolak / dianggep belum disetujui
 				//Maka tidak perlu di ganti nilainya
 				if(currentValue>=data.getAccepted()){
 					if(!(data.getAccepted()<=accept.getUnacceptCriteria())){
 						planning.setAcceptance(currentValue);						
 					}else{
 						planning.setAcceptance(data.getAccepted());																
 					}
 				}else{
 					planning.setAcceptance(data.getAccepted());										
 				}
 				
 				planning.setDateAccepted(new Date());
 				planning.setComment(accept.saveReqPlanning(data.getComment(), data.getIdReq()));
				System.out.println("Comment : "+planning.getComment());
				System.out.println("Acceptance : "+planning.getAcceptance());
				System.out.println("Quantity : "+planning.getAcceptedQuantity());
 				server.update(planning);
 			}
 			server.commitTransaction();
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			server.rollbackTransaction();
 			return e.getMessage();
 		}finally{
 			server.endTransaction();
 		}
 		
 	}
 	
 	public ReqPlanning getReqPlanning(int idReq){
 		return server.find(ReqPlanning.class, idReq);
 	}
 	
 	
 
 }
