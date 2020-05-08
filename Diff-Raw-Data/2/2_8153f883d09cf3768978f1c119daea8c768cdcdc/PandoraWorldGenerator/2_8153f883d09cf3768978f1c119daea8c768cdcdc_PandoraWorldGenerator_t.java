 /*
  * Copyright (c) 2012-2013 Sean Porter <glitchkey@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.pandora;
 
 //* IMPORTS: JDK/JRE
 	import java.util.ArrayList;
 	import java.util.HashMap;
 	import java.util.List;
 	import java.util.Map;
 	import java.util.Random;
 //* IMPORTS: BUKKIT
 	import org.bukkit.block.Block;
 	import org.bukkit.block.BlockState;
 	import org.bukkit.Bukkit;
 	import org.bukkit.Location;
 	import org.bukkit.Material;
 	import org.bukkit.plugin.Plugin;
 	import org.bukkit.World;
 //* IMPORTS: PANDORA
 	import org.pandora.datatypes.BlockValues;
 	import org.pandora.events.PandoraStructureGenerateEvent;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public abstract class PandoraWorldGenerator
 {
 	private Map<Location, Map<Block, BlockValues>> modifiedBlocks;
 	private List<BlockValues> replaceBlacklist = new ArrayList<BlockValues>();
 	private Map<Location, List<Block>> replaceWhitelist = new HashMap<Location, List<Block>>();
 
 	private final Plugin plugin;
 
 	private final boolean notifyOnBlockChanges;
 	private boolean invertBlacklist = false;
 
 	public PandoraWorldGenerator(Plugin plugin) {
 		this (plugin, false, false);
 	}
 
 	public PandoraWorldGenerator(Plugin plugin, boolean notifyOnBlockChanges) {
 		this (plugin, notifyOnBlockChanges, false);
 	}
 
 	public PandoraWorldGenerator(Plugin plugin, boolean notifyOnBlockChanges,
 		boolean invertBlacklist)
 	{
 		this.plugin = plugin;
 		this.notifyOnBlockChanges = notifyOnBlockChanges;
 		this.invertBlacklist = invertBlacklist;
 	}
 
 	public boolean addBlock(Block start, Block block, BlockValues values) {
 		if (block == null || values == null || start == null)
 			return false;
 
 		if (modifiedBlocks == null)
 			modifiedBlocks = new HashMap<Location, Map<Block, BlockValues>>();
 
 		Location loc = start.getLocation();
 
 		if (!modifiedBlocks.containsKey(loc))
 			modifiedBlocks.put(loc, new HashMap<Block, BlockValues>());
 
 		modifiedBlocks.get(loc).put(block, values);
 
 		if (isInBlacklist(block.getTypeId(), block.getData()))
 			return false;
 
 		return true;
 	}
 
 	public boolean addBlock(Block start, Block block, int id) {
 		return addBlock(start, block, id, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, Block block, Material material) {
 		return addBlock(start, block, material, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, Block block, String name) {
 		return addBlock(start, block, name, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, Location location, BlockValues values) {
 		if (location == null)
 			return false;
 
 		return addBlock(start, location.getBlock(), values);
 	}
 
 	public boolean addBlock(Block start, Location location, int id) {
 		return addBlock(start, location, id, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, Location location, Material material) {
 		return addBlock(start, location, material, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, Location location, String name) {
 		return addBlock(start, location, name, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, Block block, BlockValues values) {
 		if (start == null)
 			return false;
 
 		return addBlock(start.getBlock(), block, values);
 	}
 
 	public boolean addBlock(Location start, Block block, int id) {
 		if (start == null)
 			return false;
 
 		return addBlock(start.getBlock(), block, id, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, Block block, Material material) {
 		if (start == null)
 			return false;
 
 		return addBlock(start.getBlock(), block, material, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, Block block, String name) {
 		if (start == null)
 			return false;
 
 		return addBlock(start.getBlock(), block, name, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, Location location, BlockValues values) {
 		if (start == null || location == null)
 			return false;
 
 		return addBlock(start.getBlock(), location.getBlock(), values);
 	}
 
 	public boolean addBlock(Location start, Location location, int id) {
 		if (start == null)
 			return false;
 
 		return addBlock(start.getBlock(), location, id, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, Location location, Material material) {
 		if (start == null)
 			return false;
 
 		return addBlock(start.getBlock(), location, material, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, Location location, String name) {
 		if (start == null)
 			return false;
 
 		return addBlock(start.getBlock(), location, name, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, Block block, int id, byte data) {
 		try {
 			return addBlock(start, block, new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, Block block, int id, int data) {
 		return addBlock(start, block, id, (byte) data);
 	}
 
 	public boolean addBlock(Block start, Block block, Material material, byte data) {
 		try {
 			return addBlock(start, block, new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, Block block, Material material, int data) {
 		return addBlock(start, block, material, (byte) data);
 	}
 
 	public boolean addBlock(Block start, Block block, String name, byte data) {
 		try {
 			return addBlock(start, block, new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, Block block, String name, int data) {
 		return addBlock(start, block, name, (byte) data);
 	}
 
 	public boolean addBlock(Block start, Location location, int id, byte data) {
 		try {
 			return addBlock(start, location.getBlock(), new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, Location location, int id, int data) {
 		return addBlock(start, location, id, (byte) data);
 	}
 
 	public boolean addBlock(Block start, Location location, Material material, byte data) {
 		try {
 			return addBlock(start, location.getBlock(),
 				new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, Location location, Material material, int data) {
 		return addBlock(start, location, material, (byte) data);
 	}
 
 	public boolean addBlock(Block start, Location location, String name, byte data) {
 		try {
 			return addBlock(start, location.getBlock(), new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, Location location, String name, int data) {
 		return addBlock(start, location, name, (byte) data);
 	}
 
 	public boolean addBlock(Location start, Block block, int id, byte data) {
 		try {
 			return addBlock(start.getBlock(), block, new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, Block block, int id, int data) {
 		return addBlock(start, block, id, (byte) data);
 	}
 
 	public boolean addBlock(Location start, Block block, Material material, byte data) {
 		try {
 			return addBlock(start.getBlock(), block, new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, Block block, Material material, int data) {
 		return addBlock(start, block, material, (byte) data);
 	}
 
 	public boolean addBlock(Location start, Block block, String name, byte data) {
 		try {
 			return addBlock(start.getBlock(), block, new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, Block block, String name, int data) {
 		return addBlock(start, block, name, (byte) data);
 	}
 
 	public boolean addBlock(Location start, Location location, int id, byte data) {
 		try {
 			return addBlock(start.getBlock(), location.getBlock(),
 				new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, Location location, int id, int data) {
 		return addBlock(start, location, id, (byte) data);
 	}
 
 	public boolean addBlock(Location start, Location location, Material material, byte data) {
 		try {
 			return addBlock(start.getBlock(), location.getBlock(),
 				new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, Location location, Material material, int data) {
 		return addBlock(start, location, material, (byte) data);
 	}
 
 	public boolean addBlock(Location start, Location location, String name, byte data) {
 		try {
 			return addBlock(start.getBlock(), location.getBlock(),
 				new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, Location location, String name, int data) {
 		return addBlock(start, location, name, (byte) data);
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, BlockValues values) {
 		if (world == null || start == null || values == null)
 			return false;
 
 		return addBlock(start, world.getBlockAt(x, y, z), values);
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, int id) {
 		return addBlock(start, world, x, y, z, id, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, Material material) {
 		return addBlock(start, world, x, y, z, material, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, String name) {
 		return addBlock(start, world, x, y, z, name, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z,
 		BlockValues values)
 	{
 		if (world == null || start == null || values == null)
 			return false;
 
 		return addBlock(start.getBlock(), world.getBlockAt(x, y, z), values);
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, int id) {
 		if (world == null || start == null)
 			return false;
 
 		return addBlock(start.getBlock(), world, x, y, z, id, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z,
 		Material material)
 	{
 		if (world == null || start == null)
 			return false;
 
 		return addBlock(start.getBlock(), world, x, y, z, material, (byte) 0);
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, String name) {
 		if (world == null || start == null)
 			return false;
 
 		return addBlock(start.getBlock(), world, x, y, z, name, (byte) 0);
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, int id, byte data) {
 		try {
 			return addBlock(start, world.getBlockAt(x, y, z),
 				new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, int id, int data) {
 		return addBlock(start, world, x, y, z, id, (byte) data);
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, Material material,
 		byte data)
 	{
 		try {
 			return addBlock(start, world.getBlockAt(x, y, z),
 				new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, Material material,
 		int data)
 	{
 		return addBlock(start, world, x, y, z, material, (byte) data);
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, String name,
 		byte data)
 	{
 		try {
 			return addBlock(start, world.getBlockAt(x, y, z),
 				new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Block start, World world, int x, int y, int z, String name,
 		int data)
 	{
 		return addBlock(start, world, x, y, z, name, (byte) data);
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, int id,
 		byte data)
 	{
 		try {
 			return addBlock(start.getBlock(), world.getBlockAt(x, y, z),
 				new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, int id,
 		int data)
 	{
 		return addBlock(start, world, x, y, z, id, (byte) data);
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, Material material,
 		byte data)
 	{
 		try {
 			return addBlock(start.getBlock(), world.getBlockAt(x, y, z),
 				new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, Material material,
 		int data)
 	{
 		return addBlock(start, world, x, y, z, material, (byte) data);
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, String name,
 		byte data)
 	{
 		try {
 			return addBlock(start.getBlock(), world.getBlockAt(x, y, z),
 				new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(Location start, World world, int x, int y, int z, String name,
 		int data)
 	{
 		return addBlock(start, world, x, y, z, name, (byte) data);
 	}
 
 	public boolean addBlock(World world, int x1, int y1, int z1, int x2, int y2, int z2, int id,
 		byte data)
 	{
 		try {
 			return addBlock(world.getBlockAt(x1, y1, z1), world.getBlockAt(x2, y2, z2),
 				new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(World world, int x1, int y1, int z1, int x2, int y2, int z2, int id,
 		int data)
 	{
 		return addBlock(world, x1, y1, z1, x2, y2, z2, id, (byte) data);
 	}
 
 	public boolean addBlock(World world, int x1, int y1, int z1, int x2, int y2, int z2,
 		Material material, byte data)
 	{
 		try {
 			return addBlock(world.getBlockAt(x1, y1, z1), world.getBlockAt(x2, y2, z2),
 				new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(World world, int x1, int y1, int z1, int x2, int y2, int z2,
 		Material material, int data)
 	{
 		return addBlock(world, x1, y1, z1, x2, y2, z2, material, (byte) data);
 	}
 
 	public boolean addBlock(World world, int x1, int y1, int z1, int x2, int y2, int z2,
 		String name, byte data)
 	{
 		try {
 			return addBlock(world.getBlockAt(x1, y1, z1), world.getBlockAt(x2, y2, z2),
 				new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean addBlock(World world, int x1, int y1, int z1, int x2, int y2, int z2,
 		String name, int data)
 	{
 		return addBlock(world, x1, y1, z1, x2, y2, z2, name, (byte) data);
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Block block) {
 		if (block == null)
 			return this;
 
 		return addToBlacklist(block.getTypeId(), block.getData());
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Block blocks[]) {
 		for (Block block : blocks) {
 			if (block == null)
 				continue;
 
 			addToBlacklist(block.getTypeId(), block.getData());
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(BlockValues values) {
 		for (BlockValues listItem : replaceBlacklist) {
 			if (listItem.getId() != values.getId())
 				continue;
 
 			if (listItem.getData() != values.getData())
 				continue;
 
 			return this;
 		}
 
 		replaceBlacklist.add(values);
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(BlockValues blocks[]) {
 		for (BlockValues block : blocks) {
 			if (block == null)
 				continue;
 
 			addToBlacklist(block);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(int id) {
 		return addToBlacklist(id, (byte) 0);
 	}
 
 	public PandoraWorldGenerator addToBlacklist(int ids[]) {
 		for (int id : ids) {
 			addToBlacklist(id);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(List<Object> objects) {
 		for (Object object : objects) {
 			if (object instanceof Block)
 				addToBlacklist((Block) object);
 			else if (object instanceof BlockValues)
 				addToBlacklist((BlockValues) object);
 			else if (object instanceof Integer)
 				addToBlacklist((Integer) object);
 			else if (object instanceof Material)
 				addToBlacklist((Material) object);
 			else if (object instanceof String)
 				addToBlacklist((String) object);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Location location) {
 		if (location == null)
 			return this;
 
 		return addToBlacklist(location.getBlock());
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Location locations[]) {
 		for (Location location : locations) {
 			if (location == null)
 				continue;
 
 			addToBlacklist(location);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Material material) {
 		return addToBlacklist(material, (byte) 0);
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Material materials[]) {
 		for (Material material : materials) {
 			if (material == null)
 				continue;
 
 			addToBlacklist(material);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(String name) {
 		return addToBlacklist(name, (byte) 0);
 	}
 
 	public PandoraWorldGenerator addToBlacklist(String names[]) {
 		for (String name : names) {
 			if (name == null)
 				continue;
 
 			addToBlacklist(name);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToBlacklist(int id, byte data) {
 		try {
 			return addToBlacklist(new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator addToBlacklist(int id, int data) {
 		return addToBlacklist(id, (byte) data);
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Material material, byte data) {
 		try {
 			return addToBlacklist(new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator addToBlacklist(Material material, int data) {
 		return addToBlacklist(material, (byte) data);
 	}
 
 	public PandoraWorldGenerator addToBlacklist(String name, byte data) {
 		try {
 			return addToBlacklist(new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator addToBlacklist(String name, int data) {
 		return addToBlacklist(name, (byte) data);
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Block start, Block block) {
 		if (start == null || block == null)
 			return this;
 
 		Location loc = start.getLocation();
 
 		if (!replaceWhitelist.containsKey(loc))
 			replaceWhitelist.put(loc, new ArrayList<Block>());
 
 		replaceWhitelist.get(loc).add(block);
 		return this;
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Block start, Block blocks[]) {
 		if (start == null)
 			return this;
 
 		for (Block block : blocks) {
 			if (block == null)
 				continue;
 
 			addToWhitelist(start, block);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Block start, List<Object> objects) {
 		if (start == null)
 			return this;
 
 		for (Object object : objects) {
 			if (object instanceof Block)
 				addToWhitelist(start, (Block) object);
 			else if (object instanceof Location)
 				addToWhitelist(start, (Location) object);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Block start, Location location) {
 		if (location == null)
 			return this;
 
 		return addToWhitelist(start, location.getBlock());
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Block start, Location locations[]) {
 		if (start == null)
 			return this;
 
 		for (Location location : locations) {
 			if (location == null)
 				continue;
 
 			addToWhitelist(start, location);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Location start, Block block) {
 		if (start == null || block == null)
 			return this;
 
 		return addToWhitelist(start.getBlock(), block);
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Location start, Block blocks[]) {
 		if (start == null)
 			return this;
 
 		for (Block block : blocks) {
 			if (block == null)
 				continue;
 
 			addToWhitelist(start, block);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Location start, List<Object> objects) {
 		if (start == null)
 			return this;
 
 		for (Object object : objects) {
 			if (object instanceof Block)
 				addToWhitelist(start, (Block) object);
 			else if (object instanceof Location)
 				addToWhitelist(start, (Location) object);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Location start, Location location) {
 		if (location == null)
 			return this;
 
 		return addToWhitelist(start, location.getBlock());
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Location start, Location locations[]) {
 		if (start == null)
 			return this;
 
 		for (Location location : locations) {
 			if (location == null)
 				continue;
 
 			addToWhitelist(start, location);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Block start, World world, int x, int y, int z) {
 		try {
 			return addToWhitelist(start, world.getBlockAt(x, y, z));
 		}
 		catch (Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator addToWhitelist(Location start, World world, int x, int y,
 		int z)
 	{
 		try {
 			return addToWhitelist(start.getBlock(), world.getBlockAt(x, y, z));
 		}
 		catch (Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator addToWhitelist(World world, int x1, int y1, int z1, int x2,
 		int y2, int z2) {
 		try {
 			return addToWhitelist(world.getBlockAt(x1, y1, z1),
 				world.getBlockAt(x2, y2, z2));
 		}
 		catch (Exception e) {
 			return this;
 		}
 	}
 
 	protected abstract boolean generate(World world, Random random, int x, int y, int z);
 
 	public void invertBlacklist() {
 		invertBlacklist = (invertBlacklist ? false : true);
 	}
 
 	public boolean isInBlacklist(Block block) {
 		if (block == null)
 			return false;
 
 		return isInBlacklist(block.getTypeId(), block.getData());
 	}
 
 	public boolean isInBlacklist(BlockValues values) {
 		for (BlockValues listItem : replaceBlacklist) {
 			if (listItem.getId() != values.getId())
 				continue;
 
 			if (listItem.getData() != values.getData())
 				continue;
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean isInBlacklist(int id) {
 		return isInBlacklist(id, (byte) 0);
 	}
 
 	public boolean isInBlacklist(Location location) {
 		if (location == null)
 			return false;
 
 		return isInBlacklist(location.getBlock());
 	}
 
 	public boolean isInBlacklist(Material material) {
 		return isInBlacklist(material, (byte) 0);
 	}
 
 	public boolean isInBlacklist(String name) {
 		return isInBlacklist(name, (byte) 0);
 	}
 
 	public boolean isInBlacklist(int id, byte data) {
 		try {
 			return isInBlacklist(new BlockValues(id, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isInBlacklist(int id, int data) {
 		return isInBlacklist(id, (byte) data);
 	}
 
 	public boolean isInBlacklist(Material material, byte data) {
 		try {
 			return isInBlacklist(new BlockValues(material, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isInBlacklist(Material material, int data) {
 		return isInBlacklist(material, (byte) data);
 	}
 
 	public boolean isInBlacklist(String name, byte data) {
 		try {
 			return isInBlacklist(new BlockValues(name, data));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isInBlacklist(String name, int data) {
 		return isInBlacklist(name, (byte) data);
 	}
 
 	public boolean isInWhitelist(Block start, Block block) {
 		if (start == null || block == null)
 			return false;
 		else if (!replaceWhitelist.containsKey(start.getLocation()))
 			return false;
 
 		return replaceWhitelist.get(start.getLocation()).contains(block);
 	}
 
 	public boolean isInWhitelist(Block start, Location location) {
 		if (start == null || location == null)
 			return false;
 
 		return isInWhitelist(start, location.getBlock());
 	}
 
 	public boolean isInWhitelist(Location start, Block block) {
 		if (start == null || block == null)
 			return false;
 
 		return isInWhitelist(start.getBlock(), block);
 	}
 
 	public boolean isInWhitelist(Location start, Location location) {
 		if (start == null || location == null)
 			return false;
 
 		return isInWhitelist(start.getBlock(), location.getBlock());
 	}
 
 	public boolean isInWhitelist(Block start, World world, int x, int y, int z) {
 		try {
 			return isInWhitelist(start, world.getBlockAt(x, y, z));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isInWhitelist(Location start, World world, int x, int y, int z) {
 		try {
 			return isInWhitelist(start.getBlock(), world.getBlockAt(x, y, z));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isInWhitelist(World world, int x, int y, int z, Block block) {
 		try {
 			return isInWhitelist(world.getBlockAt(x, y, z), block);
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isInWhitelist(World world, int x, int y, int z, Location location) {
 		try {
 			return isInWhitelist(world.getBlockAt(x, y, z), location.getBlock());
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean isInWhitelist(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
 		try {
 			return isInWhitelist(world.getBlockAt(x1, y1, z1),
 				world.getBlockAt(x2, y2, z2));
 		}
 		catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean place(World world, Random random, int x, int y, int z) {
 		return generate(world, random, x, y, z);
 	}
 
 	public boolean placeBlocks(Block start) {
 		if (start == null)
 			return false;
 
 		return placeBlocks(start.getLocation(), false);
 	}
 
 	public boolean placeBlocks(Location start) {
 		return placeBlocks(start, false);
 	}
 
 	public boolean placeBlocks(Block start, boolean fastFail) {
 		if (start == null)
 			return false;
 
 		return placeBlocks(start.getLocation(), fastFail);
 	}
 
 	public boolean placeBlocks(Location start, boolean fastFail) {
		if (plugin == null || start == null || modifiedBlocks == null)
 			return false;
 		else if (!modifiedBlocks.containsKey(start))
 			return false;
 
 		Map<Block, BlockValues> modified = modifiedBlocks.get(start);
 
 		if(modified.isEmpty())
 			return true;
 
 		List<BlockState> blocks = new ArrayList<BlockState>();
 
 		for(Block block : modified.keySet()) {
 			if(block == null)
 				continue;
 
 			boolean blacklisted = isInBlacklist(block);
 
 			if (blacklisted)
 				blacklisted = isInWhitelist(start, block) ? invertBlacklist : true;
 
 			if(fastFail && blacklisted && !invertBlacklist) {
 				return false;
 			}
 			else if(fastFail && !blacklisted && invertBlacklist) {
 				return false;
 			}
 
 			BlockState state = block.getState();
 			state.setTypeId(modified.get(block).getId());
 			state.setRawData(modified.get(block).getData());
 			blocks.add(state);
 		}
 
 		PandoraStructureGenerateEvent event;
 		event = new PandoraStructureGenerateEvent(start, blocks, plugin);
 
 		Bukkit.getPluginManager().callEvent(event);
 
 		if(event.isCancelled())
 			return false;
 
 		for(Block block : modified.keySet()) {
 			if(block == null)
 				continue;
 
 			setBlock(block, modified.get(block));
 		}
 
 		replaceWhitelist.remove(start);
 		modifiedBlocks.remove(start);
 		return true;
 	}
 
 	public boolean placeBlocks(World world, int x, int y, int z) {
 		return placeBlocks(world, x, y, z, false);
 	}
 
 	public boolean placeBlocks(World world, int x, int y, int z, boolean fastFail) {
 		if (world == null)
 			return false;
 
 		return placeBlocks(new Location(world, x, y, z), fastFail);
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Block block) {
 		if(block == null)
 			return this;
 
 		return removeFromBlacklist(block.getTypeId(), block.getData());
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Block blocks[]) {
 		for(Block block : blocks) {
 			if(block == null)
 				continue;
 
 			removeFromBlacklist(block.getTypeId(), block.getData());
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(BlockValues values) {
 		for(BlockValues listItem : replaceBlacklist) {
 			if(listItem.getId() != values.getId())
 				continue;
 
 			if(listItem.getData() != values.getData())
 				continue;
 
 			return this;
 		}
 
 		replaceBlacklist.remove(values);
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(BlockValues blocks[]) {
 		for(BlockValues block : blocks) {
 			if(block == null)
 				continue;
 
 			removeFromBlacklist(block);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(int id) {
 		return removeFromBlacklist(id, (byte) 0);
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(int ids[]) {
 		for(int id : ids) {
 			removeFromBlacklist(id);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(List<Object> objects) {
 		for(Object object : objects) {
 			if(object instanceof Block)
 				removeFromBlacklist((Block) object);
 			else if(object instanceof BlockValues)
 				removeFromBlacklist((BlockValues) object);
 			else if(object instanceof Integer)
 				removeFromBlacklist((Integer) object);
 			else if(object instanceof Material)
 				removeFromBlacklist((Material) object);
 			else if(object instanceof String)
 				removeFromBlacklist((String) object);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Location location) {
 		if(location == null)
 			return this;
 
 		return removeFromBlacklist(location.getBlock());
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Location locations[]) {
 		for(Location location : locations) {
 			if(location == null)
 				continue;
 
 			removeFromBlacklist(location);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Material material) {
 		return removeFromBlacklist(material, (byte) 0);
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Material materials[]) {
 		for(Material material : materials) {
 			if(material == null)
 				continue;
 
 			removeFromBlacklist(material);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(String name) {
 		return removeFromBlacklist(name, (byte) 0);
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(String names[]) {
 		for(String name : names) {
 			if(name == null)
 				continue;
 
 			removeFromBlacklist(name);
 		}
 
 		return this;
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(int id, byte data) {
 		try {
 			return removeFromBlacklist(new BlockValues(id, data));
 		}
 		catch(Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(int id, int data) {
 		return removeFromBlacklist(id, (byte) data);
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Material material, byte data) {
 		try {
 			return removeFromBlacklist(new BlockValues(material, data));
 		}
 		catch(Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(Material material, int data) {
 		return removeFromBlacklist(material, (byte) data);
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(String name, byte data) {
 		try {
 			return removeFromBlacklist(new BlockValues(name, data));
 		}
 		catch(Exception e) {
 			return this;
 		}
 	}
 
 	public PandoraWorldGenerator removeFromBlacklist(String name, int data) {
 		return removeFromBlacklist(name, (byte) data);
 	}
 
 	private void setBlock(Block block, BlockValues values) {
 		setBlock(block, values.getId(), values.getData());
 	}
 
 	private void setBlock(Block block, int id) {
 		setBlock(block, id, (byte) 0);
 	}
 
 	private void setBlock(Block block, int id, byte data) {
 		block.setTypeIdAndData(id, data, notifyOnBlockChanges);
 	}
 
 	private void setBlock(Block block, int id, int data) {
 		setBlock(block, id, (byte) data);
 	}
 
 	public void setScale(double xScale, double yScale, double zScale) {}
 }
