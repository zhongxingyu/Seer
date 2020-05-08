 /*
  * This file is part of DropletPlayerData.
  *
  * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package org.spout.droplet.playerdata;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Level;
 
 import javax.sql.rowset.serial.SerialBlob;
 
 import org.spout.api.Spout;
 import org.spout.api.component.Component;
 import org.spout.api.datatable.ManagedHashMap;
 import org.spout.api.datatable.SerializableMap;
 import org.spout.api.entity.Player;
 import org.spout.api.entity.PlayerSnapshot;
 import org.spout.api.event.EventHandler;
 import org.spout.api.event.storage.PlayerLoadEvent;
 import org.spout.api.event.storage.PlayerSaveEvent;
 import org.spout.api.event.Listener;
 import org.spout.api.geo.discrete.Point;
 import org.spout.api.geo.discrete.Transform;
 import org.spout.api.math.Quaternion;
 import org.spout.api.math.Vector3;
import org.spout.api.plugin.CommonClassLoader;
 
 public class DataEventListener implements Listener {
 	private DropletPlayerData plugin;
 
 	public DataEventListener(DropletPlayerData instance) {
 		this.plugin = instance;
 		try {
 			Connection conn = plugin.getConnectionPool().getConnection();
 			conn.prepareStatement("CREATE TABLE IF NOT EXISTS player_data (" +
 					/* player name (Primary Key) */
 					"name varchar(255) NOT NULL," +
 					/* entity id, entity uuid (broken into 2 longs, least significant bits, and most significant bits */
 					"id int NOT NULL, uid_msb bigint NOT NULL, uid_lsb bigint NOT NULL," +
 					/* world uuid and world name */
 					"world_uid_msb bigint NOT NULL, world_uid_lsb bigint NOT NULL, world_name varchar(255) NOT NULL," +
 					/* entity position (represents Point class) */
 					"x real NOT NULL, y real NOT NULL, z real NOT NULL," +
 					/* entity rotation (represents Quaternion class) */
 					"qx real NOT NULL, qy real NOT NULL, qz real, qw real NOT NULL," +
 					/* entity scale (represents Vector3 class) */
 					"vx real NOT NULL, vy real NOT NULL, vz real NOT NULL," +
 					/* datamap, serialized into byte[] */
 					"datamap blob," +
 					/* view distance, observer, and snapshot time */
 					"view_distance int NOT NULL, observer int NOT NULL, snapshot_time bigint NOT NULL," +
 					/* components in one string, separated by ',' character */
 					"components clob)").execute();
 			conn.prepareStatement("CREATE INDEX IF NOT EXISTS IDX_NAME ON player_data(name)").execute();
 		} catch (SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, "Unable to create table", e);
 		}
 	}
 
 	@EventHandler
 	public void onPlayerSave(PlayerSaveEvent event) {
 		final PlayerSnapshot snapshot = event.getSnapshot();
 		Connection conn = null;
 		try {
 			conn = plugin.getConnectionPool().getConnection();
 			// Try to update the existing row
 			PreparedStatement st = setupUpdateStatement(conn, snapshot, false);
 			if (st.executeUpdate() == 0) {
 				// If no rows were updated, insert a new one
 				setupUpdateStatement(conn, snapshot, true).execute();
 			}
 			event.setSaved(true);
 		} catch (SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, "Unable to save player data for [" + snapshot.getName() + "] ", e);
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException ignore) { }
 			}
 		}
 	}
 
 	private PreparedStatement setupUpdateStatement(Connection conn, PlayerSnapshot snapshot, boolean insert) throws SQLException {
 		PreparedStatement statement;
 		if (insert) {
 			statement = conn.prepareStatement("INSERT INTO player_data (id, uid_msb, uid_lsb, world_uid_msb, world_uid_lsb, world_name," +
 					"x, y, z, qx, qy, qz, qw, vx, vy, vz, datamap, view_distance, observer, snapshot_time, components, name) " +
 					"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 		} else {
 			statement = conn.prepareStatement("UPDATE player_data SET id=?, uid_msb=?, uid_lsb=?, world_uid_msb=?," +
 					" world_uid_lsb=?, world_name=?, x=?, y=?, z=?, qx=?, qy=?, qz=?, qw=?, " +
 					"vx=?, vy=?, vz=?, datamap=?, view_distance=?, observer=?, snapshot_time=?, " +
 					"components=? WHERE name=?");
 		}
 
 		statement.setInt(1, snapshot.getId());
 		statement.setLong(2, snapshot.getUID().getMostSignificantBits());
 		statement.setLong(3, snapshot.getUID().getLeastSignificantBits());
 		statement.setLong(4, snapshot.getWorldUID().getMostSignificantBits());
 		statement.setLong(5, snapshot.getWorldUID().getLeastSignificantBits());
 		statement.setString(6, snapshot.getWorldName());
 		statement.setFloat(7, snapshot.getTransform().getPosition().getX());
 		statement.setFloat(8, snapshot.getTransform().getPosition().getY());
 		statement.setFloat(9, snapshot.getTransform().getPosition().getZ());
 		statement.setFloat(10, snapshot.getTransform().getRotation().getX());
 		statement.setFloat(11, snapshot.getTransform().getRotation().getY());
 		statement.setFloat(12, snapshot.getTransform().getRotation().getZ());
 		statement.setFloat(13, snapshot.getTransform().getRotation().getW());
 		statement.setFloat(14, snapshot.getTransform().getScale().getX());
 		statement.setFloat(15, snapshot.getTransform().getScale().getY());
 		statement.setFloat(16, snapshot.getTransform().getScale().getZ());
 		if (!snapshot.getDataMap().isEmpty()) {
 			statement.setBlob(17, new SerialBlob(snapshot.getDataMap().serialize()));
 		} else {
 			statement.setNull(17, Types.BLOB);
 		}
 		statement.setInt(18, snapshot.getViewDistance());
 		statement.setInt(19, snapshot.isObserver() ? 1 : 0);
 		statement.setLong(20, snapshot.getSnapshotTime());
 		if (snapshot.getComponents().size() > 0) {
 			StringBuilder builder = new StringBuilder();
 			for (Class<? extends Component> component : snapshot.getComponents()) {
 				builder.append(component.getName());
 				builder.append(",");
 			}
 			builder.deleteCharAt(builder.length() - 1);
 			statement.setClob(21, new StringReader(builder.toString()));
 		} else {
 			statement.setNull(21, Types.CLOB);
 		}
 		statement.setString(22, snapshot.getName());
 		return statement;
 	}
 
 	@EventHandler
 	public void onPlayerLoad(PlayerLoadEvent event) {
 		Connection conn = null;
 		try {
 			conn = plugin.getConnectionPool().getConnection();
 			PreparedStatement st = conn.prepareStatement("SELECT id, uid_msb, uid_lsb, world_uid_msb, world_uid_lsb, world_name," +
 				" x, y, z, qx, qy, qz, qw, vx, vy, vz, datamap, view_distance, observer, snapshot_time, components FROM player_data WHERE name = ?");
 			st.setString(1, event.getName());
 			ResultSet result = st.executeQuery();
 			if (result.next()) {
 				event.setSnapshot(new DatabasePlayerSnapshot(event.getName(), result));
 			}
 		} catch (SQLException e) {
 			plugin.getLogger().log(Level.SEVERE, "Unable to load player data for [" + event.getName() + "]", e);
 		} catch (IOException e) {
 			plugin.getLogger().log(Level.SEVERE, "Unable to load player data for [" + event.getName() + "]", e);
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException ignore) { }
 			}
 		}
 	}
 
 	private static class DatabasePlayerSnapshot implements PlayerSnapshot {
 		private final String name;
 		private final int id;
 		private final UUID uuid;
 		private final UUID worldUid;
 		private final String worldName;
 		private final Transform transform;
 		private final ManagedHashMap datamap = new ManagedHashMap();
 		private final int viewDistance;
 		private final boolean observer;
 		private final long snapshotTime;
 		private final List<Class<? extends Component>> types = new ArrayList<Class<? extends Component>>();
 		@SuppressWarnings("unchecked")
 		public DatabasePlayerSnapshot(String name, ResultSet record) throws SQLException, IOException {
 			this.name = name;
 			this.id = record.getInt(1);
 			this.uuid = new UUID(record.getLong(2), record.getLong(3));
 			this.worldUid = new UUID(record.getLong(4), record.getLong(5));
 			this.worldName = record.getString(6);
 			Point point = new Point(Spout.getEngine().getWorld(worldUid), record.getFloat(7), record.getFloat(8), record.getFloat(9));
 			Quaternion quat = new Quaternion(record.getFloat(10), record.getFloat(11), record.getFloat(12), record.getFloat(13), true);
 			Vector3 scale = new Vector3(record.getFloat(14), record.getFloat(15), record.getFloat(16));
 			this.transform = new Transform(point, quat, scale);
 			Blob blob = record.getBlob(17);
 			if (!record.wasNull()) {
 				datamap.deserialize(blob.getBytes(1, (int) blob.length()));
 			}
 			this.viewDistance = record.getInt(18);
 			this.observer = record.getInt(19) != 0;
 			this.snapshotTime = record.getLong(20);
 			Clob clob = record.getClob(21);
 			if (!record.wasNull()) {
 				String components = clob.getSubString(1, (int) clob.length());
 				for (String component : components.split(",")) {
 					try {
 						try {
							Class<? extends Component> clazz = (Class<? extends Component>) CommonClassLoader.findPluginClass(component);
 							types.add(clazz);
 						} catch (ClassNotFoundException e) {
 							Class<? extends Component> clazz = (Class<? extends Component>) Class.forName(component);
 							types.add(clazz);
 						}
 					} catch (ClassNotFoundException e) {
 						Spout.getLogger().log(Level.WARNING, "Unable to locate component class [" + component + "]");
 					}
 				}
 			}
 		}
 
 		public int getId() {
 			return id;
 		}
 
 		public UUID getUID() {
 			return uuid;
 		}
 
 		public Transform getTransform() {
 			return transform;
 		}
 
 		public UUID getWorldUID() {
 			return worldUid;
 		}
 
 		public String getWorldName() {
 			return worldName;
 		}
 
 		public SerializableMap getDataMap() {
 			return datamap;
 		}
 
 		public int getViewDistance() {
 			return viewDistance;
 		}
 
 		public boolean isObserver() {
 			return observer;
 		}
 
 		public boolean isSavable() {
 			return true;
 		}
 
 		public List<Class<? extends Component>> getComponents() {
 			return types;
 		}
 
 		public long getSnapshotTime() {
 			return snapshotTime;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public Player getReference() {
 			return null;
 		}
 	}
 }
