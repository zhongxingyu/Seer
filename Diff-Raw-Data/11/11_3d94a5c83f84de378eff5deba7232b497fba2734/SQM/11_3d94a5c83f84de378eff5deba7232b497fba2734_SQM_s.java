 package de.avdclan.arma2mapconverter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 public class SQM {
 
 	private TypeClass rootType = new TypeClass("root", null);
 	private static Logger logger = Logger.getLogger(SQM.class);
 	private BufferedReader reader;
 	private int groupCountWest = 0;
 	private int groupCountEast = 0;
 	private int groupCountGuer = 0;
 	private int groupCountCiv = 0;
 	private File source;
 	
 	public void load(File mission) throws FileNotFoundException {
 		logger.debug("Loading SQM Mission: " + mission.getAbsolutePath());
 		this.source = mission;
 		reader = new BufferedReader(new FileReader(mission));
 		String line;
 		try {
 			while((line = reader.readLine()) != null) {
 				
 				parse(line, rootType);
 				
 				
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			logger.error(e);
 		}
 		
 		
 		logger.debug("Loaded.");
 	}
 	
 	/**
 	 * This is the parsing algorithm.
 	 * If you know a better way, feel free to change it.
 	 * 
 	 * Please also send your changes to the author.
 	 * 
 	 * 
 	 * @param input
 	 * @throws IOException
 	 */
 	private void parse(String input, TypeClass parent) throws IOException {
 		String line = input.replaceAll("^\\s+", "");
 		if(line.startsWith("class")) {
 			
 			String[] spl = line.split(" ", 2);
 			TypeClass typeClass = new TypeClass(spl[1], parent);
 			parent.getChilds().add(typeClass);
 			logger.debug("Found class: " + typeClass);
 			
 			
 			while(! (line = reader.readLine().replaceAll("^\\s+", "")).startsWith("}")) {
 				parse(line, typeClass);
 			}
 			
 		}	
 		
 		if(parent.toString().startsWith("Vehicles")) {
 			if(parent.getObject() == null) {
 				parent.setObject(new Vehicle());
 			}
 			TypeClass p = parent.getParent();
 			if(p.toString().startsWith("Item")) {
 				
 				((Vehicle)parent.getObject()).setSide(((Item)p.getObject()).getSide());
 				
 			}
 			
 			
 		}
 		if(parent.toString().startsWith("Item")) {
 			if(parent.getObject() == null) {
 				parent.setObject(new Item(parent.toString()));
 			}
 			if(line.startsWith("position[]")) {
 				String[] tmp = line.split("=", 2);
 				tmp = tmp[1].split(",", 3);
 				String x, y, z;
 				x = tmp[0].replaceAll("\\{", "");
 				z = tmp[1];
 				y = tmp[2].replaceAll("\\}\\;", "");
 				((Item)parent.getObject()).setPosition(new Position(x, y, "0"));
 			}
 			if(line.startsWith("id")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setId(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("side")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setSide(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("vehicle")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setVehicle(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("skill")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setSkill(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("leader")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setLeader(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("init")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setInit(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("name")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setName(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("markerType")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setMarkerType(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("type")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setType(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("presenceCondition")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setPresenceCondition(tmp[1].replaceAll("\\;", ""));
 			}
 			if(line.startsWith("azimut")) {
 				String[] tmp = line.split("=", 2);
 				((Item)parent.getObject()).setAzimut(tmp[1].replaceAll("\\;", ""));
 			}
 		}
 		
 	}
 
 	
 
 	public SQF toSQF() {
 		SQF sqf = new SQF();
 		String code = "" +
 				"/* converted with Arma2MapConverter v" + Arma2MapConverter.VERSION + "\n" +
 				" *\n" +
 				" * Source: " + source.getAbsolutePath() + "\n" +
 				" * Date: " + DateFormat.getInstance().format(Calendar.getInstance().getTime()) + "\n" +
 				" */\n\n";
 		code +=	"_westHQ = createCenter west;\n" +
 				"_eastHQ = createCenter east;\n" +
 				"_guerHQ = createCenter resistance;\n";
 		
 		code += generateSQF(rootType);
 		sqf.setCode(code);
 		return sqf;
 	}
 
 	private String generateSQF(TypeClass typeClass) {
 		String code = "";
 						
 		int groupCount = 0;
 		for(TypeClass tc : typeClass.getChilds()) {
 			if(tc.equals("Vehicles")) {
 				
 				String side = ((Vehicle)tc.getObject()).getSide().toLowerCase();
 				String group = "_group_" + side + "_" + getGroupCound(side);
 				
 				code +=	"\n// group " + group + "\n" +
 						group + " = createGroup _" + side +"HQ;\n";
 					
 				for(TypeClass items : tc.getChilds()) {
 					
 					Item item = (Item) items.getObject();
 					
 					
 					if(item.getName() == null) {
 						// generate unique unit name
 						item.setName("autogen_" + System.currentTimeMillis());
 						try {
 							Thread.sleep(105);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							logger.error(e);
 						}
 					}
 					code += "\n// begin " + item.getName() +", part of group " + group + "\n";
 					code += "if (" + item.getPresenceCondition() + ") then\n{";
 					if(item.getSide().equals("EMPTY")) {
 						code += "\n" +
 							"\t" + item.getName() + " = createVehicle [" + item.getVehicle() + ", " + item.getPosition() + ", [], 0, \"CAN_COLLIDE\"];\n";
 					} else {
 						code += "\n" +
 								"\t" + item.getName() + " = " + group + " createUnit [" + item.getVehicle() + ", " + item.getPosition() + ", [], 0, \"CAN_COLLIDE\"];\n";
 					}
 					if(item.getInit() != null) {
 						code += "\t" + item.getName() + " setVehicleInit " + item.getInit() + ";\n";
 					}
 					
 					if(item.getAzimut() != null) {
 						code += "\t" + item.getName() + " setDir " + item.getAzimut() + ";\n";
 					}
 					
 					if(item.getSkill() != null) {
 						code += "\t" + item.getName() + " setUnitAbility " + item.getSkill() +";\n";
 					}
 					
 					if(item.getLeader() != null) {
						code += "\tif((alive " + item.getName() + ") || (!isEmpty " + item.getName() + ")) then {" + group + " selectLeader " + item.getName() + "; };\n";
 					}
 					
 					code += "};\n// end of " + item.getName() + "\n";
 					
 				}
 				
 				
 			} else {
 				code += generateSQF(tc); 
 			}
 		}
 		
 		return code;
 	}
 
 	private String getGroupCound(String side) {
 		if(side.equals("west")) {
 			++groupCountWest;
 			return String.valueOf(groupCountWest);
 		}
 		if(side.equals("east")) {
 			++groupCountEast;
 			return String.valueOf(groupCountEast);
 		}
 		if(side.equals("guer")) {
 			++groupCountGuer;
 			return String.valueOf(groupCountGuer);
 		}
 		
 		++groupCountCiv;
 		return String.valueOf(groupCountCiv);
 		
 		
 	}
 	
 
 }
