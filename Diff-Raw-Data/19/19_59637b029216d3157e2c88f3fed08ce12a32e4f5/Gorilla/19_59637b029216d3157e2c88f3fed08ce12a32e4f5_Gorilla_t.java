 import java.util.ArrayList;
 
 public class Gorilla {
 	
 	private static final int d = 20; // fixed profile size
 	private static final int k = 2;  // fixed k-gram size
 	
 	private static int hash(String s) {
 		int hval = s.hashCode() % d;
 		return hval;
 	} 
 	
 	// Compute profile array
 	private static int[] computeProfile(String s) {
 		int[] p = new int[d];
 		// count k-gram hash occurences
 		for (int i = 0; i < s.length() - k; i++) {
 			String kgram = s.substring(i,i+k);
 			p[hash(kgram)]++;
 		}
 		return p;
 	}
 	
 	// profile class to hold species sequence, profile etc.
 	private static final class Profile {
 		String name;		// species name
 		String seq;			// proteine sequence
 		int[] profile;		// hash profile
 		String relative;	// closest relative
 		
 		public Profile(String name, String seq, int[] profile) {
 			this.name = name;
 			this.seq = seq;
 			this.profile = profile;
 		}
 	}
 	
 	// vector dot product
 	public static int vectorDot(int[] v1, int[] v2) {
 		int p = 0;
 		for (int i = 0; i < v1.length; i++) {
 			p += v1[i] * v2[i];
 		}
 		return p;
 	}
 	
 	// vector length
 	public static double vectorLength(int[] v) {
 		return Math.sqrt(vectorDot(v, v));
 	}
 	
 	// vector angle
 	public static double vectorAngle(int[] v1, int[] v2) {
 		double costoa = vectorDot(v1, v2) / (vectorLength(v1) * vectorLength(v2));
 		return Math.acos(costoa);
 	}
 	
 	// test client
 	public static void main(String[] args) {
 		
 		// test vector methods
 		int[] t1 = {1, 0};
 		int[] t2 = {0, 1};
 		StdOut.println("vl: "+vectorLength(t1));
 		StdOut.println("va: "+vectorAngle(t1, t2));
 		
 		ArrayList<Profile> speciesCol = new ArrayList<Profile>();
 		
 		// read sequences from file, compute a profile for each and
 		// store the result in the 'species' array 
 		In in = new In(args[0]);
 		while(in.hasNextLine()) {
 			String line = in.readLine();
 			String spec = line.substring(1, line.indexOf(" "));
 			String seq = in.readLine()+in.readLine()+in.readLine();
 			int[] pro = computeProfile(seq); 
 			speciesCol.add(new Profile(spec, seq, pro));
 		}
 		
 		Profile[] species = speciesCol.toArray(new Profile[0]);
		
		for(Profile animal1 : species) {
			double closest = 1.0;
			for(Profile animal2 : species) {
				if(animal1.name.equals(animal2.name)) continue;
				
				double dist = vectorAngle(animal1.profile, animal2.profile);
				if (dist < closest) {
					animal1.relative = animal2.name;
					closest = dist;
				}
			}
		}
		
 		for(int i=0; i < species.length; i++) {
 			for(int j = i+1; j < species.length; j++) {
 				double sim = vectorAngle(species[i].profile, species[j].profile);
 				StdOut.println(species[i].name + " and " + species[j].name + " similarity (angle): " + sim);
 			}
 		}
 		
		for(Profile animal : species) {
			StdOut.println("Closest relative to " + animal.name + " is " + animal.relative);
		}
		
 	}
 }
