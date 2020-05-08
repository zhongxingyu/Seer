 package ee.phon.ioc.awebrec.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 
 public class RecSessionHandler {
 	private final static int ALLOCATION_SIZE = 1000000;
 	private final static int FETCH_RESULT_DELAY = 500;
	private final static int SEND_BUFFER_SIZE = 1024*64;
 	
 	private ByteFIFO buffer;
 	RecSession recSession;
 	String result;
 	boolean finishing = false;
 	final RecResultReceiver resultReceiver;
 	
 	public boolean isFinishing() {
 		return finishing;
 	}
 
 	public void setFinishing(boolean finishing) {
 		this.finishing = finishing;
 	}
 
 	public RecSessionHandler(RecResultReceiver resultReceiver) throws IOException, NotAvailableException {
 		this.resultReceiver = resultReceiver;
 		buffer = new ByteFIFO(ALLOCATION_SIZE);
 		recSession = new AWebRecSession();
 		recSession.create();
 		Thread handler = new Thread() {
 			public void run() {
 				try {
 					synchronized (buffer) {
 						while (true) {
 							if ((buffer.getSize() > SEND_BUFFER_SIZE) || finishing) {
 								//System.out.println("remaining before: " + buffer.remaining());
 								byte[] tmp = buffer.removeAll();
 								
 								//System.out.println("remaining after: " + buffer.remaining());
 								System.out.println("Sending chunk, size=" + tmp.length);
 								recSession.sendChunk(tmp, finishing);
 								
 								result = recSession.getCurrentResult();
 								if (result != null) {
 									RecSessionHandler.this.resultReceiver.receiveResult(result, false);
 								}
 								
 								if (finishing) {
 									break;
 								}
 							} else {
 								buffer.wait(FETCH_RESULT_DELAY);
 							}
 						}
 					}
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 			}
 		};
 		handler.setDaemon(true);
 		handler.start();
 	}
 	
 	public void dataFromMic(byte[] bytes) {
 		try {
 			synchronized (buffer) {
 				buffer.add(bytes);
 				buffer.notify();	
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			System.out.println(e);
 			e.printStackTrace();
 		}
 		
 		
 	}
 
 	public void waitFinal() throws InterruptedException, IOException {
 		System.out.println("waitFinal 0");
 		synchronized (buffer) {
 			while (!buffer.isEmpty()) {
 				System.out.println("waitFinal wait(), buffer size=" + buffer.getSize());
 				buffer.wait();
 			}
 		}
 		boolean finished = false;
 		do {
 			result = recSession.getCurrentResult();
 			finished = recSession.isFinished();
 			if (result != null) {
 				resultReceiver.receiveResult(result, finished);
 			}
 			Thread.sleep(FETCH_RESULT_DELAY);
 		} while (!finished);
 		
 	}
 	
      	
 }
