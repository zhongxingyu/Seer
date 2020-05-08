 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 public class Arguments {
 	
 	List<String> list;
 	
 	public Arguments(String args) {
 		list = args != null ? Arrays.asList(args.split(";")) : new ArrayList<String>();
 	}
 
 	public Arguments() {
 		list = new ArrayList<String>();
 	}
 
 	public String getArg(int index) {
 		if ((index < 0) || (index >= list.size())) return "";
 		return list.get(index).trim();
 	}
 	
	public Integer getArgInt(int index) {
		try {
			return Integer.parseInt(getArg(index));
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
 	public List<String> getArgs(int index) {
 		if ((index < 0) || (index >= list.size())) return new ArrayList<String>();
 		List<String> args = new ArrayList<String>();
 		for (int i=index; i<list.size(); i++) {
 			args.add(list.get(i).trim());
 		}
 		return args;
 	}
 	
 	public int size() {
 		return list.size();
 	}
 	
 	public String toString() {
 		StringBuffer s = new StringBuffer();
 		for (Iterator<String> i = list.iterator(); i.hasNext(); ) {
 			s.append(i.next().trim()+(i.hasNext() ? ";" : ""));
 		}
 		return s.toString();
 	}
 }
