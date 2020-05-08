 package com.fuyou.thrift.jnb;
 
 import org.apache.thrift.TDeserializer;
 import org.apache.thrift.TException;
 import org.apache.thrift.TSerializer;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.protocol.TCompactProtocol;
 import org.apache.thrift.protocol.TProtocolFactory;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: yubaofu
  * Date: 12-7-31
  * Time: 5:44
  * To change this template use File | Settings | File Templates.
  */
 public class ThriftSerializer {
     public static void main(String[] args) throws TException, IOException {
         Person person = new Person();
         person.setEmail("fuyou001@gmail.com");
         person.setFirstName("yubao");
         person.setLastName("fu");
         person.setId(100000);
         TProtocolFactory tProtocolFactory = new TBinaryProtocol.Factory();
         TProtocolFactory tCompactFactory = new TCompactProtocol.Factory();
         byte[] bytes = serialize(tProtocolFactory, person);
 
         System.out.println("TBinaryProtocol size:" + bytes.length);
         Person person1 = deserialize(tProtocolFactory, bytes);
         System.out.println(person.toString());
         System.out.println(person1.toString());
         System.out.println("person equals person1:" + person.equals(person1));
 
         byte[] tCompactBytes = serialize(tCompactFactory, person);
         Person tcompactPerson = deserialize(tCompactFactory, tCompactBytes);
         System.out.println("TCompactProtocol size :" + tCompactBytes.length);
         System.out.println(person.toString());
         System.out.println(tcompactPerson.toString());
         System.out.println("person equals person1:" + person.equals(tcompactPerson));
 
         List<Person> list = new ArrayList<Person>();
         int len = 10;
         for (int i = 0; i < len; i++) {
             Person temp = new Person();
            person.setEmail("fuyou00" + i + "@gmail.com");
            person.setId(i);
            person.setFirstName("fu" + i);
            person.setLastName("yubao" + i);
            list.add(person);
         }
 
         byte[] bytes1 = serialize(tProtocolFactory, list);
         List<Person> persons = deserialize(tProtocolFactory, bytes1, len);
         for (int i = 0; i < len; i++) {
            Person p1 = list.get(0);
            Person p2 = persons.get(0);
             System.out.println("p1:" + p1);
             System.out.println("p2:" + p2);
             System.out.println(p1.equals(p2));
         }
 
 
     }
 
     public static byte[] serialize(TProtocolFactory tProtocolFactory, Person person) throws TException {
         TSerializer tSerializer = new TSerializer(tProtocolFactory);
         byte[] bytes = tSerializer.serialize(person);
         return bytes;
     }
 
     public static byte[] serialize(TProtocolFactory tProtocolFactory, List<Person> persons) throws TException, IOException {
         TSerializer tSerializer = new TSerializer(tProtocolFactory);
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(bytes);
         for (Person person : persons) {
             byte[] data = tSerializer.serialize(person);
             out.writeInt(data.length);
             out.write(data);
         }
         out.flush();
         return bytes.toByteArray();
     }
 
     public static Person deserialize(TProtocolFactory tProtocolFactory, byte[] bytes) throws TException {
         TDeserializer deser = new TDeserializer(tProtocolFactory);
         Person person = new Person();
         deser.deserialize(person, bytes);
         return person;
     }
 
     public static List<Person> deserialize(TProtocolFactory tProtocolFactory, byte[] bytes, int len) throws TException, IOException {
         TDeserializer deser = new TDeserializer(tProtocolFactory);
         InputStream inputStream = new ByteArrayInputStream(bytes);
         DataInputStream inData = new DataInputStream(inputStream);
         List<Person> list = new ArrayList<Person>();
         for (int i = 0; i < len; i++) {
             byte[] data = new byte[inData.readInt()];
             inData.readFully(data);
             Person person = new Person();
             deser.deserialize(person, data);
             list.add(person);
         }
         return list;
     }
 }
