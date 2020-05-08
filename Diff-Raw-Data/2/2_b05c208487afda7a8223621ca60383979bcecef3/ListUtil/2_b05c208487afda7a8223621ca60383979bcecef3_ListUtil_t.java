 package util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class ListUtil
 {
 	public static List<int[]> deserialize(String serialized)
 	{
 		List<int[]> res = new ArrayList<int[]>();
 		if (serialized != null)
 		{
 			StringTokenizer tok = new StringTokenizer(serialized, ";");
 			while (tok.hasMoreElements())
 			{
 				String elem = (String) tok.nextElement();
 				if (elem.trim().length() > 0)
 					res.add(ArrayUtil.intFromCSVString(elem));
 			}
 		}
 		return res;
 	}
 
 	public static String serialize(List<int[]> l)
 	{
 		String s = "";
 		for (int[] i : l)
 			s += ArrayUtil.intToCSVString(i) + ";";
 		return s;
 	}
 
 	public static <T> void sort(List<T> sortable, List<T> order)
 	{
 		int matches = 0;
 		for (T t : order)
 		{
 			int index = sortable.indexOf(t);
 			if (index != -1)
 			{
 				sortable.add(matches, sortable.remove(index));
 				matches++;
 			}
 		}
 	}
 
 	public static List<?> cut(List<?> l1, List<?> l2)
 	{
 		List<Object> l = new ArrayList<Object>();
 
 		for (Object object : l1)
 			if (l2.contains(object))
 				l.add(object);
 
 		return l;
 	}
 
 	public static Double getMean(List<?> list)
 	{
 		if (list == null || list.size() == 0)
 			return null;
 
 		double mean = 0;
 		for (int i = 0; i < list.size(); i++)
 			mean = (mean * i + (Double) list.get(i)) / (double) (i + 1);
 		return mean;
 	}
 
 	public static Double getMin(List<?> list)
 	{
 		if (list == null || list.size() == 0)
 			return null;
 
 		double min = Double.MAX_VALUE;
 		for (int i = 0; i < list.size(); i++)
 			min = Math.min(min, (Double) list.get(i));
 		return min;
 	}
 
 	public static Double getMax(List<?> list)
 	{
 		if (list == null || list.size() == 0)
 			return null;
 
 		double max = -Double.MAX_VALUE;
 		for (int i = 0; i < list.size(); i++)
 			max = Math.max(max, (Double) list.get(i));
 		return max;
 	}
 
 	public static String toString(List<?> l, String seperator)
 	{
 		String s = "[ ";
 		for (Object object : l)
 			s += object + seperator;
 		if (l.size() > 0)
			s = s.substring(0, s.length() - seperator.length());
 		s += " ]";
 		return s;
 	}
 
 	public static String toString(List<?> l)
 	{
 		return toString(l, "; ");
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T, T2> List<T> cast(Class<T> type, List<T2> list)
 	{
 		List<T> l = new ArrayList<T>(list.size());
 		for (T2 e : list)
 			l.add((T) e);
 		return l;
 	}
 
 	public static <T> List<T> clone(List<T> list)
 	{
 		List<T> l = new ArrayList<T>(list.size());
 		for (T e : list)
 			l.add((T) e);
 		return l;
 	}
 
 	public static <T> List<T> concat(List<T>... lists)
 	{
 		List<T> l = new ArrayList<T>();
 		for (List<T> list : lists)
 			for (T t : list)
 				l.add(t);
 		return l;
 	}
 
 	public static void main(String[] args)
 	{
 		List<String> l = new ArrayList<String>();
 		l.add("vier");
 		l.add("eins");
 		l.add("fünf");
 		l.add("sieben");
 		l.add("neun");
 		l.add("drei");
 
 		List<String> o = new ArrayList<String>();
 		o.add("eins");
 		o.add("zwei");
 		o.add("drei");
 
 		System.out.println(l);
 		ListUtil.sort(l, o);
 		System.out.println(l);
 	}
 }
