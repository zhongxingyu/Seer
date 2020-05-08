 
 //
 // Vicente Caballero Navarro
 
 
 package com.iver.cit.gvsig.gui.cad.tools.smc;
 
 //import com.iver.cit.gvsig.ActivateFormsExtension;
 //import com.iver.cit.gvsig.OpenFormsExtension;
 import java.awt.event.InputEvent;
 import java.util.prefs.Preferences;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.gui.cad.tools.MultiPolylineCADTool;
 
 public final class MultiPolylineCADToolContext
     extends statemap.FSMContext
 {
 //---------------------------------------------------------------
 // Member methods.
 //
 
 	private static Preferences prefs = Preferences.userRoot().node( "cadtooladapter" );
 
     public MultiPolylineCADToolContext(MultiPolylineCADTool owner)
     {
         super();
 
         _owner = owner;
         switch (_owner.getPointsCount()) {
         case 0 :
         	setState(MultiLinea.FirstPoint);
         	MultiLinea.FirstPoint.Entry(this);
         	break;
         case 1 :
         	setState(MultiLinea.SecondPoint);
         	MultiLinea.SecondPoint.Entry(this);
         	break;
         default :
         	setState(MultiLinea.NextPoint);
     		MultiLinea.NextPoint.Entry(this);
     		break;
         }
 
 
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
 
     public void addValue(double d)
     {
         _transition = "addValue";
         getState().addValue(this, d);
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
 
     public MultiLineaCADToolState getState()
         throws statemap.StateUndefinedException
     {
         if (_state == null)
         {
             throw(
                 new statemap.StateUndefinedException());
         }
 
         return ((MultiLineaCADToolState) _state);
     }
 
     protected MultiPolylineCADTool getOwner()
     {
         return (_owner);
     }
 
 //---------------------------------------------------------------
 // Member data.
 //
 
     transient private MultiPolylineCADTool _owner;
 
 //---------------------------------------------------------------
 // Inner classes.
 //
 
     public static abstract class MultiLineaCADToolState
         extends statemap.State
     {
     //-----------------------------------------------------------
     // Member methods.
     //
 
         protected MultiLineaCADToolState(String name, int id)
         {
             super (name, id);
         }
 
         protected void Entry(MultiPolylineCADToolContext context) {
         	context.getOwner().setDescription(getDescription());
             }
         protected void Exit(MultiPolylineCADToolContext context) {}
 
         protected abstract String[] getDescription();
 
         protected void addOption(MultiPolylineCADToolContext context, String s)
         {
             Default(context);
         }
 
         protected void addPoint(MultiPolylineCADToolContext context, double pointX, double pointY, InputEvent event)
         {
             Default(context);
         }
 
         protected void addValue(MultiPolylineCADToolContext context, double d)
         {
             Default(context);
         }
 
         protected void removePoint(MultiPolylineCADToolContext context, InputEvent event, int numPoints)
         {
             Default(context);
         }
 
         protected void Default(MultiPolylineCADToolContext context)
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
 
     /* package */ static abstract class MultiLinea
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
         /* package */ static MultiLinea_Default.MultiLinea_FirstPoint FirstPoint;
         /* package */ static MultiLinea_Default.MultiLinea_SecondPoint SecondPoint;
         /* package */ static MultiLinea_Default.MultiLinea_NextPoint NextPoint;
         private static MultiLinea_Default Default;
 
         static
         {
             FirstPoint = new MultiLinea_Default.MultiLinea_FirstPoint("MultiLinea.FirstPoint", 0);
             SecondPoint = new MultiLinea_Default.MultiLinea_SecondPoint("MultiLinea.SecondPoint", 1);
             NextPoint = new MultiLinea_Default.MultiLinea_NextPoint("MultiLinea.NextPoint", 2);
             Default = new MultiLinea_Default("MultiLinea.Default", -1);
         }
 
     }
 
     protected static class MultiLinea_Default
         extends MultiLineaCADToolState
     {
     //-----------------------------------------------------------
     // Member methods.
     //
 
         protected MultiLinea_Default(String name, int id)
         {
             super (name, id);
         }
 
         protected void Entry(MultiPolygonCADToolContext context) {
         	context.getOwner().setDescription(getDescription());
         }
 
         protected String[] getDescription() {
         	return new String[]{"cancelar"};
         }
 
         protected void addOption(MultiPolylineCADToolContext context, String s)
         {
             MultiPolylineCADTool ctxt = context.getOwner();
 
             if (s.equals("espacio")|| s.equals(PluginServices.getText(this, "terminate")))
             {
                 MultiLineaCADToolState endState = context.getState();
 
                 context.clearState();
                 try
                 {
                     ctxt.throwInvalidGeometryException(PluginServices.getText(this,"two_points_at_least"));
                 }
                 finally
                 {
                     context.setState(endState);
                 }
             }
             else if (s.equals("C")||s.equals("c")||s.equals(PluginServices.getText(this,"cancel")))
             {
                 boolean loopbackFlag =
                     context.getState().getName().equals(
                         MultiLinea.FirstPoint.getName());
 
                 if (loopbackFlag == false)
                 {
                     (context.getState()).Exit(context);
                 }
 
                 context.clearState();
                 try
                 {
                     ctxt.cancel();
                 }
                 finally
                 {
                     context.setState(MultiLinea.FirstPoint);
 
                     if (loopbackFlag == false)
                     {
                         (context.getState()).Entry(context);
                     }
 
                 }
             }
             else
             {
                 MultiLineaCADToolState endState = context.getState();
 
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
 
         protected void addValue(MultiPolylineCADToolContext context, double d)
         {
             MultiPolylineCADTool ctxt = context.getOwner();
 
             boolean loopbackFlag =
                 context.getState().getName().equals(
                     MultiLinea.FirstPoint.getName());
 
             if (loopbackFlag == false)
             {
                 (context.getState()).Exit(context);
             }
 
             context.clearState();
             try
             {
                 ctxt.throwValueException(PluginServices.getText(this,"incorrect_value"), d);
             }
             finally
             {
                 context.setState(MultiLinea.FirstPoint);
 
                 if (loopbackFlag == false)
                 {
                     (context.getState()).Entry(context);
                 }
 
             }
             return;
         }
 
         protected void addPoint(MultiPolylineCADToolContext context, double pointX, double pointY, InputEvent event)
         {
             MultiPolylineCADTool ctxt = context.getOwner();
 
             boolean loopbackFlag =
                 context.getState().getName().equals(
                     MultiLinea.FirstPoint.getName());
 
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
                 context.setState(MultiLinea.FirstPoint);
 
                 if (loopbackFlag == false)
                 {
                     (context.getState()).Entry(context);
                 }
 
             }
             return;
         }
 
         protected void removePoint(MultiPolylineCADToolContext context, InputEvent event, int numPoints)
         {
             MultiPolylineCADTool ctxt = context.getOwner();
 
             boolean loopbackFlag =
                 context.getState().getName().equals(
                     MultiLinea.FirstPoint.getName());
 
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
                 context.setState(MultiLinea.FirstPoint);
 
                 if (loopbackFlag == false)
                 {
                     (context.getState()).Entry(context);
                 }
 
             }
             return;
         }
 
     //-----------------------------------------------------------
     // Inner classse.
     //
 
 
         private static final class MultiLinea_FirstPoint
             extends MultiLinea_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private MultiLinea_FirstPoint(String name, int id)
             {
                 super (name, id);
             }
 
             protected void Entry(MultiPolylineCADToolContext context)
             {
                 MultiPolylineCADTool ctxt = context.getOwner();
 
                 ctxt.setQuestion(PluginServices.getText(this,"insert_first_point"));
                 ctxt.setDescription(getDescription());
                 return;
             }
 
             protected void addPoint(MultiPolylineCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 MultiPolylineCADTool ctxt = context.getOwner();
 
 
                 (context.getState()).Exit(context);
                 context.clearState();
                 try
                 {
                     ctxt.setQuestion(PluginServices.getText(this,"insert_next_point"));
                     ctxt.addPoint(pointX, pointY, event);
                 }
                 finally
                 {
                     context.setState(MultiLinea.SecondPoint);
                     (context.getState()).Entry(context);
                 }
                 return;
             }
 
         //-------------------------------------------------------
         // Member data.
         //
         }
 
         private static final class MultiLinea_SecondPoint
             extends MultiLinea_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private MultiLinea_SecondPoint(String name, int id)
             {
                 super (name, id);
             }
 
             protected String[] getDescription() {
             	return new String[]{"terminate", "next", "cancel"};
             }
 
             protected void addPoint(MultiPolylineCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 MultiPolylineCADTool ctxt = context.getOwner();
 
 
                 (context.getState()).Exit(context);
                 context.clearState();
                 try
                 {
                 	boolean deleteButton3 = prefs.getBoolean("isDeleteButton3", true);
                 	if (deleteButton3) {
                 		ctxt.setQuestion(PluginServices.getText(this,"insert_next_point_or_line_del"));
                 	} else {
                 		ctxt.setQuestion(PluginServices.getText(this,"insert_next_point_or_line"));
                 	}
                     ctxt.addPoint(pointX, pointY, event);
                 }
                 finally
                 {
                     context.setState(MultiLinea.NextPoint);
                     (context.getState()).Entry(context);
                 }
                 return;
             }
 
             protected void removePoint(MultiPolylineCADToolContext context, InputEvent event, int numPoints)
             {
                 MultiPolylineCADTool ctxt = context.getOwner();
 
 
                 (context.getState()).Exit(context);
                 context.clearState();
                 try
                 {
                     ctxt.setQuestion(PluginServices.getText(this,"insert_first_point"));
                     ctxt.removePoint(event);
                 }
                 finally
                 {
                     context.setState(MultiLinea.FirstPoint);
                     (context.getState()).Entry(context);
                 }
                 return;
             }
 
         //-------------------------------------------------------
         // Member data.
         //
         }
 
         private static final class MultiLinea_NextPoint
             extends MultiLinea_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private MultiLinea_NextPoint(String name, int id)
             {
                 super (name, id);
             }
 
             protected String[] getDescription() {
             	return new String[]{"terminate", "next", "cancel"};
             }
 
             protected void addOption(MultiPolylineCADToolContext context, String s)
             {
                 MultiPolylineCADTool ctxt = context.getOwner();
 
                 if (s.equals("espacio")|| s.equals(PluginServices.getText(this, "terminate"))) //&& ctxt.checksOnInsertion(ctxt.getCurrentGeom()))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                         ctxt.saveTempGeometry();
 //                        ctxt.updateFormConstants();
 //                        ctxt.openForm();
                     }
                     finally
                     {
                         context.setState(MultiLinea.FirstPoint);
                         (context.getState()).Entry(context);
 //                        if (ActivateFormsExtension.getActivated()) {
 //                        	VectorialLayerEdited vle=ctxt.getVLE();
 //                        	OpenFormsExtension.openForm(vle);
 //                    	}
                         ctxt.clear();
                     }
                 }
                else if (s.equals("espacio")|| s.equals(PluginServices.getText(this, "terminate")))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                         ctxt.cancel();
                     }
                     finally
                     {
                         context.setState(MultiLinea.FirstPoint);
                         (context.getState()).Entry(context);
                     }
                 }
                 else if (s.equals("tab")|| s.equals(PluginServices.getText(this, "next")))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                         ctxt.saveTempGeometry();
                         ctxt.clearPoints();
                         ctxt.setQuestion(PluginServices.getText(this,"insert_first_point"));
                     }
                     finally
                     {
                         context.setState(MultiLinea.FirstPoint);
                         (context.getState()).Entry(context);
                     }
                 }                else
                 {
                     super.addOption(context, s);
                 }
 
                 return;
             }
 
             protected void addPoint(MultiPolylineCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 MultiPolylineCADTool ctxt = context.getOwner();
 
                 MultiLineaCADToolState endState = context.getState();
 
                 context.clearState();
                 try
                 {
                 	boolean deleteButton3 = prefs.getBoolean("isDeleteButton3", true);
                 	if (deleteButton3) {
                 		ctxt.setQuestion(PluginServices.getText(this,"insert_next_point_or_line_del"));
                 	} else {
                 		ctxt.setQuestion(PluginServices.getText(this,"insert_next_point_or_line"));
                 	}
                     ctxt.addPoint(pointX, pointY, event);
                 }
                 finally
                 {
                     context.setState(endState);
                 }
                 return;
             }
 
             protected void removePoint(MultiPolylineCADToolContext context, InputEvent event, int numPoints)
             {
                 MultiPolylineCADTool ctxt = context.getOwner();
 
                 if (numPoints>2)
                 {
                     MultiLineaCADToolState endState = context.getState();
 
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
                 else if (numPoints==2)
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                         ctxt.removePoint(event);
                     }
                     finally
                     {
                         context.setState(MultiLinea.SecondPoint);
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
