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
 
 package eu.tomylobo.abstraction.platform.spout;
 
 import org.spout.api.entity.Position;
 import org.spout.vanilla.entity.VanillaEntity;
 
 import eu.tomylobo.abstraction.entity.Entity;
 import eu.tomylobo.math.Location;
 import eu.tomylobo.math.Vector;
 import static eu.tomylobo.abstraction.platform.spout.SpoutUtils.*;
 
 public class SpoutEntity implements Entity {
 	final org.spout.api.entity.Entity backend;
 
 	public SpoutEntity(org.spout.api.entity.Entity entity) {
 		this.backend = entity;
 	}
 
 	@Override
 	public void teleport(Location location) {
 		teleport(location, true, true);
 	}
 
 	@Override
 	public void teleport(Location location, boolean withAngles, boolean notify) {
 		// TODO: spout
 		//if (withAngles) {
 			final Position position = unwrap(location);
 			backend.setPosition(position.getPosition());
 			backend.setPitch(position.getPitch());
 			backend.setYaw(position.getYaw());
 			backend.setRoll(position.getRoll());
 			// TODO: implement notify=false
 		/*}
 		else {
 			net.minecraft.server.Entity notchEntity = getCBHandle();
 
 			Vector position = location.getPosition();
 			final double x = position.getX();
 			final double y = position.getY();
 			final double z = position.getZ();
 
 			notchEntity.setPosition(x, y, z);
 		}
 	}
 
 	private net.minecraft.server.Entity getCBHandle() {
 		return ((org.bukkit.craftbukkit.entity.CraftEntity) backend).getHandle();*/
 	}
 
 	@Override
 	public void setVelocity(Vector velocity) {
 		// TODO: spout
 		final VanillaEntity vanillaEntity = (VanillaEntity) backend.getController();
		vanillaEntity.setVelocity(unwrap(velocity));
 	}
 
 	@Override
 	public Location getLocation() {
 		return wrap(backend.getPosition());
 	}
 
 	@Override
 	public int getEntityId() {
 		return backend.getId();
 	}
 
 	@Override
 	public void remove() {
 		backend.kill();
 	}
 
 	@Override
 	public Entity getPassenger() {
 		// TODO: spout
 		return null;//wrap(backend.getController().getPassenger());
 	}
 
 	@Override
 	public boolean setPassenger(Entity passenger) {
 		// TODO: spout
 		//unwrap(passenger).getController().attachToEntity(backend);
 		return true;
 	}
 
 	@Override
 	public boolean isDead() {
 		return backend.isDead();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof SpoutEntity))
 			return false;
 
 		return backend.equals(unwrap((SpoutEntity) obj));
 	}
 
 	@Override
 	public int hashCode() {
 		return backend.hashCode();
 	}
 
 	@Override
 	public Entity getVehicle() {
 		// TODO: spout
 		return null;
 	}
 }
