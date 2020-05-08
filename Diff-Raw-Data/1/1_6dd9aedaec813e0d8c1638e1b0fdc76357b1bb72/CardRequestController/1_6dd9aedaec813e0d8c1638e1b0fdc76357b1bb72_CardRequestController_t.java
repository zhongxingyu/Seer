 package edu.ibs.webui.client.controller;
 
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.IButton;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import edu.ibs.common.dto.BankBookDTO;
 import edu.ibs.common.dto.CurrencyDTO;
 import edu.ibs.common.dto.UserDTO;
 import edu.ibs.common.dto.VocDTO;
 import edu.ibs.common.enums.CardBookType;
 import edu.ibs.common.interfaces.IPaymentServiceAsync;
 import edu.ibs.webui.client.ApplicationManager;
 import edu.ibs.webui.client.utils.AppCallback;
 import edu.ibs.webui.client.utils.Components;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: EgoshinME
  * Date: 05.01.13
  * Time: 8:11
  */
 public class CardRequestController extends GenericWindowController {
 
     private GenericController bankBookControl = Components.getComboBoxControll();
     private GenericController cardTypeControl = Components.getComboBoxControll();
     private GenericController currenciesControl = Components.getComboBoxControll();
 
 	private List<CurrencyDTO> currencyDTOList = new ArrayList<CurrencyDTO>();
 
 	public CardRequestController() {
 		getWindow().setTitle("Заявка на карт-счёт");
 
         UserDTO userDTO = ApplicationManager.getInstance().getAccount().getUser();
         if (userDTO != null) {
             IPaymentServiceAsync.Util.getInstance().getBankBooks(userDTO, new AppCallback<List<BankBookDTO>>() {
                 @Override
                 public void onSuccess(List<BankBookDTO> bankBookDTOs) {
                     if (bankBookDTOs != null && bankBookDTOs.size() > 0) {
                         List<VocDTO<String, String>> bindList = new LinkedList<VocDTO<String, String>>();
                         for (BankBookDTO dto : bankBookDTOs) {
                             VocDTO<String, String> vocDTO = new VocDTO<String, String>(
                                     String.valueOf(dto.getId()), String.valueOf(dto.getId()));
                             bindList.add(vocDTO);
                         }
                         bankBookControl.bind(bindList);
                     } else {
                         SC.say("Нет банковских счетов.");
                         getWindow().hide();
                     }
                 }
             });
         } else {
             SC.warn("Аккаунт не связан с пользователем.");
             getWindow().hide();
         }
 
 		List<VocDTO<String, String>> types = new LinkedList<VocDTO<String, String>>();
 		for (CardBookType type : CardBookType.values()) {
 			VocDTO<String, String> vocDTO = new VocDTO<String, String>();
             vocDTO.setId(type.toString());
 			if (CardBookType.DEBIT.equals(type)) {
 				vocDTO.setValue("Дебетная");
 			} else if (CardBookType.CREDIT.equals(type)) {
 				vocDTO.setValue("Кредитная");
 			}
 			types.add(vocDTO);
 		}
 		cardTypeControl.bind(types);
 
 		IPaymentServiceAsync.Util.getInstance().getCurrencies(new AppCallback<List<CurrencyDTO>>() {
 			@Override
 			public void onSuccess(List<CurrencyDTO> currencyDTOs) {
 				if (currencyDTOs != null && currencyDTOs.size() > 0) {
 					List<VocDTO<String, String>> bindList = new LinkedList<VocDTO<String, String>>();
 
 					for (CurrencyDTO dto : currencyDTOs) {
 						currencyDTOList.add(dto);
 						VocDTO<String, String> vocDTO = new VocDTO<String, String>();
                         vocDTO.setId(String.valueOf(dto.getId()));
                         vocDTO.setValue(dto.getName());
 						bindList.add(vocDTO);
 					}
 					currenciesControl.bind(bindList);
 				}
 			}
 		});
 
 		final IButton createButton = new IButton("Отправить");
 		createButton.setWidth(80);
 		createButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(final ClickEvent clickEvent) {
 				String bankBookIdTxt = ((VocDTO<String, String>) bankBookControl.unbind()).getId();
 				VocDTO<String, String> cardTypeVoc = ((VocDTO<String, String>) cardTypeControl.unbind());
 				String cardTypeTxt = cardTypeVoc.getValue();
 				VocDTO<String, String> currencyVoc = ((VocDTO<String, String>) currenciesControl.unbind());
 				String currencyTxt = currencyVoc.getValue();
 				if (bankBookIdTxt == null || "".equals(bankBookIdTxt) || bankBookIdTxt.length() == 0) {
 					SC.warn("Номер банковского счёта не заполнен.");
 				} else if (cardTypeTxt == null || "".equals(cardTypeTxt) || cardTypeTxt.length() == 0 || cardTypeVoc.getId() == null) {
 					SC.warn("Тип карты не выбран.");
 				} else if (currencyTxt == null || "".equals(currencyTxt) || currencyTxt.length() == 0 || currencyVoc.getId() == null) {
 					SC.warn("Не выбрана валюта счёта.");
 				} else {
 					CardBookType cardBookType = CardBookType.forName(cardTypeVoc.getId());
 					CurrencyDTO currencyDTO = null;
 					for (CurrencyDTO currencyDTO1 : currencyDTOList) {
 						if (currencyDTO1.getId() == Integer.parseInt(currencyVoc.getId())) {
 							currencyDTO = currencyDTO1;
 						}
 					}
 					createButton.setDisabled(true);
 					IPaymentServiceAsync.Util.getInstance().requestCard(ApplicationManager.getInstance().getAccount().getUser(),
                             bankBookIdTxt, cardBookType, currencyDTO, new AppCallback<Void>() {
 
 						@Override
 						public void onFailure(Throwable t) {
 							super.onFailure(t);
 							createButton.setDisabled(false);
 						}
 
 						@Override
 						public void onSuccess(Void aVoid) {
 							createButton.setDisabled(false);
 							SC.say("Заявка отправлена успешно!");
							getWindow().hide();
 						}
 					});
 				}
 			}
 		});
 		VLayout layoutForm = new VLayout();
 		layoutForm.setWidth100();
 		layoutForm.setHeight100();
 
 		layoutForm.addMember(Components.addTitle("№ банковского счёта", bankBookControl.getView()));
 		layoutForm.addMember(Components.addTitle("Тип карты", cardTypeControl.getView()));
 		layoutForm.addMember(Components.addTitle("Валюта", currenciesControl.getView()));
 
 		HLayout buttons = new HLayout();
 		buttons.addMember(createButton);
 
 		VLayout view = new VLayout();
 		view.setMembersMargin(MARGIN);
 		view.addMember(layoutForm);
 		view.addMember(buttons);
 		view.setMargin(MARGIN);
 		view.setShowResizeBar(false);
 
 		getWindow().addItem(view);
 	}
 }
