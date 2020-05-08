 package org.gethydrated.hydra.actors.node;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.gethydrated.hydra.actors.Actor;
 import org.gethydrated.hydra.actors.ActorFactory;
 import org.gethydrated.hydra.actors.ActorRef;
 import org.gethydrated.hydra.actors.ActorSource;
 import org.gethydrated.hydra.actors.ActorSystem;
 import org.gethydrated.hydra.actors.ActorURI;
 import org.gethydrated.hydra.actors.SystemMessages.*;
 import org.gethydrated.hydra.actors.dispatch.Dispatcher;
 import org.gethydrated.hydra.actors.internal.InternalRef;
 import org.gethydrated.hydra.actors.internal.InternalRefImpl;
 import org.gethydrated.hydra.actors.internal.StandardActorFactory;
 import org.gethydrated.hydra.actors.logging.LoggingAdapter;
 import org.gethydrated.hydra.actors.mailbox.Mailbox;
 import org.gethydrated.hydra.actors.mailbox.Message;
 import org.slf4j.Logger;
 
 /**
  * @author Christian Kulpa
  *
  */
 public class ActorNode implements ActorSource, ActorContext {
     
 	private final ActorSystem system;
 
     private final InternalRef self;
 	
 	private final ActorFactory factory;
 	
 	private final String name;
 	
 	private final Logger logger;
 	
 	private final ConcurrentMap<String, InternalRef> children = new ConcurrentHashMap<>();
 
     private final Set<ActorRef> watchers = new HashSet<>();
 
     private final Set<ActorRef> watched = new HashSet<>();
 
     private final Dispatcher dispatcher;
 	
 	private final Mailbox mailbox;
 	
 	private Actor actor;
 
     private ActorRef sender = null;
 	
 	private static final ThreadLocal<ActorNode> nodeRef = new ThreadLocal<>();
 	
 	private boolean running = true;
 	
 	public ActorNode(String name, ActorFactory factory, InternalRef self, ActorSystem system, Dispatcher dispatcher) {
 		this.name = Objects.requireNonNull(name);
 		this.factory = factory;
         this.self = self;
 		this.system = system;
         this.dispatcher = dispatcher;
         this.mailbox = dispatcher.createMailbox(this);
 		logger = new LoggingAdapter(ActorNode.class, system);
 	}
 	
 	public void process(Message message) {
 		try {
             handleInternal(message.getMessage());
             sender = message.getSender();
 			actor.onReceive(message.getMessage());
             sender = null;
 		} catch (Exception e) {
 			logger.error("Error processing messages: ", e);
 		}
 	}
 
     public void processSystem(Message message) {
         Object o = message.getMessage();
         if(o instanceof Stop) {
             stop();
         } else if (o instanceof Pause) {
             pause();
         } else if (o instanceof Resume) {
             resume();
         } else if(o instanceof Watch) {
             addWatcher(((Watch)o).getTarget());
         } else if (o instanceof UnWatch) {
             deleteWatcher(((Watch) o).getTarget());
         }
     }
 
     private void handleInternal(Object o) {
     }
 
     @Override
     public ActorRef getSender() {
         return sender;
     }
 
 	@Override
 	public ActorRef spawnActor(Class<? extends Actor> actorClass, String name) {
 		return spawnActor(new StandardActorFactory(actorClass), name);
 	}
 
 	@Override
 	public ActorRef spawnActor(ActorFactory actorFactory, String name) {
 		return createChild(name, actorFactory);
 	}
 
 	@Override
 	public ActorRef getActor(String uri) {
 		if(uri.startsWith("/")) {
 			return system.getActor(uri);
 		} else if (uri.startsWith("../")) {
 			if(self.parent() != null) {
 				return self.parent().unwrap().getActor(uri.substring(3));
 			} else {
 				throw new RuntimeException("Actor not found.");
 			}
 		} else {
 			int del = uri.indexOf('/');
 			if(del == -1) {
 				ActorRef n = children.get(uri);
 				if(n != null) {
 					return n;
 				} else {
 					throw new RuntimeException("Actor not found.");
 				}
 			} else {
 				String child = uri.substring(0, del);
 				String remain = uri.substring(del+1);
 				InternalRef n = children.get(child);
 				if(n != null) {
 					return n.unwrap().getActor(remain);
 				} else {
 					throw new RuntimeException("Actor not found.");
 				}
 			}
 		}
 	}
 
 	@Override
 	public ActorRef getActor(ActorURI uri) {
 		return getActor(uri.toString());
 	}
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 
     @Override
     public void watch(ActorRef target) {
         if(target != self) {
             synchronized (watched) {
                 if(!watched.contains(target)) {
                     watched.add(target);
                     ((InternalRef)target).tellSystem(new Watch(self), self);
                 }
             }
         }
     }
 
     @Override
     public void unwatch(ActorRef target) {
         if(target != self) {
             synchronized (watched) {
                 if(watched.contains(target)) {
                     watched.remove(target);
                     ((InternalRef)target).tellSystem(new UnWatch(self), self);
                 }
             }
         }
     }
 
     @Override
     public void stop(ActorRef target) {
         ((InternalRef)target).tellSystem(new Stop(), self);
     }
 
     public ActorRef getRef() {
 		return self;
 	}
 
 	public ActorContext getContext() {
 		return this;
 	}
 	
 	public ActorSystem getSystem() {
 		return system;
 	}
 	
 	public Mailbox getMailbox() {
 		return mailbox;
 	}
 	
 	public InternalRef getChildByName(String name) {
 		return children.get(name);
 	}
 	
 	public void start() {
 		createActor();
         mailbox.setSuspended(false);
 	}
 	
 	private void stop() {
        System.out.println("Stopping actor " + name);
 		running = false;
         mailbox.setSuspended(true);
 		stopChildren();
 		try {
 		    actor.onStop();
         } catch (Exception e) {
             logger.error("Error shutting down actor ", e);
         }
 		informWatchers();
         self.parent().tellSystem(new Stopped(), self);
 	}
 
     private void pause() {
         mailbox.setSuspended(true);
     }
 
     private void resume() {
         mailbox.setSuspended(false);
     }
 
 	public boolean isTerminated() {
 		return !running;
 	}
 	
 	private void createActor() {
 		nodeRef.set(this);
 		try {
 			actor = factory.create();
 			actor.onStart();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		nodeRef.remove();
 	}
 	
	private synchronized InternalRef createChild(String name, ActorFactory factory) {
 	    if(running) {
            if(!children.containsKey(name)) {
                InternalRef node = new InternalRefImpl(name, Objects.requireNonNull(factory), self, system, dispatcher);
                try {
                    node.start();
                    children.put(name, node);
                } catch (Exception e) {
                    logger.error("Could not start actor '{}'", name, e);
                    throw e;
                }
                return node;
            } else {
                throw new RuntimeException("Actor name '" + name + "' already in use");
            }
 	    } else {
 	        throw new IllegalStateException("Actor not running");
 	    }
 	}
 	
 	private void stopChildren() {
 	    for(InternalRef n : children.values()) {
            System.out.println("sending stop to child "+n.getName());
             n.stop();
 	    }
 	}
 
     private void addWatcher(ActorRef target) {
         synchronized (watchers) {
             watchers.add(target);
         }
         if(!running) {
             target.tell(new WatcheeStopped(self), self);
         }
     }
 
     private void deleteWatcher(ActorRef target) {
         synchronized (watchers) {
             watchers.remove(target);
         }
     }
 
     private void informWatchers() {
         synchronized (watchers) {
             for(ActorRef r : watchers) {
                 r.tell(new WatcheeStopped(self), self);
             }
         }
     }
 
 	public static ActorNode getLocalActorNode() {
 		return nodeRef.get();
 	}
 
 }
