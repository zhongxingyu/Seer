 package com.voidzm.novamenu.gui;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.gui.Gui;
 import net.minecraft.client.gui.GuiScreen;
 import net.minecraft.client.multiplayer.LanServer;
 import net.minecraft.client.multiplayer.ServerData;
 import net.minecraft.client.renderer.Tessellator;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.StatCollector;
 
 import org.lwjgl.opengl.GL11;
 
 import com.voidzm.novamenu.thread.ThreadNovamenuPollServers;
 
 public class GuiNovamenuServerSlot extends GuiNovamenuSlot {
 
 	private final GuiNovamenuMultiplayer parent;
 
 	public GuiNovamenuServerSlot(GuiNovamenuMultiplayer screen) {
 		super(screen.getMinecraft(), screen.width, screen.height, 32, screen.height - 64, 36);
 		this.parent = screen;
 	}
 
 	@Override
 	protected int getSize() {
 		return GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers() + GuiNovamenuMultiplayer.getListOfLanServers(this.parent).size() + 1;
 	}
 
 	@Override
 	protected void elementClicked(int par1, boolean par2) {
 		if(par1 < GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers() + GuiNovamenuMultiplayer.getListOfLanServers(this.parent).size()) {
 			int j = GuiNovamenuMultiplayer.getSelectedServer(this.parent);
 			GuiNovamenuMultiplayer.getAndSetSelectedServer(this.parent, par1);
 			ServerData serverdata = GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers() > par1 ? GuiNovamenuMultiplayer.getInternetServerList(this.parent).getServerData(par1) : null;
			boolean flag1 = GuiNovamenuMultiplayer.getSelectedServer(this.parent) >= 0 && GuiNovamenuMultiplayer.getSelectedServer(this.parent) < this.getSize() && (serverdata == null || serverdata.field_82821_f == 78);
 			boolean flag2 = GuiNovamenuMultiplayer.getSelectedServer(this.parent) < GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers();
 			GuiNovamenuMultiplayer.getButtonSelect(this.parent).enabled = flag1;
 			GuiNovamenuMultiplayer.getButtonEdit(this.parent).enabled = flag2;
 			GuiNovamenuMultiplayer.getButtonDelete(this.parent).enabled = flag2;
 			if(par2 && flag1) {
 				GuiNovamenuMultiplayer.func_74008_b(this.parent, par1);
 			}
 			else if(flag2 && GuiScreen.isShiftKeyDown() && j >= 0 && j < GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers()) {
 				GuiNovamenuMultiplayer.getInternetServerList(this.parent).swapServers(j, GuiNovamenuMultiplayer.getSelectedServer(this.parent));
 			}
 		}
 	}
 
 	@Override
 	protected boolean isSelected(int i) {
 		return i == GuiNovamenuMultiplayer.getSelectedServer(this.parent);
 	}
 
 	@Override
 	protected int getContentHeight() {
 		return this.getSize() * 36;
 	}
 
 	@Override
 	protected void drawSlot(int par1, int par2, int par3, int par4, Tessellator par5Tessellator) {
 		if(par1 < GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers()) {
 			this.func_77247_d(par1, par2, par3, par4, par5Tessellator);
 		}
 		else if(par1 < GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers() + GuiNovamenuMultiplayer.getListOfLanServers(this.parent).size()) {
 			this.func_77248_b(par1, par2, par3, par4, par5Tessellator);
 		}
 		else {
 			this.func_77249_c(par1, par2, par3, par4, par5Tessellator);
 		}
 	}
 
 	private void func_77248_b(int par1, int par2, int par3, int par4, Tessellator par5Tessellator) {
 		LanServer lanserver = (LanServer)GuiNovamenuMultiplayer.getListOfLanServers(this.parent).get(par1 - GuiNovamenuMultiplayer.getInternetServerList(this.parent).countServers());
 		this.parent.drawString(this.parent.getFontRenderer(), StatCollector.translateToLocal("lanServer.title"), par2 + 2, par3 + 1, 16777215);
 		this.parent.drawString(this.parent.getFontRenderer(), lanserver.getServerMotd(), par2 + 2, par3 + 12, 8421504);
 		if(this.parent.getMinecraft().gameSettings.hideServerAddress) {
 			this.parent.drawString(this.parent.getFontRenderer(), StatCollector.translateToLocal("selectServer.hiddenAddress"), par2 + 2, par3 + 12 + 11, 3158064);
 		}
 		else {
 			this.parent.drawString(this.parent.getFontRenderer(), lanserver.getServerIpPort(), par2 + 2, par3 + 12 + 11, 3158064);
 		}
 	}
 
 	private void func_77249_c(int par1, int par2, int par3, int par4, Tessellator par5Tessellator) {
 		this.parent.drawCenteredString(this.parent.getFontRenderer(), StatCollector.translateToLocal("lanServer.scanning"), this.parent.width / 2, par3 + 1, 16777215);
 		String s;
 		switch(GuiNovamenuMultiplayer.getTicksOpened(this.parent) / 3 % 4) {
 			case 0:
 			default:
 				s = "O o o";
 				break;
 			case 1:
 			case 3:
 				s = "o O o";
 				break;
 			case 2:
 				s = "o o O";
 		}
 		this.parent.drawCenteredString(this.parent.getFontRenderer(), s, this.parent.width / 2, par3 + 12, 8421504);
 	}
 
 	private void func_77247_d(int par1, int par2, int par3, int par4, Tessellator par5Tessellator) {
 		ServerData serverdata = GuiNovamenuMultiplayer.getInternetServerList(this.parent).getServerData(par1);
 		synchronized(GuiNovamenuMultiplayer.getLock()) {
 			if(GuiNovamenuMultiplayer.getThreadsPending() < 5 && !serverdata.field_78841_f) {
 				serverdata.field_78841_f = true;
 				serverdata.pingToServer = -2L;
 				serverdata.serverMOTD = "";
 				serverdata.populationInfo = "";
 				GuiNovamenuMultiplayer.increaseThreadsPending();
 				(new ThreadNovamenuPollServers(this, serverdata)).start();
 			}
 		}
		boolean flag = serverdata.field_82821_f > 78;
		boolean flag1 = serverdata.field_82821_f < 78;
 		boolean flag2 = flag || flag1;
 		this.parent.drawString(this.parent.getFontRenderer(), serverdata.serverName, par2 + 2, par3 + 1, 16777215);
 		this.parent.drawString(this.parent.getFontRenderer(), serverdata.serverMOTD, par2 + 2, par3 + 12, 8421504);
 		this.parent.drawString(this.parent.getFontRenderer(), serverdata.populationInfo, par2 + 215 - this.parent.getFontRenderer().getStringWidth(serverdata.populationInfo), par3 + 12, 8421504);
 		if(flag2) {
 			String s = EnumChatFormatting.DARK_RED + serverdata.gameVersion;
 			this.parent.drawString(this.parent.getFontRenderer(), s, par2 + 200 - this.parent.getFontRenderer().getStringWidth(s), par3 + 1, 8421504);
 		}
 		if(!this.parent.getMinecraft().gameSettings.hideServerAddress && !serverdata.isHidingAddress()) {
 			this.parent.drawString(this.parent.getFontRenderer(), serverdata.serverIP, par2 + 2, par3 + 12 + 11, 5263440);
 		}
 		else {
 			this.parent.drawString(this.parent.getFontRenderer(), StatCollector.translateToLocal("selectServer.hiddenAddress"), par2 + 2, par3 + 12 + 11, 5263440);
 		}
 		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		this.parent.getMinecraft().renderEngine.bindTexture(Gui.icons);
 		byte b0 = 0;
 		String s1 = "";
 		int i1;
 		if(flag2) {
 			s1 = flag ? "Client out of date!" : "Server out of date!";
 			i1 = 5;
 		}
 		else if(serverdata.field_78841_f && serverdata.pingToServer != -2L) {
 			if(serverdata.pingToServer < 0L) {
 				i1 = 5;
 			}
 			else if(serverdata.pingToServer < 150L) {
 				i1 = 0;
 			}
 			else if(serverdata.pingToServer < 300L) {
 				i1 = 1;
 			}
 			else if(serverdata.pingToServer < 600L) {
 				i1 = 2;
 			}
 			else if(serverdata.pingToServer < 1000L) {
 				i1 = 3;
 			}
 			else {
 				i1 = 4;
 			}
 			if(serverdata.pingToServer < 0L) {
 				s1 = "(no connection)";
 			}
 			else {
 				s1 = serverdata.pingToServer + "ms";
 			}
 		}
 		else {
 			b0 = 1;
 			i1 = (int)(Minecraft.getSystemTime() / 100L + par1 * 2 & 7L);
 			if(i1 > 4) {
 				i1 = 8 - i1;
 			}
 			s1 = "Polling..";
 		}
 		this.parent.drawTexturedModalRect(par2 + 205, par3, 0 + b0 * 10, 176 + i1 * 8, 10, 8);
 		byte b1 = 4;
 		if(this.mouseX >= par2 + 205 - b1 && this.mouseY >= par3 - b1 && this.mouseX <= par2 + 205 + 10 + b1 && this.mouseY <= par3 + 8 + b1) {
 			GuiNovamenuMultiplayer.getAndSetLagTooltip(this.parent, s1);
 		}
 	}
 
 }
