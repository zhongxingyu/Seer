 package si.kcclass.deliverycost.services;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 
 import org.apache.axis.types.NonNegativeInteger;
 
 import com.fedex.rate.stub.Address;
 import com.fedex.rate.stub.ClientDetail;
 import com.fedex.rate.stub.Dimensions;
 import com.fedex.rate.stub.DropoffType;
 import com.fedex.rate.stub.LinearUnits;
 import com.fedex.rate.stub.Money;
 import com.fedex.rate.stub.NotificationSeverityType;
 import com.fedex.rate.stub.PackageRateDetail;
 import com.fedex.rate.stub.PackageSpecialServicesRequested;
 import com.fedex.rate.stub.PackagingType;
 import com.fedex.rate.stub.Party;
 import com.fedex.rate.stub.Payment;
 import com.fedex.rate.stub.PaymentType;
 import com.fedex.rate.stub.RatePortType;
 import com.fedex.rate.stub.RateReply;
 import com.fedex.rate.stub.RateReplyDetail;
 import com.fedex.rate.stub.RateRequest;
 import com.fedex.rate.stub.RateRequestType;
 import com.fedex.rate.stub.RateServiceLocator;
 import com.fedex.rate.stub.RatedPackageDetail;
 import com.fedex.rate.stub.RatedShipmentDetail;
 import com.fedex.rate.stub.RequestedPackageLineItem;
 import com.fedex.rate.stub.RequestedShipment;
 import com.fedex.rate.stub.ServiceType;
 import com.fedex.rate.stub.ShipmentRateDetail;
 import com.fedex.rate.stub.Surcharge;
 import com.fedex.rate.stub.TransactionDetail;
 import com.fedex.rate.stub.VersionId;
 import com.fedex.rate.stub.WebAuthenticationCredential;
 import com.fedex.rate.stub.WebAuthenticationDetail;
 import com.fedex.rate.stub.Weight;
 import com.fedex.rate.stub.WeightUnits;
 
 import si.kcclass.deliverycost.domain.DeliveryCostRequest;
 
 public class FedexDeliveryCostService implements DeliveryCostService {
 
 	@Override
 	public Double getDeliveryCost(DeliveryCostRequest request) {
 		// Build a RateRequest request object
 		boolean getAllRatesFlag = false; // set to true to get the rates for different service types
 	    RateRequest fedexWsRequest = new RateRequest();
 	    fedexWsRequest.setClientDetail(createClientDetail());
 	    fedexWsRequest.setWebAuthenticationDetail(createWebAuthenticationDetail());
 	    fedexWsRequest.setReturnTransitAndCommit(true);
 	    //
 	    TransactionDetail transactionDetail = new TransactionDetail();
 	    transactionDetail.setCustomerTransactionId("java sample - Rate Request"); // The client will get the same value back in the response
 	    fedexWsRequest.setTransactionDetail(transactionDetail);
 
         //
         VersionId versionId = new VersionId("crs", 13, 0, 0);
         fedexWsRequest.setVersion(versionId);
         
         //
         RequestedShipment requestedShipment = new RequestedShipment();
         
         requestedShipment.setShipTimestamp(Calendar.getInstance());
         requestedShipment.setDropoffType(DropoffType.REGULAR_PICKUP);
         if (! getAllRatesFlag) {
        	requestedShipment.setServiceType(getServiceType(request));
         	requestedShipment.setPackagingType(PackagingType.YOUR_PACKAGING);
         }
         
         
         Party shipper = new Party();
 	    Address shipperAddress = new Address(); // Origin information
 	    //shipperAddress.setStreetLines(new String[] {"6233 Winchester Rd"});
 	    //shipperAddress.setCity("Memphis");
 	    //shipperAddress.setStateOrProvinceCode("TN");
 	    shipperAddress.setPostalCode("38115");
 	    shipperAddress.setCountryCode("US");
         shipper.setAddress(shipperAddress);
         requestedShipment.setShipper(shipper);
 
 	    //
         Party recipient = new Party();
 	    Address recipientAddress = new Address(); // Destination information
 	    //recipientAddress.setStreetLines(new String[] {"8001 Pauline Vanier"});
 	    //recipientAddress.setCity("COLLIERVILLE");
 	    //recipientAddress.setStateOrProvinceCode("TN");
 	    //recipientAddress.setPostalCode("38017");
 	    //recipientAddress.setCountryCode("US");
 	    recipientAddress.setPostalCode("6225");
 	    recipientAddress.setCountryCode("SI");
 	    recipient.setAddress(recipientAddress);
 	    requestedShipment.setRecipient(recipient);
 
 	    //
 	    Payment shippingChargesPayment = new Payment();
 	    shippingChargesPayment.setPaymentType(PaymentType.SENDER);
 	    requestedShipment.setShippingChargesPayment(shippingChargesPayment);
 
 	    RequestedPackageLineItem rp = new RequestedPackageLineItem();
 	    rp.setGroupPackageCount(new NonNegativeInteger("1"));
 	    rp.setWeight(new Weight(WeightUnits.LB, new BigDecimal(15.0)));
 	    //
 	    rp.setInsuredValue(new Money("USD", new BigDecimal("100.00")));
 	    //
 	    rp.setDimensions(new Dimensions(new NonNegativeInteger("1"), new NonNegativeInteger("1"), new NonNegativeInteger("1"), LinearUnits.IN));
 	    PackageSpecialServicesRequested pssr = new PackageSpecialServicesRequested();
 	    rp.setSpecialServicesRequested(pssr);
 	    requestedShipment.setRequestedPackageLineItems(new RequestedPackageLineItem[] {rp});
 
 	    
 	    requestedShipment.setPackageCount(new NonNegativeInteger("1"));
 	    requestedShipment.setRateRequestTypes(new RateRequestType[] {RateRequestType.ACCOUNT});
 	    fedexWsRequest.setRequestedShipment(requestedShipment);
 	    
 	    //
 		try {
 			// Initialize the service
 			RateServiceLocator service;
 			RatePortType port;
 			//
 			service = new RateServiceLocator();
 			updateEndPoint(service);
 			port = service.getRateServicePort();
 			// This is the call to the web service passing in a RateRequest and returning a RateReply
 			RateReply reply = port.getRates(fedexWsRequest); // Service call
 			if (isResponseOk(reply.getHighestSeverity())) {
 				return getMinimalDeliveryCost(reply);
 			}
 		}
 		catch (Exception e) {
 		    e.printStackTrace();
 		}
 		return null;
 	}
 	
 	private static ClientDetail createClientDetail() {
         ClientDetail clientDetail = new ClientDetail();
         String accountNumber = System.getProperty("accountNumber");
         String meterNumber = System.getProperty("meterNumber");
         
         //
         // See if the accountNumber and meterNumber properties are set,
         // if set use those values, otherwise default them to "XXX"
         //
         if (accountNumber == null) {
         	accountNumber = "510087224"; // Replace "XXX" with clients account number
         }
         if (meterNumber == null) {
         	meterNumber = "118574485"; // Replace "XXX" with clients meter number
         }
         clientDetail.setAccountNumber(accountNumber);
         clientDetail.setMeterNumber(meterNumber);
         return clientDetail;
 	}
 	
 	private static WebAuthenticationDetail createWebAuthenticationDetail() {
         WebAuthenticationCredential wac = new WebAuthenticationCredential();
         String key = System.getProperty("key");
         String password = System.getProperty("password");
         
         //
         // See if the key and password properties are set,
         // if set use those values, otherwise default them to "XXX"
         //
         if (key == null) {
         	key = "UCRMCXUzM8Rsnrpk";
         }
         if (password == null) {
         	password = "bVaJmWFeBSpnXmFECOw444VDf";
         }
         wac.setKey(key);
         wac.setPassword(password);
 		return new WebAuthenticationDetail(wac);
 	}
 	
 	private static void updateEndPoint(RateServiceLocator serviceLocator) {
 		String endPoint = System.getProperty("endPoint");
 		if (endPoint != null) {
 			serviceLocator.setRateServicePortEndpointAddress(endPoint);
 		}
 	}
 	
 	private static boolean isResponseOk(NotificationSeverityType notificationSeverityType) {
 		if (notificationSeverityType == null) {
 			return false;
 		}
 		if (notificationSeverityType.equals(NotificationSeverityType.WARNING) ||
 			notificationSeverityType.equals(NotificationSeverityType.NOTE)    ||
 			notificationSeverityType.equals(NotificationSeverityType.SUCCESS)) {
 			return true;
 		}
  		return false;
 	}
 	
 	private Double getMinimalDeliveryCost(RateReply reply) {
 		Double minimalDeliveryCost = null;
 		RateReplyDetail[] rrds = reply.getRateReplyDetails();
 		for (int i = 0; i < rrds.length; i++) {
 			RateReplyDetail rrd = rrds[i];
 //			print("Packaging type", rrd.getPackagingType());
 //			int month = rrd.getDeliveryTimestamp().get(Calendar.MONTH) + 1;
 //			int date = rrd.getDeliveryTimestamp().get(Calendar.DAY_OF_MONTH);
 //			int year = rrd.getDeliveryTimestamp().get(Calendar.YEAR);
 //			String delDate = new String(month + "/" + date + "/" + year);
 //			print("Delivery date", delDate);
 //			print("Calendar DOW",
 //					rrd.getDeliveryTimestamp().get(Calendar.DAY_OF_WEEK));
 
 			RatedShipmentDetail[] rsds = rrd.getRatedShipmentDetails();
 			for (int j = 0; j < rsds.length; j++) {
 				//print("RatedShipmentDetail " + j, "");
 				RatedShipmentDetail rsd = rsds[j];
 				ShipmentRateDetail srd = rsd.getShipmentRateDetail();
 				Double totalNetCharge = srd.getTotalNetCharge().getAmount().doubleValue();
 				if (minimalDeliveryCost == null || totalNetCharge < minimalDeliveryCost) {
 					minimalDeliveryCost = totalNetCharge;
 				}
 				//print("  Rate type", srd.getRateType());
 //				printWeight("  Total Billing weight",
 //						srd.getTotalBillingWeight());
 //				printMoney("  Total surcharges", srd.getTotalSurcharges());
 //				printMoney("  Total net charge", srd.getTotalNetCharge());
 
 //				RatedPackageDetail[] rpds = rsd.getRatedPackages();
 //				if (rpds != null && rpds.length > 0) {
 //					print("  RatedPackageDetails", "");
 //					for (int k = 0; k < rpds.length; k++) {
 //						print("  RatedPackageDetail " + i, "");
 //						RatedPackageDetail rpd = rpds[k];
 //						PackageRateDetail prd = rpd.getPackageRateDetail();
 //						if (prd != null) {
 //							printWeight("    Billing weight",
 //									prd.getBillingWeight());
 //							printMoney("    Base charge", prd.getBaseCharge());
 //							Surcharge[] surcharges = prd.getSurcharges();
 //							if (surcharges != null && surcharges.length > 0) {
 //								for (int m = 0; m < surcharges.length; m++) {
 //									Surcharge surcharge = surcharges[m];
 //									printMoney(
 //											"    " + surcharge.getDescription()
 //													+ " surcharge",
 //											surcharge.getAmount());
 //								}
 //							}
 //						}
 //					}
 //				}
 			}
 		}
 		return minimalDeliveryCost;
 	}
	
	private ServiceType getServiceType(DeliveryCostRequest request) {
		if (request.getShipper().getCountry().equals("US") && 
				request.getRecipient().getCountry().equals("US")) {
			return ServiceType.PRIORITY_OVERNIGHT;
		}
		else {
			return ServiceType.INTERNATIONAL_ECONOMY;
		}
	}
 }
