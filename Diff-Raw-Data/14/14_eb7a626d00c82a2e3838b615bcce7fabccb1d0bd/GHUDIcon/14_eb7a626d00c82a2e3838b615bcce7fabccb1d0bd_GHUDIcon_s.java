 /*
 
  IGO Software SL  -  info@igosoftware.es
 
  http://www.glob3.org
 
 -------------------------------------------------------------------------------
  Copyright (c) 2010, IGO Software SL
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the IGO Software SL nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL IGO Software SL BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -------------------------------------------------------------------------------
 
 */
 
 
 package es.igosoftware.globe.layers.hud;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.opengl.GL;
 
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureCoords;
 import com.sun.opengl.util.texture.TextureIO;
 
 import es.igosoftware.util.GAssert;
 import es.igosoftware.util.GLogger;
 import es.igosoftware.util.GMath;
 import gov.nasa.worldwind.geom.Vec4;
 import gov.nasa.worldwind.render.DrawContext;
 
 
 public class GHUDIcon
          implements
             IHUDElement {
 
    private static final GLogger logger = GLogger.instance();
 
 
    public static enum Position {
       NORTHWEST,
       SOUTHWEST,
       NORTHEAST,
       SOUTHEAST;
    }
 
 
    private String                  _iconFileName;
    private int                     _iconWidth;
    private int                     _iconHeight;
    private final GHUDIcon.Position _position;
    private int                     _borderWidth     = 20;
    private int                     _borderHeight    = 20;
   private float                   _opacity         = 0.7f;
 
    private boolean                 _isEnable        = true;
    private double                  _distanceFromEye = 0;
 
    private Rectangle               _lastScreenBounds;
    private boolean                 _highlighted;
 
    private List<ActionListener>    _actionListeners;
 
 
    public GHUDIcon(final String iconFileName,
                    final GHUDIcon.Position position) {
       GAssert.notNull(iconFileName, "iconFileName");
       GAssert.notNull(position, "position");
 
       _iconFileName = iconFileName;
       _position = position;
    }
 
 
    @Override
    public double getDistanceFromEye() {
       return _distanceFromEye;
    }
 
 
    @Override
    public void pick(final DrawContext dc,
                     final Point pickPoint) {
       //      drawIcon(dc);
    }
 
 
    @Override
    public void render(final DrawContext dc) {
       drawIcon(dc);
    }
 
 
    private void drawIcon(final DrawContext dc) {
       if (_iconFileName == null) {
          return;
       }
 
       final GL gl = dc.getGL();
 
       boolean attribsPushed = false;
       boolean modelviewPushed = false;
       boolean projectionPushed = false;
 
       try {
          gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT
                          | GL.GL_TRANSFORM_BIT | GL.GL_VIEWPORT_BIT | GL.GL_CURRENT_BIT);
          attribsPushed = true;
 
          // Initialize texture if not done yet 
          Texture iconTexture = dc.getTextureCache().get(_iconFileName);
          if (iconTexture == null) {
             initializeTexture(dc);
             iconTexture = dc.getTextureCache().get(_iconFileName);
             if (iconTexture == null) {
                logger.warning("Can't load icon \"" + _iconFileName + "\"");
                return;
             }
          }
 
          gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
          gl.glDisable(GL.GL_DEPTH_TEST);
 
          // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
          // into the GL projection matrix.
          final Rectangle viewport = dc.getView().getViewport();
          gl.glMatrixMode(GL.GL_PROJECTION);
          gl.glPushMatrix();
          projectionPushed = true;
          gl.glLoadIdentity();
          final double maxwh = (_iconWidth > _iconHeight) ? _iconWidth : _iconHeight;
          gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);
 
          gl.glMatrixMode(GL.GL_MODELVIEW);
          gl.glPushMatrix();
          modelviewPushed = true;
          gl.glLoadIdentity();
 
          // Translate and scale
          final float scale = computeScale(viewport);
          final Vec4 locationSW = computeLocation(viewport, scale);
          gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
          // Scale to 0..1 space
          gl.glScalef(scale, scale, 1f);
          gl.glScaled(_iconWidth, _iconHeight, 1d);
 
          _lastScreenBounds = calculateScreenBounds(viewport, locationSW, scale);
 
          if (!dc.isPickingMode()) {
             gl.glColor4f(1, 1, 1, calculateOpacity());
             gl.glEnable(GL.GL_TEXTURE_2D);
             iconTexture.bind();
 
             final TextureCoords texCoords = iconTexture.getImageTexCoords();
             dc.drawUnitQuad(texCoords);
          }
       }
       finally {
          if (projectionPushed) {
             gl.glMatrixMode(GL.GL_PROJECTION);
             gl.glPopMatrix();
          }
          if (modelviewPushed) {
             gl.glMatrixMode(GL.GL_MODELVIEW);
             gl.glPopMatrix();
          }
          if (attribsPushed) {
             gl.glPopAttrib();
          }
       }
    }
 
 
    private float computeScale(final Rectangle viewport) {
       return Math.min(1, (float) viewport.width / _iconWidth) * (_highlighted ? 1.15f : 1f);
    }
 
 
    private Rectangle calculateScreenBounds(final Rectangle viewport,
                                            final Vec4 position,
                                            final float scale) {
       final int iWidth = toInt(_iconWidth * scale);
       final int iHeight = toInt(_iconHeight * scale);
       final int iX = toInt(position.x);
       final int iY = viewport.height - iHeight - toInt(position.y);
       return new Rectangle(iX, iY, iWidth, iHeight);
    }
 
 
    private static int toInt(final double value) {
       return GMath.toInt(Math.round(value));
    }
 
 
    private void initializeTexture(final DrawContext dc) {
       Texture iconTexture = dc.getTextureCache().get(_iconFileName);
       if (iconTexture != null) {
          return;
       }
 
       try {
          InputStream iconStream = getClass().getResourceAsStream(_iconFileName);
          if (iconStream == null) {
             final File iconFile = new File(_iconFileName);
             if (iconFile.exists()) {
                iconStream = new FileInputStream(iconFile);
             }
          }
 
          iconTexture = TextureIO.newTexture(iconStream, true, null);
          iconTexture.bind();
          _iconWidth = iconTexture.getWidth();
          _iconHeight = iconTexture.getHeight();
          dc.getTextureCache().put(_iconFileName, iconTexture);
       }
       catch (final IOException e) {
          throw new RuntimeException(e);
       }
 
       final GL gl = dc.getGL();
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
       gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
 
    }
 
 
    private Vec4 computeLocation(final Rectangle viewport,
                                 final float scale) {
       final double width = _iconWidth;
       final double height = _iconHeight;
 
       final double scaledWidth = scale * width;
       final double scaledHeight = scale * height;
 
       double x = 0;
       double y = 0;
 
       switch (_position) {
          case NORTHEAST:
             x = viewport.getWidth() - scaledWidth - _borderWidth;
             y = viewport.getHeight() - scaledHeight - _borderHeight;
             break;
          case SOUTHEAST:
             x = viewport.getWidth() - scaledWidth - _borderWidth;
             y = 0d + _borderHeight;
             break;
          case NORTHWEST:
             x = 0d + _borderWidth;
             y = viewport.getHeight() - scaledHeight - _borderHeight;
             break;
          case SOUTHWEST:
             x = 0d + _borderWidth;
             y = 0d + _borderHeight;
             break;
       }
 
       return new Vec4(x, y, 0);
    }
 
 
    public float getOpacity() {
       return _opacity;
    }
 
 
    private float calculateOpacity() {
       if (_highlighted) {
         return 1;
       }
       return _opacity;
    }
 
 
    public void setOpacity(final float opacity) {
       _opacity = opacity;
    }
 
 
    public int getBorderWidth() {
       return _borderWidth;
    }
 
 
    public void setBorderWidth(final int borderWidth) {
       _borderWidth = borderWidth;
    }
 
 
    public int getBorderHeight() {
       return _borderHeight;
    }
 
 
    public void setBorderHeight(final int borderHeight) {
       _borderHeight = borderHeight;
    }
 
 
    @Override
    public boolean isEnable() {
       return _isEnable;
    }
 
 
    public void setEnable(final boolean isEnable) {
       _isEnable = isEnable;
    }
 
 
    public String getIconFileName() {
       return _iconFileName;
    }
 
 
    public void setIconFileName(final String iconFileName) {
       _iconFileName = iconFileName;
    }
 
 
    public void setDistanceFromEye(final double distanceFromEye) {
       _distanceFromEye = distanceFromEye;
    }
 
 
    @Override
    public String toString() {
       return "GHUDIcon [iconFileName=" + _iconFileName + ", position=" + _position + "]";
    }
 
 
    @Override
    public Rectangle getLastScreenBounds() {
       return _lastScreenBounds;
    }
 
 
    @Override
    public void setHighlighted(final boolean highlighted) {
       _highlighted = highlighted;
    }
 
 
    @Override
    public boolean hasActionListeners() {
       return (_actionListeners != null) && !_actionListeners.isEmpty();
    }
 
 
    @Override
    public void mouseClicked(final MouseEvent evt) {
       if (_actionListeners != null) {
          for (final ActionListener listener : _actionListeners) {
             final ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, evt.getWhen(), 0);
             listener.actionPerformed(actionEvent);
          }
       }
    }
 
 
    public void removeActionListener(final ActionListener listener) {
       if (_actionListeners == null) {
          return;
       }
 
       _actionListeners.remove(listener);
 
       if (_actionListeners.isEmpty()) {
          _actionListeners = null;
       }
    }
 
 
    public void addActionListener(final ActionListener listener) {
       if (_actionListeners == null) {
          _actionListeners = new ArrayList<ActionListener>(2);
       }
       _actionListeners.add(listener);
    }
 
 
 }
