 //$HeadURL$
 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2008 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 package org.deegree.igeo.views.swing;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.JInternalFrame;
 import javax.swing.JPopupMenu;
 import javax.swing.border.Border;
 import javax.swing.border.EtchedBorder;
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 
 import org.deegree.igeo.config.InnerFrameViewFormType;
 import org.deegree.igeo.config.ViewFormType;
 import org.deegree.igeo.modules.IModule;
 import org.deegree.igeo.views.ComponentPosition;
 import org.deegree.igeo.views.IView;
 import org.deegree.igeo.views.swing.util.IconRegistry;
 import org.deegree.igeo.views.swing.util.PopUpRegister;
 
 /**
  * 
  * 
  * 
  * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version. $Revision$, $Date$
  */
 public class DefaultInnerFrame extends JInternalFrame implements IView<Container> {
 
     private static final long serialVersionUID = -2576901670691046056L;
 
     protected IModule<Container> owner;
 
     private JPopupMenu popup;
 
     /**
      * 
      * 
      */
     public DefaultInnerFrame() {
        super( "", true, true );
         addComponentListener( new DIFComponentListener() );
         addInternalFrameListener( new DIFInternalFrameListener() );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.views.IView#init(org.deegree.client.presenter.state.ComponentStateAdapter,
      * org.deegree.client.configuration.ViewForm)
      */
     public void init( ViewFormType viewForm )
                             throws Exception {
         setResizable( ( (InnerFrameViewFormType) viewForm.get_AbstractViewForm().getValue() ).isResizeable() );
         setMaximizable( true );
         setIconifiable( true );
         Border outsideBorder = BorderFactory.createEtchedBorder( EtchedBorder.RAISED );
         Border insideBorder = BorderFactory.createLineBorder( new Color( 197, 197, 220 ), 3 );
         setBorder( BorderFactory.createCompoundBorder( outsideBorder, insideBorder ) );
         // TODO
         // each internal frame should have its own icon
         setFrameIcon( IconRegistry.getIcon( "layers.png" ) );
         ComponentPosition compPosAdapter = this.owner.getComponentPositionAdapter();
         if ( compPosAdapter.hasWindow() ) {
             setSize( compPosAdapter.getWindowWidth(), compPosAdapter.getWindowHeight() );
             setLocation( compPosAdapter.getWindowLeft(), compPosAdapter.getWindowTop() );
         }
         setTitle( this.owner.getName() );
         if ( getParent() == null ) {
             // parent is null view has not been yet added to its parent. This is the case if a
             // module's view is 'closed' when loading a project
             Container con = (Container) owner.getGUIContainer();
             if ( con != null ) {
                 con.add( this );
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.views.IView#registerModule(org.deegree.client.application.modules.IModule)
      */
     public void registerModule( IModule<Container> module ) {
         this.owner = module;
         ControlElement popUpController = PopUpRegister.registerPopups( module.getApplicationContainer(), this, owner,
                                                                        null, new PopupListener() );
         popup = (PopUpMenu) popUpController.getView();
     }
 
     /**
      * updates a DefaultInnerFrame by invoking {@link #repaint()}
      */
     public void update() {
         repaint();
     }
 
     /**
      * Override this method if a PopUpContrller is required!
      */
     public ControlElement getPopUpController() {
         return null;
     }
 
     // /////////////////////////////////////////////////////////////////////////////////
     // inner classe //
     // /////////////////////////////////////////////////////////////////////////////////
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     class PopupListener extends MouseAdapter {
 
         @Override
         public void mousePressed( MouseEvent e ) {
             maybeShowPopup( e );
         }
 
         @Override
         public void mouseReleased( MouseEvent e ) {
             maybeShowPopup( e );
         }
 
         private void maybeShowPopup( MouseEvent e ) {
             if ( e.isPopupTrigger() ) {
                 popup.show( e.getComponent(), e.getX(), e.getY() );
             }
         }
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     private class DIFComponentListener extends ComponentAdapter {
 
         @Override
         public void componentMoved( ComponentEvent e ) {
             ComponentPosition compPos = owner.getComponentPositionAdapter();
             if ( compPos.hasWindow() ) {
                 compPos.setWindowPosition( (int) e.getComponent().getLocation().getX(),
                                            (int) e.getComponent().getLocation().getY() );
             }
             repaint();
         }
 
         @Override
         public void componentResized( ComponentEvent e ) {
             if ( owner != null ) {
                 ComponentPosition compPos = owner.getComponentPositionAdapter();
                 if ( compPos.hasWindow() && e.getComponent().getSize().getWidth() > 10
                      && e.getComponent().getSize().getHeight() > 10 ) {
                     compPos.setWindowSize( (int) e.getComponent().getSize().getWidth(),
                                            (int) e.getComponent().getSize().getHeight() );
                 }
             }
         }
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     private class DIFInternalFrameListener extends InternalFrameAdapter {
 
         @Override
         public void internalFrameActivated( InternalFrameEvent event ) {
             owner.getComponentStateAdapter().setActive( true );
         }
 
         @Override
         public void internalFrameClosed( InternalFrameEvent event ) {
             owner.getComponentStateAdapter().setClosed( true );
             owner.clear();
         }
 
         @Override
         public void internalFrameDeactivated( InternalFrameEvent event ) {
             owner.getComponentStateAdapter().setActive( false );
         }
 
         @Override
         public void internalFrameDeiconified( InternalFrameEvent event ) {
             // means minimized
             owner.getComponentStateAdapter().setMinimized( true );
         }
 
         @Override
         public void internalFrameIconified( InternalFrameEvent event ) {
             // means 'normal' state
             owner.getComponentStateAdapter().setMinimized( false );
         }
 
         @Override
         public void internalFrameOpened( InternalFrameEvent event ) {
             owner.getComponentStateAdapter().setClosed( false );
         }
     }
 
 }
