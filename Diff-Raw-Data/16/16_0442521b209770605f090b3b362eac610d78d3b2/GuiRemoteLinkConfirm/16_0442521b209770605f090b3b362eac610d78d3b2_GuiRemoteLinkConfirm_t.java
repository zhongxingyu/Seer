 /**
  * HyperSignsClient - Client-side plugin for extended in-game sign interaction.
  * Copyright (C) 2012, EnderVille.com
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.enderville.hypersigns;
 
 import java.net.URI;
 
import net.minecraft.src.GuiButton;
 import net.minecraft.src.GuiConfirmOpenLink;
 import net.minecraft.src.GuiScreen;
import net.minecraft.src.StringTranslate;
 
 public class GuiRemoteLinkConfirm extends GuiConfirmOpenLink {
 
 	final URI uri;
 
 	public GuiRemoteLinkConfirm(URI uri) {
 		super(new GuiRemoteLinkDummyScreen(uri), uri.toString(), 0);
 		this.uri = uri;
 	}
 
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 * Overridden here because the original method has a visual bug with positioning the buttons.
	 */
	@Override
	public void initGui()
	{
		this.controlList.add(new GuiButton(0, this.width / 2 - 155 + 0, this.height / 6 + 96, 100, 20, this.buttonText1));
		this.controlList.add(new GuiButton(2, this.width / 2 - 155 + 105, this.height / 6 + 96, 100, 20, StringTranslate.getInstance().translateKey("chat.copy")));
		this.controlList.add(new GuiButton(1, this.width / 2 - 155 + 210, this.height / 6 + 96, 100, 20, this.buttonText2));
	}

 	@Override
 	public void func_73945_e() {
 		setClipboardString(uri.toString());
 	}
 
 }
