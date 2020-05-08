 package edu.ibs.webui.client.cards;
 
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
 import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
 import edu.ibs.common.dto.CardBookDTO;
 import edu.ibs.common.dto.CardRequestDTO;
 import edu.ibs.common.interfaces.IPaymentServiceAsync;
 import edu.ibs.webui.client.utils.AppCallback;
 import edu.ibs.webui.client.utils.Components;
 
 /**
  * User: EgoshinME
  * Date: 08.01.13
  * Time: 8:37
  */
 public class CardRequestsGrid extends ListGrid {
 	public CardRequestsGrid() {
 		super();
 		Components.prepareGrid(this);
 		this.setShowAllRecords(true);
		this.setSortField(1);
 
 		ListGridField idField = new ListGridField("id", "ID", 320);
 		ListGridField userField = new ListGridField("user", "Пользователь", 100);
 		ListGridField cardBookTypeField = new ListGridField("cardbooktype", "Тип", 100);
 		ListGridField bankBookIdField = new ListGridField("bankbookid", "Банковский счет", 100);
 		ListGridField approveActionField = Components.getIconGridField("approve", "Создать карт-счет",
 				"toolbar/assign.png", new RecordClickHandler() {
 			@Override
 			public void onRecordClick(RecordClickEvent event) {
 				if (event.getRecord() != null) {
 					CardRequestDTO requestDTO
 							= (CardRequestDTO) event.getRecord().getAttributeAsObject("cardrequestdto");
 					if (requestDTO != null) {
 						IPaymentServiceAsync.Util.getInstance().approveCardRequest(requestDTO, new AppCallback<CardBookDTO>() {
 							@Override
 							public void onSuccess(CardBookDTO cardBookDTO) {
 								if (cardBookDTO != null) {
 									SC.say("Создан карт счёт " + cardBookDTO.getId()
 											+ " для пользователя " + cardBookDTO.getBankBook().getOwner().getId());
 									fetchData();
 								}
 							}
 						});
 					}
 				}
 			}
 		});
 		ListGridField emptyField = new ListGridField("emptyField", " ");
 		this.setFields(new ListGridField[]{
 				idField, userField, cardBookTypeField, bankBookIdField, approveActionField, emptyField});
 		this.setDataSource(new CardRequestDataSource());
 	}
 
 }
