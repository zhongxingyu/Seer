 package de.leonhardt.sbm.gui.newGui.worker;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.swing.SwingWorker;
 import org.beanfabrics.model.ListPM;
 
 import de.leonhardt.sbm.core.model.Message;
 import de.leonhardt.sbm.core.util.Utils;
 import de.leonhardt.sbm.gui.common.MessagePM;
 import de.leonhardt.sbm.gui.common.resource.IconService;
 
 /**
  * Background Worker to generate MesagePMs and populate the list model afterwards.
  *
  * This worker processes messages in chunks, so the list will be filled continuously.
  *
  * @author Frederik Leonhardt
  */
 public class PopulateListWorker extends SwingWorker<Void, MessagePM> {
 
 	private Logger log;
 	private Collection<Message> messages;
 	private ListPM<MessagePM> messagePMs;
 	private IconService is;
 	
 	// chunk size
 	private static final int CHUNK_SIZE = 50;
 	
 	/**
 	 * Creates a new populate list worker.
 	 * @param messages - the messages to use
 	 * @param messagePMs - the LiustPM to fill
 	 * @param iconService - the icon service to propagate to the MesagePMs
 	 */
 	public PopulateListWorker(Collection<Message> messages, ListPM<MessagePM> messagePMs, IconService iconService) {
 		this.messages = messages;
 		this.messagePMs = messagePMs;
 		this.is = iconService;
 		this.log = Logger.getLogger("GenerateMessageViewsWorker");
 	}
 	
 	@Override
 	protected Void doInBackground() throws Exception {
 		MessagePM[] chunks = new MessagePM[CHUNK_SIZE];
 		
 		setText("Rendering messages..");
 		int curPos = 0; // counts position in array
 		int i = 0; // counts position in messages
 		for (Message msg: messages) {
 			curPos = i % CHUNK_SIZE;
 			
			MessagePM mPM = new MessagePM(msg, is);
 
 			chunks[curPos] = mPM;
 
 			// publish when array is filled
 			if (curPos == CHUNK_SIZE - 1) {
 				publish(chunks);
 				// reset array
 				chunks = new MessagePM[CHUNK_SIZE];
								
 				// announce progress
 				int progress = Utils.calcProgress(i+1, messages.size());
 				setProgress(progress);
 				setText("Rendering messages.. %d%%", progress);
 			}
 			
 			i++;
 		}

		// If we handled exactly CHUNK_SIZE messages before,
		// the counter will be at CHUNK_SIZE - 1.
		// In this case we are already done.
		if (curPos < CHUNK_SIZE - 1) {
			// trim array to remove null values at the end
			publish(Arrays.copyOf(chunks, curPos+1));
		}
 		
 		// finish up
 		setProgress(100);
 		// log status
 		log.info("[Render] Rendered " + messages.size() + " messages in this conversation.");
 		return null;
 	}
 	
 	@Override
 	protected void process(List<MessagePM> chunks) {
 		// populate model step by step
 		messagePMs.addAll(chunks);
 	}
 
 	@Override
 	public void done() {
 		log.fine("[Render] Done.");
 			
 		setText("%d messages in this conversation.", messages.size());
 	}
 	
     /**
      * Fires a property change (text)
      * @param text
      */
     private void setText(String text) {
 		firePropertyChange("text", null, text);
     }
     
     /**
      * Fires a property change (text)
      * Takes a format string.
      * @param format
      * @param args
      */
     private void setText(String format, Object... args) {
     	firePropertyChange("text", null, String.format(format, args));
     }
 
 }
