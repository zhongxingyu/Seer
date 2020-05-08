 package de.fettlaus.thekraken;
 
 import java.util.List;
 
 import com.google.common.eventbus.DeadEvent;
 import com.google.common.eventbus.Subscribe;
 
 import de.fettlaus.thekraken.events.CloseConnectionEvent;
 import de.fettlaus.thekraken.events.EventBus;
 import de.fettlaus.thekraken.events.NewConnectionEvent;
 import de.fettlaus.thekraken.events.NewNotificationEvent;
 import de.fettlaus.thekraken.events.SendMessageEvent;
 import de.fettlaus.thekraken.events.SendPingEvent;
 import de.fettlaus.thekraken.events.SynchronizeClientsEvent;
 import de.fettlaus.thekraken.model.Connection;
 import de.fettlaus.thekraken.model.KrakenMessage;
 import de.fettlaus.thekraken.model.Message;
 import de.fettlaus.thekraken.model.MessageType;
 import de.fettlaus.thekraken.model.Model;
 import de.fettlaus.thekraken.view.Messages;
 import de.fettlaus.thekraken.view.View;
 
 public class Presenter {
 	Model model;
 	View view;
 	long pingpongdiff = 0l;
 
 	public Presenter(Model model, View view) {
 		super();
 		this.model = model;
 		this.view = view;
 
 		EventBus.instance().register(this);
 	}
 
 	@Subscribe
 	public void deadHandler(DeadEvent dead) {
 		System.out.println("Missed event: " + dead.getEvent().toString());
 	}
 
 	@Subscribe
 	public void handleCloseConnection(CloseConnectionEvent evt) {
 		final Connection con = model.getConnection(evt.getConnection());
 		if (con != null) {
 			con.close();
 		}
 	}
 
 	@Subscribe
 	public void handleConnectionList(List<Connection> conlist) {
 		final int connections_size = conlist.size();
 		final String[] connections_txt = new String[connections_size];
 		for (int i = 0; i < connections_size; ++i) {
 			final Connection this_con = conlist.get(i);
 			connections_txt[i] = this_con.getAddress() + ":" + this_con.getPort();
 		}
 		view.setClients(connections_txt);
 	}
 
 	@Subscribe
 	public void handleNewConnection(NewConnectionEvent evt) {
 		int port;
 		String host;
 		try {
 			port = Integer.parseInt(evt.getPort());
 			host = evt.getAddress();
 			model.newConnection(host, port);
 		} catch (final NumberFormatException e1) {
 			// TODO localize
 			view.setNotification("Wrong Port specified");
 		}
 	}
 
 	@Subscribe
 	public void handleNewMessage(Message msg) {
 		try {
 			final String timestamp = String.valueOf(msg.getTimestamp());
 			final String connection = msg.getSourceConnection().getAddress().toString();
 			final MessageType type = msg.getType();
 			if (type == MessageType.UART) {
 				view.addUARTMessage(timestamp, connection, msg.getMessage());
 			} else if (type == MessageType.MESS) {
 				view.addLogmessage(timestamp, connection, "\"" + msg.getMessage() + "\"");
 			} else if (type == MessageType.PONG) {
 				final long diff = System.nanoTime() - pingpongdiff;
 				view.addLogmessage(timestamp, connection, "PONG (" + diff + " ns)");
 				view.addLogmessage(timestamp, connection, String.valueOf(System.nanoTime()));
 			}
 		} catch (final ClassCastException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Subscribe
 	public void handleNotifications(NewNotificationEvent evt) {
 		String text = Messages.getString("Notification." + evt.getType().toString());
 		final String msg = evt.getMessage();
 		if (msg != null) {
 			text = text + ": " + msg;
 		}
 		// TODO: handle in swing-thread
 		view.setNotification(text);
		view.addHostMessage(String.valueOf(evt.getTimestamp()), text);
 	}
 
 	@Subscribe
 	public void handleSendMessage(SendMessageEvent evt) {
 		final Message msg = new KrakenMessage(MessageType.MESS, evt.getMessage());
 		final int index = evt.getTargetIndex();
 		if (index < 0) {
 			model.broadcastMessage(msg);
 		} else {
 			model.getConnection(index).sendMessage(msg);
 		}
 	}
 
 	@Subscribe
 	public void handleSendPing(SendPingEvent evt) {
 		final Connection con = model.getConnection(evt.getConnection());
 		if (con != null) {
 			pingpongdiff = System.nanoTime();
 			con.sendMessage(new KrakenMessage(MessageType.PING));
 		}
 
 	}
 
 	@Subscribe
 	public void handleSynchronizeClients(SynchronizeClientsEvent evt) {
 		model.synchronizeClients();
 	}
 
 }
