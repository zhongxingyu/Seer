 package org.osjava.signals.natives;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.osjava.signals.Dispatcher;
 import org.osjava.signals.SignalListener;
 import org.osjava.signals.SignalListener.SignalListener1;
 import org.osjava.signals.SignalListener.SignalListener2;
 import org.osjava.signals.SignalListener.SignalListener3;
 import org.osjava.signals.SignalListener.SignalListener4;
 import org.osjava.signals.SignalListener.SignalListener5;
 import org.osjava.signals.Slot;
 import org.osjava.signals.impl.DispatcherImpl;
 import org.osjava.signals.impl.NativeSignalImpl;
 
 public class NativeAPIAdapterSignal<L extends SignalListener, T> extends NativeSignalImpl<L, T> {
 
 	private Method _invokeMethod;
 
 	private String _listenerName;
 
 	private Class<?> _adapterClass;
 
 	private NativeAPIAdapterDelegate _delegate;
 
 	private List<Method> _delegateMethods;
 
 	private Object _proxy;
 
 	public NativeAPIAdapterSignal(List<Slot<L>> bindings) {
 		super(bindings);
 	}
 
 	protected void createProxy(final String adapterName, final String listenerName,
 			final Class<?>[] parameterTypes, final NativeAPIAdapterDelegate delegate,
 			final List<String> delegateMethodNames) {
		assert adapterName != null : "Adapter name can not be null";
		assert listenerName != null : "Listener name can not be null";
		assert parameterTypes != null : "Parameter types can not be null";
		assert delegate != null : "Delegate can not be null";
		assert delegateMethodNames != null : "Delegate method name can not be null";
 
 		_delegate = delegate;
 		_listenerName = listenerName;
 
 		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
 		try {
 			_adapterClass = classLoader.loadClass(adapterName);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 
 		_delegateMethods = new ArrayList<Method>();
 		for (final String delegateMethodName : delegateMethodNames) {
 			try {
 				final Method delegateMethod =
 						_delegate.getClass().getMethod(delegateMethodName, parameterTypes);
 				_delegateMethods.add(delegateMethod);
 			} catch (SecurityException e) {
 				throw new RuntimeException(e);
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		if (_delegateMethods.size() != delegateMethodNames.size()) {
 			throw new InternalError("Invalid delegate method size");
 		}
 
 		final Class<?>[] proxyInterfaces = { _adapterClass };
 		final NativeAPIAdapterProxy adapterProxy =
 				new NativeAPIAdapterProxy(_delegate, _delegateMethods);
 		_proxy = Proxy.newProxyInstance(classLoader, proxyInterfaces, adapterProxy);
 
 		if (null == _proxy) {
 			throw new InternalError("Invalid or null proxy instance");
 		}
 	}
 
 	protected void removeProxyMethod() {
 		T target = getTarget();
 		if (null != _invokeMethod) {
 			try {
 				if (!_invokeMethod.isAccessible())
 					_invokeMethod.setAccessible(true);
 
 				Object[] arguments = { null };
 				_invokeMethod.invoke(target, arguments);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException(e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			} catch (InvocationTargetException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		_invokeMethod = null;
 	}
 
 	protected void registerProxyMethod() {
 		if (null != _adapterClass && null != _proxy) {
 			T target = getTarget();
 			if (null != target) {
 				try {
 					final Method[] methods = target.getClass().getMethods();
 
 					for (final Method method : methods) {
 						if (method.getName().equals(_listenerName)) {
 							_invokeMethod = method;
 
 							if (!_invokeMethod.isAccessible())
 								_invokeMethod.setAccessible(true);
 
 							Object[] arguments = { _proxy };
 							_invokeMethod.invoke(target, arguments);
 							break;
 						}
 					}
 				} catch (SecurityException e) {
 					throw new RuntimeException(e);
 				} catch (IllegalArgumentException e) {
 					throw new RuntimeException(e);
 				} catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				} catch (InvocationTargetException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @return True if the native signal is available
 	 */
 	@Override
 	public boolean isAvailable() {
 		return null != _adapterClass && null != _proxy && null != _invokeMethod;
 	}
 
 	/**
 	 * Private target listener class which implements the
 	 */
 	private final class NativeAPIAdapterProxy implements InvocationHandler {
 
 		private final Method toStringMethod;
 
 		{
 			try {
 				toStringMethod = Object.class.getMethod("toString");
 			} catch (NoSuchMethodException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		private final NativeAPIAdapterDelegate _delegate;
 
 		private final List<Method> _delegateMethods;
 
 		public NativeAPIAdapterProxy(final NativeAPIAdapterDelegate delegate,
 				final List<Method> delegateMethods) {
			assert delegate != null : "Delegate can not be null";
			assert delegateMethods != null : "Delegate methods can not be null";
 
 			_delegate = delegate;
 			_delegateMethods = delegateMethods;
 		}
 
 		@Override
 		public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
 
 			final Class<?> declaringClass = method.getDeclaringClass();
 			if (declaringClass == Object.class) {
 				if (method.equals(toStringMethod)) {
 					return proxyToString(proxy);
 				} else {
 					throw new InternalError("unexpected Object method dispatched: " + method);
 				}
 			} else {
 
 				for (final Method delegateMethod : _delegateMethods) {
 					final String delegateMethodName = delegateMethod.getName();
 					if (method.getName().equals(delegateMethodName)) {
 						try {
 							if (!delegateMethod.isAccessible())
 								delegateMethod.setAccessible(true);
 
 							return delegateMethod.invoke(_delegate, arguments);
 						} catch (InvocationTargetException e) {
 							throw e.getTargetException();
 						}
 					}
 				}
 
 				throw new InternalError("Unexpected method dispatched: " + method);
 			}
 		}
 
 		protected String proxyToString(Object proxy) {
 			return proxy.getClass().getName();
 		}
 	}
 
 	public static interface NativeAPIAdapterDelegate {
 
 	}
 
 	public static class NativeAPIAdapterSignal1<A> implements NativeSignal1<A> {
 
 		private final List<Slot<SignalListener1<A>>> _bindings =
 				new CopyOnWriteArrayList<Slot<SignalListener1<A>>>();
 
 		private final Dispatcher<SignalListener1<A>> _dispatcher = DispatcherImpl
 				.newInstance(_bindings);
 
 		private final NativeAPIAdapterSignal<SignalListener1<A>, A> _signal =
 				new NativeAPIAdapterSignal<SignalListener1<A>, A>(_bindings);
 
 		/**
 		 * Private constructor
 		 */
 		protected NativeAPIAdapterSignal1() {
 		}
 
 		/**
 		 * Create a newInstance of NativeSignal1
 		 * 
 		 * @return {@link NativeSignal1}
 		 */
 		public static <A> NativeAPIAdapterSignal1<A> newInstance() {
 			return new NativeAPIAdapterSignal1<A>();
 		}
 
 		protected void createProxy(final String adapterName, final String listenerName,
 				final Class<?>[] parameterTypes, final NativeAPIAdapterDelegate delegate,
 				final List<String> delegateMethodNames) {
 			_signal.createProxy(adapterName, listenerName, parameterTypes, delegate,
 					delegateMethodNames);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener1<A>> add(SignalListener1<A> listener) {
 			return _signal.add(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener1<A>> addOnce(SignalListener1<A> listener) {
 			return _signal.addOnce(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener1<A>> remove(SignalListener1<A> listener) {
 			return _signal.remove(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void removeAll() {
 			_signal.removeAll();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public int getNumListeners() {
 			return _signal.getNumListeners();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @throws IllegalAccessError
 		 *             if target is null when the dispatch method is called.
 		 */
 		public void dispatch(A value0) throws Throwable {
 			if (null == _signal.getTarget())
 				throw new IllegalAccessError("Target can not be null");
 
 			_dispatcher.dispatch(value0);
 		}
 
 		@Override
 		public A getTarget() {
 			return _signal.getTarget();
 		}
 
 		@Override
 		public void setTarget(A target) {
 			_signal.removeProxyMethod();
 			_signal.setTarget(target);
 			_signal.registerProxyMethod();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean isAvailable() {
 			return _signal.isAvailable();
 		}
 	}
 
 	public static class NativeAPIAdapterSignal2<A, B> implements NativeSignal2<A, B> {
 
 		private final List<Slot<SignalListener2<A, B>>> _bindings =
 				new CopyOnWriteArrayList<Slot<SignalListener2<A, B>>>();
 
 		private final Dispatcher<SignalListener2<A, B>> _dispatcher = DispatcherImpl
 				.newInstance(_bindings);
 
 		private final NativeAPIAdapterSignal<SignalListener2<A, B>, A> _signal =
 				new NativeAPIAdapterSignal<SignalListener2<A, B>, A>(_bindings);
 
 		/**
 		 * Private constructor
 		 */
 		protected NativeAPIAdapterSignal2() {
 		}
 
 		/**
 		 * Create a newInstance of NativeSignal2
 		 * 
 		 * @return {@link NativeSignal2}
 		 */
 		public static <A, B> NativeAPIAdapterSignal2<A, B> newInstance() {
 			return new NativeAPIAdapterSignal2<A, B>();
 		}
 
 		protected void createProxy(final String adapterName, final String listenerName,
 				final Class<?>[] parameterTypes, final NativeAPIAdapterDelegate delegate,
 				final List<String> delegateMethodNames) {
 			_signal.createProxy(adapterName, listenerName, parameterTypes, delegate,
 					delegateMethodNames);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener2<A, B>> add(SignalListener2<A, B> listener) {
 			return _signal.add(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener2<A, B>> addOnce(SignalListener2<A, B> listener) {
 			return _signal.addOnce(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener2<A, B>> remove(SignalListener2<A, B> listener) {
 			return _signal.remove(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void removeAll() {
 			_signal.removeAll();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public int getNumListeners() {
 			return _signal.getNumListeners();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @throws IllegalAccessError
 		 *             if target is null when the dispatch method is called.
 		 */
 		public void dispatch(A value0, B value1) throws Throwable {
 			if (null == _signal.getTarget())
 				throw new IllegalAccessError("Target can not be null");
 
 			_dispatcher.dispatch(value0, value1);
 		}
 
 		@Override
 		public A getTarget() {
 			return _signal.getTarget();
 		}
 
 		@Override
 		public void setTarget(A target) {
 			_signal.removeProxyMethod();
 			_signal.setTarget(target);
 			_signal.registerProxyMethod();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean isAvailable() {
 			return _signal.isAvailable();
 		}
 	}
 
 	public static class NativeAPIAdapterSignal3<A, B, C> implements NativeSignal3<A, B, C> {
 
 		private final List<Slot<SignalListener3<A, B, C>>> _bindings =
 				new CopyOnWriteArrayList<Slot<SignalListener3<A, B, C>>>();
 
 		private final Dispatcher<SignalListener3<A, B, C>> _dispatcher = DispatcherImpl
 				.newInstance(_bindings);
 
 		private final NativeAPIAdapterSignal<SignalListener3<A, B, C>, A> _signal =
 				new NativeAPIAdapterSignal<SignalListener3<A, B, C>, A>(_bindings);
 
 		/**
 		 * Private constructor
 		 */
 		protected NativeAPIAdapterSignal3() {
 		}
 
 		/**
 		 * Create a newInstance of NativeSignal3
 		 * 
 		 * @return {@link NativeSignal3}
 		 */
 		public static <A, B, C> NativeAPIAdapterSignal3<A, B, C> newInstance() {
 			return new NativeAPIAdapterSignal3<A, B, C>();
 		}
 
 		protected void createProxy(final String adapterName, final String listenerName,
 				final Class<?>[] parameterTypes, final NativeAPIAdapterDelegate delegate,
 				final List<String> delegateMethodNames) {
 			_signal.createProxy(adapterName, listenerName, parameterTypes, delegate,
 					delegateMethodNames);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener3<A, B, C>> add(SignalListener3<A, B, C> listener) {
 			return _signal.add(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener3<A, B, C>> addOnce(SignalListener3<A, B, C> listener) {
 			return _signal.addOnce(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener3<A, B, C>> remove(SignalListener3<A, B, C> listener) {
 			return _signal.remove(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void removeAll() {
 			_signal.removeAll();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public int getNumListeners() {
 			return _signal.getNumListeners();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @throws IllegalAccessError
 		 *             if target is null when the dispatch method is called.
 		 */
 		public void dispatch(A value0, B value1, C value2) throws Throwable {
 			if (null == _signal.getTarget())
 				throw new IllegalAccessError("Target can not be null");
 
 			_dispatcher.dispatch(value0, value1, value2);
 		}
 
 		@Override
 		public A getTarget() {
 			return _signal.getTarget();
 		}
 
 		@Override
 		public void setTarget(A target) {
 			_signal.removeProxyMethod();
 			_signal.setTarget(target);
 			_signal.registerProxyMethod();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean isAvailable() {
 			return _signal.isAvailable();
 		}
 	}
 
 	public static class NativeAPIAdapterSignal4<A, B, C, D> implements NativeSignal4<A, B, C, D> {
 
 		private final List<Slot<SignalListener4<A, B, C, D>>> _bindings =
 				new CopyOnWriteArrayList<Slot<SignalListener4<A, B, C, D>>>();
 
 		private final Dispatcher<SignalListener4<A, B, C, D>> _dispatcher = DispatcherImpl
 				.newInstance(_bindings);
 
 		private final NativeAPIAdapterSignal<SignalListener4<A, B, C, D>, A> _signal =
 				new NativeAPIAdapterSignal<SignalListener4<A, B, C, D>, A>(_bindings);
 
 		/**
 		 * Private constructor
 		 */
 		protected NativeAPIAdapterSignal4() {
 		}
 
 		/**
 		 * Create a newInstance of NativeSignal4
 		 * 
 		 * @return {@link NativeSignal4}
 		 */
 		public static <A, B, C, D> NativeAPIAdapterSignal4<A, B, C, D> newInstance() {
 			return new NativeAPIAdapterSignal4<A, B, C, D>();
 		}
 
 		protected void createProxy(final String adapterName, final String listenerName,
 				final Class<?>[] parameterTypes, final NativeAPIAdapterDelegate delegate,
 				final List<String> delegateMethodNames) {
 			_signal.createProxy(adapterName, listenerName, parameterTypes, delegate,
 					delegateMethodNames);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener4<A, B, C, D>> add(SignalListener4<A, B, C, D> listener) {
 			return _signal.add(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener4<A, B, C, D>> addOnce(SignalListener4<A, B, C, D> listener) {
 			return _signal.addOnce(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener4<A, B, C, D>> remove(SignalListener4<A, B, C, D> listener) {
 			return _signal.remove(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void removeAll() {
 			_signal.removeAll();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public int getNumListeners() {
 			return _signal.getNumListeners();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @throws IllegalAccessError
 		 *             if target is null when the dispatch method is called.
 		 */
 		public void dispatch(A value0, B value1, C value2, D value3) throws Throwable {
 			if (null == _signal.getTarget())
 				throw new IllegalAccessError("Target can not be null");
 
 			_dispatcher.dispatch(value0, value1, value2, value3);
 		}
 
 		@Override
 		public A getTarget() {
 			return _signal.getTarget();
 		}
 
 		@Override
 		public void setTarget(A target) {
 			_signal.removeProxyMethod();
 			_signal.setTarget(target);
 			_signal.registerProxyMethod();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean isAvailable() {
 			return _signal.isAvailable();
 		}
 	}
 
 	public static class NativeAPIAdapterSignal5<A, B, C, D, E> implements
 			NativeSignal5<A, B, C, D, E> {
 
 		private final List<Slot<SignalListener5<A, B, C, D, E>>> _bindings =
 				new CopyOnWriteArrayList<Slot<SignalListener5<A, B, C, D, E>>>();
 
 		private final Dispatcher<SignalListener5<A, B, C, D, E>> _dispatcher = DispatcherImpl
 				.newInstance(_bindings);
 
 		private final NativeAPIAdapterSignal<SignalListener5<A, B, C, D, E>, A> _signal =
 				new NativeAPIAdapterSignal<SignalListener5<A, B, C, D, E>, A>(_bindings);
 
 		/**
 		 * Private constructor
 		 */
 		protected NativeAPIAdapterSignal5() {
 		}
 
 		/**
 		 * Create a newInstance of NativeSignal5
 		 * 
 		 * @return {@link NativeSignal5}
 		 */
 		public static <A, B, C, D, E> NativeAPIAdapterSignal5<A, B, C, D, E> newInstance() {
 			return new NativeAPIAdapterSignal5<A, B, C, D, E>();
 		}
 
 		protected void createProxy(final String adapterName, final String listenerName,
 				final Class<?>[] parameterTypes, final NativeAPIAdapterDelegate delegate,
 				final List<String> delegateMethodNames) {
 			_signal.createProxy(adapterName, listenerName, parameterTypes, delegate,
 					delegateMethodNames);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener5<A, B, C, D, E>> add(SignalListener5<A, B, C, D, E> listener) {
 			return _signal.add(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener5<A, B, C, D, E>>
 				addOnce(SignalListener5<A, B, C, D, E> listener) {
 			return _signal.addOnce(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public Slot<SignalListener5<A, B, C, D, E>> remove(SignalListener5<A, B, C, D, E> listener) {
 			return _signal.remove(listener);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void removeAll() {
 			_signal.removeAll();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public int getNumListeners() {
 			return _signal.getNumListeners();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @throws IllegalAccessError
 		 *             if target is null when the dispatch method is called.
 		 */
 		public void dispatch(A value0, B value1, C value2, D value3, E value4) throws Throwable {
 			if (null == _signal.getTarget())
 				throw new IllegalAccessError("Target can not be null");
 
 			_dispatcher.dispatch(value0, value1, value2, value3, value4);
 		}
 
 		@Override
 		public A getTarget() {
 			return _signal.getTarget();
 		}
 
 		@Override
 		public void setTarget(A target) {
 			_signal.removeProxyMethod();
 			_signal.setTarget(target);
 			_signal.registerProxyMethod();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public boolean isAvailable() {
 			return _signal.isAvailable();
 		}
 	}
 }
