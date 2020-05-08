 package net.cheney.motown.protocol.http.threaded;
 
 import java.io.IOException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.Set;
 import java.util.concurrent.Executor;
 
 public class HttpListener {
 
 	private final Executor executor;
 	private final Selector selector;
 
 	private HttpListener(Executor executor) throws IOException {
 		this.executor = executor;
 		this.selector = SelectorProvider.provider().openSelector();
 	}
 
 	public static HttpListener create(Executor executor) throws IOException {
 		return new HttpListener(executor).submit();
 	}
 
 	private HttpListener submit() {
 		executor.execute(new Worker());
 		return this;
 	}
 
 	private class Worker implements Runnable {
 
 		@Override
 		public void run() {
 			try {
 				select();
 				executor.execute(this);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		private void select() throws IOException {
 			int count = selector.selectNow();
 			Set<SelectionKey> keys = selector.selectedKeys();
 			for(SelectionKey key : keys) {
 				handleSelectionKey(key);
 			}
 			keys.clear();
 		}
 
 		private void handleSelectionKey(SelectionKey key) {
 			if(key.isAcceptable()) {
 				ServerSocketChannel channel = (ServerSocketChannel) key.channel();
				channel.
 			}
 		}
 
 	}
 }
