 import java.io.*;
 import java.awt.*;
 import java.net.*;
 import java.awt.event.*;
 import java.util.Vector;
 
 import javax.media.*;
 import javax.media.rtp.*;
 import javax.media.rtp.event.*;
 import javax.media.rtp.rtcp.*;
 import javax.media.protocol.*;
 import javax.media.protocol.DataSource;
 import javax.media.format.AudioFormat;
 import javax.media.format.VideoFormat;
 import javax.media.Format;
 import javax.media.format.FormatChangeEvent;
 import javax.media.control.BufferControl;
 
 
 /**
  * MediaReceiver to receive RTP transmission using the RTPManagers.
  */
 public class ClientReceiver implements ReceiveStreamListener, SessionListener,ControllerListener
 {
 	String mediaSessions[] = null;
 	RTPManager managers[] = null;
 	
 	Player p;
 	
 	ConferenceClient rootApplication;
 	ReceiverGUI PP;
 	
 	int mediaBufferSize;
 	
 	boolean sourceFound = false;
 	Object dataSync = new Object();
 	
 	DataSource[] sources;
 	int receivedEventsSoFar;
 
 	public ClientReceiver (String sessions[], ConferenceClient root, int allocatedBufferSize)
 	{
 		mediaSessions = sessions;
 		rootApplication = root;
 		mediaBufferSize = allocatedBufferSize;
 		managers = new RTPManager[sessions.length];
 		
 		for (int i = 0; i < sessions.length; i++)
 		{
 			managers[i] = null;
 		}
 	}
 	
 	protected boolean initalize ()
 	{
 		try
 		{
 			InetAddress addr;
 			SessionAddress localAddr = new SessionAddress();
 			SessionAddress destAddr;
 			
 			managers = new RTPManager[mediaSessions.length];
			sources = new DataSource[mediaSessions.length];
 			receivedEventsSoFar = 0;
 			
 			SessionLabel session;
 			
 			for (int i = 0; i < mediaSessions.length; i++)
 			{
 				try
 				{
 					session = new SessionLabel(mediaSessions[i]);
 				}
 				catch (IllegalArgumentException e)
 				{
 					System.out.println("Totally failed at parsing the session address: " + mediaSessions[i]);
 					return false;
 				}
 				
 				System.err.println(" - Open RTP session for::: addr: " + session.addr + " port: " + session.port + " ttl: " + session.ttl);
 
 				managers[i] = (RTPManager) RTPManager.newInstance();
 				managers[i].addSessionListener(this);
 				managers[i].addReceiveStreamListener(this);
 				
 				addr = InetAddress.getByName(session.addr);
 				
 				if (addr.isMulticastAddress())
 				{
 					localAddr = new SessionAddress(addr, session.port, session.ttl);
 					destAddr = new SessionAddress(addr, session.port, session.ttl);
 				}
 				else
 				{
 					localAddr = new SessionAddress(InetAddress.getLocalHost(), session.port);
 					destAddr = new SessionAddress(addr, session.port);
 				}
 				
 				managers[i].initialize(localAddr);
 				
 				BufferControl bc = (BufferControl)managers[i].getControl("javax.media.control.BufferControl");
 				if (bc != null)
 				{
 					bc.setBufferLength(this.mediaBufferSize);
 				}
 				
 				managers[i].addTarget(destAddr);
 			}
 		}
 		catch (Exception e)
 		{
 			System.out.println("Cannot create an RTP session: " + e.getMessage());
 			return false;
 		}
 
 		System.out.println("...waiting for RTP data to be received...");
 		
 		return true;
 	}
 	
 	public boolean isFinished()
 	{
 		return this.receivedEventsSoFar == mediaSessions.length;
 	}
 	
 	protected void close ()
 	{
 		synchronized(this)
 		{
 			if (PP != null)
 			{
 				rootApplication.basePanel.remove(PP);
 				PP = null;
 			}
 			
 			if (p != null)
 			{
 				p.stop();
 				//p.collaborate();
 				//p.listen();
 				
 				p.close();
 				p = null;
 			}
 			
 			//terminate RTP connections
 			for (int i = 0; i < this.managers.length; i++)
 			{
 				if (managers[i] != null)
 				{
 					try
 					{
 						managers[i].removeTargets("terminating RTP session");
 						managers[i].dispose();
 						managers[i] = null;
 					}
 					catch (Exception e)
 					{
 						//do nothing I guess
 					}
 				}
 			}
 
 			//GUI related stuff
 			
 			receivedEventsSoFar = 0;
 		}
 	}
 	
 	// interface update functions
 	
 	public synchronized void update (SessionEvent evt)
 	{
 		if (evt instanceof NewParticipantEvent)
 		{
 			Participant p = ((NewParticipantEvent)evt).getParticipant();
 			System.out.println(" A new participant/challenger approaches: " + p.getCNAME());
 		}
 	}
 	
 	public synchronized void update (ReceiveStreamEvent evt)
 	{
 		RTPManager mgr = (RTPManager)evt.getSource();
 		
 		//the following two declarations may evaluate to null at this point
 		Participant participant = evt.getParticipant();
 		ReceiveStream stream = evt.getReceiveStream();
 		
 		if (evt instanceof RemotePayloadChangeEvent)
 		{
 			System.out.println("Received PayloadChangeEvent (RTP)");
 			System.out.println("Sorry, cannot handle the change in payload");
 			System.exit(0);
 		}
 		else if (evt instanceof NewReceiveStreamEvent)
 		{
 			try
 			{
 				stream = ((NewReceiveStreamEvent)evt).getReceiveStream();
 				sources[receivedEventsSoFar] = stream.getDataSource();
 
 				RTPControl ctl = (RTPControl)sources[receivedEventsSoFar].getControl("javax.media.rtp.RTPControl");
 				if (ctl != null)
 				{
 					System.out.println(" - Recevied new RTP stream: " + ctl.getFormat());
 				}
 				else
 				{
 					System.out.println(" - Recevied new RTP stream");
 				}
 				
 				if (participant == null)
 				{
 					System.out.println("The RTP stream sender hasn't been identified.");
 				}
 				else
 				{
 					System.out.println("The name of the RTP stream sender is: " + participant.getCNAME());
 				}
 				
 				if (++receivedEventsSoFar == mediaSessions.length)
 				{
 					DataSource mergedSource = Manager.createMergingDataSource(sources);
 					p = javax.media.Manager.createPlayer(mergedSource);
 					if (p == null)
 					{
 						return;
 					}

 					p.addControllerListener(this);
 					p.realize();
 				}

 				synchronized (dataSync)
 				{
 					sourceFound = true;
 					dataSync.notifyAll();
 				}
 			}
 			catch (Exception e)
 			{
				System.out.println("NewReceiveStreamEvent exception:");
				e.printStackTrace();
 				return;
 			}
 		}
 		else if (evt instanceof StreamMappedEvent)
 		{
 			if (stream != null && stream.getDataSource() != null)
 			{
 				DataSource ds = stream.getDataSource();
 				
 				RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
 				System.out.print("The previously unidentified stream: ");
 				if (ctl != null)
 				{
 					System.out.print(ctl.getFormat() + " ");
 				}
 				System.out.println("had now been identified as sent by: " + participant.getCNAME());
 			}
 		}
 		else if (evt instanceof ByeEvent)
 		{
 			System.out.println("Got a BYE signal from: " + participant.getCNAME());
 			
 			//GUI related stuff for setting buttons
 			
 			this.close();
 		}
 	}
 	
 	public synchronized void controllerUpdate(ControllerEvent ce)
 	{
 		if (ce instanceof RealizeCompleteEvent)
 		{
 			PP = new ReceiverGUI(p);
 			
 			rootApplication.basePanel.add("Center", PP);
 			rootApplication.validate();
 
 			p.start();
 		}
 		
 		if (ce instanceof ControllerErrorEvent)
 		{
 			p.removeControllerListener(this);
 			this.close();
 			System.out.println("INTERNAL ERROR::: " + ce);
 		}
 	}
 }
 
 class ReceiverGUI extends Panel
 {
 	Component vc, cc;
 	
 	ReceiverGUI (Player pl)
 	{
 		setLayout(new BorderLayout());
 		if ((vc = pl.getVisualComponent()) != null)
 		{
 			add("Center", vc);
 		}
 	}
 
 	public Dimension getPreferredSize()
 	{
 		int w = 0;
 		int h = 0;
 		
 		if (vc != null)
 		{
 			Dimension size = vc.getPreferredSize();
 			w = size.width;
 			h = size.height;
 		}
 		
 		if (cc != null)
 		{
 			Dimension size = cc.getPreferredSize();
 			if (w == 0)
 			{
 				w = size.width;
 			}
 			h += size.height;
 		}
 		
 		if (w < 160)
 		{
 			w = 160;
 		}
 		
 		return new Dimension(w, h);
 	}
 }
 
 
 /// THE NEXT CLASS WAS NOT WRITTEN BY DANIEL SAVAGE
 /// IT WAS COPIED FROM THE EXAMPLE SOLUTION FOR
 /// ASSIGNMENT 2 PROVIDED BY THE LAB INSTRUCTOR
 ///
 /// IT'S PURPOSE IS ALMOST ENTIRELY FOR STRING PARSING,
 /// AND WAS CONSIDERED BY THE STUDENT TO BE TRIVIAL IN NATURE
 
     /**
      * A utility class to parse the session addresses.
      */
     class SessionLabel {
 
 	public String addr = null;
 	public int port;
 	public int ttl = 1;
 
 	SessionLabel(String session) throws IllegalArgumentException {
 
 	    int off;
 	    String portStr = null, ttlStr = null;
 
 	    if (session != null && session.length() > 0) {
 		while (session.length() > 1 && session.charAt(0) == '/')
 		    session = session.substring(1);
 
 		// Now see if there's a addr specified.
 		off = session.indexOf('/');
 		if (off == -1) {
 		    if (!session.equals(""))
 			addr = session;
 		} else {
 		    addr = session.substring(0, off);
 		    session = session.substring(off + 1);
 		    // Now see if there's a port specified
 		    off = session.indexOf('/');
 		    if (off == -1) {
 			if (!session.equals(""))
 			    portStr = session;
 		    } else {
 			portStr = session.substring(0, off);
 			session = session.substring(off + 1);
 			// Now see if there's a ttl specified
 			off = session.indexOf('/');
 			if (off == -1) {
 			    if (!session.equals(""))
 				ttlStr = session;
 			} else {
 			    ttlStr = session.substring(0, off);
 			}
 		    }
 		}
 	    }
 
 	    if (addr == null)
 		throw new IllegalArgumentException();
 
 	    if (portStr != null) {
 		try {
 		    Integer integer = Integer.valueOf(portStr);
 		    if (integer != null)
 			port = integer.intValue();
 		} catch (Throwable t) {
 		    throw new IllegalArgumentException();
 		}
 	    } else
 		throw new IllegalArgumentException();
 
 	    if (ttlStr != null) {
 		try {
 		    Integer integer = Integer.valueOf(ttlStr);
 		    if (integer != null)
 			ttl = integer.intValue();
 		} catch (Throwable t) {
 		    throw new IllegalArgumentException();
 		}
 	    }
 	}
     }
     
 /// COPIED CLASS ENDS HERE
