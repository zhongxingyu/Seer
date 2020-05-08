 package ca.ilanguage.oprime.content;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import ca.ilanguage.oprime.R;
 
 public class Stimulus implements Serializable{
 	
 	private static final long serialVersionUID = -4023355491498842498L;
 	protected String audioFilePath = "";
	protected int audioFileId = R.raw.chime;
 	protected String imageFilePath = "";
 	protected int imageFileId = R.drawable.androids_experimenter_kids;
 	protected String videoFilePath = "";
 	
 	protected String label = ""; 
 	
 	public ArrayList<Touch> touches = new ArrayList<Touch>();
 	
 	protected long totalReactionTime = 0;
 	protected long reactionTimePostOffset = 0;
 	protected long audioOffset = 0;
 	protected long startTime = 0;
 	
 	
 	public Stimulus() {
 		super();
 		this.audioFilePath = "";
		this.audioFileId= R.raw.chime;
 		this.imageFilePath = "";
 		this.imageFileId = R.drawable.androids_experimenter_kids;
 		this.videoFilePath = "";
 		this.touches = new ArrayList<Touch>();
 		this.totalReactionTime = 0;
 		this.reactionTimePostOffset = 0;
 	}
 	public Stimulus(int imageid) {
 		super();
 		this.audioFilePath = "";
		this.audioFileId= R.raw.chime;
 		this.imageFilePath = "";
 		this.imageFileId = imageid;
 		this.videoFilePath = "";
 		this.touches = new ArrayList<Touch>();
 		this.totalReactionTime = 0;
 		this.reactionTimePostOffset = 0;
 	}
 	public Stimulus(int imageid,String label) {
 		super();
 		this.audioFilePath = "";
		this.audioFileId= R.raw.chime;
 		this.imageFilePath = "";
 		this.imageFileId = imageid;
 		this.videoFilePath = "";
 		this.touches = new ArrayList<Touch>();
 		this.totalReactionTime = 0;
 		this.reactionTimePostOffset = 0;
 		this.label= label;
 	}
 	
 	public Stimulus(String audioFilePath, String imageFilePath,
 			String videoFilePath) {
 		super();
 		this.audioFilePath = audioFilePath;
 		this.imageFilePath = imageFilePath;
 		this.videoFilePath = videoFilePath;
 		this.touches = new ArrayList<Touch>();
 		this.totalReactionTime = 0;
 		this.reactionTimePostOffset = 0;
 	}
 	
 	public Stimulus(String audioFilePath, String imageFilePath,
 			String videoFilePath, ArrayList<Touch> touches,
 			long totalReactionTime, long reactionTimePostOffset) {
 		super();
 		this.audioFilePath = audioFilePath;
 		this.imageFilePath = imageFilePath;
 		this.videoFilePath = videoFilePath;
 		this.totalReactionTime = totalReactionTime;
 		this.reactionTimePostOffset = reactionTimePostOffset;
 	}
 	
 	public String getAudioFilePath() {
 		return audioFilePath;
 	}
 	public void setAudioFilePath(String audioFilePath) {
 		this.audioFilePath = audioFilePath;
 	}
 	public int getAudioFileId() {
 		return audioFileId;
 	}
 	public void setAudioFileId(int audioFileId) {
 		this.audioFileId = audioFileId;
 	}
 	public String getImageFilePath() {
 		return imageFilePath;
 	}
 	public void setImageFilePath(String imageFilePath) {
 		this.imageFilePath = imageFilePath;
 	}
 	public int getImageFileId() {
 		return imageFileId;
 	}
 	public void setImageFileId(int imageFileId) {
 		this.imageFileId = imageFileId;
 	}
 	public String getVideoFilePath() {
 		return videoFilePath;
 	}
 	public void setVideoFilePath(String videoFilePath) {
 		this.videoFilePath = videoFilePath;
 	}
 	public String getLabel() {
 		return label;
 	}
 	public void setLabel(String label) {
 		this.label = label;
 	}
 	public ArrayList<Touch> getTouches() {
 		return touches;
 	}
 	public void setTouches(ArrayList<Touch> touches) {
 		this.touches = touches;
 	}
 	public long getTotalReactionTime() {
 		return totalReactionTime;
 	}
 	public void setTotalReactionTime(long totalReactionTime) {
 		this.totalReactionTime = totalReactionTime;
 	}
 	public long getReactionTimePostOffset() {
 		return reactionTimePostOffset;
 	}
 	public void setReactionTimePostOffset(long reactionTimePostOffset) {
 		this.reactionTimePostOffset = reactionTimePostOffset;
 	}
 	@Override
 	public String toString() {
 		String s = label+touches.get(touches.size()).toString();
 		return s;
 	}
 	public long getStartTime() {
 		return startTime;
 	}
 	public void setStartTime(long startTime) {
 		this.startTime = startTime;
 	}
 	public long getAudioOffset() {
 		return audioOffset;
 	}
 	public void setAudioOffset(long audioOffset) {
 		this.audioOffset = audioOffset;
 	}
 	
 	
 }
