 /*
  * This file is part of VanillaClassic.
  *
  * Copyright (c) 2012 - 2013, Greatman <http://www.github.com/greatman/>
  * VanillaClassic is licensed under the SpoutDev License Version 1.
  *
  * VanillaClassic is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * In addition, 180 days after any changes are published, you can use the
  * software, incorporating those changes, under the terms of the MIT license,
  * as described in the SpoutDev License Version 1.
  *
  * VanillaClassic is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License,
  * the MIT license and the SpoutDev License Version 1 along with this program.
  * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
  * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
  * including the MIT license.
  */
 package com.greatmancode.vanillaclassic;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.util.List;
 import java.util.UUID;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 
 import org.apache.commons.lang3.text.WordUtils;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.spout.api.Server;
 import org.spout.api.geo.World;
 import org.spout.api.geo.discrete.Point;
 import org.spout.api.geo.discrete.Transform;
 import org.spout.api.math.Quaternion;
 import org.spout.api.math.Vector3;
 import org.spout.api.plugin.CommonPlugin;
import org.spout.api.plugin.Platform;
 import org.spout.api.protocol.PortBinding;
 import org.spout.api.protocol.Protocol;
 
 import com.greatmancode.vanillaclassic.configuration.VanillaClassicConfiguration;
 import com.greatmancode.vanillaclassic.configuration.WorldConfigurationNode;
 import com.greatmancode.vanillaclassic.protocol.VanillaClassicProtocol;
 import com.greatmancode.vanillaclassic.world.generator.VanillaClassicGenerator;
 import com.greatmancode.vanillaclassic.world.generator.VanillaClassicGenerators;
 
 public class VanillaClassicPlugin extends CommonPlugin {
 	private static VanillaClassicPlugin instance;
 	private VanillaClassicConfiguration config;
 	private VanillaClassicProtocol protocol;
 	private InetSocketAddress address;
 	private ScheduledFuture<?> heartbeat;
 	public static final byte PROTOCOL_VERSION = 0x07;
 	public static final String SALT = UUID.randomUUID().toString().replace("-", "").substring(0, 15); // TODO: Need to random that
 
 	@Override
 	public void onEnable() {
 		config.load();
 
 		// Commands
 
 		if (getEngine().debugMode() || getEngine().getPlatform() == Platform.SERVER) {
 			setupWorlds();
 		}
 		getEngine().getEventManager().registerEvents(new VanillaClassicListener(), this);
 		if (getEngine().getPlatform() == Platform.SERVER) {
 			//Find what is our IP/Port
 			List<PortBinding> ipList = ((Server)getEngine()).getBoundAddresses();
 			for (PortBinding port : ipList) {
 				if (port.getProtocol().equals(protocol))
 				{
 					address = (InetSocketAddress) port.getAddress();
 					break;
 				}
 			}
 			final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 			heartbeat = scheduler.scheduleWithFixedDelay(new Runnable() {
 
 				@Override
 				public void run() {
 					URIBuilder builder = new URIBuilder();
 					builder.setScheme("http").setHost("www.minecraft.net").setPath("/heartbeat.jsp");
 					builder.setParameter("port", String.valueOf(address.getPort())).setParameter("max", String.valueOf(((Server)getEngine()).getMaxPlayers())).setParameter("name", VanillaClassicConfiguration.SERVER_NAME.getString());
 					builder.setParameter("public", WordUtils.capitalize(Boolean.toString(VanillaClassicConfiguration.PUBLIC.getBoolean())));
 					builder.setParameter("version", Integer.toString(PROTOCOL_VERSION));
 					builder.setParameter("salt", SALT);
 					builder.setParameter("users", String.valueOf(((Server)getEngine()).getOnlinePlayers().length));
 					HttpGet get;
 					try {
 						get = new HttpGet(builder.build());
 						String reply = new BufferedReader(new InputStreamReader(new DefaultHttpClient().execute(get).getEntity().getContent())).readLine();
 						getLogger().info("Connect to the server using this URL: " + reply);
 					} catch (Exception e) {
 						getLogger().log(Level.WARNING, "Error while sending a heartbeat to minecraft.net!", e);
 					}
 				}
 			}, 0, 45, TimeUnit.SECONDS);
 		}
 
 		getLogger().info("v" + getDescription().getVersion() + " enabled. Protocol: " + getDescription().getData("protocol"));
 	}
 
 	private void setupWorlds() {
 		for (WorldConfigurationNode worldNode : VanillaClassicConfiguration.WORLDS.getAll()) {
 			if (worldNode.LOAD.getBoolean()) {
 				String generatorName = worldNode.GENERATOR.getString();
 				VanillaClassicGenerator generator = VanillaClassicGenerators.byName(generatorName);
 				if (generator == null) {
 					throw new IllegalArgumentException("Invalid generator name for world '" + worldNode.getWorldName() + "': " + generatorName);
 				}
 				World world = getEngine().loadWorld(worldNode.getWorldName(), generator);
 
 				if (world.getAge() <= 0) {
 					world.setSpawnPoint(new Transform(new Point(generator.getSafeSpawn(world)), Quaternion.IDENTITY, Vector3.ONE));
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void onDisable() {
 		heartbeat.cancel(true);
 		
 		getLogger().info("Stopped!");
 
 	}
 
 	@Override
 	public void onLoad() {
 		instance = this;
 		config = new VanillaClassicConfiguration(getDataFolder());
 		protocol = new VanillaClassicProtocol();
 		Protocol.registerProtocol(protocol);
 	}
 
 	public static VanillaClassicPlugin getInstance() {
 		return instance;
 	}
 
 	public VanillaClassicConfiguration getConfig() {
 		return config;
 	}
 }
