 /*
  * Copyright (C) 2012 TomyLobo
  *
  * This file is part of Routes.
  *
  * Routes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.tomylobo.abstraction.bukkit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import eu.tomylobo.abstraction.World;
 import eu.tomylobo.abstraction.block.BlockState;
 import eu.tomylobo.abstraction.block.Sign;
 import eu.tomylobo.abstraction.entity.Player;
 import eu.tomylobo.math.Vector;
 
 public class BukkitWorld implements World {
 	final org.bukkit.World backend;
 
 	BukkitWorld(org.bukkit.World backend) {
 		this.backend = backend;
 	}
 
 	@Override
 	public String getName() {
 		return backend.getName();
 	}
 
 	@Override
 	public List<Player> getPlayers() {
 		final List<org.bukkit.entity.Player> players = backend.getPlayers();
 		final List<Player> ret = new ArrayList<Player>(players.size());
 		for (org.bukkit.entity.Player player : players) {
 			ret.add(BukkitUtils.wrap(player));
 		}
 		return ret;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof BukkitWorld))
 			return false;
 
 		return backend.equals(BukkitUtils.unwrap((BukkitWorld) obj));
 	}
 
 	@Override
 	public int hashCode() {
 		return backend.hashCode();
 	}
 
 	@Override
 	public BlockState getBlockState(Vector position) {
 		final org.bukkit.block.Block block = backend.getBlockAt((int) position.getX(), (int) position.getY(), (int) position.getZ());
 
 		final int type = block.getTypeId();
 		final int data = block.getData();
 
 		switch (type) {
 		case 63: // SIGN_POST
 		case 68: // WALL_SIGN
			final String[] lines = ((Sign) block.getState()).getLines();
 			return new Sign(type, data, lines);
 
 		default:
 			return new BlockState(type, data);
 		}
 	}
 
 	@Override
 	public void setBlockState(Vector position, BlockState blockState) {
 		final org.bukkit.block.Block block = backend.getBlockAt((int) position.getX(), (int) position.getY(), (int) position.getZ());
 
 		block.setTypeIdAndData(blockState.getType(), (byte) blockState.getData(), true);
 	}
 
 	@Override
 	public int getBlockType(Vector position) {
 		return backend.getBlockTypeIdAt((int) position.getX(), (int) position.getY(), (int) position.getZ());
 	}
 
 	@Override
 	public int getBlockData(Vector position) {
 		return backend.getBlockAt((int) position.getX(), (int) position.getY(), (int) position.getZ()).getData();
 	}
 }
