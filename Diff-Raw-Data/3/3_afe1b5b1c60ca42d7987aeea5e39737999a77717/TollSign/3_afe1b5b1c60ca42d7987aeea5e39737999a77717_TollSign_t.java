 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 package graindcafe.tribu.Signs;
 
 import java.util.LinkedList;
 
 import graindcafe.tribu.PlayerStats;
 import graindcafe.tribu.Tribu;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.Door;
 
 public class TollSign extends TribuSign {
 
 	private int cost;
 	// private boolean clicked = false;
 	private Block linkedButton;
 	private static LinkedList<Player> allowedPlayer;
 
 	public TollSign(Tribu plugin, Location pos, String[] lines) {
 		super(plugin, pos);
 		cost = TribuSign.parseInt(lines[1]);
 		setAllowedPlayer(new LinkedList<Player>());
 	}
 
 	@Override
 	protected String[] getSpecificLines() {
 		String[] lines = new String[4];
 		lines[0] = "";
 		lines[1] = String.valueOf(cost);
 		lines[2] = "";
 		lines[3] = "";
 		return lines;
 	}
 
 	@Override
 	public void init() {
 		Block current;
 		// plugin.LogInfo("Toll Sign at " + pos.getBlockX() + ", " +
 		// pos.getBlockY() + ", " + pos.getBlockZ());
 		BlockFace[] firstFaces = new BlockFace[] { BlockFace.SELF, BlockFace.UP, BlockFace.DOWN };
 		BlockFace[] secondFaces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
 		for (BlockFace bf : firstFaces) {
 			current = pos.getBlock().getRelative(bf);
 			for (BlockFace bf2 : secondFaces) {
 				/*
 				 * plugin.LogInfo(bf2.name() + " face to " + bf.name() +
 				 * " face at " +
 				 * current.getRelative(bf2).getLocation().getBlockX() + ", " +
 				 * current.getRelative(bf2).getLocation().getBlockY() + ", " +
 				 * current.getRelative(bf2).getLocation().getBlockZ() + " is " +
 				 * current.getRelative(bf2).getType());
 				 */
 
 				if (current.getRelative(bf2).getType() == Material.LEVER || current.getRelative(bf2).getType() == Material.STONE_BUTTON
 						|| current.getRelative(bf2).getType() == Material.STONE_PLATE || current.getRelative(bf2).getType() == Material.WOOD_PLATE
						|| current.getRelative(bf2).getType() == Material.WOODEN_DOOR || current.getRelative(bf2).getType() == Material.TRAP_DOOR 
						) {
 					linkedButton = current.getRelative(bf2);
 					return;
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public boolean isUsedEvent(Event e) {
 		return e instanceof PlayerInteractEvent;
 	}
 
 	@Override
 	public void raiseEvent(Event ev) {
 		PlayerInteractEvent e = (PlayerInteractEvent) ev;
 		// Wait for the second event of a button
 
 		if (linkedButton != null) {
 
 			if (e.getClickedBlock().equals(linkedButton)) {
 				Player p = e.getPlayer();
 				
 				PlayerStats stats = plugin.getStats(p);
 				if (!getAllowedPlayer().contains(p) && !stats.subtractmoney(cost)) {
 					
 					Tribu.messagePlayer(p,plugin.getLocale("Message.YouDontHaveEnoughMoney"));
 					e.setCancelled(true);
 					if(linkedButton.getType() == Material.WOODEN_DOOR)
 					{
 						Door d=new Door(linkedButton.getData());
 						d.setOpen(!d.isOpen());
 						linkedButton.setData(d.getData());	
 					}
 				} else
 				{
 					getAllowedPlayer().add(p);
 					Tribu.messagePlayer(p,String.format(plugin.getLocale("Message.PurchaseSuccessfulMoney"), String.valueOf(stats.getMoney())));
 				}
 			}
 		}
 
 	}
 
 	public static LinkedList<Player> getAllowedPlayer() {
 		return allowedPlayer;
 	}
 
 	public static void setAllowedPlayer(LinkedList<Player> allowedPlayer) {
 		TollSign.allowedPlayer = allowedPlayer;
 	}
 
 }
