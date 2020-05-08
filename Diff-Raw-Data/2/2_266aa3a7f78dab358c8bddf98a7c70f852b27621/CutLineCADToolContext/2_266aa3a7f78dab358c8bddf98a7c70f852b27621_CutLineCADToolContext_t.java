 /*
  * Copyright 2008 Deputacin Provincial de A Corua
  * Copyright 2009 Deputacin Provincial de Pontevedra
  * Copyright 2010 CartoLab, Universidad de A Corua
  *
  * This file is part of openCADTools, developed by the Cartography
  * Engineering Laboratory of the University of A Corua (CartoLab).
  * http://www.cartolab.es
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  */
 
 package com.iver.cit.gvsig.gui.cad.tools.smc;
 
 import com.iver.cit.gvsig.gui.cad.tools.CutLineCADTool;
 import java.awt.event.InputEvent;
 import com.iver.andami.PluginServices;
 
 /**
  * @author Jos Ignacio Lamas Fonte [LBD]
  * @author Nacho Varela [Cartolab]
  * @author Pablo Sanxiao [CartoLab]
  */
 
 public final class CutLineCADToolContext
     extends statemap.FSMContext
 {
 //---------------------------------------------------------------
 // Member methods.
 //
 
     public CutLineCADToolContext(CutLineCADTool owner)
     {
         super();
 
         _owner = owner;
         setState(CutLine.SetCutPoint);
         CutLine.SetCutPoint.Entry(this);
     }
 
     public void addOption(String s) {
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
 
     public void removeCutPoint(InputEvent event)
     {
         _transition = "removeCutPoint";
         getState().removeCutPoint(this, event);
         _transition = "";
         return;
     }
 
     public CutLineCADToolState getState()
         throws statemap.StateUndefinedException
     {
         if (_state == null)
         {
             throw(
                 new statemap.StateUndefinedException());
         }
 
         return ((CutLineCADToolState) _state);
     }
 
     protected CutLineCADTool getOwner()
     {
         return (_owner);
     }
 
 //---------------------------------------------------------------
 // Member data.
 //
 
     transient private CutLineCADTool _owner;
 
 //---------------------------------------------------------------
 // Inner classes.
 //
 
     public static abstract class CutLineCADToolState
         extends statemap.State
     {
     //-----------------------------------------------------------
     // Member methods.
     //
 
         protected CutLineCADToolState(String name, int id)
         {
             super (name, id);
         }
 
         protected void Entry(CutLineCADToolContext context) {}
         protected void Exit(CutLineCADToolContext context) {}
 
         protected void addOption(CutLineCADToolContext context, String s)
         {
             Default(context);
         }
 
         protected void addPoint(CutLineCADToolContext context, double pointX, double pointY, InputEvent event)
         {
             Default(context);
         }
 
         protected void removeCutPoint(CutLineCADToolContext context, InputEvent event)
         {
             Default(context);
         }
 
         protected void Default(CutLineCADToolContext context)
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
 
     /* package */ static abstract class CutLine
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
         /* package */ static CutLine_Default.CutLine_SetCutPoint SetCutPoint;
         /* package */ static CutLine_Default.CutLine_CutPointSet CutPointSet;
         private static CutLine_Default Default;
 
         static
         {
             SetCutPoint = new CutLine_Default.CutLine_SetCutPoint("CutLine.SetCutPoint", 0);
             CutPointSet = new CutLine_Default.CutLine_CutPointSet("CutLine.CutPointSet", 1);
             Default = new CutLine_Default("CutLine.Default", -1);
         }
 
     }
 
     protected static class CutLine_Default
         extends CutLineCADToolState
     {
     //-----------------------------------------------------------
     // Member methods.
     //
 
         protected CutLine_Default(String name, int id)
         {
             super (name, id);
         }
 
     //-----------------------------------------------------------
     // Inner classse.
     //
 
 
         private static final class CutLine_SetCutPoint
             extends CutLine_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private CutLine_SetCutPoint(String name, int id)
             {
                 super (name, id);
             }
 
             protected void Entry(CutLineCADToolContext context)
             {
                 CutLineCADTool ctxt = context.getOwner();
 
                 ctxt.setQuestion(PluginServices.getText(this,"cut_insert_intersection_point"));
                 ctxt.setDescription(new String[0]); //no popup menu
                 return;
             }
 
             protected void addPoint(CutLineCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 CutLineCADTool ctxt = context.getOwner();
 
                 if (ctxt.pointInsideFeature(pointX,pointY))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                         ctxt.setQuestion(PluginServices.getText(this,"cut_intersection_point"));
                     }
                     finally
                     {
                         context.setState(CutLine.CutPointSet);
                         (context.getState()).Entry(context);
                     }
                 }
                 else
                 {
                     CutLineCADToolState endState = context.getState();
 
                     context.clearState();
                     try
                     {
                         ctxt.throwPointException(PluginServices.getText(this,"redigitaliza_incorrect_point"), pointX, pointY);
                         ctxt.setQuestion(PluginServices.getText(this,"cut_insert_intersection_point"));
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
 
         private static final class CutLine_CutPointSet
             extends CutLine_Default
         {
         //-------------------------------------------------------
         // Member methods.
         //
 
             private CutLine_CutPointSet(String name, int id)
             {
                 super (name, id);
             }
 
             protected void Entry(CutLineCADToolContext context)
             {
                 CutLineCADTool ctxt = context.getOwner();
 
                ctxt.setQuestion(PluginServices.getText(this,"cut_intersection_point"));
                 ctxt.setDescription(new String[]{"cancel"});
                 return;
             }
 
             protected void addOption(CutLineCADToolContext context, String s)
             {
                 CutLineCADTool ctxt = context.getOwner();
 
                 if (s.equals("tab"))
                 {
                     CutLineCADToolState endState = context.getState();
 
                     context.clearState();
                     try
                     {
                         ctxt.changePieceOfGeometry();
                     }
                     finally
                     {
                         context.setState(endState);
                     }
                 }
                 else if (s.equals("espacio"))//&& ctxt.checksOnEditionSinContinuidad(ctxt.getGeometriaResultante(), ctxt.getCurrentGeoid())))
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                     	ctxt.saveChanges();
                         ctxt.setQuestion(PluginServices.getText(this,"cut_insert_intersection_point"));
                         ctxt.clear();
                     }
                     finally
                     {
                         context.setState(CutLine.SetCutPoint);
                         (context.getState()).Entry(context);
                     }
                 }                else
                 {
                     super.addOption(context, s);
                 }
 
                 return;
             }
 
             protected void addPoint(CutLineCADToolContext context, double pointX, double pointY, InputEvent event)
             {
                 CutLineCADTool ctxt = context.getOwner();
 
                 if (ctxt.pointInsideFeature(pointX,pointY))
                 {
                     CutLineCADToolState endState = context.getState();
 
                     context.clearState();
                     try
                     {
                         ctxt.setQuestion(PluginServices.getText(this,"cut_intersection_point"));
                     }
                     finally
                     {
                         context.setState(endState);
                     }
                 }
                 else
                 {
 
                     (context.getState()).Exit(context);
                     context.clearState();
                     try
                     {
                         ctxt.clear();
                         ctxt.throwPointException(PluginServices.getText(this,"redigitaliza_incorrect_point"), pointX, pointY);
                         ctxt.setQuestion(PluginServices.getText(this,"cut_insert_intersection_point"));
                     }
                     finally
                     {
                         context.setState(CutLine.SetCutPoint);
                         (context.getState()).Entry(context);
                     }
                 }
 
                 return;
             }
 
             protected void removeCutPoint(CutLineCADToolContext context, InputEvent event)
             {
                 CutLineCADTool ctxt = context.getOwner();
 
 
                 (context.getState()).Exit(context);
                 context.clearState();
                 try
                 {
                     ctxt.setQuestion(PluginServices.getText(this,"cut_insert_intersection_point"));
                     ctxt.clear();
                 }
                 finally
                 {
                     context.setState(CutLine.SetCutPoint);
                     (context.getState()).Entry(context);
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
