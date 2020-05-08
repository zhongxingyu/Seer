 package org.nlogo.extensions.mapred;
 
 import org.apache.log4j.Logger;
 
 import org.nlogo.api.DefaultReporter;
 import org.nlogo.api.ExtensionException;
 import org.nlogo.api.Syntax;
 import org.nlogo.api.Argument;
 import org.nlogo.api.Context;
 import org.nlogo.api.LogoListBuilder;
 
 public class Result extends DefaultReporter
 {
 	Logger logger = Logger.getLogger(Result.class);
 	
 	public Syntax getSyntax()
 	{
		return Syntax.reporterSyntax(new int[] {}, Syntax.ListType());
 	}
 	
 	public Object report(Argument args[], Context context) throws ExtensionException
 	{
     LogoListBuilder list = new LogoListBuilder();
     LogoListBuilder hh;
     Object keys[];
     int i;
     
     keys= MapRedProto.rmap.keySet().toArray();
 		for(i= 0; i < keys.length; i++)
 		{
 			hh= new LogoListBuilder();
 			hh.add(keys[i]);
 			hh.add(MapRedProto.rmap.get(keys[i]));
 			list.add(hh.toLogoList());
     }
     return list.toLogoList();
 	}
 }
 
