 package ex03_10;
 
 import ex03_09.Vehicle;
 
 public class LinkedList implements Cloneable {
 	private Object data; // 変更を許さない
 	private LinkedList next;
 
 	LinkedList(Object obj, LinkedList nextData) {
 		this.data = obj;
 		this.next = nextData;
 	}
 
 	public Object getObject() {
 		return data;
 	}
 
 	public LinkedList getLinkedList() {
 		return next;
 	}
 
 	public int countList() {
 		int num = 1;
 		LinkedList target = this;
 		for (; null != target.next; num++) {
 			target = target.next;
 		}
 
 		return num;
 	}
 
 	public void show() {
 		if (data instanceof Vehicle) {
 			Vehicle obj = (Vehicle) this.data;
 			obj.print();
 		}
 		if (this.next != null) {
 			next.show();
 		}
 	}
 
 	public String toString() {
 		String kaigyo = System.getProperty("line.separator");
 		String buf;
 		Object data = this.data;
 		buf = kaigyo + data.toString();
 		Object next = this.next;
 		if (next != null) {
 			buf += next.toString() + kaigyo;
 		}
 		return buf;
 	}
 
	public void setCar(Object car) {
		this.data = car;
 	}
 
 	public LinkedList clone() {
 		try {
 			LinkedList obj = (LinkedList) super.clone();
 			LinkedList next = obj.next;
 			if (next != null) {
				next.clone();
 			}
 			return obj;
 		} catch (CloneNotSupportedException e) {
 			throw new InternalError(e.toString());
 		}
 	}
 
 	public static void main(String[] args) {
 
 		Vehicle car = new Vehicle("Satoh");
 		LinkedList arg3 = new LinkedList(car, null);
 
 		car = new Vehicle("Mike");
 		LinkedList arg2 = new LinkedList(car, arg3);
 
 		car = new Vehicle("Bob");
 		LinkedList arg1 = new LinkedList(car, arg2);
 
 		LinkedList cloneArg1 = arg1.clone();
 		car = new Vehicle("Suzuki");
		cloneArg1.setCar(car);
 
 		System.out.println("toString = " + arg1.toString());
 		System.out.println("CountList = " + arg1.countList());
 		System.out.println("Clone List");
 		System.out.println("toString = " + cloneArg1.toString());
 		System.out.println("CountList = " + cloneArg1.countList());
 	}
 }
