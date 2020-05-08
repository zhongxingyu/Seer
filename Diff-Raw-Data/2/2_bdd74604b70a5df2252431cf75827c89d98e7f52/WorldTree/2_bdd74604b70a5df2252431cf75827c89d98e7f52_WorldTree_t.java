 package internal.tree;
 
 import internal.parser.containers.Constraint;
 import internal.parser.containers.Datum;
 import internal.parser.containers.condition.Condition;
 import internal.parser.containers.condition.ICondition;
 import internal.parser.containers.pattern.IPattern;
 import internal.parser.containers.pattern.Pattern;
 import internal.parser.containers.query.IQuery;
 import internal.parser.containers.query.Query;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Stack;
 
 import static internal.Helper.titleCase;
 
 /**
  * WorldTree is the abstract class that every object in the hierarchy extends.
  * It is used to provide a common structure and interface to all the objects in the world.
  * 
  * @author guru
  */
 
 public abstract class WorldTree implements IWorldTree, Serializable {
 	private static final long serialVersionUID = -5257914696549384766L;
 	
 	protected IWorldTree parent;
 	protected Collection<IWorldTree> children;
 	private String name;
 	private Collection<Constraint> constraints;
 	private Map<String, Datum> properties;
 	protected List<String> stringRepresentation;
 
 	protected WorldTree(String name, IWorldTree parent, Collection<Constraint> constraints) {
 		this.parent 		= parent;
 		this.children 		= null;
 		this.name 			= name;
 		this.constraints 	= constraints;
 		this.stringRepresentation 	= new ArrayList<String>();
 		this.properties				= new HashMap<String, Datum>();
 		
 		if(parent != null) {
 			IWorldTree root = this.root();
 			if(root.constraints() != null) {
 				for(Constraint c : root.constraints()) {
 					String constraintClass 	= c.query().level().getName();
 					String constraintClassLevel = constraintClass.substring(constraintClass.indexOf("$") + 1);
 					
 					String myClass			= this.getClass().getName();
 					String myClassLevel		= myClass.substring(myClass.indexOf("$") + 1);
 					if(myClassLevel.equalsIgnoreCase(constraintClassLevel))
 						constraints.add(c);
 				}
 			}
 		}
 	}
 
 	public String name() {
 		return name;
 	}
 	
 	public String absoluteName() {
 		Stack<String> stack = new Stack<String>();
 		
 		IWorldTree node = this;
 		while(node.parent() != null) {
 			stack.push(node.name());
 			node = node.parent();
 		}
 		
 		StringBuffer result = new StringBuffer();
 		while(stack.size() > 1)
 			result.append(stack.pop() + " -> ");
 		result.append(stack.pop());
 		
 		return result.toString();
 	}
 	
 	public IWorldTree parent() {
 		return parent;
 	}
 	
 	public Collection<IWorldTree> children() {
 		return children;
 	}
 	
 	@Override
 	public Collection<IWorldTree> getAllChildren() {
 		List<IWorldTree> result = new ArrayList<IWorldTree>();
 		
 		result.addAll(this.children());
 		
 		IWorldTree node = null;
 		int listIndex = 0;
 		while(listIndex < result.size()) {
 			node = result.get(listIndex);
 			result.addAll(node.children());
 			listIndex++;
 		}
 		return result;
 	}
 	
 	@Override
 	public Collection<IWorldTree> getChildrenByClass(String className) {
 		List<IWorldTree> result			= new ArrayList<IWorldTree>();
 		Class<?> clazz = null;
 		
 		className = (WorldTreeFactory.class.getName() + "$" + className);
 		try {
 			clazz = Class.forName(className);
 		} catch (ClassNotFoundException e) {
 			System.err.println("No class found with name :" + className);
 		}
 		
 		for(IWorldTree child : getAllChildren()) {
 			if(!child.getClass().equals(clazz))
 				result.add(child);
 		}
 		return result;
 	}
 	
 	public IWorldTree root() {
 		if(this.parent == null)
 			return this;
 		else
 			return this.parent.root();
 	}
 	
 	@Override
 	public Collection<Constraint> constraints() {
 		return constraints;
 	}
 	
 	@Override
 	public void addConstraint(Constraint constraint) {
 		assert constraints != null : "Trying to add constraint to " + name + " when " + name + ".constraints = null\n";
 		constraints.add(constraint);
 	}
 	
 	@Override
 	public void addProperty(String name, Datum value) {
 		properties.put(name, value);
 	}
 	
 	@Override
 	public Map<String, Datum> properties() {
 		return properties;
 	}
 	
 //	FIXME: Added this to solve NPE on constraints()
 	@Override
 	public void setConstraints(Collection<Constraint> constraints) {
 		this.constraints = constraints;
 	}
 	
 	protected void pushDownConstraints() {
 		if(this.children == null)
 			return;
 		for(Constraint c : this.constraints) {
 			try {
 				Class<?> constraintClass = Class.forName(WorldTreeFactory.class.getName() + "$" + titleCase(c.level()));
 				if(constraintClass.equals(this.getClass())) {
 					Datum d = c.condition().value();
 					List<Datum> values = d.split(children.size());
 					for(IWorldTree child : children) {
 						Datum value = values.get((new Random().nextInt(values.size())));
 						String className = child.getClass().getName();
 						String level = className.substring(className.indexOf("$") + 1);
 
 //						FIXME: This will only work for very simple conditions!
 						IQuery subConstraintQuery 			= null;
 						ICondition subConstraintCondition 	= null;
 						
 						{
 							subConstraintCondition = new Condition(c.condition().notFlag(), c.condition());
 							subConstraintCondition.setValue(value);
 							
 							IPattern constraintQueryPattern 	= c.query().pattern();
 							ICondition constraintQueryCondition = c.query().condition();
 							IQuery constraintSubQuery 			= c.query().subQuery();
 							
 //							FIXME: This will fail if constraintSubQuery is anything other than null due to different 'level'
 							subConstraintQuery	= new Query(level, constraintQueryPattern, constraintQueryCondition, constraintSubQuery);
 						}
 						
 						Constraint subConstraint = new Constraint(level, subConstraintQuery, subConstraintCondition);
 						child.addConstraint(subConstraint);
						
						values.remove(value);
 					}
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/* -------------------------------------------  String methods  ------------------------------------------- */
 	public List<String> getStringRepresentation() {
 		stringRepresentation.removeAll(stringRepresentation);
 		initString();
 		return stringRepresentation;
 	}
 	
 	@Override
 	public String toString() {
 		List<String> stringList = getStringRepresentation();
 		StringBuffer result = new StringBuffer();
 		for(String string : stringList)
 			result.append(string + System.getProperty("line.separator"));
 		
 		return result.toString();
 	}
 	
 	/**
 	 * Method provided to initialize the string representation of this instance.
 	 * This method is to be called once all it's children have been initialized.
 	 * <p>
 	 * The logic is as follows:<br>
 	 * Every child is expected to have a {@code List<String>} representing each line of its visual.<br>
 	 * We need to concatenate each line of every child together and then CR+LF onto the next line.<br>
 	 * {@code listStringList} contains the list of stringLists (2-D {@code ArrayList}).<br>
 	 * We append every line of every {@code List<List<String>>} before moving onto the next index.<br>
 	 */
 	protected void initString() {
 		if(children() == null)
 			return;
 		List<List<String>> listStringList = new ArrayList<List<String>>();
 		for(IWorldTree child : children()) {
 			listStringList.add(child.getStringRepresentation());
 		}
 		
 //		Check for equal lines
 		int maxListSize = 0;
 		for(List<String> list : listStringList) {
 			maxListSize = maxListSize > list.size() ? maxListSize : list.size();
 		}
 		
 		for(List<String> list : listStringList) {
 			if(list.size() < maxListSize) {
 //				Add new strings of largest length to this list
 				int maxLength = 0;
 				for(String s :  list) {
 					maxLength = maxLength > s.length() ? maxLength : s.length();
 				}
 				StringBuffer emptySB = new StringBuffer();
 				while(emptySB.length() < maxLength)
 					emptySB.append(" ");
 				
 //				Now add them to the list
 				while(list.size() < maxListSize)
 					list.add(0, emptySB.toString());
 			}
 		}
 		
 		int lineCount = 0;
 		for(List<String> stringList : listStringList)
 				lineCount = lineCount > stringList.size() ? lineCount : stringList.size();
 		for(int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
 			StringBuffer fullLine = new StringBuffer();
 			for(List<String> stringList : listStringList) {
 				if(stringList.size() > lineIndex)
 					fullLine.append(stringList.get(lineIndex) + " ");
 			}
 //			if(!stringRepresentation.contains(fullLine.toString()))
 				stringRepresentation.add(fullLine.toString());
 		}
 		
 //		We have obtained every line as we should. We now need to own everything that is within is.
 		prepareToString();
 		
 	}
 	
 	/**
 	 * Helper method that wraps around the initialized string representation to print ownership in the visual.
 	 */
 	protected void prepareToString() {
 //		The string representation is in place. find the maximum length and wrap around it to own it.\
 //		Make the top part of the outer shell that wraps around all of this instance's children.
 		List<String> newStringRepresentation = new ArrayList<String>();
 		int maxLineLength = 0;
 		for(String string : stringRepresentation)
 			maxLineLength = string.length() > maxLineLength ? string.length() : maxLineLength;
 		StringBuffer header = new StringBuffer();
 		maxLineLength += this.name.length();
 		maxLineLength += (maxLineLength % 2 == 0) ? 0 : 1;
 		header.append("+");
 		for(int i = 0; i < maxLineLength; i++)
 			header.append("-");
 		header.append("+");
 		newStringRepresentation.add(header.toString());
 		header = new StringBuffer();
 		header.append("|" + this.name);
 		for(int i = 0; i < maxLineLength - name.length(); i++)
 			header.append(" ");
 		header.append("|");
 		maxLineLength = header.toString().length();
 		
 		newStringRepresentation.add(header.toString());
 		header = null;
 		
 //		Now that the top is done, add the middle components (string representation of children)
 		for(String string : stringRepresentation) {
 			int spaces = maxLineLength - string.length() - 2;	//The 2 is because of the starting and ending '|'
 			StringBuffer line = new StringBuffer("|");
 			for(int i = 0; i <= spaces / 2; i++)
 				line.append(" ");
 			line.append(string);
 			while(line.length() < maxLineLength - 1)
 				line.append(" ");
 			line.append("|");
 			if(line.length() != newStringRepresentation.get(0).length())
 				throw new IllegalStateException();
 			newStringRepresentation.add(line.toString());
 //			System.out.println(newStringRepresentation.toString().replaceAll("(\\[|\\]|,  )", ""));
 		}
 		
 //		Add the bottom part of the outer shell that wraps around all of this instance's children
 		StringBuffer footer = new StringBuffer();
 		footer.append("|");
 		for(int i = 0; i < maxLineLength - 2; i++)
 			footer.append(" ");
 		footer.append("|");
 		newStringRepresentation.add(footer.toString());
 		String line = footer.toString();
 		footer = new StringBuffer();
 		
 		line = line.replace(" ", "-").replace("|", "+");
 		footer.append(line);
 		newStringRepresentation.add(footer.toString());
 		footer = null;
 		
 //		Update pointer.
 		stringRepresentation = newStringRepresentation;
 	}
 }
