 import java.util.ArrayList;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 /***
  * This class is used as a utility class for Stint language to replace the 
  * original String class Java offers
  * @author JohnWoo
  *
  */
 
 public class Stint implements Cloneable{
 
 	StringBuilder content;
 	TreeMap<Integer,Integer> integers;
 	TreeMap<Integer, String> strings;
 	String spliter;
 	String chooser;
 
 	public Stint(){
 		this("");
 	}
 
 	public Stint(String arg){
 		content=new StringBuilder();
 		content.append(arg);
 		integers=new TreeMap<Integer, Integer>();
 		strings=new TreeMap<Integer, String>();
 		update();
 	}
 
 	public Stint(int arg){
 		this(arg+"");
 	}
 
 	public Stint(boolean arg){
 		this(arg?"true":"false");
 	}
 
 	public Stint(Stint s){
 		this(s.toString());
 	}
 
 	/* NEVER call this directly */
 	public String toString(){
 		return content.toString();
 	}
 
 	public boolean equals(Stint s){
 		return content.toString().equals(s.toString());
 	}
 
 	public boolean nonEquals(Stint s){
 		return !equals(s);
 	}
 
 	public Stint add(Stint s){
 		String s1=this.toString();
 		String s2=s.toString();
 		return new Stint(s1+s2);
 	}
 
 	public Stint addAt(Stint s, int index){
 		if(index<0)
 			index=0;
 		String s1=this.toString();
 		String s2=s.toString();
 		if(s1.length()<index || index<0){
 			exception("Stint: Invalid Index: "+index);
 			return this;
 		}
 		if(index==0)
 			return new Stint(s2+s1);
 		if(index==s1.length())
 			return new Stint(s1+s2);
 		return new Stint(s1.substring(0,index)+s2+s1.substring(index));
 	}
 
 	//Not Tested
 	public Stint minus(Stint s){
 		String s1=this.toString();
 		String s2=s.toString();
 		if(s1.indexOf(s2)==-1)
 			return this;
 		else{
 			return new Stint(s1.replaceFirst(s2,""));
 		}
 	}
 
 	//Not Tested
 	public Stint minusAt(Stint s, int index){
 		String s1=this.toString().substring(index);
 		String s2=s.toString();
 		String s3=(index==0?null:this.toString().substring(0,index));
 		if(s1.indexOf(s2)==-1)
 			return this;
 		else{
 			return new Stint(s3+s1.replaceFirst(s2, ""));
 		}
 	}
 
 	public Stint getSubstring(int index){
 		if(index>=this.toString().length()){
 			exception("Stint: Invalid Index");
 			return this;
 		}
 		return new Stint(this.toString().substring(index, index+1));
 	}
 
 	public Stint getSubstring(int start, int length){
 		if(start+length>this.toString().length()){
 			exception("Stint: Invalid Length");
 			return this;
 		}
 		return new Stint(this.toString().substring(start, start+length));
 	}
 
 	public int getInt(int index){
 		if(integers.size()==0){
 			exception("Stint: Invalid Index");
 		}
 		int t=0;
 		int key=0;
 		for(Integer i:integers.keySet()){
 			if(t==index){
 				key=i;
 				break;
 			}
 			else t++;
 		}
 		if(t!=index)
 			exception("Stint: Invalid Index");
 		return integers.get(key);
 	}
 
 	public Stint getString(int index){
 		if(spliter==null && chooser==null){
 			if(strings.size()==0){
 				exception("Stint: Invalid Index");
 			}
 			int t=0;
 			int key=0;
 			for(Integer i:strings.keySet()){
 				if(t==index){
 					key=i;
 					break;
 				}
 				else t++;
 			}
 			if(t!=index)
 				exception("Stint: Invalid Index");
 			return new Stint(strings.get(key));
 		}else if(spliter!=null){
 			String[] temp=this.toString().split(spliter);
 			if(temp.length-1<index)
 				exception("Stint: Invalid Index");
 			return new Stint(temp[index]);
 		}else{
 			return new Stint(chooser);
 		}
 	}
 
 	//Function not Tested
 	public int split(Stint s){
 		spliter=s.toString();
 		chooser=null;
 		return this.toString().split(spliter).length;
 	}
 
 	public int getCount(Stint s){
 		chooser=s.toString();
 		spliter=null;
 		return this.toString().endsWith(s.toString())?this.toString().split(chooser).length:this.toString().split(chooser).length-1;
 	}
 
 	public Stint removeInt(int index){
 		if(index>integers.size()-1)
 			exception("Stint: Invalid Index: "+index);
 		else{
 			int t=0;
 			int key=0;
 			for(Integer i:integers.keySet()){
 				if(t==index){
 					key=i;
 					break;
 				}
 				else t++;
 			}
 			if(t!=index)
 				return this;
 			integers.remove(key);
 			reBuild();
 			update();
 			//			content=new StringBuilder();
 			//			content.append(key);
 			//			update();
 		}
 		return this;
 	}
 
 	public Stint removeRange(int start, int length){
 		String temp=content.toString();
 		if(start==0)
 			temp=temp.substring(length);
 		else temp=temp.substring(0,start)+temp.substring(start+length);
 		content=new StringBuilder();
 		content.append(temp);
 		update();
 		return this;
 	}
 
 	public Stint removeChar(int index){
 		return removeRange(index,1);
 	}
 
 	public Stint removeString(int index){
 		this.setByString(new Stint(),index);
 		update();
 		return this;
 	}
 
 	//a<index>=s
 	public void setByString(Stint s,int index){
 		if(chooser!=null){
 			if(this.getCount(new Stint(chooser))<=index)
 				exception("Stint: Invalid Index");
 			String s1=this.toString();
 			int j=-1;
 			for(int i=0;i<=index;i++){
 				j=s1.indexOf(chooser,j+1);
 			}
 			String s2=this.toString().substring(j);
 			s2=s2.replaceFirst(chooser,s.toString());
 			String f=this.toString().substring(0,j)+s2;
 			content=new StringBuilder();
 			content.append(f);
 			update();
 			return;
 		}
 		if(spliter!=null){
 			if(this.split(new Stint(spliter))<=index)
 				exception("Stint: Invalid Index");
 			String[] temp=this.toString().split(spliter);
 			temp[index]=s.toString();
 			content=new StringBuilder();
 			for(int k=0;k<temp.length;k++){
 				content.append(temp[k]);
 				if(k!=temp.length-1)
 					content.append(spliter);
 			}
 			
 			update();
 			return;
 		}
 		if(strings.size()==0){
 			exception("Stint: Invalid Index");
 		}
 		int t=0;
 		int key=0;
 		for(Integer i:strings.keySet()){
 			if(t==index){
 				key=i;
 				break;
 			}
 			else t++;
 		}
 		if(t!=index)
 			return;
 		strings.remove(key);
 		strings.put(key,s.toString());
 		reBuild();
		update();
 	}
 
 	//a[index]=s
 	public void setByIndex(Stint s, int index){
 		String temp=content.toString();
 		if(index==0){
 			temp=s.toString()+temp.substring(1);
 			content=new StringBuilder();
 			content.append(temp);
 		}
 		else if(index==content.length()-1){
 			temp=temp.substring(0,content.length()-1);
 			temp=temp+s.toString();
 			content=new StringBuilder();
 			content.append(temp);
 		}
 		else{
 			String left=content.substring(0,index);
 			String right=content.substring(index+1);
 			content=new StringBuilder();
 			content.append(left+s.toString()+right);
 		}
 		update();
 	}
 
 	//a.<index>=value
 	public void setByInt(int value, int index){
 		if(integers.size()==0){
 			exception("Stint: Invalid Index");
 		}
 		int t=0;
 		int key=-1;
 		for(Integer i:integers.keySet()){
 			if(t==index){
 				key=i;
 				break;
 			}
 			else t++;
 		}
 		if(t!=index || key==-1)
 			return;
 		integers.remove(key);
 		integers.put(key,value);
 		reBuild();
 	}
 
 	//a[start, length]=s
 	public void setByRange(Stint s, int start, int length){
 		String temp=content.toString();
 		if(start==0)
 			temp=s.toString()+temp.substring(length);
 		else temp=temp.substring(0,start)+s.toString()+temp.substring(start+length);
 		content=new StringBuilder();
 		content.append(temp);
 		update();
 	}
 	
 	/* Below are methods that are used to support build-in functions */
 	
 	public Stint getUpperCase(){
 		return new Stint(content.toString().toUpperCase());
 	}
 	
 	public Stint getoLowerCase(){
 		return new Stint(content.toString().toLowerCase());
 	}
 
 	/* Below are private methods for the maintaince of internal structure */
 
 	public Stint clone(){
 		return new Stint(content.toString());
 	}
 
 	private void update(){
 		chooser=null;
 		integers.clear();
 		strings.clear();
 		String temp=content.toString();
 		if(temp.length()==0)
 			return;
 		String[] ints=temp.split("[^0-9]+");
 		ArrayList<String> t=new ArrayList<String>();
 		for(String s:ints){
 			if(!s.equals(""))
 				t.add(s);
 		}
 		ints=new String[t.size()];
 		t.toArray(ints);
 		int index=0;
 		if(ints.length>0 && temp.startsWith(ints[0])){
 			integers.put(0, Integer.parseInt(ints[0]));
 			index=2;
 		}else{
 			index=1;
 		}
 		for(int i=(index==2?1:0);i<ints.length;i++){
 			integers.put(index, Integer.parseInt(ints[i]));
 			index=index+2;
 		}
 
 		String[] strs=temp.split("[0-9]+");
 		t=new ArrayList<String>();
 		for(String s:strs){
 			if(!s.equals(""))
 				t.add(s);
 		}
 		strs=new String[t.size()];
 		t.toArray(strs);
 		index=0;
 		if(strs.length>0 && temp.startsWith(strs[0])){
 			strings.put(0, strs[0]);
 			index=2;
 		}else{
 			index=1;
 		}
 		for(int i=(index==2?1:0);i<strs.length;i++){
 			strings.put(index, strs[i]);
 			index=index+2;
 		}
 	}
 
 	private String reBuild(){
 		chooser=null;
 		TreeMap<Integer, String> temp=new TreeMap<Integer, String>();
 		for(Entry<Integer, String> e:strings.entrySet()){
 			temp.put(e.getKey(), e.getValue());
 		}
 		for(Entry<Integer, Integer> e:integers.entrySet()){
 			temp.put(e.getKey(), e.getValue()+"");
 		}
 		content=new StringBuilder();
 		for(Entry<Integer, String> e:temp.entrySet()){
 			content.append(e.getValue());
 		}
 		return content.toString();
 	}
 
 	private void exception(String message){
 		throw new RuntimeException(message);
 	}
 
 }
