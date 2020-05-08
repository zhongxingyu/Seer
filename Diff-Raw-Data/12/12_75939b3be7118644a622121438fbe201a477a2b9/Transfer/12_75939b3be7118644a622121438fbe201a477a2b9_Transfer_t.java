 package de.tr0llhoehle.buschtrommel.network;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import de.tr0llhoehle.buschtrommel.IGUICallbacks;
 import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;
 
 public abstract class Transfer extends MessageMonitor implements ITransferProgress {
 
 	protected InetSocketAddress partner;
 	protected java.util.logging.Logger logger;
 	protected TransferType transferType;
 	protected String hash;
 	protected TransferStatus transferState;
 	protected long expectedTransferVolume;
	protected long initialTransferVolume;
 	protected long offset;
 	protected long totalTransferedVolume;
 	protected boolean keepTransferAlive;
 	protected static final int FALLBACK_BUFFER_SIZE = 256;
 
 	public Transfer(InetSocketAddress partner) {
 		this.partner = partner;
 	}
 	
 	protected void updatePartner(InetSocketAddress partner) {
 		this.partner = partner;
 	}
 
 	@Override
 	public TransferType getType() {
 		return transferType;
 	}
 
 	@Override
 	public long getLength() {
		return initialTransferVolume;
 	}
 
 	@Override
 	public long getOffset() {
 		return offset;
 	}
 
 	@Override
 	public long getTransferedAmount() {
 		return totalTransferedVolume;
 	}
 
 	@Override
 	public String getExpectedHash() {
 		return hash;
 	}
 
 	@Override
 	public TransferStatus getStatus() {
 		return transferState;
 	}
 
 	@Override
 	public List<ITransferProgress> getSubTransfers() {
 		return new ArrayList<>();
 	}
 
 	@Override
 	public InetSocketAddress getTransferPartner() {
 		return partner;
 	}
 
 	@Override
 	public void RegisterLogHander(Handler h) {
 		logger.addHandler(h);
 	}
 	
 	@Override
 	public void SetLoggerParent(Logger l) {
 		logger.setParent(l);
 	}
 
 	@Override
 	public void RemoveLogHander(Handler h) {
 		logger.removeHandler(h);
 	}
 	
 	@Override
 	public boolean isActive() {
 		return keepTransferAlive;
 	}
 	
 	@Override
 	public void cleanup() {
 		logger.log(Level.INFO, "cleanup transfer");
 		partner = null;
 		keepTransferAlive = false;
 		
 	}
 }
