 package com.sk.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
import org.powerbot.script.lang.Validatable;
 
 public interface Condition {
 	public static final Condition TRUE = new Condition() {
 		@Override
 		public boolean check() {
 			return true;
 		}
 	};
 
 	public boolean check();
 
 	public static class Builder {
 
 		private final boolean and;
 		private List<Condition> blocks = new ArrayList<>();
 
 		public Builder(boolean and) {
 			this.and = and;
 		}
 
 		public Builder() {
 			this(true);
 		}
 
 		public Condition build() {
 			return new Built();
 		}
 
 		public Builder add(Condition... conds) {
 			if (conds != null)
 				Collections.addAll(blocks, conds);
 			return this;
 		}
 
 		public Builder add(Builder b) {
 			for (Condition block : b.blocks)
 				add(block);
 			return this;
 		}
 
 		private class Built implements Condition {
 			private final Condition[] conds;
 
 			private Built() {
 				this.conds = blocks.toArray(new Condition[blocks.size()]);
 				blocks.clear();
 			}
 
 			@Override
 			public boolean check() {
 				for (Condition c : conds) {
 					if (c.check() ^ and)
 						return !and;
 				}
 				return and;
 			}
 		}
 	}
 
 	public static class Converter {
 		public static Condition of(Validatable v) {
 			return new VConverter(v);
 		}
 
 		public static Condition not(Condition c) {
 			return new NConverter(c);
 		}
 
 		private static class NConverter implements Condition {
 			private final Condition src;
 
 			private NConverter(Condition v) {
 				this.src = v;
 			}
 
 			@Override
 			public boolean check() {
 				return !src.check();
 			}
 		}
 
 		private static class VConverter implements Condition {
 			private final Validatable src;
 
 			private VConverter(Validatable v) {
 				this.src = v;
 			}
 
 			@Override
 			public boolean check() {
 				return src.isValid();
 			}
 		}
 
 	}
 }
