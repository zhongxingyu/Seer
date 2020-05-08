 
 public class Queue {
 
 	private Node _root;
 	private Node _end;
 	private int _count;
 	
 	public Queue()
 	{
 		_count = 0;
 		_root = null;
 	}
 	
 	public void enqueue(Object item)
 	{
 		Node newNode = new Node(item);
 		
 		if (_count++ == 0)
 		{
 			_root = newNode;
 			_end = newNode;
 		}
 		else
 		{
 			newNode.Next = _root;
 			_root = newNode;
 			_root.Next.Last = _root;
 		}
 	}
 	
 	public Object dequeue()
 	{	
 		Object data;
 		
 		if (_count == 0)
 			return null;
 		else if (_count == 1)
 		{
 			data = _end.Data;
 			_end = null;
 			_root = null;
 		}	
 		else
 		{
 			data = _end.Data;
 			_end = _end.Last;
			_end.Next = null;
 		}
 		
 		_count--;
 		return data;
 	}
 	
 	public Object peek()
 	{
 		if (_end != null)
 			return _end.Data;
 		
 		return null;
 	}
 	
 	public int count()
 	{
 		return _count;
 	}
 	
 	public String toString()
 	{
 		return (_root != null ? _root.toString() : "Empty");
 	}
 }
