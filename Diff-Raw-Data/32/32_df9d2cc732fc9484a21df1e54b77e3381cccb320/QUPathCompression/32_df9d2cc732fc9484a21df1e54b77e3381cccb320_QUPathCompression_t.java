 package unionfind;
 
 public class QUPathCompression implements UnionFind {
 	
	private int[] id;
	private int[] size;
 	
 	public QUPathCompression(int number) {
 		id = new int[number];
 		size = new int[number];
		for (int i = 0; i < id.length; i++) {
 			id[i] = i;
			size[i] = 1;
		}
 	}
 	
 	private int root(int i) {
 		while (i != id[i]) {
 			id[i] = id[id[i]];
 			i = id[i];
 		}
 		return i;
 	} // end method root
 	
 	@Override
 	public boolean connected(int p, int q) {
 		return root(p) == root(q);
 	} // end method connected
 	
 	@Override
 	public void union(int p, int q) {
 		int i = root(p);
 		int j = root(q);
 		if (size[i] < size[j]) {
 			id[i] = j;
 			size[j] += size[i];
 		} else {
 			id[j] = i;
 			size[i] += size[j];
 		}
 	} // end method union
 } // end class QUPathCompression
