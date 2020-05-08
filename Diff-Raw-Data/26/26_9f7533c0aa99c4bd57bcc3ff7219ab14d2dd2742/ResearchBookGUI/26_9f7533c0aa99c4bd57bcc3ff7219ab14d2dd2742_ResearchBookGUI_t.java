 package mod.industrialscience.modules.research;
 
 
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.nbt.NBTTagString;
 import net.minecraft.util.StatCollector;
 @SideOnly(Side.CLIENT)
 public class ResearchBookGUI extends GuiScreen {
 	    /** The player editing the book */
 	    private final ItemStack itemstackBook;
 
 
 	    private int bookImageWidth = 192;
 	    private int bookImageHeight = 192;
 	    private int bookTotalPages = 1;
 	    private int currPage;
 	    private NBTTagList bookPages;
 	    private GuiButtonNextPage buttonNextPage;
 	    private GuiButtonNextPage buttonPreviousPage;
 		public ResearchBookGUI(EntityPlayer player, ItemStack istack) {
 	        this.itemstackBook = istack;
 	        NBTTagCompound nbttagcompound = istack.getTagCompound();
 	        this.bookPages = nbttagcompound.getTagList("pages");
 	        generateBookPages(istack);
 			
 		}
 	    private void generateBookPages(ItemStack istack) {
 			// TODO Auto-generated method stub
 			
 		}
 		/**
 	     * Called from the main game loop to update the screen.
 	     */
 	    public void updateScreen()
 	    {
 	        super.updateScreen();
 	    }
 
 	    /**
 	     * Adds the buttons (and other controls) to the screen in question.
 	     */
 	    @SuppressWarnings("unchecked")
 		public void initGui()
 	    {
 	        this.buttonList.clear();
 	        Keyboard.enableRepeatEvents(true);
 	        int i = (this.width - this.bookImageWidth) / 2;
 	        byte b0 = 2;
 	        this.buttonList.add(this.buttonNextPage = new GuiButtonNextPage(1, i + 120, b0 + 154, true));
 	        this.buttonList.add(this.buttonPreviousPage = new GuiButtonNextPage(2, i + 38, b0 + 154, false));
 	        this.updateButtons();
 	    }
 
 	    /**
 	     * Called when the screen is unloaded. Used to disable keyboard repeat events
 	     */
 	    public void onGuiClosed()
 	    {
 	        Keyboard.enableRepeatEvents(false);
 	    }
 
 	    private void updateButtons()
 	    {
 	        this.buttonNextPage.drawButton = this.currPage < this.bookTotalPages - 1;
 	        this.buttonPreviousPage.drawButton = this.currPage > 0;
 	    }
 
 //	    private void sendBookToServer(boolean par1)
 //	    {
 //	        if (this.bookIsUnsigned && this.bookModified)
 //	        {
 //	            if (this.bookPages != null)
 //	            {
 //	                while (this.bookPages.tagCount() > 1)
 //	                {
 //	                    NBTTagString nbttagstring = (NBTTagString)this.bookPages.tagAt(this.bookPages.tagCount() - 1);
 //
 //	                    if (nbttagstring.data != null && nbttagstring.data.length() != 0)
 //	                    {
 //	                        break;
 //	                    }
 //
 //	                    this.bookPages.removeTag(this.bookPages.tagCount() - 1);
 //	                }
 //
 //	                if (this.itemstackBook.hasTagCompound())
 //	                {
 //	                    NBTTagCompound nbttagcompound = this.itemstackBook.getTagCompound();
 //	                    nbttagcompound.setTag("pages", this.bookPages);
 //	                }
 //	                else
 //	                {
 //	                    this.itemstackBook.setTagInfo("pages", this.bookPages);
 //	                }
 //
 //	                String s = "MC|BEdit";
 //
 //	                if (par1)
 //	                {
 //	                    s = "MC|BSign";
 //	                    this.itemstackBook.setTagInfo("author", new NBTTagString("author", this.editingPlayer.username));
 //	                    this.itemstackBook.setTagInfo("title", new NBTTagString("title", this.bookTitle.trim()));
 //	                    this.itemstackBook.itemID = Item.writtenBook.itemID;
 //	                }
 //
 //	                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
 //	                DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
 //
 //	                try
 //	                {
 //	                    Packet.writeItemStack(this.itemstackBook, dataoutputstream);
 //	                    this.mc.getNetHandler().addToSendQueue(new Packet250CustomPayload(s, bytearrayoutputstream.toByteArray()));
 //	                }
 //	                catch (Exception exception)
 //	                {
 //	                    exception.printStackTrace();
 //	                }
 //	            }
 //	        }
 //	    }
 
 	    /**
 	     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
 	     */
 	    protected void actionPerformed(GuiButton par1GuiButton)
 	    {
 	        if (par1GuiButton.enabled){
 	     
 	            if (par1GuiButton.id == 1)
 	            {
 	                if (this.currPage < this.bookTotalPages - 1)
 	                {
 	                    ++this.currPage;
 	                }
 	      
 	            }
 	            else if (par1GuiButton.id == 2)
 	            {
 	                if (this.currPage > 0)
 	                {
 	                    --this.currPage;
 	                }
 	            }
 	            this.updateButtons();
 	        }
 	    }
 
 //	    private void addNewPage()
 //	    {
 //	        if (this.bookPages != null && this.bookPages.tagCount() < 50)
 //	        {
 //	            this.bookPages.appendTag(new NBTTagString("" + (this.bookTotalPages + 1), ""));
 //	            ++this.bookTotalPages;
 //	            this.bookModified = true;
 //	        }
 //	    }
 
 	    /**
 	     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
 	     */
 	    protected void keyTyped(char par1, int par2)
 	    {
 	        super.keyTyped(par1, par2);
 	    }
 	    /**
 	     * Draws the screen and all the components in it.
 	     */
 	    public void drawScreen(int par1, int par2, float par3)
 	    {
 	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        this.mc.renderEngine.bindTexture("/gui/book.png");
 	        int k = (this.width - this.bookImageWidth) / 2;
 	        byte b0 = 2;
 	        this.drawTexturedModalRect(k, b0, 0, 0, this.bookImageWidth, this.bookImageHeight);
 	        String s;
 	        String s1;
 	        int l;
 	        s = String.format(StatCollector.translateToLocal("book.pageIndicator"), new Object[] {Integer.valueOf(this.currPage + 1), Integer.valueOf(this.bookTotalPages)});
 	        s1 = "";
 	     	if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
 	            {
 	                NBTTagString nbttagstring = (NBTTagString)this.bookPages.tagAt(this.currPage);
 	                s1 = nbttagstring.toString();
 	            }
 	            l = this.fontRenderer.getStringWidth(s);
 	            this.fontRenderer.drawString(s, k - l + this.bookImageWidth - 44, b0 + 16, 0);
 	            this.fontRenderer.drawSplitString(s1, k + 36, b0 + 16 + 16, 116, 0);
 	            super.drawScreen(par1, par2, par3);
 	}
 
 
 
 }
