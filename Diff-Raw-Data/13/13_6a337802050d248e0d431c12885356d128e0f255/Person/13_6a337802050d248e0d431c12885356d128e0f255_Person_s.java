 /*
  * Copyright (c) 2011 Ayan Dave http://www.linkedin.com/in/daveayan
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
  * associated documentation files (the "Software"), to deal in the Software without restriction, including 
  * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
  * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the 
  * following conditions:
  * 
  * 1) The above copyright notice and this permission notice shall be included without any changes or alterations 
  * in all copies or substantial portions of the Software.
  * 2) The copyright notice part of the org.json package and its classes shall be honored.
  * 3) This software shall be used for Good, not Evil.
  * portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
  * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
  * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
  * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package rjson.domain;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.Vector;
 
 public class Person extends BaseDomainObject {
 	public String name;
 	private Map<String, Address> addresses = new HashMap<String, Address>();
 	protected List<PhoneNumber> phones = new ArrayList<PhoneNumber>();
	private Vector<Account> accounts = new Vector<Account>();
	private Vector<Drivable> drives = new Vector<Drivable>();
 
 	public static Person getPartialInstance1() {
 		PhoneNumber ph1 = new PhoneNumber("443", "3233323");
 		PhoneNumber ph2 = new PhoneNumber("657", "9466354");
 
 		Person p1 = new Person();
 		p1.setName("QWERTY");
 		p1.addAccount(null);
 		p1.addPhone(ph1);
 		p1.addPhone(ph2);
 		p1.setAddresses(null);
 		
 		return p1;
 	}
 
 	public static Person getFullyLoadedInstance() {
 		Account a1 = new Account(123, 100);
 		Address add1 = new Address("tuttles creek", "OH", "Dublin", "43016");
 		Address add2 = new Address("sawmill village dr", "OH", "Columbus", "43235");
 		PhoneNumber ph1 = new PhoneNumber("443", "3233323");
 		PhoneNumber ph2 = new PhoneNumber("657", "9466354");
 
 		Person p1 = new Person();
 		p1.setName("QWERTY");
 		p1.addAccount(a1);
 		p1.addPhone(ph1);
 		p1.addPhone(ph2);
 		p1.addAddress("Home", add1);
 		p1.addAddress("Office", add2);
 		
 		p1.addDrives(new Car());
 		p1.addDrives(new Scooter());
 		
 		return p1;
 	}
 	
 	public static Person getFullyLoadedInstanceWithNullAddress() {
 		Person p = getFullyLoadedInstance();
 		p.setAddresses(null);
 		return p;
 	}
 	
 	private void addDrives(Drivable vehicle) {
 		this.drives.add(vehicle);
 	}
 	
 	private void addAddress(String string, rjson.domain.Address add1) {
 		addresses.put(string, add1);
 	}
 
 	private void addPhone(rjson.domain.PhoneNumber ph1) {
 		phones.add(ph1);
 	}
 
 	private void addAccount(rjson.domain.Account a1) {
 		accounts.add(a1);
 	}
 
 	private void setName(String string) {
 		this.name = string;
 	}
 
 	public String toString() {
 		return name + "\n" + addresses + "\n" + phones + "\n" + accounts + "\n" + drives;
 	}
 	
 	public void setAddresses(Map<String, Address> addresses) {
 		this.addresses = addresses;
 	}
 }
