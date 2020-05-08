 import java.io.*;
 public class test{
 	public static void main(String[] args)throws IOException{
		StringHolder comms = new StringHolder();
		comms.add("import java.io.*;");
		comms.add("}public static String fisch(){return \"fisch\";};");
		comms.add("fisch();");
		RunCode run = new RunCode(comms.toFile());
		System.out.println(comms.toFile());
 		run.comprun();
 	}
 }
