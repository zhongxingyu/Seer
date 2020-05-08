 package org.common.atms;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.common.configs.Config;
 import org.common.conversion.XMLConversion;
 import org.hamcrest.Matchers;
 
 import static ch.lambdaj.Lambda.*;
 
 
 @XmlRootElement
 public class Atms {
 	@XmlElement(name = "atm")
 	private ArrayList<Atm> data = new ArrayList<Atm>();
 	private boolean blocked = false;
 	private static String path = Config.atmsFile;
 	
 	public ArrayList<Atm> init(){
 		Atms atms = XMLConversion.unMarshall(Atms.class, path);
         int i = 1;
 		try{
 			while(atms.getAtm(i) != null){
 				data.add(atms.getAtm(i));
 				i++;
 			}
 		}
 		catch(ArrayIndexOutOfBoundsException e){
 			e.getMessage();			
 		}
 		return data;
 	}
 	
 	public void store(){
 		Atms atms = new Atms();
 		atms.data = data;
 		XMLConversion.marshall(atms, path);
 	}
 	
 	public Atms getAtms() {
 		Atms atms = XMLConversion.unMarshall(Atms.class, path);
 		return atms;
 	}
 	
 	public void clear() {
 		data.clear();
 	}
 	
 	public Atm getAtm(String id) {
 		List<Atm> accounts = filter(having(on(Atm.class).getId(), Matchers.equalTo(id)), data);
 		Object[] values = accounts.toArray();
 		return (values.length > 0) ? (Atm) values[0] : null;
 	}
 	
 	public void addAtm(Atm atm) {
 		data.add(atm);
         Atms atms = new Atms();
         atms.data = data;
 		XMLConversion.marshall(atms, path);
 	}
 
 	public boolean isBlocked() {
 		return blocked;
 	}
 
 	public void setBlocked(boolean blocked) {
 		this.blocked = blocked;
 	}
 
 	public int count() {
 		return data.size();
 	}
 
 	public Atm getAtm(int index) {
 		Object[] values = data.toArray();
 		if (values.length > 0 && ( (index-1) <= values.length && (index-1) >= 0) ) {
 			return (Atm) values[index-1];
 		}
 		return null;
 	}
 
 	public void removeAtm(String id) {
 		List<Atm> atmList = filter(having(on(Atm.class).getId(), Matchers.equalTo(id)), data);
 		if (!atmList.isEmpty()) {
 			Atm atm = atmList.get(0);
 			data.remove(atm);
 		}
         Atms atms = new Atms();
         atms.data = data;
 		XMLConversion.marshall(atms, path);
 	}
 
 	public boolean isExisting(String id) {
 		List<Atm> atmList = filter(having(on(Atm.class).getId(), Matchers.equalTo(id)), data);
 		if (atmList.isEmpty()){
 			return false;
 		}
 		return true;
 	}
 
 	public  ArrayList<Atm> getAll () {
 		return data;
 	}
 
 }
