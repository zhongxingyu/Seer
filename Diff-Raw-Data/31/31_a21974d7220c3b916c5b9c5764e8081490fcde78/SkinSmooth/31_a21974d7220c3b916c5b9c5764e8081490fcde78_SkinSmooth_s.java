 package com.comze_instancelabs.skins;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 
 public class SkinSmooth {
 
 	static Main main;
 
 	public static void smoothPartOfImageEast(Main m, Location t, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, String component) {
 		main = m;
 		if (component.equalsIgnoreCase("leg1_left")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() - 11, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY(), t.getBlockZ());
 			;
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i, end.getBlockY() - j + max_y, t.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("leg1_front")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY() - 12, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY(), t.getBlockZ() + 4);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("leg1_right")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() - 11, t.getBlockZ() + 3);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY(), t.getBlockZ() + 3);
 			;
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + min_x, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("leg1_behind")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() - 12, t.getBlockZ() + 3);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ() + 7);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() - i + min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("leg2_left")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() - 11, t.getBlockZ() + 4);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY(), t.getBlockZ() + 4);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i, end.getBlockY() - j + max_y, t.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("leg2_front")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY() - 12, t.getBlockZ() + 4);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY(), t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() - i + min_x + 3);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("leg2_right")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() - 11, t.getBlockZ() + 7);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY(), t.getBlockZ() + 7);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + min_x, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("leg2_behind")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() - 12, t.getBlockZ() + 4);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("body_left")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 1, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 12, t.getBlockZ());
 			;
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + min_x, end.getBlockY() - j + max_y, t.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("body_front")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY() + 1, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY() + 12, t.getBlockZ() + 4);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("body_right")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 1, t.getBlockZ() + 7);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 12, t.getBlockZ() + 7);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + min_x, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("body_behind")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 12, t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("arm1_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 13, t.getBlockZ() - 4);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 13, t.getBlockZ());
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x - 4, end.getBlockY(), start.getBlockZ() - j + max_y - 1);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 24, t.getBlockZ() - 4);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 24, t.getBlockZ());
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x - 4, end.getBlockY(), start.getBlockZ() - j + max_y - 1);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_left")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ() - 4);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 12, t.getBlockZ() - 4);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x - 4, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_front")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY(), t.getBlockZ() - 4);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY() + 12, t.getBlockZ());
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_right")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY(), t.getBlockZ() - 1);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 7, t.getBlockY() + 12, t.getBlockZ() - 1);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + i - min_x, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_behind")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ() - 1);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 12, t.getBlockZ() + 3);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() - i + min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("arm2_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 13, t.getBlockZ() + 11);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 13, t.getBlockZ() + 11);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x - 4, end.getBlockY(), start.getBlockZ() + j - max_y);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 24, t.getBlockZ() + 11);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 24, t.getBlockZ() + 11);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x - 4, end.getBlockY(), start.getBlockZ() + j - max_y);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_left")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ() + 11);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 12, t.getBlockZ() + 11);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x - 4, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_front")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY(), t.getBlockZ() + 8);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 2, t.getBlockY() + 12, t.getBlockZ() + 12);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() - i + min_x + 3);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_right")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ() + 8);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 1, t.getBlockY() + 12, t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + i - min_x - 3, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_behind")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY(), t.getBlockZ() + 8);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 5, t.getBlockY() + 12, t.getBlockZ() + 12);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("head_left")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 7, t.getBlockY(), t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() - 1, t.getBlockY() + 24, t.getBlockZ());
 			;
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i, end.getBlockY() - j + max_y, t.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_front")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 12, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 24, t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_right")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 7, t.getBlockY() + 12, t.getBlockZ() + 7);
 			Location end = new Location(t.getWorld(), t.getBlockX() - 1, t.getBlockY() + 24, t.getBlockZ() + 7);
 			;
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + i - min_x - 7, end.getBlockY() - j + max_y, start.getBlockZ());
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_behind")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 7, t.getBlockY(), t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 7, t.getBlockY() + 24, t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX(), end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 1, t.getBlockY() + 32, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 32, t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - j + max_y, end.getBlockY(), start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 1, t.getBlockY() + 25, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 25, t.getBlockZ() + 8);
 
 			int[] pixel;
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 					pixel = bi.getRaster().getPixel(i, j, new int[6]);
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + j - max_y + 9, end.getBlockY(), start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("hat_front")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 12, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 24, t.getBlockZ() + 8);
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					if (!isTransparent(bi, i, j)) {
 						Block change = t.getWorld().getBlockAt(start.getBlockX() - 1, end.getBlockY() - j + max_y, start.getBlockZ() + i - min_x);
 						
 						if (m.isHumanSkin(c)) {
 							change.setType(Material.WOOD);
 							change.setData((byte) 2);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public static void smoothPartOfImageWest(Main m, Location t, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, String component) {
 		main = m;
 		if (component.equalsIgnoreCase("arm1_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 6, t.getBlockY() + 13, t.getBlockZ() + 4);
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 13, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - j + max_y, end.getBlockY(), start.getBlockZ() - i + max_x - 4);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 6, t.getBlockY() + 24, t.getBlockZ() + 4);
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 24, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - j + max_y, end.getBlockY(), start.getBlockZ() - i + max_x - 4);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("arm2_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 1, t.getBlockY() + 13, t.getBlockZ() - 7);
 			Location end = new Location(t.getWorld(), t.getBlockX() - 11, t.getBlockY() + 13, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + j - max_y, end.getBlockY(), start.getBlockZ() + i - max_x);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 6, t.getBlockY() + 24, t.getBlockZ() - 7);
 			Location end = new Location(t.getWorld(), t.getBlockX() - 11, t.getBlockY() + 24, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x, end.getBlockY(), start.getBlockZ() + j - max_y);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("head_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 8, t.getBlockY() + 25, t.getBlockZ() - 7);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 25, t.getBlockZ() + 8);
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + j - max_y + 9, end.getBlockY(), start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 7, t.getBlockY() + 32, t.getBlockZ() - 8);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 32, t.getBlockZ() + 8);
 
 			// //getLogger().info(start.toString());
 			// //getLogger().info(end.toString());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + i - min_x, end.getBlockY(), start.getBlockZ() - j + max_y);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 	}
 
 	public static void smoothPartOfImageSouth(Main m, Location t, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, String component) {
 		main = m;
 		if (component.equalsIgnoreCase("arm1_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 13, t.getBlockZ() + 5);
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 13, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - j + max_y, end.getBlockY(), start.getBlockZ() - i + max_x - 4);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 24, t.getBlockZ() + 5);
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 24, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - j + max_y, end.getBlockY(), start.getBlockZ() - i + max_x - 4);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("arm2_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 7, t.getBlockY() + 13, t.getBlockZ() + 1);
 			Location end = new Location(t.getWorld(), t.getBlockX() - 11, t.getBlockY() + 13, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + j - max_y, end.getBlockY(), start.getBlockZ() - i + max_x);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 11, t.getBlockY() + 24, t.getBlockZ() + 6);
 			Location end = new Location(t.getWorld(), t.getBlockX() - 11, t.getBlockY() + 24, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x, end.getBlockY(), start.getBlockZ() + j - max_y);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("head_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 25, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 25, t.getBlockZ() + 8);
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + j - max_y + 1, end.getBlockY(), start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 32, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 32, t.getBlockZ() + 8);
 
 			// //getLogger().info(start.toString());
 			// //getLogger().info(end.toString());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + i - max_x + 1, end.getBlockY(), start.getBlockZ() - j + max_y - 1);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 	}
 
 	public static void smoothPartOfImageNorth(Main m, Location t, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, String component) {
 		main = m;
 		if (component.equalsIgnoreCase("arm1_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 7, t.getBlockY() + 13, t.getBlockZ() - 1);
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 13, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - j + max_y, end.getBlockY(), start.getBlockZ() + i - max_x);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm1_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() + 7, t.getBlockY() + 24, t.getBlockZ() - 1);
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 24, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - j + max_y, end.getBlockY(), start.getBlockZ() + i - max_x);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("arm2_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 13, t.getBlockZ() - 1);
 			Location end = new Location(t.getWorld(), t.getBlockX() - 11, t.getBlockY() + 13, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + j - max_y, end.getBlockY(), start.getBlockZ() + i - max_x);
					
					change.setType(Material.WOOL);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("arm2_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 24, t.getBlockZ() - 6);
 			Location end = new Location(t.getWorld(), t.getBlockX() - 11, t.getBlockY() + 24, t.getBlockZ());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + i - max_x, end.getBlockY(), start.getBlockZ() - j + max_y);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("head_bottom")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 25, t.getBlockZ());
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 25, t.getBlockZ() + 8);
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() + j - max_y + 1, end.getBlockY(), start.getBlockZ() + i - min_x);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		} else if (component.equalsIgnoreCase("head_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX() - 1, t.getBlockY() + 32, t.getBlockZ() + 1);
 			Location end = new Location(t.getWorld(), t.getBlockX() + 6, t.getBlockY() + 32, t.getBlockZ() + 8);
 
 			// //getLogger().info(start.toString());
 			// //getLogger().info(end.toString());
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					Block change = t.getWorld().getBlockAt(start.getBlockX() - i + max_x, end.getBlockY(), start.getBlockZ() + j - max_y);
 					
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 
 		if (component.equalsIgnoreCase("hat_top")) {
 			Location current = t;
 
 			Location start = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 32, t.getBlockZ() - 8);
 			Location end = new Location(t.getWorld(), t.getBlockX(), t.getBlockY() + 32, t.getBlockZ() + 8);
 
 			for (int i = min_x; i < max_x; i++) {
 				for (int j = min_y; j < max_y; j++) {
 
 					Color c = new Color(bi.getRGB(i, j));
 
 					if (!isTransparent(bi, i, j)) {
 						Block change = t.getWorld().getBlockAt(start.getBlockX() + i - min_x, end.getBlockY() + 1, start.getBlockZ() - j + max_y);
 						
 						if (m.isHumanSkin(c)) {
 							change.setType(Material.WOOD);
 							change.setData((byte) 2);
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	// WEST FRONT/BEHIND CUBOID
 	public static void smoothWestFrontHAT(Main m, Location p, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				if (!isTransparent(bi, min_x + i, min_y + j)) {
 					Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 					Block change = p.getWorld().getBlockAt(start.getBlockX(), start.getBlockY() + height - j, start.getBlockZ() - i);
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 	}
 
 	// WEST FRONT/BEHIND CUBOID
 	public static void smoothWestFront(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX(), start.getBlockY() + height - j, start.getBlockZ() - i);
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// WEST FRONT/BEHIND CUBOID INVERTED
 	public static void smoothWestFrontInvert(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX(), start.getBlockY() + height - j, start.getBlockZ() - width + i);
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// WEST RIGHT/LEFT CUBOID
 	public static void smoothWestSide(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX() - width + i, start.getBlockY() + height - j, start.getBlockZ());
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// WEST RIGHT/LEFT CUBOID
 	public static void smoothWestSideInvert(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX() + width - i, start.getBlockY() + height - j, start.getBlockZ());
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// SOUTH FRONT/BEHIND CUBOID HAT LAYERS
 	public static void smoothSouthFrontHAT(Main m, Location p, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				if (!isTransparent(bi, min_x + i, min_y + j)) {
 					Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 					Block change = p.getWorld().getBlockAt(start.getBlockX() - i, start.getBlockY() + height - j, start.getBlockZ());
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 	}
 
 	// SOUTH FRONT/BEHIND CUBOID
 	public static void smoothSouthFront(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX() - i, start.getBlockY() + height - j, start.getBlockZ());
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// SOUTH FRONT/BEHIND CUBOID INVERTED
 	public static void smoothSouthFrontInvert(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX() - width + i, start.getBlockY() + height - j, start.getBlockZ());
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// SOUTH RIGHT/LEFT CUBOID
 	public static void smoothSouthSide(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX(), start.getBlockY() + height - j, start.getBlockZ() + width - i);
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// SOUTH RIGHT/LEFT CUBOID
 	public static void smoothSouthSideInvert(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX(), start.getBlockY() + height - j, start.getBlockZ() - width + i);
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// NORTH FRONT/BEHIND CUBOID HAT LAYERS
 	public static void smoothNorthFrontHAT(Main m, Location p, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				if (!isTransparent(bi, min_x + i, min_y + j)) {
 					Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 					Block change = p.getWorld().getBlockAt(start.getBlockX() + i, start.getBlockY() + height - j, start.getBlockZ());
 					if (m.isHumanSkin(c)) {
 						change.setType(Material.WOOD);
 						change.setData((byte) 2);
 					}
 				}
 			}
 		}
 	}
 
 	// NORTH FRONT/BEHIND CUBOID
 	public static void smoothNorthFront(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX() + i, start.getBlockY() + height - j, start.getBlockZ());
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// NORTH FRONT/BEHIND CUBOID INVERTED
 	public static void smoothNorthFrontInvert(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX() + width - i, start.getBlockY() + height - j, start.getBlockZ());
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// NORTH RIGHT/LEFT CUBOID
 	public static void smoothNorthSide(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX(), start.getBlockY() + height - j, start.getBlockZ() - width + i);
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	// NORTH RIGHT/LEFT CUBOID
 	public static void smoothNorthSideInvert(Main m, World w, BufferedImage bi, int min_x, int max_x, int min_y, int max_y, Location start) {
 
 		int width = max_x - min_x;
 		int height = max_y - min_y;
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				Color c = new Color(bi.getRGB(min_x + i, min_y + j));
 				Block change = w.getBlockAt(start.getBlockX(), start.getBlockY() + height - j, start.getBlockZ() + width - i);
 				if (m.isHumanSkin(c)) {
 					change.setType(Material.WOOD);
 					change.setData((byte) 2);
 				}
 			}
 		}
 	}
 
 	public static boolean isTransparent(BufferedImage img, int x, int y) {
 		int pixel = img.getRGB(x, y);
 		if ((pixel >> 24) == 0x00) {
 			return true;
 		}
 		return false;
 	}
 }
