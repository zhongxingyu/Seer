 import java.util.Vector;
 
 public class Tree
 {
 	//Private Members
 	private Vector<Tree> _branches;
 
 	private String _printValue;
 	private String _displayValue;
 
 
 	// Constructors
 	public Tree(String displayValue, String printValue)
 	{
 		_displayValue = displayValue;
 		_printValue = printValue;
		_branches = new Vector<Tree>();
 	}
 
 	public Tree(String displayValue)
 	{
 		this(displayValue, "");
 	}
 
 	public Tree()
 	{
 		this("");
 	}
 
 	// Accessors
 	public Tree next(int branch)
 	{
 		return _branches.elementAt(branch);
 	}
 
 	public String displayValue()
 	{
 		return _displayValue;
 	}
 
 	public String printValue()
 	{
 		return _printValue;
 	}
 
 	public boolean isLeaf()
 	{
 		return _branches.size() == 0;
 	}
 
 	// Mutators
 	public Tree addNode(int location, Tree t)
 	{
 		_branches.add(location, t);
 		return t;
 	}
 
 	public Tree addNode(int location, String displayValue, String printValue)
 	{
 		return addNode(location, new Tree(displayValue, printValue));
 	}
 
 	public Tree addNode(int location, String displayValue)
 	{
 		return addNode(location, displayValue, "");
 	}
 
 	public Tree addNode(int location)
 	{
 		return addNode(location, "");
 	}
 
 	public Tree addNode(String displayValue, String printValue)
 	{
 		return addNode(_branches.size(), displayValue, printValue);
 	}
 
 	public Tree addNode(String displayValue)
 	{
 		return addNode(displayValue, "");
 	}
 
 	public Tree addNode()
 	{
 		return addNode("");
 	}
 }
