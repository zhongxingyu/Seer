 package dima.introspectionbasedagents.shells;
 
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import dima.basiccommunicationcomponents.AbstractMessage;
 import dima.basicinterfaces.AbstractMessageInterface;
 import dima.basicinterfaces.DimaComponentInterface;
 import dima.introspectionbasedagents.annotations.MessageCollectionHandler;
 import dima.introspectionbasedagents.annotations.MessageHandler;
 import dima.introspectionbasedagents.ontologies.ClassEnveloppe;
 import dima.introspectionbasedagents.ontologies.Envelope;
 import dima.introspectionbasedagents.ontologies.MessageInEnvelope;
 import dima.introspectionbasedagents.services.loggingactivity.LogService;
 import dima.support.DimaException;
 import dimaxx.tools.mappedcollections.HashedHashSet;
 
 public class BasicCommunicatingMethodTrunk extends BasicIntrospectedMethodsTrunk implements CommunicationMethodsTrunk {
 
 	//
 	// Fields
 	//
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 7246686690403688221L;
 
 	private final HashedHashSet<Envelope, MethodHandler> messageMethods =
 			new HashedHashSet<Envelope, MethodHandler>();
 
 	private final HashedHashSet<MethodHandler, AbstractMessage> collectedMessage =
 			new HashedHashSet<MethodHandler, AbstractMessage>();
 
 	//
 	// Construtor
 	//
 
 	public BasicCommunicatingMethodTrunk() {
 		super();
 	}
 
 	//
 	// Accessors
 	//
 
 	@Override
 	public void removeMethod(final MethodHandler mt){
 		if (this.isStepMethod(mt)) {
 			super.removeMethod(mt);
 		}
 		if (this.isMessageMethod(mt)){
 			this.messageMethods.remove(this.getEnvellopeOfMethod(mt));
 			this.collectedMessage.remove(mt);
 		}
 	}
 
 	protected boolean isMessageMethod(final MethodHandler mt){
 		return mt.isAnnotationPresent(MessageHandler.class)
 				|| mt.isAnnotationPresent(MessageCollectionHandler.class);
 	}
 
 	@Override
 	public Collection<Envelope> getHandledEnvellope(){
 		return this.messageMethods.keySet();
 	}
 
 	//
 	// Methods
 	//
 
 	//Return the methods to be removed
 	@Override
 	public Collection<MethodHandler> parseMail(final AbstractMessage m)
 			throws UnHandledMessageException, IllegalArgumentException, Throwable{
 
 		final Collection<MethodHandler> toRemove = new ArrayList<MethodHandler>();
 		this.getStatus().setCurrentlyReadedMail(m);
 		final Collection<MethodHandler> mts = this.getMethod(m);
 
 		for (final MethodHandler mt : mts){
 
 			this.getStatus().setCurrentlyExecutedAgent(mt.getMyComponent());
 			this.getStatus().setCurrentlyExecutedMethod(mt);
 
 			if (mt.isAnnotationPresent(MessageHandler.class)){
 				final Object resultat = mt.execute(new Object[]{m});
 
 				if (this.toRemove(mt, resultat)) {
 					toRemove.add(mt);
 				}
 			}
 
 			else if (mt.isAnnotationPresent(MessageCollectionHandler.class)){
 				this.collectedMessage.add(mt, m);
 
 				final Object resultat = mt.execute(
 						new Object[]{this.collectedMessage.get(mt)});
 
 				if (resultat != null && resultat.equals(new Boolean(true))) {
 					this.collectedMessage.remove(mt);
 				}
 
 				if (this.toRemove(mt, resultat)) {
 					toRemove.add(mt);
 				}
 			} else {
 				LogService.writeException(mt.getMyComponent(), "Impossible : mauvais annotation pour "+mt);
 			}
 
 			this.getStatus().resetCurrentlyExecutedMethod();
 			this.getStatus().resetCurrentlyExecutedAgent();
 		}
 
 		return toRemove;
 	}
 
 	//
 	// Primitives
 	//
 
 	/*
 	 * Override the initiation methods :
 	 */
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected Collection<MethodHandler> getRelevantMethods(final DimaComponentInterface a){
 		final Collection<MethodHandler> r =
 				this.getMethods(a, MessageCollectionHandler.class,MessageHandler.class);
 		r.addAll(super.getRelevantMethods(a));
 		return r;
 	}
 
 	@Override
 	protected boolean checkMethodValidity(final MethodHandler mt) {
 		return super.checkMethodValidity(mt) && this.checkMessageMethodValidity(mt);
 	}
 
 	@Override
 	protected void addMethod(final MethodHandler mt){
 		super.addMethod(mt);
 		if (this.isMessageMethod(mt)){
 			final Envelope e = this.getEnvellopeOfMethod(mt);
 			//			if (getMyComponent() instanceof IdentifiedComponentInterface)
 			//				if (((IdentifiedComponentInterface) getMyComponent()).getIdentifier().toString().equals("OBSERVER"))
 			//					LoggerManager.write(getMyComponent(), "envellope "+e+" added !");
 
 			for (final MethodHandler mtAlrea : this.messageMethods.get(e)) {
 				if (mtAlrea.getMyComponent().equals(mt.getMyComponent())) {
 					throw new RuntimeException(mt.getMyComponent()+" : "+
 							"Duplicate methods for parsing fipa message : " + e
 							+ "\n --> " + mt + "\n -->  AND "
 							+ this.messageMethods.get(e) + ")");
 				}
 			}
 
 			this.messageMethods.add(e, mt);
 
 		}
 	}
 
 	/*
 	 *
 	 */
 
 	private boolean checkMessageMethodValidity(final MethodHandler mt) {
 		if (mt.isAnnotationPresent(MessageHandler.class)){
 			if (!(mt.getParameterTypes().length == 1
 					&& AbstractMessage.class.isAssignableFrom(
 							mt.getParameterTypes()[0]))) {
 				LogService.writeException(
 						mt.getMyComponent(),
 						"Wrong parameters type for message parser method " + mt
 						+ " should be only one message class");
 				return false;
 			} else {
 				return true;
 			}
 		} else if (mt.isAnnotationPresent(MessageCollectionHandler.class)){
 			if (mt.getParameterTypes().length == 1
 					&&
 					AbstractMessage.class.isAssignableFrom(mt.getGenericClassOfFirstArgument())
 					&&
 					(mt.getReturnType().equals(boolean.class) || mt.getReturnType().equals(Boolean.class))) {
 				return true;
 			} else{
 				LogService.writeException(
 						mt.getMyComponent(),
 						"Wrong parameters type for message parser method "
 								+ mt
 								+ " should be only one message class");
 				return false;
 			}
 		} else {
 			return true;
 		}
 	}
 	/**
 	 * Compute the envelope of a message
 	 * @return the associated method
 	 * @throws UnHandledMessageException
 	 * if no method is associated to the message envelope
 	 */
 	@SuppressWarnings("unchecked")
 	private Collection<MethodHandler> getMethod(final AbstractMessage mess)
 			throws UnHandledMessageException{
 		// Generation de l'envellope
 		Envelope e = BasicCommunicatingMethodTrunk.getEnvellopeOfMessage(mess);
 
 		/*
 		 * Dans le cas ou l'envellope est une classe envellope, si elle n'est
 		 * pas trouvé dans les table de hasahage, la recherche est effectuer
 		 * dans les class mere de la classe du message
 		 */
 		if (e.getClass().equals(ClassEnveloppe.class)) {
 			Class<?> m = mess.getClass();
 			while (!this.messageMethods.containsKey(e)
					&& AbstractMessageInterface.class.isAssignableFrom(m.getClass()
							.getSuperclass())) {
				m = m.getClass().getSuperclass();
 				e = new ClassEnveloppe((Class<? extends AbstractMessageInterface>) m);
 			}
 		}
 
 		/*
 		 * Recuperation de la methode
 		 */
 		if (!this.messageMethods.containsKey(e)) {
 			//System.out.println("OOOOOOOOOOOOOOOHHHHHHHH =(\n"+messageMethods);
 			throw new UnHandledMessageException(mess);
 		} else {
 			//System.out.println("YEEEEEEEEEEAAAAAAAH =)\n"+messageMethods);
 			return this.messageMethods.get(e);
 		}
 	}
 
 	/*
 	 *
 	 */
 
 	public static Envelope getEnvellopeOfMessage(final AbstractMessage mess) {
 		if (mess instanceof MessageInEnvelope) {
 			return ((MessageInEnvelope) mess).getMyEnvelope();
 		} else {
 			return new ClassEnveloppe(mess);
 		}
 	}
 
 	/**
 	 * Compute the envelope of a message
 	 */
 	private Envelope getEnvellopeOfMethod(final MethodHandler mt) {
 
 		Envelope e = null;
 		//Searching annotation of envelope
 		for (final Annotation a : mt.getAnnotations()) {
 			if (a.annotationType().getEnclosingClass() != null
 					&& Envelope.class.isAssignableFrom(
 							a.annotationType().getEnclosingClass())) {
 				if (e!=null){
 					//The method has more than one envelope!
 					LogService.writeException(
 							mt.getMyComponent(),
 							"Conflicting envelopes for method "+mt);
 					return null;
 
 				} else {
 					//Getting the envelope:
 					try {
 						e = (Envelope) a.annotationType().getEnclosingClass().
 								getConstructor(a.annotationType(), MethodHandler.class).
 								newInstance(a,mt);
 					} catch (final Exception ex) {
 						LogService.writeException(mt.getMyComponent(),
 								"La classe envelope " +
 										"n'est pas bien construite pour la methode "
 										+mt+" et l'annotation "+a, ex);
 						return null;
 					}
 				}
 			}
 		}
 
 		if (e!=null) {
 			return e;
 		} else {
 			return new ClassEnveloppe(this.getMessageTypeOfMethod(mt));
 		}
 	}
 
 	private Class<? extends AbstractMessageInterface> getMessageTypeOfMethod(final MethodHandler mt){
 		if (mt.isAnnotationPresent(MessageHandler.class)) {
 			return (Class<? extends AbstractMessageInterface>) mt.getParameterTypes()[0];
 		} else if (mt.isAnnotationPresent(MessageCollectionHandler.class)) {
 			return (Class<? extends AbstractMessageInterface>) mt.getGenericClassOfFirstArgument();
 		} else{
 			LogService.writeException(
 					mt.getMyComponent(),
 					"Inappropriate usage : the method is not a message handler:"+mt);
 			return null;
 		}
 	}
 
 	//
 	// SubClass
 	//
 
 
 	public class UnHandledMessageException extends DimaException {
 
 		private static final long serialVersionUID = 9075235122415400081L;
 		AbstractMessage mess;
 
 		public UnHandledMessageException(final AbstractMessage mess) {
 			this.mess = mess;
 		}
 	}
 }
 
 
 //LoggerManager.write(getMyComponent(), "adding method "+mt+"\n"+Arrays.asList(mt.getAnnotations()).toString());
 //
 //LoggerManager.write(getMyComponent(), "using env: "+e);
 //LoggerManager.write(getMyComponent(), "using class env: "+new ClassEnveloppe(getMessageTypeOfMethod(mt)));
