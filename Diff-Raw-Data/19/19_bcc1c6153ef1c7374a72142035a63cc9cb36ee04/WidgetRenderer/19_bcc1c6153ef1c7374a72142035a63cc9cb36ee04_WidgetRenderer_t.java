 /*
  * [gui4gl] OpenGL game-oriented GUI library
  * 
  * Copyright (C) 2003 Tako Schotanus
  * 
  * This library is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation; either version 2.1 of the License, or (at your
  * option) any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  * Created on Nov 3, 2003
  */
 package org.codejive.gui4gl.themes.blues;
 
 import net.java.games.jogl.GL;
 
 import org.codejive.utils4gl.GLColor;
 import org.codejive.utils4gl.RenderContext;
import org.codejive.utils4gl.textures.Texture;
 import org.codejive.gui4gl.themes.WidgetRendererModel;
 import org.codejive.gui4gl.themes.RenderHelper;
 import org.codejive.gui4gl.widgets.*;
 
 /**
  * @author tako
 * @version $Revision: 224 $
  */
 public class WidgetRenderer implements WidgetRendererModel {
 
 	/* (non-Javadoc)
 	 * @see org.codejive.world3d.gui.WidgetRenderer#initRendering(org.codejive.world3d.gui.Widget, org.codejive.world3d.RenderContext)
 	 */
 	public void initRendering(Widget _widget, RenderContext _context) {
 		// No code necessary
 	}
 
 	public void updateRendering(Widget _widget, RenderContext _context) {
 		// No code necessary
 	}
 
 	/* (non-Javadoc)
 	 * @see org.codejive.gui4gl.themes.WidgetRenderer#render(org.codejive.gui4gl.widgets.AbstractWidget, org.codejive.world3d.RenderContext)
 	 */
 	public void render(Widget _widget, RenderContext _context) {
 		GL gl = _context.getGl();
 
 		GLColor backgroundColor;
 		float fTransparancy;
 		Texture backgroundImage;
 
 		if (_widget.hasFocus()) {
 			backgroundColor = _widget.getFocusedBackgroundColor();
 			fTransparancy = _widget.getFocusedTransparancy();
 			backgroundImage = _widget.getFocusedBackgroundImage();
 		} else {
 			if (_widget.isEnabled()) {
 				backgroundColor = _widget.getBackgroundColor();
 				fTransparancy = _widget.getTransparancy();
 				backgroundImage = _widget.getBackgroundImage();
 			} else {
 				backgroundColor = _widget.getDisabledBackgroundColor();
 				fTransparancy = _widget.getDisabledTransparancy();
 				backgroundImage = _widget.getDisabledBackgroundImage();
 			}
 		}
 		
 		if (backgroundImage == null) {
 			gl.glDisable(GL.GL_TEXTURE_2D);
 		} else {
 			gl.glEnable(GL.GL_TEXTURE_2D);
			backgroundImage.bind();
 		}
 		gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 		gl.glBegin(GL.GL_QUADS);
 
 		// Widget background
		if (backgroundColor != null) {
			gl.glColor4f(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 1.0f - fTransparancy);
		} else {
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f - fTransparancy);
		}
 		RenderHelper.drawRectangle(gl, _widget.getCurrentBounds());
 
 		gl.glEnd();
 		gl.glDisable(GL.GL_BLEND);
 	}
 
 }
 
 /*
  * $Log$
 * Revision 1.8  2004/03/07 18:25:48  tako
 * Fixed problem that backgroundColor was not allowed to be null.
 * The backgroundImage is now properly bound.
 *
  * Revision 1.7  2003/12/05 01:05:11  tako
  * Implemented rendering of enabled/disabled state for widgets.
  * Renamed all caption properties to text properties leaving only one set of
  * properties instead some widgets using text and others caption.
  *
  * Revision 1.6  2003/11/25 16:27:59  tako
  * All code is now subject to the Lesser GPL.
  *
  * Revision 1.5  2003/11/24 16:50:43  tako
  * Implemented updateRendering().
  *
  * Revision 1.4  2003/11/17 10:54:49  tako
  * Added CVS macros for revision and log.
  *
  */
