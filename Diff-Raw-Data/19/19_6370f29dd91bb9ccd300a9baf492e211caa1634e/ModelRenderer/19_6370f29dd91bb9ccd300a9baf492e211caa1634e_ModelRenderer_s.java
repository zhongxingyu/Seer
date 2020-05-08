 package model;
 
 import java.nio.IntBuffer;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.glu.GLU;
 
 import org.jdesktop.animation.timing.interpolation.PropertySetter;
 
 import util.DCCamera;
 import util.DCUtil;
 import util.GraphicUtil;
 import TimingFrameExt.FloatEval;
 
 import com.jogamp.opengl.util.GLBuffers;
 
 import datastore.CacheManager;
 import datastore.HierarchyTable;
 import datastore.MM;
 import datastore.SSM;
 import datastore.SchemeManager;
 import gui.DCScrollPane;
 import gui.DCTip;
 import gui.GTag;
 import gui.StatusWindow;
 
 /////////////////////////////////////////////////////////////////////////////////
 // This class is responsible for rendering the 3D part of the visualizaiton
 /////////////////////////////////////////////////////////////////////////////////
 public class ModelRenderer extends BaseModelRenderer {
    
    /* To keep compiler happy */
    private static final long serialVersionUID = 1L;
    
    public static GLU glu = new GLU();
    
    public float vpadding = 30.0f;
    public float rpadding = 30.0f;
    public float lpadding = 30.0f;
    public float spadding = 10.0f;
    
    
   
    ////////////////////////////////////////////////////////////////////////////////
    // Default constructor
    ////////////////////////////////////////////////////////////////////////////////
    public ModelRenderer(){
       super();
    }     
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Rules
    //
    // 1) If object A is picked and A is currently selected, then de-select A
    // 2) If object A is picked and B is currently selected, then de-select A and select B
    // 3) If object A is picked and nothing is currently selected, then select A
    ////////////////////////////////////////////////////////////////////////////////
    public void picking(GL2 gl2) {
       // Quickie way to get out and save unnecessary rendering 
       if (SSM.instance().l_mouseClicked == false) return;
       
       // Force trigger depth peel re-render on mouse press action
       SSM.instance().refreshOITTexture = true;
       
       
       
       float mx = SSM.instance().mouseX;
       float my = SSM.instance().windowHeight - SSM.instance().mouseY;
       
       
       // Check the UI elements first
       
       // Handling vehicle manufacture
       this.pickingScrollPane(mx, my, manufactureScroll, SSM.instance().manufactureAttrib, 
             makeScroll, SSM.instance().makeAttrib,     // level 1
             modelScroll, SSM.instance().modelAttrib,   // level 2
             yearScroll,  SSM.instance().yearAttrib     // level 3
       );
       this.scrollPaneTransition(mx, my, manufactureScroll, SSM.instance().manufactureAttrib);
       
       
       // Handling vehicle make
       this.pickingScrollPane(mx, my, makeScroll, SSM.instance().makeAttrib, 
             modelScroll, SSM.instance().modelAttrib,   // level 2
             yearScroll, SSM.instance().yearAttrib      // level 3
       );
       this.scrollPaneTransition(mx, my, makeScroll, SSM.instance().makeAttrib);
       
       
       // Handling vehicle model
       this.pickingScrollPane(mx, my, modelScroll, SSM.instance().modelAttrib,
             yearScroll, SSM.instance().yearAttrib      // level 3
       );
       this.scrollPaneTransition(mx, my, modelScroll, SSM.instance().modelAttrib);
       
       
       // Handling vehicle year
       this.pickingScrollPane(mx, my, yearScroll, SSM.instance().yearAttrib);
       this.scrollPaneTransition(mx, my, yearScroll, SSM.instance().yearAttrib);
       
       
       
       // Check 3D first, then 2D
       Integer obj = null;
       obj = picking3D(gl2);
       if (obj == null) {
          for (int i=0; i < SSM.instance().lensList.size(); i++) {
             obj = picking2DBalanced(gl2, SSM.instance().lensList.elementAt(i));
             if (obj != null) break;
          }
       }
       
       
       // We have hit something
       // This can be a select or a de-select
       if (obj != null) {
          
          
          // Disable any action if in local focus mode and 
          // the part clicked is not related nor selected
          if (SSM.instance().useLocalFocus == true) {
             if (SSM.instance().selectedGroup.size() > 0 &&  !SSM.instance().relatedList.contains(obj)) 
                return;
          }
          
          
         
         
          if (SSM.instance().selectedGroup.size() > 0 ) {
             //if ( SSM.instance().selectedGroup.intValue() == obj.intValue()) {
             if (SSM.instance().selectedGroup.contains(obj)) {
                SSM.instance().selectedGroup.remove(obj);
             } else {
                SSM.instance().selectedGroup.put(obj, obj);
             }
             SSM.instance().dirty = 1;
             SSM.instance().dirtyGL = 1; // for the text panel
             SSM.instance().t1Start = 0;
             SSM.instance().t2Start = SSM.instance().globalFetchSize;
             SSM.instance().yoffset = SSM.instance().docHeight;
             SSM.instance().docMaxSize = 0;
             for (Integer key : SSM.instance().selectedGroup.keySet()) {
                SSM.instance().docMaxSize += CacheManager.instance().groupOccurrence.get( key );
             }
             //SSM.instance().docMaxSize = CacheManager.instance().groupOccurrence.get( SSM.instance().selectedGroup );
             
          } else {
             //M.instance().selectedGroup = obj;   
             SSM.instance().selectedGroup.put(obj,obj);
             SSM.instance().dirty = 1;
             SSM.instance().dirtyGL = 1; // for the text panel
             SSM.instance().t1Start = 0;
             SSM.instance().t2Start = SSM.instance().globalFetchSize;
             SSM.instance().yoffset = SSM.instance().docHeight;
             //SSM.instance().docMaxSize = CacheManager.instance().groupOccurrence.get( SSM.instance().selectedGroup );
             SSM.instance().docMaxSize = 0;
             for (Integer key : SSM.instance().selectedGroup.keySet()) {
                SSM.instance().docMaxSize += CacheManager.instance().groupOccurrence.get( key );
             }
         }
       }
       
       /*
       picking3D(gl2);   
 //      if (SSM.instance().selectedGroup == null) {
          for (int i=0; i < SSM.instance().lensList.size(); i++) {
 //            picking2D(gl2, SSM.instance().lensList.elementAt(i));
             picking2DBalanced(gl2, SSM.instance().lensList.elementAt(i));
 //            if (SSM.instance().selectedGroup != null) break;
          }
 //      }
       
       // Signal the process to refresh the data at next iteration
       if (SSM.instance().selectedGroup != null) {
          SSM.instance().dirty = 1;
       }
       */
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Picking with more or less balanced labels
    ////////////////////////////////////////////////////////////////////////////////
    public Integer picking2DBalanced(GL2 gl2, LensAttrib la) {
       //if (SSM.instance().l_mouseClicked == false) return;
       IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
       this.startPickingOrtho(gl2, buffer);      
       
       String list[] = this.getComponentSortedByProjY(gl2);    
       float rightHeight = 0;
       float leftHeight = 0;
       
       
       
       Vector<DCComponent> rightList = new Vector<DCComponent>();
       Vector<DCComponent> leftList  = new Vector<DCComponent>();
       
       float lensRadius = la.magicLensRadius;
       float lensX = la.magicLensX;
       float lensY = la.magicLensY;
       
       
       
       // New padding, always apply outside of the 3D model, in addition 
       // position with respect to the radius size ie: do not go "insde" the circumference
       rpadding = Math.abs( MM.currentModel.maxx - lensX);
       lpadding = Math.abs( MM.currentModel.minx - lensX);
       if (rpadding > lensRadius) {
          rpadding -= lensRadius;
       } else {
          rpadding = vpadding;
       }
       if (lpadding > lensRadius) {
          lpadding -= lensRadius;
       } else { 
          lpadding = vpadding; 
       }
       
       // check if the paddings are out of bound (ie: when we are close up)
       // default the padding space to space padding
       if ( rpadding + lensX > SSM.instance().windowWidth ) {
          rpadding = spadding;   
       }
       if (lensX - lpadding < 0) {
          lpadding = spadding;   
       }      
       
       Hashtable<String, String> tmp = new Hashtable<String, String>();
       
       // First filter into left and right list
       for (int i=0; i < list.length; i++) {
          DCComponent comp = MM.currentModel.componentTable.get(list[i]);
          // If the centroid point is before the lens's near plane, do not render the label
          if (DCCamera.instance().eye.sub(comp.center).mag() <= la.nearPlane) continue;
          
          // If the object has 0 occurrences, do not render the label
          if (comp.hasContext == false) continue;         
          
          // If local mode than don't render components that are not related
          comp.cchart.active = true;
          if (SSM.instance().useLocalFocus == true) {
             if (SSM.instance().selectedGroup.size() > 0 && ! SSM.instance().relatedList.contains(comp.id)) {
                comp.cchart.active = false;   
             } else {
                comp.cchart.active = true;   
             }
          }
          
          
          
          // Check parent and model table capability in aggregation mode
          boolean skip = false;
          if (SSM.instance().useAggregate == true) {
             Integer parentId = comp.id;
             while(true) {
                parentId = HierarchyTable.instance().getParentId(parentId);   
                
                if (parentId == null) break;
                if (MM.currentModel.componentTableById.get(parentId) != null) {
                   skip = true;
                   break;
                }
             } // end while            
          } else {
             skip = false; 
          }
          if (skip == true) continue;
          
          
          float xx = comp.projCenter.x - lensX;
          float yy = comp.projCenter.y - (SSM.instance().windowHeight - lensY);
          float c = (float)Math.sqrt(xx*xx + yy*yy);
          
          if ( c <= lensRadius ) {
             // If the base object is already registered, skip the rendering
             // so we don't draw so many guide lines
             if (tmp.contains(comp.baseName)) continue; 
             tmp.put(comp.baseName, comp.baseName);
 
             this.alternateSideLayout(comp, rightList, leftList, i);
             //this.dualSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
             //this.singleSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
             
          }
       } // end for
       
       // Now actually render the labels
       rightHeight = Math.min((SSM.instance().windowHeight-la.magicLensY) + (rightList.size()/2)*(SSM.instance().sparkLineHeight + vpadding),
                               SSM.instance().windowHeight-SSM.instance().sparkLineHeight-vpadding);
       leftHeight  = Math.min((SSM.instance().windowHeight-la.magicLensY) + (leftList.size()/2)*(SSM.instance().sparkLineHeight + vpadding),
                               SSM.instance().windowHeight-SSM.instance().sparkLineHeight-vpadding); 
       
       
       // Right side
       for (int i=0; i < rightList.size(); i++) {
          DCComponent comp = rightList.elementAt(i);
          if (comp.id < 0) continue;
             
          String txt = comp.cname+"(" + comp.selectedTotal + "/" + CacheManager.instance().groupOccurrence.get(comp.id) + ")";
             
          // double size[] = FontRenderer.instance().getDimension(txt);      
          double size[] = GraphicUtil.getFontDim(txt);
          rightHeight -= Math.max(size[1], comp.cchart.height);
          rightHeight -= vpadding;
                
          comp.cchart.setAnchor(lensX+lensRadius+rpadding+spadding, rightHeight);
          gl2.glLoadName(comp.cchart.id);
          gl2.glPushMatrix();
             comp.cchart.renderBorder(gl2);
          gl2.glPopMatrix();
       }
       
       
       // Left Side
       for (int i=0; i < leftList.size(); i++) {
          DCComponent comp = leftList.elementAt(i);
          if (comp.id < 0) continue;
             
          String txt = comp.cname+"(" + comp.selectedTotal + "/" + CacheManager.instance().groupOccurrence.get(comp.id) + ")";
             
          //double size[] = FontRenderer.instance().getDimension(txt);      
          double size[] = GraphicUtil.getFontDim(txt);
          leftHeight -= Math.max(size[1], comp.cchart.height);
          leftHeight -= vpadding;
         
          //gl2.glTranslated(lensX-lensRadius-lpadding-comp.sparkLine.width, leftHeight, 0);
          comp.cchart.setAnchor(lensX-lensRadius-lpadding-comp.cchart.width-spadding, leftHeight);
          gl2.glLoadName(comp.cchart.id);
          gl2.glPushMatrix();
             comp.cchart.renderBorder(gl2);
          gl2.glPopMatrix();
       }      
       
       return finishPicking(gl2, buffer);
       
    }
    
    
    
    /////////////////////////////////////////////////////////////////////////////////       
    // Performs picking operation
    // Set state to select mode, then redraw the group components
    // Ported from NeHe site : http://nehe.gamedev.net
    /////////////////////////////////////////////////////////////////////////////////       
    public Integer picking3D(GL2 gl2) {
       // Quick way to get out and save some FPS from rendering useless cycles
       //if (SSM.instance().l_mouseClicked == false) return null;
       
       int hits;
       IntBuffer buffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, 512);
       this.startPickingPerspective(gl2, buffer);
       
       Enumeration<String> e = MM.currentModel.componentTable.keys();
       while (e.hasMoreElements()) {   
          //String partName = e.nextElement(); 
          String partName = e.nextElement(); 
          String baseName = MM.currentModel.componentTable.get(partName).baseName;
          int partId = MM.currentModel.componentTable.get(partName).id;
          //Integer partId  = HierarchyTable.instance().getGroupId(baseName).size() > 0 ? HierarchyTable.instance().getGroupId(baseName).elementAt(0) : null;
          //if (partId == null) continue;
          if (partId < 0) continue;
          
          gl2.glLoadName(partId);
          gl2.glPushMatrix();
             if (MM.currentModel.componentTable.get(partName).level >= SSM.instance().occlusionLevel) 
                 MM.currentModel.componentTable.get(partName).renderBasicMesh(gl2);
             //model.componentTable.get(partName).boundingBox.renderBoundingBox(gl2);  
          gl2.glPopMatrix();
                      
       }
       return finishPicking(gl2, buffer);
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Layout that places on either the left or the right side
    //    right and left are passed in containers
    ////////////////////////////////////////////////////////////////////////////////
    public void singleSideLayout(DCComponent comp, LensAttrib la, Vector<DCComponent> right, Vector<DCComponent> left, float paddings[]) {
       float lensRadius = la.magicLensRadius;
       float lensX = la.magicLensX;
       float lensY = la.magicLensY;
       float rpadding = paddings[0];
       float lpadding = paddings[1];
       
       if (rpadding < lpadding) {
          right.add( comp );
       } else {
          left.add( comp );
       }      
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Layout that will place the chart on either side of the center of the lens
    // with respect to how far their projected coordinate is from the center
    //    right and left are passed in containers
    ////////////////////////////////////////////////////////////////////////////////
    public void dualSideLayout(DCComponent comp, LensAttrib la, Vector<DCComponent> right, Vector<DCComponent> left, float paddings[]) {
       float lensRadius = la.magicLensRadius;
       float lensX = la.magicLensX;
       float lensY = la.magicLensY;
       float rpadding = paddings[0];
       float lpadding = paddings[1];
       
       if (comp.projCenter.x >= lensX) {
          if (rpadding < 1.5*lensRadius)
             right.add( comp );
          else 
             left.add( comp );
       } else {
          if (lpadding < 1.5*lensRadius)
             left.add( comp );
          else
             right.add( comp );
       }      
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////// 
    // Alternate between right and left
    ///////////////////////////////////////////////////////////////////////////////// 
    public void alternateSideLayout(DCComponent comp, Vector<DCComponent> right, Vector<DCComponent> left, int index) {
       if (index % 2 == 0) {
          right.add(comp);
       } else {
          left.add(comp); 
       }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Render the small chart/labels 
    ////////////////////////////////////////////////////////////////////////////////
    public void renderLabelBalanced(GL2 gl2, LensAttrib la) {
       String list[] = this.getComponentSortedByProjY(gl2);    
       float rightHeight = 0;
       float leftHeight = 0;
       
       int startIdx = CacheManager.instance().getDateKey( SSM.instance().startTimeFrame ) == null ? 0:
          CacheManager.instance().getDateKey( SSM.instance().startTimeFrame );
       int endIdx   = CacheManager.instance().getDateKey( SSM.instance().endTimeFrame) == null ? CacheManager.instance().occurrenceTable.size():
          CacheManager.instance().getDateKey( SSM.instance().endTimeFrame );      
       
      
       Vector<DCComponent> rightList = new Vector<DCComponent>();
       Vector<DCComponent> leftList  = new Vector<DCComponent>();
       
       float lensRadius = la.magicLensRadius;
       float lensX = la.magicLensX;
       float lensY = la.magicLensY;
       
       float mx = SSM.instance().mouseX;
       float my = SSM.instance().windowHeight - SSM.instance().mouseY;
       
       // New padding, always apply outside of the 3D model, in addition 
       // position with respect to the radius size ie: do not go "inside" the circumference
       rpadding = Math.abs( MM.currentModel.maxx - lensX);
       lpadding = Math.abs( MM.currentModel.minx - lensX);
       if (rpadding > lensRadius) {
          rpadding -= lensRadius;
       } else  {
          rpadding = vpadding;
       }
       if (lpadding > lensRadius) {
          lpadding -= lensRadius;
       } else  {
          lpadding = vpadding;
       }
       
       // check if the paddings are out of bound (ie: when we are close up)
       // default the padding space to space padding
       if ( rpadding + lensX > SSM.instance().windowWidth ) {
          rpadding = spadding;   
       }
       if (lensX - lpadding < 0) {
          lpadding = spadding;   
       }
       
       
       Hashtable<String, String> tmp = new Hashtable<String, String>();
       
       // First filter into left and right list
       for (int i=0; i < list.length; i++) {
          DCComponent comp = MM.currentModel.componentTable.get(list[i]);
          // If the centroid point is before the lens's near plane, do not render the label
          if (DCCamera.instance().eye.sub(comp.center).mag() <= la.nearPlane) continue;
          
          // If the object has 0 occurrences, do not render the label
          if (comp.hasContext == false) continue;
          
          
          // If local mode than don't render components that are not related
          comp.cchart.active = true;
          if (SSM.instance().useLocalFocus == true) {
             if (SSM.instance().selectedGroup.size() > 0 && ! SSM.instance().relatedList.contains(comp.id))  {
                comp.cchart.active = false;
             } else {
                comp.cchart.active = true;   
             }
          } 
             
          
          
          // Check parent and model table capability in aggregation mode
          boolean skip = false;
          if (SSM.instance().useAggregate == true) {
             Integer parentId = comp.id;
             while(true) {
                parentId = HierarchyTable.instance().getParentId(parentId);   
                
                if (parentId == null) break;
                if (MM.currentModel.componentTableById.get(parentId) != null) {
                   skip = true;
                   break;
                }
             } // end while            
          } else {
             skip = false; 
          }
          if (skip == true) continue;
              
          
          
          float xx = comp.projCenter.x - lensX;
          float yy = comp.projCenter.y - (SSM.instance().windowHeight - lensY);
          float c = (float)Math.sqrt(xx*xx + yy*yy);
          
          if ( c <= lensRadius ) {
             // If the base object is already registered, skip the rendering
             // so we don't draw so many guide lines
             if (tmp.contains(comp.baseName)) continue; 
             tmp.put(comp.baseName, comp.baseName);
                
             this.alternateSideLayout(comp, rightList, leftList, i);
             //this.dualSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
             //this.singleSideLayout(comp, la, rightList, leftList, new float[]{rpadding, lpadding});
             
          }
       } // end for
       
       
       
       
       // Now actually render the labels
       /*
       rightHeight = (SSM.instance().windowHeight-la.magicLensY) + (rightList.size()/2)*(SSM.instance().sparkLineHeight + vpadding);
       leftHeight  = (SSM.instance().windowHeight-la.magicLensY) + (leftList.size()/2)*(SSM.instance().sparkLineHeight + vpadding);
       */
       rightHeight = Math.min((SSM.instance().windowHeight-la.magicLensY) + (rightList.size()/2)*(SSM.instance().sparkLineHeight + vpadding),
                               SSM.instance().windowHeight-SSM.instance().sparkLineHeight-vpadding);
       leftHeight  = Math.min((SSM.instance().windowHeight-la.magicLensY) + (leftList.size()/2)*(SSM.instance().sparkLineHeight + vpadding),
                               SSM.instance().windowHeight-SSM.instance().sparkLineHeight-vpadding); 
       
       
       gl2.glEnable(GL2.GL_BLEND);
       
       // Right side
       for (int i=0; i < rightList.size(); i++) {
          DCComponent comp = rightList.elementAt(i);
          if (comp.id < 0) continue;
          
          
             
          //String txt = comp.cname+"(" + comp.selectedTotal + "/" + groupOccurrence.get(id) + ")";
          int occ = CacheManager.instance().groupOccurrence.get(comp.id); 
          int relatedOcc = 0;
          int relatedOccNew = 0;
          if (SSM.instance().selectedGroup.size() > 0 ) {
             
             if (SSM.instance().useAggregate == true) {
                Vector<Integer> selectedGroup =  new Vector<Integer>();
                selectedGroup.addAll( SSM.instance().selectedGroup.values());
             
 //               relatedOcc = CacheManager.instance().getRelatedOccAgg(startIdx, endIdx, SSM.instance().startMonth, SSM.instance().endMonth, comp.id, SSM.instance().selectedGroup, 
 //                  SSM.instance().selectedManufacture, 
 //                  SSM.instance().selectedMake,
 //                  SSM.instance().selectedModel);
                
                relatedOccNew = CacheManager.instance().getCoOccurringAgg(
                      startIdx, endIdx, 
                      SSM.instance().startMonth, SSM.instance().endMonth, 
                      HierarchyTable.instance().getAgg(comp.id),
                      selectedGroup,
                      //SSM.instance().selectedManufacture, 
                      SSM.instance().manufactureAttrib.selected,
                      SSM.instance().makeAttrib.selected, 
                      SSM.instance().modelAttrib.selected,
                      SSM.instance().yearAttrib.selected);
 //System.out.println(">>>> " + comp.id + " " + related + " " + HierarchyTable.instance().getAgg(comp.id));               
             } else {
                Vector<Integer> related =  new Vector<Integer>();
                related.addAll( SSM.instance().selectedGroup.values());
                
                Vector<Integer> t = new Vector<Integer>();
                t.add(comp.id);
 //               relatedOcc = CacheManager.instance().getRelatedOcc(startIdx, endIdx, SSM.instance().startMonth, SSM.instance().endMonth, comp.id, SSM.instance().selectedGroup, 
 //                  SSM.instance().selectedManufacture, 
 //                  SSM.instance().selectedMake,
 //                  SSM.instance().selectedModel);
                
                relatedOccNew = CacheManager.instance().getCoOccurring(
                      startIdx, endIdx, 
                      SSM.instance().startMonth, SSM.instance().endMonth, 
                      t,
                      related,
                      //SSM.instance().selectedManufacture, 
                      SSM.instance().manufactureAttrib.selected,
                      SSM.instance().makeAttrib.selected, 
                      SSM.instance().modelAttrib.selected,
                      SSM.instance().yearAttrib.selected);              
                
             }
          }
          String txt = comp.baseName+"(" + relatedOccNew + "/" + relatedOcc + "/" + occ + ")";
             
          double size[] = GraphicUtil.getFontDim(txt);
          rightHeight -= Math.max(size[1], comp.cchart.height);
          rightHeight -= vpadding;
                
                
          //FontRenderer.instance().setColour(SchemeManager.font_default);
          //comp.sparkLine.label = txt;
          comp.cchart.setAnchor(lensX+lensRadius+rpadding+spadding, rightHeight);
          comp.cchart.colour = comp.colour;
          comp.cchart.render(gl2);
          
          comp.cchart.tf.width = comp.cchart.width;
          comp.cchart.tf.height = comp.cchart.height;
          comp.cchart.tf.anchorX = comp.cchart.anchorX;
          comp.cchart.tf.anchorY = comp.cchart.anchorY;
          
          comp.cchart.setLabel(txt);
          comp.cchart.tf.render(gl2);
          comp.cchart.tf.renderBorder(gl2);
          
          
          // doodle
          float doodleY = comp.projCenter.y - (SSM.instance().windowHeight - lensY);
          float doodleAngle = (float)Math.asin( doodleY/lensRadius);
          
          float edgeX = 1.0f*lensRadius*(float)Math.cos(doodleAngle);
          
          if (SSM.instance().selectedGroup.size() > 0  && SSM.instance().selectedGroup.contains(comp.cchart.id)){
             gl2.glColor4fv( SchemeManager.colour_blue.toArray(), 0);
          } else if (SSM.instance().relatedList != null && SSM.instance().relatedList.contains(comp.id)) { 
             gl2.glColor4fv( SchemeManager.colour_related.toArray(), 0);
          } else {
             gl2.glColor4fv( SchemeManager.sparkline_guideline.toArray(), 0);
          }
          
          gl2.glLineWidth(2.0f);
          gl2.glBegin(GL2.GL_LINES);
             gl2.glVertex2d( comp.projCenter.x, comp.projCenter.y);
             gl2.glVertex2d( lensX + edgeX, comp.projCenter.y);
             
             gl2.glVertex2d( lensX + edgeX, comp.projCenter.y);
             gl2.glVertex2d(lensX+lensRadius + rpadding, rightHeight + 0.5*SSM.instance().sparkLineHeight);
             
             gl2.glVertex2d(lensX+lensRadius + rpadding, rightHeight + 0.5*SSM.instance().sparkLineHeight);
             gl2.glVertex2d(lensX+lensRadius + rpadding+spadding, rightHeight + 0.5*SSM.instance().sparkLineHeight);
          gl2.glEnd();
          gl2.glLineWidth(1.0f);
          
       }
       
       
       // Left Side
       for (int i=0; i < leftList.size(); i++) {
          DCComponent comp = leftList.elementAt(i);
          if (comp.id < 0) continue;
             
          //String txt = comp.cname+"(" + comp.selectedTotal + "/" + groupOccurrence.get(id) + ")";
          int occ = CacheManager.instance().groupOccurrence.get(comp.id); 
          int relatedOcc = 0;
          int relatedOccNew = 0;
          if (SSM.instance().selectedGroup.size() > 0 ) {
             
             if (SSM.instance().useAggregate == true) {
                Vector<Integer> selectedGroup =  new Vector<Integer>();
                selectedGroup.addAll( SSM.instance().selectedGroup.values());
                
 //               relatedOcc = CacheManager.instance().getRelatedOccAgg(startIdx, endIdx, SSM.instance().startMonth, SSM.instance().endMonth, comp.id, SSM.instance().selectedGroup, 
 //                   SSM.instance().selectedManufacture, 
 //                   SSM.instance().selectedMake,
 //                   SSM.instance().selectedModel);
                
                relatedOccNew = CacheManager.instance().getCoOccurringAgg(
                      startIdx, endIdx, 
                      SSM.instance().startMonth, SSM.instance().endMonth, 
                      HierarchyTable.instance().getAgg(comp.id),
                      selectedGroup,
                      //SSM.instance().selectedManufacture, 
                      SSM.instance().manufactureAttrib.selected,
                      SSM.instance().makeAttrib.selected, 
                      SSM.instance().modelAttrib.selected,
                      SSM.instance().yearAttrib.selected);               
             } else {
                Vector<Integer> related =  new Vector<Integer>();
                related.addAll(SSM.instance().selectedGroup.keySet());
                Vector<Integer> t = new Vector<Integer>();
                t.add(comp.id);
                
 //               relatedOcc = CacheManager.instance().getRelatedOcc(startIdx, endIdx, SSM.instance().startMonth, SSM.instance().endMonth, comp.id, SSM.instance().selectedGroup, 
 //                   SSM.instance().selectedManufacture, 
 //                   SSM.instance().selectedMake,
 //                   SSM.instance().selectedModel);
                
                relatedOccNew = CacheManager.instance().getCoOccurring(
                      startIdx, endIdx, 
                      SSM.instance().startMonth, SSM.instance().endMonth, 
                      t,
                      related,
                      SSM.instance().manufactureAttrib.selected,
                      //SSM.instance().selectedManufacture, 
                      SSM.instance().makeAttrib.selected, 
                      SSM.instance().modelAttrib.selected,
                      SSM.instance().yearAttrib.selected);                   
             }
          }
          String txt = comp.baseName+"(" + relatedOccNew + "/" + relatedOcc + "/" + occ + ")";
             
          double size[] = GraphicUtil.getFontDim(txt);
          leftHeight -= Math.max(size[1], comp.cchart.height);
          leftHeight -= vpadding;
         
          comp.cchart.setAnchor( lensX-lensRadius-lpadding-comp.cchart.width-spadding, leftHeight);
          comp.cchart.colour = comp.colour;
          comp.cchart.render(gl2);
          
          comp.cchart.tf.width = comp.cchart.width;
          comp.cchart.tf.height = comp.cchart.height;
          comp.cchart.tf.anchorX = comp.cchart.anchorX;
          comp.cchart.tf.anchorY = comp.cchart.anchorY;         
          comp.cchart.setLabel(txt);
          comp.cchart.tf.render(gl2);
          comp.cchart.tf.renderBorder(gl2);
          
             
          // Doodle
          float doodleY = comp.projCenter.y - (SSM.instance().windowHeight - lensY);
          float doodleAngle = (float)Math.asin( doodleY/lensRadius);
          float edgeX = 1.0f*lensRadius*(float)Math.cos(doodleAngle);
          
          //if (SSM.instance().selectedGroup != null && SSM.instance().selectedGroup == comp.cchart.id){
          if (SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(comp.cchart.id)){
             gl2.glColor4fv( SchemeManager.colour_blue.toArray(), 0);
          } else if (SSM.instance().relatedList != null && SSM.instance().relatedList.contains(comp.id)) {   
             gl2.glColor4fv( SchemeManager.colour_related.toArray(), 0);
          } else {
             gl2.glColor4fv( SchemeManager.sparkline_guideline.toArray(), 0);
          }         
          
          
          gl2.glLineWidth(2.0f);
          gl2.glBegin(GL2.GL_LINES);
             gl2.glVertex2d( comp.projCenter.x, comp.projCenter.y);
             gl2.glVertex2d( lensX - edgeX, comp.projCenter.y);
             
             gl2.glVertex2d( lensX - edgeX, comp.projCenter.y);
             gl2.glVertex2d( lensX-lensRadius - lpadding, leftHeight + 0.5*SSM.instance().sparkLineHeight);
             
             gl2.glVertex2d( lensX-lensRadius - lpadding, leftHeight + 0.5*SSM.instance().sparkLineHeight);
             gl2.glVertex2d( lensX-lensRadius - lpadding-spadding, leftHeight + 0.5*SSM.instance().sparkLineHeight);
             
          gl2.glEnd();
          gl2.glLineWidth(1.0f);
          
       }
    }
    
    
 
    ////////////////////////////////////////////////////////////////////////////////
    // Render labels/sparklines in a circular pattern around the
    // magic lens
    ////////////////////////////////////////////////////////////////////////////////
    public void renderLabelCircular(GL2 gl2, LensAttrib la) {
       String list[] = this.getComponentUnsorted(gl2);
       for (int i=0; i < list.length; i++) {
          DCComponent comp = MM.currentModel.componentTable.get(list[i]);
          
          float xx = comp.projCenter.x - la.magicLensX;
          float yy = comp.projCenter.y - (SSM.instance().windowHeight - la.magicLensY);
          //float xx = comp.projCenter.x - SSM.instance().mouseX;
          //float yy = comp.projCenter.y - (SSM.instance().windowHeight - SSM.instance().mouseY);
          float c = (float)Math.sqrt(xx*xx + yy*yy);
          
          double angle = 0.0f;
          angle = Math.atan2( yy, xx+0.000001);
          //angle = xx/yy;
          
          if ( c <= la.magicLensRadius ) {
          	/*
             Vector<Integer> ids = HierarchyTable.instance().getGroupId(comp.baseName);
             if (ids == null || ids.size() == 0) continue;
             Integer id = ids.elementAt(0);
             */
          	if (comp.id < 0) continue;
             
             float scale = 1.0f + (float)CacheManager.instance().groupOccurrence.get(comp.id)/(float)SSM.instance().maxOccurrence;
             textRenderer.setColor( 0.0f, 0.5f, 2.0f, 1.0f);
             this.renderTextPolar(gl2, la, (float)angle, scale, comp, comp.cname+"(" + CacheManager.instance().groupOccurrence.get(comp.id) + ")");
          }
       }
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Main rendering method
    ////////////////////////////////////////////////////////////////////////////////
    public void render(GL2 gl2) {
       gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
       
       // To avoid threading issues, lets
       // just put the update GL stuff here
       if (SSM.instance().dirtyGL == 1) {
          resetDataGL(gl2);
          SSM.instance().dirtyGL = 0;
       }
       
       ////////////////////////////////////////////////////////////////////////////////
       // Need to re-adjust the buffer size if the screen size is changed
       ////////////////////////////////////////////////////////////////////////////////
       if (SSM.instance().refreshOITBuffers == true) {
          this.DeleteDualPeelingRenderTargets(gl2);
          this.InitDualPeelingRenderTargets(gl2);
          SSM.instance().refreshOITTexture = true;
          SSM.instance().refreshOITBuffers = false;   
       }
       
       
       ////////////////////////////////////////////////////////////////////////////////
       // Initialize the glowTexture
       ////////////////////////////////////////////////////////////////////////////////
       if (SSM.instance().refreshGlowTexture == true) {
         System.out.println("In Modelrenderer init " + SSM.instance().windowHeight + " " + SSM.instance().windowWidth);         
         glowTexture= new FrameBufferTexture();
         glowTexture.TEXTURE_SIZE_W = SSM.instance().windowWidth;
         glowTexture.TEXTURE_SIZE_H = SSM.instance().windowHeight;
         glowTexture.init(gl2);
         
         // Redo shader inits
         glowTexture.shader.createShader(gl2, "src\\Shader\\vert_fbt.glsl", GL2.GL_VERTEX_SHADER);
         glowTexture.shader.createShader(gl2, "src\\Shader\\frag_fbt_white.glsl", GL2.GL_FRAGMENT_SHADER);
         glowTexture.shader.createProgram(gl2);
         
         gl2.glBindAttribLocation(glowTexture.shader.programID,  0, "in_position");
         gl2.glBindAttribLocation(glowTexture.shader.programID,  1, "in_colour");
         gl2.glBindAttribLocation(glowTexture.shader.programID,  2, "in_texcoord");      
         
         glowTexture.shader.linkProgram(gl2);
         glowTexture.shader.bindFragColour(gl2, "outColour");   
         
         SSM.instance().refreshGlowTexture = false;
         System.out.println("In Modelrenderer init " + glowTexture.TEXTURE_SIZE_W + " " + glowTexture.TEXTURE_SIZE_H);         
      }      
       
     
       
 
       ////////////////////////////////////////////////////////////////////////////////
       // Render any scenes that we want to cache...ie: Lens, Filters
       ////////////////////////////////////////////////////////////////////////////////
       if (SSM.instance().refreshMagicLens == true ||
          MM.instance().currentModel.isAnimationRunning()) {
          for (int i=0;i < SSM.instance().lensList.size(); i++) {
             LensAttrib la = SSM.instance().lensList.elementAt(i);   
             if (la.mlen == null) {
                la.mlen = new MagicLens();   
                la.mlen.init(gl2);
             }
             
             la.mlen.startRecording(gl2); {
                // Do not render again in dual depthing peeling mode, 
                // We already have transparency and it is too expensive
                if ( ! SSM.instance().useDualDepthPeeling) {
                   setPerspectiveView(gl2, la.nearPlane, la.farPlane); {
                      gl2.glEnable(GL2.GL_BLEND);
                      renderColourRamp(gl2, la);
                   }
                   setPerspectiveView(gl2, 0.01f, la.nearPlane); {
                      renderSil(gl2);   
                   }
                }
             } la.mlen.stopRecording(gl2);
             
          }
          SSM.instance().refreshMagicLens = false;
       }
      
       
       ////////////////////////////////////////////////////////////////////////////////
       // Render the default scene we want to show
       ////////////////////////////////////////////////////////////////////////////////
       setPerspectiveView(gl2); {
          gl2.glEnable(GL2.GL_BLEND);
          
          if (SSM.instance().useDualDepthPeeling) {
             if (SSM.instance().refreshOITTexture) {
                this.RenderDualPeeling(gl2);
                //SSM.instance().refreshOITTexture = false;
             } else {
                this.RenderOITTexture(gl2);   
             }
          } else {
             renderColourRamp(gl2, null);
          }
          //renderNormal(gl2);
          //nderFNormal(gl2);
          //renderVNormal(gl2);
          //renderSil(gl2);
          //renderSilLineWidth(gl2);
          //renderBoundingBox(gl2);
          setProjectedCoord(gl2);
          float coord[] = MM.currentModel.getMaxMinScreenX(gl2);
          //System.out.println(coord[0] + " " + coord[1]);
          
          //MM.instance().currentModel.setProjectedCoord(gl2);
          //System.out.println(MM.instance().currentModel.projbox);
       }
       
       
       ////////////////////////////////////////////////////////////////////////////////
       // Record glow effects and render to to a 1-1 square in ortho mode 
       ////////////////////////////////////////////////////////////////////////////////
       if (SSM.instance().useGlow && SSM.instance().selectedGroup.size() > 0) {
          glowTexture.startRecording(gl2); 
             setPerspectiveView(gl2); 
             gl2.glPushMatrix();
             for (DCComponent comp : MM.currentModel.componentTable.values()) {
                //if (SSM.instance().relatedList.contains(comp.id) && ! SSM.instance().selectedGroup.contains(comp.id)) {
                if ( SSM.instance().selectedGroup.contains(comp.id) ) {
                   //gl2.glScaled(1.2, 1.2, 1.2);
                   comp.renderBuffer(gl2, DCColour.fromInt(0, 10, 210));
                   //comp.renderBufferAdj(gl2, DCColour.fromInt(20, 10, 210));
                   //gl2.glScaled(1.0/1.2, 1.0/1.2, 1.0/1.2);
                }
                   //comp.renderFNormal(gl2);
                   //comp.renderBuffer(gl2, DCColour.fromInt(0, 10, 210));
                   //comp.renderBufferAdj(gl2, DCColour.fromInt(200, 200, 0));
             }          
          glowTexture.stopRecording(gl2);
          
          GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
          gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
          glowTexture.render(gl2);
       }         
       
       
       ////////////////////////////////////////////////////////////////////////////////
       // Render any filters we want to show
       ////////////////////////////////////////////////////////////////////////////////
       for (int i=0; i < SSM.instance().lensList.size(); i++) {
          LensAttrib la = SSM.instance().lensList.elementAt(i);
          setOrthonormalView(gl2); {
             if (la.mlen != null)
               la.mlen.renderLens( gl2, la );
          }
       }
       
       
       
       
       
       ////////////////////////////////////////////////////////////////////////////////
       // Render any 2D components
       ////////////////////////////////////////////////////////////////////////////////
       if (SSM.instance().showLabels == true) {
          for (int i=0; i < SSM.instance().lensList.size(); i++) {
             setOrthonormalView(gl2, 0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight); {
                if (SSM.instance().useCircularLabel == true) {
                   renderLabelCircular(gl2, SSM.instance().lensList.elementAt(i));
                } else {
                   renderLabelBalanced(gl2, SSM.instance().lensList.elementAt(i));
                }
             }
          }
       }
       
       
       // TODO : Move this out later...just to test if animation can work, probably have a flag and put in resetDataGL or soemthing
       if (SSM.instance().resizePanel == 1) {
          if (dcTextPanel.animatorH != null) dcTextPanel.animatorH.stop();
          if (dcTextPanel.animatorW != null) dcTextPanel.animatorW.stop();
          
          float goalH = SSM.instance().docActive ? SSM.instance().docHeight : 0.0f;
          float goalW = SSM.instance().docActive ? SSM.instance().docWidth : 0.0f;
          
          dcTextPanel.animatorH = PropertySetter.createAnimator(600, dcTextPanel, "displayH", new FloatEval(), dcTextPanel.displayH, goalH);
          dcTextPanel.animatorW = PropertySetter.createAnimator(600, dcTextPanel, "displayW", new FloatEval(), dcTextPanel.displayW, goalW);
          dcTextPanel.animatorH.start();
          dcTextPanel.animatorW.start();
          SSM.instance().resizePanel = 0;   
       }
       
       
       ////////////////////////////////////////////////////////////////////////////////
       // Show the document viewer box
       ////////////////////////////////////////////////////////////////////////////////
       //if (SSM.instance().docVisible == true) {
       if (dcTextPanel.displayH >= 0.1) {
          setOrthonormalView(gl2, 0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight); {
             dcTextPanel.render(gl2);
          }
       }
      
       
       ////////////////////////////////////////////////////////////////////////////////
       // Optional, debugging elements
       ////////////////////////////////////////////////////////////////////////////////
       if (SSM.instance().useGuide == true) {
          setPerspectiveView(gl2); {
             GraphicUtil.drawAxis(gl2, 0, 0, 0);
          }
          
          setOrthonormalView(gl2, 0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight); {
             StatusWindow.tf.anchorX = SSM.instance().windowWidth - StatusWindow.tf.width;
             StatusWindow.tf.anchorY = 200; //StatusWindow.tf.height;
             StatusWindow.render(gl2);
          }
          
       }         
       
       ////////////////////////////////////////////////////////////////////////////////
       // Rener the combo boxes
       ////////////////////////////////////////////////////////////////////////////////
       setOrthonormalView(gl2, 0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight); {
          // Update the yoffset before rendering
          manufactureScroll.yoffset = SSM.instance().manufactureAttrib.yOffset;
          makeScroll.yoffset  = SSM.instance().makeAttrib.yOffset;
          modelScroll.yoffset = SSM.instance().modelAttrib.yOffset;
          yearScroll.yoffset  = SSM.instance().yearAttrib.yOffset;
          
          manufactureScroll.render(gl2);   
          makeScroll.render(gl2);
          modelScroll.render(gl2);
          yearScroll.render(gl2);
       }
       
       ////////////////////////////////////////////////////////////////////////////////
       // Renders a tool tip
       ////////////////////////////////////////////////////////////////////////////////
       setOrthonormalView(gl2, 0, SSM.instance().windowWidth, 0, SSM.instance().windowHeight); {
          DCTip.render(gl2);
       }
    }
       
    
    ////////////////////////////////////////////////////////////////////////////////
    // Render components in a single colour
    ////////////////////////////////////////////////////////////////////////////////
    public void renderNormal(GL2 gl2) {
       gl2.glPushMatrix();
          gl2.glEnable(GL2.GL_DEPTH_TEST);
          gl2.glDisable(GL2.GL_BLEND);
          String[] clist = this.getComponentSortedByCentroid2(gl2);
          for (int cidx = 0; cidx < clist.length; cidx++) {
             String partName  = clist[cidx];
             DCComponent modelComp = MM.currentModel.componentTable.get(partName);         
             modelComp.renderBufferToon(gl2);
             //modelComp.renderBuffer(gl2);
          }
       gl2.glPopMatrix();
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Render 'silhouette'...Why? because I can...that's why
    ////////////////////////////////////////////////////////////////////////////////
    public void renderSil(GL2 gl2) {
       String[] clist = this.getComponentSortedByCentroid(gl2);
       for (int cidx = 0; cidx < clist.length; cidx++) {
          DCComponent modelComp = MM.currentModel.componentTable.get( clist[cidx] );
          //modelComp.renderTriangleWithAdj(gl2);
          modelComp.renderBufferAdj(gl2, null);
       }
       //gl2.glUseProgram(0);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Renders a face normal visualization...more for debugging polygons than
    // any actual use
    ////////////////////////////////////////////////////////////////////////////////
    public void renderFNormal(GL2 gl2) {
       String clist[] = this.getComponentUnsorted(gl2);      
       for (int i=0; i < clist.length; i++) {
          DCComponent c = MM.currentModel.componentTable.get(clist[i]);        
          c.renderFNormal(gl2);
       }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Renders a per vertex normal visualization ... for debugging
    ////////////////////////////////////////////////////////////////////////////////
    public void renderVNormal(GL2 gl2) {
       String clist[] = this.getComponentUnsorted(gl2);      
       for (int i=0; i < clist.length; i++) {
          DCComponent c = MM.currentModel.componentTable.get(clist[i]);        
          c.renderVNormal(gl2);
       }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Render silhouette with the line width proportional to the frequence of occurrences
    ////////////////////////////////////////////////////////////////////////////////
    public void renderSilLineWidth(GL2 gl2) {
       String[] clist = this.getComponentSortedByCentroid2(gl2);   
       gl2.glLineWidth( 0.5f);
       for (int i=0; i < clist.length; i++) {
          DCComponent c = MM.currentModel.componentTable.get( clist[i] );   
          //gl2.glLineWidth( 0.5f + 4.0f*c.occurrence/(float)this.maxOccurrence); 
          c.renderBufferAdj(gl2, c.silhouetteColour);
       }
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Just for fun...use as a minimal rendering example
    ////////////////////////////////////////////////////////////////////////////////
    public void renderBoundingBox(GL2 gl2) {
       String[] clist = this.getComponentSortedByCentroid(gl2);
       for (int i = 0; i < clist.length; i++) {
          DCComponent c = MM.currentModel.componentTable.get( clist[i] );   
          c.boundingBox.renderBoundingBox(gl2);
       }
    }
       
    
    ///////////////////////////////////////////////////////////////////////////////// 
    // Render each component based up frequency of occurrences
    ///////////////////////////////////////////////////////////////////////////////// 
    public void renderColourRamp(GL2 gl2, LensAttrib la) {
       // Render the scene
       //gl2.glDisable(GL2.GL_DEPTH_TEST);
       gl2.glPushMatrix();
       
       //String[] clist = this.getComponentSortedByCentroid(gl2);
       String[] clist = null; 
       switch (SSM.instance().sortingMethod) {
          case 0: clist = this.getComponentSortedByCentroid2(gl2); break;
          case 1: clist = this.getComponentSortedByCentroid(gl2); break;
          case 2: clist = this.getComponentUnsorted(gl2); break;
          default:
             clist = this.getComponentSortedByCentroid2(gl2);
       }
       for (int cidx = 0; cidx < clist.length; cidx++) {
          String partName  = clist[cidx];
          
 
          DCComponent modelComp = MM.currentModel.componentTable.get(partName);         
          
          // Test
          if (la != null)
             if (DCCamera.instance().eye.sub(modelComp.center).mag() <= la.nearPlane) continue;
          
          
          
          // Check if the part is currently focused
          //Integer current  = SSM.instance().currentGroup;
          //Integer selected = SSM.instance().selectedGroup;
          
          //Hack TODO: Test
 //         if (partName.indexOf("_") > 0) {
 //            System.out.println(partName + " " + partName.indexOf("_"));
 //            partName = partName.substring(0, partName.indexOf('_')-1); 
 //         }         
 //         
          
 //         if (current  != null && HierarchyTable.instance().partTable.get(current).contains(modelComp.baseName) ||
 //             selected != null && HierarchyTable.instance().partTable.get(selected).contains(modelComp.baseName)) {
 	   
          if ( SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(modelComp.id)) {
             gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
             gl2.glColor4fv(SchemeManager.component_selected.toArray(), 0);
             
             gl2.glPushMatrix();
                // Render the component (selected)
                gl2.glUseProgram(0);
                modelComp.renderBuffer( gl2, modelComp.colour );
                gl2.glLineWidth(1.5f);
                modelComp.renderBufferAdj(gl2, SchemeManager.colour_blue);
                
                modelComp.boundingBox.renderBoundingBox(gl2);
                gl2.glLineWidth(0.5f);
                
                if (SSM.instance().renderSihoulette) {
                   modelComp.renderBufferAdj(gl2, null);
                }
             gl2.glPopMatrix();
             continue;
          } 
          
          // TODO:Highlight the related groups - temporary test, should move to better id/string check structure
          if (SSM.instance().relatedList.size() > 0 ) {
             boolean done = false;   
             for (int i=0; i < SSM.instance().relatedList.size(); i++) {
                //if ( HierarchyTable.instance().partTable.get(SSM.instance().relatedList.elementAt(i)).contains(modelComp.baseName)) {
             	if ( modelComp.id == SSM.instance().relatedList.elementAt(i)) {
                   done = true;   
                   gl2.glPushMatrix();
                      //modelComp.boundingBox.renderBoundingBox(gl2, DCColour.fromInt(0, 255, 255, 128));
                      //modelComp.boundingBox.renderBoundingBox(gl2, DCColour.fromInt(0, 255, 255, 128));
                      modelComp.renderBufferAdj(gl2, SchemeManager.colour_related.adjustAlpha(0.5f));
                   gl2.glPopMatrix();
                   break;
                }
             }
             //if (done == true) continue;
          }
          
          
          gl2.glPushMatrix();
             //gl2.glScalef( 1.0f/modelComp.scaleFactor.x, 1.0f/modelComp.scaleFactor.x, 1.0f/modelComp.scaleFactor.x);
             if (! modelComp.hasContext || ! modelComp.active) {
                gl2.glLineWidth(0.5f);
                modelComp.renderBufferAdj(gl2, null);
             } else {
                gl2.glUseProgram(0);
                //modelComp.renderBuffer(gl2, modelComp.colour);
                gl2.glEnable(GL2.GL_BLEND);
                //modelComp.renderBufferVaryLight(gl2, modelComp.colour);
                modelComp.renderBuffer(gl2, modelComp.colour);
                //modelComp.renderFNormal(gl2);
                
                if (SSM.instance().renderSihoulette) {
                   gl2.glLineWidth(0.5f);
                   modelComp.renderBufferAdj(gl2, null);
                }
             }
          gl2.glPopMatrix();
       
       } // end while
       gl2.glPopMatrix();
       //gl2.glEnable(GL2.GL_DEPTH_TEST);
    }
    
    
    
    /*
    public void renderExploded(GL2 gl2) {
       gl2.glPushMatrix();
       String[] clist = this.getComponentUnsorted(gl2);
       for (int i=0; i < clist.length; i++) {
          String partName  = clist[i];
          DCComponent modelComp = MM.currentModel.componentTable.get(partName);         
          
          // Find the distance away from the eye position
          float distance = modelComp.center.sub(DCCamera.instance().eye).mag();
          
          gl2.glTranslatef(0.0f, 1.0f, 0.0f);
          gl2.glPushMatrix();
 //            gl2.glScalef( 2.0f/modelComp.scaleFactor.x, 2.0f/modelComp.scaleFactor.y, 2.0f/modelComp.scaleFactor.z);
 //            gl2.glColor4f(modelComp.colour.r, modelComp.colour.g, modelComp.colour.b, modelComp.colour.a);
             gl2.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
             gl2.glUseProgram(0);
             modelComp.renderBasicMeshWithNormal(gl2);
          gl2.glPopMatrix();
                       
       }
       gl2.glPopMatrix();
    }
    */
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Check the X,Y coord and renders a tool tip, if necessary
    ////////////////////////////////////////////////////////////////////////////////
    public void renderToolTip(GL2 gl2) {
       //tip.render(gl2); 
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Init vert,geom and frag shaders
    // Init model and model component
    ////////////////////////////////////////////////////////////////////////////////
    public void init(GL2 gl2) {
       super.init(gl2);
       dcTextPanel.init(gl2);
       
       manufactureScroll = new DCScrollPane("MFR");
       manufactureScroll.anchorX = SSM.instance().manufactureAttrib.anchorX;
       manufactureScroll.anchorY = SSM.instance().manufactureAttrib.anchorY;
       manufactureScroll.calculate();
       manufactureScroll.renderToTexture(SchemeManager.colour_red.convertToAWT());
       
       makeScroll = new DCScrollPane("MAKE");
       makeScroll.anchorX = SSM.instance().makeAttrib.anchorX;
       makeScroll.anchorY = SSM.instance().makeAttrib.anchorY;
       makeScroll.calculate();
       makeScroll.renderToTexture(SchemeManager.colour_green.convertToAWT());
       
       modelScroll = new DCScrollPane("MODEL");
       modelScroll.anchorX = SSM.instance().modelAttrib.anchorX;
       modelScroll.anchorY = SSM.instance().modelAttrib.anchorY;
       modelScroll.calculate();
       modelScroll.renderToTexture(SchemeManager.colour_blue.convertToAWT());
       
       yearScroll = new DCScrollPane("YEAR");
       yearScroll.anchorX = SSM.instance().yearAttrib.anchorX;
       yearScroll.anchorY = SSM.instance().yearAttrib.anchorY;
       yearScroll.calculate();
       yearScroll.renderToTexture(SchemeManager.colour_blue.convertToAWT());
       
       
       MM.instance().initGPU(gl2);
       SSM.instance().dirty = 1;
       SSM.instance().dirtyGL = 1;
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Handles the animated transition for scrolling panels, specifically those
    // in the hierarchical search filter
    ////////////////////////////////////////////////////////////////////////////////
    public void scrollPaneTransition(float mx, float my, DCScrollPane widget, PaneAttrib attrib ) {
       if (DCUtil.between(mx, widget.anchorX, widget.anchorX+SSM.instance().scrollWidth)) {
          if (DCUtil.between(my, widget.anchorY-20, widget.anchorY)) {
             attrib.active = ! attrib.active;
             
             if (attrib.active) {
                widget.animator = PropertySetter.createAnimator(600, widget, "height", new FloatEval(), widget.height, attrib.height); 
                widget.animator.start();
             } else {
                widget.animator = PropertySetter.createAnimator(600, widget, "height", new FloatEval(), widget.height, 0.0f); 
                widget.animator.start();
             }
          }
       }   
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Handles select action for hierarchical scrolling panel filters
    ////////////////////////////////////////////////////////////////////////////////
    public void pickingScrollPane(float mx, float my, DCScrollPane widget, PaneAttrib attrib, Object ...childrenPair) {
       System.out.println("blah...");
       if (DCUtil.between(mx, attrib.anchorX, attrib.anchorX+SSM.instance().scrollWidth)) {
         if (DCUtil.between(my, attrib.anchorY, attrib.anchorY+attrib.height)) {
             
             // 1) Calculate the texture coordinate
             float texX = mx - attrib.anchorX;
             float texY = my - attrib.anchorY;
             
             // 2) Adjust for Y-offset
             texY = attrib.yOffset - (texY);
             System.out.println("Tex : " + texX + " " + texY);
             
             for (int i=0; i < widget.tagList.size(); i++) {
                GTag t = widget.tagList.elementAt(i);                
                // Window system is upside down
                if (texY >= t.yPrime && texY <= t.y) {
                   SSM.instance().dirtyDateFilter = 1;
                   widget.current = i; 
                   widget.currentStr = t.val;
                   widget.dirty  = true;
                   
                   SSM.instance().dirty = 1;
                   SSM.instance().dirtyGL = 1;
                   attrib.selected = i==0? null:t.val; 
                   
                   // Clear the children
                   for (int j=0; j < childrenPair.length; j+=2) {
                      ((DCScrollPane)childrenPair[j]).current = 0;
                      ((PaneAttrib)childrenPair[j+1]).selected = null;
                   }
                   
                   break;
                }
             }            
             return;
          }
       } 
       System.out.println("blah...end");
    }
    
 
    
 }
 
