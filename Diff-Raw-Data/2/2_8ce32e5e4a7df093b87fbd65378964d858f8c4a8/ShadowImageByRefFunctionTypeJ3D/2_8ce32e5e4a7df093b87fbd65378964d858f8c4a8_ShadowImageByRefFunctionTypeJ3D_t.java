 //
 // ShadowImageByRefFunctionTypeJ3D.java
 //
 
 /*
 VisAD system for interactive analysis and visualization of numerical
 data.  Copyright (C) 1996 - 2009 Bill Hibbard, Curtis Rueden, Tom
 Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
 Tommy Jasmin.
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Library General Public
 License as published by the Free Software Foundation; either
 version 2 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Library General Public License for more details.
 
 You should have received a copy of the GNU Library General Public
 License along with this library; if not, write to the Free
 Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 MA 02111-1307, USA
 */
 
 package visad.bom;
 
 import visad.*;
 import visad.data.CachedBufferedByteImage;
 import visad.java3d.*;
 import visad.data.mcidas.BaseMapAdapter;
 import visad.data.mcidas.AreaAdapter;
 import visad.data.gif.GIFForm;
 import visad.util.Util;
 
 import javax.media.j3d.*;
 
 import java.io.*;
 
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.rmi.*;
 
 import java.net.URL;
 
 import java.awt.event.*;
 import javax.swing.*;
 import java.awt.color.*;
 import java.awt.image.*;
 
 /**
    The ShadowImageFunctionTypeJ3D class shadows the FunctionType class for
    ImageRendererJ3D, within a DataDisplayLink, under Java3D.<P>
 */
 public class ShadowImageByRefFunctionTypeJ3D extends ShadowFunctionTypeJ3D {
 
   private static final int MISSING1 = Byte.MIN_VALUE;      // least byte
 
   private VisADImageNode imgNode = null;
   private VisADImageNode prevImgNode = null;
   private int prevDataWidth = -1;
   private int prevDataHeight = -1;
   private int prevNumImages = -1;
 
   //- Ghansham (New variables introduced to preserve scaled values and colorTables)
   private byte scaled_Bytes[][];  //scaled byte values 
   private float scaled_Floats[][];  //scaled Float Values
   private float rset_scalarmap_lookup[][]; //GHANSHAM:30AUG2011 create a lookup for rset FlatField range values
 
   private byte[][] itable; //For single band
   private byte[][] fast_table; //For fast_lookup
   private byte[][][] threeD_itable; //for multiband
 
   private float[][] color_values; //special case
   private boolean first_time; //This variable indicates the first tile of the image.
   //------------------------------------------------------------------------------
 
   AnimationControlJ3D animControl = null;
 
   private boolean reuse = false;
   private boolean reuseImages = false;
 
   int[] inherited_values = null;
   ShadowFunctionOrSetType adaptedShadowType = null;
   int levelOfDifficulty = -1;
 
   //REUSE GEOMETRY/COLORBYTE VARIABLES (STARTS HERE)
   boolean regen_colbytes = false;
   boolean regen_geom = false;
   boolean apply_alpha = false;
   //REUSE GEOMETRY/COLORBYTE VARIABLES (ENDS HERE)
 
   public ShadowImageByRefFunctionTypeJ3D(MathType t, DataDisplayLink link, ShadowType parent) 
          throws VisADException, RemoteException {
     super(t, link, parent);
   }
 
   public ShadowImageByRefFunctionTypeJ3D(MathType t, DataDisplayLink link, ShadowType parent,
                   int[] inherited_values, ShadowFunctionOrSetType adaptedShadowType, int levelOfDifficulty)
          throws VisADException, RemoteException {
     super(t, link, parent);
     this.inherited_values  = inherited_values;
     this.adaptedShadowType = adaptedShadowType;
     this.levelOfDifficulty = levelOfDifficulty;
   }
 
   //REUSE GEOMETRY/COLORBYTE UTILITY METHODS (STARTS HERE)
    /*This method returns two things:
 	1. whether any spatial maps has return true in checkTicks() function 
 	2. Current ZAxis value 
   */
   private Object[] findSpatialMapTicksAndCurrZValue(ShadowFunctionOrSetType MyAdaptedShadowType, DisplayImpl display, 
 				      float  default_values[], float value_array[], int valueToScalar[], DataRenderer renderer, 
 				      DataDisplayLink link, int valueArrayLength) throws VisADException, DisplayException {
     ShadowRealTupleType Domain = MyAdaptedShadowType.getDomain();
     ShadowRealType[] DomainComponents = MyAdaptedShadowType.getDomainComponents();
     ShadowRealTupleType domain_reference = Domain.getReference();
     ShadowRealType[] DC = DomainComponents;
     if (domain_reference != null && domain_reference.getMappedDisplayScalar()) {
 	DC = MyAdaptedShadowType.getDomainReferenceComponents();
     }
 
     int[] tuple_index = new int[3];
     DisplayTupleType spatial_tuple = null;
     boolean spatial_maps_check_ticks = false;
     for (int i=0; i<DC.length; i++) {
       Enumeration maps = DC[i].getSelectedMapVector().elements();
       ScalarMap map = (ScalarMap) maps.nextElement();
       if (map.checkTicks(renderer, link)) {
               spatial_maps_check_ticks = true;
       }
       DisplayRealType real = map.getDisplayScalar();
       spatial_tuple = real.getTuple();
       if (spatial_tuple == null) {
         /*throw new DisplayException("texture with bad tuple: " +
                                    "ShadowImageFunctionTypeJ3D.doTransform");*/
 	return null;
       }
       tuple_index[i] = real.getTupleIndex();
       if (maps.hasMoreElements()) {
         /*throw new DisplayException("texture with multiple spatial: " +
                                    "ShadowImageFunctionTypeJ3D.doTransform");*/
 	return null;
       }
     } 
 
 
     // get spatial index not mapped from domain_set
     tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
     DisplayRealType real = (DisplayRealType) spatial_tuple.getComponent(tuple_index[2]);
     int value2_index = display.getDisplayScalarIndex(real);
     float value2 = default_values[value2_index];
     for (int i=0; i<valueArrayLength; i++) {
       if (inherited_values[i] > 0 && real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
         value2 = value_array[i];
         break;
       }
     }
     tuple_index = null;
     Object ret_values[] = new Object[2];
     ret_values[0] = spatial_maps_check_ticks;
     ret_values[1] = value2;
     return ret_values;
   }
 
   /*This method retuns whether any of the rangemap has return true in checkTicks()*/
   private boolean findRadianceMapColorControlCheckTicks(ScalarMap cmap, ScalarMap cmaps[], DataRenderer renderer, DataDisplayLink link) {
 	BaseColorControl cc;
 	boolean color_map_changed = false;
 	if (cmap!= null) {
         	cc = (BaseColorControl) cmap.getControl();
         	color_map_changed = (cmap.checkTicks(renderer, link) || cc.checkTicks(renderer,link));
 	} else if (cmaps !=null) {
         	for (int i = 0; i < cmaps.length; i++) {
             		cc = (BaseColorControl) cmaps[i].getControl();
 			if (null != cc) {
 	            		if (cc.checkTicks(renderer,link) || cmaps[i].checkTicks(renderer, link)) {
         	        		color_map_changed = true;
 					break;
             			}
 			} else {
 				if (cmaps[i].checkTicks(renderer, link)) {
                                         color_map_changed = true;
 					break;
                                 }
 			}
         	}
 	}
 	return color_map_changed;
   }
   /*This method just applies the texture on the already generated geometry.
     This is used when only colorbytes are generated and geometry is reused. 
     This does away with buildTexture(Linear/Curve) when geometry is reused */
   private void applyTexture(Shape3D shape, VisADImageTile tile, boolean apply_alpha, float constant_alpha) {
         Appearance app = shape.getAppearance();
 	if (regen_colbytes) {
               if (animControl == null) {
                   imgNode.setCurrent(0);
               }
 	}
 
         if (apply_alpha) {
             TransparencyAttributes transp_attribs = app.getTransparencyAttributes();
             if (null == transp_attribs) {
                 transp_attribs = new TransparencyAttributes();
                 transp_attribs.setTransparencyMode(TransparencyAttributes.BLENDED);
                 transp_attribs.setTransparency(constant_alpha);
                 transp_attribs.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
                 app.setTransparencyAttributes(transp_attribs);
             } else {
                 transp_attribs.setTransparency(constant_alpha);
             }
         }
   }
 
 
     /* This is the real nasty logic that decides following things:
 	1. Regenerate gometry
 	2. Regenerate ColorBytes
 	3. Change in alpha
 	Before doing this it inializes range ScalarMaps, constant_alpha value.
 	It also takes out the terminal ShadowType required in case of animations
     */
 	//GHANSHAM:30AUG2011 Changed the signaure of initRegenFlags.. passing the ShadowFunctionOrSetType, constant_lapha, cmap and cmaps
   private void initRegenFlags(ImageRendererJ3D imgRenderer, ShadowFunctionOrSetType MyAdaptedShadowType, 
 			float constant_alpha, ScalarMap cmap, ScalarMap cmaps[], Data data, DisplayImpl display, 
 			float default_values[], float[] value_array, int []valueToScalar, int valueArrayLength, 
 			DataDisplayLink link, int curved_size) throws BadMappingException, VisADException {
 	
 	/*The nasty logic starts from here
 		Retrieves the curve size, zaxis value, alpha, ff hashcode  value from Renderer class.
 		Compares them with current values and does other checks.
 		Finally store the current values for above variables in the renderer class.*/
        	int last_curve_size = imgRenderer.getLastCurveSize();
         float last_zaxis_value = imgRenderer.getLastZAxisValue();
         float last_alpha_value = imgRenderer.getLastAlphaValue();
         long last_data_hash_code = imgRenderer.getLastDataHashCode();
         long current_data_hash_code = data.hashCode();
 	
 	boolean last_adjust_projection_seam = imgRenderer.getLastAdjustProjectionSeam(); //27FEB2012: Projection Seam Change Bug Fix
 	boolean current_adjust_projection_seam = adaptedShadowType.getAdjustProjectionSeam(); //27FEB2012: Projection Seam Change Bug Fix
 	
 	Object map_ticks_z_value[] = findSpatialMapTicksAndCurrZValue(MyAdaptedShadowType, display, default_values, value_array, valueToScalar, imgRenderer, link, valueArrayLength);
 	if (null == map_ticks_z_value) {
 		return;
 	}
 	float current_zaxis_value = Float.parseFloat(map_ticks_z_value[1].toString());
         if ((-1 != last_curve_size) && Float.isNaN(last_zaxis_value) && (-1 == last_data_hash_code)) { //First Time
                 regen_colbytes = true;
                 regen_geom = true;
                 apply_alpha = true;
         } else {
                 boolean data_hash_code_changed = (current_data_hash_code != last_data_hash_code);
                 if (data_hash_code_changed) { //dataref.setData()
                         regen_colbytes = true;
                         regen_geom = true;
                         apply_alpha =true;
                 } else {
                         boolean spatial_maps_check_ticks = Boolean.parseBoolean(map_ticks_z_value[0].toString());
                         boolean zaxis_value_changed = (Float.compare(last_zaxis_value, current_zaxis_value) != 0);
                         boolean curve_texture_value_change = (last_curve_size != curved_size);
                         boolean alpha_changed = (Float.compare(constant_alpha, last_alpha_value) != 0);
                         boolean radiancemap_colcontrol_check_ticks = findRadianceMapColorControlCheckTicks(cmap, cmaps, imgRenderer, link);
 			boolean projection_seam_changed = (current_adjust_projection_seam != last_adjust_projection_seam); //27FEB2012: Projection Seam Change Bug Fix
                         if  (spatial_maps_check_ticks ||  zaxis_value_changed || curve_texture_value_change || projection_seam_changed) { //change in geometry 27FEB2012: Projection Seam Change Bug Fix
                                 regen_geom = true;
                         } else if (alpha_changed) { //change in alpha value
                                 apply_alpha = true;
 			} else if (radiancemap_colcontrol_check_ticks) { //change in Radiance ScalarMaps or ColorTable
                                 regen_colbytes = true;
                         } else { //Assuming that ff.setSamples() has been called.
                                 regen_colbytes = true;
                         }
                 }
         }
         imgRenderer.setLastCurveSize(curved_size);
         imgRenderer.setLastZAxisValue(current_zaxis_value);
         imgRenderer.setLastAlphaValue(constant_alpha);
 	imgRenderer.setLastAdjustProjectionSeam(current_adjust_projection_seam); //27FEB2012: Projection Seam Change Bug Fix
         imgRenderer.setLastDataHashCode(current_data_hash_code);
     }
     //REUSE GEOMETRY/COLORBYTE UTILITY METHODS (ENDS HERE)
 
   // transform data into a depiction under group
   public boolean doTransform(Object group, Data data, float[] value_array,
                              float[] default_values, DataRenderer renderer)
          throws VisADException, RemoteException {
 	
     	DataDisplayLink link = renderer.getLink();
     	// return if data is missing or no ScalarMaps
     	if (data.isMissing()) {
       		((ImageRendererJ3D) renderer).markMissingVisADBranch();
       		return false;
     	}
 
     	if (levelOfDifficulty == -1) {
       		levelOfDifficulty = getLevelOfDifficulty();
     	}
 
     	if (levelOfDifficulty == NOTHING_MAPPED) return false;
 
 
     	if (group instanceof BranchGroup && ((BranchGroup) group).numChildren() > 0) {
        		Node g = ((BranchGroup) group).getChild(0);
         	// WLH 06 Feb 06 - support switch in a branch group.
         	if (g instanceof BranchGroup && ((BranchGroup) g).numChildren() > 0) {
             		reuseImages = true;
         	}
     	}
 
     	DisplayImpl display = getDisplay();
 
     	int cMapCurveSize = (int) default_values[display.getDisplayScalarIndex(Display.CurvedSize)];
 
     	int curved_size = (cMapCurveSize > 0) ? cMapCurveSize : display.getGraphicsModeControl().getCurvedSize();
  
     	// length of ValueArray
     	int valueArrayLength = display.getValueArrayLength();
     	// mapping from ValueArray to DisplayScalar
     	int[] valueToScalar = display.getValueToScalar();
 	//GHANSHAM:30AUG2011 Restrutured the code  to extract the constant_alpha, cmap, cmaps and ShadowFunctionType so that they can be passed to initRegenFlags method
 	if (adaptedShadowType == null) {
       		adaptedShadowType = (ShadowFunctionOrSetType) getAdaptedShadowType();
     	}
 
 	boolean anyContour = adaptedShadowType.getAnyContour();
     	boolean anyFlow = adaptedShadowType.getAnyFlow();
     	boolean anyShape = adaptedShadowType.getAnyShape();
     	boolean anyText = adaptedShadowType.getAnyText();
 
     	if (anyContour || anyFlow || anyShape || anyText) {
 		throw new BadMappingException("no contour, flow, shape or text allowed");
     	}
 
 	FlatField imgFlatField = null;
 	Set domain_set = ((Field) data).getDomainSet();
 
     	ShadowRealType[] DomainComponents = adaptedShadowType.getDomainComponents();
 	int numImages = 1;
 	if (!adaptedShadowType.getIsTerminal()) {
 
       		Vector domain_maps = DomainComponents[0].getSelectedMapVector();
       		ScalarMap amap = null;
       		if (domain_set.getDimension() == 1 && domain_maps.size() == 1) {
         		ScalarMap map = (ScalarMap) domain_maps.elementAt(0);
         		if (Display.Animation.equals(map.getDisplayScalar())) {
           			amap = map;
         		}
       		}
       		if (amap == null) {
         		throw new BadMappingException("time must be mapped to Animation");
       		}
       		animControl = (AnimationControlJ3D) amap.getControl();
 
         	numImages = domain_set.getLength();
 
       		adaptedShadowType = (ShadowFunctionOrSetType) adaptedShadowType.getRange();
       		DomainComponents = adaptedShadowType.getDomainComponents();
       		imgFlatField = (FlatField) ((FieldImpl)data).getSample(0);
     	} else {
       		imgFlatField = (FlatField)data;
     	}
 
 	 // check that range is single RealType mapped to RGB only
     	ShadowRealType[] RangeComponents = adaptedShadowType.getRangeComponents();
     	int rangesize = RangeComponents.length;
     	if (rangesize != 1 && rangesize != 3) {
       		throw new BadMappingException("image values must single or triple");
     	}
     	ScalarMap cmap  = null;
     	ScalarMap[] cmaps = null;
     	int[] permute = {-1, -1, -1};
     	boolean hasAlpha = false;
     	if (rangesize == 1) {
       		Vector mvector = RangeComponents[0].getSelectedMapVector();
                 if (mvector.size() != 1) {
                         throw new BadMappingException("image values must be mapped to RGB only");
                 }
                 cmap = (ScalarMap) mvector.elementAt(0);
                 if (Display.RGB.equals(cmap.getDisplayScalar())) {
 
                 } else if (Display.RGBA.equals(cmap.getDisplayScalar())) {
                         hasAlpha = true;
                 } else {
                         throw new BadMappingException("image values must be mapped to RGB or RGBA");
                 }
     	} else {
       		cmaps = new ScalarMap[3];
       		for (int i=0; i<3; i++) {
         		Vector mvector = RangeComponents[i].getSelectedMapVector();
         		if (mvector.size() != 1) {
           			throw new BadMappingException("image values must be mapped to color only");
         		}
        			cmaps[i] = (ScalarMap) mvector.elementAt(0);
         		if (Display.Red.equals(cmaps[i].getDisplayScalar())) {
           			permute[0] = i;
 	        	} else if (Display.Green.equals(cmaps[i].getDisplayScalar())) {
         	  		permute[1] = i;
         		} else if (Display.Blue.equals(cmaps[i].getDisplayScalar())) {
 	          		permute[2] = i;
         		} else if (Display.RGB.equals(cmaps[i].getDisplayScalar())) { //Inserted by Ghansham for Mapping all the three scalarMaps to Display.RGB (starts here) 
                 		permute[i] = i;
 	        	} else {               ////Inserted by Ghansham for Mapping all the three scalarMaps to Display.RGB(ends here)
         	  		throw new BadMappingException("image values must be mapped to Red, Green or Blue only");
         		}
       		}
       		if (permute[0] < 0 || permute[1] < 0 || permute[2] < 0) {
        			throw new BadMappingException("image values must be mapped to Red, Green or Blue only");
       		}
       		//Inserted by Ghansham for Checking that all should be mapped to Display.RGB or not even a single one should be mapped to Display.RGB(starts here)
         	//This is to check if there is a single Display.RGB ScalarMap
       		int indx = -1;
 		for (int i = 0; i < 3; i++) {
       			if (cmaps[i].getDisplayScalar().equals(Display.RGB)) {
         			indx = i;
 	                	break;
         	        }
         	}
 
         	if (indx != -1){    //if there is a even a single Display.RGB ScalarMap, others must also Display.RGB only
                 	for (int i = 0; i < 3; i++) {
                         	if (i !=indx && !(cmaps[i].getDisplayScalar().equals(Display.RGB))) {
                                 	throw new BadMappingException("image values must be mapped to (Red, Green, Blue) or (RGB,RGB,RGB) only");
                         	}
                 	}
         	}
         	//Inserted by Ghansham for Checking that all should be mapped to Display.RGB or not even a single one should be mapped to Display.RGB(Ends here)        
     	}
 
     	float constant_alpha = default_values[display.getDisplayScalarIndex(Display.Alpha)];
     	int color_length;
     	ImageRendererJ3D imgRenderer = (ImageRendererJ3D) renderer;
     	int imageType = imgRenderer.getSuggestedBufImageType();
     	if (imageType == BufferedImage.TYPE_4BYTE_ABGR) {
       		color_length = 4;
       		if (!hasAlpha) {
         		color_length = 3;
         		imageType = BufferedImage.TYPE_3BYTE_BGR;
       		}
     	} else if (imageType == BufferedImage.TYPE_3BYTE_BGR) {
       		color_length = 3;
     	} else if (imageType == BufferedImage.TYPE_USHORT_GRAY) {
       		color_length = 2;
     	} else if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
       		color_length = 1;
     	} else {
       		// we shouldn't ever get here because the renderer validates the 
       		// imageType before we get it, but just in case...
       		throw new VisADException("renderer returned unsupported image type");
     	}
     	if (color_length == 4) constant_alpha = Float.NaN; // WLH 6 May 2003
 
 
 	//REUSE GEOMETRY/COLORBYTE LOGIC (STARTS HERE)
 	regen_colbytes = false;
   	regen_geom = false;
   	apply_alpha = false; 
 	initRegenFlags((ImageRendererJ3D)renderer, adaptedShadowType, constant_alpha, cmap, cmaps, data, display, default_values, value_array, valueToScalar, valueArrayLength, link, curved_size);
 	if(!reuseImages) {
 		regen_geom = true;
 		regen_colbytes = true;
 		apply_alpha = true;
 	}
 
         /**
         System.err.println("Regenerate Color Bytes:" + regen_colbytes);
         System.err.println("Regenerate Geometry:" + regen_geom);
         System.err.println("Apply Alpha:" + apply_alpha);
 	System.err.println("ReuseImages:" + reuseImages);
         */
         
 	//REUSE GEOMETRY/COLORBYTE LOGIC (ENDS HERE)
      	prevImgNode = ((ImageRendererJ3D)renderer).getImageNode();
 
      	BranchGroup bgImages = null;
 
 	/*REUSE GEOM/COLBYTE: Replaced reuse with reuseImages. Earlier else part of this decision was never being used.
 	  The reason was reuse was always set to false. Compare with your version.
 	  Added one extra line in the else part where I extract the bgImages from the switch.
 	  Now else part occurs when either reuse_colbytes or regen_geom is true.
 	  But when regen_colbytes and regen_geom both are true, then I assume that a new flatfield is set so
 	  go with the if part.
 	*/
 	if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE GEOM/COLBYTE:Earlier reuse variable was used. Replaced it with reuseImages.
 								//Added regen_colbytes and regen_geom. 
 								//This is used when either its first time or full new data has been with different dims.
 		BranchGroup branch = new BranchGroup();
        		branch.setCapability(BranchGroup.ALLOW_DETACH);
        		branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        		branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        		branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        		Switch swit = (Switch) makeSwitch();
 
        		imgNode = new VisADImageNode();
 
        		bgImages = new BranchGroup();
        		bgImages.setCapability(BranchGroup.ALLOW_DETACH);
        		bgImages.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        		bgImages.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        		bgImages.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 
        		swit.addChild(bgImages);
        		swit.setWhichChild(0);
        		branch.addChild(swit);
 
        		imgNode.setBranch(branch);
        		imgNode.setSwitch(swit);
        		((ImageRendererJ3D)renderer).setImageNode(imgNode);
 
        		if ( ((BranchGroup) group).numChildren() > 0 ) {
          		((BranchGroup)group).setChild(branch, 0);
        		} else {
          		((BranchGroup)group).addChild(branch);
          /*
          // make sure group is live.  group not empty (above addChild)
          if (group instanceof BranchGroup) {
            ((ImageRendererJ3D) renderer).setBranchEarly((BranchGroup) group);
          }
          */
        		}
 	} else { //REUSE GEOM/COLBYTE: If its not the first time. And the dims have not changed but either color bytes or geometry has changed.
        		imgNode = ((ImageRendererJ3D)renderer).getImageNode();
        		bgImages = (BranchGroup) imgNode.getSwitch().getChild(0);	//REUSE GEOM/COLBYTE:Extract the bgImages from the avaialable switch
      	} 
 
 
     GraphicsModeControl mode = (GraphicsModeControl)
           display.getGraphicsModeControl().clone();
 
     // get some precomputed values useful for transform
     // mapping from ValueArray to MapVector
     int[] valueToMap = display.getValueToMap();
     Vector MapVector = display.getMapVector();
 
     Unit[] dataUnits = null;
     CoordinateSystem dataCoordinateSystem = null;
 
 
    if (animControl != null) {
 	Switch swit = new SwitchNotify(imgNode, numImages);  
       	((AVControlJ3D) animControl).addPair((Switch) swit, domain_set, renderer);
       	((AVControlJ3D) animControl).init();
     }
 
 
     domain_set = imgFlatField.getDomainSet();
     dataUnits = ((Function) imgFlatField).getDomainUnits();
     dataCoordinateSystem =
       ((Function) imgFlatField).getDomainCoordinateSystem();
 
     int domain_length = domain_set.getLength();
     int[] lengths = ((GriddedSet) domain_set).getLengths();
     int data_width = lengths[0];
     int data_height = lengths[1];
 
     imgNode.numImages = numImages;
     imgNode.data_width = data_width;
     imgNode.data_height = data_height;
 
     int texture_width_max = link.getDisplay().getDisplayRenderer().getTextureWidthMax();
     int texture_height_max = link.getDisplay().getDisplayRenderer().getTextureWidthMax();
 
     int texture_width = textureWidth(data_width);
     int texture_height = textureHeight(data_height);
 
     if (reuseImages) {
       if (prevImgNode.numImages != numImages || 
           prevImgNode.data_width != data_width || prevImgNode.data_height != data_height) {
         reuseImages = false;
       }
     }
     if (reuseImages) {
       imgNode.numChildren = prevImgNode.numChildren;
       imgNode.imageTiles = prevImgNode.imageTiles;
     }
     else {
 	Mosaic mosaic = new Mosaic(data_height, texture_height_max, data_width, texture_width_max);
       	for (Iterator iter = mosaic.iterator(); iter.hasNext();) {
         	Tile tile = (Tile) iter.next();
         	imgNode.addTile(new VisADImageTile(numImages, tile.height, tile.y_start, tile.width, tile.x_start));
       	}
     }
 
     prevImgNode = imgNode;
 
     ShadowRealTupleType Domain = adaptedShadowType.getDomain();
     Unit[] domain_units = ((RealTupleType) Domain.getType()).getDefaultUnits();
     float[] constant_color = null;
 
     // check that domain is only spatial
     if (!Domain.getAllSpatial() || Domain.getMultipleDisplayScalar()) {
       throw new BadMappingException("domain must be only spatial");
     }
 
     // check domain and determine whether it is square or curved texture
     boolean isTextureMap = adaptedShadowType.getIsTextureMap() &&
                              (domain_set instanceof Linear2DSet ||
                               (domain_set instanceof LinearNDSet &&
                                domain_set.getDimension() == 2)) && 
                              (domain_set.getManifoldDimension() == 2);
 
 
     boolean curvedTexture = adaptedShadowType.getCurvedTexture() &&
                             !isTextureMap &&
                             curved_size > 0 &&
                             (domain_set instanceof Gridded2DSet ||
                              (domain_set instanceof GriddedSet &&
                               domain_set.getDimension() == 2)) &&
                              (domain_set.getManifoldDimension() == 2);
 
 	if (group instanceof BranchGroup) {
         	((ImageRendererJ3D) renderer).setBranchEarly((BranchGroup) group);
         }
 
     first_time =true; //Ghansham: this variable just indicates to makeColorBytes whether it's the first tile of the image
     boolean branch_added = false;
     if (isTextureMap) { // linear texture
 
         if (imgNode.getNumTiles() == 1) {
           VisADImageTile tile = imgNode.getTile(0);
 	  if (regen_colbytes) { //REUSE COLBYTES: regenerate only if required
 	          makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
         	              data_width, data_height, imageType, tile, 0);
 	  }
 		if (regen_geom) { //REUSE : REGEN GEOM  regenerate the geometry
           		buildLinearTexture(bgImages, domain_set, dataUnits, domain_units, default_values, DomainComponents,
                              valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha, 
                              value_array, constant_color, display, tile);
 		} else { //REUSE Reuse the branch fully along with geometry. Just apply the colorbytes(Buffered Image)
                     BranchGroup Branch_L1 = (BranchGroup) bgImages.getChild(0);
                     Shape3D shape = (Shape3D) Branch_L1.getChild(0);
                     applyTexture(shape, tile, apply_alpha, constant_alpha);
                 }
 
         }
         else {
           BranchGroup branch = null;
 	  //if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE: Make a fresh branch
 	  if (!reuseImages) {
 	  	branch = new BranchGroup();
           	branch.setCapability(BranchGroup.ALLOW_DETACH);
           	branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
           	branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
           	branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 	   } else { //REUSE the branch
 		branch = (BranchGroup) bgImages.getChild(0);
 	   }
 	  int branch_tile_indx = 0; //REUSE: to get the branch for a tile in case of multi-tile rendering
           for (Iterator iter = imgNode.getTileIterator(); iter.hasNext();) {
              VisADImageTile tile = (VisADImageTile) iter.next();
 
 		if (regen_colbytes) { //REUSE COLBYTES: regenerate only if required
 	                makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                       		data_width, data_height, imageType, tile, 0);
                 	first_time = false; //Ghansham: setting 'first_time' variable false after the first tile has been generated
 		}
 		if (regen_geom) { //REUSE: Regenerate the geometry
 
               		float[][] g00 = ((GriddedSet)domain_set).gridToValue(
                    			new float[][] {{tile.xStart}, {tile.yStart}});
               		float[][] g11 = ((GriddedSet)domain_set).gridToValue(
                    			new float[][] {{tile.xStart+tile.width-1}, {tile.yStart+tile.height-1}});
 
               		double x0 = g00[0][0];
               		double x1 = g11[0][0];
               		double y0 = g00[1][0];
               		double y1 = g11[1][0];
               		Set dset = new Linear2DSet(x0, x1, tile.width, y0, y1, tile.height);
 
               		BranchGroup branch1 = null;
 			if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE: Make a fresh branch for each tile
 				branch1 = new BranchGroup();
         	      		branch1.setCapability(BranchGroup.ALLOW_DETACH);
               			branch1.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
               			branch1.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
               			branch1.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 			} else { //REUSE: Reuse the already built branch for each tile
 				branch1 = (BranchGroup) branch.getChild(branch_tile_indx);
 			}
 
               		buildLinearTexture(branch1, dset, dataUnits, domain_units, default_values, DomainComponents,
                                  valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha, 
                                  value_array, constant_color, display, tile);
 			if (!reuseImages|| (regen_colbytes && regen_geom)) {
 		        	branch.addChild(branch1);
 			}
 			g00 = null;
 			g11 = null;
 			dset = null;
 		} else { //REUSE Reuse the branch fully along with geometry. Just apply the colorbytes(Buffered Image)
                         BranchGroup branch1 = (BranchGroup) branch.getChild(branch_tile_indx);
                         BranchGroup branch2 = (BranchGroup) branch1.getChild(0); //Beause we create a branch in textureToGroup
                         Shape3D shape = (Shape3D) branch2.getChild(0);
                         applyTexture(shape, tile, apply_alpha, constant_alpha);
                 }
 		if (0 == branch_tile_indx) { //Add the branch to get rendered as early as possible
                         if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE : Add a new branch if created
                                 if (((Group) bgImages).numChildren() > 0) {
                                         ((Group) bgImages).setChild(branch, 0);
                                 } else {
                                         ((Group) bgImages).addChild(branch);
                                 }
                         }
                 }
 
                	branch_tile_indx++;
 
           }
 
         }
       } // end if (isTextureMap)
       else if (curvedTexture) {
 
         int[] lens = ((GriddedSet)domain_set).getLengths();
         int[] domain_lens = lens;
 
         if (imgNode.getNumTiles() == 1) {
           VisADImageTile tile = imgNode.getTile(0);
 	  	if (regen_colbytes) {  //REUSE COLBYTES: regenerate only if required
                 	makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                       		data_width, data_height, imageType, tile,  0);
 		}
 	        if (regen_geom) { //REUSE: REGEN GEOM regenerate 
           		buildCurvedTexture(bgImages, domain_set, dataUnits, domain_units, default_values, DomainComponents,
                              valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha,
                              value_array, constant_color, display, curved_size, Domain,
                              dataCoordinateSystem, renderer, adaptedShadowType, new int[] {0,0},
                              domain_lens[0], domain_lens[1], null, domain_lens[0], domain_lens[1], tile);
 		} else { //REUSE Reuse the branch fully along with geometry. Just apply the colorbytes(Buffered Image)
 			BranchGroup Branch_L1 = (BranchGroup) bgImages.getChild(0);
                     	Shape3D shape = (Shape3D) Branch_L1.getChild(0);
                     	applyTexture(shape, tile, apply_alpha, constant_alpha);
 		}
         }
         else
         {
           float[][] samples = ((GriddedSet)domain_set).getSamples(false);
 
 	  BranchGroup branch = null;
 	if (!reuseImages || (regen_colbytes && regen_geom)) {  //REUSE: Make a fresh branch
 		branch = new BranchGroup();
           	branch.setCapability(BranchGroup.ALLOW_DETACH);
           	branch.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
           	branch.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
           	branch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 	  } else { //REUSE: Reuse already built branch 
 		branch = (BranchGroup) bgImages.getChild(0);
           } 
 	  int branch_tile_indx = 0; //REUSE: to get the branch for a tile in case of multi-tile rendering
           for (Iterator iter = imgNode.getTileIterator(); iter.hasNext();) {
              VisADImageTile tile = (VisADImageTile) iter.next();
 		if (regen_colbytes) { //REUSE COLBYTES: regenerate only if required
                 	makeColorBytesDriver(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                       		data_width, data_height, imageType, tile, 0);
                 	first_time = false; //Ghansham: setting 'first_time' variable false after the first tile has been generated
 		}
 
 		if (regen_geom) { //REUSE REGEN GEOM regenerate geometry 
 			BranchGroup branch1 = null;
 			if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE: Make a fresh branch group for each tile
 				branch1 = new BranchGroup();
                         	branch1.setCapability(BranchGroup.ALLOW_DETACH);
                         	branch1.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
                         	branch1.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
                         	branch1.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 			} else { //REUSE: Reuse the already existing branch for each tile
 				branch1 = (BranchGroup) branch.getChild(branch_tile_indx);
 			}
 			
              		buildCurvedTexture(branch1, null, dataUnits, domain_units, default_values, DomainComponents,
                                 valueArrayLength, inherited_values, valueToScalar, mode, constant_alpha,
                                 value_array, constant_color, display, curved_size, Domain,
                                 dataCoordinateSystem, renderer, adaptedShadowType, 
                                 new int[] {tile.xStart,tile.yStart}, tile.width, tile.height,
                                 samples, domain_lens[0], domain_lens[1], tile);
 
 			if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE: Add newly created branch 
                               branch.addChild(branch1);
 			}
 		} else { //REUSE Reuse the branch fully along with geometry. Just apply the colorbytes(Buffered Image)
 			BranchGroup branch1 = (BranchGroup) branch.getChild(branch_tile_indx);
                         BranchGroup branch2 = (BranchGroup) branch1.getChild(0);
                         Shape3D shape = (Shape3D) branch2.getChild(0);
                         applyTexture(shape, tile, apply_alpha, constant_alpha);
 		}
 		if (0 == branch_tile_indx) { //Add the branch to get rendered as early as possible
 			if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE : Add a new branch if created
                 		if (((Group) bgImages).numChildren() > 0) {
                         		((Group) bgImages).setChild(branch, 0);
                 		} else {
                         		((Group) bgImages).addChild(branch);
                 		}
            		}
 		}
 		branch_tile_indx++;
            }
 
         }
       } // end if (curvedTexture)
       else { // !isTextureMap && !curvedTexture
         throw new BadMappingException("must be texture map or curved texture map");
       } 
 
 
       // make sure group is live.  group not empty (above addChild)
       /*if (group instanceof BranchGroup) {
         ((ImageRendererJ3D) renderer).setBranchEarly((BranchGroup) group);
       }*/
 
 
       for (int k=1; k<numImages; k++) {
         FlatField ff = (FlatField) ((Field)data).getSample(k);
         CoordinateSystem dcs = ff.getDomainCoordinateSystem();
         GriddedSet domSet = (GriddedSet) ff.getDomainSet();
         int[] lens = domSet.getLengths();
 
         // if image dimensions, or dataCoordinateSystem not equal to first image, resample to first
 	if (regen_colbytes) { //REUSE COLBYTES: resample the flatfield only if colorbytes need to be regenerated
 	        if ( (lens[0] != data_width || lens[1] != data_height) || !(dcs.equals(dataCoordinateSystem))) {
          		ff = (FlatField) ff.resample(imgFlatField.getDomainSet(), Data.NEAREST_NEIGHBOR, Data.NO_ERRORS);
         	}
 	}
 
         first_time = true;
         scaled_Bytes = null; //scaled byte values 
         scaled_Floats = null; //scaled Float Values
 	fast_table = null;
 	rset_scalarmap_lookup = null; //GHANSHAM:30AUG2011 create a lookup for rset FlatField range values
         itable = null; //For single band
         threeD_itable = null; //for multiband
         color_values = null; //special case
 
         for (Iterator iter = imgNode.getTileIterator(); iter.hasNext();) {
           VisADImageTile tile = (VisADImageTile) iter.next();
 	  if(regen_colbytes) {	//REUSE COLBYTES: regenerate colobytes only if required
           	makeColorBytesDriver(ff, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                 	data_width, data_height, imageType, tile, k);
            	first_time = false;
 	  }
           //image.bytesChanged(byteData);
         }
       }
 
       cmaps = null;
       first_time = true;
       scaled_Bytes = null; //scaled byte values 
       scaled_Floats = null; //scaled Float Values
       fast_table = null;
       rset_scalarmap_lookup = null; //GHANSHAM:30AUG2011 create a lookup for rset FlatField range values
       itable = null; //For single band
       threeD_itable = null; //for multiband
       color_values = null; //special case
 
 
       ensureNotEmpty(bgImages);
       return false;
   }
 
 
 // This function calls makeColorBytes function (Ghansham)
 public void makeColorBytesDriver(Data imgFlatField, ScalarMap cmap, ScalarMap[] cmaps, float constant_alpha,
               ShadowRealType[] RangeComponents, int color_length, int domain_length, int[] permute,
               int data_width, int data_height,
               int imageType, VisADImageTile tile, int image_index) throws VisADException, RemoteException {
         BufferedImage image = null;
         byte byteData[] = null;
         int tile_width = tile.width;
         int tile_height = tile.height;
         int xStart = tile.xStart;
         int yStart = tile.yStart;
         int texture_width = textureWidth(tile_width);
         int texture_height = textureHeight(tile_height);
 
        if (!reuseImages) {
          image = createImageByRef(texture_width, texture_height, imageType);
          tile.setImage(image_index, image);
        } else {
          //image = (CachedBufferedByteImage) tile.getImage(0);
          image = (BufferedImage) tile.getImage(image_index);
        }
 
        java.awt.image.Raster raster = image.getRaster();
        DataBuffer db = raster.getDataBuffer();
        byteData = ((DataBufferByte)db).getData();
        java.util.Arrays.fill(byteData, (byte)0);
        	makeColorBytes(imgFlatField, cmap, cmaps, constant_alpha, RangeComponents, color_length, domain_length, permute,
                     byteData,
                     data_width, data_height, tile_width, tile_height, xStart, yStart, texture_width, texture_height);
 }
 
 /*  New version contributed by Ghansham (ISRO)
  This function scales the flatfield values and the colortable for the first tile only using the first_time variable. Rest of the time it only
  uses scaled values and color table to generate colorbytes for respective tile. Just see the first_time variable use. That is the only difference between
  this function and earlier function makeColorBytes(). Some new class variables have been introduced to preserve scaled values and colortable.
  They are made null after all the tiles for a single image has been generated. At the end of doTransform(), they are made null.
 */
 public void makeColorBytes(Data data, ScalarMap cmap, ScalarMap[] cmaps, float constant_alpha,
               ShadowRealType[] RangeComponents, int color_length, int domain_length, int[] permute,
               byte[] byteData, int data_width, int data_height, int tile_width, int tile_height, int xStart, int yStart,
               int texture_width, int texture_height)
                 throws VisADException, RemoteException {
         if (cmap != null) {
                 BaseColorControl control = (BaseColorControl) cmap.getControl();
                 float[][] table = control.getTable();
                 Set rset = null;
                 boolean is_default_unit = false;
 
                 if (data instanceof FlatField) {
                         // for fast byte color lookup, need:
                         // 1. range data values are packed in bytes
                         //bytes = ((FlatField) data).grabBytes();
                         if (first_time) {
                                 scaled_Bytes = ((FlatField) data).grabBytes();
                         }
                         // 2. range set is Linear1DSet
                         Set[] rsets = ((FlatField) data). getRangeSets();
                         if (rsets != null) rset = rsets[0];
                         // 3. data Unit equals default Unit
                         RealType rtype = (RealType) RangeComponents[0].getType();
                         Unit def_unit = rtype.getDefaultUnit();
                         if (def_unit == null) {
                                 is_default_unit = true;
                         } else {
                                 Unit[][] data_units = ((FlatField) data).getRangeUnits();
                                 Unit data_unit = (data_units == null) ? null : data_units[0][0];
                                 is_default_unit = def_unit.equals(data_unit);
                         }
                 }
                 if (table != null) {
                         // combine color table RGB components into ints
                         if (first_time) {
                                 itable = new byte[table[0].length][4];
                                 // int r, g, b, a = 255;
                                 int r, g, b;
                                 int c = (int) (255.0 * (1.0f - constant_alpha));
                                 int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                 for (int j=0; j<table[0].length; j++) {
                                         c = (int) (255.0 * table[0][j]);
                                         r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                         c = (int) (255.0 * table[1][j]);
                                         g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                         c = (int) (255.0 * table[2][j]);
                                         b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                         if (color_length == 4) {
                                                 c = (int) (255.0 * table[3][j]);
                                                 a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                         }
                                         itable[j][0] = (byte) r;
                                         itable[j][1] = (byte) g;
                                         itable[j][2] = (byte) b;
                                         itable[j][3] = (byte) a;
                                 }
                         }
                         int tblEnd = table[0].length - 1;
                         // get scale for color table
                         int table_scale = table[0].length;
                         if (data instanceof ImageFlatField && scaled_Bytes != null && is_default_unit) {
                                 if (ImageFlatField.DEBUG) {
                                         System.err.println("ShadowImageFunctionTypeJ3D.doTransform: " + "cmap != null: looking up color values");
                                 }
                                 // avoid unpacking floats for ImageFlatFields
                                 if (first_time) {
                                        	scaled_Bytes[0]= cmap.scaleValues(scaled_Bytes[0], table_scale); 
                                 }
                                 // fast lookup from byte values to color bytes
                                 byte[] bytes0 = scaled_Bytes[0];
 
                                 int k =0;
 				int color_length_times_texture_width = texture_width*color_length;
                                 for (int y=0; y<tile_height; y++) {
                                         int image_col_factor = (y+yStart)*data_width + xStart;
 					k= y*color_length_times_texture_width;
                                         for (int x=0; x<tile_width; x++) {
                                                 int i = x + image_col_factor;
                                                 int j = bytes0[i] & 0xff; // unsigned
                                                 // clip to table
        	                                        int ndx = j < 0 ? 0 : (j > tblEnd ? tblEnd : j);
                	                                if (color_length == 4) {
                        	                                byteData[k] = itable[ndx][3];
                                	                        byteData[k+1] = itable[ndx][2];
                                        	                byteData[k+2] = itable[ndx][1];
                                                	        byteData[k+3] = itable[ndx][0];
                                                 }
        	                                        if (color_length == 3) {
                	                                        byteData[k] = itable[ndx][2];
                        	                                byteData[k+1] = itable[ndx][1];
                                	                        byteData[k+2] = itable[ndx][0];
                                        	        }
                                                	if (color_length == 1) {
                                                        	byteData[k] = itable[ndx][0];
                                                 }
                                                 k += color_length;
                                         }
                                 }
                         } else if (scaled_Bytes != null && scaled_Bytes[0] != null && is_default_unit && rset != null && rset instanceof Linear1DSet) {
                                 // fast since FlatField with bytes, data Unit equals default
                                 // Unit and range set is Linear1DSet
                                 // get "scale and offset" for Linear1DSet
                                 if (first_time) {
                                         double first = ((Linear1DSet) rset).getFirst();
                                         double step = ((Linear1DSet) rset).getStep();
                                         // get scale and offset for ScalarMap
                                         double[] so = new double[2];
                                         double[] da = new double[2];
                                         double[] di = new double[2];
                                         cmap.getScale(so, da, di);
                                         double scale = so[0];
                                         double offset = so[1];
                                         // combine scales and offsets for Set, ScalarMap and color table
                                         float mult = (float) (table_scale * scale * step);
                                         float add = (float) (table_scale * (offset + scale * first));
 
                                         // build table for fast color lookup
                                         fast_table = new byte[256][];
                                         for (int j=0; j<256; j++) {
                                                 int index = j - 1;
                                                 if (index >= 0) { // not missing
                                                         int k = (int) (add + mult * index);
                                                         // clip to table
                                                         int ndx = k < 0 ? 0 : (k > tblEnd ? tblEnd : k);
                                                         fast_table[j] = itable[ndx];
                                                 }
                                         }
                                 }
                                 // now do fast lookup from byte values to color bytes
                                 byte[] bytes0 = scaled_Bytes[0];
 
                                 int k = 0;
 				int color_length_times_texture_width = texture_width*color_length;
                                 for (int y=0; y<tile_height; y++) {
                                         int image_col_factor = (y+yStart)*data_width + xStart;
 					k = y*color_length_times_texture_width;
                                         for (int x=0; x<tile_width; x++) {
                                                 int i = x + image_col_factor;
                                                 int ndx = ((int) bytes0[i]) - MISSING1;
                                                 if (color_length == 4) {
                                                         byteData[k] = itable[ndx][3];
                                                         byteData[k+1] = itable[ndx][2];
                                                         byteData[k+2] = itable[ndx][1];
                                                         byteData[k+3] = itable[ndx][0];
                                                 }
                                                 if (color_length == 3) {
                                                         byteData[k] = itable[ndx][2];
                                                         byteData[k+1] = itable[ndx][1];
                                                         byteData[k+2] = itable[ndx][0];
                                                 }
                                                 if (color_length == 1) {
                                                         byteData[k] = itable[ndx][0];
                                                 }
 						k+=color_length;
                                         }
                                 }
                         } else {
                                 // medium speed way to build texture colors
                                 if (first_time) {
                                         scaled_Bytes = null;
                                         scaled_Floats = ((Field) data).getFloats(false);
 					//GHANSHAM:30AUG2011 If rset can be used to create a lookup for range values, create them
 					if (rset instanceof Integer1DSet) {
 						rset_scalarmap_lookup = new float[1][rset.getLength()];
         					for (int i = 0; i < rset_scalarmap_lookup[0].length; i++) {
                 					rset_scalarmap_lookup[0][i] = i;
         					}
 						rset_scalarmap_lookup[0] = cmap.scaleValues(rset_scalarmap_lookup[0], false);
 					} else {
                                         	scaled_Floats[0] = cmap.scaleValues(scaled_Floats[0]);
 					}
                                 }
                                 // now do fast lookup from byte values to color bytes
                                 float[] values0 = scaled_Floats[0];
                                 int k = 0;
 				int color_length_times_texture_width = texture_width*color_length;
 				int image_col_offset = yStart*data_width + xStart;
 				int image_col_factor = 0;
                                 for (int y=0; y<tile_height; y++) {
 					image_col_factor = y*data_width+image_col_offset;
 					k = y*color_length_times_texture_width;
                                         for (int x=0; x<tile_width; x++) {
                                                 int i = x + image_col_factor;
                                                 if (!Float.isNaN(values0[i])) { // not missing
 							int j = 0;
 							//GHANSHAM:30AUG2011 Use the rset lookup to find scaled Range Values
 							if (null != rset_scalarmap_lookup && null != rset_scalarmap_lookup[0]) {
 								j = (int) (table_scale*rset_scalarmap_lookup[0][(int)values0[i]]);
 							} else {
 								j = (int) (table_scale*values0[i]);
 							}
                                 	                // clip to table
                         	                        int ndx = j < 0 ? 0 : (j > tblEnd ? tblEnd : j);
                 	                                if (color_length == 4) {
         	                                       		byteData[k] = itable[ndx][3];
 	                                                        byteData[k+1] = itable[ndx][2];
                                                                	byteData[k+2] = itable[ndx][1];
                                                                 byteData[k+3] = itable[ndx][0];
                                                 	}
                                         	        if (color_length == 3) {
                                 	                        byteData[k] = itable[ndx][2];
                         	                                byteData[k+1] = itable[ndx][1];
                 	                                        byteData[k+2] = itable[ndx][0];
         	                                        }
 	                                                if (color_length == 1) {
                                                              	byteData[k] = itable[ndx][0];
                                                         }
                                                 }
 						k+=color_length;
                                         }
                                 }
                         }
                 } else { // if (table == null)
                         // slower, more general way to build texture colors
                         if (first_time) {
                                 // call lookupValues which will use function since table == null
                                 scaled_Bytes = null;
                                 itable = null;
                                 scaled_Floats = ((Field) data).getFloats(false);
                                 scaled_Floats[0] = cmap.scaleValues(scaled_Floats[0]);
                                 color_values = control.lookupValues(scaled_Floats[0]);
                         }
 
                         // combine color RGB components into bytes
                         // int r, g, b, a = 255;
                         int r, g, b;
                         int c = (int) (255.0 * (1.0f - constant_alpha));
                         int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                         int k = 0;
 			int color_length_times_texture_width = texture_width*color_length;
 			int image_col_offset = yStart*data_width + xStart;
 			int image_col_factor = 0;
                         for (int y=0; y<tile_height; y++) {
 				image_col_factor = y*data_width+image_col_offset;
 				k = y*color_length_times_texture_width;
                                 for (int x=0; x<tile_width; x++) {
                                         int i = x + image_col_factor;
                                         if (!Float.isNaN(scaled_Floats[0][i])) { // not missing
                                                	c = (int) (255.0 * color_values[0][i]);
                                                 r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
        	                                        c = (int) (255.0 * color_values[1][i]);
                	                                g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                        	                        c = (int) (255.0 * color_values[2][i]);
                                	                b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                        	        if (color_length == 4) {
                                                	        c = (int) (255.0 * color_values[3][i]);
                                                        	a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                 }
        	                                        if (color_length == 4) {
                	                                        byteData[k] = (byte) a;
                        	                                byteData[k+1] = (byte) b;
                                	                        byteData[k+2] = (byte) g;
                                        	                byteData[k+3] = (byte) r;
                                                	}
                                                 if (color_length == 3) {
        	                                                byteData[k] = (byte) b;
                	                                        byteData[k+1] = (byte) g;
                        	                                byteData[k+2] = (byte) r;
                                	                }
                                        	        if (color_length == 1) {
                                                	        byteData[k] = (byte) b;
                                                	}	
                                         }
 					k+=color_length;
                                 }
                         }
                 }
         } else if (cmaps != null) {
 		Set rsets[] = null;
                 if (data instanceof ImageFlatField) {
                         if (first_time) {
                                 scaled_Bytes = ((FlatField) data).grabBytes();
                         }
                 }
 		//GHANSHAM:30AUG2011 Extract rsets from RGB FlatField
 		if (data instanceof FlatField) {
                         rsets = ((FlatField) data). getRangeSets();
 		}
 
 
                 boolean isRGBRGBRGB = ((cmaps[0].getDisplayScalar() == Display.RGB) && (cmaps[1].getDisplayScalar() == Display.RGB) && (cmaps[2].getDisplayScalar() == Display.RGB));
 
                 int r, g, b, c;
                 int tableEnd = 0;
                 if (first_time) {
                         if  (isRGBRGBRGB) { //Inserted by Ghansham (starts here)
                                 int map_indx;
                                 threeD_itable = new byte[cmaps.length][][];
                                 for (map_indx = 0; map_indx < cmaps.length; map_indx++) {
                                         BaseColorControl basecolorcontrol = (BaseColorControl)cmaps[map_indx].getControl();
                                         float color_table[][] = basecolorcontrol.getTable();
                                         threeD_itable[map_indx] = new byte[color_table[0].length][3];
                                         int table_indx;
                                         for(table_indx = 0; table_indx < threeD_itable[map_indx].length; table_indx++) {
                                                 c = (int) (255.0 * color_table[0][table_indx]);
                                                 r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                 c = (int) (255.0 * color_table[1][table_indx]);
                                                 g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                 c = (int) (255.0 * color_table[2][table_indx]);
                                                 b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                 threeD_itable[map_indx][table_indx][0] = (byte) r;
                                                 threeD_itable[map_indx][table_indx][1] = (byte) g;
                                                 threeD_itable[map_indx][table_indx][2] = (byte) b;
                                         }
                                 }
                         }
                 }
 
                 if (scaled_Bytes != null) {
                         // grab bytes directly from ImageFlatField
                         if (ImageFlatField.DEBUG) {
                                 System.err.println("ShadowImageFunctionTypeJ3D.doTransform: " + "cmaps != null: grab bytes directly");
                         }
                         //Inserted by Ghansham starts here
 			//IFF:Assume that FlatField is of type (element,line)->(R,G,B) with (Display.RGB,Display.RGB,Display.RGB) as mapping
                         if  (cmaps[0].getDisplayScalar() == Display.RGB && cmaps[1].getDisplayScalar() == Display.RGB && cmaps[2].getDisplayScalar() == Display.RGB) {
                                 int map_indx = 0;
                                 for (map_indx = 0; map_indx < cmaps.length; map_indx++) {
                                         int table_length = threeD_itable[0].length;
                                         int color_indx = permute[map_indx];
                                         if (first_time) {
                                                 scaled_Bytes[color_indx] = cmaps[color_indx].scaleValues(scaled_Bytes[color_indx], table_length);
                                         }
                                         int domainLength =  scaled_Bytes[color_indx].length;
                                         int tblEnd = table_length - 1;
                                         int data_indx = 0;
                                         int texture_index = 0;
 
 					int image_col_offset = yStart*data_width + xStart;
                                 	int image_col_factor = 0;
 					for (int y=0; y<tile_height; y++) { 
 						image_col_factor = y*data_width + image_col_offset;
                                                 for (int x=0; x<tile_width; x++) {
                                                         data_indx = x + image_col_factor;
                                                         texture_index = x + y*texture_width;
                                                         texture_index *= color_length;
                                                         int j = scaled_Bytes[color_indx][data_indx] & 0xff; // unsigned
                                                         // clip to table
                                                         int ndx = j < 0 ? 0 : (j > tblEnd ? tblEnd : j);
                                                         byteData[texture_index+(color_length-color_indx-1)]=threeD_itable[map_indx][ndx][map_indx]; //Check if this logic works well
                                                 }
                                         }
 
                                  }
                         } else { //Inserted by Ghansham (Ends here)
 				int data_indx = 0;
                                 int texture_index = 0;
 				int offset=0;
 				c = 0;
 				if (color_length == 4) {
 					c = (int) (255.0 * (1.0f - constant_alpha));
 				}
 				//IFF:with (Red,Green,Blue) or (Red,Green,Blue,Alpha) as mapping
 				int color_length_times_texture_width = color_length*texture_width;
 				int image_col_offset = yStart*data_width + xStart;
                                 int image_col_factor = 0;
                                 for (int y=0; y<tile_height; y++) {
 					image_col_factor = y*data_width + image_col_offset;
 					texture_index = y*color_length_times_texture_width;
                                         for (int x=0; x<tile_width; x++) {
                                                 data_indx = x + image_col_factor;
 						if (color_length == 4) {
 	                                                byteData[texture_index] =   (byte)c; //a
 	                                                byteData[texture_index+1] = scaled_Bytes[2][data_indx]; //b
         	                                        byteData[texture_index+2] = scaled_Bytes[1][data_indx]; //g
                 	                                byteData[texture_index+3] = scaled_Bytes[0][data_indx]; //r
 
 						} else {
 	                                                byteData[texture_index] = scaled_Bytes[2][data_indx]; //b
         	                                        byteData[texture_index+1] = scaled_Bytes[1][data_indx]; //g
                 	                                byteData[texture_index+2] = scaled_Bytes[0][data_indx]; //r
 						}
 						texture_index += color_length;
                                         }
                                 }
 
                         }
                 } else {
                         if (first_time) {
                                 float[][] values = ((Field) data).getFloats(false);
                                 scaled_Floats = new float[3][];
 				for (int i = 0; i < scaled_Floats.length; i++) {
 					//GHANSHAM:30AUG2011 Use the rset lookup to find scaled Range Values	
 					if (rsets != null) {
 						if (rsets[permute[i]] instanceof Integer1DSet) {
 							if (null == rset_scalarmap_lookup) {
 								rset_scalarmap_lookup = new float[3][];
 							}
 							rset_scalarmap_lookup[i] = new float[rsets[permute[i]].getLength()];
 							for (int j = 0; j < rset_scalarmap_lookup[i].length; j++) {
                                                         	rset_scalarmap_lookup[i][j] = j;
                                                 	}
                                                 	rset_scalarmap_lookup[i] = cmaps[permute[i]].scaleValues(rset_scalarmap_lookup[i], false);
 							scaled_Floats[i] = values[permute[i]];
 						} else {
 							scaled_Floats[i] = cmaps[permute[i]].scaleValues(values[permute[i]]);
 						}
 					} else {
 						scaled_Floats[i] = cmaps[permute[i]].scaleValues(values[permute[i]]);
 					}
 				}
                         }
                         c = (int) (255.0 * (1.0f - constant_alpha));
                         int a = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                         int m = 0;
 			//GHANSHAM:30AUG2011 Create tableLengths for each of the tables separately rather than single table_length. More safe
 			int RGB_tableEnd[] = null;;
                         if  (isRGBRGBRGB) {
 				RGB_tableEnd = new int[threeD_itable.length];
 				for (int indx = 0; indx < threeD_itable.length; indx++) {
 					RGB_tableEnd[indx]= threeD_itable[indx].length - 1;
 				}
                         }
                         int k = 0;
 			int color_length_times_texture_width = color_length*texture_width;
 			int image_col_offset = yStart*data_width + xStart;
                         int image_col_factor = 0;
                         for (int y=0; y<tile_height; y++) {
 				image_col_factor = y*data_width + image_col_offset;
 				k = y*color_length_times_texture_width;
                                 for (int x=0; x<tile_width; x++) {
                                         int i = x + image_col_factor;
                                         if (!Float.isNaN(scaled_Floats[0][i]) && !Float.isNaN(scaled_Floats[1][i]) && !Float.isNaN(scaled_Floats[2][i])) { // not missing
 						r=0;g=0;b=0;
                                                 if (isRGBRGBRGB) { //Inserted by Ghansham (start here)
 							int indx = -1;
 							//GHANSHAM:30AUG2011 Use the rset_scalarmap lookup to find scaled Range Values
 							if (rset_scalarmap_lookup != null && rset_scalarmap_lookup[0] != null) {
 								indx = (int)(RGB_tableEnd[0] * rset_scalarmap_lookup[0][(int)scaled_Floats[0][i]]);
 							} else{
 								indx = (int)(RGB_tableEnd[0] * scaled_Floats[0][i]);
 							}
 							indx = (indx < 0) ? 0 : ((indx > RGB_tableEnd[0]) ? RGB_tableEnd[0] : indx);
 							r = threeD_itable[0][indx][0];
 							//GHANSHAM:30AUG2011 Use the rset_scalarmap lookup to find scaled Range Values
 							if (rset_scalarmap_lookup != null && rset_scalarmap_lookup[1] != null) {
 								indx = (int)(RGB_tableEnd[1] * rset_scalarmap_lookup[1][(int)scaled_Floats[1][i]]);
                                                         } else{
 								indx = (int)(RGB_tableEnd[1] * scaled_Floats[1][i]);
 							}
                                                         indx = (indx < 0) ? 0 : ((indx > RGB_tableEnd[1]) ? RGB_tableEnd[1] : indx);
                                                         g = threeD_itable[1][indx][1];
 							//GHANSHAM:30AUG2011 Use the rset_scalarmap lookup to find scaled Range Values
 							if (rset_scalarmap_lookup != null && rset_scalarmap_lookup[2] != null) {
 								indx = (int)(RGB_tableEnd[2] * rset_scalarmap_lookup[2][(int)scaled_Floats[2][i]]);
                                                         } else {
 								indx = (int)(RGB_tableEnd[2] * scaled_Floats[2][i]);
 							}
 
                                                         indx = (indx < 0) ? 0 : ((indx > RGB_tableEnd[2]) ? RGB_tableEnd[2] : indx);
                                                         b = threeD_itable[2][indx][2];
                                                 } else { //Inserted by Ghansham (ends here)
 							//GHANSHAM:30AUG2011 Use the rset_scalarmap lookup to find scaled Range Values
 							if (rset_scalarmap_lookup != null && rset_scalarmap_lookup[0] != null) {
                                                                 c=(int) (255.0 * rset_scalarmap_lookup[0][(int)scaled_Floats[0][i]]);
 							} else {
 	                                                        c = (int) (255.0 * scaled_Floats[0][i]);
 							}
                                                         r = (c < 0) ? 0 : ((c > 255) ? 255 : c);
 							//GHANSHAM:30AUG2011 Use the rset_scalarmap lookup to find scaled Range Values
 							if (rset_scalarmap_lookup != null && rset_scalarmap_lookup[1] != null) {
                                                                 c=(int) (255.0 * rset_scalarmap_lookup[1][(int)scaled_Floats[1][i]]);
                                                         } else {
                                                         	c = (int) (255.0 * scaled_Floats[1][i]);
 							}
                                                         g = (c < 0) ? 0 : ((c > 255) ? 255 : c);
 							//GHANSHAM:30AUG2011 Use the rset_scalarmap lookup to find scaled Range Values
 							if (rset_scalarmap_lookup != null && rset_scalarmap_lookup[2] != null) {
                                                                 c=(int) (255.0 * rset_scalarmap_lookup[2][(int)scaled_Floats[2][i]]);
                                                         } else {
                                                         	c = (int) (255.0 * scaled_Floats[2][i]);
 							}
                                                         b = (c < 0) ? 0 : ((c > 255) ? 255 : c);
                                                 }
                                                 if (color_length == 4) {
                                                         byteData[k] = (byte) a;
                                                         byteData[k+1] = (byte) b;
                                                         byteData[k+2] = (byte) g;
                                                         byteData[k+3] = (byte) r;
                                                 } if (color_length == 3) {
                                                         byteData[k] = (byte) b;
                                                         byteData[k+1] = (byte) g;
                                                         byteData[k+2] = (byte) r;
                                                 }
                                                 if (color_length == 1) {
                                                         byteData[k] = (byte) b;
                                                 }
                                         }
 					k+=color_length;
                                 }
                         }
 			RGB_tableEnd = null;
                 }
         } else {
                 throw new BadMappingException("cmap == null and cmaps == null ??");
         }
 }
 
 
   public void buildCurvedTexture(Object group, Set domain_set, Unit[] dataUnits, Unit[] domain_units,
                                  float[] default_values, ShadowRealType[] DomainComponents,
                                  int valueArrayLength, int[] inherited_values, int[] valueToScalar,
                                  GraphicsModeControl mode, float constant_alpha, float[] value_array, 
                                  float[] constant_color, DisplayImpl display,
                                  int curved_size, ShadowRealTupleType Domain, CoordinateSystem dataCoordinateSystem,
                                  DataRenderer renderer, ShadowFunctionOrSetType adaptedShadowType,
                                  int[] start, int lenX, int lenY, float[][] samples, int bigX, int bigY,
                                  VisADImageTile tile)
          throws VisADException, DisplayException {
     float[] coordinates = null;
     float[] texCoords = null;
     int data_width = 0;
     int data_height = 0;
     int texture_width = 1;
     int texture_height = 1;
 
     int[] lengths = null;
                                                                                                                      
     // get domain_set sizes
     if (domain_set != null) {
       lengths = ((GriddedSet) domain_set).getLengths();
     }
     else {
       lengths = new int[] {lenX, lenY};
     }
 
     data_width = lengths[0];
     data_height = lengths[1];
 
     // texture sizes must be powers of two on older graphics cards.
     texture_width = textureWidth(data_width);
     texture_height = textureHeight(data_height);
                                                                                                                    
     // transform for any CoordinateSystem in data (Field) Domain
     ShadowRealTupleType domain_reference = Domain.getReference();
     ShadowRealType[] DC = DomainComponents;
 
     if (domain_reference != null &&
         domain_reference.getMappedDisplayScalar()) {
       RealTupleType ref = (RealTupleType) domain_reference.getType();
       renderer.setEarthSpatialData(Domain, domain_reference, ref,
                   ref.getDefaultUnits(), (RealTupleType) Domain.getType(),
                   new CoordinateSystem[] {dataCoordinateSystem},
                   domain_units);
 
       // ShadowRealTypes of DomainReference
       DC = adaptedShadowType.getDomainReferenceComponents();
     }
     else {
       RealTupleType ref = (domain_reference == null) ? null :
                           (RealTupleType) domain_reference.getType();
       Unit[] ref_units = (ref == null) ? null : ref.getDefaultUnits();
       renderer.setEarthSpatialData(Domain, domain_reference, ref,
                   ref_units, (RealTupleType) Domain.getType(),
                   new CoordinateSystem[] {dataCoordinateSystem},
                   domain_units);
     }
                                                                                                                    
     int[] tuple_index = new int[3];
     int[] spatial_value_indices = {-1, -1, -1};
     ScalarMap[] spatial_maps = new ScalarMap[3];
                                                                                                                    
     DisplayTupleType spatial_tuple = null;
     for (int i=0; i<DC.length; i++) {
       Enumeration maps =
         DC[i].getSelectedMapVector().elements();
       ScalarMap map = (ScalarMap) maps.nextElement();
       DisplayRealType real = map.getDisplayScalar();
       spatial_tuple = real.getTuple();
       if (spatial_tuple == null) {
         throw new DisplayException("texture with bad tuple: " +
                                    "ShadowImageFunctionTypeJ3D.doTransform");
       }
       // get spatial index
       tuple_index[i] = real.getTupleIndex();
       spatial_value_indices[tuple_index[i]] = map.getValueIndex();
       spatial_maps[tuple_index[i]] = map;
       if (maps.hasMoreElements()) {
         throw new DisplayException("texture with multiple spatial: " +
                                    "ShadowImageFunctionTypeJ3D.doTransform");
       }
     } // end for (int i=0; i<DC.length; i++)
 
 
     // get spatial index not mapped from domain_set
     tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
     DisplayRealType real =
       (DisplayRealType) spatial_tuple.getComponent(tuple_index[2]);
     int value2_index = display.getDisplayScalarIndex(real);
     float value2 = default_values[value2_index];
     for (int i=0; i<valueArrayLength; i++) {
       if (inherited_values[i] > 0 &&
           real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
         value2 = value_array[i];
         break;
       }
     }
                                                                                                                    
     boolean useLinearTexture = false;
     double[] scale = null;
     double[] offset = null;
     CoordinateSystem coord = null;
 
     if (spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
 // inside 'if (anyFlow) {}' in ShadowType.assembleSpatial()
 	renderer.setEarthSpatialDisplay(null, spatial_tuple, display,
                spatial_value_indices, default_values, null);
     } else {
       	coord = spatial_tuple.getCoordinateSystem();
 
       	if (coord instanceof CachingCoordinateSystem) {
         	coord = ((CachingCoordinateSystem)coord).getCachedCoordinateSystem();
       	}
 
       	if (coord instanceof InverseLinearScaledCS) {
         	InverseLinearScaledCS invCS = (InverseLinearScaledCS)coord;
         	useLinearTexture = (invCS.getInvertedCoordinateSystem()).equals(dataCoordinateSystem);
         	scale = invCS.getScale();
         	offset = invCS.getOffset();
       	}
 
 // inside 'if (anyFlow) {}' in ShadowType.assembleSpatial()
       	renderer.setEarthSpatialDisplay(coord, spatial_tuple, display,
                spatial_value_indices, default_values, null);
     }
     if (useLinearTexture) {
     	float scaleX = (float) scale[0];
       	float scaleY = (float) scale[1];
       	float offsetX = (float) offset[0];
       	float offsetY = (float) offset[1];
 
       	float[][] xyCoords = null;
       	if (domain_set != null) {
         	xyCoords = getBounds(domain_set, data_width, data_height, scaleX, offsetX, scaleY, offsetY);
       	} else {
                 //If there is tiling in linear texture domain set is coming null if number of tiles is greater than 1
                 //Code inserted by Ghansham (starts here)
                 int indx0 = (start[0]) + (start[1])*bigX;
                 int indx1 = (start[0]) + (start[1] + lenY-1)*bigX;
                 int indx2 = (start[0] + lenX -1) + (start[1] + lenY - 1)*bigX;
                 int indx3 = (start[0] + lenX -1 ) + (start[1])*bigX;
 
                 float x0 = samples[0][indx0];
                 float y0 = samples[1][indx0];
                 float x1 = samples[0][indx1];
                 float y1 = samples[1][indx1];
                 float x2 = samples[0][indx2];
                 float y2 = samples[1][indx2];
                 float x3 = samples[0][indx3];
                 float y3 = samples[1][indx3];
 
                 xyCoords = new float[2][4];
                 xyCoords[0][0] = (x0 - offsetX)/scaleX;
                 xyCoords[1][0] = (y0 - offsetY)/scaleY;
 
                 xyCoords[0][1] = (x1 - offsetX)/scaleX;
                 xyCoords[1][1] = (y1 - offsetY)/scaleY;
 
                 xyCoords[0][2] = (x2 - offsetX)/scaleX;
                 xyCoords[1][2] = (y2 - offsetY)/scaleY;
 
                 xyCoords[0][3] = (x3 - offsetX)/scaleX;
                 xyCoords[1][3] = (y3 - offsetY)/scaleY;
                 //Code inserted by Ghansham (Ends here)
       	}
 
 
       // create VisADQuadArray that texture is mapped onto
       coordinates = new float[12];
       // corner 0 (-1,1)
       coordinates[tuple_index[0]] = xyCoords[0][0];
       coordinates[tuple_index[1]] = xyCoords[1][0];
       coordinates[tuple_index[2]] = value2;
       // corner 1 (-1,-1)
       coordinates[3+tuple_index[0]] = xyCoords[0][1];
       coordinates[3+tuple_index[1]] = xyCoords[1][1];
       coordinates[3 + tuple_index[2]] = value2;
       // corner 2 (1, -1)
       coordinates[6+tuple_index[0]] = xyCoords[0][2];
       coordinates[6+tuple_index[1]] = xyCoords[1][2];
       coordinates[6 + tuple_index[2]] = value2;
       // corner 3 (1,1)
       coordinates[9+tuple_index[0]] = xyCoords[0][3];
       coordinates[9+tuple_index[1]] = xyCoords[1][3];
       coordinates[9 + tuple_index[2]] = value2;
 
       // move image back in Java3D 2-D mode
       adjustZ(coordinates);
 
       texCoords = new float[8];
       float ratiow = ((float) data_width) / ((float) texture_width);
       float ratioh = ((float) data_height) / ((float) texture_height);
 
       boolean yUp = true;
       setTexCoords(texCoords, ratiow, ratioh, yUp);
 
       VisADQuadArray qarray = new VisADQuadArray();
       qarray.vertexCount = 4;
       qarray.coordinates = coordinates;
       qarray.texCoords = texCoords;
 
       /*REUSE GEOM/COLORBYTES:I have replaced reuse with reuseImages.
       And here in the else logic I have added a few more lines. 
         The else part of this never got executed because reuse was always false.
         Now else part gets executed when reuse is true and either regen_geom or regen_colbytes is true.
         It just applies geometry to the already available texture.
         When both are true then if part gets executed. 
     */
       	//if (!reuse) 
       	if (!reuseImages || (regen_colbytes && regen_geom)) {	//REUSE GEOM/COLORBYTES: Earlier reuse variable was used. Replaced it with reuseImages and regeom_colbytes and regen_geom
         	BufferedImage image = tile.getImage(0);
          	textureToGroup(group, qarray, image, mode, constant_alpha,
                         constant_color, texture_width, texture_height, true, true, tile);
       	} else {	//REUSE GEOM/COLORBYTES: reuse the colorbytes just apply the geometry
 		int num_children = ((BranchGroup) group).numChildren();
         	if (num_children > 0) {
                 	BranchGroup branch1 = (BranchGroup) ((BranchGroup) group).getChild(0); //This the branch group created by textureToGroup Function
                 	Shape3D shape = (Shape3D) branch1.getChild(0);
                 	shape.setGeometry(((DisplayImplJ3D) display).makeGeometry(qarray));
         	}
  
         	if (animControl == null) {
           		imgNode.setCurrent(0);
         	}
       	}
 
     }
     else {
       // compute size of triangle array to map texture onto
       int size = (data_width + data_height) / 2;
       curved_size = Math.max(1, Math.min(curved_size, size / 32));
       int nwidth = 2 + (data_width - 1) / curved_size;
       int nheight = 2 + (data_height - 1) / curved_size;
 
       // compute locations of triangle vertices in texture
       int nn = nwidth * nheight;
       int[] is = new int[nwidth];
       int[] js = new int[nheight];
 
       for (int i=0; i<nwidth; i++) {
         is[i] = Math.min(i * curved_size, data_width - 1);
       }
       for (int j=0; j<nheight; j++) {
         js[j] = Math.min(j * curved_size, data_height - 1);
       }
 	
 
       // get spatial coordinates at triangle vertices
       int k = 0;
       float[][] spline_domain = null;
       if (domain_set == null) {
 	//Ghansham: We generate the indices for the samples directly from 'is' and 'js' array
 	spline_domain = new float[2][nn];
 	int kk = 0;
 	int ndx = 0;
 	int col_factor = 0;
 	for (int j = 0; j < nheight; j++) {
             col_factor = (start[1] + js[j]) * bigX;
             for (int i = 0; i < nwidth; i++) {
                 ndx = (start[0] + is[i]) + col_factor;
 		spline_domain[0][kk] = samples[0][ndx];
 		spline_domain[1][kk] = samples[1][ndx];
                 kk++;
             }
         }
       }
       else {
 	int[] indices = new int[nn]; //Ghansham:Calculate indices only if there is a single tile in the full image
       	k=0;
 	int col_factor;
       	for (int j=0; j<nheight; j++) {
         	col_factor = data_width * js[j];
         	for (int i=0; i<nwidth; i++) {
           		indices[k] = is[i] + col_factor;
           		k++;
         	} 
       	}
         spline_domain = domain_set.indexToValue(indices);
 	indices = null;
       }
 
       spline_domain = Unit.convertTuple(spline_domain, dataUnits, domain_units, false);
 
 
        if (domain_reference != null
              && domain_reference.getMappedDisplayScalar()) {
            RealTupleType ref = (RealTupleType) domain_reference.getType();
 
             spline_domain =
                    CoordinateSystem.transformCoordinates(
                    ref, null, ref.getDefaultUnits(), null,
                    (RealTupleType) Domain.getType(), dataCoordinateSystem,
                    domain_units, null, spline_domain);
        }
 
        float[][] spatial_values = new float[3][];
        spatial_values[tuple_index[0]] = spline_domain[0];
        spatial_values[tuple_index[1]] = spline_domain[1];
        for (int i = 0; i < 3; i++) {                
           if (spatial_maps[i] != null) {
              spatial_values[i] = spatial_maps[i].scaleValues(spatial_values[i], false);
           }
        }
 
        if (!spatial_tuple.equals(Display.DisplaySpatialCartesianTuple)) {
           spatial_values = coord.toReference(spatial_values);
        }
 
       boolean isSpherical = spatial_tuple.equals(Display.DisplaySpatialSphericalTuple);
       boolean spatial_all_select = true;
 
        if (isSpherical) {
             for (int i=0; i<nn; i++) {
                if (Float.isNaN(spatial_values[0][i]) || Float.isNaN(spatial_values[1][i]) || Float.isNaN(spatial_values[2][i])) {
                       spatial_all_select = false;
                       break;
                }
             }
        } else {
             if (Float.isNaN(value2)) {
                  spatial_all_select = false;
             } else {
                  for (int i=0; i<nn; i++) {
                       if (Float.isNaN(spatial_values[0][i]) || Float.isNaN(spatial_values[1][i])) {
                           spatial_all_select = false;
                           break;
                       }
                  }
            }
        } 
 
                                                                                                                    
     VisADTriangleStripArray tarray = new VisADTriangleStripArray();
     tarray.stripVertexCounts = new int[nheight - 1];
     java.util.Arrays.fill(tarray.stripVertexCounts, 2 * nwidth);
 
     int len = (nheight - 1) * (2 * nwidth);
     tarray.vertexCount = len;
     tarray.coordinates = new float[3 * len];
     tarray.texCoords = new float[2 * len];
                                                                                                                    
     int m = 0;
     k = 0;
     int kt = 0;
 
     	float y_coord = 0f; 
 	float y_coord2 = 0f;
 	float  x_coord = 0f;
     for (int j=0; j<nheight-1; j++) {
 	if (0 ==j){
 		y_coord = (0.5f + js[j])/texture_height;
 	} else {
 		y_coord = y_coord2;
 	}
 	y_coord2 = (0.5f + js[j+1])/texture_height;
       for (int i=0; i<nwidth; i++) {
 	
 		tarray.coordinates[k++] = spatial_values[0][m];
 		tarray.coordinates[k++] = spatial_values[1][m];
 		tarray.coordinates[k++] =  value2;
 		tarray.coordinates[k++] = spatial_values[0][m+nwidth];
 		tarray.coordinates[k++] = spatial_values[1][m+nwidth];
 		tarray.coordinates[k++] = value2;
 
 
 		x_coord = (0.5f + is[i])/texture_width;
         	tarray.texCoords[kt++] = x_coord;
         	tarray.texCoords[kt++] = y_coord;
         	tarray.texCoords[kt++] = x_coord;
         	tarray.texCoords[kt++] = y_coord2;
 	
                                                                                                                    
 	m += 1;
       }
     }
 	is = null;
 	js = null;
 	spatial_values[0] = null;
 	spatial_values[1] = null;
 	spatial_values[2] = null;
 	spatial_values = null;
 	spline_domain[0] = null;
 	spline_domain[1] = null;
 	spline_domain = null;
     // do surgery to remove any missing spatial coordinates in texture
     if (!spatial_all_select) {
       tarray = (VisADTriangleStripArray) tarray.removeMissing();
     }
 
     // do surgery along any longitude split (e.g., date line) in texture
     if (adaptedShadowType.getAdjustProjectionSeam()) {
       tarray = (VisADTriangleStripArray) tarray.adjustLongitude(renderer);
       tarray = (VisADTriangleStripArray) tarray.adjustSeam(renderer);
     }
     
     /*REUSE GEOM/COLORBYTES:I have replaced reuse with reuseImages.
       	And here in the else logic I have added a few more lines. 
         The else part of this never got executed because reuse was always false.
         Now else part gets executed when reuseImages is true or either regen_geom or regen_colbytes is true.
         It just applies geometry to the already available texture.
         When both regen_geom or regen_colbytes are true then if part gets executed. 
     */                                                                                                               
     // add texture as sub-node of group in scene graph
     	//if (!reuse) 
     	if (!reuseImages || (regen_colbytes && regen_geom)) { //REUSE GEOM/COLORBYTES: Earlier reuse variable was used. Replaced it with reuseImages and regeom_colbytes and regen_geom
        		BufferedImage image = tile.getImage(0);
        		textureToGroup(group, tarray, image, mode, constant_alpha, constant_color, texture_width, texture_height, true, true, tile);
     	} else { //REUSE GEOM/COLORBYTES: Reuse the colorbytes and just apply the geometry
 		int num_children = ((BranchGroup) group).numChildren();
 		if (num_children > 0) {
         		BranchGroup branch1 = (BranchGroup) ((BranchGroup) group).getChild(0); //This is the branch group created by textureToGroup Function
                 	Shape3D shape = (Shape3D) branch1.getChild(0);
                 	shape.setGeometry(((DisplayImplJ3D) display).makeGeometry(tarray));
 			
         	} 
       		if (animControl == null) {
         		imgNode.setCurrent(0);
       		}
     	}
 
    }
 	tuple_index = null;
 // System.out.println("end curved texture " + (System.currentTimeMillis() - link.start_time));
   }
 
   public void buildLinearTexture(Object group, Set domain_set, Unit[] dataUnits, Unit[] domain_units,
                                  float[] default_values, ShadowRealType[] DomainComponents,
                                  int valueArrayLength, int[] inherited_values, int[] valueToScalar,
                                  GraphicsModeControl mode, float constant_alpha,
                                  float[] value_array, float[] constant_color, DisplayImpl display,
                                  VisADImageTile tile)
          throws VisADException, DisplayException {
 
     float[] coordinates = null;
     float[] texCoords = null;
     float[] normals = null;
     int data_width = 0;
     int data_height = 0;
     int texture_width = 1;
     int texture_height = 1;
 
     Linear1DSet X = null;
     Linear1DSet Y = null;
     if (domain_set instanceof Linear2DSet) {
       X = ((Linear2DSet) domain_set).getX();
       Y = ((Linear2DSet) domain_set).getY();
     }
     else {
       X = ((LinearNDSet) domain_set).getLinear1DComponent(0);
       Y = ((LinearNDSet) domain_set).getLinear1DComponent(1);
     }
     float[][] limits = new float[2][2];
     limits[0][0] = (float) X.getFirst();
     limits[0][1] = (float) X.getLast();
     limits[1][0] = (float) Y.getFirst();
     limits[1][1] = (float) Y.getLast();
                                                                                                                        
     // get domain_set sizes
     data_width = X.getLength();
     data_height = Y.getLength();
     // texture sizes must be powers of two on older graphics cards
     texture_width = textureWidth(data_width);
     texture_height = textureHeight(data_height);
                                                                                                                        
                                                                                                                        
     // WLH 27 Jan 2003
     float half_width = 0.5f / ((float) (data_width - 1));
     float half_height = 0.5f / ((float) (data_height - 1));
     half_width = (limits[0][1] - limits[0][0]) * half_width;
     half_height = (limits[1][1] - limits[1][0]) * half_height;
     limits[0][0] -= half_width;
     limits[0][1] += half_width;
     limits[1][0] -= half_height;
     limits[1][1] += half_height;
                                                                                                                        
                                                                                                                        
     // convert values to default units (used in display)
     limits = Unit.convertTuple(limits, dataUnits, domain_units);
 
     int[] tuple_index = new int[3];
     if (DomainComponents.length != 2) {
       throw new DisplayException("texture domain dimension != 2:" +
                                  "ShadowFunctionOrSetType.doTransform");
     }
                                                                                                                        
     // find the spatial ScalarMaps
     for (int i=0; i<DomainComponents.length; i++) {
       Enumeration maps = DomainComponents[i].getSelectedMapVector().elements();
       ScalarMap map = (ScalarMap) maps.nextElement();
       // scale values
       limits[i] = map.scaleValues(limits[i]);
       DisplayRealType real = map.getDisplayScalar();
       DisplayTupleType tuple = real.getTuple();
       if (tuple == null ||
           !tuple.equals(Display.DisplaySpatialCartesianTuple)) {
         throw new DisplayException("texture with bad tuple: " +
                                    "ShadowFunctionOrSetType.doTransform");
       }
       // get spatial index
       tuple_index[i] = real.getTupleIndex();
       if (maps.hasMoreElements()) {
         throw new DisplayException("texture with multiple spatial: " +
                                    "ShadowFunctionOrSetType.doTransform");
       }
     } // end for (int i=0; i<DomainComponents.length; i++)
 
 
     // get spatial index not mapped from domain_set
     tuple_index[2] = 3 - (tuple_index[0] + tuple_index[1]);
     DisplayRealType real = (DisplayRealType)
       Display.DisplaySpatialCartesianTuple.getComponent(tuple_index[2]);
     int value2_index = display.getDisplayScalarIndex(real);
     float value2 = default_values[value2_index];
     for (int i=0; i<valueArrayLength; i++) {
       if (inherited_values[i] > 0 &&
           real.equals(display.getDisplayScalar(valueToScalar[i])) ) {
         // value for unmapped spatial dimension
         value2 = value_array[i];
         break;
       }
     }
 
     // create VisADQuadArray that texture is mapped onto
     coordinates = new float[12];
     // corner 0
     coordinates[tuple_index[0]] = limits[0][0];
     coordinates[tuple_index[1]] = limits[1][0];
     coordinates[tuple_index[2]] = value2;
     // corner 1
     coordinates[3 + tuple_index[0]] = limits[0][0];
     coordinates[3 + tuple_index[1]] = limits[1][1];
     coordinates[3 + tuple_index[2]] = value2;
     // corner 2
     coordinates[6 + tuple_index[0]] = limits[0][1];
     coordinates[6 + tuple_index[1]] = limits[1][1];
     coordinates[6 + tuple_index[2]] = value2;
     // corner 3
     coordinates[9 + tuple_index[0]] = limits[0][1];
     coordinates[9 + tuple_index[1]] = limits[1][0];
     coordinates[9 + tuple_index[2]] = value2;
                                                                                                                        
     // move image back in Java3D 2-D mode
     adjustZ(coordinates);
                                                                                                                        
     texCoords = new float[8];
     float ratiow = ((float) data_width) / ((float) texture_width);
     float ratioh = ((float) data_height) / ((float) texture_height);
 
     boolean yUp = true;
     setTexCoords(texCoords, ratiow, ratioh, yUp);
                                                                                                                        
     VisADQuadArray qarray = new VisADQuadArray();
     qarray.vertexCount = 4;
     qarray.coordinates = coordinates;
     qarray.texCoords = texCoords;
  
     /*REUSE GEOM/COLORBYTES:I have replaced reuse with reuseImages.
       And here in the else logic I have added a few more lines. 
 	The else part of this never got executed because reuse was always false.
 	Now else part gets executed when reuse is true and either regen_geom or regen_colbytes is true.
         It just applies geometry to the already available texture.
 	When both are true then if part gets executed. 
     */                                                                                                                      
     // add texture as sub-node of group in scene graph
     if (!reuseImages|| (regen_colbytes && regen_geom)) { //REUSE GEOM/COLORBYTES: Earlier reuse variable was used. Replaced it with reuseImages and regeom_colbytes and regen_geom
       BufferedImage image = tile.getImage(0);
       textureToGroup(group, qarray, image, mode, constant_alpha,
                      constant_color, texture_width, texture_height, true, true, tile);
     }
     else {
 	int num_children = ((BranchGroup) group).numChildren();
         if (num_children > 0) { //REUSE GEOM/COLORBYTES: Reuse the colorbytes and apply geometry
                 BranchGroup branch1 = (BranchGroup) ((BranchGroup) group).getChild(0); //This the branch group created by textureToGroup Function
                 Shape3D shape = (Shape3D) branch1.getChild(0);
                 shape.setGeometry(((DisplayImplJ3D) display).makeGeometry(qarray));
         }
       if (animControl == null) {
         imgNode.setCurrent(0);
       }
     }
 
   }
 
   //public CachedBufferedByteImage createImageByRef(final int texture_width, final int texture_height, final int imageType) {
   public BufferedImage createImageByRef(final int texture_width, final int texture_height, final int imageType) {
       return new BufferedImage(texture_width, texture_height, imageType);
   }
 
   public static float[][] getBounds(Set domain_set, float data_width, float data_height,
            float scaleX, float offsetX, float scaleY, float offsetY)
       throws VisADException
   {
     float[][] xyCoords = new float[2][4];
 
     float[][] coords0 = ((GriddedSet)domain_set).gridToValue(new float[][] {{0f},{0f}});
     float[][] coords1 = ((GriddedSet)domain_set).gridToValue(new float[][] {{0f},{(float)(data_height-1)}});
     float[][] coords2 = ((GriddedSet)domain_set).gridToValue(new float[][] {{(data_width-1f)},{(data_height-1f)}});
     float[][] coords3 = ((GriddedSet)domain_set).gridToValue(new float[][] {{(data_width-1f)},{0f}});
 
     float x0 = coords0[0][0];
     float y0 = coords0[1][0];
     float x1 = coords1[0][0];
     float y1 = coords1[1][0];
     float x2 = coords2[0][0];
     float y2 = coords2[1][0];
     float x3 = coords3[0][0];
     float y3 = coords3[1][0];
 
     xyCoords[0][0] = (x0 - offsetX)/scaleX;
     xyCoords[1][0] = (y0 - offsetY)/scaleY;
 
     xyCoords[0][1] = (x1 - offsetX)/scaleX;
     xyCoords[1][1] = (y1 - offsetY)/scaleY;
 
     xyCoords[0][2] = (x2 - offsetX)/scaleX;
     xyCoords[1][2] = (y2 - offsetY)/scaleY;
 
     xyCoords[0][3] = (x3 - offsetX)/scaleX;
     xyCoords[1][3] = (y3 - offsetY)/scaleY;
 
     return xyCoords;
   }
 
 }
 
 
 class SwitchNotify extends Switch {
   VisADImageNode imgNode;
   int numChildren;
   Switch swit;
 
   SwitchNotify(VisADImageNode imgNode, int numChildren) {
     super();
     this.imgNode = imgNode;
     this.numChildren = numChildren;
     this.swit = imgNode.getSwitch();
   }
 
   public int numChildren() {
     return numChildren;
   }
 
   public void setWhichChild(int index) {
     if (index == Switch.CHILD_NONE) {
       swit.setWhichChild(Switch.CHILD_NONE);
     }
     else if (index >= 0) {
       if ( swit.getWhichChild() == Switch.CHILD_NONE) {
         swit.setWhichChild(0);
       }
       imgNode.setCurrent(index);
     }
   }
 }
 
 
 class Mosaic {
 
   Tile[][] tiles;
   ArrayList<Tile>  tileList = new ArrayList<Tile>();
 
   int n_x_sub = 1;
   int n_y_sub = 1;
 
   Mosaic(int lenY, int limitY, int lenX, int limitX) {
 
     int y_sub_len = limitY;
     n_y_sub = lenY/y_sub_len;
     if (n_y_sub == 0) n_y_sub++;
     if ((lenY - n_y_sub*y_sub_len) > 4) n_y_sub += 1;
 
     int[][] y_start_stop = new int[n_y_sub][2];
     for (int k = 0; k < n_y_sub-1; k++) {
        y_start_stop[k][0] = k*y_sub_len - k;
        y_start_stop[k][1] = ((k+1)*y_sub_len - 1) - k;
        // check that we don't exceed limit
        if ( ((y_start_stop[k][1]-y_start_stop[k][0])+1) > limitY) {
          y_start_stop[k][1] -= 1; //too big, take away gap fill
        }
     }
     int k = n_y_sub-1;
     y_start_stop[k][0] = k*y_sub_len - k;
     y_start_stop[k][1] = lenY - 1 - k;
 
     int x_sub_len = limitX;
     n_x_sub = lenX/x_sub_len;
     if (n_x_sub == 0) n_x_sub++;
     if ((lenX - n_x_sub*x_sub_len) > 4) n_x_sub += 1;
 
     int[][] x_start_stop = new int[n_x_sub][2];
     for (k = 0; k < n_x_sub-1; k++) {
       x_start_stop[k][0] = k*x_sub_len - k;
       x_start_stop[k][1] = ((k+1)*x_sub_len - 1) - k;
       // check that we don't exceed limit
       if ( ((x_start_stop[k][1]-x_start_stop[k][0])+1) > limitX) {
         x_start_stop[k][1] -= 1; //too big, take away gap fill
       }
     }
     k = n_x_sub-1; 
     x_start_stop[k][0] = k*x_sub_len - k;
     x_start_stop[k][1] = lenX - 1 - k;
 
     tiles = new Tile[n_y_sub][n_x_sub];
 
     for (int j=0; j<n_y_sub; j++) {
       for (int i=0; i<n_x_sub; i++) {
          tiles[j][i] =
            new Tile(y_start_stop[j][0], y_start_stop[j][1], x_start_stop[i][0], x_start_stop[i][1]);
          tileList.add(tiles[j][i]);
       }
     }
   }
 
   Iterator iterator() {
     return tileList.iterator();
   }
 }
 
 class Tile {
    int y_start;
    int x_start;
    int y_stop;
    int x_stop;
  
    int height;
    int width;
 
    Tile(int y_start, int y_stop, int x_start, int x_stop) {
      this.y_start = y_start;
      this.y_stop = y_stop;
      this.x_start = x_start;
      this.x_stop = x_stop;
      
      height = y_stop - y_start + 1;
      width = x_stop - x_start + 1;
    }
 }
