 package ch.uzh.ddis.katts.bolts;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import backtype.storm.topology.OutputFieldsDeclarer;
 import backtype.storm.topology.base.BaseRichBolt;
 import backtype.storm.tuple.Fields;
 import backtype.storm.tuple.Tuple;
 import ch.uzh.ddis.katts.query.stream.Stream;
 import ch.uzh.ddis.katts.query.stream.StreamConsumer;
 import ch.uzh.ddis.katts.query.stream.Variable;
 
 public abstract class AbstractBolt extends BaseRichBolt implements Bolt {
 
 	private static final long serialVersionUID = 1L;
 
 	private Map<String, StreamConsumer> streamConsumer = new LinkedHashMap<String, StreamConsumer>();
 
 	/*
 	 * This map holds a reference to all outgoing streams of this bolt. The key of the map is the id of the
 	 * stream while the stream is the actual object representation of the outgoing stream.
 	 */
 	private Map<String, Stream> streams = new LinkedHashMap<String, Stream>();
 
 	private OutputCollector collector;
 
 	private Emitter emitter = null;
 
 	@Override
 	public void prepare(Map stormConf, TopologyContext context,
 			OutputCollector collector) {
 		this.setCollector(collector);
 	}
 
 	@Override
 	public void execute(Tuple input) {
 		execute(createEvent(input));
 	}
 
 	public Event createEvent(Tuple input) {
 		StreamConsumer emittedOn = this.streamConsumer.get(input
 				.getSourceStreamId());
 		return new Event(input, this, emittedOn);
 	}
 
 	@Override
 	public abstract void execute(Event event);
 
 	@Override
 	public void ack(Event event) {
 		this.getCollector().ack(event.getTuple());
 	}
 
 	public Emitter getEmitter() {
 		if (this.emitter == null) {
 			this.emitter = new Emitter(this);
 		}
 		return this.emitter;
 	}
 
 	@Override
 	public void declareOutputFields(OutputFieldsDeclarer declarer) {
 		for (Stream stream : this.getStreams()) {
 			List<String> fields = new ArrayList<String>();
 			fields.add("sequenceNumber");
 			fields.add("startDate");
 			fields.add("endDate");
 			fields.addAll(Variable.getFieldList(stream.getAllVariables()));
 			declarer.declareStream(stream.getId(), new Fields(fields));
 		}
 	}
 
 	@Override
 	public Collection<StreamConsumer> getStreamConsumer() {
 		return streamConsumer.values();
 	}
 
 	@Override
 	public void setConsumerStreams(Collection<StreamConsumer> streamConsumers) {
 		streamConsumer = new HashMap<String, StreamConsumer>();
 		for (StreamConsumer stream : streamConsumers) {
			if (stream.getStream() == null) {
				throw new NullPointerException("The given consumer stream is not linked back to the producing stream. Check if there is a bolt that consums a stream, which is not defined.");
			}
 			streamConsumer.put(stream.getStream().getId(), stream);
 		}
 	}
 
 	@Override
 	public Collection<Stream> getStreams() {
 		return streams.values();
 	}
 
 	@Override
 	public void setStreams(Collection<Stream> streams) {
 		this.streams = new HashMap<String, Stream>();
 		for (Stream stream : streams) {
 			this.streams.put(stream.getId(), stream);
 		}
 	}
 
 	@Override
 	public OutputCollector getCollector() {
 		return collector;
 	}
 
 	public void setCollector(OutputCollector collector) {
 		this.collector = collector;
 	}
 
 }
