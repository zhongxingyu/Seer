 package com.bergerkiller.bukkit.rm.circuit;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.material.Diode;
 
 import com.bergerkiller.bukkit.common.BlockLocation;
import com.bergerkiller.bukkit.common.collections.BlockMap;
 import com.bergerkiller.bukkit.common.utils.BlockUtil;
 import com.bergerkiller.bukkit.common.utils.FaceUtil;
 import com.bergerkiller.bukkit.common.utils.MaterialUtil;
 import com.bergerkiller.bukkit.rm.PlayerSelect;
 import com.bergerkiller.bukkit.rm.RedstoneContainer;
 import com.bergerkiller.bukkit.rm.RedstoneMania;
 import com.bergerkiller.bukkit.rm.RedstoneMap;
 import com.bergerkiller.bukkit.rm.Util;
 import com.bergerkiller.bukkit.rm.element.PhysicalPort;
 import com.bergerkiller.bukkit.rm.element.SolidComponent;
 import com.bergerkiller.bukkit.rm.element.Inverter;
 import com.bergerkiller.bukkit.rm.element.Port;
 import com.bergerkiller.bukkit.rm.element.Redstone;
 import com.bergerkiller.bukkit.rm.element.Repeater;
 
 /**
  * Creates new Circuit instances from player-selected areas on the world
  */
 public class CircuitCreator {
 	private static final int TORCH_DELAY = 2; // Tick delay of a Redstone torch
 	private Player by;
 	private RedstoneMap map = new RedstoneMap();
 	private ArrayList<Redstone> items = new ArrayList<Redstone>();
 	private HashMap<String, CircuitInstance> subcircuits = new HashMap<String, CircuitInstance>();
 	private ArrayList<Block> ports = new ArrayList<Block>();
 	private BlockMap<Integer> delays = new BlockMap<Integer>();
 
 	public CircuitCreator(Player by, PlayerSelect from) {
 		this.by = by;
 		//prepare the ports, items and delays
 		this.delays.putAll(from.getDelays());
 		for (Map.Entry<String, BlockLocation> entry : from.getPorts().entrySet()) {
 			Port p = new Port();
 			p.name = entry.getKey();
 			Block b = entry.getValue().getBlock();
 			ports.add(b);
 			map.get(b).setValue(p).setPosition(entry.getValue().x, entry.getValue().z);
 			items.add(p);
 		}
 	}
 
 	/**
 	 * Creates and saves a new Circuit instance from the information in this Circuit Creator
 	 * 
 	 * @return new Circuit instance
 	 */
 	public Circuit create() {
 		//generate circuit for ALL ports
 		for (Block p : ports) {
 			createWire(map.get(p).value, p, Material.REDSTONE_WIRE);
 		}
 		//Set the position offset so the circuit will be nicely centered at 0x0
 		double midx = 0;
 		double midz = 0;
 		for (Redstone r : items) {
 			midx += r.getX() / items.size();
 			midz += r.getZ() / items.size();
 		}
 		for (Redstone r : items) {
 			r.setPosition(r.getX() - (int) midx, r.getZ() - (int) midz);
 		}
 
 		//save
 		Circuit c = new Circuit();
 		c.elements = items.toArray(new Redstone[0]);
 		c.subcircuits = subcircuits.values().toArray(new CircuitInstance[0]);
 		c.initialize();
 		return c;
 	}
 
 	private int getDelay(Block b, Material type) {
 		BlockLocation pos = new BlockLocation(b);
 		if (delays.containsKey(pos)) {
 			return delays.get(pos);
 		} else if (MaterialUtil.ISREDSTONETORCH.get(type)) {
 			return TORCH_DELAY;
 		} else if (MaterialUtil.ISDIODE.get(type)) {
 			return ((Diode) type.getNewData(b.getData())).getDelay() * TORCH_DELAY;
 		} else {
 			return 0;
 		}
 	}
 
 	private void msg(String message) {
 		this.by.sendMessage(ChatColor.YELLOW + message);
 	}
 
 	/**
 	 * Removes the Redstone from one position and places it again in to
 	 * @param from Redstone to transfer
 	 * @param to Redstone to replace
 	 */
 	private void transfer(Redstone from, Redstone to) {
 		if (from != to) {
 			map.merge(from, to);
 			from.transfer(to);
 			items.remove(from);
 		}
 	}	
 
 	/**
 	 * Creates the information of a single Block
 	 * 
 	 * @param block to create
 	 * @return Redstone Container of the resulting block
 	 */
 	private RedstoneContainer create(Block block) {
 		Material type = block.getType();
 		RedstoneContainer m = map.get(block);
 		if (m.value == null) {
 			if (MaterialUtil.ISREDSTONETORCH.get(type)) {
 				// Creates an inverter
 				m.setValue(new Inverter()).setPosition(block);
 				m.value.setPowered(type == Material.REDSTONE_TORCH_OFF);
 				m.value.setDelay(getDelay(block, type));
 				items.add(m.value);
 				createInverter((Inverter) m.value, block, type);
 			} else if (MaterialUtil.ISDIODE.get(type)) {
 				// Creates a repeater
 				m.setValue(new Repeater()).setPosition(block);
 				m.value.setPowered(type == Material.DIODE_BLOCK_ON);
 				m.value.setDelay(getDelay(block, type));
 				items.add(m.value);
 				createRepeater((Repeater) m.value, block, type);
 			} else if (type == Material.REDSTONE_WIRE) {
 				// Creates a wire
 				m.setValue(new Redstone()).setPosition(block);
 				m.value.setPowered(block.getData() > 0);
 				items.add(m.value);
 				createWire(m.value, block, type);
 			} else if (type == Material.LEVER) {
 				// Creates a port
 				Port searchport = Port.get(block);
 				if (searchport != null) {
 					CircuitBase base = searchport.getCircuit();
 					if (base == null) {
 						RedstoneMania.plugin.log(Level.SEVERE, "[Creation] Failed to obtain circuit from port '" + searchport.name + "'!");
 					} else {
 						// Create a new circuit instance
 						CircuitInstance cb = (CircuitInstance) base;
 						String fullname = cb.getFullName();
 						CircuitInstance ci = subcircuits.get(fullname);
 						if (ci == null) {
 							ci = cb.source.createInstance();
 							subcircuits.put(fullname, ci);
 						}
 						if (ci == null) {
 							RedstoneMania.plugin.log(Level.SEVERE, "[Creation] Failed to convert circuit '" + base.getFullName() + "'!");
 						} else {
 							//get the ports of the found circuit
 							Collection<Port> realports = base.getPorts();
 							for (Port realport : realports) {
 								Port port = ci.getPort(realport.name);
 								if (port == null) {
 									RedstoneMania.plugin.log(Level.WARNING, "[Creation] Failed to find port '" + realport.name + "' in circuit '" + ci.getFullName() + "'!");
 								} else {
 									port.setPowered(realport.isPowered());
 									boolean outofreach = false;
 									for (PhysicalPort pp : realport.locations) {
 										Block at = pp.position.getBlock();
 										if (at == null) {
 											outofreach = true;
 										} else {
 											for (BlockFace leverface : FaceUtil.ATTACHEDFACES) {
 												Block lever = at.getRelative(leverface);
 												if (lever.getType() == Material.LEVER) {
 													map.get(lever).setValue(port).setPosition(lever.getX(), lever.getZ());
 													createPort(port, lever, Material.LEVER);
 												}
 											}
 										}
 									}
 									if (outofreach) {
 										msg("One or more ports of '" + ci.getFullName() + "' are out of reach!");
 									}
 								}								
 							}
 						}
 					}
 				}
 			} else if (Util.ISSOLID.get(type)) {
 				createSolid(m.setValue(new SolidComponent(block)), block, type);
 			}
 		}
 		return m;
 	}
 
 	private void createPort(Port redstone, Block lever, Material type) {
 		for (BlockFace face : FaceUtil.ATTACHEDFACESDOWN) {
 			Block b = lever.getRelative(face);
 			Material btype = b.getType();
 			if (btype == Material.REDSTONE_WIRE) {
 				if (face == BlockFace.DOWN) {
 					redstone.connectTo(create(b).value);
 				} else {
 					create(b).value.connect(redstone);
 				}
 			} else if (MaterialUtil.ISREDSTONETORCH.get(btype)) {
 				create(b).value.connectTo(redstone);
 			}
 		}
 	}
 
 	private void createInverter(Inverter redstone, Block inverter, Material type) {
 		for (BlockFace face : FaceUtil.ATTACHEDFACESDOWN) {
 			Block b = inverter.getRelative(face);
 			Material btype = b.getType();
 			if (btype == Material.REDSTONE_WIRE) {
 				redstone.connectTo(create(b).value);
 			} else if (btype == Material.DIODE_BLOCK_OFF || btype == Material.DIODE_BLOCK_ON) {
 				if (face != BlockFace.DOWN) { 
 					//connected to the input?
 					BlockFace facing = BlockUtil.getFacing(b);
 					if (facing == face) {
 						redstone.connectTo(create(b).value);
 					}
 				}
 			}
 		}
 		Block above = inverter.getRelative(BlockFace.UP);
 		Material abovetype = above.getType();
 		if (Util.ISSOLID.get(abovetype)) {
 			create(above);
 		}
 		create(BlockUtil.getAttachedBlock(inverter));
 	}
 
 	private void createRepeater(Repeater redstone, Block repeater, Material type) {
 		BlockFace facing = BlockUtil.getFacing(repeater);
 		Block output = repeater.getRelative(facing);
 		Material outputtype = output.getType();
 		if (outputtype == Material.REDSTONE_WIRE) {
 			//connect this repeater to wire
 			redstone.connectTo(create(output).value);
 		} else if (MaterialUtil.ISDIODE.get(outputtype)) {
 			BlockFace oface = BlockUtil.getFacing(output);
 			if (facing == oface) {
 				redstone.connectTo(create(output).value);
 			}
 		} else if (Util.ISSOLID.get(outputtype)) {
 			create(output);
 		}
 		Block input = repeater.getRelative(facing.getOppositeFace());
 		Material inputtype = repeater.getType();
 		if (inputtype == Material.REDSTONE_WIRE) {
 			//connect this repeater to wire
 			create(input).value.connectTo(redstone);
 		} else if (MaterialUtil.ISDIODE.get(inputtype)) {
 			BlockFace oface = BlockUtil.getFacing(input);
 			if (facing == oface) {
 				create(input).value.connectTo(redstone);
 			}
 		} else if (Util.ISSOLID.get(inputtype)) {
 			create(input);
 		}
 	}
 
 	private Redstone connectWire(Block wire, Redstone redstone) {
 		RedstoneContainer m = map.get(wire);
 		if (m.value == redstone) {
 			return redstone;
 		}
 		if (m.value == null) {
 			m.setValue(redstone);
 			//added block to this wire
 			createWire(redstone, wire, Material.REDSTONE_WIRE);
 			return redstone;
 		} else {
 			//merge the two wires
 			if (redstone instanceof Port) {
 				if (m.value instanceof Port) {
 					Port p1 = (Port) redstone;
 					Port p2 = (Port) m.value;
 					msg("Port '" + p1.name + "' merged with port '" + p2.name + "'!");
 				}
 				transfer(m.value, redstone);
 				return redstone;
 			} else {
 				transfer(redstone, m.value);
 				return m.value;
 			}
 		}
 	}
 
 	private void createWire(Redstone redstone, Block wire, Material type) {
 		//wire - first find all nearby elements
 		Block abovewire = wire.getRelative(BlockFace.UP);
 		Material abovetype = abovewire.getType();
 		for (BlockFace face : FaceUtil.AXIS) {
 			Block b = wire.getRelative(face);
 			Material btype = b.getType();
 			if (btype == Material.REDSTONE_WIRE) {
 				//same wire
 				redstone = connectWire(b, redstone);				
 			} else if (btype == Material.AIR) {
 				//wire below?
 				Block below = b.getRelative(BlockFace.DOWN);
 				if (below.getType() == Material.REDSTONE_WIRE) {
 					redstone = connectWire(below, redstone);
 				}
 			} else if (MaterialUtil.ISREDSTONETORCH.get(btype)) {
 				//this wire receives input from this torch
 				create(b); //we assume that the torch handles direct wire connection
 			} else if (MaterialUtil.ISDIODE.get(btype)) {
 				//powering or receiving power
 				BlockFace facing = BlockUtil.getFacing(b);
 				if (facing == face) {
 					//wire powers repeater
 					redstone.connectTo(create(b).value);
 				} else if (facing.getOppositeFace() == face) {
 					//repeater powers wire
 					create(b); //we assume that the repeater handles direct wire connections
 				}
 			} else if (btype == Material.LEVER) {
 				//let the port handle this
 				create(b);
 			} else if (abovetype == Material.AIR && Util.ISSOLID.get(btype)) {
 				//wire on top?
 				Block above = b.getRelative(BlockFace.UP);
 				if (above.getType() == Material.REDSTONE_WIRE) {
 					redstone = connectWire(above, redstone);
 				}
 				create(b);
 			}
 		}
 		//Lever above?
 		if (abovetype == Material.LEVER) {
 			create(abovewire);
 		}
 		//update the block this wire sits on
 		create(wire.getRelative(BlockFace.DOWN));
 		//a torch above this wire?
 		Block above = wire.getRelative(BlockFace.UP);
 		if (MaterialUtil.ISREDSTONETORCH.get(above)) create(above);
 	}
 
 	private void createSolid(SolidComponent comp, Block block, Material type) {
 		//create block data
 		RedstoneContainer[] inputs = new RedstoneContainer[comp.inputs.size()];
 		RedstoneContainer[] outputs = new RedstoneContainer[comp.outputs.size()];
 		for (int i = 0; i < inputs.length; i++) {
 			inputs[i] = create(comp.inputs.get(i));
 		}
 		for (int i = 0; i < outputs.length; i++) {
 			outputs[i] = create(comp.outputs.get(i));
 		}
 		//connect inputs with outputs
 		for (RedstoneContainer input : inputs) {
 			for (RedstoneContainer output : outputs) {
 				if (input.value.isType(0, 3)) {
 					if (output.value.isType(0, 3)) {
 						//a wire does NOT power other wires!
 						continue;
 					}
 				}
 				input.value.connectTo(output.value);
 			}
 		}
 	}
 }
