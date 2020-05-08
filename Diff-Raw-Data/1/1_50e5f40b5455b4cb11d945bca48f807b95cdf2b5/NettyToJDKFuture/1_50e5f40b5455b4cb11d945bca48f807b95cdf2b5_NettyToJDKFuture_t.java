 // Copyright 2011 Nathaniel Harward
 //
 // This file is part of commons-j.
 //
 // commons-j is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // commons-j is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with commons-j. If not, see <http://www.gnu.org/licenses/>.
 
 package nerds.antelax.commons.net.pubsub;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.jboss.netty.channel.ChannelFuture;
 
 import com.google.common.base.Preconditions;
 
 final class NettyToJDKFuture implements Future<Boolean> {
 
     static final Future<Boolean> WRITE_FAILED = new Future<Boolean>() {
 
                                                   @Override
                                                   public boolean cancel(final boolean mayInterruptIfRunning) {
                                                       return false;
                                                   }
 
                                                   @Override
                                                   public boolean isCancelled() {
                                                       return false;
                                                   }
 
                                                   @Override
                                                   public boolean isDone() {
                                                       return true;
                                                   }
 
                                                   @Override
                                                   public Boolean get() throws InterruptedException, ExecutionException {
                                                       return Boolean.FALSE;
                                                   }
 
                                                   @Override
                                                   public Boolean get(final long timeout, final TimeUnit unit)
                                                           throws InterruptedException, ExecutionException, TimeoutException {
                                                       return Boolean.FALSE;
                                                   }
 
                                               };
     private final ChannelFuture  wrapped;
 
     public NettyToJDKFuture(final ChannelFuture wrapped) {
         Preconditions.checkNotNull(wrapped);
         this.wrapped = wrapped;
     }
 
     @Override
     public boolean cancel(final boolean mayInterruptIfRunning) {
         return wrapped.cancel();
     }
 
     @Override
     public boolean isCancelled() {
         return wrapped.isCancelled();
     }
 
     @Override
     public boolean isDone() {
         return wrapped.isDone();
     }
 
     @Override
     public Boolean get() throws InterruptedException, ExecutionException {
        wrapped.await();
         return wrapped.isSuccess();
     }
 
     @Override
     public Boolean get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
         if (!wrapped.await(timeout, unit))
             throw new TimeoutException();
         return get();
     }
 
 }
