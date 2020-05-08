 package internal.parser.containers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public abstract class Datum {
 	Object data;
 	Class<?> type;
 	
 	protected Datum() {
 //		Stops empty constructor initialization
 	}
 	
 	private Datum(Integer data) {
 		this.data 	= data;
 		this.type 	= Datum.Int.class;
 	}
 	
 	private Datum(Float data) {
 		this.data	= data;
 		this.type	= Datum.Int.class;
 	}
 	
 	private Datum(String data) {
 		this.data	= data;
 		this.type	= Datum.Str.class;
 	}
 	
 	private Datum(Boolean data) {
 		this.data	= data;
 		this.type	= Datum.Bool.class;
 	}
 	
 	public Datum toInt() {
 		Datum datum = null;
 		
 		if(type.equals(Datum.Int.class)) {
 			datum	= new Datum.Int(new Integer((Integer) this.data));
 		}
 		
 		else if(type.equals(Datum.Flt.class)) {
 			datum	= new Datum.Int(new Integer((Integer.parseInt(Float.toString((Float) this.data)))));
 		}
 		
 		else if(type.equals(Datum.Str.class)) {
 			datum	= new Datum.Int(new Integer(Integer.parseInt((String) this.data)));
 		}
 		datum.type	= Datum.Int.class;
 		return datum;
 	}
 	
 	public Datum toFlt() {
 		Datum datum = null;
 		
 		if(type.equals(Datum.Int.class)) {
 			datum	= new Datum.Flt(new Float((Integer) this.data));
 		}
 		
 		else if(type.equals(Datum.Flt.class)) {
 			datum	= new Datum.Flt(new Float((Float) this.data));
 		}
 		
 		else if(type.equals(Datum.Str.class)) {
 			datum	= new Datum.Flt(new Float(Float.parseFloat((String) this.data)));
 		}
 		datum.type	= Datum.Flt.class;
 		return datum;
 	}
 	
 	public Datum toStr() {
 		Datum datum = null;
 		
 		if(type.equals(Datum.Int.class)) {
 			datum	= new Datum.Str(Integer.toString((Integer)this.data));
 		}
 		
 		else if(type.equals(Datum.Flt.class)) {
 			datum	= new Datum.Str(Float.toString((Float) this.data));
 		}
 		
 		else if(type.equals(Datum.Str.class)) {
 			datum	= new Datum.Str(new String((String) this.data));
 		}
 		
 		datum.type	= Datum.Str.class;
 		
 		return datum;
 	}
 	
 	
 	
 	
 	
 	public static class Int extends Datum {
 		public Int(Integer data) {
 			super(data);
 		}
 		
 		@Override
 		public String toString() {
 			return Integer.toString((Integer) data);
 		}
 
 		@Override
 		public List<Datum> allocate(int size) {
 			List<Datum> result = new ArrayList<Datum>();
 			
 			int availableQty = (Integer) data;
 			while(result.size() < size) {
				int qty = 0 + ((int) (Math.random() * (availableQty + 1)));
				result.add(new Datum.Int(qty));
				availableQty -= qty;
 			}
 			
 //			TODO: Remove this
 			int sum = 0;
 			for(Datum d : result) {
 				sum += (Integer) d.data;
 			}
 			assert sum == (Integer) data : "Datum.Int.allocate failed to allocate accurately!\n"
 					+ "Total allocated :" + sum + "\n"
 					+ "Available       :" + data + "\n";
 			return result;
 		}
 	}
 	
 	
 	
 	public static class Flt extends Datum {
 		public Flt(Float data) {
 			super(data);
 		}
 		
 		@Override
 		public String toString() {
 			return Float.toString((Float)data);
 		}
 
 		@Override
 		public List<Datum> allocate(int size) {
 			List<Datum> result = new ArrayList<Datum>();
 			
 			float availableQty = (Float) data;
 			while(result.size() < size) {
 				float qty = 0 + ((float) (Math.random() * (availableQty + 1)));
 				result.add(new Datum.Flt(qty));
 				availableQty -= qty;
 			}
 			
 //			TODO: Remove this
 			float sum = 0;
 			for(Datum d : result) {
 				sum += (Float) d.data;
 			}
 			assert sum == (Integer) data : "Datum.Int.allocate failed to allocate accurately!\n"
 					+ "Total allocated :" + sum + "\n"
 					+ "Available       :" + data + "\n";
 			return result;
 		}
 	}
 	
 	
 	
 	public static class Str extends Datum {
 		public Str(String data) {
 			super(data);
 		}
 		
 		@Override
 		public String toString() {
 			return (String) data;
 		}
 
 		@Override
 		public List<Datum> allocate(int size) {
 			throw new IllegalStateException("Cannot allocate value of type " + this.getClass().getName());
 		}
 	}
 	
 	
 	
 	public static class Bool extends Datum {
 		public Bool(Boolean data) {
 			super(data);
 		}
 		
 		@Override
 		public String toString() {
 			return Boolean.toString((Boolean) data);
 		}
 
 		@Override
 		public List<Datum> allocate(int size) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	public abstract List<Datum> allocate(int size);
 }
