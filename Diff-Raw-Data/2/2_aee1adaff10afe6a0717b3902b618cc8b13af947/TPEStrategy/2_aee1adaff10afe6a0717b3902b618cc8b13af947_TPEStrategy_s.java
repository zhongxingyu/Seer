 package model.strategies;
 
 import java.util.HashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import simulator.Utils;
 import model.component.Component;
 import model.component.ComponentIO;
 import model.mediator.Mediator;
 
 public class TPEStrategy implements IStrategy {
 
 	private static Logger log = LoggerFactory.getLogger(TPEStrategy.class);
 	
 	private ComponentIO tpe;
 	
 	public TPEStrategy(ComponentIO _tpe) {
 		tpe = _tpe;
 	}
 	
 	private boolean cmdOK(HashMap<String, String> d){
 		if (d.get("content-type") != null){
 			
 			return d.get("content-type").equalsIgnoreCase("iso7816");
 		}
 		return false;
 	}
 
 	@Override
 	public void inputTreatment(Mediator c, String data) {
 		HashMap<String, String> d = Utils.string2Hashmap(data);
 		
 		if (!cmdOK(d)){
 			log.warn(tpe.getName() + " impossible de gerer la donnee");
 			return;
 		}
 		//tpe.output(m, "content-type:iso7816;type:rq;msg:initco;protocols:B0',CB2A;ciphersetting:none,RSA2048")
 		
 		switch(d.get("msg")){
 			case "initco":
				c.send(tpe,"content-type:iso7816;type:rq;msg:initco;protocols:B0',CB2A;ciphersetting:none,RSA2048");
 				break;
 			case "pin":
 				
 				break;
 			case "arpc":
 				
 				break;		
 		}
 	}
 
 	@Override
 	public void outputTreatment(Mediator c, String data) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
