 /* Copyright (c) 2009 Henrik Gustafsson <henrik.gustafsson@fnord.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 /**
  *
  */
 package se.fnord.xmms2.client.commands.internal;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import se.fnord.xmms2.client.Client;
 import se.fnord.xmms2.client.CommandErrorException;
 import se.fnord.xmms2.client.commands.Command;
 import se.fnord.xmms2.client.commands.CommandResponseListener;
 import se.fnord.xmms2.client.commands.SignalCommand;
 import se.fnord.xmms2.client.internal.IpcCommand;
 import se.fnord.xmms2.client.internal.IpcObject;
 import se.fnord.xmms2.client.internal.IpcSignal;
 
 public class BroadcastCommand extends AbstractCommand implements SignalCommand {
 	private static final class RealBroadcastCommand extends AbstractSimpleCommand {
 		private final IpcSignal signal;
 
 		protected RealBroadcastCommand(IpcSignal signal) {
			super(IpcObject.SIGNAL, IpcCommand.BROADCAST, signal);
 			this.signal = signal;
 		}
 
 		public IpcSignal getSignal() {
 			return signal;
 		}
 	}
 
 	/* TODO: Removal of 'dead' proxies. */
 	private static final ConcurrentMap<IpcSignal, Set<BroadcastCommand>> proxy_map = new ConcurrentHashMap<IpcSignal, Set<BroadcastCommand>>();
 
 	private static final ConcurrentMap<IpcSignal, Command> command_map = new ConcurrentHashMap<IpcSignal, Command>();
 
 	private static final CommandResponseListener handler = new CommandResponseListener() {
 		public void responseReceived(Command c) {
 			RealBroadcastCommand cc = (RealBroadcastCommand) c;
 			Object o;
 
 			try {
 				o = cc.getReply();
 			} catch (CommandErrorException e) {
 				o = e.getError();
 			}
 
 			for (BroadcastCommand proxy : proxy_map.get(cc.getSignal())) {
 				proxy.addReply(o);
 			}
 		}
 	};
 
 	private static void addProxy(IpcSignal signal, BroadcastCommand c) {
 		Set<BroadcastCommand> p = proxy_map.get(signal);
 		if (p == null) {
 			p = new HashSet<BroadcastCommand>();
 			final Set<BroadcastCommand> p2 = proxy_map.putIfAbsent(signal, p);
 			if (p2 != null)
 				p = p2;
 		}
 		p.add(c);
 	}
 
 	private static void execute(Client client, IpcSignal signal) {
 		Command c = command_map.get(signal);
 		if (c == null) {
 			c = new RealBroadcastCommand(signal);
 			final Command c2 = command_map.putIfAbsent(signal, c);
 
 			if (c2 == null) {
 				c.setHandler(handler);
 				c.execute(client);
 			}
 		}
 	}
 
 	private final IpcSignal signal;
 
 	public BroadcastCommand(IpcSignal signal) {
 		this.signal = signal;
 		addProxy(signal, this);
 	}
 
 	public void execute(Client client) {
 		execute(client, signal);
 	}
 
 	/* (non-Javadoc)
 	 * @see se.fnord.xmms2.client.commands.SignalCommand#getSignal()
 	 */
 	public IpcSignal getSignal() {
 		return signal;
 	}
 
 	public void reset() {
 		throw new UnsupportedOperationException();
 	}
 
 }
