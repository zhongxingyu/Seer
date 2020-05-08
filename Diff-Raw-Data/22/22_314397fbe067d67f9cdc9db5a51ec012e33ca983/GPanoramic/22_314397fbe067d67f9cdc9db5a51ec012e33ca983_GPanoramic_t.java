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
 
 
 package es.igosoftware.panoramic;
 
 import es.igosoftware.euclid.vector.GVector2D;
 import es.igosoftware.euclid.vector.GVector3D;
 import es.igosoftware.euclid.vector.GVectorUtils;
 import es.igosoftware.euclid.vector.IVector2;
 import es.igosoftware.euclid.vector.IVector3;
 import es.igosoftware.globe.GGlobeApplication;
 import es.igosoftware.io.GFileName;
 import es.igosoftware.io.GIOUtils;
 import es.igosoftware.io.ILoader;
 import es.igosoftware.loading.GDisplayListCache;
 import es.igosoftware.scenegraph.GElevationAnchor;
 import es.igosoftware.util.GAssert;
 import es.igosoftware.util.GHolder;
 import es.igosoftware.util.GUtils;
 import es.igosoftware.util.LRUCache;
 import es.igosoftware.utils.GPanoramicCompiler;
 import es.igosoftware.utils.GTexturesCache;
 import es.igosoftware.utils.GWWUtils;
 import gov.nasa.worldwind.geom.Angle;
 import gov.nasa.worldwind.geom.Box;
 import gov.nasa.worldwind.geom.Extent;
 import gov.nasa.worldwind.geom.Frustum;
 import gov.nasa.worldwind.geom.LatLon;
 import gov.nasa.worldwind.geom.Matrix;
 import gov.nasa.worldwind.geom.Position;
 import gov.nasa.worldwind.geom.Sector;
 import gov.nasa.worldwind.geom.Sphere;
 import gov.nasa.worldwind.geom.Vec4;
 import gov.nasa.worldwind.globes.Globe;
 import gov.nasa.worldwind.layers.Layer;
 import gov.nasa.worldwind.render.DrawContext;
 import gov.nasa.worldwind.render.OrderedRenderable;
 import gov.nasa.worldwind.util.WWMath;
 
 import java.awt.Point;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 import javax.media.opengl.GL;
 
 import com.sun.opengl.util.texture.Texture;
 
 
 public class GPanoramic
          implements
             OrderedRenderable {
 
    private static final int                                                  TILE_THETA_SUBDIVISIONS = 2;
    private static final int                                                  TILE_RHO_SUBDIVISIONS   = 1;
 
    private static final double                                               MIN_PROYECTED_SIZE      = 12;
 
    private static final GDisplayListCache<PanoramicTile>                     QUAD_STRIPS_DISPLAY_LIST_CACHE;
 
 
    static {
       QUAD_STRIPS_DISPLAY_LIST_CACHE = new GDisplayListCache<PanoramicTile>(false) {
          @Override
          protected void beforeRenderingToDisplayList(final PanoramicTile tile,
                                                      final DrawContext dc) {
          }
 
 
          @Override
          protected void renderToDisplayList(final PanoramicTile tile,
                                             final DrawContext dc) {
 
             final GL gl = dc.getGL();
 
             for (final List<Vertex> quadStrip : tile._quadStrips) {
                gl.glBegin(GL.GL_QUAD_STRIP);
 
                for (final Vertex vertex : quadStrip) {
                   final IVector2<?> texCoord = vertex._texCoord;
                   gl.glTexCoord2f((float) texCoord.x(), (float) vertex._texCoord.y());
 
                   final IVector3<?> point = vertex._point;
                   gl.glVertex3f((float) point.x(), (float) point.y(), (float) point.z());
                }
 
                gl.glEnd();
             }
          }
       };
 
    }
 
    private final LRUCache<PanoramicTileKey, PanoramicTile, RuntimeException> _tilesCache             = new LRUCache<PanoramicTileKey, PanoramicTile, RuntimeException>(
                                                                                                               128,
                                                                                                               new LRUCache.ValueFactory<PanoramicTileKey, PanoramicTile, RuntimeException>() {
                                                                                                                  @Override
                                                                                                                  public PanoramicTile create(final PanoramicTileKey key) {
                                                                                                                     return new PanoramicTile(
                                                                                                                              key._sector,
                                                                                                                              key._level,
                                                                                                                              key._row,
                                                                                                                              key._column);
                                                                                                                  }
                                                                                                               });
 
    private final String                                                      _name;
    private final ILoader                                                     _loader;
    private final GFileName                                                   _panoramicFile;
    private final double                                                      _radius;
    private final Position                                                    _position;
    private final double                                                      _headingInDegrees;
 
    private Globe                                                             _lastGlobe;
    private double                                                            _lastVerticalExaggeration;
    private Frustum                                                           _lastFrustum;
 
    private final GElevationAnchor                                            _anchor;
 
    private final List<PanoramicTile>                                         _tiles;
 
    private Matrix                                                            _modelCoordinateOriginTransform;
    private final float[]                                                     _modelViewMatrixArray   = new float[16];
    private Sphere                                                            _boundsInGlobalCoordinates;
    private boolean                                                           _renderWireframe        = false;
    private boolean                                                           _renderNormals          = false;
    private final GPanoramicCompiler.ZoomLevels                               _zoomLevels;
 
    private final List<PanoramicTile>                                         _visibleTiles           = new ArrayList<PanoramicTile>(
                                                                                                               100);
 
    private final int                                                         _maxLevel               = 6;
    private final int                                                         _maxResolutionInPanoramic;
    private int                                                               _currentLevel;
 
    private final Layer                                                       _layer;
    private double                                                            _currentDistanceFromEye;
 
    private boolean                                                           _isHidden;
 
 
    public GPanoramic(final Layer layer,
                      final String name,
                      final ILoader loader,
                      final GFileName panoramicName,
                      final double radius,
                      final Position position) throws IOException {
       this(layer, name, loader, panoramicName, radius, position, 0, GElevationAnchor.SURFACE);
    }
 
 
    public GPanoramic(final Layer layer,
                      final String name,
                      final ILoader loader,
                      final GFileName panoramicName,
                      final double radius,
                      final Position position,
                      final double headingInDegrees,
                      final GElevationAnchor anchor) throws IOException {
       GAssert.notNull(name, "name");
       GAssert.notNull(loader, "loader");
       GAssert.isPositive(radius, "radius");
       GAssert.notNull(position, "position");
       GAssert.notNull(anchor, "anchor");
 
       _layer = layer;
 
       _name = name;
       _loader = loader;
       _panoramicFile = panoramicName;
       _radius = radius;
       _position = position;
       _headingInDegrees = headingInDegrees;
       _anchor = anchor;
 
       _tiles = createTopTiles();
 
       _zoomLevels = readZoomLevels();
 
       _maxResolutionInPanoramic = _zoomLevels.getLevels().size() - 1;
    }
 
 
    public Layer getLayer() {
       return _layer;
    }
 
 
    public String getName() {
       return _name;
    }
 
 
    public Position getPosition() {
       return _position;
    }
 
 
    public double getRadius() {
       return _radius;
    }
 
 
    public Sphere getGlobalBounds() {
       return _boundsInGlobalCoordinates;
    }
 
 
    public double getCurrentDistanceFromEye() {
       return _currentDistanceFromEye;
    }
 
 
    public GElevationAnchor getElevationAnchor() {
       return _anchor;
    }
 
 
    public Globe getGlobe() {
       return _lastGlobe;
    }
 
 
    private List<PanoramicTile> createTopTiles() {
       final List<PanoramicTile> result = new ArrayList<PanoramicTile>(TILE_RHO_SUBDIVISIONS * TILE_THETA_SUBDIVISIONS);
 
       final double deltaRho = 180d / TILE_RHO_SUBDIVISIONS;
       final double deltaTheta = 360d / TILE_THETA_SUBDIVISIONS;
 
       Angle lastLat = Angle.ZERO;
 
       for (int row = 0; row < TILE_RHO_SUBDIVISIONS; row++) {
          Angle lat = lastLat.addDegrees(deltaRho);
          if (lat.degrees + 1d > 180d) {
             lat = Angle.POS180;
          }
 
          Angle lastLon = Angle.ZERO;
 
          for (int col = 0; col < TILE_THETA_SUBDIVISIONS; col++) {
             Angle lon = lastLon.addDegrees(deltaTheta);
             if (lon.degrees + 1d > 360d) {
                lon = Angle.POS360;
             }
 
             result.add(new PanoramicTile(new Sector(lastLat, lat, lastLon, lon), 0, row, col));
 
             lastLon = lon;
          }
 
          lastLat = lat;
       }
 
       return result;
    }
 
 
    private GPanoramicCompiler.ZoomLevels readZoomLevels() throws IOException {
 
       final GFileName fileName = new GFileName(_panoramicFile, GPanoramicCompiler.LEVELS_FILE_NAME);
 
       final GHolder<GPanoramicCompiler.ZoomLevels> result = new GHolder<GPanoramicCompiler.ZoomLevels>(null);
       final GHolder<IOException> exception = new GHolder<IOException>(null);
       final GHolder<Boolean> done = new GHolder<Boolean>(false);
 
       _loader.load(fileName, -1, false, Integer.MAX_VALUE, new ILoader.IHandler() {
          @Override
          public void loaded(final File file,
                             final long bytesLoaded,
                             final boolean completeLoaded) {
 
            System.out.println("Downloading levels.zip");

             if (!completeLoaded) {
                return;
             }
 
 
             ObjectInputStream is = null;
             try {
                is = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
 
                final GPanoramicCompiler.ZoomLevels zoomLevels = (GPanoramicCompiler.ZoomLevels) is.readObject();
 
                result.set(zoomLevels);
             }
             catch (final IOException e) {
                exception.set(e);
             }
             catch (final ClassNotFoundException e) {
                exception.set(new IOException(e));
             }
             catch (final ClassCastException e) {
                exception.set(new IOException(e));
             }
             finally {
                GIOUtils.gentlyClose(is);
             }
 
             done.set(true);
          }
 
 
          @Override
          public void loadError(final IOException e) {
             exception.set(e);
             done.set(true);
          }
       });
 
       while (!done.get()) {
          GUtils.delay(10);
       }
 
       if (exception.hasValue()) {
          throw exception.get();
       }
 
       return result.get();
    }
 
 
    private static double computeSurfaceElevation(final DrawContext dc,
                                                  final LatLon latLon) {
 
       final Vec4 surfacePoint = GGlobeApplication.instance().getTerrain().getSurfacePoint(latLon);
 
       final Globe globe = dc.getGlobe();
 
       if (surfacePoint == null) {
          return globe.getElevation(latLon.latitude, latLon.longitude);
       }
 
       return globe.computePositionFromPoint(surfacePoint).elevation;
    }
 
 
    public void asureModelCoordinateOriginTransform(final DrawContext dc,
                                                    final boolean terrainChanged) {
       if (terrainChanged || (_modelCoordinateOriginTransform == null)) {
          Position position = null;
 
          switch (getElevationAnchor()) {
             case SEA_LEVEL:
                position = _position;
                break;
             case SURFACE:
                final double surfaceElevation = computeSurfaceElevation(dc, _position);
                position = new Position(_position.latitude, _position.longitude, surfaceElevation + _position.elevation);
                break;
          }
 
          final Globe globe = dc.getGlobe();
          final double verticalExaggeration = dc.getVerticalExaggeration();
          _modelCoordinateOriginTransform = GWWUtils.computeModelCoordinateOriginTransform(position, globe, verticalExaggeration);
          _modelCoordinateOriginTransform = _modelCoordinateOriginTransform.multiply(Matrix.fromRotationZ(Angle.fromDegrees(_headingInDegrees)));
       }
    }
 
 
    public boolean isVisible(final DrawContext dc,
                             final boolean terrainChanged) {
       final Sphere bounds = getBoundsInModelCoordinates(terrainChanged);
       if (bounds == null) {
          return true;
       }
 
       final Frustum frustum = dc.getView().getFrustumInModelCoordinates();
 
       final boolean isVisibleInFrustum = frustum.intersects(bounds);
       if (!isVisibleInFrustum) {
          return false;
       }
 
       final double proyectedSize = WWMath.computeSizeInWindowCoordinates(dc, bounds);
       if (proyectedSize < MIN_PROYECTED_SIZE) {
          return false;
       }
 
       return true;
    }
 
 
    private Sphere getBoundsInModelCoordinates(final boolean matrixChanged) {
       if (matrixChanged || (_boundsInGlobalCoordinates == null)) {
          _boundsInGlobalCoordinates = calculateBoundsInModelCoordinates();
       }
 
       return _boundsInGlobalCoordinates;
    }
 
 
    private Sphere calculateBoundsInModelCoordinates() {
       final Vec4 center = Vec4.ZERO;
       final Vec4 back = new Vec4(0, 0, -_radius);
       final Vec4 front = new Vec4(0, 0, +_radius);
       final Vec4 up = new Vec4(0, +_radius, 0);
       final Vec4 down = new Vec4(0, -_radius, 0);
 
       final Vec4[] transformedPoints = GWWUtils.transform(_modelCoordinateOriginTransform, center, back, front, up, down);
 
       return Sphere.createBoundingSphere(transformedPoints);
    }
 
 
    public boolean isTerrainChanged(final DrawContext dc) {
 
       final Globe globe = dc.getGlobe();
       final double verticalExaggeration = dc.getVerticalExaggeration();
 
       final boolean terrainChanged;
 
       final boolean checkViewport = (_anchor == GElevationAnchor.SURFACE);
       if (checkViewport) {
          final Frustum currentFustum = dc.getView().getFrustumInModelCoordinates();
 
          terrainChanged = ((globe != _lastGlobe) || (verticalExaggeration != _lastVerticalExaggeration) || (!currentFustum.equals(_lastFrustum)));
 
          if (terrainChanged) {
             _lastGlobe = globe;
             _lastVerticalExaggeration = verticalExaggeration;
             _lastFrustum = currentFustum;
          }
       }
       else {
          terrainChanged = ((globe != _lastGlobe) || (verticalExaggeration != _lastVerticalExaggeration));
 
          if (terrainChanged) {
             _lastGlobe = globe;
             _lastVerticalExaggeration = verticalExaggeration;
          }
       }
 
       return terrainChanged;
    }
 
 
    public void doRender(final DrawContext dc) {

       if (dc.isPickingMode()) {
         System.out.println("--not rendering because in pickingMode---");
          return;
       }
 
       final boolean terrainChanged = isTerrainChanged(dc);
       asureModelCoordinateOriginTransform(dc, terrainChanged);
 
       if (!isVisible(dc, terrainChanged)) {
          return;
       }
 
 
       calculateDistanceFromEye(dc);
 
 
       final Matrix modelViewMatrix = dc.getView().getModelviewMatrix().multiply(_modelCoordinateOriginTransform);
       GWWUtils.toGLArray(modelViewMatrix, _modelViewMatrixArray);
 
       _visibleTiles.clear();
       _currentLevel = 0;
       final Frustum frustum = dc.getView().getFrustumInModelCoordinates();
       for (final PanoramicTile tile : _tiles) {
          selectVisibleTiles(dc, frustum, terrainChanged, tile, modelViewMatrix);
       }
 
       if (!_visibleTiles.isEmpty()) {
          dc.addOrderedRenderable(this);
       }

    }
 
 
    private void calculateDistanceFromEye(final DrawContext dc) {
       final Vec4 panoramicPoint = GWWUtils.toVec4(_position, dc.getGlobe(), dc.getVerticalExaggeration());
       final Vec4 eyePoint = dc.getView().getCurrentEyePoint();
       final double distance = panoramicPoint.subtract3(eyePoint).getLength3();
       _currentDistanceFromEye = distance;
    }
 
 
    private void selectVisibleTiles(final DrawContext dc,
                                    final Frustum frustum,
                                    final boolean terrainChanged,
                                    final PanoramicTile tile,
                                    final Matrix modelViewMatrix) {
       final Extent extent = tile.getExtent(terrainChanged);
       if ((extent != null) && !extent.intersects(frustum)) {
          return;
       }
 
       //      final Vec4 normal = GWWUtils.transform(GWWUtils.toVec4(tile._normal), _modelCoordinateOriginTransform);
       //
       //      final Vec4 forwardVector = dc.getView().getForwardVector();
       //      final double dot = normal.dot3(forwardVector);
       //      System.out.println("dot=" + dot + ", forwardVector=" + forwardVector);
       //      //      if (dot <= 0) {
       //      //         return;
       //      //      }
 
 
       if ((_currentLevel < _maxLevel) && !tile.atBestResolution() && tile.needToSplit(dc)) {
          _currentLevel++;
          final PanoramicTile[] subtiles = tile.split();
          for (final PanoramicTile child : subtiles) {
             selectVisibleTiles(dc, frustum, terrainChanged, child, modelViewMatrix);
          }
          _currentLevel--;
          return;
       }
 
       _visibleTiles.add(tile);
    }
 
 
    private void renderTiles(final DrawContext dc) {
 
       for (final PanoramicTile tile : _visibleTiles) {
          tile.render(dc);
       }
    }
 
 
    private void renderTilesNormals(final DrawContext dc) {
 
       for (final PanoramicTile tile : _visibleTiles) {
          tile.renderNormal(dc);
       }
 
    }
 
 
    public boolean isRenderWireframe() {
       return _renderWireframe;
    }
 
 
    public void setRenderWireframe(final boolean renderWireframe) {
       _renderWireframe = renderWireframe;
    }
 
 
    public boolean isRenderNormals() {
       return _renderNormals;
    }
 
 
    public void setRenderNormals(final boolean renderNormals) {
       _renderNormals = renderNormals;
    }
 
 
    public void setHidden(final boolean hidden) {
       _isHidden = hidden;
    }
 
 
    public boolean isHidden() {
       return _isHidden;
    }
 
 
    @Override
    public double getDistanceFromEye() {
       return _currentDistanceFromEye;
    }
 
 
    @Override
    public void pick(final DrawContext dc,
                     final Point pickPoint) {
       // do nothing on pick
    }
 
 
    @Override
    public void render(final DrawContext dc) {
       final GL gl = dc.getGL();
 
       gl.glPushAttrib(GL.GL_TEXTURE_BIT | GL.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT /*| GL.GL_HINT_BIT*/);
 
       gl.glMatrixMode(GL.GL_MODELVIEW);
       gl.glPushMatrix();
       gl.glLoadMatrixf(_modelViewMatrixArray, 0);
 
       final double opacity = _layer.getOpacity();
       if ((opacity > 0) && (opacity < 1)) {
          gl.glEnable(GL.GL_BLEND);
          gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
          gl.glColor4d(1, 1, 1, opacity);
       }
 
       try {
          if (_renderWireframe) {
             gl.glEnable(GL.GL_CULL_FACE);
             gl.glCullFace(GL.GL_BACK);
 
             gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
             gl.glColor3f(1, 1, 1);
             gl.glDisable(GL.GL_DEPTH_TEST);
             gl.glDisable(GL.GL_TEXTURE_2D);
             renderTiles(dc);
             gl.glEnable(GL.GL_TEXTURE_2D);
             gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
             gl.glEnable(GL.GL_DEPTH_TEST);
          }
          else {
             gl.glEnable(GL.GL_CULL_FACE);
             gl.glCullFace(GL.GL_BACK);
             gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
 
             gl.glDisable(GL.GL_DEPTH_TEST);
             renderTiles(dc);
          }
 
 
          if (_renderNormals) {
             gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE);
             gl.glColor3f(1, 1, 1);
             gl.glDisable(GL.GL_DEPTH_TEST);
             gl.glDisable(GL.GL_TEXTURE_2D);
             renderTilesNormals(dc);
             gl.glEnable(GL.GL_TEXTURE_2D);
             gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
             gl.glEnable(GL.GL_DEPTH_TEST);
          }
          //doRender(dc);
       }
       finally {
          gl.glDisable(GL.GL_BLEND);
 
          gl.glEnable(GL.GL_DEPTH_TEST);
 
          gl.glPopAttrib();
 
          gl.glPopMatrix();
       }
    }
 
 
    private class PanoramicTile {
 
       private final Sector             _sector;
       private final List<List<Vertex>> _quadStrips;
 
       private int                      _displayList = -1;
 
       private Box                      _extent;
 
       private final IVector3<?>        _center;
       private final IVector3<?>        _normal;
 
       private final int                _level;
       private final int                _row;
       private final int                _column;
 
       private ILoader.IHandler         _handler;
       private URL                      _url;
 
 
       private PanoramicTile(final Sector sector,
                             final int level,
                             final int row,
                             final int column) {
          _sector = sector;
          _level = level;
          _row = row;
          _column = column;
 
          _quadStrips = initializeQuadStrips();
 
          _normal = initializeNormal();
          _center = initializeCenter();
       }
 
 
       private IVector3<?> initializeCenter() {
          final List<IVector3<?>> points = new ArrayList<IVector3<?>>();
          for (final List<Vertex> quadStrip : _quadStrips) {
             for (final Vertex vertex : quadStrip) {
                points.add(vertex._point);
             }
          }
          return GVectorUtils.getAverage3(points);
       }
 
 
       private IVector3<?> initializeNormal() {
          final List<IVector3<?>> normals = new ArrayList<IVector3<?>>();
          for (final List<Vertex> quadStrip : _quadStrips) {
             for (final Vertex vertex : quadStrip) {
                normals.add(vertex._normal);
             }
          }
          return GVectorUtils.getAverage3(normals).normalized();
       }
 
 
       private List<List<Vertex>> initializeQuadStrips() {
          final int TILE_SLICES = (int) Math.round(_sector.getDeltaLon().degrees / 8);
          final int TILE_STACKS = (int) Math.round(_sector.getDeltaLat().degrees / 4);
 
          final List<List<Vertex>> result = new ArrayList<List<Vertex>>(TILE_STACKS);
 
 
          final double initialRho = _sector.getMinLatitude().radians;
          final double initialTheta = _sector.getMinLongitude().radians;
 
          final double deltaRho = _sector.getDeltaLat().radians / TILE_STACKS;
          final double deltaTheta = _sector.getDeltaLon().radians / TILE_SLICES;
 
          final double deltaT = 1.0 / TILE_STACKS;
          final double deltaS = 1.0 / TILE_SLICES;
 
 
          for (int i = 0; i < TILE_STACKS; i++) {
             final double t = i * deltaT;
 
             final double rho = initialRho + (i * deltaRho);
             final double sinRho = Math.sin(rho);
             final double cosRho = Math.cos(rho);
             final double sinRhoPlusDeltaRho = Math.sin(rho + deltaRho);
             final double cosRhoPlusDeltaRho = Math.cos(rho + deltaRho);
 
             final ArrayList<Vertex> quadStrip = new ArrayList<Vertex>(TILE_SLICES + 1);
             result.add(quadStrip);
             for (int j = 0; j <= TILE_SLICES; j++) {
                final double s = j * deltaS;
 
                final double theta = initialTheta + (j * deltaTheta);
                final double sinTheta = Math.sin(theta);
                final double cosTheta = Math.cos(theta);
 
 
                final double x1 = -sinTheta * sinRho;
                final double y1 = cosTheta * sinRho;
                final double z1 = -cosRho;
                final GVector3D point1 = new GVector3D(x1, y1, z1);
                final GVector3D normal1 = point1.negated().normalized();
                quadStrip.add(new Vertex(point1.scale(_radius), normal1, new GVector2D(1.0 - s, t)));
 
 
                final double x2 = -sinTheta * sinRhoPlusDeltaRho;
                final double y2 = cosTheta * sinRhoPlusDeltaRho;
                final double z2 = -cosRhoPlusDeltaRho;
                final GVector3D point2 = new GVector3D(x2, y2, z2);
                final GVector3D normal2 = point2.negated().normalized();
                quadStrip.add(new Vertex(point2.scale(_radius), normal2, new GVector2D(1 - s, t + deltaT)));
             }
          }
 
          return result;
       }
 
 
       private void render(final DrawContext dc) {
          if (_displayList < 0) {
             _displayList = QUAD_STRIPS_DISPLAY_LIST_CACHE.getDisplayList(this, dc, true);
          }
 
          if (_displayList < 0) {
             return;
          }
 
          if ((_url == null) && (_handler == null)) {
             _handler = new ILoader.IHandler() {
                @Override
                public void loaded(final File file,
                                   final long bytesLoaded,
                                   final boolean completeLoaded) {
                   if (!completeLoaded) {
                      return;
                   }
                   try {
                      _url = file.toURI().toURL();
                   }
                   catch (final MalformedURLException e) {
                      e.printStackTrace();
                   }
                   _handler = null;
                }
 
 
                @Override
                public void loadError(final IOException e) {
                   System.err.println("error=" + e);
                   _handler = null;
                }
             };
 
             final GFileName tilePath = new GFileName(_panoramicFile, Integer.toString(_level), _row + "-" + _column + ".jpg");
 
             _loader.load(tilePath, -1, false, _level, _handler);
          }
 
          final Texture texture = (_url == null) ? null : GTexturesCache.getTexture(_url, true);
          if (texture != null) {
             texture.enable();
             texture.bind();
          }
 
          final GL gl = dc.getGL();
          gl.glCallList(_displayList);
 
          if (texture != null) {
             texture.disable();
          }
       }
 
 
       private boolean atBestResolution() {
          return (_level >= _maxResolutionInPanoramic);
       }
 
 
       @Override
       public String toString() {
          return "PanoramicTile [sector=" + _sector + ", level=" + _level + ", cell=" + _row + "-" + _column + "]";
       }
 
 
       private Box getExtent(final boolean terrainChanged) {
          if (terrainChanged || (_extent == null)) {
 
             final List<Vec4> points = new ArrayList<Vec4>();
 
             for (final List<Vertex> quadStrip : _quadStrips) {
                for (final Vertex vertex : quadStrip) {
                   points.add(GWWUtils.transform(_modelCoordinateOriginTransform, GWWUtils.toVec4(vertex._point)));
                }
             }
 
             _extent = Box.computeBoundingBox(points);
          }
 
          return _extent;
       }
 
 
       private void renderNormal(final DrawContext dc) {
          final GL gl = dc.getGL();
 
          gl.glColor3f(1, 0, 0);
 
          gl.glPointSize(4);
          gl.glBegin(GL.GL_POINTS);
          gl.glVertex3d(_center.x(), _center.y(), _center.z());
          gl.glEnd();
 
          gl.glBegin(GL.GL_LINES);
          gl.glVertex3d(_center.x(), _center.y(), _center.z());
 
          final IVector3<?> destination = _center.add(_normal.scale(_radius / 5));
          gl.glVertex3d(destination.x(), destination.y(), destination.z());
          gl.glEnd();
 
          gl.glColor3f(1, 1, 1);
       }
 
 
       private boolean needToSplit(final DrawContext dc) {
          // _extent is already calculated as getExtent() was called before needToSplit()
          final double proyectedSize = WWMath.computeSizeInWindowCoordinates(dc, _extent);
 
          return (proyectedSize > (256.0 * 3.5));
       }
 
 
       private PanoramicTile[] split() {
          final Sector[] sectors = _sector.subdivide();
 
          final PanoramicTile[] subTiles = new PanoramicTile[4];
          subTiles[0] = _tilesCache.get(new PanoramicTileKey(sectors[0], _level + 1, _row * 2 + 1, _column * 2 + 1));
          subTiles[1] = _tilesCache.get(new PanoramicTileKey(sectors[1], _level + 1, _row * 2 + 1, _column * 2 + 0));
          subTiles[2] = _tilesCache.get(new PanoramicTileKey(sectors[2], _level + 1, _row * 2 + 0, _column * 2 + 1));
          subTiles[3] = _tilesCache.get(new PanoramicTileKey(sectors[3], _level + 1, _row * 2 + 0, _column * 2 + 0));
 
          return subTiles;
       }
 
 
       @Override
       public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + getOuterType().hashCode();
          result = prime * result + _column;
          result = prime * result + _level;
          result = prime * result + _row;
          return result;
       }
 
 
       @Override
       public boolean equals(final Object obj) {
          if (this == obj) {
             return true;
          }
          if (obj == null) {
             return false;
          }
          if (getClass() != obj.getClass()) {
             return false;
          }
          final PanoramicTile other = (PanoramicTile) obj;
          if (!getOuterType().equals(other.getOuterType())) {
             return false;
          }
          if (_column != other._column) {
             return false;
          }
          if (_level != other._level) {
             return false;
          }
          if (_row != other._row) {
             return false;
          }
          return true;
       }
 
 
       private Layer getOuterType() {
          return _layer;
       }
 
 
    }
 
 
    private final class PanoramicTileKey {
       private final Sector _sector;
       private final int    _level;
       private final int    _row;
       private final int    _column;
 
 
       private PanoramicTileKey(final Sector sector,
                                final int level,
                                final int row,
                                final int column) {
          _sector = sector;
          _level = level;
          _row = row;
          _column = column;
       }
 
 
       @Override
       public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + getOuterType().hashCode();
          result = prime * result + _column;
          result = prime * result + _level;
          result = prime * result + _row;
          result = prime * result + ((_sector == null) ? 0 : _sector.hashCode());
          return result;
       }
 
 
       @Override
       public boolean equals(final Object obj) {
          if (this == obj) {
             return true;
          }
          if (obj == null) {
             return false;
          }
          if (getClass() != obj.getClass()) {
             return false;
          }
          final PanoramicTileKey other = (PanoramicTileKey) obj;
          if (!getOuterType().equals(other.getOuterType())) {
             return false;
          }
          if (_column != other._column) {
             return false;
          }
          if (_level != other._level) {
             return false;
          }
          if (_row != other._row) {
             return false;
          }
          if (_sector == null) {
             if (other._sector != null) {
                return false;
             }
          }
          else if (!_sector.equals(other._sector)) {
             return false;
          }
          return true;
       }
 
 
       private Layer getOuterType() {
          return _layer;
       }
    }
 
 
    private static class Vertex {
       private final IVector3<?> _point;
       private final IVector3<?> _normal;
       private final IVector2<?> _texCoord;
 
 
       private Vertex(final IVector3<?> point,
                      final IVector3<?> normal,
                      final IVector2<?> texCoord) {
          _point = point;
          _normal = normal.normalized();
          _texCoord = texCoord;
       }
    }
 
 
 }
