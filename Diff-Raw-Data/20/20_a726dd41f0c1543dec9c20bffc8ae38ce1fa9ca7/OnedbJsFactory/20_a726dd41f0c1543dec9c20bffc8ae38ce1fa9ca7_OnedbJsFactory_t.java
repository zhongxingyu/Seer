 package com.ononedb.nextweb.js.internal;
 
 import io.nextweb.Session;
 import io.nextweb.operations.callbacks.CallbackFactory;
 import io.nextweb.promise.Fn;
 import io.nextweb.promise.NextwebOperation;
 import io.nextweb.promise.NextwebPromise;
 import io.nextweb.promise.exceptions.NextwebExceptionManager;
 
 import com.ononedb.nextweb.common.NextwebPromiseImpl;
 import com.ononedb.nextweb.common.OnedbFactory;
 
 import de.mxro.async.Operation;
 import de.mxro.async.callbacks.ValueCallback;
 import de.mxro.fn.Closure;
 import de.mxro.promise.Promise;
 import de.mxro.promise.PromisesCommon;
 
 public class OnedbJsFactory extends OnedbFactory {
 
     @Override
     public <ResultType> NextwebPromise<ResultType> createPromise(final NextwebExceptionManager exceptionManager,
             final Session session, final NextwebOperation<ResultType> asyncResult) {
 
         final Promise<ResultType> promise = createPromiseNew(exceptionManager, session, asyncResult);
 
         return new NextwebPromiseImpl<ResultType>(asyncResult, promise, exceptionManager, session);
 
     }
 
     private <ResultType> Promise<ResultType> createPromiseNew(final NextwebExceptionManager exceptionManager,
             final Session session, final NextwebOperation<ResultType> operation) {
 
         final Promise<ResultType> promise = PromisesCommon.createUnsafe(new Operation<ResultType>() {
 
             @Override
             public String toString() {
                 return "[(" + operation + ") wrapped by (" + super.toString() + ")]";
             }
 
             @Override
             public void apply(final ValueCallback<ResultType> callback) {
                 operation.apply(CallbackFactory.wrap(callback));
 
             }
         });
 
        promise.addExceptionFallback(new Closure<Throwable>() {
 
            @Override
            public void apply(final Throwable o) {
                exceptionManager.onFailure(Fn.exception(this, o));
            }
        });
 
         return promise;
     }
 }
