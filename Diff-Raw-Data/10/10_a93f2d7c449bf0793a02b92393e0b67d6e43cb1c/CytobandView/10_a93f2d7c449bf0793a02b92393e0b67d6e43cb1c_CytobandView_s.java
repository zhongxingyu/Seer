 /*
  *   cytoband View
  *   http://casmi.github.com/
  *   Copyright (C) 2011, Xcoo, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jp.xcoo.casmi.cytobandview.view;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import jp.xcoo.casmi.cytobandview.data.Chromosome;
 import jp.xcoo.casmi.cytobandview.data.Cytoband;
 import jp.xcoo.casmi.cytobandview.net.CytobandLoader;
 import casmi.Applet;
 import casmi.AppletRunner;
 import casmi.KeyEvent;
 import casmi.MouseButton;
 import casmi.MouseEvent;
 import casmi.graphics.color.ColorSet;
 import casmi.graphics.element.Element;
import casmi.graphics.element.MouseOverCallback;
 import casmi.graphics.element.Text;
 import casmi.graphics.font.Font;
 import casmi.graphics.group.Group;
 
 /**
  * cytoband view
  * Class for viewer for cytoband (Human Genome. UCSC hg.19)
  *
  * @author K. Nishimura
  * @author Takashi AOKI <federkasten@me.com>
  *
  */
 
 public class CytobandView extends Applet {
 
 	private static final String TITLE_NAME = "Cytoband View";
 
 	private static final String REMOTE_DATA_URL = "http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/cytoBand.txt.gz";
 
 	private Font f;
     private Text out;
 
     private final double X_STEP = 40;
 
     private List<CytobandElement> bandElementList = new ArrayList<CytobandElement>();
 
     @Override
     public void setup() {
         setSize(1024, 768);
         setFPS(30.0);
 
     	final Map<String, Chromosome> chrMap = CytobandLoader.load(REMOTE_DATA_URL);
 
     	Group elementGroup = new Group();
 
     	// build render elements
     	for( String name : Chromosome.CHROMOSOME_NAMES ) {
     		Chromosome chr = chrMap.get(name);
 
     		for( Cytoband cb : chr.getBands() ) {
     		    CytobandElement e = new CytobandElement(cb, cb.getChromosomeNumber() * X_STEP, - chr.getLength() / 2);
 
     		    e.addMouseEventCallback( new MouseOverCallback() {
 
                     @Override
                    public void run(MouseOverTypes eventtype, Element element) {
                         CytobandElement e = (CytobandElement) element;
 
                        switch(eventtype) {
                         case ENTERED:
                             e.setSelected(true);
                             break;
 
                         case EXITED:
                             e.setSelected(false);
                             break;
                         default:
                             break;
                         }
 
                     }
                 });
 
         		bandElementList.add(e);
         		elementGroup.add(e);
     		}
     	}
 
     	elementGroup.setPosition(0, 384);
     	addObject(elementGroup);
 
         f = new Font("San-Serif");
         f.setSize(20);
 
         out = new Text("                                  ", f, 450, 730);
         out.setStrokeColor(ColorSet.WHITE);
 
         addObject(out);
     }
 
     @Override
     public void update() {
         CytobandElement selected = null;
 
         for (CytobandElement e : bandElementList) {
             if (e.isSelected()) {
                 selected = e;
             }
         }
 
         if (selected != null) {
             out.setText(selected.getName());
         } else {
             out.setText(TITLE_NAME);
         }
     }
 
     @Override
     public void exit() {}
 
     @Override
     public void mouseEvent(MouseEvent e, MouseButton b) {
     }
 
     @Override
     public void keyEvent(KeyEvent e) {
         if (e == KeyEvent.PRESSED) {
             if (getKeyCode() == 27) {
                 System.exit(0);
             }
         }
     }
 
     public static void main(String args[]) {
         AppletRunner.run("jp.xcoo.casmi.cytobandview.view.CytobandView", TITLE_NAME);
     }
 }
