 package org.jagatoo.loaders.models.collada.datastructs.animation;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Iterator for an easy manager of the bones of a Skeleton
  *
  * @author Matias Leone
  */
 public class SkeletonIterator implements Iterator<Bone> {
 
 	private Skeleton skeleton;
 	private List<Bone> bones = new ArrayList<Bone>();
 	private int currentIndex;
 
 
 	/**
 	 * Creates an iterator for the bones of the skeleton
 	 * @param skeleton
 	 */
 	public SkeletonIterator( Skeleton skeleton ) {
 		this.skeleton = skeleton;
 		reset();
 	}
 
 	/**
	 * Reset the iterator to it�s initial position
 	 */
 	public void reset() {
 		Bone bone = this.skeleton.rootBone;
 		fillBoneList( bone, bones );
 		currentIndex = -1;
 	}
 
 	/**
 	 * Fills the bone list with a depth first criteria
 	 * @param bone
 	 * @param boneList
 	 */
 	private void fillBoneList( Bone bone, List<Bone> boneList ) {
 		boneList.add( bone );
 		for (int i = 0; i < bone.numChildren(); i++) {
 			fillBoneList( bone.getChild( i ), boneList );
 		}
 	}
 
 	public boolean hasNext() {
 		return currentIndex + 1 < bones.size();
 	}
 
 	public Bone next() {
		return bones.get( ++currentIndex );
 	}
 
 
 	public void remove() {
 		throw new UnsupportedOperationException( "Can not remove a bone" );
 	}
 
 }
