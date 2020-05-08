 package ch.compass.gonzoproxy.relay.modifier;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import ch.compass.gonzoproxy.model.Field;
 import ch.compass.gonzoproxy.model.Packet;
 import ch.compass.gonzoproxy.utils.PacketUtils;
 
 public class PacketModifier {
 
 
 	private static final String REGEX_FILE = "resources/regex_rules.dat";
 	private static final String MODIFIER_FILE = "resources/modifier_rules.dat";
 	
 	private ArrayList<PacketRule> packetRules = new ArrayList<PacketRule>();
 	private HashMap<String, Boolean> packetRegex = new HashMap<String, Boolean>();
 	
 	public PacketModifier() {
 		
 		// freaks out with tests cause used same command and rules, have to fix it first ;)
 		loadModifiers();
 //		loadRegex();
 	}
 
 	public Packet modifyByRule(Packet originalPacket) {
 		for (PacketRule modifier : packetRules) {
 			if (ruleSetMatches(modifier, originalPacket)) {
 				return applyRules(modifier, originalPacket);
 			}
 		}
 	
 		return originalPacket;
 	}
 
 	public Packet modifyByRegex(Packet packet) {
 		Packet modifiedPacket = packet.clone();
 		//TODO: naming / regex
 		for (String regex : packetRegex.keySet()) {
 			if(packetRegex.get(regex)){
 				String originalPacketData = new String(modifiedPacket.getOriginalPacketData());
 				originalPacketData = originalPacketData.replaceAll(regex, "");
 				modifiedPacket.setOriginalPacketData(originalPacketData.getBytes());
 				modifiedPacket.setModified(true);
 			}
 		}
 		return modifiedPacket;
 	}
 
 	public void addRule(String packetName, FieldRule fieldRule, Boolean updateLength) {
 		PacketRule existingRuleSet = findRuleSet(packetName);
 		if (existingRuleSet != null) {
 			existingRuleSet.add(fieldRule);
 			existingRuleSet.shouldUpdateLength(updateLength);
 		} else {
 			PacketRule createdRuleSet = new PacketRule(packetName);
 			createdRuleSet.add(fieldRule);
 			packetRules.add(createdRuleSet);
 			createdRuleSet.shouldUpdateLength(updateLength);
 		}
 		saveModifiers();
 	}
 	
 	public void addRegex(String regex, Boolean isActive){
 		packetRegex.put(regex, isActive);
 		saveRegex();
 	}
 
 	public ArrayList<PacketRule> getRuleSets() {
 		return packetRules;
 	}
 
 	private PacketRule findRuleSet(String packetName) {
 		for (PacketRule existingModifier : packetRules) {
 			if (existingModifier.getCorrespondingPacket().equals(packetName))
 				return existingModifier;
 		}
 		return null;
 	}
 
 	private Packet applyRules(PacketRule modifier, Packet originalPacket) {
 
 		Packet modifiedPacket = originalPacket.clone();
 
 		for (Field field : modifiedPacket.getFields()) {
 			FieldRule rule = modifier.findMatchingRule(field);
 
 			if (rule != null && rule.isActive()) {
 				int fieldLengthDiff;
 
 				if (rule.getOriginalValue().isEmpty()) {
 					fieldLengthDiff= computeLengthDifference(field.getValue(),
 							rule.getReplacedValue());
 					
 					updatePacketLenght(modifiedPacket, fieldLengthDiff);
 
 					if (shouldUpdateContentLength(modifier, field)) {
 						updateContentLengthField(modifiedPacket,
 								fieldLengthDiff);
 					}
 					field.setValue(rule.getReplacedValue());
 
 				} else {
 					fieldLengthDiff = computeLengthDifference(rule.getOriginalValue(),
 							rule.getReplacedValue());
 					
 					updatePacketLenght(modifiedPacket, fieldLengthDiff);
 					
					if (modifier.shouldUpdateContentLength()) {
 						updateContentLengthField(modifiedPacket,
 								fieldLengthDiff);
 					}
 					field.replaceValue(rule.getOriginalValue(),
 							rule.getReplacedValue());
 				}
 				modifiedPacket.setModified(true);
 			}
 		}
 		return modifiedPacket;
 	}
 
 	private boolean shouldUpdateContentLength(PacketRule modifier, Field field) {
 		return modifier.shouldUpdateContentLength() && field.getName().toUpperCase().contains(PacketUtils.CONTENT_DATA);
 	}
 
 	private Field findContentLengthField(Packet packet) {
 		for (Field field : packet.getFields()) {
 			if (field.getName().equals(PacketUtils.CONTENT_LENGTH_FIELD))
 				return field;
 		}
 		return new Field();
 	}
 
 	private void updatePacketLenght(Packet modifiedPacket, int fieldLengthDiff) {
 		int updatedPacketSize = modifiedPacket.getSize() + fieldLengthDiff;
 		modifiedPacket.setSize(updatedPacketSize);
 	}
 
 	private void updateContentLengthField(Packet packet, int fieldLengthDiff) {
 
 		Field contentLengthField = findContentLengthField(packet);
 		int currentContentLength = Integer.parseInt(
 				contentLengthField.getValue(), 16);
 		int newContentLength = currentContentLength + fieldLengthDiff;
 		contentLengthField.setValue(toHexString(newContentLength));
 
 	}
 
 	private int computeLengthDifference(String originalValue,
 			String replacedValue) {
 		int diff = (replacedValue.length() - originalValue.length())
 				/ (PacketUtils.ENCODING_OFFSET + PacketUtils.WHITESPACE_OFFSET);
 		return diff;
 	}
 
 	private boolean ruleSetMatches(PacketRule existingRuleSet,
 			Packet originalPacket) {
 		return existingRuleSet.getCorrespondingPacket().equals(
 				originalPacket.getDescription());
 	}
 
 	private String toHexString(int newContentLength) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(Integer.toHexString(newContentLength));
 		if (sb.length() < 2) {
 			sb.insert(0, '0');
 		}
 		return sb.toString();
 	}
 	
 	
 	private void saveModifiers(){
         try {
         	FileOutputStream fout = new FileOutputStream(MODIFIER_FILE);
         	ObjectOutputStream oos = new ObjectOutputStream(fout);
 			oos.writeObject(packetRules);
 			oos.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         
 	}
 	
 	private void saveRegex(){
 		 try {
 	        	FileOutputStream fout = new FileOutputStream(REGEX_FILE);
 	        	ObjectOutputStream oos = new ObjectOutputStream(fout);
 				oos.writeObject(packetRegex);
 				oos.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadModifiers(){
 		try {
 			File modifierFile = new File(MODIFIER_FILE);
 			FileInputStream fin = new FileInputStream(modifierFile);
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			packetRules = (ArrayList<PacketRule>) ois.readObject();
 			ois.close();
 		} catch (IOException | ClassNotFoundException e) {
 			packetRules = new ArrayList<PacketRule>();
 			System.out.println("Modifier File not found !");
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void loadRegex() {
 		try {
 			File regexFile = new File(REGEX_FILE);
 			FileInputStream fin = new FileInputStream(regexFile);
 			ObjectInputStream ois = new ObjectInputStream(fin);
 			packetRegex = (HashMap<String, Boolean>) ois.readObject();
 			ois.close();
 		} catch (IOException | ClassNotFoundException e) {
 			packetRegex = new HashMap<String, Boolean>();
 			System.out.println("Regex File not found !");
 		}
 	}
 
 }
