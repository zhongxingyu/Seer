 	package parkservice.resources;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.ext.ContextResolver;
 import javax.xml.bind.JAXBElement;
 
 import parkservice.gridservice.model.FindGridsByGPSCoordinateRequest;
 import parkservice.gridservice.model.FindGridsByGPSCoordinateResponse;
 import parkservice.gridservice.model.GetSpotLevelInfoRequest;
 import parkservice.gridservice.model.GetSpotLevelInfoResponse;
 import parkservice.gridservice.model.GetUpdatedGridInfoRequest;
 import parkservice.gridservice.model.GetUpdatedGridInfoResponse;
 import parkservice.gridservice.model.GetUpdatedSpotLevelInfoRequest;
 import parkservice.gridservice.model.GetUpdatedSpotLevelInfoResponse;
 import parkservice.gridservice.model.GetUpdatedStreetInfoRequest;
 import parkservice.gridservice.model.GetUpdatedStreetInfoResponse;
 import parkservice.gridservice.model.GpsCoorWithOrder;
 import parkservice.gridservice.model.GpsCoordinate;
 import parkservice.gridservice.model.ParkingSpaceWithStatus;
 import parkservice.gridservice.model.SearchArea;
 import parkservice.gridservice.model.SearchForStreetsRequest;
 import parkservice.gridservice.model.SearchForStreetsResponse;
 import parkservice.gridservice.model.SimpleParkingSpaceWithStatus;
 import parkservice.gridservice.model.UserLoginResponse;
 import parkservice.model.AuthRequest;
 import parkservice.model.ParkRequest;
 import parkservice.model.ParkResponse;
 import parkservice.model.ParkSync;
 import parkservice.model.RefillRequest;
 import parkservice.model.RefillResponse;
 import parkservice.model.UnparkRequest;
 import parkservice.model.UnparkResponse;
 import parkservice.userscore.model.AddUserActionLogRequest;
 import parkservice.userscore.model.AddUserActionLogResponse;
 import parkservice.userscore.model.AddUserReportingRequest;
 import parkservice.userscore.model.AddUserReportingResponse;
 import parkservice.userscore.model.GetCountRequest;
 import parkservice.userscore.model.GetCountResponse;
 import parkservice.userscore.model.GetUserReportingHistoryRequest;
 import parkservice.userscore.model.GetUserReportingHistoryResponse;
 import parkservice.userscore.model.GetUserScoreRequest;
 import parkservice.userscore.model.GetUserScoreResponse;
 import parkservice.userscore.model.UpdateUserScoreRequest;
 import parkservice.userscore.model.UpdateUserScoreResponse;
 import parkservice.userscore.model.UserParkingStatusReport;
 import AuthNet.Rebill.CreateCustomerProfileTransactionResponseType;
 import AuthNet.Rebill.OrderExType;
 import AuthNet.Rebill.ProfileTransAuthCaptureType;
 import AuthNet.Rebill.ProfileTransactionType;
 import AuthNet.Rebill.ServiceSoap;
 
 import com.parq.server.dao.LicensePlateDao;
 import com.parq.server.dao.MiscellaneousDao;
 import com.parq.server.dao.ParkingRateDao;
 import com.parq.server.dao.ParkingSpaceDao;
 import com.parq.server.dao.ParkingStatusDao;
 import com.parq.server.dao.PaymentAccountDao;
 import com.parq.server.dao.UserDao;
 import com.parq.server.dao.UserPrePaidAccountBalanceDao;
 import com.parq.server.dao.model.object.LicensePlate;
 import com.parq.server.dao.model.object.ParkingInstance;
 import com.parq.server.dao.model.object.ParkingRate;
 import com.parq.server.dao.model.object.ParkingSpace;
 import com.parq.server.dao.model.object.Payment;
 import com.parq.server.dao.model.object.Payment.PaymentType;
 import com.parq.server.dao.model.object.PaymentAccount;
 import com.parq.server.dao.model.object.User;
 import com.parq.server.dao.model.object.UserActionLog;
 import com.parq.server.dao.model.object.UserPrePaidAccountBalance;
 import com.parq.server.dao.model.object.UserScore;
 import com.parq.server.dao.model.object.UserSelfReporting;
 import com.parq.server.grid.GridManagementService;
 import com.parq.server.grid.model.object.GridWithFillRate;
 import com.parq.server.grid.model.object.ParkingLocationWithFillRate;
 
 @Path("/")
 public class ParkResource {
 	@Context 
 	ContextResolver<JAXBContextResolver> providers;
 	/**
 	 * returns User_ID, or -1 if bad
 	 * */
 	private User innerAuthenticate(AuthRequest in){
 		UserDao userDb = new UserDao();
 		User user = null;
 		try{
 			user = userDb.getUserByEmail(in.getEmail());
 		}catch(RuntimeException e){
 		}
 		if(user!=null&&user.getPassword().equals(in.getPassword())){
 			return user;
 		}else{
 			return null;
 		}
 	}
 
 	private CreateCustomerProfileTransactionResponseType chargeUser(int pay_amount, long profileId, long paymentProfileId, long uid){
 		java.math.BigDecimal amount = java.math.BigDecimal.valueOf(Double.parseDouble(""+(pay_amount/100)+"."+(pay_amount%100)));
 
 		//try to charge their payment profile the pay_amount
 		ProfileTransAuthCaptureType auth_capture = new ProfileTransAuthCaptureType();
 		auth_capture.setCustomerProfileId(profileId);
 		auth_capture.setCustomerPaymentProfileId(paymentProfileId);
 		auth_capture.setAmount(amount);
 		OrderExType order = new OrderExType();
 
 		Date x = new Date();
 		String dateString = (x.getMonth()+1>9 ? ""+(x.getMonth()+1): "0"+(x.getMonth()+1));
 		dateString+=(x.getDate()>9 ? ""+x.getDate(): "0"+x.getDate());
 		dateString+=x.getYear()+1900;
 		dateString+=(x.getHours()>9 ? ""+x.getHours(): "0"+x.getHours());
 		dateString+=(x.getMinutes()>9 ? ""+x.getMinutes(): "0"+x.getMinutes());
 
 		//for us, invoice set to uid:MMddyyyyhhmm
 		order.setInvoiceNumber(uid+":"+dateString);
 
 		auth_capture.setOrder(order);
 		ProfileTransactionType trans = new ProfileTransactionType();
 		trans.setProfileTransAuthCapture(auth_capture);
 
 		ServiceSoap soap = SoapAPIUtilities.getServiceSoap();
 		return soap.createCustomerProfileTransaction(SoapAPIUtilities.getMerchantAuthentication(), trans, null);
 	}
 
 	@POST
 	@Path("/pilotpark")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ParkResponse pilotParkUser(JAXBElement<ParkRequest> info){
 		System.out.println("ParkResource pilotpark is being called");
 		Date start = new Date();
 		ParkResponse output = new ParkResponse();
 		ParkRequest in = info.getValue();
 		//verify user
 		long uid = in.getUid();
 		//does user supplied info authenticate?
 		User user = innerAuthenticate(in.getUserInfo());
 		if(user != null && uid == user.getUserID()){
 			System.out.println("ParkResource pilotpark: user is authenticated: " + uid);
 			ParkingStatusDao psd = new ParkingStatusDao();
 			List<ParkingInstance> ps = null;
 			try{
 				ps = psd.getParkingStatusBySpaceIds(new long[]{in.getSpotId()});
 			}catch (Exception e){
 				System.out.println("ParkResource pilotpark: parking status retrival encoutered an error:");
 				e.printStackTrace();
 			}
 			//was user previously parked?
 			if(ps==null || ps.isEmpty() || ps.get(0).getParkingEndTime().compareTo(start) < 0){
 				System.out.println("ParkResource pilotpark: parking status was retrived correctly: " + ps);
 				long spot_id = in.getSpotId();
 
 				//nowtime is after previous end time.  user was not parked.  
 				int durationMinutes = in.getDurationMinutes();
 				Date end = new Date(); //end is iterations of increment + old time.  
 				long msec = 1000*durationMinutes*60;
 				end.setTime(start.getTime()+msec);
 				ParkingInstance newPark = new ParkingInstance();
 				newPark.setPaidParking(true);
 				newPark.setParkingBeganTime(start);
 				newPark.setParkingEndTime(end);
 				newPark.setUserId(uid);
 				newPark.setSpaceId(spot_id);
 				
 				// setup a dummy payment during the MIT pilot
 				Payment pilotPayment = new Payment();
 				pilotPayment.setPaymentType(PaymentType.PrePaid);
 				pilotPayment.setPaymentRefNumber("MIT_PILOT");
 				pilotPayment.setPaymentDateTime(new Date(System.currentTimeMillis()));
 				pilotPayment.setAmountPaidCents(0);
 				pilotPayment.setAccountId(0L);
 				newPark.setPaymentInfo(pilotPayment);
 				
 				System.out.println("ParkResource pilotpark: attempting to insert parking status");
 				
 				boolean result = false;
 				try{
 					result = psd.addNewParkingAndPayment(newPark);
 				}catch(Exception e){
 					System.out.println("add error");
 				}
 				//then set output to ok
 				if(result){
 					ParkingInstance finalInstance = psd.getUserParkingStatus(uid);
 					output.setParkingReferenceNumber(finalInstance.getParkingRefNumber());
 					output.setEndTime(end.getTime());
 					output.setResp("OK");
 					output.setStatusCode(0);
 
 					System.out.println("ParkResource pilotpark: parking status update was successful");
 					
 					// logic for updating the user points spend on parking
 					UserDao userDao = new UserDao();
 					UserScore currScore = userDao.getScoreForUser(uid);
 					currScore.setScore1(currScore.getScore1() - durationMinutes);
 					userDao.updateUserScore(currScore);
 					
 					try{
 						File file = new File("/user_logs/ParkRequests.txt");
 			            file.createNewFile();
 			            FileOutputStream fout = new FileOutputStream(file, true);
 						fout.write(("Park() asked at "+new Date() + " INFO: spot id=" + spot_id + " uid=" + uid).getBytes());
 						fout.close();
 					}catch (Exception e){//Catch exception if any
 						System.err.println("Error: " + e.getMessage());
 					}	
 				} else {
 					output.setStatusCode(-1000);
 					output.setResp("DAO_Error: unable to insert parking status for user");
 				}
 			} else {
 				//user is parked
 				output.setStatusCode(-1000);
 				output.setResp("USER_IS_PARKED");
 				System.out.println("ParkResources pilotPark: user:" + uid + " is currently parked");
 			}
 		} else {
 			//user doesn't exist
 			output.setStatusCode(-1000);
 			output.setResp("INVALID_USER");
 			System.out.println("ParkResources pilotPark: user:" + in.getUserInfo().getEmail() + " does not exist, or is deleted, or have duplicate");
 		}
 		return output;
 	}
 	
 	
 	@POST
 	@Path("/pilotunpark")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public UnparkResponse pilotUnparkUser(JAXBElement<UnparkRequest> info){
 		UnparkResponse output = new UnparkResponse();
 		UnparkRequest in = info.getValue();
 
 		System.out.println("ParkResources pilotunpark: attempt to unpark user");
 		
 		User user = innerAuthenticate(in.getUserInfo());
 		if(user != null && in.getUid() == user.getUserID()){
 			ParkingStatusDao psd = new ParkingStatusDao();
 			boolean result = false;
 			System.out.println("ParkResources pilotunpark: unpark user: " + in.getUid() + " parkingRefNum: " + in.getParkingReferenceNumber());
 			try{
 				long spotId = psd.getSpaceIdByParkingRefNum(in.getParkingReferenceNumber());
 				List<ParkingInstance> pi = psd.getParkingStatusBySpaceIds(new long[]{spotId});
 				Date curTime = new Date();
 				if (pi != null && !pi.isEmpty()) {
 					long secLeftOver = pi.get(0).getParkingEndTime().getTime() - curTime.getTime();
 					// if the user have more then 1 minute left on their parking, refund their difference
 					if (secLeftOver > 60000) {
 						//refund user's unused points
 						int leftOverPoints = (int) (secLeftOver / 60000);
 						UserDao userDao = new UserDao();
 						UserScore currScore = userDao.getScoreForUser(user.getUserID());
 						currScore.setScore1(currScore.getScore1() + leftOverPoints);
 						userDao.updateUserScore(currScore);
 					}
 				}
 				// unpark the user
 				result = psd.unparkBySpaceIdAndParkingRefNum(spotId,in.getParkingReferenceNumber(), curTime);
 				
 			}catch(Exception e){
 				System.out.println("ParkResources pilotunpark: unpark failed");
 				output.setResp("EXCEPTION CAUGHT.  spotid:" + in.getSpotId() + " refNum" + in.getParkingReferenceNumber());
 				return output;
 			}
 			if(result){
 				output.setResp("OK");
 			}else{
 				output.setStatusCode(-1000);
 				output.setResp("FAILED_TO_UNPARK_USER");
 			}
 		}else{
 			output.setStatusCode(-1000);
 			output.setResp("USER_AUTHENTICATION_FAILED");
 		}
 		
 		return output;
 	}
 	
 	@POST
 	@Path("/pilotrefill")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public RefillResponse pilotRefillTime(JAXBElement<RefillRequest> info){
 		RefillResponse  output = new RefillResponse ();
 		RefillRequest in = info.getValue();
 
 		System.out.println("ParkResources pilotunpark: attempt to refill parking for user: " + in.getUid());
 		
 		User user = innerAuthenticate(in.getUserInfo());
 		if(user != null && in.getUid() == user.getUserID()){
 			String parkingReference = in.getParkingReferenceNumber();
 			ParkingStatusDao psd = new ParkingStatusDao();
 			ParkingInstance pi = null; try{
 				pi =psd.getUserParkingStatus(in.getUid());
 			}catch(Exception e){
 				System.out.println("ParkResource pilotrefil: dao error for ParkingStatusDao trying to get user status uid: " + in.getUid());
 			}
 
 			if(pi!=null && pi.getParkingRefNumber().equals(parkingReference)){
 				long spotid = psd.getSpaceIdByParkingRefNum(parkingReference);
 				int durationMinutes = in.getDurationMinutes();
 
 				Date oldEndTime = pi.getParkingEndTime();
 				Date newEndTime= new Date();
 				long msec = 1000*durationMinutes*60;
 				newEndTime.setTime(oldEndTime.getTime()+msec);
 	
 				// setup a dummy payment during the MIT pilot
 				Payment pilotPayment = new Payment();
 				pilotPayment.setPaymentType(PaymentType.PrePaid);
 				pilotPayment.setPaymentRefNumber("MIT_PILOT");
 				pilotPayment.setPaymentDateTime(new Date(System.currentTimeMillis()));
 				pilotPayment.setAmountPaidCents(0);
 				pilotPayment.setAccountId(0L);
 				
 				boolean result = false; 
 				try{
 					result = psd.refillParkingForParkingSpace(spotid, newEndTime, pilotPayment);
 				}catch(Exception e){
 					System.out.println("ParkResource pilotrefil: dao error for ParkingStatusDao trying to refill user parking for user uid: " + in.getUid());
 				}
 				if(result){
 					ParkingInstance finalInstance = psd.getUserParkingStatus(in.getUid());
 					output.setParkingReferenceNumber(finalInstance.getParkingRefNumber());
 					output.setEndTime(newEndTime.getTime());
 					output.setResp("OK");
 					
 					// logic for updating the user points spend on parking
 					UserDao userDao = new UserDao();
 					UserScore currScore = userDao.getScoreForUser(user.getUserID());
 					currScore.setScore1(currScore.getScore1() - durationMinutes);
 					userDao.updateUserScore(currScore);
 					
 				}else{
 					output.setStatusCode(-1000);
 					output.setResp("FAILED_TO_UPDATE_USER_PARKING_STATUS");
 				}
 
 			}else{
 				output.setStatusCode(-1000);
 				output.setResp("USER_IS_NOT_CURRENTLY_PARKED");
 			}
 		}else{
 			output.setStatusCode(-1000);
 			output.setResp("USER_AUTHENTICATION_FAILED");
 		}
 
 		return output;
 	}
 
 	@POST
 	@Path("/park")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ParkResponse parkUser(JAXBElement<ParkRequest> info){
 		Date start = new Date(); //start off now
 		//check if user is currently parked.  
 		ParkResponse output = new ParkResponse();
 		ParkRequest in = info.getValue();
 
 		long uid = in.getUid();
 		//does user supplied info authenticate?
 		User user = innerAuthenticate(in.getUserInfo());
 		if(user != null && uid == user.getUserID()){
 
 			ParkingStatusDao psd = new ParkingStatusDao();
 			ParkingInstance ps = null;
 			try{
 				ps = psd.getUserParkingStatus(uid);
 			}catch (Exception e){}
 			//was user previously parked?
 			if(ps==null||ps.getParkingEndTime().compareTo(start)<0){
 
 				long spot_id = in.getSpotId();
 
 				//nowtime is after previous end time.  user was not parked.  
 				ParkingRateDao prd = new ParkingRateDao();
 				ParkingRate pr = null; 
 				try{
 					pr = prd.getParkingRateBySpaceId(spot_id);
 				}catch(Exception e){}
 				if(pr!=null){
 					int durationMinutes = in.getDurationMinutes();
 					int payment_amount = in.getChargeAmount();
 					int payment_type = in.getPaymentType();
 
 					int iterations = durationMinutes/(pr.getTimeIncrementsMins());
 					if(iterations*pr.getParkingRateCents()==payment_amount){
 						//if the price, duration, and rate supplied match up,
 						Date end = new Date(); //end is iterations of increment + old time.  
 						long msec = 1000*durationMinutes*60;
 						end.setTime(start.getTime()+msec);
 
 
 						PaymentAccountDao pad = new PaymentAccountDao();
 						List<PaymentAccount> pad_list = null;
 						try{
 							pad_list = pad.getAllPaymentMethodForUser(uid);
 						}catch(Exception e){}
 
 						long profileId = -1;
 						long paymentProfileId = -1;
 						long account_id = -1;
 						if(pad_list!=null && pad_list.size()>0){
 							PaymentAccount pa = pad_list.get(0);
 							profileId = Integer.parseInt(pa.getCustomerId()); //this is profileId
 							paymentProfileId = Integer.parseInt(pa.getPaymentMethodId()); //this is paymentProfileId
 							account_id = pa.getAccountId();
 
 						}
 
 						if(paymentProfileId>0){
 							CreateCustomerProfileTransactionResponseType response = chargeUser(payment_amount, profileId, paymentProfileId, uid);
 							if(response.getResultCode().value().equalsIgnoreCase("Ok")){
 								//if charge completes, store parking instance into db
 								//mark user as parked
 								ParkingInstance newPark = new ParkingInstance();
 								newPark.setPaidParking(true);
 								newPark.setParkingBeganTime(start);
 								newPark.setParkingEndTime(end);
 								newPark.setUserId(uid);
 								newPark.setSpaceId(spot_id);
 
 								Payment p = new Payment();
 								p.setAmountPaidCents(payment_amount);
 								p.setPaymentDateTime(start);
 								p.setPaymentType(PaymentType.CreditCard);
 								p.setPaymentRefNumber(Arrays.asList(response.getDirectResponse().split(",")).get(6));
 								p.setAccountId(account_id);
 								newPark.setPaymentInfo(p);
 
 								boolean result = false;
 								try{
 									result = psd.addNewParkingAndPayment(newPark);
 								}catch(Exception e){}
 								//then set output to ok
 								if(result){
 									ParkingInstance finalInstance = psd.getUserParkingStatus(uid);
 									output.setParkingReferenceNumber(finalInstance.getParkingRefNumber());
 									output.setEndTime(end.getTime());
 									output.setResp("OK");
 
 
 									try{
 										File file = new File("/user_logs/ParkRequests.txt");
 							            file.createNewFile();
 							            FileOutputStream fout = new FileOutputStream(file, true);
 										fout.write(("Park() asked at "+new Date() + " INFO: spot id=" + spot_id + " uid=" + uid).getBytes());
 										fout.close();
 									}catch (Exception e){//Catch exception if any
 										System.err.println("Error: " + e.getMessage());
 									}
 									
 								}else{
 									output.setResp("DAO_ERROR");
 								}
 							}else{
 								output.setResp("BAD_PAY");
 							}
 						}else{
 							output.setResp("NO_PAYMENT_PROFILE");
 						}
 
 					}
 
 					//send back info for app to display new session.  
 
 				}else{
 					output.setResp("parkrate doesn't exist, check spotid");
 				}
 			}else{
 				//nowtime is before previous end time.  user currently parked.  
 				//THIS SHOULD NEVER HAPPEN.  ONLY occurs if two users log into separate apps, then one parks, the other still logged in.  
 				//alert user that they're currently parked somewhere else.  
 				output.setResp("USER_PARKED");
 			}
 
 		}else{
 			output.setResp("BAD_AUTH, check login fields or uid");
 		}
 		
 		return output;
 	}
 
 	@POST
 	@Path("/refill")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public RefillResponse refillTime(JAXBElement<RefillRequest> info){
 		RefillResponse  output = new RefillResponse ();
 		RefillRequest in = info.getValue();
 		long uid = in.getUid();
 		int payment_type = in.getPaymentType();
 		
 		User user = innerAuthenticate(in.getUserInfo());
 		if(user != null && uid == user.getUserID()){
 			String parkingReference = in.getParkingReferenceNumber();
 			ParkingStatusDao psd = new ParkingStatusDao();
 			ParkingInstance pi = null; try{
 				pi =psd.getUserParkingStatus(uid);
 			}catch(Exception e){}
 
 			if(pi!=null && pi.getParkingRefNumber().equals(parkingReference)){
 				long spotid = psd.getSpaceIdByParkingRefNum(parkingReference);
 				int durationMinutes = in.getDurationMinutes();
 				int pay_amount = in.getChargeAmount();
 				ParkingRateDao prd = new ParkingRateDao();
 				ParkingRate pr = null;
 				try{
 					pr = prd.getParkingRateBySpaceId(spotid);
 				}catch(Exception e){}
 				if(pr!=null && pay_amount/pr.getParkingRateCents()==durationMinutes/pr.getTimeIncrementsMins()){
 					//if the rate, amount, and duration match up,
 					Date oldEndTime = pi.getParkingEndTime();
 					Date newEndTime= new Date();
 					long msec = 1000*durationMinutes*60;
 					newEndTime.setTime(oldEndTime.getTime()+msec);
 
 					//get user payment information.  
 					PaymentAccountDao pad = new PaymentAccountDao();
 					List<PaymentAccount> pad_list = null; 
 					try{
 						pad_list =pad.getAllPaymentMethodForUser(uid);
 					}catch(Exception e){}
 					int profileId = -1;
 					int paymentProfileId = -1;
 					long account_id = -1;
 					if(pad_list!=null && pad_list.size()>0){
 						PaymentAccount pa = pad_list.get(0);
 						profileId = Integer.parseInt(pa.getCustomerId()); //this is profileId
 						paymentProfileId = Integer.parseInt(pa.getPaymentMethodId()); //this is paymentProfileId
 						account_id = pa.getAccountId();
 					}
 					if(paymentProfileId>0){
 						//charge user for refill
 						CreateCustomerProfileTransactionResponseType response = chargeUser(pay_amount, profileId, paymentProfileId, uid);
 						if(response.getResultCode().value().equalsIgnoreCase("Ok")){
 							//update databases
 
 							ParkingInstance newPark = new ParkingInstance();
 							newPark.setPaidParking(true);
 							newPark.setParkingBeganTime(oldEndTime);
 							newPark.setParkingEndTime(newEndTime);
 							newPark.setUserId(uid);
 							newPark.setSpaceId(spotid);
 
 							Payment p = new Payment();
 							p.setAmountPaidCents(pay_amount);
 							p.setPaymentDateTime(oldEndTime);
 							p.setPaymentType(PaymentType.CreditCard);
 							p.setPaymentRefNumber(Arrays.asList(response.getDirectResponse().split(",")).get(6));
 							p.setAccountId(account_id);
 							newPark.setPaymentInfo(p);
 
 							boolean result = false; 
 							try{
 								result = psd.refillParkingForParkingSpace(spotid, newEndTime, p);
 							}catch(Exception e){}
 							if(result){
 								ParkingInstance finalInstance = psd.getUserParkingStatus(uid);
 								output.setParkingReferenceNumber(finalInstance.getParkingRefNumber());
 								output.setEndTime(newEndTime.getTime());
 								output.setResp("OK");
 							}else{
 								output.setResp("WAS_NOT_PARKED");
 							}
 
 						}else{
 							output.setResp("BAD_PAY");
 						}
 					}else{
 						output.setResp("NO_PAY_PROFILE");
 					}
 				}else{
 					output.setResp("PR may be null, or 3way check failed");
 				}
 
 			}else{
 				output.setResp("BAD_INST_REF");
 			}
 		}else{
 			output.setResp("BAD_AUTH");
 		}
 
 		return output;
 	}
 
 	@POST
 	@Path("/unpark")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public UnparkResponse unparkUser(JAXBElement<UnparkRequest> info){
 		UnparkResponse  output = new UnparkResponse ();
 		UnparkRequest in = info.getValue();
 		long uid = in.getUid();
 		
 		User user = innerAuthenticate(in.getUserInfo());
 		if(user != null && uid == user.getUserID()){
 			ParkingStatusDao psd = new ParkingStatusDao();
 			boolean result = false;
 			try{
 				long spotId = psd.getSpaceIdByParkingRefNum(in.getParkingReferenceNumber());
 				result = psd.unparkBySpaceIdAndParkingRefNum(spotId,in.getParkingReferenceNumber(), new Date());
 			}catch(Exception e){
 				output.setResp("EXCEPTION CAUGHT.  spotid:" + in.getSpotId() + " refNum" + in.getParkingReferenceNumber());
 				return output;
 			}
 			if(result){
 				output.setResp("OK");
 			}else{
 				output.setResp("dao FALSE.  spotid:" + in.getSpotId() + " refNum" + in.getParkingReferenceNumber());
 			}
 
 		}else{
 			output.setResp("BAD_AUTH");
 		}
 		return output;
 	}
 
 	@GET
 	@Produces(MediaType.TEXT_PLAIN)
 	public String sayPlainTextHello1() {
 		return "Peter Parker: I love you Mary Jane!";
 	}
 	
 	
 	
 	/****************************************************
 	 *
 	 * New Grid search and management methods added by Gordon
 	 *
 	 ***************************************************/
 	
 	
 	@POST
 	@Path("/FindGridsByGPSCoordinateRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<FindGridsByGPSCoordinateResponse> findGridByGPSCoor(JAXBElement<FindGridsByGPSCoordinateRequest> jaxbRequest){
 		FindGridsByGPSCoordinateRequest request = jaxbRequest.getValue();
 		long lastUpdateDateTime = request.getLastUpdateTime();
 		List<SearchArea> searchArea = request.getSearchArea();
 		
 		// get all the grids with fill status with in the given bounding box
 		List<GridWithFillRate> gridWFillRate = getGridWithFillRates(searchArea);
 		
 		ArrayList<FindGridsByGPSCoordinateResponse> responseList = new ArrayList<FindGridsByGPSCoordinateResponse>();
 		for (GridWithFillRate grid: gridWFillRate) {
 			// only send the items that has been recently updated
 			if (grid.getLastUpdatedDateTime() > lastUpdateDateTime) {
 				FindGridsByGPSCoordinateResponse response = new FindGridsByGPSCoordinateResponse();
 				response.setGridId(grid.getGridId());
 				response.setFillRate(1.0 * grid.getFillRate() / 100);
 				response.setLatitude(grid.getLatitude());
 				response.setLongitude(grid.getLongitude());
 				responseList.add(response);
 			}
 		}
 		
 		// return the array representation of the FindGridsByGPSCoordinateResponse
 		return responseList;
 	}
 	
 	@POST
 	@Path("/GetUpdatedGridInfoRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<GetUpdatedGridInfoResponse> getUpdatedGridInfo(
 				JAXBElement<GetUpdatedGridInfoRequest> jaxbRequest){
 		GridManagementService gridService = GridManagementService.getInstance();
 		GetUpdatedGridInfoRequest request = jaxbRequest.getValue();
 		List<Long> gridList = new ArrayList<Long>();
 		for (long gridId : request.getGridIds()) {
 			gridList.add(gridId);
 		}
 		
 		List<GridWithFillRate> gridFillRateList = gridService.getGridStatus(gridList);
 		ArrayList<GetUpdatedGridInfoResponse> responses = new ArrayList<GetUpdatedGridInfoResponse>();
 		for (int i = 0; i < gridFillRateList.size(); i++) {
 			GetUpdatedGridInfoResponse grid = new GetUpdatedGridInfoResponse();
 			grid.setFillRate((0.0 + gridFillRateList.get(i).getFillRate()) / 100);
 			grid.setGridId(gridFillRateList.get(i).getGridId());
 			responses.add(grid);
 		}
 		return responses;
 	}
 	
 	private List<GridWithFillRate> getGridWithFillRates(
 			List<SearchArea> searchArea) {
 		
 		Set<GridWithFillRate> gridWFillRate = new HashSet<GridWithFillRate>();
 		GridManagementService gridService = GridManagementService.getInstance();
 		
 		// go through all the bounding boxes
 		for (SearchArea se : searchArea) {
 			GpsCoordinate northEastCorner = se.getNorthEastCorner();
 			GpsCoordinate southWestCorner = se.getSouthWestCorner();
 			
 			// get all the grids with fill status with in the given bounding box
 			List<GridWithFillRate> tempWithFillRate = gridService.findGridWithFillrateByGPSCoor(
 					northEastCorner.getLatitude(), northEastCorner.getLongitude(), 
 					southWestCorner.getLatitude(), southWestCorner.getLongitude());
 			
 			gridWFillRate.addAll(tempWithFillRate);
 		}
 		return new ArrayList<GridWithFillRate>(gridWFillRate);
 	}
 
 	@POST
 	@Path("/SearchForStreetsRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<SearchForStreetsResponse> searchForStreets(JAXBElement<SearchForStreetsRequest> jaxbRequest){
 		
 		SearchForStreetsRequest request = jaxbRequest.getValue();
 		List<SearchArea> searchArea = request.getSearchArea();
 		// get all the Parking Blocks with fill status within the given gps bounding box
 		List<ParkingLocationWithFillRate> parkingLocationsWithFillRate = getParkingLocaitonWithFillRate(searchArea);
 
 		ArrayList<SearchForStreetsResponse> responseList = new ArrayList<SearchForStreetsResponse>();
 		// create the individual street response
 		for (ParkingLocationWithFillRate pl : parkingLocationsWithFillRate) {
 			SearchForStreetsResponse response = new SearchForStreetsResponse();
 			response.setFillRate(1.0 * pl.getFillRate() / 100);
 			response.setStreetId(pl.getLocationId());
 			// set the gps coordinate for the streets
 			for (int i = 0; i < pl.getGeoPoints().size(); i++) {
 				GpsCoorWithOrder gpsCoor = new GpsCoorWithOrder();
 				gpsCoor.setLatitude(pl.getGeoPoints().get(i).getLatitude());
 				gpsCoor.setLongitude(pl.getGeoPoints().get(i).getLongitude());
 				gpsCoor.setOrder(i);
 				response.getGpsCoor().add(gpsCoor);
 			}
 			responseList.add(response);
 		}
 		
 		// return the array representation of the StreetStatusList
 		return responseList;
 	}
 	
 	private List<ParkingLocationWithFillRate> getParkingLocaitonWithFillRate(
 			List<SearchArea> searchArea) {
 		
 		Set<ParkingLocationWithFillRate> resultSet = new HashSet<ParkingLocationWithFillRate>();
 		GridManagementService gridService = GridManagementService.getInstance();
 		
 		// go through all the bounding boxes
 		for (SearchArea se : searchArea) {
 			GpsCoordinate northEastCorner = se.getNorthEastCorner();
 			GpsCoordinate southWestCorner = se.getSouthWestCorner();
 		
 			List<ParkingLocationWithFillRate> parkingLocationsWithFillRate = gridService
 				.findStreetByGPSCoor(					
 						northEastCorner.getLatitude(), northEastCorner.getLongitude(), 
 						southWestCorner.getLatitude(), southWestCorner.getLongitude());
 			
 			resultSet.addAll(parkingLocationsWithFillRate);
 		}
 
 		return new ArrayList<ParkingLocationWithFillRate>(resultSet);
 	}
 
 	@POST
 	@Path("/GetUpdatedStreetInfoRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<GetUpdatedStreetInfoResponse> getUpdatedStreetInfo(JAXBElement<GetUpdatedStreetInfoRequest> jaxbRequest){
 		GetUpdatedStreetInfoRequest request = jaxbRequest.getValue();
 		long lastUpdateDateTime = request.getLastUpdateTime();
 		List<SearchArea> searchArea = request.getSearchArea();
 		
 		ArrayList<GetUpdatedStreetInfoResponse> responseList = new ArrayList<GetUpdatedStreetInfoResponse>();
 		List<ParkingLocationWithFillRate> parkingLocationsWithFillRate = 
 				getParkingLocaitonWithFillRate(searchArea);
 		
 		// create the individual street response
 		for (ParkingLocationWithFillRate pl : parkingLocationsWithFillRate) {
 			if (pl.getLastUpdatedDateTime() > lastUpdateDateTime) {
 				GetUpdatedStreetInfoResponse response = new GetUpdatedStreetInfoResponse();
 				response.setFillRate(1.0 * pl.getFillRate() / 100);
 				response.setStreetId(pl.getLocationId());
 				responseList.add(response);
 			}
 		}
 
 		// return the array representation of the GetUpdatedStreetInfoResponse
 		return responseList;
 	}
 	
 	@POST
 	@Path("/GetStreetInfoRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<GetSpotLevelInfoResponse> getStreetInfo(JAXBElement<GetSpotLevelInfoRequest> jaxbRequest){
 		
 		GetSpotLevelInfoRequest request = jaxbRequest.getValue();
 		List<SearchArea> searchArea = request.getSearchArea();
 		
 		ArrayList<GetSpotLevelInfoResponse> responseList = new ArrayList<GetSpotLevelInfoResponse>();
 		
 		// get all the Parking Blocks with fill status within the given gps bounding box
 		Set<ParkingLocationWithFillRate> parkingLocationsWithFillRate = 
 			new HashSet<ParkingLocationWithFillRate>(getParkingLocaitonWithFillRate(searchArea));
 		
 		// for each of the parking block, get all the parking space information inside the block
 		for (ParkingLocationWithFillRate pl : parkingLocationsWithFillRate) {
 			GetSpotLevelInfoResponse response = new GetSpotLevelInfoResponse();
 			response.setStreetId(pl.getLocationId());
 			response.setFillRate(1.0 * pl.getFillRate() / 100);
 			
 			// insert the spaces list
 			for (ParkingSpace ps :  pl.getSpaces()) {
 				ParkingSpaceWithStatus spaceWithStatus = new ParkingSpaceWithStatus();
 				spaceWithStatus.setSpaceId(ps.getSpaceId());
 				spaceWithStatus.setSpaceName(ps.getSpaceIdentifier());
 				spaceWithStatus.setStreetId(pl.getLocationId());
 				spaceWithStatus.setLatitude(ps.getLatitude());
 				spaceWithStatus.setLongitude(ps.getLongitude());
 				spaceWithStatus.setSegment(ps.getSegment());
 				// set the parking space status
 				if (pl.isSpaceAvaliable(ps.getSpaceId())) {
 					spaceWithStatus.setStatus("available");
 				} else {
 					spaceWithStatus.setStatus("parked");
 				}
 				response.getParkingSpace().add(spaceWithStatus);
 			}
 			responseList.add(response);
 		}
 		
 		// return the array representation of the GetUpdatedStreetInfoResponse
 		return responseList;
 	}
 	
 	@POST
 	@Path("/GetUpdatedSpotLevelInfoRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public ArrayList<GetUpdatedSpotLevelInfoResponse>
 			getUpdatedSpotLevelInfo(JAXBElement<GetUpdatedSpotLevelInfoRequest> jaxbRequest) {
 		
 		GetUpdatedSpotLevelInfoRequest request = jaxbRequest.getValue();
 		List<SearchArea> searchArea = request.getSearchArea();
 		
 		ArrayList<GetUpdatedSpotLevelInfoResponse> responseList = new ArrayList<GetUpdatedSpotLevelInfoResponse>();
 		
 		// get all the Parking Blocks with fill status within the given gps bounding box
 		Set<ParkingLocationWithFillRate> parkingLocationsWithFillRate = new HashSet<ParkingLocationWithFillRate>(
 				getParkingLocaitonWithFillRate(searchArea));
 		
 		// for each of the parking block, get all the parking space information inside the block
 		for (ParkingLocationWithFillRate pl : parkingLocationsWithFillRate) {
 			GetUpdatedSpotLevelInfoResponse response = new GetUpdatedSpotLevelInfoResponse();
 			response.setStreetId(pl.getLocationId());
 			
 			// insert the spaces list
 			for (ParkingSpace ps :  pl.getSpaces()) {
 				SimpleParkingSpaceWithStatus spaceWithStatus = new SimpleParkingSpaceWithStatus();
 				spaceWithStatus.setSpaceId(ps.getSpaceId());
 				// set the parking space status
 				if (pl.isSpaceAvaliable(ps.getSpaceId())) {
 					spaceWithStatus.setStatus("available");
 				} else {
 					spaceWithStatus.setStatus("parked");
 				}
 				response.getParkingSpace().add(spaceWithStatus);
 			}
 			responseList.add(response);
 		}
 		
 		// return the array representation of the GetUpdatedSpotLevelInfoResponse
 		return responseList;
 	}
 
 	@POST
 	@Path("/GetUserScoreRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public GetUserScoreResponse
 			getUserScore(JAXBElement<GetUserScoreRequest> jaxbRequest) {
 		
 		GetUserScoreRequest request = jaxbRequest.getValue();
 		// call the helper method to get the user score
 		return getUserScore(request);
 	}
 	
 	private GetUserScoreResponse getUserScore(GetUserScoreRequest request) {
 		UserDao userDao = new UserDao();
 		UserScore userScore = userDao.getScoreForUser(request.getUserId());
 		
 		GetUserScoreResponse score = new GetUserScoreResponse();
 		if (userScore != null) {
 			score.setScoreId(userScore.getScoreId());
 			score.setUserId(userScore.getUserId());
 			score.setScore1(userScore.getScore1());
 			score.setScore2(userScore.getScore2());
 			score.setScore3(userScore.getScore3());
 		}
 		return score;
 	}
 
 	@POST
 	@Path("/UpdateUserScoreRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public UpdateUserScoreResponse
 			updateUserScore(JAXBElement<UpdateUserScoreRequest> jaxbRequest) {
 		UpdateUserScoreRequest request = jaxbRequest.getValue();
 		// call the private helper method to peform the update
 		return updateUserScore(request);
 	}
 	
 	private UpdateUserScoreResponse updateUserScore(
 			UpdateUserScoreRequest request) {
 		
 		UserDao userDao = new UserDao();
 		UserScore uScore = new UserScore();
 		uScore.setScoreId(request.getScoreId());
 		uScore.setUserId(request.getUserId());
 		uScore.setScore1(request.getScore1());
 		uScore.setScore2(request.getScore2());
 		uScore.setScore3(request.getScore3());
 		
 		boolean updateSuccessful = userDao.updateUserScore(uScore);
 		UpdateUserScoreResponse response = new UpdateUserScoreResponse();
 		response.setUpdateSuccessful(updateSuccessful);
 		return response;
 	}
 
 	@POST
 	@Path("/GetCountRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public GetCountResponse getCount(JAXBElement<GetCountRequest> countRequest) {
 		MiscellaneousDao mDao = new MiscellaneousDao();
 		long countId = mDao.getNextCount();
 		GetCountResponse response = new GetCountResponse();
 		response.setCount((int)(countId % 4));
 		return response;
 	}
 	
 	@POST
 	@Path("/AddUserReportingRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public AddUserReportingResponse addUserReporting(JAXBElement<AddUserReportingRequest> jaxbRequest) {
 		AddUserReportingRequest request = jaxbRequest.getValue();
 		ParkingStatusDao statusDao = new ParkingStatusDao();
 		MiscellaneousDao mDao = new MiscellaneousDao();
 		
 		String status = "AVAILABLE";
 		Date curTime = new Date(System.currentTimeMillis());
	
 		
 		long[] spaceIds =  new long[request.getSpaceIds().size()];
 		for (int i = 0; i < request.getSpaceIds().size(); i ++) {
 			spaceIds[i] = request.getSpaceIds().get(i);
 		}
 		List<ParkingInstance> parkingInstance = 
 			statusDao.getParkingStatusBySpaceIds(spaceIds);
 		
 		List<Long> spaces = new ArrayList<Long>();
 		List<String> statuses = new ArrayList<String>(); 
 		
 		for (int i = 0; i < spaceIds.length; i++ ) {
 			spaces.add(spaceIds[i]);
 			status = "AVAILABLE";
 			// use a loop to search for the space status
 			for (int j = 0; j < parkingInstance.size(); j++) {
 				if (parkingInstance.get(j).getSpaceId() == spaceIds[i] &&
 						parkingInstance.get(j).getParkingEndTime().compareTo(curTime) > 0) {
 					status = "PARKED";
 				}
 			}
 			statuses.add(status);
 		}
 		
 		UserSelfReporting report = new UserSelfReporting();
 		report.setReportDateTime(curTime);
 		report.setUserId(request.getUserId());
 		report.setSpaceIds(spaces);
 		report.setParkingSpaceStatus(statuses);
 		report.setScore1(request.getScore1());
 		report.setScore2(request.getScore2());
 		report.setScore3(request.getScore3());
 		report.setScore4(request.getScore4());
 		report.setScore5(request.getScore5());
 		report.setScore6(request.getScore6());
 		
 		boolean isSucccessful = mDao.insertUserSelfReporting(report);
 		
		AddUserReportingResponse response = new AddUserReportingResponse();
		// check to make sure that the user can only report between 8am and 6pm
		if (curTime.getHours() < 7 || curTime.getHours() >= 17) {
			response.setUpdateSuccessful(false);
			response.setResp("ONLY_ALLOW_TO_REPORT_BETWEEN_8AM_AND_6PM");
			response.setStatusCode(-10);
			return response;
		}
		
 		if (isSucccessful) {
 			boolean hasUserReportTwiceAlready = false;
 			boolean oneMinBetweenReportCheck = true;
 			List<UserSelfReporting> userReports = mDao.getUserSelfReportingHistoryForUser(request.getUserId());
 			int todayReport = 0;
 			Date halfDayAgo = new Date(System.currentTimeMillis() - (1000 * 60 * 12));
 			
 			for (UserSelfReporting uReport : userReports) {
 				if (uReport.getReportDateTime().after(halfDayAgo)) {
 					todayReport++;
 					long timeDiff = (curTime.getTime() - uReport.getReportDateTime().getTime());
 					// make sure that user have at least 1 minute difference between 2 back to back reports
 					oneMinBetweenReportCheck &= (timeDiff > 60000L);
 				}
 			}
 			hasUserReportTwiceAlready = todayReport >= 3;
 			
 			if (!hasUserReportTwiceAlready && oneMinBetweenReportCheck) {
 				// update the user scores
 				GetUserScoreRequest getRequest = new GetUserScoreRequest();
 				getRequest.setUserId(request.getUserId());
 				GetUserScoreResponse getResponse = getUserScore(getRequest);
 				
 				UpdateUserScoreRequest updateRequest = new UpdateUserScoreRequest();
 				updateRequest.setUserId(request.getUserId());
 				// user get 10 points for each space status they report
 				updateRequest.setScore1(getResponse.getScore1() + (spaceIds.length * 10));
 				updateRequest.setScore2(getResponse.getScore2());
 				updateRequest.setScore3(getResponse.getScore3());
 				UpdateUserScoreResponse updateResponse = updateUserScore(updateRequest);
 				
 				response.setUpdateSuccessful(updateResponse.isUpdateSuccessful());
 				response.setResp("OK");
 				response.setStatusCode(0);
 				if (!updateResponse.isUpdateSuccessful()){
 					System.out.println("failed to give user points for reporting");
 				}
 			} else {
 				response.setUpdateSuccessful(false);
 				response.setResp("USER_REPORTED_TWICE_ALREADY");
 				response.setStatusCode(-5);
 			}
 		} else {
 			response.setUpdateSuccessful(false);
 			response.setResp("USER_REPORTED_INVALID");
 			response.setStatusCode(-1000);
 		}
 		
 		
 		return response;
 	}
 	
 	@POST
 	@Path("/GetUserReportingHistoryRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public GetUserReportingHistoryResponse getUserReportingHistory(
 			JAXBElement<GetUserReportingHistoryRequest> jaxbRequest) {
 	
 		GetUserReportingHistoryRequest request = jaxbRequest.getValue();
 		MiscellaneousDao mDao = new MiscellaneousDao();
 		List<UserSelfReporting> selfReports = mDao.getUserSelfReportingHistoryForUser(request.getUserId());
 		
 		List<UserParkingStatusReport> reports = new ArrayList<UserParkingStatusReport>();
 		for (UserSelfReporting sReport : selfReports) {
 			UserParkingStatusReport report = new UserParkingStatusReport();
 			report.setReportId(sReport.getReportId());
 			report.setSpaceIds(sReport.getSpaceIds());
 			report.setStatus(sReport.getParkingSpaceStatus());
 			report.setUserId(sReport.getUserId());
 			report.setReportDateTime(sReport.getReportDateTime());
 			report.setScore1(sReport.getScore1());
 			report.setScore2(sReport.getScore2());
 			report.setScore3(sReport.getScore3());
 			report.setScore4(sReport.getScore4());
 			report.setScore5(sReport.getScore5());
 			report.setScore6(sReport.getScore6());
 			reports.add(report);
 		}
 		
 		GetUserReportingHistoryResponse response = new GetUserReportingHistoryResponse();
 		response.setReports(reports);
 		return response;
 	}
 	
 	@POST
 	@Path("/AuthRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public UserLoginResponse login(JAXBElement<AuthRequest> jaxbRequest) {
 		AuthRequest authRequest = jaxbRequest.getValue();
 		Date nowTime = new Date();
 		UserLoginResponse response = new UserLoginResponse();
 		User user = innerAuthenticate(authRequest);
 		if(user != null){
 			response.setAutherized(true);
 			response.setEmail(authRequest.getEmail());
 			response.setUid(user.getUserID());
 			//Get license plate
 			LicensePlateDao lpDao = new LicensePlateDao();
 			List<LicensePlate> plates = lpDao.getLicensePlateByUserId(user.getUserID());
 			if (plates != null && !plates.isEmpty()) {
 				response.setLicense(plates.get(0).getPlateNum());
 			}
 			// Get Prepaid account
 			UserPrePaidAccountBalanceDao prePaidDao = new UserPrePaidAccountBalanceDao();
 			UserPrePaidAccountBalance balance = 
 						prePaidDao.getUserPrePaidAccountBalance(user.getUserID());
 			if (balance != null) {
 				response.setBalance(balance.getAccountBalance());
 			}
 			//now check to see if user is parked.  
 			ParkingStatusDao psd = new ParkingStatusDao();
 			Date endTime = null;
 			ParkingInstance pi = null;
 			try{
 				pi = psd.getUserParkingStatus(user.getUserID());
 				endTime = pi.getParkingEndTime();
 				if(endTime.compareTo(nowTime)>0){
 					// user is currently parked.
 					ParkingRateDao prd = new ParkingRateDao();
 					ParkSync sync = new ParkSync();
 					// ParkingRate pr = prd.getParkingRateBySpaceId(pi.getSpaceId());
 					// TODO for the pilot the above call return null value. so disabled it for now.
 					ParkingSpaceDao psdao = new ParkingSpaceDao();
 					ParkingSpace pspace = psdao.getParkingSpaceBySpaceId(pi.getSpaceId());
 					sync.setLocation(pspace.getSpaceName());
 					sync.setSpotNumber(pspace.getSpaceIdentifier());
 					sync.setEndTime(endTime.getTime());
 					sync.setParkingReferenceNumber(pi.getParkingRefNumber());
 					sync.setDefaultRate(1);
 					sync.setMaxTime(120);
 					sync.setMinIncrement(1);
 					sync.setMinTime(15);
 					sync.setSpotId(pi.getSpaceId());
 					response.setSync(sync);
 					//else, user was parked and endTime has passed.
 				}
 			}catch(Exception e){
 				e.printStackTrace(System.out); 
 			}
 			
 		} else {
 			response.setAutherized(false);
 		}
 		return response;
 	}
 	
 	@POST
 	@Path("/AddUserActionLogRequest")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public AddUserActionLogResponse addUserActionLogs(JAXBElement<AddUserActionLogRequest> jaxbRequest) {
 		AddUserActionLogRequest logRequest = jaxbRequest.getValue();
 		
 		MiscellaneousDao miscDao = new MiscellaneousDao();
 		UserActionLog actionLog = new UserActionLog();
 		actionLog.setUserId(logRequest.getUserId());
 		actionLog.setLog(logRequest.getLog());
 		
 		AddUserActionLogResponse response = new AddUserActionLogResponse();
 		boolean isSuccessful = miscDao.insertUserActionLogging(actionLog);
 		response.setSuccessful(isSuccessful);
 		
 		return response;
 	}
 }
