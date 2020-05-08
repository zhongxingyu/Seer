 /*
  * Copyright (C) 2011 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.jamgotchian.abcd.core.controlflow;
 
 import fr.jamgotchian.abcd.core.ABCDContext;
 import fr.jamgotchian.abcd.core.util.SimplestFormatter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class RPSTTest {
 
     private static final Logger logger = Logger.getLogger(RPSTTest.class.getName());
 
     public RPSTTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         // root logger configuration
         Logger rootLogger = Logger.getLogger(ABCDContext.class.getPackage().getName());
         ConsoleHandler handler = new ConsoleHandler();
         handler.setFormatter(new SimplestFormatter());
         handler.setLevel(Level.FINEST);
         rootLogger.setLevel(Level.ALL);
         rootLogger.addHandler(handler);
         rootLogger.setUseParentHandlers(false);
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
         Logger rootLogger = Logger.getLogger(ABCDContext.class.getPackage().getName());
         for (Handler handler : rootLogger.getHandlers()) {
             handler.close();
         }
 
         Thread.sleep(1000);
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     @Test
     public void testCase1() throws IOException {
         BasicBlock a = new BasicBlockTestImpl("a");
         BasicBlock b = new BasicBlockTestImpl("b");
         BasicBlock c = new BasicBlockTestImpl("c");
         BasicBlock d = new BasicBlockTestImpl("d");
         BasicBlock e = new BasicBlockTestImpl("e");
         BasicBlock f = new BasicBlockTestImpl("f");
         BasicBlock g = new BasicBlockTestImpl("g");
         BasicBlock h = new BasicBlockTestImpl("h");
         BasicBlock i = new BasicBlockTestImpl("i");
         BasicBlock j = new BasicBlockTestImpl("j");
         BasicBlock k = new BasicBlockTestImpl("k");
         BasicBlock l = new BasicBlockTestImpl("l");
         ControlFlowGraphImpl cfg = new ControlFlowGraphImpl("Test", a, e);
         cfg.addBasicBlock(b);
         cfg.addBasicBlock(c);
         cfg.addBasicBlock(d);
         cfg.addBasicBlock(f);
         cfg.addBasicBlock(g);
         cfg.addBasicBlock(h);
         cfg.addBasicBlock(i);
         cfg.addBasicBlock(j);
         cfg.addBasicBlock(k);
         cfg.addBasicBlock(l);
         cfg.addEdge(a, g);
         cfg.addEdge(g, b);
         cfg.addEdge(g, l);
         cfg.addEdge(b, f);
         cfg.addEdge(b, c);
         cfg.addEdge(f, c);
         cfg.addEdge(c, d);
         cfg.addEdge(l, d);
         cfg.addEdge(d, g);
         cfg.addEdge(d, e);
         cfg.addEdge(a, h);
         cfg.addEdge(h, i);
         cfg.addEdge(h, j);
         cfg.addEdge(i, j);
         cfg.addEdge(j, i);
         cfg.addEdge(i, k);
         cfg.addEdge(j, k);
         cfg.addEdge(k, e);
         cfg.updateDominatorInfo();
         cfg.performDepthFirstSearch();
 
         Writer writer = new FileWriter("/tmp/RPSTTest_CFG.dot");
         try {
             cfg.export(writer, new BasicBlockRangeAttributeFactory(),
                                new EdgeAttributeFactory(false));
         } finally {
             writer.close();
         }
         RPST rpst = new RPST(cfg);
         writer = new FileWriter("/tmp/RPSTTest_RPST.dot");
         try {
             rpst.export(writer);
         } finally {
             writer.close();
         }
 
     }
 }
