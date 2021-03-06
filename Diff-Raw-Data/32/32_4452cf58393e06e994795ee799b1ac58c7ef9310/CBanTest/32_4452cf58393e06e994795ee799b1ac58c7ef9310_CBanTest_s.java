 /*
  * Copyright (c) 2013 University of Nice Sophia-Antipolis
  *
  * This file is part of btrplace.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package btrplace.solver.choco.constraint;
 
 import btrplace.model.*;
 import btrplace.model.constraint.Ban;
 import btrplace.model.constraint.Online;
 import btrplace.model.constraint.Running;
 import btrplace.model.constraint.SatConstraint;
 import btrplace.plan.ReconfigurationPlan;
 import btrplace.solver.SolverException;
 import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
 import btrplace.solver.choco.MappingFiller;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Unit tests for {@link CBan}.
  *
  * @author Fabien Hermenier
  */
 public class CBanTest {
 
     @Test
     public void testBasic() throws SolverException {
         Node[] nodes = new Node[5];
         VM[] vms = new VM[5];
         Model mo = new DefaultModel();
         Mapping m = mo.getMapping();
         Set<VM> sVMs = new HashSet<>();
         Set<Node> sNodes = new HashSet<>();
         for (int i = 0; i < vms.length; i++) {
             nodes[i] = mo.newNode();
             vms[i] = mo.newVM();
             m.addOnlineNode(nodes[i]);
             m.addRunningVM(vms[i], nodes[i]);
             if (i % 2 == 0) {
                 sVMs.add(vms[i]);
                 sNodes.add(nodes[i]);
             }
         }
         Ban b = new Ban(sVMs, sNodes);
         Collection<SatConstraint> s = new HashSet<>();
         s.add(b);
         s.add(new Running(m.getAllVMs()));
         s.add(new Online(m.getAllNodes()));
 
         DefaultChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
         cra.labelVariables(true);
         cra.setTimeLimit(-1);
         ReconfigurationPlan p = cra.solve(mo, s);
         Assert.assertNotNull(p);
         System.out.println(p);

         Assert.assertEquals(3, p.getSize());
     }
 
     /**
      * Test getMisPlaced() in various situations.
      */
     @Test
     public void testGetMisPlaced() {
         Model mo = new DefaultModel();
         VM vm1 = mo.newVM();
         VM vm2 = mo.newVM();
         VM vm3 = mo.newVM();
         VM vm4 = mo.newVM();
         VM vm5 = mo.newVM();
         Node n1 = mo.newNode();
         Node n2 = mo.newNode();
         Node n3 = mo.newNode();
         Node n4 = mo.newNode();
         Node n5 = mo.newNode();
         Mapping m = new MappingFiller(mo.getMapping()).on(n1, n2, n3, n4, n5)
                 .run(n1, vm1, vm2)
                 .run(n2, vm3)
                 .run(n3, vm4)
                 .sleep(n4, vm5).get();
 
         Set<VM> vms = new HashSet<>(Arrays.asList(vm1, vm2));
         Set<Node> ns = new HashSet<>(Arrays.asList(n3, n4));
 
         CBan c = new CBan(new Ban(vms, ns));
         org.testng.Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
         ns.add(mo.newNode());
         org.testng.Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
         vms.add(mo.newVM());
         org.testng.Assert.assertTrue(c.getMisPlacedVMs(mo).isEmpty());
         ns.add(n1);
         Set<VM> bad = c.getMisPlacedVMs(mo);
         org.testng.Assert.assertEquals(2, bad.size());
         org.testng.Assert.assertTrue(bad.contains(vm1) && bad.contains(vm2));
     }
 }
