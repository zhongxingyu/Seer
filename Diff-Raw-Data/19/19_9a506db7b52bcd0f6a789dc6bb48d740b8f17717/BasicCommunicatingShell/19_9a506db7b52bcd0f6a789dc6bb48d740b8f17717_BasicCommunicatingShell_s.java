 package dima.introspectionbasedagents.shells;
 
 import java.util.Collection;
 import java.util.Date;
 
 import dima.basiccommunicationcomponents.AbstractMailBox;
 import dima.basiccommunicationcomponents.AbstractMessage;
 import dima.basicinterfaces.DimaComponentInterface;
 import dima.basicinterfaces.MailBoxBasedCommunicatingComponentInterface;
 import dima.introspectionbasedagents.services.loggingactivity.LogService;
 import dima.introspectionbasedagents.shells.BasicCommunicatingMethodTrunk.UnHandledMessageException;
 
 public class BasicCommunicatingShell extends BasicIntrospectiveShell {
 
 	private static final long serialVersionUID = 7066756901339740248L;
 
 
 	//
 	// Fields
 	//
 
 	protected final AbstractMailBox mailBox;
 
 	//
 	// Constructor
 	//
 
 	public BasicCommunicatingShell(
 			final MailBoxBasedCommunicatingComponentInterface myComponent) {
 		super(myComponent, new BasicCommunicatingMethodTrunk());
 		this.mailBox=myComponent.getMailBox();
 	}
 
 
 	public BasicCommunicatingShell(
 			final DimaComponentInterface myComponent,
 			final AbstractMailBox mailBox) {
 		super(myComponent, new BasicCommunicatingMethodTrunk());
 		this.mailBox=mailBox;
 	}
 
 	/*
 	 *
 	 */
 
 	public BasicCommunicatingShell(
 			final MailBoxBasedCommunicatingComponentInterface myComponent,
 			final SimpleExceptionHandler exceptionHandler) {
 		super(myComponent, new BasicCommunicatingMethodTrunk(), exceptionHandler);
 		this.mailBox=myComponent.getMailBox();
 	}
 
 	public BasicCommunicatingShell(
 			final DimaComponentInterface myComponent,
 			final AbstractMailBox mailBox,
 			final SimpleExceptionHandler exceptionHandler) {
 		super(myComponent, new BasicCommunicatingMethodTrunk(), exceptionHandler);
 		this.mailBox=mailBox;
 	}
 
 	//
 	// Accessors
 	//
 
 	public AbstractMailBox getMailBox() {
 		return this.mailBox;
 	}
 
 	@Override
 	public BasicCommunicatingMethodTrunk getMyMethods() {
 		return (BasicCommunicatingMethodTrunk) super.getMyMethods();
 	}
 
 	//
 	// Methods
 	//
 
 	@Override
 	public final void step(final Date creation){
 		this.parseMails();
 		super.step(creation);
 	}
 
 	//
 	// Primitives
 	//
 
 	protected AbstractMessage getNextMail(){
 		return this.mailBox.readMail();
 	}
 
 	/**
 	 * Step behavior to handle the mail box mails
 	 */
 	protected void parseMails() {
 
 		while (this.getMailBox().hasMail()){
 			final AbstractMessage mess = this.getNextMail();
 			//			parseJavaMessage(mess);
 			try {
 				final Collection<MethodHandler> mts = this.getMyMethods().parseMail(mess);
 				for (final MethodHandler mt : mts) {
 					this.metToRemove.add(mt);
 				}
 			} catch (final UnHandledMessageException e) {
 				// Unhandled envellope
 				LogService.writeWarning(
 						this.getStatus(),
						" Unhandled envellope!:\n"
 								+mess+
 								"\n sended by "+mess.getSender()+" to "+mess.getReceiver()+
 								"\n --> Known envellopes are: "+this.getMyMethods().getHandledEnvellope()+
 								"\n --> Exception handle say :\n"+this.getExceptionHandler().handleUnhandledMessage(mess, this.getStatus()));
 			} catch (final Throwable e) {
 				this.getExceptionHandler().handleException(e, this.getStatus());
 			}
 
 			this.getStatus().resetCurrentlyReadedMail();
 			this.getStatus().resetCurrentlyExecutedMethod();
 
 			for (final MethodHandler meth : this.metToRemove) {
 				this.getMyMethods().removeMethod(meth);
 			}
 		}
 	}
 
 	//	private void parseJavaMessage(AbstractMessage mess) {
 	//		if (mess instanceof Message //MESSAGE TYPE EST JAVA : EXECUTION AUTOMATIQUE
 	//				&& ((Message) mess).getType().equals("java")
 	//				&& this.getMyMethods().getMyComponent() instanceof BasicCommunicatingAgent)
 	//			((BasicCommunicatingAgent) this.getMyMethods().getMyComponent()).processMessage(
 	//					(Message) mess);
 	//	}
 }
 
 
 
 
 
 ///**
 //* Parse all the mails of agent ag.
 //*
 //* @param myComponent
 //*            agent to that will handle the mails
 //* @param b
 //*            the mail box of ag
 //*/
 //void parseMails() {
 //final Collection<Message> unparsed = new ArrayList<Message>();
 //for (Message m : myAgent.getReceivedMessages())
 //try {
 //myAgent.getStatus().setCurrentlyReadedMail(m);
 //this.parseMail(m);
 //myAgent.getStatus().resetCurrentlyReadedMail();
 //} catch (final UnHandledMessageException e) {
 //if (e.cause.equals(UnHandledMessageExceptionType.TickerNotReady))
 ////To be re-read when the method ticker will be ready
 //unparsed.add(m);
 //else // Unhandled envellope
 //if (this.myAgent.getAgent() instanceof BasicCommunicatingAgent) {
 //((BasicCommunicatingAgent) this.myAgent.getAgent()).getMailBox().writeMail(m);
 //myAgent.logException(
 //+ "it has been added to the mail box for be handled by the agent step()"
 //+ " :\n" + m);
 //} else
 //myAgent.logException("Unhandled mail: \n"
 //+ m + "\n * * message has been lost!");
 //}
 //for (final Message m : unparsed)
 //this.myAgent.getCom().receiveAsyncMessage(m);
 //}
 /*
  *
  */
 //
 ///**
 // * handle the mail box mails
 // * with the annotated methods
 // * @param mess
 // * @throws UnHandledMessageException
 // */
 //protected void processMail(final AbstractMessage mess) throws UnHandledMessageException {
 //
 //	MethodHandler mt =  this.getMyMethods().getMethod(mess);
 //	final Set<MethodHandler> metToRemove = new HashSet<MethodHandler>();
 //	this.getStatus().setCurrentlyReadedMail(mess);
 //	this.getStatus().setCurrentlyExecutedMethod(mt);
 //
 //	Object resultat;
 //	try {
 //		resultat = this.getMyMethods().execute(mt, new Object[] { mess });
 //		// Remove the method if transient
 //		if (mt.isAnnotationPresent(Transient.class)
 //				&& resultat != null
 //				&& resultat.equals(new Boolean(true)))
 //			this.getMyMethods().removeMessageMethod(mt);
 //
 //	} catch (InvocationTargetException ex) {
 //		if (this.getExceptionHandler() == null)
 //			LoggerManager.writeException(this,
 //					"Method "+mt.getMethodName()
 //					+"\n(" + this.getStatus()+")"
 //					+"\n has raised EXCEPTION :\n" , ex.getCause());
 //		else
 //			this.getExceptionHandler().handleException(ex, this.getStatus());
 //	}
 //
 //	this.getStatus().resetCurrentlyReadedMail();
 //	this.getStatus().resetCurrentlyExecutedMethod();
 //}
