 package world;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import security.SecurityCallback;
 
 import bank.security.SKonto;
 
 public class Person extends SecurityCallback implements IPerson  {
 	private String name;
 	private SPerson partner;
 	private List<SPerson> freunde = new ArrayList<SPerson>();
 	private SKonto konto;
 	
 	
 	public Person(String name) {
 		super();
 		this.name = name;
 	}
 	public String getName() {
 		return name;
 	}
 	public Person setName(String name) {
 		this.name = name;
 		return this;
 	}
 	public SPerson getPartner() {
 		return partner;
 	}
 	public Person setPartner(SPerson partner) {
 		this.partner = partner;
 		return this;
 	}
 	public SKonto getKonto() {
 		return konto;
 	}
 	public Person setKonto(SKonto konto) {
 		this.konto = konto;
 		return this;
 	}
 	
 	public Person addFreund(SPerson person){
 	  freunde.add(person);	
 	  return this;
 	}
 	@Override
 	public void leben() {
 		System.out.println(name+" lebt");
 		if(partner != null)
 			partner.doSex(this,createTan());
 		if(freunde.size()>0){
 			try{
		    	freunde.get(0).doSex(this,createTan());
 			}catch(Exception e){
 				System.out.println("Can't do sex with: "+ freunde.get(0));
 			}
 		}
 		
 	}
 	@Override
 	public String toString() {
 		return name;
 	}
 	@Override
 	public void doSex() {
 		System.out.println(name+ ": OHHH YEAH!");
 	}
 
 }
