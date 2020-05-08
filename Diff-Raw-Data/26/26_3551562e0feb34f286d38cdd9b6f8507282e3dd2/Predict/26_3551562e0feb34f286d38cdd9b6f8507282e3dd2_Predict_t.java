 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.TreeMap;
 
 public class Predict {
 	Data data;
 	public double CA;
 	public Predict(Data d) {
 		data = d;
 	}
 	
 	public double predictKnown() {
 		int hit = 0, all = 0;
 		for (Entity en : data.entity) {
 			if (!en.clasHidden) {
 				if (predictFor(en)) {
 					hit++;
 				} 
 				all++;;
 			}
 		}
 		CA = (double)(hit * 1000 / all) / 10;
 		System.out.println("Known " + hit + " / " + all + " = " + CA + "%");
 		return (double)hit / all;
 	}
 	
 	public double predict() {
 		int hit = 0, all = 0;
 		for (Entity en : data.entity) {
 			if (en.clasHidden) {
 				if (predictFor(en)) {
 					hit++;
 				} 
 				all++;;
 			}
 		}
 		CA = (double)(hit * 1000 / all) / 10;
 		//System.out.println(hit + " / " + all + " = " + CA + "%");
 		return CA;
 	}
 	
 	public boolean predictFor(Entity en) {
 		TreeMap<Clas, Double> cats = new TreeMap<Clas, Double>();
 		ArrayList<ClasDist> na = new ArrayList<ClasDist>();
 		
 		for (Entity en1 : data.entity) {
 			if (en != en1 && !en1.clasHidden) {
				double d = Math.pow(en1.x - en.x, 2) + Math.pow(en1.y - en.y, 2);
 				d = Math.max(d, 5);
 				na.add(new ClasDist(en1.clas, 1.0 / d));
 			}
 		}
 		
 		Collections.sort(na);
 		
 		double sum = 0;
 		for (int i = 0; i < na.size() / 2; i++) {
 			ClasDist n = na.get(i);
 			
 			double d = 1 / n.dist;
 
 			if (d > 20) {
 				double avg = sum / i;
 				if (i > 2 && d > avg * 4 || i > 0 && d > avg * 10) {
 					//System.out.println(i);
 					break;
 				}
 			}
 			
 			if (cats.containsKey(n.clas)) n.dist += cats.get(n.clas);
 			cats.put(n.clas, n.dist);
 			
 			sum += d;
 		}
 		
 		Clas maxKey = null;
 		double maxValue = -1;
 		for (Clas key : cats.keySet()) {
 			double value = cats.get(key);
 			//System.out.println(key.name + ": " + value);
 			if (value > maxValue) {
 				maxValue = value;
 				maxKey = key;
 			}
 		}
 		Clas trueW = en.clas;
 		
 		//System.out.println("Winner: " + maxKey.name);
 		//System.out.println("True winner: " + trueW.name);
 		
 		//System.out.println();
 		
 		return trueW.id == maxKey.id;
 	}
 	
 
 public double predictTest() {
 	int hit = 0, all = 0;
 	for (Entity en : data.entity) {
 		if (en.clasHidden) {
 			if (predictTestFor(en)) {
 				hit++;
 			} 
 			all++;;
 		}
 	}
 	CA = (double)(hit * 1000 / all) / 10;
 	return CA;
 }
 
 public boolean predictTestFor(Entity en) {
 	TreeMap<Clas, Double> cats = new TreeMap<Clas, Double>();
 	ArrayList<ClasDist> na = new ArrayList<ClasDist>();
 	
 	for (Conn conn : data.conn) {
 		Entity en1 = null;
 		if (conn.e1 == en) {
 			en1 = conn.e2;
 		} else if (conn.e2 == en) {
 			en1 = conn.e1;
 		} else {
 			continue;
 		}
 		
 		if (!en1.clasHidden) {
 			na.add(new ClasDist(en1.clas, conn.strength));
 		}
 	}
 	//System.out.println(na.size());
 	Collections.sort(na);
 	
 	double sum = 0;
 	for (int i = 0; i < na.size() / 2; i++) {
 		ClasDist n = na.get(i);
 		
 		double d = 1 / n.dist;
 
 		if (d > 20) {
 			double avg = sum / i;
 			if (i > 2 && d > avg * 4 || i > 0 && d > avg * 10) {
 				//System.out.println(i);
 				break;
 			}
 		}
 		
 		if (cats.containsKey(n.clas)) n.dist += cats.get(n.clas);
 		cats.put(n.clas, n.dist);
 		
 		sum += d;
 	}
 	
 	Clas maxKey = null;
 	double maxValue = -1;
 	for (Clas key : cats.keySet()) {
 		double value = cats.get(key);
 		//System.out.println(key.name + ": " + value);
 		if (value > maxValue) {
 			maxValue = value;
 			maxKey = key;
 		}
 	}
 	Clas trueW = en.clas;
 	
 	//System.out.println("Winner: " + maxKey.name);
 	//System.out.println("True winner: " + trueW.name);
 	
 	//System.out.println();
 	
 	return trueW.id == maxKey.id;
 }
 
 }
 
 class ClasDist implements Comparable<ClasDist> {
 	public Clas clas;
 	public double dist;
 	public ClasDist(Clas c, double d) {
 		clas = c;
 		dist = d;
 	}
 	public int compareTo(ClasDist a) {
 		return dist < a.dist ? 1 : -1;
 	}
 }
