 package org.xmodel.net.bind;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.List;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.buffer.ChannelBufferOutputStream;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.xmodel.IModelObject;
 import org.xmodel.external.IExternalReference;
 import org.xmodel.log.Log;
 import org.xmodel.net.XioChannelHandler.Type;
 import org.xmodel.net.XioException;
 
 /**
  * TODO: attrLengthEstimate needs to be progressively refined
  */
 public class UpdateProtocol
 {
   public UpdateProtocol( BindProtocol protocol)
   {
     this.protocol = protocol;
     this.attrLengthEstimate = 64;
   }
   
   /**
    * Reset this instance by releasing internal resources.  This method should be called after 
    * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
    */
   public void reset()
   {
   }
   
   /**
    * Send an update.
    * @param channel The channel.
    * @param parent The parent.
    * @param child The child.
    * @param index The insertion index.
    */
   public void sendAddChild( Channel channel, IModelObject parent, IModelObject child, int index) throws IOException
   {
     int parentNetID = protocol.responseCompressor.getLocalID( parent);
         
     List<byte[]> buffers = protocol.responseCompressor.compress( child);
     ChannelBuffer buffer2 = ChannelBuffers.wrappedBuffer( buffers.toArray( new byte[ 0][]));
     ChannelBuffer buffer1 = protocol.headerProtocol.writeHeader( 8, Type.addChild, buffer2.readableBytes());
     buffer1.writeInt( parentNetID);
     buffer1.writeInt( index);
     
     long childNetID = protocol.responseCompressor.getLocalID( child);
     log.debugf( "UpdateProtocol.sendAddChild: parent=%s/%X, child=%s/%X, index=%d", parent.getType(), parentNetID, child.getType(), childNetID, index);
     
     // ignoring write buffer overflow for this type of messaging
     channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
   }
   
   /**
    * Send an update.
    * @param channel The channel.
    * @param parent The parent.
    * @param child The child that was removed.
    * @param index The index of the child.
    */
   public void sendRemoveChild( Channel channel, IModelObject parent, IModelObject child, int index) throws IOException
   {
     int parentNetID = protocol.responseCompressor.getLocalID( parent);
     protocol.responseCompressor.freeLocal( child);
     
     ChannelBuffer buffer = protocol.headerProtocol.writeHeader( 8, Type.removeChild, 0);
     buffer.writeInt( parentNetID);
     buffer.writeInt( index);
     
     log.debugf( "UpdateProtocol.sendRemoveChild: parent=%s/%X, index=%d", parent.getType(), parentNetID, index);
     
     // ignoring write buffer overflow for this type of messaging
     channel.write( buffer);
   }
 
   /**
    * Send an update.
    * @param channel The channel.
    * @param element The element.
    * @param attrName The name of the attribute.
    * @param newValue The new value of the attribute.
    */
   public void sendChangeAttribute( Channel channel, IModelObject element, String attrName, Object newValue) throws IOException
   {
     int netID = protocol.responseCompressor.getLocalID( element);
    byte[] bytes = attrName.getBytes( "UTF-8");
     
     ChannelBuffer buffer2 = ChannelBuffers.dynamicBuffer( attrLengthEstimate);
     int attrLength = protocol.serializer.writeObject( new DataOutputStream( new ChannelBufferOutputStream( buffer2)), newValue);
     
     ChannelBuffer buffer1 = protocol.headerProtocol.writeHeader( 5 + 1 + bytes.length, Type.changeAttribute, attrLength);
     buffer1.writeInt( netID);
     buffer1.writeByte( bytes.length);
     buffer1.writeBytes( bytes);
     
     log.debugf( "UpdateProtocol.sendChangeAttribute: parent=%X, attrName=%s, attrValue=%s", netID, attrName, newValue);
     
     // ignoring write buffer overflow for this type of messaging
     channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
   }
 
   /**
    * Send an update.
    * @param channel The channel.
    * @param element The element.
    * @param attrName The name of the attribute.
    */
   public void sendClearAttribute( Channel channel, IModelObject element, String attrName) throws IOException
   {
     int netID = protocol.responseCompressor.getLocalID( element);
    byte[] bytes = attrName.getBytes( "UTF-8");
     
     ChannelBuffer buffer = protocol.headerProtocol.writeHeader( 5 + 1 + bytes.length, Type.changeAttribute, 0);
     buffer.writeInt( netID);
     
     buffer.writeByte( bytes.length);
     buffer.writeBytes( bytes);
     
     log.debugf( "UpdateProtocol.sendClearAttribute: parent=%X, attrName=%s", netID, attrName);
     
     // ignoring write buffer overflow for this type of messaging
     channel.write( buffer);
   }
 
   /**
    * Send an update.
    * @param channel The channel.
    * @param element The element.
    * @param dirty The dirty state.
    */
   public void sendChangeDirty( Channel channel, IModelObject element, boolean dirty) throws IOException
   {
     int netID = protocol.responseCompressor.getLocalID( element);
     
     ChannelBuffer buffer = protocol.headerProtocol.writeHeader( 5, Type.changeAttribute, 0);
     buffer.writeInt( netID);
     buffer.writeByte( dirty? 1: 0);
     
     log.debugf( "UpdateProtocol.sendChangeDirty: element=%X, dirty=%s", netID, dirty);
     
     // ignoring write buffer overflow for this type of messaging
     channel.write( buffer);
   }
   
   /**
    * Handle an update.
    * @param channel The channel.
    * @param buffer The buffer.
    */
   public void handleAddChild( Channel channel, ChannelBuffer buffer) throws IOException, XioException
   {
     int parentNetID = buffer.readInt();
     int index = buffer.readInt();
     IModelObject child = protocol.requestCompressor.decompress( new ChannelBufferInputStream( buffer));
   
     IModelObject parent = protocol.requestCompressor.findRemote( parentNetID);
     if ( parent == null) throw new XioException( String.format( "Parent %X not found", parentNetID));
     
     log.debugf( "UpdateProtocol.handleAddChild: parent=%X, child=%s, index=%d", parentNetID, child.getType(), index);
     
     protocol.executor.execute( new AddChildEvent( parent, child, index));
   }
 
   /**
    * Handle an update.
    * @param channel The channel.
    * @param buffer The buffer.
    */
   public void handleRemoveChild( Channel channel, ChannelBuffer buffer) throws IOException, XioException
   {
     int parentNetID = buffer.readInt();
     int index = buffer.readInt();
   
     IModelObject parent = protocol.requestCompressor.findRemote( parentNetID);
     if ( parent == null) throw new XioException( String.format( "Parent %X not found", parentNetID));
     
     log.debugf( "UpdateProtocol.handleRemoveChild: parent=%X, index=%d", parentNetID, index);
     
     protocol.executor.execute( new RemoveChildEvent( parent, index));
   }
 
   /**
    * Handle an update.
    * @param channel The channel.
    * @param buffer The buffer.
    */
   public void handleChangeAttribute( Channel channel, ChannelBuffer buffer) throws IOException, ClassNotFoundException, XioException
   {
     int netID = buffer.readInt();
     
     byte[] bytes = new byte[ buffer.readUnsignedByte()];
     buffer.readBytes( bytes);
    String attrName = new String( bytes);
     
     Object newValue = protocol.serializer.readObject( new DataInputStream( new ChannelBufferInputStream( buffer)));
   
     log.debugf( "UpdateProtocol.handleChangeAttribute: element=%X, attrName=%s, attrValue=%s", netID, attrName, newValue);
     
     IModelObject element = protocol.requestCompressor.findRemote( netID);
     if ( element == null) throw new XioException( String.format( "Element %X not found", netID));
     
     protocol.executor.execute( new ChangeAttributeEvent( element, attrName, newValue));
   }
 
   /**
    * Handle an update.
    * @param channel The channel.
    * @param buffer The buffer.
    */
   public void handleClearAttribute( Channel channel, ChannelBuffer buffer) throws IOException, XioException
   {
     int netID = buffer.readInt();
     
     byte[] bytes = new byte[ buffer.readUnsignedByte()];
     buffer.readBytes( bytes);
    String attrName = new String( bytes);
   
     log.debugf( "UpdateProtocol.handleClearAttribute: element=%X, attrName=%s", netID, attrName);
     
     IModelObject element = protocol.requestCompressor.findRemote( netID);
     if ( element == null) throw new XioException( String.format( "Element %X not found", netID));
     
     protocol.executor.execute( new ClearAttributeEvent( element, attrName));
   }
 
   /**
    * Handle an update.
    * @param channel The channel.
    * @param buffer The buffer.
    */
   public void handleChangeDirty( Channel channel, ChannelBuffer buffer) throws IOException, XioException
   {
     int netID = buffer.readInt();
     boolean dirty = buffer.readByte() != 0;
 
     log.debugf( "UpdateProtocol.handleChangeDirty: element=%X, dirty=%s", netID, dirty);
     
     IModelObject element = protocol.requestCompressor.findRemote( netID);
     if ( element == null) throw new XioException( String.format( "Element %X not found", netID));
     if ( !(element instanceof IExternalReference)) throw new XioException( String.format( "Element %X is not a reference", netID));
     
     protocol.executor.execute( new ChangeDirtyEvent( (IExternalReference)element, dirty));
   }
   
   private final class AddChildEvent implements Runnable
   {
     public AddChildEvent( IModelObject parent, IModelObject child, int index)
     {
       this.parent = parent;
       this.child = child;
       this.index = index;
     }
     
     public void run()
     {
       parent.addChild( child, index);
     }
     
     private IModelObject parent;
     private IModelObject child;
     private int index;
   }
   
   private final class RemoveChildEvent implements Runnable
   {
     public RemoveChildEvent( IModelObject parent, int index)
     {
       this.parent = parent;
       this.index = index;
     }
     
     public void run()
     {
       protocol.requestCompressor.freeRemote( parent.removeChild( index));
     }
     
     private IModelObject parent;
     private int index;
   }
   
   private final class ChangeAttributeEvent implements Runnable
   {
     public ChangeAttributeEvent( IModelObject element, String attrName, Object attrValue)
     {
       this.element = element;
       this.attrName = attrName;
       this.attrValue = attrValue;
     }
     
     public void run()
     {
       element.setAttribute( attrName, attrValue);
     }
     
     private IModelObject element;
     private String attrName;
     private Object attrValue;
   }
   
   private final class ClearAttributeEvent implements Runnable
   {
     public ClearAttributeEvent( IModelObject element, String attrName)
     {
       this.element = element;
       this.attrName = attrName;
     }
     
     public void run()
     {
       element.removeAttribute( attrName);
     }
     
     private IModelObject element;
     private String attrName;
   }
   
   private final class ChangeDirtyEvent implements Runnable
   {
     public ChangeDirtyEvent( IExternalReference reference, boolean dirty)
     {
       this.reference = reference;
       this.dirty = dirty;
     }
     
     public void run()
     {
       reference.setDirty( dirty);
     }
     
     private IExternalReference reference;
     private boolean dirty;
   }
   
   private final static Log log = Log.getLog( UpdateProtocol.class);
   
   private BindProtocol protocol;
   private int attrLengthEstimate;
 }
