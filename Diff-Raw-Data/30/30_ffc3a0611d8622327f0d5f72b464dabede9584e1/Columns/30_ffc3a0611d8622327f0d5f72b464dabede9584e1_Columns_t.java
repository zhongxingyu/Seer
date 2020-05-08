 package engine;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import util.Utils;
 
 public class Columns {
 	private final Options opt;
 	private final Field output;
 	private boolean isLearning;
 	private ArrayList<Segment>[][] segments;
 	private boolean[][][] active;
 	private boolean[][][] learn;
 	private boolean[][][] predictive;
 	private ArrayList<Synapse>[] learningCells;
 	private int time;
 	ArrayList<SegmentUpdate>[][] segmentUpdateList;
 
 	public boolean isLearning() {
 		return isLearning;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Columns(Options opt, Field output) {
 		this.opt = opt;
 		this.output = output;
 		this.time = 0;
 		isLearning = true;
 		active = new boolean[opt.SENSORS][opt.CELLS][2];
 		predictive = new boolean[opt.SENSORS][opt.CELLS][2];
 		learn = new boolean[opt.SENSORS][opt.CELLS][2];
 		learningCells = new ArrayList[2];
 		learningCells[0] = new ArrayList<Synapse>();
 		segments = new ArrayList[opt.SENSORS][opt.CELLS];
 		segmentUpdateList = new ArrayList[opt.SENSORS][opt.CELLS];
 		for (int b = 0; b < opt.SENSORS; b++) {
 			for (int c = 0; c < opt.CELLS; c++) {
 				segmentUpdateList[b][c] = new ArrayList<SegmentUpdate>();
 				segments[b][c] = new ArrayList<Segment>();
 			}
 		}
 	}
 
 	private void init() {
 		learningCells[time] = new ArrayList<Synapse>();
 		for (int i = 0; i < opt.SENSORS; i++) {
 			for (int j = 0; j < opt.CELLS; j++) {
 				active[i][j][time] = false;
 				learn[i][j][time] = false;
 				predictive[i][j][time] = false;
 			}
 		}
 	}
 	
 	public State learn(int[] activeColumns) {
 		int oldt = time;
 		time = 1 - time;
 		init();
 
 		for (int b : activeColumns) {
 			boolean lcChosen = false;
 			boolean buPredicted = false;
 			for (int c = 0; c < opt.CELLS; c++) {
 				if (predictive[b][c][oldt]) {
 					Segment s = getActiveSegment(b, c, oldt);
 					if (s != null && s.sequenceSegment) {
 						buPredicted = true;
 						active[b][c][time] = true;
 						if (getLearnActiveSize(s, oldt) > opt.ACTIVATION_THRESHOLD) {
 							lcChosen = true;
 							learn[b][c][time] = true;
 							learningCells[time].add(new Synapse(b, c));
 						}
 					}
 				}
 			}
 			if (!buPredicted) {
 				for (int c = 0; c < opt.CELLS; c++) {
 					active[b][c][time] = true;
 				}
 			}
 			if (!lcChosen) {
 				int c = getBestMatchingCell(b, oldt);
 				learn[b][c][time] = true;
 				learningCells[time].add(new Synapse(b, c));
 				SegmentUpdate sUpdate = getSegmentActiveSynapses(null, oldt, true);
 				sUpdate.sequenceSegment = true;
 				segmentUpdateList[b][c].add(sUpdate);
 			}
 		}
 		for (int b = 0; b < opt.SENSORS; b++) {
 			for (int c = 0; c < opt.CELLS; c++) {
 				for (Segment s : segments[b][c]) {
 					if (getActiveSizeOfConnectedSynapses(s, time) > opt.ACTIVATION_THRESHOLD) {
 						predictive[b][c][time] = true;
 						SegmentUpdate activeUpdate = getSegmentActiveSynapses(s, time, false);
 						segmentUpdateList[b][c].add(activeUpdate);
 						Segment predSegment = getBestMatchingSegment(b, c, oldt);
 						SegmentUpdate predUpdate = getSegmentActiveSynapses(predSegment, oldt, true);
 						segmentUpdateList[b][c].add(predUpdate);
 					}
 				}
 			}
 		}
 		for (int b = 0; b < opt.SENSORS; b++) {
 			for (int c = 0; c < opt.CELLS; c++) {
 				if (learn[b][c][time]) {
 					adaptSegments(b, c, true);
 					segmentUpdateList[b][c].clear();
 				} else if (!predictive[b][c][oldt] && predictive[b][c][time]) {
 					adaptSegments(b, c, false);
 					segmentUpdateList[b][c].clear();
 				}
 			}
 		}
 		set_output();
 		return State.TRAIN;
 	}
 
 	private void set_output() {
 		for (int b = 0; b < opt.SENSORS; b++) {
 			for (int c = 0; c < opt.CELLS; c++) {
 				output.set(b, c, active[b][c][time] || predictive[b][c][time]);
 			}
 		}
 	}
 
 	public State run(int[] activeColumns) {
 		int oldt = time;
 		time = 1 - time;
 		init();
 		for (int b : activeColumns) {
 			boolean buPredicted = false;
 			for (int c = 0; c < opt.CELLS; c++) {
 				if (predictive[b][c][oldt]) {
 					Segment s = getActiveSegment(b, c, oldt);
 					if(s != null && s.sequenceSegment){
 						buPredicted = true;
 						active[b][c][time] = true;
 					}
 				}
 			}
 			if(!buPredicted){
 				for (int c = 0; c < opt.CELLS; c++) {
 					active[b][c][time] = true;
 				}				
 			}
 		}
 		for (int b = 0; b < opt.SENSORS; b++) {
 			for (int c = 0; c < opt.CELLS; c++) {
 				for (Segment s: segments[b][c]) {
 					if(getActiveSizeOfConnectedSynapses(s, time) > opt.ACTIVATION_THRESHOLD){
 						predictive[b][c][time] = true;
 					}
 				}
 			}
 		}
 		set_output();
 		return State.LEARNED;
 	}
 
 	private void adaptSegments(int b, int c, boolean positiveReinforcement) {
 		for (SegmentUpdate segmentUpdate : segmentUpdateList[b][c]) {
 			Segment targetSegment = segmentUpdate.segment;
 			if (targetSegment == null) {
 				addSegment(b, c, segmentUpdate);
 				continue;
 			}
 			HashSet<Synapse> synapses = targetSegment.synapses;
 			HashSet<Synapse> possibleSynapses = targetSegment.possibleSynapses;
 			HashSet<Synapse> updates = new HashSet<Synapse>(segmentUpdate.updatedSynapses);
 			if (positiveReinforcement) {
 				for (Synapse s: possibleSynapses) {
 					if (updates.contains(s)) {
 						s.addPermanence(opt.PERMANENCE_INC);
 						updates.remove(s);
 					} else {
 						int permanence = s.decPermanence(opt.PERMANENCE_DEC);
 						if (permanence < opt.PERMANENCE_CONNECTED) {
 							synapses.remove(s);
 						}
 					}
 				}
 				for (Synapse new_synapse : updates) {
 					if (new_synapse.isNew()) {
 						new_synapse.setPermanence(opt.PERMANENCE_INITIAL);
 						synapses.add(new_synapse);
						possibleSynapses.add(new_synapse);
 					}
 				}
 			} else {
 				for (Synapse s: possibleSynapses) {
 					if (updates.contains(s)) {
 						int permanence = s.decPermanence(opt.PERMANENCE_DEC);
 						if (permanence < opt.PERMANENCE_CONNECTED) {
 							synapses.remove(s);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void addSegment(int b, int c, SegmentUpdate segmentUpdate) {
 		if (!segmentUpdate.updatedSynapses.isEmpty()) {
 			Segment segment = new Segment();
 			segment.sequenceSegment = segmentUpdate.sequenceSegment;
 			segment.synapses = segmentUpdate.updatedSynapses;
			segment.possibleSynapses = new HashSet<Synapse>(segmentUpdate.updatedSynapses);
 			for (Synapse s : segment.synapses) {
 				s.setPermanence(opt.PERMANENCE_INITIAL);
 			}
 			segments[b][c].add(segment);
 		}
 	}
 
 	private Segment getBestMatchingSegment(int b, int c, int time) {
 		int pretender_size = opt.MIN_THRESHOLD;
 		Segment pretender = null;
 		for (Segment segment : segments[b][c]) {
 			// RECHECK THIS
 			int size = getActiveSize(segment, time);
 			if (size > pretender_size) {
 				pretender = segment;
 				pretender_size = size;
 			}
 		}
 		return pretender;
 	}
 
 	private int getActiveSize(Segment segment, int time) {
 		int count = 0;
 		for (Synapse synapse : segment.possibleSynapses) {
 			if (active[synapse.bit][synapse.cell][time]) {
 				count += 1;
 			}
 		}
 		return count;
 	}
 
 	private int getBestMatchingCell(int b, int time) {
 		int champion_size = opt.MIN_THRESHOLD;
 		int champion_cell = -1;
 		for (int c = 0; c < opt.CELLS; c++) {
 			for (Segment segment : segments[b][c]) {
 				int size = getActiveSize(segment, time);
 				if (size > champion_size) {
 					champion_cell = c;
 					champion_size = size;
 				}
 			}
 		}
 		if (champion_cell == -1) {
 			return getCellWithFewestSegments(b);
 		}
 		return champion_cell;
 	}
 
 	private int getCellWithFewestSegments(int b) {
 		int champion_size = segments[b][0].size();
 		int champion_cell = 0;
 		for (int c = 1; c < opt.CELLS; c++) {
 			int pretender_size = segments[b][c].size();
 			if (pretender_size < champion_size) {
 				champion_cell = c;
 				champion_size = pretender_size;
 			}
 		}
 		return champion_cell;
 	}
 
 	private SegmentUpdate getSegmentActiveSynapses(Segment segment, int time, boolean newSynapses) {
 		SegmentUpdate update = new SegmentUpdate();
 		HashSet<Synapse> items = new HashSet<Synapse>();
 		if (segment != null) {
 			for (Synapse synapse : segment.synapses) {
 				if (active[synapse.bit][synapse.cell][time]) {
 					items.add(synapse);
 				}
 			}
 		}
 		update.segment = segment;
 		update.updatedSynapses = items;
 		if (!newSynapses) {
 			return update;
 		}
 		int to_add = opt.NEW_SYNAPSES - items.size();
 		ArrayList<Synapse> choices = learningCells[time];
 		if (to_add > 0) {
 			Collection<Integer> samples = Utils.sample(choices.size(), Math.min(to_add, choices.size()));
 			for (int index : samples) {
 				Synapse proto = choices.get(index);
 				items.add(new Synapse(proto.bit, proto.cell));
 			}
 		}
 		return update;
 	}
 
 	private int getActiveSizeOfConnectedSynapses(Segment segment, int time) {
 		int count = 0;
 		for (Synapse synapse : segment.synapses) {
 			if (active[synapse.bit][synapse.cell][time]) {
 				count += 1;
 			}
 		}
 		return count;
 	}
 
 	private int getLearnActiveSize(Segment segment, int time) {
 		int count = 0;
 		for (Synapse synapse : segment.synapses) {
 			if (learn[synapse.bit][synapse.cell][time]) {
 				count += 1;
 			}
 		}
 		return count;
 	}
 
 	private Segment getActiveSegment(int bit, int cell, int time) {
 		Segment pretender = null;
 		int pretender_value = -1;
 		boolean pretender_sequence = false;
 		for (Segment segment : segments[bit][cell]) {
 			int value = getActiveSizeOfConnectedSynapses(segment, time);
 			if (value > opt.ACTIVATION_THRESHOLD) {
 				if (pretender_sequence && !segment.sequenceSegment)
 					continue;
 				if (segment.sequenceSegment && !pretender_sequence) {
 					pretender = segment;
 					pretender_value = value;
 					pretender_sequence = true;
 				} else if (value > pretender_value) {
 					pretender = segment;
 					pretender_value = value;
 				}
 			}
 		}
 		return pretender;
 	}
 }
