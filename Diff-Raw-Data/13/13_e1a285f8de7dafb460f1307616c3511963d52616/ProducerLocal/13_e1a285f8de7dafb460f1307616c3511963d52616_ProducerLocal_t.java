 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import models.sc_offer;
 import models.sc_offers_subscriptions;
 import play.Logger;
 import play.db.jpa.Blob;
 import play.db.jpa.JPA;
 import play.libs.MimeTypes;
 import play.mvc.Controller;
 import play.mvc.Finally;
 
 public class ProducerLocal extends Controller {
 
 	public static void subscribedOffers() {
 
 		Logger.info(" -----INSIDE PRODUCER SUBSCRIBEDOFFERS()------");
 		String user = session.get("username");
 		if (user != null) {
 			List<sc_offers_subscriptions> resultSet = null;
 			try {
 				resultSet = ProducerDAO
 						.getSubscribedOffersGroupByServiceLevels();
 				Logger.info(" -----EXITING PRODUCER SUBSCRIBEDOFFERS()------");
 				render(resultSet, user);
 			} catch (Exception e) {
 				Logger.warn(
 						e,
 						"Exception occured retrieving offers . No of offers retrieved : ",
 						resultSet.size());
 			}
 
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 
 	}
 
 	public static void displayOffer(final String service_level) {
 		Logger.info(" -----INSIDE PRODUCER DISPLAYOFFER()------");
 		Logger.info(" Service_level " + service_level);
 		String user = session.get("username");
 		if (user != null) {
 			List<sc_offers_subscriptions> resultSet = ProducerDAO
 					.getSubscribedOffersGroupByServiceLevels();
 			List<sc_offers_subscriptions> resultSet1 = ProducerDAO
 					.getSubscribedOffers(service_level);
 			Logger.info(" -----INSIDE PRODUCER DISPLAYOFFER()------");
 			render("/ProducerLocal/subscribedOffers.html", resultSet,
 					resultSet1, user);
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 	/* disable the selected offer */
 	public static void disableOffer(final Long scOfferId) {
 		Logger.info(" -----INSIDE PRODUCER DISABLEOFFER()------");
 		Logger.info(" Offer Id to delete : " + scOfferId);
 
 		sc_offers_subscriptions offerSub = JPA.em().find(
 				sc_offers_subscriptions.class, scOfferId);
 		sc_offer sc_offer = offerSub.getSc_offer();
 		Integer sc_offer_id = sc_offer.getSc_offer_id();
 
 		Query query = JPA.em().createNativeQuery(
 				"delete from mkt_enterprise_view where sc_offer_id = ?1");
 		query.setParameter(1, sc_offer_id);
 		query.executeUpdate();
 		Logger.info(" Offer deleted ");
 		offerSub.delete();
 		Logger.info(" -----EXITING PRODUCER DISABLEOFFER()------");
		//Producer.poe();
 		render("/Producer/saveConfigure.html");
 	}
 
 	/**
 	 * configure offer existing in portal database
 	 * 
 	 * @param sc_offer_id
 	 */
 	public static void configureExistingOffer(final Integer sc_offer_id) {
 		Logger.info("------ INSIDE CONFIGURE EXISTING OFFER -------");
 		String user = session.get("username");
 		if (user != null) {
 
 			if (sc_offer_id == null) {
 				flash.error("Oops!! Unable to get Offer . Please try again ");
 				Producer.poe();
 
 			} else {
 				Logger.info(" sc_offer_Id to configure existing offer : "
 						+ sc_offer_id);
 				try {
 					List<sc_offer> scOffer = ProducerDAO
 							.getOfferDetails(sc_offer_id);
 
 					/*
 					 * Query query1 =JPA.em().createNativeQuery(
 					 * "select * from sc_offers_subscriptions where sc_offer_sc_offer_id = ?1"
 					 * ,sc_offers_subscriptions.class);
 					 * query1.setParameter(1,sc_offer_id);
 					 * List<sc_offers_subscriptions> subscribedOffers =
 					 * query1.getResultList();
 					 */
 
 					List<sc_offers_subscriptions> subscribedOffers = ProducerDAO
 							.getSubscribedOfferGivenOfferId(sc_offer_id);
 					Date expireDate = null;
 					Date startDate = null;
 					String lease_period = null;
 					for (sc_offers_subscriptions subscribedOffer : subscribedOffers) {
 						expireDate = subscribedOffer.getExpiration_date();
 						startDate = subscribedOffer.getStart_date();
 						lease_period = subscribedOffer.getLease_period();
 						Logger.info(" expire date for selected  offer "
 								+ expireDate);
 						Logger.info(" start date for selected  offer "
 								+ startDate);
 						Logger.info(" Lease Period for selected  offer "
 								+ lease_period);
 
 					}
 					Logger.info("------ EXITING CONFIGURE EXISTING OFFER -------");
 					render(scOffer, user, expireDate, startDate, lease_period);
 				}
 
 				catch (Exception e) {
 					Logger.warn(e,
 							"EXCEPTION OCCURED IN configureExistingOffer ",
 							sc_offer_id);
 				}
 			}
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 	/**
 	 * Save the configuration for selected offer
 	 * 
 	 * @param sc_offers
 	 * @param icon
 	 * @param image
 	 */
 	public static void saveConfigure(final sc_offer sc_offers, final File icon,
 			final File image, final sc_offers_subscriptions subscription) {
 		String user = session.get("username");
 		if (user != null) {
 			Logger.info("-----------INSIDE SAVECONFIGURE()------------");
 			Logger.info("------ saveConfigure() id------- "
 					+ sc_offers.getSc_offer_id());
 			Logger.info("------ saveConfigure() Offer name ------- "
 					+ sc_offers.getSc_offer_name());
 			Logger.info("------ saveConfigure() short description------- "
 					+ sc_offers.getShort_description());
 			Logger.info("------ saveConfigure() description------- "
 					+ sc_offers.getDescription());
 
 			Logger.info("------ saveConfigure() icon ----" + icon);
 			Logger.info("------ saveConfigure() image ----" + image);
 			try {
 				sc_offer scOffer = sc_offer
 						.findById(sc_offers.getSc_offer_id());
 
 				scOffer.setSc_offer_id(sc_offers.getSc_offer_id());
 				scOffer.setSc_offer_name(sc_offers.getSc_offer_name());
 				if (icon != null) {
 					scOffer.setIcon_name(sc_offers.getIcon_name());
 					scOffer.setIcon_name(icon.getName());
 					scOffer.setIcon(new Blob());
 					scOffer.getIcon().set(new FileInputStream(icon),
 							MimeTypes.getContentType(icon.getName()));
 				}
 				if (image != null) {
 					scOffer.setImage(new Blob());
 					scOffer.getImage().set(new FileInputStream(image),
 							MimeTypes.getContentType(image.getName()));
 				}
 				scOffer.setShort_description(sc_offers.getShort_description());
 				scOffer.setDescription(sc_offers.getDescription());
 				scOffer.save();
 
 				List<sc_offers_subscriptions> subscribedOffers = ProducerDAO
 						.getSubscribedOfferGivenOfferId(sc_offers
 								.getSc_offer_id());
 				for (sc_offers_subscriptions subOffer : subscribedOffers) {
 					/*
 					 * subOffer.setExpiration_date(subscription.getExpiration_date
 					 * ());
 					 * subOffer.setStart_date(subscription.getStart_date());
 					 */
 					subOffer.setLease_period(subscription.getLease_period());
 					subOffer.save();
 				}
 
 				Logger.info("-----------EXITING SAVECONFIGURE()------------");
 				render(user);
 			} catch (FileNotFoundException e) {
 				Logger.info("Icon /Image File not found");
 			}
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 	public static void admin() {
 
 		String user = session.get("username");
 		if (user != null) {
 			render(user);
 		} else {
 
 			flash.error("You are not connected.Please Login");
 			Login.login_page();
 		}
 	}
 
 	@Finally
 	static void clearFlash() {
 		flash.clear();
 	}
 
 }
