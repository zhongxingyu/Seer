 /*
  * @(#) NonWebUIFragmentCallback.java @VERSION@
  * 
  * Copyright (c) 2008+ Amit Kumar
  * 
  * The software is released under ASL 2.0, Please
  * read License.txt
  *
  */
 
 package org.myexperiment;
 
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 import java.nio.DoubleBuffer;
 import java.util.logging.Logger;
 
 import org.gstreamer.Buffer;
 import org.gstreamer.Bus;
 import org.gstreamer.Caps;
 import org.gstreamer.Element;
 import org.gstreamer.ElementFactory;
 import org.gstreamer.Gst;
 import org.gstreamer.GstObject;
 import org.gstreamer.Pipeline;
 import org.gstreamer.State;
 import org.gstreamer.Structure;
 import org.gstreamer.elements.AppSink;
 import org.gstreamer.elements.AppSink.NEW_BUFFER;
 import org.imirsel.m2k.util.Signal;
 import org.imirsel.m2k.util.noMetadataException;
 import org.imirsel.meandre.m2k.StreamTerminator;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.ExecutableComponent;
 
 import com.sun.jna.Pointer;
 
 /** This executable component uses the Gstreamer library to stream 
  * audio accross the network
  *
  * @author Gianni O'Neill;
  *
  */
 @Component(creator="Gianni O'Neill", description="GStreamer Read", 
 		name="GStreamer Read",
 		tags="inputs audio")
 public class GStreamerRead implements ExecutableComponent {
 
 
 	@ComponentInput(description="Signal", name="signal")
 	final static String DATA_INPUT_SIGNAL = "signal";
 	
 	@ComponentOutput(description="PCM Output", name="pcm_out")
 	final static String DATA_OUTPUT_PCM = "pcm_out";
 	
 	@ComponentOutput(description="Signal Object Output", name="signal_out")
 	final static String DATA_OUTPUT_SIGNAL = "signal_out";
 	
 	@ComponentOutput(description="Sample Rate", name="rate")
 	final static String DATA_OUTPUT_RATE = "rate";
 	
 	@ComponentProperty(description="Username", 
 			name = "username",
 			defaultValue = "")
 	final static String DATA_PROPERTY_USER = "username";
 	
 	@ComponentProperty(description="Username", 
 			name = "password",
 			defaultValue = "")
 	final static String DATA_PROPERTY_PASS = "password";
 	
 	// log messages are here
 	private Logger _logger;
 	
 	
 	/** This method is invoked when the Meandre Flow is being prepared for 
 	 * getting run.
 	 *
 	 * @param ccp The properties associated to a component context
 	 */
 	public void initialize ( ComponentContextProperties ccp ) {
 		this._logger = ccp.getLogger();
 		
 	}
 	
 	private static Pipeline makePipe(final String uri, final ComponentContext cc) 
		throws URISyntaxException, ComponentContextException{
 		Pipeline pipe = new Pipeline("getaudiopipe");
 		final AppSink sink = (AppSink) ElementFactory.make("appsink", "samplesink");
 		sink.set("emit-signals", true);
 		sink.set("sync", false); //Don't run slow (stops waiting for clock synchronisation)
 		sink.setCaps(Caps.fromString("audio/x-raw-float, width=64, rate=44100, channels=1"));
 		sink.connect(new NEW_BUFFER(){
 			public void newBuffer(Element element, Pointer data) {
 				Buffer buffer = sink.pullBuffer();
 				ByteBuffer bytes = buffer.getByteBuffer();
 				DoubleBuffer doubles = bytes.asDoubleBuffer();
 				double[] samples = new double[doubles.limit()];
 				doubles.get(samples);
 				try {
 					cc.pushDataComponentToOutput(DATA_OUTPUT_PCM, samples);
 				} catch (ComponentContextException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
		String username = (String)cc.getDataComponentFromInput(DATA_PROPERTY_USER);
		String password = (String)cc.getDataComponentFromInput(DATA_PROPERTY_PASS);
 		Element src = ElementFactory.make("souphttpsrc","src");
 		src.set("user-agent","GStreamer Input Component");
 		if(!username.equals("")){
 			src.set("user-id", username);
 			src.set("user-pw", password);
 		}
 		src.set("location",uri);
 		src.set("extra-headers", Structure.fromString("extra-headers, Accept=audio/mpeg"));
 		Element decoder = ElementFactory.make("mad","decoder");
 		Element converter = ElementFactory.make("audioconvert","converter");
 		Element resampler = ElementFactory.make("audioresample","resampler");
 		
 		pipe.addMany(src, decoder, converter, resampler, sink);
 		
		pipe.getBus().connect(new Bus.EOS() {
			public void endOfStream(GstObject arg0) {
				Gst.quit();
			}
		});
 		
 		src.link(decoder);
 		decoder.link(converter);
 		converter.link(resampler);
 		resampler.link(sink);
 		
 		return pipe;
 	}
 	
 	public void execute(final ComponentContext cc) throws ComponentExecutionException, ComponentContextException {
 		Gst.init("AudioPlayer", new String[0]);
 		Object input = cc.getDataComponentFromInput(DATA_INPUT_SIGNAL);
 		if(StreamTerminator.isStreamTerminator(input)){
 			cc.pushDataComponentToOutput(DATA_OUTPUT_SIGNAL, input);
 			return;
 		}
 		
 		try {
 			Signal signal = (Signal) input;
 			signal.setMetadata(Signal.PROP_SAMPLE_RATE, new Integer(44100));
 			signal.setMetadata(Signal.PROP_FRAME_SIZE, new Integer(1152)); //this may not always hold, check!
 			signal.setMetadata(Signal.PROP_OVERLAP_SIZE, new Integer(0));
 			cc.pushDataComponentToOutput(DATA_OUTPUT_SIGNAL, signal);
 			cc.pushDataComponentToOutput(DATA_OUTPUT_RATE, new Integer(44100));
 			String uri = signal.getStringMetadata(Signal.PROP_FILE_LOCATION);
 			Pipeline pipe = makePipe(uri, cc);
 			pipe.setState(State.PLAYING);
 			Gst.main();
 			pipe.setState(State.NULL);
 			cc.pushDataComponentToOutput(DATA_OUTPUT_PCM, new StreamTerminator());
 		} catch (URISyntaxException e) {
 			cc.getOutputConsole().println("not a well formed URI.");
 			cc.requestFlowAbortion();
 			return;
 		} catch (noMetadataException e1) {
 			e1.printStackTrace();
 		}				
 			
 	}
 
 	/** This method is called when the Menadre Flow execution is completed.
 	 *
 	 * @param ccp The properties associated to a component context
 	 */
 	public void dispose ( ComponentContextProperties ccp ) {
 
 	}
 }
