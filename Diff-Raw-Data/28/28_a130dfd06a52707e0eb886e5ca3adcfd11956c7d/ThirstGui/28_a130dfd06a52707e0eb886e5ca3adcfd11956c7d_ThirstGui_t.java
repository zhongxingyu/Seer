 package mmm.esm.thirst.gui;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import mmm.esm.thirst.ThirstStats;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.potion.Potion;
 import net.minecraft.profiler.Profiler;
 
 public class ThirstGui {
 
 	private ThirstIconCollection iconcon;
 	private int iconscount = 9;
 	
 	public ThirstGui(ThirstIconCollection iconcon)
 	{
 		this.iconcon = iconcon;
 	}
 	
 	public void Draw(ThirstStats thirstmodel, Gui gui, int x, int y)
 	{
 		float percentage = thirstmodel.thirstLevelPercentage();
 		int filledcount =(int)Math.round(percentage*((float)iconscount));
 		
		int unfilled = iconscount - filledcount;
		
 		for(int i = 1; i <= filledcount;i++)
 		{
 			//drawicon(gui, x-i*iconcon.getIconWidth(), y, iconcon.getIcon(ThirstIcons.WaterDropShadow));
 			drawicon(gui, x-i*iconcon.getIconWidth(), y, iconcon.getIcon(ThirstIcons.WaterDrop));
 			
 		}
		for(int i = filledcount+1; i<= iconscount; i++)
		{
			drawicon(gui, x-i*iconcon.getIconWidth(), y, iconcon.getIcon(ThirstIcons.WaterDropEmpty));
		}
 
 	}
 	
 	private void drawicon(Gui gui,int x, int y, Rectangle icon)
 	{
 		gui.drawTexturedModalRect(x, y, icon.x, icon.y, icon.width, icon.height);
 	}
 }
