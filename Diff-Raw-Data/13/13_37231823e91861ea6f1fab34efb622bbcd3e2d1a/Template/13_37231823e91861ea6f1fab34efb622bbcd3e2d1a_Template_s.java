 package examples;
 
 import types.Executer;
 import types.FlowData;
 
 public class Template implements Executer {
 
 	private String wild;
 
 	@Override
 	public String getLabel() {
 		return "Template";
 	}
 
 	@Override
 	public Class[] getInputTypes() {
 		return new Class[]{String.class};
 	}
 
 	@Override
 	public FlowData execute(FlowData data) {
 		String str = (String)data.getData();
		String newStr = str.replaceAll(getWildCard(),str);
 		newStr = new String(newStr);
 		return new FlowData(newStr);
 	}
 
 	private String getWildCard() {
 		return wild;
 	}
 	
 	public void setWildCard(String wild){
 		this.wild = wild;
 	}
 
 	@Override
 	public Class getOutputType() {
 		return String.class;
 	}
 
 
 
 }
