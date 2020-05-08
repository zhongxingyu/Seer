 package ru.osm.dkiselev.mpreader.sections;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public abstract class SectionReaderBase<T extends Section> implements SectionReader<T> {
 
 	public static Pair<String, String> readPair(String line) {
 		//if '=' chars exists more then once 
 		int index = line.indexOf('=');
 		
 		if(index < 0) {
 			return null;
 		}
 
 		if(index == line.length() - 1) {
 			return null;
 		}
 		
		return new Pair<String, String>(line.substring(0, index),  line.substring(index + 1, line.length() - 1));
 	}
 	
 	public static class Pair<K, V> {
 		
 		public Pair(K key, V value) {
 			super();
 			this.key = key;
 			this.value = value;
 		}
 		
 		private K key;
 		private V value;
 		
 		public K getKey() {
 			return key;
 		}
 		public void setKey(K key) {
 			this.key = key;
 		}
 		public V getValue() {
 			return value;
 		}
 		public void setValue(V value) {
 			this.value = value;
 		}
 		
 	}
 	
 	public static List<Point> parsePointsLine(String value) {
 		
 		value = value.replaceAll("\\(", "");
 		value = value.replaceAll("\\)", "");
 		
 		String[] split = value.split(",");
 		
 		List<Point> result = new ArrayList<Point>();
 		
 		int i = 0;
 		Point currentPoint = null;
 		for(String coord : split)
 		{
 			if(i++ % 2 == 0) {
 				currentPoint = new Point();
 				result.add(currentPoint);
 
 				currentPoint.setX(Double.parseDouble(coord));
 			}
 			else {
 				currentPoint.setY(Double.parseDouble(coord));
 			}
 		}
 		
 		return result;
 	}
 	
 	@Override
 	public void setComments(List<String> comments, T section) {
 		if(!comments.isEmpty()) {
 			section.setComments(comments);
 		}
 	}
 
 	@Override
 	public void endSection(T section) {
 		//do nothing
 	}
 
 }
