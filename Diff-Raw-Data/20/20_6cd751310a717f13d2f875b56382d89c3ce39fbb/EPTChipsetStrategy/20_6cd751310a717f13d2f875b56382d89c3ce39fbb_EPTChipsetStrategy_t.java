 package fr.ensicaen.simulator_ep.ep.strategies.ept;
 
 import java.text.SimpleDateFormat;
 
 import org.jpos.iso.ISOException;
 import org.jpos.iso.ISOMsg;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.ensicaen.simulator.model.component.Component;
 import fr.ensicaen.simulator.model.component.ComponentIO;
 import fr.ensicaen.simulator.model.component.IOutput;
 import fr.ensicaen.simulator.model.mediator.Mediator;
 import fr.ensicaen.simulator.model.response.DataResponse;
 import fr.ensicaen.simulator.model.response.IResponse;
 import fr.ensicaen.simulator.model.response.VoidResponse;
 import fr.ensicaen.simulator.model.strategies.IStrategy;
 import fr.ensicaen.simulator.simulator.Context;
 import fr.ensicaen.simulator.simulator.exception.ContextException;
 import fr.ensicaen.simulator_ep.utils.ISO7816Exception;
 import fr.ensicaen.simulator_ep.utils.ISO7816Tools;
 import fr.ensicaen.simulator_ep.utils.ISO7816Tools.MessageType;
 import fr.ensicaen.simulator_ep.utils.ISO8583Exception;
 import fr.ensicaen.simulator_ep.utils.ISO8583Tools;
 
 public class EPTChipsetStrategy implements IStrategy<ComponentIO> {
 
 	private static Logger log = LoggerFactory.getLogger(EPTChipsetStrategy.class);
 
 	public EPTChipsetStrategy() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void init(IOutput _this, Context ctx) {
 		ctx.subscribeEvent(_this, "SMART_CARD_INSERTED");
 	}
 
 	@Override
 	public void processEvent(ComponentIO _this, String event) {
 		switch (event) {
 			case "SMART_CARD_INSERTED":
 				// setting secure channel with the card
 				// prepare initialization message
 				ISOMsg msg = null;
 
 				// get the card linked
 				try {
 					msg = prepareSecureChannelRQ(_this);
 					Mediator m = Context.getInstance().getFirstMediator(_this, "Card");
 					DataResponse res = (DataResponse) m.send(_this, new String(msg.pack()));
 					ISOMsg sdata = ISO7816Tools.read(res.getData());
 
 					// card holder authentication (amount + PIN)
 					msg = prepareCardHolderAuthRQ(_this, sdata);
 					res = (DataResponse) m.send(_this, new String(msg.pack()));
 					sdata = ISO7816Tools.read(res.getData());
 
 					// auth request to bank (TPE -> Bank and bank -> TPE)
 					Mediator mediateurFrontOffice = Context.getInstance().getFirstMediator(_this, "FO");
 					msg = generateAuthorizationRequest(_this, sdata);
 					res = (DataResponse) mediateurFrontOffice.send(_this, new String(msg.pack()));
 					sdata = ISO8583Tools.read(res.getData());
 
 					// ARPC
 					msg = prepareARPC(_this, sdata);
					res = (DataResponse) m.send(_this, new String(msg.pack()));
 					sdata = ISO7816Tools.read(res.getData());
 
 					// final agreement
 					manageFinalAgrement(_this, sdata);
 
 				}
 				catch (ContextException e) {
 					log.error("Context error", e);
 					return; // ABORT (to think)
 				}
 				catch (ISO7816Exception e) {
 					log.error("Get unreadable message from card", e);
 					return; // ABORT
 				}
 				catch (ISOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				catch (ISO8583Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				break;
 
 			default:
 				log.info("Event " + event + " not implemented.");
 		}
 	}
 
 	@Override
 	public IResponse processMessage(ComponentIO _this, Mediator c, String data) {
 
 		return VoidResponse.build();
 	}
 
 	private ISOMsg prepareSecureChannelRQ(Component _this) throws ISOException {
 		ISOMsg ret = ISO7816Tools.create();
 		ret.setMTI(ISO7816Tools.convertType2CodeMsg(MessageType.SECURE_CHANNEL_RQ));
 		ret.set(ISO7816Tools.FIELD_POSID, _this.getProperty("pos_id"));
 		ret.set(ISO7816Tools.FIELD_PROTOCOLLIST, _this.getProperty("protocol_list"));
 		ret.set(ISO7816Tools.FIELD_PROTOCOLPREFERRED, _this.getProperty("protocol_prefered"));
 		// ret.set(ISO7816Tools.FIELD_STAN, _this.getProperty("stan"));
 		// ret.set(ISO7816Tools.FIELD_RRN, generateTransactid(_this));
 		ret.set(ISO7816Tools.FIELD_DATETIME, ISO7816Tools.writeDATETIME(Context.getInstance().getTime()));
 		return ret;
 	}
 
 	/**
 	 * Preparation de la transcation montant + PIN
 	 * 
 	 * @param _this
 	 * @return
 	 * @throws ISOException
 	 */
 	private ISOMsg prepareCardHolderAuthRQ(Component _this, ISOMsg data) throws ISOException {
 		String pan = data.getString(ISO7816Tools.FIELD_STAN);
 		ISOMsg ret = ISO7816Tools.create();
 		ret.setMTI(ISO7816Tools.convertType2CodeMsg(MessageType.CARDHOLDER_AUTH_RQ));
 		ret.set(ISO7816Tools.FIELD_POSID, _this.getProperty("pos_id"));
 		ret.set(ISO7816Tools.FIELD_OPCODE, "00");
 		ret.set(ISO7816Tools.FIELD_AMOUNT, ISO7816Tools.writeAMOUNT(80));
 		ret.set(ISO7816Tools.FIELD_PINDATA, _this.getProperty("pin_enter"));
 		// ret.set(ISO7816Tools.FIELD_STAN, generateNextSTAN(_this, pan));
 		// ret.set(ISO7816Tools.FIELD_RRN, generateTransactid(_this));
 		ret.set(ISO7816Tools.FIELD_DATETIME, ISO7816Tools.writeDATETIME(Context.getInstance().getTime()));
 		return ret;
 	}
 
 	/**
 	 * Preparation du msg d'autorisation pour le FO
 	 * 
 	 * @param _this
 	 * @param parsedData
 	 * @return
 	 * @throws ISOException
 	 */
 	public ISOMsg generateAuthorizationRequest(ComponentIO _this, ISOMsg parsedData) throws ISOException {
 		String pan = parsedData.getString(ISO7816Tools.FIELD_PAN);
 		String amount = parsedData.getString(ISO7816Tools.FIELD_AMOUNT);
 		String stan = parsedData.getString(ISO7816Tools.FIELD_STAN);
 		String apcode = parsedData.getString(ISO7816Tools.FIELD_APPROVALCODE);
 
 		ISOMsg authorizationRequest = ISO8583Tools.create();
 
 		authorizationRequest.setMTI("0100");
 		authorizationRequest.set(2, pan); // PAN
 		authorizationRequest.set(3, "000101"); // Type of Auth + accounts
 		authorizationRequest.set(4, amount);
 		authorizationRequest.set(7, ISO7816Tools.writeDATETIME(Context.getInstance().getTime()));
 		// authorizationRequest.set(11, generateNextSTAN(_this, stan));
 		authorizationRequest.set(38, apcode);
 		authorizationRequest.set(42, _this.getProperty("acceptor_id")); // Acceptor's
 																		// ID
 		authorizationRequest.set(123, _this.getProperty("posdatacode"));
 
 		return authorizationRequest;
 	}
 
 	/**
 	 * Preparation de l'arpc depuis les info donnees par le FO de la banque
 	 * 
 	 * @param _this
 	 * @param data
 	 *            msg in format 8583
 	 * @return format 7816
 	 * @throws ISOException
 	 */
 	private ISOMsg prepareARPC(Component _this, ISOMsg data) throws ISOException {
		String amount = data.getString(4);
		String apcode = data.getString(38);
		String rescode = data.getString(39);
		String pan = data.getString(2);
 
 		ISOMsg ret = ISO7816Tools.create();
 		ret.setMTI(ISO7816Tools.convertType2CodeMsg(MessageType.AUTHORIZATION_RP_CRYPTO));
 		ret.set(ISO7816Tools.FIELD_POSID, _this.getProperty("pos_id"));
 		ret.set(ISO7816Tools.FIELD_OPCODE, "00");
 		ret.set(ISO7816Tools.FIELD_AMOUNT, amount);
 		ret.set(ISO7816Tools.FIELD_APPROVALCODE, apcode);
 		ret.set(ISO7816Tools.FIELD_RESPONSECODE, rescode);
 		ret.set(ISO7816Tools.FIELD_PAN, pan);
 		// ret.set(ISO7816Tools.FIELD_STAN, generateNextSTAN(_this, stan));
 		// ret.set(ISO7816Tools.FIELD_RRN, generateTransactid(_this));
 		ret.set(ISO7816Tools.FIELD_DATETIME, ISO7816Tools.writeDATETIME(Context.getInstance().getTime()));
 
 		return ret;
 	}
 
 	/**
 	 * Permet de gerer la reponse de la carte lors de la fin d'une transaction :
 	 * le final agreement Cette reponse de la carte indique au TPE de stocker
 	 * les donnees de la transaction pour la telecollecte
 	 * 
 	 * @param _this
 	 * @param data
 	 */
 	private void manageFinalAgrement(Component _this, ISOMsg data) {
 		// stockage de la transaction
 		String datetime = data.getString(ISO7816Tools.FIELD_DATETIME);
 		// String stan = data.getString(ISO7816Tools.FIELD_STAN);
 		_this.getProperties().put(datetime /* + stan */, data.toString());
 	}
 
 	/**
 	 * Permet de donner le numero de stan suivant. Le STAN ce code sur 6 digits
 	 * !! met a jour les proprietes du chipset. !!
 	 * 
 	 * @param stan
 	 *            courant
 	 * @return
 	 */
 	public static String generateNextSTAN(Component _this, String curStan) {
 		String ret = null;
 		int val_stan = Integer.parseInt(curStan); // NumberFormatException non
 													// gérée !!!
 		if (val_stan > 999999) {
 			ret = "000001";
 		}
 		else {
 			ret = String.format("%06d", ++val_stan);
 		}
 		_this.getProperties().put("stan", ret);
 		return ret;
 	}
 
 	/**
 	 * Permet de generer un numero de transaction.
 	 * 
 	 * @param stan
 	 * @return
 	 */
 	public static String generateTransactid(Component _this) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yDDDhh");
 		return sdf.format(Context.getInstance().getTime()).substring(3) + _this.getProperty("stan");
 	}
 
 	@Override
 	public String toString() {
 		return "EPT/Chipset";
 	}
 }
