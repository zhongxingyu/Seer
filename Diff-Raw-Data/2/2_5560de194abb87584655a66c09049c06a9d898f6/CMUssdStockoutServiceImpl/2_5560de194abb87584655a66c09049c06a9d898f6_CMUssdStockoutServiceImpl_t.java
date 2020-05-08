 package za.org.opengov.ussd.service.stockout.cm;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import za.org.opengov.stockout.entity.Facility;
 import za.org.opengov.stockout.entity.Stockout;
 import za.org.opengov.stockout.entity.StockoutReport;
 import za.org.opengov.stockout.entity.medical.Product;
 import za.org.opengov.stockout.service.FacilityService;
 import za.org.opengov.stockout.service.StockoutReportService;
 import za.org.opengov.stockout.service.StockoutService;
 import za.org.opengov.stockout.service.medical.ProductService;
 import za.org.opengov.ussd.controller.cm.CMUssdRequest;
 import za.org.opengov.ussd.controller.cm.CMUssdResponse;
 import za.org.opengov.ussd.service.stockout.UssdStockoutDao;
 import za.org.opengov.ussd.util.KeyValueStore;
 
 /**
  * Take note: When a REST call is made, the request will be delegated to the
  * service which matches the name as specified below.
  */
 @Service("cm.stockout")
 public class CMUssdStockoutServiceImpl implements CMUssdStockoutService {
 
 	@Autowired
 	private KeyValueStore keyValueStore;
 
 	@Autowired
 	private UssdStockoutDao stockoutDao;
 
 	@Autowired
 	private StockoutReportService stockoutReportService;
 
 	@Autowired
 	private FacilityService facilityService;
 
 	@Autowired
 	private ProductService productService;
 
 	@Autowired
 	private StockoutService stockoutService;
 
 	@Override
 	public CMUssdResponse createUssdResponse(CMUssdRequest request) {
 
 		CMUssdResponse response = new CMUssdResponse();
 		String displayText = "";
 		String sessionId = "";
 		int menuRequest = 0;
 
 		try {
 			// get the stage at which the user is in,in the ussd session
 			menuRequest = Integer.parseInt(request.getRequestid());
 			// session id identifies session specific data stored in the key
 			// value store
 			sessionId = request.getUssdSessionId();
 
 			switch (menuRequest) {
 
 			case 0: // welcome message..clinic code prompt
 
 				displayText = stockoutDao.getMenu(0);
 				++menuRequest;
 
 				break;
 
 			case 1: // validate clinic and display ussd stock out services
 
 				displayText = request.getRequest();
 				// stockoutDao.checkClinic(request.getRequest());****database
 				// call
 				
 				//-----------------------------------------------------------------------------
 				//String clinicName = displayText;
 				//matches the best matching facility
 				Facility facility = facilityService.getClosestMatch(displayText);
 				//-----------------------------------------------------------------------------
 
				if (facility != null) {
 					// Need to set clinic name so that it can be re-used later
 					keyValueStore.put(
 							"facilityName." + request.getUssdSessionId(),
 							facility);
 					displayText = facility.getLocalName() + " " + stockoutDao.getMenu(1);
 					++menuRequest;
 
 				} else {
 					// displayed failed message and redisplay same menu
 					displayText += " " + stockoutDao.getMenu(92);
 					throw new NumberFormatException();
 				}
 
 				break;
 
 			case 2: // process service request,get list of recent medicines
 
 				displayText = stockoutDao.getMenu(91); // set error message for
 														// incorrect string
 														// input and invalid
 														// integer selection
 
 				int requestSelection = Integer.parseInt(request.getRequest());
 
 				if (requestSelection >= 1 && requestSelection <= 3) {
 
 					displayText = stockoutDao.getMenu(21);
 					
 					//displayText += "1.Medicine1 \n2.Medicine2 \n3.Medicine3 \n4.Medicine4";
 
 					//String[] recentStockouts = { "Medicine1", "Medicine2",
 						//	"Medicine3", "Medicine4" };
 					//keyValueStore.put("recentStockouts." + sessionId,
 							//recentStockouts);
 
 					// method that retrieves commonly reported stock out, from a
 					// clinic
 					// *****Need clinic name entered earlier
 					// StockoutDao.retrieveCommonStockouts(keyValueStore.get("facilityName."+request.getUssdSessionId()));
 					// **************************************************************
 
 					
 					//-----------------------------------------------------------------------------
 					int limit = 5;
 					//must be facility code, not facility name
 					String facilityCode = ((Facility) (keyValueStore.get("facilityName." + sessionId))).getUid();
 					//facilityCode.
 					List<Stockout> stockouts = stockoutService.getMostCommonlyReportedStockoutsForFacility(facilityCode, limit);
 					
 					keyValueStore.put("commonStockouts." + sessionId,stockouts);
 					
 					for(int index=0;index<stockouts.size();index++){
 						
 						displayText += (index+1) + "." + stockouts.get(index).getProduct().getName() + "\n";
 					}
 					//or 
 
 					//this returns most recent reports, though I could also just return most recent actual stockouts
 					//since multiple recent reports could be for same stockouts
 					//List<StockoutReport> recentReports = stockoutReportService.getRecentlyReportedStockouts(limit);
 					
 					//-----------------------------------------------------------------------------
 					
 					
 					displayText += stockoutDao.getMenu(22);
 
 					keyValueStore.put("service." + sessionId,
 							Integer.toString(requestSelection));
 					++menuRequest;
 
 				} else {
 
 					// number greater 3 or less than 1 was chosen
 					throw new NumberFormatException();
 				}
 				break;
 
 			case 3: // process user selection of recent reports or manual
 					// medicine name entry
 
 				displayText = stockoutDao.getMenu(91);
 
 				int requestMedicine = Integer.parseInt(request.getRequest());
 
 				if (requestMedicine >= 1 && requestMedicine <= 7) { // process
 																	// medicine
 																	// selection
 																	// 1-8
 
 					List<Stockout> stockouts = (List<Stockout>) keyValueStore.get("commonStockouts." + sessionId);
 						
 					
 					Product selectedProduct = stockouts.get(requestMedicine - 1).getProduct();
 					displayText = selectedProduct.getName();
 					keyValueStore.put("productName." + sessionId,selectedProduct);
 					displayText += " " + stockoutDao.getMenu(4);
 					menuRequest += 2;
 
 				} else if (requestMedicine == 8) { // display enter medicine
 													// name prompt
 
 					displayText = stockoutDao.getMenu(3);
 					++menuRequest;
 
 				} else {// user enters a number less than 1 or greater than 8
 
 					throw new NumberFormatException();
 
 				}
 				break;
 
 			case 4: // validate/find nearest match to medicine name+display
 					// appropriate menu as above
 
 				// displayText =
 				// stockoutDao.CheckAndFindNearestMatch(request.getRequest());
 				
 				//-----------------------------------------------------------------------------
 				displayText = request.getRequest();
 				
 				//String productName = "foobar";
 				Product searchProduct = productService.getClosestMatch(displayText);
 				
 				//-----------------------------------------------------------------------------
 
 				
 
 				if ((searchProduct != null)) { // medicine name found, go
 														// to next menu
 					keyValueStore.put("productName." + sessionId,searchProduct);
 					displayText = searchProduct.getName() + " " + stockoutDao.getMenu(4);
 					++menuRequest;
 
 				} else { // medicine name not found
 					displayText += " " + stockoutDao.getMenu(92);
 					throw new NumberFormatException();
 				}
 				
 				break;
 
 			case 5: // run methods for each of the different services+display
 					// result.
 
 				String serviceRequest = (String) keyValueStore.get("service."
 						+ sessionId);
 				Product selectedProduct = (Product) keyValueStore
 						.get("productName." + sessionId);
 				Facility selectedFacility = (Facility) keyValueStore
 						.get("facilityName." + sessionId);
 
 				int service = Integer.parseInt(serviceRequest);
 				int requestOption = Integer.parseInt(request.getRequest());
 
 				if (requestOption == 1) {
 					switch (service) {
 					case 1:
 						// StockoutDao.reportStockout(medicineName,facilityName);
 						
 						//-----------------------------------------------------------------------------
 						//must be the correct facility and product code
 						String productCode = selectedProduct.getUid();
 						String facilityCode = selectedFacility.getUid();
 						stockoutReportService.submitStockoutReport(productCode, facilityCode, null,
 								null, null, false, false);
 						//-----------------------------------------------------------------------------
 						
 						
 						displayText = selectedProduct.getName() + " in " + selectedFacility.getLocalName()
 								+ " " + stockoutDao.getMenu(5);
 						break;
 					case 2:
 						// displayText =
 						// StockoutDao.getStatus(MedicineName,facilityName);
 						
 						//-----------------------------------------------------------------------------
 						//against must be proper facility code and product code (no matching is done)
 						String selectedFacilityCode = selectedFacility.getUid();
 						String selectedProductCode = selectedProduct.getUid();
 						Stockout stockout = stockoutService.getStockout(selectedFacilityCode, selectedProductCode);
 						//-----------------------------------------------------------------------------
 						
 						displayText = stockoutDao.getMenu(6) + " " + stockout.getIssue().getState().toString();
 						break;
 					case 3:
 						// displayText =
 						// stockoutDao.findNearestNeighbourWithStock(medicineName,facilityName);
 						
 						//-----------------------------------------------------------------------------
 						Facility closestFacility = facilityService.getNearestFacilityWithStock(selectedProduct, selectedFacility);
 						
 						//-----------------------------------------------------------------------------
 						
 						displayText = stockoutDao.getMenu(7) + " " + closestFacility.getLocalName();
 						break;
 					}
 
 					menuRequest = 99;
 
 				} else if (requestOption == 2) {
 
 					displayText = stockoutDao.getMenu(21);
 					//displayText += "1.Medicine1 \n2.Medicine2 \n3.Medicine3 \n4.Medicine4";
 
 					//String[] recentStockouts = { "Medicine1", "Medicine2",
 						//	"Medicine3", "Medicine4" };
 					//keyValueStore.put("recentStockouts." + sessionId,
 						//	recentStockouts);
 
 					// method that retrieves commonly reported stock out, from a
 					// clinic
 					// *****Need clinic name entered earlier
 					// StockoutDao.retrieveCommonStockouts(keyValueStore.get("facilityName."+request.getUssdSessionId()));
 					// **************************************************************
 					
 					//-----------------------------------------------------------------------------
 					int limit = 5;
 					//must be facility code, not facility name
 					String facilityCode = ((Facility) (keyValueStore.get("facilityName." + sessionId))).getUid();
 					//facilityCode.
 					List<Stockout> stockouts = stockoutService.getMostCommonlyReportedStockoutsForFacility(facilityCode, limit);
 					
 					keyValueStore.put("commonStockouts." + sessionId,stockouts);
 					
 					for(int index=0;index<stockouts.size();index++){
 						
 						displayText += (index+1) + "." + stockouts.get(index).getProduct().getName() + "\n";
 					}
 					
 					//-----------------------------------------------------------------------------
 
 					displayText += stockoutDao.getMenu(22);
 					menuRequest = 3;
 
 				} else if (requestOption == 3) {
 					displayText = ((Facility)keyValueStore
 							.get("facilityName." + sessionId)).getLocalName()
 							+ " "
 							+ stockoutDao.getMenu(1);
 					menuRequest = 2;
 
 				} else {
 
 					displayText = stockoutDao.getMenu(91);
 					throw new NumberFormatException();
 				}
 
 				break;
 
 			case 99:
 
 				keyValueStore.remove("facilityName." + sessionId);
 				keyValueStore.remove("service." + sessionId);
 				keyValueStore.remove("productName." + sessionId);
 				keyValueStore.remove("displayText." + sessionId);
 				keyValueStore.remove("requestId." + sessionId);
 				keyValueStore.remove("commonStockouts." +sessionId);
 
 			}
 		} catch (NumberFormatException e) {
 			// reload data from the last request if an error in the users input
 			// was detected
 			// do not call setResponse as there is no need to overwrite
 			// previously saved menu
 			// text with the exact same text. Also avoid concatenating multiple
 			// error messages
 
 			String strMenuRequest = (String) keyValueStore.get("requestId."
 					+ sessionId);
 			displayText += (String) keyValueStore.get("displayText."
 					+ sessionId);
 			response.setDisplayText(displayText);
 			response.setRequestID(strMenuRequest);
 
 			return response;
 		}
 
 		// set response once menu logic processing is complete
 		setResponse(response, displayText, menuRequest, sessionId);
 
 		return response;
 
 	}
 
 	// set the response object with menu text and menu level(request id), store
 	// these variables in
 	// key value store so they can be used for error handling if needed.
 	private void setResponse(CMUssdResponse response, String menuText,
 			int requestId, String sessionId) {
 
 		response.setDisplayText(menuText);
 		response.setRequestID(Integer.toString(requestId));
 		keyValueStore.put("displayText." + sessionId, menuText);
 		keyValueStore
 				.put("requestId." + sessionId, Integer.toString(requestId));
 	}
 
 }
