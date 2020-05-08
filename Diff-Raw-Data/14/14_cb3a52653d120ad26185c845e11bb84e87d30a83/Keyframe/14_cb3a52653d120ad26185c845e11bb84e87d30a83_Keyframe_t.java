 package animation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Keyframe {
 	private int frameNumber;
 	private List<Bone> bones;
 	private String trigger;
 	private boolean mirrored;
 
 	//TODO JAVA HOW DO
 	//[Microsoft.Xna.Framework.Content.ContentSerializerIgnore]
 	private float frameTime;
 	//[Microsoft.Xna.Framework.Content.ContentSerializerIgnore]
 	private List<Bone> updateOrderBones;
 
 	public Keyframe()
 	{
 		updateOrderBones = new ArrayList<Bone>();
 	}
 
 	public void sortBones()
 	{
 		updateOrderBones.clear();
 		for (Bone bone : bones)
 		{
 			boneSortAdd(bone);
 		}
 	}
 
 	protected void boneSortAdd(Bone b)
 	{
 		if (updateOrderBones.contains(b))
 			return;
 
 		if (b.getParentIndex() != -1)
 			boneSortAdd(bones.get(b.getParentIndex()));
 
 		updateOrderBones.add(b);
 		b.setUpdateIndex(updateOrderBones.size() - 1);
 	}
 
 	public int getFrameNumber() {
 		return frameNumber;
 	}
 
 	public void setFrameNumber(int frameNumber) {
 		this.frameNumber = frameNumber;
 	}
 
 	public List<Bone> getBones() {
 		return bones;
 	}
 
 	public void setBones(List<Bone> bones) {
 		this.bones = bones;
 	}
 
 	public String getTrigger() {
 		return trigger;
 	}
 
 	public void setTrigger(String trigger) {
 		this.trigger = trigger;
 	}
 
 	public boolean isMirrored() {
 		return mirrored;
 	}
 
	public void setMirrored(boolean mirrored) {
		this.mirrored = mirrored;
 	}
 
 	public float getFrameTime() {
 		return frameTime;
 	}
 
 	public void setFrameTime(float frameTime) {
 		this.frameTime = frameTime;
 	}
 
 	public List<Bone> getUpdateOrderBones() {
 		return updateOrderBones;
 	}	
 	
 }
