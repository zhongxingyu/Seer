 package no.rmz.blobee.rpc.server;
 import static com.google.common.base.Preconditions.checkNotNull;

 import com.google.protobuf.Message;
 import com.google.protobuf.RpcCallback;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import no.rmz.blobee.controllers.RpcServiceController;
 import no.rmz.blobee.controllers.RpcServiceControllerImpl;
 import no.rmz.blobee.rpc.peer.RemoteExecutionContext;
 import org.jboss.netty.channel.ChannelHandlerContext;
 
 
 final class MethodInvokingRunnable implements Runnable {
     private final Object implementation;
     private final RemoteExecutionContext dc;
     private final ChannelHandlerContext ctx;
     private final Object parameter;
    private final RpcExecutionServiceImpl executor; // XXX Impl, not interface!!!
    // XXX Impl, not interface!!!
 
    public MethodInvokingRunnable(final Object implementation, final RemoteExecutionContext dc, final ChannelHandlerContext ctx, final Object parameter, final ControllerStorage ctStor, final RpcExecutionServiceImpl executor) {
         this.implementation = checkNotNull(implementation);
         this.dc = checkNotNull(dc);
         this.ctx = checkNotNull(ctx);
         this.parameter = checkNotNull(parameter);
         this.executor = checkNotNull(executor);
     }
 
     @Override
     public void run() {
         final Method method = executor.getMethod(dc.getMethodSignature());
         final RpcServiceController controller = new RpcServiceControllerImpl(dc);
         executor.storeController(ctx, dc.getRpcIndex(), controller);
         final RpcCallback<Message> callbackAdapter = new RpcCallback<Message>() {
             public void run(final Message response) {
                 controller.invokeCancelledCallback();
                 dc.returnResult(response);
             }
         };
         try {
             method.invoke(implementation, controller, parameter, callbackAdapter);
         } // XXX Throwing Runtime Exceptions is evil, should be caught,
         //     then logged.
         catch (IllegalAccessException ex) {
             throw new RuntimeException(ex);
         }
         catch (IllegalArgumentException ex) {
             throw new RuntimeException(ex);
         }
         catch (InvocationTargetException ex) {
             throw new RuntimeException(ex);
         }
        executor.removeController(ctx, dc.getRpcIndex());
     }
 
 }
