 package edu.teco.dnd.network.tcp.tests;
 
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelHandler;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelPipeline;
 import io.netty.channel.ChannelPromise;
 import io.netty.util.concurrent.EventExecutorGroup;
 
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 
 /**
  * Provides a Mock implementation of ChannelPipeline. Stores ChannelHandlers with their name and EventExecutorGroup, but
  * does nothing else. All methods not related to storing or retrieving ChannelHandlers throw an
  * UnsupportedOperationException.
  * 
  * @author Philipp Adolf
  */
 public class MockPipeline implements ChannelPipeline {
 	private final LinkedList<PipelineEntry> channelHandlers = new LinkedList<PipelineEntry>();
 
 	private int generatedNameIndex = 0;
 
 	@Override
 	public ChannelPipeline addFirst(final ChannelHandler... handlers) {
 		return addFirst(null, handlers);
 	}
 
 	@Override
 	public ChannelPipeline addFirst(final EventExecutorGroup group, final ChannelHandler... handlers) {
 		checkNotNull((Object) handlers);
 		// iterating over the array backwards so that the order is preserved
 		for (int i = handlers.length - 1; i > 0; i--) {
 			if (handlers[i] != null) {
 				addFirst(group, generateName(), handlers[i]);
 			}
 		}
 		return this;
 	}
 
 	@Override
 	public ChannelPipeline addFirst(final String name, final ChannelHandler handler) {
 		return addFirst(null, name, handler);
 	}
 
 	@Override
 	public ChannelPipeline addFirst(final EventExecutorGroup group, final String name, final ChannelHandler handler) {
 		final PipelineEntry newPipelineEntry = new PipelineEntry(handler, name);
 		addFirst(newPipelineEntry);
 		setExecutorGroup(newPipelineEntry, group);
 		return this;
 	}
 
 	private void addFirst(final PipelineEntry pipelineEntry) {
 		assert pipelineEntry != null;
 		checkNotNull(pipelineEntry.handler, pipelineEntry.name);
 		checkNameMissingFromPipeline(pipelineEntry.name);
 		channelHandlers.addFirst(pipelineEntry);
 	}
 
 	@Override
 	public ChannelPipeline addLast(final ChannelHandler... handlers) {
 		return addLast(null, handlers);
 	}
 
 	@Override
 	public ChannelPipeline addLast(final EventExecutorGroup group, final ChannelHandler... handlers) {
 		checkNotNull((Object) handlers);
 		for (final ChannelHandler handler : handlers) {
 			addLast(group, generateName(), handler);
 		}
 		return this;
 	}
 
 	@Override
 	public ChannelPipeline addLast(final String name, final ChannelHandler handler) {
 		return addLast(null, name, handler);
 	}
 
 	@Override
 	public ChannelPipeline addLast(final EventExecutorGroup group, final String name, final ChannelHandler handler) {
 		final PipelineEntry newPipelineEntry = new PipelineEntry(handler, name);
 		addLast(newPipelineEntry);
 		setExecutorGroup(newPipelineEntry, group);
 		return this;
 	}
 
 	private void addLast(final PipelineEntry pipelineEntry) {
 		assert pipelineEntry != null;
 		checkNotNull(pipelineEntry.handler, pipelineEntry.name);
 		checkNameMissingFromPipeline(pipelineEntry.name);
 		channelHandlers.addLast(pipelineEntry);
 	}
 
 	@Override
 	public ChannelPipeline addBefore(final String baseName, final String name, final ChannelHandler handler) {
 		return addBefore(null, baseName, name, handler);
 	}
 
 	@Override
 	public ChannelPipeline addBefore(final EventExecutorGroup group, final String baseName, final String name,
 			final ChannelHandler handler) {
 		final int index = getIndexOf(baseName);
 		final PipelineEntry newPipelineEntry = new PipelineEntry(handler, name);
 		addBefore(index, newPipelineEntry);
 		setExecutorGroup(newPipelineEntry, group);
 		return this;
 	}
 
 	private void addBefore(final int index, final PipelineEntry pipelineEntry) {
 		assert pipelineEntry != null;
 		assert index >= 0;
 		assert index <= channelHandlers.size();
 		checkNotNull(pipelineEntry.handler, pipelineEntry.name);
 		checkNameMissingFromPipeline(pipelineEntry.name);
 		channelHandlers.add(index, pipelineEntry);
 	}
 
 	@Override
 	public ChannelPipeline addAfter(final String baseName, final String name, final ChannelHandler handler) {
 		return addAfter(null, baseName, name, handler);
 	}
 
 	@Override
 	public ChannelPipeline addAfter(final EventExecutorGroup group, final String baseName, final String name,
 			final ChannelHandler handler) {
 		final int index = getIndexOf(name);
 		final PipelineEntry newPipelineEntry = new PipelineEntry(handler, name);
 		addAfter(index, newPipelineEntry);
 		setExecutorGroup(newPipelineEntry, group);
 		return this;
 	}
 
 	private String generateName() {
 		return "handler " + (generatedNameIndex++);
 	}
 
 	private void addAfter(final int index, final PipelineEntry pipelineEntry) {
 		assert pipelineEntry != null;
 		assert index >= 0;
 		assert index <= channelHandlers.size();
 		checkNotNull(pipelineEntry.handler, pipelineEntry.name);
 		checkNameMissingFromPipeline(pipelineEntry.name);
 		channelHandlers.add(index + 1, pipelineEntry);
 	}
 
 	private static void checkNotNull(final Object... args) {
 		for (final Object arg : args) {
 			if (arg == null) {
 				throw new NullPointerException();
 			}
 		}
 	}
 
 	private void checkNameMissingFromPipeline(final String name) {
 		for (final PipelineEntry existingPipelineEntry : channelHandlers) {
 			if (existingPipelineEntry.name.equals(name)) {
 				throw new IllegalArgumentException(String.format("name '%s' already exists in pipeline", name));
 			}
 		}
 	}
 
 	private void setExecutorGroup(final PipelineEntry pipelineEntry, final EventExecutorGroup group) {
 		assert pipelineEntry != null;
 		pipelineEntry.group = group;
 	}
 
 	@Override
 	public ChannelPipeline remove(final ChannelHandler handler) {
 		final int index = getIndexOf(handler);
 		remove(index);
 		return this;
 	}
 
 	@Override
 	public ChannelHandler remove(final String name) {
 		final int index = getIndexOf(name);
 		return remove(index);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends ChannelHandler> T remove(final Class<T> handlerType) {
 		final int index = getIndexOf(handlerType);
 		return (T) remove(index);
 	}
 
 	@Override
 	public ChannelHandler removeFirst() {
 		return remove(0);
 	}
 
 	@Override
 	public ChannelHandler removeLast() {
 		return remove(channelHandlers.size() - 1);
 	}
 
 	private ChannelHandler remove(final int index) {
 		assert index >= 0;
 		assert index < channelHandlers.size();
 		final PipelineEntry removedEntry = channelHandlers.remove(index);
 		return removedEntry.handler;
 	}
 
 	@Override
 	public ChannelPipeline replace(final ChannelHandler oldHandler, final String newName,
 			final ChannelHandler newHandler) {
 		final int index = getIndexOf(oldHandler);
 		replace(index, new PipelineEntry(newHandler, newName));
 		return this;
 	}
 
 	@Override
 	public ChannelHandler replace(final String oldName, final String newName, final ChannelHandler newHandler) {
 		final int index = getIndexOf(oldName);
 		return replace(index, new PipelineEntry(newHandler, newName));
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends ChannelHandler> T replace(final Class<T> oldHandlerType, final String newName,
 			final ChannelHandler newHandler) {
 		final int index = getIndexOf(oldHandlerType);
 		return (T) replace(index, new PipelineEntry(newHandler, newName));
 	}
 
 	private ChannelHandler replace(final int index, final PipelineEntry newPipelineEntry) {
 		assert newPipelineEntry != null;
 		assert index >= 0;
 		assert index < channelHandlers.size();
 		checkNotNull(newPipelineEntry.handler, newPipelineEntry.name);
 		final PipelineEntry oldPipelineEntry = channelHandlers.remove(index);
 		checkNameMissingFromPipeline(newPipelineEntry.name);
 		channelHandlers.add(index, newPipelineEntry);
 		return oldPipelineEntry.handler;
 	}
 
 	@Override
 	public ChannelHandler first() {
 		if (channelHandlers.isEmpty()) {
 			return null;
 		}
 		return channelHandlers.get(0).handler;
 	}
 
 	@Override
 	public ChannelHandler last() {
 		if (channelHandlers.isEmpty()) {
 			return null;
 		}
 		return channelHandlers.peekLast().handler;
 	}
 
 	@Override
 	public ChannelHandler get(final String name) {
 		ChannelHandler handler = null;
 		try {
 			handler = channelHandlers.get(getIndexOf(name)).handler;
 		} catch (final NoSuchElementException e) {
 		}
 		return handler;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends ChannelHandler> T get(final Class<T> handlerType) {
 		T handler = null;
 		try {
 			handler = (T) channelHandlers.get(getIndexOf(handlerType)).handler;
 		} catch (final NoSuchElementException e) {
 		}
 		return handler;
 	}
 
 	public EventExecutorGroup getGroup(final ChannelHandler handler) {
 		EventExecutorGroup group = null;
 		try {
 			group = channelHandlers.get(getIndexOf(handler)).group;
 		} catch (final NoSuchElementException e) {
 		}
 		return group;
 	}
 
 	public EventExecutorGroup getGroup(final String name) {
 		EventExecutorGroup group = null;
 		try {
 			group = channelHandlers.get(getIndexOf(name)).group;
 		} catch (final NoSuchElementException e) {
 		}
 		return group;
 	}
 
 	public <T extends ChannelHandler> EventExecutorGroup getGroup(final Class<T> handlerType) {
 		EventExecutorGroup group = null;
 		try {
 			group = channelHandlers.get(getIndexOf(handlerType)).group;
 		} catch (final NoSuchElementException e) {
 		}
 		return group;
 	}
 
 	private int getIndexOf(final String name) {
 		checkNotNull(name);
 		int index = 0;
 		final Iterator<PipelineEntry> iterator = channelHandlers.iterator();
 		while (iterator.hasNext()) {
 			final PipelineEntry pipelineEntry = iterator.next();
 			if (name.equals(pipelineEntry.name)) {
 				return index;
 			}
 			index++;
 		}
 		throw new NoSuchElementException(String.format("no ChannelHandler with name '%s' in this pipeline", name));
 	}
 
 	private int getIndexOf(final ChannelHandler handler) {
 		checkNotNull(handler);
 		int index = 0;
 		final Iterator<PipelineEntry> iterator = channelHandlers.iterator();
 		while (iterator.hasNext()) {
 			final PipelineEntry pipelineEntry = iterator.next();
 			if (handler.equals(pipelineEntry.handler)) {
 				return index;
 			}
 			index++;
 		}
 		throw new NoSuchElementException(String.format("ChannelHandler '%s' is not in this pipeline", handler));
 	}
 
 	private <T extends ChannelHandler> int getIndexOf(final Class<T> handlerType) {
 		checkNotNull(handlerType);
 		int index = 0;
 		final Iterator<PipelineEntry> iterator = channelHandlers.iterator();
 		while (iterator.hasNext()) {
 			final PipelineEntry pipelineEntry = iterator.next();
 			if (handlerType.isAssignableFrom(pipelineEntry.handler.getClass())) {
 				return index;
 			}
 		}
 		throw new NoSuchElementException(String.format("no ChannelHandler of class '%s' in this pipeline", handlerType));
 	}
 	
 	public List<ChannelHandler> handler() {
 		final List<ChannelHandler> handlers = new ArrayList<ChannelHandler>();
 		for (final PipelineEntry entry : channelHandlers) {
 			handlers.add(entry.handler);
 		}
 		return Collections.unmodifiableList(handlers);
 	}
 
 	@Override
 	public List<String> names() {
 		final List<String> names = new ArrayList<String>();
 		for (final PipelineEntry entry : channelHandlers) {
 			names.add(entry.name);
 		}
 		return Collections.unmodifiableList(names);
 	}
 
 	@Override
 	public Map<String, ChannelHandler> toMap() {
 		final Map<String, ChannelHandler> map = new HashMap<String, ChannelHandler>();
 		for (final PipelineEntry entry : channelHandlers) {
 			map.put(entry.name, entry.handler);
 		}
 		return Collections.unmodifiableMap(map);
 	}
 
 	@Override
 	public Iterator<Entry<String, ChannelHandler>> iterator() {
 		final Iterator<PipelineEntry> realIterator = channelHandlers.iterator();
 		return new Iterator<Entry<String, ChannelHandler>>() {
 			@Override
 			public boolean hasNext() {
 				return realIterator.hasNext();
 			}
 
 			@Override
 			public Entry<String, ChannelHandler> next() {
 				return realIterator.next();
 			}
 
 			@Override
 			public void remove() {
 				realIterator.next();
 			}
 		};
 	}
 
 	private static class PipelineEntry implements Entry<String, ChannelHandler> {
 		public final ChannelHandler handler;
 		public final String name;
 		public EventExecutorGroup group = null;
 
 		public PipelineEntry(final ChannelHandler handler, final String name) {
 			this.handler = handler;
 			this.name = name;
 		}
 
 		@Override
 		public String getKey() {
 			return name;
 		}
 
 		@Override
 		public ChannelHandler getValue() {
 			return handler;
 		}
 
 		@Override
 		public ChannelHandler setValue(ChannelHandler value) {
 			throw new UnsupportedOperationException("PipelineEntry is immutable");
 		}
 
 		@Override
 		public boolean equals(final Object other) {
 			if (this == other) {
 				return true;
 			}
 			if (!(other instanceof Entry)) {
 				return false;
 			}
 			return getKey().equals(((Entry<?, ?>) other).getKey())
 					&& getValue().equals(((Entry<?, ?>) other).getValue());
 		}
 
 		@Override
 		public int hashCode() {
 			return getKey().hashCode() ^ getValue().hashCode();
 		}
 	}
 
 	@Override
 	public ChannelHandlerContext firstContext() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelHandlerContext lastContext() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Channel channel() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture bind(SocketAddress localAddress) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture connect(SocketAddress remoteAddress) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture disconnect() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture close() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture deregister() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture disconnect(ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture close(ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture deregister(ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture write(Object msg) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture write(Object msg, ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelFuture writeAndFlush(Object msg) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelHandlerContext context(ChannelHandler handler) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelHandlerContext context(String name) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireChannelRegistered() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireChannelUnregistered() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireChannelActive() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireChannelInactive() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireExceptionCaught(Throwable cause) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireUserEventTriggered(Object event) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireChannelRead(Object msg) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireChannelReadComplete() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline fireChannelWritabilityChanged() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline flush() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ChannelPipeline read() {
 		throw new UnsupportedOperationException();
 	}
 }
