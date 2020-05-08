 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 abstract class OrbManagerSkel extends OrbManager implements ObjectImpl {
 
 	public OrbManagerSkel() {
 		
 	    Address addr = ORB.instance().address();
 	    ObjectReference ior = new ObjectReference ("IDL:Account:1.0", addr);
 	    objectReference(ior);
 
 	    ORB.instance().registerObjectImpl(ior.stringify(),this);
 	}
 	
 	public void invoke(ServerRequest req) {
 		boolean a = dispatch(req);
 		assert (a):"dispatch Error";
 	}
 	
 	protected boolean dispatch(ServerRequest req) {
 		
 		/**
 		 * TODO: implementar as operacoes
 		 */
 		if (req.opname().equals("migrate")) {
 			echo("migrate received");
 			Map hash_obj = new HashMap();
 			String string_xml = req.getStringStriped();
 			System.out.println("string striped\n"+string_xml);
 			XmlMapper xml_mapper = req.getXmlMapper(string_xml);
 			
 			boolean result = migrate(xml_mapper);
 			req.putBooleanReply(result);
 			
 			return result;		
 
 		}
 		
 		return false;
 	}
 	
 }
