 package com.gmail.quarzekk.worldstudio.core.config.mesh;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import com.gmail.quarzekk.worldstudio.core.config.IConfigParser;
 
 public class MeshParser implements IConfigParser {
 	
 	/**
 	 * The MeshFile parsed by this MeshParser.
 	 */
 	private MeshFile meshFile;
 	
 	/**
 	 * The current state of the parse procedure. This is used to create a
 	 * context with which subsequent lines are parsed.
 	 */
 	private int parseState;
 	
 	public MeshParser(MeshFile meshFile) {
 		this.meshFile = meshFile;
 		this.parseState = 0;
 	}
 	
 	/**
 	 * Gets the {@link MeshFile} parsed by this MeshParser.
 	 * @return The parsed MeshFile
 	 */
 	public MeshFile getMeshFile() {
 		return this.meshFile;
 	}
 	
 	/**
 	 * Parses the contents of the mesh file and stores extracted data. It is
 	 * necessary to parse the file prior to accessing any data.
 	 * @throws IOException If an I/O error occurs while reading the mesh file
 	 */
 	public void parse() throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.meshFile.getFile())));
 		
 		String line = null;
 		int lineNum = 0;
 		
 		while ((line = reader.readLine()) != null) {
 			lineNum++;
 			line = line.trim();
 			
 			if (line.isEmpty() || line.startsWith("#")) {
 				continue;
 			}
 			
 			while (line.contains("#")) {
				line = line.substring(line.lastIndexOf("#")).trim();
 			}
 			
 			this.parseLine(line, lineNum);
 		}
 		
 		reader.close();
 	}
 	
 	/**
 	 * Parses an individual line of the mesh file as provided by
 	 * {@link MeshParser#parse()}.
 	 * @param line The line to parse
 	 */
 	private void parseLine(String line, int lineNum) {
 		if (this.parseState == 0) {
 			if (line.length() >= 5 && line.substring(0, 5).equalsIgnoreCase("block")) {
 				if (line.length() >= 8 && line.substring(6, 8).equalsIgnoreCase("id")) {
 					String idString = line.substring(9);
 					int id = -1;
 					try {
 						id = Integer.parseInt(idString);
 					} catch (NumberFormatException e) {
 						this.printLineError(e, "Could not parse block ID as integer", line, lineNum, 9);
 					}
 					this.meshFile.setBlockId(id);
 				}
 				if (line.length() >= 10 && line.substring(6, 10).equalsIgnoreCase("data")) {
 					String dataString = line.substring(11);
 					int data = -1;
 					try {
 						data = Integer.parseInt(dataString);
 					} catch (NumberFormatException e) {
 						this.printLineError(e, "Could not parse block data as integer", line, lineNum, 11);
 					}
 					this.meshFile.setBlockData(data);
 				}
 			}
 			if (line.length() >= 7 && line.substring(0, 7).equalsIgnoreCase("texture")) {
 				if (line.length() >= 13 && line.substring(8, 13).equalsIgnoreCase("tileU")) {
 					String tileUString = line.substring(14);
 					int tileU = -1;
 					try {
 						tileU = Integer.parseInt(tileUString);
 					} catch (NumberFormatException e) {
 						this.printLineError(e, "Could not parse texture tile U as integer", line, lineNum, 14);
 					}
 					this.meshFile.setTextureTileU(tileU);
 				}
 				if (line.length() >= 13 && line.substring(8, 13).equalsIgnoreCase("tileV")) {
 					String tileVString = line.substring(14);
 					int tileV = -1;
 					try {
 						tileV = Integer.parseInt(tileVString);
 					} catch (NumberFormatException e) {
 						this.printLineError(e, "Could not parse texture tile V as integer", line, lineNum, 14);
 					}
 					this.meshFile.setTextureTileV(tileV);
 				}
 				if (line.length() >= 16 && line.substring(8, 17).equalsIgnoreCase("tileSpanU")) {
 					String tileSpanUString = line.substring(18);
 					int tileSpanU = -1;
 					try {
 						tileSpanU = Integer.parseInt(tileSpanUString);
 					} catch (NumberFormatException e) {
 						this.printLineError(e, "Could not parse texture tile span U as integer", line, lineNum, 18);
 					}
 					this.meshFile.setTextureTileSpanU(tileSpanU);
 				}
 				if (line.length() >= 16 && line.substring(8, 17).equalsIgnoreCase("tileSpanV")) {
 					String tileSpanVString = line.substring(18);
 					int tileSpanV = -1;
 					try {
 						tileSpanV = Integer.parseInt(tileSpanVString);
 					} catch (NumberFormatException e) {
 						this.printLineError(e, "Could not parse texture tile span V as integer", line, lineNum, 18);
 					}
 					this.meshFile.setTextureTileSpanU(tileSpanV);
 				}
 			}
 			if (line.length() >= 5 && line.substring(0, 5).equalsIgnoreCase("begin")) {
 				if (line.length() >= 6) {
 					String groupString = line.substring(6);
 					if (groupString.equals("vertex")) {
 						this.parseState = 1;
 					} else if (groupString.equals("texcoord")) {
 						this.parseState = 2;
 					}
 				} else {
 					this.printLineError(null, "Group name not specified", line, lineNum, line.length() - 1);
 				}
 			}
 			if (line.length() >= 3 && line.substring(0, 3).equalsIgnoreCase("end")) {
 				this.printLineError(null, "Attempt to end a nonexistant group", line, lineNum, -1);
 			}
 		} else if (this.parseState == 1) {
 			if (line.length() >= 6 && line.substring(0, 6).equalsIgnoreCase("vertex")) {
 				String[] components = line.substring(7).split(" ");
 				if (components.length < 3) {
 					this.printLineError(null, "Insufficient amount of coordinates (" + components.length + "/3 given)", line, lineNum, -1);
 					if (components.length == 0) {
 						components = new String[] {
 							"0",
 							"0",
 							"0",
 						};
 					} else if (components.length == 1) {
 						components = new String[] {
 							components[0],
 							"0",
 							"0",
 						};
 					} else if (components.length == 2) {
 						components = new String[] {
 							components[0],
 							components[1],
 							"0",
 						};
 					}
 				}
 				String coordXString = components[0];
 				String coordYString = components[1];
 				String coordZString = components[2];
 				float coordX = 0;
 				float coordY = 0;
 				float coordZ = 0;
 				try {
 					coordX = Float.parseFloat(coordXString);
 				} catch (NumberFormatException e) {
 					this.printLineError(e, "Could not parse x-coordinate of vertex as float", line, lineNum, 7);
 				}
 				try {
 					coordY = Float.parseFloat(coordYString);
 				} catch (NumberFormatException e) {
 					this.printLineError(e, "Could not parse y-coordinate of vertex as float", line, lineNum, 8 + coordXString.length());
 				}
 				try {
 					coordZ = Float.parseFloat(coordZString);
 				} catch (NumberFormatException e) {
 					this.printLineError(e, "Could not parse z-coordinate of vertex as float", line, lineNum, 9 + coordXString.length() + coordYString.length());
 				}
 				this.meshFile.addVertexData(coordX);
 				this.meshFile.addVertexData(coordY);
 				this.meshFile.addVertexData(coordZ);
 			}
 			if (line.length() >= 3 && line.substring(0, 3).equalsIgnoreCase("end")) {
 				this.parseState = 0;
 			}
 		} else if (this.parseState == 2) {
 			if (line.length() >= 8 && line.substring(0, 8).equalsIgnoreCase("texcoord")) {
 				String[] components = line.substring(9).split(" ");
 				if (components.length < 2) {
 					this.printLineError(null, "Insufficient amount of coordinates (" + components.length + "/2 given)", line, lineNum, -1);
 					if (components.length == 0) {
 						components = new String[] {
 							"0",
 							"0",
 						};
 					} else if (components.length == 1) {
 						components = new String[] {
 							components[0],
 							"0",
 						};
 					}
 				}
 				String coordUString = components[0];
 				String coordVString = components[1];
 				float coordU = 0;
 				float coordV = 0;
 				try {
 					coordU = Float.parseFloat(coordUString);
 				} catch (NumberFormatException e) {
 					this.printLineError(e, "Could not parse u-coordinate of texcoord as float", line, lineNum, 9);
 				}
 				try {
 					coordV = Float.parseFloat(coordVString);
 				} catch (NumberFormatException e) {
 					this.printLineError(e, "Could not parse v-coordinate of texcoord as float", line, lineNum, 10 + coordUString.length());
 				}
 				this.meshFile.addTexcoordData(coordU);
 				this.meshFile.addTexcoordData(coordV);
 			}
 			if (line.length() >= 3 && line.substring(0, 3).equalsIgnoreCase("end")) {
 				this.parseState = 0;
 			}
 		}
 	}
 	
 	/**
 	 * Prints a formatted error string to STDERR.
 	 * @param throwable The Throwable associated with the error
 	 * @param message A message describing the error
 	 * @param line The offending line
 	 * @param lineNum The number of the line
 	 * @param charNum The number of the character within the line at which the
 	 * error occurs (-1 for the entire line)
 	 */
 	private void printLineError(Throwable throwable, String message, String line, int lineNum, int charNum) {
 		System.err.println("An error occurred while parsing mesh file \"" + this.meshFile.getFile().getAbsolutePath() + "\"");
 		System.err.println("Error: " + message);
 		System.err.println();
 		
 		System.err.println(lineNum + ":\"" + line + "\"");
 		if (charNum > -1) {
 			System.err.print("+-");
 			for (int i = 0; i < String.valueOf(lineNum).length(); i++) {
 				System.err.print("-");
 			}
 			for (int i = 0; i < charNum; i++) {
 				System.err.print("-");
 			}
 			System.err.println("^");
 			System.err.println();
 		} else {
 			System.err.println();
 		}
 		
 		if (throwable != null) {
 			throwable.printStackTrace();
 			System.err.println();
 		}
 	}
 	
 }
