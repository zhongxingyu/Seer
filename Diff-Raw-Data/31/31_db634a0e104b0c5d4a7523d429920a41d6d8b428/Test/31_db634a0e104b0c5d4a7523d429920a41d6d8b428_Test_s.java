 package test;
 import resources.test.TestHelper;
 import ru.sabstest.ClientList;
 import ru.sabstest.Init;
 import ru.sabstest.PacketList;
 import ru.sabstest.PaymentDocumentList;
 import ru.sabstest.Settings;
 
 
 
 
 public class Test extends TestHelper
 {
 	public void testMain(Object[] args) 
 	{
 		Settings.testProj = (String) args[0];
 		
 		
 		Init.load();
 		Settings.readXML(Settings.testProj + "settings\\general.xml");
 		
 //		PaymentDocumentList pl = new PaymentDocumentList();
 //		pl.readFile("C:\\test\\nach\\5\\K7182000.101");
 //		
 ////		callScript("PrProvod.PervVvod.Vvod", new Object[]{ pl});
 //		
 //		callScript("PrProvod.PervVvod.ContrVvod", new Object[]{ pl});
 		
 		//Settings.readXML("C:\\general.xml");
 		ClientList.readFile("C:\\test\\nach\\clients.xml");
 		Settings.Sign.readXML(Settings.testProj + "settings\\sign.xml");
 		
 		PacketList pl = new PacketList();
		pl.generateFromXML("C:\\test\\otv\\gen3.xml");
 		//pl.createFile("C:\\test\\");
 	//	pl.insertIntoDB();
 						
		callScript("SABS.CreateSignedXML",new Object[]{"C:\\test\\otv\\3\\", pl});
 		
 		
 	}
 }
 
