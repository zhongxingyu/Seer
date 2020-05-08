 package com.github.desmaster.Devio.cons;
 
 import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
 import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glDisable;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 import static org.lwjgl.opengl.GL11.glVertex2i;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.Rectangle;
 
 import com.github.desmaster.Devio.Devio;
 import com.github.desmaster.Devio.gfx.Screen;
 import com.github.desmaster.Devio.gfx.userinterface.UserInterface;
 import com.github.desmaster.Devio.realm.entity.Player;
 
 public class Console extends UserInterface {
 
 	static List<String> commands = new LinkedList<String>();
 
 	private static String msg = "";
 	private static int textX;
 	private int textRenderX = 15;
 	private int textRenderY = 144;
 
 	boolean shouldRenderLine = true;
 	int line;
 	int lineDelay = 35;
 	int lineX = 12;
 
 	public Console() {
 		super("Console");
 		container = new Rectangle(0, 0, Display.getWidth(), (Display.getHeight() / 4));
 	}
 
 	public void tick() {
 		if (Screen.getInput().console.clicked) {
 			active = !active;
 		}
 
 		if (!active && msg != "")
 			msg = "";
 
 		if (active) {
 			Screen.getPlayer().disableInput();
 			pollInput();
 		} else {
			Screen.getPlayer().enableInput();
 		}
 
 		if (shouldRenderLine) {
 			if (line < lineDelay)
 				line++;
 		} else {
 			line--;
 		}
 
 		if (line == lineDelay)
 			shouldRenderLine = false;
 
 		if (line == 00)
 			shouldRenderLine = true;
 
 		lineX = textX + 3;
 
 		if (Screen.getInput().enter.clicked) {
 			if (msg.startsWith("/")) {
 				msg = msg.substring(1, msg.length());
 				sendToCommandHandler(msg);
 			} else {
 				sendToChat(msg);
 			}
 			if (ChatHandler.chatList.size() > 6) {
 				ChatHandler.chatList.remove(0);
 			}
 		}
 
 		if (active) {
 			if (Screen.getInput().consoleUp.clicked) {
 				if (ChatHandler.chatList.size() > 0) {
 					log("ConsoleUpClicked");
 					msg = ChatHandler.chatList.get(ChatHandler.chatList.size() - 1);
 				}
 			}
 		}
 
 	}
 
 	public void pollInput() {
 		if (Screen.getInput().backspace.clicked) {
 			if (msg != "") {
 				if (!(msg.length() <= 0))
 					msg = msg.substring(0, msg.length() - 1);
 			}
 		}
 	}
 
 	public void render() {
 		if (active) {
 			renderContainer();
 			renderConsole();
 			renderInputText();
 			drawString(msg, textRenderX, textRenderY);
 		}
 	}
 
 	public static void sendToChat(String text) {
 		msg = "";
 		if (!(text == "" || text == " "))
 			ChatHandler.log(text);
 	}
 
 	public void sendToCommandHandler(String s) {
 		msg = "";
 		String command = s.toLowerCase();
 		sendToChat("Command: " + command);
 		CommandHandler.RunCommand(command);
 	}
 
 	public void renderContainer() {
 		glLoadIdentity();
 		glDisable(GL_TEXTURE_2D);
 		glColor4f(0.15f, 0.15f, 0.15f, 0.8f);
 		glTranslatef(container.getX(), container.getY(), 0);
 		glBegin(GL_QUADS);
 		glVertex2f(container.getX(), container.getY());
 		glVertex2f(container.getX() + container.getWidth(), container.getY());
 		glVertex2f(container.getX() + container.getWidth(), container.getY() + container.getHeight());
 		glVertex2f(container.getX(), container.getY() + container.getHeight());
 		glEnd();
 	}
 
 	public void renderConsole() {
 		glLoadIdentity();
 		glDisable(GL_TEXTURE_2D);
 		glColor4f(0.05f, 0.05f, 0.05f, 0.8f);
 		glTranslatef(container.getX(), container.getY(), 0);
 		glBegin(GL_QUADS);
 		glVertex2i(container.getX() + 10, container.getY() + 10);
 		glVertex2i(container.getX() + container.getWidth() - 10, container.getY() + 10);
 		glVertex2i(container.getX() + container.getWidth() - 10, container.getY() + container.getHeight() - 40);
 		glVertex2i(container.getX() + 10, container.getY() + container.getHeight() - 40);
 		glEnd();
 		ChatHandler.renderConsoleText();
 	}
 
 	public void renderInputText() {
 		glLoadIdentity();
 		glDisable(GL_TEXTURE_2D);
 		glColor4f(0.05f, 0.05f, 0.05f, 0.5f);
 		glTranslatef(container.getX(), container.getY(), 0);
 		glBegin(GL_QUADS);
 		glVertex2i(container.getX() + 10, container.getY() + container.getHeight() - 7);
 		glVertex2i(container.getX() + container.getWidth() - 10, container.getY() + container.getHeight() - 7);
 		glVertex2i(container.getX() + container.getWidth() - 10, container.getY() + container.getHeight() - 33);
 		glVertex2i(container.getX() + 10, container.getY() + container.getHeight() - 33);
 		glEnd();
 		glColor4f(1, 1, 1, 1);
 		if (shouldRenderLine) {
 			glBegin(GL_LINE_STRIP);
 			glVertex2f(lineX, 132);
 			glVertex2f(lineX, 147);
 			glEnd();
 		}
 	}
 
 	public void setString(String s) {
 		msg = s;
 	}
 
 	public static void log(String msg) {
 		System.out.println("Console: " + msg);
 	}
 
 	public static void logC(String msg) {
 		String header = "Console Command: ";
 		System.out.println(header + msg);
 	}
 
 	public static boolean isActive() {
 		return active;
 	}
 
 	public static void setMessage(String s) {
 		msg = s;
 	}
 
 	public static void type(char letter) {
 		msg = msg + letter;
 	}
 
 	public static void type(String s) {
 		msg = msg + s;
 	}
 
 	public static void drawString(String s, int x, int y) {
 		int startX = x;
 		GL11.glDisable(GL11.GL_BLEND);
 		GL11.glBegin(GL11.GL_POINTS);
 		GL11.glColor4f(1, 1, 1, 0.5f);
 		GL11.glLoadIdentity();
 		for (char c : s.toLowerCase().toCharArray()) {
 			if (c == 'a') {
 				for (int i = 0; i < 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				x += 8;
 			} else if (c == 'b') {
 				for (int i = 0; i < 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 1; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y);
 					GL11.glVertex2f(x + i, y - 4);
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				GL11.glVertex2f(x + 7, y - 5);
 				GL11.glVertex2f(x + 7, y - 7);
 				GL11.glVertex2f(x + 7, y - 6);
 
 				GL11.glVertex2f(x + 7, y - 1);
 				GL11.glVertex2f(x + 7, y - 2);
 				GL11.glVertex2f(x + 7, y - 3);
 				x += 8;
 			} else if (c == 'c') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y);
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				GL11.glVertex2f(x + 6, y - 1);
 				GL11.glVertex2f(x + 6, y - 2);
 
 				GL11.glVertex2f(x + 6, y - 6);
 				GL11.glVertex2f(x + 6, y - 7);
 
 				x += 8;
 			} else if (c == 'd') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y);
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				GL11.glVertex2f(x + 6, y - 1);
 				GL11.glVertex2f(x + 6, y - 2);
 				GL11.glVertex2f(x + 6, y - 3);
 				GL11.glVertex2f(x + 6, y - 4);
 				GL11.glVertex2f(x + 6, y - 5);
 				GL11.glVertex2f(x + 6, y - 6);
 				GL11.glVertex2f(x + 6, y - 7);
 
 				x += 8;
 			} else if (c == 'e') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 1; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 0);
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				x += 8;
 			} else if (c == 'f') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 1; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				x += 8;
 			} else if (c == 'g') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y);
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				GL11.glVertex2f(x + 6, y - 1);
 				GL11.glVertex2f(x + 6, y - 2);
 				GL11.glVertex2f(x + 6, y - 3);
 				GL11.glVertex2f(x + 5, y - 3);
 				GL11.glVertex2f(x + 7, y - 3);
 
 				GL11.glVertex2f(x + 6, y - 6);
 				GL11.glVertex2f(x + 6, y - 7);
 
 				x += 8;
 			} else if (c == 'h') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				x += 8;
 			} else if (c == 'i') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 3, y - i);
 				}
 				for (int i = 1; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 0);
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				x += 7;
 			} else if (c == 'j') {
 				for (int i = 1; i <= 8; i++) {
 					GL11.glVertex2f(x + 6, y - i);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 0);
 				}
 				GL11.glVertex2f(x + 1, y - 3);
 				GL11.glVertex2f(x + 1, y - 2);
 				GL11.glVertex2f(x + 1, y - 1);
 				x += 8;
 			} else if (c == 'k') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				GL11.glVertex2f(x + 6, y - 8);
 				GL11.glVertex2f(x + 5, y - 7);
 				GL11.glVertex2f(x + 4, y - 6);
 				GL11.glVertex2f(x + 3, y - 5);
 				GL11.glVertex2f(x + 2, y - 4);
 				GL11.glVertex2f(x + 2, y - 3);
 				GL11.glVertex2f(x + 3, y - 4);
 				GL11.glVertex2f(x + 4, y - 3);
 				GL11.glVertex2f(x + 5, y - 2);
 				GL11.glVertex2f(x + 6, y - 1);
 				GL11.glVertex2f(x + 7, y);
 				x += 8;
 			} else if (c == 'l') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 1; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y);
 				}
 				x += 7;
 			} else if (c == 'm') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				GL11.glVertex2f(x + 3, y - 6);
 				GL11.glVertex2f(x + 2, y - 7);
 				GL11.glVertex2f(x + 4, y - 5);
 
 				GL11.glVertex2f(x + 5, y - 6);
 				GL11.glVertex2f(x + 6, y - 7);
 				GL11.glVertex2f(x + 4, y - 5);
 				x += 8;
 			} else if (c == 'n') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				GL11.glVertex2f(x + 2, y - 7);
 				GL11.glVertex2f(x + 2, y - 6);
 				GL11.glVertex2f(x + 3, y - 5);
 				GL11.glVertex2f(x + 4, y - 4);
 				GL11.glVertex2f(x + 5, y - 3);
 				GL11.glVertex2f(x + 6, y - 2);
 				GL11.glVertex2f(x + 6, y - 1);
 				x += 8;
 			} else if (c == 'o' || c == '0') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y - 0);
 				}
 				x += 8;
 			} else if (c == 'p') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				GL11.glVertex2f(x + 6, y - 7);
 				GL11.glVertex2f(x + 6, y - 5);
 				GL11.glVertex2f(x + 6, y - 6);
 				x += 8;
 			} else if (c == 'q') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					if (i != 1)
 						GL11.glVertex2f(x + 7, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					if (i != 6)
 						GL11.glVertex2f(x + i, y - 0);
 				}
 				GL11.glVertex2f(x + 4, y - 3);
 				GL11.glVertex2f(x + 5, y - 2);
 				GL11.glVertex2f(x + 6, y - 1);
 				GL11.glVertex2f(x + 7, y);
 				x += 8;
 			} else if (c == 'r') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				GL11.glVertex2f(x + 6, y - 7);
 				GL11.glVertex2f(x + 6, y - 5);
 				GL11.glVertex2f(x + 6, y - 6);
 
 				GL11.glVertex2f(x + 4, y - 3);
 				GL11.glVertex2f(x + 5, y - 2);
 				GL11.glVertex2f(x + 6, y - 1);
 				GL11.glVertex2f(x + 7, y);
 				x += 8;
 			} else if (c == 's') {
 				for (int i = 2; i <= 7; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				GL11.glVertex2f(x + 1, y - 7);
 				GL11.glVertex2f(x + 1, y - 6);
 				GL11.glVertex2f(x + 1, y - 5);
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 					GL11.glVertex2f(x + i, y);
 				}
 				GL11.glVertex2f(x + 7, y - 3);
 				GL11.glVertex2f(x + 7, y - 2);
 				GL11.glVertex2f(x + 7, y - 1);
 				GL11.glVertex2f(x + 1, y - 1);
 				GL11.glVertex2f(x + 1, y - 2);
 				x += 8;
 			} else if (c == 't') {
 				for (int i = 0; i <= 8; i++) {
 					GL11.glVertex2f(x + 4, y - i);
 				}
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				x += 7;
 			} else if (c == 'u') {
 				for (int i = 1; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 0);
 				}
 				x += 8;
 			} else if (c == 'v') {
 				for (int i = 2; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 6, y - i);
 				}
 				GL11.glVertex2f(x + 2, y - 1);
 				GL11.glVertex2f(x + 5, y - 1);
 				GL11.glVertex2f(x + 3, y);
 				GL11.glVertex2f(x + 4, y);
 				x += 7;
 			} else if (c == 'w') {
 				for (int i = 1; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				GL11.glVertex2f(x + 2, y);
 				GL11.glVertex2f(x + 3, y);
 				GL11.glVertex2f(x + 5, y);
 				GL11.glVertex2f(x + 6, y);
 				for (int i = 1; i <= 6; i++) {
 					GL11.glVertex2f(x + 4, y - i);
 				}
 				x += 8;
 			} else if (c == 'x') {
 				y -= 1;
 				int yy = y - 8;
 				for (int i = 1; i <= 10; i++)
 					GL11.glVertex2f(x + i, yy + i);
 				for (int i = 10; i >= 1; i--)
 					GL11.glVertex2f(x + i, y - i + 3);
 				x += 13;
 				y += 1;
 			} else if (c == 'y') {
 				GL11.glVertex2f(x + 4, y);
 				GL11.glVertex2f(x + 4, y - 1);
 				GL11.glVertex2f(x + 4, y - 2);
 				GL11.glVertex2f(x + 4, y - 3);
 				GL11.glVertex2f(x + 4, y - 4);
 
 				GL11.glVertex2f(x + 3, y - 5);
 				GL11.glVertex2f(x + 2, y - 6);
 				GL11.glVertex2f(x + 1, y - 7);
 				GL11.glVertex2f(x + 1, y - 8);
 
 				GL11.glVertex2f(x + 5, y - 5);
 				GL11.glVertex2f(x + 6, y - 6);
 				GL11.glVertex2f(x + 7, y - 7);
 				GL11.glVertex2f(x + 7, y - 8);
 				x += 8;
 			} else if (c == 'z') {
 				for (int i = 1; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y);
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y - i);
 				}
 				GL11.glVertex2f(x + 6, y - 7);
 				x += 8;
 			} else if (c == '1') {
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y);
 				}
 				for (int i = 1; i <= 8; i++) {
 					GL11.glVertex2f(x + 4, y - i);
 				}
 				GL11.glVertex2f(x + 3, y - 7);
 				x += 8;
 			} else if (c == '2') {
 				for (int i = 1; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				GL11.glVertex2f(x + 1, y - 7);
 				GL11.glVertex2f(x + 1, y - 6);
 
 				GL11.glVertex2f(x + 6, y - 7);
 				GL11.glVertex2f(x + 6, y - 6);
 				GL11.glVertex2f(x + 6, y - 5);
 				GL11.glVertex2f(x + 5, y - 4);
 				GL11.glVertex2f(x + 4, y - 3);
 				GL11.glVertex2f(x + 3, y - 2);
 				GL11.glVertex2f(x + 2, y - 1);
 				x += 8;
 			} else if (c == '3') {
 				for (int i = 1; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y);
 				}
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 6, y - i);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				x += 8;
 			} else if (c == '4') {
 				for (int i = 2; i <= 8; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 7; i++) {
 					GL11.glVertex2f(x + i, y - 1);
 				}
 				for (int i = 0; i <= 4; i++) {
 					GL11.glVertex2f(x + 4, y - i);
 				}
 				x += 8;
 			} else if (c == '5') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				for (int i = 4; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				GL11.glVertex2f(x + 1, y - 1);
 				GL11.glVertex2f(x + 2, y);
 				GL11.glVertex2f(x + 3, y);
 				GL11.glVertex2f(x + 4, y);
 				GL11.glVertex2f(x + 5, y);
 				GL11.glVertex2f(x + 6, y);
 
 				GL11.glVertex2f(x + 7, y - 1);
 				GL11.glVertex2f(x + 7, y - 2);
 				GL11.glVertex2f(x + 7, y - 3);
 
 				GL11.glVertex2f(x + 6, y - 4);
 				GL11.glVertex2f(x + 5, y - 4);
 				GL11.glVertex2f(x + 4, y - 4);
 				GL11.glVertex2f(x + 3, y - 4);
 				GL11.glVertex2f(x + 2, y - 4);
 				x += 8;
 			} else if (c == '6') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y);
 				}
 				for (int i = 2; i <= 5; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 					GL11.glVertex2f(x + i, y - 8);
 				}
 				GL11.glVertex2f(x + 7, y - 1);
 				GL11.glVertex2f(x + 7, y - 2);
 				GL11.glVertex2f(x + 7, y - 3);
 				GL11.glVertex2f(x + 6, y - 4);
 				x += 8;
 			} else if (c == '7') {
 				for (int i = 0; i <= 7; i++)
 					GL11.glVertex2f(x + i, y - 8);
 				GL11.glVertex2f(x + 7, y - 7);
 				GL11.glVertex2f(x + 7, y - 6);
 
 				GL11.glVertex2f(x + 6, y - 5);
 				GL11.glVertex2f(x + 5, y - 4);
 				GL11.glVertex2f(x + 4, y - 3);
 				GL11.glVertex2f(x + 3, y - 2);
 				GL11.glVertex2f(x + 2, y - 1);
 				GL11.glVertex2f(x + 1, y);
 				x += 8;
 			} else if (c == '8') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y - 0);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				x += 8;
 			} else if (c == '9') {
 				for (int i = 1; i <= 7; i++) {
 					GL11.glVertex2f(x + 7, y - i);
 				}
 				for (int i = 5; i <= 7; i++) {
 					GL11.glVertex2f(x + 1, y - i);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 8);
 					GL11.glVertex2f(x + i, y - 0);
 				}
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 4);
 				}
 				GL11.glVertex2f(x + 1, y - 0);
 				x += 8;
 			} else if (c == '.') {
 				GL11.glVertex2f(x + 1, y);
 				x += 2;
 			} else if (c == ',') {
 				GL11.glVertex2f(x + 1, y);
 				GL11.glVertex2f(x + 1, y - 1);
 				x += 2;
 			} else if (c == '=') {
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x + i, y - 5);
 					GL11.glVertex2f(x + i, y - 3);
 				}
 				x += 8;
 			} else if (c == '\n') {
 				y -= 10;
 				x = startX;
 			} else if (c == ' ') {
 				x += 4;
 			} else if (c == '/') {
 				int yy = y + 2;
 				for (int i = 9; i >= 1; i--)
 					GL11.glVertex2f(x + i / 2, yy - i * 1.35f);
 				x += 6;
 			} else if (c == ':') {
 				for (int i = 2; i <= 6; i++) {
 					GL11.glVertex2f(x, y - 7);
 					GL11.glVertex2f(x, y - 1);
 				}
 			}
 		}
 		textX = x;
 		GL11.glColor4f(1, 1, 1, 1);
 		GL11.glEnd();
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 	}
 
 }
