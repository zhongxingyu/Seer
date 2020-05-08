 package de.fhh.pr2.jhass.u.b3;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashSet;
 
 public class SerializeingRoundtripMain {
 	private static Person alex;
 	private static Person maria;
 	private static Person gerd;
 	private static Person lara;
 
 	public static void main(String[] args) {
 		HashSet<Person> bestFriends = new HashSet<Person>();
 		HashSet<Person> rescuedFriends = new HashSet<Person>();
 		alex = new Person("Alex");
 		maria = new Person("Maria");
 		gerd = new Person("Gerd");
 		lara = new Person("Lara");
 		alex.setBestFriend(gerd);
 		maria.setBestFriend(gerd);
 		gerd.setBestFriend(maria);
 		lara.setBestFriend(lara);
 		bestFriends.add(alex);
 		bestFriends.add(maria);
 		bestFriends.add(gerd);
 		bestFriends.add(lara);
 		
 		try {
 			FileOutputStream personDst = new FileOutputStream("persons.bin");
 			ObjectOutputStream personSink = new ObjectOutputStream(personDst);
 			
			personSink.writeObject(bestFriends);
 			
 			personSink.close();
 			
 			FileInputStream personData = new FileInputStream("persons.bin");
 			ObjectInputStream personSource = new ObjectInputStream(personData);
			try {
				Object rescued =  personSource.readObject();
				if (rescued instanceof HashSet<?>) {
					rescuedFriends = (HashSet<Person>) rescued;
 				}
			} catch (IOException e) {}
 			personSource.close();
			for (Person rescued : rescuedFriends) {
				resetPerson(rescued);
			}
 		} catch (IOException e) {
 			System.out.println("Serialization roundtrip failed! "+e);
 		} catch (ClassNotFoundException e) {
 			System.out.println("Deserialization failed!");
 		}
 		
 		for (Person rescued : rescuedFriends) {
 			if (checkFriendship(rescued)) {
 				System.out.println(rescued.getName()+" has right friendship. ("+rescued.getBestFriend().getName()+")");
 			} else {
 				System.out.println(rescued.getName()+" has wrong friendship. ("+rescued.getBestFriend().getName()+")");
 			}
 		}
 	}
 	
 	
 	private static void resetPerson(Person person) {
 		if (person.getName().equals("Alex")) {
 			alex = person;
 		} else if (person.getName().equals("Maria")) {
 			maria = person;
 		} else if (person.getName().equals("Gerd")) {
 			gerd = person;
 		} else if (person.getName().equals("Lara")) {
 			lara = person;
 		}
 	}
 	
 	private static boolean checkFriendship(Person person) {
 		if (person == alex) {
 			return person.getBestFriend() == gerd;
 		} else if (person == maria) {
 			return person.getBestFriend() == gerd;
 		} else if (person == gerd) {
 			return person.getBestFriend() == maria;
 		} else if (person == lara) {
 			return person.getBestFriend() == lara;
 		} else {
 			return false;
 		}
 	}
 }
