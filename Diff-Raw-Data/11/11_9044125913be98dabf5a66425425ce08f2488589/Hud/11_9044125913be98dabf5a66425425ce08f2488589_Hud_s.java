 package propinquity;
 
 import javax.media.opengl.GL;
 
 import processing.core.*;
 import processing.opengl.PGraphicsOpenGL;
 
 /**
  * Handles Propinquity's Heads-Up Display, which shows player names, scores and
  * other graphics.
  * 
  * @author Stephane Beniak, Severin Smith
  */
 public class Hud {
 
 	// HUD constants
 	public static final int FONT_SIZE = 30;
 	public static final int WIDTH = 50;
 	public static final int OFFSET = 20;
 	public static final int SCORE_RADIUS_OFFSET = 40;
 	public static final float SCORE_ANGLE_OFFSET = 0.35f;
 	public static final float SCORE_ROT_SPEED = 0.0001f;
 	public static final float PROMPT_ROT_SPEED = 0.002f;
 	public static final int BOUNDARY_WIDTH = 5;
 
 	private Propinquity parent;
 
 	float angle = 0;
 	float velocity = -PConstants.TWO_PI / 500f;
 
 	boolean isSnapped = false;
 
 	public PFont font;
 
 	public PImage hudInnerBoundary, hudOuterBoundary;
 	public PImage hudPlay, hudLevelComplete, hudPlayAgain;
 	public PImage hudBannerSide, hudBannerCenter;
 
 	/**
 	 * Create the hud and load the images it will use.
 	 * 
 	 * @param parent The parent processing applet.
 	 */
 	public Hud(Propinquity parent) {
 		this.parent = parent;
 
 		font = parent.loadFont("hud/Calibri-Bold-32.vlw");
 
 		hudInnerBoundary = parent.loadImage("hud/innerBoundary.png");
 		hudOuterBoundary = parent.loadImage("hud/outerBoundary.png");
 
 		hudBannerCenter = parent.loadImage("hud/bannercenter.png");
 		hudBannerSide = parent.loadImage("hud/bannerside.png");
 
 		hudPlay = parent.loadImage("hud/sbtoplay.png");
 		hudLevelComplete = parent.loadImage("hud/levelcomplete.png");
 		hudPlayAgain = parent.loadImage("hud/sbtoplayagain.png");
 	}
 
 	/**
 	 * Return the HUD's current angle.
 	 * 
 	 * @return The HUD's current angle in radians.
 	 */
 	public float getAngle() {
 		return angle;
 	}
 
 	/**
 	 * Reset the HUD to its default angle and velocity.
 	 */
 	public void reset() {
 		isSnapped = false;
 		velocity = -PConstants.TWO_PI / 500f;
 	}
 
 	/**
 	 * Snap the hud to its final position when displaying scores.
 	 */
 	public void snap() {
 		if (!isSnapped) {
 			angle -= ((int) (angle / PConstants.TWO_PI)) * PConstants.TWO_PI;
 			isSnapped = true;
 		}
 
 		update(PConstants.TWO_PI, PConstants.TWO_PI / 500f, PConstants.TWO_PI / 10f);
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param targetAngle 
 	 * @param targetAcceleration
 	 * @param maxVelocity
 	 */
 	public void update(float targetAngle, float targetAcceleration, float maxVelocity) {
 		float diff = targetAngle - angle;
 		int dir = diff < 0 ? -1 : 1;
 
 		if (diff * dir < PConstants.TWO_PI / 5000f) {
 			angle = targetAngle;
 			return;
 		}
 
 		velocity += dir * targetAcceleration;
 		if (velocity / dir > maxVelocity)
 			velocity = dir * maxVelocity;
 
 		velocity *= 0.85;
 
 		angle += velocity;
 	}
 
 	/**
 	 * Draw the score banner(s) for the current level based on the game mode and
 	 * number of players.
 	 */
 	public void drawScoreBanners() {
 		if (parent.level.isCoop()) {
 			String score = String.valueOf(parent.level.getTotalPoints());
 			String name = "Coop";
 
 			while (parent.textWidth(score + name) < 240)
 				name += ' ';
 
 			drawBannerCenter(name + score, PlayerConstants.NEUTRAL_COLOR, angle);
 		} else {
			for (int i = 0; i < parent.level.getNumberOfPlayers(); i++) {
 				String score = String.valueOf(parent.players[i].score.getScore());
 				String name = parent.players[i].getName();
 
 				while (parent.textWidth(score + name) < 240)
 					name += ' ';
 
				drawBannerSide(name + score, PlayerConstants.PLAYER_COLORS[i], angle + (i * PConstants.PI));
 			}
 		}
 	}
 
 	/**
 	 * Draw a string of text in a nice circular arc.
 	 * 
 	 * @param message The message to be displayed.
 	 * @param radius The radius of the circle it'll be drawn on.
 	 * @param startAngle The angle heading in radians where it will be drawn..
 	 */
 	public void drawArc(String message, float radius, float startAngle) {
 		// We must keep track of our position along the curve
 		float arclength = 0;
 
 		// For every box
 		for (int i = 0; i < message.length(); i++) {
 			// Instead of a constant width, we check the width of each
 			// character.
 			char currentChar = message.charAt(i);
 			float w = parent.textWidth(currentChar);
 
 			// Each box is centered so we move half the width
 			arclength += w / 2;
 			// Angle in radians is the arclength divided by the radius
 			// Starting on the left side of the circle by adding PI
 			float theta = startAngle + arclength / radius;
 
 			parent.pushMatrix();
 			// Polar to Cartesian coordinate conversion
 			parent.translate(radius * PApplet.cos(theta), radius * PApplet.sin(theta));
 			// Rotate the box
 			parent.rotate(theta + PConstants.PI / 2); // rotation is offset by
 														// 90 degrees
 			// Display the character
 			// fill(0);
 			parent.text(currentChar, 0, 0);
 			parent.popMatrix();
 			// Move halfway again
 			arclength += w / 2;
 		}
 	}
 
 	/**
 	 * Draws a side banner on the border of the outer game boundary.
 	 * 
 	 * @param text The text to be printed on the banner.
 	 * @param color The tint color applied to the image.
 	 * @param angle The angle in radians at which the banner will be drawn.
 	 */
 	public void drawBannerSide(String text, Color color, float angle) {
 		drawBanner(text, color, angle, hudBannerSide);
 	}
 
 	/**
 	 * Draws a center banner on the border of the outer game boundary.
 	 * 
 	 * @param text The text to be printed on the banner.
 	 * @param color The tint color applied to the image.
 	 * @param angle The angle in radians at which the banner will be drawn.
 	 */
 	public void drawBannerCenter(String text, Color color, float angle) {
 		drawBanner(text, color, angle, hudBannerCenter);
 	}
 
 	/**
 	 * Draws a banner on the border of the outer game boundary.
 	 * 
 	 * @param text The text to be printed on the banner.
 	 * @param color The tint color applied to the image.
 	 * @param angle The angle in radians at which the banner will be drawn.
 	 * @param bannerImg The banner image to be drawn.
 	 */
 	public void drawBanner(String text, Color color, float angle, PImage bannerImg) {
 		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
 		parent.gl.glEnable(GL.GL_BLEND);
 		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		parent.noStroke();
 		parent.noFill();
 
 		parent.pushMatrix();
 		parent.translate(parent.width / 2, parent.height / 2);
 		parent.translate(PApplet.cos(angle) * (parent.height / 2 - Hud.WIDTH + Hud.OFFSET), PApplet.sin(angle)
 				* (parent.height / 2 - Hud.WIDTH + Hud.OFFSET));
 		parent.rotate(angle + PApplet.PI / 2);
 		parent.scale(bannerImg.width / 2, bannerImg.height / 2);
 		parent.beginShape(PApplet.QUADS);
 		parent.texture(bannerImg);
 		parent.tint(color.toInt(parent));
 		parent.vertex(-1, -1, 0, 0, 0);
 		parent.vertex(1, -1, 0, 1, 0);
 		parent.vertex(1, 1, 0, 1, 1);
 		parent.vertex(-1, 1, 0, 0, 1);
 		parent.noTint();
 		parent.endShape(PApplet.CLOSE);
 		parent.popMatrix();
 
 		parent.pushMatrix();
 		parent.fill(255);
 		parent.noStroke();
 		parent.translate(parent.width / 2, parent.height / 2);
 		parent.textAlign(PApplet.CENTER, PApplet.BASELINE);
 		parent.textFont(font, FONT_SIZE);
 		String cropped_text = text.length() > 30 ? text.substring(0, 30) : text;
 		float offset = (parent.textWidth(cropped_text) / 2)
 				/ (2 * PApplet.PI * (parent.height / 2 - Hud.SCORE_RADIUS_OFFSET)) * PApplet.TWO_PI;
 		drawArc(cropped_text, parent.height / 2 - Hud.SCORE_RADIUS_OFFSET + Hud.OFFSET, angle - offset);
 		parent.popMatrix();
 	}
 
 	/**
 	 * Draw the given text in the center of the game world.
 	 * 
 	 * @param line1 The line of text to be drawn.
 	 */
 	void drawCenterText(String line1) {
 		drawCenterText(line1, null);
 	}
 
 	/**
 	 * Draw the given text in the center of the game world.
 	 * 
 	 * @param line1 The first line of text to be drawn.
 	 * @param line2 The second line of text to be drawn.
 	 */
 	void drawCenterText(String line1, String line2) {
 		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
 		parent.gl.glEnable(GL.GL_BLEND);
 		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		parent.fill(255);
 		parent.pushMatrix();
 		parent.translate(parent.width / 2, parent.height / 2);
 		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);
 
 		parent.textFont(font, FONT_SIZE);
 		parent.text(line1, 0, 0);
 		if (line2 != null)
 			parent.text(line2, 0, -20);
 
 		parent.popMatrix();
 	}
 
 	/**
 	 * Draw the given image in the center of the game world.
 	 * 
 	 * @param image The image to be drawn.
 	 */
 	public void drawCenterImage(PImage image) {
 		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
 		parent.gl.glEnable(GL.GL_BLEND);
 		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		parent.fill(255);
 		parent.pushMatrix();
 		parent.rotate(parent.frameCount * Hud.PROMPT_ROT_SPEED);
 
 		parent.image(image, 0, 0);
 
 		parent.popMatrix();
 	}
 
 	/**
 	 * Draw the inner fence boundary circle graphic.
 	 */
 	public void drawInnerBoundary() {
 		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
 		parent.gl.glEnable(GL.GL_BLEND);
 		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		parent.pushMatrix();
 		parent.translate(parent.width / 2 - 1, parent.height / 2);
 		parent.image(hudInnerBoundary, 0, 0);
 		parent.popMatrix();
 	}
 
 	/**
 	 * Draw the outer fence boundary circle graphic.
 	 */
 	public void drawOuterBoundary() {
 		parent.gl = ((PGraphicsOpenGL) parent.g).gl;
 		parent.gl.glEnable(GL.GL_BLEND);
 		parent.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
 		parent.pushMatrix();
 		parent.translate(parent.width / 2 - 1, parent.height / 2);
 		parent.image(hudOuterBoundary, 0, 0);
 		parent.popMatrix();
 	}
 
 }
