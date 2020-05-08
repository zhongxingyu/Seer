 /*
  * Created by IntelliJ IDEA.
  * User: guillaume
  * Date: 20 juin 02
  * Time: 00:26:12
  * To change template for new class use 
  * Code Style | Class Templates options (Tools | IDE Options).
  */
 package zz.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractListModel;
 
 public class SimpleListModel extends AbstractListModel
 {
 	private List itsList;
 
 	public SimpleListModel(){
 		this(new ArrayList());	
 	}
 
 	public SimpleListModel (List aList)
 	{
 		itsList = aList;
 	}
 
 
 	public int getIndexOf (Object aElement)
 	{
 		if (itsList == null) return -1;
 		else return itsList.indexOf(aElement);
 	}
 
 	public Object getElementAt (int index)
 	{
 		return itsList.get (index);
 	}
 
 	public int getSize ()
 	{
 		if (itsList == null) return 0;
 		else return itsList.size();
 	}
 
 	public void setList(List aList){
 		if (itsList != null && getSize()>0) fireIntervalRemoved(this,0,getSize()-1)	;
 		itsList = aList;
 		if (itsList != null && getSize()>0) fireIntervalAdded(this,0,getSize()-1)	;
 	}
 
	public List getList()
	{
		return itsList;
	}
	
 	public void fireContentsChanged ()
 	{
 		fireContentsChanged (this, 0, getSize());
 	}
 
 	public void fireContentsChanged (int index0, int index1)
 	{
 		super.fireContentsChanged (this, index0, index1);
 	}
 
 	public void fireIntervalAdded (int index0, int index1)
 	{
 		super.fireIntervalAdded (this, index0, index1);
 	}
 
 	public void fireIntervalRemoved (int index0, int index1)
 	{
 		super.fireIntervalRemoved (this, index0, index1);
 	}
 }
