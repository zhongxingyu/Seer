 package net.minecraft.src;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import net.minecraft.client.Minecraft;
 
 public class GuiModCanned extends GuiScreen {
 	public String chatmessage = "";
 	Properties p = new Properties();
 	File configDir = new File(Minecraft.getMinecraftDir(), "/config/");
 	File config = new File(configDir, "FogHelperConfig.cfg");
 	boolean grabbed = false;
 	String[] cannedButtonName = new String[25];
 	String[] cannedButtonMessage = new String[25];
 
 	public GuiModCanned(GuiScreen guiscreen) {
 		screenTitle = "DMR";
 		guiScreen = guiscreen;
 	}
 
 	public void grabdata() {
 		try {
 
 			p.load(new FileInputStream(config));
 			for (int c = 1; c <= 24; c++) {
 				cannedButtonName[c] = p.getProperty("CannedReplyButtonLabel_0"
 						+ c);
 			}
 			for (int c = 1; c <= 24; c++) {
 				cannedButtonMessage[c] = p.getProperty("Reply/Message_0" + c);
 			}
 
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void initGui() {
 		if (grabbed == false) {
 			grabdata();
 			grabbed = !grabbed;
 		}
 
 		StringTranslate stringtranslate = StringTranslate.getInstance();
 		screenTitle = stringtranslate.translateKey("Fogest's Easy Messages!");
 		controlList.add(new GuiButton(8, width / 2 + 2, height / 6 + 168, 98,
 				20, ("Return to Game")));
 		controlList.add(new GuiButton(1, width / 2 - 100, height / 6 + 168, 98,
 				20, ("Back")));
 
 		controlList.add(new GuiButton(2, width / 2 - 200, height / 6 - 10, 98,
 				20, (cannedButtonName[1])));
 		controlList.add(new GuiButton(3, width / 2 - 100, height / 6 - 10, 98,
 				20, (cannedButtonName[2])));
 		controlList.add(new GuiButton(9, width / 2 + 2, height / 6 - 10, 98,
 				20, (cannedButtonName[3])));
 		controlList.add(new GuiButton(10, width / 2 + 102, height / 6 - 10, 98,
 				20, (cannedButtonName[4])));
 
 		controlList.add(new GuiButton(11, width / 2 - 200, height / 6 + 14, 98,
 				20, (cannedButtonName[5])));
 		controlList.add(new GuiButton(12, width / 2 - 100, height / 6 + 14, 98,
 				20, (cannedButtonName[6])));
 		controlList.add(new GuiButton(13, width / 2 + 2, height / 6 + 14, 98,
 				20, (cannedButtonName[7])));
 		controlList.add(new GuiButton(14, width / 2 + 102, height / 6 + 14, 98,
 				20, (cannedButtonName[8])));
 
 		controlList.add(new GuiButton(4, width / 2 - 200, height / 6 + 38, 98,
 				20, (cannedButtonName[9])));
 		controlList.add(new GuiButton(5, width / 2 - 100, height / 6 + 38, 98,
 				20, (cannedButtonName[10])));
 		controlList.add(new GuiButton(6, width / 2 + 2, height / 6 + 38, 98,
 				20, (cannedButtonName[11])));
 		controlList.add(new GuiButton(7, width / 2 + 102, height / 6 + 38, 98,
 				20, (cannedButtonName[12])));
 
 		controlList.add(new GuiButton(15, width / 2 - 200, height / 6 + 62, 98,
 				20, (cannedButtonName[13])));
 		controlList.add(new GuiButton(16, width / 2 - 100, height / 6 + 62, 98,
 				20, (cannedButtonName[14])));
 		controlList.add(new GuiButton(17, width / 2 + 2, height / 6 + 62, 98,
 				20, (cannedButtonName[15])));
 		controlList.add(new GuiButton(18, width / 2 + 102, height / 6 + 62, 98,
 				20, (cannedButtonName[16])));
 
 		controlList.add(new GuiButton(19, width / 2 - 200, height / 6 + 86, 98,
 				20, (cannedButtonName[17])));
 		controlList.add(new GuiButton(20, width / 2 - 100, height / 6 + 86, 98,
 				20, (cannedButtonName[18])));
 		controlList.add(new GuiButton(21, width / 2 + 2, height / 6 + 86, 98,
 				20, (cannedButtonName[19])));
 		controlList.add(new GuiButton(22, width / 2 + 102, height / 6 + 86, 98,
 				20, (cannedButtonName[20])));
 
 		controlList.add(new GuiButton(23, width / 2 - 200, height / 6 + 110,
 				98, 20, (cannedButtonName[21])));
 		controlList.add(new GuiButton(24, width / 2 - 100, height / 6 + 110, 98,
 				20, (cannedButtonName[22])));
 		controlList.add(new GuiButton(25, width / 2 + 2, height / 6 + 110,
 				98, 20, (cannedButtonName[23])));
 		controlList.add(new GuiButton(26, width / 2 + 102, height / 6 + 110,
 				98, 20, (cannedButtonName[24])));
 
 		controlList.add(new GuiButton(27, width / 2 - 100, height / 6 + 134,
 				98, 20, ("Page 1")));
 		controlList.add(new GuiButton(28, width / 2 + 2, height / 6 + 134, 98,
 				20, ("Page 2")));
 		controlList.add(new GuiButton(29, width / 2 + 102, height / 6 + 134,
 				98, 20, ("Page 3")));
 		controlList.add(new GuiButton(30, width / 2 - 200, height / 6 + 134,
 				98, 20, ("Page 4")));
 	}
 
 	protected void actionPerformed(GuiButton guibutton) {
 		if (!guibutton.enabled) {
 			return;
 		}
 
 		if (guibutton.id == 1) {
 			mc.displayGuiScreen(guiScreen);
 		}
 		// For n00b's who want Guest Rank
 		if (guibutton.id == 2) {
 			chatmessage = cannedButtonMessage[1];
 			chatgo();
 		}
 		// For Guest's Who want Builder
 		if (guibutton.id == 3) {
 			chatmessage = cannedButtonMessage[2];
 			chatgo();
 		}
 		// For Welcoming n00b
 		if (guibutton.id == 4) {
 			chatmessage = cannedButtonMessage[9];
 			chatgo();
 		}
 		// For telling users to stop helping guests!
 		if (guibutton.id == 5) {
 			chatmessage = cannedButtonMessage[10];
 			chatgo();
 		}
 		// Guest Passed the Build aspect and now got promoted. This is used to
 		// deal with telling them what to do now!
 		if (guibutton.id == 6) {
 			chatmessage = cannedButtonMessage[11];
 			chatgo();
 		}
 		// For telling users where they can build if they need some freeland
 		if (guibutton.id == 7) {
 			chatmessage = cannedButtonMessage[12];
 			chatgo();
 		}
 		// Return Directly to game bypassing the other menu's
 		if (guibutton.id == 8) {
 			mc.displayGuiScreen(null);
 			mc.setIngameFocus();
 		}
 		// user wants staff
 		if (guibutton.id == 9) {
 			chatmessage = cannedButtonMessage[3];
 			chatgo();
 		}
 		// user wants next rank in promotion scheme
 		if (guibutton.id == 10) {
 			chatmessage = cannedButtonMessage[4];
 			chatgo();
 		}
 		// Website Link
 		if (guibutton.id == 11) {
 			chatmessage = cannedButtonMessage[5];
 			chatgo();
 		}
 		// Wiki Promotion Message
 		if (guibutton.id == 12) {
 			chatmessage = cannedButtonMessage[6];
 			chatgo();
 		}
 		// Website promotion message
 		if (guibutton.id == 13) {
 			chatmessage = cannedButtonMessage[7];
 			chatgo();
 		}
 		// Vote for the server promotion
 		if (guibutton.id == 14) {
 			chatmessage = cannedButtonMessage[8];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 15) {
 			chatmessage = cannedButtonMessage[13];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 16) {
 			chatmessage = cannedButtonMessage[14];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 17) {
 			chatmessage = cannedButtonMessage[15];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 18) {
 			chatmessage = cannedButtonMessage[16];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 19) {
 			chatmessage = cannedButtonMessage[17];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 20) {
 			chatmessage = cannedButtonMessage[18];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 21) {
 			chatmessage = cannedButtonMessage[19];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 22) {
 			chatmessage = cannedButtonMessage[20];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 23) {
 			chatmessage = cannedButtonMessage[21];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 24) {
 			chatmessage = cannedButtonMessage[22];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 25) {
 			chatmessage = cannedButtonMessage[23];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 26) {
 			chatmessage = cannedButtonMessage[24];
 			chatgo();
 		}
 		// unused
 		if (guibutton.id == 27) {
 		}
 		// unused
 		if (guibutton.id == 28) {
 		}
 		// unused
 		if (guibutton.id == 29) {
 		}
 		// unused
 		if (guibutton.id == 30) {
 		}
 	}
 
 	public void chatgo() {
		mc.getSendQueue().addToSendQueue(new Packet3Chat(chatmessage));
 		mc.displayGuiScreen(null);
 		mc.setIngameFocus();
 	}
 
 	public void drawScreen(int i, int j, float f) {
 		drawDefaultBackground();
 		drawCenteredString(fontRenderer, screenTitle, width / 2, 20, 0xff0000);
 		super.drawScreen(i, j, f);
 	}
 
 	protected String screenTitle;
 	protected GuiScreen guiScreen;
 }
