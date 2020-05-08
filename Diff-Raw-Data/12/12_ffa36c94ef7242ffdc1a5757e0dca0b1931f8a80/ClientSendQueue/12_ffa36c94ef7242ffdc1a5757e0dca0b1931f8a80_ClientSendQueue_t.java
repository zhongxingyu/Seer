 package com.nexus.network;
 
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.nexus.NexusServer;
 import com.nexus.client.NexusClient;
 import com.nexus.interfaces.IHttpPacketHandler;
 import com.nexus.interfaces.IPacket;
 import com.nexus.interfaces.ITickHandler;
 import com.nexus.logging.NexusLog;
 import com.nexus.main.HTTPPacket;
 import com.nexus.network.exception.PacketConstructionErrorException;
 import com.nexus.network.handlers.INetworkHandler;
 import com.nexus.network.handlers.NetworkHandlerHTTP;
 import com.nexus.network.handlers.NetworkHandlerServerEvent;
 import com.nexus.network.packets.Packet;
 import com.nexus.network.packets.Packet1RedirectedPacket;
 import com.nexus.network.packets.Packet8MultiPacket;
import com.nexus.network.packets.Packet9AdminLogLine;
 import com.nexus.webserver.WebServerSession;
 import com.nexus.webserver.WebServerStatus;
 
 public class ClientSendQueue implements IHttpPacketHandler, ITickHandler{
 
 	public final NexusClient Owner;
 
 	public final ArrayList<IPacket> SendQueue = new ArrayList<IPacket>();
 	
 	private INetworkHandler NetworkHandler;
 	
 	private WebServerSession ServerEventSession;
 	
 	private static Logger Log = NexusLog.MakeLogger("SendQueue");
 
 	public ClientSendQueue(ProtocolType Protocol, NexusClient Owner){
 		this.Owner = Owner;
 		this.NetworkHandler = Protocol.GetHandlerInstance(this);
 		
 		NexusServer.Instance.Timer.RegisterTickHandler(this);
 	}
 
 	@Override
 	public void OnTick(){
 		if(this.SendQueue.isEmpty()) return;
 		
 		boolean UseMultiPackets = this.NetworkHandler.SupportsMultiPackets() && this.SendQueue.size() > 1;
 		
 		if(UseMultiPackets){
 			Packet8MultiPacket MultiPacket = new Packet8MultiPacket();
 			for(int i = 0; i < this.SendQueue.size(); i++){
 				IPacket p = this.SendQueue.get(i);
 				if(p instanceof Packet8MultiPacket){
 					this.SendPacket(p);
 				}else{
 					MultiPacket.Packets.add(Packet.GetJSONPacket(p));
 				}
 			}
 			this.SendPacket(MultiPacket);
 			this.SendQueue.clear();
 		}else{
 			for(int i = 0; i < this.SendQueue.size(); i++){
 				this.SendPacket(this.SendQueue.get(i));
 			}
 		}
 	}
 	
 	private void SendPacket(IPacket p){
 		try{
 			this.NetworkHandler.SendPacket(p);
			if(this.SendQueue.contains(p)){
				this.SendQueue.remove(this.SendQueue.indexOf(p));
			}
 		}catch(PacketConstructionErrorException e){
 			Log.log(Level.SEVERE, "Error while sending packet", e);
 		}catch(Exception e){
 			//Maybe handle failed connections a little better later on
 			Log.log(Level.SEVERE, "Client " + this.Owner.GetClientID() + " got an connection error!", e);
 			this.NetworkHandler.Close();
 			this.NetworkHandler = ProtocolType.FETCH.GetHandlerInstance(this);
 			Logger.getLogger("ClientManager").info("The network handler for client " + this.Owner.GetClientID() + " was changed to " + this.NetworkHandler.toString() + " due to a connection error!");
 		}
 	}
 
 	public void addToSendQueue(IPacket IncomingPacket){
 		if(Owner.RedirectAllPackages){
 			Owner.RedirectedPacketsDestination.SendQueue.addToSendQueue(new Packet1RedirectedPacket(Owner.GetClientTypeName(), IncomingPacket));
 			return;
 		}
		if(!(IncomingPacket instanceof Packet9AdminLogLine || IncomingPacket instanceof Packet1RedirectedPacket)) Log.finer("Sending " + IncomingPacket.toString());
 		this.SendQueue.add(IncomingPacket);
 	}
 
 	@Override
 	public void OnDataReceived(HTTPPacket Packet) throws Exception{
 		if(!Packet.Internal){
 			Packet.Response.SendHeaders(WebServerStatus.Forbidden);
 			Packet.Response.SendError("This SendQueue does not belong to you!");
 			Packet.Response.Close();
 		}
 		if(Packet.Data.split("/")[3].equalsIgnoreCase("SetProtocol")){
 			if(Packet.Request.GetParameter("type").equalsIgnoreCase("ServerEvent")){
 				if(this.ServerEventSession == null){
 					Packet.Response.SendHeaders(WebServerStatus.BadRequest);
 					Packet.Response.SendError("First subscribe to a ServerEvent service, then you can change your protocol");
 					Packet.Response.Close();
 					return;
 				}
 			}
 			if(this.CreateNetworkHandler(Packet)){
 				Packet.Response.SendHeaders(WebServerStatus.OK);
 				Packet.Response.SendError("none");
 				Packet.Response.Close();
 				Logger.getLogger("ClientManager").info("Client with id " + this.Owner.GetClientID() + " changed network handler to " + this.NetworkHandler.toString());
 			}else{
 				Packet.Response.SendHeaders(WebServerStatus.BadRequest);
 				Packet.Response.SendError("Unknown protocol type");
 				Packet.Response.Close();
 			}
 		}else if(Packet.Data.split("/")[3].equalsIgnoreCase("ServerEvent")){
 			Packet.Response.SetHeader("Content-Type", "text/event-stream");
			Packet.Response.SetHeader("Cache-Control", "no-cache");
			Packet.Response.SetHeader("Connection", "keep-alive");
 			Packet.Response.SendHeaders(WebServerStatus.OK);
 			this.ServerEventSession = Packet.Response.GetSession();
 		}else if(Packet.Data.split("/")[3].equalsIgnoreCase("Close")){
 			Packet.Response.SendHeaders(WebServerStatus.OK);
 			Packet.Response.SendError("none");
 			Packet.Response.Close();
 			this.Owner.Close();
 		}else if(this.NetworkHandler instanceof IHttpPacketHandler){
 			((IHttpPacketHandler) this.NetworkHandler).OnDataReceived(Packet);
 		}
 	}
 	
 	public void Close(){
 		this.NetworkHandler.Close();
 	}
 
 	private boolean CreateNetworkHandler(HTTPPacket p){
 		INetworkHandler NewHandler = null;
 		if(ProtocolType.valueOf(p.Request.GetParameter("type")) != null){
 			NewHandler = ProtocolType.valueOf(p.Request.GetParameter("type")).GetHandlerInstance(this);
 			if(NewHandler == null){
 				return false;
 			}
 			if(NewHandler instanceof NetworkHandlerHTTP){
 				HashMap<String, Object> InjectedData = new HashMap<String, Object>();
 				InjectedData.put("path", "http://" + p.Request.Address + ":" + p.Data.split("/")[3].toLowerCase() + "/");
 				NewHandler.InjectData(InjectedData);
 			}else if(NewHandler instanceof NetworkHandlerServerEvent){
 				if(this.ServerEventSession == null){
 					throw new ConcurrentModificationException("ServerEventSession is null");
 				}
 				HashMap<String, Object> InjectedData = new HashMap<String, Object>();
 				InjectedData.put("session", this.ServerEventSession);
 				NewHandler.InjectData(InjectedData);
 				this.ServerEventSession = null;
 			}
 			this.NetworkHandler.Close();
 			this.NetworkHandler = NewHandler;
 		}
 		return true;
 	}
 }
