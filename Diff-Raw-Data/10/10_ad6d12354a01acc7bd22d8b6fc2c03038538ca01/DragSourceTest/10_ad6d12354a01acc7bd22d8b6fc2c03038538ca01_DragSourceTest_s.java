 /* 
  * This file is part of the Echo2 Extras Project.
  * Copyright (C) 2005-2006 NextApp, Inc.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package nextapp.echo2.extras.testapp.testscreen;
 
 import nextapp.echo2.app.Border;
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.Column;
 import nextapp.echo2.app.Label;
 import nextapp.echo2.app.Row;
 import nextapp.echo2.extras.app.DragSource;
 import nextapp.echo2.extras.app.event.DropEvent;
 import nextapp.echo2.extras.app.event.DropListener;
 import nextapp.echo2.extras.testapp.AbstractTest;
 import nextapp.echo2.extras.testapp.Styles;
 
 /**
  * Interactive test module for <code>ColorSelect</code>s.
  */
 public class DragSourceTest extends AbstractTest {
     
     public DragSourceTest() {
         super("DragSource", Styles.ICON_16_DRAG_SOURCE);
         
         Row row = new Row();
         add(row);
         setTestComponent(this, row);
         
         final Column column1 = new Column();
         column1.setBorder(new Border(2, Color.BLUE, Border.STYLE_INSET));
         final Column column2 = new Column();
         column2.setBorder(new Border(2, Color.RED, Border.STYLE_OUTSET));
         
         row.add(column1);
         row.add(column2);
         
        column2.add(new Label("Drag Components Here"));
        
         for (int i=0; i<6; i++) {
         	Label label = new Label("Draggable Label " + i);
         	DragSource ds = new DragSource(label);
         	ds.addDropTarget(column2);
         	ds.addDropTargetListener(new DropListener(){
     			public void dropPerformed(DropEvent event) {
     				DragSource dragged = (DragSource)event.getSource();
     				column1.remove(dragged);
     				column2.add(dragged.getComponent(0));
     			}
     		});
         	column1.add(ds);
         }
                  
         addStandardIntegrationTests();
     }
     
 }
