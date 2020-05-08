 package fi.jawsy.jawwa.zk.frp;
 
 import java.io.Serializable;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import lombok.val;
 
 import org.zkoss.zk.ui.Desktop;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 
 import com.google.common.collect.Sets;
 
 import fi.jawsy.jawwa.frp.CleanupHandle;
 import fi.jawsy.jawwa.frp.EventSink;
 import fi.jawsy.jawwa.frp.EventStream;
 import fi.jawsy.jawwa.frp.EventStreamBase;
 import fi.jawsy.jawwa.lang.Effect;
 
 public class ServerPushEventSource<E> extends EventStreamBase<E> implements EventSink<E> {
 
     private static final long serialVersionUID = -9056310527499308343L;
 
     private final Desktop desktop;
 
     private final EventListener<ServerPushEvent<E>> listener;
 
     private final Set<Effect<? super E>> effects = Sets.newLinkedHashSet();
     private final AtomicBoolean hasListeners = new AtomicBoolean();
 
     static class ServerPushEvent<E> extends Event {
         private static final long serialVersionUID = 3159112399616454726L;
 
         public final E data;
 
         public ServerPushEvent(E data) {
             super("onServerPush", null, null);
             this.data = data;
         }
     }
 
     public ServerPushEventSource() {
         this(Executions.getCurrent().getDesktop());
     }
 
     public ServerPushEventSource(Desktop desktop) {
         this.desktop = desktop;
         class Listener implements EventListener<ServerPushEvent<E>>, Serializable {
             private static final long serialVersionUID = 1297814172054452846L;
 
             @Override
             public void onEvent(ServerPushEvent<E> event) throws Exception {
                 for (val effect : effects) {
                     effect.apply(event.data);
                 }
             }
         }
         this.listener = new Listener();
     }
 
     @Override
     public CleanupHandle foreach(final Effect<? super E> e) {
         effects.add(e);
         desktop.enableServerPush(true);
         hasListeners.set(true);
         class ServerPushCleanup implements CleanupHandle, Serializable {
             private static final long serialVersionUID = 3049683062815028246L;
 
             @Override
             public void cleanup() {
                 effects.remove(e);
                 if (effects.isEmpty())
                     hasListeners.set(false);
             }
         }
         return new ServerPushCleanup();
     }
 
     @Override
     public void fire(E event) {
         if (hasListeners.get())
             Executions.schedule(desktop, listener, new ServerPushEvent<E>(event));
     }
 
     @Override
    public CleanupHandle pipeFrom(EventStream<? extends E> es) {
         class FireEvent implements Effect<E>, Serializable {
             private static final long serialVersionUID = -6329853533953354977L;
 
             @Override
             public void apply(E input) {
                 fire(input);
             }
         }
         return es.foreach(new FireEvent());
     }
 
 }
