 package com.ubempire.not.a.portal;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
 public class CheckBlock {
 	private Block block;
 	private int typeId;
     private Pinapp p;
 
 	public CheckBlock(Pinapp p, Block block) {
         this.p = p;
 		this.block = block;
 		this.typeId = block.getTypeId();
 	}
 
 	private boolean portalType(Block block) {
 		if (block.getTypeId() == typeId)
 			return true;
 		return false;
 	}
 
 	public static void createPortal(Block block, int typeId, int orientation) {
 
 		if (orientation == 0) {
 			for (int x = -2; x <= 2; x++) {
 					for (int i = 0; i <= 4; i++) {
 						Block a = block.getRelative(x, i, 0);
 						Block b = block.getRelative(x, i, 1);
 						Block c = block.getRelative(x, i, -1);
 						Block d = block.getRelative(x, i, 2);
 						int id = 0;
 						if (i == 0)
 							id = 1;
 						if(i > 0 || a.getTypeId() == 0)
 						a.setTypeId(id);
 						if(i > 0 || b.getTypeId() == 0)
 						b.setTypeId(id);
 						if(i > 0 || c.getTypeId() == 0)
 						c.setTypeId(id);
 						if(i > 0 || d.getTypeId() == 0)
 						d.setTypeId(id);
 					}
 			}
 			for (int i = 0; i <= 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(0, i, 1);
 				Block c = block.getRelative(0, i, -1);
 				Block d = block.getRelative(0, i, 2);
 				a.setTypeId(typeId);
 				b.setTypeId(typeId);
 				c.setTypeId(typeId);
 				d.setTypeId(typeId);
 			}
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(0, i, 1);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 		} else if (orientation == 1) {
 			for (int x = -2; x <= 2; x++) {
 				for (int i = 0; i <= 4; i++) {
 					Block a = block.getRelative(x, i, 0);
 					Block b = block.getRelative(x, i, -1);
 					Block c = block.getRelative(x, i, 1);
 					Block d = block.getRelative(x, i, -2);
 					int id = 0;
 					if (i == 0)
 						id = 1;
 					if(i > 0 || a.getTypeId() == 0)
 						a.setTypeId(id);
 						if(i > 0 || b.getTypeId() == 0)
 						b.setTypeId(id);
 						if(i > 0 || c.getTypeId() == 0)
 						c.setTypeId(id);
 						if(i > 0 || d.getTypeId() == 0)
 						d.setTypeId(id);
 				}
 		}
 			for (int i = 0; i <= 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(0, i, -1);
 				Block c = block.getRelative(0, i, 1);
 				Block d = block.getRelative(0, i, -2);
 				a.setTypeId(typeId);
 				b.setTypeId(typeId);
 				c.setTypeId(typeId);
 				d.setTypeId(typeId);
 			}
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(0, i, -1);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 
 		} else if (orientation == 2) {
 			for (int x = -2; x <= 2; x++) {
 				for (int i = 0; i <= 4; i++) {
 					Block a = block.getRelative(0, i, x);
 					Block b = block.getRelative(1, i, x);
 					Block c = block.getRelative(-1, i, x);
 					Block d = block.getRelative(2, i, x);
 					int id = 0;
 					if (i == 0)
 						id = 1;
 					if(i > 0 || a.getTypeId() == 0)
 						a.setTypeId(id);
 						if(i > 0 || b.getTypeId() == 0)
 						b.setTypeId(id);
 						if(i > 0 || c.getTypeId() == 0)
 						c.setTypeId(id);
 						if(i > 0 || d.getTypeId() == 0)
 						d.setTypeId(id);
 				}
 		}
 			for (int i = 0; i <= 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(1, i, 0);
 				Block c = block.getRelative(-1, i, 0);
 				Block d = block.getRelative(2, i, 0);
 				a.setTypeId(typeId);
 				b.setTypeId(typeId);
 				c.setTypeId(typeId);
 				d.setTypeId(typeId);
 			}
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(1, i, 0);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 		} else if (orientation == 3) {
 			for (int x = -2; x <= 2; x++) {
 				for (int i = 0; i <= 4; i++) {
 					Block a = block.getRelative(0, i, x);
 					Block b = block.getRelative(-1, i, x);
 					Block c = block.getRelative(1, i, x);
 					Block d = block.getRelative(-2, i, x);
 					int id = 0;
 					if (i == 0)
 						id = 1;
 					if(i > 0 || a.getTypeId() == 0)
 						a.setTypeId(id);
 						if(i > 0 || b.getTypeId() == 0)
 						b.setTypeId(id);
 						if(i > 0 || c.getTypeId() == 0)
 						c.setTypeId(id);
 						if(i > 0 || d.getTypeId() == 0)
 						d.setTypeId(id);
 				}
 		}
 			for (int i = 0; i <= 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(-1, i, 0);
 				Block c = block.getRelative(1, i, 0);
 				Block d = block.getRelative(-2, i, 0);
 				a.setTypeId(typeId);
 				b.setTypeId(typeId);
 				c.setTypeId(typeId);
 				d.setTypeId(typeId);
 			}
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(-1, i, 0);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 		}
 
 	}
 
 	public void createPortal(int orientation) {
 		if (orientation == 0) {
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(0, i, 1);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 		} else if (orientation == 1) {
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(0, i, -1);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 
 		} else if (orientation == 2) {
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(1, i, 0);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 		} else if (orientation == 3) {
 			for (int i = 1; i < 4; i++) {
 				Block a = block.getRelative(0, i, 0);
 				Block b = block.getRelative(-1, i, 0);
 				a.setType(Material.PORTAL);
 				b.setType(Material.PORTAL);
 			}
 		}
 	}
 
 	public int check() {
		 if (p.pc.getId(block.getWorld().getName()) == block.getTypeId() || !Pinapp.portalTypes.contains(block.getTypeId()))
 		 return -1;
 		if (
 		// Base check Z1
 		portalType(block.getRelative(0, 0, 0))
 				&& portalType(block.getRelative(0, 0, 1))
 				&&
 				// Frame check Z1
 				portalType(block.getRelative(0, 1, 2))
 				&& portalType(block.getRelative(0, 2, 2))
 				&& portalType(block.getRelative(0, 3, 2))
 				&&
 				// Frame check Z1
 				portalType(block.getRelative(0, 1, -1))
 				&& portalType(block.getRelative(0, 2, -1))
 				&& portalType(block.getRelative(0, 3, -1))
 				&&
 				// Frame top Z1
 				portalType(block.getRelative(0, 4, 0))
 				&& portalType(block.getRelative(0, 4, 1)))
 			return 0;
 		else if (
 		// Base check Z2
 		portalType(block.getRelative(0, 0, 0))
 				&& portalType(block.getRelative(0, 0, -1))
 				&&
 				// Frame check Z2
 				portalType(block.getRelative(0, 1, 1))
 				&& portalType(block.getRelative(0, 2, 1))
 				&& portalType(block.getRelative(0, 3, 1))
 				&&
 				// Frame check Z2
 				portalType(block.getRelative(0, 1, -2))
 				&& portalType(block.getRelative(0, 2, -2))
 				&& portalType(block.getRelative(0, 3, -2))
 				&&
 				// Frame top Z2
 				portalType(block.getRelative(0, 4, 0))
 				&& portalType(block.getRelative(0, 4, -1)))
 			return 1;
 		else if (
 		// Base check X1
 		portalType(block.getRelative(0, 0, 0))
 				&& portalType(block.getRelative(1, 0, 0))
 				&&
 				// Frame check X1
 				portalType(block.getRelative(2, 1, 0))
 				&& portalType(block.getRelative(2, 2, 0))
 				&& portalType(block.getRelative(2, 3, 0))
 				&&
 				// Frame check X1
 				portalType(block.getRelative(-1, 1, 0))
 				&& portalType(block.getRelative(-1, 2, 0))
 				&& portalType(block.getRelative(-1, 3, 0))
 				&&
 				// Frame top X1
 				portalType(block.getRelative(0, 4, 0))
 				&& portalType(block.getRelative(1, 4, 0)))
 			return 2;
 		else if (
 		// Base check X2
 		portalType(block.getRelative(0, 0, 0))
 				&& portalType(block.getRelative(-1, 0, 0))
 				&&
 				// Frame check X2
 				portalType(block.getRelative(1, 1, 0))
 				&& portalType(block.getRelative(1, 2, 0))
 				&& portalType(block.getRelative(1, 3, 0))
 				&&
 				// Frame check X2
 				portalType(block.getRelative(-2, 1, 0))
 				&& portalType(block.getRelative(-2, 2, 0))
 				&& portalType(block.getRelative(-2, 3, 0))
 				&&
 				// Frame top X2
 				portalType(block.getRelative(0, 4, 0))
 				&& portalType(block.getRelative(-1, 4, 0)))
 			return 3;
 		else
 			return -1;
 	}
 }
