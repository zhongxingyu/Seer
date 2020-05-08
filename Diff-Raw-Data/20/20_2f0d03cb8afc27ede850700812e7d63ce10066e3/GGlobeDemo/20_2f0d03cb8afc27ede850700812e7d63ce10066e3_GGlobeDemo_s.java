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
 
 
 package es.igosoftware.globe.demo;
 
 
 import java.awt.Color;
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import es.igosoftware.concurrent.GConcurrent;
 import es.igosoftware.experimental.ndimensional.G3DImageMultidimensionalData;
 import es.igosoftware.experimental.ndimensional.GMultidimensionalDataModule;
 import es.igosoftware.experimental.ndimensional.GNetCDFMultidimentionalData;
 import es.igosoftware.experimental.ndimensional.IMultidimensionalData;
 import es.igosoftware.experimental.pointscloud.rendering.GPointsCloudModule;
 import es.igosoftware.experimental.vectorial.GPolygon2DModule;
 import es.igosoftware.globe.GGlobeApplication;
 import es.igosoftware.globe.GHomePositionModule;
 import es.igosoftware.globe.GLayersManagerModule;
 import es.igosoftware.globe.GStatisticsModule;
 import es.igosoftware.globe.IGlobeModule;
 import es.igosoftware.globe.modules.GFullScreenModule;
 import es.igosoftware.globe.modules.view.GAnaglyphViewerModule;
 import es.igosoftware.globe.modules.view.GFlatWorldModule;
 import es.igosoftware.globe.modules.view.GShowLatLonGraticuleModule;
 import es.igosoftware.globe.modules.view.GShowMeasureToolModule;
 import es.igosoftware.globe.modules.view.GShowUTMGraticuleModule;
 import es.igosoftware.globe.view.customView.GCustomView;
 import es.igosoftware.io.GFileLoader;
 import es.igosoftware.io.GFileName;
 import es.igosoftware.io.GHttpLoader;
 import es.igosoftware.io.ILoader;
 import es.igosoftware.io.pointscloud.GPointsCloudFileLoader;
 import es.igosoftware.io.pointscloud.IPointsCloudLoader;
 import es.igosoftware.loading.G3DModel;
 import es.igosoftware.loading.GAsyncObjLoader;
 import es.igosoftware.loading.modelparts.GMaterial;
 import es.igosoftware.loading.modelparts.GModelData;
 import es.igosoftware.loading.modelparts.GModelMesh;
 import es.igosoftware.panoramic.GPanoramic;
 import es.igosoftware.panoramic.GPanoramicLayer;
 import es.igosoftware.scenegraph.G3DModelNode;
 import es.igosoftware.scenegraph.GElevationAnchor;
 import es.igosoftware.scenegraph.GGroupNode;
 import es.igosoftware.scenegraph.GPositionRenderableLayer;
 import es.igosoftware.scenegraph.GTransformationOrder;
 import es.igosoftware.util.GUtils;
 import gov.nasa.worldwind.AnaglyphSceneController;
 import gov.nasa.worldwind.Configuration;
 import gov.nasa.worldwind.avlist.AVKey;
 import gov.nasa.worldwind.geom.Angle;
 import gov.nasa.worldwind.geom.Position;
 import gov.nasa.worldwind.layers.CompassLayer;
 import gov.nasa.worldwind.layers.Layer;
 import gov.nasa.worldwind.layers.LayerList;
 import gov.nasa.worldwind.layers.RenderableLayer;
 import gov.nasa.worldwind.render.Ardor3DModel;
 
 
 public class GGlobeDemo
          extends
             GGlobeApplication {
    private static final long              serialVersionUID = 1L;
 
 
    static {
       Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, AnaglyphSceneController.class.getName());
    }
 
 
    private static IMultidimensionalData[] _multidimentionaldata;
 
 
    public GGlobeDemo() {
       super("en");
    }
 
 
    @Override
    public String getApplicationName() {
       return "Globe Demo";
    }
 
 
    @Override
    public String getApplicationVersion() {
       return "0.1";
    }
 
 
    @Override
    public Image getImageIcon() {
       return GUtils.getImage("globe-icon.png", getClass().getClassLoader());
    }
 
 
    @Override
    protected LayerList getDefaultLayers() {
       final LayerList layers = super.getDefaultLayers();
 
 
       final List<Layer> compasses = layers.getLayersByClass(CompassLayer.class);
       final CompassLayer compass = (CompassLayer) compasses.get(0);
       GUtils.getImage("value_compass_just.png", getClass().getClassLoader());
       compass.setIconFilePath("./bitmaps/value_compass_just.png");
       compass.setIconScale(1.0);
       compass.setShowTilt(false);
 
 
       final RenderableLayer layer = new RenderableLayer();
       layer.setName("Ship Model");
       final gov.nasa.worldwind.render.Ardor3DModel ship = new Ardor3DModel("./data/models/ship.dae", new Position(
                Angle.fromDegrees(-44.0), Angle.fromDegrees(146.6), 1000));
       layer.addRenderable(ship);
 
       layers.add(layer);
 
       // layers.getLayerByName("MS Virtual Earth Aerial").setEnabled(true);
 
       //      layers.add(new OSMMapnikLayer());
 
       // layers.add(new TerrainProfileLayer());
 
       //      layers.add(new GPNOAWMSLayer(GPNOAWMSLayer.ImageFormat.JPEG));
 
       //      layers.add(new GDielmoWMSLayer(GDielmoWMSLayer.ImageFormat.PNG));
 
       final GPositionRenderableLayer caceres3DLayer = createCaceres3DModelLayer();
       layers.add(caceres3DLayer);
 
 
       /*
       try {
          final GPanoramicLayer panoramicLayer = createPanoramicLayer();
          layers.add(panoramicLayer);
       }
       catch (final RuntimeException e) {
          e.printStackTrace();
       }
       */
 
       //      final IconLayer iconLayer = new IconLayer();
       //      final Position iconPos = new Position(Angle.fromDegrees(39.4737), Angle.fromDegrees(-6.3910), 0.0);
       //      final UserFacingIcon icon = new UserFacingIcon(
       //               "/home/oliver/Desktop/GLOB3-Repository/glob3/media/logo/bitmaps/logo32x32.png", iconPos);
       //      iconLayer.addIcon(icon);
       //      layers.add(iconLayer);
 
       //      createVectorialLayer(layers);
 
 
       return layers;
    }
 
 
    private GPositionRenderableLayer createCaceres3DModelLayer() {
       final GPositionRenderableLayer caceres3DLayer = new GPositionRenderableLayer("Caceres 3D Model", true);
       //      caceres3DLayer.setEnabled(false);
 
       GConcurrent.getDefaultExecutor().submit(new Runnable() {
          @Override
          public void run() {
             loadCaceres3DModel(caceres3DLayer);
             //            caceres3DLayer.setEnabled(true);
          }
       });
 
       return caceres3DLayer;
    }
 
 
    private GPanoramicLayer createPanoramicLayer() {
       final GPanoramicLayer panoramicLayer = new GPanoramicLayer("Panoramics");
 
       //      panoramicLayer.addPanoramic(new GPanoramic(panoramicLayer, "Sample Panoramic", "PANOS/Badajoz", 500, new Position(
       //               Angle.fromDegrees(39.4737), Angle.fromDegrees(-6.3910), 0)));
 
       try {
          final ILoader loader = new GFileLoader(GFileName.relative("PANOS"));
          panoramicLayer.addPanoramic(new GPanoramic(panoramicLayer, "Sample Panoramic", loader, GFileName.relative("Barrancos"),
                   500, new Position(Angle.fromDegrees(39.4737), Angle.fromDegrees(-6.3910), 0)));
 
          //panoramicLayer.addPanoramic(new GPanoramic(panoramicLayer, "Sample Panoramic", "data/panoramics/barruecos", 500,
          //         new Position(Angle.fromDegrees(39.4737), Angle.fromDegrees(-6.3910), 0)));
 
          // panoramicLayer.setEnabled(false);
 
          panoramicLayer.addPickListener(new GPanoramicLayer.PickListener() {
             @Override
             public void picked(final GPanoramic pickedPanoramic) {
                if (pickedPanoramic != null) {
                   panoramicLayer.enterPanoramic(pickedPanoramic, (GCustomView) getView());
                }
             }
          });
       }
       catch (final IOException e) {
          logSevere(e);
       }
       return panoramicLayer;
    }
 
 
    private void hackCaceres3DModel(final GModelData model) {
       for (final GModelMesh mesh : model.getMeshes()) {
          GMaterial material = mesh.getMaterial();
 
 
          if (material == null) {
             material = new GMaterial("");
             material._diffuseColor = Color.WHITE;
             mesh.setMaterial(material);
          }
          else {
             if (material.hasTexture()) {
                material._diffuseColor = Color.WHITE;
             }
          }
 
          material._emissiveColor = new Color(0.2f, 0.2f, 0.2f);
       }
    }
 
 
    @Override
    protected IGlobeModule[] getInitialModules() {
       final Position homePosition = new Position(Angle.fromDegrees(39.4737), Angle.fromDegrees(-6.3710), 0);
       final Angle heading = Angle.ZERO;
       final Angle pitch = Angle.fromDegrees(45);
       final double homeElevation = 2000;
       final GHomePositionModule homePositionModule = new GHomePositionModule(homePosition, heading, pitch, homeElevation, true);
 
       final IPointsCloudLoader loader = new GPointsCloudFileLoader(GFileName.relative("data", "pointsclouds"));
       final GPointsCloudModule pointsCloudModule = new GPointsCloudModule(loader);
 
       //      GPointsCloudModule pointsCloudModule = null;
       //      try {
       //         final IPointsCloudLoader loader = new GPointsCloudStreamingLoader("127.0.0.1", 8000);
       //
       //         pointsCloudModule = new GPointsCloudModule(loader);
       //      }
       //      catch (final IOException e) {
       //         e.printStackTrace();
       //      }
 
       return new IGlobeModule[] { homePositionModule, new GLayersManagerModule(), new GPolygon2DModule(),
                new GFullScreenModule(), pointsCloudModule, new GAnaglyphViewerModule(false), new GStatisticsModule(),
                new GFlatWorldModule(), new GShowLatLonGraticuleModule(), new GShowUTMGraticuleModule(),
                new GMultidimensionalDataModule(_multidimentionaldata), new GShowMeasureToolModule() };
    }
 
 
    private void loadCaceres3DModel(final GPositionRenderableLayer layer) {
 
       ILoader loader = null;
       //      loader = new GFileLoader(GFileName.CURRENT_DIRECTORY);
 
       final boolean verbose = true;
       try {
          final boolean debug = false;
          final boolean simulateSlowConnection = false;
          //         loader = new GHttpLoader(new URL("http://localhost/globe-demo/"), GConcurrent.AVAILABLE_PROCESSORS, verbose, debug,
          //                  simulateSlowConnection);
          loader = new GHttpLoader(new URL("http://213.165.81.201:8080/"), GConcurrent.AVAILABLE_PROCESSORS, verbose, debug,
                   simulateSlowConnection);
       }
       catch (final MalformedURLException e1) {
          e1.printStackTrace();
       }
 
       if (loader != null) {
          final GAsyncObjLoader objLoader = new GAsyncObjLoader(loader);
 
          objLoader.load(GFileName.relative("globe-demo-data", "models", "caceres3d.obj"), new GAsyncObjLoader.IHandler() {
             @Override
             public void loadError(final IOException e) {
                logSevere(e);
             }
 
 
             @Override
             public void loaded(final GModelData modelData) {
                hackCaceres3DModel(modelData);
 
                final G3DModel model = new G3DModel(modelData);
                final G3DModelNode caceres3DModelNode = new G3DModelNode("Caceres3D",
                         GTransformationOrder.ROTATION_SCALE_TRANSLATION, model);
 
 
                final GGroupNode caceres3DRootNode = new GGroupNode("Caceres3D root",
                         GTransformationOrder.ROTATION_SCALE_TRANSLATION);
                caceres3DRootNode.setHeading(-90);
                //caceres3DRootNode.setScale(10);
                caceres3DRootNode.addChild(caceres3DModelNode);
 
                layer.addNode(caceres3DRootNode, new Position(Angle.fromDegrees(39.4737), Angle.fromDegrees(-6.3710), 24.7),
                         GElevationAnchor.SEA_LEVEL);
             }
          }, verbose);
       }
    }
 
 
    private static void checkDataDirectory() {
       final File dataDirectory = new File("data");
       if (!dataDirectory.exists()) {
          final String message = "Can't find the directory data\n\n"
                                 + "- Go to http://sourceforge.net/projects/glob3/files_beta/globe-demo/\n"
                                 + "- Download the file data.zip\n" + "- Uncompress the file in the directory "
                                 + new File("data").getAbsolutePath();
          System.out.println(message);
          JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
          System.exit(1);
       }
    }
 
 
    private static void loadMultidimensionalData() {
       try {
          //FIXME: Unused currently auto-detect code works
          //final String[] valueVariablesNames = new String[] { "salt", "temp", "dens" };
          final GNetCDFMultidimentionalData.VectorVariable[] vectorVariables = new GNetCDFMultidimentionalData.VectorVariable[] { new GNetCDFMultidimentionalData.VectorVariable(
                   "Water Velocity", "u", "v") };
 
          final IMultidimensionalData cfData = new GNetCDFMultidimentionalData("data/mackenzie_depth_out_cf.nc", "longitude",
                   "latitude", "zc", "eta", null, vectorVariables, "n", true, true);
 
          final GNetCDFMultidimentionalData.VectorVariable[] vectorVariablesBig = new GNetCDFMultidimentionalData.VectorVariable[] { new GNetCDFMultidimentionalData.VectorVariable(
                   "Wind Velocity", "ueavg", "veavg") };
 
          final IMultidimensionalData cfDataBig = new GNetCDFMultidimentionalData("data/BigData/ramsNZ12_l3.nc", "longitude",
                   "latitude", "level", "pblht", null, vectorVariablesBig, "time", true, true);
 
 
          final Position headPosition = new Position(Angle.fromDegrees(39.4737), Angle.fromDegrees(-6.3710), 0);
          final IMultidimensionalData headData = new G3DImageMultidimensionalData("Mr Head", "data/cthead-8bit", ".png",
                   headPosition, 10, 10, 20);
 
 
         _multidimentionaldata = new IMultidimensionalData[] { cfData, cfDataBig, headData };
          // _multidimentionaldata = new IMultidimensionalData[] { headData };
       }
       catch (final IOException e) {
          e.printStackTrace();
          System.exit(1);
       }
    }
 
 
    @Override
    public void init() {
       super.init();
 
       checkDataDirectory();
 
       //loadMultidimensionalData();
    }
 
 
    public static void main(final String[] args) {
 
       SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
 
             checkDataDirectory();
 
             loadMultidimensionalData();
 
             new GGlobeDemo().openInFrame();
          }
       });
    }
 
 }
