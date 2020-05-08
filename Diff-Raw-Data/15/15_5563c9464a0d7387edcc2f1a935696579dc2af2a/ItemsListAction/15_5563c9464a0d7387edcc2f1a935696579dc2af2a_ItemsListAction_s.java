 package com.vaps.action;
 
 import java.util.List;
 
 import com.vaps.bean.BoardList;
 import com.vaps.bean.Items;
 import com.vaps.dao.ItemsDAO;
 
 
 public class ItemsListAction {
 	ItemsDAO dao;
 	
 	public ItemsListAction(ItemsDAO items){
 		this.dao = items;
 	}
 	
 	public List<Items> getItemsList(){
 		// 상품 리스트
 		return dao.getItemsList();
 	}
 	public int setItems(Items item) {
 		// 상품등록
 		convertDBcontents(item); // 내용저장 처리
 		return dao.setItems(item);
 	}
 	public int setItemsTored(Items item){
 		// 상품재고등록(상품등록과 같은 시점에서 등록함)
 		return dao.setItemsTored(item);
 	}
 	public Items getContents(String i_name){
 		// 상품 상세 정보
 		Items item = dao.getContents(i_name); 
 		return item;
 	}
 	public Items getContentsModi(String i_name){
 		// 상품 상세 정보 수정 textarea 문제때문에 변환 필요
 		Items item = dao.getContents(i_name); 
 		String str = item.getI_description();
		str = str.replaceAll("<br>", "\r\n");
		str = str.replaceAll("&nbsp;", "\u0020");
		item.setI_description(str);
 		return item;
 	}
 	public int setItemsUpdate(Items item) {
 		// 상품update
 		convertDBcontents(item); // 내용저장 처리
 		return dao.setItemsUpdate(item);
 	}
 	public int setItemsToredUpdate(Items item){
 		// 상품재고update
 		return dao.setItemsToredUpdate(item);
 	}
 	
 	// add method
 	public void convertDBcontents(Items item){
 		// db에 줄바꿈과 공백 처리
 		String str = item.getI_description();
 		str = str.replaceAll("\r\n", "<br>");
 		str = str.replaceAll("\u0020", "&nbsp;");
 		item.setI_description(str);
 	}
 }
