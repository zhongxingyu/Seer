 package me.asofold.bukkit.fattnt.propagation;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import me.asofold.bukkit.fattnt.FatTnt;
 import me.asofold.bukkit.fattnt.config.Defaults;
 import me.asofold.bukkit.fattnt.config.ExplosionSettings;
 import me.asofold.bukkit.fattnt.config.Settings;
 import me.asofold.bukkit.fattnt.effects.ExplosionManager;
 import me.asofold.bukkit.fattnt.utils.Utils;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 public class ArrayPropagation extends Propagation {
 	
 	private int[] sequence;
 	private float[] strength;
 	private int[] ids;
 	private int seqMax = 0;
 	
 	private int center = -1;
 	private int fY = 0;
 	private int fZ = 0;
 	private int izMax = 0;
 	private int iCenter = -1;
 	
 	/**
 	 * Stats: number of visited blocks (some count double)
 	 */
 	private int n = 0;
 	
 	/**
 	 * Explosion center block coords.
 	 */
 	private int cx = 0;
 	private int cy = 0;
 	private int cz = 0;
 	
 	private static final int[] ortDir = new int[]{2,4,6,8,10,12};
 	
 	long tsLastIdle = System.currentTimeMillis();
 	
 	private final float[] rand = new float[1024];
 	/**
 	 * Blocks destroyed by the xplosion.
 	 */
 	private List<Block> blocks = null;
 	
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
 	
 	/**
 	 * Runtime ints.
 	 */
 	int[][] rInts = null;
 	/**
 	 * Runtime floats.
 	 */
 	float[] rFloats = null;
 	
 	/**
 	 * Maximum path lenght/recursion depth.
 	 */
 	int maxDepth = 0;
 	
 	public ArrayPropagation(Settings settings) {
 		super(settings);
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
 		ids = new int[sz];
 		for ( int i = 0; i<sz; i++){
 			sequence[i] = 0;
 		}
 		for (int i=0; i<aInc.length; i++){
 			aInc[i] = xInc[i] + yInc[i]*fY + zInc[i]*fZ;
 		}
 		
 		maxDepth = (1+ (int)maxRadius) * 2;
 		// six times (for 6 directions):
 		rFloats = new float[6*maxDepth];
 		rInts = new int[6*maxDepth][6];
 		for (int i = 0; i<6*maxDepth; i++){
 			rInts[i] = new int[6];
 		}
 		
 		initRandomArrays();
 	}
 
 	private void initRandomArrays() {
 		// random arrays:
 		Random temp = ExplosionManager.random;
 		for (int i = 0; i <rand.length; i++){
 			rand[i] = temp.nextFloat()-0.5f;
 		}
 	}
 
 	@Override
 	public final float getStrength(final double x, final double y, final double z) {
 		final int index = getValidIndex(Utils.floor(x), Utils.floor(y), Utils.floor(z));
 		if (index == -1) return 0.0f;
 		return strength[index]; // effective radius / strength
 	}
 	
 	/**
 	 * 1-dim matrix index , still check if out of range: if ( index<0 || index>= strength.length)
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return
 	 */
 	private final int getIndex(final int x, final int y, final int z){
 		final int dx = center + x - cx;
 		final int dy = center + y - cy;
 		final int dz = center + z - cz ;
 		return dx+fY*dy+fZ*dz;
 	}
 	
 	/**
 	 * Return -1 if invalid.
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @return
 	 */
 	private final int getValidIndex(final int x, final int y, final int z){
 		final int index  = getIndex(x,y,z);
 		if ( index<0 || index>= strength.length) return -1;
 		if ( sequence[index] != seqMax) return -1;
 		return index;
 	}
 	
 	@Override
 	public final int getTypeId(final int x, final int y, final int z){
 		final int index = getValidIndex(x, y, z);
 		if (index == -1) return -1;
 		return ids[index];
 	}
 
 	@Override
 	public List<Block> getExplodingBlocks(World world, double cx, double cy,
 			double cz, float realRadius, ExplosionSettings settings) {
 		{
 			if (realRadius > settings.maxRadius.value.floatValue()) realRadius = settings.maxRadius.value.floatValue();
 			if (realRadius > maxRadius) realRadius = maxRadius;
 			if (realRadius == 0.0){
 				// TODO: maybe more checks (minRes).
 				return new LinkedList<Block>();
 			}
 			
 			final float maxPath = settings.maxPathMultiplier.value.floatValue();
			final float minRes = settings.minResistance;
 			
 			if ( this.blocks != null) this.blocks.clear(); // maybe gc :), should only happen on errors.
 			final List<Block> blocks = new LinkedList<Block>(); // could change this to an array, but ....
 			this.blocks = blocks;
 			seqMax ++; // new round !
 			// starting at center block decrease weight and check neighbor blocks recursively, while weight > durability continue, only check
 			if (FatTnt.DEBUG) System.out.println(Defaults.msgPrefix+"Explosion at: "+world.getName()+" / "+cx+","+cy+","+cz);
 			this.cx = Utils.floor(cx);
 			this.cy = Utils.floor(cy);
 			this.cz = Utils.floor(cz);
 			n = 0;
 			propagate(world, this.cx, this.cy, this.cz, iCenter, 0, 1+(int)(realRadius*maxPath), realRadius, settings);
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
 	 * @param i index of  array(s)
 	 * @param dir Last direction taken to this point
 	 * @param mpl maximum path length allowed from here.
 	 * @param expStr Strength of explosion, or radius
 	 * @param seq
 	 * @param blocks
 	 */
 	final void propagate(final World w, int x, int y, int z, 
 			int i, int dir, int mpl, float expStr, final ExplosionSettings settings){
 		// preparation:
 		// TODO: also set these from the configuration.
 		final int wyMin = 0;
 		final int wyMax = w.getMaxHeight();
 		final int yMin;
 		final int yMax;
 		if (settings.confine.enabled.getValue(false)){
 			yMin = settings.confine.yMin.getValue(wyMin).intValue();
 			yMax = settings.confine.yMax.getValue(wyMax).intValue();
 		} else{
 			yMin = wyMin;
 			yMax = wyMax;
 		}
 		
 		final int seqMax = this.seqMax;
 		
 		final int[][] rInts = this.rInts;
 		final float[] rFloats = this.rFloats;
 		final float[] resistance = settings.resistance.value;
 		final float[] passthrough = settings.passthrough.value;
 		final float[] strength = this.strength;
 		final int[] sequence = this.sequence;
 		// kk exaggerated maybe...
 		
 		// set initial checking point:
 		int size = 1;
 		rInts[0] = new int[]{x,y,z,i,dir,Math.min(mpl, maxDepth)};
 		rFloats[0] = expStr;
 		// ? opt: boolean set = false; => get from stack ! if set continue with set values.
 		
 		int ir = ExplosionManager.random.nextInt(rand.length);
 		final int is = rand.length-1;
 		final int iinc = ExplosionManager.random.nextInt(4) + 1;
 		
 		final float defaultResistance = settings.defaultResistance.value.floatValue();
 		final boolean useRand = settings.randRadius.value.floatValue() > 0;
 		final float randRadius = settings.randRadius.value.floatValue();
 		final float fStraight = settings.fStraight.value.floatValue();
 		final float minRes = settings.minResistance;
 		// TODO: use minPassthrough ?
 
 		// iterate while points to check are there:
 		int n = 0;
 		while (size > 0){
 			n ++;
 			expStr = rFloats[size-1];
 			int[] temp = rInts[size-1];
 			x = temp[0];
 			y = temp[1];
 			z = temp[2];
 			i = temp[3];
 			dir = temp[4];
 			mpl = temp[5];
 			size --;
 			// TODO: can still be optimized in order [...]
 			if (sequence[i] == seqMax){
 				if ( strength[i] >= expStr) continue;
 			}
 			// Block type check (id):
 			final int id;
 			float dur ; // AIR
 			final boolean ign;
 			final boolean isSeq = sequence[i] == seqMax;
 			if ( y>=yMin && y <= yMax){// TODO: maybe +-1 ?
 				if (isSeq) id = ids[i];
 				else{
 					id = w.getBlockTypeIdAt(x,y,z);
 					ids[i] = id;
 				}
 				
 				if ( id == 0 ){
 					ign = true;
 					dur = resistance[0];
 				}
 				else if (id>0 && id<4096){
 					dur = resistance[id];
 					if ( isSeq && strength[i] >= dur) ign = true; // TODO: might be unnecessary
 					else ign = false;
 				}
 				else{
 					dur = defaultResistance;
 					ign = true;
 				}
 			} 
 			else if (y<wyMin || y>wyMax){
 				// Outside of world: no destruction, treat as air (use resistance).
 				dur = resistance[0];
 				id = 0;
 				ign = true;
 			}
 			else{
 				// Confinement: no destruction, use pass-through resistance.
 				// TODO: Get stored id if available.
 				if (isSeq) id = ids[i];
 				else{
 					id = w.getBlockTypeIdAt(x,y,z);
 					ids[i] = id;
 				}
 				dur = passthrough[id];
 				ign = true;
 			}
 			// Resistance check:
 			// Matrix position:
 			if (!isSeq) sequence[i] = seqMax;
 			strength[i] = expStr;
 //			if ( randDec > 0.0) dur += random.nextFloat()*randDec;
 			
 			if ( dur > expStr){
 				final float ptRes = passthrough[id];
 				if (ptRes>expStr) continue;// this block stopped this path of propagation.
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
 			if (mpl==0) continue;	
 			if (i<fZ || i>izMax) continue; // no propagation from edge on.
 			if (useRand){
 				// TODO: find out something fast, probably just have a task running filling in random numbers now and then.
 				// TODO: maybe the memory size matters...
 				expStr += rand[ir] * randRadius;
 				ir += iinc;
 				if (ir>is) ir =  mpl;
 			}
 			// TODO: use predefined directions + check here if maximum number of dirction changes is reached !
 			// propagate:
 			for (int k = 0; k < ortDir.length; k++){
 				final int nd = ortDir[k];
 				// (iterate over orthogonal directions)
 				if (nd == oDir[dir]) continue; // prevent walking back.
 				final float effStr; // strength to be used.
 				// Check penalty for propagation in the same direction again:
 				if (nd == dir) effStr = expStr * fStraight;
 				else effStr = expStr;
 				if (effStr<minRes) continue; // not strong enough to propagate through any further block.
 				// Propagate if appropriate (not visited or with smaller strength).
 				final int j = i + aInc[nd];
 				if (sequence[j]!=seqMax || effStr>strength[j]){
 					rFloats[size] = effStr;
 					final int[] nInts = rInts[size];
 					nInts[0] = x+xInc[nd];
 					nInts[1] = y+yInc[nd];
 					nInts[2] = z+zInc[nd];
 					nInts[3] = j;
 					nInts[4] = nd;
 					nInts[5] = mpl-1;
 					size++;
 				}
 			}
 		}
 		this.n = n;
 	}
 
 	@Override
 	public void onIdle() {
 		if (System.currentTimeMillis() - tsLastIdle >= 30000) initRandomArrays();
 	}
 }
