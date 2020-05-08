 package Client.Application;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import common.entities.CompanyResult;
 import common.entities.CompanyResultList;
 import common.entities.Supply;
 
 import Client.Entities.Company;
 import Client.Entities.Period;
 import Client.Entities.ProductionAndDistribution;
 import Client.Entities.ProfitAndLoss;
 import Client.Entities.PeriodInfo;
 import Client.Entities.Player;
 import Client.Network.Client;
 import NetworkCommunication.BroadcastCompanyResultMessage;
 import NetworkCommunication.MessageType;
 import NetworkCommunication.NetMessage;
 import NetworkCommunication.SendAssignedDisposalMessage;
 import NetworkCommunication.SendCompanyResultMessage;
 import NetworkCommunication.SendSupplyMessage;
 import Client.Presentation.MainWindow;
 import Client.Presentation.ResultPanel;
 
 public abstract class PeriodController {
 	
 	public static void ClosePeriod() throws CannotProduceException, CannotSaleException {
 		if (!CompanyController.canSale())
 			throw new CannotSaleException();
 		CompanyController.produce();
 		SendSupply();
 	}
 	
 	/**
 	 * Sendet den Supply an den Server, Supplydaten werden aus der Klasse prodAndDistr gezogen
 	 */
 	private static void SendSupply() {
 		Period period = PeriodInfo.getActualPeriod();
 		ProductionAndDistribution pad = Company.getInstance().getProdAndDistr();
 		period.setProductPrice(pad.getSellingPrice());
 		Supply s = new Supply(pad.getUnitsToSell(), pad.getSellingPrice());
		//s.quantity = 20000;
 		
 		Client client = Client.getInstance();
 		client.SendMessage(new SendSupplyMessage(s));
 	}
 
 	public static void RecieveAssignedDisposal(SendAssignedDisposalMessage sendAssignedDemandMessage) {
 
 		int quantity = sendAssignedDemandMessage.getQuantity();
 		
 		Period actPeriod = PeriodInfo.getActualPeriod();
 		double price = actPeriod.getProductPrice();
 		
 		double revenue = quantity * price;		
 		CompanyController.receiveSalesRevenue(revenue, quantity);
 		
 		try {
 			//double wages = CompanyController.payEmployeesSallery();
 			//double deprecation = CompanyController.depcrecateMachines();
 			CompanyController.paySallery();
 			CompanyController.depcrecateMachines();
 			CompanyController.payInterestAndRepayment();
 			
 			
 			//CompanyController.payEmployersSalery(); //integrated in paySallery.
 			CompanyController.payRent();
 			
 			//Company comp = Company.getInstance();
 			//double produceCostPerProdukt = wages / Math.min(comp.getEmployeeCapacity(EmployeeType.Produktion), comp.getEmployeeCapacity(EmployeeType.Verwaltung)) 
 			//							 + deprecation /  Math.min(comp.getMachineCapacity(MachineType.Filitiermaschine), comp.getMachineCapacity(MachineType.Verpackungsmaschine)) 
 			//							 + Ressource.getNeed(RessourceType.Rohfisch) * Ressource.getFixedCosts(RessourceType.Rohfisch) 
 			//							 + Ressource.getNeed(RessourceType.Verpackungsmaterial) * Ressource.getFixedCosts(RessourceType.Verpackungsmaterial);		
 			//PeriodInfo.getActualPeriod().setFinishedProductsValue(produceCostForLeftFinishedProducts);
 			//comp.setWarehouseCostPerProduct(produceCostPerProdukt);
 			CompanyController.payWarehouseCosts();
 			
 			ProfitAndLoss guv = actPeriod.makeGuV();	
 			
 			SendCompanyResultMessage message = new SendCompanyResultMessage(guv.profit);
 			Client.getInstance().SendMessage(message);
 			
 		} catch (UserCanNotPayException e) {
 			NetMessage insolvencyMessage = new NetMessage(MessageType.SEND_INSOLVENCY, new byte[0]);
 			Client.getInstance().SendMessage(insolvencyMessage);
 			
 			MainWindow.getInstance().showInsolvency();
 		}
 	}
 
 	public static void RecieveCompanyResult(
 		BroadcastCompanyResultMessage Message) {
 		CompanyResultList crl = Message.getCompanyResults();
 
 		try {
 			for(Iterator<CompanyResult> it = crl.profitList.values().iterator(); it.hasNext();){
 				CompanyResult result = it.next();
 				try {
 					
 					Player player = Player.getPlayer(result.clientid);
 					if (result.sales == -1) {
 						//Spieler ist Insolvent.
 						Player.getPlayer(player.getID()).becameInsolvent();
 						
 						SimpleDateFormat format = new SimpleDateFormat("kk:mm");
 						String time = format.format(new Date());
 						MainWindow.getInstance().addChatMessage(time + "\n" + player.getName() + " ist insolvent.");
 						
 						int activePlayers = 0;
 						List<Player> players = Player.getPlayers();
 						for (Player checkPlayer : players) {
 							if (!checkPlayer.hasLeftGame() && !checkPlayer.isInsolvent()) {
 								activePlayers++;
 							}
 						}
 						if (activePlayers <= 1 ) {
 							MainWindow.getInstance().changeScreen(new ResultPanel(ResultPanel.FinishReason.OnePlayerLeft));
 						}
 					} else {
 						player.addCompanyResult(result);
 					}
 				} catch (Exception e2) {
 					// Player not found. Do nothing.
 				}
 			}
 		} catch (Exception e1) {
 			// list is buggy.
 			throw new RuntimeException("Ergebnisliste nicht valide.");
 		}
 		
 		PeriodInfo.nextPeriod();
 		if(PeriodInfo.getNumberOfActPeriod() == PeriodInfo.getMaxPeriods()){
 			MainWindow.getInstance().changeScreen(new ResultPanel(ResultPanel.FinishReason.EndOfRoundsReached));
 			return;
 		}	
 		MainWindow.getInstance().reactiviateAfterPeriod();
 	}
 	
 }
