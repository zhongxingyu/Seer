 package ghost606.networkcraft.gui.elements.tab;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import ghost606.networkcraft.gui.GuiNetworkCraft;
 import ghost606.networkcraft.gui.elements.listview.ListView;
 import ghost606.networkcraft.resources.IconProvider;
 
 @SideOnly(Side.CLIENT)
 public class UserTab extends GuiTab {
 	private ListView list;
 	public UserTab(GuiNetworkCraft gui, int x, int y)
 	{
 		super(gui, x, y);
 		
 		this.list = new ListView(gui, x + 25, y + 5, 80, 73);
 		
 		backgroundColor = 564812;
 	}
 	
 	@Override
 	public void initGui() {
 		this.list.addElement("Test01");
 		this.list.addElement("Test02");
 		this.list.addElement("Test03");
 		this.list.addElement("Test04");
 		this.list.addElement("Test05");
 		
 		super.initGui();
 	}
 	
 	@Override
 	public void draw() {
 		super.draw();
		drawIcon(IconProvider.getInstance().getIcon(IconProvider.Access_Private), x + 3, y + 4);
 		if (this.isFullyOpened())
 		{
 			this.list.draw();
 		}
 	}
 	
 	@Override
 	public Boolean collide(int mouseX, int mouseY, int mouseButton) {
 		if (this.isFullyOpened())
 		{
 			if (this.list.collide(mouseX, mouseY, mouseButton))
 			{
 				return true;
 			}
 			else
 			{
 				return super.collide(mouseX, mouseY, mouseButton);
 			}
 		}
 		else
 		{
 			return super.collide(mouseX, mouseY, mouseButton);
 		}
 	}
 }
