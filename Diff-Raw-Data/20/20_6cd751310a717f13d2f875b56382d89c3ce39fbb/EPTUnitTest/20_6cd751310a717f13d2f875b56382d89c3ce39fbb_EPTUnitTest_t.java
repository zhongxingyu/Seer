 package ep.strategies.ept;
 
 import java.util.Date;
 
 import org.jpos.iso.ISOException;
 import org.jpos.iso.ISOMsg;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ep.strategies.card.CardTest;
 import fr.ensicaen.simulator.model.component.ComponentIO;
 import fr.ensicaen.simulator.model.component.IOutput;
 import fr.ensicaen.simulator.model.factory.MediatorFactory;
 import fr.ensicaen.simulator.model.factory.MediatorFactory.EMediator;
 import fr.ensicaen.simulator.model.mediator.Mediator;
 import fr.ensicaen.simulator.model.response.DataResponse;
 import fr.ensicaen.simulator.model.response.IResponse;
 import fr.ensicaen.simulator.model.response.VoidResponse;
 import fr.ensicaen.simulator.model.strategies.IStrategy;
 import fr.ensicaen.simulator.simulator.Context;
 import fr.ensicaen.simulator.simulator.SimulatorFactory;
 import fr.ensicaen.simulator.simulator.exception.SimulatorException;
 import fr.ensicaen.simulator.tools.TestPass;
 import fr.ensicaen.simulator_ep.ep.strategies.ept.EPTChipsetStrategy;
 import fr.ensicaen.simulator_ep.ep.strategies.ept.EPTSmartCardReaderStrategy;
 import fr.ensicaen.simulator_ep.ep.strategies.ept.EPTStrategy;
 import fr.ensicaen.simulator_ep.utils.ISO7816Tools;
 import fr.ensicaen.simulator_ep.utils.ISO7816Tools.MessageType;
 import fr.ensicaen.simulator_ep.utils.ISO8583Exception;
 import fr.ensicaen.simulator_ep.utils.ISO8583Tools;
 
 /**
  * Electronic Payment Terminal (EPT)
  * 
  * Terminal de Paiement Electronique |- Chipset |- Lecteur carte à piste |-
  * Lecteur carte à puce |- Lecteur carte NFC [*] |- Emplacement SAM |-
  * Emplacement SIM [*] |- Imprimante |- Pinpad sécurisé |- Interface réseau
  * acquéreur (Modem, Ethernet, GPRS, ...) |- Interface caisse (Serial, Ethernet,
  * ...) [*]
  * 
  * [*] : facultatif
  * 
  * Source : http://fr.wikipedia.org/wiki/Terminal_de_paiement_%C3%A9lectronique
  * 
  * @author Flo
  */
 public class EPTUnitTest {
 	private static Logger log = LoggerFactory.getLogger(CardTest.class);
 
 	private static ISOMsg card_sc = ISO7816Tools.create();
 	private static ISOMsg card_ch = ISO7816Tools.create();
 	private static ISOMsg card_finalagree = ISO7816Tools.create();
 	private static ISOMsg fo_authrp = ISO8583Tools.create();
 
 	private static ComponentIO ept;
 	private static ComponentIO smartCardReader;
 	private static ComponentIO chipset;
 	private static ComponentIO printer;
 	private static ComponentIO securePinPad;
 	private static ComponentIO networkInterface;
 	private static ComponentIO frontOffice;
 	private static ComponentIO fakeSmartCard;
 
 	@Before
 	public void init() throws Exception {
 		TestPass.init();
 
 		MediatorFactory factory = MediatorFactory.getInstance();
 
 		Context.getInstance().autoRegistrationMode();
 
 		frontOffice = new ComponentIO("Front Office");
 
 		fakeSmartCard = new ComponentIO("Card");
 
 		/* ******** Définition du TPE ******** BEGIN */
 		ept = new ComponentIO("Electronic Payment Terminal");
 		ept.setStrategy(new EPTStrategy());
 
 		/* Enfant : lecteur de carte */
 		smartCardReader = new ComponentIO("Smart Card Reader");
 		smartCardReader.setStrategy(new EPTSmartCardReaderStrategy());
 		ept.addChild(smartCardReader);
 
 		/* Enfant : chipset */
 		chipset = new ComponentIO("Chipset");
 		chipset.setStrategy(new EPTChipsetStrategy());
 		chipset.getProperties().put("pos_id", "0000623598");
 		chipset.getProperties().put("acceptor_id", "0000623598");
 		chipset.getProperties().put("posdatacode", "510101511326105");
 		// chipset.getProperties().put("stan", "000001");
 		chipset.getProperties().put("protocol_list", "ISO7816 ISO8583 CB2A-T");
 		chipset.getProperties().put("protocol_prefered", "ISO7816");
 		chipset.getProperties().put("pin_enter", "1234");
 		ept.addChild(chipset);
 
 		/* Enfant : imprimante */
 		printer = new ComponentIO("Printer");
 		ept.addChild(printer);
 
 		/* Enfant : Pin pad */
 		securePinPad = new ComponentIO("Secure pin pad");
 		ept.addChild(securePinPad);
 
 		/* ******** Définition du TPE ******** END */
 
 		// static mediators
 		factory.getMediator(ept, frontOffice, EMediator.HALFDUPLEX);
 		factory.getMediator(ept, fakeSmartCard, EMediator.HALFDUPLEX);
 
 		generateMsg();
 	}
 
 	public void generateMsg() throws ISOException {
 		card_sc.setMTI(ISO7816Tools.convertType2CodeMsg(MessageType.SECURE_CHANNEL_RP));
 		card_sc.set(ISO7816Tools.FIELD_POSID, "0000623598");
 		card_sc.set(ISO7816Tools.FIELD_PROTOCOL, "ISO7816");
 		card_sc.set(ISO7816Tools.FIELD_PAN, "4976710025642130");
 		// card_sc.set(ISO7816Tools.FIELD_RRN, "320012000001");
 		// card_sc.set(ISO7816Tools.FIELD_STAN, "000002");
 		card_sc.set(ISO7816Tools.FIELD_DATETIME, "1008170100");
 
 		card_ch.setMTI(ISO7816Tools.convertType2CodeMsg(MessageType.CARDHOLDER_AUTH_RP));
 		card_ch.set(ISO7816Tools.FIELD_POSID, "0000623598");
 		card_ch.set(ISO7816Tools.FIELD_OPCODE, "00");
 		card_ch.set(ISO7816Tools.FIELD_AMOUNT, "0000008000");
 		card_ch.set(ISO7816Tools.FIELD_PINVERIFICATION, "1");
 		card_ch.set(ISO7816Tools.FIELD_CARDAGREEMENT, "1");
 		card_sc.set(ISO7816Tools.FIELD_PAN, "4976710025642130");
 		// card_ch.set(ISO7816Tools.FIELD_RRN, "320012000001");
 		// card_ch.set(ISO7816Tools.FIELD_STAN, "000004");
 		card_ch.set(ISO7816Tools.FIELD_DATETIME, "1008170100");
 
 		card_finalagree.setMTI(ISO7816Tools.convertType2CodeMsg(MessageType.TRANSCATION_VAL_NOTIF));
 		card_finalagree.set(ISO7816Tools.FIELD_POSID, "0000623598");
 		card_finalagree.set(ISO7816Tools.FIELD_OPCODE, "00");
 		card_finalagree.set(ISO7816Tools.FIELD_AMOUNT, "0000008000");
 		card_finalagree.set(ISO7816Tools.FIELD_APPROVALCODE, "07B56=");
 		card_finalagree.set(ISO7816Tools.FIELD_RESPONSECODE, "00");
 		card_finalagree.set(ISO7816Tools.FIELD_PAN, "4976710025642130");
 		// card_finalagree.set(ISO7816Tools.FIELD_RRN, "320012000001");
 		// card_finalagree.set(ISO7816Tools.FIELD_STAN, "000006");
 		card_finalagree.set(ISO7816Tools.FIELD_DATETIME, "1008170100");
 
 		fo_authrp.setMTI("0110");
 		fo_authrp.set(2, "4976710025642130"); // PAN
 		fo_authrp.set(3, "000101"); // Type of Auth + accounts
 		fo_authrp.set(4, "0000008000"); // 80€
 		fo_authrp.set(7, "1008170100"); // date : MMDDhhmmss
 		// fo_authrp.set(11, "000004"); // System Trace Audit Number
 		fo_authrp.set(38, "07B56="); // Approval Code
 		fo_authrp.set(39, "00"); // Response Code
 		fo_authrp.set(42, "0000623598"); // Acceptor's ID
 		fo_authrp.set(123, "510101511326105"); // POS Data Code
 	}
 
 	@After
 	public void clean() throws Exception {
 		Context.getInstance().reset();
 	}
 
 	@Test
 	public void testPipedMediator() throws SimulatorException {
 		fakeSmartCard.setStrategy(new IStrategy<ComponentIO>() {
 
 			@Override
 			public void init(IOutput _this, Context ctx) {
 			}
 
 			@Override
 			public void processEvent(ComponentIO _this, String event) {
 			}
 
 			@Override
 			public IResponse processMessage(ComponentIO _this, Mediator mediator, String data) {
 				TestPass.passed();
 				return DataResponse.build(mediator, "TEST ANSWER CARD");
 			}
 
 		});
 
 		// add start point for the simulator
 		Context.getInstance().addStartPoint(new Date(), "SMART_CARD_INSERTED");
 
 		// execute simulation.
 		SimulatorFactory.getSimulator().start();
 
 		TestPass.assertTest();
 	}
 
 	@Test
 	public void card_tpetest() {
 		log.debug("----TEST FO<->EPT<->CARD----");
 		fakeSmartCard.setStrategy(new IStrategy<ComponentIO>() {
 			private int msg = -1;
 
 			@Override
 			public void processEvent(ComponentIO _this, String event) {
 
 			}
 
 			@Override
 			public IResponse processMessage(ComponentIO _this, Mediator mediator, String data) {
 				try {
 					ISOMsg rpdata = ISO7816Tools.read(data);
 					msg++;
 					switch (msg) {
 						case 0:
 							log.debug("card receiv secure channel");
 
 							// verification du MTI
 							Assert.assertEquals(ISO7816Tools.convertType2CodeMsg(MessageType.SECURE_CHANNEL_RQ),
 									rpdata.getMTI());
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_PROTOCOLLIST),
 									"ISO7816 ISO8583 CB2A-T");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_POSID), "0000623598");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_PROTOCOLPREFERRED), "ISO7816");
 							// Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_STAN),
 							// "000001");
 							return DataResponse.build(mediator, new String(card_sc.pack()));
 						case 1:
 							log.debug("card receiv card holder");
 							// verification du MTI
 							Assert.assertEquals(ISO7816Tools.convertType2CodeMsg(MessageType.CARDHOLDER_AUTH_RQ),
 									rpdata.getMTI());
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_POSID), "0000623598");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_OPCODE), "00");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_AMOUNT), "0000008000");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_PINDATA), "1234");
 							// Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_STAN),
 							// "000003");
 							return DataResponse.build(mediator, new String(card_ch.pack()));
 						case 2:
 							log.debug("card receiv final agreement");
 							// verification du MTI
 							Assert.assertEquals(ISO7816Tools.convertType2CodeMsg(MessageType.AUTHORIZATION_RP_CRYPTO),
 									rpdata.getMTI());
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_POSID), "0000623598");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_OPCODE), "00");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_AMOUNT), "0000008000");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_APPROVALCODE), "07B56=");
 							Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_RESPONSECODE), "00");
 							// Assert.assertEquals(rpdata.getString(ISO7816Tools.FIELD_STAN),
 							// "000005");
 							return DataResponse.build(mediator, new String(card_finalagree.pack()));
 						default:
 							Assert.assertTrue(false);
 							break;
 					}
 				}
 				catch (Exception e) {
 					Assert.assertFalse(true);
 				}
 
 				return VoidResponse.build();
 			}
 
 			@Override
 			public void init(IOutput _this, Context ctx) {
 				ctx.subscribeEvent(_this, "SMART_CARD_INSERTED");
 			}
 
 		});
 
 		frontOffice.setStrategy(new IStrategy<ComponentIO>() {
 
 			@Override
 			public void init(IOutput _this, Context ctx) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void processEvent(ComponentIO _this, String event) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public IResponse processMessage(ComponentIO _this, Mediator mediator, String data) {
 				Assert.assertTrue(true);
 				try {
 					ISOMsg rpdata = null;
 					log.debug("fo receiv auth");
 					rpdata = ISO8583Tools.read(data);
 					// verification du MTI
 					Assert.assertEquals("0100", rpdata.getMTI());
 					Assert.assertEquals(rpdata.getString(3), "000101");//
 					Assert.assertEquals(rpdata.getString(4), "0000008000");// amount
 					Assert.assertEquals(rpdata.getString(42), "0000623598");
 					Assert.assertEquals(rpdata.getString(123), "510101511326105");// pos
 					return DataResponse.build(mediator, new String(fo_authrp.pack()));
 				}
 				catch (ISOException | ISO8583Exception e) {
 
 				}
 				return null;
 			}
 		});
 
 		// on insert la carte dans le tpe, le tpe envoie des donnees a la carte
 		Context.getInstance().addStartPoint(new Date(), "SMART_CARD_INSERTED");
 
 		// execute simulation.
 		try {
 			SimulatorFactory.getSimulator().start();
 		}
 		catch (SimulatorException e) {
 			Assert.assertTrue(false);
 		}
 		log.debug("----TEST EPT<->CARD END----");
 	}
 }
