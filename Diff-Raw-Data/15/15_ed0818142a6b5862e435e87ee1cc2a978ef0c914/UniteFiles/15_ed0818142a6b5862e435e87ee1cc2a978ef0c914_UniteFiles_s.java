 package examples;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import types.Executer;
 import types.FlowData;
 import types.MultipleExecuter;
 
 public class UniteFiles implements MultipleExecuter {
 
 	@Override
 	public String getLabel() {
 		return "Unite files";
 	}
 
 	@Override
 	public Class[] getInputTypes() {
 		return new Class[]{String.class, String.class};
 	}
 
 	@Override
 	public FlowData execute(FlowData[] data) {
 		String uniteFiles = GeneralUtils.convertFileToString(data[0].toString()) + 
 							GeneralUtils.convertFileToString(data[1].toString());
 		return new FlowData(uniteFiles);
 	}
 }
