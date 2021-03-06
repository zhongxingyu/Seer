 /**
  * Copyright 2009, Frederic Bregier, and individual contributors
  * by the @author tags. See the COPYRIGHT.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3.0 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package goldengate.ftp.core.data;
 
 import goldengate.common.command.ReplyCode;
 import goldengate.common.command.exception.CommandAbstractException;
 import goldengate.common.command.exception.Reply425Exception;
 import goldengate.common.file.FileInterface;
 import goldengate.common.future.GgFuture;
 import goldengate.common.logging.GgInternalLogger;
 import goldengate.common.logging.GgInternalLoggerFactory;
 import goldengate.ftp.core.command.FtpCommandCode;
 import goldengate.ftp.core.command.service.ABOR;
 import goldengate.ftp.core.config.FtpInternalConfiguration;
 import goldengate.ftp.core.control.NetworkHandler;
 import goldengate.ftp.core.data.handler.DataNetworkHandler;
 import goldengate.ftp.core.exception.FtpNoConnectionException;
 import goldengate.ftp.core.exception.FtpNoFileException;
 import goldengate.ftp.core.exception.FtpNoTransferException;
 import goldengate.ftp.core.session.FtpSession;
 import goldengate.ftp.core.utils.FtpCommandUtils;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.Channels;
 
 /**
  * Main class that handles transfers and their execution
  *
  * @author Frederic Bregier
  *
  */
 public class FtpTransferControl {
     /**
      * Internal Logger
      */
     private static final GgInternalLogger logger = GgInternalLoggerFactory
             .getLogger(FtpTransferControl.class);
 
     /**
      * SessionInterface
      */
     private final FtpSession session;
 
     /**
      * Lock for Transfer Control
      */
     private final ReentrantLock lock = new ReentrantLock();
 
     /**
      * Step in order to wait that the DataNetworkHandler is ready
      */
     private boolean isDataNetworkHandlerReady = false;
 
     /**
      * The associated DataChannel
      */
     private Channel dataChannel = null;
 
     /**
      * Blocking step in order to wait that the DataNetworkHandler is ready
      */
     private GgFuture dataNetworkHandlerReady = null;
 
     /**
      * Concurrent list to wait for the dataChannel to be opened
      */
     private final LinkedBlockingQueue<Channel> waitForOpenedDataChannel = new LinkedBlockingQueue<Channel>();
 
     /**
      * Concurrent list to wait for the dataChannel to be closed
      */
     private GgFuture closedDataChannel = null;
 
     /**
      * Is the current Command Finished (or previously current command)
      */
     private boolean isExecutingCommandFinished = true;
 
     /**
      * Current command executed
      */
     private FtpTransfer executingCommand = null;
 
     /**
      * Thread pool for execution of transfer command
      */
     private ExecutorService executorService = null;
 
     /**
      * Blocking step for the Executor in order to wait for the end of the
      * command.
      */
     private GgFuture endOfCommand = null;
 
     /**
      * A boolean to know if Check was called once
      */
     private boolean isCheckAlreadyCalled = false;
 
     /**
      *
      * @param session
      */
     public FtpTransferControl(FtpSession session) {
         this.session = session;
         dataNetworkHandlerReady = new GgFuture();
         closedDataChannel = new GgFuture();
         endOfCommand = null;
     }
 
     // XXX DataNetworkHandler functions
     /**
      * The DataNetworkHandler is ready (from setNewFtpExecuteTransfer)
      *
      */
     private void setDataNetworkHandlerReady() {
         isCheckAlreadyCalled = false;
         if (isDataNetworkHandlerReady) {
             return;
         }
         dataNetworkHandlerReady.setSuccess();
         isDataNetworkHandlerReady = true;
     }
 
     /**
      * Wait for the DataNetworkHandler to be ready (from trueRetrieve of
      * {@link FileInterface})
      *
      * @throws InterruptedException
      *
      */
     public void waitForDataNetworkHandlerReady() throws InterruptedException {
         if (!isDataNetworkHandlerReady) {
             GgFuture future = dataNetworkHandlerReady.await();
             dataNetworkHandlerReady = new GgFuture();
             logger.debug("Wait for DataNetwork Ready over {}", future
                     .isSuccess());
         }
     }
 
     /**
      * Set the new opened Channel (from channelConnected of
      * {@link DataNetworkHandler})
      *
      * @param channel
      * @param dataNetworkHandler
      */
     public void setOpenedDataChannel(Channel channel,
             DataNetworkHandler dataNetworkHandler) {
         session.getDataConn().setDataNetworkHandler(dataNetworkHandler);
         if (channel != null) {
             waitForOpenedDataChannel.add(channel);
         } else {
             waitForOpenedDataChannel.add(session.getControlChannel());
         }
     }
 
     /**
      * Wait that the new opened connection is ready (same method in
      * {@link FtpDataAsyncConn} from openConnection)
      *
      * @return the new opened Channel
      * @throws InterruptedException
      */
     public Channel waitForOpenedDataChannel() throws InterruptedException {
         Channel channel = waitForOpenedDataChannel.take();
         logger.debug("Wait for New opened Data Channel over");
         if (session.getControlChannel() == channel) {
             return null;
         }
         return channel;
     }
 
     /**
      * Set the closed Channel (from channelClosed of {@link DataNetworkHandler})
      */
     public void setClosedDataChannel() {
         closedDataChannel.setSuccess();
     }
 
     /**
      * Wait for the client to be connected (Passive) or Wait for the server to
      * be connected to the client (Active) (called from {@link FtpCommandUtils}
      * with same method)
      *
      * @return True if the connection is OK
      * @throws Reply425Exception
      */
     public boolean openDataConnection() throws Reply425Exception {
         lock.lock();
         try {
             FtpDataAsyncConn dataAsyncConn = session.getDataConn();
             if (!dataAsyncConn.isStreamFile()) {
                 // FIXME isConnected or isDNHReady ?
                 if (dataAsyncConn.isConnected()) {
                     // Already connected
                     logger.debug("Connection already open");
                     session.setReplyCode(
                             ReplyCode.REPLY_125_DATA_CONNECTION_ALREADY_OPEN,
                             dataAsyncConn.getType().name() +
                                     " mode data connection already open");
                     return true;
                 }
             } else {
                 // Stream, Data Connection should not be opened
                 if (dataAsyncConn.isConnected()) {
                     logger
                             .error("Connection already open but should not since in Stream mode");
                     setTransferAbortedFromInternal(false);
                     return false;
                 }
             }
             // Need to open connection
             session.setReplyCode(ReplyCode.REPLY_150_FILE_STATUS_OKAY,
                     "Opening " + dataAsyncConn.getType().name() +
                             " mode data connection");
             if (dataAsyncConn.isPassiveMode()) {
                 // Wait for the connection to be done by the client
                 logger.debug("Passive mode standby");
                 try {
                     dataChannel = dataAsyncConn.waitForOpenedDataChannel();
                 } catch (InterruptedException e) {
                     logger.warn("Connection abort in passive mode", e);
                     // Cannot open connection
                     throw new Reply425Exception(
                             "Cannot open passive data connection");
                 }
                 logger.debug("Passive mode connected");
             } else {
                 // Wait for the server to be connected to the client
                 logger.debug("Active mode standby");
                 ChannelFuture future = null;
                 for (int i = 0; i < FtpInternalConfiguration.RETRYNB; i ++) {
                     ClientBootstrap clientBootstrap = session
                             .getConfiguration().getFtpInternalConfiguration()
                             .getActiveBootstrap();
                     // Set the session for the future dataChannel
                     session.getConfiguration()
                             .getFtpInternalConfiguration().setNewFtpSession(
                                     dataAsyncConn.getLocalAddress()
                                             .getAddress(),
                                     dataAsyncConn.getRemoteAddress(),
                                     session);
                     future = clientBootstrap.connect(dataAsyncConn
                             .getRemoteAddress(), dataAsyncConn
                             .getLocalAddress());
                     future.awaitUninterruptibly().getChannel();
                     if (future.isSuccess()) {
                         // Wait for the server to be fully connected to the
                         // client
                         try {
                             dataChannel = dataAsyncConn
                                     .waitForOpenedDataChannel();
                         } catch (InterruptedException e) {
                             logger.warn("Connection abort in active mode", e);
                             // Cannot open connection
                             throw new Reply425Exception(
                                     "Cannot open active data connection");
                         }
                         logger.debug("Active mode connected");
                         break;
                     } else {
                         try {
                             Thread.sleep(FtpInternalConfiguration.RETRYINMS);
                         } catch (InterruptedException e) {
                             break;
                         }
                     }
                 }
                 if (future == null) {
                     // Cannot open connection
                     throw new Reply425Exception(
                             "Cannot open active data connection");
                 }
                 if (!future.isSuccess()) {
                     logger.error("Can't do Active connection:", future
                             .getCause());
                     // Cannot open connection
                     throw new Reply425Exception(
                             "Cannot open active data connection");
                 }
             }
             if (dataChannel == null) {
                 // Cannot have a new Data connection since shutdown
                 throw new Reply425Exception(
                         "Cannot open data connection, shuting down");
             }
         } finally {
             lock.unlock();
         }
         return true;
     }
 
     // XXX FtpTransfer functions
     /**
      * Run the command from an executor
      */
     private void runExecutor() {
         // Unlock Mode Codec
         try {
             session.getDataConn().getDataNetworkHandler()
                     .unlockModeCodec();
         } catch (FtpNoConnectionException e) {
             setTransferAbortedFromInternal(false);
             return;
         }
         // Run the command
         if (executorService == null) {
             executorService = Executors.newSingleThreadExecutor();
         }
         endOfCommand = new GgFuture(true);
         executorService.execute(new FtpTransferExecutor(session,
                 executingCommand));
     }
 
     /**
      * Add a new transfer to be executed. This is to be called from Command
      * after connection is opened and before answering to the client that
      * command is ready to be executed (for Store or Retrieve like operations).
      *
      * @param command
      * @param file
      */
     public void setNewFtpTransfer(FtpCommandCode command, FileInterface file) {
         lock.lock();
         try {
             isExecutingCommandFinished = false;
             logger.debug("setNewCommand: {}", command);
             setDataNetworkHandlerReady();
             executingCommand = new FtpTransfer(command, file);
             runExecutor();
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Add a new transfer to be executed. This is to be called from Command
      * after connection is opened and before answering to the client that
      * command is ready to be executed (for List like operations).
      *
      * @param command
      * @param list
      * @param path
      *            as Original Path
      */
     public void setNewFtpTransfer(FtpCommandCode command, List<String> list,
             String path) {
         lock.lock();
         try {
             isExecutingCommandFinished = false;
             logger.debug("setNewCommand: {}", command);
             setDataNetworkHandlerReady();
             executingCommand = new FtpTransfer(command, list, path);
             runExecutor();
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Is a command currently executing (called from {@link NetworkHandler} when
      * a message is received to see if another transfer command is already in
      * execution, which is not allowed)
      *
      * @return True if a command is currently executing
      */
     public boolean isFtpTransferExecuting() {
         return !isExecutingCommandFinished;
     }
 
     /**
      *
      * @return the current executing FtpTransfer
      * @throws FtpNoTransferException
      */
     public FtpTransfer getExecutingFtpTransfer() throws FtpNoTransferException {
         if (executingCommand != null) {
             return executingCommand;
         }
         throw new FtpNoTransferException("No Command currently running");
     }
 
     /**
      *
      * @return True if the current FtpTransfer is a Retrieve like transfer
      * @throws FtpNoTransferException
      * @throws CommandAbstractException
      * @throws FtpNoFileException
      */
     private boolean isExecutingRetrLikeTransfer()
             throws FtpNoTransferException, CommandAbstractException,
             FtpNoFileException {
         return FtpCommandCode.isRetrLikeCommand(getExecutingFtpTransfer()
                 .getCommand()) && getExecutingFtpTransfer().getFtpFile()
                 .isInReading();
     }
 
     /**
      * Run the retrieve operation if necessary (called from
      * channelInterestChanged in {@link DataNetworkHandler})
      */
     public void runTrueRetrieve() {
         try {
             if (isExecutingRetrLikeTransfer()) {
                 getExecutingFtpTransfer().getFtpFile().trueRetrieve();
             }
         } catch (CommandAbstractException e) {
         } catch (FtpNoTransferException e) {
         } catch (FtpNoFileException e) {
         }
     }
 
     /**
      * Called when a transfer is finished from setEndOfTransfer
      *
      * @return True if it was already called before
      * @throws FtpNoTransferException
      */
     private boolean checkFtpTransferStatus() throws FtpNoTransferException {
         if (isCheckAlreadyCalled) {
             logger.warn("Check: ALREADY CALLED");
             return true;
         }
         if (isExecutingCommandFinished) {
             // already done
             logger.warn("Check: already Finished");
             throw new FtpNoTransferException("No transfer running");
         }
         if (!isDataNetworkHandlerReady) {
             // already done
             logger.warn("Check: already DNH not ready");
             throw new FtpNoTransferException("No connection");
         }
         lock.lock();
         try {
             isCheckAlreadyCalled = true;
             FtpTransfer executedTransfer = getExecutingFtpTransfer();
             logger.debug("Check: command {}", executedTransfer.getCommand());
             // DNH is ready and Transfer is running
             if (FtpCommandCode.isListLikeCommand(executedTransfer.getCommand())) {
                 if (executedTransfer.getStatus()) {
                     // Special status for List Like command
                     logger.debug("Check: List OK");
                     closeTransfer(true);
                     return false;
                 }
                 logger.debug("Check: List Ko");
                 abortTransfer(true);
                 return false;
             } else if (FtpCommandCode.isRetrLikeCommand(executedTransfer
                     .getCommand())) {
                 FileInterface file = null;
                 try {
                     file = executedTransfer.getFtpFile();
                 } catch (FtpNoFileException e) {
                     logger.debug("Check: Retr no FileInterface for Retr");
                     abortTransfer(true);
                     return false;
                 }
                 try {
                     if (file.isInReading()) {
                         logger
                                 .debug("Check: Retr FileInterface still in reading KO");
                         abortTransfer(true);
                     } else {
                         logger
                                 .debug("Check: Retr FileInterface no more in reading OK");
                         closeTransfer(true);
                     }
                 } catch (CommandAbstractException e) {
                     logger.warn("Retr Test is in Reading problem", e);
                     closeTransfer(true);
                 }
                 return false;
             } else if (FtpCommandCode.isStoreLikeCommand(executedTransfer
                     .getCommand())) {
                 logger.debug("Check: Store OK");
                 closeTransfer(true);
                 return false;
             } else {
                 logger.warn("Check: Unknown command");
                 abortTransfer(true);
             }
             return false;
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Abort the current transfer
      *
      * @param write
      *            True means the message is write back to the control command,
      *            false it is only prepared
      */
     private void abortTransfer(boolean write) {
         logger.debug("Will abort transfer and write: ", write);
         FileInterface file = null;
         FtpTransfer current = null;
         try {
             current = getExecutingFtpTransfer();
             file = current.getFtpFile();
             file.abortFile();
         } catch (FtpNoTransferException e) {
             logger.warn("Abort problem", e);
         } catch (FtpNoFileException e) {
         } catch (CommandAbstractException e) {
             logger.warn("Abort problem", e);
         }
         if (current != null) {
             current.setStatus(false);
         }
         endDataConnection();
         session.setReplyCode(
                 ReplyCode.REPLY_426_CONNECTION_CLOSED_TRANSFER_ABORTED,
                 "Transfer aborted for " +
                         (current == null? "Unknown command" : current
                                 .toString()));
         if (write) {
             session.getNetworkHandler().writeIntermediateAnswer();
         }
         finalizeExecution();
         if (current != null) {
             if (!FtpCommandCode.isListLikeCommand(current.getCommand())) {
                 session.getBusinessHandler().afterTransferDone(current);
             }
         }
     }
 
     /**
      * Finish correctly a transfer
      *
      * @param write
      *            True means the message is write back to the control command,
      *            false it is only prepared
      */
     private void closeTransfer(boolean write) {
         logger.debug("Will close transfer and write: {}", write);
         FileInterface file = null;
         FtpTransfer current = null;
         try {
             current = getExecutingFtpTransfer();
             file = current.getFtpFile();
             file.closeFile();
         } catch (FtpNoTransferException e) {
             logger.warn("Close problem", e);
         } catch (FtpNoFileException e) {
         } catch (CommandAbstractException e) {
             logger.warn("Close problem", e);
         }
         if (current != null) {
             current.setStatus(true);
         }
         if (session.getDataConn().isStreamFile()) {
             endDataConnection();
         }
         session.setReplyCode(
                 ReplyCode.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
                 "Transfer correctly finished for " +
                         (current == null? "Unknown command" : current
                                 .toString()));
         if (write) {
             session.getNetworkHandler().writeIntermediateAnswer();
         }
         finalizeExecution();
         if (current != null) {
             if (!FtpCommandCode.isListLikeCommand(current.getCommand())) {
                 session.getBusinessHandler().afterTransferDone(current);
             }
         }
     }
 
     /**
      * Set the current transfer as finished. Called from
      * {@link FtpTransferExecutor} when a transfer is over.
      *
      */
     public void setEndOfTransfer() {
         lock.lock();
         try {
             try {
                 checkFtpTransferStatus();
             } catch (FtpNoTransferException e) {
                 return;
             }
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * To enable abort from internal error
      *
      * @param write
      *            True means the message is write back to the control command,
     *            false it is only prepared
      */
     public void setTransferAbortedFromInternal(boolean write) {
         logger.debug("Set transfer aborted internal {}", write);
         lock.lock();
         try {
             abortTransfer(write);
             endOfCommand.cancel();
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Called by messageReceived, channelClosed (from {@link DataNetworkHandler}
      * ) and trueRetrieve (from {@link FileInterface}) when the transfer is over
     * or by channelClosed
      */
     public void setPreEndOfTransfer() {
         endOfCommand.setSuccess();
     }
 
     /**
      * Wait for the current transfer to finish, called from
      * {@link FtpTransferExecutor}
      *
      * @throws InterruptedException
      */
     public void waitForEndOfTransfer() throws InterruptedException {
         endOfCommand.await();
         logger.debug("waitEndOfCommand over");
     }
 
     // XXX ExecutorHandler functions
     /**
      * Finalize execution
      *
      */
     private void finalizeExecution() {
         logger.debug("Finalize execution");
         isExecutingCommandFinished = true;
         executingCommand = null;
     }
 
     // XXX Finalize of Transfer
     /**
      * End the data connection if any
      */
     private void endDataConnection() {
         logger.debug("End Data connection");
         lock.lock();
         try {
             if (isDataNetworkHandlerReady) {
                 isDataNetworkHandlerReady = false;
                 Channels.close(dataChannel);
                 closedDataChannel.awaitUninterruptibly();
                 // set ready for a new connection
                 closedDataChannel = new GgFuture();
                 logger.debug("waitForClosedDataChannel over");
                 dataChannel = null;
             }
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Clear the FtpTransferControl (called when the data connection must be
      * over like from clear of {@link FtpDataAsyncConn}, abort from {@link ABOR}
      * or ending control connection from {@link NetworkHandler}.
      *
      */
     public void clear() {
         logger.debug("Clear Ftp Transfer Control");
         endDataConnection();
         finalizeExecution();
         dataNetworkHandlerReady = null;
         closedDataChannel = null;
         endOfCommand = null;
         waitForOpenedDataChannel.clear();
         if (executorService != null) {
             executorService.shutdownNow();
             executorService = null;
         }
     }
 }
