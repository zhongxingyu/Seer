 package com.deaboy.manhunt.settings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jnbt.ListTag;
 import org.jnbt.StringTag;
 import org.jnbt.Tag;
 
 public class SettingListString extends SettingBase<ArrayList<String>> implements Setting, SettingList
 {
 
 	public SettingListString(String label, String description, String...defaultValues)
 	{
 		super(label, new ArrayList<String>(), SettingType.LIST_STRING, description);
 		
 		ArrayList<String> list = new ArrayList<String>();
 		for (String value : defaultValues)
 			list.add(value);
 		
 		super.setValue(list);
 	}
 	
 	@Override
 	public boolean setValue(String value)
 	{
		String[] values1 = value.split(",");
		ArrayList<String> values2 = new ArrayList<String>(values1.length);
 		
 		for (int i = 0; i < values1.length; i++)
 		{
			values2.set(i, values1[i]);
 		}
 		
		return super.setValue(values1);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public ArrayList<String> getValue()
 	{
 		return (ArrayList<String>) super.getValue();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public ArrayList<String> getValueDefault()
 	{
 		return (ArrayList<String>) super.getValueDefault();
 	}
 	
 	@Override
 	public void add(Object o) throws IllegalArgumentException
 	{
 		if (!(o instanceof String))
 		{
 			throw new IllegalArgumentException("Argument must be of type String");
 		}
 		
 		ArrayList<String> list = getValue();
 		list.add((String) o);
 		setValue(list);
 	}
 	
 	@Override
 	public void add(Object o, int i) throws IllegalArgumentException
 	{
 		if (!(o instanceof String))
 		{
 			throw new IllegalArgumentException("Argument must be of type String");
 		}
 		
 		ArrayList<String> list = getValue();
 		list.set(i, (String) o);
 		setValue(list);
 	}
 	
 	@Override
 	public void remove(Object o) throws IllegalArgumentException
 	{
 		if (!(o instanceof String))
 		{
 			throw new IllegalArgumentException("Argument must be of type String");
 		}
 		
 		ArrayList<String> list = getValue();
 		if (!list.contains((String) o))
 		{
 			return;
 		}
 		list.remove((String) o);
 		setValue(list);
 	}
 	
 	@Override
 	public void remove(int i)
 	{
 		ArrayList<String> list = getValue();
 		if (list.size() <= i)
 		{
 			return;
 		}
 		list.remove(i);
 		setValue(list);
 	}
 	
 	@Override
 	public void clear()
 	{
 		setValue(new ArrayList<String>());
 	}
 	
 	@Override
 	public String getDescription()
 	{
 		return super.getDescriptions().get(0);
 	}
 	
 	@Override
 	public String toString()
 	{
 		int i;
 		String string = new String();
 		ArrayList<String> list = getValue();
 		
 		for (i = 0; i < list.size(); i++)
 		{
 			string += list.get(i) + ",";
 		}
 		
 		return string;
 	}
 	
 	@Override
 	public ListTag getNBT()
 	{
 		List<Tag> tags = new ArrayList<Tag>();
 		
 		for (String string : getValue())
 		{
 			tags.add(new StringTag("", string));
 		}
 		
 		return new ListTag(getLabel(), StringTag.class, tags);
 	}
 	
 }
