 package bohnanza.alcabohne.model;
 
 import java.util.HashMap;
 import bohnanza.core.IBeanType;
 
 public enum EBeanType implements IBeanType {
 	BLUEBEAN {
 		@Override
 		public final String toString() {
			return "Blue Bean";
 		}
 
 		@Override
 		public final int numberOfCards() { // 20
			return 20;
 		}
 
 		@Override
 		public final HashMap<Integer, Integer> beanometer() {
			HashMap<Integer, Integer> beano = new HashMap<Integer, Integer>();
			beano.put(10, 4);
			beano.put(8, 3);
			beano.put(6, 2);
			beano.put(4, 1);
			return beano;
 		}
 	},
 	KIDNEYBEAN {
 		@Override
 		public final String toString() {
 			return "Kidney Bean";
 		}
 
 		@Override
 		public int numberOfCards() {
 			return 19;
 		}
 
 		@Override
 		public HashMap<Integer, Integer> beanometer() {
 			HashMap<Integer, Integer> beano = new HashMap<Integer, Integer>();
 			beano.put(8, 4);
 			beano.put(7, 3);
 			beano.put(6, 2);
 			beano.put(3, 1);
 			return beano;
 		}
 
 	},
 	FIREBEAN {
 		@Override
 		public final String toString() {
 			return "Fire Bean";
 		}
 
 		@Override
 		public int numberOfCards() {
 			return 18;
 		}
 
 		@Override
 		public HashMap<Integer, Integer> beanometer() {
 			HashMap<Integer, Integer> beano = new HashMap<Integer, Integer>();
 			beano.put(9, 4);
 			beano.put(8, 3);
 			beano.put(6, 2);
 			beano.put(3, 1);
 			return beano;
 		}
 	},
 	PUFFBOHNE {
 		@Override
 		public final String toString() {
 			return "Puff Bean";
 		}
 
 		@Override
 		public int numberOfCards() {
 			return 16;
 		}
 
 		@Override
 		public HashMap<Integer, Integer> beanometer() {
 			HashMap<Integer, Integer> beano = new HashMap<Integer, Integer>();
 			beano.put(7, 4);
 			beano.put(6, 3);
 			beano.put(3, 2);
 			beano.put(1, 1);
 			return beano;
 		}
 	},
 	BOARDBEAN {
 		@Override
 		public final String toString() {
 			return "Board Bean";
 		}
 
 		@Override
 		public int numberOfCards() {
 			return 16;
 		}
 
 		@Override
 		public HashMap<Integer, Integer> beanometer() {
 			HashMap<Integer, Integer> beano = new HashMap<Integer, Integer>();
 			beano.put(8, 4);
 			beano.put(7, 3);
 			beano.put(5, 2);
 			beano.put(3, 1);
 			return beano;
 		}
 	},
 	FRENCHBEAN {
 		@Override
 		public final String toString() {
 			return "French Bean";
 		}
 
 		@Override
 		public int numberOfCards() {
 			return 14;
 		}
 
 		@Override
 		public HashMap<Integer, Integer> beanometer() {
 			HashMap<Integer, Integer> beano = new HashMap<Integer, Integer>();
 			beano.put(7, 4);
 			beano.put(6, 3);
 			beano.put(5, 2);
 			beano.put(3, 1);
 			return beano;
 		}
 	},
 	RUNNERBEAN {
 		@Override
 		public final String toString() {
 			return "Runner Bean";
 		}
 
 		@Override
 		public int numberOfCards() {
 			return 13;
 		}
 
 		@Override
 		public HashMap<Integer, Integer> beanometer() {
 			HashMap<Integer, Integer> beano = new HashMap<Integer, Integer>();
 			beano.put(6, 4);
 			beano.put(5, 3);
 			beano.put(4, 2);
 			beano.put(3, 1);
 			return beano;
 		}
 	}
 }
