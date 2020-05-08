 package net.sprauer.sitzplaner.EA;
 
 import java.awt.Point;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 import java.util.Vector;
 
 import net.sprauer.sitzplaner.model.DataBase;
 
 public class Chromosome implements Comparable<Chromosome>, Comparator<Chromosome>, Serializable, Cloneable {
 
 	private static final long serialVersionUID = -3578751251444286705L;
 	private double fitness = -1;
 	Configuration configuration;
 	private Vector<StudentPosition> byIndex = new Vector<StudentPosition>();
 	private HashMap<Point, StudentPosition> positionMap = new HashMap<Point, StudentPosition>();
 	private boolean calculating;
 	public String id = UUID.randomUUID().toString();
 
 	private class StudentPosition {
 		int studentIndex;
 		Point position = new Point();
 	}
 
 	public Chromosome(Configuration conf) {
 		configuration = conf;
 		byIndex.setSize(DataBase.instance().getSize());
 		for (int i = 0; i < size(); i++) {
 			byIndex.set(i, new StudentPosition());
 		}
 	}
 
 	public int size() {
 		return byIndex.size();
 	}
 
 	public double getFitness() {
 		if (fitness < 0 && !calculating) {
 			try {
 				calculating = true;
 				fitness = EAFactory.fitness(this);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				calculating = false;
 			}
 		}
 		return fitness;
 	}
 
 	public Point getPositionOf(int index) {
 		return byIndex.get(index).position;
 	}
 
 	public void setPositionOf(int i, Point newPos) {
 		byIndex.get(i).position = newPos;
 		positionMap.put(newPos, byIndex.get(i));
 	}
 
 	@Override
 	public int compare(Chromosome o1, Chromosome o2) {
 		return Double.compare(o1.fitness, o2.fitness);
 	}
 
 	public void setFitness(double f) {
 		fitness = f;
 	}
 
 	public int studentAt(Point pos) {
 		if (positionMap.containsKey(pos)) {
 			return positionMap.get(pos).studentIndex;
 		} else {
 			return -1;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return id + "(" + fitness + ")";
 	}
 
 	public List<Chromosome> mutate() {
 		List<Chromosome> children = swap();
 		children.addAll(invert());
 		return children;
 	}
 
 	public List<Chromosome> swap() {
 		List<Chromosome> swapped = new ArrayList<Chromosome>();
 		for (int i = 0; i < configuration.getDescendantsUsingSwap(); i++) {
 			Chromosome child = doClone().doSwap(configuration.getNumberOfSwaps());
 			swapped.add(child);
 		}
 		return swapped;
 	}
 
 	public List<Chromosome> invert() {
 		List<Chromosome> swapped = new ArrayList<Chromosome>();
		for (int i = 0; i < configuration.getDescendantsUsingSwap(); i++) {
 			Chromosome child = doClone().doInvert(configuration.getNumberOfInversions());
 			swapped.add(child);
 		}
 		return swapped;
 	}
 
 	private Chromosome doInvert(int times) {
 		for (int k = 0; k < times; k++) {
 			int i = (int) (Math.random() * byIndex.size());
 			int j = (int) (Math.random() * byIndex.size());
 			final int max = Math.abs(i - j);
 			int y = max;
 			for (int x = 0; (x < max && x != y); x++, y--) {
 				doSwap(x, y);
 			}
 		}
 		return this;
 	}
 
 	private Chromosome doSwap(int times) {
 		for (int y = 0; y < times; y++) {
 			int i = (int) (Math.random() * byIndex.size());
 			int j = (int) (Math.random() * byIndex.size());
 			doSwap(i, j);
 		}
 		return this;
 	}
 
 	private void doSwap(int i, int j) {
 		if (DataBase.instance().getStudent(i).isLocked() || DataBase.instance().getStudent(j).isLocked()) {
 			return;
 		}
 		positionMap.put(byIndex.get(i).position, byIndex.get(j));
 		positionMap.put(byIndex.get(j).position, byIndex.get(i));
 		Collections.swap(byIndex, i, j);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Chromosome doClone() {
 		Chromosome chrome;
 		try {
 			chrome = (Chromosome) this.clone();
 			chrome.positionMap = (HashMap<Point, StudentPosition>) positionMap.clone();
 			chrome.byIndex = (Vector<StudentPosition>) byIndex.clone();
 			chrome.id = UUID.randomUUID().toString();
 			chrome.fitness = -1;
 		} catch (CloneNotSupportedException e) {
 			e.printStackTrace();
 			return this;
 		}
 		return chrome;
 	}
 
 	public Configuration getConfig() {
 		return configuration;
 	}
 
 	@Override
 	public int compareTo(Chromosome o) {
 		return compare(o, this);
 	}
 
 }
