 package org.common.conversion;
 
 
 import java.io.File;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 
 public class XMLConversion {
 
 	
 	public static void marshall(Object object, String path){
 		  try {
 			  	String fileName = object.getClass().getClassLoader().getResource(path).getPath();
 				File file = new File(fileName);
 				JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
 				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 
 				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 		 
 				jaxbMarshaller.marshal(object, file);
				//jaxbMarshaller.marshal(object, System.out);
 		 
 	      } catch (JAXBException e) {
 				e.printStackTrace();
 	      }
 	}
 
     @SuppressWarnings("unchecked")
 	public static < E > E unMarshall(Class< E > typeClass, String path){
         try {
         	String fileName = typeClass.getClassLoader().getResource(path).getPath();
             File file = new File(fileName);
             JAXBContext jaxbContext = JAXBContext.newInstance(typeClass);
 
             Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
             return (E)jaxbUnmarshaller.unmarshal(file);
         } catch (JAXBException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
 
 //	public static void main(String[] args) throws Exception {
 //		Account acc = new Account();
 //		acc.setAmount(100.00);
 //		acc.setCardNumber("1000000000000");
 //		acc.setPassword("pass");
 //		acc.setStatus(true);
 //		marshall(acc, "D:/JavaDev/nxc_bank/workspace/nxc_bank/common/src/main/java/org/common/accounts/accounts.xml");
 //	}
 //
 //	public static void main(String[] args) throws Exception {
 //        Atms atms = unMarshall(Atms.class, "target/classes" + Config.atmsFile);
 //        System.out.println(atms.getAtm(5).getBills());
 //    }
 //	public static void main(String[] args) throws Exception {
 //        Atms atms = new Atms();
 //        marshall(atms, "target/classes" + Config.atmsFile);
 //    }
 //	public static void main(String[] args) throws Exception {
 ////            for (int i = 0, l = 5; i < l; i++) {
 //				Account acc = new Account();
 //				acc.setAmount(100.00);
 //				acc.setCardNumber("1000000000000"+1);
 //				acc.setPassword("pass"+5);
 //				acc.setStatus(true);
 //                marshall(acc, "common/src/main/java/org/common/accounts/accounts.xml");
 ////			}
 //    }
 //    public static void main(String[] args) throws Exception {
 //        Accounts accs = unMarshall(Accounts.class, "common/src/main/java/org/common/accounts/accounts.xml");
 //        System.out.println(accs.toString());
 //    }
 }
