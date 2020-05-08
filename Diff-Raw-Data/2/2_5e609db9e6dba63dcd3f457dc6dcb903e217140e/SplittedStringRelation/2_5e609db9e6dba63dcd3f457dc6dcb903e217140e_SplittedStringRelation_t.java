 /**
  * SplittedStringRelation.java, (c) 2012, Immanuel Albrecht; Dresden University of
  * Technology, Professur für die Psychologie des Lernen und Lehrens
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.tu_dresden.psy.regexp;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * implements a multiple chain pattern splitter that is able to map the
  * splittings to new strings using multiple formulae
  * 
  * @author albrecht
  * 
  */
 public class SplittedStringRelation implements StringRelationInterface {
 
 	public interface MapSplitting {
 
 		/**
 		 * returns a set of Strings that is the result of the given splitting
 		 * 
 		 * @param splitting
 		 * @return the function applied to the splitting
 		 */
 		public Set<String> map(String[] splitting);
 	};
 
 	/**
 	 * implements a constant MapSplitting
 	 * 
 	 * @author albrecht
 	 * 
 	 */
 	public static class ConstantMap implements MapSplitting {
 		private Set<String> value;
 
 		/**
 		 * create a constant map always returning value
 		 * 
 		 * @param value
 		 */
 
 		public ConstantMap(String value) {
 			this.value = new HashSet<String>();
 			this.value.add(value);
 		}
 
 		@Override
 		public Set<String> map(String[] splitting) {
 			return this.value;
 		}
 	}
 
 	/**
 	 * implements the (graceful) projection of a part of a splitting
 	 * 
 	 * @author albrecht
 	 * 
 	 */
 	public static class ProjectionMap implements MapSplitting {
 		private int part;
 		private Set<String> empty;
 
 		/**
 		 * create a projection
 		 * 
 		 * @param part
 		 *            splittings index of the projection
 		 */
 		public ProjectionMap(int part) {
 			this.part = part;
 			this.empty = new HashSet<String>();
 		}
 
 		public Set<String> map(String[] splitting) {
 			if (splitting.length > part) {
 				Set<String> result = new HashSet<String>();
 				result.add(splitting[part]);
 				return result;
 			}
 			return empty;
 		};
 	}
 
 	public static class SplittedStringProjectionMap implements MapSplitting {
 		private int part;
 		private Set<String> empty;
 		private SplittedStringRelation relative_map;
 
 		/**
 		 * create a projection
 		 * 
 		 * @param part
 		 *            splittings index of the projection
 		 */
 		public SplittedStringProjectionMap(int part,
 				SplittedStringRelation relativeMap) {
 			this.part = part;
 			this.empty = new HashSet<String>();
 			this.relative_map = relativeMap;
 		}
 
 		public Set<String> map(String[] splitting) {
 			if (splitting.length > part) {
 				return relative_map.allMaps(splitting[part]);
 			}
 			return empty;
 		};
 	}
 
 	private Set<StringSplitter> splitters;
 
 	private Set<Vector<MapSplitting>> maps;
 
 	/**
 	 * default constructor with empty inputs & outputs
 	 */
 	public SplittedStringRelation() {
 		this.splitters = new HashSet<StringSplitter>();
 		this.maps = new HashSet<Vector<MapSplitting>>();
 	}
 
 	/**
 	 * takes a rule of the form [Input ·-delimited k-RegExp]→[Output Function]
 	 * 
 	 * @param delimitedRule
 	 */
 	public SplittedStringRelation(String delimitedRule) {
 		this.splitters = new HashSet<StringSplitter>();
 		this.maps = new HashSet<Vector<MapSplitting>>();
 		String[] left_right = delimitedRule.split("→");
 		String left = left_right[0];
 		String right = left_right[1];
 
 		this.splitters.add(new KRegExp(left));
 		this.maps.add(getOutputFunction(right));
 	}
 
 	/**
 	 * takes a · delimited string where every part is constant but parts of the
 	 * form »[NUMBER] where NUMBER determines the n-th split part
 	 * 
 	 * @param code
 	 * @return
 	 */
 	public static Vector<MapSplitting> getOutputFunction(String code) {
 		String[] parts = code.split("·");
 
 		Vector<MapSplitting> result = new Vector<SplittedStringRelation.MapSplitting>();
 
 		for (String part : parts) {
 			if (part.startsWith("»")) {
 				result.add(new ProjectionMap(
 						Integer.parseInt(part.substring(1)) - 1));
 			} else {
 				result.add(new ConstantMap(part));
 			}
 		}
 
 		return result;
 	}
 
 	public void addInput(StringSplitter splitter) {
 		this.splitters.add(splitter);
 	}
 
 	public void addOutput(Vector<MapSplitting> maps) {
 		this.maps.add(maps);
 	}
 
 	/**
 	 * map using all inputs and all outputs
 	 * 
 	 * @param s
 	 *            input String
 	 * @return all possible result values
 	 */
 	@Override
 	public Set<String> allMaps(String s) {
 		Set<String> result = new HashSet<String>();
 		Set<String[]> splittings = new HashSet<String[]>();
 
 		for (StringSplitter splitter : splitters) {
 			splittings.addAll(splitter.split(s));
 		}
 
 		for (String[] split : splittings) {
 			for (Vector<MapSplitting> map_vector : maps) {
 				Set<String> prefixes = new HashSet<String>();
 				Set<String> concat = new HashSet<String>();
 				prefixes.add("");
 				
 				for (MapSplitting map : map_vector) {
 					concat.clear();
 					for (String left : prefixes) {
 						for (String right : map.map(split)) {
 							concat.add(left + right);
 						}
 					}
 					
 					Set<String> delta = prefixes;
 					prefixes = concat;
					concat = delta;
 				}
 				result.addAll(prefixes);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * testing routine
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		SplittedStringRelation rel = new SplittedStringRelation();
 
 		rel.addInput(new KRegExp(new String[] { "AB", "CD", "EF" }));
 		rel.addInput(new KRegExp(new String[] { "ABC", "D", "EF" }));
 		rel.addInput(new KRegExp(new String[] { "A", "BC", "DEF" }));
 		Vector<SplittedStringRelation.MapSplitting> map = new Vector<SplittedStringRelation.MapSplitting>();
 		map.add(new SplittedStringRelation.ConstantMap("2="));
 		map.add(new SplittedStringRelation.ProjectionMap(2));
 		map.add(new SplittedStringRelation.ConstantMap(" 1="));
 		map.add(new SplittedStringRelation.ProjectionMap(1));
 		map.add(new SplittedStringRelation.ConstantMap(" 0="));
 		map.add(new SplittedStringRelation.ProjectionMap(0));
 		rel.addOutput(map);
 
 		SplittedStringRelation rel2 = new SplittedStringRelation();
 		rel2.addInput(new KRegExp(new String[] { "ABCDEF", "ABCDEF" }));
 		Vector<SplittedStringRelation.MapSplitting> map2 = new Vector<SplittedStringRelation.MapSplitting>();
 		map2.add(new SplittedStringProjectionMap(0, rel));
 		map2.add(new SplittedStringProjectionMap(1, rel));
 		rel2.addOutput(map2);
 
 		System.out.println(rel2.allMaps("ABCDEFABCDEF").size());
 		
 		SplittedStringRelation fromStr = new SplittedStringRelation("A*B*·(AB)*·A*B*→middle part: ·»2");
 		
 		System.out.println(fromStr.allMaps("ABABABABAB"));
 	}
 
 }
