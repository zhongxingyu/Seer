 package xtc.oop.helper;
 import java.util.ArrayList;
 
 public class Bubble{
     String name;
     String[] methods;
     String[] dataFields;
     String packageName;
     Bubble parent;
     String[] children;
     Bubble[] bChildren;
     ArrayList<String> vtable;
     String[] constructors;
 
     public Bubble(String name, String[] methods,
 		  String[] dataFields, Bubble parent, String[] children, String packageName, String[] constructors){
 
         this.name = name;
         this.packageName = packageName;
         this.methods = methods;
         this.dataFields = dataFields;
         this.parent = parent;
         this.children = children;
         this.vtable = new ArrayList<String>();
         this.constructors = constructors;
     }
 
     public Bubble(String name, String child) {
         this.name = name;
         if (child != null) {
             String temp[] = { child };
             this.children = temp;
         }
         this.vtable = new ArrayList<String>();
         this.methods = null;
     }
 
     public void setConstructors(String[] constructors){
         this.constructors = constructors;
     }
 
     public String[] getConstructors(){
         return this.constructors;
     }
 
     public void setPackageName(String name){
         this.packageName = name;
     }
 
     public String getPackageName(){
         return this.packageName;
     }
 
     public void setMethods(String[] methods) {
 	if (methods == null) {
 	    return;
 	}
 	this.methods = methods;
     }
 
     public String[] getMethods(){
         return this.methods;
     }
 
 
 
     //changed to make it arraylist
     public void setVtable(ArrayList<String> vtable) {
 	if (vtable == null) {
 	    return;
 	}
 	this.vtable = vtable;
     }
 
     public void add2Vtable(String add){
 	//format the string
 	add = format(add, this);
 	//if it's a method [in the format: rt_type (*name)(params) ]
 	if(add.matches(".*\\(\\*.*\\)\\(.*\\).*")) {
 	    String sig = add.split("([\\w\\s]*\\(\\*)|(\\)\\(.*)")[1];
 	    //	    System.out.println("SIG: \t\t"+sig);
 	    int index = -1;
 	    for(int i = 0; i < this.vtable.size(); i++) {
 		//System.out.println("-----"+this.vtable.get(i));
 		if(this.vtable.get(i).matches(".*\\(\\*.*\\)\\(.*\\).*") &&
 		    this.vtable.get(i).split("([\\w\\s]*\\(\\*)|(\\)\\(.*)")[1].equals(sig))
 		    {
 			//System.out.println("WOWOOWOWOWOWOWOWOWOWO");
 			index = i;
 		    }
 	    }
 
 	    if(index != -1) {
 		System.out.println("==========OVERWRITING " + sig + "in " + this.name);
 
 		this.vtable.set(index,add + "\t");
 	    }
 	    else {
 		this.vtable.add(add);
 	    }
 
 	}
 	//if it's not a method
 	else {
 	    this.vtable.add(add);
 	}
     }
 
     public ArrayList<String> getVtable(){
         return this.vtable;
     }
 
     public void printVtable(){
         System.out.println("================================");
         System.out.println(this.name + "'s vtable:");
         for(String s : this.vtable)
             System.out.println(s);
 
         System.out.println("================================");
     }
 
     public void setDataFields(String[] dataFields) {
 	if (dataFields == null) {
 	    return;
 	}
 
 	//find number of non-null strings
 	int r_length = 0;
 	for(int i = 0; i < dataFields.length; i++) {
 	    if(!dataFields[i].equals(""))
 		r_length++;
 	}
 	//make temp array of correct size
 	String [] temp = new String [r_length];
 	int temp_i = 0;
 
 	for(int i = 0; i < dataFields.length; i++) {
 	    //types
 	    dataFields[i] = dataFields[i].replaceAll("(?<!\\w)int(?!\\w)","int32_t");
 	    dataFields[i] = dataFields[i].replaceAll("(?<!\\w)boolean(?!\\w)","bool");
 	    dataFields[i] = dataFields[i].replaceAll("(?<!\\w)final(?!\\w)","const");
 
 	    //don't add nulls to temp
 	    if(!dataFields[i].equals("")){
		System.out.println("_______________________"+dataFields[i]);
 		temp[temp_i++] = dataFields[i]+";";
 	    }
 	}
 
 	this.dataFields = temp;
     }
 
     public String[] getDataFields(){
         return this.dataFields;
     }
 
     public String getName() {
 	if (name == null) {
 	    return "No Name";
 	}
 	return name;
     }
 
     public Bubble getParent() {
 	return parent;
     }
 
     public void setParent(Bubble parent) {
 	if (parent == null) {
 	    return;
 	}
 	this.parent = parent;
     }
 
     public void setChildren(String[] children) {
 	if (children == null) {
 	    return;
 	}
 	this.children = children;
     }
 
     public String[] getChildren()
     {
         return this.children;
     }
 
     //sets the vtable at index i to string s
     public void setVtableIndex(int i, String s)
     {
         this.vtable.set(i, s);
     }
 
     public void addChild(String child) {
 	if (child == null) {
 	    return;
 	}
         int len = children == null ? 1 : children.length + 1;
         String[] temp = new String[len];
         if (children == null) {
             temp[0] = child;
             children = temp;
         }
         else {
             for (int i = 0; i < children.length; i++) {
                 temp[i] = children[i];
             }
 
             temp[len - 1] = child;
             children = temp;
         }
     }
 
     public String childrenToString() {
 	if (children == null) {
 	    return "No Children";
 	}
 	else {
 	    StringBuilder s = new StringBuilder("[");
 	    for (int i = 0; i < children.length; i++) {
 		s.append(children[i]);
 		if (i != children.length - 1)
 		    s.append(", ");
 	    }
 	    return s.append("]").toString();
 	}
     }
 
     public String parentToString(){
         if(this.parent != null) {
             return this.parent.getName();
 	}
         else {
             return "No Parent";
 	}
     }
 
     public String toString() {
         StringBuilder s = new StringBuilder("Name: " + getName() + "\n");
         s.append("Package: " + getPackageName() + "\n");
         s.append("Children: " + childrenToString() + "\n");
         s.append("Parent: " + parentToString());
         return s.toString();
     }
 
     public String format(String method, Bubble b) {
 	//System.out.println(method);
 	if (method.startsWith(" ")) {
 	    int square = 0;
 	    for (int i = 0; i < method.length(); i++) {
 		if (method.charAt(i) == '[') square++;
 	    }
 	    String[] temp2 = method.split(" ");
 	    int count = 0;
 	    for (int j = 0; j < temp2.length; j++) {
 		if (temp2[j].length() != 0) count++;
 	    }
 
 	    String[] temp = new String[count-square];
 	    int index = 0;
 	    for (int j = 0; j < temp2.length; j++) {
 		if (temp2[j].length() != 0) {
 		    if (temp2[j].charAt(0) == '[') {
 			temp[index-1] += "[]";
 		    }
 		    else {
 			temp[index++] = temp2[j];
 		    }
 		}
 	    }
 
 	    int num = 0;
 	    for (int j = 0; j < temp.length; j++) {
 		if (temp[j].equals("public") ||
 		    temp[j].equals("private") ||
 		    temp[j].equals("protected") ||
 		    temp[j].equals("static")) {
 		    //do nothing
 		}
 		else {
 		    num++;
 		}
 	    }
 	    String s = "";
 	    if (num % 2 == 0) { // there is a return type
 		s += temp[temp.length-num] + " ";
 		index = temp.length-num+1;
 	    }
 	    else { // void
 		s += "void ";
 		index = temp.length-num;
 	    }
 
 	    s += "(*" + temp[temp.length-1] + ")(" + b.getName();
 
 	    for (int j = index; j < temp.length - 1; j+=2) {
 		s += ", " + temp[j];
 	    }
 
 
 	    s += ");";
 	    return s;
 	}
 	return method;
     }
 }
