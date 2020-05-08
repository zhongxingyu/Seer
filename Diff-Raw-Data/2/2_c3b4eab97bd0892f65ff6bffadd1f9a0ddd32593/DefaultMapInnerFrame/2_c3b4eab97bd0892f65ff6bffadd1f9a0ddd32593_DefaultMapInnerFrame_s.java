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
 
 package org.deegree.igeo.views.swing.map;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.config.ViewFormType;
 import org.deegree.igeo.views.swing.DefaultInnerFrame;
 import org.deegree.model.Identifier;
 
 /**
  * <code>DefaultMapInnerFrame</code>
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class DefaultMapInnerFrame extends DefaultInnerFrame implements ChangeListener {
 
     private static final long serialVersionUID = 2298633513328447143L;
 
     private DefaultMapComponent dmc;
     
     /**
      * 
      */
     public DefaultMapInnerFrame() {
        setClosable( true ); 
     }
     
 
     @Override
     public void init( ViewFormType viewForm )
                             throws Exception {
         super.init( viewForm );
         int sz = owner.getApplicationContainer().getMapModelCollection().getMapModels().size();
         if ( sz > 1 ) {
             setClosable( true );
         }
 
         // use the DefaultMapComponent to show the map!
         dmc = new DefaultMapComponent();
         dmc.registerModule( this.owner );
         dmc.init( viewForm );
 
         Container contentPane = getContentPane();
         contentPane.setLayout( new BorderLayout() );
         contentPane.add( dmc, BorderLayout.CENTER );
         addComponentListener( new DMIFComponentListener() );
         addInternalFrameListener( new DMInternalFrameListener() );
     }
 
     @Override
     public void setBounds( int x, int y, int width, int height ) {
         super.setBounds( x, y, width, height );
     }
 
     @Override
     public void setSize( int width, int height ) {
         super.setSize( width, height );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.client.presenter.ChangeListener#valueChanged(org.deegree.client.presenter.ValueChangedEvent)
      */
     public void valueChanged( ValueChangedEvent event ) {
         dmc.valueChanged( event );
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
     private class DMIFComponentListener extends ComponentAdapter {
 
         @Override
         public void componentHidden( ComponentEvent arg0 ) {
             owner.getApplicationContainer().removeModule( owner );            
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
     private class DMInternalFrameListener extends InternalFrameAdapter {
         
         @Override
         public void internalFrameActivated( InternalFrameEvent event ) {
             ApplicationContainer<Container> appCont = owner.getApplicationContainer();
             String mmId = owner.getInitParameter( "assignedMapModel" );
             appCont.setActiveMapModel( appCont.getMapModel( new Identifier( mmId ) ) );
         }
     }
 
 }
