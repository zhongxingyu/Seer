 package com.jdevelopstation.l2ce.test;
 
 import java.io.File;
 
 import com.jdevelopstation.commons.logging.Log4JHelper;
 import com.jdevelopstation.l2ce.data.xml.holder.ClientVersionHolder;
 import com.jdevelopstation.l2ce.data.xml.parser.ClientVersionParser;
 import com.jdevelopstation.l2ce.version.ClientVersion;
 import com.jdevelopstation.l2ce.version.node.data.ClientData;
 import com.jdevelopstation.l2ce.version.node.file.ClientFile;
 
 /**
  * @author VISTALL
  * @date 1:24/26.05.2011
  */
 public class MainTest
 {
 	public static void main(String... arg)
 	{
 		Log4JHelper.load();
 		ClientVersionParser.getInstance().load();
 
 		ClientVersion version = ClientVersionHolder.getInstance().getVersion("CT3_Awakening");
 		ClientFile f = null;
 		for(ClientFile f2 : version.getClientFiles())
 			if(f2.getPattern().matcher("msconditiondata.dat").find())
 				f = f2;
 		if(f == null)
 			return;
 		ClientData data = f.parse(new File("D:\\MyTests\\l2encdec\\dec-msconditiondata.dat"));
 		data.toXML("C:/msconditiondata.xml");
 	}
 }
