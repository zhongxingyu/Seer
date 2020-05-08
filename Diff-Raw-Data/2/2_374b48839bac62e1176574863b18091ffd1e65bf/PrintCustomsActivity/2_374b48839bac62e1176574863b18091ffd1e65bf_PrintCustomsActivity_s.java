 package org.fhw.asta.kasse.client.activity;
 
 import java.util.List;
 
 import org.fhw.asta.kasse.client.common.EuroFormatter;
 import org.fhw.asta.kasse.client.common.PrintCustomsToken;
 import org.fhw.asta.kasse.client.place.PrintCustomsPlace;
 import org.fhw.asta.kasse.client.widget.HasTopbar;
 import org.fhw.asta.kasse.client.widget.print.PrintWidget;
 import org.fhw.asta.kasse.shared.basket.BasketItem;
 import org.fhw.asta.kasse.shared.common.EuroAmount;
 import org.fhw.asta.kasse.shared.model.BillOrder;
 import org.fhw.asta.kasse.shared.model.LetterHead;
 import org.fhw.asta.kasse.shared.model.Person;
 import org.fhw.asta.kasse.shared.service.billorder.BillOrderServiceAsync;
 import org.fhw.asta.kasse.shared.service.letterhead.LetterHeadServiceAsync;
 import org.fhw.asta.kasse.shared.service.user.UserServiceAsync;
 
 import com.google.common.base.Optional;
 import com.google.gwt.activity.shared.AbstractActivity;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.inject.Inject;
 import com.google.inject.assistedinject.Assisted;
 
 public class PrintCustomsActivity extends AbstractActivity {
 
 	@Inject
 	private PrintWidget printWidget;
 
 	@Inject
 	private BillOrderServiceAsync billOrderService;
 
 	@Inject
 	private UserServiceAsync userService;
 
 	@Inject
 	private LetterHeadServiceAsync letterHeadService;
 
 	@Inject
 	private HasTopbar topBarContainer;
 
 	private PrintCustomsPlace printCustomsPlace;
 
 	private BillOrder billOrder;
 	private Person recp;
 	private Person issue;
 	private List<BasketItem> articles;
 
 	@Inject
 	public PrintCustomsActivity(@Assisted PrintCustomsPlace printCustomsPlace) {
 		this.printCustomsPlace = printCustomsPlace;
 	}
 
 	@Override
 	public void start(AcceptsOneWidget panel, EventBus eventBus) {
 		panel.setWidget(printWidget);
 		printWidget.clear();
 		topBarContainer.setTopbar(null);
 
 		letterHeadService.getLetterHead(new getHeadCallback());
 
 	}
 
 	private class getHeadCallback implements AsyncCallback<LetterHead> {
 
 		@Override
 		public void onFailure(Throwable caught) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onSuccess(LetterHead result) {
 			printLetterHead(result);
 
 			if (printCustomsPlace.getToken().isPresent()) {
 
 				switch (printCustomsPlace.getToken().get().getPrintType()) {
 				case BILLORDER:
 					printBILLORDER(printCustomsPlace.getToken().get().getId());
 					break;
 
 				case RECEIPT:
 					printRECEIPT(printCustomsPlace.getToken().get().getId());
 					break;
 
 				case TAX:
 					printTAX(printCustomsPlace.getToken().get().getId());
 					break;
 
 				case USERLIST:
 					printUSERLIST(printCustomsPlace.getToken());
 					break;
 				}
 			}
 
 		}
 
 	}
 
 	private void printLetterHead(LetterHead letterHead) {
 		printWidget.addHtml("<div class='headline'>" + letterHead.getName()
 				+ " - " + letterHead.getStreet() + letterHead.getStreetnumber()
 				+ " - " + letterHead.getZipcode() + " " + letterHead.getTown()
 				+ "</div>");
 	}
 
 	private void printBILLORDER(int id) {
 
 		billOrderService.getBillOrder(id,
 				new AsyncCallback<Optional<BillOrder>>() {
 
 					@Override
 					public void onSuccess(Optional<BillOrder> result) {
 						billOrder = result.or(new BillOrder());
 						billOrderService.getBillOrderArticles(
 								billOrder.getId(),
 								new AsyncCallback<List<BasketItem>>() {
 
 									@Override
 									public void onFailure(Throwable caught) {
 										// TODO Auto-generated method stub
 
 									}
 
 									@Override
 									public void onSuccess(
 											List<BasketItem> result) {
 										articles = result;
 
 										userService.getUserByIdAndRevision(
 												billOrder.getIssuerLdapName(),
 												billOrder.getIssuerRevision(),
 												new AsyncCallback<Optional<Person>>() {
 
 													@Override
 													public void onFailure(
 															Throwable caught) {
 														// TODO Auto-generated
 														// method
 														// stub
 
 													}
 
 													@Override
 													public void onSuccess(
 															Optional<Person> result) {
 														issue = result
 																.or(new Person());
 														userService
 																.getUserByIdAndRevision(
 																		billOrder
 																				.getRecipientLdapName(),
 																		billOrder
 																				.getReceipientRevision(),
 																		new AsyncCallback<Optional<Person>>() {
 
 																			@Override
 																			public void onFailure(
 																					Throwable caught) {
 																				// TODO
 																				// Auto-generated
 																				// method
 																				// stub
 
 																			}
 
 																			@Override
 																			public void onSuccess(
 																					Optional<Person> result) {
 																				recp = result
 																						.or(new Person());
 																				printBillOrder();
 																				printArticles();
 
 																			}
 																		});
 
 													}
 												});
 
 									}
 								});
 
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						// TODO Auto-generated method stub
 
 					}
 				});
 	}
 
 	private void printBillOrder() {
 		printWidget.addHtml("<br /><br />");
 		if (recp.getLdapName().equals("default")) {
 			printWidget.addHtml("<div class='recipient'><table><tr>"
 					+ "<td><strong>BARKUNDE </strong></td><td></td>"
 					+ "</tr><tr><td><strong></strong></td><td>"
 					+ "</td></tr></table>" + "<div>");
 
 		} else {
 			printWidget.addHtml("<div class='recipient'><table><tr>"
 					+ "<td><strong>Name: </strong></td>" + "<td>"
 					+ recp.getPrename() + " " + recp.getSurname() + "</td>"
 					+ "</tr><tr><td><strong>Matrikel-Nr.: </strong></td>"
 					+ "<td>" + recp.getMatrNo() + "</td></tr></table>"
 					+ "<div>");
 
 		}
 		printWidget.addHtml("<div class='billdata'><table class='billtbl'><tr>"
 				+ "<td><strong>Rechnungs-Nr.: </strong></td>"
 				+ "<td class='billdata-right'>"
 				+ billOrder.getId()
 				+ "</td>"
 				+ "</tr><tr><td><strong>Datum: </strong></td>"
 				+ "<td class='billdata-right'>"
 				+ DateTimeFormat.getFormat("dd.MM.yyyy").format(
 						billOrder.getDatetimeOfCreation()) + "</td></tr>"
 
 				+ "<tr><td><strong>Bediener: </strong></td>"
 				+ "<td class='billdata-right'>" + issue.getPrename() + " "
 				+ issue.getSurname() + "</td></tr></table>" + "<div>");
 		printWidget
 				.addHtml("<br/><br/><br/><br/><h2><strong>Rechnung</strong></h2>");
 	}
 
 	private void printArticles() {
 		StringBuilder strb = new StringBuilder();
 		//int sum = 0;
 		
 		EuroAmount sum = EuroAmount.ZERO_AMOUNT;
 		
 		strb.append("<br/>");
 		strb.append("<table class='regtb'><tr class='headline'><td>Menge</td><td class='desc'>Beschreibung</td><td>Rabatt (&#037;)</td><td>E-Preis</td><td>G-Preis</td><td>Abgehohlt</td></tr>");
 		for (BasketItem art : articles) {
 
 			EuroAmount euroAmount = art.totalWithDiscount();
 						
 /*			EuroAmount euroAmount = new EuroAmount((int) Math.round((art
 					.getItemPrice().getCentAmount() * art.getAmount())
 					* ((100.0 - art.getDiscount()) / 100)));*/
 			 
 			sum = sum.plus(euroAmount);
 			
 //			sum += euroAmount.getCentAmount();
 			strb.append("<tr class='unbreakable'><td>" + art.getAmount()
 					+ "</td><td class='desc'>" + art.getItemName()
 					+ "</td><td>" + art.getDiscount() + "</td><td>"
 					+ EuroFormatter.format(art.getItemPrice()) + "</td><td>"
 					+ EuroFormatter.format(euroAmount)
 					+ "</td><td><div class='abghe'>"
 					+ "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</div>"
 					+ "</td></tr>");
 		}
 		strb.append("<tr class='headline'><td></td><td class='desc'></td><td></td><td></td><td></td><td></td></tr>");
 		strb.append("<tr><td></td><td class='desc'></td><td></td><td></td><td><strong>Gesamt:</strong></td><td>"
 				+ EuroFormatter.format(sum) + "</td></tr>");
 		strb.append("<tr><td></td><td class='desc'></td><td></td><td></td><td><strong>Rabatt (&#037;):</strong></td><td>"
 				+ billOrder.getDiscount() + "</td></tr>");
 		strb.append("<tr><td></td><td class='desc'></td><td></td><td></td><td><strong>Endsumme:</strong></td><td>"
 				+ EuroFormatter.format(sum.withDiscount(billOrder.getDiscount()))
 				+ "</td></tr>");
 		strb.append("</table>");
 		printWidget.addHtml(strb.toString());
 		printWidget.addHtml("<br /><br /><br />");
 		printWidget
 				.addHtml("<strong><center>Diese Rechnung ist laut &sect;19 UStG Umsatzsteuerbefreit</center></strong>");
 	}
 
 	private void printRECEIPT(int id) {
 
 	}
 
 	private void printTAX(int year) {
 
 	}
 
 	private void printUSERLIST(Optional<PrintCustomsToken> maybeToken) {
 		if (maybeToken.isPresent()) {
 			userService.getUsersByGroup(String.valueOf(maybeToken.get().getId()),
 					new UserListCallback());			
 		} else {
 			userService.getAllUsers(new UserListCallback());			
 		}		
 	}
 
 
 	private class UserListCallback implements AsyncCallback<List<Person>> {
 
 		@Override
 		public void onFailure(Throwable caught) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onSuccess(List<Person> result) {
 			StringBuilder strb = new StringBuilder();
 			strb.append("<table class='usertb'><tr class='headlineus'><td>RZ-Login</td><td class>Name</td><td>Vorname</td><td>Mobil</td><td>Festnetz</td><td>E-Mail</td><td>Straße</td><td>Ort</td></tr>");
 			int t = 0;
 			for (Person p : result) {
 				
				strb.append("<tr class='" + t % 2 == 1 ? "uneven" : "even" + "'><td>"+p.getLdapName()+"</td><td class>"+p.getSurname()+
 						"</td><td>"+p.getPrename()+"</td><td>"+p.getPhoneMobile()+"</td><td>" +
 								p.getPhoneHome()+"</td><td>"+p.getEmail()+"</td><td>" +
 										p.getStreet()+" "+p.getStreetnumber()+"</td><td>"+p.getZipcode()+" "+p.getTown()+"</td></tr>");
 				
 				t++;
 			}
 			strb.append("</table>");
 			printWidget.addHtml(strb.toString());
 
 		}
 
 	}
 }
