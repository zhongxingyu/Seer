 package com.LRFLEW.bukkit.skygrid;
 
 import java.util.Random;
 import java.util.TreeMap;
 
 public class BlockProbobility {
 	
 	TreeMap<Integer, Byte> p = new TreeMap<Integer, Byte>();
 	int total = 0;
 	
 	public void addBlock(int id, int prob) {
 		addBlock((byte) id, prob);
 	}
 	
 	public void addBlock(byte id, int prob) {
 		p.put(total, id);
 		total += prob;
 	}
 	
 	public byte getBlock(Random random, boolean bottom, boolean b) {
 		byte temp = p.floorEntry(random.nextInt(total)).getValue();
		if (bottom && temp == 81) {
			return getBlock(random, bottom, b);
		} else if (b && (temp == 9 || temp == 11)) {
			return getBlock(random, bottom, b);
 		}
 		return temp;
 	}
 	
 }
