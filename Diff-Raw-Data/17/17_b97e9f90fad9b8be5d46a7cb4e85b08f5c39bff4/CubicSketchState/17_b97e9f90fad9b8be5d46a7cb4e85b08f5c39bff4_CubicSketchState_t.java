 package gui.cubic;
 
 import gui.Legend;
 import gui.PlayState;
 import gui.Utils;
 import ijeoma.motion.Motion;
 import ijeoma.motion.tween.Tween;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import processing.core.PApplet;
 import processing.core.PGraphics;
 import processing.core.PVector;
 import snapshot.Snapshot;
 import timeline.*;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 
 /**
  * The SketchState is a background thread responsible for drawing things which are relatively static to a buffer. 
  * It has its own animation loop which tracks the timeline of the visualization at whatever rate the user selects. 
  * Because it is decoupled from the main animation loop, it has no specific timing it has to meet, so it can be 
  * relatively slow and precise. All of its drawing is done with pixel precision, using Java 2D instead of OpenGL. 
  * 
  * Anything which needs to be animated smoothly (e.g. job tweening) is left up to the enclosing sketch, and its 
  * main animation loop. 
  * 
  * @author <a href="mailto:krokicki@gmail.com">Konrad Rokicki</a>
  */
 public class CubicSketchState implements Runnable {
 
     private static final Logger log = LoggerFactory.getLogger(CubicSketchState.class);
 
     // Timing
     private static final float DURATION_JOB_SUB = 40.0f;
     private static final float DURATION_JOB_START = 20.0f;
     private static final float DURATION_JOB_END = 70.0f;
     private static final float DISTANCE_JOB_START = 50.0f;
 
     // Invariants
     private final float width;
     private final float height;
     private final int slotsPerNode = 8;
     
     // Sizing variables
     private float nodeSize = 40;
     private float nodePadding = 5;
     private float jobPadding = 2;
     private int nodeLatSides = (int)Math.ceil(Math.pow(slotsPerNode,1/3.0));
     private float jobSize = (nodeSize - nodeLatSides * jobPadding) / nodeLatSides;
     
     private PApplet p;
     
     // Actors
     private Multimap<String, JobActor> jobActorMap = Multimaps.synchronizedMultimap(HashMultimap.<String, JobActor>create()); 
     
     // Overall state
     private Legend legend;
     private Timeline timeline;
     
     // State for playing
     private PlayState playState = PlayState.PAUSED;
     private GridState state;
     private double playSpeed = 10.0f;
     private long totalElapsed = 0;
     private Date lastSliceRequestDate;
     private long nextStartingPosition = 0;
 
     // Should changes to actors be tweened? This is usually disabled during buffering, for example.
     private boolean tweenChanges = true;
     
     
     public CubicSketchState(PApplet p, Timeline timeline, float width, float height) {
         this.p = p;
         this.width = width;
         this.height = height;
         this.timeline = timeline;
     }
     
     @Override
     public void run() {
         
         while (true) {
             
             switch (playState) {
             
             case BUFFERING:
                 bufferToNextPosition();
                 break;
                 
             case PLAYING:
                 Date currDate = new Date();
                 long elapsed = (int)((currDate.getTime() - lastSliceRequestDate.getTime()) * playSpeed);
                 this.lastSliceRequestDate = currDate;
                 
                 // Check if we've been truncated, and move forward if necessary
                 if (totalElapsed < timeline.getFirstOffset()) {
                     log.info("Elapsed time ({}) occurs before the current timeline ({})",elapsed,timeline.getFirstOffset());
                     
                     long position = elapsed;
                     boolean noMatch = true;
                     while (noMatch) {
                         if (position != timeline.getFirstOffset()) {
                             position = timeline.getFirstOffset();
                             try {
                                 Thread.sleep(500);  
                             }
                             catch (InterruptedException e) {
                                 // Ignore
                             }
                         }
                         else {
                             noMatch = false;
                         }
                     }
                     log.info("Will buffer to new position: {}",position);
                     bufferAtPosition(position);
                     break;
                 }
 
                 // Update actors and build a usage map
                 Map<String,Integer> slotsUsedByUser = new HashMap<String,Integer>();
                 Iterator<? extends Actor> i = getJobActors().values().iterator();
                 while (i.hasNext()) {
                     Actor actor = i.next();
                     if (actor instanceof JobActor) {
                         JobActor jobActor = (JobActor)actor;
                         if (jobActor.defunct) {
                             removeJobActor(jobActor.name, jobActor);
                         }
                         else {
                             // Parse a jobId like this: 1275988.2828-4000:1
                             int slots = 1;
                             if (jobActor.queued) {
                                 // If a job is queued then it is represented by a single sprite, so we need the
                                 // actual number of slots
                                 GridJob job = state.getJobByFullId(jobActor.name);
                                 if (job!=null) {
                                     slots = job.getSlots();
                                 }
                             }
                             
                             if (jobActor.getName().contains(":")) {
                                 try {
                                     Pattern p = Pattern.compile("(\\d+)\\.(\\d+)-(\\d+):(\\d+)");
                                     Matcher m = p.matcher(jobActor.getName());
                                     if (m.matches()) {
                                         int start = Integer.parseInt(m.group(2));
                                         int end = Integer.parseInt(m.group(3));
                                         int interval = Integer.parseInt(m.group(4));
                                         slots = (end-start)/interval;
                                     }
                                 } 
                                 catch (Exception e) {
                                     log.error("Error parsing jobId: "+jobActor.getName(),e);
                                 }
                             }
 
                             String user = jobActor.getUsername();
                             if (!slotsUsedByUser.containsKey(user)) {
                                 slotsUsedByUser.put(user, 0);
                             }
                             if (!jobActor.queued) {
                                 slotsUsedByUser.put(user, slotsUsedByUser.get(user)+slots);
                             }
                         }
                     }
                 }
                 
                 legend.retain(slotsUsedByUser);
                 
                 updateState(elapsed);
                 
                 break;
 
             case READY:
                 break;
                 
             case PAUSED:
                 break;
 
             case END:
                 return;
                 
             default:
                 log.error("Invalid play state: "+playState);
                 break;
                 
             }
             
             try {
                 Thread.sleep(50);   
             }
             catch (InterruptedException e) {
                 // Ignore
             }
         }
     }
     
     private void bufferToNextPosition() {
 
         log.info("Buffering to next position: {}",nextStartingPosition);
         
         int i=0;
         Snapshot reqSnapshot = null;
         Snapshot prevSnapshot = null;
         for(Snapshot snapshot : timeline.getSnapshots()) {
             long offset = timeline.getOffset(snapshot.getSamplingTime());
             
             log.info("Snapshot {} has offset {}",i,offset);
             
             if (offset>=nextStartingPosition) {
                 if (prevSnapshot==null) {
                     reqSnapshot = snapshot;
                 }
                 else {
                     reqSnapshot = prevSnapshot;
                 }
                 break;
             }
             prevSnapshot = snapshot;
             i++;
         }
         
         if (reqSnapshot==null) {
             reqSnapshot = prevSnapshot;
             if (reqSnapshot==null) {
                 log.error("Could not find snapshot for offset {}",nextStartingPosition);
                 setPlayState(PlayState.PAUSED);
                 return;
             }
         }
         
         // Initialize the state at the closest possible snapshot
         log.info("Init with snapshot with offset {}",timeline.getOffset(reqSnapshot.getSamplingTime()));
         
         this.state = new GridState(reqSnapshot,"runState");
         initState();
         
         // Apply all events between the closet snapshot and the desired starting position
         this.totalElapsed = timeline.getOffset((reqSnapshot.getSamplingTime()))+1;
         long elapsed = nextStartingPosition - totalElapsed;
 
         log.info("Buffering elapsed: {}",elapsed);
         this.tweenChanges = false;
         updateState(elapsed);
         this.tweenChanges = true;
         
         // Get ready to start playing
         this.lastSliceRequestDate = new Date();
         if (totalElapsed != nextStartingPosition) {
             totalElapsed = nextStartingPosition;
         }
 
         setPlayState(PlayState.READY);
         log.info("Buffered at totalElapsed={}",totalElapsed);
     }
     
     public synchronized void bufferAtPosition(long position) {
         if (playState==PlayState.PAUSED) {
             setPlayState(PlayState.BUFFERING);
             this.nextStartingPosition = position;
         }
         else {
             log.error("Cannot transition from "+playState+" to BUFFERING");
         }
     }
     
     public synchronized void play() {
         if (playState==PlayState.PAUSED || playState==PlayState.READY) {
             log.info("Beginning playback at totalElapsed={}",totalElapsed);
             this.lastSliceRequestDate = new Date();
             setPlayState(PlayState.PLAYING);
         }
         else {
             log.error("Cannot transition from "+playState+" to PLAYING");
         }
     }
     
     public synchronized void pause() {
         if (playState==PlayState.PLAYING || playState==PlayState.PAUSED) {
             setPlayState(PlayState.PAUSED);
         }
         else {
             log.error("Cannot transition from "+playState+" to PAUSED");
         }
     }
     
     public synchronized void end() {
         setPlayState(PlayState.END);
     }
     
     private void setPlayState(PlayState playState) {
         log.info("Entering state: {}",playState);
         this.playState = playState;
     }
 
     public boolean isPaused() {
         return playState==PlayState.PAUSED;
     }
     
     public boolean isReady() {
         return playState==PlayState.READY;
     }
     
     public boolean isPlaying() {
         return playState==PlayState.PLAYING;
     }
     
     public boolean isBuffering() {
         return playState==PlayState.BUFFERING;
     }
 
     public boolean isEnded() {
         return playState==PlayState.END;
     }
     
     public double getPlaySpeed() {
         return playSpeed;
     }
 
     public void setPlaySpeed(double playSpeed) {
         this.playSpeed = playSpeed;
     }
 
     public long getPosition() {
         return totalElapsed;
     }
     
     /**
      * Transliterated from Python solution available here: 
      * http://stackoverflow.com/questions/6463297/algorithm-to-fill-rectangle-with-small-squares
      */
     private int bestSquare(float w, float h, float n) {
         float hi = Math.max(w, h);
         float lo = 0;
         while (Math.abs(hi - lo) > 0.0001) {
             float mid = (lo+hi)/2;
             float midval = (float)Math.floor(w / mid) * (float)Math.floor(h / mid);
             if (midval >= n) {
                 lo = mid;
             }
             else if (midval < n) {
                 hi = mid;
             }
         }
         return (int)Math.min(w/Math.floor(w/lo), h/Math.floor(h/lo));
     }
     
     public class Lattice<T> {
     	
     	int size;
     	Map<String, PVector> locationMap = new HashMap<String, PVector>();
     	Map<String, T> contentsMap = new HashMap<String, T>();
     	
     	public Lattice(int size) {
     		this.size = size;
     	}
     	
     	public void addItems(List<String> names, List<T> items) {
     		if (!locationMap.isEmpty()) {
     			throw new IllegalStateException("addItems can only be called once for any given Lattice");
     		}
     		Queue<String> nameQueue = new LinkedList<String>(names);
     		Queue<T> itemQueue = new LinkedList<T>(items);
     		for(int i=0; i<size; i++) {
         		for(int j=0; j<size; j++) {
             		for(int k=0; k<size; k++) {
     					if (nameQueue.isEmpty() || itemQueue.isEmpty()) return;
     					String name = nameQueue.remove();
     					T item = itemQueue.remove();
     					locationMap.put(name, new PVector(i, j, k));
     					contentsMap.put(name, item);
     				}
     			}
     		}
     		
     		if (!nameQueue.isEmpty()) {
     			log.warn("Could not add all items to lattice of size "+size+". Items remaining: "+nameQueue.size());
     		}
     	}
     	
     	public PVector getLocation(String name) {
     		return locationMap.get(name);
     	}
 
     	public T getContents(String name) {
     		return contentsMap.get(name);
     	}
     }
     
     private Lattice<Lattice<Void>> lattice;
     
 
     private void initState() {
 
     	int numNodes = state.getNodeMap().size();
     	int numSlots = numNodes * slotsPerNode;
     	
         this.legend = new Legend(null, null, 0);
         
     	int latSides = (int)Math.ceil(Math.pow(numNodes,1/3.0));
     	log.info("Lattice of "+numNodes+" requires "+latSides+" per side");
     	this.lattice = new Lattice<Lattice<Void>>(latSides);
     	
     	List<String> nodeNames = new ArrayList<String>();
     	List<Lattice<Void>> nodeLatti = new ArrayList<Lattice<Void>>();
     	for(GridNode node : state.getNodeMap().values()) {
     		Lattice<Void> nodeLattice = new Lattice<Void>(nodeLatSides);
 
         	List<String> slotNames = new ArrayList<String>();
         	List<Void> slots = new ArrayList<Void>();
         	for(int i=0; i<slotsPerNode; i++) {
         		slotNames.add(""+i);
         		slots.add(null);
         	}
         	nodeLattice.addItems(slotNames, slots);
     		
     		nodeNames.add(node.getShortName());
     		nodeLatti.add(nodeLattice);
         	//log.warn("Adding lattice for node "+node.getShortName());
     	}
     	
     	lattice.addItems(nodeNames, nodeLatti);
     	
         // Initialize actors from the grid state
     	for(GridNode node : state.getNodeMap().values()) {
             
     		int s = 0;
             for(GridJob job : node.getSlots()) {
             	if (job==null) continue;
                 String slotName = s+"";
                 JobActor jobActor = createJobActor(job);
                 jobActor.pos = getLatticePos(node.getShortName(), slotName);
                 log.info("Starting job {} on slot: {}",job.getFullJobId(),node.getShortName()+"#"+slotName);
                 addJobActor(job.getFullJobId(), jobActor);
                 s++;
             }
         }
     }
     
     private void updateState(long elapsed) {
         // Update the job actors
         List<GridEvent> events = getNextSlice(elapsed);
         for(GridEvent event : events) {
             applyEvent(event);
         }
     }
     
     private List<GridEvent> getNextSlice(long elapsed) {
         
         List<GridEvent> slice = new ArrayList<GridEvent>();
         if (elapsed<=0) return slice;
         
         long prevElapsed = totalElapsed;
         totalElapsed += elapsed;
         
         log.trace("getNextSlice, prevElapsed={}, totalElapsed={}",prevElapsed,totalElapsed);
         
         if (prevElapsed>=totalElapsed) {
             log.warn("No slice possible with (prevElapsed={}, totalElapsed={})",prevElapsed,totalElapsed);
             return slice;
         }
         
         SortedMap<Long,List<GridEvent>> eventSlice = timeline.getEvents().subMap(prevElapsed, totalElapsed);
         
         log.trace("getNextSlice, eventSlice.size={}",eventSlice.size());
         
         for(Long offset : eventSlice.keySet()) {
             List<GridEvent> events = eventSlice.get(offset);
             for(GridEvent event : events) {
                 if (event.getOffset()>=totalElapsed) {
                     break;
                 }
                 log.trace("getNextSlice\t{}\t{}",event.getOffset(),event.getCacheKey());
                 slice.add(event);
             }
         }
         
         return slice;
     }
     
     private void applyEvent(GridEvent event) {
 
         String fullJobId = event.getJobId();
         GridJob job = state.getJobByFullId(fullJobId);
         
         // Update the run state
         state.applyEvent(event);
         
         // Update the actor state
         switch(event.getType()) {
         case SUB:
             if (job==null) {
                 job = new GridJob(event.getSnapshotJob());
             }
             applySub(job);  
             break;
         case START:
             if (job==null) {
                 log.error("Cannot start null job");
             }
             else {
                 applyStart(job);    
             }
             break;
         case END:
             if (job==null) {
                 log.error("Cannot end null job");
             }
             else {
                 applyEnd(job, event.getOffset());   
             }
             break;
         default:
             log.warn("Unrecognized event type: {}",event.getType());
             break;
         }
     }
     
     private void applySub(GridJob job) {
 
 //        String fullJobId = job.getFullJobId();
 //        Collection<JobActor> actors = jobActorMap.get(fullJobId);
 //        if (actors!=null && !actors.isEmpty()) {
 //            log.warn("Ignoring sub event for known job {}",fullJobId);
 //            return;
 //        }
 //
 //        log.debug("Adding queued job {}",fullJobId);
     }
     
     private void applyStart(GridJob job) {
 
         String fullJobId = job.getFullJobId();
         Collection<JobActor> actors = jobActorMap.get(fullJobId);
         if (actors.isEmpty()) {
             JobActor jobActor = createJobActor(job);
             addJobActor(fullJobId, jobActor);
             actors = jobActorMap.get(fullJobId);
         }
 
         if (job.getNode()==null) {
             log.warn("No node for job being started: {}",fullJobId);
             return;
         }
         
         log.debug("Starting job {} on {}",fullJobId,job.getNode().getShortName());
         
         String nodeName = job.getNode().getShortName();
         if (actors.size()>1) {
             log.warn("More than one actor for job being started: "+fullJobId);
         }
         
         JobActor jobActor = actors.iterator().next();
         
         int i = 0;
         GridJob[] nodeJobs = job.getNode().getSlots();
         for(int s=0; s<nodeJobs.length; s++) {
         	GridJob nodeJob = nodeJobs[s];
             if (nodeJob==null) continue;
             if (!nodeJob.getFullJobId().equals(fullJobId)) continue;
             
             if (i>0) {
                 jobActor = cloneJobActor(fullJobId);
             }
 
             PVector endPos = getLatticePos(nodeName, s+"");
             // TODO: random outside location in direction of vector from 0,0,0
             PVector startPos = new PVector(1000, 1000, 1000); 
             jobActor.pos = startPos;
             
             if (tweenChanges) {
                 // scale duration to the distance that needs to be traveled
                 float distance = jobActor.pos.dist(endPos);
                 float duration = (DURATION_JOB_START * distance / DISTANCE_JOB_START) * 0.6f;
                 
                 Tween tween = new Tween("start_job_"+fullJobId+"#"+i,getTweenDuration(duration))
                     .addPVector(jobActor.pos, endPos)
                     .call(jobActor, "jobStarted")
                     .setEasing(Tween.SINE_OUT)
                     .noAutoUpdate(); 
 
                 jobActor.tweens.add(tween);
             }
             else {
                 jobActor.pos.set(endPos);
             }
             
             i++;
         }
     }
     
     private void applyEnd(GridJob job, long endOffset) {
 
         String fullJobId = job.getFullJobId();
         Collection<JobActor> actors = jobActorMap.get(fullJobId);
         if (actors==null || actors.isEmpty()) {
             log.warn("Cannot end job that does not exist: {}",fullJobId);
             return;
         }
         
         log.debug("Finishing job {} on node {}",fullJobId, job.getNode().getShortName());
 
         int i = 0;
         for(JobActor jobActor : actors) {
 
             PVector endPos = jobActor.getPos().get();
             endPos.mult(10.0f);
             
             if (tweenChanges) {
                 // scale duration to the distance that needs to be traveled
                 float distance = jobActor.pos.dist(endPos);
                 float duration = (DURATION_JOB_END * distance / DURATION_JOB_END) * 0.6f;
                 
                 Tween tween = new Tween("end_job_"+fullJobId+"#"+i,getTweenDuration(duration))
                     .addPVector(jobActor.pos, endPos)
                     .call(jobActor, "jobEnded")
                     .setEasing(Tween.SINE_IN)
                     .noAutoUpdate();
                 jobActor.tweens.add(tween);
             }
             else {
                 jobActor.jobEnded();
             }
             
             i++;
         }
     }
     
     private JobActor createJobActor(GridJob job) {
     	JobActor jobActor = new JobActor(null, job.getOwner());
         jobActor.color = legend.getItemColor(job.getOwner());
         jobActor.name = job.getFullJobId();
         return jobActor;
     }
 
     private void addJobActor(String fullJobId, JobActor jobActor) {
     	jobActorMap.put(fullJobId, jobActor);
     }
     
     private void removeJobActor(String fullJobId, JobActor jobActor) {
     	jobActorMap.remove(fullJobId, jobActor);
     }
 
     private JobActor cloneJobActor(String fullJobId) {
         Collection<JobActor> actors = jobActorMap.get(fullJobId);
         if (actors==null || actors.isEmpty()) {
             log.error("Cannot expand actors for non-existing job: "+fullJobId);
             return null;
         }
         
         JobActor jobActor = actors.iterator().next(); // first actor
         GridJob job = state.getJobByFullId(fullJobId);
         
         if (job==null) {
             log.error("Cannot expand actors for unknown job: "+fullJobId);
             return null;
         }
         
         JobActor copy = jobActor.copy();
         copy.name += "#"+actors.size();
         jobActorMap.put(fullJobId, copy);
         return copy;
     }
 
     private PVector getLatticePos(String nodeName, String slotName) {
 
         PVector nodeLoc = lattice.getLocation(nodeName);
         Lattice<Void> slotLat = lattice.getContents(nodeName);
         
         if (slotLat==null) {
         	log.warn("No slot lattice for node "+nodeName);
        	return new PVector(0,0,0);
         }
         
        // Node position within the lattice, natural coordinates
        float nodeOffset = nodeSize + 2 * nodePadding;
        PVector nodePos = nodeLoc.get();
        nodePos.mult(nodeOffset);
        nodePos.add(nodePadding, nodePadding, nodePadding);
        
         PVector slotLoc = slotLat.getLocation(slotName);
         if (slotLoc==null) {
         	log.warn("No slot location for slot "+slotName+" in node "+nodeName);
        	return nodePos.get();
         }
         
         // TODO: cache these calculations
         
         // Slot position within the node, natural coordinates
         float slotOffset = jobSize + 2 * jobPadding;
         PVector slotPos = slotLoc.get();
         slotPos.mult(slotOffset);
         slotPos.add(jobPadding, jobPadding, jobPadding);
         
         // Combine node and slot position, natural coordinates
         PVector pos = slotPos.get();
         pos.add(nodePos);
         
         // Convert into central coordinate system
         float latticeWidth = lattice.size * nodeOffset;
         float t = latticeWidth/2;
         pos.sub(t, t, t);
         
         return pos;
     }
     
     private float getTweenDuration(float durationJobSub) {
         if (playSpeed<=1) return durationJobSub;
         return durationJobSub/(float)Math.log(playSpeed);
     }
     
     public class Actor {
 
     	protected PVector pos;
     	
         public Actor(PVector pos) {
             this.pos = pos;
         }
         
         public PVector getPos() {
             return pos;
         }
 
         public void setPos(PVector pos) {
             this.pos = pos;
         }
     }
     
     public class JobActor extends Actor {
 
         protected String name;
         protected String username;
         protected int color = Utils.color("ff0000");
         protected float opacity = 150;
         protected  Set<Motion> tweens = new HashSet<Motion>();
         protected boolean queued = false;
         protected boolean defunct = false;
         
         JobActor(PVector pos, String username) {
             super(pos);
             this.username = username;
         }
 
         public void update() {
             if (isInMotion()) {
                 Set<Motion> toRemove = new HashSet<Motion>();
                 for(Motion m : tweens) {
                     if (!m.isPlaying()) {
                         if (m.getPosition()==0) {
                             m.play();
                         }
                         else {
                             toRemove.add(m);    
                         }
                     }
                     else {
                         m.update();
                     }
                 }
                 for (Motion m : toRemove) {
                     tweens.remove(m);
                 }       
             }
         }
 
         public boolean isInMotion() {
             return !tweens.isEmpty();
         }
         
         public void draw(PGraphics buf) { 
 
         	buf.pushMatrix();
             buf.stroke(Utils.color("ffffff"), opacity);
         	buf.strokeWeight(1);
             buf.fill(color, opacity);
         	buf.translate(pos.x, pos.y, pos.z);
             buf.box(jobSize);
             buf.popMatrix();
         }
 
         public JobActor copy() {
         	JobActor copy = new JobActor(pos, username);
             copy.name = name;
             copy.color = this.color;
             return copy;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
         
         public String getUsername() {
             return username;
         }
 
         public float getOpacity() {
             return opacity;
         }
 
         public void setOpacity(float opacity) {
             this.opacity = opacity;
         }
         
         public Set<Motion> getTweens() {
             return tweens;
         }
         
         public boolean isDefunct() {
         	return defunct;
         }
         
         public void jobStarted() {
         }
         
         public void jobEnded() {
             defunct = true;
             color = Utils.color("000000");
 //            opacity = 0; // just in case
         }
     }
     
     public Multimap<String, JobActor> getJobActors() {
         synchronized (jobActorMap) {
             return ImmutableMultimap.copyOf(jobActorMap);
         }
     }
     
     public void setTimeline(Timeline timeline) {
         this.timeline = timeline;
     }
 }
