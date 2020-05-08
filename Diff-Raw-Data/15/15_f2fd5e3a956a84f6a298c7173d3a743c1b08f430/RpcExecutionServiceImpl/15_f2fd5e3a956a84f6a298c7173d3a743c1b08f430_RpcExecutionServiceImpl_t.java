 /**
  * Copyright 2013 Bjørn Remseth (la3lma@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package no.rmz.blobee.rpc.server;
 
 import com.google.common.base.Objects;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.protobuf.Descriptors;
 import com.google.protobuf.Message;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.rmz.blobee.controllers.RpcServiceController;
 import no.rmz.blobee.rpc.methods.ResolverImpl;
 import no.rmz.blobee.rpc.methods.ServerSideMethodDesc;
 import no.rmz.blobee.rpc.peer.RemoteExecutionContext;
 import no.rmz.blobee.threads.ErrorLoggingThreadFactory;
 import no.rmz.blobeeproto.api.proto.Rpc.MethodSignature;
 import org.jboss.netty.channel.ChannelHandlerContext;
 
 /**
  * The implementation object implements one or more interfaces each of those
  * interfaces present methods that can be served through.
  */
 public final class RpcExecutionServiceImpl
         implements RpcExecutionService {
 
     private static  final Logger log =
             Logger.getLogger(RpcExecutionServiceImpl.class.getName());
     /**
      * A thread pool using the EXCEPTION_HANDLER that is used to execute
      * incoming RPC requests.
      */
     private final ExecutorService threadPool = Executors.newCachedThreadPool(
             new ErrorLoggingThreadFactory(
                "Executor thread for RpcExecutionServiceImpl", log));
 
     // XXX Used in a similar way as MethodSignatureResolver,
     //     some consolidation is in order.
     private Map<MethodSignature, ServerSideMethodDesc> xmap =
             new ConcurrentHashMap<>();
 
     private Object implementation;
     private Map<Class, Object> implementations = new HashMap<>();
     private final ControllerStorage controllerStorage = new ControllerStorage();
 
     @Override
     public Class getReturnType(final MethodSignature sig) {
         // Preconditions
         checkNotNull(sig);
         // checkArgument(!returnTypes.isEmpty());
         // final Class<?> result = returnTypes.get(sig);
         final Class<?> result = xmap.get(sig).getReturnType();
         // Postcondition
         checkNotNull(result);
         return result;
     }
 
     @Override
     public Class getParameterType(final MethodSignature sig) {
         checkNotNull(sig);
 
         final ServerSideMethodDesc ms = xmap.get(sig);
         if (ms != null) {
             return ms.getPmType();
         } else {
             return null;
         }
     }
 
     private final String name;
 
     public RpcExecutionServiceImpl(
             final String name) {
         this.name = checkNotNull(name);
     }
 
     @Override
     public void addImplementation(
             final Object implementation,
             final Class interfaceClasses) throws RpcServerException {
 
         addImplementation(implementation, new Class[]{interfaceClasses});
     }
 
     private void addImplementation(
             final Object implementation,
             final Class[] interfaceClasses) throws RpcServerException {
         this.implementation = checkNotNull(implementation);
 
         final Collection<Class> ifaces = new HashSet<>();
 
         for (final Class i : implementation.getClass().getClasses()) {
             ifaces.add(i);
         }
 
         for (final Class ic : interfaceClasses) {
             if (implementations.containsKey(ic)) {
                 throw new RpcServerException(
                         "Interface "
                         + ic
                         + " already has an implementation");
             }
         }
 
         log.info("The interfaces are " + ifaces);
         for (final Class iface : interfaceClasses) {
 
             if (!ifaces.contains(iface)) {
                 throw new RpcServerException(
                         "The implementation " + implementation + "does not implement interface " + iface);
             }
 
             for (final Method interfaceMethod : iface.getMethods()) {
                 final TypeVariable<Method>[] typeParameters =
                         interfaceMethod.getTypeParameters();
 
                 final String name = interfaceMethod.getName();
 
                 final Descriptors.MethodDescriptor descriptor;
                 try {
                     descriptor = ServiceAnnotationMapper.getMethodDescriptor(
                             implementation.getClass(), name);
                 } catch (Exception ex) { // XXX Bad habit
                     throw new RpcServerException(ex);
                 }
 
                if (descriptor == null) {
                    throw new RpcServerException("method descriptor was null");
                }
                 final MethodSignature methodSignature =
                         ResolverImpl.getMethodSignatureFromMethodDescriptor(
                         descriptor);
 
                 final Method implementationMethod =
                         findMethod(name, implementation.getClass());
                 if (implementationMethod == null) {
                     throw new IllegalStateException("Unknown method " + name);
                 }
 
 
                 // First we get the list of parameters for the implementation,
                 // the length of this array should be exactly 3: The first
                 // parameter
                 // is the controller, the second is the parameter and the
                 // third is the  callback method, that takes the
                 // return type as its parameter.
                 final Class<?>[] parameterTypes =
                         implementationMethod.getParameterTypes();
 
                 // So this would then be the type of the callback
                 final Class pmtype = parameterTypes[1];
 
                 // Then we get the return value
                 final Type typeOfReturnvalue =
                         extractCallbackParamType(interfaceMethod);
                 /// this is the new way
                 final ServerSideMethodDesc methodDesc =
                         new ServerSideMethodDesc(
                             implementationMethod,
                             (Class) typeOfReturnvalue,
                             pmtype);
                 xmap.put(methodSignature, methodDesc);
 
                 implementations.put(iface, implementation);
             }
         }
     }
 
     private static Type extractCallbackParamType(
             final Method interfaceMethod) {
         checkNotNull(interfaceMethod);
         // Now calculate the return type:
         // First we get the type of the RpcCallback
         final Type rpcCallbackType =
                 interfaceMethod.getGenericParameterTypes()[2];
         final ParameterizedType ptype = (ParameterizedType) rpcCallbackType;
         final Type[] actualTypeArguments = ptype.getActualTypeArguments();
         final Type typeOfReturnvalue = actualTypeArguments[0];
         return typeOfReturnvalue;
     }
 
     // XXX Linear search.  I'm sure there is a better way.
     private Method findMethod(final String name, final Class clazz) {
         for (final Method method : clazz.getMethods()) {
             if (method.getName().equals(name)) {
                 return method;
             }
         }
         return null;
     }
 
     public void removeController(
             final ChannelHandlerContext ctx,
             final long rpcIndex) {
         controllerStorage.removeController(ctx, rpcIndex);
     }
 
     public static  final class ControllerCoordinate {
 
         private final ChannelHandlerContext ctx;
         private final Long rpcIdx;
 
         public ControllerCoordinate(
                 final ChannelHandlerContext ctx,
                 final long rpcIdx) {
             this.ctx = checkNotNull(ctx);
             checkArgument(rpcIdx >= 0);
             this.rpcIdx = rpcIdx;
         }
 
         @Override
         public int hashCode() {
             return Objects.hashCode(rpcIdx, ctx);
         }
 
         @Override
         public boolean equals(final Object obj) {
             if (obj instanceof ControllerCoordinate) {
                 final ControllerCoordinate ob = (ControllerCoordinate) obj;
                 return Objects.equal(ctx, ob.ctx)
                         && Objects.equal(rpcIdx, ob.rpcIdx);
             } else {
                 return false;
             }
         }
     }
 
     @Override
     public void execute(
             final RemoteExecutionContext dc,
             final ChannelHandlerContext ctx, // XXX Redundant? dc.getCtx?
             final Message parameter,
             final boolean multiReturn,
             final boolean noReturn) {

         checkNotNull(dc);
         checkNotNull(ctx);
         checkNotNull(parameter);
 
         final Runnable runnable =
                 new MethodInvokingRunnable(
                   implementation, dc, ctx, parameter, controllerStorage, this,
                   multiReturn, noReturn);
         try {
             threadPool.submit(runnable);
         }  catch (Exception e) {
             log.log(Level.SEVERE,
                     "Couldn't submit runnable.  That's awful!", e);
         }
     }
 
     @Override
     public void startCancel(
             final ChannelHandlerContext ctx,
             final long rpcIndex) {
         checkNotNull(ctx);
         checkArgument(rpcIndex >= 0);
         // XXX Bogus error handling
         controllerStorage.getController(ctx, rpcIndex).startCancel();
         controllerStorage.removeController(ctx, rpcIndex);
     }
 
 
 
     public Method getMethod(final MethodSignature ms) {
         checkNotNull(ms);
         final ServerSideMethodDesc item = xmap.get(ms);
         if (item != null) {
             return item.getMethod();
         } else {
             return null;
         }
     }
 
     public void storeController(
             final ChannelHandlerContext ctx,
             final long rpcIdx,
             final RpcServiceController controller) {
         checkArgument(rpcIdx >= 0);
         checkNotNull(controller);
         checkNotNull(ctx);
         controllerStorage.storeController(ctx, rpcIdx, controller);
     }
 }
