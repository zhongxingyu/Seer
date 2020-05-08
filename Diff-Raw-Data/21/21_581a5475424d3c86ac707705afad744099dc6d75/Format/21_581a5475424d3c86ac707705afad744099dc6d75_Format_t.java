 package uk.co.brotherlogic.mdb;
 
 /**
  * Class to represent a format
  * @author Simon Tucker
  */
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 public class Format implements Comparable<Format>
 {
 	String name;
 	TreeSet<Category> categories;
 	int formatNumber;
 
 	public Format()
 	{
 		name = "";
 		categories = new TreeSet<Category>();
 		formatNumber = 0;
 	}
 
 	public Format(int num, String sIn)
 	{
 		name = sIn;
 		formatNumber = num;
 		categories = new TreeSet<Category>();
 	}
 
 	public Format(int num, String sIn, Collection<Category> cats)
 	{
 		name = sIn;
 		formatNumber = num;
 		categories = new TreeSet<Category>(cats);
 	}
 
 	public void addCategory(Category cat)
 	{
 		categories.add(cat);
 	}
 
 	public int compareTo(Format o)
 	{
 		return name.compareTo(o.getName());
 	}
 
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Format)
			return compareTo((Format) o) == 0;
		else
			return false;
	}

 	public String fullString()
 	{
 		String out = formatNumber + ": " + name + "\n";
 		Iterator<Category> cIt = categories.iterator();
 		while (cIt.hasNext())
 			out += cIt.next() + ", ";
 
 		return out;
 	}
 
 	public Collection<Category> getCategories()
 	{
 		return categories;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public int getNumber()
 	{
 		return formatNumber;
 	}
 
 	public void setCategories(Collection<Category> vec)
 	{
 		// Clear the old categories
 		categories.clear();
 
 		// Add the new formats
 		categories.addAll(vec);
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public void setNumber(int number)
 	{
 		formatNumber = number;
 	}
 
 	public String toString()
 	{
 		return name;
 	}
 }
