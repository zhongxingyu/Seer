 package cern.ess.opclib;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 
 import cern.ess.opclib.OPC;
 
 class OpcApiImpl implements OpcApi
 {
 	@Override
 	public synchronized void init(String host, String server) throws OPCException 
 	{
 		OPC.init(host, server);		
 	}	
 	
 	@Override
 	public synchronized String[] getItemNames() throws OPCException 
 	{
 		return OPC.getItemNames();
 	}
 
 	@Override
 	public synchronized String[] getLocalServerList() throws OPCException 
 	{
 		return OPC.getLocalServerList();
 	}
 
 	@Override
 	public synchronized boolean readBoolean(String item) throws OPCException 
 	{
 		return OPC.readBoolean(item);
 	}
 
 	@Override
 	public synchronized float readFloat(String item) throws OPCException 
 	{
 		return OPC.readFloat(item);
 	}
 
 	@Override
 	public synchronized int readInt(String item) throws OPCException 
 	{
 		return OPC.readInt(item);
 	}
 
 	@Override
 	public synchronized String readString(String item) throws OPCException 
 	{
 		return OPC.readString(item);
 	}
 
 	@Override
 	public synchronized void writeBoolean(String item, boolean val) throws OPCException 
 	{
		OPC.writeBoolean(item, val);
 	}
 
 	@Override
 	public synchronized void writeFloat(String item, String type, float val) throws OPCException 
 	{
		OPC.writeFloat(item, type, val);
 	}
 
 	@Override
 	public synchronized void writeInt(String item, String type, int val) throws OPCException 
 	{
		OPC.writeInt(item, type, val);
 	}
 
 	@Override
 	public synchronized void writeString(String item, String val) throws OPCException 
 	{
		OPC.writeString(item, val);
 	}
 
 	@Override
 	public int stop() throws InterruptedException, ExecutionException, TimeoutException 
 	{
 		throw new UnsupportedOperationException("not supported in this context - only thread based implementations support this (like OPCClient)");
 	}
 }
