 package net.gerritk.vehiclebuilder.models;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import net.gerritk.vehiclebuilder.items.Bluelight;
 import net.gerritk.vehiclebuilder.items.Cabin;
 import net.gerritk.vehiclebuilder.items.Child;
 import net.gerritk.vehiclebuilder.items.Structure;
 import net.gerritk.vehiclebuilder.items.Template;
 
 public class VehicleModel extends Model {
 	private String name;
 	private Cabin cabin;
 	private Structure structure;
 	private Template template;
 	private ArrayList<Child> childs;
 	
 	public VehicleModel(CabinModel cabins, StructureModel structures, TemplateModel templates) {
 		cabin = cabins.getCabins().get(0);
 		structure = structures.getStructures().get(0);
 		template = templates.getTemplates().get(0);
 		childs = new ArrayList<Child>();
 	}
 	
 	public void save() {
 		// TODO implement new saveLogic
 	}
 	
 	public void load() {
 		// TODO implement new loadLogic
 	}
 	
 	public boolean export() {
 		File dir = new File("output");
 		
 		if(!dir.exists()) {
 			dir.mkdir();
 		}
 		
 		File off = new File(dir.getPath() + "/" + name + ".png");
 		try {
 			ImageIO.write(generateOutput(false), "png", off);
 		} catch(IOException ex) {
 			ex.printStackTrace();
 			return false;
 		}
 		
 		File on = new File(dir.getPath() + "/" + name + "_on.png");
 		try {
 			ImageIO.write(generateOutput(true), "png", on);
 		} catch(IOException ex) {
 			ex.printStackTrace();
 			off.delete();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public BufferedImage generateOutput(boolean bluelight) {
		BufferedImage out = generateTemplate();
		if(out != null) {
			BufferedImage outVehicle = generateVehicle(bluelight);
 			Graphics2D g = out.createGraphics();
 			
			int border = template.getBorder();
			int bottomBorder = border;
			if(template.getExtra() != null) {
				bottomBorder = template.getExtra().getHeight();
			}
			
			g.drawImage(outVehicle, border, out.getHeight() - bottomBorder - outVehicle.getHeight(), null);
 		} else {
 			out = generateVehicle(bluelight);
 		}
 		
 		return out;
 	}
 	
 	public BufferedImage generateVehicle(boolean bluelight) {
 		BufferedImage out;
 		Dimension d = getVehicleDimension();
 		
 		if(d.width > 0 && d.height > 0) {
 			out = new BufferedImage(d.width, d.height, BufferedImage.TRANSLUCENT);
 			Graphics2D g = out.createGraphics();
 			Point off = new Point(0, 0);
 			
 			drawChilds(g, d, off, true, bluelight);
 			drawCabin(g, d, off);
 			drawStructure(g, d, off);
 			drawChilds(g, d, off, false, bluelight);
 		} else {
 			out = null;
 		}
 		
 		return out;
 	}
 	
 	public BufferedImage generateTemplate() {
 		BufferedImage out;
 		Dimension d = getOutputDimension();
 		
 		if(d.width > 0 && d.height > 0) {
 			out = new BufferedImage(d.width, d.height, BufferedImage.TRANSLUCENT);
 			Graphics2D g = out.createGraphics();
 			
 			drawTemplate(g, d);
 		} else {
 			out = null;
 		}
 		
 		return out;
 	}
 	
 	public Dimension getVehicleDimension() {
 		Dimension d = new Dimension();
 		
 		if(cabin != null) {
 			d.width += cabin.getWidth();
 			d.height = cabin.getHeight();
 		}
 		
 		if(structure != null) {
 			d.width += structure.getWidth();
 			if(structure.getHeight() > d.height) {
 				d.height = structure.getHeight();
 			}
 		}
 		
 		if(childs != null) {
 			Point max = Child.getMaxPositions(childs);
 			Point min = Child.getMinPositions(childs);
 			
 			if(d.width < max.x) d.width += max.x - d.width;
 			
 			if(d.width < d.width - min.x) d.width -= min.x;
 			
 			if(d.height < d.height - min.y) d.height -= min.y;
 		}
 		
 		return d;
 	}
 	
 	public Dimension getOutputDimension() {
 		Dimension d = getVehicleDimension();
 		
 		if(template != null && template.getImage() != null) {
 			if(template.getHeight() > d.height) {
 				d.height = template.getHeight();
 			}
 			
 			if(template.getExtra() != null) {
 				d.height += template.getExtra().getHeight() - template.getBorder();
 			}
 		
 			d.width += template.getBorder() * 2;
 		}
 		
 		return d;
 	}
 	
 	public void sortChildUp(Child child) {
 		int sindex = childs.indexOf(child);
 		
 		if(sindex > 0) {
 			Child tmp = childs.get(sindex - 1);
 			
 			childs.set(sindex - 1, child);
 			childs.set(sindex, tmp);
 			setChanged();
 		}
 	}
 
 	public void sortChildDown(Child child) {
 		int sindex = childs.indexOf(child);
 		
 		if(sindex + 1 < childs.size()) {
 			Child tmp = childs.get(sindex + 1);
 			
 			childs.set(sindex + 1, child);
 			childs.set(sindex, tmp);
 			setChanged();
 		}
 	}
 	
 	/*
 	 * Private Methods
 	 */
 	private void drawTemplate(Graphics2D g, Dimension d) {
 		if(template != null && template.getImage() != null) {
 			g.drawImage(template.getImage(d.width), 0, 0, null);
 		}
 	}
 	
 	private void drawChilds(Graphics2D g, Dimension d, Point p, boolean behind, boolean bluelight) {
 		if(childs != null) {
 			// NEGATIV VALUES!!!
 			Point off = Child.getMinPositions(childs);
 			
 			for(Child child : childs) {
 				int yc = child.getY() - off.y;
 				int xc = child.getX() - off.x;
 				
 				if(child.isBehind() == behind) {
 					if(child instanceof Bluelight && bluelight) {
 						Bluelight bl = (Bluelight) child;
 						g.drawImage(bl.getOn(), xc, yc, null);
 					} else {
 						g.drawImage(child.getImage(), xc, yc, null);
 					}
 				}
 			}
 			p.x -= off.x;
 		}
 	}
 	
 	private void drawCabin(Graphics2D g, Dimension d, Point p) {
 		if(cabin != null) {
			g.drawImage(cabin.getImage(), p.x, (int) d.getHeight() - cabin.getHeight() - p.y, null);
 			p.x += cabin.getWidth();
 		}
 	}
 	
 	private void drawStructure(Graphics2D g, Dimension d, Point p) {
 		if(structure != null) {
 			g.drawImage(structure.getImage(), p.x, (int) d.getHeight() - structure.getHeight() - p.y, null);
 			p.x += structure.getWidth();
 		}
 	}
 	
 	/*
 	 * Getter & Setter
 	 */
 	public Cabin getCabin() {
 		return cabin;
 	}
 	
 	public void setCabin(Cabin cabin) {
 		this.cabin = cabin;
 		setChanged();
 	}
 	
 	public Structure getStructure() {
 		return structure;
 	}
 
 	public void setStructure(Structure structure) {
 		this.structure = structure;
 		setChanged();
 	}
 
 	public ArrayList<Child> getChilds() {
 		return childs;
 	}
 
 	public void setChilds(ArrayList<Child> childs) {
 		this.childs = childs;
 		setChanged();
 	}
 
 	public Template getTemplate() {
 		return template;
 	}
 
 	public void setTemplate(Template template) {
 		this.template = template;
		setChanged();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 }
