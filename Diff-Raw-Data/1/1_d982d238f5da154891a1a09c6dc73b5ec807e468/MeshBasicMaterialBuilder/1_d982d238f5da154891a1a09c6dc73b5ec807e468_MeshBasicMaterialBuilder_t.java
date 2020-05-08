 /*
  * (c) 2011 by Oliver Damm, Am Wasserberg 8, 22869 Schenefeld 
  */
 package net.blimster.gwt.threejs.materials;
 
 import net.blimster.gwt.threejs.textures.CombineOperation;
 import net.blimster.gwt.threejs.textures.Texture;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 /**
  * @author Oliver Damm
  */
 public final class MeshBasicMaterialBuilder extends JavaScriptObject
 {
 
     protected MeshBasicMaterialBuilder()
     {
     }
 
     protected static MeshBasicMaterialBuilder create()
     {
 
 	return JavaScriptObject.createObject().cast();
 
     }
 
     public native MeshBasicMaterialBuilder color(int color)
     /*-{
 
 		this['color'] = color;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder map(Texture map)
     /*-{
 
 		this['map'] = map;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder lightMap(Texture leightMap)
     /*-{
 
 		this['lightMap'] = leightMap;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder envMap(Texture envMap)
     /*-{
 
 		this['envMap'] = envMap;
 		return this;
 
     }-*/;
 
     public MeshBasicMaterialBuilder combine(CombineOperation combine)
     {
 	return combine(combine.intValue());
     }
 
     private native MeshBasicMaterialBuilder combine(int combine)
     /*-{
 
 		this['combine'] = combine;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder reflectivity(double reflectivity)
     /*-{
 
 		this['reflectivity'] = reflectivity;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder refractionRatio(double refractionRatio)
     /*-{
 
 		this['refractionRatio'] = refractionRatio;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder fog(boolean fog)
     /*-{
 
 		this['fog'] = fog;
 		return this;
 
     }-*/;
 
     public MeshBasicMaterialBuilder shading(Shading shading)
     {
 	return shading(shading.intValue());
     }
 
     private native MeshBasicMaterialBuilder shading(int shading)
     /*-{
 
 		this['shading'] = shading;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder wireframe(boolean wireframe)
     /*-{
 
 		this['wireframe'] = wireframe;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder wireframeLinewidth(double wireframeLinewidth)
     /*-{
 
 		this['wireframeLinewidth'] = wireframeLinewidth;
 		return this;
 
     }-*/;
 
     public MeshBasicMaterialBuilder wireframeLinecap(LineCap wireframeLinecap)
     {
 	return wireframeLinecap(wireframeLinecap.stringValue());
     }
 
     private native MeshBasicMaterialBuilder wireframeLinecap(String wireframeLinecap)
     /*-{
 
 		this['wireframeLinecap'] = wireframeLinecap;
 		return this;
 
     }-*/;
 
     public MeshBasicMaterialBuilder wireframeLinejoin(LineCap wireframeLinejoin)
     {
 	return wireframeLinejoin(wireframeLinejoin.stringValue());
     }
 
     private native MeshBasicMaterialBuilder wireframeLinejoin(String wireframeLinejoin)
     /*-{
 
 		this['wireframeLinejoin'] = wireframeLinejoin;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder vertexColors(boolean vertexColors)
     /*-{
 
 		this['vertexColors'] = vertexColors;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder skinning(boolean skinning)
     /*-{
 
 		this['skinning'] = skinning;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterialBuilder morphTargets(boolean morphTargets)
     /*-{
 
 		this['morphTargets'] = morphTargets;
 		return this;
 
     }-*/;
 
     public native MeshBasicMaterial build()
     /*-{
 
 		return new $wnd.THREE.MeshBasicMaterial(this);
 
     }-*/;
 
 }
