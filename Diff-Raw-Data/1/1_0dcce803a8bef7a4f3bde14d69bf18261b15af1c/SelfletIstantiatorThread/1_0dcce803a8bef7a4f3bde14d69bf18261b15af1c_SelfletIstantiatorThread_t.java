 package it.polimi.elet.selflet.istantiator;
 
 import org.apache.log4j.Logger;
 
 import polimi.reds.TCPDispatchingService;
 
 import com.google.common.collect.Sets;
 
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.id.ISelfLetID;
 import it.polimi.elet.selflet.message.MessageBridge;
 import it.polimi.elet.selflet.message.RedsMessage;
 import it.polimi.elet.selflet.message.SelfLetMessageTypeEnum;
 import it.polimi.elet.selflet.message.SelfLetMsg;
 
 public class SelfletIstantiatorThread extends Thread {
 
 	private static final Logger LOG = Logger
 			.getLogger(SelfletIstantiatorThread.class);
 	private static final long MINIMUM_TIME_BETWEEN_ISTANTIATIONS = DispatcherConfiguration.minimumTimeBetweenTwoIstantiationsInSec * 1000;
 
 	private static final String DEFAULT_TEMPLATE = DispatcherConfiguration.defaultProjectTemplate;
 	private static final String COMPLETE_TEMPLATE = DispatcherConfiguration.completeProjectTemplate;
 
 	private static long lastIstantiationTime = 0;
 
 	private final ISelfletIstantiator selfletIstantiator = SelfletIstantiator
 			.getInstance();
 	private final SelfLetMsg selfletMessage;
 	private final TCPDispatchingService dispatchingService;
 	private final String selfletTemplate;
 
 	public SelfletIstantiatorThread(TCPDispatchingService dispatchingService,
 			SelfLetMsg selfletMessage) {
 		this.selfletMessage = selfletMessage;
 		this.dispatchingService = dispatchingService;
 		this.selfletTemplate = DEFAULT_TEMPLATE;
 	}
 
 	@Override
 	public void run() {
 		if (recentlyIstantiatedNewSelflet()) {
 			LOG.debug("Skipping selflet istantiation. Already instantiated recently");
 			return;
 		}
 		LOG.debug("Istantiating new selflet");
 		try {
 			// AllocatedSelflet allocatedSelflet =
 			selfletIstantiator.istantiateNewSelflet(selfletTemplate);
			lastIstantiationTime = System.currentTimeMillis();
 			// replyToSelflet(allocatedSelflet.getSelfletID());
 		} catch (IllegalStateException e) {
 			LOG.error("No more IPs available", e);
 		}
 	}
 
 	private boolean recentlyIstantiatedNewSelflet() {
 		long now = System.currentTimeMillis();
 		long elapsed = (now - lastIstantiationTime);
 		return elapsed < MINIMUM_TIME_BETWEEN_ISTANTIATIONS;
 	}
 
 	private void replyToSelflet(ISelfLetID newSelfletID) {
 		RedsMessage reply = createReply(selfletMessage.getFrom(), newSelfletID);
 		dispatchingService.reply(reply, selfletMessage.getId());
 	}
 
 	private RedsMessage createReply(ISelfLetID receiver, ISelfLetID newSelfletID) {
 		SelfLetMsg selfletMsg = new SelfLetMsg(MessageBridge.THIS_SELFLET_ID,
 				receiver, SelfLetMessageTypeEnum.ISTANTIATE_NEW_SELFLET_REPLY,
 				newSelfletID);
 		return new RedsMessage(selfletMsg, Sets.newHashSet(receiver.toString()));
 	}
 
 }
