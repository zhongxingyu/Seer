 package org.morden.lightcontrol;
 
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.command.CommandSender;
 import org.bukkit.block.BlockFace;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.Location;
import org.tal.redstonechips.channel.ReceivingCircuit;
 import org.tal.redstonechips.util.BitSet7;
 import org.tal.redstonechips.util.BitSetUtils;
 import org.tal.redstonechips.util.Locations;
 
 /**
  *
  * @author Dennis Flanagan
  */
 public abstract class LightCore extends ReceivingCircuit {
 	private int mode = 0;
 	private int addressSize;
 	private boolean currentState = false;
 	private int currentAddress;
 	protected BitSet7 output = null;
 	protected static BlockFace[] faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
 	protected Material onMaterial = null;
 	protected Material offMaterial = null;
  
     @Override
     public void inputChange(int inIdx, boolean newLevel) {
 		if (mode == 0) {
 			if (inIdx == 0) {
 				if (newLevel) {
 					output.set(currentAddress, currentState);
 					updateOutputs();
 				}
 			} else if (inIdx == 1) {
 				currentState = newLevel;
 			} else {
 				currentAddress = BitSetUtils.bitSetToUnsignedInt(inputBits.get(2, addressSize + 2), 0, addressSize);
 			}	
 		} else if (mode == 1) {
 			output.set(inIdx, newLevel);
 			updateOutputs();
 		} else {
 			output.set(0, interfaceBlocks.length, newLevel);
 			updateOutputs();
 		}
 	}
 
     @Override
     protected boolean init(CommandSender sender, String[] args) {
 		String channelName = new String();
 		boolean wireless = false;
 		if (interfaceBlocks.length==0) {
             error(sender, "Expecting at least 1 interface block.");
             return false;
         }
 		
 		if (inputs.length==0 && args.length==0) {
             error(sender, "Wireless mode requires a channel name.");
             return false;
         }
 		
 		if (inputs.length == ((int)Math.ceil(Math.log(interfaceBlocks.length)/Math.log(2)) + 2)) {
 			mode = 0;
 		}
 		
 		if (interfaceBlocks.length==inputs.length) {
             mode = 1;
         }
 		
 		if (inputs.length==1) {
             mode = 2;
         }
 		
 		if (args.length > 0) {
 			if (args[0].equalsIgnoreCase("addressed")) {
 				mode = 0;
 				if (args.length > 1) {
 					channelName = args[1];
 					wireless = true;
 				}
 			} else if (args[0].equalsIgnoreCase("indexed")) {
 				mode = 1;
 				if (args.length > 1) {
 					channelName = args[1];
 					wireless = true;
 				}
 			} else if (args[0].equalsIgnoreCase("all")) {
 				mode = 2;
 				if (args.length > 1) {
 					channelName = args[1];
 					wireless = true;
 				}
 			} else {
 				channelName = args[0];
 				wireless = true;
 			}
 		}
 		
 		if (!wireless) {
 			if (mode == 0) {
 				addressSize = (int)Math.ceil(Math.log(interfaceBlocks.length)/Math.log(2));
 				if (inputs.length != addressSize + 2) {
 					error(sender, "Expecting " + (addressSize + 2) + " inputs for Addressed Mode.");
 					return false;
 				}
 			} else if (mode == 1) {
 				if (inputs.length != interfaceBlocks.length) {
 					error(sender, "Expecting the same number of inputs and interface blocks for Indexed Mode.");
 					return false;
 				}
 			} else {
 				if (inputs.length > 1 ) {
 					error(sender, "Expecting only one input block for All Mode.");
 					return false;
 				}
 			}
 		} else {
 			try {
 				if (mode == 0) {
 					addressSize = (int)Math.ceil(Math.log(interfaceBlocks.length)/Math.log(2));
 				}

				this.initWireless(sender, channelName);
 			} catch (IllegalArgumentException ie) {
 				error(sender, ie.getMessage());
 				return false;
 			}
 		}
 		
 		if (output == null) {
 			output = new BitSet7(interfaceBlocks.length);
 		}
 		
 		updateOutputs();
 		
 		return true;
     }
 
     @Override
     public void receive(BitSet7 bits) {
        if (hasDebuggers()) debug("Received " + BitSetUtils.bitSetToBinaryString(bits, 0, getChannelLength()));
 		//if (hasDebuggers()) debug("Received " + BitSetUtils.bitSetToBinaryString(bits, 0, getChannelLength()));
 		if (mode == 0) {
 			output.set(BitSetUtils.bitSetToUnsignedInt(bits.get(1, addressSize + 1), 0, addressSize), bits.get(0));
 		} else if (mode == 1) {
 			for (int i=0; i<interfaceBlocks.length; i++) {
                 output.set(i, bits.get(i));
             }
 		} else {
 			output.set(0, interfaceBlocks.length, bits.get(0));
 		}
 		updateOutputs();
     }
 
     /*@Override
     public int getChannelLength() {
 		if (mode == 0) {
 			return addressSize + 2;
 		} else if (mode == 1) {
 			return interfaceBlocks.length;
 		}
 		return 1;
     }*/
 	
 	@Override
    public int getChannelLength() {
 		if (mode == 0) {
 			return addressSize + 2;
 		} else if (mode == 1) {
 			return interfaceBlocks.length;
 		}
 		return 1;
     }
 	
 	@Override
     public void circuitDestroyed() {
         if (getChannel()!=null) redstoneChips.removeReceiver(this);
 	}
 	
 	@Override
 	protected boolean isStateless() {
         return false;
     }
 	
 	@Override
     public void setInternalState(Map<String, String> state) {
 		BitSet7 outputState = BitSetUtils.mapToBitSet(state, "output") ;
         if (outputState!=null) {
             output = outputState;
         }
     }
 
     @Override
     public Map<String, String> getInternalState() {
         Map<String,String> state = new HashMap<String, String>();
 		BitSetUtils.bitSetToMap(state, "output", output, interfaceBlocks.length);
 		return state;
     }
 	
 	protected void updateOutputs()
 	{
 		for (int i = 0; i < interfaceBlocks.length; i++) {
 			for (BlockFace face : faces) {
 				Location f = Locations.getFace(interfaceBlocks[i], face);
 				Block block = world.getBlockAt(f);
 				updateBlock(block, output.get(i));
 			}
 		}
 	}
 	
 	protected abstract void updateBlock(Block block, boolean value);
 }
