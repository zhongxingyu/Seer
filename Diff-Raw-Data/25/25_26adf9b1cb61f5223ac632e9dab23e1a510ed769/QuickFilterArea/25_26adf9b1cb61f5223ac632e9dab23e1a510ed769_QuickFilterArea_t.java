 /*******************************************************************************
  * Copyright (c) 2011 Ralf Sternberg.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ralf Sternberg - initial implementation and API
  ******************************************************************************/
 package ralfstx.mylyn.bugview.internal;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.mylyn.tasks.core.ITask;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.hamcrest.CoreMatchers;
 import org.hamcrest.Matcher;
 
 
 public class QuickFilterArea extends Composite {
 
   private final Set<Matcher<? extends ITask>> matchers;
   private Matcher<ITask> aggregatedMatcher;
   private Runnable matcherChangedListener;
 
   public QuickFilterArea( Composite parent, int style ) {
     super( parent, style );
     matchers = new HashSet<Matcher<? extends ITask>>();
     RowLayout layout = new RowLayout( SWT.HORIZONTAL );
     layout.fill = true;
    layout.marginLeft = 2;
    layout.marginRight = 2;
    layout.marginTop = 2;
    layout.marginBottom = 2;
    layout.spacing = 2;
     super.setLayout( layout );
   }
 
   public void createToolBar( QuickFilterContribution... contributions ) {
     ToolBar toolBar = new ToolBar( this, SWT.HORIZONTAL | SWT.FLAT );
     for( QuickFilterContribution contribution : contributions ) {
       addToolBarItem( toolBar, contribution );
     }
   }
 
   public Matcher<ITask> getMatcher() {
     return aggregatedMatcher;
   }
 
   public void setMatcherChangedListener( Runnable runnable ) {
     matcherChangedListener = runnable;
   }
 
   @Override
   public void setLayout( Layout layout ) {
     // prevent layout changes from outside
   }
 
   private void addToolBarItem( ToolBar parent, QuickFilterContribution contribution ) {
     final ToolItem item = new ToolItem( parent, SWT.CHECK );
    item.setImage( contribution.image.createImage() );
    item.setToolTipText( contribution.text );
     final Matcher<ITask> matcher = contribution.matcher;
     item.addSelectionListener( new SelectionAdapter() {
       @Override
       public void widgetSelected( SelectionEvent e ) {
         if( item.getSelection() ) {
           matchers.add( matcher );
         } else {
           matchers.remove( matcher );
         }
         refreshFilter();
       }
     } );
   }
 
   private void refreshFilter() {
     aggregatedMatcher = CoreMatchers.allOf( matchers );
     if( matcherChangedListener != null ) {
       matcherChangedListener.run();
     }
   }
 
 }
