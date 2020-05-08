 package model;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.geom.Rectangle2D;
 import java.nio.IntBuffer;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.glu.GLU;
 
 import org.jdesktop.animation.timing.interpolation.PropertySetter;
 
 import util.ALogger;
 import util.DCCamera;
 import util.DCUtil;
 import util.DWin;
 import util.GraphicUtil;
 import util.MatrixUtil;
 import util.ShaderObj;
 import Jama.Matrix;
 import TimingFrameExt.DCColourEval;
 
 import com.jogamp.opengl.util.awt.TextRenderer;
 
 import datastore.CacheManager;
 import datastore.HierarchyTable;
 import datastore.MM;
 import datastore.SSM;
 import datastore.SchemeManager;
 import db.QueryObj;
 import exec.RenderTask;
 import gui.DCScrollPane;
 import gui.DCTextPanel2;
 import gui.DCTip;
 import gui.GTag;
 import gui.StatusWindow;
 
 /////////////////////////////////////////////////////////////////////////////////
 // This class provides basic function that should be shared among all model rendering classes
 // The render and picking methods are declared abstract and must be implemented
 /////////////////////////////////////////////////////////////////////////////////
 public abstract class BaseModelRenderer implements RenderTask {
    
    public GLU glu = new GLU();
    public TextRenderer textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 22)); 
    
    public DCTextPanel2 dcTextPanel = new DCTextPanel2();
    
    // Data filter combo boxes
    public DCScrollPane manufactureScroll; 
    public DCScrollPane makeScroll;
    public DCScrollPane modelScroll;
    public DCScrollPane yearScroll;
    
    
    // Flow effect filter
    public FrameBufferTexture glowTexture;
    
    
    
    @Override
    public abstract void render(GL2 gl2);
 
    @Override
    public abstract void picking(GL2 gl2);
    
    @Override
    public void init(GL2 gl2) {
       // Initialize modeling data
       try {
          CacheManager.instance(); // Initialize the manager
          MM.instance();
          MM.instance().loadModels();
          
          
          InitDualPeelingRenderTargets(gl2);
          
          gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
          BuildShaders(gl2);
          MakeFullScreenQuad(gl2);
          
          gl2.glDisable(GL2.GL_CULL_FACE);
          gl2.glDisable(GL2.GL_LIGHTING);
          gl2.glDisable(GL2.GL_NORMALIZE);         
          
          gl2.glGenQueries(1, g_queryId, 0);
          
          DCTip.init(gl2);
          StatusWindow.init(gl2);
          
       } catch (Exception e) { 
          e.printStackTrace(); 
          System.exit(-1);
       }      
    }
 
    
   
    
    ////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////
    public void renderTextPolar(GL2 gl2, LensAttrib la, float angle, float scale, DCComponent comp, String s) {
       //gl2.glEnable(GL2.GL_BLEND);
       gl2.glEnable(GL2.GL_TEXTURE_2D);
       gl2.glActiveTexture(GL2.GL_TEXTURE0);
       gl2.glBindTexture(GL2.GL_TEXTURE_2D, 0);
       float radius = la.magicLensRadius;
       float x;
       float y;
       x = radius * (float)Math.cos(angle);
       y = radius * (float)Math.sin(angle);
       textRenderer.beginRendering(SSM.instance().windowWidth, SSM.instance().windowHeight);
       Rectangle2D rect = textRenderer.getBounds(s);
       
       angle = angle*180.0f/3.14f;
       if (angle < 0) angle += 360.0f;
       
       //if (angle*180.0f/3.14f >=90 && angle*180.0f/3.14f < 270.0f) {
       if (angle >= 90.0f && angle < 270.0f) {
          x =  x -(float) rect.getWidth()*scale;
       }
       
       /*
       textRenderer.draw3D(s, SSM.instance().mouseX+(int)x, 
                              (SSM.instance().windowHeight - SSM.instance().mouseY)+(int)y, 
                              0.0f, scale);
                              */
       textRenderer.draw3D(s, la.magicLensX+(int)x, 
             (SSM.instance().windowHeight - la.magicLensY)+(int)y, 
             0.0f, scale);      
       textRenderer.endRendering();
       gl2.glDisable(GL2.GL_TEXTURE_2D);          
       
       
       gl2.glLoadIdentity();
       gl2.glEnable(GL2.GL_BLEND);
       
       if (angle >= 90.0f && angle < 270.0f) {
          x -= comp.cchart.width;
       } else {
          x += rect.getWidth()*scale;
       } 
    
       //gl2.glTranslated(SSM.instance().mouseX + x , 
       //                 (SSM.instance().windowHeight - SSM.instance().mouseY) + y, 0);
       gl2.glTranslated(la.magicLensX + x , 
                        (SSM.instance().windowHeight - la.magicLensY) + y, 0);
       comp.cchart.renderImmediate(gl2);
       
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Standard transformation for model rendering
    ////////////////////////////////////////////////////////////////////////////////
    public void setPerspectiveView(GL2 gl2) {
       float aspect = (float)SSM.instance().windowWidth/ (float)SSM.instance().windowHeight;
       GraphicUtil.setPerspectiveView(gl2, aspect, SSM.instance().fov, 
             1, 1000, 
             DCCamera.instance().eye.toArray3f(), new float[]{0,0,0}, DCCamera.instance().up.toArray3f());
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Perspective with near and far
    ////////////////////////////////////////////////////////////////////////////////
    public void setPerspectiveView(GL2 gl2, float near, float far) {
       float aspect = (float)SSM.instance().windowWidth/ (float)SSM.instance().windowHeight;
       GraphicUtil.setPerspectiveView(gl2, aspect, SSM.instance().fov, 
             near, far, 
             DCCamera.instance().eye.toArray3f(), new float[]{0,0,0}, DCCamera.instance().up.toArray3f());
    }
    
    public void setOrthonormalView(GL2 gl2) {
       GraphicUtil.setOrthonormalView(gl2, 0, 1, 0, 1, -10, 10);
    }
    
    
    public void setOrthonormalView(GL2 gl2, float minx, float maxx, float miny, float maxy) {
       GraphicUtil.setOrthonormalView(gl2, minx, maxx, miny, maxy, -10, 10);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Set the condition to pick in perspective mode
    ////////////////////////////////////////////////////////////////////////////////
    public void startPickingPerspective(GL2 gl2, IntBuffer buffer) {
       GraphicUtil.startPickingPerspective(gl2, buffer, 
             SSM.instance().mouseX, SSM.instance().mouseY, 
             SSM.instance().windowWidth, SSM.instance().windowHeight, SSM.instance().fov, 
             DCCamera.instance().eye.toArray3f(), new float[]{0,0,0}, DCCamera.instance().up.toArray3f());
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Setup the viewport and the initial naming stack
    ////////////////////////////////////////////////////////////////////////////////
    public void startPickingOrtho(GL2 gl2, IntBuffer buffer) {
       GraphicUtil.startPickingOrtho(gl2, buffer, 
             SSM.instance().mouseX, SSM.instance().mouseY, 
             SSM.instance().windowWidth, SSM.instance().windowHeight);
    }   
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Calculate the hit
    ////////////////////////////////////////////////////////////////////////////////
    public Integer finishPicking(GL2 gl2, IntBuffer buffer) {
       return GraphicUtil.finishPicking(gl2, buffer);
    }   
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Reset a scroll-able panel
    ////////////////////////////////////////////////////////////////////////////////
    public void resetPane(Hashtable<String, Integer> table, DCScrollPane widget, PaneAttrib attrib) {
       int counter = 0;
       int prevManufacture = -1;
       widget.tagList.add( new GTag(10.0f, (counter+1)*DCScrollPane.spacing, counter*DCScrollPane.spacing, "--- ALL ---", "--- ALL ---"));
       counter++;
       
       Vector<String> list = new Vector<String>();
       for (String s : table.keySet()) {
          list.add( s );  
       }
       Collections.sort(list);
       for (int i=0; i < list.size(); i++) {
          String s = list.elementAt(i);
          String txt = s + " (" + table.get(s) + ")";
          GTag t = new GTag(10.0f, (counter+1)*DCScrollPane.spacing, counter*DCScrollPane.spacing, txt, s);
          if (t.val.equals(attrib.selected)) {
             prevManufacture = i;
             widget.currentStr = t.val;
          }
          widget.tagList.add( t );
          counter++;   
       }
       widget.texPanelHeight = widget.tagList.lastElement().y;
       attrib.textureHeight = widget.texPanelHeight;
       attrib.height = Math.min(attrib.textureHeight, SSM.instance().defaultScrollHeight);
       
       if (widget.height > 0) widget.height = attrib.height;
       widget.dirty = true;       
       if (prevManufacture < 0) {
          widget.current = 0;   
          widget.currentStr = widget.tagList.elementAt(0).val;
          attrib.selected = null;
          attrib.yOffset = attrib.height;
       }
       
       // Final adjustment
       if (prevManufacture >= 0) {
          float tempY = 0;
          tempY = widget.tagList.elementAt(prevManufacture).y + DCScrollPane.spacing;
          attrib.yOffset = Math.max( tempY, attrib.height);
       }
       
       // If the list only contains one element ==> IE: "ALL", then that means we did not select an item
       // in the upper hierarchy somewhere. So this means that we might as well hide it. 
       // We will still do the calculations above, just so everything is synchronized
       if (widget.tagList.size() == 1 ) {
          widget.visible = false;   
          widget.height = 0.0f;
          attrib.active = false;
       } else {
          widget.visible = true;
       }
       System.out.println(widget.label + " " + widget.tagList.size());
       
 
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Get a list of available filters, based on hierarchical order
    //  - startIdx : starting time frame
    //  - endIdx   : ending time frame
    //  - ancestor : higher level options that is applied to the current filter 
    ////////////////////////////////////////////////////////////////////////////////
    public Hashtable<String, Integer> getHierFilter(int startIdx, int endIdx, Object... ancestor) {
       Hashtable<String, Integer> result = new Hashtable<String, Integer>();
       
       for (int i=startIdx; i <= endIdx; i++) {
          String dtStr = CacheManager.instance().getTimeByIndex(i); 
          int month = Integer.parseInt(dtStr.substring(4,6)) - 1;
          if (month < SSM.instance().startMonth || month > SSM.instance().endMonth) continue;      
          
          QueryObj root = CacheManager.instance().queryTableU.elementAt(i);
          QueryObj qobj = root;
          QueryObj current = root;
          
          for (int j=0; j < ancestor.length; j++) {
             qobj = current.get( ((DCScrollPane)ancestor[j]).currentStr);
             if (qobj == null) break;
             current = qobj;
          }
          if (qobj == null) continue;
          
          for (String s: qobj.children.keySet()) {
             int count = qobj.children.get(s).count;
             if (result.containsKey(s)) {
                int prev = result.get(s);
                result.put(s, count+prev);
             } else {
                result.put(s, count);
             }
          } // end for qobj
          
       }
       
       return result;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Reset the groupOccurrence dataset 
    // using the updated date strings in SSM
    ////////////////////////////////////////////////////////////////////////////////
    public void resetData() {
       
       int startIdx = CacheManager.instance().getDateKey( SSM.instance().startTimeFrame ) == null ? 0:
          CacheManager.instance().getDateKey( SSM.instance().startTimeFrame );
       int endIdx   = CacheManager.instance().getDateKey( SSM.instance().endTimeFrame) == null ? CacheManager.instance().occurrenceTable.size():
          CacheManager.instance().getDateKey( SSM.instance().endTimeFrame);
 
       
       manufactureScroll.tagList.clear();
       makeScroll.tagList.clear();
       modelScroll.tagList.clear();
       yearScroll.tagList.clear();
       
       DWin.instance().error(SSM.instance().startMonth + " " + SSM.instance().endMonth);
       DWin.instance().error("Starting indices: " + startIdx + " " + endIdx );
       
       
       Hashtable<String, Integer> manufactureHash = this.getHierFilter(startIdx, endIdx);
       // Hack: Trim down the outliers
       DCUtil.removeLowerBound(manufactureHash, 100);
       this.resetPane(manufactureHash, manufactureScroll, SSM.instance().manufactureAttrib);
       
       
       
       Hashtable<String, Integer> makeHash = this.getHierFilter(startIdx, endIdx, manufactureScroll);
       // Trim down the outliers
       //DCUtil.removeLowerBound(makeHash, 0);
       this.resetPane(makeHash, makeScroll, SSM.instance().makeAttrib);
       
       
       
       Hashtable<String, Integer> modelHash = this.getHierFilter(startIdx, endIdx, manufactureScroll, makeScroll);
       // Trim the outliers
       //DCUtil.removeLowerBound(modelHash, 0);
       this.resetPane(modelHash, modelScroll, SSM.instance().modelAttrib);
       
       
       
       
       Hashtable<String, Integer> yearHash = this.getHierFilter(startIdx, endIdx, manufactureScroll, makeScroll, modelScroll);
       this.resetPane(yearHash, yearScroll, SSM.instance().yearAttrib);
 
             
       
       
       
       
       // Set up the month data for the selected criteria
       CacheManager.instance().filterMonthData = 
          CacheManager.instance().getFilterMonthlyStat(SSM.instance().startTimeFrame, SSM.instance().endTimeFrame, 
                SSM.instance().manufactureAttrib.selected, 
                SSM.instance().makeAttrib.selected, 
                SSM.instance().modelAttrib.selected,
                SSM.instance().yearAttrib.selected);
       
       System.out.print("??? Month: "); 
       for (int x=0; x < CacheManager.instance().filterMonthData.length; x++) {
          System.out.print( CacheManager.instance().filterMonthData[x]+  " " );   
       } System.out.println("\n");
       
       // Set up the year data for the selected criteria
       CacheManager.instance().filterYearData = 
          CacheManager.instance().getFilterYearStatArray(
                SSM.instance().manufactureAttrib.selected,
                SSM.instance().makeAttrib.selected,
                SSM.instance().modelAttrib.selected,
                SSM.instance().yearAttrib.selected);
       
       
       
       
       // Reset the occurrence frequency table
       SSM.instance().maxOccurrence = 0;
       SSM.instance().minOccurrence = 0;
       
       
       // Reset the global values
       if (SSM.instance().useAggregate == false) {
          CacheManager.instance().groupOccurrence = 
             CacheManager.instance().getPartOccurrenceFilter(
                startIdx, endIdx, 
                SSM.instance().startMonth,
                SSM.instance().endMonth, 
                SSM.instance().manufactureAttrib.selected, 
                SSM.instance().makeAttrib.selected, 
                SSM.instance().modelAttrib.selected,
                SSM.instance().yearAttrib.selected);
          
           if (SSM.instance().selectedGroup.size() > 0) {
              CacheManager.instance().monthMaximum = 
                CacheManager.instance().getMonthMaximumSelected(
                      SSM.instance().manufactureAttrib.selected, 
                      SSM.instance().makeAttrib.selected, 
                      SSM.instance().modelAttrib.selected,
                      SSM.instance().yearAttrib.selected);
           } else {
              CacheManager.instance().monthMaximum = 
                CacheManager.instance().getMonthMaximum(
                      SSM.instance().manufactureAttrib.selected, 
                      SSM.instance().makeAttrib.selected, 
                      SSM.instance().modelAttrib.selected,
                      SSM.instance().yearAttrib.selected);
           }
       } else {
           CacheManager.instance().groupOccurrence = 
             CacheManager.instance().getPartOccurrenceFilterAgg(
                startIdx, endIdx, 
                SSM.instance().startMonth,
                SSM.instance().endMonth, 
                SSM.instance().manufactureAttrib.selected, 
                SSM.instance().makeAttrib.selected, 
                SSM.instance().modelAttrib.selected,
                SSM.instance().yearAttrib.selected);
           
            if (SSM.instance().selectedGroup.size() > 0) {
               CacheManager.instance().monthMaximum = 
                 CacheManager.instance().getMonthMaximumSelectedAgg(
                       SSM.instance().manufactureAttrib.selected, 
                       SSM.instance().makeAttrib.selected, 
                       SSM.instance().modelAttrib.selected,
                       SSM.instance().yearAttrib.selected);
            } else {
               CacheManager.instance().monthMaximum = 
                 CacheManager.instance().getMonthMaximumAgg(
                       SSM.instance().manufactureAttrib.selected, 
                       SSM.instance().makeAttrib.selected, 
                       SSM.instance().modelAttrib.selected,
                       SSM.instance().yearAttrib.selected);
            }
       }
       
       
       // Pick up the related group(s) if there is a user
       // selected group in context
       if (SSM.instance().selectedGroup.size() > 0 ) {
          Vector<Integer> t = new Vector<Integer>();
          t.addAll( SSM.instance().selectedGroup.values());
          
          SSM.instance().relatedList = CacheManager.instance().getRelatedGroup(
                startIdx, endIdx,
                SSM.instance().startMonth, SSM.instance().endMonth,
                t,
                SSM.instance().manufactureAttrib.selected,
                SSM.instance().makeAttrib.selected,
                SSM.instance().modelAttrib.selected,
                SSM.instance().yearAttrib.selected);
                
       } else {
          // Remove all 
          SSM.instance().relatedList.clear(); 
       }
       
    
       
       
       
       
       
       Iterator<Integer> iter = CacheManager.instance().groupOccurrence.values().iterator();
       while (iter.hasNext()) {
          Integer v = iter.next();
          //if (SSM.instance().useLocalFocus ==  true) {
          //   if (SSM.instance().selectedGroup.size() > 0 && ! SSM.instance().relatedList.contains(v)) continue;     
          //} 
          
          if (SSM.instance().maxOccurrence < v) SSM.instance().maxOccurrence = v;
          if (SSM.instance().minOccurrence > v) SSM.instance().minOccurrence = v;
       }     
       
       System.out.println("Resetting Model Renderer Data");
       System.out.println("Max occurrence : " + SSM.instance().maxOccurrence);
       System.out.println("Min occurrence : " + SSM.instance().minOccurrence);
       System.out.println("\n");
       
       
       // Calculate the colour for each component
       String[] clist = getComponentUnsorted( null ); //passing in null (no context)
       for (int i = 0; i < clist.length; i++) {
          String partName = clist[i];
          DCComponent comp = MM.currentModel.componentTable.get(partName);
          comp.hasContext = true;
          
          
          // Calc based on an frequency to intensity colour scale
          //Integer partId  = HierarchyTable.instance().getGroupId(comp.baseName).size() > 0 ? HierarchyTable.instance().getGroupId(comp.baseName).elementAt(0) : null;
          float occ  = 0.0f;
          float mOcc = SSM.instance().maxOccurrence;
          if (comp.id > 0 && CacheManager.instance().groupOccurrence.get(comp.id) != null) {
             occ = (float)CacheManager.instance().groupOccurrence.get(comp.id).intValue(); 
             
             // Agg up to parent part (if possible) when using aggregation mode
             if (SSM.instance().useAggregate == true) {
                Integer tmp = comp.id;
                while(true) {
                   tmp = HierarchyTable.instance().getParentId(tmp);   
                   
                   // If parent is not null, and the model contains a reference to the parent, agg up to the 
                   // parent occurrence instance
                   if (tmp == null || MM.currentModel.componentTableById.get(tmp) == null)  break;
                   occ = (float)CacheManager.instance().groupOccurrence.get(tmp);
                } // end while
             } // end if agg
          }
          
          
          if (occ == 0.0 || comp.id < 0) 
             comp.hasContext = false;
          
          comp.active = true;
          if (SSM.instance().useLocalFocus == true) {
             if (SSM.instance().selectedGroup.size() > 0 && ! SSM.instance().relatedList.contains(comp.id)) {
                comp.active= false;
             }
          }
          
          
          //model.componentTable.get(partName).colour = SchemeManager.instance().intensityRamp02(partId, occ, maxOccurrence);
          //MM.currentModel.componentTable.get(partName).colour = SchemeManager.instance().intensityRamp03(partId, occ, maxOccurrence);
          //DCColour nextColour = SchemeManager.instance().intensityRamp03(partId, occ, maxOccurrence);
          DCColour nextColour = SchemeManager.instance().getColour(comp.id, occ, SSM.instance().maxOccurrence);
          //comp.occurrence = occ;
          
          
          // Adjust based on the current focus
          //Integer selected = SSM.instance().selectedGroup;
          if (SSM.instance().useLocalFocus != true) {
             if (SSM.instance().useAggregate) {
                if (SSM.instance().selectedGroup.size() > 0) {
                   Vector<Integer> v = HierarchyTable.instance().getAgg(SSM.instance().selectedGroup);
                   if ( ! v.contains(comp.id)) {
                      nextColour = nextColour.adjustAlpha(0.3f);
                   }
                }
             } else {
                // If not the selected group, make it out of focus
                //if (selected != null && selected != comp.id)
                if (SSM.instance().selectedGroup.size() > 0 && ! SSM.instance().selectedGroup.contains(comp.id)) 
                   nextColour = nextColour.adjustAlpha(0.3f);
             }
          }
          
          // Reset sparline for any colouring changes
          comp.cchart.colour = comp.colour;
          
          
          
          // Animator construction
          comp.canimator = PropertySetter.createAnimator(1000, comp.colour , "colour", new DCColourEval(), comp.colour, nextColour);
          //if (comp.lineAnimator != null && comp.lineAnimator.isRunning()) {
          //   comp.lineAnimator.stop();   
          //}
          //comp.lineAnimator = PropertySetter.createAnimator(1200, comp, "occurrence", new FloatEval(), 0.0f, occ);
 //         DCColour nextSilColour = SchemeManager.silhouette_default.add(new DCColour( occ/SSM.instance().maxOccurrence, -occ/SSM.instance().maxOccurrence, -occ/SSM.instance().maxOccurrence, 1));
 //         comp.lineAnimator = PropertySetter.createAnimator(1500, comp.silhouetteColour, "colour", new DCColourEval(), SchemeManager.silhouette_default, nextSilColour);
 //         comp.lineAnimator.setRepeatBehavior(RepeatBehavior.REVERSE);
 //         comp.lineAnimator.setRepeatCount(Animator.INFINITE);
       }
       
       MM.currentModel.startAnimation();
       
       
       // Log the changes
       ALogger.instance().log("Selected Year :" + SSM.instance().startYear + "-" + SSM.instance().endYear);
       ALogger.instance().log("Selected Month:" + SSM.instance().startMonth + "-" + SSM.instance().endMonth);
       ALogger.instance().log("Selected Group:" + SSM.instance().selectedGroup);
       
    }   
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Reset any graphic, user-interface or rendering related data
    // We do it here because it may require access to a OpenGL context, which 
    // is not directly(or easily) available in resetData() since they processed
    // in different threads
    ////////////////////////////////////////////////////////////////////////////////
    public void resetDataGL(GL2 gl2) {
 
          
       int startIdx = CacheManager.instance().getDateKey( SSM.instance().startTimeFrame ) == null ? 0:
                      CacheManager.instance().getDateKey( SSM.instance().startTimeFrame );
       
       //TODO: Need to fix this later, as dates may not be exact
       int endIdx   = CacheManager.instance().getDateKey( SSM.instance().endTimeFrame) == null ? CacheManager.instance().occurrenceTable.size():
                      CacheManager.instance().getDateKey( SSM.instance().endTimeFrame );
       int size = (endIdx - startIdx) + 1;      
       
       
       int chartStartIdx = startIdx;
       int chartEndIdx   = endIdx;
       int chartSize     = size;
       
       // Reassign the time-period for the graphs
       if (SSM.instance().useFullTimeLine == true) {
          chartSize = CacheManager.instance().queryTable.size();      
          chartStartIdx = 0;
          chartEndIdx   = chartSize - 1;
       }      
       
       
       DWin.instance().debug("startIndex:" + startIdx + "  endIdnex:" + endIdx);
       
       
       if (SSM.instance().selectedGroup.size() > 0 ) {
          dcTextPanel.t1.documentList.clear();
          dcTextPanel.t2.documentList.clear();
          dcTextPanel.t2.tagList.clear();
          dcTextPanel.t2.tagList.clear();
          
          Vector<Integer> groupList = new Vector<Integer>();
          groupList.addAll(SSM.instance().selectedGroup.values());
          /*
          if (SSM.instance().useAggregate) {
             groupList =  HierarchyTable.instance().getAgg(SSM.instance().selectedGroup);
          } else {
             //groupList.add(SSM.instance().selectedGroup);
             groupList.addAll(SSM.instance().selectedGroup.values());
          }
          */
         
          if (SSM.instance().useAggregate == true) {
             dcTextPanel.t1.documentList = CacheManager.instance().setDocumentDataAgg(
                   SSM.instance().startTimeFrame,
                   SSM.instance().endTimeFrame,
                   SSM.instance().startMonth,
                   SSM.instance().endMonth,
                   groupList,
                   SSM.instance().t1Start);
             
             dcTextPanel.t2.documentList = CacheManager.instance().setDocumentDataAgg(
                   SSM.instance().startTimeFrame,
                   SSM.instance().endTimeFrame,
                   SSM.instance().startMonth,
                   SSM.instance().endMonth,
                   groupList,
                   SSM.instance().t2Start);           
          } else {
             dcTextPanel.t1.documentList = CacheManager.instance().setDocumentData(
                   SSM.instance().startTimeFrame,
                   SSM.instance().endTimeFrame,
                   SSM.instance().startMonth,
                   SSM.instance().endMonth,
                   groupList,
                   SSM.instance().t1Start);
             
             dcTextPanel.t2.documentList = CacheManager.instance().setDocumentData(
                   SSM.instance().startTimeFrame,
                   SSM.instance().endTimeFrame,
                   SSM.instance().startMonth,
                   SSM.instance().endMonth,
                   groupList,
                   SSM.instance().t2Start);
          }
          
          
          DWin.instance().debug("Panel 1 size : " + dcTextPanel.t1.documentList.size());
          DWin.instance().debug("Panel 2 size : " + dcTextPanel.t2.documentList.size());
          
       }         
       
       
       if (dcTextPanel.t1.documentList.size() >= 0) {
          dcTextPanel.t1.calculate();
          dcTextPanel.t1.renderToTexture(Color.BLACK);
          SSM.instance().t1Height = dcTextPanel.t1.textPaneHeight;
       } 
       if (dcTextPanel.t2.documentList.size() >= 0) {
          dcTextPanel.t2.calculate();
          dcTextPanel.t2.renderToTexture(Color.BLACK);
          SSM.instance().t2Height = dcTextPanel.t2.textPaneHeight;
       } 
       
       if (SSM.instance().docAction == 1) {
          SSM.instance().yoffset = SSM.instance().t1Height + SSM.instance().docHeight;
       }
       if (SSM.instance().yoffset > dcTextPanel.t1.textPaneHeight + dcTextPanel.t2.textPaneHeight) {
          SSM.instance().yoffset = SSM.instance().docHeight;
       }
     
       
       float range = (CacheManager.timeLineEndYear - CacheManager.timeLineStartYear)+1;
       
       
       String[] clist = getComponentUnsorted( null ); //passing in null (no context)         
       for (int i = 0; i < clist.length; i++) {
          DCComponent comp = MM.currentModel.componentTable.get( clist[i] );   
          comp.selectedTotal = 0; // reset 
          
          if (comp.id < 0) continue;
          
          float data[] = new float[chartSize];
          short h[] = new short[chartSize];
          
          float localMax = 0.0f;
          for (int idx = chartStartIdx; idx <= chartEndIdx; idx++) {
             float value = 0;
             
             if (SSM.instance().useAggregate == false) {
                if (SSM.instance().selectedGroup.size() > 0) {
                   Vector<Integer> selectedGroup =  new Vector<Integer>();
                   selectedGroup.addAll( SSM.instance().selectedGroup.values());
                   Vector<Integer> t = new Vector<Integer>();
                   t.add(comp.id);                  
                   
                   value = CacheManager.instance().getCoOccurring(
                         idx, 
                         t, 
                         selectedGroup, 
                         SSM.instance().manufactureAttrib.selected,
                         SSM.instance().makeAttrib.selected,
                         SSM.instance().modelAttrib.selected,
                         SSM.instance().yearAttrib.selected);
                } else {
                   value = CacheManager.instance().getOcc(idx, comp.id, 
                         SSM.instance().manufactureAttrib.selected, 
                         SSM.instance().makeAttrib.selected, 
                         SSM.instance().modelAttrib.selected,
                         SSM.instance().yearAttrib.selected);
                }
             } else {
                if (SSM.instance().selectedGroup.size() > 0) {
                   Vector<Integer> selectedGroup =  new Vector<Integer>();
                   selectedGroup.addAll( SSM.instance().selectedGroup.values());                  
                  
                   value = CacheManager.instance().getCoOccurringAgg(
                         idx, 
                         HierarchyTable.instance().getAgg(comp.id), 
                         selectedGroup,
                         SSM.instance().manufactureAttrib.selected,
                         SSM.instance().makeAttrib.selected,
                         SSM.instance().modelAttrib.selected,
                         SSM.instance().yearAttrib.selected);
                  
                } else {
                   value = CacheManager.instance().getOccAgg(idx, comp.id, 
                         SSM.instance().manufactureAttrib.selected, 
                         SSM.instance().makeAttrib.selected, 
                         SSM.instance().modelAttrib.selected,
                         SSM.instance().yearAttrib.selected);              
                }
             }
             
             if (value > localMax) localMax = value;
             data[idx-chartStartIdx] = value;
             
             int y = Integer.parseInt(CacheManager.instance().getTimeByIndex(idx).substring(0, 4));
             int m = Integer.parseInt(CacheManager.instance().getTimeByIndex(idx).substring(4, 6))-1;
             
             comp.selectedTotal += 1;
             if (y >= SSM.instance().startYear && y <= SSM.instance().endYear) {
                if (m >= SSM.instance().startMonth && m <= SSM.instance().endMonth) {
                   h[idx-chartStartIdx] = 1;
                } else {
                   h[idx-chartStartIdx] = 0;
                }
             }
              
          }
          
          // # Segment restrictions
          int segSize = 0;
          if (data.length <= 24) {
             segSize = data.length;
          } else {
             segSize = 24;
          }
          
          
          
          
          
          comp.cchart.setHighlight(h);
          if (SSM.instance().sparklineMode == 0) {
             comp.cchart.setData( DCUtil.getDeriv(data) );
          } else if (SSM.instance().sparklineMode == 1){
             comp.cchart.setData( data ); // Original
          }
          //comp.sparkLine.setData( data ); // Original
          comp.cchart.setMaxValue(localMax);
          //if (SSM.instance().selectedGroup!= null && comp.id == SSM.instance().selectedGroup) {
          if (SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(comp.id)) {
             //comp.cchart.resize(SSM.instance().sparkLineWidth*1.2f, SSM.instance().sparkLineHeight*1.2f);
             //comp.cchart.resize(5*range*1.2f, SSM.instance().sparkLineHeight*1.2f);
             comp.cchart.resize(SSM.instance().sparkLineWidth*1.2f, 5*range*1.2f+20);
          } else {
             //comp.cchart.resize(SSM.instance().sparkLineWidth, SSM.instance().sparkLineHeight);
             //comp.cchart.resize(5*range, SSM.instance().sparkLineHeight);
             comp.cchart.resize(SSM.instance().sparkLineWidth, 5*range+20);
          }
          comp.cchart.createSegment( segSize ); // needs to go after setData and setHeight
          
          // Set it to the same colour as the component
          comp.cchart.colour = comp.colour;
       }
       
       
       // Update status
       StatusWindow.update();
    }
    
    
    
    
    public String getBaseName(String s) {
       if (s.indexOf("_") < 0) return s;
       return s.substring(0, s.indexOf('_')); 
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Returns a list of components via whatever order the
    // enumeration returns them.
    // Don't really need GL2 ... but to keep the API consistent
    ////////////////////////////////////////////////////////////////////////////////
    public String[] getComponentUnsorted(GL2 gl2) {
       Enumeration<String> e = MM.currentModel.componentTable.keys();
       String list[] = new String[ MM.currentModel.componentTable.size()];
       int c = 0;
       while (e.hasMoreElements()) {
          list[c] = e.nextElement();
          c++;
       }
       return list;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Returns a list of component sorted by their projected Y onto the near plane.
    // The projection point is based on the projection of the objects centroid
    ////////////////////////////////////////////////////////////////////////////////
    public String[] getComponentSortedByProjY(GL2 gl2) {
       Enumeration<String> e = MM.currentModel.componentTable.keys();
       String sortingList[] = new String[ MM.currentModel.componentTable.size()];
       
       int c = 0;
       while(e.hasMoreElements()) {
          sortingList[c] = e.nextElement();
          c++;
       }
       
       for (int i=0; i < sortingList.length; i++) {
          for (int j=0; j < sortingList.length-1; j++) {
             float d1 =  MM.currentModel.componentTable.get(sortingList[j]).projCenter.y;
             float d2 =  MM.currentModel.componentTable.get(sortingList[j+1]).projCenter.y;
             
             if (d1 < d2) {
                String tmp = sortingList[j];
                sortingList[j] = sortingList[j+1];
                sortingList[j+1] = tmp;
             }
          } 
       }
      
       return sortingList;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Returns a list of components sorted by distance away from the origin
    ////////////////////////////////////////////////////////////////////////////////
    public String[] getComponentSortedByCentroid2(GL2 gl2) {
       Enumeration<String> e = MM.currentModel.componentTable.keys();
       String sortingList[] = new String[ MM.currentModel.componentTable.size()];
       
       int c = 0;
       while(e.hasMoreElements()) {
          sortingList[c] = e.nextElement();
          c++;
       }      
       
       
       for (int i=0; i < sortingList.length; i++) {
          for (int j=0; j < sortingList.length; j++) {
             float d1 = MM.instance().currentModel.componentTable.get(sortingList[i]).center.sub(new DCTriple(0,0,0)).mag();
             float d2 = MM.instance().currentModel.componentTable.get(sortingList[j]).center.sub(new DCTriple(0,0,0)).mag();
             if ( d1 < d2 ) {
                String tmp = sortingList[i];   
                sortingList[i] = sortingList[j];
                sortingList[j] = tmp;
             }
          }
       }
       
       return sortingList;      
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Returns a list of component sorted by their distance away from the
    // eye position. This is a naive sort as it is based on the object centroid
    // which can be inaccurate if the components overlaps, or if the component 
    // has non-uniform distribution of polygons
    ////////////////////////////////////////////////////////////////////////////////
    public String[] getComponentSortedByCentroid(GL2 gl2) {
       Enumeration<String> e = MM.currentModel.componentTable.keys();
       String sortingList[] = new String[ MM.currentModel.componentTable.size()];
       
       // Get the modelView matrix from the openGL stack
       float modelV[] = new float[16];
       gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelV, 0);
       Matrix modelV4x4 = new Matrix(new double[][] {
             {modelV[0], modelV[4], modelV[8],  modelV[12]},
             {modelV[1], modelV[5], modelV[9],  modelV[13]},
             {modelV[2], modelV[6], modelV[10], modelV[14]},
             {modelV[3], modelV[7], modelV[11], modelV[15]}
       });
       Matrix modelV4x4Inv = modelV4x4.inverse();
       
       // Inverse transform the eye position to calculate the distance to the centroid
       double eye[] = { DCCamera.instance().eye.x, DCCamera.instance().eye.y, DCCamera.instance().eye.z, 1 };
       double eye_inv[] = MatrixUtil.multVector(modelV4x4Inv, eye);
       DCTriple eyeT = new DCTriple( (float)eye_inv[0], (float)eye_inv[1], (float)eye_inv[2]);
       
       int c = 0;
       while(e.hasMoreElements()) {
          sortingList[c] = e.nextElement();
          c++;
       }
       
       // Run-of-the mill bubble sort
       // Slow, but if we do get thousands of components there are better
       // problems to worry about...
       for (int i=0; i < sortingList.length; i++) {
          for (int j=0; j < sortingList.length; j++) {
             DCTriple centroid1 =  MM.currentModel.componentTable.get(sortingList[i]).center;   
             DCTriple centroid2 =  MM.currentModel.componentTable.get(sortingList[j]).center;   
             
             float d1 = eyeT.sub(centroid1).mag();
             float d2 = eyeT.sub(centroid2).mag();
             if (d1 < d2) {
                String tmp = sortingList[i];
                sortingList[i] = sortingList[j];
                sortingList[j] = tmp;
             }
          } 
       }
       return sortingList;
    }   
    
    
    ////////////////////////////////////////////////////////////////////////////////
    // Set the projected 3D coordinates in 2D space
    ////////////////////////////////////////////////////////////////////////////////
    public void setProjectedCoord(GL2 gl2) {
       float buffer[];
       float viewBuffer[], projBuffer[];
       
       buffer = new float[16];   
       gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
       projBuffer = buffer;
       
       buffer = new float[16];
       gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
       viewBuffer = buffer;
       
       int viewport[] = new int[4];
       gl2.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);             
       
       float wincoord[] = new float[3];      
       
       String clist[] = this.getComponentUnsorted(gl2);
       for (int i=0; i < clist.length; i++) {
          DCComponent comp = MM.currentModel.componentTable.get(clist[i]);
          glu.gluProject(
                comp.center.x, 
                comp.center.y,
                comp.center.z,
                viewBuffer, 0,
                projBuffer, 0,
                viewport, 0,
                wincoord, 0
                );         
          comp.projCenter = new DCTriple( wincoord[0], wincoord[1], wincoord[2]);
       }
       
    }
    
    
    // Ported from JOGL demo pakcage 
    // Translated from C++ Version see below:
    //--------------------------------------------------------------------------------------
    // Order Independent Transparency with Dual Depth Peeling
    //
    // Author: Louis Bavoil
    // Email: sdkfeedback@nvidia.com
    //
    // Depth peeling is traditionally used to perform order independent transparency (OIT)
    // with N geometry passes for N transparency layers. Dual depth peeling enables peeling
    // N transparency layers in N/2+1 passes, by peeling from the front and the back
    // simultaneously using a min-max depth buffer. This sample performs either normal or
    // dual depth peeling and blends on the fly.
    //
    // Copyright (c) NVIDIA Corporation. All rights reserved.
    //--------------------------------------------------------------------------------------
      public void InitDualPeelingRenderTargets(GL2 gl) {
         DWin.instance().msg("SSM height is : " + SSM.instance().windowHeight + " SSM width : " + SSM.instance().windowWidth);
         
         gl.glGenTextures(2, g_dualDepthTexId, 0);
         gl.glGenTextures(2, g_dualFrontBlenderTexId, 0);
         gl.glGenTextures(2, g_dualBackTempTexId, 0);
         gl.glGenFramebuffers(1, g_dualPeelingSingleFboId, 0);
         for (int i = 0; i < 2; i++) {
            gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualDepthTexId[i]);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S,  GL2.GL_CLAMP);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T,  GL2.GL_CLAMP);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
            
            //gl.glEnable( GL2.GL_PIXEL_UNPACK_BUFFER );
            gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0,  GL2.GL_FLOAT_RG32_NV, 
                  SSM.instance().windowWidth, SSM.instance().windowHeight,
                  0,  GL2.GL_RGB,  GL2.GL_FLOAT, null);
 
            gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualFrontBlenderTexId[i]);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
            gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0,  GL2.GL_RGBA, 
                  SSM.instance().windowWidth, SSM.instance().windowHeight,
                  0,  GL2.GL_RGBA,  GL2.GL_FLOAT, null);
 
            gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackTempTexId[i]);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
            gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
            gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0,  GL2.GL_RGBA, 
                  SSM.instance().windowWidth, SSM.instance().windowHeight,
                  0,  GL2.GL_RGBA,  GL2.GL_FLOAT, null);
         }
 
         gl.glGenTextures(1, g_dualBackBlenderTexId, 0);
         gl.glBindTexture( GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackBlenderTexId[0]);
         gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
         gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
         gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MIN_FILTER,  GL2.GL_NEAREST);
         gl.glTexParameteri( GL2.GL_TEXTURE_RECTANGLE_ARB,  GL2.GL_TEXTURE_MAG_FILTER,  GL2.GL_NEAREST);
         gl.glTexImage2D( GL2.GL_TEXTURE_RECTANGLE_ARB, 0, GL2.GL_RGB, 
               SSM.instance().windowWidth, SSM.instance().windowHeight,
               0, GL2.GL_RGB, GL2.GL_FLOAT, null);
 
         gl.glGenFramebuffers(1, g_dualBackBlenderFboId, 0);
         gl.glBindFramebuffer( GL2.GL_FRAMEBUFFER, g_dualBackBlenderFboId[0]);
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackBlenderTexId[0], 0);
 
         gl.glBindFramebuffer( GL2.GL_FRAMEBUFFER, g_dualPeelingSingleFboId[0]);
 
         int j = 0;
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT0,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualDepthTexId[j], 0);
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT1,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualFrontBlenderTexId[j], 0);
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT2,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackTempTexId[j], 0);
 
         j = 1;
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT3,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualDepthTexId[j], 0);
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT4,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualFrontBlenderTexId[j], 0);
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT5,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackTempTexId[j], 0);
 
         gl.glFramebufferTexture2D( GL2.GL_FRAMEBUFFER,  GL2.GL_COLOR_ATTACHMENT6,
               GL2.GL_TEXTURE_RECTANGLE_ARB, g_dualBackBlenderTexId[0], 0);
         
         
         // Back to normal ???? - DC
         gl.glBindFramebuffer( GL2.GL_FRAMEBUFFER, 0);
      }
 
      
      void DeleteDualPeelingRenderTargets(GL2 gl) {
         gl.glDeleteFramebuffers(1, g_dualBackBlenderFboId, 0);
         gl.glDeleteFramebuffers(1, g_dualPeelingSingleFboId, 0);
         gl.glDeleteTextures(2, g_dualDepthTexId, 0);
         gl.glDeleteTextures(2, g_dualFrontBlenderTexId, 0);
         gl.glDeleteTextures(2, g_dualBackTempTexId, 0);
         gl.glDeleteTextures(1, g_dualBackBlenderTexId, 0);
      }
      
      void BuildShaders(GL2 gl2) {
         System.err.println("\nloading shaders...\n");
 
         g_shaderDualInit = new ShaderObj();
         g_shaderDualInit.createShader(gl2, "src\\Shader\\dual_peeling_init_vertex.glsl", GL2.GL_VERTEX_SHADER);
         g_shaderDualInit.createShader(gl2, "src\\Shader\\dual_peeling_init_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
         g_shaderDualInit.createProgram(gl2);
         g_shaderDualInit.linkProgram(gl2);
 
         g_shaderDualPeel = new ShaderObj();
         g_shaderDualPeel.createShader(gl2, "src\\Shader\\shade_vertex.glsl", GL2.GL_VERTEX_SHADER);
         g_shaderDualPeel.createShader(gl2, "src\\Shader\\dual_peeling_peel_vertex.glsl", GL2.GL_VERTEX_SHADER);
         g_shaderDualPeel.createShader(gl2, "src\\Shader\\shade_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
         g_shaderDualPeel.createShader(gl2, "src\\Shader\\dual_peeling_peel_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
         g_shaderDualPeel.createProgram(gl2);
         g_shaderDualPeel.linkProgram(gl2);
 
         g_shaderDualBlend = new ShaderObj();
         g_shaderDualBlend.createShader(gl2, "src\\Shader\\dual_peeling_blend_vertex.glsl", GL2.GL_VERTEX_SHADER);
         g_shaderDualBlend.createShader(gl2, "src\\Shader\\dual_peeling_blend_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
         g_shaderDualBlend.createProgram(gl2);
         g_shaderDualBlend.linkProgram(gl2);
 
         g_shaderDualFinal = new ShaderObj();
         g_shaderDualFinal.createShader(gl2, "src\\Shader\\dual_peeling_final_vertex.glsl", GL2.GL_VERTEX_SHADER);
         g_shaderDualFinal.createShader(gl2, "src\\Shader\\dual_peeling_final_fragment.glsl", GL2.GL_FRAGMENT_SHADER);
         g_shaderDualFinal.createProgram(gl2);
         g_shaderDualFinal.linkProgram(gl2);
 
      }     
      void MakeFullScreenQuad(GL2 gl) {
         GLU glu = GLU.createGLU(gl);
 
         g_quadDisplayList = gl.glGenLists(1);
         gl.glNewList(g_quadDisplayList, GL2.GL_COMPILE);
 
         gl.glMatrixMode(GL2.GL_MODELVIEW);
         gl.glPushMatrix();
         gl.glLoadIdentity();
         glu.gluOrtho2D(0.0f, 1.0f, 0.0f, 1.0f);
         gl.glBegin(GL2.GL_QUADS);
         {
            gl.glVertex2f(0.0f, 0.0f); 
            gl.glVertex2f(1.0f, 0.0f);
            gl.glVertex2f(1.0f, 1.0f);
            gl.glVertex2f(0.0f, 1.0f);
         }
         gl.glEnd();
         gl.glPopMatrix();
 
         gl.glEndList();
      }     
      void RenderDualPeeling(GL2 gl) {
         gl.glDisable(GL2.GL_DEPTH_TEST);
         gl.glEnable(GL2.GL_BLEND);
 
         // ---------------------------------------------------------------------
         // 1. Initialize Min-Max Depth Buffer
         // ---------------------------------------------------------------------
 
         gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, g_dualPeelingSingleFboId[0]);
 
         // Render targets 1 and 2 store the front and back colors
         // Clear to 0.0 and use MAX blending to filter written color
         // At most one front color and one back color can be written every pass
         gl.glDrawBuffers(2, g_drawBuffers, 1);
         gl.glClearColor(0, 0, 0, 0);
         gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
 
         // Render target 0 stores (-minDepth, maxDepth, alphaMultiplier)
         gl.glDrawBuffer(g_drawBuffers[0]);
         gl.glClearColor(-MAX_DEPTH, -MAX_DEPTH, 0, 0);
         gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
         gl.glBlendEquation(GL2.GL_MAX);
 
         g_shaderDualInit.bind(gl);
         DrawModel(gl);
         g_shaderDualInit.unbind(gl);
 
         // ---------------------------------------------------------------------
         // 2. Dual Depth Peeling + Blending
         // ---------------------------------------------------------------------
 
         // Since we cannot blend the back colors in the geometry passes,
         // we use another render target to do the alpha blending
         //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, g_dualBackBlenderFboId);
         gl.glDrawBuffer(g_drawBuffers[6]);
         //gl.glClearColor(g_backgroundColor[0], g_backgroundColor[1], g_backgroundColor[2], 0);
         gl.glClearColor(1.0f, 1.0f, 1.0f, 0);
         gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
 
         
 
 
         currId = 0;
         for (int pass = 1; g_useOQ || pass < g_numPasses; pass++) {
            currId = pass % 2;
            int prevId = 1 - currId;
            int bufId = currId * 3;
 
            //glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, g_dualPeelingFboId[currId]);
 
            gl.glDrawBuffers(2, g_drawBuffers, bufId+1);
            gl.glClearColor(0, 0, 0, 0);
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
 
            gl.glDrawBuffer(g_drawBuffers[bufId+0]);
            gl.glClearColor(-MAX_DEPTH, -MAX_DEPTH, 0, 0);
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
 
            // Render target 0: RG32F MAX blending
            // Render target 1: RGBA MAX blending
            // Render target 2: RGBA MAX blending
            gl.glDrawBuffers(3, g_drawBuffers, bufId+0);
            gl.glBlendEquation(GL2.GL_MAX);
 
            g_shaderDualPeel.bind(gl);
            g_shaderDualPeel.bindTextureRECT(gl,"DepthBlenderTex", g_dualDepthTexId[prevId], 0);
            g_shaderDualPeel.bindTextureRECT(gl,"FrontBlenderTex", g_dualFrontBlenderTexId[prevId], 1);
            //g_shaderDualPeel.setUniform(gl,"Alpha", g_opacity, 1);
            // Hackhack
            g_shaderDualPeel.setUniform1fv(gl, "Alpha", g_opacity);
            
            DrawModel(gl);
            g_shaderDualPeel.unbind(gl);
 
            // Full screen pass to alpha-blend the back color
            gl.glDrawBuffer(g_drawBuffers[6]);
 
            gl.glBlendEquation(GL2.GL_FUNC_ADD);
            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
 
            if (g_useOQ) {
               gl.glBeginQuery(GL2.GL_SAMPLES_PASSED, g_queryId[0]);
            }
 
            g_shaderDualBlend.bind(gl);
            g_shaderDualBlend.bindTextureRECT(gl,"TempTex", g_dualBackTempTexId[currId], 0);
            gl.glCallList(g_quadDisplayList);
            g_shaderDualBlend.unbind(gl);
 
            if (g_useOQ) {
               gl.glEndQuery(GL2.GL_SAMPLES_PASSED);
               int[] sample_count = new int[]{0};
               gl.glGetQueryObjectuiv(g_queryId[0], GL2.GL_QUERY_RESULT, sample_count, 0);
               if (sample_count[0] == 0) {
                  break;
               }
            }
         }
         
         
         // Hackhack : Put this here for now
         // Draw silhouette where necessary
         gl.glEnable(GL2.GL_DEPTH_TEST);
         gl.glEnable(GL2.GL_BLEND);
         for (DCComponent comp : MM.currentModel.componentTable.values()) {
            if (!comp.hasContext || !comp.active) {
               comp.renderBufferAdj(gl, null);
            } else if (SSM.instance().relatedList.size() > 0 && SSM.instance().relatedList.contains(comp.id) && !SSM.instance().selectedGroup.contains(comp.id)){
               //comp.renderBufferAdj(gl, SchemeManager.colour_related.adjustAlpha(0.6f));   
            } 
         }          
         
 
         gl.glDisable(GL2.GL_BLEND);
 
         // ---------------------------------------------------------------------
         // 3. Final Pass
         // ---------------------------------------------------------------------
 
         gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
         gl.glDrawBuffer(GL2.GL_BACK);
 
         g_shaderDualFinal.bind(gl);
         g_shaderDualFinal.bindTextureRECT(gl,"FrontBlenderTex", g_dualFrontBlenderTexId[currId], 1);
         g_shaderDualFinal.bindTextureRECT(gl,"BackBlenderTex", g_dualBackBlenderTexId[0], 2);
         gl.glCallList(g_quadDisplayList);
         g_shaderDualFinal.unbind(gl);
         
         
       
      }     
      
      
      public void RenderOITTexture(GL2 gl2) {
         gl2.glDisable(GL2.GL_BLEND);
         gl2.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
         gl2.glDrawBuffer(GL2.GL_BACK);
         
         g_shaderDualFinal.bind(gl2);
            g_shaderDualFinal.bindTextureRECT(gl2,"FrontBlenderTex", g_dualFrontBlenderTexId[currId], 1);
            g_shaderDualFinal.bindTextureRECT(gl2,"BackBlenderTex", g_dualBackBlenderTexId[0], 2);
            gl2.glCallList(g_quadDisplayList);
         g_shaderDualFinal.unbind(gl2);
         
         // Hackhack : Put this here for now
         // Draw silhouette if necessary
         /*
         gl2.glEnable(GL2.GL_DEPTH_TEST);
         gl2.glEnable(GL2.GL_BLEND);
         for (DCComponent comp : MM.currentModel.componentTable.values()) {
            if (SSM.instance().relatedList.size() > 0 && SSM.instance().relatedList.contains(comp.id) && !SSM.instance().selectedGroup.contains(comp.id)){
               comp.renderBufferAdj(gl2, SchemeManager.colour_related.adjustAlpha(0.3f));   
            }
         }        
         */
     }
      
      public void DrawModel(GL2 gl2) {
         for (DCComponent comp : MM.currentModel.componentTable.values()) {
            if (! comp.hasContext || ! comp.active) continue; // Dont' render if it does not have associated values
            
            g_shaderDualPeel.setUniformf(gl2, "compColour", comp.colour.r, comp.colour.g, comp.colour.b, comp.colour.a);
            g_shaderDualPeel.setUniform1i(gl2, "useLight", 1);
            //g_shaderDualPeel.setUniform1i(gl2, "useLight", 0);
            
            // Hack Test
            if (SSM.instance().useConstantAlpha == false) {
               g_opacity[0] = comp.colour.a;
               g_shaderDualPeel.setUniform1fv(gl2, "Alpha", g_opacity);
            }
               
            gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, comp.vboOIT[0]);
            gl2.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, comp.vboOIT[1]);
            gl2.glVertexPointer(3, GL2.GL_FLOAT, 24, 0);
            gl2.glNormalPointer(GL2.GL_FLOAT, 24, 12);
            
            gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            gl2.glDrawElements( GL2.GL_TRIANGLES, comp.faceList.size()*3, GL2.GL_UNSIGNED_INT, 0);
            gl2.glBindBuffer( GL2.GL_ARRAY_BUFFER, 0);
            gl2.glBindBuffer( GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
            
            if (SSM.instance().selectedGroup.size() > 0 && SSM.instance().selectedGroup.contains(comp.id)){
               //g_opacity[0] = comp.colour.a;
               //g_shaderDualPeel.setUniform1fv(gl2, "Alpha", g_opacity);
               g_shaderDualPeel.setUniformf(gl2, "compColour", SchemeManager.colour_blue.r, SchemeManager.colour_blue.g, SchemeManager.colour_blue.b, 1.0f);
               g_shaderDualPeel.setUniform1fv(gl2, "Alpha", new float[]{1.0f});
               g_shaderDualPeel.setUniform1i(gl2, "useLight", 0);
               comp.boundingBox.renderBoundingBox(gl2);   
               g_shaderDualPeel.setUniform1fv(gl2, "Alpha", g_opacity);
            } else if (SSM.instance().relatedList.size() > 0 && SSM.instance().relatedList.contains(comp.id)) {
               //g_shaderDualPeel.setUniform1i(gl2, "useLight", 0);
               //g_shaderDualPeel.setUniformf(gl2, "compColour", SchemeManager.colour_related.r, SchemeManager.colour_related.g, SchemeManager.colour_related.b, 1.0f);
               //comp.renderBufferAdj(gl2, SchemeManager.colour_related); 
            }
 
         }
         g_numGeoPasses++;
      }     
      
      public int currId;
      public int[]  g_dualBackBlenderFboId = new int[1];
      public int[]  g_dualPeelingSingleFboId = new int[1];
      public int[]  g_dualDepthTexId = new int[2];
      public int[]  g_dualFrontBlenderTexId = new int[2];
      public int[]  g_dualBackTempTexId = new int[2];
      public int[]  g_dualBackBlenderTexId = new int[1]; 
      
      public int g_drawBuffers[] = {GL2.GL_COLOR_ATTACHMENT0,
            GL2.GL_COLOR_ATTACHMENT1,
            GL2.GL_COLOR_ATTACHMENT2,
            GL2.GL_COLOR_ATTACHMENT3,
            GL2.GL_COLOR_ATTACHMENT4,
            GL2.GL_COLOR_ATTACHMENT5,
            GL2.GL_COLOR_ATTACHMENT6
      };   
      
      
   	  public int g_quadDisplayList;
      public final static float MAX_DEPTH = 1.0f;  	  
 //     public boolean g_useOQ = true;
      public boolean g_useOQ = false;
      public int[] g_queryId = new int[1];
 //     public float[] g_opacity = new float[]{0.6f};     
      public float[] g_opacity = new float[]{0.5f};     
 //     public int g_numPasses = 4;
 //     public int g_numPasses = 8;
      public int g_numPasses = 8;
      public int g_numGeoPasses = 0;
      
      
      public ShaderObj g_shaderDualInit;
      public ShaderObj g_shaderDualPeel;
      public ShaderObj g_shaderDualBlend;
      public ShaderObj g_shaderDualFinal;    
        
    
    
    
 }
