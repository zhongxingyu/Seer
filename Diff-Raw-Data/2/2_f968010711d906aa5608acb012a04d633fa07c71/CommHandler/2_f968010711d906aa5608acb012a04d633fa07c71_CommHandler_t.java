 
 
 package wuw.comm;
 
 import wuw.core.MsgHandler;
 import wuw.core.PeerID;
 
 
 /**
  * An object that handles transport messages must implements this interface.
  *
  * @author Marco Biazzini
  * @date 2012 Jan 27
  */
 public interface CommHandler {
 
 /**
  * By calling this method and providing a pointer to a handler, any object can
  * subscribe to a delivery service. This means that the provided handler will be
  * given any incoming message of the returned type.
  *
  * @param mh
 *          The pointer to the receiver object that can handle this message
  * @return The message ID that makes it possible to match messages and handlers.
  */
 public int addMsgHandler(MsgHandler mh);
 
 
 /**
  * Sends message <code>msg</code> to peer <code>dest</code>.
  *
  * @param dest
  *          The destination peer
  * @param mid
  *          The ID of the message, as returned to the caller by
  *          {@link #addMsgHandler}
  * @param msg
  *          message to be sent
  */
 public void send(PeerID dest, int mid, Object msg);
 
 
 /**
  * By calling this method whenever a message is received, the payload will be
  * dispatch to the proper handler ( @see wuw.comm.TMessage).
  *
  * @param msg
  *          The message to be dispatched to the proper handler.
  */
 void dispatch(TMessage msg);
 
 }
