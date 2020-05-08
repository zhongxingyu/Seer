 package com.nilhcem.mowitnow.core.instruction.parser;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.nilhcem.mowitnow.core.Field;
 import com.nilhcem.mowitnow.core.Mower;
 import com.nilhcem.mowitnow.core.instruction.MowerInstruction;
 import com.nilhcem.mowitnow.core.instruction.MowerOrientation;
 
 /**
  * Provides useful methods to parse lawn-mower instruction files.
  */
 public final class InstructionsParser {
 	private static final int MIN_INSTRUCTIONS_REQUIRED = 3;
 	private static final String REGEX_SURFACE = "^\\s*(\\d+)\\s+(\\d+)\\s*$";
 	private static final String REGEX_MOWER_LOCATION = "^\\s*(\\d+)\\s+(\\d+)\\s+([NEWS])\\s*$";
 
 	private final Field field;
 	private final Queue<Mower> mowers = new LinkedList<Mower>();
 	private final Map<Mower, List<MowerInstruction>> instructions = new HashMap<Mower, List<MowerInstruction>>();
 
 	/**
	 * Parses instructions and creates appropriate objects.
 	 * <p>
 	 * Once the constructor has been called, the following objects will be created:
 	 * <ul>
 	 * <li>Field: a garden with a specified size.</li>
 	 * <li>Mowers: the lawn-mowers that will be placed in the garden.</li>
 	 * <li>Instructions: each lawn-mower will have its list of instructions, to process.</li>
 	 * </ul>
 	 * </p>
 	 *
 	 * @param instructions a list of instructions.
 	 * @see "instructions specifications".
 	 */
 	public InstructionsParser(List<String> instructions) {
 		if (instructions.size() < MIN_INSTRUCTIONS_REQUIRED) {
 			throw new ParserException("At least " + MIN_INSTRUCTIONS_REQUIRED + " instructions are required");
 		}
 
 		field = parseFieldData(instructions.get(0));
 		parseMowersData(instructions.subList(1, instructions.size()));
 	}
 
 	/**
 	 * Retrieves the surface where mowers will be placed.
 	 *
 	 * @return the Field.
 	 */
 	public Field getField() {
 		return field;
 	}
 
 	/**
 	 * Retrieves and removes the Mower in the head of the queue, or returns null if the queue is empty.
 	 *
 	 * @return the next Mower, or the head of this queue, or {@code null} if the queue is empty.
 	 */
 	public Mower getNextMower() {
 		return mowers.poll();
 	}
 
 	/**
 	 * Retrieves a list of instructions for the mower passed in parameter.
 	 *
 	 * @param mower the Mower we need instructions for.
 	 * @return a list of instructions for the mower passed in parameter.
 	 */
 	public List<MowerInstruction> getInstructionsForMower(Mower mower) {
 		return instructions.get(mower);
 	}
 
 	private Field parseFieldData(String data) {
 		Pattern pattern = Pattern.compile(REGEX_SURFACE);
 		Matcher matcher = pattern.matcher(data);
 
 		if (matcher.find()) {
 			try {
 				int width = Integer.parseInt(matcher.group(1));
 				int height = Integer.parseInt(matcher.group(2));
 				return new Field(width, height);
 			} catch (NumberFormatException e) {
 				throw new ParserException(e);
 			}
 		} else {
 			throw new ParserException("Invalid field surface instruction. Should match " + REGEX_SURFACE);
 		}
 	}
 
 	private void parseMowersData(List<String> instrs) {
 		boolean locationLine = true;
 
 		Pattern locPattern = Pattern.compile(REGEX_MOWER_LOCATION);
 		Mower mower = null;
 		for (String data : instrs) {
 			if (locationLine) {
 				mower = createMowerFromLocationData(locPattern, data);
 			} else {
 				mowers.add(mower);
 				instructions.put(mower, createInstructionsListFromData(data));
 			}
 			locationLine = !locationLine;
 		}
 	}
 
 	private Mower createMowerFromLocationData(Pattern pattern, String data) {
 		Matcher matcher = pattern.matcher(data);
 		if (matcher.find()) {
 			try {
 				int x = Integer.parseInt(matcher.group(1));
 				int y = Integer.parseInt(matcher.group(2));
 				char c = matcher.group(3).charAt(0);
 				MowerOrientation orientation = MowerOrientation.getFromChar(c);
 				if (orientation != null) {
 					return new Mower(new Point(x, y), orientation, field);
 				}
 			} catch (IndexOutOfBoundsException e) {
 				throw new ParserException(e);
 			} catch (NumberFormatException e) {
 				throw new ParserException(e);
 			}
 		}
 		throw new ParserException("Invalid mower location/orientation: " + data
 				+ " - should match " + REGEX_MOWER_LOCATION);
 	}
 
 	private List<MowerInstruction> createInstructionsListFromData(String data) {
 		List<MowerInstruction> list = new ArrayList<MowerInstruction>();
 
 		for (char c : data.toCharArray()) {
 			MowerInstruction instr = MowerInstruction.getFromChar(c);
 			if (instr != null) {
 				list.add(instr);
 			}
 			// Note: we should probably display a warning log here to point out that one instruction was invalid.
 		}
 
 		if (list.isEmpty()) {
 			throw new ParserException("Invalid mower instructions: " + data
 					+ " - should match [AGD]+");
 		}
 		return list;
 	}
 }
