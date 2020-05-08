 package edu.teco.dnd.network.tcp.tests;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.util.Attribute;
 import io.netty.util.AttributeKey;
 import io.netty.util.concurrent.Future;
 import io.netty.util.concurrent.GenericFutureListener;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import edu.teco.dnd.network.ChannelFutureNotifier;
 
 public class MockChannel {
 	@Mock
 	private Channel channel;
 
 	@Mock
 	private ChannelFutureNotifier channelFutureNotifier;
 
 	@Mock
 	private ChannelFuture closeFuture;
 
 	private ChannelFutureListenerHandler closeListenerHandler;
 	
 	private Map<AttributeKey<?>, Attribute<?>> attributes = new HashMap<AttributeKey<?>, Attribute<?>>();
 
 	@SuppressWarnings("unchecked")
 	public MockChannel() {
 		MockitoAnnotations.initMocks(this);
 		when(channelFutureNotifier.channel()).thenReturn(channel);
 		when(channel.close()).thenReturn(closeFuture);
 		when(channel.closeFuture()).thenReturn(closeFuture);
 		when(closeFuture.channel()).thenReturn(channel);
 		closeListenerHandler = new ChannelFutureListenerHandler(closeFuture);
 		when(channel.attr(any(AttributeKey.class))).then(new AttributeAnswer());
 	}
 
 	public Channel getChannel() {
 		return channel;
 	}
 
 	public ChannelFutureNotifier getChannelFutureNotifier() {
 		return channelFutureNotifier;
 	}
 
 	public void close() throws Exception {
 		closeFuture();
 		notifyCloseFutureListeners();
 	}
 
 	private void closeFuture() {
 		when(closeFuture.isSuccess()).thenReturn(true);
 		when(closeFuture.isDone()).thenReturn(true);
 	}
 
 	private void notifyCloseFutureListeners() throws Exception {
 		for (final GenericFutureListener<Future<Void>> listener : closeListenerHandler.getListeners()) {
 			listener.operationComplete(closeFuture);
 		}
 	}
 
 	public <T> void setAttribute(final AttributeKey<T> key, T value) {
 		when(attr(key).get()).thenReturn(value);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> Attribute<T> attr(final AttributeKey<T> key) {
 		if (!attributes.containsKey(key)) {
			attributes.put(key, mock(Attribute.class));
 		}
 		return (Attribute<T>) attributes.get(key);
 	}
 
 	private class AttributeAnswer implements Answer<Attribute<?>> {
 		@Override
 		public Attribute<?> answer(final InvocationOnMock invocation) throws Throwable {
 			return attr((AttributeKey<?>) invocation.getArguments()[0]);
 		}
 	}
 }
