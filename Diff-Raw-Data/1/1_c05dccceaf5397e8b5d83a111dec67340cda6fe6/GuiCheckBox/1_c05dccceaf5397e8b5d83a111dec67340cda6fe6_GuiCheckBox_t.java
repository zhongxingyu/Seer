 /*
  *  bUpload - a minecraft mod which improves the existing screenshot functionality
  *  Copyright (C) 2013 TheCodingBadgers
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 package uk.codingbadgers.Gui;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.audio.SoundHandler;
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.util.ResourceLocation;
 
 import static org.lwjgl.opengl.GL11.*;
 
 @SideOnly(Side.CLIENT)
 public class GuiCheckBox extends GuiButton {
 
 	/** The current checked state of the check box */
 	private boolean m_checked = false;
 
 	/** The amount of space between the check box and its label */
 	private final static int BOX_LABEL_SPACER = 5;
 
 	/**
 	 * Default check box constructor
 	 * 
 	 * @param id The id of the callback used in actionPerformed.
 	 * @param xPosition The x coordinate of the position of the check box
 	 * @param yPosition The y coordinate of the position of the check box
 	 * @param width The width of the check box
 	 * @param height The height of the check box
 	 * @param label The label of the check box
 	 */
 	public GuiCheckBox(int id, int xPosition, int yPosition, int width, int height, String label) {
 		super(id, xPosition, yPosition, width, height, label);
 	}
 
 	/**
 	 * Returns 0 if the check box is disabled, 1 if the mouse is NOT hovering
 	 * over this check box and 2 if it is hovering over this check box.
 	 * 
 	 * @param isMouseOver if the mouse is over the check box
 	 */
 	@Override
 	protected int func_146114_a(boolean isMouseOver) {
 		if (!field_146124_l) {
 			return 0;
 		}
 
 		if (isMouseOver) {
 			return 2;
 		}
 
 		return 1;
 	}
 
 	/**
 	 * Sets the current checked state of the check box
 	 * 
 	 * @param check True to set the checked state to true, false otherwise
 	 */
 	public void setChecked(boolean check) {
 		m_checked = check;
 	}
 
 	/**
 	 * Get the current checked state of the check box
 	 * 
 	 * @return True if checked, false otherwise
 	 */
 	public boolean getChecked() {
 		return m_checked;
 	}
 
 	/**
 	 * Draws the check box to the screen.
 	 * 
 	 * @param minecraft The minecraft instance
 	 * @param mouseX The x coordinate of the mouse
 	 * @param mouseY The y coordinate of the mouse
 	 */
 	@Override
 	public void func_146112_a(Minecraft minecraft, int mouseX, int mouseY) {
 		if (!field_146125_m) {
 			return;
 		}
 
 		// field_146123_n represents if the mouse is over the check box region
 		this.field_146123_n = mouseX >= this.field_146128_h && mouseY >= this.field_146129_i && mouseX < this.field_146128_h + this.field_146120_f && mouseY < this.field_146129_i + this.field_146121_g;
 		// get the hover state of the mouse and check box
 		final int hoverState = func_146114_a(field_146123_n);
 
 		// work out the local offset into the image atlas
 		final int localYoffset = hoverState == 2 ? 186 : (m_checked ? 146 : 166);
 		final int hoverColor = field_146124_l == false ? -6250336 : hoverState == 2 ? 16777120 : 14737632;
 		final int labelWidth = minecraft.fontRenderer.getStringWidth(field_146126_j);
 		final int checkboxImageSize = 20;
 		final int xOffset = field_146128_h + checkboxImageSize + BOX_LABEL_SPACER + (((field_146120_f - checkboxImageSize - BOX_LABEL_SPACER) / 2) - ((labelWidth) / 2));
 
 		drawString(minecraft.fontRenderer, field_146126_j, xOffset, field_146129_i + (field_146121_g - 8) / 2, hoverColor);
 		minecraft.renderEngine.bindTexture(new ResourceLocation("bUpload:textures/gui/tcb-gui.png"));
 		glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
 		drawTexturedModalRect(field_146128_h, field_146129_i, 0, localYoffset, checkboxImageSize, checkboxImageSize);
 	}
 
 	/**
 	 * Handle a mouse pressed event
 	 * 
 	 * @param handler the current sound handler
 	 */
 	@Override
 	public void func_146113_a(SoundHandler handler) {
 		m_checked = !m_checked;
 		super.func_146113_a(handler);
 	}
 
 	/**
 	 * Gets whether the mouse is currently over the check box.
 	 * 
 	 * @return if the mouse is currently over the check box
 	 * @see {@link GuiButton#func_146115_a()}
 	 */
 	@Override
 	public boolean func_146115_a() {
 		return this.field_146123_n;
 	}
 }
