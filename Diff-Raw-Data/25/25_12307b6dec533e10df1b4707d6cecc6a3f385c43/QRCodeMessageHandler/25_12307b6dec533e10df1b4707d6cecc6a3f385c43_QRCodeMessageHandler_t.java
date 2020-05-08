 package de.dsi8.vhackandroidgame.handler;
 
 import de.dsi8.dsi8acl.communication.handler.AbstractMessageHandler;
 import de.dsi8.dsi8acl.communication.impl.CommunicationPartner;
 import de.dsi8.dsi8acl.exception.InvalidMessageException;
 import de.dsi8.vhackandroidgame.communication.model.QRCodeMessage;
 import de.dsi8.vhackandroidgame.logic.impl.PresentationLogic;
 
 public class QRCodeMessageHandler extends AbstractMessageHandler<QRCodeMessage> {
 
 	private PresentationLogic	presentationLogic;
 
 	public QRCodeMessageHandler(PresentationLogic presentationLogic) {
 		this.presentationLogic = presentationLogic;
 	}
 
 	@Override
 	public void handleMessage(CommunicationPartner partner, QRCodeMessage message)
 			throws InvalidMessageException {
		if(message.QRCodeProvided()) {
 			presentationLogic.getPresentationView().showQRCode(message.text,
 					message.position);			
 		} else {
 			presentationLogic.getPresentationView().disableQRCode(message.position);
 		}
 	}
 
 }
