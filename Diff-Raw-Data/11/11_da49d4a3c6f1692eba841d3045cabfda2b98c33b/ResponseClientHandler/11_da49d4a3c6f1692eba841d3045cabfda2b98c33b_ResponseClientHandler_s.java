 package bomber.bombclient;
 
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * User: Eugene Shurupov
  * Date: 29.07.13
  * Time: 19:02
  */
 public class ResponseClientHandler extends ChannelInboundHandlerAdapter {
 
     private static final Logger logger = LoggerFactory.getLogger(ResponseClientHandler.class);
 
     @Override
     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
 
         if (msg instanceof HttpResponse) {
             HttpResponse response = (HttpResponse) msg;
             logger.info("http response received {}", response.getDecoderResult().toString());
         }
 
        if (msg instanceof LastHttpContent) {
            LastHttpContent content = (LastHttpContent) msg;
            logger.info("http content received {}", content.content().readableBytes());
         }
 
     }
 }
