 package com.github.bcap.dht.node;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.math.BigInteger;
 import java.net.Inet4Address;
 import java.util.Random;
 
 import org.junit.Test;
 
 public class NodeTest {
 
 	@Test
 	public void testNodeDistance() {
 		BigInteger bits1 = new BigInteger("100000000100000000000010", 2);
 		BigInteger bits2 = new BigInteger("000010000000010000000010", 2);
 		BigInteger bitsR = new BigInteger("100010000100010000000000", 2);
 		
 		Node node1 = new Node(bits1);
 		Node node2 = new Node(bits2);
 		
 		BigInteger distance = node1.getDistance(node2);
 		BigInteger reverseDistance = node2.getDistance(node1);
 		
 		assertEquals(bitsR, distance);
 		assertEquals(distance, reverseDistance);
 	}
 	
 	@Test
 	public void testBucketCreation() {
 		Node node = new Node(BigInteger.ONE);
 		for(int i = 0; i < Identifier.LENGTH; i++) {
 			Bucket bucket = node.getBucket(i);
 			assertNotNull(bucket);
 			assertEquals(BigInteger.ONE.shiftLeft(i), bucket.getValue());
 		}
 		
 		try {
 			node.getBucket(Identifier.LENGTH);
 			fail("Exception should be thrown");
 		} catch(ArrayIndexOutOfBoundsException e) {
 			
 		}
 	}
 	
 	@Test
 	public void testBucketLocation() {
 		Node baseNode = new Node(BigInteger.ZERO);
 		
 		BigInteger[] nodes = new BigInteger[] {
 			new BigInteger("00000000000000", 2),
 			new BigInteger("00000000000001", 2),
 			new BigInteger("00000000000010", 2),
 			new BigInteger("10000000000000", 2),
 			new BigInteger("10101010101010", 2),
 			new BigInteger("00000010010011", 2),
 			new BigInteger("01010110010011", 2),
 		};
 		BigInteger[] buckets = new BigInteger[] {
 			new BigInteger("00000000000001", 2),
 			new BigInteger("00000000000001", 2),
 			new BigInteger("00000000000010", 2),
 			new BigInteger("10000000000000", 2),
 			new BigInteger("10000000000000", 2),
 			new BigInteger("00000010000000", 2),
 			new BigInteger("01000000000000", 2),
 		};
 		
 		for (int i = 0; i < nodes.length; i++) {
 			Node node = new Node(nodes[i]);
 			Bucket bucket = baseNode.getBucketForId(node);
 			assertNotNull(bucket);
 			assertEquals(buckets[i], bucket.getValue());
 		}
 	}
 	
 	@Test
 	public void testNodeSerialization() throws Exception {
 		Identifier key = new Identifier(BigInteger.ONE);
 		byte[] data = "polaco".getBytes();
 		int bucketIndex = 3;
 		Contact contact = new Contact(BigInteger.ONE, Inet4Address.getByName("127.0.0.1"), 5000);
 
 		Node node = new Node(new BigInteger(Identifier.LENGTH, new Random()));
 		node.getDataStorage().put(key, data);
		node.getBucket(bucketIndex).updateContact(contact);
 		
 		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
 		
 		ObjectOutputStream out = new ObjectOutputStream(byteOut);
 		out.writeObject(node);
 		out.close();
 		
 		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
 		Object readedObj = in.readObject();
 		in.close();
 		
 		Node readedNode = (Node) readedObj;
 		assertEquals(node, readedNode);
 		assertArrayEquals(data, readedNode.getDataStorage().get(key));
 		assertEquals(contact, readedNode.getBucket(bucketIndex).iterator().next());
 	}
 }
