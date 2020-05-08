 
 
 package com.iver.cit.gvsig.gui.cad.tools.smc;
 
 import java.awt.event.InputEvent;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.gui.cad.CADStatus;
 import com.iver.cit.gvsig.gui.cad.tools.CutPolygonCADTool;
 
 
 /**
  * @author Jos Ignacio Lamas Fonte [LBD]
  * @author Nacho Varela [Cartolab]
  * @author Pablo Sanxiao [CartoLab]
  */
 
 public final class CutPolygonCADToolContext
     extends statemap.FSMContext
 {
 //---------------------------------------------------------------
 // Member methods.
 //
 
     public CutPolygonCADToolContext(CutPolygonCADTool owner)
     {
         super();
 
         _owner = owner;
         setState(CutPolygon.FirstPoint);
         CutPolygon.FirstPoint.Entry(this);
     }
 
     public void addOption(String s)
     {
         _transition = "addOption";
         getState().addOption(this, s);
         _transition = "";
         return;
     }
 
     public void addPoint(double pointX, double pointY, InputEvent event)
     {
         _transition = "addPoint";
         getState().addPoint(this, pointX, pointY, event);
         _transition = "";
         return;
     }
 
     public void removePoint(InputEvent event, int numPoints)
     {
         _transition = "removePoint";
         getState().removePoint(this, event, numPoints);
         _transition = "";
         return;
     }
 
     public CutPolygonCADToolState getState()
         throws statemap.StateUndefinedException
     {
         if (_state == null)
         {
             throw(
                 new statemap.StateUndefinedException());
         }
 
         return ((CutPolygonCADToolState) _state);
     }
 
     protected CutPolygonCADTool getOwner()
     {
         return (_owner);
     }
 
 //---------------------------------------------------------------
 // Member data.
 //
 
     transient private CutPolygonCADTool _owner;
 
 //---------------------------------------------------------------
 // Inner classes.
 //
 
     public static abstract class CutPolygonCADToolState
         extends statemap.State
     {
     //-----------------------------------------------------------
     // Member methods.
     //
 
         protected CutPolygonCADToolState(String name, int id)
         {
             super (name, id);
         }
 
         protected void Entry(CutPolygonCADToolContext context) {}
         protected void Exit(CutPolygonCADToolContext context) {}
 
         protected void addOption(CutPolygonCADToolContext context, String s)
         {
             Default(context);
         }
 
         protected void addPoint(CutPolygonCADToolContext context, double pointX, double pointY, InputEvent event)
         {
             Default(context);
         }
 
         protected void removePoint(CutPolygonCADToolContext context, InputEvent event, int numPoints)
         {
             Default(context);
         }
 
         protected void Default(CutPolygonCADToolContext context)
         {
             throw (
                 new statemap.TransitionUndefinedException(
                     "State: " +
                     context.getState().getName() +
                     ", Transition: " +
                     context.getTransition()));
         }
 
     //-----------------------------------------------------------
     // Member data.
     //
     }
 
     /* package */ static abstract class CutPolygon
     {
     //-----------------------------------------------------------
     // Member methods.
     //
 
     //-----------------------------------------------------------
     // Member data.
     //
 
         //-------------------------------------------------------
         // Statics.
         //
         /* package */ static CutPolygon_Default.CutPolygon_FirstPoint FirstPoint;
         /* package */ static CutPolygon_Default.CutPolygon_SecondPoint SecondPoint;
         /* package */ static CutPolygon_Default.CutPolygon_NextPoint NextPoint;
         private static CutPolygon_Default Default;
 
         static
         {
             FirstPoint = new CutPolygon_Default.CutPolygon_FirstPoint("CutPolygon.FirstPoint", 0);
             SecondPoint = new CutPolygon_Default.CutPolygon_SecondPoint("CutPolygon.SecondPoint", 1);
             NextPoint = new CutPolygon_Default.CutPolygon_NextPoint("CutPolygon.NextPoint", 2);
             Default = new CutPolygon_Default("CutPolygon.Default", -1);
         }
 
     }
 
     protected static class CutPolygon_Default
         extends CutPolygonCADToolState
     {
     //-----------------------------------------------------------
     // Member methods.
     //
 
         protected CutPolygon_Default(String name, int id)
         {
             super (name, id);
         }
 
         protected void addPoint(CutPolygonCADToolContext context, double pointX, double pointY, InputEvent event)
         {
             CutPolygonCADTool ctxt = context.getOwner();
 
             boolean loopbackFlag =
                 context.getState().getName().equals(
                     CutPolygon.FirstPoint.getName());
 
             if (loopbackFlag == false)
             {
                 (context.getState()).Exit(context);
             }
 
             context.clearState();
             try
             {
                 ctxt.throwPointException(PluginServices.getText(this,"incorrect_point"), pointX, pointY);
             }
             finally
             {
                 context.setState(CutPolygon.FirstPoint);
 
                 if (loopbackFlag == false)
                 {
                     (context.getState()).Entry(context);
                 }
 
             }
             return;
         }
 
         protected void removePoint(CutPolygonCADToolContext context, InputEvent event, int numPoints)
         {
             CutPolygonCADTool ctxt = context.getOwner();
 
             boolean loopbackFlag =
                 context.getState().getName().equals(
                     CutPolygon.FirstPoint.getName());
 
             if (loopbackFlag == false)
             {
                 (context.getState()).Exit(context);
             }
 
             context.clearState();
             try
             {
                 ctxt.throwNoPointsException(PluginServices.getText(this,"no_points"));
             }
             finally
             {
                 context.setState(CutPolygon.FirstPoint);
 
                 if (loopbackFlag == false)
                 {
                     (context.getState()).Entry(context);
                 }
 
             }
             return;
         }
 
         protected void addOption(CutPolygonCADToolContext context, String s)
         {
             CutPolygonCADTool ctxt = context.getOwner();
 
             if (s.equals("C")||s.equals("c")||s.equals(PluginServices.getText(this,"cancel")))
             {
                 boolean loopbackFlag =
                     context.getState().getName().equals(
                         CutPolygon.FirstPoint.getName());
 
                 if (loopbackFlag == false)
                 {
                     (context.getState()).Exit(context);
                 }
 
                 context.clearState();
                 try
                 {
                     ctxt.clear();
                 }
                 finally
                 {
                     context.setState(CutPolygon.FirstPoint);
 
                     if (loopbackFlag == false)
                     {
                         (context.getState()).Entry(context);
                     }
 
                 }
             }
             else
             {
                 CutPolygonCADToolState endState = context.getState();
 
                 context.clearState();
                 try
                 {
                     ctxt.throwOptionException(PluginServices.getText(this,"incorrect_option"), s);
                 }
                 finally
                 {
                     context.setState(endState);
                 }
             }
 
             return;
         }
 
     //-----------------------------------------------------------
     // Inner classse.
     //
 
 
         private static final class CutPolygon_FirstPoint
             extends CutPolygon_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private CutPolygon_FirstPoint(String name, int id)
             {
                 super (name, id);
             }
 
             protected void Entry(CutPolygonCADToolContext context)
             {
                 CutPolygonCADTool ctxt = context.getOwner();
 
                 ctxt.setQuestion(PluginServices.getText(this,"redigitaliza_insert_first_point"));
                 ctxt.setDescription(new String[]{"cancel"});
                 return;
             }
 
             protected void addPoint(CutPolygonCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 CutPolygonCADTool ctxt = context.getOwner();
 
                 if (ctxt.pointInsideFeature(pointX,pointY))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
 		    context.setState(CutPolygon.SecondPoint);
                         (context.getState()).Entry(context);
 
                 }
                 else
                 {
                     CutPolygonCADToolState endState = context.getState();
 
                     context.clearState();
                     try
                     {
                         ctxt.throwPointException(PluginServices.getText(this,"redigitaliza_incorrect_point"), pointX, pointY);
                         ctxt.setQuestion(PluginServices.getText(this,"redigitaliza_insert_first_point"));
                     }
                     finally
                     {
                         context.setState(endState);
                     }
                 }
 
                 return;
             }
 
         //-------------------------------------------------------
         // Member data.
         //
         }
 
         private static final class CutPolygon_SecondPoint
             extends CutPolygon_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private CutPolygon_SecondPoint(String name, int id)
             {
                 super (name, id);
             }
 
             protected void Entry(CutPolygonCADToolContext context) {
 
             	CutPolygonCADTool tool = context.getOwner();
             	tool.setDescription(new String[]{"cancel"});
 		boolean deleteButton3 = CADStatus.getCADStatus()
 			.isDeleteButtonActivated();
 		if (deleteButton3) {
 		    tool.setQuestion(PluginServices.getText(this,
 			    "redigitaliza_insert_second_point_end"));
 		} else {
 		    tool.setQuestion(PluginServices.getText(this,
 			    "redigitaliza_insert_second_point"));
 		}
 
             }
 
             protected void addPoint(CutPolygonCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 CutPolygonCADTool ctxt = context.getOwner();
 
                 if (ctxt.secondPointInsideFeature(pointX,pointY))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                         context.setState(CutPolygon.NextPoint);
                         (context.getState()).Entry(context);
                 }
                 else
                 {
                     CutPolygonCADToolState endState = context.getState();
 
                     context.clearState();
                     try
                     {
                         ctxt.throwPointException(PluginServices.getText(this,"redigitaliza_incorrect_point"), pointX, pointY);
 
                     }
                     finally
                     {
                         context.setState(endState);
                     }
                 }
 
                 return;
             }
 
             protected void removePoint(CutPolygonCADToolContext context, InputEvent event, int numPoints)
             {
                 CutPolygonCADTool ctxt = context.getOwner();
 
 
                 (context.getState()).Exit(context);
                 context.clearState();
                 try
                 {
                     ctxt.removeFirstPoint(event);
                 }
                 finally
                 {
                     context.setState(CutPolygon.FirstPoint);
                     (context.getState()).Entry(context);
                 }
                 return;
             }
 
         //-------------------------------------------------------
         // Member data.
         //
         }
 
         private static final class CutPolygon_NextPoint
             extends CutPolygon_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private CutPolygon_NextPoint(String name, int id)
             {
                 super (name, id);
             }
 
             protected void Entry(CutPolygonCADToolContext context) {
 
             	CutPolygonCADTool tool = context.getOwner();
             	tool.setDescription(new String[]{"cancel", "terminate", "change_base_geom"});
 		boolean deleteButton3 = CADStatus.getCADStatus()
 			.isDeleteButtonActivated();
 		if (deleteButton3) {
 		    tool.setQuestion(PluginServices.getText(this,
 			    "redigitaliza_poligono_insert_other_point_del"));
 		} else {
 		    tool.setQuestion(PluginServices.getText(this,
 			    "redigitaliza_poligono_insert_other_point"));
 		}
             }
 
             protected void addOption(CutPolygonCADToolContext context, String s)
             {
                 CutPolygonCADTool ctxt = context.getOwner();
 
                 if (s.equals("espacio") || s.equals(PluginServices.getText(this, "terminate")))//&& ctxt.checksOnEditionSinContinuidad(ctxt.getGeometriaResultante(), ctxt.getCurrentGeoid())))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try {
                         ctxt.saveChanges();
                         ctxt.clear();
			ctxt.fireEndGeometry(CutPolygonCADTool.CUT_END);
                     }
                     finally   {
                         context.setState(CutPolygon.FirstPoint);
                         (context.getState()).Entry(context);
                     }
                 } else if (s.equals("g")||s.equals("G")) {
 
                     // No actions.
                 } else if (s.equals("tab") || s.equals(PluginServices.getText(this, "change_base_geom"))) {
 
                     CutPolygonCADToolState endState = context.getState();
                     context.clearState();
 
                     try {
 
                         ctxt.changePieceOfGeometry();
 
                     } finally{
 
                         context.setState(endState);
                     }
                 }  else {
                     super.addOption(context, s);
                 }
 
                 return;
             }
 
             protected void addPoint(CutPolygonCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 CutPolygonCADTool ctxt = context.getOwner();
 
                 CutPolygonCADToolState endState = context.getState();
 
                 context.clearState();
                 try
                 {
                     ctxt.addPoint(pointX, pointY, event);
                 }
                 finally
                 {
                     context.setState(endState);
                 }
                 return;
             }
 
             protected void removePoint(CutPolygonCADToolContext context, InputEvent event, int numPoints)
             {
                 CutPolygonCADTool ctxt = context.getOwner();
 
                 if (numPoints>0)
                 {
                     CutPolygonCADToolState endState = context.getState();
 
                     context.clearState();
                     try
                     {
                         ctxt.removePoint(event);
                     }
                     finally
                     {
                         context.setState(endState);
                     }
                 }
                 else if (numPoints==0)
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                         ctxt.removeSecondPoint(event);
                     }
                     finally
                     {
                         context.setState(CutPolygon.SecondPoint);
                         (context.getState()).Entry(context);
                     }
                 }                else
                 {
                     super.removePoint(context, event, numPoints);
                 }
 
                 return;
             }
 
         //-------------------------------------------------------
         // Member data.
         //
         }
 
     //-----------------------------------------------------------
     // Member data.
     //
     }
 }
