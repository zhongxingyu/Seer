 package pt.com.broker.client.net;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.concurrent.CustomExecutors;
 
 import pt.com.broker.client.BaseNetworkConnector;
 
 /**
  * ProtocolHandler is an abstract class that defines the basic functionality for implementing Sapo-Broker messaging aspects.
  * 
  */
 
 public abstract class ProtocolHandler<T>
 {
 	private final AtomicBoolean _isReconnecting = new AtomicBoolean(false);
 
 	private final ExecutorService exec = CustomExecutors.newThreadPool(4, "protocol-handler");
 
 	private final ScheduledExecutorService shed_exec = CustomExecutors.newScheduledThreadPool(1, "sched-protocol-handler");
 
 	protected AtomicBoolean closed = new AtomicBoolean(false);
 
 	public abstract T decode(DataInputStream in) throws IOException;
 
 	public abstract void encode(T message, DataOutputStream out) throws IOException;
 
 	public abstract void onConnectionClose();
 
 	public abstract void onConnectionOpen();
 
 	public abstract void onError(Throwable error);
 
 	protected abstract void onIOFailure(long connectionVersion);
 
 	public abstract BaseNetworkConnector getConnector();
 
 	protected abstract void handleReceivedMessage(T request);
 
 	protected abstract int getNumberOfTries();
 
 	private final Runnable reader = new Runnable()
 	{
 		public void run()
 		{
 			BaseNetworkConnector connector = getConnector();
 			DataInputStream in = connector.getInput();
 
 			boolean continueReading = true;
 
 			long connectionVersion = connector.getConnectionVersion();
 
 			while (continueReading)
 			{
 
 				try
 				{
 					T message = doDecode(in);
 					handleReceivedMessage(message);
 				}
 				catch (Throwable error)
 				{
 					final Throwable rootCause = ErrorAnalyser.findRootCause(error);
 
 					if (getNumberOfTries() == 0)
 					{
 						if(!closed.get())
 						{
 							onError(rootCause);
 						}
 						return;
 					}
 
 					if (rootCause instanceof IOException)
 					{
 						if (!connector.isClosed())
 							onIOFailure(connectionVersion);
 						continueReading = false;
 					}
 					else
 					{
 						try
 						{
 							if (!closed.get())
 							{
 								onError(rootCause);
 							}
 						}
 						catch (Throwable t)
 						{
 							// ignore
 						}
 					}
 
 				}
 			}
 		}
 	};
 
 	private Throwable resetConnection(final BaseNetworkConnector connector, Throwable error, long connectionVersion)
 	{
 		final Throwable rootCause = ErrorAnalyser.findRootCause(error);
 		if (rootCause instanceof IOException)
 		{
 			if (getNumberOfTries() == 0)
 				return rootCause;
 			onIOFailure(connectionVersion);
 		}
 		return rootCause;
 	}
 
 	private T doDecode(DataInputStream in) throws IOException
 	{
 		synchronized (in)
 		{
 			return decode(in);
 		}
 	}
 
 	public void doEncode(T message, DataOutputStream out) throws IOException
 	{
 		synchronized (out)
 		{
 			encode(message, out);
 		}
 	}
 
 	public void sendMessage(final T message) throws Throwable
 	{
 		final BaseNetworkConnector connector = getConnector();
 		long connectionVersion = connector.getConnectionVersion();
 		try
 		{
 			DataOutputStream out = connector.getOutput();
 			doEncode(message, out);
 		}
 		catch (Throwable error)
 		{
 			Throwable rootCause = resetConnection(connector, error, connectionVersion);
 			onError(rootCause);
 			throw rootCause;
 		}
 	}
 
 	public final void start() throws Throwable
 	{
 		exec.execute(reader);
 	}
 
 	public final void stop()
 	{
 		closed.set(true);
 
 		getConnector().close();
 		try
 		{
 			exec.shutdown();
 			shed_exec.shutdown();
 		}
 		catch (Throwable e)
 		{
 			// ignore
 		}
 	}
 }
