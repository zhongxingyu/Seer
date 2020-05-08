 package us.exultant.ahs.io;
 
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.thread.*;
 import java.io.*;
 import java.nio.channels.*;
 import org.slf4j.*;
 
 class WorkTargetChannelCloser extends WorkTargetAdapterFollowup<Void> {
 	WorkTargetChannelCloser(InputSystem<?> $iosys) {
 		super($iosys.getFuture(), 0);
 		this.$channel = $iosys.getChannel();
 	}
 	
 	WorkTargetChannelCloser(OutputSystem<?> $iosys) {
 		super($iosys.getFuture(), 0);
 		this.$channel = $iosys.getChannel();
 	}
 	
 	private final Channel $channel;
 	
 	protected Void run() throws IOException {
		assert new Loggar(LoggerFactory.getLogger(WorkTargetChannelCloser.class)).debug("closing channel {}", $channel);
 		$channel.close();
 		return null;
 	}
 }
