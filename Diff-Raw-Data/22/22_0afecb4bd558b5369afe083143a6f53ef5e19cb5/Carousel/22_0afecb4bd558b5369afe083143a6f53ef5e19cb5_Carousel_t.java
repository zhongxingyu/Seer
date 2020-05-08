 /******************************************************************************* 
 * Copyright (c) 2010 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/ 
 package org.eclipse.rap.widget.carousel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.browser.BrowserFunction;
 import org.eclipse.swt.browser.ProgressEvent;
 import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 
 
public class Carousel extends Composite {
   
   private static final String URL = "/carousel/carousel.html";
   private final static String CLICK_FUNCTION = "itemClicked";
   private boolean loaded;
   private List<CarouselItem> queue;
   private List<CarouselItem> items;
   private boolean wantsToStart;
  private Browser browser;
 
   public Carousel( Composite parent ) {
     super( parent, SWT.NONE );
    super.setLayout( new FillLayout() );
    browser = new Browser( this, SWT.NONE );
    browser.setUrl( URL );
    browser.addProgressListener( new ProgressListener() {
       public void completed( ProgressEvent event ) {
         loaded = true;
         createBrowserFunctions();
         flushQueue();
         if( wantsToStart ) {
           render();
         }
       }     
       public void changed( ProgressEvent event ) {
       }
     } );
   }
 
   private void createBrowserFunctions() {
    new BrowserFunction( browser, CLICK_FUNCTION ) {
       @Override
       public Object function( Object[] arguments ) {
         int positionOfItem = ( ( Double )arguments[ 0 ] ).intValue();
         CarouselItem carouselItem = items.get( positionOfItem );
         carouselItem.fireClickEvent();
         return null;
       }
     };
   }
 
   public void internalRegisterItem( CarouselItem carouselItem ) {
     internalAddItem( carouselItem );
     if( loaded ) {
       String resourceName = carouselItem.getIconResourceName();
       int itemNumber = items.indexOf( carouselItem );
       String script = "addListItem('" + resourceName + "', " + itemNumber + ");";
      browser.evaluate( script );
     } else {
       addItemToQueue( carouselItem );
     }
   }
 
   private void internalAddItem( CarouselItem carouselItem ) {
     if( items == null ) {
       items = new ArrayList<CarouselItem>();
     }
     if( !items.contains( carouselItem ) ) {
       items.add( carouselItem );
     }
   }
 
   private void addItemToQueue( CarouselItem carouselItem ) {
     if( queue == null ) {
       queue = new ArrayList<CarouselItem>();
     }
     queue.add( carouselItem );
   }
 
   private void flushQueue() {
     if( queue != null ) {
       for( CarouselItem item : queue ) {
         internalRegisterItem( item );
       }
     }
   }
   
   /**
    * Renders the carousel within the browser when the website
    * was loaded completely.
    */
   public void render() {
     if( loaded ) {
      browser.evaluate( "render();" );
     } else {
       wantsToStart = true;
     }
   }
 }
