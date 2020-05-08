 package com.remirran.digitalmenu.data;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Order {
 	private static Order me = null;
 	private class Pair {
 		private Dish dish;
 		private int count;
 		public Pair(Dish dish) {
 			this.dish = dish;
 			this.count = 1;
 		}
 		public int inc() {
 			return ++count;
 		}
 		public Dish toDish(){
 			return dish;
 		}
 		public int getCount() {
 			return count;
 		}
 		public void setCount(int count) {
 			this.count = count;
 		}
 		public float getSum() {
 			return dish.getPrice() * count;
 		}
 		@Override
 		public boolean equals(Object o) {
 			return dish.equals(o);
 		}
 	}
 	private static ArrayList<Pair> list = new ArrayList<Order.Pair>();
 	private static int sum;
 	
 	private static Pair search(Dish d) {
 		synchronized (list) {
 			Iterator<Pair> itr = list.iterator();
 			while (itr.hasNext()) {
 				Pair elem = itr.next();
 				if (elem.equals(d)) return elem;
 			}
 			return null;
 		}
 	}
 	
 	public static Order getOrder() {
 		if (me == null) me = new Order();
 		return me;
 	}
 	
 	public static Integer getCount(Dish d) {
 		Pair p = search(d);
 		if (p == null) return 0;
 		return p.getCount();
 	}
 	
 	public static void setCount(Dish d, Integer c) {
 		Pair p = search(d);
 		if (p == null) {
 			p = me.new Pair(d);
 			synchronized (list) {
 				list.add(p);
 			}
 		}
 		p.setCount(c);
 		calc();
 	}
 	
 	public void inc(Dish d) {
 		Pair pair = search(d);
 		if (pair == null) {
 			synchronized (list) {
 				list.add(new Pair(d));
 			}
 		} else {
 			pair.inc();
 		}
 		calc();
 	}
 	public static void remove(int pos) {
 		synchronized (list) {
 			list.remove(pos);
 		}
 		calc();
 	}
 	public static void remove(Dish d) {
 		synchronized (list) {
 			Iterator<Pair> itr = list.iterator();
 			while (itr.hasNext()) {
 				if (itr.next().equals(d))
 					itr.remove();
 			}
 		}
 		calc();
 	}
 	public void clear() {
 		list.clear();
 		sum = 0;
 	}
 	public static String getSum() {
 		return "" + sum;
 	}
 	private static void calc() {
 		synchronized (list) {
 			sum = 0;
 			for (Pair pair : list) {
 				sum += pair.getSum();
 			}
 		}
 	}
 	public static int getSize() {
 		return list.size();
 	}
 	
	public static int invert (int pos) {
 		return getSize() - pos - 1;
 	}
 	public static Dish getItemByIndex(int pos) {
 		return list.get(invert(pos)).toDish();
 	}
 	public static int getCountByIndex(int pos) {
 		return list.get(invert(pos)).getCount();
 	}
 	public static String getSumByIndex(int pos) {
 		return "" + list.get(invert(pos)).getSum();
 	}
 	
 	public static JSONObject toJSON() throws JSONException {
 		if (list.size() == 0) return null;
 		
 		JSONObject retval = new JSONObject();
 		JSONObject order = new JSONObject();
 		retval.put("order", order);
 		order.put("device", android.os.Build.MODEL);
 		order.put("table", "0");
 		
 		JSONArray dishes = new JSONArray();
 		JSONArray count = new JSONArray();
 		
 		Iterator<Pair> iter = list.iterator();
 		
 		while(iter.hasNext()) {
 			Pair item = iter.next();
 			dishes.put(item.toDish().getId());
 			count.put(item.getCount());
 		}
 		
 		order.put("dishes", dishes);
 		order.put("count", count);
 		
 		return retval;
 	}
 }
