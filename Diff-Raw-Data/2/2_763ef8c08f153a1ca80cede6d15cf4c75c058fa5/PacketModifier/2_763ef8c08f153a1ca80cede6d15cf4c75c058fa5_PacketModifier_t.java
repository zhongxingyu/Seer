 package ch.compass.gonzoproxy.relay.modifier;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import ch.compass.gonzoproxy.model.Field;
 import ch.compass.gonzoproxy.model.Packet;
 import ch.compass.gonzoproxy.utils.PacketUtils;
 import ch.compass.gonzoproxy.utils.PersistingUtils;
 
 public class PacketModifier {
 
 	private static final String REGEX_FILE = "resources/regex_rules.dat";
 	private static final String MODIFIER_FILE = "resources/modifier_rules.dat";
 
 	private ArrayList<PacketRule> packetRules = new ArrayList<PacketRule>();
 	private ArrayList<PacketRegex> packetsRegex = new ArrayList<PacketRegex>();
 
 	public PacketModifier() {
 
 		// freaks out with tests cause used same command and rules, have to fix
 		// it first ;)
 		loadModifiers();
 		loadRegex();
 	}
 
 	public void modifyByRule(Packet originalPacket) {
 		for (PacketRule modifier : packetRules) {
 			if (ruleSetMatches(modifier, originalPacket)) {
 				applyRules(modifier, originalPacket);
 			}
 		}
 	}
 
 	public void modifyByRegex(Packet packet) {
 		for (PacketRegex regex : packetsRegex) {
 			if (regex.isActive()) {
 				
 				String originalPacketData = new String(
 						packet.getOriginalPacketData());
 				String modifiedPacketData = originalPacketData.replaceAll(
 						regex.getRegex(), regex.getReplaceWith());
 				if(!originalPacketData.equals(modifiedPacketData)){
					packet.setOriginalPacketData(modifiedPacketData
 							.getBytes());
 					packet.setModified(true);
 				}
 			}
 		}
 	}
 
 	public void addRule(String packetName, FieldRule fieldRule,
 			Boolean updateLength) {
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
 	}
 
 	public void addRegex(PacketRegex regex, Boolean isActive) {
 		regex.setActive(isActive);
 		packetsRegex.add(regex);
 	}
 
 	public ArrayList<PacketRule> getPacketRule() {
 		return packetRules;
 	}
 	
 	public ArrayList<PacketRegex> getPacketRegex() {
 		return packetsRegex;
 	}
 
 	private PacketRule findRuleSet(String packetName) {
 		for (PacketRule existingModifier : packetRules) {
 			if (existingModifier.getCorrespondingPacket().equals(packetName))
 				return existingModifier;
 		}
 		return null;
 	}
 
 	private void applyRules(PacketRule modifier, Packet packet) {
 
 
 		for (Field field : packet.getFields()) {
 			FieldRule rule = modifier.findMatchingRule(field);
 
 			if (rule != null && rule.isActive()) {
 				int fieldLengthDiff;
 
 				if (rule.getOriginalValue().isEmpty()) {
 					fieldLengthDiff = computeLengthDifference(field.getValue(),
 							rule.getReplacedValue());
 
 					updatePacketLenght(packet, fieldLengthDiff);
 
 					if (shouldUpdateContentLength(modifier, field)) {
 						updateContentLengthField(packet,
 								fieldLengthDiff);
 					}
 					field.setValue(rule.getReplacedValue());
 
 				} else {
 					fieldLengthDiff = computeLengthDifference(
 							rule.getOriginalValue(), rule.getReplacedValue());
 
 					updatePacketLenght(packet, fieldLengthDiff);
 
 					if (shouldUpdateContentLength(modifier, field)) {
 						updateContentLengthField(packet,
 								fieldLengthDiff);
 					}
 					field.replaceValue(rule.getOriginalValue(),
 							rule.getReplacedValue());
 				}
 				packet.setModified(true);
 			}
 		}
 	}
 
 	private boolean shouldUpdateContentLength(PacketRule modifier, Field field) {
 		return modifier.shouldUpdateContentLength()
 				&& field.getName().toUpperCase()
 						.contains(PacketUtils.CONTENT_DATA);
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
 
 	@SuppressWarnings("unchecked")
 	private void loadModifiers() {
 		File modifierFile = new File(MODIFIER_FILE);
 		try {
 			packetRules = (ArrayList<PacketRule>) PersistingUtils
 					.loadFile(modifierFile);
 
 		} catch (ClassNotFoundException | IOException e) {
 			// TODO: LOG FILE LOAD PROB
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadRegex() {
 		File regexFile = new File(REGEX_FILE);
 		try {
 			packetsRegex = (ArrayList<PacketRegex>) PersistingUtils
 					.loadFile(regexFile);
 		} catch (ClassNotFoundException | IOException e) {
 			// TODO: LOG FILE LOAD PROB
 		}
 	}
 
 	public void persistRegex() throws IOException {
 		File regexFile = new File(REGEX_FILE);
 		PersistingUtils.saveFile(regexFile, packetsRegex);
 	}
 
 	public void persistRules() throws IOException {
 		File modifierFile = new File(MODIFIER_FILE);
 		PersistingUtils.saveFile(modifierFile, packetRules);
 	}
 
 }
