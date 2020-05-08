 package org.openspaces.analytics.archive;
 
 
 
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 
 import org.openspaces.analytics.archive.DynamicArchiverContainer.ArchiverCommand;
 import org.openspaces.analytics.archive.DynamicArchiverContainer.ArchiverCommand.Mode;
 import org.openspaces.analytics.support.AbstractMessageServer;
 import org.openspaces.analytics.support.AsyncMessage.MessageType;
 import org.openspaces.archive.ArchiveOperationHandler;
 import org.openspaces.archive.ArchivePollingContainer;
 import org.openspaces.archive.ArchivePollingContainerConfigurer;
 import org.openspaces.core.GigaSpace;
 import org.openspaces.events.DynamicEventTemplateProvider;
 import org.springframework.transaction.PlatformTransactionManager;
 
 import com.gigaspaces.annotation.pojo.SpaceId;
 
 /**
  * A manager that allows real time control of an archiver.
  * 
  * @author DeWayne
  *
  */
 public class DynamicArchiverContainer extends AbstractMessageServer<ArchiverCommand> {
 	private static final Logger log=Logger.getLogger(DynamicArchiverContainer.class.getName());
 	private GigaSpace space;
 	private ArchivePollingContainer container;
 	private ArchiveOperationHandler handler;
 	private Object template;
 	private int batchSize;
 	private PlatformTransactionManager txm;
 	private Mode currentMode=Mode.ACTIVE;
 
 	public DynamicArchiverContainer(GigaSpace space,PlatformTransactionManager txm,ArchiveOperationHandler handler,Object template, int batchSize){
 		super(space,ArchiverCommand.class);
 		this.space=space;
 		this.template=template;
 		this.handler=handler;
 		this.txm=txm;
 		this.batchSize=batchSize;
 	}
 
 	@Override
 	public ArchiverCommand handleMessage(ArchiverCommand cmd){
 		log.info("received command "+cmd.getMode());
 		cmd.setMessageType(MessageType.RESPONSE);
 
 		if(cmd.getCommand()==ArchiverCommand.Command.SET_MODE){
 			try{
 
 				switch (cmd.getMode()){
 				case PAUSED:
 					if(currentMode==Mode.PAUSED)break;
 					if(handler instanceof DiscardingArchiveHandler)((DiscardingArchiveHandler)handler).setDiscard(false);
 					if(container!=null){
 						if(!destroyContainer()){
 							cmd.setHadError(true);
 							cmd.setErrorMessage("archiver stop timed out");
 						}
 					}
 					container=null;
 					break;
 				case ACTIVE:
 					if(currentMode==Mode.ACTIVE)break;
 					if(container==null)createContainer();
 					if(handler instanceof DiscardingArchiveHandler)((DiscardingArchiveHandler)handler).setDiscard(false);
 					break;
 				case DISCARD:
 					if(!(handler instanceof DiscardingArchiveHandler)){
 						log.severe("discard mode not supported by archiver");
 						cmd.setHadError(true);
 						cmd.setErrorMessage("discard mode not supported by archiver");
 						break;
 					}
 					if(currentMode==Mode.DISCARD)break;
 					((DiscardingArchiveHandler)handler).setDiscard(true);
 					if(container==null)createContainer();
 					break;
 				}
 			}
 			catch(Throwable t){
 				cmd.setHadError(true);
 				cmd.setErrorMessage(t.getMessage());
 			}
 
 		}
 		else if(cmd.getCommand()==ArchiverCommand.Command.GET_MODE){
 			cmd.setMode(currentMode);
 		}
 		return cmd;
 	}
 
 	private boolean stopContainer(){
 		container.stop();
 		for(int i=0;i<10;i++){
 			if(!container.isRunning())return true;
 			try{
 				Thread.sleep(500);
 			}
 			catch(Exception e){}
 		}
 		return false;
 	}
 
 	private boolean destroyContainer(){
 		container.destroy();
 		for(int i=0;i<10;i++){
 			if(!container.isActive())return true;
 			try{
 				Thread.sleep(500);
 			}
 			catch(Exception e){}
 		}
 		return false;
 	}
 
 	private void createContainer() {
 
 		if(container==null){
 			log.info("creating container");
 			container=new ArchivePollingContainerConfigurer(space)   
 			.archiveHandler(handler)
 			.transactionManager(txm)
 			.autoStart(true)
 			.batchSize(batchSize)
 			.dynamicTemplate(new DynamicEventTemplateProvider() {
 				@Override
 				public Object getDynamicTemplate() {
 					return template;
 				}}).create();
 		}
 	}
 
 	@PostConstruct
 	public void postConstruct(){
 		startListener();
 	}
 
 	@PreDestroy
 	public void preDestroy(){
 		stopListener();
 	}
 
 	//Do not remove the explicit annotation reference: breaks build
 	@com.gigaspaces.annotation.pojo.SpaceClass
	public static class ArchiverCommand implements org.openspaces.analytics.support.AsyncMessage
 	{
 		private String id;
 		private Command command;
 		private Mode mode;
 		private MessageType messageType;
 		private Boolean hadError=false;
 		private String errmsg;
 
 		public static enum Command{
 			GET_MODE,
 			SET_MODE
 		}
 		public static enum Mode{
 			PAUSED,
 			ACTIVE,
 			DISCARD;
 
 			public static Mode parse(String modestr) {
 				if(modestr.toLowerCase().equals("paused"))return PAUSED;
 				if(modestr.toLowerCase().equals("active"))return PAUSED;
 				if(modestr.toLowerCase().equals("discard"))return PAUSED;
 				return null;
 			}
 		}
 
 		public ArchiverCommand(){}
 
 		public ArchiverCommand(Mode mode){
 			this.mode=mode;
 		}
 
 		public Mode getMode() {
 			return mode;
 		}
 
 		public void setMode(Mode mode) {
 			this.mode = mode;
 		}
 		@Override
 		public void setId(String id) {
 			this.id=id;
 		}
 		@Override
 		@SpaceId(autoGenerate=true)
 		public String getId() {
 			return id;
 		}
 		@Override
 		public MessageType getMessageType() {
 			return messageType;
 		}
 		@Override
 		public void setMessageType(MessageType messageType) {
 			this.messageType=messageType;
 		}
 
 		@Override
 		public Boolean getHadError() {
 			return hadError;
 		}
 
 		@Override
 		public void setHadError(Boolean status) {
 			hadError=status;
 		}
 
 		@Override
 		public String getErrorMessage() {
 			return errmsg;
 		}
 
 		@Override
 		public void setErrorMessage(String message) {
 			errmsg=message;
 		}
 
 		public Command getCommand() {
 			return command;
 		}
 
 		public void setCommand(Command command) {
 			this.command = command;
 		}
 
 	}
 
 }
