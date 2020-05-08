 package chainTree;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import geom3d.PointSet3d;
 import j3dScene.J3DScene;
 import molecule.Protein;
 
 /**
  * An Adjustable ChainTree is a data structure with locked secondary structures.
  * @author hkb
  */
 public class AdjustableChainTree extends ChainTree {
 
 	/**
 	 * Creates a new adjustable chain tree from a protein.
 	 * 
 	 * @param protein The protein to create the adjustable chain tree from.
 	 * @param scene A 3D scene to render the protein on. 
 	 */
 	public AdjustableChainTree(Protein protein, J3DScene scene) {
 		super(extractProteinPoints(protein), protein, true);
 
 		// lock peptide planes
 		for (int i = 2; i < this.nodes.length; i = i+3) this.nodes[i].isLocked = true;
 		
 		// lock chain endpoints
 		this.nodes[this.nodes.length-1].isLocked = true;
 		this.nodes[0].isLocked = true;
 		
 		// lock secondary structures.
 		super.lockAlphaHelices(true);
 		super.lockBetaStrands(true);
 		
 		// re-balance the tree
 		super.removeLockedSubtrees(true);
 		super.newRebalanceSubtree(this.root);
 		super.addLockedSubtrees(true);
 		
 		// compute three bounding volume and energy.
 		super.createBoundingVolume(this.root);
 		super.createEnergyBoundingVolume(this.root);
 
 		// pre-compute distance matrix. <-- silly but avoids error
 		super.getDistanceMatrix();
 		
 		// setup 3D display properties if specified.
 		if (scene != null) {
 			this.j3dg = scene;
 		    this.initPaint();
 		    
 		    this.j3dg.autoZoom();
 		}
 	}
 	
 	/**
 	 * Creates a new adjustable chain tree from a protein.
 	 * 
 	 * @param protein The protein to create the adjustable chain tree from.
 	 */
 	public AdjustableChainTree(Protein protein) {
 		this(protein, null);
 	}
 	
 	/**
 	 * Creates a new adjustable chain tree from the proteins PDB id.
 	 * 
 	 * @param pdbId The id of the protein.
 	 * @param scene A 3D scene to render the protein on. 
 	 */
 	public AdjustableChainTree(String pdbId, J3DScene scene) {
 		this(new Protein(pdbId, 2, true), scene);
 	}
 	
 	/**
 	 * Creates a new adjustable chain tree from the proteins PDB id.
 	 * 
 	 * @param pdbId The id of the protein.
 	 */
 	public AdjustableChainTree(String pdbId) {
 		this(pdbId, null);
 	}
 	
 	
 	
 	/* ----------------- PUBLIC METHODS ---------------- */
 	
 	/**
 	 * Rotates the i-th bond by the given angle.
 	 * 
 	 * @param i The index of the bond to rotate.
 	 * @param angle The angle to rotate the bond.
 	 */
 	public void changeRotationAngle(int i, double angle) {
 		if (this.nodes[i].isLocked) {
 			throw new IllegalArgumentException("You can't rotate a locked angle!");
 		}
 
 		super.changeRotationAngle(i, angle);
 	}
 	
     /**
      * Group the nodes between l and r (both included) into their own subtree.
      * The root node of the tree is returned.
      * 
      * @param l The index of the leftmost node in the subtree.
      * @param r The index of the rightmost node in the subtree.
      * @return The root node of the grouped subtree. 
      */
     public CTNode group(int l, int r) {
    	if (l > r) {
     		throw new IllegalArgumentException("Invalid subtree!");
     	}
     	
     	// group subtree
 		CTNode nd = super.regroupLeft(this.nodes[l], r);
 		nd = super.regroupRight(this.nodes[r], l);
 
 		// re-balance regrouped subtree and return
     	return super.newRebalanceSubtree(nd);
     }
 	
 	/**
 	 * Unfolds a folded protein to some unfolded state.
 	 */
 	public void unfold() {
 		for (int i = 1, j = this.nodes.length-1; i < j; i++) {
 			if (!this.nodes[i].isLocked) { 
 				// if bound is not locked then obfuscate
 				this.changeRotationAngle(i, -this.getDihedralAngle(i));
 			}
 		}
 	}
 	
 	/**
 	 * Returns the indices of the bonds that are not locked in sorted order.
 	 * 
 	 * @return A collection of indices of the bonds that are not locked.
 	 */
     public ArrayList<Integer> rotateableBonds() {
 		ArrayList<Integer> rotatableBonds = new ArrayList<Integer>();
 		
 		for (int i = 0, j = this.nodes.length; i < j; i++) { 
 			if (!this.nodes[i].isLocked) {
 				rotatableBonds.add(i);
 			}
 		}
 		
     	return rotatableBonds;
     }
     
     
     
 	/* ----------------- PRIVATE METHODS ---------------- */
     
 	/**
 	 * Returns a set of the points in the protein.
 	 * 
 	 * THIS IS NECCESERY ONLY BECAUSE JAVA REQUIRES THE super CALL TO
 	 * BE THE FIRST IN THE CONSTRUCTOR!
 	 * 
 	 * @param protein The protein to calculate the points for.
 	 * @return The points of the protein.
 	 */
 	private static PointSet3d extractProteinPoints(Protein protein) {
 		PointSet3d allPoints = protein.getPointSet();
 		PointSet3d points = new PointSet3d();
 		for (int i = 0; i < allPoints.getSize(); i++) points.insert(allPoints.get(i));
 		return points;
 	}
 	
 }
