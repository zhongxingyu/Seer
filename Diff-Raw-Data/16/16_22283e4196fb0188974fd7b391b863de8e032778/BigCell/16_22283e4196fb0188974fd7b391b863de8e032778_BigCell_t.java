 package hashlife;
 
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class BigCell extends MacroCell {
 
 	static private HashMap<MacroCell, MacroCell> built = new HashMap<MacroCell, MacroCell>();
 	
 	/*  +---+---+
 	 *  | 0 | 1 |
 	 *  +---+---+
 	 *  | 2 | 3 |
 	 *  +---+---+
 	 */	
 	final protected MacroCell quad[] = new MacroCell[4];
 	private MacroCell result[];
 	
 	private BigCell(MacroCell ... quad) {
 		super(quad[0].dim+1, quad[0].off && quad[1].off && quad[2].off && quad[3].off);
 		for(int i=0; i<4; i++)
 			this.quad[i] = quad[i];
 		this.result = new MacroCell[this.dim-1];
 		
 		if(this.off)
 			Arrays.fill(this.result, empty(this.dim-1));
 	}
 	
 	static public MacroCell get(MacroCell ... quad) {
 		assert(quad.length == 4 && quad[0].dim == quad[1].dim && quad[1].dim == quad[2].dim && quad[2].dim == quad[3].dim);
 		MacroCell m = new BigCell(quad);
 	
 		if(built.containsKey(m))
 			m = built.get(m);
 		else
 			built.put(m, m);
 		
 		return m;
 	}
 	
 	public boolean equals(Object o) {
 		return (o instanceof BigCell) && Arrays.equals(quad, ((BigCell) o).quad);
 	}
 	
 	public int hashCode() {
 		int hashCode = 1;
 		  for(int i=0; i<4; i++)
 		      hashCode = 31*hashCode + System.identityHashCode(quad[i]);
 		return hashCode;
 	}
 	
 	public MacroCell part(int depth, int i, int j) {
 		if(depth > dim)
 			throw new RuntimeException("Too large depth for this MacroCell");
 		if(depth == 0)
 			return this;
 		int factor = ((int) Math.pow(2, depth))/2;
 		return quad[2*(i/factor) + j/factor].part(depth-1, i%factor, j%factor);
 	}
 //test	
 	public MacroCell oneStep() {
 		
 		if(off)
 			return empty(dim-1);
 
 		MacroCell resQuad[] = new MacroCell[4];
 		if(dim == 2) {
 			for(int i=0; i<4; i++) {
 				int baseI = 1+i/2, baseJ = 1+i%2;
 				int count = 0;
 				for(int j=0; j<9; j++)
 					if(j != 4 && part(2, baseI+j/3-1, baseJ+j%3-1) == BooleanCell.True)
 						count++;
 
 				if(count == 2)
 					resQuad[i] = part(2, baseI, baseJ);
 				else if(count == 3)
 					resQuad[i] = BooleanCell.True;
 				else
 					resQuad[i] = BooleanCell.False;
 			}
 		} else {			
 			MacroCell tmp[][] = new MacroCell[3][3];
 			MacroCell cut[][] = new MacroCell[6][6];
 			for(int i=0; i<6; i++)
 				for(int j=0; j<6; j++)
 					cut[i][j] = part(3, i+1, j+1);
 			for(int i=0; i<3; i++)
 				for(int j=0; j<3; j++)
 					tmp[i][j] = get(cut[i*2][j*2], cut[i*2][j*2+1], cut[i*2+1][j*2], cut[i*2+1][j*2+1]);
 			for(int i=0; i<4; i++)
 				resQuad[i] = get(tmp[i/2][i%2], tmp[i/2][i%2+1], tmp[i/2+1][i%2], tmp[i/2+1][i%2+1]).result(0);
 		}
 		return get(resQuad);	
 	}
 	
 	public MacroCell result(int s) {
 		if(s > dim-2)
 			throw new RuntimeException("Cannot return the result at time " + s + " of a " + dim + " cell");
 		
 		if(result[s] != null)
 			return result[s];
 		
 		if(s == 0) {			
 			result[0] = oneStep();			
 			return result[0];
 		}
 		
 		// Case s > 0
 		MacroCell tmp[][] = new MacroCell[3][3];
 		MacroCell hexa[][] = new MacroCell[4][4];
 		for(int i=0; i<4; i++)
 			for(int j=0; j<4; j++)
 				hexa[i][j] = part(2, i, j);
 
 		for(int i=0; i<4 ;i++)
 			tmp[(i/2)*2][(i%2)*2] = quad[i].result(s-1);
 		for(int i=0; i<2; i++) {
 			tmp[i*2][1] = get(hexa[i*2][1], hexa[i*2][2], hexa[i*2+1][1], hexa[i*2+1][2]).result(s-1);
 			tmp[1][i*2] = get(hexa[1][i*2], hexa[1][i*2+1], hexa[2][i*2], hexa[2][i*2+1]).result(s-1);
 		}
 		tmp[1][1] = get(hexa[1][1], hexa[1][2], hexa[2][1], hexa[2][2]).result(s-1);
 				
 		MacroCell resQuad[] = new MacroCell[4];		
 		for(int i=0; i<4; i++)
 			resQuad[i] = get(tmp[i/2][i%2], tmp[i/2][i%2+1], tmp[i/2+1][i%2], tmp[i/2+1][i%2+1]).result(s-1);
 		
 		result[s] = get(resQuad);
 		return result[s];
 	}
 	
 	public MacroCell evolve(int t) {
 		MacroCell r = this.simplify();
 		
		int s = 32 - Integer.numberOfLeadingZeros(t);
		int n = 1<<s;
		for(int i = 0; i<=s; i++)
 			r = r.borderize();
 		while(n > 0) {
 			if((t&n) != 0)
 				r = r.result(s).borderize();
 			n /= 2;
			s--;
 		}
 		return r.simplify();
 	}
 	
 	public MacroCell simplify() {
 		if(dim < 2)
 			return this;
 		
 		if(off)
 			return empty(1);
 		
 		for(int i=0; i<4; i++) {
 			if(!part(2, i, 0).off || !part(2, i, 3).off || !part(2, (i/2)*3, i%2+1).off)
 				return this;
 		}
 		return get(part(2, 1, 1), part(2, 1, 2), part(2, 2,1), part(2, 2, 2)).simplify();
 	}
 	
 	public MacroCell borderize() {
 		if(off)
 			return empty(dim+1);
 		MacroCell e = empty(dim-1);
 		return get(get(e,e,e,quad[0]), get(e,e,quad[1],e), get(e,quad[2],e,e), get(quad[3],e,e,e));
 	}
 	
 	public void fillTab(boolean[][] tab, int i, int j) {
 		if(off)
 			for(int k=0; k<size; k++)
 				for(int l=0; l<size; l++)
 					tab[i+k][j+l] = false;
 		else
 			for(int k=0; k<4; k++)
 				quad[k].fillTab(tab, i+(k/2)*(size/2), j+(k%2)*(size/2));
 	}
 
 	public int getDensity() {
 		return 0; //TODO
 	}
 	
 }
