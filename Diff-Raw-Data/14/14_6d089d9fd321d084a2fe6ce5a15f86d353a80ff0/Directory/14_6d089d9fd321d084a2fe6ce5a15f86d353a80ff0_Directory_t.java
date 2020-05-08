 
 import java.util.*;
 import java.io.*;
 
 public class Directory extends Component implements Serializable{
 	/*Constructors*/
 	public Directory(String name, Component parent) {
 		super(name);
 		
 		mComponents = new ArrayList<Component>();
 		mParent = parent;
 		
 	}
 	
 	/*Members*/
 	private ArrayList<Component> mComponents;
 	private Component mParent;
 	
 	/*Attributes*/
 	
 	/*Methods*/
 	
 	public Component getParent() {
 	
 		return mParent;
 	}
 	
 	public Component getComponent(String name) {
 	
 		/* 
 		 * Searches directory for component with specific name,
 		 * returns null if the component isn't found
 		 */ 
 		
 		Component returnComponent = null;
 		
 		for(int i=0; i<mComponents.size(); i++)
 		{
 			if(mComponents.get(i).name().equals(name)) {
 				
 				returnComponent = mComponents.get(i);
 			}
 		}
 		
 		if(name.equals("..")) {
 			
 			returnComponent = mParent;
 		}
		if(name.equals(".")) {
 			
			Directory tempDirectory = this;
			Directory parent = null;
 			
			while(tempDirectory.getParent() != null) {
 			
				tempDirectory = (Directory)tempDirectory.getParent();
 			}
 			
 			returnComponent = tempDirectory;
 		}
 		
 		return returnComponent;
 	}
 	
 	public Component getComponent(int index) {
 	
 		/* 
 		 * Returns component that resides on the given index.
 		 * Returns null if the index is invalid
 		 */ 
 		
 		Component returnComponent = null;
 	
 		if(index>=0 && index<mComponents.size()) {
 		
 			returnComponent = mComponents.get(index);
 		}
 		
 		return returnComponent;
 	}
 	
 	public void addComponent(Component component) {
 	
 		mComponents.add(component);
 	}
 	
 	public boolean removeComponent(Component component) {
 	
 		boolean success = false;
 		success = mComponents.remove(component);
 		
 		return success;
 	} 
 	
 	public boolean removeComponent(int index) {
 	
 		boolean success = false;
 		Component temp = null;
 	
 		if(index>=0 && index<mComponents.size()) {
 		
 			temp = mComponents.remove(index);
 			
 			if(temp != null) {
 				success = true;	
 			}
 		}
 		
 		return success;
 	}
 	
 	public boolean removeComponent(String name) {
 	
 		boolean success = false;
 		Component temp = null;
 		
 		for(int i=0; i<mComponents.size(); i++) {
 		
 			if(mComponents.get(i).name().equals(name)) {
 			
 				temp = mComponents.remove(i);
 				
 				if(temp != null) {
 					success = true;
 				}
 			}
 		}
 		
 		return success;
 	}
 	
 	public void print() {
 	
 		for(int i=0; i<mComponents.size(); i++) {
 		
 			System.out.println(mComponents.get(i).name());
 		}
 	}
 }
