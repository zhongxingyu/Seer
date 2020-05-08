 package org.nohope.rpc;
 
 import org.nohope.rpc.protocol.RPC;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelUpstreamHandler;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
 import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
 import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
 import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
 
 /**
 * Set of decoders/encoders to convert packet format from/to {@link org.nohope.rpc.protocol.RPC.RpcRequest RpcRequest}.
  * <p/>
  * <b>Packet format</b>:
  * <pre>
  * +-----------------------+-------------+
  * | Serialized RpcRequest | Serialized  |
  * |    Length (4 bytes)   | RpcRequest  |
  * +-----------------------+-------------+
  * </pre>
  *
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 8/19/13 6:16 PM
  */
 public class PipelineFactory implements ChannelPipelineFactory {
     private static final int MAX_FRAME_BYTES_LENGTH = Integer.MAX_VALUE;
     private static final int HEADER_BYTES = 4;
 
     private final ChannelUpstreamHandler handler;
 
     public PipelineFactory(final ChannelUpstreamHandler handler) {
         this.handler = handler;
     }
 
     @Override
     public ChannelPipeline getPipeline() throws Exception {
         final ChannelPipeline p = Channels.pipeline();
         p.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(MAX_FRAME_BYTES_LENGTH, 0, HEADER_BYTES, 0, HEADER_BYTES));
         p.addLast("protobufDecoder", new ProtobufDecoder(RPC.RpcRequest.getDefaultInstance()));
 
         p.addLast("frameEncoder", new LengthFieldPrepender(HEADER_BYTES));
         p.addLast("protobufEncoder", new ProtobufEncoder());
         p.addLast("handler", handler);
         return p;
     }
 }
