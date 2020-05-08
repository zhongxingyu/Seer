 package me.asofold.bukkit.fattnt.propagation;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import me.asofold.bukkit.fattnt.FatTnt;
 import me.asofold.bukkit.fattnt.config.Defaults;
 import me.asofold.bukkit.fattnt.config.Settings;
 import me.asofold.bukkit.fattnt.utils.Utils;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 public class ArrayPropagation extends Propagation {
 	
 	int[] sequence = null;
 	float[] strength = null;
 	int seqMax = 0;
 	
 	int center = -1;
 	int fY = 0;
 	int fZ = 0;
 	int izMax = 0;
 	int iCenter = -1;
 	
 	/**
 	 * Stats: number of visited blocks (some count double)
 	 */
 	int n = 0;
 	
 	/**
 	 * Explosion center block coords.
 	 */
 	int cx = 0;
 	int cy = 0;
 	int cz = 0;
 	
 	float fStraight;
 	
 	float minRes;
 	
 	float maxPath;
 	
 	private static final int[] ortDir = new int[]{2,4,6,8,10,12};
 	
 	/**
 	 * Blocks destroyed by the xplosion.
 	 */
 	List<Block> blocks = null;
 	
 	/**
 	 * opposite direction:<br>
 	 * 0:  no direction<br>
 	 * 1:  reserved: diagonal<br>
 	 * 2:  x+<br>
 	 * 3:  reserved: diagonal<br>
 	 * 4:  x-<br>
 	 * 5:  reserved: diagonal<br>
 	 * 6:  y+<br>
 	 * 7:  reserved: diagonal<br>
 	 * 8:  y-<br>
 	 * 9:  reserved: diagonal<br>
 	 * 10: z+<br>
 	 * 11: reserved: diagonal<br>
 	 * 12: z-<br>
 	 */
 	private static final int[] oDir = new int[]{
 		0,  // 0: no direction maps to no direction
 		0,  // UNUSED
 		4,  // x+ -> x-
 		0,  // UNUSED
 		2,  // x- -> x+
 		0,  // UNUSED
 		8,  // y+ -> y-
 		0,  // UNUSED
 		6,  // y- -> y+
 		0,  // UNUSED
 		12, // z+ -> z-
 		0,  // UNUSED
 		10, // z- -> z+
 	} ;
 	
 	/**
 	 * x increment by direction.
 	 */
 	private static final int[] xInc = new int[]{
 		0,  // 0: no direction maps to no direction
 		0,  // UNUSED
 		1,  // x+ 
 		0,  // UNUSED
 		-1,  // x-
 		0,  // UNUSED
 		0,  // y+ 
 		0,  // UNUSED
 		0,  // y- 
 		0,  // UNUSED
 		0, // z+ 
 		0,  // UNUSED
 		0, // z-
 	};
 	
 	/**
 	 * y increment by direction.
 	 */
 	private static final int[] yInc = new int[]{
 		0,  // 0: no direction maps to no direction
 		0,  // UNUSED
 		0,  // x+ 
 		0,  // UNUSED
 		0,  // x- 
 		0,  // UNUSED
 		1,  // y+ 
 		0,  // UNUSED
 		-1,  // y- 
 		0,  // UNUSED
 		0, // z+ 
 		0,  // UNUSED
 		0, // z- 
 	};
 	
 	/**
 	 * z increment by direction.
 	 */
 	private static final int[] zInc = new int[]{
 		0,  // 0: no direction maps to no direction
 		0,  // UNUSED
 		0,  // x+ 
 		0,  // UNUSED
 		0,  // x- 
 		0,  // UNUSED
 		0,  // y+ 
 		0,  // UNUSED
 		0,  // y- 
 		0,  // UNUSED
 		1, // z+ 
 		0,  // UNUSED
 		-1, // z- 
 	};
 	
 	/**
 	 * Array increments by direction.
 	 */
 	private final int[] aInc =  new int[13];
 
 	public ArrayPropagation(Settings settings) {
 		super(settings);
 		fStraight = settings.fStraight;
 		minRes = settings.minResistance;
 		maxPath = settings.maxPathMultiplier;
 		createArrays();
 	}
 	
 	private void createArrays() {
 		int d = 1 + (int) (maxRadius*2.0);
 		center = 1 + (int) maxRadius;
 		fY = d;
 		fZ = d*d;
 		int sz = d*d*d;
 		izMax = sz - fZ;
 		iCenter = center+ center*fY + center*fZ; // TODO: check if such is right
 		sequence = new int[sz];
 		strength = new float[sz];
 		for ( int i = 0; i<sz; i++){
 			sequence[i] = 0;
 		}
 		for (int i=0; i<aInc.length; i++){
 			aInc[i] = xInc[i] + yInc[i]*fY + zInc[i]*fZ;
 		}
 	}
 
 	@Override
 	public float getStrength(final double x, final double y, final double z) {
 		final int dx = center + Utils.floor(x) - cx;
 		final int dy = center + Utils.floor(y) - cy;
 		final int dz = center + Utils.floor(z) - cz ;
 		final int index = dx+fY*dy+fZ*dz;
 		if ( index<0 || index>= strength.length) return 0.0f; // outside of possible bounds.
 		if ( sequence[index] != seqMax) return 0.0f; // unaffected // WARNING: this uses seqMax, which has been set in getExplodingBlocks !
 		return strength[index]; // effective radius / strength
 	}
 
 	@Override
 	public List<Block> getExplodingBlocks(World world, double cx, double cy,
 			double cz, float realRadius) {
 		{
 			if ( realRadius > maxRadius){
 				// TODO: setttings ?
 				realRadius = maxRadius;
 			}
 			if ( this.blocks != null) this.blocks.clear(); // maybe gc :), should only happen on errors.
 			List<Block> blocks = new LinkedList<Block>(); // could change this to an array, but ....
 			this.blocks = blocks;
 			seqMax ++; // new round !
 			// starting at center block decrease weight and check neighbor blocks recursively, while weight > durability continue, only check
 			if (FatTnt.DEBUG) System.out.println(Defaults.msgPrefix+"Explosion at: "+world.getName()+" / "+cx+","+cy+","+cz);
 			this.cx = Utils.floor(cx);
 			this.cy = Utils.floor(cy);
 			this.cz = Utils.floor(cz);
 			n = 0;
 			propagate(world, this.cx, this.cy, this.cz, iCenter, 0, 1+(int)(realRadius*maxPath), realRadius);
 			if (FatTnt.DEBUG) System.out.println(Defaults.msgPrefix+"Strength="+realRadius+"("+maxRadius+"/"+minRes+"), visited="+n+", blocks="+blocks.size());
 			stats.addStats(FatTnt.statsBlocksVisited, n);
 			this.blocks = null;
 			return blocks;
 		}
 	}
 	
 	/**
 	 * TEST VERSION / LOW OPTIMIZATION !
 	 * Recursively collect blocks that get destroyed.
 	 * @param w
 	 * @param x Current real world pos.
 	 * @param y
 	 * @param z
 	 * @param i index of  array
 	 * @param dir Last direction taken to this point
 	 * @param mpl maximum path length allowed from here.
 	 * @param expStr Strength of explosion, or radius
 	 * @param seq
 	 * @param blocks
 	 */
 	final void propagate(final World w, final int x, final int y, final int z, 
 			final int i, final int dir, int mpl, float expStr){
 		n ++;
 		// Block type check (id):
 		final int id;
 		float dur ; // AIR
 		final boolean ign;
 		if ( y>=0 && y <= w.getMaxHeight()){// TODO: maybe +-1 ?
 			id = w.getBlockTypeIdAt(x,y,z);
 			if ( id == 0 ){
 				ign = true;
 				dur = resistance[0];
 			}
 			else if (id>0 && id<4096){
 				dur = resistance[id];
 				if ( sequence[i] == seqMax && strength[i] >= dur) ign = true; // TODO: might be unnecessary
 				else ign = false;
 			}
 			else{
 				dur = defaultResistance;
 				ign = true;
 			}
 		} 
 		else{
 			dur = resistance[0];
 			id = 0;
 			ign = true;
 		}
 		// Resistance check:
 		if (FatTnt.DEBUG_LOTS) System.out.println(x+","+y+","+z+" - "+expStr+" | "+id+"@"+dur); // TODO: remove this
 		// Matrix position:
 		sequence[i] = seqMax;
 		strength[i] = expStr;
 //		if ( randDec > 0.0) dur += random.nextFloat()*randDec;
 		if ( dur > expStr){
			final float ptRes = passthrough[id];
 			if (ptRes>dur) return;// this block stopped this path of propagation.
 			else{
 				// passthrough: continue to propagate
 				expStr -= ptRes;
 			}
 		} 
 		else{
 			if (!ign) blocks.add(w.getBlockAt(x,y,z));
 			expStr -= dur; // decrease after setting the array
 		}
 		// Checks for propagation:
 		if (mpl==0) return;	
 		if (i<fZ || i>izMax) return; // no propagation from edge on.
 		// TODO: use predefined directions + check here if maximum number of dirction changes is reached !
 		// propagate:
 		for (final int nd : ortDir){
 			// (iterate over orthogonal directions)
 			if (nd == oDir[dir]) continue; // prevent walking back.
 			final float effStr; // strength to be used.
 			// Check penalty for propagation in the same direction again:
 			if (nd == dir) effStr = expStr * fStraight;
 			else effStr = expStr;
 			if (effStr<minRes) continue; // not strong enough to propagate through any further block.
 			// Propagate if appropriate (not visited or with smaller strength).
 			final int j = i + aInc[nd];
 			if (sequence[j]!=seqMax || effStr>strength[j]) propagate(w, x+xInc[nd], y+yInc[nd], z+zInc[nd], j, nd, mpl-1, effStr);
 		}
 	}
 }
