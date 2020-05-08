 /*******************************************************************************
  * JBoss, Home of Professional Open Source
  * Copyright 2010, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  *******************************************************************************/
 package org.richfaces.tests.metamer.ftest.richTreeModelAdaptor;
 
 import static org.jboss.test.selenium.guard.request.RequestTypeGuardFactory.guardXhr;
 import static org.jboss.test.selenium.locator.LocatorFactory.jq;
 import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
 
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.richfaces.tests.metamer.ftest.annotations.Inject;
 import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
 import org.richfaces.tests.metamer.ftest.annotations.Use;
 import org.richfaces.tests.metamer.ftest.annotations.Uses;
 import org.richfaces.tests.metamer.ftest.richTree.TestTreeSelection;
 import org.richfaces.tests.metamer.ftest.richTree.TreeAttributes;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * @author <a href="mailto:lfryc@redhat.com">Lukas Fryc</a>
  * @version $Revision$
  */
 @IssueTracking("https://issues.jboss.org/browse/RF-10497")
 public class TestTreeModelAdaptorSelection extends TestTreeSelection {
 
     @Inject
     PathsCrate paths;
     PathsCrate pathsForListModel = new PathsCrate("listModel", new Integer[][] { { 2, 1, 3 }, { 3, 3, 2, 2 } });
     PathsCrate pathsForMapModel = new PathsCrate("mapModel", new Integer[][] { { 2, 1, 5 }, { 3, 3, 2, 6 } });
     PathsCrate pathsForRecursiveModel = new PathsCrate("recursiveModel", new Integer[][] { { 4, 1, 10, 2 },
         { 1, 4, 3, 11, 4, 1 } });
 
     @Inject
     @Use(enumeration = true)
     public RecursiveModelRepresentation representation;
 
     @Inject
     @Use(booleans = { true, false })
     public boolean recursiveLeafChildrenNullable;
 
     public URL getTestUrl() {
         return buildUrl(contextPath, "http://localhost:8080/metamer/faces/components/richTree/treeAdaptors.xhtml");
     }
 
     @BeforeClass
     public void initTreeAttributes() {
         treeAttributes = new TreeAttributes(jq("span[id*=treeAttributes]"));
     }
 
     @BeforeMethod
     public void initPathsAndModelRepresentation() {
         if (paths != null) {
             selectionPaths = paths.paths;
         }
         if (representation == RecursiveModelRepresentation.MAP) {
             guardXhr(selenium).click(pjq(":radio[id*=recursiveModelRepresentation]").getNthOccurence(2));
         }
         if (recursiveLeafChildrenNullable) {
             guardXhr(selenium).click(pjq(":checkbox[id$=recursiveLeafChildrenNullable]"));
         }
     }
 
     @Test
     @Override
     @Use(field = "paths", empty = true)
     public void testTopLevelSelection() {
         super.testTopLevelSelection();
     }
 
     @Test
     @Override
     @Use(field = "paths", value = "paths*")
     public void testSubNodesSelection() {
         super.testSubNodesSelection();
     }
 
     @Test
     @Override
     @Uses({ @Use(field = "paths", value = "paths*"),
         @Use(field = "selectionType", value = "eventEnabledSelectionTypes") })
     public void testSubNodesSelectionEvents() {
         super.testSubNodesSelectionEvents();
     }
 
     @Override
     protected void expandAll() {
         for (Integer[] path : selectionPaths) {
             treeNode = null;
             for (int i = 0; i < path.length; i++) {
                 int index = path[i];
                 treeNode = (treeNode == null) ? tree.getNode(index) : treeNode.getNode(index);
                 if (i < path.length - 1) {
                     treeNode.expand();
                 }
             }
         }
     }
 
     @Override
     protected Integer[] getIntsFromString(String string) {
         Pattern pattern = Pattern.compile("(?:\\{[^}]+modelKey=(\\d+)\\})");
         Matcher matcher = pattern.matcher(string);
         List<Integer> list = new LinkedList<Integer>();
         while (matcher.find()) {
             int integer = Integer.valueOf(matcher.group(1)) + 1;
             integer = fixShiftWhenModelPresent(list, integer);
             list.add(integer);
         }
         if (list.isEmpty()) {
             throw new IllegalStateException("selection string does not match pattern: " + string);
         }
         return list.toArray(new Integer[list.size()]);
     }
 
     private Integer fixShiftWhenModelPresent(List<Integer> list, int integer) {
         if (!list.isEmpty()) {
             if (list.get(0) % 2 == 0) {
                 if (list.size() == 2) {
                     if (paths.toString().equals(pathsForRecursiveModel.toString())) {
                         return integer + 7;
                     } else if (paths.toString().equals(pathsForMapModel.toString())) {
                         return integer + 3;
                     }
                 }
             } else {
                 if (list.size() == 3) {
                     if (paths.toString().equals(pathsForRecursiveModel.toString())) {
                         return integer + 7;
                     } else if (paths.toString().equals(pathsForMapModel.toString())) {
                         return integer + 3;
                     }
                 }
             }
         }
         return integer;
     }
 
     private class PathsCrate {
         String name;
         Integer[][] paths;
 
         public PathsCrate(String name, Integer[][] paths) {
             this.name = name;
             this.paths = paths;
         }
 
         public String toString() {
             return name;
         }
     }
 }
