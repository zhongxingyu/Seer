 package botto.xmpp.service.dispatcher;
 
 import botto.xmpp.service.Bot;
 import botto.xmpp.service.BottoService;

 import com.codahale.metrics.MetricRegistry;
 import com.codahale.metrics.Timer;
 import com.google.common.base.Optional;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmpp.packet.Packet;
 
 public abstract class EnvelopeDispatcher<TLabel, TTarget> {
 
     private final EnvelopeRouter<TLabel, TTarget> router = new EnvelopeRouter<TLabel, TTarget>();
     private final Logger Log;
     private final Timer mDispatch;
 
     public EnvelopeDispatcher() {
         Log = LoggerFactory.getLogger(this.getClass());
         mDispatch = BottoService.Metrics.timer(MetricRegistry.name(this.getClass(), "packets", "processing"));
     }
 
     protected abstract ListenableConfirmation doDispatch(TTarget destination, Packet packet);
 
     public ListenableConfirmation dispatch(PacketEnvelope<TLabel> envelope) {
         // this timer measures how long it takes to process a packet:
         // for incoming packets, this includes adding and getting from the incoming queue,
         // processing in the bot and sending the response if one is generated
         // for outgoing packets, this includes only the time spent in the queue and the time to send it
         final Timer.Context time = mDispatch.time();
         Optional<TTarget> route = router.route(envelope);
         if (!route.isPresent()) {
             Log.warn("No destination found for {}", envelope);
             time.stop();
             return ListenableConfirmation.failed(new RuntimeException("Could not route packet to any destination"));
 
         }
 
         ListenableConfirmation confirmation = doDispatch(route.get(), envelope.getPacket());
         Futures.addCallback(confirmation, new FutureCallback<Boolean>() {
             @Override
             public void onSuccess(Boolean result) {
                 // TODO: log
                 time.stop();
             }
 
             @Override
             public void onFailure(Throwable t) {
                 // TODO: log
                 time.stop();
             }
         });
 
         return confirmation;
     }
 
     public final void addRoute(PacketEnvelopeFilter<TLabel> filter, TTarget target) {
         Log.info("Adding route filter {} to {}", filter, target);
         router.addRoute(filter, target);
     }
 
     public final void removeRoute(Bot bot) {
         Log.info("Removing route to bot {}", bot);
         router.removeRoute(bot);
     }
 }
