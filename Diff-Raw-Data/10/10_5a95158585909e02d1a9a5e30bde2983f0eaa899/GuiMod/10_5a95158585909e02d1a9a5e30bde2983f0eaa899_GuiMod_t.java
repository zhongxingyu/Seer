 package net.minecraft.src;
 
 import java.io.File;
 import java.util.List;
 import org.lwjgl.input.Keyboard;
 import net.minecraft.client.Minecraft;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 public class GuiMod extends GuiScreen {
 	Properties p = new Properties();
 	File configDir = new File(Minecraft.getMinecraftDir(), "/config/");
 	File config = new File(configDir, "FogHelperConfig.cfg");
 
 	boolean grabbed = false;
 	String[] commandButtonName = new String[21];
 	String[] commandButtonCommand = new String[21];
 
 	public GuiMod(GuiScreen guiscreen) {
 		screenTitle = "DMR";
 		guiScreen = guiscreen;
 	}
 
 	public void grabdata() {
 		try {
 
 			p.load(new FileInputStream(config));
 			for (int c = 1; c <= 20; c++) {
 				commandButtonName[c] = p
 						.getProperty(("CommandButtonLabel_0" + c));
 			}
 			for (int c = 1; c <= 20; c++) {
 				commandButtonCommand[c] = p.getProperty("Command_0" + c);
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
 		screenTitle = stringtranslate.translateKey("Fogest's Easy Commands");
 		controlList.add(new GuiButton(8, width / 2 + 2, height / 6 + 168, 98,
 				20, ("Return to Game")));
 		controlList.add(new GuiButton(1, width / 2 - 100, height / 6 + 168, 98,
 				20, ("Back")));
 
 		controlList.add(new GuiButton(2, width / 2 - 200, height / 6 - 10, 98,
 				20, (commandButtonName[1])));
 		controlList.add(new GuiButton(3, width / 2 - 100, height / 6 - 10, 98,
 				20, (commandButtonName[2])));
 		controlList.add(new GuiButton(9, width / 2 + 2, height / 6 - 10, 98,
 				20, (commandButtonName[3])));
 		controlList.add(new GuiButton(10, width / 2 + 102, height / 6 - 10, 98,
 				20, (commandButtonName[4])));
 
 		controlList.add(new GuiButton(11, width / 2 - 200, height / 6 + 14, 98,
 				20, (commandButtonName[5])));
 		controlList.add(new GuiButton(12, width / 2 - 100, height / 6 + 14, 98,
 				20, (commandButtonName[6])));
 		controlList.add(new GuiButton(13, width / 2 + 2, height / 6 + 14, 98,
 				20, (commandButtonName[7])));
 		controlList.add(new GuiButton(14, width / 2 + 102, height / 6 + 14, 98,
 				20, (commandButtonName[8])));
 
 		controlList.add(new GuiButton(15, width / 2 - 200, height / 6 + 38, 98,
 				20, (commandButtonName[9])));
 		controlList.add(new GuiButton(16, width / 2 - 100, height / 6 + 38, 98,
 				20, (commandButtonName[10])));
 		controlList.add(new GuiButton(17, width / 2 + 2, height / 6 + 38, 98,
 				20, (commandButtonName[11])));
 		controlList.add(new GuiButton(18, width / 2 + 102, height / 6 + 38, 98,
 				20, (commandButtonName[12])));
 
 		controlList.add(new GuiButton(19, width / 2 - 200, height / 6 + 62, 98,
 				20, (commandButtonName[13])));
 		controlList.add(new GuiButton(20, width / 2 - 100, height / 6 + 62, 98,
 				20, (commandButtonName[14])));
 		controlList.add(new GuiButton(21, width / 2 + 2, height / 6 + 62, 98,
 				20, (commandButtonName[15])));
 		controlList.add(new GuiButton(22, width / 2 + 102, height / 6 + 62, 98,
 				20, (commandButtonName[16])));
 
 		controlList.add(new GuiButton(4, width / 2 - 200, height / 6 + 86, 98,
 				20, (commandButtonName[17])));
 		controlList.add(new GuiButton(5, width / 2 - 100, height / 6 + 86, 98,
 				20, (commandButtonName[18])));
 		controlList.add(new GuiButton(6, width / 2 + 2, height / 6 + 86, 98,
 				20, (commandButtonName[19])));
 		controlList.add(new GuiButton(7, width / 2 + 102, height / 6 + 86, 98,
 				20, (commandButtonName[20])));
 
 		usernameTextField = new GuiTextField(fontRenderer,width / 2 + 100, height / 6 + 134, 100, 20);
 		usernameTextField.setMaxStringLength(26);
 		var1 = new GuiTextField(fontRenderer, width / 2 - 100,
 				height / 6 + 134, 100, 20);
 		var1.setMaxStringLength(80);
 
 		controlList.add(new GuiButton(23, width / 2 - 200, height / 6 + 110,
 				98, 20, ("Page 1")));
 		controlList.add(new GuiButton(24, width / 2 - 100, height / 6 + 110,
 				98, 20, ("Page 2")));
 		controlList.add(new GuiButton(25, width / 2 + 2, height / 6 + 110, 98,
 				20, ("Page 3")));
 		controlList.add(new GuiButton(26, width / 2 + 102, height / 6 + 110,
 				98, 20, ("Page 4")));
 
 	}
 
 	protected void actionPerformed(GuiButton guibutton) {
 		if (!guibutton.enabled) {
 			return;
 		}
 
 		if (guibutton.id == 1) {
 			mc.displayGuiScreen(guiScreen);
 		}
 
 		if (guibutton.id == 2) {
 			chatmessage = commandButtonCommand[1];
 			chatprocess();
 			chatgo();
 		}
 
 		if (guibutton.id == 3) {
 			chatmessage = commandButtonCommand[2];
 			chatprocess();
 			chatgo();
 		}
 
 		if (guibutton.id == 4) {
 			chatmessage = commandButtonCommand[17];
 			chatprocess();
 			chatgo();
 		}
 
 		if (guibutton.id == 5) {
 			chatmessage = commandButtonCommand[18];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 6) {
 				chatmessage = commandButtonCommand[19];
 				chatprocess();
 				chatgo();
 		}
 		if (guibutton.id == 7) {
 				chatmessage = commandButtonCommand[20];
 				chatprocess();
 				chatgo();
 		}
 		if (guibutton.id == 8) {
 			mc.displayGuiScreen(null);
 			mc.setIngameFocus();
 		}
 		if (guibutton.id == 9) {
 			chatmessage = commandButtonCommand[3];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 10) {
 			chatmessage = commandButtonCommand[4];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 11) {
 			chatmessage = commandButtonCommand[5];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 12) {
 			chatmessage = commandButtonCommand[6];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 13) {
 			chatmessage = commandButtonCommand[7];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 14) {
 			chatmessage = commandButtonCommand[8];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 15) {
 			chatmessage = commandButtonCommand[9];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 16) {
 				chatmessage = commandButtonCommand[10];
 				chatprocess();
 				chatgo();
 		}
 		if (guibutton.id == 17) {
 				chatmessage = commandButtonCommand[11];
 				chatprocess();
 				chatgo();
 		}
 		if (guibutton.id == 18) {
 			chatmessage = commandButtonCommand[12];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 19) {
 			chatmessage = commandButtonCommand[13];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 20) {
 			chatmessage = commandButtonCommand[14];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 21) {
 			chatmessage = commandButtonCommand[15];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 22) {
 			chatmessage = commandButtonCommand[16];
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 23) {
 			chatmessage = "Unused";
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 24) {
 			chatmessage = "Unused";
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 25) {
 			chatmessage = "Unused";
 			chatprocess();
 			chatgo();
 		}
 		if (guibutton.id == 26) {
 			chatmessage = "Unused";
 			chatprocess();
 			chatgo();
 		}
 	}
 	public void chatprocess(){
 		usernameTextField.updateCursorCounter();
 		player = usernameTextField.getText();
 		var1.updateCursorCounter();
 		var2 = var1.getText();
		chatmessage = chatmessage.replace("{var1}",var2);
		chatmessage = chatmessage.replace("{var2}",player);
		chatmessage = chatmessage.replace("{user}",player);
		chatmessage = chatmessage.replace("{box1}",var2);
		chatmessage = chatmessage.replace("{box2",player);
 	}
 
 	public void chatgo() {
 		mc.thePlayer.sendChatMessage(chatmessage);
 		mc.thePlayer.addChatMessage("The Command Preformed Was: " + chatmessage);
 		mc.displayGuiScreen(null);
 		mc.setIngameFocus();
 	}
 
 	public void updateScreen() {
 		usernameTextField.updateCursorCounter();
 		player = usernameTextField.getText();
 		var1.updateCursorCounter();
 		var2 = var1.getText();
 	}
 
 	public void onGuiClosed() {
 		Keyboard.enableRepeatEvents(false);
 	}
 
 	protected void keyTyped(char c, int i) {
 		super.keyTyped(c, i);
 		usernameTextField.func_50037_a(c, i);
 		var1.func_50037_a(c, i);
 	}
 
 	public void mouseClicked(int i, int j, int k) {
 		super.mouseClicked(i, j, k);
 		usernameTextField.mouseClicked(i, j, k);
 		var1.mouseClicked(i, j, k);
 	}
 
 	public void drawScreen(int i, int j, float f) {
 		drawDefaultBackground();
 		drawCenteredString(fontRenderer, screenTitle, width / 2, 20, 0xff0000);
 		usernameTextField.drawTextBox();
 		var1.drawTextBox();
 		drawString(fontRenderer, text1, width / 2 - 200, height / 6 + 140,
 				0x0091FF);
 		drawString(fontRenderer, text2, width / 2 + 15, height / 6 + 140,
 				0xBB00FF);
 		super.drawScreen(i, j, f);
 	}
 
 	protected String screenTitle;
 	protected GuiScreen guiScreen;
 	private String text1 = "Variable (i.e. rank)";
 	private String text2 = "Username(full)";
 	public String chatmessage;
 	private GuiTextField usernameTextField;
 	private GuiTextField var1;
 	public String player;
 	public String var2;
 }
