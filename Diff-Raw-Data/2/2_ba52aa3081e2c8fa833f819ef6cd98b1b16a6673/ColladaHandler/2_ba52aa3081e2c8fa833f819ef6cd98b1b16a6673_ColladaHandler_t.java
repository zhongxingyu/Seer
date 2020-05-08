 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.jollada.reader;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import de.ailis.gramath.MutableMatrix4d;
 import de.ailis.gramath.MutableVector3d;
 import de.ailis.jollada.builders.CameraBuilder;
 import de.ailis.jollada.builders.CommonEffectProfileBuilder;
 import de.ailis.jollada.builders.CommonEffectTechniqueBuilder;
 import de.ailis.jollada.builders.CommonNewParamBuilder;
 import de.ailis.jollada.builders.EffectInstanceBuilder;
 import de.ailis.jollada.builders.GeometryBuilder;
 import de.ailis.jollada.builders.ImageSourceBuilder;
 import de.ailis.jollada.builders.LightBuilder;
 import de.ailis.jollada.builders.LightSourceBuilder;
 import de.ailis.jollada.builders.MaterialBuilder;
 import de.ailis.jollada.builders.MeshBuilder;
 import de.ailis.jollada.builders.OrthographicBuilder;
 import de.ailis.jollada.builders.PerspectiveBuilder;
 import de.ailis.jollada.builders.PolyListBuilder;
 import de.ailis.jollada.builders.PolygonsBuilder;
 import de.ailis.jollada.builders.PrimitivesBuilder;
 import de.ailis.jollada.builders.ProjectionBuilder;
 import de.ailis.jollada.builders.TrianglesBuilder;
 import de.ailis.jollada.model.Accessor;
 import de.ailis.jollada.model.Animation;
 import de.ailis.jollada.model.AnimationBehavior;
 import de.ailis.jollada.model.AnimationChannel;
 import de.ailis.jollada.model.AnimationLibrary;
 import de.ailis.jollada.model.AnimationSampler;
 import de.ailis.jollada.model.BRDFShader;
 import de.ailis.jollada.model.BlinnShader;
 import de.ailis.jollada.model.CameraInstance;
 import de.ailis.jollada.model.CameraLibrary;
 import de.ailis.jollada.model.ColorAttribute;
 import de.ailis.jollada.model.CommonSourceTechnique;
 import de.ailis.jollada.model.ConstantShader;
 import de.ailis.jollada.model.DataFlowParam;
 import de.ailis.jollada.model.DataFlowParams;
 import de.ailis.jollada.model.DataFlowSource;
 import de.ailis.jollada.model.DiffuseShader;
 import de.ailis.jollada.model.Document;
 import de.ailis.jollada.model.Effect;
 import de.ailis.jollada.model.EffectLibrary;
 import de.ailis.jollada.model.Element;
 import de.ailis.jollada.model.Filter;
 import de.ailis.jollada.model.FloatArray;
 import de.ailis.jollada.model.FloatAttribute;
 import de.ailis.jollada.model.FloatParam;
 import de.ailis.jollada.model.FloatValue;
 import de.ailis.jollada.model.GeometryInstance;
 import de.ailis.jollada.model.GeometryLibrary;
 import de.ailis.jollada.model.Image;
 import de.ailis.jollada.model.ImageInstance;
 import de.ailis.jollada.model.ImageLibrary;
 import de.ailis.jollada.model.IntList;
 import de.ailis.jollada.model.LambertShader;
 import de.ailis.jollada.model.LightInstance;
 import de.ailis.jollada.model.LightLibrary;
 import de.ailis.jollada.model.LookAtTransform;
 import de.ailis.jollada.model.MaterialInstance;
 import de.ailis.jollada.model.MaterialLibrary;
 import de.ailis.jollada.model.MatrixTransform;
 import de.ailis.jollada.model.NameArray;
 import de.ailis.jollada.model.Node;
 import de.ailis.jollada.model.NodeType;
 import de.ailis.jollada.model.Param;
 import de.ailis.jollada.model.PhongShader;
 import de.ailis.jollada.model.RGBAColor;
 import de.ailis.jollada.model.RGBColor;
 import de.ailis.jollada.model.RotateTransform;
 import de.ailis.jollada.model.Sampler2DParam;
 import de.ailis.jollada.model.ScaleTransform;
 import de.ailis.jollada.model.Scene;
 import de.ailis.jollada.model.Shader;
 import de.ailis.jollada.model.SharedInput;
 import de.ailis.jollada.model.SkewTransform;
 import de.ailis.jollada.model.Texture;
 import de.ailis.jollada.model.TranslateTransform;
 import de.ailis.jollada.model.UnsharedInput;
 import de.ailis.jollada.model.Vertices;
 import de.ailis.jollada.model.VisualScene;
 import de.ailis.jollada.model.VisualSceneInstance;
 import de.ailis.jollada.model.VisualSceneLibrary;
 import de.ailis.jollada.model.Wrap;
 
 
 /**
  * SAX Parser Handler for reading a Document XML file.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 
 public class ColladaHandler extends DefaultHandler
 {
     /** The current parser mode */
     private ParserMode mode = ParserMode.ROOT;
 
     /** The parser mode stack */
     private final Stack<ParserMode> modeStack = new Stack<ParserMode>();
 
     /** String Builder for building a string from element content */
     private StringBuilder stringBuilder;
 
     /** The asset */
     private final Document document;
 
     /** The current image */
     private Image image;
 
     /** The current effect */
     private Effect effect;
 
     /** The current shading information */
     private Shader shader;
 
     /** The current color or texture */
     private ColorAttribute colorOrTexture;
 
     /** The chunk float reader */
     private ChunkFloatReader chunkFloatReader;
 
     /** The chunk string reader */
     private ChunkStringReader chunkStringReader;
 
     /** The chunk int reader */
     private ChunkIntReader chunkIntReader;
 
     /** The current vertices */
     private Vertices vertices;
 
     /** The int array builder */
     private List<Integer> intArrayBuilder;
 
     /** The current accessor */
     private Accessor accessor;
 
     /** The current profile param */
     private Param profileParam;
 
     /** The current image library. */
     private ImageLibrary imageLibrary;
 
     /** The current material library. */
     private MaterialLibrary materialLibrary;
 
     /** The current material builder. */
     private MaterialBuilder materialBuilder;
 
     /** The current effect instance builder. */
     private EffectInstanceBuilder effectInstanceBuilder;
 
     /** The current effect profile builder. */
     private CommonEffectProfileBuilder commonEffectProfileBuilder;
 
     /** The current new_param builder. */
     private CommonNewParamBuilder commonNewParamBuilder;
 
     /** The current RGBA color. */
     private RGBAColor rgbaColor;
 
     /** The current float value. */
     private FloatValue floatValue;
 
     /** The current float attrib. */
     private FloatAttribute floatAttrib;
 
     /** The current effect library. */
     private EffectLibrary effectLibrary;
 
     /** The current geometry library. */
     private GeometryLibrary geometryLibrary;
 
     /** The current geometry builder. */
     private GeometryBuilder geometryBuilder;
 
     /** The current mesh builder. */
     private MeshBuilder meshBuilder;
 
     /** The current float array. */
     private FloatArray floatArray;
 
     /** The current name array. */
     private NameArray nameArray;
 
     /** The current triangles builder. */
     private TrianglesBuilder trianglesBuilder;
 
     /** The current data source. */
     private DataFlowSource dataSource;
 
     /** The current image source builder. */
     private ImageSourceBuilder imageSourceBuilder;
 
     /** The current common effect technique builder. */
     private CommonEffectTechniqueBuilder commonEffectTechniqueBuilder;
 
     /** The current camera builder. */
     private CameraBuilder cameraBuilder;
 
     /** The current perspective builder. */
     private PerspectiveBuilder perspectiveBuilder;
 
     /** The current projection builder. */
     private ProjectionBuilder projectionBuilder;
 
     /** The current camera library. */
     private CameraLibrary cameraLibrary;
 
     /** The current orthographic builder. */
     private OrthographicBuilder orthographicBuilder;
 
     /** The current light library. */
     private LightLibrary lightLibrary;
 
     /** The current light builder. */
     private LightBuilder lightBuilder;
 
     /** Ther current light source builder. */
     private LightSourceBuilder lightSourceBuilder;
 
     /** The current RGB color. */
     private RGBColor rgbColor;
 
     /** The current visual scene library. */
     private VisualSceneLibrary visualSceneLibrary;
 
     /** The current visual scene. */
     private VisualScene visualScene;
 
     /** The current scene node. */
     private Node node;
 
     /** The current geometry instance. */
     private GeometryInstance geometryInstance;
 
     /** The current material instance. */
     private MaterialInstance materialInstance;
 
     /** The current polylist builder. */
     private PolyListBuilder polyListBuilder;
 
     /** The current primitives builder. */
     private PrimitivesBuilder primitivesBuilder;
 
     /** The current polygons builder. */
     private PolygonsBuilder polygonsBuilder;
 
     /** The current animation library. */
     private AnimationLibrary animationLibrary;
 
     /** The current animation. */
     private Animation animation;
 
     /** The current animation sampler. */
     private AnimationSampler sampler;
 
 
     /**
      * Constructs a new parser.
      */
 
     public ColladaHandler()
     {
         this.document = new Document();
     }
 
 
     /**
      * @see DefaultHandler#startElement(String, String, String, Attributes)
      */
 
     @Override
     public void startElement(final String uri, final String localName,
             final String qName, final Attributes attributes)
             throws SAXException
     {
         try
         {
             if (localName.equals("extra"))
             {
                 enterElement(ParserMode.EXTRA);
                 return;
             }
 
             switch (this.mode)
             {
                 case ROOT:
                     if (localName.equals("COLLADA"))
                         enterCOLLADA(attributes);
                     break;
 
                 case COLLADA:
                     if (localName.equals("library_images"))
                         enterLibraryImages(attributes);
                     else if (localName.equals("library_materials"))
                         enterLibraryMaterials(attributes);
                     else if (localName.equals("library_effects"))
                         enterLibraryEffects(attributes);
                     else if (localName.equals("library_geometries"))
                         enterLibraryGeometries(attributes);
                     else if (localName.equals("library_cameras"))
                         enterLibraryCameras(attributes);
                     else if (localName.equals("library_lights"))
                         enterLibraryLights(attributes);
                     else if (localName.equals("library_visual_scenes"))
                         enterLibraryVisualScenes(attributes);
                     else if (localName.equals("library_animations"))
                         enterLibraryAnimations(attributes);
                     else if (localName.equals("scene")) enterScene();
                     break;
 
                 case LIBRARY_IMAGES:
                     if (localName.equals("image")) enterImage(attributes);
                     break;
 
                 case IMAGE:
                     if (localName.equals("init_from"))
                         enterImageInitFrom(attributes);
                     break;
 
                 case IMAGE_INIT_FROM:
                     if (localName.equals("ref"))
                         enterImageInitFromRef();
                     break;
 
                 case LIBRARY_MATERIALS:
                     if (localName.equals("material"))
                         enterMaterial(attributes);
                     break;
 
                 case MATERIAL:
                     if (localName.equals("instance_effect"))
                         enterInstanceEffect(attributes);
                     break;
 
                 case LIBRARY_EFFECTS:
                     if (localName.equals("effect")) enterEffect(attributes);
                     break;
 
                 case EFFECT:
                     if (localName.equals("profile_COMMON"))
                         enterProfileCommon(attributes);
                     break;
 
                 case PROFILE_COMMON:
                     if (localName.equals("technique"))
                         enterTechniqueCommon(attributes);
                     else if (localName.equals("newparam"))
                         enterCommonNewParam(attributes);
                     break;
 
                 case NEWPARAM:
                     if (localName.equals("sampler2D"))
                         enterSampler2DParam();
                     else if (localName.equals("float"))
                         enterFloatParam();
                     else if (localName.equals("semantic"))
                         enterParamSemantic();
                     break;
 
                 case SAMPLER2D:
                     if (localName.equals("minfilter"))
                         enterSampler2DMinFilter();
                     else if (localName.equals("magfilter"))
                         enterSampler2DMagFilter();
                     else if (localName.equals("wrap_s"))
                         enterSampler2DWrapS();
                     else if (localName.equals("wrap_t"))
                         enterSampler2DWrapT();
                     else if (localName.equals("instance_image"))
                         enterInstanceImage(attributes);
                     break;
 
                 case TECHNIQUE_COMMON:
                     if (localName.equals("phong"))
                         enterPhong();
                     else if (localName.equals("blinn"))
                         enterBlinn();
                     else if (localName.equals("constant"))
                         enterConstant();
                     else if (localName.equals("lambert"))
                         enterLambert();
                     break;
 
                 case PHONG:
                 case BLINN:
                 case CONSTANT:
                 case LAMBERT:
                     if (localName.equals("emission"))
                         enterElement(ParserMode.EMISSION);
                     else if (localName.equals("ambient"))
                         enterElement(ParserMode.AMBIENT);
                     else if (localName.equals("diffuse"))
                         enterElement(ParserMode.DIFFUSE);
                     else if (localName.equals("specular"))
                         enterElement(ParserMode.SPECULAR);
                     else if (localName.equals("reflective"))
                         enterElement(ParserMode.REFLECTIVE);
                     else if (localName.equals("transparent"))
                         enterElement(ParserMode.TRANSPARENT);
                     else if (localName.equals("reflectivity"))
                         enterElement(ParserMode.REFLECTIVITY);
                     else if (localName.equals("shininess"))
                         enterElement(ParserMode.SHININESS);
                     else if (localName.equals("transparency"))
                         enterElement(ParserMode.TRANSPARENCY);
                     else if (localName.equals("index_of_refraction"))
                         enterElement(ParserMode.INDEX_OF_REFRACTION);
                     break;
 
                 case REFLECTIVITY:
                 case TRANSPARENCY:
                 case SHININESS:
                 case INDEX_OF_REFRACTION:
                     if (localName.equals("float")) enterFloat(attributes);
                     break;
 
                 case EMISSION:
                 case AMBIENT:
                 case DIFFUSE:
                 case SPECULAR:
                 case REFLECTIVE:
                 case TRANSPARENT:
                     if (localName.equals("color"))
                         enterShadingColor(attributes);
                     else if (localName.equals("texture"))
                         enterTexture(attributes);
                     break;
 
                 case LIBRARY_GEOMETRIES:
                     if (localName.equals("geometry"))
                         enterGeometry(attributes);
                     break;
 
                 case GEOMETRY:
                     if (localName.equals("mesh")) enterMesh();
                     break;
 
                 case MESH:
                     if (localName.equals("source"))
                         enterMeshDataSource(attributes);
                     else if (localName.equals("vertices"))
                         enterVertices(attributes);
                     else if (localName.equals("triangles"))
                         enterTriangles(attributes);
                     else if (localName.equals("polylist"))
                         enterPolyList(attributes);
                     else if (localName.equals("polygons"))
                         enterPolygons(attributes);
                     break;
 
                 case MESH_DATA_SOURCE:
                 case ANIMATION_DATA_SOURCE:
                     if (localName.equals("float_array"))
                         enterFloatArray(attributes);
                     else if (localName.equals("Name_array"))
                         enterNameArray(attributes);
                     if (localName.equals("technique_common"))
                         enterElement(ParserMode.SOURCE_TECHNIQUE_COMMON);
                     break;
 
                 case SOURCE_TECHNIQUE_COMMON:
                     if (localName.equals("accessor"))
                         enterAccessor(attributes);
                     break;
 
                 case ACCESSOR:
                     if (localName.equals("param"))
                         enterParam(attributes);
                     break;
 
                 case VERTICES:
                     if (localName.equals("input"))
                         enterVerticesInput(attributes);
                     break;
 
                 case POLYGONS:
                     if (localName.equals("input"))
                         enterPrimitivesInput(attributes);
                     else if (localName.equals("p"))
                         enterPolygonsP();
                     break;
 
                 case POLYLIST:
                     if (localName.equals("input"))
                         enterPrimitivesInput(attributes);
                     else if (localName.equals("vcount"))
                         enterPolyListVcount();
                     else if (localName.equals("p"))
                         enterPolyListP();
                     break;
 
                 case TRIANGLES:
                     if (localName.equals("input"))
                         enterPrimitivesInput(attributes);
                     else if (localName.equals("p"))
                         enterTrianglesP();
                     break;
 
                 case LIBRARY_ANIMATIONS:
                     if (localName.equals("animation"))
                         enterAnimation(attributes);
                     break;
 
 
                 case ANIMATION:
                     if (localName.equals("animation"))
                         enterAnimation(attributes);
                     else if (localName.equals("source"))
                         enterAnimationDataSource(attributes);
                     else if (localName.equals("sampler"))
                         enterSampler(attributes);
                     else if (localName.equals("channel"))
                         enterChannel(attributes);
                     break;
 
                 case SAMPLER:
                     if (localName.equals("input"))
                         enterSamplerInput(attributes);
                     break;
 
                 case LIBRARY_LIGHTS:
                     if (localName.equals("light")) enterLight(attributes);
                     break;
 
                 case LIGHT:
                     if (localName.equals("technique_common"))
                         enterElement(ParserMode.LIGHT_TECHNIQUE_COMMON);
                     break;
 
                 case LIGHT_TECHNIQUE_COMMON:
                     if (localName.equals("directional"))
                         enterLightSource(ParserMode.LIGHT_DIRECTIONAL);
                     else if (localName.equals("point"))
                         enterLightSource(ParserMode.LIGHT_POINT);
                     else if (localName.equals("ambient"))
                         enterLightSource(ParserMode.LIGHT_AMBIENT);
                     else if (localName.equals("spot"))
                         enterLightSource(ParserMode.LIGHT_SPOT);
                     break;
 
                 case LIGHT_AMBIENT:
                 case LIGHT_DIRECTIONAL:
                     if (localName.equals("color")) enterLightColor(attributes);
                     break;
 
                 case LIGHT_POINT:
                     if (localName.equals("color"))
                         enterLightColor(attributes);
                     else if (localName.equals("constant_attenuation"))
                         enterLightFloatValue(attributes,
                             ParserMode.CONSTANT_ATTENUATION);
                     else if (localName.equals("linear_attenuation"))
                         enterLightFloatValue(attributes,
                             ParserMode.LINEAR_ATTENUATION);
                     else if (localName.equals("quadratic_attenuation"))
                         enterLightFloatValue(attributes,
                             ParserMode.QUADRATIC_ATTENUATION);
                     break;
 
                 case LIGHT_SPOT:
                     if (localName.equals("color"))
                         enterLightColor(attributes);
                     else if (localName.equals("falloff_angle"))
                         enterLightFloatValue(attributes,
                             ParserMode.FALLOFF_ANGLE);
                     else if (localName.equals("falloff_exponent"))
                         enterLightFloatValue(attributes,
                             ParserMode.FALLOFF_EXPONENT);
                     else if (localName.equals("constant_attenuation"))
                         enterLightFloatValue(attributes,
                             ParserMode.CONSTANT_ATTENUATION);
                     else if (localName.equals("linear_attenuation"))
                         enterLightFloatValue(attributes,
                             ParserMode.LINEAR_ATTENUATION);
                     else if (localName.equals("quadratic_attenuation"))
                         enterLightFloatValue(attributes,
                             ParserMode.QUADRATIC_ATTENUATION);
                     break;
 
                 case LIBRARY_CAMERAS:
                     if (localName.equals("camera")) enterCamera(attributes);
                     break;
 
                 case CAMERA:
                     if (localName.equals("optics"))
                         enterElement(ParserMode.OPTICS);
                     break;
 
                 case OPTICS:
                     if (localName.equals("technique_common"))
                         enterElement(ParserMode.OPTICS_TECHNIQUE_COMMON);
                     break;
 
                 case OPTICS_TECHNIQUE_COMMON:
                     if (localName.equals("perspective"))
                         enterPerspective();
                     else if (localName.equals("orthographic"))
                         enterOrthographic();
                     break;
 
                 case PERSPECTIVE:
                 case ORTHOGRAPHIC:
                     if (localName.equals("xfov"))
                         enterProjectionValue(ParserMode.XFOV, attributes);
                     else if (localName.equals("yfov"))
                         enterProjectionValue(ParserMode.YFOV, attributes);
                     else if (localName.equals("xmag"))
                         enterProjectionValue(ParserMode.XMAG, attributes);
                     else if (localName.equals("ymag"))
                         enterProjectionValue(ParserMode.YMAG, attributes);
                     else if (localName.equals("aspect_ratio"))
                         enterProjectionValue(ParserMode.ASPECT_RATIO,
                             attributes);
                     else if (localName.equals("znear"))
                         enterProjectionValue(ParserMode.ZNEAR, attributes);
                     else if (localName.equals("zfar"))
                         enterProjectionValue(ParserMode.ZFAR, attributes);
                     break;
 
                 case LIBRARY_VISUAL_SCENES:
                     if (localName.equals("visual_scene"))
                         enterVisualScene(attributes);
                     break;
 
                 case VISUAL_SCENE:
                     if (localName.equals("node"))
                         enterNode(attributes);
                     break;
 
                 case NODE:
                     if (localName.equals("lookat"))
                         enterLookAt(attributes);
                     else if (localName.equals("matrix"))
                         enterMatrix(attributes);
                     else if (localName.equals("rotate"))
                         enterRotate(attributes);
                     else if (localName.equals("scale"))
                         enterScale(attributes);
                     else if (localName.equals("skew"))
                         enterSkew(attributes);
                     else if (localName.equals("translate"))
                         enterTranslate(attributes);
                     else if (localName.equals("node"))
                         enterNode(attributes);
                     else if (localName.equals("instance_geometry"))
                         enterInstanceGeometry(attributes);
                     else if (localName.equals("instance_light"))
                         enterInstanceLight(attributes);
                     else if (localName.equals("instance_camera"))
                         enterInstanceCamera(attributes);
                     break;
 
                 case INSTANCE_GEOMETRY:
                     if (localName.equals("bind_material"))
                         enterElement(ParserMode.BIND_MATERIAL);
                     break;
 
                 case BIND_MATERIAL:
                     if (localName.equals("param"))
                         enterMaterialBindParam(attributes);
                     else if (localName.equals("technique_common"))
                         enterElement(ParserMode.BIND_MATERIAL_TECHNIQUE_COMMON);
                     break;
 
                 case BIND_MATERIAL_TECHNIQUE_COMMON:
                     if (localName.equals("instance_material"))
                         enterInstanceMaterial(attributes);
                     break;
 
                 case SCENE:
                     if (localName.equals("instance_visual_scene"))
                         enterInstanceVisualScene(attributes);
                     break;
 
                 default:
                     // Ignored
             }
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(e.toString(), e);
         }
     }
 
 
     /**
      * @see DefaultHandler#endElement(String, String, String)
      */
 
     @Override
     public void endElement(final String uri, final String localName,
             final String qName) throws SAXException
     {
         // Ignore element when it is not the one we are currently watching
         if (!localName.equals(this.mode.getTagName())) return;
 
         switch (this.mode)
         {
             case IMAGE_INIT_FROM_REF:
                 leaveImageInitFromRef();
                 break;
 
             case IMAGE_INIT_FROM:
                 leaveImageInitFrom();
                 break;
 
             case IMAGE:
                 leaveImage();
                 break;
 
             case LIBRARY_IMAGES:
                 leaveLibraryImages();
                 break;
 
             case INSTANCE_EFFECT:
                 leaveInstanceEffect();
                 break;
 
             case MATERIAL:
                 leaveMaterial();
                 break;
 
             case LIBRARY_MATERIALS:
                 leaveLibraryMaterials();
                 break;
 
             case SHADING_COLOR:
                 leaveShadingColor();
                 break;
 
             case FLOAT:
                 leaveFloat();
                 break;
 
             case SHININESS:
             case REFLECTIVITY:
             case TRANSPARENCY:
             case INDEX_OF_REFRACTION:
                 leaveFloatOrParam();
                 break;
 
             case EMISSION:
             case AMBIENT:
             case DIFFUSE:
             case SPECULAR:
             case REFLECTIVE:
             case TRANSPARENT:
                 leaveColorOrTexture();
                 break;
 
             case PHONG:
             case BLINN:
             case CONSTANT:
             case LAMBERT:
                 leaveShader();
                 break;
 
             case TECHNIQUE_COMMON:
                 leaveTechniqueCommon();
                 break;
 
             case SAMPLER2D_WRAP_S:
                 leaveSampler2DWrapS();
                 break;
 
             case SAMPLER2D_WRAP_T:
                 leaveSampler2DWrapT();
                 break;
 
             case SAMPLER2D_MAGFILTER:
                 leaveSampler2DMagFilter();
                 break;
 
             case SAMPLER2D_MINFILTER:
                 leaveSampler2DMinFilter();
                 break;
 
             case FLOAT_PARAM:
                 leaveFloatParam();
                 break;
 
             case PARAM_SEMANTIC:
                 leaveParamSemantic();
                 break;
 
             case NEWPARAM:
                 leaveNewParam();
                 break;
 
             case PROFILE_COMMON:
                 leaveProfileCommon();
                 break;
 
             case EFFECT:
                 leaveEffect();
                 break;
 
             case LIBRARY_EFFECTS:
                 leaveLibraryEffects();
                 break;
 
             case FLOAT_ARRAY:
                 leaveFloatArray();
                 break;
 
             case NAME_ARRAY:
                 leaveNameArray();
                 break;
 
             case ACCESSOR:
                 leaveAccessor();
                 break;
 
             case MESH_DATA_SOURCE:
                 leaveMeshDataSource();
                 break;
 
             case VERTICES:
                 leaveVertices();
                 break;
 
             case TRIANGLES_P:
                 leaveTrianglesP();
                 break;
 
             case TRIANGLES:
                 leaveTriangles();
                 break;
 
             case POLYLIST_VCOUNT:
                 leavePolyListVcount();
                 break;
 
             case POLYLIST_P:
                 leavePolyListP();
                 break;
 
             case POLYLIST:
                 leavePolyList();
                 break;
 
             case POLYGONS_P:
                 leavePolygonsP();
                 break;
 
             case POLYGONS:
                 leavePolygons();
                 break;
 
             case MESH:
                 leaveMesh();
                 break;
 
             case GEOMETRY:
                 leaveGeometry();
                 break;
 
             case LIBRARY_GEOMETRIES:
                 leaveLibraryGeometries();
                 break;
 
             case ANIMATION_DATA_SOURCE:
                 leaveAnimationDataSource();
                 break;
 
             case SAMPLER:
                 leaveSampler();
                 break;
 
             case ANIMATION:
                 leaveAnimation();
                 break;
 
             case LIBRARY_ANIMATIONS:
                 leaveLibraryAnimations();
                 break;
 
             case LIGHT_COLOR:
                 leaveLightColor();
                 break;
 
             case FALLOFF_ANGLE:
                 leaveFalloffAngle();
                 break;
 
             case FALLOFF_EXPONENT:
                 leaveFalloffExponent();
                 break;
 
             case CONSTANT_ATTENUATION:
                 leaveConstantAttenuation();
                 break;
 
             case LINEAR_ATTENUATION:
                 leaveLinearAttenuation();
                 break;
 
             case QUADRATIC_ATTENUATION:
                 leaveQuadraticAttenuation();
                 break;
 
             case LIGHT_AMBIENT:
                 leaveAmbient();
                 break;
 
             case LIGHT_DIRECTIONAL:
                 leaveDirectional();
                 break;
 
             case LIGHT_POINT:
                 leavePoint();
                 break;
 
             case LIGHT_SPOT:
                 leaveSpot();
                 break;
 
             case LIGHT:
                 leaveLight();
                 break;
 
             case LIBRARY_LIGHTS:
                 leaveLibraryLights();
                 break;
 
             case XFOV:
                 leaveXfov();
                 break;
 
             case YFOV:
                 leaveYfov();
                 break;
 
             case XMAG:
                 leaveXMag();
                 break;
 
             case YMAG:
                 leaveYMag();
                 break;
 
             case ASPECT_RATIO:
                 leaveAspectRatio();
                 break;
 
             case ZFAR:
                 leaveZfar();
                 break;
 
             case ZNEAR:
                 leaveZnear();
                 break;
 
             case PERSPECTIVE:
             case ORTHOGRAPHIC:
                 leaveProjection();
                 break;
 
             case CAMERA:
                 leaveCamera();
                 break;
 
             case LIBRARY_CAMERAS:
                 leaveLibraryCameras();
                 break;
 
 
             case INSTANCE_MATERIAL:
                 leaveInstanceMaterial();
                 break;
 
             case LOOKAT:
             case MATRIX:
             case ROTATE:
             case SCALE:
             case SKEW:
             case TRANSLATE:
                 leaveTransform();
                 break;
 
             case INSTANCE_GEOMETRY:
                 leaveInstanceGeometry();
                 break;
 
             case NODE:
                 leaveNode();
                 break;
 
             case VISUAL_SCENE:
                 leaveVisualScene();
                 break;
 
             case LIBRARY_VISUAL_SCENES:
                 leaveLibraryVisualScenes();
                 break;
 
             default:
                 leaveElement();
         }
     }
 
 
     /**
      * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
      */
 
     @Override
     public void characters(final char[] ch, final int start, final int length)
             throws SAXException
     {
         switch (this.mode)
         {
             case XFOV:
             case YFOV:
             case XMAG:
             case YMAG:
             case ASPECT_RATIO:
             case ZNEAR:
             case ZFAR:
             case LIGHT_COLOR:
             case SHADING_COLOR:
             case FLOAT:
             case PARAM_SEMANTIC:
             case FLOAT_PARAM:
             case IMAGE_INIT_FROM:
             case IMAGE_INIT_FROM_REF:
             case FALLOFF_ANGLE:
             case FALLOFF_EXPONENT:
             case CONSTANT_ATTENUATION:
             case LINEAR_ATTENUATION:
             case QUADRATIC_ATTENUATION:
             case SAMPLER2D_MAGFILTER:
             case SAMPLER2D_SOURCE:
             case SAMPLER2D_MINFILTER:
             case SAMPLER2D_WRAP_S:
             case SAMPLER2D_WRAP_T:
                 if (this.stringBuilder != null) this.stringBuilder.append(ch, start, length);
                 break;
 
             case FLOAT_ARRAY:
             case MATRIX:
             case TRANSLATE:
             case LOOKAT:
             case SCALE:
             case ROTATE:
             case SKEW:
                 this.chunkFloatReader.addChunk(ch, start, length);
                 break;
 
             case NAME_ARRAY:
                 this.chunkStringReader.addChunk(ch, start, length);
                 break;
 
             case POLYGONS_P:
             case POLYLIST_P:
             case TRIANGLES_P:
             case POLYLIST_VCOUNT:
                 this.chunkIntReader.addChunk(ch, start, length);
                 break;
 
             default:
                 // Ignored
         }
     }
 
 
     /**
      * Enters an element and sets the specified parser mode. The old parser mode
      * is pushed to the mode stack.
      *
      * @param newParserMode
      *            The new parser mode to set
      */
 
     private void enterElement(final ParserMode newParserMode)
     {
         this.modeStack.push(this.mode);
         this.mode = newParserMode;
     }
 
 
     /**
      * Leaves the current element. Pops the previous parser mode from the mode
      * stack and sets it as the current mode
      */
 
     private void leaveElement()
     {
         this.mode = this.modeStack.pop();
     }
 
 
     /**
      * Returns the parsed Document document.
      *
      * @return The parsed Document document
      */
 
     public Document getDocument()
     {
         if (this.mode != ParserMode.ROOT)
             throw new IllegalStateException("Internal parser error");
         return this.document;
     }
 
 
     /**
      * Enters COLLADA element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterCOLLADA(final Attributes attributes)
     {
        //final String version = attributes.getValue("version");
        //if (!"1.5.0".equals(version))
             //throw new ParserException("Only COLLADA 1.5.0 supported");
         enterElement(ParserMode.COLLADA);
     }
 
 
     /**
      * Enters a library_images element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryImages(final Attributes attributes)
     {
         this.imageLibrary = new ImageLibrary();
         final String name = attributes.getValue("name");
         final String id = attributes.getValue("id");
         if (name != null) this.imageLibrary.setName(name);
         if (id != null) this.imageLibrary.setId(id);
         enterElement(ParserMode.LIBRARY_IMAGES);
     }
 
 
     /**
      * Enters a image element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterImage(final Attributes attributes)
     {
         this.image = new Image();
         this.image.setId(attributes.getValue("id"));
         this.image.setName(attributes.getValue("name"));
         this.image.setSid(attributes.getValue("sid"));
         enterElement(ParserMode.IMAGE);
     }
 
 
     /**
      * Enters image init_from element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterImageInitFrom(final Attributes attributes)
     {
         this.imageSourceBuilder = new ImageSourceBuilder();
         final String tmp = attributes.getValue("mips_generate");
         if (tmp != null)
             this.imageSourceBuilder.setGenerateMips(Boolean.valueOf(tmp));
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.IMAGE_INIT_FROM);
     }
 
 
     /**
      * Enters image init_from ref element.
      */
 
     private void enterImageInitFromRef()
     {
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.IMAGE_INIT_FROM_REF);
     }
 
 
     /**
      * Leaves image init_from ref element.
      */
 
     private void leaveImageInitFromRef()
     {
         final String text = this.stringBuilder.toString();
         try
         {
             this.imageSourceBuilder.setRef(new URI(text));
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(text + " is not a valid URI: " + e, e);
         }
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves an image init_from element.
      */
 
     private void leaveImageInitFrom()
     {
         // If string builder is still there then we got no ref attribute and
         // maybe we got an image with the old COLLADA 1.4 syntax.
         if (this.stringBuilder != null)
         {
             final String text = this.stringBuilder.toString();
             try
             {
                 this.imageSourceBuilder.setRef(new URI(text));
             }
             catch (final URISyntaxException e)
             {
                 throw new ParserException(text + " is not a valid URI: " + e, e);
             }
             this.stringBuilder = null;
         }
         this.image.setSource(this.imageSourceBuilder.build());
         this.imageSourceBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves an image element.
      */
 
     private void leaveImage()
     {
         this.imageLibrary.getImages().add(this.image);
         this.image = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_images element.
      */
 
     private void leaveLibraryImages()
     {
         this.document.getImageLibraries().add(this.imageLibrary);
         this.imageLibrary = null;
         leaveElement();
     }
 
 
     /**
      * Enters a library_images element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryMaterials(final Attributes attributes)
     {
         this.materialLibrary = new MaterialLibrary();
         final String name = attributes.getValue("name");
         final String id = attributes.getValue("id");
         if (name != null) this.materialLibrary.setName(name);
         if (id != null) this.materialLibrary.setId(id);
         enterElement(ParserMode.LIBRARY_MATERIALS);
     }
 
 
     /**
      * Enters a material element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterMaterial(final Attributes attributes)
     {
         this.materialBuilder = new MaterialBuilder();
         this.materialBuilder.setId(attributes.getValue("id"));
         this.materialBuilder.setName(attributes.getValue("name"));
         enterElement(ParserMode.MATERIAL);
     }
 
 
     /**
      * Enters a instance_effect element.
      *
      * @param attributes
      *            The element attributes.
      * @throws URISyntaxException
      *             When URI is invalid.
      */
 
     private void enterInstanceEffect(final Attributes attributes)
         throws URISyntaxException
     {
         this.effectInstanceBuilder = new EffectInstanceBuilder();
         this.effectInstanceBuilder.setUrl(new URI(attributes.getValue("url")));
         this.effectInstanceBuilder.setSid(attributes.getValue("sid"));
         this.effectInstanceBuilder.setName(attributes.getValue("name"));
         enterElement(ParserMode.INSTANCE_EFFECT);
     }
 
 
     /**
      * Leaves a instance_effect element.
      */
 
     private void leaveInstanceEffect()
     {
         this.materialBuilder.setEffectInstance(this.effectInstanceBuilder
                 .build());
         this.effectInstanceBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a material element.
      */
 
     private void leaveMaterial()
     {
         this.materialLibrary.getMaterials().add(this.materialBuilder.build());
         this.materialBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_materials element.
      */
 
     private void leaveLibraryMaterials()
     {
         this.document.getMaterialLibraries().add(this.materialLibrary);
         this.materialLibrary = null;
         leaveElement();
     }
 
 
     /**
      * Enters a library_effects element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryEffects(final Attributes attributes)
     {
         this.effectLibrary = new EffectLibrary();
         this.effectLibrary.setName(attributes.getValue("name"));
         this.effectLibrary.setId(attributes.getValue("id"));
         enterElement(ParserMode.LIBRARY_EFFECTS);
     }
 
 
     /**
      * Enters a effect element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterEffect(final Attributes attributes)
     {
         final String id = attributes.getValue("id");
         this.effect = new Effect(id);
         this.effect.setName(attributes.getValue("name"));
         enterElement(ParserMode.EFFECT);
     }
 
 
     /**
      * Enters a profile_COMMON element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterProfileCommon(final Attributes attributes)
     {
         this.commonEffectProfileBuilder = new CommonEffectProfileBuilder();
         this.commonEffectProfileBuilder.setId(attributes.getValue("id"));
         enterElement(ParserMode.PROFILE_COMMON);
     }
 
 
     /**
      * Enters newparam element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterCommonNewParam(final Attributes attributes)
     {
         this.commonNewParamBuilder = new CommonNewParamBuilder();
         this.commonNewParamBuilder.setSid(attributes.getValue("sid"));
         enterElement(ParserMode.NEWPARAM);
     }
 
 
     /**
      * Enters a float param.
      */
 
     private void enterFloatParam()
     {
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.FLOAT_PARAM);
     }
 
 
     /**
      * Leaves a float param.
      */
 
     private void leaveFloatParam()
     {
         final double value = Double.parseDouble(this.stringBuilder.toString()
                 .trim());
         this.stringBuilder = null;
         this.profileParam = new FloatParam(value);
         leaveElement();
     }
 
 
     /**
      * Enters a sampler2D element.
      */
 
     private void enterSampler2DParam()
     {
         this.profileParam = new Sampler2DParam();
         enterElement(ParserMode.SAMPLER2D);
     }
 
 
     /**
      * Enters sampler2D wrap_s element.
      */
 
     private void enterSampler2DWrapS()
     {
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.SAMPLER2D_WRAP_S);
     }
 
 
     /**
      * Leaves sampler2D wrap_s element.
      */
 
     private void leaveSampler2DWrapS()
     {
         ((Sampler2DParam) this.profileParam).setWrapS(Wrap
                 .valueOf(this.stringBuilder.toString()));
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters sampler2D wrap_t element.
      */
 
     private void enterSampler2DWrapT()
     {
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.SAMPLER2D_WRAP_T);
     }
 
 
     /**
      * Enters an instance_image element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterInstanceImage(final Attributes attributes)
     {
         final String urlString = attributes.getValue("url");
         URI url;
         try
         {
             url = new URI(urlString);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(urlString + " is not a valid URI: " + e,
                 e);
         }
         final ImageInstance imageInstance = new ImageInstance(url);
         imageInstance.setName(attributes.getValue("name"));
         imageInstance.setSid(attributes.getValue("sid"));
         ((Sampler2DParam) this.profileParam).setImageInstance(imageInstance);
         enterElement(ParserMode.INSTANCE_IMAGE);
     }
 
 
     /**
      * Leaves sampler2D wrap_t element.
      */
 
     private void leaveSampler2DWrapT()
     {
         ((Sampler2DParam) this.profileParam).setWrapT(Wrap
                 .valueOf(this.stringBuilder.toString()));
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters sampler2D minfilter element.
      */
 
     private void enterSampler2DMinFilter()
     {
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.SAMPLER2D_MINFILTER);
     }
 
 
     /**
      * Leaves sampler2D minfilter element.
      */
 
     private void leaveSampler2DMinFilter()
     {
         ((Sampler2DParam) this.profileParam).setMinFilter(Filter
                 .valueOf(this.stringBuilder.toString()));
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters sampler2D magfilter element.
      */
 
     private void enterSampler2DMagFilter()
     {
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.SAMPLER2D_MAGFILTER);
     }
 
 
     /**
      * Leaves sampler2D magfilter element.
      */
 
     private void leaveSampler2DMagFilter()
     {
         ((Sampler2DParam) this.profileParam).setMagFilter(Filter
                 .valueOf(this.stringBuilder.toString()));
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters a param semantic element.
      */
 
     private void enterParamSemantic()
     {
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.PARAM_SEMANTIC);
     }
 
 
     /**
      * Leaves a param semantic element.
      */
 
     private void leaveParamSemantic()
     {
         this.commonNewParamBuilder.setSemantic(this.stringBuilder.toString());
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a newparam element.
      */
 
     private void leaveNewParam()
     {
         this.commonNewParamBuilder.setParameter(this.profileParam);
         this.profileParam = null;
         this.commonEffectProfileBuilder.getParams()
                 .add(this.commonNewParamBuilder.build());
         this.commonNewParamBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters a common_PROFILE technique element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterTechniqueCommon(final Attributes attributes)
     {
         this.commonEffectTechniqueBuilder = new CommonEffectTechniqueBuilder();
         this.commonEffectTechniqueBuilder.setSid(attributes.getValue("sid"));
         this.commonEffectTechniqueBuilder.setId(attributes.getValue("id"));
         enterElement(ParserMode.TECHNIQUE_COMMON);
     }
 
 
     /**
      * Enters a phong element.
      */
 
     private void enterPhong()
     {
         this.shader = new PhongShader();
         enterElement(ParserMode.PHONG);
     }
 
 
     /**
      * Enters a blinn element.
      */
 
     private void enterBlinn()
     {
         this.shader = new BlinnShader();
         enterElement(ParserMode.BLINN);
     }
 
 
     /**
      * Enters a constant element.
      */
 
     private void enterConstant()
     {
         this.shader = new ConstantShader();
         enterElement(ParserMode.CONSTANT);
     }
 
 
     /**
      * Enters a lambert element.
      */
 
     private void enterLambert()
     {
         this.shader = new LambertShader();
         enterElement(ParserMode.LAMBERT);
     }
 
 
     /**
      * Enters a color element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterShadingColor(final Attributes attributes)
     {
         this.rgbaColor = new RGBAColor();
         this.rgbaColor.setSid(attributes.getValue("sid"));
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.SHADING_COLOR);
     }
 
 
     /**
      * Leaves a shading color element.
      */
 
     private void leaveShadingColor()
     {
         final String[] parts = this.stringBuilder.toString().trim().split(
                 "\\s+");
         this.rgbaColor.setRed(Float.parseFloat(parts[0]));
         this.rgbaColor.setGreen(Float.parseFloat(parts[1]));
         this.rgbaColor.setBlue(Float.parseFloat(parts[2]));
         this.rgbaColor.setAlpha(Float.parseFloat(parts[3]));
         this.colorOrTexture = new ColorAttribute(this.rgbaColor);
         this.stringBuilder = null;
         this.rgbaColor = null;
         leaveElement();
     }
 
 
     /**
      * Enters a texture element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterTexture(final Attributes attributes)
     {
         final String texture = attributes.getValue("texture");
         final String texcoord = attributes.getValue("texcoord");
         this.colorOrTexture = new ColorAttribute(new Texture(texture,
             texcoord));
         enterElement(ParserMode.TEXTURE);
     }
 
 
     /**
      * Leaves a colorOrTexture element
      */
 
     private void leaveColorOrTexture()
     {
         switch (this.mode)
         {
             case EMISSION:
                 this.shader.setEmission(this.colorOrTexture);
                 break;
 
             case AMBIENT:
                 ((DiffuseShader) this.shader).setAmbient(this.colorOrTexture);
                 break;
 
             case DIFFUSE:
                 ((DiffuseShader) this.shader).setDiffuse(this.colorOrTexture);
                 break;
 
             case SPECULAR:
                 ((BRDFShader) this.shader).setSpecular(this.colorOrTexture);
                 break;
 
             case REFLECTIVE:
                 this.shader.setReflective(this.colorOrTexture);
                 break;
 
             case TRANSPARENT:
                 this.shader.setTransparent(this.colorOrTexture);
                 break;
 
             default:
                 throw new ParserException(
                         "Unknown parser mode for colorOrTexture: " + this.mode);
 
         }
         this.colorOrTexture = null;
         leaveElement();
     }
 
 
     /**
      * Enters a float element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterFloat(final Attributes attributes)
     {
         this.floatValue = new FloatValue(0);
         this.floatValue.setSid(attributes.getValue("sid"));
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.FLOAT);
     }
 
 
     /**
      * Leaves a float element.
      */
 
     private void leaveFloat()
     {
         final double value = Double.parseDouble(this.stringBuilder.toString()
                 .trim());
         this.stringBuilder = null;
         this.floatValue.setValue(value);
         this.floatAttrib = new FloatAttribute(this.floatValue);
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a shading float element.
      */
 
     private void leaveFloatOrParam()
     {
         switch (this.mode)
         {
             case REFLECTIVITY:
                 this.shader.setReflectivity(this.floatAttrib);
                 break;
 
             case SHININESS:
                 ((BRDFShader) this.shader).setShininess(this.floatAttrib);
                 break;
 
             case TRANSPARENCY:
                 this.shader.setTransparency(this.floatAttrib);
                 break;
 
             case INDEX_OF_REFRACTION:
                 this.shader.setIndexOfRefraction(this.floatAttrib);
                 break;
 
             default:
                 throw new ParserException(
                         "Unknown parser mode for a shading float: " + this.mode);
         }
         leaveElement();
     }
 
 
     /**
      * Leaves a shader element.
      */
 
     private void leaveShader()
     {
         this.commonEffectTechniqueBuilder.setShader(this.shader);
         this.shader = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a common_PROFILE technique element.
      */
 
     private void leaveTechniqueCommon()
     {
         this.commonEffectProfileBuilder
                 .setTechnique(this.commonEffectTechniqueBuilder.build());
         this.commonEffectTechniqueBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a common_PROFILE element.
      */
 
     private void leaveProfileCommon()
     {
         this.effect.getProfiles().add(this.commonEffectProfileBuilder.build());
         this.commonEffectProfileBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a effect element.
      */
 
     private void leaveEffect()
     {
         this.effectLibrary.getEffects().add(this.effect);
         this.effect = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_effects element.
      */
 
     private void leaveLibraryEffects()
     {
         this.document.getEffectLibraries().add(this.effectLibrary);
         this.effectLibrary = null;
         leaveElement();
     }
 
 
     /**
      * Enters a library_geometries element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryGeometries(final Attributes attributes)
     {
         this.geometryLibrary = new GeometryLibrary();
         this.geometryLibrary.setName(attributes.getValue("name"));
         this.geometryLibrary.setId(attributes.getValue("id"));
         enterElement(ParserMode.LIBRARY_GEOMETRIES);
     }
 
 
     /**
      * Enters a geometry element
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterGeometry(final Attributes attributes)
     {
         this.geometryBuilder = new GeometryBuilder();
         this.geometryBuilder.setId(attributes.getValue("id"));
         this.geometryBuilder.setName(attributes.getValue("name"));
         enterElement(ParserMode.GEOMETRY);
     }
 
 
     /**
      * Enters a mesh element
      */
 
     private void enterMesh()
     {
         this.meshBuilder = new MeshBuilder();
         enterElement(ParserMode.MESH);
     }
 
 
     /**
      * Enters a data source element
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterMeshDataSource(final Attributes attributes)
     {
         final String id = attributes.getValue("id");
         this.dataSource = new DataFlowSource(id);
         this.dataSource.setName(attributes.getValue("name"));
         enterElement(ParserMode.MESH_DATA_SOURCE);
     }
 
 
     /**
      * Enters a float_array element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterFloatArray(final Attributes attributes)
     {
         final int count = Integer.parseInt(attributes.getValue("count"));
         final FloatArray array = this.floatArray = new FloatArray(count);
         final String digits = attributes.getValue("digits");
         final String magnitude = attributes.getValue("magnitude");
         if (digits != null)
             this.floatArray.setDigits(Integer.parseInt(digits));
         if (magnitude != null)
             this.floatArray.setMagnitude(Integer.parseInt(magnitude));
         this.floatArray.setId(attributes.getValue("id"));
         this.floatArray.setName(attributes.getValue("name"));
         this.chunkFloatReader = new ChunkFloatReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final double value)
             {
                 array.setValue(this.index++, value);
             }
         };
 
         enterElement(ParserMode.FLOAT_ARRAY);
     }
 
 
     /**
      * Leaves a float_array element
      */
 
     private void leaveFloatArray()
     {
         this.chunkFloatReader.finish();
         this.chunkFloatReader = null;
         this.dataSource.setArray(this.floatArray);
         this.floatArray = null;
         leaveElement();
     }
 
 
     /**
      * Enters a float_array element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterNameArray(final Attributes attributes)
     {
         final int count = Integer.parseInt(attributes.getValue("count"));
         final NameArray array = this.nameArray = new NameArray(count);
         array.setId(attributes.getValue("id"));
         array.setName(attributes.getValue("name"));
         this.chunkStringReader = new ChunkStringReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final String value)
             {
                 array.setValue(this.index++, value);
             }
         };
 
         enterElement(ParserMode.NAME_ARRAY);
     }
 
 
     /**
      * Leaves a Name_array element
      */
 
     private void leaveNameArray()
     {
         this.chunkStringReader.finish();
         this.chunkStringReader = null;
         this.dataSource.setArray(this.nameArray);
         this.nameArray = null;
         leaveElement();
     }
 
 
     /**
      * Enters an accessor element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterAccessor(final Attributes attributes)
     {
         final String sourceStr = attributes.getValue("source");
         URI source;
         try
         {
             source = new URI(sourceStr);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(sourceStr + " is not a valid URI: " + e,
                 e);
         }
         final int count = Integer.valueOf(attributes.getValue("count"));
         this.accessor = new Accessor(source, count);
         final String offset = attributes.getValue("offset");
         if (offset != null) this.accessor.setOffset(Integer.valueOf(offset));
         final String stride = attributes.getValue("stride");
         if (stride != null) this.accessor.setStride(Integer.valueOf(stride));
         enterElement(ParserMode.ACCESSOR);
     }
 
 
     /**
      * Enters a param element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterParam(final Attributes attributes)
     {
         final DataFlowParam param = new DataFlowParam(
             attributes.getValue("type"));
         param.setName(attributes.getValue("name"));
         param.setSemantic(attributes.getValue("semantic"));
         param.setSid(attributes.getValue("sid"));
         this.accessor.getParams().add(param);
     }
 
 
     /**
      * Leaves an accessor element.
      */
 
     private void leaveAccessor()
     {
         this.dataSource.setCommonTechnique(new CommonSourceTechnique(
             this.accessor));
         this.accessor = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a mesh data source element.
      */
 
     private void leaveMeshDataSource()
     {
         this.meshBuilder.getSources().add(this.dataSource);
         this.dataSource = null;
         leaveElement();
     }
 
 
     /**
      * Enters a vertices element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterVertices(final Attributes attributes)
     {
         final String id = attributes.getValue("id");
         this.vertices = new Vertices(id);
         this.vertices.setName(attributes.getValue("name"));
         enterElement(ParserMode.VERTICES);
     }
 
 
     /**
      * Enters vertices input element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterVerticesInput(final Attributes attributes)
     {
         final String semantic = attributes.getValue("semantic");
         final String text = attributes.getValue("source");
         URI source;
         try
         {
             source = new URI(text);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(text + " is not a valid URI: " + e, e);
         }
         final UnsharedInput input = new UnsharedInput(semantic, source);
         this.vertices.getInputs().add(input);
         enterElement(ParserMode.VERTICES_INPUT);
     }
 
 
     /**
      * Leaves a vertices element
      */
 
     private void leaveVertices()
     {
         this.meshBuilder.setVertices(this.vertices);
         this.vertices = null;
         leaveElement();
     }
 
 
     /**
      * Enters a triangles element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterTriangles(final Attributes attributes)
     {
         this.trianglesBuilder = new TrianglesBuilder();
         this.trianglesBuilder.setCount(Integer.parseInt(attributes
                 .getValue("count")));
         this.trianglesBuilder.setMaterial(attributes.getValue("material"));
         this.trianglesBuilder.setName(attributes.getValue("name"));
         this.primitivesBuilder = this.trianglesBuilder;
         enterElement(ParserMode.TRIANGLES);
     }
 
 
     /**
      * Enters a triangles p element.
      */
 
     private void enterTrianglesP()
     {
         final List<Integer> builder = this.intArrayBuilder = new ArrayList<Integer>();
         this.chunkIntReader = new ChunkIntReader()
         {
             @Override
             protected void valueFound(final int value)
             {
                 builder.add(value);
             }
         };
         enterElement(ParserMode.TRIANGLES_P);
     }
 
 
     /**
      * Enters a primitives input element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterPrimitivesInput(final Attributes attributes)
     {
         final String semantic = attributes.getValue("semantic");
         final int offset = Integer.parseInt(attributes.getValue("offset"));
         final String sourceString = attributes.getValue("source");
         final String setString = attributes.getValue("set");
         final Integer set = setString == null ? null : Integer
                 .parseInt(setString);
         URI source;
         try
         {
             source = new URI(sourceString);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(sourceString + " is not a valid URI: "
                     + e, e);
         }
         final SharedInput input = new SharedInput(semantic, source, offset);
         input.setSet(set);
         this.primitivesBuilder.getInputs().add(input);
         enterElement(ParserMode.PRIMITIVES_INPUT);
     }
 
 
     /**
      * Leaves a triangles p element.
      */
 
     private void leaveTrianglesP()
     {
         this.chunkIntReader.finish();
         this.chunkIntReader = null;
         final int size = this.intArrayBuilder.size();
         final IntList data = new IntList(size);
         data.setValues(this.intArrayBuilder);
         this.trianglesBuilder.setData(data);
         this.intArrayBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a polygons element.
      */
 
     private void leaveTriangles()
     {
         this.meshBuilder.getPrimitives().add(this.trianglesBuilder.build());
         this.trianglesBuilder = null;
         this.primitivesBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters a polylist element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterPolyList(final Attributes attributes)
     {
         this.polyListBuilder = new PolyListBuilder();
         this.polyListBuilder.setCount(Integer.parseInt(attributes
                 .getValue("count")));
         this.polyListBuilder.setMaterial(attributes.getValue("material"));
         this.polyListBuilder.setName(attributes.getValue("name"));
         this.primitivesBuilder = this.polyListBuilder;
         enterElement(ParserMode.POLYLIST);
     }
 
 
     /**
      * Enters a polygons vcount element.
      */
 
     private void enterPolyListVcount()
     {
         final List<Integer> builder = this.intArrayBuilder = new ArrayList<Integer>();
         this.chunkIntReader = new ChunkIntReader()
         {
             @Override
             protected void valueFound(final int value)
             {
                 builder.add(value);
             }
         };
         enterElement(ParserMode.POLYLIST_VCOUNT);
     }
 
 
     /**
      * Leaves a polylist vcount element.
      */
 
     private void leavePolyListVcount()
     {
         this.chunkIntReader.finish();
         this.chunkIntReader = null;
         final int size = this.intArrayBuilder.size();
         final IntList data = new IntList(size);
         data.setValues(this.intArrayBuilder);
         this.polyListBuilder.setVcount(data);
         this.intArrayBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters a polygons p element.
      */
 
     private void enterPolyListP()
     {
         final List<Integer> builder = this.intArrayBuilder = new ArrayList<Integer>();
         this.chunkIntReader = new ChunkIntReader()
         {
             @Override
             protected void valueFound(final int value)
             {
                 builder.add(value);
             }
         };
         enterElement(ParserMode.POLYLIST_P);
     }
 
 
     /**
      * Leaves a polylist p element.
      */
 
     private void leavePolyListP()
     {
         this.chunkIntReader.finish();
         this.chunkIntReader = null;
         final int size = this.intArrayBuilder.size();
         final IntList data = new IntList(size);
         data.setValues(this.intArrayBuilder);
         this.polyListBuilder.setData(data);
         this.intArrayBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a polylist element.
      */
 
     private void leavePolyList()
     {
         this.meshBuilder.getPrimitives().add(this.polyListBuilder.build());
         this.polyListBuilder = null;
         this.primitivesBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Enters a polylist element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterPolygons(final Attributes attributes)
     {
         this.polygonsBuilder = new PolygonsBuilder();
         this.polygonsBuilder.setCount(Integer.parseInt(attributes
                 .getValue("count")));
         this.polygonsBuilder.setMaterial(attributes.getValue("material"));
         this.polygonsBuilder.setName(attributes.getValue("name"));
         this.primitivesBuilder = this.polygonsBuilder;
         enterElement(ParserMode.POLYGONS);
     }
 
 
     /**
      * Enters a polygons p element.
      */
 
     private void enterPolygonsP()
     {
         final List<Integer> builder = this.intArrayBuilder = new ArrayList<Integer>();
         this.chunkIntReader = new ChunkIntReader()
         {
             @Override
             protected void valueFound(final int value)
             {
                 builder.add(value);
             }
         };
         enterElement(ParserMode.POLYGONS_P);
     }
 
 
     /**
      * Leaves a polygons p element.
      */
 
     private void leavePolygonsP()
     {
         this.chunkIntReader.finish();
         this.chunkIntReader = null;
         final int size = this.intArrayBuilder.size();
         final IntList data = new IntList(size);
         data.setValues(this.intArrayBuilder);
         this.polygonsBuilder.getData().add(data);
         this.intArrayBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a polygons element.
      */
 
     private void leavePolygons()
     {
         this.meshBuilder.getPrimitives().add(this.polygonsBuilder.build());
         this.polygonsBuilder = null;
         this.primitivesBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a mesh element.
      */
 
     private void leaveMesh()
     {
         this.geometryBuilder.setGeometric(this.meshBuilder.build());
         this.meshBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a geometry element.
      */
 
     private void leaveGeometry()
     {
         this.geometryLibrary.getGeometries().add(this.geometryBuilder.build());
         this.geometryBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_geometries element.
      */
 
     private void leaveLibraryGeometries()
     {
         this.document.getGeometryLibraries().add(this.geometryLibrary);
         this.geometryLibrary = null;
         leaveElement();
     }
 
 
     /**
      * Enters a library_animations element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryAnimations(final Attributes attributes)
     {
         this.animationLibrary = new AnimationLibrary();
         this.animationLibrary.setName(attributes.getValue("name"));
         this.animationLibrary.setId(attributes.getValue("id"));
         enterElement(ParserMode.LIBRARY_ANIMATIONS);
     }
 
 
     /**
      * Enters a animation element
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterAnimation(final Attributes attributes)
     {
         final Animation animation = new Animation();
         animation.setId(attributes.getValue("id"));
         animation.setName(attributes.getValue("name"));
 
         if (this.animation == null)
             this.animationLibrary.getAnimations().add(animation);
         else
             this.animation.getAnimations().add(animation);
 
         this.animation = animation;
 
         enterElement(ParserMode.ANIMATION);
     }
 
 
     /**
      * Enters a animation data source element
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterAnimationDataSource(final Attributes attributes)
     {
         final String id = attributes.getValue("id");
         this.dataSource = new DataFlowSource(id);
         this.dataSource.setName(attributes.getValue("name"));
         enterElement(ParserMode.ANIMATION_DATA_SOURCE);
     }
 
 
     /**
      * Leaves a animation data source element.
      */
 
     private void leaveAnimationDataSource()
     {
         this.animation.getSources().add(this.dataSource);
         this.dataSource = null;
         leaveElement();
     }
 
 
     /**
      * Enters a sampler element
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterSampler(final Attributes attributes)
     {
         this.sampler = new AnimationSampler();
         this.sampler.setId(attributes.getValue("id"));
         final String preBehavior = attributes.getValue("pre_behavior");
         if (preBehavior != null)
             this.sampler.setPreBehavior(AnimationBehavior.valueOf(preBehavior));
         final String postBehavior = attributes.getValue("post_behavior");
         if (postBehavior != null)
             this.sampler.setPostBehavior(AnimationBehavior
                 .valueOf(postBehavior));
         enterElement(ParserMode.SAMPLER);
     }
 
 
     /**
      * Enters sampler input element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterSamplerInput(final Attributes attributes)
     {
         final String semantic = attributes.getValue("semantic");
         final String text = attributes.getValue("source");
         URI source;
         try
         {
             source = new URI(text);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(text + " is not a valid URI: " + e, e);
         }
         final UnsharedInput input = new UnsharedInput(semantic, source);
         this.sampler.getInputs().add(input);
         enterElement(ParserMode.SAMPLER_INPUT);
     }
 
 
     /**
      * Enters a channel element
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterChannel(final Attributes attributes)
     {
         final String sourceStr = attributes.getValue("source");
         URI source;
         try
         {
             source = new URI(sourceStr);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(sourceStr + " is not a valid URI: " + e,
                 e);
         }
         final String target = attributes.getValue("target");
         this.animation.getChannels().add(new AnimationChannel(source, target));
         enterElement(ParserMode.CHANNEL);
     }
 
 
     /**
      * Leaves a sampler element.
      */
 
     private void leaveSampler()
     {
         this.animation.getSamplers().add(this.sampler);
         this.sampler = null;
         leaveElement();
     }
 
 
     /**
      * Leaves animation element
      */
 
     private void leaveAnimation()
     {
         final Element parent = this.animation.getParent();
         if (parent instanceof Animation)
             this.animation = (Animation) parent;
         else
             this.animation = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_animations element.
      */
 
     private void leaveLibraryAnimations()
     {
         this.document.getAnimationLibraries().add(this.animationLibrary);
         this.animationLibrary = null;
         leaveElement();
     }
 
 
     /**
      * Enters a library_light element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryLights(final Attributes attributes)
     {
         this.lightLibrary = new LightLibrary();
         this.lightLibrary.setName(attributes.getValue("name"));
         this.lightLibrary.setId(attributes.getValue("id"));
         enterElement(ParserMode.LIBRARY_LIGHTS);
     }
 
 
     /**
      * Enters a light element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLight(final Attributes attributes)
     {
         this.lightBuilder = new LightBuilder();
         this.lightBuilder.setId(attributes.getValue("id"));
         this.lightBuilder.setName(attributes.getValue("name"));
         enterElement(ParserMode.LIGHT);
     }
 
 
     /**
      * Enters a directional light element.
      *
      * @param mode
      *            The light source mode.
      */
 
     private void enterLightSource(final ParserMode mode)
     {
         this.lightSourceBuilder = new LightSourceBuilder();
         enterElement(mode);
     }
 
 
     /**
      * Enters a light color element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLightColor(final Attributes attributes)
     {
         this.rgbColor = new RGBColor();
         this.rgbColor.setSid(attributes.getValue("sid"));
         this.stringBuilder = new StringBuilder();
         enterElement(ParserMode.LIGHT_COLOR);
     }
 
 
     /**
      * Leaves a light color element.
      */
 
     private void leaveLightColor()
     {
         final String[] parts = this.stringBuilder.toString().trim().split(
             "\\s+");
         this.rgbColor.setRed(Double.parseDouble(parts[0]));
         this.rgbColor.setGreen(Double.parseDouble(parts[1]));
         this.rgbColor.setBlue(Double.parseDouble(parts[2]));
         this.stringBuilder = null;
         this.lightSourceBuilder.setColor(this.rgbColor);
         this.rgbColor = null;
         leaveElement();
     }
 
 
     /**
      * Enters a light float value.
      *
      * @param attributes
      *            The element attributes.
      * @param mode
      *            The parser mode.
      */
 
     private void enterLightFloatValue(final Attributes attributes,
         final ParserMode mode)
     {
         this.floatValue = new FloatValue(0);
         this.floatValue.setSid(attributes.getValue("sid"));
         this.stringBuilder = new StringBuilder();
         enterElement(mode);
     }
 
 
     /**
      * Leaves a falloff_angle element.
      */
 
     private void leaveFalloffAngle()
     {
         final double value = Double.parseDouble(this.stringBuilder.toString());
         this.floatValue.setValue(value);
         this.lightSourceBuilder.setFalloffAngle(this.floatValue);
         this.floatValue = null;
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a falloff_exponent element.
      */
 
     private void leaveFalloffExponent()
     {
         final double value = Double.parseDouble(this.stringBuilder.toString());
         this.floatValue.setValue(value);
         this.lightSourceBuilder.setFalloffExponent(this.floatValue);
         this.floatValue = null;
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a constant_attenuation element.
      */
 
     private void leaveConstantAttenuation()
     {
         final double value = Double.parseDouble(this.stringBuilder.toString());
         this.floatValue.setValue(value);
         this.lightSourceBuilder.setConstantAttenuation(this.floatValue);
         this.floatValue = null;
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a linear_attenuation element.
      */
 
     private void leaveLinearAttenuation()
     {
         final double value = Double.parseDouble(this.stringBuilder.toString());
         this.floatValue.setValue(value);
         this.lightSourceBuilder.setLinearAttenuation(this.floatValue);
         this.floatValue = null;
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a quadratic_attenuation element.
      */
 
     private void leaveQuadraticAttenuation()
     {
         final double value = Double.parseDouble(this.stringBuilder.toString());
         this.floatValue.setValue(value);
         this.lightSourceBuilder.setQuadraticAttenuation(this.floatValue);
         this.floatValue = null;
         this.stringBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves an ambient element.
      */
 
     private void leaveAmbient()
     {
         this.lightBuilder
                 .setLightSource(this.lightSourceBuilder.buildAmbient());
         this.lightSourceBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a directional element.
      */
 
     private void leaveDirectional()
     {
         this.lightBuilder.setLightSource(this.lightSourceBuilder
                 .buildDirectional());
         this.lightSourceBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a point element.
      */
 
     private void leavePoint()
     {
         this.lightBuilder.setLightSource(this.lightSourceBuilder.buildPoint());
         this.lightSourceBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a spot element.
      */
 
     private void leaveSpot()
     {
         this.lightBuilder.setLightSource(this.lightSourceBuilder.buildSpot());
         this.lightSourceBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a light element.
      */
 
     private void leaveLight()
     {
         this.lightLibrary.getLights().add(this.lightBuilder.build());
         this.lightBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_lights element.
      */
 
     private void leaveLibraryLights()
     {
         this.document.getLightLibraries().add(this.lightLibrary);
         this.lightLibrary = null;
         leaveElement();
     }
 
 
     /**
      * Enters a library_cameras element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryCameras(final Attributes attributes)
     {
         this.cameraLibrary = new CameraLibrary();
         this.cameraLibrary.setName(attributes.getValue("name"));
         this.cameraLibrary.setId(attributes.getValue("id"));
         enterElement(ParserMode.LIBRARY_CAMERAS);
     }
 
 
     /**
      * Enters a camera element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterCamera(final Attributes attributes)
     {
         this.cameraBuilder = new CameraBuilder();
         this.cameraBuilder.setId(attributes.getValue("id"));
         this.cameraBuilder.setName(attributes.getValue("name"));
         enterElement(ParserMode.CAMERA);
     }
 
 
     /**
      * Enters perspective element
      */
 
     private void enterPerspective()
     {
         this.perspectiveBuilder = new PerspectiveBuilder();
         this.projectionBuilder = this.perspectiveBuilder;
         enterElement(ParserMode.PERSPECTIVE);
     }
 
 
     /**
      * Enters perspective element
      */
 
     private void enterOrthographic()
     {
         this.orthographicBuilder = new OrthographicBuilder();
         this.projectionBuilder = this.orthographicBuilder;
         enterElement(ParserMode.ORTHOGRAPHIC);
     }
 
 
     /**
      * Enters a perspective value element.
      *
      * @param mode
      *            The next parser mode
      * @param attributes
      *            The element attributes
      */
 
     private void enterProjectionValue(final ParserMode mode,
         final Attributes attributes)
     {
         this.floatValue = new FloatValue(0);
         this.floatValue.setSid(attributes.getValue("sid"));
         this.stringBuilder = new StringBuilder();
         enterElement(mode);
     }
 
 
     /**
      * Leaves xfov element.
      */
 
     private void leaveXfov()
     {
         this.floatValue.setValue(Double.parseDouble(this.stringBuilder
                 .toString().trim()));
         this.perspectiveBuilder.setXFov(this.floatValue);
         this.stringBuilder = null;
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves yfov element.
      */
 
     private void leaveYfov()
     {
         this.floatValue.setValue(Double.parseDouble(this.stringBuilder
                 .toString().trim()));
         this.perspectiveBuilder.setYFov(this.floatValue);
         this.stringBuilder = null;
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves xmag element.
      */
 
     private void leaveXMag()
     {
         this.floatValue.setValue(Double.parseDouble(this.stringBuilder
                 .toString().trim()));
         this.orthographicBuilder.setXMag(this.floatValue);
         this.stringBuilder = null;
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves ymag element.
      */
 
     private void leaveYMag()
     {
         this.floatValue.setValue(Double.parseDouble(this.stringBuilder
                 .toString().trim()));
         this.orthographicBuilder.setYMag(this.floatValue);
         this.stringBuilder = null;
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves aspect_ratio element.
      */
 
     private void leaveAspectRatio()
     {
         this.floatValue.setValue(Double.parseDouble(this.stringBuilder
                 .toString().trim()));
         this.projectionBuilder.setAspectRatio(this.floatValue);
         this.stringBuilder = null;
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves znear element.
      */
 
     private void leaveZnear()
     {
         this.floatValue.setValue(Double.parseDouble(this.stringBuilder
                 .toString().trim()));
         this.projectionBuilder.setZNear(this.floatValue);
         this.stringBuilder = null;
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves zfar element.
      */
 
     private void leaveZfar()
     {
         this.floatValue.setValue(Double.parseDouble(this.stringBuilder
                 .toString().trim()));
         this.projectionBuilder.setZFar(this.floatValue);
         this.stringBuilder = null;
         this.floatValue = null;
         leaveElement();
     }
 
 
     /**
      * Leaves perspective or orthographic element.
      */
 
     private void leaveProjection()
     {
         this.cameraBuilder.setProjection(this.projectionBuilder.build());
         this.perspectiveBuilder = null;
         this.orthographicBuilder = null;
         this.projectionBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a camera element.
      */
 
     private void leaveCamera()
     {
         this.cameraLibrary.getCameras().add(this.cameraBuilder.build());
         this.cameraBuilder = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_cameras element.
      */
 
     private void leaveLibraryCameras()
     {
         this.document.getCameraLibraries().add(this.cameraLibrary);
         this.cameraLibrary = null;
         leaveElement();
     }
 
     /**
      * Enters a library_visual_scenes element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterLibraryVisualScenes(final Attributes attributes)
     {
         this.visualSceneLibrary = new VisualSceneLibrary();
         this.visualSceneLibrary.setName(attributes.getValue("name"));
         this.visualSceneLibrary.setId(attributes.getValue("id"));
         enterElement(ParserMode.LIBRARY_VISUAL_SCENES);
     }
 
 
     /**
      * Enters a visual_scene element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterVisualScene(final Attributes attributes)
     {
         this.visualScene = new VisualScene();
         this.visualScene.setId(attributes.getValue("id"));
         this.visualScene.setName(attributes.getValue("name"));
         enterElement(ParserMode.VISUAL_SCENE);
     }
 
 
     /**
      * Enters a visual scene node element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterNode(final Attributes attributes)
     {
         final Node node = new Node();
         node.setId(attributes.getValue("id"));
         node.setName(attributes.getValue("name"));
         node.setSid(attributes.getValue("sid"));
         final String type = attributes.getValue("type");
         if (type != null) node.setType(NodeType.valueOf(type));
         final String layers = attributes.getValue("layer");
         if (layers != null)
         {
             for (final String layer : layers.split("\\s+"))
                 node.getLayers().add(layer);
         }
 
         // Append the node to the scene or its parent node.
         if (this.node == null)
             this.visualScene.getNodes().add(node);
         else
             this.node.getNodes().add(node);
         this.node = node;
 
         enterElement(ParserMode.NODE);
     }
 
 
     /**
      * Enters a matrix element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterMatrix(final Attributes attributes)
     {
         final MatrixTransform transform = new MatrixTransform();
         transform.setSid(attributes.getValue("sid"));
 
         final MutableMatrix4d matrix = transform.getMatrix();
         this.chunkFloatReader = new ChunkFloatReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final double value)
             {
                 matrix.setElement(this.index / 4, this.index % 4, value);
                 this.index++;
             }
         };
         this.node.getTransforms().add(transform);
         enterElement(ParserMode.MATRIX);
     }
 
 
     /**
      * Leaves a transform element.
      */
 
     private void leaveTransform()
     {
         this.chunkFloatReader.finish();
         this.chunkFloatReader = null;
         leaveElement();
     }
 
 
     /**
      * Enters a scale element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterRotate(final Attributes attributes)
     {
         final RotateTransform transform = new RotateTransform();
         transform.setSid(attributes.getValue("sid"));
 
         final MutableVector3d vector = transform.getAxis();
         this.chunkFloatReader = new ChunkFloatReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final double value)
             {
                 switch (this.index)
                 {
                     case 0:
                         vector.setX(value);
                         break;
 
                     case 1:
                         vector.setY(value);
                         break;
 
                     case 2:
                         vector.setZ(value);
                         break;
 
                     case 3:
                         transform.setAngle(value);
                         break;
                 }
                 this.index++;
             }
         };
         this.node.getTransforms().add(transform);
         enterElement(ParserMode.ROTATE);
     }
 
 
     /**
      * Enters a translate element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterTranslate(final Attributes attributes)
     {
         final TranslateTransform transform = new TranslateTransform();
         transform.setSid(attributes.getValue("sid"));
 
         final MutableVector3d vector = transform.getTranslation();
         this.chunkFloatReader = new ChunkFloatReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final double value)
             {
                 switch (this.index)
                 {
                     case 0:
                         vector.setX(value);
                         break;
 
                     case 1:
                         vector.setY(value);
                         break;
 
                     case 2:
                         vector.setZ(value);
                         break;
                 }
                 this.index++;
             }
         };
         this.node.getTransforms().add(transform);
         enterElement(ParserMode.TRANSLATE);
     }
 
 
     /**
      * Enters a scale element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterLookAt(final Attributes attributes)
     {
         final LookAtTransform transform = new LookAtTransform();
         transform.setSid(attributes.getValue("sid"));
 
         final MutableVector3d eye = transform.getEye();
         final MutableVector3d interest = transform.getInterest();
         final MutableVector3d up = transform.getUp();
         this.chunkFloatReader = new ChunkFloatReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final double value)
             {
                 switch (this.index)
                 {
                     case 0:
                         eye.setX(value);
                         break;
 
                     case 1:
                         eye.setY(value);
                         break;
 
                     case 2:
                         eye.setZ(value);
                         break;
                     case 3:
                         interest.setX(value);
                         break;
 
                     case 4:
                         interest.setY(value);
                         break;
 
                     case 5:
                         interest.setZ(value);
                         break;
 
                     case 6:
                         up.setX(value);
                         break;
 
                     case 7:
                         up.setY(value);
                         break;
 
                     case 8:
                         up.setZ(value);
                         break;
                 }
                 this.index++;
             }
         };
         this.node.getTransforms().add(transform);
         enterElement(ParserMode.LOOKAT);
     }
 
 
     /**
      * Enters a scale element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterScale(final Attributes attributes)
     {
         final ScaleTransform transform = new ScaleTransform();
         transform.setSid(attributes.getValue("sid"));
 
         final MutableVector3d vector = transform.getScaling();
         this.chunkFloatReader = new ChunkFloatReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final double value)
             {
                 switch (this.index)
                 {
                     case 0:
                         vector.setX(value);
                         break;
 
                     case 1:
                         vector.setY(value);
                         break;
 
                     case 2:
                         vector.setZ(value);
                         break;
                 }
                 this.index++;
             }
         };
         this.node.getTransforms().add(transform);
         enterElement(ParserMode.SCALE);
     }
 
 
     /**
      * Enters a skew element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterSkew(final Attributes attributes)
     {
         final SkewTransform transform = new SkewTransform();
         transform.setSid(attributes.getValue("sid"));
 
         final MutableVector3d rotationAxis = transform.getRotationAxis();
         final MutableVector3d translationAxis = transform.getTranslationAxis();
         this.chunkFloatReader = new ChunkFloatReader()
         {
             private int index = 0;
 
             @Override
             protected void valueFound(final double value)
             {
                 switch (this.index)
                 {
                     case 0:
                         transform.setAngle(value);
                         break;
 
                     case 1:
                         rotationAxis.setX(value);
                         break;
 
                     case 2:
                         rotationAxis.setY(value);
                         break;
 
                     case 3:
                         rotationAxis.setZ(value);
                         break;
 
                     case 4:
                         translationAxis.setX(value);
                         break;
 
                     case 5:
                         translationAxis.setY(value);
                         break;
 
                     case 6:
                         translationAxis.setZ(value);
                         break;
                 }
                 this.index++;
             }
         };
         this.node.getTransforms().add(transform);
         enterElement(ParserMode.SKEW);
     }
 
 
     /**
      * Enters a instance_geometry element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterInstanceGeometry(final Attributes attributes)
     {
         final String urlString = attributes.getValue("url");
         URI url;
         try
         {
             url = new URI(urlString);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(urlString + " is not a valid URI: " + e,
                 e);
         }
         this.geometryInstance = new GeometryInstance(url);
         this.geometryInstance.setName(attributes.getValue("name"));
         this.geometryInstance.setSid(attributes.getValue("sid"));
         enterElement(ParserMode.INSTANCE_GEOMETRY);
     }
 
     /**
      * Enters a bind_material param element.
      *
      * @param attributes
      *            The element attributes.
      */
 
     private void enterMaterialBindParam(final Attributes attributes)
     {
         final DataFlowParams params = this.geometryInstance
                 .getMaterialBinding().getParams();
         final DataFlowParam param = new DataFlowParam(
             attributes.getValue("type"));
         param.setName(attributes.getValue("name"));
         param.setSemantic(attributes.getValue("semantic"));
         param.setSid(attributes.getValue("sid"));
         params.add(param);
         enterElement(ParserMode.BIND_MATERIAL_PARAM);
     }
 
 
     /**
      * Enters a instance_material element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterInstanceMaterial(final Attributes attributes)
     {
         final String symbol = attributes.getValue("symbol");
         final String targetString = attributes.getValue("target");
         URI target;
         try
         {
             target = new URI(targetString);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(targetString + " is not a valid URI: "
                 + e, e);
         }
         this.materialInstance = new MaterialInstance(symbol, target);
         this.materialInstance.setName(attributes.getValue("name"));
         this.materialInstance.setSid(attributes.getValue("sid"));
         enterElement(ParserMode.INSTANCE_MATERIAL);
     }
 
 
     /**
      * Leaves a instance_material element.
      */
 
     private void leaveInstanceMaterial()
     {
         this.geometryInstance.getMaterialBinding().getCommonTechnique()
                 .getMaterialInstances().add(this.materialInstance);
         this.materialInstance = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a instance_geometry element.
      */
 
     private void leaveInstanceGeometry()
     {
         this.node.getGeometryInstances().add(this.geometryInstance);
         this.geometryInstance = null;
         leaveElement();
     }
 
 
     /**
      * Enters a instance_light element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterInstanceLight(final Attributes attributes)
     {
         final String urlString = attributes.getValue("url");
         URI url;
         try
         {
             url = new URI(urlString);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(urlString + " is not a valid URI: " + e,
                 e);
         }
         final LightInstance lightInstance = new LightInstance(url);
         lightInstance.setName(attributes.getValue("name"));
         lightInstance.setSid(attributes.getValue("sid"));
         this.node.getLightInstances().add(lightInstance);
         enterElement(ParserMode.INSTANCE_LIGHT);
     }
 
 
     /**
      * Enters a instance_camera element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterInstanceCamera(final Attributes attributes)
     {
         final String urlString = attributes.getValue("url");
         URI url;
         try
         {
             url = new URI(urlString);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(urlString + " is not a valid URI: " + e,
                 e);
         }
         final CameraInstance cameraInstance = new CameraInstance(url);
         cameraInstance.setName(attributes.getValue("name"));
         cameraInstance.setSid(attributes.getValue("sid"));
         this.node.getCameraInstances().add(cameraInstance);
         enterElement(ParserMode.INSTANCE_CAMERA);
     }
 
 
     /**
      * Leaves a node element.
      */
 
     private void leaveNode()
     {
         final Element element = this.node.getParent();
         if (element instanceof Node)
             this.node = (Node) element;
         else
             this.node = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a visual_scene element.
      */
 
     private void leaveVisualScene()
     {
         this.visualSceneLibrary.getVisualScenes().add(this.visualScene);
         this.visualScene = null;
         leaveElement();
     }
 
 
     /**
      * Leaves a library_visual_scenes element.
      */
 
     private void leaveLibraryVisualScenes()
     {
         this.document.getVisualSceneLibraries().add(this.visualSceneLibrary);
         this.visualSceneLibrary = null;
         leaveElement();
     }
 
 
     /**
      * Enters a scene element.
      */
 
     private void enterScene()
     {
         this.document.setScene(new Scene());
         enterElement(ParserMode.SCENE);
     }
 
 
     /**
      * Enters a instance_visual_scene element.
      *
      * @param attributes
      *            The element attributes
      */
 
     private void enterInstanceVisualScene(final Attributes attributes)
     {
         final String urlString = attributes.getValue("url");
         URI url;
         try
         {
             url = new URI(urlString);
         }
         catch (final URISyntaxException e)
         {
             throw new ParserException(urlString + " is not a valid URI: " + e,
                 e);
         }
         final VisualSceneInstance instance = new VisualSceneInstance(url);
         instance.setName(attributes.getValue("name"));
         instance.setSid(attributes.getValue("sid"));
         this.document.getScene().setVisualSceneInstance(instance);
         enterElement(ParserMode.INSTANCE_VISUAL_SCENE);
     }
 }
